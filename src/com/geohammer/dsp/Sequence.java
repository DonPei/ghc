package com.geohammer.dsp;

import edu.mines.jtk.dsp.*;
import edu.mines.jtk.util.Check;
import static edu.mines.jtk.util.ArrayMath.*;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.InputMismatchException;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A sequence is a real-valued sampled function of one variable.
 * A {@link Sequence} combines a {@link edu.mines.jtk.dsp.Sampling} with 
 * a reference to an an array of function values. Because array values 
 * are referenced (not copied), the cost of wrapping any array with a 
 * {@link Sequence} is small.
 * <p> 
 * One consequence of referencing the array of function values is that 
 * changes to elements in such an array are reflected in <em>all</em> 
 * {@link Sequences}s that reference that array. If this behavior is not
 * desired, the copy constructor {@link Sequence#Sequence(Sequence)} 
 * creates a new array copy of function values.
 * @author Dave Hale, Colorado School of Mines
 * @version 2006.08.19
 */
public class Sequence {

	/**
	 * Constructs a sequence with specified sampling and values zero.
	 * @param s the sampling.
	 */
	public Sequence(Sampling s) {
		this(s,new float[s.getCount()]);
		_s = s;
		_v = new float[s.getCount()];
	}

	/**
	 * Constructs a sequence with specified values and default sampling.
	 * The default sampling has number (count) of samples = v.length, 
	 * sampling interval (delta) = 1.0 and first sample value (first) = 0.0.
	 * @param v array of sequence values; referenced, not copied.
	 */
	public Sequence(float[] v) {
		_s = new Sampling(v.length,1.0,0.0);
		_v = v;
	}

	/**
	 * Constructs a sequence with specified sampling and values.
	 * @param s the sampling.
	 * @param v array of sequence values; referenced, not copied.
	 *  The array length v.length must equal the number of samples in s.
	 */
	public Sequence(Sampling s, float[] v) {
		Check.argument(s.getCount()==v.length,
				"v.length equals the number of samples in s");
		_s = s;
		_v = v;
	}

	/**
	 * Constructs a sequence with specified sampling and values zero.
	 * @param n the number (count) of samples.
	 * @param d the sampling interval (delta).
	 * @param f the value of the first sample.
	 */
	public Sequence(int n, double d, double f) {
		this(new Sampling(n,d,f));
	}

	/**
	 * Constructs a sequence with specified sampling and values.
	 * @param n the number (count) of time samples.
	 * @param d the sampling interval (delta).
	 * @param f the value of the first sample.
	 * @param v array of sequence values; referenced, not copied. 
	 *  The array length v.length must equal n.
	 */
	public Sequence(int n, double d, double f, float[] v) {
		this(new Sampling(n,d,f),v);
	}

	/**
	 * Constructs a copy of the specified sampled sequence. This constructor 
	 * <em>copies</em> (does not reference) the array of function values from 
	 * the specified sequence.
	 * @param s the sequence to copy.
	 */
	public Sequence(Sequence s) {
		this(s._s,copy(s._v));
	}

	/**
	 * Gets the sampling for this sequence.
	 * @return the sampling.
	 */
	public Sampling getSampling() {
		return _s;
	}

	/**
	 * Gets the array of values for this sequence.
	 * @return the array of values; by reference, not by copy.
	 */
	public float[] getValues() {
		return _v;
	}

	/**
	 * Reads a sequence from the specified file.
	 * @param fileName name of file containing the sequence.
	 * @return the sequence.
	 */
	public static Sequence read(String fileName) {
		return read(fileName,1);
	}

	/**
	 * Reads a sequence from the specified column of the specified file.
	 * @param fileName name of file containing the sequence.
	 * @return the sequence.
	 */
	public static Sequence read(String fileName, int column) {
		BufferedReader br = null;
		try {
			java.net.URL url = Sequence.class.getResource("data/"+fileName);
			Reader reader;
			if (url!=null) {
				reader = new InputStreamReader(url.openStream());
			} else {
				reader = new FileReader(fileName);
			}
			br = new BufferedReader(reader);
		} catch (IOException ioe) {
			throw new RuntimeException("Cannot open file: "+fileName);
		}
		try {
			Scanner s = new Scanner(br);
			s.findWithinHorizon("nt=",0);
			int nt = s.nextInt();
			s.findWithinHorizon("dt=",0);
			double dt = s.nextDouble();
			s.findWithinHorizon("ft=",0);
			double ft = s.nextDouble();
			s.findWithinHorizon("x=",0);
			s.nextLine();
			float[] x = new float[nt];
			for (int it=0; it<nt; ++it) {
				for (int skip=1; skip<column; ++skip)
					s.next();
				x[it] = s.nextFloat();
				s.nextLine();
			}
			s.close();
			return new Sequence(nt,dt,ft,x);
		} catch (InputMismatchException ime) {
			throw new RuntimeException("Unknown format of file: "+fileName);
		}
	}

	///////////////////////////////////////////////////////////////////////////
	// Not used, yet.

	/*
  scanf()               Regular Expression 
  %c 	                .
  %5c 	                .{5}
  %d 	                [-+]?\d+
  %e, %E, %f, %g 	[-+]?(\d+(\.\d*)?|\d*\.\d+)([eE][-+]?\d+)?
  %i 	                [-+]?(0[xX][\dA-Fa-f]+|0[0-7]*|\d+)
  %o 	                0[0-7]*
  %s 	                \S+
  %u 	                \d+
  %x, %X 	        0[xX][\dA-Fa-f]+
	 */
	// quoted string: "[^"\\\r\n]*(\\.[^"\\\r\n]*)*"

	private static class Named {
		public String name;
		public boolean found;
		public Named(String name) {
			this(name,"\\s");
		}
		protected Named(String name, String pattern) {
			this.name = name;
			_pattern = Pattern.compile(name+"=("+pattern+")");
		}
		protected String findString(String s) {
			Matcher m = _pattern.matcher(s);
			if (!m.find())
				return null;
			found = true;
			return m.group(1);
		}
		private Pattern _pattern;

		private static class Integer extends Named {
			public int value;
			public Integer(String name, int value) {
				super(name,"([-+]?\\d+)");
			}
			public boolean find(String s) {
				String v = super.findString(s);
				if (v==null)
					return false;
				value = java.lang.Integer.parseInt(v);
				found = true;
				return true;
			}
		}

		private static class Double extends Named {
			public double value;
			public Double(String name, double value) {
				super(name,"[-+]?(\\d+(\\.\\d*)?|\\d*\\.\\d+)([eE][-+]?\\d+)?");
			}
			public boolean find(String s) {
				String v = super.findString(s);
				if (v==null)
					return false;
				value = java.lang.Double.parseDouble(v);
				found = true;
				return true;
			}
		}
	}
	
//	public static void main(String[] args) {
//		String fileName = "hunter1.txt";
//		int column = 1;
//		if (args.length>0)
//			fileName = args[0];
//		if (args.length>1)
//			column = Integer.parseInt(args[1]);
//		Sequence x = read(fileName,column);
//		Sampling s = x.getSampling();
//		int nt = s.getCount();
//		double dt = s.getDelta();
//		double ft = s.getFirst();
//		System.out.println("In "+fileName);
//		System.out.println("  nt="+nt+" dt="+dt+" ft="+ft);
//	}

	///////////////////////////////////////////////////////////////////////////
	// private

	Sampling _s;
	float[] _v;
}

