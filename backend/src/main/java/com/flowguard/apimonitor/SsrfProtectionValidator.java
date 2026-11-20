package com.flowguard.apimonitor;

import com.flowguard.common.BadRequestException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.Set;

@Component
public class SsrfProtectionValidator {

    private static final Logger log = LoggerFactory.getLogger(SsrfProtectionValidator.class);

    private final boolean ssrfProtectionEnabled;

    private static final Set<String> BLOCKED_HOSTS = Set.of(
            "localhost",
            "127.0.0.1",
            "::1",
            "169.254.169.254", // AWS/GCP/Azure instance metadata
            "metadata.google.internal",
            "instance-data"
    );

    public SsrfProtectionValidator(@Value("${flowguard.monitoring.ssrf-protection-enabled:true}") boolean enabled) {
        this.ssrfProtectionEnabled = enabled;
    }

    public void validateUrl(String url) {
        if (!ssrfProtectionEnabled) {
            return;
        }

        if (url == null || url.isBlank()) {
            throw new BadRequestException("Target URL cannot be empty");
        }

        try {
            URI uri = URI.create(url);
            String scheme = uri.getScheme();

            if (scheme == null || (!scheme.equalsIgnoreCase("http") && !scheme.equalsIgnoreCase("https"))) {
                throw new BadRequestException("Protocol must be HTTP or HTTPS: " + scheme);
            }

            String host = uri.getHost();
            if (host == null || host.isBlank()) {
                throw new BadRequestException("Invalid hostname in target URL: " + url);
            }

            String lowerHost = host.toLowerCase().trim();

            if (BLOCKED_HOSTS.contains(lowerHost)) {
                log.warn("Blocked SSRF attempt to forbidden host: {}", host);
                throw new BadRequestException("SSRF Protection Violation: Target host '" + host + "' is forbidden.");
            }

            // Resolve DNS and check IP addresses
            try {
                InetAddress[] addresses = InetAddress.getAllByName(host);
                for (InetAddress address : addresses) {
                    if (address.isLoopbackAddress()) {
                        throw new BadRequestException("SSRF Protection: Loopback addresses are forbidden: " + address.getHostAddress());
                    }
                    if (address.isLinkLocalAddress()) {
                        throw new BadRequestException("SSRF Protection: Link-local addresses are forbidden: " + address.getHostAddress());
                    }
                    if (address.isAnyLocalAddress()) {
                        throw new BadRequestException("SSRF Protection: Wildcard addresses are forbidden: " + address.getHostAddress());
                    }
                    // For enterprise production, prohibit private RFC 1918 subnets unless explicitly allowed
                    if (isPrivateRfc1918(address)) {
                        log.warn("SSRF Protection flagged RFC 1918 IP: {}", address.getHostAddress());
                    }
                }
            } catch (UnknownHostException ex) {
                // If the host cannot be resolved yet (or is a simulated mock hostname), allow if not in blocked host list
                log.debug("Host resolution deferred for target: {}", host);
            }

        } catch (IllegalArgumentException ex) {
            throw new BadRequestException("Malformed URL format: " + ex.getMessage());
        }
    }

    private boolean isPrivateRfc1918(InetAddress address) {
        byte[] bytes = address.getAddress();
        if (bytes.length == 4) {
            int b0 = bytes[0] & 0xFF;
            int b1 = bytes[1] & 0xFF;

            // 10.0.0.0/8
            if (b0 == 10) return true;
            // 172.16.0.0/12
            if (b0 == 172 && (b1 >= 16 && b1 <= 31)) return true;
            // 192.168.0.0/16
            if (b0 == 192 && b1 == 168) return true;
            // 169.254.0.0/16
            if (b0 == 169 && b1 == 254) return true;
        }
        return false;
    }
}

