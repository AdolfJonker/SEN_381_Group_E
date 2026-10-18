
Context
Implementation has reached the status-change boundary. The StatusTransitionService writes to the database and notifies its observers. This is the first meaningful interaction boundary in the system. The question is whether observers should call out-of-process services, such as an email or SMS API, at this stage. NFR001 requires that the requester sees status changes quickly. RISK-007 identifies notification delivery failure as a risk if the system depends on an external service.
Decision
At M2, all observers run in the same process as the application. No network calls are made when a status changes. The core transaction, updating ServiceRequest.status and writing the StatusHistory row, is never delayed by a notification attempt. The Observer pattern creates a natural seam. The StatusTransitionService does not know what its observers do. It only calls a notify method. This means a new ExternalNotificationObserver can be added later, registered alongside the existing observers, without changing StatusTransitionService or the transaction logic.
Keeping notifications in-process at M2 also means no external message broker can fail during a status change. The risk recorded in RISK-007 is therefore contained at this milestone.
Forward engineering: When out-of-process notification is needed (email or SMS), a new ExternalNotificationObserver class will implement the same observer interface. It will be registered in StatusTransitionService. No existing code changes. This is deferred and recorded as an update to FEC-005 (Observability & Notification).

ADR-INT-001 — In-Process Observer Seam for Status Notifications
ID	ADR-INT-001
Status	Decided - M2
Context	NFR001 requires that status changes are visible to the requester quickly. RISK-007 records the risk that an external notification service could fail during a status change. A2 API/integration research noted that a network boundary should only be introduced when the project genuinely needs it.
Alternatives considered	1) In process only (chosen): zero external dependencies at this stage; no network latency or failure risk added to the core transaction. 2) External notification API (deferred): adds RISK-007 failure surface and requires secrets management; justified later when confirmed user need exists.
Decision	All observers are in process at M2. StatusVisibilityUpdater signals the UI layer directly. AuditLogObserver writes to the database in the same transaction. No external API, email service, or message broker is called.
Evidence	A2 Task 3 (API/integration research): in-process seam preferred until a real external boundary is needed. RISK-007: notification delivery failure. M1 FEC-005: observability/notification deferred.
Consequence	External notification (email, SMS) is deferred to a later milestone. A new ExternalNotificationObserver class will be added when needed. StatusTransitionService does not change.
RTM links	RTM-014 (NFR001); RISK-007; FEC-005 (updated)

 
