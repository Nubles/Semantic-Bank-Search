package com.semanticbanksearch.ui;

import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import javax.swing.JComponent;

/** A suggested question, set like a small engraved token; clicking it asks the question. */
public class Chip extends JComponent
{
	private final String text;
	private boolean hover;

	public Chip(String text, Runnable action)
	{
		this.text = text;
		setFont(Theme.small());
		setOpaque(false);
		setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		setPreferredSize(new Dimension(getFontMetrics(getFont()).stringWidth(text) + 18, 22));
		setToolTipText("Search for \"" + text + "\"");
		addMouseListener(new MouseAdapter()
		{
			@Override
			public void mouseEntered(MouseEvent e)
			{
				hover = true;
				repaint();
			}

			@Override
			public void mouseExited(MouseEvent e)
			{
				hover = false;
				repaint();
			}

			@Override
			public void mouseReleased(MouseEvent e)
			{
				if (contains(e.getPoint()))
				{
					action.run();
				}
			}
		});
	}

	@Override
	protected void paintComponent(Graphics graphics)
	{
		Graphics2D g = (Graphics2D) graphics.create();
		Theme.smooth(g);
		int w = getWidth();
		int h = getHeight();
		RoundRectangle2D shape = new RoundRectangle2D.Float(0.5f, 0.5f, w - 1, h - 1, h - 2, h - 2);
		g.setPaint(new GradientPaint(0, 0, hover ? Theme.CARD_HOVER : Theme.CARD_TOP, 0, h, Theme.CARD));
		g.fill(shape);
		g.setColor(hover ? Theme.alpha(Theme.GOLD, 150) : Theme.alpha(Theme.GOLD, 55));
		g.draw(shape);
		g.setFont(getFont());
		FontMetrics fm = g.getFontMetrics();
		String shown = Ui.clip(text, fm, w - 14);
		g.setColor(hover ? Theme.GOLD_BRIGHT : Theme.mix(Theme.TEXT, Theme.GOLD, 0.35));
		g.drawString(shown, (w - fm.stringWidth(shown)) / 2, (h + fm.getAscent()) / 2 - 2);
		g.dispose();
	}
}
