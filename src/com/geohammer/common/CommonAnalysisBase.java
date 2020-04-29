package com.geohammer.common;

import static edu.mines.jtk.util.ArrayMath.sqrt;

import java.util.ArrayList;

import com.geohammer.dsp.DSP;
import com.geohammer.dsp.MedianFilter;

public abstract class CommonAnalysisBase {
	public int 			_wellIndex 			= 0;
	public int 			_stageIndex 		= 0;
	public int 			_index 				= 0;
	public String 		_wellName 			= null; 
	public String 		_stageName 			= null;
	public String 		_startTimeString 	= null; 
	public String 		_durationString 	= null;
	public boolean 		_enabled 			= true;
	
	public double [] 	_data 				= null;
	public ArrayList<String>  _resultList 	= null;
	
	public CommonAnalysisBase() { super(); }
	public CommonAnalysisBase(String wellName, String stageName, int wellIndex, int stageIndex, int index, boolean enabled) {
		this(wellName, stageName, null, null, wellIndex, stageIndex, index, enabled);
	}
	public CommonAnalysisBase(int wellIndex, int stageIndex, int index, boolean enabled) {
		this(null, null, null, null, wellIndex, stageIndex, index, enabled);
	}
	public CommonAnalysisBase(String wellName, String stageName, String startTimeString, String durationString, int wellIndex, int stageIndex, int index, boolean enabled) {
		_wellName 	= wellName;
		_stageName 	= stageName;
		_startTimeString = startTimeString;
		_durationString = durationString;
		_wellIndex 	= wellIndex;
		_stageIndex = stageIndex;
		_index 		= index;
		_enabled 	= enabled;
	}
	
	public void allocateResultList() 			{ _resultList = new ArrayList<String>(); }
	
	public void setWellName(String wellName) 					{ _wellName = wellName; }
	public void setStageName(String stageName) 					{ _stageName = stageName; }
	public void setWellIndex(int wellIndex) 					{ _wellIndex = wellIndex; }
	public void setStageIndex(int stageIndex) 					{ _stageIndex = stageIndex; }
	public void setStartTimeString(String startTimeString) 		{ _startTimeString = startTimeString; }
	public void setDurationString(String durationString) 		{ _durationString = durationString; }
	
	public String getWellName() 					{ return _wellName; }
	public String getStageName() 					{ return _stageName; }
	public String getStartTimeString() 				{ return _startTimeString; }
	public String getDurationString() 				{ return _durationString; }
	public int getWellIndex() 						{ return _wellIndex; }
	public int getStageIndex() 						{ return _stageIndex; }
	public boolean getEnabled() 					{ return _enabled; }
	public double [] getData() 						{ return _data; }
	public ArrayList<String> getResultList() 		{ return _resultList; }
	
	public void setEnabled(boolean enabled) 		{ _enabled = enabled; }
	public void setData(double [] data) 			{ _data = data; }
	public void setData(int index, double v) 		{ _data[index] = v; }
	public void addResultList(String value) 		{ _resultList.add(value);}
	
	protected void applyMedianFilter(int medianFilterLen, float [] buffer) {
		int traceLen = buffer.length;
		for(int i=0; i<traceLen; i++) {
			MedianFilter medianFilter = new MedianFilter(medianFilterLen);
			medianFilter.apply(buffer);
		}
	}
	protected static void exponentiallySmooth(int halfWidth, float [] buffer) {
		int m = halfWidth; //half-width m of a boxcar filter
		float sigma = sqrt(m*(m+1)/3.0f);  //a Gaussian filter with a specified half-width sigma
		float ss = sigma*sigma;
		float a = (1.0f+ss-sqrt(1.0f+2.0f*ss))/ss;		
		DSP.exponentialFilterEndingWithZeroSlope(a, buffer, null);
	}
	
	protected float mean(float[] paramArrayOfFloat) {
		int i = paramArrayOfFloat.length;
		float f = 0.0F;
		for (int j = 0; j < i; j++)  f += paramArrayOfFloat[j]; 
		return f / i;
	}
	protected  float standardDeviation(float[] paramArrayOfFloat) {
		return (float)Math.sqrt(variance(paramArrayOfFloat));
	}
	protected float variance(float[] paramArrayOfFloat) {
		if(paramArrayOfFloat.length==1) return 0;
		float f1 = 0.0F;
		float f2 = mean(paramArrayOfFloat);
		for (int j = 0; j < paramArrayOfFloat.length; j++) {
			f1 += (paramArrayOfFloat[j] - f2)*(paramArrayOfFloat[j] - f2);
		}
		float f3 = f1 / (paramArrayOfFloat.length - 1);
		return f3;
	}
	protected void setZero(double [] v, double tol) {
		double [] absV = abs(v);
		
		double a = absV[0];
		for(int i=1; i<absV.length; i++) { if(absV[i]>a) a = absV[i]; }
		
		if(a==0.0) return;
		for(int i=0; i<absV.length; i++) { absV[i] /= a; }
		
		for(int i=0; i<absV.length; i++) { if(absV[i]<tol) v[i] = 0; }
	}
	protected double [] abs(double [] rx) {
		int n1 = rx.length;
		double [] ax = new double[n1];
		for (int i1=0; i1<n1; ++i1) ax[i1] = abs(rx[i1]);
		return ax;
	}
	protected double abs(double x) {
		return (x>=0.0)?x:-x;
	}
	
	protected double [] normalize(double [] v) {
		double norm = Math.sqrt(v[0]*v[0]+v[1]*v[1]+v[2]*v[2]);
		double [] n = new double[3];
		for(int i=0; i<n.length; i++) n[i] = v[i]/norm;
		return n;
	}
	
	public double [][] getResults() {
		if(_resultList==null) return null;
		double [][] v = new double[_resultList.size()][];
		
		for(int i=0; i<_resultList.size(); i++) {
			String [] splits = _resultList.get(i).split("[,]");
			double [] av = new double[splits.length];
			for(int ik=0; ik<splits.length; ik++) av[ik] = Double.parseDouble(splits[ik].trim());
			v[i] = av;
		}
		return v;
	}
	
	public String toString(int id) {
		String a = _wellIndex+", "+_stageIndex+", "+_index+", "+_wellName+", "+_stageName+", "
				+_startTimeString+", "+_durationString;
		StringBuilder b = new StringBuilder();
		b.append(a+"\n");
		if(id==0) {
			
		} else if(id==1) {
			if(_resultList==null) return b.toString();
			for(int i=0; i<_resultList.size(); i++) {
				b.append(_resultList.get(i)+"\n");
			}
		}

		return b.toString();
	}
}
