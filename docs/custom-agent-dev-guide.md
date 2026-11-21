# Build and deploy a Krista custom agent

Use this guide to make a web app available inside a Krista agent. The example uses a native Java extension to tell Studio where the app is hosted and an Angular app to render the agent in the Krista client. The key deployment step is copying the **Extension Base URL** of the installed workspace invoker, then using it to configure and assign the agent.

## How the pieces fit together

| Piece | What it does |
| --- | --- |
| **Catalog extension** | The versioned Java JAR registered in Studio under **Krista → Agent**. |
| **Workspace invoker** | The installed instance of that extension in one workspace. It has its own route and **Setup** values. Installing the same catalog entry in another workspace creates a separate invoker. |
| **Extension Base URL** | The complete routed HTTPS address shown on the live invoker’s **Details** tab. It includes the extension host and routing ID. Use it as the prefix for the extension’s HTTP routes. |
| **App URL** | The HTTPS web entry point saved on the invoker. The browser opens this URL in the agent iframe. For the bundled app, append `/static/app/index.html` to **Extension Base URL**. |
| **Agent assignment** | The configuration saved in Studio for the agent and its client topic. Studio discovers the **Hosting URL** from the selected invoker and saves a copy with the assignment. |

```text
Build the app + native Java JAR
  → upload to the Agent domain in Studio Catalog
  → add to the workspace to create an invoker
  → copy its Extension Base URL and save the app entry in Setup
  → select the invoker in the Studio agent and save the discovered Hosting URL
  → open the agent in the Krista client
```

## Start locally

The Hello sample uses JDK 21, the included Gradle wrapper, and Node.js 22.12 or later in the 22 series, or Node.js 24. A clean build needs access to the Krista artifact repository, Maven Central, and the UI package registry. For the live walkthrough, use a test workspace where you can upload to the catalog, add and configure an invoker, and create or edit a test agent. Arrange a separate regular workspace user who can open the test agent if you are validating identity. [Extension development prerequisites](https://github.com/krista-software/extension-development-documentation/blob/main/getting-started/Prerequisites.md)

From the sample checkout, run the development app and open `http://localhost:4200/`. Expect **Hello, Alex**, **Workspace: Sample Space**, and the **Local sample data** label. [Standalone entry point](../ui/src/main.standalone.ts)

```sh
cd ui
npm ci
npm run start:standalone
```

Select standalone and platform mode explicitly in your build. Use fixture data for local UI work and wait for launch context in the platform build. If that context is missing, show a message that directs the user to open the agent from Krista. The sample shares its greeting view between the two modes. [Angular build configurations](../ui/angular.json) · [Shared greeting view](../ui/src/greeting.ts) · [Local extension development workflow](https://github.com/krista-software/extension-development-documentation/blob/main/getting-started/LocalDevelopmentWorkflow.md)

## Build the extension and host the app

1. Register the extension for the **Agent** domain in the **Krista** ecosystem. Declare a required text Setup attribute for **App URL** and validate that it is an absolute HTTPS URL with a host and a valid port if specified. The minimal Hello extension uses Framework 4 and registers a JAX-RS application for URL discovery. It installed successfully without a placeholder catalog request. Add catalog requests when your extension exposes catalog actions. [Agent domain identifiers](https://github.com/krista-software/extension-development-documentation/blob/main/custom-agents/DomainRegistration.md#ecosystem-and-domain-values) · [Setup attributes](https://github.com/krista-software/extension-development-documentation/blob/main/setup-tab/Attributes.md) · [Sample extension declaration](../src/main/java/app/krista/sample/hello/HelloExtension.java)
2. Choose where the browser will get the app. For bundled hosting, put the web build in the JAR and map the resource directory with `@StaticResource`. The sample maps `app` to `app` and uses relative asset URLs so its scripts and styles load from a nested invoker path. For a separately hosted UI, use a browser-reachable HTTPS entry point and configure the app host to allow framing by the US Krista client origin, `https://us.krista.app`. If that UI calls the extension across origins, verify the actual request and preflight with your deployment’s allowed origins and headers. Check framing in your target workspace because the browser receives headers from the deployed host and gateway. [Framework 4 static resources](https://github.com/krista-software/extension-development-documentation/blob/main/development/Framework4Features.md#staticresource)
3. Build and inspect the JAR. For the Hello sample, run these commands from the checkout root. Check that the JAR contains the extension classes, generated `META-INF/krista/extension.json`, and `app/index.html` with its scripts and styles. The sample uses Krista SDK and annotation processor `1.0.127` by default and requests the platform’s default Framework 4 container base. Use versions supported by your target deployment. [Sample Gradle configuration](../build.gradle) · [Java extension packaging](https://github.com/krista-software/extension-development-documentation/blob/main/getting-started/BuildingAndPackaging.md) · [Framework 4 container configuration](https://github.com/krista-software/extension-development-documentation/blob/main/development/Framework4Features.md#containerize)

```sh
(cd ui && npm ci && npm run build:platform)
./gradlew jar
jar tf build/libs/sample-custom-agent-1.0.0.jar
```

### Implement URL discovery

Register `GET /hostUrl` in the extension’s JAX-RS application. Studio accesses it at `GET <Extension Base URL>/rest/hostUrl`. Read the current invoker’s saved **App URL** for each request. The sample injects the current invoker as `@Named("self")`, validates the saved value and returns it unchanged, including the path and query string. For missing or invalid Setup, the sample returns HTTP 503 with a configuration message. Keep the URL free of secrets and keep discovery callable during Studio assignment, before any end-user launch token exists. [JAX-RS registration](../src/main/java/app/krista/sample/hello/HelloApplication.java) · [Discovery implementation](../src/main/java/app/krista/sample/hello/HostingResource.java)

HTTP 200 response body:

```json
{
  "payload": {
    "hostingUrl": "https://apps.example.com/hello/index.html"
  }
}
```

## Deploy and assign the agent in US production

Start in [US production Studio](https://studio.us.krista.app/) and select the workspace that will host the agent. You’ll install the JAR and configure the agent in that workspace, then open the same workspace in [the US Krista client](https://us.krista.app/). Copy the invoker route and choose the verification user during deployment.

1. **Upload the native JAR.** Open Studio for the target workspace. Go to **Catalog → Krista → Agent → Create New Extension**. On the **Native Java Jar** tab, select **Upload**, choose the JAR, and supply a catalog name and description. The **Import Catalog** action accepts `.daz` catalog bundles. Use **Native Java Jar** when following this guide. [Native JAR build and packaging](https://github.com/krista-software/extension-development-documentation/blob/main/getting-started/BuildingAndPackaging.md)
2. **Install it in the workspace.** Open the new catalog entry and select **Add to Workspace**. Choose **Krista Managed (Internal)** for this walkthrough, then select **Add**. Wait for provisioning. Open the installed invoker under the workspace’s **Extensions → Agents** and confirm it is in **Live** mode.
3. **Copy the complete routed URL.** In the invoker’s **Details** tab, confirm **Primary Domain** is **Agent**, then copy **Extension Base URL**. It commonly looks like `https://extension.<krista-domain-name>/extension/api/<routing-id>`. Use the full value Studio displays for this invoker, including its host and routing ID. For bundled hosting, append `/static/app/index.html`. Open the resulting entry URL directly in a browser to verify that the page and assets load. The Hello platform build displays its recovery message when opened directly.
4. **Save the app entry point.** Open the live invoker’s **Setup** tab. Enter the full HTTPS entry URL in **App URL** and select **Save changes**. If you host the app separately, enter its full HTTPS entry URL here. Check discovery using the **Extension Base URL** copied from this workspace. Expect HTTP 200 and the exact app entry in `payload.hostingUrl`. Replace the placeholder below with the copied value.

   ```sh
   extension_base='PASTE_EXTENSION_BASE_URL_HERE'
   curl -i "${extension_base%/}/rest/hostUrl"
   ```

5. **Create and configure the Studio agent.** Go to **Agents → New Agent**, give the test agent a name, and open it. Under **Agent setup → Agent configuration**, use the invoker picker labeled **Select how the agent is triggered from the dashboard**. Select the invoker you installed. Wait for **Hosting URL** to show the expected app entry and select **Save**. Give the intended test users access through the workspace’s normal configuration.
6. **Open the agent in the Krista client.** Select **Open Client** in Studio, confirm you are in the same workspace on `us.krista.app`, then open your new agent. Confirm that the iframe document uses the expected app URL and that its scripts and styles load. Check this step from the client as well as opening the URL directly. A page that serves over HTTPS can still be blocked from loading in a frame.

### Refresh the app URL or upgrade the JAR

- **Change only the app URL.** Save the new **App URL** on the same live invoker. In the Studio agent’s **Agent configuration**, clear the invoker picker, reselect the same invoker before saving, wait for **Hosting URL** to show the new URL, then save once. Reload the agent in the client and check its iframe URL. The Setup save updates the invoker, while this second save updates the agent assignment. Saving while the picker is empty clears the assignment.
- **Deploy updated extension code or bundled assets.** Upload the new JAR version using **Update Extension** on the catalog entry, then select **Upgrade** on the installed invoker. Wait for the upgrade and verify the installed version, Setup value, and client launch. If the app entry URL changes, refresh the agent assignment as described above. The sample build hashes JavaScript and CSS filenames. Use a cache-bypassing reload when diagnosing a browser that still shows old content. [Extension deployment and configuration](https://github.com/krista-software/extension-development-documentation/blob/main/operations/DeploymentAndConfiguration.md)

## Establish the caller before reading KSDK identity

Studio discovery and client authentication are separate requests. When the assigned topic opens, the Krista client loads the saved app URL in an iframe and sends launch context with `postMessage`. For US production, validate that the sending window is the parent and its origin is `https://us.krista.app`. Confirm which launch fields and credential the target client supplies. Read only the fields your app needs. Derive the caller’s authority on the server.

Use the platform-supported user authentication path for your workspace. If that deployment supplies a Krista access token and supports IAM introspection for this extension, the normal sample implements the following flow using the verified Solution Labs settings. For US production, confirm the target extension and client origins and the IAM endpoint before adapting the settings in the Angular launch entry and the Java authenticator. [Angular launch entry](../ui/src/main.platform.ts) · [Java authenticator](../src/main/java/app/krista/sample/hello/GreetingAuthenticator.java)

1. The iframe accepted the client’s access token and sent it in an `Authorization: Bearer …` header to the same invoker’s `GET /rest/greeting` route. The request stayed on the extension origin. Keep the credential in memory for the request, and keep it out of URLs, persistent browser storage, logs, and diagnostics.
2. The extension’s `RequestAuthenticator` checks the token with the deployment’s trusted Krista IAM introspection endpoint. Confirm the approved endpoint, request and response formats, and reachability from the invoker in US production before sending a launch token. Configure the trusted IAM destination on the server, independently of the launch message.
3. The authenticator required an active access token for the runtime workspace, resolved the verified identity with the workspace-scoped `AccountProvider` and compared the person ID to IAM’s verified subject. It returned the matched account ID for Krista to bind to the request. Reject the invocation if any check fails. In the reflected extension runtime, the callback itself can run with a built-in workspace admin context. Verify the bound caller **inside the greeting resource**, after Krista binds the returned account.
4. The greeting resource read `AuthorizationContext.getAuthorizedAccount().getPerson().getPersonName()` and `Workspace.getWorkspaceName()` from the bound KSDK context. It also checked that the KSDK workspace ID was present. Return only the fields the app needs. Keep identity and authorization scoped to the current request. [KSDK authorization and request authentication](https://github.com/krista-software/extension-development-documentation/blob/main/reference/KsdkReference.md#authentication-and-request-identity) · [KSDK account provider](https://github.com/krista-software/extension-development-documentation/blob/main/reference/KsdkReference.md#accountprovider)

### Verify each part before release

| Check | Expected result |
| --- | --- |
| Direct app entry | HTML, JavaScript and CSS load. A platform build opened directly shows its missing-context state. |
| Studio selection | The invoker appears and Studio fills **Hosting URL** from discovery before the user launches the app. |
| Embedded launch | The client loads the expected iframe URL. The app accepts launch context from the configured parent origin. |
| Authenticated call | Open the agent in a separate non-admin session. Compare the KSDK account and workspace with the signed-in user and test workspace. Record the result without copying any token or raw launch message. |
| Credential-free call | Repeat the greeting request with the launch token and credentials omitted. Expect HTTP 401 and no greeting fields. Studio discovery should still succeed. |
| Changed app URL | Give the app entry a distinguishable new URL, refresh the assignment, and verify the iframe moved to that URL on the same invoker. |

For the profile demo, v1.2.0 also reads role names and primary email from the bound account. The workspace attribute provider supplies the visible definitions, which the extension matches to that same account’s scalar values. The response carries display labels and values for the current user. [Greeting resource](../src/main/java/app/krista/sample/hello/GreetingResource.java)

The Solution Labs run verified installation, discovery, iframe rendering, and URL refresh. After validating the IAM contract with a separate probe, the normal sample was upgraded in place to v1.1.0. The Krista topic rendered the signed-in Studio admin’s KSDK display name and workspace, including after a fresh client reload. Discovery returned HTTP 200 and the greeting returned HTTP 401 without the launch credential. The non-admin acceptance run and verification in a workspace on the US production domains remain pending. [Verification findings](../docs-specs/sample-agent/findings.md) · [Acceptance scenarios](../docs-specs/sample-agent/test-plan.md)

## Troubleshoot by stage

| Symptom | What to check |
| --- | --- |
| Invoker is missing from the agent picker | Confirm the JAR was added to this workspace, **Primary Domain** is **Agent**, and the intended invoker is available in **Live** mode. Refresh Studio if it was open while you installed the extension from another tab. |
| **Hosting URL** stays blank | Check the saved **App URL** and request `/rest/hostUrl` from the routed **Extension Base URL**. A 404 points first to the route or JAX-RS registration. A sample 503 points to missing or invalid Setup. Confirm the response uses `payload.hostingUrl`. |
| Client opens the old app URL | Reopen Studio to verify Setup, then clear and reselect the same invoker in the agent form before saving once. Check the iframe URL after reloading. |
| Direct open works but the iframe is blank | Check browser network and console for the iframe document and its assets. Inspect the framing policy, HTTPS reachability, JavaScript MIME types and relative asset paths. |
| App waits for Krista | Check the actual parent origin, the message source and the expected launch fields on this client version. If the app is opened directly, the waiting or recovery state is expected and the platform build has no development fixture data. |
| Greeting returns HTTP 401 with a token | Trace the first failing stage in the test invoker. Check header delivery, IAM response, workspace membership, subject/account matching and the bound KSDK context. Record only fixed stages and status codes. The probe initially failed because its KSDK account implementation throws on `Account.isBlocked()`. Confirm which SDK accessors your runtime implements before using them. |

For general Setup errors, dependency problems, and support evidence, see the existing extension documentation. [Troubleshooting](https://github.com/krista-software/extension-development-documentation/blob/main/operations/Troubleshooting.md) · [Observability](https://github.com/krista-software/extension-development-documentation/blob/main/operations/Observability.md)
