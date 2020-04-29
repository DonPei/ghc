package com.geohammer.core.stress;

import com.geohammer.core.acquisition.VCPair;
import com.geohammer.core.planarmodel.Grid2D;
import com.geohammer.core.planarmodel.Layer3D;

import edu.mines.jtk.mosaic.SimplePlot;

//G:\TFS\DuDevelopment\FracNetwork\Documents

public class EllipticalStress extends EllipticalStressTilt {

	private Layer3D 	_layer3D 			= null;
	private VCPair 		_vcPW 				= null;		

	public double 		_youngsModulus		= 4.0e6;
	public double 		_poissonsRatio		= 0.25;

	public EllipticalStress() { super(); }

	public EllipticalStress(double youngsModulus, double poissonsRatio, VCPair vcPW, Layer3D layer3D) {
		_vcPW 				= vcPW;
		_layer3D 			= layer3D;

		_youngsModulus		= youngsModulus;
		_poissonsRatio		= poissonsRatio;
	}
	//	private void  calYoungModulusAndPoissonRatio(DipLayer1D dipLayer1D) {
	//		double vp	= dipLayer1D.getLayer(0).getVp();      // P-wave velocity m/s
	//		double vs	= dipLayer1D.getLayer(0).getVs();      // S-wave velocity m/s 
	//		double den	= dipLayer1D.getLayer(0).getDen();    	// density g/m^3
	//		double mu = vs*vs*den; 
	//		//System.out.println("vp="+vp+" vs="+vs+" den="+den+" qp="+qp+" qs="+qs);
	//		_poissonRatio		= 0.5*(vp*vp-2.0*vs*vs)/(vp*vp-vs*vs);
	//		_youngModulus		= 2.0*mu*(1.0+_poissonRatio);
	//	}

	public void calDownHoleTilt(int method, double fa, double fb, double fPressure) {
		double am, ak, acon, g, ek;
		double [] vlam = new double[1];
		double [] vmu = new double[1];
		double [] vnu = new double[1];

		double se, sn, sd, re, rn, rd;

		g = _youngsModulus / (2.0 * (1.0 + _poissonsRatio));      // shear modulus
		//	     GET MULTIPLYING CONSTANT WITHOUT ELLIPTIC INTEGRAL
		acon = -fa*fb*fb * fPressure / (16.0 * g);
		//	     ALSO NEED ELLIPTIC INTEGRAL FOR MULTIPLYING CONSTANT
		ak = Math.sqrt(fa*fa - fb*fb) / fa;
		//	     NOW GET THE COMPLEMENTARY PARAMETER (NOT MODULUS)
		am = 1.0 - ak * ak;
		ek = 0.0;
		if(method==0) 		ek = SurgiFracEllint(am);
		else if(method==1) 	ek = ellint(am);
		if(ek != 0.0) acon = acon / ek;

		System.out.println("fa="+fa+" fb="+fb+" fPressure="+fPressure+" youngsModulus="+_youngsModulus+
				" poissonsRatio="+_poissonsRatio+" ak="+ak+" ek="+ek+" acon="+acon);
		//double [] dS = new double[6]; //{sigx, sigy, sigz, tauxy, tauyz, tauxz}
		double [] sigx = new double[1];
		double [] sigy = new double[1];
		double [] sigz = new double[1];
		double [] tauxy = new double[1];
		double [] tauyz = new double[1];
		double [] tauxz = new double[1];
		
		double [] dudx = new double[1];
		double [] dudy = new double[1];
		double [] dudz = new double[1];
		double [] dvdx = new double[1];
		double [] dvdy = new double[1];
		double [] dvdz = new double[1];
		double [] dwdx = new double[1];
		double [] dwdy = new double[1];
		double [] dwdz = new double[1];

		double [][] data = _vcPW.getData();
		for(int i=0, k=0; i<_vcPW.getNumOfEvents(); i++) {
			//se = _vcPW.getEE(i);
			//sn = _vcPW.getEN(i);
			//sd = _vcPW.getED(i);
			for(int j=0; j<_vcPW.getFlag(i); j++, k++) {	
				re = _vcPW.getRE(k);
				rn = _vcPW.getRN(k);				
				rd = _vcPW.getRD(k);
				if(method==0) {
					SurgiFracTrip(re, -rd, -rn, fa, fb, vlam, vmu, vnu);
					SurgiFracEllcrack(fa, fb, vlam[0], vmu[0], vnu[0], g, _poissonsRatio, acon, re, -rd, -rn,
							sigx, sigy, sigz, tauxy, tauyz, tauxz);
					int jj = 0;
					data[k][jj++] = sigx[0]; 	data[k][jj++] = sigy[0]; 	data[k][jj++] = sigz[0];
					data[k][jj++] = tauxy[0]; 	data[k][jj++] = tauyz[0]; 	data[k][jj++] = tauxz[0];

//					_vcPW.setData(k, _vcPW.getCalPT(), sigx[0]);
//					_vcPW.setData(k, _vcPW.getCalST(), sigy[0]);
//					_vcPW.setData(k, _vcPW.getCalSV(), sigz[0]);
//					_vcPW.setData(k, _vcPW.getObsPT(), tauxy[0]);
//					_vcPW.setData(k, _vcPW.getObsST(), tauyz[0]);
//					_vcPW.setData(k, _vcPW.getObsSV(), tauxz[0]);
				} else if(method==1) {
					trip(re, -rd, -rn, fa, fb, vlam, vmu, vnu);
					tilts(re, -rd, -rn, fa, fb, vlam[0], vmu[0], vnu[0], _poissonsRatio, acon, 
							dudx, dudy, dudz, dvdx, dvdy, dvdz, dwdx, dwdy, dwdz);
					int jj = 0;
					data[k][jj++] = dudx[0]; 	data[k][jj++] = dudy[0]; 	data[k][jj++] = dudz[0];
					data[k][jj++] = dvdx[0]; 	data[k][jj++] = dvdy[0]; 	data[k][jj++] = dvdz[0];
					data[k][jj++] = dwdx[0]; 	data[k][jj++] = dwdy[0]; 	data[k][jj++] = dwdz[0];
				}
			}
		}
	}
	
	public void elliptical3DTest2() {
		double pfrac, lfrac, hfrac;
		double E, poisson;
		double x, y, z;
		int nz;
		//double sigx, sigy, sigz, tauxy, tauyz, tauxz;
		double Depth, Azimuth, Dip, Pitch; 
		double XOffset, YOffset;

		int i;

		// Testing data 
		XOffset=0.;         // fracture center
		YOffset=0.;         // fracture center
		Depth=0.;        // fracture depth, depth is positive
		Azimuth=0.;        // fracture azimuth
		Dip=90.001;            // fracture dip
		Pitch=0.0;          // fracture pitch

		pfrac = 1000.0;      // pressure of 1000 psi
		lfrac = 500.0;       // wing length 
		hfrac = 100.0;      // total fracture height 
		E = 6.e+06;         // Young's modulus of 4,000,000 psi
		poisson = 0.25;       // Poisson's ratio of 0.25
		x = 0.0;            // x location  
		y = 0.0;          // y location  
		z=0;           //z location
		nz = 11;           // number of points for calculation

		System.out.println("Pressure Half_Length Height X Y ");
		System.out.println(String.format("%.1f    %.1f   %.1f  ", pfrac, lfrac, hfrac));
		System.out.println("\n X Y Z SigX-3D SigY-3D SigZ-3D Tauxy-3D Tauyz-3D Tauxz-3D ");

		//double [] dS = new double[6]; //{sigx, sigy, sigz, tauxy, tauyz, tauxz}
		double [] sigx = new double[1];
		double [] sigy = new double[1];
		double [] sigz = new double[1];
		double [] tauxy = new double[1];
		double [] tauyz = new double[1];
		double [] tauxz = new double[1];


		for (i=0; i < nz; i++) { 
			x = i*10;
			y = 0;
			z = 0;
			int k = 0;
			EllipFracStressCalc(Depth, Azimuth, Dip, Pitch, pfrac, lfrac, hfrac,
					E, poisson, x, y, z, XOffset, YOffset,
					sigx, sigy, sigz, tauxy, tauyz, tauxz);
			//System.out.println(String.format("\n %.4e %.4e %.4e %.16e %.16e %.16e %.16e %.16e %.16e ",
			//		x, y, z, sigx[0], sigy[0], sigz[0], tauxy[0], tauyz[0], tauxz[0]));
			System.out.println(String.format("%.1f %.1f %.1f %.6f %.6f %.6f %.6f %.6f %.6f ",
					x, y, z, sigx[0], sigy[0], sigz[0], tauxy[0], tauyz[0], tauxz[0]));
		};
	}

	public void elliptical3DTest() {
		double pfrac, lfrac, hfrac;
		double E, poisson;
		double x, y, z;
		int nz;
		//double sigx, sigy, sigz, tauxy, tauyz, tauxz;
		double Depth, Azimuth, Dip, Pitch; 
		double XOffset, YOffset;

		int i;

		// Testing data 
		XOffset=0.;         // fracture center
		YOffset=0.;         // fracture center
		Depth=0.;        // fracture depth, depth is positive
		Azimuth=0.;        // fracture azimuth
		Dip=90.;            // fracture dip
		Pitch=0.0;          // fracture pitch

		pfrac = 500.0;      // pressure of 500 psi
		lfrac = 500.0;       // wing length 
		hfrac = 150.0;      // total fracture height 
		E = 4.e+06;         // Young's modulus of 4,000,000 psi
		poisson = 0.25;       // Poisson's ratio of 0.25
		x = 0.0;            // x location  
		y = 500.0;          // y location  
		z=-5000;           //z location
		nz = 50;           // number of points for calculation

		System.out.println("Pressure Half_Length Height X Y ");
		System.out.println(String.format("%.1f    %.1f   %.1f  ", pfrac, lfrac, hfrac));
		System.out.println("\n X Y Z SigX-3D SigY-3D SigZ-3D Tauxy-3D Tauyz-3D Tauxz-3D ");

		//double [] dS = new double[6]; //{sigx, sigy, sigz, tauxy, tauyz, tauxz}
		double [] sigx = new double[1];
		double [] sigy = new double[1];
		double [] sigz = new double[1];
		double [] tauxy = new double[1];
		double [] tauyz = new double[1];
		double [] tauxz = new double[1];


		for (i=0; i < nz; i++) { 
			x = 30*i;
			y = 500;
			z = -5000;
			int k = 0;
			EllipFracStressCalc(Depth, Azimuth, Dip, Pitch, pfrac, lfrac, hfrac,
					E, poisson, x, y, z, XOffset, YOffset,
					sigx, sigy, sigz, tauxy, tauyz, tauxz);
			//System.out.println(String.format("\n %.4e %.4e %.4e %.16e %.16e %.16e %.16e %.16e %.16e ",
			//		x, y, z, sigx[0], sigy[0], sigz[0], tauxy[0], tauyz[0], tauxz[0]));
			System.out.println(String.format("%.1f %.1f %.1f %.6f %.6f %.6f %.6f %.6f %.6f ",
					x, y, z, sigx[0], sigy[0], sigz[0], tauxy[0], tauyz[0], tauxz[0]));
		};
	}

	//The same coordinates as these in Figure 1
	//Z is distance normal to fracture X is distance along fracture
	public float [][][] elliptical3D(double fractureHalfLength, double fractureHeight, 
			double fracturePressure, double youngs, double poisson, 
			int nz, double z0, double dz, int nx, double x0, double dx) {
		double am, ak, acon, g, ek;
		double [] vlam = new double[1];
		double [] vmu = new double[1];
		double [] vnu = new double[1];

		g = youngs / (2.0 * (1.0 + poisson));      // shear modulus
		//	     GET MULTIPLYING CONSTANT WITHOUT ELLIPTIC INTEGRAL
		acon = fractureHalfLength * 0.25 * fractureHeight * fractureHeight * fracturePressure / (16.0 * g);
		//	     ALSO NEED ELLIPTIC INTEGRAL FOR MULTIPLYING CONSTANT
		ak = Math.sqrt(fractureHalfLength * fractureHalfLength - 0.25 * fractureHeight * fractureHeight) / fractureHalfLength;
		//	     NOW GET THE COMPLEMENTARY PARAMETER (NOT MODULUS)
		am = 1.0 - ak * ak;
		ek = SurgiFracEllint(am);
		if(ek != 0.0) acon = acon / ek;

		//double [] dS = new double[6]; //{sigx, sigy, sigz, tauxy, tauyz, tauxz}
		double [] sigx = new double[1];
		double [] sigy = new double[1];
		double [] sigz = new double[1];
		double [] tauxy = new double[1];
		double [] tauyz = new double[1];
		double [] tauxz = new double[1];

		float [][][] data = new float[6][nx][nz];

		double ax = 0;
		double ay = 0;
		double az = 0;
		for (int i=0; i<nx; i++) {
			ax = x0+i*dx;
			for (int j=0; j<nz; j++) {
				az = z0+j*dz;
				SurgiFracTrip(ax, ay, az, fractureHalfLength, fractureHeight/2.0, vlam, vmu, vnu);
				SurgiFracEllcrack(fractureHalfLength, fractureHeight/2.0, 
						vlam[0], vmu[0], vnu[0], g, poisson, acon, ax, ay, az,
						sigx, sigy, sigz, tauxy, tauyz, tauxz);

				data[0][i][j] = (float)sigx[0];
				data[1][i][j] = (float)sigy[0];
				data[2][i][j] = (float)sigz[0];
				data[3][i][j] = (float)tauxy[0];
				data[4][i][j] = (float)tauyz[0];
				data[5][i][j] = (float)tauxz[0];
			}
		};
		return data;
	}
	public void normPaperFigure4_5() {
		double fractureHalfLength = 400; //ft
		double fractureHeight = 100; //ft
		double fracturePressure = 4000; //psi

		double E = 4.e+06;         // Young's modulus of 4,000,000 psi
		double poisson = 0.2;       // Poisson's ratio of 0.25

		double x0 = 0;
		double x1 = 2000.0;
		double dx = 20.0;
		int nx = (int)((x1-x0)/dx);

		double y0 = 0;
		double y1 = 2000.0;
		double dy = 20.0;
		int ny = (int)((y1-y0)/dy);

		double z0 = 0;
		double z1 = 2000.0;
		double dz = 20.0;
		int nz = (int)((z1-z0)/dz);

		double am, ak, acon, g, ek;
		double [] vlam = new double[1];
		double [] vmu = new double[1];
		double [] vnu = new double[1];

		g = E / (2.0 * (1.0 + poisson));      // shear modulus
		//	     GET MULTIPLYING CONSTANT WITHOUT ELLIPTIC INTEGRAL
		acon = fractureHalfLength * 0.25 * fractureHeight * fractureHeight * fracturePressure / (16.0 * g);
		//	     ALSO NEED ELLIPTIC INTEGRAL FOR MULTIPLYING CONSTANT
		ak = Math.sqrt(fractureHalfLength * fractureHalfLength - 0.25 * fractureHeight * fractureHeight) / fractureHalfLength;
		//	     NOW GET THE COMPLEMENTARY PARAMETER (NOT MODULUS)
		am = 1.0 - ak * ak;
		ek = SurgiFracEllint(am);
		if(ek != 0.0) acon = acon / ek;

		//double [] dS = new double[6]; //{sigx, sigy, sigz, tauxy, tauyz, tauxz}
		double [] sigx = new double[1];
		double [] sigy = new double[1];
		double [] sigz = new double[1];
		double [] tauxy = new double[1];
		double [] tauyz = new double[1];
		double [] tauxz = new double[1];

		float [][] data = new float[nx][nz];

		double ax = 0;
		double ay = 0;
		double az = 0;
		for (int i=0; i<nx; i++) {
			ax = x0+i*dx;
			for (int j=0; j<nz; j++) {
				az = z0+j*dz;
				SurgiFracTrip(ax, ay, az, fractureHalfLength, fractureHeight/2.0, vlam, vmu, vnu);
				SurgiFracEllcrack(fractureHalfLength, fractureHeight/2.0, 
						vlam[0], vmu[0], vnu[0], g, poisson, acon, ax, ay, az,
						sigx, sigy, sigz, tauxy, tauyz, tauxz);

				data[i][j] = (float)sigz[0];
			}
		};

		SimplePlot simplePlot = SimplePlot.asPixels(data);
		//simplePlot.setColorModel(ColorMap.HUE_BLUE_TO_RED);;
		//simplePlot.addColorBar(ColorMap.HUE_BLUE_TO_RED);

		//		Sampling s1 = new Sampling(nz,dz,z0);
		//		Sampling s2 = new Sampling(ny,dy,y0);
		//
		//		PlotPanel panel = new PlotPanel(1,1);
		//		PixelsView pv0 = panel.addPixels(0,0,s1,s2,data);
		//		pv0.setInterpolation(PixelsView.Interpolation.NEAREST);
		//		pv0.setColorModel(ColorMap.JET);
		//		//pv0.setPercentiles(0.0f,100.0f);
		//		//f = mul(10.0f,f);
		//		//pv0.set(f);
		//
		//		PlotFrame frame = new PlotFrame(panel);
		//		frame.setDefaultCloseOperation(PlotFrame.EXIT_ON_CLOSE);
		//		frame.setVisible(true);
		//		//frame.paintToPng(300,6,"junk.png");


		Grid2D grid2D = new Grid2D(1, ny, nz, dy, dz, y0, z0, data);

	}

	public void crack2DColorMapTest() {
		//******* Calculate the stresses due to the 2D crack case **********
		double pfrac = 500.0;      // pressure of 500 psi
		double lfrac = 400.0;      // wing length 
		double hfrac = 226.0;      // total fracture height 
		double E = 4.e+06;         // Young's modulus of 4,000,000 psi
		double poisson = 0.25;     // Poisson's ratio of 0.25
		double x = 0.0;            // x location 
		double y = 100.0;          // y location  
		double x0 = 0;
		double x1 = 2000.0;
		double dx = 20.0;
		double nx = (int)((x1-x0)/dx);

		double [] dS = new double[4]; //{dSSx, dSSy, dTauxy, dSSv}
		//	    for (int i=0; i <nx; i++) {
		//	    	x = x0+i*dx;
		//	    	elliptical2D(pfrac, E, poisson, x, y, lfrac, dS);   
		//	        System.out.println("x="+x+" y="+y+" dSSx="+dS[0]+" dSSy="+dS[1]+" dTauxy="+dS[2]+" dSSv="+dS[3]);
		//	    };

		int iUnit = 1;
		int ny = 100; 
		int nz= 50;
		double dy = 20;
		double dz = 20;
		double y0 = -1000;
		double z0 = -500;
		float [][] data = new float[ny][nz];

		double ay = 0;
		double az = 0;
		for (int i=0; i<ny; i++) {
			ay = y0+i*dy;
			for (int j=0; j<nz; j++) {
				az = z0+j*dz;
				crack2D(pfrac, E, poisson, az, ay, lfrac, dS);
				data[i][j] = (float)dS[1];
			}
		};

		SimplePlot.asPixels(data).addColorBar();

		//		Sampling s1 = new Sampling(nz,dz,z0);
		//		Sampling s2 = new Sampling(ny,dy,y0);
		//
		//		PlotPanel panel = new PlotPanel(1,1);
		//		PixelsView pv0 = panel.addPixels(0,0,s1,s2,data);
		//		pv0.setInterpolation(PixelsView.Interpolation.NEAREST);
		//		pv0.setColorModel(ColorMap.JET);
		//		//pv0.setPercentiles(0.0f,100.0f);
		//		//f = mul(10.0f,f);
		//		//pv0.set(f);
		//
		//		PlotFrame frame = new PlotFrame(panel);
		//		frame.setDefaultCloseOperation(PlotFrame.EXIT_ON_CLOSE);
		//		frame.setVisible(true);
		//		//frame.paintToPng(300,6,"junk.png");


		Grid2D grid2D = new Grid2D(iUnit, ny, nz, dy, dz, y0, z0, data);

	}



	public String toString() {

		String a = "* * * * * * * * * * * * * * * * *               ";
		String b = "";
		b = b.concat(a+"\n");

		a = "      CRACK LENGTH --->            *            "; 	b = b.concat(a+"\n");
		a = "* * * * * * * * * * * * * * * * *               "; 	b = b.concat(a+"\n");
		a = "                     ^                          "; 	b = b.concat(a+"\n");
		a = "                     |                          "; 	b = b.concat(a+"\n");
		a = "                     Z                          "; 	b = b.concat(a+"\n");
		a = "                     ^                          "; 	b = b.concat(a+"\n");
		a = "                     |                          "; 	b = b.concat(a+"\n");
		a = "                     |                          "; 	b = b.concat(a+"\n");
		a = "<-------- X -------->+ OBSERVATION POINT        "; 	b = b.concat(a+"\n");
		a = "* * * * * * * * * * * * * * * * *               "; 	b = b.concat(a+"\n");
		a = "                      ^                      "; 	b = b.concat(a+"\n");
		a = "       *    ---       |                      "; 	b = b.concat(a+"\n");
		a = "      * *    ^        Y                      "; 	b = b.concat(a+"\n");
		a = "     *   *   |        |                      "; 	b = b.concat(a+"\n");
		a = "    *     *  |        |       SIDE VIEW      "; 	b = b.concat(a+"\n");
		a = "    *     * HEIGHT  -----                    "; 	b = b.concat(a+"\n");
		a = "     *   *   |                               "; 	b = b.concat(a+"\n");
		a = "      * *    |                               "; 	b = b.concat(a+"\n");
		a = "       *    ---                              "; 	b = b.concat(a+"\n");
		a = "                                             "; 	b = b.concat(a+"\n");
		a = " INPUT POISSONS RATIO: "+_poissonsRatio; 	b = b.concat(a+"\n");
		a = " INPUT YOUNGS MODULUS: "+_youngsModulus; 	b = b.concat(a+"\n");
		//a = " INPUT THE NET PRESSURE: "+_; 	b = b.concat(a+"\n\n");

		a = " Results: "; 	b = b.concat(a+"\n");
		//a = "Easting, Northing, Down, sigx, sigy, sigz, tauxy, tauyz, tauxz"; 	b = b.concat(a+"\n");
		a = "Easting, Northing, Down, dudx, dudy, dudz, dvdx, dvdy, dvdz, dwdx, dwdy, dwdz"; 	b = b.concat(a+"\n");
		double factor = 1.0e6;
		for(int i=0, k=0; i<_vcPW.getNumOfEvents(); i++) {
			for(int j=0; j<_vcPW.getFlag(i); j++, k++) {	
				a = _vcPW.getRE(k)+", "+_vcPW.getRN(k)+", "+_vcPW.getRD(k)+", "+ 
						_vcPW.getData(k, 0)*factor+", "+	_vcPW.getData(k, 1)*factor+", "+_vcPW.getData(k, 2)*factor+", "+
						_vcPW.getData(k, 3)*factor+", "+_vcPW.getData(k, 4)*factor+", "+_vcPW.getData(k, 5)*factor+", "+
						_vcPW.getData(k, 6)*factor+", "+_vcPW.getData(k, 7)*factor+", "+_vcPW.getData(k, 8)*factor; 	
				b = b.concat(a+"\n");
			}
		}

		return b;
	}
}
