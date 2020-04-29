package com.geohammer.core.stress;

import com.geohammer.complex.Complex;

//import org.apache.commons.math3.complex.Complex;

//G:\TFS\DuDevelopment\FracNetwork\SRC\EllipFracStressCalc.cpp
//C:\arima\conference\2018\MS_Tilt_JointInversion\FracStressCalculationDevelopLog.docx

public class EllipticalStressCore {

	protected double PI  = 3.14159265358979;

	public EllipticalStressCore() { super(); }

	// Variables:
	// Depth    - fracture depth, positive number
	// Azimuth  - fracture azimuth
	// Dip      - fracture dip
	// Pitch    - fracture pitch
	// pfrac    - average pressure in the fracture (any units ok)
	// lfrac    - fracture wing length (any length unit ok)
	// hfrac    - total fracture height (all length units must be the same, but they can be anything)
	// E        - Young's modulus (units must be the same as pfrac, but any units ok)
	// poiss    - Poisson's ratio
	// sigx, sigy, sigz, tauxy, tauyz, tauxz
	// Convention on stresses:  positive is compression

	// To call this function, the coordinates (x,y,z) is our regular coordinate system, (EW,NS,UP).
	// The free surface is at z=0. But inside the stress calculation routine (SurgiFracEllcrack),
	// all the equations are in the coordinates used in Green & Sneddon's paper, which is:
	//	      x - distance along length of frac, measured from center of frac
	//	      y - distances along height of frac, measured from center of frac 
	//	      z - distance normal to fracture plane, measured from fracture
	// so the coordinates passed in this function EllipFracStressCalc() are transformed into the
	// coordinates used in the paper, after finishing the stress calculation, the calculated stresses
	// are transformed back to our regular coordinate system (EW,NS,UP).

	//********************************************************************************
	//		Calculate the stresses at one observation point (xpoint,ypoint,zpoint) due to
	//		fracture located at (Xoffset,Yoffset,-Depth) based on the analytical solution of 
	//		the elliptical fracture model (Green & Sneddon model) in a whole space.
	//*******************************************************************************
	public void EllipFracStressCalc(
			double Depth, double Azimuth, double Dip, double Pitch, 
			double pfrac, double lfrac, double hfrac,
			double E, double poiss,
			double xpoint, double ypoint, double zpoint,
			double XOffset, double YOffset,
			double [] sigx, double [] sigy, double [] sigz, 
			double [] tauxy, double [] tauyz, double [] tauxz)
	{
		double am, ak, acon, g, ek;
		double [] vlam = new double[1];
		double [] vmu = new double[1];
		double [] vnu = new double[1];

		if (pfrac <= 0. || lfrac <= 0. || hfrac <= 0. || E <= 0. || poiss <= 0.) 
		{
			sigy[0]=0;
			return;
		}

		if (lfrac == hfrac/2.0) hfrac=0.9999*hfrac;  // fracture height and length can't be equal

		boolean DimensionsSwitchFlag = false;
		if (lfrac < hfrac/2.0) DimensionsSwitchFlag = true;

		if (DimensionsSwitchFlag) {
			double temp = lfrac;
			lfrac = hfrac/2.0;
			hfrac = 2.0*temp;
		}
		g = E / (2.0 * (1.0 + poiss));      // shear modulus
		//	     GET MULTIPLYING CONSTANT WITHOUT ELLIPTIC INTEGRAL
		acon = lfrac * 0.25 * hfrac * hfrac * pfrac / (16.0 * g);
		//	     ALSO NEED ELLIPTIC INTEGRAL FOR MULTIPLYING CONSTANT
		ak = Math.sqrt(lfrac * lfrac - 0.25 * hfrac * hfrac) / lfrac;
		//	     NOW GET THE COMPLEMENTARY PARAMETER (NOT MODULUS)
		am = 1.0 - ak * ak;
		ek = SurgiFracEllint(am);
		if(ek != 0.0)
			acon = acon / ek;

		// **************** Coordinates transformation ********************
		double X, Y, Z, sinaz, cosaz, Theta, cosTheta, sinTheta;
		double Alpha, cosAlpha, sinAlpha;

		sinaz = Math.sin(3.14159265358979/180. * Azimuth );
		cosaz = Math.cos(3.14159265358979/180. * Azimuth );

		Theta=Dip-90;
		cosTheta = Math.cos(3.14159265358979/180. * Theta);
		sinTheta = Math.sin(3.14159265358979/180. * Theta);

		Alpha=Pitch;
		cosAlpha = Math.cos(3.14159265358979/180. * Alpha);
		sinAlpha = Math.sin(3.14159265358979/180. * Alpha);

		//Coordinate system transformation 1-- changing azimuth and fracture depth
		double X1, Y1, Z1;
		X1 = (xpoint - XOffset) * cosaz - (ypoint-YOffset) * sinaz;
		Y1 = (xpoint - XOffset) * sinaz + (ypoint-YOffset) * cosaz;
		Z1 = (zpoint + Depth);

		//Coordinate system transformation 2-- changing to XYZ in Norm's equations in document
		double X2,Y2,Z2;
		X2 = Y1;
		Z2 = X1;
		Y2 = Z1;

		//Before calling the trip function, we need to taking the dip into account for (X,Y,Z)
		//Coordinate system transformation 3-- considering fracture dip
		double X3,Y3,Z3;
		X3=X2;
		Y3=Y2*cosTheta+Z2*sinTheta;
		Z3=-Y2*sinTheta+Z2*cosTheta;

		//Coordinate system transformation 4-- considering fracture pitch (Alpha)
		X=X3*cosAlpha-Y3*sinAlpha;
		Y=X3*sinAlpha+Y3*cosAlpha;
		Z=Z3;
		// **************** end of coordinates transformation *************
		if (DimensionsSwitchFlag) // fracture length is shorter than height
		{ // switch X and Y, and related stresses component   
			//	      SurgiFracTrip(Y, X, Z, lfrac, hfrac / 2.0, &vlam, &vmu, &vnu);
			//	      SurgiFracTripNew(Y, X, Z, lfrac, hfrac / 2.0, vlam, vmu, vnu);
			//SurgiFracEllcrack(lfrac, hfrac / 2.0, vlam, vmu, vnu, g, poiss, acon, Y, X, Z,
			//    sigy, sigx, sigz, tauxy, tauxz, tauyz);
			double [] sigxtemp = new double[1];
			double [] sigytemp = new double[1];
			double [] sigztemp = new double[1];
			double [] tauxytemp = new double[1];
			double [] tauyztemp = new double[1];
			double [] tauxztemp = new double[1];
			//double sigxtemp, sigytemp,sigztemp,tauxytemp,tauyztemp,tauxztemp;
			SurgiFracTrip(Y, -X, Z, lfrac, hfrac / 2.0, vlam, vmu, vnu);
			SurgiFracEllcrack(lfrac, hfrac / 2.0, vlam[0], vmu[0], vnu[0], g, poiss, acon, Y, -X, Z,
					sigxtemp, sigytemp, sigztemp, tauxytemp, tauyztemp, tauxztemp);
			sigx[0] = sigytemp[0];
			sigy[0] = sigxtemp[0];
			sigz[0] = sigztemp[0];
			tauxy[0] = -tauxytemp[0];
			tauyz[0] = tauxztemp[0];
			tauxz[0] = -tauyztemp[0];
		} else 	{// fracture length is greater than height, keep 
			SurgiFracTrip(X, Y, Z, lfrac, hfrac / 2.0, vlam, vmu, vnu);
			//	      SurgiFracTripNew(X, Y, Z,lfrac, hfrac / 2.0, vlam, vmu, vnu);
			SurgiFracEllcrack(lfrac, hfrac / 2.0, vlam[0], vmu[0], vnu[0], g, poiss, acon, X, Y, Z,
					sigx, sigy, sigz, tauxy, tauyz, tauxz);
		}
		// **************** Transform the stresses back to the input coordinate system ****** 
		// Transformation matrix from local (fracture) coordinates into global (EW,NS,UP) 
		double [][] dAtrans = new double[3][3];    // transformation matrix
		double [][] dStresses= new double[3][3];
		double [][] dStressesGlobal= new double[3][3];

		dStresses[0][0]=sigx[0];
		dStresses[1][1]=sigy[0];
		dStresses[2][2]=sigz[0];
		dStresses[0][1]=tauxy[0];
		dStresses[0][2]=tauxz[0];
		dStresses[1][2]=tauyz[0];
		dStresses[1][0]=dStresses[0][1];
		dStresses[2][1]=dStresses[1][2];
		dStresses[2][0]=dStresses[0][2];

		dAtrans[0][0] = sinaz*cosAlpha-cosaz*sinTheta*sinAlpha;
		dAtrans[0][1] = sinaz*sinAlpha+cosaz*sinTheta*cosAlpha;
		dAtrans[0][2] = cosaz*cosTheta;

		dAtrans[1][0] = cosaz*cosAlpha+sinaz*sinTheta*sinAlpha;
		dAtrans[1][1] = cosaz*sinAlpha-sinaz*sinTheta*cosAlpha;
		dAtrans[1][2] = -sinaz*cosTheta;

		dAtrans[2][0] = -cosTheta*sinAlpha;
		dAtrans[2][1] = cosTheta*cosAlpha;
		dAtrans[2][2] = -sinTheta;

		int i, j, k, m;
		double [][] dTempMatrix = new double[3][3];
		double dTemp;
		// Calculate the product of A*dStresses
		for ( i=0; i < 3; i++)		{
			for ( m=0; m < 3; m++)			{
				dTemp=0.0;
				for ( k=0; k < 3; k++)	 dTemp=dTemp+dAtrans[i][k]*dStresses[k][m]; 
				dTempMatrix[i][m]=dTemp;
			};
		};
		// Calculate the final product of A*dStresses*A'
		for ( i=0; i < 3; i++)		{
			for ( j=0; j < 3; j++)			{
				dTemp=0.0;
				for ( m=0; m < 3; m++) dTemp=dTemp+dTempMatrix[i][m]*dAtrans[j][m]; 
				dStressesGlobal[i][j]=dTemp;
			};
		};
		// **************** end of coordinates transformation **************************

		// assign the updated stress back to those variable passing out
		sigx[0]=dStressesGlobal[0][0];
		sigy[0]=dStressesGlobal[1][1];
		sigz[0]=dStressesGlobal[2][2];
		tauxy[0]=dStressesGlobal[0][1];
		tauyz[0]=dStressesGlobal[1][2];
		tauxz[0]=dStressesGlobal[0][2];
	}//EllipFracStressCalc()


	//**********************************************************************************
	// One important note on ellcrack.  These equations are based on a paper by Green and
	// Sneddon.  To preserve the correspondence between the paper and the equations used here,
	// their conventions were employed.  Most importantly, the geometry was set up so that x
	// is along the fracture length, y is along the fracture height, and z is normal to fracture.
	// This was done so that an imaginary value, x+iy, could be employed.
	public void SurgiFracEllcrack(double a, double b, double vlam, double vmu, double vnu,
			double g, double poiss, double acon, double x, double y,
			double z, double []ssx, double []ssy, double []ssz, double []txy,
			double []tyz, double []txz)
	{
		double ak, akp, snu, cnu, dnu, snu2, u, euk;
		double sigminus, sigsum;
		double term1, term2, term3, term4, term5, add1, add2, add3, add4, add5;
		double h1sq;
		double dldx, dldy, dldz, dldz2, dldx2, dldy2;
		double parts, dudl, dudl2;
		double dldxdy, dldxdz, dldydz;   //used for shear stress calculation

		//   get the values of the jacobian elliptic functions
		ak = Math.sqrt(a * a - b * b) / a;
		akp = Math.sqrt(1.0 - ak * ak);
		snu2 = a * a / (a * a + vlam);
		snu = Math.sqrt(snu2);
		cnu = Math.sqrt(1.0 - snu2);
		dnu = Math.sqrt(1.0 - ak * ak * snu2);
		//	     get the value of u from snu
		u = SurgiFracGetu(snu, ak);
		//	     get the value of the incomplete elliptic integral, e(u,k)
		double phi;
		phi=Math.asin(snu);
		euk = SurgiFracGeteukD2(phi, ak);
		//   euk = SurgiFracGeteukD2(snu, ak);  //wrong way to use the routine
		//euk = SurgiFracGeteuk(snu, ak);    //less accurate
		//	     one singular case -- vlam --> 0
		//	Deal with special case where the general solution doesn't apply
//		When Z=0 and inside the ellipse (x^2/a^2+y^2/b^2<1)
//		VLAM=0, all the derivaties have to be calculated specifically
//		using th solution of displacement in z direction w(x,y)
		if(Math.abs(vlam) < 0.00001)
		{
			term1 = -euk;
			term4 =(-euk * (1.0 + 1.0 / (akp*akp)) + 2.0 * u) / (ak * ak);
			add1 = 2.0 * acon / (a * b * b) * term1;
			sigsum = -8.0 * g * ((1.0 + 2.0 * poiss) * add1);
			add1 = -add1;
			ssz[0] = 8.0 * g * (add1);    // perpendicular to fracture plane
			add1 = acon / (2.0 * a * a * a) * term4;
			sigminus = 32.0 * g * ((1.0 - 2.0 * poiss) * add1);
			txz[0] = 0.0;
			txy[0] = 0.0;
			tyz[0] = 0.0;
		}
		else
		{
			//	     start with general functions
			h1sq = (vlam - vmu) * (vlam - vnu) / (4.0 * (a * a + vlam)
					* (b * b + vlam) * vlam);
			dldy = 2.0 * y * vlam * (a * a + vlam) / ((vlam - vmu) * (vlam - vnu));
			dldx = 2.0 * x * vlam * (b * b + vlam) / ((vlam - vmu)*(vlam - vnu));
			dldz = 2.0 * z *(a * a + vlam) * (b * b + vlam) / ((vlam - vmu)
					* (vlam - vnu));
			parts = x * x / Math.pow((a * a + vlam), 3) + y * y / Math.pow((b * b + vlam), 3)
					+ z * z / (vlam * vlam * vlam);
			dldx2 = (-x * x / (Math.pow((a * a + vlam), 3) * h1sq) + 1.0 / (a * a + vlam))
					/ (2.0 * h1sq) + (parts * x * x / (2.0 * Math.pow((a * a + vlam), 2)
							* h1sq * h1sq)) / (4.0 * h1sq);			
			dldy2 = (-y * y / (Math.pow((b * b + vlam), 3) * h1sq) + 1.0 / (b * b + vlam))
					/ (2.0 * h1sq) + (parts * y * y / (2.0 * Math.pow((b * b + vlam), 2)
							* h1sq * h1sq)) / (4.0 * h1sq);
			dldz2 = (-z * z / (vlam * vlam * vlam * h1sq) + 1.0 / vlam) / (2.0 * h1sq)
					+ (parts * z * z / (2.0 * vlam * vlam * h1sq * h1sq)) / (4.0 * h1sq);
			//the following lines are for shear stress calculation
			dldxdy = (-(a * a + b * b + 2.0 * vlam) / (Math.pow((a * a + vlam), 2)
					* Math.pow((b * b + vlam), 2)) * x * y / h1sq + 0.50 / (h1sq * h1sq)
					* parts * x * y / ((a * a + vlam) * (b * b + vlam))) / (4.0 * h1sq);
			dldydz = (-(b * b + 2.0 * vlam) / (vlam * vlam * Math.pow((b * b + vlam), 2))
					* y * z / h1sq + 0.5 / (h1sq * h1sq) * parts * y * z
					/ (vlam * (b * b + vlam))) / (4.0 * h1sq);
			dldxdz = (-(a * a + 2.0 * vlam) / (vlam * vlam * Math.pow((a * a + vlam), 2))
					* x * z / h1sq + 0.5 / (h1sq * h1sq) * parts * x * z
					/ (vlam * (a * a + vlam))) / (4.0 * h1sq);
			// end of terms for shear stress calculation
			dudl = -a / (2.0 * Math.sqrt((a * a + vlam) * vlam * (vlam
					+ a * a * (1.0 - ak * ak))));
			dudl2 = -(3.0 * snu2 + snu2 * snu2 / (cnu * cnu) + ak * ak * snu2 * snu2
					/ (dnu * dnu)) / (2.0 * a * a) * dudl;
			//	     first term is sigx + sigy
			term1 = snu * dnu / cnu - euk;
			term2 = -ak * ak * snu2 + snu2 * dnu * dnu / (cnu * cnu);
			term3 = akp * akp * snu * dnu / (cnu * cnu * cnu);
			add1 = 2.0 * acon / (a * b * b) * term1;
			add2 = 2.0 * acon * z / (a * b * b) * term2 * dudl * dldz;
			add3 = 4.0 * acon * z / (a * b * b) * term2 * dudl * dldz;
			add4 = 4.0 * acon * z * z / (a * b * b) * term3 * dudl * dudl * dldz * dldz;
			add5 = 2.0 * acon * z * z / (a * b * b) * term2 * (dudl2 * dldz * dldz
					+ dudl * dldz2);
			sigsum = -8.0 * g * ((1.0 + 2.0 * poiss) * (add1 + add2) + add3 + add4 + add5);
			//	     next term is sigz
			add1 = -2.0 * acon / (a * b * b) * term1;
			add2 = -2.0 * acon * z / (a * b * b) * term2 * dudl * dldz ;
			add3 = 4.0 * acon * z / (a * b * b) * term2 * dudl * dldz;
			add4 = 4.0 * acon * z * z / (a * b * b) * term3 * dudl * dudl * dldz * dldz;
			add5 = 2.0 * acon * z * z / (a * b * b) * term2 * (dudl2 * dldz * dldz
					+ dudl * dldz2);
			ssz[0] = 8.0 * g * (add1 + add2 + add3 + add4 + add5);
			//	     next term is sigx - sigy
			term4 = (-euk * (1.0 + 1.0 / (akp * akp)) + 2.0 * u) / (ak * ak) + snu * cnu
					/ (dnu * akp * akp);
			term5 = snu2 * snu2 * snu / (cnu * dnu);
			add1 = acon / (2.0 * a * a * a) * term4;
			add2 = -acon / (2.0 * a * a * a * a * a) * term5 * (0.5 * x * dldx - 0.5 * y
					/ (dnu * dnu) * dldy);
			add3 = acon * z * z / (a * b * b) * term3 * dudl * dudl
					* (dldx * dldx - dldy * dldy);
			add4 = 0.5 * acon * z * z / (a * b * b) * term2 * (dudl2
					* (dldx * dldx - dldy * dldy)+ dudl * (dldx2 - dldy2));
			sigminus = 32.0 * g * ((1.0 - 2.0 * poiss) * (add1 + add2) + add3 + add4);
			//	     next get tauxy
			add1 = -acon / (2.0 * a * a * a * a * a) * term5 * 0.5 * (x * dldy + y
					/ (dnu * dnu) * dldx);
			add2 = 4.0 * acon * z * z / (a * b * b) * term3 * dudl * dudl
					* 0.5 * dldx * dldy;
			add3 = 2.0 * acon * z * z / (a * b * b) * term2 * (dudl2
					* 0.5 * dldx * dldy + 0.5 * dudl * dldxdy);
			txy[0] = 16.0 * g * ((1.0 - 2.0 * poiss) * add1 + add2 + add3);
			//	     next get tauxz
			add1 = 2.0 * acon * z / (a * b * b) * term2 * 0.5 * dudl * dldx;
			add2 = 4.0 * acon * z * z / (a * b * b) * term3 * dudl * dudl * dldz
					* 0.5 * dldx;
			add3 = 2.0 * acon * z * z / (a * b * b) * term2 * (dudl2 * dldz
					* 0.5 * dldx + dudl * 0.5 * dldxdz);
			txz[0] = 16.0 * g * (add1 + add2 + add3);
			//	     last get tauyz
			add1 = 2.0 * acon * z / (a * b * b) * term2 * dudl * 0.5 * dldy;
			add2 = 4.0 * acon * z * z / (a * b * b) * term3 * dudl * dudl * dldz
					* 0.5 * dldy;
			add3 = 2.0 * acon * z * z / (a * b * b) * term2 * (dudl2 * dldz
					* 0.5 * dldy + dudl * 0.5 * dldydz);
			tyz[0] = 16.0 * g * (add1 + add2 + add3);
		}
		//	     now get the separate sigx and sigy components
		ssx[0] = 0.5 * (sigsum + sigminus);
		ssy[0] = 0.5 * (sigsum - sigminus);
		return;
	}//end of SurgiFracEllcrack()

	//**********************************************
	// Function to calculate elliptic integral
	public double SurgiFracEllint(double akp)	{
		double a1, a2, a3, a4, b1, b2, b3, b4, ekd;
		double ek;
		//	     function to get ek for quarter period using expansion
		//	     from abramowitz and stegun
		a1 = 0.443251414630;
		a2 = 0.062606012200;
		a3 = 0.047573835460;
		a4 = 0.017365064510;
		b1 = 0.249983683100;
		b2 = 0.092001800370;
		b3 = 0.040696975260;
		b4 = 0.005264496390;
		ekd = 1.0 + a1 * akp + a2 * akp * akp + a3 * akp * akp * akp
				+ a4 * akp * akp * akp * akp + (b1 * akp + b2 * akp * akp
						+ b3 * akp * akp * akp + b4 * akp * akp * akp *akp) * Math.log(1.0 / akp);
		ek = ekd;
		return ek;
	}// end of SurgiFracEllint()

	//**********************************************
	private double SurgiFracGetu(double y, double ak)   
	{
		//subroutine to get the parameter u through integration of the elliptic integral
		double [] z = new double[21];
		int i, n, nend;
		double an, c, sum, denom, arg;
		double sumf;

		n = 20;
		nend = n + 1;
		an = (double)n;
		c = ak * ak;
		sum = 0.0;
		for(i = 0; i < nend; i++)
		{
			z[i] = Math.cos((3.141592654 * (2.0 * (double)i + 1.0)) / (2.0 * an + 2.0));
			denom = (1.0 - Math.pow((y / 2.0 * (z[i] + 1.0)), 2)) *
					(1.0 - c * Math.pow((y / 2.0 * (z[i] + 1.0)), 2));
			arg = (1.0 - z[i] * z[i]) / denom;
			if(arg > 0.0)
				sum = sum + Math.sqrt(arg);
		}
		sumf = 3.141592654 * y / (2.0 * (an + 1.0)) * sum;
		return sumf;
	}// end of SurgiFracGetu()

	//**********************************************
	private double SurgiFracGeteuk(double y, double ak)
	{
		//	     subroutine to get the elliptic integral through integration
		//	     using chebeshev polynomials
		//   double z[21];
		double [] z = new double[41];
		int i, n, nend;
		double an, c, denom, sum, anum, arg;
		double sumf;

		//   n = 20;
		n = 40;
		nend = n + 1;
		an = (double)n;
		c = ak * ak;
		sum = 0.0;
		for(i = 0; i < nend; i++)
		{
			z[i] = Math.cos((3.141592654 * (2.0 * (double)i + 1.0)) / (2.0 * an + 2.0));
			denom = 1.0 - Math.pow((y / 2.0 * (z[i] + 1.0)), 2);
			anum = 1.0 - c * Math.pow((y / 2.0 * (z[i] + 1.0)), 2);
			arg = (1.0 - z[i] * z[i]) * anum / denom;
			if(arg > 0.0)
				sum = sum + Math.sqrt(arg);
		}
		sumf = 3.141592654 * y / (2.0 * (an + 1.0)) * sum;
		return sumf;
	}// end of SurgiFracGeteuk()


	//*********************************************************
	public void SurgiFracTrip(double x, double y, double z, double a, double b, double []vlamout,
			double []vmuout, double []vnuout)	{
		//	     subroutine to calculate triply orthogonal elliptic coordinates
		//	     uses direct calculation approach; solution of cubic equation
		//	     function to get lam, mu and nu values from cartesian position
		//	     solves a cubic equation in lam
		double arg, tb, tc, as, bs, xs, ys, zs, a0, a1, a2, q, r;
		double test, arg1, arg2, res1, res2, ims1, ims2, r1r, r1i, r2r, r2i, r3r, r3i;
		double d3, root1, root2, root3, thet;
		double vlam=0, vmu, vnu;
		if(Math.abs(z) < 1.e-5)
		{
			arg = (x / a) * (x / a) + (y / b) * (y / b);
			if(arg < 1.0)
			{
				//	     inside the ellipse, lamda is 0
				vlam = 0.0;
				tb = a * a + b * b - x * x - y * y;
				tc = a * a * b * b - b * b * x * x - a * a * y * y;
				arg = tb * tb - 4.0 * tc;
				if(arg < 0.0)			{
					//printf("problem with vmu for z=0"," Cannot Continue");
					//exit(0);
				}
				vmu = (-tb + Math.sqrt(arg)) / 2.0;
				vnu = (a * a - b * b) * x * x / (a * a + vmu) - a * a;
			}
			else
			{
				//	     outside the ellipse, mu is 0
				vmu = 0.0;
				tb = a * a + b * b - x * x - y * y;
				tc = a * a * b * b - b * b * x * x - a * a * y * y;
				arg = tb * tb - 4.0 * tc;
				if(arg < 0.0)				{
					//printf("problem with vnu for z=0", "Cannot Continue");
					//exit(0);
				}
				vlam = (-tb + Math.sqrt(arg)) / 2.0;
				vnu = (a * a - b * b) * x * x / (a * a + vlam) - a * a;
			}
			vlamout[0] = vlam;
			vmuout[0] = vmu;
			vnuout[0] = vnu;
			return;
		}		
		as = a * a;
		bs = b * b;
		xs = x * x;
		ys = y * y;
		zs = z * z;
		a0 = -as * bs * zs;
		a1 = as * bs - bs * xs - as * ys - as * zs - bs * zs;
		a2 = as + bs - xs - ys - zs;
		q = a1 / 3.0 - a2 * a2 / 9.0;
		r = (a1 * a2 - 3.0 * a0) / 6.0 - a2 * a2 * a2 / 27.0;
		test = q * q * q + r * r;
		if(test > 0.0)
		{
			arg1 = r + Math.sqrt(test);
			arg2 = r - Math.sqrt(test);
			thet = 0.0;
		}
		else
		{
			arg1 = Math.sqrt(r * r + Math.abs(test));
			arg2 = arg1;
			if(Math.abs(r) < 1.e-8)
				thet = 3.141592654/2.0 ;
			else
				thet = Math.atan(Math.sqrt(Math.abs(test)) / r);
			if(thet < 0.0) thet = thet + 3.141592654;
		}
		res1 = Math.pow(arg1, (1.0 / 3.0)) * Math.cos(thet / 3.0);
		res2 = Math.pow(arg2, (1.0 / 3.0)) * Math.cos(thet / 3.0);
		ims1 = Math.pow(arg1, (1.0 / 3.0)) * Math.sin(thet / 3.0);
		ims2 = -Math.pow(arg2, (1.0 / 3.0)) * Math.sin(thet / 3.0);
		r1r = res1 + res2 - a2 / 3.0;
		r1i = ims1 + ims2;
		d3 = Math.sqrt(3.0);
		r2r = -0.5 * (res1 + res2) - a2 / 3.0 - d3 / 2.0 * (ims1 - ims2);
		r2i = -0.5 * (ims1 + ims2) + d3 / 2.0 * (res1 - res2);
		r3r = -0.5 * (res1 + res2) - a2 / 3.0 + d3 / 2.0 * (ims1 - ims2);
		r3i = -0.5 * (ims1 + ims2) - d3 / 2.0 * (res1 - res2);
		root1 = -1.e+35;
		root2 = -1.e+35;
		root3 = -1.e+35;
		if(Math.abs(r1i) < 1.e-08) root1 = r1r;
		if(Math.abs(r2i) < 1.e-08) root2 = r2r;
		if(Math.abs(r3i) < 1.e-08) root3 = r3r;
		if(root1 > 0.0)
		{
			vlam = root1;
			if(root2 > 0.0 || root3 > 0.0)			{
				//printf("problem: several real positive roots", "Cannot Continue");
				//exit(0);
			}
		}
		else if(root2 > 0.0)
		{
			vlam = root2;
			if(root3 > 0.0)
			{
				//printf("problem: two real positive roots", "Cannot Continue");
				if(Math.abs(root2 - root3) > 1.0)
				{
					//printf("two roots are different: stop", "Cannot Continue");
					//exit(0);
				}
				else
					vlam = root2;
			}
		}
		else if(root3 > 0.0)
		{
			vlam = root3;
		}
		else
		{
			//printf("no real positive roots", "Cannot Continue");
			//exit(0);
		}
		//	     now get the value for mu
		tb = a * a * a * a * vlam + a * a * vlam * vlam - a * a * vlam * x * x
				+ b * b * vlam * x * x + a * a * b * b * z * z + b * b * vlam * z * z;
		tc = 4.0 * a * a * b * b * vlam * Math.pow((a * a + vlam), 2) * z * z;
		arg1 = tb * tb - tc;
		if(arg1 < 0.0)
			arg1 = 0.0;
		vmu = (-tb + Math.sqrt(arg1)) / (2.0 * vlam * (a * a + vlam));
		if(vmu < (-b * b)) vmu = -b * b;
		if(vmu > 0.0) vmu = -0.00000001;
		//	     last get nu
		if(Math.abs(vlam) > 0.0  &&  Math.abs(vmu) > 0.0)
			vnu = a * a * b * b * z * z / (vlam * vmu);
		else
			vnu = a * a * (a * a - b * b) * x * x / ((a * a + vlam) * (a * a + vmu))
			- a * a;
		vlamout[0] = vlam;
		vmuout[0] = vmu;
		vnuout[0] = vnu;

		return;
	}// end of SurgiFracTrip()

	//************************* GeteukD() **********************************
	//	     New subroutine to calculate the elliptic integral and return the value
	//	      by Dmitriy 01/27/2005
	//	    "Table and Integrals and Other Mathematical Data" 
	//	      by Herbert Bristol Dwight, 4th Edition, NewYork, 1961, 150 pages
	//*********************************************************************
	private double SurgiFracGeteukD(double U,double AK)
	{
		double A2, A4, A6, A8, A10, A12, A14, A16, A18, A20, E;
		double sn2, sn4, sn6, sn8, sn10, sn12, sn14, sn16, sn18;
		double tmp = Math.sin(U);
		sn2=Math.pow(tmp,2);
		sn4=Math.pow(tmp,4);
		sn6=Math.pow(tmp,6);
		sn8=Math.pow(tmp,8);
		sn10=Math.pow(tmp,10);
		sn12=Math.pow(tmp,12);
		sn14=Math.pow(tmp,14);
		sn16=Math.pow(tmp,16);
		sn18=Math.pow(tmp,18);
		A2=0.5;
		A4=0.375+0.25*sn2;
		A6=0.3125+5./24.*sn2+1./6.*sn4;
		A8=0.2734375+35./192.*sn2+7./48.*sn4+0.125*sn6;
		A10=0.24609375+0.1640625*sn2+0.13125*sn4+0.1125*sn6+0.1*sn8;
		A12=231./1024.+77./512.*sn2+77./640.*sn4+0.103125*sn6+11./120.*sn8+1./12.*sn10;
		A14=429./2048.+143./1024.*sn2+0.11171875*sn4+429./4480.*sn6+143./1680.*sn8+13./168.*sn10+1./14.*sn12;
		A16=6435./32768.+2145./16384.*sn2+429./4096.*sn4+1287./14336.*sn6+143./1792.*sn8+65./896.*sn10+15./224.*sn12+0.0625*sn14;
		A18=12155./65536.+12155./98304.*sn2+2431./24576.*sn4+2431./28672.*sn6+2431./32256.*sn8
				+1105./16128.*sn10+85./1344.*sn12+17./288.*sn14+sn16/18.;
		A20=46189./262144.+46189./393216.*sn2+46189./491520.*sn4+46189./573440.*sn6+46189./645120.*sn8+4199./64512.*sn10
				+323./5376.*sn12+323./5760.*sn14+19./360.*sn16+0.05*sn18;
		double AM=1.-AK*AK;

		double EK = SurgiFracEllint(AM);
		E=2.*U/PI*EK+Math.sin(U)*Math.cos(U)*(0.5*A2*AK*AK+0.125*A4*Math.pow(AK,4)+0.0625*A6*Math.pow(AK,6)+0.0390625*A8*Math.pow(AK,8)
				+0.02734375*A10*Math.pow(AK,10)+0.0205078125*A12*Math.pow(AK,12)+0.01611328125*A14*Math.pow(AK,14)
				+429./32768.*A16*Math.pow(AK,16)+715./65536.*A18*Math.pow(AK,18)+2431./262144.*A20*Math.pow(AK,20));


		return E;
	}// end of SurgiFracGeteuk()

	//************************* GeteukD2() **********************************
	//	     New subroutine to calculate the elliptic integral and return the value
	//	      by Dmitriy 05/17/2005. All previous subroutines had insufficient accuracy
	//*********************************************************************
	//double SurgiFracGeteukD2(double U,double AK)
	private double SurgiFracGeteukD2(double phi,double AK) 
	// change U into phi to be consistent with the integral E(phi,k) JingD 05/04/06
	{
		//page 261 in Numerical Recipes in Fortran 77, chapter 6.11
		double E;
		double cc,q,s;
		s = Math.sin(phi);
		cc = Math.cos(phi)*Math.cos(phi);
		q = (1.-s*AK)*(1.+s*AK);

		E=s*(SurgiFracRF(cc,q,1.)-((s*AK)*(s*AK)*SurgiFracRD(cc,q,1.)/3.));

		return E;
	}// end of SurgiFracGeteukD2()

	private double SurgiFracRF(double x,double y, double z)
	{
		//page 257 in Numerical Recipes in Fortran 77, chapter 6.11
		double RF, ERRTOL, TINY, BIG, THIRD, C1, C2, C3, C4;
		boolean bcontinueiterations = true;
		ERRTOL = 0.0025;
		//	    ERRTOL = 0.08;
		TINY = 1.5e-38;
		BIG = 3.e37;
		THIRD = 1./3.;
		C1 = 1./24.;
		C2 = 0.1;
		C3 = 3./44.;
		C4 = 1./14.;
		double alamb,ave=0,delx=0, dely=0, delz=0, e2, e3, sqrtx, sqrty, sqrtz, xt, yt, zt;
		boolean condition = ((x < TINY)||(y < TINY)||(z < TINY)||(x>BIG)||(y>BIG)||(z>BIG));
		if (condition)
		{
			//			cout << "invalid arguments in RF "<< endl;
			return 1.;
		}
		else
		{
			xt = x;
			yt = y;
			zt = z;

			while (bcontinueiterations == true)
			{
				sqrtx = Math.sqrt(xt);
				sqrty = Math.sqrt(yt);
				sqrtz = Math.sqrt(zt);
				alamb = sqrtx*(sqrty+sqrtz)+sqrty*sqrtz;
				xt = 0.25*(xt+alamb);
				yt = 0.25*(yt+alamb);
				zt = 0.25*(zt+alamb);
				ave = THIRD*(xt+yt+zt);
				delx = (ave-xt)/ave;
				dely = (ave-yt)/ave;
				delz = (ave-zt)/ave;

				if((Math.abs(delx)<ERRTOL)&&(Math.abs(dely)<ERRTOL)&&(Math.abs(delz)<ERRTOL))
					bcontinueiterations = false;
			}
			e2 = delx*dely-delz*delz;
			e3 = delx*dely*delz;
			RF = (1.+(C1*e2-C2-C3*e3)*e2+C4*e3)/Math.sqrt(ave);
			return RF;
		}
	}// end of SurgiFracRF()

	private double SurgiFracRD(double x,double y, double z)
	{
		//page 257 in Numerical Recipes in Fortran 77, chapter 6.11
		double RD, ERRTOL, TINY, BIG, C1, C2, C3, C4, C5, C6;
		boolean bcontinueiterations = true;
		ERRTOL = 0.0015;
		//	    ERRTOL = 0.05;
		TINY = 1.e-25;
		BIG = 4.5e21;
		C1 = 3./14.;
		C2 = 1./6.;
		C3 = 9./22.;
		C4 = 3./26.;
		C5 = 0.25*C3;
		C6 = 1.5*C4;
		double alamb,ave=0,delx=0, dely=0, delz=0, ea, eb, ec, ed, ee, fac, sqrtx, sqrty, sqrtz, sum, xt, yt, zt;
		boolean condition = ((SurgiFracMin(x,y) < 0.)||(SurgiFracMin(x+y,z) < TINY) ||(x>BIG)||(y>BIG)||(z>BIG));
		if (condition)
		{
			//			cout << "invalid arguments in DF "<< endl;
			return 1.;
		}
		else
		{
			xt = x;
			yt = y;
			zt = z;
			sum = 0.;
			fac = 1.;
			while (bcontinueiterations == true)
			{
				sqrtx = Math.sqrt(xt);
				sqrty = Math.sqrt(yt);
				sqrtz = Math.sqrt(zt);
				alamb = sqrtx*(sqrty+sqrtz)+sqrty*sqrtz;
				sum = sum + fac/(sqrtz*(zt+alamb));
				fac = 0.25*fac;
				xt = 0.25*(xt+alamb);
				yt = 0.25*(yt+alamb);
				zt = 0.25*(zt+alamb);
				ave = 0.2*(xt+yt+3.*zt);
				delx = (ave-xt)/ave;
				dely = (ave-yt)/ave;
				delz = (ave-zt)/ave;
				if((Math.abs(delx)<ERRTOL)&&(Math.abs(dely)<ERRTOL)&&(Math.abs(delz)<ERRTOL))
					bcontinueiterations = false;
			}
			ea = delx*dely;
			eb = delz*delz;
			ec = ea - eb;
			ed = ea - 6.*eb;
			ee = ed+ec+ec;
			RD = 3.*sum+fac*(1.+ed*(-C1+C5*ed-C6*delz*ee)+delz*(C2*ee+delz*(-C3*ec+delz*C4*ea)))/(ave*Math.sqrt(ave));;
			return RD;
		}
	}// end of SurgiFracRD()

	private double SurgiFracMax(double a, double b) 
	{
		return (((a) > (b)) ? (a) : (b));
	}

	private double SurgiFracMin(double a, double b) 
	{
		return (((a) < (b)) ? (a) : (b));
	}

	//********************TripNew()******************************************************
	//		Subrountine to get lam, mu and nu values from cartesian position
	//		solves a cubic equation in lam (x^3+a2x^2+a1x+a0)
	//		Due to the limitation of the Borlanc C on the calculations of double precision
	//		umbers, the equation is normallized before solving it.
	//		The equation is divided by the constant (a^3*b^3)
	//		09/18/01 by jdu
	//		Due to the existing Trip function has problems (can't find roots in the z=0 plane,etc.),
	//		TripNew is developed to solve the Cardan equation x^3+px+q=0 using complex numbers
	//************************************************************************************
	private int SurgiFracTripNew(double x,double y,double z,double a,double b,double []vlam,double []vmu, double []vnu)
	{
		Complex term1, term2,root1,root2,root3;
		Complex w = new Complex(-0.5,0.8660254);
		Complex w2 = new Complex(-0.5,-0.8660254);
		double as=a*a;
		double bs=b*b;
		double xs=x*x;
		double ys=y*y;
		double zs=z*z;
		double a0=-as*bs*zs;
		double a1=as*bs-bs*xs-as*ys-as*zs-bs*zs;
		double a2=as+bs-xs-ys-zs;
		//	 	normalize the equation, the constants (a2,a1,a0) are changed
		//			Mulitplying the calcualted roots for the normalized equation by (ab)
		//			to get the roots for the original equation (vlam, vmu and vnu)
		a2=a2/(a*b);
		a1=a1/(as*bs);
		a0=a0/(as*a*bs*b);
		double q0=2.E0/27.E0*a2*a2*a2-a1*a2/3.E0+a0;
		double q1=-a2*a2/3.E0+a1;
		//		To be consistent with equations in the mathematical manual,
		//		use notation of  p and q instead of q0,q1
		double p=q1;
		double q=q0;
		double delta=(q/2.)*(q/2.)+(p/3.)*(p/3.)*(p/3.);
		Complex complexdelta=new Complex(delta, 0);
		Complex sqrtdelta=complexdelta.sqrt();
		//? why this is not working??
		//	complex <double> sqrtdelta=pow((complexdelta,0.5);
		//term1=pow((-q/2.+sqrtdelta),(1./3.));
		//term2=pow((-q/2.-sqrtdelta),(1./3.));
		Complex tmp = new Complex(-q/2.0, 0);
		term1=tmp.add(sqrtdelta).pow(1.0/3.0);
		term2=tmp.subtract(sqrtdelta).pow(1.0/3.0);

		//root1=term1+term2;
		//root2=w*term1+w2*term2;
		//root3=w2*term1+w*term2;
		
		root1=term1.add(term2);
		tmp = w2.multiply(term2);
		root2=w.multiply(term1);
		root2.add(tmp);
		tmp = w.multiply(term2);
		root3=w2.multiply(term1);
		root3.add(tmp);
		//		assign the roots to real variables
		//double r1=root1.real();
		//double r2=root2.real();
		//double r3=root3.real();
		double r1=root1.getReal();
		double r2=root2.getReal();
		double r3=root3.getReal();
		//		transfer all the root back to the original equation
		r1=r1-a2/3.;
		r2=r2-a2/3.;
		r3=r3-a2/3.;
		if (delta > 1.E-08)
		{
			//			cerr << " problem: Only one real root" << endl;
			return 1;
		}
		else if (delta < 1.E-08)
		{
			// Three non-equal real roots!
			SurgiFracRootsSort(a2,r1,r2,r3,vlam,vmu,vnu);
		}
		else
		{
			if ((p == 0.) && (q == 0.))
			{
				//Three zero roots!
				vlam[0]=0.;
				vmu[0]=0.;
				vnu[0]=0.;
			}
			else
			{
				//Three real roots,two are the same!
				SurgiFracRootsSort(a2,r1,r2,r3,vlam,vmu,vnu);
			}
		}
		//			get the roots for the original equation
		vlam[0]=vlam[0]*a*b;
		vmu[0]=vmu[0]*a*b;
		vnu[0]=vnu[0]*a*b;
		return 0;
	}//end of SurgiFracTripNew()

	//*********************** RootSort () ***************************************
	//		Sort the three roots and assign them into lam, mu and nu based on the
	//		elliptical coordinates requirements:
	//		infinity > lam >= 0 >= mu >= -B^2 >= nu >= -A^2
	//***************************************************************************
	private int SurgiFracRootsSort(double a2, double r1, double r2, double r3, double []vlam,double []vmu,double []vnu)
	{
		double RootMax,RootMin;
		RootMax=dMaximum(r1,r2,r3);
		RootMin=dMinimum(r1,r2,r3);
		if (Math.abs(RootMax) >= 0.)
		{
			vlam[0]=RootMax;
		}
		else
		{
			//			cerr << "No positive root" << endl;
			return 1;
		}

		if (RootMin < 0.)
		{
			vnu[0]=RootMin;
		}
		else
		{
			//			cerr << "Three positive roots"<< endl;
			return 1;
		}
		//		due to the transformation to use the analytical solution for (x^3+px+q=0)
		//		the summation of vlam, vmu and vnu is no longer equal to zero
		//		They are three roots of (x^3+a2x^2+a1x+a0=0)
		vmu[0]=-a2-(vlam[0]+vnu[0]);
		//		making the value of the root to be ZERO, if they are very small
		if (Math.abs(vlam[0]) < (1.E-8)) vlam[0]=0.;
		if (Math.abs(vmu[0]) < (1.E-8)) vmu[0]=0.;

		return 0;
	}// end of SurgiFracRootSort()

	//***************** dMaximum() ***************************************
	//		Find and return the maximum value of the three input variables.
	//********************************************************************
	private double dMaximum(double dVal1, double dVal2, double dVal3)	{
		double dmax;
		if (dVal1 >= dVal2)	dmax=dVal1;
		else	dmax=dVal2;

		if (dmax <= dVal3)	dmax=dVal3;
		return dmax;
	}// end of dMaximum()

	//***************** dMinimum() ***************************************
	//		Find and return the minimum value of the three input variables.
	//********************************************************************
	private double dMinimum(double dVal1, double dVal2, double dVal3)	{
		double dmin;
		if (dVal1 >= dVal2)	dmin=dVal2;
		else	dmin=dVal1;

		if (dmin >= dVal3)	dmin=dVal3;
		return dmin;
	}// end of dMinimum()












	//E:\DuJing\Development\FracNetwork\MatlabProgram
	//************* stress calculation for 2D Crack ****************************
	//  dX           -- Along the crack length direction
	//  dY           -- perpendicular to the crack length direction
	//  dPressure    -- pressure inside of the crack
	//  dSSx         -- Normal stress in X direction (along crack length direction)
	//  dSSy         -- Normal stress in Y direction (perpendicular to length direction)
	//  dTauxy       -- Shear stress 
	//  dSSv         -- Out of plane stress
	public void crack2D(double dPressure, double dYoungs, double dPoisson, 
			double dX, double dY, double dHalfLength, double [] dS) {
		double dTheta, dTheta1, dTheta2;
		double dSigsum, dSigminus, dTauxy, dSSx, dSSy, dSSv;
		double dR, dR1, dR2;
		double dc;  // Crack half length

		dc=dHalfLength;
		dR=Math.sqrt(dX*dX+dY*dY);               //distance to the origin
		dR1=Math.sqrt((dX-dc)*(dX-dc)+dY*dY);    //distance to point (0,+c)
		dR2=Math.sqrt((dX+dc)*(dX+dc)+dY*dY);    //distance to point (0,-c)
		dTheta=Math.atan2(dY,dX);                 //angle to the origin
		dTheta1=Math.atan2(dY,(dX-dc));           //angle to point (0,+c)
		dTheta2=Math.atan2(dY,(dX+dc));           //angle to point (0,-c)

		dSigsum=-dPressure*(dR/Math.sqrt(dR1*dR2)*Math.cos(dTheta-0.5*dTheta1-0.5*dTheta2)-1.0);
		dSigminus=-dPressure*(dR*Math.sin(dTheta)/dc*Math.pow((dc*dc/(dR1*dR2)),1.5)*Math.sin(1.5*dTheta1+1.5*dTheta2));
		dTauxy=dPressure*(dR*Math.sin(dTheta)/dc*Math.pow((dc*dc/(dR1*dR2)),1.5)*Math.cos(1.5*dTheta1+1.5*dTheta2));

		//calculate SSx, SSy, Tauxy, and stress
		dSSx=dSigsum-dSigminus;
		dSSy=dSigsum+dSigminus;
		dSSv = dPoisson*2.0*dSigsum;
		dS[0] = dSSx; 
		dS[1] = dSSy; 
		dS[2] = dTauxy; 
		dS[3] = dSSv; 
	}

	// ******* mean stress calculation for 2D Crack using analytical solution*********
	// The mean stress is calculated along fracture length direction
	public void crack2DIntegral(double dPressure, double dYoungs, double dPoisson, 
			double dY, double dHalfLength, double [] dS) {
		double dc=dHalfLength;
		double dR=Math.sqrt(dY*Math.sqrt(4*dc*dc+dY*dY)); 
		double dBeta=Math.atan2(-2*dc,dY);

		double dSSx=dPressure/dc*(dR*Math.cos(dBeta/2.) - dc + dY*dc/dR*Math.sin(dBeta/2.) - dY*dY/dR*Math.cos(dBeta/2.));
		double dSSy=dPressure/dc*(dR*Math.cos(dBeta/2.) - dc - dY*dc/dR*Math.sin(dBeta/2.) + dY*dY/dR*Math.cos(dBeta/2.));

		double dSSv=dPoisson*(dSSx+dSSy);
		dS[0] = dSSx; 
		dS[1] = dSSy; 
		dS[2] = 0; 
		dS[3] = dSSv; 
	}
	
	// ******* mean stress calculation for 2D Crack using curve fitting numerical integrated results*********
	//  dY           -- distance away from crack (perpendicular to the crack
	//                   length direction)
	//  dPressure    -- pressure inside of the crack
	//  dSSy_mean    -- Normal stress in Y direction (perpendicular to length direction)
	// *************************************************************************
		public void crack2DCurveFitting(double dPressure, double dY, double dHalfLength, double [] dS) {
			double dc=dHalfLength;
			double a = 0.5*dY/dc;
			double a2 = a*a;
			double dSSy_mean=0.0629*a2*a2 -0.3923*a*a2 + 0.888*a2 -0.8915*a + 0.3742; // R2=0.9908
			//double dSSy_mean=-0.0989034539598793*a*a2 + 0.5593202172193898*a2 -1.11350773511662*a + 0.869095857416663;
			dS[0] = 0; 
			dS[1] = 0; 
			dS[2] = 0; 
			dS[3] = dSSy_mean*dPressure; 
		}

	// ******* stress calculation for 2D Strip with constant pressure applied on strip **************
	//  dX           -- Vertical direction (perpendicular to free surface)
	//  dY           -- Horizontal direction (along free surface) (=constant)
	//  dPressure    -- pressure acting on the strip
	//  dHalfLength  -- Half length of the strip where the constant pressure acts on
	//  dSSx         -- Normal stress in X direction (perpendicular to free surface)
	//  dSSy         -- Normal stress in Y direction (along free surface)
	//  dTauxy       -- Shear stress 
	//  dSSv         -- Out of plane stress
	//
	//           2a
	//   ----|-----------|-------> (Y)
	//             |
	//             |
	//             |
	//             |
	//             V  (X)
	// *************************************************************************
	public void strip2D(double dPressure, double dPoisson, 
			double dX, double dY, double dHalfLength, double [] dS) {
		// da        -- half length of strip where the constant pressure acts on
		// dTheta1   -- angle to point (0,a)
		// dTheta2   -- angle to point (0,-a)
		double pi = Math.PI;
		double da=dHalfLength;
		double dTheta1=Math.atan2(dX,(dY-da));
		double dTheta2=Math.atan2(dX,(dY+da));

		double dSSx=dPressure/(2.*pi)*(2.*(dTheta1-dTheta2)-(Math.sin(2.*dTheta1)-Math.sin(2.*dTheta2)));
		double dSSy=dPressure/(2.*pi)*(2.*(dTheta1-dTheta2)+(Math.sin(2.*dTheta1)-Math.sin(2.*dTheta2)));
		double dTauxy=dPressure/(2.*pi)*(Math.cos(2.*dTheta1)-Math.cos(2.*dTheta2));
		double dSSv=dPoisson*(dSSx+dSSy);

		dS[0] = dSSx; 
		dS[1] = dSSy; 
		dS[2] = dTauxy; 
		dS[3] = dSSv;
	}

	public void Strip2DIntegral(double dPressure, double dPoisson, 
			double dX1, double dX2, double dY, double dHalfLength, double [] dS) {
		// This mean stress is the average over a length (dX2-dX1) and dY=constant

		double da=dHalfLength;
		double dSmall=1.e-8;
		double dTerm1_X1 = 0;
		double dTerm1_X2 = 0;
		double dTerm2_X1 = 0;
		double dTerm2_X2 = 0;
		double dTerm3_X1 = 0;
		double dTerm3_X2 = 0;
		if(Math.abs(dY-da) <= dSmall) {  // special case for y=a
			dTerm1_X1=2*(dX1*Math.atan2(dX1,(dY-da)))-2*(dX1*Math.atan2(dX1,(dY+da))-((dY+da)/2)*Math.log(1+(dX1*dX1)/(dY+da)*(dY+da)));
			dTerm1_X2=2*(dX2*Math.atan2(dX2,(dY-da)))-2*(dX2*Math.atan2(dX2,(dY+da))-((dY+da)/2)*Math.log(1+(dX2*dX2)/(dY+da)*(dY+da)));
			if (dX1 <= dSmall) {
				dTerm2_X1=-(dY+da)*Math.log((dY+da)*(dY+da)+dX1*dX1);
				dTerm3_X1=0.5-dX1*(dY+da)/((dY+da)*(dY+da)+dX1*dX1);
			}  else {
				dTerm2_X1=(dY-da)*Math.log((dY-da)*(dY-da)+dX1*dX1)-(dY+da)*Math.log((dY+da)*(dY+da)+dX1*dX1);
				dTerm3_X1=dX1*(dY-da)/((dY-da)*(dY-da)+dX1*dX1)-dX1*(dY+da)/((dY+da)*(dY+da)+dX1*dX1);        
			}
		} else if(Math.abs(dY+da) <= dSmall) {  // special case for y=-a
			dTerm1_X1=2*(dX1*Math.atan2(dX1,(dY-da)))-((dY-da)/2)*Math.log(1+(dX1*dX1)/(dY-da)*(dY-da))-2*(dX1*Math.atan2(dX1,(dY+da)));
			dTerm1_X2=2*(dX2*Math.atan2(dX2,(dY-da)))-((dY-da)/2)*Math.log(1+(dX2*dX2)/(dY-da)*(dY-da))-2*(dX2*Math.atan2(dX2,(dY+da)));
			if (dX1 <= dSmall) {
				dTerm2_X1=-(dY-da)*Math.log((dY-da)*(dY-da)+dX1*dX1);
				dTerm3_X1=dX1*(dY-da)/((dY-da)*(dY-da)+dX1*dX1)-0.5;
			}  else {
				dTerm2_X1=(dY-da)*Math.log((dY-da)*(dY-da)+dX1*dX1)-(dY+da)*Math.log((dY+da)*(dY+da)+dX1*dX1);
				dTerm3_X1=dX1*(dY-da)/((dY-da)*(dY-da)+dX1*dX1)-dX1*(dY+da)/((dY+da)*(dY+da)+dX1*dX1);        
			}
		} else {
			dTerm1_X1=2*(dX1*Math.atan2(dX1,(dY-da)))-((dY-da)/2)*Math.log(1+(dX1*dX1)/(dY-da)*(dY-da))-
					2*(dX1*Math.atan2(dX1,(dY+da))-(dY+da)/2)*Math.log(1+(dX1*dX1)/(dY+da)*(dY+da));
			dTerm1_X2=2*(dX2*Math.atan2(dX2,(dY-da)))-((dY-da)/2)*Math.log(1+(dX2*dX2)/(dY-da)*(dY-da))-
					2*(dX2*Math.atan2(dX2,(dY+da))-(dY+da)/2)*Math.log(1+(dX2*dX2)/(dY+da)*(dY+da));

			dTerm2_X1=(dY-da)*Math.log((dY-da)*(dY-da)+dX1*dX1)-(dY+da)*Math.log((dY+da)*(dY+da)+dX1*dX1);
			dTerm3_X1=dX1*(dY-da)/((dY-da)*(dY-da)+dX1*dX1)-dX1*(dY+da)/((dY+da)*(dY+da)+dX1*dX1); 
		}
		dTerm2_X2=(dY-da)*Math.log((dY-da)*(dY-da)+dX2*dX2)-(dY+da)*Math.log((dY+da)*(dY+da)+dX2*dX2);
		dTerm3_X2=dX2*(dY-da)/((dY-da)*(dY-da)+dX2*dX2)-dX2*(dY+da)/((dY+da)*(dY+da)+dX2*dX2); 

		double dSSx_mean=dPressure/(2.*Math.PI*(dX2-dX1))*((dTerm1_X2-dTerm1_X1)-(dTerm2_X2-dTerm2_X1));
		double dSSy_mean=dPressure/(2.*Math.PI*(dX2-dX1))*((dTerm1_X2-dTerm1_X1)+(dTerm2_X2-dTerm2_X1));
		double dTauxy_mean=dPressure/(2.*Math.PI)*(dTerm3_X2-dTerm3_X1);

		double dSSv_mean=dPoisson*(dSSx_mean+dSSy_mean);

		dS[0] = dSSx_mean; 
		dS[1] = dSSy_mean; 
		dS[2] = dTauxy_mean; 
		dS[3] = dSSv_mean;
	}

	// ******* mean stress calculation for 2D Crack using curve fitting numerical integrated results*********
	//  dY           -- distance away from crack (perpendicular to the crack length direction)
	//  dPressure    -- pressure inside of the crack
	//  dSSy_mean    -- Normal stress in Y direction (perpendicular to length direction)
	public double strip2DCurveFitting(double dPressure, double dPoisson, double dX, double dY, double dHalfLength) {
		// dc    -- Crack half length in 2D crack equations
		// In NetworkFrac model, 2*dc is the fracture HEIGHT used for the cross
		// fracture and subparallel fracture
		double dc=dHalfLength;
		double a = dY/(2.*dc);
		double dSSy_mean=dPressure*(-0.0989034539598793*a*a*a + 0.559320217219389*a*a 
				-1.11350773511662*a + 0.869095857416663);
		return dSSy_mean;
	}
	
	//G:\TFS\DuDevelopment\FracNetwork\SRC\SCALC.FOR
	/*
	 * 
	 * 
	 * 
	 * 
	 *       subroutine twod(x,y,c,poiss,pi,sx,sy,sz,txy,phi,pin,dzetdx,
     1                dzetdy,detady,disx)
c
c  ***   calculates stresses for 2-d crack                             ***
c
      r=sqrt(x**2+y**2)
      r1=sqrt(x**2+(y+c)**2)
      r2=sqrt(x**2+(y-c)**2)
      if(abs(x) .lt. 0.00001 .and. abs(y) .lt. 0.00001) then
         thet=0.0
      else if(abs(x) .gt. 0.00001 .and. abs(y) .lt. 0.00001) then
         thet=pi/2.0
      else
c        thet=atan(x/(-y))
         thet=atan2(x,y)
      endif
c     if(thet .lt. 0.0) thet=thet+pi
      if(abs(x) .lt. 0.00001 .and. y .lt. 0.0) thet=pi-0.00001
      if( abs(x) .lt. 0.00001 .and. abs(y+c) .lt. 0.00001) then
         thet1=0.0
      else if( abs(x) .gt. 0.00001 .and. abs(y+c) .lt. 0.00001) then
         thet1=pi/2.0
      else
c        thet1=atan(x/(-y-c))
         thet1=atan2(x,y+c)
      endif
c     if(thet1 .lt. 0.0) thet1=thet1+pi
      if(abs(x) .lt. 0.00001 .and. y .lt. c) thet1=pi-0.00001
      if(abs(x) .lt. 0.00001 .and. abs(y-c) .lt. 0.00001) then
         thet2=0.0
      else if(abs(x) .gt. 0.00001 .and. abs(y-c) .lt. 0.00001) then
         thet2=pi/2.0
      else
c        thet2=atan(x/(c-y))
         thet2=atan2(x,y-c)
      endif
c     if(thet2 .lt. 0.0) thet2=thet2+pi
c     if(thet2 .lt. 0.0) thet2=thet2+pi
      if(abs(x) .lt. 0.00001 .and. y .lt. (-c)) thet2=pi-0.00001
      a=r/sqrt(r1*r2)*cos(thet-thet1/2.0-thet2/2.0)-1.0
      b=r*sin(thet)/c*(c**2/(r1*r2))**1.5*sin(1.5*(thet1+thet2))
      sx=-(a+b)*pin
      sy=-(a-b)*pin
      sz=-poiss*2.0*a*pin
      txy=r*sin(thet)/c*(c**2/(r1*r2))**1.5*cos(1.5*(thet1+thet2))
      ta=r/sqrt(r1*r2)*sin(thet-0.5*(thet1+thet2))
c     if(abs(x) .lt. 0.00001) ta=-ta
      phi=2.0*(1.0+poiss)*((1.0-poiss)*ta+txy/2.0)
      disx1=2.*(1-poiss)*(sqrt(r1*r2)*sin(0.5*(thet1+thet2))-
     1     r*sin(thet))
      disx2=-a*r*sin(thet)
      disx=disx1+disx2
c     write(*,400) disx1,disx2,disx3
c 400 format(' disx1=',e14.4,' disx2=',e14.4,' disx=',e14.4)
c     calculate some additional derivatives for reference
      dzetdx=(1.0+poiss)*((1.0-2.0*poiss)*a-b)
      dzetdy=(1.0+poiss)*(-2.0*(1.0-poiss)*ta+txy)
      detady=(1.0+poiss)*((1.0-2.0*poiss)*a+b)
c     write(*,300) dzetdx,dzetdy,detady
c 300 format(' dzetdx=',e12.5,'  dzetdy=',e12.5,'  detady=',e12.5)
      txy=txy*pin
      return
      end
c
c
c
      subroutine penny(x,y,c,poiss,pi,sr,st,sz,tzr,duzdr,durdr,pin,ur,
     1                 duzdz,durdz)
c
c  ***   calculates stresses for penny-shaped problem                  ***
c
      if(x .le. 0.00001 .and. y .le. c) then
         sz=pin
         st=(0.5+poiss)*pin
         sr=st
         tzr=0.0
         duzdr=-8.*(1.0-poiss**2)*y/pi
         duzdr=duzdr/sqrt(c**2-y**2)
         return
      endif
      rho=abs(y/c)
      zet=x/c
      r=sqrt(1.0+zet**2)
      rr=sqrt((rho**2+zet**2-1.0)**2+4.0*zet**2)
      if(zet .eq. 0.0) then
         thet=pi/2.0
      else
c        thet=atan(1.0/zet)
         thet=atan2(1.0,zet)
      endif
      if((rho**2+zet**2) .eq. 1.0) then
         phi=pi/2.0
      else
c        phi=atan(2.0*zet/(rho**2+zet**2-1.0))
         py=2.0*zet
         px=rho**2+zet**2-1.0
         phi=atan2(py,px)
      endif
c     if(phi .lt. 0.0) phi=phi+pi
c     if(zet .eq. 0.0 .and. rho .eq. 0.0) phi=pi
      sr=sqrt(rr)
      p=phi/2.0
      c10=cos(p)/sr
      s10=sin(p)/sr
      c20=r/(rr*sr)*cos(3.0*p-thet)
      s20=r/(rr*sr)*sin(3.0*p-thet)
      arg=sr*cos(p)+r*cos(thet)
      if(abs(arg) .ge. 1.e-06) then
c       s00=atan((sr*sin(p)+r*sin(thet))/arg)
        py=sr*sin(p)+r*sin(thet)
        s00=atan2(py,arg)
      else
         s00=pi/2.0
      endif
      if(abs(rho) .lt. 1.e-04) then
         tzr=0.0
         b=0.0
         duzdr=0.0
      else
         c01=(sr*cos(p)-zet)/rho
         s01=(1.0-sr*sin(p))/rho
         c11=1.0/rho-r/(rho*sr)*cos(thet-p)
         s11=r/(rho*sr)*sin(thet-p)
         c21=rho/(rr*sr)*cos(3.0*p)
         s21=rho/(rr*sr)*sin(3.0*p)
         c12=2.0/rho*c01-c10
         s12=2.0/rho*s01-s10
         c22=2.0/rho*c11-c20
         s02=(c01-zet*s01)/rho
         tzr=2.0/pi*(c21-s11)*zet
         b=2.0/pi*((1.0-2.0*poiss)*(c12-s02)-zet*(c22-s12))
         term=c11-s01+zet*0.5/(1.0-poiss)*(c21-s11)
         duzdr=4.0*(1.0-poiss**2)/pi*term
      endif
      terma=c10-c12-s00+s02
      termb=c20-c22-s10+s12
      durdr=(1.0+poiss)/pi*((1.0-2.0*poiss)*terma-zet*termb)
      ur=2.0*c*(1.0+poiss)/pi*((1.0-2.0*poiss)*(c01-rho/2.0*(s00+s02))
     1   -zet*(c11-s01))
c     if(y .gt. 0.0) duzdr=-duzdr
      sz=2.0/pi*(c10-s00+zet*(c20-s10))
      a=4.0*(1.0+poiss)/pi*(c10-s00)
      st=-(a+b-sz)/2.0*pin
      sr=-(a-b-sz)/2.0*pin
      sz=-sz*pin
      tzr=-tzr*pin
c     calculate some other derivatives for reference
c     arg=rr+r*r+2.0*sr*r*(cos(p)*cos(thet)+sin(p)*sin(thet))
c     if(arg .lt. 0.0) then
c        c00=0.0
c     else
c        c00=alog(sqrt(arg)/rho)
c     endif
      duzdz=4.0*(1.-poiss**2)/pi*(c10-s00+
     1      0.5/(1.0-poiss)*(-c10+s00+zet*(c20-s10)))
      durdz=-2.0*(1.0+poiss)/pi*(c11-s01-zet*(c21-s11)
     1      +(1.0-2.0*poiss)*(c11-s01))
c     write(*,350) x,y,duzdr,durdr,duzdz,durdz
c 350 format(' x,y:',f6.2,f6.2,' zr',e10.3,' rr',e10.3,' zz',e10.3,
c    1       ' rz',e10.3)
      return
      end

/
	 */










}
