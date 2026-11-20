import React, { useState, useRef, useEffect } from 'react';
import { useMutation } from '@tanstack/react-query';
import {
  Bot,
  Send,
  Sparkles,
  User,
  ShieldAlert,
  Terminal,
  RefreshCw,
  HelpCircle,
  ShieldCheck,
  Tag,
  ArrowRight,
} from 'lucide-react';
import { Card } from '../../components/ui/Card';
import { Button } from '../../components/ui/Button';
import { useToast } from '../../context/ToastContext';
import { aiService } from '../../services/aiService';
import { AiChatResponse } from '../../types/ai';

interface ChatMessage {
  id: string;
  sender: 'user' | 'assistant';
  text: string;
  timestamp: string;
  entities?: string[];
  suggestedPrompts?: string[];
  model?: string;
}

export const AiAssistantPage: React.FC = () => {
  const toast = useToast();
  const messagesEndRef = useRef<HTMLDivElement>(null);

  const [input, setInput] = useState('');
  const [messages, setMessages] = useState<ChatMessage[]>([
    {
      id: 'welcome',
      sender: 'assistant',
      text: "Hello! I am your FlowGuard AI Monitoring Assistant. I continuously correlate telemetry signals across your microservices, distributed transaction traces, health probes, and open incident post-mortems.\n\nAsk me any diagnostic question about system latency, active outages, or failing API endpoints.",
      timestamp: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }),
      model: 'Ollama (qwen2.5:7b)',
      suggestedPrompts: [
        'Why did the payment service fail?',
        'Show slowest APIs and latency bottlenecks',
        'Summarize active engineering incidents',
      ],
    },
  ]);

  const scrollToBottom = () => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  };

  useEffect(() => {
    scrollToBottom();
  }, [messages]);

  const chatMutation = useMutation({
    mutationFn: (promptText: string) =>
      aiService.chatWithCopilot({
        prompt: promptText,
        conversationHistory: messages.map((m) => ({
          role: m.sender === 'user' ? 'user' : 'assistant',
          content: m.text,
        })),
      }),
    onSuccess: (data: AiChatResponse) => {
      const assistantMsg: ChatMessage = {
        id: Math.random().toString(),
        sender: 'assistant',
        text: data.response,
        timestamp: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }),
        entities: data.referencedEntities,
        suggestedPrompts: data.suggestedNextPrompts,
        model: data.model,
      };
      setMessages((prev) => [...prev, assistantMsg]);
    },
    onError: () => {
      toast.error('AI Assistant Error', 'Failed to generate response. Try rephrasing.');
      const errorMsg: ChatMessage = {
        id: Math.random().toString(),
        sender: 'assistant',
        text: "I encountered a transient error querying the observability index. Please verify backend connectivity and try again.",
        timestamp: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }),
      };
      setMessages((prev) => [...prev, errorMsg]);
    },
  });

  const handleSend = (textToSend?: string) => {
    const query = (textToSend || input).trim();
    if (!query) return;

    const userMsg: ChatMessage = {
      id: Math.random().toString(),
      sender: 'user',
      text: query,
      timestamp: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }),
    };

    setMessages((prev) => [...prev, userMsg]);
    if (!textToSend) setInput('');
    chatMutation.mutate(query);
  };

  const starterPrompts = [
    'Why did the payment service fail?',
    'Show slowest APIs and latency trends',
    'Summarize active engineering incidents',
    'Check inventory reservation health status',
    'Identify cascade timeout bottlenecks',
    'Explain distributed transaction waterfall',
  ];

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h1 className="text-xl font-bold tracking-tight text-white flex items-center gap-2">
            <Sparkles className="w-5 h-5 text-electric-400" /> AI Monitoring Assistant &amp; Copilot
          </h1>
          <p className="text-xs text-slate-400 mt-0.5">
            Natural language inquiry over live telemetry signals, distributed trace spans, and incident root cause analysis
          </p>
        </div>

        <div className="flex items-center gap-2">
          <div className="flex items-center gap-1.5 px-2.5 py-1 rounded-full bg-navy-900 border border-navy-750 text-xs text-electric-300">
            <Bot className="w-3.5 h-3.5 text-electric-400" />
            <span>OpenAI + FlowGuard Telemetry Engine</span>
          </div>
        </div>
      </div>

      {/* Main Chat Interface */}
      <Card className="flex flex-col h-[640px] p-0 overflow-hidden">
        {/* Messages Scroll Area */}
        <div className="flex-1 overflow-y-auto p-4 space-y-4">
          {messages.map((m) => (
            <div
              key={m.id}
              className={`flex items-start gap-3 ${
                m.sender === 'user' ? 'flex-row-reverse' : 'flex-row'
              }`}
            >
              <div
                className={`w-8 h-8 rounded-full flex items-center justify-center flex-shrink-0 ${
                  m.sender === 'user'
                    ? 'bg-electric-600 text-white'
                    : 'bg-navy-800 border border-electric-500/40 text-electric-400'
                }`}
              >
                {m.sender === 'user' ? <User className="w-4 h-4" /> : <Bot className="w-4 h-4" />}
              </div>

              <div
                className={`max-w-2xl rounded-2xl p-4 text-xs space-y-2 leading-relaxed ${
                  m.sender === 'user'
                    ? 'bg-electric-600 text-white rounded-tr-none'
                    : 'bg-navy-900 border border-navy-750 text-slate-200 rounded-tl-none shadow-lg'
                }`}
              >
                <div className="whitespace-pre-wrap font-sans">{m.text}</div>

                {/* Referenced Entities */}
                {m.entities && m.entities.length > 0 && (
                  <div className="pt-2 border-t border-navy-750 flex flex-wrap items-center gap-1.5">
                    <span className="text-[10px] text-slate-400 font-semibold flex items-center gap-1">
                      <Tag className="w-3 h-3 text-slate-400" /> Correlated:
                    </span>
                    {m.entities.map((ent) => (
                      <span
                        key={ent}
                        className="px-2 py-0.5 rounded bg-navy-950 border border-navy-700 text-[10px] font-mono text-electric-300"
                      >
                        {ent}
                      </span>
                    ))}
                  </div>
                )}

                {/* Suggested Next Prompts */}
                {m.suggestedPrompts && m.suggestedPrompts.length > 0 && (
                  <div className="pt-2 border-t border-navy-750 space-y-1.5">
                    <span className="text-[10px] text-slate-400 font-semibold block">
                      Suggested Investigation Follow-ups:
                    </span>
                    <div className="flex flex-wrap gap-1.5">
                      {m.suggestedPrompts.map((sug) => (
                        <button
                          key={sug}
                          onClick={() => handleSend(sug)}
                          className="px-2.5 py-1 rounded-lg bg-navy-950 border border-navy-700 hover:border-electric-500/60 hover:text-white text-[11px] text-slate-300 transition-colors flex items-center gap-1 group"
                        >
                          <span>{sug}</span>
                          <ArrowRight className="w-3 h-3 opacity-0 group-hover:opacity-100 transition-opacity text-electric-400" />
                        </button>
                      ))}
                    </div>
                  </div>
                )}

                <div className="flex items-center justify-between pt-1 text-[10px] text-slate-400">
                  <span>{m.model || ''}</span>
                  <span>{m.timestamp}</span>
                </div>
              </div>
            </div>
          ))}

          {/* Typing Loading Indicator */}
          {chatMutation.isPending && (
            <div className="flex items-start gap-3">
              <div className="w-8 h-8 rounded-full bg-navy-800 border border-electric-500/40 text-electric-400 flex items-center justify-center flex-shrink-0 animate-pulse">
                <Bot className="w-4 h-4" />
              </div>
              <div className="p-3.5 rounded-2xl rounded-tl-none bg-navy-900 border border-navy-750 text-xs text-slate-400 flex items-center gap-2">
                <RefreshCw className="w-3.5 h-3.5 animate-spin text-electric-400" />
                <span>Correlating logs, traces, and metrics...</span>
              </div>
            </div>
          )}

          <div ref={messagesEndRef} />
        </div>

        {/* Starter Prompts Bar (when few messages) */}
        {messages.length <= 2 && (
          <div className="px-4 py-2 border-t border-navy-800/80 bg-navy-950/40">
            <div className="text-[11px] text-slate-400 mb-1.5 font-medium">Quick Starters:</div>
            <div className="flex flex-wrap gap-1.5">
              {starterPrompts.map((prompt) => (
                <button
                  key={prompt}
                  onClick={() => handleSend(prompt)}
                  className="px-2.5 py-1 rounded-md bg-navy-900 border border-navy-750 hover:border-electric-500/60 text-[11px] text-slate-300 hover:text-white transition-colors"
                >
                  {prompt}
                </button>
              ))}
            </div>
          </div>
        )}

        {/* Input Bar */}
        <div className="p-3 bg-navy-900 border-t border-navy-750">
          <form
            onSubmit={(e) => {
              e.preventDefault();
              handleSend();
            }}
            className="flex gap-2"
          >
            <input
              type="text"
              placeholder="Ask FlowGuard AI about an incident, failing API, or latency spike..."
              value={input}
              onChange={(e) => setInput(e.target.value)}
              disabled={chatMutation.isPending}
              className="flex-1 bg-navy-950 border border-navy-750 text-xs rounded-xl px-4 py-2.5 text-slate-200 placeholder-slate-500 focus:outline-none focus:border-electric-500"
            />
            <Button
              variant="primary"
              size="md"
              type="submit"
              disabled={!input.trim() || chatMutation.isPending}
              leftIcon={<Send className="w-4 h-4" />}
            >
              Ask
            </Button>
          </form>

          <div className="flex items-center justify-between mt-2 px-1 text-[10px] text-slate-400">
            <span className="flex items-center gap-1">
              <ShieldCheck className="w-3 h-3 text-emerald-400" /> Sensitive tokens and PII are redacted prior to inference.
            </span>
            <span>Safety disclaimer: AI responses are advisory only.</span>
          </div>
        </div>
      </Card>
    </div>
  );
};
