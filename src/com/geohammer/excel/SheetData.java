package com.geohammer.excel;


public class SheetData {
	private ExcelSheet 		_excelSheet 	= null;
	private String []		_columnName 	= null;
	private double [][] 	_data 			= null; 
	private String [][] 	_dataS 			= null;
	
	private double [][] 		_minMax 		= null;
	
	public SheetData() {
	}
	
	public SheetData(ExcelSheet sheet, String [] columnName,  double [][] data,  String [][] dataS) {
		_excelSheet = sheet;
		_columnName = columnName;
		_data = data;
		_dataS = dataS;
	}
	
	public SheetData copy() {
		SheetData target = new SheetData();
		if(_columnName!=null) {
			target._columnName = new String[_columnName.length];
			for(int i=0; i<_columnName.length; i++) {
				target._columnName[i] = _columnName[i];
			}
		}
		if(_data!=null) {
			target._data = new double[_data.length][];
			for(int i=0; i<_data.length; i++) {
				double [] v = new double[_data[i].length];
				for(int j=0; j<v.length; j++) {
					v[j] = _data[i][j];
				}
				target._data[i] = v;
			}
		}
		if(_minMax!=null) {
			target._minMax = new double[_minMax.length][];
			for(int i=0; i<_minMax.length; i++) {
				double [] v = new double[_minMax[i].length];
				for(int j=0; j<v.length; j++) {
					v[j] = _minMax[i][j];
				}
				target._minMax[i] = v;
			}
		}
		if(_dataS!=null) {
			target._dataS = new String[_dataS.length][];
			for(int i=0; i<_dataS.length; i++) {
				String [] v = new String[_dataS[i].length];
				for(int j=0; j<v.length; j++) {
					v[j] = _dataS[i][j];
				}
				target._dataS[i] = v;
			}
		}
		return target;
	}

	public void updateData(int iRow, int iCol, float v) {
		_data[iRow][iCol] = v;
	}
	
	public int getNumOfRow() 							{ return _data.length; }
	public ExcelSheet getExcelSheet() 					{ return _excelSheet; }
	public String [] getColumnName() 					{ return _columnName; }
	public double [][] getData() 						{ return _data; }
	public String [][] getDataS() 						{ return _dataS; }
	
	public void setExcelSheet(ExcelSheet excelSheet) 	{ _excelSheet = excelSheet; }
	public void setColumnName(String [] columnName) 	{ _columnName = columnName; }
	public void setData(double [][] data) 				{ _data = data; }
	public void setDataS(String [][] dataS) 			{ _dataS = dataS; }	
	
	public void calMinMax() 			{ 
		if(_data==null) return; 
		_minMax 	= new double[2][_data[0].length];
		for(int i=0; i<_data[0].length; i++) {
			double min = 1.0e10f;
			double max = -1.0e10f;
			for(int j=0; j<_data.length; j++) {
				if(min>_data[j][i]) min=_data[j][i];
				if(max<_data[j][i]) max=_data[j][i];
			}
			_minMax[0][i] = min;
			_minMax[1][i] = max;
		}
	}
	public void getMinMaxNED(double [][] minMax) 			{ 
		if(_data==null) return;
		for(int i=0; i<2; i++) {
			minMax[i][0] = getNorthingMinMax(true, i);
			minMax[i][1] = getEastingMinMax(true, i);
			minMax[i][2] = getTVDMinMax(true, i);
		}
	}
	
	public double [] getColumn(int iCol) { 
		double [] v = new double[getNumOfRow()];
		for(int i=0; i<v.length; i++) {
			v[i] = _data[i][iCol];
		}
		return v;
	}
	public String [] getColumnS(int iCol) { 
		String [] v = new String[getNumOfRow()];
		for(int i=0; i<v.length; i++) {
			v[i] = _dataS[i][iCol];
		}
		return v;
	}
	
	public double [] getCoordinate(boolean local, int iAxis) { 
		double [] v = new double[getNumOfRow()];
		for(int i=0; i<v.length; i++) {
			if(iAxis==0) v[i] = getNorthing(local, i);
			else if(iAxis==1) v[i] = getEasting(local, i);
			else if(iAxis==2) v[i] = getTVD(local, i);
		}
		return v;
	}
	
	public void shiftCenter(boolean local, double cN, double cE, double cD) {
		for(int i=0; i<_data.length; i++) {
			setNorthing(local, i, getNorthing(local, i)+cN);
			setEasting(local, i, getEasting(local, i)+cE);
			setTVD(local, i, getTVD(local, i)+cD);
		}
	}
	
	public double getEasting(boolean local, int iRow) { 
		if(_columnName.length==8) { int index=6; if(local) index=6-3; return _data[iRow][index]; } //well
		else if(_columnName.length==10) { int index=6; if(local) index=6-3; return _data[iRow][index]; } //perf
		else if(_columnName.length==11) { int index=7; if(local) index=7-3; return _data[iRow][index]; } //tool
		else return 0;
	}
	public double getNorthing(boolean local, int iRow) { 
		if(_columnName.length==8) { int index=7; if(local) index=7-3; return _data[iRow][index]; } //well
		else if(_columnName.length==10) { int index=7; if(local) index=7-3; return _data[iRow][index]; } //perf
		else if(_columnName.length==11) { int index=8; if(local) index=8-3; return _data[iRow][index]; } //tool
		else return 0;
	}
	public double getTVD(boolean local, int iRow) { 
		if(_columnName.length==8) { int index=2; if(local) index=2; return _data[iRow][index]; } //well
		else if(_columnName.length==10) { int index=2; if(local) index=2; return _data[iRow][index]; } //perf
		else if(_columnName.length==11) { int index=3; if(local) index=3; return _data[iRow][index]; } //tool
		else return 0;
	}
	
	public double getEastingMinMax(boolean local, int iRow) { 
		if(_columnName.length==8) { int index=6; if(local) index=6-3; return _minMax[iRow][index]; } //well
		else if(_columnName.length==10) { int index=6; if(local) index=6-3; return _minMax[iRow][index]; } //perf
		else if(_columnName.length==11) { int index=7; if(local) index=7-3; return _minMax[iRow][index]; } //tool
		else return 0;
	}
	public double getNorthingMinMax(boolean local, int iRow) { 
		if(_columnName.length==8) { int index=7; if(local) index=7-3; return _minMax[iRow][index]; } //well
		else if(_columnName.length==10) { int index=7; if(local) index=7-3; return _minMax[iRow][index]; } //perf
		else if(_columnName.length==11) { int index=8; if(local) index=8-3; return _minMax[iRow][index]; } //tool
		else return 0;
	}
	public double getTVDMinMax(boolean local, int iRow) { 
		if(_columnName.length==8) { int index=2; if(local) index=2; return _minMax[iRow][index]; } //well
		else if(_columnName.length==10) { int index=2; if(local) index=2; return _minMax[iRow][index]; } //perf
		else if(_columnName.length==11) { int index=3; if(local) index=3; return _minMax[iRow][index]; } //tool
		else return 0;
	}
	
	public void setEasting(boolean local, int iRow, double v) { 
		if(_columnName.length==8) { int index=6; if(local) index=6-3; _data[iRow][index] = v; } //well
		else if(_columnName.length==10) { int index=6; if(local) index=6-3; _data[iRow][index] = v; } //perf
		else if(_columnName.length==11) { int index=7; if(local) index=7-3; _data[iRow][index] = v; } //tool
	}
	public void setNorthing(boolean local, int iRow, double v) { 
		if(_columnName.length==8) { int index=7; if(local) index=7-3; _data[iRow][index] = v; } //well
		else if(_columnName.length==10) { int index=7; if(local) index=7-3; _data[iRow][index] = v; } //perf
		else if(_columnName.length==11) { int index=8; if(local) index=8-3; _data[iRow][index] = v; } //tool
	}
	public void setTVD(boolean local, int iRow, double v) { 
		if(_columnName.length==8) { int index=2; if(local) index=2; _data[iRow][index] = v; } //well
		else if(_columnName.length==10) { int index=2; if(local) index=2; _data[iRow][index] = v; } //perf
		else if(_columnName.length==11) { int index=3; if(local) index=3; _data[iRow][index] = v; } //tool
	}

	public String toString() {
		String b = "";
		String a = _columnName[0];

		for(int i=1; i<_columnName.length; i++) {
			a += ", "+_columnName[i];
		}
		b = a;
		
		for(int i=0; i<_data.length; i++) {
			a = _data[i][0]+"";

			for(int j=1; j<_data[i].length; j++) {
				a += ", "+_data[i][j];
			}
			if(_dataS!=null) {
				for(int j=0; j<_dataS[i].length; j++) {
					a += ", "+_dataS[i][j];
				}
			}
			b = b.concat("\n"+a);
		}

		return b;
	}
}
