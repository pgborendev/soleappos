# Soleap POS

A restaurant point-of-sale system built on [Frappe / ERPNext](https://frappeframework.com), with a custom React web POS frontend, Android wrapper app, and Telegram bot integration.

**Repository:** https://github.com/pgborendev/soleappos

---

## Table of Contents

- [Stack Overview](#stack-overview)
- [Project Structure](#project-structure)
- [Prerequisites](#prerequisites)
- [Development Setup](#development-setup)
- [Building the Web POS](#building-the-web-pos)
- [Site Configuration](#site-configuration)
- [Telegram Bot Configuration](#telegram-bot-configuration)
- [Internationalisation (EN / KH)](#internationalisation-en--kh)
- [Android App](#android-app)
- [Useful Commands](#useful-commands)

---

## Stack Overview

| Layer | Technology |
|---|---|
| Backend framework | Frappe 15 + ERPNext 15 + HRMS |
| Custom app | `ury` (this repo, `apps/ury/`) |
| Web POS frontend | React 19 + Vite 8 + TypeScript + Tailwind CSS 3 |
| State management | Zustand 5 |
| i18n | i18next 26 + react-i18next 17 |
| Database | MariaDB |
| Cache / Queue | Redis (3 instances: cache, queue, socketio) |
| Android | Android WebView wrapper (`apps/ury/ury_android/`) |

---

## Project Structure

```
soleappos/                        ← bench root (git repo)
├── apps/
│   └── ury/
│       ├── ury/                  ← Frappe app (Python backend)
│       │   ├── hooks.py          ← doc_events, scheduler hooks
│       │   ├── ury/
│       │   │   ├── api/          ← whitelisted Python APIs
│       │   │   │   ├── telegram_bot.py      ← Telegram bot logic
│       │   │   │   ├── telegram_webhook.py  ← webhook endpoint
│       │   │   │   └── ury_kot_*.py         ← KOT print/notify
│       │   │   └── doctype/      ← Frappe doctypes
│       │   └── ury_pos/
│       │       └── api.py        ← POS menu / order sync APIs
│       ├── pos/                  ← React web POS (Vite app)
│       │   ├── src/
│       │   │   ├── components/   ← React components
│       │   │   ├── pages/        ← POS / Orders / Table pages
│       │   │   ├── store/        ← Zustand stores
│       │   │   ├── i18n/
│       │   │   │   ├── index.ts            ← i18n setup + setLanguage()
│       │   │   │   └── locales/
│       │   │   │       ├── en.ts           ← English strings
│       │   │   │       └── km.ts           ← Khmer strings
│       │   │   └── assets/fonts/
│       │   │       └── KhmerOSSiemreap.ttf ← Khmer font (bundled)
│       │   ├── public/
│       │   │   └── soleap_pos.png          ← Header logo
│       │   └── vite.config.ts
│       └── ury_android/          ← Android WebView wrapper
├── sites/
│   ├── common_site_config.json   ← bench-wide settings
│   └── soleap.local/             ← site directory (not tracked in git)
│       └── site_config.json      ← per-site secrets + custom config
├── Procfile                      ← bench process definitions
└── README.md
```

---

## Prerequisites

| Requirement | Version |
|---|---|
| Python | 3.11+ |
| Node.js | **24.x** (via nvm — Vite 8 requires Node ≥ 20) |
| Yarn | 1.22+ |
| MariaDB | 10.6+ |
| Redis | 6+ |
| bench CLI | latest |

> Node 18 (system default on many distros) **cannot** build this project — Vite 8 requires Node ≥ 20.
> Use nvm: `export PATH=~/.nvm/versions/node/v24.13.1/bin:$PATH`

---

## Development Setup

### 1. Clone and init bench

```bash
bench init soleappos --frappe-branch version-15
cd soleappos
bench get-app erpnext --branch version-15
bench get-app hrms --branch version-15
# clone this repo into apps/ury
git clone https://github.com/pgborendev/soleappos.git apps/ury
bench install-app ury
```

### 2. Create the site

```bash
bench new-site soleap.local --install-app erpnext --install-app hrms --install-app ury
bench use soleap.local
```

### 3. Start the bench

```bash
bench start
# web server runs on http://localhost:8001
```

### 4. Run the React POS dev server

```bash
cd apps/ury/pos
export PATH=~/.nvm/versions/node/v24.13.1/bin:$PATH
yarn install --ignore-engines
yarn dev          # http://localhost:8080/pos
```

The dev server proxies `/api`, `/assets`, and `/files` to `http://localhost:8001` so the POS connects to the live Frappe backend.

---

## Building the Web POS

```bash
cd apps/ury/pos
export PATH=~/.nvm/versions/node/v24.13.1/bin:$PATH
yarn build
```

Output is written to `apps/ury/ury/public/pos/` and served by Frappe at `/pos`.

---

## Site Configuration

### `sites/common_site_config.json`

Bench-wide settings (tracked in git):

```json
{
  "background_workers": 1,
  "default_site": "soleap.local",
  "webserver_port": 8001,
  "socketio_port": 9001,
  "redis_cache": "redis://localhost:6380",
  "redis_queue": "redis://localhost:6381",
  "redis_socketio": "redis://localhost:6382",
  "host": "0.0.0.0"
}
```

### `sites/soleap.local/site_config.json`

Per-site secrets — **not tracked in git**. Set values with `bench set-config`:

```bash
bench --site soleap.local set-config <key> <value>
```

| Key | Description |
|---|---|
| `db_name` | MariaDB database name |
| `db_type` | `mariadb` |
| `root_login` | MariaDB root user |
| `encryption_key` | Frappe encryption key (auto-generated) |

---

## Telegram Bot Configuration

The Telegram integration lives in `apps/ury/ury/ury/api/telegram_bot.py` and `telegram_webhook.py`.

### What it does

- **Staff notifications** — every new POS Invoice triggers a message to the configured staff group (order number, type, table, items, total)
- **Customer ordering bot** — customers `/start` the bot, pick order type (Dine In / Take Away / Delivery), browse the active menu by category, build a cart, and confirm → creates a `POS Invoice` in Frappe automatically

### Setup steps

**1. Create the bot via @BotFather**

Open Telegram → search `@BotFather` → send `/newbot` → follow prompts → copy the token.

**2. Add bot settings to site config**

```bash
bench --site soleap.local set-config telegram_bot_token    "123456:ABCdef..."
bench --site soleap.local set-config telegram_staff_chat_id "-1001234567890"
bench --site soleap.local set-config telegram_pos_profile  "Your POS Profile Name"
bench --site soleap.local set-config telegram_bot_user     "Administrator"
```

| Key | Description |
|---|---|
| `telegram_bot_token` | Token from @BotFather |
| `telegram_staff_chat_id` | Chat/group ID to receive staff notifications (add `@userinfobot` to the group to get it — usually a negative number) |
| `telegram_pos_profile` | Frappe POS Profile used when creating orders from the bot |
| `telegram_bot_user` | Frappe user the bot acts as when inserting invoices |

**3. Register the webhook URL** (requires a public HTTPS domain)

```bash
bench --site soleap.local execute ury.ury.api.telegram_webhook.setup_webhook \
  --kwargs '{"webhook_url": "https://yourdomain.com/api/method/ury.ury.api.telegram_webhook.handle"}'
```

**4. Restart bench**

```bash
bench restart
```

### Bot commands (customer-facing)

| Command | Action |
|---|---|
| `/start` | Start a new order — choose order type |
| `/cart` | View current cart and confirm or remove items |
| `/cancel` | Clear cart and start over |

---

## Internationalisation (EN / KH)

The web POS supports English and Khmer. Language is toggled from the user dropdown menu (top-right avatar).

### How it works

- Language preference stored in `localStorage` under key `ury_language`
- `document.documentElement.lang` is set on init and on every switch
- CSS `html:lang(km) *` applies `KhmerOSSiemreap` font globally
- Translation keys live in:
  - `apps/ury/pos/src/i18n/locales/en.ts`
  - `apps/ury/pos/src/i18n/locales/km.ts`

### Adding a new string

1. Add the key to `en.ts` and `km.ts`
2. Use `const { t } = useTranslation()` in the component
3. Replace the hardcoded string with `{t('your_key')}`
4. Rebuild: `yarn build`

### Khmer font

`KhmerOSSiemreap.ttf` is bundled at `apps/ury/pos/src/assets/fonts/`. Source: [KhmerOS fonts](https://sourceforge.net/projects/khmer/).

---

## Android App

Located at `apps/ury/ury_android/`. An Android WebView wrapper that loads the Frappe site URL.

Build with Android Studio or:

```bash
cd apps/ury/ury_android
./gradlew assembleDebug
```

APK output: `app/build/outputs/apk/debug/app-debug.apk`

Khmer locale strings: `app/src/main/res/values-km/strings.xml`

---

## Useful Commands

```bash
# Start all bench processes
bench start

# Restart after Python changes
bench restart

# Clear Frappe cache
bench --site soleap.local clear-cache

# Run migrations after pulling changes
bench --site soleap.local migrate

# Build assets (Python/JS for the Frappe app)
bench build --app ury

# Build the React POS (requires Node 24)
cd apps/ury/pos
export PATH=~/.nvm/versions/node/v24.13.1/bin:$PATH
yarn build

# Open Frappe console
bench --site soleap.local console

# Check bench logs
tail -f logs/web.log
tail -f logs/worker.log
```
