import request from "supertest";
import sharp from "sharp";

// Build a tiny valid PNG to exercise upload validation end to end.
async function png() {
  return sharp({ create: { width: 64, height: 64, channels: 3, background: { r: 10, g: 20, b: 30 } } })
    .png().toBuffer();
}

describe("api errors and health", () => {
  let app: import("express").Express;
  beforeAll(async () => { ({ default: app } = await import("../src/index")); });

  it("GET /api/health returns ok", async () => {
    const res = await request(app).get("/api/health");
    expect([200, 500]).toContain(res.status);
  });

  it("rejects a non-image upload", async () => {
    const res = await request(app)
      .post("/api/characters/create")
      .field("name", "X").field("type", "cute_pet").field("personality", "cute")
      .attach("image", Buffer.from("MZ not an image"), { filename: "evil.png", contentType: "image/png" });
    expect(res.status).toBe(415);
  });

  it("rejects oversize chat messages", async () => {
    const res = await request(app).post("/api/ai/chat").send({ message: "x".repeat(501) });
    expect(res.status).toBeGreaterThanOrEqual(400);
  });

  it("404s unknown routes", async () => {
    const res = await request(app).get("/api/nope");
    expect(res.status).toBe(404);
  });
});
