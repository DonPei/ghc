package org.ucdm.launcher.dialog;

import static edu.mines.jtk.util.ArrayMath.sqrt;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

import javax.swing.JMenuItem;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.ucdm.common.CommonMouseEditingMode;
import org.ucdm.common.CommonPanel;
import org.ucdm.component.LabelTextCombo;
import org.ucdm.core.SeismicTraceComponent;
import org.ucdm.core.Semblance;
import org.ucdm.das.DasBinary;
import org.ucdm.dsp.wavelet.OrthogonalFilters;
import org.ucdm.dsp.wavelet.DiscreteWT.Wavelet;
import org.ucdm.dsp.AmplitudeCosineTaper;
import org.ucdm.dsp.DSP;
import org.ucdm.dsp.StaOverLta;
import org.ucdm.dsp.wavelet.DiscreteWT;
import org.ucdm.launcher.LauncherFrame;
import org.ucdm.seg2.SEG2Trace;
import org.ucdm.vc.dialog.PlotDialog;

import edu.mines.jtk.dsp.BandPassFilter;
import edu.mines.jtk.mosaic.GridView;
import edu.mines.jtk.mosaic.PointsView;
import edu.mines.jtk.util.ArrayMath;

import javax.print.attribute.standard.PrinterLocation;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerListModel;

import org.ucdm.common.CommonDialog;
import org.ucdm.common.CommonFrame;

public class WaveletDemoPlotDWTDialog extends CommonDialog {

	public CommonFrame 			_frame 					= null;
	public double [][] 			_xy 					= null;
	public WaveletDemoPlotPanel _waveletDemoPlotPanel 	= null;
	public int 					_iApp 					= 0;
	public int 					_iMethod 				= 0;
	public JLabel 				_trackingLabel 			= new JLabel("tracking");
	
	public WaveletDemoPlotDWTDialog(JFrame aParent, String aTitle, boolean modal, int iApp, double [][] xy, int iMethod) {
		super(aParent, aTitle, modal);
		setDialogWindowSize(1800, 700);
		_frame 				= (CommonFrame)aParent;
		_iApp 				= iApp;
		_iMethod			= iMethod;
		_xy 				= xy;
	}

	protected JComponent getCommandRow() { return null; }
	protected boolean okAction() {	return true;	}
	protected JPanel createContents() {
		JPanel jPanel = new JPanel(new BorderLayout());
		_waveletDemoPlotPanel = new WaveletDemoPlotPanel(_xy);
		ControlPanel controlPanel = new ControlPanel(_iApp, _xy[0].length);
		//controlPanel.calculateAndPlot(1);
		jPanel.add(_waveletDemoPlotPanel, BorderLayout.CENTER);
		jPanel.add(controlPanel, BorderLayout.SOUTH);
		return jPanel;
	}
	
	private class ControlPanel extends JPanel {

		private JSpinner 			_groupSpinner 		= null;
		private JSpinner 			_parameterSpinner 	= null;
		private JSpinner 			_lenSpinner 		= null;
		
		private Wavelet 			_wavelet 			= null;
		public int 					_coarsestScale 		= 1;
		public int 					_waveletParameter 	= 1;
		private int 				_signalLength 		= 1024;
		
		private JTextField 			_traceNoTF 			= null;
		
		public ControlPanel(int iApp, int signalLength) {
			_signalLength 	= signalLength;
			_wavelet 		= Wavelet.Daubechies;
			
			int iCol = 10+1;
			if(iApp==1) iCol = 8+1;
			setLayout(new GridLayout(1, iCol, 2, 2));
			
			_parameterSpinner = new JSpinner();
			_lenSpinner = new JSpinner();
			
			add(_trackingLabel);
			add(new JLabel("Group: ", JLabel.RIGHT));			
			_groupSpinner = new JSpinner();			
			_groupSpinner.setModel(new SpinnerListModel(Wavelet.values()));
			_groupSpinner.setValue(Wavelet.Coiflet);
			_groupSpinner.addChangeListener(new ChangeListener() {
				public void stateChanged(ChangeEvent arg0) {
					updateGroupSpinner();
				}
			});		
			add(_groupSpinner);
			
			updateGroupSpinner();
			
			add(new JLabel("Wavelet: ", JLabel.RIGHT));	
			_parameterSpinner.addChangeListener(new ChangeListener() {
				public void stateChanged(ChangeEvent arg0) {
					updateParameterSpinner();
				}
			});			
			add(_parameterSpinner);
			
			add(new JLabel("Coarsest Scale: ", JLabel.RIGHT));
			//add(new JLabel("Level of Coarsest Scale (2^L): ", JLabel.RIGHT));	
			_lenSpinner.addChangeListener(new ChangeListener() {
				public void stateChanged(ChangeEvent arg0) {
					_coarsestScale = Integer.valueOf((String)_lenSpinner.getValue());
				}
			});		
			add(_lenSpinner);
			
			JButton jButton = new JButton("Cal DWT");
			jButton.addActionListener( new ActionListener() {
				public void actionPerformed( ActionEvent e ) {
					calculateAndPlot(1);
				}
			});	
			add(jButton);
			
			jButton = new JButton("Cal MRA");
			jButton.addActionListener( new ActionListener() {
				public void actionPerformed( ActionEvent e ) {
					calculateAndPlot(0);
				}
			});	
			add(jButton);
			
			_traceNoTF 	= new JTextField("45");
			if(iApp==0) add(_traceNoTF);
			if(iApp==10) {_traceNoTF.setText("50,300"); add(_traceNoTF);}
			jButton = new JButton("Select");
			jButton.addActionListener( new ActionListener() {
				public void actionPerformed( ActionEvent e ) {
					int traceNo = Integer.parseInt(_traceNoTF.getText().trim());
					_xy = nextSelectedTrace(traceNo);
					_waveletDemoPlotPanel.update(_xy, 0, "Raw", "k-", true);
					calculateAndPlot(1);
				}
			});	
			if(iApp==0) add(jButton);
			
			jButton = new JButton("STA/LTA");
			jButton.addActionListener( new ActionListener() {
				public void actionPerformed( ActionEvent e ) {
					float [][] data = _waveletDemoPlotPanel.displayTrigger(0, _traceNoTF.getText().trim());
					_waveletDemoPlotPanel.update(data, 2, "Trigger", "m-", false);
				}
			});	
			if(iApp==10) add(jButton);
			
		}
		private void updateGroupSpinner() {
			_wavelet = (Wavelet) _groupSpinner.getValue();
			ArrayList<Integer> validParameters = OrthogonalFilters.validParameters(_wavelet);
			String [] params = new String[validParameters.size()];
			for (int i = 0; i <params.length; i++) {
				params[i] = Integer.toString(validParameters.get(i));
			}
			_parameterSpinner.setModel(new SpinnerListModel(params));
			//_parameterSpinner.setValue("30");
			//System.out.println("wavelet="+_wavelet+" "+Arrays.toString(params));
			try {
				ArrayList<Integer> validScales = OrthogonalFilters.validScales(validParameters.get(0), _wavelet, _signalLength);
				String[] scales = new String[validScales.size()];
				for (int i = 0; i < scales.length; i++) {
					scales[i] = Integer.toString(validScales.get(i));
				}
				_lenSpinner.setModel(new SpinnerListModel(scales));
				_lenSpinner.setEnabled(true);
				updateParameterSpinner();
			} catch (Exception e) {
				_lenSpinner.setEnabled(false);
			}
		}
		private void updateParameterSpinner() {
			_waveletParameter = Integer.valueOf((String)_parameterSpinner.getValue());
			try {
				ArrayList<Integer> validScales = OrthogonalFilters.validScales(_waveletParameter, _wavelet, _signalLength);
				String[] scales = new String[validScales.size()];
				for (int i = 0; i < scales.length; i++) {
					scales[i] = Integer.toString(validScales.get(i));
				}
				_lenSpinner.setModel(new SpinnerListModel(scales));
				_lenSpinner.setEnabled(true);
				_lenSpinner.setValue("10");
			} catch (Exception e) {
				_lenSpinner.setEnabled(false);
			}
		}
		
		private void calculateAndPlot(int id) {
			_wavelet = (Wavelet) _groupSpinner.getValue();
			_waveletParameter = Integer.valueOf((String)_parameterSpinner.getValue());
			_coarsestScale = Integer.valueOf((String)_lenSpinner.getValue());
			if(id==0) {
				double[][] signal = DiscreteWT.padPow2(_xy);
				try {
					ArrayList<Object> multResol = DiscreteWT.mRA(signal[1], _wavelet, _waveletParameter, _coarsestScale);
					double[][] mra = toMatrix((ArrayList<double[]>) multResol.get(0));
					//double[][] columnVectors = MatrixOps.transpose(mRAMatrix);
					int [] scale = (int[]) multResol.get(1);
					
					for(int i=0; i<mra.length; i++) {
						//_waveletDemoPlotPanel.update(new double[][]{signal[0], mra[i]}, 3+i);
						//update(_data, 0, "Raw", "k-");
					}
				} catch (Exception e1) {
					System.out.println("mra");
				}
			} else if(id>0) {
				double[][] forwardData = doDWT(1, _xy);
				if(_iMethod<10) {
					_waveletDemoPlotPanel.update(forwardData, 2, "DWT", "m-", true);
					//double[][] inverseData = doDWT(-1, forwardData);
					//_waveletDemoPlotPanel.update(inverseData, 2, "iDWT", "m-", true);
				} else if(_iMethod>=10) {
					_waveletDemoPlotPanel.update(forwardData, 2, "DWT", "m-", true);
					//double[][] inverseData = doDWT(-1, forwardData);
					//_waveletDemoPlotPanel.update(inverseData, 1, "iDWT", "r-");
				}
			}
		}
		
		private double[][] doDWT(int direction, double [][] xy) {
			double[][] signal = DiscreteWT.padPow2(xy);
			double[] wT = null;
			try {
				wT = DiscreteWT.transform(signal[1], _wavelet, _waveletParameter, _coarsestScale, direction);
				
				int subLength = (int) (Math.pow(2, _coarsestScale));	
				double [] trendData = new double[wT.length];	
				double [] x = new double[wT.length];
				//double [] trendData = new double[subLength];	
				//double [] x = new double[subLength];
//				for(int i=0; i<subLength; i++) {
//				trendData[i] = wT[i];
//				x[i] = i;
//			}
				for(int i=0; i<wT.length; i++) {
					trendData[i] = wT[i];
					x[i] = i;
				}
//				for(int i=subLength, k=0; i<wT.length; i++) {
//					trendData[k] = wT[i];
//					x[k] = i;
//					k++;
//				}
				if(direction==1) {					
					for(int i=subLength; i<wT.length; i++)  {
						//if(wT[i]>-0.15&&wT[i]<0.15); wT[i] = 0;
						//wT[i] = 0;
					}
				}
				double[][] dwt = new double[2][];
				if(_iMethod==0) { dwt[1] = wT; dwt[0] = signal[0]; }
				else if(_iMethod==10) { dwt[1] = trendData; dwt[0] = x; }
				return dwt;
			} catch (Exception e1) {
				return null;
			}			
		}
		
		private double[][] toMatrix(ArrayList<double[]> data) {
			int m = data.get(0).length;
			int n = data.size();
			double[][] matrix = new double[n][m];
			for (int i = 0; i < m; i++) {
				for (int j = 0; j < n; j++) {
					matrix[j][i] = data.get(j)[i];
				}
			}
			return matrix;
		}
		
		public double[][] nextSelectedTrace(int traceNo) {
			String fileName = "C:\\PINN_DATA\\COG\\2017Mabee\\DAS\\LH-MS-Stg37\\DOSS_20150721T183035_578290Z.bin";
			//int c1Inclusive = 210; //130;
			//int c2Inclusive = 210; //130;
			System.out.println("traceNo="+traceNo);
			return readDas(new File(fileName), traceNo, traceNo);		
		}
		
		private double[][] readDas(File selectedFile, int c1Inclusive, int c2Inclusive) {
			if(selectedFile==null) return null;
			
			DasBinary dasBinary = new DasBinary(selectedFile);					
			//dasBinary.downSample(cMin, cMax, cIncremental, fMin, fMax, fIncremental);
			//dasBinary.removeDc();
			//dasBinary.smooth();
			
			float [][] data = dasBinary.getData();
			
			double [] x = new double[data[0].length];
			double [] y = new double[data[0].length];
			for(int i=0; i<x.length; i++) {
				x[i] = i;
				y[i] = data[c1Inclusive][i];
			}
			
			return new double[][] { x, y};		
		}
	}
	
	private class WaveletDemoPlotPanel extends CommonPanel {
		public CommonPanel 		_world				= null;		
		public float [][] 		_data 				= null;
		public JTextField 		_beginFTF 			= new JTextField("0");
		public JTextField 		_endFTF 			= new JTextField("0");
		public JTextField 		_sampleIntervalTF 	= new JTextField("0.0001");
		
		public JTextField 		_beginWaveFTF 		= new JTextField("0");
		public JTextField 		_endWaveFTF 		= new JTextField("0");
		
		public JTextField 		_decimateIntervalTF 	= new JTextField("4");
		
		public JTextField 		_beforeWinLenTF 	= new JTextField("200");
		public JTextField 		_afterWinLenTF 		= new JTextField("400");
		
		public WaveletDemoPlotPanel(double [][] xy) {
			setLayout(new BorderLayout());
			int m = 5; //half-width m of a boxcar filter
			float sigma = sqrt(m*(m+1)/3.0f);  //a Gaussian filter with a specified half-width sigma
			float ss = sigma*sigma;
			float a = (1.0f+ss-sqrt(1.0f+2.0f*ss))/ss;
			DSP.exponentialFilterEndingWithZeroSlope(a, xy[1], null);
			
			float [][] data = toFloatMatrix(xy);		

			int nTrace = 1;
			double sampleInterval = 0.0001;
			SeismicTraceComponent [] comps = new SeismicTraceComponent[nTrace];
			comps[0] = new SeismicTraceComponent(sampleInterval, data[1]);			
			Semblance semblance = new Semblance(0, 0, comps);
			double klowerInHz = 80;
			double kupperInHz = 1000;
			double klower = klowerInHz*sampleInterval;
			double kupper = kupperInHz*sampleInterval;
			double kwidth = (kupper-klower)*0.1;	
			double aerror = 0.1*kwidth;
			BandPassFilter bandPassFilter = new BandPassFilter(klower, kupper, kwidth, aerror);
			bandPassFilter.setExtrapolation(BandPassFilter.Extrapolation.ZERO_VALUE);
			bandPassFilter.setFilterCaching(false);
			semblance.applyBandPassFilter(bandPassFilter, null);
			
			_data = toFloatMatrix(xy);
			
//			float [][] aa = _data;
//			_data = data;
//			data = aa;
//			
//			double [][] bb = new double[_data.length][];
//			for(int i=0; i<_data.length; i++) {
//				double [] v = new double[_data[i].length];
//				for(int j=0; j<xy[i].length; j++) v[j] = _data[i][j];
//				bb[i] = v;
//			}
//			_xy = bb;
			
			init();
			update(_data, 0, "Raw", "k-", true);
			
			update(data, 1, "BP", "b-", true);

			add(getWorld(), BorderLayout.CENTER);
		}
		
		public float [][] toFloatMatrix(double [][] xy) {
			float [][] data = new float[xy.length][];
			for(int i=0; i<xy.length; i++) {
				float [] v = new float[xy[i].length];
				for(int j=0; j<xy[i].length; j++) v[j] = (float)xy[i][j];
				data[i] = v;
			}
			return data;
		}

		private CommonPanel getWorld() { return _world; }

		private void init() {
			_world = new CommonPanel(3, 1, CommonPanel.Orientation.X1RIGHT_X2UP,  CommonPanel.AxesPlacement.LEFT_BOTTOM, null);
			_world.setBorder(new EmptyBorder(10, 0, 0, 10));
			_world.setBackground(Color.white);
			_world.setHLabel("Sample No");
			_world.setVLabel(0, "Sample Value");
			_world.setVLabel(1, "Sample Value");
			_world.setVLabel(2, "Sample Value");
			_world.setFrame(WaveletDemoPlotDWTDialog.this._frame);
			
			_world.setEnableTracking(true);
			_world.setEnableEditing(true);
			_world.setEnableZoom(true);
			
			_world.addModeManager();
			CommonMouseEditingMode mouseEditingMode = _world.getMouseEditingMode();
			mouseEditingMode.setJComponent(genJMenuItem());
			
			_world.getMouseTrackInfoMode().setTrackingLabel(WaveletDemoPlotDWTDialog.this._trackingLabel);
		}
		protected JComponent[] genJMenuItem() {
			JMenuItem jMenuItem  	= null;
			
			JMenu waveform = new JMenu("Waveform");	
			waveform.add(new LabelTextCombo("From Sample: ", _beginWaveFTF));
			waveform.add(new LabelTextCombo("To Sample:    ", _endWaveFTF));
			jMenuItem  	= new JMenuItem("    Plot Waveform");  
			waveform.add(jMenuItem);
			jMenuItem.addActionListener(new ActionListener() 	{
				public void actionPerformed(ActionEvent e)   {
					displayWaveform();
				}
			});
			
			JMenu spectrum = new JMenu("Spectrum");		
			spectrum.add(new LabelTextCombo("Sample Interval: ", _sampleIntervalTF));
			spectrum.add(new LabelTextCombo("From Sample: ", _beginFTF));
			spectrum.add(new LabelTextCombo("To Sample:    ", _endFTF));
			jMenuItem  	= new JMenuItem("    Plot Spectrum");  
			spectrum.add(jMenuItem);
			jMenuItem.addActionListener(new ActionListener() 	{
				public void actionPerformed(ActionEvent e)   {
					displaySpectrum(0, 0);
				}
			});
			
			JMenu decimation = new JMenu("Decimation");	
			decimation.add(new LabelTextCombo("Decimate Interval: ", _decimateIntervalTF));
			jMenuItem  	= new JMenuItem("    Decimate Waveform");  
			decimation.add(jMenuItem);
			jMenuItem.addActionListener(new ActionListener() 	{
				public void actionPerformed(ActionEvent e)   {
					decimateWaveform();
				}
			});
			
			JMenu snrMenu = new JMenu("SNR");	
			snrMenu.add(new LabelTextCombo("Before Win Len: ", _beforeWinLenTF));
			snrMenu.add(new LabelTextCombo("After Win Len: ", _afterWinLenTF));
			jMenuItem  	= new JMenuItem("    Cal SNR");  
			snrMenu.add(jMenuItem);
			jMenuItem.addActionListener(new ActionListener() 	{
				public void actionPerformed(ActionEvent e)   {
					calSNR();
				}
			});
			
			return new JComponent[] { waveform, spectrum, decimation, snrMenu};
		}
		
		public float [][] displayTrigger(int iMethod, String inputText) {
			int iRow = 1;
			int iCol = 0;
			PointsView pv = (PointsView)_world.getTile(iRow, iCol).getTiledView(0);
			float [] dataX = pv.getX1().get(0);
			float [] seisData = pv.getX2().get(0);
			
			double sampleInterval = 0.0001;
			SeismicTraceComponent  comp = new SeismicTraceComponent(0, "1", 0.0, 0.0, 0.0, 
					1, 1, 1, sampleInterval, seisData);
			
			String [] split = inputText.split("[,]+");
			int sta = Integer.parseInt(split[0]);
			int lta = Integer.parseInt(split[1]);
			float [] ratio = comp.calStaLtaRatio(seisData, sta, lta);
			
			return new float [][] {dataX, ratio};			
		}
		
		private double calSNR() {
			int beforeWinLen = Integer.parseInt(_beforeWinLenTF.getText().trim());
			int afterWinLen = Integer.parseInt(_afterWinLenTF.getText().trim());
			if(afterWinLen<=0 || afterWinLen<=0) {
				System.out.println("snr=0");
				return 0;
			}
			
			String a = WaveletDemoPlotDWTDialog.this._trackingLabel.getText().trim();
			String [] split = a.split("[:,]+");
			
			int indexP = (int)(Double.parseDouble(split[1]));
			int p1 = indexP-beforeWinLen;
			int p2 = indexP+afterWinLen;
			
			int iRow = (int)(Double.parseDouble(split[4]));
			int iCol = (int)(Double.parseDouble(split[5]));
			PointsView pv = (PointsView)_world.getTile(iRow, iCol).getTiledView(0);
			float [] data = pv.getX2().get(0);
			
			double sum1 = 0.0;
			double sum2 = 0.0;
			for(int i=p1; i<indexP; i++) sum1 += data[i]*data[i];
			for(int i=indexP; i<=p2; i++) sum2 += data[i]*data[i];
			
			double SNR = 0.0;
			if(sum1>0.0)  	SNR = 10.0 * Math.log10( (sum2/afterWinLen) / (sum1/beforeWinLen) );
			System.out.println("snr="+SNR+" p1="+p1+" indexP="+indexP+" p2="+p2+" iRow="+iRow+" iCol="+iCol);
			//if(sum1>0.0)  	SNR = Math.sqrt( (sum2/len2) / (sum1/len1) );
			return SNR;
		}
		private void displaySpectrum(int iMethod, int iComp) {
			double sampleInterval = Double.parseDouble(_sampleIntervalTF.getText().trim());
			int beginIndex = Integer.parseInt(_beginFTF.getText().trim());
			int endIndex = Integer.parseInt(_endFTF.getText().trim());
			
			String text = WaveletDemoPlotDWTDialog.this._trackingLabel.getText().trim();
			String [] split = text.split("[:,]+");
			int iRow = (int)(Double.parseDouble(split[4]));
			int iCol = (int)(Double.parseDouble(split[5]));
			PointsView pv = (PointsView)_world.getTile(iRow, iCol).getTiledView(0);
			float [] data = pv.getX2().get(0);
			
			SeismicTraceComponent  comp = new SeismicTraceComponent(0, "1", 0.0, 0.0, 0.0, 
					1, 1, 1, sampleInterval, data);
			
			if(endIndex>beginIndex) {
				comp = comp.copy(beginIndex, endIndex);
			}

			System.out.println(" sampleInterval10="+sampleInterval);			
			
			ArrayList<float[]> axList = new ArrayList<float[]>();
			ArrayList<float[]> ayList = new ArrayList<float[]>();
			
			double alpha = 0.2;
			AmplitudeCosineTaper.taper(data, alpha);

			comp.fft(data);
			comp.calSpectrum();
			//comp.multiply(comp.getSampleInterval(), comp.getSpectrum());
			
			float [][] afft = new float[][]{comp.getFrequency(), comp.getSpectrum()};
			//DSP.exponentialFilterEndingWithZeroSlope(0.35f, afft[1], null);
			axList.add(ArrayMath.copy(afft[0]));
			ayList.add(ArrayMath.copy(afft[1]));
			float [][] ax = new float[axList.size()][];
			float [][] ay = new float[axList.size()][];
			for(int i=0; i<axList.size(); i++) {
				ax[i] = axList.get(i);
				ay[i] = ayList.get(i);
			}

			if(iMethod==0) {
				PlotDialog dialog = new PlotDialog(WaveletDemoPlotDWTDialog.this._frame, "Frequency Spectrum - Component "+(iComp+1), false, 
						null, 160, false, "Frequency", "Frequency (Hz)", "Spectrum");
				if(iComp==3) dialog.setCurves(new float [][] {ax[0], ay[0], ax[1], ay[1], ax[2], ay[2]});
				else dialog.setCurves(new float [][] {ax[0], ay[0]});
				dialog.showDialog();
			} else {
				float [][] curve = new  float [2*ax.length][];
				float [] a = null;
				for(int i=0, k=0; i<axList.size(); i++) {
					a = ax[i];
					float [] b = new float[a.length];
					for(int j=0; j<b.length; j++) b[j] = (float)Math.log10(a[j]);
					curve[k++] = b;
					a = ay[i];
					b = new float[a.length];
					for(int j=0; j<b.length; j++) b[j] = (float)Math.log10(a[j]);
					curve[k++] = b;
				}

				PlotDialog dialog = new PlotDialog(WaveletDemoPlotDWTDialog.this._frame, "Frequency Spectrum - Component "+(iComp+1), false, 
						null, 160, false, "Frequency", "Frequency (Hz)", "Displacement Spectrum (x10^ -20 m-s)");
				dialog.setCurves(curve);
				dialog.showDialog();
			}
		}
		
		private void displayWaveform() {
			int beginIndex = Integer.parseInt(_beginWaveFTF.getText().trim());
			int endIndex = Integer.parseInt(_endWaveFTF.getText().trim());
			
			String text = WaveletDemoPlotDWTDialog.this._trackingLabel.getText().trim();
			String [] split = text.split("[:,]+");
			int iRow = (int)(Double.parseDouble(split[4]));
			int iCol = (int)(Double.parseDouble(split[5]));
			PointsView pv = (PointsView)_world.getTile(iRow, iCol).getTiledView(0);
			float [] data = pv.getX2().get(0);
			
			SeismicTraceComponent  comp = new SeismicTraceComponent(0, "1", 0.0, 0.0, 0.0, 
					1, 1, 1, 1.0, data);
			
			if(endIndex>beginIndex) {
				comp = comp.copy(beginIndex, endIndex);
			}
		
			data = comp.getData();
			float [] dataI = new float[data.length];
			for(int i=0; i<dataI.length; i++) dataI[i] = i+1;
			
			PlotDialog dialog2 = new PlotDialog(WaveletDemoPlotDWTDialog.this._frame, "Component ", false, 
					null, 160, false, "Data", "Sample #", "Sample Value");
			dialog2.setCurves(new float [][] {dataI, data});
			dialog2.showDialog();
		}
		private void decimateWaveform() {
			int skipLen = Integer.parseInt(_decimateIntervalTF.getText().trim());
			
			String text = WaveletDemoPlotDWTDialog.this._trackingLabel.getText().trim();
			String [] split = text.split("[:,]+");
			int iRow = (int)(Double.parseDouble(split[4]));
			int iCol = (int)(Double.parseDouble(split[5]));
			PointsView pv = (PointsView)_world.getTile(iRow, iCol).getTiledView(0);
			float [] data = pv.getX2().get(0);
			
		
			int len = (int)(data.length/skipLen)+((data.length%skipLen)==0?0:1);
			float [] dataI = new float[len];
			float [] dataF = new float[len];
			for(int i=0, k=0; i<dataI.length; i++) {
				k = i*skipLen;
				dataI[i] = i+1;
				dataF[i] = data[k];
			}
			
			PlotDialog dialog2 = new PlotDialog(WaveletDemoPlotDWTDialog.this._frame, "Component ", false, 
					null, 160, false, "Data", "Sample #", "Sample Value");
			dialog2.setCurves(new float [][] {dataI, dataF});
			dialog2.showDialog();
		}
		private Color getColor(int id) {
			int k = id%7;
			switch(k) {
			case 0: return Color.red;
			case 1: return Color.green;
			case 2: return Color.blue;
			case 3: return Color.black;
			case 4: return Color.cyan;			
			case 5: return Color.orange;
			case 6: return Color.magenta;				
			default:
				return Color.gray;
			}
		}
		private float[][] normalize(float [][] data) {
			float [] x = data[0];
			float [] y = data[1];
			float [] y1 = new float[y.length];
			float min = ArrayMath.min(y);
			float max = ArrayMath.max(y);
			float c = Math.abs(min)>Math.abs(max)?Math.abs(min):Math.abs(max);
			for(int i=0; i<y.length; i++) y1[i] = y[i]/c;
			return new float[][]{x, y1};			
		}
		public void update(double [][] dataD, int iRow, String name, String styles, boolean norm) { 
			update(toFloatMatrix(dataD), iRow, name, styles, norm); 
		}		
		public void update(float [][] data, int iRow, String name, String styles, boolean norm) { 
			//getWorld().removeAllViews();
			//_data = data;
			
			//StaOverLta staOverLta = new StaOverLta(100, 50, 4.0f);
			//ArrayList<Float> pick = staOverLta.calTriggers(data[1]);
//			if(pick==null) System.out.println("null");
//			else {
//				int n = pick.size()/2;
//				for(int i=0, j=0; i<n; i++) {
//					System.out.println("i="+i+" index="+pick.get(j++).intValue()+" v="+pick.get(j++).floatValue() );
//				}
//			}
			float[][] xy = data;
			if(norm) xy = normalize(data);
			PointsView pv = new PointsView(xy[0], xy[1]);
			pv.setLineWidth(1);
			pv.setStyle(styles); 
			pv.setName("Data", name); 
			
//			if(id==0) {pv.setStyle("k-"); pv.setName("Data", "Origin"); }
//			else if(id==1) {pv.setStyle("r-"); pv.setName("Data", "Forward DWT"); }
//			else if(id==2) {pv.setStyle("b-"); pv.setName("Data", "Inverse DWT"); }
//			else  {pv.setStyle("-"); pv.setLineColor(getColor(id-3));pv.setName("Data", "RMA "+id); }
			//pv.setVisible(getWorld().getVisible(pv.getCategory(), pv.getName()));
			getWorld().removeView(pv.getCategory(), pv.getName());
			getWorld().addPointsView(iRow, 0, pv);
			if(norm) getWorld().setVLimits(iRow, -1, 1);
		}
	}

	

}