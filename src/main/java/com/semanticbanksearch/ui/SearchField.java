package com.semanticbanksearch.ui;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.RoundRectangle2D;
import java.util.function.Consumer;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 * The question box: an inked well with a small glass at the left, a gilt rim while it has
 * focus and a cross to clear it. It answers as you type, after a short pause.
 */
public class SearchField extends JPanel
{
	private static final int HEIGHT = 32;

	private final JTextField field = new JTextField();
	private final Timer debounce;
	private final Consumer<String> onChange;
	private String placeholder = "";
	private boolean focused;
	private boolean clearHover;

	public SearchField(Consumer<String> onChange)
	{
		super(new BorderLayout());
		this.onChange = onChange;
		setOpaque(false);
		setBorder(new EmptyBorder(0, 26, 0, 24));
		setPreferredSize(new Dimension(10, HEIGHT));
		setMinimumSize(new Dimension(10, HEIGHT));

		field.setOpaque(false);
		field.setBorder(new EmptyBorder(0, 0, 0, 0));
		field.setFont(Theme.body());
		field.setForeground(Theme.TEXT);
		field.setCaretColor(Theme.GOLD_BRIGHT);
		field.setSelectionColor(Theme.alpha(Theme.GOLD, 90));
		field.setSelectedTextColor(Theme.TEXT);
		add(field, BorderLayout.CENTER);

		debounce = new Timer(180, e -> onChange.accept(getText()));
		debounce.setRepeats(false);
		field.getDocument().addDocumentListener(new DocumentListener()
		{
			@Override
			public void insertUpdate(DocumentEvent e)
			{
				changed();
			}

			@Override
			public void removeUpdate(DocumentEvent e)
			{
				changed();
			}

			@Override
			public void changedUpdate(DocumentEvent e)
			{
				changed();
			}
		});
		field.addKeyListener(new KeyAdapter()
		{
			@Override
			public void keyPressed(KeyEvent e)
			{
				if (e.getKeyCode() == KeyEvent.VK_ENTER)
				{
					debounce.stop();
					onChange.accept(getText());
				}
				else if (e.getKeyCode() == KeyEvent.VK_ESCAPE)
				{
					setText("");
				}
			}
		});
		field.addFocusListener(new FocusAdapter()
		{
			@Override
			public void focusGained(FocusEvent e)
			{
				focused = true;
				repaint();
			}

			@Override
			public void focusLost(FocusEvent e)
			{
				focused = false;
				repaint();
			}
		});

		MouseAdapter clear = new MouseAdapter()
		{
			@Override
			public void mouseMoved(MouseEvent e)
			{
				boolean over = overClear(e.getX());
				if (over != clearHover)
				{
					clearHover = over;
					setCursor(Cursor.getPredefinedCursor(over ? Cursor.HAND_CURSOR : Cursor.DEFAULT_CURSOR));
					repaint();
				}
			}

			@Override
			public void mouseExited(MouseEvent e)
			{
				clearHover = false;
				repaint();
			}

			@Override
			public void mousePressed(MouseEvent e)
			{
				if (overClear(e.getX()) && !getText().isEmpty())
				{
					setText("");
					field.requestFocusInWindow();
				}
				else
				{
					field.requestFocusInWindow();
				}
			}
		};
		addMouseListener(clear);
		addMouseMotionListener(clear);
	}

	private void changed()
	{
		repaint();
		debounce.restart();
	}

	private boolean overClear(int x)
	{
		return x >= getWidth() - 24;
	}

	public String getText()
	{
		return field.getText();
	}

	/** Sets the text and answers at once, as a click on a suggestion should. */
	public void setText(String text)
	{
		field.setText(text);
		field.setCaretPosition(field.getText().length());
		debounce.stop();
		onChange.accept(getText());
	}

	public void setPlaceholder(String placeholder)
	{
		this.placeholder = placeholder;
		repaint();
	}

	public JComponent field()
	{
		return field;
	}

	@Override
	protected void paintComponent(Graphics graphics)
	{
		Graphics2D g = (Graphics2D) graphics.create();
		Theme.smooth(g);
		int w = getWidth();
		int h = getHeight();
		RoundRectangle2D well = new RoundRectangle2D.Float(0.5f, 0.5f, w - 1, h - 1, 9, 9);
		g.setPaint(new GradientPaint(0, 0, Theme.INK_DEEP, 0, h, Theme.mix(Theme.INK_DEEP, Theme.CARD, 0.5)));
		g.fill(well);
		g.setColor(focused ? Theme.alpha(Theme.GOLD, 170) : Theme.STROKE);
		g.draw(well);
		if (focused)
		{
			g.setColor(Theme.alpha(Theme.GOLD, 30));
			g.draw(new RoundRectangle2D.Float(1.5f, 1.5f, w - 3, h - 3, 8, 8));
		}

		// The small glass.
		g.setStroke(new BasicStroke(1.4f));
		g.setColor(focused ? Theme.GOLD : Theme.GOLD_DIM);
		g.draw(new Ellipse2D.Float(9, h / 2f - 6, 9, 9));
		g.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
		g.draw(new Line2D.Float(16.5f, h / 2f + 1.5f, 20, h / 2f + 5));

		if (field.getText().isEmpty() && placeholder != null)
		{
			g.setFont(Theme.body());
			g.setColor(Theme.TEXT_FAINT);
			int base = (h + g.getFontMetrics().getAscent()) / 2 - 2;
			g.drawString(Ui.clip(placeholder, g.getFontMetrics(), w - 52), 26, base);
		}
		else if (!field.getText().isEmpty())
		{
			float cx = w - 13;
			float cy = h / 2f;
			g.setColor(clearHover ? Theme.alpha(Theme.GOLD, 50) : Theme.alpha(Theme.GOLD, 18));
			g.fill(new Ellipse2D.Float(cx - 7, cy - 7, 14, 14));
			g.setStroke(new BasicStroke(1.4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
			g.setColor(clearHover ? Theme.GOLD_BRIGHT : Theme.GOLD);
			g.draw(new Line2D.Float(cx - 3, cy - 3, cx + 3, cy + 3));
			g.draw(new Line2D.Float(cx + 3, cy - 3, cx - 3, cy + 3));
		}
		g.dispose();
	}
}
