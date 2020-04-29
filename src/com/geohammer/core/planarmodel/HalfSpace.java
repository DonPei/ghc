package com.geohammer.core.planarmodel;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class HalfSpace {
	protected int  		id 		= 0;
	protected double 	vp 		= 0;
	protected double  	vs 		= 0;
	protected double  	vpToVs		= 0;
	protected double  	den 		= 2300.0; //kg/m^3
	protected double  	delta 		= 0;
	protected double  	epsilon 	= 0;
	protected double  	gamma 		= 0;
	
	protected double  	delta1 	= 0;
	protected double  	epsilon1 	= 0;
	protected double  	gamma1 	= 0;
	protected double  	delta3 	= 0;
	
	protected double  	qp 		= 1.0e10;
	protected double 	qs 		= 1.0e10;
	protected double 	theta 		= 0; //polarization angle 0-90 degree
	protected double 	phi 		= 0; //azimuth clockwise from North 0-360 degree
	
	protected double  	vpGradientX = 0; 	//(gradient of Vp)
	protected double  	vpGradientY = 0; 	//(gradient of Vp)
	protected double  	vpGradientZ = 0; 	//(gradient of Vp)
	protected double  	vsGradientX = 0; 	//(gradient of Vs)
	protected double  	vsGradientY = 0; 	//(gradient of Vs)
	protected double  	vsGradientZ = 0; 	//(gradient of Vs)
	
	/******************************************************************
	 *                 specify elastic constants                      *
	 ******************************************************************
	 *            [ c11    c12    c13     0     0    0 ]
	 *            [ c12    c22    c23     0     0    0 ] 
	 *      Cij = [ c13    c23    c33     0     0    0 ] 
	 *            [  0      0     0    c44      0    0 ] 
	 *            [  0      0     0     0    c55     0 ]
	 *            [  0      0     0     0     0   c66  ]
	 ********************************************************************/
	
	private double 	c11 		= 0;
	private double  c33 		= 0;
	private double 	c12 		= 0;
	private double  c13 		= 0;
	private double 	c44 		= 0;
	private double  c55 		= 0;
	private double 	c66 		= 0;
	
	/******************************************************************
	 *                 specify elastic constants                      *
	 ******************************************************************
	 *
	 *      c0 = density
	 *
	 *      Stiffness matrix (9 independent elastic constants):
	 *
	 *            [ c1    c2    c3     0     0    0 ]
	 *            [ c2    c4    c5     0     0    0 ] 
	 *      Cij = [ c3    c5    c6     0     0    0 ] 
	 *            [  0     0     0    c7     0    0 ] 
	 *            [  0     0     0     0    c8    0 ]
	 *            [  0     0     0     0     0   c9 ]
	 *
	 *   zmatbot = bottom depth of each layer
	 *
	 *  recall:  P-wave velocity = sqrt( ( lambda + 2 mu ) / dens ) 
	 *           S-wave velocity = sqrt( mu / dens )                
	 *           Poisson's ratio = 0.5 * lambda / ( lambda + mu ) 
	 ********************************************************************/
	private double 	c1 		= 0;
	private double 	c2 		= 0;
	private double 	c3 		= 0;
	private double 	c4 		= 0;
	private double 	c5 		= 0;
	private double 	c6 		= 0;
	private double 	c7 		= 0;
	private double 	c8 		= 0;
	private double 	c9 		= 0;
	
	private double []	aux 	= null;
	private double INVALID_D 	= -99999.0;
	
	public HalfSpace() { this(1);}
	public HalfSpace(int id) {this(id, 2500.0, 1600.0);}
	
	public HalfSpace(int id, double vp, double vs) {
		this(id, vp, vs, 2,
				0, 0, 0, 1.0e10, 1.0e10, 0, 0);
	}
	public HalfSpace(int id, double vp, double vs, double den, 
			double delta, double epsilon, double gamma, double qp, double qs, double theta, double phi) {
		this(id, vp, vs, den,  0, 0, 0, 0, 0, 0, delta, epsilon, gamma, qp, qs, theta, phi);
	}
	public HalfSpace(int id, double vp, double vs, double den, 
			double vpGradientX, double vpGradientY, double vpGradientZ, double vsGradientX, double vsGradientY, double vsGradientZ, 
			double delta, double epsilon, double gamma, double qp, double qs, double theta, double phi) {
		this(id, vp, vs, den, 
				vpGradientX, vpGradientY, vpGradientZ, vsGradientX, vsGradientY, vsGradientZ, 
				delta, epsilon, gamma, qp, qs, theta, phi,
				0,0,0,0,
				0,0,0,0,0,0,0);
	}
	public HalfSpace(int id, double vp, double vs, double den, 
			double vpGradientX, double vpGradientY, double vpGradientZ, double vsGradientX, double vsGradientY, double vsGradientZ, 
			double delta, double epsilon, double gamma, double qp, double qs, double theta, double phi, 
			double delta1, double epsilon1, double gamma1, double delta3) {
		this(id, vp, vs, den, 
				vpGradientX, vpGradientY, vpGradientZ, vsGradientX, vsGradientY, vsGradientZ,
				delta, epsilon, gamma, qp, qs, theta, phi,  
				delta1, epsilon1, gamma1, delta3, 
				0,0,0,0,0,0,0);
	}
	public HalfSpace(int id, double vp, double vs, double den, 
			double vpGradientX, double vpGradientY, double vpGradientZ, double vsGradientX, double vsGradientY, double vsGradientZ, 
			double delta, double epsilon, double gamma, double qp, double qs, double theta, double phi, 
			double delta1, double epsilon1, double gamma1, double delta3, 
			double c11, double c12, double c13, double c33, double c44, double c55, double c66) {
		setValues(id, vp, vs, den, 
				vpGradientX, vpGradientY, vpGradientZ, vsGradientX, vsGradientY, vsGradientZ,
				delta, epsilon, gamma, qp, qs, theta, phi,  
				delta1, epsilon1, gamma1, delta3, 
				c11, c12, c13, c33, c44, c55, c66);
	}
	
	public void setValues(int id, double vp, double vs, double den, 
			double vpGradientX, double vpGradientY, double vpGradientZ, double vsGradientX, double vsGradientY, double vsGradientZ,
			double delta, double epsilon, double gamma, double qp, double qs, double theta, double phi, 
			double delta1, double epsilon1, double gamma1, double delta3,
			double c11, double c12, double c13, double c33, double c44, double c55, double c66) {

		this.id 			= id;
		this.vp 			= vp;
		this.vs 			= vs;
		this.den 			= den;

		this.vpGradientX 	= vpGradientX;
		this.vpGradientY 	= vpGradientY;
		this.vpGradientZ 	= vpGradientZ;
		this.vsGradientX 	= vsGradientX;
		this.vsGradientY 	= vsGradientY;
		this.vsGradientZ 	= vsGradientZ;
		
		this.delta 			= delta;
		this.epsilon 		= epsilon;
		this.gamma 			= gamma;
		this.qp 			= qp;
		this.qs 			= qs;
		this.theta 			= theta;
		this.phi 			= phi;

		this.delta1 		= delta1;
		this.epsilon1 		= epsilon1;
		this.gamma1			= gamma1;
		this.delta3 		= delta3;
		
		this.c11 			= c11;
		this.c12 			= c12;
		this.c13 			= c13;
		this.c33 			= c33;
		this.c44 			= c44;
		this.c55 			= c55;
		this.c66 			= c66;
	}
	public HalfSpace copy() { 
		return new HalfSpace(getId(), getVp(), getVs(), getDen(), 
				vpGradientX, vpGradientY, vpGradientZ, vsGradientX, vsGradientY, vsGradientZ, 
			getDelta(), getEpsilon(), getGamma(), getQp(), getQs(), getTheta(), getPhi(), 
			delta1, epsilon1, gamma1, delta3, 
			c11, c12, c13, c33, c44, c55, c66); 
	}
	
	public void allocateAux(int n) 	{ aux = new double[n]; }
	public double [] getAux()  		{ return aux; }
	
	public boolean isIso() {
		if(delta==0.0&&epsilon==0.0&&gamma==0.0) return true;
		else return false;
	}
	
	public void trimAnisotropicParameters() {
		if(Math.abs(delta)<=0.00001) delta = 0.0;
		if(Math.abs(epsilon)<=0.00001) epsilon = 0.0;
		if(Math.abs(gamma)<=0.00001) gamma = 0.0;
	}
	
	public void convertUnitSystem(double velScalor, double denScalor) {
		vp 			*= velScalor;
		vs 			*= velScalor;
		den 			*= denScalor;

		vpGradientX 	*= velScalor;
		vpGradientY 	*= velScalor;
		vpGradientZ 	*= velScalor;
		vsGradientX 	*= velScalor;
		vsGradientY 	*= velScalor;
		vsGradientZ 	*= velScalor;
	}
	
	public void setValues(double v) {
		setValues((int)v, v, v, v, 
				v, v, v, v, v, v,
				v, v, v, v, v, v, v, 
				v, v, v, v,
				v, v, v, v, v, v, v);
	}
	public void toCij() {
		toCij(den, 1.0);
	}
//	public void toCij(double den) {
//		toCij(den, 0.001);
//	}
	
	// SI: Density (kg/m^3), Vp and Vs (m/sec), Cij(MPa) 
	// English: Density (lb/ft^3), Vp and Vs (ft/sec), Cij(MPsi)
	// Const_English = 2.1584e-4 / 1.e6; % so Cij could be in MPsi
	// Const_MPsiToMPa = 6893;
	// Const_Den_SI_To_English = 6.242822e-2; % kg/m^3 to lb/ft^3
	// Cij = Cij * 1.e6 * 6893;   % to convert psi to pa
	// CijVect = CijVect * 1.e6 * 6893;    % to convert psi to pa
	// ModuliVect = ModuliVect * 1.e6 * 6893;    % to convert psi to pa
	public void toCij(double den, double scalor) {
		double pa = scalor*vp;
		double sa = scalor*vs;
		c33 = pa*pa*den;
		c55 = sa*sa*den;
		c44 = c55;
		
		c11 = c33*(1.0+2.0*epsilon);
		c66 = c55*(1.0+2.0*gamma);
		
		double a = c33-c55;
		c13 = Math.sqrt(delta*2.0*c33*a+a*a)-c55;
		c12 = c11-2.0*c66;
		
//		 		 *            [ c1    c2    c3     0     0    0 ]
//				 *            [ c2    c4    c5     0     0    0 ] 
//				 *      Cij = [ c3    c5    c6     0     0    0 ] 
//				 *            [  0     0     0    c7     0    0 ] 
//				 *            [  0     0     0     0    c8    0 ]
//				 *            [  0     0     0     0     0   c9 ]
		
//				 *            [ c11    c12    c13     0     0    0 ]
//				 *            [ c12    c22    c23     0     0    0 ] 
//				 *      Cij = [ c13    c23    c33     0     0    0 ] 
//				 *            [  0      0     0    c44      0    0 ] 
//				 *            [  0      0     0     0    c55     0 ]
//				 *            [  0      0     0     0     0   c66  ]
				
		c1 = c11; c2 = c12; c3 = c13; 
		c4 = c11; c5 = c13; c6 = c33; 
		c7 = c44; c8 = c55; c9 = c66; 
		
	}
	
//	public void cijToVelocity(boolean vp, boolean vs, double den) {
//		cijToVelocity(vp, vs, den, 1.0);
//	}
	//1GPa = 1e9Pa 1Pa = 1kg/m.s2
	public void cijToVelocity(boolean isVp, boolean isVs, double isDen, double isScalor) {
		if(isVp) vp = isScalor*Math.sqrt(c33/den);
		if(isVs) vs = isScalor*Math.sqrt(c55/den);
	}
	public void cijToAnisotropy(boolean isDelta, boolean isEpsilon, boolean isGamma) {
		if(isEpsilon) epsilon = (c11-c33)/(2.0*c33);
		if(isGamma) gamma = (c66-c55)/(2.0*c55);
		if(isDelta) delta = ((c13+c55)*(c13+c55)-(c33-c55)*(c33-c55))/(2.0*c33*(c33-c55));
	}
	
	//calculate Young's module and Poisson's ratio
	public double calE11() 						{ return (c11-c12)*(c11*c33+c12*c33-2.0*c13*c13)/(c11*c33-c13*c13); }
	public double calE33() 						{ return c33-2.0*c13*c13/(c11+c12); }
	public double calV31() 						{ return c13/(c11+c12); }
	public double calV12() 						{ return (c12*c33-c13*c13)/(c11*c33-c13*c13); }
	public double calV13() 						{ return c13*(c11-c12)/(c11*c33-c13*c13); }
	
	public void ANNIE() {
		c13 = c33-2.0*c55;
		c12 = c13;
		c11 = c33+2.0*(c66-c55);
		
		cijToAnisotropy(false, true, true);
		delta = 0;
	}
	public void calUsingANNIE(double ft2m, double denFactor, double velFl, double denFl) {
		double den = this.den*denFactor;
		double ft2m2 = ft2m*ft2m;
		
		c33 = vp*vp*den*ft2m2;
		c44 = vs*vs*den*ft2m2;
		c55 = vsGradientX*vsGradientX*den*ft2m2;

//		System.out.println("vp="+_vp+" vs="+_vs+" vsGradientX="+_vsGradientX+" vsGradientY="+_vsGradientY+
//				" velFl="+velFl+" denFl="+denFl+" den="+_den);
		//borehole fluid compressional velocity in ft/s
		//double vFl = 8000.0; 
		//borehole fluid density in kg/m3
		//double denFl = 1200.0;
		////borehole fluid bulk modulus
		//double kFl = 1.0e-6*velFl*velFl*denFl*denFactor*ft2m2;
		double kFl = velFl*velFl*denFl*denFactor*ft2m2;
		
		double a =  (velFl*velFl)/(vsGradientY*vsGradientY) - 1.0;
		//_c66 = 0.145*kFl/a;	
		if(a!=0) c66 = kFl/a;
		else c66 = kFl/0.01;
		
		a = 0.5*(c44+c55);
		c44 = a;
		c55 = a;		

//		_gamma = (_c66-_c55)/(2.0*_c55);
//		System.out.println("vp="+_vp+" vs="+_vs+" vsGradientX="+_vsGradientX+" vsGradientY="+_vsGradientY+
//				" velFl="+velFl+" denFl="+denFl+" den="+_den+" kFl="+kFl+" c55="+_c55+" c66="+_c66+" a="+ a+
//				" gamma="+_gamma);
		//annie assumption
		ANNIE();
	}
	public void calUsingHorizonAndVerticalVelocity(double ft2m, double denFactor) {
		double den = this.den*denFactor;
		double ft2m2 = ft2m*ft2m;
		
		c11 = vpGradientX*vpGradientX*den*ft2m2;
		c33 = vp*vp*den*ft2m2;
		c55 = vs*vs*den*ft2m2;
		c66 = vsGradientX*vsGradientX*den*ft2m2;

		c44 = c55;
		
		c12 = c11-2.0*c66;
		c13 = c33-2.0*c55;
		
		cijToAnisotropy(false, true, true);
		delta = 0;
	}
	public void calUsingVelocityRegression(double ft2m, double denFactor) {
		double den = this.den*denFactor;
		double ft2m2 = ft2m*ft2m;
		
		c33 = vp*vp*den*ft2m2;
		c44 = vs*vs*den*ft2m2;
		c55 = vsGradientX*vsGradientX*den*ft2m2;

		double a = 0.5*(c44+c55);
		c44 = a;
		c55 = c44;
		
		a = 0.5*(vs+vsGradientX)*ft2m*0.001;  //km/s
		//a = 1.0*(_vs)*ft2m*0.001;  //km/s
		double vs90 = 1000*(0.8467*a+0.81611); // m/s
		c66 = vs90*vs90*den;
		
		//annie assumption
		ANNIE();
	}
	
	public double calLayerProperty(int id) {
		if(id==1) 		return calE11();
		if(id==2) 		return calE33();
		if(id==3) 		return calV12();
		if(id==4) 		return calV13();
		if(id==5) 		return calV31();
		
		else 			return calE11();
	}
	
	public String validate() {
		if(getVp()==0) {
			return "Vp is ZERO";
		} else if(getVs()==0) {
			return "Vs is ZERO";
		} else return null;
	}
	
	public double getLayerProperty(int id) {
		if(id==1) 		return getVp();
		else if(id==2) 	return getVs();
		else if(id==3) 	return getVpToVs();
		else if(id==4) 	return getDen();
		else if(id==5) 	return getVpGradientX();
		else if(id==6) 	return getVpGradientY();
		else if(id==7) 	return getVpGradientZ();
		else if(id==8) 	return getVsGradientX();
		else if(id==9) 	return getVsGradientY();
		else if(id==10) return getVsGradientZ();

		else if(id==11) return getC11();
		else if(id==12) return getC12();
		else if(id==13) return getC13();
		else if(id==14) return getC33();
		else if(id==15) return getC44();
		else if(id==16) return getC55();
		else if(id==17) return getC66();
		
		else if(id==20) return getEpsilon();
		else if(id==21) return getDelta();
		else if(id==22) return getGamma();

		else if(id==23) return getQp();
		else if(id==24) return getQs();
		else if(id==25) return getTheta();
		else if(id==26) return getPhi();
		else  			return getVp();
	}
	public void setLayerProperty(int id, double v) {
		if(id==1) 		setVp(v);
		else if(id==2) 	setVs(v);
		else if(id==3) 	setVpToVs(v);
		else if(id==4) 	setDen(v);
		else if(id==5) 	setVpGradientX(v);
		else if(id==6) 	setVpGradientY(v);
		else if(id==7) 	setVpGradientZ(v);
		else if(id==8) 	setVsGradientX(v);
		else if(id==9) 	setVsGradientY(v);
		else if(id==10) setVsGradientZ(v);

		else if(id==11) setC11(v);
		else if(id==12) setC12(v);
		else if(id==13) setC13(v);
		else if(id==14) setC33(v);
		else if(id==15) setC44(v);
		else if(id==16) setC55(v);
		else if(id==17) setC66(v);
		
		else if(id==20) setEpsilon(v);
		else if(id==21) setDelta(v);
		else if(id==22) setGamma(v);

		else if(id==23) setQp(v);
		else if(id==24) setQs(v);
		else if(id==25) setTheta(v);
		else if(id==26) setPhi(v);
		else  			setVp(v);
	}

	public void scale(int id, int [] tabIndex, double scalor) {
		if(id==0) { 
			for(int i=0; i<tabIndex.length; i++) {setLayerProperty(tabIndex[i], scalor*getLayerProperty(tabIndex[i]));} 
		}
		else setLayerProperty(id, scalor*getLayerProperty(id));
	}
	public void set(int id, double v) {
		if(id==0) for(int i=1; i<30; i++) setLayerProperty(id, v);
		else setLayerProperty(id, v);
	}
	
	public String cijToString() {
		return c11+" "+c12+" "+c13+" "+c33+" "+c44+" "+c55+" "+c66;
	}
	public String toString() {
		return toHalfSpaceString();
	}
	public String toHalfSpaceString() {
		return id+ vp+ " "+vs+ " "+den+ " "+
				vpGradientX+ " "+ vpGradientY+ " "+ vpGradientZ+ " "+ 
				vsGradientX+ " "+ vsGradientY+ " "+ vsGradientZ + " "+ 
				delta+ " "+epsilon+ " "+gamma+ " "+qp+ " "+qs+" "+theta+ " "+phi + " "+ 
				delta1+ " "+ epsilon1+ " "+ gamma1+ " "+ delta3 + " "+ 
				c11+" "+c12+" "+c13+" "+c33+" "+c44+" "+c55+" "+c66;
	}

}
