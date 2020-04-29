package com.geohammer.vc.run;

import com.geohammer.core.acquisition.VCPair;
import com.geohammer.core.planarmodel.DipLayer1D;

public class EffectiveVelocityAlone {
	int 		_iVTI 		= 0;  //=0 iso 1=vti
	int 		_iWave 		= 1;  //=0 all =1 VP =2 SH =3 SV =4 VP+SH =5 VP+SH+SV
	
	DipLayer1D 	_dipLayer1D = null;
	VCPair 		_vcPW 		= null;
	//double [][] _dataRange	= new double [5][3];
	
	double 		_minRms 	= 0;
	float [] 	_rms 		= null;
	
	double [] 		_min	= null;
	
	double [] 	_d0 		= null;
	double [] 	_takeOffAngle= null;
	
	boolean 	_cancel 	= false;

	public EffectiveVelocityAlone(int iVTI, int iWave, DipLayer1D dipLayer1D, VCPair vcPW) {
		_iVTI 		= iVTI; 
		_iWave 		= iWave; 
		_dipLayer1D	= dipLayer1D;
		_min 		= new double[5];
//		for(int j=0; j<3; j++) _dataRange[0][j] = dipLayer1D.getLayer(j+1).getVp();
//		for(int j=0; j<3; j++) _dataRange[1][j] = dipLayer1D.getLayer(j+1).getVs();
//		for(int j=0; j<3; j++) _dataRange[2][j] = dipLayer1D.getLayer(j+1).getDelta();
//		for(int j=0; j<3; j++) _dataRange[3][j] = dipLayer1D.getLayer(j+1).getEpsilon();
//		for(int j=0; j<3; j++) _dataRange[4][j] = dipLayer1D.getLayer(j+1).getGamma();
		
		_vcPW 		= vcPW;
		_d0 		= new double[_vcPW.getTotalNumOfReceivers()];
		for(int i=0, k=0; i<_vcPW.getNEvents(); i++) {
			for(int j=0; j<_vcPW.getFlag(i); j++, k++) {
				_d0[k] = _vcPW.distance(i, k);
			}
		}
		//System.out.println("d0="+Arrays.toString(_d0));
		
	}
	public float [] getRms() 						{ return _rms; }
	public double  getMinRms() 						{ return _minRms; }
	public DipLayer1D getDipLayer1D() 				{ return _dipLayer1D; }
	public void setCancel(boolean cancel) 			{ _cancel = cancel; }
	
	public double start()	{
		if(_iVTI==0) return startISO();
		else return startISO();
	}
	public double startISO()	{
		double vp = 0.0;
		double vpMin = _dipLayer1D.getLayer(1).getVp();
		double vpMax = _dipLayer1D.getLayer(2).getVp();
		double vpD = _dipLayer1D.getLayer(3).getVp();
		
		int np = (int)((vpMax-vpMin)/vpD+1.1);
		
		double vs = 0.0;
		double vsMin = _dipLayer1D.getLayer(1).getVs();
		double vsMax = _dipLayer1D.getLayer(2).getVs();
		double vsD = _dipLayer1D.getLayer(3).getVs();
		
		int ns = (int)((vsMax-vsMin)/vsD+1.1);
		
		double currRms = 0.0;
		
		_minRms = 1.0e20;
		_rms = new float[np*ns];
		for(int i=0, k=0; i<np; i++) {
			vp = vpMin+i*vpD;
			for(int j=0; j<ns; j++, k++) {
				if(_cancel) return _minRms;
				vs = vsMin+j*vsD;
				rayTracing(vp, vs);
				currRms = calRms();
				_rms[k] = (float)currRms;
				//System.out.println("currRms="+currRms+" minRms="+_minRms);
				if(currRms<_minRms) {
					_minRms = currRms;
					_min[0] = vp;
					_min[1] = vs;
				}
			}
		}
		
		//System.out.println("minRms="+_minRms);
		for(int i=0; i<1; i++) {
		//for(int i=0; i<_dipLayer1D.getNumOfBoundaries(); i++) {
			_dipLayer1D.getLayer(i).setVp(_min[0]);
			_dipLayer1D.getLayer(i).setVs(_min[1]);
			_dipLayer1D.getLayer(i).setDelta(_min[2]);
			_dipLayer1D.getLayer(i).setEpsilon(_min[3]);
			_dipLayer1D.getLayer(i).setGamma(_min[4]);
		}
		return _minRms;
	}

	public void rayTracing(double vp, double vs)	{
		double t = 0.0;
		for(int i=0, k=0; i<_vcPW.getNEvents(); i++) {
			for(int j=0; j<_vcPW.getFlag(i); j++, k++) {
				t = _d0[k]/vp;
				_vcPW.setData(k, _vcPW.getCalPT(), t);
				t = _d0[k]/vs;
				_vcPW.setData(k, _vcPW.getCalST(), t);
			}
		}
		applyT0();
	}
	
	public double calRms() {
		VCPair vcPW 	= _vcPW;
		if(_iWave==1) {
			return vcPW.getRMS(1, 0, 0);
		} else if(_iWave==2) {
			return vcPW.getRMS(0, 1, 0);
		} else if(_iWave==3) {
			return vcPW.getRMS(0, 0, 1);
		} else if(_iWave==4) {
			return vcPW.getRMS(1, 1, 0);
		} else if(_iWave==5) {
			return vcPW.getRMS(1, 1, 1);
		} else return 0;
	}
	
	private void applyT0() {
		if(_iWave==1) {
			_vcPW.estimateT0(1, 0, 0);
			_vcPW.applyT0(1, 0, 0);
		} else if(_iWave==2) {
			_vcPW.estimateT0(0, 1, 0);
			_vcPW.applyT0(0, 1, 0);
		} else if(_iWave==3) {
			_vcPW.estimateT0(0, 0, 1);
			_vcPW.applyT0(0, 0, 1);
		} else if(_iWave==4) {
			_vcPW.estimateT0(1, 1, 0);
			_vcPW.applyT0(1, 1, 0);
		} else if(_iWave==5) {
			_vcPW.estimateT0(1, 1, 1);
			_vcPW.applyT0(1, 1, 1);
		}
	}
	
	
}
