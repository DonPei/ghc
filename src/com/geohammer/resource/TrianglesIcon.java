package com.geohammer.resource;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Transparency;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.UIManager;

public class TrianglesIcon implements Icon, SwingConstants {
	private static final float DB = -.06f;
	private int direction;
	private int size;
	private Color color;
	private BufferedImage arrowImage;
	private int arrowCount;

	public TrianglesIcon(int direction) {
		this(1, direction, 10, null);
	}
	public TrianglesIcon(int direction, int size) {
		this(1, direction, size, null);
	}
	public TrianglesIcon(int direction, Color color) {
		this(1, direction, 10, color);
	}

	public TrianglesIcon(int direction, int size, Color color) {
		this.size = size;
		this.direction = direction;
		this.color = color;
	}

	public TrianglesIcon(int arrowCount, int direction, int size, Color color) {
		this.arrowCount = arrowCount;
		this.size = size;
		this.direction = direction;
		this.color = color;
	}
	public int getIconHeight() {        
		return size;
	}

	public int getIconWidth() {
		return size;
	}

	/** Returns the number of arrows. */
	public int getArrowCount() {
		return arrowCount;
	}

	/** Sets the number of arrows. */
	public void setArrowCount(int arrowCount) {
		this.arrowCount = arrowCount;
	}
	
	public void paintIcon(Component c, Graphics g, int x, int y) {
		g.drawImage(getArrowImage(), x, y, c);
	}
	public static BufferedImage createTranslucentImage(int width, int height) {

		return GraphicsEnvironment.getLocalGraphicsEnvironment().
				getDefaultScreenDevice().getDefaultConfiguration().createCompatibleImage(width, height, Transparency.TRANSLUCENT);

	}
	protected Image getArrowImage() {
		if (arrowImage == null) {
			arrowImage = createTranslucentImage(size, size);
			AffineTransform atx = direction != SOUTH? new AffineTransform() : null;
			switch(direction ) {
			case NORTH:
				atx.setToRotation(Math.PI, size/2, size/2);
				break;
			case EAST:
				atx.setToRotation(-(Math.PI/2), size/2, size/2);
				break;
			case WEST:
				atx.setToRotation(Math.PI/2, size/2, size/2);
			case SOUTH:
			default:{ /* no xform*/ }                   
			}       
			Graphics2D ig = (Graphics2D)arrowImage.getGraphics();
			if (atx != null) {
				ig.setTransform(atx);
			}
			int width = size;
			int height = size/2 + 1;
			int xx = (size - width)/2;
			int yy = (size - height + 1)/2;

			Color base = color != null? color : UIManager.getColor("controlDkShadow").darker(); 

			//paintArrowSimple(ig, base, xx, yy);
			//paintArrow(ig, base, xx, yy);
			//paintArrowBevel(ig, base, xx, yy);
			//paintArrowBevel(ig, deriveColorHSB(base, 0f, 0f, .20f), xx, yy + 1);
			
			paintArrow(ig, xx, yy, base);
		}
		return arrowImage;
	}
	
	boolean isEnabled() {
		return true;
	}
	private void paintArrow(Graphics g, int x, int y, Color highlight) {
		int mid, i, j;
		int arrowSize = size;

		Color oldColor = g.getColor();
		boolean isEnabled = isEnabled();

		j = 0;
		arrowSize = Math.max(arrowSize, 2);
		mid = (arrowSize / 2) - 1;

		g.translate(x, y);

		switch (direction) {
		case NORTH:
			for (i = 0; i < arrowSize; i++) {
				g.drawLine(mid - i, i, mid + i, i);
			}
			if(!isEnabled)  {
				g.setColor(highlight);
				g.drawLine(mid-i+2, i, mid+i, i);
			}
			break;
		case SOUTH:
			if (!isEnabled) {
				g.translate(1, 1);
				g.setColor(highlight);
				for (i = arrowSize - 1; i >= 0; i--) {
					g.drawLine(mid - i, j, mid + i, j);
					j++;
				}
				g.translate(-1, -1);
				g.setColor(oldColor);
			}
			j = 0;
			for (i = arrowSize - 1; i >= 0; i--) {
				g.drawLine(mid - i, j, mid + i, j);
				j++;
			}
			break;
		case WEST:
			for (i = 0; i < arrowSize; i++) {
				g.drawLine(i, mid - i, i, mid + i);
			}
			if(!isEnabled)  {
				g.setColor(highlight);
				g.drawLine(i, mid-i+2, i, mid+i);
			}
			break;
		case EAST:
			if(!isEnabled)  {
				g.translate(1, 1);
				g.setColor(highlight);
				for(i = arrowSize-1; i >= 0; i--)   {
					g.drawLine(j, mid-i, j, mid+i);
					j++;
				}
				g.translate(-1, -1);
				g.setColor(oldColor);
			}
			j = 0;
			for (i = arrowSize - 1; i >= 0; i--) {
				g.drawLine(j, mid - i, j, mid + i);
				j++;
			}
			break;
		}

		g.translate(-x, -y);
		g.setColor(oldColor);
	}
	
	protected void paintArrowSimple(Graphics2D g, Color base, int x, int y) {
		g.setColor(base);

		Path2D.Float arrowShape = new Path2D.Float();
		arrowShape.moveTo(x, y-1);
		System.out.println("moveTo "+(x)+","+(y-1));
		arrowShape.lineTo(size-1, y-1);
		System.out.println("lineTo "+(size-1)+","+(y-1));
		arrowShape.lineTo(size/2, y+(size/2));
		System.out.println("lineTo "+(size/2)+","+(y+(size/2)));
		arrowShape.lineTo(size/2 - 1, y+(size/2));
		System.out.println("lineTo "+ (size/2 - 1)+","+(y+(size/2)));
		arrowShape.lineTo(x, y-1);
		System.out.println("lineTo "+(x)+","+(y-1));
		//g.fill(arrowShape);
		g.draw(arrowShape);

		//		int len = size - 2;
		//		int xx = x;
		//		int yy = y-1;
		//		while (len >= 2) {
		//			xx++;
		//			yy++;
		//			g.fillRect(xx, yy, len, 1);
		//			len -= 2;
		//		}
	}

	protected void paintArrow(Graphics2D g, Color base, int x, int y) {
		g.setColor(base);

		//        Path2D.Float arrowShape = new Path2D.Float();
		//        arrowShape.moveTo(x, y-1);
		//        System.out.println("moveTo "+(x)+","+(y-1));
		//        arrowShape.lineTo(size-1, y-1);
		//        System.out.println("lineTo "+(size-1)+","+(y-1));
		//        arrowShape.lineTo(size/2, y+(size/2));
		//        System.out.println("lineTo "+(size/2)+","+(y+(size/2)));
		//        arrowShape.lineTo(size/2 - 1, y+(size/2));
		//        System.out.println("lineTo "+ (size/2 - 1)+","+(y+(size/2)));
		//        arrowShape.lineTo(x, y-1);
		//        System.out.println("lineTo "+(x)+","+(y-1));
		//        g.fill(arrowShape);

		int len = size - 2;
		int xx = x;
		int yy = y-1;
		while (len >= 2) {
			xx++;
			yy++;
			g.fillRect(xx, yy, len, 1);
			len -= 2;
		}
	}

	protected void paintArrowBevel(Graphics g, Color base, int x, int y) {
		int len = size;
		int xx = x;
		int yy = y;
		Color c2 = deriveColorHSB(base, 0f, 0f, (-DB)*(size/2));
		while (len >= 2) {
			c2 = deriveColorHSB(c2, 0f, 0f, DB);
			g.setColor(c2);
			g.fillRect(xx, yy, 1, 1);
			g.fillRect(xx + len - 1, yy, 1, 1);
			len -= 2;
			xx++;
			yy++;
		}

	}

	/**
	 * Derives a color by adding the specified offsets to the base color's 
	 * hue, saturation, and brightness values.   The resulting hue, saturation,
	 * and brightness values will be contrained to be between 0 and 1.
	 * @param base the color to which the HSV offsets will be added
	 * @param dH the offset for hue
	 * @param dS the offset for saturation
	 * @param dB the offset for brightness
	 * @return Color with modified HSV values
	 */
	public static Color deriveColorHSB(Color base, float dH, float dS, float dB) {
		float hsb[] = Color.RGBtoHSB(
				base.getRed(), base.getGreen(), base.getBlue(), null);

		hsb[0] += dH;
		hsb[1] += dS;
		hsb[2] += dB;
		return Color.getHSBColor(
				hsb[0] < 0? 0 : (hsb[0] > 1? 1 : hsb[0]),
						hsb[1] < 0? 0 : (hsb[1] > 1? 1 : hsb[1]),
								hsb[2] < 0? 0 : (hsb[2] > 1? 1 : hsb[2]));

	}
	
	public static void main(String args[]) {
		JFrame frame = new JFrame();
		JPanel panel = new JPanel();
		frame.add(panel);

		panel.add(new JLabel("north", new TrianglesIcon(SwingConstants.NORTH, 30, Color.RED), JLabel.CENTER));

		//panel.add(new JButton(new ArrowIconLine(20, SwingConstants.EAST)));

		//panel.add(getSystemIcons());
		frame.pack();
		frame.setVisible(true);
	}
}
