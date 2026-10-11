package civicconnect.security;

import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Blocks repeated failed logins for the same email to slow down guessing attacks.
 */
@Service
public class LoginAttemptService {

    private static final int MAX_ATTEMPTS = 5;
    private static final Duration LOCK_DURATION = Duration.ofMinutes(15);

    private final Map<String, AttemptState> attempts = new ConcurrentHashMap<>();

    public boolean isBlocked(String email) {
        AttemptState state = attempts.get(normalize(email));
        if (state == null) {
            return false;
        }
        if (state.lockedUntil != null && Instant.now().isBefore(state.lockedUntil)) {
            return true;
        }
        if (state.lockedUntil != null && Instant.now().isAfter(state.lockedUntil)) {
            attempts.remove(normalize(email));
        }
        return false;
    }

    public void loginSucceeded(String email) {
        attempts.remove(normalize(email));
    }

    public void loginFailed(String email) {
        String key = normalize(email);
        AttemptState state = attempts.computeIfAbsent(key, ignored -> new AttemptState());
        state.failures += 1;
        if (state.failures >= MAX_ATTEMPTS) {
            state.lockedUntil = Instant.now().plus(LOCK_DURATION);
        }
    }

    private static String normalize(String email) {
        return email == null ? "" : email.trim().toLowerCase();
    }

    private static final class AttemptState {
        private int failures;
        private Instant lockedUntil;
    }
}