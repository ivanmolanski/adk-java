/**
 * GitHub Models Provider (Direct API) - Privacy focused, no telemetry.
 * Supports usage alongside existing Firebase Gemini integration.
 */

export const GITHUB_MODELS = {
  GPT_4O: 'openai/gpt-4o',
  GPT_4: 'openai/gpt-4',
  GPT_35_TURBO: 'openai/gpt-3.5-turbo'
} as const;

export type GitHubModelId = typeof GITHUB_MODELS[keyof typeof GITHUB_MODELS];

export interface ChatMessage { role: 'system' | 'user' | 'assistant'; content: string }
interface GitHubChatChoiceMessage { role: string; content: string }
interface GitHubChatChoice { message?: GitHubChatChoiceMessage; finish_reason?: string }
interface GitHubChatResponse { choices?: GitHubChatChoice[] }

function getGitHubToken(): string {
  const token = process.env.GITHUB_TOKEN || process.env.NEXT_PUBLIC_GITHUB_TOKEN;
  if (!token) throw new Error('GITHUB_TOKEN environment variable not set');
  return token;
}

export async function githubChatComplete(params: {
  model: GitHubModelId;
  messages: ChatMessage[];
  temperature?: number;
  max_tokens?: number;
  signal?: AbortSignal;
}): Promise<string> {
  const response = await fetch('https://models.github.ai/inference/chat/completions', {
    method: 'POST',
    headers: {
      'Authorization': `Bearer ${getGitHubToken()}`,
      'Content-Type': 'application/json'
    },
    body: JSON.stringify({
      model: params.model,
      messages: params.messages,
      temperature: params.temperature ?? 1.0,
      max_tokens: params.max_tokens ?? 1024
    }),
    signal: params.signal
  });

  if (!response.ok) {
    const text = await response.text();
    throw new Error(`GitHub chat completion failed ${response.status}: ${text}`);
  }
  const json: GitHubChatResponse = await response.json();
  const content = json.choices && json.choices[0] && json.choices[0].message?.content;
  if (!content) throw new Error('No content returned from GitHub Models API');
  return content;
}

export async function askGitHub(model: GitHubModelId, prompt: string): Promise<string> {
  return githubChatComplete({
    model,
    messages: [
      { role: 'system', content: 'You are a helpful assistant.' },
      { role: 'user', content: prompt }
    ]
  });
}
