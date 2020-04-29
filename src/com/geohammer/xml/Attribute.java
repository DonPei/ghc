package com.geohammer.xml;

import javax.xml.stream.XMLStreamException;

public class Attribute extends CommonBase{
	public Attribute() { 
		this(0);
	}
	public Attribute(int nLength) { 
		super(nLength);
	}
	
	public Attribute copy() {
		if(getNumOfItems()<=0) return null;
		Attribute cp = new Attribute(getNumOfItems());
		cp.copy(this);		
		return cp;
	}
	
	
//	public Attribute copy() {
//		if(getNumOfItems()==0) return null;
//		Attribute cp = new Attribute(getNumOfItems());
//		for(int i=0; i<_item.length; i++) {
//			cp._item[i] = _item[i];
//			cp._value[i] = _value[i];
//		}
//		return cp;
//	}
	
	public String toString() {
		StringBuilder lines = new StringBuilder(" ");
		String a = null;
		int nLength = getNumOfItems();
		for(int i=0; i<nLength; i++) {
			a = getItem(i)+"="+getValue(i);
			lines.append(" "+a);
		}
		return lines.toString( );
	}
	
	public void writeXml(PrettyXMLStreamWriter writer) {
		try {
			int nLength = getNumOfItems();
			if(nLength<=0) return;
			for(int i=0; i<nLength; i++) {
				writer.writeStartElement("", getItem(i), "");
				writer.writeCharacters(getValue(i));
				writer.writeEndElement();
			}
		} catch (XMLStreamException e) {
			e.printStackTrace();
		}
	}
	
	public void write2DAttributeXml(PrettyXMLStreamWriter writer, String item, String value) {
		try {
			int nLength = getNumOfItems();
			if(nLength<=0) return;
			for(int i=0; i<nLength; i++) {
				writer.writeStartElement("", getItem(i), "");
				writer.writeCharacters(getValue(i));
				writer.writeEndElement();
			}
			
			writer.writeStartElement("", item, "");
			writer.writeCharacters(value);
			writer.writeEndElement();
		} catch (XMLStreamException e) {
			e.printStackTrace();
		}
	}
	

}

