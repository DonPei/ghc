package com.geohammer.rt;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.ucdm.core.SeisPTUtil;
import org.ucdm.core.SeismicTraceComponent;
import org.ucdm.core.acquisition.VCPair;
import org.ucdm.core.planarmodel.DipLayer1D;
import org.ucdm.core.planarmodel.Layer2D;
import org.ucdm.core.planarmodel.Vecon2D;
import org.ucdm.fd3d.FdWavelet;
import org.ucdm.fd3d.option.HistogramOption;
import org.ucdm.fd3d.option.ModelOption;
import org.ucdm.fd3d.option.SnapshotOption;
import org.ucdm.fd3d.option.SourceOption;
import org.ucdm.ivc.LayerFrame;
import org.ucdm.ivc.LayerView;
import org.ucdm.ivc.dialog.PointsPlotDialog;
import org.ucdm.mti.MtiFrame;
import org.ucdm.resource.Resource;
import org.ucdm.rt.pseudo3d.RayTracerFlat3D;
import org.ucdm.seg2.SEG2;
import org.ucdm.segy.Gather;
import org.ucdm.sgl.VolumeFrame;
import org.ucdm.sgl.util.UiUtil;

import edu.mines.jtk.util.ArrayMath;


public class TravelTimeTableDialog  extends JDialog {
	private LayerFrame 	_frame 			= null;
	private VolumeFrame _frameV 		= null;
	private String 		_cwd 			= null;
	
	private double 		_dx 			= 5.0;
	private JTextField 	_gridTF 		= null;
	
	TravelTimeTableNative _travelTimeTableNative = null;
	

	public int 		_iRayTracer 		= 0; // =0 pseudo3DRayTracer =1 FMM =2 Jing

	public int 		_iShot 			= 0;
	long _startTime;	// Get current starting time in milliseconds
	long _endTime;		// Get current ending time in milliseconds 

	private CalculatorTask 		_calcTask 	= null;
	private int 				_forceQuit	= 0;
	private JButton 			_calculateButton;
	private JButton 			_cancelButton;

	public TravelTimeTableDialog(JFrame aParent, JFrame bParent, String aTitle, boolean modal, String cwd) {
		super(aParent==null?bParent:aParent, aTitle, modal);
		_frame 	= (LayerFrame)aParent;
		_frameV = (VolumeFrame)bParent;
		_cwd = cwd;
	}

	public final void showDialog(){
		setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
		setResizable(true);
		addCancelByEscapeKey();

		JPanel panel = new JPanel();
		panel.setLayout( new BorderLayout() );
		panel.setBorder( UiUtil.getStandardBorder() );

		JPanel jc = createContents();
		Border loweredetched = BorderFactory.createEtchedBorder(EtchedBorder.LOWERED);
		jc.setBorder(loweredetched);

		String tipsText = "<html>3D finite difference waveform modeling (FD3D):  4th order in space and 2nd order in time" +
				"<UL>" +
				"  <LI>Grid Spacing dz is equal to grid spacing dx (dx=dz)" +
				"  <LI>At least 5 grid points per wavelength are needed to minimize grid dispersion" +
				"</UL>";

		JLabel fancyTipsLabel = new JLabel(tipsText, UIManager.getIcon("OptionPane.informationIcon"), JLabel.CENTER);
		fancyTipsLabel.setBorder(BorderFactory.createTitledBorder("Tips"));


		//panel.add(fancyTipsLabel, BorderLayout.NORTH);
		panel.add(jc, BorderLayout.CENTER);
		panel.add( commandPanel(), BorderLayout.SOUTH );

		getContentPane().add( panel );
		//UiUtil.centerOnParentAndShow( this, 1200, 800 );
		UiUtil.centerOnParentAndShow( this);
		setSize(600, 540);
		setVisible(true);
	}

	private JPanel createContents() {
		return new TtTablePanel(_dx);
	}

	private JPanel commandPanel() {

		_calculateButton = new JButton("Calculate...");
		_calculateButton.addActionListener( new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				startCal();
			}
		});

		_cancelButton = new JButton("Stop");
		_cancelButton.addActionListener( new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				stopCal();
			}
		});

		JPanel p = new JPanel();
		p.setLayout(new GridBagLayout());
		Insets insets = new Insets(5, 5, 5, 5);

		GridBagConstraints gbc= new GridBagConstraints(0, 0, 1, 1, 0.0, 1.0,
				GridBagConstraints.EAST, GridBagConstraints.BOTH, insets, 0, 0);
		p.add(_calculateButton, gbc);

		gbc=  new GridBagConstraints(1, 0, 1, 1, 0.0, 1.0,
				GridBagConstraints.EAST, GridBagConstraints.BOTH, insets, 0, 0);
		p.add(_cancelButton, gbc);
		
		this.getRootPane().setDefaultButton( _calculateButton );

		return p;
	}

	/**
	 * Force the escape key to call the same action as pressing the Cancel button.
	 *
	 * <P>This does not always work. See class comment.
	 */
	private void addCancelByEscapeKey(){
		String CANCEL_ACTION_KEY = "CANCEL_ACTION_KEY";
		int noModifiers = 0;
		KeyStroke escapeKey = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, noModifiers, false);
		InputMap inputMap = this.getRootPane().getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
		inputMap.put(escapeKey, CANCEL_ACTION_KEY);
		AbstractAction cancelAction = new AbstractAction(){
			public void actionPerformed(ActionEvent e){
				dispose();
			}
		}; 
		this.getRootPane().getActionMap().put(CANCEL_ACTION_KEY, cancelAction);
	}
	
	public void startCal() {
		_startTime = System.currentTimeMillis(); 
		_dx = Double.parseDouble(_gridTF.getText().trim());
		
		VCPair vcPW 	= _frame.getProject().getVCPair();		
		Layer2D layer2D = _frame.getProject().getLayer();
		DipLayer1D dipLayer1D = _frame.getProject().getDipLayer1D();

		Vecon2D vp2D = layer2D.toVecon2D(1, _dx, _dx);
		Vecon2D vs2D = layer2D.toVecon2D(2, _dx, _dx);
		Vecon2D den2D = layer2D.toVecon2D(4, _dx, _dx);
		Vecon2D epsilon2D = layer2D.toVecon2D(20, _dx, _dx);
		Vecon2D delta2D = layer2D.toVecon2D(21, _dx, _dx);
		Vecon2D gamma2D = layer2D.toVecon2D(22, _dx, _dx);

//		Vecon2D vp2D = null;
//		Vecon2D vs2D = null;
//		Vecon2D den2D = null;
//		Vecon2D epsilon2D = null;
//		Vecon2D delta2D = null;
//		Vecon2D gamma2D = null;
		
		_calcTask = new CalculatorTask(_frame, _frameV, _cwd, _iRayTracer, dipLayer1D, layer2D, vcPW, _dx, 
				vp2D, vs2D, den2D, epsilon2D, delta2D, gamma2D);

		_calcTask.execute();
	}

	public void stopCal() {
		//LOG.fine("Enter VelocityModelerFrame stopCalc");

		//savePreferences();
		// Is a Calculator Task running in background?
		if (_calcTask != null  &&  !_calcTask.isDone()) {
			//LOG.fine("Calculator Task is running. Cancel it.");
			_forceQuit = 1;
			// Call SwingWorker cancel method to cancel CalculatorTask.
			// true means interrupt task if it is running.

			System.out.println("Enter VelocityModelerFrame stopCalc1");
			_calcTask.cancel(true);
			//LOG.fine("Stop Progress Timer");
		} else {
			//setVisible( false );
			//dispose();
		}
		//LOG.fine("Exit AnisoLSFrame stopCalc");
	}	

	private class CalculatorTask extends SwingWorker<TravelTimeTableToken, TravelTimeTableToken> implements TravelTimeTableWorkerI {

		public CalculatorTask(LayerFrame frame, VolumeFrame frameV, String cwd, 
				int iRayTracer, DipLayer1D dipLayer1D, Layer2D layer2D, VCPair vcPW, double dx, 
				Vecon2D vp2D, Vecon2D vs2D, Vecon2D den2D, Vecon2D epsilon2D, Vecon2D delta2D, Vecon2D gamma2D) {	

			_travelTimeTableNative = new TravelTimeTableNative(frame, frameV, cwd, 
					iRayTracer, dipLayer1D, layer2D, vcPW, dx, 
					vp2D, vs2D, den2D, epsilon2D, delta2D, gamma2D);

			setCursor(new Cursor(Cursor.WAIT_CURSOR));
			_calculateButton.setEnabled(false);
		}

		@Override
		protected TravelTimeTableToken doInBackground() {
			while (isCancelled()) return null;
			_travelTimeTableNative.start();
			return _travelTimeTableNative.getToken();
		}

		@Override
		public void update(TravelTimeTableToken token) {
			publish(token); 
		}

		protected void process(List<TravelTimeTableToken>  tokens) {
//			if(_mtiFrame==null) return;
//			for(TravelTimeTableToken token : tokens){
//				_mtiFrame.setView(token.getGather(), 2);
//				Vecon2D vecon2D = token.getVz();	
//				int nSnapshot = vecon2D.getNx();
//				if(nSnapshot>0) {				
//					int iSnapshot = (int)(vecon2D.getX0());
//					//double snapshotInterval = vecon2D.getDx();
//					//double t = iSnapshot*snapshotInterval;
//					String label = (iSnapshot+1)+"/"+nSnapshot;
//					if(vecon2D.getId()==0) {
//						label += " vz";
//					} else {
//						label += " vx";
//					}
//					_frame.addTransparentPixelView(false, vecon2D, label);
//				}
//			}
		}

		@Override
		protected void done() {
			//_progBar.setValue( 100 );
			try {
				TravelTimeTableToken token = get();
				if(token!=null) {
//					Gather gather = token.getGather();				
//					SeismicTraceComponent [] comps = getZYXComponent(gather);
//					
//					String fileName = _modelOption.getResultDir()+File.separator+"v.dat";
//					SEG2 seg2 = new SEG2(comps);
//					seg2.write(fileName);
				}
			} catch(InterruptedException e) {
				System.out.println("Iterrupted while waiting for results.");
			} catch(ExecutionException e) {
				System.out.println("Error performing computation.");
			} catch (CancellationException e) {
				if(_travelTimeTableNative!=null) _travelTimeTableNative.setCancel(true);
				System.out.println("User cancelled.");
			}	

			//_progBar.setString("Done");
			_calculateButton.setEnabled(true);		
			setCursor( null );
		}
		
	}

	private class TtTablePanel extends JPanel {

		public TtTablePanel(double grid) {
			setLayout(new GridBagLayout());
			Insets insets = new Insets(5, 5, 5, 5);
			GridBagConstraints gbc;

			_gridTF = new JTextField (grid+"");

			int iRow = 0;
			gbc= new GridBagConstraints(0, iRow, 1, 1, 1.0, 0.0, GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH, insets, 0, 0);
			add(new JLabel("Grid Spacings:"), gbc);

			gbc= new GridBagConstraints(1, iRow, 1, 1, 1.0, 0.0, GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH, insets, 0, 0);
			add(_gridTF, gbc);
			
			JPanel modulePanel = null;
			Border loweredetched = BorderFactory.createEtchedBorder(EtchedBorder.LOWERED);
			int n = 0;
			String [] moduleString = null;	
			double [] values = null;

			n = 4;
			moduleString = new String[n];	
			moduleString[0] = new String( "Peudo 3D Ray Tracer");
			moduleString[1] = new String( "Fast Marching Method");
			moduleString[2] = new String( "Jing Du's");
			moduleString[3] = new String( "Vidale-Nelson (isotropic media only)");

			modulePanel = new JPanel( new GridLayout(n, 1, 5, 5));	
			ButtonGroup moduleRadioGroup = new ButtonGroup();
			JRadioButton [] moduleRadioButton = new JRadioButton[n];

			for(int i=0; i<n; i++) {
				final int j = i;
				moduleRadioButton[i] = new JRadioButton(moduleString[i], i==(_iRayTracer));
				moduleRadioButton[i].addItemListener( new ItemListener() {
					public void itemStateChanged(ItemEvent itemEvent) {
						if (itemEvent.getStateChange() == ItemEvent.SELECTED) { _iRayTracer = j; } 
					}
				});

				moduleRadioGroup.add(moduleRadioButton[i]);
				modulePanel.add(moduleRadioButton[i]);
			}
			modulePanel.setBorder(BorderFactory.createTitledBorder(loweredetched, "Travel Time Table Calculation Method:"));
			iRow++;
			gbc= new GridBagConstraints(0, iRow, 4, 1, 1.0, 0.0, GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, insets, 0, 0);
			add(modulePanel, gbc);

			iRow++;
			gbc= new GridBagConstraints(0, iRow, 4, 1, 1.0, 1.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, insets, 0, 0);
			add(new JLabel(" "), gbc);
		}
	}

	



}
