package com.geohammer.core.planarmodel;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.StringTokenizer;

import javax.swing.JOptionPane;

import org.apache.commons.io.FilenameUtils;
import com.geohammer.core.MatrixFileReader;
import com.geohammer.xml.CsvSheet;
import com.geohammer.xml.CsvXml;

public class LabRecord {
	public ArrayList<SingleLabRecord> _records = null;

	public LabRecord( ){ }
	public LabRecord( int nBoundaries ){ _records=new ArrayList<SingleLabRecord>(nBoundaries); }
	public LabRecord(String selectedFileName)	{
		_records = read(selectedFileName);
		//System.out.println(toString());
	}

	public void addRecord(int index, SingleLabRecord record) {
		if(index<0) return;
		if(index>_records.size()) return;
		_records.add(index, record);
	}
	public void deleteRecord(int index) {
		if(index<0) return;
		_records.remove(index);
	}
	public void deleteRecord(String layerName) 		{ deleteRecord(getRecordIndex(layerName)); }
	public void deleteRecord(boolean [] removable) 	{
		ArrayList<SingleLabRecord> 	records = new ArrayList<SingleLabRecord>();
		int k = 0;
		for(int i=0; i<_records.size(); i++) {
			if(!removable[k]) {         
				records.add(getRecord(i));     
			} 
			k++;
		}
		_records = records;
	}

	public int getNumOfRecords() 						{ return _records.size(); }
	public SingleLabRecord getRecord(int index) 		{ return _records.get(index); }
	public ArrayList<SingleLabRecord> getRecord() 		{ return _records; }

	public int getRecordIndex(String name) {
		for(int i=0; i<_records.size(); i++) {
			if(_records.get(i).getSampleName().equalsIgnoreCase(name)) { return i; }
		}	
		return 0;
	}
	public int getFileType(String selectedFileName)	{
		String extension = FilenameUtils.getExtension(selectedFileName);
		if(extension.equalsIgnoreCase("csv")) return 0;
		else return 1;
	}
	public ArrayList<SingleLabRecord> read(String selectedFileName) {
		if(getFileType(selectedFileName)==0) { //csv
			return readCsv(selectedFileName);
		} else {
			return readXml(selectedFileName);
		}
	}
	public ArrayList<SingleLabRecord> readCsv(String selectedFileName) {
		ArrayList<SingleLabRecord> records = new ArrayList<SingleLabRecord>();

		try{
			BufferedReader reader = new BufferedReader(new FileReader(selectedFileName));

			String sampleCategory = " ";
			String sampleName = " ";
			String lithology = " ";
			double depth = 0.0;
			double porosity = 0.0;
			double density = 0.0;
			double overburdenPressure = 0.0;
			double porePressure = 0.0;

			double c11 = 0.0;
			double c33 = 0.0;
			double c44 = 0.0;
			double c13 = 0.0;
			double c66 = 0.0;
			double c12 = 0.0;
			double sigma = 0.0;
			double eta = 0.0;
			double delta = 0.0;
			double epsilon = 0.0;
			double gamma = 0.0;

			String line=null;
			StringTokenizer st = null;

			line=reader.readLine();
			st = null;
			int k = 0;
			while((line = reader.readLine())!=null) {
				st = new StringTokenizer(line, ",\t");
				sampleCategory 	= st.nextToken().trim();
				sampleName 		= st.nextToken().trim();
				depth 			= Double.parseDouble(st.nextToken());
				porosity 		= Double.parseDouble(st.nextToken());
				density 		= Double.parseDouble(st.nextToken());
				lithology 		= st.nextToken().trim();
				overburdenPressure 	= Double.parseDouble(st.nextToken());
				porePressure 	= Double.parseDouble(st.nextToken());
				c11 			= Double.parseDouble(st.nextToken());
				c33 			= Double.parseDouble(st.nextToken());
				c44 			= Double.parseDouble(st.nextToken());
				c13 			= Double.parseDouble(st.nextToken());
				c66 			= Double.parseDouble(st.nextToken());
				c12 			= Double.parseDouble(st.nextToken());

				epsilon 		= Double.parseDouble(st.nextToken());
				gamma 			= Double.parseDouble(st.nextToken());
				delta 			= Double.parseDouble(st.nextToken());
				sigma 			= Double.parseDouble(st.nextToken());
				eta 			= Double.parseDouble(st.nextToken());

				double a = 0.0;
				SingleLabRecord record = new SingleLabRecord(k, a, a, density, 
						depth, overburdenPressure, porePressure, porosity, sigma, eta, 
						delta, epsilon, gamma, 0, 0, 0, 0,  
						0, 0, 0, 0, 
						c11, c12, c13, c33, c44, c44, c66, 
						null, lithology, sampleCategory, sampleName);
				record.cijToVelocity(true, true, record.getDen(), 1.0);

				records.add(record);
			}
			reader.close();
		} catch (IOException ioexception) {
			JOptionPane.showMessageDialog(null, "\nFile Format is not right!", 
					"Error", JOptionPane.ERROR_MESSAGE);
		}	
		return records;
	}
	public ArrayList<SingleLabRecord> readXml(String selectedFileName) {
		ArrayList<SingleLabRecord> records = new ArrayList<SingleLabRecord>();

		CsvXml csvXml = new CsvXml(selectedFileName);

		String sampleCategory = " ";
		String sampleName = " ";
		String lithology = " ";
		double depth = 0.0;
		double porosity = 0.0;
		double density = 0.0;
		double overburdenPressure = 0.0;
		double porePressure = 0.0;

		double c11 = 0.0;
		double c33 = 0.0;
		double c44 = 0.0;
		double c13 = 0.0;
		double c66 = 0.0;
		double c12 = 0.0;
		double sigma = 0.0;
		double eta = 0.0;
		double delta = 0.0;
		double epsilon = 0.0;
		double gamma = 0.0;

		CsvSheet csvSheet = csvXml.getSheet(0);
		MatrixFileReader reader = csvSheet.parseDataText();

		int j = 0;
		for(int i=1; i<reader.getRowCount(); i++) {
			j = 0;
			sampleCategory 	= reader.getData(i, j++);
			sampleName 		= reader.getData(i, j++);
			depth 			= Double.parseDouble(reader.getData(i, j++));
			porosity 		= Double.parseDouble(reader.getData(i, j++));
			density 		= Double.parseDouble(reader.getData(i, j++));
			lithology 		= reader.getData(i, j++);
			overburdenPressure 	= Double.parseDouble(reader.getData(i, j++));
			porePressure 	= Double.parseDouble(reader.getData(i, j++));
			c11 			= Double.parseDouble(reader.getData(i, j++));
			c33 			= Double.parseDouble(reader.getData(i, j++));
			c44 			= Double.parseDouble(reader.getData(i, j++));
			c13 			= Double.parseDouble(reader.getData(i, j++));
			c66 			= Double.parseDouble(reader.getData(i, j++));
			c12 			= Double.parseDouble(reader.getData(i, j++));

			epsilon 		= Double.parseDouble(reader.getData(i, j++));
			gamma 			= Double.parseDouble(reader.getData(i, j++));
			delta 			= Double.parseDouble(reader.getData(i, j++));
			sigma 			= Double.parseDouble(reader.getData(i, j++));
			eta 			= Double.parseDouble(reader.getData(i, j++));

			double a = 0.0;
			SingleLabRecord record = new SingleLabRecord(i-1, a, a, density, 
					depth, overburdenPressure, porePressure, porosity, sigma, eta, 
					delta, epsilon, gamma, 0, 0, 0, 0,  
					0, 0, 0, 0, 
					c11, c12, c13, c33, c44, c44, c66, 
					null, lithology, sampleCategory, sampleName);
			record.cijToVelocity(true, true, record.getDen(), 1.0);

			records.add(record);
		}

		return records;
	}
	public void writeRecords(String selectedFileName) {
		try{
			BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(selectedFileName, false));
			bufferedWriter.write(_records.size()+" 0.0 0 \n");

			//			for(int i=0; i<_records.size(); i++) {
			//				SingleLabRecord sl = _records.get(i);
			//				bufferedWriter.write(sl.toString(0)+"\n");
			//				int nxy = sl.getNx()*sl.getNy();
			//				for(int j=0; j<nxy; j++) {
			//					bufferedWriter.write(sl.getDepth(j)+"\n");
			//				}
			//			}
			bufferedWriter.close();
		} catch (IOException ioexception) {
			JOptionPane.showMessageDialog(null, "\nFile Format is not right!", 
					"Error", JOptionPane.ERROR_MESSAGE);
		}
	}

	public String toString() {
		return toString(1);
	}
	public String toString(int id) {
		String b = " ";
		String a = " "; 

		if(id==1) {
			for(int i=0; i<_records.size(); i++) {
				b = new String(b.concat("\n"+_records.get(i).toString()));
			} 
		}


		return b;
	}


}
