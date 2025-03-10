package com.geohammer.common.util;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;

import javax.swing.Icon;

/**
 * An icon for painting a square swatch of a specified Color.
 * 
 * @author Christopher Bach
 */
public class ColorSwatch implements Icon {

	private Color ourSwatchColor = Color.white;

	private Color ourBorderColor = Color.black;

	private boolean ourBorderPainted = true;

	private boolean ourSwatchIsMultiColor = false;

	private boolean ourSwatchIsVoid = false;

	private int ourSwatchSize = 14;
	private int ourSwatchSizeY = -1;

	/**
	 * Creates a standard 14 x 14 swatch with a black border and white background.
	 */
	public ColorSwatch() {

	}

	/**
	 * Creates a swatch of the specified size with a black border and white
	 * background.
	 */
	public ColorSwatch(int size) {
		setSwatchSize(size);
	}

	/**
	 * Creates a swatch of the specified size with a black border and white
	 * background and determines whether or n not the border should be painted.
	 */
	public ColorSwatch(int size, boolean borderPainted) {
		setSwatchSize(size);
		setBorderPainted(borderPainted);
	}

	/**
	 * 
	 */
	public ColorSwatch(Color color) {
		setColor(color);
	}

	/**
	 * 
	 */
	public ColorSwatch(int size, Color color) {
		setSwatchSize(size);
		setColor(color);
	}
	
	public ColorSwatch(int size, int sizeY, Color color) {
		this(size, color);
		setSwatchSizeY(sizeY);
	}

	/**
	 * 
	 */
	public ColorSwatch(int size, Color color, Color borderColor) {
		setSwatchSize(size);
		setColor(color);
		setBorderColor(borderColor);
		setBorderPainted(true);
	}

	/**
	 * Sets the size of this swatch.
	 */
	public void setSwatchSize(int size) {
		if (size > 0)
			ourSwatchSize = size;
		else
			ourSwatchSize = 14;
	}
	public void setSwatchSizeY(int sizeY) {
		if (sizeY > 0)
			ourSwatchSizeY = sizeY;
		else
			ourSwatchSizeY = -1;
	}

	/**
	 * Returns the size of this swatch.
	 */
	public int getSwatchSize() {
		return ourSwatchSize;
	}
	public int getSwatchSizeY() {
		return ourSwatchSizeY;
	}

	/**
	 * Determines whether or not this swatch's border should be painted.
	 */
	public void setBorderPainted(boolean borderPainted) {
		ourBorderPainted = borderPainted;
	}

	/**
	 * Returns whether or not this swatch's border is painted.
	 */
	public boolean isBorderPainted() {
		return ourBorderPainted;
	}

	/**
	 * Sets the color of this swatch's border.
	 */
	public void setBorderColor(Color color) {
		ourBorderColor = color;
	}

	/**
	 * Returns the color of this swatch's border.
	 */
	public Color getBorderColor() {
		return ourBorderColor;
	}

	/**
	 * Sets the color that this swatch represents.
	 */
	public void setColor(Color color) {
		ourSwatchIsMultiColor = false;
		ourSwatchColor = color;
	}

	/**
	 * Returns the color that this swatch represents.
	 */
	public Color getColor() {
		return ourSwatchColor;
	}

	/**
	 * Sets this swatch to represent more than one color.
	 */
	public void setMultiColor() {
		ourSwatchIsMultiColor = true;
	}

	/**
	 * Returns whether or not this swatch represents more than one color.
	 */
	public boolean isMultiColor() {
		return ourSwatchIsMultiColor;
	}

	/**
	 * Determines whether or not this swatch is void. If the swatch is void, it
	 * will not be painted at all.
	 */
	public void setVoid(boolean isVoid) {
		// When true, this icon will not be painted at all.
		ourSwatchIsVoid = isVoid;
	}

	/**
	 * Returns whether this swatch is void. If the swatch is void, it will not be
	 * painted at all.
	 */
	public boolean isVoid() {
		return ourSwatchIsVoid;
	}

	// // Icon implementation ////

	/**
	 * Returns the width of this Icon.
	 */
	public int getIconWidth() {
		return ourSwatchSize;
	}

	/**
	 * Returns the height of this Icon.
	 */
	public int getIconHeight() {
		if(ourSwatchSizeY>0) return ourSwatchSizeY;
		else return ourSwatchSize;
	}

	/**
	 * Paints this Icon into the provided graphics context.
	 */
	public void paintIcon(Component c, Graphics g, int x, int y) {
		if (ourSwatchIsVoid)
			return;

		int w = ourSwatchSize;
		int h = ourSwatchSize;
		if(ourSwatchSizeY>0) h = ourSwatchSizeY;
		Color oldColor = g.getColor();

		if (ourSwatchIsMultiColor) {
			g.setColor(Color.white);
			g.fillRect(x, y, w, h);
			g.setColor(ourBorderColor);
			for (int i = 0; i <h; i += 2) {
				g.drawLine(x + i, y, x + i, y + h);
			}
		}

		else if (ourSwatchColor != null) {
			g.setColor(ourSwatchColor);
			g.fillRect(x, y, w, h);
		}

		else {
			g.setColor(Color.white);
			g.fillRect(x, y, w, h);
			g.setColor(ourBorderColor);
			g.drawLine(x, y, x + w, y + h);
			g.drawLine(x, y + h, x + w, y);
		}

		if (ourBorderPainted) {
			g.setColor(ourBorderColor);
			g.drawRect(x, y, w, h);
		}

		g.setColor(oldColor);
	}

}
