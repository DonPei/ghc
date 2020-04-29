package com.geohammer.core;

import java.util.Arrays;

public class GreenComp {
	public int _compId 				= 0;
	public int _numOfSamples 		= 0;
	public double _sampleInterval 	= 0.00025;   // second
	public float [] _data 			= null;
	
	public GreenComp(int compId, double sampleInterval, int numOfSamples, float [] data) {
		_compId 		= compId;
		_sampleInterval = sampleInterval;
		if(data!=null) {
			_numOfSamples 	= data.length;
			_data 			= data;
		} else { 
			allocate(numOfSamples); 
			_numOfSamples 	= numOfSamples;
		}
	}
	public void allocate(int numOfSamples) {
		_data = new float[numOfSamples];
	}
	public double getSampleInterval() 			{ return _sampleInterval; }
	public float [] getData() 					{ return _data; }
	
	public void setData(float [] data) 			{ _data = data; }
	public void setData(int index, float v) 	{ _data[index] = v; }
	
	
	public GreenComp copy() {
		GreenComp comp = new GreenComp(_compId, _sampleInterval, _data.length, _data);
		comp.allocate(_numOfSamples);
		System.arraycopy(comp._data, 0, _data, 0, _numOfSamples);
		
		return comp;
	}
	public String toString() {
		return Arrays.toString(_data);
	}
}
