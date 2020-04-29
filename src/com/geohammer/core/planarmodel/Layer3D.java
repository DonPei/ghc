package com.geohammer.core.planarmodel;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.StringTokenizer;

import javax.swing.JOptionPane;

import com.geohammer.core.planarmodel.LayersI;
import com.geohammer.core.vecon.VeconModel;

import edu.mines.jtk.util.ArrayMath;

public class Layer3D extends HalfSpaceBox implements LayersI {
	public int 	_iUnit = 2;		//1 m; 2 ft; 3 km
	public ArrayList<SingleLayer3D> _layers = null;
	public double [] _rp 					= null;
	private double 		_azimuth 			= 0.0; 	
	private double 		_dip 				= 0.0;	
	private double [] 	_topDepth 			= null;
	private double [] 	_botDepth 			= null; 
	
	public void setAzimuth(double azimuth) 	{ _azimuth = azimuth; }
	public void setDip(double dip) 			{ _dip = dip; }
	
	public void setTopDepth(double [] topDepth) 		{ _topDepth = topDepth; }
	public void setBotDepth(double [] botDepth) 		{ _botDepth = botDepth; }
	
	public double getAzimuth( ) 		{ return _azimuth; }
	public double getDip( ) 			{ return _dip; }

	public Layer3D( ){ }
	public Layer3D( int nBoundaries ){ _layers=new ArrayList<SingleLayer3D>(nBoundaries); }
	public Layer3D(String selectedFileName)	{ this(1, selectedFileName ); }
	public Layer3D(int iType, String selectedFileName)	{
		_layers = readLayer(iType, selectedFileName );
	}

	public Layer3D(String layerName, int id, double vp, double vs, double den,
			double vpGradientX, double vpGradientY, double vpGradientZ, double vsGradientX, double vsGradientY, double vsGradientZ,  
			double delta, double epsilon, double gamma, double qp, double qs, double theta, double phi,
			int nx, int ny, double dx, double dy, double x0, double y0, float dp) {

		float [][] horizon = new float[ny][nx];
		for(int i=0; i<ny; i++) {
			for(int j=0; j<nx; j++) horizon[i][j] = dp;
		}

		_layers = new ArrayList<SingleLayer3D>();

		SingleLayer3D singleLayer = new SingleLayer3D(layerName, id, vp, vs, den, 
				vpGradientX, vpGradientY, vpGradientZ, vsGradientX, vsGradientY, vsGradientZ,  
				delta, epsilon, gamma, qp, qs, theta, phi,
				nx, ny, dx, dy, x0, y0, horizon);
		_layers.add(singleLayer); 
	}

	public Layer3D copy() {
		Layer3D layer3D = new Layer3D();
		layer3D._layers = new ArrayList<SingleLayer3D>();
		layer3D.setTopDepth(ArrayMath.copy(_topDepth));
		layer3D.setBotDepth(ArrayMath.copy(_botDepth));
		layer3D.setAzimuth(_azimuth);
		layer3D.setDip(_dip);
		layer3D.setIUnit(_iUnit);

		for(int i=0; i<_layers.size(); i++) { layer3D._layers.add(_layers.get(i).copy()); }
		return layer3D;
	}
	public void localize() {
		for(int i=0; i<_layers.size(); i++) { 
			SingleLayer3D sl = _layers.get(i);
			sl.translate(-sl.getX0(), -sl.getY0(), 0); 
		}
	}
	
	public void cut(int ix1, int ix2, int iy1, int iy2) {
		for(int i=0; i<_layers.size(); i++) { 
			SingleLayer3D sl = _layers.get(i);
			sl.cut(ix1, ix2, iy1, iy2); 
		}
		setBoundingBox();
	}
	public void setBoundingBox() {
		int k = 0;
		double [][] bb = new double[6][_layers.size()];
		for(int i=0; i<_layers.size(); i++) { 
			SingleLayer3D sl = _layers.get(i);
			sl.setBoundingBox(); 
			k = 0;
			bb[k++][i] = sl.getX0(); bb[k++][i] = sl.getX1();
			bb[k++][i] = sl.getY0(); bb[k++][i] = sl.getY1();
			bb[k++][i] = sl.getZ0(); bb[k++][i] = sl.getZ1();
		}
		k = 0;
		double x0 = ArrayMath.min(bb[k++]);
		double x1 = ArrayMath.max(bb[k++]);
		double y0 = ArrayMath.min(bb[k++]);
		double y1 = ArrayMath.max(bb[k++]);
		double z0 = ArrayMath.min(bb[k++]);
		double z1 = ArrayMath.max(bb[k++]);
		
		setSize(2, 2, 2, x1-x0, y1-y0, z1-z0, x0, y0, z0);
	}
	public int getIUnit() 								{ return _iUnit; }

	public void setIUnit(int iUnit) 					{ _iUnit = iUnit; }

	public boolean isFlat() {
		for(int i=1; i<_layers.size()-1; i++) { if(!_layers.get(i).isFlat()) return false; }
		return true;
	}
	public boolean isParallelDipping() {
		Point3D norm = _layers.get(1).getPlaneNorm();
		if(norm.isVertical()) return false;
		for(int i=2; i<_layers.size()-1; i++) { 
			Point3D norm1 = _layers.get(i).getPlaneNorm();
			//System.out.println(norm1.cross(norm));
			if(!norm1.cross(norm).isZero()) return false;
		}
		return true;
	}
	public DipLayer1D toDipLayer1D() {
		DipLayer1D dlayer1D = new DipLayer1D(_layers.size());

		dlayer1D.setAzimuth(_azimuth);
		dlayer1D.setDip(_dip);

		for(int i=0; i<_layers.size(); i++) {

			SingleFlatLayer singleLayer = new SingleFlatLayer(_topDepth[i], _botDepth[i], "layer_"+i, i, 
					_layers.get(i).getVp(), _layers.get(i).getVs(), _layers.get(i).getDen(), 
					_layers.get(i).getDelta(), _layers.get(i).getEpsilon(), _layers.get(i).getGamma(), 
					_layers.get(i).getQp(), _layers.get(i).getQs(), _layers.get(i).getTheta(), _layers.get(i).getPhi());
			singleLayer.setSize(_layers.get(i).getNx(), _layers.get(i).getNy(), _layers.get(i).getNz(), 
					_layers.get(i).getDx(), _layers.get(i).getDy(), _layers.get(i).getDz(), 
					_layers.get(i).getX0(), _layers.get(i).getY0(), _layers.get(i).getZ0());	

			dlayer1D.getLayer().add(singleLayer);
		}
		dlayer1D.updateDepth();
		SingleFlatLayer lastLayer = dlayer1D.getLayer(_layers.size()-1);
		lastLayer.setBottomDepth(lastLayer.getTopDepth());
		dlayer1D.updateH();
		dlayer1D.setIUnit(getIUnit());
		
		return dlayer1D;
	}
	
	public double [] calPlaneNorm() { 
		double r = Math.sin(_dip);
		double z = Math.cos(_dip);

		double theSin = Math.sin(_azimuth+Math.PI);
		double theCos = Math.cos(_azimuth+Math.PI);

		double y = r*theSin;
		double x = r*theCos;

		return new double[]{x, y, z};
	}
	
	public Layer2D toLayer2D(double px0, double py0, double px1, double py1) {
		double [] norm = calPlaneNorm();
		double A = norm[0];
		double B = norm[1];
		double C = norm[2];		
		double D = 0;		
		double az = 0;
		double bz = 0;
		
		Layer2D layer2D = new Layer2D(_layers.size());
		for(int i=0; i<_layers.size(); i++) {  
			SingleLayer3D sl = _layers.get(i);
			if(i==0||i==_layers.size()-1||isFlat()) {
				az = _topDepth[i]; 
				bz = _topDepth[i]; 
			} else {
				D = -(A*0+B*0+C*_topDepth[i]);
				az = -(A*px0+B*py0+D)/C;
				bz = -(A*px1+B*py1+D)/C; 
			}
			
			ArrayList<Point2D> 	mp = new ArrayList<Point2D>(2);
			mp.add(new Point2D(px0, az));
			mp.add(new Point2D(px1, bz));
			//System.out.println(mp.get(0).getX()+" "+mp.get(0).getZ()+" "+mp.get(1).getX()+" "+mp.get(1).getZ());
			
			String name = sl.getLayerName();
			if(name==null) name = "layer_"+(i+1);
			SingleLayer2D singleLayer = new SingleLayer2D(name, sl.getId(), 
					sl.getVp(), sl.getVs(), sl.getDen(), 
					0, 0, 0, 0, 0, 0,  
					sl.getDelta(), sl.getEpsilon(), sl.getGamma(), 
					sl.getQp(), sl.getQs(), sl.getTheta(), sl.getPhi(),
					2, 2, px1-px0, py1-py0, px0, py0, null, mp, null);

			layer2D.getLayer().add(singleLayer); 
		}
		layer2D.setTopDepth(ArrayMath.copy(_topDepth));
		layer2D.setBotDepth(ArrayMath.copy(_botDepth));
		layer2D.setAzimuth(_azimuth);
		layer2D.setDip(_dip);
		layer2D.setIUnit(_iUnit);		

		return layer2D;
	}
	public float[] makeVerticalQuads(double px0, double py0, double px1, double py1) {
		double qx0 = 0.0;
		double qx1 = 0.0;
		double qy0 = 0.0;
		double qy1 = 0.0;
		double qz0 = _layers.get(0).getZ0();
		double qz1 = _layers.get(_layers.size()-1).getZ1();

		SingleLayer3D sl = _layers.get(0);
		double [] q = sl.intersection(px0, py0, px1, py1);

		if(q[0]<q[2]) {
			qx0=q[0]; qy0=q[1]; qx1=q[2]; qy1=q[3]; 
		} else {
			qx0=q[2]; qy0=q[3]; qx1=q[0]; qy1=q[1];
		}

		float[] xyz = new float[3*4];
		int i = 0;
		xyz[i++] = (float)qx0;  xyz[i++] = (float)qy0;  xyz[i++] = (float)qz0;
		xyz[i++] = (float)qx0;  xyz[i++] = (float)qy0;  xyz[i++] = (float)qz1;
		xyz[i++] = (float)qx1;  xyz[i++] = (float)qy1;  xyz[i++] = (float)qz1;
		xyz[i++] = (float)qx1;  xyz[i++] = (float)qy1;  xyz[i++] = (float)qz0;

		System.out.println(Arrays.toString(xyz));
		return xyz;
	}	
	public void addLayer(int index, SingleLayer3D veconLayer) {
		if(index<0) return;
		if(index>_layers.size()) return;
		_layers.add(index, veconLayer);
	}
	public void deleteLayer(int index) {
		if(index<0) return;
		_layers.remove(index);
	}
	public void deleteLayer(String layerName) 		{ deleteLayer(getLayerIndex(layerName)); }
	public void deleteLayer(boolean [] removable) 	{
		ArrayList<SingleLayer3D> 	veconLayer = new ArrayList<SingleLayer3D>();
		int k = 0;
		for(int i=0; i<_layers.size(); i++) {
			if(!removable[k]) {         
				veconLayer.add(getLayer(i));     
			} 
			k++;
		}
		_layers = veconLayer;
	}
	public void addLayer(double z) 		{ addFlatLayer(z); }
	public void addFlatLayer(double z) 	{ addLayer(1, z); }
	public void addCurveLayer(double z) { addLayer(2, z); }
	public void addLayer(int id, double z) {
		//		//double layerTop[] = getLayerTop(0.0, 0.0);
		//		double layerTop[] = getLayerTop(_layers.get(0).getCenterX(), _layers.get(0).getCenterY());
		//		int lastIndex = layerTop.length-1;
		//
		//		if(z<=layerTop[0]) {
		//			SingleLayer3D veconLayer = _layers.get(0).copy();
		//			String newName = veconLayer.getLayerName().trim()+"_copy";
		//			veconLayer.setLayerName(newName);
		//			veconLayer.setDepth((float)z);
		//			addLayer(0, veconLayer);
		//		} else if(z>=layerTop[lastIndex]) {
		//			SingleLayer3D veconLayer = _layers.get(lastIndex).copy();
		//			String newName = veconLayer.getLayerName().trim()+"_copy";
		//			veconLayer.setLayerName(newName);
		//			veconLayer.setDepth((float)z);
		//			addLayer(lastIndex+1, veconLayer);
		//		} else {
		//			int j1 = 0;
		//			int j2 = 0;
		//			for(int j=1; j<_layers.size(); j++) {
		//				if(z>layerTop[j-1]&&z<layerTop[j]) {
		//					j1 = j-1;
		//					j2 = j;
		//					break;
		//				}
		//			}
		//			if(j1==j2) return;
		//			//System.out.println("j1= "+j1+" j2="+j2);
		//			SingleLayer3D veconLayer1 = _layers.get(j1);
		//			SingleLayer3D veconLayer2 = _layers.get(j2);
		//			SingleLayer3D veconLayer = veconLayer1.copy();
		//			String newName = veconLayer.getLayerName().trim()+"_copy";
		//			veconLayer.setLayerName(newName);
		//			veconLayer.setDepth((float)z);
		//			//			if(isDepthValid(j1, j2, veconLayer)) {
		//			//				//System.out.println("j1= "+j1+" j2="+j2);
		//			//				addLayer(j2, veconLayer);
		//			//			} else {
		//			//				double ratio = (z-layerTop[j1])/(layerTop[j2]-layerTop[j1]);
		//			//				if(id==2) veconLayer.setDepth(veconLayer1, veconLayer2, ratio);
		//			//				//System.out.println("layer at "+z+" need mix" +" j1= "+j1+" j2="+j2+" zj1= "+ layerTop[j1]+" zj2="+layerTop[j2]+" ratio="+ratio);
		//			//				addLayer(j2, veconLayer);
		//			//			}
		//
		//
		//			if(id==1) addLayer(j2, veconLayer);
		//			if(id==2) {
		//				double ratio = (z-layerTop[j1])/(layerTop[j2]-layerTop[j1]);
		//				veconLayer.setDepth(veconLayer1, veconLayer2, ratio);
		//				addLayer(j2, veconLayer);
		//			}
		//		}
	}


	public double [] getRotationParameter() 		{ return _rp; }

	public int getNumOfBoundaries() 				{ return _layers.size(); }
	public SingleLayer3D getLayer(int index) 		{ return _layers.get(index); }
	public ArrayList<SingleLayer3D> getLayer() 		{ return _layers; }
	public boolean isDepthValid(int index1, int index2, SingleLayer3D veconLayer) {
		//		SingleLayer3D veconLayer1 = _layers.get(index1);
		//		SingleLayer3D veconLayer2 = _layers.get(index2);
		//		int nxy = veconLayer.getNx()*veconLayer.getNy();
		//		for(int j=0; j<nxy; j++) {
		//			if(veconLayer.getDepth(j)<=veconLayer1.getDepth(j) || veconLayer.getDepth(j)>=veconLayer2.getDepth(j) ) {
		//				//System.out.println("dp "+veconLayer._dp[j]+" dp1= "+veconLayer1._dp[j] +" dp2= "+veconLayer2._dp[j]);
		//				//System.exit(-1);
		//				return false;
		//			}
		//		}
		return true;
	}
	public double [] getLayerTops() { return getLayerTop(_layers.get(0).getCenterX(), _layers.get(0).getCenterY()); }
	public double getLayerTop(int index, double x, double y) {
		return _layers.get(index).getDepth(x, y);
	}
	public double [] getLayerTop(double x, double y) {
		double layerTop[] = new double[_layers.size()];
		for(int j=0; j<_layers.size(); j++) {
			layerTop[j] = _layers.get(j).getDepth(x, y);
		}
		return layerTop;
	}
	public int getLayerIndex(String name) {
		for(int i=0; i<_layers.size(); i++) {
			if(_layers.get(i).getLayerName().equalsIgnoreCase(name)) { return i; }
		}	
		return 0;
	}
	public void clip() {
		int nLayers = _layers.size();
		for(int i=1; i<nLayers-1; i++) {
			SingleLayer3D p1 = _layers.get(i-1);
			SingleLayer3D p2 = _layers.get(i);
			SingleLayer3D p3 = _layers.get(i+1);

			float dp1 = 0.0f;
			float dp2 = 0.0f;
			float dp3 = 0.0f;
			for(int j=0; j<p1.getNy(); j++) {
				for(int k=0; k<p1.getNx(); k++){
					dp1 = p1.getDepth(j, k);
					dp2 = p2.getDepth(j, k);
					dp3 = p3.getDepth(j, k);
					if(dp2<dp1) dp2 = dp1;
					if(dp2>dp3) dp2 = dp3;
					p2.setDepth(j, k, dp2);
				}
			}
		}
	}

	//for parallelDipping case
	public Layer3D rotateToFlat(int iDir) {
		System.out.println(isFlat()+" "+isParallelDipping()+" "+_layers.get(1).getPlaneNorm());
		if(_rp==null) _rp = getRotate();
		return rotate(_rp[0], _rp[1], _rp[2], iDir, _rp[3], _rp[4]);
	}
	public double [] getRotate() {
		SingleLayer3D sl = getLayer(1).copy();
		double dipX = Math.atan((sl.getDepth(1,0)-sl.getDepth(0,0))/(sl.getX1()-sl.getX0()));

		double dx = sl.getX0();
		double dy = sl.getY0();
		double dz = sl.getDepth(0, 0);
		sl.shift(-dx, -dy);
		sl.shift(-(float)dz);

		Point3D [] P = sl.genPoints();
		//shiftPoints
		for(int j=0; j<P.length; j++) {
			P[j].shift(-dx, -dy, -dz);
		}

		double [][] Ry = new double[][] {
				{Math.cos(dipX), 0, Math.sin(dipX)},
				{0, 1, 0},
				{-Math.sin(dipX), 0, Math.cos(dipX)}
		};
		for(int j=0; j<P.length; j++) {
			P[j].multiply(Ry);
		}
		double dipY = Math.atan((P[2].getD()-P[0].getD())/(P[2].getN()-P[0].getN()));
		//double dipY1 = Math.atan((sl.getDepth(0,1)-sl.getDepth(0,0))/(sl.getY1()-sl.getY0()));
		//System.out.println(dipX*180/3.1415926+" dipY1="+dipY1*180/3.1415926+" dipY="+dipY*180/3.1415926);
		return new double [] {getLayer(0).getX0(), getLayer(0).getY0(), getLayer(0).getDepth(0, 0), dipX, dipY};
	}

	public Layer3D rotate(double dx, double dy, double dz, int iDir, double dipX, double dipY) {
		System.out.println(dx+" dy="+dy+" dz="+dz+" dipX="+dipX*180/3.1415926+" dipY="+dipY*180/3.1415926);
		double [][] rm = genRotationMatrix(iDir, dipX, dipY);
		//double [][] R1 = getRotateMatrix(-iDir, dipX, dipY);

		Layer3D layer3D = new Layer3D();
		layer3D._layers = new ArrayList<SingleLayer3D>();
		Point3D [][] Q = new Point3D[_layers.size()][];
		for(int i=0; i<_layers.size(); i++) {
			Point3D [] P = _layers.get(i).genPoints();
			for(int j=0; j<P.length; j++) { System.out.print(P[j].toString(3)+" "); }
			System.out.println(" G "+_layers.get(i).getId());
			//shiftPoints
			for(int j=0; j<P.length; j++) { P[j].shift(-dx, -dy, -dz); }
			for(int j=0; j<P.length; j++) { P[j].multiply(rm); }
			for(int j=0; j<P.length; j++) { P[j].shift(dx, dy, dz); }
			for(int j=0; j<P.length; j++) { System.out.print(P[j].toString(3)+" "); }
			System.out.println(" R "+_layers.get(i).getId());
			if(i==0) for(int j=0; j<P.length; j++) { P[j].setD(P[0].getD()); }
			if(i==_layers.size()-1) for(int j=0; j<P.length; j++) { P[j].setD(P[0].getD()); }
			layer3D._layers.add(_layers.get(i).update(P));
			Q[i] = P;
		}

		double x0 = getExtreme(0, 1, Q);
		double y0 = getExtreme(0, 2, Q);
		double x1 = getExtreme(1, 1, Q);
		double y1 = getExtreme(1, 2, Q);
		for(int i=0; i<_layers.size(); i++) {
			layer3D._layers.get(i).setSize(2, 2, 2, x1-x0, y1-y0, 1, x0, y0, 0);
		}
		layer3D._rp = _rp;
		return layer3D;
	}

	public double [][] genRotationMatrix(int iDir, double dipX, double dipY) {
		dipX = iDir*dipX;
		dipY = iDir*dipY;
		double [][] Ry = new double[][] {
				{Math.cos(dipX), 0, Math.sin(dipX)},
				{0, 1, 0},
				{-Math.sin(dipX), 0, Math.cos(dipX)}
		};
		double [][] Rx = new double[][] {
				{1, 0, 0},
				{0, Math.cos(-dipY), -Math.sin(-dipY)},
				{0, Math.sin(-dipY), Math.cos(-dipY)}
		};		

		//Y then X
		double [][] R = new double[3][3];
		double sum = 0.0;
		for(int i=0; i<3; i++) {
			for(int j=0; j<3; j++) {
				sum = 0.0;
				if(iDir==1) for(int k=0; k<3; k++) sum += Rx[i][k]*Ry[k][j];
				else for(int k=0; k<3; k++) sum += Ry[i][k]*Rx[k][j];
				R[i][j] = sum;
			}
		}

		return R;
	}
	private double getExtreme(int id, int iComp, Point3D [][] P) {
		double min = 1.0e10;
		double max = -min;
		if(id==0) {
			if(iComp==1) {
				for(int i=0; i<P.length; i++) {
					for(int j=0; j<P[i].length; j++)  min = min<P[i][j].getE()?min:P[i][j].getE();
				}
			} else if(iComp==2) {
				for(int i=0; i<P.length; i++) {
					for(int j=0; j<P[i].length; j++)  min = min<P[i][j].getN()?min:P[i][j].getN();
				}
			} else if(iComp==3) {
				for(int i=0; i<P.length; i++) {
					for(int j=0; j<P[i].length; j++)  min = min<P[i][j].getD()?min:P[i][j].getD();
				}
			}
			return min;
		} else {
			if(iComp==1) {
				for(int i=0; i<P.length; i++) {
					for(int j=0; j<P[i].length; j++) max = max>P[i][j].getE()?max:P[i][j].getE();
				}
			} else if(iComp==2) {
				for(int i=0; i<P.length; i++) {
					for(int j=0; j<P[i].length; j++) max = max>P[i][j].getN()?max:P[i][j].getN();
				}
			} else if(iComp==3) {
				for(int i=0; i<P.length; i++) {
					for(int j=0; j<P[i].length; j++) max = max>P[i][j].getD()?max:P[i][j].getD();
				}
			}
			return max;
		}
	}
	public void shift(double dx, double dy) {
		for(int i=0; i<_layers.size(); i++) {
			_layers.get(i).shift(dx, dy);
		}
	}
	public void shift(double dx, double dy, double dz) {
		for(int i=0; i<_layers.size(); i++) {
			shift(i, dx, dy, dz);
		}
	}
	public void shift(int index, double dx, double dy, double dz) {
		_layers.get(index).shift(dx, dy);
		_layers.get(index).shift((float)dz);
	}
	
	
	private VeconModel toVeconModel(int iProperty) {
		SingleLayer3D sl = _layers.get(0);
		double x0 	= sl.getX0();
		double dx 	= sl.getDx();
		double y0 	= sl.getY0();
		double dy 	= sl.getDy();
		double z0 	= sl.getZ0();
		double dz 	= sl.getDz();
		int nx 		= sl.getNx();
		int ny 		= sl.getNy();
		int nz 		= sl.getNz();

		//System.out.println(sl.toString(1));
		return toVeconModel(iProperty, nx, ny, nz, dx, dy, dz, x0, y0, z0);
	}
	public VeconModel toVeconModel(int iProperty, int nz) {
		SingleLayer3D sl = _layers.get(0);
		double x0 = getX0();
		double x1 = getX1();
		double y0 = getY0();
		double y1 = getY1();
		double z0 = getZ0();
		double z1 = getZ1();
		int nx 		= sl.getNx();
		int ny 		= sl.getNy();
		double dx = (x1-x0)/(nx-1);
		double dy = (y1-y0)/(ny-1);
		double dz = (z1-z0)/(nz-1);
		
		return toVeconModel(iProperty, nx, ny, nz, dx, dy, dz, x0, y0, z0);
	}
	
	public VeconModel toVeconModel(int iProperty, double dx, double dy, double dz) {
		double x0 = getX0();
		double x1 = getX1();
		double y0 = getY0();
		double y1 = getY1();
		double z0 = getZ0();
		double z1 = getZ1();

		int nx = (int)((x1-x0)/dx+1);
		int ny = (int)((y1-y0)/dy+1);
		int nz = (int)((z1-z0)/dz+1);

		return toVeconModel(iProperty, nx, ny, nz, dx, dy, dz, x0, y0, z0);
	}
	public VeconModel toVeconModel(int iProperty, 
			int nx, int ny, int nz, double dx, double dy, double dz, double x0, double y0, double z0) {

		float [] v = new float [nx*ny*nz];
		VeconModel veconModel = new VeconModel(getIUnit(), 1, 
				nx, ny, nz, dx, dy, dz, x0, y0, z0, v);

		for(int i=0; i<_layers.size(); i++) {
			SingleLayer3D sl = getLayer(i).copy();
			//System.out.println(sl.toString(1));
			sl.regrid(nx, ny, dx, dy, x0, y0);
			//sl.printDepth();

			double az 	= 0.0;
			float a 	= 0;
			int kz 		= 0;

			for(int ix=0; ix<nx; ix++) { 
				for(int iy=0; iy<ny; iy++) {
					az = (double)sl.getDepth(ix, iy);
					kz = (int)((az-z0)/dz);
					a = (float)sl.getLayerProperty(iProperty);
					//System.out.println("kz="+kz);
					for(int iz=kz; iz<nz; iz++) { veconModel.setData(ix, iy, iz, a); }
				}
			}
		} 
		return veconModel;
	}

	
	public ArrayList<SingleLayer3D> readLayer(int iType, String selectedFileName) {
		ArrayList<SingleLayer3D> veconLayer = new ArrayList<SingleLayer3D>();

		try{
			BufferedReader reader = new BufferedReader(new FileReader(selectedFileName));

			int nx = 2;
			int ny = 2;
			double x0 = 0;
			double y0 = 0;
			double dx = 1;
			double dy = 1;

			double vpGradientX = 0.0;
			double vpGradientY = 0.0;
			double vpGradientZ = 0.0;
			double vsGradientX = 0.0;
			double vsGradientY = 0.0;
			double vsGradientZ = 0.0;

			double vp = 0.0;
			double vs = 0.0;
			double vpToVs = 0.0;
			double den = 0.0;
			double delta = 0.0;
			double epsilon = 0.0;
			double gamma = 0.0;
			double qp = 0.0;
			double qs = 0.0;
			double theta = 0.0;
			double phi = 0.0;

			double delta1 = 0.0;
			double epsilon1 = 0.0;
			double gamma1 = 0.0;
			double delta3 = 0.0;

			String layerName = " ";

			String line=null;
			StringTokenizer st = null;
			if(iType==0) { //reserve
				
			} else if(iType==1) { //ms vecon format
				line=reader.readLine();
				st = new StringTokenizer(line, " ,\t");
				int nLayers = Integer.parseInt(st.nextToken());
				for(int i=0; i<nLayers; i++) {
					layerName = reader.readLine();

					//line vp
					line=reader.readLine(); 	st = new StringTokenizer(line, " ,\t");
					vp = Double.parseDouble(st.nextToken());
					//vp gradient
					//line=reader.readLine(); 	st = new StringTokenizer(line, " ,\t");
					//if(st.hasMoreTokens()) 
					vpGradientX = Double.parseDouble(st.nextToken());
					vpGradientY = Double.parseDouble(st.nextToken());
					vpGradientZ = Double.parseDouble(st.nextToken());
					//line vp/vs
					line=reader.readLine(); 	st = new StringTokenizer(line, " ,\t");
					vs = Double.parseDouble(st.nextToken());
					vpToVs = 0.0;
					if(vs>0.0) vpToVs = vp/vs;
					vsGradientX = Double.parseDouble(st.nextToken());
					vsGradientY = Double.parseDouble(st.nextToken());
					vsGradientZ = Double.parseDouble(st.nextToken());
					//line den
					line=reader.readLine(); 	st = new StringTokenizer(line, " ,\t");
					den = Double.parseDouble(st.nextToken());
					//line anisotropy
					line=reader.readLine(); st = new StringTokenizer(line, " ,\t");
					epsilon = Double.parseDouble(st.nextToken());
					delta = Double.parseDouble(st.nextToken());
					gamma = Double.parseDouble(st.nextToken());
					qp = Double.parseDouble(st.nextToken());
					qs = Double.parseDouble(st.nextToken());
					theta = Double.parseDouble(st.nextToken());
					phi = Double.parseDouble(st.nextToken());

					line=reader.readLine(); 	st = new StringTokenizer(line, " ,\t");
					nx = Integer.parseInt(st.nextToken());
					//line=reader.readLine(); 	st = new StringTokenizer(line, " ,\t");
					ny = Integer.parseInt(st.nextToken());
					//line=reader.readLine(); 	st = new StringTokenizer(line, " ,\t");
					x0 = Double.parseDouble(st.nextToken());
					//line=reader.readLine(); 	st = new StringTokenizer(line, " ,\t");
					y0 = Double.parseDouble(st.nextToken());
					//line=reader.readLine(); 	st = new StringTokenizer(line, " ,\t");
					dx = Double.parseDouble(st.nextToken());
					//line=reader.readLine(); 	st = new StringTokenizer(line, " ,\t");
					dy = Double.parseDouble(st.nextToken());


					SingleLayer3D veconLayer1 = new SingleLayer3D(layerName, i, vp, vs, den, 
							vpGradientX, vpGradientY, vpGradientZ, vsGradientX, vsGradientY, vsGradientZ, 
							delta, epsilon, gamma, qp, qs, theta, phi, 
							nx, ny, dx, dy, x0, y0, null);

					for(int j=0; j<ny; j++) {
						for(int k=0; k<nx; k++){
							line=reader.readLine(); 	st = new StringTokenizer(line, " ,\t");
							veconLayer1.setDepth(j, k, Float.parseFloat(st.nextToken()) );
						}
					}

					//System.out.println(veconLayer1.toString(2));
					veconLayer.add(veconLayer1);
				}
			} else if(iType==2||iType==3) {
				double x2=0, y2=0, z1=0, z2=0, z3=0, z4=0;
				line = reader.readLine(); // 1st line Title
				line = reader.readLine(); // second line Title
				st = new StringTokenizer(line, " ,\t");
				if(st.countTokens()<=1) return null;

				x0 = Double.parseDouble(st.nextToken().trim());
				x2 = Double.parseDouble(st.nextToken().trim());
				if(iType==3) {
					y0 = Double.parseDouble(st.nextToken().trim());
					y2 = Double.parseDouble(st.nextToken().trim());
				} else {
					y0 = 0.0;
					y2 = 1.0;
				}

				nx = 2;
				ny = 2;
				dx = x2-x0;
				dy = y2-y0;

				System.out.println("x0="+x0+" x2="+x2+" y0="+y0+" y2="+y2);

				int k = 0;
				while((line = reader.readLine())!=null) {
					st = new StringTokenizer(line, " ,\t");
					if(st.countTokens()<=1) continue;

					z1 = Double.parseDouble(st.nextToken().trim());
					z2 = Double.parseDouble(st.nextToken().trim());
					if(iType==3) {
						z3 = Double.parseDouble(st.nextToken().trim());
						//z4 = Double.parseDouble(st.nextToken().trim());						
					} else {
						z3 = z1;
						z4 = z2;	
					}
					vp = Double.parseDouble(st.nextToken().trim());
					if(st.hasMoreTokens()) vs = Double.parseDouble(st.nextToken().trim());
					if(st.hasMoreTokens()) den = Double.parseDouble(st.nextToken().trim());

					if(st.hasMoreTokens()) delta = Double.parseDouble(st.nextToken().trim());
					if(st.hasMoreTokens()) epsilon = Double.parseDouble(st.nextToken().trim());
					if(st.hasMoreTokens()) gamma = Double.parseDouble(st.nextToken().trim());

					if(st.hasMoreTokens()) theta = Double.parseDouble(st.nextToken().trim());
					if(st.hasMoreTokens()) phi = Double.parseDouble(st.nextToken().trim());
					if(st.hasMoreTokens()) qp = Double.parseDouble(st.nextToken().trim());
					if(st.hasMoreTokens()) qs = Double.parseDouble(st.nextToken().trim());

					if(st.hasMoreTokens()) delta1 = Double.parseDouble(st.nextToken().trim());
					if(st.hasMoreTokens()) epsilon1 = Double.parseDouble(st.nextToken().trim());
					if(st.hasMoreTokens()) gamma1 = Double.parseDouble(st.nextToken().trim());
					if(st.hasMoreTokens()) delta3 = Double.parseDouble(st.nextToken().trim());
					vpToVs = 0.0;
					if(vs!=0.0) vpToVs = vp/vs;

					SingleLayer3D layer = new SingleLayer3D("layer_"+k, k++, vp, vs, den, 
							vpGradientX, vpGradientY, vpGradientZ, vsGradientX, vsGradientY, vsGradientZ, 
							delta, epsilon, gamma, qp, qs, theta, phi, 
							delta1, epsilon1, gamma1, delta3, 
							nx, ny, dx, dy, x0, y0, null);

					if(iType==3) {
						if(z1==z2 && z1==z3)  z4 = z3;  
						else  z4 = layer.getDepthOnPlane(new Point3D(x0, y0, z1), new Point3D(x2, y0, z2), 
								new Point3D(x0, y2, z3), x2, y2);

					} else {
						z3 = z1;
						z4 = z2;	
					}
					float [][] dp = new float[][]{{(float)z1, (float)z2}, {(float)z4, (float)z3}};
					//for(int i=0; i<ny; i++) { for(int j=0; j<nx; j++) { } }
					layer.setLayerDepth(dp);
					//System.out.println(veconLayer1.toString(2));
					veconLayer.add(layer);
				}
			}
			else if(iType==4) {
				line=reader.readLine();
				st = new StringTokenizer(line, " ,\t");
				int nLayers = Integer.parseInt(st.nextToken());
				for(int i=0; i<nLayers; i++) {
					layerName = reader.readLine();
					//line vp
					line=reader.readLine(); 	st = new StringTokenizer(line, " ,\t");
					vp = Double.parseDouble(st.nextToken());
					//vp gradient
					line=reader.readLine(); 	st = new StringTokenizer(line, " ,\t");
					vpGradientZ = Double.parseDouble(st.nextToken());
					if(st.hasMoreTokens()) vpGradientX = Double.parseDouble(st.nextToken());
					if(st.hasMoreTokens()) vpGradientY = Double.parseDouble(st.nextToken());
					//line vp/vs
					line=reader.readLine(); 	st = new StringTokenizer(line, " ,\t");
					vpToVs = Double.parseDouble(st.nextToken());
					vs = vp/vpToVs;
					if(st.hasMoreTokens()) vsGradientZ = Double.parseDouble(st.nextToken());
					if(st.hasMoreTokens()) vsGradientX = Double.parseDouble(st.nextToken());
					if(st.hasMoreTokens()) vsGradientY = Double.parseDouble(st.nextToken());
					//line den
					line=reader.readLine(); 	st = new StringTokenizer(line, " ,\t");
					den = Double.parseDouble(st.nextToken());
					//line anisotropy
					//					line=reader.readLine(); st = new StringTokenizer(line, " ,\t");
					//					epsilon = Double.parseDouble(st.nextToken());
					//					delta = Double.parseDouble(st.nextToken());
					//					gamma = Double.parseDouble(st.nextToken());
					//					if(st.hasMoreTokens()) qp = Double.parseDouble(st.nextToken());
					//					if(st.hasMoreTokens()) qs = Double.parseDouble(st.nextToken());
					//					if(st.hasMoreTokens()) theta = Double.parseDouble(st.nextToken());
					//					if(st.hasMoreTokens()) phi = Double.parseDouble(st.nextToken());

					line=reader.readLine(); 	st = new StringTokenizer(line, " ,\t");
					nx = Integer.parseInt(st.nextToken());
					line=reader.readLine(); 	st = new StringTokenizer(line, " ,\t");
					ny = Integer.parseInt(st.nextToken());
					line=reader.readLine(); 	st = new StringTokenizer(line, " ,\t");
					x0 = Double.parseDouble(st.nextToken());
					line=reader.readLine(); 	st = new StringTokenizer(line, " ,\t");
					y0 = Double.parseDouble(st.nextToken());
					line=reader.readLine(); 	st = new StringTokenizer(line, " ,\t");
					dx = Double.parseDouble(st.nextToken());
					line=reader.readLine(); 	st = new StringTokenizer(line, " ,\t");
					dy = Double.parseDouble(st.nextToken());


					SingleLayer3D veconLayer1 = new SingleLayer3D(layerName, i, vp, vs, den, 
							vpGradientX, vpGradientY, vpGradientZ, vsGradientX, vsGradientY, vsGradientZ, 
							delta, epsilon, gamma, qp, qs, theta, phi, 
							nx, ny, dx, dy, x0, y0, null);

					for(int j=0; j<ny; j++) {
						for(int k=0; k<nx; k++){
							line=reader.readLine(); 	st = new StringTokenizer(line, " ,\t");
							veconLayer1.setDepth(j, k, Float.parseFloat(st.nextToken()) );
						}
					}

					//System.out.println(veconLayer1.toString(2));
					veconLayer.add(veconLayer1);
				}
			}
			reader.close();
		} catch (IOException ioexception) {
			JOptionPane.showMessageDialog(null, "\nFile Format is not right!", 
					"Error", JOptionPane.ERROR_MESSAGE);
		}	
		return veconLayer;
	}
	public int getNumOfValidLines(String selectedFileName) {
		int nLines = 0;
		try{
			BufferedReader reader = new BufferedReader(new FileReader(selectedFileName));
			String line = reader.readLine(); // 1st line Title
			StringTokenizer st = null;
			while((line = reader.readLine())!=null) {
				st = new StringTokenizer(line, " ,\t");
				if(st.countTokens()>1) nLines++;
			}
			reader.close();
		} catch (IOException ioexception) {
			JOptionPane.showMessageDialog(null, "\nFile Format is not right!", 
					"Error", JOptionPane.ERROR_MESSAGE);
		}
		return nLines;
	}

	public void writeLayer(String selectedFileName) {
		try{
			BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(selectedFileName, false));
			bufferedWriter.write(_layers.size()+" 0.0 0 \n");

			for(int i=0; i<_layers.size(); i++) {
				SingleLayer3D sl = _layers.get(i);
				bufferedWriter.write(sl.toString(0)+"\n");
				for(int j=0; j<sl.getNy(); j++) {
					for(int k=0; k<sl.getNx(); k++){
						bufferedWriter.write(sl.getDepth(j, k)+"\n");
					}
				}
			}
			bufferedWriter.close();
		} catch (IOException ioexception) {
			JOptionPane.showMessageDialog(null, "\nFile Format is not right!", 
					"Error", JOptionPane.ERROR_MESSAGE);
		}
	}

	public String toString() {
		return toString(1);
	}
	public String toString(int id) {
		String b = String.format("nLayers=%d",  _layers.size());
		String a = String.format(" "); 

		if(id==1) {
			for(int i=0; i<_layers.size(); i++) {
				a = String.format("layer=%d", i);
				b = new String(b.concat("\n"+a));
				a = String.format("%s", _layers.get(i).toString(1));
				b = new String(b.concat("\n"+a));
			} 
		}
		if(id==4) {
			for(int i=0; i<_layers.size(); i++) {
				a = String.format("layer=%d", i);
				b = new String(b.concat("\n"+a));
				a = String.format("%s", _layers.get(i).toString(4));
				b = new String(b.concat("\n"+a));
			} 
		}

		return b;
	}
	public String[] getLayerNames() {
		String []  layerNames = new String[_layers.size()];
		for(int i=0; i<_layers.size(); i++) {
			layerNames[i] = _layers.get(i).getLayerName()+"";
		}
		return layerNames;
	}


}
