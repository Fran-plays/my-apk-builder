import { readFileSync } from "fs";
import path from "path";
import { pool } from "./db";
import { logger } from "../utils/logger";

async function migrate() {
  const sql = readFileSync(path.join(__dirname, "schema.sql"), "utf8");
  await pool.query(sql);
  logger.info("migration 001 applied");
  await pool.end();
}

migrate().catch((e) => { logger.error("migration failed", e); process.exit(1); });
