# Test Plan · Hello Custom Agent

**Created:** 2026-11-18  
**Requirements:** [Validated requirements](requirements-analysis.md)  
**Context:** [Source and platform findings](findings.md) · [UI and launch states](ui-ux-design.md)  
**Status:** CTO reviewed and approved. All acceptance checks are planned.

## Test strategy

Use a fresh sample checkout, a real browser, the built JAR, and a Krista test workspace with Studio and the extension runtime. The test workspace run exercises the installed invoker, the real topic launch, and the KSDK account and workspace reads. The standalone run uses the documented fixture inputs and exercises the actual local launch command and shared greeting view. Each acceptance criterion below is **Must Pass**.

The Scope Contract defines the audience, security posture, and regression sensitivity. Performance, load, penetration, and backward-compatibility criteria are N/A per that contract.

### Setup and cleanup

- Build from `sample-custom-agent` with the documented development prerequisites. Use a test workspace where the installer can upload under **Krista → Agent**, create an invoker, and edit a topic. Use a regular, non-administrator test account that can open the topic for the live greeting check. Record the account’s known display name and the workspace’s known name and ID from the test setup for comparison. Choose fixture names distinct from both.
- Create a test-owned catalog entry, invoker, and topic. Prepare two distinguishable, browser-reachable HTTPS entry URLs, A and B, serving the sample. Both must be reachable from the browser used for the Krista test. Use the same invoker and topic for both URL checks.
- After the run, remove the test assignment, invoker, and any topic or catalog entry created for the test where permissions allow. Stop local processes and remove temporary diagnostics and temporary hosting created for the run. Restore any test-owned configuration that will be reused.
- For each check record the build revision, environment and platform version where relevant, procedure, observable result and status in [`findings.md`](findings.md). Sanitize evidence for publication. Record route and credential type, never credential values, raw launch messages, cookies, or personal account identifiers.

## Acceptance criteria by requirement

| ID | Requirement | Given | When | Then |
| --- | --- | --- | --- | --- |
| AC-001-1 | FR-001 | The fixture and test-workspace live launch paths | Each mode reaches its ready state | The same greeting view presents `Hello, <display name>` and the workspace name from that mode’s source. |
| AC-002-1 | FR-002 | A fresh sample checkout and a test installer with the needed Studio permissions | The installer follows the documented build and **Native Java Jar** workflow under **Krista → Agent**, adds the extension to the workspace, configures it, and assigns the invoker to a topic | The JAR is accepted, its metadata provides the expected extension and Setup field, and the configured invoker is selectable and saved on the topic. A workspace user can open the assigned topic. |
| AC-003-1 | FR-003 | The test invoker has a saved Setup URL A | Studio selects the invoker for the initial topic assignment | `GET <routed invoker base>/rest/hostUrl` returns JSON with `payload.hostingUrl` equal to A, and the topic opens the app at A. |
| AC-003-2 | FR-003 | The same topic is assigned to the invoker with URL A | The installer saves URL B in invoker Setup, clears the selection in the topic form, reselects the same invoker, waits for discovery, and saves the topic once | Discovery returns B and reopening the topic loads B. The topic remains assigned to the same invoker. The instructions record the working refresh procedure alongside initial setup. |
| AC-004-1 | FR-004 | The assigned topic has a live greeting operation in the real workspace | A regular signed-in user opens the topic and a test client also calls that same operation without the approved end-user credential | The valid invocation runs in that user’s authenticated Krista context and displays the names from the KSDK account and scoped workspace. The credential-free call returns non-success with neither greeting field. Normal Studio URL discovery succeeds before the app acquires a greeting credential. |
| AC-005-1 | FR-005 | A developer has the prerequisites and a fresh browser session | They use the explicit standalone command without a Krista login | The shared greeting view shows the expected fixture names and a visible local sample-data label. The documentation explains how to run each mode and what each exercises. |
| AC-005-2 | FR-005 | The app is configured for the Krista launch | It opens without the required launch context | It displays the specified waiting or error state and presents no fixture greeting. A fresh launch from Krista can proceed to the live greeting. |
| AC-006-1 | FR-006 | The sample has been built and both launch modes have been tried | A reviewer checks `findings.md` | Source observations and guide discrepancies are accompanied by identifiable implementation and test-workspace outcomes, with no claim of live verification based only on static inspection. |
| AC-CON-001 | CON-001 | The sample is delivered in the adjacent `sample-custom-agent` checkout | A developer follows its own instructions | The sample source, instructions, and findings are present in that checkout. The documented hands-on workflow is run from the sample repo. |

## Integration and end-to-end scenarios

### ITS-001 · Build and install through Studio

1. From the sample checkout, use the documented build command. Inspect the produced JAR with `jar tf`, checking the location of sample classes and generated extension metadata. Record the build command, produced artifact and metadata entries.
2. Upload that JAR in Studio via **Native Java Jar** under **Krista → Agent**. Use **Add to Workspace** to create the test invoker. Confirm Studio presents the sample’s Setup field and the invoker appears in the topic’s Agent Configuration.
3. Save URL A in Setup, select the invoker in Agent Configuration, and let discovery complete before saving. Capture the normal Studio request status and `payload.hostingUrl` in browser network evidence before the app first launches or acquires its greeting credential. Reopen the topic and capture the actual iframe document URL. A successful JAR build or separate direct app visit does not satisfy Studio installation or discovery.

### ITS-002 · Verify the live greeting as a regular workspace user

1. Use the regular test account, separately from the installer session, and open the assigned topic. Match the greeting and workspace name to the known test values.
2. Follow the request from the iframe action to the extension invocation. Use a debugger or request-scoped test diagnostic to confirm that the platform supplied the regular user as the authorized account and that the KSDK workspace resolves the expected ID and name on the same invocation. Pair this with the actual source call sites for the display-name and workspace reads. A UI screenshot alone cannot establish the KSDK source.
3. From a clean test client, make the same ordinary request to the selected greeting operation and omit the approved end-user credential, including any cookies or session headers that would supply it. Observe the operation’s actual response. Assert non-success and verify that neither `displayName` nor `workspaceName` is returned. Use the successful discovery observed in ITS-001 as the companion evidence that Studio can obtain the app URL before the app acquires its greeting credential.
4. Record the actual route and platform credential type, the match against the runtime account and workspace, the observable UI outcome, and the credential-free response status and shape. Record unresolved or service-account-only invocations as failures of this criterion.

### ITS-003 · Refresh the saved topic URL

1. Use the topic and invoker from ITS-001. Save the different HTTPS URL B in the same invoker’s Setup.
2. Open the topic’s Agent Configuration. Clear the invoker selection in the editor, reselect the same invoker before saving, and wait for discovery to return B. Save the topic once. Saving while the selection is empty clears the assignment.
3. Reopen the topic with a fresh page load. Verify the recorded iframe target changed from A to B and the same invoker remains assigned. Record the exact working Studio sequence and the before/after discovery and launch evidence. Update the install instructions with the verified refresh steps.

### ITS-004 · Run the standalone greeting

Start the explicitly documented standalone command from the sample repo with the documented fixture inputs. Open the local URL in a fresh browser session with no Krista login. Verify the fixture user and workspace, the visible local sample-data label, and the same greeting structure used in ITS-002.

### ITS-005 · Open platform mode without launch context

Open the platform-mode app in a browser context where the required Krista launch message is absent. Verify that it shows its waiting or error state, with no fixture greeting. Reopen it through the valid test topic to confirm that a real launch still renders the live greeting.

### V-006 · Verify the instructions and findings

Use the sample repo’s instructions as the entry point for ITS-001 through ITS-005. Check that the instructions identify prerequisites, both launch modes, standard Studio registration, Setup and refresh, and the KSDK source of the account and workspace values. Confirm the source evidence and runtime outcomes are recorded in the sample repo’s findings, including any upload, metadata, routing, or UI-label corrections learned from the test workspace.

## Coverage matrix

| Requirement | Criteria | Scenario |
| --- | --- | --- |
| FR-001 | AC-001-1 | ITS-002, ITS-004 |
| FR-002 | AC-002-1 | ITS-001, V-006 |
| FR-003 | AC-003-1, AC-003-2 | ITS-001, ITS-003, V-006 |
| FR-004 | AC-004-1 | ITS-002, V-006 |
| FR-005 | AC-005-1, AC-005-2 | ITS-002, ITS-004, ITS-005, V-006 |
| FR-006 | AC-006-1 | V-006 |
| CON-001 | AC-CON-001 | V-006 |

## Completion evidence

Implementation passes acceptance when all criteria have recorded results, the real test-workspace checks pass, and test-owned artifacts and diagnostics are cleaned up. If the test workspace or necessary account access is unavailable, record the affected checks as blocked. Static inspection, fixtures, and local smoke results do not count as live evidence.
