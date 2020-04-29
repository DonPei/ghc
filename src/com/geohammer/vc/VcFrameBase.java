package com.geohammer.vc;

import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.image.ColorModel;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import javax.swing.JLabel;

import com.geohammer.common.CommonFrame;
import com.geohammer.common.ProjectVc;
import com.geohammer.common.WellTrack;
import com.geohammer.component.StatusBar;
import com.geohammer.component.TransparentPixelsView;
import com.geohammer.core.acquisition.VCPair;
import com.geohammer.core.planarmodel.DipLayer1D;
import com.geohammer.core.planarmodel.FlatLayer1D;
import com.geohammer.core.planarmodel.Layer2D;
import com.geohammer.core.planarmodel.Layer3D;
import com.geohammer.rt.RayPath;
import com.geohammer.rt.RayPaths;
import com.geohammer.rt.pseudo3d.FrameI;
import com.geohammer.rt.pseudo3d.Pseudo3DRayTracer;

import edu.mines.jtk.awt.ColorMap;

//public class VcFrameBase extends CommonFrame {
public class VcFrameBase extends CommonFrame implements FrameI {
	private static final long serialVersionUID 		= 1L;	
	public int 					exitCode 			= 1;
	public float [][] 			minMax 				= null; 	
	public ColorMap 			colorMap 			= null;
	
	//public Vc2DPanel 			vc2DPanel			= null;
	public ProjectVc 			project 			= null;
	public VcMenuBar 			vcMenuBar			= null;
	
	public VcCardPanel 			card  				= null;
	public Vel2dPanel 			vel2dPanel			= null;
	public Vel3dPanel 			vel3dPanel			= null;
	
	public Pseudo3DRayTracer 	rt 					= null;
	public RayPaths []			rayPath 			= null;
	public VCPair 				vcPW 				= null;
	private Random 				fRandom 			= new Random();
	
	public int 					iVp 				= 1;
	public String 				saCwd 				= null;

	public float 				clipMin 			= Float.MAX_VALUE;
	public float 				clipMax 			= Float.MIN_VALUE;
	public TransparentPixelsView transparentPixelsView = null;
	
	public RayPaths 			rayPaths 			= null;
	
	//public VcFrameBase() { super(); }
	public VcFrameBase(String title) { 
		super(title); 
	}
	
	public ColorMap getColorMap() 					{ return colorMap; }
	public ProjectVc getProject() 					{ return project; }
	public Vel2dPanel getVel2dPanel() 				{ return vel2dPanel; }
	public Vel3dPanel getVel3dPanel() 				{ return (Vel3dPanel)card.getJPanel("3D"); }
	public Pseudo3DRayTracer getRayTracer() 		{ return rt; }
	
	public void newRayTracer() 						{ 
		if(rt==null) newRayTracer(getIVp(), 0, 0, null, null, 2, 1); 
		else newRayTracer(getIVp(), rt.getIVTI(), rt.getIApproximation(), rt.getReflectorIndex(), 
				rt.getPsIndex(), rt.getIWave(), rt.getIR()); 
	}
	public void newRayTracer(int iVp, int iVTI, int iApprox, int [] rayCodes, int [] psCodes, int iWave, int iR) {
		DipLayer1D dipLayer1D 	= project.getDipLayer1D();
		VCPair vcPW 			= project.getVCPair3D();
		double tol = 0.01;
		//System.out.println(dipLayer1D.toString());
		//System.out.println("iVP="+iVp+" iVTI="+iVTI+" iWave="+iWave+" iR="+iR);
		//System.out.println(vcPW.toString(true));
		rt = new Pseudo3DRayTracer(this, dipLayer1D, vcPW, iVp, iVTI, iApprox, rayCodes, psCodes, iWave, 
				tol, iR, 0, project.is2D()?2:3);
	}

	public void rayTracing1(int iMethod) {
		if(rt!=null) {
			rt.setIMethod(iMethod);
			rt.setIVp(iVp); 
			rt.setConfiguration(); 
			rt.start(1);
			project.updateTravelTimeForAll();
		}
	}
	public void startRayTracing(int runNo) {
		if(rt!=null) {
			rt.setIMethod(1);
			rt.setIVp(iVp); rt.setConfiguration(); rt.start(runNo);
			
			FlatLayer1D layer1D = rt.getFlatLayer1D();
			if(!layer1D.isVsZero()) {
				rt.setIVp(2); rt.setConfiguration(); rt.start(runNo);
				rt.setIVp(3); rt.setConfiguration(); rt.start(runNo);
			}
			
			project.updateTravelTimeForAll();
			
			int iT0 = rt.getIT0();
			VCPair vcPW = rt.getVCPair();
			if(iT0>0) {
				if(iT0==1)  {
					vcPW.estimateT0(1, 0, 0);
					vcPW.applyT0(1, 0, 0);
				} else if(iT0==2)  {
					vcPW.estimateT0(0, 1, 0);
					vcPW.applyT0(0, 1, 0);
				} else if(iT0==3)  {
					vcPW.estimateT0(0, 0, 1);
					vcPW.applyT0(0, 0, 1);
				} else if(iT0==11)  {
					vcPW.estimateT0(1, 1, 0);
					vcPW.applyT0(1, 1, 0);
				} else if(iT0==111)  {
					vcPW.estimateT0(1, 1, 1);
					vcPW.applyT0(1, 1, 1);
				}
			}
			//System.out.println(vcPW.toString(false));
			
			addRayPath(rayPath[iVp-1]);
		}
	}
	
	public void removeRayPath() {
		if(getVel3dPanel()!=null) getVel3dPanel().removeRayPath(); 
		else getVel2dPanel().removeViews(0, 0, "Ray Path"); 
	}
	public void addRayPath(RayPaths rayPaths) {
		if(rayPaths==null) return;
		if(rayPaths.getNumOfTraces()<1) return;
		rayPaths = rayPaths;
//		if(getProject().is2D()) {
//			double azimuth = getProject().getAzimuth();
//			double centerN = getProject().getCenterN();
//			double centerE = getProject().getCenterE();
//			ArrayList<RayPath> rays = rayPaths.getRayPathList();
//			for(int i=0; i<rays.size(); i++) {
//				RayPath rayPath = rays.get(i);
//				double [] E = rayPath.getE();
//				double [] N = rayPath.getN();
//				double [] D = rayPath.getD();
//				for(int j=0; j<E.length; j++) {
//					E[j] -= centerE;
//					N[j] -= centerN;
//				}
//				getProject().rotate(azimuth, E, N);
//			}
//		}
		//System.out.println(rayPaths.toString());
		if(getVel3dPanel()!=null) { getVel3dPanel().addRayPath(rayPaths); return; }
		getVel2dPanel().removeViews("Ray Path");
		double x1min = getProject().getLayer2D().getZ0();
		double x2min = getProject().getLayer2D().getX0();
		double x1max = getProject().getLayer2D().getZ1();
		double x2max = getProject().getLayer2D().getX1();
		//System.out.println("x1min="+x1min+" x2min="+x2min+" x1max="+x1max+" x2max="+x2max);
		int nShots = rayPaths.getNumOfEmsembles();
		//System.out.println("nShots="+nShots);
		for(int i=0; i<nShots; i++) {
			RaypathView rv	 = new RaypathView(rayPaths.getEmsemble(i), x1min, x2min, x1max, x2max);
			rv.setName("Ray Path", "Path "+i);
			//rv.setLineColor(Color.WHITE);
			float width = rv.getLineWidth()*0.5f;
			rv.setLineWidth(width);
			rv.setVisible(getVel2dPanel().getVisible(rv.getCategory(), rv.getName()));
			//getWorld().removeView(rv.getCategory(), rv.getName());
			getVel2dPanel().addRaypathView(0, 0, rv);
		}
	}	

	public void setRayPath(int index, RayPaths rayPaths) { rayPath[index] = rayPaths; }
	public int getIVp() 							{ return iVp;  }	
	public void setIVp(int iVp) 					{  this.iVp = iVp; }

	public String genSaCwd() 			{ return getProject().createFolder(getProject().getFullPath()+"sa"); }
	public void setSaCwd(String cwd) 	{ this.saCwd = cwd; }
	public String getSaCwd() 			{ return saCwd==null?genSaCwd():saCwd; }
	public int getDimension() 			{ return project.is2D()?2:3; }
	
	@Override
	public void saveVCPairAs(boolean eventOnly) { }

	public void redrawAndUpdateLayer(int [] index, FlatLayer1D layer1D) {
		if(getVel3dPanel()!=null) {
			Layer3D layer3D = getProject().getLayer3D();
			getRayTracer().getFlatLayer1D();

			DipLayer1D dlayer1D = getRayTracer().getDipLayer1D();
			FlatLayer1D flayer1D = getRayTracer().getDipLayer1D();
			for(int i=0; i<index.length; i++) {
				for(int j=0; j<layer3D.getNumOfBoundaries(); j++) {
					layer3D.getLayer(j).setLayerProperty(index[i], layer1D.getLayer(j).getLayerProperty(index[i]));
					dlayer1D.getLayer(j).setLayerProperty(index[i], layer1D.getLayer(j).getLayerProperty(index[i]));
					flayer1D.getLayer(j).setLayerProperty(index[i], layer1D.getLayer(j).getLayerProperty(index[i]));

					if(index[i]==3) {
						double vs = layer1D.getLayer(j).getVp()/layer1D.getLayer(j).getVpToVs();
						layer3D.getLayer(j).setLayerProperty(2, vs);
						dlayer1D.getLayer(j).setLayerProperty(2, vs);
						flayer1D.getLayer(j).setLayerProperty(2, vs);
					}
				}
			}
			getVel3dPanel().add(getVel3dPanel().getWorld(), layer3D, true);
		} else {
			Layer2D layer2D = getProject().getLayer2D();
			for(int i=0; i<index.length; i++) {
				for(int j=0; j<layer2D.getNumOfBoundaries(); j++) {
					layer2D.getLayer(j).setLayerProperty(index[i], layer1D.getLayer(j).getLayerProperty(index[i]));
					if(index[i]==3) {
						double vs = layer1D.getLayer(j).getVp()/layer1D.getLayer(j).getVpToVs();
						layer2D.getLayer(j).setLayerProperty(2, vs);
					}
				}
			}
			updateLayer(layer2D);
		}
	}
	public void updateLayer(Layer2D layer2D) 	{ updateLayer(layer2D, null, null); }
	public void updateLayer(Layer2D layer2D, VCPair vcPW, WellTrack wellTrack) {
		if(layer2D==null) return;
		updateWorld();
	}
	public void updateWorld() {  }

	public void setVisible(boolean visible) {
		super.setVisible(visible);

		if(visible) {
			GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
			GraphicsDevice gd = ge.getDefaultScreenDevice();
			GraphicsConfiguration [] gc = gd.getConfigurations();
			Rectangle gcBounds = gc[0].getBounds();

			int w = (int)(6.0*gcBounds.getWidth() / 10.0);
			int h = (int)(8.0*gcBounds.getHeight() / 10.0);
			setSize(w, h);
			//setSize(1000, 800);

			setLocationRelativeTo(null);
		}
	}

}
