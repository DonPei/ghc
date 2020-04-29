package com.geohammer.dsp;

import com.geohammer.core.SeismicTraceComponent;

import edu.mines.jtk.util.ArrayMath;

public class DSP {
	//http://www.source-code.biz/dsp/java/
	//http://www.dickbaldwin.com/tocdsp.htm
	//http://show.docjava.com/book/book.htm
	//https://github.com/JorenSix/TarsosDSP

	//http://inside.mines.edu/~dhale/notebook.html	
	//Each output value y[i] is a weighted average of input values x[j]. 
	//The weights are proportional to a^|i-j|, so that (for 0 <= a <= 1) 
	//they decrease exponentially with increasing |i-j|. In this example, the parameter a = 0.932. 
	//it assumes that input values beyond the ends are equal to the end values. 
	//This zero-slope assumption is often appropriate.
	
	//I compute the parameter a to obtain an exponential filter that (for low frequencies) is 
	//comparable to a Gaussian filter with a specified half-width sigma (measured in samples), using: 
	//  float ss = sigma*sigma;
	//  float a = (1.0f+ss-sqrt(1.0f+2.0f*ss))/ss;


	// And if you would rather think in terms of the integer half-width m of a boxcar filter, 
	// then (again, for low frequencies) the half-width sigma of the comparable Gaussian filter can be computed using: 
	//  float sigma = sqrt(m*(m+1)/3.0f);

	// For the boxcar filter with half-width m = 10 displayed in the figure above, 
	// these expressions yield sigma = 6.06 and a = 0.792. 

	public static void exponentialFilterEndingWithZeroSlope(float a, float [] x, float [] f) { exponentialFilter(a, x, 1.0f, f); }
	public static void exponentialFilterEndingWithZeroValue(float a, float [] x, float [] f) { exponentialFilter(a, x, 1.0f-a, f); }
	private static void exponentialFilter(float a, float [] x, float ending, float [] f) {
		int n = x.length;
		float [] y = new float[n];
		float b = 1.0f-a;
		float sx = ending, sy = a;
		float yi = 0.0f;
		y[0] = yi = sy*yi+sx*x[0];
		for (int i=1; i<n-1; ++i)
			y[i] = yi = a*yi+b*x[i];
		sx /= 1.0f+a; sy /= 1.0f+a;
		y[n-1] = yi = sy*yi+sx*x[n-1];
		for (int i=n-2; i>=0; --i)
			y[i] = yi = a*yi+b*y[i];
		
		if(f==null) { for(int i=0; i<n; i++) x[i] = y[i]; }
		else  		{ for(int i=0; i<n; i++) f[i] = y[i]; }
	}
	public static void exponentialFilterEndingWithZeroSlope(double a, double [] x, double [] f) { exponentialFilter(a, x, 1.0f, f); }
	public static void exponentialFilterEndingWithZeroValue(double a, double [] x, double [] f) { exponentialFilter(a, x, 1.0f-a, f); }
	private static void exponentialFilter(double a, double [] x, double ending, double [] f) {
		int n = x.length;
		double [] y = new double[n];
		double b = 1.0-a;
		double sx = ending, sy = a;
		double yi = 0.0;
		y[0] = yi = sy*yi+sx*x[0];
		for (int i=1; i<n-1; ++i)
			y[i] = yi = a*yi+b*x[i];
		sx /= 1.0f+a; sy /= 1.0f+a;
		y[n-1] = yi = sy*yi+sx*x[n-1];
		for (int i=n-2; i>=0; --i)
			y[i] = yi = a*yi+b*y[i];
		
		if(f==null) { for(int i=0; i<n; i++) x[i] = y[i]; }
		else  		{ for(int i=0; i<n; i++) f[i] = y[i]; }
	}
	public static void movingAverage(int nWinLen, float [] x, float [] f) {
		movingAverage(nWinLen, x, f, 0, x.length);
	}
	public static void movingAverage(int nWinLen, float [] x, float [] f, int i1Inclusive, int i2Exclusive) {
		int side = (int)(nWinLen/2.0);
		if(side<=0) return;
		
		int n = x.length;
		float [] y = new float[n];
		for(int i=0; i<n; i++) y[i] = x[i];
		for(int i=i1Inclusive; i<i2Exclusive; i++) {
			int i1 = (i-side)<0?0:(i-side);
			int i2 = (i+side)>=n?(n-1):(i+side);
			float sum = 0;
			for(int j=i1; j<=i2; j++) { sum += x[j]; }
			y[i] = sum/(i2-i1+1);
		}
		if(f==null) { for(int i=0; i<n; i++) x[i] = y[i]; }
		else  		{ for(int i=0; i<n; i++) f[i] = y[i]; }
	}
	
	public static void multiply(float a, float [] x, float [] f) {
		int n = x.length;
		if(f==null) { for(int i=0; i<n; i++) x[i] *= a; }
		else  		{ for(int i=0; i<n; i++) f[i] = a*x[i]; }
	}
		
	//Cumulative sum of an Array
	public static void cumSum(float [] x, float [] f) {
		int n = x.length;
		float [] y = new float[n];
		float sum = 0.0f;
		for (int i=0; i<n; i++) {
			sum += x[i];
			y[i] = sum;
		}
		if(f==null) { for(int i=0; i<n; i++) x[i] = y[i]; }
		else  		{ for(int i=0; i<n; i++) f[i] = y[i]; }
	}
	
	public static void leakyIntegrate(float b, float [] x, float [] f) {
		int n = x.length;
		float [] y = new float[n];
		float a = 1.0f-b;
		y[0] = x[0];
		for (int i=1; i<n; ++i)
			y[i] = a*y[i-1]+x[i];
		
		if(f==null) { for(int i=0; i<n; i++) x[i] = y[i]; }
		else  		{ for(int i=0; i<n; i++) f[i] = y[i]; }
	}
	
	public static float [][] fft(int [] p1p2, SeismicTraceComponent comp) { 
		if(p1p2==null) return null;

		float [] data = null;
		if(p1p2[0]>p1p2[1]) {
			data = comp.cutTrace(p1p2[1], p1p2[0]);
		} else if(p1p2[0]==p1p2[1]) {
			data = ArrayMath.copy(comp.getData());
		} else {
			data = comp.cutTrace(p1p2[0], p1p2[1]);
		}
		//for(int i=0; i<data.length; i++) data[i] *= 1.0e-20;
		double alpha = 0.2;
		AmplitudeCosineTaper.taper(data, alpha);

		comp.fft(data);
		comp.calSpectrum();
		//double r = vcPW.distance(0, i)*factor;
		//comp.limitWithin1000();
		//comp.deAttenuate(qs, vs, r);
		//comp.multiply(comp.getSampleInterval()*(endIndex-beginIndex+1), comp.getSpectrum());
		//comp.multiply(comp.getSampleInterval(), comp.getSpectrum());
		return new float[][]{comp.getFrequency(), comp.getSpectrum()};
	}
	
	/******************************************************************************
	 *  Compilation:  javac Gamma.java
	 *  Execution:    java Gamma 5.6
	 *  
	 *  Reads in a command line input x and prints Gamma(x) and
	 *  log Gamma(x). The Gamma function is defined by
	 *  
	 *        Gamma(x) = integral( t^(x-1) e^(-t), t = 0 .. infinity)
	 *
	 *  Uses Lanczos approximation formula. See Numerical Recipes 6.1.
	 *
	 * https://introcs.cs.princeton.edu/java/91float/Gamma.java.html
	 *
	 ******************************************************************************/
	static double logGamma(double x) {
		double tmp = (x - 0.5) * Math.log(x + 4.5) - (x + 4.5);
		double ser = 1.000000000190015 + 76.18009172947146/(x + 0) - 86.50532032941677/(x + 1)
				+ 24.01409824083091/(x + 2) -  1.231739572450155/(x + 3)
				+  0.1208650973866179e-2/(x + 4)   -  0.5395239384953e-5/(x + 5);
		return tmp + Math.log(ser * Math.sqrt(2 * Math.PI));
	}
	public static double gamma(double x) { return Math.exp(logGamma(x)); }

	//function [h]=taylora2f1(ar,ai,br,bi,cr,ci,zr,zi,tol)
	//By John Pearson  Part of MSc dissertation 'Computation of Hypergeometric Functions'
	//https://www.math.ucla.edu/~mason/research/pearson_final.pdf
	//http://people.maths.ox.ac.uk/~porterm/research/hypergeometricpackage.zip
	//Output: h=Computed value of 2F1(a,b;c;z) 
	public static double hypgeo(double a, double b, double c, double z, double tol) {
		double C0 = 1.0;
		double C1 = C0;
		double S0 = C0;
		double S1 = C0;
		
		double S_1 = C0;
		double S_0 = C0;
		for(int j=1; j<500; j++) {
			//Update value of a1, current term, and b1, sum of all terms in terms of previous values
			C1=C0*((a+j)*(b+j)/(c+j))*(z/(j+1));
			S1 = S0+C1;
			//Terminate summation if stopping criterion is satisfied
			if(j>10 && Math.abs(C1/S0)<tol && Math.abs(C0/S_0)<tol) break;
			//if(j>10 && Math.abs(S1-S0)<tol && Math.abs(S0-S_0)<tol && Math.abs(S_0-S_1)<tol) break;
			//if(j>10 && Math.abs(S1-S0)<tol && Math.abs(S0-S_0)<tol && Math.abs(S_0-S_1)<tol) break;
			S_1 	= S_0;
			S_0 	= S0;
			
			S0 		= S1;
			C0 		= C1;
		}
		return S1;
	}
	public static double hypgeo2(double a, double b, double c, double z, double tol) {
		double a1 = 0.0;
		double a2 = c;
		double a3 = 0.0;
		double b1 = 1.0;
		double b2 = a*b*z;
		double b3 = 0.0;
		double c1 = 1.0;
		double c2 = c;
		double c3 = 0.0;
		double d1 = 1.0;
		double d2 = (c+a*b*z)/c;
		double d3 = 0.0;
		
		for(int j=2; j>1000; j++) {
			//Update value of a1, current term, and b1, sum of all terms in terms of previous values
			a3 = (a2+b2)*j*(c+j-1);
			b3 = b2*(a+j-1)*(b+j-1)*z;
			c3 = c2*j*(c+j-1);
			//Terminate summation if stopping criterion is satisfied
			//if(Math.abs(C1/S0)<tol && Math.abs(C0/S_0)<tol && Math.abs(C_0/S_1)<tol) break;
			if(j>10 && (Math.abs(d3-d2)/Math.abs(d2))<tol && (Math.abs(d2-d1)/Math.abs(d1))<tol) break;
			
			
			d3 = (a3+b3)/c3;
			
			a1 	= a2;
			a2 	= a3;
			b1 	= b2;
			b2 	= b3;
			c1 	= c2;
			c2 	= c3;
			d1 	= d2;
			d2 	= d3;
		}
		return d3;
	}

	
}
