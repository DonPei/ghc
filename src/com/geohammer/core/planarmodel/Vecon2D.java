package com.geohammer.core.planarmodel;

import java.util.Arrays;

public class Vecon2D extends Vecon3D {
	
	public Vecon2D() { }
	public Vecon2D(String selectedFileName)	{
		this(0, selectedFileName );
		//System.out.println(toString());
	}
	public Vecon2D(int iMethod, String selectedFileName)	{
		read(iMethod, selectedFileName );
		//System.out.println(toString());
	}
	public Vecon2D(int iUnit, int id, 
			int nx, int nz, double dx, double dz, double x0, double z0, float [][] v) {
		this(iUnit, id, 0, 0, 0, 
				0, 0, 0, 0, 0, 0, 0, 
				0, 0, 0, 0, 0, 0, 
				0, 0, 0, 0,
				1, nx, nz, 1, dx, dz, 0, x0, z0, v);
	}
	public Vecon2D(int iUnit, int id, double vp, double vs, double den, 
			double delta, double epsilon, double gamma, double qp, double qs, double theta, double phi,
			double vpGradientX, double vpGradientY, double vpGradientZ, double vsGradientX, double vsGradientY, double vsGradientZ, 
			double delta1, double epsilon1, double gamma1, double delta3, 
			int nx, int ny, int nz, double dx, double dy, double dz, double x0, double y0, double z0, float [][] v) {
		super(iUnit, id, vp, vs, den, delta, epsilon, gamma, qp, qs, theta, phi, 
				vpGradientX, vpGradientY, vpGradientZ, vsGradientX, vsGradientY, vsGradientZ, 
				delta1, epsilon1, gamma1, delta3, 
				nx, ny, nz, dx, dy, dz, x0, y0, z0, new float [][][] {v});
	}
	
	public Vecon2D copy() {
		Vecon2D target = new Vecon2D(_iUnit, getId(), 
				getNx(), getNz(), getDx(), getDz(), getX0(), getZ0(), _data[0]);
		return target;
	}

	public float getData(int iy, int iz) 			{ return _data[0][iy][iz];	}
	public float getData(double y, double z) 	{ 
		if(onGridPoint(y, z)) return getData((int)(y/getDy()), (int)(z/getDz()) );
		else return interpolate(y, z);
	}
	public boolean onGridPoint(double y, double z) {
		double ay = y/getDy(), az = z/getDz();
		return (ay-(int)ay)==0&&(az-(int)az)==0;
	}
	
	public void setIUnit(int iUnit) 				{ _iUnit = iUnit; }
	public void setData(float [][] v) 				{ _data[0] = v; }
	public void setData(int iy, int iz, float av) 	{ _data[0][iy][iz] = av;	}
	
	public float [][] toZXArray() {
		float [][] src = _data[0];
		float [][] target = new float[src[0].length][src.length];
		for(int i=0; i<target.length; i++) {
			for(int j=0; j<target[i].length; j++) {
				target[i][j] = src[j][i];
			}
		}
		return target;		
	}
	
	//z is the fastest direction
	public float interpolate(double y, double z) {
		float [][] v = getData()[0];
		int iz = (int)((z-getZ0())/getDz());
		int iy = (int)((y-getY0())/getDy());

		double az = getZ0() + iz*getDz();
		double ay = getY0() + iy*getDy();

		double uz = (z-az)/getDz();
		double uy = (y-ay)/getDy();
		
		double y00 = v[iy][iz];
		double y01 = v[iy][iz+1];
		double y10 = v[iy+1][iz];
		double y11 = v[iy+1][iz+1];
		
		return (float)(y00*(1.0-uz)*(1.0-uy)+y10*(1.0-uz)*(uy)+ y01*(uz)*(1.0-uy)+y11*(uz)*(uy));
	}
	
	public void printData() {
		float [][] v = getData()[0];
		int j = v[0].length;
		for(int i=0; i<v.length; i++) System.out.println(i+" "+j+" "+ Arrays.toString(v[i]));
	}

}
