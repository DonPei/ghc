package com.geohammer.resource;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics; 
import java.awt.Graphics2D;

/**
 *  Arrow describes a two-dimensional Arrow.
 *
 */

public class Arrow extends Line2 {

	public static final int AH_OUTLINE = 1;
	public static final int AH_OPEN = 2;
	public static final int AH_FILLED = 3;
	private static final double std_size = 0.3;

	private double arrow_size = std_size;
	private int arrow_type = AH_FILLED;

	public Arrow() { super(1.0,0.0); }
	public Arrow(Vec2 a, Vec2 b) { super(a,b); }
	public Arrow(double x1, double y1, double x2, double y2) {
		super(x1,y1,x2,y2);
	}

	public int getArrowHead() { return arrow_type; }
	public double getArrowSize() { return arrow_size; }
	public void setArrowHead(int type) {
		switch (type) {
		case AH_OUTLINE:
		case AH_OPEN:
			arrow_type = type;
		default:
			arrow_type = AH_FILLED;
		}
	}
	public void setArrowSize(double size)	{
		arrow_size =(size>0.0? size: 1.0);
	}

	public void draw(Graphics g)	{
		Arrow.draw(this,g,arrow_size,arrow_type, 5, 2, Color.BLACK);
	}
	public static void draw(Line2 line, Graphics g, double size, int style)	{
		draw(line, g, size, style, 5, 1, Color.BLACK);
	}
	
	public static void draw(Line2 line, Graphics g, double size, int style, double arrowAngle, int lineSize, Color color)	{
		final double fa = arrowAngle; // tan of arrow angle
		final double fb = 0.9; // break in base Arrow
		int x[] = new int[4];
		int y[] = new int[4];
		
		Graphics2D g2 = (Graphics2D)g;
		g2.setColor(color);
		g2.setStroke(new BasicStroke(lineSize));
		Vec2 va, vf;
		va = line.getDirection();
		va.setSize(1.0);
		vf = new Vec2(va);
		vf.setSize(1.0/fa);
		Vec2 pa = line.pt[0];
		Vec2 pb = line.pt[1];

		Vec2 pc = new Vec2((vf.v[1]-va.v[0]),-(vf.v[0]+va.v[1]));
		Vec2 pd = new Vec2(-va.v[0],-va.v[1]);
		Vec2 pe = new Vec2(-(vf.v[1]+va.v[0]),(vf.v[0]-va.v[1]));
		size *= std_size;
		pc.scale(size).translate(pb);
		pd.scale(size*fb).translate(pb);
		pe.scale(size).translate(pb);

		int [] iv;
		iv = pc.convert();
		x[0] = iv[0]; y[0] = iv[1];
		iv = pd.convert();
		x[1] = iv[0]; y[1] = iv[1];
		iv = pe.convert();
		x[2] = iv[0]; y[2] = iv[1];
		iv = pb.convert();
		x[3] = iv[0]; y[3] = iv[1];
		iv = pa.convert();
		g2.drawLine(iv[0],iv[1],x[3],y[3]);
		switch (style) {
		case AH_OUTLINE:
			g2.drawPolygon(x,y,4);
			break;
		case AH_OPEN:
			g2.drawLine(x[0],y[0],x[3],y[3]);
			g2.drawLine(x[2],y[2],x[3],y[3]);
			break;
		case AH_FILLED:
		default:
			g2.fillPolygon(x,y,4);
			break;
		}
	}
}	
