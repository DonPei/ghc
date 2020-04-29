package com.geohammer.core.vecon;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.swing.JOptionPane;

import com.geohammer.core.planarmodel.HalfSpaceBox;

import edu.mines.jtk.util.ArrayMath;

public class VeconModel  extends HalfSpaceBox {
	//#define tt(ix,iy,iz)				timeMap[nxy*(iz) + nx*(iy) + (ix)]
	private float [] 	_data 		= null; 	// x is the fastest direction z is the slowest direction
	private int 		_iUnit 		= 2;		// =1 m; 2 ft; 3 km
	
	public VeconModel() {	}
	
	public VeconModel(int iUnit, int id, 
			int nx, int ny, int nz, double dx, double dy, double dz, double x0, double y0, double z0, float [] data) {
		this(iUnit, id, 0, 0, 0, 
				0, 0, 0, 0, 0, 0, 0, 
				0, 0, 0, 0, 0, 0, 
				0, 0, 0, 0,
				nx, ny, nz, dx, dy, dz, x0, y0, z0, data);
	}
	public VeconModel(int iUnit, int id, double vp, double vs, double den, 
			double delta, double epsilon, double gamma, double qp, double qs, double theta, double phi,
			double vpGradientX, double vpGradientY, double vpGradientZ, double vsGradientX, double vsGradientY, double vsGradientZ, 
			double delta1, double epsilon1, double gamma1, double delta3, 
			int nx, int ny, int nz, double dx, double dy, double dz, double x0, double y0, double z0, float [] data) {
		super(id, vp, vs, den, delta, epsilon, gamma, qp, qs, theta, phi, 
				vpGradientX, vpGradientY, vpGradientZ, vsGradientX, vsGradientY, vsGradientZ, 
				delta1, epsilon1, gamma1, delta3, 
				nx, ny, nz, dx, dy, dz, x0, y0, z0);
				
		_iUnit 		= iUnit;
		_data		= data;
	}
	public VeconModel(String selectedFileName)	{
		readVeconModel(selectedFileName );
	}
	
	public VeconModel copy() {
		VeconModel veconModel = new VeconModel(_iUnit, 0, getNx(), getNy(), getNz(), 
				getDx(), getDy(), getDz(), getX0(), getY0(), getZ0(), null);
		veconModel.setData(ArrayMath.copy(_data));
		return veconModel;
	}
	
	public int getIUnit() 									{ return _iUnit; }
	public float [] getData() 								{ return _data; }
	public float getData(int index) 						{ return _data[index]; }
	public float getData(int ix, int iy, int iz) 			{ return _data[getSequenceIndex(ix, iy, iz)]; }
	public float getData(double x, double y, double z) 		{ return interpolate(x, y, z); }
	
	public void setData(float v) 							{ for(int i=0; i<_data.length; i++) _data[i] = v; }
	public void setData(int ix, int iy, int iz, float data) { _data[getSequenceIndex(ix, iy, iz)] = data; }
	public void setData(int index, float data) 				{ _data[index] = data; }
	public void setData(float [] data) 						{ _data = data; }

	
	public float getExtremeValue(int id, float [] vel)  { 
		float a = 0.0f;

		if(id==1) a = 1.0e20f;
		else a = -1.0e20f;

		int k = 0;
		for(int iz=0; iz<getNz(); iz++) {
			for(int iy=0; iy<getNy(); iy++) {
				for(int ix=0; ix<getNx(); ix++) {
					if(id==1) a = a<vel[k] ? a:vel[k];
					else a = a>vel[k] ? a:vel[k];
					k++;
				}
			}
		}
		return a; 
	}
	public int getExtremeValueIndex(int id, float [] vel )  { 
		float a = 0.0f;
		int index = 0;

		if(id==1) a = 1.0e20f;
		else a = -1.0e20f;

		int k = 0;
		for(int iz=0; iz<getNz(); iz++) {
			for(int iy=0; iy<getNy(); iy++) {
				for(int ix=0; ix<getNx(); ix++) {
					if(id==1) {
						if(a>vel[k]) {
							a = vel[k];
							index = k;
						}
					}
					else {
						if(a<vel[k]) {
							a = vel[k];
							index = k;
						}
					}
					k++;
				}
			}
		}
		return index; 
	}

	public int getSequenceIndex( float x, float y, float z )  { 
		int ix = (int)((x-getX0())/getDx());
		int iy = (int)((y-getY0())/getDy());
		int iz = (int)((z-getZ0())/getDz());
		return getSequenceIndex(ix, iy, iz);
	}
	public int getSequenceIndex( int ix, int iy, int iz )  { 
		return iz*getNx()*getNy()+iy*getNx()+ix;
	}
	
	public void reciprical() { for(int i=0; i<_data.length; i++) _data[i] = 1.0f/_data[i]; }
	
	
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
	
//	public void scaleHeader(double scale) {
//		scaleHeader(scale, scale, scale);
//	}
//	public void scaleHeader(double scaleX, double scaleY, double scaleZ) {
//		_dx *= scaleX;
//		_dy *= scaleY;
//		_dz *= scaleZ;
//		
//		_x0 *= scaleX;
//		_y0 *= scaleY;
//		_z0 *= scaleZ;
//
//		setNModelSize(_nx, _ny, _nz, _dx, _dy, _dz, _x0, _y0, _z0);
//		setMModelSize();
//	}
//	public void shiftHeader(double shiftX, double shiftZ) {
//		shiftHeader(shiftX, 0.0, shiftZ);
//	}
//	public void shiftHeader(double shiftX, double shiftY, double shiftZ) {
//		_x0 += shiftX;
//		_y0 += shiftY;
//		_z0 += shiftZ;
//
//		setNModelSize(_nx, _ny, _nz, _dx, _dy, _dz, _x0, _y0, _z0);
//		setMModelSize();
//	}
//	public void shiftDepth(double dz) {
//		shift(0.0, 0.0, dz);
//	}
//	public void shift(double dx, double dy, double dz) {
//		setNModelSize(_nx, _ny, _nz, _dx, _dy, _dz, _x0+dx, _y0+dy, _z0+dz);
//		setMModelSize();
//	}

	public class Header {
		public int 		_nx;
		public int 		_ny;
		public int 		_nz;
		public double 	_dx;
		public double 	_dy;
		public double 	_dz;
		public double 	_x0;
		public double 	_y0;
		public double 	_z0;

		public int 	_iUnit;		//1 m; 2 ft; 3 km

		public Header( )	{
		}
		public String toString(){
			return String.format("nx=%d ny=%d nz=%d dx=%f dy=%f dz=%f x0=%f y0=%f z0=%f unit=%d",
					_nx, _ny, _nz, _dx, _dy, _dz, _x0, _y0, _z0, _iUnit);
		}
	}

	public void smoothVelMatrix (float [][]vel, int nz, int nx, float hsmooth, float vsmooth, int slowness) {
		/*****************************************************************************
		Smooth a matrix of velocities
		 *****************************************************************************
		man_file=vel
		Input:
		vel		Two dimensional matrix of velocities.  z is the fastest dimension.
		nz		vertical dimension of matrix, faster direction
		nx		horizontal dimension of matrix
		hsmooth		width of triangular smooth to apply in the horizontal
				direction  (in number of points)
		vsmooth		height of triangular smooth to apply in the vertical
				direction  (in number of points)
		slowness	TRUE if the velocity should be smoothed in the slowness
				domain, such as if the velocity is interval in depth.

		Output:
		vel		Output matrix
		 *****************************************************************************/
		int i,ix,iz;
		int k1, k2;
		int nsmooth;
		float weight,denom;
		float [][]vtemp = new float[nx][nz];

		nsmooth= (int)Math.abs(hsmooth);
		if ((float)nsmooth==Math.abs(hsmooth))
			nsmooth--;
		if(nsmooth<0)nsmooth=0;

		if (slowness==1){
			for (i = 0; i < nx; i++)
				for (iz = 0; iz < nz; iz++)
					vel[i][iz]= 1.0f/vel[i][iz];
		}

		if (nsmooth>0) {
			//Copy elements from one vector to another
			for (i = 0; i < nx; i++)
				for (iz = 0; iz < nz; iz++)
					vtemp[i][iz]= vel[i][iz];
			for (i = 0; i < nx; i++) {
				denom= 0.0f;
				for (iz = 0; iz < nz; iz++)
					vel[i][iz]=0.0f;
				k1 = (i-nsmooth)>0 ? (i-nsmooth):0; //MAX(0,i-nsmooth);
				k2 = (i+nsmooth+1)<nx ? (i+nsmooth+1):nx; //MIN(nx,i+nsmooth+1)
				for (ix = k1; ix < k2; ix++) {
					weight= (hsmooth-Math.abs(ix-i));
					denom+= weight;
					for (iz = 0; iz < nz; iz++)
						vel[i][iz]+= weight*vtemp[ix][iz];
				}
				denom= 1.0f/denom;
				for (iz = 0; iz < nz; iz++)
					vel[i][iz]*=denom;

			}
		}

		nsmooth= (int)Math.abs(vsmooth);
		if ((float)nsmooth==Math.abs(vsmooth))
			nsmooth--;
		if (nsmooth>1 ) {
			for (i = 0; i < nx; i++)
				for (iz = 0; iz < nz; iz++)
					vtemp[i][iz]= vel[i][iz];
			for (i = 0; i < nz; i++) {
				denom= 0.0f;
				for (ix = 0; ix < nx; ix++)
					vel[ix][i]=0.0f;
				k1 = (i-nsmooth)>0 ? (i-nsmooth):0; //MAX(0,i-nsmooth);
				k2 = (i+nsmooth+1)<nz ? (i+nsmooth+1):nz; //MIN(nx,i+nsmooth+1)
				for (iz = k1; iz < k2; iz++) {
					weight= (vsmooth-Math.abs(iz-i));
					denom+= weight;
					for (ix = 0; ix < nx; ix++)
						vel[ix][i]+= weight*vtemp[ix][iz];
				}
				denom= 1.0f/denom;
				for (ix = 0; ix < nx; ix++)
					vel[ix][i]= (denom * vel[ix][i]);
			}

		}

		if (slowness==1)
			for (ix = 0; ix < nx; ix++)
				for (i = 0; i < nz; i++)
					vel[ix][i]= 1.0f/(vel[ix][i]);
	}
	
	

	public void readVeconModel(String selectedFileName ){ }
	public void writeVeconModel(String selectedFileName ){ }
	
	public void writeAsVtkText(String label, String selectedFileName) {
		writeAsVtkText("Vencon3D", label, selectedFileName);
	}
	
	public void writeAsVtkText(String idString, String label, String selectedFileName){	
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
						bufferedWriter.write(getData(ix, iy, iz)+"\n");
					}
				}
			}
			bufferedWriter.close();
		} catch (IOException ioexception) {
			JOptionPane.showMessageDialog(null, "\nFile Format is not right!", 
					"Error", JOptionPane.ERROR_MESSAGE);
		}
	}

}
