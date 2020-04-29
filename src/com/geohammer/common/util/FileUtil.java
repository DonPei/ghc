package com.geohammer.common.util;

import java.io.*;
import java.util.*;
import java.util.logging.*;

/** Collected methods to ease manipulation of text files.*/
public final class FileUtil {

  /**
  * Return the full content of <tt>aInputStream</tt> as a <tt>String</tt>.
  *
  * <P>If <tt>aInputStream</tt> has no content, then return an empty 
  * <tt>String</tt>.
  */
  public static String getStreamAsString(InputStream aInputStream){
    StringBuilder result = new StringBuilder(Consts.EMPTY_STRING);
    try {
      BufferedReader input = new BufferedReader(new InputStreamReader(aInputStream));
      try { 
        String line = null;
        while (( line = input.readLine()) != null){
          result.append(line);
          result.append(Consts.NEW_LINE);
        }
      }
      finally {
        input.close();
      }
    }
    catch(IOException ex){
      fLogger.severe("Cannot read input stream:" + aInputStream);
    }
    return result.toString();
  }
  
  /**
  * Return the full content of <tt>aInputStream</tt> as an unmodifiable 
  * <tt>List</tt> of <tt>String</tt>s, one for each line of input. 
  *
  * <P>The iteration order of the returned <tt>List</tt> reflects the 
  * line order in the underlying stream. If <tt>aInputStream</tt> has 
  * no content, then return an empty <tt>List</tt>.
  *
  * <P>Intended for text files having structured data, with each line 
  * corresponding to a single record. The caller may use this method to transform 
  * such a file into a collection, which may be iterated in the usual fashion.
  */
  public static List<String> getStreamAsList(InputStream aInputStream){
    List<String> result = new ArrayList<String>();
    try {
      BufferedReader input = new BufferedReader( new InputStreamReader(aInputStream) );
      try {       
        String line = null;
        while (( line = input.readLine()) != null){
          result.add(line);
        }
      }
      finally {
        input.close();
      }
    }
    catch(IOException ex){
      fLogger.severe("Cannot read input stream:" + aInputStream);
    }
    return Collections.unmodifiableList(result);
  }
  
  // PRIVATE //
  private static final Logger fLogger = Util.getLogger(FileUtil.class);  
}
