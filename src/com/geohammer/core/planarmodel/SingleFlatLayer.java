package com.geohammer.core.planarmodel;

import com.geohammer.rt.pseudo3d.Almost;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SingleFlatLayer extends SingleLayer3D {
	private double  topDepth 		= 0;
	private double  bottomDepth 	= 0;
	private double  thickness 		= 0;

	public SingleFlatLayer() { super();	}

	public SingleFlatLayer(double tDepth, double bDepth, String layerName, int id, double vp, double vs) {
		this(tDepth, bDepth, layerName, id, vp, vs, 0, 
				0, 0, 0, 0, 0, 0, 0);
	}
	public SingleFlatLayer(double tDepth, double bDepth, String layerName, int id, double vp, double vs, double den, 
			double delta, double epsilon, double gamma, double qp, double qs, double theta, double phi) {
		super(layerName, id, vp, vs, den, 
				0, 0, 0, 0, 0, 0,
				delta, epsilon, gamma, qp, qs, theta, phi, 
				2, 2, 1, 1, 0, 0, null);
		init(tDepth, bDepth);
		updateH();
	}
	public void init(double tDepth, double bDepth) {
		topDepth 	= tDepth;
		bottomDepth 	= bDepth;
	}
	public void shiftZ(double z) {
		topDepth 	+= z;
		bottomDepth 	+= z;
	}
	public void convertUnitSystem(double velScalor, double denScalor) {
		super.convertUnitSystem(velScalor, denScalor);
		topDepth 	*= velScalor;
		bottomDepth 	*= velScalor;
		updateH();
	}
	public void updateH() { thickness = bottomDepth-topDepth; }

	public SingleFlatLayer copy() { 
		SingleFlatLayer sl = new SingleFlatLayer(getTopDepth(), getBottomDepth(), getLayerName(), getId(), getVp(), getVs(), getDen(), 
				getDelta(), getEpsilon(), getGamma(), getQp(), getQs(), getTheta(), getPhi()); 
		sl.setSize(getNx(), getNy(), getNz(), getDx(), getDy(), getDz(), getX0(), getY0(), getZ0()); 
		return sl;
	}
	public SingleFlatLayer split(double z) {
		if(!inside(z)) return null;
		SingleFlatLayer lowerPart = copy();
		setBottomDepth(z);
		updateH();
		lowerPart.setTopDepth(z);
		lowerPart.updateH();

		return lowerPart;
	}
	public boolean inside(double z) 	{ return Almost.DOUBLE.cmp(z,topDepth)==1 && Almost.DOUBLE.cmp(z,bottomDepth)==-1; }
	public boolean onTDepth(double z) 	{ return Almost.DOUBLE.cmp(topDepth,z)==0; }
	public boolean onBDepth(double z) 	{ return Almost.DOUBLE.cmp(bottomDepth,z)==0; }
	public boolean include(double z) 	{ return inside(z)||onTDepth(z)||onBDepth(z); }

//	public double getTDepth() 					{ return topDepth; }
//	public double getBDepth() 					{ return bottomDepth; }
//	public double getH() 						{ return thickness; }
//
//	public void setTDepth(double tDepth) 		{ topDepth = tDepth; }
//	public void setBDepth(double bDepth) 		{ bottomDepth = bDepth; }
//	public void setH(double h) 					{ thickness = h; }

	public void scale(double lScalor, double dScalor) {
		super.scale(lScalor, dScalor);
		topDepth *= lScalor;
		bottomDepth *= lScalor;
		updateH();
	}

	public String toString() {
		return getId()+ " "+topDepth+ " "+bottomDepth+ " "+thickness+ " "+getVp()+ " "+getVs()+ " "+getDen()+ " "+
				getDelta()+ " "+getEpsilon()+ " "+getGamma()+ " "+getQp()+ " "+getQs()+" "+getTheta()+ " "+getPhi();
	}

	// sensitivity on elastic moduli (see Li et al., 2013)
	//vpha phase velocity
	//pa phase angle
	//vg group velocity
	//len raypath length
	//p 1-P; -1-SV; 0-SH;
	public double[] sensitivity(int iVp, double vpha, double pa, double vg, double len) {
		if(iVp==2) return sensitivitySH(vpha, pa, vg, len); //SH
		int sign = 1; // P
		if(iVp==3) sign = -1; //SV
		
		//System.out.println("pVel="+vpha+" pAngle="+pa+" gVel="+vg+" len="+len);
		double vpha2 = vpha*vpha;
		double vpha4 = vpha2*vpha2;
		double a = getC13()+getC55();
		double b = getC33()-getC55();
		double c = getC11()+getC33()-2.0*getC55();
		double e = getC11()-getC33();

		double theSin = Math.sin(pa);
		double theSin2 = theSin*theSin;
		double theCos = Math.cos(pa);
		double theCos2 = theCos*theCos;
		double the2Sin = Math.sin(2.0*pa);
		double the2Cos = Math.cos(2.0*pa);

		double D1 = 2.0*(2.0*a*a-b*c);
		double D2 = c*c - 4.0*a*a;
		double Dpa = Math.sqrt(b*b + D1*theSin2 + D2*theSin2*theSin2);
		double dv2dpa = e*theSin*theCos+sign*0.25*((D1*the2Sin+4.0*D2*theSin2*theSin*theCos)/Dpa);
		//System.out.println("D1="+D1+" D2="+D2+" Dpa="+Dpa+" dv2dpa="+dv2dpa);

		double dD1dc11 =2.0*( 4.0*a*0 - 0.0*c - b);
		double dD1dc13 =2.0*( 4.0*a*1 - 0.0*c - b*0);
		double dD1dc33 =2.0*( 4.0*a*0 - 1.0*c - b*1);
		double dD1dc55 =2.0*( 4.0*a*1 - (-1.0)*c - b*(-2));

		double dD2dc11 = 2.0*c - 8.0*a*0;
		double dD2dc13 = 2.0*c*0 - 8.0*a*1;
		double dD2dc33 = 2.0*c*1 - 8.0*a*0;
		double dD2dc55 = 2.0*c*(-2) - 8.0*a*1;

		double tmp = dv2dpa/(2.0*vpha2);
		double atander = 1.0/(1+tmp*tmp);

		tmp = dD1dc11*theSin2+dD2dc11*theSin2*theSin2;
		double dumn = dD1dc11*the2Sin+4.0*dD2dc11*theSin2*theSin*theCos;
		double r1c11 = 0 + 1*theSin2 + sign*(0.5/Dpa)*(2.0*b*0 + tmp);
		double r2c11 = 1*theSin*theCos + sign*(0.25/Dpa)*(dumn);
		double r3c11 = 0.25*(D1*the2Sin+4.0*D2*theSin2*theSin*theCos)*(-0.5/(Dpa*Dpa*Dpa))*(2.0*b*0+tmp);
		double RH_dpadc11 = (0.25*atander/(vpha4))*dv2dpa*r1c11 - (0.5*atander/(vpha2))*(r2c11+ sign*r3c11);

		tmp = dD1dc13*theSin2+dD2dc13*theSin2*theSin2;
		dumn = dD1dc13*the2Sin+4.0*dD2dc13*theSin2*theSin*theCos;
		double r1c13 = 0 + 0*theSin2 + sign*(0.5/Dpa)*(2.0*b*0 + tmp);
		double r2c13 = 0*theSin*theCos + sign*(0.25/Dpa)*(dumn);
		double r3c13 = 0.25*(D1*the2Sin+4.0*D2*theSin2*theSin*theCos)*(-0.5/(Dpa*Dpa*Dpa))*(2.0*b*0+tmp);
		double RH_dpadc13 = (0.25*atander/(vpha4))*dv2dpa*r1c13 - (0.5*atander/(vpha2))*(r2c13+ sign*r3c13);

		tmp = dD1dc33*theSin2+dD2dc33*theSin2*theSin2;
		dumn = dD1dc33*the2Sin+4.0*dD2dc33*theSin2*theSin*theCos;
		double r1c33 = 1 + (-1)*theSin2 + sign*(0.5/Dpa)*(2.0*b*1 + tmp);
		double r2c33 = (-1)*theSin*theCos + sign*(0.25/Dpa)*(dumn);
		double r3c33 = 0.25*(D1*the2Sin+4.0*D2*theSin2*theSin*theCos)*(-0.5/(Dpa*Dpa*Dpa))*(2.0*b*(1)+tmp);
		double RH_dpadc33 = (0.25*atander/(vpha4))*dv2dpa*r1c33 - (0.5*atander/(vpha2))*(r2c33+ sign*r3c33);

		tmp = dD1dc55*theSin2+dD2dc55*theSin2*theSin2;
		dumn = dD1dc55*the2Sin+4.0*dD2dc55*theSin2*theSin*theCos;
		double r1c55 = 1 + (0)*theSin2 + sign*(0.5/Dpa)*(2.0*b*(-1) + tmp);
		double r2c55 = (0)*theSin*theCos + sign*(0.25/Dpa)*(dumn);
		double r3c55 = 0.25*(D1*the2Sin+4.0*D2*theSin2*theSin*theCos)*(-0.5/(Dpa*Dpa*Dpa))*(2.0*b*(-1)+tmp);
		double RH_dpadc55 = (0.25*atander/(vpha4))*dv2dpa*r1c55 - (0.5*atander/(vpha2))*(r2c55+ sign*r3c55);

		tmp = 4*D2*theSin2*theSin*theCos;
		dumn = D1*the2Sin+tmp;
		double l2cij = e*the2Cos + sign*(0.25/Dpa)*(2.0*D1*the2Cos+4.0*D2*(3.0*theSin2*theCos2-theSin2*theSin2));
		double l3cij = 0.25*(dumn)*(-0.5/(Dpa*Dpa*Dpa))*(dumn);
		
		double l1c11 = e*the2Sin + sign*(0.5/Dpa)*(D1*the2Sin+dD2dc11*theSin2*theSin2+tmp);
		double LH_dpadc11 = 1.0-(0.25*atander/(vpha4))*dv2dpa*l1c11 + (0.5*atander/(vpha2))*(l2cij+ sign*l3cij);
		double l1c13 = e*the2Sin + sign*(0.5/Dpa)*(D1*the2Sin+dD2dc13*theSin2*theSin2+tmp);
		double LH_dpadc13 = 1.0-(0.25*atander/(vpha4))*dv2dpa*l1c13 + (0.5*atander/(vpha2))*(l2cij+ sign*l3cij);
		double l1c33 = e*the2Sin + sign*(0.5/Dpa)*(D1*the2Sin+dD2dc33*theSin2*theSin2+tmp);
		double LH_dpadc33 = 1.0-(0.25*atander/(vpha4))*dv2dpa*l1c33 + (0.5*atander/(vpha2))*(l2cij+ sign*l3cij);
		double l1c55 = e*the2Sin + sign*(0.5/Dpa)*(D1*the2Sin+dD2dc55*theSin2*theSin2+tmp);
		double LH_dpadc55 = 1.0-(0.25*atander/(vpha4))*dv2dpa*l1c55 + (0.5*atander/(vpha2))*(l2cij+ sign*l3cij);

//		double l1c11 = e*the2Sin + sign*(0.5/Dpa)*(D1*the2Sin+tmp);
//		double LH_dpadc11 = 1.0-(0.25*atander/(vpha4))*dv2dpa*l1c11 + (0.5*atander/(vpha2))*(l2cij+ sign*l3cij);
//		double l1c13 = l1c11;
//		double LH_dpadc13 = 1.0-(0.25*atander/(vpha4))*dv2dpa*l1c13 + (0.5*atander/(vpha2))*(l2cij+ sign*l3cij);
//		double l1c33 = l1c11;
//		double LH_dpadc33 = 1.0-(0.25*atander/(vpha4))*dv2dpa*l1c33 + (0.5*atander/(vpha2))*(l2cij+ sign*l3cij);
//		double l1c55 = l1c11;
//		double LH_dpadc55 = 1.0-(0.25*atander/(vpha4))*dv2dpa*l1c55 + (0.5*atander/(vpha2))*(l2cij+ sign*l3cij);
		
		//System.out.println("c11="+RH_dpadc11+" c13="+RH_dpadc13+" c33="+RH_dpadc33+" c55="+RH_dpadc55);
		
		double dpadc11 = RH_dpadc11/LH_dpadc11;
		double dpadc13 = RH_dpadc13/LH_dpadc13;
		double dpadc33 = RH_dpadc33/LH_dpadc33;
		double dpadc55 = RH_dpadc55/LH_dpadc55;
		
		tmp = 4*D2*theSin2*theSin*theCos;
		dumn = theSin2*theSin2;
		double dDpadc11 = (0.5/Dpa)*( 2.0*b*0 + dD1dc11*theSin2 + D1*the2Sin*dpadc11 + dD2dc11*dumn + tmp*dpadc11);
		double dDpadc13 = (0.5/Dpa)*( 2.0*b*0 + dD1dc13*theSin2 + D1*the2Sin*dpadc13 + dD2dc13*dumn + tmp*dpadc13);
		double dDpadc33 = (0.5/Dpa)*( 2.0*b*1 + dD1dc33*theSin2 + D1*the2Sin*dpadc33 + dD2dc33*dumn + tmp*dpadc33);
		double dDpadc55 = (0.5/Dpa)*( 2.0*b*(-1) + dD1dc55*theSin2 + D1*the2Sin*dpadc55 + dD2dc55*dumn + tmp*dpadc55);
		//System.out.println("c11="+dDpadc11+" c13="+dDpadc13+" c33="+dDpadc33+" c55="+dDpadc55);
		
		double dv2dc11 = 0.5*( 0 + 1*theSin2 + e*the2Sin*dpadc11 + sign*dDpadc11);
		double dv2dc13 = 0.5*( 0 + 0*theSin2 + e*the2Sin*dpadc13 + sign*dDpadc13);
		double dv2dc33 = 0.5*( 1 + (-1)*theSin2 + e*the2Sin*dpadc33 + sign*dDpadc33);
		double dv2dc55 = 0.5*( 1 + 0*theSin2 + e*the2Sin*dpadc55 + sign*dDpadc55);

		//System.out.println("dv2dc11="+dv2dc11+" dv2dc13="+dv2dc13+" dv2dc33="+dv2dc33+" dv2dc55="+dv2dc55);
		//System.exit(0);
		tmp = (D1*the2Sin+4*D2*theSin2*theSin*theCos)*(-1.0/(Dpa*Dpa));
		dumn = 4*D2*(3.0*theSin2*theCos2-theSin2*theSin2);

		double tc11 = dD1dc11*the2Sin + 2.0*D1*the2Cos*dpadc11 + 4.0*dD2dc11*theSin2*theSin*theCos + dumn*dpadc11;
		double ddv2dpadc11 = 1*theSin*theCos + e*the2Cos*dpadc11 + sign*0.25*( (1/Dpa)*(tc11) + tmp*dDpadc11 );
		double tc13 = dD1dc13*the2Sin + 2.0*D1*the2Cos*dpadc13 + 4.0*dD2dc13*theSin2*theSin*theCos + dumn*dpadc13;
		double ddv2dpadc13 = 0*theSin*theCos + e*the2Cos*dpadc13 + sign*0.25*( (1/Dpa)*(tc13) + tmp*dDpadc13 );
		double tc33 = dD1dc33*the2Sin + 2.0*D1*the2Cos*dpadc33 + 4.0*dD2dc33*theSin2*theSin*theCos + dumn*dpadc33;
		double ddv2dpadc33 = -1*theSin*theCos + e*the2Cos*dpadc33 + sign*0.25*( (1/Dpa)*(tc33) + tmp*dDpadc33 );
		double tc55 = dD1dc55*the2Sin + 2.0*D1*the2Cos*dpadc55 + 4.0*dD2dc55*theSin2*theSin*theCos + dumn*dpadc55;
		double ddv2dpadc55 = 0*theSin*theCos + e*the2Cos*dpadc55 + sign*0.25*( (1/Dpa)*(tc55) + tmp*dDpadc55 );

		double dvgdc11 = (0.5/vg)*( dv2dc11*(1 - (0.25/vpha4)*dv2dpa*dv2dpa) + (0.5/vpha2)*dv2dpa*ddv2dpadc11 );   
		double dvgdc13 = (0.5/vg)*( dv2dc13*(1 - (0.25/vpha4)*dv2dpa*dv2dpa) + (0.5/vpha2)*dv2dpa*ddv2dpadc13 ); 
		double dvgdc33 = (0.5/vg)*( dv2dc33*(1 - (0.25/vpha4)*dv2dpa*dv2dpa) + (0.5/vpha2)*dv2dpa*ddv2dpadc33 ); 
		double dvgdc55 = (0.5/vg)*( dv2dc55*(1 - (0.25/vpha4)*dv2dpa*dv2dpa) + (0.5/vpha2)*dv2dpa*ddv2dpadc55 ); 

		//System.out.println("c11="+dvgdc11+" c13="+dvgdc13+" c33="+dvgdc33+" c55="+dvgdc55);
		double dtdc11 = -(len/(vg*vg))*dvgdc11;
		double dtdc13 = -(len/(vg*vg))*dvgdc13;
		double dtdc33 = -(len/(vg*vg))*dvgdc33;
		double dtdc55 = -(len/(vg*vg))*dvgdc55;
		//System.out.println("c11="+dtdc11+" c13="+dtdc13+" c33="+dtdc33+" c55="+dtdc55);
		return new double[]{dtdc11, dtdc13, dtdc33, dtdc55, 0};
	}

	private double[] sensitivitySH(double vpha, double pa, double vg, double len) {
		double vpha2 = vpha*vpha;
		double vpha4 = vpha2*vpha2;
		double a = getC66()-getC55();

		double theSin = Math.sin(pa);
		double theSin2 = theSin*theSin;
		double theCos = Math.cos(pa);
		double theCos2 = theCos*theCos;
		double the2Sin = Math.sin(2.0*pa);
		double the2Cos = Math.cos(2.0*pa);        

		double dv2dpa = getC66()*the2Sin-getC55()*the2Sin;        
		double tmp = dv2dpa/(2.0*vpha2);
		double atander = 1.0/(1+tmp*tmp);
		//System.out.println("dv2dpa="+dv2dpa+" atander="+atander+" pa="+pa+" vpha="+vpha);
		
		double RH_dpadc55 = atander*( (0.5/(vpha4))*dv2dpa*theCos2 + theSin*theCos/vpha2 );
		double RH_dpadc66 = atander*( (0.5/(vpha4))*dv2dpa*theSin2 - theSin*theCos/vpha2 );
		double LH_dpadc55 = 1.0-(0.5*atander/(vpha4))*dv2dpa*a*the2Sin + (atander/(vpha2))*a*the2Cos;
		double LH_dpadc66 = 1.0-(0.5*atander/(vpha4))*dv2dpa*a*the2Sin + (atander/(vpha2))*a*the2Cos;
		
		double dpadc55 = RH_dpadc55/LH_dpadc55;
		double dpadc66 = RH_dpadc66/LH_dpadc66;
		//System.out.println("RH_dpadc55="+RH_dpadc55+" RH_dpadc66="+RH_dpadc66+" dpadc55="+dpadc55);
		//System.out.println("LH_dpadc55="+LH_dpadc55+" LH_dpadc66="+LH_dpadc66+" dpadc66="+dpadc66);
		//System.exit(0);

		tmp = 1 + (-1)*theSin2+a*2*theSin*theCos*dpadc55;
		double dumn = (-1)*the2Sin+a*the2Cos*2*dpadc55;
		double dvgdc55 = (0.5/vg)*( tmp*(1 - (0.25/vpha4)*dv2dpa*dv2dpa) + (0.5/vpha2)*dv2dpa*(dumn) );  
		tmp = 0 + (1)*theSin2+a*2*theSin*theCos*dpadc66;
		dumn = (1)*the2Sin+a*the2Cos*2*dpadc66;
		double dvgdc66 = (0.5/vg)*( tmp*(1 - (0.25/vpha4)*dv2dpa*dv2dpa) + (0.5/vpha2)*dv2dpa*(dumn) );

		double dtdc55 = -(len/(vg*vg))*dvgdc55;
		double dtdc66 = -(len/(vg*vg))*dvgdc66;
		//System.out.println("dtdc55="+dtdc55+" dtdc66="+dtdc66+" dpadc66="+dpadc66);
		//System.exit(0);
		
		return new double[]{0, 0, 0, dtdc55, dtdc66};
	}


}
