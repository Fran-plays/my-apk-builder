import dotenv from "dotenv";
dotenv.config();

function required(name: string, fallback = ""): string {
  const v = process.env[name] ?? fallback;
  if (process.env.NODE_ENV === "production" && !v) {
    // Warn only — mock providers keep the server usable without secrets.
    console.warn(`[config] ${name} is not set`);
  }
  return v;
}

export const config = {
  port: Number(process.env.PORT ?? 4000),
  env: process.env.NODE_ENV ?? "development",
  databaseUrl: required("DATABASE_URL", "postgres://postgres:postgres@localhost:5432/petmorph"),
  jwtSecret: required("JWT_SECRET", "dev-only-insecure-secret"),
  ai: {
    provider: (process.env.AI_PROVIDER ?? "mock") as "openai" | "mock",
    apiKey: required("AI_API_KEY"),
    model: process.env.AI_MODEL ?? "gpt-4o-mini",
    baseUrl: process.env.AI_BASE_URL ?? "https://api.openai.com/v1",
  },
  tts: {
    provider: (process.env.TTS_PROVIDER ?? "mock") as "cloud" | "mock",
    apiKey: required("TTS_API_KEY"),
    baseUrl: required("TTS_BASE_URL"),
  },
  image: {
    provider: (process.env.IMAGE_PROVIDER ?? "local") as "local" | "removebg",
    apiKey: required("IMAGE_API_KEY"),
  },
  storage: {
    provider: (process.env.STORAGE_PROVIDER ?? "local") as "local" | "s3",
    url: process.env.STORAGE_URL ?? "./data/storage",
  },
  maxUploadMb: Number(process.env.MAX_UPLOAD_MB ?? 10),
};
