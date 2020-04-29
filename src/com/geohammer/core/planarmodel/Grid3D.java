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

public class Grid3D extends HalfSpaceBox {
	protected float [][][] 	_data 			= null; // _data[ix][iy][iz]
	protected int 			_iUnit 			= 2;	// =1 m; 2 ft; 3 km
	protected String 		_description	= "unknown";	
	protected String 		_fileName		= null;
	
	public Grid3D()	{ this(2); }

	public Grid3D(int iUnit)	{ 
		super(); 
		_iUnit = iUnit;
	}
	
	/*
	c
	c               o--------------o
	c              /:             /|
	c             / :            / |	* origin in float [] v
	c            #--------------o  |    # origin in float [][][] data
	c            |  :           |  |
	c            |  o  -  -  -  |- o
	c            | /            | /
	c            |/             |/
	c            *--------------o
	c
	c -------------------------------------------------------------*/
//	public Grid3D(int iUnit, int nxE, int nyN, int nzU, double dxE, double dyN, double dzU, 
//			double x0E, double y0N, double z0U, float [] v) {
//		_iUnit = iUnit;
//		double z = z0U +(nzU-1)*dzU;
//		setSize(nyN, nxE, nzU, dyN, dxE, dzU, y0N, x0E, z0U);
//		if(v!=null)  {
//			allocate();
//			int nxy = nxE*nyN;
//			for(int i=0; i<nxE; i++) {
//				for(int j=0; j<nyN; j++) {
//					for(int k=0; k<nzU; k++) {
//						_data[j][i][k] = v[nxy*(nzU-1-k) + nxE*j + i];
//					}
//				}
//			}
//		}
//	}
	
	public Grid3D(int iUnit, int nx, int ny, int nz, double dx, double dy, double dz, 
			double x0, double y0, double z0, float [][][] data) {
		_iUnit = iUnit;
		setSize(nx, ny, nz, dx, dy, dz, x0, y0, z0);
		if(data==null) allocate();
		else _data = data;
	}

	public Sampling getSampling(int iAxis) {
		if(iAxis==0) return new Sampling(getNz(), getDz(), getZ0());
		else if(iAxis==1) return new Sampling(getNy(), getDy(), getY0());
		else return new Sampling(getNx(), getDx(), getZ0());
	}

	public Grid3D(String selectedFileName)	{
		read(selectedFileName );
		_fileName		= selectedFileName;
	}

	public String getFileName() 				{ return _fileName; }
	public void setFileName(String fileName) 	{ _fileName = fileName; }
	
	public void allocate() { _data = new float[getNx()][getNy()][getNz()]; }

	public Grid3D copy() {
		Grid3D target = new Grid3D(_iUnit, getNx(), getNy(), getNz(), 
				getDx(), getDy(), getDz(), getX0(), getY0(), getZ0(), copy(_data));
		return target;
	}
	public float [][][] copy(float [][][] src) {
		if(src==null) return null;
		float [][][] target = new float[src.length][src[0].length][src[0][0].length];
		for(int i=0; i<src.length; i++) {
			for(int j=0; j<src[0].length; j++) {
				for(int k=0; k<src[0][0].length; k++) target[i][j][k] = src[i][j][k];
			}
		}
		return target;
	}
	
	public void scaleData(float scalor) {
		for(int i=0; i<_data.length; i++) {
			for(int j=0; j<_data[0].length; j++) {
				for(int k=0; k<_data[0][0].length; k++) _data[i][j][k] *= scalor;
			}
		}
	}

	public float max() 								{ return ArrayMath.max(_data); }
	public float min() 								{ return ArrayMath.min(_data); }
	public String getDescription() 					{ return _description; }
	public int getIUnit() 							{ return _iUnit; }
	public float [][][] getData() 					{ return _data; }
	public float getData(int ix, int iy, int iz) 	{ return _data[ix][iy][iz];	}
	public float getData(double x, double y, double z) 	{ 
		double ax = clipX(x);
		double ay = clipY(y);
		double az = clipZ(z);
		if(onGridPoint(ax, ay, az)) return getData((int)(ax/getDx()), (int)(ay/getDy()), (int)(az/getDz()) );
		else return interpolate(ax, ay, az);
	}
	public boolean onGridPoint(double x, double y, double z) {
		double ax = x/getDx(), ay = y/getDy(), az = z/getDz();
		return (ax-(int)ax)==0&&(ay-(int)ay)==0&&(az-(int)az)==0;
	}
	public int getGridIndexX(double x) {
		double x0 = getX0(); double dx = getDx(); double ax = (x-x0)/dx;
		return clipX((int)ax);
	}
	public int getGridIndexY(double y) {
		double x0 = getY0(); double dx = getDy(); double ax = (y-x0)/dx;
		return clipY((int)ax);
	}
	public int getGridIndexZ(double z) {
		double x0 = getZ0(); double dx = getDz(); double ax = (z-x0)/dx;
		return clipZ((int)ax);
	}

	public float [] extractAxis(int ix) {
		int n = getNx();
		if(ix==0) n = getNz();
		else if(ix==1) n = getNy();
		float [] data = new float[n];
		double a = 0;
		for(int i=0; i<n; i++) {
			if(ix==0) a = getZ0()+i*getDz();
			else if(ix==1) a = getY0()+i*getDy();
			else a = getX0()+i*getDx();
			data[i] = (float)a;
		}
		return data;
	}
	
	public float [] extractCurveX(int iy, int iz) {
		int n = getNx();
		float [] data = new float[n];
		for(int i=0; i<n; i++) data[i] = _data[i][iy][iz];
		return data;
	}
	public float [] extractCurveY(int ix, int iz) {
		int n = getNy();
		float [] data = new float[n];
		for(int i=0; i<n; i++) data[i] = _data[ix][i][iz];
		return data;
	}
	public float [] extractCurveZ(int ix, int iy) {
		int n = getNz();
		float [] data = new float[n];
		for(int i=0; i<n; i++) data[i] = _data[ix][iy][i];
		return data;
	}
	
	public void setDescription(String description) 			{ _description = description; }
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
	
	public void setOutmostFaces(float av) 							{ 
		for(int i=0; i<_data.length; i+=_data.length-1) {
			for(int j=0; j<_data[0].length; j++) {
				for(int k=0; k<_data[0][0].length; k++) _data[i][j][k] = av;
			}
		}
		for(int i=0; i<_data.length; i++) {
			for(int j=0; j<_data[0].length; j+=_data[0].length-1) {
				for(int k=0; k<_data[0][0].length; k++) _data[i][j][k] = av;
			}
		}
		for(int i=0; i<_data.length; i++) {
			for(int j=0; j<_data[0].length; j++) {
				for(int k=0; k<_data[0][0].length; k+=_data[0][0].length-1) _data[i][j][k] = av;
			}
		}
	}

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
		File file 		= null;
		ArrayFile af 	= null;

		try {
			file = new File(selectedFileName);
			af = new ArrayFile(file,"rw", ByteOrder.LITTLE_ENDIAN, ByteOrder.LITTLE_ENDIAN);
			af.seek(0);
			setId(af.readInt());
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
			af.writeInt(getId());
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
