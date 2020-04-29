package com.geohammer.rt.pseudo3d;

import java.util.Arrays;

import com.geohammer.core.acquisition.VCPair;
import com.geohammer.core.planarmodel.FlatLayer1D;
import com.geohammer.core.planarmodel.Layer2D;
import com.geohammer.core.planarmodel.Point2D;
import com.geohammer.core.planarmodel.SingleLine;
import com.geohammer.rt.RayPath;
import com.geohammer.rt.RayPaths;

// Theoretical travel time calculation for layered velocity model
public class RayTracing2D implements RayTracerI {
	private FrameI 	_frame 			= null;
	private Layer2D _layer2D 		= null;
	private Layer2D _flayer1DTrim 	= null;
	private VCPair 		_vcPW 		= null;
	public SeismicVel _vel 			= null;

	private int		 	_iDynamic	= 0;		// 1- dynamically search x-dir, 0- a constant x-dir;
	private int 		_iDebug 	= 0;		// 0 - no debug 1- level 1 2- level 2 ...
	private int 		_iFlood 	= 0;		// 0 - no debug 1- level 1 2- level 2 ...

	private double 		_tol 		= 0.1; 		// distance to terminate a ray
	private double 		_quaterPI 	= Math.atan(1.0);
	private double 		_halfPI 	= 2.0*_quaterPI;

	private int 		_iVTI		= 0;	// 0- non-anisotropic, 1- VTI; 2- HTI; 
	private int 		_iVp		= 1;	// 1- P-wave, 2- S-wave parallel SH; 3- S-wave vertical SV;
	// 11= for aniso P-wave, 12= for aniso SH-wave; 13= for aniso SV-wave

	private int 		_iApprox 	= 0; 	//0= use exact formula for anisotropic ray tracing (need S velocity), 
	// 1= use approximate formula

	private RayPaths 	_rayPaths 	= null;
	private int [] 		_rayCodes	= null; // null for direct, rayCodes[0] first refelctor, rayCodes[1] second reflector ...
	//private int 		_iHeadWave	= 0;	// 0- no head wave, 1- consider head wave when combined with rayCodes
	//private int 		_iDirectWave		= 1;	// 0- no direct wave, 1- direct
	//private int 		_iHeadDirectWave	= 0;	// 0- no head wave, 1- consider head wave when combined with rayCodes
	private int 		_iWave	= 1;	// 1- Direct Waves Only 2 -Head Waves Only 3-Direct + Head Waves 4-Direct + Reflect + Multiple Waves

	private double [] 	_calT 		= null; // symbolic
	private double [] 	_takeOffAngle= null; 
	private float  [] 	_jac 		= null;

	private int 		_iT			= 1;	// travel time calculation 1- on, 0- off;
	private int 		_iT0		= 1;	// origin time calculation 1- on, 0- off;
	private int 		_iR			= 0;	// ray path calculation 1- on, 0- off;
	private int 		_iJ			= 0;	// derivative calculation 1- on, 0- off;
 
	private int 		_shotLayerIndex	= 0;
	private double 		_INVALID_D 	= -99999.0;

	public RayTracing2D(FrameI frame, Layer2D layer2D, VCPair vcPW, int iVp, int iVTI, 
			int [] rayCodes, int iWave, int iR) {
		this(frame, layer2D, vcPW, iVp, iVTI, 0, rayCodes, iWave, 0.1, 1, iR);
	}

	public RayTracing2D(FrameI frame, Layer2D layer2D, VCPair vcPW, int iVp, int iVTI, int iApprox, 
			int [] rayCodes, int iWave, double tol, int iT0, int iR) {
		_frame 		= frame;
		_layer2D 	= layer2D;
		_vcPW		= vcPW;
		_iVp 		= iVp;
		_iVTI 		= iVTI;
		_iApprox 	= iApprox;
		_rayCodes 	= rayCodes;
		_iWave 		= iWave;
		_tol 		= tol;
		_iR 		= iR;
		_iT0 		= iT0;
		if(_iVp>10) System.exit(0);
		setConfiguration(layer2D, vcPW);
		System.out.println(statusToString());
		//System.out.println(vcPW.toString());
	}
	public void setConfiguration(Layer2D layer2D, VCPair vcPW) {
		if(_layer2D==null) 	_layer2D 	= layer2D;
		if(_vcPW==null) 	_vcPW		= vcPW;

		setConfiguration();
	}
	public void setConfiguration() {
		for(int i=0; i<_layer2D.getNumOfBoundaries(); i++) _layer2D.getLayer(i).setCurve();
		_takeOffAngle 	= new double[_vcPW.getNumOfReceivers()];
		if(_iVp==1) 	_calT 	= _vcPW._calPT;
		else 			_calT 	= _vcPW._calST;

		for(int i=0; i<_calT.length; i++) _calT[i] = 0.0;
		
		if(_iVTI>0) { _vel = new SeismicVel(_layer2D, _iVTI, _iVp, _iApprox);} 
		if(_iR==1) 	{ _rayPaths = new RayPaths(_vcPW.getNumOfEvents()); }
		//System.out.println(Arrays.toString(_layer2D.getVp()));
	}
	
	public void setIApproximation( int v) 			{ if(_vel!=null) _vel.setIApproximation(v); }
	public void setIT( int v) 						{ _iT = v; }
	public void setIT0( int v) 						{ _iT0 = v; }
	public void setIR( int v) 						{ _iR = v; }
	public void setIJ( int v) 						{ _iJ = v; }
	public SeismicVel getSeismicVel() 				{ return _vel; }
	public void setLayer(Layer2D layer2D) 		{ _layer2D = layer2D; }
	public void setPair(VCPair vcPW) 				{ setConfiguration(null, vcPW); }

	public void setRayCode(int [] rayCodes) 		{ _rayCodes = rayCodes; }
	public void  setIWave(int iWave) 				{ _iWave = iWave;}
	public void  setIVp(int iVp) 					{ _iVp = iVp;}
	public void  setIVTI(int iVTI) 					{ _iVTI = iVTI; }
	public void setTolerance(double tol) 			{ _tol = tol; }

	public int getNumOfBoundaries() 				{ return _layer2D.getNumOfBoundaries(); }
	public double getTDepth(int index) 				{ return _layer2D.getLayer(index).getTDepth(); }
	public RayPaths getRayPaths() 					{ return _rayPaths; }
	public double [] getTakeOffAngle()				{ return _takeOffAngle; }
	
	public int [] getRayCode() 						{ return _rayCodes; }
	public double getTolerance() 					{ return _tol; }
	public int  getIWave() 							{ return _iWave;}
	public int  getIVp() 							{ return _iVp;}
	public int  getIVTI() 							{ return _iVTI;}
	
	public int  getIT0() 							{ return _iT0;}
	public int  getIT() 							{ return _iT;}
	public int  getIR() 							{ return _iR;}
	public int  getIApproximation() 				{ if(_vel!=null) return _vel.getIApproximation(); return 0;}
	
	public FlatLayer1D getFlatLayer1D() 			{ return _layer2D.toFlatLayer1D(true); }
	public Layer2D getLayer() 						{ return _layer2D; }
	public VCPair getVCPair() 						{ return _vcPW; }
	
	
	private SingleLine getLine(int index) 			{ return _layer2D.getLayer(index).getCurve().getLine(0); }
	
	public double getRayPar(double takeOffAngle) 	{ //could be negative
		double pVel = _layer2D.getLayer(_shotLayerIndex).getVs();
		if(_iVp==1) pVel 	= _layer2D.getLayer(_shotLayerIndex).getVp();
		if(_iVTI==0) return Math.sin(takeOffAngle)/pVel;

		double theta = takeOffAngle;
		if(_iVTI==3) theta = _halfPI-takeOffAngle;
		pVel = _vel.getLayerPhaseVel(_iVTI, _iVp, _shotLayerIndex, theta);
		return Math.sin(takeOffAngle)/pVel; 
	}
	public double getPhaseAngle(int iVp, int iBnry, double p) {
		//return _vel.getExactLayerPhaseAngle(iVp, iBnry, p);
		return _vel.getPhaseAngleFromRayParsTable(iBnry, p);
	}
	public double getGroupAngle(int iVp, int iBnry, double p) {
		double phaseAngle = getPhaseAngle(iVp, iBnry, p);
		return _vel.getLayerGroupAngle(_iVTI, iVp, iBnry, phaseAngle);
	}

	public void start() { startRayTracing(1, null); }
	public void startRayTracing(int runNo, int [] iLayer) {
		if(runNo==2) {
			System.out.println(statusToString());
			if(_iR==1) 	{ _rayPaths = new RayPaths(_vcPW.getNumOfEvents()); }
		}
		for(int i=0, k=0; i<_vcPW.getNumOfEvents(); i++) {
			int ke 		= _layer2D.getTopIndex(_vcPW.getED(i));
			for(int j=0; j<_vcPW.getNumOfReceivers(i); j++, k++) {
				if(_iWave==1||_iWave==3||_iWave==4) { 
					//other wave types
					int kr 			= _layer2D.getTopIndex(_vcPW.getRD(k));
					_shotLayerIndex = ke;
					boolean inTheSameLayer = false;
					if(ke==kr) { inTheSameLayer = true; }
					if(ke==_layer2D.getBoundaryIndex(_vcPW.getRD(k))) { inTheSameLayer = true; }
					if(_layer2D.getBoundaryIndex(_vcPW.getED(i))==kr) { inTheSameLayer = true; _shotLayerIndex--; }
					if(inTheSameLayer && _rayCodes==null) { //direct arrivals
						double dx = _vcPW.getEE(i)-_vcPW.getRE(k);
						double dy = _vcPW.getEN(i)-_vcPW.getRN(k);
						double dz = _vcPW.getED(i)-_vcPW.getRD(k);
						double theSin 	= Math.sqrt(dx*dx+dy*dy)/Math.sqrt(dx*dx+dy*dy+dz*dz);
						double gVel 	= _layer2D.getLayer(_shotLayerIndex).getVs();
						if(_iVp==1) gVel 	= _layer2D.getLayer(_shotLayerIndex).getVp();
						double gAngle 	= Math.asin(theSin); //[-90, 90]
						double pAngle 	= gAngle;
						if(_iVTI>0) {
							gAngle 	= Math.asin(theSin);
							if(_iVTI==3) gAngle = _halfPI-gAngle;
							pAngle 	= _vel.getPhaseAngleFromAngleTable(_shotLayerIndex, gAngle);
							gVel 	= _vel.getTableValue(2, _shotLayerIndex, pAngle);
						} 
						if(_iT==1) { 
							int sign = dz>0.0 ? -1:1;
							_takeOffAngle[k] = sign*pAngle; 
							if(_iVTI==3) _takeOffAngle[k] = sign*(_halfPI-pAngle);
							_calT[k] = Math.sqrt(dx*dx+dy*dy+dz*dz)/gVel; 
						}
						if(_iR==1) {
							RayPath rayPath = new RayPath(2);
							rayPath.setPoint(0, _vcPW.getEE(i), _vcPW.getEN(i), _vcPW.getED(i));
							rayPath.setPoint(1, _vcPW.getRE(k), _vcPW.getRN(k), _vcPW.getRD(k));
							_rayPaths.add(rayPath);
						}
					} else {
						double [] rayCodesZ = null;
						int [] rayCodesZPS = null;
						int iPS = 1; 
						if(_iVp==2) { iPS = -1;}
						if(_rayCodes!=null) { 
							rayCodesZ = new double [2+_rayCodes.length];
							rayCodesZPS = new int [2+_rayCodes.length];
							int ik = 0;
							rayCodesZ[ik] = _vcPW.getED(i); 
							rayCodesZPS[ik] = iPS; 
							ik++;
							for(int ii=0; ii<_rayCodes.length; ii++, ik++) { 
								rayCodesZ[ik] = getTDepth(Math.abs(_rayCodes[ii])-1); 
								if(_rayCodes[ii]<0) rayCodesZPS[ik] = -1; 
								else rayCodesZPS[ik] = 1;
							}
							rayCodesZ[ik] = _vcPW.getRD(k); 
							rayCodesZPS[ik] = 1; //the sign of last one does not matter
						} else { 
							rayCodesZ = new double []{_vcPW.getED(i), _vcPW.getRD(k)}; 
							rayCodesZPS = new int []{iPS, iPS};
						}
						//System.out.println("rayCodesZ= "+ Arrays.toString(rayCodesZPS));
						if(_iDebug==1) {System.out.println("rayCodesZ= "+ Arrays.toString(rayCodesZ));}

						if(_layer2D.onTBoundary(_vcPW.getED(i))) { if(rayCodesZ[1]<rayCodesZ[0]) _shotLayerIndex--; } 

						_takeOffAngle[k] = searchOneStraightRay(_layer2D.copy(), _vcPW.getEE(i), _vcPW.getEN(i), _vcPW.getED(i), _vcPW.getRE(k), _vcPW.getRN(k), _vcPW.getRD(k),
								rayCodesZ, rayCodesZPS);

						if(_iDebug==1) {System.out.println("rayPar= "+ _takeOffAngle[k]+" shotLayerIndex= "+ _shotLayerIndex);}
						if(_takeOffAngle[k]==_INVALID_D) { 
							_calT[k] = _INVALID_D; 
						} else {
							traceOneStraightRay(_layer2D.copy(), _vcPW.getEE(i), _vcPW.getEN(i), _vcPW.getED(i), _vcPW.getRE(k), _vcPW.getRN(k), _vcPW.getRD(k),
									rayCodesZ, rayCodesZPS, _iT, k, _calT, _takeOffAngle[k], _iR, _iJ, null);
							int sign = rayCodesZ[0]>rayCodesZ[1] ? -1:1;
							_takeOffAngle[k] *= sign;

							if(_iDebug==1) {System.out.println("\np="+ _takeOffAngle[k]+" _takeOffAngle="+ _takeOffAngle[k]);}
							if(_iDebug==1) {System.out.println("\nTrim Model: "+ _flayer1DTrim.toDeepString());}
						}
					}
				}
				if(_iWave==2||_iWave==3) { 
					double [] calT1 = new double[_layer2D.getNumOfBoundaries()];
					for(int ii=0; ii<calT1.length; ii++) calT1[ii] = 1.0e10;
					int kLayer = searchHeadWave(_layer2D, calT1, _vcPW.getEE(i), _vcPW.getEN(i), _vcPW.getED(i), 
							_vcPW.getRE(k), _vcPW.getRN(k), _vcPW.getRD(k));
					if(_iDebug==1) {System.out.println("kLayer= "+ kLayer);}
					System.out.println("kLayer= "+ kLayer);
					if(kLayer>=0&&kLayer<_layer2D.getNumOfBoundaries()) {
						if(_iWave==3) {
							if(calT1[kLayer]>_calT[k]) continue;
							else _rayPaths.getRayPathList().remove(_rayPaths.getRayPathList().size()-1);
						}
						double [] rayCodesZ = new double []{_vcPW.getED(i), _layer2D.getLayer(kLayer).getTDepth(), _vcPW.getRD(k)};
						double p = traceHeadWave(_layer2D.copy(), _vcPW.getEE(i), _vcPW.getEN(i), _vcPW.getED(i), 
								_vcPW.getRE(k), _vcPW.getRN(k), _vcPW.getRD(k), 
								rayCodesZ, _iT, k, _calT, _iR, _iJ, null);

						if(_iVTI==0) {
							double theSin = p*_flayer1DTrim.getVel(_iVp, 0);
							_takeOffAngle[k] = Math.asin(theSin);
						} else {
							if(_flayer1DTrim.getLayer(1).getTDepth()>_flayer1DTrim.getLayer(0).getTDepth())
								_takeOffAngle[k] = getGroupAngle(_iVp, ke, p);
							else _takeOffAngle[k] = getGroupAngle(_iVp, ke-1, p);
						}
						if(_iDebug==1) {System.out.println("\np="+ p+" _takeOffAngle="+ _takeOffAngle[k]);}
						if(_iDebug==1) {System.out.println("\nTrim Model: "+ _flayer1DTrim.toDeepString());}
					} else {
						
					}
				}	
			}
			//System.out.println(timeToString());
		}
		if(_iT==1 &&_iT0==1) { _vcPW.estimateT0(); _vcPW.addT0(); }
		else { _vcPW.zeroT0(); }
		if(_iR==1) {
			if(_rayPaths.getNumOfTraces()>0) {
				//System.out.println(_rayPaths.toString());
				_rayPaths.updateHeaders();
			}
			//for case from ray to none ray
			if(_frame!=null) { _frame.addRayPath(_rayPaths); }
		} else {
			if(_frame!=null&&runNo==2) { _frame.removeRayPath(); }
		}
		if(_frame!=null&&runNo==2) { _frame.updateTime(_iVp, 0);	}
	}

	public double searchOneStraightRay(Layer2D layer2D, double sx, double sy, double sz, double rx, double ry, double rz,
			double [] rayCodesZ, int [] rayCodesZPS) {

		layer2D.split(sz, rz);
		if(_iDebug==21) {System.out.println("\nInserted Model: "+ layer2D.toDeepString());}
		int [][] rayCodesIdz = layer2D.calRayCodesIdz(rayCodesZ, rayCodesZPS);
		if(_iDebug==21) {
			System.out.println("\nray Code: "+Arrays.toString(rayCodesIdz[0]));
			System.out.println("idz= "+" "+Arrays.toString(rayCodesIdz[1]));
		}

		Layer2D flayer1DTrim = layer2D.toLayer1DPrime(rayCodesIdz[0]);
		if(_iDebug==21) {System.out.println("\nTrim Model: "+ flayer1DTrim.toDeepString());}
		double [] hPrime 	= flayer1DTrim.calHPrime();
		double [] vPrime	= flayer1DTrim.calVPrime(1);
		int    [] index 	= flayer1DTrim.calLayerIndex();
		double [] xPrime 	= new double[hPrime.length];
		double [] zPrime 	= layer2D.calZPrime(rayCodesIdz);
		if(_iDebug==21) {
			System.out.println("hPrime= "+ Arrays.toString(hPrime));
			System.out.println("index= "+ Arrays.toString(index));			
			System.out.println("zPrime= "+ Arrays.toString(zPrime));
		}

		double d0 			= Math.sqrt((rx-sx)*(rx-sx)+(ry-sy)*(ry-sy));

		if(_iVTI>0) return bisection(d0, hPrime, xPrime, vPrime, index);
		else 		return bisection(d0, hPrime, xPrime, vPrime, null);
	}

	public double bisection(double d0, double [] hPrime, double [] xPrime, double [] vPrime, int [] index) {
		double x1 = 0.0;	
		double x2 = _halfPI;
		double dx = 0.0;
		double f = 0.0;
		double xMid = 0.5*(x1+x2);
		double rtb = 0.0;
		double t = 0.0;

		t = epiDistance(xMid, _iVp, hPrime, vPrime, xPrime, index); 
		f = d0-xPrime[xPrime.length-1];
		//System.exit(1);
		if(_iDebug==22) {System.out.println("\nd0: "+d0+" d="+ (d0-f) + " f="+f);}
		if(Math.abs(f) <= _tol ) { return xMid; } 
		else if(f>0.0) { 	x1 = xMid; 	x2 = _halfPI; } 
		else { 				x1 = 0.0; x2 = xMid; }
		dx = x2-x1; 
		rtb = x1;

		for(int JMAX=100, j=1; j<JMAX; j++) {
			dx *= 0.5;
			xMid = rtb + dx;
			t = epiDistance(xMid, _iVp, hPrime, vPrime, xPrime, index); 
			f = d0-xPrime[xPrime.length-1]; 
			if(_iDebug==22) {System.out.println("\nd0: "+d0+" d="+ (d0-f) + " f="+f+ " xMid="+xMid*90/_halfPI);}

			if( Math.abs(f) <= _tol ) 	{ return xMid; } 
			else if( f>0.0) 			{ rtb = xMid; } 
			else { }
		} 

		return _INVALID_D;
	}
	// receivers and sources must be in different layers
	private int epiDistance(double [] hPrime, double [] vPrime, double [] xPrime, int maxVindex, double d0, double q, 
			double [] f, double []fPrime,  double []dq ){
		double b 		= 0.0;
		double epsilon 	= 0.0;

		f[0]=0.0; fPrime[0]=0.0; dq[0]=0.0;
		//equation (6) and (7)
		for(int i=0; i<hPrime.length; i++ ) {
			epsilon = vPrime[i]/vPrime[maxVindex];
			b 		= hPrime[maxVindex]*hPrime[maxVindex] + (1.0-epsilon*epsilon)*q*q;

			f[0] 		+= q*epsilon*hPrime[i] / Math.sqrt(b);
			xPrime[i] 	= f[0];
			fPrime[0] 	+= epsilon*hPrime[i] / Math.pow(b, 1.5);
		}

		f[0] 		= f[0] - d0;
		fPrime[0] 	= hPrime[maxVindex]*hPrime[maxVindex]*fPrime[0];
		dq[0] 		= f[0] / fPrime[0];
		return 0;
	}

	private double epiDistance(double takeOffPhaseAngle, int iVp, double [] hPrime, double [] vPrime, double [] xPrime, int [] index){
		double p = getRayPar(takeOffPhaseAngle);
		return epiDistanceP(p, iVp, hPrime, vPrime, xPrime, index);
	}
	private double epiDistanceP(double p, int iVp, double [] hPrime, double [] vPrime, double [] xPrime, int [] index) {
		double theta = 0.0;
		double gAngle = 0.0;
		double gVel = 0.0;
		double t = 0;
		double x = 0;
		//iso
		if(index==null) {
			for(int i=0; i<hPrime.length; i++ ) {
				x += hPrime[i]*p*vPrime[i]/(Math.sqrt(1.0-vPrime[i]*vPrime[i]*p*p));
				xPrime[i] = x;
				t += hPrime[i]/(vPrime[i] * Math.sqrt(1.0-vPrime[i]*vPrime[i]*p*p));
			}
			return t;
		}
		//aniso
		for(int i=0, iBnry=0; i<index.length; i++) {
			iBnry 		= index[i];
			theta 		= getPhaseAngle(iVp, iBnry, p);
			gAngle 		= _vel.getTableValue(1, iBnry, theta);
			gVel 		= _vel.getTableValue(2, iBnry, theta);
			if(_iVTI==3) gAngle = _halfPI-gAngle;
			x 			+= hPrime[i]*Math.tan(gAngle);
			xPrime[i] 	= x;

			t 			+= hPrime[i]/(gVel*Math.cos(gAngle));
		}
		return t;
	}
	
	// receivers and sources must be in different layers
	/*
	RayCode[3][]
    [1,:] -- types of rays: = 1 for P, = 2 for fast S (S1), = 3 for slow S (S2);
    [2,:] -- number of layer where the segment is located;
    [3,:] -- number of interface where the next reflection/transmission occurs.
                            
    RayCode(1,:) = [1, 1, 1, 1, 1, 1];     %% ray type
    RayCode(2,:) = [1, 2, 3, 3, 2, 1];     %% layers
    RayCode(3,:) = [2, 3, 4, 3, 2, 1];     %% interfaces
 
        S               R
    ---------------------------------     interface 1                
        \              /                                                
       1 \            /6   layer 1                                       
    ---------------------------------     interface 2
           \        /       
          2 \      /5      layer 2
    ---------------------------------     interface 3
              \  /
             3 \/4         layer 3
    ---------------------------------     interface 4

*/
    
	public int traceOneStraightRay(Layer2D layer2D, double sx, double sz, double rx, double rz,
			int [][] rayCode, Point2D p0, 
			int iT, int iPair, double [] calT, int iR, int iJacobian, double [] jac) {

		for(int i=0; i<rayCode[1].length; i++) {
			SingleLine line = getLine(rayCode[2][i]);
			
			
		}

		return 0;
	}

	private RayPath calRayPath(int nPoints, double sz, double [] xPrime, double [] zPrime) {
		RayPath rayPath 	= new RayPath(nPoints);
		rayPath.setPoint(0, 0, 0, sz);
		for(int i=1, j=1; i<nPoints; i++) { 
			j = i-1;
			rayPath.setPoint(i, xPrime[j], 0.0, zPrime[j]);
		}
		return rayPath;
	}
	
	//positive is shooting downward; negative is shooting upward
	public int getSignOfTakeOffAngle(RayPath rayPath) {
		double sz = rayPath.getZ(0);
		double rz = rayPath.getZ(1);
		if(sz>rz) return -1;
		else return 1;
	}

	public int searchHeadWave(Layer2D layer2D, double [] calT, double sx, double sy, double sz, double rx, double ry, double rz) {
		//System.out.println("n="+layer2D.getNumOfBoundaries());
		for(int i=1; i<layer2D.getNumOfBoundaries()-1; i++) {
			double [] rayCodesZ = new double []{sz, layer2D.getLayer(i).getTDepth(), rz};
			traceHeadWave(layer2D.copy(), sx, sy, sz, rx, ry, rz, rayCodesZ, 1, i, calT, 0, 0, null);
		}

		int minK 	= 0;
		double min 	= 1.0e10;
		for(int i=0; i<calT.length; i++) {
			if(min>calT[i]) { min = calT[i]; minK = i; }
		}
		if(_iDebug==30) { System.out.println("calT= "+ Arrays.toString(calT)); }
		if(min>0.0 && min<100.0) 	return minK;
		else 						return -1;
	}
	public double traceHeadWave(Layer2D layer2D, double sx, double sy, double sz, double rx, double ry, double rz, 
			double [] rayCodesZ, 
			int iT, int iPair, double [] calT, int iR, int iJacobian, double [] jac) {
		layer2D.split(sz, rz);
		int [][] rayCodesIdz = layer2D.calRayCodesIdz(rayCodesZ, null);

		_flayer1DTrim = layer2D.toLayer1DPrime(rayCodesIdz[0]);
		double [] vPrime	= _flayer1DTrim.calVPrime(1);
		double [] hPrime 	= _flayer1DTrim.calHPrime();
		int    [] index 	= _flayer1DTrim.calLayerIndex();
		double [] xPrime 	= new double[hPrime.length];
		double [] zPrime 	= layer2D.calZPrime(rayCodesIdz);

		double [] vMax 		= new double[]{0.0};
		if(!headWavePossible(_iVp, rayCodesZ, vMax)) { calT[iPair] = 1.0e10; return _INVALID_D; }
		double p = 1.0/vMax[0];
		double t = 0.0;
		double [] dHeadWave = null;

		if(_iDebug==30) {
			System.out.println("vMax= "+vMax[0]+" t= "+t+" p= "+p);
			System.out.println("hPrime= "+ Arrays.toString(hPrime));
			System.out.println("vpPrime= "+ Arrays.toString(vPrime));			
			System.out.println("zPrime= "+ Arrays.toString(zPrime));
		}
		if(_iVTI>0) t += epiDistanceP(p, _iVp, hPrime, vPrime, xPrime, index);
		else 		t += epiDistanceP(p, _iVp, hPrime, vPrime, xPrime, null);

		if(rayCodesZ[1]==-4000.0&&_iDebug==30) {
			System.out.println("xPrime= "+ Arrays.toString(xPrime));
		}
		double d0 	= Math.sqrt((rx-sx)*(rx-sx)+(ry-sy)*(ry-sy));
		double d 	= xPrime[xPrime.length-1];
		if(d0<d) { calT[iPair] = 1.0e10; return _INVALID_D; }
		else {
			int iK = layer2D.getTopIndex(rayCodesZ[1]);
			dHeadWave = new double[]{d0-d, layer2D.getLayer(iK).getTDepth()};
			t += (d0-d)/vMax[0];
		}
		if(iT==1) {calT[iPair] = t; }

		if(rayCodesZ[1]==-4000.0&&_iDebug==30) {
			System.out.println("d= "+d+" t= "+t+" d0= "+d0);
			System.out.println("dHeadWave= "+ Arrays.toString(dHeadWave));
		}
		if(iR==1) {
			int nPoints = rayCodesIdz[0].length+1;
			RayPath rayPath = calRayPath(nPoints, sz, xPrime, zPrime);
			RayPath rayPath1 = rayPath.insert(dHeadWave);
			rayPath1.rotate(sx, sy, sz, rx, ry, rz);
			rayPath1.transfer(sx, sy);
			_rayPaths.add(rayPath1);
		}

		return p;
	}
	
	public boolean headWavePossible(int iVp, double[] z, double [] max) { 
		assert z.length != 3 : "Error: only three element array needed";
		int ke = _layer2D.getTopIndex(z[0]);
		int kr = _layer2D.getTopIndex(z[2]);
		int k  = _layer2D.getTopIndex(z[1]);

		double vMax = 0.0;
		if(k>ke && k>kr) {
			if(k==getNumOfBoundaries()-1) return false;
			if(_iVTI==0) 	vMax = _layer2D.getVel(_iVp, k);
			else 			vMax = _vel.getLayerPhaseVel(_iVTI, _iVp, k, _halfPI);
			
			for(int i=ke; i<k; i++) { if(_layer2D.getVel(iVp, i)>vMax) return false; }
			for(int i=kr; i<k; i++) { if(_layer2D.getVel(iVp, i)>vMax) return false; }
		} else if(k<ke && k<kr) {
			if(k==0) return false;

			if(_iVTI==0) 	vMax = _layer2D.getVel(_iVp, k-1);
			else 			vMax = _vel.getLayerPhaseVel(_iVTI, _iVp, k-1, _halfPI);
			for(int i=k; i<ke; i++) { if(_layer2D.getVel(iVp, i)>vMax) return false; }
			for(int i=k; i<kr; i++) { if(_layer2D.getVel(iVp, i)>vMax) return false; }
		} else { return false; }
		max[0] = vMax;
		//		if(k==5) {
		//			System.out.println("idz= "+" "+Arrays.toString(z));
		//			System.out.println("ke= "+ke+" k= "+k+" kr= "+kr+" vMax= "+vMax);
		//		}
		return true;
	}
	public String timeToString()	{
		String a;
		String b = new String(" ");

		int k = 0;
		int n = 0;
		double radToDegree = 180.0/3.1415926;
		for(int i=0; i<_vcPW._nEvents; i++) {
			for(int j=0; j<_vcPW._flag[i]; j++, k++) {
				//if(_calT[k]==_INVALID_D) {
				n++;
				a = String.format( "%10.2f, %10.2f, %10.2f, %10.2f, %10.2f, %10.2f, %10.6f, %10.6f, %10.10f\n", 
						_vcPW._eE[i], _vcPW._eN[i], _vcPW._eD[i], _vcPW._rE[k], _vcPW._rN[k], _vcPW._rD[k], 
						_vcPW._obsPT[k], _calT[k], _takeOffAngle[k]*radToDegree );
				b = new String(b.concat(a));
				//}
			}
		}
		//a = String.format( "k=%d, n=%d, percent=%10.2f\n", k, n, 100.0*n/k );
		//b = new String(b.concat(a));
		return b;
	}

	public String statusToString()	{
		return String.format( "iVTI=%d, iVp=%d, iApprox=%d iWave=%d tol=%f iT=%d, iT0=%d iR=%d, iJ=%d\n", 
				_iVTI, _iVp, _iApprox, _iWave, _tol, _iT, _iT0, _iR, _iJ);
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
