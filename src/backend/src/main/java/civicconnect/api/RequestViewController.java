package civicconnect.api;

import civicconnect.api.dto.RequestListQuery;
import civicconnect.api.dto.RequestResponse;
import civicconnect.api.dto.ServiceRequestDetailDto;
import civicconnect.api.dto.SummaryResponse;
import civicconnect.services.RequestListService;
import civicconnect.services.RequestSummaryService;
import civicconnect.services.ServiceRequestService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Read-only request views (FR005, FR006, FR012, FR013, NFR003).
 * Role rules for these routes are in SecurityConfig; row scope is in RequestListService.
 */
@RestController
@RequestMapping("/api/v1/requests")
public class RequestViewController {

    private final RequestListService listService;
    private final RequestSummaryService summaryService;
    private final ServiceRequestService requestService;

    public RequestViewController(
            RequestListService listService,
            RequestSummaryService summaryService,
            ServiceRequestService requestService
    ) {
        this.listService = listService;
        this.summaryService = summaryService;
        this.requestService = requestService;
    }

    /**
     * FR005 role-scoped list. Optional query params implement FR006 / FR013
     * (search, status, category, sort) on top of the strategy scope.
     */
    @GetMapping
    public List<RequestResponse> list(
            Authentication authentication,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String sort
    ) {
        return listService.listFor(
                authentication.getName(),
                new RequestListQuery(search, status, category, sort)
        );
    }

    /** FR012: Management summary counts. Must be declared before /{id}. */
    @GetMapping("/summary")
    public SummaryResponse summary() {
        return summaryService.summary();
    }

    /**
     * Detail for staff/management UI (history + comments). Visibility uses RequestListService
     * via ServiceRequestService (NFR003, ASR-01). Path constrained to digits so /mine stays free.
     */
    @GetMapping("/{id:\\d+}")
    public ServiceRequestDetailDto get(@PathVariable Long id, Authentication authentication) {
        return requestService.get(authentication.getName(), id);
    }
}
