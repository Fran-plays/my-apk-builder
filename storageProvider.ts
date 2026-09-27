import { promises as fs } from "fs";
import path from "path";
import { StorageProvider } from "./types";
import { config } from "../config";

export class LocalStorageProvider implements StorageProvider {
  private root = path.resolve(config.storage.url);

  async put(key: string, data: Buffer, contentType: string): Promise<string> {
    const safe = path.normalize(key).replace(/^([/\\])+/, "");
    const file = path.join(this.root, safe);
    await fs.mkdir(path.dirname(file), { recursive: true });
    await fs.writeFile(file, data);
    return `/storage/${safe}`;
  }
  async get(key: string): Promise<Buffer | null> {
    try { return await fs.readFile(path.join(this.root, key)); }
    catch { return null; }
  }
  async delete(key: string): Promise<void> {
    await fs.rm(path.join(this.root, key), { force: true });
  }
}

/** S3-compatible storage — implement with @aws-sdk/client-s3 in production. */
export class S3StorageProvider implements StorageProvider {
  async put(): Promise<string> { throw new Error("S3StorageProvider not configured — set STORAGE_PROVIDER=local or implement S3"); }
  async get(): Promise<Buffer | null> { throw new Error("S3 not configured"); }
  async delete(): Promise<void> { throw new Error("S3 not configured"); }
}

export function createStorageProvider(): StorageProvider {
  return config.storage.provider === "s3" ? new S3StorageProvider() : new LocalStorageProvider();
}
