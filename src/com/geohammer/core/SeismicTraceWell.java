package com.geohammer.core;

import org.ucdm.seg2.SEG2Trace;

public class SeismicTraceWell {
	public int _wellId 				= 0;
	public SeismicTraceSensor [] _sensors = null; //usually 12 sensors
	
	public SeismicTraceWell(int wellId, SEG2Trace [] trcs) {
		this(wellId);
		int nSensors = trcs.length/3;
		_sensors 	= new SeismicTraceSensor[nSensors];
		int j = 0;
		for(int i=0; i<nSensors; i++) {
			SEG2Trace [] data = new SEG2Trace[]{trcs[j++], trcs[j++], trcs[j++] };
			_sensors[i] = new SeismicTraceSensor(wellId, i, data);
		}
		j = 0;
		for(int i=0; i<nSensors; i++) {
			SeismicTraceComponent [] comps = _sensors[i].getComp();
			comps[0].setSEG2Trace(trcs[j++]);
			comps[1].setSEG2Trace(trcs[j++]);
			comps[2].setSEG2Trace(trcs[j++]);
			//System.out.println("i="+i+" set 3 comps");
		}
	}
	
	public SeismicTraceWell(int wellId, SeismicTraceSensor [] sensors) {
		this(wellId);
		_sensors 	= sensors;
	}
	public SeismicTraceWell(int nSensors, int wellId) {
		this(wellId);
		_sensors 	= new SeismicTraceSensor[nSensors];
	}
	public SeismicTraceWell(int wellId) {
		_wellId 	= wellId;
	}
	public SeismicTraceWell clone() {
		SeismicTraceWell well = new SeismicTraceWell(_sensors.length, _wellId);
		for(int i=0; i<_sensors.length; i++) {
			well._sensors[i] = _sensors[i].copy();
		}
		return well;
	}
	public int getWellId() 										{ return _wellId; }
	public SeismicTraceSensor [] getSensors() 					{ return _sensors; }
	public SeismicTraceSensor  getSensor(int index) 			{ return _sensors[index]; }

	public void setWellId(int id)								{ _wellId = id; }
	public void setSensor(SeismicTraceSensor [] v) 				{ _sensors = v; }
	public void setSensor(int index, SeismicTraceSensor v) 		{ _sensors[index] = v; }
	
	public int getNumOfSensors() 								{ return _sensors.length; }
	public int getNumOfComps() 	{ 
		int k = 0; 
		for(int i=0; i<_sensors.length; i++) k+=_sensors[i].getNumOfComps(); 
		return k; 
	}
	
	public String toString() {
		String b = "wellId= "+_wellId+" NumOfSensors()="+ getNumOfSensors()+" NumOfComps()="+getNumOfComps()+"\n";
		
		for(int i=0; i<_sensors.length; i++) {
			String a = _sensors[i].toString(); 			b = b.concat(a+"\n");
		}
		
		return b;
	}
	
	public void channelOrderToZYX(String currentChannelOrder) 	{ 
		if(currentChannelOrder.equalsIgnoreCase("ZYX")) return;
		for(int i=0; i<_sensors.length; i++) _sensors[i].channelOrderToZYX(currentChannelOrder); 
	}
	
	//order is zyx
//	public float [] getRecordZ(int geophoneIndex) {
//		int traceIndex = _seg2.getTraceIndex(geophoneIndex);
//		return _seg2.getTrace(traceIndex);
//	}
//	public float [] getRecordY(int geophoneIndex) {
//		int traceIndex = _seg2.getTraceIndex(geophoneIndex);
//		return _seg2.getTrace(traceIndex+1);
//	}
//	public float [] getRecordX(int geophoneIndex) {
//		int traceIndex = _seg2.getTraceIndex(geophoneIndex);
//		return _seg2.getTrace(traceIndex+2);
//	}
//	public float [][] getRecordZYX(int geophoneIndex) {
//		return new float[][]{getRecordZ(geophoneIndex), getRecordY(geophoneIndex), getRecordX(geophoneIndex)};
//	}

}
