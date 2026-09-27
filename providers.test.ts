import { MockAIProvider } from "../src/providers/aiProvider";
import { MockTTSProvider } from "../src/providers/ttsProvider";
import { LocalImageProcessor } from "../src/providers/imageProcessor";
import sharp from "sharp";

describe("providers", () => {
  it("mock AI returns a short reply honoring maxLength", async () => {
    const p = new MockAIProvider();
    const reply = await p.chat({ systemPrompt: "s", name: "Mochi", message: "how are you?", maxLength: 50, language: "auto" });
    expect(reply.length).toBeGreaterThan(0);
    expect(reply.length).toBeLessThanOrEqual(50);
  });

  it("mock TTS returns a valid WAV file", async () => {
    const wav = await new MockTTSProvider().synthesize("hello");
    expect(wav.subarray(0, 4).toString()).toBe("RIFF");
    expect(wav.subarray(8, 12).toString()).toBe("WAVE");
  });

  it("local image processor outputs a smaller PNG", async () => {
    const src = await sharp({
      create: { width: 1200, height: 1200, channels: 4, background: { r: 255, g: 100, b: 150, alpha: 0 } },
    }).png().toBuffer();
    const out = await new LocalImageProcessor().process(src);
    const meta = await sharp(out).metadata();
    expect(meta.format).toBe("png");
    expect(Math.max(meta.width!, meta.height!)).toBeLessThanOrEqual(512);
  });
});
