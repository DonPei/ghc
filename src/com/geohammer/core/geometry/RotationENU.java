package com.geohammer.core.geometry;

public class RotationENU {
	//x - Easting y-northing z-up system 
	//the sense of the angle is defined by the right hand rule.
	public RotationENU() { }
	
	public static double[][] matrixRotateOnX(double omega) {
		double theCos = Math.cos(omega*Math.PI/180.0);
		double theSin = Math.sin(omega*Math.PI/180.0);
		return new double [][] {
				{1, 	0, 			0		},
				{0, 	theCos, 	theSin	},
				{0, 	-theSin, 	theCos	}
		};
	}
	public static double[][] matrixRotateOnY(double phi) {
		double theCos = Math.cos(phi*Math.PI/180.0);
		double theSin = Math.sin(phi*Math.PI/180.0);
		return new double [][] {
				{theCos, 	0, 		-theSin	},
				{0, 		1, 		0		},
				{theSin, 	0, 		theCos	}
		};
	}
	public static double[][] matrixRotateOnZ(double kappa) {
		double theCos = Math.cos(kappa*Math.PI/180.0);
		double theSin = Math.sin(kappa*Math.PI/180.0);
		return new double [][] {
				{theCos, 	theSin, 	0},
				{-theSin, 	theCos, 	0},
				{0, 		0, 			1}
		};
	}
	
	public static double[][] matrixRotateOnX2D(double omega) {
		double theCos = Math.cos(omega*Math.PI/180.0);
		double theSin = Math.sin(omega*Math.PI/180.0);
		return new double [][] {
				{theCos, 	theSin	},
				{-theSin, 	theCos	}
		};
	}
	public static double[][] matrixRotateOnY2D(double phi) {
		double theCos = Math.cos(phi*Math.PI/180.0);
		double theSin = Math.sin(phi*Math.PI/180.0);
		return new double [][] {
				{theCos, 	-theSin	},
				{theSin, 	theCos	}
		};
	}
	public static double[][] matrixRotateOnZ2D(double kappa) {
		double theCos = Math.cos(kappa*Math.PI/180.0);
		double theSin = Math.sin(kappa*Math.PI/180.0);
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

