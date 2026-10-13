package civicconnect.api.dto;

import civicconnect.records.RequestStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateStatusBody(
        @NotNull(message = "Choose a status.")
        RequestStatus status,
        Integer version
) {
}
