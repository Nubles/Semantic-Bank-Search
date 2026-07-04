package com.semanticbanksearch;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.awt.Component;
import java.awt.Container;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.swing.AbstractButton;
import javax.swing.JLabel;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import org.junit.Test;

public class SemanticBankSearchPanelTest
{
    @Test
    public void panelUsesCompactModeButtonsAndSearchSummary() throws Exception
    {
        SemanticBankSearchPanel panel = new SemanticBankSearchPanel(ignored -> { }, () -> { }, () -> { }, () -> { });

        runOnEdt(() -> panel.updateResults("prayer", Arrays.asList(
            result("Prayer potion(4)", "Prayer restoration"),
            result("Super restore(4)", "Prayer restoration")), ""));

        List<String> text = visibleText(panel);
        assertTrue(text.contains("Search"));
        assertTrue(text.contains("All"));
        assertTrue(text.contains("Coverage"));
        assertTrue(text.contains("Clear"));
        assertFalse(text.contains("All Indexed"));
        assertTrue(text.contains("2 matches"));
    }

    @Test
    public void coverageAuditGroupsUncoveredBeforeCoveredItems() throws Exception
    {
        SemanticBankSearchPanel panel = new SemanticBankSearchPanel(ignored -> { }, () -> { }, () -> { }, () -> { });

        runOnEdt(() -> panel.updateCoverageAudit(Arrays.asList(
            coverage("Prayer potion(4)", Collections.singletonList("Prayer restoration")),
            coverage("Coins", Collections.emptyList())),
            "Covered 1 of 2 observed items."));

        List<String> text = visibleText(panel);
        assertTrue(text.contains("Covered 1 of 2 observed items."));
        assertTrue(text.contains("Uncovered"));
        assertTrue(text.contains("Covered"));
        assertTrue(indexOf(text, "Uncovered") < indexOf(text, "Coins"));
        assertTrue(indexOf(text, "Coins") < indexOf(text, "Covered"));
        assertTrue(indexOf(text, "Covered") < indexOf(text, "Prayer potion(4)"));
    }

    private static SemanticSearchResult result(String itemName, String category)
    {
        return new SemanticSearchResult(
            100,
            itemName,
            1,
            StorageSourceType.BANK,
            "Bank",
            true,
            category,
            "Useful item.",
            100);
    }

    private static SemanticCoverageResult coverage(String itemName, List<String> categories)
    {
        return new SemanticCoverageResult(
            new ObservedItem(100, itemName, 1, StorageSourceType.BANK, "Bank", true, 1_000L),
            categories,
            categories.isEmpty() ? Collections.emptyList() : Collections.singletonList("Useful item."),
            categories.isEmpty() ? 0 : 100);
    }

    private static void runOnEdt(Runnable runnable) throws Exception
    {
        if (SwingUtilities.isEventDispatchThread())
        {
            runnable.run();
            return;
        }
        SwingUtilities.invokeAndWait(runnable);
    }

    private static List<String> visibleText(Container container)
    {
        List<String> text = new ArrayList<>();
        collectText(container, text);
        return text;
    }

    private static void collectText(Component component, List<String> text)
    {
        if (component instanceof JLabel)
        {
            addText(((JLabel) component).getText(), text);
        }
        else if (component instanceof AbstractButton)
        {
            addText(((AbstractButton) component).getText(), text);
        }
        else if (component instanceof JTextArea)
        {
            addText(((JTextArea) component).getText(), text);
        }

        if (component instanceof Container)
        {
            for (Component child : ((Container) component).getComponents())
            {
                collectText(child, text);
            }
        }
    }

    private static void addText(String value, List<String> text)
    {
        if (value != null && !value.trim().isEmpty())
        {
            text.add(value.trim());
        }
    }

    private static int indexOf(List<String> values, String expected)
    {
        int index = values.indexOf(expected);
        if (index < 0)
        {
            throw new AssertionError("Missing text: " + expected + " in " + values);
        }
        return index;
    }
}