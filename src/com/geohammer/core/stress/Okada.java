package com.geohammer.core.stress;

import com.geohammer.core.acquisition.VCPair;
import com.geohammer.core.planarmodel.DipLayer1D;
import com.geohammer.core.planarmodel.Grid2D;
import com.geohammer.core.planarmodel.Layer3D;

//useful web
//https://www.unavco.org/software/modeling/3d-def/manual.html
//https://pubs.usgs.gov/tm/13/b1/pdf/tm13-b1.pdf
//https://pangea.stanford.edu/cdfm/software
//https://avo.alaska.edu/Software/Kilauea/software.html
//http://www.bosai.go.jp/study/application/dc3d/DC3Dhtml_E.html


public class Okada extends OkadaCore {

	private Layer3D 			_layer3D 			= null;
	private VCPair 				_vcPW 				= null;
	private double 				_alpha 				= 2.0/3.0;
	
	private double 				_halfLength			= 1.0;
	private double 				_halfHeight			= 1.0;
	private double 				_ellipseApex		= 1.0;
	
	private double 				_recLength			= 1.0;
	private double 				_recWidth			= 1.0;
	private double 				_recSlip    		= 1.0;
	
	private double 				_strike				= 1.0;
	private double 				_dip    			= 1.0;
	
	private double 				_PI  				= 3.1415926;
	
	private Grid2D 				_grid2D 			= null;
	
	public Okada(DipLayer1D dipLayer1D, VCPair vcPW, Layer3D layer3D) {
		_vcPW 		= vcPW;
		_layer3D 	= layer3D;
		if(dipLayer1D==null)  	_alpha 		= 2.0/3.0;
		else 					_alpha 		= calAlpha(dipLayer1D);
	}
	
	public Okada(VCPair vcPW, double alpha) {
		_vcPW 		= vcPW;
		_alpha 		= alpha;
	}
	
	public VCPair getVCPair() { return _vcPW; }
	//uz
	public void calSurfaceDisplacementZ(double centerDepth, double L, double W, double U, double strike, double dip, int faultType) {
		double [] xtilt = new double[1];
		
	    int 	nn = _layer3D.getLayer(0).getNx();
	    double 	dn = _layer3D.getLayer(0).getDx();
	    double 	n0 = _layer3D.getLayer(0).getX0();
		int 	ne = _layer3D.getLayer(0).getNy();
	    double 	de = _layer3D.getLayer(0).getDy();
	    double 	e0 = _layer3D.getLayer(0).getY0();
		
	    float [][] data = _layer3D.getLayer(0).getDepth();
	    double rn = 0;
	    double re = 0;
	    for (int ie=0; ie<ne; ++ie) {
	    	re = e0+ie*de;
	    	for (int in=0; in<nn; ++in) {
	    		rn = n0+in*dn;
	    		calDisplacementZ(0, 0, centerDepth, rn, re, 0, L, W, U, strike, dip, xtilt, faultType, _alpha);
	    		data[ie][in] = (float)(xtilt[0]*1.0e6);
	    	}
	    }
	}

	public void calSurfaceDisplacementZGroup(double centerDepth, double L, double W, double U, double strike, double dip, int faultType) {
		double [] xtilt = new double[1];
		
	    int 	nn = _layer3D.getLayer(0).getNx();
	    double 	dn = _layer3D.getLayer(0).getDx();
	    double 	n0 = _layer3D.getLayer(0).getX0();
		int 	ne = _layer3D.getLayer(0).getNy();
	    double 	de = _layer3D.getLayer(0).getDy();
	    double 	e0 = _layer3D.getLayer(0).getY0();	    

		double sinAz = Math.sin((90-strike)*Math.PI/180.0);
		double cosAz = Math.cos((90-strike)*Math.PI/180.0);
		double sinDip = Math.sin(dip*Math.PI/180.0);
		double cosDip = Math.cos(dip*Math.PI/180.0);
		
		double dL = 0.5*L;
		double dW = 0.5*W*cosDip;
		double C = centerDepth+0.5*W*sinDip;
		
	    float [][] data = _layer3D.getLayer(0).getDepth();
	    double sn = 0;
	    double se = 0;
	    double rn = 0;
	    double re = 0;
	    for (int ie=0; ie<ne; ++ie) {
	    	re = e0+ie*de;
	    	re -= se; 
	    	for (int in=0; in<nn; ++in) {
	    		rn = n0+in*dn;
	    		rn -= sn;
	    		//calDisplacementZ(0, 0, centerDepth, rn, re, 0, L, W, U, strike, dip, xtilt, faultType, _alpha);
	    		double ae = re * cosAz + rn * sinAz;
	    		double an = -re * sinAz + rn * cosAz;
	    			
	    		double be = ae+dL;
	    		double bn = an+dW;
	    		double bz = 0;
	    		
	    		calDisplacementZ(C, L, W, U, dip, be, bn, bz, xtilt, faultType, _alpha);
	    		data[ie][in] = (float)(xtilt[0]*1.0e6);
	    	}
	    }
	}
	
	public void calSurfaceTiltGroup(double centerDepth, double L, double W, double U, double strike, double dip, int faultType) {
		double [] tilt = new double[2];
		
	    int 	nn = _layer3D.getLayer(0).getNx();
	    double 	dn = _layer3D.getLayer(0).getDx();
	    double 	n0 = _layer3D.getLayer(0).getX0();
		int 	ne = _layer3D.getLayer(0).getNy();
	    double 	de = _layer3D.getLayer(0).getDy();
	    double 	e0 = _layer3D.getLayer(0).getY0();	    

		double sinAz = Math.sin((90-strike)*Math.PI/180.0);
		double cosAz = Math.cos((90-strike)*Math.PI/180.0);
		double sinDip = Math.sin(dip*Math.PI/180.0);
		double cosDip = Math.cos(dip*Math.PI/180.0);
		
		double dL = 0.5*L;
		double dW = 0.5*W*cosDip;
		double C = centerDepth+0.5*W*sinDip;
		
	    float [][] data = _layer3D.getLayer(0).getDepth();
	    float [][] data1 = _layer3D.getLayer(1).getDepth();
	    double sn = 0;
	    double se = 0;
	    double rn = 0;
	    double re = 0;
	    for (int ie=0; ie<ne; ++ie) {
	    	re = e0+ie*de;
	    	re -= se; 
	    	for (int in=0; in<nn; ++in) {
	    		rn = n0+in*dn;
	    		rn -= sn;
	    		//calDisplacementZ(0, 0, centerDepth, rn, re, 0, L, W, U, strike, dip, xtilt, faultType, _alpha);
	    		double ae = re * cosAz + rn * sinAz;
	    		double an = -re * sinAz + rn * cosAz;
	    			
	    		double be = ae+dL;
	    		double bn = an+dW;
	    		double bz = 0;
	    		
	    		calPartialxyOverZ(C, L, W, U, dip, be, bn, bz, tilt, faultType, _alpha);
	    		double a = tilt[0]*1.0e6;
	    		double b = tilt[1]*1.0e6;
	    		//data[ie][in] = (float)(Math.sqrt(a*a+b*b));
	    		data[ie][in] = (float)(a);
	    		data1[ie][in] = (float)(b);
	    	}
	    }
	}
	
	//partialUxPartialZ and partialUyPartialZ	
	public void calDownHoleTilt(double C, double L, double W, double U, double strike, double dip, int faultType) {
		double se, sn, sd, re, rn, ru;
		double [] tilt = new double[2];
		for(int i=0, k=0; i<_vcPW.getNumOfEvents(); i++) {
			se = _vcPW.getEE(i);
			sn = _vcPW.getEN(i);
			sd = _vcPW.getED(i);
			for(int j=0; j<_vcPW.getFlag(i); j++, k++) {	
				re = _vcPW.getRE(k)-se;
				rn = _vcPW.getRN(k)-sn;				
				ru = -(_vcPW.getRD(k)-sd);
				
				//calPartialxyOverZ(C, L, W, U, dip, re, rn, ru, tilt, faultType, _alpha);
				calPartialxyOverZ(C, L, W, U, strike, dip, re, rn, ru, tilt, faultType, _alpha);
				
				//If the orientation is 0 , then TiltX is +East and TiltY is +North
				//If the orientation is 90, then TiltX is -Northing and TiltY is +Easting
				//The tiltmeter orientation is the azimuth of +Y.
				//Positive tiltx means the top of the instrument moved in the x direction (+Easting) relative 
				//to the bottom of the instrument.  Similarly for Y.
				_vcPW.setData(k, _vcPW.getCalPT(), tilt[0]*1.0e6);
				_vcPW.setData(k, _vcPW.getCalST(), tilt[1]*1.0e6);
			}
		}
	}
	
	public float[] figure7() {
		double L = 20000.0; //m
		double W = 10000.0; // m
		double U = 0.5; // m
		double strike = 90.0;
		double dip = 90.0;
		double c = 20000.0; //m
		int faultType = 2;
		calSurfaceTiltGroup(c, L, W, U, strike, dip, faultType);
		float a = 5.0E5f; //0.5E10
		float[] xyz = _grid2D.makeTriangles(a);
		
	    return xyz;	    
	}	
	
	public float[] figure7BackUp() {
		double L = 20000.0; //m
		double W = 10000.0; // m
		double U = 0.5; // m
		double dip = 90.0;
		double c = 20000.0; //m
		_alpha 		= 2.0/3.0;
		double se, sn, su, re, rn, ru;
		double [][] xtilt = new double[4][1];
		double [][] ytilt = new double[4][1];
		
		int ne = _grid2D.getNz();
	    double de = _grid2D.getDz();
	    double e0 = _grid2D.getZ0();
	    int nn = _grid2D.getNy();
	    double dn = _grid2D.getDy();
	    double n0 = _grid2D.getY0();
	    
	    float a = 5.0E5f; //0.5E10
	    int faultType = 2;
	    //System.out.println(_grid2D.toString(1));
	    float[] xyz = new float[3*6*ne*nn];
	    for (int in=0,i=0; in<nn; ++in) {
	    	float x0 = (float)(n0+in*dn);
	    	float x1 = (float)(x0+dn);
	    	for (int ie=0; ie<ne; ++ie) {
	    		float y0 = (float)(e0+ie*de);
	    		float y1 = (float)(y0+de);
	    		
//				calZderivatives(c, L, W, U, dip, y0, x0, 0, xtilt[0], ytilt[0], faultType, _alpha);
//				calZderivatives(c, L, W, U, dip, y1, x0, 0, xtilt[1], ytilt[1], faultType, _alpha);
//				calZderivatives(c, L, W, U, dip, y0, x1, 0, xtilt[2], ytilt[2], faultType, _alpha);
//				calZderivatives(c, L, W, U, dip, y1, x1, 0, xtilt[3], ytilt[3], faultType, _alpha);
	    		
	    		calDisplacementZ(c, L, W, U, dip, y0, x0, 0, xtilt[0], faultType, _alpha);
	    		calDisplacementZ(c, L, W, U, dip, y1, x0, 0, xtilt[1], faultType, _alpha);
	    		calDisplacementZ(c, L, W, U, dip, y0, x1, 0, xtilt[2], faultType, _alpha);
	    		calDisplacementZ(c, L, W, U, dip, y1, x1, 0, xtilt[3], faultType, _alpha);
				//System.out.println(xtilt[0][0] + " "+xtilt[1][0]+ " "+xtilt[2][0]+ " "+xtilt[3][0]);
				
				xyz[i++] = x0;  xyz[i++] = y0;  xyz[i++] = (float)(xtilt[0][0]*a);
	    		xyz[i++] = x0;  xyz[i++] = y1;  xyz[i++] = (float)(xtilt[1][0]*a);
	    		xyz[i++] = x1;  xyz[i++] = y0;  xyz[i++] = (float)(xtilt[2][0]*a);
	    		xyz[i++] = x1;  xyz[i++] = y0;  xyz[i++] = (float)(xtilt[2][0]*a);
	    		xyz[i++] = x0;  xyz[i++] = y1;  xyz[i++] = (float)(xtilt[1][0]*a);
	    		xyz[i++] = x1;  xyz[i++] = y1;  xyz[i++] = (float)(xtilt[3][0]*a);
	    		
//	    		xyz[i++] = x0;  xyz[i++] = y0;  xyz[i++] = sin(x0,y0);
//	    		xyz[i++] = x0;  xyz[i++] = y1;  xyz[i++] = sin(x0,y1);
//	    		xyz[i++] = x1;  xyz[i++] = y0;  xyz[i++] = sin(x1,y0);
//	    		xyz[i++] = x1;  xyz[i++] = y0;  xyz[i++] = sin(x1,y0);
//	    		xyz[i++] = x0;  xyz[i++] = y1;  xyz[i++] = sin(x0,y1);
//	    		xyz[i++] = x1;  xyz[i++] = y1;  xyz[i++] = sin(x1,y1);
	    	}
	    }
	    return xyz;
	    
	}
	public void figure8() {
		double L = 12000.0; //m
		double W = 8000.0; // m
		double U = 0.500; // m
		double dip = 40.0;
		double c = 10000.0; //m
		//_alpha = 0.0;
		double se, sn, su, re, rn, ru;
		double [] tilt = new double[2];
		for(int i=0, k=0; i<_vcPW.getNumOfEvents(); i++) {
			se = _vcPW.getEE(i);
			sn = _vcPW.getEN(i);
			su = _vcPW.getED(i);
			for(int j=0; j<_vcPW.getFlag(i); j++, k++) {	
				re = _vcPW.getRE(k)-se;
				rn = _vcPW.getRN(k)-sn;				
				ru = -_vcPW.getRD(k);
				calPartialxyOverZ(c, L, W, U, dip, re, rn, ru, tilt, 0, _alpha);
				//System.out.println("i="+k+" xtitle="+xtilt[0]+" ytitle="+ytilt[0]);
				_vcPW.setData(k, _vcPW.getCalPT(), tilt[0]);
				//_vcPW.setData(k, _vcPW.getObsPT(), ytilt[0]);
				calPartialxyOverZ(c, L, W, U, dip, re, rn, ru, tilt, 1, _alpha);
				_vcPW.setData(k, _vcPW.getCalST(), tilt[0]);
				calPartialxyOverZ(c, L, W, U, dip, re, rn, ru, tilt, 2, _alpha);
				_vcPW.setData(k, _vcPW.getCalSV(), tilt[0]);
			}
		}
	}
	
	public void setEclipse(double halfLength, double halfHeight, double ellipseApex) 		{ 
		_halfLength = halfLength; _halfHeight = halfHeight; _ellipseApex = ellipseApex;
	}
	public void setRectangle(double recLength, double recWidth, double recSlip) 		{ 
		_recLength = recLength; _recWidth = recWidth; _recSlip = recSlip;
	}
	public void setHalfLength(double halfLength) 		{ _halfLength = halfLength; }
	public void setHalfHeight(double halfHeight) 		{ _halfHeight = halfHeight; }
	public void setEllipseApex(double ellipseApex) 		{ _ellipseApex = ellipseApex; }
	
	public void setRecLength(double recLength) 			{ _recLength = recLength; }
	public void setRecWidth(double recWidth) 			{ _recWidth = recWidth; }
	public void setRecSlip(double recSlip) 				{ _recSlip = recSlip; }
	
	public void setStrike(double strike) 				{ _strike = strike; }
	public void setDip(double dip) 						{ _dip = dip; }
	
	public void toRectangleModel() {
		// Our length and height are for an elliptical fracture, 
		// so we must multiply by sqrt(PI/4) to get the rectangular coordinates...
		_recLength 	= _halfLength * Math.sqrt(_PI/4.0);
		_recWidth 	= _halfHeight * Math.sqrt(_PI/4.0);
		_recSlip 	= _ellipseApex;
	}
	
	public void startTest() {
		double c = 0.0;
		
		double sinaz = Math.sin(_PI/180. * _strike );
		double cosaz = Math.cos(_PI/180. * _strike );

		double cosdip = Math.cos(_PI/180. * _dip);
		double sindip = Math.sin(_PI/180. * _dip);
		
		double se, sn, su, re, rn, ru;
		double [] xtilt = new double[1];
		double [] ytilt = new double[1];
		for(int i=0, k=0; i<_vcPW.getNumOfEvents(); i++) {
			se = _vcPW.getEE(i);
			sn = _vcPW.getEN(i);
			su = _vcPW.getED(i);
			for(int j=0; j<_vcPW.getFlag(i); j++, k++) {	
				re = _vcPW.getRE(k)-se;
				rn = _vcPW.getRN(k)-sn;				
				ru = -_vcPW.getRD(k);
				
				// The left corner of Okada's fracture is positioned at the Z axis
				// so must correct by adding half of fracture length to X position...
				// Fix for well offset
				double X = re * cosaz + re * sinaz + _recLength/2.;
				//     and must correct by adding half of fracture height * cos dip to Y position...
				double Y = rn * sinaz - rn * cosaz + _recWidth/2. * cosdip;
				double Z = ru;
				c = su + _recWidth/2. * sindip;
				calcOkadaSite(c, _recLength, _recWidth, _recSlip, _dip, re, rn, ru, xtilt, ytilt, 0, _alpha);
				//System.out.println("i="+k+" xtitle="+xtilt[0]+" ytitle="+ytilt[0]);
				_vcPW.setData(k, _vcPW.getCalPT(), xtilt[0]);
				_vcPW.setData(k, _vcPW.getCalST(), ytilt[0]);
			}
		}
	}


	public int test() 	{
		int N=1;
		double fracX,fracY,fracZ,azimuth,dip,length,height,width,depth;
		double [] xtilt = new double[1];
		double [] ytilt = new double[1];
		double [] xpoint = new double[50];
		double [] ypoint = new double[50];
		double [] zpoint = new double[50];
		double strikeslip,dipslip;

		fracX=10.;
		fracY=10.;
		fracZ=-800.;
		azimuth=-77.;
		dip=80.;
		length=50.*Math.sqrt(4./Math.PI);//this is for elliptical frac length
		height=length;//this is for elliptical frac height
		//   width=1.0; //inch
		width=0.0338;//feet
		strikeslip=0.00;
		dipslip=-0.00;
		depth=800.;

		//ofstream OutFile ("TestTiltPT.txt",ios::out);

		for (int i=0; i < N; i++)	   {
			xpoint[i]=100.;
			ypoint[i]=200.;
			zpoint[i]=-20.;

			Okada1992(depth,azimuth,dip,length,height,width,strikeslip,dipslip,
					xpoint[i],ypoint[i],zpoint[i],xtilt,ytilt,fracX,fracY);

			//OutFile << xpoint[i]<<' '<<ypoint[i]<<' '<<zpoint[i]<<' '<<xtilt<<' '<<ytilt<<'\n';
			System.out.println("x="+xpoint[i]+" y="+ypoint[i]+" z="+zpoint[i]+" xtilt="+xtilt[0]+" ytilt="+ytilt[0]);

		}
		//OutFile.close();
		return 0;
	}
	
//	public void centerAndENUSystem() {
//		for(int i=0, k=0; i<_vcPW.getNumOfEvents(); i++) {
//			double se = _vcPW.getEE(i);
//			double sn = _vcPW.getEN(i);
//			
//			for(int j=0; j<_vcPW.getFlag(i); j++, k++) {	
//				_vcPW.setData(k, _vcPW.getRE(), _vcPW.getRE(k)-se);
//				_vcPW.setData(k, _vcPW.getRN(), _vcPW.getRN(k)-sn);
//				_vcPW.setData(k, _vcPW.getRD(), -_vcPW.getRD(k));
//			}
//			_vcPW.setData(i, _vcPW.getEE(), _vcPW.getEE(i)-se);
//			_vcPW.setData(i, _vcPW.getEN(), _vcPW.getEN(i)-sn);
//			_vcPW.setData(i, _vcPW.getED(), -_vcPW.getED(i));
//		}
//	}
	
	private double  calAlpha(DipLayer1D dipLayer1D) {
		double vp	= dipLayer1D.getLayer(0).getVp();      // P-wave velocity m/s
		double vs	= dipLayer1D.getLayer(0).getVs();      // S-wave velocity m/s 
		double den	= dipLayer1D.getLayer(0).getDen();    	// density g/m^3
		double qp 	= dipLayer1D.getLayer(0).getQp();
		double qs 	= dipLayer1D.getLayer(0).getQs();
		//System.out.println("vp="+vp+" vs="+vs+" den="+den+" qp="+qp+" qs="+qs);
		
		double a = vp*vp*den; //lamda+2mu
		double mu = vs*vs*den; //mu
		double lamda = a-2.0*mu;
		return (lamda+mu)/a;
	}
	
}
