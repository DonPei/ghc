package com.geohammer.core.planarmodel;

import java.util.Arrays;

import com.geohammer.rt.pseudo3d.Almost;

import edu.mines.jtk.dsp.LinearInterpolator;
import edu.mines.jtk.util.ArrayMath;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SingleLayer3D  extends HalfSpaceBox {
	public String 		layerName 	= null;    	// name of layer
	private float [][] 	layerDepth	= null; 	//dp[iy][ix] 

	public SingleLayer3D() { }
	public SingleLayer3D(String layerName, int id, double vp, double vs, double den, 
			double vpGradientX, double vpGradientY, double vpGradientZ, double vsGradientX, double vsGradientY, double vsGradientZ, 
			double delta, double epsilon, double gamma, double qp, double qs, double theta, double phi,
			int nx, int ny, double dx, double dy, double x0, double y0, float [][] dp) {
		this(layerName, id, vp, vs, den, 
				vpGradientX, vpGradientY, vpGradientZ, vsGradientX, vsGradientY, vsGradientZ, 
				delta, epsilon, gamma, qp, qs, theta, phi, 
				0, 0, 0, 0,
				nx, ny, dx, dy, x0, y0, dp);
	}
	public SingleLayer3D(String layerName, int id, double vp, double vs, double den, 
			double vpGradientX, double vpGradientY, double vpGradientZ, double vsGradientX, double vsGradientY, double vsGradientZ, 
			double delta, double epsilon, double gamma, double qp, double qs, double theta, double phi,
			int nx, int ny, int nz, double dx, double dy, double dz, double x0, double y0, double z0, float [][] dp) {
		this(layerName, id, vp, vs, den, 
				vpGradientX, vpGradientY, vpGradientZ, vsGradientX, vsGradientY, vsGradientZ, 
				delta, epsilon, gamma, qp, qs, theta, phi, 
				0.0, 0.0, 0.0, 0.0,
				2, 2, 1, 1, 0, 0, dp);
		setSize(nx, ny, nz, dx, dy, dz, x0, y0, z0);
	}
	public SingleLayer3D(String layerName, int id, double vp, double vs, double den, 
			double vpGradientX, double vpGradientY, double vpGradientZ, double vsGradientX, double vsGradientY, double vsGradientZ,
			double delta, double epsilon, double gamma, double qp, double qs, double theta, double phi, 
			double delta1, double epsilon1, double gamma1, double delta3, 
			int nx, int ny, double dx, double dy, double x0, double y0, float [][] dp) {
		super(id, vp, vs, den, 
				vpGradientX, vpGradientY, vpGradientZ, vsGradientX, vsGradientY, vsGradientZ, 
				delta, epsilon, gamma, qp, qs, theta, phi, 
				delta1, epsilon1, gamma1, delta3, 
				nx, ny, 2, dx, dy, 1, x0, y0, 0);

		this.layerName 	= layerName;
		this.layerDepth = new float[ny][nx];
		if(dp!=null) this.layerDepth = ArrayMath.copy(dp);
	}

	public SingleLayer3D copy() {
		SingleLayer3D target = new SingleLayer3D(layerName, getId(), getVp(), getVs(), getDen(), 
				getVpGradientX(), getVpGradientY(), getVpGradientZ(), getVsGradientX(), getVsGradientY(), getVsGradientZ(),
				getDelta(), getEpsilon(), getGamma(), getQp(), getQs(), getTheta(), getPhi(), 
				getNx(), getNy(), getNz(), getDx(), getDy(), getDz(), getX0(), getY0(), getZ0(), layerDepth);
		return target;
	}
	public void convertUnitSystem(double velScalor, double denScalor) {
		super.convertUnitSystem(velScalor, denScalor);
		if(layerDepth!=null) {
			for(int i=0; i<layerDepth.length; i++) { 
				for(int j=0; j<layerDepth[i].length; j++) layerDepth[i][j] *= velScalor;
			}
		}
	}
	
	public void setBoundingBox() { setZ0(ArrayMath.min(layerDepth)); setZ1(ArrayMath.max(layerDepth)); }
	private float interpolate(double x, double y) {
		int ix = (int)((x-getX0())/getDx());
		if(ix<0) ix=0;
		int ix1 = ix+1;
		if(ix1>=layerDepth[0].length) ix1 = layerDepth[0].length-1;
		int iy = (int)((y-getY0())/getDy());
		if(iy<0) iy=0;
		int iy1 = iy+1;
		if(iy1>=layerDepth.length) iy1 = layerDepth.length-1;
		
		double ax = getX0() + ix*getDx();
		double ay = getY0() + iy*getDy();

		double ux = (x-ax)/getDx();
		double uy = (y-ay)/getDy();

		double y00 = layerDepth[iy][ix];
		double y01 = layerDepth[iy][ix1];
		double y10 = layerDepth[iy1][ix];
		double y11 = layerDepth[iy1][ix1];

		return (float)(y00*(1.0-ux)*(1.0-uy)+y10*(1.0-ux)*(uy)+ y01*(ux)*(1.0-uy)+y11*(ux)*(uy));
	}
	//public Point3D getPoint(int index) { return _p[index]; }
	public Point3D [] genPoints() {
		Point3D [] p = new Point3D[layerDepth.length*layerDepth[0].length]; 
		double x=0, y=0, z=0;
		for(int i=0, k=0; i<layerDepth.length; i++) {
			y = getY0()+i*getDy();
			for(int j=0; j<layerDepth[0].length; j++, k++) {
				x = getX0()+j*getDx();
				p[k] = new Point3D(x,y,layerDepth[i][j]);
			}
		}
		return p;
	}

	public SingleLayer3D update(Point3D [] P) {
		SingleLayer3D target = new SingleLayer3D(layerName, getId(), getVp(), getVs(), getDen(), 
				getVpGradientX(), getVpGradientY(), getVpGradientZ(), getVsGradientX(), getVsGradientY(), getVsGradientZ(), 
				getDelta(), getEpsilon(), getGamma(), getQp(), getQs(), getTheta(), getPhi(),
				getDelta1(), getEpsilon1(), getGamma1(), getDelta3(), 
				getNx(), getNy(), getDx(), getDy(), getX0(), getY0(), 
				new float[][] {{(float)P[0].getD(), (float)P[1].getD()}, {(float)P[2].getD(), (float)P[3].getD()}});
		return target;
	}

	public void flatten() { setDepth(getAverageDepth()); }
	public float getAverageDepth() {
		double sum = 0.0;
		for(int i=0; i<layerDepth.length; i++) {
			for(int j=0; j<layerDepth[0].length; j++) sum += layerDepth[i][j];
		}
		return (float)(sum/(layerDepth.length*layerDepth[0].length));
	}
	public void shift(float dz) {
		for(int i=0; i<layerDepth.length; i++) {
			for(int j=0; j<layerDepth[0].length; j++) layerDepth[i][j] += dz;
		}
	}
	public void shift(double dx, double dy) {
		setSize(getNx(), getNy(), getNz(), getDx(), getDy(), getDz(), getX0()+dx, getY0()+dy, getZ0());
	}
	public void scale(double lScalor, double dScalor) {
		scale(1, null, lScalor);
		scale(2, null, lScalor);
		scale(4, null, dScalor);
	}
	public void setLayerSize(double x0, double x1) { setLayerSize(x0, x1, getY0(), getY1()); }
	public void setLayerSize(double x0, double x1, double y0, double y1) {
		double z0 = getZ0();
		double z1 = getZ1();
		setSize(x1-x0, y1-y0, z1-z0, x0, y0, z0, x1, y1, z1);
	}

	public boolean isFlat() {
		float z = layerDepth[0][0];
		for(int i=0; i<layerDepth.length; i++) {
			for(int j=0; j<layerDepth[0].length; j++) { if(z!=layerDepth[i][j]) return false; }
		}
		return true;
	}

	public double getDepthOnPlane(Point3D A, Point3D B, Point3D C, double x, double y) {
		Point3D norm = getPlaneNorm(A, B, C);

		//plane equation ax+by+cz+d=0
		double d = -(norm.getE()*A.getE()+norm.getN()*A.getN()+norm.getD()*A.getD());

		return -(norm.getE()*x+norm.getN()*y+d)/norm.getD();
	}

	public Point3D getPlaneNorm(Point3D A, Point3D B, Point3D C) {
		Point3D P1 = new Point3D(B.getE()-A.getE(), B.getN()-A.getN(), B.getD()-A.getD());
		Point3D P2 = new Point3D(C.getE()-A.getE(), C.getN()-A.getN(), C.getD()-A.getD());
		return P1.cross(P2);
	}
	public Point3D getPlaneNorm() {
		if(layerDepth.length*layerDepth[0].length>4) return new Point3D(0, 0, 0);
		return getPlaneNorm(new Point3D(getX0(), getY0(), layerDepth[0][0]), 
				new Point3D(getX1(), getY0(), layerDepth[0][1]), 
				new Point3D(getX0(), getY1(), layerDepth[1][0]));
	}
	public float[] makeTriangles() {
		int nx = getNx()-1;
		int ny = getNy()-1;

		float[] xyz = new float[3*6*nx*ny];
		float x0 = 0.0f;
		float x1 = 0.0f;
		float y0 = 0.0f;
		float y1 = 0.0f;

		for (int ix=0,i=0; ix<nx; ++ix) {
			x0 = (float)((ix*getDx())+getX0());
			x1 = (float)(((ix+1)*getDx())+getX0());
			for (int iy=0; iy<ny; ++iy) {
				y0 = (float)((iy*getDy())+getY0());
				y1 = (float)(((iy+1)*getDy())+getY0());
				xyz[i++] = x0;  xyz[i++] = y0;  xyz[i++] = layerDepth[iy  ][ix  ];
				xyz[i++] = x0;  xyz[i++] = y1;  xyz[i++] = layerDepth[iy+1][ix  ];
				xyz[i++] = x1;  xyz[i++] = y0;  xyz[i++] = layerDepth[iy  ][ix+1];
				xyz[i++] = x1;  xyz[i++] = y0;  xyz[i++] = layerDepth[iy  ][ix+1];
				xyz[i++] = x0;  xyz[i++] = y1;  xyz[i++] = layerDepth[iy+1][ix  ];
				xyz[i++] = x1;  xyz[i++] = y1;  xyz[i++] = layerDepth[iy+1][ix+1];
			}
		}

		return xyz;
	}

	public float[] makeQuads(float scalor) {
		int nx = getNx()-1;
		int ny = getNy()-1;

		float[] xyz = new float[3*4*nx*ny];
		float x0 = 0.0f;
		float x1 = 0.0f;
		float y0 = 0.0f;
		float y1 = 0.0f;


		for (int ix=0,i=0; ix<nx; ++ix) {
			x0 = (float)((ix*getDx())+getX0());
			x1 = (float)(((ix+1)*getDx())+getX0());
			for (int iy=0; iy<ny; ++iy) {
				y0 = (float)((iy*getDy())+getY0());
				y1 = (float)(((iy+1)*getDy())+getY0());
				xyz[i++] = x0;  xyz[i++] = y0;  xyz[i++] = scalor*layerDepth[iy][ix];
				xyz[i++] = x0;  xyz[i++] = y1;  xyz[i++] = scalor*layerDepth[iy+1][ix];
				xyz[i++] = x1;  xyz[i++] = y1;  xyz[i++] = scalor*layerDepth[iy+1][ix+1];
				xyz[i++] = x1;  xyz[i++] = y0;  xyz[i++] = scalor*layerDepth[iy][ix+1];
			}
		}

		return xyz;
	}

	public double[] intersection(double px0, double py0, double px1, double py1) {
		double diffX = px1-px0;
		double diffY = py1-py0;
		if(Almost.DOUBLE.zero(diffX)) return new double[] { px0, getY0(), px0, getY1()};
		if(Almost.DOUBLE.zero(diffY)) return new double[] { getX0(), py0, getX1(), py0};

		double x = 0.0;
		double y = 0.0;
		double [] q = new double[4];
		int k=0;
		//left line
		x = getX0();
		y = py0+(diffY/diffX)*(x-px0);
		if((Almost.DOUBLE.cmp(y, getY0())==1&&Almost.DOUBLE.cmp(y, getY1())==-1) ||
				Almost.DOUBLE.cmp(y, getY0())==0||Almost.DOUBLE.cmp(y, getY1())==0) {
			q[k++] = x; q[k++] = y; 
		}
		//right line
		x = getX1();
		y = py0+(diffY/diffX)*(x-px0);
		if((Almost.DOUBLE.cmp(y, getY0())==1&&Almost.DOUBLE.cmp(y, getY1())==-1) ||
				Almost.DOUBLE.cmp(y, getY0())==0||Almost.DOUBLE.cmp(y, getY1())==0) {
			q[k++] = x; q[k++] = y; 
		}
		//top
		y = getY0();
		x = px0+(diffX/diffY)*(y-py0);
		if((Almost.DOUBLE.cmp(x, getX0())==1&&Almost.DOUBLE.cmp(x, getX1())==-1) ||
				Almost.DOUBLE.cmp(x, getX0())==0||Almost.DOUBLE.cmp(x, getX1())==0) {
			if(k<3) { q[k++] = x; q[k++] = y; }
		}
		//bottom
		y = getY1();
		x = px0+(diffX/diffY)*(y-py0);
		if((Almost.DOUBLE.cmp(x, getX0())==1&&Almost.DOUBLE.cmp(x, getX1())==-1) ||
				Almost.DOUBLE.cmp(x, getX0())==0||Almost.DOUBLE.cmp(x, getX1())==0) {
			if(k<3) { q[k++] = x; q[k++] = y; }
		}
		return q;
	}

	public void regrid(int nx, int ny, double dx, double dy, double x0, double y0) {
		float [][] dp = null;
		if(isFlat()) {
			float a = this.layerDepth[0][0];
			dp = new float[ny][nx]; 
			for(int i=0; i<ny; i++) {
				for(int j=0; j<nx; j++) dp[i][j] = a;
			}
		} else {
			dp = regridInternal(nx, ny, dx, dy, x0, y0);
		}
		setSize(nx, ny, 1, dx, dy, 1, x0, y0, 0);
		this.layerDepth = dp;
	}
	//children could be larger than parents
	public float [][] regridInternal(int nx, int ny, double dx, double dy, double x0, double y0) {
		LinearInterpolator li = new LinearInterpolator();
		li.setExtrapolation(LinearInterpolator.Extrapolation.CONSTANT);
		li.setUniform(getNx(), getDx(), getX0(), getNy(), getDy(), getY0(), layerDepth);
		//li.setUniform(layerDepth[0].length, getDx(), getX0(), layerDepth.length, getDy(), getY0(), layerDepth);

		float [][] dp = new float[ny][nx];

		double ax = 0.0;
		double ay = 0.0;
		for(int iy=0; iy<ny; iy++) {
			ay = y0+iy*dy;
			for(int ix=0; ix<nx; ix++) {
				ax = x0+ix*dx;
				dp[iy][ix] = li.interpolate(ax, ay);
			}
		}
		return dp;
	}
	public void setDepth(SingleLayer3D source1, SingleLayer3D source2, double ratio) {
		for(int i=0; i<layerDepth.length; i++) {
			for(int j=0; j<layerDepth[0].length; j++) { 
				layerDepth[i][j] = (float)(source1.layerDepth[i][j]+ratio*(source2.layerDepth[i][j]-source1.layerDepth[i][j])); 
			}
		}
	}

	//public double getZ0() 		{ return getExtremeDepth("min")[1];  }
	//public double getZ1() 		{ return getExtremeDepth("max")[1];  }
	//public int getZ0Index() 	{ return (int)getExtremeDepth("min")[0];  }
	//public int getZ1Index() 	{ return (int)getExtremeDepth("max")[0];  }
	public double [] getExtremeDepth(String method) {
		double extrem[] = new double[2];
		int nxy = getNx()*getNy();
		float min = 1.0e10f;
		int k = 0;
		if(method.equalsIgnoreCase("min")) {
			for(int i=0; i<layerDepth.length; i++) {
				for(int j=0; j<layerDepth[0].length; j++) { 
					if(layerDepth[i][j]<min) {
						min = layerDepth[i][j];
						k = i; 
					} 
				}
			}
		}
		if(method.equalsIgnoreCase("max")) {
			min = -min;
			for(int i=0; i<layerDepth.length; i++) {
				for(int j=0; j<layerDepth[0].length; j++) { 
					if(layerDepth[i][j]>min) {
						min = layerDepth[i][j];
						k = i; 
					}
				}
			}
		}

		extrem[0] = k;
		extrem[1] = min;
		return extrem;
	}

	public double getVpToVs() 						{ if(getVs()!=0.0) return getVp()/getVs(); else return 0.0;}
	public float getDepth(int iy, int ix) 			{ return layerDepth[iy][ix]; }
	public double getDepth(double x, double y) 		{ return interpolate(x, y); }

	public void setVpToVs(double VpToVs) 			{ if(VpToVs!=0.0) setVs(getVp()/VpToVs); else setVs(0.0);}
	public void setDepth(int iy, int ix, float v) 	{ layerDepth[iy][ix] = v; }
	public void setDepth(float av) 					{ 
		for(int i=0; i<layerDepth.length; i++) {
			for(int j=0; j<layerDepth[0].length; j++) layerDepth[i][j] = av;
		}
	}

	public double getVel(int iy, int ix, double z) {
		double az = getDepth(iy, ix);
		if(z==az) {
			return getVp();
		} else if(z>az) {
			if(getVpGradientZ()==0.0) {
				return getVp();
			} else {
				return getVpGradientZ()*(z-az)+getVp();
			}
		}

		return 0.0;
	}

	private double interp2D(double x, double y) {
		int ix = (int)((x-getX0())/getDx());
		int iy = (int)((y-getY0())/getDy());

		double ax = getX0() + ix*getDx();
		double ay = getY0() + iy*getDy();

		double ux = (x-ax)/getDx();
		double uy = (y-ay)/getDy();

		double y00 = (double)getDepth(ix, iy);
		double y01 = (double)getDepth(ix+1, iy);
		double y10 = (double)getDepth(ix, iy+1);
		double y11 = (double)getDepth(ix+1, iy+1);
		//System.out.print(y00 + " "+ y01 + " "+ y10 + " "+y11 + " "+ x + " "+y + " " +ux + " "+ uy + "; ");
		//System.out.println(toString(1));
		//System.exit(1);
		return y00*(1.0-ux)*(1.0-uy)+y10*(1.0-ux)*(uy)+ y01*(ux)*(1.0-uy)+y11*(ux)*(uy);
	}

	public void interpolate(int nx, double dx, double x0, int ny, double dy, double y0, float[][] fu){
		LinearInterpolator li = new LinearInterpolator();
		li.setExtrapolation(LinearInterpolator.Extrapolation.CONSTANT);
		li.setUniform(nx, dx, x0, ny, dy, y0, fu);

		int k = 0;
		double ax = 0.0;
		double ay = 0.0;
		for(int iy=0; iy<getNy(); iy++) {
			ay = getY0()+iy*getDy();
			for(int ix=0; ix<getNx(); ix++) {
				ax = getX0()+ix*getDx();
				layerDepth[iy][ix] = li.interpolate(ax, ay);
				k++;
			}
		}
	}

	public float [][] cut(int ix1, int ix2, int iy1, int iy2) {
		int mnx = ix2-ix1+1;
		int mny = iy2-iy1+1;
		float [][] v = new float[mny][mnx];
	
		for(int i=iy1, ik=0; i<=iy2; i++, ik++) {
			for(int j=ix1, jk=0; j<=ix2; j++, jk++) {
				v[ik][jk] = layerDepth[i][j];
			}
		}
		
		layerDepth = v;
		
		double x0 = getX0()+ix1*getDx();
		double y0 = getY0()+iy1*getDy();
		setSize(mnx, mny, getNz(), getDx(), getDy(), getDz(), x0, y0, getZ0());
		setBoundingBox();
		return v;
	}
	
	public String toString(int id) {
		if(id==1) {
			return String.format("nx=%d ny=%d nz=%d dx=%f dy=%f dz=%f x0=%f y0=%f z0=%f x1=%f y1=%f z1=%f",
					getNx(), getNy(), getNz(), getDx(), getDy(), getDz(), getX0(), getY0(), getZ0(), getX1(), getY1(), getZ1());
		}
		double x1 = getX0()+(getNx()-1)*getDx();
		double y1 = getY0()+(getNy()-1)*getDy();
		String b = new String(layerName); 
		String a = new String("");
		a = String.format("%f %f %f %f", getVp(), getVpGradientX(), getVpGradientY(), getVpGradientZ()); 
		b = new String(b.concat("\n"+a));
		a = String.format("%f %f %f %f", getVs(), getVsGradientX(), getVsGradientY(), getVsGradientZ()); 
		b = new String(b.concat("\n"+a));
		a = String.format("%f", getDen()); 
		b = new String(b.concat("\n"+a));

		a = String.format("%f %f %f %f %f %f %f", getEpsilon(), getDelta(), getGamma(), getQp(), getQs(), getTheta(), getPhi());
		b = new String(b.concat("\n"+a));
		a = String.format("%d %d %f %f %f %f", getNx(), getNy(), getX0(), getY0(), getDx(), getDy());
		b = new String(b.concat("\n"+a));
		if(id==2) {
			a = String.format("minZ=%f, maxZ=%f", getExtremeDepth("min")[1], getExtremeDepth("max")[1]);
			b = new String(b.concat("\n"+a));
		}
		if(id==3) {
		}
		if(id==4) {
			b = new String(b.concat("\n"+Arrays.toString(layerDepth)));
		}

		return b;
	}
	public void printDepth() {
		int j = layerDepth[0].length;
		for(int i=0; i<layerDepth.length; i++) System.out.println(i+" "+j+" "+ Arrays.toString(layerDepth[i]));
	}
	public String toDeepString() { return toDeepString(2); }
	public String toDeepString(int id) {
		double x1 = getX0()+(getNx()-1)*getDx();
		double y1 = getY0()+(getNy()-1)*getDy();
		String b = new String(""); 
		String a = new String("");
		a = String.format("layerName=%s, vp=%f", layerName, getVp()); 
		b = new String(b.concat("\n"+a));
		a = String.format("vpGradientX=%f, vpGradientY=%f, vpGradientZ=%f", getVpGradientX(), getVpGradientY(), getVpGradientZ());
		b = new String(b.concat("\n"+a));
		a = String.format("vs=%f, den=%f", getVs(), getDen()); 
		b = new String(b.concat("\n"+a));
		a = String.format("vsGradientX=%f, vsGradientY=%f, vsGradientZ=%f", getVsGradientX(), getVsGradientY(), getVsGradientZ());
		b = new String(b.concat("\n"+a));

		a = String.format("epsilon=%f, delta=%f, gamma=%f, qp=%f, qs=%f, theta=%f, phi=%f", 
				getEpsilon(), getDelta(), getGamma(), getQp(), getQs(), getTheta(), getPhi());
		b = new String(b.concat("\n"+a));
		a = String.format("nx=%d, ny=%d, nxy=%d, x0=%f, y0=%f, x1=%f, y1=%f, dx=%f, dy=%f", 
				getNx(), getNy(), getNx()*getNy(), getX0(), getY0(), x1, y1, getDx(), getDy());
		b = new String(b.concat("\n"+a));
		if(id==2) {
			a = String.format("minZ=%f, maxZ=%f", getExtremeDepth("min")[1], getExtremeDepth("max")[1]);
			b = new String(b.concat("\n"+a));
		}
		if(id==3) {
		}

		return b;
	}

}

