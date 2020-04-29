package com.geohammer.vc;

import java.awt.*;

import edu.mines.jtk.awt.ColorMap;
import edu.mines.jtk.mosaic.Projector;
import edu.mines.jtk.mosaic.TiledView;
import edu.mines.jtk.mosaic.Transcaler;
import edu.mines.jtk.mosaic.PixelsView.Interpolation;

import com.geohammer.core.planarmodel.Layer2D;
import com.geohammer.rt.RayPaths;

import edu.mines.jtk.util.Clips;
import static edu.mines.jtk.util.ArrayMath.*;

/**
 * A view of points (x1,x2) with marks at points and/or lines between them.
 * Points (x1,x2) may be specified as arrays x1 and x2 of coordinates. Each 
 * pair of arrays x1 and x2 corresponds to one plot segment. Multiple plot 
 * segments may be specified by arrays of arrays of x1 and x2 coordinates.
 * <p>
 * For each point (x1,x2), a mark with a specified style, size, and color
 * may be painted. Between each consecutive pair of points (x1,x2) within 
 * a plot segment, lines with specified style, width, and color may be 
 * painted.
 * <p>
 * For example, to view sampled functions x2 = sin(x1) and x2 = cos(x1),
 * one might construct two plot segments by specifying an array of two 
 * arrays of x1 coordinates and a corresponding array of two arrays of 
 * x2 coordinates.
 * <p>
 * Note that mark and line attributes are constant for each points view.
 * These attributes do not vary among plot segments. To paint marks and 
 * lines with different attributes, construct multiple views.
 * @author Dave Hale, Colorado School of Mines
 * @version 2005.12.28
 */
public class RaypathView extends TiledView {

	/**
	 * Orientation of axes x1 and x2. For example, the default orientation 
	 * X1RIGHT_X2UP corresponds to x1 increasing horizontally from left to 
	 * right, and x2 increasing vertically from bottom to top.
	 */
	public enum Orientation {
		X1RIGHT_X2UP,
		X1DOWN_X2RIGHT
	}

	/**
	 * The style of mark plotted at points (x1,x2).
	 * The default mark style is none.
	 */
	public enum Mark {
		NONE,
		POINT,
		PLUS,
		CROSS,
		ASTERISK,
		HOLLOW_CIRCLE,
		HOLLOW_SQUARE,
		FILLED_CIRCLE,
		FILLED_SQUARE,
	}

	/**
	 * The style of line plotted between consecutive points (x1,x2).
	 * The default line style is solid.
	 */
	public enum Line {
		NONE,
		SOLID,
		DASH,
		DOT,
		DASH_DOT,
	}

	public RayPaths _rayPaths 	= null;
	public float [] _rayX 		= null;
	public float [] _rayZ 		= null;
	public int [] 	_raySegIndex	= null;
	public int [] 	_raySegNumPoint	= null;

	public float [] _segX 		= null;
	public float [] _segY 		= null;
	public int [] 	_px			= null;
	public int [] 	_py			= null;

	//constants for this class
	double 	_INVALID_D		= -99999.0;
	float 	_INVALID_F		= -99999.0f;
	int 	_INVALID_I		= -99999;
	int 	_pixelTol 		= 6;		//pixels
	
	double _x1min = 0;
	double _x2min = 0;
	double _x1max = 0;
	double _x2max = 0;
	
	public RaypathView(float [] rayX, float [] rayZ, double x1min, double x2min, double x1max, double x2max) {
		_x1min = x1min;
		_x2min = x2min;
		_x1max = x1max;
		_x2max = x2max;
		set(rayX, rayZ);
	}
	public RaypathView(RayPaths rayPaths, double x1min, double x2min, double x1max, double x2max) {
		_x1min = x1min;
		_x2min = x2min;
		_x1max = x1max;
		_x2max = x2max;
		set(rayPaths);
	}
	public void set(RayPaths rayPaths) {
		_rayPaths = rayPaths; 
		init(rayPaths);
		_clips = new Clips[1];
		//_clips[0] = new Clips(_vel);
		updateBestProjectors();
		repaint();
	}

	public void init(RayPaths rayPaths) {
		int nSegments = rayPaths.getNumOfRayPaths();
		//System.out.println("nsegment="+nSegments);
		_raySegIndex 		= new int[nSegments];
		_raySegNumPoint 	= new int[nSegments];
		
		for(int i=0; i<nSegments; i++) {
			_raySegIndex[i] = 0;
			_raySegNumPoint[i] = rayPaths.getRayPath(i).getNumOfPoints();
		}
		_mxmax = -1;
		for(int i=0; i<_raySegIndex.length; i++) {
			_mxmax = _mxmax>_raySegNumPoint[i] ? _mxmax:_raySegNumPoint[i];
		}
		_px 		= new int[_mxmax];
		_py 		= new int[_mxmax];
		_segX 		= new float[_mxmax];
		_segY 		= new float[_mxmax];
	}
	public void set(float [] rayX, float [] rayZ) {
		_rayX = rayX; 
		_rayZ = rayZ; 
		init(rayX);
		_clips = new Clips[1];
		//_clips[0] = new Clips(_vel);
		updateBestProjectors();
		repaint();
	}

	public void init(float [] rayX) {
		int nSegments = getNumOfSegments(rayX);
		//System.out.println("nsegment="+nSegments);
		_raySegIndex 		= new int[nSegments];
		_raySegNumPoint 	= new int[nSegments];
		boolean begin = true;
		
		int k=0; 
		int iSegment=0; 
		for(int i=0; i<rayX.length; i++) {
			if(_rayX[i] != _INVALID_F) {
				begin = true;
				k++;
			} else {
				if(begin) {
					_raySegIndex[iSegment] = i-k;
					_raySegNumPoint[iSegment] = k;
					//System.out.println("index="+_raySegIndex[iSegment]+" index2="+_raySegNumPoint[iSegment]);
					iSegment++;
					begin = false;
					k = 0;
				}
			}
		}
		if(_rayX[_rayX.length-1] != _INVALID_F) {
			_raySegIndex[iSegment] = _rayX.length-k;
			_raySegNumPoint[iSegment] = k;
			//System.out.println("index3="+_raySegIndex[iSegment]+" index4="+_raySegNumPoint[iSegment]);
		}
		_mxmax = -rayX.length;
		for(int i=0; i<_raySegIndex.length; i++) {
			_mxmax = _mxmax>_raySegNumPoint[i] ? _mxmax:_raySegNumPoint[i];
		}
		_px 		= new int[_mxmax];
		_py 		= new int[_mxmax];
		_segX 		= new float[_mxmax];
		_segY 		= new float[_mxmax];
	}
	public int getNumOfSegments(float [] rayX) {
		int nSegments = 0;
		boolean begin = true;
		
		for(int i=0; i<rayX.length; i++) {
			if(_rayX[i] != _INVALID_F) {
				begin = true;
			} else {
				if(begin) {
					begin = false;
					nSegments++;
				}
			}
		}
		if(_rayX[_rayX.length-1] != _INVALID_F) {
			nSegments++;
		}
		return nSegments;
	}
	/**
	 * Sets the orientation of (x1,x2) axes.
	 * @param orientation the orientation.
	 */
	public void setOrientation(Orientation orientation) {
		if (_orientation!=orientation) {
			_orientation = orientation;
			updateBestProjectors();
			repaint();
		}
	}

	/**
	 * Gets the orientation of (x1,x2) axes.
	 * @return the orientation.
	 */
	public Orientation getOrientation() {
		return _orientation;
	}

	/**
	 * Sets the color, line style, and mark style from a style string.
	 * This method provides a convenient way to set the most commonly
	 * specified attributes of lines and marks painted by this view.
	 * <p>
	 * To specify a color, the style string may contain one of "r" for red,
	 * "g" for green, "b" for blue, "c" for cyan, "m" for magenta, "y" for
	 * yellow, "k" for black, or "w" for white. If the style string contains 
	 * none of these colors, then the default color is used.
	 * <p>
	 * To specify a line style, the style string may contain one of "-" for 
	 * solid lines, "--" for dashed lines, "-." for dotted lines, or "--."
	 * for dash-dotted lines. If the style string contains none of these
	 * line styles, then no lines are painted.
	 * <p>
	 * To specify a mark style, the style string may contain one of "." for
	 * point, "+" for plus, "x" for cross, "o" for hollow circle", "O" for
	 * filled circle, "s" for hollow square, or "S" for filled square. If
	 * the style string contains none of these mark styles, then no marks
	 * are painted.
	 * @param style the style string.
	 */
	public void setStyle(String style) {
		// Color.
		if (style.contains("r")) {
			setLineColor(Color.RED);
			setMarkColor(Color.RED);
		} else if (style.contains("g")) {
			setLineColor(Color.GREEN);
			setMarkColor(Color.GREEN);
		} else if (style.contains("b")) {
			setLineColor(Color.BLUE);
			setMarkColor(Color.BLUE);
		} else if (style.contains("c")) {
			setLineColor(Color.CYAN);
			setMarkColor(Color.CYAN);
		} else if (style.contains("m")) {
			setLineColor(Color.MAGENTA);
			setMarkColor(Color.MAGENTA);
		} else if (style.contains("y")) {
			setLineColor(Color.YELLOW);
			setMarkColor(Color.YELLOW);
		} else if (style.contains("k")) {
			setLineColor(Color.BLACK);
			setMarkColor(Color.BLACK);
		} else if (style.contains("w")) {
			setLineColor(Color.WHITE);
			setMarkColor(Color.WHITE);
		} else {
			setLineColor(null);
			setMarkColor(null);
		}

		// Line style.
		if (style.contains("--.")) {
			setLineStyle(Line.DASH_DOT);
		} else if (style.contains("--")) {
			setLineStyle(Line.DASH);
		} else if (style.contains("-.")) {
			setLineStyle(Line.DOT);
		} else if (style.contains("-")) {
			setLineStyle(Line.SOLID);
		} else {
			setLineStyle(Line.NONE);
		}

		// Mark style.
		if (style.contains("+")) {
			setMarkStyle(Mark.PLUS);
		} else if (style.contains("x")) {
			setMarkStyle(Mark.CROSS);
		} else if (style.contains("o")) {
			setMarkStyle(Mark.HOLLOW_CIRCLE);
		} else if (style.contains("O")) {
			setMarkStyle(Mark.FILLED_CIRCLE);
		} else if (style.contains("s")) {
			setMarkStyle(Mark.HOLLOW_SQUARE);
		} else if (style.contains("S")) {
			setMarkStyle(Mark.FILLED_SQUARE);
		} else if (style.contains(".")) {
			int i = style.indexOf(".");
			if (i==0 || style.charAt(i-1)!='-')
				setMarkStyle(Mark.POINT);
		} else {
			setMarkStyle(Mark.NONE);
		}
	}

	/**
	 * Sets the line style.
	 * The default style is solid.
	 * @param style the line style.
	 */
	public void setLineStyle(Line style) {
		_lineStyle = style;
		repaint();
	}

	/**
	 * Sets the line width.
	 * The default width is zero, for the thinnest lines.
	 * @param width the line width.
	 */
	public void setLineWidth(float width) {
		if (_lineWidth!=width) {
			_lineWidth = width;
			updateBestProjectors();
			repaint();
		}
	}
	public void doubleLineWidth() {
		_lineWidth += _lineWidth;
		updateBestProjectors();
		repaint();
	}

	/**
	 * Sets the line color.
	 * The default line color is the tile foreground color. 
	 * That default is used if the specified line color is null.
	 * @param color the line color; null, for tile foreground color.
	 */
	public void setLineColor(Color color) {
		if (!equalColors(_lineColor,color)) {
			_lineColor = color;
			repaint();
		}
	}

	/**
	 * Sets the mark style.
	 * The default mark style is none, for no marks.
	 * @param style the mark style.
	 */
	public void setMarkStyle(Mark style) {
		if (_markStyle!=style) {
			_markStyle = style;
			updateBestProjectors();
			repaint();
		}
	}

	/**
	 * Sets the mark size.
	 * The default mark size is half the tile font size.
	 * The default is used if the specified mark size is zero.
	 * @param size the mark size.
	 */
	public void setMarkSize(float size) {
		if (_markSize!=size) {
			_markSize = size;
			updateBestProjectors();
			repaint();
		}
	}

	public void doubleMarkSize() {
		_markSize += _markSize;
		updateBestProjectors();
		repaint();
	}

	/**
	 * Sets the mark color.
	 * The default mark color is the tile foreground color. 
	 * That default is used if the specified mark color is null.
	 * @param color the mark color.
	 */
	public void setMarkColor(Color color) {
		if (!equalColors(_markColor,color)) {
			_markColor = color;
			repaint();
		}
	}

	/**
	 * Sets the format used for text labels.
	 * The default format is "%1.4G".
	 * @param format the text format.
	 */
	public void setTextFormat(String format) {
		_textFormat = format;
		repaint();
	}

	public ColorMap getColorMap() {
		return _colorMap;
	}
	/**
	 * Gets the color corresponding to the specified value.
	 * @param v the value to be mapped to a color.
	 * @return the color.
	 */
	public Color getColor(double v) {
		return getColorMap().getColor(v);
	}

	public int getTileHeight() {
		return getTile().getHeight();
	}

	public Color getLineColor() {
		return _lineColor;
	}
	public Line getLineStyle() {
		return _lineStyle;
	}
	public float getLineWidth() {
		return _lineWidth;
	}
	public Color getMarkColor() {
		return _markColor;
	}
	public float getMarkSize() {
		return _markSize;
	}
	public Mark getMarkStyle() {
		return _markStyle;
	}

	public int getNumOfItem(){
		return 3;
	}

	public void paint(Graphics2D g2d) {
		if(!isVisible()) return;

		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		Projector hp = getHorizontalProjector();
		Projector vp = getVerticalProjector();
		Transcaler ts = getTranscaler();

		// Font size and line width from graphics context.
		float fontSize = g2d.getFont().getSize2D();
		float lineWidth = 1.0f;
		Stroke stroke = g2d.getStroke();
		if (stroke instanceof BasicStroke) {
			BasicStroke bs = (BasicStroke)stroke;
			lineWidth = bs.getLineWidth();
		}

		// Graphics context for lines.
		Graphics2D gline 			= null;
		if (_lineStyle!=Line.NONE) {
			gline = (Graphics2D)g2d.create();
			float width = lineWidth;
			//_lineWidth = 0.5f;
			if (_lineWidth>0.0f)
				width *= _lineWidth;
			float[] dash = null;
			if (_lineStyle!=Line.SOLID) {
				float dotLength = 0.5f*width;
				float dashLength = 2.0f*width;
				float gapLength = 2.0f*dotLength+dashLength;
				if (_lineStyle==Line.DASH) {
					dash = new float[]{dashLength,gapLength};
				} else if (_lineStyle==Line.DOT) {
					dash = new float[]{dotLength,gapLength};
				} else if (_lineStyle==Line.DASH_DOT) {
					dash = new float[]{dashLength,gapLength,dotLength,gapLength};
				}
			}
			BasicStroke bs;
			if (dash!=null) {
				int cap = BasicStroke.CAP_ROUND;
				int join = BasicStroke.JOIN_ROUND;
				float miter = 10.0f;
				float phase = 0.0f;
				bs = new BasicStroke(width,cap,join,miter,dash,phase);
			} else {
				bs = new BasicStroke(width);
			}
			gline.setStroke(bs);
			if (_lineColor!=null)
				gline.setColor(_lineColor);
		}
		
		Graphics2D gmark = null;
		int markSize = round(fontSize/2.0f);
		if (_markStyle!=Mark.NONE) {
			gmark = (Graphics2D)g2d.create();
			if (_markSize>=0.0f)
				markSize = round(_markSize*lineWidth);
			if (_markColor!=null)
				gmark.setColor(_markColor);
			float width = lineWidth;
			if (_lineWidth!=0.0f)
				width *= _lineWidth;
			BasicStroke bs = new BasicStroke(width);
			gmark.setStroke(bs);
		}
		gmark = null;
		
		// Arrays for (x,y) coordinates.
		int[] x = new int[_mxmax];
		int[] y = new int[_mxmax];

		Rectangle clipRect = new Rectangle();
		g2d.getClipBounds(clipRect);

		// For all plot segments, ...
		for(int i=0; i<_raySegIndex.length; i++) {
			int n = _raySegNumPoint[i];
			if(_rayX!=null) {
				for(int j=0, k=0; j<n; j++) {
					k = _raySegIndex[i]+j;
					_segX[j] = _rayX[k];
					_segY[j] = _rayZ[k];
				}
			} else {				
				for(int j=0; j<n; j++) {
					_segX[j] = (float)_rayPaths.getRayPath(i).getN(j);
					_segY[j] = (float)_rayPaths.getRayPath(i).getD(j);
				}	
			}

			computeXY(hp,vp,ts,n,_segY,_segX,_px,_py);

			// Draw lines between consecutive points.
			if (gline!=null) {
				gline.drawPolyline(_px,_py,n);
			}
			if (gmark!=null) {
				if (_markStyle==Mark.POINT) {
					paintPoint(gmark,n,_px,_py);
				} else if (_markStyle==Mark.PLUS) {
					paintPlus(gmark,markSize,n,_px,_py);
				} else if (_markStyle==Mark.CROSS) {
					paintCross(gmark,markSize,n,_px,_py);
				} else if (_markStyle==Mark.FILLED_CIRCLE) {
					paintFilledCircle(gmark,markSize,n,_px,_py);
				} else if (_markStyle==Mark.HOLLOW_CIRCLE) {
					paintHollowCircle(gmark,markSize,n,_px,_py);
				} else if (_markStyle==Mark.FILLED_SQUARE) {
					paintFilledSquare(gmark,markSize,n,_px,_py);
				} else if (_markStyle==Mark.HOLLOW_SQUARE) {
					paintHollowSquare(gmark,markSize,n,_px,_py);
				}
			}
		}
	}


///////////////////////////////////////////////////////////////////////////
// private
private int _mxmax = -1;
private Orientation _orientation = Orientation.X1RIGHT_X2UP;
private Line _lineStyle = Line.SOLID;
private float _lineWidth = 1.0f;
private Color _lineColor = Color.BLACK;
//private Mark _markStyle = Mark.HOLLOW_CIRCLE;
private Mark _markStyle = Mark.FILLED_SQUARE;
private float _markSize = 8.0f;
private Color _markColor = Color.BLACK;
private String _textFormat = "%1.4G";

// Interpolation method.
private Interpolation _interpolation = Interpolation.LINEAR;

// Clips, one for each component.
Clips[] _clips 			= null;
float[] _clipMin		= null;
float[] _clipMax 		= null;

// Color map with default gray color model.
private ColorMap _colorMap = new ColorMap(ColorMap.JET);

/**
 * Called when we might new realignment.
 */
private void updateBestProjectors() {
	// Min and max (x1,x2) values.
	double x1min = _x1min;
	double x2min = _x2min;
	double x1max = _x1max;
	double x2max = _x2max;

	// Assume mark sizes and line widths less than 2% of plot dimensions.
	// The goal is to avoid clipping big marks and wide lines. The problem
	// is that mark sizes and line widths are specified in screen pixels
	// (or points), but margins u0 and u1 are specified in normalized 
	// coordinates, fractions of our tile's width and height. Here, we do 
	// not know those dimensions.
	double u0 = 0.0;
	double u1 = 1.0;
	//		if (_markStyle!=Mark.NONE || _lineWidth>1.0f) {
	//			u0 = 0.01;
	//			u1 = 0.99;
	//		}

	// Best projectors.
	Projector bhp = null;
	Projector bvp = null;
	if (_orientation==Orientation.X1RIGHT_X2UP) {
		bhp = (x1min<x1max)?new Projector(x1min,x1max,u0,u1):null;
		bvp = (x2min<x2max)?new Projector(x2max,x2min,u0,u1):null;
	} else if (_orientation==Orientation.X1DOWN_X2RIGHT) {
		bhp = (x2min<x2max)?new Projector(x2min,x2max,u0,u1):null;
		bvp = (x1min<x1max)?new Projector(x1min,x1max,u0,u1):null;
	}
	setBestProjectors(bhp,bvp);
}

private boolean equalColors(Color ca, Color cb) {
	return (ca==null)?cb==null:ca.equals(cb);
}

private void computeXY(
		Projector hp, Projector vp, Transcaler ts,
		int n, float[] x1, float[] x2, int[] x, int[] y) 
{
	ts = ts.combineWith(hp,vp);
	float[] xv = null;
	float[] yv = null;
	if (_orientation==Orientation.X1RIGHT_X2UP) {
		xv = x1;
		yv = x2;
	} else if (_orientation==Orientation.X1DOWN_X2RIGHT) {
		xv = x2;
		yv = x1;
	}
	for (int i=0; i<n; ++i) {
		x[i] = ts.x(xv[i]);
		y[i] = ts.y(yv[i]);
	}
}

private void paintLines(Graphics2D g2d, int n, int[] x, int[] y) {
	int x1 = x[0];
	int y1 = y[0];
	for (int i=1; i<n; ++i) {
		int x2 = x[i];
		int y2 = y[i];
		g2d.drawLine(x1,y1,x2,y2);
		x1 = x2;
		y1 = y2;
	}
}
private void paintPoint(Graphics2D g2d, int n, int[] x, int[] y) {
	for (int i=0; i<n; ++i) {
		int xi = x[i];
		int yi = y[i];
		g2d.drawLine(xi,yi,xi,yi);
	}
}

private void paintPlus(Graphics2D g2d, int s, int n, int[] x, int[] y) {
	int wh = 2*(s/2);
	int xy = wh/2;
	for (int i=0; i<n; ++i) {
		int xi = x[i];
		int yi = y[i];
		g2d.drawLine(xi-xy,yi,xi+xy,yi);
		g2d.drawLine(xi,yi-xy,xi,yi+xy);
	}
}

private void paintCross(Graphics2D g2d, int s, int n, int[] x, int[] y) {
	int wh = 2*(s/2);
	int xy = wh/2;
	for (int i=0; i<n; ++i) {
		int xi = x[i];
		int yi = y[i];
		g2d.drawLine(xi-xy,yi-xy,xi+xy,yi+xy);
		g2d.drawLine(xi+xy,yi-xy,xi-xy,yi+xy);
	}
}

private void paintFilledCircle(Graphics2D g2d, int s, int n, int[] x, int[] y) {
	int wh = 1+2*(s/2);
	int xy = wh/2;
	for (int i=0; i<n; ++i)
		g2d.fillOval(x[i]-xy,y[i]-xy,wh,wh);
}

private void paintHollowCircle(Graphics2D g2d, int s, int n, int[] x, int[] y) 	{
	int wh = 1+2*(s/2);
	int xy = wh/2;
	for (int i=0; i<n; ++i)
		g2d.drawOval(x[i]-xy,y[i]-xy,wh-1,wh-1);
}

private void paintFilledSquare(Graphics2D g2d, int s, int n, int[] x, int[] y) {
	int wh = 1+2*(s/2);
	int xy = wh/2;
	for (int i=0; i<n; ++i)
		g2d.fillRect(x[i]-xy,y[i]-xy,wh,wh);
}

private void paintHollowSquare(Graphics2D g2d, int s, int n, int[] x, int[] y) {
	int wh = 1+2*(s/2);
	int xy = wh/2;
	for (int i=0; i<n; ++i)
		g2d.drawRect(x[i]-xy,y[i]-xy,wh-1,wh-1);
}



}

