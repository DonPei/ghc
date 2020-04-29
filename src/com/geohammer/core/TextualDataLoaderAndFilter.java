package com.geohammer.core;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.StringTokenizer;

import javax.swing.JOptionPane;

public class TextualDataLoaderAndFilter {
	public int 			_nRows 	= 1; 
	public int 			_nCols 	= 1; 
	public float [][] 	_data 	= null; 
	

	public String [] _headerText = null;
	public String [] _tailText 	= null;
	
	public float _INVALID_F = -99999.0f;
	
	public TextualDataLoaderAndFilter() {
		
	}
	public TextualDataLoaderAndFilter(int nRows, int nCols) {
		_nRows = nRows;
		_nCols = nCols;
		allocate(nRows, nCols);
		init(_INVALID_F);
	}
	
	public TextualDataLoaderAndFilter(String inputFileName, int nHeaders, int nTails, int [] colIndex) {
		this(inputFileName, 0, nHeaders, nTails, colIndex);
	}
	public TextualDataLoaderAndFilter(String inputFileName, int skipNumOfLines, int nHeaders, int nTails, int [] colIndex) {
		readTextualData(inputFileName, skipNumOfLines, nHeaders, nTails, colIndex);
	}
	public void allocate() {
		allocate(_nRows, _nCols);
	}
	public void allocate(int nRows, int nCols) {
		_data = new float[nRows][nCols];
	}
	public void init(float v) {
		for(int i=0; i<_nRows; i++) {
			for(int j=0; j<_nCols; j++) {
				_data[i][j] = v;
			}
		}
	}
	
	public TextualDataLoaderAndFilter copy() {
		TextualDataLoaderAndFilter textualDataLoaderAndFilter = new TextualDataLoaderAndFilter(_nRows, _nCols);
		for(int i=0; i<_nRows; i++) {
			for(int j=0; j<_nCols; j++) {
				textualDataLoaderAndFilter._data[i][j] = _data[i][j];
			}
		}
		if(_headerText!=null) for(int i=0; i<_headerText.length; i++) textualDataLoaderAndFilter._headerText[i] = _headerText[i];
		if(_tailText!=null) for(int i=0; i<_tailText.length; i++) textualDataLoaderAndFilter._tailText[i] = _tailText[i];
		return textualDataLoaderAndFilter;
	}

	public String [] getHeaderText() {
		return _headerText;
	}
	public String [] getTailText() {
		return _tailText;
	}
	public int getRowCount() {
		return _nRows;		
	}
	public int getColumnCount() {
		return _nCols;		
	}
	public void setRowCount( int v) {
		_nRows = v;		
	}
	public void setColumnCount(int v) {
		_nCols = v;		
	}
	public void setInvalidRow(int index){
		for(int j=0; j<_nCols; j++) {
			_data[index][j]=_INVALID_F;
		}
	}
	public boolean isInvalidRow(int index){ // all element must be _INVALID_F
		int k = 0;
		for(int j=0; j<_nCols; j++) {
			if(_data[index][j]==_INVALID_F)  {
				k++;
			}
		}
		if(k==_nCols) return true;
		else return false;
	}
	public int NumOfInvalidRow(){
		int k = 0;
		for(int i=0; i<_nRows; i++) {
			if(isInvalidRow(i)) k++;
		}
		return k;
	}
	public void scale(float scalor){
		for(int i=0; i<_nRows; i++) {
			for(int j=0; j<_nCols; j++) {
				if(_data[i][j]!=_INVALID_F) _data[i][j] *= scalor;
			}
		}
	}
	public void setData(int rowIndex, int colIndex, float v) {
		_data[rowIndex][colIndex] = v;
	}
	public float [][] getData() {
		return _data;
	}
	public float getData(int rowIndex, int colIndex) {
		return _data[rowIndex][colIndex];
	}
	public void insertColumn(int iCol, float v){
		float [][] data = new float[_nRows][_nCols+1];
		for(int i=0; i<_nRows; i++) {
			for(int j=0; j<_nCols+1; j++) {
				data[i][j] = v;
			}
		}
		
		int k = 0;
		
		for(int j=0; j<_nCols; j++) {
			if(j==iCol) k++;
			for(int i=0; i<_nRows; i++) {
				data[i][k] = _data[i][j];
			}
			k++;
		}
		_data = data;
		_nCols++;
	}
	public void setColumn(int iCol, float v){
		for(int i=0; i<_nRows; i++) {
			for(int j=0; j<_nCols; j++) {
				if(j==iCol) _data[i][j] = v;
			}
		}
	}
	public float[][] trimData(){
		int k =  _nRows-NumOfInvalidRow();
		//System.out.println("k="+k+" nRow="+_nRows+" n="+ NumOfInvalidRow());
		
		float [][] data = new float[k][_nCols];
		k = 0;
		for(int i=0; i<_nRows; i++) {
			if(isInvalidRow(i)) {
			} else {
				for(int j=0; j<_nCols; j++) {
					data[k][j] = _data[i][j];  
				}
				k++;
			}
		}
		return data;
	}
	public void trim(){
		_data = trimData();
		_nRows = _data.length;
	}
	
	public float getMaxValueOfColumn(int colIndex){
		float max = -1.0e10f;
		for(int i=0; i<_nRows; i++) {
			if(_data[i][colIndex]>max){
				max = _data[i][colIndex];
			}
		}
		return max;
	}
	public int getMaxValueIndexOfColumn(int colIndex){
		float max = -1.0e10f;
		int k = 0;
		for(int i=0; i<_nRows; i++) {
			if(_data[i][colIndex]>max){
				max = _data[i][colIndex];
				k = i;
			}
		}
		return k;
	}
	public float getMinValueOfColumn(int colIndex){
		float min = 1.0e10f;
		for(int i=0; i<_nRows; i++) {
			if(_data[i][colIndex]<min){
				min = _data[i][colIndex];
			}
		}
		return min;
	}
	public int getMinValueIndexOfColumn(int colIndex){
		float min = 1.0e10f;
		int k = 0;
		for(int i=0; i<_nRows; i++) {
			if(_data[i][colIndex]<min){
				min = _data[i][colIndex];
				k = i;
			}
		}
		return k;
	}
	public void readTextualData(String selectedFileName, int nHeaders, int nTails, int [] colIndex) {
		readTextualData(selectedFileName, 0, nHeaders, nTails, colIndex);
	}
	public void readTextualData(String selectedFileName, int skipNumOfLines, int nHeaders, int nTails, int [] colIndex) {
		String vString = "";
		_nCols = colIndex.length;
		
		try{
			if(nHeaders>0) 	_headerText = new String[nHeaders];
			else 			_headerText = null;
			if(nTails>0) 	_tailText = new String[nTails];
			else 			_tailText = null;
			
			BufferedReader reader = new BufferedReader(new FileReader(selectedFileName));
			String line;
			for(int i=0; i<skipNumOfLines; i++) {
				reader.readLine(); 
			}
			int nLines = 0;
			while ((line=reader.readLine()) != null) {
				nLines++;
			}
			if(nLines==0) {_nRows=-1; reader.close(); return;} //empty file
			
			nLines -= (nHeaders+nTails);
			_nRows = nLines;
			reader.close();
			
			//System.out.println(_nRows + " ncol="+_nCols);
			BufferedReader reader1 = new BufferedReader(new FileReader(selectedFileName));
			for(int i=0; i<skipNumOfLines; i++) {
				reader1.readLine(); 
			}
			for(int i=0; i<nHeaders; i++) {
				line=reader1.readLine(); 
				_headerText[i] = line;
			}
			//System.out.println(line);
			if(_nRows==0) {reader1.close(); return; } //only header in file
			allocate(_nRows, _nCols);
			init(_INVALID_F);
			int k = 0;
			StringTokenizer st = null;
			int nRows = 0;
			//System.out.println(_nRows + " nLines="+nLines + " ncol="+_nCols);
			for(int i=0; i<nLines; i++) {
				line=reader1.readLine();
				
				st = new StringTokenizer(line, ",");
				//System.out.println(line+"  "+st.countTokens());
//				if(st.countTokens()==1) {
//					//System.out.println(line+"  "+st.countTokens());
//					continue;
//				}
//				if(st.countTokens()==4) {
//					System.out.println(line+"  "+st.countTokens());
//					line = line + " "+ _data[nRows-1][3];
//					System.out.println(line+"  "+st.countTokens());
//					st = new StringTokenizer(line, " ,");
//				}
				k = 0;

				//System.out.println(line);
				while (st.hasMoreTokens()) {
					vString = st.nextToken().trim();
					//System.out.println(vString);
					for(int j=0; j<_nCols; j++) {
						if(k==colIndex[j]) {
							_data[nRows][j] = Float.parseFloat(vString);
							//System.out.print(_data[nRows][j]+" ");
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
//			System.out.println(_nRows+"  "+nRows);
//			if(_nRows>nRows) {
//				float [][] data = new float[nRows][_nCols];
//				for(int i=0; i<nRows; i++) {
//					for(int j=0; j<_nCols; j++) {
//						data[i][j] = _data[i][j];
//					}
//				}
//				_data = data;
//				_nRows = nRows;
//			}
			
			reader1.close();
		} catch (IOException ioexception) {
			JOptionPane.showMessageDialog(null, "\nFile Format is not right!", 
					"Error", JOptionPane.ERROR_MESSAGE);
		}	
		
	}
	public void writeTextualData(String header, String selectedFileName, String tail, boolean append) {
		try{
			BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(selectedFileName, append));
			if(header!=null) bufferedWriter.write(header+"\n");
			if(_nRows==0) {bufferedWriter.close(); return; }
			String b = "";
			for(int i=0; i<_nRows; i++) {
				if(_data[i][0]!=_INVALID_F) b = ""+_data[i][0];
				else b = "";
				for(int j=1; j<_nCols; j++) {
					if(_data[i][j]!=_INVALID_F) 
						b = new String(b.concat(", " + _data[i][j]));
				}
				if(i==_nRows-1) {
					bufferedWriter.write(b);
				} else {
					bufferedWriter.write(b+"\n");
				}
			}
			if(tail!=null) bufferedWriter.write("\n"+tail);
			bufferedWriter.close();
		} catch (IOException ioexception) {
			JOptionPane.showMessageDialog(null, "\nFile Format is not right!", 
					"Error", JOptionPane.ERROR_MESSAGE);
		}
	}
	
	public void writeTextualData2(String header, String selectedFileName, String tail, boolean append) {
		try{
			BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(selectedFileName, append));
			if(header!=null) bufferedWriter.write(header+"\n");
			String b = "";
			if(_nRows==0) {bufferedWriter.close(); return; }
			//System.out.println("nrows= "+_nRows);
			for(int i=0; i<_nRows; i++) {
				if(isInvalidRow(i)) continue;
				if(_data[i][0]!=_INVALID_F) {
					int tmp = (int)_data[i][0];
					if(tmp==_data[i][0]) b = ""+tmp;
					else b = ""+_data[i][0];
				}
				else b = "";
				//System.out.println(_data[i][0] + " ");
				for(int j=1; j<_nCols; j++) {
					if(_data[i][j]!=_INVALID_F) {
						int tmp = (int)_data[i][j];
						if(tmp==_data[i][j]) b = new String(b.concat(" " + tmp));
						else b = new String(b.concat(" " + _data[i][j]));
					}
					//System.out.println(_data[i][j] + " ");
				}
				if(i==_nRows-1) {
					bufferedWriter.write(b);
				} else {
					bufferedWriter.write(b+"\n");
				}
			}
			if(tail!=null) bufferedWriter.write("\n"+tail);
			bufferedWriter.close();
		} catch (IOException ioexception) {
			JOptionPane.showMessageDialog(null, "\nFile Format is not right!", 
					"Error", JOptionPane.ERROR_MESSAGE);
		}
	}
	public void printClass() {
		String b = "";
		for(int i=0; i<_nRows; i++) {
			b = ""+_data[i][0];
			for(int j=1; j<_nCols; j++) {
				b = new String(b.concat(", " + _data[i][j]));
			}
			System.out.println(b);
		}
	}
	public void printClassOutline() {
		for(int i=0; i<_headerText.length; i++) {
			System.out.println(_headerText[i]);
		}
		String b = _data.length+" "+_data[0].length+" "+_nRows+" "+_nCols+"\n";
		int [] index = new int[]{0, _nRows/2, _nRows-1};
		for(int i=0; i<index.length; i++) {
			int k = index[i];
			for(int j=0; j<_nCols; j++) {
				b = new String(b.concat(_data[k][j]+" "));
			}
			b = new String(b.concat("\n"));
		}
		System.out.println(b);
	}
}

