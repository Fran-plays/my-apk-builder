import { Router } from "express";
import rateLimit from "express-rate-limit";
import * as characters from "../controllers/charactersController";
import * as ai from "../controllers/aiController";
import * as health from "../controllers/healthController";
import { optionalAuth, requireAuth } from "../middleware/auth";
import { upload } from "../middleware/upload";
import { apiLimiter, aiLimiter, uploadLimiter } from "../middleware/rateLimit";
import { createImageProcessor } from "../providers/imageProcessor";
import { HttpError } from "../middleware/errorHandler";

const router = Router();
const imageProcessor = createImageProcessor();

router.use(apiLimiter);

router.get("/health", health.health);

router.get("/characters", optionalAuth, characters.listCharacters);
router.post("/characters/create", optionalAuth, uploadLimiter, upload.single("image"), characters.createCharacter);
router.get("/characters/:id", optionalAuth, characters.getCharacter);
router.delete("/characters/:id/delete", optionalAuth, requireAuth, characters.deleteCharacter);

router.post("/ai/chat", aiLimiter, ai.chat);
router.post("/tts", aiLimiter, ai.ttsHandler);

// JSON variant of image processing (Android app uses local pipeline by default)
router.post("/image/process", uploadLimiter, upload.single("image"), async (req, res, next) => {
  try {
    if (!req.file) throw new HttpError(400, "image file is required");
    const out = await imageProcessor.process(req.file.buffer);
    res.json({ imageBase64: out.toString("base64") });
  } catch (e) { next(e); }
});

export default router;
