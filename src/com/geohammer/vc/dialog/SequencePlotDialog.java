package com.geohammer.vc.dialog;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

import com.geohammer.common.CommonDialog;
import com.geohammer.common.CommonFrame;
import com.geohammer.common.CommonMouseEditingMode;
import com.geohammer.common.CommonPanel;

import edu.mines.jtk.mosaic.BarsView;
import edu.mines.jtk.mosaic.SequenceView;

public class SequencePlotDialog extends CommonDialog {
	
	private CommonFrame 		_frame 		= null;
	private CommonPanel 		_plot 		= null;
	private String 				_title 		= null;
	
	private int 				_iH 		= 0;
	private int 				_iV 		= 1;
	private int 				_iType 		= 1;
	
	public float [][][] 		_curves		= null;
	
	public SequencePlotDialog(JFrame aParent, String aTitle, boolean modal,	float [] v) {
		this(1, 1, aParent, aTitle, modal, 1);
		setCurves(new float [][][] {{v}});
	}
	public SequencePlotDialog(int nRow, int nCol, JFrame aParent, String aTitle, boolean modal,	int iType) {
		super(aParent, aTitle, modal);
		setBackground(Color.white);
		setDialogWindowSize(800, 800);		
		_frame 		= (CommonFrame)aParent;
		_title 		= aTitle;
		_iType 		= iType;
		
		_plot 		= new CommonPanel(nRow, nCol);
		Font font = new Font ("Arial", Font.BOLD, 12); //Monospaced, Serif, Dialog, Sanserif
		_plot.setFont(font);
		_plot.setBorder(new EmptyBorder(10, 1, 3, 1));
		_plot.setBackground(Color.white);
		_plot.setFrame(_frame);		

		_plot.setEnableTracking(false);
		_plot.setEnableEditing(true);
		_plot.setEnableZoom(true);
		
		_plot.addModeManager();
		CommonMouseEditingMode mouseEditingMode = _plot.getMouseEditingMode();
		//mouseEditingMode.setShowAutoAxisLimit(false);
		mouseEditingMode.setShowManualAxisLimit(true);
		mouseEditingMode.setJComponent(genJMenuItem());	
	}
	
	public void setIH(int iH) 					{ _iH = iH; }
	public void setIV(int iV) 					{ _iV = iV; }
	public void setCurves(float [][][] curves) 	{ _curves = curves; }
	
	public CommonPanel getCommonPanel() { return _plot; }
	protected JComponent getCommandRow() { return null; }
	protected boolean okAction() {	return true;	}
		
	protected JPanel createContents() {	
		double [] hValue = null;
		double [] vValue = null;
		String hLabel = null;
		String vLabel = null; 
		String style = null;
		String name = null;
		float markSize =12.0f;
		float lineWidth = 2.0f;
		
		if(_iType==1) {
			setDialogWindowSize(800, 500);
			for(int i=0; i<_curves[0].length; i++) {
				SequenceView sv = _plot.addSequence(i,0,_curves[0][i]);
				sv.setColor(Color.RED);
				sv.setZero(SequenceView.Zero.MIDDLE);
			}
		}
		if(_iType==2) {
			setDialogWindowSize(1000, 600);
			_plot.setHLabel(0, "P Wave Window");
			_plot.setHLabel(1, "S Wave Window");
			_plot.setVLabel(0, "SNR(dB) Z");
			_plot.setVLabel(1, "SNR(dB) Y");
			_plot.setVLabel(2, "SNR(dB) X");
			for(int iCol=0; iCol<_curves.length; iCol++) {
				for(int j=0; j<_curves[iCol].length; j++) {
					SequenceView sv = _plot.addSequence(j,iCol,_curves[iCol][j]);
					if(iCol==0) sv.setColor(Color.RED);
					else if(iCol==1) sv.setColor(Color.BLUE);
					sv.setZero(SequenceView.Zero.MIDDLE);
				}
			}
		}
		if(_iType==12) {
			setDialogWindowSize(1000, 600);
			_plot.setHLabel(0, "P Wave Window");
			_plot.setHLabel(1, "S Wave Window");
			_plot.setVLabel(0, "SNR(dB) Z");
			_plot.setVLabel(1, "SNR(dB) Y");
			_plot.setVLabel(2, "SNR(dB) X");
			for(int iCol=0; iCol<_curves.length; iCol++) {
				for(int j=0; j<_curves[iCol].length; j++) {
					BarsView bv = _plot.addBars(j,iCol,_curves[iCol][j]);
					if(iCol==0) bv.setFillColor(Color.RED);
					else if(iCol==1) bv.setFillColor(Color.BLUE);
				}
			}
		}
		
		return _plot;
	}
	
	
	private JMenuItem[] genJMenuItem() {
		int iType = _iType;
		JMenuItem jMenuItem1 = new JMenuItem("Data");		
		jMenuItem1.addActionListener(new ActionListener() 	{
			public void actionPerformed(ActionEvent e)   {
//				HtmlDialog dialog = new HtmlDialog(_frame, _title, false);
//				dialog.addTable(_plot.getHList(), _plot.getVList());
//				dialog.showDialog();
			}
		});
				
		return new JMenuItem[] { jMenuItem1 };	
	}
	public void refresh() {
		SwingUtilities.getWindowAncestor( this ).repaint(); 
	}
	

}
