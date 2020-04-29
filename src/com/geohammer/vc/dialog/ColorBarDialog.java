package com.geohammer.vc.dialog;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;

import com.geohammer.common.CommonDialog;
import com.geohammer.core.planarmodel.Layer2D;
import com.geohammer.core.planarmodel.Layer3D;
import com.geohammer.vc.VcFrame;
import com.geohammer.vc.Vel2dPanel;
import com.geohammer.vc.Vel2dView;
import com.geohammer.vc.Vel3dPanel;

public class ColorBarDialog extends CommonDialog {
	
	private VcFrame 	_frame 		= null;
	private String 		_name 		= null;
	
	public ColorBarDialog(JFrame aParent, String aTitle, boolean modal) {
		super(aParent, aTitle, modal);
		setDialogWindowSize(250, 800);
		_frame 		= (VcFrame)aParent;
	}

	public void setName(String name) { _name = name; }
	
	protected JComponent getCommandRow() { return null; }
	protected boolean okAction() {	return true;	}
	protected JPanel createContents() {	
		int nBoundary = 0;
		if(_frame.getProject().is2D()) {
			Layer2D layer2D = _frame.getProject().getLayer2D();
			nBoundary = layer2D.getNumOfBoundaries();
		} else {
			Layer3D layer3D = _frame.getProject().getLayer3D();
			nBoundary = layer3D.getNumOfBoundaries();
		}

		JPanel jPanel = new JPanel(new GridLayout(nBoundary, 2, 5, 5));
		if(_frame.getProject().is2D()) {
			Layer2D layer2D = _frame.getProject().getLayer2D();
			nBoundary = layer2D.getNumOfBoundaries();
			Vel2dPanel panel = _frame.getVel2dPanel();
			for(int i=0; i<nBoundary; i++) {
				JLabel bk = new JLabel("");
				Vel2dView view = (Vel2dView)panel.getView("Layer");
				bk.setBackground(view.getColor(view.getLayerVel(i)));
				bk.setOpaque(true);
				jPanel.add(bk);
				jPanel.add(new JLabel(view.getLayerVel(i)+""));
			}
		} else {
			Vel3dPanel panel = _frame.getVel3dPanel();
			for(int i=0; i<nBoundary; i++) {
				JLabel bk = new JLabel("");
				bk.setBackground(panel.getLayerColor(i));
				bk.setOpaque(true);
				jPanel.add(bk);
				jPanel.add(new JLabel(panel.getVel(i)+""));
			}
		}
		
		JScrollPane scroll = new JScrollPane(jPanel);
		scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED); 
		scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED); 
		//scroll.setMinimumSize(new Dimension(400, 300));     
		//scroll.setPreferredSize(new Dimension(400, 300));
		
		JPanel jPanel1 = new JPanel(new BorderLayout());
		JLabel title = new JLabel(_name, JLabel.CENTER);
		title.setBorder(new EmptyBorder(10, 5, 10, 5));
		title.setFont(new Font ("Arial", Font.BOLD, 14));
		jPanel1.add(title, BorderLayout.NORTH);
		jPanel1.add(scroll, BorderLayout.CENTER);
		return jPanel1;
	}
}
