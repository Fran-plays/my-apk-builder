import { Response } from "express";
import { AuthRequest } from "../middleware/auth";
import { CharacterService } from "../services/characterService";
import { createImageProcessor } from "../providers/imageProcessor";

const service = new CharacterService();
const imageProcessor = createImageProcessor();

export async function listCharacters(req: AuthRequest, res: Response) {
  res.json({ characters: await service.list(req.userId) });
}

export async function getCharacter(req: AuthRequest, res: Response) {
  res.json({ character: await service.get(req.params.id as string) });
}

export async function createCharacter(req: AuthRequest, res: Response) {
  if (!req.file) return res.status(400).json({ error: "image file is required" });
  // Validate magic bytes so a renamed executable can't slip past the MIME check
  const head = req.file.buffer.subarray(0, 12);
  const isPng = head.subarray(0, 8).equals(Buffer.from([0x89, 0x50, 0x4e, 0x47, 0x0d, 0x0a, 0x1a, 0x0a]));
  const isJpg = head[0] === 0xff && head[1] === 0xd8;
  const isWebp = head.subarray(8, 12).toString() === "WEBP";
  if (!isPng && !isJpg && !isWebp) {
    return res.status(415).json({ error: "file content does not match a supported image format" });
  }
  const processed = await imageProcessor.process(req.file.buffer);
  const body = { ...req.body };
  const character = await service.create(body, req.file.buffer, processed, req.userId);
  res.status(201).json({ character });
}

export async function deleteCharacter(req: AuthRequest, res: Response) {
  await service.remove(req.params.id as string);
  res.status(204).end();
}
