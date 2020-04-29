package com.geohammer.core.planarmodel;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import javax.swing.JOptionPane;

public class ZmapPlus {
	private double [][] 	_data 			= null; 
	private double [][] 	_header			= new double[3][6];
	private int 			_nNodePerLine	= 4;
	
	//public ArrayList<String> 	_headerList 	= null;
	
	public double _INVALID_D = -99999.9;
	
	public ZmapPlus() { }
	public ZmapPlus(String inputFileName) {
		read(inputFileName);
	}

	public void allocate(int nRows, int nCols) {
		_data = new double[nRows][nCols];
	}
	
	public double getInvalidNumber() 	{ 
		return getDefaultInvalidNumber()!=_INVALID_D ? getDefaultInvalidNumber():getUserInvalidNumber(); 
	}
	public double getDefaultInvalidNumber() 	{ return _header[0][1]; }
	public double getUserInvalidNumber()		{ return _header[0][2]; }
	public int getDecimal() 					{ return (int)_header[0][3]; }
	public int getStartingColumn() 				{ return (int)_header[0][4]; }
	
	public int getRowCount() 					{ return (int)_header[1][0]; }
	public int getColumnCount() 				{ return (int)_header[1][1]; }
	public double getX0() 						{ return _header[1][2]; }
	public double getX1() 						{ return _header[1][3]; }
	public double getY0() 						{ return _header[1][4]; }
	public double getY1() 						{ return _header[1][5]; }
	
	public double [][] getData() 				{ return _data; }
	public double getData(int iRow, int iCol) 	{ return _data[iRow][iCol]; }
	public double [] getRowData(int iRow) 	{ return _data[iRow]; }
	public double [] getColumnData(int jCol) 	{ 
		double [] col = new double[getRowCount()];
		for(int i=0; i<col.length; i++) {
			col[i] = _data[i][jCol];
		}
		return col; 
	}
	public float [][] getDataFloat() 			{ 
		float [][] v = new float[_data.length][_data[0].length];
		for(int i=0; i<_data.length; i++) {
			for(int j=0; j<_data[i].length; j++) {
				v[i][j] = (float)_data[i][j];
			}
		}
		return v; 
	}
	
	public boolean invalidNumberExists() {
		double invalidNumber = getInvalidNumber();
		for(int i=0; i<_data.length; i++) {
			for(int j=0; j<_data[i].length; j++) {
				if(_data[i][j]==invalidNumber) return true;
			}
		}
		return false;
	}
	public void condition() {
		double invalidNumber = getInvalidNumber();
		double a = _data[0][0];
		for(int i=0; i<_data.length; i++) {
			for(int j=0; j<_data[i].length; j++) {
				if(_data[i][j]==invalidNumber) _data[i][j] = a;
				else a = _data[i][j];
			}
		}
	}
	public void multiply(double factor) {
		for(int i=0; i<_data.length; i++) {
			for(int j=0; j<_data[i].length; j++) {
				_data[i][j] *= factor;
			}
		}
	}
	
	public void trimCol() {
//		int k = 0;
//		for(int i=0; i<getColumnCount(); i++) { if(isValidCol(i)) k++;	}
//		
//		String [][] data = new String[getRowCount()][k];
//		
//		k = 0;
//		for(int i=0; i<getColumnCount(); i++) {
//			if(isValidCol(i)) {
//				for(int j=0; j<getRowCount(); j++) { data[j][k]=_data[j][i]; }
//				k++;
//			}
//		}
//		_data = data;
	}
	
	private void parseLine(int index, String line) {
		String delimiter = "[,]";
		String[] splits = line.split(delimiter);
		String a = null;
		for(int j=0; j<splits.length; j++) {
			a = splits[j].trim();
			if(a.isEmpty()) {
				_header[index][j] = _INVALID_D;
			} else {
				_header[index][j] = Double.parseDouble(a);
			}
		}
	}
	public void read(String selectedFileName) {
		try{			
			BufferedReader reader = new BufferedReader(new FileReader(selectedFileName));
			
			String[] splits = null;
			String delimiter = "[,]";
			String line = null;
			int nAt 	= 0;
			while (nAt<2)  {
				line=reader.readLine();
				if(line.startsWith("!")) {
					
				} else if(line.startsWith("@")) {
					nAt++;
					if(nAt==1) {
						splits = line.split(delimiter);
						_nNodePerLine = Integer.parseInt(splits[2].trim());
						parseLine(0, reader.readLine()); 
						parseLine(1, reader.readLine()); 
						line=reader.readLine(); 							
					}
				} else { }
			}

			int nCols 	= getColumnCount();
			int nRows 	= getRowCount();
			allocate(nRows, nCols);
			
			delimiter = "[ ]+";
			int nLines = nCols/_nNodePerLine;
			if(nCols%_nNodePerLine>0) nLines++;			

//			System.out.println(nRows + " nLines="+nLines+ " ncol="+nCols);
//			for(int j=0; j<nCols; j++) {
//				for(int i=0, k=0; i<nLines; i++) {
//					line=reader.readLine().trim();
//					//System.out.println(line);
//					splits = line.split(delimiter);
//					//System.out.println(Arrays.toString(splits));
//					for(int p=0; p<splits.length; p++) {
//						_data[k++][j] = Double.parseDouble(splits[p].trim());
//					}
//				}
//			}
			
			int iCol 	= 0;
			int iRow 	= 0;
			int k = 0;
			while ((line=reader.readLine()) != null)  {
				splits = line.trim().split(delimiter);
				for(int p=0; p<splits.length; p++) {
					iCol 	= k/nRows;
					if(iRow>=nRows) iRow = 0;
					_data[iRow++][iCol] = Double.parseDouble(splits[p].trim());
					k++;
				}
			}
			reader.close();
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
//		for(int i=0; i<_headerText.length; i++) {
//			System.out.println(_headerText[i]);
//		}
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
