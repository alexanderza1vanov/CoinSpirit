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


## Микросервисы:
## Auth & Users

Цель: регистрация, логин, refresh-токены, управление пользователями.

Данные: users, sessions, refresh_tokens

API: POST /v1/auth/signup|login|refresh, GET /v1/users/me

События: user.created, user.deactivated

API: GET/PUT /v1/profile

События: profile.updated

## Search (по активам)

Цель: полнотекстовый поиск по активам.

Данные: индекс (OpenSearch/Meilisearch)

API: GET /v1/search?q=...

## Калькулятор и конвертер криптовалют.


Цель: Позволяет быстро конвертировать одну валюту в другую, проводить расчёты не только между криптовалютами, но и с традиционными валютами

Данные: timeseries (TimescaleDB/ClickHouse/Redis)

API: GET /v1/quotes?assets=BTC,ETH&convert=USD, GET /v1/ohlcv/{asset}

События: price.tick, ohlcv.updated

## Portfolio (позиции/оценка)

Цель: хранение позиций, вычисление стоимости и PnL.

Данные: portfolios, positions, valuations

API: GET /v1/positions, GET /v1/valuations?at=...

События: portfolio.position_changed, portfolio.valuation_ready

## Transactions (журнал операций)

Цель: buy/sell/transfer/staking/reward, идемпотентность, консистентность.

Данные: transactions(id, portfolio_id, asset_id, type, qty, price, fee, ts, source, idempotency_key)

API: POST /v1/transactions, GET /v1/transactions?cursor=...

События: transaction.recorded → слушает Portfolio для пересчёта позиций

Примечание: можно объединить 6 и 7 в один сервис на старте, но разделение упрощает масштабирование.

## Уведомления

Цель: хранение и оценка условий (цена выше/ниже, %-изменение, дневные лимиты).

Данные: alerts(user_id, asset_id, rule, threshold, active)

API: POST/GET/PATCH /v1/alerts

События: alert.triggered (в Notifications)
## news-service
новости допустим парсинг flash crypto
## микросервис аналитики 
<img width="1420" height="102" alt="image" src="https://github.com/user-attachments/assets/b7ee8559-2942-41d0-b6ae-a26f9d9204d1" />
