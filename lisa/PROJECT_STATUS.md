# Lisa Project Status

Last updated: 2026-09-05

## Current architecture

- Next.js frontend in `src/app`
- Normal user login at `/login`
- Protected user chat at `/`
- Private admin workspace at `/dev/login` and `/dev`
- Java Spring Boot API in `api/`
- PostgreSQL database in Docker
- Database schema and seed files in `database/init/`
- Chat messages are stored in PostgreSQL
- Cross-tab chat updates use dirty events via `BroadcastChannel` with a `localStorage` fallback

## Authentication

- Admin accounts are stored in `admin_users`.
- Normal accounts are stored in `users`.
- Passwords use a unique salt and PBKDF2-HMAC-SHA-256 hashing.
- Admin sessions use the `lisa_session` HttpOnly cookie.
- Normal user sessions use the `lisa_user_session` HttpOnly cookie.
- `/dev` is protected by middleware and requires a valid admin session.
- Unauthenticated access to `/` redirects to `/login`.
- API access is protected by `LISA_API_KEY`.

## Current local services

- Web app: `http://localhost:3002`
- Java API: `http://localhost:8080`
- PostgreSQL: `localhost:5432`
- Docker services: `lisa-db` and `lisa-api`

## Environment

Local credentials are stored in `.env`, which is ignored by Git. Do not copy real values into `.env.example` or commit `.env`.

Required local variables include:

- `LISA_API_KEY`
- `LISA_ADMIN_USERNAME`
- `LISA_ADMIN_PASSWORD`
- `LISA_USER_USERNAME`
- `LISA_USER_PASSWORD`
- `LISA_USER_CONVERSATION_ID`
- `JAVA_API_URL`

## Current task status

- [x] Docker PostgreSQL database
- [x] Java API build and container
- [x] Admin login and protected developer workspace
- [x] Normal user login
- [x] User and admin session cookies
- [x] Persistent conversations and messages
- [x] Local image database and image response flow
- [x] Dirty-event updates without interval polling
- [x] Redesigned user login page
- [ ] Registration flow for new users
- [ ] Logout controls in both authenticated interfaces
- [ ] Production-grade session storage instead of in-memory Java sessions
- [ ] Automated API tests in `api/src/test/java`

## Next implementation plan

1. Complete user registration with server-side validation and a new conversation per account.
2. Add logout buttons and invalidate sessions in the Java API.
3. Replace in-memory sessions with persistent, expiring session records in PostgreSQL or a dedicated session store.
4. Add controller, authentication, authorization, and repository tests.
5. Move frontend API calls to the Java API directly or document the Next.js proxy boundary for production deployment.
6. Replace development credentials with generated deployment secrets before production use.

## Start locally

```bash
cd /home/julia/Projekte/lisa
docker compose up -d --build
npm run dev -- --hostname 0.0.0.0 --port 3002
```

Open `http://localhost:3002/login` for users or `http://localhost:3002/dev/login` for the admin workspace.
