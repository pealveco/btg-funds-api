package co.com.pactual.api.helper;

public final class InputSanitizer {

    private static final String IDENTIFIER_PATTERN = "^[A-Za-z0-9_-]+$";

    private InputSanitizer() {
    }

    public static String trim(String value) {
        return value == null ? null : value.trim();
    }

    public static boolean hasValidIdentifierFormat(String value) {
        return value != null && value.matches(IDENTIFIER_PATTERN);
    }
}
