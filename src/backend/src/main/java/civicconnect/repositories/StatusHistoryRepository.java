package civicconnect.repositories;

import civicconnect.records.StatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StatusHistoryRepository extends JpaRepository<StatusHistory, Long> {
    List<StatusHistory> findByRequestIdOrderByChangedAtAsc(Long requestId);
}
