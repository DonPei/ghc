package com.geohammer.core.geometry;

public class Point3D extends Tuple3D {

  /**
   * Constructs a point with coordinates zero.
   */
  public Point3D() {
  }

  /**
   * Constructs a point with specified coordinates.
   * @param x the x coordinate.
   * @param y the y coordinate.
   * @param z the z coordinate.
   */
  public Point3D(double x, double y, double z) {
    super(x,y,z);
  }

  public double surfaceDistance(Point3D other) {
	  return surfaceDistance(other.x, other.y);
  }
  public double surfaceDistance(double ax, double ay) {
	  double bx = x-ax;
	  double by = y-ay;
	  return Math.sqrt(bx*bx+by*by);
  }
	
  /**
   * Constructs a copy of the specified point.
   * @param p the point.
   */
  public Point3D(Point3D p) {
    super(p.x,p.y,p.z);
  }

  /**
   * Returns the point q = p+v, for this point p and the specified vector v.
   * @param v the vector v.
   * @return the point q = p+v.
   */
  public Point3D plus(Vector3D v) {
    return new Point3D(x+v.x,y+v.y,z+v.z);
  }

  /**
   * Returns the point q = p-v, for this point p and the specified vector v.
   * @param v the vector v.
   * @return the point q = p-v.
   */
  public Point3D minus(Vector3D v) {
    return new Point3D(x-v.x,y-v.y,z-v.z);
  }

  /**
   * Returns the vector v = p-q, for this point p and the specified point q.
   * @param q the point q.
   * @return the vector v = p-q.
   */
  public Vector3D minus(Point3D q) {
    return new Vector3D(x-q.x,y-q.y,z-q.z);
  }

  /**
   * Moves this point p by adding the specified vector v.
   * @param v the vector v.
   * @return this point, p += v, moved.
   */
  public Point3D plusEquals(Vector3D v) {
    x += v.x;
    y += v.y;
    z += v.z;
    return this;
  }

  /**
   * Moves this point p by subtracting the specified vector v.
   * @param v the vector v.
   * @return this point, p -= v, moved.
   */
  public Point3D minusEquals(Vector3D v) {
    x -= v.x;
    y -= v.y;
    z -= v.z;
    return this;
  }

  /**
   * Returns an affine combination of this point p and the specified point q.
   * @param a the weight of the point q.
   * @param q the point q.
   * @return the affine combination (1-a)*p + a*q.
   */
  public Point3D affine(double a, Point3D q) {
    double b = 1.0-a;
    Point3D p = this;
    return new Point3D(b*p.x+a*q.x,b*p.y+a*q.y,b*p.z+a*q.z);
  }

  /**
   * Returns the distance between this point p and the specified point q.
   * @param q the point.
   * @return the distance |q-p|.
   */
  public double distanceTo(Point3D q) {
    double dx = x-q.x;
    double dy = y-q.y;
    double dz = z-q.z;
    return Math.sqrt(dx*dx+dy*dy+dz*dz);
  }
  
}

