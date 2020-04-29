package com.geohammer.core.planarmodel;

import com.geohammer.rt.pseudo3d.Almost;

public class SingleLine {

	private Point2D _p1 = new Point2D(0, 0);
	private Point2D _p2 = new Point2D(1, 0);
	
	private double 			_EPSILON 		= 0.01; // tolerance to determine if a point is in a line
	private double 			_incidentAngle	= 1.0e-4;
	private double 			_halfPi			= 2.0*Math.atan(1.0);

	public SingleLine() {
		this(0,0,1,0);
    }
	public SingleLine(Point2D p1, Point2D p2) {
		setLine(p1, p2);
    }
	public SingleLine(double x1, double z1, double x2, double z2) {
		this(x1, 0, z1, x2, 0, z2);
	}
	public SingleLine(double x1, double y1, double z1, double x2, double y2, double z2) {
		setLine(x1, y1, z1, x2, y2, z2);
	}
	public void convertUnitSystem(double scalor) {
		_p1.convertUnitSystem(scalor);
		_p2.convertUnitSystem(scalor);
	}
	public Point2D getP1() 			{ return _p1; }
	public Point2D getP2() 			{ return _p2; }
	public void setP1(Point2D p1) 	{ _p1 = p1; }
	public void setP2(Point2D p2) 	{ _p2 = p2; }
	
	public void setLine(double x1, double z1, double x2, double z2) {
		setLine(x1, 0, z1, x2, 0, z2);
    }
	public void setLine(double x1, double y1, double z1, double x2, double y2, double z2) {
		_p1.setLocation(x1, y1, z1);
		_p2.setLocation(x2, y2, z2);
    }
	public void setLine(Point2D p1, Point2D p2) {
		_p1.setLocation(p1.getE(), p1.getN(), p1.getD());
		_p2.setLocation(p2.getE(), p2.getN(), p2.getD());
    }
	
	public SingleLine copy() 	{ return new SingleLine(_p1, _p2); }
	public double length() 		{ return _p1.distance(_p2); }
	
	public boolean on(Point2D p) 				{ return on(p.getE(), p.getD()); }
	public boolean above(Point2D p) 			{ return above(p.getE(), p.getD()); }
	public boolean below(Point2D p) 			{ return below(p.getE(), p.getD()); }
	public boolean left(Point2D p) 				{ return left(p.getE(), p.getD()); }
	public boolean right(Point2D p) 			{ return right(p.getE(), p.getD()); }
	
	public boolean on(double x, double z) 		{ 
		if(isHorizontal()) return Almost.DOUBLE.cmp(_p1.getD(),z)==0;
		else if(isVertical()) return Almost.DOUBLE.cmp(_p1.getE(),x)==0;
		return Almost.DOUBLE.cmp(getPointZ(x),z)==0; 
	}
	public boolean above(double x, double z) 	{ 
		if(isHorizontal()) return Almost.DOUBLE.cmp(_p1.getD(),z)==1;
		else if(isVertical()) return Almost.DOUBLE.cmp(_p1.getD(),z)==1;
		else return Almost.DOUBLE.cmp(getPointZ(x),z)==1; 
	}
	public boolean below(double x, double z) 	{ 
		if(isHorizontal()) return Almost.DOUBLE.cmp(_p1.getD(),z)==-1;
		else if(isVertical()) return Almost.DOUBLE.cmp(_p2.getD(),z)==-1;
		else return Almost.DOUBLE.cmp(getPointZ(x),z)==-1; 
	}
	public boolean left(double x, double z) 	{ 
		if(isHorizontal()) return Almost.DOUBLE.cmp(_p1.getE(),x)==1;
		else if(isVertical()) return Almost.DOUBLE.cmp(_p1.getE(),x)==1;
		else return Almost.DOUBLE.cmp(getPointX(z),x)==1; 
	}
	public boolean right(double x, double z) 	{ 
		if(isHorizontal()) return Almost.DOUBLE.cmp(_p2.getE(),x)==-1;
		else if(isVertical()) return Almost.DOUBLE.cmp(_p1.getE(),x)==-1;
		else return Almost.DOUBLE.cmp(getPointX(z),x)==-1; 
	}
	public boolean inside(Point2D p) 			{ return inside(false, false, p); }
	public boolean insideIncludeBoth(Point2D p) { return inside(true, true, p); }
	public boolean insideIncludeLeft(Point2D p) { return inside(true, false, p); }
	public boolean insideIncludeRight(Point2D p){ return inside(false, true, p); }
	
	private boolean inside(boolean includeLeft, boolean includeRight, Point2D p) { 
		if(Almost.DOUBLE.cmp(_p1.getE(),p.getE())==-1 && 
			Almost.DOUBLE.cmp(_p2.getE(),p.getE())==1) return true; 
		else if(includeLeft) return onLeftEdge(p); 
		else if(includeRight) return onRightEdge(p);
		else return false;
	}
	public boolean onLeftEdge(Point2D p) { 
		if(Almost.DOUBLE.cmp(_p1.getE(),p.getE())==0) return true; 
		else return false;
	}
	public boolean onRightEdge(Point2D p) { 
		if(Almost.DOUBLE.cmp(_p2.getE(),p.getE())==0) return true; 
		else return false;
	}
	public boolean outLeftEdge(Point2D p) { 
		if(Almost.DOUBLE.cmp(_p1.getE(),p.getE())==1) return true; 
		else return false;
	}
	public boolean outRightEdge(Point2D p) { 
		if(Almost.DOUBLE.cmp(_p2.getE(),p.getE())==-1) return true; 
		else return false;
	}
	
	public double getPointZ(double x) {
		double slope = getSlope();
		if(slope==0.0) return _p1.getD(); //horizontal
		else if(slope==Almost._INVALID_D)  return Almost._INVALID_D; //vertical
		else { return _p1.getD()+slope*(x-_p1.getE()); }
	}
	public double getPointX(double z) {
		if(isHorizontal()) return _p2.getE(); //horizontal
		else if(isVertical()) return _p1.getE(); //vertical
		else { return _p1.getE()+(z-_p1.getD())/getSlope(); }
	}
	
	public double getDerivative() 	{ return getSlope(); }
	public double getSlope() 		{
		if(isHorizontal()) return 0.0; //horizontal
		if(isVertical()) return Almost._INVALID_D; //vertical
		return (_p2.getD()-_p1.getD())/(_p2.getE()-_p1.getE());
	}
	public double getTakeOffAngle() 		{
		if(isHorizontal()) return _halfPi; //horizontal
		if(isVertical()) return 0.0; //vertical
		return Math.atan((_p2.getE()-_p1.getE())/(_p2.getD()-_p1.getD()));
	}

	public boolean isHorizontal() 	{ return Almost.DOUBLE.cmp(_p1.getD(),_p2.getD())==0; }
	public boolean isVertical() 	{ return Almost.DOUBLE.cmp(_p1.getE(),_p2.getE())==0; }
	
	public void shift(double dx, double dz) 	{ _p1.shift(dx, 0, dz);  _p2.shift(dx, 0, dz); }

	public String toString() { return _p1.toString()+", "+_p2.toString(); }
	
//	public SingleLine extend(double top, double bot, double left, double right) {
//		if(isHorizontal()) return new SingleLine(left, _p1.getD(), right, _p2.getD());
//		else if(isVertical()) return new SingleLine(_p1.getE(), top, _p2.getE(), bot);
//		else {
//			double ratio = (bot-top)/(right-left);
//			
//			if((_p2.getE()-_p1.getE())>(_p2.getD()-_p1.getD()))
//			
//			return new SingleLine(left, getPointZ(left), right, getPointZ(right));
//			return new SingleLine(getPointX(top), top, getPointX(bot), bot);
//		}
//	}
	
	public double getIncidentAngle() { return _incidentAngle; }
	public Point2D intersection(SingleLine ray) {
		double Ax, Az, Bx, Bz, Cx, Cz, Dx, Dz;
		double distAB, theCos, theSin;
		double Ex, Ez, newX;
		
		//System.out.println(String.format("p1=%s p2=%s p3=%s p4=%s", p1.toString(), p2.toString(), p3.toString(), p4.toString()));
		//p3 and p4 are almost the same point
		if(ray.length()<_EPSILON) return ray.getP1();
				
		//zero thickness
		if(on(ray.getP1())) return ray.getP1();
		if(on(ray.getP2())) return ray.getP2();		
		
		//if(L.isHorizontal()) return new Point2D(getPointX(L.getP1().getZ()), L.getP1().getZ());
		//if(L.isVertical()) return new Point2D(L.getP1().getX(), getPointZ(L.getP1().getX()));
		
		Ax = _p1.getE();	Az = _p1.getD();
		Bx = _p2.getE();	Bz = _p2.getD();
		Cx = ray.getP1().getE();	Cz = ray.getP1().getD();
		Dx = ray.getP2().getE();	Dz = ray.getP2().getD();
		
		//  (1) Translate the system so that point A is on the origin.
		Bx-=Ax; Bz-=Az;
		Cx-=Ax; Cz-=Az;
		Dx-=Ax; Dz-=Az;

		//  Discover the length of segment A-B.
		distAB=Math.sqrt(Bx*Bx+Bz*Bz);

		//  (2) Rotate the system so that point B is on the positive X axis.
		theCos=Bx/distAB;
		theSin=Bz/distAB;
		newX=Cx*theCos+Cz*theSin;
		Cz  =-Cx*theSin+Cz*theCos; Cx=newX;
		newX=Dx*theCos+Dz*theSin;
		Dz  =-Dx*theSin+Dz*theCos; Dx=newX;

		//  (3) Discover the position of the intersection point along line A-B.
		Ex = 0.0;
		Ez = 0.0;
		_incidentAngle = 0.0;
		
		//System.out.println(String.format("Ax=%f Az=%f Bx=%f Bz=%f ", Ax, Az, Bx, Bz));
		//System.out.println(String.format("Cx=%f Cz=%f Dx=%f Dz=%f ", Cx, Cz, Dx, Dz));
		
		//clip if the line CD are perpendicular to axis.
		if (Almost.DOUBLE.cmp(Cx,Dx)==0) 			{ Ex = Cx; _incidentAngle = _halfPi;}
		//clip if the line CD are parallel to axis.
		else if (Almost.DOUBLE.cmp(Cz,Dz)==0)  		{Ez = Cz; if(Cx<Dx) Ex = distAB; }
		else {
			Ez = 0.0;
			Ex = Cx+(Dx-Cx)*(Ez-Cz)/(Dz-Cz);
			if(Ex<=_EPSILON || Ex>distAB-_EPSILON ) {
				if(Dx>Cx) { // right edge 
					return new Point2D(_p2.getE(), ray.getPointZ(_p2.getE()));
				} else { // left edge
					return new Point2D(_p1.getE(), ray.getPointZ(_p1.getE()));
				}
			} 
			double distCD=Math.sqrt((Cx-Dx)*(Cx-Dx)+(Cz-Dz)*(Cz-Dz));	
			_incidentAngle = Math.asin(Math.abs(Cx-Dx)/distCD);
		}
		
//		System.out.println(String.format("ex1=%f ez=%f distAB=%f", Ex, Ez, distAB));
		
		//System.out.println(String.format("p3=%s p4=%s incidentAngle=%f", p3.toString(), p4.toString(), _incidentAngle));
		//  (4) Apply the discovered position to line A-B in the original coordinate system.
		//reverse rotation
		newX = Ex*theCos - Ez*theSin;
		Ez = Ex*theSin + Ez*theCos; Ex = newX;

//		if(Math.abs(Ex)<_EPSILON) Ex = 0.0;
//		if(Math.abs(Ez)<_EPSILON) Ez = 0.0;
//		System.out.println(String.format("ex2=%f ez=%f distAB=%f", Ex, Ez, distAB));
		return new Point2D(Ax+Ex, Az+Ez);
	}

}
