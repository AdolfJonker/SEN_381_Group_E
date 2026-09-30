
## Access Patterns

Three queries drive most database activity.

The first is the request list query. It fetches `ServiceRequest`s filtered by the logged-in user's role (FR005). This runs on every page load. It needs an index on `submittedById` for requesters and on `assignedToId` for staff. Without these indexes, the query scans every row in the table as the dataset grows.

The second is the request detail query. It fetches one `ServiceRequest` and joins it with its `StatusHistory` rows and `Comment`s. This runs when a user opens a request to read its full history (FR007, FR010).

The third is the status update. It writes one `ServiceRequest` row and one `StatusHistory` row inside a single transaction (ADR-DATA-001). This runs far less often than reads. It is the write path that optimistic concurrency protects.

## Data Sensitivity

`User.email` is personal information. It must not appear in list API responses or application log files. `User.passwordHash` must never be included in any API response. It is stored as a one-way hash and cannot be reversed. `ServiceRequest` descriptions may contain sensitive operational details, such as a report of a security concern. Role-filtered access through the Strategy pattern ensures that each user sees only the requests they are allowed to see (NFR003).

`StatusHistory` rows record who changed a request's status and when (NFR004). Staff and Management can read the full audit trail, including the actor identity. Requesters get a restricted view of their own requests only. They see each status and its timestamp, but not the name of the staff member who made the change. This keeps FR002 and FR004 intact while limiting what staff identity data is shown to requesters.

## Growth Estimate

CivicConnect targets a single campus. Expected load is hundreds to thousands of service requests per month. This is a small dataset. FR009 defines four forward transitions: Open → Assigned → In Progress → Resolved → Closed. A normal lifecycle therefore adds four `StatusHistory` rows per request, or five if the initial Open state is logged at creation. FR009 blocks or flags skipped states, so a flagged skip produces fewer rows, not more. FR011 keeps Resolved and Closed as separate steps, which is already counted above. The current state model has no reopen transition. If one is added later, the row count per request is no longer capped, but growth would still be small.

Free-tier database storage is enough for this volume at M2. No partitioning or archiving strategy is needed at this milestone. The connection pool is sized to the free-tier limit of the hosting platform (RISK-006, FEC-003). If the campus expands to multiple sites in a later phase, this estimate must be reviewed.

## SPOF

The single-instance database SPOF is documented and accepted in Section 5.3. The M2 design adds no other single points of failure. Because the Observer runs in-process, no external message broker or notification service sits in the critical path of a status change.

**RTM link:** FR005 (RTM-005), FR007 (RTM-007), FR009 (RTM-009), FR010 (RTM-010), NFR003 (RTM-016), NFR004 (RTM-017)
