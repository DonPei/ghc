package com.geohammer.segy;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;

import edu.mines.jtk.io.ArrayFile;

public class SEGYFileHeader {
	byte [] _bytes 			= new byte[400];
	byte [] _ebcdic 		= new byte[3200];
	public String _EBCDIC 	= null; 

	public SEGYFileHeader() {
	}
	public byte [] getByte() { return _bytes; }
	
	public void readSEGYFileHeader(ArrayFile af) {	
		try {
			af.seek(0);
			af.read(_ebcdic);
			_EBCDIC = new String(_ebcdic, "Cp1047");
			af.read(_bytes);
		} catch (IOException e) {
			System.err.println("Caught IOException: " + e.getMessage());
		} 
	}
	public void writeSEGYFileHeader(ArrayFile af){
		try {
			af.seek(0);
			//String[] text = generateTextHeader(description);
			//String[] text = fetchTextHeader();
			//byte[] textHdr = makeTextHeaderBytes(text,"EBCDIC_CHARSET");
			byte[] textHdr = makeTextHeaderBytes(_EBCDICArray,"IBM-1047");
			af.writeBytes(textHdr);

			// Write the binary file header.
			af.writeBytes(_bytes);
		} catch (IOException e) {
			System.err.println("Caught IOException: " + e.getMessage());
		} 
	}
	
	public String [] getEBCDICArray() 			{ return _EBCDICArray; }
	public String getEBCDICArray(int index) 	{ return _EBCDICArray[index]; }
	
	public void setEBCDICArray(int index, String v) 	{ _EBCDICArray[index] = v; }
	public void setEBCDICArray(String [] v) 			{ _EBCDICArray = v; }
	
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


	private String [] _itemDescription = new String[] {
			"Job identification number",
			"Line number",
			"Reel number",
			"Number of data traces per record",
			"Number of auxiliary traces per record",

			"Sample interval in µsec (for this reel of data)",
			"Sample interval in µsec (for original field recording)",
			"Number of samples per data trace (for this reel of data)",
			"Number of samples per data trace (for original field recording)",
			"Data sample format code",

			"CDP fold",
			"Trace sorting code",
			"Vertical sum code",
			"Sweep frequency at start",
			"Sweep frequency at end",

			"Sweep length (msec)",
			"Sweep type code",
			"Trace number of sweep channel",
			"Sweep trace taper length in msec at start",
			"Sweep trace taper length in msec at end",

			"Taper type",
			"Correlated data traces",
			"Binary gain recovered",
			"Amplitude recovery method",
			"Measurement system",

			"Impulse signal polarity",
			"Vibratory polarity code",
			"Unassigned"
	};

	private int [] _itemPos = new int[] {
			1,   5,  9, 13, 15, 
			17, 19, 21, 23, 25, 
			27, 29, 31, 33, 35, 
			37, 39, 41, 43, 45, 
			47, 49, 51, 53, 55,
			57, 59, 61, 401
	};

	private String[] _EBCDICArray = new String[] {
			"C 1 CLIENT                        COMPANY                       CREW NO", 
			"C 2 LINE            AREA                        MAP ID", 
			"C 3 REEL NO           DAY-START OF REEL     YEAR      OBSERVER", 
			"C 4 INSTRUMENT: MFG            MODEL            SERIAL NO", 
			"C 5 DATA TRACES/RECORD        AUXILIARY TRACES/RECORD         CDP FOLD", 
			"C 6 SAMPLE INTERVAL         SAMPLES/TRACE       BITS/IN      BYTES/SAMPLE", 
			"C 7 RECORDING FORMAT        FORMAT THIS REEL        MEASUREMENT SYSTEM", 
			"C 8 SAMPLE CODE: FLOATING PT     FIXED PT     FIXED PT-GAIN     CORRELATED", 
			"C 9 GAIN TYPE:  FIXED     BINARY     FLOATING POINT     OTHER ", 
			"C10 FILTERS: ALIAS     HZ  NOTCH     HZ  BAND     -     HZ  SLOPE    -    DB/OCT", 
			"C11 SOURCE: TYPE            NUMBER/POINT        POINT INTERVAL", 
			"C12     PATTERN:                           LENGTH       WIDTH", 
			"C13 SWEEP: START     HZ  END     HZ  LENGTH      MS  CHANNEL NO     TYPE", 
			"C14 TAPER: START LENGTH       MS  END LENGTH       MS  TYPE", 
			"C15 SPREAD: OFFSET        MAX DISTANCE        GROUP INTERVAL", 
			"C16 GEOPHONES: PER GROUP     SPACING     FREQUENCY     MFG          MODEL", 
			"C17     PATTERN:                            LENGTH       WIDTH", 
			"C18 TRACES SORTED BY: RECORD     CDP      OTHER", 
			"C19 AMPLITUDE RECOVERY: NONE      SPHERICAL DIV       AGC    OTHER", 
			"C20 MAP PROJECTION                      ZONE ID       COORDINATE UNITS", 
			"C21 PROCESSING: Pinnacle, Halliburton Services", 
			"C22 PROCESSING: Pinnacle MicroSeismic SEG Y Writer Version 0.1 ", 
			"C23", 
			"C24", 
			"C25", 
			"C26", 
			"C27", 
			"C28", 
			"C29", 
			"C30", 
			"C31", 
			"C32", 
			"C33", 
			"C34", 
			"C35", 
			"C36", 
			"C37", 
			"C38", 
			"C39 SEG Y REV0", 
			"C40 END TEXTUAL HEADER"
	};

	/**
	 * Convert an array of strings into an array of bytes.
	 * The returned array is guaranteed to be 3200 bytes long, representing
	 * the first 80 characters of each of the first 40 strings.  If any of
	 * the strings are less than 80 characters, or if there are fewer than
	 * 40 strings, the strings will be padded with spaces.
	 * @param lines Array of strings.
	 * @param encoding The character encoding used to for the conversion.
	 * @return Array of 3200 bytes.
	 * @throws IllegalArgumentException If the specified charset is not supported.
	 */
	public static byte[] makeTextHeaderBytes(String[] lines,String encoding) {
		if (!Charset.isSupported(encoding)) {
			throw new IllegalArgumentException(
					"Charset "+encoding+" is not supported.");
		}

		int nlines = 40;
		int ncols = 80;
		byte[] bytes = new byte[nlines*ncols];

		try {
			// Make an array of encoded space characters for padding.
			byte[] spaces = null;
			StringBuilder sb = new StringBuilder(ncols);
			while (sb.length()<ncols)
				sb.append(" ");
			spaces = sb.toString().getBytes(encoding);
			assert spaces.length==ncols;

			// Populate the byte array one line at a time.
			for (int i=0; i<nlines; ++i) {
				int ncopy = 0;
				if (i<lines.length) {
					// Encode the line.
					byte[] lineBytes = lines[i].getBytes(encoding);

					// Copy at most 80 characters to the byte array.
					ncopy = Math.min(lineBytes.length,ncols);
					assert ncopy>=0 && ncopy<=ncols;
					System.arraycopy(lineBytes,0,bytes,i*ncols,ncopy);
				}
				// Pad the line to 80 characters (using spaces).
				int npad = ncols-ncopy;
				assert npad>=0 && npad<=ncols;
				System.arraycopy(spaces,0,bytes,i*ncols+ncopy,npad);
			}
		} catch (UnsupportedEncodingException ex) {
			// Should not happen because we checked for support above.
			throw new IllegalStateException(ex);
		}

		return bytes;
	}

}
