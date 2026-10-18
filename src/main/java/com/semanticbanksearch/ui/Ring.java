package com.semanticbanksearch.ui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import javax.swing.JComponent;

/** A gilt progress ring with the count inside: how much of a trip's kit the bank already holds. */
public class Ring extends JComponent
{
	private final Animator fill = new Animator(this);
	private final int size;
	private String label = "";
	private double target;

	public Ring(int size)
	{
		this.size = size;
		setOpaque(false);
		Dimension d = new Dimension(size, size);
		setPreferredSize(d);
		setMinimumSize(d);
		setMaximumSize(d);
	}

	public void set(int have, int need)
	{
		target = need <= 0 ? 1 : have / (double) need;
		label = have + "/" + need;
		fill.set(target, true);
		repaint();
	}

	@Override
	protected void paintComponent(Graphics graphics)
	{
		Graphics2D g = (Graphics2D) graphics.create();
		paint(g, 0, 0, size, fill.get(), target, label);
		g.dispose();
	}

	/** Draws a ring in a size by size box; cards that are not components use it directly. */
	public static void paint(Graphics2D g, int x, int y, int size, double shown, double progress, String label)
	{
		Theme.smooth(g);
		float stroke = Math.max(3f, size / 9f);
		float inset = stroke / 2 + 1;
		Ellipse2D track = new Ellipse2D.Float(x + inset, y + inset, size - inset * 2, size - inset * 2);
		g.setColor(Theme.INK_DEEP);
		g.fill(new Ellipse2D.Float(x, y, size, size));
		g.setStroke(new BasicStroke(stroke));
		g.setColor(Theme.STROKE_SOFT);
		g.draw(track);
		Color color = Theme.progressColor(progress);
		double sweep = 360 * Math.max(0, Math.min(1, shown));
		if (sweep > 0.5)
		{
			g.setStroke(new BasicStroke(stroke, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
			g.setColor(Theme.alpha(color, 70));
			g.setStroke(new BasicStroke(stroke + 2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
			g.draw(new Arc2D.Double(track.getX(), track.getY(), track.getWidth(), track.getHeight(), 90, -sweep, Arc2D.OPEN));
			g.setStroke(new BasicStroke(stroke, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
			g.setColor(color);
			g.draw(new Arc2D.Double(track.getX(), track.getY(), track.getWidth(), track.getHeight(), 90, -sweep, Arc2D.OPEN));
		}
		g.setFont(size >= 40 ? Theme.title() : Theme.small());
		FontMetrics fm = g.getFontMetrics();
		Ui.shadowed(g, label, x + (size - fm.stringWidth(label)) / 2, y + (size + fm.getAscent()) / 2 - 2,
			progress >= 1 ? Theme.GREEN : Theme.TEXT);
	}
}
