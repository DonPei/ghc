package com.geohammer.core;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import edu.mines.jtk.io.ArrayFile;

public abstract class CommonBinaryHeader {
	protected byte [] 			_bytes 					= null;
	protected String [] 		_itemDescription 		= null;
	protected int [] 			_itemLen 				= null;
	protected String [] 		_itemDescriptionV2 		= null;
	protected int [] 			_itemLenV2 				= null;
	
	public CommonBinaryHeader() { }
	
	public byte [] getByte() { return _bytes; }
	
	public void read(File file) {
		try {
			ArrayFile af = new ArrayFile(file,"r", ByteOrder.LITTLE_ENDIAN, ByteOrder.LITTLE_ENDIAN);
			read(af);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	public void read(ArrayFile af) {	
		try {
			af.seek(0);
			af.read(_bytes);
		} catch (IOException e) {
			System.err.println("Caught IOException: " + e.getMessage());
		} 
	}
	
	public void write(ArrayFile af){
		try {
			af.seek(0);
			// Write the binary file header.
			af.writeBytes(_bytes);
		} catch (IOException e) {
			System.err.println("Caught IOException: " + e.getMessage());
		} 
	}

	public int getInt(String key) {
		int k = getItemIndex(key);
		int len = _itemLen[k];
		byte [] buffer = new byte[len];
		int start = getStartingByteIndex(k);
		int to = start+len;
		//for(int i=start, ik=0; i<to; i++, ik++) buffer[ik] = _bytes[i];	
		for(int i=to-1, ik=0; i>=start; i--, ik++) buffer[ik] = _bytes[i];
		return byteArrayToInt(buffer);		
	}
	public short getShort(String key) {
		int k = getItemIndex(key);
		int len = _itemLen[k];
		byte [] buffer = new byte[len];
		int start = getStartingByteIndex(k);
		int to = start+len;
		//for(int i=start, ik=0; i<to; i++, ik++) buffer[ik] = _bytes[i];	
		for(int i=to-1, ik=0; i>=start; i--, ik++) buffer[ik] = _bytes[i];	
		return byteArrayToShort(buffer);		
	}
	
	public long getLong(String key) {
		int k = getItemIndex(key);
		int len = _itemLen[k];
		byte [] buffer = new byte[len];
		int start = getStartingByteIndex(k);
		int to = start+len;
		//for(int i=start, ik=0; i<to; i++, ik++) buffer[ik] = _bytes[i];	
		for(int i=to-1, ik=0; i>=start; i--, ik++) buffer[ik] = _bytes[i];
		return byteArrayToLong(buffer);		
	}
	
	public double getDouble(String key) {
		int k = getItemIndex(key);
		int len = _itemLen[k];
		byte [] buffer = new byte[len];
		int start = getStartingByteIndex(k);
		int to = start+len;
		//for(int i=start, ik=0; i<to; i++, ik++) buffer[ik] = _bytes[i];	
		for(int i=to-1, ik=0; i>=start; i--, ik++) buffer[ik] = _bytes[i];
		return byteArrayToDouble(buffer);
	}
	public double getFloat(String key) {
		int k = getItemIndex(key);
		int len = _itemLen[k];
		byte [] buffer = new byte[len];
		int start = getStartingByteIndex(k);
		int to = start+len;
		//for(int i=start, ik=0; i<to; i++, ik++) buffer[ik] = _bytes[i];	
		for(int i=to-1, ik=0; i>=start; i--, ik++) buffer[ik] = _bytes[i];
		return byteArrayToFloat(buffer);
	}
	public String getString(String key) {
		int k = getItemIndex(key);
		int len = _itemLen[k];
		byte [] buffer = new byte[len];
		int start = getStartingByteIndex(k);
		int to = start+len;
		for(int i=start, ik=0; i<to; i++, ik++) buffer[ik] = _bytes[i];		
		//for(int i=to-1, ik=0; i>=start; i--, ik++) buffer[ik] = _bytes[i];
		return byteArrayToString(buffer);
	}

	public int getItemIndex(String key) {
		for(int i=0; i<_itemDescription.length; i++) {
			if(key.equalsIgnoreCase(_itemDescription[i])) return i;
		}
		return 0;
	}
	public int getStartingByteIndex(int itemIndex) {
		int sum = 0;
		for(int i=0; i<_itemLen.length; i++) {
			if(itemIndex==i) return sum;
			sum += _itemLen[i];
		}
		return sum;
	}
	public void setShort(String key, short v) {	
		int k = getItemIndex(key);
		int len = _itemLen[k];
		int start = getStartingByteIndex(k);
		int to = start+len;
		byte[] buffer = ByteBuffer.allocate(len).putShort(v).array();
		for(int i=to-1, ik=0; i>=start; i--, ik++) _bytes[i] = buffer[ik];
	}
	public void setInt(String key, int v) {	
		int k = getItemIndex(key);
		int len = _itemLen[k];
		int start = getStartingByteIndex(k);
		int to = start+len;
		byte[] buffer = ByteBuffer.allocate(len).putInt(v).array();
		for(int i=to-1, ik=0; i>=start; i--, ik++) _bytes[i] = buffer[ik];
	}
	public void setLong(String key, long v) {	
		int k = getItemIndex(key);
		int len = _itemLen[k];
		int start = getStartingByteIndex(k);
		int to = start+len;
		byte[] buffer = ByteBuffer.allocate(len).putLong(v).array();
		for(int i=to-1, ik=0; i>=start; i--, ik++) _bytes[i] = buffer[ik];
	}
	public void setDouble(String key, double v) {
		//byte[] buffer = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(v).array();		
		int k = getItemIndex(key);
		int len = _itemLen[k];
		int start = getStartingByteIndex(k);
		int to = start+len;
		byte[] buffer = ByteBuffer.allocate(len).putDouble(v).array();
		for(int i=to-1, ik=0; i>=start; i--, ik++) _bytes[i] = buffer[ik];
	}
	
	public short byteArrayToShort(byte[] bytes) {
	    return ByteBuffer.wrap(bytes).getShort();
	}

	public int byteArrayToInt(byte[] bytes) {
	    return ByteBuffer.wrap(bytes).getInt();
	}

	public float byteArrayToFloat(byte[] bytes) {
	    return ByteBuffer.wrap(bytes).getFloat();
	}

	public double byteArrayToDouble(byte[] bytes) {
	    return ByteBuffer.wrap(bytes).getDouble();
	}

	public long byteArrayToLong(byte[] bytes) {
	    return ByteBuffer.wrap(bytes).getLong();
	}
	
	public String byteArrayToString(byte[] bytes) {
		try {
			//byte[] bytes = doc.getBytes("UTF-8");
			return new String(bytes, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			return null;
		}
	}
	public static  byte[] my_int_to_bb_le(int myInteger){
	    return ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(myInteger).array();
	}

	public static int my_bb_to_int_le(byte [] byteBarray){
	    return ByteBuffer.wrap(byteBarray).order(ByteOrder.LITTLE_ENDIAN).getInt();
	}

	public static  byte[] my_int_to_bb_be(int myInteger){
	    return ByteBuffer.allocate(4).order(ByteOrder.BIG_ENDIAN).putInt(myInteger).array();
	}

	public static int my_bb_to_int_be(byte [] byteBarray){
	    return ByteBuffer.wrap(byteBarray).order(ByteOrder.BIG_ENDIAN).getInt();
	}
	
	private final static char[] hexArray = "0123456789ABCDEF".toCharArray();
	public static String bytesToHex(byte[] bytes) {
	    char[] hexChars = new char[bytes.length * 2];
	    for ( int j = 0; j < bytes.length; j++ ) {
	        int v = bytes[j] & 0xFF;
	        hexChars[j * 2] = hexArray[v >>> 4];
	        hexChars[j * 2 + 1] = hexArray[v & 0x0F];
	    }
	    return new String(hexChars);
	}
}
