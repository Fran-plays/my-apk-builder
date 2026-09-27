import { Request, Response } from "express";
import { createAIProvider } from "../providers/aiProvider";
import { createTTSProvider } from "../providers/ttsProvider";
import { chatSchema, ttsSchema, PERSONALITY_PROMPTS } from "../models/character";

const ai = createAIProvider();
const tts = createTTSProvider();

export async function chat(req: Request, res: Response) {
  const data = chatSchema.parse(req.body);
  const systemPrompt =
    (PERSONALITY_PROMPTS[data.personality.toLowerCase()] ?? PERSONALITY_PROMPTS.friendly) +
    ` You are ${data.name}. Reply in ${data.language === "auto" ? "the user's language" : data.language}. ` +
    `Keep answers under ${data.maxLength} characters. Never break character.`;
  const reply = await ai.chat({
    systemPrompt,
    name: data.name,
    message: data.message,
    maxLength: data.maxLength,
    language: data.language,
  });
  res.json({ reply });
}

export async function ttsHandler(req: Request, res: Response) {
  const data = ttsSchema.parse(req.body);
  const audio = await tts.synthesize(data.text, data.voice);
  res.setHeader("Content-Type", "audio/wav");
  res.send(audio);
}
