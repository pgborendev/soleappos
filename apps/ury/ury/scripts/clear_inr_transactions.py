"""
Remove all INR-currency transactions so the site can be
re-configured with KHR as the base currency.

Run with:
    bench --site soleap.local execute ury.scripts.clear_inr_transactions.run
"""

import frappe


def run():
    site = frappe.local.site
    print(f"[clear_inr] site: {site}")

    # Order matters: children / dependents before parents,
    # submitted docs must be cancelled before deletion.

    # ── URY transaction doctypes ──────────────────────────────────────────
    ury_tx = [
        "URY KOT Items",
        "URY KOT",
        "URY KOT Error Log",
        "URY Order Item",
        "URY Order",
        "URY Daily P and L",
        "URY Cost of Goods",
        "URY Materials",
        "URY P and L Breakup",
        "URY P and L Materials",
        "URY Variable Expenses",
        "URY Fixed Expenses",
    ]

    # ── ERPNext / Frappe transaction doctypes ─────────────────────────────
    erpnext_tx = [
        # POS session
        "Sub POS Invoices",
        "Sub POS Closing Payment",
        "Sub POS Closing",
        "POS Invoice Item",
        "POS Invoice Merge Log",
        "POS Closing Entry Detail",
        "POS Closing Entry Taxes",
        "POS Closing Entry",
        "POS Opening Entry Detail",
        "POS Opening Entry",
        # Accounting
        "Payment Ledger Entry",
        "GL Entry",
        "Sales Invoice Item",
        "Sales Invoice",
        "POS Invoice",
        # Stock
        "Stock Ledger Entry",
        "Stock Entry Detail",
        "Stock Entry",
    ]

    def _delete_all(doctype):
        names = frappe.get_all(doctype, pluck="name")
        if not names:
            return 0
        for name in names:
            try:
                frappe.delete_doc(doctype, name, force=True, ignore_permissions=True)
            except Exception as e:
                print(f"  [warn] could not delete {doctype} {name}: {e}")
        return len(names)

    def _delete_inr(doctype, currency_field="currency"):
        names = frappe.get_all(
            doctype,
            filters={currency_field: "INR"},
            pluck="name",
        )
        if not names:
            return 0
        for name in names:
            try:
                doc = frappe.get_doc(doctype, name)
                if getattr(doc, "docstatus", 0) == 1:
                    doc.cancel()
                frappe.delete_doc(doctype, name, force=True, ignore_permissions=True)
            except Exception as e:
                print(f"  [warn] could not delete {doctype} {name}: {e}")
        return len(names)

    total = 0

    print("\n── URY transaction records ──────────────────────────────────────")
    for dt in ury_tx:
        try:
            n = _delete_all(dt)
            if n:
                print(f"  deleted {n:>4}  {dt}")
            total += n
        except Exception as e:
            print(f"  [skip] {dt}: {e}")

    print("\n── ERPNext INR-currency records ─────────────────────────────────")
    for dt in erpnext_tx:
        try:
            # GL Entry uses 'account_currency'; everything else uses 'currency'
            field = "account_currency" if dt == "GL Entry" else "currency"
            n = _delete_inr(dt, field)
            if n:
                print(f"  deleted {n:>4}  {dt}")
            total += n
        except Exception as e:
            print(f"  [skip] {dt}: {e}")

    # Currency Exchange records for INR
    print("\n── Currency Exchange (INR) ───────────────────────────────────────")
    for rec in frappe.get_all(
        "Currency Exchange",
        filters=[["from_currency", "in", ["INR"]]],
        pluck="name",
    ):
        frappe.delete_doc("Currency Exchange", rec, force=True, ignore_permissions=True)
        print(f"  deleted Currency Exchange {rec}")
        total += 1

    frappe.db.commit()
    print(f"\n[clear_inr] done — {total} records removed.")
    print("Next steps:")
    print("  1. Set Company default currency → KHR")
    print("  2. Set Price List currency      → KHR")
    print("  3. Set POS Profile currency     → KHR")
    print("  4. Set Global Defaults currency → KHR")
