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

import edu.mines.jtk.lapack.DMatrix;
import edu.mines.jtk.util.ArrayMath;

///public class SaWorker extends SwingWorker<Integer, Integer>{
	public class SaAlone {

	//static final Logger logger = Logger.getLogger(SaWorker.class);
	//FlatLayer1D 	_layer1D 		= null;
	RayTracerFlat3D _rayTracer 	= null;

	String 		_cwd 		= null;
	double [][] _min 		= null;
	double [][] _max 		= null;
	int [][]	_linkTo 	= null;
	String [] 	_tabNames 	= null;
	int []		_tabIndex 	= null;

	boolean 	_cancel 	= false;

	//private JProgressBar _progBar	= null;
	private double [][] _rmsArray	= null;

	// these are inversion variables
	int 		_saLength;			// total iteration for annealing process.
	int 		_eqLength;			// total iteration for Markov Chain.
	int 		_sampleLength;		// total iteration for error estimating.
	int			_coolingSchedule; 	//control to cool temperature from the following list
									// 1-"2", 2-"FSA", 3-"FSA2", 4-"VFSA".
	double 		_noise;				// noise level.
	double 		_tol;				// tolorance.
	double 		_c;					// free parameter for simulated annealing
	double 		_T0;				// initial tempreture for simulated annealing

	double 		_c1;				// free parameter for simulated annealing
	double 		_c2;				// free parameter for simulated annealing
	double 		_c3;				// free parameter for simulated annealing
	
	// random number generator parameters
	SaRandom 	_random 			= null;
	int 		_randomGenerator 	= 2; 	// 1-Numerical Receipt method; 2-java.
	int 		_rnDistribution 	= 5; 	// probability distribution from the following list:
										 	// 1-"random", 2-"Uniform", 3-"Gaussian", 4-"Cauchy", and 5-"VFSA".
	int 		_rnMethod 			= 1;	// for "Gaussian" only.
	double 		_rnMu 				= 0;	// expectation.
	double 		_rnSigma 			= 1.0;	// standard deviation.
	double 		_rnLamda 			= 0.0;	// for gamma distribution only.
	double 		_rnTemp 			= 0.0;	// reserved.

	private double  _minRms 		= 10; 
	private double  _initRms 		= 10; 
	private double [][]  _initX 	= null;
	private double [][]  _minX 		= null;
	private double  _wAz 			= 0.1; 	// relative weight of the back azimuth compared with travel times

	//private int 	_iT0			= 0;
	private int 	_iType			= 1; 	
	private int 	_iIte 			= 0;
	private int 	_nVariables		= 2;
	private int 	_iRun			= 1;
	
	SaWorkerI 		_worker 		= null;
	SaToken 		_token 		= null;

	private int 	_iMethod 		= 0; //=0 SA ==1 LMA

	public SaAlone(int iMethod, int iRun, RayTracerFlat3D rayTracer, 
			int iRMS, FlatLayer1D minLayer, FlatLayer1D maxLayer, FlatLayer1D linkToLayer, 
			int saLength, int eqLength, double T0, double tol, double noise, double c, 	int coolingSchedule, 
			int iType, String cwd, SaWorkerI worker) {
		_cancel 	= false;
		_iMethod 	= iMethod;
		
		_iRun 		= iRun;
		_cwd 		= cwd;
		_worker 	= worker;
		
		_rayTracer = rayTracer;
		FlatLayer1D layer1D = rayTracer.getFlatLayer1D();
		_tabNames = new String[]{"Vp", "Vp2Vs", "Delta", "Epsilon", "Gamma"};
		_tabIndex = new int[]{1, 3, 21, 20, 22};
		_min = new double[_tabNames.length][layer1D.getNumOfBoundaries()];
		_max = new double[_tabNames.length][layer1D.getNumOfBoundaries()];
		_linkTo = new int[_tabNames.length][layer1D.getNumOfBoundaries()];
		
		for(int i=0; i<_min.length; i++) {
			for(int j=0; j<_min[i].length; j++) {
				_min[i][j] = minLayer.getLayer(j).getLayerProperty(_tabIndex[i]);
				_max[i][j] = maxLayer.getLayer(j).getLayerProperty(_tabIndex[i]);
				_linkTo[i][j] = (int)linkToLayer.getLayer(j).getLayerProperty(_tabIndex[i])-1;
			}
		}
		//printMatrix(_min, "min");
		//printMatrix(_max, "max");
		//printMatrix(_linkTo, "linkTo");

		_saLength 	= saLength;
		_eqLength 	= eqLength;
		_T0			= T0;
		_tol		= tol;
		_noise		= noise;
		_c			= c;
		_coolingSchedule = coolingSchedule;
		_iType		= iType;
		_nVariables	= getNumOfVariables();
		_c1			= 1.0e-1*c;
		_c2			= 0.5*_c1;
		_c3			= 0.5*_c2;

		//		_c1			= c;
		//		_c2			= c;
		//		_c3			= c;

		if(_coolingSchedule==5) 		{ _rnDistribution	= 5; } 
		else if (_coolingSchedule==2) 	{ _rnDistribution	= 2; } 
		else 							{ _rnDistribution	= 5; }

		_rmsArray 	= new double[2][_saLength+1];
		for(int i=0; i<_rmsArray.length; i++) {
			for(int j=0; j<_rmsArray[0].length; j++) _rmsArray[i][j] = -99999.0;
		}		
		_random 	= new SaRandom(_randomGenerator);

		_token 	= new SaToken(_rayTracer, _rmsArray);
		
	}

	protected void write() {
		File file = new File(_cwd);
		if (!file.exists()) {
			if (file.mkdir()) {
				System.out.println(_cwd+" directory is created!");
			} 
		}
		String localName = null;
		if(_iType==0) localName = "sa1Aniso";
		else if(_iType==1) localName = "sa0Vel";
		else if(_iType==2) localName = "sa2Full";
		else {
			if(_iRun==10) localName = "sa1AnisoLoc";
			else localName = "sa0VelLoc";
		}
		
		//String fileName = _cwd+File.separator+"run_"+_iRun+"_sa_"+_iType;
		String fileName = _cwd+File.separator+localName;
		//System.out.println(fileName);		
		write(0, fileName+"_result.txt");
		writeMatrix(fileName+"_rms.txt", _rmsArray);
		//WayzettaTest wayzettaTest = new WayzettaTest(2, 1, _rayTracer[0].getFlatLayer1D().copy(), _rayTracer[0].getVCPair());
	}
	public int start() {
		if(_iMethod==0) return startSA();
		else if(_iMethod==2) return startLMA();
		else return -1;
	}
	public int startLMA() { return 1; }
	
//	@Override
//	protected Integer doInBackground() throws InterruptedException {
	public int startSA() {
		System.out.println(paramToString()+"\n");
		double [][] v = getModelVariables();
		_initX = ArrayMath.copy(v);
		//_initXYZ = getModelVariables(3);
		//printMatrix(v, "v");
		double [][] X0 = ArrayMath.copy(v);
		rayTracing();
		//double [] D 	= getDiffTT(-1, 1);
		double currRms 	= calRms();
		_iIte = 0;
		_rmsArray[0][_iIte] = _T0;
		_rmsArray[1][_iIte++] = currRms;
		_initRms = currRms;
		System.out.println( "initial currRms=" +1000*currRms);
		//System.out.println( _layer1D.toString());

		if( Math.abs(currRms) <= _tol ) {
			System.out.println(String.format("\nInitial model is accepted with error = %f (ms)\n", 1000*currRms));
		} else {
			System.out.println(String.format("\nHeating Up. Wait ..."));
			System.out.println(String.format("\nStarting Temperature is %f ", _T0)); 

			if( _T0>0.0 ) {
				//System.out.println(String.format("\nAnnealing with RMS=%f ", 1000*currRms)); 
				_minX = annealing(_saLength, _eqLength, _T0, _coolingSchedule, v, X0);

				updateModel(_minX);
				rayTracing();
				currRms = calRms();
				System.out.println( "final currRms=" +1000*currRms);
				_minRms = currRms;
				//System.out.println( _layer1D.toString());
			}
		}
		write();
		
		return 1;
	}

	private double [][] annealing(int SAlength, int EQlength, double T0, 
			int coolingSchedule, double [][] v, double [][] X0) {

		double currRms, prevRms, minRms, deltaE, temperature, minTemp;
		double AcceptanceRatio=0, probRandom, probC, tscale=0.1;

		double [][] minX = new double[v.length][v[0].length];
		ArrayMath.copy(X0, minX);

		temperature = T0;
		minTemp		= 1.0e-20;
		rayTracing();
		prevRms 	= calRms();
		minRms 		= prevRms;

		int i0=0, i1=0, i2=0, i3=0;
		for(int i=0; i<SAlength; i++) {
			//if(_progBar!=null) _progBar.setValue((int)(100*i/SAlength));
			if(_cancel) break;
			i0++;

			//change temperature
			if( coolingSchedule==2 )  { //BSA
				temperature *= 0.95; //temperature = T0 / log( i );
			} else if( coolingSchedule==3 )  { //FSA
				temperature = (T0)/(1.0+i*tscale);
			} else if( coolingSchedule==4 ) { //FSA2
				temperature *= (T0)/(1.0+i*tscale);
			} else if( coolingSchedule==5 ) { //VFSA
				double c = _c;
				if(temperature<1.0e-2) c = _c1;
				else if(temperature<1.0e-6) c = _c2;
				else if(temperature<1.0e-9) c = _c3;
				temperature *= Math.exp(-c*Math.pow(i,1.0/_nVariables));
				//temperature *= Math.exp(-c*Math.pow(i,1.0/getNumOfVariables()));
				//temperature = T0 / Math.log( i );
			} else { }
			_rnTemp = temperature;

			AcceptanceRatio = equilibrate(EQlength, temperature, v, X0, prevRms);
			if(AcceptanceRatio>=0) {
				perturbModel(X0, v);
				updateModel(v);
			}
			rayTracing();
			currRms 		= calRms();
			if(i%100==0) {
				//VCPair vcPW 	= _rayTracer.getVCPair();
				//System.out.println(i+" "+temperature+" "+1000*currRms+" "+vcPW.getCalPT(13));
				//System.out.println(Arrays.toString(v[2]));
				if(_worker!=null) _worker.update(_token);
			}
			//System.out.println(i+" "+temperature+" "+1000*currRms+" "+AcceptanceRatio);
			//logger.debug(i+" "+temperature+" "+1000*currRms+" "+AcceptanceRatio);
			_rmsArray[0][_iIte] = (float)temperature;
			_rmsArray[1][_iIte++] = (float)currRms;

			//			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
			//			try {
			//				String line = bufferedReader.readLine();
			//				System.out.println(line);
			//			} catch (IOException e) { e.printStackTrace(); }

			if( currRms < _tol ) {
				ArrayMath.copy(v, minX);
				minRms 	= currRms;
				i 		= SAlength+10;
			}
			if( temperature < minTemp )	i=SAlength+20;

			deltaE = currRms - prevRms;

			//if ( fabs(deltaE) <= bTol )	k++;
			//else							k=0;
			//if(k>=5)						i = SAlength+4;

			if ( deltaE <= 0.0 ) {
				prevRms = currRms;
				i1++;
				ArrayMath.copy(v, X0);

				if(currRms <= minRms ) { minRms = currRms; ArrayMath.copy(v, minX); }
			} else {
				probRandom = getUniformRN();
				probC = Math.exp( -deltaE / temperature );
				if ( probC > probRandom ) {
					prevRms = currRms;
					i2++;
					ArrayMath.copy(v, X0);
				}
				else { i3++; }
			}
		}

		//convergence criterion
		// 1) The objective function < tol;
		// 2) The number of SA has exceed user-specified SALength;
		// 3) The proportion of accepted perturbation is smaller than a threshold;
		// 4) The error has not been changed in a continuous run of SA;
		// 5) temperature is lower than a threshold;

		ArrayMath.copy(minX, X0);
		//System.out.println(i0+" "+i1+" "+i2+" "+i3);
		//System.out.println( "\nminXL2: " +minRms+" minX: \n" +toString(v));
		//logger.debug(" ");		logger.debug("#D2 Annealing Statistics:");  logger.debug(i0+" "+i1+" "+i2+" "+i3);
		//logger.debug("\nminXL2: " +minRms+" minX: \n" +toString(v));
		//for(j=0;j<q2;j++) X0[j] = minX[j];
		//		printf("\nT=%20.17lf SA=%d EQ=%d i=%d i0=%d i1=%d i2=%d i3=%d EPS=%e minTemp=%e \n",
		//				temperature, SAlength, EQlength, i, i0, i1, i2, i3, _EPS, minTemp );
		//printf("\nfinalT=%e tol=%lf minRms=%lf", temperature, tol, minRms);

		//		*T0 = temperature;
		//		delete []minX;

		return minX;
	}

	private double equilibrate(int EQlength, double temperature, double [][]v, double [][]X0, double prevRms) {
		if(EQlength==0) return 0;
		double currRms, deltaE;
		double probRandom, probC;
		double AcceptanceRatio 	= 0.0;

		//prevRms 				= calError();

		int i=0, i0=0, i1=0, i2=0, i3=0;
		for(i=0; i<EQlength; i++)	{
			i0++;
			perturbModel(X0, v);
			updateModel(v);
			rayTracing();
			currRms 		= calRms();

			if(currRms<_tol) { ArrayMath.copy(v, X0); i = EQlength+2;
			} else {
				deltaE = currRms - prevRms;
				if ( deltaE <= 0.0 ) {
					prevRms = currRms;
					ArrayMath.copy(v, X0);
					i1++;
				} else {
					probRandom = getUniformRN();
					probC = Math.exp( -deltaE / temperature );
					if ( probC >= probRandom ) {
						prevRms = currRms;
						ArrayMath.copy(v, X0);
						i2++;
					} else {
						i3++;
					}
				}
			}
		}

		if(i==EQlength+3) {
			AcceptanceRatio = -1.0;
		} else {
			AcceptanceRatio = (i1+i2)/EQlength ;
		}

		return AcceptanceRatio;
	}
	
	public SaToken getToken() { return _token; }

	private double getRN() { return _random.rn(_rnDistribution, _rnMethod, _rnMu, _rnSigma, _rnLamda, _rnTemp); }
	private double getUniformRN() { return _random.rn(1, _rnMethod, _rnMu, _rnSigma, _rnLamda, _rnTemp); }

	private double [] copyModelVariables(double [] minX){
		double [][] v = getModelVariables();
		double [] minX1 = null;
		if(minX==null) minX1 = new double[v.length*v[0].length];
		else minX1 = minX;
		for(int i=0, k=0; i<v[0].length; i++) {
			for(int j=0; j<v.length; j++, k++) minX1[k] = v[j][i];
		}
		return minX1;
	}
	private double [][] getModelVariables(){
		FlatLayer1D layer1D 	= _rayTracer.getFlatLayer1D();
		double [][] X = new double [_tabNames.length][layer1D.getNumOfBoundaries()];
		for(int i=0; i<X.length; i++) {
			for(int j=0; j<X[i].length; j++) {
				X[i][j] = layer1D.getLayer(j).getLayerProperty(_tabIndex[i]);
			}
		}
		return X;
	}

	private void updateModel(double [][] X){
		if(_rayTracer==null) return;
		
			FlatLayer1D layer1D 	= _rayTracer.getFlatLayer1D();
			for(int i=0; i<_tabNames.length; i++) {
				for(int j=0; j<_linkTo[i].length; j++) {
				//for(int j=0; j<layer1D.getNumOfBoundaries(); j++) {
					//int k = j;
					
					//System.out.println("i="+i+" j="+j+" x="+X[i][j]+" min="+_min[i][j]+" max="+_max[i][j]);
					if(X[i][j]>_min[i][j]&&X[i][j]<_max[i][j]) {
						int k = _linkTo[i][j];
						layer1D.getLayer(k).setLayerProperty(_tabIndex[i], X[i][k]);
						if(_tabIndex[i]==3) {
							double vs = layer1D.getLayer(k).getVp()/layer1D.getLayer(k).getVpToVs();
							layer1D.getLayer(k).setLayerProperty(2, vs);
							//System.out.println("i="+i+" j="+j+" x="+X[i][j]+" min="+_min[i][j]+" max="+_max[i][j]+" vp="+layer1D.getLayer(0).getVp());
						}
					}
				}
			}

	}
	private int getNumOfVariables() {
		FlatLayer1D layer1D 	= _rayTracer.getFlatLayer1D();
		int k=0;
		for(int i=0; i<_tabNames.length; i++) {
			for(int j=0; j<layer1D.getNumOfBoundaries(); j++) {
				if(_max[i][j]>_min[i][j]) {k++;}
			}
		}
		return k;
	}
	private void perturbModel(double [][] X0, double [][] v) {
		int k=0, maxLoop = 100;
		double a=0, dv=0;
		for(int i=0; i<_tabNames.length; i++) {
			for(int j=0; j<_min[i].length; j++) {
				if(_max[i][j]>_min[i][j]) {
					dv = _max[i][j]-_min[i][j];
					k = 0;
					while (k<maxLoop) {
						a = getRN();
						a = X0[i][j]+a*dv;
						k++;
						if (a>=_min[i][j] && a<=_max[i][j]) k = maxLoop+10;
					}
					if(k==maxLoop+10) { v[i][j]=a;  }//System.out.print(a+" ");}
				}
			}
		}
	}

	public void rayTracing() {
		if(_rayTracer==null) return;

		//FlatLayer1D layer1D 	= _rayTracer.getFlatLayer1D();
		//System.out.println(layer1D.toString());
		_rayTracer.setIVp(1); _rayTracer.setConfiguration(); _rayTracer.start();
		if(_iType<10) {
			_rayTracer.setIVp(2); _rayTracer.setConfiguration(); _rayTracer.start();
		}
		//_rayTracer.setIVp(3); _rayTracer.setConfiguration(); _rayTracer.start();
		//_rayTracer.printCal();
	}
	public double calRms1() {
		RayTracerFlat3D rayTracer = _rayTracer;
		VCPair vcPW 	= rayTracer.getVCPair();
		vcPW.applyT0(1, 1, 0);
		//System.out.println(vcPW.toString());
		return vcPW.getRMS(1, 1, 0);
//		if(_rayTracer.length==1) {
//			return vcPW.getRMS(1, 0, 0);
//		} else if(_rayTracer.length==2) {
//			return vcPW.getRMS(1, 1, 0);
//		} else if(_rayTracer.length==3) {
//			return vcPW.getRMS(1, 1, 1);
//		} else return 0;
	}
	public double calRms() {
		RayTracerFlat3D rayTracer = _rayTracer;
		VCPair vcPW 	= rayTracer.getVCPair();
		//vcPW.applyT0(1, 0, 0);
		//System.out.println(vcPW.toString());
		if(_iType<10) {
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
	public void printMatrix(int [][] M, String label) {
		System.out.println(label+" M="+M.length+" M[0]="+M[0].length);
		for(int i=0; i<M.length; i++) {
			System.out.println(i+" "+Arrays.toString(M[i]));
		}
	}
	public double [][] calBackAzimuth(int nCol, int eventStartIndex, VCPair azPW) {
		if(azPW==null) return null;
		int k = 0;
		for(int i=0; i<azPW.getNEvents(); i++) { k += azPW.getFlag(i); }
		double [][] B = new double[k][nCol];
		for(int i=0; i<B.length; i++) {
			for(int j=0; j<B[i].length; j++) B[i][j] = 0;
		}

		double diffX = 0;
		double diffY = 0;
		double d = 0;
		k = eventStartIndex;
		for(int i=0, jj=0; i<azPW.getNEvents(); i++) {
			k = eventStartIndex+i*4;
			for(int j=0; j<azPW.getFlag(i); j++, jj++) {
				diffX = azPW.getEE(i)-azPW.getRE(jj);
				diffY = azPW.getEN(i)-azPW.getRN(jj);
				d = diffX*diffX+diffY*diffY;

				B[jj][k+0] = -diffY/d; 			//derivative in X
				B[jj][k+1] = diffX/d; 			//derivative in Y
				B[jj][k+2] = 0.0; 				//derivative in Z
				B[jj][k+3] = 0.0; 				//derivative wrt origin time

				azPW.setData(jj, azPW.getCalPT(), Math.atan2(diffY, diffX));
			}
		}

		return B;
	}

	public double [][] getRmsArray() 				{ return _rmsArray; }
	public RayTracerI getRayTracer() 				{ return _rayTracer; }
	public String getCwd() 							{ return _cwd; }

	public void setCwd(String cwd) 					{ _cwd = cwd; }
	public void setCancel(boolean cancel) 			{ _cancel = cancel; }
	//public void setProgressBar(JProgressBar progBar){ _progBar	= progBar; }

	public String paramToString() {
		String separator = System.getProperty( "line.separator" );
		StringBuilder lines = new StringBuilder("SA inversion parameters:");
		lines.append( separator );
		lines.append(String.format( "SAlength=%d EQlength=%d sampleLength=%d T0=%f tol=%f c=%f cooling=%d iType=%d", 
				_saLength, _eqLength, _sampleLength, _T0, _tol, _c, _coolingSchedule, _iType));
		lines.append( separator );
		lines.append("SA random number generator parameters:");
		lines.append( separator );
		lines.append(String.format("randomGenerator=%d rnDistribution=%d rnMethod=%d rnMu=%f rnSigma=%f rnLamda=%f rnTemp=%f", 
				_randomGenerator, _rnDistribution, _rnMethod, _rnMu, _rnSigma, _rnLamda, _rnTemp));
		lines.append( separator );
		
		return lines.toString( );
	}

	public void write(int id, String outputFileName) {
		try{
			BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(outputFileName, false));
			bufferedWriter.write(paramToString());
			bufferedWriter.newLine();
			if(id==0) {
				bufferedWriter.write("Layer# ");
				for(int i=0; i<_tabNames.length; i++) { bufferedWriter.write(" min_"+_tabNames[i]+" max_"+_tabNames[i]); }
				bufferedWriter.newLine();

				for(int j=0; j<_min[0].length; j++) {
					bufferedWriter.write(j+"");
					for(int i=0; i<_tabNames.length; i++) {
						bufferedWriter.write(" "+ _min[i][j]+" "+_max[i][j]);
					}
					bufferedWriter.newLine();
				}
				bufferedWriter.newLine();

				bufferedWriter.write("Init RMS (ms)="+_initRms*1000); bufferedWriter.newLine();
				bufferedWriter.write("Layer# ");
				for(int i=0; i<_tabNames.length; i++) { bufferedWriter.write(" inverted_"+_tabNames[i]); }
				bufferedWriter.newLine();
				if(_initX!=null) {
					for(int j=0; j<_initX[0].length; j++) {
						bufferedWriter.write(j+"");
						for(int i=0; i<_tabNames.length; i++) {
							bufferedWriter.write(" "+ _initX[i][j]);
						}
						bufferedWriter.newLine();
					}
				}

				bufferedWriter.newLine();
				bufferedWriter.write("Min RMS (ms)="+_minRms*1000); bufferedWriter.newLine();
				bufferedWriter.write("Layer# ");
				for(int i=0; i<_tabNames.length; i++) { bufferedWriter.write(" inverted_"+_tabNames[i]); }
				bufferedWriter.newLine();
				if(_minX!=null) {
					for(int j=0; j<_minX[0].length; j++) {
						bufferedWriter.write(j+"");
						for(int i=0; i<_tabNames.length; i++) {
							bufferedWriter.write(" "+ _minX[i][j]);
						}
						bufferedWriter.newLine();
					}
				}
				bufferedWriter.newLine();
			} else if(id==1) {
				VCPair vcPW 	= _rayTracer.getVCPair();
				bufferedWriter.newLine();
				bufferedWriter.write("Init RMS (ms)="+_initRms*1000); bufferedWriter.newLine();
				if(_initX!=null) {
					bufferedWriter.write("Init RMS Event#: "); 
					bufferedWriter.write(" Easting"+" Northing"+" Depth"+ " SurfaceDistance"+" Depth"+" Null"+" T0"); 
					bufferedWriter.newLine();
					
					for(int j=0; j<_initX.length; j++) {
						bufferedWriter.write(j+" ");
						String line1 = " ";
						//String line1 = String.format("%f %f %f ", _initXYZ[j][0], _initXYZ[j][1], _initXYZ[j][2]);
						String line2 = String.format("%f %f %f %f ", _initX[j][0], _initX[j][1], _initX[j][2], _initX[j][3]);
						bufferedWriter.write(line1+line2);
						bufferedWriter.newLine();
					}
				}
				bufferedWriter.write("Min RMS (ms)="+_minRms*1000); bufferedWriter.newLine();
				if(_minX!=null) {
					bufferedWriter.write("Min RMS Event#: "); 
					bufferedWriter.write(" Easting"+" Northing"+" Depth"+ " SurfaceDistance"+" Depth"+" Null"+" T0"); 
					bufferedWriter.newLine();

					for(int j=0; j<_minX.length; j++) {
						bufferedWriter.write(j+" ");
						String line1 = String.format("%f %f %f ", vcPW.getEE(j), vcPW.getEN(j), vcPW.getED(j));
						String line2 = String.format("%f %f %f %f ", _minX[j][0], _minX[j][1], _minX[j][2], _minX[j][3]);
						bufferedWriter.write(line1+line2);
						bufferedWriter.newLine();
					}
				}
				bufferedWriter.newLine();
			}
			
			bufferedWriter.close();
		} catch (IOException ioexception) {
			JOptionPane.showMessageDialog(null, "\nFile Format is not right!", 
					"Error", JOptionPane.ERROR_MESSAGE);
		}
	}

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
