import { Pool } from "pg";

const globalForDb = globalThis as unknown as { lisaPool?: Pool };

export const db = globalForDb.lisaPool ?? new Pool({
  connectionString: process.env.DATABASE_URL ?? "postgresql://lisa:lisa_dev_password@localhost:5432/lisa",
});

if (process.env.NODE_ENV !== "production") globalForDb.lisaPool = db;
