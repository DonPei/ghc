package com.geohammer.rt.pseudo3d;

import com.geohammer.core.planarmodel.FlatLayer1D;
import com.geohammer.rt.RayPaths;

public interface FrameI {
	//public void updateTime(int iAutoSet);
	public void setRayPath(int index, RayPaths rayPaths);
	
	public Pseudo3DRayTracer getRayTracer();
	public void newRayTracer();
	public void startRayTracing(int runNo);
	
	public String getSaCwd();
	public String genSaCwd();
	public void setSaCwd(String cwd);
	
	public int getDimension();
	
	public void saveVCPairAs(boolean eventOnly);
	public void redrawAndUpdateLayer(int [] index, FlatLayer1D layer1D);
}
