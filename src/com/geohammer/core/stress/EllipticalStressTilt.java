package com.geohammer.core.stress;

//G:\TFS\DuDevelopment\JointMSTilt\JointInv\DataEngine.cpp
public class EllipticalStressTilt extends EllipticalStressCore {

	public EllipticalStressTilt() {

	}
	
	public void trip(double xx, double yy, double zz, double a, double b, double []vlam, double []vmu, double []vnu) {
		double as, bs, xs, ys, zs, a0, a1, a2, q, r, test;
		double arg1, arg2, thet, res1, res2, ims1, ims2, r1r, r1i, d3;
		double r2r, r2i, r3r, r3i, root1, root2, root3, tb, tc;
		//function to get lam, mu and nu values from cartesian position
		//solves a cubic equation in lam
		as = a * a;
		bs = b * b;
		xs = xx * xx;
		ys = yy * yy;
		zs = zz * zz;
		a0 = -as * bs * zs;
		a1 = as * bs - bs * xs -as * ys - as * zs - bs * zs;
		a2 = as + bs - xs - ys - zs;
		q = a1 / 3.0 - a2 * a2 / 9.0;
		r = (a1 * a2 - 3.0 * a0) / 6.0 - a2 * a2 * a2 / 27.0;
		test = q * q * q + r * r;
		if(test >= 0.0) {
			arg1 = r + Math.sqrt(test);
			arg2 = r - Math.sqrt(test);
			thet = 0.0;
		} else {
			arg1 = Math.sqrt(r * r + Math.abs(test));
			arg2 = arg1;
			thet = Math.atan(Math.sqrt(Math.abs(test)) / r);
			if(r < 0.0) thet = thet + PI;
		}
		res1 = Math.pow(arg1, (1.0/3.0)) * Math.cos(thet / 3.0);
		res2 = Math.pow(arg2, (1.0/3.0)) * Math.cos(thet / 3.0);
		ims1 = Math.pow(arg1, (1.0/3.0)) * Math.sin(thet / 3.0);
		ims2 = -Math.pow(arg2, (1.0/3.0)) * Math.sin(thet / 3.0);
		r1r = res1 + res2 - a2 / 3.0;
		r1i = ims1 + ims2;
		d3 = Math.sqrt(3.0);
		r2r = -0.50 * (res1 + res2) - a2 / 3.0 - d3 / 2.0 * (ims1 - ims2);
		r2i = -0.50 * (ims1 + ims2) + d3 / 2.0 * (res1 - res2);
		r3r = -0.50 * (res1 + res2) - a2 / 3.0 + d3 / 2.0 * (ims1 - ims2);
		r3i = -0.50 * (ims1 + ims2) - d3 / 2.0 * (res1 - res2);
		root1 = -1.e+35;
		root2 = -1.e+35;
		root3 = -1.e+35;
		if(Math.abs(r1i) < 1.e-20) root1 = r1r;
		if(Math.abs(r2i) < 1.e-20) root2 = r2r;
		if(Math.abs(r3i) < 1.e-20) root3 = r3r;

		if(root1 >= 1.e-10) {
			vlam[0] = root1;
			if(root2 > 1.e-10 || root3 >= 1.e-10)
			{
				//AfxMessageBox("Several Real Positive Roots", MB_OK, 0);
				if( ((root1 / root2) > 1.e+04 && (root1 / root3) > 1.e+04)  ||
						(root2 < 0.0 && (root1 / root3) > 1.e+04)  ||
						((root1 / root2) > 1.e+04  &&  root3 < 0.0) ) {
					//AfxMessageBox("Other Roots Are Small; Ignore Them", MB_OK, 0);
				}
			}
		} else if(root2 >= 1.e-10) {
			vlam[0] = root2;
			if(root3 >= 1.e-10)
			{
				//AfxMessageBox("Problem: 2 Real Positive Rroots", MB_OK, 0);
				if((root2 / root3) > 1.e+04 || root3 < 0.0) {
					//AfxMessageBox("Other Roots Are Small; Ignore Them", MB_OK, 0);
				}
			}
		} else if(root3 > 1.e-10) {
			vlam[0] = root3;
		} else {
			//AfxMessageBox("No Real Positive Roots", MB_OK, 0);
		}

		tb = a * a * a * a * (vlam[0]) + a * a * (vlam[0]) * (vlam[0]) - a * a * (vlam[0]) * xx * xx +
				b * b * (vlam[0]) * xx * xx + a * a * b * b * zz * zz + b * b * (vlam[0]) * zz * zz;
		tc = 4.0 * a * a * b * b * (vlam[0]) * Math.pow((a * a + (vlam[0])), 2) * zz * zz;
		vmu[0] = (-tb + Math.sqrt(tb * tb - tc)) / (2.0 * (vlam[0]) * (a * a + (vlam[0])));
		if(vmu[0] < (-b  *b)) vmu[0] = -b * b;
		if(vmu[0] > 0.0) vmu[0] = -0.00000001;
		//last get nu
		if(vlam[0] > 0.0)
			vnu[0] = a * a * b * b * zz * zz / ((vlam[0]) * (vmu[0]));
		else
			vnu[0] = -1.e+10;
		return;
	}

	public void tilts(double xx, double yy, double zz, double a, double b, double vlam, double vmu, double vnu,
			double poiss, double acon, double []dudx, double []dudy, double []dudz, 
			double []dvdx, double []dvdy, double []dvdz, double []dwdx, double []dwdy, double []dwdz)
	{
		double ak, akp, snu2, snu, cnu, dnu, u, euk;
		double h1sq, dmdy, dndy, dldy, dldx, dldz, parts;
		double dldx2, dldy2, dldz2, dldxdy, dldydz, dldxdz, dudl, du2dl;
		double arg0, arg1, arg2, fac, term1, term2, term3, term4;
		double arga, argb, argc, terma, termb, termc;
		double arg5, argd, argf, argg, argh;
		//function to calculate tilts around an elliptic crack
		//xtilt is the tilt parallel to crack length
		//ytilt is the tilt in the direction of crack opening

		//get the values of the jacobian elliptic functions
		ak = Math.sqrt(a * a - b * b) / a;
		if(ak > 0.9999999) ak = 0.9999999;
		akp = Math.sqrt(1.0 - ak * ak);
		snu2 = a * a / (a * a + vlam);
		snu = Math.sqrt(snu2);
		cnu = Math.sqrt(1.0 - snu2);
		dnu = Math.sqrt(1.0 - ak * ak * snu2); 
		//get the parameter u based on value of snu
		u = getu(snu, ak);
		//get the value of the incomplete elliptic integral, e(u,k)
		euk = geteuk(snu, ak);

		//start with general functions
		h1sq = (vlam - vmu) * (vlam - vnu) / (4.0 * (a * a + vlam) * (b * b + vlam) * vlam);
		//h2sq = (vmu-vnu)*(vmu-vlam)/(4.0*(a*a+vmu)*(b*b+vmu)*vmu)
		//h3sq = (vnu-vlam)*(vnu-vmu)/(4.0*(a*a+vnu)*(b*b+vnu)*vnu)
		dmdy = 2.0 * yy * vmu * (a * a + vmu) / ((vmu - vnu) * (vmu - vlam));
		dndy = 2.0 * yy * vnu * (a * a + vnu) / ((vnu - vlam) * (vnu - vmu));
		dldy = 2.0 * yy * vlam * (a * a + vlam) / ((vlam - vmu) * (vlam - vnu));
		dldx = 2.0 * xx * vlam * (b * b + vlam) / ((vlam - vmu) * (vlam - vnu));
		dldz = 2.0 * zz * (a * a + vlam) * (b * b + vlam) / ((vlam - vmu) * (vlam - vnu));
		parts = xx * xx / Math.pow((a * a + vlam), 3) + yy * yy / Math.pow((b * b + vlam), 3) + zz * zz / Math.pow(vlam, 3);
		dldx2 = (-xx * xx / (Math.pow((a * a + vlam), 3) * h1sq) + 1.0 / (a * a + vlam)) / (2.0 * h1sq)
				+ (parts * xx * xx / (2.0 * Math.pow((a * a + vlam), 2) * h1sq * h1sq)) / (4.0 * h1sq);
		dldy2 = (-yy * yy / (Math.pow((b * b + vlam), 3) * h1sq) + 1.0 / (b * b + vlam)) / (2.0 * h1sq)
				+ (parts * yy * yy / (2.0 * Math.pow((b * b + vlam), 2) * h1sq*h1sq)) / (4.0 * h1sq);
		dldz2 = (-zz * zz / (vlam * vlam * vlam * h1sq) + 1.0 / vlam) / (2.0 * h1sq)
				+ (parts * zz * zz / (2.0 * vlam * vlam * h1sq * h1sq)) / (4.0 * h1sq); 
		dldxdy = (-(a * a + b * b + 2.0 * vlam) / (Math.pow((a * a + vlam), 2) * Math.pow((b * b + vlam), 2)) 
				* xx * yy / h1sq + 0.50 / (h1sq * h1sq) * parts * xx * yy /((a * a + vlam) * (b * b + vlam))) /
				(4.0 * h1sq);
		dldydz = (-(b * b + 2.0 * vlam) / (vlam * vlam * Math.pow((b * b + vlam), 2)) * yy * zz / h1sq
				+ 0.50 / (h1sq * h1sq) * parts * yy * zz / (vlam * (b * b + vlam))) / (4.0 * h1sq);
		dldxdz = (-(a * a + 2.0 * vlam) / (vlam * vlam * Math.pow((a * a + vlam), 2)) * xx * zz /h1sq
				+ 0.50 / (h1sq * h1sq) * parts * xx * zz / (vlam * (a * a + vlam))) / (4.0 * h1sq);

		dudl = -a / (2.0 * Math.sqrt((a * a + vlam) * vlam * (vlam + a * a * (1.0 - ak * ak))));
		du2dl = -(3.0 * snu2 + snu2 * snu2 / (cnu * cnu) + ak * ak * snu2 * snu2 / (dnu * dnu)) /
				(2.0 * a * a) * dudl;
		//first term
		arg0 = 2.0 * acon * zz / (a * b * b) * (-ak * ak * snu2 + dnu * dnu * snu2 / (cnu * cnu));
		term1 = arg0 * dudl * dldy;
		//second term
		arg1 = -ak * ak * snu2 + snu2 * dnu * dnu / (cnu * cnu);
		arg2 = 2.0 * snu * (-ak * ak * cnu * dnu + dnu * dnu * dnu / cnu - ak * ak * snu2 * dnu / cnu
				+ snu2 * dnu * dnu * dnu / (cnu * cnu * cnu));
		fac = 2.0 * acon / (a * b * b);
		term2 = fac * arg1 * dudl * dldy;
		term3 = fac * zz * arg2 * dudl * dudl * dldz * dldy;
		term4 = fac * zz * arg1 * (du2dl * dldz * dldy + dudl * dldydz);
		dwdy[0] = -8.0 * (1.0 - poiss) * term1 + 4.0 * zz * (term2 + term3 + term4); 

		//get the x tilts
		terma = xx * (-dnu * dnu / (ak * ak) + 1.0 / (ak * ak)) * dudl * dldy;
		argb = 2.0 * (-ak * ak * snu * cnu * dnu + snu * dnu * dnu * dnu / cnu - ak * ak * snu * snu2 *
				dnu / cnu + snu * snu2 * dnu * dnu * dnu / (cnu * cnu * cnu));
		argc = -ak * ak * snu2 + snu2 * dnu * dnu / (cnu * cnu);
		termb = argb * dudl * dudl * dldx * dldy;
		termc = argc * (du2dl * dldy * dldx + dudl * dldxdy);
		dudy[0] = 8.0 * (1.0 - 2.0 * poiss) * acon / (a * a * a) * terma
				+ 8.0 * acon * zz * zz / (a * b * b) * (termb + termc);

		//get the rest of the gradients
		term1 = arg0 * dudl * dldx;
		term2 = fac * arg1 * dudl * dldx;
		term3 = fac * zz * arg2 * dudl * dudl * dldz * dldx;
		term4 = fac * zz * arg1 * (du2dl * dldz * dldx + dudl * dldxdz);
		dwdx[0] = -8.0 * (1.0 - poiss) * term1 + 4.0 * zz * (term2 + term3 + term4);

		arga = (dnu * dnu - akp * akp - ak * ak * (cnu * cnu - snu2) - ak * ak * ak * ak * snu2 *
				(cnu * cnu) / (dnu * dnu)) / (ak * ak * akp * akp);
		terma = yy * arga * dudl * dldx;
		termb = argb * dudl * dudl * dldx * dldy;
		termc = argc * (du2dl * dldy * dldx + dudl * dldxdy);
		dvdx[0] = 8.0 * (1.0 - 2.0 * poiss) * acon / (a * a * a) * terma
				+ 8.0 * acon * zz * zz / (a * b * b) * (termb + termc);

		arg5 = euk - akp * akp * u - ak * ak * snu * cnu / dnu;
		term1 = arg5 * acon / (ak * ak * akp * akp * a * a * a);
		term2 = acon * yy * arga / (a * a * a) * dudl * dldy;
		term3 = fac * zz * zz * arg2 * dudl * dudl * dldy * dldy;
		term4 = fac * zz * zz * arg1 * (du2dl * dldy * dldy + dudl * dldy2);
		dvdy[0] = 8.0 * (1.0 - 2.0 * poiss) * (term1 + term2) + 4.0 * (term3 + term4);

		argd = snu * dnu / cnu - euk;
		argf = akp * akp * snu * dnu / (cnu * cnu * cnu);
		term1 = argd + zz * arg1 * dudl * dldz;
		term2 = 2.0 * arg1 * dudl * dldz;
		term3 = 2.0 * zz *argf * dudl * dudl * dldz * dldz;
		term4 = zz * arg1 * (du2dl * dldz * dldz + dudl * dldz2);
		dwdz[0] = -4.0 * (1.0 - 2.0 * poiss) * fac * term1
				+ 4.0 * zz * fac * (term2 + term3 + term4);

		argg = (-dnu * dnu + 1.0) / (ak * ak);
		term1 = acon * xx / (a * a * a) * argg * dudl * dldz;
		term2 = fac * zz * arg2 * dudl * dudl * dldx * dldz;
		term3 = fac * zz * arg1 * (du2dl * dldx * dldz + dudl * dldxdz);
		term4 = 2.0 * fac * arg1 * dudl * dldx;
		dudz[0] = 8.0 * (1.0 - 2.0 * poiss) * term1 + 4.0 * zz * (term2 + term3 + term4);

		term1 = acon * yy / (a * a * a) * arga * dudl * dldz;
		term2 = fac * zz * arg2 * dudl * dudl * dldy * dldz;
		term3 = fac * zz * arg1 * (du2dl * dldy * dldz + dudl * dldydz);
		term4 = 2.0 * fac * arg1 * dudl * dldy;
		dvdz[0] = 8.0 * (1.0 - 2.0 * poiss) * term1 + 4.0 * zz * (term2 + term3 + term4);

		argh = (-euk + u) / (ak * ak);
		term1 = acon / (a * a * a) * (argh + xx * argg * dudl * dldx);
		term2 = fac * zz * zz * arg2 * dudl * dudl * dldx * dldx;
		term3 = fac * zz * zz * arg1 * (du2dl * dldx * dldx + dudl * dldx2);
		dudx[0] = 8.0 * (1.0 - 2.0 * poiss) * term1 + 4.0 * (term2 + term3);
	}

	protected double ellint(double akp) {
		double ek, a1, a2, a3, a4, b1, b2, b3, b4;
		//	     function to get ek for quarter period using expansion
		//	     from abramowitz and stegun
		//   first check for near zero argument and return value of 1
		if(akp < 1.e-10)		{
			ek = 1.0;
			return ek;
		}
		a1 = 0.443251414630;
		a2 = 0.062606012200;
		a3 = 0.047573835460;
		a4 = 0.017365064510;
		b1 = 0.249983683100;
		b2 = 0.092001800370;
		b3 = 0.040696975260;
		b4 = 0.005264496390;
		ek = 1.0 + a1 * akp + a2 * akp * akp + a3 * akp * akp * akp + a4 * akp * akp * akp * akp +
				(b1 * akp + b2 * akp * akp + b3 * akp * akp * akp+ b4 * akp * akp * akp * akp) *
				Math.log(1.0 / akp);
		return ek;
	}

	//    function to get the parameter u through integration
	//    of the elliptic integral using chebechev polynomials
	private double getu(double y, double ak) {
		int i, n, nend;
		double an, c, sum, denom, arg;
		double z = 0.0; //double [] z = new double[41];
		n = 40;
		nend  =n+1;
		an = (double)n;
		c = ak * ak;
		sum = 0.0;
		for(i = 0; i < nend; i++) {
			z = Math.cos((PI * (2.0*i + 1.0)) / (2.0 * an + 2.0));
			denom = (1.0 - Math.pow((y / 2.0 * (z + 1.0)), 2)) *
					(1.0 - c * Math.pow((y / 2.0 * (z + 1.0)), 2));
			arg = (1.0 - z * z) / denom;
			if(arg > 0.0) sum = sum + Math.sqrt(arg);
		}
		sum = PI * y / (2.0 * (an + 1.0)) * sum;
		return sum;

	}

	
	// subroutine to get the elliptic integral through integration using chebeshev polynomials
	private double geteuk(double y, double ak) {
		int i, n, nend;
		double an, c, sum, anum, denom, arg;
		double z = 0.0; //double [] z = new double[41];
		n = 40;
		nend = n + 1;
		an = (double)n;
		c = ak * ak;
		sum = 0.0;
		for(i = 0; i < nend; i++)	{
			z = Math.cos((PI * (2.0 * (double)i + 1.0)) / (2.0 * an + 2.0));
			denom = 1.0 - Math.pow((y / 2.0 * (z + 1.0)), 2);
			anum = 1.0 - c * Math.pow((y / 2.0 * (z + 1.0)), 2);
			arg = (1.0 - z * z) * anum / denom;
			if(arg > 0.0)  sum = sum + Math.sqrt(arg);
		}
		sum = PI * y / (2.0 * (an + 1.0)) * sum;
		return sum;
	}
}
