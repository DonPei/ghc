package com.geohammer.rt.pseudo3d;

import java.util.Arrays;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import com.geohammer.core.MatrixFileReader;
import com.geohammer.rt.pseudo3d.RayTracerFlat3D;
import com.geohammer.core.acquisition.VCPair;
import com.geohammer.core.planarmodel.FlatLayer1D;
import com.geohammer.core.planarmodel.Layer2D;
import com.geohammer.core.planarmodel.Layer3D;
import com.geohammer.core.planarmodel.SingleFlatLayer;
import com.geohammer.core.planarmodel.Vecon3D;
import com.geohammer.vc.run.SA;
import com.geohammer.vc.run.panel.LocationPanel;
import com.geohammer.vc.run.panel.PlotVelocity;

import edu.mines.jtk.util.ArrayMath;

public class RayTracerTest {
	public FlatLayer1D _initModel = null;
	public FlatLayer1D _trueModel = null;

	public VCPair _vcPWPPtrue = null;
	public VCPair _vcPWSHtrue = null;
	public VCPair _vcPWSVtrue = null;
	public VCPair _vcPWAZtrue = null;
	public VCPair _vcPWPPinit = null;
	public VCPair _vcPWSHinit = null;
	public VCPair _vcPWSVinit = null;
	public VCPair _vcPWAZinit = null;

	public RayTracerTest() {
			test1();
	}
	
	public void test1() {
//		Layer2D layer2D = new Layer2D(2, "C:\\PINN_DATA\\EOG_VTI\\university\\data\\layer2D.csv");
//		FlatLayer1D layer1D = layer2D.toFlatLayer1D(true);
//		VCPair vcPW = new VCPair(1, "C:\\PINN_DATA\\EOG_VTI\\university\\data\\pck2D.csv");
		

		long startTime = System.currentTimeMillis(); 
		
		Layer2D layer2D = new Layer2D(2, "C:\\PINN_DATA\\rt2Test\\calgary\\04Layer.csv");
		FlatLayer1D layer1D = layer2D.toFlatLayer1D(true);
		VCPair vcPW = new VCPair(1, "C:\\PINN_DATA\\rt2Test\\calgary\\pck01.csv");
		System.out.println(vcPW.toDeepString());

		double x0 = -250;
		double x1 = 250;
		double dx = 5;
		double y0 = -250;
		double y1 = 250;
		double dy = 5;
		double z0 = 5000;
		double z1 = 9000;
		double dz = 40;	
		//System.out.println(vcPW.toDeepString());
		
		for(int i=0; i<layer1D.getNumOfBoundaries(); i++) {
			layer1D.getLayer(i).setSize(dx, dy, dz, x0, y0, z0, x1, y1, z1);
		}
		
		SingleFlatLayer sl = layer1D.getLayer(0);
		Layer3D layer3D = layer1D.toLayer2D(sl.getX0(), sl.getX1()).toLayer3D(sl.getY0(), sl.getY1());
		Vecon3D v3d = layer3D.toVecon3D(1, 
				sl.getNx(), sl.getNy(), sl.getNz(), 
				sl.getDx(), sl.getDy(), sl.getDz(), 
				x0, y0, z0);
		//sl.getX0(), sl.getY0(), sl.getZ0());
		v3d.setData(0);
		
		int iVp = 1;
		int iVTI = 1;
		int iWave = 1;
		int iR = 0;
		int iApprox = 0;
		int [] reflectorIndex = null;
		//int [] reflectorIndex = new int[]{4};
		double tol = 0.1;
		
		RayTracerFlat3D rayTracerP = new RayTracerFlat3D(null, layer1D, vcPW, iVp, iVTI, 
				iApprox, reflectorIndex, iWave, tol, iR, 0);
		rayTracerP.start();
		
		for(int iy=0; iy<v3d.getNy(); iy++) {
			vcPW.setData(0, vcPW.getRN(), v3d.getY(iy));
			for(int ix=0; ix<v3d.getNx(); ix++) {
				vcPW.setData(0, vcPW.getRE(), v3d.getX(ix));
				for(int iz=0; iz<v3d.getNz(); iz++) { 
					vcPW.setData(0, vcPW.getRD(), v3d.getZ(iz));
										
					rayTracerP.start();
					//System.out.print(vcPW.getCalPT(0)+" ");
					v3d.setData(ix, iy, iz, (float)vcPW.getCalPT(0));
				}
				//System.out.println();
			}
		}
		

		long endTime = System.currentTimeMillis();
		float elapsedTimeMillis = endTime - startTime;
		float elapsedTimeSec = elapsedTimeMillis/1000F; 
		float elapsedTimeMin = elapsedTimeMillis/(60*1000F); 
		float elapsedTimeHour = elapsedTimeMillis/(60*60*1000F);
		float elapsedTimeDay = elapsedTimeMillis/(24*60*60*1000F);

		String aTime = new String("Successfully Done With Elapsed Time:\n " + (int)(elapsedTimeSec) + " second(s). \n");
		if( elapsedTimeMin>1.0 ) {
			elapsedTimeSec = elapsedTimeSec - (int)(elapsedTimeMin)*60;
			String bTime = new String(" Or " + (int)(elapsedTimeMin) + " minute(s) " + " and " + (int)(elapsedTimeSec) + " second(s). \n");
			aTime = new String(aTime.concat(bTime));
		}
		System.out.println(aTime);
		
	}
	
	public void test2() {
		int iType = 2;
		Layer2D layer2D = new Layer2D(2, "C:\\PINN_DATA\\rt2Test\\T10\\bakkan.csv");
		FlatLayer1D layer1D = layer2D.toFlatLayer1D(true);
		VCPair vcPW = new VCPair(1, "C:\\PINN_DATA\\rt2Test\\T10\\pck3.csv");
		
		int iVp = 1;
		int iVTI = 1;
		int [] rayCodes = null;
		int iWave = 2;
		int iR = 0;
		RayTracerFlat3D rayTracerP = new RayTracerFlat3D(null, layer1D, vcPW, iVp, iVTI, 
				0, null, iWave, 0.01, 0, 0);
		rayTracerP.start();
		//vcPWPp.setCalPT(rayTracerP.getRayPar());
		//vcPWPp.write(1, "C:\\PINN_DATA\\rt2Test\\altdd\\p1.csv");
	}
	

	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				new RayTracerTest();
			}
		});
	}
}
