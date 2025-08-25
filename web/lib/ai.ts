import { initializeApp } from "firebase/app";
import { getAI, getGenerativeModel, GoogleAIBackend } from "firebase/ai";

const firebaseConfig = {
  apiKey: "AIzaSyAPDjj37OF9fdO2nsq2Qezwea-xGfPJRlA",
  authDomain: "contentforge-ai-ygy25.firebaseapp.com",
  projectId: "contentforge-ai-ygy25",
  storageBucket: "contentforge-ai-ygy25.firebasestorage.app",
  messagingSenderId: "51060608349",
  appId: "1:51060608349:web:12c14f56648ced0ae96cb4"
};

const firebaseApp = initializeApp(firebaseConfig);
const ai = getAI(firebaseApp, { backend: new GoogleAIBackend() });
export const geminiModel = getGenerativeModel(ai, { model: "gemini-2.5-flash" });

export async function askGemini(prompt: string): Promise<string> {
  const result = await geminiModel.generateContent(prompt);
  return result.response.text();
}
