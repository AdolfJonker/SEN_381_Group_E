package civicconnect.config;

import civicconnect.records.Role;
import civicconnect.records.User;
import civicconnect.repositories.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Creates the three demo accounts (RISK-004).
 * Only runs when civicconnect.demo-users.enabled is true. That is the default on the demo
 * profile (in-memory H2) and off on every other profile, including postgres and staging.
 * The password comes from configuration (DEMO_PASSWORD). It is never hard-coded in this class
 * and never written to the log. If demo users are enabled without a password, start-up stops.
 */
@Component
public class DemoUserLoader implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DemoUserLoader.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final boolean enabled;
    private final String demoPassword;

    public DemoUserLoader(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            @Value("${civicconnect.demo-users.enabled:false}") boolean enabled,
            @Value("${civicconnect.demo-users.password:}") String demoPassword
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.enabled = enabled;
        this.demoPassword = demoPassword;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (!enabled) {
            log.info("Demo login accounts are disabled on this profile.");
            return;
        }
        if (demoPassword == null || demoPassword.isBlank()) {
            throw new IllegalStateException(
                    "Demo users are enabled but no password is set. Set DEMO_PASSWORD "
                            + "(civicconnect.demo-users.password) or turn demo users off.");
        }
        ensureUser("requester@civicconnect.local", "Demo Requester", Role.Requester);
        ensureUser("staff@civicconnect.local", "Demo Staff", Role.Staff);
        ensureUser("management@civicconnect.local", "Demo Management", Role.Management);
        log.info("Demo login accounts ready (password from configuration, not logged).");
    }

    private void ensureUser(String email, String name, Role role) {
        userRepository.findByEmail(email).ifPresentOrElse(existing -> {
            // Keep existing row; refresh hash so the configured demo password always works.
            existing.setPasswordHash(passwordEncoder.encode(demoPassword));
            existing.setRole(role);
            existing.setName(name);
            userRepository.save(existing);
        }, () -> {
            User user = new User();
            user.setEmail(email);
            user.setName(name);
            user.setRole(role);
            user.setPasswordHash(passwordEncoder.encode(demoPassword));
            userRepository.save(user);
        });
    }
}
