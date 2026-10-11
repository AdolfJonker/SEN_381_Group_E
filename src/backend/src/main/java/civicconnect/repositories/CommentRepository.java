package civicconnect.repositories;

import civicconnect.records.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByRequestIdOrderByCreatedAtAsc(Long requestId);
}
