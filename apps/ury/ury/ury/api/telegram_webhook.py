import json
import frappe


@frappe.whitelist(allow_guest=True)
def handle():
    """Telegram calls this endpoint for every incoming update."""
    from ury.ury.api.telegram_bot import handle_update

    raw = frappe.request.data
    if not raw:
        return {"ok": True}

    try:
        update = json.loads(raw)
        handle_update(update)
    except Exception as e:
        frappe.log_error(str(e), "Telegram Webhook Error")

    return {"ok": True}


@frappe.whitelist()
def setup_webhook(webhook_url):
    """
    Register your public HTTPS URL with Telegram.
    Call once from the Frappe console or bench execute:

        bench --site <site> execute ury.ury.api.telegram_webhook.setup_webhook \
            --kwargs '{"webhook_url": "https://yourdomain.com/api/method/ury.ury.api.telegram_webhook.handle"}'
    """
    import requests as req
    from ury.ury.api.telegram_bot import _token, TELEGRAM_API

    token = _token()
    if not token:
        frappe.throw("telegram_bot_token is not set in site_config.json")

    r = req.post(
        TELEGRAM_API.format(token=token, method="setWebhook"),
        json={"url": webhook_url},
        timeout=10,
    )
    result = r.json()
    if result.get("ok"):
        frappe.msgprint(f"Webhook registered: {webhook_url}")
    else:
        frappe.throw(f"Telegram error: {result.get('description')}")
    return result
