// Minimal Genkit config for MCP server integration
import { defineFlow } from "@genkit-ai/flow";

export const helloFlow = defineFlow({
  name: "helloFlow",
  inputSchema: {
    type: "object",
    properties: {
      name: { type: "string" }
    },
    required: ["name"]
  },
  run: async ({ name }) => {
    return { greeting: `Hello, ${name}!` };
  }
});
