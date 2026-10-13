package civicconnect.api.dto;

import civicconnect.records.RequestStatus;
import civicconnect.records.ServiceRequest;

import java.time.Instant;

public record RequestResponse(
        Long id,
        String title,
        String description,
        String category,
        RequestStatus status,
        Long submittedById,
        Instant createdAt,
        Instant updatedAt,
        Integer version
) {
    public static RequestResponse from(ServiceRequest request) {
        return new RequestResponse(
                request.getId(),
                request.getTitle(),
                request.getDescription(),
                request.getCategory(),
                request.getStatus(),
                request.getSubmittedBy().getId(),
                request.getCreatedAt(),
                request.getUpdatedAt(),
                request.getVersion()
        );
    }
}