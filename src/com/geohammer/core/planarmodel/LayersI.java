package com.geohammer.core.planarmodel;

public interface LayersI {
	public int getNumOfBoundaries();
	public HalfSpace getLayer(int index);
}
