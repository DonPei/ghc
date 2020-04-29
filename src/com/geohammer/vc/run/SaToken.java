package com.geohammer.vc.run;

import com.geohammer.rt.pseudo3d.RayTracerFlat3D;

public class SaToken {

	RayTracerFlat3D  	_rayTracer 	= null;
	private double [][] _rmsArray	= null;
	
	public SaToken(RayTracerFlat3D  rayTracer, double [][] rmsArray) {
		_rayTracer = rayTracer;
		_rmsArray = rmsArray;
	}
	
	public RayTracerFlat3D getRayTracerFlat3D() { return _rayTracer;}
	public double [][] getRmsArray() { return _rmsArray;}
}
