package com.geohammer.rt;

import java.util.ArrayList;

public class RayPaths {
	private ArrayList<RayPath> _rays 			= null;
	private ArrayList<EnsembleHeader> _headers	= null;
	
	public RayPaths() {
	}

	public RayPaths(int nRayPaths) {
		_rays =  new ArrayList<RayPath>(nRayPaths);
	}

	public int getNumOfRayPaths() 					{ return _rays.size(); }
	public RayPath getRayPath(int index) 			{ return _rays.get(index); }
	public ArrayList<RayPath> getRayPathList() 		{ return _rays; }
	public RayPaths getRayPaths(int [] index) 		{ return getRayPaths(0, index); }
	public RayPaths getRayPathsDeep(int [] index) 	{ return getRayPaths(1, index); }

	public RayPaths getShotRayPathsShallow(double [] point) 	{ return getShotRayPaths(0, point); }
	public RayPaths getShotRayPathsDeep(double [] point) 		{ return getShotRayPaths(1, point); }
	public RayPaths getReceiverRayPathsShallow(double [] point) { return getReceiverRayPaths(0, point); }
	public RayPaths getReceiverRayPathsDeep(double [] point) 	{ return getReceiverRayPaths(1, point); }
	
	public int getNumOfEmsembles() 			{ return _headers.size(); }
	public int getNumOfTraces() 			{ return _rays.size(); }
	public RayPaths getEmsemble(int index) 	{ return getRayPaths(0, _headers.get(index).getIndex()); }
	public void updateHeaders() {
		RayPath rayPath = getRayPath(0);
		int k = 0;
		_headers =  new ArrayList<EnsembleHeader>(1);
		_headers.add(new EnsembleHeader(k++, 0, 0, rayPath.getE(0), rayPath.getN(0), rayPath.getD(0)));
		
		for(int i=1; i<_rays.size(); i++) {
			if(!rayPath.containEmsemble(getRayPath(i))) { 
				rayPath= getRayPath(i);
				_headers.add(new EnsembleHeader(k++, i, 0, rayPath.getE(0), rayPath.getN(0), rayPath.getD(0)));
			}
		}
	
		for(int i=0; i<_headers.size(); i++) {
			if(i==_headers.size()-1) 	_headers.get(i).setI2(_rays.size()-1);
			else  						_headers.get(i).setI2(_headers.get(i+1).getI1()-1); 
		}
	}
	
	public RayPaths getShotRayPaths(int isCopy, double [] point) {
		int k = 0;
		for(int i=0; i<_rays.size(); i++) { if(_rays.get(i).equalFirstPoint(point)) k++; }
		
		int [] index = new int[k];
		for(int i=0, j=0; i<_rays.size(); i++) { if(_rays.get(i).equalFirstPoint(point)) {index[j]=i; j++;} }
		return getRayPaths(isCopy, index);
	}
	public RayPaths getReceiverRayPaths(int isCopy, double [] point) {
		int k = 0;
		for(int i=0; i<_rays.size(); i++) { if(_rays.get(i).equalLastPoint(point)) k++; }
		
		int [] index = new int[k];
		for(int i=0, j=0; i<_rays.size(); i++) { if(_rays.get(i).equalLastPoint(point)) {index[j]=i; j++;} }
		return getRayPaths(isCopy, index);
	}
	public RayPaths getRayPaths(int isCopy, int [] index) {
		RayPaths other = new RayPaths(index.length);
		for(int j=0; j<index.length; j++) {
			for(int i=0; i<_rays.size(); i++) {
				if(i==index[j]) {
					RayPath rayPath = getRayPath(i);
					if(isCopy==1) rayPath = rayPath.copy();
					other.add(rayPath);
				}
			}
		}
		return other;
	}
	public void clear() { 
		if(_rays==null) return;
		if(_rays.size()==0) return;
		_rays.clear(); 
	}
	public void remove(int index) 				{ _rays.remove(index); }
	public void add(RayPaths other) 			{ _rays.addAll(other.getRayPathList()); }
	public void add(RayPath ray) 			 	{ _rays.add(ray); }
	public void add(int index, RayPath ray) 	{ _rays.add(index, ray); }
	public void set(int index, RayPath ray) 	{ _rays.set(index, ray); }
	
	public RayPaths copy() {
		if(_rays==null) return null;
		if(_rays.size()<0) return null;
		RayPaths other = new RayPaths(getNumOfRayPaths());
		for(int i=0; i<_rays.size(); i++) {
			RayPath rayPath = getRayPath(i).copy();
			other.add(rayPath);
		}
		return other;
	}
	
	public String toString() {
		String b =_rays.size()+"\n";
		String a = null;
		for(int i=0; i<_rays.size(); i++) {
			a =_rays.get(i).toString(); 
			b = b.concat(a+"\n");
		}

		return b;
	}
	
	public double [] calEmergentAngle() {
		double [] angle = new double[_rays.size()];
		for(int i=0; i<_rays.size(); i++) {
			angle[i] = _rays.get(i).calEmergentAngle();
		}
		return angle;
	}
	
	private class EnsembleHeader {
		private int _id 	= 0;
		private int _i1 	= 0;
		private int _i2 	= 0;
		private double _e 	= 0.0;
		private double _n 	= 0.0;
		private double _d 	= 0.0;
		
		public EnsembleHeader(int id, int i1, int i2, double e, double n, double d)	{
			_id = id;
			_i1 = i1;
			_i2 = i2;
			_e = e;
			_n = n;
			_d = d;
		}
		public int getId() 				{ return _id; }
		public int getI1() 				{ return _i1; }
		public int getI2() 				{ return _i2; }
		public double getE() 			{ return _e; }
		public double getN() 			{ return _n; }
		public double getD() 			{ return _d; }
		public int getNumOfTraces() 	{ return _i2-_i1+1; }
		public int [] getIndex() 		{ 
			int [] index = new int[getNumOfTraces()];
			for(int i=_i1, k=0; i<=_i2; i++, k++) index[k] = i;
			return index; 
		}
		
		public void setId(int id) 		{ _id = id; }
		public void setI1(int i1) 		{ _i1 = i1; }
		public void setI2(int i2) 		{ _i2 = i2; }
		public void setE(double e) 		{ _e = e; }
		public void setN(double n) 		{ _n = n; }
		public void setD(double d) 		{ _d = d; }
		
		public void setLocation(double e, double n, double d) {
			_e = e;
			_n = n;
			_d = d;
		}
		
		public String toString() {
			return "id="+_id+" i1="+_i1+" i2="+_i2+" e="+_e+" n="+_n+" d="+_d;
		}
	}
}
