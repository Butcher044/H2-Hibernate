# ComFlow — Беклог (черновик на согласование)

> **Статус: ЧЕРНОВИК. Не выполнять** — задачи стартуют только по команде владельца.
> Составлен по итогам аудита кода (архитектор по банам + стратег по аналитике) и решений владельца.
> Обновлено: 2026-07-30.

## Согласованные решения (зафиксированы)
1. **Платёжки:** оставляем только **Platega**. Heleket/CryptoPay/Lava/Telegram Stars — убрать.
2. **Аккаунты:** выдаём **3** (и на триале, и на платной), вместо 5.
3. **Партнёрка — комиссия по числу оплат:** 1-я оплата клиента = **30%**, оплаты 2–6 = **10%** каждая, с 7-й = 0. Многомесячный тариф (3/6 мес) = **одна оплата**. Не пожизненно.
4. **Прогрев:** убираем **принудительный** прогрев. После загрузки — быстрая read-only проверка, затем аккаунт **сразу доступен**. Анти-бан — через «smart connect». Автопрогрев остаётся **опциональным**.

## Легенда
`ADD` — добавить · `CHG` — изменить · `DEL` — удалить · **P0/P1/P2** — приоритет · `⚠️` — риск/зависимость.

---

# AREA 1 — Платёжки: только Platega

**Факт из кода:** реального кода шлюзов Heleket/CryptoPay/Lava/Stars в приложении **НЕТ** — только Platega ([platega.py](backend/api/routers/platega.py)) + внутренний баланс ([payments.py](backend/api/routers/payments.py), пополняется через Platega). «Убрать» = вычистить конфиги, зависимость, orphan-тесты, одну строку и устаревшие доки. **Кода-платёжки удалять нечего.**

| # | Тип | Задача | Файл |
|---|---|---|---|
| 1.1 | `DEL` | Убрать env-переменные `HELEKET_MERCHANT_ID`, `HELEKET_API_KEY`, `CRYPTOPAY_TOKEN`, `CRYPTOPAY_TESTNET` | [.env:46-51](.env) (+ `.env.example` если есть) |
| 1.2 | `DEL` | Убрать неиспользуемую зависимость `aiocryptopay` | [requirements/api.txt:25](requirements/api.txt#L25) |
| 1.3 | `DEL` | Удалить orphan-тесты Lava (патчат несуществующий `backend.api.routers.lava`, сейчас падают) | [tests/unit/test_new_features.py:115-160](tests/unit/test_new_features.py#L115) |
| 1.4 | `CHG` | Строку про слоты «за Telegram Stars» → «купить слоты» (слоты идут через Platega, action `slot`, $3) | [crud.py:150](database/crud.py#L150) |
| 1.5 | `CHG` | CLAUDE.md: убрать строки `HELEKET_*`/`CRYPTOPAY_*` из таблицы env | [CLAUDE.md:94-95](CLAUDE.md#L94) |
| 1.6 | `CHG/DEL` | `PROJECT_OVERVIEW.md` — весь про Heleket (10,37,65,76,133-134,157,181,220,253,278,291,311,323); переписать под Platega или удалить как устаревший | [PROJECT_OVERVIEW.md](PROJECT_OVERVIEW.md) |
| 1.7 | `CHG` | ROADMAP.md: «Heleket webhook» → Platega | [ROADMAP.md:229](ROADMAP.md#L229) |
| 1.8 | `CHG` | Убрать «Telegram Stars / Heleket / CryptoPay» из персоны стратега | [.claude/agents/product-strategist.md:12](.claude/agents/product-strategist.md#L12) |

**Приоритет: P2** (косметика/гигиена, не влияет на работу). **⚠️** проверить, что `slot`-покупка полностью закрыта Platega (сейчас — да, `slot_{uid}`), т.к. Stars-путь только в тексте.

---

# AREA 2 — Мёртвый код, устаревшие доки, аналитика и мониторинг

## 2A. Мёртвое/битое — удалить (дёшево, снимает риск дёрнуть битый путь)

| # | Тип | Задача | Файл |
|---|---|---|---|
| 2A.1 | `DEL` | Мёртвая страница (не в роутере, не импортится, тянет Recharts) | [frontend/src/pages/Analytics.jsx](frontend/src/pages/Analytics.jsx) |
| 2A.2 | `DEL` | Мёртвая страница (аналогично) | [frontend/src/pages/Dashboard.jsx](frontend/src/pages/Dashboard.jsx) |
| 2A.3 | `DEL` | Kafka-топик `analytics.events` — объявлен, 0 продюсеров/консьюмеров | [kafka/topics.py:6](kafka/topics.py#L6) |
| 2A.4 | `DEL` | Мёртвые схемы `CommentPostedEvent`/`DmSentEvent` (только в тестах) | [kafka/schemas.py:171-190](kafka/schemas.py#L171) |
| 2A.5 | `DEL` | 6 orphan/битых экспортов в admin-API клиента: `checkBans` (битый путь `/admin/check-bans`), `setBulkBio` (битый `/admin/bulk-bio`), `startWarmup`/`stopWarmup` (баг контракта + orphan), `getWarmupData`, `sendBroadcast` | [frontend/src/api/admin.js:13-37](frontend/src/api/admin.js#L13) |
| 2A.6 | `DEL/CHG` | Убрать пустой блок «Топ действий» ИЛИ расставить `trackAction` (см. 2C.8) — сейчас всегда пуст, вводит в заблуждение | [Admin/index.jsx:603](frontend/src/pages/Admin/index.jsx#L603) |
| 2A.7 | `CHG` | Recharts удалить из зависимостей после 2A.1/2A.2 (живой чарт — visx `AreaChartPro`) | frontend/package.json |
| 2A.8 | `CHG` | Legacy on-demand ban-check `_check_bans_task` пересекается с плановым — выпилить после переноса ручного триггера на `scheduled_ban_check` | [admin.py:203](backend/api/routers/admin.py#L203) |

## 2B. Устаревшие доки (дрейф)

| # | Тип | Задача | Файл |
|---|---|---|---|
| 2B.1 | `CHG` | CLAUDE.md: «9 моделей» неверно (моделей больше); «endpoints.py» больше нет (роутеры разбиты) | [CLAUDE.md](CLAUDE.md) |
| 2B.2 | `CHG` | CLAUDE.md: **«1:5» = 5 аккаунтов на 1 прокси** (не «1 аккаунт на 5 юзеров»); каждому юзеру выдаётся 3 эксклюзивных аккаунта | [CLAUDE.md:89](CLAUDE.md#L89) |
| 2B.3 | `CHG` | «Расписание прогрева 7-12 дней» — после AREA 5 не актуально | [CLAUDE.md:95](CLAUDE.md#L95) |

## 2C. Аналитика: изменить + добавить (стратег)

**Что есть:** комменты/ЛС (`DailyActionStat`), выручка/подписки (`PaymentRecord`, `/admin/stats`), партнёрская воронка (`/partner/analytics` — лучшее в проекте), продуктовые клики (`AnalyticsEvent`). **Чего нет: банрейт, время жизни аккаунта, COGS, retention/LTV/churn, глобальная воронка триал→оплата.**

**CHG:**
- 2C.1 `CHG` **P1** `/admin/stats` revenue разделить: `gross_cash_in` (все Platega) / `sub_revenue` (action='sub') / `slot_revenue` — сейчас «Выручка» мешает пополнения и подписки. [admin.py:72](backend/api/routers/admin.py#L72)
- 2C.2 `CHG` **P1** `subs_sold` разложить new / renewal / balance для MRR (признак первичная/продление — как скидка в [users.py:211](backend/api/routers/users.py#L211)). [admin.py:77](backend/api/routers/admin.py#L77)
- 2C.3 `CHG` **P1** Глобальная воронка: обобщить логику `/partner/analytics` (clicks→reg→trial→pay) на весь трафик → `GET /admin/analytics/funnel`. [partner.py:258-327](backend/api/routers/partner.py#L258)

**ADD — мониторинг банрейта/COGS/времени жизни (приоритет владельца):**
> ⚠️ **Корень проблемы:** смерть аккаунта нигде не фиксируется с таймстемпом, а `release_owned_account(banned=True)` и admin-delete **стирают BotLog** → историю смертей теряем. Нужна отдельная нестираемая таблица.

- 2C.4 `ADD` **P0** Таблица **`AccountLifecycleEvent`** (иммутабельный журнал): `account_id, event(activated|banned|frozen|revoked|replaced), reason, niche, proxy_type(shared_pool|byo|dedicated), source(tdata|session), warmup_level, activated_at, occurred_at, lifespan_hours`. Индексы (event,occurred_at),(niche,…),(proxy_type,…). [models.py](database/models.py) + миграция.
- 2C.5 `ADD` **P0** Писать события в 4 точках: активация [orchestrator.py:267](workers/orchestrator.py#L267); смерть в [crud.py:308](database/crud.py#L308) (`release_owned_account banned=True`, снять `proxy_type` ДО обнуления), [scheduled_ban_check.py:96](workers/scheduled_ban_check.py#L96), [fleet.py:86](workers/fleet.py#L86) (`_mark_session_dead`).
- 2C.6 `ADD` **P0** Колонки-снимки на `TelegramAccount`: `niche`, `source`, `proxy_type` (для группировок). [models.py:127](database/models.py#L127)
- 2C.7 `ADD` **P1** `GET /admin/analytics/pool-health`: hazard-rate (deaths/active-account-days), кривая выживаемости (день 1/3/7/14/30 от `activated_at`), медианное время жизни, разрезы по niche / **proxy_type (shared vs dedicated — прямой ответ на вопрос владельца)** / source / warmup_level.
- 2C.8 `ADD` **P1** `GET /admin/analytics/unit-economics`: замены/мес, COGS (цены в `utils/config.py` — сейчас нет; счётчик OpenAI-токенов опционально), **COGS/подписчика**, маржа = ARPU − COGS/подписчика.
- 2C.9 `ADD` **P1** Алерты в бота (инфра готова): банрейт/сутки > порога; медиана жизни просела >20% н/н; свободный пул < буфера под отток.
- 2C.10 `ADD` **P2** Вкладка «Здоровье пула» в админке (survival-кривая, бары по нишам/прокси, карточки COGS/маржа). [Admin/index.jsx TABS](frontend/src/pages/Admin/index.jsx#L262)
- 2C.11 `ADD` **P2** Бизнес-метрики `GET /admin/analytics/business`: активация (reg→first account→first action), триал→оплата, ARPU/LTV/churn/MRR, когорты по неделе регистрации.
- 2C.12 `ADD` **P2** Расставить `trackAction` на бизнес-действиях (покупка, аллокация, старт прогрева/кампании, вывод) — чтобы продуктовая воронка заработала. [analytics.js:68](frontend/src/lib/analytics.js#L68)

---

# AREA 3 — Партнёрка: понятная для всех + онбординг + новая модель комиссий

## 3A. Починить протечку комиссии (P0 — иначе «кидалово»)
- 3A.1 `CHG` **P0** Партнёрская комиссия начисляется только на Platega-пути ([platega.py:245](backend/api/routers/platega.py#L245)), а на оплате с **внутреннего баланса** ([payments.py:123-155](backend/api/routers/payments.py#L123)) — нет. Унифицировать: оба пути должны начислять. Иначе блогер приводит платящих, часть платит с баланса → комиссия $0.

## 3B. Новая модель комиссий (по числу оплат, макс 6)
Сейчас: разовая flat 30/40% (`UniqueConstraint(referral_id)` в `ReferralReward` блокирует повтор). Нужна схема **30% / 10%×5 / потом 0**.
- 3B.1 `CHG` **P0** Убрать «разовость»: разрешить до **6** наград на одного реферала. Считать **порядковый номер оплаты** клиента, атрибутированной этому партнёру.
- 3B.2 `CHG` **P0** Начисление: `payment_index==1 → 30%`, `2..6 → 10%`, `>6 → 0`. Многомесячный тариф = 1 оплата. Начислять на `balance_withdrawable`.
- 3B.3 `CHG` **P0** `ReferralReward`: снять `UniqueConstraint(referral_id)`, добавить `payment_index` / `payment_record_id`; идемпотентность по `payment_record_id` (не начислить дважды за один платёж, важно на ретраях вебхука). [models.py:76-88](database/models.py#L76)
- 3B.4 `CHG` **P1** Тиры 30/40% в [partner.py:36-39](backend/api/routers/partner.py#L36) больше не нужны в старом виде — заменить на новую схему; `is_partner` (флаг из админки) остаётся гейтом доступа.

## 3C. Фронтенд: вкладка «Партнёр» доступна ВСЕМ + питч + «Стать партнёром»
Сейчас иконка «Партнёр» видна только при `is_partner` ([BottomNav.jsx:19-23](frontend/src/components/layout/BottomNav.jsx#L19)).
- 3C.1 `CHG` **P1** Показывать вкладку «Партнёр» всем пользователям.
- 3C.2 `ADD` **P1** Для пользователя **без** статуса партнёра — красивый лендинг-питч (стратег, черновик оформления ниже) + кнопка **«Стать партнёром»** с инструкцией, что написать (владелец созванивается лично в Telegram, затем выдаёт статус в админке).
- 3C.3 (без изменений) Пользователь со статусом — текущий кабинет партнёра.

**Черновик оформления питча (для не-партнёров)** — стратег:
1. **Герой:** «Зарабатывай с ComFlow — приводи клиентов, получай до 30% с их оплат.» Крупная цифра-крючок.
2. **Как это работает** (3 шага, иконки): привёл → клиент оплатил → тебе капает (30% с 1-й оплаты + 10% со 2–6-й, до 6 оплат на клиента).
3. **Калькулятор дохода:** ползунок «сколько клиентов приведёшь/мес» → прогноз $ (наглядно).
4. **Кому подходит:** блогеры, владельцы TG-каналов, арбитражники, SMM.
5. **Что даём:** именной промокод (скидка 10% зрителю), кабинет с воронкой, вывод в USDT/на карту, creative-pack.
6. **CTA «Стать партнёром»** → экран с текстом-инструкцией: «Напиши @<owner> — расскажи, кто ты, как будешь приводить трафик (блог/канал/арбитраж), ссылки на площадки». После созвона владелец выдаёт статус.
7. Соцдоказательство/лидерборд (когда появится).

## 3D. Антифрод под новую модель
- 3D.1 `ADD` **P1** **Hold-период**: комиссия «созревает» после окна возврата / 2-й недели реферала (критично — раздаём бесплатные аккаунты, самореф ради триалов). [partner.py payouts](backend/api/routers/partner.py#L482)
- 3D.2 `CHG` **P1** `is_same_origin_referral` сейчас **fail-open** (нет хеша → комиссию разрешаем) — закрыть. [crud.py:106](database/crud.py#L106)
- 3D.3 `ADD` **P2** Velocity-лимиты + ручная проверка топ-эрнеров + KYC-lite на выплаты выше порога.

**Приоритет AREA 3: P0** (протечка + модель комиссий), затем P1 (фронт + антифрод).

---

# AREA 4 — Выдавать 3 аккаунта вместо 5

| # | Тип | Задача | Файл |
|---|---|---|---|
| 4.1 | `CHG` | `allocate_system_accounts` default `count=5` → `3` (оплата и триал) | [crud.py:198](database/crud.py#L198) |
| 4.2 | `CHG` | `max_accounts >= 5` → `>= 3` при активации подписки и триала | [platega.py:167-168](backend/api/routers/platega.py#L167), [platega.py:689](backend/api/routers/platega.py#L689) |
| 4.3 | `CHG` | Вызовы `allocate_system_accounts(count=5)` → `count=3` | [platega.py:178](backend/api/routers/platega.py#L178), [platega.py:702](backend/api/routers/platega.py#L702) |
| 4.4 | `CHG` | Тексты/лимиты на фронте, где «5 аккаунтов» | frontend (Payments/Profile) |

**Приоритет: P1.** **⚠️** увязать с AREA 3B (при трёх аккаунтах слот-докупка через Platega остаётся). Дефолт `accounts_per_proxy=5` не трогаем в этом айтеме (прокси — в AREA 5).

---

# AREA 5 — Баны: убрать форс-прогрев + «Smart Connect» (архитектор)

> **Главный вывод архитектора:** форс-прогрев **не был** причиной банов. Реальная причина — **отпечаток первого логина**: (1) **device-less коннект → дефолты Telethon** (машина хоста + версия Telethon как app_version) = сигнал «угона» → бан на первом логине (вероятно, основной источник мгновенных банов); (2) смена IP/DC/гео на существующем auth_key (датацентровый/чужой-гео прокси, гео-матчинга нет); (3) **не-sticky прокси** (аккаунт прыгает по IP между рестартами); (4) headless-паттерн — на старте только `get_me()`, никогда `getDialogs/getState/updateStatus`.
>
> ⚠️ **КРИТИЧЕСКАЯ ЗАВИСИМОСТЬ: 5A и 5B выкатывать ТОЛЬКО ВМЕСТЕ.** Если убрать прогрев, не починив device/proxy/geo — баны **вырастут** (прогрев маскировал часть проблемы, давая 20-40 мин «отлёжки» на новом IP).
>
> Это частично меняет P0-коммит `cf80ee3` (форс-адаптация): убираем адаптацию, но оставляем хорошие части P0 (real-or-none device как политику, read-only валидацию, PeerFlood, fail-closed, settle joiner/profile).

## 5A. Убрать принудительную адаптацию
| # | Тип | Задача | Файл |
|---|---|---|---|
| 5A.1 | `CHG` | При загрузке: вместо `lifecycle_state="adapting"`/`is_warming_up=True`/`warmup_level=1` → `lifecycle_state="active"`, `is_warming_up=False`, `activated_at=now`. Перед этим — **read-only валидация загрузки** (`check_account_health`, которой сейчас для user-uploaded НЕТ) | [user_accounts.py:257-266](backend/api/routers/user_accounts.py#L257) |
| 5A.2 | `CHG` | Текст уведомления «обязательная адаптация ~1 день» → «аккаунт готов к работе» | [user_accounts.py:307-318](backend/api/routers/user_accounts.py#L307) |
| 5A.3 | `DEL` | `_anchor_adaptation_window` + его вызов | [fleet.py:105-122](workers/fleet.py#L105), [fleet.py:600-602](workers/fleet.py#L600) |
| 5A.4 | `CHG` | `warmup_finisher`: убрать спец-обработку `was_adapting`; ветку реального прогрева оставить | [orchestrator.py:239-312](workers/orchestrator.py#L239) |
| 5A.5 | `DEL` | `is_adapting`/`get_adaptation_cfg`/skip-SpamBot-during-adaptation | [warmup_worker.py:237-251](workers/pyrogram_engine/warmup_worker.py#L237) |
| 5A.6 | `DEL` | `ADAPTATION_DAYS`, `ADAPTATION_CFG`, `get_adaptation_cfg` | [warmup_levels.py:64-92](services/warmup_levels.py#L64) |
| 5A.7 | `CHG` | Снять гейты `assert_not_adapting`/`is_adapting` (toggle, settings, bulk, warmup, поля ответа) | [deps.py:28-41](backend/api/deps.py#L28) + accounts/settings/bulk/warmup |
| 5A.8 | `CHG` | Убрать фильтр adapting в `sync_warmup_folder` | [crud.py:35](database/crud.py#L35) |
| 5A.9 | keep | Колонки `lifecycle_state`/`activated_at` НЕ удалять (`activated_at` нужен для day-0 ramp; `lifecycle_state` упрощается до active/restricted) | [models.py:148-154](database/models.py#L148) |

## 5B. Smart Connect (замена прогрева) — **ADD, P0**
| # | Задача | Файл |
|---|---|---|
| 5B.1 | **Cold-start серия** при каждом коннекте после `get_me`: `help.GetConfig` → `get_me` → `account.UpdateStatus(online)` → `updates.GetState` → `messages.GetDialogs(10-20)`, паузы 2-6с между вызовами («человек открыл Telegram»). | [fleet.py:556-609](workers/fleet.py#L556) |
| 5B.2 | **Гейт `first_action_not_before` = connect_time + jitter** для РЕАКТИВНЫХ действий (DM/коммент) — **3-10 мин** (сейчас гейта нет — дыра). | [autoresponder.py](workers/pyrogram_engine/autoresponder.py), [comment_generator.py](workers/pyrogram_engine/comment_generator.py) |
| 5B.3 | **device-less → не запускать**: статус `needs_reimport` (или дорекавери через **opentele из tdata**), а не коннект на дефолтах Telethon. | [session_service.py:180-186](services/session_service.py#L180) |
| 5B.4 | **Гео-матчинг прокси = страна номера** + приоритет резидентных над датацентровыми. | [fleet.py:394-441](workers/fleet.py#L394), [crud.py:170-187](database/crud.py#L170) |
| 5B.5 | **Sticky-прокси**: привязать аккаунт к одному egress-IP, переиспользовать при рестартах (для user-owned сохранять выбранный прокси — сейчас `proxy_id` не пишется). | [fleet.py:467](workers/fleet.py#L467) |
| 5B.6 | **Day-0 мягкий ramp**: первые часы active резать дневные лимиты до ~20-30% (сейчас flat 50/50). `activated_at` уже есть. | [crud.py:78-82](database/crud.py#L78) |
| 5B.7 | **Профиль в day-0 не трогать** (username/аватар — сильный takeover-сигнал даже через 15-30 мин). | [fleet.py:186](workers/fleet.py#L186) |
| 5B.8 | `lang_code/system_lang_code` синхронизировать со страной номера. | [session_service.py](services/session_service.py) |

## 5C. Оставить (это правильно)
Real-or-none device как политику (ужесточить fallback), всегда-через-прокси, settle joiner/profile 20-40/15-30 мин, PeerFlood-restriction 24ч, Redis fail-closed, read-only pre-validation (+добавить для загрузок), опциональный пользовательский прогрев (уровни 1-3).

## 5D. Что валидировать первым (данные, до кода)
1. **Корреляция банов с (страна номера vs страна прокси)** и **(device recovered vs device-less)** — подтвердит, что причина в логине.
2. Доля аккаунтов, где `device_for_config` вернул `None` (молча стартуют на дефолтах Telethon) — вероятно основной источник мгновенных банов.
3. Меняется ли egress-IP между рестартами (последствие least-loaded `_pick_user_proxy`) — если да, sticky даст быстрый выигрыш.
4. A/B на маленькой партии: текущий флоу vs smart-connect (без форс-прогрева) — метрика «жив через 30 мин / 24ч».

**Приоритет AREA 5: P0** (это главная боль). Требует мониторинга из AREA 2C (без журнала смертей нечем мерить результат).

---

# Сводная приоритизация и зависимости

**P0 (сначала):**
- 2C.4–2C.6 — журнал смертей аккаунтов (фундамент под замер; без него AREA 5 нечем валидировать).
- AREA 5 (5A+5B **вместе**) — smart connect + убрать форс-прогрев (главная боль: баны).
- 3A + 3B — протечка партнёрки + новая модель комиссий (риск «кидалово»).

**P1:**
- AREA 4 — 3 аккаунта.
- 2C.1–2C.3, 2C.7–2C.9 — разделить revenue, воронка, pool-health/unit-economics, алерты.
- 3C + 3D — фронт партнёрки для всех + антифрод.

**P2:**
- AREA 1 — чистка платёжных доков/конфигов.
- 2A/2B — мёртвый код + доки.
- 2C.10–2C.12 — вкладка «Здоровье пула», бизнес-метрики, trackAction.

**Ключевые зависимости:**
- AREA 5: `5A` и `5B` — **только вместе** (иначе баны вырастут).
- AREA 5 замер результата ← нужен `AccountLifecycleEvent` (2C.4–2C.6).
- AREA 3B (модель комиссий) ← сначала `3A` (протечка).
- AREA 4 (3 аккаунта) — самостоятельно, но увязать с текстами тарифов.

# Открытые вопросы (уточнить перед выполнением)
1. AREA 5B.4: гео-матчинг требует страны прокси — у shared-пула её может не быть. Нужен fallback-приоритет (резидентные) или помечать пул без гео?
2. AREA 5B.3: device-less аккаунты — блокировать (`needs_reimport`) или вкладываться в opentele-дорекавери из tdata (тогда нужен исходный tdata, а не .session)?
3. AREA 2C.8: заводить ли реальные цены (аккаунт/прокси/OpenAI) в конфиг для COGS сейчас или позже?
4. AREA 3B: при возврате/чарджбэке оплаты — списывать уже начисленную партнёру комиссию? (сейчас нет логики reversal.)
