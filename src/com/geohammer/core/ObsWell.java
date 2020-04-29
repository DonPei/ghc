package com.geohammer.core;

import org.ucdm.excel.Well;


public class ObsWell extends Well {
	//Easting, Northing, KB Elevation, 
	String _chanOrder = null;  // default is zyx
	Sensor [] _sensors = null; 
	String _dimension = null;
	public ObsWell(int nSensors, int id, String label, double x, double y, double z, double kbElev, 
			String chanOrder, String dimension) {
		super(id, label, x, y, z, kbElev);
		_chanOrder = chanOrder;
		_dimension = dimension;
		allocate(nSensors);
	}
	
	public ObsWell clone() {
		ObsWell well = new ObsWell(_sensors.length, getId(), getLabel(), getX(), getY(), getZ(), getKbElev(), _chanOrder, _dimension);
		for(int i=0; i<_sensors.length; i++) {
			well._sensors[i] = _sensors[i].copy();
		}
		return well;
	}
	private void allocate(int nSensors) {
		_sensors = new Sensor[nSensors];
	}
	
	public Sensor getSensor(int index) {
		return _sensors[index];
	}
	
	public void setSensor(int index, Sensor sensor) {
		_sensors[index] = sensor;
	}
	public int getNumOfSensors() {
		return _sensors.length;
	}
	
	public String getChanOrder ( ) {return _chanOrder; }
	
	public String toString() {
		String b = String.format("%.2f \t%.2f \t%.2f  // obs well location", getY(), getX(), getZ());
		
		//String b = getX()+" "+getY()+" "+getZ()+" // obs well location";
		String a = getNumOfSensors() + " // Number of sensors";
		b = b.concat("\n"+a+"\n");
		a = _chanOrder + " // Tool channel order";
		b = b.concat(a+"\n");
		
		if(_dimension.equalsIgnoreCase("2D")) {
			for(int i=0; i<getNumOfSensors(); i++) {
				a = String.format("\t%.2f \t%.2f ", _sensors[i].getZ(), _sensors[i]._delta);
				if(i==getNumOfSensors()-1) b = b.concat(a);
				else b = b.concat(a+"\n");
			}				
		} else  {
			for(int i=0; i<getNumOfSensors(); i++) {
				a = String.format("\t%.2f \t%.2f \t%.2f \t%.2f \t%.2f \t%.2f", 
						_sensors[i].getY(), _sensors[i].getX(), _sensors[i].getZ(), 
						_sensors[i]._theta, _sensors[i]._phi, _sensors[i]._delta);
	
				if(i==getNumOfSensors()-1) b = b.concat(a);
				else b = b.concat(a+"\n");
			}
		}
		
		return b;
	}
}
