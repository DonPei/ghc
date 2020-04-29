package com.geohammer.core;

public abstract class PointAbstract {
	public int id 		= 0;
	public String label = null; 
	
	public double x 	= 0.0; 			//East
	public double y		= 0.0; 			//North
	public double z		= 0.0; 			//Depth
		
	public PointAbstract() {
		this(0, null, 0.0, 0.0, 0.0);
	}
	public PointAbstract(double x, double y, double z) {
		this(0, null, x, y, z);
	}
	public PointAbstract(int id, String label, double x, double y, double z) {
		this.id 	= id;
		this.label 	= label;
		this.x 		= x;
		this.y 		= y;
		this.z 		= z;
	}
	
	public int getId() 					{ return this.id; }
	public String getLabel() 			{ return this.label; }
	
	public double getX() 				{ return this.x; }
	public double getY() 				{ return this.y; }
	public double getZ() 				{ return this.z; }
	public double [] getXYZ() 			{ return new double[] {this.x, this.y, this.z}; }
	
	public void setId(int id) 			{ this.id = id; }
	public void setLabel(String label) 	{ this.label = label; }
	
	public void setX(double x) 			{ this.x = x; }
	public void setY(double y) 			{ this.y = y; }
	public void setZ(double z) 			{ this.z = z; }
	public void setXYZ(double [] xyz) 	{ this.x = xyz[0]; this.y = xyz[1]; this.z = xyz[2];}
	
	public String getReceiverLocationString() { return this.x + " "+ this.y+" "+this.z;	}
	
	public String toString() {
		String b = "(x,y,z)= "+this.x + ", "+ this.y+", "+this.z;
		String a = "id="+this.id+" label="+this.label;
		b = b.concat("\n"+a+"\n");
		
		return b;
	}
	
	public double distance(PointAbstract other) {
		return distance(other.getX(), other.getY(), other.getZ());
	}
	
	public double distance(double ax, double ay, double az) {
		double bx = x-ax;
		double by = y-ay;
		double bz = z-az;
		return Math.sqrt(bx*bx+by*by+bz*bz);
	}
	
	public double SurfaceDistance(PointAbstract other) {
		return SurfaceDistance(other.getX(), other.getY());
	}
	public double SurfaceDistance(double ax, double ay) {
		double bx = x-ax;
		double by = y-ay;
		return Math.sqrt(bx*bx+by*by);
	}

}
