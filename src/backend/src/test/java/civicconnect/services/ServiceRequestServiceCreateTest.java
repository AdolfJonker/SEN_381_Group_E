package civicconnect.services;

import civicconnect.api.dto.CreateRequestBody;
import civicconnect.api.dto.RequestResponse;
import civicconnect.observers.AuditLogObserver;
import civicconnect.records.RequestStatus;
import civicconnect.records.ServiceRequest;
import civicconnect.records.User;
import civicconnect.repositories.ServiceRequestRepository;
import civicconnect.repositories.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ServiceRequestServiceCreateTest {

    @Mock
    private ServiceRequestRepository requestRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AuditLogObserver auditLogObserver;

    @InjectMocks
    private ServiceRequestService service;

    private User requester() {
        User u = new User();
        u.setId(1L);
        u.setEmail("requester@civicconnect.local");
        return u;
    }

    @Test
    @DisplayName("TC-E-07 Valid category is accepted and request is saved with initial history")
    void validCategoryIsAccepted() {
        User requester = requester();

        when(userRepository.findByEmail("requester@civicconnect.local"))
                .thenReturn(Optional.of(requester));

        when(requestRepository.save(any(ServiceRequest.class)))
                .thenAnswer(inv -> {
                    ServiceRequest r = inv.getArgument(0);
                    r.setId(42L);
                    return r;
                });

        CreateRequestBody body =
                new CreateRequestBody("Broken tap", "Dripping", "Maintenance");

        RequestResponse result =
                service.create("requester@civicconnect.local", body);

        assertEquals(42L, result.id());

        ArgumentCaptor<ServiceRequest> captor =
                ArgumentCaptor.forClass(ServiceRequest.class);

        verify(requestRepository).save(captor.capture());

        ServiceRequest savedRequest = captor.getValue();

        assertEquals("Maintenance", savedRequest.getCategory());
        assertEquals(RequestStatus.Open, savedRequest.getStatus());

        verify(auditLogObserver).record(
                savedRequest,
                null,
                RequestStatus.Open,
                requester,
                "Initial status"
        );
    }

    @Test
    @DisplayName("TC-E-10 Category not on the list is rejected")
    void unknownCategoryIsRejected() {
        CreateRequestBody body =
                new CreateRequestBody("Broken tap", "Dripping", "Plumbing");

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> service.create("requester@civicconnect.local", body)
        );

        assertEquals(400, ex.getStatusCode().value());
        assertTrue(ex.getReason().contains("category"));

        verify(requestRepository, never()).save(any());
        verifyNoInteractions(auditLogObserver);
    }

    @Test
    @DisplayName("Unknown user is rejected with 401")
    void unknownUserIsRejected() {
        when(userRepository.findByEmail("unknown@example.com"))
                .thenReturn(Optional.empty());

        CreateRequestBody body =
                new CreateRequestBody("Broken tap", "Dripping", "Maintenance");

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> service.create("unknown@example.com", body)
        );

        assertEquals(401, ex.getStatusCode().value());

        verify(requestRepository, never()).save(any());
        verifyNoInteractions(auditLogObserver);
    }
}