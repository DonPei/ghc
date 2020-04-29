package com.geohammer.common;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import com.geohammer.common.CommonDialog;
import com.geohammer.common.CommonFrame;
import com.geohammer.common.CommonMouseEditingMode;
import com.geohammer.common.CommonPointPanel;
import com.geohammer.core.acquisition.VCPair;
import com.geohammer.core.planarmodel.DipLayer1D;

public class LogLogDialog extends CommonDialog {
	
	private CommonFrame 		_frame 		= null;
	private LogLogPanel 		_plot 		= null;
	private DipLayer1D 			_dipLayer1D = null;
	public VCPair 				_vcPW 		= null;
	private String 				_title 		= null;
	
	private int 				_iH 		= 0;
	private int 				_iV 		= 1;
	
	public float [][] 			_curves		= null;
	
	public LogLogDialog(JFrame aParent, String aTitle, boolean modal,
			int iType, String catalog, String hLabel, String vLabel) {
		this(1, 1, aParent, aTitle, modal,
				iType, catalog, hLabel, vLabel);
	}
	public LogLogDialog(int nRow, int nCol, JFrame aParent, String aTitle, boolean modal,
			int iType, String catalog, String hLabel, String vLabel) {
		super(aParent, aTitle, modal);
		setBackground(Color.white);
		setDialogWindowSize(800, 800);		
		_frame 		= (CommonFrame)aParent;
		_title 		= aTitle;
		
		_plot 		= new LogLogPanel(nRow, nCol, iType, catalog, hLabel, vLabel);
		//_plot.setBorder(new EmptyBorder(3, 1, 3, 100));
		_plot.setBackground(Color.white);
//		_plot.setFrame(_frame);		
//
//		_plot.setEnableTracking(false);
//		_plot.setEnableEditing(true);
//		_plot.setEnableZoom(true);
//		
//		_plot.addModeManager();
//		CommonMouseEditingMode mouseEditingMode = _plot.getMouseEditingMode();
//		//mouseEditingMode.setShowAutoAxisLimit(false);
//		mouseEditingMode.setShowManualAxisLimit(true);
//		mouseEditingMode.setJComponent(genJMenuItem());	
	}
	
	public void setIH(int iH) 					{ _iH = iH; }
	public void setIV(int iV) 					{ _iV = iV; }
	public void setVcPair(VCPair vcPW) 			{ _vcPW = vcPW; }
	public void setCurves(float [][] curves) 	{ _curves = curves; }
	
	public LogLogPanel getLogLogPanel() { return _plot; }
	protected JComponent getCommandRow() { return null; }
	protected boolean okAction() {	return true;	}
	
		
	protected JPanel createContents() {		
		
		
		double [] hValue = null;
		double [] vValue = null;
		String hLabel = null;
		String vLabel = null; 
		String style = null;
		String name = null;
		
		float lineWidth = 1.0f;
		float markSize = 12.0f;
		String [] styles = new String[]{"r-", "g-", "b-"};
		String [] names = new String[]{"r-", "g-", "b-"};
		int nc = _curves.length/2;
		for(int i=0; i<nc; i++) {	
			_plot.add(0,0,_curves[2*i+1], _curves[2*i], 0, hLabel, vLabel, styles[i], 
					names[i], markSize, lineWidth, false, false);
			_plot.adjustAxislimit(0.2, 0.2, 0.2, 0.2);
		}
		
		//_plot.addBackgroundGrid();
		return _plot;
	}
	
	private JMenuItem[] genJMenuItem() {
		//int iType = _plot.getIType();
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
