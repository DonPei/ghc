package com.geohammer.vc.run;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;

import javax.swing.JOptionPane;
import javax.swing.JProgressBar;

//import org.apache.log4j.Logger;
//import org.ucdm.mt.SolvePseudoInverseSvd;
import com.geohammer.rt.pseudo3d.RayTracerFlat3D;
import com.geohammer.rt.pseudo3d.RayTracerI;
import com.geohammer.core.acquisition.VCPair;
import com.geohammer.core.planarmodel.FlatLayer1D;
import com.geohammer.core.planarmodel.Layer2D;
import com.geohammer.core.planarmodel.SingleFlatLayer;

import edu.mines.jtk.lapack.DMatrix;
import edu.mines.jtk.util.ArrayMath;

	public class StrippingAlone {

	RayTracerFlat3D _rayTracer 	= null;
	VCPair 			_azPW 		= null;

	String 		_cwd 		= null;
	double [][] _min 		= null;
	double [][] _max 		= null;
	String [] 	_tabNames 	= null;
	int [] 		_tabIndex 	= null;

	boolean 	_cancel 	= false;

	//private JProgressBar _progBar	= null;
	private double [][] _rmsArray	= null;
	
	private SaWorkerI 	_worker 	= null;
	private SaToken 	_token 		= null;
	
	private int []		_kLayers 	= null;
	private int [] 		_index 		= null;
	double [][] 		_steps 		= null;
	
	private boolean 	_isVsZero 		= false;
	private boolean 	_isObsSTExist 	= true;

	public StrippingAlone(RayTracerFlat3D rayTracer, int iRMS, 
			int [] kLayers, double [][] steps, int [] index, String cwd, SaWorkerI worker) {
		_cancel 	= false;
		_kLayers 	= kLayers;
		_steps 		= steps;
		_index 		= index;
		
		_cwd 		= cwd;
		_worker 	= worker;
		
		_rayTracer = rayTracer;
		FlatLayer1D layer1D = rayTracer.getFlatLayer1D().copy();
		//System.out.println(layer1D.toString());
		_isVsZero = layer1D.isVsZero();
		VCPair vcPW = rayTracer.getVCPair();
		//System.out.println(vcPW.toString(false));
		_isObsSTExist = vcPW.isObsSTExist();

		_rmsArray 	= new double[steps[0].length][steps[1].length];
		for(int i=0; i<_rmsArray.length; i++) {
			for(int j=0; j<_rmsArray[i].length; j++) _rmsArray[i][j] = 3.0;
		}

		_token 	= new SaToken(_rayTracer, _rmsArray);
	}


	protected void write() {
//		File file = new File(_cwd);
//		if (!file.exists()) {
//			if (file.mkdir()) {
//				System.out.println(_cwd+" directory is created!");
//			} 
//		}
//		String localName = "sa1Aniso";
//		
//		//String fileName = _cwd+File.separator+"run_"+_iRun+"_sa_"+_iType;
//		String fileName = _cwd+File.separator+localName;
//		//System.out.println(fileName);
//		if(_iType==0||_iType==1||_iType==2) {			
//			write(0, fileName+"_result.txt");
//			writeMatrix(fileName+"_rms.txt", _rmsArray);
//			//WayzettaTest wayzettaTest = new WayzettaTest(2, 1, _rayTracer[0].getFlatLayer1D().copy(), _rayTracer[0].getVCPair());
//		} else {
//			write(1, fileName+"_result.txt");
//			writeMatrix(fileName+"_rms.txt", _rmsArray);
//		}
	}
	public int start() {
		
		int m = _steps[0].length;
		int n = _steps[1].length;
		
		for(int i=0; i<m; i++) {
			for(int k=0; k<_kLayers.length; k++)
				_rayTracer.getFlatLayer1D().getLayer(_kLayers[k]).setLayerProperty(_index[0], _steps[0][i]);
			for(int j=0; j<n; j++) {
				for(int k=0; k<_kLayers.length; k++)
					_rayTracer.getFlatLayer1D().getLayer(_kLayers[k]).setLayerProperty(_index[1], _steps[1][j]);
				
				rayTracing();
				double currRms 	= calRms();
				_rmsArray[i][j] = 1000*currRms;
			}
			if(_worker!=null) _worker.update(_token);
			//System.out.println(i+" "+_rmsArray[i][0]);
		}
		//write();
		return 1; 
	}

	public SaToken getToken() { return _token; }

	public void rayTracing() {
		if(_rayTracer==null) return;

		_rayTracer.setIVp(1); _rayTracer.setConfiguration(); _rayTracer.start();
		if(!_isVsZero && _isObsSTExist) {
			_rayTracer.setIVp(2); _rayTracer.setConfiguration(); _rayTracer.start();
			_rayTracer.setIVp(3); _rayTracer.setConfiguration(); _rayTracer.start();
		}
		//_rayTracer.printCal();
	}
	public double calRms() {
		VCPair vcPW 	= _rayTracer.getVCPair();
		//vcPW.applyT0(1, 0, 0);
		//System.out.println(vcPW.toString());
		if(!_isVsZero && _isObsSTExist) {
			return vcPW.getRMS(1, 1, 0);
		} else {
			return vcPW.getRMS(1, 0, 0);
		}
	}
	
	public void printArray(int start, int step, double [] X, String label) {
		System.out.println(label+" ");
		for(int i=start; i<X.length; i+=step) {
			System.out.println((i+1)+" "+X[i]+" ");
		}
		System.out.println();
	}
	public void printMatrix(double [][] M, String label) {
		System.out.println(label+" M="+M.length+" M[0]="+M[0].length);
		for(int i=0; i<M.length; i++) {
			System.out.println(i+" "+Arrays.toString(M[i]));
		}
	}
	
	public double [][] getRmsArray() 				{ return _rmsArray; }
	public RayTracerI getRayTracer() 				{ return _rayTracer; }
	public String getCwd() 							{ return _cwd; }

	public void setCwd(String cwd) 					{ _cwd = cwd; }
	public void setCancel(boolean cancel) 			{ _cancel = cancel; }
	//public void setProgressBar(JProgressBar progBar){ _progBar	= progBar; }

	
	public void writeMatrix(String outputFileName, double [][] v) {
		try{
			BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(outputFileName, false));
			bufferedWriter.write("Temperature Rms(s) ");
			bufferedWriter.newLine();
			for(int j=0; j<v[0].length; j++) { 
				bufferedWriter.write(v[0][j]+" "+v[1][j]);
				bufferedWriter.newLine();
			}

			bufferedWriter.close();
		} catch (IOException ioexception) {
			JOptionPane.showMessageDialog(null, "\nFile Format is not right!", 
					"Error", JOptionPane.ERROR_MESSAGE);
		}
	}
}
