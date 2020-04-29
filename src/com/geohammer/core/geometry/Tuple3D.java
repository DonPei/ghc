package com.geohammer.core.geometry;

public class Tuple3D {

  public double x;

  /**
   * The component y.
   */
  public double y;

  /**
   * The component z.
   */
  public double z;

  /**
   * Constructs a tuple with all components equal to zero.
   */
  public Tuple3D() {
  }

  /**
   * Constructs a tuple with specified components.
   * @param x the x component.
   * @param y the y component.
   * @param z the z component.
   */
  public Tuple3D(double x, double y, double z) {
    this.x = x;
    this.y = y;
    this.z = z;
  }

  /**
   * Constructs a copy of the specified tuple.
   * @param t the tuple.
   */
  public Tuple3D(Tuple3D t) {
    x = t.x;
    y = t.y;
    z = t.z;
  }

  public boolean equals(Object obj) {
    if (this==obj)
      return true;
    if (obj==null || this.getClass()!=obj.getClass())
      return false;
    Tuple3D that = (Tuple3D)obj;
    return this.x==that.x && this.y==that.y && this.z==that.z;
  }

  public int hashCode() {
    long xbits = Double.doubleToLongBits(x);
    long ybits = Double.doubleToLongBits(y);
    long zbits = Double.doubleToLongBits(z);
    return (int)(xbits^(xbits>>>32)^
                 ybits^(ybits>>>32)^
                 zbits^(zbits>>>32));
  }

  public String toString() {
    return "("+x+","+y+","+z+")";
  }  

	public double getN() { return x; }
	public double getE() { return y; }
	public double getD() { return z; }

	public void setN(double n) { x = n; }
	public void setE(double e) { y = e; }
	public void setD(double d) { z = d; }
}
