package civicconnect.api;

import civicconnect.records.RequestStatus;
import civicconnect.records.Role;
import civicconnect.records.ServiceRequest;
import civicconnect.records.User;
import civicconnect.repositories.CommentRepository;
import civicconnect.repositories.ServiceRequestRepository;
import civicconnect.repositories.StatusHistoryRepository;
import civicconnect.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * API tests (NFR003, ASR-01, FR005, FR012, RISK-003, RISK-004).
 * DemoUserLoader creates the three demo users.
 * Each test runs in a transaction that is rolled back.
 * Seed clears request tables first so committed data from other SpringBoot tests
 * (shared H2 context) cannot skew absolute counts.
 */
@SpringBootTest
@ActiveProfiles("demo")
@AutoConfigureMockMvc
@Transactional
class AccessControlApiTest {

    private static final String REQUESTER = "requester@civicconnect.local";
    private static final String STAFF = "staff@civicconnect.local";
    private static final String MANAGEMENT = "management@civicconnect.local";
    private static final String OTHER_REQUESTER = "other.requester@civicconnect.local";

    @Autowired
    private MockMvc mvc;

    @Autowired
    private UserRepository users;

    @Autowired
    private ServiceRequestRepository requests;

    @Autowired
    private StatusHistoryRepository statusHistory;

    @Autowired
    private CommentRepository comments;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private ServiceRequest requesterOwn;
    private ServiceRequest otherRequesterOwn;

    @BeforeEach
    void seed() {
        comments.deleteAll();
        statusHistory.deleteAll();
        requests.deleteAll();

        User requester = users.findByEmail(REQUESTER).orElseThrow();
        User other = users.findByEmail(OTHER_REQUESTER).orElseGet(() -> {
            User created = new User();
            created.setEmail(OTHER_REQUESTER);
            created.setName("Other Requester");
            created.setRole(Role.Requester);
            created.setPasswordHash(passwordEncoder.encode("test-only-password"));
            return users.save(created);
        });

        requesterOwn = requests.save(newRequest("Leak in Block A", "Facility Fault", RequestStatus.Open, requester, 1));
        otherRequesterOwn = requests.save(newRequest("Lost laptop charger", "Lost Property", RequestStatus.Open, other, 10));
        requests.save(newRequest("Broken window", "Maintenance", RequestStatus.Closed, other, 20));
    }

    private static ServiceRequest newRequest(String title, String category, RequestStatus status, User by, int daysOld) {
        ServiceRequest request = new ServiceRequest();
        request.setTitle(title);
        request.setDescription("Test data for access-control tests.");
        request.setCategory(category);
        request.setStatus(status);
        request.setSubmittedBy(by);
        request.setCreatedAt(Instant.now().minus(daysOld, ChronoUnit.DAYS));
        return request;
    }

    // ---------- TC-D-INT-01: not signed in -> 401 ----------

    @Test
    @DisplayName("TC-D-INT-01a no session on a data route returns 401")
    void unauthenticatedListIsRejected() throws Exception {
        mvc.perform(get("/api/v1/requests")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/v1/requests/summary")).andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("TC-D-INT-01b wrong password returns 401 with a generic message")
    void wrongPasswordIsRejected() throws Exception {
        mvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + MANAGEMENT + "\",\"password\":\"wrong-password\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid email or password"));
    }

    @Test
    @DisplayName("TC-D-INT-01c no session on the requester list and status routes returns 401, not 403")
    void unauthenticatedRequesterAndStatusRoutesAreRejected() throws Exception {
        // Re-checks observation 1 in status-lifecycle-test-evidence.md (signed-out GET /requests/mine gave 403).
        mvc.perform(get("/api/v1/requests/mine")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/v1/requests/" + requesterOwn.getId())).andExpect(status().isUnauthorized());
        mvc.perform(post("/api/v1/requests/" + requesterOwn.getId() + "/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"toStatus\":\"InProgress\",\"expectedVersion\":0}"))
                .andExpect(status().isUnauthorized());
    }

    // ---------- TC-D-INT-02: wrong role -> 403 ----------

    @Test
    @DisplayName("TC-D-INT-02a Requester and Staff cannot read the Management summary (403)")
    void nonManagementCannotReadSummary() throws Exception {
        mvc.perform(get("/api/v1/requests/summary").with(user(REQUESTER).roles("Requester")))
                .andExpect(status().isForbidden());
        mvc.perform(get("/api/v1/requests/summary").with(user(STAFF).roles("Staff")))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("TC-D-INT-02b Management reads the summary with the FR012 counts (200)")
    void managementReadsSummary() throws Exception {
        mvc.perform(get("/api/v1/requests/summary").with(user(MANAGEMENT).roles("Management")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.open").value(2))
                .andExpect(jsonPath("$.closed").value(1))
                .andExpect(jsonPath("$.resolved").value(0))
                // Open and 10 days old -> overdue. Closed 20 days old -> not overdue.
                .andExpect(jsonPath("$.overdue").value(1));
    }

    @Test
    @DisplayName("TC-D-INT-02c Staff cannot submit a request (403)")
    void staffCannotSubmit() throws Exception {
        mvc.perform(post("/api/v1/requests").with(user(STAFF).roles("Staff"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"x\",\"description\":\"y\",\"category\":\"Other\"}"))
                .andExpect(status().isForbidden());
    }

    // ---------- TC-D-INT-03 (ASR-01): a requester cannot get another requester's data ----------

    @Test
    @DisplayName("TC-D-INT-03a Requester opening another requester's request by id returns 403")
    void requesterCannotOpenOthersRequest() throws Exception {
        mvc.perform(get("/api/v1/requests/" + otherRequesterOwn.getId()).with(user(REQUESTER).roles("Requester")))
                .andExpect(status().isForbidden());
        mvc.perform(get("/api/v1/requests/" + requesterOwn.getId()).with(user(REQUESTER).roles("Requester")))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("TC-D-INT-03b Requester list holds only the requester's own requests")
    void requesterListIsScoped() throws Exception {
        Long requesterId = users.findByEmail(REQUESTER).orElseThrow().getId();
        mvc.perform(get("/api/v1/requests").with(user(REQUESTER).roles("Requester")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].submittedById", everyItem(is(requesterId.intValue()))))
                .andExpect(jsonPath("$[*].id", not(hasItem(otherRequesterOwn.getId().intValue()))));
    }

    // ---------- FR005: staff scope through the API ----------

    @Test
    @DisplayName("TC-D-INT-04 Staff list holds Open requests and own assigned work, not closed work of others")
    void staffListIsScoped() throws Exception {
        mvc.perform(get("/api/v1/requests").with(user(STAFF).roles("Staff")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].status", everyItem(is("Open"))))
                .andExpect(jsonPath("$.length()").value(2));
    }
}
