package com.geohammer.xml;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamWriter;

public class XMLStreamWriterDemo {
	public static void main(String[] args) throws Exception {
		XMLOutputFactory factory = XMLOutputFactory.newInstance();
		XMLStreamWriter xmlw = factory.createXMLStreamWriter(System.out);
		PrettyXMLStreamWriter writer = new PrettyXMLStreamWriter(xmlw);
		writer.writeStartDocument("1.0");

		writer.writeStartElement("catalog");

		writer.writeStartElement("book");

		writer.writeAttribute("id", "1");

		writer.writeStartElement("code");
		writer.writeCharacters("I01");
		writer.writeCharacters(" I02");
		writer.writeCharacters(" I03");
		writer.writeCharacters(" I04");
		
		writer.writeEndElement();

		writer.writeStartElement(""+"title"+"");
		writer.writeCharacters("This is the title");
		writer.writeEndElement();

		writer.writeStartElement("price");
		writer.writeCharacters("$2.95");
		writer.writeEndElement();

		writer.writeEndElement();
		
		writer.writeEndElement();
		
		writer.writeEndDocument();

		writer.flush();
		writer.close();
	}
}

