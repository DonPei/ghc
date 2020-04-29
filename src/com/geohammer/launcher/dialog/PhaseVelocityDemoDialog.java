package org.ucdm.launcher.dialog;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.ucdm.common.CommonDialog;
import org.ucdm.common.CommonMouseEditingMode;
import org.ucdm.common.CommonPointPanel;
import org.ucdm.core.acquisition.VCPair;
import org.ucdm.core.planarmodel.DipLayer1D;
import org.ucdm.core.planarmodel.FlatLayer1D;
import org.ucdm.core.planarmodel.SingleFlatLayer;
import org.ucdm.launcher.LauncherFrame;
import org.ucdm.rt.pseudo3d.Pseudo3DRayTracer;
import org.ucdm.rt.pseudo3d.SeismicVel;
import org.ucdm.vc.dialog.PlotDialog;

import edu.mines.jtk.mosaic.PointsView;
import edu.mines.jtk.util.ArrayMath;


public class PhaseVelocityDemoDialog  extends CommonDialog {
	
	LauncherFrame 		_frame 		= null;
	
	private String [] _sliderName 	= new String[] { "Vp(ft/s)", "Vs(ft/s)", "Delta", "Epsilon", "Gamma" };
	private double [] _sliderValue 	= new double [_sliderName.length];
	private double [][] _sliderMinMax= new double [][] { {8000, 16000}, {6000, 12000}, 
			{-0.3, 0.3}, {-0.5, 0.5}, {-0.5, 0.5}};
	
	private JLabel 				_valueLabel 	= null;
	private CommonPointPanel	_plot 			= null;
	private Pseudo3DRayTracer 	_rt 			= null;
	
	public PhaseVelocityDemoDialog(JFrame aParent, String aTitle, boolean modal) {
		super(aParent, aTitle, modal);
		setDialogWindowSize(800, 800);
		_frame 	= (LauncherFrame)aParent;
		for(int i=0; i<_sliderName.length; i++) _sliderValue[i] = 0.5*(_sliderMinMax[i][1]+_sliderMinMax[i][0]);
		_valueLabel 	= new JLabel(getValueString(), JLabel.CENTER);
		_rt = genRayTracing();
		
		String hLabel = "Horizontal Phase Velocity (qP-red qSH-black qSV-blue) (dotted lines for iso)";
		String vLabel = "Vertical Phase Velocity";
		_plot 		= new CommonPointPanel(0, false,"Phase", hLabel, vLabel);
		_plot.setHLabel(0, hLabel);
	    _plot.setVLabel(0, vLabel);
		_plot.setBackground(Color.white);
		_plot.setFrame(_frame);		

		_plot.setEnableTracking(false);
		_plot.setEnableEditing(true);
		_plot.setEnableZoom(false);
		
		_plot.addModeManager();
		CommonMouseEditingMode mouseEditingMode = _plot.getMouseEditingMode();
		//mouseEditingMode.setShowAutoAxisLimit(false);
		mouseEditingMode.setShowManualAxisLimit(true);
		mouseEditingMode.setJComponent(genJMenuItem());	
		
		updatePlot();
	}
	
	private JMenuItem[] genJMenuItem() {
		JMenuItem jMenuItem1 = new JMenuItem("Sensitivity");		
		jMenuItem1.addActionListener(new ActionListener() 	{
			public void actionPerformed(ActionEvent e)   {
				updateModel();
				for(int i=0; i<3; i++) {
					_rt.setIVp(i+1); _rt.setConfiguration(); _rt.start(1);
				}
				VCPair vcPW = _rt.getVCPair().copy();
				
				//delta
				updateModel(1.0, 1.0, 1.2, 1.0, 1.0);
				double dd = _sliderValue[2]*0.2;
				for(int i=0; i<3; i++) {
					_rt.setIVp(i+1); _rt.setConfiguration(); _rt.start(1);
				}
				VCPair vcPWD = _rt.getVCPair().copy();
				
				//epsilon
				updateModel(1.0, 1.0, 1.0, 1.2, 1.0);
				double de = _sliderValue[3]*0.2;
				for(int i=0; i<3; i++) {
					_rt.setIVp(i+1); _rt.setConfiguration(); _rt.start(1);
				}
				VCPair vcPWE = _rt.getVCPair().copy();
				
				//gamma
				updateModel(1.0, 1.0, 1.0, 1.0, 1.2);
				double dg = _sliderValue[4]*0.2;
				for(int i=0; i<3; i++) {
					_rt.setIVp(i+1); _rt.setConfiguration(); _rt.start(1);
				}
				VCPair vcPWG = _rt.getVCPair().copy();
				
				int n = 90;
				float [] x = new float[n];
				for(int i=0; i<n; i++) x[i] = i;
				
				float [] dtdd = new float[n];
				float [] dtde = new float[n];
				float [] dtdg = new float[n];
				for(int i=0, k=0; i<vcPW.getNumOfEvents(); i++) {
					for(int j=0; j<vcPW.getFlag(i); j++, k++) {
						if(dd!=0.0) dtdd[k] = (float)(1000*(vcPW.getCalPT(k)-vcPWD.getCalPT(k))/dd);
						if(de!=0.0) dtde[k] = (float)(1000*(vcPW.getCalPT(k)-vcPWE.getCalPT(k))/de);
						if(dg!=0.0) dtdg[k] = (float)(1000*(vcPW.getCalST(k)-vcPWG.getCalST(k))/dg);
					}
				}
				
				String aTitle = new String("Sensitivity");
				String catalog = "Sensitivity";
				final String vLabel = "Travel Time Derivative (dt/dd in ms)";
				String hLabel = "Ray Emergent Angle (degree)";
				
				PlotDialog dialog = new PlotDialog(_frame, aTitle, false, null,
						130, false, catalog, hLabel, vLabel);
				dialog.setCurves(new float [][] {dtdd, x, dtde, x, dtdg, x});
				dialog.showDialog();
			}
		});
				
		return new JMenuItem[] { jMenuItem1 };	
	}

	protected JComponent getCommandRow() { return null; }
	protected boolean okAction() {	return true;	}
	protected JPanel createContents() {
		JPanel innerPanel = new JPanel();

		innerPanel.setLayout(new GridBagLayout());
		Insets insets = new Insets(5, 5, 5, 5);
		GridBagConstraints gbc= null;
		
		for(int i=0; i<_sliderName.length; i++) {
			gbc= new GridBagConstraints(0, i, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, insets, 0, 0);
			innerPanel.add(new JLabel(_sliderName[i]+":"), gbc);
			final int j = i;
		    JSlider slider = new JSlider();
		    slider.setFocusable(false);
		    slider.addChangeListener(new ChangeListener() {
		    	public void stateChanged(ChangeEvent evt) {
		    		JSlider slider = (JSlider) evt.getSource();
		    		if (!slider.getValueIsAdjusting()) {
		    			int value = slider.getValue();
		    			_sliderValue[j] = _sliderMinMax[j][0]+value*(_sliderMinMax[j][1]-_sliderMinMax[j][0])/100.0;
		    			_valueLabel.setText(getValueString());
		    			updatePlot();
		    		}
		    	}
		    });
		    gbc= new GridBagConstraints(1, i, 1, 1, 1.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, insets, 0, 0);
		    innerPanel.add(slider, gbc);
		}
		gbc= new GridBagConstraints(1, _sliderName.length, 1, 1, 1.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, insets, 0, 0);
	    innerPanel.add(_valueLabel, gbc);
	    
		gbc= new GridBagConstraints(0, _sliderName.length+1, 2, 1, 1.0, 1.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, insets, 0, 0);
	    innerPanel.add(_plot, gbc);
		return innerPanel;
	}

	private void updatePlot() {
		float size = 12;
		float width = 2.0f;
		updateModel();
		String [] names = new String[]{"P", "SH","SV"};
		String [] styles = new String[]{"r", "k", "b"};
		double min = 1.0e10;
		double max = -min;
		double a = 0;
		for(int i=0; i<names.length; i++) {
			_rt.setIVp(i+1); _rt.setConfiguration(); _rt.start(1);
			SeismicVel sv = _rt.getSeismicVel();
			double [] x = sv.getTable(0)[0]; 		// phase angle
			double [] y = sv.getPhaseVelTable(0); 	// phase velocity
			a = ArrayMath.min(y);
			min = min<a?min:a;
			a = ArrayMath.max(y);
			max = max>a?max:a;
			float [] fx = new float[y.length];
			float [] fy = new float[y.length];
			float [] fx0 = new float[y.length];
			float [] fy0 = new float[y.length];
			double y0 = y[0];
			for(int j=0; j<fy.length; j++) {
				fx[j] = (float)(y[j]*Math.sin(x[j]));
				fy[j] = (float)(y[j]*Math.cos(x[j]));
				fx0[j] = (float)(y0*Math.sin(x[j]));
				fy0[j] = (float)(y0*Math.cos(x[j]));
			}

			PointsView pv = new PointsView(fx, fy);
			pv.setName("Points", "q"+names[i]);
			pv.setStyle(styles[i]+"-");
			pv.setLineWidth(width);
			_plot.removeView(pv.getCategory(), pv.getName());
			_plot.addPointsView(0, 0, pv);
			
			pv = new PointsView(fx0, fy0);
			pv.setName("Points", names[i]);
			pv.setStyle(styles[i]+"--");
			pv.setLineWidth(width);
			_plot.removeView(pv.getCategory(), pv.getName());
			_plot.addPointsView(0, 0, pv);
		}
	    //_panel.revalidate();
	}
	
	private void updateModel() {
		updateModel(1.0, 1.0, 1.0, 1.0, 1.0);
	}
	private void updateModel(double vpFraction, double vsFraction, double deltaFraction, 
			double epsilonFraction, double gammaFraction){
		FlatLayer1D layer1D 	= _rt.getFlatLayer1D();
		for(int j=0; j<layer1D.getNumOfBoundaries(); j++) {
			int k = 0;
			layer1D.getLayer(j).setVp(_sliderValue[k++]*vpFraction);
			layer1D.getLayer(j).setVs(_sliderValue[k++]*vsFraction);
			layer1D.getLayer(j).setDelta(_sliderValue[k++]*deltaFraction);
			layer1D.getLayer(j).setEpsilon(_sliderValue[k++]*epsilonFraction);
			layer1D.getLayer(j).setGamma(_sliderValue[k++]*gammaFraction);
		}
	}
	private DipLayer1D genSyntheticModel() {
		int nBoundaries = 3;
		DipLayer1D dipLayer1D = new DipLayer1D(nBoundaries);
		dipLayer1D.setIUnit(2);
		for(int i=0; i<nBoundaries; i++) {
			SingleFlatLayer layer = new SingleFlatLayer();
			layer.setLayerName("S"+i);
			layer.setDen(2500);
			layer.setId(i);
			layer.setTDepth(i*1000);
			int k = 0;
			layer.setVp(_sliderValue[k++]);
			layer.setVs(_sliderValue[k++]);
			layer.setDelta(_sliderValue[k++]);
			layer.setEpsilon(_sliderValue[k++]);
			layer.setGamma(_sliderValue[k++]);
			
			
			layer.setX0(-5000);
			layer.setX1(5000);
			layer.setY0(-5000);
			layer.setY1(5000);
			layer.setZ0(0);
			layer.setZ1(2000);
			layer.setDx(10);
			layer.setDy(10);
			layer.setDz(10);
			
			dipLayer1D.add(layer);
		}
		
		dipLayer1D.setAzimuth(0);
		dipLayer1D.setDip(0);
		
		return dipLayer1D;
	}

	private String getValueString() {
		int k = 0;
		return String.format("vp(%10.2f ) vs(%10.2f ) delat(%10.3f ) epsilon(%10.3f ) gamma(%10.3f )", 
				_sliderValue[k++], _sliderValue[k++],_sliderValue[k++],_sliderValue[k++],_sliderValue[k++]);
	}
	private VCPair genSyntheticVCPair() {
		VCPair vcPW = new VCPair(new double [] {0}, new double [] {0}, new double [] {50}, 
				new double [] {1000, 1000}, new double [] {1000, 1000}, new double [] {500, 600}, 2);
		return vcPW; 		
	}
	private Pseudo3DRayTracer genRayTracing() {
		DipLayer1D dipLayer1D = genSyntheticModel();
		VCPair vcPW = genSyntheticVCPairCircle();
		Pseudo3DRayTracer rt = new Pseudo3DRayTracer(null, dipLayer1D, vcPW, 
				1, 1, 0, null, null, 1, 0.01, 0, 0, 2);
		rt.setIMethod(0);
		rt.setIVp(1); rt.setConfiguration(); rt.start(0);
		rt.setIVp(2); rt.setConfiguration(); rt.start(0);
		rt.setIVp(3); rt.setConfiguration(); rt.start(0);

		return rt;
	}
	
	private VCPair genSyntheticVCPairCircle() {
		int n = 90;
		double dtheta = Math.PI/180.0;
		double theta = 0;
		double r = 800;
		double [] rx = new double[n];
		double [] ry = new double[n];
		double [] rz = new double[n];
		for(int i=0; i<n; i++) {
			theta = dtheta*i;
			rx[i] = r*Math.sin(theta);
			ry[i] = 0;
			rz[i] = 50+r*Math.cos(theta);
		}
		
		VCPair vcPW = new VCPair(new double [] {0}, new double [] {0}, new double [] {50}, 
				rx, ry, rz, 2);
		return vcPW; 		
	}

}

