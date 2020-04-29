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
import com.geohammer.core.planarmodel.SingleLayer2D;

import edu.mines.jtk.util.ArrayMath;

public class Layer2D implements LayersI {
	private double [] 	_anchors 	= null; //cut line from 3D
	public int 			_iUnit 		= 2;		//1 m; 2 ft; 3 km
	public ArrayList<SingleLayer2D> _layers = null;
	
	private double 		_azimuth 			= 0.0; 	
	private double 		_dip 				= 0.0;	
	private double [] 	_topDepth 			= null;
	private double [] 	_botDepth 			= null; 
	
	public void setAzimuth(double azimuth) 			{ _azimuth = azimuth; }
	public void setDip(double dip) 					{ _dip = dip; }
	
	public void setTopDepth(double [] topDepth) 	{ _topDepth = topDepth; }
	public void setBotDepth(double [] botDepth) 	{ _botDepth = botDepth; }
	
	public double getAzimuth( ) 					{ return _azimuth; }
	public double getDip( ) 						{ return _dip; }
	
	public Layer2D( ){ }
	public Layer2D(int nLayers) {_layers = new ArrayList<SingleLayer2D>(nLayers); }
	public Layer2D(String selectedFileName)	{ this(1, selectedFileName ); }
	public Layer2D(int iType, String selectedFileName)	{
		_layers = readLayer(iType, selectedFileName );
		//System.out.println(toString());
	}

	public Layer2D(String [] layerName, int [] id, double [] vp, double [] vs, double [] den,
			double [] vpGradientX, double [] vpGradientZ, double [] vsGradientX, double [] vsGradientZ, 
			double [] delta, double [] epsilon, double [] gamma, double [] qp, double [] qs, double [] theta, double [] phi,
			double [] dp, double x0, double x1, double dx) {
		_layers = new ArrayList<SingleLayer2D>();
		for(int i=0; i<vp.length; i++) {
			ArrayList<Point2D> 	mp = new ArrayList<Point2D>(2);
			mp.add(new Point2D(x0, dp[i]));
			mp.add(new Point2D(x1, dp[i]));
			SingleLayer2D singleLayer = new SingleLayer2D(layerName[i], id[i], vp[i], vs[i], den[i], 
					vpGradientX[i], vpGradientZ[i], vsGradientX[i], vsGradientZ[i],  
					delta[i], epsilon[i], gamma[i], qp[i], qs[i], theta[i], phi[i],
					mp, null);
			singleLayer.setCurve();
			_layers.add(singleLayer);
		}
	}
	public Layer2D(String layerName, int id, double vp, double vs, double den,
			double vpGradientX, double vpGradientZ, double vsGradientX, double vsGradientZ,  
			double delta, double epsilon, double gamma, double qp, double qs, double theta, double phi,
			ArrayList<Point2D> 	mp) {

		_layers = new ArrayList<SingleLayer2D>();
		SingleLayer2D singleLayer = new SingleLayer2D(layerName, id, vp, vs, den, 
				vpGradientX, vpGradientZ, vsGradientX, vsGradientZ,  
				delta, epsilon, gamma, qp, qs, theta, phi,
				mp, null);
		_layers.add(singleLayer); 
	}

	public Layer2D copy() {
		Layer2D layer2D = new Layer2D();
		layer2D._layers = new ArrayList<SingleLayer2D>();

		for(int i=0; i<_layers.size(); i++) layer2D._layers.add(_layers.get(i).copy());
		
		layer2D.setTopDepth(ArrayMath.copy(_topDepth));
		layer2D.setBotDepth(ArrayMath.copy(_botDepth));
		layer2D.setAzimuth(_azimuth);
		layer2D.setDip(_dip);
		layer2D.setIUnit(_iUnit);
		
		return layer2D;
	}
	
	public void convertUnitSystem(double velScalor, double denScalor) {
		for(int i=0; i<_layers.size(); i++) { 
			SingleLayer2D sl = _layers.get(i);
			sl.convertUnitSystem(velScalor, denScalor);
		}
		for(int i=0; i<_layers.size(); i++) {
			_topDepth[i] *= velScalor;
			_botDepth[i] *= velScalor;
		}
	}
	
	public DipLayer1D toDipLayer1D() {
		DipLayer1D dlayer1D = new DipLayer1D(_layers.size());
	
		dlayer1D.setAzimuth(_azimuth);
		dlayer1D.setDip(_dip);
		
		for(int i=0; i<_layers.size(); i++) {
			
			SingleFlatLayer singleLayer = new SingleFlatLayer(_topDepth[i], _botDepth[i]+1, "layer_"+i, i, 
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
		
		return dlayer1D;
	}
	
	public FlatLayer1D toFlatLayer1D(boolean archorAtLeft) {
		FlatLayer1D flayer1D = new FlatLayer1D(_layers.size());
		flayer1D.setAnchors(getAnchors());
		
		for(int i=0; i<_layers.size(); i++) {  
			SingleLayer2D sl = _layers.get(i);
			
			SingleFlatLayer layer = new SingleFlatLayer();			
			for(int j=0; j<100; j++) {
				layer.setLayerProperty(j, sl.getLayerProperty(j));
			}
			layer.setLayerName(sl.getLayerName());
			layer.setId(sl.getId());
			
			ArrayList<Point2D> mp = sl.getMp();
			if(archorAtLeft) layer.setTopDepth(mp.get(0).getD());
			else layer.setTopDepth(mp.get(mp.size()-1).getD());
			flayer1D.getLayer().add(layer); 
		}
		flayer1D.setIUnit(_iUnit);
		flayer1D.updateDepth();
		flayer1D.updateH();
		return flayer1D;
	}
	
	public Vecon2D toVecon2D(int iProperty) {
		SingleLayer2D sl = _layers.get(0);
		double x0 	= sl.getX0();
		double dx 	= sl.getDx();
		double y0 	= sl.getY0();
		double dy 	= sl.getDy();
		double z0 	= sl.getZ0();
		double dz 	= sl.getDz();
		int nx 		= sl.getNx();
		int ny 		= sl.getNy();
		int nz 		= sl.getNz();

		return toVecon2D(iProperty, nx, nz, dx, dz, x0, z0);
	}

	public Vecon2D toVecon2D(int iProperty, double dx, double dz) {
		double x0 = getX0();
		double x1 = getX1();
		double z0 = getZ0();
		double z1 = getZ1();
		
		int nx = (int)((x1-x0)/dx+1);
		int nz = (int)((z1-z0)/dz+1);
		
		return toVecon2D(iProperty, nx, nz, dx, dz, x0, z0);
	}
	public Vecon2D toVecon2D(int iProperty, 
			int nx, int nz, double dx, double dz, double x0, double z0) {
		float [][] v = new float [nx][nz];
		Vecon2D vecon2D = new Vecon2D(getIUnit(), 0, nx, nz, dx, dz, x0, z0, v);
		float a = 0;
		for(int i=0; i<_layers.size(); i++) {  
			SingleLayer2D sl = _layers.get(i);
			sl.setCurve(dx);
			SingleCurve sc = sl.getCurve();
			for(int ix=0; ix<nx; ix++) {
				double x = x0+ix*dx;
				double z = sc.getPointZ(x);
				int iz = (int)((z-z0)/dz);
								
				a = (float)sl.getLayerProperty(iProperty);
				
				for(int j=iz; j<v[0].length; j++) { v[ix][j] = a; }
			} 
		}
		
		return vecon2D;
	}

	
	public Layer3D toLayer3D(double y0, double y1) {
		Layer3D layer3D = new Layer3D(_layers.size());
		int nx = 2;
		int ny = 2;
		double x0 = getX0();
		double x1 = getX1();
		for(int i=0; i<_layers.size(); i++) {  
			SingleLayer2D sl = _layers.get(i);
			float [][] dp = new float[][]{{(float)sl.getMasterZ(0), (float)sl.getMasterZ(1)}, 
					{(float)sl.getMasterZ(0), (float)sl.getMasterZ(1)}};
			SingleLayer3D layer = new SingleLayer3D(sl.getLayerName(), sl.getId(), sl.getVp(), sl.getVs(), sl.getDen(), 
					sl.getVpGradientX(), sl.getVpGradientY(), sl.getVpGradientZ(), 
					sl.getVsGradientX(), sl.getVsGradientY(), sl.getVsGradientZ(),  
					sl.getDelta(), sl.getEpsilon(), sl.getGamma(), sl.getQp(), sl.getQs(), sl.getTheta(), sl.getPhi(),
					nx, ny, x1-x0, y1-y0, x0, y0, dp);
			layer3D.getLayer().add(layer); 
		}
	
		return layer3D;
	}

	public double getX0() 								{ return getLayer(0).getMasterX(0);}
	public double getX1() 								{ return getLayer(0).getMasterX(getLayer(0).getMp().size()-1);}
	public double getZ0() 								{ return getLayer(0).getMasterZ(0);}
	public double getZ1() 								{ return getLayer(_layers.size()-1).getMasterZ(0);}
	
	public double [] getAnchors() 						{ return _anchors; }
	public int getIUnit() 								{ return _iUnit; }

	public void setAnchors(double [] anchors) 			{ _anchors = anchors; }
	public void setIUnit(int iUnit) 					{ _iUnit = iUnit; }
	
	public void addLayer(int index, SingleLayer2D veconLayer) {
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
		ArrayList<SingleLayer2D> 	veconLayer = new ArrayList<SingleLayer2D>();
		int k = 0;
		for(int i=0; i<_layers.size(); i++) {
			if(!removable[k]) {         
				veconLayer.add(getLayer(i));     
			} 
			k++;
		}
		_layers = veconLayer;
	}
	public void addLayer(int id, double x, double z) {
		double layerTop[] = getLayerTop(x);
		int lastIndex = layerTop.length-1;

		if(z<=layerTop[0]) {
			SingleLayer2D sl = _layers.get(0).copy();
			sl.setLayerName(sl.getLayerName().trim()+"_copy");
			sl.setCurve();
			sl.moveTo(z);
			addLayer(0, sl);
		} else if(z>=layerTop[lastIndex]) {
			SingleLayer2D sl = _layers.get(lastIndex).copy();
			sl.setLayerName(sl.getLayerName().trim()+"_copy");
			sl.setCurve();
			sl.moveTo(z);
			addLayer(lastIndex+1, sl);
		} else {
			int j1 = 0;
			int j2 = 0;
			for(int j=1; j<_layers.size(); j++) {
				if(z>layerTop[j-1]&&z<layerTop[j]) {
					j1 = j-1;
					j2 = j;
					break;
				}
			}
			if(j1==j2) return;
			SingleLayer2D sl = _layers.get(j1).copy();
			sl.setLayerName(sl.getLayerName().trim()+"_copy");
			sl.setCurve();
			if(id==1) addLayer(j1, sl);
			else if(id==2) {
				double ratio = (z-layerTop[j1])/(layerTop[j2]-layerTop[j1]);
				for(int i=0; i<sl.getMp().size(); i++) {
					double ax = sl.getMasterX(i);
					double az = _layers.get(j1).getCurve().getPointZ(ax) +
							ratio*(_layers.get(j2).getCurve().getPointZ(ax)-_layers.get(j1).getCurve().getPointZ(ax));
					sl.moveTo(i, ax, az);
				}
				addLayer(j1, sl);
			} else { }
		}
	}
	
	public int getNumOfBoundaries() 				{ return _layers.size(); }
	public SingleLayer2D getLayer(int index) 		{ return _layers.get(index); }
	public ArrayList<SingleLayer2D> getLayer() 		{ return _layers; }
	public boolean isDepthValid(int index1, int index2, SingleLayer2D veconLayer) {
		SingleLayer2D veconLayer1 = _layers.get(index1);
		SingleLayer2D veconLayer2 = _layers.get(index2);
		int nxy = veconLayer.getNx()*veconLayer.getNy();
		for(int j=0; j<nxy; j++) {
//			if(veconLayer.getDepth(j)<=veconLayer1.getDepth(j) || veconLayer.getDepth(j)>=veconLayer2.getDepth(j) ) {
//				//System.out.println("dp "+veconLayer._dp[j]+" dp1= "+veconLayer1._dp[j] +" dp2= "+veconLayer2._dp[j]);
//				//System.exit(-1);
//				return false;
//			}
		}
		return true;
	}
	public double minLayerThickness() {
		double [] layerTop = getLayerTop(getX0());
		double [] thickness = new double[layerTop.length-1];
		for(int j=0; j<_layers.size()-1; j++) {
			thickness[j] = layerTop[j+1] - layerTop[j];
		}
		double min = thickness[0]; 
		for(int j=1; j<thickness.length; j++) {
			min = min<thickness[j] ? min:thickness[j];
		}
		
		layerTop = getLayerTop(getX1());
		for(int j=0; j<_layers.size()-1; j++) {
			thickness[j] = layerTop[j+1] - layerTop[j];
		}
		for(int j=1; j<thickness.length; j++) {
			min = min<thickness[j] ? min:thickness[j];
		}
		return min;
	}
	public double [] getLayerTops() { return getLayerTop(_layers.get(0).getCenterX()); }
	public double getLayerTop(int index, double x) {
		return _layers.get(index).getCurve().getPointZ(x);
	}
	public double [] getLayerTop(double x) {
		double layerTop[] = new double[_layers.size()];
		for(int j=0; j<_layers.size(); j++) {
			layerTop[j] = getLayerTop(j, x);
		}
		return layerTop;
	}
	public int getLayerIndex(double x, double z) {
		double layerTop[] = getLayerTop(x);
		int k=0; 
		for(int j=0; j<_layers.size()-1; j++) {
			if(z>=layerTop[j]&&z<=layerTop[j+1]) return j;
		}
		return k;
	}
	public int getLayerTopIndex(double x, double z) {
		double layerTop[] = getLayerTop(x);
		int k=0; 
		for(int j=0; j<_layers.size()-1; j++) {
			if(z>=layerTop[j]&&z<layerTop[j+1]) return j;
		}
		return _layers.size()-1;
	}
	public int onBoundary(Point2D p) {
		for(int j=0; j<_layers.size(); j++) {
			if(getLayer(j).getCurve().on(p)) return j;
		}
		return -1;
	}
	public int getLayerIndex(String name) {
		for(int i=0; i<_layers.size(); i++) {
			if(_layers.get(i).getLayerName().equalsIgnoreCase(name)) { return i; }
		}	
		return 0;
	}
	public double [] getLayerProperty(int id) {
		double [] vel = new double[_layers.size()];
		for(int i=0; i<_layers.size(); i++) { vel[i] = getLayer(i).getLayerProperty(id); }
		return vel;
	}
	public double min(double [] vel) {
		double min = vel[0];
		for(int i=1; i<vel.length; i++) min = min<vel[i] ? min:vel[i];
		return min;
	}
	public double max(double [] vel) {
		double max = vel[0];
		for(int i=1; i<vel.length; i++) max = max>vel[i] ? max:vel[i];
		return max;
	}
	public void setLayerProperty(int id, double [] v) {
		for(int i=0; i<v.length; i++) { getLayer(i).setLayerProperty(id, v[i]); }
	}
	public void clip() {
		int nLayers = _layers.size();
		for(int i=1; i<nLayers-1; i++) {
			SingleLayer2D p1 = _layers.get(i-1);
			SingleLayer2D p2 = _layers.get(i);
			SingleLayer2D p3 = _layers.get(i+1);

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
	
	public ArrayList<SingleLayer2D> readLayer(int iType, String selectedFileName) {
		ArrayList<SingleLayer2D> veconLayer = new ArrayList<SingleLayer2D>();

		try{
			BufferedReader reader = new BufferedReader(new FileReader(selectedFileName));

			int nx = 2;
			int ny = 2;
			double x1 = 0;
			double y1 = 0;
			double dx = 1;
			double dy = 1;
			double z1 = 0;
			double dz = 1;

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
			double epsilon = 0.0;
			double delta = 0.0;
			double gamma = 0.0;
			double qp = 0.0;
			double qs = 0.0;
			double theta = 0.0;
			double phi = 0.0;
			String layerName = " ";

			String line=null;
			StringTokenizer st = null;
			
			if(iType==1) {
				line=reader.readLine(); 						st = new StringTokenizer(line, " ,\t");
				x1 = Double.parseDouble(st.nextToken()); 		dx = Double.parseDouble(st.nextToken())-x1;
				z1 = Double.parseDouble(st.nextToken()); 		dz = Double.parseDouble(st.nextToken())-z1;
				line=reader.readLine(); 						st = new StringTokenizer(line, " ,\t");
				line=reader.readLine(); 						st = new StringTokenizer(line, " ,\t");
				int nLayers = Integer.parseInt(st.nextToken());
				for(int i=0; i<nLayers; i++) {
					line=reader.readLine(); 					st = new StringTokenizer(line, " ,\t");
					layerName 	= st.nextToken().trim(); 
					int sequenceNo 		= (int)Double.parseDouble(st.nextToken().trim()); 	
					int layerType 		= (int)Double.parseDouble(st.nextToken().trim()); 	
					vp 				= Double.parseDouble(st.nextToken().trim()); 			
					vpGradientZ 	= Double.parseDouble(st.nextToken().trim()); 	
					vpToVs 			= Double.parseDouble(st.nextToken().trim()); 		
					int layerBehind 	= (int)Double.parseDouble(st.nextToken().trim());
					
					line=reader.readLine(); 					st = new StringTokenizer(line, " ,\t");
					vpGradientX 	= Double.parseDouble(st.nextToken().trim()); 		
					den 			= Double.parseDouble(st.nextToken().trim()); 
					epsilon 		= Double.parseDouble(st.nextToken().trim()); 
					delta 			= Double.parseDouble(st.nextToken().trim());
					
					line=reader.readLine(); 					st = new StringTokenizer(line, " ,\t");
					int layerVisible 	= 1;
					int layerEditable 	= 0;
					int layerSmooth = (int)Double.parseDouble(st.nextToken().trim()); 
					theta 			= Double.parseDouble(st.nextToken().trim()); 
					phi 			= Double.parseDouble(st.nextToken().trim());
					gamma			= Double.parseDouble(st.nextToken().trim());

					if(st.hasMoreTokens()) {
						layerVisible 	= (int)Double.parseDouble(st.nextToken().trim()); 
					}
					if(st.hasMoreTokens()) {
						layerEditable 	= (int)Double.parseDouble(st.nextToken().trim()); 
						//System.out.println(layerVisible + " e2=" + layerEditable);
					}
					
					vs = 0.0;
					if(vpToVs!=0.0) vs = vp/vpToVs;
					
					line=reader.readLine(); 					st = new StringTokenizer(line, " ,\t");
					int nMaster			= (int)Double.parseDouble(st.nextToken().trim()); 
					ArrayList<Point2D> mp = new ArrayList<Point2D>();
					for(int j=0; j<nMaster; j++) {
						line=reader.readLine(); 	st = new StringTokenizer(line, " ,\t");
						mp.add(new Point2D(Double.parseDouble(st.nextToken().trim()), 
								Double.parseDouble(st.nextToken().trim())));
					}

					line=reader.readLine();
					line=reader.readLine(); 					st = new StringTokenizer(line, " ,\t");
					int N			= (int)Double.parseDouble(st.nextToken().trim());
					SingleCurve curve = new SingleCurve(N);
					for(int j=0; j<N; j++) {
						line=reader.readLine(); 	st = new StringTokenizer(line, " ,\t");
						Point2D p1 = new Point2D(Double.parseDouble(st.nextToken().trim()), 
								Double.parseDouble(st.nextToken().trim()));
						curve.getLines().add(new SingleLine(p1, p1));
					}
					curve.updateP2();
					
					SingleLayer2D sl = new SingleLayer2D(layerName, i, vp, vs, den, 
							vpGradientX, vpGradientY, vpGradientZ, vsGradientX, vsGradientY, vsGradientZ, 
							delta, epsilon, gamma, qp, qs, theta, phi, 
							2, 2, dx, dy, x1, y1, null, mp, curve);
					sl.setCurve();
					//System.out.println(sl.toString(2));
					veconLayer.add(sl);
				}
			} else if(iType==2) {
				double x2=0, y2=0, z2=0, z3=0, z4=0;
				line = reader.readLine(); // 1st line Title
				line = reader.readLine(); // second line Title
				st = new StringTokenizer(line, " ,\t");
				if(st.countTokens()<=1) return null;

				x1 = Double.parseDouble(st.nextToken().trim());
				x2 = Double.parseDouble(st.nextToken().trim());
				y1 = 0.0;
				y2 = 1.0;

				nx = 2;
				ny = 2;
				dx = x2-x1;
				dy = y2-y1;
				
				int k = 0;
				while((line = reader.readLine())!=null) {
					st = new StringTokenizer(line, " ,\t");
					if(st.countTokens()<=1) continue;

					z1 = Double.parseDouble(st.nextToken().trim());
					z2 = Double.parseDouble(st.nextToken().trim());
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

					//System.out.println(z1+" "+z2+" "+vp+" "+vs+" "+den+" "+delta+" "+epsilon+" "+gamma+" ");
					vpToVs = 0.0;
					if(vs!=0.0) vpToVs = vp/vs;

					ArrayList<Point2D> 	mp = new ArrayList<Point2D>(2);
					mp.add(new Point2D(x1, z1));
					mp.add(new Point2D(x2, z2));
					SingleLayer2D sl = new SingleLayer2D("layer_"+k, k++, vp, vs, den, 
							vpGradientX, vpGradientY, vpGradientZ, vsGradientX, vsGradientY, vsGradientZ, 
							delta, epsilon, gamma, qp, qs, theta, phi, 
							2, 2, dx, dy, x1, y1, null, mp, null);
					
					sl.setCurve();
					//System.out.println(sl.toString(3));
					veconLayer.add(sl);
				}
			} else if(iType==4) { // read ZeltModel
				double x2=0, y2=0, z2=0, z3=0, z4=0;
				line = reader.readLine(); // 1st line Title
				y1 = 0.0;
				y2 = 1.0;

				nx = 2;
				ny = 2;
				dy = y2-y1;
				
				int k = 0;
				while((line = reader.readLine())!=null) {
					st = new StringTokenizer(line, " ,\t"); 				st.nextToken().trim(); 
					x1 = Double.parseDouble(st.nextToken().trim());
					z1 = Double.parseDouble(st.nextToken().trim());
					line = reader.readLine(); 
					st = new StringTokenizer(line, " ,\t"); 				st.nextToken().trim();
					x2 = Double.parseDouble(st.nextToken().trim());
					z2 = Double.parseDouble(st.nextToken().trim());
					if(k==0) { dx = x2-x1; }
					vp = Double.parseDouble(st.nextToken().trim()); 		
					if(vp==0) vp = Double.parseDouble(st.nextToken().trim()); 
					else st.nextToken().trim();
					vs = Double.parseDouble(st.nextToken().trim());			
					if(vs==0) vs = Double.parseDouble(st.nextToken().trim()); 
					else st.nextToken().trim();
					den = Double.parseDouble(st.nextToken().trim());		st.nextToken().trim();

					theta = Double.parseDouble(st.nextToken().trim()); 		st.nextToken().trim();
					delta = Double.parseDouble(st.nextToken().trim()); 		st.nextToken().trim();
					epsilon = Double.parseDouble(st.nextToken().trim()); 	st.nextToken().trim();
					gamma = Double.parseDouble(st.nextToken().trim()); 		st.nextToken().trim();

					//System.out.println(z1+" "+z2+" "+vp+" "+vs+" "+den+" "+delta+" "+epsilon+" "+gamma+" ");
					vpToVs = 0.0;
					if(vs!=0.0) vpToVs = vp/vs;

					ArrayList<Point2D> 	mp = new ArrayList<Point2D>(2);
					mp.add(new Point2D(x1, z1));
					mp.add(new Point2D(x2, z2));
					SingleLayer2D sl = new SingleLayer2D("layer_"+k, k++, vp, vs, den, 
							vpGradientX, vpGradientY, vpGradientZ, vsGradientX, vsGradientY, vsGradientZ, 
							delta, epsilon, gamma, qp, qs, theta, phi, 
							2, 2, dx, dy, x1, y1, null, mp, null);
					
					sl.setCurve();
					//System.out.println(sl.toString(3));
					veconLayer.add(sl);
				} 
			} else if(iType==5) { //contains layer name
				double x2=0, y2=0, z2=0, z3=0, z4=0;
				line = reader.readLine(); // 1st line Title
				line = reader.readLine(); // second line Title
				st = new StringTokenizer(line, ",\t");
				if(st.countTokens()<=1) return null;
				st.nextToken();
				x1 = Double.parseDouble(st.nextToken().trim());
				x2 = Double.parseDouble(st.nextToken().trim());
				y1 = 0.0;
				y2 = 1.0;

				nx = 2;
				ny = 2;
				dx = x2-x1;
				dy = y2-y1;
				
				int k = 0;
				while((line = reader.readLine())!=null) {
					st = new StringTokenizer(line, ",\t");
					if(st.countTokens()<=1) continue;
					String name = st.nextToken().trim();
					z1 = Double.parseDouble(st.nextToken().trim());
					z2 = Double.parseDouble(st.nextToken().trim());
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

					//System.out.println(z1+" "+z2+" "+vp+" "+vs+" "+den+" "+delta+" "+epsilon+" "+gamma+" ");
					vpToVs = 0.0;
					if(vs!=0.0) vpToVs = vp/vs;

					ArrayList<Point2D> 	mp = new ArrayList<Point2D>(2);
					mp.add(new Point2D(x1, z1));
					mp.add(new Point2D(x2, z2));
					SingleLayer2D sl = new SingleLayer2D(name, k++, vp, vs, den, 
							vpGradientX, vpGradientY, vpGradientZ, vsGradientX, vsGradientY, vsGradientZ, 
							delta, epsilon, gamma, qp, qs, theta, phi, 
							2, 2, dx, dy, x1, y1, null, mp, null);
					
					sl.setCurve();
					//System.out.println(sl.toString(3));
					veconLayer.add(sl);
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
			bufferedWriter.write(getX0()+" "+getX1()+" "+getZ0()+" "+getZ1()+" \n");
			bufferedWriter.write("1 370.0 0.0 1.0 0 \n");
			bufferedWriter.write(_layers.size()+" 0.0 0 \n");
			for(int i=0; i<_layers.size(); i++) {
				SingleLayer2D sl = _layers.get(i);
				bufferedWriter.write(sl.toString(2)+"\n");
				bufferedWriter.write(sl.getCurve().toString(3));
			}
			bufferedWriter.close();
		} catch (IOException ioexception) {
			JOptionPane.showMessageDialog(null, "\nFile Format is not right!", 
					"Error", JOptionPane.ERROR_MESSAGE);
		}
	}
	public void writeLayer(int id, String selectedFileName) {
		try{
			BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(selectedFileName, false));
			bufferedWriter.write(toString(id));
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
			a = _layers.get(0).toString(1);
			b = new String(b.concat("\n"+a));
			for(int i=0; i<_layers.size(); i++) {
				a = String.format("layer=%d", i);
				b = new String(b.concat("\n"+a));
				a = String.format("%s", _layers.get(i).toString(1));
				b = new String(b.concat("\n"+a));
			} 
		}
		if(id==2) {
			a = _layers.get(0).toString(1);
			b = new String(b.concat("\n"+a));
			for(int i=0; i<_layers.size(); i++) {
				a = String.format("layer=%d", i);
				b = new String(b.concat("\n"+a));
				a = String.format("%s", _layers.get(i).toString(2));
				b = new String(b.concat("\n"+a));
			} 
		}
		if(id==3) {
			b = new String("Z1_NW/X1, Z2_NE/X2, Vp/Dx, Vs/Dz, Density/None, Delta/None, Epsilon/None, Gamma/None, Qp/None, Qs/None");
			//a = getX0()+", "+getX1()+", "+getDx()+", "+getDz()+", 10, 10, 0, 0, 0, 0, 0, 0"; 
			a = getX0()+", "+getX1()+", 10, 10, 0, 0, 0, 0, 0, 0"; 
			b = new String(b.concat("\n"+a));
			for(int i=0; i<_layers.size(); i++) {
				a = getLayer(i).getMasterZ(0)+", "+getLayer(i).getMasterZ(1)+", "+getLayer(i).getVp()+", "+getLayer(i).getVs()+", "+
						getLayer(i).getDen()+", "+getLayer(i).getDelta()+", "+getLayer(i).getEpsilon()+", "+getLayer(i).getGamma()+", "+
						getLayer(i).getQp()+", "+getLayer(i).getQs();
				b = new String(b.concat("\n"+a));
			} 
		}
		if(id==10) { // to comply with seispt specs
			b = new String("Layer# \tDepth \tP_Vel \tS_Vel \tDelta \tEpsilon \tGamma");
			for(int i=0; i<_layers.size(); i++) {
				a = (i+1)+" \t"+getLayer(i).getMasterZ(0)+" \t"+getLayer(i).getVp()+" \t"+getLayer(i).getVs()+" \t"+
						getLayer(i).getDelta()+" \t"+getLayer(i).getEpsilon()+" \t"+getLayer(i).getGamma();
				b = new String(b.concat("\n"+a));
			} 
		}
		return b;
	}
	
	public String printString(int id) {
		String b = String.format("x0=%f x1=%f z0=%f z1=%f",  getX0(), getX1(), getZ0(), getZ1());
		String a = String.format("iUnit=%d grid size=1 v0=0 v1=0 time(1)/depth(0)=0", _iUnit); 
		b = new String(b.concat("\n"+a));

		a = String.format("Nlayers=%d", _layers.size());
		b = new String(b.concat("\n"+a));

		if(id==1) {
			for(int i=0; i<_layers.size(); i++) {
				b = new String(b.concat("\n"+_layers.get(i).toString(2)));
			}
		}
		
		
		return b;
	}

	

	
}
