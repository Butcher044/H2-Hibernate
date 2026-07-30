# ComFlow — Архитектура и потоки

Живая карта того, **как всё работает на самом деле** (по коду, не по старым докам). Читается сверху вниз: сначала общая картина, потом отдельные потоки. Диаграммы — Mermaid, GitHub рисует их автоматически.

> Обновление: 2026-06-26. Если менял поток — поправь соответствующую диаграмму.

**Что за система.** ComFlow — SaaS для автоматизации Telegram (AI-комментирование, авто-ответы в ЛС, участие в группах, прогрев аккаунтов). Работает на клиентских Telegram-аккаунтах (session+json / tdata). Продаётся через Telegram Mini App с подписками и крипто-оплатой.

**Четыре процесса** (см. `docker-compose.yml`):
| Процесс | Что делает | Точка входа |
|---|---|---|
| **API** | REST для Mini App, ×2 uvicorn-воркера | `backend.main:app` |
| **Bot** | aiogram-бот (polling), кнопка Mini App, уведомления | `bot/main_bot.py` |
| **Worker** | оркестратор + Telethon-флот аккаунтов | `workers/orchestrator.py` |
| **Frontend** | React SPA, раздаётся через nginx | `frontend/` |

Инфраструктура: **PostgreSQL** (источник правды), **Redis** (кэш/локи/heartbeat/pub-sub логов), **Kafka** (команды и события между процессами).

---

## 0. Обзор системы

Как связаны процессы и данные.

```mermaid
flowchart TD
    U["👤 Пользователь Telegram"]
    TG["Telegram API / MTProto"]
    BOT["🤖 Bot — aiogram, polling"]
    NGINX["nginx"]
    SPA["📱 Mini App — React SPA"]
    API["⚙️ Backend API — FastAPI x2"]
    WORKER["🛠️ Worker / Orchestrator — Telethon-флот"]
    PG[("🗄️ PostgreSQL")]
    REDIS[("⚡ Redis")]
    KAFKA{{"📨 Kafka"}}
    PLATEGA["💳 Platega.io"]

    U -->|"/start, кнопки"| BOT
    U -->|"открывает приложение"| SPA
    BOT -->|"ставит WebApp-кнопку"| U
    SPA --> NGINX --> API
    SPA <-->|"WS: живые логи"| API
    API <-->|"read / write"| PG
    WORKER <-->|"источник правды"| PG
    API -->|"команды start/stop, tools"| KAFKA
    WORKER -->|"события ban/dead/stopped"| KAFKA
    KAFKA --> WORKER
    KAFKA -->|"уведомления"| BOT
    WORKER -->|"управляет аккаунтами"| TG
    API <--> REDIS
    WORKER <--> REDIS
    API -->|"создать инвойс"| PLATEGA
    PLATEGA -->|"webhook CONFIRMED"| API
```

**Ключевая идея связи процессов:** PostgreSQL — единственный источник правды. Kafka — быстрый путь (мгновенная реакция), а 10-секундный поллинг воркера — страховка, которая всё сверяет и самовосстанавливается.

---

## 1. Кто что запускает

```mermaid
flowchart TD
    DC["docker-compose"]
    DC --> PG[("postgres:15")]
    DC --> RD[("redis:7")]
    DC --> KF{{"kafka (KRaft)"}}
    DC --> API["API: alembic upgrade → uvicorn x2"]
    DC --> WK["Worker: python -m workers.orchestrator"]
    DC --> BT["Bot: python -m bot.main_bot"]
    DC --> FE["Frontend: build → nginx :80/:443"]

    API --> LIFE["lifespan: open Redis + Kafka producer, safety-net create_all"]
    WK --> T11["11 фоновых задач (см. ниже)"]
    BT --> POLL["polling + 2 Kafka-consumer'а + WebApp-кнопка + heartbeat"]
```

**11 задач воркера** (`workers/orchestrator.py`): `account_sync_manager` (поллинг 10с), `grace_period_manager` (час), `subscription_notifier` (час), `command_consumer` (Kafka), `comment_editor`, `scheduled_ban_check` (09:00/21:00 MSK), `digest_notifier`, `analytics_cleanup`, `warmup_finisher` (120с), `admin_broadcast_manager` (30с), `heartbeat`.

---

## 2. Запуск бота и открытие Mini App (`/start`)

```mermaid
sequenceDiagram
    actor U as Пользователь
    participant B as Bot
    participant DB as PostgreSQL
    participant S as Mini App
    participant API as Backend API

    U->>B: /start (возможно ref-код)
    B->>DB: get_or_create_user + захват реферала
    B-->>U: Приветствие + кнопка "🚀 Открыть приложение"
    U->>S: Открывает Mini App
    S->>API: GET /users/me (Authorization = initData)
    API->>API: validate_init_data — HMAC-SHA256, свежесть 24ч
    API->>DB: профиль пользователя
    API-->>S: is_admin, is_partner, подписка, балансы
    S-->>U: Интерфейс, вкладки по ролям
```

---

## 3. Жизненный цикл аккаунта

Самая важная часть продукта. Загруженный аккаунт **не идёт сразу в работу** — он проходит принудительную адаптацию.

```mermaid
stateDiagram-v2
    [*] --> adapting: загрузка session+json
    adapting --> adapting: первый коннект — якорь окна на 1 день
    adapting --> active: прогрев завершён
    active --> active: работа — комменты, ЛС, группы
    active --> restricted: PeerFlood
    restricted --> active: авто через 24 часа
    active --> dead: мёртвый ключ или бан SpamBot
    adapting --> dead: мёртвый ключ
    dead --> [*]: авто-удаление и уведомление
```

А вот как оркестратор каждые 10 секунд решает, кого запустить/остановить:

```mermaid
flowchart TD
    T["⏱️ Каждые 10 сек"] --> Q["Выбрать: is_running AND NOT banned AND (есть владелец OR прогрев)"]
    Q --> R{"Сверка с running_clients"}
    R -->|"в БД, но не запущен"| ST["start_account"]
    R -->|"запущен, но не в БД"| SP["stop_account"]
    ST --> PX{"Есть прокси?"}
    PX -->|"нет"| BO["Пауза 30 мин + лог (раз в 6ч)"]
    PX -->|"да"| CN["Коннект + get_me (3 попытки)"]
    CN --> DEAD{"Мёртвый ключ?"}
    DEAD -->|"да"| RM["Удалить + событие AccountDead"]
    DEAD -->|"нет"| MODE{"is_warming_up?"}
    MODE -->|"да"| WU["warmup_process — БЕЗ реактивных хендлеров"]
    MODE -->|"нет"| AC["init_client_handlers + joiner + profile"]
```

**Важно:** реактивные хендлеры (ЛС/комменты/группы) навешиваются **только в active-режиме**. Холодный/адаптирующийся аккаунт никому не отвечает.

---

## 4. Реактивная работа — как появляется комментарий / ответ

Когда аккаунт активен, на каждое входящее событие Telegram срабатывает хендлер и проходит цепочку защит.

```mermaid
sequenceDiagram
    participant TG as Telegram
    participant H as Хендлер (Telethon)
    participant R as Redis
    participant DB as PostgreSQL
    participant AI as OpenAI gpt-4o-mini

    TG->>H: Новое событие — пост / ЛС / сообщение в группе
    H->>R: is_restricted? (пауза после PeerFlood)
    alt Ограничен
        H-->>TG: Пропуск действия
    else Норма
        H->>DB: Это целевой канал? Настройки аккаунта?
        H->>DB: Дневной лимит (DailyActionStat)
        H->>R: Часовой лимит GPT (fail-closed)
        H->>H: Человеческая задержка — чтение, typing
        H->>AI: Сгенерировать текст
        AI-->>H: Ответ
        H->>TG: Отправить комментарий / сообщение
        H->>DB: increment_daily_stat + BotLog
        H->>R: publish logs:{account_id}
        R-->>H: WS транслирует лог в Mini App
    end
```

Лимиты по умолчанию: **25 каналов** и **10 групп** на аккаунт; **50 комментов/день**, **50 ЛС/день**; в группах лимита на число сообщений нет (только пауза 8–20 мин между ответами в одной группе).

---

## 5. Включение / выключение аккаунта (быстрый путь + страховка)

```mermaid
sequenceDiagram
    actor U as Пользователь
    participant API as Backend API
    participant DB as PostgreSQL
    participant K as Kafka account.commands
    participant W as Worker (fleet)

    U->>API: POST /accounts/toggle
    API->>DB: is_running = true / false
    API->>K: AccountStart/StopCommand (быстрый путь)
    K->>W: команда
    W->>DB: проверка is_running (БД — авторитет)
    W->>W: start_account / stop_account
    Note over W,DB: 10-сек поллинг = страховка: потерянная команда самовосстанавливается за 1 цикл
```

---

## 6. Оплата → подписка → выдача аккаунтов

Реально реализован **только Platega.io** (SBP / карта / крипта). Heleket/CryptoPay/Lava есть только в старых доках — в коде их нет (см. §10).

```mermaid
sequenceDiagram
    actor U as Пользователь
    participant S as Mini App
    participant API as Backend API
    participant P as Platega.io
    participant DB as PostgreSQL
    participant K as Kafka payment.events
    participant B as Bot

    U->>S: Выбор тарифа
    S->>API: POST /payments/platega/invoice
    API->>DB: 5% реф-скидка на первую покупку?
    API->>P: create transaction
    P-->>API: ссылка на оплату (meta сохраняется в Redis)
    API-->>U: Редирект на оплату
    U->>P: Оплата
    P->>API: webhook CONFIRMED (X-MerchantId / X-Secret)
    API->>API: проверка подписи + идемпотентность по order_id
    API->>DB: subscription_until += дни, max_accounts>=5, PaymentRecord
    API->>DB: реф-бонус (партнёру % в withdrawable ИЛИ +5 дней премиума)
    API->>DB: pre_validate + выдать 5 аккаунтов из пула (стратегия 1:5)
    API->>K: SubPaidEvent
    K->>B: уведомить покупателя и реферера
```

**Дальше по жизни подписки:** `subscription_notifier` шлёт предупреждения за 3 и 1 день и авто-продлевает с баланса ($49); `grace_period_manager` после истечения останавливает аккаунты, а через 7 дней возвращает пуловые аккаунты в общий пул (загруженные пользователем — не трогает).

---

## 7. Прогрев и адаптация

```mermaid
flowchart TD
    A["Аккаунт в режиме прогрева"] --> C{"lifecycle_state?"}
    C -->|"adapting"| AD["Адаптация: 1 день, мягко, без комментов, без SpamBot"]
    C -->|"active + user warmup"| LV["Уровни 1/2/3: 14/10/7 дней, рампа бюджета 0.2→1.0"]
    AD --> ACT["warmup_process: онлайн, чтение постов, реакции, 1 вступление/день, заметка в Избранное"]
    LV --> ACT
    ACT --> F{"warmup_end_time прошёл?"}
    F -->|"нет"| ACT
    F -->|"да"| G["warmup_finisher: lifecycle=active, activated_at=now, уведомить владельца"]
    G --> H["Следующий поллинг: перезапуск в ACTIVE — хендлеры включаются"]
```

Каналы и промпт для прогрева берутся из админки (Redis `warmup:config`). **Без заданных каналов адаптация почти ничего не делает** — аккаунт просто «отлёживается» онлайн.

---

## 8. Аутентификация и права

Один заголовок `Authorization` несёт два типа credential, различаются по префиксу.

```mermaid
flowchart TD
    REQ["Запрос с Authorization"] --> CHK{"Начинается с eyJ?"}
    CHK -->|"да — JWT (веб)"| JWT["decode HS256 → telegram_id"]
    CHK -->|"нет — initData (Mini App)"| HMAC["validate_init_data: HMAC-SHA256 + свежесть 24ч → telegram_id"]
    JWT --> UID["user_id"]
    HMAC --> UID
    UID --> ADM{"Эндпоинт /admin?"}
    ADM -->|"нет"| OK["✅ Доступ пользователя"]
    ADM -->|"да"| VA{"telegram_id в ADMIN_IDS?"}
    VA -->|"нет"| DENY["⛔ 403"]
    VA -->|"да"| DEST{"Деструктивное действие?"}
    DEST -->|"нет"| AOK["✅ Доступ админа"]
    DEST -->|"да"| TOTP{"X-TOTP-Code валиден?"}
    TOTP -->|"да"| AOK
    TOTP -->|"нет"| DENY
```

Публичные (без auth): `/health`, `/auth/telegram-login`, `/analytics/track`, `/payments/platega/rates`, webhook Platega (по подписи), `/r/{ref_code}`.

---

## 9. Kafka — топики и потоки сообщений

```mermaid
flowchart LR
    API2["API"]
    WFleet["Worker / fleet"]
    TC["account.commands"]
    TE["account.events"]
    TP["payment.events"]
    WC["Worker — group worker-fleet"]
    BC["Bot — listeners"]

    API2 -->|"start/stop, check_ban, keyword_search, parse, broadcast"| TC
    TC --> WC
    WFleet -->|"connected / dead / stopped / banned"| TE
    TE --> BC
    API2 -->|"SubPaidEvent, WithdrawalRequested"| TP
    TP --> BC
```

Продюсер идемпотентный (`acks=all`), консьюмер с ручным коммитом и ретраями ×3 → DLQ. `analytics.events` объявлен, но пока не используется.

---

## 10. Реальность vs документация (drift)

Что нашли при разборе — стоит однажды почистить, чтобы доки не вводили в заблуждение:

- **Платёжки:** в коде только **Platega.io**. `HELEKET_*` / `CRYPTOPAY_*` / Lava упоминаются в `CLAUDE.md` и `PROJECT_OVERVIEW.md`, но их нет в `utils/config.py` и роутерах — это legacy-доки.
- **Мёртвые страницы фронта:** `pages/Analytics.jsx` и `pages/Dashboard.jsx` импортируют Recharts, но не подключены в `router.jsx`. Живой чарт — `components/ui/AreaChartPro.jsx` (на visx), Recharts фактически не используется.
- **Устаревшие пути в `frontend/src/api/admin.js`:** `/admin/check-bans` и `/admin/bulk-bio` не совпадают с реальными `/admin/accounts/check_bans` и `/admin/warmup/bulk_bio`.
- **CLAUDE.md** говорит про 9 моделей БД и `endpoints.py` — фактически моделей больше, а эндпоинты давно разбиты по `api/routers/*`.

---

## Приложение: карта каталогов

| Путь | Что там |
|---|---|
| `bot/` | aiogram-бот: `main_bot.py`, `handlers/`, `kafka_listener.py` |
| `backend/main.py` | FastAPI-приложение, lifespan, middleware, монтирование роутеров |
| `backend/api/routers/*` | эндпоинты по доменам (см. §8/§9) |
| `backend/core/security.py` | валидация initData (HMAC) |
| `workers/orchestrator.py` | 11 фоновых задач, поллинг 10с, grace/warmup/ban-check |
| `workers/fleet.py` | старт/стоп клиентов, прокси, active-vs-warmup |
| `workers/pyrogram_engine/*` | сабворкеры: comment_generator, autoresponder, group_chat, channel_joiner, warmup_worker, account_checker, spam_checker, keyword_search, admin_broadcaster |
| `services/` | session_service, device_profiles, warmup_levels, account_limits(удалён), restriction |
| `database/models.py` `crud.py` | модели и операции БД |
| `kafka/` | producer, consumer, topics, schemas |
| `frontend/src/pages/*` | Profile, Payments, Accounts, Tools, Partner, Admin |
