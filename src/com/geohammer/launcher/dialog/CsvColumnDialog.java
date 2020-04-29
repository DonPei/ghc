package org.ucdm.launcher.dialog;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Enumeration;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import org.ucdm.common.CommonDialog;
import org.ucdm.common.CommonFrame;
import org.ucdm.common.ProjectMsa;
import org.ucdm.excel.TerraVistaExcel;
import org.ucdm.excel.Well;
import org.ucdm.excel.tv.StageTreatment;
import org.ucdm.excel.tv.Treatment;
import org.ucdm.launcher.LauncherFrame;
import org.ucdm.vc.dialog.PlotDialog;
import org.ucdm.common.d3.StageAttribute;

public class CsvColumnDialog  extends CommonDialog {
	private LauncherFrame 		_frame 				= null;
	private JTextField 	 		_sourceFolderTF 	= null;
	private JTextField 	 		_iColTF 			= null;
	private JTextField 	 		_nHeadTF 			= null;
	
	public int  				_iFileType 			= 0; 
	
	public int  				_nSegment 			= 2; //
	public JTextField  			_skipNumOfLinesTF 	= new JTextField("1"); //
	public ProjectMsa 			_projectMsa 		= null;
	public StageAttribute[][] 	_stageAttribute 	= null;
	public Treatment 			_treatment 			= null;
	public TerraVistaExcel  	_terraVistaExcel	= null;

	public CsvColumnDialog(JFrame aParent, String aTitle, boolean modal, int iFileType, 
			String fileName, String iColS, String nHeadS) {
		super(aParent, aTitle, modal);
		setDialogWindowSize(1000, 300);
		_frame 		= (LauncherFrame)aParent;
		_iFileType 	= iFileType;

		_sourceFolderTF = new JTextField(fileName);
		if(iColS.startsWith("C")) _iColTF = new JTextField("0");
		else _iColTF = new JTextField(iColS);
		if(iColS.startsWith("C")) _nHeadTF = new JTextField("0");
		else _nHeadTF = new JTextField(nHeadS);
	}

	protected JPanel createContents() { 
		JPanel jPanel = new JPanel(new GridBagLayout());
		Insets insets = new Insets(5, 5, 5, 5);
		GridBagConstraints gbc;

		ButtonGroup moduleRadioGroup = null;
		String [] moduleString = null;
		JRadioButton [] moduleRadioButton = null;


		int iRow = 0;
		moduleString = new String[]{"Time Serial CSV", "Treatment CSV"};
		int n = moduleString.length;
		moduleRadioGroup = new ButtonGroup();
		moduleRadioButton = new JRadioButton[n];	
		//_skipNumOfLinesTF.setEnabled(_iFileType==1);
		for(int i=0; i<n; i++) {
			final int j = i;
			moduleRadioButton[i] = new JRadioButton(moduleString[i], _iFileType==j);
			moduleRadioButton[i].addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent event) {
					if(((JRadioButton)event.getSource()).isSelected()) _iFileType = j; 
				}
			});

			moduleRadioGroup.add(moduleRadioButton[i]);
			gbc= new GridBagConstraints(2*i, iRow, 1, 1, 1.0, 1.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, insets, 0, 0);
			jPanel.add(moduleRadioButton[i], gbc);
		}

		JButton ssBB = new JButton("Browse");
		ssBB.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				FileNameExtensionFilter exts[] = new FileNameExtensionFilter [] {
						new FileNameExtensionFilter("csv (*.csv)", "csv"),
						new FileNameExtensionFilter("xlsx (*.xlsx)", "xlsx")};
				String fileName = _frame.openFileUsingJFileChooser(exts, _sourceFolderTF.getText()); 
				if(fileName==null) return;
				else {
					_sourceFolderTF.setText(fileName);
				}
			}
		}); 

		iRow++;		
		gbc= new GridBagConstraints(0, iRow, 5, 1, 1.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, insets, 0, 0);
		jPanel.add(new JLabel("Source File Name:"), gbc);
		
		iRow++;
		gbc= new GridBagConstraints(0, iRow, 4, 1, 1.0, 1.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, insets, 0, 0);
		jPanel.add(_sourceFolderTF, gbc);
		gbc= new GridBagConstraints(4, iRow, 1, 1, 0.0, 1.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, insets, 0, 0);
		jPanel.add(ssBB, gbc);
		
		iRow++;
		int jx = 0;
		gbc= new GridBagConstraints(jx++, iRow, 1, 1, 1.0, 1.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, insets, 0, 0);
		jPanel.add(new JLabel("Column Index:", JLabel.RIGHT), gbc);
		gbc= new GridBagConstraints(jx++, iRow, 1, 1, 1.0, 1.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, insets, 0, 0);
		jPanel.add(_iColTF, gbc);
		gbc= new GridBagConstraints(jx++, iRow, 1, 1, 1.0, 1.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, insets, 0, 0);
		jPanel.add(new JLabel("# of Header Line:", JLabel.RIGHT), gbc);
		gbc= new GridBagConstraints(jx++, iRow, 1, 1, 1.0, 1.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, insets, 0, 0);
		jPanel.add(_nHeadTF, gbc);
		
		return jPanel;
	}

	protected boolean okAction() {
		String fileName = _sourceFolderTF.getText().trim();
		_frame.setCwd(25, fileName);
		String a = _iColTF.getText().trim();
		_frame.setCwd(26, a);
		int iCol = Integer.parseInt(a);
		a = _nHeadTF.getText().trim();
		int nHead = Integer.parseInt(a);
		_frame.setCwd(27, a);
		
		if(_iFileType==0) {
			String aTitle = new String("CSV File");
			float [] data = read(fileName, iCol, nHead);
			float [] dataI = new float[data.length];
			for(int i=0; i<data.length; i++) dataI[i] = i;
			//System.out.println("n="+data.length);
			
			PlotDialog dialog = new PlotDialog(_frame, "Component ", false, 
					null, 160, false, "Data", "Sample #", "Sample Value");
			dialog.setCurves(new float [][] {dataI, data});
			dialog.showDialog();
			
			//TrimmerDialog dialog = new TrimmerDialog(_frame, aTitle, false);
			//dialog.showDialog();
		}

		return true;
	}
	
	public float [] read(String fileName, int iCol, int nHead) { 
		String inputFileName = fileName;
		BufferedReader br 	= null;
		String line = null;
		ArrayList<Float> dataList = new ArrayList<Float>();
		String a = null;
		
		float c = -999.25f;
		float c0 = c;
		String [] splits = null;
		String delimit = "[,]";
		int k = 0;
		try {
			br =  new BufferedReader(new InputStreamReader(new FileInputStream(inputFileName), "UTF8"));
			for(int i=0; i<nHead; i++) line = br.readLine();
			while ((line = br.readLine()) != null) {
				splits = line.split(delimit);
				a = splits[iCol].trim();
				if(a==null) {
					dataList.add(c0);
				} else {
					dataList.add(Float.parseFloat(a));
				}
				k++;				
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (br != null) br.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		} 

		if(k<=0) return null;
		float [] data = new float[dataList.size()];
		for(int i=0; i<data.length; i++) {
			data[i] = dataList.get(i);
		}
		return data;		
	}	
	
	private class TrimmerDialog  extends CommonDialog {
		private CommonFrame 		_frame 				= null;
		private TrimmerTreePanel 	_trimmerTreePanel	= null;

		public TrimmerDialog(JFrame aParent, String aTitle, boolean modal) {
			super(aParent, aTitle, modal);
			setDialogWindowSize(800, 1000);
			setBackground(Color.white);	
			setApplyButtonLabel("Save As...");
			setEnableOKButton(false);
			setEnableApplyButton(true);
			_frame 		= (CommonFrame)aParent;
		}

		//protected JComponent getCommandRow() 	{ return null; }
		//protected boolean okAction() 			{ return true; }
		protected boolean okAction() 			{ 
			FileNameExtensionFilter exts[] = new FileNameExtensionFilter [] {
					new FileNameExtensionFilter("CSV (*.csv)", "csv"),
					new FileNameExtensionFilter("xlsx (*.xlsx)", "xlsx")
			};
			String inputFileName = null;

			//String targetFoler = _targetFolderTF.getText().trim();
			
			if(_iFileType==0) {
				inputFileName = _projectMsa.getTvEventFileName();
			} else if(_iFileType==1) {
				inputFileName = _projectMsa.getTvTreatmentFileName();
			} else if(_iFileType==2) {
				inputFileName = _projectMsa.getTvWellFileName();
			}
			String fileName = _frame.saveFileUsingJFileChooser(exts, inputFileName); 
			if(fileName==null) return false;
			else {
				//String selectedExt = FilenameUtils.getExtension(fileName);
				//if(selectedExt==null||selectedExt.isEmpty()) fileName = fileName+"."+ext;
				//saveFile(fileName);			
				String a = _trimmerTreePanel._textPane.getText()+"Successfully Saved As: "+fileName+"\n";
				_trimmerTreePanel._textPane.setText(a);
			}
			return true; 
		}

		protected JPanel createContents() { 
			_trimmerTreePanel	= new TrimmerTreePanel();
			return _trimmerTreePanel;
		}
	}

	private class TrimmerTreePanel  extends JPanel implements TreeSelectionListener {

		private JTree 		_tree 			= null;
		public JTextPane 	_textPane		= null;
		public JPopupMenu 	_popupMenu 		= new JPopupMenu();
		
		public TrimmerTreePanel() {
			setLayout(new BorderLayout());
			JSplitPane splitPane = createContents();
			add(splitPane, BorderLayout.CENTER);
		}

		private JSplitPane createContents() {
			JSplitPane splitPane = null;	
			_tree = constructTree();

			_tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
			_tree.addTreeSelectionListener(this);
			_tree.addMouseListener ( new MouseAdapter ()   {
				public void mousePressed ( MouseEvent e )       {
					if ( SwingUtilities.isRightMouseButton ( e ) )   {
						TreePath path = _tree.getPathForLocation(e.getX(), e.getY());
						StageTreeNode node = (StageTreeNode)path.getLastPathComponent();
						if (node == null) return;
						int iWell = node.getWellIndex();
						int iStage = node.getStageIndex();
						if(iStage==-1) {
							_popupMenu = new JPopupMenu();
							setPopupMenu(path, node);
							_popupMenu.show(e.getComponent(), e.getX(), e.getY());
						}
					}
				}
			} );

			TreeNode root = (TreeNode) _tree.getModel().getRoot();
			expandAll(_tree, new TreePath(root));

			JScrollPane treeView = new JScrollPane(_tree);
			treeView.setMinimumSize(new Dimension(450, 600));     
			treeView.setPreferredSize(new Dimension(450, 600));

			splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
			splitPane.setLeftComponent(treeView);
//			String htmlText = "<html>" + "<font size=\"5\" color=\"maroon\"><b>File Trimmer Usage:</b></font>";
//			String usageText = htmlText +
//					"<UL>" +
//					"  <LI>To select/unselect item: <b>Left-click</b>;" +
//					"  <LI>To save trimmed contents: <b>click Save As...</b>"+					
//					"</UL>" +
//					"\n\n";
			
			String usageText = "File Trimmer Usage:\n" +
					"To select/unselect item: Left-click;\n" +
					"Right-click for menu...\n\n\n";
			_textPane = new JTextPane();
			_textPane.setText(usageText);
			splitPane.setRightComponent(_textPane); 

			splitPane.setDividerLocation(0.7); 
			splitPane.setPreferredSize(new Dimension(900, 1000));
			//System.out.println("size="+splitPane.getDividerSize());

			return splitPane;
		}
		
		public void setPopupMenu(TreePath path, StageTreeNode node) {
			JMenuItem jMenuItem = null;
			
			jMenuItem = new JMenuItem("Toggle Stages");
			_popupMenu.add(jMenuItem);
			//jMenuItem.setToolTipText("participate calculation");
			jMenuItem.addActionListener( new ActionListener() {
				public void actionPerformed( ActionEvent e ) {
					int iWell = node.getWellIndex();
					int nRow = _tree.getRowCount();
					for(int i=0; i<nRow; i++){
						TreePath rPath = _tree.getPathForRow(i);
						StageTreeNode rNode = (StageTreeNode)rPath.getLastPathComponent();
						if(rNode.getWellIndex()==iWell) {
							if(rNode.isLeaf()) {
								rNode.setEnabled(!rNode.getEnabled());
							}
						}
					}
					repaint();
				}
			});
		}
		
		public void valueChanged(TreeSelectionEvent e) {
			StageTreeNode node = (StageTreeNode)_tree.getLastSelectedPathComponent();
			if(node==null) return;
			if(node.isLeaf()) {
				node.setEnabled(!node.getEnabled());
			} else {
				
			}
		}

		private JTree constructTree() {
			StageTreeNode leftNode = new StageTreeNode();
			if(_iFileType==0) {
				int k = 0;
				for(int i=0; i<_stageAttribute.length; i++) {	
					String displayWellName = _stageAttribute[i][0].getWellName();
					StageTreeNode wellNode = new StageTreeNode(displayWellName, i, "All", -1, k, true);

					for(int j=0; j<_stageAttribute[i].length; j++, k++) {
						int k0 = _stageAttribute[i][j].getIndex0();
						int k1 = _stageAttribute[i][j].getIndex1();

						String stageName = _stageAttribute[i][j].getStageName();
						int p = stageName.indexOf("Stage");
						String displayStageName = stageName.substring(p);
						//int numOfEvents = k1-k0;
						//System.out.println("i1="+i+" j="+j+" k0="+k0+" k1="+k1+" s="+displayStageName+" n="+numOfEvents+" w="+displayWellName);

						StageTreeNode stageNode = new StageTreeNode(displayWellName, i, displayStageName, j, k, true);

						wellNode.add(stageNode);
					}
					leftNode.add(wellNode);
				}				
			} else if(_iFileType==1){
				ArrayList<StageTreatment> 	stageTreatments 	=  _treatment.getStageTreatments();
				int k = 0;
				for(int i=0; i<stageTreatments.size(); i++, k++) {
					StageTreatment a = stageTreatments.get(i);
					String stageName = a.getWellName()+"-"+a.getStageName();
					StageTreeNode stageNode = new StageTreeNode("well 0", 0, stageName, i, k, true);
					leftNode.add(stageNode);
					//System.out.println(stageName+" "+a.getStartIndex()+" "+a.getEndIndex());
				}				
			} else if(_iFileType==2){
				ArrayList<Well> wells = null;
				int k = 0;
				for(int i=0; i<3; i++) {	
					String displayWellName = null;
					if(i==0) 		{ wells = _terraVistaExcel.getWells(); displayWellName = "Wells"; }
					else if(i==1) 	{ wells = _terraVistaExcel.getPerfs(); displayWellName = "Perfs"; }
					else if(i==2) 	{ wells = _terraVistaExcel.getTools(); displayWellName = "Tools"; }
					
					StageTreeNode wellNode = new StageTreeNode(displayWellName, i, "All", 0, k, true);

					for(int j=0; j<wells.size(); j++, k++) {
						String stageName = wells.get(j).getSpreadsheetName();
						String displayStageName = stageName;
						//int numOfEvents = k1-k0;
						//System.out.println("i1="+i+" j="+j+" k0="+k0+" k1="+k1+" s="+displayStageName+" n="+numOfEvents+" w="+displayWellName);

						StageTreeNode stageNode = new StageTreeNode(displayWellName, i, displayStageName, j, k, true);

						wellNode.add(stageNode);
					}
					leftNode.add(wellNode);
				}
			}


			JTree tree = new JTree(leftNode){ 
				private static final long serialVersionUID = 1L;

				public String getToolTipText(MouseEvent evt) {
					int iRow = getRowForLocation(evt.getX(), evt.getY());
					if (iRow==-1||iRow==0) return null;    
					TreePath curPath = getPathForLocation(evt.getX(), evt.getY());
					return ((StageTreeNode)curPath.getLastPathComponent()).getToolTipText();
				}
			};
			tree.setToolTipText("");

			tree.setCellRenderer(new DefaultTreeCellRenderer() {
				public Component getTreeCellRendererComponent(JTree tree, Object value,
						boolean sel, boolean exp, boolean leaf, int row, boolean hasFocus) {
					super.getTreeCellRendererComponent(tree, value, sel, exp, leaf, row, hasFocus);
					StageTreeNode node = (StageTreeNode)(value);
					if(node.isLeaf()) {
						if (!node.getEnabled()) { 
							setForeground(Color.red); 
							if(_iFileType==0 ) _stageAttribute[node._wellIndex][node._stageIndex].setVisible(false);
							else if(_iFileType==1 ) _treatment.getStageTreatments(node._stageIndex).setEnabled(false);
							else if(_iFileType==2 ) {
								ArrayList<Well> wells = null;
								if(node._wellIndex==0) 			{ wells = _terraVistaExcel.getWells(); }
								else if(node._wellIndex==1) 	{ wells = _terraVistaExcel.getPerfs(); }
								else if(node._wellIndex==2) 	{ wells = _terraVistaExcel.getTools(); }
								wells.get(node._stageIndex).setId(-100);
							}
						} else {
							if(_iFileType==0 ) _stageAttribute[node._wellIndex][node._stageIndex].setVisible(true);
							else if(_iFileType==1 ) _treatment.getStageTreatments(node._stageIndex).setEnabled(true);
							else if(_iFileType==2 ) {
								ArrayList<Well> wells = null;
								if(node._wellIndex==0) 			{ wells = _terraVistaExcel.getWells(); }
								else if(node._wellIndex==1) 	{ wells = _terraVistaExcel.getPerfs(); }
								else if(node._wellIndex==2) 	{ wells = _terraVistaExcel.getTools(); }
								wells.get(node._stageIndex).setId(100);
							}
						}
					}
					return this;
				}  
			});

			return tree;
		}

		private void expandAll(JTree tree, TreePath parent) {
			TreeNode node = (TreeNode) parent.getLastPathComponent();
			if (node.getChildCount() >= 0) {
				for (Enumeration e = node.children(); e.hasMoreElements();) {
					TreeNode n = (TreeNode) e.nextElement();
					TreePath path = parent.pathByAddingChild(n);
					expandAll(tree, path);
				}
			}
			tree.expandPath(parent);
			// tree.collapsePath(parent);
		}
	}



	private class StageTreeNode extends DefaultMutableTreeNode {
		private String 		_wellName 			= null;
		private int 		_wellIndex 			= 0;
		private String 		_stageName 			= null;
		private int 		_stageIndex 		= 0;
		private int 		_index 				= 0;
		private boolean  	_enabled			= true;

		public StageTreeNode() {
			super();
		}
		public StageTreeNode(String wellName, int wellIndex, String stageName, int stageIndex, int index, boolean enabled) {
			super(wellName+"_"+stageName);
			_wellName 	= wellName;
			_wellIndex 	= wellIndex;
			_stageName 	= stageName;
			_stageIndex = stageIndex;
			_index 		= index;
			_enabled 	= enabled;
		}

		public String getToolTipText() { return String.format("wellIndex=%d stageIndex=%d index=%d", 
				_wellIndex, _stageIndex, _index); }

		public String 	getWellName() 		{ return _wellName; }
		public int 		getWellIndex() 		{ return _wellIndex; }
		public String 	getStageName() 		{ return _stageName; }
		public int 		getStageIndex() 	{ return _stageIndex; }
		public int 		getIndex() 			{ return _index; }

		public boolean getEnabled() 		{ return _enabled; }
		public void setEnabled(boolean enabled) { _enabled = enabled; }

		//		public boolean	[][] getEnabled() 					{ return _enabled; }
		//		public boolean	[] getEnabledWell() 				{ return _enabled[_wellIndex]; }
		//		public boolean getEnabledStage() 					{ return _enabled[_wellIndex][_stageIndex]; }
		//
		//		public void setEnabledStage(boolean enabledStage) 	{ _enabled[_wellIndex][_stageIndex] = enabledStage; }
	}

}
