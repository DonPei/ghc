package com.geohammer.core.planarmodel;

import java.io.IOException;

import com.geohammer.rt.pseudo3d.Geometry2D;

public class Curve {
	private int _id				= 0;
	
	private int _idx			= 1;
	private int _idz			= 1;
	private float []  	_dzdxs			= null;		//first derivative
	private float [] 	_gridX 			= null;
	private float [] 	_gridY 			= null;
	
	public Curve(float [] gridX) {
		this(gridX.length, 1, 1);
		_gridX = gridX;
	}
	public Curve(int n, int idx, int idz) {
		_gridY = new float[n];
		_dzdxs = new float[n];
		_idx = idx;
		_idz = idz;
	}
	public void updateCurve(Point2D p3, Point2D p4) {
		for(int i=0; i<getNumOfSamples(); i++) {
			double y = getOnlinePointY(p3, p4, (double)getAxisValue(i));
			setValue(i, (float)y);
		}
	}
	public double getOnlinePointY(Point2D p1, Point2D p2, double x) {
		double fraction = (x-p1.getE())/(p2.getE() - p1.getE());
		return p1.getN() + fraction*(p2.getN() - p1.getN());
	}
	
	public double getYAlongDerivative(double x, double endX) {
		double y = getValue((float)x);
		double dzdx = getDerivativeArray((float)x);
		//System.out.println(String.format("x=%f y=%f deriv=%f", x, y, dzdx));
		return dzdx*(endX-x)+y;
	}

	//derivative
//	public double getDerivative() {
//		return _dzdx;
//	}
	public float [] getDerivativeArray() {
		return _dzdxs;
	}

	public float getDerivativeArray(int k) {
		return _dzdxs[k];
	}
	public float getDerivativeArray(float x) {
		int k = getAxisIndex(x);
		if(k==_gridX.length-1) return _dzdxs[k];
		float x1 = _gridX[k];
		float x2 = _gridX[k+1];
		float y1 = _dzdxs[k];
		float y2 = _dzdxs[k+1];
		return (float)interpolation(x, x1, x2, y1, y2);
	}
//	public void setDerivative(double dzdx) {
//		_dzdx = dzdx;
//	}
	public void setDerivativeArray(float [] dzdxs) {
		_dzdxs = dzdxs;
	}
	public void setDerivativeArray(float v, int k) {
		_dzdxs[k] = v;
	}
	
	//direction
	public int getiDx() {
		return _idx;
	}
	public int getiDz() {
		return _idz;
	}
	public void setDirection(int idx, int idz) {
		_idx = idx;
		_idz = idz;
	}
	
	//axis
	public int getNumOfSamples() {
		return _gridX.length;
	}
	public float getAxisValue(int k) {
		return _gridX[k];
	}
	public float [] getAxisArray() {
		return _gridX;
	}
	public void setAxisArray(float [] gridX) {
		_gridX = gridX;
	}
	public int getAxisIndex(double x) {
		int k = 0;
		if(x<=_gridX[0]) return 0;
		else if(x>_gridX[_gridX.length-1]) return _gridX.length-1; 
		else {
			for(int i=1; i<_gridX.length; i++) {
				if(x>=_gridX[i-1] && x<=_gridX[i]) {
					return i-1;
				}
			}
		}
		return k;
	}
	
	public void setId(int id) {
		_id = id;
	}
	public int getId() {
		return _id;
	}
	
	//value
	public float getValue(int k) {
		return _gridY[k];
	}
	public float getValue(float x) {
		int k = getAxisIndex(x);
		if(k==_gridX.length-1) return _gridY[k];
		float x1 = _gridX[k];
		float x2 = _gridX[k+1];
		float y1 = _gridY[k];
		float y2 = _gridY[k+1];
		return (float)interpolation(x, x1, x2, y1, y2);
	}
	public float [] getValueArray() {
		return _gridY;
	}
	public void setValueArray(float [] gridY) {
		_gridY = gridY;
	}
	public void setValue(float [] gridY) {
		_gridY = gridY;
	}
	public void setValue(int k, float v) {
		_gridY[k] = v;
	}
	public void floodValue(float v) {
		for(int i=0; i<getNumOfSamples(); i++) {
			_gridY[i] = v;
		}
	}
	
	private double interpolation(double x, double x1, double x2, double y1, double y2){
		double fraction = (x-x1)/(x2-x1);
		return (y1+(y2-y1)*fraction);
	}
	
//	private float getDifference(int k, Curve ray){
//		return getValue(k) - ray.getValue(k);
//	}
//	public int intersectExist(Curve ray) {
//		int k = 0;
//		for(int i=1; i<getNumOfSamples(); i++) {
//			if(getValue(i)!=TraceLayerModelField._INVALID_F) {
//				k = i;
//				break;
//			}
//		}
//		double diff = getDifference(k, ray);
//		for(int i=k; i<getNumOfSamples(); i++) {
//			if(diff*(getDifference(i, ray))<=0.0) {
//				return i-1;
//			}
//		}
//		return -1;
//	}	
	
//	public int intersectExist(Point2D p1, Point2D p2) {
//		int n = getNumOfSamples();
//		
//		int k = 0;
//		int k1 = 0;
//		int k2 = n-1;
//		int resultID = intersect(k1, k2, p1, p2);
//		if(resultID==-1||resultID==k1||resultID==k2) return resultID;
//		
//		//1-order subdivide
//		k1 = 0;
//		k2 = n/2-1;
//		resultID = intersect(k1, k2, p1, p2);
//		if(resultID==k1||resultID==k2) return resultID;
//		else if(resultID==-2) 	k = k1;
//		else 					k = k2;		
//	}
	
	public int intersectExist(Point2D p1, Point2D p2) {
		int k1 = 0;
		int k2 = getNumOfSamples()-1;
		int resultID = intersect(k1, k2, p1, p2);
		if(resultID==-1) return resultID;
		else if(resultID==k1||resultID==k2) return 100000+resultID;
		else { }
		
		int k = getNumOfSamples()/2-1;
		
		// 1-order divide
		k1 = 0;
		k2 = k;
		resultID = intersect(k1, k2, p1, p2);
		if(resultID==k1||resultID==k2) return 100000+resultID;
		if(resultID==-1) {
			for(int i=k+1; i<getNumOfSamples(); i++) {
				k1 = i-1;
				k2 = i;
				resultID = intersect(k1, k2, p1, p2);
				if(resultID==k1||resultID==k2) return 100000+resultID;
				else if(resultID==-2) return k1;
				else { }
			}
		}else { 
			for(int i=1; i<=k; i++) {
				k1 = i-1;
				k2 = i;
				resultID = intersect(k1, k2, p1, p2);
				if(resultID==k1||resultID==k2) return 100000+resultID;
				else if(resultID==-2) return k1;
				else { }
			}
		}
			
		return -1;
	}
	
	public int intersect(int k1, int k2, Point2D p1, Point2D p2) {
		int isLeft = Geometry2D.pointLinePosition(p2.getE(), p2.getN(), p1.getE(), p1.getN(), getAxisValue(k1), getValue(k1));
		int isRight = Geometry2D.pointLinePosition(p2.getE(), p2.getN(), p1.getE(), p1.getN(), getAxisValue(k2), getValue(k2));
		
		if(isLeft==0) 				return k1;
		else if(isRight==0) 		return k2;
		else if(isLeft*isRight<0) 	return -2;
		else 						return -1;
	}
	
	public boolean pause1() {
		System.out.println("Press ENTER key to continue...");
		int i = 0; 
		char c; 
		try {
			while((c = (char) System.in.read()) != 'S') {
				if((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z')) {
					i++;
					System.out.println("Pass # " + i + " c="+c);
				}
			}
		}
		catch (IOException e){
			System.out.println("Error reading from user");
		}
		return false;
	}
	
	public String toString()	{
		String a;
		String b = new String(" ");
		for(int i=0; i<getNumOfSamples(); i++) {
			a = String.format( "%f %f %f\n", _gridX[i], _gridY[i], _dzdxs[i]);
			b = new String(b.concat(a));
		}
		
		return b;
	}
}
