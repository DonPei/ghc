
package com.geohammer.core;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.StringTokenizer;

import javax.swing.JOptionPane;

import org.apache.commons.io.FilenameUtils;
import org.ucdm.seg2.SEG2;
import org.ucdm.core.acquisition.VCPair;

public class SeisPTSrcMech {
	Project _project 		= null;
	FracWell _fracWell 		= null;
	ObsWell [] _obsWells 	= null;
	MSEvent [] _msEvents 	= null;

	String 		_fullPath 					= null;
	String 		_baseName 					= null;

	public SeisPTSrcMech(String selectedFileName) {
		this(true, selectedFileName);
	}
	//perfData==true 
	public SeisPTSrcMech(boolean perfData, String selectedFileName) {
		this(perfData, selectedFileName, null);
	}

	public SeisPTSrcMech(boolean perfData, String selectedFileName, String dataPath) {
		_fullPath = FilenameUtils.getFullPathNoEndSeparator(selectedFileName);
		//int index = fullPath.lastIndexOf("\\");
		//String dataPath = fullPath.substring(0, index+1); 
		_baseName = FilenameUtils.getBaseName(selectedFileName);
		read(perfData, selectedFileName);
		if(dataPath!=null) readSeismicTrace(dataPath);
	}
	public SeisPTSrcMech(double sampleInterval, VCPair vcPW) {
		String siteName = "UCDM";
		String timeZone = "C";
		Calendar now = Calendar.getInstance();
		int month = now.get(Calendar.MONTH)+1;
		int day = now.get(Calendar.DAY_OF_MONTH);
		int year = now.get(Calendar.YEAR);

		//4th line for coordinates (East North UTM_Zone KBElev)
		double utmE = 0.0;
		double utmN = 0.0;
		String utmZone = "14N";
		double kbElev = 0;

		//5th line for Project setup
		String dimension = "2D";
		int [] obsWellIndex = vcPW.calReceiverIndexOfEachObsWell();
		int nObsWells = obsWellIndex.length;
		if(nObsWells>1) dimension = "3D";

		int iUnit = vcPW.getUnit();
		String unit = "m";
		if(iUnit==1) unit = "m";
		else if(iUnit==2) unit = "ft";
		else unit = "km";
		
		_project = new Project(siteName, year, month, day, timeZone, unit, 
				utmE, utmN, utmZone, kbElev, dimension);

		//6th line empty
		//7th line 'Fracturing Information'
		//8th line (FracSurfX, FracSurfY, FracSurfKBElev) relative to reference points
		double x = 0;
		double y = 0;
		double z = 0;

		//9th line number of perforation
		int nPerfs = vcPW.getNumOfEvents();
		_fracWell = new FracWell(nPerfs, 0, "Frac_Well", x, y, z, kbElev);

		//10th line "Easting Northing Depth"
		for(int i=0; i<nPerfs; i++) {
			_fracWell._px[i] = vcPW.getEN(i);
			_fracWell._py[i] = vcPW.getEE(i);
			_fracWell._pz[i] = vcPW.getED(i);
			_fracWell._stageNo[i] = 1;
			_fracWell._fileName[i] = "frac"+i;
		}

		//empty line separates Perf and Observation Well Information
		//line for 'Observation Well Information'
		//line for number of observation well
		
		int [] index = new int[obsWellIndex.length+1];
		for(int i=0; i<obsWellIndex.length; i++) {
			index[i] = obsWellIndex[i];
		}
		index[obsWellIndex.length] = vcPW.getFlag(0);
		
		_obsWells = new ObsWell[nObsWells];
		for(int i=0; i<nObsWells; i++) {
			//observation well location (E, N, KB) related to reference point
			x = vcPW.getRN(index[i]);
			y = vcPW.getRE(index[i]);
			z = vcPW.getRD(index[i]);

			//num of tools in current obs well
			int nSensor = index[i+1]-index[i];

			//Tool channel order
			String chanOrder = "zyx";
			_obsWells[i] = new ObsWell(nSensor, i, "Obs_Well"+i, x, y, z, kbElev, chanOrder, dimension);

			double theta = 0.0; // = 0 for vertical well
			double phi = 0.0;  // = 0 for vertical well
			double delta = 0.0; // clockwise from North

			for(int j=0; j<nSensor; j++) {
				//read in sensors (East, North, Depth, Theta, Phi, SensorAngle (Delta for deviated setup)) 
				// sensor location is related to first tool in first obs well
				if(dimension.contains("2D")) {
					z = vcPW.getRD(index[i]+j);
					delta = 0;
				} else {
					x = vcPW.getRN(index[i]+j);
					y = vcPW.getRE(index[i]+j);
					z = vcPW.getRD(index[i]+j);
					theta = 0;
					phi = 0;
					delta = 0;
				}

				Sensor sensor = new Sensor(j, "Sensor"+j, x, y, z, theta, phi, delta);
				_obsWells[i].setSensor(j, sensor);
			}
			//System.out.println(_obsWells[i].toString());
		}

		//number of events
		int nEvents = vcPW.getNumOfEvents();
		_msEvents = new MSEvent[nEvents];
		int nPicks = 0;
		for(int i=0; i<nObsWells; i++) {
			nPicks += _obsWells[i].getNumOfSensors();
		}

		_fracWell.trim();

		for(int i=0, k=0; i<nEvents; i++) {
			//datafilename, x,y,z,sample_rate,hr, min, sec
			x = vcPW.getEN(i);
			y = vcPW.getEE(i);
			z = vcPW.getED(i);
			double sampleIncrement = sampleInterval;
			int hour = now.get(Calendar.HOUR_OF_DAY);
			int min = now.get(Calendar.MINUTE)+i;
			int sec = now.get(Calendar.SECOND);
			//System.out.println("nPicks="+nPicks);

			String eventName = String.format("%s%04d", "event", (i+1));
			_msEvents[i] = new MSEvent(i, eventName, x, y, z, hour, min, sec, 
					sampleIncrement, nPicks, _obsWells.length, iUnit);
			for(int j=0; j<nPicks; j++, k++) {
				//read in sensors (East, North, Depth, Theta, Phi, SensorAngle (Delta for deviated setup)) 
				// sensor location is related to first tool in first obs well
				_msEvents[i]._sensorNo[j] = j+1;
				_msEvents[i]._pPick[j] = (int)(vcPW.getObsPT(k)/sampleInterval);
				_msEvents[i]._shPick[j] = (int)(vcPW.getObsST(k)/sampleInterval);
				_msEvents[i]._angle[j] = 0;
				_msEvents[i]._svPick[j] = (int)(vcPW.getObsSV(k)/sampleInterval);
			}
			_msEvents[i].setObsWells(_obsWells);
		}
	}	

	public void append(SeisPTSrcMech other) {
		int n1 = getNumOfMSEvents();
		int nEvents = n1 + other.getNumOfMSEvents();
		MSEvent [] msEvents 	= new MSEvent[nEvents];
		for(int i=0; i<n1; i++) msEvents[i] = getMSEvents(i);
		for(int i=n1; i<nEvents; i++) msEvents[i] = other.getMSEvents(i-n1);
		_msEvents = msEvents;
		
		_fracWell.appendPerfs(other.getFracWell());
	}
	public int getNumOfMSEvents() 				{ return _msEvents.length; }
	public MSEvent [] getMSEvents() 			{ return _msEvents; }
	public MSEvent getMSEvents(int index) 		{ return _msEvents[index]; }
	public int getNumOfObsWells() 				{ return _obsWells.length; }
	public ObsWell [] getObsWells() 			{ return _obsWells; }
	public ObsWell getObsWells(int index) 		{ return _obsWells[index]; }
	public FracWell getFracWell() 				{ return _fracWell; }
	public Project getProject() 				{ return _project; }

	public VCPair toVCPair(int iType) {
		VCPair [] vcPWs = new VCPair[_msEvents.length];
		for(int i=0; i<_msEvents.length; i++) {
			vcPWs[i] = _msEvents[i].genVCPair(-1, iType);
		}
		int fp = 0;
		for(int i=0; i<_msEvents.length; i++) {
			fp += _msEvents[i].getNumOfPicks();
		}

		VCPair vcPW = new VCPair(getNumOfMSEvents(), fp, _msEvents[0].getUnit());

		for(int i=0, k=0; i<_msEvents.length; i++) { 
			vcPW.setData(i, vcPW.getFlag(), vcPWs[i].getFlag(0));
			vcPW.setData(i, vcPW.getEN(), vcPWs[i].getEN(0));
			vcPW.setData(i, vcPW.getEE(), vcPWs[i].getEE(0));
			vcPW.setData(i, vcPW.getED(), vcPWs[i].getED(0));
			vcPW.setData(i, vcPW.getOrgT(), vcPWs[i].getOrgT(0));
			vcPW.setData(i, vcPW.getOrgW(), vcPWs[i].getOrgW(0));

			for(int j=0; j<_msEvents[i].getNumOfPicks(); j++, k++) {
				vcPW.setData(k, vcPW.getRN(), vcPWs[i].getRN(j));
				vcPW.setData(k, vcPW.getRE(), vcPWs[i].getRE(j));
				vcPW.setData(k, vcPW.getRD(), vcPWs[i].getRD(j));

				vcPW.setData(k, vcPW.getObsPT(), vcPWs[i].getObsPT(j));
				vcPW.setData(k, vcPW.getObsST(), vcPWs[i].getObsST(j));
				vcPW.setData(k, vcPW.getObsSV(), vcPWs[i].getObsSV(j));
			}
		}

		return vcPW;
	}

	public void readSeismicTrace(String dataPath) {
		if(dataPath==null) return;
		//for(int i=0; i<1; i++) {
		for(int i=0; i<getNumOfMSEvents(); i++) {
			MSEvent msEvent = _msEvents[i];
			//String fileName = "C:\\PINN\\MTInv\\Example\\"+msEvent.getFileName()+".dat";
			SEG2 seg2 = new SEG2(msEvent.getPpicks(), msEvent.getSVpicks(), msEvent.getSHpicks(), msEvent.getAngles(), 
					dataPath+File.separator+msEvent.getLabel()+".dat");

			//System.out.println(dataPath+File.separator+msEvent.getFileName()+".dat"+" "+getNumOfMSEvents());
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
				//System.out.println(stWell.toString());

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
			msEvent.pickAssignToTrace();
		}
	}

	public void read(boolean perfData, String selectedFileName) {
		try{
			BufferedReader reader = new BufferedReader(new FileReader(selectedFileName));
			String line = reader.readLine(); // 1st line line 'Project Information'
			line = reader.readLine(); //2nd line 'SiteName, Date, TimeZone'
			String[] stringArray = null;

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
			double E = Double.parseDouble(st.nextToken().trim());
			double N = Double.parseDouble(st.nextToken().trim());
			double D = Double.parseDouble(st.nextToken().trim());

			line = reader.readLine(); //9th line number of performation
			st = new StringTokenizer(line, " \t");
			int nPerfs = Integer.parseInt(st.nextToken().trim());
			_fracWell = new FracWell(nPerfs, 0, "Frac_Well", N, E, D, kbElev);

			line = reader.readLine(); //10th line "Easting Northing Depth"
			for(int i=0; i<nPerfs; i++) {
				line = reader.readLine(); 
				stringArray = line.trim().split("\\s+");
				//System.out.println(Arrays.toString(stringArray));

				_fracWell._py[i] = Double.parseDouble(stringArray[0].trim());
				_fracWell._px[i] = Double.parseDouble(stringArray[1].trim());
				_fracWell._pz[i] = Double.parseDouble(stringArray[2].trim());
				if(stringArray.length>3) _fracWell._stageNo[i] = Integer.parseInt(stringArray[3].trim());
				else _fracWell._stageNo[i] = 1;
				
				if(stringArray.length<5) {
					_fracWell._fileName[i] = null;
				} else if(stringArray.length==5) {
					_fracWell._fileName[i] = stringArray[4].trim();
				} else if(stringArray.length>5) {
					int fromIndex = stringArray[0].length();
					for(int j=1; j<4; j++) {
						int index = line.indexOf(stringArray[j], fromIndex);
						fromIndex = index+stringArray[j].length();
					}
					_fracWell._fileName[i] = line.substring(fromIndex, line.length());
				}
			}

			line = reader.readLine(); //empty line separates Perf and Observation Well Information
			line = reader.readLine(); //line for 'Observation Well Information'
			line = reader.readLine(); //line for number of observation well
			//System.out.println(line);
			st = new StringTokenizer(line, " \t");
			int nObsWells = Integer.parseInt(st.nextToken().trim());

			_obsWells = new ObsWell[nObsWells];
			for(int i=0; i<nObsWells; i++) {
				line = reader.readLine(); //observation well location (E, N, KB) related to reference point
				//System.out.println(line);
				st = new StringTokenizer(line, " ,\t");
				E = Double.parseDouble(st.nextToken().trim());
				N = Double.parseDouble(st.nextToken().trim());
				D = Double.parseDouble(st.nextToken().trim());

				line = reader.readLine(); //num of tools in current obs well
				st = new StringTokenizer(line, " \t");
				int nSensor = Integer.parseInt(st.nextToken().trim());

				line = reader.readLine(); //Tool channel order
				st = new StringTokenizer(line, " \t");
				String chanOrder = st.nextToken().trim();
				_obsWells[i] = new ObsWell(nSensor, i, "Obs_Well"+i, N, E, D, kbElev, chanOrder, dimension);

				double theta = 0.0; // = 0 for vertical well
				double phi = 0.0;  // = 0 for vertical well
				double delta = 0.0; // clockwise from North

				for(int j=0; j<nSensor; j++) {
					//read in sensors (East, North, Depth, Theta, Phi, SensorAngle (Delta for deviated setup)) 
					// sensor location is related to first tool in first obs well
					line = reader.readLine(); 
					st = new StringTokenizer(line, " \t");
					if(dimension.contains("2D")) {
						if(i>0) {
							String message = "2D SeisPT project allows only one monitoring well!";
							String title = "Error";
							JOptionPane.showMessageDialog(null, message, title, JOptionPane.ERROR_MESSAGE);
							return;
						} else {
							E = 0; 
							N = 0;
							D = Double.parseDouble(st.nextToken().trim());
							delta = Double.parseDouble(st.nextToken().trim());
						}
					} else {
						E = Double.parseDouble(st.nextToken().trim());
						N = Double.parseDouble(st.nextToken().trim());
						D = Double.parseDouble(st.nextToken().trim());
						theta = Double.parseDouble(st.nextToken().trim());
						phi = Double.parseDouble(st.nextToken().trim());
						delta = Double.parseDouble(st.nextToken().trim());
					}

					Sensor sensor = new Sensor(j, "Sensor"+j, N, E, D, theta, phi, delta);
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
			int iUnit = 2;
			if(_project.getUnit().equalsIgnoreCase("m")) iUnit = 1;
			else if(_project.getUnit().equalsIgnoreCase("km")) iUnit = 3;
			else iUnit = 2;
			
			//_fracWell.trim();
			//System.out.println("nEvents="+nEvents+" nPerfs="+_fracWell.getNumOfPerfs());
			//System.out.println(_fracWell.toString());
			if(perfData) { // this is a perf file
				if(nEvents!=_fracWell.getNumOfPerfs()) {
					String msg = "\nThe perf SrcMech file ("+selectedFileName+") \n has "+nEvents+
							" events, which is not equal to number of perfs "+ _fracWell.getNumOfPerfs()+".";
					JOptionPane.showMessageDialog(null, msg, "Error", JOptionPane.ERROR_MESSAGE);
					return;
				} 
			}
			//System.out.println("nEvents="+nEvents);
			stringArray = null;
			for(int i=0; i<nEvents; i++) {
				if(stringArray==null) line = reader.readLine(); //datafilename, E,N,D,sample_rate,hr, min, sec
				//System.out.println("nEvents="+line);

				if(line==null) {
					JOptionPane.showMessageDialog(null, "\nThe file ("+selectedFileName+
							") \n has number of events "+i+" less than "+nEvents, 
							"Error", JOptionPane.ERROR_MESSAGE);
				}

				
				stringArray = line.trim().split("\\s+");
				if(stringArray.length<8) { 
					JOptionPane.showMessageDialog(null, "\nThe file ("+selectedFileName+
						") has a bad line "+line, 
						"Error", JOptionPane.ERROR_MESSAGE);
				}
				
				//deal with white space in file name
				String fileName = null;
				int k = 1;
				if(stringArray.length==8) { fileName = stringArray[0].trim(); }
				else if(stringArray.length>8) {
					k = stringArray.length-8+1;
					int fromIndex = -1;
					for(int j=0; j<k; j++) {
						int index = line.indexOf(stringArray[j], ++fromIndex);
						fromIndex = index;
					}
					fileName = line.substring(0, fromIndex+stringArray[k-1].length()+1);
				}
				//System.out.println("k="+k+" "+Arrays.toString(stringArray));
				E = Double.parseDouble(stringArray[k++].trim());
				N = Double.parseDouble(stringArray[k++].trim());
				D = Double.parseDouble(stringArray[k++].trim());
				double sampleIncrement = Double.parseDouble(stringArray[k++].trim());
				int hour = Integer.parseInt(stringArray[k++].trim());
				int min = Integer.parseInt(stringArray[k++].trim());				
				int sec = Integer.parseInt(stringArray[k++].trim());
				
				//System.out.println(fileName);

				_msEvents[i] = new MSEvent(i, fileName, N, E, D, hour, min, sec, 
						sampleIncrement, nPicks, _obsWells.length, iUnit);
				for(int j=0; j<nPicks; j++) {
					//read in sensors (East, North, Depth, Theta, Phi, SensorAngle (Delta for deviated setup)) 
					// sensor location is related to first tool in first obs well
					line = reader.readLine(); 
					st = new StringTokenizer(line, " \t");
					_msEvents[i]._sensorNo[j] = Integer.parseInt(st.nextToken().trim());
					_msEvents[i]._pPick[j] = Double.parseDouble(st.nextToken().trim());
					_msEvents[i]._shPick[j] = Double.parseDouble(st.nextToken().trim());
					_msEvents[i]._angle[j] = Double.parseDouble(st.nextToken().trim());
					_msEvents[i]._svPick[j] = 0.0;
				}
				line = reader.readLine(); 
				if(line!=null) {
					stringArray = line.trim().split("\\s+");
					//System.out.println("k1="+stringArray.length+" "+Arrays.toString(stringArray));
					if(stringArray.length==4) {
						_msEvents[i]._perfP = Double.parseDouble(stringArray[1].trim());
						_msEvents[i]._perfSh = Double.parseDouble(stringArray[2].trim());
						_msEvents[i]._perfA = Double.parseDouble(stringArray[3].trim());
						_msEvents[i]._perfSv = 0.0;
						stringArray = null;
					} 
				}
				_msEvents[i].setObsWells(_obsWells);
			}
			if(perfData) {
				for(int i=0; i<nEvents; i++) {
					_msEvents[i].setX(_fracWell._px[i]);
					_msEvents[i].setY(_fracWell._py[i]);
					_msEvents[i].setZ(_fracWell._pz[i]);
				}
			}

			reader.close();

		} catch (IOException ioexception) {
			String msg = "\nCan not open the file ("+selectedFileName+").";
			JOptionPane.showMessageDialog(null, msg, "Error", JOptionPane.ERROR_MESSAGE);
		}	
	}
	//public void write(String selectedFileName) { write(false, selectedFileName); }
	public void write(boolean shrink, String selectedFileName) {
		try{
			BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(selectedFileName, false));
			bufferedWriter.write(toString(shrink));
			bufferedWriter.close();
		} catch (IOException ioexception) {
			JOptionPane.showMessageDialog(null, "\nFile Format is not right!", 
					"Error", JOptionPane.ERROR_MESSAGE);
		}
	}
	public String toString(boolean shrink) {
		String b = "Project Information";
		String a = _project.toString();
		b = b.concat("\n"+a+"\n");

		a = "Fracturing Information";
		b = b.concat(a+"\n");
		b = b.concat(_fracWell.toString()+"\n");

		a = "Observation Well Information";
		b = b.concat(a+"\n");
		a = _obsWells.length+" // Number of observation wells";
		b = b.concat(a+"\n");
		for(int i=0; i<_obsWells.length; i++) {
			b = b.concat(_obsWells[i].toString()+"\n");
		}

		if(shrink) {
			a = getValidNumOfEvents()+" // Number of microseismic events";
			b = b.concat(a+"\n");
			for(int i=0; i<_msEvents.length; i++) {
				if(_msEvents[i].getEnableMti()) b = b.concat(_msEvents[i].toString(1)+"\n");
			}
		} else {
			a = _msEvents.length+" // Number of microseismic events";
			b = b.concat(a+"\n");
			for(int i=0; i<_msEvents.length; i++) {
				b = b.concat(_msEvents[i].toString(1)+"\n");
			}
		}

		return b;
	}
	
	public int getValidNumOfEvents() {
		int k = 0;
		for(int i=0; i<_msEvents.length; i++) {
			if(_msEvents[i].getEnableMti()) k++;
		}
		return k;
	}
	public String toEventString() {
		String b = "";
		String a = _msEvents.length+" // Number of microseismic events";
		b = b.concat(a+"\n");
		for(int i=0; i<_msEvents.length; i++) {
			b = b.concat(_msEvents[i].toString(1)+"\n");
		}

		return b;
	}
}
