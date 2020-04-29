package com.geohammer.xml;

import javax.xml.stream.XMLStreamException;

public abstract class CommonElement extends CommonBase {
	private Attribute _attribute = null;

	public CommonElement() { this(0, 0); }
	public CommonElement(int nItem) { this(nItem, 0); }
	public CommonElement(int nItem, int nAttrubute) { 
		super(nItem); 
		if(nAttrubute<=0) return;
		else {
			_attribute = new Attribute(nAttrubute);
		}
	}
	
	public void copy(CommonElement source) {
		super.copy(source);
		if(source._attribute!=null) _attribute = source._attribute.copy();
		else _attribute = null;
	}
	
	public Attribute getAttribute() { return _attribute; }
	public void setAttribute(Attribute attribute) { _attribute = attribute; }

	public void writeXml(PrettyXMLStreamWriter writer) {
		try {
			int nLength = getNumOfItems();
			if(nLength<=0) return;
			for(int i=0; i<nLength; i++) {
				String a = getItem(i);
				if(a!=null) {
					writer.writeStartElement("", getItem(i), "");
					writer.writeCharacters(getValue(i));
					writer.writeEndElement();
				}
			}
		} catch (XMLStreamException e) {
			e.printStackTrace();
		}
	}

	public String toString() {
		String separator = System.getProperty( "line.separator" );

		StringBuilder lines = new StringBuilder("");
		String a = null;
		if(_attribute!=null) { lines.append(_attribute.toString()); lines.append( separator );}
		int nLength = getNumOfItems();
		for(int i=0; i<nLength; i++) {
			a = getItem(i)+"="+getValue(i);
			lines.append(" "+a);
		}
		
		lines.append( separator );
		return lines.toString( );
	}
//	public String toSelfString() {
//		return valueToString();
//	}
	public String valueToString() {
		StringBuilder lines = new StringBuilder("");
		int nLength = getNumOfItems();
		for(int i=0; i<nLength; i++) {
			lines.append(getValue(i)+" ");
		}
		return lines.toString( );
	}
	public String itemToString() {
		StringBuilder lines = new StringBuilder("");
		int nLength = getNumOfItems();
		for(int i=0; i<nLength; i++) {
			lines.append(getItem(i)+" ");
		}
		return lines.toString( );
	}
	
	public String itemValuePairToString() {
		String separator = System.getProperty( "line.separator" );
		StringBuilder lines = new StringBuilder("");
		int nLength = getNumOfItems();
		for(int i=0; i<nLength; i++) {
			lines.append(getItem(i)+" = "+getValue(i));
			lines.append( separator );
		}
		return lines.toString( );
	}

}
