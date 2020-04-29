package com.geohammer.core.planarmodel;

import java.util.ArrayList;

import edu.mines.jtk.interp.CubicInterpolator;
import edu.mines.jtk.util.ArrayMath;

public class SingleCurve {
	public SingleLine 				_diagonal 	= null;
	public SingleLine 				_chord 		= null;
	public ArrayList<SingleLine> 	_lines 		= null;
	
	public SingleCurve() {
		this(2);
	}
	public SingleCurve(int nx) {
		_lines = new ArrayList<SingleLine>(nx);
	}
	public SingleCurve(int nx, float [] x, float [] z, float [] zPrime) {
		_lines = new ArrayList<SingleLine>(nx);
		if(x!=null&&z!=null) {
			if(zPrime==null) zPrime = z;
			for(int i=0; i<nx; i++) {
				_lines.add( new SingleLine((double)x[i], (double)zPrime[i], (double)z[i], 
						(double)x[i], (double)zPrime[i], (double)z[i]) );
			}
			updateP2();
		}
	}
	
	public void genDiagonal() 			{ 
		double z = 0.0;
		double minZ = 1.0e10;
		for(int i=0; i<_lines.size(); i++) {
			z = _lines.get(i).getP1().getD();
			if(minZ>z) minZ = z;
			z = _lines.get(i).getP2().getD();
			if(minZ>z) minZ = z;
		}
		double maxZ = -1.0e10;
		for(int i=0; i<_lines.size(); i++) {
			z = _lines.get(i).getP1().getD();
			if(maxZ<z) maxZ = z;
			z = _lines.get(i).getP2().getD();
			if(maxZ<z) maxZ = z;
		}
		
		_diagonal = new SingleLine(_lines.get(0).getP1().getE(), minZ, 
				_lines.get(_lines.size()-1).getP2().getE(), maxZ); 
	}
	
	public double getX0() { return _lines.get(0).getP1().getE(); }
	public double getX1() { return _lines.get(_lines.size()-1).getP2().getE(); }
	public double getZ0() { return _lines.get(0).getP1().getD(); }
	public double getZ1() { return _lines.get(_lines.size()-1).getP2().getD(); }
	
	public void updateP2() 	{ 
		int N = _lines.size();
		for(int i=0; i<N-1; i++) {
			Point2D p1 = _lines.get(i+1).getP1();
			_lines.get(i).setP2(p1.copy());			
		}
		Point2D p1 = _lines.get(N-1).getP1();
		_lines.get(N-1).setP2(p1.copy());
	}
	//for graphic paint()
	public float [] getX() {
		float [] x = new float[_lines.size()];
		for(int i=0; i<x.length; i++) x[i] = (float)_lines.get(i).getP1().getE();
		return x;
	} 
	public float [] getZ() {
		float [] z = new float[_lines.size()];
		for(int i=0; i<z.length; i++) z[i] = (float)_lines.get(i).getP1().getD();
		return z;
	}
	public void genChord() 			{ 
		_chord = new SingleLine(_lines.get(0).getP1(), _lines.get(_lines.size()-1).getP2()); 
	}
	public SingleLine getDiagonal() 		{ return _diagonal; }
	public SingleLine getChord() 			{ return _chord; }
	public ArrayList<SingleLine> getLines() { return _lines; }
	public SingleLine getLine(int index) 	{ return _lines.get(index); }
	public int getNumOfLines() 				{ return _lines.size(); }
	
	public SingleCurve copy() {
		SingleCurve sc = new SingleCurve();
		sc._lines = new ArrayList<SingleLine>();

		for(int i=0; i<_lines.size(); i++) { sc._lines.add(_lines.get(i).copy()); }
		sc.genDiagonal();
		sc.genChord();
		return sc;
	}
	public void convertUnitSystem(double scalor) {
		_diagonal.convertUnitSystem(scalor);
		_chord.convertUnitSystem(scalor);
		for(int i=0; i<_lines.size(); i++) { _lines.get(i).convertUnitSystem(scalor); }
	}
	public int getLineIndex(Point2D p) {
		for(int i=0; i<_lines.size(); i++) { 
			if(_lines.get(i).insideIncludeLeft(p)) return i; 
		}
		return _lines.size()-1;
	}
	
	public boolean inside(Point2D p) 			{ return _chord.inside(p); }
	public boolean insideIncludeBoth(Point2D p) { return _chord.insideIncludeBoth(p); }
	public boolean insideIncludeLeft(Point2D p) { return _chord.insideIncludeLeft(p); }
	public boolean insideIncludeRight(Point2D p){ return _chord.insideIncludeRight(p); }
	public boolean onLeftEdge(Point2D p) 		{ return _chord.onLeftEdge(p); }
	public boolean onRightEdge(Point2D p) 		{ return _chord.onRightEdge(p); }
	public boolean outLeftEdge(Point2D p) 		{ return _chord.outLeftEdge(p); }
	public boolean outRightEdge(Point2D p) 		{ return _chord.outRightEdge(p);}
	
	public boolean on(Point2D p) 				{ return _lines.get(getLineIndex(p)).on(p); }
	public boolean above(Point2D p) 			{ return _lines.get(getLineIndex(p)).above(p); }
	public boolean below(Point2D p) 			{ return _lines.get(getLineIndex(p)).below(p); }
	
	public double getFirstZ() 					{ return _lines.get(0).getP1().getD(); }
	public double getLastZ() 					{ return _lines.get(_lines.size()-1).getP1().getD(); }
	
	public double getPointZ(double x) { 
		if(x==getX0()) return getZ0();
		if(x==getX1()) return getZ1();
		SingleLine sl = _lines.get(getLineIndex(new Point2D(x, 0)));
		return interpolation(x, sl.getP1().getE(), sl.getP2().getE(), sl.getP1().getD(), sl.getP2().getD());
	}
	public double getDerivative(double x) {
		SingleLine sl = _lines.get(getLineIndex(new Point2D(x, 0)));
		return interpolation(x, sl.getP1().getE(), sl.getP2().getE(), sl.getP1().getN(), sl.getP2().getN());
	}
	private double interpolation(double x, double x1, double x2, double y1, double y2) {
		double fraction = (x-x1)/(x2-x1);
		return (y1+(y2-y1)*fraction);
	}
	
	
	public Point2D calTransmittedPoint(int iVp, int k1, int k2, Point2D p1, Point2D p2, int dx, int dz, double tiltedAngle) {
		double endX = 0.0;
		if(dx<0)  	endX = _chord.getP1().getE();
		else 		endX = _chord.getP2().getE();

		double derivative = getDerivative(p2.getE());
		double endZ = p2.getD()+derivative*(endX-p2.getE());
		Point2D endP = new Point2D(endX, endZ);

		//System.out.println(String.format("k=%d deriv=%f p2=%s", k, derivative, p2.toString()));

		//return calTransmittedPoint(iVp, iVTI, derivative, p0, p1, p2, layerIndex, rayParameter, dx, dz, iDynamic, tiltedAngle);

		return endP;
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
	
//	public int intersectExist(Point2D p1, Point2D p2) {
//		int k1 = 0;
//		int k2 = getNumOfSamples()-1;
//		int resultID = intersect(k1, k2, p1, p2);
//		if(resultID==-1) return resultID;
//		else if(resultID==k1||resultID==k2) return 100000+resultID;
//		else { }
//		
//		int k = getNumOfSamples()/2-1;
//		
//		// 1-order divide
//		k1 = 0;
//		k2 = k;
//		resultID = intersect(k1, k2, p1, p2);
//		if(resultID==k1||resultID==k2) return 100000+resultID;
//		if(resultID==-1) {
//			for(int i=k+1; i<getNumOfSamples(); i++) {
//				k1 = i-1;
//				k2 = i;
//				resultID = intersect(k1, k2, p1, p2);
//				if(resultID==k1||resultID==k2) return 100000+resultID;
//				else if(resultID==-2) return k1;
//				else { }
//			}
//		}else { 
//			for(int i=1; i<=k; i++) {
//				k1 = i-1;
//				k2 = i;
//				resultID = intersect(k1, k2, p1, p2);
//				if(resultID==k1||resultID==k2) return 100000+resultID;
//				else if(resultID==-2) return k1;
//				else { }
//			}
//		}
//			
//		return -1;
//	}
//	
//	public int intersect(int k1, int k2, Point2D p1, Point2D p2) {
//		int isLeft = Geometry2D.pointLinePosition(p2.getX(), p2.getZ(), p1.getX(), p1.getZ(), getAxisValue(k1), getValue(k1));
//		int isRight = Geometry2D.pointLinePosition(p2.getX(), p2.getZ(), p1.getX(), p1.getZ(), getAxisValue(k2), getValue(k2));
//		
//		if(isLeft==0) 				return k1;
//		else if(isRight==0) 		return k2;
//		else if(isLeft*isRight<0) 	return -2;
//		else 						return -1;
//	}
//	
//	public boolean pause1() {
//		System.out.println("Press ENTER key to continue...");
//		int i = 0; 
//		char c; 
//		try {
//			while((c = (char) System.in.read()) != 'S') {
//				if((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z')) {
//					i++;
//					System.out.println("Pass # " + i + " c="+c);
//				}
//			}
//		}
//		catch (IOException e){
//			System.out.println("Error reading from user");
//		}
//		return false;
//	}
	
	public String toString(int id)	{
		int nx = _lines.size();
		double x0 = _lines.get(0).getP1().getE();
		double dx = _lines.get(0).getP1().distance(_lines.get(0).getP2());
		double x1 = _lines.get(nx-1).getP1().getE();
		double cal_x1 = x0+(nx-1)*dx;
		String b = "nx="+nx+" x0="+x0+ " dx="+dx+ " x1="+x1+ " cal_x1="+cal_x1;
		if(id==2) {
			for(int i=0; i<_lines.size(); i++) {
				if(i%5==0) b = new String(b.concat("\n"+_lines.get(i).getP1().toString()));
				else b = new String(b.concat(_lines.get(i).getP1().toString()));
			}
		}
		if(id==3) {
			b = nx+"\n";
			for(int i=0; i<_lines.size(); i++) {
				b = new String(b.concat(_lines.get(i).getP1().getE()+" "+_lines.get(i).getP1().getD()+"\n"));
			}
		}
		return b;
	}
}
