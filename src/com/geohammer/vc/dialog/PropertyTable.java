package com.geohammer.vc.dialog;

import java.awt.Component;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.StringTokenizer;

import javax.swing.DefaultCellEditor;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableColumn;

import com.geohammer.core.planarmodel.DipLayer1D;
import com.geohammer.core.planarmodel.SingleFlatLayer;

public class PropertyTable extends JTable{

	private PropertyTableData 	_data 	= null;
	private Clipboard 			_system	= null;
	private StringSelection 	_stsel	= null;

	public PropertyTable(String [] layerName, double [][] properties, boolean isEditable) {

		JTextField field=new JTextField();
		field.requestFocus();
		addDeletePreviousOnEditBehavior(field);

		_data = new PropertyTableData(layerName, properties, isEditable);
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

	//	public Component prepareRenderer(TableCellRenderer renderer, int rowIndex, int vColIndex) {
	//		Component c = super.prepareRenderer(renderer, rowIndex, vColIndex);
	//		if (rowIndex % 2 == 0 && !isCellSelected(rowIndex, vColIndex)) {
	//			c.setBackground(Color.yellow);
	//		} else {
	//			c.setBackground(getBackground());
	//		}
	//		return c;
	//	}

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
	
	public PropertyTableData getPropertyTableData() { return _data; }

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

	public DipLayer1D toDipLayer1D() {
		int nBoundaries = _data.getRowCount();
		DipLayer1D dlayer1D = new DipLayer1D(nBoundaries);
		
		for(int i=0; i<nBoundaries; i++) {
			int dCol = 3;
			if(_data.getColumnCount()>11) dCol = 4; //3D
			double top = Double.valueOf(_data.getValueAt(i, dCol++).toString());
			SingleFlatLayer sl = new SingleFlatLayer(
					top, top+1.0, 
					_data.getValueAt(i, 1).toString(), 
					Integer.valueOf(_data.getValueAt(i, 0).toString()), 
					//double vp, double vs, double den, 
					Double.valueOf(_data.getValueAt(i, dCol++).toString()),
					Double.valueOf(_data.getValueAt(i, dCol++).toString()),
					Double.valueOf(_data.getValueAt(i, ++dCol).toString()),

					//double delta, double epsilon, double gamma, 
					Double.valueOf(_data.getValueAt(i, ++dCol).toString()),
					Double.valueOf(_data.getValueAt(i, ++dCol).toString()),
					Double.valueOf(_data.getValueAt(i, ++dCol).toString()),

					//double qp, double qs, double theta, double phi					
					1.0e10, 1.0e10, 0.0, 0.0);
			dlayer1D.add(sl);
			if(dCol==4) {
//				pN[i] = Double.valueOf(_data.getValueAt(i, 2).toString());
//				pE[i] = Double.valueOf(_data.getValueAt(i, 3).toString());
//				pD[i] = Double.valueOf(_data.getValueAt(i, 4).toString());
			}
		}

		return dlayer1D;
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

	class PropertyTableData extends AbstractTableModel {

		public ColumnData _columns[] = null;

		boolean 		_isEditable		= true;
		double [][] 	_properties 	= null;
		String [] 		_layerName 		= null;
		int _M = 3;
		int _M1 = 7;

		public PropertyTableData(String [] layerName, double [][] properties, boolean isEditable) {
			initData(layerName, properties, isEditable);
		}
		public void initData(String [] layerName, double [][] properties, boolean isEditable)	{
			_layerName		= layerName;
			_properties		= properties;
			_isEditable		= isEditable;
			
			if(properties[0].length==9) { //2D
				ColumnData columns[] = {
						new ColumnData( "No", 100, JLabel.LEFT ),
						new ColumnData( "Name", 150, JLabel.LEFT ),
						new ColumnData( "L_Depth", 120, JLabel.CENTER ),
						new ColumnData( "R_Depth", 120, JLabel.CENTER ),
						new ColumnData( "Vp", 120, JLabel.CENTER ),
						new ColumnData( "Vs", 100, JLabel.CENTER ),
						new ColumnData( "Vp/Vs", 100, JLabel.CENTER ),
						new ColumnData( "Density", 100, JLabel.CENTER ),
						new ColumnData( "Delta", 100, JLabel.CENTER ),
						new ColumnData( "Epsilon", 100, JLabel.CENTER ),
						new ColumnData( "Gamma", 100, JLabel.CENTER )
				};
				_columns = columns;		
				_M = 3;
				_M1 = 6;
			} else {
				ColumnData columns[] = {
						new ColumnData( "No", 100, JLabel.LEFT ),
						new ColumnData( "Name", 150, JLabel.LEFT ),
						new ColumnData( "Pivot_N", 120, JLabel.CENTER ),
						new ColumnData( "Pivot_E", 120, JLabel.CENTER ),
						new ColumnData( "Pivot_D", 120, JLabel.CENTER ),
						new ColumnData( "Vp", 120, JLabel.CENTER ),
						new ColumnData( "Vs", 100, JLabel.CENTER ),
						new ColumnData( "Vp/Vs", 100, JLabel.CENTER ),
						new ColumnData( "Density", 100, JLabel.CENTER ),
						new ColumnData( "Delta", 100, JLabel.CENTER ),
						new ColumnData( "Epsilon", 100, JLabel.CENTER ),
						new ColumnData( "Gamma", 100, JLabel.CENTER )
				};
				_columns = columns;
				_M = 4;
				_M1 = 7;
			}
		}

		public int getRowCount() 							{ return _layerName.length;  }
		public int getColumnCount() 						{ return _columns.length; } 
		public String getColumnName(int column) 			{ return _columns[column]._title; }
		public boolean isCellEditable(int iRow, int iCol) 	{ if(iCol<=_M||iCol==_M1) return false; else return _isEditable; }
		public Object getValueAt(int iRow, int iCol) {
			if (iRow < 0 || iRow >= getRowCount()) 
				return "";
			if(iCol==0)  return iRow+1;
			else if(iCol==1)  return _layerName[iRow];
			else if(iCol>=2&&iCol<_properties[0].length+2)  return _properties[iRow][iCol-2];
			//else if(iCol>=2&&iCol<11)  return _properties[iRow][iCol-2];
			else   return "";
		}

		public void setValueAt(Object value, int iRow, int iCol) {
			double v = 0.0;
			try{
				if(iCol!=1) v = Double.parseDouble(value.toString());
			} catch(NumberFormatException e) { return; }
			if(iCol==0) { }
			else if(iCol==1) _layerName[iRow]=value.toString();
			else if(iCol>=2&&iCol<_properties[0].length+2)  _properties[iRow][iCol-2] = v;
			//else if(iCol>=2&&iCol<11)  _properties[iRow][iCol-2] = v;
			else {} 
			fireTableCellUpdated(iRow, iCol);
		}	
		
		public String toString(){
			String a = " ";
			String b = " "; 

			a = " ";
			for(int j=0; j<getColumnCount(); j++) { 
				if(j==getColumnCount()-1) a += getColumnName(j);
				else a += getColumnName(j)+", "; 
			} 
			b = new String(b.concat(a+"\n"));
			for(int i=0; i<getRowCount(); i++) {
				a = " ";
				for(int j=0; j<getColumnCount(); j++) { 
					if(j==getColumnCount()-1) a += getValueAt(i, j).toString();
					else a += getValueAt(i, j).toString()+", "; 
				} 
				b = new String(b.concat(a+"\n"));
			}
			return b;
		}
	}

	//	class MyTableCellRenderer extends DefaultTableCellRenderer {
	//		DecimalFormat formatter = new DecimalFormat("#.##");
	//		public void setValue(Object value) {
	//				super.setValue(value);
	//		}
	//	}

	//class MyTableCellRenderer extends JLabel implements TableCellRenderer {
	class MyTableCellRenderer extends DefaultTableCellRenderer {
		//		Color curColor;
		//		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
		//				boolean hasFocus, int rowIndex, int vColIndex) {
		//			if (curColor instanceof Color) {
		//				curColor = (Color) value;
		//			} else {
		//				curColor = table.getBackground();
		//			}
		//			return this;
		//		}

		//		public void paint(Graphics g) {
		//			g.setColor(curColor);
		//			g.fillRect(0, 0, getWidth() - 1, getHeight() - 1);
		//		}

		//		public void setValue(Object value) {
		//			if (value instanceof String) {
		//				setForeground(Color.RED);
		//				setText(value.toString());
		//			} else {
		//				setText(value.toString());
		//			}
		//		}
	}

	class MyTableCellEditor extends DefaultCellEditor implements TableCellEditor, FocusListener, KeyListener {
		JComponent component = new JTextField();
		boolean erase = true;

		public MyTableCellEditor(JTextField textField) {
			super(textField);
			component = textField;
			component.addFocusListener(this);
			component.addKeyListener(this);
			setClickCountToStart(1);
		}

		public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected,
				int rowIndex, int vColIndex) {

			((JTextField) component).setText(value.toString());

			return component;
		}

		public Object getCellEditorValue() {
			return ((JTextField) component).getText();
		}

		public void focusGained(FocusEvent fe) {
			//component.putClientProperty("DELETE_ON_EDIT", true);
			erase = true;
			System.out.println("get focus");
		}
		public void focusLost(FocusEvent fe) {  
			erase = !erase;
			System.out.println("lost focus");
		}


		public void keyPressed(KeyEvent ke) {
			System.out.println("Key:" + ke.getKeyCode() + "/" + ke.getKeyChar());
			//            if ((!(ke.isActionKey()))
			//                    && ((Boolean) component.getClientProperty("DELETE_ON_EDIT"))) {
			if ((!(ke.isActionKey())) && erase) {
				System.out.println("I am here 2");
				erase = !erase;
				//component.putClientProperty("DELETE_ON_EDIT", false);
				((JTextField) component).setText(null);
			}
		}

		public void keyTyped(KeyEvent ke) { }
		public void keyReleased(KeyEvent ke) { }
	}

	public void addDeletePreviousOnEditBehavior(final JComponent field) {

		field.addFocusListener(new FocusListener() {

			public void focusGained(FocusEvent fe) {
				field.putClientProperty("DELETE_ON_EDIT", true);
				System.out.println("I am here 1");
			}
			public void focusLost(FocusEvent fe) {     }
		});

		field.addKeyListener(new KeyListener() {
			public void keyPressed(KeyEvent ke) {
				System.out.println("Key:" + ke.getKeyCode() + "/" + ke.getKeyChar());
				if ((!(ke.isActionKey()))
						&& ((Boolean) field.getClientProperty("DELETE_ON_EDIT"))) {
					System.out.println("I am here 2");
					field.putClientProperty("DELETE_ON_EDIT", false);
					((JTextField) field).setText(null);
				}
			}

			public void keyTyped(KeyEvent ke) { }
			public void keyReleased(KeyEvent ke) { }
		});
	}
}


