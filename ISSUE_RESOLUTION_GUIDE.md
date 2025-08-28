# Guide to Resolving Remaining Issues

## Java Code Issues

### Unused Variables

Several Java classes have unused variables. These can be fixed by:
Several Java classes have unused variables. These can be fixed by:

1. Removing the variable declaration if it's not needed

2. Using the variable in the code
3. Prefixing with `@SuppressWarnings("unused")` if intentionally unused

Examples:

```java
// In CallbackContext.java
var unused = // REMOVE THIS LINE OR USE THE VARIABLE

// In GcsArtifactService.java
var unused = storageClient.delete(blobIdsToDelete); // CHANGE TO:
storageClient.delete(blobIdsToDelete);
```

### Null Pointer Access

Several methods have potential null pointer access. Fix by:

1. Adding null checks before method calls

2. Using the Optional API

3. Using the null-safe call pattern

Example:

```java
// BEFORE:
activeTool.task().dispose();

// AFTER:
if (activeTool.task() != null) {
  activeTool.task().dispose();
}
```

### Deprecated Method Usage

Update code to use non-deprecated alternatives:

```java
// BEFORE:
RequestBody.create(MEDIA_TYPE_APPLICATION_JSON, requestJson)

// AFTER:
RequestBody.create(requestJson, MEDIA_TYPE_APPLICATION_JSON)
```

## Markdown Documentation Issues

The markdown linting errors can be fixed by:
The markdown linting errors can be fixed by:

1. Adding blank lines before and after headings

2. Removing trailing punctuation from headings

3. Adding blank lines around code blocks

4. Ensuring files end with a newline

## Next.js Web App

The main issue has been fixed by updating the import path in `SocialMediaPost.tsx`. If additional issues occur:

1. Check import paths in all components
2. Verify Next.js config settings
3. Clear Next.js build cache: `rm -rf /workspaces/adk-java/web/.next`

## Firebase Functions

1. Ensure correct imports from firebase-functions (not v1/v2 subpaths)
2. Verify all API usage is compatible with the imported version
