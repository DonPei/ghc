package com.geohammer.common.util;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.Transparency;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.UIManager;


import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Graphics;
import java.awt.Polygon;
import java.awt.SystemColor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.colorchooser.AbstractColorChooserPanel;
import javax.swing.colorchooser.ColorSelectionModel;

/**
 *
 * @author Administrator
 */
public class ArrowIcon implements Icon, SwingConstants {
	private static final float DB = -.06f;
	private int direction;
	private int size;
	private Color color;
	private BufferedImage arrowImage;

	public ArrowIcon(int direction) {
		this(direction, 10, null);
	}
	public ArrowIcon(int direction, int size) {
		this(direction, size, null);
	}
	public ArrowIcon(int direction, Color color) {
		this(direction, 10, color);
	}

	public ArrowIcon(int direction, int size, Color color) {
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
			paintArrow(ig, base, xx, yy);
			paintArrowBevel(ig, base, xx, yy);
			paintArrowBevel(ig, deriveColorHSB(base, 0f, 0f, .20f), xx, yy + 1);
		}
		return arrowImage;
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

	public static JPanel getSystemIcons() {
		String [] systemIcons = new String[] {"OptionPane.questionIcon" ,
				"OptionPane.errorIcon" ,
				"OptionPane.informationIcon" ,
				"OptionPane.warningIcon" ,
				"FileView.directoryIcon", 
				"FileView.fileIcon" ,
				"FileView.computerIcon" ,
				"FileView.hardDriveIcon" ,
				"FileView.floppyDriveIcon" ,
				"FileChooser.newFolderIcon" ,

				"FileChooser.upFolderIcon" ,
				"FileChooser.homeFolderIcon" ,
				"FileChooser.detailsViewIcon" ,
				"FileChooser.listViewIcon", 
				"Tree.expandedIcon" ,
				"Tree.collapsedIcon" ,
				"Tree.openIcon", 
				"Tree.leafIcon", 
		"Tree.closedIcon" };

		int nRow = systemIcons.length;
		JPanel panel = new JPanel();
		panel.setLayout(new GridLayout(nRow, 2));
		for(int i=0; i<nRow; i++) {
			JButton button = new JButton(UIManager.getIcon(systemIcons[i]));
			JLabel label = new JLabel(systemIcons[i]);
			panel.add(button);
			panel.add(label);
		}

		return panel;

	}

	class DiamondIcon implements Icon {
		private Color color;

		private boolean selected;

		private int width;

		private int height;

		private Polygon poly;

		private static final int DEFAULT_WIDTH = 10;

		private static final int DEFAULT_HEIGHT = 10;

		public DiamondIcon(Color color) {
			this(color, true, DEFAULT_WIDTH, DEFAULT_HEIGHT);
		}

		public DiamondIcon(Color color, boolean selected) {
			this(color, selected, DEFAULT_WIDTH, DEFAULT_HEIGHT);
		}

		public DiamondIcon(Color color, boolean selected, int width, int height) {
			this.color = color;
			this.selected = selected;
			this.width = width;
			this.height = height;
			initPolygon();
		}

		private void initPolygon() {
			poly = new Polygon();
			int halfWidth = width / 2;
			int halfHeight = height / 2;
			poly.addPoint(0, halfHeight);
			poly.addPoint(halfWidth, 0);
			poly.addPoint(width, halfHeight);
			poly.addPoint(halfWidth, height);
		}

		public int getIconHeight() {
			return height;
		}

		public int getIconWidth() {
			return width;
		}

		public void paintIcon(Component c, Graphics g, int x, int y) {
			g.setColor(color);
			g.translate(x, y);
			if (selected) {
				g.fillPolygon(poly);
			} else {
				g.drawPolygon(poly);
			}
			g.translate(-x, -y);
		}
	}

	public static class ArrowButton extends JButton {

		/** The cardinal direction of the arrow(s). */
		private int direction;

		/** The number of arrows. */
		private int arrowCount;

		/** The arrow size. */
		private int arrowSize;

		public ArrowButton(int direction, int arrowCount, int arrowSize) {
			setMargin(new Insets(0, 2, 0, 2));
			setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
			this.direction = direction;
			this.arrowCount = arrowCount;
			this.arrowSize = arrowSize;
		}

		/** Returns the cardinal direction of the arrow(s).
		 * @see #setDirection(int)
		 */
		public int getDirection() {
			return direction;
		}

		/** Sets the cardinal direction of the arrow(s).
		 * @param direction the direction of the arrow(s), can be SwingConstants.NORTH,
		 * SwingConstants.SOUTH, SwingConstants.WEST or SwingConstants.EAST
		 * @see #getDirection()
		 */
		public void setDirection(int direction) {
			this.direction = direction;
		}

		/** Returns the number of arrows. */
		public int getArrowCount() {
			return arrowCount;
		}

		/** Sets the number of arrows. */
		public void setArrowCount(int arrowCount) {
			this.arrowCount = arrowCount;
		}

		/** Returns the arrow size. */
		public int getArrowSize() {
			return arrowSize;
		}

		/** Sets the arrow size. */
		public void setArrowSize(int arrowSize) {
			this.arrowSize = arrowSize;
		}

		public Dimension getPreferredSize() {
			return getMinimumSize();
		}

		public Dimension getMinimumSize() {
			return new Dimension(
					arrowSize * (direction == SwingConstants.EAST
					|| direction == SwingConstants.WEST ? arrowCount : 3)
					+ getBorder().getBorderInsets(this).left
					+ getBorder().getBorderInsets(this).right
					,
					arrowSize * (direction == SwingConstants.NORTH
					|| direction == SwingConstants.SOUTH ? arrowCount : 3)
					+ getBorder().getBorderInsets(this).top
					+ getBorder().getBorderInsets(this).bottom
					);
		}

		public Dimension getMaximumSize() {
			return getMinimumSize();
		}

		protected void paintComponent(Graphics g) {
			// this will paint the background
			super.paintComponent(g);

			Color oldColor = g.getColor();
			g.setColor(isEnabled() ? getForeground() : getForeground().brighter());

			// paint the arrows
			int w = getSize().width;
			int h = getSize().height;
			for (int i = 0; i < arrowCount; i++) {
				paintArrow(g,
						(w - arrowSize * (direction == SwingConstants.EAST
						|| direction == SwingConstants.WEST ? arrowCount : 1)) / 2
						+ arrowSize * (direction == SwingConstants.EAST
						|| direction == SwingConstants.WEST ? i : 0),
						(h - arrowSize * (direction == SwingConstants.EAST
						|| direction == SwingConstants.WEST ? 1 : arrowCount)) / 2
						+ arrowSize * (direction == SwingConstants.EAST
						|| direction == SwingConstants.WEST ? 0 : i),
						g.getColor());
			}

			g.setColor(oldColor);
		}

		private void paintArrow(Graphics g, int x, int y, Color highlight) {
			int mid, i, j;

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
	}


	// Arrow class to draw straight line segments with arrow heads.
	// Arrow.java
	// import java.awt.geom.Point2D;

	/** Arrow class with graphic paint method, implemented as a JComponent
	 *
	 *  @author <a href="mailto:bergmann@rowan.edu">Seth Bergmann and Greg Safko </a>
	 *
	 */
	public static class Arrow1 extends JComponent {

		/** x1,y1 is first endpoint of this arrow.
		 *  x2,y2 is second endpoint of this arrow.
		 *  color is the color of this arrow, default is black.
		 */
		public double x1,y1,x2,y2;		// endpoints of this Arrow
		Color color;				// color of this Arrow

		/** arrowhead's angle with respect to the shaft
		 */
		public static final double angle = Math.PI/10;	// arrowhead angle to shaft
		/** length of arrowhead
		 */
		public static final double len = 10;		// arrowhead length

		/** Create an arrow with given endpoints
		 *  @param xOne x coordinate of first endpoint
		 *  @param yOne y coordinate of first endpoint
		 *  @param xTwo x coordinate of second endpoint
		 *  @param yTwo y coordinate of second endpoint
		 */
		public Arrow1 (double xOne, double yOne, double xTwo, double yTwo)
		{  x1 = xOne;
		x2 = xTwo;
		y1 = yOne;
		y2 = yTwo;
		}

		/** Create an arrow with given endpoints and given color
		 *  @param xOne x coordinate of first endpoint
		 *  @param yOne y coordinate of first endpoint
		 *  @param xTwo x coordinate of second endpoint
		 *  @param yTwo y coordinate of second endpoint
		 *  @param c color of this arrow
		 */
		public Arrow1 (double xOne, double yOne, double xTwo, double yTwo, Color c)
		{  x1 = xOne;
		x2 = xTwo;
		y1 = yOne;
		y2 = yTwo;
		color = c;
		}

		/** Private fields needed for arrowhead endpoints   
		 *
		 */
		double ax1,ay1, ax2, ay2;		// coordinates of arrowhead endpoints

		/** Display this arrow on the given Graphics object g.
		 *
		 */
		public void paint (Graphics g)
		{ super.paint (g);
		Graphics2D graphics2D = (Graphics2D) g;
		Color oldColor;
		oldColor = graphics2D.getColor();
		graphics2D.setColor (color);

		// paint the shaft
		graphics2D.draw (new Line2D.Double (x1,y1,x2,y2));

		// paint arrowhead
		arrHead (x1,y1,x2,y2);
		graphics2D.draw (new Line2D.Double (x2,y2,ax1,ay1));
		graphics2D.draw (new Line2D.Double (x2,y2,ax2,ay2));
		graphics2D.setColor (oldColor);
		}

		/**  Compute the arrowhead endpoints
		 *  arrow is pointing from x1,y1 to x2,y2
		 *  angle to shaft is angle.
		 *  length of arrowhead is len.
		 *  return endpoints in ax1, ay1, ax2, ay2.
		 */
		private void arrHead (double x1, double y1, double x2, double y2)
		{  double c,a,beta,theta,phi;
		c = Math.sqrt ((x2-x1)*(x2-x1) + (y2-y1)*(y2-y1));
		if (Math.abs(x2-x1) < 1e-6)
			if (y2<y1) theta = Math.PI/2;
			else theta = - Math.PI/2; 
		else
		{ if (x2>x1)
			theta = Math.atan ((y1-y2)/(x2-x1)) ;
		else
			theta = Math.atan ((y1-y2)/(x1-x2));
		}
		a = Math.sqrt (len*len  + c*c - 2*len*c*Math.cos(angle));
		beta = Math.asin (len*Math.sin(angle)/a);
		phi = theta - beta;
		ay1 = y1 - a * Math.sin(phi);		// coordinates of arrowhead endpoint
		if (x2>x1) 
			ax1 = x1 + a * Math.cos(phi);
		else
			ax1 = x1 - a * Math.cos(phi);
		phi = theta + beta;				// second arrowhead endpoint
		ay2 = y1 - a * Math.sin(phi);
		if (x2>x1)
			ax2 = x1 + a * Math.cos(phi);
		else 
			ax2 = x1 - a * Math.cos(phi);
		}

	}

	/**
	 * This pluggable utility paints either a "classic" or a "sleek" filled arrow 
	 * on a given edge. To use, create an instance of the Arrow object
	 * with your preferred thickness, and then call 
	 * arrow.drawArrow( graphics, source_x1, source_y1, dest_x, dest_y2 ) for the edge.
	 * 
	 * Note that the arrow simply uses the color currently set in the graphics context.
	 * 
	 * @author Jon Froehlich
	 */
	class Arrow2  extends JComponent{

		public static final String  CLASSIC = "Arrow.CLASSIC";
		public static final String  SLEEK   = "Arrow.SLEEK";
		
		protected String m_arrowType;
		protected int m_arrowLength = 4;
		protected int m_arrowWidth  = 10;
		protected Stroke m_arrowStroke;
		
		public Arrow2(String type, int length, int width){
			m_arrowType = type;
			if(length>0){
				m_arrowLength = length;
			}
			
			if(width>0){
				m_arrowWidth = width;
			}
			
			m_arrowStroke = new BasicStroke(2);
			
			if(this.m_arrowType == SLEEK){
				arrowhead = getSleekArrow();
			}else{
				arrowhead = getClassicArrow();
			}

		}

		GeneralPath arrowhead;

	    
//	    public void drawArrow(Graphics2D g2d, double x1, double y1, 
//	        double x2, double y2, Shape vertex, boolean directed)
//	    {
//	        double theta = Math.atan2((y1 - y2), (x1 - x2)) + Math.PI;
	//
//	        // calculate offset from center of vertex bounding box;
//	        // create coordinates for source and dest centered at dest 
//	        // (since vertex shape will be centered at dest)
//	        Coordinates source = new Coordinates(x1-x2, y1-y2);
//	        Coordinates dest = new Coordinates(0,0);
//	        Coordinates c = CoordinateUtil.getClosestIntersection(source, dest, vertex.getBounds2D());
//	        if (c == null) // can happen if source and dest are the same
//	            return;
//	        double bounding_box_offset = CoordinateUtil.distance(c, dest);
	//
//	        // transform arrowhead into dest coordinate space
//	        AffineTransform at = new AffineTransform();
//	        at.translate(x2, y2);
//	        if (directed)
//	            theta += Math.atan2(SettableRenderer.CONTROL_OFFSET, 
//	                                CoordinateUtil.distance(source,dest)/2);
//	        at.rotate(theta);
//	        at.translate(-bounding_box_offset, 0);
	//
//	        // draw the arrowhead
//	        Stroke previous = g2d.getStroke();
//	        g2d.setStroke(this.m_arrowStroke);
//	        g2d.fill(at.createTransformedShape(arrowhead));
//	        g2d.setStroke(previous);
//	    }
	    
		public void drawArrow(Graphics2D g2d, int sourceX, int sourceY, int destX, int destY, int vertexDiam){
		    Stroke oldStroke = g2d.getStroke();
			g2d.setStroke(this.m_arrowStroke);
			Point point1 = new Point(sourceX, sourceY);
			Point point2 = new Point(destX, destY);
			
			// get angle of line from 0 - 360
			double thetaRadians = Math.atan2(( point1.getY() - point2.getY()),(point1.getX() -
					point2.getX()))+Math.PI;
			
//			float distance = (float) point1.distance(point2)-vertexDiam/2.0f;
			AffineTransform at = new AffineTransform();
			at.translate(point2.getX() , point2.getY() );
			at.rotate(thetaRadians);
			at.translate( - vertexDiam / 2.0f, 0 );
			Shape arrow = at.createTransformedShape(arrowhead);
			g2d.fill(arrow);
			g2d.setStroke(oldStroke);
		}
		
		protected GeneralPath getSleekArrow(){
			GeneralPath arrow = new GeneralPath();
//			float distance = 0;
//			(float) point1.distance(point2)-vertexDiam/2.0f;
			// create arrowed line general path
			int width = (int) (m_arrowWidth/2.0f);
			arrow.moveTo( 0, 0);
			arrow.lineTo( (- m_arrowLength), width);
			arrow.lineTo( (- m_arrowLength) , -width);
			arrow.lineTo( 0, 0 );
			return arrow;
		}
		
		protected GeneralPath getClassicArrow(){
			GeneralPath arrow = new GeneralPath();
//			float distance = (float) point1.distance(point2)-vertexDiam/2.0f;
			float distance = 0;
			// create arrowed line general path
			int width = (int) (m_arrowWidth/2.0f);
			arrow.moveTo( distance , 0);
			arrow.lineTo( (distance - m_arrowLength), width);
			arrow.lineTo( (distance - m_arrowLength) , -width);
			arrow.lineTo( distance , 0 );
			return arrow;
		}
	}

	public static void main(String args[]) {
		JFrame frame = new JFrame();
		JPanel panel = new JPanel();
		frame.add(panel);

		panel.add(new JLabel("north", new ArrowIcon(ArrowIcon.NORTH), JLabel.CENTER));
		panel.add(new ArrowButton(SwingConstants.EAST, 2, 10));
		panel.add(new Arrow1 (0, 0, 100, 100));
		panel.add(new JLabel("north", new ArrowIcon(ArrowIcon.NORTH), JLabel.CENTER));
		//panel.add(new Arrow2 (Arrow2.CLASSIC, 100, 100));
		//		panel.add(new JLabel("east", new ArrowIcon(ArrowIcon.EAST), JLabel.CENTER));
		//		panel.add(new JLabel("east-20", new ArrowIcon(ArrowIcon.EAST, 20, Color.blue), JLabel.CENTER));
		//		
		//		panel.add(new JLabel("east", new ColorSwatch(40, Color.RED), JLabel.CENTER));
		//		panel.add(new JLabel("east", new LayeredIcon(20, 20,50), JLabel.CENTER));

		panel.add(new JButton(new ArrowIconLine(20, SwingConstants.EAST)));

		panel.add(getSystemIcons());
		frame.pack();
		frame.setVisible(true);
	}

}
