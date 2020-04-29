package com.geohammer.vc.run;

import static org.monte.media.VideoFormatKeys.QualityKey;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Label;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.StringTokenizer;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;

import javax.imageio.ImageIO;
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
import javax.swing.JProgressBar;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.monte.media.Buffer;
import org.monte.media.Format;
import org.monte.media.FormatKeys;
import org.monte.media.MovieWriter;
import org.monte.media.Registry;
import org.monte.media.VideoFormatKeys;
import org.monte.media.FormatKeys.MediaType;
import org.monte.media.avi.AVIWriter;
import org.monte.media.math.Rational;

import com.geohammer.common.CommonDialog;
import com.geohammer.common.CommonFrame;
import com.geohammer.common.CommonPointPanel;
import com.geohammer.core.SeisPTSrcMech;
import com.geohammer.core.acquisition.VCPair;
import com.geohammer.core.planarmodel.DipLayer1D;
import com.geohammer.core.planarmodel.FlatLayer1D;
import com.geohammer.core.planarmodel.SingleFlatLayer;
import com.geohammer.rt.pseudo3d.FrameI;
import com.geohammer.rt.pseudo3d.RayTracerFlat3D;
import com.geohammer.rt.pseudo3d.RayTracerI;
import com.geohammer.vc.VcFrame;
import com.geohammer.vc.dialog.PlotDialog;

import edu.mines.jtk.util.ArrayMath;

//public class SaDialog extends CommonDialog implements PropertyChangeListener{
public class SaDialog extends CommonDialog {
	
	private static final String START = "Start SA";
    private static final String STOP = "Stop SA";
    
    VcFrame			_frame 		= null;
	SaTable	[] 		_tables 	= null;
	FlatLayer1D 	_init 		= null;
	FlatLayer1D 	_final 		= null;
	FlatLayer1D 	_min 		= null;
	FlatLayer1D 	_max 		= null;
	FlatLayer1D 	_editable	= null;
	FlatLayer1D 	_linkTo 	= null;
	
	//rms curve movie
	private boolean 	_enableRmsMovie = false;
	private String  	_rmsMovieFileName = null;
	private String [] 	_rmsImageFileNames = null;
	private int 		_rmsImageFileIndex = 0;
		
	//SA parameters
	private boolean 	_enableInternalCheck = true; 
	private int 		_coolingSchedule = 5; // 2 for BSA 5 for VFSA
	private int 		_saLength 		= 7000;
	private int 		_eqLength 		= 0;
	private double 		_T0 			= 100.0;
	private double 		_tol 			= 1.0e-5;
	private double 		_noise 			= 0.05;
	private double 		_tuneParameter 	= 0.02;
	private JTextField 	_tolTF 			= null;
	private JTextField 	_saLengthTF 	= null;
	private JTextField 	_eqLengthTF 	= null;
	private JTextField 	_noiseTF 		= null;
	private JTextField 	_T0TF 			= null;
	private JTextField 	_tuneParameterTF = null;

	//reflection parameters
	private boolean 	_reflectionEnabled 	= false; 
	private JTextField 	_rayCodeTF1 	= null;
	private JTextField 	_psCodeTF1 		= null;
	private int 		_ttFileType1 	= 0;
	private JTextField 	_pckTF1 		= null;
	private JTextField 	_rayCodeTF2 	= null;
	private JTextField 	_psCodeTF2 		= null;
	private int 		_ttFileType2 	= 0;
	private JTextField 	_pckTF2 		= null;


	private JTextField [] _minTF 		= null;
	private JTextField [] _maxTF 		= null;
	private String 	[]	_tabNames		= null;
	private int 	[]	_index			= null;
	private int 	[]	_minIndex		= null;
	private int 	[]	_maxIndex		= null;
	private String	[] 	_percentageString = null;
	JTabbedPane _tabbedPane 			= null;
	private String _cwd 				= null;

	private String _currTabTitle		= null;

	private CalculatorTask 		_calcTask 	= null;
	private JProgressBar 		_progBar	= null;
	private JButton 			_run 		= null;
	//private JButton 			_cancel 	= null;
	private JComboBox 			_minCombo 	= null;
	private JComboBox 			_maxCombo 	= null;
	private boolean 			_userComboEvent = true;

	private int 	_iType = 1; 
	//=0 vp+vs =1 aniso =2 both vel and aniso =10 vp only =11 Delta+Epsilon =12 vp+Delta+Epsilon
	private int 	_iMethod = 0; //=0 SA =1 LMA 
	private int 	_forceQuit;
	long _startTime;	// Get current starting time in milliseconds
	long _endTime;		// Get current ending time in milliseconds 

	private SaPlot _saPlot 	= null;
	private SaAlone _saAlone = null;

	public SaDialog(VcFrame aParent, String aTitle, boolean modal, int iType, int iMethod, String cwd) {
		super((JFrame)aParent, aTitle, modal);
		setDialogWindowSize(850, 600);
		//setEnableSaveButton(true);
		_frame 		= aParent;
		_iMethod 	= iMethod;
		//Layer2D layer2D = _frame.getProject().getLayer();
		//System.out.println("1 vp="+layer2D.getLayer(0).getVp()+" vs="+layer2D.getLayer(0).getVs());
		//_title 		= aTitle;
		DipLayer1D dipLayer1D = _frame.getRayTracer().getDipLayer1D();
		
		//System.out.println("name="+_dipLayer1D.toString());
		double datum = dipLayer1D.getLayer(0).getZ0();
		_min 		= dipLayer1D.toFlatLayer1D(-datum);
		_max 		= dipLayer1D.toFlatLayer1D(-datum);
		_editable 	= dipLayer1D.toFlatLayer1D(-datum);
		_linkTo 	= dipLayer1D.toFlatLayer1D(-datum);
		_iType 		= iType;
		_cwd 		= cwd;
		//System.out.println("min="+_min.toString());
		//System.out.println("max="+_max.toString());
		
		for(int i=0; i<dipLayer1D.getNumOfBoundaries(); i++) {
			SingleFlatLayer sl = dipLayer1D.getLayer(i);
			sl.setVpToVs(sl.getVp()/sl.getVs());
			sl = _min.getLayer(i);
			sl.setVpToVs(sl.getVp()/sl.getVs());
			sl = _max.getLayer(i);
			sl.setVpToVs(sl.getVp()/sl.getVs());
			sl = _editable.getLayer(i);
			sl.setVpToVs(sl.getVp()/sl.getVs());
			sl = _linkTo.getLayer(i);
			sl.setVpToVs(sl.getVp()/sl.getVs());
		}

		if(iType==0) {
			_index = new int[]{21, 20, 22};
			_tabNames = new String[]{"Delta", "Epsilon", "Gamma"};
		} else if(iType==1) {
			_index = new int[]{1, 3};
			_tabNames = new String[]{"Vp", "Vp/Vs"};
		} else if(iType==2){
			_index = new int[]{1, 3, 21, 20, 22};
			_tabNames = new String[]{"Vp", "Vp/Vs", "Delta", "Epsilon", "Gamma"};
		} else if(iType==10) {
			_index = new int[]{21, 20};
			_tabNames = new String[]{"Delta", "Epsilon"};
		} else if(iType==11) {
			_index = new int[]{1};
			_tabNames = new String[]{"Vp"};
		} else if(iType==12){
			_index = new int[]{1, 21, 20};
			_tabNames = new String[]{"Vp", "Delta", "Epsilon"};
		}
		
		_minIndex = new int[_tabNames.length];
		_maxIndex = new int[_tabNames.length];
		for(int i=0; i<_minIndex.length; i++) {
			_minIndex[i] = 10;
			_maxIndex[i] = 5;
		}
		_percentageString = new String[]{"0%", "1%", "2%", "3%", "4%", "5%", "6%", "8%", "10%", 
				"12%", "14%", "16%", "18%", "20%", "22%", "25%", "30%", "35%", "40%", 
				"50%", "75%", "100%", "125%", "150%", "175%", "200%"};

		double min =0;
		double max = 0;
		double limit = 0.001;
		
		for(int i=0; i<dipLayer1D.getNumOfBoundaries(); i++) {
			for(int j=0; j<_index.length; j++) {
				double v = dipLayer1D.getLayer(i).getLayerProperty(_index[j]);
				if(v>limit) { min = v*0.8; max = v*1.1; }
				else if(v<-limit) { min = v*1.1; max = v*0.8; }
				else { min = -0.1; max = 0.3; }
				_min.getLayer(i).setLayerProperty(_index[j], min);
				_max.getLayer(i).setLayerProperty(_index[j], max);
				_editable.getLayer(i).setLayerProperty(_index[j], 1.0);
				_linkTo.getLayer(i).setLayerProperty(_index[j], i+1);
				if(_index[j]==1) {
					int k = 3;
					double r = 0.0;
					_min.getLayer(i).setLayerProperty(k, r);
					_max.getLayer(i).setLayerProperty(k, r);
					_editable.getLayer(i).setLayerProperty(k, r);
					_linkTo.getLayer(i).setLayerProperty(k, r);	
				}
			}
			//System.out.println("minVpVs="+_min.getLayer(i).getLayerProperty(3)+" maxVpVs="+_max.getLayer(i).getLayerProperty(3));
			
		}

		_tables = new SaTable[_index.length];
		for(int i=0; i<_index.length; i++)	_tables[i] = new SaTable(_index[i], 
				dipLayer1D.toFlatLayer1D(-datum), _min, _max, _editable, _linkTo);
		
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
				int j = tabNameIndex(_currTabTitle);
				if(j>=0) {
					if(_minCombo!=null) {
						if(!_minCombo.isEnabled()) _minCombo.setEnabled(true);
						_userComboEvent = false;
						_minCombo.setSelectedIndex(_minIndex[j]);
					}
					if(_maxCombo!=null) {
						if(!_maxCombo.isEnabled()) _maxCombo.setEnabled(true);
						_userComboEvent = false;
						_maxCombo.setSelectedIndex(_maxIndex[j]);
					}
				} else {
					if(_minCombo!=null) {
						_userComboEvent = false;
						_minCombo.setSelectedIndex(0);
						_minCombo.setEnabled(false);
					}
					if(_maxCombo!=null) {
						_userComboEvent = false;
						_maxCombo.setSelectedIndex(0);
						_maxCombo.setEnabled(false);
					}
				}
			}
		});

		tabbedPane.addTab("  SA Parameters   ", new SaInputPanel(_saLength, _eqLength, _T0, _tol, _noise, _tuneParameter));

		//JPanel scrollPane = new JPanel(new GridLayout(_tables.length, 1));
		//_tabNames = new String[]{"Depth", "Vp", "Vs", "Delta", "Epsilon", "Gamma"};
		double [] min = new double[_tabNames.length];
		for(int i=0; i<_tabNames.length; i++) min[i] = 0;
		double [] max = new double[_tabNames.length];
		for(int i=0; i<_tabNames.length; i++) max[i] = 1;

		for(int i=0; i<_tables.length; i++) {
			tabbedPane.addTab("  "+_tabNames[i]+"      ", getTableTab(_tables[i]));
		}
		//tabbedPane.addTab("  Reflections   ", new ReflectionPanel(" "));
		_tabbedPane = tabbedPane;
		return tabbedPane;
	}

	public void getInput() {
		int n  = 0;
		int k1 = 100000;
		int k2 = -1;
		//System.out.println("vcPW1="+_vcPW.toString());
		//DipLayer1D dipLayer1D = _frame.getRayTracer().getDipLayer1D();
		FlatLayer1D flatLayer1D = _frame.getRayTracer().getFlatLayer1D();
		VCPair vcPW 		= _frame.getRayTracer().getVCPair();
		//internal check
		//System.out.println("vcPW="+vcPW.toString());
		//System.out.println("min="+_min.toString());
		if(_enableInternalCheck) {
			for(int i=0, k=0; i<vcPW.getNEvents(); i++) {
				n = flatLayer1D.getLayerIndex(vcPW.getED(i));
				if(n<k1) k1 = n;
				if(n>k2) k2 = n;
				for(int j=0; j<vcPW.getFlag(i); j++, k++) {
					n = flatLayer1D.getLayerIndex(vcPW.getRD(k));
					if(n<k1) k1 = n;
					if(n>k2) k2 = n;
				}
			}
		} else {
			k1 = -1;
			k2 = 100000;
		}
		System.out.println("k1="+k1+" k2="+k2+" iType="+_iType+" k="+_index.length);

		for(int i=0; i<_index.length; i++) {
			Object [][] obj = _tables[i].getTableModel().getData();

			for(int ir=0; ir<obj.length; ir++) {
				flatLayer1D.getLayer(ir).setLayerProperty(_index[i], Double.parseDouble(obj[ir][2].toString().trim()));
				//dipLayer1D.getLayer(ir).setLayerProperty(_index[i], Double.parseDouble(obj[ir][2].toString().trim()));
				boolean ba = ((Boolean) (obj[ir][5])).booleanValue();
				boolean bb = false;
				if(ba) { 
					if(ir>=k1 && ir<=k2) bb = true;
				} 

				if(bb) { 
					_editable.getLayer(ir).setLayerProperty(_index[i], 1);
					_min.getLayer(ir).setLayerProperty(_index[i], Double.parseDouble(obj[ir][3].toString().trim()));
					_max.getLayer(ir).setLayerProperty(_index[i], Double.parseDouble(obj[ir][4].toString().trim()));
				} else  {
					_editable.getLayer(ir).setLayerProperty(_index[i], 0);
					_min.getLayer(ir).setLayerProperty(_index[i], 0);
					_max.getLayer(ir).setLayerProperty(_index[i], 0);
				}
				_linkTo.getLayer(ir).setLayerProperty(_index[i], Integer.parseInt(obj[ir][6].toString().trim()));
			}
		}

		//System.out.println("minLayer2="+_min.toString());
		//System.out.println("maxLayer2="+_max.toString());

		_saLength 		= Integer.parseInt(_saLengthTF.getText().trim());
		_eqLength 		= Integer.parseInt(_eqLengthTF.getText().trim());
		_T0 			= Double.parseDouble(_T0TF.getText().trim());
		_tol 			= Double.parseDouble(_tolTF.getText().trim());
		_noise 			= Double.parseDouble(_noiseTF.getText().trim());
		_tuneParameter 	= Double.parseDouble(_tuneParameterTF.getText().trim());
	}

	public int refreshAction() {
		boolean [] skips = new boolean[_tables.length];
		for(int i=0; i<_tables.length; i++) { _tabbedPane.setForegroundAt(i+1, Color.black); }
		for(int i=0; i<_tables.length; i++) {
			boolean skip = true;
			Object [][] data = _tables[i].getTableModel().getData();
			for(int j1=0; j1<data.length; j1++) {
				Boolean f = (Boolean)(_tables[i].getTableModel().getValueAt(j1, 5));
				if(f.booleanValue()) skip = false;
			}
			if(skip) _tabbedPane.setForegroundAt(i+1, Color.red);
			skips[i] = skip;
		}

		//		if(skips[0]&&skips[2]&&skips[3]) return 2;
		//		else if(skips[1]&&skips[4]) return 1;
		//		else return -1;
		return 0;
	}
	public void refreshAction1() {
		double min = 0.0;
		double max = 1.0;
		for(int i=0; i<_tabNames.length; i++) {
			//min = Double.parseDouble(_minTF[i].getText().trim());
			//max = Double.parseDouble(_maxTF[i].getText().trim());
			if(min==max) {
				JScrollPane scrollPane = (JScrollPane)_tabbedPane.getComponentAt(i+1);
				Component[] components =scrollPane.getViewport().getComponents();
				for (int i1 = 0; i1 < components.length; i1++) {
					if (components[i1] instanceof JTable) {
						SaTable table = (SaTable)components[i1];
						Object [][] data = table.getTableModel().getData();
						for(int j1=0; j1<data.length; j1++) table.getTableModel().setValueAt(new Boolean(false), j1, 5);
					}
				}
				_tabbedPane.setForegroundAt(i+1, Color.red);
			} else {
				boolean skip = true;
				JScrollPane scrollPane = (JScrollPane)_tabbedPane.getComponentAt(i);
				Component[] components =scrollPane.getViewport().getComponents();
				for (int i1 = 0; i1 < components.length; i1++) {
					if (components[i1] instanceof JTable) {
						SaTable table = (SaTable)components[i1];
						Object [][] data = table.getTableModel().getData();
						for(int j1=0; j1<data.length; j1++) {
							Boolean f = (Boolean)(table.getTableModel().getValueAt(j1, 5));
							if(f.booleanValue()) skip = false;
						}
					}
				}
				if(skip) _tabbedPane.setForegroundAt(i, Color.red);
			}
		}
	}

	public void updateAndRayTracing(FlatLayer1D flayer1D) {		
		if(_saAlone==null) return;
		//Layer2D layer2D = _frame.getProject().getLayer();
		DipLayer1D dipLayer1D = _frame.getRayTracer().getDipLayer1D();
		FlatLayer1D layer1DR = _frame.getRayTracer().getFlatLayer1D();
		//FlatLayer1D flayer1D = _saAlone.getRayTracer(0).getFlatLayer1D();
		//System.out.println("before vp="+layer1DR.getLayer(0).getVp()+" vs="+layer1DR.getLayer(0).getVs());
		for(int i=0; i<_index.length; i++) {
			for(int j=0; j<flayer1D.getNumOfBoundaries(); j++) {
				dipLayer1D.getLayer(j).setLayerProperty(_index[i], flayer1D.getLayer(j).getLayerProperty(_index[i]));
				layer1DR.getLayer(j).setLayerProperty(_index[i], flayer1D.getLayer(j).getLayerProperty(_index[i]));
				if(_index[i]==3) {
					double vs = flayer1D.getLayer(j).getVp()/flayer1D.getLayer(j).getVpToVs();
					layer1DR.getLayer(j).setLayerProperty(2, vs);
					dipLayer1D.getLayer(j).setLayerProperty(2, vs);
				}
			}
		}
		//System.out.println("after vp="+layer1DR.getLayer(0).getVp()+" vs="+layer1DR.getLayer(0).getVs());
		//_frame.updateLayer(_index, layer2D);
		_frame.redrawAndUpdateLayer(_index, flayer1D);
		_frame.getRayTracer().start(2);
	}

	public void checkConstraints() {
		double lowerLimitP2S = Math.sqrt(4.0/3.0);
		String name = _currTabTitle.trim();
		if(!name.contains("Vp/Vs")) return;
		
		int jCol = 3;
		Object [][] obj = _tables[1].getTableModel().getData();
		double [] min = new double[obj.length];
		for(int i=0; i<min.length; i++) {
			min[i] = ((Double)obj[i][jCol]).doubleValue();
		}
		
		for(int i=0; i<min.length; i++) {
			if(min[i]<lowerLimitP2S) {
				min[i] = ((int)(lowerLimitP2S*1000))/1000.0;
			}
			_tables[1].getTableModel().setValueAt(min[i], i, jCol);
		}
	}
	public void updateMinMax(String tabName, int iType, int iCol, boolean isMin, int index) {
		String percentage = _percentageString[index];
		String percentage1 = percentage.replaceAll("%", ""); // Check for the '%' symbol and delete it.
		double scalor = Double.parseDouble(percentage1.trim());
		if(isMin) scalor = -0.01*scalor;
		else scalor = 0.01*scalor;
		System.out.println(scalor+" "+percentage+" "+percentage1);
		updateMinMax(tabName, iType, iCol, scalor);
	}
	public void updateMinMax(String tabName, int iType, int iCol, double scalor) {
		int i = 0;
		double lowerLimitP2S = Math.sqrt(4.0/3.0);
		//String name = _currTabTitle.trim();
		
		if(tabName.contains("SA")) return;
		if(tabName.contains("Reflections")) return;

		if(iType==0||iType==10) {
			if(tabName.equalsIgnoreCase("Delta")) {
				i = 0;
			} else if(tabName.equalsIgnoreCase("Epsilon")) {
				i = 1;
			} else if(tabName.equalsIgnoreCase("Gamma")) {
				i = 2;
			}
		} else if(iType==1||iType==11) {
			if(tabName.equalsIgnoreCase("Vp")) {
				i = 0;
			} else if(tabName.equalsIgnoreCase("Vp/Vs")) {
				i = 1;
			} 
		} else {
			_index = new int[]{1, 3, 21, 20, 22};
			_tabNames = new String[]{"Vp", "Vp/Vs", "Delta", "Epsilon", "Gamma"};
			if(tabName.equalsIgnoreCase("Vp")) {
				i = 0;
			} else if(tabName.equalsIgnoreCase("Vp/Vs")) {
				i = 1;
			} else if(tabName.equalsIgnoreCase("Delta")) {
				i = 2;
			} else if(tabName.equalsIgnoreCase("Epsilon")) {
				i = 3;
			} else if(tabName.equalsIgnoreCase("Gamma")) {
				i = 4;
			}
		}

		int k = 1;
		if(tabName.equalsIgnoreCase("Vp")||tabName.equalsIgnoreCase("Vp/Vs")) {
			k = 0;
		}
		
		Object [][] data = _tables[i].getTableModel().getData();
		for(int j=0; j<data.length; j++) {
			double y = Double.parseDouble(data[j][2].toString().trim());
			double dx = Math.abs(y)*scalor;
			double x = y+dx;
			if(k==0) {
				x = ((int)(x*1000))/1000.0;
			} else {
				x = ((int)(x*1000))/1000.0;
			}
			if(tabName.equalsIgnoreCase("Vp/Vs")) {
				if(x<lowerLimitP2S) {
					x = lowerLimitP2S;
					x = ((int)(x*1000))/1000.0;
				}
			}
			_tables[i].getTableModel().setValueAt(x, j, iCol);
		}
	}

	protected boolean okAction() { return true; }
	protected JComponent getCommandRow() {
//		JButton update = new JButton("Update");
//		update.setToolTipText("update velocity in memory using the inverted results");
//		update.addActionListener( new ActionListener() {
//			public void actionPerformed(ActionEvent event) {
//				updateAction();
//			}
//		});
		JButton constraints = new JButton("Constraints");
		constraints.setToolTipText("reality check upper and lower limit");
		constraints.addActionListener( new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				checkConstraints();
			}
		});
		JButton refresh = new JButton("Refresh");
		refresh.setToolTipText("refresh table");
		refresh.addActionListener( new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				refreshAction();
			}
		});
//		_run = new JButton("Run...");
//		_run.setToolTipText("start massive calculation");
//		_run.addActionListener( new ActionListener() {
//			public void actionPerformed(ActionEvent event) {
//				RayTracerI rta = _frame.getRayTracer();
//				if(rta==null) {
//					String message = "Must do ray tracing first!";
//					String title = "Error";
//					JOptionPane.showMessageDialog(null, message, title, JOptionPane.ERROR_MESSAGE);
//				} else {
//					startCal(_iMethod, 0, _iType, _frame.getRayTracer());
//				}
//			}
//		});
//		_cancel = new JButton("Stop");
//		_cancel.setToolTipText("terminate massive calculation");
//		_cancel.addActionListener( new ActionListener() {
//			public void actionPerformed(ActionEvent event) {
//				stopCal();
//			}
//		});

		_progBar = new JProgressBar(0, 100);
		_progBar.setValue(0);
		_progBar.setStringPainted(true);
		//		List<JComponent> buttons = new ArrayList<JComponent>();
		//		//if(_isEditable) buttons.add( update );
		//		buttons.add( _progBar );
		//		buttons.add( refresh );
		//		buttons.add( _run );
		//		buttons.add( _cancel );

		//final JButton run = new JButton(START);
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
						startCal(_iMethod, 0, _frame.getRayTracer());
					}
					_run.setText(STOP);
				}
			}
		});
	        
		JPanel panel = new JPanel();
		panel.setLayout(new GridBagLayout());
		Insets insets = new Insets(20, 2, 5, 2);
		GridBagConstraints gbc;

//		String[] minComboTypes = {"0", "1%", "2%", "3%", "4%", "5%", "6%", "8%", "10%", 
//				"12%", "14%", "16%", "18%", "20%", "22%", "25%", "30%", "35%", "40%", 
//				"50%", "75%", "100%", "125%", "150%", "175%", "200%"};
		_minCombo= new JComboBox(_percentageString);
		_minCombo.setToolTipText("min limit = (1-min%)*current_value");
		_minCombo.setSelectedIndex(0);
		_minCombo.setEnabled(false);
		_minCombo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JComboBox jcmbType = (JComboBox) e.getSource();
				String percentage = (String) jcmbType.getSelectedItem();
				percentage = percentage.replaceAll("%", ""); // Check for the '%' symbol and delete it.
				double scalor = Double.parseDouble(percentage.trim());
				scalor = -0.01*scalor;
				if(_userComboEvent) updateMinMax(_currTabTitle.trim(), _iType, 3, scalor);
				int k = tabNameIndex(_currTabTitle);
				if(k>=0) { _minIndex[k] = jcmbType.getSelectedIndex(); }
				_userComboEvent = true;
			}
		});
		//http://www.experts-exchange.com/Programming/Languages/Java/Q_20083642.html
//		Component[] comps = _minCombo.getComponents();
//		System.out.println("nameLength= "+comps.length);
//		for(int i = 0; i < comps.length; i++) {
//			System.out.println("name= "+comps[i].getName());
//			final int j = i;
//			comps[i].addMouseListener(new MouseAdapter() {
//				public void mouseClicked(MouseEvent me) {
//					System.out.println("clicked "+j);
//					updateMinMax(_currTabTitle.trim(), _iType, 3, true, j);
//					int k = tabNameIndex(_currTabTitle);
//					if(k>=0) { _minIndex[k] = j; }
//				}
//			});
//		}
		
		int w = 0;
		gbc= new GridBagConstraints(w++, 0, 1, 1, 0.0, 1.0,
				GridBagConstraints.WEST, GridBagConstraints.BOTH, insets, 0, 0);
		panel.add(new JLabel("min%: "), gbc);

		gbc= new GridBagConstraints(w++, 0, 1, 1, 1.0, 1.0,
				GridBagConstraints.WEST, GridBagConstraints.BOTH, new Insets(20, 2, 5, 2), 0, 0);
		panel.add(_minCombo, gbc);

//		String[] maxComboTypes = {"0", "1%", "2%", "3%", "4%", "5%", "6%", "8%", "10%", 
//				"12%", "14%", "16%", "18%", "20%", "22%", "25%", "30%", "35%", "40%", 
//				"50%", "75%", "100%", "125%", "150%", "175%", "200%"};
		_maxCombo = new JComboBox(_percentageString);
		_maxCombo.setToolTipText("max limit would be (1+max%)*current_value");
		_maxCombo.setSelectedIndex(0);
		_maxCombo.setEnabled(false);
		_maxCombo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JComboBox jcmbType = (JComboBox) e.getSource();
				String percentage = (String) jcmbType.getSelectedItem();
				percentage = percentage.replaceAll("%", ""); 
				double scalor = Double.parseDouble(percentage.trim());
				scalor = 0.01*scalor;
				if(_userComboEvent) updateMinMax(_currTabTitle.trim(), _iType, 4, scalor);
				int k = tabNameIndex(_currTabTitle);
				if(k>=0) { _maxIndex[k] = jcmbType.getSelectedIndex(); }
				_userComboEvent = true;
			}
		});
//		comps = _maxCombo.getComponents();
//		for(int i = 0; i < comps.length; i++) { 
//			final int j = i;
//			comps[i].addMouseListener(new MouseAdapter() {
//				public void mouseClicked(MouseEvent me) {
//					updateMinMax(_currTabTitle.trim(), _iType, 4, false, j);
//					int k = tabNameIndex(_currTabTitle);
//					if(k>=0) { _maxIndex[k] = j; }
//				}
//			});
//		}

		gbc= new GridBagConstraints(w++, 0, 1, 1, 0.0, 1.0,
				GridBagConstraints.WEST, GridBagConstraints.BOTH, insets, 0, 0);
		panel.add(new JLabel("max%: "), gbc);
		gbc= new GridBagConstraints(w++, 0, 1, 1, 1.0, 1.0,
				GridBagConstraints.WEST, GridBagConstraints.BOTH, new Insets(20, 2, 5, 20), 0, 0);
		panel.add(_maxCombo, gbc);

//		gbc= new GridBagConstraints(w++, 0, 1, 1, 0.0, 1.0,
//				GridBagConstraints.EAST, GridBagConstraints.NONE, insets, 0, 0);
//		panel.add(constraints, gbc);
//		gbc= new GridBagConstraints(w++, 0, 1, 1, 0.0, 1.0,
//				GridBagConstraints.EAST, GridBagConstraints.NONE, insets, 0, 0);
//		panel.add(refresh, gbc);
		gbc= new GridBagConstraints(w++, 0, 1, 1, 0.0, 1.0,
				GridBagConstraints.EAST, GridBagConstraints.NONE, insets, 0, 0);
		panel.add(_run, gbc);
//		gbc= new GridBagConstraints(w++, 0, 1, 1, 0.0, 1.0,
//				GridBagConstraints.EAST, GridBagConstraints.NONE, insets, 0, 0);
//		panel.add(_run, gbc);
		gbc= new GridBagConstraints(w++, 0, 1, 1, 0.0, 1.0,
				GridBagConstraints.EAST, GridBagConstraints.NONE, insets, 0, 0);
		//panel.add(_cancel, gbc);
		gbc= new GridBagConstraints(w++, 0, 1, 1, 0.0, 1.0,
				GridBagConstraints.EAST, GridBagConstraints.NONE, insets, 0, 0);
		//panel.add(update, gbc);

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
	public VCPair getAz(VCPair vcPW) {
		VCPair azPW = vcPW.copy();
		double angle = 0;
		double sum = 0.0;
		for(int i=0, k=0; i<vcPW.getNEvents(); i++) {
			for(int j=0; j<vcPW.getFlag(i); j++, k++) {
				double diffE = vcPW.getEE(i)-vcPW.getRE(k);
				double diffN = vcPW.getEN(i)-vcPW.getRN(k);
				angle = Math.atan(diffE/diffN);
				if(diffE>0&&diffN<0) angle += 3.1415926;
				else if(diffE<0&&diffN<0) angle += 3.1415926;
				else if(diffE<0&&diffN>0) angle += 2.0*3.1415926;

				sum += angle;
				angle *=180.0/3.1415926;
				azPW.setData(k, azPW.getObsSV(), angle);
			}
			azPW.setData(i, azPW.getOrgT(), sum/vcPW.getFlag(i));
		}
		return azPW;
	}
	public void average(VCPair vcPW) {
		double [] sumLoc = new double[]{0, 0, 0, 0};
		for(int i=0; i<vcPW.getNEvents(); i++) {
			sumLoc[0] += vcPW.getEE(i);
			sumLoc[1] += vcPW.getEN(i);
			sumLoc[2] += vcPW.getED(i);
		}
		double meanE = sumLoc[0]/vcPW.getNEvents();
		double meanN = sumLoc[1]/vcPW.getNEvents();
		double meanD = sumLoc[2]/vcPW.getNEvents();
		for(int i=0; i<vcPW.getNEvents(); i++) {
			vcPW.setData(i, vcPW.getEE(), meanE);
			vcPW.setData(i, vcPW.getEN(), meanN);
			vcPW.setData(i, vcPW.getED(), meanD);
		}
	}
	public double [][] locationRange(VCPair vcPW) {
		double minX = ArrayMath.min(vcPW.getEE());
		double maxX = ArrayMath.max(vcPW.getEE());
		double dx = 0.5*(maxX-minX);
		double minY = ArrayMath.min(vcPW.getEN());
		double maxY = ArrayMath.max(vcPW.getEN());
		double dy = 0.5*(maxY-minY);
		double minZ = ArrayMath.min(vcPW.getED());
		double maxZ = ArrayMath.max(vcPW.getED());
		double dz = 2.0*(maxZ-minZ);
		double [] minLoc = new double[]{minX-dx, minY-dy, minZ-dz, 0};
		double [] maxLoc = new double[]{maxX+dx, maxY+dy, maxZ+dz, 0};
		//double [] minLoc = new double[]{0.9*ArrayMath.min(vcPW.getEE()), 0.9*ArrayMath.min(vcPW.getEN()), 0.9*ArrayMath.min(vcPW.getED()), 0};
		//double [] maxLoc = new double[]{1.1*ArrayMath.max(vcPW.getEE()), 1.1*ArrayMath.max(vcPW.getEN()), 1.1*ArrayMath.max(vcPW.getED()), 0};
		double [] d = new double[]{Math.sqrt(minLoc[0]*minLoc[0]+minLoc[1]*minLoc[1]),
				Math.sqrt(minLoc[0]*minLoc[0]+maxLoc[1]*maxLoc[1]),
				Math.sqrt(maxLoc[0]*maxLoc[0]+minLoc[1]*minLoc[1]),
				Math.sqrt(maxLoc[0]*maxLoc[0]+maxLoc[1]*maxLoc[1])};
		double [] minLoc1 = new double[]{ArrayMath.min(d), minLoc[1], 0, 0};
		double [] maxLoc1 = new double[]{ArrayMath.max(d), maxLoc[1], 0, 0};

		return new double[][]{minLoc, maxLoc, minLoc1, maxLoc1};

	}
	
//	private RayTracerFlat3D genRayTracerFlat3D(RayTracerFlat3D rt, int ttFileType, String pckFileName, 
//			int nBoundaries, String rayCode, String psCode) {
//		if(pckFileName==null||pckFileName.isEmpty()) return null;
//		VCPair vcPW 		= null;
//		if(ttFileType==0||ttFileType==1) {
//			SeisPTSrcMech seisptSrcMech = new SeisPTSrcMech((ttFileType==0), pckFileName, null);
//			if(seisptSrcMech==null) return null;
//			//vcPW = seisptSrcMech.toVCPair(0);
//		} else {
//			vcPW = new VCPair(1, pckFileName);
//		}
//		
//		String line = rayCode;
//		if(line.contains(".")) { 
//			JOptionPane.showMessageDialog((Component)_frame, "\nRay code must be integers!", "Error", 
//					JOptionPane.ERROR_MESSAGE); 
//			return null;
//		}
//		
//		StringTokenizer st = new StringTokenizer(line, " ,\t");
//		
//		int [] rayCodes = null;
//		if(st.countTokens()>0) {
//			rayCodes = new int [st.countTokens()];
//			int iMax = -1;
//			int iMin = 10000;
//			for(int i=0; i<rayCodes.length; i++) {
//				rayCodes[i] = Integer.parseInt(st.nextToken().trim());
//				iMax = iMax>rayCodes[i] ? iMax:rayCodes[i];
//				iMin = iMin<rayCodes[i] ? iMin:rayCodes[i];
//			}
//			if(iMin<0) return null;
//			if(iMax>nBoundaries-1) return null;
//		}
//		
//		line = psCode;
//		if(line.contains(".")) { 
//			JOptionPane.showMessageDialog((Component)_frame, "\nWave code must be integers!", "Error", 
//					JOptionPane.ERROR_MESSAGE); 
//			return null;	
//		}
//		
//		st = new StringTokenizer(line, " ,\t");
//		
//		int [] psCodes = null;
//		if(st.countTokens()>1) {
//			psCodes = new int [st.countTokens()];
//			for(int i=0; i<psCodes.length; i++) {
//				psCodes[i] = Integer.parseInt(st.nextToken().trim());
//				if(psCodes[i]==1||psCodes[i]==2||psCodes[i]==3) {
//				} else return null;
//			}
//			if(psCodes.length<=rayCodes.length) { 
//				JOptionPane.showMessageDialog((Component)_frame, 
//						"\nWave code should have one more element than ray code!", "Error", JOptionPane.ERROR_MESSAGE); 
//				return null;	
//			}
//		}
//		
//		RayTracerFlat3D rayTracer = new RayTracerFlat3D(null, rt.getFlatLayer1D(), vcPW, 1, rt.getIVTI(), 
//				rt.getIApproximation(), rayCodes, psCodes, 3, rt.getTolerance(), 0, 0);
//		rayTracer.start();
//		
//		return rayTracer;
//
//	}
	public void startCal(int iMethod, int iRun, RayTracerFlat3D rt) {
		refreshAction();
		_startTime = System.currentTimeMillis(); 
		getInput();

		FlatLayer1D layer1D = rt.getFlatLayer1D().copy();
		VCPair vcPW = rt.getVCPair().copy();
		//System.out.println("vcPW="+vcPW.toString());
		//System.out.println(layer1D.toString());
		
		RayTracerFlat3D rayTracer = new RayTracerFlat3D(null, layer1D, vcPW, 1, rt.getIVTI(), 
				rt.getIApproximation(), rt.getReflectorIndex(), rt.getPsIndex(), rt.getIWave(), rt.getTolerance(), 0, 0);
		rayTracer.start();
		
		SaPlot saPlot = new SaPlot(this, false, _iType, "Velocity", layer1D, vcPW, _saLength);
		saPlot.showDialog();
		saPlot.update(layer1D, vcPW, -1, null, null);

		if(_cwd==null) {
			_cwd = _frame.genSaCwd();
			_frame.setSaCwd(_cwd);
		}
		//System.out.println("cwd_"+iType+"="+_cwd);
		
		if(_enableRmsMovie) {
			String movieDir = _cwd+File.separator+"movie";

			File file = new File(movieDir);
			if (file.exists()) {
				try {
					FileUtils.cleanDirectory(file);
				} catch (IOException e) {
					e.printStackTrace();
				}
			} else {
				if (file.mkdir()) {
					System.out.println("Directory is created! "+movieDir); 
				}
			}

			int k = 1+_saLength/100;
			if(k>1) {
				_rmsImageFileNames = new String[k];
				_rmsImageFileIndex = 0;
				String name = null;
				for(int i=0; i<_rmsImageFileNames.length; i++) {
					name = String.format("rms%04d.jpg" , i);
					_rmsImageFileNames[i] = movieDir+File.separator+name;
				}
				name = "movieRms.avi";
				_rmsMovieFileName = movieDir+File.separator+name;
			} else {
				_rmsImageFileNames = null;
				_rmsImageFileIndex = 0;
				_rmsMovieFileName = null;
			}
		} else {
			_rmsImageFileNames = null;
			_rmsImageFileIndex = 0;
			_rmsMovieFileName = null;
		}

		_init = rayTracer.getFlatLayer1D().copy();
		
		_calcTask = new CalculatorTask(iMethod, iRun, rayTracer, 1, _min, _max, _linkTo,
				_saLength, _eqLength, _T0, _tol, _noise, _tuneParameter, _coolingSchedule, 
				_iType, _cwd, saPlot);

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
		//int iMethod = 1;
		public CalculatorTask(int iMethod, int iRun, RayTracerFlat3D rayTracer, 
				int iRMS, FlatLayer1D minLayer, FlatLayer1D maxLayer, FlatLayer1D linkToLayer, 
				int saLength, int eqLength, double T0, double tol, double noise, double c, 	int coolingSchedule, 
				int iType, String cwd, SaPlot saPlot) {
			_saPlot = saPlot;

			//this.iMethod = iMethod;
			//this.iType = iType;
			//this.saLocating = saLocating;

			_saAlone = new SaAlone(iMethod, iRun, rayTracer, 1, minLayer, maxLayer, linkToLayer, 
					saLength, eqLength, T0, tol, noise, c, coolingSchedule, 
					iType, cwd, CalculatorTask.this);

			//_run.setEnabled(false);
			setCursor(new Cursor(Cursor.WAIT_CURSOR));
		}

		public void update(SaToken saToken) { publish(saToken); }

		@Override
		protected void process(List<SaToken>  saTokens) {
			for(SaToken saToken : saTokens){
				RayTracerFlat3D rayTracer = saToken.getRayTracerFlat3D();
				double [][] rmsArray = saToken.getRmsArray();
				if(_saPlot!=null) {
					if(_rmsImageFileNames==null) {
						_saPlot.update(rayTracer.getFlatLayer1D().copy(), rayTracer.getVCPair(), -1,
								new double [][] {rmsArray[1]}, null);
					} else {
						_saPlot.update(rayTracer.getFlatLayer1D().copy(), rayTracer.getVCPair(), _rmsImageFileIndex, 
								new double [][] {rmsArray[1]}, _rmsImageFileNames);
						_rmsImageFileIndex++;
					}
				}
			}
		}

		@Override
		protected SaToken doInBackground() throws InterruptedException {
			while (isCancelled()) return null;
			_saAlone.start();
			return _saAlone.getToken();
		}

		@Override
		protected void done() {
			_progBar.setValue( 100 );
			try {
				SaToken saToken = get();
				if(saToken!=null) {
					RayTracerFlat3D rayTracer = saToken.getRayTracerFlat3D();
					
					double [][] rmsArray = saToken.getRmsArray();
					if(_saPlot!=null) {
						if(_rmsImageFileNames==null) {
							_saPlot.update(rayTracer.getFlatLayer1D().copy(), rayTracer.getVCPair(), -1, 
									new double [][] {rmsArray[1]}, null);
						} else {
							_saPlot.update(rayTracer.getFlatLayer1D().copy(), rayTracer.getVCPair(), _rmsImageFileIndex, 
									new double [][] {rmsArray[1]}, _rmsImageFileNames);
							_rmsImageFileIndex++;
						}
						
						if(_rmsMovieFileName!= null) {
							makeMovie(2, 1.0f);
//							try {
//								writeMovie(new File(_rmsMovieFileName), _rmsImageFileNames);
//							} catch (IOException e) {
//								e.printStackTrace();
//							}
						}
					}
					
					_final = rayTracer.getFlatLayer1D().copy();
					System.out.println(_final.toString());
					DipLayer1D dipLayer1D = _frame.getRayTracer().getDipLayer1D();
					String localName = null;
					if(_iType==0||_iType==10) localName = "sa1Aniso";
					else if(_iType==1||_iType==11) localName = "sa0Vel";
					else if(_iType==2||_iType==12) localName = "sa2Full";
					else localName = "sa0VelLoc";
					String fileName = _cwd+File.separator+localName;
					//System.out.println(fileName);
					dipLayer1D.update(_final);
					dipLayer1D.writeLayer(fileName+"_resultModel.csv");

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

					String updateString = "\n\nThe inverted model could replace " +
							"the current one in the main program.\nDo you want to update? ";

					int answer = JOptionPane.showConfirmDialog(null, aTime+updateString);
					if (answer == JOptionPane.YES_OPTION) {
						updateAndRayTracing(_final);
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
				if(_saAlone!=null) _saAlone.setCancel(true);
				if(_saPlot!=null) _saPlot.setVisible(false);
				System.out.println("User cancelled.");
			}	

			//_run.setEnabled(true);	
			_run.setText(START);
			setCursor( null );
		}
		
		private void writeMovie1(File file, String[] imageFileNames) throws IOException {
			BufferedImage frame = ImageIO.read(new File(imageFileNames[0]));
			MovieWriter out = Registry.getInstance().getWriter(file);
			Format format = new Format(FormatKeys.MediaTypeKey, MediaType.VIDEO, //
					VideoFormatKeys.EncodingKey, VideoFormatKeys.ENCODING_QUICKTIME_JPEG,
					FormatKeys.FrameRateKey, new Rational(5, 1),// bigger the first int, faster
					VideoFormatKeys.WidthKey, frame.getWidth(), //
					VideoFormatKeys.HeightKey, frame.getHeight(),//
					VideoFormatKeys.DepthKey, 24
					);
			
		
			int track = out.addTrack(format);
			try {
				Buffer buf = new Buffer();
				buf.format = new Format(VideoFormatKeys.DataClassKey, BufferedImage.class);
				buf.sampleDuration = format.get(FormatKeys.FrameRateKey).inverse();
				for (int i = 0; i < imageFileNames.length; i++) {
					buf.data = ImageIO.read(new File(imageFileNames[i]));;
					out.write(track, buf);
				}
			} finally {
				out.close();
			}
		}
		
		protected void makeMovie(int fps, float movieQuality) {
			try {
				int k = 0;
				for(int i=0; i<_rmsImageFileNames.length; i++) {
					if(_rmsImageFileNames[i]!=null) {
						k++;
					}
				}
				String [] selectedFileName = new String[k];

				k = 0;
				for(int i=0; i<_rmsImageFileNames.length; i++) {
					if(_rmsImageFileNames[i]!=null) {
						selectedFileName[k] = _rmsImageFileNames[i];
						k++;
					}
				}
				writeMovieAVI(new File(_rmsMovieFileName), selectedFileName, fps, movieQuality);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		private void writeMovieAVI(File file, String[] imageFileNames, int fps, float movieQuality) throws IOException {
			BufferedImage frame = ImageIO.read(new File(imageFileNames[0]));
			Format format = new Format(FormatKeys.MediaTypeKey, MediaType.VIDEO, //
					VideoFormatKeys.EncodingKey, VideoFormatKeys.ENCODING_AVI_MJPG,
					FormatKeys.FrameRateKey, new Rational(fps, 1),// 
					VideoFormatKeys.WidthKey, frame.getWidth(), //
					VideoFormatKeys.HeightKey, frame.getHeight(),//
					VideoFormatKeys.DepthKey, 24,
					QualityKey, movieQuality
					);
			AVIWriter out = null;
	        try {
	            out = new AVIWriter(file);

	            int track = out.addTrack(format);
				Buffer buf = new Buffer();
				buf.format = new Format(VideoFormatKeys.DataClassKey, BufferedImage.class);
				buf.sampleDuration = format.get(FormatKeys.FrameRateKey).inverse();
				for (int i = 0; i < imageFileNames.length; i++) {
					buf.data = ImageIO.read(new File(imageFileNames[i]));;
					out.write(track, buf);
				}
	        } finally {
	            if (out != null) { out.close(); }
	        }
		}
		
		private void writeMovieMOV(File file, String[] imageFileNames, int fps) throws IOException {
			BufferedImage frame = ImageIO.read(new File(imageFileNames[0]));
			MovieWriter out = Registry.getInstance().getWriter(file);
			Format format = new Format(FormatKeys.MediaTypeKey, MediaType.VIDEO, //
					VideoFormatKeys.EncodingKey, VideoFormatKeys.ENCODING_QUICKTIME_JPEG,
					FormatKeys.FrameRateKey, new Rational(fps, 1),// 
					VideoFormatKeys.WidthKey, frame.getWidth(), //
					VideoFormatKeys.HeightKey, frame.getHeight(),//
					VideoFormatKeys.DepthKey, 24
					);
			
		
			int track = out.addTrack(format);
			try {
				Buffer buf = new Buffer();
				buf.format = new Format(VideoFormatKeys.DataClassKey, BufferedImage.class);
				buf.sampleDuration = format.get(FormatKeys.FrameRateKey).inverse();
				for (int i = 0; i < imageFileNames.length; i++) {
					buf.data = ImageIO.read(new File(imageFileNames[i]));;
					out.write(track, buf);
				}
			} catch(javax.imageio.IIOException e) {
				
			} finally {
				out.close();
			}
		}	
	}
	private class ReflectionPanel extends JPanel {
		private JButton 	_pckBrowserButton1;
		private JButton 	_pckBrowserButton2;

		public ReflectionPanel(String pckCWD) {
			setLayout(new GridBagLayout());
			Insets insets = new Insets(5, 5, 5, 5);
			Insets tallInsets = new Insets(5, 5, 30, 5);
			GridBagConstraints gbc;

			JCheckBox jCheckBox = new JCheckBox("Enabled Reflection Input", _reflectionEnabled);
			jCheckBox.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent event) {
					_reflectionEnabled = ((JCheckBox)event.getSource()).isSelected();
					setEnabled(_reflectionEnabled);
				}
			});

			int iRow = 0;
			gbc= new GridBagConstraints(0, iRow, 1, 1, 1.0, 0.0, GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH, insets, 0, 0);
			add(jCheckBox, gbc);

			JPanel modulePanel = new JPanel( new GridLayout(2, 2, 5, 5));
			Border loweredetched = BorderFactory.createEtchedBorder(EtchedBorder.LOWERED);
			_rayCodeTF1 = new JTextField ("1");
			_psCodeTF1 = new JTextField ("1, 1");
			modulePanel.add(_rayCodeTF1);
			modulePanel.add(new JLabel("Reflector Index (increasing downwardly from 0)"));
			modulePanel.add(_psCodeTF1);
			modulePanel.add(new JLabel("Wave Mode (=1 P, =2 fast S (SH), =3 slow S (SV)"));

			modulePanel.setBorder(BorderFactory.createTitledBorder(loweredetched, "1st Reflector and Type"));

			iRow++;
			gbc= new GridBagConstraints(0, iRow, 5, 1, 1.0, 0.0, GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH, insets, 0, 0);
			add(modulePanel, gbc);


			_pckTF1 = new JTextField (pckCWD, 5);

			_pckBrowserButton1 = new JButton("Browse"); 
			_pckBrowserButton1.setToolTipText("Browse shot-receiver geometry file");
			_pckBrowserButton1.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					readTtFile(1);
				}
			});

			iRow++;
			gbc= new GridBagConstraints(0, iRow, 1, 1, 1.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, insets, 0, 0);
			add(new JLabel("1st Reflected Travel Time (SrcMech) File:"), gbc);

			int n = 3;
			ButtonGroup moduleRadioGroup = new ButtonGroup();
			JRadioButton [] moduleRadioButton = new JRadioButton[n];

			int k = 0;
			moduleRadioButton[k] = new JRadioButton("SrcMech Perf", _ttFileType1==0);
			moduleRadioButton[k].setToolTipText("SeisPT exported *.txt file. Perf event only. May or may not have perf timing");
			moduleRadioButton[k].addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent event) {
					if(((JRadioButton)event.getSource()).isSelected()) _ttFileType1 = 0; 
				}
			});
			moduleRadioGroup.add(moduleRadioButton[k]);
			gbc= new GridBagConstraints(1, iRow, 1, 1, 1.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, insets, 0, 0);
			add(moduleRadioButton[k], gbc);

			k++;
			moduleRadioButton[k] = new JRadioButton("SrcMech Event", _ttFileType1==1);
			moduleRadioButton[k].setToolTipText("SeisPT exported *.txt file. MS event only");
			moduleRadioButton[k].addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent event) {
					if(((JRadioButton)event.getSource()).isSelected()) _ttFileType1 = 1;  
				}
			});
			moduleRadioGroup.add(moduleRadioButton[k]);
			gbc= new GridBagConstraints(2, iRow, 1, 1, 1.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, insets, 0, 0);
			add(moduleRadioButton[k], gbc);

			k++;
			moduleRadioButton[k] = new JRadioButton("Plain CSV", _ttFileType1==2);
			moduleRadioButton[k].setToolTipText("Plain CSV format file");
			moduleRadioButton[k].addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent event) {
					if(((JRadioButton)event.getSource()).isSelected()) _ttFileType1 = 2;  
				}
			});
			moduleRadioGroup.add(moduleRadioButton[k]);
			gbc= new GridBagConstraints(3, iRow, 1, 1, 1.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, insets, 0, 0);
			add(moduleRadioButton[k], gbc);

			iRow++;
			gbc= new GridBagConstraints(0, iRow, 4, 1, 1.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, tallInsets, 0, 0);
			add(_pckTF1, gbc);
			gbc= new GridBagConstraints(4, iRow, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, tallInsets, 0, 0);
			add(_pckBrowserButton1, gbc);

			modulePanel = new JPanel( new GridLayout(2, 2, 5, 5));
			_rayCodeTF2 = new JTextField ("1");
			_psCodeTF2 = new JTextField ("1, 1");
			modulePanel.add(_rayCodeTF2);
			modulePanel.add(new JLabel("Reflector Index (increasing downwardly from 0)"));
			modulePanel.add(_psCodeTF2);
			modulePanel.add(new JLabel("Wave Mode (=1 P, =2 fast S (SH), =3 slow S (SV)"));

			modulePanel.setBorder(BorderFactory.createTitledBorder(loweredetched, "2nd Reflector and Type"));

			iRow++;
			gbc= new GridBagConstraints(0, iRow, 5, 1, 1.0, 0.0, GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH, insets, 0, 0);
			add(modulePanel, gbc);


			_pckTF2 = new JTextField (pckCWD, 5);

			_pckBrowserButton2 = new JButton("Browse"); 
			_pckBrowserButton2.setToolTipText("Browse shot-receiver geometry file");
			_pckBrowserButton2.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					readTtFile(2);
				}
			});
			iRow++;
			gbc= new GridBagConstraints(0, iRow, 1, 1, 1.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, insets, 0, 0);
			add(new JLabel("2nd Reflected Travel Time (SrcMech) File:"), gbc);

			n = 3;
			moduleRadioGroup = new ButtonGroup();
			moduleRadioButton = new JRadioButton[n];

			k = 0;
			moduleRadioButton[k] = new JRadioButton("SrcMech Perf", _ttFileType2==0);
			moduleRadioButton[k].setToolTipText("SeisPT exported *.txt file. Perf event only. May or may not have perf timing");
			moduleRadioButton[k].addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent event) {
					if(((JRadioButton)event.getSource()).isSelected()) _ttFileType2 = 0; 
				}
			});
			moduleRadioGroup.add(moduleRadioButton[k]);
			gbc= new GridBagConstraints(1, iRow, 1, 1, 1.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, insets, 0, 0);
			add(moduleRadioButton[k], gbc);

			k++;
			moduleRadioButton[k] = new JRadioButton("SrcMech Event", _ttFileType2==1);
			moduleRadioButton[k].setToolTipText("SeisPT exported *.txt file. MS event only");
			moduleRadioButton[k].addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent event) {
					if(((JRadioButton)event.getSource()).isSelected()) _ttFileType2 = 1;  
				}
			});
			moduleRadioGroup.add(moduleRadioButton[k]);
			gbc= new GridBagConstraints(2, iRow, 1, 1, 1.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, insets, 0, 0);
			add(moduleRadioButton[k], gbc);

			k++;
			moduleRadioButton[k] = new JRadioButton("Plain CSV", _ttFileType2==2);
			moduleRadioButton[k].setToolTipText("Plain CSV format file");
			moduleRadioButton[k].addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent event) {
					if(((JRadioButton)event.getSource()).isSelected()) _ttFileType2 = 2;  
				}
			});
			moduleRadioGroup.add(moduleRadioButton[k]);
			gbc= new GridBagConstraints(3, iRow, 1, 1, 1.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, insets, 0, 0);
			add(moduleRadioButton[k], gbc);

			iRow++;
			gbc= new GridBagConstraints(0, iRow, 4, 1, 1.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, tallInsets, 0, 0);
			add(_pckTF2, gbc);
			gbc= new GridBagConstraints(4, iRow, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, tallInsets, 0, 0);
			add(_pckBrowserButton2, gbc);
			
			iRow++;
			gbc= new GridBagConstraints(0, iRow, 5, 1, 1.0, 1.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, insets, 0, 0);
			add(new JLabel(" "), gbc);
			
			setEnabled(_reflectionEnabled);
		}
		public void setEnabled(boolean enable) {
			_rayCodeTF1.setEnabled(enable);
			_psCodeTF1.setEnabled(enable);
			_pckTF1.setEnabled(enable);
			_rayCodeTF2.setEnabled(enable);
			_psCodeTF2.setEnabled(enable);
			_pckTF2.setEnabled(enable);
			
			_pckBrowserButton1.setEnabled(enable);
			_pckBrowserButton2.setEnabled(enable);			
		}
		
		public void readTtFile(int id) {
			JTextField 	pckTF = null;
			if(id==1) 	pckTF = _pckTF1;
			else 		pckTF = _pckTF2;
			
			JFrame jframe = new JFrame();
			JFileChooser chooser = new JFileChooser(pckTF.getText().trim() );
			chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			chooser.setDialogTitle("Select Directory For Output");

			int returnVal = chooser.showOpenDialog( jframe );
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				File file = chooser.getSelectedFile();
				String loc = file.getAbsolutePath();
				if(_ttFileType1==0||_ttFileType1==1) {
					if(FilenameUtils.isExtension(loc, "txt")) pckTF.setText( loc );
				} else {
					if(FilenameUtils.isExtension(loc, "csv")) pckTF.setText( loc );
				}
			}
		}
	}
	private class GlobelPanel extends JPanel{

		private int		_fieldWidth		= 10;
		public GlobelPanel(double [] min, double [] max) {
			_minTF = new JTextField[_tabNames.length];
			_maxTF = new JTextField[_tabNames.length];
			setLayout(new GridBagLayout());
			Insets insets = new Insets(15, 5, 0, 5);
			GridBagConstraints gbc;
			double vertical = 0.0;
			for(int i=0, k=0; i<_tabNames.length; i++, k++) {
				_minTF[i] = new JTextField (min[i]+"", _fieldWidth);
				_maxTF[i] = new JTextField (max[i]+"", _fieldWidth);

				if(i==_tabNames.length-1) { vertical = 1.0; } 
				else  { vertical = 0.0; } 
				gbc= new GridBagConstraints(0, k, 1, 1, 1.0, vertical,
						GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, insets, 0, 0);
				add(new JLabel("Minimum "+_tabNames[i]+":",Label.LEFT), gbc);
				gbc= new GridBagConstraints(1, k, 1, 1, 1.0, vertical,
						GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, insets, 0, 0);
				add(_minTF[i], gbc);
				gbc= new GridBagConstraints(2, k, 1, 1, 1.0, vertical,
						GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, insets, 0, 0);
				add(new JLabel("Maximum "+_tabNames[i]+":",Label.LEFT), gbc);
				gbc= new GridBagConstraints(3, k, 1, 1, 1.0, vertical,
						GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, insets, 0, 0);
				add(_maxTF[i], gbc);
			}
		}
	}

	private class SaInputPanel extends JPanel{

		public SaInputPanel(int saLength, int eqLength, double T0, double tol, double noise, double tuneParameter) {
			_saLengthTF 		= new JTextField(saLength+"");
			_eqLengthTF 		= new JTextField(eqLength+"");
			_T0TF 				= new JTextField(T0+"");
			_tolTF 				= new JTextField(tol+"");
			_noiseTF 			= new JTextField(noise+"");
			_tuneParameterTF 	= new JTextField(tuneParameter+"");

			//			String [] lable = new String[] {
			//					"Number of Iteration Of SA:",
			//					"Number of Iteration Of Each Equilibrium Stage:",
			//					"Initial Temperature:",
			//					"The Minimum RMS (Root Mean Square) in second):",
			//					"Noise Level (currently un-used):",
			//					"Tune Parameter:",
			//			};
			//			JTextField [] tf = new JTextField[] {_saLengthTF, _eqLengthTF, _T0TF, _tolTF, _noiseTF, _tuneParameterTF };

			String [] lable = new String[] {
					"Number of Iteration Of SA:",
					"Initial Temperature:",
					"The Minimum RMS (Root Mean Square) in second:",
					"Tune Parameter:",
			};
			JTextField [] tf = new JTextField[] {_saLengthTF, _T0TF, _tolTF, _tuneParameterTF };

			setLayout(new GridBagLayout());
			Insets insets = new Insets(15, 5, 0, 5);
			GridBagConstraints gbc;
			double vertical = 0.0;
			int k = 0;

			JCheckBox jCheckBox = null;

			jCheckBox = new JCheckBox( "Record RMS curve into a movie",  _enableRmsMovie);
			jCheckBox.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent event) {
					_enableRmsMovie = ((JCheckBox)event.getSource()).isSelected();
				}
			});

			gbc= new GridBagConstraints(0, k++, 2, 1, 1.0, vertical,
					GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, insets, 0, 0);
			add(jCheckBox, gbc);
			
			String label = "Internal check on the inversion eligibility of each layer";
			jCheckBox = new JCheckBox(label,  _enableInternalCheck);
			jCheckBox.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent event) {
					_enableInternalCheck = ((JCheckBox)event.getSource()).isSelected();
				}
			});

			gbc= new GridBagConstraints(0, k++, 2, 1, 1.0, vertical,
					GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, insets, 0, 0);
			add(jCheckBox, gbc);
			
			gbc= new GridBagConstraints(0, k++, 2, 1, 1.0, vertical,
					GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH, insets, 0, 0);
			add(new JSeparator(JSeparator.HORIZONTAL), gbc);
			
			vertical = 0.0;
			for(int i=0; i<lable.length; i++, k++) {
				//if(i==lable.length-1) { vertical = 1.0; } 
				//else  { vertical = 0.0; } 
				gbc= new GridBagConstraints(0, k, 1, 1, 1.0, vertical,
						GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, insets, 0, 0);
				add(new JLabel(lable[i],Label.LEFT), gbc);
				gbc= new GridBagConstraints(1, k, 1, 1, 1.0, 1.0,
						GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH, insets, 0, 0);
				add(tf[i], gbc);
			}
			
//			String[] minComboTypes = {"0", "1%", "2%", "3%", "4%", "5%", "6%", "8%", "10%", 
//					"12%", "14%", "16%", "18%", "20%", "22%", "25%", "30%", "35%", "40%", 
//					"50%", "75%", "100%", "125%", "150%", "175%", "200%"};
			JComboBox minCombo= new JComboBox(_percentageString);
			minCombo.setToolTipText("min limit = (1-min%)*current_value");
			minCombo.setSelectedIndex(0);
			minCombo.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					JComboBox jcmbType = (JComboBox) e.getSource();
					String percentage = (String) jcmbType.getSelectedItem();
					percentage = percentage.replaceAll("%", ""); // Check for the '%' symbol and delete it.
					double scalor = Double.parseDouble(percentage.trim());
					scalor = -0.01*scalor;
					for(int i=0; i<_tabNames.length; i++) {
						String tabName = _tabNames[i];
						updateMinMax(tabName, _iType, 3, scalor);
					}
					
					//if(!_minCombo.isEnabled()) _minCombo.setEnabled(true);
					int k = jcmbType.getSelectedIndex();
					_userComboEvent = false;
					_minCombo.setSelectedIndex(k);
					for(int i=0; i<_minIndex.length; i++) _minIndex[i] = k;
				}
			});
			
			gbc= new GridBagConstraints(0, k, 1, 1, 1.0, 0.0,
					GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, insets, 0, 0);
			add(new JLabel("Global min% (apply to all layer material properties): ",Label.LEFT), gbc);
			gbc= new GridBagConstraints(1, k++, 1, 1, 1.0, 1.0,
					GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH, insets, 0, 0);
			add(minCombo, gbc);
			

//			String[] maxComboTypes = {"0", "1%", "2%", "3%", "4%", "5%", "6%", "8%", "10%", 
//					"12%", "14%", "16%", "18%", "20%", "22%", "25%", "30%", "35%", "40%", 
//					"50%", "75%", "100%", "125%", "150%", "175%", "200%"};
			JComboBox maxCombo = new JComboBox(_percentageString);
			maxCombo.setToolTipText("max limit would be (1+max%)*current_value");
			maxCombo.setSelectedIndex(0);
			maxCombo.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					JComboBox jcmbType = (JComboBox) e.getSource();
					String percentage = (String) jcmbType.getSelectedItem();
					percentage = percentage.replaceAll("%", ""); 
					double scalor = Double.parseDouble(percentage.trim());
					scalor = 0.01*scalor;
					for(int i=0; i<_tabNames.length; i++) {
						String tabName = _tabNames[i];
						updateMinMax(tabName, _iType, 4, scalor);
					}

					//if(!_maxCombo.isEnabled()) _maxCombo.setEnabled(true);
					int k = jcmbType.getSelectedIndex();
					_userComboEvent = false;
					_maxCombo.setSelectedIndex(k);
					for(int i=0; i<_maxIndex.length; i++) _maxIndex[i] = k;
				}
			});

			gbc= new GridBagConstraints(0, k, 1, 1, 1.0, 0.0,
					GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, insets, 0, 0);
			add(new JLabel("Global max% (apply to all layer material properties): ",Label.LEFT), gbc);
			gbc= new GridBagConstraints(1, k++, 1, 1, 1.0, 1.0,
					GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH, insets, 0, 0);
			add(maxCombo, gbc);
			

			//final String[] saCooling = { "Very Fast Simulated Annealing        ", "Boltzmann Simulated Annealing      "};
			final String[] saCooling = { "Very Fast Simulated Annealing        ", "Very Fast Simulated Annealing      "};
			JComboBox saCoolingJB = new JComboBox(saCooling);
			saCoolingJB.setSelectedIndex(0);
			saCoolingJB.addActionListener( new ActionListener() {
				public void actionPerformed( ActionEvent e ) {
					JComboBox cb = (JComboBox)e.getSource();
					String saName = ((String)cb.getSelectedItem()).trim();
					if(saName.equalsIgnoreCase(saCooling[0].trim())) { _coolingSchedule = 5; }
					else { _coolingSchedule = 5; }
				}
			});
			vertical = 1.0;
			gbc= new GridBagConstraints(0, k, 1, 1, 1.0, vertical,
					GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, insets, 0, 0);
			//add(new JLabel("Cooling Schedule:",Label.LEFT), gbc);
			gbc= new GridBagConstraints(1, k, 1, 1, 1.0, vertical,
					GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, insets, 0, 0);
			//add(saCoolingJB, gbc);
			
			
			gbc= new GridBagConstraints(0, k, 2, 1, 1.0, 1.0,
					GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH, insets, 0, 0);
			add(new JLabel(" "), gbc);
			
			k++;
			gbc= new GridBagConstraints(0, k++, 2, 1, 1.0, 1.0,
					GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH, insets, 0, 0);
			add(new JLabel(" "), gbc);
			
			gbc= new GridBagConstraints(0, k++, 2, 1, 1.0, vertical,
					GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, insets, 0, 0);
			add(new JSeparator(JSeparator.HORIZONTAL), gbc);
			
			gbc= new GridBagConstraints(0, k, 2, 1, 1.0, 0.0,
					GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, insets, 0, 0);
			//add(new VelPanel(), gbc);
		}
	}
}

