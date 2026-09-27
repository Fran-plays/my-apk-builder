import rateLimit from "express-rate-limit";
import { config } from "../config";

export const apiLimiter = rateLimit({
  windowMs: 60_000,
  max: config.env === "production" ? 60 : 1000,
  standardHeaders: true,
  legacyHeaders: false,
  message: { error: "too many requests, slow down" },
});

export const aiLimiter = rateLimit({
  windowMs: 60_000,
  max: 20,
  standardHeaders: true,
  legacyHeaders: false,
  message: { error: "AI rate limit exceeded" },
});

export const uploadLimiter = rateLimit({
  windowMs: 60_000,
  max: 10,
  standardHeaders: true,
  legacyHeaders: false,
  message: { error: "upload rate limit exceeded" },
});
