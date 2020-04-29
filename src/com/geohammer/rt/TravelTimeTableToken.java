package com.geohammer.rt;

import com.geohammer.core.planarmodel.Vecon2D;
import org.ucdm.core.planarmodel.Vecon3D;

public class TravelTimeTableToken {

	Vecon2D 	_tt2D 		= null;
	Vecon3D 	_tt3D 		= null;
	
	double _x; 			
	double _y;
	double _z;
	
	public TravelTimeTableToken() {

	}

	public TravelTimeTableToken(double x, double y, double z, Vecon2D tt2D, Vecon3D tt3D) {
		_x 		= x;
		_y 		= y;
		_z 		= z;
		_tt2D 	= tt2D;
		_tt3D 	= tt3D;
	}
	
	public double getX() { return _x; }
	public double getY() { return _y; }
	public double getZ() { return _z; }
	public Vecon2D getVecon2D() { return _tt2D; }
	public Vecon3D getVecon3D() { return _tt3D; }
	
	
}
