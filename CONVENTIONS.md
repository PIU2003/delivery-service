# Shared conventions — Parcel Delivery System
# Keep these names/ports/headers identical across all services.

## Ports

| Component         | Port | Hostname (Docker)   |
|-------------------|------|---------------------|
| Client (Nginx)    | 3000 | client              |
| API Gateway       | 8080 | gateway             |
| Parcel Service    | 8081 | parcel-service      |
| Courier Service   | 8082 | courier-service     |
| Delivery Service  | 8083 | delivery-service    |
| Auth Service      | 8084 | auth-service        |
| MySQL (auth only) | 3306 | mysql               |
| Mongo Parcel      | 27016| mongo-parcel        |
| Mongo Courier     | 27018| mongo-courier       |
| Mongo Delivery    | 27019| mongo-delivery      |
| RabbitMQ AMQP     | 5672 | rabbitmq            |
| RabbitMQ UI       | 15672| rabbitmq            |
| Redis             | 6379 | redis               |

## Package prefix

`com.courier.*` — e.g. `com.courier.delivery`, `com.courier.parcel`, `com.courier.courier`, `com.courier.auth`, `com.courier.gateway`

## API key (service-to-service)

- Header name: `X-API-KEY`
- Gateway injects the correct key when proxying; microservices reject requests without a valid key.
- Default keys (override via environment / compose):

| Service           | Env var              | Default value           |
|-------------------|----------------------|-------------------------|
| Parcel Service    | `API_KEY`            | `parcel-service-key`    |
| Courier Service   | `API_KEY`            | `courier-service-key`   |
| Delivery Service  | `API_KEY`            | `delivery-service-key`  |
| Auth Service      | `API_KEY` (optional) | `auth-service-key`      |

Delivery → Courier sync calls use `COURIER_API_KEY` (= courier service key).

## RabbitMQ contract

- Exchange: `delivery.exchange` (type: **topic**)
- Routing keys:
  - `parcel.assigned`
  - `parcel.pickedup`
  - `parcel.delivered`
- Queues:
  - `parcel.status.queue` (Parcel Service consumer)
  - `courier.availability.queue` (Courier Service consumer)
- Event IDs (`parcelId`, `courierId`, `deliveryId`) are **String** Mongo ObjectIds.

Suggested bindings (implemented by owning services):

| Queue                       | Binding key(s)                                      |
|-----------------------------|-----------------------------------------------------|
| `parcel.status.queue`       | `parcel.assigned`, `parcel.pickedup`, `parcel.delivered` |
| `courier.availability.queue`| `parcel.assigned`, `parcel.delivered`               |

## Databases

| Service           | Engine | Database / container | Host (Compass) |
|-------------------|--------|----------------------|----------------|
| Parcel Service    | MongoDB | `parceldb` / `mongo-parcel` | `mongodb://localhost:27016` |
| Courier Service   | MongoDB | `courierdb` / `mongo-courier` | `mongodb://localhost:27018` |
| Delivery Service  | MongoDB | `deliverydb` / `mongo-delivery` | `mongodb://localhost:27019` |
| Auth Service      | MySQL   | `authdb` / `mysql` | `localhost:3306` |

MySQL app user (auth only): `courier` / `courier` (see `mysql-init/init.sql`).
MongoDB has no auth in the local demo.

Document IDs are MongoDB ObjectId hex strings (not numeric).

## Auth / Gateway

- Client obtains JWT from Auth (`/auth/register`, `/auth/login`) via Gateway.
- Gateway validates JWT on `/api/**`; `/auth/**` is public.
- Client origin for CORS: `http://localhost:3000`
- Shared HMAC JWT secret: env `JWT_SECRET` (must be identical on `auth-service` and `gateway`)
- Default demo user: `admin` / `password` (seeded by auth-service)
- Gateway injects `X-API-KEY` per route and strips any client-supplied value
- Rate limiting: Redis-backed `RequestRateLimiter` per client IP

## Demo seed data

Seeders run once when the target collection/table is empty (idempotent on restart with existing data):

| Service | Seed content |
|---------|----------------|
| Auth | user `admin` / `password` |
| Parcel | 2 × `PENDING` parcels |
| Courier | available couriers in `Colombo` (×2) and `Kandy` (×1) |
| Delivery | none (created by assign flow) |
