package bti.pds.dinner;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@Import(TestcontainersConfiguration.class)
@SpringBootTest(properties = {
    "jwt.public.key=classpath:env/app.pub",
    "jwt.secret=classpath:env/app.key",
    "resend.api.key=dummy_api_key"
})
class DinnerApplicationTests {

    @Test
    void contextLoads() {
    }

}
