package com.geohammer.rt.pseudo3d;

import java.util.Arrays;

import javax.swing.JOptionPane;

import com.geohammer.core.planarmodel.FlatLayer1D;
import com.geohammer.core.planarmodel.LayersI;

public class SeismicVel {
	private LayersI _flayer1D 		= null;
	private int 		_iVTI		= 0;	// 0- non-anisotropic, 1- VTI; 2- HTI; 
	private int 		_iVp		= 1;	// 1- P-wave, 2- S-wave parallel SH; 3- S-wave vertical SV;
	private int 		_iApprox 	= 0; 	//0= use exact formula for anisotropic ray tracing (need S velocity), 
	private double 		_halfPI 	= 2.0*Math.atan(1.0);
	private int 		_nAngles 	= 450;
	private int 		_iMethod 	= 0;

	private double  	[] _pAngles 	= null; // predefined phase angle table [_nAngles] 0<= table <=90
	private double  	[][] _gAngles 	= null; // predefined P-wave group angle table, corresponding to the phase angle
	private double  	[][] _gVel 		= null; // predefined P-wave group vel table, corresponding to the phase angle
	private double  	[][] _rayPars 	= null; // predefined SH-wave group vel table, corresponding to the phase angle

	public SeismicVel(int iMethod,  LayersI flayer1D, int iVTI, int iVp, int iApprox) {
		_iMethod 	= iMethod;
		_flayer1D 	= flayer1D;
		_iVp 		= iVp;
		_iVTI 		= iVTI;
		_iApprox 	= iApprox;
		//System.out.println("iVP="+iVp+" iVTI="+iVTI);
		if(iMethod==1) _nAngles = 1; 

		allocate(_nAngles);

//		if(_iApprox==0 && !svelExist() && iVTI>0) {
//			JOptionPane.showMessageDialog( null, "S-wave velocity is not provided!", "Error",
//					JOptionPane.ERROR_MESSAGE );
//			return;
//		}

		if(iMethod==1) {
			//System.out.println(tableToString( ));
		} else {
			definePhaseAngles(_nAngles);
			preDefineThetaTables(iVTI, iVp);
			preDefineGroupVelTables(iVTI, iVp);
			preDefineRayParTables(iVTI, iVp);
		}
	}
	private void allocate(int nCols)	{
		int nBnry = getNumOfBoundaries();
		allocate(nBnry, nCols);
	}
	private void definePhaseAngles(int nCols)	{
		double min = 0.0;
		double max = _halfPI;
		double d = (max-min)/(nCols-1);
		double a = min;
		for(int i=0; i<nCols; i++) { _pAngles[i] = (a+i*d);} 
	}
	private void allocate(int nBnry, int nCols)	{
		_pAngles 	= new double[nCols];
		_gAngles 	= allocateInit(nBnry, nCols);
		_gVel 		= allocateInit(nBnry, nCols);
		_rayPars	= allocateInit(nBnry, nCols);
	}
	private double [][] allocateInit(int nBnry, int nCols)	{
		double [][] v 	= new double[nBnry][nCols];
		for(int j=0; j<nBnry; j++) {
			for(int i=0; i<nCols; i++) { v[j][i] = 0.0f; }
		}
		return v;
	}

	public double [] getTable(int iType, int iVTI, int iVp, int iBnry, double[] pAngles)	{
		double [] pVel = getPhaseVelTable(iVTI, iVp, iBnry, pAngles);
		if(iType==5) return pVel;
		double[] p = new double [pAngles.length];
		for(int i=0; i<pVel.length; i++) { p[i] = Math.sin(pAngles[i])/pVel[i]; }
		if(iType==3) return p;
		
		double[][] table = new double [5][pAngles.length];
		int [] index = new int[]{iBnry};
		double [] pgs = new double[5];
		for(int i=0; i<pAngles.length; i++) {
			setRayPara(p[i], iVp, 1, getVp(iBnry), getVs(iBnry), 
					getDelta(iBnry), getEpsilon(iBnry), getGamma(iBnry), pgs);
			
			table[0][i] = pgs[0];
			table[1][i] = pgs[1];
			table[2][i] = pgs[2];
			table[3][i] = pgs[3];
			table[4][i] = pgs[4];
		}
		
		if(iType==1) return table[1];
		else if(iType==2) return table[2];
		else return table[4];
	}
	
	public void setRayPara(double p, int iVTI, int iVp, double hPrime, int iBnry, double[] pgs)	{
			setRayPara(p, iVp, hPrime, getVp(iBnry), getVs(iBnry), 
					getDelta(iBnry), getEpsilon(iBnry), getGamma(iBnry), pgs);
	}
	
	public void setRayPara(double p, int iVTI, int [] pIndex, double [] hPrime, int [] index, double[][] pgs)	{
		for(int i=0; i<index.length; i++) {
			int iBnry = index[i];
			setRayPara(p, pIndex[i], hPrime[i], getVp(iBnry), getVs(iBnry), 
					getDelta(iBnry), getEpsilon(iBnry), getGamma(iBnry), pgs[i]);
		}
	}
	public void setRayPara(double p, int iVTI, int iVp, double [] hPrime, int [] index, double[][] pgs)	{
		int [] pIndex = new int[index.length];
		for(int i=0; i<index.length; i++)  pIndex[i] = iVp;
		setRayPara(p, iVTI, pIndex, hPrime, index, pgs);
	}
	
	public void setRayPara(double p, int iVp, double th, double vp, double vs, double delta, double epsilon, double gamma, double [] pg)	{
		for(int i=0; i<pg.length; i++) pg[i] = 0.0;

		double sign = 1.0;
		if(iVp==3) sign = -1.0;
		double ap 	= vp*p;
		double ap2 	= ap*ap;
		double ap4 	= ap2*ap2;		
		
		double bp 	= vs*p;
		double bp2 	= bp*bp;
		
		double a	= vs/vp;
		double	F 	= 1.0 - a*a;
		
		//phase angle in each layer by Snell's law.  for definition and method see Tang and Li, 2008
		double theSin2 		= 0.0;
		if(iVp==2) {
			theSin2 		= bp2/(1.0-2.0*gamma*bp2);
		} else {
			double c0 		= (1.0-F)*ap4;
			double c1 		= (2.0*(epsilon-F*delta)*ap2-2.0+F)*ap2;
			double c2 		= 1.0-2.0*epsilon*ap2-2.0*F*(epsilon-delta)*ap4;

			double d 		= Math.sqrt(c1*c1-4.0*c0*c2);
			theSin2 		= (-c1+sign*d)/(2.0*c2);
		}
		double theSin 		= Math.sqrt(theSin2);
		double theCos 		= Math.sqrt(1.0-theSin2);
		double pAngle 		= Math.asin(theSin);

		//phase velocity, group angle, group velocity, and derivative
		double pVel2 		= 0.0;
		double dv2dpa 		= 0.0;
		if(iVp==2) {
			pVel2 			= vs*vs*(1.0+2.0*gamma*theSin2);
			dv2dpa 			= 4.0*vs*vs*gamma*theSin*theCos;
		} else {
			double the2Sin 	= Math.sin(2.0*pAngle);
			double the4Sin 	= Math.sin(4.0*pAngle);
			a 				= 1.0 + 2.0*epsilon*theSin2/F;
			double 	D 		= Math.sqrt(a*a - 2.0*(epsilon-delta)*the2Sin*the2Sin/F);
			pVel2 			= vp*vp*(1.0+epsilon*theSin2-0.5*F+sign*0.5*F*D);
			double E 		= epsilon*(F+2.0*epsilon*theSin2)*the2Sin-F*(epsilon-delta)*the4Sin;
			double G 		= E/(F*D);
			dv2dpa 			= vp*vp*(epsilon*the2Sin+sign*G);
		}
		double pVel 	= Math.sqrt(pVel2);
		double theTan 	= 0.5*dv2dpa/pVel2;
		double da 		= Math.atan(theTan);
		double gAngle 	= pAngle+da;
		double gVel 	= Math.sqrt(pVel2*(1.0+theTan*theTan));
		//double t 		= th/(gVel*Math.cos(gAngle));
		//double x 		= th*Math.tan(gAngle);

		pg[0] = pAngle;
		pg[1] = gAngle;
		pg[2] = gVel;
		pg[3] = pVel;
		pg[4] = dv2dpa;
	}

	public int  getNAngles() 			{ return _nAngles;}
	public int getIApproximation() 		{ return _iApprox;}
	public int  getIVp() 				{ return _iVp;}
	public int  getIVTI() 				{ return _iVTI;}
	public int getNumOfBoundaries() 	{ return _flayer1D.getNumOfBoundaries();}
	public double getVp(int iBnry) 		{ return _flayer1D.getLayer(iBnry).getVp();}
	public double getVs(int iBnry) 		{ return _flayer1D.getLayer(iBnry).getVs();}
	public double getDelta(int iBnry) 	{ return _flayer1D.getLayer(iBnry).getDelta();	}
	public double getEpsilon(int iBnry) { return _flayer1D.getLayer(iBnry).getEpsilon();}
	public double getGamma(int iBnry) 	{ return _flayer1D.getLayer(iBnry).getGamma();}
	public double getTheta(int iBnry) 	{ return _flayer1D.getLayer(iBnry).getTheta(); }

	public boolean svelExist() 			{ if(getVs(0)<=0.0) return false; else return true; 	}
	public void setIApproximation( int v) { _iApprox = v; }

	public double [] getPhaseVelTable(int iBnry) { return getPhaseVelTable(_iVTI, _iVp, iBnry); }
	public double [] getPhaseVelTable(int iVTI, int iVp, int iBnry) { 
		return getPhaseVelTable(iVTI, iVp, iBnry, _pAngles);
	}
	public double [] getPhaseVelTable(int iVTI, int iVp, int iBnry, double [] pAngles) { 
		double [] pVel = new double[pAngles.length];
		for(int i=0; i<pVel.length; i++) pVel[i] = getLayerPhaseVel(iVTI, iVp, iBnry, pAngles[i]);
		return pVel;
	}
	
	public void preDefineGroupVelTables(int iVTI, int iVp)	{
		int nCols = _pAngles.length;
		int nBnry = _gVel.length;
		for(int j=0; j<nBnry; j++) {
			for(int i=0; i<nCols; i++) { _gVel[j][i] = getLayerGroupVel(iVTI, iVp, j, _pAngles[i]);}
		}
	}
	public void preDefineThetaTables(int iVTI, int iVp)	{
		int nCols = _pAngles.length;
		int nBnry = _gAngles.length;
		for(int j=0; j<nBnry; j++) {
			for(int i=0; i<nCols; i++) { _gAngles[j][i] = getLayerGroupAngle(iVTI, iVp, j, _pAngles[i]);}
		}
	}
	public void preDefineRayParTables(int iVTI, int iVp)	{
		int nCols = _pAngles.length;
		int nBnry = _rayPars.length;
		for(int j=0; j<nBnry; j++) {
			if(iVTI==3) {
				for(int i=0; i<nCols; i++) {_rayPars[j][i] = Math.cos(_pAngles[i])/getLayerPhaseVel(iVTI, iVp, j, _pAngles[i]);}
			} else  {
				for(int i=0; i<nCols; i++) {_rayPars[j][i] = Math.sin(_pAngles[i])/getLayerPhaseVel(iVTI, iVp, j, _pAngles[i]);}
			}
		}
	}

	public double [][] getTable(int iTable) {
		if(iTable==0)		{ return new double[][]{_pAngles}; }
		else if(iTable==1)	{ return _gAngles; } 
		else if(iTable==2)	{ return _gVel; } 
		else 				{ return _rayPars; }
	}
	public int getTableIndex(int iTable, int iBnry, double v) {
		if(iTable==0) iBnry = 0;
		double [] data = getTable(iTable)[iBnry];
		for(int i=0; i<data.length; i++) {
			if(v==data[i]) return i;
		}
		for(int i=0; i<data.length-1; i++) {
			if(isBetween(v, data[i], data[i+1])) return i;
		}
		if(v<data[0]) return 0;
		else if(v>data[data.length-1]) return data.length-1;
		return 0;
	}
	public double getTableValue(int iTable, int iBnry, int index) {
		double [][] data = getTable(iTable);
		return data[iBnry][index];
	}

	public double getTableValue(int to, int iBnry, int from, double x) {
		int iBnry1 = iBnry;
		int iBnry2 = iBnry;
		if(from==0) iBnry1 = 0;
		if(to==0) iBnry2 = 0;

		double [] xArray = getTable(from)[iBnry1];
		double [] yArray = getTable(to)[iBnry2];

		int index = getTableIndex(from, iBnry1, x);
		if(index==0) return yArray[0];
		else if(index>=xArray.length-1) return yArray[xArray.length-1]; 
		else { }

		double x1 = xArray[index];
		double x2 = xArray[index+1];
		double y1 = yArray[index];
		double y2 = yArray[index+1];

		return interpolation(x, x1, x2, y1, y2);
	}

	public double getPhaseAngleFromGroupAngleTable(int iBnry, double gAngle) {
		double [][] gAngleTable = getTable(1);
		double [] gAngles = gAngleTable[iBnry];

		int index = 0;
		if(Almost.DOUBLE.cmp(gAngle,gAngles[0])==0) {return _pAngles[0];} 
		for(int i=1; i<gAngles.length; i++) { 
			if(Almost.DOUBLE.cmp(gAngle,gAngles[i])==0) {return _pAngles[i];} 
			else {
				if(Almost.DOUBLE.cmp(gAngle,gAngles[i-1])==1 && Almost.DOUBLE.cmp(gAngle,gAngles[i])==-1) {index = i; break;}
				if(Almost.DOUBLE.cmp(gAngle,gAngles[i-1])==-1 && Almost.DOUBLE.cmp(gAngle,gAngles[i])==1) {index = i; break;}
			}
		}
		if(index==0) {
			if(Almost.DOUBLE.cmp(gAngle,gAngles[0])==-1) {return _pAngles[0];} 
			if(Almost.DOUBLE.cmp(gAngle,gAngles[gAngles.length-1])==1) {return _pAngles[_pAngles.length-1];} 
		}
		double x1 = gAngles[index-1];
		double x2 = gAngles[index];
		double y1 = _pAngles[index-1];
		double y2 = _pAngles[index];

		return interpolation(gAngle, x1, x2, y1, y2);
	}
	
	private boolean isBetween(double x, double x1, double x2){
		if(x1==x2)  	return x==x1;
		else if(x1<x2) 	return x>x1&&x<x2;
		else 			return x<x1&&x>x2;
	}
	private double interpolation(double x, double x1, double x2, double y1, double y2){
		return y1+(x-x1)*(y2-y1)/(x2-x1);
	}

	public double getLayerGroupAngle(int iVTI, int iVp, int iBnry, double theta) {
		if(iVTI==0) {
			return theta;
		} else {
			double theTan 	= getLayerGroupTan(iVTI, iVp, iBnry, theta);
			//System.out.println(_pAngles[i]*180.0/3.1415926 + " " + Math.atan(theTan)*180.0/3.1415926);
			double theTan2	= theTan*theTan;
			double a		= Math.sqrt(theTan2/(1.0+theTan2));
			if(Double.isNaN(a)) {
				a 	= 1.0f;
			} else if(a<0.0) {
				a 	= 0.0f;
			} else if(a>1.0) {
				a 	= 1.0f;
			} else {}
			return Math.asin(a);
		}
	}	
	public double getLayerGroupVel(int iVTI, int iVp, int iBnry, double theta) {
		double vel = 1.0;
		if(iVTI==0) {
			if( iVp==1 ) {
				return getVp(iBnry);
			} else if( iVp==2 ) { //vsParallel
				return getVs(iBnry);
			}  else if( iVp==3 ) {//vsVertical
				return getVs(iBnry);
			} 
		} else {
			if(_iApprox==0 ) {
				vel = getExactLayerGroupVel(iVTI, iVp, iBnry, theta);
			} else if(_iApprox==1 ) {
				vel = getThomsenLayerGroupVel(iVTI, iVp, iBnry, theta);
			} else if(_iApprox==2 ) {

			} else { }
		}
		return vel;
	}	
	public double getLayerPhaseVel(int iVTI, int iVp, int iBnry, double theta) {
		double vel = 1.0;
		if(iVTI==0) {
			if( iVp==1 ) {
				return getVp(iBnry);
			} else if( iVp==2 ) { //vsParallel
				return getVs(iBnry);
			}  else if( iVp==3 ) {//vsVertical
				return getVs(iBnry);
			} 
		} else {
			if( iVp==1 ) {
				if(getEpsilon(iBnry)==0.0 && getDelta(iBnry)==0.0) return getVp(iBnry);
			} else if( iVp==2 ) {//vsParallel
				if(getGamma(iBnry)==0.0) return getVs(iBnry);
			} else if( iVp==3 ) { //vsVertical
				if(getEpsilon(iBnry)==0.0 && getDelta(iBnry)==0.0) return getVs(iBnry);
			}  else { }

			if(_iApprox==0 ) {
				vel = getExactLayerPhaseVel(iVTI, iVp, iBnry, theta);
			} else if(_iApprox==1 ) {
				vel = getThomsenLayerPhaseVel(iVTI, iVp, iBnry, theta);
			} else if(_iApprox==2 ) {

			} else { }
		}

		return vel;
	}
	public double getPhaseVelDeriv(int iVTI, int iVp, int iBnry, double theta) {
		double vel = 1.0;
		if(iVTI==0) {
			return 0.0;
		} else {
			if(_iApprox==0 ) {
				vel = getExactPhaseVelDeriv(iVTI, iVp, iBnry, theta);
			} else if(_iApprox==1 ) {
				vel = getThomsenPhaseVelDeriv(iVTI, iVp, iBnry, theta);
			} else if(_iApprox==2 ) {

			} else { }
		}

		return vel;
	}
	public double getLayerGroupTan(int iVTI, int iVp, int iBnry, double theta) {
		double theGroupTan = 0.0;
		if(iVTI==0) {
			return Math.tan(theta);
		} else {
			if(_iApprox==0 ) {
				theGroupTan = getExactGroupTan(iVTI, iVp, iBnry, theta);
			} else if(_iApprox==1 ) {
				theGroupTan = getThomsenGroupTan(iVTI, iVp, iBnry, theta);
			} else if(_iApprox==2 ) {
			} else { }
		}
		return theGroupTan;
	}




	//private members
	private double getExactLayerPhaseVel(int iVTI, int iVp, int iBnry, double theta) {
		double a;
		double theSin 		= Math.sin(theta);
		double theSin2 		= theSin*theSin;
		double theCos 		= Math.cos(theta);
		double theSin2Theta = 2.0*theSin*theCos;
		a	= getVs(iBnry)/getVp(iBnry);
		double	F 	= 1.0 - a*a;
		a 	= 1.0 + 2.0*getEpsilon(iBnry)*theSin2/F;
		double 	G 	= a*a - 2.0*(getEpsilon(iBnry)-getDelta(iBnry))*theSin2Theta*theSin2Theta/F;
		double 	D 	= 0.5*F*Math.sqrt(G);
		double  vel	= 0.0;

		if( iVp==1 ) {
			if(getEpsilon(iBnry)==getDelta(iBnry)) 
				return getVp(iBnry)*Math.sqrt(1.0+2.0*getEpsilon(iBnry)*theSin2);
			a 	= 1.0+getEpsilon(iBnry)*theSin2-0.5*F+D;
			vel = getVp(iBnry)*Math.sqrt(a);
		} else if( iVp==2 ) {//vsParallel
			a 	= 1.0+2.0*getGamma(iBnry)*theSin2;
			vel = getVs(iBnry)*Math.sqrt(a);
		} else if( iVp==3 ) { //vsVertical
			if(getEpsilon(iBnry)==getDelta(iBnry)) return getVp(iBnry);
			a 	= 1.0+getEpsilon(iBnry)*theSin2-0.5*F-D;
			vel = getVp(iBnry)*Math.sqrt(a);
		}  else {}

		return vel;
	}
	private double getExactPhaseVelDeriv(int iVTI, int iVp, int iBnry, double theta) {
		double a, b;
		double theSin 		= Math.sin(theta);
		double theSin2 		= theSin*theSin;
		double theCos2 		= 1.0-theSin2;
		double theCos 		= Math.cos(theta);
		double theSin2Theta = 2.0*theSin*theCos;
		double theCos2Theta = 2.0*theCos2-1.0;
		a	= getVs(iBnry)/getVp(iBnry);
		double	F 	= 1.0 - a*a;

		double G = 1.0+getEpsilon(iBnry)*theSin2;
		double dG = 2.0*getEpsilon(iBnry)*theSin*theCos;

		double E = 1.0+2.0*getEpsilon(iBnry)*theSin2/F;
		double dE = 4.0*getEpsilon(iBnry)*theSin*theCos/F;

		a 	= 1.0 + 2.0*getEpsilon(iBnry)*theSin2/F;
		double D = a*a - 2.0*(getEpsilon(iBnry)-getDelta(iBnry))*theSin2Theta*theSin2Theta/F;
		double dD = 2.0*E*dE-8.0*(getEpsilon(iBnry)-getDelta(iBnry))*theSin2Theta*theCos2Theta/F;

		double  vel	= 0.0;

		if( iVp==1 ) {
			a 	= G-0.5*F+0.5*F*Math.sqrt(D);
			b 	= dG+0.25*F*dD/Math.sqrt(D);
			vel = 0.5*getVp(iBnry)*b/Math.sqrt(a);
		} else if( iVp==2 ) {//vsParallel
			a 	= 1.0+2.0*getGamma(iBnry)*theSin2;
			b 	= 2.0*getGamma(iBnry)*theSin*theCos;
			vel = b*getVs(iBnry)/Math.sqrt(a);
		} else if( iVp==3 ) { //vsVertical
			a 	= G-0.5*F-0.5*F*Math.sqrt(D);
			b 	= dG-0.25*F*dD/Math.sqrt(D);
			vel = 0.5*getVp(iBnry)*b/Math.sqrt(a);
		}  else {}

		return vel;
	}
	private double getExactLayerGroupVel(int iVTI, int iVp, int iBnry, double theta) {
		double V 	= getExactLayerPhaseVel(iVTI, iVp, iBnry, theta);
		double dV 	= getExactPhaseVelDeriv(iVTI, iVp, iBnry, theta);
		return Math.sqrt(V*V+dV*dV);
	}
	private double getExactGroupTan(int iVTI, int iVp, int iBnry, double theta) {
		double theTan		= Math.tan(theta);
		double VEL 			= getLayerPhaseVel(iVTI, iVp, iBnry, theta);
		double dVEL 		= getPhaseVelDeriv(iVTI, iVp, iBnry, theta);

		double a			= theTan+dVEL/VEL;
		double b			= 1.0 - theTan*dVEL/VEL;

		return a/b;
	}
	public double getExactLayerPhaseAngle(int iVTI, int iVp, int iBnry, double p) {
		double a;
		double theSin2 	= 0.0;
		double p2 		= p*p;
		double vp2 		= getVp(iBnry)*getVp(iBnry);
		double vs2 		= getVs(iBnry)*getVs(iBnry);
		double	F 		= 1.0 - vs2/vp2;

		if( iVp==1 || iVp==3) {
			a = 1.0/(getVp(iBnry)*Math.sqrt((1.0+2.0*getEpsilon(iBnry))));
			if(Math.abs(p)>a) return _halfPI; // head wave occur

			double c0 	= (1.0-F)*vp2*vp2*p2*p2;
			double c1 	= 2.0*(getEpsilon(iBnry)-F*getDelta(iBnry))*vp2*p2-2.0+F;
			c1 	*= vp2*p2;
			double c2 	= 1.0-2.0*getEpsilon(iBnry)*vp2*p2-2.0*F*(getEpsilon(iBnry)-getDelta(iBnry))*vp2*vp2*p2*p2;

			a = c1*c1-4.0*c0*c2;

			if(iVp==1) theSin2 = (-c1+Math.sqrt(a))/(2.0*c2);
			else 		theSin2 = (-c1-Math.sqrt(a))/(2.0*c2); //vsVertical
		} else if( iVp==2 ) {//vsParallel
			a 	= p2*vs2;
			theSin2 = a/(1.0-2.0*getGamma(iBnry)*a);
		} else {}

		a = Math.sqrt(theSin2);
		return Math.asin(a);
	}
	private double getThomsenLayerPhaseVel(int iVTI, int iVp, int iBnry, double theta) {
		double theSin2, theSin4, theCos2;
		double a, vel;
		double thePhaseSin 		= Math.sin(theta);
		theSin2 = thePhaseSin*thePhaseSin;
		theCos2 = 1.0-theSin2;
		theSin4 = theSin2*theSin2;
		vel 	= 0.0;

		if( iVp==1 ) {
			a 	= 1.0+getDelta(iBnry)*theSin2+(getEpsilon(iBnry)-getDelta(iBnry))*theSin4;
			vel = getVp(iBnry)*a;
		} else if( iVp==2 ) {//vsParallel
			a 	= 1.0+getGamma(iBnry)*theSin2;
			vel = getVs(iBnry)*a;
		} else if( iVp==3 ) { //vsVertical
			if(getVs(iBnry)!=0.0) {
				a = getVp(iBnry)/getVs(iBnry);
				a = a*a;
			} else {
				a = 0.0;
			}
			a 	= 1.0+a*(getEpsilon(iBnry)-getDelta(iBnry))*theSin2*theCos2;
			vel = getVs(iBnry)*a;
		}  else {}

		return vel;
	}

	private double getThomsenPhaseVelDeriv(int iVTI, int iVp, int iBnry, double theta) {
		double theSin 		= Math.sin(theta);
		double theSin2 		= theSin*theSin;
		double theCos2 		= 1.0-theSin2;
		double theCos 		= Math.sqrt(theCos2);
		double theSin2Theta = 2.0*theSin*theCos;
		double dv = 1.0, a, b;

		if( iVp==1 ) {
			a 	= getDelta(iBnry)*(theCos2-theSin2);
			b 	= 2.0*getEpsilon(iBnry)*theSin2;
			dv 	= getVp(iBnry)*theSin2Theta*(a+b);
		} else if( iVp==2 ) {//vsParallel
			dv 	= getVs(iBnry)*getGamma(iBnry)*theSin2Theta;
		} else if( iVp==3 ) { //vsVertical
			if(getVs(iBnry)!=0.0) {
				a = getVp(iBnry)/getVs(iBnry);
				a = a*a;
			} else {
				a = 0.0;
			}

			b 	= a*(getEpsilon(iBnry)-getDelta(iBnry))*(theCos2-theSin2)*theSin2Theta;
			dv 	= getVs(iBnry)*b;
		}  else {}

		return dv;
	}

	private double getThomsenLayerGroupVel(int iVTI, int iVp, int iBnry, double theta) {
		double V 	= getThomsenLayerPhaseVel(iVTI, iVp, iBnry, theta);
		double dV 	= getThomsenPhaseVelDeriv(iVTI, iVp, iBnry, theta);
		return Math.sqrt(V*V+dV*dV);
	}
	private double getThomsenGroupTan(int iVTI, int iVp, int iBnry, double theta) {
		double a, theGroupTan = 0.0;

		double theSin 	= Math.sin(theta);
		double theSin2 	= theSin*theSin;
		double theTan 	= Math.tan(theta);
		//theTan 	= theSin/Math.sqrt(1.0-theSin2);

		if( iVp==1 ) {
			a 	= 1.0+2.0*getDelta(iBnry)+4.0*(getEpsilon(iBnry)-getDelta(iBnry))*theSin2;
			theGroupTan = theTan*a;
		} else if( iVp==2 ) {//vsParallel
			a 	= 1.0+2.0*getGamma(iBnry);
			theGroupTan = theTan*a;
		} else if( iVp==3 ) { //vsVertical
			if(getVs(iBnry)!=0.0) {
				a = getVp(iBnry)/getVs(iBnry);
				a = a*a;
			} else {
				a = 0.0;
			}
			a 	= 1.0+2.0*a*(getEpsilon(iBnry)-getDelta(iBnry))*(1.0-2.0*theSin2);
			theGroupTan = theTan*a;
		}  else {}

		return theGroupTan;
	}

	public String toString() {
		String b = new String("min_phaseAngle, max_phaseAngle, min_groupAngle, max_groupAngle, min_groupVel, max_groupVel, min_RP, max_RP");
		for(int i=0; i<_gAngles.length; i++) {
			b = new String(b.concat("\n"+min(_pAngles)*90.0/_halfPI+" "+max(_pAngles)*90.0/_halfPI+" "+
					min(_gAngles[i])*90.0/_halfPI+" "+max(_gAngles[i])*90.0/_halfPI+" "+min(_gVel[i])+" "
					+max(_gVel[i])+" "+min(_rayPars[i])+" "+max(_rayPars[i])));
		}
		return b;
	}

	public double max(double [] v) {
		double max = v[0];
		for(int i=1; i<v.length; i++) { max = max>v[i] ? max:v[i]; }
		return max;
	}
	public double min(double [] v) {
		double min = v[0];
		for(int i=1; i<v.length; i++) { min = min<v[i] ? min:v[i]; }
		return min;
	}
	public int maxIndex(double [] v) {
		double max = v[0];
		int k = 0;
		for(int i=1; i<v.length; i++) {
			if(max<v[i]) { max = v[i]; k = i; }
		}
		return k;
	}
	public int minIndex(double [] v) {
		double min = v[0];
		int k = 0;
		for(int i=1; i<v.length; i++) {
			if(min>v[i]) { min = v[i]; k = i; }
		}
		return k;
	}

}
