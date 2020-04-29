package com.geohammer.common;

import java.awt.image.IndexColorModel;

import edu.mines.jtk.awt.ColorMap;

public class CommonColorMap {

	public float 			_minColor 			= 0;
	public float 			_maxColor 			= 1;
	public boolean 			_autoColor 			= true;
	public int 				_indexColorModelId 	= 0;
	
	public CommonColorMap() { }
	

	public float getMinColor() 					{ return _minColor; }
	public float getMaxColor() 					{ return _maxColor; }
	public boolean getAutoColor() 				{ return _autoColor; }	
	public int getIndexColorModelId()    		{ return _indexColorModelId; }

	public void setMinColor(float minColor) 	{ _minColor = minColor; }
	public void setMaxColor(float maxColor) 	{ _maxColor = maxColor; }
	public void setAutoColor(boolean autoColor) { _autoColor = autoColor; }
	public void setIndexColorModelId(int indexColorModelId) { _indexColorModelId = indexColorModelId; }

	public IndexColorModel getIndexColorModel() {
		return getIndexColorModel(_indexColorModelId);
	}
	public IndexColorModel getIndexColorModel(int indexColorModelId) {
		if(indexColorModelId==0) return ColorMap.GRAY;
		else if(indexColorModelId==1) return ColorMap.JET;
		else if(indexColorModelId==2) return ColorMap.GMT_JET;
		else if(indexColorModelId==3) return ColorMap.HUE;
		else if(indexColorModelId==4) return ColorMap.HUE_RED_TO_BLUE;
		
		else if(indexColorModelId==5) return ColorMap.HUE_BLUE_TO_RED;
		else if(indexColorModelId==6) return ColorMap.PRISM;
		else if(indexColorModelId==7) return ColorMap.RED_WHITE_BLUE;
		else if(indexColorModelId==8) return ColorMap.BLUE_WHITE_RED;
		else if(indexColorModelId==9) return ColorMap.BLUE_WHITE_WHITE_RED;
		
		else if(indexColorModelId==10) return ColorMap.GRAY_YELLOW_RED;
		else if(indexColorModelId==11) return ColorMap.SPECTRAL;
		else if(indexColorModelId==12) return ColorMap.PLUS_MINUS;
		return ColorMap.GRAY;
	}
	
	public String[] getIndexColorModelString() {
		return new String[] {"GRAY", "JET", "GMT_JET", "HUE", "HUE_RED_TO_BLUE", 
				"HUE_BLUE_TO_RED", "PRISM",  "RED_WHITE_BLUE", "BLUE_WHITE_RED", "BLUE_WHITE_WHITE_RED", 
				"GRAY_YELLOW_RED", "SPECTRAL", "PLUS_MINUS"			
		};
	}
}

	  
