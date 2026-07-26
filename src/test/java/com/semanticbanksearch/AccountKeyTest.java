package com.semanticbanksearch;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Optional;
import org.junit.Test;

public class AccountKeyTest
{
    @Test
    public void zeroHashHasNoAccountKey()
    {
        assertFalse(AccountKey.fromAccountHash(0L).isPresent());
    }

    @Test
    public void sameHashProducesStableLowercaseSha256Key()
    {
        Optional<AccountKey> first = AccountKey.fromAccountHash(123456789L);
        Optional<AccountKey> second = AccountKey.fromAccountHash(123456789L);
        String expected = sha256Hex("semantic-bank-search/account/v1:123456789");

        assertTrue(first.isPresent());
        assertEquals(first, second);
        assertEquals(expected, first.get().value());
        assertEquals(64, first.get().value().length());
        assertTrue(first.get().value().matches("[0-9a-f]+"));
    }

    @Test
    public void signedHashValuesProduceDifferentKeys()
    {
        assertNotEquals(
            AccountKey.fromAccountHash(1L).get(),
            AccountKey.fromAccountHash(-1L).get());
    }

    @Test
    public void keyDoesNotContainRawHash()
    {
        long accountHash = 987654321L;

        assertFalse(AccountKey.fromAccountHash(accountHash).get().value().contains(Long.toString(accountHash)));
    }

    @Test
    public void domainSeparationChangesPlainSha256Result()
    {
        long accountHash = 123456789L;
        String plainHash = sha256Hex(Long.toString(accountHash));

        assertNotEquals(plainHash, AccountKey.fromAccountHash(accountHash).get().value());
    }

    private static String sha256Hex(String input)
    {
        try
        {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                .digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder(digest.length * 2);
            for (byte value : digest)
            {
                hex.append(String.format("%02x", value & 0xff));
            }
            return hex.toString();
        }
        catch (NoSuchAlgorithmException ex)
        {
            throw new AssertionError(ex);
        }
    }
}
