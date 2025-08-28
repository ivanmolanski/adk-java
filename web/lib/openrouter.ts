export async function askOpenRouter(prompt: string): Promise<string> {
  const key = process.env.NEXT_PUBLIC_OPENROUTER_API_KEY;
  if (!key) throw new Error('OPENROUTER API key not configured');

  const resp = await fetch('https://api.openrouter.ai/v1/chat/completions', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${key}`,
    },
    body: JSON.stringify({
      model: 'gpt-4o-mini-1',
      messages: [{ role: 'user', content: prompt }]
    }),
  });

  if (!resp.ok) {
    const txt = await resp.text();
    throw new Error(`OpenRouter error: ${resp.status} ${txt}`);
  }

  const data = await resp.json();
  // Safely extract first choice text
  const content = data?.choices?.[0]?.message?.content || data?.choices?.[0]?.text || '';
  return content;
}
