package bti.pds.dinner;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class DinnerApplication {

    public static void main(String[] args) {
        SpringApplication.run(DinnerApplication.class, args);
    }

}
