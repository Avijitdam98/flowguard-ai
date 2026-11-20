package com.flowguard.apimonitor;

import com.flowguard.common.BadRequestException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SsrfProtectionValidatorTest {

    private SsrfProtectionValidator validator;

    @BeforeEach
    void setUp() {
        validator = new SsrfProtectionValidator(true);
    }

    @Test
    @DisplayName("Should accept valid external HTTPS URLs")
    void testValidExternalUrl() {
        assertDoesNotThrow(() -> validator.validateUrl("https://api.github.com"));
        assertDoesNotThrow(() -> validator.validateUrl("https://httpbin.org/status/200"));
    }

    @Test
    @DisplayName("Should block loopback localhost addresses")
    void testBlockLocalhost() {
        assertThrows(BadRequestException.class, () -> validator.validateUrl("http://localhost:8080"));
        assertThrows(BadRequestException.class, () -> validator.validateUrl("http://127.0.0.1:5432"));
    }

    @Test
    @DisplayName("Should block cloud metadata service IP 169.254.169.254")
    void testBlockCloudMetadata() {
        assertThrows(BadRequestException.class, () -> validator.validateUrl("http://169.254.169.254/latest/meta-data"));
    }

    @Test
    @DisplayName("Should reject non-HTTP/HTTPS protocols like file://, ftp://, or gopher://")
    void testRejectNonHttpProtocols() {
        assertThrows(BadRequestException.class, () -> validator.validateUrl("file:///etc/passwd"));
        assertThrows(BadRequestException.class, () -> validator.validateUrl("ftp://ftp.example.com"));
    }
}
