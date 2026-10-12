package civicconnect.api.dto;

import java.util.List;

public class ErrorResponse {

    private final String message;
    private final List<String> details;

    public ErrorResponse(String message, List<String> details) {
        this.message = message;
        this.details = details;
    }

    public static ErrorResponse of(String message) {
        return new ErrorResponse(message, List.of());
    }

    public String getMessage() {
        return message;
    }

    public List<String> getDetails() {
        return details;
    }
}