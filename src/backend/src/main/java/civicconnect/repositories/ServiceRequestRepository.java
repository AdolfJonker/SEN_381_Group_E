package civicconnect.repositories;

import civicconnect.records.RequestStatus;
import civicconnect.records.ServiceRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ServiceRequestRepository extends JpaRepository<ServiceRequest, Long> {
    List<ServiceRequest> findBySubmittedById(Long submittedById);

    List<ServiceRequest> findByAssignedToId(Long assignedToId);

    @Query("""
            select r from ServiceRequest r
            left join fetch r.assignedTo
            where r.status = :openStatus
               or r.assignedTo.id = :staffId
            """)
    List<ServiceRequest> findVisibleToStaff(
            @Param("openStatus") RequestStatus openStatus,
            @Param("staffId") Long staffId
    );
}
