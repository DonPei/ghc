package com.geohammer.xml;

import javax.xml.stream.XMLStreamException;

public class CommonElementData extends CommonElement {
	String _tagName = null;
	String [] _items = null;
	
	public CommonElementData(int nAttribute, String tagName, String [] items) {
		super(0, nAttribute);
		_tagName = tagName;
		_items = items;
		
		if(items!=null) initItemAndValue(items);
	}

	public CommonElementData(int nItems, int nAttribute, String tagName, String [] items) {
		super(nItems, nAttribute);
		_tagName = tagName;
		_items = items;
		
		if(items!=null) initItemAndValue(items);
	}
	
	public void copy(CommonElementData source) {
		super.copy(source);
		if(source._tagName!=null) _tagName = source._tagName;
		
		String [] itemsCp = null;
		if(source._items!=null) {
			itemsCp = new String[_items.length];
			for(int i=0; i<source._items.length; i++) itemsCp[i] = source._items[i];
			_items = itemsCp;
			initItemAndValue(itemsCp);
		} else _items = null;
	}
	
	public CommonElementData copy() {
		int nAttribute = 0;
		if(getAttribute()!=null) nAttribute = getAttribute().getNumOfItems();
		String [] itemsCp = null;
		if(_items!=null) {
			itemsCp = new String[_items.length];
			for(int i=0; i<_items.length; i++) itemsCp[i] = _items[i];
		}
		CommonElementData cp = new CommonElementData(getNumOfItems(), nAttribute, _tagName, itemsCp);
		cp.copy(this);	
		
		return cp;
	}
	
	public void writeXml(PrettyXMLStreamWriter writer) {
		try {
			writer.writeStartElement("", _tagName, "");

			if(getAttribute()!=null) {
				for(int j=0; j<getAttribute().getNumOfItems(); j++) {
					writer.writeAttribute(getAttribute().getItem(j), getAttribute().getValue(j));
				}
			}
			if(_items==null) {
				super.writeXml(writer);
			}
			writer.writeEndElement();
		} catch (XMLStreamException e) {
			e.printStackTrace();
		}
	}
}
