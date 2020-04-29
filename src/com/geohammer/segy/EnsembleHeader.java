package com.geohammer.segy;

public class EnsembleHeader {
	private int _id 	= 0;
	private int _i1 	= 0;
	private int _i2 	= 0;
	private double _x 	= 0.0;
	private double _y 	= 0.0;
	private double _z 	= 0.0;
	
	public EnsembleHeader(int id, int i1, int i2, double x, double y, double z)	{
		_id = id;
		_i1 = i1;
		_i2 = i2;
		_x = x;
		_y = y;
		_z = z;
	}
	public int getId() 				{ return _id; }
	public int getI1() 				{ return _i1; }
	public int getI2() 				{ return _i2; }
	public double getX() 			{ return _x; }
	public double getY() 			{ return _y; }
	public double getZ() 			{ return _z; }
	public int getNumOfTraces() 	{ return _i2-_i1+1; }
	public int [] getIndex(int [] a) 		{ 
		int [] index = new int[getNumOfTraces()];
		for(int i=_i1, k=0; i<=_i2; i++, k++) index[k] = i;
		return index; 
	}
	
	public void setId(int id) 		{ _id = id; }
	public void setI1(int i1) 		{ _i1 = i1; }
	public void setI2(int i2) 		{ _i2 = i2; }
	public void setX(double x) 		{ _x = x; }
	public void setY(double y) 		{ _y = y; }
	public void setZ(double z) 		{ _z = z; }
	
	public void setLocation(double x, double y, double z) {
		_x = x;
		_y = y;
		_z = z;
	}
	
	public String toString() {
		return "id="+_id+" i1="+_i1+" i2="+_i2+" x="+_x+" y="+_y+" z="+_z;
	}
}
