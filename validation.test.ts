import { createCharacterSchema, chatSchema, ttsSchema } from "../src/models/character";

describe("input validation", () => {
  it("accepts a valid character", () => {
    const r = createCharacterSchema.safeParse({
      name: "Mochi", type: "cute_pet", personality: "energetic",
    });
    expect(r.success).toBe(true);
  });
  it("rejects oversize names and bad enums", () => {
    expect(createCharacterSchema.safeParse({ name: "x".repeat(41), type: "cute_pet", personality: "cute" }).success).toBe(false);
    expect(createCharacterSchema.safeParse({ name: "A", type: "dragon", personality: "cute" }).success).toBe(false);
  });
  it("rejects empty chat messages and oversize tts text", () => {
    expect(chatSchema.safeParse({ message: "" }).success).toBe(false);
    expect(ttsSchema.safeParse({ text: "y".repeat(1001) }).success).toBe(false);
  });
});
