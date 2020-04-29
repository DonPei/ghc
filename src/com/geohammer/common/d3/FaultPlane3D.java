package com.geohammer.common.d3;

import com.geohammer.core.planarmodel.Point3D;

public class FaultPlane3D {
	private String 		_eventName 	= null;
	private Point3D 	_origin 	= null;
	private Point3D []	_plane 		= null;
	private Point3D []	_squarePlane 	= null;
	private double 		_strike 	= 0;
	private double 		_dip 		= 0;
	private double 		_rake 		= 0;
	private double 		_strike2 	= 0;
	private double 		_dip2 		= 0;
	private double 		_rake2 		= 0;
	private double 		_radius 	= 0;
	private double 		_alpha 		= 0;
	private double 		_magnitude 	= 0;
	
	double [] 	_P 				= new double[3];
	double [] 	_T 				= new double[3];
	double [] 	_B 				= new double[3];
	double [] 	_normV 			= new double[3];
	double [] 	_slipV 			= new double[3];
	double [] 	_normVtau 		= new double[3];
	double [] 	_slipVtau 		= new double[3];
	double [][] _deviatoricMT	= new double[3][3];	

	double _percentageISO 		= 0.0;
	double _percentageCLVD 		= 0.0;
	double _percentageDC 		= 0.0;
	
	
	private Point3D []	_greatCircle= null;
	private Point3D []	_greatCircle2= null;
	
	public FaultPlane3D(String eventName, double northing, double easting, double down, 
			double strike, double dip, double rake, double alpha, double magnitude, 
			double percentageISO, double percentageCLVD, double percentageDC, double radius) {
		_origin 	= new Point3D(easting, northing, down);
		_strike 	= strike;
		_dip 		= dip;
		_rake 		= rake;
		_alpha 		= alpha;
		_magnitude  = magnitude;
		_radius 	= radius;
		_eventName 	= eventName;
		
		_percentageISO 	= percentageISO;
		_percentageCLVD = percentageCLVD;
		_percentageDC 	= percentageDC;
		
		double l = Math.sqrt(getArea());
		_squarePlane = applyRotation(dip, strike, l, l);
		_plane = applyRotation(36, dip, strike, _radius);
	}	
	
	public String getEventName() 				{ return _eventName; }
	public double getDip()						{ return _dip; }
	public double getStrike()					{ return _strike; }
	public double getRake()						{ return _rake; }
	public double getAlpha()					{ return _alpha; }
	public double getMagnitude()				{ return _magnitude; }
	public double getDip2()						{ return _dip2; }
	public double getStrike2()					{ return _strike2; }
	public double getRake2()					{ return _rake2; }
	
	public double [] getP() 					{ return _P; }
	public double [] getT() 					{ return _T; }
	public double [] getB() 					{ return _B; }
	public double [] getNormV() 				{ return _normV; }
	public double [] getSlipV() 				{ return _slipV; }
	
	public double getRadius()					{ return _radius; }
	public double getArea()						{ return Math.PI*_radius*_radius; }
	public Point3D getOrigin() 					{ return _origin; }
	public double getN() 						{ return _origin.getN(); }
	public double getE() 						{ return _origin.getE(); }
	public double getD() 						{ return _origin.getD(); }
	public Point3D [] getPlane() 				{ return _plane;}
	public Point3D getPlane(int index) 			{ return _plane[index]; }
	public Point3D [] getGreatCircle() 			{ return _greatCircle;}
	public Point3D [] getGreatCircle2() 		{ return _greatCircle2;}
	
	public double getPercentageISO() 			{ return _percentageISO; }
	public double getPercentageCLVD() 			{ return _percentageCLVD; }
	public double getPercentageDC() 			{ return _percentageDC; }
	
	public void setStrike2(double strike2) 		{ _strike2 = strike2; }
	public void setDip2(double dip2) 			{ _dip2 = dip2; }
	public void setRake2(double rake2) 			{ _rake2 = rake2; }
	public void setP(int index, double v) 		{ _P[index] = v; }
	public void setB(int index, double v) 		{ _B[index] = v; }
	public void setT(int index, double v) 		{ _T[index] = v; }
	public void setNormV(int index, double v) 	{ _normV[index] = v; }
	public void setSlipV(int index, double v) 	{ _slipV[index] = v; }
	
	public void calGreatCircle(int N) {
		if(_greatCircle==null) _greatCircle = calGreatCircle(_dip, _strike, N);
		if(_greatCircle2==null) _greatCircle2 = calGreatCircle(_dip2, _strike2, N);
	}
	public Point3D [] calGreatCircle(double dip, double strike, int N) 	{ 
		Point3D [] greatCircle = new Point3D[N]; 
		//init to a vertical plane in right-hand system positive Z up
		double dAngle = 2*Math.PI/(N-1);
		for(int i=0; i<N; i++) {
			double angle = i*dAngle;
			greatCircle[i] = new Point3D(0, Math.cos(angle), Math.sin(angle));
			
		}
		applyRotation(dip, strike, greatCircle);
		
		return greatCircle;
	}
	public static void applyRotation(double dip, double strike, float [] v) {
		Point3D[] planes = new Point3D[v.length/3];
		for(int i=0, k=0; i<planes.length; i++) {
			planes[i] = new Point3D(v[k++], v[k++], v[k++]);
		}
		applyRotation(dip, strike, planes);
		for(int i=0, k=0; i<planes.length; i++) {
			v[k++] = (float)planes[i].getE();
			v[k++] = (float)planes[i].getN();
			v[k++] = (float)planes[i].getD();
		}
	}
	// input Point3D in XRIGHT_YIN_ZUP
	// output Point3D in XRIGHT_YOUT_ZDOWN
	public static void applyRotation(double dip, double strike, Point3D[] plane) {
		double degToRat = Math.PI/180.0;
		double theCos = Math.cos((90-dip)*degToRat);
		double theSin = Math.sin((90-dip)*degToRat);
		double [][] rmY = new double[][] {
				{theCos, 	0, 		-theSin },
				{0, 		1, 		0 },
				{theSin, 	0, 		theCos }
		};
		for(int i=0; i<plane.length; i++) plane[i].multiply(rmY);
		
		double rz = 0.0;
		if(strike>0&&strike<=90) { rz = 90-strike; }
		else if(strike>90&&strike<=180) { rz = 90-strike; }
		else if(strike>180&&strike<=270) { rz = 90+(360-strike); }
		else if(strike>270&&strike<=360) { rz = 90+(360-strike); }
		
		theCos = Math.cos(rz*degToRat);
		theSin = Math.sin(rz*degToRat);
		double [][] rmZ = new double[][] {
				{theCos, 	theSin, 	0 },
				{-theSin, 	theCos, 	0 },
				{0, 		0, 			1 }
		};
		for(int i=0; i<plane.length; i++) plane[i].multiply(rmZ);
		
		for(int i=0; i<plane.length; i++) { plane[i].setD(-plane[i].getD()); }
		for(int i=0; i<plane.length; i++) { plane[i].setN(-plane[i].getN()); }
	}
	
//	public Point3D[] applyRotation(double dip, double strike, double l) {
//		// init to a vertical plane in right-hand system positive Z up
//		// Point3D x-easting y-northing z up
//		Point3D[] plane = new Point3D[] {
//				new Point3D(0, l, -l), new Point3D(0, l, l), new Point3D(0, -l, l), new Point3D(0, -l, -l)
//		};
//		applyRotation(dip, strike, plane);
//		
//		return plane;
//	}
	public Point3D[] applyRotation(double dip, double strike, double length, double height) {
		double l = 0.5*length;
		double h = 0.5*height;
		// init to a vertical plane in right-hand system positive Z up
		// Point3D x-easting y-northing z up
		Point3D[] plane = new Point3D[] {
				new Point3D(0, l, -h), new Point3D(0, l, h), new Point3D(0, -l, h), new Point3D(0, -l, -h)
		};
		applyRotation(dip, strike, plane);
		
		return plane;
	}
	public Point3D[] applyRotation(int m, double dip, double strike, double r) {
		//init to a vertical plane in right-hand system positive Z up
		Point3D [] plane = new Point3D[m];
		double dAngle = (2*Math.PI)/(m-1);
		for(int i=0; i<plane.length; i++) {
			double y = r*Math.cos(dAngle*i);
			double z = r*Math.sin(dAngle*i);
			plane[i] = new Point3D(0, y, z);
		}
		
		applyRotation(dip, strike, plane);
		
		return plane;
	}
	
	public float[] makeQuads(double height) {		
		if(height<0.0) return null;
		double length = getArea()/height;
		Point3D []	recPlane = applyRotation(_dip, _strike, length, height);
		float[] xyz = new float[3*recPlane.length];
		
		float px = (float)_origin.getN();
		float py = (float)_origin.getE();
		float pz = (float)_origin.getD();

		for(int i=0, k=0; i<recPlane.length; i++) {
			xyz[k++] = (float)(px+recPlane[i].getN());  
			xyz[k++] = (float)(py+recPlane[i].getE());  
			xyz[k++] = (float)(pz+recPlane[i].getD());
		}

		return xyz;
	}
	
	public float[] makeQuads() {		
		float[] xyz = new float[3*_squarePlane.length];
		
		float px = (float)_origin.getN();
		float py = (float)_origin.getE();
		float pz = (float)_origin.getD();

		for(int i=0, k=0; i<_squarePlane.length; i++) {
			xyz[k++] = (float)(px+_squarePlane[i].getN());  
			xyz[k++] = (float)(py+_squarePlane[i].getE());  
			xyz[k++] = (float)(pz+_squarePlane[i].getD());
		}

		return xyz;
	}
	
	public float[] makeTriangles() {		
		float[] xyz = new float[3*(_plane.length-1)*3];
		
		float px = (float)_origin.getN();
		float py = (float)_origin.getE();
		float pz = (float)_origin.getD();

		for(int i=0, k=0; i<_plane.length-1; i++) {
			xyz[k++] = (float)(px+_plane[i].getN());  
			xyz[k++] = (float)(py+_plane[i].getE());  
			xyz[k++] = (float)(pz+_plane[i].getD());
			
			xyz[k++] = px;  
			xyz[k++] = py;  
			xyz[k++] = pz;
			
			xyz[k++] = (float)(px+_plane[i+1].getN());  
			xyz[k++] = (float)(py+_plane[i+1].getE());  
			xyz[k++] = (float)(pz+_plane[i+1].getD());			
		}

		return xyz;
	}

}
