import { Request, Response } from "express";
import { checkDb } from "../database/db";

export async function health(_req: Request, res: Response) {
  const db = await checkDb();
  res.json({ status: "ok", db, uptime: process.uptime() });
}
