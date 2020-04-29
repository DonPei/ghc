package com.geohammer.dsp;

import static edu.mines.jtk.util.ArrayMath.randfloat;
import static edu.mines.jtk.util.ArrayMath.zerofloat;

import java.awt.Color;
import java.util.Arrays;
import java.util.Random;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import com.geohammer.common.CommonFrame;
import com.geohammer.common.CommonPanel;
import com.geohammer.component.ScatterPlotDialog;
import com.geohammer.core.MatrixFileReader;
import com.geohammer.core.SeismicTraceComponent;
import com.geohammer.core.planarmodel.DipLayer1D;
import com.geohammer.vc.dialog.PlotDialog;

import edu.mines.jtk.dsp.Conv;
import edu.mines.jtk.dsp.Fft;
import edu.mines.jtk.dsp.Sampling;
import edu.mines.jtk.mosaic.PlotPanel;
import edu.mines.jtk.mosaic.PointsView;
import edu.mines.jtk.util.ArrayMath;
import edu.mines.jtk.util.RandomFloat;

public class DspTest implements Runnable {

	//private RandomFloat 	_random 	= new RandomFloat(19580427);
	//private Random 	_random 	= new Random();
	private RandomFloat 	_random 	= new RandomFloat();
	public DspTest() {

	}
	public void run() {	
		//test1();
		//test2();
		//powerLaw3();
		//singleFreqency();
		//testRotation();
		//testExponentialFilter();
		//testBruneOrBoatwright();
		//testMtaper();
		//testGoto();
		//testMtaperPlot();
		//testXcorr();
		//testPhaseWrapping();
		//testNoisyPhaseWrapping();
		//testPhaseWrappingUnderSampling();
		//testWavelet();
		//testCepstrum();
		testLinearRegression();
		System.out.println("Successfully Done!");
	}
	public static void main(String[] args) {
		System.out.println("Executing Test()");
		SwingUtilities.invokeLater( new DspTest() );
	}

	public void testXcorr() {
		int lx = 4;
		int ly = 5;
		int lz = lx+ly-1;
		int kx = 0;
		int ky = 0;
		int kz = 1-lx;
		//int kz = 0;
		float[] x = new float[]{1, 3, -2, 4};
		//float[] y = new float[]{1, 3, -2, 4};
		float[] y = new float[]{2, 3, -1, 3, 6};
		float[] zf = zerofloat(lz);
		Conv.xcor(lx,kx,x,ly,ky,y,lz,kz,zf);
		System.out.println(Arrays.toString(zf));
		//[8.0, 8.0, -4.0, 25.0, 18.0, -4.0, 21.0, 6.0]
	}
	public void testCepstrum() {
		// signal1 njeff.dat O18.dat
		String fileName = "C:\\arima\\mti\\techReview\\mtm\\MTM_2006.09.18.tar\\MTM\\signal1";
		
		float [] x = Mtaper.read(fileName);
		double dt = Mtaper.readDt(fileName);
		double nyquist = 1.0/(2.0*dt);
		System.out.println("dt="+dt+" nyquist="+nyquist);
		
		Cepstrum cepstrum = new Cepstrum();
		cepstrum.removeMean(x);
		//float [] y = cepstrum.calCepstrum(-1, x, dt, 0.1);
		float [] y = cepstrum.calCepstrum(x, dt, 0.1);
		Sampling sampling = cepstrum.getFrequencySampling();
		
		int iPlot = 0;
		String aTitle = new String(" Cross Plot");
		String catalog = "Cross";
		String vLabel = "Power Spectrum (dB)";
		String hLabel = "Frequency (Hz)";

		CommonFrame frame = new CommonFrame();
		frame.setSize(20,20);
		frame.setVisible(true);
		if(iPlot==0) {
		} else if(iPlot==1) {
		} else if(iPlot==2) {
			vLabel = "Degree Of Freedom";
		} else if(iPlot==3) {
			vLabel = "F-Test";
		}

		PlotDialog dialog = new PlotDialog(frame, aTitle, false, null,
				100, false, catalog, hLabel, vLabel);

		if(iPlot==0) {
			//float [] a = new float[x.length];
			//for(int i=0; i<a.length; i++) a[i] = (float)(i*dt);	
			float [] a = new float[sampling.getCount()];
			for(int i=0; i<a.length; i++) a[i] = (float)(sampling.getValue(i));
			dialog.setCurves(new float[][]{y, a});
		} 
//		else if(iPlot==1) {
//			float [] a = taper.getData();
//			float [] b = new float[a.length];
//			for(int i=0; i<b.length; i++) b[i] = i;		
//			dialog.setCurves(new float[][]{a, b});
//		} else if(iPlot==2) {
//			vLabel = "Degree Of Freedom";
//			float [] a = taper.getDegreeOfFreedom();
//			float [] b = new float[sampling.getCount()];
//			for(int i=0; i<b.length; i++) b[i] = (float)sampling.getValue(i);	
//			dialog.setCurves(new float[][]{a, b});
//		} else if(iPlot==3) {
//			vLabel = "F-Test";
//			float [] a = taper.getFTest();
//			float [] b = new float[sampling.getCount()];
//			for(int i=0; i<b.length; i++) b[i] = (float)sampling.getValue(i);	
//			dialog.setCurves(new float[][]{a, b});
//		}
		dialog.showDialog();
	}
	
	public void testMtaper() {
		// signal1 njeff.dat O18.dat
		String fileName = "C:\\arima\\mti\\techReview\\mtm\\MTM_2006.09.18.tar\\MTM\\signal1";
		Mtaper taper = new Mtaper(fileName, 3.0f, 5, 2, 0, true);
		float [] specMultiple = taper.multiTaper();
		float [] specSingle = taper.singleTaper();
		Sampling sampling = taper.getFrequencySampling();

		for (int i = 0; i<sampling.getCount(); i++) {
			specMultiple[i] = (float)(10.0 * Math.log10(specMultiple[i]));
			specSingle[i] = (float)(10.0 * Math.log10(specSingle[i]));
			//double a = (10.0 * Math.log10(spec[i]*spec[i]));
			//double a = specSingle[i];
			//double a = specMultiple[i];
			//System.out.println(i+" f=" + sampling.getValue(i) + " s=" + a);
		}

		float [][] specFFT = taper.singleTaperByFFT();
		for (int i = 0; i<specFFT[1].length; i++) {
			specFFT[1][i] = (float)(10.0 * Math.log10(specFFT[1][i]));
			//System.out.println(i+" f=" + specFFT[0][i]+" s=" + specFFT[1][i]);
		}
		int iPlot = 3;
		String aTitle = new String(" Cross Plot");
		String catalog = "Cross";
		String vLabel = "Power Spectrum (dB)";
		String hLabel = "Frequency (Hz)";

		CommonFrame frame = new CommonFrame();
		frame.setSize(20,20);
		frame.setVisible(true);
		if(iPlot==0) {
		} else if(iPlot==1) {
		} else if(iPlot==2) {
			vLabel = "Degree Of Freedom";
		} else if(iPlot==3) {
			vLabel = "F-Test";
		}

		PlotDialog dialog = new PlotDialog(frame, aTitle, false, null,
				100, false, catalog, hLabel, vLabel);

		if(iPlot==0) {
			float [] a = new float[sampling.getCount()];
			for(int i=0; i<a.length; i++) a[i] = (float)sampling.getValue(i);
			float [] b = new float[sampling.getCount()];
			for(int i=0; i<b.length; i++) b[i] = (float)sampling.getValue(i);		
			dialog.setCurves(new float[][]{specMultiple, a, specSingle, b, specFFT[1], specFFT[0]});
		} else if(iPlot==1) {
			float [] a = taper.getData();
			float [] b = new float[a.length];
			for(int i=0; i<b.length; i++) b[i] = i;		
			dialog.setCurves(new float[][]{a, b});
		} else if(iPlot==2) {
			vLabel = "Degree Of Freedom";
			float [] a = taper.getDegreeOfFreedom();
			float [] b = new float[sampling.getCount()];
			for(int i=0; i<b.length; i++) b[i] = (float)sampling.getValue(i);	
			dialog.setCurves(new float[][]{a, b});
		} else if(iPlot==3) {
			vLabel = "F-Test";
			float [] a = taper.getFTest();
			float [] b = new float[sampling.getCount()];
			for(int i=0; i<b.length; i++) b[i] = (float)sampling.getValue(i);	
			dialog.setCurves(new float[][]{a, b});
		}
		dialog.showDialog();
	}
	public void testGoto() {
		int k = 0;
		int k1 = 0;
		boolean kk = true;

		L320: do {
			System.out.println("L320 k="+ k);
			if(k<3) continue L320;

			L50: do {
				System.out.println("L50 k="+ k);
				if(k<5) break L50;

				L60: {
					System.out.println("L100 k="+ k);
				}
				k++;
			} while(k<20);

			k++;

		} while(k<20);


		ahead: {
			System.out.println("Before break");
			break ahead;
		}

	}
	public void testMtaperPlot() {
		String fileName = "C:\\arima\\mti\\techReview\\mtm\\MTM_2006.09.18.tar\\MTM\\spec.out";
		MatrixFileReader reader = new MatrixFileReader();
		reader.readTextualData(fileName, 0, 0, 0, null, "[ ]+");
		//reader.printClass();

		int nRow = reader.getRowCount();
		int nCol = reader.getColumnCount();
		//System.out.println("nRow="+nRow+" nCol="+nCol);		

		float [] frequency = new float[nRow];
		float [][] y = new float[5][nRow];
		for(int i=0; i<nRow; i++) {
			frequency[i] = Float.parseFloat(reader.getData(i, 1));	
			for(int j=0; j<5; j++)
				y[j][i] = Float.parseFloat(reader.getData(i, j+2));
		}

		String aTitle = new String(" Cross Plot");
		String catalog = "Cross";
		String vLabel = "Power Spectrum (dB)";
		String hLabel = "Frequency (Hz)";

		CommonFrame frame = new CommonFrame();
		frame.setVisible(true);
		//frame.setSize(20,20);
		CommonPanel panel = new CommonPanel(5, 1);
		for(int j=0; j<5; j++) panel.addPoints(j, 0, frequency, y[j]);
		frame.add(panel);


		//		PlotDialog dialog = new PlotDialog(frame, aTitle, false, null,
		//				100, false, catalog, hLabel, vLabel);
		//		dialog.setCurves(new float[][]{y[0], frequency});
		//
		//		dialog.showDialog();
	}

	public void testExponentialFilter() {
		double dt = 0.00025;
		double nyquest = 1.0/dt;
		double dNyquest = 1;
		int nNyquest = (int)(nyquest/dNyquest)+1;

		double amp0 = 1.0;
		double freq0 = 2.5;
		double K = 0.0;
		int nt = 2000;
		double [] trace = new double[nt];
		genMinimumPhase(dt, amp0, freq0, K, trace);

		for(int j=0; j<nt; j++) trace[j] += amp0*_random.uniform();

		float [] x = new float[nt];
		float [] y = new float[nt];

		for(int i=0; i<nt; i++) {
			x[i] = (float)(i);
			y[i] = (float)(trace[i]);
		}

		float a = 0.17f;
		DSP.exponentialFilterEndingWithZeroSlope(a, y, null);

		String aTitle = "Time";
		String xAxisLabel = "No";
		String yAxisLabel = "Amp";

		JFrame frame = new JFrame();
		frame.setSize(300, 300); 
		frame.setLocationRelativeTo(null);  
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); 
		frame.setVisible(true);

		plotLines(frame, x, y, aTitle, xAxisLabel, yAxisLabel, 0, 0);
	}
	public void testBruneOrBoatwright() {
		double dt = 0.00025;
		double nyquest = 1.0/dt;
		double dNyquest = 1;
		int nNyquest = (int)(nyquest/dNyquest)+1;

		double amp0 = 1.0;
		double freq0 = 200;
		double K = 0.0;
		int nt = 2000;
		double [] trace = new double[nt];
		genMinimumPhase(dt, amp0, freq0, K, trace);

		for(int j=0; j<nt; j++) trace[j] += amp0*_random.uniform();

		float [] x = new float[nt];
		float [] y = new float[nt];

		double gamma = 2.0;
		double n = 2.0;
		double f = 0;
		//double q = 1.0e10;
		double q = 200;
		double vs = 2500.0; //m/s
		double r = 300.0; //m
		double a = 0;
		double b = 0;

		for(int i=0; i<nt; i++) {
			f = i+1.0;
			a = 1.0+Math.pow(f/freq0, n*gamma);
			b = Math.exp(3.1415926*f*r/(vs*q));
			x[i] = (float)(f);
			y[i] = (float)(amp0*b/(Math.pow(a, gamma)));
		}

		String aTitle = "Time";
		String xAxisLabel = "No";
		String yAxisLabel = "Amp";

		JFrame frame = new JFrame();
		frame.setSize(300, 300); 
		frame.setLocationRelativeTo(null);  
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); 
		frame.setVisible(true);

		//plotLines(frame, x, y, aTitle, xAxisLabel, yAxisLabel, 0, 0);

		float[] bx = new float[x.length];
		float[] by = new float[x.length];
		for(int i=0; i<x.length; i++) {
			if(x[i]==0) bx[i] = 0;
			else bx[i] = (float)(Math.log10(x[i]));
			if(y[i]==0) by[i] = 0;
			else by[i] = (float)(Math.log10(y[i]));
			//by[i] = (y[i]);
		}
		aTitle = "Frequency-log";
		plotLines(frame, bx, by, aTitle, xAxisLabel, yAxisLabel, 0, 0);
	}
	
	public void testLinearRegression() {
		//double [] x = new double[] {1, 1.1, 1.2, 1.3, 1.4, 1.5, 1.6, 1.7, 1.8, 1.9};
		double [] x = new double[] {1, 2, 4, 7, 9, 12, 14, 15, 17, 20};
		double [] y = new double[] {20, 19.5, 17, 15, 14, 13.5, 12.4, 11.6, 11, 9};
		double minX = ArrayMath.min(x);
		double maxX = ArrayMath.max(x);
		double dx = maxX-minX;
		double meanX = (maxX+minX)/2.0;
		double minY = ArrayMath.min(y);
		double maxY = ArrayMath.max(y);
		double dy = maxY-minY;
		double meanY = (maxY+minY)/2.0;
		
		double azimuth = 0;
		boolean x1y2 = dx>=dy;
		if(x1y2) {
			LinearRegression linearRegression = new LinearRegression(x, y);
			//-0.544817 n + 19.802650  (R^2 = 0.969)
			System.out.println(linearRegression.toString());		

			double centerX = meanX;
			double centerY = linearRegression.predict(centerX);

			for(int i=0; i<x.length; i++) {
				x[i] -= centerX;
				y[i] -= centerY;
			}
			
			double slope = linearRegression.slope();
			double theCos = 1.0/Math.sqrt(1+slope*slope);
			double theta = Math.acos(theCos)*180.0/Math.PI;
			if(slope>0) {
				azimuth = 90-theta;
			} else {
				azimuth = 90+theta;
			}
		} else {
			LinearRegression linearRegression = new LinearRegression(y, x);
			//-0.544817 n + 19.802650  (R^2 = 0.969)
			System.out.println(linearRegression.toString());		

			double centerY = meanY;
			double centerX = linearRegression.predict(centerY);

			for(int i=0; i<x.length; i++) {
				x[i] -= centerX;
				y[i] -= centerY;
			}

			double slope = linearRegression.slope();
			double theCos = 1.0/Math.sqrt(1+slope*slope);
			double theta = Math.acos(theCos)*180.0/Math.PI;
			if(slope>0) {
				azimuth = theta;
			} else {
				azimuth = 180-theta;
			}
		}
		System.out.println("azimuth="+azimuth+" x1y2="+x1y2);
		
		//rotate
		double theCos = Math.cos(azimuth*Math.PI/180.0);
		double theSin = Math.sin(azimuth*Math.PI/180.0);
		double [] xprime = new double[x.length];
		double [] yprime = new double[x.length];
		
		for(int i=0; i<x.length; i++) {
			xprime[i] = x[i]*theCos - y[i]*theSin;
			yprime[i] = x[i]*theSin + y[i]*theCos;
		}
		
		System.out.println(Arrays.toString(xprime));
		System.out.println(Arrays.toString(yprime));
		
		String aTitle = "Scatter Plot";
		String xAxisLabel = "No";
		String yAxisLabel = "Amp";

		CommonFrame frame = new CommonFrame();
		frame.setSize(300, 300); 
		frame.setLocationRelativeTo(null);  
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); 
		frame.setVisible(true);
		
		ScatterPlotDialog dialog = new ScatterPlotDialog(frame, aTitle, false, null,
				100, false, "Scatter", "Y", "X");
		dialog.setCurves(new double [][] {yprime, xprime});
		dialog.showDialog();
		
	}
	
	public void rotate(double azimuth){
		double ang = azimuth*Math.PI/180.0;
		double theSin = Math.sin(ang);
		double theCos = Math.cos(ang);
		double[][] M = new double[][] {{theCos, -theSin},{theSin, theCos}}; 
		//double[][] M = new double[][] {{theCos, theSin},{-theSin, theCos}};
		double ax = 0.0;
		double ay = 0.0;
		for(int i=0, k=0; i<nEvents; i++) {
			ax = M[0][0]*eE[i]+M[0][1]*eN[i]; //xprime
			ay = M[1][0]*eE[i]+M[1][1]*eN[i];
			eE[i] = ax;
			eN[i] = ay;
			for(int j=0; j<flag[i]; j++, k++) {
				ax = M[0][0]*rE[k]+M[0][1]*rN[k];
				ay = M[1][0]*rE[k]+M[1][1]*rN[k];
				rE[k] = ax;
				rN[k] = ay;
			}
		}
	}
	
	public void test1() {
		double dt = 0.00025;
		double nyquest = 1.0/dt;
		double dNyquest = 1;
		int nNyquest = (int)(nyquest/dNyquest)+1;

		double amp0 = 1.0;
		double freq0 = 500.0;
		double K = 50.0;
		int nt = 1000;
		double [] trace = new double[nt];
		double [] sum = new double[nt];

		double exponent = -2;
		double minFrquency = freq0;
		MersenneTwister generator = new cern.jet.random.engine.MersenneTwister(new java.util.Date());

		double aMean = freq0; 
		double aVariance = freq0/2.5;
		double rFreq = 0;

		int nF = 2000;
		for(int i=0; i<nF; i++) {
			rFreq = aMean * (_random.uniform());
			//System.out.println("f="+rFreq);
			//rFreq = aMean + _random.nextGaussian() * aVariance;
			//rFreq = aMean + _random.normal() * aVariance;
			if(rFreq>aMean) {
			} else {
				//rFreq = aMean * (_random.uniform());
				//rFreq = Distributions.nextPowLaw(exponent, minFrquency, generator);;
			}

			genMinimumPhase(dt, amp0, rFreq, K, sum);
			for(int j=0; j<nt; j++) trace[j] += sum[j];
		}	
		for(int j=0; j<nt; j++) trace[j] /= nF;

		float [] x = new float[nt];
		float [] y = new float[nt];

		for(int i=0; i<nt; i++) {
			x[i] = (float)(i);
			y[i] = (float)(trace[i]);
		}

		String aTitle = "Time";
		String xAxisLabel = "No";
		String yAxisLabel = "Amp";

		JFrame frame = new JFrame();
		frame.setSize(300, 300); 
		frame.setLocationRelativeTo(null);  
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); 
		frame.setVisible(true);

		plotLines(frame, x, y, aTitle, xAxisLabel, yAxisLabel, 0, 0);

		SeismicTraceComponent comp = new SeismicTraceComponent(dt, y);
		int beginIndex = 0;
		int endIndex = 0;
		//System.out.println("sPick="+sPick+" beginIndex="+beginIndex+" endIndex="+endIndex);
		comp.cosineTaperThenfft(beginIndex, endIndex, 0.5);
		//comp.fft(beginIndex, endIndex);
		comp.calSpectrum();

		float[] ax = comp.getFrequency();
		float[] ay = comp.getSpectrum();
		//		for(int i=0; i<ax.length; i++) {
		//    		if(ax[i]==0) ax[i] = 0;
		//    		else ax[i] = (float)(Math.log10(ax[i]));
		//    		if(ay[i]==0) ay[i] = 0;
		//    		else ay[i] = (float)(Math.log10(ay[i]));
		//    	}
		aTitle = "Frequency-linear";
		plotLines(frame, ax, ay, aTitle, xAxisLabel, yAxisLabel, 0, 0);

		float[] bx = new float[ax.length];
		float[] by = new float[ax.length];
		for(int i=0; i<ax.length; i++) {
			if(ax[i]==0) bx[i] = 0;
			else bx[i] = (float)(Math.log10(ax[i]));
			if(ay[i]==0) by[i] = 0;
			else by[i] = (float)(Math.log10(ay[i]));
		}
		aTitle = "Frequency-log";
		plotLines(frame, bx, by, aTitle, xAxisLabel, yAxisLabel, 0, 0);
	}

	public void genMinimumPhase(double dt, double amp0, double freq0, double K, double [] trace) {
		double t = 0;
		double a = 2.0*Math.PI*freq0;
		double q = 10.0;
		double c = Math.PI*freq0/q;
		for(int i=0; i<trace.length; i++) { 
			t = i*dt;
			//trace[i] = amp0*Math.sin(a*t)*Math.exp(-K*t);
			//trace[i] = amp0*Math.sin(a*t)*Math.exp(-K*t)*Math.exp(-c*t); 
			trace[i] = amp0*Math.sin(a*t);
		}
	}

	private void plotLines(JFrame frame, float [] ax, float [] ay, 
			String aTitle, String xAxisLabel, String yAxisLabel, double delta, double epsilon) { 
		PointsPlotDialog dialog = new PointsPlotDialog(frame, aTitle, 2, ax, ay, xAxisLabel, yAxisLabel);
		dialog.setLineOn(true);
		dialog.setMarkOn(false);
		//dialog.setLineOn(false);
		//dialog.setMarkOn(true);
		dialog.showDialog();
	}

	public void genSingleMinimumPhase(double dt, double amp0, double freq0, double K, double [] trace) {
		double t = 0;
		double a = 2.0*Math.PI*freq0;
		for(int i=0; i<trace.length; i++) { 
			t = i*dt;
			trace[i] = amp0*Math.sin(a*t)*Math.exp(-K*t); 
		}
	}
	public void singleFreqency() {
		double dt = 0.00025;
		double nyquest = 1.0/dt;
		double dNyquest = 1;
		int nNyquest = (int)(nyquest/dNyquest)+1;

		double amp0 = 1.0;
		double freq0 = 200.0;
		double K = 200.0;
		int nt = 150;
		String aTitle = "Time";
		String xAxisLabel = "Sample No";
		String yAxisLabel = "Amplitude";

		JFrame frame = new JFrame();
		frame.setSize(300, 300); 
		frame.setLocationRelativeTo(null);  
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); 
		frame.setVisible(true);
		frame.setSize(500, 300);

		PlotPanel panel = new PlotPanel(PlotPanel.Orientation.X1RIGHT_X2UP);
		panel.setBackground(Color.WHITE);
		panel.setVLabel(yAxisLabel);
		panel.setHLabel(xAxisLabel);

		double [] decayK = new double[]{200, 500, 1000};
		String [] style = new String[]{"b--.", "r-.", "k-"};
		for(int i=0; i<decayK.length; i++) {
			//for(int i=0; i<1; i++) {
			double [] trace = new double[nt];
			genSingleMinimumPhase(dt, amp0, freq0, decayK[i], trace);

			float [] x = new float[nt];
			float [] y = new float[nt];

			for(int j=0; j<nt; j++) {
				x[j] = (float)(j);
				y[j] = (float)(trace[j]);
			}
			//plotLines(frame, x, y, aTitle, xAxisLabel, yAxisLabel, 0, 0);

			PointsView pv = panel.addPoints(x,y);
			pv.setStyle(style[i]);
			//pv.setLineStyle(PointsView.Line.SOLID);
			//pv.setMarkStyle(PointsView.Mark.NONE);
			//		if(_lineOn) {
			//			pv.setLineStyle(PointsView.Line.SOLID);
			//		} else {
			//			pv.setLineStyle(PointsView.Line.NONE);
			//		}
			//		if(_markOn) {
			//			
			//		} else {
			//			pv.setMarkStyle(PointsView.Mark.NONE);
			//		}
			//	    pv.setMarkSize(pv.getMarkSize()*0.1f);
			//	    pv.setLineStyle(PointsView.Line.NONE);
			//	    pv.setMarkStyle(PointsView.Mark.FILLED_CIRCLE);
			//	    pv.setTextFormat("%4.2f");
			pv.setLineWidth(2);
		}

		frame.add( panel );
		//UiUtil.centerOnParentAndShow( this );

		//setVisible(true);

	}

	public int getNumOfElement(double min, double max, double [] v) {
		int k = 0;
		for(int i=0; i<v.length; i++) {
			if(v[i]>=min && v[i]<max) k++;
		}
		return k;
	}
	public void plotDistribution(int nBin, double [] v) { 
		double min = ArrayMath.min(v);
		double max = ArrayMath.max(v);
		double dv = (max-min)/(nBin);
		System.out.println("min="+min+" max="+max+" dv="+dv);
		float [] ax = new float[nBin];
		float [] ay = new float[nBin];
		float sum = 0;
		for(int i=0; i<nBin; i++) {
			ax[i] = (float)(min+i*dv);
			ay[i] = (float)getNumOfElement(min+i*dv, min+(i+1)*dv, v);
			sum += ay[i];
		}
		for(int i=0; i<nBin; i++) {ay[i] /= sum;}		

		float[] bx = new float[ax.length];
		float[] by = new float[ax.length];
		for(int i=0; i<ax.length; i++) {
			if(ax[i]==0) bx[i] = 0;
			else bx[i] = (float)(Math.log10(ax[i]));
			if(ay[i]==0) by[i] = 0;
			else by[i] = (float)(Math.log10(ay[i]));
		}

		String aTitle = "Distribution";
		String xAxisLabel = "min";
		String yAxisLabel = "Percentage";

		JFrame frame = new JFrame();
		frame.setSize(300, 300); 
		frame.setLocationRelativeTo(null);  
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); 
		frame.setVisible(true);

		PlotPanel panel = new PlotPanel(PlotPanel.Orientation.X1RIGHT_X2UP);
		panel.setBackground(Color.WHITE);
		panel.setVLabel(yAxisLabel);
		panel.setHLabel(xAxisLabel);
		//panel.setVLimits(0, 1);
		//panel.setHLimits(100, 1000);
		PointsView pv = panel.addPoints(bx,by);
		pv.setStyle("r-");

		frame.add(panel);
		//plotLines(frame, ax, ay, aTitle, xAxisLabel, yAxisLabel, 0, 0);
	}
	public void gamma() {
		// define distribution parameters
		double mean = 5;
		double variance = 1.5;
		double alpha = mean*mean / variance; 
		double lambda = 1 / (variance / mean); 

		// for tests and debugging use a random engine with CONSTANT seed --> deterministic and reproducible results
		cern.jet.random.engine.RandomEngine engine = new cern.jet.random.engine.MersenneTwister(); 

		// your favourite distribution goes here
		cern.jet.random.AbstractDistribution dist = new cern.jet.random.Gamma(alpha,lambda,engine);

		// collect random numbers and print statistics
		int size = 100000;
		cern.colt.list.DoubleArrayList numbers = new cern.colt.list.DoubleArrayList(size);
		//for (int i=0; i < size; i++) numbers.add(dist.nextDouble());
		for (int i=0; i < size; i++) numbers.add(Distributions.nextPowLaw(-2, 200, engine));

		hep.aida.bin.DynamicBin1D bin = new hep.aida.bin.DynamicBin1D();
		bin.addAllOf(numbers);
		System.out.println(bin);
	}
	public void powerLaw() {
		//		gamma();
		double exponent = 10;
		double minFrquency = 1;
		MersenneTwister generator = new cern.jet.random.engine.MersenneTwister(new java.util.Date());
		cern.jet.random.AbstractDistribution dist = new Normal(0,0.3,generator);
		int nf = 2000;
		double [] vx = new double[nf]; //freq
		double [] vy = new double[nf]; //omega
		for (int i=0; i<nf; i++ ) {
			vx[i] = (minFrquency+i);
			vy[i] = Distributions.nextPowLaw(exponent, minFrquency, generator); 
			//vy[i] = Distributions.nextCauchy(generator);
			//vy[i] = generator.nextDouble();
			//vy[i] = generator.nextDouble();
			//vy[i] = dist.nextDouble();
		}
		plotDistribution(100, vy);
	}
	public void powerLaw2() {
		//		gamma();
		double exponent = 10;
		double minFrquency = 200;
		MersenneTwister generator = new cern.jet.random.engine.MersenneTwister(new java.util.Date());
		cern.jet.random.AbstractDistribution dist = new Normal(0,0.3,generator);
		int nf = 2000;
		double [] vx = new double[nf]; //freq
		double [] vy = new double[nf]; //omega
		for (int i=0; i<nf; i++ ) {
			vx[i] = (minFrquency+i);
			vy[i] = Distributions.nextPowLaw(exponent, minFrquency, generator); 
			//vy[i] = Distributions.nextCauchy(generator);
			//vy[i] = generator.nextDouble();
			//vy[i] = generator.nextDouble();
			//vy[i] = dist.nextDouble();
		}
		// plotDistribution(1000, vy);
		//int n = 1;
		//if(n==1) return;

		int mf = (int)(minFrquency-1);
		double amp0 = 1.0;
		double K = 200.0;
		int nt = 5000;
		double [] trace = new double[nt];
		double [] sum = new double[nt];
		double dt = 0.00025;
		double nyquest = 1.0/dt;
		double dNyquest = 1;
		int nNyquest = (int)(nyquest/dNyquest)+1;

		//    	double fs = 1000;     // Sampling rate [Hz]
		//    	double Ts = 1/fs;     // Sampling period [s]
		//    	double fNy = fs / 2;  // Nyquist frequency [Hz]
		//    	double duration = 10; // Duration [s]
		//    	//t = 0 : Ts : duration-Ts; // Time vector
		//    	double noSamples = length(t);    // Number of samples

		double f0 = minFrquency;
		for(int i=0; i<nNyquest; i++) {
			double f = 1+i;
			//if(f>f0) genMinimumPhase(dt, amp0/(f0*f), f, K, sum);
			//else genMinimumPhase(dt, amp0, f, K, sum);
			genMinimumPhase(dt, amp0, f, K, sum);
			for(int j=0; j<nt; j++) trace[j] += sum[j];
		}
		for(int j=0; j<nt; j++) trace[j] /= (nNyquest);
		//    	for(int i=0; i<mf; i++) {
		//    		 double f = 1+i;
		//     		genMinimumPhase(dt, amp0, f, K, sum);
		//     		for(int j=0; j<nt; j++) trace[j] += sum[j];
		// 	    }
		//    	nf = 0;
		//    	for(int i=0; i<nf; i++) {
		//   		 	double f = vy[i];
		//    		genMinimumPhase(dt, 1.0, f, K, sum);
		//    		for(int j=0; j<nt; j++) trace[j] += sum[j];
		//	    }
		//    	for(int j=0; j<nt; j++) trace[j] /= (nf+mf);

		float [] x = new float[nt];
		float [] y = new float[nt];

		for(int i=0; i<nt; i++) {
			x[i] = (float)(i);
			y[i] = (float)(trace[i]);
		}
		String aTitle = "Time";
		String xAxisLabel = "No";
		String yAxisLabel = "Amp";

		JFrame frame = new JFrame();
		frame.setSize(300, 300); 
		frame.setLocationRelativeTo(null);  
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); 
		frame.setVisible(true);

		plotLines(frame, x, y, aTitle, xAxisLabel, yAxisLabel, 0, 0);

		SeismicTraceComponent comp = new SeismicTraceComponent(dt, y);
		int beginIndex = 0;
		int endIndex = 0;
		comp.cosineTaperThenfft(beginIndex, endIndex, 0.5);
		comp.calSpectrum();

		float[] ax = comp.getFrequency();
		float[] ay = comp.getSpectrum();
		//		for(int i=0; i<ax.length; i++) {
		//    		if(ax[i]==0) ax[i] = 0;
		//    		else ax[i] = (float)(Math.log10(ax[i]));
		//    		if(ay[i]==0) ay[i] = 0;
		//    		else ay[i] = (float)(Math.log10(ay[i]));
		//    	}
		aTitle = "Frequency-linear";
		plotLines(frame, ax, ay, aTitle, xAxisLabel, yAxisLabel, 0, 0);

		float[] bx = new float[ax.length];
		float[] by = new float[ax.length];
		for(int i=0; i<ax.length; i++) {
			if(ax[i]==0) bx[i] = 0;
			else bx[i] = (float)(Math.log10(ax[i]));
			if(ay[i]==0) by[i] = 0;
			else by[i] = (float)(Math.log10(ay[i]));
		}
		aTitle = "Frequency-log";
		plotLines(frame, bx, by, aTitle, xAxisLabel, yAxisLabel, 0, 0);		
	}
	public void powerLaw3() {    	 
		double amp0 = 1.0e8;
		double K = 200.0;
		int nt = 10000;
		double [] trace = new double[nt];
		double [] sum = new double[nt];
		double dt = 0.00025;
		double nyquest = 0.5/dt;
		double dNyquest = 2;
		int nNyquest = (int)(nyquest/dNyquest)+1;

		//    	double fs = 1000;     // Sampling rate [Hz]
		//    	double Ts = 1/fs;     // Sampling period [s]
		//    	double fNy = fs / 2;  // Nyquist frequency [Hz]
		//    	double duration = 10; // Duration [s]
		//    	//t = 0 : Ts : duration-Ts; // Time vector
		//    	double noSamples = length(t);    // Number of samples

		int nf = 1000;
		double f0 = 200;
		for(int i=0; i<nf; i++) {
			double f = 1+i;
			if(f>f0) genMinimumPhase(dt, amp0/(0.05*f0*f), f, K, sum);
			else genMinimumPhase(dt, amp0, f, K, sum);
			//genMinimumPhase(dt, amp0, f, K, sum);
			for(int j=0; j<nt; j++) trace[j] += sum[j];
		}
		for(int j=0; j<nt; j++) trace[j] /= (nf);
		//    	for(int i=0; i<mf; i++) {
		//    		 double f = 1+i;
		//     		genMinimumPhase(dt, amp0, f, K, sum);
		//     		for(int j=0; j<nt; j++) trace[j] += sum[j];
		// 	    }
		//    	nf = 0;
		//    	for(int i=0; i<nf; i++) {
		//   		 	double f = vy[i];
		//    		genMinimumPhase(dt, 1.0, f, K, sum);
		//    		for(int j=0; j<nt; j++) trace[j] += sum[j];
		//	    }
		//    	for(int j=0; j<nt; j++) trace[j] /= (nf+mf);

		float [] x = new float[nt];
		float [] y = new float[nt];

		for(int i=0; i<nt; i++) {
			x[i] = (float)(i);
			y[i] = (float)(trace[i]);
		}
		String aTitle = "Time";
		String xAxisLabel = "No";
		String yAxisLabel = "Amp";

		JFrame frame = new JFrame();
		frame.setSize(300, 300); 
		frame.setLocationRelativeTo(null);  
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); 
		frame.setVisible(true);

		plotLines(frame, x, y, aTitle, xAxisLabel, yAxisLabel, 0, 0);

		SeismicTraceComponent comp = new SeismicTraceComponent(dt, y);
		int beginIndex = 0;
		int endIndex = 0;
		comp.cosineTaperThenfft(beginIndex, endIndex, 0.5);
		comp.calSpectrum();

		float[] ax = comp.getFrequency();
		float[] ay = comp.getSpectrum();
		//		for(int i=0; i<ax.length; i++) {
		//    		if(ax[i]==0) ax[i] = 0;
		//    		else ax[i] = (float)(Math.log10(ax[i]));
		//    		if(ay[i]==0) ay[i] = 0;
		//    		else ay[i] = (float)(Math.log10(ay[i]));
		//    	}
		aTitle = "Frequency-linear";
		plotLines(frame, ax, ay, aTitle, xAxisLabel, yAxisLabel, 0, 0);

		float[] bx = new float[ax.length];
		float[] by = new float[ax.length];
		for(int i=0; i<ax.length; i++) {
			if(ax[i]==0) bx[i] = 0;
			else bx[i] = (float)(Math.log10(ax[i]));
			if(ay[i]==0) by[i] = 0;
			else by[i] = (float)(Math.log10(ay[i]));
		}
		aTitle = "Frequency-log";
		plotLines(frame, bx, by, aTitle, xAxisLabel, yAxisLabel, 0, 0);		
	}
	public void test5() {
		double dt = 0.00025;
		double nyquestFreq = 0.5/dt;
		double df = 1;
		int nf = (int)(nyquestFreq/df)-1;
		double [] fs = new double[nf];
		double [] ws = new double[nf];
		for(int i=0; i<nf; i++) {
			fs[i] = 1+i;
		}

		double amp0 = 1.0;
		double freq0 = 500.0;
		double K = 50.0;
		int nt = 1000;
		double [] trace = new double[nt];
		genSingleMinimumPhase(dt, amp0, freq0, K, trace);
		float [] traceF = new float[nt];
		for(int i=0; i<nt; i++) { traceF[i] = (float)trace[i]; }

		double exponent = -2;
		double minFrquency = freq0;
		MersenneTwister generator = new cern.jet.random.engine.MersenneTwister(new java.util.Date());

		double aMean = freq0; 
		double aVariance = freq0/2.5;
		double rFreq = 0;

		double omega = 10;

		double [][] p = {{1, omega}, {freq0, omega}, {nyquestFreq, omega*1.0e-3} };

		float [] ax = new float[3];
		float [] ay = new float[3];

		for(int i=0; i<3; i++) {
			ax[i] = (float)Math.log10(p[i][0]);
			ay[i] = (float)Math.log10(p[i][1]);
		}

		double slope = (ay[2]-ay[1])/(ax[2]-ax[1]);
		double px = 0;
		double py = 0;		
		for(int i=0; i<nf; i++) {
			if(i<freq0) ws[i] = omega;
			else {
				px = Math.log10(fs[i]);
				py = ay[2]-slope*(ax[2]-px);
				ws[i] = Math.pow(10, py);
			}
		}	    

		float [] bx = new float[nf];
		float [] by = new float[nf];

		for(int i=0; i<nf; i++) {
			bx[i] = (float)fs[i];
			by[i] = (float)ws[i];
			//bx[i] = (float)Math.log10(fs[i]);
			//by[i] = (float)Math.log10(ws[i]);
		}

		String aTitle = "Time";
		String xAxisLabel = "No";
		String yAxisLabel = "Amp";

		JFrame frame = new JFrame();
		frame.setSize(300, 300); 
		frame.setLocationRelativeTo(null);  
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); 
		frame.setVisible(true);

		//plotLines(frame, ax, ay, aTitle, xAxisLabel, yAxisLabel, 0, 0);

		Fft fft = new Fft(traceF); // nx = number of samples of f(x)
		Sampling sk = fft.getFrequencySampling1();
		int nk = sk.getCount(); // number of frequencies sampled
		float[] g = fft.applyForward(traceF); // nk complex samples of g(k)

		float [] spectrum 	= new float[nk];
		for(int i=0; i<nk; i++) {
			if(i<nf) spectrum[i] = by[i];
		}
		float [] frequency 	= new float[nk];
		for (int kk=0,kr=0,ki=kr+1; kk<nk; ++kk,kr+=2,ki+=2) {
			double k = sk.getValue(kk); // frequency k in cycles/sample
			frequency[kk] = (float)(k/dt);
			g[ki] = 0;
			g[kr] = spectrum[kk]*spectrum[kk];
			//spectrum[kk] = (float)Math.sqrt(g[kr]*g[kr]+g[ki]*g[ki]);
		}; 
		float[] h = fft.applyInverse(g); // nx real samples of output h(x)
		float [] hx = new float[h.length];
		for(int i=0; i<hx.length; i++) hx[i] = i+1;

		plotLines(frame, hx, h, aTitle, xAxisLabel, yAxisLabel, 0, 0);

		//    	SeismicTraceComponent comp = new SeismicTraceComponent(dt, y);
		//		int beginIndex = 0;
		//		int endIndex = 0;
		//		//System.out.println("sPick="+sPick+" beginIndex="+beginIndex+" endIndex="+endIndex);
		//		comp.cosineTaperThenfft(beginIndex, endIndex, 0.5);
		//		comp.fft(beginIndex, endIndex);
		//		comp.calSpectrum();
		//		
		//		float[] ax = comp.getFrequency();
		//		float[] ay = comp.getSpectrum();
		//		for(int i=0; i<ax.length; i++) {
		//    		if(ax[i]==0) ax[i] = 0;
		//    		else ax[i] = (float)(Math.log10(ax[i]));
		//    		if(ay[i]==0) ay[i] = 0;
		//    		else ay[i] = (float)(Math.log10(ay[i]));
		//    	}
		//		aTitle = "Frequency-linear";
		//		plotLines(frame, ax, ay, aTitle, xAxisLabel, yAxisLabel, 0, 0);
		//		
		//		float[] bx = new float[ax.length];
		//		float[] by = new float[ax.length];
		//		for(int i=0; i<ax.length; i++) {
		//    		if(ax[i]==0) bx[i] = 0;
		//    		else bx[i] = (float)(Math.log10(ax[i]));
		//    		if(ay[i]==0) by[i] = 0;
		//    		else by[i] = (float)(Math.log10(ay[i]));
		//    	}
		//		aTitle = "Frequency-log";
		//		plotLines(frame, bx, by, aTitle, xAxisLabel, yAxisLabel, 0, 0);
	}

	public void testRotation() {
		double dt = 0.00025; 	// Sampling period [s]
		double fs = 1/dt;     	// Sampling rate [Hz]
		double fNy = fs / 2;  	// Nyquist frequency [Hz]
		int nSamples = 2000; 	// Number of samples
		double duration = nSamples*dt; 	// Duration [s]

		double amp0 = 1.0;
		double freq0 = 500.0;
		double K = 50.0;
		double [] trace = new double[nSamples];
		genSingleMinimumPhase(dt, amp0, freq0, K, trace);

		float [] ax = new float[nSamples];
		float [] ay = new float[nSamples];

		for(int i=0; i<nSamples; i++) {
			ax[i] = i;
			ay[i] = (float)trace[i];
		}

		float [] by = new float[nSamples];

		String aTitle = "Time";
		String xAxisLabel = "No";
		String yAxisLabel = "Amp";

		JFrame frame = new JFrame();
		frame.setSize(300, 300); 
		frame.setLocationRelativeTo(null);  
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); 
		frame.setVisible(true);

		plotLines(frame, ax, ay, aTitle, xAxisLabel, yAxisLabel, 0, 0);

		SeismicTraceComponent [] comps = new SeismicTraceComponent[3];
		comps[0] = new SeismicTraceComponent(dt, ArrayMath.copy(ay));
		for(int i=1; i<comps.length; i++) comps[i] = new SeismicTraceComponent(dt, ArrayMath.copy(by));
		TraceDisplayDialog dialog = new TraceDisplayDialog(frame, "Trace Waveform", false, 1, comps);
		dialog.showDialog();
	}

	public void testPhaseWrapping() {
		int n = 512;
		double PI = Math.PI;
		double PI2 = 2*PI;
		Sampling sampling = new Sampling(n, 1.0, 0.0);
		double [] xPhase = new double[n];
		for(int i=0; i<n; i++) {
			xPhase[i] = 6.0*Math.sin(PI2*i/n);			
		}

		double [] wrappedPhase = new double[n];
		for(int i=0; i<n; i++) {
			wrappedPhase[i] = Math.atan2(Math.sin(xPhase[i]), Math.cos(xPhase[i]));			
		}

		double [] unwrappedPhase = new double[n];
		for(int i=0; i<n; i++) {
			unwrappedPhase[i] = wrappedPhase[i];			
		}
		double d = 0;
		for(int i=1; i<n; i++) {
			d = wrappedPhase[i] - wrappedPhase[i-1];
			if(d>PI) {
				for(int j=i; j<n; j++) unwrappedPhase[j] -= PI2;
			} else if(d<-PI) {
				for(int j=i; j<n; j++) unwrappedPhase[j] += PI2;
			}			
		}

		int iPlot = 0;
		String aTitle = new String(" Cross Plot");
		String catalog = "Cross";
		String vLabel = "Original Phase in Radians";
		String hLabel = "Sample Index";

		CommonFrame frame = new CommonFrame();
		frame.setSize(20,20);
		frame.setVisible(true);
		if(iPlot==0) {
		} else if(iPlot==1) {
		} else if(iPlot==2) {
			vLabel = "Degree Of Freedom";
		} else if(iPlot==3) {
			vLabel = "F-Test";
		}

		PlotDialog dialog = new PlotDialog(frame, aTitle, false, null,
				100, false, catalog, hLabel, vLabel);

		if(iPlot==0) {
			float [] a = new float[sampling.getCount()];
			for(int i=0; i<a.length; i++) a[i] = (float)sampling.getValue(i);
			float [] b = new float[sampling.getCount()];
			//for(int i=0; i<b.length; i++) b[i] = (float)xPhase[i];	
			//for(int i=0; i<b.length; i++) b[i] = (float)wrappedPhase[i];
			for(int i=0; i<b.length; i++) b[i] = (float)unwrappedPhase[i];
			dialog.setCurves(new float[][]{b, a});
		}
		dialog.showDialog();			
	}

	public void testNoisyPhaseWrapping() {
		int n = 512;
		double PI = Math.PI;
		double PI2 = 2*PI;
		Sampling sampling = new Sampling(n, 1.0, 0.0);
		double [] xPhase = new double[n];
		double noiseVariance = 0.7;
		for(int i=0; i<n; i++) {
			xPhase[i] = 6.0*Math.sin(PI2*i/n) + noiseVariance*_random.normal();			
		}

		double [] wrappedPhase = new double[n];
		for(int i=0; i<n; i++) {
			wrappedPhase[i] = Math.atan2(Math.sin(xPhase[i]), Math.cos(xPhase[i]));			
		}

		double [] increment = new double[n];
		double d = 0;
		double K = 0;
		for(int i=1; i<n; i++) {
			d = wrappedPhase[i] - wrappedPhase[i-1];
			if(d>PI) {
				K -= PI2;
			} else if(d<-PI) {
				K += PI2;
			}	
			increment[i] = K;
		}

		double [] unwrappedPhase = new double[n];
		for(int i=0; i<n; i++) {
			unwrappedPhase[i] = wrappedPhase[i]+increment[i];			
		}

		int iPlot = 0;
		String aTitle = new String(" Cross Plot");
		String catalog = "Cross";
		String vLabel = "Original Phase in Radians";
		String hLabel = "Sample Index";

		CommonFrame frame = new CommonFrame();
		frame.setSize(20,20);
		frame.setVisible(true);
		if(iPlot==0) {
		} else if(iPlot==1) {
		} else if(iPlot==2) {
			vLabel = "Degree Of Freedom";
		} else if(iPlot==3) {
			vLabel = "F-Test";
		}

		PlotDialog dialog = new PlotDialog(frame, aTitle, false, null,
				100, false, catalog, hLabel, vLabel);

		if(iPlot==0) {
			float [] a = new float[sampling.getCount()];
			for(int i=0; i<a.length; i++) a[i] = (float)sampling.getValue(i);
			float [] b = new float[sampling.getCount()];
			//for(int i=0; i<b.length; i++) b[i] = (float)xPhase[i];	
			//for(int i=0; i<b.length; i++) b[i] = (float)wrappedPhase[i];
			for(int i=0; i<b.length; i++) b[i] = (float)unwrappedPhase[i];
			dialog.setCurves(new float[][]{b, a});
		}
		dialog.showDialog();
	}

	public void testPhaseWrappingUnderSampling() {
		int n = 31;
		double PI = Math.PI;
		double PI2 = 2*PI;
		Sampling sampling = new Sampling(n, 1.0/n, 0.0);
		double [] xPhase = new double[n];
		for(int i=0; i<n; i++) {
			xPhase[i] = 10.0*Math.sin(10*sampling.getValue(i));			
		}

		double [] wrappedPhase = new double[n];
		for(int i=0; i<n; i++) {
			wrappedPhase[i] = Math.atan2(Math.sin(xPhase[i]), Math.cos(xPhase[i]));			
		}

		double [] unwrappedPhase = new double[n];
		for(int i=0; i<n; i++) {
			unwrappedPhase[i] = wrappedPhase[i];			
		}
		double d = 0;
		for(int i=1; i<n; i++) {
			d = wrappedPhase[i] - wrappedPhase[i-1];
			if(d>PI) {
				for(int j=i; j<n; j++) unwrappedPhase[j] -= PI2;
			} else if(d<-PI) {
				for(int j=i; j<n; j++) unwrappedPhase[j] += PI2;
			}			
		}

		int iPlot = 0;
		String aTitle = new String(" Cross Plot");
		String catalog = "Cross";
		String vLabel = "Original Phase in Radians";
		String hLabel = "Sample Index";

		CommonFrame frame = new CommonFrame();
		frame.setSize(20,20);
		frame.setVisible(true);
		if(iPlot==0) {
		} else if(iPlot==1) {
		} else if(iPlot==2) {
			vLabel = "Degree Of Freedom";
		} else if(iPlot==3) {
			vLabel = "F-Test";
		}

		PlotDialog dialog = new PlotDialog(frame, aTitle, false, null,
				100, false, catalog, hLabel, vLabel);

		if(iPlot==0) {
			float [] a = new float[sampling.getCount()];
			for(int i=0; i<a.length; i++) a[i] = (float)sampling.getValue(i);
			float [] b = new float[sampling.getCount()];
			//for(int i=0; i<b.length; i++) b[i] = (float)xPhase[i];	
			//for(int i=0; i<b.length; i++) b[i] = (float)wrappedPhase[i];
			for(int i=0; i<b.length; i++) b[i] = (float)unwrappedPhase[i];
			dialog.setCurves(new float[][]{b, a});
		}
		dialog.showDialog();			
	}


	public void testWavelet() {

	}



}