package com.geohammer.core;

public class Sensor extends PointAbstract{
	public double [][] _rm = null;

	//all should be in global East-North-Up coordinate system
	//local coordinate system for DS250’s with the data read in z,y,x order
	double _theta 	= 0; 		// a drilling trajectory with a bearing angle (theta) at any location relative to north.
	double _phi 	= 0; 		// deviation angle phi, relative to a vertical 
	double _delta 	= 0; 		// tool orientation angle, For 2D or 3D vertical well the tool angle (d) is 
								// defined as the X-component direction from North, positive clockwise

	public Sensor(int id, double x, double y, double z) {
		this(id, "Sensor", x, y, z, 0 , 0, 0);
	}
	public Sensor(int id, String label, double x, double y, double z, double theta, double phi, double delta) {
		super(id, label, x, y, z);
		_theta 	= theta;
		_phi	= phi;
		_delta 	= delta;
		_rm 	= calRotationMatrix(delta*3.1415926/180.0);
	}

	public double getTheta() 			{ return _theta; }
	public double getPhi() 				{ return _phi; }
	public double getDelta() 			{ return _delta; }
	public double [][] getRm() 			{ return _rm; }
	
	public void setTheta(double theta) 	{ _theta = theta; }
	public void setPhi(double phi) 		{ _phi = phi; }
	public void setDelta(double delta) 	{ _delta = delta; }
	
	public Sensor copy() {
		Sensor sensor = new Sensor(getId(), getLabel(), getX(), getY(), getZ(), _theta, _phi, _delta);
		return sensor;
	}
	
	public void calRotationMatrixXYZ2ENUp() { _rm = calRotationMatrixXYZ2ENUp(_theta, _phi, _delta); }
	//rotational matrix converts the original measured signals in xyz into ENUp coordinate system
	//adopted from RecordXYZ2ENUp.m
	public double [][] calRotationMatrixXYZ2ENUp(double theta, double phi, double delta) {
		double a = Math.PI/180.0;
		theta = theta*a;  //degree to radians
		phi = phi*a;
		delta = delta*a; 

		//direction cosines between XYZ and East
		double l1 = Math.sin(delta) * Math.cos(theta) + Math.cos(delta) * Math.cos(phi) * Math.sin(theta);
		double l2 = -Math.cos(delta) * Math.cos(theta) + Math.sin(delta) * Math.cos(phi) * Math.sin(theta);
		double l3 = -Math.sin(phi) * Math.sin(theta);    // this has negative sign which is different from Norm's doc

		//direction coMath.sines between XYZ and North
		double m1 = -Math.sin(delta) * Math.sin(theta) + Math.cos(delta) * Math.cos(phi) * Math.cos(theta);
		double m2 = Math.cos(delta) * Math.sin(theta) + Math.sin(delta) * Math.cos(phi) * Math.cos(theta);
		double m3 = -Math.sin(phi) * Math.cos(theta);    // this has negative sign which is different from Norm's doc

		//direction coMath.sines between XYZ and Up
		double n1 = Math.cos(delta) * Math.sin(phi);
		double n2 = Math.sin(delta) * Math.sin(phi);
		double n3 = Math.cos(phi);  

		return new double[][]{{l1, l2, l3}, {m1, m2 , m3}, {n1, n2, n3}};
		//return new double[][]{{l1, m1, n1}, {l2, m2 , n2}, {l3, m3, n3}};
	}

	public double [][] getRotationMatrix() { return _rm; }
	
	//assuming a vertical well, from North, positive clockwise
	public double [][] calRotationMatrix(double delta) {
		double a = Math.PI/180.0;
		delta = delta*a; //degree to radians

		double l1 = Math.cos(delta);
		double l2 = Math.sin(delta);
		double l3 = 0;    

		double m1 = Math.sin(delta);
		double m2 = -Math.cos(delta);
		double m3 = 0;    // this has negative sign which is different from Norm's doc

		double n1 = 0;
		double n2 = 0;
		double n3 = -1;  

		//return new double[][]{{l1, l2, l3}, {m1, m2 , m3}, {n1, n2, n3}};
		return new double[][]{{l1, m1, n1}, {l2, m2 , n2}, {l3, m3, n3}};
	}

//	public double [][] getUnitVector(double srcX, double srcY, double srcZ) {
//		return getUnitVector(srcX, srcY, srcZ, -99999.0);
//	}
//
//	public double [][] getUnitVector(double srcX, double srcY, double srcZ, double takeOffAngle) {
//		return SeisPTUtil.getUnitVector(getX(), getY(), getZ(), srcX, srcY, srcZ, -99999.0);
//	}

}
