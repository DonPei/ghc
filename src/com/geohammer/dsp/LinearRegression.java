package com.geohammer.dsp;

/******************************************************************************
 *  Compilation:  javac LinearRegression.java
 *  Execution:    java  LinearRegression
 *  Dependencies: none
 *  http://algs4.cs.princeton.edu/14analysis/
 *  Compute least squares solution to y = beta * x + alpha.
 *  Simple linear regression.
 *
 ******************************************************************************/


/**
 *  The {@code LinearRegression} class performs a simple linear regression
 *  on an set of <em>n</em> data points (<em>y<sub>i</sub></em>, <em>x<sub>i</sub></em>).
 *  That is, it fits a straight line <em>y</em> = &alpha; + &beta; <em>x</em>,
 *  (where <em>y</em> is the response variable, <em>x</em> is the predictor variable,
 *  &alpha; is the <em>y-intercept</em>, and &beta; is the <em>slope</em>)
 *  that minimizes the sum of squared residuals of the linear regression model.
 *  It also computes associated statistics, including the coefficient of
 *  determination <em>R</em><sup>2</sup> and the standard deviation of the
 *  estimates for the slope and <em>y</em>-intercept.
 *
 *  @author Robert Sedgewick
 *  @author Kevin Wayne
 */
public class LinearRegression {
    private double intercept, slope;
    private double r2;
    private double svar0, svar1;
    private double [] _x = null;
    private double [] _y = null;

   /**
     * Performs a linear regression on the data points {@code (y[i], x[i])}.
     *
     * @param  x the values of the predictor variable
     * @param  y the corresponding values of the response variable
     * @throws IllegalArgumentException if the lengths of the two arrays are not equal
     */
    public LinearRegression(double[] x, double[] y) {
        if (x.length != y.length) {
            throw new IllegalArgumentException("array lengths are not equal");
        }
        int n = x.length;

        _x = x;
        _y = y;
        
        // first pass
        double sumx = 0.0, sumy = 0.0, sumx2 = 0.0;
        for (int i = 0; i < n; i++) {
            sumx  += x[i];
            sumx2 += x[i]*x[i];
            sumy  += y[i];
        }
        double xbar = sumx / n;
        double ybar = sumy / n;

        // second pass: compute summary statistics
        double xxbar = 0.0, yybar = 0.0, xybar = 0.0;
        for (int i = 0; i < n; i++) {
            xxbar += (x[i] - xbar) * (x[i] - xbar);
            yybar += (y[i] - ybar) * (y[i] - ybar);
            xybar += (x[i] - xbar) * (y[i] - ybar);
        }
        slope  = xybar / xxbar;
        intercept = ybar - slope * xbar;

        // more statistical analysis
        double rss = 0.0;      // residual sum of squares
        double ssr = 0.0;      // regression sum of squares
        for (int i = 0; i < n; i++) {
            double fit = slope*x[i] + intercept;
            rss += (fit - y[i]) * (fit - y[i]);
            ssr += (fit - ybar) * (fit - ybar);
        }

        int degreesOfFreedom = n-2;
        r2    = ssr / yybar;
        double svar  = rss / degreesOfFreedom;
        svar1 = svar / xxbar;
        svar0 = svar/n + xbar*xbar*svar1;
    }

    public double [] getX()  { return _x; }
    public double [] getY()  { return _y; }
    
   /**
     * Returns the <em>y</em>-intercept &alpha; of the best of the best-fit line <em>y</em> = &alpha; + &beta; <em>x</em>.
     *
     * @return the <em>y</em>-intercept &alpha; of the best-fit line <em>y = &alpha; + &beta; x</em>
     */
    public double intercept() {
        return intercept;
    }

   /**
     * Returns the slope &beta; of the best of the best-fit line <em>y</em> = &alpha; + &beta; <em>x</em>.
     *
     * @return the slope &beta; of the best-fit line <em>y</em> = &alpha; + &beta; <em>x</em>
     */
    public double slope() {
        return slope;
    }

   /**
     * Returns the coefficient of determination <em>R</em><sup>2</sup>.
     *
     * @return the coefficient of determination <em>R</em><sup>2</sup>,
     *         which is a real number between 0 and 1
     */
    public double R2() {
        return r2;
    }

   /**
     * Returns the standard error of the estimate for the intercept.
     *
     * @return the standard error of the estimate for the intercept
     */
    public double interceptStdErr() {
        return Math.sqrt(svar0);
    }

   /**
     * Returns the standard error of the estimate for the slope.
     *
     * @return the standard error of the estimate for the slope
     */
    public double slopeStdErr() {
        return Math.sqrt(svar1);
    }

   /**
     * Returns the expected response {@code y} given the value of the predictor
     * variable {@code x}.
     *
     * @param  x the value of the predictor variable
     * @return the expected response {@code y} given the value of the predictor
     *         variable {@code x}
     */
    public double predict(double x) {
        return slope*x + intercept;
    }

   /**
     * Returns a string representation of the simple linear regression model.
     *
     * @return a string representation of the simple linear regression model,
     *         including the best-fit line and the coefficient of determination
     *         <em>R</em><sup>2</sup>
     */
    public String toString() {
        StringBuilder s = new StringBuilder();
        s.append(String.format("%.6f n + %.6f", slope(), intercept()));
        s.append("  (R^2 = " + String.format("%.3f", R2()) + ")");
        return s.toString();
    }

    private float interceptf, slopef;
    private float r2f;
    private float svar0f, svar1f;
    private float [] _xf = null;
    private float [] _yf = null;

   /**
     * Performs a linear regression on the data points {@code (y[i], x[i])}.
     *
     * @param  x the values of the predictor variable
     * @param  y the corresponding values of the response variable
     * @throws IllegalArgumentException if the lengths of the two arrays are not equal
     */
    public LinearRegression(float[] xf, float[] yf) {
        if (xf.length != yf.length) {
            throw new IllegalArgumentException("array lengths are not equal");
        }
        int n = xf.length;

        _xf = xf;
        _yf = yf;
        
        // first pass
        float sumx = 0.0f, sumy = 0.0f, sumx2 = 0.0f;
        for (int i = 0; i < n; i++) {
            sumx  += xf[i];
            sumx2 += xf[i]*xf[i];
            sumy  += yf[i];
        }
        float xbar = sumx / n;
        float ybar = sumy / n;

        // second pass: compute summary statistics
        float xxbar = 0.0f, yybar = 0.0f, xybar = 0.0f;
        for (int i = 0; i < n; i++) {
            xxbar += (xf[i] - xbar) * (xf[i] - xbar);
            yybar += (yf[i] - ybar) * (yf[i] - ybar);
            xybar += (xf[i] - xbar) * (yf[i] - ybar);
        }
        slopef  = xybar / xxbar;
        interceptf = ybar - slopef * xbar;

        // more statistical analysis
        float rss = 0.0f;      // residual sum of squares
        float ssr = 0.0f;      // regression sum of squares
        for (int i = 0; i < n; i++) {
            float fit = slopef*xf[i] + interceptf;
            rss += (fit - yf[i]) * (fit - yf[i]);
            ssr += (fit - ybar) * (fit - ybar);
        }

        int degreesOfFreedom = n-2;
        r2f    = ssr / yybar;
        float svar  = rss / degreesOfFreedom;
        svar1f = svar / xxbar;
        svar0f = svar/n + xbar*xbar*svar1f;
    }

    public float [] getXf()  						{ return _xf; }
    public float [] getYf()  						{ return _yf; }
    public float interceptf() 						{ return interceptf; }
    public float slopef() 							{ return slopef; }
    public float R2f() 								{ return r2f; }
    public float interceptStdErrf() 				{ return (float)Math.sqrt(svar0f); }
    public float slopeStdErrf() 					{ return (float)Math.sqrt(svar1f);}
    public float predict(float x) 					{ return slopef*x + interceptf; }

    public String toStringf() {
        StringBuilder s = new StringBuilder();
        s.append(String.format("%.6f n + %.6f", slopef(), interceptf()));
        s.append("  (R^2 = " + String.format("%.3f", R2f()) + ")");
        return s.toString();
    }
}

