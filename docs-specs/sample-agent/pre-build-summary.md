# Pre-Build Confirmation · Hello Custom Agent

**Date:** 2026-11-18  
**Status:** AWAITING USER CONFIRMATION  
**Review:** Approved with zero open critical or major findings. [CTO review](cto-review-report.md)

## Scope Contract

| Dimension | Value |
| --- | --- |
| Audience & realistic scale | Individual developers running locally and occasional demos in a Krista test workspace. |
| Perf / scale targets | None specified. |
| Security sensitivity | None-new |
| Regression sensitivity | None |
| Expansion tolerance | Minimal |
| Pipeline tier | Light |

Follow Krista’s existing identity and workspace-scoping rules when the platform launches the agent. This is a greenfield sample repo using existing Krista extension and KSDK contracts. Accepted scope expansions are zero.

## What will be built

| ID | Requirement | Category |
| --- | --- | --- |
| FR-001 | Show `Hello, <display name>` and the workspace name in one shared view. | Core |
| FR-002 | Build and install a standard extension JAR through Studio under **Krista → Agent**. | Core |
| FR-003 | Return the saved Setup URL to Studio and document initial assignment and refresh. | Core |
| FR-004 | Read the signed-in user and scoped workspace through KSDK. | Core |
| FR-005 | Document the explicit local fixture launch and the live Krista launch. | Core |
| FR-006 | Capture source and runtime findings for the extension docs. | Required |
| CON-001 | Deliver the sample in the adjacent `sample-custom-agent` repo. | Constraint |

## What implementation must verify

Install and assign the real JAR in a Krista test workspace. Check that Studio discovers the initial URL and that a changed URL takes effect after refreshing the existing topic assignment. Prove the selected route supplies the regular user’s authenticated KSDK context before wiring the live greeting. Verify the same greeting operation returns no data without the end-user credential while normal Studio URL discovery succeeds. Exercise standalone and missing-context behavior. The checks are pending. [Specification](specification.md) · [Test plan](test-plan.md) · [UI design](ui-ux-design.md)

The CTO gate blocks on in-scope critical or major findings. Minor and deferred items are advisory. **DEF-001** defers KA’s service registration and directory workflows, which serve KA-specific features.

**Confirmation needed.** Reply **confirmed** to proceed to implementation via `project-manager`, or provide any changes you want reviewed before build. Expanding DEF-001 would reopen requirements refinement.
