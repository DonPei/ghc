package com.geohammer.core.planarmodel;

import java.util.Arrays;

public class Grid2D extends Grid3D {
	public Grid2D() {
		this(2, 1, 1, 1, 1, 0, 0, null);
	}
	public Grid2D(int iUnit, int ny, int nz, double dy, double dz, double y0, double z0, float [][] data) {
		super(iUnit,  1, ny, nz, 1, dy, dz, 0,  y0, z0, data==null?null:new float[][][] { data });
	}
	
	public Grid2D(String selectedFileName)	{
		this(0, selectedFileName );
		//System.out.println(toString());
	}
	public Grid2D(int iMethod, String selectedFileName)	{
		//read(iMethod, selectedFileName );
		//System.out.println(toString());
	}
	
	public Grid2D copy() {
		Grid2D target = new Grid2D(getIUnit(),  getNy(), getNz(), getDy(), getDz(), getY0(), getZ0(), null);
		float [][][] data = copy(_data);
		target.setData(data);
		return target;
	}
	
	public float[][] getDataShallow() 			{ return _data[0];	}
	public float getData(int iy, int iz) 		{ return _data[0][iy][iz];	}
	public float getData(double y, double z) 	{
		double ay = clipY(y);
		double az = clipZ(z);
		if(onGridPoint(ay, az)) return getData((int)(ay/getDy()), (int)(az/getDz()) );
		else return interpolate(ay, az);
	}
	
	public boolean onGridPoint(double y, double z) {
		double ay = y/getDy(), az = z/getDz();
		return (ay-(int)ay)==0&&(az-(int)az)==0;
	}
	
	public void setData(float [][] v) 				{ _data[0] = v; }
	public void setData(int iy, int iz, float av) 	{ _data[0][iy][iz] = av;	}
	
	public float [][] getDataDeep() {
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
		int iy = (int)((y-getY0())/getDy());
		int iz = (int)((z-getZ0())/getDz());
		
		iy = clipY(iy);
		iz = clipZ(iz);
		
		int izPlusOne = iz+1;
		if(iz==getNz()-1) izPlusOne = iz;
		int iyPlusOne = iy+1;
		if(iy==getNy()-1) iyPlusOne = iy;
		
		double az = getZ0() + iz*getDz();
		double ay = getY0() + iy*getDy();

		double uz = (z-az)/getDz();
		double uy = (y-ay)/getDy();
		
		double v00 = v[iy][iz];
		double v01 = v[iy][izPlusOne];
		double v10 = v[iyPlusOne][iz];
		double v11 = v[iyPlusOne][izPlusOne];
		
		return (float)(v00*(1.0-uz)*(1.0-uy)+v10*(1.0-uz)*(uy)+ v01*(uz)*(1.0-uy)+v11*(uz)*(uy));
	}
	
	public void printData() {
		float [][] v = getData()[0];
		int j = v[0].length;
		for(int i=0; i<v.length; i++) System.out.println(i+" "+j+" "+ Arrays.toString(v[i]));
	}
	public float [] makeTriangles(float scalor) {
		float [][] data = getData()[0];
		int ne = getNz();
	    double de = getDz();
	    double e0 = getZ0();
	    int nn = getNy();
	    double dn = getDy();
	    double n0 = getY0();
	    
	    float[] xyz = new float[3*6*ne*nn];
	    float x0, x1, y0, y1;
	    int iePlus1 = 0;
	    int inPlus1 = 0;
	    for (int in=0,i=0; in<nn; ++in) {
	    	x0 = (float)(n0+in*dn);
	    	x1 = (float)(x0+dn);
	    	inPlus1 = clipY(in+1);
	    	for (int ie=0; ie<ne; ++ie) {
	    		y0 = (float)(e0+ie*de);
	    		y1 = (float)(y0+de);
	    		iePlus1 = clipZ(ie+1);
	    		
	    		float n0e0 = data[in][ie];
	    		float n0e1 = data[in][iePlus1];
	    		float n1e0 = data[inPlus1][ie];
	    		float n1e1 = data[inPlus1][iePlus1];
				
				xyz[i++] = x0;  xyz[i++] = y0;  xyz[i++] = n0e0*scalor;
	    		xyz[i++] = x0;  xyz[i++] = y1;  xyz[i++] = n0e1*scalor;
	    		xyz[i++] = x1;  xyz[i++] = y0;  xyz[i++] = n1e0*scalor;
	    		xyz[i++] = x1;  xyz[i++] = y0;  xyz[i++] = n1e0*scalor;
	    		xyz[i++] = x0;  xyz[i++] = y1;  xyz[i++] = n0e1*scalor;
	    		xyz[i++] = x1;  xyz[i++] = y1;  xyz[i++] = n1e1*scalor;	    		
	    	}
	    }
	    return xyz;
	}

}
