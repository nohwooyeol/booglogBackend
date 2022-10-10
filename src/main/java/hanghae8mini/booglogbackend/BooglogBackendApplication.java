package hanghae8mini.booglogbackend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class BooglogBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(BooglogBackendApplication.class, args);
    }

}
