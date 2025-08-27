import { geminiModel } from "@/lib/ai";

export async function generateGeminiContent(prompt: string): Promise<string> {
  const result = await geminiModel.generateContent(prompt);
  const response = result.response;
  return response.text();
}
