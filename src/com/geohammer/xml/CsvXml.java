package com.geohammer.xml;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Stack;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import org.apache.commons.io.FilenameUtils;
import com.geohammer.core.MatrixFileReader;

//http://www.informit.com/library/content.aspx?b=STY_XML_21days&seqNum=221
public class CsvXml extends CommonElement {
	public ArrayList<CsvSheet>			_sheets  		= null;
	
	public ArrayList<CsvSheet> getSheets() 		{return _sheets; }
	public CsvSheet getSheet(int index) 		{return _sheets.get(index); }

	public CsvXml(String selectedFileName) {
		super(0, 0);
		initItemAndValue(new String[] {"version"});
		setValue(new String[] {"1.0"});
		read(selectedFileName);
		//printClass();
		//write(System.out);
		//write(selectedFileName+".xml");
		//printClass();
	}
	public int getFileType(String selectedFileName)	{
		String extension = FilenameUtils.getExtension(selectedFileName);
		if(extension.equalsIgnoreCase("csv")) return 0;
		else return 1;
	}
	public void read(String selectedFileName) {
		if(getFileType(selectedFileName)==0) { //csv
			readCsv(selectedFileName);
		} else {
			readXml(selectedFileName);
		}
	}
	public void readCsv(String selectedFileName) {
		MatrixFileReader reader = new MatrixFileReader(selectedFileName, 0, 0, null, ",");
		//reader.printClass();
		CsvSheet csvSheet = new CsvSheet(3, "literature");
		csvSheet.parseMatrixFileReader(reader);
		//csvSheet.printClass();
		
		_sheets = new ArrayList<CsvSheet>();
		_sheets.add(csvSheet);
	}
	public void readXml(String selectedFileName) {
		SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
		try {
			SAXParser saxParser = saxParserFactory.newSAXParser();
			CsvXmlReadFileHandler handler = new CsvXmlReadFileHandler();
			saxParser.parse(new File(selectedFileName), handler);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}
	}

	public void write(String selectedFileName) {
		if(getFileType(selectedFileName)==0) { //csv
			//writeCsv(selectedFileName);
		} else {
			writeXml(selectedFileName);
		}
	}
	public void writeXml(String selectedFileName) {
		try {
			//Writer outputStreamWriter = new OutputStreamWriter(System.out, "UTF-8");
			FileOutputStream fileOutputStream = new FileOutputStream(selectedFileName);
			Writer outputStreamWriter = new OutputStreamWriter(fileOutputStream, "UTF-8");
			XMLOutputFactory outputFactory = XMLOutputFactory.newInstance();

			XMLStreamWriter xmlw = outputFactory.createXMLStreamWriter(outputStreamWriter);
			PrettyXMLStreamWriter writer = new PrettyXMLStreamWriter(xmlw);

			writer.writeStartDocument("UTF-8", "1.0");

			writer.writeStartElement("", "database", "");
			if(getAttribute()!=null) {
				for(int j=0; j<getAttribute().getNumOfItems(); j++) {
					writer.writeAttribute(getAttribute().getItem(j), getAttribute().getValue(j));
				}
			}
			super.writeXml(writer);
			for(int i=0; i<_sheets.size(); i++) {
				_sheets.get(i).writeXml(writer);
			}

			writer.writeEndElement();

			writer.writeEndDocument();

			writer.flush();
			fileOutputStream.close();
			writer.close();
			//xmlw.flush();
			xmlw.close();
		} 
		catch (FileNotFoundException e1) { 
			System.out.println(e1);
			e1.printStackTrace();
		} catch (XMLStreamException e1) {
			System.out.println(e1);
			e1.printStackTrace();
		} catch (UnsupportedEncodingException e1) {
			System.out.println(e1);
			e1.printStackTrace();
		} catch (IOException e1) {
			System.out.println(e1);
			e1.printStackTrace();
		} 
	}
	
	
	public void printClass() {
//		int numOfWells = _wells.getWell().size();
//		//int numOfWells = 1;
//		for(int i=0; i<numOfWells; i++) {
//			Well well = _wells.getWell(i);
//			well.printClass();
//		}
	}


	private class CsvXmlReadFileHandler extends DefaultHandler {
		String 		_tmpValue 	= null;

		private Stack<String> 	_elementStack 	= new Stack<String>();

		private CsvSheet _csvSheet 		= null;

		@Override
		public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
			_elementStack.push(qName);
			_tmpValue = "";

			if(qName.equalsIgnoreCase("database")){
				for(int i=0; i<attributes.getLength(); i++) {
					getAttribute().setItem(i, attributes.getLocalName(i));
					getAttribute().setValue(i, attributes.getValue(i));
				}
			} else if(qName.equalsIgnoreCase("table")){
				_csvSheet = new CsvSheet(attributes.getLength());
				for(int i=0; i<attributes.getLength(); i++) {
					_csvSheet.getAttribute().setItem(i, attributes.getLocalName(i));
					_csvSheet.getAttribute().setValue(i, attributes.getValue(i));
				}
			} 
		}
		//http://tutorials.jenkov.com/java-xml/sax-example.html
		@Override
		public void endElement(String uri, String localName, String qName) throws SAXException {
			//System.out.println("qName="+qName+" currentElement="+currentElement()+
			//		" localName="+localName+" currentElementParent="+currentElementParent());
			if(currentElementParent().equalsIgnoreCase("database")){
				if(_tmpValue!=null) {
					if(_tmpValue.length() > 0) { // ignore white space
						for(int i=0; i<getNumOfItems(); i++) {
							if(getItem(i).equalsIgnoreCase(currentElement())) setValue(i, _tmpValue);
						}
					}
				}
			} else if(currentElementParent().equalsIgnoreCase("table")) {
				//System.out.println("qName="+qName+" currentElement="+currentElement()+" localName="+localName);
				if(currentElement().equalsIgnoreCase("data")){
					_csvSheet.setDataText(_tmpValue);
				} 
				
				if(_sheets==null) _sheets = new ArrayList<CsvSheet>();
				_sheets.add(_csvSheet);
			} 
			_elementStack.pop();
		}

		@Override
		public void characters(char ch[], int start, int length) throws SAXException {
			_tmpValue += new String(ch, start, length);
			//System.out.println(_tmpValue);
		}

		private String currentElement() {
			return _elementStack.peek();
		}

		private String currentElementParent() {
			if(_elementStack.size() < 2) return "";
			return _elementStack.get(_elementStack.size()-2);
		}

	}

}
