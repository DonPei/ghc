package com.geohammer.xml;

import javax.xml.stream.XMLStreamException;

import com.geohammer.core.MatrixFileReader;

public class CsvSheet extends CommonElement {
	private String 				_dataText 			= null;
	private MatrixFileReader 	_matrixFileReader 	= null;
	
	public CsvSheet(int nAttribute) {
		super(0, nAttribute);
	}
	public CsvSheet(int nAttribute, String tagName) {
		super(0, nAttribute);
		if(getAttribute()!=null) {
			getAttribute().setItem(0, "name"); getAttribute().setValue(0, tagName);
			getAttribute().setItem(1, "nRows"); getAttribute().setValue(1, "1");
			getAttribute().setItem(2, "nCols"); getAttribute().setValue(2, "1");
		}
	}
	
	public MatrixFileReader getMatrixFileReader() 	{ return _matrixFileReader; }
	public String getDataText() 					{ return _dataText; }
	
	public void setMatrixFileReader(MatrixFileReader matrixFileReader) { _matrixFileReader = matrixFileReader; }
	public void setDataText(String dataText) 		{ _dataText = dataText; }
	
	public MatrixFileReader parseDataText() { 
		//default is comma and space delimiter = ",|\\s+";
		String delimiter = ",";
		String[] splits = _dataText.split(delimiter);
		int nRows = Integer.parseInt(getAttribute().getValue(1));
		int nCols = Integer.parseInt(getAttribute().getValue(2));
		
		MatrixFileReader matrixFileReader = new MatrixFileReader();
		matrixFileReader.allocate(nRows, nCols);
		
		for(int i=0, k=0; i<nRows; i++) {
			for(int j=0; j<nCols; j++) {
				matrixFileReader.setData(i, j, splits[k++]);
			}
			//System.out.println(Arrays.toString(_data[i]));
		}
		return matrixFileReader;
	}
	
	public void parseMatrixFileReader(MatrixFileReader matrixFileReader) { 
		String b = matrixFileReader.getData(0, 0)+"";
		for(int i=0; i<matrixFileReader.getRowCount(); i++) {
			for(int j=0; j<matrixFileReader.getColumnCount(); j++) {
				if(i==0&&j==0) continue;
				b = new String(b.concat(", " + matrixFileReader.getData(i, j)));
			}
		}
		getAttribute().setValue(1, matrixFileReader.getRowCount()+"");
		getAttribute().setValue(2, matrixFileReader.getColumnCount()+"");
		_dataText = b;
	}
	
	
	public void writeXml(PrettyXMLStreamWriter writer) {
		try {
			writer.writeStartElement("", "table", "");
			if(getAttribute()!=null) {
				for(int j=0; j<getAttribute().getNumOfItems(); j++) {
					writer.writeAttribute(getAttribute().getItem(j), getAttribute().getValue(j));
				}
			}
			
			writer.writeStartElement("", "data", "");
			writer.writeCharacters(_dataText);
			writer.writeEndElement();
			
			writer.writeEndElement();
		} catch (XMLStreamException e) {
			e.printStackTrace();
		}
	}
	
	public String toString() {
		String separator = System.getProperty( "line.separator" );
		
		StringBuilder lines = new StringBuilder(
				"colName0="+getAttribute().getValue("colName0") + 
				" colName1="+getAttribute().getValue("colName1") + 
				" colName2="+getAttribute().getValue("colName2") + 
				" colName3="+getAttribute().getValue("colName3") + 
				" colName4="+getAttribute().getValue("colName4") + 
				" colName5="+getAttribute().getValue("colName5") + 
				" numColumns="+getAttribute().getValue("numColumns") +
				" numPoints="+getAttribute().getValue("numPoints") );
		//lines.append( separator );
		return lines.toString( );
	}
	
	public void printClass() {
		System.out.println(_dataText);
	}
}
