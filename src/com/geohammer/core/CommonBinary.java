package com.geohammer.core;

import static edu.mines.jtk.util.ArrayMath.sqrt;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteOrder;

import org.ucdm.core.SeismicTraceComponent;
import org.ucdm.dsp.DSP;

import edu.mines.jtk.io.ArrayFile;

//https://stackoverflow.com/questions/2356137/read-large-files-in-java

public abstract class CommonBinary {
	public float [][] 		_data 			= null;
	public int [] 			_channelIndex 	= null;	
	
	public int 				_cMin 			= 1;
	public int 				_cMax 			= 1000000;
	public int 				_cIncremental 	= 1;
	public int 				_fMin 			= 1;
	public int 				_fMax 			= 1000000;
	public int 				_fIncremental 	= 1;

	public CommonBinary() {  }

	public float [][] getData() 				{ return _data; }
	public int getFMin() 					 	{ return _fMin; }
	public int getFIncremental() 				{ return _fIncremental; }
	public int [] getChannelIndex() 			{ return _channelIndex; }	
	
	public void setData(float [][] data) 		{ _data = data; }
	
	public void write(String selectedFileName) {
		ArrayFile af 		= null;
		try {
			File file = new File(selectedFileName);
			if (file.exists ()) {
				file.delete();
			}
			file = new File(selectedFileName);
			af = new ArrayFile(file,"rw", ByteOrder.LITTLE_ENDIAN, ByteOrder.LITTLE_ENDIAN);

			af.seek(0);
			
		} catch (FileNotFoundException e) {
			System.err.println("Caught FileNotFoundException: " + e.getMessage());
			throw new RuntimeException(e);
		} catch (IOException e) {
			System.err.println("Caught IOException: " + e.getMessage());
		} finally {
			if (af!=null) try { af.close(); } catch (IOException e) {}
		}
	}
	
//	public float [][] getPartialData(int iPart, int nPart) 	{ 
//		if(iPart==0) return _data;
//		
//		int nc = _data.length;
//		int nf0 = _data[0].length;
//		int nf = nf0/nPart;
//		float [][] data = new float[nc][nf];
//		int fMin = (iPart-1)*nf;
//		int fMax = fMin+nf;
//		
//		for(int i=0; i<nc; i++) {		
//			for(int j=fMin, kj=0; j<fMax; j++) {
//				data[i][kj++] = _data[i][j];
//			}
//		}
//		
//		return data; 
//	}
	public SeismicTraceComponent [] genSeismicTraceComponent(double sampleInterval) {
		SeismicTraceComponent [] comps = new SeismicTraceComponent[_data.length];
		for(int i=0; i<_data.length; i++) {
			SeismicTraceComponent comp = new SeismicTraceComponent(sampleInterval, _data[i]);
			comps[i] = comp;
		}
		return comps;
	}	
	public float[][] downSample(int cIncremental, int fIncremental) {
		return downSample(_cMin, _cMax, cIncremental, _fMin, _fMax, fIncremental);
	}
	public float[][] downSample(int cMin, int cMax, int cIncremental, int fMin, int fMax, int fIncremental) {
		//clap
		cMax = cMax>_data.length?_data.length:cMax;
		int nc = (cMax-cMin)/cIncremental; 	
		if((cMax-cMin)%cIncremental>0) nc++;
		fMax = fMax>_data[0].length?_data[0].length:fMax;
		int nf = (fMax-fMin)/fIncremental; 
		if((fMax-fMin)%fIncremental>0) nf++;
		
		float [][] v = new float[nc][nf];
		int [] channelIndex = new int [nc];
		for(int i=0, ki=0; i<nc; i++) {
			ki = cMin+i*cIncremental;
			for(int j=0, kj=0; j<nf; j++) {
				kj = fMin+j*fIncremental;
				v[i][j] = _data[ki][kj];
			}
			channelIndex[i] = ki;
		}
		_channelIndex = channelIndex;
		//long compression = getCompression();		
		//setCompression(compression*fIncremental);
		//System.out.println("compression="+compression+" compression1="+getCompression());
		//System.out.println("data1.length="+_data.length+" data1[0].length="+_data[0].length);	
		
		_cMin 			= cMin;
		_cMax 			= cMax;
		_cIncremental	= cIncremental;
		_fMin 			= fMin;
		_fMax 			= fMax;
		_fIncremental	= fIncremental;
		
		return v;
	}
	
	public double sum(float [] v) {
		double sum = 0;
		for(int j=0; j<v.length; j++)  sum += v[j];
		return sum;
	}
	
	public void removeDc() {
		for(int i=0; i<_data.length; i++) {
			double sum = sum(_data[i]);
			double ave = sum/_data[i].length;
			float a = (float)ave;
			
			for(int j=0; j<_data[i].length; j++)  _data[i][j] -= a;
		}
	}
	public void smooth() {
		double len = 7;
		int halfWidth = (int)(len/2);
		int m = halfWidth; //half-width m of a boxcar filter
		float sigma = sqrt(m*(m+1)/3.0f);  //a Gaussian filter with a specified half-width sigma
		float ss = sigma*sigma;
		float a = (1.0f+ss-sqrt(1.0f+2.0f*ss))/ss;

		for(int i=0; i<_data.length; i++) {
			DSP.exponentialFilterEndingWithZeroSlope(a, _data[i], null);
		}
	}
	
	public float [] bestSNR(float [] v0, float [] vUp, float [] vDw) {		
		int nBefore = 80;
		int nAfter = 150;
		float [] signal = new float[v0.length];
		double snr = 0.0;
		double max = -1.0;
		int iMax = 0;
		for(int i=-10; i<10; i++) {
			for(int j=0; j<signal.length; j++) signal[j] = 0;
			snr = bestSNR(v0, vUp, vDw, signal, i, nBefore, nAfter);
			if(max<snr) {
				max = snr;
				iMax = i;
			}
		}
		
		for(int j=0; j<signal.length; j++) signal[j] = 0;
		snr = bestSNR(v0, vUp, vDw, signal, iMax, nBefore, nAfter);
		
		float [] v = new float[v0.length];
		int iStep = iMax;
		if(iMax<0)  iStep=-iMax; 
		for(int j=0; j<iStep; j++) v[j] = v0[j];
		for(int j=v0.length-iStep; j<v0.length; j++) v[j] = v0[j];
		for(int j=iStep; j<v0.length-iStep; j++) v[j] = signal[j-iStep]/3.0f;		
		return v;
	}
	public double bestSNR(float [] v0, float [] vUp, float [] vDw, float [] signal, 
			int step, int nBefore, int nAfter) {
		int iStep = step;
		float [] v1 = vDw;
		float [] v2 = vUp;
		if(step<0) {
			iStep=-step;
			v1 = vUp;
			v2 = vDw;
		}
		
		int k = 2*iStep;
		int nf = v0.length-k;
		//float [] signal = new float[nf];
		for(int j=0; j<nf; j++)  signal[j] += v0[iStep+j]+v1[k+j]+v2[j]; 
		return bestSNR(nf, signal, nBefore, nAfter);
	}
	public double bestSNR(int nf, float [] signal, int nBefore, int nAfter) {
		double rmsE1 = 0.0;
		double rmsE2 = 0.0;
		int k1 = 0; 
		int k2 = 0;		
		double snr = 0.0;
		double max = -1.0;
		
		for(int k=nBefore; k<nf-nAfter; k++) {
			k1 = k-nBefore;
			k2 = k;
			rmsE1 = rmsSquare(signal, k1, k2);
			k1 = k;
			k2 = k+nAfter;
			rmsE2 = rmsSquare(signal, k1, k2);
			snr = rmsE1/rmsE2;	
			max = max>snr?max:snr;
		}
		return max;		
	}
	
	public float [] bestRmsSquare(float [] v0, float [] vUp, float [] vDw) {	
		float [] signal = new float[v0.length];
		double rms = 0.0;
		double max = -1.0;
		int iMax = 0;
		for(int i=-30; i<30; i++) {
			for(int j=0; j<signal.length; j++) signal[j] = 0;
			rms = rmsSquare(v0, vUp, vDw, signal, i);
			if(max<rms) {
				max = rms;
				iMax = i;
			}
		}
		
		//System.out.println(" iMax="+iMax);
		for(int j=0; j<signal.length; j++) signal[j] = 0;
		rms = rmsSquare(v0, vUp, vDw, signal, iMax);
		
		float [] v = new float[v0.length];
		int iStep = iMax;
		if(iMax<0)  iStep=-iMax; 
		for(int j=0; j<iStep; j++) v[j] = v0[j];
		for(int j=v0.length-iStep; j<v0.length; j++) v[j] = v0[j];
		for(int j=iStep; j<v0.length-iStep; j++) v[j] = signal[j-iStep]/3.0f;		
		return v;
	}
	public double rmsSquare(float [] v0, float [] vUp, float [] vDw, float [] signal, int step) {
		int iStep = step;
		float [] v1 = vDw;
		float [] v2 = vUp;
		if(step<0) {
			iStep=-step;
			v1 = vUp;
			v2 = vDw;
		}
		
		int k = 2*iStep;
		int nf = v0.length-k;
		for(int j=0; j<nf; j++)  signal[j] += (v0[iStep+j]+v1[k+j]+v2[j]); 
		return rmsSquare(signal, 0, nf);
	}
	//k1 inclusive, k2 exclusive
	public double rmsSquare(float [] signal, int k1, int k2) {
		double sum = 0.0;
		for(int i=k1; i<k2; i++) sum += signal[i]*signal[i];
		return sum/(k2-k1);
	}
	

}
