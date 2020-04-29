package org.ucdm.launcher.dialog;

import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerListModel;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import java.util.ArrayList;
import java.util.Random;

import org.ucdm.common.CommonDialog;
import org.ucdm.core.SeismicTraceComponent;
import org.ucdm.core.Semblance;
import org.ucdm.das.DasBinary;
import org.ucdm.dsp.wavelet.Gamma;
import org.ucdm.dsp.wavelet.Hermite;
import org.ucdm.launcher.LauncherFrame;


//https://dspguru.com/dsp/tricks/

public class WaveletDemoDialog extends CommonDialog {
	
	private LauncherFrame 		_frame 		= null;
	private Example 			_example 	= new Example(20);
	
	private JTextField  		_minXTF		= null;
	private JTextField  		_maxXTF		= null;
	
	private boolean 			_isDWT 		= true;
	
	public WaveletDemoDialog(JFrame aParent, String aTitle, boolean modal, boolean isDWT) {
		super(aParent, aTitle, modal);
		setDialogWindowSize(600, 700);
		setEnableApplyButton(true);
		_frame 	= (LauncherFrame)aParent;
		_isDWT 	= isDWT;
	}

	protected JPanel createContents() {
		return new LoadPanel();
	}

	protected boolean okAction() {
		getInput();
		double [][] xy = _example.calData();		
		if(_isDWT) {
			String aTitle = new String("Wavelet DWT Demo 1");
			WaveletDemoPlotDWTDialog dialog = new WaveletDemoPlotDWTDialog(_frame, aTitle, false, 10, xy, 0);
			dialog.showDialog();
		} else {
			String aTitle = new String("Wavelet CWT Demo");
			WaveletDemoPlotCWTDialog dialog = new WaveletDemoPlotCWTDialog(_frame, aTitle, false, xy);
			dialog.showDialog();
		}
		return true;
	}

	
	private class LoadPanel extends JPanel {
		public LoadPanel() {
			setLayout(new GridBagLayout());
			Insets insets = new Insets(5, 5, 5, 5);
			GridBagConstraints gbc = null;
			Font myFont = new Font("SansSerif", Font.PLAIN, 12);
	        Color myColor = Color.BLUE;
	        
			JPanel modulePanel = null;			
			String [] moduleString = null;	
			ButtonGroup moduleRadioGroup = null;
			JRadioButton [] moduleRadioButton = null;	
			int n = 0;
			
			_minXTF = new JTextField (_example._minX[_example._iFunction]+"");
			_maxXTF = new JTextField (_example._maxX[_example._iFunction]+"");
			
			//step 1
			int iRow = 0;
			moduleString = _example._functionNames;
			n = moduleString.length;
			moduleRadioGroup 	= new ButtonGroup();
			moduleRadioButton 	= new JRadioButton[n];
			modulePanel 		= new JPanel( new GridLayout(n/2+(n%2==0?0:1), 2, 2, 2));	
			
			for(int i=0; i<n; i++) {
				final int j = i;
				moduleRadioButton[i] = new JRadioButton(moduleString[i], i==_example._iFunction);
				moduleRadioButton[i].addItemListener( new ItemListener() {
					public void itemStateChanged(ItemEvent itemEvent) {
						if (itemEvent.getStateChange() == ItemEvent.SELECTED) {
							_example._iFunction=j;
							_minXTF.setText(_example._minX[_example._iFunction]+"");
							_maxXTF.setText(_example._maxX[_example._iFunction]+"");
						} 
					}
				});
				moduleRadioGroup.add(moduleRadioButton[i]);
				modulePanel.add(moduleRadioButton[i]); 
			}	
			
			modulePanel.setBorder(BorderFactory.createTitledBorder(null, "Function ", 
	        		TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, myFont, myColor));
			gbc= new GridBagConstraints(0, iRow, 4, 1, 1.0, 1.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, insets, 0, 0);
			add(modulePanel, gbc);
			
			//Step 2
			iRow++;
			modulePanel = new JPanel( new GridLayout(3, 2, 2, 2));	
			modulePanel.add(new JLabel("Min X: ", JLabel.RIGHT));
			modulePanel.add(_minXTF);
			modulePanel.add(new JLabel("Max X: ", JLabel.RIGHT));
			modulePanel.add(_maxXTF);
			modulePanel.add(new JLabel("# Samples: ", JLabel.RIGHT));
			JSpinner spinner = new JSpinner();
			spinner.setModel(new SpinnerListModel(new String[] { "4", "8", "16",
					"32", "64", "128", "256", "512", "1024", "2048", "4096",
					"8192", "16384", "32768", "65536" }));
			spinner.setValue("1024");
			spinner.addChangeListener(new ChangeListener() {
				public void stateChanged(ChangeEvent arg0) {
					String sCount = (String) spinner.getValue();
					_example._count = Integer.parseInt(sCount);
				}
			});
			//spinner.setBounds(95, 114, 63, 20);
			
			modulePanel.add(spinner);
			
			modulePanel.setBorder(BorderFactory.createTitledBorder(null, "Parameter ", 
	        		TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, myFont, myColor));
			gbc= new GridBagConstraints(0, iRow, 4, 1, 1.0, 1.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, insets, 0, 0);
			add(modulePanel, gbc);			
		}
	}
	
	public void getInput() {
		_example._min = Double.parseDouble(_minXTF.getText().trim());
		_example._max = Double.parseDouble(_maxXTF.getText().trim());
	}
	
	
	/**
	 * Class responsibility: Provide examples of certain signals, some of which are
	 * not analytic. All public members return xy as a double[][] such that xy[0] =
	 * x and xy[1] = y. X is computed on [min:max] in increments dx = (max - min +
	 * 1) / (count);
	 */

	private class Example {
		public int 		_iFunction 		= 20;
		public double 	_min 			= 0.0;
		public double 	_max 			= 1.0;
		public int 		_count 			= 1024;
		
		public String [] _functionNames = new String [] {
				"Line: 2x + 1", "Delta function", "Dirac comb, T = 128", "Sin(2*pi*3*x)", "Gauss modified mixed Sin",
				"Hyperbola (4/x)", "Parabola 4*pow(x,2)", "Tan(x)", "Morlet (real part)", "Dropout in signal",
				"Cantor", "Random", "Mexican-hat", 
				"Chirp", "Gaussian", "2nd Derivative Gaussian", "5th Derivative Gaussian", "6th Derivative Gaussian",
				"Box", "Trangle", "DAS", "Others"
		};
		
		public double [] _minX = new double [] {
				-20.0, -512.0, -512.0, 0.0, -512.0, 
				-10.0, -5.0, -2.1 * Math.PI, -3 * Math.PI, 0.0, 
				0.0, 0.0, -3 * Math.PI, 
				0.0, -2048, -2048, -2048, -2048, 
				-35, -35, 0, 0
		};
		public double [] _maxX = new double [] {
				50.0, 511.0, 511.0, Math.PI, 511.0, 
				10.0, 5.0, 2.04 * Math.PI, 3 * Math.PI, 40.0,
				1.0, 1.0, 3 * Math.PI,
				2.1 * Math.PI, 2047, 2047, 2047, 2047, 
				35, 35, 16384, 16384
		};
		
		public Example(int iFunction) {
			_iFunction = iFunction;
			_min = _minX[iFunction];
			_max = _maxX[iFunction];
		}
		
		public double dxFromCountRange(double max, double min, int count) {
			double dCount = (double) count;
			double dx = (max - min) / (dCount - 1.0);
			return dx;
		}
		
		public double[][] calData() {
			double dx = dxFromCountRange(_max, _min, _count);
			switch(_iFunction) {
			case 0: 				
				return signal(_min, _max, dx, _count, 0);
			case 1: 
				return delta(_min, _max, dx, _count);
			case 2: 
				return deltaComb(_min, _max, dx, _count, 128);
			case 3: 
				return signal(_min, _max, dx, _count, 1);
			case 4: 
				return gaussModSin(_min, _max, dx, _count);
			case 5: 
				return signal(_min, _max, dx, _count, 2);
			case 6: 
				return signal(_min, _max, dx, _count, 3);
			case 7: 
				return signal(_min, _max, dx, _count, 4);
			case 8: 
				return signal(_min, _max, dx, _count, 5);
			case 9: 
				return signalDropout();
			case 10: 
				return cantor(_min, _max, dx, _count);
			case 11: 
				return random(_min, _max, dx, _count);
			case 12: 
				return signal(_min, _max, dx, _count, 6);
			case 20: 
				//String fileName = "C:\\PINN_DATA\\COG\\2017Mabee\\DAS\\LH-MS-Stg37\\DOSS_20150721T183022_471090Z.bin";
				String fileName = "C:\\PINN_DATA\\COG\\2017Mabee\\DAS\\LH-MS-Stg37\\DOSS_20150721T183035_578290Z.bin";
				int c1Inclusive = 0+11*(15); //210; //130;
				int c2Inclusive = 0+11*(15); //210; //130;
				return readDas(new File(fileName), c1Inclusive, c2Inclusive, 9000, 9000+1024-1);
				
			default: 
				return signal(_min, _max, dx, _count, 5);
			}
		}
		
		private double[][] signal(double min, double max, double dx, int count, int index) {			
			double[][] xy = new double[2][count];
			double x = min;
			for (int i = 0; i < count; i++) {
				xy[0][i] = x;
				if(index==0) xy[1][i] = 2.0*x+1.0;
				else if(index==1) xy[1][i] = Math.sin(2.0*Math.PI*3.0*x);
				else if(index==2) xy[1][i] = 4.0/x;
				else if(index==3) xy[1][i] = 4.0*x*x;
				else if(index==4) xy[1][i] = Math.tan(x);
				else if(index==5) xy[1][i] = Math.pow(Math.PI,-0.25)*Math.cos(6*x)*Math.exp(-(x*x)/2); //function = "1/((pow(pi,0.25)))*cos(6*x)*exp(-(x*x)/2)";
				else if(index==6) xy[1][i] = Math.pow(Math.PI,-0.25)*(1.0-x*x)*Math.exp(-(x*x)/2);
				x += dx;
			}
			return xy;
		}
		
		private double[][] readDas(File selectedFile, int c1Inclusive, int c2Inclusive, int s1Inclusive, int s2Inclusive) {
			if(selectedFile==null) return null;
			
			DasBinary dasBinary = new DasBinary(selectedFile);		
			double samplingRate = dasBinary.getSamplingRate()/dasBinary.getCompression();
			double sampleInterval = 1.0/samplingRate;
			System.out.println(" sampleInterval="+sampleInterval);
			//dasBinary.downSample(cMin, cMax, cIncremental, fMin, fMax, fIncremental);
			//dasBinary.removeDc();
			//dasBinary.smooth();
			
			//float [][] data = dasBinary.getPartialData(0, 1);
			float [][] data = dasBinary.getData();

			if(s2Inclusive<=s1Inclusive) {
				s1Inclusive = 0;
				s2Inclusive = data[0].length-1;
			}
			_count = s2Inclusive-s1Inclusive+1;
			
			double [] x = new double[_count];
			double [] y = new double[_count];
			for(int i=s1Inclusive, k=0; i<=s2Inclusive; i++, k++) {
				x[k] = k;
				y[k] = data[c1Inclusive][i];
			}
			_min = x[0];
			_max = x[_count-1];
			
			return new double[][] { x, y};
			
		}
		/**
		 * An impulse at x = 0, elsewhere 0. Min, max, and count must be chosen so
		 * that x = 0 occurs in the sequence, eg., min = -8, max = 7, count = 16
		 * produces a sequence that includes x = 0 at x[8]
		 */
		private double[][] delta(double min, double max, double dx, int count) {
			double[][] xy = new double[2][count];
			double x = min;
			for (int i = 0; i < count; i++) {
				xy[0][i] = x;
				if (x == 0) {
					xy[1][i] = 1.0;
				}
				x += dx;
			}
			return xy;
		}
		
		private double[][] triangle(double t0, double A, double min, double max, double dx, int count) {
			double[][] xy = new double[2][count];
			double slope = -A * A / (-2 * t0);
			double x = min;
			for (int i = 0; i < count; i++) {
				if (x > -2 * t0 && x < 2 * t0) {
					if (x < 0) {
						xy[1][i] = slope * x+A*A;
					} else {
						xy[1][i] = -slope * x+A*A;
					}
				} else {
					xy[1][i] = 0;
				}
				xy[0][i] = x;
				x += dx;
			}
			return xy;
		}
		
		private double[][] box(double t0, double A, double min, double max, double dx, int count) {
			double[][] xy = new double[2][count];		
			double x = min;
			for (int i = 0; i < count; i++) {
				if (x > -t0 && x < t0) {				
						xy[1][i] = A;
					
				} else {
					xy[1][i] = 0;
				}
				xy[0][i] = x;
				x += dx;
			}
			return xy;
		}
		private double[][] chirp(double min, double max, double dx, int count) {
			double[][] xy = new double[2][count];
			double x = min;
			for (int i = 0; i < count; i++) {
				xy[0][i] = x;
				xy[1][i] = Math.sin(10 * x * x);
				x += dx;
			}
			return xy;
		}
		
		/**
		 * To show the impulses; min, max, and count must be chosen such that there
		 * will exist x values where x[i] mod T = 0.
		 * 
		 * @param min
		 * @param max
		 * @param count
		 * @param T
		 *            the interval between impulses
		 * @return An impulse whenever x mod T = 0, elsewhere 0.
		 */
		private double[][] shah(double min, double max, double dx, int count, int T) {
			double[][] xy = new double[2][count];
			double x = min;
			for (int i = 0; i < count; i++) {
				xy[0][i] = x;
				if (Math.abs(xy[0][i] % T) < dx) {
					xy[1][i] = 1.0;
				}
				x += dx;
			}
			return xy;
		}

		/**
		 * To show the impulses; min, max, and count must be chosen such that there
		 * will exist x values where x[i] mod T = 0.
		 * 
		 * @param min
		 * @param max
		 * @param count
		 * @param T
		 *            the interval between impulses
		 * @return An impulse whenever x mod T = 0, elsewhere 0.
		 */
		public double[][] deltaComb(double min, double max, double dx, int count, int T) {
			double[][] xy = new double[2][count];
			double x = min;
			for (int i = 0; i < count; i++) {
				xy[0][i] = x;
				if (xy[0][i] % T == 0) {
					xy[1][i] = 1.0;
				}
				x += dx;
			}
			return xy;
		}
		
		/**
		 * Gaussian modified mixed sinusoids. Gaussian centered at 0 with stdev =
		 * 128 sinusoids = sin(x) +sin(x/64)
		 */
		public double[][] gaussModSin(double min, double max, double dx, int count) {
			double[][] xy = new double[2][count];
			double mean = 0.0;
			double stdev = 128;
			//String function = "(1/(pow(2*pi,0.5)*" + stdev + "))*exp(-pow((" + mean+ "-x),2)/(2*pow(" + stdev + ",2)))";
			
			double x = min;
			double a = 1.0/(stdev*Math.pow(2.0*Math.PI, 0.5));
			for (int i = 0; i < count; i++) {
				xy[0][i] = x;
				xy[1][i] = a*Math.exp(-(x-mean)*(x-mean)/(2.0*stdev*stdev));
				double y = 0.2 * Math.sin(x) + Math.sin(x / 64);
				xy[1][i] *= y;
				x += dx;
			}
			return xy;
		}

		private double[][] gaussian(double mean, double stdev, double min, double max, double dx, int count) {
			double[][] xy = new double[2][count];
			double x = min;
			double a = 1.0/(stdev*Math.pow(2.0*Math.PI, 0.5));
			for (int i = 0; i < count; i++) {
				xy[0][i] = x;
				xy[1][i] = a*Math.exp(-(x-mean)*(x-mean)/(2.0*stdev*stdev));
				//double y = 0.2 * Math.sin(x) + Math.sin(x / 64);
				//xy[1][i] *= y;
				x += dx;
			}
			
//			String function = "(1/(pow(2*pi,0.5)* stdev"))*exp(-pow((" + mean
//					+ "-x),2)/(2*pow(" + stdev + ",2)))";
//			xy = new Signal(min, max, count, function, Signal.RangeOption.MinMax).xy;
			return xy;
		}

		private double[][] gaussianDerivative(double mean, double stdev, double min, double max, double dx, int count, int m) {
			double[][] xy = new double[2][count];
			double[][] gauss = gaussian(mean, stdev, min, max, dx, count);
			double[] x = gauss[0];
			int n = x.length;
			double f1 = Math.pow(-1, m + 1);
			double gamma = 1 / Gamma.gamma(m + .5);
			double hermite = 0;
			for (int i = 0; i < n; i++) {
				hermite = Hermite.probabilistHermitePoly(m, x[i] / (stdev));
				xy[1][i] = f1 * gamma * hermite * gauss[1][i] / Math.exp(m * m);

			}
			xy[0] = x;
			return xy;
		}

		
		/**
		 * n = 1024, x[0] = 0, x[n-1] = 40 Sinusoidal frequencies at 1.4, 1/2, and
		 * 1/9. However, but y is momentarily forced to zero at x[459], producing an
		 * effective singularity.
		 */
		public double[][] signalDropout() {
			double min = 0;
			double max = 40;
			int count = 1024;
			double[] freq = new double[] { 1.4, 0.5, 1.0 / 3.0 };
			double[][] xy = new double[2][count];
			String function = "sin(2*pi*" + freq[0] + "*x)+sin(2*pi*" + freq[1]+ "*x)+0.3*sin(2*pi*" + freq[2] + "*x)";
			//xy = new Signal(min, max, count, function, Signal.RangeOption.MinMax).xy;
			double dx = dxFromCountRange(max, min, count);
			
			double x = min;
			for (int i = 0; i < count; i++) {
				xy[0][i] = x;
				xy[1][i] = Math.sin(2.0*Math.PI*freq[0]*x)+Math.sin(2.0*Math.PI*freq[1]*x)+0.3*Math.sin(2.0*Math.PI*freq[2]*x);
				x += dx;
			}
			
			xy[1][459] = 0;
			return xy;
		}

		/**
		 * Devil's staircase
		 * 
		 * @param min
		 *            prefer 0
		 * @param max
		 *            prefer 1
		 * @param count
		 * @return Cantor function
		 */
		public double[][] cantor(double min, double max, double dx, int count) {
			double[][] xy = new double[2][count];
			double x = min;
			for (int i = 0; i < count; i++) {
				xy[0][i] = x;
				x += dx;
			}
			xy[1] = cantorFn(xy[0]);
			return xy;
		}

		/**
		 * Singular everywhere.
		 * 
		 * @param min
		 * @param max
		 * @param count
		 * @return a random sequence.
		 */
		public double[][] random(double min, double max, double dx, int count) {
			Random r = new Random();
			double[][] xy = new double[2][count];
			double x = min;
			for (int i = 0; i < count; i++) {
				xy[0][i] = x;
				x += dx;
				xy[1][i] = r.nextDouble();
			}
			return xy;
		}

		/**
		 * Notes for the following methods: In string representations of fractional
		 * values in bases other than 10, a colon (:) is used instead of a decimal
		 * point. All methods target positive bases only.
		 */

		/**
		 * Compute the Cantor function using a method of typographic substitution.
		 * 
		 * @param x
		 *            sequence for x axis
		 * @return
		 */
		private double[] cantorFn(double[] x) {
			int n = x.length;
			double[] y = new double[n];
			String one = "1";
			String zero = "0";
			String point = ":";
			boolean isNegative = false;
			for (int i = 0; i < n; i++) {
				double base10 = x[i];
				// Express in base 3.
				String strB3 = base10ToBase(base10, 8, 3);
				if (strB3.contains("-")) {
					isNegative = true;
					strB3 = strB3.replaceFirst("-", "");
				} else {
					isNegative = false;
				}
				char[] charB3 = strB3.toCharArray();
				int l = charB3.length;
				boolean isFirst1 = true;
				for (int j = 0; j < l; j++) {
					// If it contains a 1, replace every digit after the first 1 by
					// 0.
					if (charB3[j] == one.charAt(0) && isFirst1) {
						isFirst1 = false;
						continue;
					} else if (!isFirst1 && charB3[j] != point.charAt(0)) {
						charB3[j] = zero.charAt(0);
					}
				}
				String strTransit = new String(charB3);
				// Replace all 2s with 1s
				strTransit = strTransit.replaceAll("2", "1");
				// Interpret the result as a binary number.
				y[i] = baseToBase10(strTransit, 2);
				if (isNegative) {
					y[i] *= -1.0;
				}
			}
			return y;
		}

		/**
		 * @return x Using the first "precision" places to the right of the decimal,
		 *         express x in base = newBase
		 */
		public String base10ToBase(double x, int precision, int newBase) {
			String result = "";
			if (x < 0) {
				result += "-";
			}
			int characteristic = (int) x;
			char[] cCharacteristic = toBase((int) Math.abs(x), newBase);
			result += (new String(cCharacteristic) + ":");
			// discard digits below precision
			double mantissa = Math
					.abs((int) (Math.pow(10, precision) * (x - characteristic))
							/ Math.pow(10, precision));
			double interimProduct = mantissa;
			for (int i = 0; i < precision; i++) {
				double nextProduct = interimProduct * newBase;
				if ((int) nextProduct < 10) {
					result += (int) nextProduct;
				} else {
					result += Integer.toString((int) nextProduct, newBase);
				}
				interimProduct = nextProduct - (int) nextProduct;
			}
			return result.trim();
		}

		public double baseToBase10(String strB2, int initialBase) {
			boolean isNegative = false;
			if (strB2.contains("-")) {
				isNegative = true;
				strB2 = strB2.replaceFirst("-", "");
			}
			double b10 = 0;
			String[] split = strB2.split(":");
			int characteristic = Integer.valueOf(split[0], initialBase);
			b10 += characteristic;
			if (split.length > 1) {
				char[] mantissa = split[1].toCharArray();
				int n = mantissa.length;
				for (int i = 0; i < n; i++) {
					double d = (double) (Character.getNumericValue(mantissa[i]));
					if (d != 0) {
						b10 += d * Math.pow(initialBase, -(i + 1));
					}
				}
			}
			if (isNegative) {
				b10 *= -1.0;
			}
			return b10;
		}

		/**
		 * Integers only.
		 * 
		 * @param x
		 *            an integer in base 10
		 * @param base
		 *            the base in which to re-express x
		 * @return x in base as a character array
		 */
		private char[] toBase(int x, int base) {
			String strB = Integer.toString(x, base);
			char[] charB = strB.toCharArray();
			return charB;
		}

	}

}