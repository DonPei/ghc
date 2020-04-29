package com.geohammer.core.stress;

//E:\DuJing\Development\FracNetwork\Documents
//G:\Pinnacle\DuJing\Development\RetiredProjects\InversionGUI\Inversion-working\src
//G:\Pinnacle\DuJing\Development\FracNetwork\SRC\Phase2
//G:\Pinnacle\DuJing\Development\FracNetwork\SRC
//arrowsmith510.asu.edu/TheLectures/Lecture18
//http://cefor-umn.com/
//http://journal.frontiersin.org/article/10.3389/feart.2013.00004/full
//http://www.sciencedirect.com/science/article/pii/S2405656115000140
//http://www.colorado.edu/engineering/CAS/courses.d/Structures.d/
//https://www.ecomputingx.com/SM-07.jsp
public class OkadaCore {

	private double PI = 3.14159265358979;

	double alpha, sindip, cosdip;
	double h, d, p, q, R, yt, ct, dt;
	double X0, X11, X32, X53;
	double Y0, Y11, Y32, Y53;
	double Z0, Z11, Z32, Z53;
	double Q1, P1, F1, G1, H1, D11, K1, K3, K4, E1, K2;
	double I1, I2, I3, I4, Theta;


	public OkadaCore () { }

	//all source and receiver are in NED system
	public void calDisplacementZ(double sn, double se, double sz, double rn, double re, double rz,
			double L, double W, double U, double strike, double dip, 
			double [] disp, int FracType, double alpha1) {
		//center
		re -= se; rn -= sn;
		//rotation along Z axis

		double sinAz = Math.sin((90-strike)*Math.PI/180.0);
		double cosAz = Math.cos((90-strike)*Math.PI/180.0);
		double sinDip = Math.sin(dip*Math.PI/180.0);
		double cosDip = Math.cos(dip*Math.PI/180.0);
		double ae = re * cosAz + rn * sinAz;
		double an = -re * sinAz + rn * cosAz;
		
		//move center to left bottom corner
		double de = 0.5*L;
		double dn = 0.5*W*cosDip;
		double dz = 0.5*W*sinDip;
		double C = sz+dz;
			
		double be = ae+de;
		double bn = an+dn;
		double bz = -rz;
		
		calDisplacementZ(C, L, W, U, dip, be, bn, bz, disp, FracType, alpha1);
	}


	//	Calculate(Frac, i,SiteX,SiteY, SiteZ, TheoX, TheoY);
	public int Okada1992(double Depth, double Azimuth, double Dip, double Length, double Height,
			double Width, double StrikeSlip, double DipSlip, double xpoint, double ypoint, double zpoint,
			double [] xtilt1, double [] ytilt1, double XOffset, double YOffset)
	{
		double X, Y, Z, sinaz, cosaz;
		double [] xt = new double[1];
		double [] yt = new double[1];
		//cout<< "Okada1992, Depth="<<Depth<<" Az="<<Azimuth<<" Dip="<<Dip<<" L="<<Length;
		//cout<<"H="<<Height<<" W="<<Width<<" SS="<<StrikeSlip<<" DS="<<DipSlip<<" X="<<xpoint;
		//cout<<" Y="<<ypoint<<" XOffset="<<XOffset<<" YOffset"<<YOffset<<endl;

		sinaz = Math.sin(PI/180. * Azimuth );
		cosaz = Math.cos(PI/180. * Azimuth );

		// Our length and height are for an elliptical fracture,
		// 		so we must multiply by sqrt(PI/4) to get the rectangular coordinates...
		Length = Length * my_sqrt(PI/4);
		Height = Height * my_sqrt(PI/4);

		cosdip = Math.cos(PI/180. * Dip);
		sindip = Math.sin(PI/180. * Dip);

		// The left corner of Okada's fracture is positioned at the Z axis
		//     so must correct by adding half of fracture length to X position...
		// Fix for well offset
		X = (ypoint-YOffset) * cosaz + (xpoint-XOffset) * sinaz + Length/2.;
		//     and must correct by adding half of fracture height * cos dip to Y position...
		Y = (ypoint-YOffset) * sinaz - (xpoint-XOffset) * cosaz + Height/2. * cosdip;
		Z = zpoint;
		//     and must correct by adding half of
		//				fracture height * sine of the dip to depth...
		double xtilt = 0;
		double ytilt = 0;
		double alpha1 = 2./3.;
		if (Width != 0)
		{
			calcOkadaSite(Depth + Height/2. * sindip, Length, Height, Width, Dip, X, Y, Z, xt, yt, 0, alpha1);
			xtilt += 1.e6 *(-yt[0] * cosaz + xt[0] * sinaz);
			ytilt += 1.e6 *(yt[0] * sinaz + xt[0] * cosaz);
		}
		if (StrikeSlip != 0)
		{
			calcOkadaSite(Depth + Height/2. * sindip, Length, Height, StrikeSlip, Dip, X, Y, Z, xt, yt, 1, alpha1);
			xtilt += 1.e6 *(-yt[0] * cosaz + xt[0] * sinaz);
			ytilt += 1.e6 *(yt[0] * sinaz + xt[0] * cosaz);
		}
		if (DipSlip != 0)
		{
			calcOkadaSite(Depth + Height/2. * sindip, Length, Height, DipSlip, Dip, X, Y, Z, xt, yt, 2, alpha1);
			xtilt += 1.e6 *(-yt[0] * cosaz + xt[0] * sinaz);
			ytilt += 1.e6 *(yt[0] * sinaz + xt[0] * cosaz);
		}
		xtilt1[0] = xtilt;
		ytilt1[0] = ytilt;
		return 0;
	}
	
	// Calculates the tilt at this site.
	// c=Depth to center of frac                             d
	// L = length of frac
	//	W = height of frac
	// U = width of frac
	// x = X location of site
	//	y = Y location of site
	// zpos = Z location of site
	// xtilt and ytilt are the return values


//	public int OkadaDisplacement(double Depth, double Azimuth, double Dip, double Length, double Height,
//			double Width, double StrikeSlip, double DipSlip, double xpoint, double ypoint, double zpoint,
//			double [] displacement1)
//	{
//		double X, Y, Z, sinaz, cosaz;
//
//		sinaz = Math.sin(PI/180. * Azimuth );
//		cosaz = Math.cos(PI/180. * Azimuth );
//
//		// Our length and height are for an elliptical fracture,
//		//	 		so we must multiply by sqrt(PI/4) to get the rectangular coordinates...
//		Length = Length * my_sqrt(PI/4);
//		Height = Height * my_sqrt(PI/4);
//
//		cosdip = Math.cos(PI/180. * Dip);
//		sindip = Math.sin(PI/180. * Dip);
//
//		// The left corner of Okada's fracture is positioned at the Z axis
//		//	     so must correct by adding half of fracture length to X position...
//		// Fix for well offset
//		X = ypoint * cosaz + xpoint * sinaz + Length/2.;
//		//	     and must correct by adding half of fracture height * cos dip to Y position...
//		Y = ypoint* sinaz - xpoint * cosaz + Height/2. * cosdip;
//		Z = zpoint;
//		//	     and must correct by adding half of
//		//					fracture height * sine of the dip to depth...
//		double displacement = 0;
//		double [] tempdouble = new double[1];
//		if (Width != 0)
//		{
//			calcOkadaDispSite(Depth + Height/2. * sindip, Length, Height, Width, X, Y, Z, tempdouble, 0);
//			displacement += tempdouble[0];
//		}
//		if (StrikeSlip != 0)
//		{
//			calcOkadaDispSite(Depth + Height/2. * sindip, Length, Height, StrikeSlip, X, Y, Z, tempdouble, 1);
//			displacement += tempdouble[0];
//		}
//		if (DipSlip != 0)
//		{
//			calcOkadaDispSite(Depth + Height/2. * sindip, Length, Height, DipSlip, X, Y, Z, tempdouble, 2);
//			displacement += tempdouble[0];
//		}
//
//		displacement1[0] = displacement;
//		return 0;
//	}
//
//	public void calcOkadaDispSite(double c, double L, double W, double U, double x, double y, double zpos,
//			double [] disp, int FracType) {
//		// Frac type is 0 for tensile, 1 for strikeslip, and 2 for dipslip.
//		double u2A=0, u2Ahat=0, u2B=0, u2C=0;
//		double u3A=0, u3Ahat=0, u3B=0, u3C=0;
//		double ss, eta, z;
//
//		// assuming lambda = mu,
//		alpha = 2./3.;
//		z = zpos;
//
//		double [] sschange 	= new double [] {0, -L, 0, -L};
//		double [] etachange = new double [] {0, 0, -W, -W};
//		double [] signref 	= new double [] {1, -1, -1, 1};
//
//		int q;
//		for (q = 0 ; q < 4 ; q ++)		{
//			d = c-z;
//			p = y * cosdip + d * sindip;
//			ss = x + sschange[q];
//			eta = p + etachange[q];
//
//			calValues(y,c,ss,eta,z);
//			u2A		+= signref[q] * f2A(ss,eta,z,FracType);
//			u3A		+= signref[q] * f3A(ss,eta,z,FracType);
//			u2B		+= signref[q] * f2B(ss,eta,z,FracType);
//			u3B		+= signref[q] * f3B(ss,eta,z,FracType);
//			if (z!=0)
//			{
//				u2C	+= signref[q] * f2C(ss,eta,z,FracType);
//				u3C	+= signref[q] * f3C(ss,eta,z,FracType);
//			}
//
//			d = c + z;
//			p = y * cosdip + d * sindip;
//			ss = x + sschange[q];
//			eta = p + etachange[q];
//			calValues(y,c,ss,eta,-z);
//			u2Ahat	+= signref[q] * f2A(ss,eta,-z,FracType);
//			u3Ahat	+= signref[q] * f3A(ss,eta,-z,FracType);
//		}
//		disp[0] = U/(2.*PI) * ( (u2A-u2Ahat + u2B - z*u2C) * sindip +
//				( u3A - u3Ahat + u3B - z * u3C ) * cosdip);
//	}
	
	
	public int calDisplacementZFromEclipticalCavity(
			double centerDepth, double azimuth, double dip, double fullLength, double fullHeight,
			double tensileSlip, double strikeSlip, double dipSlip, double alpha1, 
			double xpoint, double ypoint, double zpoint, double [] displacement1)
	{
		double X, Y, Z, sinaz, cosaz;

		sinaz = Math.sin(PI/180. * azimuth );
		cosaz = Math.cos(PI/180. * azimuth );

		// an elliptical fracture to rectangle by multiply sqrt(PI/4)
		double L = fullLength * my_sqrt(PI/4);
		double W = fullHeight * my_sqrt(PI/4);

		cosdip = Math.cos(PI/180. * dip);
		sindip = Math.sin(PI/180. * dip);

		//The left corner of Okada's fracture is positioned at the Z axis
		//so must correct by adding half of fracture length to X position...
		//Fix for well offset
		X = ypoint * cosaz + xpoint * sinaz + L/2.;
		//and must correct by adding half of fracture height * cos dip to Y position...
		Y = ypoint* sinaz - xpoint * cosaz + W/2. * cosdip;
		Z = zpoint;
		//and must correct by adding half of
		//fracture height * sine of the dip to depth...
		double displacement = 0;
		double [] tempdouble = new double[1];
		double c = centerDepth + 0.5*fullHeight*sindip;
		if (tensileSlip != 0) {
			calDisplacementZ(c, L, W, dip, tensileSlip, X, Y, Z, tempdouble, 0, alpha1);
			displacement += tempdouble[0];
		}
		if (strikeSlip != 0) {
			calDisplacementZ(c, L, W, dip, strikeSlip, X, Y, Z, tempdouble, 1, alpha1);
			displacement += tempdouble[0];
		}
		if (dipSlip != 0) {
			calDisplacementZ(c, L, W, dip, dipSlip, X, Y, Z, tempdouble, 2, alpha1);
			displacement += tempdouble[0];
		}

		displacement1[0] = displacement;
		return 0;
	}
	
	public void calcOkadaSite(double c, double L, double W, double U, double dip, 
			double x, double y, double zpos, double [] xtilt1, double [] ytilt1, int FracType, double alpha1)
	{
		//alpha = (lambda + mu) / (lambda + 2 * mu);
		// assuming lambda = mu,
		//alpha = 2./3.;
		alpha = alpha1;
		
		cosdip = Math.cos(PI/180. * dip);
		sindip = Math.sin(PI/180. * dip);
		
		// Frac type is 0 for tensile, 1 for strikeslip, and 2 for dipslip.
		double l1A=0, l1Ahat=0, l1B=0, u1C=0, l1C=0;
		double l2A=0, l2Ahat=0, l2B=0, u2C=0, l2C=0;
		double l3A=0, l3Ahat=0, l3C=0, u3C=0, l3B=0;
		double ss, eta;

		double z = zpos;
		// First evaluate at x, p, zpos
		// calculate at x - L, p, zpos::
		// calculate at x, p - W, zpos::
		// calculate at x - L, p - W, zpos::

		double [] sschange 	= new double [] {0, -L, 0, -L};
		double [] etachange = new double [] {0, 0, -W, -W};
		double [] signref 	= new double [] {1, -1, -1, 1};

		int q;
		int retval;
		for (q = 0 ; q < 4 ; q ++){
			d = c-z;
			p = y * cosdip + d * sindip;
			ss = x + sschange[q];
			eta = p + etachange[q];

			retval = calValues(y,c,ss,eta,z);
			l1A		+= signref[q] * df1Adz(ss,eta,z,FracType);
			l1B		+= signref[q] * df1Bdz(ss,eta,z,FracType);
			u1C		+= signref[q] * f1C(ss,eta,z,FracType);
			if (z != 0) l1C		+= signref[q] * df1Cdz(ss,eta,z,FracType);

			l2A		+= signref[q] * df2Adz(ss,eta,z,FracType);
			l2B		+= signref[q] * df2Bdz(ss,eta,z,FracType);
			u2C		+= signref[q] * f2C(ss,eta,z,FracType);
			if (z != 0) l2C		+= signref[q] * df2Cdz(ss,eta,z,FracType);

			l3A		+= signref[q] * df3Adz(ss,eta,z,FracType);
			l3B		+= signref[q] * df3Bdz(ss,eta,z,FracType);
			u3C		+= signref[q] * f3C(ss,eta,z,FracType);
			if (z != 0) l3C		+= signref[q] * df3Cdz(ss,eta,z,FracType);

			d = c + z;
			p = y * cosdip + d * sindip;
			ss = x + sschange[q];
			eta = p + etachange[q];
			retval = calValues(y,c,ss,eta,-z);
			l1Ahat	+= signref[q] * df1Adz(ss,eta,-z,FracType);
			l2Ahat	+= signref[q] * df2Adz(ss,eta,-z,FracType);
			l3Ahat	+= signref[q] * df3Adz(ss,eta,-z,FracType);
		}
		double xtilt = U/(2*PI) * (l1A + l1Ahat + l1B + u1C + z * l1C);
		double ytilt = U/(2*PI) * ( (l2A + l2Ahat + l2B + u2C + z * l2C) * cosdip -
						(l3A + l3Ahat + l3B + u3C + z * l3C) * sindip );
		if ((xtilt > 1e-3) || (ytilt > 1e-3))
		{
			xtilt = 2. * xtilt / 2.;
		}

		xtilt1[0] = xtilt;
		ytilt1[0] = ytilt;
	}
	
	//in NED coordinates
	public void calPartialxyOverZ(double sz, double L, double W, double U, double strike, double dip, 
			double rn, double re, double rz, double [] disp, int FracType, double alpha1) {
		//center
		//re -= se; rn -= sn;
		//rotation along Z axis
		double sinAz = Math.sin((90-strike)*Math.PI/180.0);
		double cosAz = Math.cos((90-strike)*Math.PI/180.0);
		double sinDip = Math.sin(dip*Math.PI/180.0);
		double cosDip = Math.cos(dip*Math.PI/180.0);
		double ae = re * cosAz + rn * sinAz;
		double an = -re * sinAz + rn * cosAz;
		//move center to left bottom corner
		double de = 0.5*L;
		double dn = 0.5*W*cosDip;
		double dz = 0.5*W*sinDip;
		double c = sz+dz;
			
		double be = ae+de;
		double bn = an+dn;
		double bz = -rz;
		
		calPartialxyOverZ(c, L, W, U, dip, be, bn, bz, disp, FracType, alpha1);
		
		double theSin = Math.sin(strike*Math.PI/180.0);
		double theCos = Math.cos(strike*Math.PI/180.0);
		double ux = disp[0];
		double uy = disp[1];
		
		double vx = ux*theSin-uy*theCos;
		double vy = ux*theCos+uy*theSin;
		
		disp[0] = vx;
		disp[1] = vy;
	}
	
	// all are in Okada coordinate system
	protected void calPartialxyOverZ(double c, double L, double W, double U, double dip, double easting, double northing, double zpos, 
			double [] tilt, int FracType, double alpha1)
	{
		alpha = alpha1;		
		cosdip = Math.cos(PI/180. * dip);
		sindip = Math.sin(PI/180. * dip);
		
		// Frac type is 0 for tensile, 1 for strikeslip, and 2 for dipslip.
		double l1A=0, l1Ahat=0, l1B=0, u1C=0, l1C=0;
		double l2A=0, l2Ahat=0, l2B=0, u2C=0, l2C=0;
		double l3A=0, l3Ahat=0, l3C=0, u3C=0, l3B=0;
		double ss, eta;

		double z = zpos;
		// First evaluate at x, p, zpos
		// calculate at x - L, p, zpos::
		// calculate at x, p - W, zpos::
		// calculate at x - L, p - W, zpos::

		double [] sschange 	= new double [] {0, -L, 0, -L};
		double [] etachange = new double [] {0, 0, -W, -W};
		double [] signref 	= new double [] {1, -1, -1, 1};

		int q;
		int retval = 0;
		for (q = 0 ; q < 4 ; q ++){
			d = c-z;
			p = northing * cosdip + d * sindip;
			ss = easting + sschange[q];
			eta = p + etachange[q];

			retval = calValues(northing,c,ss,eta,z);
			l1A		+= signref[q] * df1Adz(ss,eta,z,FracType);
			l1B		+= signref[q] * df1Bdz(ss,eta,z,FracType);
			u1C		+= signref[q] * f1C(ss,eta,z,FracType);
			if (z != 0) l1C		+= signref[q] * df1Cdz(ss,eta,z,FracType);

			l2A		+= signref[q] * df2Adz(ss,eta,z,FracType);
			l2B		+= signref[q] * df2Bdz(ss,eta,z,FracType);
			u2C		+= signref[q] * f2C(ss,eta,z,FracType);
			if (z != 0) l2C		+= signref[q] * df2Cdz(ss,eta,z,FracType);

			l3A		+= signref[q] * df3Adz(ss,eta,z,FracType);
			l3B		+= signref[q] * df3Bdz(ss,eta,z,FracType);
			u3C		+= signref[q] * f3C(ss,eta,z,FracType);
			if (z != 0) l3C		+= signref[q] * df3Cdz(ss,eta,z,FracType);

			d = c + z;
			p = northing * cosdip + d * sindip;
			ss = easting + sschange[q];
			eta = p + etachange[q];
			retval = calValues(northing,c,ss,eta,-z);
			l1Ahat	+= signref[q] * df1Adz(ss,eta,-z,FracType);
			l2Ahat	+= signref[q] * df2Adz(ss,eta,-z,FracType);
			l3Ahat	+= signref[q] * df3Adz(ss,eta,-z,FracType);
		}
		double xtilt = U/(2*PI) * (l1A + l1Ahat + l1B + u1C + z * l1C);
		double ytilt = U/(2*PI) * ( (l2A + l2Ahat + l2B + u2C + z * l2C) * cosdip -
						(l3A + l3Ahat + l3B + u3C + z * l3C) * sindip );
		//if ((xtilt > 1e-3) || (ytilt > 1e-3)) { xtilt = 2. * xtilt / 2.; }
		//double ztilt = U/(2*PI) * ( (l2A + l2Ahat + l2B - u2C - z * l2C) * sindip +
		//		(l3A + l3Ahat + l3B - u3C - z * l3C) * cosdip );

		tilt[0] = xtilt;
		tilt[1] = ytilt;
	}

	protected void calDisplacementZ(double c, double L, double W, double U, double dip, double easting, double northing, double zpos,
			double [] disp, int FracType, double alpha1) {
		// Frac type is 0 for tensile, 1 for strikeslip, and 2 for dipslip.
		double u2A=0, u2Ahat=0, u2B=0, u2C=0;
		double u3A=0, u3Ahat=0, u3B=0, u3C=0;
		double ss, eta, z;

		alpha = alpha1;		
		cosdip = Math.cos(PI/180. * dip);
		sindip = Math.sin(PI/180. * dip);
		z = zpos;

		double [] sschange 	= new double [] {0, -L, 0, -L};
		double [] etachange = new double [] {0, 0, -W, -W};
		double [] signref 	= new double [] {1, -1, -1, 1};

		int q;
		for (q = 0 ; q < 4 ; q ++)		{
			d = c-z;
			p = northing * cosdip + d * sindip;
			ss = easting + sschange[q];
			eta = p + etachange[q];

			calValues(northing,c,ss,eta,z);
			u2A		+= signref[q] * f2A(ss,eta,z,FracType);
			u3A		+= signref[q] * f3A(ss,eta,z,FracType);
			u2B		+= signref[q] * f2B(ss,eta,z,FracType);
			u3B		+= signref[q] * f3B(ss,eta,z,FracType);
			if (z!=0)
			{
				u2C	+= signref[q] * f2C(ss,eta,z,FracType);
				u3C	+= signref[q] * f3C(ss,eta,z,FracType);
			}

			d = c + z;
			p = northing * cosdip + d * sindip;
			ss = easting + sschange[q];
			eta = p + etachange[q];
			calValues(northing,c,ss,eta,-z);
			u2Ahat	+= signref[q] * f2A(ss,eta,-z,FracType);
			u3Ahat	+= signref[q] * f3A(ss,eta,-z,FracType);
		}
		disp[0] = U/(2.*PI) * ( (u2A-u2Ahat + u2B - z*u2C) * sindip +
				( u3A - u3Ahat + u3B - z * u3C ) * cosdip);
	}

	private int calValues(double y, double c, double ss, double eta, double z)	{
		q = y * sindip - d * cosdip;
		R = my_sqrt (ss * ss + eta * eta + q * q);
		yt = eta * cosdip + q * sindip;
		dt = eta * sindip - q * cosdip;
		ct = dt + z;

//		if (R == 0) return 0; 
//		if (R + ss == 0) return 0; 
//		if (R + eta == 0) return 0; 
//		if (R + dt == 0) return 0; 
//		if (cosdip == 0) return 0; 
//		if (ss == 0) return 0; 
//		if (q == 0) return 0; 

		X11 = 1. / (R * (R + ss));
		X32 = (2. * R + ss)/(R*R*R * (R + ss)*(R + ss));
		X53 = (8. * R*R + 9. * R * ss + 3. * ss*ss)/(Math.pow(R,5) * Math.pow(R+ss,3));

		Y11 = 1./(R * (R + eta));
		Y32 = (2. * R + eta)/(R*R*R * (R + eta) * (R + eta));
		Y53 = (8. *R*R + 9. * R * eta + 3. * eta*eta)/(Math.pow(R,5)*Math.pow(R+eta,3));
		h = q * cosdip - z;
		Z32 = sindip/(R*R*R) - h * Y32;
		Z53 = 3. * sindip/Math.pow(R,5) - h * Y53;
		Y0 = Y11 - ss*ss * Y32;
		Z0 = Z32 - ss*ss * Z53;

		E1 = cosdip/R + dt*q/(R*R*R);
		F1 = yt/(R*R*R) + ss*ss * Y32 * cosdip;
		G1 = 2. * X11 * cosdip + dt * q * X32;
		H1 = yt * q * X32 + ss * q * Y32 * cosdip;
		P1 = sindip / (R*R*R) - q * Y32 * cosdip;
		Q1 = 3. * ct * yt / Math.pow(R,5) + q * Y32 -
				(z * Y32 + Z32 + Z0) * cosdip;

		D11 = 1. / (R * (R + dt));
		K1 = ss/cosdip * (D11 - Y11 * sindip);
		K3 = 1./cosdip * (q * Y11 - yt * D11);
		K2 = 1./R + K3 * sindip;
		K4 = ss * Y11 * cosdip - K1 * sindip;

		double X = my_sqrt(ss*ss+q*q);
		if (R+eta==0) {
			if (cosdip != 0) 	I3 = 1./cosdip*yt/(R+dt)-1./(cosdip*cosdip)*(-my_log(R-eta)-sindip*my_log(R+dt));
			else 				I3 = 1./2.*(eta/(R+dt)+yt*q/((R+dt)*(R+dt)) + my_log(R-eta));
		} else {
			if (cosdip != 0) 	I3 = 1./cosdip*yt/(R+dt)-1./(cosdip*cosdip)*(my_log(R+eta)-sindip*my_log(R+dt));
			else 				I3 = 1./2.*(eta/(R+dt)+yt*q/((R+dt)*(R+dt)) - my_log(R+eta));
		}
		
		I2 = my_log(R+dt)+I3*sindip;
		
		if (eta==0) I4 = 0;
		else {
			if (cosdip != 0) 
				I4 = sindip/cosdip*ss/(R+dt)+2./(cosdip*cosdip)*
				Math.atan( (eta*(X+q*cosdip)+X*(R+X)*sindip)/(ss*(R+X)*cosdip));
			else I4 = 1./2.*ss*yt/((R+dt)*(R+dt));
		}
		
		I1 = -ss/(R+dt)*cosdip - I4*sindip;
		if (q==0) Theta = 0;
		else Theta = Math.atan(ss*eta/(q*R));
		return 1; 
	}

	private double df1Adz(double ss, double eta, double z, int FracType)	{
		switch (FracType)		{
		case 0:
			return (1.-alpha)/2. * ( sindip / R - q * Y11 * cosdip)
					- alpha/2. * q * F1;
		case 1:
			return (1.-alpha)/2. * ss * Y11 * cosdip + yt / 2. * X11
					+ (alpha/2.) * ss * F1;
		case 2:
			return (alpha/2.) * E1;
		default: return 0;
		}
	}

	private double df1Bdz(double ss, double eta, double z, int FracType){
		if (alpha == 0) {return 0;}
		switch (FracType)	{
		case 0:
			return q*F1 + (1.-alpha)/alpha * K3 * sindip * sindip;
		case 1:
			return -ss * F1 - yt * X11 + (1.-alpha)/alpha * K1 * sindip;
		case 2:
			return -E1 - (1.-alpha)/alpha * K3 * sindip * cosdip;
		default: return 0;
		}
	}

	private double df1Cdz(double ss, double eta, double z, int FracType)	{
		if (R == 0) {return 0;}
		switch (FracType)		{
		case 0:
			return -eta/(R*R*R) + Y0 * (cosdip * cosdip)
					- alpha * (z/(R*R*R) * sindip - 3.*ct*yt*q/Math.pow(R,5) -
							Y0*sindip*sindip + q * Z0 * cosdip);
		case 1:
			return (1.-alpha) * ss * P1 * cosdip - alpha * ss * Q1;
		case 2:
			return -q/(R*R*R) + Y0*sindip*cosdip
					- alpha * ((ct+dt)/(R*R*R)*cosdip + 3*ct*dt*q/(R*R*R*R*R));
		default: return 0;
		}
	}

	private double df2Adz(double ss, double eta, double z, int FracType)	{
		switch (FracType)		{
		case 0:
			return (1.-alpha)/2. * dt * X11 - alpha/2. * q * G1;
		case 1:
			return alpha/2. * E1;
		case 2:
			return (1.-alpha)/2. * yt * X11 + ss / 2. * Y11 * cosdip
					+ (alpha/2.) * eta * G1;
		default: return 0;
		}
	}

	private double df2Bdz(double ss, double eta, double z, int FracType)	{
		if (alpha == 0) {return 0;}
		switch (FracType)		{
		case 0:
			return q * G1 + (1.-alpha)/alpha * ss * D11 * sindip * sindip;
		case 1:
			return -E1 + (1.-alpha)/alpha * yt * D11 * sindip;
		case 2:
			return -eta * G1 - ss * Y11 * cosdip
					- (1.-alpha)/alpha * ss * D11 * sindip * cosdip;
		default: return 0;
		}
	}

	private double df2Cdz(double ss, double eta, double z, int FracType)	{
		if (R == 0) {return 0;}
		switch (FracType)		{
		case 0:
			return (1.-alpha) * 2. * ss * P1 * sindip - X11 + dt*dt* X32
					- alpha * ct * ( (dt - 2. * q * cosdip) * X32 - dt * q*q * X53);
		case 1:
			return 2. * (1.-alpha) * (yt/(R*R*R)-Y0*cosdip) * sindip
					+ dt/(R*R*R) * cosdip - alpha * ((ct+dt)/(R*R*R)*cosdip + (3*ct*dt*q)/(R*R*R*R*R));
		case 2:
			return (1.-alpha) * yt * dt * X32 - alpha * ct * ((yt-2*q*sindip)*X32+dt*eta*q*X53);
		default: return 0;
		}
	}

	private double df3Adz(double ss, double eta, double z, int FracType)	{
		if (R == 0) { return 0;}
		switch (FracType)		{
		case 0:
			return (1.-alpha)/2. * (yt * X11 + ss * Y11 * cosdip)
					+ alpha/2. * q * H1;
		case 1:
			return -(1.-alpha)/2.*(sindip/R-q*Y11*cosdip)-alpha/2.*q*F1;
		case 2:
			return -(1.-alpha)/2.*dt*X11-alpha/2.*q*G1; 
		default: return 0;
		}
	}

	private double df3Bdz(double ss, double eta, double z, int FracType)	{
		if (alpha == 0) { return 0;}
		switch (FracType)		{
		case 0:
			return -q * H1 + (1.-alpha)/alpha * K4 * sindip * sindip;
		case 1:
			return q * F1 + (1.-alpha)/alpha * K2 * sindip;
		case 2:
			return q * G1 - (1.-alpha)/alpha * K4 * sindip * cosdip;
		default: return 0;
		}
	}

	private double df3Cdz(double ss, double eta, double z, int FracType)	{
		if (R == 0) { return 0;}
		switch (FracType)		{
		case 0:
			return (1.-alpha) * (ss * P1 * cosdip + yt * dt * X32) +
					alpha * ct * ( (yt - 2. * q * sindip) * X32 + dt * eta * q * X53) +
					alpha * ss * Q1;
		case 1:
			return (yt/(R*R*R)-Y0*cosdip)*cosdip
					- alpha * ((ct+dt)/(R*R*R)*sindip - (3*ct*yt*q)/(R*R*R*R*R)-Y0*sindip*sindip+q*Z0*cosdip);
		case 2:
			return -ss * P1 * sindip + X11 - dt*dt*X32
					- alpha * ct * ((dt-2*q*cosdip)*X32 - dt*q*q*X53);
		default: return 0;
		}
	}

	private double f1A(double ss, double eta, double z, int FracType)	{
		if (R == 0) { return 0;}
		switch (FracType)		{
		case 0:
			if(R+eta==0) {
				return (1.-alpha)/2.*my_log(R-eta)-alpha/2.*q*q*Y11;
			} else {
				return -(1.-alpha)/2.*my_log(R+eta)-alpha/2.*q*q*Y11;
			}
		case 1:
			return Theta/2.+alpha/2.*ss*q*Y11;
		case 2:
			return alpha/2.*q/R;
		default: return 0;
		}
	}

	private double f2A(double ss, double eta, double z, int FracType)	{
		if (R == 0) {return 0;}
		switch (FracType)		{
		case 0:
			if(R+ss==0) {
				return (1.-alpha)/2.*my_log(R-ss)-alpha/2.*q*q*X11;
			} else {
				return -(1.-alpha)/2.*my_log(R+ss)-alpha/2.*q*q*X11;
			}
		case 1:
			return alpha/2.*q/R;
		case 2:
			return Theta/2.+alpha/2.*eta*q*X11;
		default: return 0;
		}
	}

	private double f3A(double ss, double eta, double z, int FracType)	{
		switch (FracType)	{
		case 0:
			return Theta/2.-alpha/2.*q*(eta*X11+ss*Y11);
		case 1:
			if(R+eta==0) {
				return -(1.-alpha)/2.*my_log(R-eta)-alpha/2.*q*q*Y11;
			} else {
				return (1.-alpha)/2.*my_log(R+eta)-alpha/2.*q*q*Y11;
			}
		case 2:
			if(R+ss==0) {
				return -(1.-alpha)/2.*my_log(R-ss )-alpha/2.*q*q*X11;
			} else {
				return (1.-alpha)/2.*my_log(R+ss )-alpha/2.*q*q*X11;
			}			
		default: return 0;
		}
	}

	private double f1B(double ss, double eta, double z, int FracType)	{
		if (alpha == 0) { return 0;}
		if (R == 0) { return 0;}
		switch (FracType)		{
		case 0:
			return q*q*Y11-(1.-alpha)/alpha*I3*sindip*sindip;
		case 1:
			return -ss*q*Y11-Theta- (1.-alpha)/alpha*I1*sindip;
		case 2:
			return -q/R + (1.-alpha)/alpha*I3*sindip*cosdip;
		default: return 0;
		}
	}

	private double f2B(double ss, double eta, double z, int FracType)	{
		if (alpha == 0) { return 0;}
		if (R == 0) { return 0;}
		if (R+dt == 0) { return 0;}
		switch (FracType)		{
		case 0:
			return q*q*X11+(1.-alpha)/alpha*ss/(R+dt)*sindip*sindip;
		case 1:
			return -q/R + (1.-alpha)/alpha * yt/(R+dt)*sindip;
		case 2:
			return -eta*q*X11-Theta-(1.-alpha)/alpha*ss/(R+dt)*sindip*cosdip;
		default: return 0;
		}
	}

	private double f3B(double ss, double eta, double z, int FracType)	{
		if (alpha == 0) { return 0;}
		switch (FracType)		{
		case 0:
			return q*(eta*X11+ss*Y11)-Theta-(1.-alpha)/alpha *I4*sindip*sindip;
		case 1:
			return q*q*Y11-(1.-alpha)/alpha*I2*sindip;
		case 2:
			return q*q*X11+(1.-alpha)/alpha*I4*sindip*cosdip;
		default: return 0;
		}
	}

	private double f1C(double ss, double eta, double z, int FracType)	{
		if (R == 0) { return 0;}
		switch (FracType)		{
		case 0:
			return -(1.-alpha)*(sindip/R + q * Y11 * cosdip)
					- alpha * (z * Y11 - q*q * Z32);
		case 1:
			return (1.-alpha)*ss*Y11*cosdip - alpha * ss * q * Z32;
		case 2:
			return (1.-alpha)*cosdip/R - q * Y11 * sindip - alpha * ct*q/(R*R*R);
		default: return 0;
		}
	}

	private double f2C(double ss, double eta, double z, int FracType)	{
		if (R == 0) { return 0;}
		switch (FracType)		{
		case 0:
			return (1.-alpha) * 2. * ss * Y11 * sindip + dt * X11
					- alpha * ct * (X11 - q*q * X32);
		case 1:
			return (1.-alpha) * (cosdip/R + 2*q*Y11*sindip) - alpha * ct * q / (R*R*R);
		case 2:
			return (1.-alpha) * yt * X11 - alpha * ct * eta * q * X32;
		default: return 0;
		}
	}

	private double f3C(double ss, double eta, double z, int FracType)	{
		if (R == 0) { return 0;}
		switch (FracType)		{
		case 0:
			return (1-alpha) * (yt * X11 + ss * Y11 * cosdip) +
					alpha * q * (ct * eta * X32 + ss * Z32);
		case 1:
			return (1.-alpha)*q*Y11*cosdip - alpha*(ct*eta/(R*R*R)-z*Y11+ss*ss*Z32);
		case 2:
			return -dt*X11 - ss * Y11*sindip - alpha * ct *(X11-q*q*X32);
		default: return 0;
		}
	}

	private double my_log (double x) 	{
		if (x <= 0.0)  return (0.0); 
		else  return (Math.log(x)); 
	}
	private double my_sqrt(double x) 	{
		if (x < 0)  return (0.0);
		else return (Math.sqrt(x)); 
	}


}
