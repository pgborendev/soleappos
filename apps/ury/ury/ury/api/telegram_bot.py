import json
import frappe
import requests

TELEGRAM_API = "https://api.telegram.org/bot{token}/{method}"
SESSION_TTL = 3600  # seconds


# ── config ────────────────────────────────────────────────────────────────────

def _token():
    return frappe.conf.get("telegram_bot_token", "")

def _staff_chat():
    return frappe.conf.get("telegram_staff_chat_id", "")

def _pos_profile_name():
    return frappe.conf.get("telegram_pos_profile", "")

def _bot_user():
    return frappe.conf.get("telegram_bot_user", "Administrator")


# ── Telegram API helpers ──────────────────────────────────────────────────────

def _call(method, **data):
    token = _token()
    if not token:
        return
    try:
        r = requests.post(
            TELEGRAM_API.format(token=token, method=method),
            json=data,
            timeout=10,
        )
        return r.json()
    except Exception as e:
        frappe.log_error(str(e), "Telegram API Error")


def send_message(chat_id, text, reply_markup=None, parse_mode="HTML"):
    kwargs = {"chat_id": chat_id, "text": text, "parse_mode": parse_mode}
    if reply_markup is not None:
        kwargs["reply_markup"] = reply_markup
    return _call("sendMessage", **kwargs)


def inline_kb(rows):
    """rows: list of lists of {"text": str, "callback_data": str}"""
    return {"inline_keyboard": rows}


def remove_kb():
    return {"remove_keyboard": True}


# ── session (Redis cache) ─────────────────────────────────────────────────────

def _skey(user_id):
    return f"tg_session_{user_id}"


def _empty_session():
    return {"state": "idle", "cart": {"order_type": None, "table": None, "items": []}}


def get_session(user_id):
    raw = frappe.cache().get_value(_skey(user_id))
    if not raw:
        return _empty_session()
    try:
        return json.loads(raw) if isinstance(raw, str) else raw
    except Exception:
        return _empty_session()


def save_session(user_id, session):
    frappe.cache().set_value(_skey(user_id), json.dumps(session), expires_in_sec=SESSION_TTL)


def clear_session(user_id):
    frappe.cache().delete_key(_skey(user_id))


# ── menu helpers ──────────────────────────────────────────────────────────────

def _get_menu_items():
    restaurant = frappe.db.get_value("URY Restaurant", {}, "name")
    if not restaurant:
        return []
    menu = frappe.db.get_value("URY Restaurant", restaurant, "active_menu")
    if not menu:
        return []
    return frappe.get_all(
        "URY Menu Item",
        filters={"parent": menu, "disabled": 0},
        fields=["item", "item_name", "rate", "course"],
        order_by="course asc, item_name asc",
    )


def _categories(items):
    seen, cats = set(), []
    for it in items:
        if it.course and it.course not in seen:
            cats.append(it.course)
            seen.add(it.course)
    return cats


# ── staff order notification ──────────────────────────────────────────────────

def notify_staff_new_order(doc, method=None):
    chat_id = _staff_chat()
    if not chat_id:
        return

    lines = "\n".join(
        f"  • {it.item_name}  ×{int(it.qty)}"
        for it in doc.get("items", [])
    )
    order_type = doc.get("order_type") or "—"
    table = doc.get("restaurant_table") or ""
    order_no = doc.get("custom_ury_order_number") or doc.name

    text = (
        f"🔔 <b>New Order #{order_no}</b>\n"
        f"📋 {order_type}"
        + (f"  |  Table: {table}" if table else "")
        + f"\n\n<b>Items:</b>\n{lines}\n\n"
        f"💰 Total: {doc.grand_total}"
    )
    send_message(chat_id, text)


# ── order creation ────────────────────────────────────────────────────────────

def create_order_from_bot(cart):
    pos_profile_name = _pos_profile_name()
    if not pos_profile_name:
        frappe.throw("telegram_pos_profile not configured in site_config.json")

    pos_profile = frappe.get_doc("POS Profile", pos_profile_name)
    restaurant = frappe.db.get_value("URY Restaurant", {"branch": pos_profile.branch}, "name")
    menu = frappe.db.get_value("URY Restaurant", restaurant, "active_menu")
    price_list = frappe.db.get_value("Price List", {"restaurant_menu": menu, "enabled": 1}, "name")
    tax_template = frappe.db.get_value("URY Restaurant", restaurant, "default_tax_template")

    invoice = frappe.new_doc("POS Invoice")
    invoice.is_pos = 1
    invoice.update_stock = 1
    invoice.pos_profile = pos_profile_name
    invoice.company = pos_profile.company
    invoice.branch = pos_profile.branch
    invoice.restaurant = restaurant
    invoice.order_type = cart.get("order_type", "Take Away")
    invoice.customer = pos_profile.customer
    invoice.selling_price_list = price_list
    invoice.taxes_and_charges = tax_template

    if cart.get("table"):
        invoice.restaurant_table = cart["table"]

    for ci in cart.get("items", []):
        invoice.append("items", {
            "item_code": ci["item"],
            "item_name": ci["item_name"],
            "qty": ci["qty"],
            "rate": ci["rate"],
            "uom": "Nos",
        })

    invoice.set_missing_values()
    invoice.calculate_taxes_and_totals()

    prev_user = frappe.session.user
    frappe.set_user(_bot_user())
    try:
        invoice.insert(ignore_permissions=True)
        frappe.db.commit()
    finally:
        frappe.set_user(prev_user)

    return invoice.name


# ── webhook dispatcher ────────────────────────────────────────────────────────

def handle_update(update):
    if "callback_query" in update:
        _on_callback(update["callback_query"])
    elif "message" in update:
        _on_message(update["message"])


# ── message handler ───────────────────────────────────────────────────────────

def _on_message(msg):
    chat_id = msg["chat"]["id"]
    user_id = msg["from"]["id"]
    first_name = msg["from"].get("first_name", "Customer")
    text = msg.get("text", "").strip()

    session = get_session(user_id)

    if text == "/start":
        clear_session(user_id)
        send_message(
            chat_id,
            f"👋 Welcome to <b>Soleap POS</b>, {first_name}!\n\nHow would you like to order?",
            reply_markup=inline_kb([
                [{"text": "🍽 Dine In",    "callback_data": "otype:Dine In"}],
                [{"text": "🥡 Take Away",  "callback_data": "otype:Take Away"}],
                [{"text": "🛵 Delivery",   "callback_data": "otype:Delivery"}],
            ]),
        )
        return

    if text == "/cart":
        _show_cart(chat_id, user_id)
        return

    if text in ("/cancel", "/clear"):
        clear_session(user_id)
        send_message(chat_id, "🗑 Cart cleared. Send /start to order again.", reply_markup=remove_kb())
        return

    if session["state"] == "entering_table":
        if not frappe.db.exists("URY Table", text):
            send_message(chat_id, "❌ Table not found. Please enter a valid table name (e.g. <b>T01</b>):")
            return
        session["cart"]["table"] = text
        session["state"] = "browsing"
        save_session(user_id, session)
        _show_categories(chat_id, session)
        return

    send_message(
        chat_id,
        "Send /start to begin, /cart to review your order, or /cancel to clear.",
        reply_markup=remove_kb(),
    )


# ── callback handler ──────────────────────────────────────────────────────────

def _on_callback(cb):
    chat_id = cb["message"]["chat"]["id"]
    user_id = cb["from"]["id"]
    data = cb.get("data", "")

    _call("answerCallbackQuery", callback_query_id=cb["id"])

    session = get_session(user_id)

    if data.startswith("otype:"):
        order_type = data[6:]
        session["cart"] = {"order_type": order_type, "table": None, "items": []}
        if order_type == "Dine In":
            session["state"] = "entering_table"
            save_session(user_id, session)
            send_message(chat_id, "🪑 Please type your table name (e.g. <b>T01</b>):")
        else:
            session["state"] = "browsing"
            save_session(user_id, session)
            _show_categories(chat_id, session)

    elif data.startswith("cat:"):
        _show_items(chat_id, data[4:])

    elif data.startswith("add:"):
        item_code = data[4:]
        items = _get_menu_items()
        found = next((i for i in items if i.item == item_code), None)
        if found:
            cart_items = session["cart"]["items"]
            existing = next((ci for ci in cart_items if ci["item"] == item_code), None)
            if existing:
                existing["qty"] += 1
            else:
                cart_items.append({
                    "item": found.item,
                    "item_name": found.item_name,
                    "qty": 1,
                    "rate": float(found.rate or 0),
                })
            save_session(user_id, session)
            send_message(chat_id, f"✅ <b>{found.item_name}</b> added.\n\n/cart to review  |  /cancel to clear")

    elif data.startswith("rm:"):
        item_code = data[3:]
        session["cart"]["items"] = [ci for ci in session["cart"]["items"] if ci["item"] != item_code]
        save_session(user_id, session)
        _show_cart(chat_id, user_id)

    elif data == "back_cats":
        session["state"] = "browsing"
        save_session(user_id, session)
        _show_categories(chat_id, session)

    elif data == "clear_cart":
        session["cart"]["items"] = []
        save_session(user_id, session)
        send_message(chat_id, "🗑 Cart cleared.")

    elif data == "confirm_order":
        _place_order(chat_id, user_id, session)


# ── UI builders ───────────────────────────────────────────────────────────────

def _show_categories(chat_id, session):
    items = _get_menu_items()
    cats = _categories(items)
    if not cats:
        send_message(chat_id, "❌ No menu available right now. Please try again later.")
        return
    order_type = session["cart"].get("order_type", "")
    table = session["cart"].get("table", "")
    header = f"📋 <b>Menu</b> – {order_type}" + (f"  |  Table {table}" if table else "") + "\n\nSelect a category:"
    send_message(chat_id, header, reply_markup=inline_kb(
        [[{"text": c, "callback_data": f"cat:{c}"}] for c in cats]
    ))


def _show_items(chat_id, category):
    items = _get_menu_items()
    cat_items = [i for i in items if i.course == category]
    if not cat_items:
        send_message(chat_id, "No items in this category.")
        return
    buttons = [
        [{"text": f"{i.item_name}  –  {i.rate:.2f}", "callback_data": f"add:{i.item}"}]
        for i in cat_items
    ]
    buttons.append([{"text": "◀ Back to Categories", "callback_data": "back_cats"}])
    send_message(chat_id, f"🍽 <b>{category}</b>\n\nTap an item to add it to your cart:", reply_markup=inline_kb(buttons))


def _show_cart(chat_id, user_id):
    session = get_session(user_id)
    cart_items = session["cart"].get("items", [])
    if not cart_items:
        send_message(chat_id, "🛒 Your cart is empty. Send /start to begin.")
        return

    total = sum(ci["qty"] * ci["rate"] for ci in cart_items)
    lines = [f"  • {ci['item_name']}  ×{ci['qty']}  =  {ci['qty'] * ci['rate']:.2f}" for ci in cart_items]
    order_type = session["cart"].get("order_type", "")
    table = session["cart"].get("table", "")

    text = (
        f"🛒 <b>Your Cart</b>\n"
        f"{order_type}" + (f"  |  Table: {table}" if table else "")
        + "\n\n" + "\n".join(lines)
        + f"\n\n<b>Total: {total:.2f}</b>"
    )

    buttons = [[{"text": f"❌ {ci['item_name']}", "callback_data": f"rm:{ci['item']}"}] for ci in cart_items]
    buttons.append([
        {"text": "✅ Confirm Order", "callback_data": "confirm_order"},
        {"text": "🗑 Clear",         "callback_data": "clear_cart"},
    ])
    send_message(chat_id, text, reply_markup=inline_kb(buttons))


def _place_order(chat_id, user_id, session):
    if not session["cart"].get("items"):
        send_message(chat_id, "🛒 Your cart is empty.")
        return

    send_message(chat_id, "⏳ Placing your order, please wait...")
    try:
        invoice_name = create_order_from_bot(session["cart"])
        clear_session(user_id)
        send_message(
            chat_id,
            f"✅ <b>Order placed!</b>\n\nOrder number: <b>{invoice_name}</b>\n\nThank you! 🙏\n\nSend /start to place another order.",
            reply_markup=remove_kb(),
        )
    except Exception as e:
        frappe.log_error(str(e), "Telegram Bot Order Error")
        send_message(chat_id, "❌ Could not place order. Please contact staff.")
