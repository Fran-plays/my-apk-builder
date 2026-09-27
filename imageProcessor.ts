import sharp from "sharp";
import { ImageProcessor } from "./types";
import { config } from "../config";

/** Local processing: trim borders + resize. Transparent-background removal from
 * a single arbitrary image needs ML; the app also does on-device flood-fill
 * removal. This provider normalizes whatever the app sends. */
export class LocalImageProcessor implements ImageProcessor {
  async process(input: Buffer): Promise<Buffer> {
    return sharp(input)
      .trim({ threshold: 20 })
      .resize(512, 512, { fit: "inside", withoutEnlargement: true })
      .png()
      .toBuffer();
  }
}

/** Remote background removal (remove.bg-compatible API). */
export class RemoteImageProcessor implements ImageProcessor {
  async process(input: Buffer): Promise<Buffer> {
    const form = new FormData();
    form.append("image_file", new Blob([new Uint8Array(input)]), "image.png");
    form.append("size", "auto");
    const res = await fetch("https://api.remove.bg/v1.0/removebg", {
      method: "POST",
      headers: { "X-Api-Key": config.image.apiKey },
      body: form,
    });
    if (!res.ok) throw new Error(`image provider error: ${res.status}`);
    return Buffer.from(await res.arrayBuffer());
  }
}

export function createImageProcessor(): ImageProcessor {
  return config.image.provider === "removebg" && config.image.apiKey
    ? new RemoteImageProcessor()
    : new LocalImageProcessor();
}
