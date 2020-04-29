package com.geohammer.common.d3;

import java.awt.BorderLayout;
import java.awt.Color;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import com.geohammer.common.CommonFrame;
import com.geohammer.common.CommonPanel;
import com.geohammer.common.WellTrack;
import com.geohammer.core.acquisition.VCPair;

import edu.mines.jtk.sgl.BoundingBox;
import edu.mines.jtk.sgl.BoundingSphere;
import edu.mines.jtk.sgl.ColorState;
import edu.mines.jtk.sgl.Group;
import edu.mines.jtk.sgl.LineGroup;
import edu.mines.jtk.sgl.LineState;
import edu.mines.jtk.sgl.Node;
import edu.mines.jtk.sgl.OrbitView;
import edu.mines.jtk.sgl.OrbitViewMode;
import edu.mines.jtk.sgl.Point3;
import edu.mines.jtk.sgl.PointState;
import edu.mines.jtk.sgl.StateSet;
import edu.mines.jtk.sgl.ViewCanvas;
import edu.mines.jtk.sgl.World;

@SuppressWarnings("serial")
public class Common3DPanel  extends CommonPanel {
	public CommonFrame 		 _frame 		= null;

	public boolean 	[]		_showGridFace 	= null;
	public boolean 	[]		_showFaceFace 	= null;
	//3D view
	public OrbitView 		_view 			= null;
	public ViewCanvas 		_canvas			= null;

	public World 			_world 			= null;
	public BoundingBox 		_globalBox		= null;
	public OrbitViewMode 	_ovm			= null;

	public int 				_iType 			= 1;
	
	public Common3DPanel(int iType, CommonFrame frame) {
		_iType 		= iType;
		_frame 		= frame;
		setLayout(new BorderLayout(2, 2));
	}
	
	public Common3DPanel(int iType, CommonFrame frame, ViewCanvas canvas, OrbitView view, OrbitViewMode ovm, 
			World world, BoundingBox globalBox) {
		_iType 		= iType;
		_frame 		= frame;
		_canvas 	= canvas;
		_view 		= view;
		_ovm 		= ovm;
		_world 		= world;
		_globalBox 	= globalBox;
		setLayout(new BorderLayout(2, 2));
		add(canvas, BorderLayout.CENTER);
	}

	public void setParameters(ViewCanvas canvas, OrbitView view, OrbitViewMode ovm, World world, BoundingBox globalBox) {
		_canvas 	= canvas;
		_view 		= view;
		_ovm 		= ovm;
		_world 		= world;
		_globalBox 	= globalBox;
	}
	
	public CommonFrame getFrame() 						{ return _frame; }
	public World getWorld() 							{ return _world; }	
	public OrbitViewMode getObitViewMode() 				{ return _ovm; }
	public OrbitView getOrbitView() 					{ return _view; }
	public ViewCanvas getViewCanvas() 					{ return _canvas; } 
	public BoundingBox getGlobalBoundingSphere() 		{ return _globalBox; }
	
	public BoundingBox getBoundingBox() 				{ return _globalBox; }
	public Color getBackgroundColor() 					{ return _canvas.getBackground(); }

	public void setBoundingBox(BoundingBox globalBox) 	{ _globalBox = globalBox; }
	public void setBackgroundColor(Color c) 			{ _canvas.setBackground(c); }

	public void resetWorldSphere() {
		setWorldSphere(_globalBox);
		repaint();
	}
	public void setWorldSphere(double percentage) {
		Point3 p0 = _globalBox.getCenter();
		double r = _globalBox.getRadius()*percentage;
		_view.setWorldSphere(new BoundingSphere(p0, r));
		repaint();
	}
	public void setWorldSphere(BoundingBox globalBox) {
		_view.setWorldSphere(new BoundingSphere(globalBox));
	}

	public void refresh() { _view.repaint(); }

	public void removeAllViews() {
		String [] categories = getWorldUniqueCategory();
		for(int i=0; i<categories.length; i++) {
			remove(getWorld(), categories[i]);
		}
	}
	public void remove(World world, String category, String name) {
		ArrayList<Node> list = null;
		String fullName = (category+"-"+name).trim();
		for(int i=0; i<world.countChildren(); i++) {
			Group element = world.getChildren(i);
			if(element.getFullName().equalsIgnoreCase(fullName)) {
				Node child = (Node)element;
				if(list==null) list = new ArrayList<Node>();
				list.add(child);
				//world.removeChild(child);
			}
		}
		if(list!=null) {
			for(int i=0; i<list.size(); i++) { world.removeChild(list.get(i)); }
		}
	}
	public boolean isRemovable(World world, String category) {
		if(world==null) return false;
		String categoryName = category.trim();
		if(categoryName==null) return false;
		for(int i=0; i<world.countChildren(); i++) {
			Group element = world.getChildren(i);
			if(element.getCategory().equalsIgnoreCase(categoryName)) {
				return true;
			}
		}
		return false;
	}

	public void removeCategoryStartWith(World world, String startLetter) {
		if(world==null) return;
		ArrayList<Node> list = null;
		String categoryName = startLetter.trim();
		if(categoryName==null) return;
		for(int i=0; i<world.countChildren(); i++) {
			Group element = world.getChildren(i);
			if(element.getCategory().startsWith(categoryName)) {
				Node child = (Node)element;
				if(list==null) list = new ArrayList<Node>();
				list.add(child);
			}
		}
		if(list!=null) {
			for(int i=0; i<list.size(); i++) { world.removeChild(list.get(i)); }
		}
	}
	public void remove(World world, String category) {
		if(world==null) return;
		ArrayList<Node> list = null;
		String categoryName = category.trim();
		if(categoryName==null) return;
		if(categoryName.equalsIgnoreCase("Horizon")) {
			int k = 0;
			for(int i=0; i<world.countChildren(); i++) {
				Group element = world.getChildren(i);
				if(element.getCategory().equalsIgnoreCase(categoryName)) {
					k++;
				}
			}
//			if(k==0)_layerVisibility = null;
//			else _layerVisibility = new boolean[k];
//			k = 0;
//			for(int i=0; i<world.countChildren(); i++) {
//				Group element = world.getChildren(i);
//				if(element.getCategory().equalsIgnoreCase(categoryName)) {
//					_layerVisibility[k] = element.isVisible();
//					k++;
//				}
//			}
		}
		for(int i=0; i<world.countChildren(); i++) {
			Group element = world.getChildren(i);
			if(element.getCategory().equalsIgnoreCase(categoryName)) {
				Node child = (Node)element;
				if(list==null) list = new ArrayList<Node>();
				list.add(child);
				//world.removeChild(child);
			}
		}
		if(list!=null) {
			for(int i=0; i<list.size(); i++) { world.removeChild(list.get(i)); }
		}
	}
	
	public String [] getUniqueName(String [] name) {
		ArrayList<String> pvector = new ArrayList<String>();
		for(int i=0; i<name.length; i++) {
			pvector.add(name[i]);
		}
		removeDuplicateWithOrder(pvector);

		String [] newString = new String[pvector.size()];
		for(int i=0; i<pvector.size(); i++) {
			newString[i] = pvector.get(i).trim();
		}
		return newString;
	}
	
	public void removeDuplicateWithOrder(ArrayList<String> arlList) {    
		Set<String> set 			= new HashSet<String>();    
		ArrayList<String> newList 	= new ArrayList<String>();    
		for (String element : arlList) {      
			if (set.add(element))  {
				newList.add(element);  
			}
		}    
		arlList.clear();    
		arlList.addAll(newList); 
	}
	
	public String [] getWorldUniqueCategory() {
		String [] category = getWorldCategory();
		ArrayList<String> pvector = new ArrayList<String>();
		for(int i=0; i<category.length; i++) {
			pvector.add(category[i]);
		}
		removeDuplicateWithOrder(pvector);

		String [] newString = new String[pvector.size()];
		for(int i=0; i<pvector.size(); i++) {
			newString[i] = pvector.get(i).trim();
		}
		return newString;
	}
	
	public String [] getWorldCategory() {
		World world = getWorld();
		String [] category = new String[world.countChildren()];
		//System.out.println(world.countChildren());
		for(int i=0; i<world.countChildren(); i++) {
			Group element = world.getChildren(i);
			category[i] = element.getCategory().trim();
		}
		return category;
	}
	public String [] getEntityName(String category) {
		World world = getWorld();
		ArrayList<String> 	list = new ArrayList<String>();
		for(int i=0; i<world.countChildren(); i++) {
			Group element = world.getChildren(i);

			if(element.getCategory().equalsIgnoreCase(category.trim())) {
				list.add(element.getName());
			}
		}
		if(list.size()==0) return null;
		String [] name = new String[list.size()];
		for(int i=0; i<list.size(); i++) {
			name[i] = list.get(i);
		}
		return name;
	}
	public Boolean [] getEntityVisibility(String category) {
		ArrayList<Boolean> 	list = new ArrayList<Boolean>();
		for(int i=0; i<_world.countChildren(); i++) {
			Group element = _world.getChildren(i);

			if(element.getCategory().equalsIgnoreCase(category.trim())) {
				list.add(new Boolean(element.isVisible()));
			}
		}
		Boolean [] visibility = new Boolean[list.size()];
		for(int i=0; i<list.size(); i++) {
			visibility[i] = list.get(i);
		}

		return visibility;
	}
	public void setVisibility(String category, String [] name, Boolean [] visibilityList) {
		boolean [] visibility = new boolean[visibilityList.length];
		for(int i=0; i<visibilityList.length; i++) {
			visibility[i] = visibilityList[i].booleanValue();
		}
		setVisibility(category, visibility);
	}
	public void setVisibility(String category, Boolean [] visibilityList) {
		boolean [] visibility = new boolean[visibilityList.length];
		for(int i=0; i<visibilityList.length; i++) {
			visibility[i] = visibilityList[i].booleanValue();
		}
		setVisibility(category, visibility);
	}
	public void setVisibility(String category, boolean [] visibility) {
		OrbitViewMode ovm = getObitViewMode();
		ovm.setVisibility(category, visibility, getViewCanvas(), getOrbitView());
	}
	
	public void setVisibility(String category, String name, boolean visibility) {
		OrbitViewMode ovm = getObitViewMode();
		if(ovm!=null) ovm.setVisibility(category, name, visibility, getViewCanvas(), getOrbitView());
	}
	public boolean isVisible(String category, String name) {
		OrbitViewMode ovm = getObitViewMode();
		if(ovm==null) return true;
		else return ovm.isVisible(category, name, getViewCanvas(), getOrbitView());
	}
	public void addCoordinates() { add(_world, _globalBox); }
	private void add(World world, BoundingBox globalBox) {
		if(globalBox==null) return;
		StateSet axisState = new StateSet();
		LineState ls = new LineState();
		ls.setWidth(1.0f);
		axisState.add(ls);

		ColorState cs1 = new ColorState();
		cs1.setColor(Color.BLACK);
		axisState.add(cs1);

		int maxNumOfMajorTicX = 10;
		int maxNumOfMajorTicY = 10;
		int maxNumOfMajorTicZ = 10;
		Axis3D axis3D = new Axis3D(globalBox, maxNumOfMajorTicX, maxNumOfMajorTicY, maxNumOfMajorTicZ);
		SegmentGroup sga1 = axis3D.calOutBox();
		sga1.setStates(axisState);
		sga1.setName("Axis", "Outline");
		remove(getWorld(), sga1.getCategory(), sga1.getName());
		world.addChild(sga1); 

		SegmentGroup sga2 = axis3D.calMajorTick();
		sga2.setStates(axisState);
		sga2.setName("Axis", "Major Tick");
		remove(getWorld(), sga2.getCategory(), sga2.getName());
		world.addChild(sga2); 
		
		SegmentGroup sga2_1 = axis3D.calMinorTick();
		sga2_1.setStates(axisState);
		sga2_1.setName("Axis", "Minor Tick");
		remove(getWorld(), sga2_1.getCategory(), sga2_1.getName());
		world.addChild(sga2_1);

		SegmentGroup sga3 = axis3D.calGrid();
		sga3.setStates(axisState);
		sga3.setName("Axis", "Grid");
		remove(getWorld(), sga3.getCategory(), sga3.getName());
		world.addChild(sga3); 
		
		TextGroup tga1 = axis3D.calTickLabel(1);
		tga1.setStates(axisState);
		tga1.setName("Axis", "Northing Axis Tick Label");
		remove(getWorld(), tga1.getCategory(), tga1.getName());
		world.addChild(tga1); 
		TextGroup tga2 = axis3D.calTickLabel(2);
		tga2.setStates(axisState);
		tga2.setName("Axis", "Easting Axis Tick Label");
		remove(getWorld(), tga2.getCategory(), tga2.getName());
		world.addChild(tga2); 
		TextGroup tga3 = axis3D.calTickLabel(3);
		tga3.setStates(axisState);
		tga3.setName("Axis", "Down Axis Tick Label");
		remove(getWorld(), tga3.getCategory(), tga3.getName());
		world.addChild(tga3); 
		
		StateSet axisState2 = new StateSet();
		LineState ls2 = new LineState();
		ls2.setWidth(1.0f);
		axisState2.add(ls2);

		ColorState cs2 = new ColorState();
		cs2.setColor(Color.RED);
		axisState2.add(cs2);
		
		TextGroup labelN = axis3D.calAxisLabel(1, "N");
		labelN.setStates(axisState2);
		labelN.setName("Axis", "Northing Axis Label");
		remove(getWorld(), labelN.getCategory(), labelN.getName());
		world.addChild(labelN); 
		TextGroup labelE = axis3D.calAxisLabel(2, "E");
		labelE.setStates(axisState2);
		labelE.setName("Axis", "Easting Axis Label");
		remove(getWorld(), labelE.getCategory(), labelE.getName());
		world.addChild(labelE);
		TextGroup labelD = axis3D.calAxisLabel(3, "D");
		labelD.setStates(axisState2);
		labelD.setName("Axis", "Down Axis Label");
		remove(getWorld(), labelD.getCategory(), labelD.getName());
		world.addChild(labelD);
	}
	public void drawBox() {
		drawBox(_world, _globalBox);
	}
	public void drawBox(World world, BoundingBox globalBox) {
		if(globalBox==null) return;
		StateSet axisState = new StateSet();
		LineState ls = new LineState();
		ls.setWidth(1.0f);
		axisState.add(ls);

		ColorState cs1 = new ColorState();
		cs1.setColor(Color.BLACK);
		axisState.add(cs1);
		
		StateSet gridState = new StateSet();
		LineState gls = new LineState();
		gls.setWidth(0.5f);
		gridState.add(ls);

		ColorState gcs1 = new ColorState();
		gcs1.setColor(Color.LIGHT_GRAY);
		gridState.add(gcs1);

		int maxNumOfMajorTicX = 10;
		int maxNumOfMajorTicY = 10;
		int maxNumOfMajorTicZ = 10;
		Axis3D axis3D = new Axis3D(globalBox, maxNumOfMajorTicX, maxNumOfMajorTicY, maxNumOfMajorTicZ);
		SegmentGroup sga1 = axis3D.calOutBox();
		sga1.setStates(axisState);
		sga1.setName("Box", "Outline");
		boolean visible = isVisible("Box", "Outline");
		//visible = true;
		sga1.setVisible(visible);	
		world.addChild(sga1);

		SegmentGroup sga2 = axis3D.calMajorTick();
		sga2.setStates(axisState);
		sga2.setName("Box", "Major Tick");
		visible = isVisible("Box", "Major Tick");
		sga2.setVisible(visible);
		world.addChild(sga2); 
		
		SegmentGroup sga2_1 = axis3D.calMinorTick();
		sga2_1.setStates(axisState);
		sga2_1.setName("Box", "Minor Tick");
		visible = isVisible("Box", "Minor Tick");
		sga2_1.setVisible(visible);
		world.addChild(sga2_1);
		

		String [] fnames = new String [] {"Face Left", "Face Right", "Face Back", "Face Front", "Face Top", "Face Bottom"};
		if(_showFaceFace==null) {
			_showFaceFace = new boolean[]{false, false, false, false, false, false};
			for(int i=0; i<fnames.length; i++) {
				float [] xyz = axis3D.genFace(fnames[i]);
				SegmentGroup sga3 = new SegmentGroup(xyz);
				sga3.setStates(axisState);
				sga3.setName("Box", fnames[i]);
				sga3.setVisible(_showFaceFace[i]);	
				world.addChild(sga3); 
			}
		} else {
			for(int i=0; i<fnames.length; i++) {
				float [] xyz = axis3D.genFace(fnames[i]);
				SegmentGroup sga3 = new SegmentGroup(xyz);
				sga3.setStates(axisState);
				sga3.setName("Box", fnames[i]);
				visible = isVisible("Box", fnames[i]);
				//sga3.setVisible(_showFaceFace[i]);
				sga3.setVisible(visible);
				if(_showFaceFace[i]) world.addChild(sga3); 	
				
//				QuadGroup qg = new QuadGroup(true, xyz, null, "Box", fnames[i]+" Shade");
//				qg.setStates(gridState);
//				qg.setVisible(_showFaceFace[i]);	
//				world.addChild(qg); 
			}
		}
		
		String [] names = new String [] {"Grid Left", "Grid Right", "Grid Back", "Grid Front", "Grid Top", "Grid Bottom"};
		//String [] names = new String [] {"Grid Top", "Grid Bottom"};
		if(_showGridFace==null) {
			_showGridFace = new boolean[]{false, false, false, false, false, false};
			for(int i=0; i<names.length; i++) {
				SegmentGroup sga3 = axis3D.calGrid(names[i]);
				sga3.setStates(gridState);
				sga3.setName("Box", names[i]);
				sga3.setVisible(_showGridFace[i]);	
				world.addChild(sga3); 
			}
		} else {
			for(int i=0; i<names.length; i++) {
				SegmentGroup sga3 = axis3D.calGrid(names[i]);
				sga3.setStates(gridState);
				sga3.setName("Box", names[i]);
				visible = isVisible("Box", names[i]);
				//sga3.setVisible(visible);	
				sga3.setVisible(_showGridFace[i]);
				world.addChild(sga3); 
			}
		}
		
		TextGroup tga1 = axis3D.calTickLabel(1);
		tga1.setStates(axisState);
		tga1.setName("Box", "Northing Axis Tick Label");
		visible = isVisible("Box", "Northing Axis Tick Label");
		tga1.setVisible(visible);	
		world.addChild(tga1); 
		TextGroup tga2 = axis3D.calTickLabel(2);
		tga2.setStates(axisState);
		tga2.setName("Box", "Easting Axis Tick Label");
		visible = isVisible("Box", "Easting Axis Tick Label");
		tga2.setVisible(visible);
		world.addChild(tga2); 
		TextGroup tga3 = axis3D.calTickLabel(3);
		tga3.setStates(axisState);
		tga3.setName("Box", "Down Axis Tick Label");
		visible = isVisible("Box", "Down Axis Tick Label");
		tga3.setVisible(visible);	
		world.addChild(tga3); 
		
		StateSet axisState2 = new StateSet();
		LineState ls2 = new LineState();
		ls2.setWidth(1.0f);
		axisState2.add(ls2);

		ColorState cs2 = new ColorState();
		cs2.setColor(Color.RED);
		axisState2.add(cs2);
		
		TextGroup labelN = axis3D.calAxisLabel(1, "N");
		labelN.setStates(axisState2);
		labelN.setName("Box", "Northing Axis Label");
		visible = isVisible("Box", "Northing Axis Label");
		labelN.setVisible(visible);	
		world.addChild(labelN); 
		TextGroup labelE = axis3D.calAxisLabel(2, "E");
		labelE.setStates(axisState2);
		labelE.setName("Box", "Easting Axis Label");
		visible = isVisible("Box", "Easting Axis Label");
		labelE.setVisible(visible);	
		world.addChild(labelE);
		TextGroup labelD = axis3D.calAxisLabel(3, "D");
		labelD.setStates(axisState2);
		labelD.setName("Box", "Down Axis Label");
		visible = isVisible("Box", "Down Axis Label");
		labelD.setVisible(visible);	
		world.addChild(labelD);
	}
//	public void addBox(World world, BoundingBox globalBox) {
//		StateSet axisState = new StateSet();
//		LineState ls = new LineState();
//		ls.setWidth(5.0f);
//		axisState.add(ls);
//
//		ColorState cs1 = new ColorState();
//		cs1.setColor(Color.BLACK);
//		axisState.add(cs1);
//
//		int maxNumOfMajorTicX = 10;
//		int maxNumOfMajorTicY = 10;
//		int maxNumOfMajorTicZ = 10;
//		Axis3D axis3D = new Axis3D(globalBox, maxNumOfMajorTicX, maxNumOfMajorTicY, maxNumOfMajorTicZ);
//		SegmentGroup sga1 = axis3D.calOutBox();
//		sga1.setStates(axisState);
//		sga1.setName("Box", "Outline");
//		remove(getWorld(), sga1.getCategory(), sga1.getName());
//		world.addChild(sga1); 
//	}
	
	public void drawSphere(World world, WellTrack shotLoc, String category, String name, Color color, int size) {
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
		boolean visible = isVisible(category, name);
		pg1.setVisible(visible);
		//remove(world, category, name);
		world.addChild(pg1); 
	}
	public void drawWellTrackName(World world, WellTrack well) {
		StateSet state = new StateSet();
		LineState ls = new LineState();
		ls.setWidth(3.0f);
		ls.setSmooth(true);
		state.add(ls);

		ColorState cs1 = new ColorState();
		cs1.setColor(Color.GRAY);
		state.add(cs1);

		PlotWellTrack plotWellTrack = new PlotWellTrack(well);
		plotWellTrack.genWellTrackName().setStates(state);
		TextGroup trackText = plotWellTrack.getWellTrackName();
		trackText.setName("Well_Name", well.getWellName());
		boolean visible = isVisible("Well_Name", well.getWellName());
		trackText.setVisible(visible);
		world.addChild(trackText); 
	}	
	public void drawWellTrack(World world, WellTrack well, Color color, float lineSize) {
		StateSet state = new StateSet();
		LineState ls = new LineState();
		ls.setWidth(lineSize);
		ls.setSmooth(true);
		//ls.setStipple(1, (short)5);
		state.add(ls);

		ColorState cs1 = new ColorState();
		cs1.setColor(color);
		state.add(cs1);

		PlotWellTrack plotWellTrack = new PlotWellTrack(well);
		LineGroup track = plotWellTrack.genWellTrack();
		track.setStates(state);
		track.setName("Well", well.getWellName());
		
		boolean visible = isVisible("Well", well.getWellName());
		track.setVisible(visible);		
		world.addChild(track); 
	}
	public void drawLine(World world, String category, String name, Color color, float [] NS, float [] EW, float [] TVD) {
		StateSet state = new StateSet();
		LineState ls = new LineState();
		ls.setWidth(3.0f);
		ls.setSmooth(true);
		state.add(ls);

		ColorState cs1 = new ColorState();
		cs1.setColor(color);
		state.add(cs1);

		PlotWellTrack plotWellTrack = new PlotWellTrack(NS, EW, TVD);
		plotWellTrack.genWellTrack().setStates(state);
		LineGroup track = plotWellTrack.getWellTrack();
		track.setName(category, name);
		
		boolean visible = isVisible(category, name);
		track.setVisible(visible);		
		world.addChild(track);  
	}
	public void drawVcPW(World world, int id, VCPair vcPW, int size) {
		if(vcPW==null) return;
		WellTrack shotLoc = new WellTrack(vcPW.getEN(), vcPW.getEE(), vcPW.getED());
		if(id==0) drawWellTrack(world, shotLoc, "Geometry", "Shots", Color.RED, size);
		if(id==1) {
			for(int i=0; i<vcPW.getNEvents(); i++) {
				WellTrack rcvrLoc = new WellTrack(vcPW.getRNArray(i), vcPW.getREArray(i), vcPW.getRDArray(i));
				drawWellTrack(world, rcvrLoc, "Geometry", "Receivers"+i, Color.GREEN, size);
			}
		}
	}
	public void drawWellTrack(World world, WellTrack shotLoc, String category, String name, Color color, int size) {
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
	
}


