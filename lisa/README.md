# Lisa

Lisa is a local-first Next.js prototype for thoughtful conversation with a fictional, adult AI companion.

For the current implementation status and next steps, see [PROJECT_STATUS.md](PROJECT_STATUS.md).

## Run locally

Node.js 20.9 or newer is required.

```bash
npm install
npm run dev
```

Open http://localhost:3000.

## Local database with Docker

Docker Desktop with WSL integration is required. Start the PostgreSQL database with:

```bash
docker compose up -d
```

The database is available at `localhost:5432` and is initialized automatically from `database/init`. The connection string is documented in `.env.example`.

To stop the database without deleting its data:

```bash
docker compose down
```

To remove the database and its persistent volume:

```bash
docker compose down -v
```

## Private developer login

The developer workspace is available at `/dev` and redirects unauthenticated visitors to `/dev/login`. Set these variables in the shell environment before starting the API and Next.js:

```bash
export LISA_API_KEY="$(openssl rand -hex 32)"
export LISA_ADMIN_USERNAME="admin"
export LISA_ADMIN_PASSWORD="use-a-long-private-password"
```

The Java API creates the initial admin record on first startup. Passwords are stored in PostgreSQL as a unique-salt, iterated PBKDF2-HMAC-SHA-256 hash. The password itself is never stored. The browser receives only an HttpOnly, SameSite session cookie after login.

Do not commit these environment variables or place real credentials in `.env.example`.

## Prototype boundaries

- Conversations are saved in the browser's local storage on this device.
- Replies are scripted for the prototype and do not require an API key.
- Lisa is fictional and intended for PG-13 conversation.
- The Anthropic SDK is listed for a future server-side AI integration; no secret key is needed for the current prototype.
