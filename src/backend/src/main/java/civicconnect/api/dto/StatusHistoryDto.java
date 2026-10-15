package civicconnect.api.dto;

import civicconnect.records.RequestStatus;
import civicconnect.records.StatusHistory;

import java.time.Instant;

public record StatusHistoryDto(
        Long id,
        RequestStatus fromStatus,
        RequestStatus toStatus,
        String changedByName,
        Instant changedAt,
        String note
) {
    public static StatusHistoryDto from(StatusHistory history) {
        return new StatusHistoryDto(
                history.getId(),
                history.getFromStatus(),
                history.getToStatus(),
                history.getChangedBy().getName(),
                history.getChangedAt(),
                history.getNote()
        );
    }
}
