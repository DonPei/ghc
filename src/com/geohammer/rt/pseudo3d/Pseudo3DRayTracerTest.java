package com.geohammer.rt.pseudo3d;

import java.io.File;

import javax.swing.SwingUtilities;

import org.ucdm.core.MSEvent;
import org.ucdm.core.ObsWell;
import org.ucdm.core.SeisPTSrcMech;
import org.ucdm.core.SeisPTVel;
import org.ucdm.core.SeismicTraceComponent;
import org.ucdm.core.Sensor;
import org.ucdm.rt.pseudo3d.RayTracerFlat3D;
import org.ucdm.seg2.SEG2;
import org.ucdm.tracedisplay.TraceDisplayFrame;
import org.ucdm.xml.mdl.MdlXml;
import org.ucdm.core.acquisition.VCPair;
import org.ucdm.core.planarmodel.DipLayer1D;
import org.ucdm.core.planarmodel.FlatLayer1D;
import org.ucdm.core.planarmodel.Layer2D;
import org.ucdm.fd3d.FdWavelet;


public class Pseudo3DRayTracerTest implements Runnable {
	public Pseudo3DRayTracerTest() {

	}
	public void run() {	
		natalia();

		System.out.println("Successfully Done!");
	}
	public static void main(String[] args) {
		System.out.println("Executing SeisPTTest()");
		SwingUtilities.invokeLater( new Pseudo3DRayTracerTest() );
	}

	public void natalia() {
		MdlXml mdlXml = new MdlXml("C:\\PINN_DATA\\aramis\\dipTest\\VM2 VTI HT3 Stage 13-14 final.par.xml");
		DipLayer1D dipLayer1D = mdlXml.toDipLayer1D();
		//System.out.println(dipLayer1D.toString());

		SeisPTSrcMech srcMech = new SeisPTSrcMech("C:\\PINN_DATA\\aramis\\dipTest\\stage18.txt");
		//System.out.println(srcMech.toString());
		MSEvent [] msEvents = srcMech.getMSEvents();
		//msEvents[0].printTrace(0, 0, 1);
		VCPair vcPW = msEvents[0].genVCPair(-1, 0);
		System.out.println(vcPW.toString());
		

		//FlatLayer1D layer1DPp = rt.getFlatLayer1D().copy();
//		FlatLayer1D layer1DPp = _dipLayer1D.toFlatLayer1D();
//		FlatLayer1D layer1DSh = _dipLayer1D.toFlatLayer1D();
//		//System.out.println("start vp="+layer1DPp.getLayer(0).getVp()+" vs="+layer1DPp.getLayer(0).getVs());
//		VCPair vcPW = rt.getVCPair();
//		//System.out.println(vcPW.toString());
//		VCPair vcPWPp = vcPW.copy();
//		VCPair vcPWSh = vcPW.copy();
//		if(iType==3) { average(vcPWPp); average(vcPWSh); }
//		RayTracerFlat3D rayTracerP = new RayTracerFlat3D(null, _dipLayer1D, layer1DPp, vcPWPp, 1, rt.getIVTI(), 
//				rt.getIApproximation(), rt.getReflectorIndex(), rt.getPsIndex(), rt.getIWave(), rt.getTolerance(), 0, 0);
//		rayTracerP.start();
//		//vcPWPp.setCalPT(rayTracerP.getRayPar());
//		//vcPWPp.write(1, "C:\\PINN_DATA\\rt2Test\\altdd\\p1.csv");
//
//		RayTracerFlat3D rayTracerSh = new RayTracerFlat3D(null, rayTracerP.getDipLayer1D(), layer1DSh, vcPWSh, 2, rt.getIVTI(), 
//				rt.getIApproximation(), rt.getReflectorIndex(), rt.getPsIndex(), rt.getIWave(), rt.getTolerance(), 0, 0);
//		rayTracerSh.start();
//		//vcPWSh.setCalST(rayTracerSh.getRayPar());
//		//vcPWSh.write(3, "C:\\PINN_DATA\\rt2Test\\altdd\\sh1.csv");
//
//		//System.out.println(layer1DSh.toString());
//		//System.out.println(vcPWSh.toDeepString());
//		//System.exit(1);
//
//		//if(iType==1) tuneParameter 	= 0.01;
//		//RayTracerFlat3D [] rayTracers = new RayTracerFlat3D[] {rayTracerP, rayTracerSh, rayTracerSv};
//		RayTracerFlat3D [] rayTracers = new RayTracerFlat3D[4];
//		rayTracers[0] = rayTracerP;
//		rayTracers[1] = rayTracerSh;
//		//RayTracerFlat3D [] rayTracers = new RayTracerFlat3D[] {rayTracerP};
//
//		int nRT = 2;
//		if(_reflectionEnabled) {
//			int nBoundaries = layer1DPp.getNumOfBoundaries();
//			RayTracerFlat3D reflectRt1 = genRayTracerFlat3D(rayTracers[0], _ttFileType1, _pckTF1.getText().trim(), 
//					nBoundaries, _rayCodeTF1.getText().trim(), _psCodeTF1.getText().trim());
//
//			RayTracerFlat3D reflectRt2 = genRayTracerFlat3D(rayTracers[0], _ttFileType2, _pckTF2.getText().trim(), 
//					nBoundaries, _rayCodeTF2.getText().trim(), _psCodeTF2.getText().trim());
//
//			if(reflectRt1!=null) { rayTracers[nRT++] = reflectRt1; }
//			if(reflectRt2!=null) { rayTracers[nRT++] = reflectRt2; }
//		}
//
//		RayTracerFlat3D [] rayTracers1 = new RayTracerFlat3D[nRT];
//
//		VCPair azPW = null;

	}



}

