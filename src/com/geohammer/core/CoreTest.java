package com.geohammer.core;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.StringTokenizer;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.ucdm.core.acquisition.VCPair;
import org.ucdm.core.geometry.RotationNED;
import org.ucdm.core.geometry.Vector3D;
import org.ucdm.core.planarmodel.DipLayer1D;
import org.ucdm.core.planarmodel.FlatLayer1D;
import org.ucdm.core.planarmodel.Grid3D;
import org.ucdm.core.planarmodel.Layer3D;
import org.ucdm.core.planarmodel.SingleLayer3D;
import org.ucdm.core.planarmodel.ZmapPlus;

public class CoreTest implements Runnable {
	public CoreTest() {

	}
	public void run() {
		//test1();
		//testZmapPlus();
		//testVecon();
		//toSrcMech();
		//testRotation();
		//testTextReader();
		//testGrid3D();
		//testRotationVector();
		//testRotationVector1();
		//rotate2D();
		testCircleReceiverGeometry();
		
		System.out.println("Successfully Done!");
	}
	public static void main(String[] args) {
		System.out.println("Executing SeisPTTest()");
		SwingUtilities.invokeLater( new CoreTest() );
	}
	public void testZmapPlus() {
		String [] formations = new String[] {
				"C:\\PINN_DATA\\Anadarko\\Tops\\NBRR_ZMAP_grid.dat",
				"C:\\PINN_DATA\\Anadarko\\Tops\\CDLL_ZMAP_grid.dat",
				"C:\\PINN_DATA\\Anadarko\\Tops\\GRNR_ZMAP_grid.dat",
				"C:\\PINN_DATA\\Anadarko\\Tops\\HTLD_ZMAP_grid.dat",
				"C:\\PINN_DATA\\Anadarko\\Tops\\LINC_ZMAP_grid.dat",
				"C:\\PINN_DATA\\Anadarko\\Tops\\GRRS_ZMAP_grid.dat"
		};
		ZmapPlus zmapPlus = new ZmapPlus("C:\\PINN_DATA\\Anadarko\\Tops\\CDLL_ZMAP_grid.dat");
		if(zmapPlus.invalidNumberExists()) zmapPlus.condition();
		
	}
	public void testGrid3D() {
		VCPair vcPW = new VCPair(1, "C:\\PINN_DATA\\test\\Quake3D\\msModelPck.csv");
		double sampleInterval = 0.0025;
		
		DipLayer1D dipLayer1D = new DipLayer1D("C:\\PINN_DATA\\test\\Quake3D\\msModel.csv");
		//System.out.println(dipLayer1D.toString());
		
		Grid3D grid3D = dipLayer1D.toGrid3D(1);
		grid3D.writeAsBinary("C:\\PINN_DATA\\test\\Quake3D\\msModel.bin");
	}
	
	public void testVecon() {
		//String layerName = "layer 1";  int id = 0; 
		double vp = 10000; 			double vs = 8000; 			double den = 2.0;
		double vpGradientX = 0; 	double vpGradientY = 0; 	double vpGradientZ = 0; 
		double vsGradientX = 0; 	double vsGradientY = 0; 	double vsGradientZ = 0; 
		double delta = 0;  			double epsilon = 0;  		double gamma = 0;  
		double qp = 0;  			double qs = 0;  			double theta = 0;  
		double phi = 0; 
		
//		String [] formationTops = new String[] {
//				"C:\\PINN_DATA\\Anadarko\\Tops\\NBRR_ZMAP_grid.dat",
//				"C:\\PINN_DATA\\Anadarko\\Tops\\CDLL_ZMAP_grid.dat",
//				"C:\\PINN_DATA\\Anadarko\\Tops\\GRNR_ZMAP_grid.dat",
//				"C:\\PINN_DATA\\Anadarko\\Tops\\HTLD_ZMAP_grid.dat",
//				"C:\\PINN_DATA\\Anadarko\\Tops\\LINC_ZMAP_grid.dat",
//				"C:\\PINN_DATA\\Anadarko\\Tops\\GRRS_ZMAP_grid.dat"
//		};
//		String [] formationNames = new String[] { "NBRR", "CDLL", "GRNR", "HTLD", "LINC", "GRRS" };
		String [] formationTops = new String[] {
				"C:\\PINN_DATA\\Anadarko\\Tops\\NBRR_ZMAP_grid.dat",
				"C:\\PINN_DATA\\Anadarko\\Tops\\CDLL_ZMAP_grid.dat",
				"C:\\PINN_DATA\\Anadarko\\Tops\\GRNR_ZMAP_grid.dat",
				"C:\\PINN_DATA\\Anadarko\\Tops\\LINC_ZMAP_grid.dat",
				"C:\\PINN_DATA\\Anadarko\\Tops\\GRRS_ZMAP_grid.dat"
		};
		String [] formationNames = new String[] { "NBRR", "CDLL", "GRNR", "LINC", "GRRS" };
		int nBoundaries = formationNames.length;
		Layer3D layer3D = new Layer3D(nBoundaries);

		for(int i=0; i<nBoundaries; i++) {
			ZmapPlus zmapPlus = new ZmapPlus(formationTops[i]);
			if(zmapPlus.invalidNumberExists()) zmapPlus.condition();
			zmapPlus.multiply(-1.0);

			String layerName = formationNames[i]; 
			int nx = zmapPlus.getColumnCount();
			int ny = zmapPlus.getRowCount();
			double x0 = zmapPlus.getX0();
			double x1 = zmapPlus.getX1();
			double y0 = zmapPlus.getY0();
			double y1 = zmapPlus.getY1();
			double dx = (x1-x0)/(nx-1);
			double dy = (y1-y0)/(ny-1);
			vp += 1000;
			vs += 1000;
			float [][] dp = zmapPlus.getDataFloat();
			SingleLayer3D sl = new SingleLayer3D(layerName, i, vp, vs, den, 
					vpGradientX, vpGradientY, vpGradientZ, vsGradientX, vsGradientY, vsGradientZ, 
					delta, epsilon, gamma, qp, qs, theta, phi,
					nx, ny, dx, dy, x0, y0, dp);		

			System.out.println(sl.toString(1));
			
			layer3D.getLayer().add(sl);
		}
		layer3D.writeLayer("C:\\PINN_DATA\\Anadarko\\Tops\\wardell.3lay");
		
		Layer3D layer3D1 = new Layer3D(1, "C:\\PINN_DATA\\Anadarko\\Tops\\wardell.3lay");
	}
	public void testCircleReceiverGeometry() {
		int nEvents = 1;
		int nReceivers = 72;
		int distUnit = 2;
		
		VCPair vcPair = new VCPair(nEvents, nReceivers, distUnit);
		vcPair.genReceiverCircle(0, 0, 9400, 400);
		String outputFileName = "C:\\PINN_DATA\\EP\\distorted\\pck.csv";
		vcPair.write(false, outputFileName);		
	}
	
	public void test1() {
		SeisPTSrcMech srcMech = new SeisPTSrcMech(false, "C:\\PINN_DATA\\natalia\\case2\\4H stg07-09-10.txt", 
				null);
		
		String a = "  this   ";
		String b = "     ";
		System.out.println("a="+a.trim()+" b="+b.trim());
		StringTokenizer st = new StringTokenizer(b, " ,\t");
		if(b.trim()==null) System.out.println("a="+a.trim()+" b=null");
		System.out.println("a="+a.trim()+" b="+st.countTokens());
//		MSEvent [] msEvents = srcMech.getMSEvents();
//		//System.out.println(msEvents[0].toString());
//		//msEvents[0].printTrace(0, 0, 1);
//		SeisPTVel vel = new SeisPTVel("C:\\PINN_DATA\\COP\\stage5kc1\\SrcMech\\SeisPTVel.txt", 2);
//		FlatLayer1D flayer1D = vel.toFlatLayer1D();
//		int nx = 5;
//		int ny = 5;
//		int nz = 3;
//		double dx = 10.0;
//		double dy = 10.0;
//		double dz = 5.0;
//		double x0 = -650.0;
//		double y0 = -30.0;
//		double z0 = 12250.0;
//		double x1 = 30.0;
//		double y1 = 950.0;
//		double z1 = 13150.0;
//
//		VCPair vcPW = msEvents[0].toVCPair(-1, 0);
		//System.out.println(vcPW.toDeepString());
	}
	
	public void toSrcMech() {
		VCPair vcPW = new VCPair(1, "C:\\PINN_DATA\\mti\\syntheticTest\\pckOneWell.csv");
		double sampleInterval = 0.0025;
		
		SeisPTSrcMech seisPTSrcMech = new SeisPTSrcMech(sampleInterval, vcPW);
		seisPTSrcMech.write(false, "C:\\PINN_DATA\\mti\\syntheticTest\\pckOneWell.txt");
	}
	public void testTextReader() {
		String fileName = "C:\\Don\\Job\\14\\halliburton\\jobTitleAllCompany.txt";
		
		try{
			BufferedReader reader = new BufferedReader(new FileReader(fileName));
			String line = reader.readLine();
			String delims = "[)]";
			String [] a = line.split(delims);
			for(int i=0; i<a.length; i++) System.out.println(a[i]+")");
			reader.close();
		} catch (IOException ioexception) {
			JOptionPane.showMessageDialog(null, "\nFile Format is not right!", 
					"Error", JOptionPane.ERROR_MESSAGE);
		}
		
	}
	
	public void testRotation() {
		double nx = 1.0;
		double ey = 0.0;
		double dz = 1.0;
		
		double azimuth = 0*Math.PI/180.0;
		double dip = -45*Math.PI/180.0;
		
		double [] px = new double[]{ nx };
		double [] py = new double[]{ ey };		
		double [] pz = new double[]{ dz };
		
		fromFlatToTilt(px, py, pz, azimuth, dip);
		
		fromTiltToFlat(px, py, pz, -azimuth, -dip);
	}
	public void testRotationVector1() {
		double az = 10;
		double theta = 89.9;
		
		Vector3D [] units = new Vector3D [] {
				new Vector3D(1, 0, 0),
				new Vector3D(0, 1, 0),
				new Vector3D(0, 0, 1)
		};

		double [][] mZ = RotationNED.matrixRotateOnD(-az);
		double [][] mN = RotationNED.matrixRotateOnN(-theta);
		double [][] m = new double[3][3];
		for(int i=0; i<3; i++) {
			for(int j=0; j<3; j++) {
				double sum = 0;
				for(int k=0; k<3; k++) sum += mZ[i][k]*mN[k][j];
				m[i][j] = sum;			
			}
		}
		for(int i=0; i<units.length; i++) {
			units[i].applyMatrixEqual(m);
			System.out.println("i="+i+" "+units[i].toString());
		}
		
	}
	public void testRotationVector() {
		double nx = 1.732;
		double ey = 1.0;
		double dz = 1.0;
		
		Vector3D vector3D = new Vector3D(nx, ey, dz);
		System.out.println("vector3D="+vector3D.toString());
		double az = vector3D.calAzimuth();
		double theta = vector3D.calTheta();
		System.out.println("az="+az+" theta="+theta);
		
		double[][] mZ = RotationNED.matrixRotateOnD(az);
		double[][] mE = RotationNED.matrixRotateOnE(-theta);
		double [][] m = new double[3][3];
		for(int i=0; i<3; i++) {
			for(int j=0; j<3; j++) {
				double sum = 0;
				for(int k=0; k<3; k++) sum += mE[i][k]*mZ[k][j];
				m[i][j] = sum;			
			}
		}
		Vector3D v = vector3D.applyMatrix(m);
		System.out.println("v="+v.toString());
		//Vector3D v1 = v.applyMatrix(mY);
		//System.out.println("v1="+v1.toString());
		
		
		Vector3D unit = new Vector3D(2.236, 0, 0);
		mZ = RotationNED.matrixRotateOnD(-az);
		mE = RotationNED.matrixRotateOnE(theta);
		for(int i=0; i<3; i++) {
			for(int j=0; j<3; j++) {
				double sum = 0;
				for(int k=0; k<3; k++) sum += mZ[i][k]*mE[k][j];
				m[i][j] = sum;			
			}
		}
		Vector3D v3 = unit.applyMatrix(m);
		System.out.println("v3="+v3.toString());
	}
	public void rotate2D() {
		double azimuth = 30;
		double theSin = Math.sin(azimuth*Math.PI/180.0);
		double theCos = Math.cos(azimuth*Math.PI/180.0);
		double n = 1;
		double e = 0;
		double rn = 0;
		double re = 0;
		rn = n*theCos+e*theSin;
		re = -n*theSin+e*theCos;
		System.out.println("azimuth="+azimuth+" n="+n+" e="+e+" rn="+rn+" re="+re);
		//azimuth=30.0 n=1.0 e=0.0 rn=0.8660254037844387 re=-0.49999999999999994
		//azimuth=45.0 n=1.0 e=0.0 rn=0.7071067811865476 re=-0.7071067811865475
		//azimuth=60.0 n=1.0 e=0.0 rn=0.5000000000000001 re=-0.8660254037844386
		//azimuth=89.0 n=1.0 e=0.0 rn=0.017452406437283376 re=-0.9998476951563913
	}
	//nx0, ey0, dz0 is in flat coordinate system
	//azimuth and dip define the tilt coordinate system
	//Positive dipAngle means dip downward along dipAzimuth.
	//public void fromFlatToTilt(double nx0, double ey0, double dz0, double azimuth, double dip) {
	public void fromFlatToTilt(double [] px, double [] py, double [] pz, double azimuth, double dip) {
		DipLayer1D.rotate2D(px, py, azimuth);
		System.out.println("pN1="+px[0]+" pE="+py[0]+" pD="+pz[0]);	
		
		DipLayer1D.rotate2D(pz, px, dip);	
		System.out.println("pN2="+px[0]+" pE="+py[0]+" pD="+pz[0]);
	}
	public void fromTiltToFlat(double [] px, double [] py, double [] pz, double azimuth, double dip) {
		DipLayer1D.rotate2D(pz, px, dip);
		System.out.println("pN3="+px[0]+" pE="+py[0]+" pD="+pz[0]);
			
		DipLayer1D.rotate2D(px, py, azimuth);
		System.out.println("pN4="+px[0]+" pE="+py[0]+" pD="+pz[0]);	
	}
	
	public double [] fromTiltToFlat1(double nx0, double ey0, double dz0, double azimuth, double dip) {
		//rotation along Y
		double [] qx = new double[]{ dz0 };
		double [] qy = new double[]{ nx0 };		
		DipLayer1D.rotate2D(qx, qy, dip);
		
		System.out.println("qx="+qx[0]+" qy="+qy[0]);
		
		double [] px = new double[]{ qy[0] };
		double [] py = new double[]{ ey0 };		
		DipLayer1D.rotate2D(px, py, azimuth);

		System.out.println("pN="+px[0]+" pE="+py[0]);
		
		System.out.println("xPrime="+px[0]+" yPrime="+py[0]+" zPrime="+qx[0]);	
		return new double[]{px[0], py[0], qx[0]};
	}
}
