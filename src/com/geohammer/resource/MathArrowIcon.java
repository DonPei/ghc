package com.geohammer.resource;

import java.awt.BasicStroke;
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

public class MathArrowIcon implements Icon, SwingConstants {
	private static final float DB = -.06f;
	private int direction;
	private int sizeW;
	private int sizeH;
	private Color color;
	private int lineWidth;
	private int style;
	private double headSize;
	private double arrowAngle;
	private int type;
	private BufferedImage arrowImage;

	public MathArrowIcon(int direction, int sizeW, int sizeH, int style, int type, double headSize, 
			double arrowAngle, int lineWidth, Color color) {
		this.sizeW = sizeW;
		this.sizeH = sizeH;
		this.direction = direction;
		this.color = color;
		this.lineWidth = lineWidth;
		this.style = style;
		this.type = type;
		this.headSize = headSize;
		this.arrowAngle = arrowAngle;
	}

	public int getIconHeight() {        
		return sizeH;
	}

	public int getIconWidth() {
		return sizeW;
	}

	public void paintIcon(Component c, Graphics g, int x, int y) {
		g.drawImage(getArrowImage(), x, y, c);
	}
	public static BufferedImage createTranslucentImage(int width, int height) {
		return GraphicsEnvironment.getLocalGraphicsEnvironment().
				getDefaultScreenDevice().getDefaultConfiguration().createCompatibleImage(
						width, height, Transparency.TRANSLUCENT);
	}
	protected Image getArrowImage() {
		if (arrowImage != null) return arrowImage;
		arrowImage = createTranslucentImage(sizeW, sizeH);
    
		Graphics2D ig = (Graphics2D)arrowImage.getGraphics();

		Line2 [] lines = new Line2[4];
		for(int i=0; i<lines.length; i++) {
			lines[i] = new Line2();
		}

		double max = (sizeW>sizeH? sizeW: sizeH);
		if(type==1) { //simple single arrow full range
			switch(direction ) {
			case NORTH:
				lines[0].set(0, sizeH/max, 0, -sizeH/max);
				break;
			case EAST:
				lines[1].set(-sizeW/max, 0, sizeW/max, 0);
				break;
			case WEST:
				lines[2].set(sizeW/max, 0, -sizeW/max, 0);
				break;
			case SOUTH:
				lines[3].set(0, -sizeH/max, 0, sizeH/max);
				break;
			default:{ /* no xform*/ }                   
			}
		}
		if(type==2) {//simple single arrow starting from the center
			switch(direction ) {
			case NORTH:
				lines[0].set(0, 0, 0, sizeH/max);
				break;
			case EAST:
				lines[1].set(0, 0, sizeW/max, 0);
				break;
			case WEST:
				lines[2].set(0, 0, -sizeW/max, 0);
				break;
			case SOUTH:
				lines[3].set(0, 0, 0, -sizeH/max);
				break;
			default:{ /* no xform*/ }                   
			}
		}
		if(type==3) { //double arrow head at both ends
			switch(direction ) {
			case NORTH:
				lines[0].set(0, 0, 0, -sizeH/max);
				lines[3].set(0, 0, 0, sizeH/max);
				break;
			case EAST:
				lines[1].set(0, 0, sizeW/max, 0);
				lines[2].set(0, 0, -sizeW/max, 0);
				break;
			default:{ /* no xform*/ }                   
			}
		}
		if(type==4) { //double arrow head to the center
			switch(direction ) {
			case NORTH:
				lines[0].set(0, -sizeH/max, 0, -5/max);
				lines[3].set(0, sizeH/max, 0, 5/max);
				break;
			case EAST:
				lines[1].set(sizeW/max, 0, 5/max, 0);
				lines[2].set(-sizeW/max, 0, -5/max, 0);
				break;   
			}
		}
		if(type==5) { //double arrow head for Y axis 45 degree
			switch(direction ) {
			case NORTH:
				lines[0].set(-sizeW/max, -sizeH/max, 0, -5/max);
				lines[3].set(sizeW/max, sizeH/max, 0, 5/max);
				break;
			case EAST:
				lines[1].set(0, 0, sizeW/max, sizeH/max);
				lines[2].set(0, 0, -sizeW/max, -sizeH/max);
				break;
			default:{ /* no xform*/ }                   
			}
		}
		if(type==6) { //coordinate system
			lines[3].set(0, sizeH/max-5/max, 0, -sizeH/max);  // south
			lines[1].set(0, sizeH/max-5/max, sizeW/max, sizeH/max-5/max); // east
			lines[2].set(0, sizeH/max-5/max, -sizeW/max, 0); // Y-axis
		}
		if(type==7) { //simple single arrow both heads
			lines[0].set(0, sizeH/max, 0, -sizeH/max);
			lines[3].set(0, -sizeH/max, 0, sizeH/max);
			lines[1].set(-sizeW/max, 0, sizeW/max, 0);
			lines[2].set(sizeW/max, 0, -sizeW/max, 0);
		}
		if(type==8) { //simple single arrow both heads
			switch(direction ) {
			case NORTH:
				lines[0].set(0, sizeH/max, 0, -sizeH/max);
				lines[3].set(0, -sizeH/max, 0, sizeH/max);
				break;
			case EAST:
				lines[1].set(-sizeW/max, 0, sizeW/max, 0);
				lines[2].set(sizeW/max, 0, -sizeW/max, 0);
				break;
			default:{ /* no xform*/ }                   
			}
		}
		 

		paintArrow(ig, lines, arrowImage.getWidth(), arrowImage.getHeight());

		return arrowImage;
	}
	
	public void paintArrow(Graphics g, Line2 [] line, int w, int h) {
		// world/screen transformation
		Vec2.xoffset = w/2;
		Vec2.yoffset = h/2;
		int resx = w/2;
		int resy = h/2;
		Vec2.Resolution = (resx>resy? resx: resy);
		// Here are our objects
		//for (int i=0; i<line.length; i++) line[i].draw(g);
		for (int i=0; i<line.length; i++) {
			if(line[i].length()<0.0001) continue;
			if(headSize>0) {
				Arrow.draw(line[i], g, headSize, style, arrowAngle, lineWidth, color);
			} else {
				Graphics2D g2 = (Graphics2D)g;
				g2.setColor(color);
				g2.setStroke(new BasicStroke(lineWidth));
				line[i].draw(g2);
			}
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

		int direction = SwingConstants.EAST;
		int sizeW = 30;
		int sizeH = 30;
		int style = 3;
		int type = 8;
		double headSize = -1.0;
		double arrowAngle = 3.0;
		int lineWidth = 10;
		Color color = Color.red;
		
		panel.add(new JLabel(new MathArrowIcon(direction, sizeW, sizeH, 
				style, type, headSize, arrowAngle, lineWidth, color)));

		//panel.add(new JButton(new ArrowIconLine(20, SwingConstants.EAST)));

		//panel.add(getSystemIcons());
		frame.pack();
		frame.setVisible(true);
	}
}
