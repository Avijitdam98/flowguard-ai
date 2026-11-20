package com.flowguard;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FlowGuardSmokeTest {

    @Test
    @DisplayName("Verify FlowGuard AI project bootstrap and environment prerequisites")
    void testProjectBootstrap() {
        String javaVersion = System.getProperty("java.version");
        assertNotNull(javaVersion, "Java version should not be null");
        assertTrue(javaVersion.startsWith("21"), "Should be running on Java 21, found: " + javaVersion);
    }
}

