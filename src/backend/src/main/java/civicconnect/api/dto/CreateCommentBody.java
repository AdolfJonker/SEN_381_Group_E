package civicconnect.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateCommentBody(
        @NotBlank(message = "Write a comment before saving.")
        @Size(max = 2000, message = "Comment must be at most 2000 characters.")
        String content
) {
    public CreateCommentBody {
        content = content == null ? null : content.strip();
    }
}
