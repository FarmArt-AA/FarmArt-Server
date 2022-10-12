package cmc.farmart;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class FarmArtApplication {

    public static void main(String[] args) {
        SpringApplication.run(FarmArtApplication.class, args);
    }


}
