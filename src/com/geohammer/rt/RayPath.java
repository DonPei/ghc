package com.geohammer.rt;

import java.util.Arrays;

import com.geohammer.rt.pseudo3d.Almost;

public class RayPath {
	private int _nPoints = 0;
	
	private double [] _n = null;		
	private double [] _e = null;		
	private double [] _d = null;		
	
	private double _INVALID_D = -99999.0;
	
	public RayPath() { }
	
	public RayPath(int nPoints) {
		_nPoints 		= nPoints;
		allocate(nPoints);
	}
	public RayPath(int nPoints, double [] e, double [] n, double [] d) {
		_nPoints= nPoints;
		_n 		= e;
		_e 		= n;
		_d 		= d;
	}
	public RayPath(double [] e, double [] n, double [] d) {
		int nPoints = 0; 
		for(int i=0; i<_e.length; i++) {
			if(e[i] != _INVALID_D) nPoints++;
		}
		_nPoints 		= nPoints;
		allocate(nPoints);
		
		for(int i=0, j=0; i<_e.length; i++) {
			if(e[i] != _INVALID_D) {
				_e[j] = e[i];
				_n[j] = n[i];
				_d[j] = d[i];
				j++;
			}
		}
	}
	
	public void allocate(int nPoints) {
		_n 		= new double[nPoints];
		_e 		= new double[nPoints];
		_d 		= new double[nPoints];
		for(int i=0; i<nPoints; i++) { _e[i] = 0; _n[i] = 0; _d[i] = 0; }
	}

	public int getNumOfPoints() 		{ return _nPoints; }
	public double [] getE() 			{ return _e; }
	public double [] getN() 			{ return _n; }
	public double [] getD() 			{ return _d; }
	
	public double getE(int index) 		{ return _e[index]; }
	public double getN(int index) 		{ return _n[index]; }
	public double getD(int index) 		{ return _d[index]; }
	
	public void setNumOfPoints(int nPoints) 	{ _nPoints = nPoints; }
	public void setE(double [] rE) 				{ _e = rE; }
	public void setN(double [] rN) 				{ _n = rN; }
	public void setD(double [] rD) 				{ _d = rD; }
	
	public void setE(int index, double v) 		{ _e[index] = v; }
	public void setN(int index, double v) 		{ _n[index] = v; }
	public void setD(int index, double v) 		{ _d[index] = v; }
	
	public void setPoint(int index, double e, double n, double d) 	{ _e[index] = e; _n[index] = n; _d[index] = d;}
	public void setPoint(int index, double e, double d) 			{ _e[index] = e; _n[index] = 0.0; _d[index] = d;}
	
	public boolean containEmsemble(RayPath other) 	{ return equal(0, new double[]{other.getE(0), other.getN(0), other.getD(0)}); }
	public boolean equalFirstPoint(double [] p) 	{ return equal(0, p); }
	public boolean equalLastPoint(double [] p) 		{ return equal(_nPoints-1, p); }
	public boolean equal(int index, double [] p) 	{
		return Almost.DOUBLE.cmp(getE(index),p[0])==0 && Almost.DOUBLE.cmp(getN(index),p[1])==0 && Almost.DOUBLE.cmp(getD(index),p[2])==0; 
	}
	
	public RayPath copy() {
		if(_nPoints==0) return null;
		RayPath other = new RayPath(_nPoints);
		for(int i=0; i<_nPoints; i++) {
			other._e[i] = _e[i];
			other._n[i] = _n[i];
			other._d[i] = _d[i];
		}
		return other;
	}
	
	public RayPath insert(double []d) {
		if(d==null) return null;
		int k = 0;
		for(int i=0; i<_d.length; i++) {
			if(Almost.DOUBLE.cmp(d[1],_d[i])==0) { k=i; break;}
		}
		//System.out.println("k="+k+" "+Arrays.toString(d));
		
		RayPath other = new RayPath(_nPoints+1);
		double dx = 0.0;
		for(int i=0, j=0; i<_nPoints; i++) {
			other._e[j] = _e[i]+dx;
			other._n[j] = _n[i];
			other._d[j] = _d[i];
			j++;
			if(j-1==k) {
				dx = d[0];
				other._e[j] = _e[i]+dx;
				other._n[j] = _n[i];
				other._d[j] = _d[i];
				j++;
			}
		}
		return other;
	}
	
	public void to3D(double sx, double sy, double sz, double rx, double ry, double rz) {
		double surfaceDist = Math.sqrt((sx-rx)*(sx-rx)+(sy-ry)*(sy-ry));
		double theSin = (ry-sy)/surfaceDist;
		double theCos = (rx-sx)/surfaceDist;
		for(int i=0; i<_nPoints; i++) {
			double d = _e[i];
			_e[i] = d*theSin;
			_n[i] = d*theCos;
		}
	}
	
	public void transfer(double x0, double y0) {
		for(int i=0; i<_nPoints; i++) {
			_e[i] += x0;
			_n[i] += y0;
		}
	}
	public void rotate(double azimuth) {
		double theSin = Math.sin(azimuth);
		double theCos = Math.cos(azimuth);
		rotate(theSin, theCos);
	}
	public void rotate(double theSin, double theCos) {
		for(int i=0; i<_nPoints; i++) {
			_n[i] = _e[i]*theCos;
			_e[i] = _e[i]*theSin;
		}
	}
	public void rotate(double sx, double sy, double rx, double ry) {
		if(sx==rx && sy==ry) return;
		double surfaceDistance = Math.sqrt((sx-rx)*(sx-rx)+(sy-ry)*(sy-ry));
		double theSin = 0.0;  //azimuth
		double theCos = 0.0;

		theSin = (rx-sx) / surfaceDistance;
		theCos = (ry-sy) / surfaceDistance;
		rotate(theSin, theCos);
	}
	public void reverse() {
		RayPath other = copy();
		for(int i=0, j=_nPoints-1; i<_nPoints; i++) {
			_e[i] = other._e[j];
			_n[i] = other._n[j];
			_d[i] = other._d[j];
			j--;
		}
	}
	
	public double [][] getAzimuthInclination() {
		if(_nPoints==0) return null;
		double [] azimuth = new double[_nPoints-1];
		double [] inclination = new double[_nPoints-1];
		double diffX = 0.0;
		double diffY = 0.0;
		double diffZ = 0.0;
		double d = 0.0;
		for(int i=0; i<_nPoints-1; i++) {
			diffX = _e[i+1]-_e[i];
			diffY = _n[i+1]-_n[i];
			diffZ = _d[i+1]-_d[i];
			d = Math.sqrt(diffX*diffX+diffY*diffY+diffZ*diffZ);
			azimuth[i] = Math.atan2(diffY, diffX);
			inclination[i] = Math.acos(diffZ/d);
		}
		
		return new double[][]{azimuth, inclination};
	}
	public double [] getRayLength() {
		if(_nPoints==0) return null;
		double [] len = new double[_nPoints-1];
		double diffX = 0.0;
		double diffY = 0.0;
		double diffZ = 0.0;
		double d = 0.0;
		for(int i=0; i<_nPoints-1; i++) {
			diffX = _e[i+1]-_e[i];
			diffY = _n[i+1]-_n[i];
			diffZ = _d[i+1]-_d[i];
			len[i] = Math.sqrt(diffX*diffX+diffY*diffY+diffZ*diffZ);
		}
		
		return len;
	}
	
	public String toString() {
		String b =_nPoints+"\n";
		String a = null;
		for(int i=0; i<_nPoints; i++) {
			a =_n[i]+ " "+_e[i]+ " "+_d[i]; 
			b = b.concat(a+"\n");
		}

		return b;
	}
	
	public double calEmergentAngle() {
		double dx = _n[_nPoints-1] - _n[_nPoints-2];
		double dz = _d[_nPoints-1] - _d[_nPoints-2];
		return Math.atan2(Math.abs(dx), Math.abs(dz));
	}
}
