// AIChatBox.tsx
import React, { useState, useRef } from 'react';
// import { askGemini } from '../lib/ai';
import { Loader2 } from 'lucide-react';

export default function AIChatBox() {
  const [messages, setMessages] = useState([
    { role: 'system', text: 'Hi! I am your AI assistant. Ask me anything, or type /viral to run a viral search.' }
  ]);
  const [input, setInput] = useState('');
  const [loading, setLoading] = useState(false);
  const chatRef = useRef<HTMLDivElement>(null);

  async function handleSend() {
    if (!input.trim()) return;
    const userMsg = { role: 'user', text: input };
    setMessages(msgs => [...msgs, userMsg]);
    setInput('');
    setLoading(true);
    let aiMsg;
    if (input.trim().toLowerCase().startsWith('/viral')) {
      // Call backend API to run agent pipeline
      const res = await fetch('/api/viral-search', { method: 'POST', body: JSON.stringify({ prompt: input }) });
      const data = await res.json();
      aiMsg = { role: 'ai', text: data.result };
    } else {
      // Normal Gemini chat (now routed to backend API)
      const res = await fetch('/api/ai-chat', { method: 'POST', body: JSON.stringify({ prompt: input }) });
      const data = await res.json();
      aiMsg = { role: 'ai', text: data.result };
    }
    setMessages(msgs => [...msgs, aiMsg]);
    setLoading(false);
    setTimeout(() => chatRef.current?.scrollTo(0, chatRef.current.scrollHeight), 100);
  }

  return (
    <div className="flex flex-col w-full max-w-xl mx-auto h-[600px] border rounded-lg shadow-lg bg-white">
      <div ref={chatRef} className="flex-1 overflow-y-auto p-4 space-y-4">
        {messages.map((msg, i) => (
          <div key={i} className={`flex ${msg.role === 'user' ? 'justify-end' : 'justify-start'}`}>
            <div className={`rounded-lg px-4 py-2 max-w-[80%] ${msg.role === 'user' ? 'bg-blue-500 text-white' : 'bg-gray-100 text-gray-900'}`}>{msg.text}</div>
          </div>
        ))}
        {loading && <div className="flex justify-start"><Loader2 className="animate-spin" /> <span className="ml-2">Thinking...</span></div>}
      </div>
      <div className="p-4 border-t flex gap-2">
        <input
          className="flex-1 border rounded px-3 py-2 focus:outline-none"
          placeholder="Type your message..."
          value={input}
          onChange={e => setInput(e.target.value)}
          onKeyDown={e => e.key === 'Enter' && handleSend()}
          disabled={loading}
        />
        <button
          className="bg-blue-600 text-white px-4 py-2 rounded disabled:opacity-50"
          onClick={handleSend}
          disabled={loading}
        >Send</button>
      </div>
    </div>
  );
}
