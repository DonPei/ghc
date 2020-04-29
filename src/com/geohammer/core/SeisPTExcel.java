package com.geohammer.core;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.StringTokenizer;

import javax.swing.JOptionPane;

//import jxl.Cell;
//import jxl.CellType;
//import jxl.NumberCell;
//import jxl.Sheet;
//import jxl.Workbook;
//import jxl.read.biff.BiffException;



import org.ucdm.seg2.SEG2;

public class SeisPTExcel {
	Project _project 		= null;
	FracWell _fracWell 		= null;
	ObsWell [] _obsWells 	= null;
	MSEvent [] _msEvents 	= null;
	
	public SeisPTExcel(String selectedFileName) {
		read(selectedFileName);
		readSeismicTrace();
	}
	
	public int getNumOfMSEvents() 				{ return _msEvents.length; }
	public MSEvent [] getMSEvents() 			{ return _msEvents; }
	public MSEvent getMSEvents(int index) 		{ return _msEvents[index]; }
	public int getNumOfObsWells() 				{ return _obsWells.length; }
	public ObsWell [] getObsWells() 			{ return _obsWells; }
	public ObsWell getObsWells(int index) 		{ return _obsWells[index]; }
	public FracWell getFracWell() 				{ return _fracWell; }
	public Project getProject() 				{ return _project; }
	
	public void readSeismicTrace() {
		//for(int i=0; i<1; i++) {
			for(int i=0; i<getNumOfMSEvents(); i++) {
			MSEvent msEvent = _msEvents[i];
			String fileName = "C:\\PINN\\MTInv\\Example\\"+msEvent.getLabel()+".dat";
			SEG2 seg2 = new SEG2(msEvent.getPpicks(), msEvent.getSVpicks(), msEvent.getSHpicks(), msEvent.getAngles(), 
					fileName);
			//System.out.println(seg2.toString());
			
			for(int j=0, nComps = 0; j<getNumOfObsWells(); j++) {
				int nSensors = _obsWells[j].getNumOfSensors();
				
				int [] selectedIndex = new int[3*nSensors];
				for(int k=0; k<selectedIndex.length; k++) {
					selectedIndex[k] = k+j*nComps;
				}
				SEG2 obsTraces = seg2.selectTraces(selectedIndex);
				//System.out.println(obs1.toString());
				SeismicTraceWell stWell = new SeismicTraceWell(_obsWells[j].getId(), obsTraces.getSEG2Trace());
				//System.out.println(stWell1.toString());
				
				msEvent.setSTWells(j, stWell);
				
				nComps = selectedIndex.length;
				
//				int nx = stWell.getSensor(0).getCompZ().getNumOfSamples();
//				double dx = stWell.getSensor(0).getCompZ().getSampleInterval();
//				SeisPTUtil.plotPoints(nx, dx, 0.0, new float[][] {stWell.getSensor(0).getCompZ().getData(),
//						stWell.getSensor(1).getCompZ().getData(),
//						stWell.getSensor(2).getCompZ().getData(),
//						stWell.getSensor(3).getCompZ().getData(),
//						stWell.getSensor(4).getCompZ().getData(),
//						stWell.getSensor(5).getCompZ().getData()});
			}
		}
	}
	
//	public void readExcel(String selectedFileName) throws IOException  {
//		File inputWorkbook = new File(selectedFileName);
//		Workbook w;
//		try {
//			w = Workbook.getWorkbook(inputWorkbook);
//			Sheet sheet = w.getSheet("ReFrac2-Adv");
//			int iCol1 = 3;
//			int iCol2 = 388;
//			int nEvents = iCol2-iCol1+1;
//			_msEvents = new MSEvent[nEvents];
//
//			double sampleIncrement = 0.00025;
//			for (int i=iCol1; i<=iCol2; i++) {
//				double x = ((NumberCell)(sheet.getCell("AS"+i))).getValue();
//				double y = ((NumberCell)(sheet.getCell("AT"+i))).getValue();
//				double z = ((NumberCell)(sheet.getCell("AV"+i))).getValue();
//				double hour = ((NumberCell)(sheet.getCell("AZ"+i))).getValue();
//				double min = ((NumberCell)(sheet.getCell("BA"+i))).getValue();
//				double sec = ((NumberCell)(sheet.getCell("BB"+i))).getValue();
//				
////				_msEvents[i] = new MSEvent(x, y, z, hour, min, sec, 
////						sampleIncrement, fileName, nPicks, _obsWells.length);
//
//			}
//			
//			for (int j = 0; j < sheet.getColumns(); j++) {
//				for (int i = 0; i < sheet.getRows(); i++) {
//					Cell cell = sheet.getCell(j, i);
//					CellType type = cell.getType();
//					if (type == CellType.LABEL) {
//						System.out.println("I got a label " + cell.getContents());
//					}
//
//					if (type == CellType.NUMBER) {
//						System.out.println("I got a number " + cell.getContents());
//					}
//
//				}
//			}
//		} catch (BiffException e) {
//			e.printStackTrace();
//		}
//	}
	
	public void read(String selectedFileName) {
		try{
			BufferedReader reader = new BufferedReader(new FileReader(selectedFileName));
			String line = reader.readLine(); // 1st line line 'Project Information'
			line = reader.readLine(); //2nd line 'SiteName, Date, TimeZone'
			
			StringTokenizer st = new StringTokenizer(line, ",\t");
			String siteName = st.nextToken().trim();
			String dateS = st.nextToken().trim();
			String timeZone = st.nextToken().trim();
			st = new StringTokenizer(dateS, "/");
			int month = Integer.parseInt(st.nextToken().trim());
			int day = Integer.parseInt(st.nextToken().trim());
			int year = Integer.parseInt(st.nextToken().trim());

			line = reader.readLine(); //3rd line 'Length unit: meters'
			st = new StringTokenizer(line, ":\t");
			st.nextToken();
			String unit = st.nextToken().trim();
			
			line = reader.readLine(); //4th line for coordinates (East North UTM_Zone KBElev)
			//Reference point is the first tool in the first observation well
			st = new StringTokenizer(line, " \t");
			double utmE = Double.parseDouble(st.nextToken().trim());
			double utmN = Double.parseDouble(st.nextToken().trim());
			String utmZone = st.nextToken().trim();
			double kbElev = Double.parseDouble(st.nextToken().trim());
			
			line = reader.readLine(); //5th line for Project setup
			st = new StringTokenizer(line, ":\t");
			st.nextToken();
			String dimension = st.nextToken().trim();
			
			_project = new Project(siteName, year, month, day, timeZone, unit, 
					utmE, utmN, utmZone, kbElev, dimension);
			
			line = reader.readLine(); //6th line empty
			line = reader.readLine(); //7th line 'Fracturing Information'
			line = reader.readLine(); //8th line (FracSurfX, FracSurfY, FracSurfKBElev) relative to reference points
			st = new StringTokenizer(line, " ,\t");
			double x = Double.parseDouble(st.nextToken().trim());
			double y = Double.parseDouble(st.nextToken().trim());
			double z = Double.parseDouble(st.nextToken().trim());
			
			line = reader.readLine(); //9th line number of performation
			st = new StringTokenizer(line, " \t");
			int nPerfs = Integer.parseInt(st.nextToken().trim());
			_fracWell = new FracWell(nPerfs, 0, "Frac_Well", x, y, z, kbElev);
			
			line = reader.readLine(); //10th line "Easting Northing Depth"
			for(int i=0; i<nPerfs; i++) {
				line = reader.readLine(); 
				st = new StringTokenizer(line, " \t");
				_fracWell._px[i] = Double.parseDouble(st.nextToken().trim());
				_fracWell._py[i] = Double.parseDouble(st.nextToken().trim());
				_fracWell._pz[i] = Double.parseDouble(st.nextToken().trim());
				_fracWell._stageNo[i] = Integer.parseInt(st.nextToken().trim());
				_fracWell._fileName[i] = st.nextToken().trim();
			}
			
			line = reader.readLine(); //empty line separates Perf and Observation Well Information
			line = reader.readLine(); //line for 'Observation Well Information'
			line = reader.readLine(); //line for number of observation well
			st = new StringTokenizer(line, " \t");
			int nObsWells = Integer.parseInt(st.nextToken().trim());
			
			_obsWells = new ObsWell[nObsWells];
			for(int i=0; i<nObsWells; i++) {
				line = reader.readLine(); //observation well location (E, N, KB) related to reference point
				st = new StringTokenizer(line, " ,\t");
				x = Double.parseDouble(st.nextToken().trim());
				y = Double.parseDouble(st.nextToken().trim());
				z = Double.parseDouble(st.nextToken().trim());
				
				line = reader.readLine(); //num of tools in current obs well
				st = new StringTokenizer(line, " \t");
				int nSensor = Integer.parseInt(st.nextToken().trim());
				
				line = reader.readLine(); //Tool channel order
				st = new StringTokenizer(line, " \t");
				String chanOrder = st.nextToken().trim();
				_obsWells[i] = new ObsWell(nSensor, i, "Obs_Well"+i, x, y, z, kbElev, "zyx", dimension);
				for(int j=0; j<nSensor; j++) {
					//read in sensors (East, North, Depth, Theta, Phi, SensorAngle (Delta for deviated setup)) 
		            // sensor location is related to first tool in first obs well
					line = reader.readLine(); 
					st = new StringTokenizer(line, " \t");
					x = Double.parseDouble(st.nextToken().trim());
					y = Double.parseDouble(st.nextToken().trim());
					z = Double.parseDouble(st.nextToken().trim());
					double theta = Double.parseDouble(st.nextToken().trim());
					double phi = Double.parseDouble(st.nextToken().trim());
					double angle = Double.parseDouble(st.nextToken().trim());
					
					Sensor sensor = new Sensor(j, "Sensor"+j, x, y, z, theta, phi, angle);
					_obsWells[i].setSensor(j, sensor);
				}
				//System.out.println(_obsWells[i].toString());
			}
			
			line = reader.readLine(); //number of events
			st = new StringTokenizer(line, " \t");
			int nEvents = Integer.parseInt(st.nextToken().trim());
			_msEvents = new MSEvent[nEvents];
			int nPicks = 0;
			for(int i=0; i<nObsWells; i++) {
				nPicks += _obsWells[i].getNumOfSensors();
			}
			for(int i=0; i<nEvents; i++) {
				line = reader.readLine(); //datafilename, x,y,z,sample_rate,hr, min, sec
				st = new StringTokenizer(line, " \t");
				String fileName = st.nextToken().trim();
				x = Double.parseDouble(st.nextToken().trim());
				y = Double.parseDouble(st.nextToken().trim());
				z = Double.parseDouble(st.nextToken().trim());
				
				double sampleIncrement = Double.parseDouble(st.nextToken().trim());

				int hour = Integer.parseInt(st.nextToken().trim());
				int min = Integer.parseInt(st.nextToken().trim());
				int sec = Integer.parseInt(st.nextToken().trim());
				
				_msEvents[i] = new MSEvent(i, fileName, x, y, z, hour, min, sec, 
						sampleIncrement, nPicks, _obsWells.length, 2);
				for(int j=0; j<nPicks; j++) {
					//read in sensors (East, North, Depth, Theta, Phi, SensorAngle (Delta for deviated setup)) 
		            // sensor location is related to first tool in first obs well
					line = reader.readLine(); 
					st = new StringTokenizer(line, " \t");
					_msEvents[i]._sensorNo[j] = Integer.parseInt(st.nextToken().trim());
					_msEvents[i]._pPick[j] = Double.parseDouble(st.nextToken().trim());
					_msEvents[i]._svPick[j] = Double.parseDouble(st.nextToken().trim());
					_msEvents[i]._angle[j] = Double.parseDouble(st.nextToken().trim());
					_msEvents[i]._shPick[j] = _msEvents[i]._svPick[j];
				}
				_msEvents[i].setObsWells(_obsWells);
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
			bufferedWriter.write(toString());
			bufferedWriter.close();
		} catch (IOException ioexception) {
			JOptionPane.showMessageDialog(null, "\nFile Format is not right!", 
					"Error", JOptionPane.ERROR_MESSAGE);
		}
	}
	public String toString() {
		String b = "Project Information";
		String a = _project.toString();
		b = b.concat("\n"+a+"\n");
		
		a = "Fracturing Information";
		b = b.concat(a+"\n");
		b = b.concat(_fracWell.toString()+"\n\n");
		
		a = "Observation Well Information";
		b = b.concat(a+"\n");
		a = _obsWells.length+" // Number of observation wells";
		b = b.concat(a+"\n");
		for(int i=0; i<_obsWells.length; i++) {
			b = b.concat(_obsWells[i].toString()+"\n");
		}
		
		a = _msEvents.length+" // Number of microseismic events";
		b = b.concat(a+"\n");
		for(int i=0; i<_msEvents.length; i++) {
			b = b.concat(_msEvents[i].toString()+"\n");
		}
		
		return b;
	}
}
