package civicconnect.services;

import civicconnect.api.dto.SummaryResponse;
import civicconnect.records.RequestStatus;
import civicconnect.repositories.ServiceRequestRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.EnumSet;

/**
 * FR012: aggregate service activity summary for Management. Access is restricted to Management in SecurityConfig.
 */
@Service
public class RequestSummaryService {

    static final Duration OVERDUE_AFTER = Duration.ofDays(7);

    private final ServiceRequestRepository requestRepository;

    public RequestSummaryService(ServiceRequestRepository requestRepository) {
        this.requestRepository = requestRepository;
    }

    @Transactional(readOnly = true)
    public SummaryResponse summary() {
        Instant cutoff = Instant.now().minus(OVERDUE_AFTER);
        long overdue = requestRepository.countByStatusInAndCreatedAtBefore(
                EnumSet.of(RequestStatus.Open, RequestStatus.Assigned, RequestStatus.InProgress),
                cutoff
        );
        return new SummaryResponse(
                requestRepository.countByStatus(RequestStatus.Open),
                requestRepository.countByStatus(RequestStatus.Assigned),
                requestRepository.countByStatus(RequestStatus.InProgress),
                requestRepository.countByStatus(RequestStatus.Resolved),
                requestRepository.countByStatus(RequestStatus.Closed),
                overdue
        );
    }
}
