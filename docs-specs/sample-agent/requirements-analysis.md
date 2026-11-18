# Requirements Analysis

**STATUS: VALIDATED**  
**Validated Date:** 2026-11-18  
**Rounds of Refinement:** 1  
**Source:** Inline project request recorded in the [Round 1 analysis](round-1-analysis.md#restated-requirements).

## Scope Contract (BINDING)

| Dimension | Value |
| --- | --- |
| Audience & realistic scale | Individual developers running locally and occasional demos in a Krista test workspace. |
| Perf / scale targets | None specified. |
| Security sensitivity | None-new |
| Regression sensitivity | None |
| Expansion tolerance | Minimal |
| Pipeline tier | Light |

Follow Krista’s existing identity and workspace-scoping rules when the platform launches the agent. This is a greenfield sample repo using existing Krista extension and KSDK contracts.

**Pipeline tier legend**

- **Light** uses one post-build review at medium effort and one remediation iteration. For this sample the review is the independent project review.
- **Standard** uses both post-build reviews at high effort and one remediation iteration.
- **Critical** uses both post-build reviews at xhigh effort and up to two remediation iterations.

**Regression sensitivity legend**

- **None** describes a greenfield project and skips regression impact analysis.
- **Intentional-Break** runs forward-looking regression analysis and treats authorized compatibility and migration breaks as intentional.
- **Standard** runs full regression analysis for existing behavior, contracts, and data.
- **Critical** runs exhaustive regression analysis for core dependencies and requires lossless, reversible data migrations.

Downstream specification, implementation, test, and review work must use this contract to assess findings. A ship-blocker must be in scope and be an observed failure or a clear defect against a stated requirement. Work outside the contract belongs in a deferred backlog. Regression findings follow the declared regression sensitivity. Apply review effort through the shared Codex runtime.

## Restated Requirements

### Functional Requirements

#### FR-001. Render the greeting

**Original Text:** “I want to create a \"Hello, \<user>\" sample custom agent.”  
**Interpretation:** Deliver a runnable custom agent whose UI displays `Hello, <display name>` and the workspace name. Use the same greeting UI in both launch modes.  
**Category:** Core  
**Testable:** Yes  
**Validation Notes:** GAP-004 confirmed the two visible fields. GAP-005 confirmed that standalone mode uses labeled fixture data to render the same UI. In live mode the names come from FR-004.

#### FR-002. Upload and register the extension through Studio

**Original Text:** “shows how registration into our platform works (you upload it no differently than other extensions today)”  
**Interpretation:** Produce a standard Krista extension JAR. Document and verify the normal Studio flow in a Krista test workspace. Upload through **Native Java Jar** under **Krista → Agent**, use **Add to Workspace** to create an invoker, save the app URL in that invoker’s Setup tab, and assign the invoker in the topic’s Agent Configuration. Show how a developer opens the resulting topic.  
**Category:** Core  
**Testable:** Yes  
**Validation Notes:** GAP-002 confirmed the JAR workflow and the need to verify minimum metadata in the target workspace. Studio’s `.daz` picker belongs to catalog import. See [upload and assignment evidence](findings.md#1-upload-an-extension-jar-then-configure-and-assign-its-invoker).

#### FR-003. Discover the configured app URL

**Original Text:** “how our extension provides that URL” and “we will need to make an extension setup attribute for the URL to the running agent UI / App.”  
**Interpretation:** A developer enters a stable, browser-reachable HTTPS app entry URL as an invoker Setup attribute. The extension returns the saved value through `GET <routed invoker base>/rest/hostUrl`, using the platform’s `payload.hostingUrl` response field. The topic uses the URL Studio discovers and saves during assignment. Document initial Setup and the verified procedure for refreshing an existing assignment after the URL changes.  
**Category:** Core  
**Testable:** Yes  
**Validation Notes:** GAP-003 confirmed live refresh verification. Source shows that clearing the selection in the form, reselecting the invoker, waiting for discovery, and saving once should refresh the topic URL. Saving while the selection is cleared removes the assignment. See [URL evidence](findings.md#2-the-extension-returns-the-app-url-and-studio-saves-it-with-the-topic).

#### FR-004. Read the signed-in user and workspace through KSDK

**Original Text:** “how to fetch existing user and workspace info. We should execute a number of `KSDK` calls to show our developer how our existing agents like `KA` does this.”  
**Interpretation:** In Krista-launched mode, use the platform-authenticated user and workspace context supplied to the extension. Read the current account from the Krista Software Development Kit (KSDK) authorization context and the display name from that account’s person record. Use the scoped KSDK workspace for its ID and name. Document where those values come from and verify the live request path as a regular user in a Krista test workspace.  
**Category:** Core  
**Testable:** Yes  
**Validation Notes:** GAP-001 confirmed the authenticated request boundary and regular-user verification. GAP-004 confirmed that the signed-in account already contains the person’s display name and that the UI shows the workspace name. The workspace ID demonstrates the request scope in the developer documentation. See [KSDK evidence](findings.md#3-keep-the-browser-launch-context-and-ksdk-request-identity-distinct).

#### FR-005. Run standalone and through a Krista launch

**Original Text:** “we can run the custom agent locally, outside of Krista, and run it via the production mode where Krista invokes it. We should provide that mode here as well and document it”  
**Interpretation:** Provide an explicit local command that renders the greeting with a clearly labeled fixture user and workspace, without a Krista login. In platform mode, Krista opens the same UI and the extension supplies live KSDK data. If launch context is missing in platform mode, show a waiting or error state. Document how to start each mode and what it exercises.  
**Category:** Core  
**Testable:** Yes  
**Validation Notes:** GAP-005 confirmed that fixture data is selected by the standalone launch. Iframe detection is separate from mode selection in the KA precedent. See [launch mode evidence](findings.md#4-standalone-mode-exercises-the-app-with-explicit-dev-data).

#### FR-006. Maintain findings for the extension documentation

**Original Text:** “create a `findings.md` document along the way so we can capture the important details you had to learn from the KA project and from trial and error.”  
**Interpretation:** Maintain [findings.md](findings.md) in this repo. Capture the relevant source evidence, discrepancies in the current extension guide, and observations from building, installing, and trying both modes. Identify which observations came from a live run so the extension documentation team can reuse the findings.  
**Category:** Secondary, required  
**Testable:** Yes  
**Validation Notes:** The initial findings and source revisions are recorded. Add implementation and test-workspace observations as they are obtained.

### Non-Functional Requirements

No additional non-functional requirements were agreed. The Scope Contract defines the applicable security posture and the absence of performance targets.

### Constraints

#### CON-001. Separate sample repository

**Source:** “This sample application will end up in its own repo adjacent to this documentation repo.”  
**Description:** Deliver the sample, its developer instructions, and its findings in `sample-custom-agent`, alongside `extension-development-documentation`. The documentation repo is reference material for the sample and the eventual consumer of the findings.

## Design Analysis

### Hard Constraints

| ID | Constraint | Rationale | Source |
| --- | --- | --- | --- |
| HC-001 | Use the standard extension JAR upload and Agent domain assignment. | The sample teaches the ordinary Studio installation path. | FR-002, GAP-002 |
| HC-002 | Return the manually saved invoker URL through the platform discovery contract. | The installer configures the app address in Setup and Studio uses the resulting topic assignment. | FR-003, GAP-003 |
| HC-003 | Use Krista’s authenticated context for live KSDK reads. | The live greeting demonstrates the signed-in user and workspace. | Scope Contract, FR-004, GAP-001, GAP-004 |
| HC-004 | Provide a standalone launch with explicit fixture data and a Krista launch with live data. | Developers can iterate locally and verify real integration in Krista. | FR-005, GAP-005 |
| HC-005 | Keep the sample, instructions, and findings in the adjacent sample repo. | Developers get a runnable reference, and the docs team gets a record of what was learned. | FR-005, FR-006, CON-001 |

### Design Hints

| ID | Hint from requirements | Assessment | Decision |
| --- | --- | --- | --- |
| DH-001 | “our existing agents like `KA`” | Knowledge Agent demonstrates metadata, URL discovery, KSDK access, and separate launch modes. Its app registration and directory lookup serve KA-specific workflows. | Use KA as the precedent for the corresponding platform seams. Select the smallest existing authenticated invocation supported by the target workspace. |
| DH-002 | “Our deployment automation does this for KA.” | KA’s extension reads a saved Setup value at discovery time. | Expose the saved field directly to the developer installing the sample. |

### Requirements Challenged and Resolved

None.

## Resolved Gaps

### Critical Gaps

#### GAP-001. Establish the signed-in identity for the embedded data request

**Original Question:** How will the hosted app’s embedded launch reach an extension request running as the user who opened it?

**Resolution:** Agreed with clarification. Use the authenticated Krista context for both the user and workspace. Verify the request route by opening the agent as a regular user in a Krista test workspace and confirming that the greeting and workspace come from that user's invocation. KA initializes its UI from the parent message and then calls its backend for authoritative roles (`knowledge-agent/ui/src/app/core/services/auth.service.ts:406–450,465–477`). The KA callback is a workspace-scoped account lookup, while the platform can also resolve a built-in administrator context (`kristagrand/extension/runtime/plugins/ksdk/src/main/java/app/krista/extension/runtime/plugins/ksdk/impl/AccountProviderImpl.java:30–34`, `kristagrand/servers/src/main/java/com/krista/services/accesspoint/service/AuthorizationContextProviderImpl.java:65–89,95–111`). A successful lookup alone cannot establish that the extension was invoked as the regular user. The exact request route remains an engineering follow-up. Apply this decision to the KSDK reads in GAP-004.

**Impact on Requirements:** FR-004 requires authenticated context and verification as a regular user in the test workspace.

### Important Gaps

#### GAP-002. Pick the artifact and make registration reproducible

**Original Question:** Which package and workflow should the sample teach for standard extension upload and registration?

**Resolution:** Agreed. Build a standard extension JAR and verify the documented workflow in the test workspace. Upload it through Native Java Jar under Krista → Agent, add it to the workspace, save the app URL in Setup, and assign the invoker to a topic. Verify the minimum JAR metadata in that run. Studio stores the binary in the selected catalog domain, and the platform creates the workspace invoker and its route from that catalog entry (`webmono/projects/StudioNgNew/src/app/modules/catalog/extensions/list/create-new-extension-dialog/create-new-extension-dialog.ts:83–123`, `kristagrand/servers/src/main/java/com/krista/services/defn/fn/FnAddExtensionToWorkspace.java:72–91,105–127,138–151`). The platform uses the catalog entry's domain to classify the invoker (`kristagrand/servers/src/main/java/com/krista/services/defn/fn/helper/InvokerWithHintValuesFactory.java:310–332`). Correction to the evidence in `findings.md`: the inspected `.daz` picker belongs to Import Catalog on an existing catalog provider (`webmono/projects/StudioNgNew/src/app/modules/editors/workspace/content/tabs/6-setup/content/5-catalog-providers/create/ws-catalog-providers-create.component.html:12–19` and companion `.ts:23–25,147–156`). GAP-003 covers URL refresh after assignment.

#### GAP-003. Define how Setup URL changes take effect

**Original Question:** Is changing the URL part of what the sample should demonstrate, or is first-time setup enough?

**Resolution:** Agreed. Teach first-time setup with a stable HTTPS app entry URL, verify URL refresh in the Krista test workspace, and document the verified refresh steps alongside initial setup. The extension Setup save persists the invoker's attributes (`kristagrand/servers/src/main/java/com/krista/services/defn/fn/FnInvokerUpdateSetup.java:140–181,236–237`), and the topic save separately persists the discovered app URL (`FnSetAgentSetup.java:152–159,186–189` in the same package). Second-pass clarification: clear the invoker selection in the Studio form, reselect it before saving, wait for URL discovery to finish, then save once and reopen the topic to verify the new target. Saving while the selection is cleared removes the assignment (`webmono/projects/StudioNgNew/src/app/modules/editors/workspace/content/tabs/2-channels/details/ws-channel-agent-configuration/ws-channel-agent-configuration.component.ts:70–89,159–173`, sibling `ws-channel-agentic-guide/ws-channel-agentic-guide.component.ts:284–304,338–394`, `kristagrand/servers/src/main/java/com/krista/services/defn/fn/FnSetAgentSetup.java:152–159`). The sequence is supported by source inspection and awaits the live verification. This follows installation and first-time assignment from GAP-002.

#### GAP-004. Choose the user and workspace fields that the sample teaches

**Original Question:** Which user and workspace values should the sample read and show?

**Resolution:** Agreed. Show `Hello, <display name>` and the workspace name as the two visible fields. Read the person name from the authenticated KSDK account and show in the documentation how the workspace name and ID come from the bound KSDK workspace. Second-pass clarification: Krista constructs the authenticated account with a person record containing the display name, so the sample can read it directly from that account (`kristagrand/servers/src/main/java/com/krista/services/AuthorizationContextFactory.java:86–94`, `kristagrand/extension/runtime/plugins/ksdk/src/main/java/app/krista/extension/runtime/plugins/ksdk/impl/AccountImpl.java:66–69`, `PersonImpl.java:22–25` and `WorkspaceImpl.java:80–90` in the same package). KA's browser derives its initial name from the launch message or the email, while its directory handler demonstrates the KSDK name accessor (`knowledge-agent/ui/src/app/core/services/auth.service.ts:430–449`, `krista-global-catalog@d2ca488844:knowledge-agent/src/main/java/app/krista/global/knowledgeagent/service/GetWorkspaceHandler.java:89–99`). Follow GAP-001 for authenticated invocation and the regular-user check.

#### GAP-005. Decide what developers can exercise outside Krista

**Original Question:** Does standalone execution use fixture data or depend on live workspace access?

**Resolution:** Agreed. Provide an explicit standalone developer command that renders the same greeting with a clearly labeled fixture user and workspace, without a Krista login. Keep real KSDK reads in the Krista-launched mode. Show a waiting or error state when that mode lacks launch context. Document what each command or launch exercises. Second-pass clarification: KA selects its development or production environment at build time and gates fixture identity on the standalone development configuration, while a distinct iframe check drives layout and loading (`knowledge-agent/ui/angular.json:83–98,123–146`, `ui/src/app/core/services/auth.service.ts:99–128,316–332`, `ui/src/app/app.component.ts:24–37`). KA's backend handles development identity separately from the live session path (`knowledge-agent/core/src/main/java/app/krista/knowledgeagent/api/filter/TenantContextFilter.java:196–212`). `klag/docs-specs/to-1.0.0/requirements-analysis.md:107–111` gives adjacent precedent for isolating fixture controls from live mode. Follow GAP-001 for live identity and GAP-004 for the shared visible fields.

## Defaults Applied

| # | Decision | Rationale |
| --- | --- | --- |
| 1 | Keep the refinement artifacts at the sample repo root. | Easy to find in the new checkout. |
| 2 | Use “Hello” as the working sample name. | Matches the requested greeting. |

## Accepted Scope Expansions

None.

## Declined Scope Expansions

None proposed.

## Assumptions Log

| # | Assumption | Discussed in | User response |
| --- | --- | --- | --- |
| 1 | Local use is for individual development. Live use is for occasional demos in a Krista test workspace. | Scope Contract | Confirmed. |
| 2 | Real user and workspace reads are exercised in the Krista launch. Standalone mode uses labeled fixtures. | GAP-001, GAP-005 | Confirmed. |
| 3 | The test workspace’s normal Studio workflow is the basis for the documented install and refresh steps. | GAP-002, GAP-003 | Confirmed, with live verification required. |

## Codebase Context

### Existing Functionality Affected

The greenfield sample integrates with existing Krista Studio, catalog, extension runtime, and KSDK contracts. Its platform path uses the existing Agent domain, invoker Setup attributes, and topic configuration. [Evidence and source revisions](findings.md)

### Terminology Alignment

- An **extension JAR** is the binary uploaded to the catalog. An **invoker** is the installed, configured instance of that extension in a workspace.
- The **invoker URL** routes to the extension. The **hosting URL** is the app entry point Studio saves for the topic’s iframe.
- **Standalone** is the explicit local development launch with fixtures. **Krista-launched** is the embedded run with platform identity and real KSDK reads.

### Integration Points

1. Studio uploads the JAR, creates the workspace invoker, and saves its Setup attributes. Topic assignment calls the invoker’s URL discovery resource and persists the returned app URL.
2. Krista opens the app for the topic. The app starts the live request from the launch context. An existing authenticated Krista route must supply the user and workspace to the extension before it reads the greeting data through KSDK.
3. The standalone command supplies fixture names to the same greeting UI. The developer instructions explain both paths.

### Corrections Made During Refinement

- **GAP-001.** KA’s callback demonstrates a workspace-scoped account lookup. A successful lookup leaves open whether the extension had the current regular user’s authenticated context. The platform can also supply a built-in administrator context. [Lookup](/Users/john/Projects/kristagrand/extension/runtime/plugins/ksdk/src/main/java/app/krista/extension/runtime/plugins/ksdk/impl/AccountProviderImpl.java:30) · [Context provider](/Users/john/Projects/kristagrand/servers/src/main/java/com/krista/services/accesspoint/service/AuthorizationContextProviderImpl.java:64)
- **GAP-002.** The inspected `.daz` picker belongs to **Import Catalog** on an existing provider. The individual extension upload uses the JAR flow. [Catalog import](/Users/john/Projects/webmono/projects/StudioNgNew/src/app/modules/editors/workspace/content/tabs/6-setup/content/5-catalog-providers/create/ws-catalog-providers-create.component.html:12) · [Extension upload](/Users/john/Projects/webmono/projects/StudioNgNew/src/app/modules/catalog/extensions/list/create-new-extension-dialog/create-new-extension-dialog.html:17)
- **GAP-003.** Studio keeps the old topic URL when the selected invoker remains the same. The inferred refresh clears and reselects within the form before a single save. Saving the cleared state removes the assignment. [Selection and discovery](/Users/john/Projects/webmono/projects/StudioNgNew/src/app/modules/editors/workspace/content/tabs/2-channels/details/ws-channel-agentic-guide/ws-channel-agentic-guide.component.ts:338) · [Topic persistence](/Users/john/Projects/kristagrand/servers/src/main/java/com/krista/services/defn/fn/FnSetAgentSetup.java:152)
- **GAP-004.** KA initializes its visible name from browser launch context. The authenticated account already carries its person name, which gives this sample a direct KSDK source. [KA UI](/Users/john/Projects/knowledge-agent/ui/src/app/core/services/auth.service.ts:430) · [Authenticated account construction](/Users/john/Projects/kristagrand/servers/src/main/java/com/krista/services/AuthorizationContextFactory.java:86)
- **GAP-005.** KA’s build configuration selects the development identity path. Its separate iframe detection drives layout and loading. [Build modes](/Users/john/Projects/knowledge-agent/ui/angular.json:83) · [Iframe handling](/Users/john/Projects/knowledge-agent/ui/src/app/app.component.ts:24)

### Verification Still Required During Implementation

Confirm the minimum JAR metadata and actual Studio upload and assignment flow. Verify that an assigned topic opens the changed app URL after the refresh procedure. Establish the exact authenticated request route, then verify that it yields the correct KSDK account and workspace when a regular user opens the sample. Record the results in [findings.md](findings.md).

## Readiness Certification

One refinement round resolved all five gaps with explicit user input. The scope check found six source-backed functional requirements, one constraint, and zero scope expansions. The live checks above remain implementation acceptance work.

**This document is the validated input for `create-spec-from-requirements`.**
