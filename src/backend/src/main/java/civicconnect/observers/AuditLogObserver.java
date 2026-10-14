package civicconnect.observers;

import civicconnect.records.RequestStatus;
import civicconnect.records.ServiceRequest;
import civicconnect.records.StatusHistory;
import civicconnect.records.User;
import civicconnect.repositories.StatusHistoryRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

/**
 * Writes one StatusHistory row for a status change (NFR004, ADR-DESIGN-001).
 * MANDATORY means it can only run inside the status change transaction, so a history row
 * can never be written for a change that did not commit (ADR-DATA-001).
 */
@Component
public class AuditLogObserver {

    private final StatusHistoryRepository historyRepository;

    public AuditLogObserver(StatusHistoryRepository historyRepository) {
        this.historyRepository = historyRepository;
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public StatusHistory record(ServiceRequest request,
                                RequestStatus from,
                                RequestStatus to,
                                User actor,
                                String note) {
        StatusHistory row = new StatusHistory();
        row.setRequest(request);
        row.setFromStatus(from);
        row.setToStatus(to);
        row.setChangedBy(actor);
        row.setChangedAt(Instant.now());
        row.setNote(note);
        return historyRepository.save(row);
    }
}
