
package civicconnect.repositories;

import civicconnect.records.StatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface StatusHistoryRepository extends JpaRepository<StatusHistory, Long> {

    List<StatusHistory> findByRequestIdOrderByChangedAtAsc(Long requestId);

    @Query("""
            SELECT sh.changedBy.email
            FROM StatusHistory sh
            WHERE sh.request.id = :requestId
            ORDER BY sh.changedAt ASC
            """)
    List<String> findActorEmailsByRequestIdOrderByChangedAtAsc(
            @Param("requestId") Long requestId
    );
}
