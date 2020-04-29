package com.geohammer.core;

import org.ucdm.excel.Well;

public class FracWell extends Well{

	double [] _px; 			//Northing
	double [] _py; 			//Easting
	double [] _pz; 			//Depth
	int [] _stageNo;
	String [] _fileName;
	
	public FracWell(int id, String label, double x, double y, double z, double kbElev) {
		super(id, label, x, y, z, kbElev);
	}
	public FracWell(int nPerfs, int id, String label, double x, double y, double z, double kbElev) {
		this(id, label, x, y, z, kbElev);
		allocate(nPerfs);
	}
	
	public int getNumOfPerfs() 				{ return _px.length; }
	
	private void allocate(int nPerfs) {
		_px = new double[nPerfs];
		_py = new double[nPerfs];
		_pz = new double[nPerfs];
		
		_stageNo = new int[nPerfs];
		_fileName = new String[nPerfs];
	}
	
	public void appendPerfs(FracWell other) {
		double [] px = _px; 			//Northing
		double [] py = _py; 			//Easting
		double [] pz = _pz; 			//Depth
		int [] stageNo = _stageNo;
		String [] fileName = _fileName;
		
		int m1 = getNumOfPerfs();
		int nPerfs = m1 + other.getNumOfPerfs();
		allocate(nPerfs);
		
		for(int i=0; i<m1; i++) {
			_px[i] = px[i];
			_py[i] = py[i];
			_pz[i] = pz[i];
			_stageNo[i] = stageNo[i];
			_fileName[i] = fileName[i];
		}
		for(int i=m1; i<nPerfs; i++) {
			_px[i] = other._px[i-m1];
			_py[i] = other._py[i-m1];
			_pz[i] = other._pz[i-m1];
			_stageNo[i] = other._stageNo[i-m1];
			_fileName[i] = other._fileName[i-m1];
		}
	}
	public void trim() {
		int nPerfs = 0;
		for(int i=0; i<_px.length; i++) {
			//System.out.println("nPerfs="+_fileName[i]);
			if(_fileName[i]==null||_fileName[i].length()<8) {
			} else { nPerfs++; }
		}
		if(nPerfs==0) return;
		//System.out.println("nPerfs="+nPerfs);
		
		double [] px = new double[nPerfs];
		double [] py = new double[nPerfs];
		double [] pz = new double[nPerfs];
		
		int [] stageNo = new int[nPerfs];
		String [] fileName = new String[nPerfs];
		
		int k = 0;
		for(int i=0; i<_px.length; i++) {
			if(_fileName[i]==null||_fileName[i].length()<8) {
			} else { 
				px[k] = _px[i];
				py[k] = _py[i];
				pz[k] = _pz[i];
				
				stageNo[k] = _stageNo[i];
				fileName[k] = _fileName[i]+"";
				k++; 
			}
		}
		
		_px = px;
		_py = py;
		_pz = pz;
		
		_stageNo = stageNo;
		_fileName = fileName;
	}
	public String toString() {
		String b = String.format("%.2f \t%.2f \t%.2f  // well location ", getY(), getX(), getZ());
		String a = getNumOfPerfs() + " // Number of perforations";
		b = b.concat("\n"+a+"\n");
		
		a = "Easting	Northing	Depth	Stage No	PerfFile";
		b = b.concat(a+"\n");
		for(int i=0; i<getNumOfPerfs(); i++) {
			a = String.format("%.2f \t%.2f \t%.2f \t%d ", _py[i], _px[i], _pz[i], _stageNo[i]);
			if(_fileName[i]!=null) a +="\t "+_fileName[i];
			b = b.concat(a+"\n");
			//if(i==getNumOfPerfs()-1) b = b.concat(a+"\n");
			//else b = b.concat(a+"\n");
		}
		
		return b;
	}

}
