# Руководство пользователя CoinSpirit

## Установка приложения
1. Скачайте APK с сайта или Google Play
2. Установите на Android-устройство

## Регистрация и вход
- Откройте приложение, выберите "Регистрация"
- Введите e-mail, пароль, имя
- Подтвердите регистрацию
- Для входа используйте e-mail и пароль

## Добавление криптовалюты
- На главном экране нажмите "+" или "Добавить"
- Введите тикер, количество, цену покупки
- Подтвердите операцию

## Просмотр портфеля
- В разделе "Портфель" видны все активы, их стоимость и прибыль/убыток

## Работа с историей транзакций
- Откройте "Историю" для просмотра всех операций

## Настройки
- Перейдите в меню "Настройки"
- Измените тему, язык, валюту отображения

## Выход из аккаунта
- В настройках выберите "Выйти"

## FAQ
- Как восстановить пароль?
- Что делать, если курс не обновляется?
Микросервисы
Auth & Users – регистрация, логин, refresh токены.

API Gateway / BFF (Mobile)

Цель: единая точка входа, агрегация данных под экраны мобильного клиента.

API: GET /bff/home, GET /bff/history, POST /bff/transactions

Зависит от: Auth, Portfolio, Market Data, Alerts, Profiles.

Auth & Users

Цель: регистрация, логин, refresh-токены, управление пользователями.

Данные: users, sessions, refresh_tokens

API: POST /v1/auth/signup|login|refresh, GET /v1/users/me

События: user.created, user.deactivated

Profiles (настройки пользователя)

Цель: валюта по умолчанию, локаль, тема, часовой пояс, опции приватности.

Данные: profiles(user_id, base_currency, locale, theme, tz, ...)

API: GET/PUT /v1/profile

События: profile.updated

Assets Catalog (справочник активов)

Цель: метаданные монет/токенов, символы, точности, привязки к провайдерам.

Данные: assets, asset_aliases, icons

API: GET /v1/assets?query=ETH, GET /v1/assets/{id}

События: asset.updated (редко)

Market Data (котировки/история)

Цель: агрегация цен, кэширование, ohlcv, конвертация в базовую валюту.

Данные: timeseries (TimescaleDB/ClickHouse/Redis)

API: GET /v1/quotes?assets=BTC,ETH&convert=USD, GET /v1/ohlcv/{asset}

События: price.tick, ohlcv.updated

Portfolio (позиции/оценка)

Цель: хранение позиций, вычисление стоимости и PnL.

Данные: portfolios, positions, valuations

API: GET /v1/positions, GET /v1/valuations?at=...

События: portfolio.position_changed, portfolio.valuation_ready

Transactions (журнал операций)

Цель: buy/sell/transfer/staking/reward, идемпотентность, консистентность.

Данные: transactions(id, portfolio_id, asset_id, type, qty, price, fee, ts, source, idempotency_key)

API: POST /v1/transactions, GET /v1/transactions?cursor=...

События: transaction.recorded → слушает Portfolio для пересчёта позиций

Примечание: можно объединить 6 и 7 в один сервис на старте, но разделение упрощает масштабирование.

Уведомления

Цель: хранение и оценка условий (цена выше/ниже, %-изменение, дневные лимиты).

Данные: alerts(user_id, asset_id, rule, threshold, active)

API: POST/GET/PATCH /v1/alerts

События: alert.triggered (в Notifications)
