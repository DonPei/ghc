package com.geohammer.vc.run;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.geohammer.component.StatusBar;

import com.geohammer.common.CommonDialog;

import edu.mines.jtk.dsp.Sampling;

public class StrippingPlot extends CommonDialog {
	private StrippingDialog 	_strippingDialog 		= null;
	private StrippingPixelPanel _strippingPixelPanel 	= null;

	int 			_iType 		= 0;
	int 			_nPoints 	= 0;
	double [][] 	_steps 		= null;
	String 			_hLabel 	= null;
	String 			_vLabel 	= null;
	
	public StrippingPlot(StrippingDialog aParent, boolean modal, int iType , String aTitle, 
			String hLabel, String vLabel, double [][] steps) {
		super(aParent.getFrame(), aTitle, modal);
		setDialogWindowSize(800, 800);
		_strippingDialog = (StrippingDialog)aParent;
		_iType  = iType;
		_hLabel  = hLabel;
		_vLabel  = vLabel;
		_steps  = steps;
	}
	protected JComponent getCommandRow() { return null; }
	protected boolean okAction() {	return true;	}
	protected JPanel createContents() {
		Sampling secondIndexSampling = new Sampling(_steps[0]); //m
		Sampling firstIndexSampling = new Sampling(_steps[1]);
		

		StatusBar statusBar = new StatusBar(); 
		statusBar.setZoneBorder(BorderFactory.createLineBorder(Color.GRAY)); 
		statusBar.setZones( new String[] { "first_zone","remaining_zones"},     
				new Component[] { new JLabel("first"), new JLabel("remaining")},     
				new String[] {"40%", "*"} );

		JLabel minValueLabel 	= (JLabel)statusBar.getZone("first_zone");	
		JLabel trackingLabel 	= (JLabel)statusBar.getZone("remaining_zones");	
		_strippingPixelPanel = new StrippingPixelPanel(_strippingDialog.getFrame(), _hLabel, _vLabel, 
				firstIndexSampling, secondIndexSampling, minValueLabel, trackingLabel); 
		
		JPanel panel = new JPanel();
		panel.setBackground(Color.white);
		panel.setLayout(new BorderLayout());
		panel.add(_strippingPixelPanel, BorderLayout.CENTER);
		panel.add(statusBar, BorderLayout.SOUTH);
		
		return panel;
	}
	public StrippingPixelPanel getStrippingPixelPanel() { return _strippingPixelPanel; }
	public void update(double [][] rms) {
		_strippingPixelPanel.update(rms);
	}
	

	@Override
	public void setVisible(boolean visible) {
		super.setVisible(visible);

		if(visible) {
			GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
			GraphicsDevice gd = ge.getDefaultScreenDevice();
			GraphicsConfiguration [] gc = gd.getConfigurations();
			Rectangle gcBounds = gc[0].getBounds();

			//Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize();
			//int w = dimension.width / 2;
			//int h = 3*dimension.height / 4;
			//System.out.println("w=" + w + " h=" + h + " 1=" + dimension.width + " 2=" + dimension.height);

			int w = (int)(6.0*gcBounds.getWidth() / 10.0);
			int h = (int)(8.0*gcBounds.getHeight() / 10.0);
			//int h = (int)(w/2);
			//int h = (int)(gcBounds.getHeight()-40.0);
			//setSize((int)(gcBounds.getWidth()-10), (int)(gcBounds.getHeight()/2));
			if(_iType==0||_iType==1||_iType==2) {
				setSize(w, h);
			} else {
				int s = w<h?w:h;
				setSize(s, s);
			}
			//setSize(1000, 800);

			setLocationRelativeTo(null);
		}
	}
}
