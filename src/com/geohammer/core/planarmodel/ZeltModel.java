
package com.geohammer.core.planarmodel;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

import javax.swing.JOptionPane;

public class ZeltModel {
	public int _nBoundaries;
	public int _nNodes;
	public int _iVp;
	public int _iUnit;		//1 m; 2 ft; 3 km

	public int _i1[];
	public int _i2[];
	public int _bndryNo[];

	public double _x[];
	public double _z[];
	public double _vp1[];
	public double _vp2[];
	public double _vs1[];
	public double _vs2[];
	public double _den1[];
	public double _den2[];
	public double _theta1[]; 	// TTI symmetry axis angle.
	public double _theta2[]; 	// optional.

	// required for anisotropic study
	public double _gamma1[]; 	//
	public double _gamma2[]; 	//
	public double _delta1[]; 	//
	public double _delta2[]; 	//
	public double _epsilon1[]; 	// 
	public double _epsilon2[]; 	//

	public double _x0;
	public double _z0;
	public double _x1;
	public double _z1;

	public ZeltModel( )	{	}

	public ZeltModel(String selectedFileName)	{
		this(selectedFileName, 1);
	}

	public ZeltModel(String selectedFileName, int unit)	{
		read(selectedFileName, unit);
	}
	public ZeltModel(int nBndry, int nNode)	{
		this();
		_nBoundaries 	= nBndry;
		_nNodes 		= nNode;
		allocate(nBndry, nNode);
	}
	public ZeltModel(Vector<ZeltNode> nvector)	{
		fromNode(nvector);
	}
	
	public void setRangle(double x0, double x1, double z0, double z1) {
		_x0 	= x0;
		_x1 	= x1;
		_z0 	= z0;
		_z1 	= z1;
	}
	public void fromNode(Vector<ZeltNode> nvector)	{
		_nNodes = nvector.size()-1;
		ZeltNode p = nvector.elementAt(0);	
		_nBoundaries = p.getBndry(); 	

		_x0 	= p.getX();  				
		_z0 	= p.getZ(); 				
		_x1 	= p.getvp1(); 				
		_z1 	= p.getvp2();	

		_iUnit= (int)p.getden1();

		allocate(_nBoundaries, _nNodes);

		for (int i = 1; i <nvector.size(); i++) { 
			int j = i-1;
			p = nvector.elementAt(i);
			_bndryNo[j] = p.getBndry();
			_x[j] 		= p.getX();
			_z[j] 		= p.getZ();
			_vp1[j] 	= p.getvp1();
			_vp2[j] 	= p.getvp2();
			_vs1[j] 	= p.getvs1();
			_vs2[j] 	= p.getvs2();
			_den1[j] 	= p.getden1();
			_den2[j] 	= p.getden2();

			_gamma1[j] 	= p.getgamma1();
			_gamma2[j] 	= p.getgamma2();
			_delta1[j] 	= p.getdelta1();
			_delta2[j] 	= p.getdelta2();
			_epsilon1[j]= p.getepsilon1();
			_epsilon2[j]= p.getepsilon2();
			
			_theta1[j] 	= p.gettheta1();
			_theta2[j] 	= p.gettheta2();
		}
		calI1I2();
		calModelSize();
	} 
	public Vector<ZeltNode> toNode()	{
		Vector<ZeltNode> nvector = new Vector<ZeltNode>();
		ZeltNode p1 = new ZeltNode();	
		p1.setBndry(_nBoundaries); 	
		p1.setX(_x0);  				
		p1.setZ(_z0); 				
		p1.setvp1(_x1); 				
		p1.setvp2(_z1);	
		p1.setden1(_iUnit);
		nvector.add(p1);
		
		for (int i = 1; i <= _nNodes; i++) { 
			ZeltNode p = new ZeltNode();
			int j = i-1;
			
			p = nvector.elementAt(i);
			p.setBndry(_bndryNo[j]);
			p.setX(_x[j]);
			p.setZ(_z[j]);
			p.setvp1(_vp1[j]);
			p.setvp2(_vp2[j]);
			p.setvs1(_vs1[j]);
			p.setvs2(_vs2[j]);
			p.setden1(_den1[j]);
			p.setden2(_den2[j]);

			p.settheta1(_theta1[j]);
			p.settheta2(_theta2[j]);
			p.setgamma1(_gamma1[j]);
			p.setgamma2(_gamma2[j]);
			p.setdelta1(_delta1[j]);
			p.setdelta2(_delta2[j]);
			p.setepsilon1(_epsilon1[j]);
			p.setepsilon2(_epsilon2[j]);
			nvector.add(p);
		}
		return nvector;
	}
	
	public void calI1I2() {
		int s, ni;
		//System.out.println("nNodes=" + _nNodes+ " len="+_i1.length);
		//System.out.println(Arrays.toString(_bndryNo));
		s 		= 0;
		ni 		= 1;
		_i1[0]	= 0;
		for(int i=1; i<_nNodes; i++)	{
			if( _bndryNo[i]!=ni){
				ni		= _bndryNo[i];
				_i2[s] 	= i-1;
				s++;
				_i1[s] 	= i;
			}
		}
		_i2[s] = _nNodes-1;
	}
	public int calNumOfBoundaries() {
		int s = _bndryNo[0];
		int k = 1;
		for(int i=1; i<_nNodes; i++)	{
			if( _bndryNo[i]!=s){
				k++;
				s = _bndryNo[i];
			}
		}
		return k;
	}
	public double[] getModelSize() {
		return new double[]{_x0, _x1, _z0, _z1};
	}
	public void calModelSize() {
		_x0 = getArrayExtreme(1, _x);
		_x1 = getArrayExtreme(2, _x);
		_z0 = getArrayExtreme(1, _z);
		_z1 = getArrayExtreme(2, _z);
	}
	//id==1 for min value;
	public double getArrayExtreme(int id, double [] v) {
		double m = 1.0e10;
		if(id==2) m = -m;
		for(int i=0; i<v.length; i++) {
			if(id==1) {
				if(v[i]<m)  m = v[i];
			} else {
				if(v[i]>m)  m = v[i];
			}
		}
		return m;
	}
	
	private void allocate(int nBoundaries, int nNodes)	{	
		allocate1(nBoundaries);
		allocate2(nNodes);
	}
	private void allocate1(int nBoundaries)	{	
		_nBoundaries = nBoundaries;
		_i1 		= new int[nBoundaries];
		_i2 		= new int[nBoundaries];
	}
	private void allocate2(int nNodes)	{	
		_nNodes 	= nNodes;
		_bndryNo 	= new int[nNodes];	
		_x 			= new double[nNodes];
		_z 			= new double[nNodes];
		_vp1 		= new double[nNodes];
		_vp2 		= new double[nNodes];
		_vs1 		= new double[nNodes];
		_vs2 		= new double[nNodes];
		_den1 		= new double[nNodes];
		_den2 		= new double[nNodes];
		
		_theta1 	= new double[nNodes];
		_theta2 	= new double[nNodes];
		_gamma1 	= new double[nNodes];
		_gamma2 	= new double[nNodes];
		_delta1 	= new double[nNodes];
		_delta2 	= new double[nNodes];
		_epsilon1 	= new double[nNodes];
		_epsilon2 	= new double[nNodes];
	}
	private void init(int k, double v)	{	
		for(int i=0; i<_nBoundaries; i++) {
			_i1[i] 		= k;
			_i2[i] 		= k;
		}
		for(int i=0; i<_nNodes; i++) {
			_bndryNo[i] 		= k;	
			_x[i] 			= v;
			_z[i] 			= v;
			_vp1[i] 		= v;
			_vp2[i] 		= v;
			_vs1[i] 		= v;
			_vs2[i] 		= v;
			_den1[i] 		= v;
			_den2[i] 		= v;

			_theta1[i] 		= v;
			_theta2[i] 		= v;
			_gamma1[i] 		= v;
			_gamma2[i] 		= v;
			_delta1[i] 		= v;
			_delta2[i] 		= v;
			_epsilon1[i] 	= v;
			_epsilon2[i] 	= v;
		}
	}

	public int interpolateNodes( int id, int index, int j, int k, double[] V, double dx) {
		double x1, x2, v1=1, v2=1;
		double slope, dx1;
		int length;

		x1 = _x[j];
		x2 = _x[k];
		if( index==0) 			{ v1 = _z[j]; 	v2 = _z[k]; }
		else {
			if( id==1 ) {
				if( index==1) 	{ v1 = _vp1[j]; v2 = _vp1[k]; }
				else 			{ v1 = _vp2[j]; v2 = _vp2[k]; }
			} else if( id==2 ) {
				if( index==1) 	{ v1 = _vs1[j]; v2 = _vs1[k]; }
				else 			{ v1 = _vs2[j]; v2 = _vs2[k]; }
			} else if( id==4 ) {
				if( index==1) 	{ v1 = _delta1[j]; v2 = _delta1[k]; }
				else 			{ v1 = _delta2[j]; v2 = _delta2[k]; }
			} else if( id==5 ) {
				if( index==1) 	{ v1 = _epsilon1[j]; v2 = _epsilon1[k]; }
				else 			{ v1 = _epsilon2[j]; v2 = _epsilon2[k]; }
			} else if( id==6 ) {
				if( index==1) 	{ v1 = _gamma1[j]; v2 = _gamma1[k]; }
				else 			{ v1 = _gamma2[j]; v2 = _gamma2[k]; }
			} else {}
		}

		length = (int)((x2-x1)/dx +1.1);	
		slope = (v2-v1) / (x2-x1);

		for(int i=0; i<length; i++)	{
			dx1 = dx*i;
			V[i] = v1 + dx1*slope;
		}
		return length;
	}	

	public void getTopV( int id, double[] tmpV, double[] v2, double dx) {
		int i, s, length;
		int nx = (int)((_x1-_x0)/dx+1.1);


		i = 0;
		//interpolate V
		s = 0;
		for(int j=_i1[i]+1; j<=_i2[i]; j++) {
			int jj = j-1;
			if( _x[jj]!=_x[j]) {
				length = interpolateNodes( id, 2, jj, j, tmpV, dx);
				for(int k=0; k<length; k++) v2[s+k] = tmpV[k];
				s += (length-1);
			}
		}
		if( s<nx){for(int j=s+1; j<nx; j++)	{v2[j] = v2[s];} }
	}	

	public float [] create2DGrid(int id, double dx, double dz) {
		int length, s, kz1=0, kz2=0;
		int iz1[], iz2[];
		double tmp, kv1=0, kv2=0, slope=0, dz1=0.0;
		double v1[], v2[], v3[], tmpV[];
		float [] vel;

		int nx = (int)((_x1-_x0)/dx+1.1);
		int nz = (int)((_z1-_z0)/dz+1.1);
		vel = new float[nx*nz];
		tmpV= new double[nx];
		v1 	= new double[nx];
		v2 	= new double[nx];
		v3 	= new double[nx];
		iz1 = new int[nx];
		iz2 = new int[nx];

		for(int i=1; i<_nBoundaries; i++) {
			//interpolate Z
			s = 0;
			for(int j=_i1[i]+1; j<=_i2[i]; j++) {
				int jj = j-1;
				if( _x[jj]!=_x[j]) {
					length = interpolateNodes( id, 0, jj, j, tmpV, dx);
					for(int k=0; k<length; k++) iz2[s+k] = (int)((tmpV[k] - _z0)/dz);
					s += (length-1);
				}					
			}
			if( s<nx){ for(int j=s+1; j<nx; j++) {	iz2[j] = iz2[s]; } }

			//interpolate V
			s = 0;
			for(int j=_i1[i]+1; j<=_i2[i]; j++) {
				int jj = j-1;
				if( _x[jj]!=_x[j]) {
					length = interpolateNodes( id, 1, jj, j, tmpV, dx);
					for(int k=0; k<length; k++) v1[s+k] = tmpV[k];
					length = interpolateNodes( id, 2, jj, j, tmpV, dx);
					for(int k=0; k<length; k++) v2[s+k] = tmpV[k];
					s += (length-1);
				}
			}
			if( s<nx){for(int j=s+1; j<nx; j++)	{v1[j] = v1[s];	v2[j] = v2[s];} }

			//linearize in Z direction
			if(i==1) { 
				for(int j=0; j<nx; j++) {	iz1[j] = 0; }
				getTopV( id, tmpV, v3, dx); 
			} 
			for(int j=0; j<nx; j++)	{
				kz1 = iz1[j]; kz2 = iz2[j]; 	kv1 = v3[j]; kv2 = v1[j];

				if( kz2==kz1) 	{ slope=0.0; 					tmp=kv2; }
				else 			{ slope=(kv2-kv1)/(kz2-kz1); 	tmp=kv1; }

				dz1 = 0.0;
				for(int k=kz1; k<=kz2; k++) 	{
					vel[j+k*nx] = (float)(tmp + dz1*slope);
					dz++;
				}	
			}

			for(int k=0; k<nx; k++) iz1[k] = iz2[k];
			for(int k=0; k<nx; k++) v3[k] = v2[k];
		}

		return vel;	
	}

	public float [][] to2DGrid(int iColumn, double dx, double dz) {
		int nx = (int)((_x1-_x0)/dx+1.1);
		int nz = (int)((_z1-_z0)/dz+1.1);
		float [][] vel = new float[nz][nx];
		
		float [] ax = createGridX(dx);
		int [][] z = new int[_nBoundaries][ax.length];
		for(int ix=0; ix<nx; ix++) {
			double [] az = getIntersectionDepth((double)ax[ix] );
			for(int i=0; i<_nBoundaries; i++)	{
				z[i][ix] = (int)((az[i]-_z0)/dz);
			}
		}
		for(int i=0; i<_nBoundaries; i++)	{
			float v = (float)getLayerProperties(iColumn, i);
			for(int ix=0; ix<nx; ix++) {
				for(int iz=z[i][ix]; iz<nz; iz++) {
					vel[iz][ix] = v;
				}
			}
		}
		return vel;
	}
	
	public float [] createGridX(int iBoundary, double dx) {
		int k = _i2[iBoundary]-_i1[iBoundary]+1;
		if(k==2) return new float[]{(float)_x0, (float)_x1};
		else return createGridX(dx);
	}
	
	public float [] createGridX(double dx) {
		int nx = (int)((_x1-_x0)/dx) + 1;
		float [] gridX = new float[nx];
		for(int i=0; i<nx; i++) {
			gridX[i] = (float)(_x0 + i*dx);
		}
		gridX[nx-1] = (float)(_x1);
		return gridX;
	}
	public double getMasterPointX(int index) {
		return _x[index];
	}
	public double getMasterPointZ(int index) {
		return _z[index];
	}
	public float [] getMasterPoint(int id, int iBoundary) {
		int k = _i2[iBoundary]-_i1[iBoundary]+1;
		
		float [] gridY = new float[k];
		for(int i=_i1[iBoundary], j=0; i<=_i2[iBoundary]; i++, j++) {
			if(id==1) gridY[j] = (float)_x[i];
			else gridY[j] = (float)_z[i];
		}
		return gridY;
	}
	public int [] create2DImage(int id, double dx, double dz, float min, float max) {
		int nx = (int)((_x1-_x0)/dx+1.1);
		int nz = (int)((_z1-_z0)/dz+1.1);
		min = 0.9f*min;
		int[] ai = new int[nx*nz];
		float [] v = create2DGrid(id, dx, dz);

		for(int i=0; i<nz; i++) {
			for(int j=0; j<nx; j++)
				ai[i*nx+j] = (int)( 255.0f*(v[i*nx+j]-min)/(max-min) ); 
		}

		return ai;		
	}

	public void saveAsVeconFormatGrid( int id, double dx, double dz, int unit, String userSelectedFilePath ) { 
		BufferedWriter bufferedWriter = null;

		int nx = (int)((_x1-_x0)/dx+1.1);
		int nz = (int)((_z1-_z0)/dz+1.1);

		float [] vel = create2DGrid(id, dx, dz);
		int k = 0;
		int s = (nx/6);
		int t = nx - s*6;

		try {
			bufferedWriter = new BufferedWriter(new FileWriter(userSelectedFilePath));

			// Start writing to the output stream
			bufferedWriter.write(nx+" "+nz+" "+dx+" "+dz+" "+_x0+" "+_z0+" "+unit);
			bufferedWriter.newLine();
			nz--;
			for(int i=0; i<nz; i++) {
				for(int j=0; j<s; j++) {
					bufferedWriter.write(vel[k]+" "); k++;
					bufferedWriter.write(vel[k]+" "); k++;
					bufferedWriter.write(vel[k]+" "); k++;
					bufferedWriter.write(vel[k]+" "); k++;
					bufferedWriter.write(vel[k]+" "); k++;
					bufferedWriter.write(vel[k]+" "); k++;
					bufferedWriter.newLine();	
				}
				for(int j=0; j<t; j++) {
					bufferedWriter.write(vel[k]+" "); k++;
				}
				bufferedWriter.newLine();	
			}
			for(int j=0; j<s; j++) {
				bufferedWriter.write(vel[k]+" "); k++;
				bufferedWriter.write(vel[k]+" "); k++;
				bufferedWriter.write(vel[k]+" "); k++;
				bufferedWriter.write(vel[k]+" "); k++;
				bufferedWriter.write(vel[k]+" "); k++;
				bufferedWriter.write(vel[k]+" "); k++;
				bufferedWriter.newLine();	
			}
			for(int j=0; j<t; j++) {
				bufferedWriter.write(vel[k]+" "); k++;
			}
		} catch (IOException ex) {
			ex.printStackTrace();
		} finally {
			// Close the BufferedWriter
			try {
				if (bufferedWriter != null) {
					bufferedWriter.flush();
					bufferedWriter.close();
				}
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
	}

	public double [] getBoundaryDip() {
		double [] dip = new double[getNumOfBoundaries()];
		for(int i=0; i<getNumOfBoundaries(); i++) {
			dip[i] = getBoundaryDip(i);
		}
		return dip;
	}
	//+ up, - down
	public double getBoundaryDip( int iBoundary ) {
		double a, b, dip;
		a 		= _x[_i1[iBoundary]]-_x[_i2[iBoundary]];
		b 		= _z[_i1[iBoundary]]-_z[_i2[iBoundary]];
		b 		= Math.abs(b);
		dip 	= Math.atan(b/Math.sqrt(a*a+b*b));
		dip 	*= 180.0/Math.PI; 
		return dip;
	}
	//+ up, - down
	public double getBoundaryDerivative( int iBoundary ) {
		return (_z[_i2[iBoundary]]-_z[_i1[iBoundary]])/(_x[_i2[iBoundary]]-_x[_i1[iBoundary]]);
	}

	public double calRightDepth(double x0, double x1, double leftDepth, double angle ) {
		if(angle==0.0) return leftDepth;
		return leftDepth-(x1-x0)*Math.tan(angle*Math.PI/180.0);
	}
	public double [] calRightDepth(double x0, double x1, double [] leftDepths, double [] angles ) {
		int nBoundaries = leftDepths.length;
		double [] rightDepths = new double[nBoundaries];
		for(int i=0; i<nBoundaries; i++)	{
			if(i==0 || i==nBoundaries-1) {
				rightDepths[i] = leftDepths[i];
			} else {
				rightDepths[i] = calRightDepth(x0, x1, leftDepths[i], angles[i]);
			}
		}
		return rightDepths;
	}

	public String to1DString(double atX)	{
		String a;
		double [] TVD = getIntersectionDepth( atX );
		String b = new String("Depth(ft) VP(ft/s) VS(ft/s) EPSILON DELTA GAMMA X(ft) \n");

		if(_iUnit==1) {
			b=new String("Depth(m) VP(m/s) VS(m/s) EPSILON DELTA GAMMA X(m) \n");
		} else if(_iUnit==2) {
			b=new String("Depth(ft) VP(ft/s) VS(ft/s) EPSILON DELTA GAMMA X(ft) \n");
		} else if(_iUnit==3) {
			b=new String("Depth(km) VP(km/s) VS(km/s) EPSILON DELTA GAMMA X(km) \n");
		} else { }

		for(int i=0; i<_nBoundaries; i++) {
			int j = i;
			if(i==_nBoundaries-1) {
				j = i-1;
			}
			a = String.format( "%15.5f %15.5f %15.5f %15.5f %15.5f %15.5f %15.5f \n", 
					TVD[i], getLayerVelocity(j, 1), getLayerVelocity(j, 2), 
					getLayerEpsilon(j), getLayerDelta(j), getLayerGamma(j), atX );
			b = new String(b.concat(a));
		}

		return b;
	}

	public String toString()	{
		String a;
		String b = new String(" ");

		// Units for density are 1000 kg/m3 = 1000 g/l = 1 g/cm3.
		// The SI unit for density is: kilograms per cubic metre (kg/m3).
		// At 4 degrees Celsius, water has a density of 1000 kg/m3.

		if(_iUnit==1) {
			a=new String( "Bdry#, Node_X(m), Node_Z(m), VP1(m/s), VP2(m/s), VS1(m/s), VS2(m/s), " + 
					"DEN1(g/cm3), DEN2(g/cm3), THETA1, THETA2, DELTA1, DELTA2, EPSILON1, EPSILON2, GAMMA1, GAMMA2");
		} else if(_iUnit==2) {
			a=new String( "Bdry#, Node_X(ft), Node_Z(ft), VP1(ft/s), VP2(ft/s), VS1(ft/s), VS2(ft/s), " + 
					"DEN1(g/cm3), DEN2(g/cm3), THETA1, THETA2, DELTA1, DELTA2, EPSILON1, EPSILON2, GAMMA1, GAMMA2");
		} else if(_iUnit==3) {
			a=new String( "Bdry#, Node_X(km), Node_Z(km), VP1(km/s), VP2(km/s), VS1(km/s), VS2(km/s), " + 
					"DEN1(g/cm3), DEN2(g/cm3), THETA1, THETA2, DELTA1, DELTA2, EPSILON1, EPSILON2, GAMMA1, GAMMA2");
		} else {
			a=new String( "Bdry#, Node_X(ft), Node_Z(ft), VP1(ft/s), VP2(ft/s), VS1(ft/s), VS2(ft/s), " + 
					"DEN1(g/cm3), DEN2(g/cm3), THETA1, THETA2, DELTA1, DELTA2, EPSILON1, EPSILON2, GAMMA1, GAMMA2");
		}
		b = new String(b.concat(a+"\n"));
		
		for(int i=0; i<_nNodes; i++) {
			a = String.format( "%3d, %10.2f, %10.2f, %10.2f, %10.2f, %10.2f, %10.2f, %10.2f, %10.2f, %10.2f, %10.2f, " + 
					"%10.2f, %10.2f, %10.2f, %10.2f, %10.2f, %10.2f", 
					_bndryNo[i], _x[i], _z[i], _vp1[i], _vp2[i], _vs1[i], _vs2[i], _den1[i], _den2[i], _theta1[i], _theta2[i], 
					_delta1[i], _delta2[i], _epsilon1[i], _epsilon2[i], _gamma1[i], _gamma2[i] );
			b = new String(b.concat(a+"\n"));
		}

		return b;
	}

	public String toDeepString( )	{
		String a;
		String b=toString( );

		a = String.format( "x0=%f x1=%f z0=%f z1=%f nBndry=%d nNode=%d \n", _x0, _x1, _z0, _z1, _nBoundaries, _nNodes ); 
		b = new String(b.concat(a));

		for(int i=0; i<_nBoundaries; i++) { 
			a = String.format( "i1[%d]=%d \ti2[%d]=%d\n", i, _i1[i], i, _i2[i]); 
			b = new String(b.concat(a));
		}
		for(int i=0; i<_nBoundaries; i++) { 
			for(int j=_i1[i]; j<=_i2[i]; j++) {
				a = String.format( "j=%d [%f, %f] ", j, _x[j], _z[j]); 
				b = new String(b.concat(a));
			}
			b = new String(b.concat("\n"));
		}
		
		return b;
	}
	
	public double getMinimunLayerThickness() {
		double min = 1.0e10;
		double a = 0.0;
		for(int i=0; i<_nBoundaries-1; i++) { 
			for(int j=_i1[i]; j<=_i2[i]; j++) {
				a = getIntersectionDepth(i+1, _x[j]);
				a = Math.abs(a-_z[j]);
				min = min<a ? min:a;
			}
		}
		return min;
	}

	public double [] to1DTopFormat() {
		double [] leftDepth = getIntersectionDepth( _x0 );
		double [] dip = getBoundaryDip( );
		double [] vp = getLayerVelocity( 1 );
		double [] vs = getLayerVelocity( 2 );

		int n = 5;
		int m = _nBoundaries+1;
		double [] v = new double[m*n];

		int k = 0;
		v[k] = (double)_nBoundaries;k++;
		v[k] = (double)_nNodes;		k++;
		v[k] = (double)_iUnit; 		k++;
		v[k] = _x0; 				k++;
		v[k] = _x1; 				k++;

		for(int i=0; i<_nBoundaries; i++) { 
			v[k] = (double)(i+1);	k++;
			v[k] = leftDepth[i];	k++;
			v[k] = vp[i]; 			k++;
			v[k] = vs[i];  			k++;
			v[k] = dip[i]; 			k++;
		}

		return v;
	}

	public String toString1DTopFormat() {
		String a = null;
		String b = null;

		if(_iUnit==1) {
			b=new String("Depth(m), Vp(m/s), Vs(m/s), Dip(deg)  \n");
		} else if(_iUnit==2) {
			b=new String("Depth(ft), Vp(ft/s), Vs(ft/s), Dip(deg) \n");
		} else if(_iUnit==3) {
			b=new String("Depth(km), Vp(km/s), Vs(km/s), Dip(deg)  \n");
		} else { }

		double [] v = to1DTopFormat();
		int k = 0;
		double v1 = v[k]; k++;
		double v2 = v[k]; k++;
		double v3 = v[k]; k++;
		double v4 = v[k]; k++;
		double v5 = v[k]; k++;
		a = String.format("%f, %f, %d, 0.0\n", v4, v5, (int)v3); 
		b = new String(b.concat(a));

		for(int i=0; i<_nBoundaries; i++) { 
			v1 = v[k]; k++;
			v2 = v[k]; k++;
			v3 = v[k]; k++;
			v4 = v[k]; k++;
			v5 = v[k]; k++;
			a = String.format("%f, %f, %f, %f\n", v2, v3, v4, v5); 
			b = new String(b.concat(a));
		}
		return b;
	}

	//given a point, return its boundary index
	public int getBoundaryIndex(double x, double y) {
		int k = 0;
		double [] topDepth = getIntersectionDepth(x);
		
		if(y<=topDepth[0]) return 0;
		else if(y>=topDepth[topDepth.length-1]) return getNumOfBoundaries()-1;
		else {
			for(int i=0; i<getNumOfBoundaries(); i++)	{
				if(y>topDepth[i] && y<=topDepth[i+1]) {
					return i;
				}
			}
		}
		return k;
	}
	public double [] getIntersectionDepth( double x ) {
		double [] TVD;

		TVD = new double[_nBoundaries];
		for(int i=0; i<_nBoundaries; i++)	{
			TVD[i] = getIntersectionDepth(i, x);
		}
		return TVD;
	}
	public double getIntersectionDepth(int iBoundary, double x ) {
		int k = getIndexOfSegment(iBoundary, x);
		double z0 = _z[k]; 
		double z1 = _z[k+1];
		if(x==_x[k]) {
			return z0;
		}else if(x==_x[k+1]) {
			return z1;
		} else {
			double fraction = (x-_x[k+1])/(_x[k]-_x[k+1]);
			return z1 + fraction*(z0-z1);
		}
	}
	public int getIndexOfSegment(int iBoundary, double x) {
		for(int i=_i1[iBoundary]; i<_i2[iBoundary]; i++)	{
			if(x>=_x[i] && x<_x[i+1]) {
				return i;
			}
		}
		return 0;
	}
	
	
	public int getNumOfNodes() {
		return _nNodes;
	}
	public int getNumOfNodes(int iBoundary) {
		int k = 0;
		for(int i=0; i<getNumOfNodes(); i++) {
			if(_bndryNo[i]==iBoundary) k++;
		}
		return k;
	}
	public int getMinNumOfNodesPerBoundary() {
		int min=100000000;
		for(int i=0; i<getNumOfBoundaries(); i++) {
			min = min<getNumOfNodes(i) ? min:getNumOfNodes(i);
		}
		return min;
	}
	public int getMaxNumOfNodesPerBoundary() {
		int max=-100000000;
		for(int i=0; i<getNumOfBoundaries(); i++) {
			max = max>getNumOfNodes(i) ? max:getNumOfNodes(i);
		}
		return max;
	}
	public int getNumOfBoundaries() {
		return _nBoundaries;
	}
	public int getUnit() {
		return _iUnit;
	}

	
	public void setUnit( int iUnit ) {
		_iUnit = iUnit;
	}

	public double [] getLayerVelocity( int iVp ) {
		double [] v = new double[_nBoundaries];
		for(int i=0; i<_nBoundaries; i++)	{
			v[i] = getLayerVelocity( i, iVp );
		}
		return v;
	}
	
	public boolean svelExist() {
		for(int i=0; i<_nBoundaries-1; i++)	{
			if(getLayerVelocity( i, 2 )<=0.0) return false;
		}
		return true;
	}
	
	public double getLayerVelocity( int iBoundary, int iVp) {
		if( iVp==1||iVp==11 ) {
			return getLayerVp( iBoundary);
		} else if( iVp==2||iVp==12||iVp==3||iVp==13 ) {
			return getLayerVs( iBoundary);
		} else {  }
		return 1.0;
	}
	public void setLayerVelocity( int iBoundary, int iVp, double v) {
		if( iVp==1||iVp==11 ) {
			setLayerVp( iBoundary, v);
		} else if( iVp==2||iVp==12||iVp==3||iVp==13 ) {
			setLayerVs( iBoundary, v);
		} else {  }
	}
	public void updateLayerVelocity( int iBoundary, int iVp, double v) {
		updateLayerProperties(1, iBoundary, v);
		if( iVp==1||iVp==11 ) {
			updateLayerVp(iBoundary, v);
		} else if( iVp==2||iVp==12||iVp==3||iVp==13 ) {
			updateLayerVs(iBoundary, v);
		} else {  }
	}
	public void updateLayerVelocity( int iBoundary, int iVp) {
		double v = getLayerProperties(1, iBoundary);
		updateLayerVelocity(iBoundary, iVp, v);
	}
	
	public double getLayerVp( int iBoundary) {
		return getLayerProperties(1, iBoundary);
	}
	public void setLayerVp( int iBoundary, double v) {
		setLayerProperties(1, iBoundary, v);
	}
	public void updateLayerVp( int iBoundary, double v) {
		updateLayerProperties(1, iBoundary, v);
	}
	public void updateLayerVp( int iBoundary) {
		double v = getLayerProperties(1, iBoundary);
		updateLayerProperties(1, iBoundary, v);
	}
	
	public double getLayerVs( int iBoundary) {
		return getLayerProperties(2, iBoundary);
	}
	public void setLayerVs( int iBoundary, double v) {
		setLayerProperties(2, iBoundary, v);
	}
	public void updateLayerVs( int iBoundary, double v) {
		updateLayerProperties(2, iBoundary, v);
	}
	public void updateLayerVs( int iBoundary) {
		double v = getLayerProperties(2, iBoundary);
		updateLayerProperties(2, iBoundary, v);
	}
	public double getLayerDen( int iBoundary) {
		return getLayerProperties(3, iBoundary);
	}
	public void setLayerDen( int iBoundary, double v) {
		setLayerProperties(3, iBoundary, v);
	}
	public void updateLayerDen( int iBoundary, double v) {
		updateLayerProperties(3, iBoundary, v);
	}
	public void updateLayerDen( int iBoundary) {
		double v = getLayerProperties(3, iBoundary);
		updateLayerProperties(3, iBoundary, v);
	}
	
	public void setLayerPhi( int iBoundary, double v) {
		setLayerProperties(4, iBoundary, v);
	}
	public void updateLayerPhi( int iBoundary, double v) {
		updateLayerProperties(4, iBoundary, v);
	}
	public void updateLayerPhi( int iBoundary) {
		double v = getLayerProperties(4, iBoundary);
		updateLayerProperties(4, iBoundary, v);
	}
	
	public double getLayerDelta( int iBoundary) {
		return getLayerProperties(5, iBoundary);
	}
	public void setLayerDelta( int iBoundary, double v) {
		setLayerProperties(5, iBoundary, v);
	}
	public void updateLayerDelta( int iBoundary, double v) {
		updateLayerProperties(5, iBoundary, v);
	}
	public void updateLayerDelta( int iBoundary) {
		double v = getLayerProperties(5, iBoundary);
		updateLayerProperties(5, iBoundary, v);
	}
	
	public double getLayerEpsilon( int iBoundary) {
		return getLayerProperties(6, iBoundary);
	}
	public void setLayerEpsilon( int iBoundary, double v) {
		setLayerProperties(6, iBoundary, v);
	}
	public void updateLayerEpsilon( int iBoundary, double v) {
		updateLayerProperties(6, iBoundary, v);
	}
	public void updateLayerEpsilon( int iBoundary) {
		double v = getLayerProperties(6, iBoundary);
		updateLayerProperties(6, iBoundary, v);
	}
	
	public double getLayerGamma( int iBoundary) {
		return getLayerProperties(7, iBoundary);
	}
	public void setLayerGamma( int iBoundary, double v) {
		setLayerProperties(7, iBoundary, v);
	}
	public void updateLayerGamma( int iBoundary, double v) {
		updateLayerProperties(7, iBoundary, v);
	}
	public void updateLayerGamma( int iBoundary) {
		double v = getLayerProperties(7, iBoundary);
		updateLayerProperties(7, iBoundary, v);
	}
	
	public double getLayerTheta( int iBoundary) {
		return getLayerProperties(4, iBoundary);
	}
	public void setLayerTheta( int iBoundary, double v) {
		setLayerProperties(4, iBoundary, v);
	}
	public void updateLayerTheta( int iBoundary, double v) {
		updateLayerProperties(4, iBoundary, v);
	}
	public void updateLayerTheta( int iBoundary) {
		double v = getLayerProperties(4, iBoundary);
		updateLayerProperties(4, iBoundary, v);
	}
	private double [] getLayerAttibute1(int iColumn) {
		double [] data = null;
		if(iColumn==1) 		data = _vp1;
		else if(iColumn==2) data = _vs1;
		else if(iColumn==3) data = _den1;
		else if(iColumn==4) data = _theta1;
		else if(iColumn==5) data = _delta1;
		else if(iColumn==6) data = _epsilon1;
		else if(iColumn==7) data = _gamma1;
		else  				data = _vp1;
		return data;
	}
	private double [] getLayerAttibute2(int iColumn) {
		double [] data = null;
		if(iColumn==1) 		data = _vp2;
		else if(iColumn==2) data = _vs2;
		else if(iColumn==3) data = _den2;
		else if(iColumn==4) data = _theta2;
		else if(iColumn==5) data = _delta2;
		else if(iColumn==6) data = _epsilon2;
		else if(iColumn==7) data = _gamma2;
		else  				data = _vp2;
		return data;
	}
	public double getLayerProperties(int iColumn, int iBoundary) {
		double [] data2 = getLayerAttibute2(iColumn);
		return data2[_i1[iBoundary]];
	}
	
	public void setLayerProperties(int iColumn, int iBoundary, double v) {
		double [] data2 = getLayerAttibute2(iColumn);
		data2[_i1[iBoundary]] = v;
		updateLayerProperties(iColumn, iBoundary, v);
	}
	
	public void updateLayerProperties(int iColumn, int iBoundary, double v) {
		double [] data1 = getLayerAttibute1(iColumn);
		double [] data2 = getLayerAttibute2(iColumn);
		for(int i=_i1[iBoundary]; i<=_i2[iBoundary]; i++) {
			data2[i] = v;
		}

		if(iBoundary<getNumOfBoundaries()-1) {
			for(int i=_i1[iBoundary+1]; i<=_i2[iBoundary+1]; i++) {
				data1[i] = v;
			}
		}
	}

	//vp = 1; vs = 2
	public double getLayerVelocityMaxValue(int iVp) {
		if( iVp==1||iVp==11 ) {
			return getLayerPropertyMaxValue(1);
		} else if( iVp==2||iVp==12||iVp==3||iVp==13 ) {
			return getLayerPropertyMaxValue(2);
		} else {  }
		return 0.0;
	}
	public int getLayerVelocityMaxValueIndex(int iVp) {
		if( iVp==1||iVp==11 ) {
			return getLayerPropertyMaxValueIndex(1);
		} else if( iVp==2||iVp==12||iVp==3||iVp==13 ) {
			return getLayerPropertyMaxValueIndex(2);
		} else {  }
		return 0;
	}
	public double getLayerVelocityMinValue(int iVp) {
		if( iVp==1||iVp==11 ) {
			return getLayerPropertyMinValue(1);
		} else if( iVp==2||iVp==12||iVp==3||iVp==13 ) {
			return getLayerPropertyMinValue(2);
		} else {  }
		return 0.0;
	}
	public int getLayerVelocityMinValueIndex(int iVp) {
		if( iVp==1||iVp==11 ) {
			return getLayerPropertyMinValueIndex(1);
		} else if( iVp==2||iVp==12||iVp==3||iVp==13 ) {
			return getLayerPropertyMinValueIndex(2);
		} else {  }
		return 0;
	}
	
	public double getLayerVpMaxValue() {
		return getLayerPropertyMaxValue(1);
	}
	public int getLayerVpMaxValueIndex() {
		return getLayerPropertyMaxValueIndex(1);
	}
	public double getLayerVpMinValue() {
		return getLayerPropertyMinValue(1);
	}
	public int getLayerVpMinValueIndex() {
		return getLayerPropertyMinValueIndex(1);
	}
	public double getLayerVsMaxValue() {
		return getLayerPropertyMaxValue(2);
	}
	public int getLayerVsMaxValueIndex() {
		return getLayerPropertyMaxValueIndex(2);
	}
	public double getLayerVsMinValue() {
		return getLayerPropertyMinValue(2);
	}
	public int getLayerVsMinValueIndex() {
		return getLayerPropertyMinValueIndex(2);
	}

	public double getLayerPropertyMinValue(int iColumn) {
		double min = 1.0e10;
		double v = 0.0;
		for(int i=0; i<_nBoundaries-1; i++) {
			v = getLayerProperties(iColumn, i);
			if(min>v) {
				min = v;
			}
		}
		return min;
	}
	public int getLayerPropertyMinValueIndex(int iColumn) {
		double min = 1.0e10;
		double v = 0.0;
		int k = 0;
		for(int i=0; i<_nBoundaries-1; i++) {
			v = getLayerProperties(iColumn, i);
			if(min>v) {
				min = v;
				k = i;
			}
		}
		return k;
	}
	public double getLayerPropertyMaxValue(int iColumn) {
		double max = -1.0e10;
		double v = 0.0;
		for(int i=0; i<_nBoundaries-1; i++) {
			v = getLayerProperties(iColumn, i);
			if(max<v) {
				max = v;
			}
		}
		return max;
	}
	public int getLayerPropertyMaxValueIndex(int iColumn) {
		double max = -1.0e10;
		double v = 0.0;
		int k = 0;
		for(int i=0; i<_nBoundaries-1; i++) {
			v = getLayerProperties(iColumn, i);
			if(max<v) {
				max = v;
				k = i;
			}
		}
		return k;
	}
	
	public double [] getLeftEdgeDepth( ) {
		double [] dp = new double[_nBoundaries];
		for(int i=0; i<_nBoundaries; i++)	{
			dp[i] = getLeftEdgeDepth( i );
		}
		return dp;
	}
	public double getLeftEdgeDepth( int iBoundary ) {
		return _z[_i1[iBoundary]];
	} 
	public double [] getRightEdgeDepth( ) {
		double [] dp = new double[_nBoundaries];
		for(int i=0; i<_nBoundaries; i++)	{
			dp[i] = getRightEdgeDepth( i );
		}
		return dp;
	}
	public double getRightEdgeDepth( int iBoundary ) {
		return _z[_i2[iBoundary]];
	} 

	public double [] getLeftEdgeX( ) {
		double [] dp = new double[_nBoundaries];
		for(int i=0; i<_nBoundaries; i++)	{
			dp[i] = getLeftEdgeX( i );
		}
		return dp;
	}
	public double getLeftEdgeX( int iBoundary ) {
		return _x[_i1[iBoundary]];
	} 
	public double [] getRightEdgeX( ) {
		double [] dp = new double[_nBoundaries];
		for(int i=0; i<_nBoundaries; i++)	{
			dp[i] = getRightEdgeX( i );
		}
		return dp;
	}
	public double getRightEdgeX( int iBoundary ) {
		return _x[_i2[iBoundary]];
	} 
	public double getX0() {	return _x0; }
	public double getX1() {	return _x1; }
	public double getZ0() {	return _z0; }
	public double getZ1() {	return _z1; }
	
	public String validate(int id, int iVp, int iPS ) {
		int errorID = 0, errorIDP = 0, errorIDS = 0;
		String errorMsg = new String("OK");

		//0) at least 2 layers
		if(_nBoundaries<2) {
			errorMsg = new String("ERROR 001: At least 2 layers for a model!\n");
			errorID = 1;
		}

		//1) only two nodes on the first and the last boundaries
		if(_i2[0]-_i1[0]!=1) {
			errorMsg = new String("ERROR 011: Only two nodes allowed on the first boundary!\n");
			errorID = 11;
		}
		if(_i2[_nBoundaries-1]-_i1[_nBoundaries-1]!=1) {
			errorMsg = new String("ERROR 012: Only two nodes allowed on the last boundary!\n");
			errorID = 12;
		}

		//2) the flat layer on the top and bottom
		if(_z[0]!=_z[1]) {
			errorMsg = new String("ERROR 021: The first boundary must be flat!\n");
			errorID = 21;
		}
		if(_z[_nNodes-2]!=_z[_nNodes-1]) {
			errorMsg = new String("ERROR 022: The last boundary must be flat!\n");
			errorID = 22;
		}

		//3) at least two nodes for each boundary
		for(int i=0; i<_nBoundaries; i++)	{
			if(_i2[i]-_i1[i]<1) {
				errorMsg = new String("ERROR 031: At least two nodes at one of boundaries!\n");
				errorID = 31;
				break;
			}
		}

		//4) Min layer thickness is 1.0
		for(int i=1; i<_nBoundaries; i++)	{
			for(int j=_i1[i]; j<=_i2[i]; j++)	{
				int k = j-2;
				if(_z[j]-_z[k]<1) {
					errorMsg = new String("ERROR 041: Min layer thickness is 2.0!\n");
					errorID = 41;
					break;
				}
			}
		}

		//5) Vp>0.0
		for(int i=1; i<_nBoundaries; i++)	{
			for(int j=_i1[i]; j<=_i2[i]; j++)	{
				if(_vp1[j]==0.0||_vp2[j]==0.0) {
					errorMsg = new String("ERROR 051: P-wave velocity at one of nodes is zero!\n");
					errorIDP = 51;
					break;
				}
			}
		}

		//6) Vs>0.0
		if(iPS==1||iVp==2||iVp==3||iVp==12||iVp==13) {
			for(int i=1; i<_nBoundaries; i++)	{
				for(int j=_i1[i]; j<=_i2[i]; j++)	{
					if(_vs1[j]==0.0||_vs2[j]==0.0) {
						errorMsg = new String("ERROR 061: S-wave velocity at one of nodes is zero!\n");
						errorIDS = 61;
						break;
					}
				}
			}
		}

		if(errorIDP==51 && errorIDS==61) {
			errorMsg = new String("ERROR 051: Both P and S-wave velocity at one of nodes are zero!\n");
			errorID = 51;
		} else {
			errorMsg = new String("OK");
			errorID = 0;
		}

		return errorMsg;
	}

	public void spline(int n, double []x, double []y, double yp1, double ypn, double []y2) {
		int i,k;
		double  p,qn,sig,un;
		double []u = new double[n-1];

		if (yp1 > 0.99e30)
			y2[0]=u[0]=0.0;
		else {
			y2[0] = -0.5;
			u[0]=(3.0/(x[1]-x[0]))*((y[1]-y[0])/(x[1]-x[0])-yp1);
		}
		for (i=1;i<n-1;i++) {
			sig=(x[i]-x[i-1])/(x[i+1]-x[i-1]);
			p=sig*y2[i-1]+2.0;
			y2[i]=(sig-1.0)/p;
			u[i]=(y[i+1]-y[i])/(x[i+1]-x[i]) - (y[i]-y[i-1])/(x[i]-x[i-1]);
			u[i]=(6.0*u[i]/(x[i+1]-x[i-1])-sig*u[i-1])/p;
		}
		if (ypn > 0.99e30)
			qn=un=0.0;
		else {
			qn=0.5;
			un=(3.0/(x[n-1]-x[n-2]))*(ypn-(y[n-1]-y[n-2])/(x[n-1]-x[n-2]));
		}
		y2[n-1]=(un-qn*u[n-2])/(qn*y2[n-2]+1.0);
		for (k=n-2;k>=0;k--)
			y2[k]=y2[k]*y2[k+1]+u[k];
	}

	public void splint(int n, double []xa, double []ya, double []y2a, double x, double y) {
		int k;
		double h,b,a;

		int klo=0;
		int khi=n-1;
		while (khi-klo > 1) {
			k=(khi+klo) >> 1;
			if (xa[k] > x) khi=k;
			else klo=k;
		}
		h=xa[khi]-xa[klo];
		if (h == 0.0) System.out.println("Bad xa input to routine splint");
		a=(xa[khi]-x)/h;
		b=(x-xa[klo])/h;
		y=a*ya[klo]+b*ya[khi]+((a*a*a-a)*y2a[klo]
				+(b*b*b-b)*y2a[khi])*(h*h)/6.0;
	}

	public void splint(int iBndry, int n, double[] x, double[] y) {
		int i, j;
		double h,b,a;
		int [] khi = new int[n];

		b = _x[_i1[iBndry]];
		a = _x[_i2[iBndry]] - b;
		h = a/(n-1);
		for(i=0; i<n; i++) {
			x[i] = b + i*h;
		}

		i = 0;
		for(j=_i1[iBndry]+1; j<=_i2[iBndry]; j++) {
			while( i<n ) {
				if(x[i]<=_x[j]) {
					khi[i] = j;
				} else {
					break;
				}
				i++;
			}
		}

//		for(i=0; i<n; i++) {
//			h=_x[khi[i]]-_x[khi[i]-1];
//			a=(_x[khi[i]]-x[i])/h;
//			b=(x[i]-_x[khi[i]-1])/h;
//			y[i]=a*_z[khi[i]-1]+b*_z[khi[i]]+((a*a*a-a)*_dz2[khi[i]-1]+(b*b*b-b)*_dz2[khi[i]])*(h*h)/6.0;		
//		}
	}

	public void read( String selectedFileName){
		read(selectedFileName, 1);
	}
	public void read(String selectedFileName, int iUnit) {
		_iUnit = iUnit;
		try{
			int nLines = readNumOfLines(selectedFileName)-1;
			allocate2(nLines);
			
			BufferedReader reader1 = new BufferedReader(new FileReader(selectedFileName));
			StringTokenizer st = null;
			String line = reader1.readLine();
			for(int i=0; i<nLines; i++) {
				line=reader1.readLine();
				st = new StringTokenizer(line, " ,");
				_bndryNo[i] 	= Integer.parseInt(st.nextToken().trim());
				_x[i] 			= Double.parseDouble(st.nextToken().trim());
				_z[i] 			= Double.parseDouble(st.nextToken().trim());
				_vp1[i] 		= Double.parseDouble(st.nextToken().trim());
				_vp2[i] 		= Double.parseDouble(st.nextToken().trim());
				_vs1[i] 		= Double.parseDouble(st.nextToken().trim());
				_vs2[i] 		= Double.parseDouble(st.nextToken().trim());
				_den1[i] 		= Double.parseDouble(st.nextToken().trim());
				_den2[i] 		= Double.parseDouble(st.nextToken().trim());

				_theta1[i] 		= Double.parseDouble(st.nextToken().trim());
				_theta2[i] 		= Double.parseDouble(st.nextToken().trim());
				_delta1[i] 		= Double.parseDouble(st.nextToken().trim());
				_delta2[i] 		= Double.parseDouble(st.nextToken().trim());
				_epsilon1[i] 	= Double.parseDouble(st.nextToken().trim());
				_epsilon2[i] 	= Double.parseDouble(st.nextToken().trim());
				_gamma1[i] 		= Double.parseDouble(st.nextToken().trim());
				_gamma2[i] 		= Double.parseDouble(st.nextToken().trim());
			}
			reader1.close();

			allocate1(calNumOfBoundaries());
			calI1I2();
			calModelSize();
		} catch (IOException ioexception) {
			System.out.println("I am here 2");
			JOptionPane.showMessageDialog(null, "\nFile Format is not right!", 
					"Error", JOptionPane.ERROR_MESSAGE);
		}	
	}
	public int readNumOfLines(String selectedFileName) {
		int nLines = 0;
		try{
			BufferedReader reader = new BufferedReader(new FileReader(selectedFileName));
			while ((reader.readLine()) != null) {
				nLines++;
			}
			reader.close();
		} catch (IOException ioexception) {
			System.out.println("I am here 1");
			JOptionPane.showMessageDialog(null, "\nFile Format is not right!", 
					"Error", JOptionPane.ERROR_MESSAGE);
		}	
		return nLines;
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
	
}
