package com.geohammer.core;

import java.util.Arrays;

import org.ucdm.core.acquisition.VCPair;


public class MSEvent  extends PointAbstract {
	public int _iUnit 					= 2;		// distance unit in m (1), ft (2), km(3)
	public int _hour 					= 0; 			
	public int _min 					= 0; 			
	public int _sec 					= 0; 		
	public double _sampleInterval 		= 0.00025; // second 
	public String _fracWellName 		= "unknown";
	public String _stageName 			= "unknown";

	public double [] _pPick 			= null; // num of sample points
	public double [] _svPick 			= null;
	public double [] _shPick 			= null;
	public double [] _angle 			= null;
	public int [] _sensorNo 			= null;

	public double _perfP 				= 0; 
	public double _perfSh 				= 0; 
	public double _perfSv 				= 0; 
	public double _perfA 				= 0; 

	public ObsWell [] _obsWells 		= null;
	public SeismicTraceWell []	_stWells= null;

	public double _INVALID_D 			= -99999.0;

	private boolean _enableMti 			= true;
	
	public MSEvent(int id, String label, double x, double y, double z) {
		super(id, label, x, y, z);
	}
	public MSEvent(int id, String label, double x, double y, double z, int hour, int min, int sec, 
			double sampleInterval, int nPicks, int nWells, int iUnit) {
		super(id, label, x, y, z);
		_iUnit 			= iUnit;
		_hour 			= hour;
		_min 			= min;
		_sec 			= sec;
		_sampleInterval = sampleInterval;
		allocatePicks(nPicks);
		allocateWells(nWells);
	}
	
	public MSEvent(boolean obsPick, VCPair vcPW) {
		super(0, "vcPW", vcPW.getEN(0), vcPW.getEE(0), vcPW.getED(0));
		int nWells 			= vcPW.getNumOfWells();
		int nSensors		= vcPW.getFlag(0)/nWells;
		double sampleInterval = 0.00025;
		allocatePicks(vcPW.getFlag(0));
		ObsWell [] obsWells 		= new ObsWell[nWells];
		SeismicTraceWell [] stWells	= new SeismicTraceWell[nWells];
		//System.out.println("nWells="+nWells+" nSensors="+nSensors);

		for(int i=0, iReceiver=0; i<nWells; i++) {
			double x = vcPW.getRN(iReceiver);
			double y = vcPW.getRE(iReceiver);
			double z = vcPW.getRD(iReceiver);
			obsWells[i] = new ObsWell(nSensors, i, "Obs_Well"+i, x, y, 0.0, 0.0, "zyx", "3D");
			stWells[i] 	= new SeismicTraceWell(nSensors, i);
			for(int j=0; j<nSensors; j++) {
				x = vcPW.getRN(iReceiver);
				y = vcPW.getRE(iReceiver);
				z = vcPW.getRD(iReceiver);
				if(obsPick) {
					_pPick[iReceiver] 		= vcPW.getObsPT(iReceiver);
					_svPick[iReceiver] 		= vcPW.getObsST(iReceiver);
					_shPick[iReceiver] 		= vcPW.getObsSV(iReceiver);
				} else {
					_pPick[iReceiver] 		= vcPW.getCalPT(iReceiver);
					_svPick[iReceiver] 		= vcPW.getCalST(iReceiver);
					_shPick[iReceiver] 		= vcPW.getCalSV(iReceiver);
				}
				_angle[iReceiver] 		= 0.0;
				_sensorNo[iReceiver] 	= iReceiver;
				//System.out.println(iReceiver+ " _pPick="+_pPick[iReceiver]);
				
				Sensor sensor = new Sensor(j, "Sensor"+j, x, y, z, 0.0, 0.0, 0.0);
				SeismicTraceSensor stSensor = new SeismicTraceSensor(i, j);
				SeismicTraceComponent [] comps = new SeismicTraceComponent[3];
				for(int k=0; k<3; k++) {
					comps[k] = new SeismicTraceComponent(i, j, k, sampleInterval, new float[5]);
					comps[k].setPpick(_pPick[iReceiver]);
					comps[k].setSVpick(_svPick[iReceiver]);
					comps[k].setSHpick(_shPick[iReceiver]);
				}
				stSensor.setComp(comps);
				obsWells[i].setSensor(j, sensor);
				stWells[i].setSensor(j, stSensor);
				
				iReceiver++;
			}
			//System.out.println(obsWells[i].toString());
		}
		_obsWells = obsWells;
		_stWells = stWells;
	}

	public MSEvent copy() {
		MSEvent msEvent = new MSEvent(getId(), getLabel(), getX(), getY(), getZ(), _hour, _min, _sec, 
				_sampleInterval, _pPick.length, _obsWells.length, _iUnit);
		for(int i=0; i<_pPick.length; i++) {
			msEvent._pPick[i] 		= _pPick[i];
			msEvent._svPick[i] 		= _svPick[i];
			msEvent._shPick[i] 		= _shPick[i];
			msEvent._angle[i] 		= _angle[i];
			msEvent._sensorNo[i] 	= _sensorNo[i];
		}
		for(int i=0; i<_obsWells.length; i++) {
			msEvent._obsWells[i] 	= _obsWells[i].clone();
			msEvent._stWells[i] 	= _stWells[i].clone();
		}

		msEvent._perfP 				= _perfP;
		msEvent._perfSh 			= _perfSh;
		msEvent._perfSv 			= _perfSv;
		msEvent._perfA 				= _perfA;

		return msEvent;
	}
	private void allocatePicks(int nPicks) {
		_pPick = new double[nPicks];
		_svPick = new double[nPicks];
		_shPick = new double[nPicks];
		_angle = new double[nPicks];
		_sensorNo = new int[nPicks];
	}

	public void allocateWells(int nWells) {
		_obsWells = new ObsWell[nWells];
		_stWells  = new SeismicTraceWell[nWells];
	}	

	//suggest to do a gc
	public void gcSeismicTraceData() { 
		for(int i=0; i<_stWells.length; i++) _stWells[i] = null;
	}
	public boolean getEnableMti() 		{ return _enableMti; }
	public int getUnit() 				{ return _iUnit; }
	public String getFracWellName() 	{ return _fracWellName; }
	public String getStageName() 		{ return _stageName; }
	public double getSampleInterval() 	{ return _sampleInterval; }
	public int getHour() 				{ return _hour; }
	public int getMin() 				{ return _min; }
	public int getSec() 				{ return _sec; }

	public double getPerfP() 			{ return _perfP; }
	public double getPerfSh() 			{ return _perfSh; }
	public double getPerfSv() 			{ return _perfSv; }
	public double getPerfA() 			{ return _perfA; }

	public int getNumOfPicks() 			{ if(_pPick==null) return 0; return _pPick.length; }
	public double [] getPpicks() 		{ return _pPick; }
	public double getPpicks(int index) 	{ return _pPick[index]; }
	public double [] getSVpicks() 		{ return _svPick; }
	public double getSVpicks(int index) { return _svPick[index]; }
	public double [] getSHpicks() 		{ return _shPick; }
	public double getSHpicks(int index) { return _shPick[index]; }
	public double [] getAngles() 		{ return _angle; }
	public double getAngles(int index) 	{ return _angle[index]; }
	public int getPpickIndex(int index) { return (int)(_pPick[index]/_sampleInterval); }
	public int getSHpickIndex(int index) { return (int)(_shPick[index]/_sampleInterval); }
	public int getSVpickIndex(int index) { return (int)(_svPick[index]/_sampleInterval); }
	
	public int getNumOfObsWells() 					{ if(_obsWells==null) return 0; return _obsWells.length; }
	public ObsWell [] getObsWells() 				{ return _obsWells; }
	public ObsWell getObsWells(int index) 			{ return _obsWells[index]; }
	public int getNumOfSTWells() 					{ if(_stWells==null) return 0; return _stWells.length; }
	public SeismicTraceWell [] getSTWells() 		{ return _stWells; }
	public SeismicTraceWell getSTWells(int index) 	{ return _stWells[index]; }

	public int getNumOfReceivers(int wellId) {
		int nPairs = 0;
		for(int i=0; i<getNumOfSTWells(); i++) { 
			if(wellId>=0&&i!=wellId) continue;
			nPairs += getObsWells(i).getNumOfSensors(); 
		}
		return nPairs;
	}
	public void setSampleInterval(double sampleInterval) 		{ _sampleInterval = sampleInterval; }
	public void setFracWellName(String fracWellName) 			{ _fracWellName = fracWellName; }
	public void setStageName(String stageName) 					{ _stageName = stageName; }
	public void setPpicks(int index, double v) 					{ _pPick[index] = v; }
	public void setSHpicks(int index, double v) 				{ _shPick[index] = v; }
	public void setSVpicks(int index, double v) 				{ _svPick[index] = v; }
	
	public void setEnableMti(boolean enableMti) 				{ _enableMti = enableMti; }
	public void setUnit(int v ) 								{ _iUnit = v; }
	public void setObsWells(ObsWell [] wells) 					{ _obsWells = wells; }
	public void setObsWells(int index, ObsWell well) 			{ _obsWells[index] = well; }
	public void setSTWells(SeismicTraceWell [] wells) 			{ _stWells = wells; }
	public void setSTWells(int index, SeismicTraceWell well) 	{ _stWells[index] = well; }	

	public void pickInt() {
		pickInt(_pPick);
		pickInt(_shPick);
		pickInt(_svPick);
	}
	public void pickInt(double [] v) {
		for(int i=0; i<v.length; i++) {
			if(v[i]>10.0) v[i] = (int)v[i];
			else v[i] = 10.0;
		}
	}
	public void pickAdd(double a) {
		pickAdd(_pPick, a);
		pickAdd(_shPick, a);
		pickAdd(_svPick, a);
	}
	public void pickAdd(double [] v, double a) {
		for(int i=0; i<v.length; i++) {
			if(v[i]>10.0) v[i] += a;
		}
	}
	public void pickMultiply(double a) {
		pickMultiply(_pPick, a);
		pickMultiply(_shPick, a);
		pickMultiply(_svPick, a);
	}
	public void pickMultiply(double [] v, double a) {
		for(int i=0; i<v.length; i++) {
			if(v[i]>10.0) v[i] *= a;
		}
	}
	public void pickAssignToTrace() {
		int k = 0;
		for(int i=0; i<getNumOfSTWells(); i++) {
			SeismicTraceWell	stWell 	= getSTWells(i);
			for(int j=0; j<stWell.getNumOfSensors(); j++) {
				SeismicTraceSensor stSensor = stWell.getSensor(j);
				for(int jj=0; jj<stSensor.getNumOfComps(); jj++) {
					stSensor.getComp(jj).setPpick(_pPick[k]);
					stSensor.getComp(jj).setSHpick(_shPick[k]);
					stSensor.getComp(jj).setSVpick(_svPick[k]);
				}
				k++; 
			}
		}
	}
	public void pickAssignFromTrace() {
		int k = 0;
		for(int i=0; i<getNumOfSTWells(); i++) {
			SeismicTraceWell	stWell 	= getSTWells(i);
			for(int j=0; j<stWell.getNumOfSensors(); j++) {
				SeismicTraceSensor stSensor = stWell.getSensor(j);
				for(int jj=0; jj<stSensor.getNumOfComps(); jj++) {
					_pPick[k] = stSensor.getComp(jj).getPpick();
					_shPick[k] = stSensor.getComp(jj).getSHpick();
					_svPick[k] = stSensor.getComp(jj).getSVpick();
				}
				k++; 
			}
		}
	}
	
//	public void setPicks(int format) {
//		int k = 0;
//		for(int i=0; i<getNumOfSTWells(); i++) {
//			SeismicTraceWell	stWell 	= getSTWells(i);
//			for(int j=0; j<stWell.getNumOfSensors(); j++) {
//				SeismicTraceSensor stSensor = stWell.getSensor(j);
//				for(int jj=0; jj<stSensor.getNumOfComps(); jj++) {
//					if(format==1) {
//						stSensor.getComp(jj).setPpick(_pPick[k]);
//						stSensor.getComp(jj).setSHpick(_shPick[k]);
//						stSensor.getComp(jj).setSVpick(_svPick[k]);
//					} else if(format==2) {
//						if(_pPick[k]<=10) 	{ stSensor.getComp(jj).setPpick(_INVALID_D); } 
//						else 				{ stSensor.getComp(jj).setPpick(_pPick[k]*_sampleInterval); }
//
//						if(_shPick[k]<=10) 	{ stSensor.getComp(jj).setSHpick(_INVALID_D); } 
//						else 				{ stSensor.getComp(jj).setSHpick(_shPick[k]*_sampleInterval); }
//
//						if(_svPick[k]<=10) 	{ stSensor.getComp(jj).setSVpick(_INVALID_D); } 
//						else 				{ stSensor.getComp(jj).setSVpick(_svPick[k]*_sampleInterval); }
//					} else if(format==1) {
//						stSensor.getComp(jj).setPpick(_pPick[k]);
//						stSensor.getComp(jj).setSHpick(_shPick[k]);
//						stSensor.getComp(jj).setSVpick(_svPick[k]);
//					} 
//				}
//				k++; 
//			}
//		}
//	}

	public void updateTime(int wellId, int iType, boolean useObs, VCPair vcPW, double sampleInterval) {
		double [] calPT = vcPW.getCalPT();
		double [] calST = vcPW.getCalST();
		double [] calSV = vcPW.getCalSV();
		
		if(useObs) {
			calPT = vcPW.getObsPT();
			calST = vcPW.getObsST();
			calSV = vcPW.getObsSV();
		}

		int k = 0;
		for(int i=0; i<getNumOfObsWells(); i++) {
			if(wellId>=0&&i!=wellId) continue;
			ObsWell well 	= getObsWells(i);
			for(int j=0; j<well.getNumOfSensors(); j++) {
				_pPick[k] = calPT[k]/sampleInterval;
				_shPick[k] = calST[k]/sampleInterval;
				_svPick[k] = calSV[k]/sampleInterval;

				if(iType==0) {
					
				} else {
					if(_angle!=null) { _angle[k] = calSV[k]/sampleInterval;  }
				}
				k++;
			}
		}
	}
	
	public VCPair genVCPair(int wellId, int iType) {
		int fp = 0;
		for(int i=0; i<getNumOfObsWells(); i++) {
			ObsWell well 	= getObsWells(i);
			fp +=well.getNumOfSensors();
		}
		if(wellId>=0) fp = getNumOfReceivers(wellId);

		VCPair vcPW = new VCPair(1, fp, _iUnit);
		vcPW.setData(0, vcPW.getEN(), getX());
		vcPW.setData(0, vcPW.getEE(), getY());
		vcPW.setData(0, vcPW.getED(), getZ());

		double t0 = _perfP;
		t0 = t0>_perfSh ? t0:_perfSh;
		t0 = t0>_perfSv ? t0:_perfSv;

		if(t0<=10) 	{ vcPW.setData(0, vcPW.getOrgT(), 0.0); vcPW.setData(0, vcPW.getOrgW(), 1);} 
		else 		{ vcPW.setData(0, vcPW.getOrgT(), t0*_sampleInterval); vcPW.setData(0, vcPW.getOrgW(), 0);}

		int k = 0;
		for(int i=0; i<getNumOfObsWells(); i++) {
			if(wellId>=0&&i!=wellId) continue;
			ObsWell well 	= getObsWells(i);
			for(int j=0; j<well.getNumOfSensors(); j++) {
				Sensor 	sensor 	= well.getSensor(j);
				vcPW.setData(k, vcPW.getRN(), sensor.getX());
				vcPW.setData(k, vcPW.getRE(), sensor.getY());
				vcPW.setData(k, vcPW.getRD(), sensor.getZ());

				vcPW.setData(k, vcPW.getObsPT(), vcPW._INVALID_D);
				vcPW.setData(k, vcPW.getObsST(), vcPW._INVALID_D);
				vcPW.setData(k, vcPW.getObsSV(), vcPW._INVALID_D);

				if(_pPick!=null) { if(_pPick[k]>10.0) vcPW.setData(k, vcPW.getObsPT(), _pPick[k]*_sampleInterval); }

				if(_shPick!=null) { if(_shPick[k]>10.0) vcPW.setData(k, vcPW.getObsST(), _shPick[k]*_sampleInterval); }

				if(iType==0) {
					if(_svPick!=null) { if(_svPick[k]>10.0) vcPW.setData(k, vcPW.getObsSV(), _svPick[k]*_sampleInterval); }
				} else {
					if(_angle!=null) { vcPW.setData(k, vcPW.getObsSV(), _angle[k]);  }
				}
				k++;
			}
			vcPW.setData(0, vcPW.getFlag(), k);
		}

		return vcPW;
	}
	
	public double [] getSampleAtPickZcomp(int ip, int isv, int ish) {
		double [] p = getSampleAtPick(ip, 1);
		double [] sv = getSampleAtPick(isv, 1);
		double [] sh = getSampleAtPick(ish, 1);
		if(p==null) return null;

		if(sv==null) {
			if(sh==null) {
			} else {
				return append(p, sh);
			}
		} else {
			if(sh==null) {
				return append(p, sv);
			} else {
				return append(append(p, sv), sh);
			}
		}
		return p;
	}
	private double [] append(double[] A, double[] B) {
		if(A==null) return B;
		if(B==null) return A;

		int m = A.length;
		int n = B.length;

		double [] C = new double[m+n];

		for(int i=0; i<m; i++) {
			C[i] = A[i];
		}
		for(int i=0, j=m; i<n; i++, j++) {
			C[j] = B[i];
		}
		return C;
	}
	
	public double [] getSampleAtPpick(int iComp) 	{ return getSampleAtPick(1, iComp); 	}
	public double [] getSampleAtSVpick(int iComp) 	{ return getSampleAtPick(2, iComp); 	}
	public double [] getSampleAtSHpick(int iComp) 	{ return getSampleAtPick(3, iComp); 	}

	// iPick=1 p 2=sv 3=sh
	// iComp=1 zComp 2=yComp 3=xComp 
	private double [] getSampleAtPick(int iPick, int iComp) {  
		if(iPick==0) return null;
		if(iComp==0) return null;

		int m = getNumOfPicks();
		double [] D = new double[m];

		for(int i=0, k1=0; i<getNumOfSTWells(); i++) {
			SeismicTraceWell well = getSTWells(i);
			for(int j=0; j<well.getNumOfSensors(); j++) {
				SeismicTraceSensor  sensor = well.getSensor(j);
				if(iPick==1) {
					if(iComp==1) { 
						D[k1++] = sensor.getCompZ().getSampleAtPpick();
					} else if(iComp==2) { 
						D[k1++] = sensor.getCompY().getSampleAtPpick();
					} else if(iComp==3) { 
						D[k1++] = sensor.getCompX().getSampleAtPpick();
					} else { }
				} else if(iPick==2) {
					if(iComp==1) { 
						D[k1++] = sensor.getCompZ().getSampleAtSVpick();
						//D[k1++] = sensor.getCompZ().getSample(3);
					} else if(iComp==2) { 
						D[k1++] = sensor.getCompY().getSampleAtSVpick();
					} else if(iComp==3) { 
						D[k1++] = sensor.getCompX().getSampleAtSVpick();
					} else { }
				} else if(iPick==3) {
					if(iComp==1) { 
						D[k1++] = sensor.getCompZ().getSampleAtSHpick();
					} else if(iComp==2) { 
						D[k1++] = sensor.getCompY().getSampleAtSHpick();
					} else if(iComp==3) { 
						D[k1++] = sensor.getCompX().getSampleAtSHpick();
					} else { }
				} else {}
			}
		}
		return D;
	}

	public double [][] getGreenMatrix(int ip, int isv, int ish) {
		double [][] p = null;
		double [][] sv = null;
		double [][] sh = null;

		if(ip==1) p = getGreenMatrixP();
		if(isv==1) sv = getGreenMatrixSV();
		if(ish==1) sh = getGreenMatrixSH();
		if(p==null) return null;

		if(sv==null) {
			if(sh==null) {
			} else {
				return append(p, sh);
			}
		} else {
			if(sh==null) {
				return append(p, sv);
			} else {
				return append(append(p, sv), sh);
			}
		}
		return p;
	}
	private double [][] append(double[][] A, double[][] B) {
		if(A==null) return B;
		if(B==null) return A;

		int m = A.length;
		int n = B.length;

		double [][] C = new double[m+n][6];

		for(int i=0; i<m; i++) {
			for(int jj=0; jj<6; jj++) C[i][jj] = A[i][jj];
		}
		for(int i=0, j=m; i<n; i++, j++) {
			for(int jj=0; jj<6; jj++) C[j][jj] = B[i][jj];
		}
		return C;
	}
	private double [][] getGreenMatrixP() 	{ return getGreenMatrix(1); }
	private double [][] getGreenMatrixSV() 	{ return getGreenMatrix(2); }
	private double [][] getGreenMatrixSH() 	{ return getGreenMatrix(3); }
	private double [][] getGreenMatrix(int id) {
		if(id==0) return null;
		int m = getNumOfPicks();
		int n = 6;
		double [][] G = new double[m][n];

		for(int i=0, k=0; i<getNumOfSTWells(); i++) {
			SeismicTraceWell well = getSTWells(i);
			for(int j=0; j<well.getNumOfSensors(); j++) {
				SeismicTraceSensor  sensor = well.getSensor(j);
				double [][] green = sensor.getGreen();
				//System.out.println(SeisPTUtil.printArray(green));
				if(id==1) { //pPicks
					for(int jj=0; jj<n; jj++) G[k][jj] = green[0][jj];
				} else if(id==2) { //svPicks
					for(int jj=0; jj<n; jj++) G[k][jj] = green[1][jj];
				} else if(id==3) { //shPicks
					for(int jj=0; jj<n; jj++) G[k][jj] = green[2][jj];
				} else { }
				k++;
			}
		}
		return G;
	}
	
	

	// the first well is at (0, 0). The second is at (1000, 0)
	// 12 receivers from topDp ft with dDp ft step for both wells
	// sampleInterval = 0.00025 s
	public static MSEvent createSynthetics(double sN, double sE, double sD, double topDp, double dDp) {
		int nWells 			= 2;
		int nSensors		= 12;
		int nPicks 			= nWells*nSensors;

		double sampleInterval = 0.00025;
		double pPick 		= sampleInterval;
		double svPick 		= 2.0*sampleInterval;
		double shPick 		= 3.0*sampleInterval;

		ObsWell [] obsWells 		= new ObsWell[nWells];
		SeismicTraceWell [] stWells	= new SeismicTraceWell[nWells];

		for(int i=0; i<nWells; i++) {
			double x = 1000.0*i;
			double y = 0.0;
			obsWells[i] = new ObsWell(nSensors, i, "Obs_Well"+i, x, y, 0.0, 0.0, "zyx", "3D");
			stWells[i] 	= new SeismicTraceWell(nSensors, i);
			for(int j=0; j<nSensors; j++) {
				double z = topDp+j*dDp;
				Sensor sensor = new Sensor(j, "Sensor"+j, x, y, z, 0.0, 0.0, 0.0);
				SeismicTraceSensor stSensor = new SeismicTraceSensor(i, j);
				SeismicTraceComponent [] comps = new SeismicTraceComponent[3];
				for(int k=0; k<3; k++) {
					comps[k] = new SeismicTraceComponent(i, j, k, sampleInterval, new float[5]);
					comps[k].setPpick(pPick);
					comps[k].setSVpick(svPick);
					comps[k].setSHpick(shPick);
				}
				stSensor.setComp(comps);
				obsWells[i].setSensor(j, sensor);
				stWells[i].setSensor(j, stSensor);
			}
			//System.out.println(obsWells[i].toString());
		}
		MSEvent msEvent = new MSEvent(1, "synthetic", sN, sE, sD);
		msEvent.setObsWells(obsWells);
		msEvent.setSTWells(stWells);
		return msEvent;
	}
	
	public String toString(int id) {
		String b = String.format("%s %.2f %.2f %.2f %f %02d %02d %02d \n", 
				getLabel(), getY(), getX(), getZ(), _sampleInterval, _hour, _min, _sec);
		String a = String.format("numOfObsWells=%02d numOfSTWells=%02d ", 
				getNumOfObsWells(), getNumOfSTWells());

		if(id==0) {
			for(int i=0; i<getNumOfPicks(); i++) {
				a = _sensorNo[i]+" "+_pPick[i]+" "+_shPick[i]+" "+_angle[i];
				if(i==_pPick.length-1) b = b.concat(a);
				else b = b.concat(a+"\n");
			}
		}
		if(id==1) {
			for(int i=0; i<getNumOfPicks(); i++) {
				a = String.format("\t%d \t%d \t%d \t%.2f", _sensorNo[i], (int)_pPick[i], (int)_shPick[i], _angle[i]);
				if(i==_pPick.length-1) b = b.concat(a);
				else b = b.concat(a+"\n");
			}
		}
		
		if(id>2) {
			b = b.concat(a+"\n");
			if(_pPick!=null) {
				for(int i=0; i<getNumOfPicks(); i++) {
					a = _sensorNo[i]+" "+_pPick[i]+" "+_svPick[i]+" "+_shPick[i]+" "+_angle[i];
					if(i==_pPick.length-1) b = b.concat(a);
					else b = b.concat(a+"\n");
				}
			}
		}
		return b;
	}

	// iPick=1 p 2=sv 3=sh
	//iComp=1 zComp 2=yComp 3=xComp 
	public void printTrace(int iWell, int iRec, int iComp) {  
		for(int i=0; i<getNumOfSTWells(); i++) {
			if(iWell<0) { }
			else if(iWell!=i) continue;
			else{ }
			SeismicTraceWell well = getSTWells(i);
			for(int j=0; j<well.getNumOfSensors(); j++) {
				if(iRec<0) { }
				else if(iRec!=j) continue;
				else{ }
				SeismicTraceSensor  sensor = well.getSensor(j);
				if(iComp==1) { 
					System.out.println("nz: "+sensor.getCompZ().toString()+"\n"+
							Arrays.toString(sensor.getCompZ().getData()));
				} else if(iComp==2) { 
					System.out.println("ny: "+sensor.getCompY().toString()+"\n"+
							Arrays.toString(sensor.getCompY().getData()));
				} else if(iComp==3) { 
					System.out.println("nx: "+sensor.getCompX().toString()+"\n"+
							Arrays.toString(sensor.getCompX().getData()));
				} else { }
			}
		}
	}

	public String toDeepString() {
		String b = String.format("%d, %s, %f %f %f %f %02d %02d %02d ", 
				getId(), getLabel(), getY(), getX(), getZ(), _sampleInterval, _hour, _min, _sec);
		String a = toString();

		b = b.concat(a+"\n");


		return b;
	}
}
