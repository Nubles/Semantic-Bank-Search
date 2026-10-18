package com.semanticbanksearch.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import javax.swing.JComponent;

/**
 * One item's line: the item in a dark well like a bank slot, its name and a quiet note under it,
 * and its stack size on the right in the game's own colours. A gold bead on the well means the
 * item is in the bank that is open right now, so it is lit up there too.
 */
public class ItemRow extends JComponent
{
	private static final int WELL = 32;

	private final int itemId;
	private final String name;
	private final String note;
	private final long quantity;
	private final boolean lit;
	private final ItemIcons icons;

	public ItemRow(int itemId, String name, String note, long quantity, boolean lit, ItemIcons icons, String tip)
	{
		this.itemId = itemId;
		this.name = name;
		this.note = note;
		this.quantity = quantity;
		this.lit = lit;
		this.icons = icons;
		setOpaque(false);
		setPreferredSize(new Dimension(10, WELL + 4));
		setToolTipText(tip);
	}

	/** A stack size the way the game writes it: 950, 125K, 10M. */
	public static String stack(long qty)
	{
		if (qty >= 10_000_000)
		{
			return (qty / 1_000_000) + "M";
		}
		if (qty >= 100_000)
		{
			return (qty / 1_000) + "K";
		}
		return String.format("%,d", qty);
	}

	@Override
	protected void paintComponent(Graphics graphics)
	{
		Graphics2D g = (Graphics2D) graphics.create();
		Theme.smooth(g);
		int w = getWidth();
		int y = 2;
		RoundRectangle2D well = new RoundRectangle2D.Float(0.5f, y + 0.5f, WELL + 4, WELL - 1, 7, 7);
		g.setPaint(new GradientPaint(0, y, Theme.INK_DEEP, 0, y + WELL, Theme.mix(Theme.INK_DEEP, Theme.CARD, 0.6)));
		g.fill(well);
		g.setColor(lit ? Theme.alpha(Theme.GOLD, 120) : Theme.STROKE_SOFT);
		g.draw(well);
		BufferedImage icon = icons == null ? null : icons.icon(itemId, this::repaint);
		if (icon != null)
		{
			g.drawImage(icon, 2 + (WELL - icon.getWidth()) / 2, y + (WELL - icon.getHeight()) / 2 + 1, null);
		}
		if (lit)
		{
			g.setColor(Theme.alpha(Theme.GOLD_BRIGHT, 70));
			g.fill(new Ellipse2D.Float(WELL - 4, y + WELL - 9, 8, 8));
			g.setColor(Theme.GOLD_BRIGHT);
			g.fill(new Ellipse2D.Float(WELL - 2, y + WELL - 7, 4, 4));
		}

		int tx = WELL + 12;
		g.setFont(Theme.body());
		FontMetrics fm = g.getFontMetrics();
		String amount = quantity > 1 ? stack(quantity) : "";
		int aw = amount.isEmpty() ? 0 : fm.stringWidth(amount);
		int base = y + fm.getAscent() + 1;
		if (!amount.isEmpty())
		{
			Color color = Theme.quantity(quantity);
			Ui.shadowed(g, amount, w - aw, base, color);
		}
		Ui.shadowed(g, Ui.clip(name, fm, w - tx - aw - 8), tx, base, Theme.TEXT);

		if (note != null && !note.isEmpty())
		{
			g.setFont(Theme.small());
			FontMetrics small = g.getFontMetrics();
			g.setColor(Theme.TEXT_FAINT);
			g.drawString(Ui.clip(note, small, w - tx), tx, y + WELL - 6);
		}
		g.dispose();
	}
}
