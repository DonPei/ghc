package com.geohammer.core.planarmodel;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class HalfSpaceBox extends HalfSpace {
	protected int 		nx		= 2;
	protected int 		ny		= 2;
	protected int 		nz		= 2;
	protected double 	dx		= 1.0;
	protected double 	dy		= 1.0;
	protected double 	dz		= 1.0;
	protected double 	x0		= 0.0;
	protected double 	y0		= 0.0;
	protected double 	z0		= 0.0;
	protected double 	x1		= 1.0;
	protected double 	y1		= 1.0;
	protected double 	z1		= 1.0;

	public HalfSpaceBox()	{ }

	public HalfSpaceBox(int id, double vp, double vs, double den, 
			double vpGradientX, double vpGradientY, double vpGradientZ, double vsGradientX, double vsGradientY, double vsGradientZ, 
			double delta, double epsilon, double gamma, double qp, double qs, double theta, double phi, 
			int nx, int ny, int nz, double dx, double dy, double dz, double x0, double y0, double z0)	{
		this(id, vp, vs, den, 
				vpGradientX, vpGradientY, vpGradientZ, vsGradientX, vsGradientY, vsGradientZ, 
				delta, epsilon, gamma, qp, qs, theta, phi, 
				0, 0, 0, 0, 
				nx, ny, nz, dx, dy, dz, x0, y0, z0);
	}
	public HalfSpaceBox(int id, int nx, int ny, int nz, double dx, double dy, double dz, double x0, double y0, double z0) {
		this(id, 1, 1, 1, 
				0, 0, 0, 0, 0, 0, 
				0, 0, 0, 0, 0, 0, 0, 
				0, 0, 0, 0, 
				nx, ny, nz, dx, dy, dz, x0, y0, z0);
	}
	public HalfSpaceBox(int id, double vp, double vs, double den, 
			double vpGradientX, double vpGradientY, double vpGradientZ, double vsGradientX, double vsGradientY, double vsGradientZ, 
			double delta, double epsilon, double gamma, double qp, double qs, double theta, double phi, 
			double delta1, double epsilon1, double gamma1, double delta3, 
			int nx, int ny, int nz, double dx, double dy, double dz, double x0, double y0, double z0)	{
		setValues(id, vp, vs, den, 
				vpGradientX, vpGradientY, vpGradientZ, vsGradientX, vsGradientY, vsGradientZ,
				delta, epsilon, gamma, qp, qs, theta, phi,  
				delta1, epsilon1, gamma1, delta3,
				0, 0, 0, 0, 0, 0, 0);
		setSize(nx, ny, nz, dx, dy, dz, x0, y0, z0);
	}
	
	public HalfSpaceBox copy() { 
		return new HalfSpaceBox(getId(), getVp(), getVs(), getDen(), 
				getVpGradientX(), getVpGradientY(), getVpGradientZ(), getVsGradientX(), getVsGradientY(), getVsGradientZ(), 
				getDelta(), getEpsilon(), getGamma(), getQp(), getQs(), getTheta(), getPhi(),
				getDelta1(), getEpsilon1(), getGamma1(), getDelta3(), 
				getNx(), getNy(), getNz(), getDx(), getDy(), getDz(), getX0(), getY0(), getZ0()); 
	}
	
	public void convertUnitSystem(double velScalor, double denScalor) {
		super.convertUnitSystem(velScalor, denScalor);
		x0 	*= velScalor;
		y0 	*= velScalor;
		z0 	*= velScalor;
		dx 	*= velScalor;
		dy 	*= velScalor;
		dz 	*= velScalor;
		x1 	*= velScalor;
		y1 	*= velScalor;
		z1 	*= velScalor;
	}
	
	public double getX( int ix )  		{ return x0 + clipX(ix)*dx; }
	public double getY( int iy )  		{ return y0 + clipY(iy)*dy; }
	public double getZ( int iz )  		{ return z0 + clipZ(iz)*dz; }
	
	public int getXIndex( double x)  	{ if(x<=x0) return 0; else if(x>=x1) return nx-1; else return (int)((x-x0)/dx); }
	public int getYIndex( double y)  	{ if(y<=y0) return 0; else if(y>=y1) return ny-1; else return (int)((y-y0)/dy); }
	public int getZIndex( double z)  	{ if(z<=z0) return 0; else if(z>=z1) return nz-1; else return (int)((z-z0)/dz); }
	
	public double getCenterX() 			{ return (x1+x0)/2.0; }
	public double getCenterY() 			{ return (y1+y0)/2.0; }
	public double getCenterZ() 			{ return (z1+z0)/2.0; }
	
	public int size() 			{ return nx*ny*nz; }
	public int clipX(int ix) 	{ return clip(ix, nx-1); }
	public int clipY(int iy) 	{ return clip(iy, ny-1); }
	public int clipZ(int iz) 	{ return clip(iz, nz-1); }
	private int clip(int iz, int N) {
		int k = 0;
		k = iz<0 ? 0:iz;
		k = k>N ? N:k;
		return k;
	}
	public double clipX(double x) 	{ return clip(x, x0, x1); }
	public double clipY(double y) 	{ return clip(y, y0, y1); }
	public double clipZ(double z) 	{ return clip(z, z0, z1); }
	private double clip(double x, double x0, double x1) {
		double a = x;
		a = x<x0?x0:a;
		a = x>x1?x1:a;
		return a;
	}
	public void translate(double dx, double dy, double dz) {
		x0 += dx; x1 += dx; 
		y0 += dy; y1 += dy; 
		z0 += dz; z1 += dz; 
	}
	
	public void scale(double scalor){
		x0 *= scalor;		y0 *= scalor;		z0 *= scalor;
		x1 *= scalor;		y1 *= scalor;		z1 *= scalor;
		dx *= scalor;		dy *= scalor;		dz *= scalor;
	}
	
	public void scale(double scalorx, double scalory, double scalorz) {
		x0 *= scalorx; x1 *= scalorx; 
		y0 *= scalory; y1 *= scalory; 
		z0 *= scalorz; z1 *= scalorz; 
		setSizeBySize(nx, ny, nz, x0, y0, z0, x1, y1, z1);
	}
	
	public String printClass(int id) {
		String a;
		String b = new String("");

		if(id==1) {
			a = String.format("nx=%d ny=%d nz=%d dx=%f dy=%f dz=%f x0=%f y0=%f z0=%f x1=%f y1=%f z1=%f",
					nx, ny, nz, dx, dy, dz, x0, y0, z0, x1, y1, z1);
			b = new String(b.concat("\n"+a));
		}

		return b;
	}
	public String getModelBasicInfo(int id) {
		String a;
		String b = new String("");

		a = String.format("Grid number in X direction nx=%d", nx); b = new String(b.concat("\n"+a));
		if(id==3) {a = String.format("Grid number in Y direction ny=%d", ny); b = new String(b.concat("\n"+a));}
		a = String.format("Grid number in Z direction nz=%d", nz); b = new String(b.concat("\n"+a));
		b = new String(b.concat("\n"));

		a = String.format("Spatial spacing in X direction dx=%f", dx); b = new String(b.concat("\n"+a));
		if(id==3) {a = String.format("Spatial spacing in Y direction dy=%f", dy); b = new String(b.concat("\n"+a));}
		a = String.format("Spatial spacing in Z direction dz=%f", dz); b = new String(b.concat("\n"+a));
		b = new String(b.concat("\n"));

		a = String.format("Starting point in X direction x0=%f", x0); b = new String(b.concat("\n"+a));
		if(id==3) {a = String.format("Starting point in Y direction y0=%f", y0); b = new String(b.concat("\n"+a));}
		a = String.format("Starting point in Z direction z0=%f", z0); b = new String(b.concat("\n"+a));
		b = new String(b.concat("\n"));

		a = String.format("Ending point in X direction x1=%f", x1); b = new String(b.concat("\n"+a));
		if(id==3) {a = String.format("Ending point in Y direction y1=%f", y1); b = new String(b.concat("\n"+a));}
		a = String.format("Ending point in Z direction z1=%f", z1); b = new String(b.concat("\n"+a));
		b = new String(b.concat("\n"));

		return b;
	}

	public String getModelBasicInfoSimple(int id) {
		String a;
		String b = new String("");

		a = String.format("nx=%d", nx); b = new String(b.concat(" "+a));
		if(id==3) {a = String.format("ny=%d", ny); b = new String(b.concat(" "+a));}
		a = String.format("nz=%d", nz); b = new String(b.concat(" "+a));
		b = new String(b.concat("\n"));

		a = String.format("dx=%f", dx); b = new String(b.concat(" "+a));
		if(id==3) {a = String.format("dy=%f", dy); b = new String(b.concat(" "+a));}
		a = String.format("dz=%f", dz); b = new String(b.concat(" "+a));
		b = new String(b.concat("\n"));

		a = String.format("x0=%.2f", x0); b = new String(b.concat(" "+a));
		if(id==3) {a = String.format("y0=%.2f", y0); b = new String(b.concat(" "+a));}
		a = String.format("z0=%.2f", z0); b = new String(b.concat(" "+a));
		b = new String(b.concat("\n"));

		a = String.format("x1=%.2f", x1); b = new String(b.concat(" "+a));
		if(id==3) {a = String.format("y1=%.2f", y1); b = new String(b.concat(" "+a));}
		a = String.format("z1=%.2f", z1); b = new String(b.concat(" "+a));
		b = new String(b.concat("\n"));

		return b;
	}

	public float getExtremeValue(int id, float [] vel)  { 
		float a = 0.0f;

		if(id==1) a = 1.0e20f;
		else a = -1.0e20f;

		int k = 0;
		for(int iz=0; iz<nz; iz++) {
			for(int iy=0; iy<ny; iy++) {
				for(int ix=0; ix<nx; ix++) {
					if(id==1) a = a<vel[k] ? a:vel[k];
					else a = a>vel[k] ? a:vel[k];
					k++;
				}
			}
		}
		return a; 
	}
	public int getExtremeValueIndex(int id, float [] vel)  { 
		float a = 0.0f;
		int index = 0;

		if(id==1) a = 1.0e20f;
		else a = -1.0e20f;

		int k = 0;
		for(int iz=0; iz<nz; iz++) {
			for(int iy=0; iy<ny; iy++) {
				for(int ix=0; ix<nx; ix++) {
					if(id==1) {
						if(a>vel[k]) {
							a = vel[k];
							index = k;
						}
					}
					else {
						if(a<vel[k]) {
							a = vel[k];
							index = k;
						}
					}
					k++;
				}
			}
		}
		return index; 
	}
	
	public float getExtremeValue(int id, float [][][] vel)  { 
		float a = 0.0f;
		float b = 0.0f;

		if(id==1) a = 1.0e20f;
		else a = -1.0e20f;

		for(int iz=0; iz<nz; iz++) {
			for(int iy=0; iy<ny; iy++) {
				for(int ix=0; ix<nx; ix++) {
					b = vel[iz][iy][ix];
					if(id==1) a = a<b ? a:b;
					else a = a>b ? a:b;
				}
			}
		}
		return a; 
	}
	public int [] getExtremeValueIndex(int id, float [][][] vel )  { 
		float a = 0.0f;
		float b = 0.0f;
		int [] index = new int[3];

		if(id==1) a = 1.0e20f;
		else a = -1.0e20f;

		for(int iz=0; iz<nz; iz++) {
			for(int iy=0; iy<ny; iy++) {
				for(int ix=0; ix<nx; ix++) {
					b = vel[iz][iy][ix];
					if(id==1) {
						if(a>b) {
							a = b;
							index[0] = iz; index[1] = iy; index[2] = ix;
						}
					}
					else {
						if(a<b) {
							a = b;
							index[0] = iz; index[1] = iy; index[2] = ix;
						}
					}
				}
			}
		}
		return index; 
	}
	public void setSizeBySize(int nx, int ny, int nz,
			double x0, double y0, double z0, double x1, double y1, double z1) {
		double dx = (x1-x0)/(nx-1);
		double dy = (y1-y0)/(ny-1);
		double dz = (z1-z0)/(nz-1);
		setSize(nx, ny, nz, dx, dy, dz, x0, y0, z0, x1, y1, z1);
	}
	public void setSize(double dx, double dy, double dz, double x0, double y0, double z0,
			double x1, double y1, double z1) {
		int nx = (int) ((x1 - x0) / dx) + 1;
		int ny = (int) ((y1 - y0) / dy) + 1;
		int nz = (int) ((z1 - z0) / dz) + 1;
		setSize(nx, ny, nz, dx, dy, dz, x0, y0, z0);
	}
	public void setSize(int nx, int ny, int nz,
			double dx, double dy, double dz, double x0, double y0, double z0) {
		double x1 = x0 + (nx-1)*dx;
		double y1 = y0 + (ny-1)*dy;
		double z1 = z0 + (nz-1)*dz;
		setSize(nx, ny, nz, dx, dy, dz, x0, y0, z0, x1, y1, z1);
	}
	private void setSize(int nx, int ny, int nz, double dx, double dy, double dz, 
			double x0, double y0, double z0, double x1, double y1, double z1) {
		this.nx = nx;
		this.ny = ny;
		this.nz = nz;

		this.dx = dx;
		this.dy = dy;
		this.dz = dz;

		this.x0 = x0;
		this.y0 = y0;
		this.z0 = z0;

		this.x1 = x1;
		this.y1 = y1;
		this.z1 = z1;
	}
	
	public void smoothVelMatrix (float [][]vel, int nz, int nx, float hsmooth, float vsmooth, int slowness) {
		/*****************************************************************************
		Smooth a matrix of velocities
		 *****************************************************************************
		man_file=vel
		Input:
		vel		Two dimensional matrix of velocities.  z is the fastest dimension.
		nz		vertical dimension of matrix, faster direction
		nx		horizontal dimension of matrix
		hsmooth		width of triangular smooth to apply in the horizontal
				direction  (in number of points)
		vsmooth		height of triangular smooth to apply in the vertical
				direction  (in number of points)
		slowness	TRUE if the velocity should be smoothed in the slowness
				domain, such as if the velocity is interval in depth.

		Output:
		vel		Output matrix
		 *****************************************************************************/
		int i,ix,iz;
		int k1, k2;
		int nsmooth;
		float weight,denom;
		float [][]vtemp = new float[nx][nz];

		nsmooth= (int)Math.abs(hsmooth);
		if ((float)nsmooth==Math.abs(hsmooth))
			nsmooth--;
		if(nsmooth<0)nsmooth=0;

		if (slowness==1){
			for (i = 0; i < nx; i++)
				for (iz = 0; iz < nz; iz++)
					vel[i][iz]= 1.0f/vel[i][iz];
		}

		if (nsmooth>0) {
			//Copy elements from one vector to another
			for (i = 0; i < nx; i++)
				for (iz = 0; iz < nz; iz++)
					vtemp[i][iz]= vel[i][iz];
			for (i = 0; i < nx; i++) {
				denom= 0.0f;
				for (iz = 0; iz < nz; iz++)
					vel[i][iz]=0.0f;
				k1 = (i-nsmooth)>0 ? (i-nsmooth):0; //MAX(0,i-nsmooth);
				k2 = (i+nsmooth+1)<nx ? (i+nsmooth+1):nx; //MIN(nx,i+nsmooth+1)
				for (ix = k1; ix < k2; ix++) {
					weight= (hsmooth-Math.abs(ix-i));
					denom+= weight;
					for (iz = 0; iz < nz; iz++)
						vel[i][iz]+= weight*vtemp[ix][iz];
				}
				denom= 1.0f/denom;
				for (iz = 0; iz < nz; iz++)
					vel[i][iz]*=denom;

			}
		}

		nsmooth= (int)Math.abs(vsmooth);
		if ((float)nsmooth==Math.abs(vsmooth))
			nsmooth--;
		if (nsmooth>1 ) {
			for (i = 0; i < nx; i++)
				for (iz = 0; iz < nz; iz++)
					vtemp[i][iz]= vel[i][iz];
			for (i = 0; i < nz; i++) {
				denom= 0.0f;
				for (ix = 0; ix < nx; ix++)
					vel[ix][i]=0.0f;
				k1 = (i-nsmooth)>0 ? (i-nsmooth):0; //MAX(0,i-nsmooth);
				k2 = (i+nsmooth+1)<nz ? (i+nsmooth+1):nz; //MIN(nx,i+nsmooth+1)
				for (iz = k1; iz < k2; iz++) {
					weight= (vsmooth-Math.abs(iz-i));
					denom+= weight;
					for (ix = 0; ix < nx; ix++)
						vel[ix][i]+= weight*vtemp[ix][iz];
				}
				denom= 1.0f/denom;
				for (ix = 0; ix < nx; ix++)
					vel[ix][i]= (denom * vel[ix][i]);
			}

		}

		if (slowness==1)
			for (ix = 0; ix < nx; ix++)
				for (i = 0; i < nz; i++)
					vel[ix][i]= 1.0f/(vel[ix][i]);
	}
	
	
	
}
