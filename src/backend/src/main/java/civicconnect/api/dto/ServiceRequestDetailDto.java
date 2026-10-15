package civicconnect.api.dto;

import civicconnect.records.RequestStatus;
import civicconnect.records.ServiceRequest;
import civicconnect.records.User;

import java.time.Instant;
import java.util.List;

public record ServiceRequestDetailDto(
        Long id,
        String title,
        String description,
        String category,
        RequestStatus status,
        String submittedByName,
        Long assignedToId,
        String assignedToName,
        Instant createdAt,
        Instant updatedAt,
        Integer version,
        List<StatusHistoryDto> history,
        List<CommentDto> comments
) {
    public static ServiceRequestDetailDto from(
            ServiceRequest request,
            List<StatusHistoryDto> history,
            List<CommentDto> comments
    ) {
        User assignedTo = request.getAssignedTo();
        return new ServiceRequestDetailDto(
                request.getId(),
                request.getTitle(),
                request.getDescription(),
                request.getCategory(),
                request.getStatus(),
                request.getSubmittedBy().getName(),
                assignedTo == null ? null : assignedTo.getId(),
                assignedTo == null ? null : assignedTo.getName(),
                request.getCreatedAt(),
                request.getUpdatedAt(),
                request.getVersion(),
                history,
                comments
        );
    }
}
