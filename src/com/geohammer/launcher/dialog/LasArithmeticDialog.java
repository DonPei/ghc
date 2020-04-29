package org.ucdm.launcher.dialog;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListModel;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.apache.commons.io.FilenameUtils;
import org.ucdm.common.CommonFrame;
import org.ucdm.common.CommonProject;
import org.ucdm.launcher.LauncherFrame;
import org.ucdm.soniclog.LasTree;
import org.ucdm.soniclog.las.LAS;
import org.ucdm.soniclog.las.LasCurve;
import org.ucdm.soniclog.las.LasLine;
import org.ucdm.common.util.UiUtil;

import edu.mines.jtk.mosaic.IPanel;
import edu.mines.jtk.mosaic.Mosaic;

public class LasArithmeticDialog   extends JDialog{
	LauncherFrame			_frame 					= null;
	private LAS 			_las 					= null;
	private int [] 			_selectedCurveIndex 	= null;
	private JList 			_curveNameList			= null;
	private JButton [] 		_jButtons 				= null;
	private String [] 		_toolTips 				= null;
	private int  			_iMethod 				= 0;
	private int  			_currSelectedCurveIndex = 0;
	private String  		_currSelectedCurveName 	= "None";
	
	private String [] 		_jButtonString 	= new String[] {"A: ", "B: ", "C: "};
	private JTextField [] 	_jButtonTF 		= null;
	private JTextField 		_outputTF 		= null;
	
	
	public LasArithmeticDialog(JFrame aParent, String aTitle, String lasFileName, boolean modal) {
		super(aParent, aTitle, modal);
		_frame 	= (LauncherFrame)aParent;
		_las 	= new LAS(lasFileName);	        
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

		panel.add(jc, BorderLayout.CENTER);
		panel.add( getCommandRow(), BorderLayout.SOUTH );

		getContentPane().add( panel );

		UiUtil.centerOnParentAndShow( this );
		setSize(800, 700);
		setVisible(true);
	}

	private JPanel createContents() {
		JPanel innerPanel = new JPanel();

		innerPanel.setLayout(new GridBagLayout());
		Insets insets1 = new Insets(1, 1, 1, 1);
		Insets insets2 = new Insets(1, 1, 5, 5);
		Insets insets3 = new Insets(5, 5, 5, 5);
		GridBagConstraints gbc;

		Font myFont = new Font("SansSerif", Font.PLAIN, 12);
        Color myColor = Color.BLUE;
        
        int iRow = 0;
        int n = 0;
        String [] moduleString = null;
        String lasFileName = _las.getFileName();
        String output = FilenameUtils.getFullPath(lasFileName)+FilenameUtils.getBaseName(lasFileName)+"_arithmetic.las";
        LoadFilePanel loadFilePanel = new LoadFilePanel(output);
        loadFilePanel.setBorder(BorderFactory.createTitledBorder(null, "Output file: ", 
        		TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, myFont, myColor));
		gbc= new GridBagConstraints(0, iRow++, 4, 1, 1.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, insets2, 0, 0);
		innerPanel.add(loadFilePanel, gbc);
		
        n = _jButtonString.length;
		_jButtonTF = new JTextField [n]; 
		for(int i=0, k=0; i<n; i++) { _jButtonTF[i] = new JTextField("1.0"); }

		_jButtons = new JButton[n];
		_selectedCurveIndex = new int[n];
		for(int i=0, k=0; i<n; i++) {
			if(i==2) {
				_jButtonTF[i] = new JTextField("New_Curve");
				_jButtonTF[i].setToolTipText("input a curve name");
			} else {
				_jButtonTF[i] = new JTextField("1.0");
				_jButtonTF[i].setToolTipText("input a number");
			}
			final int j = i;
			_jButtons[i] = new JButton(_jButtonString[i]+" - None selected");
			_jButtons[i].setFocusable(false);
			_jButtons[i].addActionListener(new ActionListener() { 
				  public void actionPerformed(ActionEvent e) { 
					  JButton myButton = (JButton)(e.getSource());
					  String a = _jButtonString[j];
					  myButton.setText(a+" - "+_currSelectedCurveName+" selected");
					  //_jButtons[j].setText(label);
					  _selectedCurveIndex[j] = _currSelectedCurveIndex;
					  //System.out.println("j="+j+" index="+_selectedCurveIndex[j]);
					  if(_currSelectedCurveIndex==0)  	myButton.setBackground(null);
					  else  							myButton.setBackground(Color.GREEN); 
					  _jButtonTF[j].setEnabled(_currSelectedCurveIndex==0);
				} 
			} );
		}
		
		moduleString = new String[]{"A+B=C", "A-B=C", "A*B=C", "A/B=C"};
		n = moduleString.length;
		ButtonGroup moduleRadioGroup = new ButtonGroup();
		JRadioButton [] moduleRadioButton = new JRadioButton[n];
		JPanel modulePanel = new JPanel( new GridLayout(1, n, 1, 1));
		for(int i=0; i<n; i++) {
			final int j = i;
			moduleRadioButton[i] = new JRadioButton(moduleString[i], _iMethod==i);
			moduleRadioButton[i].addItemListener( new ItemListener() {
				public void itemStateChanged(ItemEvent itemEvent) {
					if (itemEvent.getStateChange() == ItemEvent.SELECTED) {
						_iMethod = j;
					} 
				}
			});
			moduleRadioGroup.add(moduleRadioButton[i]);
			modulePanel.add(moduleRadioButton[i]);
		}
		
        modulePanel.setBorder(BorderFactory.createTitledBorder(null, "Arithmetic Operation: ", 
        		TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, myFont, myColor));
		gbc= new GridBagConstraints(0, iRow++, 4, 1, 1.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, insets2, 0, 0);
		innerPanel.add(modulePanel, gbc);
		
		modulePanel = new JPanel( new GridLayout(1, 2, 5, 5));
		LasCurve [] curves = _las.getCurves();
		_toolTips = new String[curves.length];
		DefaultListModel<String> listModel = new DefaultListModel<>();
		listModel.addElement("None");
		_toolTips[0] = "None";
    	for(int i=1; i<curves.length; i++) {
    		listModel.addElement(curves[i].getLine().getMnem());
    		_toolTips[i] = curves[i].getLine().getFullName();
    	}
    	
    	_curveNameList = new JList(listModel) {
    		public String getToolTipText(MouseEvent evt) {
    	        int index = locationToIndex(evt.getPoint());
    	        //Object item = getModel().getElementAt(index);
    	        return _toolTips[index];
    	      }
    	};
    	_curveNameList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    	_curveNameList.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    final List<String> selectedValuesList = _curveNameList.getSelectedValuesList();
                    _currSelectedCurveName = selectedValuesList.toString().replaceAll("(^\\[|\\]$)", "");
                    _currSelectedCurveIndex = _curveNameList.getSelectedIndex();
                    //System.out.println(_currSelectedCurveName+" "+_currSelectedCurveIndex);
                }
            }
        });
    	modulePanel.add(new JScrollPane(_curveNameList));
    	
//    	_selectedCurveIndex 	=  new int[] {30, 2, 3, 4, 5, 
//    			0, 0, 0, 0, 
//    			0, 0, 1};
//    	
		JPanel jButtonPanel = new JPanel(new GridBagLayout());
		moduleString = new String[]{"A: select a curve or input a number", 
				"B: select a curve or input a number", "C: overwrite/select a curve  or add a new curve"};
		for(int i=0, k=0; i<_jButtonString.length; i++) {			
			gbc = new GridBagConstraints(0, k++, 1, 1, 1.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, insets3, 0, 0);
			jButtonPanel.add(new JLabel(" "), gbc);
			gbc = new GridBagConstraints(0, k++, 1, 1, 1.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, insets3, 0, 0);
			jButtonPanel.add(new JLabel(moduleString[i]), gbc);

			gbc = new GridBagConstraints(0, k++, 1, 1, 1.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, insets3, 0, 0);
			jButtonPanel.add(_jButtons[i], gbc);
			gbc = new GridBagConstraints(0, k++, 1, 1, 1.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, insets3, 0, 0);
			jButtonPanel.add(_jButtonTF[i], gbc);
		}
		modulePanel.add(jButtonPanel);
		
		modulePanel.setBorder(BorderFactory.createTitledBorder(null, "Log Curve Selection: ", 
        		TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, myFont, myColor));
		gbc= new GridBagConstraints(0, iRow++, 4, 1, 1.0, 1.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, insets2, 0, 0);
		innerPanel.add(modulePanel, gbc);
		
		return innerPanel;
	}	

	protected void okAction() {
		if(applyAction()) dispose();
	}

	protected boolean applyAction() {
		float invalidV = _las.getInvalidNum();
		LasCurve curveA = getCurve(0);
		LasCurve curveB = getCurve(1);
		LasCurve curveC = getCurve(2);
		float [] dataA = curveA.getData();
		float [] dataB = curveB.getData();
		float [] dataC = curveC.getData();
		for(int i=0; i<dataA.length; i++) {
			if(dataA[i]==invalidV || dataB[i]==invalidV ) dataC[i] = invalidV; 
			else {
				if(_iMethod==0) 		dataC[i] = dataA[i]+dataB[i]; 
				else if(_iMethod==1) 	dataC[i] = dataA[i]-dataB[i]; 
				else if(_iMethod==2) 	dataC[i] = dataA[i]*dataB[i]; 
				else if(_iMethod==3) 	{
					if(dataB[i]==0) dataC[i] = invalidV;
					else dataC[i] = dataA[i]/dataB[i]; 
				}
			}
		}
		
		int curveIndex = _selectedCurveIndex[2];
		if(curveIndex==0)  _las.appendCurve(curveC); 
		String selectedFileName = _outputTF.getText().trim();
		_las.write(selectedFileName);	
		
		//Create and set up the window.
		CommonFrameInstant frame = new CommonFrameInstant("LAS Log Viewer");
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

		//Create and set up the content pane.
		LasTree newContentPane = new LasTree(frame, selectedFileName, true);
		newContentPane.setOpaque(true); //content panes must be opaque
		frame.setContentPane(newContentPane);

		//Display the window.
		frame.pack();
		frame.setVisible(true);
		frame.setSize(1000, 800);
		
		return true;
	}
	
	private LasCurve getCurve(int iA) {
		LasCurve [] curves = _las.getCurves();
		int curveIndex = _selectedCurveIndex[iA];
		LasCurve curve = null;
		if(curveIndex==0) {
			curve = curves[0].copy();
			if(iA==2) {
				String curveName = _jButtonTF[iA].getText().trim();
				LasLine line = new LasLine(curveName, "none", "0", "Syn");
				curve.setLine(line);
			} else {
				float fv = Float.parseFloat(_jButtonTF[iA].getText().trim());
				float [] data = curve.getData();
				for(int i=0; i<data.length; i++) data[i] = fv;
			}
		} else {
			curve = curves[curveIndex];
		}
		return curve;
	}
	
//	public static void genProject(String projectName, VbFrame frame, LAS las, int iModule, int [] selectedCurveIndex, 
//			float [] boundaryDepth, double top, double bottom, double [] modelSize, double [] operation, int iUnit, 
//			double [] formationTop, String [] formationName) {
//		String fullPath = FilenameUtils.getFullPath(las.getFileName());
//
//		if(projectName==null) projectName = fullPath+"untitled.prj1d";
//		frame.createProject(projectName, las, iModule,selectedCurveIndex, boundaryDepth, top, bottom, 
//				modelSize, operation, iUnit, formationTop, formationName);
//		frame.getProject().setIDimension(1);
//		frame.getProject().setTtFileType(0);
//		frame.getProject().setVelFileType(1);
//		frame.getProject().setSrcMechFileName("null");
//		frame.getProject().setSeisPTVelFileName("null");
//		LasCurve [][] lasCurveGroup = frame.extractLasCurveGroup(top, bottom);
//		frame.setLasCurveGroup(lasCurveGroup);
//		frame.setModelSize(modelSize);
//		frame.initWorld();
//		frame.revalidate();
//		frame.refresh();
//	}

	private JComponent getCommandRow() {
		JButton ok = new JButton("OK");
		ok.addActionListener( new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				okAction();
			}
		});
		JButton cancel = new JButton("Cancel");
		cancel.addActionListener( new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				dispose();
			}
		});
		
		this.getRootPane().setDefaultButton( ok );
		List<JComponent> buttons = new ArrayList<JComponent>();
		buttons.add( ok );
		buttons.add( cancel );
		return UiUtil.getCommandRow( buttons );
	}
	
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
	
	
	private class LoadFilePanel extends JPanel implements ActionListener {

		private JButton 	_outputBrowserButton = null;

		public LoadFilePanel(String outputCwd) {
			setLayout(new GridBagLayout());
			Insets insets = new Insets(5, 5, 5, 5);
			GridBagConstraints gbc;

			_outputTF = new JTextField (outputCwd, 5);

			_outputBrowserButton = new JButton("Browse"); 
			_outputBrowserButton.setToolTipText("Browse a LAS log file");
			_outputBrowserButton.addActionListener(this);

			int iRow = 0;
			gbc= new GridBagConstraints(0, iRow++, 1, 1, 1.0, 1.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, insets, 0, 0);
			add(new JLabel("Output File:"), gbc);
			gbc= new GridBagConstraints(0, iRow, 2, 1, 1.0, 1.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, insets, 0, 0);
			add(_outputTF, gbc);
			gbc= new GridBagConstraints(2, iRow++, 1, 1, 0.0, 1.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, insets, 0, 0);
			add(_outputBrowserButton, gbc);
		}

		public void actionPerformed(ActionEvent actionevent) {
			if(actionevent.getSource() == _outputBrowserButton) 	{ 
				String fileName = _outputTF.getText().trim();
				if(fileName.isEmpty()) {
					fileName = System.getProperty("user.dir");
				} 

				FileNameExtensionFilter [] exts = new FileNameExtensionFilter [] { 
						new FileNameExtensionFilter("LAS (*.las)", "las") }; 

				String selectedFileName = null;
				if(_frame!=null) { 	
					selectedFileName = _frame.openFileUsingJFileChooser(exts, fileName); 
				}

				if(selectedFileName==null) return;
				else { _outputTF.setText(selectedFileName.trim()); }
			} 
		}	
	}
	private class CommonFrameInstant extends CommonFrame {
		public CommonFrameInstant(String title) { super(title); }
		public CommonProject getProject() {return null;}
		public IPanel getBaseWorld() {return null;}
		public Mosaic getMosaic() {return null;}
	}
}

