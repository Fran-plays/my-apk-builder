import { randomUUID } from "crypto";
import { query } from "../database/db";
import { HttpError } from "../middleware/errorHandler";
import { CharacterRow, createCharacterSchema } from "../models/character";
import { createStorageProvider } from "../providers/storageProvider";
import { logger } from "../utils/logger";

const storage = createStorageProvider();

export class CharacterService {
  async list(userId?: string): Promise<CharacterRow[]> {
    return userId
      ? query<CharacterRow>("SELECT * FROM characters WHERE user_id = $1 ORDER BY created_at DESC", [userId])
      : query<CharacterRow>("SELECT * FROM characters ORDER BY created_at DESC LIMIT 100");
  }

  async get(id: string): Promise<CharacterRow> {
    const rows = await query<CharacterRow>("SELECT * FROM characters WHERE id = $1", [id]);
    if (!rows[0]) throw new HttpError(404, "character not found");
    return rows[0];
  }

  async create(
    input: unknown,
    originalImage: Buffer,
    processedImage: Buffer,
    userId?: string,
  ): Promise<CharacterRow> {
    const data = createCharacterSchema.parse(input);
    const id = randomUUID();
    const originalUrl = await storage.put(`characters/${id}/original.png`, originalImage, "image/png");
    const processedUrl = await storage.put(`characters/${id}/processed.png`, processedImage, "image/png");
    const rows = await query<CharacterRow>(
      `INSERT INTO characters (id, user_id, name, type, personality, original_image_url, processed_image_url)
       VALUES ($1,$2,$3,$4,$5,$6,$7) RETURNING *`,
      [id, userId ?? null, data.name, data.type, data.personality, originalUrl, processedUrl],
    );
    logger.info("character created", { id, name: data.name });
    return rows[0];
  }

  async remove(id: string): Promise<void> {
    const existing = await query("SELECT id FROM characters WHERE id = $1", [id]);
    if (!existing[0]) throw new HttpError(404, "character not found");
    await query("DELETE FROM characters WHERE id = $1", [id]);
    await storage.delete(`characters/${id}/original.png`);
    await storage.delete(`characters/${id}/processed.png`);
  }
}
