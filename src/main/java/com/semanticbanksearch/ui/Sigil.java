package com.semanticbanksearch.ui;

import java.awt.BasicStroke;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;

/**
 * The mark: a gilt reading glass held over the dark, with a teal spark of understanding in the
 * lens. It stands for searching by what an item is for, not by what it is called.
 */
public final class Sigil
{
	private Sigil()
	{
	}

	public static BufferedImage icon(int size)
	{
		BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = image.createGraphics();
		Theme.smooth(g);
		paint(g, 0, 0, size);
		g.dispose();
		return image;
	}

	/** Draws the mark in a size by size box. */
	public static void paint(Graphics2D g, float x, float y, float size)
	{
		float s = size / 16f;

		// The handle, angled down to the right, under the lens.
		Line2D handle = new Line2D.Float(x + 10.4f * s, y + 10.4f * s, x + 14.6f * s, y + 14.6f * s);
		g.setStroke(new BasicStroke(Math.max(2.6f, 3.0f * s), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
		g.setColor(Theme.INK_DEEP);
		g.draw(handle);
		g.setStroke(new BasicStroke(Math.max(1.6f, 2.0f * s), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
		g.setPaint(new GradientPaint(x + 10 * s, y + 10 * s, Theme.GOLD, x + 15 * s, y + 15 * s, Theme.GOLD_DIM));
		g.draw(handle);

		// The glass: deep ink with a breath of teal, catching light at its upper left.
		float cx = x + 6.8f * s;
		float cy = y + 6.8f * s;
		float r = 5.4f * s;
		Ellipse2D glass = new Ellipse2D.Float(cx - r, cy - r, r * 2, r * 2);
		g.setPaint(new GradientPaint(cx - r, cy - r, Theme.mix(Theme.INK_DEEP, Theme.TEAL, 0.28f), cx + r, cy + r, Theme.INK_DEEP));
		g.fill(glass);

		// The spark: a four-pointed star.
		float sr = 2.9f * s;
		float in = 0.75f * s;
		Path2D star = new Path2D.Float();
		star.moveTo(cx, cy - sr);
		star.lineTo(cx + in, cy - in);
		star.lineTo(cx + sr, cy);
		star.lineTo(cx + in, cy + in);
		star.lineTo(cx, cy + sr);
		star.lineTo(cx - in, cy + in);
		star.lineTo(cx - sr, cy);
		star.lineTo(cx - in, cy - in);
		star.closePath();
		g.setColor(Theme.alpha(Theme.TEAL, 80));
		g.fill(new Ellipse2D.Float(cx - sr * 0.9f, cy - sr * 0.9f, sr * 1.8f, sr * 1.8f));
		g.setColor(Theme.mix(Theme.TEAL, Theme.TEXT, 0.45));
		g.fill(star);

		// The gilt rim.
		g.setStroke(new BasicStroke(Math.max(1.3f, 1.5f * s)));
		g.setPaint(new GradientPaint(cx - r, cy - r, Theme.GOLD_BRIGHT, cx + r, cy + r, Theme.GOLD_DIM));
		g.draw(glass);
		g.setStroke(new BasicStroke(Math.max(0.7f, 0.6f * s), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
		g.setColor(Theme.alpha(Theme.TEXT, 120));
		g.draw(new java.awt.geom.Arc2D.Float(cx - r + 1.6f * s, cy - r + 1.6f * s, (r - 1.6f * s) * 2, (r - 1.6f * s) * 2,
			110, 60, java.awt.geom.Arc2D.OPEN));
	}
}
