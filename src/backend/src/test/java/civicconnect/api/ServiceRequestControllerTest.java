package civicconnect.api;

import civicconnect.api.dto.CreateRequestBody;
import civicconnect.api.dto.RequestResponse;
import civicconnect.services.ServiceRequestService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ServiceRequestControllerTest {

    @Mock
    private ServiceRequestService requestService;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private ServiceRequestController controller;

    @Test
    @DisplayName("POST /api/v1/requests returns 201 and the created response")
    void createReturnsCreated() {
        when(authentication.getName()).thenReturn("requester@civicconnect.local");

        RequestResponse response = mock(RequestResponse.class);
        when(response.id()).thenReturn(1L);

        when(requestService.create(eq("requester@civicconnect.local"), any(CreateRequestBody.class)))
                .thenReturn(response);

        CreateRequestBody body = new CreateRequestBody("Broken tap", "Dripping", "Maintenance");
        ResponseEntity<RequestResponse> result = controller.create(body, authentication);

        assertEquals(HttpStatus.CREATED, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals(1L, result.getBody().id());
        verify(requestService).create("requester@civicconnect.local", body);
    }
}
