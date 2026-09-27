import { Request, Response, NextFunction } from "express";
import { logger } from "../utils/logger";

export class HttpError extends Error {
  constructor(public status: number, message: string) { super(message); }
}

export function notFound(_req: Request, res: Response) {
  res.status(404).json({ error: "not found" });
}

export function errorHandler(err: unknown, _req: Request, res: Response, _next: NextFunction) {
  const status = err instanceof HttpError ? err.status : 500;
  if (status >= 500) logger.error("unhandled error", err);
  res.status(status).json({
    error: err instanceof Error ? err.message : "internal server error",
  });
}
