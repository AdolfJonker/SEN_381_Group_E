package civicconnect.api;

import civicconnect.api.dto.AcceptRequestBody;
import civicconnect.api.dto.CreateCommentBody;
import civicconnect.api.dto.CreateRequestBody;
import civicconnect.api.dto.RequestResponse;
import civicconnect.api.dto.ServiceRequestDetailDto;
import civicconnect.services.ServiceRequestService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Write paths for service requests (create, accept, comments).
 * Status changes live in {@link RequestStatusController}.
 * GET list / detail / summary live in {@link RequestViewController} (access-control slice).
 */
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

    @PostMapping("/{id}/accept")
    public ServiceRequestDetailDto accept(
            @PathVariable Long id,
            @RequestBody(required = false) AcceptRequestBody body,
            Authentication authentication
    ) {
        Integer version = body == null ? null : body.version();
        return requestService.accept(authentication.getName(), id, version);
    }

    @PostMapping("/{id}/comments")
    public ServiceRequestDetailDto addComment(
            @PathVariable Long id,
            @Valid @RequestBody CreateCommentBody body,
            Authentication authentication
    ) {
        return requestService.addComment(authentication.getName(), id, body);
    }
}
