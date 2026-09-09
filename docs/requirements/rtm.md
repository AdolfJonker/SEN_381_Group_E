# Requirements Traceability Matrix (RTM)

The RTM traces requirements from their originating stakeholder need or source through the requirement itself, to the acceptance criteria. The RTM is structured so that later evidence can be added without rebuilding it. The full expected chain runs as follows:

**Stakeholder/Source → Requirement → Design/Architecture → Issue/PR → Implementation → Test → Acceptance/Release Evidence**

In the table below, the Source, Requirement and Acceptance Criteria portions of the chain are established. The Design and Test reference columns are marked "TBD" (to be determined) for now, as these will be filled in during Milestone 2 and Milestone 3 respectively, once design and test evidence exist. Once a requirement is baselined, it cannot be changed without an approved change record. Maintaining structured traceability is a recognised and consistent challenge in software engineering practice (Mucha, Kaufmann & Riehle, 2024); for this reason, the RTM is treated as a live artefact from this milestone onward.

| RTM ID | Source | Req ID | Requirement (summary) | Priority | Acceptance Criteria (summary) | Design Ref (M2) | Test Ref (M3) | Status |
|---|---|---|---|---|---|---|---|---|
| RTM-001 | Requester stakeholder need: easy submission; Conflict #1 | FR001 | Submit request with title, description and required category | High | Rejected if category missing; successful submission returns unique request ID | TBD | TBD | Baselined |
| RTM-002 | Requester stakeholder need: visibility of status | FR002 | Requester can view status of any submitted request | High | Status retrievable within 2 navigation steps of login | TBD | TBD | Baselined |
| RTM-003 | Requester stakeholder need: history of requests | FR003 | Requester can view history/list of previously submitted requests | Medium | List shows ID, category, date submitted, current status | TBD | TBD | Baselined |
| RTM-004 | Requester stakeholder need (Conflict #2) | FR004 | Requester receives feedback when status changes | High | Updated status visible without manual refresh or enquiry to staff | TBD | TBD | Baselined |
| RTM-005 | Staff stakeholder need: relevant workload view | FR005 | Staff can view requests relevant to their authorised role | High | List filtered to staff member's authorised scope | TBD | TBD | Baselined |
| RTM-006 | Staff stakeholder need: manageable workload | FR006 | Staff can search, filter or sort requests | Medium | Filter by status/category and sort by date available | TBD | TBD | Baselined |
| RTM-007 | Staff stakeholder need: clear ownership | FR007 | Staff can view full details of a specific request | High | All fields, status and action history visible within 2 steps of the list | TBD | TBD | Baselined |
| RTM-008 | Staff stakeholder need: clear ownership | FR008 | Staff can assign or accept responsibility for a request | High | Request linked to exactly one responsible staff member; assignment visible | TBD | TBD | Baselined |
| RTM-009 | Staff stakeholder need (Conflict #2) | FR009 | Staff update status via controlled transitions (Open→Assigned→In Progress→Resolved→Closed) | High | Only defined transitions permitted; skipped states prevented or flagged | TBD | TBD | Baselined |
| RTM-010 | Staff stakeholder need: accountability | FR010 | Staff can record comments/resolution notes on a request | Medium | At least one note attached and visible in request history | TBD | TBD | Baselined |
| RTM-011 | Staff stakeholder need: accountability | FR011 | Authorised staff can resolve or close a request | High | Only authorised staff set Resolved/Closed; recorded as separate sequential actions | TBD | TBD | Baselined |
| RTM-012 | Management stakeholder need: reliable oversight (Conflict #3) | FR012 | Management can view aggregate activity summary (counts by status) | Medium | Summary displays counts of Open, Overdue, Resolved, Closed | TBD | TBD | Baselined |
| RTM-013 | Management stakeholder need: reliable oversight | FR013 | Management can filter requests by category, status or justified dimension | Medium | Filtering by at least category and status available | TBD | TBD | Baselined |
| RTM-014 | (Conflict #2) transparency vs staff workload | NFR001 | Status changes visible to requester without a separate manual notification step | High | Visible on next page load or within 5 seconds of a staff action | TBD | TBD | Baselined |
| RTM-015 | (Conflict #1) speed vs structure | NFR002 | Minimal submission path requires no more than 6 fields | Medium | Field count on minimal path ≤ 6 | TBD | TBD | Baselined |
| RTM-016 | Lifecycle-wide security requirement: authorisation/least privilege (see CON005) | NFR003 | System restricts access to request data by role (RBAC) | High | A requester cannot retrieve another requester's data via UI or direct request | TBD | TBD | Baselined |
| RTM-017 | Traceability/accountability need (CON005) | NFR004 | System retains complete, unaltered history of status changes | High | Every status change logged with timestamp and actor identity; not editable/deletable by normal users | TBD | TBD | Baselined |
| RTM-018 | Requester accessibility need | NFR005 | Core submission/status-view usable on desktop and common mobile widths | Medium | Usable at 375px width without horizontal scrolling for core flows | TBD | TBD | Baselined |
