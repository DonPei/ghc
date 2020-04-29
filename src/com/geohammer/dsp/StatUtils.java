package com.geohammer.dsp;


/**
 * StatUtils provides static methods for computing statistics based on data
 * stored in double[] arrays.
 *
 */
public final class StatUtils {

    /**
     * Private Constructor
     */
    private StatUtils() {
    }

    /**
     * Returns the sum of the values in the input array, or
     * <code>Double.NaN</code> if the array is empty.
     * <p>
     * Throws <code>IllegalArgumentException</code> if the input array
     * is null.</p>
     *
     * @param values  array of values to sum
     * @return the sum of the values or <code>Double.NaN</code> if the array
     * is empty
     * @throws MathIllegalArgumentException if the array is null
     */
    public static double sum(final double[] values) {
    	double sigma = 0.0;
		for (int i = 0; i < values.length; i++) {
			sigma += values[i];
		}
		return sigma;
    }
    public static float sum(final float[] values) {
    	float sigma = 0.0f;
		for (int i = 0; i < values.length; i++) {
			sigma += values[i];
		}
		return sigma;
    }
   

    /**
     * Returns the arithmetic mean of the entries in the input array, or
     * <code>Double.NaN</code> if the array is empty.
     * <p>
     * Throws <code>IllegalArgumentException</code> if the array is null.</p>
     * <p>
     * See {@link org.apache.commons.math3.stat.descriptive.moment.Mean} for
     * details on the computing algorithm.</p>
     *
     * @param values the input array
     * @return the mean of the values or Double.NaN if the array is empty
     * @throws MathIllegalArgumentException if the array is null
     */
    public static double mean(final double[] values) {
        return sum(values)/values.length;
    }
    public static float mean(final float[] values) {
        return sum(values)/values.length;
    }


    /**
     * Returns the variance of the entries in the input array, or
     * <code>Double.NaN</code> if the array is empty.
     *
     * <p>This method returns the bias-corrected sample variance (using {@code n - 1} in
     * the denominator).  Use {@link #populationVariance(double[])} for the non-bias-corrected
     * population variance.</p>
     * <p>
     * See {@link org.apache.commons.math3.stat.descriptive.moment.Variance} for
     * details on the computing algorithm.</p>
     * <p>
     * Returns 0 for a single-value (i.e. length = 1) sample.</p>
     * <p>
     * Throws <code>MathIllegalArgumentException</code> if the array is null.</p>
     *
     * @param values the input array
     * @return the variance of the values or Double.NaN if the array is empty
     * @throws MathIllegalArgumentException if the array is null
     */
    public static double variance(final double[] values) {
    	double mean  = 0.0;
		double sigma = 0.0;
	
		mean = mean(values);
		for(int i=0; i<values.length; i++)  
			sigma += (values[i] - mean)*(values[i] - mean); 
		
		return sigma;
    }
    public static float variance(final float[] values) {
    	float mean  = 0.0f;
    	float sigma = 0.0f;
	
		mean = mean(values);
		for(int i=0; i<values.length; i++)  
			sigma += (values[i] - mean)*(values[i] - mean); 
		
		return sigma;
    }
}
