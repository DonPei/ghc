package com.geohammer.common.util;

/**
* Hides low-level exceptions in a higher-level abstraction, to hide details
* regarding the data storage mechanism. 
* See <a href=http://www.javapractices.com/Topic77.cjp>Data Exception Wrapping</a>
* topic for discussion.
*
*<P> Every constructor maps to a corresponding <tt>Exception</tt> constructor,
* and every constructor is package-private (it does not make sense to construct 
* these objects outside the <tt>data</tt> layer). See <tt>Exception</tt>
* for all conditions on constructor arguments.
*
* <P><tt>Throwable.getStackTrace</tt> returns all underlying exceptions.
* <tt>Throwable.getCause</tt> returns the underlying cause.
* (This standard exception chaining mechanism was introduced in JDK 1.4.)
*/
public final class DataAccessException extends Exception { 

  public DataAccessException(){
    super();
  }

  public DataAccessException(String aMessage) {
    super(aMessage);
  }

  public DataAccessException(String aMessage, Throwable aThrowable){
    super(aMessage, aThrowable);
  }

  public DataAccessException(Throwable aThrowable){
    super(aThrowable);
  }
} 
