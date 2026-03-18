package co.com.pactual.api.helper;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InputSanitizerTest {

    @Test
    void shouldTrimValue() {
        assertEquals("client-001", InputSanitizer.trim("  client-001  "));
    }

    @Test
    void shouldReturnNullWhenValueIsNull() {
        assertNull(InputSanitizer.trim(null));
    }

    @Test
    void shouldValidateIdentifierFormat() {
        assertTrue(InputSanitizer.hasValidIdentifierFormat("client-001"));
        assertFalse(InputSanitizer.hasValidIdentifierFormat("client 001"));
        assertFalse(InputSanitizer.hasValidIdentifierFormat(null));
    }
}
