package com.semanticbanksearch.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.RoundRectangle2D;
import javax.swing.JComponent;

/** One bar split into coloured shares, each a part of the whole. */
public class ShareBar extends JComponent
{
	private final int[] counts;
	private final Color[] colors;

	public ShareBar(int[] counts, Color[] colors)
	{
		this.counts = counts;
		this.colors = colors;
		setOpaque(false);
		setPreferredSize(new Dimension(10, 8));
	}

	@Override
	protected void paintComponent(Graphics graphics)
	{
		Graphics2D g = (Graphics2D) graphics.create();
		Theme.smooth(g);
		int w = getWidth();
		int h = getHeight();
		RoundRectangle2D track = new RoundRectangle2D.Float(0, 0, w, h, h, h);
		g.setColor(Theme.INK_DEEP);
		g.fill(track);
		int total = 0;
		for (int c : counts)
		{
			total += c;
		}
		if (total > 0)
		{
			g.setClip(track);
			float x = 0;
			for (int i = 0; i < counts.length; i++)
			{
				float part = w * counts[i] / (float) total;
				g.setColor(colors[i]);
				g.fill(new java.awt.geom.Rectangle2D.Float(x, 0, part + 0.5f, h));
				x += part;
			}
			g.setClip(null);
		}
		g.setColor(Theme.STROKE_SOFT);
		g.draw(new RoundRectangle2D.Float(0.5f, 0.5f, w - 1, h - 1, h, h));
		g.dispose();
	}
}
