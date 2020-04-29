package com.geohammer.launcher.dialog;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.StringTokenizer;

import javax.swing.DefaultCellEditor;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;

public class LabRecordTable extends JTable{
	
	private LabRecordTableData 	_data 	= null;
	private Clipboard 			_system	= null;
	private StringSelection 	_stsel	= null;
	
	public LabRecordTable(String [] category, String [] lithology, double [][] properties) {
		setSelectionModel( new NullSelectionModel() );
		JTextField field=new JTextField();
		field.requestFocus();
		
		_data = new LabRecordTableData(category, lithology, properties);
		setAutoCreateColumnsFromModel(false);
		setModel(_data); 
		for (int k = 0; k <_data._columns.length; k++) {
			//MyTableCellRenderer renderer = new MyTableCellRenderer();
			DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
			DefaultCellEditor editor = new DefaultCellEditor(new JTextField());
			//editor.setClickCountToStart(1);
			//MyTableCellEditor editor = new MyTableCellEditor(new JTextField());
			renderer.setHorizontalAlignment(_data._columns[k]._alignment);
			TableColumn column = new TableColumn(k, _data._columns[k]._width, renderer, editor);
			//column.setPreferredWidth(100);
			addColumn(column); 
		}
		setRowHeight(30);
		setCellSelectionEnabled(true);
		getTableHeader().setReorderingAllowed(false);
		
		_system = Toolkit.getDefaultToolkit().getSystemClipboard();
		
		addKeyListener(new KeyAdapter(){
			public void keyPressed(KeyEvent e) {
				String modifiers = e.getKeyModifiersText(e.getModifiers());
		        if (e.getKeyCode() == KeyEvent.VK_C && modifiers.equalsIgnoreCase("CTRL"))  {  
		        	copy(); 
		        } else if (e.getKeyCode() == KeyEvent.VK_V && modifiers.equalsIgnoreCase("CTRL"))  {
		        	paste(); 
		        } else if (e.getKeyCode() == KeyEvent.VK_X && modifiers.equalsIgnoreCase("CTRL"))  {
		        	cutAndCopy(); 
		        } else { }
			}
			public void keyReleased(KeyEvent e) { }
			public void keyTyped(KeyEvent e) 	{ }
		});
	}
	public LabRecordTableData getData() { return _data; }
	public void copy() {
		StringBuffer sbf=new StringBuffer();
		// Check to ensure we have selected only a contiguous block of
		// cells
		int numcols=getSelectedColumnCount();
		int numrows=getSelectedRowCount();
		int[] rowsselected=getSelectedRows();
		int[] colsselected=getSelectedColumns();
		if (!((numrows-1==rowsselected[rowsselected.length-1]-rowsselected[0] &&
				numrows==rowsselected.length) &&
				(numcols-1==colsselected[colsselected.length-1]-colsselected[0] &&
						numcols==colsselected.length)))
		{
			JOptionPane.showMessageDialog(null, "Invalid Copy Selection",
					"Invalid Copy Selection",
					JOptionPane.ERROR_MESSAGE);
			return;
		}
		for (int i=0;i<numrows;i++) {
			for (int j=0;j<numcols;j++) {
				sbf.append(getValueAt(rowsselected[i],colsselected[j]));
				if (j<numcols-1) sbf.append("\t");
			}
			sbf.append("\n");
		}
		_stsel  = new StringSelection(sbf.toString());
		_system.setContents(_stsel, _stsel);
	}
	
	public void paste()	{
		String rowstring;
		String value;
		//System.out.println("Trying to Paste");
		int startRow=(getSelectedRows())[0];
		int startCol=(getSelectedColumns())[0];
		try {
			String trstring= (String)(_system.getContents(this).getTransferData(DataFlavor.stringFlavor));
			StringTokenizer st1=new StringTokenizer(trstring,"\n");
			for(int i=0;st1.hasMoreTokens();i++) {
				rowstring=st1.nextToken();
				StringTokenizer st2=new StringTokenizer(rowstring,"\t");
				for(int j=0;st2.hasMoreTokens();j++){
					value=st2.nextToken();
					if (startRow+i<getRowCount()  && startCol+j<getColumnCount())
						setValueAt(value,startRow+i,startCol+j);
					//System.out.println("Putting "+ value+"at row="+startRow+i+"column="+startCol+j);
				}
			}
		} catch(Exception ex){}
	}
	
	public void cutAndCopy() {
		StringBuffer sbf=new StringBuffer();
		int numcols=getSelectedColumnCount();
		int numrows=getSelectedRowCount();
		int[] rowsselected=getSelectedRows();
		int[] colsselected=getSelectedColumns();
		if (!((numrows-1==rowsselected[rowsselected.length-1]-rowsselected[0] &&
				numrows==rowsselected.length) &&
				(numcols-1==colsselected[colsselected.length-1]-colsselected[0] &&
						numcols==colsselected.length)))
		{
			JOptionPane.showMessageDialog(null, "Invalid Selection",
					"Invalid Selection", JOptionPane.ERROR_MESSAGE);
			return;
		}
		for (int i=0;i<numrows;i++) {
			for (int j=0;j<numcols;j++) {
				sbf.append(getValueAt(rowsselected[i],colsselected[j]));
				if (j<numcols-1) sbf.append("\t");
				setValueAt("",rowsselected[i],colsselected[j]);
			}
			sbf.append("\n");
		}
		_stsel  = new StringSelection(sbf.toString());
		_system.setContents(_stsel, _stsel);		
	}
	
	class ColumnData {
		public String _title;
		public int _width;
		public int _alignment;
		public ColumnData(String title, int width, int alignment) {
			_title = title;
			_width = width;
			_alignment = alignment;
		}
	}
	
	class LabRecordTableData extends AbstractTableModel {
		
		final public ColumnData _columns[] = {
			new ColumnData( "1-No", 30, JLabel.LEFT ),
			new ColumnData( "2-Category", 260, JLabel.LEFT ),
			new ColumnData( "3-Lithology", 60, JLabel.LEFT ),
			new ColumnData( "4-Depth", 80, JLabel.CENTER ),
			new ColumnData( "5-Vp", 120, JLabel.CENTER ),
			new ColumnData( "6-Vs", 100, JLabel.CENTER ),
			new ColumnData( "7-Density", 80, JLabel.CENTER ),
			new ColumnData( "8-Delta", 80, JLabel.CENTER ),
			new ColumnData( "9-Epsilon", 80, JLabel.CENTER ),
			new ColumnData( "10-Gamma", 80, JLabel.CENTER )
		};
	
		double [][] 	_properties 	= null;
		String [] 		_category 		= null;
		String [] 		_lithology 		= null;
		
		public LabRecordTableData(String [] category, String [] lithology, double [][] properties) {
			initData(category, lithology, properties);
		}
		public void initData(String [] category, String [] lithology, double [][] properties)	{
			_category		= category;
			_lithology		= lithology;
			_properties		= properties;
		}
		
		public int getRowCount() 							{ return _category.length;  }
		public int getColumnCount() 						{ return _columns.length; } 
		public String getColumnName(int column) 			{ return _columns[column]._title; }
		public boolean isCellEditable(int iRow, int iCol) 	{ return false; }
		public Object getValueAt(int iRow, int iCol) {
			if (iRow < 0 || iRow >= getRowCount()) 
				return "";
			if(iCol==0)  return iRow+1;
			else if(iCol==1)  return _category[iRow];
			else if(iCol==2)  return _lithology[iRow];
			else if(iCol>=3&&iCol<=9)  return _properties[iRow][iCol-3];
			else   return "";
		}
		
		public void setValueAt(Object value, int iRow, int iCol) {
			double v = 0.0;
			try{
				if(iCol!=1) v = Double.parseDouble(value.toString());
			} catch(NumberFormatException e) { return; }
			if(iCol==0) { }
			else if(iCol==1) _category[iRow]=value.toString();
			else if(iCol==2) _lithology[iRow]=value.toString();
			else if(iCol>=3&&iCol<=9)  _properties[iRow][iCol-3] = v;
			else {} 
			fireTableCellUpdated(iRow, iCol);
		}	
	}
	
	public class NullSelectionModel implements ListSelectionModel {
		public boolean isSelectionEmpty() { return true; }
		public boolean isSelectedIndex(int index) { return false; }
		public int getMinSelectionIndex() { return -1; }
		public int getMaxSelectionIndex() { return -1; }
		public int getLeadSelectionIndex() { return -1; }
		public int getAnchorSelectionIndex() { return -1; }
		public void setSelectionInterval(int index0, int index1) { }
		public void setLeadSelectionIndex(int index) { }
		public void setAnchorSelectionIndex(int index) { }
		public void addSelectionInterval(int index0, int index1) { }
		public void insertIndexInterval(int index, int length, boolean before) { }
		public void clearSelection() { }
		public void removeSelectionInterval(int index0, int index1) { }
		public void removeIndexInterval(int index0, int index1) { }
		public void setSelectionMode(int selectionMode) { }
		public int getSelectionMode() { return SINGLE_SELECTION; }
		public void addListSelectionListener(ListSelectionListener lsl) { }
		public void removeListSelectionListener(ListSelectionListener lsl) { }
		public void setValueIsAdjusting(boolean valueIsAdjusting) { }
		public boolean getValueIsAdjusting() { return false; }
	}
}


