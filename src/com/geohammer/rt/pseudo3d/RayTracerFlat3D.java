package com.geohammer.rt.pseudo3d;

import java.util.Arrays;

import com.geohammer.core.acquisition.VCPair;
import com.geohammer.core.planarmodel.DipLayer1D;
import com.geohammer.core.planarmodel.FlatLayer1D;
import com.geohammer.core.planarmodel.Layer2D;
import com.geohammer.core.planarmodel.Point3D;
import com.geohammer.rt.RayPath;
import com.geohammer.rt.RayPaths;

// Theoretical travel time calculation for layered velocity model
public class RayTracerFlat3D implements RayTracerI {
	public FrameI 		_frame 			= null;
	public FlatLayer1D 	_layer1D 		= null;
	public VCPair 		_vcPW 			= null;
	public SeismicVel 	_vel 			= null;

	public int		_iDynamic	= 0;		// 1- dynamically search x-dir, 0- a constant x-dir;
	public int 		_iDebug 	= 0;		// 0 - no debug 1- level 1 2- level 2 ...
	public int 		_iFlood 	= 0;		// 0 - no debug 1- level 1 2- level 2 ...

	public double 	_tol 		= 0.01; 		// distance to terminate a ray
	public double 	_quaterPI 	= Math.atan(1.0);
	public double 	_halfPI 	= 2.0*_quaterPI;
	
	public int 		_iMethod 	= 1;   // 0 - tabulate 1- nAngles = 1
	public int 		_iVTI		= 0;	// 0- non-anisotropic, 1- VTI; 2- HTI; 
	public int 		_iVp		= 1;	// 1- P-wave, 2- S-wave parallel SH; 3- S-wave vertical SV;
	// 11= for aniso P-wave, 12= for aniso SH-wave; 13= for aniso SV-wave

	public int 		_iApprox 	= 0; 	//0= use exact formula for anisotropic ray tracing (need S velocity), 
	// 1= use approximate formula

	public RayPaths 	_rayPaths 	= null;
	public int [] 		_psIndex	= null;
	public int [] 		_reflectorIndex	= null; // null for direct, rayCodes[0] first refelctor, rayCodes[1] second reflector ...
	public int 			_iWave		= 1;	// 1- Direct Waves Only 2 -Head Waves Only 3-Direct + Head Waves 4-Reflect + Multiple Waves
	public boolean 		_headOn 	= true;
	public double [] 	_calT 		= null; // symbolic
	public double [] 	_rayPar 	= null; 
	public double [] 	_rayPoint 	= null; 
	public double  [][]	_pgs 		= null;
	public double  [][]	_jac 		= null;
	public double  [][]	_A	 		= null;
	public double  []	_L	 		= null;

	public int 		_iT			= 1;	// travel time calculation 1- on, 0- off;
	private int 	_iT0		= 0;	// not used but for carry purpose;
	public int 		_iR			= 0;	// ray path calculation 1- on, 0- off;
	public int 		_iJ			= 0;	// derivative calculation 1- on, 0- off;
	
	public int 		_shotLayerIndex	= 0;
	public double 	_INVALID_D 	= -99999.0;

	public RayTracerFlat3D() { }
	
	public RayTracerFlat3D(FrameI frame, FlatLayer1D layer1D, VCPair vcPW, int iVp, int iVTI, 
			int [] reflectorIndex, int [] psIndex, int iWave, int iR, int iJ) {
		this(frame, layer1D, vcPW, iVp, iVTI, 0, reflectorIndex, psIndex, iWave, 0.1, iR, iJ);
	}

	public RayTracerFlat3D(FrameI frame, FlatLayer1D layer1D, VCPair vcPW, int iVp, int iVTI, int iApprox, 
			int [] reflectorIndex, int [] psIndex, int iWave, double tol, int iR, int iJ) {
		selfConstructor(frame, layer1D, vcPW, iVp, iVTI, iApprox, 
				reflectorIndex, psIndex, iWave, tol, iR, iJ);
	}
	public void selfConstructor(FrameI frame, FlatLayer1D layer1D, VCPair vcPW, int iVp, int iVTI, int iApprox, 
			int [] reflectorIndex, int [] psIndex, int iWave, double tol, int iR, int iJ) {
		_frame 		= frame;
		_layer1D 	= layer1D;
		_vcPW		= vcPW;
		_iVp 		= iVp;
		_iVTI 		= iVTI;
		_iApprox 	= iApprox;
		_reflectorIndex 	= reflectorIndex;
		_psIndex 	= psIndex;
		_iWave 		= iWave;
		_tol 		= tol;
		_iR 		= iR;
		_iJ 		= iJ;
		if(_iVp>10) System.exit(0);
		setConfiguration(layer1D, vcPW);
		//System.out.println(statusToString());
		//System.out.println(vcPW.toString());
		//System.out.println("dip="+ layer1D.toString());
	}
	
	//make sure datum of both input are equal and zero
	public void setConfiguration(FlatLayer1D layer1D, VCPair vcPW0) {
		if(_layer1D==null) 	_layer1D 	= layer1D;
		if(_vcPW==null) 	_vcPW		= vcPW0;

		setConfiguration();
	}
	public void setConfiguration() {
		_rayPar 	= new double[_vcPW.getTotalNumOfReceivers()];
		_rayPoint 	= new double[6];
		if(_iVp==1) 		_calT 	= _vcPW.getCalPT();
		else if(_iVp==2)	_calT 	= _vcPW.getCalST();
		else if(_iVp==3)	_calT 	= _vcPW.getCalSV();
		setZero(_calT);
		
		if(_iVTI>0) { _vel = new SeismicVel(_iMethod, _layer1D, _iVTI, _iVp, _iApprox); } 
		if(_iVTI>0&&_iMethod==1) 	{  _pgs = new double[1][5];  setZero(_pgs); }
		if(_iR==1) 	{ _rayPaths = new RayPaths(_vcPW.getNEvents()); }
		if(_iJ==1) 	{ 
			_jac = new double[_layer1D.getNumOfBoundaries()-1][5]; 
			setZero(_jac);
			_L = new double[_layer1D.getNumOfBoundaries()-1]; 
			setZero(_L);
			int k = 0;
			for(int i=0; i<_vcPW.getNEvents(); i++) {
				k += _vcPW.getFlag(i);
			}
			//int numOfVariables	= _layer1D.getNumOfBoundaries()*5;
			int numOfVariables	= (_layer1D.getNumOfBoundaries()-1)*6+_vcPW.getNEvents()*4;
			_A = new double[k][numOfVariables];
			setZero(_A);
		}
		//System.out.println(Arrays.toString(_layer1D.getVp()));
	}

	public void printCal() { System.out.println(Arrays.toString(_calT)); }
	private void setZero(double [][] A) 			{ for(int i=0; i<A.length; i++) setZero(A[i]); }
	private void setZero(double [] A) 				{ for(int i=0; i<A.length; i++) A[i] = 0.0; }
	public void setIApproximation( int v) 			{ if(_vel!=null) _vel.setIApproximation(v); }
	public void setIMethod( int v) 					{ _iMethod = v; }
	public void setIT0( int v) 						{ _iT0 = v; }
	public void setIT( int v) 						{ _iT = v; }
	public void setIR( int v) 						{ _iR = v; }
	public void setIJ( int v) 						{ _iJ = v; }
	public SeismicVel getSeismicVel() 				{ return _vel; }
	public void setLayer(FlatLayer1D layer1D) 		{ _layer1D = layer1D; }
	public void setPair(VCPair vcPW) 				{ setConfiguration(null, vcPW); }

	public void setReflectorIndex(int [] reflectorIndex) 		{ _reflectorIndex = reflectorIndex; }
	public void setPsIndex(int [] psIndex) 			{ _psIndex = psIndex; }
	public void setIWave(int iWave) 				{ _iWave = iWave;}
	public void setIVp(int iVp) 					{ _iVp = iVp;}
	public void setIVTI(int iVTI) 					{ _iVTI = iVTI; }
	public void setTolerance(double tol) 			{ _tol = tol; }

	public int getNumOfBoundaries() 				{ return _layer1D.getNumOfBoundaries(); }
	public double getTDepth(int index) 				{ return _layer1D.getLayer(index).getTopDepth(); }
	public RayPaths getRayPaths() 					{ return _rayPaths; }
	public double [] getRayPar()					{ return _rayPar; }
	public double [] getRayPoint()					{ return _rayPoint; }
	public double [][] getA()						{ return _A; }

	public int [] getReflectorIndex() 				{ return _reflectorIndex; }
	public int [] getPsIndex() 						{ return _psIndex; }
	public double getTolerance() 					{ return _tol; }
	public int  getIWave() 							{ return _iWave;}
	public int  getIVp() 							{ return _iVp;}
	public int  getIVTI() 							{ return _iVTI;}

	public int  getIMethod() 						{ return _iMethod;}
	public int  getIT0() 							{ return _iT0;}
	public int  getIT() 							{ return _iT;}
	public int  getIR() 							{ return _iR;}
	public int  getIApproximation() 				{ if(_vel!=null) return _vel.getIApproximation(); return 0;}

	public FlatLayer1D getFlatLayer1D() 			{ return _layer1D; }
	public Layer2D getLayer() 						{ return _layer1D.toLayer2D(-100, 100); }
	public VCPair getVCPair() 						{ return _vcPW; }

	public void start() { startRayTracing(1, null); }
	public void start(int runNo) { startRayTracing(runNo, null); }
	public void startRayTracing(int runNo, int [] iLayer) {
		//System.out.println("_vcPW.getNEvents()="+_vcPW.getNEvents());
		if(runNo==2) {
			System.out.println(statusToString());
			if(_iR==1) 	{ _rayPaths = new RayPaths(_vcPW.getNEvents()); }
		}
		for(int i=0, k=0; i<_vcPW.getNEvents(); i++) {
			int ke 		= _layer1D.getLayerTopIndex(_vcPW.getED(i));
			Point3D pe 	= new Point3D(_vcPW.getEE(i), _vcPW.getEN(i), _vcPW.getED(i));
			//System.out.println("D="+_vcPW.getED(i)+" ke="+ke);
			for(int j=0; j<_vcPW.getNumOfReceivers(i); j++, k++) {
				int kr 		= _layer1D.getLayerTopIndex(_vcPW.getRD(k));
				Point3D pr 	= new Point3D(_vcPW.getRE(k), _vcPW.getRN(k), _vcPW.getRD(k));
				if(_iJ==1) 	{ setZero(_jac); setZero(_L); }

				_shotLayerIndex = ke;
				int sameLayerCase = -1;
				boolean eOnBoundary = _layer1D.getLayer(ke).onTDepth(pe.getD());
				boolean rOnBoundary = _layer1D.getLayer(kr).onTDepth(pr.getD());
				if(ke==kr)  {
					if(eOnBoundary&&rOnBoundary) sameLayerCase = 1;
					else sameLayerCase = 2;
				} else if(kr-ke==1) {
					if(rOnBoundary) sameLayerCase = 11;
				} else if(ke-kr==1) {
					if(eOnBoundary) {sameLayerCase = 21; _shotLayerIndex--; }
				} else { }

				int [][] rayMode 	= null;
				double [] hPrime 	= null;
				double [] vPrime 	= null;
				double [] xPrime 	= null;
				double [] zPrime 	= null;
				int [] index 		= null;
				double d0 			= pe.surfaceDistance(pr);
				
				if(sameLayerCase>0 && _reflectorIndex==null) { //direct arrivals in the same layer
					double theSin 	= pe.surfaceDistance(pr)/pe.distance(pr);
					double gAngle 	= Math.asin(theSin); //[-90, 90]
					double gVel 	= _layer1D.getLayer(_shotLayerIndex).getVs();
					if(_iVp==1) gVel = _layer1D.getLayer(_shotLayerIndex).getVp();
					//System.out.println("gVel="+gVel);
					
					if(_iMethod==1) 	{  _pgs = new double[1][5];  setZero(_pgs); }
					
					if(_iVTI>0) {
						hPrime 	= new double[] { Math.abs(pe.getD()-pr.getD()) };
						vPrime	= new double[] { gVel };
						index 	= new int[] { _shotLayerIndex };
						xPrime 	= new double[hPrime.length];
						zPrime	= new double[] { pr.getD() };
						
						double maxV = 0.0;
						if(_iVp==1) {
							double a = _layer1D.getLayer(_shotLayerIndex).getEpsilon();
							maxV = _layer1D.getLayer(_shotLayerIndex).getVp()*Math.sqrt(1.0+2.0*a);
						}else if(_iVp==2) {
							double a = _layer1D.getLayer(_shotLayerIndex).getGamma();
							maxV = _layer1D.getLayer(_shotLayerIndex).getVs()*Math.sqrt(1.0+2.0*a);
						} else { maxV = _layer1D.getLayer(_shotLayerIndex).getVs(); }
						//gVel 			= _vel.getTableValue(2, _shotLayerIndex, 1, gAngle);
						//_rayPar[k] 	= _vel.getTableValue(3, _shotLayerIndex, 1, gAngle);
						
						double [] t = new double[2];
						int errorID = 0;
						if(_iVTI>0) errorID =  bisection(d0, hPrime, xPrime, vPrime, null, index, t, maxV);
						else 		errorID =  bisection(d0, hPrime, xPrime, vPrime, null, null, t, maxV);
						if(errorID>=0) { _rayPar[k] 	= t[0]; 		_calT[k] 	= t[1]; 
						} else { 		
							_rayPar[k] 	= 0.9999/maxV; 	
							//_rayPar[k] 	= _INVALID_D;
							if(_iMethod==1) _vel.setRayPara(_rayPar[k], 1, _vel.getIVp(), hPrime, index, _pgs);
							gVel = _vel.getLayerGroupVel(_iVTI, _iVp, _shotLayerIndex, 3.1415926/2.0);
							_calT[k] 	= pe.distance(pr)/gVel; 		
						}	
					} else {
						_rayPar[k] 		= theSin/gVel;
						_calT[k] 		= pe.distance(pr)/gVel;
					}

					double [] headWave = null;
					if(_iWave==2) headWave = headWaveSingleLayerSearch(sameLayerCase, d0, _shotLayerIndex, pe, pr, 0, 0, null);
					
					if(headWave!=null&&headWave[0]<_calT[k]) {
						//System.out.println("calT="+_calT[k]+" "+Arrays.toString(headWave));
						//System.out.println("calT="+_calT[k]+" "+" d="+pe.distance(pr)+" gVel="+gVel);
						//System.out.println("inTheSameLayer= "+ sameLayerCase+" shotLayerIndex= "+ _shotLayerIndex+
						//		" ke= "+ ke+" kr= "+ kr);
						//System.out.println(pe.toString()+"\n"+pr.toString());
						
						_rayPar[k] 	= headWave[3];
						_calT[k] 	= headWaveSingleLayer(_iVTI, _iVp, (int)headWave[1], (int)headWave[2], headWave[5], _rayPar[k], headWave[4], d0, 
								pe, pr, _iR, _iJ, _jac);
					} else {
						if(_iJ==1&&_iVTI>0) {
							for(int ii=0; ii<_layer1D.getNumOfBoundaries()-1; ii++) {
								if(ii!=_shotLayerIndex) continue;
								double pAngle 		= getTableValue(0, 0, ii, 1, gAngle);
								gVel 				= getTableValue(2, 0, ii, 1, gAngle);
								double pVel 		= _vel.getLayerPhaseVel(_iVTI, _iVp, ii, pAngle);
								double len 			= pe.distance(pr);

								double [] derivative= _layer1D.getLayer(ii).sensitivity(_iVp, pVel, pAngle, gVel, len);
								for(int jj=0; jj<derivative.length; jj++) _jac[ii][jj] = derivative[jj];
							}
						}
						if(_iR==1) {
							RayPath rayPath = new RayPath(2);
							rayPath.setPoint(0, pe.getE(), pe.getN(), pe.getD());
							rayPath.setPoint(1, pr.getE(), pr.getN(), pr.getD());
							_rayPaths.add(rayPath);
						}
					}
				} else {
					rayMode = calRayCode(_iVp, pe, pr, _reflectorIndex, _psIndex);
					//for(int jj=0; jj<rayMode.length; jj++) System.out.println(Arrays.toString(rayMode[jj]));
					hPrime 	= getHPrime(_layer1D, rayMode);
					vPrime	= getVPrime(_layer1D, rayMode);
					//System.out.println(Arrays.toString(vPrime));
					//index 	= rayMode[1];
					xPrime 	= new double[hPrime.length];
					zPrime 	= getZPrime(_layer1D, rayMode);
					
					zPrime[zPrime.length-1] = pr.getD();
					hPrime[0] = Math.abs(zPrime[0]-pe.getD());
					int lastLayerIndex = hPrime.length-1;
					if(lastLayerIndex>0)  hPrime[lastLayerIndex] = Math.abs(zPrime[lastLayerIndex]-zPrime[lastLayerIndex-1]);
					//System.out.println("index= "+ Arrays.toString(hPrime));	
					
					double maxV = getMaxV(_layer1D, rayMode);
					//System.out.println("maxV= "+ maxV);

					if(_iVTI>0&&_iMethod==1) 	{  _pgs = new double[hPrime.length][5];  setZero(_pgs); }
					tracing(k, ke, kr, pe, pr, d0, hPrime, xPrime, vPrime, rayMode[0], rayMode[1], zPrime, maxV);
				}

				
				//System.out.println("k="+k+" rayPar="+_rayPar[k]);
				if(_iVTI>0&&_iJ==1) {
					int m = 0;
					for(int ii=0; ii<_jac.length; ii++) {
						for(int jj=0; jj<_jac[ii].length; jj++) { _A[k][m++] = 1.0e6*_jac[ii][jj]; }
					}
					
					//thickness L
					for(int ii=0; ii<_layer1D.getNumOfBoundaries()-1; ii++) { _A[k][m++] = _L[ii]; }
					
					double pAngle 	= getTableValue(0, 0, _shotLayerIndex, 3, _rayPar[k]);
					double pVel 	= getLayerPhaseVel(_iVTI, _vel.getIVp(), 0, _shotLayerIndex, pAngle);
					double [] D 	= sensitivityOfHypocenter(_rayPar[k], pAngle, pVel, pe, pr);
					if(D!=null) {
						m += i*4;
						_A[k][m++] = D[0];_A[k][m++] = D[1];_A[k][m++] = D[2];_A[k][m++] = D[3];
					}
				} 
			} //receiver
			//System.out.println(timeToString());
		} //event
		//System.out.println(timeToString());
		//if(_iR==1) { System.out.println(_rayPaths.toString()); }
	}
	
	private void tracing(int k, int ke, int kr, Point3D pe, Point3D pr, double d0, 
			double [] hPrime, double [] xPrime, double [] vPrime, int [] pIndex, int [] index, double [] zPrime, double maxV) {
		
		double [] t = new double[2];
		int errorID = 0;
		if(_iVTI>0) errorID =  bisection(d0, hPrime, xPrime, vPrime, pIndex, index, t, maxV);
		else 		errorID =  bisection(d0, hPrime, xPrime, vPrime, null, null, t, maxV);

		//System.out.println("\nerrorID: "+errorID);

		if(errorID<0) { 
			_rayPar[k] 	= _INVALID_D;
			_calT[k] 	= _INVALID_D; 
			if(_reflectorIndex==null&&_iWave==2) { 
				double [] calT1 = headWaveSearch(-1, d0, ke, kr, pe, pr, 0, 0, null);
				int mIndex = minIndex(calT1);
				//System.out.println("calT="+_calT[k]+" "+Arrays.toString(calT1));
				if(calT1[mIndex]>0.0&&calT1[mIndex]<10.0) {
					_calT[k] = calT1[mIndex];
					calT1 = headWaveSearch(mIndex, d0, ke, kr, pe, pr, _iR, _iJ, _jac);
					_rayPar[k] = calT1[0];
				} 
				//System.out.println(pe.toString()+"\n"+pr.toString());
			}
			//if(_calT[k]==_INVALID_D) System.out.println("\nRay traceing failed: "+ke+", "+kr);
		} else {
			_rayPar[k] 	= t[0];
			_calT[k] 	= t[1];

			boolean iC = true;

			if(_reflectorIndex==null&&_iWave==2) { 
				double [] calT1 = headWaveSearch(-1, d0, ke, kr, pe, pr, 0, 0, null);
				int mIndex = minIndex(calT1);
				if(t[1]>calT1[mIndex]) {
					iC = false;
					_calT[k] = calT1[mIndex];

					//System.out.println("t="+t[1]+" t2="+_calT[k]+" calT1= "+ Arrays.toString(calT1));
					calT1 = headWaveSearch(mIndex, d0, ke, kr, pe, pr, _iR, _iJ, _jac);
					_rayPar[k] = calT1[0];
				} 
			} 
			
			if(iC) {
				traceOneStraightRay(-1, 0, 0, pe, pr, hPrime, xPrime, zPrime, vPrime, pIndex, index,
						_iT, k, _calT, _rayPar[k], _iR, _iJ, _jac);
			}
		}		
	}
	
	private double [] sensitivityOfHypocenter(double rayPar, double pAngle, double pVel, Point3D pe, Point3D pr) {
		if(_iVTI==0||_iJ==0||rayPar==_INVALID_D) return null;

		double diffX 	= pe.getE()-pr.getE();
		double diffY 	= pe.getN()-pr.getN();
		double d 		= Math.sqrt(diffX*diffX+diffY*diffY);
		double theSin 	= diffY/d;
		double theCos 	= diffX/d;

		int idz = 0;
		if(pr.getD()>pe.getD()) idz = 1; //going down
		else idz = -1;

		double dx = rayPar*theCos; 					//sensitivity on event x
		double dy = rayPar*theSin; 					//sensitivity on event y
		double dz = -idz*Math.cos(pAngle)/pVel; 	//sensitivity on event z
		double dt = 1; 
		
		return new double[]{dx, dy, dz, dt};
	}
	public double [] getHPrime(FlatLayer1D layer1D, int [][] rayCodes) {
		int [] lIndex = rayCodes[1];
		double [] h = new double[lIndex.length];
		for(int i=0; i<h.length; i++) h[i] = layer1D.getLayer(lIndex[i]).getThickness();
		return h;		
	}
	public double [] getVPrime(FlatLayer1D layer1D, int [][] rayCodes) {
		int [] pIndex = rayCodes[0];
		int [] lIndex = rayCodes[1];
		double [] v = new double[lIndex.length];
		for(int i=0; i<v.length; i++) {
			if(pIndex[i]==1) v[i] = layer1D.getLayer(lIndex[i]).getVp();
			else  v[i] = layer1D.getLayer(lIndex[i]).getVs();
		} 
		return v;		
	}
	public double getMaxV(FlatLayer1D layer1D, int [][] rayCodes) {
		int [] pIndex = rayCodes[0];
		int [] lIndex = rayCodes[1];
		double [] v = new double[lIndex.length];
		
		if(_iVTI==0) {
			v =  getVPrime(layer1D, rayCodes);
		} else {
			for(int i=0; i<v.length; i++) {
				if(pIndex[i]==1)  {
					v[i] = layer1D.getLayer(lIndex[i]).getVp()*Math.sqrt(1.0+2.0*layer1D.getLayer(lIndex[i]).getEpsilon());
				}else if(pIndex[i]==2) {
					v[i] = layer1D.getLayer(lIndex[i]).getVs()*Math.sqrt(1.0+2.0*layer1D.getLayer(lIndex[i]).getGamma());
				} else  {
//					double a = layer1D.getLayer(lIndex[i]).getVp()/layer1D.getLayer(lIndex[i]).getVs();
//					double eta = a*a*(layer1D.getLayer(lIndex[i]).getEpsilon()-layer1D.getLayer(lIndex[i]).getDelta());
//					if(eta<=0.0) v[i] = layer1D.getLayer(lIndex[i]).getVs();
//					else v[i] = layer1D.getLayer(lIndex[i]).getVs()*(1.0+0.25*eta);
					v[i] = layer1D.getLayer(lIndex[i]).getVs();
				}
			} 
		}
		return max(v);		
	}
	public double [] getZPrime(FlatLayer1D layer1D, int [][] rayCodes) {
		int [] bIndex = rayCodes[2];
		double [] dp = new double[bIndex.length];
		for(int i=0; i<dp.length; i++)  dp[i] = layer1D.getLayer(bIndex[i]).getTopDepth(); 
		return dp;		
	}

	public int bisection(double d0, double [] hPrime, double [] xPrime, double [] vPrime, int [] pIndex,  int [] index, double [] time, double maxV) {
		double x1 = 0.0;	
		double x2 = 1.0/maxV;
		double dx = 0.0;
		double f = 0.0;
		double xMid = 0.5*(x1+x2);
		double rtb = 0.0;

		time[0] = 0;
		time[1] = epiDistance(time[0], hPrime, vPrime, xPrime, pIndex, index); 
		double sum = 0; for(int i=0; i<xPrime.length; i++) sum += xPrime[i];
		f = d0-sum;
		//System.exit(1);
		//System.out.println("\nd0: "+d0+" sum="+ sum + " f="+f+" x="+ time[0] + " t="+time[1]);
		if(Math.abs(f) <= _tol ) { return 1; } 

		time[0] = xMid;
		time[1] = epiDistance(time[0], hPrime, vPrime, xPrime, pIndex, index); 
		sum = 0; for(int i=0; i<xPrime.length; i++) sum += xPrime[i];
		f = d0-sum;
		//System.exit(1);
		//System.out.println("\nd0: "+d0+" sum="+ sum + " f="+f);
		if(Math.abs(f) <= _tol ) { return 1; } 
		else if(f>0.0) { 	x1 = xMid; } 
		else { 				x1 = 0.0;  }
		dx = x2-x1; 
		rtb = x1;

		for(int JMAX=100, j=1; j<JMAX; j++) {
			dx *= 0.5;
			xMid = rtb + dx;
			time[0] = xMid;
			time[1] = epiDistance(time[0], hPrime, vPrime, xPrime, pIndex, index); 
			sum = 0; for(int i=0; i<xPrime.length; i++) sum += xPrime[i];
			f = d0-sum; 
			//System.out.println("\nd0: "+d0+" sum="+ sum + " f="+f+ " xMid="+xMid);

			if( Math.abs(f) <= _tol ) 	{ return 1; } 
			else if( f>0.0) 			{ rtb = xMid; } 
			else { }
		} 

		return -1;
	}
	
	private double epiDistance(double p, double [] hPrime, double [] vPrime, double [] xPrime, int [] pIndex, int [] index) {
		double gAngle = 0.0;
		double gVel = 0.0;
		double t = 0;
		double x = 0;
		//iso
		if(index==null) {
			for(int i=0; i<hPrime.length; i++ ) {
				x = hPrime[i]*p*vPrime[i]/(Math.sqrt(1.0-vPrime[i]*vPrime[i]*p*p));
				xPrime[i] = x;
				t += hPrime[i]/(vPrime[i] * Math.sqrt(1.0-vPrime[i]*vPrime[i]*p*p));
			}
			return t;
		}
		//aniso
		if(_pgs!=null) {
			if(pIndex!=null) {
				_vel.setRayPara(p, 1, pIndex, hPrime, index, _pgs);
			} else {
				_vel.setRayPara(p, 1, _vel.getIVp(), hPrime, index, _pgs);
			}
		}
		for(int i=0; i<index.length; i++) {
			gAngle 		= getTableValue(1, i, index[i], 3, p);
			gVel 		= getTableValue(2, i, index[i], 3, p);
			x 			= hPrime[i]*Math.tan(gAngle);
			xPrime[i] 	= x;

			t 			+= hPrime[i]/(gVel*Math.cos(gAngle));
		}
		return t;
	}
	private double getTableValue(int to, int lIndex, int iBnry, int from, double x) {
		if(_pgs!=null) {
			return _pgs[lIndex][to];
		} else {
			return _vel.getTableValue(to, iBnry, from, x);
		}
	}
	private double getLayerPhaseVel(int iVTI, int iVp, int lIndex, int iBnry, double pAngle) {
		if(_pgs!=null) {
			return _pgs[lIndex][3];
		} else {
			return _vel.getLayerPhaseVel(_iVTI, _iVp, iBnry, pAngle);
		}
	}
	private double getLayerGroupVel(int iVTI, int iVp, int lIndex, int iBnry, double pAngle) {
		if(_pgs!=null) {
			return _pgs[lIndex][2];
		} else {
			return _vel.getLayerGroupVel(_iVTI, _iVp, iBnry, pAngle);
		}
	}

	private int [] isLayerPenatrated(int iBnry, int [] index) {
		int k = 0;
		for(int i=0; i<index.length; i++) {
			if(iBnry==index[i]) k++;
		}

		if(k==0) return null;

		int [] selected = new int[k];
		k = 0;
		for(int i=0; i<index.length; i++) {
			if(iBnry==index[i]) selected[k++] = i;
		}

		return selected;
	}
	// receivers and sources must be in different layers
	private int traceOneStraightRay(double d0, int iL, int iB, Point3D pe, Point3D pr,
			double [] hPrime, double [] xPrime, double [] zPrime, double [] vPrime, int [] pIndex, int [] index,
			int iT, int iPair, double [] calT, double p, int iR, int iJ, double [][] jac) {

		double t = 0.0;
		if(p==0) {
			for(int i=0; i<hPrime.length; i++ ) {
				xPrime[i] = 0;
				t += hPrime[i]/vPrime[i];
			}
		} else {
			if(_iVTI>0) t += epiDistance(p, hPrime, vPrime, xPrime, pIndex, index);
			else 		t += epiDistance(p, hPrime, vPrime, xPrime, null, null);
		}
		if(iT==1) {calT[iPair] = t; }
		if(_iDebug==1) { System.out.println("xPrime= "+ Arrays.toString(xPrime)); }
		if(_iDebug==1) {System.out.println("t="+t+" p="+p);}

		if(iJ==1&&_iVTI>0) {
			for(int i=0; i<_layer1D.getNumOfBoundaries()-1; i++) {
				int [] layerIndex = isLayerPenatrated(i, index);
				if(layerIndex==null) { continue; }
				if(layerIndex.length==1) { // direct wave
					int lIndex 		= layerIndex[0];
					int iBnry  		= i;
					double pAngle 	= getTableValue(0, lIndex, iBnry, 3, p);
					double pVel 	= getLayerPhaseVel(_iVTI, _iVp, lIndex, iBnry, pAngle);
					double gVel 	= getTableValue(2, lIndex, iBnry, 3, p);
					double len 		= Math.sqrt(hPrime[lIndex]*hPrime[lIndex]+xPrime[lIndex]*xPrime[lIndex]);
					double [] derivative= _layer1D.getLayer(iBnry).sensitivity(_iVp, pVel, pAngle, gVel, len);
					for(int j=0; j<derivative.length; j++) jac[i][j] = derivative[j];
				} else if(layerIndex.length==2) { // reflected wave
					int lIndex 		= layerIndex[0];
					int lIndex2 	= layerIndex[1];
					int iBnry  		= i;
					double pAngle 	= getTableValue(0, lIndex, iBnry, 3, p);
					double pVel 	= getLayerPhaseVel(_iVTI, _iVp, lIndex, iBnry, pAngle);
					double gVel 	= getTableValue(2, lIndex, iBnry, 3, p);
					double len 		= Math.sqrt(hPrime[lIndex]*hPrime[lIndex]+xPrime[lIndex]*xPrime[lIndex]);
					len 			+= Math.sqrt(hPrime[lIndex2]*hPrime[lIndex2]+xPrime[lIndex2]*xPrime[lIndex2]);
					double [] derivative= _layer1D.getLayer(iBnry).sensitivity(_iVp, pVel, pAngle, gVel, len);
					for(int j=0; j<derivative.length; j++) jac[i][j] = derivative[j];
				} else { //head wave
					
					
				}
			}
		}

		if(iR==1) {
			if(d0<0) {
				int nPoints = hPrime.length+1;
				RayPath rayPath = calRayPath(nPoints, pe.getD(), pr.getD(), xPrime, zPrime);
				rayPath.rotate(pe.getE(), pe.getN(), pr.getE(), pr.getN());
				rayPath.transfer(pe.getE(), pe.getN());
				_rayPaths.add(rayPath);	
			} else {
				double sum = 0.0; for(int j=0; j<xPrime.length; j++) sum += xPrime[j];
				double [] dHeadWave = new double[]{d0-sum, _layer1D.getLayer(iB).getTopDepth()};
				int nPoints = hPrime.length+1;
				RayPath rayPath = calRayPath(nPoints, pe.getD(), pr.getD(), xPrime, zPrime);
				RayPath rayPath1 = rayPath.insert(dHeadWave);
				rayPath1.rotate(pe.getE(), pe.getN(), pr.getE(), pr.getN());
				rayPath1.transfer(pe.getE(), pe.getN());
				_rayPaths.add(rayPath1);
			}
		}

		return 0;
	}

	private RayPath calRayPath(int nPoints, double sz, double rz, double [] xPrime, double [] zPrime) {
		RayPath rayPath 	= new RayPath(nPoints);
		rayPath.setPoint(0, 0, 0, sz);
		double sum = 0;
		for(int i=1, j=1; i<nPoints; i++) { 
			j = i-1;
			sum += xPrime[j];
			if(i==nPoints-1) rayPath.setPoint(i, sum, 0.0, rz);
			else rayPath.setPoint(i, sum, 0.0, zPrime[j]);
		}
		return rayPath;
	}
	// receivers and sources must be in different layers
	private double [] headWaveSearch(int iLayer, double d0, int ke, int kr, Point3D pe, Point3D pr, 
			int iR, int iJacobian, double [][] jac) {
		double [] calT1 = new double[_layer1D.getNumOfBoundaries()];
		for(int i=0; i<calT1.length; i++) { calT1[i] = 1.0e10; }
		int [] psIndex = new int[2*_layer1D.getNumOfBoundaries()];
		for(int i=0; i<psIndex.length; i++) { psIndex[i] = _iVp; }
		
		for(int i=1; i<_layer1D.getNumOfBoundaries()-1; i++) {
			if(iLayer<0) { }
			else if(iLayer!=i) continue;
			else {}

			if((i-ke)*(i-kr)<=0) {
				if(kr>ke) {
					if(i==ke && !_layer1D.getLayer(ke).onTDepth(pe.getD())) { }
					else continue;
				} else {
					if(i==kr && !_layer1D.getLayer(kr).onTDepth(pr.getD())) { }
					else continue;
				}
			}
			
			int [][] rayMode = calRayCode(_iVp, pe, pr, new int[]{i}, psIndex);

			//System.out.println("\n\ni="+i);
			//for(int jj=0; jj<rayMode.length; jj++) System.out.println(Arrays.toString(rayMode[jj]));
			double [] hPrime 	= getHPrime(_layer1D, rayMode);
			double [] vPrime	= getVPrime(_layer1D, rayMode);
			int [] 	  index 	= rayMode[1];
			double [] xPrime 	= new double[hPrime.length];
			double [] zPrime 	= getZPrime(_layer1D, rayMode);
			zPrime[zPrime.length-1] = pr.getD();

			if(_iVTI>0&&_iMethod==1) 	{  _pgs = new double[hPrime.length][5];  setZero(_pgs); }
			
			hPrime[0] = Math.abs(zPrime[0]-pe.getD());
			int lastLayerIndex = hPrime.length-1;
			if(lastLayerIndex>0)  hPrime[lastLayerIndex] = Math.abs(zPrime[lastLayerIndex]-zPrime[lastLayerIndex-1]);

			if(_iDebug==1) {
				System.out.println("hPrime= "+ Arrays.toString(hPrime));
				System.out.println("index= "+ Arrays.toString(index));			
				System.out.println("zPrime= "+ Arrays.toString(zPrime));
				System.out.println("vPrime= "+ Arrays.toString(vPrime));
			}

			double groupVel = 0;
			int iL = 0;
			if(i<=ke&&i<=kr) {
				iL = i-1;
				groupVel = getGroupVel(_iVp, 0, i-1, -99999.0);
			} else {
				iL = i;
				groupVel = getGroupVel(_iVp, 0, i, -99999.0);
			}
			if(groupVel<max(vPrime)) continue;
			//System.out.println("i="+i+" groupVel="+groupVel+" max="+max(vPrime));
			double p = 1.0/groupVel;
			if(iLayer>=0) {calT1[0] = p; }
			traceOneStraightRay(d0, iL, i, pe, pr,
					hPrime, xPrime, zPrime, vPrime, null, index,
					1, i, calT1, p, iR, iJacobian, jac);
			//System.out.println("\nd0="+d0+" x="+xPrime[xPrime.length-1]+" calT="+calT1[i]+
			//		" c1="+(xPrime[xPrime.length-1]/groupVel)+" c2="+(d0/groupVel));
		}
		return calT1;
	}

	public double getPhaseVel(int iVp, int iVTI, int iBnry, double p) {
		if(iVTI==0) {
			if(iVp==1) return _layer1D.getLayer(iBnry).getVp();
			else return _layer1D.getLayer(iBnry).getVs();
		} else {
			if(p==-99999.0) {
				return _vel.getLayerPhaseVel(iVTI, iVp, iBnry, 3.1415926/2.0);
			} else {
				double[] pgs = new double[5];
				_vel.setRayPara(p, iVTI, iVp, _layer1D.getLayer(iBnry).getThickness(), iBnry, pgs);
				return pgs[3];
			}
		}
	}
	public double getGroupVel(int iVp, int iVTI, int iBnry, double p) {
		if(iVTI==0) {
			if(iVp==1) return _layer1D.getLayer(iBnry).getVp();
			else return _layer1D.getLayer(iBnry).getVs();
		} else {
			if(p==-99999.0) {
				return _vel.getLayerGroupVel(iVTI, iVp, iBnry, 3.1415926/2.0);
			} else {
				double[] pgs = new double[5];
				_vel.setRayPara(p, iVTI, iVp, _layer1D.getLayer(iBnry).getThickness(), iBnry, pgs);
				return pgs[2];
			}
		}
	}
	// receivers and sources must be in the same layers
	private double [] headWaveSingleLayerSearch(int sameLayerCase, double d0, int ke, Point3D pe, Point3D pr,
			int iR, int iJacobian, double [][] jac) {
		if(sameLayerCase!=2) return null;
		if(ke<=0) return null;
		if(ke>=_layer1D.getNumOfBoundaries()-2) return null;

		double phaseVel0 = getPhaseVel(_iVp, _iVTI, ke, -99999.0);

		double p1 			= 0.0;
		double phaseVel1 	= 0.0;
		double groupVel10 	= 0.0;
		double cal1 		= 0.0;

		double p2 			= 0.0;
		double phaseVel2 	= 0.0;
		double groupVel20 	= 0.0;
		double cal2 		= 0.0;

		//upper
		phaseVel1 = getPhaseVel(_iVp, _iVTI, ke-1, -99999.0);
		if(phaseVel0<phaseVel1) { 
			p1 = 0.99999/phaseVel1;
			groupVel10 = getGroupVel(_iVp, _iVTI, ke, p1);
			cal1 = headWaveSingleLayer(_iVTI, _iVp, ke-1, ke, _layer1D.getLayer(ke).getTopDepth(), p1, groupVel10, d0, pe, pr, iR, iJacobian, jac);
		}

		//lower
		phaseVel2 = getPhaseVel(_iVp, _iVTI, ke+1, -99999.0);
		if(phaseVel0<phaseVel2) { 
			p2 = 0.99999/phaseVel2;
			groupVel20 = getGroupVel(_iVp, _iVTI, ke, p2);
			cal2 = headWaveSingleLayer(_iVTI, _iVp, ke+1, ke, _layer1D.getLayer(ke).getBottomDepth(), p2, groupVel20, d0, pe, pr, iR, iJacobian, jac);
		}
		
		//System.out.println("phaseVel0="+phaseVel0+" phaseVel1="+phaseVel1+" phaseVe2="+phaseVel2+
		//		" groupVel10="+groupVel10+" groupVel20="+groupVel20+" cal1="+cal1+" cal2="+cal2);
		
		if(cal1>0.0) { //upper
			if(cal2>0.0) {
				if(cal1<cal2) return new double[] {cal1, ke-1, ke, p1, groupVel10, _layer1D.getLayer(ke).getTopDepth()};
				else return new double[] {cal2, ke+1, ke, p2, groupVel20, _layer1D.getLayer(ke).getBottomDepth()};
			} else {
				return new double[] {cal1, ke-1, ke, p1, groupVel10, _layer1D.getLayer(ke).getTopDepth()};
			}
		} else { //lower
			if(cal2>0.0) {
				return new double[] {cal2, ke+1, ke, p2, groupVel20, _layer1D.getLayer(ke).getBottomDepth()};
			} else {
				return null;
			}
		}
		
		
	}
	private double headWaveSingleLayer(int iVTI, int iVp, int iHeadLayer, int iB, double dp, double p, double groupVel, double d0, Point3D pe, Point3D pr,
			int iR, int iJacobian, double [][] jac) {
		
		double he = Math.abs(dp-pe.getD());
		double hr = Math.abs(dp-pr.getD());
		
		double v = _layer1D.getLayer(iB).getVs();
		if(iVp==1) v = _layer1D.getLayer(iB).getVp();
		double [] hPrime 	= new double[] { he };
		double [] vPrime	= new double[] { v };
		int [] index 		= new int[] { iB };
		double [] xPrime 	= new double[hPrime.length];
		double [] zPrime	= new double[] { dp };
		if(iVTI==0) epiDistance(p, hPrime, vPrime, xPrime, null, null);
		else epiDistance(p, hPrime, vPrime, xPrime, null, index);
		double xe = xPrime[0];
		
		hPrime[0] = hr;
		if(iVTI==0) epiDistance(p, hPrime, vPrime, xPrime, null, null);
		else epiDistance(p, hPrime, vPrime, xPrime, null, index);
		double xr = xPrime[0];

		if(_iJ==1&&_iVTI>0) {
			double pAngle 		= 0.5*3.1415926;
			double pVel 		= _vel.getLayerPhaseVel(_iVTI, _iVp, iHeadLayer, pAngle);
			double gVel 		= _vel.getLayerGroupVel(_iVTI, _iVp, iHeadLayer, pAngle);
			double len 			= d0-(xe+xr);

			double [] derivative= _layer1D.getLayer(iHeadLayer).sensitivity(_iVp, pVel, pAngle, gVel, len);
			for(int j=0; j<derivative.length; j++) jac[iHeadLayer][j] = derivative[j];

			int ke 		= _layer1D.getLayerTopIndex(pe.getD());

			double[] pgs = new double[5];
			_vel.setRayPara(p, _iVTI, _iVp, he+hr, ke, pgs);
			len 			= xe+xr;

			derivative= _layer1D.getLayer(ke).sensitivity(_iVp, pgs[3], pgs[0], pgs[2], len);
			for(int j=0; j<derivative.length; j++) jac[ke][j] = derivative[j];
		}

		if(iR==1) {
			int nPoints = 4;
			RayPath rayPath = calRayPath(nPoints, pe.getD(), pr.getD(), 
					new double[]{xe, d0-xr-xe, xr}, new double[]{dp, dp, pr.getD()});
			rayPath.rotate(pe.getE(), pe.getN(), pr.getE(), pr.getN());
			rayPath.transfer(pe.getE(), pe.getN());
			//System.out.println("x1="+xe+" x2="+xr+" x3="+(xe+xr)+" x4="+d0);
			//System.out.println(rayPath.toString());
			_rayPaths.add(rayPath);
		}

		//System.out.println("xe="+xe+" xr="+xr+" x3="+(xe+xr)+" d0="+d0+" groupVel="+groupVel+" p="+(1/p));
		if(d0>=(xe+xr)) {
			double len = Math.sqrt(xe*xe+he*he);
			len += Math.sqrt(xr*xr+hr*hr);
			return len/groupVel+(d0-xe-xr)*p;
		}
		return 99999.0;
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

	private double clipDepth(double minThickness, double z) {
		for(int i=0; i<_layer1D.getNumOfBoundaries(); i++) {
			if(Math.abs(z-_layer1D.getLayer(i).getTopDepth())<=minThickness) {
				return _layer1D.getLayer(i).getTopDepth();
			}
		}
		return z;
	}
	private int [][] calRayCode(int iVp, Point3D pe, Point3D pr, int [] reflectorsIndex, int [] psIndex) {
		double minThickness = 1.0e-3;
		double ez 	= clipDepth(minThickness, pe.getD());
		double rz 	= clipDepth(minThickness, pr.getD());
		if(reflectorsIndex==null)  return calRayCode(iVp, ez, rz);

		int [][][] rayM 	= new int[reflectorsIndex.length+1][][];
		boolean [] filled 	= new boolean[reflectorsIndex.length+1];
		int [][] rayN 		= null;
		for(int i=0; i<rayM.length; i++) { rayM[i] = rayN; filled[i] = false; }
		int nSegment = 0;

		ez 		= pe.getD();
		rz 		= _layer1D.getLayer(reflectorsIndex[0]).getTopDepth();
		iVp 	= psIndex[0];
		rayN 	= calRayCode(iVp, ez, rz);
		if(rayN!=null) {
			rayM[0] 	= rayN;
			filled[0] 	= true;
			nSegment 	+= rayM[0][0].length;
			//for(int jj=0; jj<rayM[0].length; jj++) System.out.println("0 " + Arrays.toString(rayM[0][jj]));
		}

		for(int i=1; i<rayM.length-1; i++) {
			ez 		= _layer1D.getLayer(reflectorsIndex[i-1]).getTopDepth();
			rz 		= _layer1D.getLayer(reflectorsIndex[i]).getTopDepth();
			iVp 	= psIndex[i];
			rayN 	= calRayCode(iVp, ez, rz);
			if(rayN!=null) {
				rayM[i] 	= rayN;
				filled[i] 	= true;
				nSegment 	+= rayM[i][0].length;
				//for(int jj=0; jj<rayM[i].length; jj++) System.out.println(i+" "+Arrays.toString(rayM[i][jj]));
			}
		}

		ez 		= _layer1D.getLayer(reflectorsIndex[reflectorsIndex.length-1]).getTopDepth();
		rz 		= pr.getD();
		iVp 	= psIndex[psIndex.length-1];
		rayN 	= calRayCode(iVp, ez, rz);
		if(rayN!=null) {
			rayM[rayM.length-1] 	= rayN;
			filled[rayM.length-1] 	= true;
			nSegment 				+= rayM[rayM.length-1][0].length;
			//for(int jj=0; jj<rayM[rayM.length-1].length; jj++) System.out.println("99 "+Arrays.toString(rayM[rayM.length-1][jj]));
		}

		int [] pIndex 	= new int[nSegment];
		int [] lIndex 	= new int[nSegment];
		int [] bIndex 	= new int[nSegment];
		for(int i=0, k=0; i<rayM.length; i++) {
			if(filled[i]) {
				for(int j=0; j<rayM[i][0].length; j++, k++) {
					pIndex[k] = rayM[i][0][j];
					lIndex[k] = rayM[i][1][j];
					bIndex[k] = rayM[i][2][j];
				}
			}
		}

		return new int [][] {pIndex, lIndex, bIndex};
	}

	private int [][] calRayCode(int iVp, double ez, double rz) {
		int ke 	= _layer1D.getLayerTopIndex(ez);
		int kr 	= _layer1D.getLayerTopIndex(rz);
		int k1 = ke;
		int k2 = kr;

		int [] pIndex 	= null;
		int [] lIndex 	= null;
		int [] bIndex 	= null;
		if(ke<kr) {
			if(_layer1D.getLayer(kr).onTDepth(rz)) k2--;
			int nSegment = Math.abs(k2-k1)+1;
			//System.out.println("nSegment="+nSegment+" ke="+ke+" kr="+kr);
			pIndex 	= new int[nSegment];
			lIndex 	= new int[nSegment];
			bIndex 	= new int[nSegment];
			for(int i=k1, j=0; i<=k2; i++, j++) pIndex[j] = iVp;
			for(int i=k1, j=0; i<=k2; i++, j++) lIndex[j] = i;
			for(int i=k1, j=0; i<=k2; i++, j++) bIndex[j] = i+1;		
		} else if(ke==kr) {
			if(rz>ez) {
				int nSegment = Math.abs(k2-k1)+1;
				pIndex 	= new int[nSegment];
				lIndex 	= new int[nSegment];
				bIndex 	= new int[nSegment];
				for(int i=k1, j=0; i<=k2; i++, j++) pIndex[j] = iVp;
				for(int i=k1, j=0; i<=k2; i++, j++) lIndex[j] = i;
				for(int i=k1, j=0; i<=k2; i++, j++) bIndex[j] = i+1;
			} else if(rz<ez) {
				int nSegment = Math.abs(k2-k1)+1;
				pIndex 	= new int[nSegment];
				lIndex 	= new int[nSegment];
				bIndex 	= new int[nSegment];
				for(int i=k1, j=0; i<=k2; i++, j++) pIndex[j] = iVp;
				for(int i=k1, j=0; i<=k2; i++, j++) lIndex[j] = i;
				for(int i=k1, j=0; i<=k2; i++, j++) bIndex[j] = i;
			} else return null;
		} else {
			if(_layer1D.getLayer(ke).onTDepth(ez))  k1--; 
			int nSegment = Math.abs(k2-k1)+1;
			pIndex 	= new int[nSegment];
			lIndex 	= new int[nSegment];
			bIndex 	= new int[nSegment];
			for(int i=k1, j=0; i>=k2; i--, j++) pIndex[j] = iVp;
			for(int i=k1, j=0; i>=k2; i--, j++) lIndex[j] = i;
			for(int i=k1, j=0; i>=k2; i--, j++) bIndex[j] = i;
		}

		return new int [][] {pIndex, lIndex, bIndex};
	}

	public String timeToString()	{
		String a;
		String b = new String(" ");

		int k = 0;
		int n = 0;
		double radToDegree = 180.0/3.1415926;
		for(int i=0; i<_vcPW.getNEvents(); i++) {
			for(int j=0; j<_vcPW.getFlag(i); j++, k++) {
				//if(_calT[k]==_INVALID_D) {
				n++;
				a = String.format( "%10.2f, %10.2f, %10.2f, %10.2f, %10.2f, %10.2f, %10.6f, %10.6f, %10.10f\n", 
						_vcPW.getEE(i), _vcPW.getEN(i), _vcPW.getED(i), _vcPW.getRE(k), _vcPW.getRN(k), _vcPW.getRD(k), 
						_vcPW.getObsPT(k), _calT[k], _rayPar[k] );
				b = new String(b.concat(a));
				//}
			}
		}
		return b;
	}

	public String statusToString()	{
		return String.format( "iMethod=%d, iVTI=%d, iVp=%d, iApprox=%d iWave=%d tol=%f iT=%d, iR=%d, iJ=%d\n", 
				_iMethod, _iVTI, _iVp, _iApprox, _iWave, _tol, _iT, _iR, _iJ);
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

}
