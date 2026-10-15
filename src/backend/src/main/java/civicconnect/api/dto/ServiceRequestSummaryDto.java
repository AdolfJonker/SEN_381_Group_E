package civicconnect.api.dto;

import civicconnect.records.RequestStatus;
import civicconnect.records.ServiceRequest;
import civicconnect.records.User;

import java.time.Instant;

public record ServiceRequestSummaryDto(
        Long id,
        String title,
        String category,
        RequestStatus status,
        Instant createdAt,
        Long assignedToId,
        String assignedToName
) {
    public static ServiceRequestSummaryDto from(ServiceRequest request) {
        User assignedTo = request.getAssignedTo();
        return new ServiceRequestSummaryDto(
                request.getId(),
                request.getTitle(),
                request.getCategory(),
                request.getStatus(),
                request.getCreatedAt(),
                assignedTo == null ? null : assignedTo.getId(),
                assignedTo == null ? null : assignedTo.getName()
        );
    }
}
