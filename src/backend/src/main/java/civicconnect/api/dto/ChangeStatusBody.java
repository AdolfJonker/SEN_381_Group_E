package civicconnect.api.dto;

import civicconnect.records.RequestStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Body of POST /api/v1/requests/{id}/status.
 * expectedVersion is the version the screen last saw, so a stale screen gets 409 (ADR-DATA-001).
 */
public record ChangeStatusBody(
        @NotNull(message = "Choose the status to move to.") RequestStatus toStatus,
        Integer expectedVersion,
        @Size(max = 1000, message = "The note must be 1000 characters or fewer.") String note
) {
}
