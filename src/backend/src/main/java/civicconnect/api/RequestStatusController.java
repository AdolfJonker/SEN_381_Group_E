package civicconnect.api;

import civicconnect.api.dto.ChangeStatusBody;
import civicconnect.api.dto.StatusChangeResponse;
import civicconnect.services.RequestStatusService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/v1/requests")
public class RequestStatusController {

    private final RequestStatusService requestStatusService;

    public RequestStatusController(RequestStatusService requestStatusService) {
        this.requestStatusService = requestStatusService;
    }

    @PostMapping("/{id}/status")
    public StatusChangeResponse change(@PathVariable Long id,
                                       @Valid @RequestBody ChangeStatusBody body,
                                       Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not signed in");
        }
        return requestStatusService.changeStatus(
                authentication.getName(), id, body.toStatus(), body.expectedVersion(), body.note());
    }
}
