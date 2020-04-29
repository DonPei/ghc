package com.geohammer.core.geometry;

import edu.mines.jtk.sgl.Tuple3;

/**
 * A vector with three components x, y, and z.
 * @author Dave Hale, Colorado School of Mines
 * @version 2005.05.20
 */
public class Vector3D extends Tuple3 {

	private double _eps = 1.0e-6;
	public double _azimuth = 0.0;  	// between 0 - 360
	public double _theta = 0.0;  	// between -90 - 90  A horizontal line has 0 angle. 
	//A positive line has 90, a negative line has -90

	public Vector3D() { }

	/**
	 * Constructs a vector with specified components.
	 * @param x the x component.
	 * @param y the y component.
	 * @param z the z component.
	 */
	public Vector3D(double x, double y, double z) {
		super(x,y,z);
	}

	/**
	 * Constructs a copy of the specified vector.
	 * @param v the vector.
	 */
	public Vector3D(Vector3D v) {
		super(v.x,v.y,v.z);
	}
	
	public double calAzimuth() {
		double an = x;
		double ae = y;
		double ad = z;
		double az = 0.0;
		if(ae<_eps && ae>-_eps) 		az = 0.0;
		else if(an<_eps && an>-_eps) 	az = 90.0;
		else {
			double theTan = Math.abs(ae/an);			
			az = Math.atan(theTan)*180/Math.PI;			
			if(ae>0 && an>0) { }
			else if(ae>0 && an<0) { az = 180-az; }
			else if(ae<0 && an<0) { az = 180+az; }
			else if(ae<0 && an>0) { az = 360-az; }	
		}		
		_azimuth = az;

		return az;		
	}
	public double calTheta() {
		double an = x;
		double ae = y;
		double ad = z;
		double al = Math.sqrt(an*an+ae*ae);
		double angle = 0.0;

		if(ad<_eps && ad>-_eps) angle = 0.0;
		else if(al<_eps && al>-_eps) {
			if(ad>0.0) angle = 90.0;
			else angle = -90.0;
		} else {
			double theTan = ad/al;			
			angle = Math.atan(theTan)*180.0/Math.PI;
		}
		_theta = angle;

		return angle;		
	}
	
	public double length() {
		return Math.sqrt(x*x+y*y+z*z);
	}

	/**
	 * Returns the length-squared of this vector.
	 * @return the length-squared.
	 */
	public double lengthSquared() {
		return x*x+y*y+z*z;
	}

	/**
	 * Returns the negation -u of this vector u.
	 * @return the negation -u.
	 */
	public Vector3D negate() {
		return new Vector3D(-x,-y,-z);
	}

	/**
	 * Negates this vector.
	 * @return this vector, negated.
	 */
	public Vector3D negateEquals() {
		x = -x;
		y = -y;
		z = -z;
		return this;
	}

	/**
	 * Returns the unit vector with the same direction as this vector.
	 * @return the unit vector.
	 */
	public Vector3D normalize() {
		double d = length();
		double s = (d>0.0)?1.0/d:1.0;
		return new Vector3D(x*s,y*s,z*s);
	}

	/**
	 * Normalizes this vector to have unit length; makes this a unit vector.
	 * @return this vector, normalized.
	 */
	public Vector3D normalizeEquals() {
		double d = length();
		double s = (d>0.0)?1.0/d:1.0;
		x *= s;
		y *= s;
		z *= s;
		return this;
	}

	/**
	 * Returns the vector sum u+v for this vector u.
	 * @param v the other vector.
	 * @return the vector sum u+v
	 */
	public Vector3D plus(Vector3D v) {
		return new Vector3D(x+v.x,y+v.y,z+v.z);
	}

	/**
	 * Adds a vector v to this vector u.
	 * @param v the other vector.
	 * @return this vector, after adding the vector v.
	 */
	public Vector3D plusEquals(Vector3D v) {
		x += v.x;
		y += v.y;
		z += v.z;
		return this;
	}

	public Vector3D applyMatrix(double[][] m) {
		double [] d = new double[]{ x, y, z };
		double [] n = new double[3];
		for(int i=0; i<m.length; i++) {
			double sum = 0;
			for(int j=0; j<m[i].length; j++) sum += m[i][j]*d[j];
			n[i] = sum;
		}
		
		return new Vector3D(n[0], n[1], n[2]);
	}
	public Vector3D applyMatrixEqual(double[][] m) {
		double [] d = new double[]{ x, y, z };
		double [] n = new double[3];
		for(int i=0; i<m.length; i++) {
			double sum = 0;
			for(int j=0; j<m[i].length; j++) sum += m[i][j]*d[j];
			n[i] = sum;
		}
		x = n[0];
		y = n[1];
		z = n[2];
		return this;
	}
	/**
	 * Returns the vector difference u-v for this vector u.
	 * @param v the other vector.
	 * @return the vector difference u-v
	 */
	public Vector3D minus(Vector3D v) {
		return new Vector3D(x-v.x,y-v.y,z-v.z);
	}

	/**
	 * Subtracts a vector v from this vector u.
	 * @param v the other vector.
	 * @return this vector, after subtracting the vector v.
	 */
	public Vector3D minusEquals(Vector3D v) {
		x -= v.x;
		y -= v.y;
		z -= v.z;
		return this;
	}

	/**
	 * Returns the scaled vector s*u for this vector u.
	 * @param s the scale factor.
	 * @return the scaled vector.
	 */
	public Vector3D times(double s) {
		return new Vector3D(x*s,y*s,z*s);
	}

	/**
	 * Scales this vector.
	 * @param s the scale factor.
	 * @return this vector, scaled.
	 */
	public Vector3D timesEquals(double s) {
		x *= s;
		y *= s;
		z *= s;
		return this;
	}

	/**
	 * Returns the dot product of this vector u and the specified vector v.
	 * @param v the vector v.
	 * @return the dot product.
	 */
	public double dot(Vector3D v) {
		return x*v.x+y*v.y+z*v.z;
	}

	/**
	 * Returns the cross product of this vector u and the specified vector v.
	 * @param v the vector v.
	 * @return the cross product.
	 */
	public Vector3D cross(Vector3D v) {
		return new Vector3D(y*v.z-z*v.y,z*v.x-x*v.z,x*v.y-y*v.x);
	}
}

