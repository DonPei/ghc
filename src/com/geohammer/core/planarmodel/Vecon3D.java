package com.geohammer.core.planarmodel;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.ByteOrder;

import javax.swing.JOptionPane;

import edu.mines.jtk.dsp.Sampling;
import edu.mines.jtk.io.ArrayFile;
import edu.mines.jtk.util.ArrayMath;

public class Vecon3D  extends HalfSpaceBox {
	protected float [][][] 	_data 	= null; 	// _data[ix][iy][iz] = [northing][Easting][Down]
	public int 	_iUnit 				= 2;		// =1 m; 2 ft; 3 km
	
	//secondary variable
	private double 		_t0 		= 0.0;
	private Sampling 	_x 			= null;
	private float [] 	_locX 		= null;
	private float [] 	_locY 		= null;
	private float [] 	_locZ 		= null;
	
	public Vecon3D() { }
	public Vecon3D(String selectedFileName)	{
		read(selectedFileName );
		//System.out.println(toString());
	}
	public Vecon3D(int iUnit, double t0, float [] locX, float [] locY, float [] locZ) { 
		this(iUnit, 0, 
				locX.length, locY.length, locZ.length, 
				(locX.length==1)?1:(locX[1]-locX[0]), 
				(locY.length==1)?1:(locY[1]-locY[0]), 
				(locZ.length==1)?1:(locZ[1]-locZ[0]), 
				locX[0], locY[0], locZ[0], null);
		_t0 	= t0;
		_locX 	= locX;
		_locY 	= locY;
		_locZ 	= locZ;
		allocate();
	}
	
	public Vecon3D(int iUnit, int id, 
			int nx, int ny, int nz, double dx, double dy, double dz, double x0, double y0, double z0, float [][][] v) {
		this(iUnit, id, 0, 0, 0, 
				0, 0, 0, 0, 0, 0, 0, 
				0, 0, 0, 0, 0, 0, 
				0, 0, 0, 0,
				nx, ny, nz, dx, dy, dz, x0, y0, z0, v);
	}
	public Vecon3D(int iUnit, int id, double vp, double vs, double den, 
			double delta, double epsilon, double gamma, double qp, double qs, double theta, double phi,
			double vpGradientX, double vpGradientY, double vpGradientZ, double vsGradientX, double vsGradientY, double vsGradientZ, 
			double delta1, double epsilon1, double gamma1, double delta3, 
			int nx, int ny, int nz, double dx, double dy, double dz, double x0, double y0, double z0, float [][][] v) {
		super(id, vp, vs, den, delta, epsilon, gamma, qp, qs, theta, phi, 
				vpGradientX, vpGradientY, vpGradientZ, vsGradientX, vsGradientY, vsGradientZ, 
				delta1, epsilon1, gamma1, delta3, 
				nx, ny, nz, dx, dy, dz, x0, y0, z0);
				
		_iUnit 		= iUnit;
		_data 			= v;
	}
	
	public void allocate() { _data = new float[getNx()][getNy()][getNz()]; }
	
	public float [] allocate(int n) 		{ return new float[n]; }
	public double getT0() 					{ return _t0; }
	public float [] getLoc(int id) 			{ if(id==1) return _locX; else if(id==2) return _locY; else return _locZ;}
	public float getLoc(int id, int index) 	{ if(id==1) return _locX[index]; else if(id==2) return _locY[index]; else return _locZ[index];}
	
	public void setT0(double t0) 					{ _t0 = t0; }
	public void setLoc(int id, float [] loc) 		{ if(id==1) _locX = loc; else if(id==2) _locY = loc; else _locZ = loc;}
	public void setLoc(int id, int index, float v) 	{ if(id==1) _locX[index] = v; else if(id==2) _locY[index] = v; else _locZ[index] = v;}
	
	public Vecon3D copy() {
		Vecon3D target = new Vecon3D(_iUnit, getId(), 
				getNx(), getNy(), getNz(), getDx(), getDy(), getDz(), getX0(), getY0(), getZ0(), copy(_data));
		return target;
	}
	public float [][][] copy(float [][][] src) {
		if(src==null) return null;
		float [][][] target = new float[_data.length][_data[0].length][_data[0][0].length];
		for(int i=0; i<src.length; i++) {
			for(int j=0; j<src[0].length; j++) {
				for(int k=0; k<src[0][0].length; k++) target[i][j][k] = src[i][j][k];
			}
		}
		return target;
	}
	
	public float max() 								{ return ArrayMath.max(_data); }
	public float min() 								{ return ArrayMath.min(_data); }
	
	public int getIUnit() 							{ return _iUnit; }
	public float [][][] getData() 					{ return _data; }
	public float getData(int ix, int iy, int iz) 	{ return _data[ix][iy][iz];	}
	public float getData(double x, double y, double z) 	{ 
		if(onGridPoint(x, y, z)) return getData((int)(x/getDx()), (int)(y/getDy()), (int)(z/getDz()) );
		else return interpolate(x, y, z);
	}
	public boolean onGridPoint(double x, double y, double z) {
		double ax = x/getDx(), ay = y/getDy(), az = z/getDz();
		return (ax-(int)ax)==0&&(ay-(int)ay)==0&&(az-(int)az)==0;
	}
	
	public void setIUnit(int iUnit) 						{ _iUnit = iUnit; }
	public void setData(float [][][] v) 					{ _data = v; }
	public void setData(int ix, int iy, int iz, float av) 	{ _data[ix][iy][iz] = av;	}
	public void setData(float av) 							{ 
		for(int i=0; i<_data.length; i++) {
			for(int j=0; j<_data[0].length; j++) {
				for(int k=0; k<_data[0][0].length; k++) _data[i][j][k] = av;
			}
		}
	}
	
//	public float [] getTraceData( int ix, int iy)  { 
//		float [] trace = new float[getNz()];
//		getTraceData(ix, iy, trace);
//		return trace;
//	}
//	public void getTraceData(int ix, int iy, float [] trace)  { 
//		for(int iz=0; iz<getNz(); iz++) { trace[iz] = _data[iy][ix][iz]; }
//	}
//	public void getTraceCoordinates(int ix, int iy, int [] coordinates)  { 
//		int k = 0;
//		coordinates[k++] = (int)(getX0()+ix*getDx());
//		coordinates[k++] = (int)(getY0()+iy*getDy());
//		coordinates[k++] = (int)(getZ0());
//		coordinates[k++] = (int)(getX0()+ix*getDx());
//		coordinates[k++] = (int)(getY0()+iy*getDy());
//		coordinates[k++] = (int)(getZ0());
//	}
//	public void setTraceData(int ix, int iy, float [] trace)  { 
//		for(int iz=0; iz<getNz(); iz++) { _data[iy][ix][iz] = trace[iz]; }
//	}
	
//	public float [][][] to3DData(int nx, int ny, int nz) { 
//		float [][][] v = new float[nz][ny][nx];
//		for(int iz=0, k=0; iz<nz; iz++) {
//			for(int iy=0; iy<ny; iy++) {
//				for(int ix=0; ix<nx; ix++, k++) { v[iz][iy][ix] = _data[k]; }
//			}
//		}
//		return v; 
//	}
	
	//children could be larger than parents
//	public float interpolate3D(double x, double y, double z) {
//		LinearInterpolator li = new LinearInterpolator();
//		li.setExtrapolation(LinearInterpolator.Extrapolation.CONSTANT);
//		li.setUniform(getNx(), getDx(), getX0(), getNy(), getDy(), getY0(), getNz(), getDz(), getZ0(), _data);
//		return li.interpolate(x, y, z);
//	}
	
	public float interpolate(double x, double y, double z) {
		double fmx 	= (x - getX0())/getDx();
		int imx1 	= (int)fmx;
		int imx2 	= imx1 + 1;
		imx1 		= clipX(imx1);
		imx2 		= clipX(imx2);
		double wmx2 = fmx - imx1;
		double wmx1 = 1.0 - wmx2;

		double fmy 	= (y - getY0())/getDy();
		int imy1 	= (int)fmy;
		int imy2 	= imy1 + 1;
		imy1 		= clipY(imy1);
		imy2 		= clipY(imy2);
		double wmy2 = fmy - imy1;
		double wmy1 = 1.0 - wmy2;

		double fmz 	= (z - getZ0())/getDz();
		int imz1 	= (int)fmz;
		int imz2 	= imz1 + 1;
		imz1 		= clipZ(imz1);
		imz2 		= clipZ(imz2);
		double wmz2 = fmz - imz1;
		double wmz1 = 1.0 - wmz2;

		double t =
				wmx1*	wmy1*	wmz1*	getData(imx1, imy1, imz1) +
				wmx1*	wmy1*	wmz2*	getData(imx1, imy1, imz2) +
				wmx1*	wmy2*	wmz1*	getData(imx1, imy2, imz1) +
				wmx1*	wmy2*	wmz2*	getData(imx1, imy2, imz2) +

				wmx2*	wmy1*	wmz1*	getData(imx2, imy1, imz1) +
				wmx2*	wmy1*	wmz2*	getData(imx2, imy1, imz2) +
				wmx2*	wmy2*	wmz1*	getData(imx2, imy2, imz1) +
				wmx2*	wmy2*	wmz2*	getData(imx2, imy2, imz2);

		return (float)t;
	}
	public void read(String selectedFileName ) {
		read(0, selectedFileName );
	}
	public void read(int iMethod, String selectedFileName ) {	
	    File file 		= null;
	    ArrayFile af 	= null;
	    
	    try {
	    	file = new File(selectedFileName);
	    	af = new ArrayFile(file,"rw", ByteOrder.LITTLE_ENDIAN, ByteOrder.LITTLE_ENDIAN);
	    	af.seek(0);
	    	if(iMethod==1) {
	    		setId(af.readInt());
	    	}
	    	int nx = af.readInt();
	    	int ny = af.readInt();
	    	int nz = af.readInt();
	    	float dx = af.readFloat();
	    	float dy = af.readFloat();
	    	float dz = af.readFloat();
	    	float x0 = af.readFloat();
	    	float y0 = af.readFloat();
	    	float z0 = af.readFloat();
	    	_iUnit = (int)af.readFloat();
	    	setSize(nx, ny, nz, (double)dx, (double)dy, (double)dz, (double)x0, (double)y0, (double)z0);

	    	//System.out.println(printClass(1));
	    	allocate();
	    	af.readFloats(_data);
	    } catch (FileNotFoundException e) {
	        System.err.println("Caught FileNotFoundException: " + e.getMessage());
	        throw new RuntimeException(e);
	    } catch (IOException e) {
	        System.err.println("Caught IOException: " + e.getMessage());
	    } finally {
	      if (af!=null)
	    	  try {
	    		  af.close();
              } catch (IOException e) {
              }
	    }
	}

	public void writeData(String selectedFileName) {	
	    File file = null;
	    ArrayFile af = null;
	    try {
	    	file = new File(selectedFileName);
	    	file = new File(selectedFileName);
	    	if (file.exists ()) {
	    		file.delete();
	    	}
	    	file = new File(selectedFileName);
	    	af = new ArrayFile(file,"rw", ByteOrder.LITTLE_ENDIAN, ByteOrder.LITTLE_ENDIAN);
	    	af.seek(0);
	    	af.writeFloats(_data);
	    } catch (FileNotFoundException e) {
	        System.err.println("Caught FileNotFoundException: " + e.getMessage());
	        throw new RuntimeException(e);
	    } catch (IOException e) {
	        System.err.println("Caught IOException: " + e.getMessage());
	    } finally {
	      if (af!=null)
	    	  try {
	    		  af.close();
              } catch (IOException e) {
              }
	    }
	}
	
	public void writeAsBinary(String selectedFileName ){
		writeAsBinary(0, selectedFileName);
	}

	public void writeAsBinary(int iMethod, String selectedFileName ){	
	    File file = null;
	    ArrayFile af = null;
	    try {
	    	file = new File(selectedFileName);
	    	if (file.exists ()) {
	    		file.delete();
	    	}
	    	file = new File(selectedFileName);
	    	af = new ArrayFile(file,"rw", ByteOrder.LITTLE_ENDIAN, ByteOrder.LITTLE_ENDIAN);
	    	af.seek(0);
	    	if(iMethod==1) {
	    		af.writeInt(getId());
	    	}
	    	af.writeInt(getNx());
	    	af.writeInt(getNy());
	    	af.writeInt(getNz());
	    	af.writeFloat((float)getDx());
	    	af.writeFloat((float)getDy());
	    	af.writeFloat((float)getDz());
	    	af.writeFloat((float)getX0());
	    	af.writeFloat((float)getY0());
	    	af.writeFloat((float)getZ0());
	    	af.writeFloat((float)_iUnit);
	    	af.writeFloats(_data);
	    } catch (FileNotFoundException e) {
	        System.err.println("Caught FileNotFoundException: " + e.getMessage());
	        throw new RuntimeException(e);
	    } catch (IOException e) {
	        System.err.println("Caught IOException: " + e.getMessage());
	    } finally {
	      if (af!=null)
	    	  try {
	    		  af.close();
              } catch (IOException e) {
              }
	    }
	}
	
	public void writeAsVtkBinary(String selectedFileName ){	
	    File file = null;
	    ArrayFile af = null;
	    try {
	    	file = new File(selectedFileName);
	    	if (file.exists ()) {
	    		file.delete();
	    	}
	    	file = new File(selectedFileName);
	    	af = new ArrayFile(file,"rw", ByteOrder.LITTLE_ENDIAN, ByteOrder.LITTLE_ENDIAN);
	    	af.seek(0);
	    	af.writeChars("# vtk DataFile Version 3.0\n");
	    	af.writeChars("vtk Vencon3D\n");
	    	af.writeChars("BINARY\n\n");
	    	af.writeChars("DATASET STRUCTURED_POINTS\n");
	    	af.writeChars("DIMENSIONS "+getNx()+" "+getNy()+" "+getNz()+"\n");
	    	af.writeChars("ORIGIN "+getX0()+" "+getY0()+" "+getZ0()+"\n");
	    	af.writeChars("SPACING "+getDx()+" "+getDy()+" "+getDz()+"\n");
	    	af.writeChars("POINT_DATA "+getNx()*getNy()*getNz()+"\n");
	    	af.writeChars("SCALARS Travel_Time_In_Seconds float 1 "+"\n");
	    	af.writeChars("LOOKUP_TABLE default "+"\n");
	    	for(int ix=0; ix<getNx(); ix++) {
	    		for(int iy=0; iy<getNy(); iy++) {
	    			for(int iz=0; iz<getNz(); iz++) {
	    				af.writeFloat(_data[ix][iy][iz]);
	    				af.writeChars("\n");
	    			}
	    		}
	    	}
	    } catch (FileNotFoundException e) {
	        System.err.println("Caught FileNotFoundException: " + e.getMessage());
	        throw new RuntimeException(e);
	    } catch (IOException e) {
	        System.err.println("Caught IOException: " + e.getMessage());
	    } finally {
	      if (af!=null)
	    	  try {
	    		  af.close();
              } catch (IOException e) {
              }
	    }
	}
	public void writeAsVtkText(String label, String selectedFileName ){	
		writeAsVtkText("Vencon3D", label, selectedFileName );
	}
	
	public void writeAsVtkText(String idString, String label, String selectedFileName ){	
		try{
			BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(selectedFileName, false));
			bufferedWriter.write("# vtk DataFile Version 3.0\n");
			bufferedWriter.write("vtk "+idString+"\n");
			bufferedWriter.write("ASCII\n\n");
			bufferedWriter.write("DATASET STRUCTURED_POINTS\n");
			bufferedWriter.write("DIMENSIONS "+getNx()+" "+getNy()+" "+getNz()+"\n");
			bufferedWriter.write("ORIGIN "+getX0()+" "+getY0()+" "+getZ0()+"\n");
			bufferedWriter.write("SPACING "+getDx()+" "+getDy()+" "+getDz()+"\n");
			bufferedWriter.write("POINT_DATA "+getNx()*getNy()*getNz()+"\n");
			bufferedWriter.write("SCALARS "+label+" float 1 "+"\n");
			bufferedWriter.write("LOOKUP_TABLE default "+"\n");
			for(int ix=0; ix<getNx(); ix++) {
				for(int iy=0; iy<getNy(); iy++) {
					for(int iz=0; iz<getNz(); iz++) {
						bufferedWriter.write(_data[ix][iy][iz]+"\n");
					}
				}
			}
			bufferedWriter.close();
		} catch (IOException ioexception) {
			JOptionPane.showMessageDialog(null, "\nFile Format is not right!", 
					"Error", JOptionPane.ERROR_MESSAGE);
		}
	}
	public void writeAsText(String selectedFileName ){	
		try{
			BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(selectedFileName, false));
			bufferedWriter.write(getNx()+"\n");	bufferedWriter.write(getNy()+"\n");	bufferedWriter.write(getNz()+"\n");
			bufferedWriter.write(getX0()+"\n");	bufferedWriter.write(getY0()+"\n");	bufferedWriter.write(getZ0()+"\n");
			bufferedWriter.write(getDx()+"\n");	bufferedWriter.write(getDy()+"\n");	bufferedWriter.write(getDz()+"\n");
			
			for(int ix=0; ix<getNx(); ix++) {
				for(int iy=0; iy<getNy(); iy++) {
					for(int iz=0; iz<getNz(); iz++) {
						bufferedWriter.write(_data[ix][iy][iz]+"\n");
					}
				}
			}
			bufferedWriter.close();
		} catch (IOException ioexception) {
			JOptionPane.showMessageDialog(null, "\nFile Format is not right!", "Error", JOptionPane.ERROR_MESSAGE);
		}
		
	}
	
	public String toString(int id) {
		if(id==1) {
			return String.format("nx=%d ny=%d nz=%d dx=%f dy=%f dz=%f x0=%f y0=%f z0=%f x1=%f y1=%f z1=%f",
					getNx(), getNy(), getNz(), getDx(), getDy(), getDz(), getX0(), getY0(), getZ0(), getX1(), getY1(), getZ1());
		}
		double x1 = getX0()+(getNx()-1)*getDx();
		double y1 = getY0()+(getNy()-1)*getDy();
		String b = new String(_iUnit+" "); 
		String a = new String("");
		a = String.format("%f %f %f %f", getVp(), getVpGradientX(), getVpGradientY(), getVpGradientZ()); 
		b = new String(b.concat("\n"+a));
		a = String.format("%f %f %f %f", getVs(), getVsGradientX(), getVsGradientY(), getVsGradientZ()); 
		b = new String(b.concat("\n"+a));
		a = String.format("%f", getDen()); 
		b = new String(b.concat("\n"+a));
		
		a = String.format("%f %f %f %f %f %f %f", getEpsilon(), getDelta(), getGamma(), getQp(), getQs(), getTheta(), getPhi());
		b = new String(b.concat("\n"+a));
		a = String.format("%d %d %f %f %f %f %f %f", getNx(), getNy(), getX0(), getY0(), getDx(), getDy(), x1, y1);
		b = new String(b.concat("\n"+a));
		if(id==2) {
			a = String.format("minV=%f, maxV=%f", getExtremeValue(1, _data), getExtremeValue(2, _data));
			b = new String(b.concat("\n"+a));
		}
		if(id==3) {
			a = String.format("%f ", getExtremeValue(1, _data), getExtremeValue(2, _data));
			b = new String(b.concat("\n"+a));
		}

		return b;
	}
	
	
	

}

