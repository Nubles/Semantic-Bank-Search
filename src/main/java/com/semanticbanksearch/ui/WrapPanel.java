package com.semanticbanksearch.ui;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.LayoutManager;
import javax.swing.JPanel;

/** Lays its children out in rows like words in a line, wrapping when the panel's width runs out. */
public class WrapPanel extends JPanel
{
	private static final int FALLBACK_WIDTH = 209;

	public WrapPanel(int gap)
	{
		super(null);
		setOpaque(false);
		setLayout(new Wrap(gap));
	}

	private static final class Wrap implements LayoutManager
	{
		private final int gap;

		Wrap(int gap)
		{
			this.gap = gap;
		}

		@Override
		public void addLayoutComponent(String name, Component comp)
		{
		}

		@Override
		public void removeLayoutComponent(Component comp)
		{
		}

		@Override
		public Dimension preferredLayoutSize(Container parent)
		{
			int width = parent.getWidth() > 0 ? parent.getWidth() : FALLBACK_WIDTH;
			return new Dimension(width, place(parent, width, false));
		}

		@Override
		public Dimension minimumLayoutSize(Container parent)
		{
			return preferredLayoutSize(parent);
		}

		@Override
		public void layoutContainer(Container parent)
		{
			place(parent, parent.getWidth(), true);
		}

		/** Places or measures the children; returns the height used. */
		private int place(Container parent, int width, boolean apply)
		{
			Insets in = parent.getInsets();
			int room = width - in.left - in.right;
			int x = 0;
			int y = in.top;
			int row = 0;
			for (Component c : parent.getComponents())
			{
				Dimension d = c.getPreferredSize();
				int w = Math.min(d.width, room);
				if (x > 0 && x + w > room)
				{
					x = 0;
					y += row + gap;
					row = 0;
				}
				if (apply)
				{
					c.setBounds(in.left + x, y, w, d.height);
				}
				x += w + gap;
				row = Math.max(row, d.height);
			}
			return y + row + in.bottom;
		}
	}
}
