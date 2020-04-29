package com.geohammer.vc.run;

import java.awt.Component;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.StringTokenizer;

import javax.swing.DefaultCellEditor;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import com.geohammer.core.planarmodel.FlatLayer1D;
import com.geohammer.core.planarmodel.Layer2D;

public class SaTable extends JTable{
	private SaTableData 		_model 	= null;
	private Clipboard 			_system	= null;
	private StringSelection 	_stsel	= null;
	private int 				_uneditableColumnIndex = -1; 
	private CheckBoxHeader 		_checkBoxHeader = null;
	
	public SaTable(int id, FlatLayer1D curr, FlatLayer1D min, FlatLayer1D max, 
			FlatLayer1D editable, FlatLayer1D linkTo) {
		JTextField field=new JTextField();
		field.requestFocus();
		
		_model = new SaTableData(id, curr, min, max, editable, linkTo);
		setAutoCreateColumnsFromModel(false);
		setModel(_model); 
		for (int k = 0; k <_model._columns.length; k++) {
			//MyTableCellRenderer renderer = new MyTableCellRenderer();
			DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
			DefaultCellEditor editor = new DefaultCellEditor(new JTextField());
			//editor.setClickCountToStart(1);
			//MyTableCellEditor editor = new MyTableCellEditor(new JTextField());
			renderer.setHorizontalAlignment(_model._columns[k]._alignment);
			TableColumn column = null;
			if(k==5) {
				column = new TableColumn(k, _model._columns[k]._width, 
						getDefaultRenderer(Boolean.class), getDefaultEditor(Boolean.class));
				_checkBoxHeader = new CheckBoxHeader();
				//_checkBoxHeader.setSelected(true);
				//checkBoxHeader.setEnabled(false);
				column.setHeaderRenderer(_checkBoxHeader);
			} else {
				column = new TableColumn(k, _model._columns[k]._width, renderer, editor);
				column.setHeaderRenderer(new HorizontalAlignmentHeaderRenderer(SwingConstants.CENTER));
			}
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
	
	public SaTableData getTableModel()  			{ return _model; }
	
	public void setUneditableColumnIndex(int uneditableColumnIndex) { 
		_uneditableColumnIndex = uneditableColumnIndex;
	}
	public void setEnableCheckBoxHeader(boolean enabled) { 
		_checkBoxHeader.setEnabled(enabled);
	}
	public boolean isCellEditable(int row, int column) {   
		if(_uneditableColumnIndex == -1) return true; 
		else {
			if(_uneditableColumnIndex==column) return false;
			else return true;
		}         
	};
	
	private class ColumnData {
		public String _title;
		public int _width;
		public int _alignment;
		public ColumnData(String title, int width, int alignment) {
			_title = title;
			_width = width;
			_alignment = alignment;
		}
	}
		
	private class HorizontalAlignmentHeaderRenderer implements TableCellRenderer {
		private int horizontalAlignment = SwingConstants.LEFT;
		public HorizontalAlignmentHeaderRenderer(int horizontalAlignment) {
			this.horizontalAlignment = horizontalAlignment;
		}
		@Override public Component getTableCellRendererComponent(
				JTable table, Object value, boolean isSelected,
				boolean hasFocus, int row, int column) {
			TableCellRenderer r = table.getTableHeader().getDefaultRenderer();
			JLabel l = (JLabel) r.getTableCellRendererComponent(
					table, value, isSelected, hasFocus, row, column);
			l.setHorizontalAlignment(horizontalAlignment);
			return l;
		}
	}
	private class CheckBoxHeader extends JCheckBox implements TableCellRenderer, MouseListener, ItemListener {
		protected CheckBoxHeader rendererComponent;
		protected int column;
		protected boolean mousePressed = false;
		public CheckBoxHeader() {
			setSelected(true);
			rendererComponent = this;
			rendererComponent.addItemListener(this);
		}
		public Component getTableCellRendererComponent( JTable table, Object value,
				boolean isSelected, boolean hasFocus, int row, int column) {
			if (table != null) {
				JTableHeader header = table.getTableHeader();
				if (header != null) {
					rendererComponent.setForeground(header.getForeground());
					rendererComponent.setBackground(header.getBackground());
					rendererComponent.setFont(header.getFont());
					header.addMouseListener(rendererComponent);
				}
			}
			setColumn(column);
			rendererComponent.setText("Editable");
			setBorder(UIManager.getBorder("TableHeader.cellBorder"));
			return rendererComponent;
		}

		protected void setColumn(int column) { this.column = column; }
		public int getColumn() { return column; }
		protected void handleClickEvent(MouseEvent e) {
			if (mousePressed) {
				mousePressed=false;
				JTableHeader header = (JTableHeader)(e.getSource());
				JTable tableView = header.getTable();
				TableColumnModel columnModel = tableView.getColumnModel();
				int viewColumn = columnModel.getColumnIndexAtX(e.getX());
				int column = tableView.convertColumnIndexToModel(viewColumn);

				if (viewColumn == this.column && e.getClickCount() == 1 && column != -1) {
					doClick();
				}
			}
		}
		public void mouseClicked(MouseEvent e) { handleClickEvent(e); ((JTableHeader)e.getSource()).repaint(); }
		public void mousePressed(MouseEvent e) { mousePressed = true; }
		public void mouseReleased(MouseEvent e) { }
		public void mouseEntered(MouseEvent e) { }
		public void mouseExited(MouseEvent e) { }

		public void itemStateChanged(ItemEvent e) {
			//Object source = e.getSource();
			//if (source instanceof AbstractButton == false) return;
			boolean checked = e.getStateChange() == ItemEvent.SELECTED;

			for(int x = 0, y = getRowCount(); x < y; x++) {
				setValueAt(new Boolean(checked),x,column);
			}
		}
	}
	
	class SaTableData extends AbstractTableModel {
		
		final public ColumnData [] _columns;

		private Object 		_data[][]	= null;
		
		public SaTableData(int id, FlatLayer1D curr, FlatLayer1D min, FlatLayer1D max, 
				FlatLayer1D editable, FlatLayer1D linkTo) {
			if(linkTo==null) {
				_columns = new ColumnData [] {
						new ColumnData( "Layer #", 10, JLabel.LEFT ),
						new ColumnData( "Name", 50, JLabel.LEFT ),
						
						new ColumnData( "Current Value", 80, JLabel.CENTER ),
						new ColumnData( "Min Limit", 60, JLabel.CENTER ),
						new ColumnData( "Max Limit", 60, JLabel.CENTER ),
						new ColumnData( "Editable", 40, JLabel.CENTER )
					};
			} else {
				_columns = new ColumnData [] {
						new ColumnData( "Layer #", 10, JLabel.LEFT ),
						new ColumnData( "Name", 50, JLabel.LEFT ),
						
						new ColumnData( "Current Value", 80, JLabel.CENTER ),
						new ColumnData( "Min Limit", 60, JLabel.CENTER ),
						new ColumnData( "Max Limit", 60, JLabel.CENTER ),
						new ColumnData( "Editable", 40, JLabel.CENTER ),
						new ColumnData( "Link To Layer #", 80, JLabel.CENTER)
					};
			}
			_data 			= new Object[curr.getNumOfBoundaries()][_columns.length];
			double a = 0;
			for( int i=0; i<_data.length; i++) {
				_data[i][0] = Integer.toString(i+1);
				_data[i][1] = curr.getLayer(i).getLayerName();
				if(id==0) {
//					_data[i][2] = new Double((int)curr.getLayer(i).getTDepth());
//					_data[i][3] = new Double((int)min.getLayer(i).getTDepth());
//					_data[i][4] = new Double((int)max.getLayer(i).getTDepth());
//					_data[i][5] = new Boolean(editable.getLayer(i).getTDepth()==1.0);
				} else {
					a = curr.getLayer(i).getLayerProperty(id);
					if(id==1||id==2) a = ((int)(a*1000))/1000.0;
					else a = ((int)(a*1000))/1000.0;
					_data[i][2] = new Double(a);
					a = min.getLayer(i).getLayerProperty(id);
					if(id==1||id==2) a = ((int)(a*1000))/1000.0;
					else a = ((int)(a*1000))/1000.0;
					_data[i][3] = new Double(a);
					a = max.getLayer(i).getLayerProperty(id);
					if(id==1||id==2) a = ((int)(a*1000))/1000.0;
					else a = ((int)(a*1000))/1000.0;
					_data[i][4] = new Double(a);
					_data[i][5] = new Boolean(editable.getLayer(i).getLayerProperty(id)==1.0);
					int ia = 6;
					if(ia<_data[i].length) _data[i][ia] = Integer.toString(i+1);;
				}
			}
		}
		
		public int getRowCount() 							{ return _data.length;  }
		public int getColumnCount() 						{ return _columns.length; } 
		public String getColumnName(int column) 			{ return _columns[column]._title; }
		public boolean isCellEditable(int iRow, int iCol) 	{ if(iCol==0||iCol==1) return false; else return true; }
		public Object getValueAt(int iRow, int iCol) 		{ 
			if(iCol==5) {
				return (Boolean)_data[iRow][iCol];
			} else {
				return _data[iRow][iCol];
			}
		}
		
		//public Class getColumnClass(int j) 					{ return getValueAt(0, j).getClass(); }
		public Class<?> getColumnClass(int columnIndex) {
			if (columnIndex == 0||columnIndex == 6) {
                return Integer.class;
            } else if (columnIndex==2||columnIndex==6) {
                return String.class;
            } else if (columnIndex == 5) {
                return Boolean.class;
            } else {
                return Double.class;
            }
        }
		public Object[][] getData()  						{ return _data; }
		
		public void setValueAt(Object obj, int iRow, int iCol) {
			if(iCol==5) {
				boolean flag = ((Boolean)obj).booleanValue();
				_data[iRow][iCol] = new Boolean(flag);
			} else if(iCol==6) {
				_data[iRow][iCol] = Integer.parseInt(obj.toString());
			} else {
				double v = Double.parseDouble(obj.toString());
				if(iCol==2) _data[iRow][iCol] = v;
				else if(iCol==3) _data[iRow][iCol] = v;
				else if(iCol==4) _data[iRow][iCol] = v;
				else {} 
			}
			fireTableCellUpdated(iRow, iCol);
		}	
	}
}


