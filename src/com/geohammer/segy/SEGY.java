package com.geohammer.segy;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteOrder;

import com.geohammer.core.SeismicTraceComponent;
import com.geohammer.segy.SEGYFileHeader;
import com.geohammer.segy.SEGYTraceHeader;

import edu.mines.jtk.io.ArrayFile;

public class SEGY {

	private SEGYFileHeader 		_fh 	= new SEGYFileHeader();
	private SEGYTraceHeader [] 	_th 	= null;
	private float [][] 			_data 	= null;

	public SEGY() {

	}
	public SEGY(String fileName) {
		readSEGY(fileName);
	}
	public SEGY(Gather gather, String [] EBCDICArray)	{
		int nRcvs 				= gather.getNumOfReceivers();
		int nCompPerReceiver 	= gather.getNumOfCompPerReceiver();
		int nSegyTraceData 		= nRcvs*nCompPerReceiver;

		int numOfSamples 		= gather.getSensor(0).getComp(0).getNumOfSamples();
		int sampleinterval 		= (int)(1000000.0*gather.getSensor(0).getComp(0).getSampleInterval());

		if(EBCDICArray!=null) _fh.setEBCDICArray(EBCDICArray);
		_fh.setInt(13, 2, nSegyTraceData);
		_fh.setInt(15, 2, nSegyTraceData);
		_fh.setInt(17, 2, sampleinterval);
		_fh.setInt(19, 2, sampleinterval);
		_fh.setInt(21, 2, numOfSamples);
		_fh.setInt(23, 2, numOfSamples);
		_fh.setInt(25, 2, 5); //IEEE float
		_fh.setInt(27, 2, 1);
		_fh.setInt(29, 2, 5);
		_fh.setInt(31, 2, 1);

		_fh.setInt(47, 2, 1);
		_fh.setInt(49, 2, 1);
		_fh.setInt(51, 2, 1);
		_fh.setInt(53, 2, 1);
		_fh.setInt(55, 2, 2);
		_fh.setInt(57, 2, 1);
		_fh.setInt(59, 2, 0);		
		
		_fh.setInt(303, 2, 1);

		_data 	= new float[nSegyTraceData][];
		_th 	= new SEGYTraceHeader[nSegyTraceData];

		for(int i=0, k=0; i<gather.getNumOfReceivers(); i++) {
			for(int j=0; j<gather.getNumOfCompPerReceiver(); j++, k++) {
				SeismicTraceComponent comp = gather.getSensor(i).getComp(j);
				_data[k] = comp.getData();
				_th[k] = gather.getHeader()[k];
			}
		}
		//System.out.println(Arrays.toString(_data[38]));
	}

	
	public SEGYFileHeader getFileHeader() 				{ return _fh; }
	public SEGYTraceHeader [] getTraceHeaders() 		{ return _th; }
	public SEGYTraceHeader getTraceHeader(int index) 	{ return _th[index]; }
	public void readSEGY(String selectedFileName) {
		ArrayFile af 		= null;
		try {
			File file = new File(selectedFileName);
			if (!file.exists ()) return; 
			file = new File(selectedFileName);
			af = new ArrayFile(file,"rw", ByteOrder.BIG_ENDIAN, ByteOrder.BIG_ENDIAN);

			_fh.readSEGYFileHeader(af);
			af.seek(3600);

			int numOfTraces 	= _fh.getInt(13, 2); 	//getNumOfSamples();
			int numOfSamples 	= _fh.getInt(21, 2); 	//getNumOfSamples();
			int dataFormat 		= _fh.getInt(25, 2); 	//getDataFormat();
			
			int dataI [] 		= new int[numOfSamples];
			short dataSS [] 	= new short[numOfSamples];
			float [] traceData 	= new float[numOfSamples];
			
			System.out.println("numOfTraces="+numOfTraces);
			//int totalNumOfTraces = _th.length;
			
			_th = new SEGYTraceHeader[numOfTraces];
			_data = new float[numOfTraces][numOfSamples];
			int ix = 0;
			int iy = 0;
			for(int k=0; k<numOfTraces; k++) {
				_th[k] = new SEGYTraceHeader();
				af.read(_th[k].getByte());
				
				if (dataFormat==1) { /* 4-byte IBM Float */
					af.readInts(dataI);
					for(int i=0; i<numOfSamples; i++) traceData[i] = intBitsIBMToFloat(dataI[i]);
				} else if (dataFormat==2) { /* 4-byte integer */
					af.readInts(dataI);
					for(int i=0; i<numOfSamples; i++)  traceData[i] = dataI[i]; 
				} else if (dataFormat==3) { /* 2-byte integer */ 
					af.readShorts(dataSS);
					for(int i=0; i<numOfSamples; i++)  traceData[i] = dataSS[i];
				} else if (dataFormat==4) { /* 4-byte integer with gain code. obsolete */
				} else if (dataFormat==5) { /* 4-byte IEEE/Sun/Java Float */
					af.readFloats(traceData); 
				} else {/* 4-byte IEEE/Sun/Java Float */
					af.readFloats(traceData); 
				}
				for(int i=0; i<numOfSamples; i++) { _data[k][i] = traceData[i]; }
			}
		} catch (FileNotFoundException e) {
			System.err.println("Caught FileNotFoundException: " + e.getMessage());
			throw new RuntimeException(e);
		} catch (IOException e) {
			System.err.println("Caught IOException: " + e.getMessage());
		} finally {
			if (af!=null) try { af.close(); } catch (IOException e) {}
		}
	}
	public void writeSEGY(String selectedFileName, int dataFormat) {
		_fh.setInt(25, 2, dataFormat);
		writeSEGY(selectedFileName);
	}
	public void writeSEGY(String selectedFileName) {
		ArrayFile af 		= null;
		try {
			File file = new File(selectedFileName);
			if (file.exists ()) {
				file.delete();
			}
			file = new File(selectedFileName);
			af = new ArrayFile(file,"rw", ByteOrder.BIG_ENDIAN, ByteOrder.BIG_ENDIAN);

			_fh.writeSEGYFileHeader(af);
			af.seek(3600);

			int numOfSamples 	= _fh.getInt(21, 2); 	//getNumOfSamples();
			int dataFormat 		= _fh.getInt(25, 2); 	//getDataFormat();
			
			int dataI [] 		= new int[numOfSamples];
			short dataSS [] 	= new short[numOfSamples];
			float [] traceData 	= new float[numOfSamples];
			
			
			int totalNumOfTraces = _th.length;
			
			int ix = 0;
			int iy = 0;
			for(int k=0; k<totalNumOfTraces; k++) {
				af.write(_th[k].getByte());
				for(int i=0; i<numOfSamples; i++) { traceData[i] = _data[k][i]; }
				
				if (dataFormat==1) { /* 4-byte IBM Float */
					for(int i=0; i<numOfSamples; i++) dataI[i] = floatToIntBitsIBM(traceData[i]); 
					af.writeInts(dataI);
				} else if (dataFormat==2) { /* 4-byte integer */
					for(int i=0; i<numOfSamples; i++)  dataI[i] = (int)( traceData[i]); 
					af.writeInts(dataI);
				} else if (dataFormat==3) { /* 2-byte integer */
					for(int i=0; i<numOfSamples; i++)  dataSS[i] = (short)(traceData[i]); 
					af.writeShorts(dataSS);
				} else if (dataFormat==4) { /* 4-byte integer with gain code. obsolete */
				} else if (dataFormat==5) { /* 4-byte IEEE/Sun/Java Float */
					af.writeFloats(traceData); 
				} else {/* 4-byte IEEE/Sun/Java Float */
					af.writeFloats(traceData); 
				}
			}
		} catch (FileNotFoundException e) {
			System.err.println("Caught FileNotFoundException: " + e.getMessage());
			throw new RuntimeException(e);
		} catch (IOException e) {
			System.err.println("Caught IOException: " + e.getMessage());
		} finally {
			if (af!=null) try { af.close(); } catch (IOException e) {}
		}
	}
	/* float_to_ibm(long from[], long to[], long n)
	   from the Colorado School of Mines, 1996 */
	/* adapted for Java by J. Louie 9/11/00 */
	public static int floatToIntBitsIBM(float from)	{
		int fconv, fmant, t;
		fconv = Float.floatToIntBits(from);
		if (fconv != 0)
		{
			fmant = (0x007fffff & fconv) | 0x00800000;
			t = (int) ((0x7f800000 & fconv) >> 23) - 126;
			while ((t & 0x3) != 0)
			{
				++t;
				fmant >>= 1;
			}
			fconv = (0x80000000 & fconv) | (((t>>2) + 64) << 24) | fmant;
		}
		return fconv;
	}

	//	// http://en.wikipedia.org/wiki/IBM_Floating_Point_Architecture
	//    // float2ibm(-118.625F) == 0xC276A000
	//    // 1 100 0010    0111 0110 1010 0000 0000 0000
	//    // IBM/370 single precision, 4 bytes
	//    // xxxx.xxxx xxxx.xxxx xxxx.xxxx xxxx.xxxx
	//    // s|-exp--| |--------fraction-----------|
	//    //    (7)          (24)
	//    // value = (-1)**s * 16**(e - 64) * .f   range = 5E-79 ... 7E+75
	//    static int float2ibm(float from)
	//    {
	//        byte[] bytes = BitConverter.GetBytes(from);
	//        int fconv = (bytes[3] << 24) | (bytes[2] << 16) | (bytes[1] << 8)| bytes[0];
	//
	//        if (fconv == 0) return 0;
	//        int fmant = (0x007fffff & fconv) | 0x00800000;
	//        int t = (int)((0x7f800000 & fconv) >> 23) - 126;
	//        while (0 != (t & 0x3)) { ++t; fmant >>= 1; }
	//        fconv = (int)(0x80000000 & fconv) | (((t >> 2) + 64) << 24) | fmant;
	//        return fconv; // big endian order
	//    }

	/***************************************************************************
	ibm2ieee: routine to convert from short (32 bit) ibm-standard floating
	point numbers (see Barry et al., Geophysics, 40, 2, 344-352, 1975) to ieee-
	standard floating point numbers. This routine was adapted from "ibmiee.c"
	(written by M. Weidersphan, U.T., Austin) to convert numbers "in place".
	 *****************************************************************************/
	/* adapted for Java by J. Louie 7/23/99 */
	public static float intBitsIBMToFloat(int fpn) {
		int exponent, mantissa, sign;

		if ((fpn & MSK_MANT_IBM) == 0)  return 0F; 

		mantissa = (fpn & MSK_MANT_IBM);
		exponent = ((((fpn & MSK_EXP_IBM) >> 24) - 64) << 2 );
		sign = (fpn & MSK_SIGN_IBM);

		while ((mantissa & MSK_NORM_IEEE) == 0) {
			mantissa <<= 1;  /* normalize */
			exponent-- ;
		}
		mantissa = mantissa & 0x7fffff;    /* shift understood one out */
		exponent += 126;
		if (( exponent < 0 ) || (exponent > 255)) /* IBM floating point exponent out of range */
			exponent = 0;
		exponent <<= 23;
		return Float.intBitsToFloat(sign | exponent | mantissa);
	}
	
	public static int MSK_MANT_IBM=0xffffff;
	public static int MSK_EXP_IBM=0x7f000000;
	public static int MSK_SIGN_IBM=0x80000000;
	public static int MSK_NORM_IEEE=0x800000;
	public static int MSK_NO_SIGN=0x7fffffff;
	
	public static String [] _itemDescription = new String[] {
		"Trace sequence number in the line",
		"Trace sequence number in the reel",
		"Original field record number",
		"Trace number within the original field record",
		"Energy source point number",

		"CDP ensemble number",
		"Trace number within the CDP ensemble",
		"Trace identification code",
		"Number of vertically summed traces",
		"Number of horizontally stacked traces",

		"Data use",
		"Distance from source point to receiver group",
		"Receiver group elevation",
		"Surface elevation at source",
		"Source depth below surface ",

		"Datum elevation at receiver group",
		"Datum elevation at source",
		"Water depth at source",
		"Water depth at group",
		"Scaler to be applied to all elevations and depths",

		"Scaler to be applied to all coordinates",
		"Source coordinate X",
		"Source coordinate Y",
		"Group coordinate X",
		"Group coordinate Y",

		"Coordinate units",
		"Weathering velocity",
		"Subweathering velocity",
		"Uphole time at source",
		"Uphole time at group",

		"Source static correction",
		"Group static correction",
		"Total static applied",
		"Lag time A",
		"Lag Time B",

		"Delay according time",
		"Brute time start",
		"Mute time end",
		"Number of samples in this trace",
		"Sample interval in µsec for this trace",

		"Gain type of field instruments",
		"Instrument gain constant",
		"Instrument early or initial gain (dB)",
		"Correlated",
		"Sweep frequency at start",

		"Sweep frequency at end",
		"Sweep length in ms",
		"Sweep type",
		"Sweep trace taper length at start in ms",
		"Sweep trace taper length at end in ms",

		"Taper type",
		"Alias filter frequency",
		"Alias filter slope",
		"Notch filter frequency",
		"Notch filter slope",

		"Low cut frequency",
		"High cut frequency",
		"Low cut slope",
		"High cut slope",
		"Year data recorded ",

		"Day of year",
		"Hour of day",
		"Minute of hour",
		"Second of minute",
		"Time basis code",

		"Trace weighting factor",
		"Geophone group number of roll switch position one",
		"Geophone group number of trace number one within original field record",
		"Geophone group number of last trace within original field record",
		"Gap size",

		"Overtravel associated with taper at beginning or end of line",
		"X coordinates of cdp",
		"Y coordinates of cdp",
		"In-line number",
		"Cross-line number",

		"Shotpoint number",
		"Shotpoint number scalar",
		"Trace value measurement unit",
		"Transduction constant - mantissa",
		"Transduction constant - power",
		"Transduction unit",
		"Device/Trace identifier",
		"Scalar to apply to times specified in bytes 95-114",
		"Source Type",
		"Source energy direction - mantissa",
		"Source energy direction - power",
		"Source measurement - mantissa",
		"Source measurement - power",
		"Source measurement unit",
		"Unassigned"
	};

	public static int [] _itemPos = new int[] {
		1,   5,  9, 13, 17, 21, 25, 29, 31, 33, //10
		35, 37, 41, 45, 49, 53, 57, 61, 65, 69, //20
		71, 73, 77, 81, 85, 89, 91, 93, 95, 97, //30
		99,101,103,105,107,109,111,113,115,117, //40
		119,121,123,125,127,129,131,133,135,137, //50
		139,141,143,145,147,149,151,153,155,157, //60
		159,161,163,165,167,169,171,173,175,177, //70
		179,181,185,189,193,197,201,203,205,209, //80
		211,213,215,217,219,221,225,229,231,233,241 //91
	};

	
	//https://github.com/dhale/idh/blob/master/bench/src/segy/IbmIeee.java
	  public static void shortToFloat(short[] s, float[] f) {
	    int n = s.length;
	    for (int i=0; i<n; ++i)
	      f[i] = s[i];
	  }
	  public static void ieeeToFloat(int[] ieee, float[] f) {
	    int n = ieee.length;
	    for (int i=0; i<n; ++i)
	      f[i] = ieeeToFloat(ieee[i]);
	  }
	  public static void ibmToFloat(int[] ibm, float[] f) {
	    int n = ibm.length;
	    for (int i=0; i<n; ++i)
	      f[i] = ibmToFloat(ibm[i]);
	  }
	  public static float ieeeToFloat(int ieee) {
	    return Float.intBitsToFloat(ieee);
	  }
	  public static float ibmToFloat(int ibm) {
	    // 1) Extract sign bit, exponent, and mantissa.
	    // 2) Convert exponent: subtract 64, multiply by 4, subtract 1, add 127
	    // 3) Convert mantissa:
	    //    while high mantissa bit is zero {
	    //      shift mantissa left (low order bits are zeroed)
	    //      decrement exponent
	    // 4) Put sign and exponent bits back in number
	    // 5) Reverse order of bytes?
	    /*
	    ibm = ((ibm    )     )<<24 |
	          ((ibm>> 8)&0xff)<<16 |
	          ((ibm>>16)&0xff)<< 8 |
	          ((ibm>>24)&0xff);
	    */
	    int s = 0x80000000&ibm; // sign bit
	    int e = 0x7f000000&ibm; // exponent
	    int m = 0x00ffffff&ibm; // mantissa
	    int ieee = 0;
	    if (m!=0) {
	      e = (e>>22)-130; // = ((e>>24)-64)*4 - 1 + 127
	      while ((m&0x00800000)==0) {
	        m <<= 1;
	        --e;
	      }
	      if (e<=0) {
	        ieee = 0;
	      } else if (e>=255) {
	        ieee = s|0x7f7fffff;
	      } else {
	        ieee = s|(e<<23)|(m&0x007fffff);
	      }
	    }
	    /*
	    ieee = ((ieee    )     )<<24 |
	           ((ieee>> 8)&0xff)<<16 |
	           ((ieee>>16)&0xff)<< 8 |
	           ((ieee>>24)&0xff);
	    */
	    return Float.intBitsToFloat(ieee);
	  }
	  private static float ibmToFloatSu(int ibm) {
	    int fconv = ((ibm    )     )<<24 |
	                ((ibm>> 8)&0xff)<<16 |
	                ((ibm>>16)&0xff)<< 8 |
	                ((ibm>>24)&0xff);
	    int fmant = 0x00ffffff&fconv;
	    if (fconv!=0 && fmant!=0) {
	      int t = ((0x7f000000&fconv)>>>22)-130;
	      while ((fmant&0x00800000)==0) {
	        --t;
	        fmant <<= 1;
	      }
	      if (t>254) {
	        fconv = (0x80000000&fconv)|0x7f7fffff;
	      } else if (t<=0) {
	        fconv = 0;
	      } else {
	        fconv = (0x80000000&fconv)|(t<<23)|(0x007fffff&fmant);
	      }
	    }
	    return Float.intBitsToFloat(fconv);
	  }


}
