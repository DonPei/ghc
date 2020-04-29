package com.geohammer.core;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.StringTokenizer;

import javax.swing.JOptionPane;

import org.ucdm.core.acquisition.VCPair;
import org.ucdm.core.planarmodel.DipLayer1D;
import org.ucdm.core.planarmodel.FlatLayer1D;
import org.ucdm.core.planarmodel.Layer2D;
import org.ucdm.core.planarmodel.Point2D;
import org.ucdm.core.planarmodel.SingleLayer2D;

//The location of the velocity model and of the receivers is defined in a cartesian
//coordinate system (x,y,z), The positive X points to the geographic East. 
//The positive Y points to the geographic true North (not magnatic north). 
//The positive Z points down.
//The reference point of (x,y) is at the first receiver of 
//the first well. The reference point of Z is at the KB of the fracturing well
//centered at the epicenter. x is North, y is East, and z is positive downward.

public class SeisPTVel {
	//This class reads in the velocity file output from SeisPT and output
	// the layer information and P and S wave velocities for Finite difference
	// calculation using MIT code.

	int _iUnit 			= 2; //=1 m; =2 ft
	int _iProjectType 	= 2; //=2 for 2D, =3 for 3D or Deviated setup
	int _iMediaFlag 	= 1; //=0 for isotropic, =1 for anisotropic
	int _iCalcTypeFlag 	= 0; //=0 for direct, =1 for first arrival

	double 	_x0 = 0.0;  //only for 3D reference location
	double 	_y0 = 0.0; 
	double 	_z0 = 0.0; 
	double 	_x1 = 0.0;  //only for 3D reference location
	double 	_y1 = 0.0; 
	double 	_z1 = 0.0;
	int 	_nx	= 1;
	int 	_ny	= 1;
	int 	_nz	= 1;

	double _gridSize 	= 1.0; 
	double _dip 		= 0.0; //in degrees (dip angle of layers)
	double _dipAzimuth 	= 0.0; //in degrees (strike angle of normal direction of dipping layers)

	int 		_nBoundaries= 3;
	double [] 	_top 		= null;
	double [] 	_vp 		= null;
	double [] 	_vs 		= null;
	double [] 	_den 		= null; // km/m^3 for SI or lb/ft^3 for English
	double [] 	_delta 		= null;
	double [] 	_epsilon 	= null;
	double [] 	_gamma 		= null;
	int [] 		_iso 		= null; //= 1 for iso layer, = 0 for aniso layer (default = 1)
	int [] 		_variable 	= null; //= 1 for inverting this layer, = 0 for not inverting this layer (default = 1)

	double [] 	_qp 		= null;
	double [] 	_qs 		= null;

	int 		_nRcv		= 12;
	double [] 	_rE 		= null;
	double [] 	_rN 		= null;
	double [] 	_rD 		= null;
	double [] 	_rAngle 	= null;

	public SeisPTVel(String selectedFileName, int iUnit) {
		_iUnit = iUnit;
		read(selectedFileName, _iUnit);
	}

	public FlatLayer1D toFlatLayer1D() {
		FlatLayer1D layer1D = new FlatLayer1D(_top, _vp, _vs, _den, _delta, _epsilon, _gamma, null, null, null, null);
		for(int i=0; i<layer1D.getNumOfBoundaries(); i++) {
			layer1D.getLayer(i).setLayerName("layer_"+i);
		}
		return layer1D;
	}
	public DipLayer1D toDipLayer1D(VCPair vcPW) {
		double dip = Math.abs(_dip); //_dip negtative up 
		double strike = 0.0;

		if(getProjectType()==2) {
			if(vcPW==null) _dipAzimuth = 0.0;
			else _dipAzimuth = vcPW.getAzimuth(-1);
		}

		if(_dip==0) strike = 0.0;
		else if(_dip>0) strike = _dipAzimuth+270;
		else strike = _dipAzimuth+90;

		DipLayer1D dlayer1D = new DipLayer1D(_top, _vp, _vs, _den, _delta, _epsilon, _gamma, null, null, null, null, strike, dip);

		if(getProjectType()==2) {
//			double x0 = vcPW.getMinE();
//			double y0 = vcPW.getMinN();
//			
//			_x0 = 0.0<x0 ?0.0:x0; 
//			_y0 = 0.0<y0 ?0.0:y0; 
//			
//			_x0 -= 1.0;
//			_y0 -= 1.0;
			
			_x0 = 0.0;
			_y0 = 0.0;
			
			_y1 = _y0+_x1;
			_x1 = _x0+_x1;
			
			_z0 = _top[0];
			_z1 = _top[_top.length-1];
		} 
		int nN = (int)((_y1-_y0)/_gridSize)+1; //Northing
		int nE = (int)((_x1-_x0)/_gridSize)+1;
		int nD = (int)((_z1-_z0)/_gridSize)+1;

		for(int i=0; i<dlayer1D.getNumOfBoundaries(); i++) {
			dlayer1D.getLayer(i).setSize(nN, nE, nD, _gridSize, _gridSize, _gridSize, _y0, _x0, _z0);
			dlayer1D.getLayer(i).setLayerName("layer_"+i);
		}

		return dlayer1D;
	}

//	public Layer2D toLayer2D() {
//		double vertexX = 0.0;
//		double vertexZ = 0.0;
//
//		double gridSize = _gridSize;
//		double refDepth = _top[0];
//		double areaHeight = _top[_top.length-1]-_top[0];
//		double areaWidth = _x1;
//		double dipAngle = _dip;
//		String dipFromObs = "t";
//
//		int nx = (int)(areaWidth/gridSize)+1;
//		int nz = (int)(areaHeight/gridSize)+1;
//
//		int ix = Integer.parseInt(_mdl2DLayers.get(0).getVertex(0).getValue("X"));
//		double x0 = ix*gridSize;
//
//		double dip = Math.abs(dipAngle);
//		double strike = 0.0;
//		double dipAzimuth = 0.0;
//		if(dipAngle==0) strike = 0.0;
//		else if(dipAngle>0) strike = dipAzimuth+270;
//		else strike = dipAzimuth+90;		
//
//		int nBoundaries = _top.length;
//		Layer2D layer2D = new Layer2D(nBoundaries);
//		layer2D.setStrike(strike);
//		layer2D.setDip(dip);
//
//		double [] pN = new double[nBoundaries];
//		double [] pE = new double[nBoundaries];
//		double [] pD = new double[nBoundaries];
//		for(int i=0; i<nBoundaries; i++) {
//			pN[i] = 0.0;
//			pE[i] = 0.0;
//			int iz = Integer.parseInt(_mdl2DLayers.get(i).getVertex(0).getValue("Z"));
//			pD[i] = refDepth+iz*gridSize;
//
//			double vp = Double.parseDouble(_mdl2DLayers.get(i).getValue(0));
//			double vs = Double.parseDouble(_mdl2DLayers.get(i).getValue(1));
//			double den = 2.0;
//			String name = _mdl2DLayers.get(i).getValue(2);
//			if(name==null||name.isEmpty()) name = "Layer_"+i;
//
//			double epsilon = Double.parseDouble(_mdl2DLayers.get(i).getValue(3));
//			double delta = Double.parseDouble(_mdl2DLayers.get(i).getValue(4));
//			double gamma = Double.parseDouble(_mdl2DLayers.get(i).getValue(5));
//
//			double qp = 1.0e10;
//			double qs = qp;
//
//			ArrayList<Point2D> 	mp = new ArrayList<Point2D>();
//			for(int j=0; j<4; j=j+2) {
//				vertexX = gridSize*Double.parseDouble(_mdl2DLayers.get(i).getVertex(j).getValue("X"));
//				vertexZ = gridSize*Double.parseDouble(_mdl2DLayers.get(i).getVertex(j).getValue("Z"));
//				mp.add(new Point2D(vertexX, vertexZ));
//			}
//
//			SingleLayer2D singleLayer = new SingleLayer2D(name, i, vp, vs, den,
//					0.0, 0.0, 0.0, 0.0, 					
//					delta, epsilon, gamma, qp, qs, 0, 0, mp, null);
//
//			singleLayer.setSize(nx, nx, nz, 
//					gridSize, gridSize, gridSize, 
//					x0, x0, pD[0]);	
//			singleLayer.setCurve();
//
//			layer2D.getLayer().add(singleLayer);
//		}
//
//		return layer2D;
//	}
	public void read(String selectedFileName, int iUnit) {
		try{
			BufferedReader reader = new BufferedReader(new FileReader(selectedFileName));
			String line = reader.readLine(); 
			StringTokenizer st = new StringTokenizer(line, " ,\t");
			try{
				_iProjectType 	= Integer.parseInt(st.nextToken().trim());
			} catch(java.lang.NumberFormatException e) {
				JOptionPane.showMessageDialog(null, "\nFile Format is not right 1! " + selectedFileName, 
						"Error", JOptionPane.ERROR_MESSAGE);
				return;
			}

			if(_iProjectType<2) {
				_iMediaFlag 	= _iProjectType;
				_iProjectType 	= 2;
			} else {
				line = reader.readLine(); 			st = new StringTokenizer(line, " ,\t");
				_iMediaFlag 	= Integer.parseInt(st.nextToken().trim());
			}
			line = reader.readLine(); 			st = new StringTokenizer(line, " ,\t");
			_iCalcTypeFlag 	= Integer.parseInt(st.nextToken().trim());

			if(_iProjectType==3) { 
				line = reader.readLine(); 			st = new StringTokenizer(line, " ,\t");
				_x0 = Double.parseDouble(st.nextToken().trim());
				_y0 = Double.parseDouble(st.nextToken().trim());
				_z0 = Double.parseDouble(st.nextToken().trim());
			}

			line = reader.readLine(); 

			line = reader.readLine(); 			st = new StringTokenizer(line, " ,\t"); 
			if(_iProjectType==3) { //need to convert the (x2,y2,z2) to model size
				_gridSize = Double.parseDouble(st.nextToken().trim());
				_x1 = Double.parseDouble(st.nextToken().trim());
				_y1 = Double.parseDouble(st.nextToken().trim());
				_z1 = Double.parseDouble(st.nextToken().trim());
				_nBoundaries = Integer.parseInt(st.nextToken().trim());
				_dip = Double.parseDouble(st.nextToken().trim());
				if(st.hasMoreTokens())
					_dipAzimuth = Double.parseDouble(st.nextToken().trim());
			} else if(_iProjectType==2) { 
				_gridSize = Double.parseDouble(st.nextToken().trim());
				_x1 = Double.parseDouble(st.nextToken().trim());  //model length in X direction (_x0 is always 0.0) 
				//_y1 = Double.parseDouble(st.nextToken().trim()); //model length in Y direction (_y0 is always 0.0)
				_nx = Integer.parseInt(st.nextToken().trim());
				_nz = Integer.parseInt(st.nextToken().trim());
				_nBoundaries = Integer.parseInt(st.nextToken().trim());
				_dip = Double.parseDouble(st.nextToken().trim());
				if(st.hasMoreTokens())
					_dipAzimuth = Double.parseDouble(st.nextToken().trim());
			} else { }
			//System.out.println("nLayers="+_nBoundaries);

			//model properties
			_top = new double[_nBoundaries];
			_vp = new double[_nBoundaries];
			_vs = new double[_nBoundaries];
			_den = new double[_nBoundaries];
			_delta = new double[_nBoundaries];
			_epsilon = new double[_nBoundaries];
			_gamma = new double[_nBoundaries];

			_iso = new int[_nBoundaries];
			_variable = new int[_nBoundaries];
			for(int i=0; i<_nBoundaries; i++) {
				_den[i] = 2.0;
				_iso[i] = 1;
				_variable[i] = 1;
			}
			//			if(_iUnit == 2) { //ft
			//				for(int i=0; i<_nBoundaries; i++) {
			//					_den[i] = 159.0;
			//				}
			//			}
			line = reader.readLine(); 	//column name
			for(int i=0; i<_nBoundaries; i++) {
				line = reader.readLine(); st = new StringTokenizer(line, " ,\t");
				st.nextToken();
				_top[i] 	= Double.parseDouble(st.nextToken().trim());
				_vp[i] 		= Double.parseDouble(st.nextToken().trim());
				_vs[i] 		= Double.parseDouble(st.nextToken().trim());
				_delta[i] 	= Double.parseDouble(st.nextToken().trim());
				_epsilon[i] = Double.parseDouble(st.nextToken().trim());
				_gamma[i] 	= Double.parseDouble(st.nextToken().trim());
				if(st.hasMoreTokens()) {
					_den[i] = Double.parseDouble(st.nextToken().trim());
				} else {
					_den[i] = 2300.0; //kg/m^3
					//					if strcmp(UnitSys, 'SI') 
					//					_den[i] = 2550.0;  // as default if no density data is avaiable in SeisPT vel file
					//					if strcmp(UnitSys, 'English') 
					//					_den[i] = 159.0;  
				}
				if(st.hasMoreTokens())
					_iso[i] = Integer.parseInt(st.nextToken().trim());
				if(st.hasMoreTokens())
					_variable[i] = Integer.parseInt(st.nextToken().trim());
			}
			_z0 = _top[0];
			_z1 = _top[_nBoundaries-1];

			//sensor information
			line = reader.readLine();
			line = reader.readLine();
			line = reader.readLine(); st = new StringTokenizer(line, " ,\t");
			_nRcv 	= Integer.parseInt(st.nextToken().trim());
			_rE 	= new double[_nRcv];
			_rN 	= new double[_nRcv];
			_rD 	= new double[_nRcv];
			_rAngle = new double[_nRcv];

			line = reader.readLine(); 	//column name
			for(int i=0; i<_nRcv; i++) {
				line = reader.readLine(); st = new StringTokenizer(line, " ,\t");
				if(_iProjectType==3) {
					_rE[i] 		= Double.parseDouble(st.nextToken().trim());
					_rN[i] 		= Double.parseDouble(st.nextToken().trim());
					_rD[i] 		= Double.parseDouble(st.nextToken().trim());
					_rAngle[i] 	= Double.parseDouble(st.nextToken().trim());
				} else if(_iProjectType==2) {
					_rD[i] 		= Double.parseDouble(st.nextToken().trim());
					_rE[i] 		= Double.parseDouble(st.nextToken().trim());
					_rAngle[i] 	= Double.parseDouble(st.nextToken().trim());
				} else { }
			}

			reader.close();
		} catch (IOException ioexception) {
			JOptionPane.showMessageDialog(null, "\nFile Format is not right 2! " + selectedFileName, 
					"Error", JOptionPane.ERROR_MESSAGE);
		}	
	}
	public void write(String selectedFileName) {
		try{
			BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(selectedFileName, false));
			bufferedWriter.write(toString());
			bufferedWriter.close();
		} catch (IOException ioexception) {
			JOptionPane.showMessageDialog(null, "\nFile Format is not right!", 
					"Error", JOptionPane.ERROR_MESSAGE);
		}
	}
	public String toString() {
		String b = _iProjectType + "    "+ "//=2 for 2D, =3 for 3D or Deviated setup";
		String a = _iMediaFlag + "    "+ "//=0 for isotropic, =1 for anisotropic";
		b = b.concat("\n"+a+"\n");
		a = _iCalcTypeFlag + "    "+ "//=0 for direct, =1 for first arrival";
		b = b.concat(a+"\n");

		if(_iProjectType==3) { 
			a = _x0 + "    "+_y0 + "    "+_z0 + "    "+ "//(x0, y0, z0)";
			b = b.concat(a+"\n");
		}

		if(_iProjectType==3) { //need to convert the (x2,y2,z2) to model size
			a = "GirdSize	x1	y1	z1	#Layers		DipAngle	DipAzimuth";
			b = b.concat(a+"\n");
			a = _gridSize + "    "+_x1 + "    "+_y1 + "    "+_z1 + "    "+_nBoundaries+ "    "+_dip+ "    "+_dipAzimuth;
			b = b.concat(a+"\n");
		} else if(_iProjectType==2) { 
			a = "GirdSize	Model_Size	nx	nz	#Layers DipAngle";
			b = b.concat(a+"\n");
			a = _gridSize + "    "+(_x1-_x0) +  "    "+_nx + "    "+_nz +"    "+_nBoundaries +"    "+_dip;
			b = b.concat(a+"\n");
		} else { }


		a = "Layer#	Depth	P Vel	S Vel	Delta	Epsilon	Gamma Density Iso Variable";
		b = b.concat(a+"\n");
		for(int i=0; i<_nBoundaries; i++) {
			a = (i+1) + "   "+_top[i] + "   "+_vp[i] + "   "+_vs[i] + "   "+_delta[i]+ "   "+_epsilon[i]+ "   "+
					_gamma[i] +"   "+_den[i] + "   "+_iso[i] + "   "+_variable[i];
			b = b.concat(a+"\n");	
		}

		a = "\nTool Locations\n" + _nRcv;
		b = b.concat(a+"\n");
		if(_iProjectType==3) {
			a = "East North Depth Tool_Angle";
			b = b.concat(a+"\n");
		} else if(_iProjectType==2) {
			a = "Depth Distance Tool_Angle";
			b = b.concat(a+"\n");
		} else { }

		for(int i=0; i<_nRcv; i++) {
			if(_iProjectType==3) {
				a = _rE[i] + " "+_rN[i] + " "+_rD[i] + " "+_rAngle[i];
				b = b.concat(a+"\n");
			} else if(_iProjectType==2) {
				a = _rD[i] + " "+_rE[i] + " "+_rAngle[i];
				b = b.concat(a+"\n");
			} else { }
		}

		return b;
	}

	public int getProjectType() 				{ return _iProjectType;	}
	public int getNumOfBoundaries() 			{ return _nBoundaries;	}
	public double [] getTop() 					{ return _top; }
	public double [] getVp() 					{ return _vp; }
	public double [] getVs() 					{ return _vs; }
	public double [] getDen() 					{ return _den; }

	public double [] getEpsilon() 				{ return _epsilon; }
	public double [] getDelta() 				{ return _delta; }
	public double [] getGamma() 				{ return _gamma; }
	public double [] getQp() 					{ return _qp; }
	public double [] getQs() 					{ return _qs; }

	public double getTop(int iB) 				{ return _top[iB]; }
	public double getVp(int iB) 				{ return _vp[iB]; }
	public double getVs(int iB) 				{ return _vs[iB]; }
	public double getVpToVs(int iB) 			{ if(_vs[iB]!=0.0) return _vp[iB]/_vs[iB]; else return _vs[iB]; }
	public double getDen(int iB) 				{ return _den[iB]; }

	public double getEpsilon(int iB) 			{ return _epsilon[iB]; }
	public double getDelta(int iB) 				{ return _delta[iB]; }
	public double getGamma(int iB) 				{ return _gamma[iB]; }
	public double getQp(int iB) 				{ return _qp[iB]; }
	public double getQs(int iB) 				{ return _qs[iB]; }	

	public double getDipAngle() 				{ return _dip; }
	public double getModelWidth() 				{ return _x1; }

	public void setTop(int iB, double top) 				{ _top[iB] = top; }
	public void setVp(int iB, double vp) 				{ _vp[iB] = vp; }
	public void setVs(int iB, double vs) 				{ _vs[iB] = vs; }
	public void setDen(int iB, double den) 				{ _den[iB] = den; }

	public void setEpsilon(int iB, double epsilon) 		{ _epsilon[iB] = epsilon; }
	public void setDelta(int iB, double delta) 			{ _delta[iB] = delta; }
	public void setGamma(int iB, double gamma) 			{ _gamma[iB] = gamma; }
	public void setQp(int iB, double qp) 				{ _qp[iB] = qp; }
	public void setQs(int iB, double qs) 				{ _qs[iB] = qs; }

	public void toThickness() {
		double [] th = getLayerThickness();
		_z0 = _top[0];
		int i = 0;
		for(i=0; i<getNumOfBoundaries()-1; i++) {
			_top[i] = th[i];
		}
		_top[i] = 0.0;
	}
	public void toTopDepth() {
		toTopDepth(_z0);
	}
	public void toTopDepth(double z0) {
		double sum = z0;
		for(int i=1; i<getNumOfBoundaries(); i++) {
			sum += _top[i-1];
			_top[i] = sum;
		}
		_top[0] = z0;
	}
	public double [] getLayerThickness() {
		double [] th = new double[getNumOfBoundaries()];
		for(int i=0; i<getNumOfBoundaries()-1; i++) {
			th[i] 		= _top[i+1] - _top[i];
		}
		th[getNumOfBoundaries()-1] 		= 0.0;
		return th;		
	}
}
