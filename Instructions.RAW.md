This is a enterprise solution.  Without taking any functionality away please review, research, and send back a cleaned up version of this while tying any lose ends, correcting any flow, adding any value, making the system workable, and make sense, without incurring any extra cost.  I want to provide what you send back to my auto coder to then create the solution based on my requirements.  

__

Your job is to create a multi agent system in this firebase environmet I have created with billing and hosting enabled using https://github.com/ivanmolanski/adk-javawhich has been cloned in the viral directory. 
) I am in a VENV Environment dalkeith@DESKTOP-KE0ADNS MINGW64 /c/bach/viral (main)
$ python -m venv venv
source venv/Scripts/activate using Git Bash on Windows 11 using Windows 11

First you need to RUN THIS 


// Import the functions you need from the SDKs you need
import { initializeApp } from "firebase/app";
// TODO: Add SDKs for Firebase products that you want to use
// https://firebase.google.com/docs/web/setup#available-libraries

// Your web app's Firebase configuration
const firebaseConfig = {
  apiKey: "AIzaSyAPDjj37OF9fdO2nsq2Qezwea-xGfPJRlA",
  authDomain: "contentforge-ai-ygy25.firebaseapp.com",
  projectId: "contentforge-ai-ygy25",
  storageBucket: "contentforge-ai-ygy25.firebasestorage.app",
  messagingSenderId: "51060608349",
  appId: "1:51060608349:web:12c14f56648ced0ae96cb4"
};

// Initialize Firebase
const app = initializeApp(firebaseConfig);

Then this npm install -g firebase-tools

You can deploy now or later. To deploy now, open a terminal window, then navigate to or create a root directory for your web app.

Sign in to Google
firebase login
Initiate your project
Run this command from your app's root directory:

firebase init

To deploy once the solution is set up

---

This solution will require a modern GUI with full Genkit Beta using Gemini Flash model gemini-2.5-flash (not preview, not 2.0)

With GROUNDING 

Skip to main content
Firebase
Build

Run

Solutions
Docs

More
Search
/


English
Blog
Studio
Go to console

Documentation
Firebase AI Logic
Overview
Fundamentals

AI

Build

Run

Reference
Samples
Filter

Firebase
Documentation
Firebase AI Logic
AI
Was this helpful?

Send feedbackGrounding with Google Search

bookmark_border


Grounding with Google Search connects a Gemini model to real-time, publicly-available web content. This allows the model to provide more accurate, up-to-date answers and cite verifiable sources beyond its knowledge cutoff.

Grounding with Google Search has the following benefits:

Increase factual accuracy: Reduce model hallucinations by basing responses on real-world information.
Access real-time information: Answer questions about recent events and topics.
Provide citations: Build user trust or allow users to browse relevant sites by showing the sources for the model's claims.
Complete more complex tasks: Retrieve artifacts and relevant images, videos, or other media to assist in reasoning tasks.
Improve region or language-specific responses: Find region-specific information, or assist in translating content accurately.
Note for web publishers: Grounding with Google Search does not use web pages for grounding that have disallowed Google-Extended. Web publishers can manage inclusion in Google-Extended with a robots.txt file.
Supported models
gemini-2.5-pro
gemini-2.5-flash
gemini-2.5-flash-lite
gemini-2.5-flash (latest stable version)
gemini-2.5-flash (recommended for production)
Supported languages
See supported languages for Gemini models.

Ground the model with Google Search
Click your Gemini API provider to view provider-specific content and code on this page.

Gemini Developer API Vertex AI Gemini API
Important: If a response contains "Google Search suggestions" (the searchEntryPoint field within the groundingMetadata object), then that response is a "grounded result" so you're required to comply with the "Grounding with Google Search" usage requirements, which includes how you display the result. Learn how to use and display a grounded result later on this page.
When you create the GenerativeModel instance, provide GoogleSearch as a tool that the model can use to generate its response.

Swift
Kotlin
Java
Web
Dart
Unity


import { initializeApp } from "firebase/app";
import { getAI, getGenerativeModel, GoogleAIBackend } from "firebase/ai";

// TODO(developer) Replace the following with your app's Firebase configuration
// See: https://firebase.google.com/docs/web/learn-more#config-object
const firebaseConfig = {
  // ...
};

// Initialize FirebaseApp
const firebaseApp = initializeApp(firebaseConfig);

// Initialize the Gemini Developer API backend service
const ai = getAI(firebaseApp, { backend: new GoogleAIBackend() });

// Create a `GenerativeModel` instance with a model that supports your use case
const model = getGenerativeModel(
  ai,
  {
    model: "GEMINI_MODEL_NAME",
    // Provide Google Search as a tool that the model can use to generate its response
    tools: [{ googleSearch: {} }]
  }
);

const result = await model.generateContent("Who won the euro 2024?");

console.log(result.response.text());

// Make sure to comply with the "Grounding with Google Search" usage requirements,
// which includes how you use and display the grounded result

Learn how to choose a model appropriate for your use case and app.

For ideal results, use a temperature of 1.0 (which is the default for all 2.5 models). Learn how to set temperature in the model's configuration.

How grounding with Google Search works
When you use the GoogleSearch tool, the model handles the entire workflow of searching, processing, and citing information automatically.

Here's the workflow of the model:

Receive prompt: Your app sends a prompt to the Gemini model with the GoogleSearch tool enabled.
Analyze prompt: The model analyzes the prompt and determines if Google Search can improve its response.
Send queries to Google Search: If needed, the model automatically generates one or multiple search queries and executes them.
Process the Search results: The model processes the Google Search results and formulates a response to the original prompt.
Return a "grounded result": The model returns a final, user-friendly response that is grounded in the Google Search results. This response includes the model's text answer and groundingMetadata with the search queries, web results, and citations.
Note that providing Google Search as a tool to the model doesn't require the model to always use the Google Search tool to generate its response. In these cases, the response won't contain a groundingMetadata object and thus it's not a "grounded result".

Diagram showing how grounding with Google Search involves the model interacting with Google Search

Understand the grounded result
If the model grounds its response in Google Search results, then the response includes a groundingMetadata object that contains structured data that's essential for verifying claims and building a rich citation experience in your application.

Important: If a response contains "Google Search suggestions" (the searchEntryPoint field within the groundingMetadata object), then that response is a "grounded result" so you're required to comply with the "Grounding with Google Search" usage requirements, which includes how you display the result. Learn how to use and display a grounded result later on this page.
The groundingMetadata object in a "grounded result" contains the following information:

webSearchQueries: An array of the search queries sent to Google Search. This information is useful for debugging and understanding the model's reasoning process.

searchEntryPoint: Contains the HTML and CSS to render the required "Google Search suggestions". You're required to comply with the "Grounding with Google Search" usage requirements for your chosen API provider: Gemini Developer API or Vertex AI Gemini API (see Service Terms section within the Service Specific Terms). Learn how to use and display a grounded result later on this page.

groundingChunks: An array of objects containing the web sources (uri and title).

groundingSupports: An array of chunks to connect model response text to the sources in groundingChunks. Each chunk links a text segment (defined by startIndex and endIndex) to one or more groundingChunkIndices. This field helps you build inline citations. Learn how to use and display a grounded result later on this page.

Here's an example response that includes a groundingMetadata object:


{
  "candidates": [
    {
      "content": {
        "parts": [
          {
            "text": "Spain won Euro 2024, defeating England 2-1 in the final. This victory marks Spain's record fourth European Championship title."
          }
        ],
        "role": "model"
      },
      "groundingMetadata": {
        "webSearchQueries": [
          "UEFA Euro 2024 winner",
          "who won euro 2024"
        ],
        "searchEntryPoint": {
          "renderedContent": "<!-- HTML and CSS for the search widget -->"
        },
        "groundingChunks": [
          {"web": {"uri": "https://vertexaisearch.cloud.google.com.....", "title": "aljazeera.com"}},
          {"web": {"uri": "https://vertexaisearch.cloud.google.com.....", "title": "uefa.com"}}
        ],
        "groundingSupports": [
          {
            "segment": {"startIndex": 0, "endIndex": 85, "text": "Spain won Euro 2024, defeatin..."},
            "groundingChunkIndices": [0]
          },
          {
            "segment": {"startIndex": 86, "endIndex": 210, "text": "This victory marks Spain's..."},
            "groundingChunkIndices": [0, 1]
          }
        ]
      }
    }
  ]
}
Use and display a grounded result
If the model uses the Google Search tool to generate a response, it will provide a groundingMetadata object in the response.

It's required to display Google Search suggestions and recommended to display citations.

Beyond complying with the requirements of using the Google Search tool, displaying this information helps you and your end users to validate responses and adds avenues for further learning.

Important: This section describes basic guidance and a general pattern for how to use and display a grounded result. Make sure that you review and comply with the usage and display requirements for your chosen Gemini API provider: Gemini Developer API or Vertex AI Gemini API (see Service Terms section within the Service Specific Terms).
(Required) Display Google Search suggestions
If a response contains "Google Search suggestions", then you're required to comply with the "Grounding with Google Search" usage requirements, which includes how you display Google Search suggestions.

The groundingMetadata object contains "Google Search suggestions", specifically the searchEntryPoint field, which has a renderedContent field that provides compliant HTML and CSS styling, which you need to implement to display Search suggestions in your app.

Review the detailed information about the display and behavior requirements for Google Search suggestions in the Google Cloud documentation. Note that even though this detailed guidance is in the Vertex AI Gemini API documentation, the guidance is applicable to the Gemini Developer API provider, as well.

See example code samples later in this section.

Note: The HTML and CSS provided in the response automatically adapts to the device settings, displaying in either light or dark mode based on the preference indicated by @media(prefers-color-scheme).
(Recommended) Display citations
The groundingMetadata object contains structured citation data, specifically the groundingSupports and groundingChunks fields. Use this information to link the model's statements directly to their sources within your UI (inline and in aggregate).

See example code samples later in this section.

Example code samples
These code samples provide generalized patterns for using and displaying the grounded result. However, it's your responsibility to make sure that your specific implementation aligns with the compliance requirements.

Swift
Kotlin
Java
Web
Dart
Unity

// ...

// Get the model's text response
const text = result.response.text();

// Get the grounding metadata
const groundingMetadata = result.response.candidates?.[0]?.groundingMetadata;

// REQUIRED - display Google Search suggestions
// (renderedContent contains HTML and CSS for the search widget)
const renderedContent = groundingMetadata?.searchEntryPoint?.renderedContent;
if (renderedContent) {
  // TODO(developer): render this HTML and CSS in the UI
}

// RECOMMENDED - display citations
const groundingChunks = groundingMetadata?.groundingChunks;
if (groundingChunks) {
  for (const chunk of groundingChunks) {
    const title = chunk.web?.title;  // for example, "uefa.com"
    const uri = chunk.web?.uri;  // for example, "https://vertexaisearch.cloud.google.com..."
    // TODO(developer): show citation in the UI
  }
}
Grounded results and AI monitoring in the Firebase console
If you've enabled AI monitoring in the Firebase console, responses are stored in Cloud Logging. By default, this data has a 30-day retention period.

It's your responsibility to ensure that this retention period, or any custom period you set, fully aligns with your specific use case and any additional compliance requirements for your chosen Gemini API provider: Gemini Developer API or Vertex AI Gemini API (see Service Terms section within the Service Specific Terms). You may need to adjust the retention period in Cloud Logging to meet these requirements.

Pricing and limits
Make sure to review pricing, model availability, and limits for grounding with Google Search in your chosen Gemini API provider documentation: Gemini Developer API | Vertex AI Gemini API.

Was this helpful?

Send feedback
Except as otherwise noted, the content of this page is licensed under the Creative Commons Attribution 4.0 License, and code samples are licensed under the Apache 2.0 License. For details, see the Google Developers Site Policies. Java is a registered trademark of Oracle and/or its affiliates.

Last updated 2025-08-06 UTC.

Learn
Developer guides
SDK & API reference
Samples
Libraries
GitHub
Stay connected
Check out the blog
Find us on Reddit
Follow on X
Subscribe on YouTube
Attend an event
Support
Contact support
Stack Overflow
Slack community
Google group
Release notes
Brand guidelines
FAQs
Google Developers
Android
Chrome
Firebase
Google Cloud Platform
All products
Terms
Privacy

English
Ask about this page
bug_report
fullscreen
close
Chat
BETA
restart_alt
Not sure what to ask?
Click below for suggestions!
Show quick start prompts
Enter a prompt here
send
Responses may display inaccurate or offensive information that doesn't represent Google's views. additional details

The new page has loaded.

And PERSISTANT CHAT 

Skip to main content
Firebase
Build

Run

Solutions
Docs

More
Search
/


English
Blog
Studio
Go to console

Documentation
Firebase AI Logic
Overview
Fundamentals

AI

Build

Run

Reference
Samples
Filter

Firebase
Documentation
Firebase AI Logic
AI
Was this helpful?

Send feedbackGrounding with Google Search

bookmark_border


Grounding with Google Search connects a Gemini model to real-time, publicly-available web content. This allows the model to provide more accurate, up-to-date answers and cite verifiable sources beyond its knowledge cutoff.

Grounding with Google Search has the following benefits:

Increase factual accuracy: Reduce model hallucinations by basing responses on real-world information.
Access real-time information: Answer questions about recent events and topics.
Provide citations: Build user trust or allow users to browse relevant sites by showing the sources for the model's claims.
Complete more complex tasks: Retrieve artifacts and relevant images, videos, or other media to assist in reasoning tasks.
Improve region or language-specific responses: Find region-specific information, or assist in translating content accurately.
Note for web publishers: Grounding with Google Search does not use web pages for grounding that have disallowed Google-Extended. Web publishers can manage inclusion in Google-Extended with a robots.txt file.
Supported models
gemini-2.5-pro
gemini-2.5-flash
gemini-2.5-flash-lite
gemini-2.5-flash (latest stable version)
gemini-2.5-flash (recommended for production)
Supported languages
See supported languages for Gemini models.

Ground the model with Google Search
Click your Gemini API provider to view provider-specific content and code on this page.

Gemini Developer API Vertex AI Gemini API
Important: If a response contains "Google Search suggestions" (the searchEntryPoint field within the groundingMetadata object), then that response is a "grounded result" so you're required to comply with the "Grounding with Google Search" usage requirements, which includes how you display the result. Learn how to use and display a grounded result later on this page.
When you create the GenerativeModel instance, provide GoogleSearch as a tool that the model can use to generate its response.

Swift
Kotlin
Java
Web
Dart
Unity


import { initializeApp } from "firebase/app";
import { getAI, getGenerativeModel, GoogleAIBackend } from "firebase/ai";

// TODO(developer) Replace the following with your app's Firebase configuration
// See: https://firebase.google.com/docs/web/learn-more#config-object
const firebaseConfig = {
  // ...
};

// Initialize FirebaseApp
const firebaseApp = initializeApp(firebaseConfig);

// Initialize the Gemini Developer API backend service
const ai = getAI(firebaseApp, { backend: new GoogleAIBackend() });

// Create a `GenerativeModel` instance with a model that supports your use case
const model = getGenerativeModel(
  ai,
  {
    model: "GEMINI_MODEL_NAME",
    // Provide Google Search as a tool that the model can use to generate its response
    tools: [{ googleSearch: {} }]
  }
);

const result = await model.generateContent("Who won the euro 2024?");

console.log(result.response.text());

// Make sure to comply with the "Grounding with Google Search" usage requirements,
// which includes how you use and display the grounded result

Learn how to choose a model appropriate for your use case and app.

For ideal results, use a temperature of 1.0 (which is the default for all 2.5 models). Learn how to set temperature in the model's configuration.

How grounding with Google Search works
When you use the GoogleSearch tool, the model handles the entire workflow of searching, processing, and citing information automatically.

Here's the workflow of the model:

Receive prompt: Your app sends a prompt to the Gemini model with the GoogleSearch tool enabled.
Analyze prompt: The model analyzes the prompt and determines if Google Search can improve its response.
Send queries to Google Search: If needed, the model automatically generates one or multiple search queries and executes them.
Process the Search results: The model processes the Google Search results and formulates a response to the original prompt.
Return a "grounded result": The model returns a final, user-friendly response that is grounded in the Google Search results. This response includes the model's text answer and groundingMetadata with the search queries, web results, and citations.
Note that providing Google Search as a tool to the model doesn't require the model to always use the Google Search tool to generate its response. In these cases, the response won't contain a groundingMetadata object and thus it's not a "grounded result".

Diagram showing how grounding with Google Search involves the model interacting with Google Search

Understand the grounded result
If the model grounds its response in Google Search results, then the response includes a groundingMetadata object that contains structured data that's essential for verifying claims and building a rich citation experience in your application.

Important: If a response contains "Google Search suggestions" (the searchEntryPoint field within the groundingMetadata object), then that response is a "grounded result" so you're required to comply with the "Grounding with Google Search" usage requirements, which includes how you display the result. Learn how to use and display a grounded result later on this page.
The groundingMetadata object in a "grounded result" contains the following information:

webSearchQueries: An array of the search queries sent to Google Search. This information is useful for debugging and understanding the model's reasoning process.

searchEntryPoint: Contains the HTML and CSS to render the required "Google Search suggestions". You're required to comply with the "Grounding with Google Search" usage requirements for your chosen API provider: Gemini Developer API or Vertex AI Gemini API (see Service Terms section within the Service Specific Terms). Learn how to use and display a grounded result later on this page.

groundingChunks: An array of objects containing the web sources (uri and title).

groundingSupports: An array of chunks to connect model response text to the sources in groundingChunks. Each chunk links a text segment (defined by startIndex and endIndex) to one or more groundingChunkIndices. This field helps you build inline citations. Learn how to use and display a grounded result later on this page.

Here's an example response that includes a groundingMetadata object:


{
  "candidates": [
    {
      "content": {
        "parts": [
          {
            "text": "Spain won Euro 2024, defeating England 2-1 in the final. This victory marks Spain's record fourth European Championship title."
          }
        ],
        "role": "model"
      },
      "groundingMetadata": {
        "webSearchQueries": [
          "UEFA Euro 2024 winner",
          "who won euro 2024"
        ],
        "searchEntryPoint": {
          "renderedContent": "<!-- HTML and CSS for the search widget -->"
        },
        "groundingChunks": [
          {"web": {"uri": "https://vertexaisearch.cloud.google.com.....", "title": "aljazeera.com"}},
          {"web": {"uri": "https://vertexaisearch.cloud.google.com.....", "title": "uefa.com"}}
        ],
        "groundingSupports": [
          {
            "segment": {"startIndex": 0, "endIndex": 85, "text": "Spain won Euro 2024, defeatin..."},
            "groundingChunkIndices": [0]
          },
          {
            "segment": {"startIndex": 86, "endIndex": 210, "text": "This victory marks Spain's..."},
            "groundingChunkIndices": [0, 1]
          }
        ]
      }
    }
  ]
}
Use and display a grounded result
If the model uses the Google Search tool to generate a response, it will provide a groundingMetadata object in the response.

It's required to display Google Search suggestions and recommended to display citations.

Beyond complying with the requirements of using the Google Search tool, displaying this information helps you and your end users to validate responses and adds avenues for further learning.

Important: This section describes basic guidance and a general pattern for how to use and display a grounded result. Make sure that you review and comply with the usage and display requirements for your chosen Gemini API provider: Gemini Developer API or Vertex AI Gemini API (see Service Terms section within the Service Specific Terms).
(Required) Display Google Search suggestions
If a response contains "Google Search suggestions", then you're required to comply with the "Grounding with Google Search" usage requirements, which includes how you display Google Search suggestions.

The groundingMetadata object contains "Google Search suggestions", specifically the searchEntryPoint field, which has a renderedContent field that provides compliant HTML and CSS styling, which you need to implement to display Search suggestions in your app.

Review the detailed information about the display and behavior requirements for Google Search suggestions in the Google Cloud documentation. Note that even though this detailed guidance is in the Vertex AI Gemini API documentation, the guidance is applicable to the Gemini Developer API provider, as well.

See example code samples later in this section.

Note: The HTML and CSS provided in the response automatically adapts to the device settings, displaying in either light or dark mode based on the preference indicated by @media(prefers-color-scheme).
(Recommended) Display citations
The groundingMetadata object contains structured citation data, specifically the groundingSupports and groundingChunks fields. Use this information to link the model's statements directly to their sources within your UI (inline and in aggregate).

See example code samples later in this section.

Example code samples
These code samples provide generalized patterns for using and displaying the grounded result. However, it's your responsibility to make sure that your specific implementation aligns with the compliance requirements.

Swift
Kotlin
Java
Web
Dart
Unity

// ...

// Get the model's text response
const text = result.response.text();

// Get the grounding metadata
const groundingMetadata = result.response.candidates?.[0]?.groundingMetadata;

// REQUIRED - display Google Search suggestions
// (renderedContent contains HTML and CSS for the search widget)
const renderedContent = groundingMetadata?.searchEntryPoint?.renderedContent;
if (renderedContent) {
  // TODO(developer): render this HTML and CSS in the UI
}

// RECOMMENDED - display citations
const groundingChunks = groundingMetadata?.groundingChunks;
if (groundingChunks) {
  for (const chunk of groundingChunks) {
    const title = chunk.web?.title;  // for example, "uefa.com"
    const uri = chunk.web?.uri;  // for example, "https://vertexaisearch.cloud.google.com..."
    // TODO(developer): show citation in the UI
  }
}
Grounded results and AI monitoring in the Firebase console
If you've enabled AI monitoring in the Firebase console, responses are stored in Cloud Logging. By default, this data has a 30-day retention period.

It's your responsibility to ensure that this retention period, or any custom period you set, fully aligns with your specific use case and any additional compliance requirements for your chosen Gemini API provider: Gemini Developer API or Vertex AI Gemini API (see Service Terms section within the Service Specific Terms). You may need to adjust the retention period in Cloud Logging to meet these requirements.

Pricing and limits
Make sure to review pricing, model availability, and limits for grounding with Google Search in your chosen Gemini API provider documentation: Gemini Developer API | Vertex AI Gemini API.

Was this helpful?

Send feedback
Except as otherwise noted, the content of this page is licensed under the Creative Commons Attribution 4.0 License, and code samples are licensed under the Apache 2.0 License. For details, see the Google Developers Site Policies. Java is a registered trademark of Oracle and/or its affiliates.

Last updated 2025-08-06 UTC.

Learn
Developer guides
SDK & API reference
Samples
Libraries
GitHub
Stay connected
Check out the blog
Find us on Reddit
Follow on X
Subscribe on YouTube
Attend an event
Support
Contact support
Stack Overflow
Slack community
Google group
Release notes
Brand guidelines
FAQs
Google Developers
Android
Chrome
Firebase
Google Cloud Platform
All products
Terms
Privacy

English
Ask about this page
bug_report
fullscreen
close
Chat
BETA
restart_alt
Not sure what to ask?
Click below for suggestions!
Show quick start prompts
Enter a prompt here
send
Responses may display inaccurate or offensive information that doesn't represent Google's views. additional details

The new page has loaded.

__

AND FIREBASE AI LOGIC 

Skip to main content
Firebase
Build

Run

Solutions
Docs

More
Search
/


English
Blog
Studio
Go to console

Documentation
Firebase AI Logic
Overview
Fundamentals

AI

Build

Run

Reference
Samples
Filter

Firebase
Documentation
Firebase AI Logic
AI
Was this helpful?

Send feedbackGet started with the Gemini API using the Firebase AI Logic SDKs

bookmark_border
This guide shows you how to get started making calls to the Gemini API directly from your app using the Firebase AI Logic client SDKs for your chosen platform.

You can also use this guide to get started with accessing Imagen models using the Firebase AI Logic SDKs.


Firebase AI Logic and its client SDKs were formerly called "Vertex AI in Firebase". In May 2025, we renamed and repackaged our services into Firebase AI Logic to better reflect our expanded services and features — for example, we now support the Gemini Developer API!
Prerequisites
Swift
Kotlin
Java
Web
Dart
Unity
This guide assumes that you're familiar with using JavaScript to develop web apps. This guide is framework-independent.

Make sure that your development environment and web app meet these requirements:

(Optional) Node.js
Modern web browser
(Optional) Check out the sample app.

Download the sample app

You can try out the SDK quickly, see a complete implementation of various use cases, or use the sample app if don't have your own web app. To use the sample app, you'll need to connect it to a Firebase project.

Step 1: Set up a Firebase project and connect your app
Sign into the Firebase console, and then select your Firebase project.

Don't already have a Firebase project?

In the Firebase console, go to the Firebase AI Logic page.

Click Get started to launch a guided workflow that helps you set up the required APIs and resources for your project.

Select the "Gemini API" provider that you'd like to use with the Firebase AI Logic SDKs. Gemini Developer API is recommended for first-time users. You can always add billing or set up Vertex AI Gemini API later, if you'd like.

Gemini Developer API — billing optional (available on the no-cost Spark pricing plan, and you can upgrade later if desired)
The console will enable the required APIs and create a Gemini API key in your project.
Do not add this Gemini API key into your app's codebase. Learn more.

Vertex AI Gemini API — billing required (requires the pay-as-you-go Blaze pricing plan)
The console will help you set up billing and enable the required APIs in your project.

If prompted in the console's workflow, follow the on-screen instructions to register your app and connect it to Firebase.

Continue to the next step in this guide to add the SDK to your app.

Note: In the Firebase console, you're strongly encouraged to set up Firebase App Check. If you're just trying out the Gemini API, you don't need to set up App Check right away; however, we recommend setting it up as soon as you start seriously developing your app.
Step 2: Add the SDK
With your Firebase project set up and your app connected to Firebase (see previous step), you can now add the Firebase AI Logic SDK to your app.

Swift
Kotlin
Java
Web
Dart
Unity
The Firebase AI Logic library provides access to the APIs for interacting with Gemini and Imagen models. The library is included as part of the Firebase JavaScript SDK for Web.

Install the Firebase JS SDK for Web using npm:


npm install firebase
Initialize Firebase in your app:


import { initializeApp } from "firebase/app";

// TODO(developer) Replace the following with your app's Firebase configuration
// See: https://firebase.google.com/docs/web/learn-more#config-object
const firebaseConfig = {
  // ...
};

// Initialize FirebaseApp
const firebaseApp = initializeApp(firebaseConfig);
Step 3: Initialize the service and create a model instance

Click your Gemini API provider to view provider-specific content and code on this page.

Gemini Developer API Vertex AI Gemini API
When using the Firebase AI Logic client SDKs with the Gemini Developer API, you do NOT add your Gemini API key into your app's codebase. Learn more.
Before sending a prompt to a Gemini model, initialize the service for your chosen API provider and create a GenerativeModel instance.

Swift
Kotlin
Java
Web
Dart
Unity


import { initializeApp } from "firebase/app";
import { getAI, getGenerativeModel, GoogleAIBackend } from "firebase/ai";

// TODO(developer) Replace the following with your app's Firebase configuration
// See: https://firebase.google.com/docs/web/learn-more#config-object
const firebaseConfig = {
  // ...
};

// Initialize FirebaseApp
const firebaseApp = initializeApp(firebaseConfig);

// Initialize the Gemini Developer API backend service
const ai = getAI(firebaseApp, { backend: new GoogleAIBackend() });

// Create a `GenerativeModel` instance with a model that supports your use case
const model = getGenerativeModel(ai, { model: "gemini-2.5-flash" });

Note that depending on the capability you're using, you might not always create a GenerativeModel instance.

To access an Imagen model, create an ImagenModel instance.
Also, after you finish this getting started guide, learn how to choose a model for your use case and app.

Step 4: Send a prompt request to a model
You're now set up to send a prompt request to a Gemini model.

You can use generateContent() to generate text from a prompt that contains text:

Swift
Kotlin
Java
Web
Dart
Unity


import { initializeApp } from "firebase/app";
import { getAI, getGenerativeModel, GoogleAIBackend } from "firebase/ai";

// TODO(developer) Replace the following with your app's Firebase configuration
// See: https://firebase.google.com/docs/web/learn-more#config-object
const firebaseConfig = {
  // ...
};

// Initialize FirebaseApp
const firebaseApp = initializeApp(firebaseConfig);

// Initialize the Gemini Developer API backend service
const ai = getAI(firebaseApp, { backend: new GoogleAIBackend() });

// Create a `GenerativeModel` instance with a model that supports your use case
const model = getGenerativeModel(ai, { model: "gemini-2.5-flash" });

// Wrap in an async function so you can use await
async function run() {
  // Provide a prompt that contains text
  const prompt = "Write a story about a magic backpack."

  // To generate text output, call generateContent with the text input
  const result = await model.generateContent(prompt);

  const response = result.response;
  const text = response.text();
  console.log(text);
}

run();
The Gemini API can also stream responses for faster interactions, as well as handle multimodal prompts that include content like images, video, audio, and PDFs. Later on this page, find links to guides for various capabilities of the Gemini API.
If you get an error, make sure that your Firebase project is set up correctly with the Blaze pricing plan and required APIs enabled.
What else can you do?

Learn more about the supported models
Learn about the models available for various use cases and their quotas and pricing.

Try out other capabilities
Learn more about generating text from text-only prompts, including how to stream the response.
Generate text by prompting with various file types, like images, PDFs, video, and audio.
Build multi-turn conversations (chat).
Generate structured output (like JSON) from both text and multimodal prompts.
Generate images from text prompts (Gemini or Imagen).
Stream input and output (including audio) using the Gemini Live API.
Use tools (like function calling and grounding with Google Search) to connect a Gemini model to other parts of your app and external systems and information.

Learn how to control content generation
Understand prompt design, including best practices, strategies, and example prompts.
Configure model parameters like temperature and maximum output tokens (for Gemini) or aspect ratio and person generation (for Imagen).
Use safety settings to adjust the likelihood of getting responses that may be considered harmful.
You can also experiment with prompts and model configurations and even get a generated code snippet using Google AI Studio.


Give feedback about your experience with Firebase AI Logic



Was this helpful?

Send feedback
Except as otherwise noted, the content of this page is licensed under the Creative Commons Attribution 4.0 License, and code samples are licensed under the Apache 2.0 License. For details, see the Google Developers Site Policies. Java is a registered trademark of Oracle and/or its affiliates.

Last updated 2025-08-08 UTC.

Learn
Developer guides
SDK & API reference
Samples
Libraries
GitHub
Stay connected
Check out the blog
Find us on Reddit
Follow on X
Subscribe on YouTube
Attend an event
Support
Contact support
Stack Overflow
Slack community
Google group
Release notes
Brand guidelines
FAQs
Google Developers
Android
Chrome
Firebase
Google Cloud Platform
All products
Terms
Privacy

English
Ask about this page
bug_report
fullscreen
close
Chat
BETA
restart_alt
Not sure what to ask?
Click below for suggestions!
Show quick start prompts
Enter a prompt here
send
Responses may display inaccurate or offensive information that doesn't represent Google's views. additional details

The new page has loaded.

_____

One this you are all set with the frameworks this is the solution.  It must be robust, enterprise, elite, powerful, without any placeholders or minimizations.  It will require a multi agent system 

Skip to content
logo
Agent Development Kit
Multi-agent systems

models











 adk-python
 adk-java
Agent Development Kit
Home
Get Started
Tutorials
Agents
LLM agents
Workflow agents
Custom agents
Multi-agent systems
Models & Authentication
Tools
Running Agents
Deploy
Sessions & Memory
Callbacks
Artifacts
Events
Context
Observability
Evaluate
MCP
Plugins
Bidi-streaming (live)
Grounding
Safety and Security
A2A Protocol
Community Resources
Contributing Guide
API Reference
Table of contents
1. ADK Primitives for Agent Composition
1.1. Agent Hierarchy (Parent agent, Sub Agents)
1.2. Workflow Agents as Orchestrators
1.3. Interaction & Communication Mechanisms
a) Shared Session State (session.state)
b) LLM-Driven Delegation (Agent Transfer)
c) Explicit Invocation (AgentTool)
2. Common Multi-Agent Patterns using ADK Primitives
Coordinator/Dispatcher Pattern
Sequential Pipeline Pattern
Parallel Fan-Out/Gather Pattern
Hierarchical Task Decomposition
Review/Critique Pattern (Generator-Critic)
Iterative Refinement Pattern
Human-in-the-Loop Pattern
Multi-Agent Systems in ADK¶
As agentic applications grow in complexity, structuring them as a single, monolithic agent can become challenging to develop, maintain, and reason about. The Agent Development Kit (ADK) supports building sophisticated applications by composing multiple, distinct BaseAgent instances into a Multi-Agent System (MAS).

In ADK, a multi-agent system is an application where different agents, often forming a hierarchy, collaborate or coordinate to achieve a larger goal. Structuring your application this way offers significant advantages, including enhanced modularity, specialization, reusability, maintainability, and the ability to define structured control flows using dedicated workflow agents.

You can compose various types of agents derived from BaseAgent to build these systems:

LLM Agents: Agents powered by large language models. (See LLM Agents)
Workflow Agents: Specialized agents (SequentialAgent, ParallelAgent, LoopAgent) designed to manage the execution flow of their sub-agents. (See Workflow Agents)
Custom agents: Your own agents inheriting from BaseAgent with specialized, non-LLM logic. (See Custom Agents)
The following sections detail the core ADK primitives—such as agent hierarchy, workflow agents, and interaction mechanisms—that enable you to construct and manage these multi-agent systems effectively.

1. ADK Primitives for Agent Composition¶
ADK provides core building blocks—primitives—that enable you to structure and manage interactions within your multi-agent system.

Note

The specific parameters or method names for the primitives may vary slightly by SDK language (e.g., sub_agents in Python, subAgents in Java). Refer to the language-specific API documentation for details.

1.1. Agent Hierarchy (Parent agent, Sub Agents)¶
The foundation for structuring multi-agent systems is the parent-child relationship defined in BaseAgent.

Establishing Hierarchy: You create a tree structure by passing a list of agent instances to the sub_agents argument when initializing a parent agent. ADK automatically sets the parent_agent attribute on each child agent during initialization.
Single Parent Rule: An agent instance can only be added as a sub-agent once. Attempting to assign a second parent will result in a ValueError.
Importance: This hierarchy defines the scope for Workflow Agents and influences the potential targets for LLM-Driven Delegation. You can navigate the hierarchy using agent.parent_agent or find descendants using agent.find_agent(name).

Python
Java

# Conceptual Example: Defining Hierarchy
from google.adk.agents import LlmAgent, BaseAgent

# Define individual agents
greeter = LlmAgent(name="Greeter", model="gemini-2.5-flash")
task_doer = BaseAgent(name="TaskExecutor") # Custom non-LLM agent

# Create parent agent and assign children via sub_agents
coordinator = LlmAgent(
    name="Coordinator",
    model="gemini-2.5-flash",
    description="I coordinate greetings and tasks.",
    sub_agents=[ # Assign sub_agents here
        greeter,
        task_doer
    ]
)

# Framework automatically sets:
# assert greeter.parent_agent == coordinator
# assert task_doer.parent_agent == coordinator

1.2. Workflow Agents as Orchestrators¶
ADK includes specialized agents derived from BaseAgent that don't perform tasks themselves but orchestrate the execution flow of their sub_agents.

SequentialAgent: Executes its sub_agents one after another in the order they are listed.
Context: Passes the same InvocationContext sequentially, allowing agents to easily pass results via shared state.

Python
Java

# Conceptual Example: Sequential Pipeline
from google.adk.agents import SequentialAgent, LlmAgent

step1 = LlmAgent(name="Step1_Fetch", output_key="data") # Saves output to state['data']
step2 = LlmAgent(name="Step2_Process", instruction="Process data from {data}.")

pipeline = SequentialAgent(name="MyPipeline", sub_agents=[step1, step2])
# When pipeline runs, Step2 can access the state['data'] set by Step1.

ParallelAgent: Executes its sub_agents in parallel. Events from sub-agents may be interleaved.
Context: Modifies the InvocationContext.branch for each child agent (e.g., ParentBranch.ChildName), providing a distinct contextual path which can be useful for isolating history in some memory implementations.
State: Despite different branches, all parallel children access the same shared session.state, enabling them to read initial state and write results (use distinct keys to avoid race conditions).

Python
Java

# Conceptual Example: Parallel Execution
from google.adk.agents import ParallelAgent, LlmAgent

fetch_weather = LlmAgent(name="WeatherFetcher", output_key="weather")
fetch_news = LlmAgent(name="NewsFetcher", output_key="news")

gatherer = ParallelAgent(name="InfoGatherer", sub_agents=[fetch_weather, fetch_news])
# When gatherer runs, WeatherFetcher and NewsFetcher run concurrently.
# A subsequent agent could read state['weather'] and state['news'].

LoopAgent: Executes its sub_agents sequentially in a loop.
Termination: The loop stops if the optional max_iterations is reached, or if any sub-agent returns an Event with escalate=True in it's Event Actions.
Context & State: Passes the same InvocationContext in each iteration, allowing state changes (e.g., counters, flags) to persist across loops.

Python
Java

# Conceptual Example: Loop with Condition
from google.adk.agents import LoopAgent, LlmAgent, BaseAgent
from google.adk.events import Event, EventActions
from google.adk.agents.invocation_context import InvocationContext
from typing import AsyncGenerator

class CheckCondition(BaseAgent): # Custom agent to check state
    async def _run_async_impl(self, ctx: InvocationContext) -> AsyncGenerator[Event, None]:
        status = ctx.session.state.get("status", "pending")
        is_done = (status == "completed")
        yield Event(author=self.name, actions=EventActions(escalate=is_done)) # Escalate if done

process_step = LlmAgent(name="ProcessingStep") # Agent that might update state['status']

poller = LoopAgent(
    name="StatusPoller",
    max_iterations=10,
    sub_agents=[process_step, CheckCondition(name="Checker")]
)
# When poller runs, it executes process_step then Checker repeatedly
# until Checker escalates (state['status'] == 'completed') or 10 iterations pass.

1.3. Interaction & Communication Mechanisms¶
Agents within a system often need to exchange data or trigger actions in one another. ADK facilitates this through:

a) Shared Session State (session.state)¶
The most fundamental way for agents operating within the same invocation (and thus sharing the same Session object via the InvocationContext) to communicate passively.

Mechanism: One agent (or its tool/callback) writes a value (context.state['data_key'] = processed_data), and a subsequent agent reads it (data = context.state.get('data_key')). State changes are tracked via CallbackContext.
Convenience: The output_key property on LlmAgent automatically saves the agent's final response text (or structured output) to the specified state key.
Nature: Asynchronous, passive communication. Ideal for pipelines orchestrated by SequentialAgent or passing data across LoopAgent iterations.
See Also: State Management

Python
Java

# Conceptual Example: Using output_key and reading state
from google.adk.agents import LlmAgent, SequentialAgent

agent_A = LlmAgent(name="AgentA", instruction="Find the capital of France.", output_key="capital_city")
agent_B = LlmAgent(name="AgentB", instruction="Tell me about the city stored in {capital_city}.")

pipeline = SequentialAgent(name="CityInfo", sub_agents=[agent_A, agent_B])
# AgentA runs, saves "Paris" to state['capital_city'].
# AgentB runs, its instruction processor reads state['capital_city'] to get "Paris".

b) LLM-Driven Delegation (Agent Transfer)¶
Leverages an LlmAgent's understanding to dynamically route tasks to other suitable agents within the hierarchy.

Mechanism: The agent's LLM generates a specific function call: transfer_to_agent(agent_name='target_agent_name').
Handling: The AutoFlow, used by default when sub-agents are present or transfer isn't disallowed, intercepts this call. It identifies the target agent using root_agent.find_agent() and updates the InvocationContext to switch execution focus.
Requires: The calling LlmAgent needs clear instructions on when to transfer, and potential target agents need distinct descriptions for the LLM to make informed decisions. Transfer scope (parent, sub-agent, siblings) can be configured on the LlmAgent.
Nature: Dynamic, flexible routing based on LLM interpretation.

Python
Java

# Conceptual Setup: LLM Transfer
from google.adk.agents import LlmAgent

booking_agent = LlmAgent(name="Booker", description="Handles flight and hotel bookings.")
info_agent = LlmAgent(name="Info", description="Provides general information and answers questions.")

coordinator = LlmAgent(
    name="Coordinator",
    model="gemini-2.5-flash",
    instruction="You are an assistant. Delegate booking tasks to Booker and info requests to Info.",
    description="Main coordinator.",
    # AutoFlow is typically used implicitly here
    sub_agents=[booking_agent, info_agent]
)
# If coordinator receives "Book a flight", its LLM should generate:
# FunctionCall(name='transfer_to_agent', args={'agent_name': 'Booker'})
# ADK framework then routes execution to booking_agent.

c) Explicit Invocation (AgentTool)¶
Allows an LlmAgent to treat another BaseAgent instance as a callable function or Tool.

Mechanism: Wrap the target agent instance in AgentTool and include it in the parent LlmAgent's tools list. AgentTool generates a corresponding function declaration for the LLM.
Handling: When the parent LLM generates a function call targeting the AgentTool, the framework executes AgentTool.run_async. This method runs the target agent, captures its final response, forwards any state/artifact changes back to the parent's context, and returns the response as the tool's result.
Nature: Synchronous (within the parent's flow), explicit, controlled invocation like any other tool.
(Note: AgentTool needs to be imported and used explicitly).

Python
Java

# Conceptual Setup: Agent as a Tool
from google.adk.agents import LlmAgent, BaseAgent
from google.adk.tools import agent_tool
from pydantic import BaseModel

# Define a target agent (could be LlmAgent or custom BaseAgent)
class ImageGeneratorAgent(BaseAgent): # Example custom agent
    name: str = "ImageGen"
    description: str = "Generates an image based on a prompt."
    # ... internal logic ...
    async def _run_async_impl(self, ctx): # Simplified run logic
        prompt = ctx.session.state.get("image_prompt", "default prompt")
        # ... generate image bytes ...
        image_bytes = b"..."
        yield Event(author=self.name, content=types.Content(parts=[types.Part.from_bytes(image_bytes, "image/png")]))

image_agent = ImageGeneratorAgent()
image_tool = agent_tool.AgentTool(agent=image_agent) # Wrap the agent

# Parent agent uses the AgentTool
artist_agent = LlmAgent(
    name="Artist",
    model="gemini-2.5-flash",
    instruction="Create a prompt and use the ImageGen tool to generate the image.",
    tools=[image_tool] # Include the AgentTool
)
# Artist LLM generates a prompt, then calls:
# FunctionCall(name='ImageGen', args={'image_prompt': 'a cat wearing a hat'})
# Framework calls image_tool.run_async(...), which runs ImageGeneratorAgent.
# The resulting image Part is returned to the Artist agent as the tool result.

These primitives provide the flexibility to design multi-agent interactions ranging from tightly coupled sequential workflows to dynamic, LLM-driven delegation networks.

2. Common Multi-Agent Patterns using ADK Primitives¶
By combining ADK's composition primitives, you can implement various established patterns for multi-agent collaboration.

Coordinator/Dispatcher Pattern¶
Structure: A central LlmAgent (Coordinator) manages several specialized sub_agents.
Goal: Route incoming requests to the appropriate specialist agent.
ADK Primitives Used:
Hierarchy: Coordinator has specialists listed in sub_agents.
Interaction: Primarily uses LLM-Driven Delegation (requires clear descriptions on sub-agents and appropriate instruction on Coordinator) or Explicit Invocation (AgentTool) (Coordinator includes AgentTool-wrapped specialists in its tools).

Python
Java

# Conceptual Code: Coordinator using LLM Transfer
from google.adk.agents import LlmAgent

billing_agent = LlmAgent(name="Billing", description="Handles billing inquiries.")
support_agent = LlmAgent(name="Support", description="Handles technical support requests.")

coordinator = LlmAgent(
    name="HelpDeskCoordinator",
    model="gemini-2.5-flash",
    instruction="Route user requests: Use Billing agent for payment issues, Support agent for technical problems.",
    description="Main help desk router.",
    # allow_transfer=True is often implicit with sub_agents in AutoFlow
    sub_agents=[billing_agent, support_agent]
)
# User asks "My payment failed" -> Coordinator's LLM should call transfer_to_agent(agent_name='Billing')
# User asks "I can't log in" -> Coordinator's LLM should call transfer_to_agent(agent_name='Support')

Sequential Pipeline Pattern¶
Structure: A SequentialAgent contains sub_agents executed in a fixed order.
Goal: Implement a multi-step process where the output of one step feeds into the next.
ADK Primitives Used:
Workflow: SequentialAgent defines the order.
Communication: Primarily uses Shared Session State. Earlier agents write results (often via output_key), later agents read those results from context.state.

Python
Java

# Conceptual Code: Sequential Data Pipeline
from google.adk.agents import SequentialAgent, LlmAgent

validator = LlmAgent(name="ValidateInput", instruction="Validate the input.", output_key="validation_status")
processor = LlmAgent(name="ProcessData", instruction="Process data if {validation_status} is 'valid'.", output_key="result")
reporter = LlmAgent(name="ReportResult", instruction="Report the result from {result}.")

data_pipeline = SequentialAgent(
    name="DataPipeline",
    sub_agents=[validator, processor, reporter]
)
# validator runs -> saves to state['validation_status']
# processor runs -> reads state['validation_status'], saves to state['result']
# reporter runs -> reads state['result']

Parallel Fan-Out/Gather Pattern¶
Structure: A ParallelAgent runs multiple sub_agents concurrently, often followed by a later agent (in a SequentialAgent) that aggregates results.
Goal: Execute independent tasks simultaneously to reduce latency, then combine their outputs.
ADK Primitives Used:
Workflow: ParallelAgent for concurrent execution (Fan-Out). Often nested within a SequentialAgent to handle the subsequent aggregation step (Gather).
Communication: Sub-agents write results to distinct keys in Shared Session State. The subsequent "Gather" agent reads multiple state keys.

Python
Java

# Conceptual Code: Parallel Information Gathering
from google.adk.agents import SequentialAgent, ParallelAgent, LlmAgent

fetch_api1 = LlmAgent(name="API1Fetcher", instruction="Fetch data from API 1.", output_key="api1_data")
fetch_api2 = LlmAgent(name="API2Fetcher", instruction="Fetch data from API 2.", output_key="api2_data")

gather_concurrently = ParallelAgent(
    name="ConcurrentFetch",
    sub_agents=[fetch_api1, fetch_api2]
)

synthesizer = LlmAgent(
    name="Synthesizer",
    instruction="Combine results from {api1_data} and {api2_data}."
)

overall_workflow = SequentialAgent(
    name="FetchAndSynthesize",
    sub_agents=[gather_concurrently, synthesizer] # Run parallel fetch, then synthesize
)
# fetch_api1 and fetch_api2 run concurrently, saving to state.
# synthesizer runs afterwards, reading state['api1_data'] and state['api2_data'].

Hierarchical Task Decomposition¶
Structure: A multi-level tree of agents where higher-level agents break down complex goals and delegate sub-tasks to lower-level agents.
Goal: Solve complex problems by recursively breaking them down into simpler, executable steps.
ADK Primitives Used:
Hierarchy: Multi-level parent_agent/sub_agents structure.
Interaction: Primarily LLM-Driven Delegation or Explicit Invocation (AgentTool) used by parent agents to assign tasks to subagents. Results are returned up the hierarchy (via tool responses or state).

Python
Java

# Conceptual Code: Hierarchical Research Task
from google.adk.agents import LlmAgent
from google.adk.tools import agent_tool

# Low-level tool-like agents
web_searcher = LlmAgent(name="WebSearch", description="Performs web searches for facts.")
summarizer = LlmAgent(name="Summarizer", description="Summarizes text.")

# Mid-level agent combining tools
research_assistant = LlmAgent(
    name="ResearchAssistant",
    model="gemini-2.5-flash",
    description="Finds and summarizes information on a topic.",
    tools=[agent_tool.AgentTool(agent=web_searcher), agent_tool.AgentTool(agent=summarizer)]
)

# High-level agent delegating research
report_writer = LlmAgent(
    name="ReportWriter",
    model="gemini-2.5-flash",
    instruction="Write a report on topic X. Use the ResearchAssistant to gather information.",
    tools=[agent_tool.AgentTool(agent=research_assistant)]
    # Alternatively, could use LLM Transfer if research_assistant is a sub_agent
)
# User interacts with ReportWriter.
# ReportWriter calls ResearchAssistant tool.
# ResearchAssistant calls WebSearch and Summarizer tools.
# Results flow back up.

Review/Critique Pattern (Generator-Critic)¶
Structure: Typically involves two agents within a SequentialAgent: a Generator and a Critic/Reviewer.
Goal: Improve the quality or validity of generated output by having a dedicated agent review it.
ADK Primitives Used:
Workflow: SequentialAgent ensures generation happens before review.
Communication: Shared Session State (Generator uses output_key to save output; Reviewer reads that state key). The Reviewer might save its feedback to another state key for subsequent steps.

Python
Java

# Conceptual Code: Generator-Critic
from google.adk.agents import SequentialAgent, LlmAgent

generator = LlmAgent(
    name="DraftWriter",
    instruction="Write a short paragraph about subject X.",
    output_key="draft_text"
)

reviewer = LlmAgent(
    name="FactChecker",
    instruction="Review the text in {draft_text} for factual accuracy. Output 'valid' or 'invalid' with reasons.",
    output_key="review_status"
)

# Optional: Further steps based on review_status

review_pipeline = SequentialAgent(
    name="WriteAndReview",
    sub_agents=[generator, reviewer]
)
# generator runs -> saves draft to state['draft_text']
# reviewer runs -> reads state['draft_text'], saves status to state['review_status']

Iterative Refinement Pattern¶
Structure: Uses a LoopAgent containing one or more agents that work on a task over multiple iterations.
Goal: Progressively improve a result (e.g., code, text, plan) stored in the session state until a quality threshold is met or a maximum number of iterations is reached.
ADK Primitives Used:
Workflow: LoopAgent manages the repetition.
Communication: Shared Session State is essential for agents to read the previous iteration's output and save the refined version.
Termination: The loop typically ends based on max_iterations or a dedicated checking agent setting escalate=True in the Event Actions when the result is satisfactory.

Python
Java

# Conceptual Code: Iterative Code Refinement
from google.adk.agents import LoopAgent, LlmAgent, BaseAgent
from google.adk.events import Event, EventActions
from google.adk.agents.invocation_context import InvocationContext
from typing import AsyncGenerator

# Agent to generate/refine code based on state['current_code'] and state['requirements']
code_refiner = LlmAgent(
    name="CodeRefiner",
    instruction="Read state['current_code'] (if exists) and state['requirements']. Generate/refine Python code to meet requirements. Save to state['current_code'].",
    output_key="current_code" # Overwrites previous code in state
)

# Agent to check if the code meets quality standards
quality_checker = LlmAgent(
    name="QualityChecker",
    instruction="Evaluate the code in state['current_code'] against state['requirements']. Output 'pass' or 'fail'.",
    output_key="quality_status"
)

# Custom agent to check the status and escalate if 'pass'
class CheckStatusAndEscalate(BaseAgent):
    async def _run_async_impl(self, ctx: InvocationContext) -> AsyncGenerator[Event, None]:
        status = ctx.session.state.get("quality_status", "fail")
        should_stop = (status == "pass")
        yield Event(author=self.name, actions=EventActions(escalate=should_stop))

refinement_loop = LoopAgent(
    name="CodeRefinementLoop",
    max_iterations=5,
    sub_agents=[code_refiner, quality_checker, CheckStatusAndEscalate(name="StopChecker")]
)
# Loop runs: Refiner -> Checker -> StopChecker
# State['current_code'] is updated each iteration.
# Loop stops if QualityChecker outputs 'pass' (leading to StopChecker escalating) or after 5 iterations.

Human-in-the-Loop Pattern¶
Structure: Integrates human intervention points within an agent workflow.
Goal: Allow for human oversight, approval, correction, or tasks that AI cannot perform.
ADK Primitives Used (Conceptual):
Interaction: Can be implemented using a custom Tool that pauses execution and sends a request to an external system (e.g., a UI, ticketing system) waiting for human input. The tool then returns the human's response to the agent.
Workflow: Could use LLM-Driven Delegation (transfer_to_agent) targeting a conceptual "Human Agent" that triggers the external workflow, or use the custom tool within an LlmAgent.
State/Callbacks: State can hold task details for the human; callbacks can manage the interaction flow.
Note: ADK doesn't have a built-in "Human Agent" type, so this requires custom integration.

Python
Java

# Conceptual Code: Using a Tool for Human Approval
from google.adk.agents import LlmAgent, SequentialAgent
from google.adk.tools import FunctionTool

# --- Assume external_approval_tool exists ---
# This tool would:
# 1. Take details (e.g., request_id, amount, reason).
# 2. Send these details to a human review system (e.g., via API).
# 3. Poll or wait for the human response (approved/rejected).
# 4. Return the human's decision.
# async def external_approval_tool(amount: float, reason: str) -> str: ...
approval_tool = FunctionTool(func=external_approval_tool)

# Agent that prepares the request
prepare_request = LlmAgent(
    name="PrepareApproval",
    instruction="Prepare the approval request details based on user input. Store amount and reason in state.",
    # ... likely sets state['approval_amount'] and state['approval_reason'] ...
)

# Agent that calls the human approval tool
request_approval = LlmAgent(
    name="RequestHumanApproval",
    instruction="Use the external_approval_tool with amount from state['approval_amount'] and reason from state['approval_reason'].",
    tools=[approval_tool],
    output_key="human_decision"
)

# Agent that proceeds based on human decision
process_decision = LlmAgent(
    name="ProcessDecision",
    instruction="Check {human_decision}. If 'approved', proceed. If 'rejected', inform user."
)

approval_workflow = SequentialAgent(
    name="HumanApprovalWorkflow",
    sub_agents=[prepare_request, request_approval, process_decision]
)

These patterns provide starting points for structuring your multi-agent systems. You can mix and match them as needed to create the most effective architecture for your specific application.

 Back to top
Previous
Custom agents
Next
Models & Authentication
Copyright Google 2025
Made with Material for MkDocs


____

SOLUTION:

The purpose of this solution is for the system to find viral contenton socal media for my Aesthetics company https://mdaesthetics.ca / https://www.tiktok.com/@copelandmda and https://www.instagram.com/mdaesthetics.ca

I require this system to research on Instagram ad tik tok which posts are currently viral so that my company can piggy back off the viral videos to post our own in order to gain viewers, followers, reposts, and ultimately sales for my company.  
 on Instagram and TIk Tik to run and then email the results to both christine.carrer@hotmail.com and dalkeith@golden.net - (YOu can use my gmail credentials already linked to the firebase solution (dalkeith74@gmail.com)
 I will provide my Instagram and Tik Tok credentials as needed but you msut research how best to research current viral content using this and Google Grounding per above)
 
 Once the viral posts are found the system must CREATE a Instagram and Tik Tok posts that can be copied and posted manually by me.  The content must be Enterprise, and the system must make the content BETTER than the viral posts that were researched.  The system MUST have AI with a chat box to be able to communicate and alter the created posts to curate them to my liking before an output occurs via a button.  SO there needs to be different areas on the web site . The Research portion on demand, the daily email sent with results in the backroun wth 4  diffrent viral  findig results2 for instagram and 2 for tik tok.The AI chat box where the AI (COmmand Centre) can be told what to do (Ie Research, and also create a post based on the findings with free flow text, before an output in proper Instagram and Tik Tok format can be provided.  It is imperative that the system does not use basic framework output but proper enterprise content creation that mimik the style and approah and content of the viral posts.

 PROBLEM STATEMENTS FOR THE SOLUTION TO NOTE AND CONTENT TO FOLLOW SPECIFICALLY WHILE SEARXH SEPERATELY FOR VIRAL CONTENT 
PHotos are here C:\bach\viral\example photos
 Artisan aesthetics 
All of their marketing is super clean and cute/girly 

Skin Vitality 
They are the biggest in aesthetics in Toronto and probably Whitby. 
Good viral Instagram profile and reels and they pump out content like crazy.
Don't love the look of the website but like the IG
They are sketchy though, you arent allowed to advertise the word "Botox" for like $4 per unit and they do it all the time.
Have to use the word Tox or Neuromodulator or neurotoxin 

Subtle Aesthetics 
Besides artisan aesthetics this is another local med spa that has great marketing on IG, educational content that's also relatable and are good at speaking to their local clients 

-   Attached photo of their IG
    


The look aesthetics 
Really love their content. Yesterday I reposted a video of theirs talking about vital tips for laser skin prep and Lisa loved it. 
Would be good to model posts after theirs considering they're in Tennessee and wouldn't look like I'm copying local competitors as much

-   Attached screenshot of their IG 
    


Also attached photos of my Inspo folder on Instagram
I can't share that folder it's only accessable to me but on each of those posts it's either a video or photo from all different profiles. 

Another way I get ideas for content, if I want to do a reel (video) about Botox I'll search the word on Instagram and I can see from there what videos have the most views from all profiles not just ones I follow 


It's hard because there is so much to focus on but I mainly need to create videos educating and promoting our current treatments and skincare that is our August promotions so videos and posts about....


-   SkinTyte treatment 
    
-   Body Care (using Vivier vitamin C Scrub and body lotion benefits, KP - keratosis pilaris what is it and how to combat it) 
    
-   Radiesse injections + ULTHERAPY treatment we have called the Duo-C-Lift which means collagen therapy
    
-   Specifically highlighting the area under the ASS and where you can get cellulite and dimpling/lax and sagging skin.
    
-   The problem is for that I can explain more but I basically can't use the actual words but I can say firm, smooth lift and tone buttocks package 

----

You msut research the BEST agents to use 
https://google.github.io/adk-docs/api-reference/python
https://google.github.io/adk-docs/api-reference/python/google-adk.html

The Multi agent system must 1. LEARN 2. ADAPT 3. COLABORATE 4. INNOVATE 5. EXECUTE
It will require ongoing training and fine-tuning to ensure optimal performance and relevance in the ever-evolving landscape of social media trends and user preferences.  The Multi agent systrem must have a AI COmmand center intake agent (with persistence and grounding), a researcher, an analyzer, a reviewer, a content creater, a reviewer to help question all the agents, a thinker to determine how to be proactive and not only mimik the creation but determine what direction iral videos are going to craete the new one, and a QA agent to make sure all requirements are met before any out put occurs.  This solution must be scaleable so that agents can be changed or added.  The GUI MUST be clean, THe code must be uniform, Everything must work.

Both Firebase and Google ADK must use the JS version so they work well together.  
All versions need to be the laterst with the mst modern and robust new features. 
