package com.geohammer.launcher.dialog;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.geohammer.common.CommonDialog;
import com.geohammer.common.CommonFrame;
import com.geohammer.common.util.UiUtil;
import com.geohammer.launcher.LauncherFrame;
import com.geohammer.launcher.dialog.LabRecordTable.LabRecordTableData;
import com.geohammer.vc.dialog.PlotDialog;

public class LabRecordDialog  extends CommonDialog {
	LauncherFrame 		_frame 		= null;

	String [] 			_catagory 	= null;
	String [] 			_lithology 	= null;
	double [][] 		_properties = null;

	LabRecordTable	[]	_tables 	= null;
	private String 	[]	_tabNames	= null;
	private int 		_currTabIndex = 0;
	
	private JTextField _rowTF1 = null;
	private JTextField _rowTF2 = null;
	private JTextField _colTF1 = null;
	private JTextField _colTF2 = null;
	
	public LabRecordDialog(CommonFrame aParent, String aTitle, boolean modal, 
			String [] catagory, String [] lithology, double [][] properties) {
		super(aParent, aTitle, modal);
		setDialogWindowSize(1200, 650);
		_frame 			= (LauncherFrame)aParent;
		_catagory		= catagory;
		_lithology		= lithology;
		_properties		= properties;
		
		_tabNames 		= new String[]{"Literature Data"};
		//_tabNames 		= new String[]{"Literature Data"};
		_tables 		= new LabRecordTable[_tabNames.length];
		for(int i=0; i<_tabNames.length; i++) _tables[i] 	= new LabRecordTable(catagory, lithology, properties);
		
		
		_rowTF1 		= new JTextField("1", 5);
		_rowTF2 		= new JTextField(catagory.length+"", 5);
		_colTF1 		= new JTextField("8", 5);
		_colTF2 		= new JTextField("9", 5);
	}
	
	//protected JComponent getCommandRow() { return null; }
	protected boolean okAction() {	return true;	}
	protected JTabbedPane createContents() {
		JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP, JTabbedPane.SCROLL_TAB_LAYOUT);
		tabbedPane.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent evt) {
				JTabbedPane pane = (JTabbedPane)evt.getSource();
				_currTabIndex = pane.getSelectedIndex();
			}
		});

		for(int i=0; i<_tables.length; i++) {
			tabbedPane.addTab("  "+_tabNames[i]+"      ", getTableTab(_tables[i]));
		}

		return tabbedPane;
	}
	private JScrollPane getTableTab(LabRecordTable table) {
		JScrollPane scrollPane = new JScrollPane(table);
		scrollPane.setAlignmentX(LEFT_ALIGNMENT); 
		scrollPane.setColumnHeaderView(null);
		scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		scrollPane.setWheelScrollingEnabled(true);
		scrollPane.setMinimumSize(new Dimension(1200, 650));     
		scrollPane.setPreferredSize(new Dimension(1200, 650));
		return scrollPane;
	}

	protected JComponent getCommandRow() {
		JButton ok = new JButton("Cross Plot");
		ok.addActionListener( new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				crossPlot();
			}
		});
		JButton cancel = new JButton("Cancel");
		cancel.addActionListener( new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				dispose();
			}
		});
		this.getRootPane().setDefaultButton( ok );
		List<JComponent> buttons = new ArrayList<JComponent>();
		//buttons.add( ok );
		buttons.add( new JLabel("Row 1:", JLabel.RIGHT) );
		buttons.add( _rowTF1 );
		buttons.add( new JLabel("Row 2:", JLabel.RIGHT) );
		buttons.add( _rowTF2 );
		buttons.add( new JLabel("Col 1 (Horizontal):", JLabel.RIGHT) );
		buttons.add( _colTF1 );
		buttons.add( new JLabel("Col 2 (Vertical):", JLabel.RIGHT) );
		buttons.add( _colTF2 );
		buttons.add( new JLabel("     ") );
		buttons.add( ok );
		buttons.add( cancel );
		return UiUtil.getCommandRow( buttons );
	}
	
	public void crossPlot() {
		LabRecordTableData table = _tables[_currTabIndex].getData();
		int iRow = Integer.parseInt(_rowTF1.getText().trim())-1;
		int jRow = Integer.parseInt(_rowTF2.getText().trim())-1;
		int iCol = Integer.parseInt(_colTF1.getText().trim())-1;
		int jCol = Integer.parseInt(_colTF2.getText().trim())-1;
		
		if(jRow<iRow) return;
		
		if(iRow<0) iRow = 0;
		if(jRow>=table.getRowCount()) jRow = table.getRowCount()-1;
		if(iCol<0) iCol = 0;
		if(jCol>=table.getColumnCount()) jCol = table.getColumnCount()-1;
		
		float [] ax = new float[jRow-iRow+1];
		float [] ay = new float[jRow-iRow+1];
		for(int i=iRow, k=0; i<=jRow; i++, k++) {
			ax[k] = Float.parseFloat(table.getValueAt(i, iCol).toString());
			ay[k] = Float.parseFloat(table.getValueAt(i, jCol).toString());
		}
		
		int k=0;
		for(int i=0; i<ax.length; i++) {
			if(ax[i]!=-99999.0f&&ay[i]!=-99999.0f) k++;
		}
		//System.out.println("iCol="+iCol+" jCol="+jCol+" k="+k);
		float [] bx = new float[k];
		float [] by = new float[k];
		k = 0;
		for(int i=0; i<ax.length; i++) {
			if(ax[i]!=-99999.0f&&ay[i]!=-99999.0f) {
				bx[k] = ax[i];
				by[k] = ay[i];
				k++;
			}
		}
		String [] label = table.getColumnName(iCol).split("-");
		String hLabel = label[1];
		label = table.getColumnName(jCol).split("-");
		String vLabel = label[1];
		
		String aTitle = "Cross Plot";
		String catalog = "Data";
		PlotDialog dialog = new PlotDialog(_frame, aTitle, false, null,
				110, false, catalog, hLabel, vLabel);
		dialog.setCurves(new float[][]{by, bx});
		dialog.showDialog();
		
	}
}

