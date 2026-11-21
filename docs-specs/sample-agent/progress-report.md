# Implementation Progress Report

**Project** Hello Custom Agent

**Last Updated** 2026-11-18

**Overall Status** Blocked

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

## Phase Status Summary

### Phase 1 — Extension and route proof

**Status:** Blocked

- TASK-001 built the Java 21 Framework 4 JAR and Angular shell. The local SDK recognized the generated Agent metadata. Real Krista SDK processors served the bundled files and returned fixture invoker Setup URL changes through the discovery contract. The local invoker is a fixture. The target install succeeded for both artifacts. Live framing and Studio discovery succeeded for both invokers.
- TASK-002 target is JM Training in Solution Labs. Studio 3.7.2-rc1 build 93 showed **Catalog → Krista → Agent → Create New Extension → Native Java Jar → Upload**. The normal JAR and launch-context probe installed as separate managed invokers. The launch probe reported the expected parent origin and a JWT-shaped access token. With the user's approval, identity probe 1.0.1 installed, framed, and tested the current admin session using the same-invoker REST route and Solution Labs IAM. The credential-free greeting returned HTTP 401 with neither greeting field. The credentialed greeting also returned HTTP 401. Diagnostic version 1.0.3 located the failure at the KSDK account status accessor, which throws UnsupportedOperationException. Version 1.0.4 removed that probe-only accessor and verified HTTP 200 CONTEXT_BOUND for the approved admin session. Its KSDK display name matched the client profile, and its bound Workspace returned JM Training. Credential-free HTTP 401 with neither greeting field was reconfirmed. The topic was restored to the normal sample. The identity probe remains installed.
- The CTO reviewer rejected phase certification while live acceptance is pending. The reviewer found that Java URI parsing accepts explicit port 65536. The shared Setup and discovery validator now rejects it. Both JARs were rebuilt and the real SDK dispatcher verified HTTP 503 without a launch target for a persisted invalid port, plus exact valid URL preservation.

### Phase 2 — Shared view and live reads

**Status:** Blocked

- TASK-003 built and browser-tested the Angular greeting. ITS-004 standalone passed. ITS-005 direct platform mode and a narrow iframe without context passed locally. The normal sample also framed in the live test topic and showed waiting then recovery. Successful live greeting and recovery after authentication still await TASK-004.
- TASK-004 depends on proving one installed invocation supplies the regular user's authenticated KSDK context.
- The CTO reviewer confirmed the local greeting and missing-context evidence and rejected phase certification pending the live greeting and topic launch.

### Phase 3 — Verification and documentation

**Status:** Blocked

- Drafted and checked the sample README, changelog, and findings against the local artifacts. Verified the native upload, managed provisioning, Setup, Studio discovery, probe framing and same-invoker URL refresh in the test workspace. The test topic remains assigned to the normal sample. The authenticated greeting check and final walkthrough remain outstanding.
- Addressed the reviewer's documentation finding about locating the routed URL. The README now identifies **Details → Extension Base URL** from the Studio source. The URL refresh check passed. The reviewer withheld certification pending the finished live walkthrough and implemented KSDK call sites.

## Blocked Items

- Full runtime acceptance still requires a non-admin session in JM Training. The approved admin-session probe passed with credential traffic confined to Solution Labs. The regular-user acceptance run and final sample integration remain open.

## Scope-Creep Observations

None.

## Decisions

**Level of effort** Days, within the specification's two to four engineer-day estimate with platform access.

**Clearance:** CLEARED 2026-11-18

- Treated the direct invocation of the project-manager skill with the approved specification as the owner's pre-build go-ahead.
- Started the standalone view in parallel with extension packaging, using the dependency-free path identified in the specification's engineering follow-up. The live adapter depends on the installed route proof.
- Implementer effort inherited from this session.
- Independent CTO review ran with medium effort. No phase is certified.
- Used the Framework 4 static resource surface as the hosting candidate. Solution Labs successfully served and framed the bundled static probe.
