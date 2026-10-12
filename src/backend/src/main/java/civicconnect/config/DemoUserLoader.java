package civicconnect.config;

import civicconnect.records.Role;
import civicconnect.records.User;
import civicconnect.repositories.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DemoUserLoader implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DemoUserLoader.class);
    private static final String DEMO_PASSWORD = "Password123!";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DemoUserLoader(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(ApplicationArguments args) {
        ensureUser("requester@civicconnect.local", "Demo Requester", Role.Requester);
        ensureUser("staff@civicconnect.local", "Demo Staff", Role.Staff);
        ensureUser("management@civicconnect.local", "Demo Management", Role.Management);
        log.info("Demo login accounts ready. Password for all demo users: {}", DEMO_PASSWORD);
    }

    private void ensureUser(String email, String name, Role role) {
        userRepository.findByEmail(email).ifPresentOrElse(existing -> {
            // Keep existing row; refresh hash so local demo login always works.
            existing.setPasswordHash(passwordEncoder.encode(DEMO_PASSWORD));
            existing.setRole(role);
            existing.setName(name);
            userRepository.save(existing);
        }, () -> {
            User user = new User();
            user.setEmail(email);
            user.setName(name);
            user.setRole(role);
            user.setPasswordHash(passwordEncoder.encode(DEMO_PASSWORD));
            userRepository.save(user);
        });
    }
}