package bti.pds.dinner;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.testcontainers.DockerClientFactory;

@Import(TestcontainersConfiguration.class)
@SpringBootTest(properties = {
    "jwt.public.key=classpath:env/app.pub",
    "jwt.secret=classpath:env/app.key",
    "resend.api.key=dummy_api_key",
    "cloudinary.cloud-name=dummy",
    "cloudinary.api-key=dummy",
    "cloudinary.api-secret=dummy"
})
@EnabledIf("isDockerAvailable")
class BurgerApplicationTests {

    static boolean isDockerAvailable() {
        try {
            return DockerClientFactory.instance().isDockerAvailable();
        } catch (Throwable e) {
            return false;
        }
    }

    @Test
    void contextLoads() {
    }

}
