package civicconnect;

import civicconnect.config.DotEnvLoader;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class CivicConnectApplication {

    public static void main(String[] args) {
        DotEnvLoader.load();
        SpringApplication.run(CivicConnectApplication.class, args);
    }
}
