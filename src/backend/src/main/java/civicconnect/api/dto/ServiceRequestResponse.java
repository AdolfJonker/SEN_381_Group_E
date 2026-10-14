
package civicconnect.api.dto;

import civicconnect.records.ServiceRequest;
import java.time.Instant;

public class ServiceRequestResponse {

    private final Long id;
    private final String title;
    private final String category;
    private final String status;
    private final Instant createdAt;

    public ServiceRequestResponse(
            Long id,
            String title,
            String category,
            String status,
            Instant createdAt) {
        this.id = id;
        this.title = title;
        this.category = category;
        this.status = status;
        this.createdAt = createdAt;
    }

    public static ServiceRequestResponse from(ServiceRequest request) {
        return new ServiceRequestResponse(
                request.getId(),
                request.getTitle(),
                request.getCategory(),
                request.getStatus().name(),
                request.getCreatedAt()
        );
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getCategory() {
        return category;
    }

    public String getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
