# FYP Network — Backend

NestJS + Prisma + PostgreSQL API for the rebuilt social/professional network.
Everything here runs locally for free — no paid services required.

## Stack
- **NestJS** (TypeScript) — modular, DI-based API framework
- **Prisma** — type-safe ORM + migrations against Postgres
- **PostgreSQL 16** — via Docker
- **MinIO** — local S3-compatible object storage, for post/profile images
- **JWT** (access + rotating refresh tokens) — bcrypt-hashed passwords and refresh tokens
- **Swagger** — auto-generated API docs at `/api/docs`

## Prerequisites
- Node.js 20+
- Docker Desktop (or Docker Engine + Compose)

## First-time setup

```bash
cd backend
cp .env.example .env       # defaults already match docker-compose.yml
npm install

# start Postgres + MinIO locally
docker compose up -d

# generate the Prisma client and create the database schema
npx prisma generate
npx prisma migrate dev --name init

# optional: seed two test users + a sample post
npx prisma db seed

# start the API in watch mode
npm run start:dev
```

The API is now at `http://localhost:3000/api`, with interactive docs at
`http://localhost:3000/api/docs`. The MinIO web console is at
`http://localhost:9001` (login: `fyp_minio_admin` / `fyp_minio_password`).

## Test login (if you ran the seed)
- `jane.doe@example.com` / `password123`
- `john.smith@example.com` / `password123`

## Useful commands

| Command | What it does |
|---|---|
| `npm run start:dev` | Run API with hot reload |
| `npx prisma studio` | Visual DB browser at localhost:5555 |
| `npx prisma migrate dev` | Create/apply a new migration after schema changes |
| `npm run test` | Unit tests |
| `docker compose down -v` | Tear down containers **and delete data** |

## Updating an existing local setup

If you already had this running before and are pulling in the latest changes,
there are two things beyond the usual `git pull` / re-extract:

**1. A new migration** — the schema picked up `Task.completionPercent`,
`Notification.groupId`, and a new `GROUP_ADDED` notification type:

```bash
npm install
npx prisma generate
npx prisma migrate dev --name phase6_fixes
```

**2. A new `.env` variable** — `.env` is gitignored, so it doesn't get
overwritten when you pull in changes, which means this one needs adding by
hand. Without it, images silently fail to load with no error anywhere: the
backend was resolving image URLs using the same address it uses to talk to
MinIO internally (`localhost`), but the Android emulator's `localhost` refers
to the emulator itself, not your machine - the exact same class of issue as
the API's own `10.0.2.2` requirement, just for MinIO. Add this line to your
existing `backend/.env`:

```
S3_PUBLIC_ENDPOINT="http://10.0.2.2:9000"
```

Then restart the backend:
```bash
npm run start:dev
```

## Connecting from the Android app
- **Emulator**: use `http://10.0.2.2:3000/api` as the base URL — this is the
  emulator's special alias for your host machine's `localhost`.
- **Physical device**: use your machine's LAN IP, e.g. `http://192.168.1.23:3000/api`,
  and make sure your machine's firewall allows inbound connections on port 3000/9000.

## Security notes (vs. the original PHP version)
- All queries go through Prisma's parameterized query builder — no string-interpolated SQL, so no SQL injection surface.
- Passwords are hashed with bcrypt (cost factor 12), not SHA-1.
- Refresh tokens are stored **hashed** in the DB and rotated on every use (single-use).
- DB/S3 credentials live in `.env` (gitignored), never in source.
