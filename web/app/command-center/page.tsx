'use client';
import { useState, useRef, useEffect } from 'react';
import { useViralIntelligence } from '@/hooks/useViralIntelligence';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';
import { Send, Bot, User, Zap, TrendingUp, FileText, Play } from 'lucide-react';
import { Alert, AlertDescription } from '@/components/ui/alert';

interface ChatMessage {
  id: string;
  type: 'user' | 'assistant';
  content: string;
  timestamp: Date;
  actions?: Array<{
    label: string;
    action: string;
    variant?: 'default' | 'outline';
  }>;
}

export default function CommandCenterPage() {
  const [messages, setMessages] = useState<ChatMessage[]>([
    {
      id: '1',
      type: 'assistant',
      content: 'Welcome to the MD Aesthetics AI Command Center! I\'m your intelligent assistant for viral content strategy. I can help you:\n\n• Analyze trending competitor content\n• Generate compliant social media drafts\n• Trigger scraping and research tasks\n• Provide strategic recommendations\n\nHow can I assist you today?',
      timestamp: new Date(),
      actions: [
        { label: 'Start Scraping', action: 'trigger_scraping' },
        { label: 'Get Latest Trends', action: 'get_trends' },
        { label: 'Generate Content', action: 'create_content' }
      ]
    }
  ]);
  
  const [currentMessage, setCurrentMessage] = useState('');
  const [isProcessing, setIsProcessing] = useState(false);
  const messagesEndRef = useRef<HTMLDivElement>(null);
  const { refreshData, trends, drafts, dailyBrief, isLoading, error } = useViralIntelligence();

  const scrollToBottom = () => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  };

  useEffect(() => {
    scrollToBottom();
  }, [messages]);

  const handleSendMessage = async (messageContent?: string) => {
    const content = messageContent || currentMessage.trim();
    if (!content) return;

    const userMessage: ChatMessage = {
      id: Date.now().toString(),
      type: 'user',
      content,
      timestamp: new Date()
    };

    setMessages(prev => [...prev, userMessage]);
    setCurrentMessage('');
    setIsProcessing(true);

    try {
      // Simulate AI processing
      setTimeout(async () => {
        const response = await processCommand(content);
        const assistantMessage: ChatMessage = {
          id: (Date.now() + 1).toString(),
          type: 'assistant',
          content: response.content,
          timestamp: new Date(),
          actions: response.actions
        };
        setMessages(prev => [...prev, assistantMessage]);
        setIsProcessing(false);
      }, 1000);
    } catch (error) {
      const errorMessage: ChatMessage = {
        id: (Date.now() + 1).toString(),
        type: 'assistant',
        content: 'I encountered an error processing your request. Please try again or contact support if the issue persists.',
        timestamp: new Date()
      };
      setMessages(prev => [...prev, errorMessage]);
      setIsProcessing(false);
    }
  };

  const processCommand = async (command: string): Promise<{ content: string; actions?: Array<{ label: string; action: string; variant?: 'default' | 'outline' }> }> => {
    const lowerCommand = command.toLowerCase();

    if (lowerCommand.includes('scraping') || lowerCommand.includes('scrape')) {
      // TODO: Implement scraping trigger logic
      return {
        content: '🚀 Scraping job initiated! I\'m now collecting the latest posts from our competitor profiles (_thelookaesthetics, subtle.enhancements, skinvitalityofficial). This will take 2-3 minutes. I\'ll analyze the content for viral patterns and generate MD Aesthetics-compliant drafts.',
        actions: [
          { label: 'Check Status', action: 'check_status' },
          { label: 'View Results', action: 'view_results' }
        ]
      };
    }

    if (lowerCommand.includes('trend') || lowerCommand.includes('viral')) {
      await refreshData();
      return {
        content: '📊 I\'ve refreshed the latest trend analysis. Based on current data, I\'m seeing strong engagement with:\n\n• **Science-based content** - BBL technology explanations\n• **Process transparency** - Treatment walkthrough videos\n• **Transformation stories** - Before/after with timelines\n\nShall I generate specific content recommendations for MD Aesthetics?',
        actions: [
          { label: 'Generate BBL Content', action: 'create_bbl_content' },
          { label: 'Create SkinTyte Draft', action: 'create_skintyte_content' },
          { label: 'Duo-C-Lift Story', action: 'create_duoclift_content' }
        ]
      };
    }

    if (lowerCommand.includes('content') || lowerCommand.includes('generate') || lowerCommand.includes('create')) {
      return {
        content: '✨ I can generate compliant content for MD Aesthetics! Here are some options based on current trends:\n\n**High-Performance Content Types:**\n• Educational posts about treatment science\n• Process demystification videos\n• Patient transformation journeys\n• Professional myth-busting content\n\nWhich type interests you most?',
        actions: [
          { label: 'Educational Science', action: 'create_science_content' },
          { label: 'Process Demo', action: 'create_process_content' },
          { label: 'Transformation', action: 'create_transformation_content' }
        ]
      };
    }

    if (lowerCommand.includes('help') || lowerCommand.includes('what can you do')) {
      return {
        content: '🤖 I\'m your AI strategist for MD Aesthetics! Here\'s what I can do:\n\n**Content Intelligence:**\n• Monitor competitor viral content\n• Analyze engagement patterns\n• Identify trending topics\n\n**Content Creation:**\n• Generate compliant social media posts\n• Create hashtag strategies\n• Adapt viral concepts to MD brand\n\n**Strategic Analysis:**\n• Daily market briefings\n• Performance recommendations\n• Trend forecasting\n\nTry commands like "start scraping", "analyze trends", or "create BBL content"!',
        actions: [
          { label: 'Start Analysis', action: 'trigger_scraping' },
          { label: 'View Dashboard', action: 'view_dashboard' },
          { label: 'Generate Content', action: 'create_content' }
        ]
      };
    }

    // Default intelligent response
    return {
      content: `I understand you're asking about "${command}". Let me help you with that! Based on MD Aesthetics' focus on physician-led, results-driven treatments, I can assist with content strategy for:\n\n• **Duo-C-Lift** combinations\n• **SkinTyte** body contouring\n• **BBL** skin rejuvenation\n• **Radiesse** biostimulation\n\nWould you like me to analyze current trends for any of these services?`,
      actions: [
        { label: 'Analyze Current Trends', action: 'get_trends' },
        { label: 'Create Custom Content', action: 'create_custom_content' }
      ]
    };
  };

  const handleQuickAction = async (action: string) => {
    const actionMessages: { [key: string]: string } = {
      trigger_scraping: 'Start scraping competitor content',
      get_trends: 'Show me the latest viral trends',
      create_content: 'Generate content for Instagram',
      create_bbl_content: 'Create BBL educational content',
      create_skintyte_content: 'Generate SkinTyte process video script',
      create_duoclift_content: 'Write Duo-C-Lift transformation story',
      check_status: 'Check scraping job status',
      view_results: 'Show me the results',
      view_dashboard: 'Take me to the dashboard'
    };

    if (action === 'view_dashboard') {
      window.location.href = '/dashboard';
      return;
    }

    const message = actionMessages[action] || action;
    await handleSendMessage(message);
  };

  const formatTimestamp = (date: Date) => {
    return date.toLocaleTimeString('en-US', { 
      hour: '2-digit', 
      minute: '2-digit' 
    });
  };

  return (
    <div className="container mx-auto p-6 h-screen flex flex-col">
      {/* Header */}
      <div className="mb-6">
        <div className="flex justify-between items-center">
          <div>
            <h1 className="text-3xl font-bold flex items-center gap-2">
              <Bot className="w-8 h-8" />
              AI Command Center
            </h1>
            <p className="text-muted-foreground">Intelligent assistant for viral content strategy</p>
          </div>
          <div className="flex items-center space-x-2">
            <Badge variant="default">Online</Badge>
            <Badge variant="outline">MD Aesthetics</Badge>
          </div>
        </div>
      </div>

      {/* Service Status Alert */}
      {/* Service is always online for now. TODO: Add health check logic. */}

      {/* Chat Messages */}
      <Card className="flex-1 flex flex-col">
        <CardHeader>
          <CardTitle className="text-lg">Chat with AI Assistant</CardTitle>
          <CardDescription>
            Ask me about trends, content generation, or competitive analysis
          </CardDescription>
        </CardHeader>
        <CardContent className="flex-1 flex flex-col">
          <div className="flex-1 overflow-y-auto space-y-4 mb-4">
            {messages.map((message) => (
              <div
                key={message.id}
                className={`flex gap-3 ${
                  message.type === 'user' ? 'justify-end' : 'justify-start'
                }`}
              >
                <div
                  className={`flex gap-2 max-w-[80%] ${
                    message.type === 'user' ? 'flex-row-reverse' : 'flex-row'
                  }`}
                >
                  <div className="flex-shrink-0">
                    {message.type === 'user' ? (
                      <div className="w-8 h-8 rounded-full bg-primary flex items-center justify-center">
                        <User className="w-4 h-4 text-primary-foreground" />
                      </div>
                    ) : (
                      <div className="w-8 h-8 rounded-full bg-blue-100 flex items-center justify-center">
                        <Bot className="w-4 h-4 text-blue-600" />
                      </div>
                    )}
                  </div>
                  <div
                    className={`rounded-lg p-3 ${
                      message.type === 'user'
                        ? 'bg-primary text-primary-foreground'
                        : 'bg-gray-100'
                    }`}
                  >
                    <div className="whitespace-pre-wrap text-sm">
                      {message.content}
                    </div>
                    <div className="text-xs opacity-70 mt-1">
                      {formatTimestamp(message.timestamp)}
                    </div>
                    {message.actions && (
                      <div className="flex flex-wrap gap-2 mt-3">
                        {message.actions.map((action, index) => (
                          <Button
                            key={index}
                            variant={action.variant === 'outline' ? 'outline' : 'default'}
                            size="sm"
                            onClick={() => handleQuickAction(action.action)}
                            className="h-7 text-xs"
                          >
                            {action.label}
                          </Button>
                        ))}
                      </div>
                    )}
                  </div>
                </div>
              </div>
            ))}
            
            {isProcessing && (
              <div className="flex gap-3 justify-start">
                <div className="flex gap-2">
                  <div className="w-8 h-8 rounded-full bg-blue-100 flex items-center justify-center">
                    <Bot className="w-4 h-4 text-blue-600" />
                  </div>
                  <div className="rounded-lg p-3 bg-gray-100">
                    <div className="flex items-center gap-2 text-sm">
                      <div className="flex space-x-1">
                        <div className="w-2 h-2 bg-gray-400 rounded-full animate-bounce"></div>
                        <div className="w-2 h-2 bg-gray-400 rounded-full animate-bounce" style={{ animationDelay: '0.1s' }}></div>
                        <div className="w-2 h-2 bg-gray-400 rounded-full animate-bounce" style={{ animationDelay: '0.2s' }}></div>
                      </div>
                      Processing...
                    </div>
                  </div>
                </div>
              </div>
            )}
            <div ref={messagesEndRef} />
          </div>

          {/* Input Area */}
          <div className="border-t pt-4">
            <div className="flex gap-2">
              <input
                type="text"
                value={currentMessage}
                onChange={(e) => setCurrentMessage(e.target.value)}
                onKeyPress={(e) => e.key === 'Enter' && handleSendMessage()}
                placeholder="Type your message... (e.g., 'analyze trends', 'create BBL content', 'start scraping')"
                className="flex-1 px-3 py-2 border border-input rounded-md bg-background"
                disabled={isProcessing}
              />
              <Button 
                onClick={() => handleSendMessage()}
                disabled={isProcessing || !currentMessage.trim()}
              >
                <Send className="w-4 h-4" />
              </Button>
            </div>
          </div>
        </CardContent>
      </Card>
    </div>
  );
}
