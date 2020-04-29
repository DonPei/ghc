package org.ucdm.tracedisplay;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.filechooser.FileSystemView;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import org.apache.commons.io.FilenameUtils;
import org.ucdm.common.CommonDialog;
import org.ucdm.common.CommonFrame;
import org.ucdm.common.CommonPanel;
import org.ucdm.common.TextViewDialog;
import org.ucdm.common.UcdmUtils;
import org.ucdm.component.StatusBar;
import org.ucdm.core.SeismicTraceComponent;
import org.ucdm.seg2.SEG2;
import org.ucdm.seg2.SEG2FileHeader;
import org.ucdm.seg2.SEG2StringBlock;

@SuppressWarnings("serial")
public class TraceDisplayTreeDialog  extends CommonDialog implements TreeSelectionListener {
	private CommonFrame 	_frame 			= null;
	private JSplitPane 		_splitPane 		= null;
	private TracePanel 		_world 			= null;
	private int 			_iFold 			= 3;
	private int 			_id 			= 0; // 2 FD2D 3 FD3D 11 DWN bin
	public int 				_iDataType 		= 0; //0-SEG2 1-Green Function

	private File 			_selectedFile 	= null;
	private JTree 			_tree 			= null;
	String  				_dataPath		= null;
	String [] 				_fileNames 		= null;
	String [] 				_fileFullNames 	= null;
	
	private JPanel 				_jPanel 	= null;
	private TraceDisplayToolBar _traceDisplayToolBar = null;
	
	public TraceDisplayTreeDialog(CommonFrame aParent, String aTitle, boolean modal, 
			int id, int iFold, int iDataType, String dataPath) {
		super((JFrame)aParent, aTitle, modal);
		setBackground(Color.white);
		setDialogWindowSize(1800, 1000);
		_frame 	= aParent;
		_id 	= id;
		_iFold  = iFold;
		_iDataType  = iDataType;
		_dataPath  = dataPath;

		StatusBar statusBar = new StatusBar(); 
		statusBar.setZoneBorder(BorderFactory.createLineBorder(Color.GRAY)); 

		statusBar.setZones( new String[] { "first_zone", "second_zone", "third_zone", "remaining_zones" },     
				new Component[] { new JLabel("first"),  new JLabel("second"),  new JLabel("third"),  new JLabel("remaining")     },     
				new String[] {"20%", "6%", "60%", "*"} );

		String usageText = "<html>Left click to <b>selection</b>; Right for <b>menu</b>"
				+ "; Alt+Shift+drag/click for <b>zoom</b></html>";
		JLabel usageLabel  = (JLabel) statusBar.getZone("third_zone");
		usageLabel.setText(usageText);
		usageLabel.setHorizontalAlignment(JLabel.LEFT);

		_world = new TracePanel(1, 1, CommonPanel.Orientation.X1DOWN_X2RIGHT, CommonPanel.AxesPlacement.LEFT_TOP, statusBar);
		_world.setVLabel("Channel No.");
		_world.setBackground(Color.white);
		_world.setFrame(_frame);
		_frame.setCommonPanel(_world);	
		_world.initModeManager();		

		_traceDisplayToolBar = new TraceDisplayToolBar(_world, 0, null);
		_jPanel = new JPanel(new BorderLayout());
		_jPanel.add(_traceDisplayToolBar, BorderLayout.NORTH);
		_jPanel.add(_world, BorderLayout.CENTER);
		
		addKeyListener(_kl);
		_world.addKeyListener(_kl);
		_frame.addKeyListener(_kl);
	}

	private KeyListener _kl = new KeyAdapter() {
		public void keyTyped(KeyEvent e) { 
			System.out.println("p1=");
		}
		public void keyReleased(KeyEvent e) { }
		public void keyPressed(KeyEvent e) {
			System.out.println("q1=");
		}
	};

	protected JComponent getCommandRow() { return null; }
	protected boolean okAction() {	return true;	}
	protected JSplitPane createContents() {
		File[] roots = new File [] { new File(_dataPath)};
		FileTreeNode rootTreeNode = new FileTreeNode(roots);
		_tree = new JTree(rootTreeNode);
		_tree.setCellRenderer(new FileTreeCellRenderer());
		_tree.setRootVisible(false);

		_tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		_tree.addTreeSelectionListener(this);
		_tree.addMouseListener ( new MouseAdapter ()   {
			public void mousePressed ( MouseEvent e )       {
				TreePath path = _tree.getPathForLocation ( e.getX (), e.getY () );
				if (path== null) return;
				
				if (e.isMetaDown())   {					
					JPopupMenu treePopupMenu = new JPopupMenu();
					setTreePopupMenu(treePopupMenu, path);
					treePopupMenu.show(e.getComponent(), e.getX(), e.getY());
				} 
			}
		} );

		JScrollPane treeJScrollPane = new JScrollPane(_tree);
		treeJScrollPane.setMinimumSize(new Dimension(250, 500));     
		treeJScrollPane.setPreferredSize(new Dimension(250, 500));

//		JEditorPane htmlPane = new JEditorPane();
//		htmlPane.setText("Click SEG2 file to display");
//		htmlPane.setEditable(false);
//		JScrollPane htmlView = new JScrollPane(htmlPane);

		_splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		_splitPane.setLeftComponent(treeJScrollPane);
		//_splitPane.setRightComponent(htmlView);
		_splitPane.setRightComponent(_jPanel);
		_splitPane.setDividerLocation(0.25); 
		//_splitPane.setPreferredSize(new Dimension(1200, 800));

		add(_splitPane);
		return _splitPane;
	}
	
	public void setTreePopupMenu(JPopupMenu treePopupMenu, TreePath path) {
		JMenuItem jMenuItem = null;
		
		jMenuItem = new JMenuItem("View File Header");
		treePopupMenu.add(jMenuItem);
		jMenuItem.addActionListener(new ActionListener() 	{
			public void actionPerformed(ActionEvent e)   {
		    	
				String fileName = null;
				try {
					fileName = _selectedFile.getCanonicalPath();
				} catch (IOException e1) {
					return;
				}
				
				if(!FilenameUtils.isExtension(fileName,"dat")) fileName += ".dat"; 
				SEG2 seg2 = new SEG2(fileName);

				String text = null;
				SEG2FileHeader fh = seg2.getSeg2FileHeader();
				text = fh.toString();
				SEG2StringBlock sb = fh.getStringBlock();
				text += sb.toString();
				
				TextViewDialog dialog = new TextViewDialog(_frame, "Trace Data", false, text);
				dialog.showDialog();
			}
		});
		
		treePopupMenu.addSeparator();
		
		jMenuItem = new JMenuItem("Refresh Tree");
		treePopupMenu.add(jMenuItem);
		jMenuItem.addActionListener(new ActionListener() 	{
			public void actionPerformed(ActionEvent e)   {
				DefaultTreeModel model = (DefaultTreeModel)_tree.getModel();
				FileTreeNode rootTreeNode = (FileTreeNode)model.getRoot();
				model.reload(rootTreeNode);
			}
		});
	}

	public TracePanel getWorld() { return _world; }

	public void valueChanged(TreeSelectionEvent e) {		
		FileTreeNode node = (FileTreeNode) e.getPath().getLastPathComponent();
		_selectedFile = node.getFile();	
		//_eventLabel.setText(_selectedFile==null?"null selected":_selectedFile.getName());
		
		String selectedFileName = null;
		try {
			selectedFileName = node.getFile().getCanonicalPath();
		} catch (IOException e1) {
			e1.printStackTrace();
			return;
		}
		//System.out.println(selectedFileName);
		File file = new File(selectedFileName);
		if(file.isDirectory()) return;
		
		if (FilenameUtils.isExtension(selectedFileName, "txt")) {
			String fileAsString = UcdmUtils.readTextFile(selectedFileName);
			JScrollPane scroll = new JScrollPane(new JTextArea(fileAsString));
			scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED); 
			scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED); 
			scroll.setMinimumSize(new Dimension(400, 300));   
			_splitPane.setRightComponent(scroll);
		} else {
			_splitPane.setRightComponent(_jPanel);
			updateCurve(selectedFileName);
		}
		
//		if (FilenameUtils.isExtension(selectedFileName, "dat")) {
//			//_eventIndex 	= eventIndex;
//			//_selectedFileName = selectedFileName;
//			updateCurve(selectedFileName);
//			//_eventLabel.setText(FilenameUtils.getBaseName(_selectedFileName));
//		} else {
////			Seg2View lv = (Seg2View)getView("SEG2", null);
////			if(lv!=null) { savePicks(lv.getSEG2InfTxt());  }
//		}
		
	}
	
	public void updateCurve(String absFileName) {
		if(absFileName==null) return;
		getWorld().removeAllViews(0, 0);

		SeismicTraceComponent [] comps = null;
		if(_iDataType==0) {
			SEG2 seg2 = new SEG2(absFileName);
			int k = seg2.getNumOfTraces();
			comps = new SeismicTraceComponent[k];
			for(int i=0; i<k; i++) {
				comps[i] = new SeismicTraceComponent(0, i/3, i%3, seg2.getSEG2Trace(i));
			}
		} else if(_iDataType==1) {
			comps = UcdmUtils.readBinaryFileToTrace(absFileName);
			if(comps==null) return;
			int maxIndex = 0;
			for(int i=0; i<comps.length; i++) {
				maxIndex = maxIndex>comps[i].getData().length?maxIndex:comps[i].getData().length;
			}
			for(int i=0; i<comps.length; i++)  comps[i].paddingZeros(maxIndex); 			
		}
		TraceView view	 = new TraceView(_frame, _iFold, comps, _traceDisplayToolBar.getState(), false, null, 1.0f);
		view.setLineWidth(1.0f);
		view.setLineColor(Color.GRAY);
		view.setLineStyle(TraceView.Line.SOLID);
		view.setName("SEG2", "All");
		getWorld().removeView(view.getCategory(), view.getName());
		getWorld().removeView("SEG2", "All");
		getWorld().addTraceView(0, 0, view);		
	}

	//http://www.javalobby.org/forums/thread.jspa?threadID=16052&tstart=0
	private static class FileTreeNode implements TreeNode {
		private File file;
		private File[] children;
		private TreeNode parent;
		private boolean isFileSystemRoot = false;

		public FileTreeNode(File file, boolean isFileSystemRoot, TreeNode parent) {
			this.file = file;
			this.isFileSystemRoot = isFileSystemRoot;
			this.parent = parent;
			this.children = this.file.listFiles();
			if (this.children == null)
				this.children = new File[0];
		}

		public FileTreeNode(File[] children) {
			this.file = null;
			this.parent = null;
			this.children = children;
		}

		public Enumeration<?> children() {
			final int elementCount = this.children.length;
			return new Enumeration<File>() {
				int count = 0;
				public boolean hasMoreElements() { return this.count < elementCount; }

				public File nextElement() {
					if (this.count < elementCount) {
						return FileTreeNode.this.children[this.count++];
					}
					throw new NoSuchElementException("Vector Enumeration");
				}
			};
		}

		public boolean getAllowsChildren() 	{ return true; }
		public int getChildCount() 			{ return this.children.length; }
		public TreeNode getParent() 		{ return this.parent; }
		public File getFile() 				{ return file; }


		public TreeNode getChildAt(int childIndex) {
			return new FileTreeNode(this.children[childIndex],
					this.parent == null, this);
		}

		public int getIndex(TreeNode node) {
			FileTreeNode ftn = (FileTreeNode) node;
			for (int i = 0; i < this.children.length; i++) {
				if (ftn.file.equals(this.children[i]))
					return i;
			}
			return -1;
		}

		public boolean isLeaf() { return (this.getChildCount() == 0); }
	}

	protected static FileSystemView fsv = FileSystemView.getFileSystemView();
	private static class FileTreeCellRenderer extends DefaultTreeCellRenderer {
		private Map<String, Icon> iconCache = new HashMap<String, Icon>();
		private Map<File, String> rootNameCache = new HashMap<File, String>();

		@Override
		public Component getTreeCellRendererComponent(JTree tree, Object value,
				boolean sel, boolean expanded, boolean leaf, int row,
				boolean hasFocus) {
			FileTreeNode ftn = (FileTreeNode) value;
			File file = ftn.file;
			String filename = "";
			if (file != null) {
				if (ftn.isFileSystemRoot) {
					// long start = System.currentTimeMillis();
					filename = this.rootNameCache.get(file);
					if (filename == null) {
						filename = fsv.getSystemDisplayName(file);
						this.rootNameCache.put(file, filename);
					}
					// long end = System.currentTimeMillis();
					// System.out.println(filename + ":" + (end - start));
				} else {
					filename = file.getName();
				}
			}
			JLabel result = (JLabel) super.getTreeCellRendererComponent(tree,
					filename, sel, expanded, leaf, row, hasFocus);
			//			if (file != null) {
			//				Icon icon = this.iconCache.get(filename);
			//				if (icon == null) {
			//					// System.out.println("Getting icon of " + filename);
			//					icon = fsv.getSystemIcon(file);
			//					this.iconCache.put(filename, icon);
			//				}
			//				result.setIcon(icon);
			//			}
			return result;
		}
	}


}

