package civicconnect.api;

import civicconnect.api.dto.ServiceRequestResponse;
import civicconnect.services.ServiceRequestService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/v1/requests")
public class RequesterRequestController {

    private final ServiceRequestService serviceRequestService;

    public RequesterRequestController(ServiceRequestService serviceRequestService) {
        this.serviceRequestService = serviceRequestService;
    }

    @GetMapping("/mine")
    public List<ServiceRequestResponse> mine(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not signed in");
        }
        return serviceRequestService.getRequestsForRequester(authentication.getName());
    }
}
