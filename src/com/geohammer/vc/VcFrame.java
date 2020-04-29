package com.geohammer.vc;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;

import com.geohammer.common.CommonPanel;
import com.geohammer.common.LayerPreferences;
import com.geohammer.common.ProjectVc;
import com.geohammer.common.WellTrack;
import com.geohammer.component.FileMenuListener;
import com.geohammer.component.StatusBar;
import com.geohammer.core.acquisition.VCPair;
import com.geohammer.core.planarmodel.DipLayer1D;
import com.geohammer.rt.RayPaths;
import com.geohammer.vc.dialog.ProjectDialog;

import edu.mines.jtk.awt.ColorMap;
import edu.mines.jtk.mosaic.PointsView;
import edu.mines.jtk.mosaic.PointsView.Line;
import edu.mines.jtk.mosaic.PointsView.Mark;
import edu.mines.jtk.util.ArrayMath;


public class VcFrame extends VcFrameBase implements FileMenuListener, KeyListener {
	private static final long serialVersionUID 		= 1L;

	public VcFrame(int exitCode, String title) {
		super(title);
		this.exitCode 	= exitCode;

		preferences = new LayerPreferences("vc");
		preferences.readPreferences();

		project = new ProjectVc(System.getProperty("user.dir")+File.separator+"untitled.prj2d");		

		vcMenuBar = new VcMenuBar(this);
		ArrayList<String> list = preferences.getRecentFileList();
		if( list != null ) {
			for( int i = list.size()-1; i >= 0; i-- )  vcMenuBar.addRecentFile(list.get(i)); 
		}
		for(int i=1; i<vcMenuBar.getMenuCount(); i++) {
			JComponent component = (JComponent)vcMenuBar.getComponent(i);
			component.setEnabled(false);
		}		
		setJMenuBar(vcMenuBar);

		setLayout(new BorderLayout());
		StatusBar statusBar = new StatusBar(); 
		statusBar.setZoneBorder(BorderFactory.createLineBorder(Color.GRAY)); 

		statusBar.setZones( new String[] { "first_zone", "second_zone", "remaining_zones" },     
				new Component[] { new JLabel("first"), new JLabel("second"),   new JLabel("remaining")     },     
				new String[] {"40%", "40%", "*"} );
		JLabel trackingLabel  = (JLabel) statusBar.getZone("remaining_zones");
		
		card  		= new VcCardPanel();
		vel2dPanel = new Vel2dPanel(1, 1, CommonPanel.Orientation.X1DOWN_X2RIGHT,  CommonPanel.AxesPlacement.LEFT_TOP, statusBar);
		vel2dPanel .init();
		vel2dPanel .setBorder(new EmptyBorder(1, 1, 1, 1));
		vel2dPanel .setBackground(Color.white);
		vel2dPanel .setHLabel("Offset");
		vel2dPanel .setVLabel("TVD");	
		vel2dPanel .setFrame(this);
		setCommonPanel(vel2dPanel);	
		
		rayPath = new RayPaths[3];

		card.addCard(vel2dPanel, "2D");
		card.setCurrentName("2D");
		card.setCurrent(vel2dPanel);
		
		//Create the HTML viewing pane.
		JEditorPane htmlPane = new JEditorPane();
		htmlPane.setText("Click SEG2 file to display");
		htmlPane.setEditable(false);
		CommonPanel htmlView = new CommonPanel();
		htmlView.add(htmlPane);
		card.addCard(htmlView, "Help");

		add(card, BorderLayout.CENTER);

		setDefaultCloseOperation( DO_NOTHING_ON_CLOSE );
		addWindowListener( new doYouWantSave() );
		addKeyListener(this);
	}
	public void keyTyped(KeyEvent e) { }
	public void keyPressed(KeyEvent e) {
		int k 			= e.getKeyCode();
		if(e.isControlDown()&&!e.isAltDown()&&!e.isShiftDown()){
			if(k==KeyEvent.VK_1) { 
				ArrayList<String> list = vcMenuBar.getRecentFileList();
				if(list!=null && list.size()>=1) openProject(list.get(0));
			}
			if(k==KeyEvent.VK_2) { 
				ArrayList<String> list = vcMenuBar.getRecentFileList();
				if(list!=null && list.size()>=2) openProject(list.get(1));
			}
			if(k==KeyEvent.VK_3) { 
				ArrayList<String> list = vcMenuBar.getRecentFileList();
				if(list!=null && list.size()>=3) openProject(list.get(2));
			}
		}
	}
	public void keyReleased(KeyEvent e) { }	
	public void savePreferences() {
		ArrayList<String> list = vcMenuBar.getRecentFileList();
		if(list.size()>0) {
			preferences.setRecentFileList(list);
			preferences.writePreferences();
		}
	}

	public void createProject(String projectFileName, int iId, int iUnit, int velFileType, String velFileName, 
			int tTimeFileType, String tTimeFileName,  
			int wellTrackFileType, String wellTrackFileName)	{
		project = new ProjectVc(projectFileName, iId, iUnit, velFileType, velFileName, 
				tTimeFileType, tTimeFileName, wellTrackFileType, wellTrackFileName);

		if(projectFileName!=null) {
			for(int i=1; i<vcMenuBar.getMenuCount(); i++) {
				JComponent component = (JComponent)vcMenuBar.getComponent(i);
				component.setEnabled(true);
			}
			vcMenuBar.getJMenuItemOpenFile().setEnabled(true);
			//vcMenuBar.setEditMenu();

			if(!projectFileName.contains("untitled")) vcMenuBar.addRecentFile(project.getProjectFileName());
			setTitle(projectFileName);

//			String unit = "";
//			if(project.getIUnit()==0) 		unit = " (ft)"; 
//			else if(project.getIUnit()==1) 	unit = " (m)"; 
//			else if(project.getIUnit()==2) 	unit = " (km)"; 			
//			world.setHLabel("Offset"+unit);
//			world.setVLabel("TVD"+unit);
		}
	}

	public boolean openProject(String projectFileName)	{
		ProjectVc project = new ProjectVc(projectFileName);

		ProjectDialog.genProject(projectFileName, this, project.getIId(), project.getIUnit(), project.getVelFileType(), 
				project.getVelFileName(), project.getTTimeFileType(), project.getTTimeFileName(), 
				project.getWellTrackFileType(), project.getWellTrackFileName());

		vcMenuBar.addRecentFile(projectFileName);
		preferences.setDataDirectory(projectFileName);		
		savePreferences();
		setTitle(projectFileName);

		return true;
	}

	public boolean saveProject(String selectedFileName)	{
		project.setProjectName(selectedFileName);
		project.write(selectedFileName);
		vcMenuBar.addRecentFile(selectedFileName);
		preferences.setDataDirectory(selectedFileName);		
		savePreferences();
		setTitle(selectedFileName);	
		fadeOut();	
		return true;
	}

	public void updateWorld() { updateWorld(true); }
	public void updateWorld(boolean globalRemove) {
		if(getProject()==null) return;
		if(globalRemove) getVel2dPanel().removeAllViews(0, 0);
		if(colorMap==null) {
			double [] vp = getProject().getDipLayer1D().getLayerProperty(1);
			double [] vs = getProject().getDipLayer1D().getLayerProperty(2);
			double vmin = ArrayMath.min(new double [][] {vp, vs});
			double vmax = ArrayMath.max(new double [][] {vp, vs});
			double d = 0.05*(vmax - vmin);
			colorMap = new ColorMap(vmin-d, vmax+d, ColorMap.JET);
		}
		if(getProject().is2D()) {
			//vc2DPanel.updateWorld(getProject().getDipLayer1D(), getProject().getVCPair(), getProject().getWellTrack());
			updateWorld2D();
		} else {		
			updateWorld3D();
		}
	}

	private void updateWorld2D() {
//		GridView gv = new GridView(Color.LIGHT_GRAY);
//		gv.setName("Grid", "Major");
//		gv.setVisible(getWorld().getVisible(gv.getCategory(), gv.getName()));
//		getWorld().removeView(gv.getCategory(), gv.getName());
//		getWorld().addGridView(0, 0, gv);

		//System.out.println("x000="+getProject().getLayer().getX0()+" x1="+getProject().getLayer().getX1()+
		//	" z0="+getProject().getLayer().getZ0()+" z1="+getProject().getLayer().getZ1());
		Vel2dView vv	 = new Vel2dView(this, getProject().getLayer2D(), iVp, colorMap);
		vv.setName("Layer", "All Layers");
		vv.setVisible(getVel2dPanel().getVisible(vv.getCategory(), vv.getName()));
		getVel2dPanel().removeView(vv.getCategory(), vv.getName());
		getVel2dPanel().addVel2dView(0, 0, vv);
		String label = "S-wave Velocity";
		if(iVp==1) label = "P-wave Velocity";
		getVel2dPanel().removeColorBar();
		getVel2dPanel().addColorBar(vv, label);
		
		VCPair vCPair = getProject().getVCPair2D();
		if(vCPair!=null) {
			for(int i=0; i<vCPair.getNEvents(); i++) {
				PointsView pv	 = new PointsView(new float[]{(float)vCPair.getED(i)}, new float[]{(float)vCPair.getEN(i)});
				pv.setStyle("kx");
				pv.setMarkSize(10.0f);
				pv.setName("Geometry", "Shots"+i);
				pv.setVisible(getVel2dPanel().getVisible(pv.getCategory(), pv.getName()));
				getVel2dPanel().removeView(pv.getCategory(), pv.getName());
				getVel2dPanel().addPointsView(0, 0, pv);
			}
			for(int j=0; j<vCPair.getFlag(0); j++) {
				PointsView pvr	 = new PointsView(new float[]{(float)vCPair.getRD(j)}, new float[]{(float)vCPair.getRN(j)});
				pvr.setStyle("ks");
				pvr.setMarkSize(10.0f);
				pvr.setName("Geometry", "Rcvr"+j);
				pvr.setVisible(getVel2dPanel().getVisible(pvr.getCategory(), pvr.getName()));
				getVel2dPanel().removeView(pvr.getCategory(), pvr.getName());
				getVel2dPanel().addPointsView(0, 0, pvr);
			}
		}
		
		WellTrack welltrack = getProject().getWellTrack(); 
		if(getProject().getWellTrack()!=null) {
			int n = welltrack.getNumOfPoints();
			float [] N = new float[n];
			float [] E = new float[n];
			float [] D = new float[n];

			for(int i=0; i<n; i++) {
				N[i] = (float)welltrack.getX(i);
				E[i] = (float)welltrack.getY(i);
				D[i] = (float)welltrack.getZ(i);
			}
			
			PointsView pv	 = new PointsView(N, D);
			pv.setStyle("k-");
			pv.setLineWidth(2.0f);
			pv.setName("Well Track ", welltrack.getWellName());
			pv.setVisible(getVel2dPanel().getVisible(pv.getCategory(), pv.getName()));
			getVel2dPanel().removeView(pv.getCategory(), pv.getName());
			getVel2dPanel().addPointsView(0, 0, pv);
		}
	}
	private void updateWorld3D() {
		if(vel3dPanel==null) {
			vel3dPanel = new Vel3dPanel(this, getProject().getDipLayer1D(),
					getProject().getLayer3D(), getProject().getVCPair(), getProject().getWellTrack(), colorMap);
			card.addCard(vel3dPanel, "3D");
			card.showCard("3D");
		} else {
			getVel3dPanel().updateColorMap();
			getVel3dPanel().updateWorld();
		}
	}

}

