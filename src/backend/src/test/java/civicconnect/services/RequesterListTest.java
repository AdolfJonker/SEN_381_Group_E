package civicconnect.services;

import civicconnect.api.dto.ServiceRequestResponse;
import civicconnect.records.RequestStatus;
import civicconnect.records.ServiceRequest;
import civicconnect.records.User;
import civicconnect.repositories.ServiceRequestRepository;
import civicconnect.repositories.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * Unit tests for the requester list (FR003, NFR003). No Spring context and no database.
 */
@ExtendWith(MockitoExtension.class)
class RequesterListTest {

    private static final String EMAIL = "requester@civicconnect.local";

    @Mock
    private ServiceRequestRepository requestRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ServiceRequestService service;

    private static ServiceRequest request(long id, String title, String category,
                                          RequestStatus status, Instant createdAt) {
        ServiceRequest request = new ServiceRequest();
        request.setId(id);
        request.setTitle(title);
        request.setCategory(category);
        request.setStatus(status);
        request.setCreatedAt(createdAt);
        return request;
    }

    private static User userWithId(long id) {
        User user = mock(User.class);
        when(user.getId()).thenReturn(id);
        return user;
    }

    @DisplayName("TC-E-12 list shows request ID, category, date submitted and status (FR003)")
    @Test
    void listShowsRequiredFields() {
        Instant created = Instant.parse("2026-10-13T10:00:00Z");
        User requester = userWithId(7L);
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(requester));
        when(requestRepository.findBySubmittedById(7L)).thenReturn(List.of(
                request(41L, "Broken light", "Facility Fault", RequestStatus.Assigned, created)));

        List<ServiceRequestResponse> result = service.getRequestsForRequester(EMAIL);

        assertEquals(1, result.size());
        ServiceRequestResponse item = result.get(0);
        assertEquals(41L, item.getId());
        assertEquals("Facility Fault", item.getCategory());
        assertEquals(created, item.getCreatedAt());
        assertEquals("Assigned", item.getStatus());
    }

    @DisplayName("TC-E-13 a requester only receives their own requests (NFR003)")
    @Test
    void requesterOnlyReceivesOwnRequests() {
        Instant created = Instant.parse("2026-10-13T10:00:00Z");
        User requester = userWithId(7L);
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(requester));
        when(requestRepository.findBySubmittedById(7L)).thenReturn(List.of(
                request(41L, "Mine", "Other", RequestStatus.Open, created)));
        lenient().when(requestRepository.findBySubmittedById(8L)).thenReturn(List.of(
                request(99L, "Somebody else", "Other", RequestStatus.Open, created)));

        List<ServiceRequestResponse> result = service.getRequestsForRequester(EMAIL);

        assertEquals(List.of(41L), result.stream().map(ServiceRequestResponse::getId).toList());
    }

    @DisplayName("TC-E-14 an unknown or signed out user is rejected with 401 and nothing is read (NFR003)")
    @Test
    void unknownUserIsRejected() {
        when(userRepository.findByEmail("ghost@civicconnect.local")).thenReturn(Optional.empty());

        ResponseStatusException error = assertThrows(ResponseStatusException.class,
                () -> service.getRequestsForRequester("ghost@civicconnect.local"));

        assertEquals(401, error.getStatusCode().value());
        verifyNoInteractions(requestRepository);
    }
}
