package com.geohammer.vc.dialog;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ButtonGroup;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import com.geohammer.common.CommonDialog;
import com.geohammer.common.CommonFrame;
import com.geohammer.core.planarmodel.DipLayer1D;

public class PlotSelectionDialog extends CommonDialog {
	
	private CommonFrame 		_frame 		= null;
	private int 				_iType 		= 52;
	private DipLayer1D 			_dipLayer1D = null;
	
	private String [] _label = new String[] {"Vp", "Vs", "Vp/Vs", "Density", "Delta", "Epsilon", "Gamma", 
			"E(h)", "E(v)", "P(h)", "P(h, v)", "P(v, h)"};
	private String [] _tooTips = new String[] {"vertical P-wave velocity", "vertical S-wave velocity", 
			"Vp/Vs", "Density", "Delta", "Epsilon", "Gamma", 
			"horizontal Young's modulus", 
			"vertical Young's modulus", 
			"horizontal Poisson's ratio", 
			"Poisson's ratio of vertical expansion from horizontal compression", 
			"Poisson's ratio of horizontal expansion from vertical compression"};
	
	private int _iH = 0;
	private int _iV = 1;
	private boolean _isDepthPlot = false;
	
	public PlotSelectionDialog(JFrame aParent, String aTitle, boolean modal, int iType, boolean isDepthPlot, DipLayer1D dipLayer1D) {
		super(aParent, aTitle, modal);
		setDialogWindowSize(400, 500);		
		_frame 		= (CommonFrame)aParent;
		_iType 		= iType;
		_dipLayer1D = dipLayer1D;
		_isDepthPlot = isDepthPlot;
	}

	//protected JComponent getCommandRow() { return null; }
	protected boolean okAction() {			
		PlotDialog dialog = new PlotDialog(_frame, "Others", false, _dipLayer1D,
				_iType, _isDepthPlot, "Cross", _label[_iH], _label[_iV]);
		dialog.setIH(_iH);
		dialog.setIV(_iV);
		dialog.showDialog();
		return true;	
		
	}
	protected JPanel createContents() {
		int n = _label.length;
		
		JPanel jPanel = new JPanel(new GridLayout(n+1, 2, 5, 5));
		jPanel.add(new JLabel("Horizontal Coordinates"));
		jPanel.add(new JLabel("Vertical Coordinates"));
		
		ButtonGroup moduleRadioGroupH = new ButtonGroup();
		JRadioButton [] moduleRadioButtonH = new JRadioButton[n];
		for(int i=0; i<n; i++) {
			final int j = i;				
			moduleRadioButtonH[i] = new JRadioButton(_label[i], _iH==j);
			moduleRadioButtonH[i].addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent event) {
					if(((JRadioButton)event.getSource()).isSelected()) _iH = j; 
				}
			});
			moduleRadioGroupH.add(moduleRadioButtonH[i]);
		}
		
		ButtonGroup moduleRadioGroupV = new ButtonGroup();
		JRadioButton [] moduleRadioButtonV = new JRadioButton[n];
		for(int i=0; i<n; i++) {
			final int j = i;				
			moduleRadioButtonV[i] = new JRadioButton(_label[i], _iV==j);
			moduleRadioButtonV[i].addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent event) {
					if(((JRadioButton)event.getSource()).isSelected()) _iV = j; 
				}
			});
			moduleRadioGroupV.add(moduleRadioButtonV[i]);
		}
		
		for(int i=0; i<n; i++) {
			jPanel.add(moduleRadioButtonH[i]);
			jPanel.add(moduleRadioButtonV[i]);
		}
		
		return jPanel;
	}
	
}
