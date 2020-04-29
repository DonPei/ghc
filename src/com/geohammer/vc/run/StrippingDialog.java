package com.geohammer.vc.run;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;

import javax.swing.AbstractAction;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingWorker;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.geohammer.common.CommonDialog;
import com.geohammer.core.acquisition.VCPair;
import com.geohammer.core.planarmodel.DipLayer1D;
import com.geohammer.core.planarmodel.FlatLayer1D;
import com.geohammer.rt.pseudo3d.RayTracerFlat3D;
import com.geohammer.rt.pseudo3d.RayTracerI;
import com.geohammer.vc.VcFrame;

//public class SaDialog extends CommonDialog implements PropertyChangeListener{
public class StrippingDialog extends CommonDialog {

	private static final String START = "Start Stripping";
	private static final String STOP = "Stop Stripping";

	VcFrame			_frame 		= null;
	SaTable	[] 		_tables 	= null;
	FlatLayer1D 	_min 		= null;
	FlatLayer1D 	_max 		= null;
	FlatLayer1D 	_editable	= null;
	//FlatLayer1D 	_linkTo 	= null;

	private String 	[]	_tabNames		= null;
	private int 	[]	_index			= null;
	JTabbedPane _tabbedPane 			= null;
	private String _cwd 				= null;

	private String _currTabTitle		= null;

	private CalculatorTask 		_calcTask 	= null;
	private JProgressBar 		_progBar	= null;
	private JButton 			_run 		= null;
	//private JButton 			_cancel 	= null;

	private JTextField  	_mEpsilonTF 	= null;
	private JTextField  	_nGammaTF 		= null;
	private int 			_iType 			= 0; 

	private int 	_forceQuit;
	long _startTime;	// Get current starting time in milliseconds
	long _endTime;		// Get current ending time in milliseconds 

	private StrippingPlot _strippingPlot 	= null;
	private StrippingAlone _strippingAlone 	= null;


	public StrippingDialog(VcFrame aParent, String aTitle, boolean modal, String cwd, int iType) {
		super((JFrame)aParent, aTitle, modal);
		setDialogWindowSize(850, 600);
		//setEnableSaveButton(true);
		_frame 		= aParent;
		DipLayer1D dipLayer1D = _frame.getRayTracer().getDipLayer1D();
		_iType 		= iType;
		
		double datum = dipLayer1D.getLayer(0).getZ0();
		_min 		= dipLayer1D.toFlatLayer1D(-datum);
		_max 		= dipLayer1D.toFlatLayer1D(-datum);
		_editable 	= dipLayer1D.toFlatLayer1D(-datum);
		//_linkTo  	= dipLayer1D.toFlatLayer1D(-datum);
		_cwd 		= cwd;

		if(iType == 0) {
			_index = new int[]{20, 22};
			_tabNames = new String[]{"Epsilon", "Gamma"};
		}else if(iType == 1) {
			_index = new int[]{20, 21};
			_tabNames = new String[]{"Epsilon", "Delta"};
		} 

		double min =0;
		double max = 0;
		double limit = 0.001;
		for(int i=0; i<dipLayer1D.getNumOfBoundaries(); i++) {
			for(int j=0; j<_index.length; j++) {
				double v = dipLayer1D.getLayer(i).getLayerProperty(_index[j]);
				if(v>limit) { min = v*0.8; max = v*1.1; }
				else if(v<-limit) { min = v*1.1; max = v*0.8; }
				else { min = -0.1; max = 0.3; }

				//if(j==0) { min=0.05; max = 0.25; }
				//else { min=-0.1; max = 0.2; }

				_min.getLayer(i).setLayerProperty(_index[j], min);
				_max.getLayer(i).setLayerProperty(_index[j], max);
				_editable.getLayer(i).setLayerProperty(_index[j], 0.0);
				//_linkTo.getLayer(i).setLayerProperty(_index[j], i);
			}
		}

		_tables = new SaTable[_index.length];
		for(int i=0; i<_index.length; i++)	{
			_tables[i] = new SaTable(_index[i], dipLayer1D.toFlatLayer1D(-datum), 
					_min, _max, _editable, null);
			_tables[i].setEnableCheckBoxHeader(false);
		}
		_tables[1].setUneditableColumnIndex(5);
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				stopCal();
			}
		});
	}

	public VcFrame getFrame() 		{ return _frame; }

	private JScrollPane getTableTab(SaTable table) {
		JScrollPane scrollPane = new JScrollPane(table);
		scrollPane.setAlignmentX(LEFT_ALIGNMENT); 
		scrollPane.setColumnHeaderView(null);
		scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		scrollPane.setWheelScrollingEnabled(true);
		scrollPane.setMinimumSize(new Dimension(750, 500));     
		scrollPane.setPreferredSize(new Dimension(750, 500));
		return scrollPane;
	}
	private int tabNameIndex(String tabName) {
		if(tabName==null) return -1;
		//System.out.println(tabName);
		for(int i=0; i<_tabNames.length; i++) {
			if(tabName.equalsIgnoreCase(_tabNames[i])) return i;
		}
		return -1;
	}
	protected JTabbedPane createContents() {
		JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP, JTabbedPane.SCROLL_TAB_LAYOUT);
		// Register a change listener
		tabbedPane.addChangeListener(new ChangeListener() {
			// This method is called whenever the selected tab changes
			public void stateChanged(ChangeEvent evt) {
				JTabbedPane pane = (JTabbedPane)evt.getSource();
				// Get current tab
				int k = pane.getSelectedIndex();
				_currTabTitle = pane.getTitleAt(k).trim();
			}
		});

		for(int i=0; i<_tables.length; i++) {
			tabbedPane.addTab("  "+_tabNames[i]+"      ", getTableTab(_tables[i]));
		}
		_tabbedPane = tabbedPane;
		return tabbedPane;
	}

	public int [] getLogicLayers() {
		int k = 0;
		for(int i=0; i<1; i++) {
			Object [][] obj = _tables[i].getTableModel().getData();

			for(int ir=0; ir<obj.length; ir++) {
				if(((Boolean) (obj[ir][5])).booleanValue()) { 
					k++;
				} 
			}
		}
		if(k==0) {
			JOptionPane.showMessageDialog(this, "No layer is selected for stripping", 
					"Error", JOptionPane.ERROR_MESSAGE );
			return null;
		}
		
		int [] selectedIndex = new int[k];
		k = 0;
		for(int i=0; i<1; i++) {
			Object [][] obj = _tables[i].getTableModel().getData();

			for(int ir=0; ir<obj.length; ir++) {
				if(((Boolean) (obj[ir][5])).booleanValue()) { 
					selectedIndex[k] = ir;
					k++;
				} 
			}
		}

		return selectedIndex;
	}
	
	public int validateInput() {
		int k = -1;
		for(int i=0; i<1; i++) {
			Object [][] obj = _tables[i].getTableModel().getData();

			for(int ir=0; ir<obj.length; ir++) {
				boolean ba = ((Boolean) (obj[ir][5])).booleanValue();

				if(ba) { 
					if(k==-1) { k = ir; }
					else {
						JOptionPane.showMessageDialog(this, "Only one layer is allowed for stripping", 
								"Error", JOptionPane.ERROR_MESSAGE );
						return -1;
					}
				} 
			}
		}
		if(k==-1) {
			JOptionPane.showMessageDialog(this, "Select one layer for stripping", 
					"Error", JOptionPane.ERROR_MESSAGE );
			return -1;
		}

		return k;
	}

	protected boolean okAction() { return true; }
	protected JComponent getCommandRow() {

		_progBar = new JProgressBar(0, 100);
		_progBar.setValue(0);
		_progBar.setStringPainted(true);

		_run = new JButton(START);
		_run.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String cmd = e.getActionCommand();
				if (STOP.equals(cmd)) {
					stopCal();
					_run.setText(START);
				} else {
					RayTracerI rta = _frame.getRayTracer();
					if(rta==null) {
						String message = "Must do ray tracing first!";
						String title = "Error";
						JOptionPane.showMessageDialog(null, message, title, JOptionPane.ERROR_MESSAGE);
					} else {
						startCal(0, _frame.getRayTracer());
					}
					_run.setText(STOP);
				}
			}
		});

		JPanel panel = new JPanel();
		panel.setLayout(new GridBagLayout());
		Insets insets = new Insets(20, 2, 5, 2);
		GridBagConstraints gbc;

		int w = 0;
		gbc= new GridBagConstraints(w++, 0, 1, 1, 0.0, 1.0,
				GridBagConstraints.WEST, GridBagConstraints.BOTH, insets, 0, 0);
		panel.add(new JLabel(_tabNames[0]+" Stripping Steps: "), gbc);
		_mEpsilonTF = new JTextField("50");
		gbc= new GridBagConstraints(w++, 0, 1, 1, 1.0, 1.0,
				GridBagConstraints.WEST, GridBagConstraints.BOTH, new Insets(20, 2, 5, 2), 0, 0);
		panel.add(_mEpsilonTF, gbc);


		gbc= new GridBagConstraints(w++, 0, 1, 1, 0.0, 1.0,
				GridBagConstraints.WEST, GridBagConstraints.BOTH, insets, 0, 0);
		panel.add(new JLabel(_tabNames[1]+" Stripping Steps: "), gbc);
		_nGammaTF = new JTextField("50");
		gbc= new GridBagConstraints(w++, 0, 1, 1, 1.0, 1.0,
				GridBagConstraints.WEST, GridBagConstraints.BOTH, new Insets(20, 2, 5, 20), 0, 0);
		panel.add(_nGammaTF, gbc);

		gbc= new GridBagConstraints(w++, 0, 1, 1, 0.0, 1.0,
				GridBagConstraints.EAST, GridBagConstraints.NONE, insets, 0, 0);
		panel.add(new JLabel(), gbc);
		gbc= new GridBagConstraints(w++, 0, 1, 1, 0.0, 1.0,
				GridBagConstraints.EAST, GridBagConstraints.NONE, insets, 0, 0);
		panel.add(_run, gbc);

		this.getRootPane().setDefaultButton( _run );

		return panel;
	}

	private void addCancelByEscapeKey(){
		String CANCEL_ACTION_KEY = "CANCEL_ACTION_KEY";
		int noModifiers = 0;
		KeyStroke escapeKey = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, noModifiers, false);
		InputMap inputMap = 
				this.getRootPane().getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT)
				;
		inputMap.put(escapeKey, CANCEL_ACTION_KEY);
		AbstractAction cancelAction = new AbstractAction(){
			public void actionPerformed(ActionEvent e){
				dispose();
			}
		}; 
		this.getRootPane().getActionMap().put(CANCEL_ACTION_KEY, cancelAction);
	}

	public void updateAndRayTracing(int [] kLayers, double [] v) {		
		if(_strippingAlone==null) return;
		DipLayer1D dipLayer1D = _frame.getRayTracer().getDipLayer1D();
		FlatLayer1D layer1DR = _frame.getRayTracer().getFlatLayer1D();
		//FlatLayer1D flayer1D = _saAlone.getRayTracer(0).getFlatLayer1D();
		//System.out.println("before vp="+layer1DR.getLayer(0).getVp()+" vs="+layer1DR.getLayer(0).getVs());
		for(int i=0; i<_index.length; i++) {
			for(int k=0; k<kLayers.length; k++) {
			dipLayer1D.getLayer(kLayers[k]).setLayerProperty(_index[i], v[i]);
			layer1DR.getLayer(kLayers[k]).setLayerProperty(_index[i], v[i]);
			}

		}
		//System.out.println("after vp="+layer1DR.getLayer(0).getVp()+" vs="+layer1DR.getLayer(0).getVs());
		//_frame.updateLayer(_index, layer2D);
		_frame.redrawAndUpdateLayer(_index, layer1DR);
		_frame.getRayTracer().start(2);
	}

	public void startCal(int iRun, RayTracerFlat3D rt) {
		_startTime = System.currentTimeMillis(); 
		//int kLayer = validateInput();
		//if(kLayer<0) return;
		
		int [] kLayers = getLogicLayers();
		if(kLayers==null) return;
		
		FlatLayer1D flatLayer1D = rt.getFlatLayer1D();

		for(int i=0; i<_index.length; i++) {
			Object [][] obj = _tables[i].getTableModel().getData();
			for(int ir=0; ir<obj.length; ir++) {
				flatLayer1D.getLayer(ir).setLayerProperty(_index[i], Double.parseDouble(obj[ir][2].toString().trim()));
				boolean ba = ((Boolean) (obj[ir][5])).booleanValue();
				_editable.getLayer(ir).setLayerProperty(_index[i], ba?1:0);
				_min.getLayer(ir).setLayerProperty(_index[i], Double.parseDouble(obj[ir][3].toString().trim()));
				_max.getLayer(ir).setLayerProperty(_index[i], Double.parseDouble(obj[ir][4].toString().trim()));
			}
		}

		int m = (int)(Double.parseDouble(_mEpsilonTF.getText().trim()));
		int n = (int)(Double.parseDouble(_nGammaTF.getText().trim()));
		double [][] steps = new double[2][];
		steps[0] = new double[m];
		steps[1] = new double[n];
		for(int i=0; i<_index.length; i++) {
			double x0 = _min.getLayer(kLayers[0]).getLayerProperty(_index[i]);
			double x1 = _max.getLayer(kLayers[0]).getLayerProperty(_index[i]);
			double dx = (x1-x0)/(steps[i].length-1);
			for(int j=0; j<steps[i].length; j++) {
				steps[i][j] = x0+j*dx;
			}
		}

		FlatLayer1D layer1D = rt.getFlatLayer1D().copy();
		VCPair vcPW = rt.getVCPair().copy();
		//System.out.println(layer1D.toString());

		RayTracerFlat3D rayTracer = new RayTracerFlat3D(null, layer1D, vcPW, 1, rt.getIVTI(), 
				rt.getIApproximation(), rt.getReflectorIndex(), rt.getPsIndex(), rt.getIWave(), rt.getTolerance(), 0, 0);
		rayTracer.start();

		//System.out.println("vcPW="+vcPW.toString());
		//rayTracer.setIVp(1); rayTracer.setConfiguration(); rayTracer.start();
		//double rms = vcPW.getRMS(1, 0, 0);
		//System.out.println("rms="+rms);


		StrippingPlot strippingPlot = new StrippingPlot(this, false, _iType, "Anisotropy Stripping", 
				_tabNames[1], _tabNames[0], steps);
		strippingPlot.showDialog();

		if(_cwd==null) {
			_cwd = _frame.genSaCwd();
			_frame.setSaCwd(_cwd);
		}
		//System.out.println("cwd_"+iType+"="+_cwd);

		_calcTask = new CalculatorTask(iRun, rayTracer, strippingPlot, 1, 
				kLayers, steps, _index, _cwd);
		//_calcTask.addPropertyChangeListener(this);
		// Execute task in worker thread.
		_calcTask.execute(); 
	}

	public void stopCal() {
		//savePreferences();
		// Is a Calculator Task running in background?
		if (_calcTask != null  &&  !_calcTask.isDone()) {
			//LOG.fine("Calculator Task is running. Cancel it.");
			_forceQuit = 1;
			_calcTask.cancel(true);
			//LOG.fine("Stop Progress Timer");
			//_progTimer.stop();
		} else {
			//setVisible( false );
			//dispose();
		}
		//LOG.fine("Exit AnisoLSFrame stopCalc");
	}

	public class CalculatorTask extends SwingWorker<SaToken, SaToken> implements SaWorkerI {	

		StrippingPlot _strippingPlot = null;
		int [] _kLayers = null;
		double [] _minValues = null;
		public CalculatorTask(int iRun, RayTracerFlat3D rayTracer, StrippingPlot strippingPlot, 
				int iRMS, int [] kLayers, double [][] steps, int [] index, String cwd) {
			_strippingPlot = strippingPlot;
			_kLayers = kLayers;

			_strippingAlone = new StrippingAlone(rayTracer, 1, kLayers, steps, 
					index, cwd, CalculatorTask.this);

			setCursor(new Cursor(Cursor.WAIT_CURSOR));
		}

		public void update(SaToken saToken) { publish(saToken); }

		@Override
		protected void process(List<SaToken>  saTokens) {
			for(SaToken saToken : saTokens){
				//RayTracerFlat3D rayTracer = saToken.getRayTracerFlat3D();
				double [][] rmsArray = saToken.getRmsArray();
				if(_strippingPlot!=null) {
					_strippingPlot.getStrippingPixelPanel().update(rmsArray);
				}
			}
		}

		@Override
		protected SaToken doInBackground() throws InterruptedException {
			while (isCancelled()) return null;
			_strippingAlone.start();
			return _strippingAlone.getToken();
		}

		@Override
		protected void done() {
			_progBar.setValue( 100 );
			try {
				SaToken saToken = get();
				if(saToken!=null) {
					//RayTracerFlat3D rayTracer = saToken.getRayTracerFlat3D();

					double [][] rmsArray = saToken.getRmsArray();
					if(_strippingPlot!=null) {
						_strippingPlot.getStrippingPixelPanel().update(rmsArray);
						_strippingPlot.getStrippingPixelPanel().addColorBar();
						_strippingPlot.getStrippingPixelPanel().addBackgroundGrid(false);
						_minValues = _strippingPlot.getStrippingPixelPanel().calMinValues();
						_strippingPlot.getStrippingPixelPanel().updateContourViewDouble(12, 1.0f, Color.white);
					}

					_endTime = System.currentTimeMillis();
					float elapsedTimeMillis = _endTime - _startTime;
					float elapsedTimeSec = elapsedTimeMillis/1000F; 
					float elapsedTimeMin = elapsedTimeMillis/(60*1000F); 
					//float elapsedTimeHour = elapsedTimeMillis/(60*60*1000F);
					//float elapsedTimeDay = elapsedTimeMillis/(24*60*60*1000F);

					String aTime = new String("Successfully done with elapsed time: " + 
							(int)(elapsedTimeSec) + " second(s). \n");
					if( elapsedTimeMin>1.0 ) {
						elapsedTimeSec = elapsedTimeSec - (int)(elapsedTimeMin)*60;
						String bTime = new String(" Or " + (int)(elapsedTimeMin) +
								" minute(s) " + " and " + (int)(elapsedTimeSec) + " second(s). \n");
						aTime = new String(aTime.concat(bTime));
					}

					double vmI = _minValues[0]; //m
					double vnJ = _minValues[1];
					double min = _minValues[2];
					String htmlText = "<html>" + "<font size=\"4\" color=\"black\"><b>"+ 
							" min RMS="+ ((int)(10000*min))/10000.0 +" at x="+
							((int)(10000*vnJ))/10000.0 +" y="+((int)(10000*vmI))/10000.0 +" </b></font>  ";

					String updateString = "\n\n" + htmlText + 
							"\n\n\nDo you want to update model in CPU? ";

					int answer = JOptionPane.showConfirmDialog(null, aTime+updateString);
					if (answer == JOptionPane.YES_OPTION) {
						updateAndRayTracing(_kLayers, _minValues);
					} else if (answer == JOptionPane.NO_OPTION) {
						//System.out.println("no update now");
					}
					_calcTask 	= null;		
				}
			} catch(InterruptedException e) {
				System.out.println("Iterrupted while waiting for results.");
			} catch(ExecutionException e) {
				System.out.println("Error performing computation.");
			} catch (CancellationException e) {
				if(_strippingAlone!=null) _strippingAlone.setCancel(true);
				if(_strippingPlot!=null) _strippingPlot.setVisible(false);
				System.out.println("User cancelled.");
			}	

			//_run.setEnabled(true);	
			_run.setText(START);
			setCursor( null );
		}
	}
}
