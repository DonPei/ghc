package com.geohammer.segy;

public class SEGYTraceHeader {
	byte [] _bytes = new byte[240];
	
	public SEGYTraceHeader() {
	}
	
	public int getIndex(int byteNo) {
		int index = 0;
		if(byteNo==1) 			index = 0;
		else if(byteNo==5) 		index = 1;
		return index;		
	}

	public byte [] getByte() { return _bytes; }
	public int getInt(int byteNo, int len) {
		byte [] buffer = new byte[len];
		int start = byteNo-1;
		int to = start+len;
		for(int i=start, k=0; i<to; i++, k++) buffer[k] = _bytes[i];		
		return ByteUtil.byteArrayToInt(buffer);		
	}
	
	public void setInt(int byteNo, int len, int value) {
		byte [] buffer = null;
		if(len==2) buffer = ByteUtil.intToTwoByteArray(value);
		else if(len==4) buffer = ByteUtil.intToFourByteArray(value);
		
		int start = byteNo-1;
		int to = start+len;
		for(int i=start, k=0; i<to; i++, k++) _bytes[i] = buffer[k];	
	}
	
	public void setInt1(int byteNo, int len, int value) {
		byte [] buffer = null;
		if(len==2) buffer = ByteUtil.intToTwoByteArray(value);
		else if(len==4) buffer = ByteUtil.intToFourByteArray(value);
		
		int start = byteNo-1;
		int to = start+len;
		for(int i=start, k=0; i<to; i++, k++) _bytes[i] = buffer[k];	
	}
	
	public void printClass(int len) {
		byte [] buffer = new byte[len];
		int result = 0;
		int nItem = 240/len;
		for(int i=0, k=0; i<nItem; i++) {
			for(int j=0; j<len; j++, k++) buffer[j] = _bytes[k];
			result = ByteUtil.byteArrayToInt(buffer);
			System.out.println((i+1)+"    "+result);
		}
	}
	
	public void test1() {
		int value = 10;
		//byte[] intByte = ByteUtil.intToFourByteArray(value);
		byte[] intByte = ByteUtil.intToTwoByteArray(value);
		int result = ByteUtil.byteArrayToInt(intByte);
		System.out.println(result);
	}
	
	
	 
//	public static void main(String args[]) {
//		new SEGYTraceHeader();
//	}
}

