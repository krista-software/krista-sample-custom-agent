# User Experience · Hello Custom Agent

This design covers the shared greeting view and its launch states. It follows FR-001 through FR-005 in the [validated requirements](requirements-analysis.md). The examples use illustrative names.

## User journeys

### Run the sample locally

A developer starts the sample with its documented standalone command and opens the local URL in their browser. The greeting shows the sample user and workspace with a visible **Local sample data** label. The developer can confirm the view renders without a Krista login. The local command explicitly selects the fixture mode. See the [launch mode findings](findings.md#4-standalone-mode-exercises-the-app-with-explicit-dev-data).

### Open the installed agent in Krista

A workspace installer uses the existing Studio workflow to upload the JAR, add its invoker to the workspace, save its HTTPS app URL in Setup, and assign it in the topic’s Agent Configuration. A regular workspace user opens that topic in Krista. The app displays a waiting state while the launch and authenticated extension request complete. Once the KSDK reads succeed, the view shows the signed-in user’s display name and the active workspace name. Verify the complete flow and the URL refresh procedure in the test workspace. See the [registration findings](findings.md#1-upload-an-extension-jar-then-configure-and-assign-its-invoker) and [URL findings](findings.md#2-the-extension-returns-the-app-url-and-studio-saves-it-with-the-topic).

## Information architecture and wireframes

One page with a main content area. The local browser and the Krista topic iframe render the same greeting view. Installation and topic configuration take place in Studio.

```text
Local browser                    Krista topic iframe
+---------------------------+    +---------------------------+
| Hello, Alex               |    | Hello, Morgan             |
| Workspace: Sample Space   |    | Workspace: Demo Team     |
| Local sample data         |    |                           |
+---------------------------+    +---------------------------+
```

## View states and interactions

| State | UI and behavior |
| --- | --- |
| Standalone ready | Render the shared greeting and workspace line with the local sample-data label. |
| Krista launch pending | Show “Waiting for Krista” while launch context or the authenticated data request is pending. Replace the message when data arrives or the launch fails. |
| Krista ready | Render the shared greeting with the display name and workspace name supplied by the authenticated extension request. |
| Krista launch unavailable | If required launch context is unavailable, show “Open this agent from its Krista topic.” If the authenticated data request fails, show “Couldn’t load your greeting. Reopen the agent from its Krista topic.” The failure state stays in the Krista launch mode. |

Render the greeting as the main heading and the workspace as a labeled text line. Use text as well as visual styling for status, allow names to wrap in a narrow iframe, and announce asynchronous state changes to assistive technology without moving focus. The view has no additional controls. Recovery uses the existing Krista topic navigation.

## Implementation verification

Verify the same greeting structure in the standalone browser and the Krista topic. In the live check, compare the shown names with the regular test user and workspace. Verify the pending or error state when the platform launch context is unavailable. These are design targets awaiting a running sample and test workspace.
