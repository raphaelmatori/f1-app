package com.f1.app;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.junit.jupiter.api.Test;

@SpringBootTest
@ActiveProfiles("test")
class AppApplicationTests {

    @Test
    void contextLoads() {
        // This test will pass if the application context loads successfully
    }
}
