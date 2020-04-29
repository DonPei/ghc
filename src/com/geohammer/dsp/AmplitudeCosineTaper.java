package com.geohammer.dsp;

//http://vault.gps.caltech.edu/trac/cisn/browser/PP/trunk/jungle/src/org/trinet/util/GeneralButterworthFilter.java?rev=428
/** Apply a cosine taper to a time series. */
public class AmplitudeCosineTaper {

	public static double [] getTaperWeights(int nsamples, double alpha) {
		/* alpha between 0. and 1.*/
		//double pi = 4.0 * Math.atan(1.0);
		double pi = Math.PI;
		int midPoint = (nsamples-1)/2;
		int taperLength = (int)((alpha * nsamples)/2.);
		double [] w = new double[taperLength];
		for (int idx =0; idx < taperLength; idx++) {
			double fraction = ((double) idx - alpha*midPoint)/(alpha*midPoint);
			w[idx] = 0.5*(1.0 + Math.cos(pi * fraction));
		}
		return w;
	}
	/** Apply a cosine taper to a float[] time series. */
	public static float [] taper(float [] inValues, double alpha) {
		/* alpha between 0. and 1.*/
		//double pi = 4.0 * Math.atan(1.0);
		int nsamples = inValues.length;
		int midPoint = (nsamples-1)/2;
		int taperLength = (int)((alpha * nsamples)/2.);
		float [] outValues = inValues;
		for (int idx =0; idx < taperLength; idx++) {
			double fraction = ((double) idx - alpha*midPoint)/(alpha*midPoint);
			double weight = 0.5*(1.0 + Math.cos(Math.PI * fraction));
			outValues[idx] = (float) (weight*inValues[idx]);
			outValues[nsamples-idx-1] = (float) (weight*inValues[nsamples-idx-1]);
		}
		return outValues;
	}
	/** Apply a cosine taper to an int[] time series. */
	public static int [] taper(int [] inValues, double alpha) {
		/* alpha between 0. and 1.*/
		//double pi = 4.0 * Math.atan(1.0);
		int nsamples = inValues.length;
		int midPoint = (nsamples-1)/2;
		int taperLength = (int)((alpha * nsamples)/2.);
		int [] outValues = inValues;
		for (int idx =0; idx < taperLength; idx++) {
			double fraction = ((double) idx - alpha*midPoint)/(alpha*midPoint);
			double weight = 0.5*(1.0 + Math.cos(Math.PI * fraction));
			outValues[idx] = (int) (weight*inValues[idx]);
			outValues[nsamples-idx-1] = (int) (weight*inValues[nsamples-idx-1]);
		}
		return outValues;
	}

	public static final void main(String args []) {
		int [] data = new int [1000];
		java.util.Arrays.fill(data, 1000);
		double [] wts = AmplitudeCosineTaper.getTaperWeights(500, 0.1);
		int size = wts.length;
		for (int idx=0; idx<size; idx++) {
			System.out.print(wts[idx]);
			System.out.print(", ");
			if (idx>0 && idx%10 == 0) System.out.println();
		}
		System.out.println();
		data = taper(data, 0.1);
		size = data.length;
		for (int idx=0; idx<size; idx++) {
			System.out.print(data[idx]);
			System.out.print(" ");
			if (idx>0 && idx%10 == 0) System.out.println();
		}
	}
}

