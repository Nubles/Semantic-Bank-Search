package com.semanticbanksearch;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;
import java.util.Optional;

final class AccountKey
{
    private static final String DOMAIN = "semantic-bank-search/account/v1:";

    private final String value;

    private AccountKey(String value)
    {
        this.value = value;
    }

    static Optional<AccountKey> fromAccountHash(long accountHash)
    {
        if (accountHash == 0L)
        {
            return Optional.empty();
        }

        try
        {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                .digest((DOMAIN + accountHash).getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder(digest.length * 2);
            for (byte value : digest)
            {
                int unsignedValue = value & 0xff;
                hex.append(Character.forDigit(unsignedValue >>> 4, 16));
                hex.append(Character.forDigit(unsignedValue & 0x0f, 16));
            }
            return Optional.of(new AccountKey(hex.toString()));
        }
        catch (NoSuchAlgorithmException ex)
        {
            throw new IllegalStateException("SHA-256 is required", ex);
        }
    }

    String value()
    {
        return value;
    }

    @Override
    public boolean equals(Object other)
    {
        if (this == other)
        {
            return true;
        }
        if (!(other instanceof AccountKey))
        {
            return false;
        }
        AccountKey that = (AccountKey) other;
        return value.equals(that.value);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(value);
    }

    @Override
    public String toString()
    {
        return value;
    }
}
