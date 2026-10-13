package civicconnect.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateRequestBody(
        @NotBlank(message = "Title is required.")
        @Size(max = 200, message = "Title must be at most 200 characters.")
        String title,

        @NotBlank(message = "Description is required.")
        @Size(max = 2000, message = "Description must be at most 2000 characters.")
        String description,

        @NotBlank(message = "Choose a category.")
        String category
) {
    public CreateRequestBody {
        title = title == null ? null : title.strip();
        description = description == null ? null : description.strip();
    }
}