package org.ucdm.launcher.dialog;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;
import java.util.ArrayList;

import javax.swing.JMenuItem;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.ucdm.common.CommonMouseEditingMode;
import org.ucdm.common.CommonPanel;
import org.ucdm.dsp.wavelet.ContinuousWT;
import org.ucdm.dsp.wavelet.DiscreteWT;
import org.ucdm.dsp.wavelet.ContinuousWT.Wavelet;
import org.ucdm.dsp.wavelet.cwt.CWT;
import org.ucdm.dsp.wavelet.ComplexNumber;
import org.ucdm.dsp.wavelet.MatrixOps;
import org.ucdm.launcher.LauncherFrame;

import edu.mines.jtk.mosaic.GridView;
import edu.mines.jtk.mosaic.PointsView;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerListModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;

import org.ucdm.common.CommonDialog;

public class WaveletDemoPlotCWTDialog extends CommonDialog {

	public LauncherFrame 		_frame 				= null;
	public double [][] 			_xy 				= null;
	ArrayList<Object> 			_wspc 				= null;
	
	public WaveletDemoPlotPanel _waveletDemoPlotPanel = null;
	
	public WaveletDemoPlotCWTDialog(JFrame aParent, String aTitle, boolean modal, double [][] xy) {
		super(aParent, aTitle, modal);
		setDialogWindowSize(1000, 700);
		_frame 				= (LauncherFrame)aParent;
		_xy 				= ContinuousWT.padPow2(xy);
	}

	protected JComponent getCommandRow() { return null; }
	protected boolean okAction() {	return true;	}
	protected JPanel createContents() {
		JPanel jPanel = new JPanel(new BorderLayout());
		_waveletDemoPlotPanel = new WaveletDemoPlotPanel(_xy);
		jPanel.add(_waveletDemoPlotPanel, BorderLayout.CENTER);
		jPanel.add(new ControlPanel(_xy[0].length, _xy[0][1]-_xy[0][0]), BorderLayout.SOUTH);
		return jPanel;
	}
	private class ControlPanel extends JPanel {

		private JSpinner 			_waveletSpinner = null;
		private JSpinner 			_parSpinner 	= null;
		private JSpinner 			_scaleS0Spinner = null;
		private JSpinner 			_scaleDsSpinner = null;
		private JSpinner 			_scaleNsSpinner = null;		
		
		private Wavelet 			_wavelet 		= null;
		public double 				_waveletParameter = 1;
		private int 				_signalLength 	= 1024;
		public double 				_sampleInterval	= 0.01;
		public double 				_s0 			= 0;
		public double 				_ds 			= 0.1;
		public int 					_ns 			= 10;
		public boolean 				_isCosineDamp 	= true;
		
		public ControlPanel(int signalLength, double sampleInterval) {
			_signalLength 	= signalLength;
			_wavelet 		= Wavelet.Morlet;
			_sampleInterval = sampleInterval;
			_s0 = sampleInterval / 4.0;
			_ds = 0.25;
			_ns = (int) ((1 + (Math.log(signalLength * _sampleInterval / _s0) / Math.log(2)) / _ds));
			setLayout(new GridLayout(3, 6, 2, 2));
			
			_scaleS0Spinner = new JSpinner();
			_scaleS0Spinner.setModel(new SpinnerNumberModel(new Double(_s0), new Double(0), null, new Double(_s0 / 25)));
			JSpinner.NumberEditor editor = (JSpinner.NumberEditor)_scaleS0Spinner.getEditor();
			DecimalFormat format = editor.getFormat();
			format.setMinimumFractionDigits(6);
			editor.getTextField().setHorizontalAlignment(SwingConstants.CENTER);			
			_scaleS0Spinner.setValue(0.000001); // set precision
			_scaleS0Spinner.setValue(_sampleInterval / 4);	

			_scaleDsSpinner = new JSpinner();
			_scaleDsSpinner.setModel(new SpinnerNumberModel(new Double(_ds), new Double(0), null, new Double(_ds / 25)));
			editor = (JSpinner.NumberEditor)_scaleDsSpinner.getEditor();
			format = editor.getFormat();
			format.setMinimumFractionDigits(6);
			editor.getTextField().setHorizontalAlignment(SwingConstants.CENTER);

			_scaleNsSpinner = new JSpinner();
			editor = (JSpinner.NumberEditor)_scaleNsSpinner.getEditor();
			editor.getTextField().setHorizontalAlignment(SwingConstants.CENTER);
			_scaleNsSpinner.setModel(new SpinnerNumberModel(new Integer(_ns), null, null, new Integer(1)));			

			_parSpinner = new JSpinner();
			editor = (JSpinner.NumberEditor)_parSpinner.getEditor();
			editor.getTextField().setHorizontalAlignment(SwingConstants.CENTER);
			//0
			add(new JLabel("CWT: ", JLabel.RIGHT));			
			_waveletSpinner = new JSpinner();			
			editor = (JSpinner.NumberEditor)_waveletSpinner.getEditor();
			editor.getTextField().setHorizontalAlignment(SwingConstants.CENTER);	
			_waveletSpinner.setModel(new SpinnerListModel(Wavelet.values()));
			_waveletSpinner.addChangeListener(new ChangeListener() {
				public void stateChanged(ChangeEvent arg0) {
					updateWaveletSpinner();
				}
			});		
			add(_waveletSpinner);
			
			updateWaveletSpinner();
			
			//2
			add(new JLabel("Wavelet Order: ", JLabel.RIGHT));	
			_parSpinner.addChangeListener(new ChangeListener() {
				public void stateChanged(ChangeEvent arg0) {
					_waveletParameter = Double.valueOf((String)_parSpinner.getValue());;
				}
			});			
			add(_parSpinner);
			
			//4
			add(new JLabel("Smallest Scale: ", JLabel.RIGHT));
			_scaleS0Spinner.addChangeListener(new ChangeListener() {
				public void stateChanged(ChangeEvent arg0) {
					updateScaleSpinner();
				}
			});		
			add(_scaleS0Spinner);
			//6
			add(new JLabel("Sample Interval: ", JLabel.RIGHT));
			_scaleDsSpinner.addChangeListener(new ChangeListener() {
				public void stateChanged(ChangeEvent arg0) {
					updateScaleSpinner();
				}
			});		
			add(_scaleDsSpinner);
			//8
			add(new JLabel("Total Scale: ", JLabel.RIGHT));
			_scaleNsSpinner.addChangeListener(new ChangeListener() {
				public void stateChanged(ChangeEvent arg0) {
					_ns = (int)_scaleNsSpinner.getValue();
				}
			});		
			add(_scaleNsSpinner);
			//10
			
			JCheckBox jCheckBox = new JCheckBox("Cos Damping", _isCosineDamp);
			add(jCheckBox);
			jCheckBox.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent event) {
					_isCosineDamp = ((JCheckBox)event.getSource()).isSelected();
				}
			});
			
			JButton jButton = new JButton("Cal CWT");
			jButton.addActionListener( new ActionListener() {
				public void actionPerformed( ActionEvent e ) {
					_wspc = calculate();
				}
			});	
			add(jButton);
			//12
			
			jButton = new JButton("Plot CWT");
			jButton.addActionListener( new ActionListener() {
				public void actionPerformed( ActionEvent e ) {
					plot(0);
				}
			});	
			add(jButton);
			jButton = new JButton("Scalogram2D");
			jButton.addActionListener( new ActionListener() {
				public void actionPerformed( ActionEvent e ) {
					plot(2);
				}
			});	
			add(jButton);
			//14
			add(new JLabel(""));
			add(new JLabel(""));
			add(new JLabel(""));
			add(new JLabel(""));
			
			jButton = new JButton("Cal MRA");
			jButton.addActionListener( new ActionListener() {
				public void actionPerformed( ActionEvent e ) {
					//calculate(0);
				}
			});	
			//add(jButton);
		}
		private void updateWaveletSpinner() {
			_wavelet = (Wavelet) _waveletSpinner.getValue();
			double[] validParameters = ContinuousWT.getSelectedParamChoices(_wavelet);

			String[] params = new String[validParameters.length];
			for (int i = 0; i < validParameters.length; i++) {
				params[i] = Double.toString(validParameters[i]);
			}
			_parSpinner.setModel(new SpinnerListModel(params));			
		}
		
		private void updateScaleSpinner() {
			_s0 = (double)_scaleS0Spinner.getValue();
			_ds = (double)_scaleDsSpinner.getValue();
			_ns = (int) ((1 + (Math.log(_signalLength * _sampleInterval / _s0) / Math.log(2)) / _ds));
			_scaleNsSpinner.setValue(_ns);
		}
		
		private ArrayList<Object> calculate() {
			_wavelet = (Wavelet) _waveletSpinner.getValue();
			_waveletParameter = Double.valueOf((String)_parSpinner.getValue());
			_s0 = (double)_scaleS0Spinner.getValue();
			_ds = (double)_scaleDsSpinner.getValue();
			_ns = (int)_scaleNsSpinner.getValue();		
			
			//double[][] signal = ContinuousWT.padPow2(_xy);
			double[][] signal = _xy;
			if(_isCosineDamp) signal = ContinuousWT.cosineDamp(_xy);
			ArrayList<Object> wspc = ContinuousWT.cWT(signal[1], _sampleInterval, _wavelet, _waveletParameter, _s0, _ds,_ns);
			
			ComplexNumber[][] wt 	= (ComplexNumber[][]) wspc.get(0);
			double[][] rWT 			= MatrixOps.getRealPart(wt);
			double signalMean 		= (double) wspc.get(4);
			double[] scales = new double[_ns];
			for(int i=0; i<_ns; i++) scales[i] = _s0 * Math.pow(2.0, (double) i * _ds);
			//scales[i] = _s0+i*_ds;
			double[] recon = ContinuousWT.cwtReconstruct(_wavelet, _waveletParameter, rWT, scales, _ds, _sampleInterval, signalMean);
			
			_waveletDemoPlotPanel.update(new double[][]{signal[0], recon}, 1);
			
			return wspc;
		}
		
		private void plot(int id) {
			if(_wspc==null) return;
			ComplexNumber[][] wt 	= (ComplexNumber[][]) _wspc.get(0);
			double[][] signal 		= _xy;
//			double[] scales 		= (double[]) wspc.get(1);
//			double[] period 		= (double[]) wspc.get(2);
//			double[] coi 			= (double[]) wspc.get(3);
//			double signalMean 		= (double) wspc.get(4);
//
//			double [][] mRaModulus 	= MatrixOps.modulus(wt);
//			double [][] mRaReal 	= MatrixOps.getRealPart(wt);
//			double [][] mRaImaginary= MatrixOps.getImaginaryPart(wt);	
			if(id==0) {
				double [][] mRaModulus 	= MatrixOps.modulus(wt);
				double[][] columnVectors = MatrixOps.transpose(mRaModulus);
				for(int i=0; i<columnVectors.length; i++) {
					_waveletDemoPlotPanel.update(new double[][]{signal[0], columnVectors[i]}, 3+i);
				}				
			} else if(id==1) {
			} else if(id==2) {
				String aTitle = new String("Wavelet Demo Scalogram");
				WaveletDemoPlotScalogramDialog dialog = new WaveletDemoPlotScalogramDialog(_frame, aTitle, false, signal, _wspc);
				dialog.showDialog();
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
		
	}
	
	
	private class WaveletDemoPlotPanel extends CommonPanel {
		public CommonPanel 		_world		= null;		
		public float [][] 		_data 		= null;
		
		public WaveletDemoPlotPanel(double [][] xy) {
			setLayout(new BorderLayout());
			_data = toFloatMatrix(xy);			
			init();
			updateWorld();

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
			_world = new CommonPanel(1, 1, CommonPanel.Orientation.X1RIGHT_X2UP,  CommonPanel.AxesPlacement.LEFT_BOTTOM, null);
			_world.setBorder(new EmptyBorder(10, 0, 0, 10));
			_world.setBackground(Color.white);
			_world.setHLabel("Sample No");
			_world.setVLabel("Value");
			_world.setFrame(WaveletDemoPlotCWTDialog.this._frame);
			
			_world.setEnableTracking(true);
			_world.setEnableEditing(true);
			_world.setEnableZoom(true);
			
			_world.addModeManager();
			CommonMouseEditingMode mouseEditingMode = _world.getMouseEditingMode();
			mouseEditingMode.setJComponent(genJMenuItem());
		}
		protected JComponent[] genJMenuItem() {
			JMenuItem jMenuItem1 = new JMenuItem("Start Play");
			jMenuItem1.addActionListener(new ActionListener() 	{
				public void actionPerformed(ActionEvent e)   {	
						
				}
			});
			
			return new JComponent[] { jMenuItem1};
		}
		
		public void updateWorld() { 
			getWorld().removeAllViews(0, 0);	

			GridView gv = new GridView(Color.LIGHT_GRAY);
			gv.setName("Grid", "Major");
			gv.setVisible(getWorld().getVisible(gv.getCategory(), gv.getName()));
			getWorld().removeView(gv.getCategory(), gv.getName());
			getWorld().addGridView(0, 0, gv);

			update(_data, 0);
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
		public void update(double [][] dataD, int id) { update(toFloatMatrix(dataD), id); }
		public void update(float [][] data, int id) { 
			PointsView pv = new PointsView(data[0], data[1]);
			pv.setLineWidth(1);
			if(id==0) {pv.setStyle("k-"); pv.setName("Data", "Origin"); }
			else if(id==1) {pv.setStyle("r-"); pv.setName("Data", "Forward DWT"); }
			else if(id==2) {pv.setStyle("b-"); pv.setName("Data", "Inverse DWT"); }
			else  {pv.setStyle("-"); pv.setLineColor(getColor(id-3));pv.setName("Data", "RMA "+id); }
			//pv.setVisible(getWorld().getVisible(pv.getCategory(), pv.getName()));
			getWorld().removeView(pv.getCategory(), pv.getName());
			getWorld().addPointsView(0, 0, pv);
		}
	}

	

}