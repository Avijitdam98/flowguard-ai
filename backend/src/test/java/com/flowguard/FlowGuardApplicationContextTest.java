package com.flowguard;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("local")
class FlowGuardApplicationContextTest {

    @Test
    @DisplayName("Verify Spring Boot application context loads with local profile")
    void contextLoads() {
    }
}

