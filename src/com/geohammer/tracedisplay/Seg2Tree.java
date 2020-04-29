package org.ucdm.tracedisplay;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTree;
import javax.swing.UIManager;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeSelectionModel;

import org.ucdm.core.MSEvent;
import org.ucdm.core.SeisPTSrcMech;
import org.ucdm.core.SeismicTraceComponent;
import org.ucdm.core.SeismicTraceSensor;
import org.ucdm.core.SeismicTraceWell;
import org.ucdm.seg2.SEG2;
import org.ucdm.component.MouseZoomMode;
import org.ucdm.mti.Seg2View;
import org.ucdm.mti.Seg2View.Line;

import edu.mines.jtk.awt.ModeManager;

public class Seg2Tree extends JPanel implements TreeSelectionListener {
	JFrame _frame = null;
    private JSplitPane 		_splitPane 		= null;
    private JTree 			_tree 			= null;
    //Optionally play with line styles.  Possible values are
    //"Angled" (the default), "Horizontal", and "None".
    private static boolean playWithLineStyle = false;
    private static String lineStyle = "Horizontal";
    
    //Optionally set the look and feel.
    private static boolean useSystemLookAndFeel = false;
    private SeisPTSrcMech 	_srcMech 		= null;
	private MSEvent [] 		_msEvents 		= null;
	String [] 				_fileNames 		= null;
	String [] 				_fileFullNames 	= null;
	
	private ModeManager 		_modeManager; 			// mode manager for this plot frame
	private MouseZoomMode 		_tileZoomMode; 			// tile zoom mode
	
    public Seg2Tree(JFrame frame, String srcFileName, String dataPath) {
        super(new GridLayout(1,0));
        _frame = frame;
        if(srcFileName==null) {}
        else if(srcFileName.contains(".")) {
        	_srcMech = new SeisPTSrcMech(false, srcFileName, dataPath);
        	_msEvents = _srcMech.getMSEvents();
        }
    	if(_srcMech==null) {
    		_fileNames = listFiles(1, dataPath); 
    		if(_fileNames==null) return;
    		_fileFullNames = listFiles(2, dataPath);
    	} else {
    		_fileNames = new String[_msEvents.length];
    		for(int i=0; i<_fileNames.length; i++) {
    			_fileNames[i] = _msEvents[i].getLabel();
    		}
    	}
        //Create the nodes.
        DefaultMutableTreeNode left = new DefaultMutableTreeNode(dataPath);
        createNodes(left, _fileNames);

        _tree = new JTree(left);
        _tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        _tree.addTreeSelectionListener(this);

        if (playWithLineStyle) {
            System.out.println("line style = " + lineStyle);
            _tree.putClientProperty("JTree.lineStyle", lineStyle);
        }

        //Create the scroll pane and add the tree to it. 
        JScrollPane treeView = new JScrollPane(_tree);

        //Create the HTML viewing pane.
        JEditorPane htmlPane = new JEditorPane();
        htmlPane.setText("Click SEG2 file to display");
        htmlPane.setEditable(false);
        //initHelp();
        JScrollPane htmlView = new JScrollPane(htmlPane);

        //Add the scroll panes to a split pane.
        _splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        _splitPane.setLeftComponent(treeView);
        _splitPane.setRightComponent(htmlView);

//        Dimension minimumSize = new Dimension(1200, 800);
//        htmlView.setMinimumSize(minimumSize);
//        treeView.setMinimumSize(minimumSize);
        _splitPane.setDividerLocation(0.2); 
        _splitPane.setPreferredSize(new Dimension(1200, 800));

        //Add the split pane to this panel.
        add(_splitPane);
    }
    
    private String [] listFiles(int id, String path) {
    	ArrayList<String> fileList = null;
    	String files = null;
    	File folder = new File(path);
    	File[] listOfFiles = folder.listFiles(); 

    	for (int i = 0; i < listOfFiles.length; i++) {
    		if (listOfFiles[i].isFile()) {
    			if(id==1) {
    				files = listOfFiles[i].getName();
    			} else {
    				try {
    					files = listOfFiles[i].getCanonicalPath();
    				} catch (IOException e) { }
    			}
    			
    			if (files.endsWith(".dat") || files.endsWith(".DAT")) {
    				if(fileList==null) fileList = new ArrayList<String>();
    				fileList.add(files);
    			}
    		}
    	}
    	if(fileList==null) return null;
    	if(fileList.size()==0) return null;
    	String [] fileNames = new String[fileList.size()];
    	for(int i=0; i<fileNames.length; i++) fileNames[i] = fileList.get(i);
    	
    	return fileNames;
    }
    
    /** Required by TreeSelectionListener interface. */
    public void valueChanged(TreeSelectionEvent e) {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode)_tree.getLastSelectedPathComponent();
        if (node == null) return;

        Object nodeInfo = node.getUserObject();
        if (node.isLeaf()) {
        	//System.out.print((String)nodeInfo + " " + getCurveIndex((String)nodeInfo)+"\n");
        	updateCurve(getCurveIndex((String)nodeInfo));
        } 
    }
    
    private int getCurveIndex(String name) {
    	for(int i=0; i<_fileNames.length; i++) {
    		if(name.equalsIgnoreCase(_fileNames[i])) return i;
    	}
    	return 0;
    }
    
    private void updateCurve(int index) {
//    	LayerPanel world = new LayerPanel(1, 1, LayerPanel.Orientation.X1DOWN_X2RIGHT,  LayerPanel.AxesPlacement.LEFT_TOP);
//		world.setVLabel("Channel No.");
//		world.setHLabel(_fileNames[index]);
//		SeismicTraceComponent [] comps = null;
//		int k = 0;
//
//		if(_srcMech!=null) {
//			MSEvent msEvent = _msEvents[index];
//			for(int i=0; i<msEvent.getNumOfSTWells(); i++) {
//				SeismicTraceWell	stWell 	= msEvent.getSTWells(i);
//				for(int j=0; j<stWell.getNumOfSensors(); j++) {
//					SeismicTraceSensor stSensor = stWell.getSensor(j);
//					k += stSensor.getNumOfComps();
//				}
//			}
//
//			//		TileAxis  lAxis = world.getMosaic().getTileAxisLeft(0);
//			//		AxisTics lAxisTics = lAxis.getAxisTics();
//			//		AxisTics(double x1, double x2, int ntic);
//			//		world.setVLimits(0, 1, k);
//
//			comps = new SeismicTraceComponent[k];
//			//System.out.println("k="+k);
//			k = 0;
//			for(int i=0; i<msEvent.getNumOfSTWells(); i++) {
//				SeismicTraceWell	stWell 	= msEvent.getSTWells(i);
//				for(int j=0; j<stWell.getNumOfSensors(); j++) {
//					SeismicTraceSensor stSensor = stWell.getSensor(j);
//					for(int jj=0; jj<stSensor.getNumOfComps(); jj++) {
//						comps[k] = stSensor.getComp(jj); 
//						k++; 
//					}
//				}
//			}
//		} else {
//			SEG2 seg2 = new SEG2(_fileFullNames[index]);
//			k = seg2.getNumOfTraces();
//			comps = new SeismicTraceComponent[k];
//			//System.out.println("k="+k);
//			for(int i=0; i<k; i++) {
//				comps[i] = new SeismicTraceComponent(1.0, seg2.getTrace(i));
//			}
//		}
//		
//		_modeManager = new ModeManager();
//		world.getMosaic().setModeManager(_modeManager);
//		_tileZoomMode = new MouseZoomMode(_modeManager);
//		_tileZoomMode.setActive(true);
//		
////		Seg2View vp	 = new Seg2View(null, comps, null);
////		vp.setLineWidth(1.0f);
////		vp.setLineColor(Color.GRAY);
////		vp.setLineStyle(Seg2View.Line.SOLID);
//////		vp.setMarkColor(Color.RED);
//////		vp.setMarkStyle(CurveView.Mark.HOLLOW_CIRCLE);
//////		vp.setMarkSize(6.0f);
////		vp.setName("SEG2", "All");
////		world.removeView(vp.getCategory(), vp.getName());
////		world.addSeg2View(0, 0, vp);
//		
//		//updateCurve(_curves);
//    	_splitPane.setRightComponent(world);
//    	_splitPane.setDividerLocation(0.2);
    }
    
    private void createNodes(DefaultMutableTreeNode left, String [] fileNames) {
    	DefaultMutableTreeNode curveNode = null;
    	for(int i=0; i<fileNames.length; i++) {
    		curveNode = new DefaultMutableTreeNode(fileNames[i]);
    		left.add(curveNode);
    	}
    }
    
    
    /**
     * Create the GUI and show it.  For thread safety,
     * this method should be invoked from the
     * event-dispatching thread.
     */
    private static void createAndShowGUI() {
        if (useSystemLookAndFeel) {
            try {
                UIManager.setLookAndFeel(
                    UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                System.err.println("Couldn't use system look and feel.");
            }
        }

        //Make sure we have nice window decorations.
        JFrame.setDefaultLookAndFeelDecorated(true);

        String dataPath = "C:\\PINN_DATA\\COP\\stage5kc1";
		String srcFileName = "C:\\PINN_DATA\\COP\\stage5kc1\\SrcMech\\SrcMech.txt";
		SeisPTSrcMech srcMech = new SeisPTSrcMech(false, srcFileName, dataPath);
        
        //Create and set up the window.
        JFrame frame = new JFrame("SEG2 Seismogram Viewer");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        //Create and set up the content pane.
        Seg2Tree newContentPane = new Seg2Tree(frame, srcFileName, dataPath);
        newContentPane.setOpaque(true); //content panes must be opaque
        frame.setContentPane(newContentPane);

        //Display the window.
        frame.pack();
        frame.setVisible(true);
    }

    public static void main(String[] args) {
        //Schedule a job for the event-dispatching thread:
        //creating and showing this application's GUI.
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI();
            }
        });
    }
}


