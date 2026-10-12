package civicconnect.api.dto;

public class LoginResponse {

    private final UserResponse user;
    private final String message;

    public LoginResponse(UserResponse user, String message) {
        this.user = user;
        this.message = message;
    }

    public UserResponse getUser() {
        return user;
    }

    public String getMessage() {
        return message;
    }
}