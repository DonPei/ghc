package com.geohammer.resource;

import java.awt.Graphics;
import java.awt.Graphics2D;

public class Line2 {
	public Vec2 [] pt;
	public Line2() { this(0.0,0.0,0.0,0.0);  } //!< default no-argument constructor
	public Line2(Vec2 to) { this(0.0,0.0,to.v[0],to.v[1]); }
	//! construct Line2 from origin to (x,y)
	public Line2(double x, double y) { this(0.0,0.0,x,y); }
	//! constructs Line2 from (x1,y1) to (x2,y2)
	public Line2(double x1, double y1, double x2, double y2) {
		pt = new Vec2[2];
		pt[0] = new Vec2(x1,y1);
		pt[1] = new Vec2(x2,y2);
	}
	//! constructs Line2 from two points
	public Line2(Vec2 from, Vec2 to){
		pt = new Vec2[2];
		pt[0] = new Vec2(from);
		pt[1] = new Vec2(to);
	}
	//! returns length of associated array
	public int rows()  { return 2; }
	public int cols()  { return 2; }
	//! return pointer to first element
	public Line2 set(double x1, double y1, double x2, double y2)	{
		pt[0].v[0]=x1; pt[0].v[1]=y1;
		pt[1].v[0]=x2; pt[1].v[1]=y2;
		return this;
	}
	public Line2 from(double x, double y)	{
		pt[0].v[0]=x; pt[0].v[1]=y;
		return this;
	}
	public Line2 from(Vec2 src)	{
		pt[0]=src;
		return this;
	}
	public Line2 to(double x, double y)	{
		pt[1].v[0]=x; pt[1].v[1]=y;
		return this;
	}

	public Line2 to(Vec2 src) {
		pt[1]=src;
		return this;
	}

	public Line2 away(double r, double theta)	{
		Vec2 angl = new Vec2(theta);
		pt[1] = Vec2.sum(pt[0],angl.scale(r));
		return this;
	}

	public Line2 translate(double tx, double ty)	{
		pt[0].translate(tx,ty); pt[1].translate(tx,ty);
		return this;
	}

	public Line2 translate(Vec2 b)	{
		pt[0].translate(b); pt[1].translate(b);
		return this;
	}

	public Vec2 getDirection()	{
		return Vec2.diff(pt[1],pt[0]);
	}

	public void draw(Graphics g)	{
		int [] from = pt[0].convert();
		int [] to = pt[1].convert();
		g.drawLine(from[0],from[1],to[0],to[1]);
	}
	
	public void draw(Graphics2D g)	{
		int [] from = pt[0].convert();
		int [] to = pt[1].convert();
		g.drawLine(from[0],from[1],to[0],to[1]);
	}

	public String toString(){
		return (pt[0] + ":" + pt[1]);
	}

	//! return distance between two endpoints
	public double length()	{
		return Vec2.distance(pt[0],pt[1]);
	}

	//! returns angle (in degrees) between two lines
	public static double angle(Line2 a, Line2 b)	{
		Vec2 va = Vec2.diff(a.pt[1],a.pt[0]);
		Vec2 vb = Vec2.diff(b.pt[1],b.pt[0]);
		return Vec2.angle(va,vb);
	}

	public Vec2 lerp(double u)	{
		double up = 1.0-u;
		double x = up*pt[0].v[0]+u*pt[1].v[0];
		double y = up*pt[0].v[1]+u*pt[1].v[1];
		return new Vec2(x,y);
	}

	public double getClosestPosition( Vec2 v)	{
		Vec2 p = Vec2.diff(v,pt[0]);
		Vec2 k = getDirection();
		return Vec2.dot(p,k)/Vec2.dot(k,k);
	}
}

