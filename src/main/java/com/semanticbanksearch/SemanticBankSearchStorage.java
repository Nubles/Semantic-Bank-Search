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
            StorageIndex index = gson.fromJson(json, StorageIndex.class);
            if (index == null)
            {
                return new StorageIndex();
            }

            index.items();
            return index;
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
