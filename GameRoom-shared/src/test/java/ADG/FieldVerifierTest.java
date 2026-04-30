package ADG;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FieldVerifierTest {

    // ── valid names ──────────────────────────────────────────────────────

    @Test
    void isValidNameAccepts4Characters() {
        assertTrue(FieldVerifier.isValidName("user"));
    }

    @Test
    void isValidNameAcceptsLongNames() {
        assertTrue(FieldVerifier.isValidName("this_is_a_very_long_username"));
    }

    @Test
    void isValidNameAcceptsUppercase() {
        assertTrue(FieldVerifier.isValidName("USER"));
    }

    @Test
    void isValidNameAcceptsNumbers() {
        assertTrue(FieldVerifier.isValidName("user123"));
    }

    @Test
    void isValidNameAcceptsSpecialCharacters() {
        assertTrue(FieldVerifier.isValidName("user_1!@"));
    }

    @Test
    void isValidNameAcceptsSpaces() {
        assertTrue(FieldVerifier.isValidName("user name"));
    }

    // ── invalid names ────────────────────────────────────────────────────

    @Test
    void isValidNameRejectsNull() {
        assertFalse(FieldVerifier.isValidName(null));
    }

    @Test
    void isValidNameRejectsEmpty() {
        assertFalse(FieldVerifier.isValidName(""));
    }

    @Test
    void isValidNameRejectsOneCharacter() {
        assertFalse(FieldVerifier.isValidName("a"));
    }

    @Test
    void isValidNameRejecksTwoCharacters() {
        assertFalse(FieldVerifier.isValidName("ab"));
    }

    @Test
    void isValidNameRejectsThreeCharacters() {
        assertFalse(FieldVerifier.isValidName("abc"));
    }

    // ── boundary cases ───────────────────────────────────────────────────

    @Test
    void isValidNameBoundaryAtExactlyFour() {
        assertTrue(FieldVerifier.isValidName("abcd"));
    }

    @Test
    void isValidNameBoundaryAtThree() {
        assertFalse(FieldVerifier.isValidName("abc"));
    }

    @Test
    void isValidNameAccepts1000Characters() {
        String longName = "a".repeat(1000);
        assertTrue(FieldVerifier.isValidName(longName));
    }

    @Test
    void isValidNameAcceptsWhitespaceOnly() {
        assertTrue(FieldVerifier.isValidName("    "));
    }

    @Test
    void isValidNameAcceptsNewlineCharacters() {
        assertTrue(FieldVerifier.isValidName("abc\n"));
    }

    @Test
    void isValidNameAcceptsUnicodeCharacters() {
        assertTrue(FieldVerifier.isValidName("用户名用"));
    }
}