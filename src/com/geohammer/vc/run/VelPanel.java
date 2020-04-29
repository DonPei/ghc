package com.geohammer.vc.run;

import java.awt.Color;
import java.awt.Font;

import com.geohammer.common.CommonPanel;
import com.geohammer.core.planarmodel.DipLayer1D;
import com.geohammer.core.planarmodel.FlatLayer1D;

import edu.mines.jtk.mosaic.PointsView;

public class VelPanel extends CommonPanel {
	
FlatLayer1D 	_layer1D 	= null;
	
	boolean 		_vpOn 		= true;
	boolean 		_vsOn 		= true;
	
	boolean 		_deltaOn 	= true;
	boolean 		_epsilonOn 	= true;
	boolean 		_gammaOn 	= true;

	float [] 		_dp 		= null;
	//float [] 		_depth 		= null;
	
	public VelPanel(FlatLayer1D layer1D, 
			boolean vpOn, boolean vsOn, boolean deltaOn, boolean epsilonOn, boolean gammaOn) {
		super(1,2,CommonPanel.Orientation.X1DOWN_X2RIGHT, CommonPanel.AxesPlacement.LEFT_TOP, null);
		
		setBackground(Color.white);
		
		Font font = new Font ("Arial", Font.BOLD, 12); //Monospaced, Serif, Dialog, Sanserif
		setFont(font);
		
		//super(new LayerPanel(1, 2, LayerPanel.Orientation.X1DOWN_X2RIGHT,  LayerPanel.AxesPlacement.LEFT_TOP), false);
		_vpOn 		= vpOn;
		_vsOn 		= vsOn;
		_deltaOn 	= deltaOn;
		_epsilonOn 	= epsilonOn;
		_gammaOn 	= gammaOn;
		
		_layer1D 	= layer1D;
		int n = layer1D.getNumOfBoundaries()-1;
//		_dp = new float[n];
//		for(int i=0; i<n; i++) {
//			_dp[i] = (float)layer1D.getLayer(i).getTopDepth();
//		}
		float a = 0;
		_dp = new float[2*n];
		int k = 0;
		for(int i=0; i<n; i++) {
			k = 2*i;
			a = (float)layer1D.getLayer(i).getTopDepth();
			if(i==0) { _dp[k] = a; }
			else {
				_dp[k-1] = a;
				_dp[k] = a;
			}
		}
		k = _dp.length-1;
		a = (float)layer1D.getLayer(n).getTopDepth();
		_dp[k] = a;
		
		if(_layer1D!=null) update(_layer1D, 1, "True");
	}

	private float[] getData(int iVp, FlatLayer1D layer1D) {
//		float [] vel = new float[_dp.length];
//		for(int i=0; i<vel.length; i++) {
//			if(iVp==1) vel[i] = (float)layer1D.getLayer(i).getVp();
//			else if(iVp==2) vel[i] = (float)layer1D.getLayer(i).getVs();
//			else if(iVp==3) vel[i] = (float)layer1D.getLayer(i).getDelta();
//			else if(iVp==4) vel[i] = (float)layer1D.getLayer(i).getEpsilon();
//			else if(iVp==5) vel[i] = (float)layer1D.getLayer(i).getGamma();
//			else  vel[i] = (float)layer1D.getLayer(i).getVp();
//		}
//		
		int n = layer1D.getNumOfBoundaries()-1;
		float a = 0;
		float [] vel = new float[2*n];
		int k = 0;
		for(int i=0; i<n; i++) {
			k = 2*i;
			if(iVp==1) a = (float)layer1D.getLayer(i).getVp();
			else if(iVp==2) a = (float)layer1D.getLayer(i).getVs();
			else if(iVp==3) a = (float)layer1D.getLayer(i).getDelta();
			else if(iVp==4) a = (float)layer1D.getLayer(i).getEpsilon();
			else if(iVp==5) a = (float)layer1D.getLayer(i).getGamma();
			else  a = (float)layer1D.getLayer(i).getVp();
			
			vel[k] = a;
			vel[k+1] = a;
		}
		
		return vel;
	}
	
	
	public void setEnable(String lable, boolean enable) {
		if(lable.equalsIgnoreCase("vp")||lable==null) 			_vpOn 		= enable;
		else if(lable.equalsIgnoreCase("vs")||lable==null) 		_vsOn 		= enable;
		else if(lable.equalsIgnoreCase("delta")||lable==null) 	_deltaOn 	= enable;
		else if(lable.equalsIgnoreCase("epsilon")||lable==null) _epsilonOn 	= enable;
		else if(lable.equalsIgnoreCase("gamma")||lable==null) 	_gammaOn 	= enable;
	}
	public void update(FlatLayer1D layer1D, int iType, String name) {
		float size = 12;
		//float size = pv.getMarkSize()*5.0f;
		float [] vel = null;
		PointsView pv = null;
	    setVLabel(0, "Depth");
    	setHLabel(0, "Vp(R) Vs(B)");
    	setHLabel(1, "Delta(R) Epsilon(B) Gamma(K)");
    	getTile(0,0).getTileAxisTop().setFormat("%1.0f");
	    
		//vp
	    if(_vpOn) {
	    	vel = getData(1, layer1D);
	    	pv = new PointsView(_dp, vel);
	    	pv.setName("Vel", "vp "+name);
	    	pv.setStyle("r-");
	    	pv.setLineWidth(1.5f);
//	    	if(iType==1) pv.setStyle("ro");
//	    	else pv.setStyle("r+");
//	    	pv.setMarkSize(size);
	    	removeView(pv.getCategory(), pv.getName());
	    	addPointsView(0, 0, pv);
	    } 
	    if(_vsOn) {
	    	//vs
	    	vel = getData(2, layer1D);
	    	pv = new PointsView(_dp, vel);
	    	pv.setName("Vel", "vs "+name);
	    	pv.setStyle("b-");
	    	pv.setLineWidth(1.5f);
//	    	if(iType==1) pv.setStyle("bo");
//	    	else pv.setStyle("b+");
//	    	pv.setMarkSize(size);
	    	removeView(pv.getCategory(), pv.getName());
	    	addPointsView(0, 0, pv);
	    } 
	    if(_deltaOn) {	
	    	//delta
	    	vel = getData(3, layer1D);
	    	pv = new PointsView(_dp, vel);
	    	pv.setName("Vel", "delta "+name);
	    	pv.setStyle("r-");
	    	pv.setLineWidth(1.5f);
//	    	if(iType==1) pv.setStyle("ro");
//	    	else pv.setStyle("r+");
//	    	pv.setMarkSize(size);
	    	removeView(pv.getCategory(), pv.getName());
	    	addPointsView(0, 1, pv);
	    } 
	    if(_epsilonOn) {
	    	//epsilon
	    	vel = getData(4, layer1D);
	    	pv = new PointsView(_dp, vel);
	    	pv.setName("Vel", "epsilon "+name);
	    	pv.setStyle("b-");
	    	pv.setLineWidth(1.5f);
//	    	if(iType==1) pv.setStyle("bo");
//	    	else pv.setStyle("b+");
//	    	pv.setMarkSize(size);
	    	removeView(pv.getCategory(), pv.getName());
	    	addPointsView(0, 1, pv);
	    } 
	    if(_gammaOn) {
	    	//gamma
	    	vel = getData(5, layer1D);
	    	pv = new PointsView(_dp, vel);
	    	pv.setName("Vel", "gamma "+name);
	    	pv.setStyle("k-");
	    	pv.setLineWidth(1.5f);
//	    	if(iType==1) pv.setStyle("ko");
//	    	else pv.setStyle("k+");
//	    	pv.setMarkSize(size);
	    	removeView(pv.getCategory(), pv.getName());
	    	addPointsView(0, 1, pv);
	    }
	    
	}

}
