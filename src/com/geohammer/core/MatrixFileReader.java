package com.geohammer.core;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.swing.JOptionPane;

public class MatrixFileReader {
	public String [][] 	_data 		= null; 
	public String [] 	_headerText = null;
	public String [] 	_tailText 	= null;
	
	public int _INVALID_I = -99999;
	public float _INVALID_F = -99999.0f;
	public double _INVALID_D = -99999.0;
	
	public MatrixFileReader() { }
	public MatrixFileReader(String inputFileName, int nHeaders, int nTails, int [] colIndex, String delimiter) {
		this(inputFileName, 0, nHeaders, nTails, colIndex, delimiter);
	}
	public MatrixFileReader(String inputFileName, int skipNumOfLines, int nHeaders, int nTails, int [] colIndex, String delimiter) {
		readTextualData(inputFileName, skipNumOfLines, nHeaders, nTails, colIndex, delimiter);
	}
	public MatrixFileReader(InputStream is, int skipNumOfLines, int nHeaders, int nTails, int [] colIndex, String delimiter) {
		readTextualData(is, skipNumOfLines, nHeaders, nTails, colIndex, delimiter);
	}

	public void allocate(int nRows, int nCols) {
		_data = new String[nRows][nCols];
	}

	public String [] getHeaderText() 	{ return _headerText; }
	public String [] getTailText() 		{ return _tailText; }
	public int getRowCount() 			{ return _data.length; }
	public int getColumnCount() 		{ return _data[0].length; }

	public String [][] getData() 					{ return _data; }
	public String getData(int iRow, int iCol) 		{ return _data[iRow][iCol]; }
	public int getDataInt(int iRow, int iCol) 		{ 
		if(_data[iRow][iCol]==null||_data[iRow][iCol].isEmpty()) return _INVALID_I; 
		else return Integer.parseInt(_data[iRow][iCol].trim()); 
	}
	public float getDataFloat(int iRow, int iCol) 	{ 
		if(_data[iRow][iCol]==null||_data[iRow][iCol].isEmpty()) return _INVALID_F; 
		else return Float.parseFloat(_data[iRow][iCol].trim()); 
	}
	public double getDataDouble(int iRow, int iCol) { 
		if(_data[iRow][iCol]==null||_data[iRow][iCol].isEmpty()) return _INVALID_D; 
		else return Double.parseDouble(_data[iRow][iCol].trim()); 
	}
	public String [] getRowData(int iRow) 	{ return _data[iRow]; }
	public String [] getColumnData(int jCol) 	{ 
		String [] col = new String[getRowCount()];
		for(int i=0; i<col.length; i++) {
			col[i] = _data[i][jCol]+"";
			//System.out.println(col[i]+"");
		}
		return col; 
	}

	public void setData(int iRow, int iCol, String v) 	{ _data[iRow][iCol]=v; }
	
	public void trimCol() {
		int k = 0;
		for(int i=0; i<getColumnCount(); i++) { if(isValidCol(i)) k++;	}
		
		String [][] data = new String[getRowCount()][k];
		
		k = 0;
		for(int i=0; i<getColumnCount(); i++) {
			if(isValidCol(i)) {
				for(int j=0; j<getRowCount(); j++) { data[j][k]=_data[j][i]; }
				k++;
			}
		}
		_data = data;
	}
	public void trimRow() {
		int k = 0;
		for(int i=0; i<getRowCount(); i++) { if(isValidRow(i)) k++;	}
		
		String [][] data = new String[k][getColumnCount()];
		
		k = 0;
		for(int i=0; i<getRowCount(); i++) {
			if(isValidRow(i)) {
				for(int j=0; j<getColumnCount(); j++) {	data[k][j]=_data[i][j]; }
				k++;
			}
		}
		_data = data;
	}
	private boolean isValidRow(int iRow) {
		boolean result = true;
		for(int i=0; i<getColumnCount(); i++) {
			if(_data[iRow][i]==null||_data[iRow][i].isEmpty()) return false;
		}		
		return result;
	}
	private boolean isValidCol(int iCol) {
		boolean result = true;
		for(int i=0; i<getRowCount(); i++) {
			if(_data[i][iCol]==null||_data[i][iCol].isEmpty()) return false;
		}		
		return result;
	}
	public void readTextualData(String selectedFileName, int nHeaders, int nTails, String delimiter) {
		readTextualData(selectedFileName, 0, nHeaders, nTails, null, delimiter);
	}
	public void readTextualData(String selectedFileName, int skipNumOfLines, int nHeaders, int nTails, int [] colIndex, String delimiter) {
		String vString = "";
		
		//default is comma and space
		if(delimiter==null) delimiter = "[ ,]+";
		
		try{
			if(nHeaders>0) 	_headerText = new String[nHeaders];
			else 			_headerText = null;
			if(nTails>0) 	_tailText = new String[nTails];
			else 			_tailText = null;
			
			BufferedReader reader = new BufferedReader(new FileReader(selectedFileName));
			for(int i=0; i<skipNumOfLines; i++)  reader.readLine();
			if(_headerText!=null) { for(int i=0; i<_headerText.length; i++)  reader.readLine(); }
			
			int nLines 	= 1;
			int nCols 	= 0;
			int nRows 	= 0;
			String line=reader.readLine();
			String[] splits = line.split(delimiter);
			if(colIndex==null) nCols = splits.length;
			else nCols = colIndex.length;
			//System.out.println(" ncol="+nCols+" "+Arrays.toString(splits));
			
			while ((line=reader.readLine()) != null)  nLines++;
			if(nLines==0) {reader.close(); return;} //empty file
			
			nLines -= nTails;
			nRows = nLines;
			reader.close();
			
			//System.out.println(nRows + " ncolReader="+nCols);
			BufferedReader reader1 = new BufferedReader(new FileReader(selectedFileName));
			for(int i=0; i<skipNumOfLines; i++) {
				reader1.readLine(); 
			}
			for(int i=0; i<nHeaders; i++) {
				line=reader1.readLine(); 
				_headerText[i] = line.trim();
				//System.out.println(i + " _headerText[i]="+_headerText[i]);
			}
			if(nRows==0) {reader1.close(); return; } //only header in file
			
			allocate(nRows, nCols);
			//System.out.println(nRows + " nLines="+nLines + " ncol1="+nCols);
			int k = 0;
			nRows = 0;
			for(int i=0; i<nLines; i++) {
				line=reader1.readLine().trim();
				splits = line.split(delimiter);
				k = 0;
				for(String item: splits){
					if(colIndex==null) {
						_data[nRows][k] = item;
					} else {
						for(int j=0; j<nCols; j++) {
							if(k==colIndex[j]) {
								_data[nRows][j] = item;
								//System.out.print(_data[nRows][j]+" ");
							}
						}
					}
					k++;
				}
				//System.out.print(_data[nRows][0]+"  ");
				nRows++;				
			}

			for(int i=0; i<nTails; i++) {
				line=reader1.readLine();
				_tailText[i] = line.trim();
			}
			
			reader1.close();
		} catch (IOException ioexception) {
			JOptionPane.showMessageDialog(null, "\nFile Format is not right!", 
					"Error", JOptionPane.ERROR_MESSAGE);
		}	
		
	}
	public void readTextualData(InputStream is, int skipNumOfLines, int nHeaders, int nTails, int [] colIndex, String delimiter) {
		String vString = "";
		
		//default is comma and space
		if(delimiter==null) delimiter = "[ ,]+";
		
		try{
			if(nHeaders>0) 	_headerText = new String[nHeaders];
			else 			_headerText = null;
			if(nTails>0) 	_tailText = new String[nTails];
			else 			_tailText = null;
			
			BufferedReader reader = new BufferedReader(new InputStreamReader(is));
			for(int i=0; i<skipNumOfLines; i++)  reader.readLine();
			if(_headerText!=null) { for(int i=0; i<_headerText.length; i++)  reader.readLine(); }
			
			int nLines 	= 1;
			int nCols 	= 0;
			int nRows 	= 0;
			String line=reader.readLine();
			String[] splits = line.split(delimiter);
			if(colIndex==null) nCols = splits.length;
			else nCols = colIndex.length;
			//System.out.println(" ncol="+nCols+" "+Arrays.toString(splits));
			
			while ((line=reader.readLine()) != null)  nLines++;
			if(nLines==0) {reader.close(); return;} //empty file
			
			nLines -= nTails;
			nRows = nLines;
			reader.close();
			
			//System.out.println(nRows + " ncol="+nCols);
			BufferedReader reader1 = new BufferedReader(new InputStreamReader(is));
			for(int i=0; i<skipNumOfLines; i++) {
				reader1.readLine(); 
			}
			for(int i=0; i<nHeaders; i++) {
				line=reader1.readLine(); 
				_headerText[i] = line.trim();
			}
			if(nRows==0) {reader1.close(); return; } //only header in file
			
			allocate(nRows, nCols);
			//System.out.println(nRows + " nLines="+nLines + " ncol1="+nCols);
			int k = 0;
			nRows = 0;
			for(int i=0; i<nLines; i++) {
				line=reader1.readLine();
				splits = line.split(delimiter);
				k = 0;
				for(String item: splits){
					if(colIndex==null) {
						_data[nRows][k] = item;
					} else {
						for(int j=0; j<nCols; j++) {
							if(k==colIndex[j]) {
								_data[nRows][j] = item;
								//System.out.print(_data[nRows][j]+" ");
							}
						}
					}
					k++;
				}
				//System.out.print(_data[nRows][0]+"  ");
				nRows++;				
			}

			for(int i=0; i<nTails; i++) {
				line=reader1.readLine();
				_tailText[i] = line;
			}
			
			reader1.close();
		} catch (IOException ioexception) {
			JOptionPane.showMessageDialog(null, "\nFile Format is not right!", 
					"Error", JOptionPane.ERROR_MESSAGE);
		}	
		
	}
	public void printClass() {
		String b = "";
		for(int i=0; i<getRowCount(); i++) {
			b = ""+_data[i][0];
			for(int j=1; j<getColumnCount(); j++) {
				b = new String(b.concat(", " + _data[i][j]));
			}
			System.out.println(b);
		}
	}
	public void printData() {
		for(int i=0; i<_data.length; i++) {
			String b = "";
			for(int j=0; j<_data[i].length; j++) {
				b = new String(b.concat(_data[i][j]+" "));
			}
			System.out.println(b);
		}
		System.out.println();
	}
	public void printClassOutline() {
		for(int i=0; i<_headerText.length; i++) {
			System.out.println("i="+i+" "+_headerText[i]);
		}
		String b = _data.length+" "+_data[0].length+" "+getRowCount()+" "+getColumnCount()+"\n";
		int [] index = new int[]{0, getRowCount()/2, getRowCount()-1};
		for(int i=0; i<index.length; i++) {
			int k = index[i];
			for(int j=0; j<getColumnCount(); j++) {
				b = new String(b.concat(_data[k][j]+" "));
			}
			b = new String(b.concat("\n"));
		}
		System.out.println(b);
	}
}
