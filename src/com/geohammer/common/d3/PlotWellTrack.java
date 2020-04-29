package com.geohammer.common.d3;


import edu.mines.jtk.sgl.LineGroup;

import com.geohammer.common.WellTrack;

public class PlotWellTrack {

	WellTrack 	_well 			= null;
	TextGroup _wellName 		= null;
	LineGroup _wellTrack 		= null;
	
	public PlotWellTrack(WellTrack 	well) {
		_well = well;
	}
	
	public PlotWellTrack(float [] dataX, float [] dataY, float [] dataZ) {
		_well = new WellTrack(dataX, dataY, dataZ);
	}
	
	public LineGroup getWellTrack(){
		return _wellTrack;
	}
	public TextGroup getWellTrackName(){
		return _wellName;
	}	
	
	public LineGroup genWellTrack(){
		_wellTrack = new LineGroup(getXYZ(_well.getNumOfPoints()));
		return _wellTrack;
	}
	public TextGroup genWellTrackName(){
		_wellName = new TextGroup(Axis3D._buf, getXYZ(1), new String[]{_well.getWellName()});
		return getWellTrackName();
	}
	
	public float [] getXYZ() {
		return getXYZ(_well.getNumOfPoints());
	}
	
	public float [] getXYZ(int nPoints) {
		float [] xyz = new float[3*nPoints];

		int k = 0;
		for(int i=0; i<nPoints; i++) {
			xyz[k++] = (float)(_well.getX(i));
			xyz[k++] = (float)(_well.getY(i));
			xyz[k++] = (float)(_well.getZ(i));
		}
		return xyz;
	}
}
