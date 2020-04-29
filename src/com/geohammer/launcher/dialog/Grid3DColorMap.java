package org.ucdm.launcher.dialog;

import javax.swing.JTextField;

import org.ucdm.common.CommonColorMap;

public class Grid3DColorMap extends CommonColorMap {
	
	JTextField _minColorTF 		= null;
	JTextField _maxColorTF 		= null;
	
	public Grid3DColorMap() {
		_minColor 			= 0;
		_maxColor 			= 1;
		_autoColor 			= true;
		_indexColorModelId 	= 1;
		
		_minColorTF 		= new JTextField(_minColor+"");
		_maxColorTF 		= new JTextField(_maxColor+"");
	}
	
	public JTextField getMinColorTF() { return _minColorTF; }
	public JTextField getMaxColorTF() { return _maxColorTF; }
	
}
