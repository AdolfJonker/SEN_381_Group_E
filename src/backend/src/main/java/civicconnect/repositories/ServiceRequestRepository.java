package civicconnect.repositories;

import civicconnect.records.RequestStatus;
import civicconnect.records.ServiceRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.Collection;
import java.util.List;

public interface ServiceRequestRepository extends JpaRepository<ServiceRequest, Long> {
    List<ServiceRequest> findBySubmittedById(Long submittedById);

    List<ServiceRequest> findByAssignedToId(Long assignedToId);

    // FR005 staff scope: assigned to this staff member, or still Open.
    List<ServiceRequest> findByAssignedToIdOrStatus(Long assignedToId, RequestStatus status);

    // FR012 summary counts.
    long countByStatus(RequestStatus status);

    long countByStatusInAndCreatedAtBefore(Collection<RequestStatus> statuses, Instant cutoff);
}
