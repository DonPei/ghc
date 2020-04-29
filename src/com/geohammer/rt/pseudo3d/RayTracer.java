package com.geohammer.rt.pseudo3d;

import java.util.Arrays;

import javax.swing.SwingUtilities;

import com.geohammer.core.acquisition.VCPair;
import com.geohammer.core.planarmodel.DipLayer1D;
import com.geohammer.core.planarmodel.FlatLayer1D;
import com.geohammer.core.planarmodel.Layer2D;
import com.geohammer.core.planarmodel.Layer3D;
import com.geohammer.core.planarmodel.Point3D;
import com.geohammer.core.planarmodel.Vecon3D;

public class RayTracer {

	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() { new RayTracer(); }
		});
	}

	public RayTracer() {
		start();
		System.out.println("Successfully Done!");
	}
	public void start() {	
		rayTracing4();
	}
	public void rayTracing0() {
		Layer2D layer2D = new Layer2D(2, "C:\\PINN_DATA\\rt2Test\\calgary\\04Layer.csv");
		FlatLayer1D layer1D = layer2D.toFlatLayer1D(true);
		VCPair vcPW = new VCPair(1, "C:\\PINN_DATA\\rt2Test\\calgary\\pck01.csv");
		System.out.println(vcPW.toDeepString());

		int iVp = 1;
		int iVTI = 1;
		int iApprox = 0;
		int iWave = 1;
		int iR = 1;
		int [] reflectorIndex = null;
		//int [] reflectorIndex = new int[]{4};
		double tol = 0.1;
		double [] rayPar = new double[vcPW.getTotalNumOfReceivers()];
		rayTracing(layer1D, vcPW, iVp, iVTI, iApprox, iWave, iR, reflectorIndex, tol, rayPar);

		//System.out.println(vcPW.toDeepString());
		//System.out.println(Arrays.toString(rayPar));
	}
	
	public void rayTracing1() {
		Layer2D layer2D = new Layer2D(2, "C:\\PINN_DATA\\rt2Test\\calgary\\04Layer.csv");
		FlatLayer1D layer1D = layer2D.toFlatLayer1D(true);
		VCPair vcPW = new VCPair(1, "C:\\PINN_DATA\\rt2Test\\calgary\\pck01.csv");
		//System.out.println(vcPW.toDeepString());

		int iVp = 1;
		int iVTI = 1;
		int iApprox = 0;
		int iWave = 1;
		int iR = 0;
		int [] reflectorIndex = null;
		//int [] reflectorIndex = new int[]{4};
		double tol = 0.1;
		double [] rayPar = new double[vcPW.getTotalNumOfReceivers()];
		
//		RayTracerFlat3D rayTracerP = new RayTracerFlat3D(null, layer1D, vcPW, 2, 1, 
//				0, null, 1, 0.1, 1, 0);
//		rayTracerP.start();
		
		rayTracing(layer1D, vcPW, iVp, iVTI, iApprox, iWave, iR, reflectorIndex, tol, rayPar);

		System.out.println(vcPW.toDeepString());
		System.out.println(Arrays.toString(rayPar));
	}
	
	public void rayTracing2() {
		DipLayer1D dlayer1D = new DipLayer1D("C:\\PINN_DATA\\rt2Test\\calgary\\04Layer3D.csv");
		//System.out.println(dlayer1D.toString());
		FlatLayer1D layer1D = dlayer1D.toFlatLayer1D();
		System.out.println(layer1D.toString());
		VCPair vcPW = new VCPair(1, "C:\\PINN_DATA\\rt2Test\\calgary\\pck01.csv");
		System.out.println(vcPW.toString());
		
		int iForward = 1;
		dlayer1D.transfer(-iForward, vcPW._eN, vcPW._eE, vcPW._eD);
		dlayer1D.rotateNE(iForward, vcPW._eN, vcPW._eE);
		dlayer1D.rotateED(iForward, vcPW._eE, vcPW._eD);
		
		dlayer1D.transfer(-iForward, vcPW._rN, vcPW._rE, vcPW._rD);
		dlayer1D.rotateNE(iForward, vcPW._rN, vcPW._rE);
		dlayer1D.rotateED(iForward, vcPW._rE, vcPW._rD);
		
		System.out.println(vcPW.toString());
		
		iForward = -1;
		dlayer1D.transfer(-iForward, vcPW._eN, vcPW._eE, vcPW._eD);
		dlayer1D.rotateNE(iForward, vcPW._eN, vcPW._eE);
		dlayer1D.rotateED(iForward, vcPW._eE, vcPW._eD);
		
		dlayer1D.transfer(-iForward, vcPW._rN, vcPW._rE, vcPW._rD);
		dlayer1D.rotateNE(iForward, vcPW._rN, vcPW._rE);
		dlayer1D.rotateED(iForward, vcPW._rE, vcPW._rD);
		
		System.out.println(vcPW.toString());
	}
	public void rayTracing3() {
		DipLayer1D dlayer1D = new DipLayer1D("C:\\PINN_DATA\\rt2Test\\calgary\\04Layer3D.csv");
		//System.out.println(dlayer1D.toString());
		//FlatLayer1D layer1D = dlayer1D.toFlatLayer1D();
		//System.out.println(layer1D.toString());
		VCPair vcPW = new VCPair(1, "C:\\PINN_DATA\\rt2Test\\calgary\\pck01.csv");
		System.out.println(vcPW.toString());
		
		int iVp = 1;
		int iVTI = 1;
		int iApprox = 0;
		int iWave = 1;
		int iR = 0;
		int [] reflectorIndex = null;
		//int [] reflectorIndex = new int[]{4};
		double tol = 0.1;
		
		Pseudo3DRayTracer rayTracer = new Pseudo3DRayTracer(null, dlayer1D, vcPW,
				iVp, iVTI, iApprox, reflectorIndex, iWave,
				tol, iR, 0, 2);
		rayTracer.start();
		
		System.out.println(vcPW.toDeepString());
	}
	
	public void rayTracing4() {
		DipLayer1D dlayer1D = new DipLayer1D("C:\\PINN_DATA\\rt2Test\\calgary\\04Layer3D.csv");
		Layer3D layer3D = dlayer1D.toLayer3D();
		int nx = 5;
		int ny = 5;
		int nz = 5;
		double dx = 1000;
		double dy = 1000;
		double x0 = -1000;
		double y0 = -1000;

		
		double z0 	= dlayer1D.getLayer().get(0).getTDepth();
		double z1 	= dlayer1D.getLayer().get(dlayer1D.getLayer().size()-1).getTDepth();
		double dz 	= (z1-z0)/(nz-1);
		
		//Layer3D layer3D = dlayer1D.toLayer3D(nx, ny, nz, dx, dy, x0, y0);
		Vecon3D v3D = layer3D.toVecon3D(0, nx, ny, nz, dx, dy, dz, x0, y0, z0);
		v3D.writeAsVtk("C:\\PINN_DATA\\rt2Test\\calgary\\04Layer3Dbin.vtk");
		//System.out.println(dlayer1D.toString());
		//FlatLayer1D layer1D = dlayer1D.toFlatLayer1D();
		//System.out.println(layer1D.toString());
		//VCPair vcPW = new VCPair(1, "C:\\PINN_DATA\\rt2Test\\calgary\\pck01.csv");
		//System.out.println(vcPW.toString());
		//dlayer1D.rotate2D(new double[]{0.5}, new double[]{1}, 45.0*3.1415926/180);
		
		//System.out.println(vcPW.toString());
	}
	public void rayTracing(FlatLayer1D layer1D, VCPair vcPW, 
			int iVp, int iVTI, int iApprox, int iWave, int iR, int [] reflectorIndex, double tol, double rayPar[]) {
		int nReflector = 0;
		if(reflectorIndex!=null) nReflector = reflectorIndex.length;
		
		JCalArrivalTimesFlatLayerModel(
				iVp, iVTI, iApprox, iWave, iR, nReflector, reflectorIndex, tol, 

				layer1D.getNumOfBoundaries(), layer1D.getTop(),  
				layer1D.getLayerProperty(1), layer1D.getLayerProperty(2), layer1D.getLayerProperty(4), 
				layer1D.getLayerProperty(21), layer1D.getLayerProperty(20), layer1D.getLayerProperty(22),

				vcPW.getNumOfEvents(), vcPW.getFlag(), vcPW.getUnit(), vcPW.getOrg(), vcPW.getOrgT(), 
				vcPW.getEE(), vcPW.getEN(), vcPW.getED(), vcPW.getRE(), vcPW.getRN(), vcPW.getRD(), 
				vcPW.getObsPT(), vcPW.getCalPT(), vcPW.getObsST(), vcPW.getCalST(), vcPW.getObsSV(), vcPW.getCalSV(), 
				
				rayPar);
	}
	
	// Load shared library which contains implementation of native methods.
	static {
		//System.loadLibrary("com_lgc_bhss_rt");
		System.load("C:/Prowess/c/seispt/bin/com_seispt_rt2d.dll");
	}

	// Native method declarations.
	/**
	 * Calculate arrival times by ray tracing through a flat layered velocity model.
	 *
	 * @param iVp		1- for P-wave, 2- for SH-wave; 3- for SV-wave
	 * @param iVTI		0- isotropic media, 1- VTI media
	 * @param iApprox	0- using exact formula for anisotropic ray tracing, 1- using approximate formula
	 * 					iVp, iVTI, and iApprox should not conflict with each other
	 * @param iWave   	1- calculate direct arrivals, 2- calculate first arrivals,
	 * 					3- calculate reflect/multiple arrivals reflectorIndex
	 * @param iR   		1- calculate ray path, 0 - no ray path calculated
	 * @param reflectorIndex 	For reflect/multiple arrivals, this array contains reflector index 
	 * @param nReflectors 		Number of elements in the array that contains reflector index
	 * @param tol		Raytracing is successful if distance between receiver and raypath is smaller than the tol
	 *
	 * Parameters below are most likely members of the velocity model:
	 * @param nBndry	Number of total boundaries, including the top of the model
	 * @param vp		P-wave velocity below the boundary, Size [nBndry]
	 * @param vs		S-wave velocity below the boundary, Size [nBndry]
	 * @param den		density below the boundary, Size [nBndry]
	 * 					density is not used during ray tracing.
	 * 					It is a place-holder now
	 * @param delta		anisotropic delta below the boundary, Size [nBndry]
	 * @param epsilon	anisotropic epsilon below the boundary, Size [nBndry]
	 * @param gamma		anisotropic gamma below the boundary, Size [nBndry]
	 *
	 * Parameters below are most likely members of a VCPair:
	 * @param nEvents	total number of events(shots)
	 * @param flag		number of valid receivers within each shot, Size [nEvents]
	 * param iUnit		distance unit in m (1), ft (2), km(3)
	 * param org		int array contains on(1) and off(0) for update T0, Size [nEvents]
	 * param orgT		double array contains T0, Size [nEvents]
	 * @param rE		receiver coordinates positive to east, Size [fp]
	 * @param rN		receiver coordinates positive to north, Size [fp]
	 * @param rD		receiver coordinates positive down, Size [fp]
	 * @param eE		source coordinates positive to east, Size [nEvents]
	 * @param eN		source coordinates positive to north, Size [nEvents]
	 * @param eD		source coordinates positive down, Size [nEvents]
	 * @param obsPT		observed/picked P-wave arrival time in seconds, Size [fp]
	 * @param calPT		calculated P-wave arrival time in seconds, Size [fp]
	 * @param obsST		observed/picked SH-wave arrival time in seconds, Size [fp]
	 * @param calST		calculated SH-wave arrival time in seconds, Size [fp]
	 * @param obsSV		observed/picked SV-wave arrival time in seconds, Size [fp]
	 * @param calSV		calculated SV-wave arrival time in seconds, Size [fp]
	 * 
	 * @param rayPar	Ray parameters for each shot-event pair, Size [fp]
	 */
	public static native void JCalArrivalTimesFlatLayerModel(
			int iVp, int iVTI, int iApprox, int iWave, int iR, int nReflectors, int [] reflectorIndex, double tol, 

			int nBndry, double top[], double vp[], double vs[], double den[], 
			double delta[], double epsilon[], double gamma[],

			int nEvents, int flag[], int iUnit, int org[], double orgT[], 
			double eE[], double eN[], double eD[], double rE[], double rN[], double rD[], 
			double obsPT[], double calPT[], double obsST[], double calST[], double obsSV[], double calSV[], 

			double rayPar[] );

}

