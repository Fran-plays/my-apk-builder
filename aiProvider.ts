import { AIProvider } from "./types";
import { config } from "../config";
import { logger } from "../utils/logger";

/** OpenAI-compatible chat provider. Swap AI_PROVIDER env to switch. */
export class OpenAIProvider implements AIProvider {
  async chat(opts: {
    systemPrompt: string; name: string; message: string;
    maxLength: number; language: string;
  }): Promise<string> {
    const res = await fetch(`${config.ai.baseUrl}/chat/completions`, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        Authorization: `Bearer ${config.ai.apiKey}`,
      },
      body: JSON.stringify({
        model: config.ai.model,
        messages: [
          { role: "system", content: opts.systemPrompt },
          { role: "user", content: opts.message },
        ],
        max_tokens: Math.ceil(opts.maxLength / 2),
      }),
    });
    if (!res.ok) throw new Error(`AI provider error: ${res.status}`);
    const data = (await res.json()) as {
      choices?: { message?: { content?: string } }[];
    };
    const reply = data.choices?.[0]?.message?.content;
    if (!reply) throw new Error("AI provider returned empty reply");
    return reply.slice(0, opts.maxLength);
  }
}

/** Offline deterministic responder — keeps chat usable without any AI key. */
export class MockAIProvider implements AIProvider {
  async chat(opts: {
    systemPrompt: string; name: string; message: string;
    maxLength: number; language: string;
  }): Promise<string> {
    logger.debug("mock AI chat", { name: opts.name });
    const m = opts.message.toLowerCase();
    const pick = (a: string[]) => a[Math.floor(Math.random() * a.length)];
    let reply: string;
    if (m.includes("how are you")) reply = pick([
      "I'm doing great! Let's hang out!", "Super happy to see you today!",
    ]);
    else if (m.includes("hello") || m.includes("hi")) reply = pick([
      `Hi! I'm ${opts.name}!`, "Hello hello! Best day ever?",
    ]);
    else if (m.includes("bye")) reply = "Come back soon, okay?";
    else reply = pick([
      "Tell me more, I'm all ears!", "That's so interesting!",
      "Hehe, I love talking with you!",
    ]);
    return reply.slice(0, opts.maxLength);
  }
}

export function createAIProvider(): AIProvider {
  return config.ai.provider === "openai" && config.ai.apiKey
    ? new OpenAIProvider()
    : new MockAIProvider();
}
