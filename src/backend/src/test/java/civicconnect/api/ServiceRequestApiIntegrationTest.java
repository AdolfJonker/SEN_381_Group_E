package civicconnect.api;

import civicconnect.records.RequestStatus;
import civicconnect.records.ServiceRequest;
import civicconnect.records.StatusHistory;
import civicconnect.repositories.ServiceRequestRepository;
import civicconnect.repositories.StatusHistoryRepository;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * API/integration tests for Adolf's M3 persistence slice (demo/H2).
 * TC-A-INT-*: real Spring context, HTTP session auth, Flyway schema.
 * INT-01 -> RTM-001; INT-02 -> RTM-017;
 * INT-03 -> RTM-008; INT-04 -> RTM-006/012/013;
 * INT-05 -> RTM-010.
 * None claim FR005/RTM-005.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("demo")
class ServiceRequestApiIntegrationTest {

    private static final String REQUESTER = "requester@civicconnect.local";
    private static final String STAFF = "staff@civicconnect.local";
    private static final String MANAGEMENT = "management@civicconnect.local";
    private static final String PASSWORD = "Password123!";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private StatusHistoryRepository statusHistoryRepository;

    @Autowired
    private ServiceRequestRepository requestRepository;

    @Test
    @DisplayName("TC-A-INT-01 (FR001/RTM-001) Submit persists a unique request ID and initial status history")
    void submitPersistsUniqueId() throws Exception {
        MockHttpSession session = login(REQUESTER);

        Long id1 = createRequest(
                session,
                "Broken projector A",
                "Room 1 projector will not power on",
                "IT Support"
        );
        Long id2 = createRequest(
                session,
                "Broken projector B",
                "Room 2 projector will not power on",
                "IT Support"
        );

        assertNotEquals(id1, id2);
        assertTrue(requestRepository.findById(id1).isPresent());
        assertTrue(requestRepository.findById(id2).isPresent());

        List<StatusHistory> history1 =
                statusHistoryRepository.findByRequestIdOrderByChangedAtAsc(id1);
        List<StatusHistory> history2 =
                statusHistoryRepository.findByRequestIdOrderByChangedAtAsc(id2);

        assertEquals(1, history1.size(), "First request should have one initial history entry");
        assertEquals(1, history2.size(), "Second request should have one initial history entry");

        assertNull(history1.get(0).getFromStatus());
        assertEquals(RequestStatus.Open, history1.get(0).getToStatus());
        assertEquals(
                REQUESTER,
                statusHistoryRepository
                        .findActorEmailsByRequestIdOrderByChangedAtAsc(id1)
                        .get(0)
        );

        assertNull(history2.get(0).getFromStatus());
        assertEquals(RequestStatus.Open, history2.get(0).getToStatus());
        assertEquals(
                REQUESTER,
                statusHistoryRepository
                        .findActorEmailsByRequestIdOrderByChangedAtAsc(id2)
                        .get(0)
        );
    }

    @Test
    @DisplayName("TC-A-INT-02 (NFR004/RTM-017) Illegal status change writes zero extra StatusHistory rows (rollback)")
    void illegalTransitionLeavesNoOrphanHistory() throws Exception {
        MockHttpSession requester = login(REQUESTER);
        MockHttpSession staff = login(STAFF);

        Long requestId = createRequest(
                requester,
                "Leaky tap",
                "Tap drips in kitchen",
                "Maintenance"
        );
        accept(staff, requestId);

        long historyBefore =
                statusHistoryRepository.findByRequestIdOrderByChangedAtAsc(requestId).size();

        mockMvc.perform(post("/api/v1/requests/" + requestId + "/status")
                        .session(staff)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"toStatus":"Closed","expectedVersion":1}
                                """))
                .andExpect(status().isBadRequest());

        long historyAfter =
                statusHistoryRepository.findByRequestIdOrderByChangedAtAsc(requestId).size();

        assertEquals(historyBefore, historyAfter);

        ServiceRequest request = requestRepository.findById(requestId).orElseThrow();
        assertEquals(RequestStatus.Assigned, request.getStatus());
    }

    @Test
    @DisplayName("TC-A-INT-03 (FR008/RTM-008, ADR-DATA-001) Two parallel updates: one 409 conflict, exactly one history row added")
    void parallelUpdatesOneConflictNoExtraHistory() throws Exception {
        MockHttpSession requester = login(REQUESTER);
        MockHttpSession staff = login(STAFF);

        Long requestId = createRequest(
                requester,
                "Parallel lock test",
                "Concurrency check for version",
                "IT Support"
        );

        JsonNode accepted = accept(staff, requestId);
        int version = accepted.get("version").asInt();

        long historyBefore =
                statusHistoryRepository.findByRequestIdOrderByChangedAtAsc(requestId).size();

        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);
        AtomicInteger success = new AtomicInteger();
        AtomicInteger conflict = new AtomicInteger();
        AtomicInteger other = new AtomicInteger();

        ExecutorService pool = Executors.newFixedThreadPool(2);

        try {
            List<Future<?>> futures = new ArrayList<>();

            for (int i = 0; i < 2; i++) {
                futures.add(pool.submit(() -> {
                    try {
                        ready.countDown();
                        start.await(5, TimeUnit.SECONDS);

                        // Fresh login per thread so sessions are not shared across threads.
                        MockHttpSession threadStaff = login(STAFF);

                        MvcResult result = mockMvc.perform(
                                        post("/api/v1/requests/" + requestId + "/status")
                                                .session(threadStaff)
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content("""
                                                        {"toStatus":"InProgress","expectedVersion":%d}
                                                        """.formatted(version)))
                                .andReturn();

                        int code = result.getResponse().getStatus();

                        if (code == 200) {
                            success.incrementAndGet();
                        } else if (code == 409) {
                            conflict.incrementAndGet();
                        } else {
                            other.incrementAndGet();
                        }
                    } catch (Exception ex) {
                        other.incrementAndGet();
                    }
                }));
            }

            assertTrue(ready.await(5, TimeUnit.SECONDS));
            start.countDown();

            for (Future<?> future : futures) {
                future.get(10, TimeUnit.SECONDS);
            }
        } finally {
            pool.shutdownNow();
        }

        assertEquals(1, success.get(), "exactly one update should succeed");
        assertEquals(1, conflict.get(), "exactly one update should conflict");
        assertEquals(0, other.get(), "no unexpected status codes");

        long historyAfter =
                statusHistoryRepository.findByRequestIdOrderByChangedAtAsc(requestId).size();

        assertEquals(historyBefore + 1, historyAfter);
        assertEquals(
                RequestStatus.InProgress,
                requestRepository.findById(requestId).orElseThrow().getStatus()
        );
    }

    @Test
    @DisplayName("TC-A-INT-04 (FR006/RTM-006, FR012/RTM-012, FR013/RTM-013 — not RTM-005) list filters + summary")
    void listFiltersAndSummary() throws Exception {
        MockHttpSession requester = login(REQUESTER);
        MockHttpSession staff = login(STAFF);
        MockHttpSession management = login(MANAGEMENT);

        Long openIt = createRequest(
                requester,
                "WiFi down in lab",
                "No connectivity",
                "IT Support"
        );
        Long openMaint = createRequest(
                requester,
                "Broken chair",
                "Chair in hall B",
                "Maintenance"
        );

        accept(staff, openIt);

        mockMvc.perform(get("/api/v1/requests")
                        .session(staff)
                        .param("status", "Open")
                        .param("category", "Maintenance")
                        .param("sort", "desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.id==" + openMaint + ")]").exists())
                .andExpect(jsonPath("$[?(@.id==" + openIt + ")]").doesNotExist());

        mockMvc.perform(get("/api/v1/requests")
                        .session(staff)
                        .param("search", String.valueOf(openMaint)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(openMaint.intValue()));

        mockMvc.perform(get("/api/v1/requests/summary").session(management))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.open").isNumber())
                .andExpect(jsonPath("$.assigned").isNumber())
                .andExpect(jsonPath("$.inProgress").isNumber())
                .andExpect(jsonPath("$.resolved").isNumber())
                .andExpect(jsonPath("$.closed").isNumber())
                .andExpect(jsonPath("$.overdue").isNumber());

        mockMvc.perform(get("/api/v1/requests/summary").session(staff))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("TC-A-INT-05 (FR010/RTM-010) Staff comment is saved and shown on detail; requester and blank comments rejected")
    void staffCommentVisibleOnDetail() throws Exception {
        MockHttpSession requester = login(REQUESTER);
        MockHttpSession staff = login(STAFF);

        Long requestId = createRequest(requester, "Printer jam", "Printer on floor 2 jams on every page", "IT Support");
        accept(staff, requestId);

        mockMvc.perform(post("/api/v1/requests/" + requestId + "/comments")
                        .session(staff)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"content":"Replaced the roller; testing now."}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.comments.length()").value(1))
                .andExpect(jsonPath("$.comments[0].content").value("Replaced the roller; testing now."))
                .andExpect(jsonPath("$.comments[0].authorName").isNotEmpty())
                .andExpect(jsonPath("$.comments[0].createdAt").isNotEmpty());

        mockMvc.perform(get("/api/v1/requests/" + requestId).session(staff))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.comments.length()").value(1))
                .andExpect(jsonPath("$.comments[0].content").value("Replaced the roller; testing now."));

        mockMvc.perform(post("/api/v1/requests/" + requestId + "/comments")
                        .session(requester)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"content":"Requester should not be able to add this."}
                                """))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/v1/requests/" + requestId + "/comments")
                        .session(staff)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"content":"   "}
                                """))
                .andExpect(status().isBadRequest());

        mockMvc.perform(get("/api/v1/requests/" + requestId).session(staff))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.comments.length()").value(1));
    }

    private MockHttpSession login(String email) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"%s","password":"%s"}
                                """.formatted(email, PASSWORD)))
                .andExpect(status().isOk())
                .andReturn();

        return (MockHttpSession) result.getRequest().getSession(false);
    }

    private Long createRequest(
            MockHttpSession session,
            String title,
            String description,
            String category
    ) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/requests")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":%s,"description":%s,"category":%s}
                                """.formatted(
                                objectMapper.writeValueAsString(title),
                                objectMapper.writeValueAsString(description),
                                objectMapper.writeValueAsString(category)
                        )))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andReturn();

        return objectMapper.readTree(result.getResponse().getContentAsString())
                .get("id")
                .asLong();
    }

    private JsonNode accept(MockHttpSession staffSession, Long requestId) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/requests/" + requestId + "/accept")
                        .session(staffSession)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"version":0}
                                """))
                .andExpect(status().isOk())
                .andReturn();

        return objectMapper.readTree(result.getResponse().getContentAsString());
    }
}