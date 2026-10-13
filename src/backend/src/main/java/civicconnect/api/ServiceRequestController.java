package civicconnect.api;

import civicconnect.api.dto.CreateRequestBody;
import civicconnect.api.dto.RequestResponse;
import civicconnect.services.ServiceRequestService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/requests")
public class ServiceRequestController {

    private final ServiceRequestService requestService;

    public ServiceRequestController(ServiceRequestService requestService) {
        this.requestService = requestService;
    }

    @PostMapping
    public ResponseEntity<RequestResponse> create(
            @Valid @RequestBody CreateRequestBody body,
            Authentication authentication
    ) {
        RequestResponse created = requestService.create(authentication.getName(), body);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
}