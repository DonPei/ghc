package com.geohammer.core.planarmodel;

import com.geohammer.rt.pseudo3d.Almost;

public class Point3D {
	private double _e = 0.0;
	private double _n = 0.0;
	private double _d = 0.0;

	public Point3D(double e, double n, double d) {
		_e = e;
		_n = n;
		_d = d;
	}

	public double getE() { return _e; }
	public double getN() { return _n; }
	public double getD() { return _d; }
	
	public void setE(double e) { _e = e; }
	public void setN(double n) { _n = n; }
	public void setD(double d) { _d = d; }
	
	
	// assuming x right y into paper z up system
	public double getX() { return _e; }
	public double getY() { return _n; }
	public double getZ() { return _d; }
	
	public void setX(double x) { _e = x; }
	public void setY(double y) { _n = y; }
	public void setZ(double z) { _d = z; }
	
	
	public Point3D copy() 		{ return new Point3D(_e, _n, _d); }
	public void convertUnitSystem(double scalor) {
		_e *= scalor;
		_n *= scalor;
		_d *= scalor;
	}
	
	public double length2() { return _e*_e+_n*_n+_d*_d; }
	public double length() { return Math.sqrt(length2()); }
	public double distance(Point3D other) {
		return Math.sqrt((_e-other.getE())*(_e-other.getE())+
				(_n-other.getN())*(_n-other.getN())+(_d-other.getD())*(_d-other.getD()));
	}
	public double surfaceDistance(Point3D other) {
		return Math.sqrt((_e-other.getE())*(_e-other.getE())+
				(_n-other.getN())*(_n-other.getN()));
	}
	public void setLocation(Point3D other) {
		_e = other.getE();
		_n = other.getN();
		_d = other.getD();
	}
	public void setLocation(double x, double y, double z) {
		_e = x;
		_n = y;
		_d = z;
	}
	public void shift(double dx, double dy, double dz) {
		_e += dx;
		_n += dy;
		_d += dz;
	}
	
	public double dot(Point3D other) {
		return _e*other.getE()+_n*other.getN()+_d*other.getD();
	}
	public Point3D cross(Point3D other) {
		return new Point3D(_n*other.getD()-_d*other.getN(), 
				-(_e*other.getD()-_d*other.getE()), 
				_e*other.getN()-_n*other.getE());
	}
	
	public boolean isZero() { return _e==0&&_n==0&&_d==0; }
	public boolean isVertical() { return _e==0&&_n==0; }
	public String toString() {
		return toString(4);
	}
	public String toString(int id) {
		if(id==1) return _e+" ";
		else if(id==2) return _n+" ";
		else if(id==3) return _d+" ";
		else return _e+" "+_n+" "+_d+" ";
	}
	public void multiply(double [][] R) {
		double x1 = (R[0][0]*_e+R[0][1]*_n+R[0][2]*_d);
		double y1 = (R[1][0]*_e+R[1][1]*_n+R[1][2]*_d);
		double z1 = (R[2][0]*_e+R[2][1]*_n+R[2][2]*_d);
		
		_e = x1;
		_n = y1;
		_d = z1;
	}
	
	public boolean equal(Point3D other) {
		if (!(other instanceof Point3D)) {
	           return false;
	       }
		return Almost.DOUBLE.cmp(getE(),other.getE())==0 && 
			Almost.DOUBLE.cmp(getN(),other.getN())==0 &&
			Almost.DOUBLE.cmp(getD(),other.getD())==0; 
	}
   
   public int hashCode() {
       long x = Double.doubleToLongBits(_e);
       long y = Double.doubleToLongBits(_n);
       long z = Double.doubleToLongBits(_d);
       int hashCode = (int)z;
       hashCode = 31 * hashCode + (int)y;
       hashCode = 31 * hashCode + (int)x;
       return hashCode;
   }
 
}
