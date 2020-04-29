package com.geohammer.core;

import org.ucdm.seg2.SEG2Trace;

public class SeismicTraceSensor extends PointAbstract {
	public int _wellId 				= 0;
	public int _sensorId 			= 0;
	public SeismicTraceComponent [] _comp = null; //in zyx order
	
	public double [][] _greenFunction= null;
	
	public SeismicTraceSensor(int wellId, int sensorId, SeismicTraceComponent [] comp) {
		this(wellId, sensorId);
		_comp 		= comp;
	}
	public SeismicTraceSensor(int wellId, int sensorId, int nComps) {
		this(wellId, sensorId);
		_comp 		= new SeismicTraceComponent[nComps];
	}
	public SeismicTraceSensor(int wellId, int sensorId, SEG2Trace [] trcs) {
		this(wellId, sensorId);
		_comp 		= new SeismicTraceComponent[trcs.length];
		for(int i=0; i<trcs.length; i++) {
			_comp[i] = new SeismicTraceComponent(wellId, sensorId, i, trcs[i]);
		}
	}
	public SeismicTraceSensor(int wellId, int sensorId) {
		_wellId 	= wellId;
		_sensorId 	= sensorId;
	}
	public SeismicTraceSensor copy() {
		SeismicTraceSensor sensor = new SeismicTraceSensor(_wellId, _sensorId, _comp.length);
		for(int i=0; i<_comp.length; i++) {
			sensor._comp[i] = _comp[i].copy();
		}
		return sensor;
	}
	
	public double [][] getGreen() 				{ return _greenFunction; }
	public int getWellId() 						{ return _wellId; }
	public int getSensorId() 					{ return _sensorId; }
	public int getNumOfComps() 					{ return _comp.length; }
	
	public void setGreen(double [][] green)		{ _greenFunction = green; }
	public void setWellId(int id)				{ _wellId = id; }
	public void setSensorId(int id)				{ _sensorId = id; }
	
	public SeismicTraceComponent [] getComp() 	{ return _comp; }
	public SeismicTraceComponent  getComp(int index) { return _comp[index]; }
	public SeismicTraceComponent  getCompZ() 	{ return _comp[0]; }
	public SeismicTraceComponent  getCompY() 	{ return _comp[1]; }
	public SeismicTraceComponent  getCompX() 	{ return _comp[2]; }
	
	public void setComp(SeismicTraceComponent [] comps) { _comp = comps; }
	public void setComp(int index, SeismicTraceComponent comp) { _comp[index] = comp; }
	public void setCompZ(SeismicTraceComponent vz) 		{ _comp[0] = vz; }
	public void setCompY(SeismicTraceComponent vy) 		{ _comp[1] = vy; }
	public void setCompX(SeismicTraceComponent vx) 		{ _comp[2] = vx; }
	
	public void channelOrderToZYX(String currentChannelOrder) 	{ 
		if(currentChannelOrder.equalsIgnoreCase("ZYX")) return;
		SeismicTraceComponent vz = _comp[0];
		if(currentChannelOrder.equalsIgnoreCase("XYZ")) {
			_comp[0] = _comp[2];
			_comp[2] = vz;
		} else if(currentChannelOrder.equalsIgnoreCase("XZY")) {
			_comp[0] = _comp[1];
			_comp[1] = _comp[2];
			_comp[2] = vz;
		} else if(currentChannelOrder.equalsIgnoreCase("YXZ")) {
			_comp[0] = _comp[2];
			_comp[2] = _comp[1];
			_comp[1] = vz;
		} else if(currentChannelOrder.equalsIgnoreCase("YZX")) {
			_comp[0] = _comp[1];
			_comp[1] = vz;
		}
	}
	
	public void rotateToNWU(double [][] rm) {
		SeismicTraceComponent compX = getCompX();
		SeismicTraceComponent compY = getCompY();
		SeismicTraceComponent compZ = getCompZ();
		
		double [] a = new double[3];

		for(int i=0, j=0, k=0; i<compZ.getNumOfSamples(); i++) {
			j = 0; k = 0;
			a[j] = rm[j][k++]*compX.getSample(i);
			a[j] += rm[j][k++]*compY.getSample(i);
			a[j] += rm[j][k]*compZ.getSample(i);
			j = 1; k = 0;
			a[j] = rm[j][k++]*compX.getSample(i);
			a[j] += rm[j][k++]*compY.getSample(i);
			a[j] += rm[j][k]*compZ.getSample(i);
			j = 2; k = 0;
			a[j] = rm[j][k++]*compX.getSample(i);
			a[j] += rm[j][k++]*compY.getSample(i);
			a[j] += rm[j][k]*compZ.getSample(i);
			
			compX.setSample(i, (float)a[0]);
			compY.setSample(i, (float)a[1]);
			compZ.setSample(i, (float)a[2]);
		}
	}
	public SeismicTraceComponent vectorAdd(double az) {
		SeismicTraceComponent compX = getCompX();
		SeismicTraceComponent compY = getCompY();
		SeismicTraceComponent comp = new SeismicTraceComponent(compY.getSampleInterval(), null);
		comp.allocate(compY.getNumOfSamples());
		
		double theSin = Math.sin(az);
		double theCos = Math.cos(az);
		double a = 0.0;
		
		for(int i=0; i<compY.getNumOfSamples(); i++) {
			a = theSin*compX.getSample(i)+theCos*compY.getSample(i);
			comp.setSample(i, (float)a);
		}
		return comp;
	}
	private void multiply(double [][] M, double [] B, double [] A) {
		double sum = 0.0;
		for(int i=0; i<M.length; i++) {
			sum = 0.0;
			for(int j=0; j<M[i].length; j++) {
				sum += M[i][j]*B[j];
			}
			A[i] = sum;
		}
	}
	// Option 1: convert field data from(x,y,z) to (E, N, U)
	public void toENU(int iRotation, double [][] rm) { applyRotation(iRotation, rm); }
	public void applyRotation(int iRotation, double [][] rm) {
		SeismicTraceComponent compX = getCompX();
		SeismicTraceComponent compY = getCompY();
		SeismicTraceComponent compZ = getCompZ();
		if(iRotation==0) {
			double [] rx = compX.getSEG2Trace().getStringBlock().getRECEIVER_ORIENTATION();
			double [] ry = compY.getSEG2Trace().getStringBlock().getRECEIVER_ORIENTATION();
			double [] rz = compZ.getSEG2Trace().getStringBlock().getRECEIVER_ORIENTATION();
			if(rx!=null&&ry!=null&&rz!=null){
				for(int i=0; i<3; i++) rm[i][0] = rx[i];
				for(int i=0; i<3; i++) rm[i][1] = ry[i];
				for(int i=0; i<3; i++) rm[i][2] = rz[i];
			}
		} else if(iRotation==1) {
		} else if(iRotation==2) {
		}
		
		//System.out.println(text);
		double [] A = new double[3];
		double [] B = new double[3];
		for(int i=0; i<compZ.getNumOfSamples(); i++) {
			B[0] = compX.getSample(i);
			B[1] = compY.getSample(i);
			B[2] = compZ.getSample(i);
			multiply(rm, B, A);
			compX.setSample(i, (float)A[0]);
			compY.setSample(i, (float)A[1]);
			compZ.setSample(i, (float)A[2]);
			//now CompX is Easting;  component Y Northing; Z Up
		}
	}
	// Option 2: convert field data from(x,y,z) to (N,E,D)
//	public void toNEDn(Sensor receiver) {
//		SeismicTraceComponent compX = getCompX();
//		SeismicTraceComponent compY = getCompY();
//		SeismicTraceComponent compZ = getCompZ();
//		
//		double [][] rm = receiver.getRotationMatrix();  //{{l1, m1, n1}, {l2, m2 , n2}, {l3, m3, n3}}
//		double [] a = new double[3];
//
//		for(int i=0, j=0, k=0; i<compZ.getNumOfSamples(); i++) {
//			j = 0; k = 0;
//			a[j] = rm[j][k++]*compX.getSample(i);
//			a[j] += rm[j][k++]*compY.getSample(i);
//			a[j] += rm[j][k]*compZ.getSample(i);
//			j = 1; k = 0;
//			a[j] = rm[j][k++]*compX.getSample(i);
//			a[j] += rm[j][k++]*compY.getSample(i);
//			a[j] += rm[j][k]*compZ.getSample(i);
//			j = 2; k = 0;
//			a[j] = rm[j][k++]*compX.getSample(i);
//			a[j] += rm[j][k++]*compY.getSample(i);
//			a[j] += rm[j][k]*compZ.getSample(i);
//			
//			compX.setSample(i, (float)a[0]);
//			compY.setSample(i, (float)a[1]);
//			compZ.setSample(i, (float)a[2]);
//		}
//	}
	
	public void flipPolarity() {
		SeismicTraceComponent compX = getCompX();
		SeismicTraceComponent compY = getCompY();
		SeismicTraceComponent compZ = getCompZ();
		if(compX!=null) compX.multiply(-1.0f);
		if(compY!=null) compY.multiply(-1.0f);
		if(compZ!=null) compZ.multiply(-1.0f);
	}
	public void swapChannelOrderToZYX(int inputChannelOrder) {
		SeismicTraceComponent temp = null;
		
		if(inputChannelOrder==1) {
			//zxy to zyx
			temp 		= _comp[1];
			_comp[1] 	= _comp[2];
			_comp[2] 	= temp;
		} else if(inputChannelOrder==2) {
			//xyz to zyx
			temp 		= _comp[0];
			_comp[0] 	= _comp[2];
			_comp[2] 	= temp;
		}
	}
	public void swapChannelOrderToXYZ(int inputChannelOrder) {
		SeismicTraceComponent temp = null;
		
		if(inputChannelOrder==0) {
			//zyx
			temp 		= _comp[0];
			_comp[0] 	= _comp[2];
			_comp[2] 	= temp;
		} else if(inputChannelOrder==1) {
			//zxy
			temp 		= _comp[0];
			_comp[0] 	= _comp[1];
			_comp[1] 	= _comp[2];
			_comp[2] 	= temp;
		} else if(inputChannelOrder==2) {
		}
	}
	
	
	//for Seg2View
	public float normalize() {
		if(getNumOfComps()==1) return normalize(null, null, getCompZ());
		else if(getNumOfComps()==2) return normalize(null, getCompY(), getCompZ());
		else return normalize(getCompX(), getCompY(), getCompZ());
	}
	public float normalize(SeismicTraceComponent X, SeismicTraceComponent Y, SeismicTraceComponent Z) {
		float max = getMaxAbsAmp(X, Y, Z);
		
		float scalor = 1.0f;
		if(max!=0) scalor = (float)(1.0/max);
		
		Z.multiply(scalor); 
		if(Y!=null) Y.multiply(scalor); 
		if(X!=null) X.multiply(scalor);
		return scalor;
	}
	public float getMaxAbsAmp() {
		if(getNumOfComps()==1) return getMaxAbsAmp(null, null, getCompZ());
		else if(getNumOfComps()==2) return getMaxAbsAmp(null, getCompY(), getCompZ());
		else return getMaxAbsAmp(getCompX(), getCompY(), getCompZ());
	}
	public float getMaxAbsAmp(SeismicTraceComponent X, SeismicTraceComponent Y, SeismicTraceComponent Z) {
		double maxZ = Z.getAbsMax();
		double minZ = Z.getAbsMin();
		double maxY = maxZ;
		double minY = minZ;
		double maxX = maxZ;
		double minX = minZ;
		
		if(Y!=null) { maxY = Y.getAbsMax(); minY = Y.getAbsMin(); }
		if(X!=null) { maxX = X.getAbsMax(); minX = X.getAbsMin(); }
		
		double max = maxX>maxY ? maxX:maxY;
		double min = minX<minY ? minX:minY;
		max = max>maxZ ? max:maxZ;
		min = min<minZ ? min:minZ;
		
		return (float)max;
	}
	public float multiply(float scalor) {
		if(getNumOfComps()==1) return multiply(scalor, null, null, getCompZ());
		else if(getNumOfComps()==2) return multiply(scalor, null, getCompY(), getCompZ());
		else return multiply(scalor, getCompX(), getCompY(), getCompZ());
	}
	public float multiply(float scalor, SeismicTraceComponent X, SeismicTraceComponent Y, SeismicTraceComponent Z) {
		Z.multiply(scalor); 
		if(Y!=null) Y.multiply(scalor); 
		if(X!=null) X.multiply(scalor);
		return scalor;
	}
	public float centerToAverage() {
		if(getNumOfComps()==1) return centerToAverage(null, null, getCompZ());
		else if(getNumOfComps()==2) return centerToAverage(null, getCompY(), getCompZ());
		else return centerToAverage(getCompX(), getCompY(), getCompZ());
	}
	public float centerToAverage(SeismicTraceComponent X, SeismicTraceComponent Y, SeismicTraceComponent Z) {
		double aveZ = Z.getAverageAmplitude();
		double aveY = aveZ;
		double aveX = aveZ;
		
		if(Y!=null) { aveY = Y.getAverageAmplitude(); }
		if(X!=null) { aveX = X.getAverageAmplitude(); }
		
		float ave = (float)((aveZ+aveY+aveX)/3.0);
		
		Z.add(-ave); 
		if(Y!=null) Y.add(-ave); 
		if(X!=null) X.add(-ave);
		return ave;
	}
	public String toString() {
		String b = "wellId= "+_wellId + " sensorId= "+_sensorId+" NumOfComps="+ getNumOfComps()+"\n";
		
		for(int i=0; i<_comp.length; i++) {
			String a = _comp[i].toString(); 			b = b.concat(a+"\n");
			//a = Arrays.toString(_comp[i].getData()); 			b = b.concat(a+"\n");
		}
		
		return b;
	}
	public String printGreen() {
		String b = "wellId= "+_wellId + " sensorId= "+_sensorId+" NumOfComps="+ getNumOfComps()+"\n";
		
		for(int i=0; i<_greenFunction.length; i++) {
			for(int j=0; j<_greenFunction[i].length; j++) {
				String a = " "+_greenFunction[i][j]+" "; 			b = b.concat(a);
			}
		}
		
		return b;
	}
}
