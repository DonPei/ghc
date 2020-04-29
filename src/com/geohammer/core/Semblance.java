package com.geohammer.core;

import static edu.mines.jtk.util.ArrayMath.sqrt;

import java.util.Arrays;

import org.apache.commons.math3.analysis.interpolation.AkimaSplineInterpolator;
import org.apache.commons.math3.analysis.polynomials.PolynomialFunction;
import org.apache.commons.math3.analysis.polynomials.PolynomialSplineFunction;
import org.ucdm.dsp.DSP;
import org.ucdm.dsp.LinearRegression;
import org.ucdm.dsp.MedianFilter;
import org.ucdm.dsp.stl.SeasonalTrendLoess;
import org.ucdm.dsp.wavelet.DiscreteWT;
import org.ucdm.seg2.SEG2Trace;

import edu.mines.jtk.dsp.BandPassFilter;
import edu.mines.jtk.dsp.ButterworthFilter;
import edu.mines.jtk.dsp.Conv;
import edu.mines.jtk.dsp.Histogram;
import edu.mines.jtk.util.ArrayMath;

public class Semblance extends PointAbstract {
	public int _wellId 				= 0;
	public int _sensorId 			= 0;
	public SeismicTraceComponent [] _comp = null; 
	
	public Semblance(int wellId, int sensorId, SeismicTraceComponent [] comp) {
		this(wellId, sensorId);
		_comp 		= comp;
	}
	public Semblance(int wellId, int sensorId, int nComps) {
		this(wellId, sensorId);
		_comp 		= new SeismicTraceComponent[nComps];
	}
	public Semblance(int wellId, int sensorId, SEG2Trace [] trcs) {
		this(wellId, sensorId);
		_comp 		= new SeismicTraceComponent[trcs.length];
		for(int i=0; i<trcs.length; i++) {
			_comp[i] = new SeismicTraceComponent(wellId, sensorId, i, trcs[i]);
		}
	}
	public Semblance(int wellId, int sensorId) {
		_wellId 	= wellId;
		_sensorId 	= sensorId;
	}
	public Semblance copy() {
		Semblance semblance = new Semblance(_wellId, _sensorId, _comp.length);
		for(int i=0; i<_comp.length; i++) {
			semblance._comp[i] = _comp[i].copy();
		}
		return semblance;
	}
	
	public int getWellId() 						{ return _wellId; }
	public int getSensorId() 					{ return _sensorId; }
	public int getNumOfComps() 					{ return _comp.length; }
	
	public void setWellId(int id)				{ _wellId = id; }
	public void setSensorId(int id)				{ _sensorId = id; }
	
	public SeismicTraceComponent [] getComp() 	{ return _comp; }
	public SeismicTraceComponent  getComp(int index) { return _comp[index]; }
	public SeismicTraceComponent  getCompX() 	{ return _comp[0]; }
	public SeismicTraceComponent  getCompY() 	{ return _comp[1]; }
	public SeismicTraceComponent  getCompZ() 	{ return _comp[2]; }
	
	public void setComp(SeismicTraceComponent [] comps) { _comp = comps; }
	public void setComp(int index, SeismicTraceComponent comp) { _comp[index] = comp; }
	
	public int calReferenceIndex(SeismicTraceComponent [] group) {
		if(group.length==1) return 0;
		int nShortTime = 60;
		int nLongTime = 200;
		float [] ratio = group[0].calStaLtaRatio(group[0].getData(), nShortTime, nLongTime);
		float max = ArrayMath.max(ratio);
		int maxIndex = 0;
		float a = 0;
		for(int j=1; j<group.length; j++) {
			ratio = group[j].calStaLtaRatio(group[j].getData(), nShortTime, nLongTime);
			a = ArrayMath.max(ratio);
			if(a>max) {
				max = a;
				maxIndex = j;
			}
		}
		return maxIndex;
	}
	
	public SeismicTraceComponent [] stacking(int stackingFold, int stackingMethod) {
		if(stackingFold<=1) return this.getComp();
		int nc = _comp.length/stackingFold;
		if(_comp.length%stackingFold>0) nc++;
		
		float [][] data = new float[nc][];
		float [] stacked = null;
		for(int i=0, k1=0, k2=0; i<nc; i++) {
			k1 = i*stackingFold;
			k2 = k1+stackingFold;
			k2 = k2>_comp.length?_comp.length:k2;	
			//System.out.println("i="+i+" k1="+k1+" k2="+k2+" nTraces="+_comp.length);
			if((k2-k1)==1) {
				data[i] = ArrayMath.copy(_comp[k1].getData());
				continue;
			}

			int nTraces = k2-k1;
			SeismicTraceComponent [] group = new SeismicTraceComponent[nTraces];
			for(int j=k1, jk=0; j<k2; j++) group[jk++] = _comp[j];

			float [][] signal = new float[group.length][];
			for(int j=0; j<group.length; j++) signal[j] = group[j].getData();
			
			if(stackingMethod==0) { //STA/LTA
				int nShortTime = 60;
				int nLongTime = 200;
				int dIndex0 = group[0].calStaLta(group[0].getData(), nShortTime, nLongTime);
				for(int j=1; j<group.length; j++) {
					int dIndex1 = group[j].calStaLta(group[j].getData(), nShortTime, nLongTime);
					group[j].shift(dIndex0-dIndex1);
					System.out.println("i="+i+" dIndex0="+dIndex0+" dIndex1="+dIndex1);
				}
			} else if(stackingMethod==1) { //Min Variance
				int offset = 50;
				stacked = minVariance(signal, offset);
				//System.out.println("i="+i+" k1="+k1+" nTraces="+nTraces);
				//_comp[k1+1].setData(stacked);
			} else if(stackingMethod==2) { //MER				
				int nTime = 250;
				int dIndex0 = group[0].calMer(group[0].getData(), nTime);
				for(int j=1; j<group.length; j++) {
					int dIndex1 = group[j].calMer(group[j].getData(), nTime);
					group[j].shift(dIndex0-dIndex1);
					//System.out.println("i="+i+" dIndex0="+dIndex0+" dIndex1="+dIndex1);
				}
			} else if(stackingMethod==3) { //Matched Filtering	
				int iRef = calReferenceIndex(group);
				int maxlag = 150;
				//System.out.println("i="+i+" iRef="+iRef);
				boolean alignmentOnly = false;
				stacked = xCorr(signal, maxlag, iRef, alignmentOnly);
			} else {
				stacked = data[i];
			}
			data[i] = stacked;
		}
		
		double sampleInterval = _comp[0].getSampleInterval();
		SeismicTraceComponent [] comps = new SeismicTraceComponent[data.length];
		for(int i=0; i<data.length; i++) {
			SeismicTraceComponent comp = new SeismicTraceComponent(sampleInterval, data[i]);
			comps[i] = comp;
		}
		
		return comps;
	}
	
	public SeismicTraceComponent [] xStacking(int stackingFold) {
		if(stackingFold<=1) return this.getComp();
		int nc = _comp.length/stackingFold;
		if(_comp.length%stackingFold>0) nc++;
		double sampleInterval = _comp[0].getSampleInterval();
		
		SeismicTraceComponent [] stackedGroupComp = new SeismicTraceComponent[nc];
		for(int i=0, k1=0, k2=0; i<nc; i++) {
			k1 = i*stackingFold;
			k2 = k1+stackingFold;
			k2 = k2>_comp.length?_comp.length:k2;	
			//System.out.println("i="+i+" k1="+k1+" k2="+k2+" nTraces="+_comp.length);
			if((k2-k1)==1) {
				stackedGroupComp[i] = _comp[k1].copy();
				continue;
			}

			int nTraces = k2-k1;
			SeismicTraceComponent [] group = new SeismicTraceComponent[nTraces];
			for(int j=k1, jk=0; j<k2; j++) group[jk++] = _comp[j];

			int iRef = calReferenceIndex(group);
			int maxlag = 150;
			//System.out.println("i="+i+" iRef="+iRef);
			boolean alignmentOnly = false;
			float [][] results = xCorr(group, maxlag, iRef, alignmentOnly);
			float [] stacked = results[0];
			float [] shift = results[1];
			
			SeismicTraceComponent stackedComp = new SeismicTraceComponent(sampleInterval, stacked);
			int iMethod = 1;
			int nShortTime = 60;
			int nLongTime = 200;
			int startIndexInclusive = 0; 
			int endIndexExclusive = stackedComp.getData().length;
			int iPhase = 0;
			
			startIndexInclusive = stackedComp.pickFirstArrival(iMethod, iPhase, nShortTime, nLongTime, startIndexInclusive, endIndexExclusive);
			for(int j=0; j<group.length; j++) {
				if(iPhase==0) group[j].setPpick((double)(startIndexInclusive-shift[j]));
				else if(iPhase==1) group[j].setSHpick((double)(startIndexInclusive-shift[j]));
			}
			
			iPhase = 1;
			startIndexInclusive = stackedComp.pickFirstArrival(iMethod, iPhase, nShortTime, nLongTime, startIndexInclusive+100, endIndexExclusive);
			for(int j=0; j<group.length; j++) {
				if(iPhase==0) group[j].setPpick((double)(startIndexInclusive-shift[j]));
				else if(iPhase==1) group[j].setSHpick((double)(startIndexInclusive-shift[j]));
			}
			
			stackedGroupComp[i] = stackedComp;
		}
		
		return stackedGroupComp;
	}
	
	public float [][] xCorr(SeismicTraceComponent [] group, int maxlag, int iRef, boolean alignmentOnly) {
		int nTraces = group.length;
		float [] ref = group[iRef].getData();
		int n = ref.length;
		float [] stack = ArrayMath.copy(ref);
		float [] shift = new float[nTraces];
		for(int i=0; i<nTraces; i++) {
			if(i==iRef) {
				shift[i] = 0;
				//System.out.println("i="+i+" iShift=0");
				continue;
			}
			float [] y = group[i].getData();
			float [] r = xCorr(ref, y, maxlag);
			//System.out.println("i="+i+" dIndex="+Arrays.toString(r));
			int dIndex = 0;
			float max = r[0];
			for(int j=1; j<r.length; j++){
				if(r[j]>max) {
					max = r[j];
					dIndex = j;
				}
			}
			//group[i].setPpick(pPick);
			int iShift = maxlag - dIndex;
			shift[i] = iShift;
			//System.out.println("i="+i+" dIndex="+dIndex+" iShift="+iShift);
//			boolean test = true;
//			if(test) return r;
			
			//shift
			if(alignmentOnly) {
				float [] v1 = new float[n];
				for(int j=0; j<n; j++) v1[j] = 0;			

				for (int ik=0, j=0; ik<n; ik++) {
					j = ik - iShift;
					if (j < 0 || j >= n)
						continue;
					else
						v1[ik]= y[j];
				}			
				for(int j=0; j<n; j++) y[j] = v1[j];
			} else {
				for (int ik=0, j=0; ik<n; ik++) {
					j = ik - iShift;
					if (j < 0 || j >= n)
						continue;
					else
						stack[ik]+= y[j];
				}
			}
		}
		
		for(int j=0; j<n; j++) stack[j] /= nTraces;
		return new float [][] {stack, shift};
	}
	//Computes the cross correlation between sequences x and y. r=x.y
    //maxDalay is the maximum lag 
	public float [] xCorr(float[] x, float[] y, int maxDelay)    {
		float[] r = new float[2*maxDelay+1];
		int is = 0;
    	float sxy = 0;
    	int n = x.length;
    	for (int delay=-maxDelay, k=0;delay<=maxDelay;delay++, k++) {
    		sxy = 0;
    		is = 0;
    		for (int i=0, j=0;i<n;i++) {
    			j = i + delay;
    			if (j < 0 || j >= n) continue;
    			else {
    				sxy += x[i]*y[j];
    				is++;
    			}
    		}
    		r[k] = sxy/is;
    	}
    	return r;    	
	}
    
	public float [] minVariance(float [][] signal, int offset) {
		int nTraces = signal.length;
		int p1 = (nTraces-1)*offset;
		int p2 = signal[0].length-p1;
		
		float a = 0;
		int dIndex = 0;
		float min = minVariance(signal, p1, p2, -offset);
		for(int i=-offset+1; i<=offset; i++){
			a = minVariance(signal, p1, p2, i);
			if(a<min) {
				min = a;
				dIndex = i;
			}
		}
		
		//System.out.println(" dIndex="+dIndex+" p1="+p1+" p2="+p2);
		float [] v = new float[signal[0].length];
		for(int i=0; i<v.length; i++) v[i] = signal[0][i];
		for(int i=p1; i<p2; i++) {
			float sum = 0.0f;
			for(int j=0, jk=0; j<nTraces; j++) {
				jk = i+j*dIndex;
				sum += signal[j][jk];
			}
			v[i] = sum/nTraces;
		}
		
		float [] v1 = new float[signal[0].length];
		for(int j=1, jk=0; j<nTraces; j++) {
			for(int i=0; i<v1.length; i++) v1[i] = signal[j][i];
			for(int i=p1; i<p2; i++) {
				jk = i+j*dIndex;
				v1[i] = signal[j][jk];
			}
			for(int i=0; i<v1.length; i++) signal[j][i] = v1[i];
		}
		
		return v;
	}
	
	//p1 inclusive, p2 exclusive
	public float minVariance(float [][] signal, int p1, int p2, int dIndex) {
		int nTraces = signal.length;
		float sum = 0.0f;
		float a = 0.0f;
		for(int i=p1; i<p2; i++) {
		//for(int i=0; i<nPoints; i++) {			
			float average = 0.0f;
			for(int j=0, jk=0; j<nTraces; j++) {
				jk = i+j*dIndex;
				average += signal[j][jk];
			}
			average /= nTraces;
			
			float variance = 0.0f;
			for(int j=0, jk=0; j<nTraces; j++) {
				jk = i+j*dIndex;
				a = signal[j][jk]-average;
				variance += a*a;
			}
			sum += variance;
		}
		return sum;
	}
	
	
	public void exponentiallySmooth(int halfWidth) {
		int m = halfWidth; //half-width m of a boxcar filter
		float sigma = sqrt(m*(m+1)/3.0f);  //a Gaussian filter with a specified half-width sigma
		float ss = sigma*sigma;
		float a = (1.0f+ss-sqrt(1.0f+2.0f*ss))/ss;
		
		for(int i=0; i<_comp.length; i++) {
			//_comp[i].smooth(2*halfWidth);
			DSP.exponentialFilterEndingWithZeroSlope(a, _comp[i].getData(), null);
			//DSP.exponentialFilterEndingWithZeroValue(a, _comp[i].getData(), null);
		}
	}
	public void multiplyAbs() { 
		for(int i=0; i<_comp.length; i++) { 
			float [] v = _comp[i].getData(); 
			_comp[i].multiplyAbs(v);
		} 
	}
	public void applyBandPassFilter(BandPassFilter bandPassFilter, ButterworthFilter butterworthFilter) {
		for(int i=0; i<_comp.length; i++) {
			if(bandPassFilter!=null) {				
				bandPassFilter.apply(_comp[i].getData(), _comp[i].getData());
			} else if(butterworthFilter!=null) {
				butterworthFilter.applyForward(_comp[i].getData(), _comp[i].getData());
			} 
		}
	}
	public void applyDWT1(int direction, int waveletIndex, int waveletParameter, int coarsestScale) {
		for(int i=0; i<_comp.length; i++) {
			float [] data = _comp[i].getData();
			//System.out.println("iStep="+i+" data.length="+data.length);
			double[] signal = DiscreteWT.padPow2(data);
			try {
				double [] wT = DiscreteWT.transform(signal, waveletIndex, waveletParameter, coarsestScale, direction);
				float [] copy = new float[wT.length];
				for(int j=0; j<copy.length; j++) {
					copy[j] = (float)wT[j];
				}
				_comp[i].setData(copy);
				//System.out.println("iStep="+i+" copy.length="+copy.length);
			} catch (Exception e1) {
			}
		}
	}
	public void applyDWT(int direction, int waveletIndex, int waveletParameter, int coarsestScale) {
		double [][] data = new double[_comp.length][];
		for(int i=0; i<_comp.length; i++) {
			data[i] = DiscreteWT.padPow2(_comp[i].getData());
		}
		double log2N = Math.log(data[0].length) / Math.log(2);
		System.out.println("i="+data.length+" j="+data[0].length+" j1="+_comp[0].getData().length+" log2N="+log2N);
		double [][] wT = null;
		try {
			wT = DiscreteWT.transform(data, waveletIndex, waveletParameter, coarsestScale, direction);	
			int subLength = (int) (Math.pow(2, coarsestScale));
			for(int i=0; i<wT.length; i++) {
				double [] wtV = wT[i];
				for(int j=subLength; j<wtV.length; j++) {
					//wtV[j] = 0;
					if(wtV[i]>-0.15&&wtV[i]<0.15); wtV[i] = 0;
				}
			}
			
			wT = DiscreteWT.transform(wT, waveletIndex, waveletParameter, coarsestScale, -direction);
		} catch (Exception e1) {
		}
		
//		int subLength = (int) (Math.pow(2, coarsestScale));		
//		for(int i=0; i<_comp.length; i++) {
//			float [] trendData = new float[subLength];
//			double [] wtV = wT[i];
//			for(int j=0; j<trendData.length; j++) trendData[j] = (float)wtV[j];
//			_comp[i].setData(trendData);
//		}
		for(int i=0; i<_comp.length; i++) {
			float [] v = _comp[i].getData();
			for(int j=0; j<v.length; j++) v[j] = (float)wT[i][j];
		}
		
		Histogram histogram = null;
	}
	
	
	public void applyMedianFilter(int medianFilterLen) {
		float [] buffer = new float[_comp.length];
		int traceLen = _comp[0].getData().length;
		for(int i=0; i<traceLen; i++) {
			for(int j=0; j<buffer.length; j++) buffer[j] = _comp[j].getData(i);
			MedianFilter medianFilter = new MedianFilter(medianFilterLen);
			medianFilter.apply(buffer);
			for(int j=0; j<buffer.length; j++) _comp[j].setData(i, _comp[j].getData(i)-buffer[j]);
			//for(int j=0; j<buffer.length; j++) _comp[j].setData(i, buffer[j]);
		}
	}
	public void applyMedianFilterTraceByTrace(int medianFilterLen) {
		MedianFilter medianFilter = new MedianFilter(medianFilterLen);
		for(int i=0; i<_comp.length; i++) {
			float [] v = _comp[i].getData();
			medianFilter.apply(v);
		}
	}
	
	public double stackingEnergy() {
		int traceLen = _comp[0].getData().length;
		float [] buffer = new float[traceLen];
		for(int i=0; i<_comp.length; i++) {
			for(int j=0; j<buffer.length; j++) buffer[j] += _comp[i].getData(j);
		}
		double sum = 0;
		for(int j=0; j<buffer.length; j++) sum += buffer[j]*buffer[j];
		return sum;
	}	
	
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
		
//		for(int i=0; i<_greenFunction.length; i++) {
//			for(int j=0; j<_greenFunction[i].length; j++) {
//				String a = " "+_greenFunction[i][j]+" "; 			b = b.concat(a);
//			}
//		}
		
		return b;
	}
}
