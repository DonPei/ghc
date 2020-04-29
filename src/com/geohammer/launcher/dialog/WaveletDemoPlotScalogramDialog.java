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
import edu.mines.jtk.mosaic.PixelsView;
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

public class WaveletDemoPlotScalogramDialog extends CommonDialog {

	public LauncherFrame 		_frame 				= null;
	public double [][] 			_xy 				= null;
	ArrayList<Object> 			_wspc 				= null;
	
	public WaveletDemoPlotPanel _waveletDemoPlotPanel = null;
	
	public WaveletDemoPlotScalogramDialog(JFrame aParent, String aTitle, boolean modal, double [][] xy, ArrayList<Object> wspc) {
		super(aParent, aTitle, modal);
		setDialogWindowSize(1000, 700);
		_frame 				= (LauncherFrame)aParent;
		_xy 				= xy;
		_wspc 				= wspc;
	}

	protected JComponent getCommandRow() { return null; }
	protected boolean okAction() {	return true;	}
	protected JPanel createContents() {
		JPanel jPanel = new JPanel(new BorderLayout());
		double[] scales 		= (double[])_wspc.get(1);
		double[] period 		= (double[])_wspc.get(2);
		double[] coi 			= (double[])_wspc.get(3);
		double signalMean 		= (double)_wspc.get(4);
		ComplexNumber[][] wt 	= (ComplexNumber[][]) _wspc.get(0);
		double [][] mRaModulus 	= MatrixOps.modulus(wt);
		double [][] mRaReal 	= MatrixOps.getRealPart(wt);
		double [][] mRaImaginary= MatrixOps.getImaginaryPart(wt);	
		_waveletDemoPlotPanel = new WaveletDemoPlotPanel(mRaModulus);
		jPanel.add(_waveletDemoPlotPanel, BorderLayout.CENTER);
		//jPanel.add(new ControlPanel(_xy[0].length, _xy[0][1]-_xy[0][0]), BorderLayout.SOUTH);
		return jPanel;
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
			_world.setFrame(WaveletDemoPlotScalogramDialog.this._frame);
			
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
			PixelsView pv = new PixelsView(data);
			
			pv.setName("Data", "Pixel "+id);
			getWorld().removeView(pv.getCategory(), pv.getName());
			getWorld().addPixelsView(0, 0, pv);
		}
	}

	

}