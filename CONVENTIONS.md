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
| MySQL             | 3306 | mysql               |
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

Suggested bindings (implemented by owning services):

| Queue                       | Binding key(s)                                      |
|-----------------------------|-----------------------------------------------------|
| `parcel.status.queue`       | `parcel.assigned`, `parcel.pickedup`, `parcel.delivered` |
| `courier.availability.queue`| `parcel.assigned`, `parcel.delivered`               |

## Databases

| Service           | Schema      |
|-------------------|-------------|
| Parcel Service    | `parceldb`  |
| Courier Service   | `courierdb` |
| Delivery Service  | `deliverydb`|
| Auth Service      | `authdb`    |

MySQL app user: `courier` / `courier` (see `mysql-init/init.sql`).

## Auth / Gateway

- Client obtains JWT from Auth (`/auth/register`, `/auth/login`) via Gateway.
- Gateway validates JWT on `/api/**`; `/auth/**` is public.
- Client origin for CORS: `http://localhost:3000`
- Shared HMAC JWT secret: env `JWT_SECRET` (must be identical on `auth-service` and `gateway`)
- Default demo user: `admin` / `password` (seeded by auth-service)
- Gateway injects `X-API-KEY` per route and strips any client-supplied value
- Rate limiting: Redis-backed `RequestRateLimiter` per client IP
