# Using GitHub Models with ADK Java

GitHub Models provides free, OpenAI-compatible inference API access for all GitHub users. This removes the barrier of requiring paid API keys for open source projects.

## Setup

1. Get a GitHub Personal Access Token (PAT) with `models:read` permission, or use the built-in `GITHUB_TOKEN` in GitHub Actions.

2. Add GitHub Models to your ADK agent:

```java
import com.google.adk.agents.LlmAgent;
import com.google.adk.models.GitHubModels;

// Create the GitHubModels instance
GitHubModels githubModel = GitHubModels.builder()
    .modelName("openai/gpt-4o")  // or other available models
    .token(System.getenv("GITHUB_TOKEN"))  // or your PAT
    .build();

// Use with an LlmAgent
LlmAgent agent = LlmAgent.builder()
    .name("github_assistant")
    .description("An assistant powered by GitHub Models")
    .model(githubModel)
    .instruction("You are a helpful assistant.")
    .build();
```

## Available Models

GitHub Models supports various models including:
- `openai/gpt-4o`
- `openai/gpt-4o-mini`
- `deepseek/deepseek-r1`
- `meta-llama/llama-3.3-70b-instruct`
- And more...

## Authentication

### Using Personal Access Token
```java
GitHubModels model = GitHubModels.builder()
    .modelName("openai/gpt-4o")
    .token("your-github-pat-here")
    .build();
```

### Using Environment Variables
```java
GitHubModels model = GitHubModels.builder()
    .modelName("openai/gpt-4o")
    .token(System.getenv("GITHUB_TOKEN"))
    .build();
```

### In GitHub Actions
When running in GitHub Actions, you can use the built-in `GITHUB_TOKEN`:

```yaml
permissions:
  contents: read
  models: read   # Required for GitHub Models access

jobs:
  ai-task:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - name: Run AI agent
        env:
          GITHUB_TOKEN: ${{ secrets.GITHUB_TOKEN }}
        run: java -jar your-agent.jar
```

## Configuration Options

```java
GitHubModels model = GitHubModels.builder()
    .modelName("openai/gpt-4o")
    .token(System.getenv("GITHUB_TOKEN"))
    .baseUrl("https://models.github.ai/inference/chat/completions")  // Optional custom URL
    .build();
```

## Benefits

- **Free**: No API keys required, uses your GitHub account
- **OpenAI Compatible**: Drop-in replacement for OpenAI-based workflows
- **GitHub Actions Ready**: Works seamlessly in CI/CD with built-in tokens
- **No Setup Friction**: Every GitHub user can run your AI-powered tools immediately

## Example Usage in Agent

```java
import com.google.adk.agents.LlmAgent;
import com.google.adk.models.GitHubModels;

public class GitHubModelsExample {
    public static void main(String[] args) {
        // Create GitHubModels instance
        GitHubModels model = GitHubModels.builder()
            .modelName("openai/gpt-4o")
            .token(System.getenv("GITHUB_TOKEN"))
            .build();

        // Create agent with GitHub Models
        LlmAgent agent = LlmAgent.builder()
            .name("code_reviewer")
            .description("AI code reviewer using GitHub Models")
            .model(model)
            .instruction("You are a helpful code reviewer. Provide constructive feedback.")
            .build();

        // Use the agent
        String response = agent.generateSync("Review this code: public class Hello { }");
        System.out.println(response);
    }
}
```

This makes AI features accessible to all GitHub users without requiring paid API subscriptions, removing barriers for open source adoption.