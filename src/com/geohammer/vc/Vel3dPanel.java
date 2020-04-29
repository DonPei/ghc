package com.geohammer.vc;

import static edu.mines.jtk.ogl.Gl.GL_AMBIENT_AND_DIFFUSE;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.JMenuItem;

import com.geohammer.common.WellTrack;
import com.geohammer.common.d3.Common3DMouseEditingMode;
import com.geohammer.common.d3.Common3DPanel;
import com.geohammer.common.d3.SphereGroup;
import com.geohammer.core.acquisition.VCPair;
import com.geohammer.core.planarmodel.DipLayer1D;
import com.geohammer.core.planarmodel.Layer3D;
import com.geohammer.rt.RayPath;
import com.geohammer.rt.RayPaths;
import com.geohammer.common.d3.PlotWellTrack;

import edu.mines.jtk.awt.ColorMap;
import edu.mines.jtk.awt.ModeManager;
import edu.mines.jtk.sgl.AxesOrientation;
import edu.mines.jtk.sgl.BoundingBox;
import edu.mines.jtk.sgl.BoundingSphere;
import edu.mines.jtk.sgl.ColorState;
import edu.mines.jtk.sgl.LightModelState;
import edu.mines.jtk.sgl.LineGroup;
import edu.mines.jtk.sgl.LineState;
import edu.mines.jtk.sgl.MaterialState;
import edu.mines.jtk.sgl.OrbitView;
import edu.mines.jtk.sgl.OrbitViewMode;
import edu.mines.jtk.sgl.PointState;
import edu.mines.jtk.sgl.QuadGroup;
import edu.mines.jtk.sgl.StateSet;
import edu.mines.jtk.sgl.ViewCanvas;
import edu.mines.jtk.sgl.World;

public class Vel3dPanel extends Common3DPanel {
	
	private VcFrame 		_frame 			= null;
	//3D view
	public DipLayer1D 			_dlayer1D 	= null;
	public VCPair 				_vcPW 		= null;
	public Layer3D 				_layer3D 	= null;
	public WellTrack 			_wellTrack 	= null;	

	//public ArrayList<Color> _colorList 		= null;
	public ColorMap 		colorMap 		= null;
	
	public Vel3dPanel(VcFrame frame, DipLayer1D dlayer1D, Layer3D layer3D, VCPair vcPW, WellTrack wellTrack, ColorMap colorMap) {
		super(1, frame);
		_frame 		= frame;		
		_dlayer1D 	= dlayer1D;
		_vcPW 		= vcPW;
		_layer3D 	= layer3D;
		_wellTrack	= wellTrack;
		
		layer3D.setBoundingBox();
		_globalBox = new BoundingBox(layer3D.getX0(), layer3D.getY0(), layer3D.getZ0(),
				layer3D.getX1(), layer3D.getY1(), layer3D.getZ1());
		_world = new World();
		updateOpenGL();
		
		this.colorMap = colorMap;
		updateWorld();
	}
	
	public void setWellTrack(WellTrack wellTrack)  { _wellTrack = wellTrack; }
	public void updateLayer3D(Layer3D layer3D) {
		_layer3D 	= layer3D;

		layer3D.setBoundingBox();
		_globalBox = new BoundingBox(layer3D.getX0(), layer3D.getY0(), layer3D.getZ0(),
				layer3D.getX1(), layer3D.getY1(), layer3D.getZ1());
		getOrbitView().setWorldSphere(new BoundingSphere(_globalBox));
	}
	public BoundingBox getBoundingBox() { return _globalBox; }
	public void updateColorMap() { 
		double vmin = 1.0e10;
		double vmax = -vmin;
		for(int i=0; i<_layer3D.getNumOfBoundaries(); i++) {
			double v = getVel(_frame.getIVp(), i, _layer3D);
			//System.out.println(v+" "+" vp="+getRayTracer().getIVp());
			vmin = vmin<v ? vmin:v;
			vmax = vmax>v ? vmax:v;
		}
		double a = colorMap.getMinValue();
		vmin = vmin<a?vmin:a;
		a = colorMap.getMaxValue();
		vmax = vmax>a?vmax:a;
		colorMap.setValueRange(vmin, vmax);
	}
	public void updateWorld() { 
		removeAllViews();
		addCoordinates();
		add(_world, _layer3D, false);
		add(_world, _vcPW);
		if(_wellTrack!=null) add(_world, _wellTrack);
	}	
	public Color getLayerColor(int iLayer) { return colorMap.getColor(getVel(iLayer)); }
	public double getVel(int iLayer) { return getVel(_frame.getIVp(), iLayer, _layer3D); }
	public double getVel(int iVp, int iLayer, Layer3D layer3D) {
		if(iVp==1) return layer3D.getLayer(iLayer).getVp();
		else if(iVp==2) return layer3D.getLayer(iLayer).getVs();
		else return layer3D.getLayer(iLayer).getVs();
	}
	public void add(World world, Layer3D layer3D, boolean changeColorMap) {
		double v = 0.0;

		for(int i=0; i<layer3D.getNumOfBoundaries(); i++) {
			StateSet states = new StateSet();
			LightModelState lms = new LightModelState();
			lms.setTwoSide(true);
			states.add(lms);
			MaterialState ms = new MaterialState();
			ms.setColorMaterial(GL_AMBIENT_AND_DIFFUSE);
			ms.setSpecular(Color.white);
			ms.setShininess(100.0f);
			states.add(ms);

			ColorState cs = new ColorState();
			//cs.setColor(_colorList.get(i));
			v = getVel(_frame.getIVp(), i, layer3D);
			//System.out.println(v+" "+" vp="+getRayTracer().getIVp());
			cs.setColor(colorMap.getColor(v));
			//cs.setColor(Color.BLACK);
			states.add(cs);

			float[] xyz = layer3D.getLayer(i).makeQuads(1.0f);
			QuadGroup qg = new QuadGroup(true, xyz, null, "Horizon", layer3D.getLayer(i).getLayerName());
			qg.setStates(states);
			qg.setName("Horizon", layer3D.getLayer(i).getLayerName());
			world.addChild(qg);
		}
	}
	
	public void add(World world, VCPair vcPW) {
		if(vcPW==null) return;
		WellTrack shotLoc = new WellTrack(vcPW.getEN(), vcPW.getEE(), vcPW.getED());
		addGeometry(world, shotLoc, "Geometry", "Shots", Color.RED, 5);
		for(int i=0; i<vcPW.getNEvents(); i++) {
			WellTrack rcvrLoc = new WellTrack(vcPW.getRNArray(i), vcPW.getREArray(i), vcPW.getRDArray(i));
			addGeometry(world, rcvrLoc, "Geometry", "Receivers"+i, Color.GREEN, 5);
		}
	}
	public void addGeometry(World world, WellTrack shotLoc, String category, String name, Color color, int size) {
		if(shotLoc==null) return;

		PlotWellTrack plotWellTrack = new PlotWellTrack(shotLoc);
		SphereGroup pg1 = new SphereGroup(plotWellTrack.getXYZ());
		StateSet state1 = new StateSet();
		ColorState cs1 = new ColorState();
		cs1.setColor(color);
		state1.add(cs1);
		PointState ps1 = new PointState();
		ps1.setSize(size);
		ps1.setSmooth(true);
		state1.add(ps1);
		pg1.setStates(state1);
		pg1.setName(category, name);
		world.addChild(pg1); 
	}
	
	public void add(World world, WellTrack welltrack) {
		int n = welltrack.getNumOfPoints();
		float [] xyz = new float[3*n];

		int k = 0;
		for(int i=0; i<n; i++) {
			xyz[k++] = (float)welltrack.getX(i);
			xyz[k++] = (float)welltrack.getY(i);
			xyz[k++] = (float)welltrack.getZ(i);
		}

		LineGroup track = new LineGroup(xyz, 2.0f);

		StateSet state = new StateSet();
		LineState ls = new LineState();
		ls.setWidth(500.0f);
		ls.setSmooth(true);
		state.add(ls);

		ColorState cs1 = new ColorState();
		cs1.setColor(Color.BLACK);
		state.add(cs1);
		track.setStates(state);
		track.setName("Well Track ", welltrack.getWellName());
		world.addChild(track);  
	}
	
	public void add(World world, int iShot, RayPaths rayPaths) {
		int nTraces = rayPaths.getNumOfTraces();
		for(int i=0; i<nTraces; i++) {
			StateSet state = new StateSet();
			LineState ls = new LineState();
			ls.setWidth(500.0f);
			ls.setSmooth(true);
			state.add(ls);

			ColorState cs1 = new ColorState();
			cs1.setColor(Color.BLACK);
			state.add(cs1);
			LineGroup track = genLineGroup(rayPaths.getRayPath(i));
			track.setStates(state);
			//track.setName("Raypath"+(iShot+1), "Receiver"+(i+1));
			track.setName("Raypath", "S"+(iShot+1)+"_R"+(i+1));
			//boolean visible = isVisible("Raypath", "Shot"+iShot);
			//track.setVisible(visible);
			world.addChild(track);  
		}
	}
	public void removeRayPath() { 
		if(isRemovable(getWorld(), "Raypath")) { remove(getWorld(), "Raypath"); }
	}
	public void addRayPath(RayPaths rayPaths) {
		if(rayPaths==null) return;
		if(rayPaths.getNumOfTraces()<1) return;
		removeRayPath();
		
		int nShots = rayPaths.getNumOfEmsembles();
		//System.out.println("nShots="+nShots);
		for(int i=0; i<nShots; i++) {
			add(getWorld(), i, rayPaths.getEmsemble(i));
		}
	}

	public LineGroup genLineGroup(RayPath rayPath){
		double pN[] = rayPath.getN();
		double pE[] = rayPath.getE();
		double pD[] = rayPath.getD();

		float [] xyz = new float[3*pN.length];

		int k = 0;
		for(int i=0; i<pN.length; i++) {
			xyz[k++] = (float)(pN[i]);
			xyz[k++] = (float)(pE[i]);
			xyz[k++] = (float)(pD[i]);
		}

		return new LineGroup(xyz, 0.75f);
	}
	
	public void rotateToAzimuth(double azimuth) { _ovm.rotateToAzimuth(azimuth, _canvas, _view); }
	private void updateOpenGL() {
		OrbitView view = (_world!=null)?new OrbitView(_world):new OrbitView();
		view.setAxesOrientation(AxesOrientation.XRIGHT_YOUT_ZDOWN);
		view.setWorldSphere(new BoundingSphere(_globalBox));

		ViewCanvas canvas = new ViewCanvas(view);
		canvas.setView(view);
		canvas.setBackground(Color.white);

		ModeManager mm = new ModeManager();
		mm.add(canvas);
		Common3DMouseEditingMode mouseEditingMode = new Common3DMouseEditingMode(mm);
		mouseEditingMode.setCommon3DPanel(this);
		mouseEditingMode.setActive(true);
		mouseEditingMode.setJComponent(genJMenuItem());

		OrbitViewMode ovm = new OrbitViewMode(mm);
		ovm.setOriginalScale(view.getScale());
		//_sdm = new SelectDragPopMode(this, _mm);

		ovm.setActive(true);
		//ovm.rotateToAzimuth(-90, _canvas, _view);

		setParameters(canvas, view, ovm, _world, _globalBox);
		add(canvas, BorderLayout.CENTER);
	}
	
	private JMenuItem[] genJMenuItem() {
		JMenuItem jMenuItem1 = new JMenuItem("Histogram");		
		jMenuItem1.addActionListener(new ActionListener() 	{
			public void actionPerformed(ActionEvent e)   {
				String title = "Histogram";
				//HistogramDialog dialog = new HistogramDialog(_frame, title, false, _world, 0);
				//dialog.showDialog();
			}
		});
				
		return new JMenuItem[] { jMenuItem1 };
	}
	
	
}
