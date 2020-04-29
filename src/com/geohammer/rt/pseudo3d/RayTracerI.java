package com.geohammer.rt.pseudo3d;

import com.geohammer.core.acquisition.VCPair;
import com.geohammer.core.planarmodel.FlatLayer1D;
import com.geohammer.core.planarmodel.Layer2D;

public interface RayTracerI {
	public FlatLayer1D getFlatLayer1D();
	public Layer2D getLayer();
	public VCPair getVCPair();
	public int  getIVp();
	
	public void  setIVp(int iVp);
	
	public void setConfiguration();
	public void start();
}
