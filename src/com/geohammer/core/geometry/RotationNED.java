package com.geohammer.core.geometry;

public class RotationNED {
	//nx - Northing ey-Easting dz-Down system 
	//the sense of the angle is defined by the right hand rule.
	public RotationNED() { }
	
	public static double[][] matrixRotateOnE(double omega) {
		double[][] m = matrixRotateOn2D(omega);
		return new double [][] {
				{m[1][1], 	0, 	m[1][0]},
				{0, 		1, 	0},
				{m[0][1], 	0, 	m[0][0]}
		};
	}
	public static double[][] matrixRotateOnN(double phi) {
		double[][] m = matrixRotateOn2D(phi);
		return new double [][] {
				{1, 	0, 	0},
				{0, 	m[0][0], 	m[0][1]},
				{0, 	m[1][0], 	m[1][1]}
		};
	}
	public static double[][] matrixRotateOnD(double azimuth) {
		double[][] m = matrixRotateOn2D(azimuth);
		return new double [][] {
				{m[0][0], 	m[0][1], 	0},
				{m[1][0], 	m[1][1], 	0},
				{0, 		0, 			1}
		};
	}
	
	public static double[][] matrixRotateOnD2D(double azimuth) 	{ return matrixRotateOn2D(azimuth); }	
	public static double[][] matrixRotateOnN2D(double angle) 	{ return matrixRotateOn2D(angle); }
	public static double[][] matrixRotateOnE2D(double angle) 	{ return matrixRotateOn2D(angle); }
	private static double[][] matrixRotateOn2D(double angle) {
		double theCos = Math.cos(angle*Math.PI/180.0);
		double theSin = Math.sin(angle*Math.PI/180.0);
		return new double [][] {
				{theCos, 	theSin},
				{-theSin, 	theCos}
		};
	}
	
	public static float[][] toFloat(double[][] dr) {
		int m = dr.length;
		float [][] ar = new float[m][];
		for(int i=0; i<m; i++) {
			int n = dr[i].length;
			ar[i] = new float[n];
			for(int j=0; j<n; j++) ar[i][j] = (float)dr[i][j];
		}
		return ar;
	}
	
	public static Point3D multiply(Point3D p, double [][] r) {
		Point3D q = new Point3D(0, 0, 0);		
		q.x = r[0][0]*p.x + r[0][1]*p.y+ r[0][2]*p.z;
		q.y = r[1][0]*p.x + r[1][1]*p.y+ r[1][2]*p.z;
		q.z = r[2][0]*p.x + r[2][1]*p.y+ r[2][2]*p.z;		
		return q;
	}
	public static void translate(Point3D p, double dn, double de, double dz) {
		p.x += dn; p.y += de; p.z += dz;
	}
	public static void scale(Point3D p, double dn, double de, double dz) {
		p.x *= dn; p.y *= de; p.z *= dz;
	}
	
	public static float [][] multiply2D(float [] x, float [] y, float [][] r) {
		int n = x.length;
		float [] ax = new float[n];
		float [] ay = new float[n];
		for(int i=0; i<n; i++) {
			ax[i] = r[0][0]*x[i] + r[0][1]*y[i];
			ay[i] = r[1][0]*x[i] + r[1][1]*y[i];		
		}
		return new float [][] { ax, ay};
	}
	
	public static double [][] multiply2D(double [] x, double [] y, double [][] r) {
		int n = x.length;
		double [] ax = new double[n];
		double [] ay = new double[n];
		for(int i=0; i<n; i++) {
			ax[i] = r[0][0]*x[i] + r[0][1]*y[i];
			ay[i] = r[1][0]*x[i] + r[1][1]*y[i];		
		}
		return new double [][] { ax, ay};
	}
	

}
