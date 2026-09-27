import { TTSProvider } from "./types";
import { config } from "../config";

/** Offline TTS fallback: returns a valid, tiny WAV tone (placeholder audio).
 * Replace with a real cloud TTS by implementing TTSProvider. */
export class MockTTSProvider implements TTSProvider {
  async synthesize(text: string): Promise<Buffer> {
    const rate = 22050;
    const seconds = Math.min(2, 0.3 + text.length * 0.03);
    const n = Math.floor(rate * seconds);
    const data = Buffer.alloc(44 + n * 2);
    data.write("RIFF", 0); data.writeUInt32LE(36 + n * 2, 4); data.write("WAVE", 8);
    data.write("fmt ", 12); data.writeUInt32LE(16, 16); data.writeUInt16LE(1, 20);
    data.writeUInt16LE(1, 22); data.writeUInt32LE(rate, 24);
    data.writeUInt32LE(rate * 2, 28); data.writeUInt16LE(2, 32); data.writeUInt16LE(16, 34);
    data.write("data", 36); data.writeUInt32LE(n * 2, 40);
    for (let i = 0; i < n; i++) {
      const t = i / rate;
      const env = Math.min(1, t * 10) * Math.min(1, (seconds - t) * 5);
      const v = Math.sin(2 * Math.PI * (440 + (i % 400)) * t) * 0.2 * env;
      data.writeInt16LE(Math.floor(v * 32767), 44 + i * 2);
    }
    return data;
  }
}

/** Cloud TTS abstraction — fill in endpoint for your provider of choice. */
export class CloudTTSProvider implements TTSProvider {
  async synthesize(text: string, voice?: string): Promise<Buffer> {
    const res = await fetch(`${config.tts.baseUrl}/v1/text-to-speech/${voice ?? "default"}`, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        "xi-api-key": config.tts.apiKey,
      },
      body: JSON.stringify({ text, model_id: "eleven_monolingual_v1" }),
    });
    if (!res.ok) throw new Error(`TTS provider error: ${res.status}`);
    return Buffer.from(await res.arrayBuffer());
  }
}

export function createTTSProvider(): TTSProvider {
  return config.tts.provider === "cloud" && config.tts.apiKey && config.tts.baseUrl
    ? new CloudTTSProvider()
    : new MockTTSProvider();
}
