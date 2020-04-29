package com.geohammer.segy;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;

//https://gist.github.com/TheRyanBurke/1649308

public class ByteUtil {

	public static int byteArrayToInt(byte[] b) {
		if (b.length == 4)
			return b[0] << 24 | (b[1] & 0xff) << 16 | (b[2] & 0xff) << 8
					| (b[3] & 0xff);
		else if (b.length == 2)
			return 0x00 << 24 | 0x00 << 16 | (b[0] & 0xff) << 8 | (b[1] & 0xff);

		return 0;
	}

	public static byte[] intToFourByteArray(int value) {
		byte[] b = new byte[4];
		for (int i = 0; i < 4; i++) {
			int offset = (b.length - 1 - i) * 8;
			b[i] = (byte) ((value >>> offset) & 0xFF);
		}
		return b;
	}
	
	public static byte[] longToEightByteArray(int value) {
		byte[] b = new byte[8];
		for (int i = 0; i <b.length; i++) {
			int offset = (b.length - 1 - i) * 8;
			b[i] = (byte) ((value >>> offset) & 0xFF);
		}
		
		return b;
	}
	
	public static byte[] intToTwoByteArray(int value) {
		byte[] b = new byte[2];
		b[0] = (byte)((value >>> 8) & 0xFF);
		b[1] = (byte)(value & 0xFF);
		
		return b;
	}

	//Convert a byte array representing an unsigned integer (4bytes) to its long value
	public static final long unsignedIntToLong(byte[] b) {
		long l = 0;
		l |= b[0] & 0xFF;
		l <<= 8;
		l |= b[1] & 0xFF;
		l <<= 8;
		l |= b[2] & 0xFF;
		l <<= 8;
		l |= b[3] & 0xFF;
		return l;
	}


	public static final int oneByteToInteger(byte value) throws UtilityException {
		return (int)value & 0xFF;
	}
	public static final int threeByteToInteger(byte [] value) throws UtilityException {
		if (value.length < 3) {
			throw new UtilityException("Byte array too short!");
		}
		int r = ((value[0] & 0xFF) << 16) | ((value[1] & 0xFF) << 8) | (value[2] & 0xFF);
		
		return r;
	}

	public static final int twoBytesToInteger(byte[] value) throws UtilityException {
		if (value.length < 2) {
			throw new UtilityException("Byte array too short!");
		}
		int temp0 = value[0] & 0xFF;
		int temp1 = value[1] & 0xFF;
		return ((temp0 << 8) + temp1);
	}
	
	//https://stackoverflow.com/questions/1026761/how-to-convert-a-byte-array-to-its-numeric-value-java
	public static final long eightBytesToLong(byte[] by) {
//		if (by.length < 8) {
//			throw new UtilityException("Byte array too short!");
//		}
		long value = 0;
		
//		Assuming the first byte is the least significant byte big-endian
//		for (int i = 0; i < by.length; i++) {
//		   value += ((long) by[i] & 0xffL) << (8 * i);
//		}
		
		//Is the first byte the most significant little-endian
		for (int i = 0; i < by.length; i++) {
		   value = (value << 8) + (by[i] & 0xff);
		}
		return value;
	}

	public static final String eightBytesToString(byte[] by) {
		try {
			//byte[] bytes = doc.getBytes("UTF-8");
			return new String(by, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			return null;
		}
	}
	
	public static final byte integerToOneByte(int value) throws UtilityException {
		if ((value > Math.pow(2,15)) || (value < 0)) {
			throw new UtilityException("Integer value " + value + " is larger than 2^15");
		}
		return (byte)(value & 0xFF);
	}

	public static final byte[] integerToTwoBytes(int value) throws UtilityException {
		byte[] result = new byte[2];
		if ((value > Math.pow(2,31)) || (value < 0)) {
			throw new UtilityException("Integer value " + value + " is larger than 2^31");
		}
		result[0] = (byte)((value >>> 8) & 0xFF);
		result[1] = (byte)(value & 0xFF);
		return result; 
	}

	public static final byte[] integerToFourBytes(int value) throws UtilityException {
		byte[] result = new byte[4];
		if ((value > Math.pow(2,63)) || (value < 0)) {
			throw new UtilityException("Integer value " + value + " is larger than 2^63");
		}
		result[0] = (byte)((value >>> 24) & 0xFF);
		result[1] = (byte)((value >>> 16) & 0xFF);
		result[2] = (byte)((value >>> 8) & 0xFF);
		result[3] = (byte)(value & 0xFF);
		return result; 
	}

	public static class UtilityException extends Exception {
		private static final long serialVersionUID = 3545800974716581680L;

		UtilityException(String mesg) {
			super(mesg);
		}
	}

	static final boolean isPowerOfTwo (int value)  {
		if(value != 0) {
			return (value & -value) == value;
		} else {
			return false;
		}
	}

}
