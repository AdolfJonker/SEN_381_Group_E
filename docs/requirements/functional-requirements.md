# Functional Requirements (FRs)

| ID | Requirement | Source | Priority | Acceptance Criteria |
|---|---|---|---|---|
| FR001 | Requester shall submit a new service request with title, description and a required category selected from a controlled list. | Requester capability; conflict #1 | High | Submission is rejected if category is missing; successful submission returns a unique request ID and confirmation. |
| FR002 | Requester shall view the status of any request they submitted. | Requester capability | High | Status (Open/Assigned/In Progress/Resolved/Closed) is retrievable within two navigation steps of login. |
| FR003 | Requester shall view a history/list of previously submitted requests. | Requester capability | Medium | List displays at least: request ID, category, date submitted, current status. |
| FR004 | Requester shall receive meaningful feedback when a request's status changes. | Requester capability; conflict #2 | High | Updated status is visible to the requester without requiring a manual refresh or enquiry to staff. |
| FR005 | Staff shall view service requests relevant to their authorised role. | Staff capability | High | List is filtered to the staff member's authorised scope. |
| FR006 | Staff shall search, filter or sort requests by status, category or date. | Staff capability | Medium | Filtering by status and category, and sorting by date, are available. |
| FR007 | Staff shall view full details of a specific request. | Staff capability | High | All submitted fields, status and action history are visible within two navigation steps of the request list. |
| FR008 | Staff shall assign or accept responsibility for a request. | Staff capability | High | Request is linked to exactly one responsible staff member; the assignment is visible. |
| FR009 | Staff shall update request status through defined, controlled transitions, per the following state model: Open → Assigned → In Progress → Resolved → Closed (Resolved and Closed are distinct, separately authorised steps — see FR011). | Staff capability; conflict #2 | High | Only transitions defined in the state model above are permitted; any attempt to skip a state (e.g. Open → Closed with no intervening action) is prevented or clearly flagged. |
| FR010 | Staff shall record comments or resolution notes on a request. | Staff capability | Medium | At least one comment/note can be attached and remains visible in the request history. |
| FR011 | Authorised staff shall resolve or close a request. | Staff capability | High | Only authorised staff can set the status to Resolved or Closed; Resolved and Closed are recorded as separate, sequential actions per FR009's state model. |
| FR012 | Management shall view an aggregate service activity summary (counts by status). | Management capability; conflict #3 | Medium | Summary displays counts of Open, Overdue, Resolved and Closed requests. |
| FR013 | Management shall view requests filtered by category, status or other justified dimension. | Management capability | Medium | Filtering by at least category and status is available. |

