package com.semanticbanksearch;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

class SemanticBankSearchStorage
{
    private SemanticBankSearchStorage()
    {
    }

    static String serialize(Gson gson, StorageIndex index)
    {
        if (gson == null)
        {
            return "";
        }

        return gson.toJson(index == null ? new StorageIndex() : index);
    }

    static StorageIndex deserialize(Gson gson, String json)
    {
        if (gson == null || json == null || json.trim().isEmpty())
        {
            return new StorageIndex();
        }

        try
        {
            StorageIndex temporary = gson.fromJson(json, StorageIndex.class);
            if (temporary == null)
            {
                return new StorageIndex();
            }

            StorageIndex sanitized = new StorageIndex();
            for (ObservedItem item : temporary.items())
            {
                sanitized.record(
                    item.getItemId(),
                    item.getName(),
                    item.getQuantity(),
                    item.getSourceType(),
                    item.getSourceName(),
                    item.isCurrentlyVisible(),
                    item.getLastSeenMillis());
            }
            return sanitized;
        }
        catch (JsonSyntaxException ex)
        {
            return new StorageIndex();
        }
        catch (RuntimeException ex)
        {
            return new StorageIndex();
        }
    }
}
