package com.geohammer.rt;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.Date;

import javax.swing.JOptionPane;

import org.apache.commons.io.FileUtils;
import org.ucdm.core.SeismicTraceComponent;
import org.ucdm.core.acquisition.VCPair;
import org.ucdm.core.planarmodel.DipLayer1D;
import org.ucdm.core.vecon.VeconModel;
import org.ucdm.mt.Green;
import org.ucdm.mt.MTForward;
import org.ucdm.mti.run.LogWindowHandler;

public class RtNative {
	//private LogWindowHandler _handler 		= null;
	String 	_resultDir 	= null;
	boolean _cancel 	= false;
	int 	_index 		= -1;
	int 	_nt 		= 1;
	Date 	_start 		= null;

	//VolumeFrame _frame 		= null;
	VeconModel 	_veconModel = null;
	VeconModel 	_veconTime = null;
	VCPair 		_vcPW 		= null;

	public RtNative() {
		
	}
	public RtNative(VeconModel veconModel, VeconModel veconTime, VCPair vcPW, String resultDir) {
		//_frame 		= frame;
		_veconModel = veconModel;
		_veconTime = veconTime;
		_vcPW 		= vcPW;
		_resultDir 	= resultDir;
//		_handler 	= new LogWindowHandler();
//		_handler.getLogWindow().setMaxNumOfLines(5000);
//
//		_start = new java.util.Date();
//		_handler.publish("\nStarting at " + new Timestamp(_start.getTime())+"\n");
	}

	public Date getStartTime() { return _start; }

	//	public static void main(String[] args) {
	//		new Fd3dNative(null);
	//	}
	
	public void test1() {
		//helloWorld();
		int nx = 101;
		int ny = 101;
		int nz = 101;
		double h = 1.0;
		double x0 = 0.0;
		double y0 = 0.0;
		double z0 = 0.0;
		int nxyz = nx*ny*nz;
		float [] slow0 = new float[nxyz];
		float [] time0 = new float[nxyz];
		for(int i=0; i<nxyz; i++) { slow0[i] = (float)(1.0/1000.0); }

		double sx = 50.0;
		double sy = 50.0;
		double sz = 50.0;
		start(nx, ny, nz, h, x0, y0, z0, sx, sy, sz, slow0, time0);
		
		
	}

	public static synchronized void helloWorld(){
		double [] v= new double[20];
		for(int i=0; i<v.length; i++) v[i] = i;
		int errorID = JHelloWorld ("C:\\prowess\\vs2012\\fd\\fd3d\\fd3dinput.asc", v );
	}

	public String makeDir(String root, String dirName) {
		String dir = root+File.separator+dirName;
		File file = new File(dir);

		if (!file.exists()) {
			if (file.mkdir()) {
				System.out.println("Directory is created! "+dir); 
			}
		} else {
			try {
				FileUtils.cleanDirectory(file);
			} catch (IOException e) {
				System.out.println("Directory cleared un-successfully");
				e.printStackTrace();
			} 
		}
		return dir+"\\";
	}
	//public void logging(String msg) { _handler.publish(msg); }
	
	private void setValue3(int iGroup, int iReceiver, int index, float v) {
		System.out.println(iGroup+" " + iReceiver+" " +index+" " +v);
	}

	public int getCancelInt() 				{ return _cancel?1:0; }
	public void flipCancel() 				{ _cancel = !_cancel; }
	public void setCancel(boolean cancel) 	{ _cancel = cancel; }

	public synchronized void start(){
		int nx = _veconModel.getNx();
		int ny = _veconModel.getNy();
		int nz = _veconModel.getNz();
		
		double h = _veconModel.getDx();
		
		double x0 = _veconModel.getX0();
		double y0 = _veconModel.getY0();
		double z0 = _veconModel.getZ0();
		
		float [] slow0 = _veconModel.getData();
		float [] time0 = _veconTime.getData();
		
		double sx = _vcPW.getEE(0);
		double sy = _vcPW.getEN(0);
		double sz = _vcPW.getED(0);
		
		JCalTime(nx, ny, nz, h, x0, y0, z0, sx, sy, sz, slow0, time0);
	}
	public synchronized void print1(){
		float [] time = _veconTime.getData();
		int di = time.length/10-1;
		for(int i=0; i<10; i++) System.out.print(" t2="+time[i*di]);
	}

	public static synchronized void start(int nx, int ny, int nz, double h, 
			double x0, double y0, double z0, double sx, double sy, double sz, float [] slow0, float [] time0){
		JCalTime(nx, ny, nz, h, x0, y0, z0, sx, sy, sz, slow0, time0);
	}


	//  native method declarations
	private static native int JHelloWorld (String fileName, double[] v);
	private native int JPrintClass ();
	public static native void JCalTime(int nx, int ny, int nz, double h, 
			double x0, double y0, double z0, double sx, double sy, double sz, float [] slow0, float [] time0);
	
	
}
