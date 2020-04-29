package com.geohammer.core.stress;

import com.geohammer.core.FractureModel;

public class TiltFractureModel extends FractureModel {
	//Whole space
	public double 		_youngsModulus		= 4.0e6;
	public double 		_poissonsRatio		= 0.25;
	
	//Okada model parameters
	public double 		_fLength 			= 12.0;
	public double 		_fWidth 			= 8.0;
	public double 		_fDepth 			= 10.0;
	public double 		_fSlip 			 	= 0.0005;
	public double 		_fDip 				= 40.0; 
	public double 		_fStrike 			= 90.0; 
	public int 			_faultType 			= 0; 		// Frac type is 0 for tensile, 1 for strikeslip, and 2 for dipslip
	
	public int 			_outputType 		= 0; 		// 0 Okada tilt, 1 for Okada displacement
	
	//3D flat elliptic crack
	public double 		_fa					= 500.0; 	//ft
	public double 		_fb    				= 50.0;	 	//ft
	public double 		_fPressure			= 1000.0; 	//psi
	
	//2D crack
	public double 		_fc2D				= 200.0; 	//ft
	public double 		_fPressure2D		= 800.0; 	//psi
	
	//[type, P1Northing, P2Easting, P1TVD, P2Northing, P2Easting, P2TVD, #ofSampleNorthing, #ofSampleEasting, #ofSampleTVD] 
	public double [] 	_data 			= null;
		
	public TiltFractureModel() {
		//this(0, "Okada", 0.0, 0.0, 0.0, 4.0e6, 0.25, 100.0, 50.0, 6500.0, -0.001, 0.0, 89.99, 0, 
		//		new double[] {0,      0.0, 200.0, 6000.0, 0.0, 200.0, 7000.0, 0, 0, 301}); //synthetic
		//this(0, "Okada", 0.0, 0.0, 0.0, 4.0e6, 0.25, 20.0, 10.0, 30.0, -0.001, 90.0, 45, 0, 
		//		new double[] {2,      -50.0, -50.0, 0.0, 50.0, 50.0, 50.0, 51, 51, 2}); //Figure 7 configure
		//this(0, "Okada", 0.0, 0.0, 0.0, 4.0e6, 0.25, 20.0, 10.0, 30.0, -0.001, 90.0, 89.99, 0, 
		//		new double[] {2,      -50.0, -50.0, 0.0, 50.0, 50.0, 50.0, 101, 101, 2}); //Figure 7 configure
		//this(0, "Okada", 0.0, 0.0, 0.0, 4.0e6, 0.25, 12.0, 8.0, 10.0, 0.0005, 90.0, 40.0, 0, 
		//		new double[] {0,      25.0, 15.0, 0.0, 25.0, 15.0, 20.0, 0, 0, 101}); //Figure 8 configure
		//Figure 12 Warpinski: analytic crack solutions for tilt fields
		//this(1, "Elliptic Crack", 0.0, 0.0, 0.0, 27.6e3, 0.2,  
		//		new double[] {2,      0.0, -152.0, -150.0, 150.0, 601}); 
		//C:\arima\Warpinski Files\Codes\OldFortranCodes\Ellsig\Debug
		this(1, "Elliptic Crack", 0.0, 0.0, 0.0, 6.0e6, 0.25,  
				new double[] {0,      0.0, 0.0, 0.0, -100.0, 0.0, 0.0, 0, 11, 0}); 
		//C:\arima\Warpinski Files\Codes\OldFortranCodes\Elltilt\Debug
		//this(1, "Elliptic Crack", 0.0, 0.0, 0.0, 6.0e6, 0.25,  
		//		new double[] {0,      -200.0, 250.0, -200.0, -200.0, 250.0, 200.0, 0, 0, 11});
	}
	public TiltFractureModel(int id) {
		super(0, "Okada", 0.0, 0.0, 0.0);
		if(id==0) {
			SelfConstruction(4.0e6, 0.25, 100.0, 50.0, 6500.0, -0.001, 0.0, 89.99, 0, 
				new double[] {id,      0.0, 200.0, 6000.0, 0.0, 200.0, 7000.0, 0, 0, 301}); //synthetic downhole
			_outputType = 0;
		} else {
			SelfConstruction(4.0e6, 0.25, 100.0, 50.0, 1500.0, -0.001, 0.0, 89.99, 0, 
					new double[] {id,      -3000.0, -3000.0, 0.0, 3000.0, 3000.0, 2000.0, 100, 100, 2}); //synthetic surface
			_outputType = 1;
		}
	}
	public TiltFractureModel(int id, String label, double x, double y, double z, double youngsModulus, double poissonsRatio, double [] receiverGeometry) {
		this(id, label, x, y, z, youngsModulus, poissonsRatio, 
				100.0, 50.0, 3000.0, 1.0, 90.0, 90.0, 0, receiverGeometry);	
	}
	public TiltFractureModel(int id, String label, double x, double y, double z, double youngsModulus, double poissonsRatio, 
			double fLength, double fWidth, double fDepth, double fSlip, double fStrike, double fDip, int faultType, double [] receiverGeometry) {
		super(id, label, x, y, z);
		SelfConstruction(youngsModulus, poissonsRatio, fLength, fWidth, fDepth, 
				fSlip, fStrike, fDip, faultType, receiverGeometry);
	}
	public void SelfConstruction(double youngsModulus, double poissonsRatio, double fLength, double fWidth, double fDepth, 
			double fSlip, double fStrike, double fDip, int faultType, double [] receiverGeometry) {
		
		_youngsModulus		= youngsModulus;
		_poissonsRatio		= poissonsRatio;
		_fLength 			= fLength;
		_fWidth 			= fWidth;
		_fDepth 			= fDepth;
		_fSlip 				= fSlip;
		_fStrike 			= fStrike;
		_fDip 				= fDip;
		_faultType 			= faultType;
		_data 				= receiverGeometry;
		if(_data==null) _data 	= new double[] {0, 15.0, 25.0, 0.0, 15.0, 25.0, 2000.0, 0, 0, 2000};
	}
	

	public double [] getData() 								{ return _data; }
	public double getData(int index) 						{ return _data[index]; }
	
	public double getYoungsModulus() 						{ return _youngsModulus; }
	public double getPoissonsRatio() 						{ return _poissonsRatio; }
	public double getAlpha() 								{ return 0.5/(1.0-_poissonsRatio); }
	public double getFLength() 								{ return _fLength; }
	public double getFWidth() 								{ return _fWidth; }
	public double getFDepth() 								{ return _fDepth; }
	public double getFStrike() 								{ return _fStrike; }
	public double getFDip() 								{ return _fDip; }
	public double getFSlip() 								{ return _fSlip; }
	public int getFaultType() 								{ return _faultType; }
	public int getOutputType() 								{ return _outputType; }
	
	public double getFa() 									{ return _fa; }
	public double getFb() 									{ return _fb; }
	public double getFPressure() 							{ return _fPressure; }
	
	public double getFc2D() 								{ return _fc2D; }
	public double getFPressure2D() 							{ return _fPressure2D; }
	
	public void setYoungsModulus(double youngsModulus) 		{ _youngsModulus = youngsModulus; }
	public void setPoissonsRatio(double poissonsRatio) 		{ _poissonsRatio = poissonsRatio; }
	

	public void setData(double [] data) 					{ _data = data; }
	public void setData(int index, double v) 				{ _data[index] = v; }
	
	public void setFLength(double fLength) 					{ _fLength = fLength; }
	public void setFWidth(double fWidth) 					{ _fWidth = fWidth; }
	public void setFDepth(double fDepth) 					{ _fDepth = fDepth; }
	public void setFStrike(double fStrike) 					{ _fStrike = fStrike; }
	public void setFDip(double fDip) 						{ _fDip = fDip; }
	public void setFSlip(double fSlip) 						{ _fSlip = fSlip; }
	public void setFaultType(int faultType) 				{ _faultType = faultType; }
	public void setOutputType(int outputType) 				{ _outputType = outputType; }
	
	public void setFa(double fa)							{ _fa 	= fa; }
	public void setFb(double fb) 							{ _fb 	= fb; }
	public void setFPressure(double fPressure) 				{ _fPressure 	= fPressure; }

	public void setFc2D(double fc2D) 						{ _fc2D 	= fc2D; }
	public void setFPressure2D(double fPressure2D) 			{ _fPressure2D 	= fPressure2D; }

}
