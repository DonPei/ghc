package com.geohammer.rt;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

import javax.swing.JOptionPane;

import org.ucdm.core.MatrixFileReader;
import org.ucdm.core.SeismicTraceComponent;
import org.ucdm.core.acquisition.VCPair;
import org.ucdm.core.planarmodel.DipLayer1D;
import org.ucdm.core.planarmodel.Layer2D;
import org.ucdm.core.planarmodel.SingleFlatLayer;
import org.ucdm.core.planarmodel.Vecon2D;
import org.ucdm.ivc.LayerFrame;
import org.ucdm.mt.Green;
import org.ucdm.mti.run.LogWindowHandler;
import org.ucdm.rt.pseudo3d.Pseudo3DRayTracer;
import org.ucdm.rt.pseudo3d.RayTracerI;
import org.ucdm.sgl.VolumeFrame;

public class TravelTimeTableNative {
	private LogWindowHandler _handler 		= null;
	SeismicTraceComponent [][] _comps = null;
	String 	_resultDir 	= null;
	boolean _cancel 	= false;
	int _index = -1;
	int _nt = 1;
	Date _start = null;

	LayerFrame _frame 	= null;
	VolumeFrame _frameV 	= null;
	double  _dt 		= 0.0001;
	double  _totalTime 	= 0.6;     	// total time for simulation
	int 	_nSources		= 1;			// number of sources to fire simultaneously 
	int 	_iSourceComp	= 0;			// source type
	int 	_iWavelet		= 0;			// wavelet type
	double 	_amp0 			= 1.0e9;       // amplitude
	double 	_freq0 			= 200.0;        // center freq
	String _sourceFileName 	= null;
	double [][] _mt 		= new double[3][3]; //moment tensor

	public TravelTimeTableNative(LayerFrame frame, VolumeFrame frameV, String resultDir, 
			int iRayTracer, DipLayer1D dipLayer1D, Layer2D layer2D, VCPair vcPW, double dx, 
			Vecon2D vp2D, Vecon2D vs2D, Vecon2D den2D, Vecon2D epsilon2D, Vecon2D delta2D, Vecon2D gamma2D) {
		_frame 		= frame;
		_frameV		= frameV;
		_resultDir 	= resultDir;
		_handler 	= new LogWindowHandler();
		_handler.getLogWindow().setMaxNumOfLines(5000);
	}

	public Date getStartTime() { return _start; }

	//	public static void main(String[] args) {
	//		new Fd3dNative(null);
	//	}

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
		}
		return dir+"\\";
	}
	public void logging(String msg) {
		_handler.publish(msg);
	}
	public void setSeismicTraceComponent(int iGroup, int iReceiver, int index, float v) {
		_comps[iGroup][iReceiver].setData(index, v);
		//System.out.println(iGroup+" " + iReceiver+" " +index+" " +v);
		if(_index!=index) {
			logging(index+" / " + _nt+"  " +v+"\n");
			_index=index;
		}
	}
	private void setValue3(int iGroup, int iReceiver, int index, float v) {
		System.out.println(iGroup+" " + iReceiver+" " +index+" " +v);
	}

	public int getCancelInt() { 
		if(_cancel==true) return 1;
		else return 0;
	}
	public void flipCancel() { 
		_cancel = !_cancel;
	}


	public void setCancel(boolean cancel) { 
		_cancel = cancel; 
	}
	public TravelTimeTableToken getToken() { return null; }

	public synchronized void start(){
		String inputFileName = _resultDir+File.separator+"input.txt";
		String logFileName = _resultDir+File.separator+"log.txt";
		String histogramCwd = makeDir(_resultDir, "hist");
		String snapCwd = makeDir(_resultDir, "snap");
		int errorID = JStart (inputFileName, logFileName, histogramCwd, snapCwd);
	}

	public static synchronized void start(String inputFileName, String logFileName, 
			String histogramCwd, String snapCwd){
		int errorID = JStart (inputFileName, logFileName, histogramCwd, snapCwd);
	}

	public synchronized int fd3dStart(int id){
		_start = new java.util.Date();
		_handler.publish("\nStarting at " + new Timestamp(_start.getTime())+"\n");

		String inputFileName = _resultDir+File.separator+"input.txt";
		String logFileName = _resultDir+File.separator+"log.txt";
		String histogramCwd = makeDir(_resultDir, "hist");
		String snapCwd = makeDir(_resultDir, "snap");
		int errorID = 0;

		if(id==0) errorID = (int)JFd3dStart (inputFileName, logFileName, histogramCwd, snapCwd);
		else if(id==1) {
			errorID = (int)fd3dStart (inputFileName, logFileName, histogramCwd, snapCwd);
		} else if(id==2) {
			errorID = (int)fd3dStart2 (inputFileName, logFileName, histogramCwd, snapCwd);
		}

		return errorID;
	}

	//public void setPseudo3DRayTracer(Pseudo3DRayTracer rt) { _rt = rt; }
	public int fd3dStart (String inputFileName, String logFileName, 
			String histogramCwd, String snapCwd) {

		Pseudo3DRayTracer rt 	= _frame.getRayTracer();
		DipLayer1D dipLayer1D 	=  rt.getDipLayer1D();
		VCPair vcPW 			= rt.getVCPair();
		//System.out.println(vcPW.toString());
		
		read(inputFileName);
		
		double vp = dipLayer1D.getLayer(0).getVp();      // P-wave velocity
		double vs = dipLayer1D.getLayer(0).getVs();      // S-wave velocity
		double den = dipLayer1D.getLayer(0).getDen();    // density

		double f0 	= _freq0;        // center frequency (Hz)
		double dt 	= _dt;    		// time sampling (sec)
		int nt 		= _nt;
		double amp0 = _amp0;       // amplitude

		//double [][] M = new double [][] { {1, 0, 0}, {0, 1, 0}, {0, 0, 1} }; // moment tensor
		double [][] M = _mt; // moment tensor

		logging("dt="+_dt+" nt="+_nt+"\n");
		logging("iWavelet="+_iWavelet+" amp0="+_amp0+" freq0="+_freq0+" iSourceComp="+_iSourceComp+"\n");
		logging("mt[0][0]="+_mt[0][0]+" "+_mt[0][1]+" "+_mt[0][2]+"\n");
		logging("mt[1][0]="+_mt[1][0]+" "+_mt[1][1]+" "+_mt[1][2]+"\n");
		logging("mt[2][0]="+_mt[2][0]+" "+_mt[2][1]+" "+_mt[2][2]+"\n");
		
		double [] t = new double[nt]; 
		for(int i=0; i<nt; i++) t[i] = i*dt;

		double [] U = new double[nt];
		double [] V = new double[nt];
		Green green  = new Green();
		
		for(int i=0; i<vcPW.getFlag(0); i++) {
			double [] src= new double [] {vcPW.getEN(0), vcPW.getEE(0), vcPW.getED(0)};  // source position
			double [] rec= new double [] {vcPW.getRN(i), vcPW.getRE(i), vcPW.getRD(i)};  // receiver position
			double [] rm = new double[] { rec[0]-src[0], rec[1]-src[1], rec[2]-src[2] };
			double r = Math.sqrt(rm[0]*rm[0]+rm[1]*rm[1]+rm[2]*rm[2]);

			double [] directionCos = new double[] { rm[0]/r, rm[1]/r, rm[2]/r };

			//System.out.println("tp="+(r/vp)+" ts="+(r/vs));
			for(int n=0; n<3; n++) {
//				green.calHomoGreen(false, false, true, r, directionCos, n, M,
//						vp, vs, den, t, f0, amp0, U);
//				
//				//displacement to velocity
//				V[0] = U[0];
//				//for(int j=1; j<nt; j++) { V[j] = (U[j]-U[j-1])/dt; }
//				for(int j=1; j<nt; j++) { V[j] = U[j]; }
//				
//				for(int j=0; j<nt; j++) {
//					_comps[n][i].setData(j, (float)V[j]);
//				}
			}
//			float [] ux = U[0]; // x-component displacement
//			float [] uy = U[1]; // y-component
//			float [] uz = U[2]; // z-component
			//_comps[iGroup][iReceiver].setData(index, U[iGroup][index]);			
		}

		return 1;
	}

	public int fd3dStart2 (String inputFileName, String logFileName, 
			String histogramCwd, String snapCwd) {

		Pseudo3DRayTracer rt 	= _frame.getRayTracer();
		DipLayer1D dipLayer1D 	=  rt.getDipLayer1D();
		VCPair vcPW 			= rt.getVCPair();
		//System.out.println(vcPW.toString());
		
		read(inputFileName);
		
		double vp = dipLayer1D.getLayer(0).getVp();      // P-wave velocity
		double vs = dipLayer1D.getLayer(0).getVs();      // S-wave velocity
		double den = dipLayer1D.getLayer(0).getDen();    // density

		double f0 	= _freq0;        // center frequency (Hz)
		double dt 	= _dt;    		// time sampling (sec)
		int nt 		= _nt;
		double amp0 = _amp0;       // amplitude

		//double [][] M = new double [][] { {1, 0, 0}, {0, 1, 0}, {0, 0, 1} }; // moment tensor
		double [][] M = _mt; // moment tensor

		logging("dt="+_dt+" nt="+_nt+"\n");
		logging("iWavelet="+_iWavelet+" amp0="+_amp0+" freq0="+_freq0+" iSourceComp="+_iSourceComp+"\n");
		logging("mt[0][0]="+_mt[0][0]+" "+_mt[0][1]+" "+_mt[0][2]+"\n");
		logging("mt[1][0]="+_mt[1][0]+" "+_mt[1][1]+" "+_mt[1][2]+"\n");
		logging("mt[2][0]="+_mt[2][0]+" "+_mt[2][1]+" "+_mt[2][2]+"\n");
		
		double [] t = new double[nt]; 
		for(int i=0; i<nt; i++) t[i] = i*dt;

		double [][] U = new double[3][nt];
		double [] V = new double[nt];
		Green green  = new Green();
		
		//System.out.println(vcPW.getFlag(0));
		for(int i=0; i<vcPW.getFlag(0); i++) {
			double [] src= new double [] {vcPW.getEN(0), vcPW.getEE(0), vcPW.getED(0)};  // source position
			double [] rec= new double [] {vcPW.getRN(i), vcPW.getRE(i), vcPW.getRD(i)};  // receiver position
			double [] rm = new double[] { rec[0]-src[0], rec[1]-src[1], rec[2]-src[2] };
			double r = Math.sqrt(rm[0]*rm[0]+rm[1]*rm[1]+rm[2]*rm[2]);

			double [] directionCos = new double[] { rm[0]/r, rm[1]/r, rm[2]/r };

			//System.out.println("tp="+(r/vp)+" ts="+(r/vs));
			double x = rm[0];
			double y = rm[1];
			double z = rm[2];
			double l = Math.sqrt(x*x+y*y);
			double ct = z/r;
			double st = l/r;
			double ca = x/l;
			double sa = y/l;
			green.calHomoFarFieldGreen(r, ct, st, ca, sa, M, vp, vs, den, t, f0, amp0, U);
			System.out.println("i="+i+" "+vcPW.getFlag(0)+" m="+_comps.length+" n="+_comps[0].length);
			for(int n=0; n<3; n++) {
				
				//displacement to velocity
				V[0] = U[n][0];
				//for(int j=1; j<nt; j++) { V[j] = (U[j]-U[j-1])/dt; }
				for(int j=1; j<nt; j++) { V[j] = U[n][j]; }
				
				for(int j=0; j<nt; j++) {
					_comps[n][i].setData(j, (float)V[j]);
				}
			}
		}

		return 1;
	}

	public void read(String selectedFileName) {
		String  delimiter = ",|\\s+";

		try{
			BufferedReader reader = new BufferedReader(new FileReader(selectedFileName));
			reader.readLine();
			reader.readLine();
			String line=reader.readLine();
			String[] splits = line.split(delimiter);

			double dt = Double.parseDouble(splits[3]);
			_dt = dt/1000.0;
			_nt = Integer.parseInt(splits[4]);
			
			reader.readLine();

			reader.readLine();
			reader.readLine();
			reader.readLine();

			line=reader.readLine();
			int modeltype = Integer.parseInt(line.trim());
			// =1 "horizotallly isotropic layer model. \n"
			// =2 "transversely isotropic layer model. \n"
			// =3 "model read from 'model.bin'. \n"
			// =4 "layer model with laterally varying interfaces. \n"
			// =21 "full anisotropy model with 21 elastic constants. \n"

			if(modeltype==1 || modeltype==2 || modeltype==4) {
				line=reader.readLine();
				int nlayer = Integer.parseInt(line.trim());	
				for(int li=0;li<nlayer;li++)  {
					if(modeltype==1) {
						reader.readLine();
					}
					else if(modeltype==2 || modeltype==4) {
						reader.readLine();
						reader.readLine();
						if(modeltype==2) reader.readLine();

					}
				}
			}
			//iBorehole
			reader.readLine();
			//readin discrete fractures parameters
			reader.readLine();

			// wavelet type: 0 = Ricker; 1 = plane wave 
			line=reader.readLine();
			_iWavelet = Integer.parseInt(line.trim());
			line=reader.readLine();
			splits = line.split(delimiter);
			_amp0 = Double.parseDouble(splits[0]);
			_freq0 = Double.parseDouble(splits[1]);
			line=reader.readLine();
			_iSourceComp = Integer.parseInt(line.trim());
			line=reader.readLine();
			splits = line.split(delimiter);
			_mt[0][0] = Double.parseDouble(splits[0]);
			_mt[0][1] = Double.parseDouble(splits[1]);
			_mt[0][2] = Double.parseDouble(splits[2]);
			line=reader.readLine();
			splits = line.split(delimiter);
			_mt[1][0] = Double.parseDouble(splits[0]);
			_mt[1][1] = Double.parseDouble(splits[1]);
			_mt[1][2] = Double.parseDouble(splits[2]);
			line=reader.readLine();
			splits = line.split(delimiter);
			_mt[2][0] = Double.parseDouble(splits[0]);
			_mt[2][1] = Double.parseDouble(splits[1]);
			_mt[2][2] = Double.parseDouble(splits[2]);
			reader.close();
		} catch (IOException ioexception) {
			JOptionPane.showMessageDialog(null, "\nFile Format is not right!", 
					"Error", JOptionPane.ERROR_MESSAGE);
		}	
	}

	//  native method declarations
	private static native int JHelloWorld (String fileName, double[] v);
	private native int JPrintClass ();
	private static native int JStart (String inputFileName, String logFileName, 
			String histogramCwd, String snapCwd);
	private native int JFd3dStart (String inputFileName, String logFileName, 
			String histogramCwd, String snapCwd);

	// Load shared libraries which contains implementation of native methods
//	static 	{
//		System.loadLibrary("org_ucdm_fd3d");
//		//System.load("C:\\prowess\\vs2012\\fd\\x64\\Debug\\org_ucdm_fd3d.dll");
//	}
}
