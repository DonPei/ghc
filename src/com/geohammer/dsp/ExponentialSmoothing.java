package com.geohammer.dsp;

public class ExponentialSmoothing {
	double _alpha 			= 1.0;
	double _preSmoothData 	= 0.0;
	double _smoothData		= 0.0;
	

	public ExponentialSmoothing(double alpha) {
		_alpha = alpha;
	}
	
	double smoothedData(double raw) {
       _smoothData = (1.0 - _alpha)*_preSmoothData + _alpha*raw;
       _preSmoothData = _smoothData;

       return _smoothData;
    }
	
	public void smoothedData(float [] rawData, float [] f) {
		int n = rawData.length;
		float [] y = new float[n];
		float a = (float)(_alpha);
		float b = (float)(1.0-_alpha);
		y[0] = rawData[0];
		for (int i=1; i<n; ++i) y[i] = b*y[i-1] + a*rawData[i];
		
		if(f==null) { for(int i=0; i<n; i++) rawData[i] = y[i]; }
		else  		{ for(int i=0; i<n; i++) f[i] = y[i]; }
	}
}
