import { z } from "zod";

export const createCharacterSchema = z.object({
  name: z.string().trim().min(1).max(40),
  type: z.enum(["cute_pet", "anime_character", "mascot", "vtuber_mini"]),
  personality: z.enum(["cute", "funny", "calm", "energetic", "tsundere", "friendly", "chaotic"]),
});

export const chatSchema = z.object({
  characterId: z.string().uuid().optional(),
  name: z.string().trim().min(1).max(40).default("Companion"),
  personality: z.string().trim().max(40).default("friendly"),
  message: z.string().trim().min(1).max(500),
  maxLength: z.number().int().min(20).max(500).default(140),
  language: z.string().trim().max(10).default("auto"),
});

export const ttsSchema = z.object({
  text: z.string().trim().min(1).max(1000),
  voice: z.string().trim().max(60).optional(),
});

export interface CharacterRow {
  id: string;
  user_id: string | null;
  name: string;
  type: string;
  personality: string;
  original_image_url: string;
  processed_image_url: string;
  voice_config: unknown;
  animation_config: unknown;
  created_at: string;
}

export const PERSONALITY_PROMPTS: Record<string, string> = {
  cute: "You are an adorable, sweet companion. Use gentle, affectionate language and short sentences.",
  funny: "You are a playful, witty companion. Make light family-friendly jokes.",
  calm: "You are a serene, thoughtful companion. Speak softly and reassuringly.",
  energetic: "You are a hyper, excited companion. Use exclamation marks and upbeat language.",
  tsundere: "You act aloof and tsundere but secretly care. Keep it playful and original.",
  friendly: "You are a warm, supportive best-friend companion.",
  chaotic: "You are an unpredictable, chaotic but harmless companion.",
};
