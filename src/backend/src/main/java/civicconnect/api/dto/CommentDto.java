package civicconnect.api.dto;

import civicconnect.records.Comment;

import java.time.Instant;

public record CommentDto(
        Long id,
        String authorName,
        String content,
        Instant createdAt
) {
    public static CommentDto from(Comment comment) {
        return new CommentDto(
                comment.getId(),
                comment.getAuthor().getName(),
                comment.getContent(),
                comment.getCreatedAt()
        );
    }
}
