package com.geohammer.vc;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JTextField;

import com.geohammer.common.CommonMouseEditingMode;
import com.geohammer.common.CommonPanel;
import com.geohammer.common.WellTrack;
import com.geohammer.common.CommonPanel.Orientation;
import com.geohammer.core.acquisition.VCPair;
import com.geohammer.core.planarmodel.DipLayer1D;
import com.geohammer.core.planarmodel.Layer2D;
import com.geohammer.core.planarmodel.SingleFlatLayer;
import com.geohammer.rt.RayPaths;

import edu.mines.jtk.awt.ColorMap;
import edu.mines.jtk.mosaic.PointsView;


public class Vc2DPanel extends CommonPanel {
	private VcFrame 			frame 				= null;	

	public Vc2DPanel(VcFrame frame) {
		super(1,1,CommonPanel.Orientation.X1DOWN_X2RIGHT, CommonPanel.AxesPlacement.LEFT_TOP, true, null);
		setBackground(Color.white);
		setFrame(frame);
		this.frame = frame;

		//setMinimumSize(new Dimension(800, 100));     
		//setPreferredSize(new Dimension(800, 250));		
		setVLabel(0, "Down (ft)");
		setHLabel(0, "Easting (ft)");
		init();
	}

	public void init() {		
		setEnableTracking(false);
		setEnableEditing(true);
		setEnableZoom(true);

		addModeManager();
		CommonMouseEditingMode mouseEditingMode = getMouseEditingMode();
		mouseEditingMode.setShowAutoAxisLimit(true);
		mouseEditingMode.setJComponent(genJMenuItem());

		//		Mosaic mosaic = getMosaic();		
		//		ModeManager modeManager = mosaic.getModeManager();
		//		//setEnableDrawLine(true);
		//		//setEnableDrawRec(true);
		//		setEnableDrawZone(true);
		//		MouseTrackInfoMode mouseTrackInfoMode = new MouseTrackInfoMode(modeManager);		
		//		mouseTrackInfoMode.setActive(true);		
	}
	private JComponent[] genJMenuItem() {	
		JMenuItem jMenuItem0 = new JMenuItem("Plot X-Section");		
		jMenuItem0.addActionListener(new ActionListener() 	{
			public void actionPerformed(ActionEvent e)   {
				//SectionDialog dialog = new SectionDialog(_frame, "Section", false);
				//dialog.showDialog();
			}
		});
		JMenuItem jMenuItem1 = new JMenuItem("Plot X-Section");		
		jMenuItem0.addActionListener(new ActionListener() 	{
			public void actionPerformed(ActionEvent e)   {
				//SectionDialog dialog = new SectionDialog(_frame, "Section", false);
				//dialog.showDialog();
			}
		});
		return new JComponent[] {jMenuItem0, jMenuItem1};
	}
	
	public Vel2dView addVel2dView(int irow, int icol, Vel2dView pv) {
		if (_orientation==Orientation.X1RIGHT_X2UP) {
			pv.setOrientation(Vel2dView.Orientation.X1RIGHT_X2UP);
		} else if (_orientation==Orientation.X1DOWN_X2RIGHT) {
			pv.setOrientation(Vel2dView.Orientation.X1DOWN_X2RIGHT);
		}
		addTiledView(irow,icol,pv);
		return pv;
	}
	public void removeRayPath() {
		removeViews(0, 0, "Ray Path"); 
	}
	//public void setRayPath(int index, RayPaths rayPaths) { rayPath[index] = rayPaths; }
	public void addRayPath(RayPaths rayPaths) {
		if(rayPaths==null) return;
		if(rayPaths.getNumOfTraces()<1) return;
		//rayPaths = rayPaths;
		//System.out.println(rayPaths.toString());
		removeViews("Ray Path");
		double x1min = frame.getProject().getLayer2D().getZ0();
		double x2min = frame.getProject().getLayer2D().getX0();
		double x1max = frame.getProject().getLayer2D().getZ1();
		double x2max = frame.getProject().getLayer2D().getX1();
		//System.out.println("x1min="+x1min+" x2min="+x2min+" x1max="+x1max+" x2max="+x2max);
		int nShots = rayPaths.getNumOfEmsembles();
		//System.out.println("nShots="+nShots);
		for(int i=0; i<nShots; i++) {
			RaypathView rv	 = new RaypathView(rayPaths.getEmsemble(i), x1min, x2min, x1max, x2max);
			rv.setName("Ray Path", "Path "+i);
			//rv.setLineColor(Color.WHITE);
			float width = rv.getLineWidth()*0.5f;
			rv.setLineWidth(width);
			//rv.setVisible(getWorld().getVisible(rv.getCategory(), rv.getName()));
			//getWorld().removeView(rv.getCategory(), rv.getName());
			addRaypathView(0, 0, rv);
		}
	}
	
	public RaypathView addRaypathView(int irow, int icol, RaypathView rv) {
		if (_orientation==Orientation.X1RIGHT_X2UP) {
			rv.setOrientation(RaypathView.Orientation.X1RIGHT_X2UP);
		} else if (_orientation==Orientation.X1DOWN_X2RIGHT) {
			rv.setOrientation(RaypathView.Orientation.X1DOWN_X2RIGHT);
		}
		addTiledView(irow,icol,rv);
		return rv;
	}
	public void updateWorld(DipLayer1D dipLayer1D, VCPair vCPair, WellTrack wellTrack) {
		removeAllViews();
		
//		GridView gv = new GridView(Color.LIGHT_GRAY);
//		gv.setName("Grid", "Major");
//		gv.setVisible(getWorld().getVisible(gv.getCategory(), gv.getName()));
//		getWorld().removeView(gv.getCategory(), gv.getName());
//		getWorld().addGridView(0, 0, gv);

		//System.out.println("x000="+getProject().getLayer().getX0()+" x1="+getProject().getLayer().getX1()+
		//	" z0="+getProject().getLayer().getZ0()+" z1="+getProject().getLayer().getZ1());
//		Vel2dView vv	 = new Vel2dView(this, getProject().getLayer2D(), iVp);
//		vv.setColorModel(ColorMap.HUE_BLUE_TO_RED);
//		vv.setName("Layer", "All Layers");
//		vv.setVisible(getWorld().getVisible(vv.getCategory(), vv.getName()));
//		getWorld().removeView(vv.getCategory(), vv.getName());
//		getWorld().addVel2dView(0, 0, vv);
//		String label = "S-wave Velocity";
//		if(iVp==1) label = "P-wave Velocity";
//		getWorld().removeColorBar();
//		getWorld().addColorBar(vv, label);

		if(dipLayer1D!=null) {
			int iVp = 1;
			Vel2dView vv	 = new Vel2dView(frame, frame.getProject().getLayer2D(), 
					iVp, frame.getColorMap());
			vv.setName("Layer", "All Layers");
			removeView(vv.getCategory(), vv.getName());
			addVel2dView(0, 0, vv);
			String label = "S-wave Velocity";
			if(iVp==1) label = "P-wave Velocity";
			removeColorBar();
			addColorBar(vv, label);
			
//			ArrayList<SingleFlatLayer> layerList = dipLayer1D.getLayer();
//
//			float x0 = (float)layerList.get(0).getX0();
//			float x1 = (float)layerList.get(0).getX1();
//			float y0 = (float)layerList.get(0).getY0();
//			float y1 = (float)layerList.get(0).getY1();
//			
//			for(int i=0; i<layerList.size(); i++) {
//				SingleFlatLayer layer = layerList.get(i);
//				float topDepth = (float)layer.getTopDepth();			
//				PointsView pv	 = new PointsView(new float[]{topDepth, topDepth}, new float[]{y0, y1});
//				pv.setStyle("-");
//				pv.setLineColor(Color.RED);
//				pv.setName("VelModel", layer.getLayerName());
//				pv.setVisible(getVisible(pv.getCategory(), pv.getName()));
//				removeView(pv.getCategory(), pv.getName());
//				addPointsView(0, 0, pv);
//			}
		}
		if(vCPair!=null) {
			for(int i=0; i<vCPair.getNEvents(); i++) {
				PointsView pv	 = new PointsView(new float[]{(float)vCPair.getED(i)}, new float[]{(float)vCPair.getEN(i)});
				pv.setStyle("kx");
				pv.setMarkSize(10.0f);
				pv.setName("Geometry", "Shots"+i);
				pv.setVisible(getVisible(pv.getCategory(), pv.getName()));
				removeView(pv.getCategory(), pv.getName());
				addPointsView(0, 0, pv);
			}
			for(int j=0; j<vCPair.getFlag(0); j++) {
				PointsView pvr	 = new PointsView(new float[]{(float)vCPair.getRD(j)}, new float[]{(float)vCPair.getRN(j)});
				pvr.setStyle("ks");
				pvr.setMarkSize(5.0f);
				pvr.setName("Geometry", "Rcvr"+j);
				pvr.setVisible(getVisible(pvr.getCategory(), pvr.getName()));
				removeView(pvr.getCategory(), pvr.getName());
				addPointsView(0, 0, pvr);
			}
		}
		
		
		
	}

}
