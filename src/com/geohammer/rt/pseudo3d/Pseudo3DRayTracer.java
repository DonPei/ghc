package com.geohammer.rt.pseudo3d;

import java.util.ArrayList;

import com.geohammer.core.acquisition.VCPair;
import com.geohammer.core.planarmodel.DipLayer1D;
import com.geohammer.core.planarmodel.FlatLayer1D;
import com.geohammer.rt.RayPath;

public class Pseudo3DRayTracer extends RayTracerFlat3D {

	public DipLayer1D 	_dipLayer1D 	= null;
	//public VCPair 		_vcPW0			= null;

	public int 			_iDimension 	= 1;
	public int 			_runNo 			= 1;
	public double 		_datum 			= 0;
	
	public double 		_top 			= 0;
	public double 		_dh 			= 10000;
	private double 		_azimuth 		= -1;
	private double 		_dip 			= 0;
	private double 		_centerN 		= 0;
	private double 		_centerE		= 0;

	public DipLayer1D getDipLayer1D() 		{ return _dipLayer1D; }
	//public VCPair getVCPair0() 				{ return _vcPW0; }
	
	public Pseudo3DRayTracer() { super(); }
//	public Pseudo3DRayTracer(FrameI frame, FlatLayer1D layer1D, VCPair vcPW, int iVp, int iVTI, 
//			int [] reflectorIndex, int [] psIndex, int iWave, int iR, int iJ, int iDimension) {
//		this(frame, layer1D, vcPW, iVp, iVTI, 0, reflectorIndex, psIndex, iWave, 0.1, iR, iJ, iDimension);
//	}
//	public Pseudo3DRayTracer(FrameI frame, FlatLayer1D layer1D, VCPair vcPW,
//			int iVp, int iVTI, int iApprox, int[] reflectorIndex, int [] psIndex, int iWave,
//			double tol, int iR, int iJ, int iDimension) {
//		super(frame, layer1D, vcPW,
//				iVp, iVTI, iApprox, reflectorIndex, psIndex, iWave,
//				tol, iR, iJ);
//		_iDimension	= 1;
//	}
	public Pseudo3DRayTracer(FrameI frame, DipLayer1D dipLayer1D, VCPair vcPW,
			int iVp, int iVTI, int iApprox, int[] reflectorIndex, int [] psIndex, int iWave,
			double tol, int iR, int iJ, int iDimension) {
		_dipLayer1D		= dipLayer1D;
		//_vcPW			= vcPW;
		//_vcPW0			= vcPW0;
		_iDimension		= iDimension;
		
		_azimuth 		= dipLayer1D.getAzimuth();
		_dip 			= dipLayer1D.getDip();
		//_centerN 		= dipLayer1D.getAnchorN();
		//_centerE 		= dipLayer1D.getAnchorE();
		//System.out.println("azimuth="+ _azimuth+" dip="+ _dip
		//		+" centerN="+ _centerN+" centerE="+ _centerE+" iDimension="+ _iDimension);
		//System.out.println("dip="+ dlayer1D.toString());
		//System.out.println("name2="+dlayer1D.getLayer(0).getLayerName());
		//System.out.println(layer1D.toString());

		_datum = dipLayer1D.getLayer(0).getZ0();
		//System.out.println("datum="+ _datum);
		//VCPair vcPW 	= vcPW0.copy();
		vcPW.shift(-_datum);
		//System.out.println(vcPW.toString());
//		if(_dipLayer1D.isFlat()) {
//		} else {
//			_dipLayer1D.fromFlatToTilt(vcPW.eN, vcPW.eE, vcPW.eD);
//			_dipLayer1D.fromFlatToTilt(vcPW.rN, vcPW.rE, vcPW.rD);
//		}

		FlatLayer1D layer1D = dipLayer1D.toFlatLayer1D(-_datum);
		_top = layer1D.getLayer(0).getZ0();
		//layer1D.getLayer(0).setZ0(_top-_dh);
		layer1D.getLayer(0).setTopDepth(_top-_dh);
		layer1D.getLayer(0).updateH();
		//System.out.println("datum="+ _datum+" top="+ _top+" dh="+ _dh);
		//System.out.println(layer1D.toString());
		
		selfConstructor(frame, layer1D, vcPW, iVp, iVTI, iApprox, reflectorIndex, psIndex, iWave, tol, iR, iJ);
	}

	public void start() 			{ startRayTracing(_dip==0, 1); }
	public void start(int runNo) 	{ 
		//System.out.println("runNo="+runNo+" isFlat="+_dipLayer1D.isFlat());
		startRayTracing(_dip==0, runNo); 
	}
	private void startRayTracing(boolean isFlat, int runNo) {
		_runNo = runNo;
		//System.out.println("runNo="+runNo+" isFlat="+isFlat);
		startRayTracing(1, null);
		//if(isFlat) return;
		
//		for(int i=0; i<_vcPW0.getNEvents(); i++) {
//			_vcPW0.orgT[i] = _vcPW.orgT[i];
//		}
//		int fp = _vcPW0.getTotalNumOfReceivers();
//		for(int i=0; i<fp; i++) {
//			_vcPW0.calPT[i] = _vcPW.calPT[i];
//			_vcPW0.calST[i] = _vcPW.calST[i];
//			_vcPW0.calSV[i] = _vcPW.calSV[i];
//		}
		//System.out.println(_vcPWOrg.toString(false));
		//System.out.println(_vcPW0.toString(false));
		
		if(_iR==1) {
			
			ArrayList<RayPath> rays = _rayPaths.getRayPathList();
			for(int i=0; i<rays.size(); i++) {
				RayPath rayPath = rays.get(i);
				double [] E = rayPath.getE();
				double [] N = rayPath.getN();
				double [] D = rayPath.getD();
				if(_dip<0.01&&_dip>-0.01) {
				} else if(_dip>89.99||_dip<-89.9) {
				} else {
					rotate(_dip, N, D);
				}
				if(_iDimension==2) continue;
				
				if(_azimuth>89.99 && _azimuth<90.01) {
					double [] tmp = E;
					rayPath.setE(rayPath.getN());
					rayPath.setN(tmp);
				} else if(_azimuth>179.99 || _azimuth<0.01) {

				} else {
					rotate(-_azimuth, E, N);
				}
				
//				for(int j=0; j<E.length; j++) {
//					E[j] += _centerE;
//					N[j] += _centerN;
//				}
			}
			for(int i=0; i<rays.size(); i++) {
				double [] z = rays.get(i).getD();
				for(int j=0; j<z.length; j++) z[j] += _datum;
			}
			if(_rayPaths.getNumOfTraces()>0) {
				//System.out.println(_rayPaths.toString());
				_rayPaths.updateHeaders();
				//new AngleCoverage(_rayPaths);
			}
			if(_frame!=null) { _frame.setRayPath(_iVp-1, _rayPaths); }
		} 
	}
	
	public void rotate(double azimuth, double [] e, double [] n) {
		double ang = azimuth*Math.PI/180.0;
		double theSin = Math.sin(ang);
		double theCos = Math.cos(ang);
		double[][] M = new double[][] {{theCos, -theSin},{theSin, theCos}}; 
		double ax = 0.0;
		double ay = 0.0;
		for(int i=0, k=0; i<e.length; i++) {
			ax = M[0][0]*e[i]+M[0][1]*n[i]; //xprime
			ay = M[1][0]*e[i]+M[1][1]*n[i];
			e[i] = ax;
			n[i] = ay;
		}
	}
}
