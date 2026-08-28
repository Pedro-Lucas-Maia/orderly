package bti.pds.dinner;

import org.springframework.boot.SpringApplication;

public class TestDinnerApplication {

    public static void main(String[] args) {
        SpringApplication.from(DinnerApplication::main).with(TestcontainersConfiguration.class).run(args);
    }

}
