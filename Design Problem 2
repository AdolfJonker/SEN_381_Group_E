# 6.2 Design Problem 2 — Role-Based Request Filtering

## Problem Statement

FR005 requires that each staff member sees only requests relevant to their role. NFR003 requires role-based access control (RBAC) for all data access. Three roles exist in CivicConnect: Requester, Staff, and Management. Each role needs different filtering logic when fetching the request list. If this logic is written as a chain of if-else statements inside the request-list endpoint, the code for all three roles sits in one place. Adding a new role or changing one role's logic means editing code that also controls the other roles. Each role cannot be tested in isolation.

## A2 Research Evidence

Assignment 2, Task 1 also covered role-based filtering. Two options were compared.

**Option 1: if-else chain inside the list endpoint.** Simple to write but hard to extend. A new role requires editing existing code. Roles cannot be unit-tested independently. This approach violates the open-closed principle (Martin, 2000).

**Option 2: Strategy pattern.** A `FilterStrategy` interface defines one method: `getFilteredRequests(userId)`. Each role has its own class that implements this interface. The list service selects the right strategy at runtime based on the logged-in user's role. This means each strategy can be tested on its own, and a new role only needs a new strategy class (Gamma et al., 1995; Six et al., 2022).

For category validation (FR001), the A2 research found that a simple membership check against a controlled list is sufficient. No pattern is needed. The valid categories are short and fixed.

## Final Judgement

Our team adopted the Strategy pattern for role-based filtering. The A2 recommendation matched the project constraints. NFR003 is a high-priority security requirement, and RISK-003 (RBAC implemented incorrectly) is rated High impact. A testable, isolated design directly reduces this risk.

Three strategy classes implement the `FilterStrategy` interface. `RequesterFilterStrategy` returns only the requests submitted by the logged-in user. `StaffFilterStrategy` returns requests assigned to or visible to that staff member. `ManagementFilterStrategy` returns all requests, supporting FR012 and FR013. Category validation uses a simple list check, as the A2 research recommended. No additional pattern is applied there.

**Trade-off:** Three strategy classes replace what could be one function. This is more code to maintain. The benefit is that each strategy is independently testable, which directly supports CON004 (quality must be demonstrated, not claimed) and reduces RISK-003.

---

## ADR-DES-002 — Strategy Pattern for Role-Based Request Filtering

| Field | Content |
| --- | --- |
| **ID** | ADR-DES-002 |
| **Status** | Decided — M2 |
| **Context** | FR005 requires role-filtered request lists. NFR003 requires RBAC. Three roles need different filtering logic. Coupling all role logic in one function makes it hard to test per role and hard to add roles. |
| **Alternatives considered** | 1) If-else chain in the request-list endpoint. Simple but couples all roles; violates open-closed principle; roles cannot be unit-tested in isolation. 2) Strategy pattern: one interface, one class per role; each independently testable; new role = new class only. |
| **Decision** | Strategy pattern. `FilterStrategy` interface with three implementations: `RequesterFilterStrategy`, `StaffFilterStrategy`, `ManagementFilterStrategy`. `RequestListService` selects the strategy based on the authenticated user's role. |
| **Evidence** | A2 Task 1: Gamma et al. (1995) Strategy pattern; Martin (2000): open-closed principle; Six et al. (2022) access-control design. Project context: FR005, NFR003, RISK-003, CON004. |
| **Trade-offs** | More classes than a single-function approach. Accepted because each strategy is independently unit-testable, directly meeting CON004 and reducing RISK-003. |
| **RTM links** | RTM-005 (FR005); RTM-016 (NFR003); RTM-001 (FR001 category list check) |

---

## Application Evidence

The following classes implement this decision.

- **`FilterStrategy`:** interface defining `getFilteredRequests(userId)`.
- **`RequesterFilterStrategy`:** returns requests where `submittedById` matches the logged-in user.
- **`StaffFilterStrategy`:** returns requests assigned to or open for the staff member's scope.
- **`ManagementFilterStrategy`:** returns all requests; no user-based filter applied.
- **`RequestListService`:** injects the correct strategy based on session role; calls `getFilteredRequests`.
