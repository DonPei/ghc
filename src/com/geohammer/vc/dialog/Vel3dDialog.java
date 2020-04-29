package com.geohammer.vc.dialog;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;

import com.geohammer.common.CommonDialog;
import com.geohammer.vc.VcFrame;
import com.geohammer.vc.Vel3dPanel;

public class Vel3dDialog extends CommonDialog {
	
	private VcFrame 	_frame 		= null;
	
	public Vel3dDialog(JFrame aParent, String aTitle, boolean modal) {
		super(aParent, aTitle, modal);
		setDialogWindowSize(1600, 800);
		_frame 			= (VcFrame)aParent;
	}

	protected JComponent getCommandRow() { return null; }
	protected boolean okAction() {	return true;	}
	protected JPanel createContents() {
//		return new Vel3dPanel(_frame, _frame.getProject().getDipLayer1D(),
//				_frame.getProject().getLayer3D(), _frame.getProject().getVCPair(), null);
		return new Vel3dPanel(_frame, _frame.getProject().getDipLayer1D(),
				_frame.getProject().getDipLayer1D().toLayer3D(), _frame.getProject().getVCPair(), null);
	}

}
