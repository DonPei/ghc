package com.geohammer.core;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.StringTokenizer;

import javax.swing.JOptionPane;

public class GreenFunction {
	private MSEvent 		_msEvent 	= null;
	private Sensor 			_sensor 	= null;
	private GreenComp [] 	_comps 		= null; //in zyx order {p, sv, sh}
	
	public GreenFunction() {
		this(null, null, null);
	}
	public GreenFunction(String selectedFileName)	{
		this();
		read(selectedFileName);
	}
	public GreenFunction(MSEvent msEvent, Sensor sensor) {
		this(msEvent, sensor, null);
	}
	public GreenFunction(MSEvent msEvent, Sensor sensor, GreenComp [] comps) {
		_msEvent 	= msEvent;
		_sensor 	= sensor;
		_comps 		= comps;
		if(_comps==null)  _comps = new GreenComp[3];
	}
	public GreenFunction(MSEvent msEvent, Sensor sensor, double sampleInterval, float[] data0, float[] data1, float[] data2) {
		this(msEvent, sensor, null);
		_comps[0] = new GreenComp(0, sampleInterval, data0.length, data0);
		_comps[1] = new GreenComp(1, sampleInterval, data1.length, data1);
		_comps[2] = new GreenComp(2, sampleInterval, data2.length, data2);
	}
	
	public MSEvent getEvent() 						{ return _msEvent; }
	public Sensor getSensor() 						{ return _sensor; }
	public GreenComp [] getComp() 					{ return _comps; }
	public GreenComp getComp(int iPSvSh) 			{ return _comps[iPSvSh]; }
	public GreenComp getCompP() 					{ return _comps[0]; }
	public GreenComp getCompSv() 					{ return _comps[1]; }
	public GreenComp getCompSh() 					{ return _comps[2]; }
	
	public void setComp(int iPSvSh, GreenComp comp) { _comps[iPSvSh] = comp; }
	public void setCompP(GreenComp comp) 			{ _comps[0] = comp; }
	public void setCompSv(GreenComp comp) 			{ _comps[1] = comp; }
	public void setCompSh(GreenComp comp) 			{ _comps[2] = comp; }
	
	public String toString() {
		return _comps[0].toString()+" "+_comps[1].toString()+" "+_comps[2].toString();
	}
	
	public void read(String selectedFileName) {
		try{
			BufferedReader reader = new BufferedReader(new FileReader(selectedFileName));
			String line = reader.readLine(); 
			line = reader.readLine();
			StringTokenizer st 		= new StringTokenizer(line, " ,\t");
			int numOfSamples 		= Integer.parseInt(st.nextToken().trim());
			double sampleInterval 	= Double.parseDouble(st.nextToken().trim());
			int nComp 				= Integer.parseInt(st.nextToken().trim());
			
			_comps[0] = new GreenComp(0, sampleInterval, numOfSamples, null);
			_comps[1] = new GreenComp(1, sampleInterval, numOfSamples, null);
			_comps[2] = new GreenComp(2, sampleInterval, numOfSamples, null);
			
			int k = 0;
			while((line = reader.readLine())!=null) {
				st 		= new StringTokenizer(line, " ,\t");
				_comps[0].setData(k, Float.parseFloat(st.nextToken().trim()));
				_comps[1].setData(k, Float.parseFloat(st.nextToken().trim()));
				_comps[2].setData(k, Float.parseFloat(st.nextToken().trim()));
				k++;
			}
			
			reader.close();
		} catch (IOException ioexception) {
			JOptionPane.showMessageDialog(null, "\nFile Format is not right!", 
					"Error", JOptionPane.ERROR_MESSAGE);
		}	
	}
	public void write(String selectedFileName) {
		try{
			BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(selectedFileName, false));
			float [] p = _comps[0].getData();
			float [] sv = _comps[1].getData();
			float [] sh = _comps[2].getData();
			
			int nPoints = p.length;
			bufferedWriter.write("P-comp, " + "SV-comp, "+"SH-comp "+"\n");
			bufferedWriter.write(nPoints + ", "+_comps[0].getSampleInterval()+", "+3+"\n");
			
			for(int i=0; i<nPoints; i++) {
				bufferedWriter.write(p[i] + ", "+sv[i]+", "+sh[i]+"\n");
			}
			
			bufferedWriter.close();
		} catch (IOException ioexception) {
			JOptionPane.showMessageDialog(null, "\nFile Format is not right!", 
					"Error", JOptionPane.ERROR_MESSAGE);
		}
	}
}
