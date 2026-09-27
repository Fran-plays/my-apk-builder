export interface AIProvider {
  chat(opts: {
    systemPrompt: string;
    name: string;
    message: string;
    maxLength: number;
    language: string;
  }): Promise<string>;
}

export interface TTSProvider {
  synthesize(text: string, voice?: string): Promise<Buffer>;
}

export interface ImageProcessor {
  process(input: Buffer): Promise<Buffer>; // returns PNG with transparent background
}

export interface StorageProvider {
  put(key: string, data: Buffer, contentType: string): Promise<string>; // returns URL
  get(key: string): Promise<Buffer | null>;
  delete(key: string): Promise<void>;
}
