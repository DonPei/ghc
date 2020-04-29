package com.geohammer.xml;

import javax.xml.stream.XMLStreamException;

public class DataAttributeOnly extends CommonElement {
	String title = null;
	public DataAttributeOnly(int nAttribute, String title, String [] attributItems) {
		super(0, nAttribute);
		initItemAndValue(new String[] {"Type", "Angle", "Depth"});
	}

	public void writeXml(PrettyXMLStreamWriter writer) {
		try {
			writer.writeStartElement("", "Level", "");

			if(getAttribute()!=null) {
				for(int j=0; j<getAttribute().getNumOfItems(); j++) {
					writer.writeAttribute(getAttribute().getItem(j), getAttribute().getValue(j));
				}
			}
			super.writeXml(writer);
			writer.writeEndElement();
		} catch (XMLStreamException e) {
			e.printStackTrace();
		}
	}
}
