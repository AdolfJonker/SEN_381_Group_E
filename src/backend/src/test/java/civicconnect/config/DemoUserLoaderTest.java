package civicconnect.config;

import civicconnect.records.User;
import civicconnect.repositories.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * RISK-004: demo accounts are only created when switched on, and never with a hard-coded password.
 * TC-D-08 to TC-D-10.
 */
class DemoUserLoaderTest {

    private final UserRepository users = mock(UserRepository.class);
    private final PasswordEncoder encoder = mock(PasswordEncoder.class);

    @Test
    @DisplayName("TC-D-08 Demo users switched off (postgres/staging default): no accounts are created")
    void disabledCreatesNoAccounts() {
        new DemoUserLoader(users, encoder, false, "anything").run(null);

        verifyNoInteractions(users);
        verifyNoInteractions(encoder);
    }

    @Test
    @DisplayName("TC-D-09 Demo users switched on without a password: start-up stops, nothing is saved")
    void enabledWithoutPasswordFailsClosed() {
        DemoUserLoader blank = new DemoUserLoader(users, encoder, true, "  ");
        DemoUserLoader missing = new DemoUserLoader(users, encoder, true, null);

        assertThrows(IllegalStateException.class, () -> blank.run(null));
        assertThrows(IllegalStateException.class, () -> missing.run(null));
        verify(users, never()).save(any());
    }

    @Test
    @DisplayName("TC-D-10 Demo users switched on: three accounts saved with the configured password")
    void enabledUsesConfiguredPassword() {
        when(users.findByEmail(anyString())).thenReturn(Optional.empty());
        when(encoder.encode("from-env-secret")).thenReturn("hashed");

        new DemoUserLoader(users, encoder, true, "from-env-secret").run(null);

        verify(encoder, times(3)).encode("from-env-secret");
        verify(users, times(3)).save(any(User.class));
    }
}
