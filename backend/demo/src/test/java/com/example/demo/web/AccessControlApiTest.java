package com.example.demo.web;

import com.example.demo.domain.Role;
import com.example.demo.domain.UserAccount;
import com.example.demo.repo.UserAccountRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.is;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * API / negative tests for Donya's slice (NFR003, ASR-01, RISK-003, RISK-004).
 * Seed data (DemoDataLoader): Aisha = Requester (owns SR-1001..SR-1003),
 * Johan = Staff, Thandi = Management.
 * The password below is a test-only value, not a real credential.
 */
@SpringBootTest(properties = {
		"civicconnect.demo-password=test-only-password",
		// Own in-memory database, so seed users are created with the test password
		// even if another test context already seeded the default database.
		"spring.datasource.url=jdbc:h2:mem:access_control_test;MODE=PostgreSQL;DB_CLOSE_DELAY=-1;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH"
})
@ActiveProfiles("demo")
@AutoConfigureMockMvc
@Transactional
class AccessControlApiTest {

	private static final String TEST_PASSWORD = "test-only-password";

	@Autowired
	private MockMvc mvc;

	@Autowired
	private UserAccountRepository users;

	@Autowired
	private PasswordEncoder passwordEncoder;

	// ---------- TC-API-SEC-01: unauthenticated requests are rejected (401) ----------

	@Test
	@DisplayName("TC-API-SEC-01a: no credentials on a data route returns 401")
	void unauthenticatedListIsRejected() throws Exception {
		mvc.perform(get("/api/requests"))
				.andExpect(status().isUnauthorized());
	}

	@Test
	@DisplayName("TC-API-SEC-01b: wrong password on login returns 401")
	void wrongPasswordIsRejected() throws Exception {
		mvc.perform(get("/api/auth/me").with(httpBasic("thandi@campus.edu", "wrong-password")))
				.andExpect(status().isUnauthorized());
	}

	@Test
	@DisplayName("TC-API-SEC-01c: correct login returns the user's role for the landing view")
	void correctLoginReturnsRole() throws Exception {
		mvc.perform(get("/api/auth/me").with(httpBasic("thandi@campus.edu", TEST_PASSWORD)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.role").value("Management"))
				.andExpect(jsonPath("$.email").value("thandi@campus.edu"));
	}

	@Test
	@DisplayName("TC-API-SEC-01d: the old user list endpoint is no longer reachable")
	void userListIsNotExposed() throws Exception {
		mvc.perform(get("/api/users").with(httpBasic("aisha@campus.edu", TEST_PASSWORD)))
				.andExpect(status().is4xxClientError());
	}

	// ---------- TC-API-SEC-02: wrong role returns 403 ----------

	@Test
	@DisplayName("TC-API-SEC-02a: Requester cannot read the Management summary (403)")
	void requesterCannotReadSummary() throws Exception {
		mvc.perform(get("/api/summary").with(user("aisha@campus.edu").roles("Requester")))
				.andExpect(status().isForbidden());
	}

	@Test
	@DisplayName("TC-API-SEC-02b: Staff cannot read the Management summary (403)")
	void staffCannotReadSummary() throws Exception {
		mvc.perform(get("/api/summary").with(user("johan@campus.edu").roles("Staff")))
				.andExpect(status().isForbidden());
	}

	@Test
	@DisplayName("TC-API-SEC-02c: Management can read the summary (200)")
	void managementCanReadSummary() throws Exception {
		mvc.perform(get("/api/summary").with(httpBasic("thandi@campus.edu", TEST_PASSWORD)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.open").isNumber())
				.andExpect(jsonPath("$.overdue").isNumber());
	}

	// ---------- TC-API-SEC-03 (ASR-01): a requester cannot see another requester's data ----------

	@Test
	@DisplayName("TC-API-SEC-03a: Requester B opening Requester A's request by id returns 403")
	void requesterCannotOpenAnotherRequestersRequest() throws Exception {
		createSecondRequester();
		mvc.perform(get("/api/requests/SR-1001").with(httpBasic("bongani@campus.edu", TEST_PASSWORD)))
				.andExpect(status().isForbidden());
	}

	@Test
	@DisplayName("TC-API-SEC-03b: Requester A's list only contains Requester A's requests")
	void requesterListOnlyContainsOwnRequests() throws Exception {
		mvc.perform(get("/api/requests").with(httpBasic("aisha@campus.edu", TEST_PASSWORD)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[*].submittedById", everyItem(is("u-req-1"))));
	}

	private void createSecondRequester() {
		users.save(new UserAccount(
				"u-req-2",
				"Bongani Dlamini",
				"bongani@campus.edu",
				passwordEncoder.encode(TEST_PASSWORD),
				Role.Requester
		));
	}
}
