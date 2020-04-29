package com.geohammer.core.planarmodel;

import java.util.ArrayList;

import com.geohammer.rt.pseudo3d.CubicInterpolator;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SingleLayer2D  extends SingleLayer3D {
	private ArrayList<Point2D> 	masterPoints 		= null; 	// master points
	private SingleCurve 		singleCurve 		= null;		// interpolated curve

	public SingleLayer2D() { }
	public SingleLayer2D(String layerName, int id, double vp, double vs, double den, 
			double vpGradientX, double vpGradientZ, double vsGradientX, double vsGradientZ, 
			double delta, double epsilon, double gamma, double qp, double qs, double theta, double phi,
			ArrayList<Point2D> 	mp, SingleCurve curve) {
		this(layerName, id, vp, vs, den, 
				vpGradientX, 0, vpGradientZ, vsGradientX, 0, vsGradientZ, 
				delta, epsilon, gamma, qp, qs, theta, phi, 
				2, 2, 1, 1, 0, 0, null, mp, curve);
	}
	public SingleLayer2D(String layerName, int id, double vp, double vs, double den, 
			double vpGradientX, double vpGradientY, double vpGradientZ, double vsGradientX, double vsGradientY, double vsGradientZ, 
			double delta, double epsilon, double gamma, double qp, double qs, double theta, double phi, 
			int nx, int ny, double dx, double dy, double x0, double y0, float [][] dp, ArrayList<Point2D> mp, SingleCurve curve) {
		super(layerName, id, vp, vs, den, 
				vpGradientX, vpGradientY, vpGradientZ, vsGradientX, vsGradientY, vsGradientZ, 
				delta, epsilon, gamma, qp, qs, theta, phi,  
				nx, ny, dx, dy, x0, y0, dp);
		masterPoints = mp;
		if(curve!=null) singleCurve = curve;
		else { setCurve(dx); }
	}

	public SingleLayer2D copy() {
		SingleLayer2D target = new SingleLayer2D(getLayerName(), getId(), getVp(), getVs(), getDen(), 
				getVpGradientX(), getVpGradientZ(), getVsGradientX(), getVsGradientZ(), 
				getDelta(), getEpsilon(), getGamma(), getQp(), getQs(), getTheta(), getPhi(),
				new ArrayList<Point2D>(), null);
		if(masterPoints!=null) {
			for(int i=0; i<masterPoints.size(); i++) target.masterPoints.add(masterPoints.get(i).copy());
		}
		if(singleCurve!=null) { target.singleCurve= singleCurve.copy(); }
		return target;
	}
	public void convertUnitSystem(double velScalor, double denScalor) {
		super.convertUnitSystem(velScalor, denScalor);
		if(masterPoints!=null) {
			for(int i=0; i<masterPoints.size(); i++) { masterPoints.get(i).convertUnitSystem(velScalor); }
		}
		if(singleCurve!=null) { singleCurve.convertUnitSystem(velScalor); }
	}

	//for graphic paint()
	public float [] getMasterX() {
		float [] x = new float[masterPoints.size()];
		for(int i=0; i<x.length; i++) x[i] = (float)masterPoints.get(i).getE();
		return x;
	} 
	public float [] getMasterZ() {
		float [] z = new float[masterPoints.size()];
		for(int i=0; i<z.length; i++) z[i] = (float)masterPoints.get(i).getD();
		return z;
	} 
	//public double getZ(double x) 				{ return singleCurve.getPointZ(x); }
	public double getMasterX(int index) 		{ return masterPoints.get(index).getE(); }
	public double getMasterZ(int index) 		{ return masterPoints.get(index).getD(); }
	public Point2D getMp(int index) 			{ return masterPoints.get(index); }
	public ArrayList<Point2D> getMp() 			{ return masterPoints; }
	public SingleCurve getCurve() 				{ return singleCurve; }

	public void setMasterX(int index, double v) { masterPoints.get(index).setE(v); }
	public void setMasterZ(int index, double v) { masterPoints.get(index).setD(v); }
	public void setMp(ArrayList<Point2D> mp) 	{ masterPoints = mp; }
	public void setCurve(SingleCurve curve) 	{ singleCurve = curve; }

	public void setCurve() 				{ setCurve(getDx()); }
	public void setCurve(double dx) { 
		if(masterPoints.size()<2) { return; }
		else if(masterPoints.size()==2) {
			double slope = (getMasterZ(1)-getMasterZ(0))/(getMasterX(1)-getMasterX(0));
			singleCurve = new SingleCurve(2, new float[]{(float)getMasterX(0), (float)getMasterX(1)}, 
					new float[]{(float)getMasterZ(0), (float)getMasterZ(1)}, 
					new float[]{(float)slope, (float)slope});
		} else {
			if(dx==0) return;
			float [] masterX = new float[masterPoints.size()];
			float [] masterZ = new float[masterPoints.size()];
			for(int i=0; i<masterPoints.size(); i++) {
				masterX[i] = (float)masterPoints.get(i).getE();
				masterZ[i] = (float)masterPoints.get(i).getD();
			}

			double distance = masterPoints.get(0).distance(masterPoints.get(masterPoints.size()-1));
			int nx 			= (int)(distance/dx)+1;

			float [] gridX 		= new float[nx];
			float [] gridY 		= new float[nx];
			float [] gridYPrime = new float[nx];

			for(int i=0; i<nx; i++) { gridX[i] = (float)(masterPoints.get(0).getE() + i*dx); }
			gridX[nx-1] = (float)(masterPoints.get(masterPoints.size()-1).getE());


			CubicInterpolator cl = new CubicInterpolator(CubicInterpolator.Method.SPLINE, masterX.length, masterX, masterZ);
			cl.interpolate(nx, gridX, gridY);
			cl.interpolate1(nx, gridX, gridYPrime);

			singleCurve = new SingleCurve(nx, gridX, gridY, gridYPrime);
		}

		//System.out.println(cv.toString());
		//if(i==1) System.exit(1);
	}
	public void addMasterPointAt(double x, double z) { 
		int index = 1;
		for(int i=1; i<masterPoints.size(); i++) {
			if(x>=masterPoints.get(i-1).getE()&&x<masterPoints.get(i).getE()) { index = i; break; }
		}
		addMasterPointAt(index, x, z); 
	}
	public void addMasterPointAt(int index, double x, double z) { masterPoints.add(index, new Point2D(x, z)); }
	public void removeMasterPointAt(int index) 					{ masterPoints.remove(index); }

	public void moveTo(double z) {moveTo(-1, 0, z);}
	public void moveTo(int index, double x, double z) {
		double top = singleCurve.getPointZ(x);
		double shiftZ = z-top;
		//if(shiftZ<0.0) return;
		shift(index, 0, shiftZ);
	}
	public void shift(double shiftZ){ shift(-1, 0, shiftZ); }
	public void shift(int index, double shiftX, double shiftZ){
		if(index<0) { // whole curve shift
			shift(shiftX, shiftZ);
			for(int i=0; i<masterPoints.size(); i++) {
				masterPoints.get(i).setE(masterPoints.get(i).getE()+shiftX);
				masterPoints.get(i).setD(masterPoints.get(i).getD()+shiftZ);
			}
		} else { // individual shift
			masterPoints.get(index).setE(masterPoints.get(index).getE()+shiftX);
			masterPoints.get(index).setD(masterPoints.get(index).getD()+shiftZ);
		}

		setCurve(getDx());
	}

	public String toDeepString() 		{ return toDeepString(2); }
	public String toDeepString(int id) 	{ return super.toDeepString(id); }

	public String toString(int id) {
		if(id==1) return super.toString(1);
		else if(id==2) {

			String b = String.format("%s %d 1 %f %f %f 1", 
					getLayerName(), getId(), getVp(), getVpGradientZ(), getVpToVs()); 
			String a = String.format("%f %f %f %f", getVpGradientX(), getDen(), getEpsilon(), getDelta());
			b = new String(b.concat("\n"+a));
			a = String.format("1 %f %f %f", getTheta(), getPhi(), getGamma());
			b = new String(b.concat("\n"+a));

			a = String.format("%d", masterPoints.size());
			b = new String(b.concat("\n"+a));
			for(int i=0; i<masterPoints.size(); i++) {
				a = String.format("%.2f %.2f", masterPoints.get(i).getE(), masterPoints.get(i).getD());
				b = new String(b.concat("\n"+a));
			}
			b = new String(b.concat("\n0"));
			//b = new String(b.concat("\n"));

			return b;
		} else {
			return String.format("%s %d %f %f %f %f %f %f %f %f %f %f %f %f", getLayerName(), getId(), 
					masterPoints.get(0).getE(), masterPoints.get(0).getD(), masterPoints.get(1).getE(), masterPoints.get(1).getD(), 
					getVp(), getVs(), 
					getDen(), getDelta(), getEpsilon(), getGamma(), getQp(), getQs());
		}
	}

}

