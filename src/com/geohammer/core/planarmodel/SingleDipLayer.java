package com.geohammer.core.planarmodel;

import com.geohammer.rt.pseudo3d.Almost;

public class SingleDipLayer extends SingleLayer3D {
	private SingleLine  _tLine 	= null;
	private SingleLine  _bLine 	= null;
	
	public SingleDipLayer() { super();	}
	public SingleDipLayer(SingleLine tLine, SingleLine bLine, String layerName, int id, double vp, double vs, double den, 
			double delta, double epsilon, double gamma, double qp, double qs, double theta, double phi) {
		super(layerName, id, vp, vs, den, 
				0, 0, 0, 0, 0, 0, 
				delta, epsilon, gamma, qp, qs, theta, phi, 
				1, 1, 1, 1, 0, 0, null);
		init(tLine, bLine);
	}
	
	public void init(SingleLine tLine, SingleLine bLine) {
		_tLine 	= tLine;
		_bLine 	= bLine;
	}
	
	public SingleDipLayer copy() { 
		return new SingleDipLayer(getTLine().copy(), getBLine().copy(), getLayerName(), getId(), getVp(), getVs(), getDen(), 
			getDelta(), getEpsilon(), getGamma(), getQp(), getQs(), getTheta(), getPhi()); 
	}
	public SingleDipLayer split(Point2D p) {
		if(!inside(p)) return null;
		SingleDipLayer lowerPart = copy();
		
		double cz1 = _tLine.getPointZ(p.getE());
		double cz2 = _bLine.getPointZ(p.getE());
		double fraction = (p.getD()-cz1)/(cz2-cz1);
		
		double az = _tLine.getP1().getD()+(_bLine.getP1().getD()-_tLine.getP1().getD())*fraction;
		double bz = _tLine.getP2().getD()+(_bLine.getP2().getD()-_tLine.getP2().getD())*fraction;
		
		getBLine().getP1().setD(az);
		getBLine().getP2().setD(bz);
		lowerPart.getTLine().getP1().setD(az);
		lowerPart.getTLine().getP2().setD(bz);
		
		return lowerPart;
	}

	public boolean onTLine(Point2D p) 		{ return _tLine.on(p); }
	public boolean onBLine(Point2D p) 		{ return _bLine.on(p); }
	public boolean onLeftEdge(Point2D p) 	{ return Almost.DOUBLE.cmp(p.getE(),_tLine.getP1().getE())==0; }
	public boolean onRightEdge(Point2D p) 	{ return Almost.DOUBLE.cmp(p.getE(),_tLine.getP2().getE())==0; }
	public boolean inside(Point2D p) 	{ 
		if(!include(p)) 	return false;
		if(onTLine(p)) 		return false;
		if(onBLine(p)) 		return false;
		if(onLeftEdge(p)) 	return false;
		if(onRightEdge(p)) 	return false;
		return true;
	}
	public boolean include(Point2D p) 	{ 
		if(Almost.DOUBLE.cmp(p.getE(),_tLine.getP1().getE())==-1) return false;
		if(Almost.DOUBLE.cmp(p.getE(),_tLine.getP2().getE())==1) 	return false;
		if(_tLine.above(p)) 					return false;
		if(_bLine.below(p)) 					return false;
		return true;
	}
	
	public SingleLine getTLine() 				{ return _tLine; }
	public SingleLine getBLine() 				{ return _bLine; }
	
	public void setTLine(SingleLine tLine) 		{ _tLine = tLine; }
	public void setBLine(SingleLine bLine) 		{ _bLine = bLine; }
	public void setTLine(double x1, double x2, double z1, double z2) 		{ 
		_tLine = new SingleLine(x1, z1, x2, z2); 
	}
	
	public String toDeepString() {
		return getId()+ " "+_tLine.toString()+ " "+_bLine.toString()+ " "+getVp()+ " "+getVs()+ " "+getDen()+ " "+
				getDelta()+ " "+getEpsilon()+ " "+getGamma()+ " "+getQp()+ " "+getQs()+" "+getTheta()+ " "+getPhi();
	}
	
	public String toString() {
		return _tLine.toString()+ " "+getVp()+ " "+getVs()+ " "+getDen()+ " "+
				getDelta()+ " "+getEpsilon()+ " "+getGamma()+ " "+getQp()+ " "+getQs()+" "+getTheta()+ " "+getPhi();
	}
}
