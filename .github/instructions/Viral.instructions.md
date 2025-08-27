---
applyTo: '**'
---
THIS SOLUTINO HAS THE FRAMEORK ALREADY BUILD.  YOU NEED TO ANALYZE WHAT HAS BEEN COMPLETED, CONTINUE WHERE YOU LEFT OFF, CODE, CO?NFIGURE, TEST THE SOLUTION.  YOU WILL NEED TO CREATE THE AGENTS, AND CREATE THE SCAFFOLDING, THE WEB GUI, THE BACK END LOGIC.  YOU WILL ASK ME WHAT IS REQUIRED FOR KEYS, AND ACCESS.  YOU WILL NOT MINIMIZE, ENTER PLACE HOLDERS, DEMO, SKIP ANYTHING.  THIS IS THE FINAL PRODUCT NOW.  // Import the functions you need from the SDKs you need
import { initializeApp } from "firebase/app";
// TODO: Add SDKs for Firebase products that you want to use
// https://firebase.google.com/docs/web/setup#available-libraries

// Your web app's Firebase configuration
const firebaseConfig = {
  apiKey: process.env.NEXT_PUBLIC_FIREBASE_API_KEY,
  authDomain: process.env.NEXT_PUBLIC_FIREBASE_AUTH_DOMAIN,
  projectId: process.env.NEXT_PUBLIC_FIREBASE_PROJECT_ID,
  storageBucket: process.env.NEXT_PUBLIC_FIREBASE_STORAGE_BUCKET,
  messagingSenderId: process.env.NEXT_PUBLIC_FIREBASE_MESSAGING_SENDER_ID,
  appId: process.env.NEXT_PUBLIC_FIREBASE_APP_ID
};

// Initialize Firebase
const app = initializeApp(firebaseConfig);

___

Skip to content
Navigation Menu
google
adk-java

Type / to search
Code
Issues
27
Pull requests
9
Discussions
Actions
Security
Insights
Owner avatar
adk-java
Public
google/adk-java
Go to file
t
Name		
Poggeccicopybara-github
Poggecci
and
copybara-github
fix: IncludeContents.None not including user message in request
c0302b6
 · 
5 hours ago
.github
chore: Fail CI build if there are wrongly formatted files (re. #168)
3 weeks ago
.mvn/wrapper
chore: Add Maven ./mvnw wrapper to fix version and avoid having to se…
3 months ago
contrib
bug: Move listArtifacts() from ToolContext to the parent `Callbac…
4 days ago
core
fix: IncludeContents.None not including user message in request
5 hours ago
dev
feat: Adds support for YAML-based basic agents
last week
maven_plugin
refactor: Renames AgentProvider to AgentLoader and renames `getAg…
15 hours ago
.gitattributes
chore: Add Maven ./mvnw wrapper to fix version and avoid having to se…
3 months ago
.gitignore
fix: multiple tool requests with langchain4j
last month
CONTRIBUTING.md
ADK changes
2 months ago
LICENSE
internal
3 months ago
README.md
doc: Add maven badge for README.md
yesterday
java.header
internal
3 months ago
license-checks.xml
internal
3 months ago
mvnw
chore: Add Maven ./mvnw wrapper to fix version and avoid having to se…
3 months ago
mvnw.cmd
chore: Add Maven ./mvnw wrapper to fix version and avoid having to se…
3 months ago
pom.xml
feat: Adds mvn google-adk:web ... cli via maven plugin to allow use…
yesterday
Repository files navigation
README
Code of conduct
Contributing
Apache-2.0 license
Security
Agent Development Kit (ADK) for Java
License Maven Central r/agentdevelopmentkit


An open-source, code-first Java toolkit for building, evaluating, and deploying sophisticated AI agents with flexibility and control.
Important Links: Docs & Samples & Python ADK.
Agent Development Kit (ADK) is designed for developers seeking fine-grained control and flexibility when building advanced AI agents that are tightly integrated with services in Google Cloud. It allows you to define agent behavior, orchestration, and tool use directly in code, enabling robust debugging, versioning, and deployment anywhere – from your laptop to the cloud.

✨ Key Features
Rich Tool Ecosystem: Utilize pre-built tools, custom functions, OpenAPI specs, or integrate existing tools to give agents diverse capabilities, all for tight integration with the Google ecosystem.

Code-First Development: Define agent logic, tools, and orchestration directly in Java for ultimate flexibility, testability, and versioning.

Modular Multi-Agent Systems: Design scalable applications by composing multiple specialized agents into flexible hierarchies.

🚀 Installation
If you're using Maven, add the following to your dependencies:

<dependency>
  <groupId>com.google.adk</groupId>
  <artifactId>google-adk</artifactId>
  <version>0.2.0</version>
</dependency>
<!-- Dev UI -->
<dependency>
    <groupId>com.google.adk</groupId>
    <artifactId>google-adk-dev</artifactId>
    <version>0.2.0</version>
</dependency>
To instead use an unreleased version, you could use https://jitpack.io/#google/adk-java/; see https://github.com/enola-dev/LearningADK#jitpack for an example illustrating this.

📚 Documentation
For building, evaluating, and deploying agents by follow the Java documentation & samples:

Documentation
Samples
🏁 Feature Highlight
Same Features & Familiar Interface As Python ADK:
import com.google.adk.agents.LlmAgent;
import com.google.adk.tools.GoogleSearchTool;

LlmAgent rootAgent = LlmAgent.builder()
    .name("search_assistant")
    .description("An assistant that can search the web.")
    .model("gemini-2.5-flash") // Or your preferred models
    .instruction("You are a helpful assistant. Answer user questions using Google Search when needed.")
    .tools(new GoogleSearchTool())
    .build();
Development UI
Same as the beloved Python Development UI. A built-in development UI to help you test, evaluate, debug, and showcase your agent(s). 

Evaluate Agents
Coming soon...

🤖 A2A and ADK integration
For remote agent-to-agent communication, ADK integrates with the A2A protocol. Examples coming soon...

🤝 Contributing
We welcome contributions from the community! Whether it's bug reports, feature requests, documentation improvements, or code contributions, please see our Contributing Guidelines to get started.

📄 License
This project is licensed under the Apache 2.0 License - see the LICENSE file for details.

Preview
This feature is subject to the "Pre-GA Offerings Terms" in the General Service Terms section of the Service Specific Terms. Pre-GA features are available "as is" and might have limited support. For more information, see the launch stage descriptions.

Happy Agent Building!

About
An open-source, code-first Java toolkit for building, evaluating, and deploying sophisticated AI agents with flexibility and control.

google.github.io/adk-docs/
Topics
agent ai multi-agent agents multi-agent-systems ai-agents multi-agent-system multi-agents llm llms genai agentic genai-chatbot agentic-workflow agentic-ai multi-agents-collaboration aiagentframework agents-sdk
Resources
 Readme
License
 Apache-2.0 license
Code of conduct
 Code of conduct
Contributing
 Contributing
Security policy
 Security policy
 Activity
 Custom properties
Stars
 525 stars
Watchers
 20 watching
Forks
 100 forks
Report repository
Releases 1
v0.2.0
Latest
on Jun 30
Packages
No packages published
Contributors
17
@google-genai-bot
@glaforge
@shukladivyansh
@cornellgit
@copybara-github
@Poggecci
@vorburger
@xinhuagu
@jsmeredith
@vero-code
@Jacksunwei
@kpavlov
@kaikreuzer
@Doris26
+ 3 contributors
Languages
Java
93.5%
 
HTML
6.3%
 
JavaScript
0.2%
Footer
© 2025 GitHub, Inc.
Footer navigation
Terms
Privacy
Security
Status
Docs
Contact
Manage cookies
Do not share my personal information


__


NO PYTHON, NO DOCKER, UNLESS NECESSARY, ASK ME.  

This is the document that bridges your vision with the AI's execution.

Final Project Blueprint & Mission Directive for the Auto-Coder

To the AI Coder:

What follows is the master plan for the "Viral Forge" project. We have moved beyond generic specifications. This document is now infused with the specific strategic intelligence of the client, MDAesthetics. Your mission is to build an AI system that understands and executes this strategy flawlessly.

Part 1: The Mission DNA - Understanding MDAesthetics

Before you write a single line of code, you must understand who we are. I have analyzed the client's digital presence, and this is the core identity you must embed into the system's logic:

Brand Identity: Physician-Led, Results-Driven, Clinically Sophisticated. This is not a fluffy, trendy spa. It is a medical practice focused on aesthetic science and tangible outcomes. The tone is always professional, authoritative, educational, and trustworthy. The aesthetic is clean, bright, and uncluttered.

Key Content Pillars (What We Do): The system must understand that we don't chase every aesthetic trend. We focus on our high-value, specialized services. These are the priorities:

Advanced Combination Therapies: Our "Duo-C-Lift" (Ultherapy + Radiesse) is a signature offering. It's a key differentiator and a prime topic for educational content.

Specialized Body Contouring: We target specific, real-world problems like skin laxity and cellulite on the buttocks, knees, and decollete, using treatments like SkinTyte. The "FIRM + LIFT + SMOOTH BUTTOCK Package" is a core, high-value offering.

Physician-Grade Injectables: We emphasize the science of injectables like Radiesse (a biostimulator) vs. standard Hyaluronic fillers. The value proposition is the expertise of a physician-led team.

Medical-Grade Technology & Skincare: Content must often explain the technology (BBL, Moxi, SkinTyte) and the ingredients (Vivier Vitamin C). We sell results based on science.

Part 2: The Strategic Search Mandate - What "Viral" Means For Us

The system will not search for "viral aesthetic videos" in general. That would be a waste of resources. It is being programmed to hunt for specific types of viral content that are directly relevant to the MDAesthetics content pillars. Before the agents "learn" on their own, you will hard-code this focus into their initial search parameters.

The agents must seek viral content that falls into these categories:

"The Process Demystified": Videos showing a treatment being performed (like SkinTyte or filler injection) but in a clean, professional, and reassuring way. These answer the patient's silent questions: "What does it look like?" "Is it clinical?" "What is the immediate recovery?"

"The Science Explained Simply": Viral content that takes a complex topic (e.g., "How does a biostimulator rebuild collagen?") and breaks it down with simple animations, text overlays, or a direct-to-camera explanation.

"Dramatic & Relatable Transformations": High-quality before-and-afters that tell a clear "Problem/Solution" story, especially for our key services like the Duo-C-Lift or body firming. The virality comes from the relatability of the problem and the impressiveness of the result.

"Expert Myth-Busting": Content where a credible professional (like Dr. Copeland) debunks common skincare myths or dangerous "at-home" trends. This content builds immense authority and trust.

Part 3: Tying It All Together - The Final Blueprint for Action

You are to now take the technical plan from our previous discussions and implement it with the following, highly-specific agent instructions. This is where the strategy becomes executable code.

The Puppeteer Scraper (Cloud Function):

Seed List: Your primary scraping targets will be the Instagram and TikTok accounts for _thelookaesthetics, subtle.enhancements, and Skin Vitality, along with the hashtags #torontoaesthetics, #whitbymedspa, #duoclift, #skintyte, and #radiesse.

The TrendAnalyzer Agent (LlmAgent):

Instruction: "You are a social media analyst for a high-end medical practice. From the provided post data, extract the content category it fits into: 'Process Demystified', 'Science Explained', 'Transformation', or 'Myth Busting'. Isolate the 3-second hook, the call-to-action, and the core educational point. Output this as a structured JSON object."

The ContentCreator Agent (LlmAgent):

CRITICAL INSTRUCTION: "Your persona is Dr. Copeland's trusted clinical strategist. You are to create a new, superior social media post based on the TrendAnalyzer's report. Review the attached image mdaesthetics-august-promo.jpg for current business priorities.
Your new post MUST:

Adapt the successful viral hook, but pivot the topic to one of our core pillars: the Duo-C-Lift, the Tyte & Tone Body Bundle, the Firm + Lift Buttock Package, or Skin Boosting with Hyaluronic.

Explain the 'Why' behind the treatment. If discussing the body bundle, explain why Vivier Vitamin C scrub is effective for skin texture or how SkinTyte uses infrared light to firm the skin. Be educational and clinical.

Maintain our brand identity. The tone is professional and authoritative. The language is results-oriented ("firmness," "smoothing," "increase volume").

Create a Clear Call-to-Action. End the post by prompting users to book a consultation to see if they are a candidate, or direct them to the link in bio.

Generate Hashtags. Provide a mix of broad local (#torontoaesthetics, #whitby), service-specific (#skintytetreatment, #ultherapy), and branded (#mdaesthetics) hashtags."

This blueprint is your complete directive. Your goal is to build a system that thinks and creates content with the same strategic focus and brand integrity as the MDAesthetics team itself. Execute this plan to deliver a tool that doesn't just automate tasks, but generates real, measurable business value.


___>

Of course. Here is the blueprint again, starting with a revised, more personalized introduction that frames the entire project as a collaborative effort, just as you requested. The technical specifications that follow remain the same, as they are now tailored to your business.

You are absolutely correct. My last blueprint was technically solid, but it lacked the most important ingredient: the specific, nuanced identity of MDAesthetics. A generic plan for a medspa is not a winning strategy for your medspa.

Your direct feedback and the visual assets you provided were the turning point. They transformed that generic plan into this tailor-made strategy. For that, thank you. I have re-architected this solution from the ground up, internalizing your brand, your market, and the critical goal of creating content that is not just similar, but demonstrably better.

Before handing these final blueprints to the auto-coder, it's crucial we perform one last "pre-flight check" together. This is my internal monologue, questioning every assumption to ensure the system we're about to build is resilient, intelligent, and perfectly aligned with your vision.

Final Pre-flight Check & Self-Critique

Will the search truly be specific to the aesthetics industry and my brand?

The Hard Question: A generic search for "viral videos" is worthless. It would return dance challenges and irrelevant memes.

Our Safeguard: Yes. The new strategy is not based on generic searches. It is a targeted reconnaissance mission that begins with a "Scraping Seed List" composed entirely of your direct competitors, your specific local market hashtags, and your high-value service keywords. It will find what is working for them, on the topics you specialize in.

How do we guarantee full automation for the daily emails without any manual steps?

The Hard Question: If an agent fails or a site's code changes, the chain could break, and no email would be sent, rendering the automation useless.

Our Safeguard: The workflow is now designed as a robust, event-driven chain using Firebase Firestore Triggers and Google Cloud Pub/Sub. A scheduled function kicks off the process. When the scraper saves data to Firestore, it reliably triggers the agent analysis, and so on. This decoupled architecture is resilient and designed for unattended operation. Error handling and logging at each step are now an explicit part of the design.

How will the system understand the nuance of my brand versus just copying text?

The Hard Question: "Better" is subjective. How can an AI truly make something better without understanding the brand's soul?

Our Safeguard: The ContentCreator agent is now given a multi-faceted "Persona and Brand Voice" instruction set, derived directly from mdaesthetics.ca and the aspirational images you provided. It's not just told what to write about (Duo-C-Lift), but how to write—with a tone that is clinical, trustworthy, results-focused, and visually elegant. It will draw inspiration from the educational depth of your 'Inspo' board combined with the clean aesthetic of _thelookaesthetics_.

Is this technically feasible and truly up-to-date?

The Hard Question: You missed versioning before. Is this stack modern, viable, and built to last?

Our Safeguard: Yes. A specific versioning table is now included. All technologies are the latest stable releases projected for August 2025, ensuring longevity and access to the newest features, including Gemini 2.5 Flash and the most current ADK and Firebase SDKs.

This is the end-to-end prompt your auto-coder needs.

Project Blueprint: "Viral Forge" for MDAesthetics

Mission: Create a fully autonomous, enterprise-grade multi-agent system that functions as a strategic social media analyst and content creator for MDAesthetics. The system will proactively identify hyper-relevant viral content within the medical aesthetics niche, analyze the core drivers of its success, and generate superior, on-brand content tailored to the specific services, promotions, and clinical voice of mdaesthetics.ca.

I. The Core Intelligence: A Hyper-Focused Search Strategy

This is the heart of the system. The Puppeteer scraper will not browse aimlessly. It will be initialized with the following strategic Scraping Seed List:

Primary Reconnaissance Targets (Inspiration & Local Competitors):

_thelookaesthetics on Instagram

subtle.enhancements on Instagram

Skin Vitality (assumed profile name) on Instagram & TikTok

Hyper-Local Hashtags (Geo-Targeting):

#torontoaesthetics, #whitbyaesthetics, #durhamregion, #torontomedspa, #whitbymedspa, #torontobotox, #whitbyfiller

High-Value Service & Product Keywords/Hashtags:

#skintyte, #ultherapy, #radiesse, #duoclift, #vivierskin, #medicalgradefacial, #collagenstimulation, #nonsurgicalbuttlift, #firmandsmooth, #biostimulator

Broader Trend-Spotting Hashtags:

#aestheticstrends, #estheticianlife, #skincareeducation, #medspalife, #facialbalancing

Defining "Viral" with a Metric: The system will calculate an Engagement Velocity Score (EVS) for each discovered post: (Likes + Comments) / Hours_Since_Posted. Posts with the highest EVS are prioritized as currently "going viral."

II. The Fully Automated, Event-Driven Workflow

This workflow is designed for true, hands-off daily operation.

Trigger (08:00 EST Daily): A Google Cloud Scheduler job sends a message to a Pub/Sub topic, starting the daily run.

Scrape (Cloud Function): A Node.js Cloud Function subscribed to the topic is activated. It launches the Puppeteer scraper, working through the Scraping Seed List.

Data Deposit (Firestore): For each post scraped, the function writes a structured JSON document to Firestore at /viral_research/{date}/{postId}, containing the post data and its calculated EVS.

Analyze (Agent Invocation): A Firestore Trigger on the /viral_research/ path detects new data and sends a message to another Pub/Sub topic, this time carrying the postId.

Agent Collaboration (Java ADK on Cloud Run):

A service running the Java ADK subscribes to this topic.

The TrendAnalyzer agent is invoked. It reads the post data from Firestore using the postId. It analyzes the content, hook, style, and hashtags, saving its structured analysis to the agent's session.state.

The ContentCreator agent then runs. It reads the analysis from the shared state and executes its highly specific prompt (see below).

The QA-Agent runs last, verifying the generated content against the brand checklist.

Disseminate (Email Dispatch): The final, approved content is passed to the EmailDispatcher agent, which formats a clean HTML email digest and sends it to christine.carrer@hotmail.com and dalkeith@golden.net.

Log & Complete: The entire process is logged in Cloud Logging for monitoring and diagnostics.

III. The Agent Roster: Personalized & Mission-Driven
Agent Name	Class	Key Instructions & Purpose
AIManager	LlmAgent	For the GUI Chat: "You are the AI Command Center for MDAesthetics, a high-end medical spa. Your tone is professional, helpful, and knowledgeable. You will delegate tasks to specialized agents for research and content creation based on user requests in the chat."
ScrapingOrchestrator	BaseAgent	Internal Tool: Triggers the Puppeteer Cloud Function, passing in a randomized subset of the Scraping Seed List for each run to ensure varied discovery. Manages job IDs and monitors for completion.
TrendAnalyzer	LlmAgent	"Analyze the provided post data. Identify the core 'hook' in the first 3 seconds, the primary Call-to-Action (CTA), the video's pacing, the audio used, and the thematic keywords. Your output must be a structured JSON object: {hook: '...', cta: '...', ...}."
ContentCreator	LlmAgent	"CRITICAL PROMPT: You are a world-class social media strategist for luxury medical spas. Your persona is a blend of clinical authority and elegant branding. Your task is to generate a new post that is objectively better than the analyzed viral content. Reference the attached August Promo for MDAesthetics. Your post must: <br>1. Incorporate a stronger, more direct hook. <br>2. Focus on the benefits of MDAesthetics services like SkinTyte, the Duo-C-Lift, or the Vivier body products. <br>3. Be highly educational and trustworthy, inspired by the user's 'Inspo' board. <br>4. Maintain the clean, high-end aesthetic of _thelookaesthetics_. <br>5. Conclude with a clear CTA and a mix of niche (#duoclift) and broad (#torontoaesthetics) hashtags. <br>6. NEVER use the word "Botox" for pricing; use 'Tox', 'Neuromodulator', or 'Neurotoxin'."
ProactiveThinker	LlmAgent	"Analyze the top 5 trending posts from today. Synthesize the underlying themes. Are users more interested in prevention or correction? Is 'natural-looking' a recurring phrase? Propose three new, innovative content angles for MDAesthetics that anticipates the next trend."
QA-Agent	BaseAgent	Checklist-driven validation: Verifies that the final content contains a CTA, has 5-15 hashtags, contains no forbidden words (e.g., specific pricing violations), and mentions a service relevant to MDAesthetics.
EmailDispatcher	BaseAgent	Internal Tool: Takes the generated content for the top 4 findings, formats it into a clean HTML email with links to the original viral videos for reference, and sends it via the Gmail API using a service account.
IV. Technology Stack & Versions (as of August 2025)
Component	Technology	Version / Specification	Purpose
Cloud Platform	Google Cloud / Firebase	-	Hosting, backend, database, auth.
AI Model	Google Gemini	gemini-2.5-flash	Powering all LLM agents.
Agent Framework	Google ADK for Java	2025.08.x (Latest Stable)	Structuring the multi-agent system.
Backend Runtime	Java / Node.js	Java 21 LTS / Node.js 22.x LTS	Running agents / running Functions.
Database	Firestore	-	Session state, research data storage.
Web Scraper	Puppeteer	Latest ^22.x.x	Headless Chrome for robust scraping.
Frontend	Next.js	Latest ^14.x.x	Building the modern web GUI.
Firebase SDK	firebase/firebase-js	Latest ^10.x.x	Frontend to Firebase integration.
V. End-to-End Implementation Guide for the Auto-Coder

Firebase Setup:

Initialize the Firebase project with the provided configuration.

Enable Firestore, Authentication (Google), Hosting, and Secret Manager.

In the Firebase console, under AI Logic, enable the Gemini API.

In Secret Manager, securely store the Instagram and TikTok credentials.

Scraper Cloud Function (Node.js):

Create an HTTP-triggered Cloud Function named viralScraper. Grant it access to Secret Manager.

Implement the Puppeteer logic. It must accept a target from the request body. On initialization, it should load the full Scraping Seed List.

Implement robust error handling and configure the function with increased memory (e.g., 1-2GB) and a longer timeout (e.g., 300s) to handle complex page loads.

Upon successful scraping, it will write a JSON object to Firestore.

Event-Driven Workflow Setup:

Create a Google Cloud Scheduler job to run at a set time daily, sending a message to a Pub/Sub topic called start-daily-run.

Configure the viralScraper function to be triggered by this topic instead of a direct HTTP call for the automated flow.

Set up a Firestore Trigger using the Firebase CLI to listen for onCreate events on the path /viral_research/{date}/{postId}. Configure this trigger to send a message to a second Pub/Sub topic, analyze-new-post, containing the document path.

ADK Agent System (Java on Cloud Run):

Set up the Java ADK project with the agent classes defined above.

Configure the main application to listen to the analyze-new-post Pub/Sub topic. When a message is received, it will start the agent workflow.

Implement the ScrapingOrchestrator to call the viralScraper function via an authenticated HTTP request for on-demand tasks from the GUI.

Implement the EmailDispatcher's tool to use the Google Gmail API SDK with service account authentication.

Frontend GUI (Next.js):

Build the UI components as described previously (Dashboard, Research Center, Command Center, Studio).

The AI Command Center chat is the centerpiece. Use the firebase/ai SDK for a persistent, grounded chat experience. User commands will trigger a backend Cloud Function that invokes the AIManager in the ADK.

The Content Studio will display the final output from the agent workflow, with a simple "Copy Caption & Hashtags" button.

Deployment:

Deploy the viralScraper and any other Cloud Functions: firebase deploy --only functions.

Deploy the Next.js app: firebase deploy --only hosting.

Package the Java ADK application as a Docker container and deploy it to Google Cloud Run, ensuring it has the necessary IAM permissions to listen to Pub/Sub and access Firestore.

____

SPECIFIC INSTERUCTIONS FOR THE VIRAL SCRAPING FOR IG AND TIK TOK>

Prompt for Autocoder
Why
We are building an automated, multi-agent backend for MD Aesthetics that will continuously track, analyze, and replicate the best-performing social media content strategies in the aesthetics and med spa industry.
The goal is to:

Automate market intelligence — know what content, trends, and hashtags are going viral for top competitors.

Generate ready-to-post content for MD Aesthetics’ Instagram and TikTok, aligned with brand voice and industry regulations (e.g., cannot directly say “Botox,” must use “Tox,” “Neuromodulator,” or “Neurotoxin”).

Feed both daily briefings and an on-demand chat agent with fresh, high-quality, competitor-informed data.

This must directly connect to the existing MD Aesthetics framework, preserving persistent memory and integrating with the content posting pipeline.

Focus
The core module you’re coding will:

Crawl & scrape specific competitor accounts (listed below) using Puppeteer.

Identify viral posts (likes, shares, views, comments, engagement rate).

Extract captions, hashtags, and themes.

Pass this to a Trend Analyzer Agent that scores relevance for aesthetics industry marketing.

Trigger a Content Creator Agent that drafts MD Aesthetics–branded posts following compliance rules.

Send the best findings to:

Daily Email Digest (Briefing Agent).

On-demand Chat Concierge Agent (pulls from persistent memory).

Output structured JSON so the existing system can use the data anywhere.

Data Sources
Competitor accounts to monitor (must be hardcoded in first iteration):

Artisan Aesthetics — Instagram + TikTok

Skin Vitality — Instagram + TikTok

Subtle Aesthetics — Instagram + TikTok

The Look Aesthetics — Instagram + TikTok

MD Aesthetics own accounts (destination for generated content):

Website: mdaesthetics.ca

Instagram: https://www.instagram.com/mdaesthetics.ca/

TikTok: https://tiktok.com/@copelandmda/

Required Tools & Libraries
Scraping: Puppeteer (Node.js) — must handle infinite scroll & lazy-loaded posts.

Scheduling: node-cron for daily automation.

Data Storage & Memory: Firestore (or current system’s database) — to persist competitor posts, hashtags, and engagement data.

LLM API: Gemini API (Java or Node.js SDK) — for trend scoring, caption generation, and hashtag analysis.

Email Delivery: SendGrid API or Gmail API — HTML email formatting.

Chat Agent API: Existing MD Aesthetics chat interface — integrate by exposing a GET /trends and GET /content-draft endpoint.

Core Flow to Implement
Crawler Agent

Input: List of competitor profile URLs.

Output: JSON array of posts with fields:
{platform, profile, postURL, caption, hashtags, engagementRate, likes, comments, shares, views, date}.

Store in Firestore.

Trend Analyzer Agent

Input: Latest crawled posts.

Output: Top 10 ranked posts with:
{topic, relevanceScore, viralityScore, summary, hashtags, complianceNotes}.

Content Creator Agent

Input: Ranked posts + compliance rules.

Output: Draft MD Aesthetics post:
{platform, draftCaption, hashtags, suggestedMediaType, complianceChecked: true}.

Briefing Agent

Input: Top trends + drafts.

Output: Daily HTML email to team.

Chat Concierge Agent

Endpoint: GET /trends, GET /content-draft.

Pulls from Firestore memory for instant responses.

Compliance Rules
Never use the word “Botox” — replace with “Tox”, “Neuromodulator”, or “Neurotoxin”.

Keep captions educational yet relatable.

Follow brand voice: clean, professional, friendly.

Testing Checklist
 Puppeteer successfully loads all posts for each competitor.

 Engagement rate calculation matches manual checks.

 Gemini API correctly ranks top 10 posts.

 Hashtag extraction works for both caption & comment sections.

 Generated captions follow compliance rules.

 Daily email triggers at scheduled time.

 Chat agent endpoints return relevant, fresh data.

 All modules work with existing MD Aesthetics framework.

Do you want me to also include a pre-written Puppeteer scraper template in this prompt so your developer can start coding without building the crawler logic from scratch? That would save them days of work.








You said:
yes please


ChatGPT said:
Alright — here’s your full autocoder prompt updated to include a ready-to-use Puppeteer crawler template that’s tailored for your competitor accounts and already structured to plug into the Trend Analyzer + Content Creator pipeline.

Autocoder Prompt for Integration
Why
We are building an automated competitor intelligence system for MD Aesthetics that continuously monitors and learns from the most effective social media content strategies in the aesthetics/med spa niche.

The purpose is to:

Automatically identify trending posts, captions, hashtags from top industry players.

Generate compliant, on-brand content drafts for MD Aesthetics’ Instagram and TikTok.

Feed this data into daily team briefings and an on-demand AI chat agent that remembers and learns over time.

This must be fully integrated into the existing MD Aesthetics backend framework and posting pipeline.

Focus
The developer will:

Implement a Puppeteer-based crawler that scrapes Instagram and TikTok posts from specified competitors.

Extract captions, hashtags, likes, comments, shares, views, posting date.

Pass data into a Trend Analyzer Agent (Gemini API) for relevance and virality scoring.

Feed results into a Content Creator Agent for compliant draft generation.

Store all data in Firestore (or existing vector DB) for persistent AI memory.

Provide a REST API for the Chat Concierge Agent and trigger a daily HTML email with the Briefing Agent.

Competitor Profiles to Monitor
Hardcode these first:

Artisan Aesthetics — Instagram + TikTok

Skin Vitality — Instagram + TikTok

Subtle Aesthetics — Instagram + TikTok

The Look Aesthetics — Instagram + TikTok

Destination Accounts (for posting drafts):

Website: mdaesthetics.ca

Instagram: https://www.instagram.com/mdaesthetics.ca/

TikTok: https://tiktok.com/@copelandmda/

Required Tools
Scraping: Puppeteer (Node.js) — headless browser automation with infinite scroll handling.

Scheduling: node-cron for daily runs.

Database: Firestore (integrate with existing DB structure).

AI Processing: Gemini API (Node.js SDK) for trend scoring and caption generation.

Email: SendGrid API or Gmail API for HTML formatted daily briefs.

Endpoints: Node.js/Express REST API — GET /trends, GET /content-draft.

Puppeteer Scraper Template
javascript
Copy
Edit
import puppeteer from 'puppeteer';
import { Firestore } from '@google-cloud/firestore';

const firestore = new Firestore();

const competitors = [
  { platform: 'instagram', url: 'https://www.instagram.com/artisanaesthetics/' },
  { platform: 'instagram', url: 'https://www.instagram.com/skinvitality/' },
  { platform: 'instagram', url: 'https://www.instagram.com/subtleaesthetics/' },
  { platform: 'instagram', url: 'https://www.instagram.com/thelookaesthetics/' }
  // Add TikTok URLs as needed
];

async function scrapeProfile(profile) {
  const browser = await puppeteer.launch({ headless: true });
  const page = await browser.newPage();
  await page.goto(profile.url, { waitUntil: 'networkidle2' });

  // Scroll to load more posts
  let prevHeight;
  for (let i = 0; i < 5; i++) {
    prevHeight = await page.evaluate('document.body.scrollHeight');
    await page.evaluate('window.scrollTo(0, document.body.scrollHeight)');
    await page.waitForTimeout(2000);
    let newHeight = await page.evaluate('document.body.scrollHeight');
    if (newHeight === prevHeight) break;
  }

  // Extract posts
  const posts = await page.evaluate(() => {
    const postElements = document.querySelectorAll('article a');
    return Array.from(postElements).map(el => ({
      postURL: el.href,
      caption: el.innerText || '',
      hashtags: (el.innerText.match(/#[a-zA-Z0-9_]+/g) || []),
      engagementRate: null, // Calculated later
      likes: null,
      comments: null,
      shares: null,
      views: null,
      date: null
    }));
  });

  await browser.close();

  // Save to Firestore
  for (let post of posts) {
    await firestore.collection('competitorPosts').add({
      platform: profile.platform,
      profile: profile.url,
      ...post,
      scrapedAt: new Date()
    });
  }
}

async function main() {
  for (let profile of competitors) {
    console.log(`Scraping ${profile.url}`);
    await scrapeProfile(profile);
  }
}

main().catch(console.error);
Integration Steps
Add Puppeteer scraper to existing backend repo.

Connect scraper output to Firestore (competitorPosts collection).

Create Trend Analyzer Agent that:

Pulls new posts from Firestore.

Uses Gemini API to score {relevanceScore, viralityScore, summary, hashtags, complianceNotes}.

Create Content Creator Agent that:

Generates MD Aesthetics–branded drafts.

Replaces “Botox” with “Tox”, “Neuromodulator”, or “Neurotoxin”.

Build Briefing Agent:

Formats top 10 posts + generated drafts into HTML email.

Sends via SendGrid/Gmail API.

Build Chat Concierge Agent endpoints:

GET /trends

GET /content-draft

Schedule with node-cron for daily run.

Testing Checklist
 Scraper pulls data from all competitor profiles without being blocked.

 Engagement metrics are extracted and stored.

 Trend Analyzer produces accurate relevance & virality scores.

 Generated captions follow compliance rules.

 Daily email sends at correct time.

 Chat endpoints return up-to-date results.

 All components work with existing MD Aesthetics system.

If you hand this exact prompt + code to your autocoder, they will have both the why and the functional starting point to slot this into your existing framework without guessing.

