package com.geohammer.xml;


public abstract class CommonBase {
	protected String [] _item = null;
	protected String [] _value = null;

	public CommonBase() { 
		this(0);
	}
	public CommonBase(int length) { 
		if(length<=0) return;
		else {
			_item =  new String[length];
			_value = new String[_item.length];
		}
	}
	
	public void copy(CommonBase source) {
		if(source._item!=null) {
			int n = source.getNumOfItems();
			if(_item==null || _item.length!=n) {
				_item =  new String[n];
				_value = new String[n];
			}
			for(int i=0; i<n; i++) _item[i] = source._item[i];
			for(int i=0; i<n; i++) _value[i] = source._value[i];
		} else {
			_item =  null;
			_value = null;
		}
	}
	public void addItemAtFirst(String key, String v) 	{ addItem(0, key, v); }
	public void addItemAtLast(String key, String v) 	{ addItem(_item.length, key, v); }
	public void addItem(int index, String key, String v) {
		String [] item = new String[_item.length+1];
		String [] value = new String[item.length];
		int k = 0;
		for(int i=0; i<index; i++, k++) {
			item[i] = _item[k]+"";
			value[i] = _value[k]+"";
		}
		item[index] = key;
		value[index] = v;
		for(int i=index+1; i<item.length; i++, k++) {
			item[i] = _item[k]+"";
			value[i] = _value[k]+"";
		}
		
		_item = item;
		_value = value;
	}
	public void setItem(int index, String key, String v) {
		_item[index] = key;
		_value[index] = v;
	}

	public void initItems1(String [] s) 		{ _item = s; }
	public void initValues(String [] s) 		{ _value = s; }	
	public void initItemAndValue(String [] s) 	{ 
		_item = s; 
		_value = new String[_item.length]; 
		for(int i=0; i<_value.length; i++) _value[i] = "0";
	}
	
	
	public int getNumOfItems() 					{ if(_item==null) return 0; else return _item.length; }
	public int getItemIndex(String s) 			{
		for(int i=0; i<_item.length; i++) {
			if(_item[i].equalsIgnoreCase(s)) return i;
		}
		return -1;
	}
	
	public String [] getItem() 					{return _item; }
	public String getItem(int index) 			{return _item[index]; }
	public String [] getValue() 				{return _value; }
	public String getValue(int index) 			{return _value[index]; }
	public String getValue(String s) 			{int k=getItemIndex(s); if(k>=0) return _value[k]; else return null;}
	
	public void setValue(String [] s) 			{ _value = s; }
	public void setValue(int index, String s) 	{ _value[index] = s; }
	public void setValue(String key, String s) 	{ int k=getItemIndex(key); if(k>=0) setValue(k, s); }
	public void setItem(String [] s) 			{ _item = s; }
	public void setItem(int index, String s) 	{ _item[index] = s; }

	public String toString() {
		String separator = System.getProperty( "line.separator" );

		StringBuilder lines = new StringBuilder("");
		String a = null;
		for(int i=0; i<_value.length; i++) {
			a = _item[i]+"="+_value[i];
			lines.append(" "+a);
		}
		lines.append( separator );
		return lines.toString( );
	}

}

