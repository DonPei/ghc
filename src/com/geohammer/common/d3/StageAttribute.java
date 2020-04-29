package com.geohammer.common.d3;

import java.awt.Color;

public class StageAttribute {
	
	private int 		_index0 	= 0;
	private int 		_index1 	= 0;
	private Color 		_color 		= Color.blue;
	private boolean 	_visible 	= true;
	private String 		_stageName 	= null;
	private String 		_wellName 	= null;
	
	public StageAttribute(String wellName, String stageName, int index0) {
		this(wellName, stageName, index0, 0);
	}
	public StageAttribute(String wellName, String stageName, int index0, int index1) { 
		_wellName 	= wellName;
		_stageName 	= stageName;
		_index0 	= index0;
		_index1 	= index1;
	}
	
	public int 	getIndex0() 				{ return _index0; }
	public int 	getIndex1() 				{ return _index1; }
	public boolean 	getVisible() 			{ return _visible; }
	public Color 	getColor() 				{ return _color; }
	public String 	getWellName() 			{ return _wellName; }
	public String 	getStageName() 			{ return _stageName; }
	
	public void setIndex0(int index) 		{ _index0 = index; }
	public void setIndex1(int index) 		{ _index1 = index; }
	public void setVisible(boolean visible) { _visible = visible; }
	public void setColor(Color color) 		{ _color = color; }
	
	public void printClass() {
		System.out.println("wellName=" + _wellName+" stageName=" + _stageName+" index0="+_index0+" index1="+_index1);
	}
}

