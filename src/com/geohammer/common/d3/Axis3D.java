package com.geohammer.common.d3;

import java.nio.ByteBuffer;

import edu.mines.jtk.sgl.BoundingBox;
import edu.mines.jtk.sgl.Point3;
import edu.mines.jtk.util.AxisTics;

public class Axis3D {
	SegmentGroup _outAxis;
	SegmentGroup _outBox;
	
	public int 		_nx		= 2;
	public int 		_ny		= 2;
	public int 		_nz		= 2;
	public double 	_dx		= 1.0;
	public double 	_dy		= 1.0;
	public double 	_dz		= 1.0;
	public double 	_x0		= 0.0;
	public double 	_y0		= 0.0;
	public double 	_z0		= 0.0;
	public double 	_x1		= 1.0;
	public double 	_y1		= 1.0;
	public double 	_z1		= 1.0;
	
	public double _maxEdgeLen = -10000000.0;
	public double _tickLenPercentage = 0.02;
	public double _tickLen = 1.0;
	
	private int _maxNumOfMajorTicX = 10;
	private int _maxNumOfMajorTicY = 10;
	private int _maxNumOfMajorTicZ = 10;
	
	public Axis3D(BoundingBox boundingBox, int maxNumOfMajorTicX, int maxNumOfMajorTicY, int maxNumOfMajorTicZ) {
		Point3 p0 = boundingBox.getMin();
		Point3 p1 = boundingBox.getMax();
		setSize(_nx, _ny, _nz, p0.x, p0.y, p0.z, p1.x, p1.y, p1.z);
		double a = _x1-_x0;
		_maxEdgeLen = _maxEdgeLen>a ? _maxEdgeLen:a;
		a = _y1-_y0;
		_maxEdgeLen = _maxEdgeLen>a ? _maxEdgeLen:a;
		a = _z1-_z0;
		_maxEdgeLen = _maxEdgeLen>a ? _maxEdgeLen:a;
		_tickLen = _maxEdgeLen*_tickLenPercentage;
		
		_maxNumOfMajorTicX = maxNumOfMajorTicX;
		_maxNumOfMajorTicY = maxNumOfMajorTicY;
		_maxNumOfMajorTicZ = maxNumOfMajorTicZ;
	}
	public SegmentGroup calOutBox(){
		setSize();
		return new SegmentGroup(getXYZ());
	}
	
	public SegmentGroup calOutAxis(){
		setSize();
		return new SegmentGroup(getXYZAxis());
	}
	public SegmentGroup calMajorTick(){ return genTick(true);	}
	public SegmentGroup calMinorTick(){ return genTick(false); }
	public SegmentGroup calGrid(){
		float[] sectionXZ = genGridSection(1, _maxNumOfMajorTicX, _maxNumOfMajorTicZ);
		float[] sectionYZ = genGridSection(2, _maxNumOfMajorTicY, _maxNumOfMajorTicZ);
		
		float [] xyz = new float[sectionXZ.length+sectionYZ.length];
		int k = 0;
		for(int i=0; i<sectionXZ.length; i++) xyz[k++] = sectionXZ[i];
		for(int i=0; i<sectionYZ.length; i++) xyz[k++] = sectionYZ[i];
		
		return new SegmentGroup(xyz);
	}
	
	public SegmentGroup calGrid(String faceName){
		float[] xyz = null;
		if(faceName.equals("Grid Left")||faceName.equals("Grid Right")){
			xyz = genGridSection(1, _maxNumOfMajorTicX, _maxNumOfMajorTicZ);
			if(faceName.equals("Grid Right")) {
				float ay = (float)_y1;
				int n = xyz.length/3;
				for(int i=0; i<n; i++) xyz[3*i+1] = ay;
			}
		} else if(faceName.equals("Grid Back")||faceName.equals("Grid Front")){
			xyz = genGridSection(2, _maxNumOfMajorTicX, _maxNumOfMajorTicZ);
			if(faceName.equals("Grid Back")) {
				float ax = (float)_x0;
				int n = xyz.length/3;
				for(int i=0; i<n; i++) xyz[3*i+0] = ax;
			}
		} else if(faceName.equals("Grid Top")||faceName.equals("Grid Bottom")){
			xyz = genGridSection(3, _maxNumOfMajorTicX, _maxNumOfMajorTicZ);
			if(faceName.equals("Grid Bottom")) {
				float az = (float)_z1;
				int n = xyz.length/3;
				for(int i=0; i<n; i++) xyz[3*i+2] = az;
			}
		}
		return new SegmentGroup(xyz);
	}
	//String [] names = new String [] {"Grid Left", "Grid Right", "Grid Back", "Grid Front", "Grid Top", "Grid Bottom"};
	public float [] genGridSection(int iSection, int maxNumOfMajorTicI, int maxNumOfMajorTicJ){
		float [] lineI = null;
		float [] lineJ = null;
		
		if(iSection==1) { //xz
			lineI = genGridLine(1, 3, maxNumOfMajorTicI, false);
			lineJ = genGridLine(3, 1, maxNumOfMajorTicJ, false);
		} else if(iSection==2) { //yz
			lineI = genGridLine(2, 3, maxNumOfMajorTicI, false);
			lineJ = genGridLine(3, 2, maxNumOfMajorTicJ, false);
		} else if(iSection==3) { //xy
			lineI = genGridLine(1, 2, maxNumOfMajorTicI, false);
			lineJ = genGridLine(2, 1, maxNumOfMajorTicJ, false);
		}
		
		float [] xyz = new float[lineI.length+lineJ.length];
		
		int k = 0;
		for(int i=0; i<lineI.length; i++) xyz[k++] = lineI[i];
		for(int i=0; i<lineJ.length; i++) xyz[k++] = lineJ[i];
		return xyz;
	}
	public float [] genGridLine(int iAxis, int alongJAxis, int maxNumOfMajorTic, boolean opposite) {
		float[] xAxis = genTick(iAxis, maxNumOfMajorTic, true, false);

		float xTickLength = 0;
		float yTickLength = 0;
		float zTickLength = 0;
		float moveX = 0;
		if(iAxis==1) {
			if(alongJAxis==2) { //xy section
				yTickLength = (float)(_y1-_y0);
			} else if(alongJAxis==3) { //xz section
				zTickLength = (float)(_z1-_z0);
			}
		} else if(iAxis==2) {
			if(alongJAxis==1) { //yx section
				xTickLength = (float)(_x0-_x1);
			} else if(alongJAxis==3) { //yz section
				zTickLength = (float)(_z1-_z0);
			}
		} else if(iAxis==3) {
			if(alongJAxis==1) { //zx section
				xTickLength = (float)(_x1-_x0);
			} else if(alongJAxis==2) { //zy section
				yTickLength = (float)(_y1-_y0);
				moveX = (float)(_x1-_x0);
			}
		}
		
		float[] xAxisTip = new float[2*xAxis.length];
		int m = xAxis.length/3;
		
		int k = 0;
		for(int i=0, j=0; i<m; i++) {
			xAxisTip[k++] = xAxis[j++]+moveX;
			xAxisTip[k++] = xAxis[j++];
			xAxisTip[k++] = xAxis[j++];
			
			j = j-3;
			xAxisTip[k++] = xAxis[j++]+xTickLength+moveX;
			xAxisTip[k++] = xAxis[j++]+yTickLength;
			xAxisTip[k++] = xAxis[j++]+zTickLength;			
		}
		return xAxisTip;
	}
	
	public SegmentGroup genTick(boolean isMajorTic){
		float[] xAxis = genTick(1, _maxNumOfMajorTicX, isMajorTic, true);
		float[] yAxis = genTick(2, _maxNumOfMajorTicY, isMajorTic, true);
		float[] zAxis = genTick(3, _maxNumOfMajorTicZ, isMajorTic, true);
		float [] xyz = new float[xAxis.length+yAxis.length+zAxis.length];
		
		int k = 0;
		for(int i=0; i<xAxis.length; i++) xyz[k++] = xAxis[i];
		for(int i=0; i<yAxis.length; i++) xyz[k++] = yAxis[i];
		for(int i=0; i<zAxis.length; i++) xyz[k++] = zAxis[i];
		
		return new SegmentGroup(xyz);
	}
	
	//iAxis=1 for x axis iAxis=2 for y axis iAxis=3 for z axis
	public float [] genTick(int iAxis, int maxNumOfMajorTic, boolean isMajorTic, boolean append) {
		double ticMin = 0;
		double ticMax = 0;
		double [] tic = null;
		float [] xyz = null;
		double ticLen = 0;
		float ax=0, ay=0, az=0, bx=0, by=0, bz=0;
		
		if(iAxis==1) 		{ ticMin = _x0; ticMax = _x1; }
		else if(iAxis==2) 	{ ticMin = _y0; ticMax = _y1; }
		else if(iAxis==3) 	{ ticMin = _z0; ticMax = _z1; }
		tic = genTic(ticMin, ticMax, maxNumOfMajorTic, isMajorTic);
		
		if(isMajorTic) 		ticLen = _tickLen;
		else 				ticLen = 0.5*_tickLen;
		
		if(append) 			xyz = new float[2*3*(tic.length)];
		else 				xyz = new float[1*3*(tic.length)];
		
		ax = (float)_x0;  		ay = (float)_y0; 			az = (float)_z0;
		
		if(iAxis==2) ax = (float)_x1;

		if(iAxis==1) 		{ bx = ax; by = ay; 	bz = (float)(_z0-ticLen); }
		else if(iAxis==2) 	{ bx = ax; by = ay; 	bz = (float)(_z0-ticLen); }
		else if(iAxis==3) 	{ bx = (float)(_x0-ticLen); by = ay; 	bz = az; }

		int k = 0;
		for(int i=0; i<tic.length; i++) {
			if(iAxis==1) 		{ ax = (float)(tic[i]); bx = ax; }
			else if(iAxis==2) 	{ ay = (float)(tic[i]); by = ay; }
			else if(iAxis==3) 	{ az = (float)(tic[i]); bz = az; }

			xyz[k++] = ax;
			xyz[k++] = ay;
			xyz[k++] = az;
			if(append) {
				xyz[k++] = bx;
				xyz[k++] = by;
				xyz[k++] = bz;
			}
		}
		return xyz;
	}
	
	private double [] genTic(double ticMin, double ticMax, int maxNumOfMajorTic, boolean isMajorTic) {
		AxisTics axisTics = new AxisTics(ticMin, ticMax, maxNumOfMajorTic);
		
		// Axis tic sampling.
	    int nticMajor = axisTics.getCountMajor();
	    double dticMajor = axisTics.getDeltaMajor();
	    double fticMajor = axisTics.getFirstMajor();
	    int nticMinor = axisTics.getCountMinor();
	    double dticMinor = axisTics.getDeltaMinor();
	    double fticMinor = axisTics.getFirstMinor();
	    int mtic = axisTics.getMultiple();
	    
	    double [] tic = null;
	    if(isMajorTic) {
	    	tic = new double[nticMajor];
	    	for(int i=0; i<nticMajor; i++) tic[i] = fticMajor+i*dticMajor;
	    } else {
	    	tic = new double[nticMinor];
	    	for(int i=0; i<nticMinor; i++) tic[i] = fticMinor+i*dticMinor;
	    }
	    
	    return tic;
	}
	
	public float [] getXYZAxis() {
		float [] xyz = new float[3*12*2];
		int k = 0;
		//top surface
		xyz[k++] = (float)_x0; xyz[k++] = (float)_y0; xyz[k++] = (float)_z0;
		xyz[k++] = (float)_x1; xyz[k++] = (float)_y0; xyz[k++] = (float)_z0;
		xyz[k++] = (float)_x0; xyz[k++] = (float)_y0; xyz[k++] = (float)_z0;
		xyz[k++] = (float)_x0; xyz[k++] = (float)_y1; xyz[k++] = (float)_z0;
		xyz[k++] = (float)_x1; xyz[k++] = (float)_y1; xyz[k++] = (float)_z0;
		xyz[k++] = (float)_x0; xyz[k++] = (float)_y1; xyz[k++] = (float)_z0;
		xyz[k++] = (float)_x1; xyz[k++] = (float)_y1; xyz[k++] = (float)_z0;
		xyz[k++] = (float)_x1; xyz[k++] = (float)_y0; xyz[k++] = (float)_z0;
		//bottom surface
		xyz[k++] = (float)_x0; xyz[k++] = (float)_y0; xyz[k++] = (float)_z1;
		xyz[k++] = (float)_x1; xyz[k++] = (float)_y0; xyz[k++] = (float)_z1;
		xyz[k++] = (float)_x0; xyz[k++] = (float)_y0; xyz[k++] = (float)_z1;
		xyz[k++] = (float)_x0; xyz[k++] = (float)_y1; xyz[k++] = (float)_z1;
		xyz[k++] = (float)_x1; xyz[k++] = (float)_y1; xyz[k++] = (float)_z1;
		xyz[k++] = (float)_x0; xyz[k++] = (float)_y1; xyz[k++] = (float)_z1;
		xyz[k++] = (float)_x1; xyz[k++] = (float)_y1; xyz[k++] = (float)_z1;
		xyz[k++] = (float)_x1; xyz[k++] = (float)_y0; xyz[k++] = (float)_z1;
				
		//column surface
		xyz[k++] = (float)_x0; xyz[k++] = (float)_y0; xyz[k++] = (float)_z0;
		xyz[k++] = (float)_x0; xyz[k++] = (float)_y0; xyz[k++] = (float)_z1;
		xyz[k++] = (float)_x0; xyz[k++] = (float)_y1; xyz[k++] = (float)_z0;
		xyz[k++] = (float)_x0; xyz[k++] = (float)_y1; xyz[k++] = (float)_z1;
		xyz[k++] = (float)_x1; xyz[k++] = (float)_y0; xyz[k++] = (float)_z0;
		xyz[k++] = (float)_x1; xyz[k++] = (float)_y0; xyz[k++] = (float)_z1;
		xyz[k++] = (float)_x1; xyz[k++] = (float)_y1; xyz[k++] = (float)_z0;
		xyz[k++] = (float)_x1; xyz[k++] = (float)_y1; xyz[k++] = (float)_z1;
		
		return xyz;
	}
	
	

	public TextGroup calTickLabel(int iAxis, int maxNumOfMajorTic){
		float[] xAxis = genTick(iAxis, maxNumOfMajorTic, true, true);
		return new TextGroup(iAxis, _buf, xAxis);
	}
	
	public SegmentGroup getOutBox(){ return _outBox; }
	public SegmentGroup getOutAxis(){ return _outAxis; }

	public TextGroup calTickLabel(int iAxis){
		int maxNumOfMajorTic = _maxNumOfMajorTicX;
		if(iAxis==1) 		maxNumOfMajorTic = _maxNumOfMajorTicX;
		else if(iAxis==2) 	maxNumOfMajorTic = _maxNumOfMajorTicY;
		else if(iAxis==3) 	maxNumOfMajorTic = _maxNumOfMajorTicZ;
		
		float[] xAxis = genTick(iAxis, maxNumOfMajorTic, true, true);
		return new TextGroup(iAxis, _buf, xAxis);
	}
	
	public TextGroup calAxisLabel(int iAxis, String label){
		int maxNumOfMajorTic = _maxNumOfMajorTicX;
		if(iAxis==1) 		maxNumOfMajorTic = _maxNumOfMajorTicX;
		else if(iAxis==2) 	maxNumOfMajorTic = _maxNumOfMajorTicY;
		else if(iAxis==3) 	maxNumOfMajorTic = _maxNumOfMajorTicZ;
		float[] xAxis = genTick(iAxis, maxNumOfMajorTic, true, true);		
		int N = xAxis.length;
		
		float [] tipXyz = new float[] { xAxis[3], xAxis[4], xAxis[5], xAxis[N-3], xAxis[N-2], xAxis[N-1]};
		
		float [] labelXyz = new float[3];
		for(int i=0; i<3; i++) {
			labelXyz[i] = 0.5f*(tipXyz[i]+tipXyz[i+3]);
		}
		
		if(iAxis==1) 		labelXyz[2] -= _tickLen;
		else if(iAxis==2) 	labelXyz[2] -= _tickLen;
		else if(iAxis==3) 	labelXyz[0] -= _tickLen;
		
		return new TextGroup(_buf, labelXyz, new String [] {label});
	}
	
	public float [] getXYZ() {
		return getXYZ(4);
	}
	public float [] getXYZ(int iAxis) {
		return getXYZ(iAxis, _nx, _ny, _nz);
	}
	public float [] getXYZ(int iAxis, double shiftX, double shiftY, double shiftZ) {
		return getXYZ(iAxis, _nx, _ny, _nz, shiftX, shiftY, shiftZ);
	}
	public float [] getXYZ(int iAxis, int nx, int ny, int nz) {
		return getXYZ(iAxis, nx, ny, nz, 0, 0, 0);
	}
	public float [] getXYZ(int iAxis, int nx, int ny, int nz, double shiftX, double shiftY, double shiftZ) {
		setSize(nx, ny, nz);
		float[] xyz = null;
		if(iAxis==4) {
			xyz = new float[3*3*nx*ny*nz];
		} else {
			xyz = new float[3*nx*ny*nz];
		}
		int k = 0;
		float x, y, z;
		if(iAxis==1||iAxis==4) { //parallel to x axis
			for(int iz=0; iz<_nz; iz++) {
				z = (float)(_z0+iz*_dz+shiftZ);
				for(int iy=0; iy<_ny; iy++) {
					y = (float)(_y0+iy*_dy+shiftY);
					for(int ix=0; ix<_nx; ix++) {
						x = (float)(_x0+ix*_dx+shiftX);
						xyz[k++] = x;
						xyz[k++] = y;
						xyz[k++] = z;
					}
				}
			}
		}
		if(iAxis==2||iAxis==4) { //parallel to y axis
			for(int iz=0; iz<_nz; iz++) {
				z = (float)(_z0+iz*_dz+shiftZ);
				for(int ix=0; ix<_nx; ix++) {
					x = (float)(_x0+ix*_dx+shiftX);
					for(int iy=0; iy<_ny; iy++) {
						y = (float)(_y0+iy*_dy+shiftY);
						xyz[k++] = x;
						xyz[k++] = y;
						xyz[k++] = z;
					}
				}
			}
		}
		if(iAxis==3||iAxis==4) { //parallel to z axis
			for(int iy=0; iy<_ny; iy++) {
				y = (float)(_y0+iy*_dy+shiftY);
				for(int ix=0; ix<_nx; ix++) {
					x = (float)(_x0+ix*_dx+shiftX);		
					for(int iz=0; iz<_nz; iz++) {
						z = (float)(_z0+iz*_dz+shiftZ);
						xyz[k++] = x;
						xyz[k++] = y;
						xyz[k++] = z;
					}
				}
			}
		}
		return xyz;
	}
	
	public float [] genFace(String faceName){
		int nx = 2;
		int ny = 2;
		int nz = 2;
		double len = 1.0;
		int nzy = nz;
		setSize(nx, ny, nz);
		float[] xAxis = getXYZ(1);
		float[] yAxis = getXYZ(2);
		float[] zAxis = getXYZ(3);
		double xTickLength = len*(_x1-_x0);
		double yTickLength = len*(_y1-_y0);
		double zTickLength = len*(_z1-_z0);
		
		float[] xAxisTip = getXYZ(1, 0, 0, zTickLength);
		float[] yAxisTip = getXYZ(2, 0, 0, zTickLength);
		float[] zAxisTip = getXYZ(3, xTickLength, 0, 0);
		float[] zyAxisTip = getXYZ(3, 0, yTickLength, 0);

		if(faceName.equals("Face Left")||faceName.equals("Face Right")){
			ny = 0;
			nzy = 0;
		} else if(faceName.equals("Face Back")||faceName.equals("Face Front")){
			nx = 0;
			nz = 0;
		} else if(faceName.equals("Face Top")||faceName.equals("Face Bottom")){
			nz = 0;
			nzy = 0;
			xAxisTip = getXYZ(1, 0, yTickLength, 0);
			yAxisTip = getXYZ(2, xTickLength, 0, 0);
		}
		float [] xyz = new float[2*3*(nx+ny+nz+nzy)];
		int mx = 3*nx;
		int my = 3*ny;
		int mz = 3*nz;
		int mzy = 3*nzy;
		
		int k = 0;
		for(int i=0; i<mx; i++) {
			int j = i;
			xyz[k++] = xAxis[i++];
			xyz[k++] = xAxis[i++];
			xyz[k++] = xAxis[i];
			xyz[k++] = xAxisTip[j++];
			xyz[k++] = xAxisTip[j++];
			xyz[k++] = xAxisTip[j];
		}
		for(int i=0; i<my; i++) {
			int j = i;
			xyz[k++] = yAxis[i++];
			xyz[k++] = yAxis[i++];
			xyz[k++] = yAxis[i];
			xyz[k++] = yAxisTip[j++];
			xyz[k++] = yAxisTip[j++];
			xyz[k++] = yAxisTip[j];
		}
		for(int i=0; i<mz; i++) {
			int j = i;
			xyz[k++] = zAxis[i++];
			xyz[k++] = zAxis[i++];
			xyz[k++] = zAxis[i];
			xyz[k++] = zAxisTip[j++];
			xyz[k++] = zAxisTip[j++];
			xyz[k++] = zAxisTip[j];
		}
		for(int i=0; i<mzy; i++) {
			int j = i;
			xyz[k++] = zAxis[i++];
			xyz[k++] = zAxis[i++];
			xyz[k++] = zAxis[i];
			xyz[k++] = zyAxisTip[j++];
			xyz[k++] = zyAxisTip[j++];
			xyz[k++] = zyAxisTip[j];
		}
		
		if(faceName.equals("Face Right")){
			float shift = (float)(_y1-_y0);
			for(int i=0; i<xyz.length;i++) {
				if(i%3==1) xyz[i] += shift;
			}
		} else if(faceName.equals("Face Front")){
			float shift = (float)(_x1-_x0);
			for(int i=0; i<xyz.length;i++) {
				if(i%3==0) xyz[i] += shift;
			}
		} else if(faceName.equals("Face Bottom")){
			float shift = (float)(_z1-_z0);
			for(int i=0; i<xyz.length;i++) {
				if(i%3==2) xyz[i] += shift;
			}
		}
		return xyz;
	}
	
	
	
	
	public void setSize() {
		setSize(2, 2, 2);
	}
	public void setSize(int nx, int ny, int nz) {
		setSize(nx, ny, nz, _x0, _y0, _z0, _x1, _y1, _z1);
	}
	public void setSize(int nx, int ny, int nz,
			double x0, double y0, double z0, double x1, double y1, double z1) {
		_nx = nx;
		_ny = ny;
		_nz = nz;

		_x0 = x0;
		_y0 = y0;
		_z0 = z0;

		_x1 = x1;
		_y1 = y1;
		_z1 = z1;

		_dx = (_x1-_x0)/(_nx-1);
		_dy = (_y1-_y0)/(_ny-1);
		_dz = (_z1-_z0)/(_nz-1);
	}
	
	public String toString(int id) {
		return "nx="+_nx+" ny="+_ny+" nz="+_nz+" x0="+_x0+" y0="+_y0+" z0="+_z0+
				" x1="+_x1+" y1="+_y1+" z1="+_z1+" dx="+_dx+" dy="+_dy+" dz="+_dz;
	}

	public static byte ASCII [][] = {
			{(byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00},
			{(byte)0x00, (byte)0x00, (byte)0x18, (byte)0x18, (byte)0x00, (byte)0x00, (byte)0x18, (byte)0x18, (byte)0x18, (byte)0x18, (byte)0x18, (byte)0x18, (byte)0x18}, 
			{(byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x36, (byte)0x36, (byte)0x36, (byte)0x36}, 
			{(byte)0x00, (byte)0x00, (byte)0x00, (byte)0x66, (byte)0x66, (byte)0xff, (byte)0x66, (byte)0x66, (byte)0xff, (byte)0x66, (byte)0x66, (byte)0x00, (byte)0x00}, 
			{(byte)0x00, (byte)0x00, (byte)0x18, (byte)0x7e, (byte)0xff, (byte)0x1b, (byte)0x1f, (byte)0x7e, (byte)0xf8, (byte)0xd8, (byte)0xff, (byte)0x7e, (byte)0x18}, 
			{(byte)0x00, (byte)0x00, (byte)0x0e, (byte)0x1b, (byte)0xdb, (byte)0x6e, (byte)0x30, (byte)0x18, (byte)0x0c, (byte)0x76, (byte)0xdb, (byte)0xd8, (byte)0x70}, 
			{(byte)0x00, (byte)0x00, (byte)0x7f, (byte)0xc6, (byte)0xcf, (byte)0xd8, (byte)0x70, (byte)0x70, (byte)0xd8, (byte)0xcc, (byte)0xcc, (byte)0x6c, (byte)0x38}, 
			{(byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x18, (byte)0x1c, (byte)0x0c, (byte)0x0e}, 
			{(byte)0x00, (byte)0x00, (byte)0x0c, (byte)0x18, (byte)0x30, (byte)0x30, (byte)0x30, (byte)0x30, (byte)0x30, (byte)0x30, (byte)0x30, (byte)0x18, (byte)0x0c}, 
			{(byte)0x00, (byte)0x00, (byte)0x30, (byte)0x18, (byte)0x0c, (byte)0x0c, (byte)0x0c, (byte)0x0c, (byte)0x0c, (byte)0x0c, (byte)0x0c, (byte)0x18, (byte)0x30}, 
			{(byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x99, (byte)0x5a, (byte)0x3c, (byte)0xff, (byte)0x3c, (byte)0x5a, (byte)0x99, (byte)0x00, (byte)0x00}, 
			{(byte)0x00, (byte)0x00, (byte)0x00, (byte)0x18, (byte)0x18, (byte)0x18, (byte)0xff, (byte)0xff, (byte)0x18, (byte)0x18, (byte)0x18, (byte)0x00, (byte)0x00}, 
			{(byte)0x00, (byte)0x00, (byte)0x30, (byte)0x18, (byte)0x1c, (byte)0x1c, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00}, 
			{(byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0xff, (byte)0xff, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00}, 
			{(byte)0x00, (byte)0x00, (byte)0x00, (byte)0x38, (byte)0x38, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00}, 
			{(byte)0x00, (byte)0x60, (byte)0x60, (byte)0x30, (byte)0x30, (byte)0x18, (byte)0x18, (byte)0x0c, (byte)0x0c, (byte)0x06, (byte)0x06, (byte)0x03, (byte)0x03}, 
			{(byte)0x00, (byte)0x00, (byte)0x3c, (byte)0x66, (byte)0xc3, (byte)0xe3, (byte)0xf3, (byte)0xdb, (byte)0xcf, (byte)0xc7, (byte)0xc3, (byte)0x66, (byte)0x3c}, 
			{(byte)0x00, (byte)0x00, (byte)0x7e, (byte)0x18, (byte)0x18, (byte)0x18, (byte)0x18, (byte)0x18, (byte)0x18, (byte)0x18, (byte)0x78, (byte)0x38, (byte)0x18}, 
			{(byte)0x00, (byte)0x00, (byte)0xff, (byte)0xc0, (byte)0xc0, (byte)0x60, (byte)0x30, (byte)0x18, (byte)0x0c, (byte)0x06, (byte)0x03, (byte)0xe7, (byte)0x7e}, 
			{(byte)0x00, (byte)0x00, (byte)0x7e, (byte)0xe7, (byte)0x03, (byte)0x03, (byte)0x07, (byte)0x7e, (byte)0x07, (byte)0x03, (byte)0x03, (byte)0xe7, (byte)0x7e}, 
			{(byte)0x00, (byte)0x00, (byte)0x0c, (byte)0x0c, (byte)0x0c, (byte)0x0c, (byte)0x0c, (byte)0xff, (byte)0xcc, (byte)0x6c, (byte)0x3c, (byte)0x1c, (byte)0x0c}, 
			{(byte)0x00, (byte)0x00, (byte)0x7e, (byte)0xe7, (byte)0x03, (byte)0x03, (byte)0x07, (byte)0xfe, (byte)0xc0, (byte)0xc0, (byte)0xc0, (byte)0xc0, (byte)0xff}, 
			{(byte)0x00, (byte)0x00, (byte)0x7e, (byte)0xe7, (byte)0xc3, (byte)0xc3, (byte)0xc7, (byte)0xfe, (byte)0xc0, (byte)0xc0, (byte)0xc0, (byte)0xe7, (byte)0x7e}, 
			{(byte)0x00, (byte)0x00, (byte)0x30, (byte)0x30, (byte)0x30, (byte)0x30, (byte)0x18, (byte)0x0c, (byte)0x06, (byte)0x03, (byte)0x03, (byte)0x03, (byte)0xff}, 
			{(byte)0x00, (byte)0x00, (byte)0x7e, (byte)0xe7, (byte)0xc3, (byte)0xc3, (byte)0xe7, (byte)0x7e, (byte)0xe7, (byte)0xc3, (byte)0xc3, (byte)0xe7, (byte)0x7e}, 
			{(byte)0x00, (byte)0x00, (byte)0x7e, (byte)0xe7, (byte)0x03, (byte)0x03, (byte)0x03, (byte)0x7f, (byte)0xe7, (byte)0xc3, (byte)0xc3, (byte)0xe7, (byte)0x7e}, 
			{(byte)0x00, (byte)0x00, (byte)0x00, (byte)0x38, (byte)0x38, (byte)0x00, (byte)0x00, (byte)0x38, (byte)0x38, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00}, 
			{(byte)0x00, (byte)0x00, (byte)0x30, (byte)0x18, (byte)0x1c, (byte)0x1c, (byte)0x00, (byte)0x00, (byte)0x1c, (byte)0x1c, (byte)0x00, (byte)0x00, (byte)0x00}, 
			{(byte)0x00, (byte)0x00, (byte)0x06, (byte)0x0c, (byte)0x18, (byte)0x30, (byte)0x60, (byte)0xc0, (byte)0x60, (byte)0x30, (byte)0x18, (byte)0x0c, (byte)0x06}, 
			{(byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0xff, (byte)0xff, (byte)0x00, (byte)0xff, (byte)0xff, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00}, 
			{(byte)0x00, (byte)0x00, (byte)0x60, (byte)0x30, (byte)0x18, (byte)0x0c, (byte)0x06, (byte)0x03, (byte)0x06, (byte)0x0c, (byte)0x18, (byte)0x30, (byte)0x60}, 
			{(byte)0x00, (byte)0x00, (byte)0x18, (byte)0x00, (byte)0x00, (byte)0x18, (byte)0x18, (byte)0x0c, (byte)0x06, (byte)0x03, (byte)0xc3, (byte)0xc3, (byte)0x7e}, 
			{(byte)0x00, (byte)0x00, (byte)0x3f, (byte)0x60, (byte)0xcf, (byte)0xdb, (byte)0xd3, (byte)0xdd, (byte)0xc3, (byte)0x7e, (byte)0x00, (byte)0x00, (byte)0x00}, 
			{(byte)0x00, (byte)0x00, (byte)0xc3, (byte)0xc3, (byte)0xc3, (byte)0xc3, (byte)0xff, (byte)0xc3, (byte)0xc3, (byte)0xc3, (byte)0x66, (byte)0x3c, (byte)0x18}, 
			{(byte)0x00, (byte)0x00, (byte)0xfe, (byte)0xc7, (byte)0xc3, (byte)0xc3, (byte)0xc7, (byte)0xfe, (byte)0xc7, (byte)0xc3, (byte)0xc3, (byte)0xc7, (byte)0xfe}, 
			{(byte)0x00, (byte)0x00, (byte)0x7e, (byte)0xe7, (byte)0xc0, (byte)0xc0, (byte)0xc0, (byte)0xc0, (byte)0xc0, (byte)0xc0, (byte)0xc0, (byte)0xe7, (byte)0x7e}, 
			{(byte)0x00, (byte)0x00, (byte)0xfc, (byte)0xce, (byte)0xc7, (byte)0xc3, (byte)0xc3, (byte)0xc3, (byte)0xc3, (byte)0xc3, (byte)0xc7, (byte)0xce, (byte)0xfc}, 
			{(byte)0x00, (byte)0x00, (byte)0xff, (byte)0xc0, (byte)0xc0, (byte)0xc0, (byte)0xc0, (byte)0xfc, (byte)0xc0, (byte)0xc0, (byte)0xc0, (byte)0xc0, (byte)0xff}, 
			{(byte)0x00, (byte)0x00, (byte)0xc0, (byte)0xc0, (byte)0xc0, (byte)0xc0, (byte)0xc0, (byte)0xc0, (byte)0xfc, (byte)0xc0, (byte)0xc0, (byte)0xc0, (byte)0xff}, 
			{(byte)0x00, (byte)0x00, (byte)0x7e, (byte)0xe7, (byte)0xc3, (byte)0xc3, (byte)0xcf, (byte)0xc0, (byte)0xc0, (byte)0xc0, (byte)0xc0, (byte)0xe7, (byte)0x7e}, 
			{(byte)0x00, (byte)0x00, (byte)0xc3, (byte)0xc3, (byte)0xc3, (byte)0xc3, (byte)0xc3, (byte)0xff, (byte)0xc3, (byte)0xc3, (byte)0xc3, (byte)0xc3, (byte)0xc3}, 
			{(byte)0x00, (byte)0x00, (byte)0x7e, (byte)0x18, (byte)0x18, (byte)0x18, (byte)0x18, (byte)0x18, (byte)0x18, (byte)0x18, (byte)0x18, (byte)0x18, (byte)0x7e}, 
			{(byte)0x00, (byte)0x00, (byte)0x7c, (byte)0xee, (byte)0xc6, (byte)0x06, (byte)0x06, (byte)0x06, (byte)0x06, (byte)0x06, (byte)0x06, (byte)0x06, (byte)0x06}, 
			{(byte)0x00, (byte)0x00, (byte)0xc3, (byte)0xc6, (byte)0xcc, (byte)0xd8, (byte)0xf0, (byte)0xe0, (byte)0xf0, (byte)0xd8, (byte)0xcc, (byte)0xc6, (byte)0xc3}, 
			{(byte)0x00, (byte)0x00, (byte)0xff, (byte)0xc0, (byte)0xc0, (byte)0xc0, (byte)0xc0, (byte)0xc0, (byte)0xc0, (byte)0xc0, (byte)0xc0, (byte)0xc0, (byte)0xc0}, 
			{(byte)0x00, (byte)0x00, (byte)0xc3, (byte)0xc3, (byte)0xc3, (byte)0xc3, (byte)0xc3, (byte)0xc3, (byte)0xdb, (byte)0xff, (byte)0xff, (byte)0xe7, (byte)0xc3}, 
			{(byte)0x00, (byte)0x00, (byte)0xc7, (byte)0xc7, (byte)0xcf, (byte)0xcf, (byte)0xdf, (byte)0xdb, (byte)0xfb, (byte)0xf3, (byte)0xf3, (byte)0xe3, (byte)0xe3}, 
			{(byte)0x00, (byte)0x00, (byte)0x7e, (byte)0xe7, (byte)0xc3, (byte)0xc3, (byte)0xc3, (byte)0xc3, (byte)0xc3, (byte)0xc3, (byte)0xc3, (byte)0xe7, (byte)0x7e}, 
			{(byte)0x00, (byte)0x00, (byte)0xc0, (byte)0xc0, (byte)0xc0, (byte)0xc0, (byte)0xc0, (byte)0xfe, (byte)0xc7, (byte)0xc3, (byte)0xc3, (byte)0xc7, (byte)0xfe}, 
			{(byte)0x00, (byte)0x00, (byte)0x3f, (byte)0x6e, (byte)0xdf, (byte)0xdb, (byte)0xc3, (byte)0xc3, (byte)0xc3, (byte)0xc3, (byte)0xc3, (byte)0x66, (byte)0x3c}, 
			{(byte)0x00, (byte)0x00, (byte)0xc3, (byte)0xc6, (byte)0xcc, (byte)0xd8, (byte)0xf0, (byte)0xfe, (byte)0xc7, (byte)0xc3, (byte)0xc3, (byte)0xc7, (byte)0xfe}, 
			{(byte)0x00, (byte)0x00, (byte)0x7e, (byte)0xe7, (byte)0x03, (byte)0x03, (byte)0x07, (byte)0x7e, (byte)0xe0, (byte)0xc0, (byte)0xc0, (byte)0xe7, (byte)0x7e}, 
			{(byte)0x00, (byte)0x00, (byte)0x18, (byte)0x18, (byte)0x18, (byte)0x18, (byte)0x18, (byte)0x18, (byte)0x18, (byte)0x18, (byte)0x18, (byte)0x18, (byte)0xff}, 
			{(byte)0x00, (byte)0x00, (byte)0x7e, (byte)0xe7, (byte)0xc3, (byte)0xc3, (byte)0xc3, (byte)0xc3, (byte)0xc3, (byte)0xc3, (byte)0xc3, (byte)0xc3, (byte)0xc3}, 
			{(byte)0x00, (byte)0x00, (byte)0x18, (byte)0x3c, (byte)0x3c, (byte)0x66, (byte)0x66, (byte)0xc3, (byte)0xc3, (byte)0xc3, (byte)0xc3, (byte)0xc3, (byte)0xc3}, 
			{(byte)0x00, (byte)0x00, (byte)0xc3, (byte)0xe7, (byte)0xff, (byte)0xff, (byte)0xdb, (byte)0xdb, (byte)0xc3, (byte)0xc3, (byte)0xc3, (byte)0xc3, (byte)0xc3}, 
			{(byte)0x00, (byte)0x00, (byte)0xc3, (byte)0x66, (byte)0x66, (byte)0x3c, (byte)0x3c, (byte)0x18, (byte)0x3c, (byte)0x3c, (byte)0x66, (byte)0x66, (byte)0xc3}, 
			{(byte)0x00, (byte)0x00, (byte)0x18, (byte)0x18, (byte)0x18, (byte)0x18, (byte)0x18, (byte)0x18, (byte)0x3c, (byte)0x3c, (byte)0x66, (byte)0x66, (byte)0xc3}, 
			{(byte)0x00, (byte)0x00, (byte)0xff, (byte)0xc0, (byte)0xc0, (byte)0x60, (byte)0x30, (byte)0x7e, (byte)0x0c, (byte)0x06, (byte)0x03, (byte)0x03, (byte)0xff}, 
			{(byte)0x00, (byte)0x00, (byte)0x3c, (byte)0x30, (byte)0x30, (byte)0x30, (byte)0x30, (byte)0x30, (byte)0x30, (byte)0x30, (byte)0x30, (byte)0x30, (byte)0x3c}, 
			{(byte)0x00, (byte)0x03, (byte)0x03, (byte)0x06, (byte)0x06, (byte)0x0c, (byte)0x0c, (byte)0x18, (byte)0x18, (byte)0x30, (byte)0x30, (byte)0x60, (byte)0x60}, 
			{(byte)0x00, (byte)0x00, (byte)0x3c, (byte)0x0c, (byte)0x0c, (byte)0x0c, (byte)0x0c, (byte)0x0c, (byte)0x0c, (byte)0x0c, (byte)0x0c, (byte)0x0c, (byte)0x3c}, 
			{(byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0xc3, (byte)0x66, (byte)0x3c, (byte)0x18}, 
			{(byte)0xff, (byte)0xff, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00}, 
			{(byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x18, (byte)0x38, (byte)0x30, (byte)0x70}, 
			{(byte)0x00, (byte)0x00, (byte)0x7f, (byte)0xc3, (byte)0xc3, (byte)0x7f, (byte)0x03, (byte)0xc3, (byte)0x7e, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00}, 
			{(byte)0x00, (byte)0x00, (byte)0xfe, (byte)0xc3, (byte)0xc3, (byte)0xc3, (byte)0xc3, (byte)0xfe, (byte)0xc0, (byte)0xc0, (byte)0xc0, (byte)0xc0, (byte)0xc0}, 
			{(byte)0x00, (byte)0x00, (byte)0x7e, (byte)0xc3, (byte)0xc0, (byte)0xc0, (byte)0xc0, (byte)0xc3, (byte)0x7e, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00}, 
			{(byte)0x00, (byte)0x00, (byte)0x7f, (byte)0xc3, (byte)0xc3, (byte)0xc3, (byte)0xc3, (byte)0x7f, (byte)0x03, (byte)0x03, (byte)0x03, (byte)0x03, (byte)0x03}, 
			{(byte)0x00, (byte)0x00, (byte)0x7f, (byte)0xc0, (byte)0xc0, (byte)0xfe, (byte)0xc3, (byte)0xc3, (byte)0x7e, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00}, 
			{(byte)0x00, (byte)0x00, (byte)0x30, (byte)0x30, (byte)0x30, (byte)0x30, (byte)0x30, (byte)0xfc, (byte)0x30, (byte)0x30, (byte)0x30, (byte)0x33, (byte)0x1e}, 
			{(byte)0x7e, (byte)0xc3, (byte)0x03, (byte)0x03, (byte)0x7f, (byte)0xc3, (byte)0xc3, (byte)0xc3, (byte)0x7e, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00}, 
			{(byte)0x00, (byte)0x00, (byte)0xc3, (byte)0xc3, (byte)0xc3, (byte)0xc3, (byte)0xc3, (byte)0xc3, (byte)0xfe, (byte)0xc0, (byte)0xc0, (byte)0xc0, (byte)0xc0}, 
			{(byte)0x00, (byte)0x00, (byte)0x18, (byte)0x18, (byte)0x18, (byte)0x18, (byte)0x18, (byte)0x18, (byte)0x18, (byte)0x00, (byte)0x00, (byte)0x18, (byte)0x00}, 
			{(byte)0x38, (byte)0x6c, (byte)0x0c, (byte)0x0c, (byte)0x0c, (byte)0x0c, (byte)0x0c, (byte)0x0c, (byte)0x0c, (byte)0x00, (byte)0x00, (byte)0x0c, (byte)0x00}, 
			{(byte)0x00, (byte)0x00, (byte)0xc6, (byte)0xcc, (byte)0xf8, (byte)0xf0, (byte)0xd8, (byte)0xcc, (byte)0xc6, (byte)0xc0, (byte)0xc0, (byte)0xc0, (byte)0xc0}, 
			{(byte)0x00, (byte)0x00, (byte)0x7e, (byte)0x18, (byte)0x18, (byte)0x18, (byte)0x18, (byte)0x18, (byte)0x18, (byte)0x18, (byte)0x18, (byte)0x18, (byte)0x78}, 
			{(byte)0x00, (byte)0x00, (byte)0xdb, (byte)0xdb, (byte)0xdb, (byte)0xdb, (byte)0xdb, (byte)0xdb, (byte)0xfe, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00}, 
			{(byte)0x00, (byte)0x00, (byte)0xc6, (byte)0xc6, (byte)0xc6, (byte)0xc6, (byte)0xc6, (byte)0xc6, (byte)0xfc, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00}, 
			{(byte)0x00, (byte)0x00, (byte)0x7c, (byte)0xc6, (byte)0xc6, (byte)0xc6, (byte)0xc6, (byte)0xc6, (byte)0x7c, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00}, 
			{(byte)0xc0, (byte)0xc0, (byte)0xc0, (byte)0xfe, (byte)0xc3, (byte)0xc3, (byte)0xc3, (byte)0xc3, (byte)0xfe, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00}, 
			{(byte)0x03, (byte)0x03, (byte)0x03, (byte)0x7f, (byte)0xc3, (byte)0xc3, (byte)0xc3, (byte)0xc3, (byte)0x7f, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00}, 
			{(byte)0x00, (byte)0x00, (byte)0xc0, (byte)0xc0, (byte)0xc0, (byte)0xc0, (byte)0xc0, (byte)0xe0, (byte)0xfe, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00}, 
			{(byte)0x00, (byte)0x00, (byte)0xfe, (byte)0x03, (byte)0x03, (byte)0x7e, (byte)0xc0, (byte)0xc0, (byte)0x7f, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00}, 
			{(byte)0x00, (byte)0x00, (byte)0x1c, (byte)0x36, (byte)0x30, (byte)0x30, (byte)0x30, (byte)0x30, (byte)0xfc, (byte)0x30, (byte)0x30, (byte)0x30, (byte)0x00}, 
			{(byte)0x00, (byte)0x00, (byte)0x7e, (byte)0xc6, (byte)0xc6, (byte)0xc6, (byte)0xc6, (byte)0xc6, (byte)0xc6, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00}, 
			{(byte)0x00, (byte)0x00, (byte)0x18, (byte)0x3c, (byte)0x3c, (byte)0x66, (byte)0x66, (byte)0xc3, (byte)0xc3, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00}, 
			{(byte)0x00, (byte)0x00, (byte)0xc3, (byte)0xe7, (byte)0xff, (byte)0xdb, (byte)0xc3, (byte)0xc3, (byte)0xc3, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00}, 
			{(byte)0x00, (byte)0x00, (byte)0xc3, (byte)0x66, (byte)0x3c, (byte)0x18, (byte)0x3c, (byte)0x66, (byte)0xc3, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00}, 
			{(byte)0xc0, (byte)0x60, (byte)0x60, (byte)0x30, (byte)0x18, (byte)0x3c, (byte)0x66, (byte)0x66, (byte)0xc3, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00}, 
			{(byte)0x00, (byte)0x00, (byte)0xff, (byte)0x60, (byte)0x30, (byte)0x18, (byte)0x0c, (byte)0x06, (byte)0xff, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00}, 
			{(byte)0x00, (byte)0x00, (byte)0x0f, (byte)0x18, (byte)0x18, (byte)0x18, (byte)0x38, (byte)0xf0, (byte)0x38, (byte)0x18, (byte)0x18, (byte)0x18, (byte)0x0f}, 
			{(byte)0x18, (byte)0x18, (byte)0x18, (byte)0x18, (byte)0x18, (byte)0x18, (byte)0x18, (byte)0x18, (byte)0x18, (byte)0x18, (byte)0x18, (byte)0x18, (byte)0x18}, 
			{(byte)0x00, (byte)0x00, (byte)0xf0, (byte)0x18, (byte)0x18, (byte)0x18, (byte)0x1c, (byte)0x0f, (byte)0x1c, (byte)0x18, (byte)0x18, (byte)0x18, (byte)0xf0},
			{(byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x06, (byte)0x8f, (byte)0xf1, (byte)0x60, (byte)0x00, (byte)0x00, (byte)0x00},
	};
	
	public static ByteBuffer _buf[] = new ByteBuffer[Axis3D.ASCII.length];
	static {
		for (int i = 0; i < ASCII.length; i++) {
			_buf[i] = ByteBuffer.allocateDirect( Axis3D.ASCII[i].length );
			_buf[i].put( ASCII[i] );
			_buf[i].rewind();
		};
	}
	
}
