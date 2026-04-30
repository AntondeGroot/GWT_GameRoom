package ADG.Utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ChatCipherTest {

    // ── encrypt/decrypt roundtrip ───────────────────────────────────────

    @Test
    void encryptAndDecryptRoundtrip() {
        String plaintext = "Hello, World!";
        String key = "secret";

        String encrypted = ChatCipher.encrypt(plaintext, key);
        String decrypted = ChatCipher.decrypt(encrypted, key);

        assertEquals(plaintext, decrypted);
    }

    @Test
    void encryptAndDecryptRoundtripWithLongText() {
        String plaintext = "This is a longer message with more characters to test the encryption algorithm.";
        String key = "mykey";

        String encrypted = ChatCipher.encrypt(plaintext, key);
        String decrypted = ChatCipher.decrypt(encrypted, key);

        assertEquals(plaintext, decrypted);
    }

    @Test
    void encryptAndDecryptRoundtripWithSpecialCharacters() {
        String plaintext = "Special chars: !@#$%^&*()_+-=[]{}|;:,.<>?";
        String key = "key123";

        String encrypted = ChatCipher.encrypt(plaintext, key);
        String decrypted = ChatCipher.decrypt(encrypted, key);

        assertEquals(plaintext, decrypted);
    }

    @Test
    void encryptAndDecryptRoundtripWithUnicode() {
        String plaintext = "Unicode: 你好 مرحبا 🎉";
        String key = "unicode_key";

        String encrypted = ChatCipher.encrypt(plaintext, key);
        String decrypted = ChatCipher.decrypt(encrypted, key);

        assertEquals(plaintext, decrypted);
    }

    // ── key variations ──────────────────────────────────────────────────

    @Test
    void encryptAndDecryptWithOneCharacterKey() {
        String plaintext = "Test";
        String key = "a";

        String encrypted = ChatCipher.encrypt(plaintext, key);
        String decrypted = ChatCipher.decrypt(encrypted, key);

        assertEquals(plaintext, decrypted);
    }

    @Test
    void encryptAndDecryptWithLongKey() {
        String plaintext = "Test";
        String key = "this_is_a_very_long_key_that_is_longer_than_the_plaintext";

        String encrypted = ChatCipher.encrypt(plaintext, key);
        String decrypted = ChatCipher.decrypt(encrypted, key);

        assertEquals(plaintext, decrypted);
    }

    @Test
    void encryptAndDecryptWithKeyLongerThanText() {
        String plaintext = "Hi";
        String key = "verylongkeyverylongkey";

        String encrypted = ChatCipher.encrypt(plaintext, key);
        String decrypted = ChatCipher.decrypt(encrypted, key);

        assertEquals(plaintext, decrypted);
    }

    @Test
    void encryptAndDecryptWithNumericKey() {
        String plaintext = "Message";
        String key = "12345";

        String encrypted = ChatCipher.encrypt(plaintext, key);
        String decrypted = ChatCipher.decrypt(encrypted, key);

        assertEquals(plaintext, decrypted);
    }

    // ── null/empty handling ──────────────────────────────────────────────

    @Test
    void encryptWithNullTextReturnsNull() {
        String result = ChatCipher.encrypt(null, "key");
        assertNull(result);
    }

    @Test
    void encryptWithEmptyTextReturnsEmpty() {
        String result = ChatCipher.encrypt("", "key");
        assertEquals("", result);
    }

    @Test
    void encryptWithNullKeyReturnsText() {
        String plaintext = "Message";
        String result = ChatCipher.encrypt(plaintext, null);
        assertEquals(plaintext, result);
    }

    @Test
    void encryptWithEmptyKeyReturnsText() {
        String plaintext = "Message";
        String result = ChatCipher.encrypt(plaintext, "");
        assertEquals(plaintext, result);
    }

    @Test
    void decryptWithNullHexReturnsNull() {
        String result = ChatCipher.decrypt(null, "key");
        assertNull(result);
    }

    @Test
    void decryptWithEmptyHexReturnsEmpty() {
        String result = ChatCipher.decrypt("", "key");
        assertEquals("", result);
    }

    @Test
    void decryptWithNullKeyReturnsHex() {
        String hex = "1234abcd5678ef";
        String result = ChatCipher.decrypt(hex, null);
        assertEquals(hex, result);
    }

    @Test
    void decryptWithEmptyKeyReturnsHex() {
        String hex = "1234abcd5678ef";
        String result = ChatCipher.decrypt(hex, "");
        assertEquals(hex, result);
    }

    // ── different keys produce different ciphertexts ─────────────────────

    @Test
    void differentKeysProduceDifferentCiphertexts() {
        String plaintext = "Secret message";
        String key1 = "key1";
        String key2 = "key2";

        String encrypted1 = ChatCipher.encrypt(plaintext, key1);
        String encrypted2 = ChatCipher.encrypt(plaintext, key2);

        assertNotEquals(encrypted1, encrypted2);
    }

    @Test
    void wrongKeyProducesGarbage() {
        String plaintext = "Secret";
        String key1 = "correct_key";
        String key2 = "wrong_key";

        String encrypted = ChatCipher.encrypt(plaintext, key1);
        String decrypted = ChatCipher.decrypt(encrypted, key2);

        assertNotEquals(plaintext, decrypted);
    }

    // ── hex encoding ─────────────────────────────────────────────────────

    @Test
    void encryptProducesHexString() {
        String plaintext = "Test";
        String key = "key";

        String encrypted = ChatCipher.encrypt(plaintext, key);

        assertTrue(encrypted.matches("[0-9a-f]+"), "Encrypted output should be valid hex");
        assertTrue(encrypted.length() % 4 == 0, "Hex output should be multiple of 4 chars per character");
    }

    @Test
    void encryptLengthIsProportionalToInput() {
        String key = "key";
        String encrypted1 = ChatCipher.encrypt("a", key);
        String encrypted2 = ChatCipher.encrypt("ab", key);

        assertEquals(encrypted1.length() + 4, encrypted2.length(),
                "Each character should produce 4 hex characters");
    }

    // ── malformed input ─────────────────────────────────────────────────

    @Test
    void decryptWithOddLengthHexHandlesGracefully() {
        String oddHex = "12345";
        String result = ChatCipher.decrypt(oddHex, "key");

        // Should not throw; behavior on odd-length hex is graceful
        assertNotNull(result);
    }

    @Test
    void decryptWithInvalidHexCharactersHandlesGracefully() {
        String invalidHex = "zzzzzzzz";
        String result = ChatCipher.decrypt(invalidHex, "key");

        // Should not throw; invalid hex chars (z) treated as 0
        assertNotNull(result);
    }

    // ── single character messages ────────────────────────────────────────

    @Test
    void encryptSingleCharacter() {
        String plaintext = "A";
        String key = "key";

        String encrypted = ChatCipher.encrypt(plaintext, key);
        String decrypted = ChatCipher.decrypt(encrypted, key);

        assertEquals(plaintext, decrypted);
    }

    @Test
    void encryptSingleSpace() {
        String plaintext = " ";
        String key = "key";

        String encrypted = ChatCipher.encrypt(plaintext, key);
        String decrypted = ChatCipher.decrypt(encrypted, key);

        assertEquals(plaintext, decrypted);
    }
}