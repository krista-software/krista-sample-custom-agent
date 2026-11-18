# CTO Review Report · Hello Custom Agent

**Reviewer:** Independent CTO reviewer  
**Review date:** 2026-11-18  
**Status:** APPROVED  
**Review effort:** Light tier. Requested and dispatched reasoning effort was `medium`.

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

## 1. Requirements coverage

| Requirement | Specification coverage | Planned evidence | Assessment |
| --- | --- | --- | --- |
| FR-001 · Greeting and workspace name | §1, §4.2–4.3 and the shared view | AC-001-1, ITS-002 and ITS-004 | Covered |
| FR-002 · Standard Studio registration | §3.1 and TASK-001–002 | AC-002-1, ITS-001 and V-006 | Covered |
| FR-003 · Discover and refresh the saved URL | §3.2–3.3 | AC-003-1 and AC-003-2, ITS-001, ITS-003 and V-006 | Covered |
| FR-004 · Live account and workspace from KSDK | §4.1–4.2 and TASK-002/004 | AC-004-1, ITS-001, ITS-002 and V-006 | Covered |
| FR-005 · Standalone and Krista launch | §4.3 and the view states | AC-005-1 and AC-005-2, ITS-002, ITS-004, ITS-005 and V-006 | Covered |
| FR-006 · Developer findings | §5 and TASK-005 | AC-006-1 and V-006 | Covered |
| CON-001 · Separate sample repository | §1–2 and §5 | AC-CON-001 and V-006 | Covered |

All six functional requirements and the repository constraint have corresponding design decisions. The validated requirements add no other non-functional requirements. Source findings add context for the integration and record their evidence provenance. [Validated requirements](requirements-analysis.md#restated-requirements) · [Acceptance matrix](test-plan.md#coverage-matrix) · [Source findings](findings.md)

## 2. Design and platform contracts

| Decision | Assessment | Evidence and rationale |
| --- | --- | --- |
| E-001 · Standard JAR and Studio discovery | Sound | The native JAR upload and existing workspace invoker path teach the ordinary flow. KA reads the saved URL from its current invoker and returns the field Studio consumes. Studio compares invoker IDs before discovery, then saves the discovered host URL with the topic. Clearing and reselecting before one save is consistent with those paths and requires live verification. |
| E-002 · One shared Angular view and explicit launch selection | Sound | The fixture launcher and embedded adapter produce the same two display fields. The embedded build waits for platform context and reports a failure in the same view. Hosting uses existing HTTPS delivery and the extension runtime. |
| E-003 · Prove the authenticated path before app wiring | Sound | The reflected dispatcher installs a request context from the authenticator result. The callback itself executes under Krista administrator context, so the KA account-returning callback cannot prove regular user identity. Candidate IAM introspection has a source-backed access-token contract. Supported credentials, reachability, origin behavior and regular-user binding are deployment facts for TASK-002. |

[Specification](specification.md#2-architecture) · [Studio JAR upload](/Users/john/Projects/webmono/projects/StudioNgNew/src/app/modules/catalog/extensions/list/create-new-extension-dialog/create-new-extension-dialog.html:18) · [Studio discovery and saved URL](/Users/john/Projects/webmono/projects/StudioNgNew/src/app/modules/editors/workspace/content/tabs/2-channels/details/ws-channel-agentic-guide/ws-channel-agentic-guide.component.ts:338) · [Reflected dispatcher](/Users/john/Projects/kristagrand/extension/executor/reflect/src/main/java/app/krista/extension/executor/reflect/ReflectInvoker.java:674) · [Authenticator context switch](/Users/john/Projects/kristagrand/extension/executor/reflect/src/main/java/app/krista/extension/executor/reflect/ReflectRequestAuthenticator.java:48) · [IAM implementation](/Users/john/Projects/kristagrand/iam/rest/src/main/java/com/krista/iam/rest/api/service/TokenIntrospectionService.java:40)

The route investigation is bounded. TASK-002 first checks for an existing end-user invocation, then considers IAM introspection when the deployed client supplies a supported credential. The second path requires an active result, membership of the runtime workspace, and a workspace-scoped account whose person ID matches the verified subject. The final greeting uses the KSDK account’s person record and the same invocation’s bound workspace. The specification requires recording a blocked platform prerequisite if neither candidate proves the user binding. This is the engineering follow-up GAP-001 authorizes. [GAP-001](requirements-analysis.md#gap-001-establish-the-signed-in-identity-for-the-embedded-data-request) · [Route probe](specification.md#41-confirm-the-live-invocation-in-the-target-workspace) · [Scoped account lookup](/Users/john/Projects/kristagrand/servers/src/main/java/com/krista/services/ksdk/accounts/AccountStoreImpl.java:121) · [Person record](/Users/john/Projects/kristagrand/servers/src/main/java/com/krista/services/AuthorizationContextFactory.java:86) · [Workspace accessors](/Users/john/Projects/kristagrand/extension/runtime/plugins/ksdk/src/main/java/app/krista/extension/runtime/plugins/ksdk/impl/WorkspaceImpl.java:80)

## 3. Acceptance plan

The six planned scenarios use the built JAR, an actual browser, and a real Krista test workspace for the platform flows. ITS-001 requires Native Java Jar upload, Add to Workspace, Setup, assignment and browser launch. ITS-003 compares URLs A and B on the same invoker and topic with an observable iframe target after one save. ITS-002 uses a distinct regular, non-administrator account and compares the handler’s KSDK account and workspace ID and name with known values on the same invocation. Both source call sites and runtime observations are required. ITS-004 and ITS-005 exercise standalone and platform-without-context behavior. V-006 checks the walkthrough, findings, and repository placement. The plan specifies cleanup and requires blocked status when test-workspace prerequisites are unavailable. [Test plan](test-plan.md#integration-and-end-to-end-scenarios)

ITS-002 calls the same greeting operation from a clean test client without the approved end-user credential, including cookies or session headers that would furnish it. The actual response must be non-success and omit both greeting fields. ITS-001 captures successful normal Studio discovery before the app acquires its greeting credential. TASK-002 applies the same checks to the route probe, and the acceptance scenarios apply them again to the completed sample. ITS-005 covers the separate embedded UI behavior when launch context is absent. [Route gate](specification.md#41-confirm-the-live-invocation-in-the-target-workspace) · [ITS-001](test-plan.md#its-001--build-and-install-through-studio) · [ITS-002](test-plan.md#its-002--verify-the-live-greeting-as-a-regular-workspace-user)

## 4. Security, tenancy and compatibility

The design retains Krista’s identity authority and runtime workspace. It makes the greeting read-only, derives no identity from browser hints, exposes only the two display names, and keeps credentials and data scoped to the current launch and request. The invoker owns Setup state in Krista. The sample introduces no datastore, global tenant, shared process state, or single-writer dependency. Any instance can resolve a request using the bound workspace. The proposed scope is consistent with the shared multi-tenant and distributed standards. [Data and operational boundaries](specification.md#5-data-and-operational-boundaries)

The runtime treats an authorization context with an account as authenticated, including the built-in administrator context. The design requires a verified end-user account at the greeting resource even when the dispatcher also handles metadata requests. Both the route probe and final test gate exercise the successful regular-user invocation, the denied credential-free greeting call, and normal Studio discovery. [Context provider](/Users/john/Projects/kristagrand/servers/src/main/java/com/krista/services/accesspoint/service/AuthorizationContextProviderImpl.java:64) · [Authentication predicate](/Users/john/Projects/kristagrand/extension/executor/impl/src/main/java/app/krista/extension/executor/impl/AuthorizationContextImpl.java:20) · [Discovery boundary](specification.md#32-setup-and-discovery-contract)

Regression sensitivity is **None**. This greenfield sample consumes existing platform contracts and changes no existing product data or implementation. Compatibility migration analysis is outside the Scope Contract.

## 5. Operations and implementation plan

The implementation plan puts packaging and live route proof before final wiring, allows the standalone view to proceed independently, and requires final live verification and documentation after wiring. The estimate of two to four engineer-days is expressly conditioned on access to the workspace, build inputs and hosting. The installer and regular user, approved static host, supported SDK set, and deployed client invocation are concrete prerequisites. All build, Studio, launch, and KSDK observations remain pending. This review examined local source and planning artifacts. Runtime acceptance will require execution and recorded evidence. [Implementation plan](specification.md#6-implementation-plan) · [Completion evidence](test-plan.md#completion-evidence)

## 6. Issues and decision

**Critical issues:** 0  
**Major issues:** 0  
**Minor issues:** 0

### Resolved during review

| ID | Initial finding | Verified resolution |
| --- | --- | --- |
| MAJ-001 | The missing-context UI test could stop before reaching the greeting endpoint. The plan could pass without proving the greeting’s end-user authorization alongside successful Studio discovery. | AC-004-1 and ITS-002 now require the credential-free request to the same operation, a non-success response, and omission of both greeting fields. ITS-001 supplies observed Studio discovery before the greeting credential. Specification §4.1 applies all three observations to TASK-002 and repeats them on the final sample. |

### Deferred (Out of Scope Contract)

| ID | Concern | Scope basis |
| --- | --- | --- |
| DEF-001 | Reuse of KA service registration, catalog or directory workflows | The sample teaches the standard JAR, URL discovery, and current-user KSDK seams. Those workflows serve KA-specific behavior outside the agreed sample. |

### Certification

I reviewed the amended design, acceptance evidence, platform contracts, operational prerequisites, and implementation sequence against the validated Scope Contract. The specification is approved. Implementation and live acceptance remain pending, including minimum JAR metadata, Studio installation and refresh, and the exact authenticated route and regular-user KSDK proof in the target workspace.

**This specification Meets My High Standards and Honors our Scope Contract. Zero in-scope critical issues, zero in-scope major issues. Out-of-scope concerns recorded in the Deferred section for user awareness; they do not block approval.**

<promise>SPECIFICATION_APPROVED</promise>
