package com.semanticbanksearch.ui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RadialGradientPaint;
import java.awt.RenderingHints;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
import javax.swing.JComponent;

/**
 * The crest at the top of the panel: the reading-glass medallion, how many of the account's
 * items the plugin knows in large gilt figures, and one line on where that knowledge stands.
 */
public class Crest extends JComponent
{
	private static final int HEIGHT = 92;

	private final Animator count = new Animator(this);
	private String heading = "SEMANTIC BANK SEARCH";
	private String status = "Log in to search your bank";
	private Color statusColor = Theme.TEXT_MUTED;
	private boolean known;

	public Crest()
	{
		setOpaque(false);
		setPreferredSize(new Dimension(10, HEIGHT));
		setMinimumSize(new Dimension(10, HEIGHT));
	}

	/**
	 * @param items how many different items are known, or below 0 for none yet
	 * @param ready whether the status is good news (teal) or a request (muted gold)
	 */
	public void update(String player, int items, String status, boolean ready)
	{
		String owner = player == null ? "" : player.toUpperCase();
		this.heading = owner.isEmpty() ? "SEMANTIC BANK SEARCH" : owner + (owner.endsWith("S") ? "' BANK" : "'S BANK");
		this.status = status;
		this.statusColor = ready ? Theme.mix(Theme.TEAL, Theme.TEXT, 0.25) : Theme.mix(Theme.TEXT_MUTED, Theme.GOLD, 0.35);
		boolean animate = known && items >= 0;
		known = items >= 0;
		count.set(Math.max(0, items), animate);
		repaint();
	}

	/**
	 * Draws text at twice the size of the game's bold font by doubling every pixel, the way the
	 * game itself draws large numbers, so the figures stay crisp instead of being re-rasterised.
	 *
	 * @return the width drawn
	 */
	static int paintLarge(Graphics2D g, String text, int x, int y)
	{
		Font font = Theme.title();
		FontMetrics fm = g.getFontMetrics(font);
		int w = Math.max(1, fm.stringWidth(text) + 1);
		int h = fm.getHeight() + 1;
		BufferedImage small = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
		Graphics2D sg = small.createGraphics();
		sg.setFont(font);
		sg.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
		int base = fm.getAscent();
		sg.setColor(Color.BLACK);
		sg.drawString(text, 1, base + 1);
		sg.setPaint(new GradientPaint(0, base - fm.getAscent() + 3, Theme.GOLD_BRIGHT, 0, base, Theme.GOLD));
		sg.drawString(text, 0, base);
		sg.dispose();
		Graphics2D lg = (Graphics2D) g.create();
		lg.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
		lg.drawImage(small, x, y - 2 * fm.getAscent() + 22, w * 2, h * 2, null);
		lg.dispose();
		return w * 2;
	}

	@Override
	protected void paintComponent(Graphics graphics)
	{
		Graphics2D g = (Graphics2D) graphics.create();
		Theme.smooth(g);
		int w = getWidth();
		int h = getHeight();
		g.setPaint(new GradientPaint(0, 0, Theme.mix(Theme.INK, Theme.GOLD_DIM, 0.22), 0, h, Theme.INK));
		g.fillRect(0, 0, w, h);

		// The medallion: a gilt ring with the mark inside, lit from above.
		int mx = 10;
		int my = 14;
		int md = 54;
		g.setPaint(new RadialGradientPaint(mx + md / 2f, my + md / 3f, md * 0.7f, new float[]{0, 1},
			new Color[]{Theme.mix(Theme.CARD_TOP, Theme.GOLD_DIM, 0.35), Theme.INK_DEEP}));
		g.fill(new Ellipse2D.Float(mx, my, md, md));
		g.setStroke(new BasicStroke(2f));
		g.setPaint(new GradientPaint(mx, my, Theme.GOLD_BRIGHT, mx, my + md, Theme.GOLD_DIM));
		g.draw(new Ellipse2D.Float(mx + 1, my + 1, md - 2, md - 2));
		g.setStroke(new BasicStroke(1f));
		g.setColor(Theme.alpha(Theme.GOLD, 70));
		g.draw(new Ellipse2D.Float(mx + 5, my + 5, md - 10, md - 10));
		Sigil.paint(g, mx + 11, my + 11, md - 22);

		int tx = mx + md + 12;
		int room = w - tx - 8;
		g.setFont(Theme.small());
		FontMetrics small = g.getFontMetrics();
		g.setColor(Theme.TEXT_MUTED);
		g.drawString(Ui.clip(heading, small, room), tx, 22);

		int top = 27;
		if (known)
		{
			int figureW = paintLarge(g, String.format("%,d", Math.round(count.get())), tx, top);
			g.setFont(Theme.small());
			g.setColor(Theme.GOLD_DIM);
			g.drawString("items known", tx + figureW + 4, top + 18);
		}
		else
		{
			g.setFont(Theme.title());
			Ui.shadowed(g, "Ask your bank", tx, top + 18, Theme.GOLD);
			g.setFont(Theme.small());
		}

		g.setColor(statusColor);
		g.drawString(Ui.clip(status, small, room), tx, top + 42);

		// The gilt rule under the crest, with a diamond at its middle.
		int y = h - 2;
		g.setPaint(new GradientPaint(0, 0, Theme.alpha(Theme.GOLD, 0), w / 2f, 0, Theme.alpha(Theme.GOLD, 150), true));
		g.drawLine(0, y, w, y);
		Path2D diamond = new Path2D.Float();
		diamond.moveTo(w / 2f, y - 3);
		diamond.lineTo(w / 2f + 3, y);
		diamond.lineTo(w / 2f, y + 3);
		diamond.lineTo(w / 2f - 3, y);
		diamond.closePath();
		g.setColor(Theme.GOLD);
		g.fill(diamond);
		g.dispose();
	}
}
