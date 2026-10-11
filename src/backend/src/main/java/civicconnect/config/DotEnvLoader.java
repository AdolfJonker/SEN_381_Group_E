package civicconnect.config;

import io.github.cdimascio.dotenv.Dotenv;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;

/**
 * Loads the project-root .env file into system properties before Spring Boot starts.
 * Secrets stay out of git; application.yml reads them as ${DB_USER}, ${DB_PASSWORD}, etc.
 */
public final class DotEnvLoader {

    private DotEnvLoader() {
    }

    public static void load() {
        Path envDirectory = findEnvDirectory();
        if (envDirectory == null) {
            return;
        }

        Dotenv dotenv = Dotenv.configure()
                .directory(envDirectory.toString())
                .ignoreIfMissing()
                .load();

        dotenv.entries().forEach(entry -> {
            String key = entry.getKey();
            String value = entry.getValue();
            if (System.getenv(key) == null && System.getProperty(key) == null) {
                System.setProperty(key, value);
            }
        });

        if (System.getProperty("DB_JDBC_URL") == null && System.getenv("DB_JDBC_URL") == null) {
            System.setProperty("DB_JDBC_URL", buildJdbcUrl());
        }
    }

    private static String buildJdbcUrl() {
        String host = firstNonBlank(System.getProperty("DB_HOST"), "localhost");
        String port = firstNonBlank(System.getProperty("DB_PORT"), "5432");
        String name = firstNonBlank(System.getProperty("DB_NAME"), "civicconnect");
        boolean ssl = Boolean.parseBoolean(
                firstNonBlank(System.getProperty("DB_SSL"), "false").toLowerCase(Locale.ROOT)
        );

        StringBuilder url = new StringBuilder("jdbc:postgresql://")
                .append(host).append(':').append(port).append('/').append(name);
        if (ssl) {
            url.append("?sslmode=require");
        }
        return url.toString();
    }

    private static Path findEnvDirectory() {
        Path current = Path.of("").toAbsolutePath();
        for (int i = 0; i < 6 && current != null; i++) {
            if (Files.isRegularFile(current.resolve(".env"))
                    || Files.isRegularFile(current.resolve(".env.example"))) {
                return current;
            }
            current = current.getParent();
        }
        return null;
    }

    private static String firstNonBlank(String value, String fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }
        return value.trim();
    }
}
