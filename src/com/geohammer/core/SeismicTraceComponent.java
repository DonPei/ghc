
package com.geohammer.core;

import static edu.mines.jtk.util.ArrayMath.sqrt;
import static edu.mines.jtk.util.ArrayMath.zerofloat;

import java.util.Arrays;
import java.util.Random;

import org.ucdm.dsp.AmplitudeCosineTaper;
import org.ucdm.complex.Complex;
import org.ucdm.dsp.DSP;
import org.ucdm.seg2.SEG2Trace;

import edu.mines.jtk.dsp.BandPassFilter;
import edu.mines.jtk.dsp.ButterworthFilter;
import edu.mines.jtk.dsp.Conv;
import edu.mines.jtk.dsp.Fft;
import edu.mines.jtk.dsp.LinearInterpolator;
import edu.mines.jtk.dsp.Sampling;
import edu.mines.jtk.util.ArrayMath;

//single component time series
// all operation fails under quality=poor
public class SeismicTraceComponent extends PointAbstract {
	public int _quality 			= 1;
	
	public int _wellId 				= 0;
	public int _sensorId 			= 0;
	public int _compId 				= 0;
	
	//public int _numOfSamples 		= 0;
	public double _sampleInterval 	= 0.00025;   // second

	public double _pPick 			= 0.0;   // second
	public double _svPick 			= 0.0;   // second
	public double _shPick 			= 0.0;   // second
	public double _angle 			= 0.0;   // second
	public double _pcPick 			= 0.0;   // second
	public double _svcPick 			= 0.0;   // second
	public double _shcPick 			= 0.0;   // second
	
	public float [] _data 			= null;
	public SEG2Trace _seg2Trace 	= null;
	
	public Fft 		_fft 			= null;  // FFT algorithm
	public float [] _g 				= null;  // forward FFT, usually the complex number
	public float [] _spectrum		= null;  // spectral of forward FFT
	public float [] _frequency		= null;  // frequency of the spectral
	
	//104000 is tool sensitivity (Avalon, DS150, and DS250)
	public double 	_gain1 	= 1.0/104000.0; 	//Converts amplitude scale from minivolts to micron/sec for PinnTech tools.
	public double 	_gain2 	= 1.0e-6/104000.0; 	//Converts amplitude scale from minivolts to m/sec for PinnTech tools.
	public double 	_gain3 	= 2.443/1000; 		//Mike's numbers to Converts amplitude scale from volts to cm/sec for PinnTech tools.
	
	public double 	_INVALID_D 		= -99999.0;
	
	public SeismicTraceComponent(double sampleInterval, float [] data) {
		this(0, 0, 0, sampleInterval, data);
	}
	public SeismicTraceComponent(int wellId, int sensorId, int compId, double sampleInterval, float [] data) {
		this(0, "unknown", 0.0, 0.0, 0.0, 
				wellId, sensorId, compId, sampleInterval, data);
	}
	
	public SeismicTraceComponent(int id, String label, double x, double y, double z, 
			int wellId, int sensorId, int compId, double sampleInterval, float [] data) {
		super(id, label, x, y, z);
		_wellId 		= wellId;
		_sensorId 		= sensorId;
		_compId 		= compId;
		_sampleInterval = sampleInterval;
		if(data!=null) {
			_data 			= data;
		}
	}
	
	public SeismicTraceComponent(int wellId, int sensorId, int compId, SEG2Trace trc) {
		this(wellId, sensorId, compId, trc.getSampleInterval(), trc.getData());
		_pPick = 0.001*trc.getPpick();
		_svPick = 0.001*trc.getSVpick();
		_shPick = 0.001*trc.getSHpick();
		_angle = trc.getAngle();
		_seg2Trace = trc;
	}
	
	public SeismicTraceComponent copy() { return copy(0, _data.length-1); }
	public SeismicTraceComponent copy(int from, int to) {
		int n = to-from+1;
		if(n<=0) return null;
		SeismicTraceComponent comp = new SeismicTraceComponent(getId(), getLabel(), getX(), getY(), getZ(),
				_wellId, _sensorId, _compId, _sampleInterval, null);
		comp.allocate(n);
		System.arraycopy(_data, from, comp._data, 0, n);
		comp._pPick 	= _pPick;
		comp._svPick 	= _svPick;
		comp._shPick 	= _shPick;
		comp._angle 	= _angle;
		comp._quality 	= _quality;
		comp._pcPick 	= _pcPick;
		comp._svcPick 	= _svcPick;
		comp._shcPick 	= _shcPick;
		if(_seg2Trace!=null) comp._seg2Trace = _seg2Trace.copy();
		
		return comp;
	}
	
	public void gc() { 
		_data 			= null;
		_seg2Trace 		= null;
		
		_fft 			= null;  // FFT algorithm
		_g 				= null;  // forward FFT, usually the complex number
		_spectrum		= null;  // spectral of forward FFT
		_frequency		= null;  // frequency of the spectral 
	}
	
	public double getGain1() 					{ return _gain1; }
	public double getGain2() 					{ return _gain2; }
	public double getGain3() 					{ return _gain3; }
	
	public float [] getData() 					{ return _data; }
	public float getData(int index) 			{ return _data[index]; }
	public int getQuality() 					{ return _quality; }
	public int getWellId() 						{ return _wellId; }
	public int getSensorId() 					{ return _sensorId; }
	public int getcompId() 						{ return _compId; }
	public int getNumOfSamples() 				{ return _data.length; }
	public double getSampleInterval() 			{ return _sampleInterval; }
	public double getPpick() 					{ return _pPick; }
	public double getSVpick() 					{ return _svPick; }
	public double getSHpick() 					{ return _shPick; }
	public double getPcpick() 					{ return _pcPick; }
	public double getSVcpick() 					{ return _svcPick; }
	public double getSHcpick() 					{ return _shcPick; }
	public double getAngle() 					{ return _angle; }
	public int getSampleIndex(double pPicks)	{ return (int)(pPicks/_sampleInterval); }
	public float getSample(int index) 			{ return _data[index]; }
	public float getSampleAtPpick() 			{ return (float)getSample(_pPick); }
	public float getSampleAtSVpick() 			{ return (float)getSample(_svPick); }
	public float getSampleAtSHpick() 			{ return (float)getSample(_shPick); }
	
	public SEG2Trace getSEG2Trace() 			{ return _seg2Trace; }
	public void setData(float [] data) 			{ _data = data; }
	public void setData(int index, float v) 	{ _data[index] = v; }
	public void setData(float v) 				{ for(int i=0; i<_data.length; i++) _data[i] = v; }
	public void setSEG2Trace(SEG2Trace seg2Trace) { _seg2Trace = seg2Trace; }
	public void setSample(int index, float v) 	{ _data[index] = v; }
	public void setWellId(int id)				{ _wellId = id; }
	public void setSensorId(int id)				{ _sensorId = id; }
	public void setcompId(int id)				{ _compId = id; }
	
	public void setSampleInterval(double sampleInterval) 	{ _sampleInterval = sampleInterval; }
	public void setPpick(double pPick) 						{ _pPick = pPick; }
	public void setSVpick(double svPick) 					{ _svPick = svPick; }
	public void setSHpick(double shPick) 					{ _shPick = shPick; }
	public void setPcpick(double pcPick) 					{ _pcPick = pcPick; }
	public void setSVcpick(double svcPick) 					{ _svcPick = svcPick; }
	public void setSHcpick(double shcPick) 					{ _shcPick = shcPick; }
	public void setAngle(double angle) 						{ _angle = angle; }
	
	public void allocate(int numOfSamples) 	{ _data = new float[numOfSamples]; }
	
	public double getSample(double picks) 		{ 
		if(picks==_INVALID_D) return _INVALID_D;
		if(picks==10.0) return _INVALID_D;
		//System.out.println(picks);
		if(picks>10.0) return getSample((int)picks);
		int k 		= getSampleIndex(picks);
		double x1 	= k*_sampleInterval;
		double y1 	= (double)getSample(k);
		k++;
		if(k>=_data.length) k = _data.length-1;
		double x2 	= k*_sampleInterval;
		double y2 	= (double)getSample(k);
		
		return interpolation(picks, x1, x2, y1, y2);
	}
	private double interpolation(double x, double x1, double x2, double y1, double y2){
		double fraction = (x-x1)/(x2-x1);
		return (y1+(y2-y1)*fraction);
	}
	
//	public void getMaxAmplitudeSample(double picks) 		{ 
//		//1) identify initial pulse
//		int pIndex = (int)_pPick;
//		float pValue = getSample(pIndex);
//		float qValue = 0;
//		int p1Index = pIndex;
//		int p2Index = pIndex;
//		
//		for(int i=pIndex; i<_data.length; i++) {
//			qValue = getSample(i);
//			if(pValue*qValue>=0) p2Index++;
//			else break;
//		}
//		for(int i=pIndex; i>=0; i--) {
//			qValue = getSample(i);
//			if(pValue*qValue>=0) p1Index--;
//			else break;
//		}
//		
//	}
//	public double getPulseAmplitudeSample(int iPick, int nPointAhead, int nPointAfter) 		{ 
//		float [] v = cutWindow(iPick, nPointAhead, nPointAfter);
//		int [] minIndex = new int[1];
//		float min = getMin(v, minIndex);
//		int [] maxIndex = new int[1];
//		float max = getMax(v, maxIndex);
//	}
//	public void refinePicks(int nPointAhead, int nPointAfter) { 
//		int pIndex = refinePicks(1, nPointAhead, nPointAfter);
//		if(pIndex>0) _pPick = pIndex;
//		int shIndex = refinePicks(2, nPointAhead, nPointAfter);
//		if(shIndex>0) _shPick = shIndex;
//	}
	
	//The Ahrens Moving Average is an IIR (Infinite Impulse Response), 
	//front-weighted moving average which can be used to filter time   
	//series data using a window as short as 2 periods. It provides  
	//superior smoothing and is less likely to be perturbed by transient 
	//spikes than either a simple moving average or a exponential moving
	//average with the same smoothing period.
	private double [] ahrensMovingAverage(float [] trace, int period) {
		double [] ave = new double[trace.length];
		int x_start = 0;
		int x_end = trace.length-1;

		int count = 0;
		double total = 0;

		for (int i = x_start; i < x_start + period && i < x_end; i++) {
			count++;
			total += (double)trace[i];
			ave[i] = (total / count);
		}

		for (int i = x_start + period; i <= x_end; i++) {
			double numerator = (double)trace[i] - (ave[i-1] + ave[i-period]/2.0);
			ave[i] = ave[i-1] + numerator / period;
		}
		return ave;
	}
	public int refinePicks(int iMethod, int iPick, int nPointAhead, int nPointAfter) { 
		if(nPointAhead==0&&nPointAfter==0) return 0;
		int [] index = getWindowIndex(iPick, nPointAhead, nPointAfter);
		if(index==null) return 0;
		int p1 = index[0];
		int p2 = index[1];
		int extremeIndex = p1;
		
		if(iMethod==0) {
			int m = 5; //half-width m of a boxcar filter
			float sigma = sqrt(m*(m+1)/3.0f);  //a Gaussian filter with a specified half-width sigma
			float ss = sigma*sigma;
			float a = (1.0f+ss-sqrt(1.0f+2.0f*ss))/ss;


			float [] trace = cutTrace(p1, p2);
			//double [] data = ahrensMovingAverage(trace, 5);
			float [] data = trace;

			DSP.exponentialFilterEndingWithZeroSlope(a, data, null);
			int positiveOne = (data[1]-data[0])>0.0?1:-1;

			for(int i=1; i<data.length; i++) {
				if(positiveOne==((data[i]-data[i-1])>0.0?1:-1)) {
				} else {
					extremeIndex += i;
					break;
				}
			}
		} else if(iMethod==1) {
			float max = Math.abs(_data[extremeIndex]);
			float a = 0;
			
			for(int i=p1+1; i<=p2; i++) {
				a = Math.abs(_data[i]); 
				if(a>max) { max=a; extremeIndex=i; }
			}
		}
		
		if(iPick==0) _pPick = extremeIndex;
		else if(iPick==1) _shPick = extremeIndex;
		else if(iPick==2) _svPick = extremeIndex;
		return extremeIndex;
	}
	
	public void cutWindow(int nPointAhead, int nPointAfter) { 
		int [] pIndex = getWindowIndex(0, nPointAhead, nPointAfter);
		int [] shIndex = getWindowIndex(1, nPointAhead, nPointAfter);
		
		float [] pData = cutWindow(0, nPointAhead, nPointAfter);
		float [] shData = cutWindow(1, nPointAhead, nPointAfter);
		
		if(pData!=null) {
			for(int i=0; i<_data.length; i++) _data[i] = 0.0f;
			for(int i=0, k=pIndex[0]; i<pData.length; i++, k++) _data[k] = pData[i];
			if(shData!=null) {
				for(int i=0, k=shIndex[0]; i<shData.length; i++, k++) _data[k] = shData[i];
			}
		}
	}
	public float [] cutTrace(int p1Index, int p2Index) { 
		p1Index = p1Index<0?0:p1Index;
		p2Index = p2Index>(_data.length-1)?(_data.length-1):p2Index;
		
		int n = p2Index-p1Index+1;
		float [] window = new float[n];
		
		for(int i=p1Index, k=0; i<=p2Index; i++, k++) window[k] = _data[i]; 
		return window;
	}
	public float [] cutWindow(int iPick, int nPointAhead, int nPointAfter) { 
		int [] index = getWindowIndex(iPick, nPointAhead, nPointAfter);
		
		if(index==null) return null;
		
		int p1 = index[0];
		int p2 = index[1];
		int n = p2-p1+1;
		float [] window = new float[n];
		
		for(int i=p1, k=0; i<=p2; i++, k++) window[k] = _data[i]; 
		return window;
	}
	public int [] getWindowIndex(int iPick, int nPointAhead, int nPointAfter) { 
		int pick = (int)_pPick;
		if(iPick==1) pick = (int)_shPick;
		else if(iPick==2) pick = (int)_svPick;
		
		if(pick<=10) return null;
		
		int p1 = pick-nPointAhead;
		int p2 = pick+nPointAfter;
		if(p1>p2) return null;
		p1 = p1<0?0:p1;
		p2 = p2>(_data.length-1)?(_data.length-1):p2;
		return new int[]{p1, p2};
	}
	
	Random random = new Random();
	//between [0.0, 1.0)
	public void random() 					{ for(int i=0; i<_data.length; i++) _data[i] = (float)Math.random();  }  
	public void addRandomNoise(float mean, float max) 	{ 
		Random random = new Random();
		for(int i=0; i<_data.length; i++) {
			_data[i] += (mean + max*(random.nextFloat()-random.nextFloat()));
		}
	} 
	public void scale(double scalor) 		{ for(int i=0; i<_data.length; i++) _data[i] *= scalor; }
	public float getAverageAmplitude() 		{ 
		float sum = 0;
		for(int i=0; i<_data.length; i++) sum += _data[i];  
		return (float)(sum/_data.length);
	} 
	public void paddingZeros(int maxN) {
		if(maxN<=_data.length) return;
		float [] data = new float[maxN];
		for(int i=0; i<_data.length; i++) data[i] = _data[i];
		for(int i=_data.length; i<maxN; i++) data[i] = 0.0f;		
		_data = data;
	}
	public void lineUp(int kx) {
		if(kx==0) return;

		int n = _data.length;
		float [] v = new float[n];
		if(kx>0) {
			for(int i=0, j=kx; i<n-kx; i++) v[i] = _data[j++];
			for(int i=n-kx; i<n; i++) v[i] = 0;
		} else if(kx<0) {
			for(int i=0; i<-kx; i++) v[i] = 0;
			for(int i=-kx, j=0; i<n; i++) v[i] = _data[j++];
		}
		_data = v;
	}
	
	// http://jean-pierre.moreau.pagesperso-orange.fr/Cplus/smooth_cpp.txt
	public void smooth(int span) {
		int radius = span/2;
		int j1 = 0;
		int j2 = 0;
		float [] smData = new float[_data.length];
		for(int i=0; i<_data.length; i++) {
			double sum = 0;
			j1 = i-radius;
			j2 = i+radius;
			if(j1<0) { j1 = 0; }
			if(j2>=_data.length) { j2 = _data.length-1; }
			for(int j=j1; j<=j2; j++) sum += _data[j];
			smData[i] = (float)(sum/(j2-j1+1));
		}
		_data = smData;
	}

	public void exponentiallySmooth(int halfWidth) {
		int m = halfWidth; //half-width m of a boxcar filter
		float sigma = sqrt(m*(m+1)/3.0f);  //a Gaussian filter with a specified half-width sigma
		float ss = sigma*sigma;
		float a = (1.0f+ss-sqrt(1.0f+2.0f*ss))/ss;
		
		DSP.exponentialFilterEndingWithZeroValue(a, _data, null);
	}
	
	//IDENTIFY INITIAL PULSES & PULSE DURATIONS
	public float [] pulse(int index) {
		float base = getSample(index);
		int j1 = index;
		int j2 = index;
		
		int k = index;
		boolean move = true;
		while(move) {
			if(--k>=0) {
				if(base*getSample(k)>=0) j1--; 
				else move = false;
			} else { j1 = 0; move = false; }
			
		}
		k = index;
		move = true;
		while(move) {
			if(++k<_data.length) {
				if(base*getSample(k)>=0) j2++; 
				else move = false;
			} else { j2 = _data.length-1; move = false; }
		}
		
		
		float [] pulse = new float[j2-j1+1];
		k = 0;
		for(int i=j1; i<=j2; i++) pulse[k++] = getSample(i);
		return pulse;
	}
	
	public void add(float v) { for(int i=0; i<_data.length; i++) { _data[i] += v; } }
	public void multiply(float v) { for(int i=0; i<_data.length; i++) { _data[i] *= v; } }
	public void multiplyAbs(float [] v) { for(int i=0; i<_data.length; i++) { _data[i] *= v[i]>0?v[i]:(-v[i]); } }
	
	//Determine the wavelet coefficient and generate a denoised signal by 
	// setting some of the coefficients to zero and re-generating.
//	public void denoising() {
//		// Choose a filter to use.
//		//private Daubechies8 filter = new Daubechies8();
//		Daubechies2 filter = new Daubechies2();
//		//private Daubechies6 filter = new Daubechies6();
//		//private Daubechies7 filter = new Daubechies7();
//		//private Daubechies4 filter = new Daubechies4();
//		// Offset into data array where real values start 
//		int offset = 0;
//		// Power of 2 used to create data areas */
//		int maxlevel = 1;
//		//Denoise threshold (0-1). */
//		double threshold = 0.5;
//
//		int filtertype = filter.getFilterType();
//		int nlines = _data.length;
//	//  Need a power of 2 for data size, plus padding for dyadic multiresolution scaling functions.
//        maxlevel = 1;
//        while ( nlines > Math.pow( 2.0, (double) maxlevel ) ) {
//            maxlevel++;
//        }
//        int count = (int) Math.pow( 2.0, (double) maxlevel ) + filtertype;
//		
//        double [] noisy = new double[count];
//        offset = count = ( count - nlines ) / 2;
//        for(int i=0; i<_data.length; i++) { noisy[count++] = (double)_data[i]; }
//        // Fill any buffered regions with end values.
//        double value = noisy[offset];
//        for ( int i = 0; i < offset; i++ ) { noisy[i] = value; }
//        value = noisy[offset + nlines - 1];
//        for ( int i = offset + nlines; i < noisy.length; i++ ) { noisy[i] = value; }
//        
//        
//		// Choose a maximum level and use that. Note 20 is max possible, and 
//		// we need to leave space for filtertype padding (-4).
//		int level = Math.min( maxlevel - 4, 20 );
//		// Make the Signal and filter it.
//		Signal signal = new Signal( noisy );
//		signal.setFilter( filter );
//		FWTCoef signalCoeffs = signal.fwt( level );
//		// Zero any coefficients that are less than some fraction of the total sum.
//		signalCoeffs.denoise( threshold );
//		//  Rebuild the signal with the new set of coefficients.
//		double[] rebuild = signalCoeffs.rebuildSignal( filter ).evaluate( 0 );
//		//System.arraycopy( rebuild, 0, _data, 0, _data.length );
//		for(int i=0; i<_data.length; i++) { _data[i] = (float)rebuild[i+offset]; }
//	}
	
	public void integration() 		{ 
		float a = 0;
		float b = 0;
		float sum = 0;
		float sampleInterval = (float)getSampleInterval();
		a = _data[0];
		_data[0] = a;
		for(int i=1; i<_data.length; i++) {
			b = _data[i];
			sum += 0.5*(a+b)*sampleInterval;  
			_data[i] = sum;
			a = b;
		}
	} 
	public void changeNan(float v) {
		for(int i=0; i<_data.length; i++) { 
			if(Float.isNaN(_data[i])) _data[i] = v; 
		}
	}
	public void shift(int nPoints) {
		if(Math.abs(nPoints)>_data.length-1) return;
		float [] data = new float[_data.length];
		float a1 = _data[0];
		float a2 = _data[_data.length-1];
		if(nPoints>0) { //shift to left
			for(int i=nPoints; i<_data.length; i++) { data[i] = _data[i-nPoints]; }
			for(int i=0; i<nPoints; i++) { data[i] = a1; }
		} else if(nPoints<0) { //shift to right
			nPoints = Math.abs(nPoints);
			for(int i=0; i<_data.length-nPoints; i++) { data[i] = _data[i+nPoints]; }
			for(int i=_data.length-nPoints; i<_data.length; i++) { data[i] = a2; }
		}
		_data = data; 
	}
	public float normalize() { 
		float max = getAbsMax();
		if(max==0) return 0;
		for(int i=0; i<_data.length; i++) { _data[i] /= max; } 
		return max;
	}
	
	public float getAbsMax() { 
		float max = Math.abs(getMax());
		float min = Math.abs(getMin());
		return max>=min?max:min;
	}
	public float getAbsMin() { 
		float max = Math.abs(getMax());
		float min = Math.abs(getMin());
		return min<=max?min:max;
	}
	public float getMin() { return getMin(_data, null); }
	public float getMin(float [] data, int [] index) {
		int k = 0;
		float min = data[k];
		for(int i=1; i<data.length; i++) {
			if(min>data[i]) { min=data[i]; k=i; }
		}
		if(index!=null) index[0] = k;
		return min;
	}
	
	public float getMax() { return getMax(_data, null); }
	public float getMax(float [] data, int [] index) {
		int k = 0;
		float max = data[k];
		for(int i=1; i<data.length; i++) {
			if(max<data[i]) { max=data[i]; k=i; }
		}
		if(index!=null) index[0] = k;
		return max;
	}
	
	public float [] getFFT() 			{ return _g; }
	public float [] getInverseFFT() 	{ return _fft.applyInverse(_g); } // nx real samples of output h(x)
	public Sampling getFrequencySampling() 	{ return _fft.getFrequencySampling1(); }
	public float [] getSpectrum() 		{ return _spectrum; }
	public float [] getFrequency() 		{ return _frequency; }
	public float [] calSpectrum() 		{ 
		Sampling sk = _fft.getFrequencySampling1();
		int nk 		= sk.getCount();
		//for(int i=0; i<nk; i++) System.out.println("i="+i+" v="+sk.getValue(i));
		_spectrum 	= new float[nk];
		_frequency 	= new float[nk];
		for (int kk=0,kr=0,ki=kr+1; kk<nk; ++kk,kr+=2,ki+=2) {
			double k = sk.getValue(kk); // frequency k in cycles/sample
			_frequency[kk] = (float)(k/_sampleInterval);
			_spectrum[kk] = (float)Math.sqrt(_g[kr]*_g[kr]+_g[ki]*_g[ki]);
		}; 
		return _spectrum;
	}
	public void limitWithin1000() {
		int k = 0;
		for(int i=0; i<_frequency.length; i++) {
			if(_frequency[i]<1000) k++;
		}
		float [] spectrum 	= new float[k];
		float [] frequency 	= new float[k];
		k = 0;
		for(int i=0; i<_frequency.length; i++) {
			if(_frequency[i]<1000) {
				frequency[k] = _frequency[i];
				spectrum[k] = _spectrum[i];
				k++;
			}
		}
		_frequency = frequency;
		_spectrum = spectrum;
	}
	public void deAttenuate(double qs, double vs, double r) {
		double a = (3.1415926*r)/(qs*vs);

		//System.out.println("r="+r+" vs="+vs+" qs="+qs+" a="+a);
		double b = 0.0;
		for(int i=0; i<_spectrum.length; i++) {
			b = Math.exp(a*_frequency[i]);
			_spectrum[i] *= (float)(b);
		}
	}
	public void multiply(double sampleInterval, float [] v) {
		double a = 0.0;
		for(int i=0; i<v.length; i++) {
			a = v[i]*sampleInterval;
			v[i] = (float)(a);
		}
	}
	public Sampling getFreqSampling() { return _fft.getFrequencySampling1(); }
	public float [] calSpectrumDB() 	{ 
		float [] dB = new float[_spectrum.length];
		for(int i=0; i<dB.length; i++) {
			dB[i] = (float)(10*Math.log10(_spectrum[i]));
		}
		return dB;
	}

//http://www.stat.umn.edu/macanova/htmlhelp/node543.htm
//	costaper(N, alpha) returns a vector h containing a 100*alpha percent
//	cosine taper (data window) of length N, tapering approximately N*alpha
//	elements on each end.  N must be a positive integer and alpha a REAL
//	scalar with 0 <= alpha <= .5.
//
//	When alpha = 0, h = rep(1,N), a "taper" that does no tapering.
//
//	For 0 < alpha <= .5 and L = ceiling(alpha*N),
//	   h[j] = .5*(1 - cos(PI*(j-.5)/L) = sin(.5*PI*(j-.5)/L)^2 , 1 <= j <= L
//	   h[j] = 1, L + 1 <= j <= N - L
//	   h[j] = h[N-j+1], N - L + 1 <= j <= N
//
//	A taper of length N is used to multiply a (usually) detrended time
//	series of length N before computing its discrete Fourier transform.
//
//	NOTE: The interpretation of tapering proportion alpha differs from that
//	used by some practitioners, for whom the tapering proportion is the
//	proportion of the entire series modified that is tapered.  To compute a
//	cosine taper to modify 100*P percent of a series of length N, use
//	costaper(N, P/2).
	public void cosineTaper(double alpha) { _data = AmplitudeCosineTaper.taper(_data, alpha); }
	
	public void applyBandPassFilter(BandPassFilter filter) { filter.apply(_data, _data); }	
	public void applyButterworthFilter(ButterworthFilter filter) { filter.applyForward(_data, _data); }
	public void applyBandPassFilter(double klower, double kupper, double kwidth, double aerror) {
		BandPassFilter filter = new BandPassFilter(klower, kupper, kwidth, aerror);
		filter.apply(_data, _data);
	}
	public void applyButterworthFilter(double fl, double al, double fh, double ah) {
		ButterworthFilter filter = new ButterworthFilter(fl, al, fh, ah);
		filter.applyForward(_data, _data);
	}
	public void fft() { fft(0,0); }
	public void fft(int p1Index, int p2Index) {
		float [] data = null;
		if(p1Index>p2Index) {
			data = cutTrace(p2Index, p1Index);
		} else if(p1Index==p2Index) {
			data = ArrayMath.copy(_data);
		} else {
			data = cutTrace(p1Index, p2Index);
		}
		_fft 	= new Fft(data.length); 				// nt = number of samples of f(t)
		//_fft.setCenter(true);
//		Sampling sk = _fft.getFrequencySampling1();
//		int nk 		= sk.getCount(); 					// number of frequencies sampled
		_g 			= _fft.applyForward(data); 		// nk complex samples of g(k)

		//System.out.println("numOfSamples="+_numOfSamples+" numOfFftSamples="+nk+" g.length="+_g.length);
		//System.out.println("f0="+sk.getValue(0)+" df="+(sk.getValue(1)-sk.getValue(0))+" f1="+sk.getValue(nk-1));
		
		//printSampling(sk);
	}

	public void fft(float [] data) {
		_fft 	= new Fft(data.length); 				// nt = number of samples of f(t)
		//_fft.setCenter(true);
//		Sampling sk = _fft.getFrequencySampling1();
//		int nk 		= sk.getCount(); 					// number of frequencies sampled
		_g 			= _fft.applyForward(data); 		// nk complex samples of g(k)

		//System.out.println("numOfSamples="+_numOfSamples+" numOfFftSamples="+nk+" g.length="+_g.length);
		//System.out.println("f0="+sk.getValue(0)+" df="+(sk.getValue(1)-sk.getValue(0))+" f1="+sk.getValue(nk-1));
		
		//printSampling(sk);
	}
	public void cosineTaperThenfft(int p1Index, int p2Index, double alpha) {
		float [] data = null;
		if(p1Index>p2Index) {
			data = cutTrace(p2Index, p1Index);
		} else if(p1Index==p2Index) {
			data = ArrayMath.copy(_data);
		} else {
			data = cutTrace(p1Index, p2Index);
		}
		data = AmplitudeCosineTaper.taper(data, alpha);
		_fft 	= new Fft(data.length); 				// nt = number of samples of f(t)
		//_fft.setCenter(true);
//		Sampling sk = _fft.getFrequencySampling1();
//		int nk 		= sk.getCount(); 					// number of frequencies sampled
		_g 			= _fft.applyForward(data); 		// nk complex samples of g(k)

		//System.out.println("numOfSamples="+_numOfSamples+" numOfFftSamples="+nk+" g.length="+_g.length);
		//System.out.println("f0="+sk.getValue(0)+" df="+(sk.getValue(1)-sk.getValue(0))+" f1="+sk.getValue(nk-1));
		
		//printSampling(sk);
	}
	//ftp://ftp.gps.caltech.edu/pub/shjwei/Brawley/
	// Displacement, Velocity, and Acceleration
	// http://blog.prosig.com/2006/12/07/acceleration-velocity-displacement-spectra-%E2%80%93-omega-arithmetic/
	// http://www.mathworks.com/matlabcentral/answers/21700
    //  function dataout =  iomega(datain, dt, datain_type, dataout_type)
	//   IOMEGA is a MATLAB script for converting displacement, velocity, or
	//   acceleration time-series to either displacement, velocity, or
	//   acceleration times-series. The script takes an array of waveform data
	//   (f), transforms into the frequency-domain in order to more easily
	//   convert into desired output form, and then converts back into the time
	//   domain resulting in output (g) that is converted into the desired form.
	// inType ==1 - Displacement; 2 - Velocity; 3 - Acceleration
	// outType ==1 - Displacement; 2 - Velocity; 3 - Acceleration
	// dt     =   time increment (units of seconds per sample)
	// in     =   input waveform data of type datain_type
	// out    =   output waveform data of type dataout_type
	//1 gn = 9.80665 m/s� = 32.174 ft/s� = 386.0886 in/s�.
	public float [] convert(int inType, float [] dataIn, int nt, double dt, int outType) {
		float ave = getAverageAmplitude();
		for(int i=0; i<nt; i++) _data[i] -= ave; 
		cosineTaper(0.1);
		Fft fft 	= new Fft(nt); 						// nt = number of samples of f(t)
		fft.setCenter(true);
		Sampling sk = fft.getFrequencySampling1();
		int nk 		= sk.getCount(); 					// number of frequencies sampled
		float[] g 	= fft.applyForward(dataIn); 		// nk complex samples of g(k)
		double nyq 	= 1.0/(2.0*dt);    					// Nyquest frequency
		double df 	= 2.0*nyq/(nk-1);					// frequency increment
		int p 		= outType-inType;
		double m2Pi = 2.0*Math.PI;

		//System.out.println("nt= "+nt+" nk="+nk+" g.length="+g.length+" nyq="+nyq+" df="+df+" p="+p);
		//System.out.println("f0= "+sk.getValue(0)+" df= "+(sk.getValue(1)-sk.getValue(0))+" f1= "+sk.getValue(nk-1));
		double fi = 0.0;
		for (int kk=0,kr=0,ki=kr+1; kk<nk; ++kk,kr+=2,ki+=2) {
			fi = -nyq+kk*df;
			if(fi!=0.0) {
				Complex a = new Complex(g[kr], g[ki]);
				Complex b = new Complex(0, m2Pi*fi).pow(p);
				Complex c = a.multiply(b);
				g[kr] = (float)c.getReal();
				g[ki] = (float)c.getImaginary();
			} else {
				g[kr] = 0.0f;
				g[ki] = 0.0f;
			}
		}
		float[] h = fft.applyInverse(g); // nx real samples of output h(x)
		//System.out.println(" h.length="+h.length);
		return h;
	}
	
	public float demean() {
		int n = _data.length;
		//1) Remove the mean from your sample (now have zero-mean sample)
		float ave = getAverageAmplitude();
		for(int i=0; i<n; i++) _data[i] -= ave; 
		return ave;
	}
	//http://earthquake.usgs.gov/research/software/
	//http://www.mathworks.com/matlabcentral/answers/17611-how-to-convert-a-accelerometer-data-to-displacements
	//http://www.liubo.us/how-to-calculate-displacement-from-acceleration-data-in-time-domain/
	//http://www.thenakedscientists.com/forum/index.php?topic=4489.0
	private void cumulativeSum() {
		//System.out.println("sampleInterval="+_sampleInterval);
		int n = _data.length;
		double [] y = new double [n];
		//1) Remove the mean from your sample (now have zero-mean sample)
		float ave = getAverageAmplitude();
		for(int i=0; i<n; i++) _data[i] -= ave; 
		//2) Integrate once to get velocity using some rule (trapezoidal, etc.)
		double sum = 0;
		y[0] = sum;
		for(int i=1; i<n; i++) {
			sum += 0.5*(_data[i-1]+_data[i])*_sampleInterval;
			y[i] = sum;
		}
		for(int i=0; i<n; i++) { _data[i] = (float)y[i]; }
	}
	
	public void velocityToAmplitude(int iMethod) {
		//System.out.println("iMethod="+iMethod);
		if(iMethod==0) {
			float [] y = convert(2, _data, _data.length, _sampleInterval, 1);
			_data = y;
		} else if(iMethod==1) cumulativeSum();
		
	}


	public String toString() {
		String b = "wellId= "+_wellId + " sensorId= "+_sensorId + " compId= "+_compId;
		String a = "numOfSamples= "+_data.length + " sampleInterval= "+_sampleInterval + 
				" pPick= "+_pPick+ " svPick= "+_svPick+ " shPick= "+_shPick+ " angle= "+_angle+ " quality= "+_quality;
		b = b.concat("\n"+a+"\n");
		
		return b;
	}
	
	public Sampling getSampling() { return new Sampling(_data.length, getSampleInterval(), _data[0]); }	
	public void reSample(Sampling gSampling) {
		Sampling selfSampling = getSampling();
		LinearInterpolator li = new LinearInterpolator();
	    li.setExtrapolation(LinearInterpolator.Extrapolation.CONSTANT);
	    li.setUniform(selfSampling.getCount(),selfSampling.getDelta(),selfSampling.getFirst(),_data);
	    
	    float [] gu = new float[gSampling.getCount()];
	    for(int i=0; i<gu.length; i++) {
	    	gu[i] = li.interpolate(gSampling.getValue(i));
	    }
	    _data = gu;
	    _sampleInterval 	= gSampling.getDelta();
	}
	
//  z[i] =  sum x[j]*y[i+j]
//           j
	private float [] xCor(float [] X, float [] Y) {
		 int lx = X.length;
	      int ly = Y.length;
	      int lz = lx-1+ly-1+1;
	      int kx = 0;
	      int ky = 0;
	      int kz = 1-lx;
	      float[] zf = zerofloat(lz);
	      Conv.xcor(lx,kx,X,ly,ky,Y,lz,kz,zf);
	      return zf;
	      //System.out.println(Arrays.toString(zf));
	}
	
	public int xCor(SeismicTraceComponent that) {
		float [] v = xCor(_data, that.getData());
		float max = v[0];
		int maxIndex = 0;
		for(int i=1; i<v.length; i++) {
			if(max<v[i]) { max = v[i]; maxIndex = i; }
		}
		int kx = _data.length-1-maxIndex;
		System.out.println("kx="+kx+" maxIndex="+maxIndex);
		//_data = v;
		return kx;
	}

	// iMethod==0 Sta/Lta 1 energyRatio 
	public int pickFirstArrival(int iMethod, int iPhase, int nShortTime, int nLongTime, 
			int startIndexInclusive, int endIndexExclusive) {
		float [] ratio = null;
		if(iMethod==0) ratio = calStaLtaRatio(getData(), nShortTime, nLongTime);
		if(iMethod==1) ratio = calEnergyRatio(getData(), nShortTime, nLongTime);
		float max = ratio[0];
		int maxIndex = 0;
		for(int j=startIndexInclusive; j<endIndexExclusive; j++) {
			if(ratio[j]>max) {
				max = ratio[j];
				maxIndex = j;
			}
		}
		
		if(iPhase==0) {
			_pPick = maxIndex;
		} else if(iPhase==1) {
			_shPick = maxIndex;
		}
		return maxIndex;
	}

	public int calStaLta(int nShortTime, int nLongTime, int iSample, int nSampleBefore, int nSampleAfter) {
		int p1Index = iSample-nSampleBefore;
		int p2Index = iSample+nSampleAfter;
		float [] seisData = cutTrace(p1Index, p2Index);
		return calStaLta(seisData, nShortTime, nLongTime);
	}
	public int calStaLta(float [] seisData, int nShortTime, int nLongTime) {
		float [] ratio = calStaLtaRatio(seisData, nShortTime, nLongTime);
		float max = ratio[0];
		int maxIndex = 0;
		for(int i=1; i<ratio.length; i++) {
			if(ratio[i]>max) {
				max = ratio[i];
				maxIndex = i;
			}
		}
		return maxIndex;
	}
	public float [] calStaLtaRatio(float [] seisData, int nShortTime, int nLongTime) {
        float cSta = 1.0f / nShortTime;
        float sta = 0;
        float cLta = 1.0f / nLongTime;
        float lta = 0;
        
        int N = seisData.length;
        float [] ratioArray = new float[N];
        for(int i=0; i<N; i++)  ratioArray[i] = 0;
        
        float [] v = new float[N];
        for(int i=0; i<N; i++)  v[i] = seisData[i]*seisData[i];
        
        int M = nLongTime;
         
        for(int i=0; i<nLongTime; i++)  lta += (v[i])*cLta ; 
        for(int i=0; i<nShortTime; i++)  sta += (v[i])*cSta ;
        for(int ks0=0, ks1=nShortTime; ks1<M; ks0++, ks1++)  {
        	sta += (v[ks1]-v[ks0])*cSta;
        }
        for(int ks0=nLongTime-nShortTime, ks1=M, kl0=0, kl1=nLongTime; kl1<N; 
        		ks0++, ks1++, kl0++, kl1++)  {
        	sta += (v[ks1]-v[ks0])*cSta;
        	lta += (v[kl1]-v[kl0])*cLta;
        	if(lta!=0) ratioArray[ks1] = sta/lta;
        }
        return ratioArray;
    }
	
	private float [] calEnergyRatio(float [] seisData, int nShortTime, int nLongTime) {
        float cSta = 1.0f / nShortTime;
        float sta = 0;
        float cLta = 1.0f / nLongTime;
        float lta = 0;
        float a = 0;
        
        int N = seisData.length;
        float [] ratioArray = new float[N];
        for(int i=0; i<N; i++)  ratioArray[i] = 0;
        
        float [] v = new float[N];
        for(int i=0; i<N; i++)  v[i] = seisData[i]*seisData[i];
        
        int M = nShortTime+nLongTime; 
         
        for(int i=0; i<nLongTime; i++)  lta += (v[i])*cLta ; 
        for(int i=0; i<nShortTime; i++)  sta += (v[i])*cSta ;
        for(int ks0=0, ks1=nShortTime; ks1<M; ks0++, ks1++)  {
        	sta += (v[ks1]-v[ks0])*cSta;
        }
        for(int ks0=nLongTime, ks1=M, kl0=0, kl1=nLongTime; ks1<N; 
        		ks0++, ks1++, kl0++, kl1++)  {
        	sta += (v[ks1]-v[ks0])*cSta;
        	lta += (v[kl1]-v[kl0])*cLta;
        	if(lta!=0) {        		
        		a = seisData[ks0]*sta/lta;
        		a = a>0?a:-a;
        		ratioArray[ks0] = a;
        	}
        }
        return ratioArray;
    }
	
	public int calMer(float [] seisData, int nTime) {
		float [] ratio = calEnergyRatio(seisData, nTime, nTime);
		float max = ratio[0];
		int maxIndex = 0;
		for(int i=1; i<ratio.length; i++) {
			if(ratio[i]>max) {
				max = ratio[i];
				maxIndex = i;
			}
		}
		return maxIndex;
	}
}
