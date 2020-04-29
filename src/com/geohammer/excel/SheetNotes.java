package com.geohammer.excel;

import com.geohammer.xml.CommonBase;

public class SheetNotes extends CommonBase {
	
	public SheetNotes(int length) {
		super(length);
	}
	
	public SheetNotes copy() {
		SheetNotes target = new SheetNotes(0);
		target.copy(this);
		return target;
	}
	
	public String toString() {
		String separator = System.getProperty( "line.separator" );

		String [] item = getItem();
		String [] value = getValue();
		StringBuilder lines = new StringBuilder("");
		String a = null;
		for(int i=0; i<value.length; i++) {
			a = item[i]+"="+value[i];
			lines.append(" "+a);
			lines.append( separator );
		}
		lines.append( separator );
		return lines.toString( );
	}
	
	public String getHeaderValue(String itemName) {
		String [] item = getItem();
		String [] value = getValue();
		for(int i=0; i<item.length; i++) {
			if(item[i].equalsIgnoreCase(itemName)) return value[i];
		}
		return null;
	}

}
