export interface OrchestrationOptions { posts?: any[] }
export interface OrchestrationResult {
  started: string;
  targetsDiscovered: number;
  cse: { total?: number; terms?: number; saved?: number };
  enrichedPosts: number;
  platformCounts?: { [platform: string]: number };
  durationMs?: number;
  status: string;
  error?: string;
}
export function runOrchestration(options?: OrchestrationOptions): Promise<OrchestrationResult>;
