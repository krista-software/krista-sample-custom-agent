# Requirements Analysis — Round 1 (Sample Custom Agent)

Produced by `refine-requirements` on 2026-11-17 from the inline project request and Round 0 confirmation. Status: ANSWERED 2026-11-18 (all five decisions confirmed during the guided walk).

The five `Decision:` entries record the confirmed requirements and the evidence from the guided walk. The approved scope is recorded in the [validated requirements analysis](requirements-analysis.md).

## Scope Contract (locked 2026-11-17)

| Dimension | Value |
| --- | --- |
| Audience & realistic scale | Individual developers running locally and occasional demos in a Krista test workspace. |
| Perf / scale targets | None specified. |
| Security sensitivity | None-new. Follow Krista’s existing identity and workspace-scoping rules when the platform launches the agent. |
| Regression sensitivity | None. Greenfield sample repo using existing Krista extension and KSDK contracts. |
| Expansion tolerance | Minimal. |
| Pipeline tier | Light. |

## Restated Requirements

| ID | Original text | Interpretation | Category | Testable |
| --- | --- | --- | --- | --- |
| FR-001 | “I want to create a \"Hello, \<user>\" sample custom agent.” | Deliver a runnable custom agent that greets the user by name in its app UI. | Core | Yes |
| FR-002 | “shows how registration into our platform works (you upload it no differently than other extensions today)” | Supply an extension artifact for normal Krista upload and show how its configured workspace instance becomes available to launch as an agent. [Upload and assignment evidence](findings.md#1-upload-an-extension-jar-then-configure-and-assign-its-invoker) | Core | Yes |
| FR-003 | “how our extension provides that URL” and “we will need to make an extension setup attribute for the URL to the running agent UI / App.” | A workspace installer sets the app URL in the extension’s Setup tab. The extension supplies that configured URL to Krista for launching the app. [URL evidence](findings.md#2-the-extension-returns-the-app-url-and-studio-saves-it-with-the-topic) | Core | Yes |
| FR-004 | “how to fetch existing user and workspace info. We should execute a number of `KSDK` calls to show our developer how our existing agents like `KA` does this.” | Demonstrate real Krista Software Development Kit reads for the current user and workspace in the platform-launched mode. [KSDK evidence](findings.md#3-keep-the-browser-launch-context-and-ksdk-request-identity-distinct) | Core | Yes |
| FR-005 | “we can run the custom agent locally, outside of Krista, and run it via the production mode where Krista invokes it. We should provide that mode here as well and document it” | Provide and document standalone local execution and embedded execution launched by Krista. [Mode evidence](findings.md#4-standalone-mode-exercises-the-app-with-explicit-dev-data) | Core | Yes |
| FR-006 | “create a `findings.md` document along the way so we can capture the important details you had to learn from the KA project and from trial and error.” | Maintain a source-backed findings log and add results from later build and runtime trials for use in the extension docs. | Secondary | Yes |
| CON-001 | “This sample application will end up in its own repo adjacent to this documentation repo.” | Deliver the sample in `sample-custom-agent`, alongside the reference checkout `extension-development-documentation`. | Core | Yes |

## Defaults Applied Silently

Round artifacts live at the sample repo root. The draft uses “Hello” as the sample’s working name.

## Gaps (5 total)

### CRITICAL

#### GAP-001. Establish the signed-in identity for the embedded data request

**What’s unclear.** How will the hosted app’s embedded launch reach an extension request running as the user who opened it? The KA UI receives a `postMessage` from the parent window, extracts workspace and account context, and forwards it to its backend (`knowledge-agent/ui/src/app/core/services/auth.service.ts:361–450`, `ui/src/app/core/interceptors/auth.interceptor.ts:87–107`). On a cache miss, KA’s backend finds the callback previously registered for that workspace and requests `/rest/session/validate` (`knowledge-agent/core/src/main/java/app/krista/knowledgeagent/api/service/SessionValidationService.java:233–246,355–374`). The callback in the catalog extension (`krista-global-catalog@d2ca488844`, `knowledge-agent/src/main/java/app/krista/global/knowledgeagent/api/SessionResource.java:43–77`) supplies the incoming account identifier to `AccountProvider.getAccount`, the account lookup API. The platform implementation requires an authenticated request and loads the specified account within the request workspace. [Account provider](/Users/john/Projects/kristagrand/extension/runtime/plugins/ksdk/src/main/java/app/krista/extension/runtime/plugins/ksdk/impl/AccountProviderImpl.java:30) · [Workspace-scoped account store](/Users/john/Projects/kristagrand/servers/src/main/java/com/krista/services/ksdk/accounts/AccountStoreImpl.java:104)

**Why it matters.** The sample has to demonstrate that its greeting belongs to the signed-in Krista user. The received account and workspace fields describe launch context. The callback shows how to look up a record, and its use in KA does not establish which end-user credentials reach a sample request.

**Going with.** Start from the verified launch context and use an existing authenticated Krista invocation that supplies the current user and workspace to the extension. After that request reaches the extension and before the reply is rendered, read the signed-in account from `AuthorizationContext` and the workspace from the injected KSDK `Workspace` object. Verify that invocation and its actual credentials in the target test workspace before finalizing the live wiring. KA’s authenticator reads the authorized account at catalog `knowledge-agent/src/main/java/app/krista/global/knowledgeagent/KnowledgeAgentAuthenticator.java:36–39`. The callback’s caller and lifecycle differ from a hosted iframe’s own request.

**Alternative.** Use a KA-style app backend that calls its workspace’s extension through an established platform route, if that route can establish the same signed-in identity end to end.

Decision: Agreed with clarification. Use the authenticated Krista context for both the user and workspace. Verify the request route by opening the agent as a regular user in a Krista test workspace and confirming that the greeting and workspace come from that user's invocation. KA initializes its UI from the parent message and then calls its backend for authoritative roles (`knowledge-agent/ui/src/app/core/services/auth.service.ts:406–450,465–477`). The KA callback is a workspace-scoped account lookup, while the platform can also resolve a built-in administrator context (`kristagrand/extension/runtime/plugins/ksdk/src/main/java/app/krista/extension/runtime/plugins/ksdk/impl/AccountProviderImpl.java:30–34`, `kristagrand/servers/src/main/java/com/krista/services/accesspoint/service/AuthorizationContextProviderImpl.java:65–89,95–111`). A successful lookup alone cannot establish that the extension was invoked as the regular user. The exact request route remains an engineering follow-up. Apply this decision to the KSDK reads in GAP-004.

### IMPORTANT

#### GAP-002. Pick the artifact and make registration reproducible

**What’s unclear.** The custom-agent docs point to a `.daz` archive, while the ordinary extension UI uploads a JAR. KA’s extension also assembles a JAR and documents the normal upload flow (`krista-global-catalog@d2ca488844`, `knowledge-agent/build.gradle:58–60,118–134`, `knowledge-agent/README.md:63–70`). The target workspace’s exact upload labels and minimum metadata for a small agent still need verification. [Custom-agent packaging docs](/Users/john/Projects/extension-development-documentation/custom-agents/PackagingAndDeployment.md:9) · [Studio extension upload](/Users/john/Projects/webmono/projects/StudioNgNew/src/app/modules/catalog/extensions/list/create-new-extension-dialog/create-new-extension-dialog.html:17)

**Why it matters.** An example built for the archive workflow cannot teach developers to upload and assign a custom agent in the ordinary extension flow they already use.

**Going with.** Use Studio’s **Native Java Jar** workflow with the Krista ecosystem and Agent domain. Teach the order developers follow in the workspace. After upload saves the catalog entry, **Add to Workspace** creates an invoker, the workspace’s configured instance of the extension. The developer saves its Setup URL before selecting it under the topic’s Agent Configuration. Verify the workflow once on the target test workspace and confirm what the JAR needs to be recognized there. KA is a precedent at the same extension upload and configuration tier. [Add to Workspace action](/Users/john/Projects/webmono/projects/StudioNgNew/src/app/modules/catalog/extensions/details/catalog-extension-detail.component.ts:172) · [Agent type assignment](/Users/john/Projects/kristagrand/servers/src/main/java/com/krista/services/defn/fn/helper/InvokerWithHintValuesFactory.java:310)

**Alternative.** If the intended test workspace uses a different supported extension upload workflow, use that workspace’s ordinary path as the reference and document where the steps differ.

Decision: Agreed. Build a standard extension JAR and verify the documented workflow in the test workspace. Upload it through Native Java Jar under Krista → Agent, add it to the workspace, save the app URL in Setup, and assign the invoker to a topic. Verify the minimum JAR metadata in that run. Studio stores the binary in the selected catalog domain, and the platform creates the workspace invoker and its route from that catalog entry (`webmono/projects/StudioNgNew/src/app/modules/catalog/extensions/list/create-new-extension-dialog/create-new-extension-dialog.ts:83–123`, `kristagrand/servers/src/main/java/com/krista/services/defn/fn/FnAddExtensionToWorkspace.java:72–91,105–127,138–151`). The platform uses the catalog entry's domain to classify the invoker (`kristagrand/servers/src/main/java/com/krista/services/defn/fn/helper/InvokerWithHintValuesFactory.java:310–332`). Correction to the evidence in `findings.md`: the inspected `.daz` picker belongs to Import Catalog on an existing catalog provider (`webmono/projects/StudioNgNew/src/app/modules/editors/workspace/content/tabs/6-setup/content/5-catalog-providers/create/ws-catalog-providers-create.component.html:12–19` and companion `.ts:23–25,147–156`). GAP-003 covers URL refresh after assignment.

#### GAP-003. Define how Setup URL changes take effect

**What’s unclear.** Is changing the URL part of what the sample should demonstrate, or is first-time setup enough? When the selected invoker changes, Studio fetches `/rest/hostUrl`, the discovery endpoint on the extension’s routed address, and reads `payload.hostingUrl`. After discovery it saves the returned app URL on the topic with the invoker routing address. When the selected invoker ID stays the same, Studio keeps the previous value for the topic. [Discovery request](/Users/john/Projects/webmono/projects/StudioNgNew/src/app/services/core/http/apis/agentSetup/ReqGetHostingUrl.ts:5) · [Studio selection and save](/Users/john/Projects/webmono/projects/StudioNgNew/src/app/modules/editors/workspace/content/tabs/2-channels/details/ws-channel-agentic-guide/ws-channel-agentic-guide.component.ts:284) · [Platform persistence](/Users/john/Projects/kristagrand/servers/src/main/java/com/krista/services/defn/fn/FnSetAgentSetup.java:152)

**Why it matters.** A developer can successfully save a new Setup URL and still open the previous app from a topic that already has an assignment.

**Going with.** Treat the Setup value as the full HTTPS app entry URL reachable from the browser. Teach a stable URL for first-time setup and document how to refresh an existing topic assignment after a change. Source suggests clear the invoker selection, reselect, wait for discovery, then save the topic. Verify those steps in Krista before documenting them as proven. KA reads the current invoker’s saved value in catalog `knowledge-agent/src/main/java/app/krista/global/knowledgeagent/api/HostingResource.java:25–38`. This is the same discovery call used by the sample. [Studio selection controls](/Users/john/Projects/webmono/projects/StudioNgNew/src/app/modules/editors/workspace/content/tabs/2-channels/details/ws-channel-agent-configuration/ws-channel-agent-configuration.component.ts:70)

**Alternative.** Teach only first-time setup against a stable URL and record the existing platform behavior for changes as a documentation finding.

Decision: Agreed. Teach first-time setup with a stable HTTPS app entry URL, verify URL refresh in the Krista test workspace, and document the verified refresh steps alongside initial setup. The extension Setup save persists the invoker's attributes (`kristagrand/servers/src/main/java/com/krista/services/defn/fn/FnInvokerUpdateSetup.java:140–181,236–237`), and the topic save separately persists the discovered app URL (`FnSetAgentSetup.java:152–159,186–189` in the same package). Second-pass clarification: clear the invoker selection in the Studio form, reselect it before saving, wait for URL discovery to finish, then save once and reopen the topic to verify the new target. Saving while the selection is cleared removes the assignment (`webmono/projects/StudioNgNew/src/app/modules/editors/workspace/content/tabs/2-channels/details/ws-channel-agent-configuration/ws-channel-agent-configuration.component.ts:70–89,159–173`, sibling `ws-channel-agentic-guide/ws-channel-agentic-guide.component.ts:284–304,338–394`, `kristagrand/servers/src/main/java/com/krista/services/defn/fn/FnSetAgentSetup.java:152–159`). The sequence is supported by source inspection and awaits the live verification. This follows installation and first-time assignment from GAP-002.

#### GAP-004. Choose the user and workspace fields that the sample teaches

**What’s unclear.** “User and workspace info” could describe the signed-in person and active workspace or include other fields and workspace members. KA’s user handler reads the display name from `Account.getPerson().getPersonName()` while listing all workspace users for a separate workflow (`krista-global-catalog@d2ca488844`, `knowledge-agent/src/main/java/app/krista/global/knowledgeagent/service/GetWorkspaceHandler.java:58–68,89–109`). KA’s service registration uses `Workspace.getWorkspaceId()` and `Workspace.getWorkspaceName()` in the same catalog revision (`KnowledgeAgentExtension.java:315–339` in the `app.krista.global.knowledgeagent` package). [Platform workspace methods](/Users/john/Projects/kristagrand/extension/runtime/plugins/ksdk/src/main/java/app/krista/extension/runtime/plugins/ksdk/impl/WorkspaceImpl.java:80)

**Why it matters.** The fields determine the live KSDK example and what developers see working. The purpose of KA’s workspace directory read is different from establishing the identity of one user in a greeting.

**Going with.** Read the current account via `AuthorizationContext.getAuthorizedAccount()` and its display name via the person accessor. Read the bound workspace ID and name from `Workspace`. Show `Hello, <display name>` and the workspace name. Document where the workspace ID comes from as part of explaining the KSDK calls. Make these reads after the authenticated request arrives and before returning the view data.

**Alternative.** Include additional fields for the current user, such as email or roles, if they are part of what you want developers to learn from the greeting sample.

Decision: Agreed. Show `Hello, <display name>` and the workspace name as the two visible fields. Read the person name from the authenticated KSDK account and show in the documentation how the workspace name and ID come from the bound KSDK workspace. Second-pass clarification: Krista constructs the authenticated account with a person record containing the display name, so the sample can read it directly from that account (`kristagrand/servers/src/main/java/com/krista/services/AuthorizationContextFactory.java:86–94`, `kristagrand/extension/runtime/plugins/ksdk/src/main/java/app/krista/extension/runtime/plugins/ksdk/impl/AccountImpl.java:66–69`, `PersonImpl.java:22–25` and `WorkspaceImpl.java:80–90` in the same package). KA's browser derives its initial name from the launch message or the email, while its directory handler demonstrates the KSDK name accessor (`knowledge-agent/ui/src/app/core/services/auth.service.ts:430–449`, `krista-global-catalog@d2ca488844:knowledge-agent/src/main/java/app/krista/global/knowledgeagent/service/GetWorkspaceHandler.java:89–99`). Follow GAP-001 for authenticated invocation and the regular-user check.

#### GAP-005. Decide what developers can exercise outside Krista

**What’s unclear.** Does “locally, outside of Krista” mean the app can run with fixture data, or should a locally running app also connect to live Krista accounts? KA uses an explicit standalone environment and initializes a development identity in that mode. In embedded mode it waits for platform context. The adjacent Krista Language Agent (KLAG) requirements accept fixture personas for isolated development and keep fixture controls out of deployed mode. [KA mode selection](/Users/john/Projects/knowledge-agent/ui/src/app/core/services/auth.service.ts:99) · [KLAG local precedent](/Users/john/Projects/klag/docs-specs/v0.0.1/requirements-analysis.md:44) · [KLAG live-mode precedent](/Users/john/Projects/klag/docs-specs/to-1.0.0/requirements-analysis.md:107)

**Why it matters.** Local rendering with fixtures has a different setup and proves different behavior from a live KSDK call. Developers need to know which run requires a workspace and which run they can use to iterate on the app immediately.

**Going with.** Provide an explicit standalone developer launch with a clearly labeled fixture user and workspace. Use the same greeting UI in both modes. The Krista-launched run exercises the live extension and KSDK reads. In platform mode, missing launch context leaves the app waiting or gives a clear error. Treat fixture workspace identity as explicit local input and keep the live path workspace-scoped across instances.

**Alternative.** Make local execution depend on a configured test workspace and authenticated access to a live extension, if live reads from a locally hosted process are essential to the developer workflow.

Decision: Agreed. Provide an explicit standalone developer command that renders the same greeting with a clearly labeled fixture user and workspace, without a Krista login. Keep real KSDK reads in the Krista-launched mode. Show a waiting or error state when that mode lacks launch context. Document what each command or launch exercises. Second-pass clarification: KA selects its development or production environment at build time and gates fixture identity on the standalone development configuration, while a distinct iframe check drives layout and loading (`knowledge-agent/ui/angular.json:83–98,123–146`, `ui/src/app/core/services/auth.service.ts:99–128,316–332`, `ui/src/app/app.component.ts:24–37`). KA's backend handles development identity separately from the live session path (`knowledge-agent/core/src/main/java/app/krista/knowledgeagent/api/filter/TenantContextFilter.java:196–212`). `klag/docs-specs/to-1.0.0/requirements-analysis.md:107–111` gives adjacent precedent for isolating fixture controls from live mode. Follow GAP-001 for live identity and GAP-004 for the shared visible fields.

## Requirements I’d Challenge

None.

## Proposed Scope Expansions

None.

## Design Hints vs Hard Constraints

**Hard constraints.** Standard extension upload, a saved Setup attribute for the app URL, the platform’s discovery and launch contracts, real KSDK reads using Krista identity and workspace scope, a standalone local run, documentation for both modes, and the findings log in the separate sample checkout.

**Design hints.** Knowledge Agent is the reference for extension and app lifecycle. Its workspace directory, service registration and callback machinery exist to support KA’s knowledge workflows. Borrow the behaviors that explain the greeting sample’s public lifecycle. Choose the smallest existing authenticated invocation that the target workspace supports.

## Codebase Reality Check

- **Registration takes three steps after build.** Catalog upload creates the extension entry, **Add to Workspace** creates the invoker, and topic Agent Configuration saves where the app will launch. The picker uses the platform’s Agent classification. [JAR upload](/Users/john/Projects/webmono/projects/StudioNgNew/src/app/modules/catalog/extensions/list/create-new-extension-dialog/create-new-extension-dialog.ts:83) · [Agent picker](/Users/john/Projects/webmono/projects/StudioNgNew/src/app/modules/editors/workspace/content/tabs/2-channels/details/ws-channel-agent-configuration/ws-channel-agent-configuration.component.ts:105) · [Topic save](/Users/john/Projects/webmono/projects/StudioNgNew/src/app/modules/editors/workspace/content/tabs/2-channels/details/ws-channel-agentic-guide/ws-channel-agentic-guide.component.ts:384)
- **The configured URL is already a KA Setup field.** KA’s extension exposes `serviceUrl` through `/rest/hostUrl`. The sample’s difference is that the developer supplies the value manually. KA separately registers its own callback with its backend. That registration follows routing and workspace resolution and feeds KA’s backend callback lookup. [Source revisions and citations](findings.md#2-the-extension-returns-the-app-url-and-studio-saves-it-with-the-topic)
- **Existing specifications give precedent for pieces of the flow.** KA’s auth requirements describe moving its callback onto the enterprise service bus, while the inspected implementation still issues a REST request. KLAG documents and isolates local fixtures, and its iframe receiver shows variation in how clients send account and workspace context. These sources inform the boundaries. The test workspace must still settle the sample’s actual launch context and authenticated call path. [KA proposed callback change](/Users/john/Projects/knowledge-agent/docs-specs/platform-auth-and-librarian-roles/requirements-analysis.md:125) · [KA actual callback](/Users/john/Projects/knowledge-agent/core/src/main/java/app/krista/knowledgeagent/api/service/SessionValidationService.java:355) · [KLAG client context variants](/Users/john/Projects/klag/ui/src/app/session/embed-context.service.ts:176)
