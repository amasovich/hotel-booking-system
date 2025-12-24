# Hotel Booking System — итоговый проект по Spring / REST API (МИФИ)

Учебный микросервисный проект: **API Gateway + Eureka + Hotel Service + Booking Service**.  
Ключевая фича — **бронирование как распределённая транзакция (сага confirm/release)** + **алгоритм рекомендаций номера**.

---

## 1) Стек и требования

- Java: **21+** (в проекте выставлен `maven.compiler.release=21`)
- Spring Boot: **3.5.8**
- Spring Cloud: **2025.0.0**
- БД: **H2 in-memory** в каждом сервисе
- Документация API: **Swagger / OpenAPI (springdoc)**

---

## 2) Архитектура и роли сервисов

```
               ┌────────────────────┐
               │   discovery-server │  :8761 (Eureka)
               └─────────┬──────────┘
                         │
┌───────────────┐   service discovery    ┌────────────────┐
│  api-gateway   │  -------------------> │ booking-service │ :8081
│     :8080      │                       │  users + bookings
│  public routes │                       │  saga + retries
└───────┬────────┘                       └───────┬────────┘
        │                                        │ internal call
        │                                        ▼
        │                               ┌────────────────┐
        └------------------------------>│  hotel-service  │ :8082
          public routes                 │ hotels + rooms  │
                                        │ recommend + lock│
                                        └────────────────┘
```

### Публичные vs internal endpoints

Gateway прокидывает **только публичные** HTTP-маршруты (для ручной проверки).  
**Internal endpoints** для саги (confirm/release) **не прокидываются через gateway** — их вызывает `booking-service` напрямую по `lb://hotel-service`.

---

## 3) Быстрый запуск (под проверяющего)

Открыть 4 терминала в корне проекта и запустить:

1) Eureka:
```bash
mvn -pl discovery-server spring-boot:run
```

2) Hotel Service:
```bash
mvn -pl hotel-service spring-boot:run
```

3) Booking Service:
```bash
mvn -pl booking-service spring-boot:run
```

4) API Gateway:
```bash
mvn -pl api-gateway spring-boot:run
```

Порты по умолчанию:
- Eureka: `http://localhost:8761`
- Gateway: `http://localhost:8080`
- Booking Service: `http://localhost:8081`
- Hotel Service: `http://localhost:8082`

---

## 4) Предзаполнение данных (Этап 5)

После старта сервисов автоматически создаются тестовые данные:

### Пользователи (booking-service)
- `admin@local / admin123` (роль **ADMIN**)
- `user@local / user123` (роль **USER**)

### Отели и комнаты (hotel-service)
- 3 отеля и 9 комнат (номера 101/102/201 …)

---

## 5) Swagger / OpenAPI (Этап 6)

Swagger UI доступен напрямую в сервисах:

- Booking Service: `http://localhost:8081/swagger-ui/index.html`
- Hotel Service: `http://localhost:8082/swagger-ui/index.html`

OpenAPI JSON:
- `http://localhost:8081/v3/api-docs`
- `http://localhost:8082/v3/api-docs`

### Как авторизоваться в Swagger

1) Получите JWT токен через gateway:
    - `POST http://localhost:8080/api/user/auth`
2) В Swagger UI нажмите **Authorize** и вставьте токен (обычно достаточно **только значения токена**, без `Bearer `).

---

## 6) Минимальный сценарий “быстрого ручного прогона” (Postman)

Ниже — самый короткий сценарий, чтобы проверяющий убедился, что система «живая» и сага реально работает.

> Все публичные вызовы — через **gateway :8080**.  
> Internal endpoints confirm/release вручную дергать не нужно.

### 6.1 Логин и токен
**POST** `http://localhost:8080/api/user/auth`
```json
{ "email": "user@local", "password": "user123" }
```
Ожидаемо: `200` и `token`.

### 6.2 Получить список отелей
**GET** `http://localhost:8080/api/hotels`  
Header: `Authorization: Bearer <token>`  
Ожидаемо: `200` и массив отелей.

### 6.3 Получить рекомендации по комнатам
**GET** `http://localhost:8080/api/rooms/recommend?start=2025-12-25&end=2025-12-28`  
Header: `Authorization: Bearer <token>`  
Ожидаемо: `200` и список комнат (отсортирован по загруженности/идентификатору).

### 6.4 Создать бронь (autoSelect=true)
**POST** `http://localhost:8080/api/booking`  
Header: `Authorization: Bearer <token>`  
Body:
```json
{
  "startDate": "2025-12-25",
  "endDate": "2025-12-28",
  "autoSelect": true
}
```
Ожидаемо: `200` и бронь со статусом **CONFIRMED**.  
Внутри booking-service выполнит сагу: `PENDING -> confirm-availability -> CONFIRMED`.

### 6.5 Проверить историю бронирований (пагинация)
**GET** `http://localhost:8080/api/bookings?page=0&size=10&sort=createdAt,desc`  
Header: `Authorization: Bearer <token>`  
Ожидаемо: `200` и Page-ответ.

### 6.6 Конфликт бронирования (409)
Сымитировать конфликт можно так:

1) Возьмите конкретную комнату из `/api/rooms/recommend` → `roomId`
2) Создайте бронь **явно** (autoSelect=false) на тот же период:
   **POST** `http://localhost:8080/api/booking`
```json
{
  "startDate": "2025-12-25",
  "endDate": "2025-12-28",
  "autoSelect": false,
  "roomId": 1
}
```
3) Повторите тот же запрос ещё раз.

Ожидаемо:
- второй запрос вернёт **409 Conflict**
- booking-service отменит бронь и **не оставит “подвисших” PENDING**

---

## 7) Где что реализовано (критерий → код)

| Критерий | Где реализовано (основное) |
|---|---|
| 1. Планирование занятости / рекомендации | `hotel-service`: `RoomService#recommend(...)`, `RoomController /api/rooms/recommend`, поле `timesBooked` |
| 2. Согласованность / сага | `booking-service`: `BookingServiceFacade` + `HotelServiceClient` (confirm/release + retry/timeout), `hotel-service`: internal endpoints `confirm-availability` / `release` |
| 3. CRUD бронирований | `booking-service`: `BookingController` (`POST/GET/DELETE`), репозитории/сущности |
| 4. CRUD отелей/комнат + статистика | `hotel-service`: `HotelController`, `RoomController`, `RoomStatsDto` / `timesBooked` |
| 5. Gateway + Eureka | `discovery-server`, `api-gateway/application.yml` (routes + discovery locator) |
| 6. Security (JWT, роли) | `booking-service`: выдача JWT, `SecurityConfig`; `hotel-service`: Resource Server; проверки ролей в security-конфиге |
| 7. Тестирование | (минимально) — акцент на ручной прогон + Swagger/Postman |
| 8. Структура БД | `entity` + `repository` в `booking-service` и `hotel-service` (H2, JPA) |
| 9. Предзаполнение данных | `BookingDataInitializer`, `HotelDataInitializer` |
| 10–11. Качество кода/читабельность | DTO/сервисы/валидация, Javadoc и “человечные” комментарии по ключевой логике |
| 12. Ошибки и надёжность | `GlobalExceptionHandler` в обоих сервисах, единый `ErrorDto` |
| 13. Логирование/трассировка | `X-Request-Id`, MDC/логирование ошибок, gateway фильтры |
| 14. Документация | Этот `README.md` + Swagger UI в `booking-service` и `hotel-service` |
| 15. Поддерживаемость тестов | N/A (в рамках учебного проекта приоритет — фичи и ручной прогон) |

---

## 8) Примечание про internal endpoints

Если пробовать дернуть internal endpoints через gateway, ожидаемо будет **404 / not routed**. Это сделано намеренно:

- `POST /api/rooms/{id}/confirm-availability`
- `POST /api/rooms/{id}/release`

Эти endpoints используются **только** сервисом `booking-service` для реализации саги.

---
