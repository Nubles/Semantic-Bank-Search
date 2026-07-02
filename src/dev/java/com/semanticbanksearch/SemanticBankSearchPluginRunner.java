package com.semanticbanksearch;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class SemanticBankSearchPluginRunner
{
    public static void main(String[] args) throws Exception
    {
        ExternalPluginManager.loadBuiltin(SemanticBankSearchPlugin.class);
        RuneLite.main(args);
    }
}
