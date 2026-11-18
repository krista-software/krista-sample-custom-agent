# Specification · Hello Custom Agent

**Version:** 1.0  
**Status:** CTO approved. Awaiting pre-build confirmation.  
**Date:** 2026-11-18  
**Author:** System architect  
**Inputs:** [Validated requirements](requirements-analysis.md) · [Acceptance test plan](test-plan.md) · [Source findings](findings.md) · [UI design](ui-ux-design.md)

## Scope Contract

| Dimension | Value |
| --- | --- |
| Audience & realistic scale | Individual developers running locally and occasional demos in a Krista test workspace. |
| Perf / scale targets | None specified. |
| Security sensitivity | None-new |
| Regression sensitivity | None |
| Expansion tolerance | Minimal |
| Pipeline tier | Light |

Follow Krista’s existing identity and workspace-scoping rules when the platform launches the agent. This is a greenfield sample repo using existing Krista extension and KSDK contracts.

## 1. Outcomes and success

Deliver one developer sample repository containing a normal Krista extension JAR and a small Angular app. Studio installs the JAR in the **Agent** domain and discovers the app entry URL from the installed extension’s Setup attribute. Krista opens the app in its topic iframe and the extension reads the signed-in user and workspace through KSDK. The app shows the display name and workspace name. An explicit developer launch uses the same view with labeled fixture data.

Success is the acceptance matrix in [test-plan.md](test-plan.md#coverage-matrix). The key live proofs are Studio upload and initial assignment, a changed Setup URL taking effect after refreshing the existing assignment, and the greeting originating from the KSDK context of a regular signed-in user. The test plan also verifies the local launch, missing platform context, instructions, and findings.

## 2. Architecture

```text
Studio → native extension JAR → workspace invoker and Setup
     └─ GET /rest/hostUrl ──────→ read saved invoker app URL
        topic assignment ←────── save discovered app URL

Krista topic → iframe → Angular app on existing HTTPS static hosting
                             → proven Krista invocation → extension → KSDK
                             ← display name and workspace name ──────────

Developer → explicit local Angular serve command → same view with fixtures
```

| Component | Responsibility | Precedent |
| --- | --- | --- |
| Java 21, Gradle extension JAR | Agent metadata, one Setup field, hosting discovery, and a read-only greeting operation | KA’s Framework 4 extension and the ordinary Studio JAR flow |
| Angular app | Shared greeting view, explicit standalone and embedded build configurations, and the platform launch adapter | KA’s Angular build configurations and local development mode |
| Existing Krista runtime and deployment services | Invoker and topic routing, scoped caller identity, KSDK data, and the target workspace’s HTTPS asset delivery | Verified on the intended deployment during Task 002 |

Keep the JAR build and UI build commands in the sample repo with their ordinary wrappers or package scripts. Serve the embedded Angular bundle from existing browser-reachable HTTPS static hosting approved for the test environment. Use an existing extension static resource surface if it can serve the entry page and assets and provides the proven invocation origin in that workspace. Deploy one selected shape for the sample. Angular’s development server runs the standalone build locally. The deployed app consists of static assets and the extension operation hosted by Krista. [Framework 4 static resources](/Users/john/Projects/extension-development-documentation/development/Framework4Features.md:45) · [KA Angular build modes](/Users/john/Projects/knowledge-agent/ui/angular.json:83)

## 3. Extension installation and URL discovery

### 3.1 Build and registration

Use the Java 21 and Gradle Framework 4 extension pattern from KA. The version of the SDK, metadata processor, and any container base must be the supported set for the selected test workspace. Generate the extension metadata using the platform annotation processor, include metadata and runtime resources in the native JAR, and inspect the resulting artifact before upload. Match the Krista ecosystem and Agent domain identifiers from the existing extension guidance and workspace. Use the minimum metadata confirmed by the platform for the sample; KA’s additional catalog operations and service registration support its application workflow. [KA revision and build evidence](findings.md#1-upload-an-extension-jar-then-configure-and-assign-its-invoker) · [Framework 4 and container versions](/Users/john/Projects/extension-development-documentation/development/Framework4Features.md:13)

Document one Studio sequence. Upload via **Native Java Jar** under **Krista → Agent**. **Add to Workspace** creates the invoker. Save the URL in the invoker’s Setup and select the invoker in the topic’s Agent Configuration. Wait for discovery and save the topic. Studio derives agent classification from the catalog domain and its picker uses that classification. [Upload and assignment](findings.md#1-upload-an-extension-jar-then-configure-and-assign-its-invoker)

### 3.2 Setup and discovery contract

Declare one required text attribute labeled **App URL** on the extension Setup form. Validate a nonblank, absolute HTTPS URL with a host and use the entered app entry path as saved. Report missing or malformed input through the standard Setup validation mechanism. Read the current invoker’s saved value for each discovery request. Treat successful browser loading and permission to embed the app as test-workspace checks. [Existing Setup attributes](/Users/john/Projects/extension-development-documentation/setup-tab/Attributes.md:24) · [KA request-time discovery evidence](findings.md#2-the-extension-returns-the-app-url-and-studio-saves-it-with-the-topic)

Register a JAX-RS `GET /hostUrl` resource on the extension's default REST path, matching KA’s registered application and the platform-dispatched route. Studio calls `GET <routed invoker base>/rest/hostUrl`. Success is HTTP 200 with JSON in the following shape. A missing or invalid saved URL returns a non-success configuration error and no launch target.

```json
{
  "payload": {
    "hostingUrl": "https://sample.example.com/hello/index.html"
  }
}
```

URL discovery only returns the configured public app address. The normal Studio discovery route and the static app entry must be available before the app obtains an end-user credential. The greeting operation separately requires authenticated request context. If the platform applies the greeting authenticator to the entire REST application, use the proven dispatcher contract for metadata requests and require the verified end-user account at the greeting resource as well. Verify those paths together in Task 002, including behavior before an end-user credential is provided. The inspected processor removes the routed invoker prefix and request identifier before forwarding the local HTTP path; use the observed local route if a callback distinguishes discovery from greeting. [Hosting URL endpoint contract](/Users/john/Projects/extension-development-documentation/custom-agents/HostingUrlEndpoint.md:13) · [Routed HTTP path handling](/Users/john/Projects/kristagrand/extension/requests/processors/http/src/main/java/app/krista/extension/requests/processors/http/InvokerHttpRequestProcessor.java:223)

### 3.3 Refresh an assigned topic

Saving a new App URL changes the invoker Setup value. Studio separately stores the discovered host URL on the topic. The source-backed refresh sequence is to clear the selected invoker in the topic form, reselect the same invoker before saving, wait for discovery, and save once. Saving the cleared form removes the topic’s assignment. Reopen the assigned topic to verify the new iframe URL. Use the observed workspace sequence in the developer instructions. [Studio and platform persistence evidence](findings.md#2-the-extension-returns-the-app-url-and-studio-saves-it-with-the-topic)

## 4. Live greeting and local execution

### 4.1 Confirm the live invocation in the target workspace

Treat the exact authenticated invocation as the engineering follow-up approved in GAP-001. Task 002 establishes the chosen route before Task 004 wires up the launch adapter. A minimal installable sample probe and a regular workspace user are sufficient to establish the boundary. Perform this probe during implementation, then repeat the complete end-to-end check on the final sample for ITS-002.

1. **Inspect the installed launch contract.** Open the test topic as the regular user. Establish the actual parent origin, message shape and readiness sequence, invoker route, and credential type. Verify how the browser reaches the operation from the actual app origin, including any preflight for a cross-origin request. Use deployment-trusted origins, routes, and platform service addresses. The probe must also show that Studio discovery still succeeds and the iframe shell loads before the greeting request authenticates. KA and KLAG show client-side launch context. KLAG records an IAM access token in newer client launches, with earlier clients lacking that field. [Launch context evidence](findings.md#3-keep-the-browser-launch-context-and-ksdk-request-identity-distinct) · [KLAG launch token](</Users/john/Projects/klag/ui/src/app/session/embed-context.service.ts:17>)
2. **Prefer an existing end-user invocation.** Use a route explicitly supported for the deployed Krista client that binds the caller’s authenticated account and workspace to the extension invocation. Trace the route to the greeting operation and inspect the runtime context there. A request reaching an invoker by its routing ID proves delivery only. For the inspected reflected REST path, the platform runs an authentication callback in the invoker workspace’s administrator context while deriving the account, then installs the context for the returned account before handling the API request. KA’s authenticator returns the account already present during that callback. Its pattern cannot establish a regular end-user invocation for this sample. [Routed invoker dispatch](/Users/john/Projects/kristagrand/extension/requests/processors/http/src/main/java/app/krista/extension/requests/processors/http/InvokerHttpRequestProcessor.java:74) · [Callback and context installation](/Users/john/Projects/kristagrand/extension/executor/reflect/src/main/java/app/krista/extension/executor/reflect/ReflectRequestAuthenticator.java:48) · [KA callback source](findings.md#3-keep-the-browser-launch-context-and-ksdk-request-identity-distinct)
3. **Use existing IAM introspection if the target supports this branch.** If no route already binds the user at entry and the deployed client supplies the regular user’s IAM access token, probe the extension authenticator with the platform’s existing access-token introspection at `POST /auth/v1/introspect`. Use its supported JSON contract and a trusted deployment endpoint accessible to the invoker. Confirm the issuer environment, token type, route permission, and end-user binding in the test workspace before adopting it. The validator checks token validity and revocation and returns the verified person ID, username, and workspace memberships. Require an active result containing the runtime invoker workspace. Look up the returned username through the workspace-scoped KSDK account provider, compare the matched account’s person ID to the verified subject, and return the matched account ID to the platform authenticator. If validation, workspace membership, or account match fails, leave the invocation unauthorized. This option uses Krista as the identity authority. The app supplies only the observed supported launch credential, and the extension lets Krista install the resulting request context. [IAM endpoint](/Users/john/Projects/kristagrand/iam/rest/src/main/java/com/krista/iam/rest/api/resources/AuthenticationResource.java:812) · [Introspection validation](/Users/john/Projects/kristagrand/iam/rest/src/main/java/com/krista/iam/rest/api/service/TokenIntrospectionService.java:40) · [Subject and workspace contract](/Users/john/Projects/kristagrand/iam/rest/src/main/java/com/krista/iam/rest/api/rto/RtoIntrospectTokenResponse.java:9) · [Workspace-scoped lookup](/Users/john/Projects/kristagrand/servers/src/main/java/com/krista/services/ksdk/accounts/AccountStoreImpl.java:121)
4. **Decide from live results.** A candidate passes only when the real browser launch reaches the installed extension and the greeting handler receives the expected regular user and workspace in Krista’s context. Also call that same greeting operation without the approved end-user credential and verify a non-success response with neither greeting field. Confirm normal Studio discovery succeeds before the app acquires that credential. Repeat these checks in ITS-001 and ITS-002 for the completed sample. Record the actual route, credential type, origin pairing, verification method, and result in `findings.md`. A launch hint, directory lookup on a browser-supplied identifier, or a service-account invocation cannot establish the required user binding. If both paths fail, record FR-004 and ITS-002 as blocked and report the failed platform prerequisite before building live wiring around it.

### 4.2 Read only the greeting data

Once Krista authenticates the request, the extension operation reads the current account from the injected KSDK `AuthorizationContext`, then reads the person’s display name from that account. It reads workspace ID and name from the same request’s bound KSDK `Workspace`. The workspace ID identifies the read scope for the teaching example and live proof. The response returns only the two display strings below. Account, role, and workspace identifiers stay outside the greeting response.

```json
{
  "displayName": "Morgan",
  "workspaceName": "Demo Team"
}
```

The operation has no identity input body. Use the request path and response envelope of the accepted Krista invocation. If the probe proves the routed JAX-RS shape, use read-only `GET <routed invoker base>/rest/greeting` and the JSON object above on success. If the supported platform route uses its own invocation wrapper, return the same two fields through that wrapper and document the verified route. Deliver a non-success response for missing authenticated context or unavailable KSDK greeting data. The app displays the launch state or recovery message in the UI design and renders the greeting on success. The extension holds no user or workspace results beyond the request. [Account person record](/Users/john/Projects/kristagrand/servers/src/main/java/com/krista/services/AuthorizationContextFactory.java:86) · [KSDK workspace methods](/Users/john/Projects/kristagrand/extension/runtime/plugins/ksdk/src/main/java/app/krista/extension/runtime/plugins/ksdk/impl/WorkspaceImpl.java:80)

### 4.3 Angular launch and view behavior

Use one greeting view with a heading and labeled workspace name. The platform launch adapter waits for supported launch context from the expected parent origin, invokes the verified route, and shows the resulting names. A missing launch context or failed request maps to the waiting or error state in [ui-ux-design.md](ui-ux-design.md). Recovery is reopening the existing Krista topic.

The documented standalone command selects the fixture build configuration explicitly and runs the Angular development server. It supplies a named sample user and workspace and displays **Local sample data**. The platform build selects only the live path. Iframe detection may support presentation and missing-context messaging; it does not select the greeting’s data source. [KA fixture mode evidence](findings.md#4-standalone-mode-exercises-the-app-with-explicit-dev-data)

## 5. Data and operational boundaries

- **Ownership and scope.** Krista owns the invoker attribute, topic assignment, and authenticated user and workspace data. The sample persists no app data. The platform runtime supplies workspace scope for each invocation, including any identity mapping inside the authenticator. Multiple instances can handle requests independently. Local fixtures have an explicit developer scope and are available in the standalone run only.
- **Existing security protections.** Use existing platform authentication and tenant scope. Use the deployment’s approved HTTPS hosting and framing/origin policy, and keep credentials in the current launch/request only. Documentation and diagnostic evidence use credential type and account/workspace match results without exposing tokens or personal identifiers.
- **Operations.** The developer instructions cover build tools and artifact preparation, both launch paths, normal Studio registration, and first-time Setup and URL refresh. Include the verified request shape and KSDK code locations. Capture source observations, any corrected guide steps, and sanitized results from the test workspace in [findings.md](findings.md).
- **Performance, migration, and bespoke observability.** N/A per the Scope Contract.

## 6. Implementation plan

| Phase | Task | Work | Depends on | Acceptance |
| --- | --- | --- | --- | --- |
| 1 · Extension and route proof | TASK-001 | Set up the sample build and minimal JAR, generated Agent metadata, App URL Setup, discovery resource, and hosted app shell. | Target workspace and supported build inputs | AC-002-1, AC-003-1 |
| 1 · Extension and route proof | TASK-002 | Install the minimal sample. Execute the bounded route probe in §4.1 and record the selected invocation or blocked prerequisite. Verify discovery, metadata and framing through Studio. | TASK-001, test installer and regular user, approved static host | ITS-001, prerequisites for ITS-002 |
| 2 · Shared view and live reads | TASK-003 | Build the Angular greeting and standalone fixture launch with the documented local command. | Angular build inputs | AC-001-1, AC-005-1 |
| 2 · Shared view and live reads | TASK-004 | Wire the proved invocation and the live KSDK greeting into the platform build. Implement the pending and error states. | TASK-002 passed, TASK-003 | AC-004-1, AC-005-2 |
| 3 · Verification and documentation | TASK-005 | Complete the sample instructions, execute the six scenarios including the URL refresh and regular-user KSDK proof, and update findings with the results. | TASK-001 through TASK-004 | All criteria in [test-plan.md](test-plan.md#coverage-matrix) |

## Decisions

**Level of effort:** Days, estimated two to four engineer-days for one engineer with workspace, build artifact, and hosting access in place. The route probe is sized at hours.  
**Clearance:** NOT CLEARED — 1 open. CTO review is approved. [Pre-build confirmation](pre-build-summary.md) awaits the user’s decision.

| ID | Engineering decision | Rationale and precedent |
| --- | --- | --- |
| E-001 | Standard JAR and platform URL discovery. | This reproduces the actual Studio path verified in source and uses the same extension seam as KA. GAP-002 and GAP-003 record the owner’s approval. |
| E-002 | One Angular greeting view, with explicit development and platform builds. | KA’s Angular build configuration supplies the mode separation. Existing HTTPS hosting or the proved extension static surface supplies the deployed assets. This keeps the local run self-contained and the live path tied to Krista. |
| E-003 | Prove one existing authenticated invocation and validate the request context before wiring the live app. | The reflected callback can run as workspace admin while resolving identity. The existing IAM introspection contract supplies a candidate verified subject if supported, and the regular-user workspace check established in GAP-001 decides whether the route is usable. |

### Engineering follow-up

The exact browser launch envelope, target invocation route, and compatible minimum JAR metadata remain to be verified. The implementation owner closes these in TASK-002 using the real test workspace. Failure to establish current-user binding blocks the live greeting and its acceptance test. Implementation of the standalone view, source documentation, and supported extension packaging can continue independently.

## References

- [Validated requirements and owner decisions](requirements-analysis.md)
- [Acceptance scenario setup, evidence, and cleanup](test-plan.md)
- [Shared greeting view and failure messages](ui-ux-design.md)
- [KA, Studio, and platform source revisions](findings.md)
