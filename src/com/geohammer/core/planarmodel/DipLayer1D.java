package com.geohammer.core.planarmodel;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.JOptionPane;

import com.geohammer.core.MatrixFileReader;

import edu.mines.jtk.util.ArrayMath;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DipLayer1D extends FlatLayer1D {
	private double anchorE		= 0.0; 
	private double anchorN		= 0.0; 
	private double anchorD		= 0.0; 
	private double eps 		= 0.001*Math.PI/180.0;
	//	private double _strike 		= 0.0; 	
	//	// Fault strike is the direction of a line created by the intersection of a fault plane and 
	//	// a horizontal surface, 0 to 360 degree, relative to North. Strike is always defined such that 
	//	// a fault dips to the right side of the trace when moving along the trace in the strike direction. 
	//	// The hanging-wall block of a fault is therefore always to the right, and the footwall block on the left. 
	//	// This is important because rake (which gives the slip direction) is defined as the movement of 
	//	// the hanging wall relative to the footwall block.
	//
	//	private double dip 		= 0.0;	
	//	// Fault dip is the angle between the fault and a horizontal plane, 0锟� to 90锟�.
	//	//http://www.opensha.org/glossary-strikeDipRake
	//
	//	private double _aStrike 		= 0.0;  //from obs
	//	private double _aDip 			= 0.0;	//appearent dip angle from obs

	//Positive dipAngle means dip downward along dipAzimuth from Timur
	private double dip 			= 0.0;  //from obs in degree
	private double azimuth 		= -1.0;	//in degree
	private double dipAnchor 	= 0.0;	//not used

	public DipLayer1D(int nBoundaries) {
		super(nBoundaries);
	}

	public DipLayer1D(String selectedFileName)	{
		read(selectedFileName);
	}
	public DipLayer1D(String [] layerNames, double [] top, double [] vp, double [] vs, double [] den, 
			double [] delta, double [] epsilon, double [] gamma, 
			double [] qp, double [] qs, double [] theta, double [] phi, double azimuth, double dip) {
		super(layerNames, top, vp, vs, den, 
				delta, epsilon, gamma, 
				qp, qs, theta, phi);
		this.azimuth = azimuth;
		this.dip 	= dip;
	}

	public void update(FlatLayer1D layer1D)	{
		for(int i=0; i<layer1D.getLayer().size(); i++) {  
			layerList.get(i).setVp(layer1D.getLayer(i).getVp());
			layerList.get(i).setVs(layer1D.getLayer(i).getVs());
			layerList.get(i).setDen(layer1D.getLayer(i).getDen());
			layerList.get(i).setDelta(layer1D.getLayer(i).getDelta());
			layerList.get(i).setEpsilon(layer1D.getLayer(i).getEpsilon());
			layerList.get(i).setGamma(layer1D.getLayer(i).getGamma());
		}
	}

	public DipLayer1D copy() { 
		DipLayer1D other = new DipLayer1D(layerList.size());
		for(int i=0; i<layerList.size(); i++) {  other.add(layerList.get(i).copy()); }
		other.setIUnit(getIUnit());
		other.azimuth 		= azimuth; 
		other.dip	 		= dip; 
		other.dipAnchor 	= dipAnchor;
		other.anchorE		= anchorE; 
		other.anchorN		= anchorN; 
		other.anchorD		= anchorD;

		return other; 
	}
	public DipLayer1D genAverageModel() { 
		//int nLayer = layerList.size();
		int [] iDepth = new int[]{0, layerList.size()-1};
		int nLayer = iDepth.length;
		DipLayer1D other = new DipLayer1D(nLayer);
		for(int i=0; i<nLayer; i++) {  other.add(layerList.get(iDepth[i]).copy()); }
		other.setIUnit(getIUnit());
		other.azimuth 		= azimuth; 
		other.dip	 		= dip; 
		other.dipAnchor 	= dipAnchor;
		other.anchorE		= anchorE; 
		other.anchorN		= anchorN; 
		other.anchorD		= anchorD; 

		int [] iProperty = new int[]{1, 2, 4, 20, 21, 22, 23, 24};
		for(int i=0; i<nLayer; i++) {
			for(int j=0; j<iProperty.length; j++) {
				double v = getLayerPropertyMean(iProperty[j]);
				other.getLayer(i).setLayerProperty(iProperty[j], v);
			}
		}

		return other; 
	}

	public void ftToMeter() {  
		super.ftToMeter(); 
		double velScalor = 0.3048;
		anchorE		*= velScalor; 
		anchorN		*= velScalor; 
		anchorD		*= velScalor;  
	}
	public void meterToFt() { 
		super.meterToFt(); 
		double velScalor = 3.28084;
		anchorE		*= velScalor; 
		anchorN		*= velScalor; 
		anchorD		*= velScalor; 
	}
	public void convertUnitSystem(double velScalor, double denScalor) {
		super.convertUnitSystem(velScalor, denScalor);
		anchorE		*= velScalor; 
		anchorN		*= velScalor; 
		anchorD		*= velScalor; 
	}
	
	public boolean isFlat() 					{ return -eps<dip&&dip<eps; }

	public double [] calPlaneNorm() { 
		double r = Math.sin(dip*Math.PI/180.0);
		double z = Math.cos(dip*Math.PI/180.0);

		double theSin = Math.sin(azimuth*Math.PI/180.0+Math.PI);
		double theCos = Math.cos(azimuth*Math.PI/180.0+Math.PI);

		double y = r*theSin;
		double x = r*theCos;

		return new double[]{x, y, z};
	}

	//nx0, ey0, dz0 is in flat coordinate system
	//azimuth and dip define the tilt coordinate system
	//Positive dipAngle means dip downward along dipAzimuth.
	//public void fromFlatToTilt(double nx0, double ey0, double dz0, double azimuth, double dip) {
	public void fromFlatToTilt(double [] px, double [] py, double [] pz) {
		DipLayer1D.rotate2D(px, py, azimuth);
		//System.out.println("pN1="+px[0]+" pE="+py[0]+" pD="+pz[0]);	

		DipLayer1D.rotate2D(pz, px, -dip);	
		//System.out.println("pN2="+px[0]+" pE="+py[0]+" pD="+pz[0]);
	}
	public void fromTiltToFlat(double [] px, double [] py, double [] pz) {
		DipLayer1D.rotate2D(pz, px, dip);
		//System.out.println("pN3="+px[0]+" pE="+py[0]+" pD="+pz[0]);

		DipLayer1D.rotate2D(px, py, -azimuth);
		//System.out.println("pN4="+px[0]+" pE="+py[0]+" pD="+pz[0]);	
	}

	public static void rotate2D(double [] pN, double [] pE, double azimuth) {
		double theSin = Math.sin(azimuth);
		double theCos = Math.cos(azimuth);
		double n = 0;
		double e = 0;

		for(int i=0; i<pN.length; i++) {
			n = pN[i]*theCos+pE[i]*theSin;
			e = -pN[i]*theSin+pE[i]*theCos;
			//System.out.println(i+" pN="+pN[i]+" pE="+pE[i]+" n="+n+" e="+e);
			pN[i] = n;
			pE[i] = e;
		}
	}

	//input will been modified
	//http://paulbourke.net/geometry/pointlineplane/
	public void shortestDistance(Point3D norm, double [] pN, double [] pE, double [] pD) {
		double A = norm.getE();
		double B = norm.getN();
		double C = norm.getD();
		double d = Math.sqrt(A*A+B*B+C*C);

		for(int i=1; i<pD.length; i++) {
			pD[i] = Math.abs((A*pN[i]+B*pE[i]+C*pD[i])/d);
		}
	}
	public double shortestDistance(Point3D norm, double pN, double pE, double pD) {
		double A = norm.getE();
		double B = norm.getN();
		double C = norm.getD();
		double d = Math.sqrt(A*A+B*B+C*C);

		return Math.abs((A*pN+B*pE+C*pD)/d);
	}

	//	public void shiftDepth(int idir, double [] z) {
	//		double datum = layerList.get(0).getZ0();
	//		for(int i=0; i<z.length; i++) z[i] += idir*datum;
	//	}
	public FlatLayer1D toFlatLayer1D(double datum) {
		//double datum = layerList.get(0).getZ0();
		updateDepth();
		updateH();

		ArrayList<SingleFlatLayer> layers = getLayer();
		FlatLayer1D other = new FlatLayer1D(layers.size());

//		if(isFlat()) {
			for(int i=0; i<layers.size(); i++) {  
				SingleFlatLayer sl = layers.get(i).copy();
				sl.setTopDepth(sl.getTopDepth()+datum);
				other.add(sl); 
			}
//		} else {	
//			double bot = 0;
//			for(int i=0; i<layers.size(); i++) {  
//				SingleFlatLayer layer = layers.get(i).copy();
//				layer.updateH();
//				double thickness = Math.abs(layer.getThickness()*Math.cos(dip));
//				if(i==0) layer.setTopDepth(0);
//				else layer.setTopDepth(bot);
//				bot = layer.getTopDepth()+thickness;
//				layer.setBottomDepth(bot);
//				layer.updateH();
//				other.add(layer);
//			}
//		}

		other.updateDepth();
		SingleFlatLayer lastLayer = other.getLayer(layers.size()-1);
		lastLayer.setBottomDepth(lastLayer.getTopDepth());
		other.updateH();

		return other;
	}

	public boolean equal(double a, double b) 	{ return Math.abs(a-b)<=eps; }  //a==b
	public boolean greater(double a, double b) 	{ return (a-b)>eps; } //a>b
	public boolean lesser(double a, double b) 	{ return (a-b)<eps; } //a<b	

	public void rotate(double azimuth, double [] e, double [] n) {
		double ang = azimuth*Math.PI/180.0;
		double theSin = Math.sin(ang);
		double theCos = Math.cos(ang);
		double[][] M = new double[][] {{theCos, -theSin},{theSin, theCos}}; 
		double ax = 0.0;
		double ay = 0.0;
		for(int i=0, k=0; i<e.length; i++) {
			ax = M[0][0]*e[i]+M[0][1]*n[i]; //xprime
			ay = M[1][0]*e[i]+M[1][1]*n[i];
			e[i] = ax;
			n[i] = ay;
		}
	}
	public Layer2D toLayer2D() {
		double x0 = layerList.get(0).getX0();
		double x1 = layerList.get(0).getX1();
		return toLayer2D(x0, x1);
	}
	//X is in Easting
	public Layer2D toLayer2D(double x0, double x1) {
		double z0 = layerList.get(0).getTopDepth();
		double z1 = layerList.get(layerList.size()-1).getTopDepth();
		double y0 = layerList.get(0).getY0();
		double y1 = layerList.get(0).getY1();
		return toLayer2D(2, 2, x1-x0, y1-y0, x0, y0);
	}
	public Layer2D toLayer2D(int nx, int ny, double dx, double dy, double x0, double y0) {
		Layer2D layer2D = new Layer2D(layerList.size());
		layer2D.setAzimuth(azimuth);
		layer2D.setDip(dip);
		layer2D.setIUnit(iUnit);

		double x1 = x0+(nx-1)*dx;
		//double y1 = y0+(ny-1)*dy;
		//double dh = (x1-x0)*Math.tan(dip*Math.PI/180.0);		

		//System.out.println("azimuth="+azimuth+" dip="+dip+" dh="+dh+" x0="+x0+" x1="+x1);

		double [] topDepth = new double[layerList.size()];
		double [] botDepth = new double[layerList.size()];
		
		for(int i=0; i<layerList.size(); i++) {
			SingleFlatLayer sl = layerList.get(i);
			double z = sl.getTopDepth();
			topDepth[i] = z;
			//System.out.println("i="+i+" z0="+z+" z1="+(z+dh));
			if(i>0) botDepth[i] = topDepth[i-1];
			double [] Ye = new double[] { x0, x1};
			double [] Xd = new double[] { z, z};
			if(i==0 || i==layerList.size()-1) {
			} else {
				rotate(dip, Ye, Xd);
			}
			ArrayList<Point2D> 	mp = new ArrayList<Point2D>(2);
			mp.add(new Point2D(Ye[0], Xd[0]));
			mp.add(new Point2D(Ye[1], Xd[1]));
			
			String name = layerList.get(i).getLayerName();
			if(name==null) name = "layer_"+(i+1);
			SingleLayer2D singleLayer = new SingleLayer2D(name, sl.getId(), 
					sl.getVp(), sl.getVs(), sl.getDen(), 
					0, 0, 0, 0, 0, 0,  
					sl.getDelta(), sl.getEpsilon(), sl.getGamma(), 
					sl.getQp(), sl.getQs(), sl.getTheta(), sl.getPhi(),
					2, 2, x1-x0, 1, x0, y0, null, mp, null);

			layer2D.getLayer().add(singleLayer);
		}
		botDepth[layerList.size()-1] = topDepth[layerList.size()-1];
	
		double z0 = topDepth[0];
		double z1 = topDepth[layerList.size()-1];
		layer2D.setTopDepth(topDepth);
		layer2D.setBotDepth(botDepth);
		
		return layer2D;
	}
	public Layer3D toLayer3D() {
		double x0 = layerList.get(0).getX0();
		double x1 = layerList.get(0).getX1();
		double y0 = layerList.get(0).getY0();
		double y1 = layerList.get(0).getY1();
		return toLayer3D(x0, x1, y0, y1);
		//return toLayer3D(ny, nx, dy, dx, y0, x0);
	}
	public Layer3D toLayer3D(double x0, double x1, double y0, double y1) {
		double z0 = layerList.get(0).getTopDepth();
		double z1 = layerList.get(layerList.size()-1).getTopDepth();
		return toLayer3D(2, 2, x1-x0, y1-y0, x0, y0);
	}
	public Layer3D toLayer3D(int nx, int ny, double dx, double dy, double x0, double y0) {
		//A plane can also be represented by the equation A x + B y + C z + D = 0
		//where The normal to the plane is the vector (A,B,C).
		double [] norm = calPlaneNorm();
		double A = norm[0];
		double B = norm[1];
		double C = norm[2];

		double D = 0;
		//System.out.println("A="+A+" B="+B+" C="+C);
		double x1 = x0+(nx-1)*dx;
		double y1 = y0+(ny-1)*dy;

		double z0 = 0.0;
		double z1 = 0.0;
		double dz = 0.0;
		int nz = 2;

		Layer3D layer3D = new Layer3D(layerList.size());
		layer3D.setAzimuth(azimuth);
		layer3D.setDip(dip);

		double [] topDepth = new double[layerList.size()];
		double [] botDepth = new double[layerList.size()];
		for(int i=0; i<layerList.size(); i++) {
			float [][] 	dp 		= new float[2][2]; 	//dp[iy][ix]
			topDepth[i] = layerList.get(i).getTopDepth();
			if(i==0||i==layerList.size()-1||isFlat()) {
				dp[0][0] = (float)topDepth[i]; 
				dp[0][1] = (float)topDepth[i];
				dp[1][0] = (float)topDepth[i]; 
				dp[1][1] = (float)topDepth[i];
			} else {
				D = -(A*0+B*0+C*topDepth[i]);

				dp[0][0] = (float)(-(A*x0+B*y0+D)/C); 
				//System.out.println(x0+" "+y0+" "+dp[0][0]);
				dp[0][1] = (float)(-(A*x1+B*y0+D)/C); 
				//System.out.println(x1+" "+y0+" "+dp[1][0]);
				dp[1][0] = (float)(-(A*x0+B*y1+D)/C);
				//System.out.println(x0+" "+y1+" "+dp[0][1]);
				dp[1][1] = (float)(-(A*x1+B*y1+D)/C); 
				//System.out.println(x1+" "+y1+" "+dp[1][1]);
			}

			z0 = ArrayMath.min(dp);
			z1 = ArrayMath.max(dp);
			dz = (z1-z0)/(nz-1);
			SingleFlatLayer sl = layerList.get(i);
			String name = sl.getLayerName();
			if(name==null) name = "layer_"+(i+1);
			SingleLayer3D singleLayer = new SingleLayer3D(name, i, 
					sl.getVp(), sl.getVs(), sl.getDen(), 
					0, 0, 0, 0, 0, 0,  
					sl.getDelta(), sl.getEpsilon(), sl.getGamma(), 
					sl.getQp(), sl.getQs(), sl.getTheta(), sl.getPhi(),
					nx, ny, nz, dx, dy, dz, x0, y0, z0, dp);		

			layer3D.getLayer().add(singleLayer);
			if(i>0) botDepth[i] = topDepth[i-1];
		}
		botDepth[layerList.size()-1] = topDepth[layerList.size()-1];

		layer3D.setTopDepth(ArrayMath.copy(topDepth));
		layer3D.setBotDepth(ArrayMath.copy(botDepth));
		layer3D.setBoundingBox();
		return layer3D;
	}
	
	public Layer2D toLayer2D1(int nx, int ny, double dx, double dy, double x0, double y0) {
		Layer3D layer3D = toLayer3D(nx, ny, dx, dy, x0, y0);
		Layer2D layer2D = layer3D.toLayer2D(x0, y0, layer3D.getX1(), layer3D.getY0());
		//System.out.println(layer2D.toString(1));
		return layer2D;
	}

	public String toSeisPTString() {
		int nBoundaries = getNumOfBoundaries();

		String separator = System.getProperty( "line.separator" );
		StringBuilder lines = new StringBuilder("Northing0, Easting0, TVD0, Northing1, Easting1, TVD1, "
				+ "#OfLayers, GridSize, DipAngle(0-90), Azimuth(0-360)");
		lines.append( separator );

		SingleFlatLayer layer = layerList.get(0);
		String a = String.format("%8.2f, %8.2f, %8.2f, %8.2f, %8.2f, %8.2f, %5d, %8.2f, %8.2f, %8.2f", 
				layer.getX0(), layer.getY0(), layer.getZ0(), layer.getX1(), layer.getY1(), layer.getZ1(), 
				nBoundaries, layer.getDx(), dip*180.0/Math.PI, azimuth*180.0/Math.PI);
		lines.append(a);
		lines.append( separator );

		a = "Layer#, Depth, Vp, Vs, Density, Delta, Epsilon, Gamma, Qp, Qs";
		lines.append(a);
		lines.append( separator );
		for(int i=0; i<nBoundaries; i++) {
			layer = layerList.get(i);
			a = String.format("%3d, %8.2f, %8.2f, %8.2f, %5.3f, %5.3f, %5.3f, %5.3f, %12.1f, %12.1f", 
					(i+1), layer.getTopDepth(), layer.getVp(), layer.getVs(),	layer.getDen(), 
					layer.getDelta(), layer.getEpsilon(), layer.getGamma(), layer.getQp(), layer.getQs());

			lines.append(a);
			if(i<nBoundaries-1) lines.append( separator );
		}
		return lines.toString( );
	}

	public String toString() {
		int nBoundaries = getNumOfBoundaries();
		String separator = System.getProperty( "line.separator" );
		StringBuilder lines = new StringBuilder("AnchorX/Azimuth, AnchorY/Dip, AnchorZ/None, Vp/X0(E), Vs/Nx, Density/Dx, Delta/Y0(N), Epsilon/Ny, Gamma/Dy, Qp/Nz, Qs/unit_1_m_2_ft");
		lines.append( separator );
		int k = 0;
		SingleFlatLayer layer = layerList.get(k);
		String a =azimuth+ ", "+dip+ ", 0, "+layer.getX0()+", "+layer.getNx()+", "+ layer.getDx()+ ", "+
				layer.getY0()+", "+layer.getNy()+", "+ layer.getDy()+ ", "+layer.getNz()+ ", "+getIUnit(); 
		lines.append(a);
		lines.append( separator );

		for(int i=0; i<nBoundaries; i++) {
			layer = layerList.get(i);
			a ="0, 0, "+layer.getTopDepth()+ ", "+layer.getVp()+ ", "+layer.getVs()+ ", "+layer.getDen()+ ", "+
					layer.getDelta()+ ", "+layer.getEpsilon()+ ", "+layer.getGamma()+ ", "+
					layer.getQp()+ ", "+layer.getQs(); 
			lines.append(a);
			if(i<nBoundaries-1) lines.append( separator );
		}
		return lines.toString( );
	}

	public String toEffectString() {
		int nBoundaries = 1;
		String separator = System.getProperty( "line.separator" );
		StringBuilder lines = new StringBuilder("AnchorX/Azimuth, AnchorY/Dip, AnchorZ/None, Vp/X0, Vs/Nx, Density/Dx, Delta/Y0, Epsilon/Ny, Gamma/Dy, Qp/Nz, Qs/unit_1_m_2_ft");
		lines.append( separator );
		int k = 0;
		SingleFlatLayer layer = layerList.get(k);
		String a =azimuth*180.0/Math.PI+ ", "+dip*180.0/Math.PI+ ", 0, "+layer.getX0()+", "+layer.getNx()+", "+ layer.getDx()+ ", "+
				layer.getY0()+", "+layer.getNy()+", "+ layer.getDy()+ ", "+layer.getNz()+ ", "+getIUnit(); 
		lines.append(a);
		lines.append( separator );

		for(int i=0; i<nBoundaries; i++) {
			layer = layerList.get(i);
			a ="0, 0, "+layer.getTopDepth()+ ", "+layer.getVp()+ ", "+layer.getVs()+ ", "+layer.getDen()+ ", "+
					layer.getDelta()+ ", "+layer.getEpsilon()+ ", "+layer.getGamma()+ ", "+
					layer.getQp()+ ", "+layer.getQs(); 
			lines.append(a);
			if(i<nBoundaries-1) lines.append( separator );
		}
		return lines.toString( );
	}

	public Grid3D toGrid3D(int iProperty) {
		SingleFlatLayer layer = layerList.get(0);
		double 	e0 	= layer.getX0();
		int 	ne 	= layer.getNx();
		double 	de 	= layer.getDx();

		double 	n0 	= layer.getY0();
		int 	nn 	= layer.getNy();
		//double 	dn 	= layer.getDy();
		double 	dn 	= layer.getDx();

		double 	z0 	= layer.getTopDepth();
		double 	dz 	= layer.getDx();
		//int 	nz 	= layer.getNz();
		double 	z1 	= layerList.get(layerList.size()-1).getTopDepth();
		//double 	dz 	= (z1-z0)/(nz-1);
		int 	nz 	= (int)((z1-z0)/dz)+1;

		return toGrid3D(iProperty, nn, ne, nz, dn, de, dz, n0, e0, z0);
	}
	public Grid3D toGrid3D(int iProperty, int nx, int ny, int nz, double dx, double dy, double dz, 
			double x0, double y0, double z0) {
		float [][][] data = new float[nx][][];
		float [][] v = toGrid2D(iProperty, ny, nz, dy, dz, y0, z0);
		for(int i=0; i<nx; i++)  data[i] = ArrayMath.copy(v); 

		Grid3D grid3D = new Grid3D(getIUnit(), nx, ny, nz, dx, dy, dz, x0, y0, z0, data);
		return grid3D;
	}

	public float [][] toGrid2D(int iProperty, int nx, int nz, double dx, double dz, double x0, double z0) {
		float [][] data = new float [nx][nz];		
		double theTan = dx*Math.tan(dip);
		float a 	= 0;
		double az 	= 0;
		
		int kz 		= 0;
		for(int i=0; i<1; i++) {
			SingleFlatLayer sl = getLayer(i);			
			for(int ix=0; ix<nx; ix++) { 
				a = (float)sl.getLayerProperty(iProperty);
				for(int iz=kz; iz<nz; iz++) data[ix][iz] = a; 
			}
		} 
		
		for(int i=0; i<layerList.size()-1; i++) {
			SingleFlatLayer sl = getLayer(i);			
			for(int ix=0; ix<nx; ix++) { 
				az = sl.getTopDepth()+ix*theTan;
				kz = (int)((az-z0)/dz);
				a = (float)sl.getLayerProperty(iProperty);
				for(int iz=kz; iz<nz; iz++) data[ix][iz] = a; 
			}
		}
		
		return data;
	}

	
	public void read(String selectedFileName) {
		MatrixFileReader reader = new MatrixFileReader();
		reader.readTextualData(selectedFileName, 0, 1, 0, null, ",");
		//reader.printClass();

		int nRow = reader.getRowCount();
		int nCol = reader.getColumnCount();

		int k = 0;
		azimuth = reader.getDataDouble(0, k++)*Math.PI/180.0; 
		dip 	= reader.getDataDouble(0, k++)*Math.PI/180.0;
		k++;
		double x0 = reader.getDataDouble(0, k++); 
		int nx = reader.getDataInt(0, k++);
		double dx = reader.getDataDouble(0, k++);
		double y0 = reader.getDataDouble(0, k++);
		int ny = reader.getDataInt(0, k++);
		double dy = reader.getDataDouble(0, k++);
		int nz = reader.getDataInt(0, k++);

		setIUnit(reader.getDataInt(0, k++));

		anchorE = reader.getDataDouble(1, 0);
		anchorN = reader.getDataDouble(1, 1);
		anchorD = reader.getDataDouble(1, 2);

		int nBoundaries = nRow-1;
		//ArrayList<SingleFlatLayer> layers = getLayer();
		layerList = new ArrayList<SingleFlatLayer>(nBoundaries);

		double a = 0;

		for(int i=1; i<nRow; i++) {
			k = 0;
			SingleFlatLayer layer = new SingleFlatLayer();
			layer.setId(i-1);
			layer.setLayerName("layer_"+i);
			a = reader.getDataDouble(i, k++);
			a = reader.getDataDouble(i, k++);
			a = reader.getDataDouble(i, k++);
			layer.setTopDepth(a);
			layer.setVp(reader.getDataDouble(i, k++));
			if(k<nCol) layer.setVs(reader.getDataDouble(i, k++));
			if(k<nCol) layer.setDen(reader.getDataDouble(i, k++));
			if(k<nCol) layer.setDelta(reader.getDataDouble(i, k++));
			if(k<nCol) layer.setEpsilon(reader.getDataDouble(i, k++));
			if(k<nCol) layer.setGamma(reader.getDataDouble(i, k++));
			if(k<nCol) layer.setQp(reader.getDataDouble(i, k++));
			if(k<nCol) layer.setQs(reader.getDataDouble(i, k++)); //k=11
			if(k<nCol) layer.setLayerName(reader.getData(i, k++));
			layerList.add(layer);
		}
		updateDepth();
		layerList.get(nBoundaries-1).setBottomDepth(layerList.get(nBoundaries-1).getTopDepth());
		updateH();	

		double z0 = layerList.get(0).getTopDepth();
		double z1 = layerList.get(layerList.size()-1).getTopDepth();
		double dz = (z1-z0)/(nz-1);

		for(int i=0; i<layerList.size(); i++) {
			layerList.get(i).setSize(nx, ny, nz, dx, dy, dz, x0, y0, z0);
		}
	}

	public void writeLayer(String selectedFileName) {
		try{
			BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(selectedFileName, false));
			bufferedWriter.write(toString());
			bufferedWriter.close();
		} catch (IOException ioexception) {
			JOptionPane.showMessageDialog(null, "\nFile Format is not right!", 
					"Error", JOptionPane.ERROR_MESSAGE);
		}
	}
}
