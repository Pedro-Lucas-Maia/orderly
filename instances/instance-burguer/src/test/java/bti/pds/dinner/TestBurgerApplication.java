package bti.pds.dinner;

import org.springframework.boot.SpringApplication;

public class TestBurgerApplication {

    public static void main(String[] args) {
        SpringApplication.from(BurgerApplication::main).with(TestcontainersConfiguration.class).run(args);
    }

}
