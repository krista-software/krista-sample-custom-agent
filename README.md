# Hello Custom Agent

Hello is a developer sample for building an Angular view and registering a native Java extension under Krista’s **Agent** domain. You can run the greeting locally with sample data or build the platform app to exercise Studio URL discovery and read the signed-in user’s name, workspace, roles, and visible personal attributes from KSDK when the topic opens. One platform JAR supports US production, Solution Labs, and POC. Configure each invoker in Studio.

## Run the standalone greeting

Use Node.js 22.12 or later in the 22 series, or Node.js 24, with npm. From this checkout, start the Angular development server.

```sh
cd ui
npm ci
npm run start:standalone
```

Open [http://localhost:4200/](http://localhost:4200/) in a browser. The page shows **Hello, Alex**, **Workspace: Sample Space**, and **Local sample data**, with example role chips and personal attributes. To change the fixture names, edit [the standalone entry point](ui/src/main.standalone.ts). This mode exercises the [shared Angular view](ui/src/greeting.ts) and needs no Krista session or running extension.

## Build the platform artifacts

Use JDK 21, the included Gradle 8.13 wrapper, and the Node.js and npm versions above. A fresh Gradle build needs access to the Krista artifact repository and Maven Central. From the checkout root, build the Angular platform shell first, then package it with the extension.

```sh
(cd ui && npm ci && npm run build:platform)
./gradlew jar
jar tf build/libs/sample-custom-agent-1.4.0.jar
```

Check for the compiled classes under `app/krista/sample/hello/` and generated metadata at `META-INF/krista/extension.json`. Gradle copies the Angular bundle from `ui/dist/platform/browser/` to `app/` in the JAR. The [extension declaration](src/main/java/app/krista/sample/hello/HelloExtension.java) exposes that directory through Framework 4 static resources.

The build defaults to Krista SDK and annotation processor version `1.0.127`. If the test workspace requires a different approved version, pass it with `-PkristaApiVersion` when building the JAR. The extension requests the platform's default Framework 4 container base. [Build configuration](build.gradle)

## Install and assign in Studio

Sign in to [US production Studio](https://studio.us.krista.app/), [Solution Labs Studio](https://studio.solution-labs.eng.krista.app/), or [POC Studio](https://studio.poc.krista.app/) and select the workspace where the agent will be installed. Use an installer account that can upload to the catalog, add an invoker, and edit agent settings. Use a dedicated agent and topic for verification. [Custom agent deployment guide](docs/custom-agent-dev-guide.md)

1. In Studio, open **Catalog → Krista → Agent → Create New Extension**. Under **Native Java Jar**, select **Upload** and provide the JAR, a catalog name, and a description.
2. Open the resulting catalog entry and choose **Add to Workspace → Krista Managed (Internal) → Add** to create its workspace invoker.
3. Open the live invoker's **Details** tab and copy its **Extension Base URL**. Append `/static/app/index.html` to form the bundled HTTPS entry. Open it directly to check that the sample displays **Open this agent from its Krista topic.**
4. Open the invoker's **Setup** tab. Set **Deployment Environment** to **US Production**, **Solution Labs**, or **POC**, then paste that invoker’s bundled HTTPS entry into **App URL** and save. Studio uses the selected environment to validate the extension host. All three choices use the same JAR.
5. Open the test agent's **Agent setup → Agent configuration**. In the picker labeled **Select how the agent is triggered from the dashboard**, select the Hello invoker. Wait for **Hosting URL** to show the discovered entry, then save.
6. Select **Open Client** in Studio to continue in the same workspace on the corresponding Krista client. Open the assigned topic and verify that its iframe loads the configured HTTPS entry.

Studio requests `GET <routed invoker base>/rest/hostUrl` during selection. The resource reads the current invoker’s Setup on each request. A missing URL or inconsistent environment and URL returns HTTP 503 with a configuration message. A valid URL yields HTTP 200 with the JSON below. [Discovery resource](src/main/java/app/krista/sample/hello/HostingResource.java)

```json
{
  "payload": {
    "hostingUrl": "https://extension.us.krista.app/extension/api/ROUTING_ID/static/app/index.html"
  }
}
```

### Refresh a saved topic URL

Studio saves a copy of the discovered URL on the topic when you save the assignment. To refresh it, save the new **App URL** on the same invoker. Under **Agent setup → Agent configuration**, use the picker's clear icon and reselect the same invoker before saving. Wait for **Hosting URL** to show the new value, save once, and reload the topic to verify its iframe target. Saving while the selection is empty removes the assignment.

## Verify the live greeting

Open the topic in your target workspace and compare the greeting with the signed-in profile and active workspace. The platform app passes the Krista launch credential to the same invoker’s `GET /rest/greeting`. After Krista binds the authenticated account, the greeting resource reads the display name from `AuthorizationContext.getAuthorizedAccount().getPerson().getPersonName()` and the workspace name from `Workspace.getWorkspaceName()`. It uses `Workspace.getWorkspaceId()` to confirm workspace context. The same account supplies workspace role names and primary email. To display custom text, numeric, and boolean values, the extension matches that account’s values to the definitions visible through the workspace’s KSDK attribute provider. [Test workspace findings](docs-specs/sample-agent/findings.md)

The [acceptance test plan](docs-specs/sample-agent/test-plan.md) covers Studio installation and discovery, a regular user's launch and a credential-free call, topic URL refresh, standalone mode, and missing launch context. Use a separate regular-user session for the live greeting check. Record the actual request route, credential type, and account and workspace match results in the [findings](docs-specs/sample-agent/findings.md).
