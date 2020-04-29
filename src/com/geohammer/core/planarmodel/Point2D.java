package com.geohammer.core.planarmodel;

import com.geohammer.rt.pseudo3d.Almost;

public class Point2D extends Point3D{

	public Point2D(double e, double d) {
		this(e, 0, d);
	}
	public Point2D(double e, double n, double d) {
		super(e, n, d);
	}
	@Override
	public Point2D copy() 	{ return new Point2D(getE(), getN(), getD()); }
	public void convertUnitSystem(double scalor) {
		super.convertUnitSystem(scalor);
	}
	public double distance(Point2D other) {
		return Math.sqrt((getE()-other.getE())*(getE()-other.getE())+
				(getD()-other.getD())*(getD()-other.getD()));
	}
	
	public boolean equal(Point2D other) {
		return Almost.DOUBLE.cmp(getE(),other.getE())==0 && Almost.DOUBLE.cmp(getD(),other.getD())==0; 
	}
}
