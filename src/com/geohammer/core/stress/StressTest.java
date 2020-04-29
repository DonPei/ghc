package com.geohammer.core.stress;

import java.awt.Color;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import org.ucdm.core.acquisition.VCPair;
import org.ucdm.core.planarmodel.DipLayer1D;
import org.ucdm.core.planarmodel.Grid2D;

import edu.mines.jtk.mosaic.PlotPanel;
import edu.mines.jtk.mosaic.PointsView;
import edu.mines.jtk.sgl.Point3;
import edu.mines.jtk.sgl.SimpleFrame;
import edu.mines.jtk.sgl.TriangleGroup;
import edu.mines.jtk.util.AxisTics;

public class StressTest implements Runnable {
	
	//https://theses.lib.vt.edu/theses/available/etd-06082010-143027/unrestricted/Jin_L_T_2010.pdf
	//matclipse

	//private RandomFloat 	_random 	= new RandomFloat(19580427);
	//private Random 	_random 	= new Random();
	//private RandomFloat 	_random 	= new RandomFloat();
	public StressTest() {

	}
	public void run() {	
		//okadaFigure7();
		//okadaFigure8();
		//okadaTest();
		//rotationTest();
		//testRotation();
		test3D();
		System.out.println("Successfully Done!");
	}
	public static void main(String[] args) {
		System.out.println("Executing Test()");
		SwingUtilities.invokeLater( new StressTest() );
	}
	
	public void test2D() {
		EllipticalStress ellipticalStress = new EllipticalStress();
		//ellipticalStress.elliptical2DColorMapTest();
		
	}
	public void test3D() {
		EllipticalStress ellipticalStress = new EllipticalStress();
		//ellipticalStress.elliptical3DTest();
		ellipticalStress.elliptical3DTest2();
		//ellipticalStress.normPaperFigure4_5();
	}

	public void crack2D() {
		//2D crack solution, X is along crack length direction Y is perpendicular to crack length direction

		double dHalfLength = 50;
		double dPoisson = 0.25;
		double dPressure = 1.;
		int nNumFracLength = 10;
		int nNumPts = 100;
		// along length direction
		double dY = 0.;
//		% dX = (linspace(0,nNumFracLength * dHalfLength * 2,nNumPts))'; 
//		% [dSSx, dSSy, dTauxy, dSSv]=StressCalc2DCrack(dPressure,dPoisson,dX,dY,dHalfLength);
//
//		// along Y direction
//		dX = 0 * dHalfLength;   % one fracture length away
//		% dX = 2 * dHalfLength;   % one fracture length away
//		dY = (linspace(0,nNumFracLength * dHalfLength * 2,nNumPts))'; 
//		[dSSx, dSSy, dTauxy, dSSv]=StressCalc2DCrack(dPressure,dPoisson,dX,dY,dHalfLength);
//		test1 = [dY,dSSx, dSSy, dTauxy, dSSv];
//
//		% figure
//		% plot(dY,dSSx);
//		% hold on
//		% plot(dY,dSSy);
//		% hold on
//		% plot(dY,dTauxy);
//
//		% along Y direction
//		dX = 6 * dHalfLength;   % one fracture length away
//		dY = (linspace(0,nNumFracLength * dHalfLength * 2,nNumPts))'; 
//		[dSSx, dSSy, dTauxy, dSSv]=StressCalc2DCrack(dPressure,dPoisson,dX,dY,dHalfLength);
//		test2 = [dY,dSSx, dSSy, dTauxy, dSSv];
//
//		% figure
//		% plot(dY,dSSx);
//		% hold on
//		% plot(dY,dSSy);
//		% hold on
//		% plot(dY,dTauxy);
//		
//		EllipticalStress ellipticalStress = new EllipticalStress();
//		//ellipticalStress.elliptical3DTest();
//		ellipticalStress.normPaperFigure4_5();
	}
	
	public void rotationTest() {
		double n0 = 0;  //northing
		double e0 = 0;  //easting
		double z0 = 0;  //down
		double n1 = 1;  
		double e1 = 1;  
		double z1 = 0;  
		
//		Point3 [] p = new Point3[] { new Point3(n0, e0, z1), new Point3(n0, e1, z1), 
//				new Point3(n1, e1, z0), new Point3(n1, e0, z0)};
		Point3 [] p = new Point3[] { new Point3(n0, e0, z1), new Point3(n0, e1, z1)};
		printP(p);
		System.out.println("");
		float [][][] NED = new float[2][3][p.length];
		for(int i=0; i<p.length; i++) {
			NED[0][0][i] = (float)p[i].x;
			NED[0][1][i] = (float)p[i].y;
			NED[0][2][i] = (float)p[i].z;
		}
		
		//rotation on Z
		double strike = 180+45;
		double theta = 90-strike;
		double theSin = Math.sin(theta*Math.PI/180.0);
		double theCos = Math.cos(theta*Math.PI/180.0);
		double [][] r2D = new double[][] { {theCos, -theSin}, {theSin, theCos} };

		for(int i=0; i<p.length; i++) {
			Point3 q = new Point3(p[i].y, p[i].x, 0);
			q = multiply(q, r2D);
			p[i].x = q.y;
			p[i].y = q.x;
			//p[i].z = -q.y;
		}
		printP(p);
		for(int i=0; i<p.length; i++) {
			NED[1][0][i] = (float)p[i].x;
			NED[1][1][i] = (float)p[i].y;
			NED[1][2][i] = (float)p[i].z;
		}
		
		String aTitle = new String("Fault EN Section");
		JFrame frame = new JFrame(aTitle);
		frame.setSize(600, 600); 
		frame.setLocationRelativeTo(null);  
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); 
		frame.setVisible(true);
		
//		PointsFancyPlot pointsFancyPlot = new PointsFancyPlot(frame, aTitle, false, 
//				LayerPanel.Orientation.X1RIGHT_X2UP,  LayerPanel.AxesPlacement.LEFT_BOTTOM, 
//				"Easting", "Northing");
//		pointsFancyPlot.update(NED[0][1], NED[0][0], 0, "Easting", "Northing", "k-", aTitle);
//		pointsFancyPlot.update(NED[1][1], NED[1][0], 1, "Easting", "Northing", "r-", aTitle+"1");
//		pointsFancyPlot.showDialog();
				
	}
	private Point3 multiply(Point3 p, double [][] r) {
		Point3 q = new Point3(0, 0, 0);
		
		q.x = r[0][0]*p.x + r[0][1]*p.y;
		q.y = r[1][0]*p.x + r[1][1]*p.y;
		
		return q;
	}
	private void printP(Point3 [] p) {
		for(int i=0; i<p.length; i++) {
			System.out.println(" "+p[i].x+" "+p[i].y+" "+p[i].z);
		}
	}
	
	public void okadaFigure7() {
//		Grid2D grid2D = genGrid2DFigure7();
//		DipLayer1D dipLayer1D = null;
//		Okada okada = new Okada(dipLayer1D, null, grid2D);
//		//float[] xyz = okada.figure7BackUp();
//		double L = 20000.0; //m
//		double W = 10000.0; // m
//		double U = 0.5; // m
//		double dip = 90.0;
//		double c = 20000.0; //m
//		int faultType = 2;
//		okada.calSurfaceTilt(c, L, W, U, 90.0, dip, faultType);
//		float a = 5.0E5f; //0.5E10
//		float[] xyz = grid2D.makeTriangles(a);
//		
//		SimpleFrame sf = new SimpleFrame();
//		TriangleGroup tg = sf.addTriangles(xyz);
//		tg.setColor(Color.BLUE);
	}
	
	
	public void okadaFigure8() {
		VCPair vcPW = genVCPairFigure8();
		DipLayer1D dipLayer1D = null;
		Okada okada = new Okada(dipLayer1D, vcPW, null);
		//okada.test();
		okada.figure8();
		//okada.figure9();
		
		int nt = vcPW.getFlag(0);
		float [] x = new float[nt];
    	float [] yTensile = new float[nt];
    	float [] yStrike = new float[nt];
    	float [] yDip = new float[nt];
    	
    	for(int i=0; i<nt; i++) {
    		x[i] = (float)(vcPW.getRD(i));
    		yTensile[i] = (float)(vcPW.getCalPT(i));
    		yStrike[i] = (float)(vcPW.getCalST(i));
    		yDip[i] = (float)(vcPW.getCalSV(i));
    	}
    	
    	float [][] y = new float[][]{yTensile, yStrike, yDip};
    	
    	String aTitle = "duxdz";
    	String xAxisLabel = "duxdz";
    	String yAxisLabel = "Depth";
    	
    	JFrame frame = new JFrame(aTitle);
		frame.setSize(600, 800); 
		frame.setLocationRelativeTo(null);  
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); 
		frame.setVisible(true);
		
		PlotPanel panel = new PlotPanel(PlotPanel.Orientation.X1DOWN_X2RIGHT);
		panel.setBackground(Color.WHITE);
		panel.setVLabel(yAxisLabel);
		panel.setHLabel(xAxisLabel);
		
		String [] style = new String[]{"r-", "g-", "b-"};
		for(int i=0; i<y.length; i++) {
			PointsView pv = panel.addPoints(x,y[i]);
			pv.setStyle(style[i]);
			pv.setLineWidth(1);	
			frame.add( panel );
		}
	}
	public void testRotation() {
		double strike = 45;
		double dip = 90;
		double rn = 1;
		double re = 1;
		double sinAz = Math.sin((90-strike)*Math.PI/180.0);
		double cosAz = Math.cos((90-strike)*Math.PI/180.0);
		double sinDip = Math.sin(dip*Math.PI/180.0);
		double cosDip = Math.cos(dip*Math.PI/180.0);
		double ae = re * cosAz + rn * sinAz;
		double an = -re * sinAz + rn * cosAz;
		
		//rotate back reverse
		double ae1 = ae * cosAz - an * sinAz;
		double an1 = ae * sinAz + an * cosAz;
		System.out.println("rn="+rn+" re="+re+" an="+an+" ae="+ae+" an1="+an1+" ae1="+ae1);
	}
	
	public void testRotation1() {
		double strike = 45;
		double dip = 90;
		double rn = 1;
		double re = 1;
		double sinAz = Math.sin(strike*Math.PI/180.0);
		double cosAz = Math.cos(strike*Math.PI/180.0);
		double sinDip = Math.sin(dip*Math.PI/180.0);
		double cosDip = Math.cos(dip*Math.PI/180.0);		
		double ae = re * cosAz - rn * sinAz;
		double an = re * sinAz + rn * cosAz;
		
		//rotate back reverse
		double ae1 = ae * cosAz + an * sinAz;
		double an1 = -ae * sinAz + an * cosAz;
		System.out.println("rn="+rn+" re="+re+" an="+an+" ae="+ae+" an1="+an1+" ae1="+ae1);
	}
	public void okadaTest() {
		VCPair vcPW = genVCPair();
		DipLayer1D dipLayer1D = null;
		Okada okada = new Okada(dipLayer1D, vcPW, null);
		//okada.setEclipse(300, 100, 0.5);
		//okada.toRectangleModel();
		okada.setRectangle(300, 100, 0.05);
		//okada.setRectangle(303, 109, 0.05);
		okada.setStrike(90);
		okada.setDip(90);
		okada.startTest();
		
		int nt = vcPW.getFlag(0);
		float [][] x = new float[2][nt];
    	float [][] y = new float[2][nt];
    	
    	for(int i=0; i<nt; i++) {
    		x[0][i] = (float)(vcPW.getRD(i));
    		y[0][i] = (float)(vcPW.getCalPT(i));
    		
    		x[1][i] = (float)(vcPW.getRD(i));
    		y[1][i] = (float)(vcPW.getCalST(i));
    	}
    	
    	String aTitle = "Time";
    	String xAxisLabel = "duxdz";
    	String yAxisLabel = "Depth";
    	
    	JFrame frame = new JFrame();
		frame.setSize(400, 800); 
		frame.setLocationRelativeTo(null);  
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); 
		frame.setVisible(true);
		
		PlotPanel panel = new PlotPanel(PlotPanel.Orientation.X1DOWN_X2RIGHT);
		panel.setBackground(Color.WHITE);
		panel.setVLabel(yAxisLabel);
		panel.setHLabel(xAxisLabel);
		
		PointsView pv = panel.addPoints(x,y);
		pv.setStyle("k-");
		pv.setLineWidth(1);
		frame.add( panel );
	}
	public Grid2D genGrid2DFigure7() {
		int ne = 200;
		double e0 = -55000;
		double e1 = 55000;
		double de = (e1-e0)/(ne-1);
		
		int nn = 200;
		double n0 = -40000;
		double n1 = 40000;
		double dn = (n1-n0)/(nn-1);
		
		Grid2D grid2D = new Grid2D(1, nn, ne, dn, de, n0, e0, null);
		grid2D.allocate();
		grid2D.setDescription("dipSlip");
		
		return grid2D;
	}
	public VCPair genVCPairFigure8() {
		int numSens = 2000;
		double [] wellHead = new double[] {25000, 15000, 0}; //[Easting, Northing, TVD]
		double dp1 = 0;
		double dp2 = 20000;
		double ddp = (dp2-dp1)/numSens;

		double [] rE = new double[numSens];
		double [] rN = new double[numSens];
		double [] rD = new double[numSens];
		for(int i=0, k=0; i<numSens; i++, k++) {
			rE[k] = wellHead[0];
			rN[k] = wellHead[1];
			rD[k] = dp1+i*ddp;
		}		
		
		int nEvents = 1;
		double sDepth = 10000;
		double r = 0;
		double theta = 0;
		double dTheta = 0;
		
		double [] sE = new double[nEvents];
		double [] sN = new double[nEvents];
		double [] sD = new double[nEvents];

		for(int i=0; i<nEvents; i++) {
			sE[i] = 0;
			sN[i] = 0;
			sD[i] = sDepth;
		}
		
		int distUnit = 1;
		
		return new VCPair(sE, sN, sD, rE, rN, rD, distUnit);
	}
	
	public VCPair genVCPair() {
		int numSens = 1000;
		double [] wellHead = new double[] {150, 150, 0}; //[Easting, Northing, TVD]
		double dp1 = 4000;
		double dp2 = 6000;
		double ddp = (dp2-dp1)/numSens;

		double [] rE = new double[numSens];
		double [] rN = new double[numSens];
		double [] rD = new double[numSens];
		for(int i=0, k=0; i<numSens; i++, k++) {
			rE[k] = wellHead[0];
			rN[k] = wellHead[1];
			rD[k] = dp1+i*ddp;
		}		
		
		int nEvents = 1;
		double sDepth = 5000;
		double r = 0;
		double theta = 0;
		double dTheta = 0;
		
		double [] sE = new double[nEvents];
		double [] sN = new double[nEvents];
		double [] sD = new double[nEvents];

		for(int i=0; i<nEvents; i++) {
//			theta = (0+i*dTheta)*Math.PI/180.0;
//			sE[i] = r*Math.sin(theta);
//			sN[i] = r*Math.cos(theta);
//			sD[i] = sDepth;
			sE[i] = 0;
			sN[i] = 0;
			sD[i] = sDepth;
		}
		
		int distUnit = 2;
		
		return new VCPair(sE, sN, sD, rE, rN, rD, distUnit);
	}
	
	private void plotLines(JFrame frame, float [] ax, float [] ay, 
			String aTitle, String xAxisLabel, String yAxisLabel, double delta, double epsilon) { 
//		PointsPlotDialog dialog = new PointsPlotDialog(frame, aTitle, 2, ax, ay, xAxisLabel, yAxisLabel);
//		dialog.setLineOn(true);
//		dialog.setMarkOn(false);
//		//dialog.setLineOn(false);
//		//dialog.setMarkOn(true);
//		dialog.showDialog();
	}
	

	  private float[] makeSineWave() {
	    int nx = 100;
	    int ny = 100;
	    float dx = 10.0f/(float)nx;
	    float dy = 10.0f/(float)ny;
	    float[] xyz = new float[3*6*nx*ny];
	    for (int ix=0,i=0; ix<nx; ++ix) {
	      float x0 = ix*dx;
	      float x1 = (ix+1)*dx;
	      for (int iy=0; iy<ny; ++iy) {
	        float y0 = iy*dy;
	        float y1 = (iy+1)*dy;
	        xyz[i++] = x0;  xyz[i++] = y0;  xyz[i++] = sin(x0,y0);
	        xyz[i++] = x0;  xyz[i++] = y1;  xyz[i++] = sin(x0,y1);
	        xyz[i++] = x1;  xyz[i++] = y0;  xyz[i++] = sin(x1,y0);
	        xyz[i++] = x1;  xyz[i++] = y0;  xyz[i++] = sin(x1,y0);
	        xyz[i++] = x0;  xyz[i++] = y1;  xyz[i++] = sin(x0,y1);
	        xyz[i++] = x1;  xyz[i++] = y1;  xyz[i++] = sin(x1,y1);
	      }
	    }
	    return xyz;
	  }
	  private static float sin(float x, float y) {
	    return (float)(5.0+0.25*Math.sin(x+y));
	  }

	  private static float[] addBulge(float[] xyz) {
	    int n = xyz.length;
	    float[] t = new float[n];
	    for (int i=0; i<n; i+=3) {
	      float x = xyz[i  ];
	      float y = xyz[i+1];
	      float z = xyz[i+2];
	      z -= exp(x,y);
	      t[i  ] = x;
	      t[i+1] = y;
	      t[i+2] = z;
	    }
	    return t;
	  }
	  private static float exp(float x, float y) {
	    x -= 5.0f;
	    y -= 5.0f;
	    x *= 0.4f;
	    y *= 0.8f;
	    return (float)(2.0*Math.exp(-x*x-y*y));
	  }

	  private static float[] addTear(float[] xyz) {
	    int n = xyz.length;
	    float[] t = new float[n];
	    int nt = n/9;
	    for (int it=0,i=0,j=0; it<nt; ++it) {
	      float xa = xyz[i++];  
	      float ya = xyz[i++];  
	      float za = xyz[i++];  
	      float xb = xyz[i++];  
	      float yb = xyz[i++];  
	      float zb = xyz[i++];  
	      float xc = xyz[i++];  
	      float yc = xyz[i++];  
	      float zc = xyz[i++];  
	      float x = 0.333333f*(xa+xb+xc);
	      if (x>5.0f) {
	        za += exp(xa,ya);
	        zb += exp(xb,yb);
	        zc += exp(xc,yc);
	      }
	      t[j++] = xa;  t[j++] = ya;  t[j++] = za;
	      t[j++] = xb;  t[j++] = yb;  t[j++] = zb;
	      t[j++] = xc;  t[j++] = yc;  t[j++] = zc;
	    }
	    return t;
	  }

}
