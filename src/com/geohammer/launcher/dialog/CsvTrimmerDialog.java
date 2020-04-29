package org.ucdm.launcher.dialog;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComponent;
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

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import org.apache.commons.io.FilenameUtils;
import org.ucdm.common.CommonDialog;
import org.ucdm.common.CommonFrame;
import org.ucdm.common.ProjectMsa;
import org.ucdm.excel.TerraVistaExcel;
import org.ucdm.excel.Well;
import org.ucdm.excel.tv.StageTreatment;
import org.ucdm.excel.tv.Treatment;
import org.ucdm.launcher.LauncherFrame;
import org.ucdm.common.d3.StageAttribute;

public class CsvTrimmerDialog  extends CommonDialog {
	private LauncherFrame 		_frame 				= null;
	private JTextField 	 		_sourceFolderTF 	= null;
	//private JTextField 	 		_targetFolderTF 	= null;
	private String  			_cwd 				= null;

	public int  				_iAppType 			= 0; //0-trimmer 1-splitter 2-spliterByWell
	public int  				_iFileType 			= 1; //0-event 1-treatment 2 - excel
	public int  				_nSegment 			= 2; //
	public JTextField  			_skipNumOfLinesTF 	= new JTextField("1"); //
	public ProjectMsa 			_projectMsa 		= null;
	public StageAttribute[][] 	_stageAttribute 	= null;
	public Treatment 			_treatment 			= null;
	public TerraVistaExcel  	_terraVistaExcel	= null;

	public CsvTrimmerDialog(JFrame aParent, String aTitle, boolean modal, int iAppType, String cwd) {
		super(aParent, aTitle, modal);
		setDialogWindowSize(1200, 300);
		if(iAppType==2) {
			setDialogWindowSize(1200, 240);
			setOKButtonLabel("Split By Event");
		} else if(iAppType==1) {
			setDialogWindowSize(1200, 300);
			setOKButtonLabel("Split");
		} else {
			setDialogWindowSize(1200, 240);
		}
		_frame 		= (LauncherFrame)aParent;
		_iAppType 	= iAppType;
		_cwd 		= cwd;
	}

	protected JPanel createContents() { 
		JPanel jPanel = new JPanel(new GridBagLayout());
		Insets insets = new Insets(5, 5, 5, 5);
		GridBagConstraints gbc;

		ButtonGroup moduleRadioGroup = null;
		String [] moduleString = null;
		JRadioButton [] moduleRadioButton = null;


		int iRow = 0;
		if(_iAppType==1||_iAppType==2) moduleString = new String[]{"MS Event CSV", "Treatment CSV"};
		else moduleString = new String[]{"MS Event CSV", "Treatment CSV", "Setup Excel File"};
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
					//if(j==1) _skipNumOfLinesTF.setEnabled(true);
					//else _skipNumOfLinesTF.setEnabled(false);
				}
			});

			moduleRadioGroup.add(moduleRadioButton[i]);
			gbc= new GridBagConstraints(i, iRow, 1, 1, 1.0, 1.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, insets, 0, 0);
			jPanel.add(moduleRadioButton[i], gbc);
		}

		_sourceFolderTF = new JTextField (_cwd);
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
		gbc= new GridBagConstraints(0, iRow, 4, 1, 1.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, insets, 0, 0);
		if(_iAppType==0) jPanel.add(new JLabel("Source File Name:"), gbc);
		else if(_iAppType==1) jPanel.add(new JLabel("Source File Name (Output file would be in the same folder as the source file):"), gbc);
		else if(_iAppType==2) jPanel.add(new JLabel("Source File Name (Output file would be in the same folder as the source file):"), gbc);
		iRow++;
		gbc= new GridBagConstraints(0, iRow, 3, 1, 1.0, 1.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, insets, 0, 0);
		jPanel.add(_sourceFolderTF, gbc);
		gbc= new GridBagConstraints(3, iRow, 1, 1, 0.0, 1.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, insets, 0, 0);
		jPanel.add(ssBB, gbc);
		
		if(_iAppType==1) {
			moduleString = new String[]{"2", "3", "4", "5", "6", "7", "8", "9", "10"};
			n = moduleString.length;
			JPanel modulePanel = new JPanel(new GridLayout(1, n, 2, 2));
			moduleRadioGroup = new ButtonGroup();
			moduleRadioButton = new JRadioButton[n];			
			for(int i=0; i<n; i++) {
				final int j = i+2;
				moduleRadioButton[i] = new JRadioButton(moduleString[i], _nSegment==j);
				moduleRadioButton[i].addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent event) {
						if(((JRadioButton)event.getSource()).isSelected()) _nSegment = j; 
					}
				});

				moduleRadioGroup.add(moduleRadioButton[i]);
				modulePanel.add(moduleRadioButton[i]);
			}	
			
			iRow++;		
			gbc= new GridBagConstraints(0, iRow, 4, 1, 1.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, insets, 0, 0);
			jPanel.add(new JLabel("Number of Segments The Source File Will be Splitted Into:"), gbc);
			
			iRow++;		
			gbc= new GridBagConstraints(0, iRow, 5, 1, 1.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, insets, 0, 0);
			jPanel.add(modulePanel, gbc);
		} else {
//			iRow++;		
//			gbc= new GridBagConstraints(0, iRow, 1, 1, 1.0, 1.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, insets, 0, 0);
//			jPanel.add(new JLabel("Number of lines to skip for reading one record :"), gbc);
//					
//			gbc= new GridBagConstraints(1, iRow, 1, 1, 1.0, 1.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, insets, 0, 0);
//			jPanel.add(_skipNumOfLinesTF, gbc);
		}
//		_targetFolderTF = new JTextField ("");
//		JButton stBB = new JButton("Browse");
//		stBB.addActionListener(new ActionListener() {
//			public void actionPerformed(ActionEvent e) {
//				String fileName = _frame.openDirectoryUsingJFileChooser(_targetFolderTF.getText()); 
//				if(fileName==null) return;
//				else {
//					_targetFolderTF.setText(fileName);
//				}
//			}
//		}); 
//		iRow++;		
//		gbc= new GridBagConstraints(0, iRow, 4, 1, 1.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, insets, 0, 0);
//		jPanel.add(new JLabel("Destination File Folder:"), gbc);
//
//		iRow++;
//		gbc= new GridBagConstraints(0, iRow, 3, 1, 1.0, 1.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, insets, 0, 0);
//		jPanel.add(_targetFolderTF, gbc);
//		gbc= new GridBagConstraints(3, iRow, 1, 1, 0.0, 1.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, insets, 0, 0);
//		jPanel.add(stBB, gbc);
		
		return jPanel;
	}

	protected boolean okAction() {
		String fileName = _sourceFolderTF.getText().trim();
		if(_iAppType==2) {
			if(_iFileType==0||_iFileType==1) splitFileByWell(fileName);
		} else if(_iAppType==1) {
			if(_iFileType==0||_iFileType==1) splitFile(fileName, _nSegment);
		} else if(_iAppType==0) {
			String aTitle = new String("TV File Trimmer");
			String projectFileName = "untitled.prjtlt";
			if(_iFileType==0) {
				_projectMsa = new ProjectMsa(projectFileName, 120, 2, 0, fileName, null, null, null);
				_projectMsa.readEventCsv();
				_projectMsa.genTerraVistaColumn();
				_projectMsa.genStageAttribute();
				_stageAttribute = _projectMsa.getStageAttribute();
			} else if(_iFileType==1){
				int skipNumOfLines = Integer.parseInt(_skipNumOfLinesTF.getText().trim());
				_projectMsa = new ProjectMsa(projectFileName, 120, 2, 0, null, null, fileName, null);
				_projectMsa.setTreatmentRowIncrement(skipNumOfLines);
				_projectMsa.readTreatment(null);
				_projectMsa.processTreatment();
				_treatment = _projectMsa.getTreatment();
			} else if(_iFileType==2){
				_projectMsa = new ProjectMsa(projectFileName, 120, 2, 0, null, fileName, null, null);
				_projectMsa.readTerraVistaExcel();
				_terraVistaExcel = _projectMsa.getTerraVistaExcel();
			}
			TrimmerDialog dialog = new TrimmerDialog(_frame, aTitle, false);
			dialog.showDialog();
		}

		return true;
	}
	
	public void splitFile(String fileName, int nSegments) { 
		String inputFileName = fileName;
		String fullPath 	= FilenameUtils.getFullPath(fileName);
		String baseName 	= FilenameUtils.getBaseName(fileName);
		String ext 			= FilenameUtils.getExtension(fileName);
		int [] endIndex 	= null;
		BufferedReader br 	= null;
		String line = null;
		int k = 0;			
		try {
			br =  new BufferedReader(new InputStreamReader(new FileInputStream(inputFileName), "UTF8"));
			line = br.readLine();
			while ((line = br.readLine()) != null) k++;				
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (br != null) br.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		} 

		if(k<=nSegments) return;

		int dk = k/nSegments;
		//if(k%nSegments>0) nSegments++;
		endIndex = new int[nSegments+1];
		for(int i=0; i<nSegments; i++) endIndex[i+1] = (i+1)*dk;
		endIndex[0] = 0;
		endIndex[endIndex.length-1] = k;
		//System.out.println("k="+k+" nSegments="+nSegments+" dk="+dk+Arrays.toString(endIndex));

		BufferedWriter bw = null;
		try {
			br =  new BufferedReader(new InputStreamReader(new FileInputStream(inputFileName), "UTF8"));
			String columnName = br.readLine();
			for(int i=0; i<endIndex.length-1; i++) {
				String selectedFileName = fullPath+baseName+(i+1)+"."+ext;
				//System.out.println(selectedFileName);
				bw = new BufferedWriter(new FileWriter(selectedFileName));
				bw.write(columnName); bw.newLine();
				for(int j=endIndex[i]; j<endIndex[i+1]; j++) {
					line = br.readLine();
					bw.write(line); 
					bw.newLine();
				}
				bw.close();
			}

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (bw != null) bw.close();
				if (br != null) br.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
	}	
	public void splitFileByWell(String fileName) { 
		String inputFileName = fileName;
		String fullPath 	= FilenameUtils.getFullPath(fileName);
		String baseName 	= FilenameUtils.getBaseName(fileName);
		String ext 			= FilenameUtils.getExtension(fileName);
		BufferedReader br 	= null;
		String line 		= null;
		int p1 				= -1;
		int p2 				= p1;
		String prevWellName = "none";
		String wellName 	= "none1";
		
		ArrayList<String> wellNameList = new ArrayList<String>();
		ArrayList<Integer> index1List = new ArrayList<Integer>();
		int k = 0;			
		try {
			br =  new BufferedReader(new InputStreamReader(new FileInputStream(inputFileName), "UTF8"));
			line = br.readLine();
			if(_iFileType==0) {
				while ((line = br.readLine()) != null) {
					p1 = line.indexOf(',');
					p2 = line.indexOf(',', p1+1);
					wellName = line.substring(p1+1, p2).trim();
					if(!wellName.equals(prevWellName)) {
						prevWellName = wellName;
						wellNameList.add(wellName);
						index1List.add(k);						
					}
					k++;				
				}
			} else if(_iFileType==1) {
				while ((line = br.readLine()) != null) {
					p2 = line.indexOf(',');
					if(p2>=1) {
						wellName = line.substring(0, p2).trim();
						if(!wellName.equals(prevWellName)) {
							prevWellName = wellName;
							wellNameList.add(wellName);
							index1List.add(k);						
						}
					}
					k++;				
				}
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
		
		int n = index1List.size();
		if(n==0) return;
		for(int i=0; i<wellNameList.size(); i++) {
			wellName = wellNameList.get(i);
		}
		
		ArrayList<Integer> index2List = new ArrayList<Integer>();
		for(int i=0; i<n-1; i++)  index2List.add(index1List.get(i+1)); 
		index2List.add(k);
		
		Set<String> set 			= new HashSet<String>();    
		ArrayList<String> uniqueList 	= new ArrayList<String>(); 
		for (String element : wellNameList) {      
			if (set.add(element))  {
				uniqueList.add(element);  
			}
		}
//		for(int i=0; i<uniqueList.size(); i++) {
//			wellName = uniqueList.get(i);
//			String outputFileName = fullPath+baseName+"_"+wellName+"_"+(i+1)+"."+ext;
//			System.out.println(wellName+" "+outputFileName); 
//			int [][] index = findIndex(wellName, wellNameList, index1List, index2List);
//			for(int ik=0; ik<index.length; ik++) System.out.println(index[ik][0]+" "+index[ik][1]);
//			//write(inputFileName, outputFileName, index);
//		}
	}	
	
	private void write(String inputFileName, String outputFileName, int [][] index) {
		BufferedReader br = null;
		BufferedWriter bw = null;
		try {
			br =  new BufferedReader(new InputStreamReader(new FileInputStream(inputFileName), "UTF8"));
			bw = new BufferedWriter(new FileWriter(outputFileName));
			//String columnName = br.readLine();
			bw.write(br.readLine()); bw.newLine();
			String line = null;
			int k = 0;
			while ((line = br.readLine()) != null) {
				for(int i=0; i<index.length; i++) {
					if(k>=index[i][0] && k<index[i][1]) {
						bw.write(line); 
						bw.newLine();
					}
				}				
				k++;				
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (bw != null) bw.close();
				if (br != null) br.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
	}
	
	private int [][] findIndex(String name, ArrayList<String> wellNameList, ArrayList<Integer> index1List, ArrayList<Integer> index2List) {
		int k = 0;
		for(int i=0; i<wellNameList.size(); i++) {
			if(name.equals(wellNameList.get(i))) k++;
		}
		
		int [][] index = new int[k][2];
		k = 0;
		for(int i=0; i<wellNameList.size(); i++) {
			if(name.equals(wellNameList.get(i))) {
				index[k][0] = index1List.get(i);
				index[k][1] = index2List.get(i);
				k++;
			}
		}
		return index;
	}

	public void saveFile(String fileName) { 
		String inputFileName = null;
		if(_iFileType==0) {
			int k = 0;
			inputFileName = _projectMsa.getTvEventFileName();
			for(int i=0; i<_stageAttribute.length; i++) {
				for(int j=0; j<_stageAttribute[i].length; j++) {
					int k0 = _stageAttribute[i][j].getIndex0();
					int k1 = _stageAttribute[i][j].getIndex1();
					int numOfEvents = k1-k0;
					k += numOfEvents;
				}
			}
			boolean [] enabled = new boolean[k];
			k = 0;
			for(int i=0; i<_stageAttribute.length; i++) {
				for(int j=0; j<_stageAttribute[i].length; j++) {
					int k0 = _stageAttribute[i][j].getIndex0();
					int k1 = _stageAttribute[i][j].getIndex1();
					boolean a = _stageAttribute[i][j].getVisible();
					int numOfEvents = k1-k0;
					for(int jk=0; jk<numOfEvents; jk++, k++) {
						enabled[k] = a;
					}
				}
			}
			BufferedWriter bw = null;
			BufferedReader br = null;
			k = 0;
			try {
				br =  new BufferedReader(new InputStreamReader(new FileInputStream(inputFileName), "UTF8"));
				bw = new BufferedWriter(new FileWriter(fileName));

				String line = br.readLine();
				bw.write(line+"\n");
				while ((line = br.readLine()) != null) { 
					if(enabled[k]) bw.write(line+"\n");
					k++;
				}
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				try {
					if (bw != null) bw.close();
					if (br != null) br.close();
				} catch (IOException ex) {
					ex.printStackTrace();
				}
			} 
		} else if(_iFileType==1) {
			_treatment.write(fileName);
		} else if(_iFileType==2) {
			int k = 0;
			inputFileName = _projectMsa.getTvWellFileName();
			
			try {
				FileInputStream inputFile = new FileInputStream(new File(inputFileName));
				Workbook workbook = new XSSFWorkbook(inputFile);
				int n = workbook.getNumberOfSheets();
		        
				for(int i=n-1; i>=0; i--) {
					String sheetName = workbook.getSheetAt(i).getSheetName();
					int id = getId(sheetName);
					if(id==-100) workbook.removeSheetAt(i);
				}					
				
				FileOutputStream outputStream = new FileOutputStream(fileName);
	            workbook.write(outputStream);
	            workbook.close();
	            
			} catch (FileNotFoundException e) {
	            e.printStackTrace();
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
		}
		//System.out.println("Successfully Done: "+fileName);
	}
	
	public int getId(String sheetName) {
		ArrayList<Well> wells = null;
		for(int i=0; i<3; i++) {	
			String displayWellName = null;
			if(i==0) 		{ wells = _terraVistaExcel.getWells(); displayWellName = "Wells"; }
			else if(i==1) 	{ wells = _terraVistaExcel.getPerfs(); displayWellName = "Perfs"; }
			else if(i==2) 	{ wells = _terraVistaExcel.getTools(); displayWellName = "Tools"; }
			

			for(int j=0; j<wells.size(); j++) {
				String stageName = wells.get(j).getSpreadsheetName();
				if(sheetName.equals(stageName)) return wells.get(j).getId();
			}
		}
		return 100;
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
				saveFile(fileName);			
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
