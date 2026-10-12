package civicconnect.services;

import civicconnect.api.dto.LoginRequest;
import civicconnect.api.dto.LoginResponse;
import civicconnect.api.dto.UserResponse;
import civicconnect.records.User;
import civicconnect.repositories.UserRepository;
import civicconnect.security.LoginAttemptService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthService {

    private static final String GENERIC_LOGIN_ERROR = "Invalid email or password";

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final LoginAttemptService loginAttemptService;

    public AuthService(
            AuthenticationManager authenticationManager,
            UserRepository userRepository,
            LoginAttemptService loginAttemptService
    ) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.loginAttemptService = loginAttemptService;
    }

    public LoginResponse login(LoginRequest request, HttpServletRequest httpRequest) {
        String email = normalizeEmail(request.getEmail());

        if (loginAttemptService.isBlocked(email)) {
            throw new ResponseStatusException(
                    HttpStatus.TOO_MANY_REQUESTS,
                    "Too many failed login attempts. Try again in 15 minutes."
            );
        }

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, request.getPassword())
            );

            SecurityContext context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(authentication);
            SecurityContextHolder.setContext(context);

            HttpSession session = httpRequest.getSession(true);
            session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, context);

            loginAttemptService.loginSucceeded(email);

            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, GENERIC_LOGIN_ERROR));

            return new LoginResponse(UserResponse.from(user), "Signed in successfully");
        } catch (BadCredentialsException ex) {
            loginAttemptService.loginFailed(email);
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, GENERIC_LOGIN_ERROR);
        }
    }

    public UserResponse currentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not signed in");
        }

        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not signed in"));
        return UserResponse.from(user);
    }

    public void logout(HttpServletRequest httpRequest) {
        SecurityContextHolder.clearContext();
        HttpSession session = httpRequest.getSession(false);
        if (session != null) {
            session.invalidate();
        }
    }

    private static String normalizeEmail(String email) {
        return email == null ? "" : email.trim().toLowerCase();
    }
}