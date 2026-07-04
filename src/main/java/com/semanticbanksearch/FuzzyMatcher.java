package com.semanticbanksearch;

final class FuzzyMatcher
{
    private FuzzyMatcher()
    {
    }

    static boolean phraseMatches(String normalizedQuery, String normalizedPhrase)
    {
        if (normalizedQuery == null || normalizedPhrase == null || normalizedQuery.isEmpty() || normalizedPhrase.isEmpty())
        {
            return false;
        }

        if ((" " + normalizedQuery + " ").contains(" " + normalizedPhrase + " "))
        {
            return true;
        }

        String[] queryTokens = normalizedQuery.split("\\s+");
        String[] phraseTokens = normalizedPhrase.split("\\s+");
        if (queryTokens.length > phraseTokens.length + 1 || phraseTokens.length > queryTokens.length + 1)
        {
            return false;
        }

        int queryIndex = 0;
        for (String phraseToken : phraseTokens)
        {
            boolean matched = false;
            while (queryIndex < queryTokens.length)
            {
                if (tokensMatch(queryTokens[queryIndex], phraseToken))
                {
                    matched = true;
                    queryIndex++;
                    break;
                }
                queryIndex++;
            }
            if (!matched)
            {
                return false;
            }
        }
        return true;
    }

    static boolean tokensMatch(String queryToken, String candidateToken)
    {
        if (queryToken == null || candidateToken == null || queryToken.isEmpty() || candidateToken.isEmpty())
        {
            return false;
        }
        if (queryToken.equals(candidateToken) || candidateToken.startsWith(queryToken))
        {
            return true;
        }
        if (queryToken.length() <= 3 || candidateToken.length() <= 3)
        {
            return false;
        }

        int allowedDistance = allowedDistance(queryToken, candidateToken);
        return editDistanceAtMost(queryToken, candidateToken, allowedDistance);
    }

    private static int allowedDistance(String left, String right)
    {
        int shorter = Math.min(left.length(), right.length());
        if (shorter <= 3)
        {
            return 0;
        }
        if (shorter <= 6)
        {
            return 1;
        }
        return 2;
    }

    private static boolean editDistanceAtMost(String left, String right, int maxDistance)
    {
        if (Math.abs(left.length() - right.length()) > maxDistance)
        {
            return false;
        }

        int[] previous = new int[right.length() + 1];
        int[] current = new int[right.length() + 1];
        for (int j = 0; j <= right.length(); j++)
        {
            previous[j] = j;
        }

        for (int i = 1; i <= left.length(); i++)
        {
            current[0] = i;
            int rowMin = current[0];
            for (int j = 1; j <= right.length(); j++)
            {
                int cost = left.charAt(i - 1) == right.charAt(j - 1) ? 0 : 1;
                current[j] = Math.min(
                    Math.min(current[j - 1] + 1, previous[j] + 1),
                    previous[j - 1] + cost);
                rowMin = Math.min(rowMin, current[j]);
            }
            if (rowMin > maxDistance)
            {
                return false;
            }

            int[] swap = previous;
            previous = current;
            current = swap;
        }

        return previous[right.length()] <= maxDistance;
    }
}