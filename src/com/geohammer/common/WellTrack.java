
package com.geohammer.common;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;

import com.geohammer.core.TextualDataLoaderAndFilter;
import com.geohammer.core.acquisition.VCPair;

public class WellTrack  extends TextualDataLoaderAndFilter{
	
	String 		_wellName 			= "unknown";
	String 		_stageName 			= "unknown";
	String 		_geometryName 		= null;
	float[] 	_geometryPar		= null;

	public WellTrack() {
		super();
	}
	public WellTrack(int nRows) {
		this(nRows, 3);
	}
	public WellTrack(int nRows, int nCols) {
		super(nRows, nCols);
	}
	public WellTrack(double [] dataX, double [] dataZ) {
		this(dataX.length);
		for(int i=0; i<getNumOfPoints(); i++) {
			_data[i][0] = (float)dataX[i];
			_data[i][1] = 0.0f;
			_data[i][2] = (float)dataZ[i];
		}
	}
	public WellTrack(double [] dataX, double [] dataY, double [] dataZ) {
		this(dataX, dataY, dataZ, null);
	}
	public WellTrack(double [] dataX, double [] dataY, double [] dataZ, double [] md) {
		this(dataX.length, md==null?3:4);
		for(int i=0; i<getNumOfPoints(); i++) {
			_data[i][0] = (float)dataX[i];
			_data[i][1] = (float)dataY[i];
			_data[i][2] = (float)dataZ[i];
			if(md!=null) _data[i][3] = (float)md[i];
		}
	}
	public WellTrack(float [] dataX, float [] dataY, float [] dataZ) {
		this(dataX, dataY, dataZ, null);
	}
	public WellTrack(float [] dataX, float [] dataY, float [] dataZ, float [] md) {
		this(dataX.length, md==null?3:4);
		for(int i=0; i<getNumOfPoints(); i++) {
			_data[i][0] = dataX[i];
			_data[i][1] = dataY[i];
			_data[i][2] = dataZ[i];
			if(md!=null) _data[i][3] = md[i];
		}
	}
	public WellTrack(int id, float [][] data) {
		this(data.length, data[0].length);
		for(int i=0; i<data.length; i++) {
			for(int j=0; j<data[i].length; j++) {
				_data[i][j] = data[i][j];
			}
		}
	}
	public WellTrack(double [][] data) {
		this(data.length);
		int nCols = data[0].length;
		if(nCols==2) {
			for(int i=0; i<getNumOfPoints(); i++) {
				_data[i][0] = (float)data[i][0];
				_data[i][1] = 0.0f;
				_data[i][2] = (float)data[i][1];
			}
		} else if(nCols==3) {
			for(int i=0; i<getNumOfPoints(); i++) {
				_data[i][0] = (float)data[i][0];
				_data[i][1] = (float)data[i][1];
				_data[i][2] = (float)data[i][2];
			}
		}
	}
	public WellTrack(double cx, double cy, double wz) {
		this(1);
		double [] dp = new double [] {wz};
		for(int i=0; i<getNumOfPoints(); i++) {
			_data[i][0] = (float)cx;
			_data[i][1] = (float)cy;
			_data[i][2] = (float)dp[i];
		}
	}
	public WellTrack(double cx, double cy, double wz0, double wz1) {
		this(2);
		double [] dp = new double [] {wz0, wz1};
		for(int i=0; i<getNumOfPoints(); i++) {
			_data[i][0] = (float)cx;
			_data[i][1] = (float)cy;
			_data[i][2] = (float)dp[i];
		}
	}
	public WellTrack(String inputFileName, int nHeaders, int nTails, int [] colIndex) {
		super(inputFileName, nHeaders, nTails, colIndex);
		parseHeader(getHeaderText());
	}
	public WellTrack(int dimension, String inputFileName) {
		super(inputFileName, 2, 0, new int [] {0, 1});
		parseHeader2D(getHeaderText());
		//printClass();
		insertColumn(1, _INVALID_F);
	}
	public WellTrack(String inputFileName) {
		super(inputFileName, 1, 0, new int [] {0, 1, 2});
		parseHeader(getHeaderText());
		//System.out.println(toString());
	}
	public WellTrack(String inputFileName, int skipNumOfLines) {
		super(inputFileName, skipNumOfLines, 1, 0, new int [] {0, 1, 2});
		parseHeader(getHeaderText());
		//System.out.println(toString());
	}
	public WellTrack(VCPair vcPW) {
		this(vcPW.getTotalNumOfReceivers());
		float [] x = new float[getNumOfPoints()];
		float [] y = new float[getNumOfPoints()];
		float [] z = new float[getNumOfPoints()];
		for(int i=0; i<getNumOfPoints(); i++) {
			x[i] = (float)vcPW.getRE(i);
			y[i] = (float)vcPW.getRN(i);
			z[i] = (float)vcPW.getRD(i);
		}
		VeconUtils.shellSort(z, x, y);
		for(int i=0; i<getNumOfPoints(); i++) {
			_data[i][0] = x[i];
			_data[i][1] = y[i];
			_data[i][2] = z[i];
		}
	}
	public void shift(double ax, double ay, double az) {
		for(int i=0; i<getNumOfPoints(); i++) {
			_data[i][0] += ax;
			_data[i][1] += ay;
			_data[i][2] += az;
		}	
	}
//	public WellTrack copy() {
//		TextualDataLoaderAndFilter a = super.copy();
//		//WellTrack wellTrack = (WellTrack)(super.copy());
//		WellTrack wellTrack = new WellTrack(a.getData()[0], a.getData()[1], a.getData()[2]);
//		wellTrack._wellName = _wellName;
//		wellTrack._geometryName = _geometryName;
//		if(_geometryPar!=null) for(int i=0; i<_geometryPar.length; i++) wellTrack._geometryPar[i] = _geometryPar[i];
//		
//		return wellTrack;
//	}
	public void parseHeader2D(String [] headerText) {
		if(headerText==null) return;
		if(headerText.length!=2) return;
		
		String line = headerText[1];
		StringTokenizer st = new StringTokenizer(line, " ,");
		st.nextToken();
		int nPoints = Integer.parseInt(st.nextToken().trim());
	}
	public void parseHeader(String [] headerText) {
		if(headerText==null) return;
		if(headerText.length==1) {
		}
		else if(headerText.length==2) {
			for(int i=0; i<headerText.length; i++) {
				String line = headerText[i];
				StringTokenizer st = new StringTokenizer(line, " ,");
				int nPoints = Integer.parseInt(st.nextToken().trim());
				if(st.hasMoreTokens()) _wellName = st.nextToken().trim();
			}
		} else  {
			_geometryPar = new float[headerText.length-1];
			for(int i=0, k=0; i<headerText.length; i++) {
				String line = headerText[i];
				StringTokenizer st = new StringTokenizer(line, " ,");
				if(i==0) {
					_geometryName = st.nextToken().trim();
				} else {
					_geometryPar[k++] = Float.parseFloat(st.nextToken().trim());
				}
			}
		}
	}
	public void rotateZ(double azimuth) {
		double theSin = Math.sin(azimuth*3.1415926/180.0);
		double theCos = Math.cos(azimuth*3.1415926/180.0);
		double x = 0.0;
		double y = 0.0;
		for(int i=0; i<getNumOfPoints(); i++) {
			x = getX(i)*theCos - getY(i)*theSin;
			y = getX(i)*theSin + getY(i)*theCos;
			setX(i, (float)x);
			setY(i, (float)y);
		}
	}

	public String getWellName() { return _wellName; }
	public void setWellName(String wellName) { _wellName = wellName; }
	public String getStageName() { return _stageName; }
	public void setStageName(String stageName) { _stageName = stageName; }
	public String getGeometryName() {
		return _geometryName;
	}
	public void setGeometryName(String geometryName) {
		_geometryName = geometryName;
	}
	public float [] getGeometryPar() {
		return _geometryPar;
	}
	public void setGeometryPar(float [] geometryPar) {
		_geometryPar = geometryPar;
	}
	public double getGeometryPar(int index) {
		return _geometryPar[index];
	}
	public void setGeometryPar(int index, float geometryPar) {
		_geometryPar[index] = geometryPar;
	}
	public void trucateUp(double z) {
		int j1 = getIndexOfFirstAbove(z);
		
		for(int i=0; i<j1; i++) {
			_data[i][0] = _INVALID_F;
			_data[i][1] = _INVALID_F;
			_data[i][2] = _INVALID_F;
		}	
		_data[j1][2] = (float)z;
		trim();
	}
	
	public void trucateDown(double z) {
		int j2 = getIndexOfFirstBelow(z);
		
		_data[j2][2] = (float)z;
		for(int i=j2+1; i<getNumOfPoints(); i++) {
			_data[i][0] = _INVALID_F;
			_data[i][1] = _INVALID_F;
			_data[i][2] = _INVALID_F;
		}	
		trim();
	}
	
	public WellTrack trim2(float minN, float maxN, float minE, float maxE, float minD, float maxD) {
		int k = 0;
		for(int i=0; i<_data.length; i++) {
			if(_data[i][0]>=minN && _data[i][0]<=maxN && 
					_data[i][1]>=minE && _data[i][1]<=maxE && 
					_data[i][2]>=minD && _data[i][2]<=maxD) k++;
		}
		if(k==0) return null;
		float [][] data = new float[3][k];
		k = 0;
		for(int i=0; i<_data.length; i++) {
			if(_data[i][0]>=minN && _data[i][0]<=maxN && 
					_data[i][1]>=minE && _data[i][1]<=maxE && 
					_data[i][2]>=minD && _data[i][2]<=maxD) {
				for(int j=0; j<3; j++) data[j][k] = _data[i][j];
				k++;
			}
		}
		
		WellTrack wellTrack = new WellTrack(data[0], data[1], data[2]);
		wellTrack._wellName = _wellName;
		wellTrack._geometryName = _geometryName;
		if(_geometryPar!=null) for(int i=0; i<_geometryPar.length; i++) wellTrack._geometryPar[i] = _geometryPar[i];
		
		return wellTrack;
	}
		
	public int trim(float minN, float maxN, float minE, float maxE, float minD, float maxD) {
		int k = 0;
		for(int i=0; i<_data.length; i++) {
			if(_data[i][0]>=minN && _data[i][0]<=maxN && 
					_data[i][1]>=minE && _data[i][1]<=maxE && 
					_data[i][2]>=minD && _data[i][2]<=maxD) k++;
		}
		float [][] data = new float[k][3];
		k = 0;
		for(int i=0; i<_data.length; i++) {
			if(_data[i][0]>=minN && _data[i][0]<=maxN && 
					_data[i][1]>=minE && _data[i][1]<=maxE && 
					_data[i][2]>=minD && _data[i][2]<=maxD) {
				for(int j=0; j<3; j++) data[k][j] = _data[i][j];
				k++;
			}
		}
		_data = data;

		_nRows = _data.length;
		return data.length;
		//isValid(0, _data, minN, maxN);
		//isValid(1, _data, minE, maxE);
		//isValid(2, _data, minD, maxD);
		//trim();
	}
	public WellTrack trim(int iCol, float min, float max) {
		int k = 0;
		for(int i=0; i<_data.length; i++) {
			if(_data[i][iCol]>=min && _data[i][iCol]<=max) k++;
		}
		if(k<=0) return this;
		
		int nCol = _data[0].length;
		float [][] data = new float[k][nCol];
		k = 0;
		for(int i=0; i<_data.length; i++) {
			if(_data[i][iCol]>=min && _data[i][iCol]<=max) {
				for(int j=0; j<nCol; j++) data[k][j] = _data[i][j];
				k++;
			}
		}
		
		WellTrack wellTrack = new WellTrack(0, data);
		wellTrack._wellName = _wellName;
		wellTrack._geometryName = _geometryName;
		if(_geometryPar!=null) {
			float [] geometryPar = new float[_geometryPar.length];			
			for(int i=0; i<_geometryPar.length; i++) geometryPar[i] = _geometryPar[i];			
			wellTrack.setGeometryPar(geometryPar);
		}
		
		return wellTrack;
	}
	public void trim(int iCol, float [][] data, float min, float max) {
		int k = 0;
		for(int i=0; i<data.length; i++) {
			if(data[i][iCol]>=min && data[i][iCol]<=max) k++;
		}
		float [] v = new float[k];
		k = 0;
		for(int i=0; i<data.length; i++) {
			if(data[i][iCol]>=min && data[i][iCol]<=max) { v[k] = data[i][iCol]; k++; }
		}
		//data[i][iCol] = v;
	}
	
	public WellTrack merge(WellTrack that) {
		WellTrack trackMerged = new WellTrack(getRowCount()+that.getRowCount(), getColumnCount()+that.getColumnCount());
		int k = 0;
		for(int i=0; i<getNumOfPoints(); i++) {
			trackMerged._data[k][0] = _data[i][0];
			trackMerged._data[k][1] = _data[i][1];
			trackMerged._data[k][2] = _data[i][2];
			k++;
		}

		for(int i=0; i<that.getNumOfPoints(); i++) {
			trackMerged._data[k][0] = that._data[i][0];
			trackMerged._data[k][1] = that._data[i][1];
			trackMerged._data[k][2] = that._data[i][2];
			k++;
		}
		return trackMerged;
	}
	
	public WellTrack uniformSample(double gaugeLength) {
		double sum = 0;		
		double dx = 0;
		double dy = 0;
		double dz = 0;
		double [] d = new double[getNumOfPoints()];
		for(int i=1; i<getNumOfPoints(); i++) {
			dx = (_data[i][0]-_data[i-1][0]);
			dy = (_data[i][1]-_data[i-1][1]);
			dz = (_data[i][2]-_data[i-1][2]);
			d[i] = Math.sqrt(dx*dx+dy*dy+dz*dz);
			sum += d[i];
		}
		
		double l = 0;
		double sum1 = 0;
		double sum2 = 0;
		double ratio = 0;
		int k = (int)(sum/gaugeLength);
		float [][] data = new float[k][3];
		for(int i=1; i<data.length; i++) {
			l = gaugeLength*i;
			sum1 = 0;
			sum2 = 0;
			k = 0;
			for(int j=1; j<d.length; j++) {
				sum1 += d[j];
				if(l>sum2 && l<=sum1) {
					k = j;
					ratio = (l-sum2)/(sum1-sum2);
					break;
				}
				sum2 = sum1;
			}
			//System.out.println("k="+k+" ratio="+ratio+" l="+l+" sum1="+sum1+" sum2="+sum2);			
			
			data[i][0] = (float)(_data[k-1][0]+ratio*(_data[k][0]-_data[k-1][0]));
			data[i][1] = (float)(_data[k-1][1]+ratio*(_data[k][1]-_data[k-1][1]));
			data[i][2] = (float)(_data[k-1][2]+ratio*(_data[k][2]-_data[k-1][2]));
		}
		data[0][0] = _data[0][0];
		data[0][1] = _data[0][1];
		data[0][2] = _data[0][2];
		
		WellTrack trackMerged = new WellTrack(data.length, 3);
		k = 0;
		for(int i=0; i<data.length; i++) {
			trackMerged._data[k][0] = data[i][0];
			trackMerged._data[k][1] = data[i][1];
			trackMerged._data[k][2] = data[i][2];
			k++;
		}
		
		return trackMerged;
	}
	
	private float [] getData(int iCol) {
		float [] x = new float[getNumOfPoints()];
		for(int i=0; i<getNumOfPoints(); i++) {
			if(iCol==0) x[i] = (float)getX(i);
			else if(iCol==1) x[i] = (float)getY(i);
			else if(iCol==2) x[i] = (float)getZ(i);
			else if(iCol==3) x[i] = (float)getT(i);
		}
		return x;
	}
	
	public float [] getX() 					{ return getData(0); }
	public float [] getY() 					{ return getData(1); }
	public float [] getZ() 					{ return getData(2); }
	public float [] getT() 					{ return getData(3); }	
	public double getX(int index) 			{ return _data[index][0]; }
	public double getY(int index) 			{ return _data[index][1]; }
	public double getZ(int index) 			{ return _data[index][2]; }
	public double getT(int index) 			{ return _data[index][3]; }
	public void setX(int index, float v) 	{ _data[index][0] = v; }
	public void setY(int index, float v) 	{ _data[index][1] = v; }
	public void setZ(int index, float v) 	{ _data[index][2] = v; }
	public void setT(int index, float v) 	{ _data[index][3] = v; }
	public int getNumOfPoints() 			{ return _data.length; }
	public int getRowCount() 				{ return _data.length; }
	
	public double getXFromZ(double z) {
		int j1 = getIndexOfFirstAbove(z);
		int j2 = getIndexOfFirstBelow(z);
		if(j1==j2) return _data[j1][0];
		
		double slope = (_data[j2][0]-_data[j1][0])/(_data[j2][2]-_data[j1][2]);
		return _data[j2][0]-slope*(_data[j2][2]-z);
	}
	public double getYFromZ(double z) {
		int j1 = getIndexOfFirstAbove(z);
		int j2 = getIndexOfFirstBelow(z);
		if(j1==j2) return _data[j1][1];
		
		double slope = (_data[j2][1]-_data[j1][1])/(_data[j2][2]-_data[j1][2]);
		return _data[j2][1]-slope*(_data[j2][2]-z);
	}
	public int getIndexOfFirstBelow(double z) {
		for(int i=0; i<getNumOfPoints(); i++) {
			if(getZ(i)>=z) {
				return i;
			} 
		} 
		return 0;
	}	
	public int getIndexOfFirstAbove(double z) {
		for(int i=getNumOfPoints()-1; i>=0; i--) {
			if(getZ(i)<=z) {
				return i;
			} 
		}
		return 0;
	}
	public void appendWellTrunk(String selectedFileName) {
		writeWellTrunk(selectedFileName, true);
	}
	public void writeWellTrunk(String selectedFileName) {
		writeWellTrunk(selectedFileName, false);
	}
	public void writeWellTrunk(String selectedFileName, boolean append) {
		String b = "";
		if(_geometryPar!=null) {
			b = new String(_geometryName);
			for(int i=0; i<_geometryPar.length; i++) {
				b = new String(b.concat(" "+Double.toString(_geometryPar[i])));
			}		
		} else {
			b = new String(b.concat(Integer.toString(getNumOfPoints())));
		}
		//System.out.println(toString());
		writeTextualData2(b, selectedFileName, null, append);
	}

	public void to2D() {
		for(int i=0; i<getNumOfPoints(); i++) {
			setY(i, _INVALID_F);
		}
	}
	public void to3D() {
		for(int i=0; i<getNumOfPoints(); i++) {
			setZ(i, (float)getY(i));
			setY(i, 0);
		}
	}
	public String toString() {
		return toString(1);
	}
	public String toString(int id) {
		String b = String.format("%d",  getNumOfPoints());
		String a = " ";

		if(_geometryPar!=null) {
			b = new String(_geometryName);
			for(int i=0; i<_geometryPar.length; i++) {
				b = new String(b.concat(" "+Double.toString(_geometryPar[i])));
			}		
		}
		for(int i=0; i<getNumOfPoints(); i++) {
			a = String.format("%f %f %f", getX(i), getY(i), getZ(i));
			b = new String(b.concat("\n"+a));
		}

		return b;
	}
	public void printClass() {
		String b = String.format("%d %s",  getNumOfPoints(), _wellName);
		System.out.println(b);
		if(_geometryPar!=null&&_geometryName!=null) {
			b = new String(_geometryName);
			for(int i=0; i<_geometryPar.length; i++) {
				b = new String(b.concat(" "+Double.toString(_geometryPar[i])));
			}		
		}
		for(int i=0; i<10; i++) {
		//for(int i=0; i<getNumOfPoints(); i++) {
			b = String.format("%f %f %f", getX(i), getY(i), getZ(i));
			System.out.println(b);
		}
	}
	public void flipDepthSign() {    
		for(int i=0; i<getNumOfPoints(); i++) {
			if(getZ(i)!=0.0) setZ(i, -(float)getZ(i));
		}
	}
	public void removeDuplicateWithOrder() {    
		ArrayList<Point3D> pvector = new ArrayList<Point3D>();
		for(int i=0; i<getNumOfPoints(); i++) {
			Point3D P= new Point3D(getX(i), getY(i), getZ(i));
			pvector.add(P);
		}
		removeDuplicateWithOrder(pvector);
		setRowCount(pvector.size());
		allocate(getRowCount(), getColumnCount());
		for(int i=0; i<getNumOfPoints(); i++) {
			Point3D P= pvector.get(i);
			setX(i, (float)P.getX());
			setY(i, (float)P.getY());
			setZ(i, (float)P.getZ());
		}
	}
	private void removeDuplicateWithOrder(ArrayList<Point3D> arlList) {    
		Set<Point3D> set 			= new HashSet<Point3D>();    
		ArrayList<Point3D> newList 	= new ArrayList<Point3D>();    
		for (Point3D element : arlList) {      
			if (set.add(element))  {
				newList.add(element);  
			}
		}    
		arlList.clear();    
		arlList.addAll(newList); 
	}
	
	public int getNx() {    
		int nx = 1;
		Point3D Q;
		Point3D P = new Point3D(getX(0), getY(0), getZ(0));
		double d1 = 0.0;
		double d2 = 0.0;
		
		for(int i=1; i<getNumOfPoints(); i++) {
			Q= new Point3D(getX(i), getY(i), getZ(i));
			d2 = P.distanceTo(Q);
			if(d2>d1) {
				nx++;
			} else {
				return nx;
			}
			d1 = d2;
		}
		return nx;
	}
	public int getNy() {
		return (int)(getNumOfPoints()/getNx());
	}
	
	public double getX0() {
		return getX(0);
	}
	public double getY0() {
		return getY(0);
	}
	public double getZ0() {
		return getZ(0);
	}
	public double getT0() {
		return getT(0);
	}
	public double getX1() {
		return getX(getNx()-1);
	}
	public double getY1() {
		return getY(getNumOfPoints()-1);
	}
	public double getDx() {
		return (getX1()-getX0())/(getNx()-1);
	}
	public double getDy() {
		return (getY1()-getY0())/(getNy()-1);
	}
	
	
	//inner class
	private class Point3D {
		private double _p3dx;    			
		private double _p3dy;			
		private double _p3dz;	

		public Point3D() {
			_p3dx = 0.0;
			_p3dy = 0.0;
			_p3dz = 0.0;
		}

		public Point3D(double x, double y, double z) {
			_p3dx = x;
			_p3dy = y;
			_p3dz = z;
		}

		public double getX() {
			return _p3dx;
		}
		public double getY() {
			return _p3dy;
		}
		public double getZ() {
			return _p3dz;
		}

		public String toString() {
			return String.format("%8.2f, %8.2f, %8.2f", _p3dx, _p3dy, _p3dz);
		}

		@Override 
		public boolean equals(Object other) {
			boolean result = false;
			if (other instanceof Point3D) {
				Point3D that = (Point3D) other;
				result = (that.canEqual(this) && 
						this.getX() == that.getX() && this.getY() == that.getY() && this.getZ() == that.getZ());
			}
			return result;
		}

		@Override 
		public int hashCode() {
			return (int)(41*(41 * (41 + getX()) + getY())+getZ());
		}

		public boolean canEqual(Object other) {
			return (other instanceof Point3D);
		}

		public double distanceTo(Point3D q) {
			double dx = _p3dx-q._p3dx;
			double dy = _p3dy-q._p3dy;
			double dz = _p3dz-q._p3dz;
			return Math.sqrt(dx*dx+dy*dy+dz*dz);
		}
	}
}
