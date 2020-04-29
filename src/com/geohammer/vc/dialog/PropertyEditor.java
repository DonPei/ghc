package com.geohammer.vc.dialog;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.apache.commons.io.FilenameUtils;
import com.geohammer.common.CommonDialog;
import com.geohammer.common.VeconUtils;
import com.geohammer.core.planarmodel.SingleLayer2D;
import com.geohammer.core.planarmodel.SingleLayer3D;
import com.geohammer.vc.VcFrame;
import com.geohammer.vc.Vel2dView;

public class PropertyEditor extends CommonDialog {
	
	private VcFrame 		_frame 		= null;
	
	private PropertyTable 	_table 		= null;	
	private boolean 		_isEditable = false;
	private String [] 		_layerName 	= null;
	private double 			_azimuth	= 0;
	private double 			_dip		= 0;
	
	public PropertyEditor(JFrame aParent, String aTitle, boolean modal, boolean isEditable) {
		super(aParent, aTitle, modal);
		setDialogWindowSize(1000, 800);	
		setEnableSaveButton(true);
		setEnableApplyButton(true);
		setApplyButtonLabel("Update Layer");
		_frame 		= (VcFrame)aParent;
		_isEditable = isEditable;
	}

	//protected JComponent getCommandRow() 	{ return null; }
	protected boolean okAction() 			{ 
		if(_frame.getProject().is2D()) updateLayer2D();
		else updateLayer3D();
		return true;	
	}
	
	//http://stackoverflow.com/questions/12823475/what-is-the-best-way-to-listen-for-changes-in-jtable-cell-values-and-update-data
	protected JPanel createContents() {
		if(_frame.getProject().getLayer2D()!=null) _table = createTable2D(_isEditable);
		else _table = createTable3D(_isEditable);
		_table.putClientProperty("terminateEditOnFocusLost", true);
		
		JScrollPane scroll = new JScrollPane(_table);
		scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED); 
		scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED); 
		scroll.setMinimumSize(new Dimension(400, 300));     
		//scroll.setPreferredSize(new Dimension(400, 300));
		
		JPanel jPanel = new JPanel(new BorderLayout());
		jPanel.add(scroll, BorderLayout.CENTER );
		//_htmlPage.setCaretPosition(0);
		return jPanel;
	}
	
	private PropertyTable createTable2D(boolean isEditable) {		
		_layerName = new String[_frame.getProject().getLayer2D().getNumOfBoundaries()];
		double [][] properties = new double[_layerName.length][9];
		for(int i=0; i<_layerName.length; i++) {
			SingleLayer2D sl = _frame.getProject().getLayer2D().getLayer(i);
			_layerName[i] 	 = sl.getLayerName();
			properties[i][0] = sl.getCurve().getFirstZ();
			properties[i][1] = sl.getCurve().getLastZ();
			properties[i][2] = sl.getVp();
			properties[i][3] = sl.getVs();
			properties[i][4] = sl.getVp()/sl.getVs();
			properties[i][5] = sl.getDen();
			properties[i][6] = sl.getDelta();
			properties[i][7] = sl.getEpsilon();
			properties[i][8] = sl.getGamma();
		}

		PropertyTable table = new PropertyTable(_layerName, properties, isEditable);
		return table;
	}
	private PropertyTable createTable3D(boolean isEditable) {		
		_layerName = new String[_frame.getProject().getLayer3D().getNumOfBoundaries()];
		double [][] properties = new double[_layerName.length][10];
		for(int i=0; i<_layerName.length; i++) {
			SingleLayer3D sl = _frame.getProject().getLayer3D().getLayer(i);
			_layerName[i] 	 = sl.getLayerName();
			properties[i][0] = 0;
			properties[i][1] = 0;
			properties[i][2] = sl.getDepth(0, 0);
			properties[i][3] = sl.getVp();
			properties[i][4] = sl.getVs();
			properties[i][5] = sl.getVp()/sl.getVs();
			properties[i][6] = sl.getDen();
			properties[i][7] = sl.getDelta();
			properties[i][8] = sl.getEpsilon();
			properties[i][9] = sl.getGamma();
		}
		_azimuth = _frame.getProject().getLayer3D().getAzimuth();
		_dip = _frame.getProject().getLayer3D().getDip();

		PropertyTable table = new PropertyTable(_layerName, properties, isEditable);
		return table;
	}
	
	public boolean saveAction() {	
		String text = _table.getPropertyTableData().toString();
		FileNameExtensionFilter exts[] = new FileNameExtensionFilter [] {
				new FileNameExtensionFilter("CSV (*.csv)", "csv")
		};
		String tmpName = _frame.getProject().getProjectFileName();
		tmpName = FilenameUtils.getFullPath(tmpName)+FilenameUtils.getBaseName(tmpName)+"_table.csv";
		String fileName = _frame.saveFileUsingJFileChooser(exts, tmpName); 
		if(fileName==null) return false;
		else {
			VeconUtils.saveTextToFile(text, fileName);
		}
		return true;	
	}
	
	
	private void updateLayer3D() {
		//System.out.println("m="+_data.length+" z="+ _data[0].length);
		for(int i=0; i<_layerName.length; i++) {
			SingleLayer3D sl = _frame.getProject().getLayer3D().getLayer(i);
			String a = _table.getValueAt(i, 1).toString();
			//sl.setLayerName(a);
			a = _table.getValueAt(i, 2).toString();
			//sl.setMasterZ(0, Float.valueOf(a));
			a = _table.getValueAt(i, 3).toString();
			//sl.setMasterZ(1, Float.valueOf(a));
			a = _table.getValueAt(i, 5).toString();
			sl.setVp(Double.valueOf(a));

			a = _table.getValueAt(i, 6).toString();
			sl.setVs(Double.valueOf(a));
			sl.setVpToVs(sl.getVp()/sl.getVs());
			//System.out.println("vp="+sl.getVp()+" vs="+sl.getVs()+" vp/vs="+sl.getVpToVs());
			a = _table.getValueAt(i, 8).toString();
			sl.setDen(Double.valueOf(a));
			a = _table.getValueAt(i, 9).toString();
			sl.setDelta(Double.valueOf(a));
			a = _table.getValueAt(i, 10).toString();
			sl.setEpsilon(Double.valueOf(a));
			a = _table.getValueAt(i, 11).toString();
			sl.setGamma(Double.valueOf(a));
		}
		_frame.updateWorld(false);
	}
	private void updateLayer2D() {
		//System.out.println("m="+_data.length+" z="+ _data[0].length);
		for(int i=0; i<_layerName.length; i++) {
			SingleLayer2D sl = _frame.getProject().getLayer2D().getLayer(i);
			String a = _table.getValueAt(i, 1).toString();
			sl.setLayerName(a);
			a = _table.getValueAt(i, 2).toString();
			sl.setMasterZ(0, Float.valueOf(a));
			a = _table.getValueAt(i, 3).toString();
			sl.setMasterZ(1, Float.valueOf(a));
			a = _table.getValueAt(i, 4).toString();
			sl.setVp(Double.valueOf(a));

			a = _table.getValueAt(i, 5).toString();
			sl.setVs(Double.valueOf(a));
			sl.setVpToVs(sl.getVp()/sl.getVs());
			//System.out.println("vp="+sl.getVp()+" vs="+sl.getVs()+" vp/vs="+sl.getVpToVs());
			a = _table.getValueAt(i, 7).toString();
			sl.setDen(Double.valueOf(a));
			a = _table.getValueAt(i, 8).toString();
			sl.setDelta(Double.valueOf(a));
			a = _table.getValueAt(i, 9).toString();
			sl.setEpsilon(Double.valueOf(a));
			a = _table.getValueAt(i, 10).toString();
			sl.setGamma(Double.valueOf(a));
			//System.out.println("col="+i+" v="+ _table.getValueAt(i, j));
			sl.setCurve();
		}
		Vel2dView vel2dView = (Vel2dView)_frame.getVel2dPanel().getView("Layer");
		vel2dView.updateVelLayer(2, 2);
		vel2dView.refresh();
	}
	
	
}
