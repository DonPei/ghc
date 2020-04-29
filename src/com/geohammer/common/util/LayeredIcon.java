package com.geohammer.common.util;

/*
 *  LayeredIcon.java
 *  2006-08-09
 */

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Insets;
import java.util.ArrayList;

import javax.swing.Icon;
import javax.swing.SwingConstants;

/**
 * @author Christopher Bach
 */
public class LayeredIcon implements Icon {

  public static final int TOP = SwingConstants.TOP, LEFT = SwingConstants.LEFT,
      BOTTOM = SwingConstants.BOTTOM, RIGHT = SwingConstants.RIGHT, CENTER = SwingConstants.CENTER;

  private Dimension ourPrefSize = new Dimension(0, 0);

  private ArrayList ourLayers = new ArrayList();

  private Insets ourPadding = new Insets(0, 0, 0, 0);

  /**
   * 
   */
  public LayeredIcon() {

  }

  /**
   * 
   */
  public LayeredIcon(int padding) {
    setPadding(padding, padding, padding, padding);
  }

  /**
   * Creates a new instance of LayeredIcon with the specified width and height,
   * but no graphic.
   */
  public LayeredIcon(int width, int height) {
    setIconSize(width, height);
  }

  /**
   * 
   */
  public LayeredIcon(int width, int height, int padding) {
    setIconSize(width, height);
    setPadding(padding, padding, padding, padding);
  }

  /**
   * 
   */
  public LayeredIcon(int topPadding, int leftPadding, int bottomPadding, int rightPadding) {
    setPadding(topPadding, leftPadding, bottomPadding, rightPadding);
  }

  /**
   * 
   */
  public LayeredIcon(int width, int height, int topPadding, int leftPadding, int bottomPadding,
      int rightPadding) {
    setIconSize(width, height);
    setPadding(topPadding, leftPadding, bottomPadding, rightPadding);
  }

  /**
   * If any client Icons have been added and painting is enabled, paints the
   * client Icons at the specified coordinates into the provided graphics
   * context for the indicated Component.
   */
  public void paintIcon(Component c, Graphics g, int x, int y) {
    Dimension iconSize = getIconSize();

    for (int i = 0, size = ourLayers.size(); i < size; i++) {
      Layer layer = (Layer) ourLayers.get(i);

      if (layer.painted) {
        int w = layer.icon.getIconWidth();
        int h = layer.icon.getIconHeight();

        int dx = (layer.halign == LEFT ? 0 : iconSize.width - w);
        if (layer.halign != RIGHT)
          dx = dx / 2;
        dx += ourPadding.left;

        int dy = (layer.valign == TOP ? 0 : iconSize.height - h);
        if (layer.valign != BOTTOM)
          dy = dy / 2;
        dy += ourPadding.top;

        layer.icon.paintIcon(c, g, x + dx, y + dy);
      }
    }
  }

  /**
   * Returns the width of the LayeredIcon. If any client Icons have been added,
   * returns the max width of all client Icons. Otherwise, returns the
   * LayeredIcon's explicit width.
   */
  public int getIconWidth() {
    return getIconSize().width + ourPadding.left + ourPadding.right;
  }

  /**
   * Returns the height of the LayeredIcon. If any client Icons have been added,
   * returns the max height of all client Icons. Otherwise, returns the
   * LayeredIcon's explicit height.
   */
  public int getIconHeight() {
    return getIconSize().height + ourPadding.top + ourPadding.bottom;
  }

  /**
   * Sets the explicit size of the LayeredIcon to be used when no client Icons
   * have been added.
   */
  public void setIconSize(int width, int height) {
    ourPrefSize.width = width;
    ourPrefSize.height = height;
  }

  /**
   * 
   */
  public void setPadding(int padding) {
    ourPadding.top = padding;
    ourPadding.left = padding;
    ourPadding.bottom = padding;
    ourPadding.right = padding;
  }

  /**
   * 
   */
  public void setPadding(int topPadding, int leftPadding, int bottomPadding, int rightPadding) {
    ourPadding.top = topPadding;
    ourPadding.left = leftPadding;
    ourPadding.bottom = bottomPadding;
    ourPadding.right = rightPadding;
  }

  /**
   * 
   */
  public Insets getPadding() {
    return new Insets(ourPadding.top, ourPadding.left, ourPadding.bottom, ourPadding.right);
  }

  /**
   * 
   */
  public void addIcon(Icon icon) {
    if (icon != null) {
      ourLayers.add(new Layer(icon));
    }
  }

  /**
   * 
   */
  public void addIcon(Icon icon, int index) {
    if (icon != null && index >= 0 && index <= ourLayers.size()) {
      ourLayers.add(index, new Layer(icon));
    }
  }

  /**
   * 
   */
  public void addIcon(Icon icon, int halign, int valign) {
    if (icon != null) {
      ourLayers.add(new Layer(icon, checkHAlign(halign), checkVAlign(valign)));
    }
  }

  /**
   * 
   */
  public void addIcon(Icon icon, int index, int halign, int valign) {
    if (icon != null && index >= 0 && index <= ourLayers.size()) {
      ourLayers.add(index, new Layer(icon, checkHAlign(halign), checkVAlign(valign)));
    }
  }

  /**
   * 
   */
  public void removeIcon(Icon icon) {
    Layer layer = getLayer(icon);

    if (layer != null) {
      ourLayers.remove(layer);
    }
  }

  /**
   * 
   */
  public void clear() {
    ourLayers.clear();
  }

  /**
   * 
   */
  public Icon getIcon(int index) {
    Layer layer = (Layer) ourLayers.get(index);
    if (layer != null)
      return layer.icon;
    else
      return null;
  }

  /**
   * 
   */
  public int indexOf(Icon icon) {
    for (int i = 0, size = ourLayers.size(); i < size; i++) {
      Layer layer = (Layer) ourLayers.get(i);
      if (layer.icon == icon)
        return i;
    }

    return -1;
  }

  /**
   * 
   */
  public int iconCount() {
    return ourLayers.size();
  }

  /**
   * 
   */
  public boolean isIconPainted(int iconIndex) {
    Layer layer = (Layer) ourLayers.get(iconIndex);
    if (layer != null)
      return layer.painted;
    else
      return false;
  }

  /**
   * 
   */
  public void setIconPainted(int iconIndex, boolean painted) {
    Layer layer = (Layer) ourLayers.get(iconIndex);
    if (layer != null)
      layer.painted = painted;
  }

  /**
   * 
   */
  public boolean isIconPainted(Icon icon) {
    Layer layer = getLayer(icon);
    if (layer != null)
      return layer.painted;
    else
      return false;
  }

  /**
   * 
   */
  public void setIconPainted(Icon icon, boolean painted) {
    Layer layer = getLayer(icon);
    if (layer != null)
      layer.painted = painted;
  }

  /**
   * 
   */
  public void setIconAlignment(Icon icon, int halign, int valign) {
    Layer layer = getLayer(icon);

    if (layer != null) {
      layer.halign = checkHAlign(halign);
      layer.valign = checkVAlign(valign);
    }
  }

  /**
   * 
   */
  public void setIconAlignment(int iconIndex, int halign, int valign) {
    Layer layer = (Layer) ourLayers.get(iconIndex);

    if (layer != null) {
      layer.halign = checkHAlign(halign);
      layer.valign = checkVAlign(valign);
    }
  }

  /**
   * 
   */
  public int getIconHAlignment(Icon icon) {
    Layer layer = getLayer(icon);

    if (layer != null)
      return layer.halign;
    else
      return CENTER;
  }

  /**
   * 
   */
  public int getIconVAlignment(Icon icon) {
    Layer layer = getLayer(icon);

    if (layer != null)
      return layer.valign;
    else
      return CENTER;
  }

  /**
   * 
   */
  public int getIconHAlignment(int iconIndex) {
    Layer layer = (Layer) ourLayers.get(iconIndex);

    if (layer != null)
      return layer.halign;
    else
      return CENTER;
  }

  /**
   * 
   */
  public int getIconVAlignment(int iconIndex) {
    Layer layer = (Layer) ourLayers.get(iconIndex);

    if (layer != null)
      return layer.valign;
    else
      return CENTER;
  }

  /**
   * 
   */
  private int checkHAlign(int halign) {
    if (halign != LEFT && halign != RIGHT && halign != CENTER)
      return CENTER;
    else
      return halign;
  }

  /**
   * 
   */
  private int checkVAlign(int valign) {
    if (valign != TOP && valign != BOTTOM && valign != CENTER)
      return CENTER;
    else
      return valign;
  }

  /**
   * 
   */
  private Layer getLayer(Icon icon) {
    for (int i = 0, size = ourLayers.size(); i < size; i++) {
      Layer layer = (Layer) ourLayers.get(i);
      if (layer.icon == icon)
        return layer;
    }

    return null;
  }

  /**
   * 
   */
  private Dimension getIconSize() {
    if (ourLayers.size() == 0)
      return ourPrefSize;

    Dimension d = new Dimension(0, 0);

    for (int i = 0, size = ourLayers.size(); i < size; i++) {
      Layer layer = (Layer) ourLayers.get(i);
      d.height = Math.max(d.height, layer.icon.getIconHeight());
      d.width = Math.max(d.width, layer.icon.getIconWidth());
    }

    return d;
  }

  /**
   * 
   */
  private class Layer {
    public Icon icon = null;

    public boolean painted = true;

    public int halign = CENTER;

    public int valign = CENTER;

    public Layer() {
    }

    public Layer(Icon icon) {
      this.icon = icon;
    }

    public Layer(Icon icon, int halign, int valign) {
      this.icon = icon;
      this.halign = halign;
      this.valign = valign;
    }
  }

}
