package civicconnect.repositories;

import civicconnect.records.ServiceRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ServiceRequestRepository extends JpaRepository<ServiceRequest, Long> {
    List<ServiceRequest> findBySubmittedById(Long submittedById);

    List<ServiceRequest> findByAssignedToId(Long assignedToId);
}
