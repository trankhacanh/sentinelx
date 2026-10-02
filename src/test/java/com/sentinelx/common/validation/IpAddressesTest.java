package com.sentinelx.common.validation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

class IpAddressesTest {

    @ParameterizedTest
    @ValueSource(strings = {"192.168.1.50", "0.0.0.0", "255.255.255.255", "::1", "2001:db8::1"})
    void validAddresses_areAccepted(String ip) {
        assertTrue(IpAddresses.isValid(ip));
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", " ", "999.1.1.1", "01.2.3.4", "1.2.3", "example.com", "abc", "::gggg",
            "1.2.3.4; DROP TABLE users"})
    void invalidAddresses_areRejected(String ip) {
        assertFalse(IpAddresses.isValid(ip));
    }

    @Test
    void ipv6_isNormalizedToOneCanonicalForm() {
        assertEquals("0:0:0:0:0:0:0:1", IpAddresses.normalize("::1"));
        assertEquals(IpAddresses.normalize("2001:db8::1"),
                IpAddresses.normalize("2001:0DB8:0:0:0:0:0:1"));
    }

    @Test
    void ipv4_isLeftUntouched() {
        assertEquals("192.168.1.50", IpAddresses.normalize(" 192.168.1.50 "));
    }
}