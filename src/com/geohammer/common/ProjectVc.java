package com.geohammer.common;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;

import com.geohammer.core.planarmodel.Layer2D;
import com.geohammer.core.planarmodel.Layer3D;
import com.geohammer.core.planarmodel.SingleFlatLayer;
import com.geohammer.excel.ExcelSheet;
import com.geohammer.excel.SheetData;
import com.geohammer.excel.SheetHeader;
import com.geohammer.excel.TerraVistaExcel;
import com.geohammer.common.WellTrack;
import com.geohammer.core.acquisition.VCPair;
import com.geohammer.core.planarmodel.DipLayer1D;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;


@Getter
@Setter
@NoArgsConstructor
//@AllArgsConstructor
@ToString
public class ProjectVc extends CommonProject {
	private int 		tTimeFileType	= 0; 	// 0 for 
	private String 		tTimeFileName 	= null;
	private int 		velFileType		= 0; 	// 0 for 
	private String 		velFileName 	= null;
	private int 		wellTrackFileType	= 0; 	// 0 for 
	private String 		wellTrackFileName 	= null;
	
	private DipLayer1D 	dipLayer1D 		= null;
	private VCPair 		vCPair 			= null;	
	private WellTrack  	wellTrack  		= null;	

	private double 		azimuth 		= -1;
	private double 		dip 			= 0;
	private double 		centerN 		= 0;
	private double 		centerE			= 0;
	private VCPair 		vCPair2D 		= null;	
	private VCPair 		vCPair3D 		= null;	
	private DipLayer1D 	dipLayer1D2D 	= null;
	private DipLayer1D 	dipLayer1D3D 	= null;
	
	private Layer2D 	layer2D 		= null;
	private Layer3D 	layer3D 		= null;
	
	public ProjectVc(String inputFileName) {
		super(inputFileName);
		if(inputFileName!=null) {
			read(inputFileName);
		}
	}	
	
	public ProjectVc(String projectFileName, int iId, int iUnit, int velFileType, String velFileName, 
			int tTimeFileType, String tTimeFileName, int wellTrackFileType, String wellTrackFileName) {
		super(projectFileName, iId, iUnit);
		this.velFileType 	= velFileType;
		this.velFileName 	= velFileName;
		this.tTimeFileType 	= tTimeFileType;
		this.tTimeFileName 	= tTimeFileName;
		this.wellTrackFileType 	= wellTrackFileType;
		this.wellTrackFileName 	= wellTrackFileName;
		
	}
	
	public boolean is2D() {
		return getIId()>=20 && getIId()<30;
	}
	public void readVelocityFile() {
		if(velFileType==0) {
			dipLayer1D = new DipLayer1D(velFileName);
		} else if(velFileType==1){
			TerraVistaExcel terraVistaExcel = new TerraVistaExcel(1, velFileName);
			SheetData sheetData = terraVistaExcel.getExcelSheets().get(0).getSheetData();
			String [] layerNames = sheetData.getColumnS(0);
			double [] top = sheetData.getColumn(0);
			double [] vp = sheetData.getColumn(1);
			double [] vs = sheetData.getColumn(2);
			double [] den = sheetData.getColumn(3);
			double [] delta = sheetData.getColumn(4);
			double [] epsilon = sheetData.getColumn(5);
			double [] gamma = sheetData.getColumn(6);
			double [] qp = sheetData.getColumn(7);
			double [] qs = sheetData.getColumn(8);
			
			SheetHeader sheetHeader = terraVistaExcel.getExcelSheets().get(0).getSheetHeader();
			String a = sheetHeader.getValue("Strike (degree)*");
			double azimuth = Double.parseDouble(a);
			a = sheetHeader.getValue("Dip (degree)*");
			double dip = Double.parseDouble(a);
			a = sheetHeader.getValue("Min Northing*");
			double x0 = Double.parseDouble(a);
			a = sheetHeader.getValue("Number of Step Northing*");
			int nx = (int)Double.parseDouble(a);
			a = sheetHeader.getValue("Step Northing*");
			double dx = Double.parseDouble(a);
			a = sheetHeader.getValue("Min Easting*");
			double y0 = Double.parseDouble(a);
			a = sheetHeader.getValue("Number of Step Easting*");
			int ny = (int)Double.parseDouble(a);
			a = sheetHeader.getValue("Step Easting*");
			double dy = Double.parseDouble(a);
			a = sheetHeader.getValue("Number of Step TVD");
			int nz = (int)Double.parseDouble(a);
			a = sheetHeader.getValue("Length Unit*");
			double z0 = top[0];
			double z1 = top[top.length-1];
			double dz = (z1-z0)/(nz-1);
			
			double pivotNorthing = x0;
			double pivotEasting = y0;
			a = sheetHeader.getValue("Pivot Northing");
			if(a!=null) pivotNorthing = (int)Double.parseDouble(a);
			a = sheetHeader.getValue("Pivot Easting");
			if(a!=null) pivotEasting = (int)Double.parseDouble(a);
			
			int distUnit = 0;
			if(a.equalsIgnoreCase("m")) distUnit = 1;
			else if(a.equalsIgnoreCase("km")) distUnit = 2;
			
			dipLayer1D = new DipLayer1D(layerNames, top, vp, vs, den, delta, epsilon, gamma, 
					qp, qs, null, null, azimuth, dip);
			dipLayer1D.setIUnit(distUnit);
			dipLayer1D.setAnchorN(pivotNorthing);
			dipLayer1D.setAnchorE(pivotEasting);
			dipLayer1D.setSize(nx, ny, nz, dx, dy, dz, x0, y0, z0);
			//System.out.println(dipLayer1D.toString());
		}
	}
	public void writeVelocityFile(DipLayer1D dipLayer1D, String selectedFileName) {
		if(velFileType==0) {
			dipLayer1D = new DipLayer1D(velFileName);
		} else if(velFileType==1){
			TerraVistaExcel terraVistaExcel = new TerraVistaExcel(1, velFileName);
			SheetData sheetData = terraVistaExcel.getExcelSheets().get(0).getSheetData();
			double [] top = sheetData.getColumn(0);
			double [] vp = sheetData.getColumn(1);
			double [] vs = sheetData.getColumn(2);
			double [] den = sheetData.getColumn(3);
			double [] delta = sheetData.getColumn(4);
			double [] epsilon = sheetData.getColumn(5);
			double [] gamma = sheetData.getColumn(6); 
			double [] qp = sheetData.getColumn(7);
			double [] qs = sheetData.getColumn(8);
			
			ArrayList<SingleFlatLayer> layerList = dipLayer1D.getLayer();
			for(int i=0; i<dipLayer1D.getLayer().size(); i++) {  
				layerList.get(i).setVp(vp[i]);
				layerList.get(i).setVs(vs[i]);
				layerList.get(i).setDen(den[i]);
				layerList.get(i).setDelta(delta[i]);
				layerList.get(i).setEpsilon(epsilon[i]);
				layerList.get(i).setGamma(gamma[i]);
			}
			
			terraVistaExcel.write(selectedFileName);
		}
	}

	public void readTTimeFile() {
		if(tTimeFileType==0) {
			vCPair = new VCPair(1, tTimeFileName);
		} else if(tTimeFileType==1) {
			TerraVistaExcel terraVistaExcel = new TerraVistaExcel(2, tTimeFileName);
			ArrayList<ExcelSheet> 	excelSheets 	= terraVistaExcel.getExcelSheets();
			SheetData sheetData = excelSheets.get(0).getSheetData();
			double [] sN = sheetData.getColumn(0);
			double [] sE = sheetData.getColumn(1);
			double [] sD = sheetData.getColumn(2);
			SheetHeader sheetHeader = excelSheets.get(0).getSheetHeader();
			String a = sheetHeader.getValue("Length Unit*");
			int distUnit = 0;
			if(a.equalsIgnoreCase("m")) distUnit = 1;
			else if(a.equalsIgnoreCase("km")) distUnit = 2;
			a = sheetHeader.getValue("Origin Northing*");
			double x0 = Double.parseDouble(a);
			a = sheetHeader.getValue("Origin Easting*");
			double y0 = Double.parseDouble(a);
			a = sheetHeader.getValue("Origin TVD*");
			double z0 = Double.parseDouble(a);
			
			sheetData = excelSheets.get(1).getSheetData();
			double [] rN = sheetData.getColumn(0);
			double [] rE = sheetData.getColumn(1);
			double [] rD = sheetData.getColumn(2);
			
			vCPair = new VCPair(sE, sN, sD, rE, rN, rD, distUnit);
			
			if(excelSheets.size()<=2) return;
			sheetHeader = excelSheets.get(2).getSheetHeader();
			a = sheetHeader.getValue("Time Unit*");
			int timeUnit = 2; //microsecond, millisecond, second, and minute
			if(a.equalsIgnoreCase("microsecond")) timeUnit = 0;
			else if(a.equalsIgnoreCase("millisecond")) timeUnit = 1;
			else if(a.equalsIgnoreCase("second")) timeUnit = 2;
			else  timeUnit = 3;

			double [] obsPT = vCPair.getObsPT();
			double [][] p = excelSheets.get(2).getSheetData().getData();
			for(int i=0, k=0; i<p.length; i++) {
				for(int j=0; j<p[i].length; j++) {
					obsPT[k++] = p[i][j];
				}
			}

			if(excelSheets.size()<=3) return;
			double [] obsST = vCPair.getObsST();
			p = excelSheets.get(3).getSheetData().getData();
			for(int i=0, k=0; i<p.length; i++) {
				for(int j=0; j<p[i].length; j++) {
					obsST[k++] = p[i][j];
				}
			}
		}
	}
	public void updateTravelTimeForAll() {
		for(int i=0; i<vCPair3D.getNEvents(); i++) {
			vCPair.orgT[i] = vCPair3D.orgT[i];
		}
		int fp = vCPair.getTotalNumOfReceivers();
		for(int i=0; i<fp; i++) {
			vCPair.calPT[i] = vCPair3D.calPT[i];
			vCPair.calST[i] = vCPair3D.calST[i];
			vCPair.calSV[i] = vCPair3D.calSV[i];
		}
	}
	public void calAzimuthDipCenterNE() {
		dipLayer1D.setIUnit(iUnit);
		vCPair.setIUnit(iUnit);
		azimuth = dipLayer1D.getAzimuth();
		dip = dipLayer1D.getDip();
		centerN = dipLayer1D.getAnchorN();
		centerE = dipLayer1D.getAnchorE();
		vCPair.shift(-centerN, -centerE);
		
		if(azimuth<0) {
			azimuth = vCPair.calAzimuthFromShots(centerN, centerE);
			//				double [] a = vCPair.calAzimuthFromShots();
			//				azimuth = a[0];
			//				centerN = -a[1];
			//				centerE = -a[2];
			//				dipLayer1D.setAnchorN(centerN);
			//				dipLayer1D.setAnchorE(centerE);
			dipLayer1D.setAzimuth(azimuth);
		} 
		//System.out.println("azimuth="+ azimuth+" dip="+ dip+" centerN="+ centerN+" centerE="+ centerE);
	}
	
	public VCPair calVCPair2D() {
		VCPair vCPair1 = vCPair.copy();
		if(azimuth>89.99 && azimuth<90.01) {
			double [] tmp = vCPair1.getEE();
			vCPair1.setEE(vCPair1.getEN());
			vCPair1.setEN(tmp);
			tmp = vCPair1.getRE();
			vCPair1.setRE(vCPair1.getRN());
			vCPair1.setRN(tmp);	
		} else if(azimuth>179.99 || azimuth<0.01) {

		} else {
			rotate(azimuth, vCPair1.getEE(), vCPair1.getEN());
			rotate(azimuth, vCPair1.getRE(), vCPair1.getRN());
		}
		return vCPair1;
	}
	public VCPair calVCPair3D() {
		VCPair vCPair1 = calVCPair2D();
		
		if(dip<0.01&&dip>-0.01) {
		} else if(dip>89.99||dip<-89.9) {
		} else {
			rotate(-dip, vCPair1.getEN(), vCPair1.getED());
			rotate(-dip, vCPair1.getRN(), vCPair1.getRD());		
		}
		return vCPair1;
	}

	public Layer2D calLayer2D() {
		Layer2D layer2D = dipLayer1D.toLayer2D();
		return layer2D;
	}
	public Layer3D calLayer3D() {
		Layer3D layer3D = dipLayer1D.toLayer3D();
		return layer3D;
	}
	
	public void rotate(double azimuth, double [] e, double [] n) {
		double ang = azimuth*Math.PI/180.0;
		double theSin = Math.sin(ang);
		double theCos = Math.cos(ang);
		double[][] M = new double[][] {{theCos, -theSin},{theSin, theCos}}; 
		double ax = 0.0;
		double ay = 0.0;
		for(int i=0, k=0; i<e.length; i++) {
			ax = M[0][0]*e[i]+M[0][1]*n[i]; //xprime
			ay = M[1][0]*e[i]+M[1][1]*n[i];
			e[i] = ax;
			n[i] = ay;
		}
	}
	
	
	public void writeTTimeFile(VCPair vCPair, String selectedFileName) {
		if(tTimeFileType==0) {
			vCPair = new VCPair(1, tTimeFileName);
		} else if(tTimeFileType==1) {
			TerraVistaExcel terraVistaExcel = new TerraVistaExcel(2, tTimeFileName);
			ArrayList<ExcelSheet> 	excelSheets 	= terraVistaExcel.getExcelSheets();
			
			ExcelSheet excelSheetPT = excelSheets.get(2).copy();
			excelSheetPT.setSpreadsheetName("P-Cal");
			excelSheets.add(excelSheetPT);

			double [] calPT = vCPair.getCalPT();
			double [][] p = excelSheetPT.getSheetData().getData();
			for(int i=0, k=0; i<p.length; i++) {
				for(int j=0; j<p[i].length; j++) {
					p[i][j]= calPT[k++];
				}
			}
			
			ExcelSheet excelSheetST = excelSheets.get(2).copy();
			excelSheetST.setSpreadsheetName("SH-Cal");
			excelSheets.add(excelSheetST);

			double [] calST = vCPair.getCalST();
			p = excelSheetST.getSheetData().getData();
			for(int i=0, k=0; i<p.length; i++) {
				for(int j=0; j<p[i].length; j++) {
					p[i][j]= calST[k++];
				}
			}
			
			ExcelSheet excelSheetSV = excelSheets.get(2).copy();
			excelSheetSV.setSpreadsheetName("SV-Cal");
			excelSheets.add(excelSheetSV);

			double [] calSV = vCPair.getCalSV();
			p = excelSheetSV.getSheetData().getData();
			for(int i=0, k=0; i<p.length; i++) {
				for(int j=0; j<p[i].length; j++) {
					p[i][j]= calSV[k++];
				}
			}
			
			terraVistaExcel.write(selectedFileName);
		}
	}
	public void readWellTrackFile() {
		if(wellTrackFileName==null) return;
		if(wellTrackFileType==0) {
			wellTrack = new WellTrack(wellTrackFileName, 1, 0, new int [] {5, 6, 3});
			wellTrack.setWellName("unknown");
			//System.out.println(wellTrack.toString(1));
		} else if(wellTrackFileType==1) {
			TerraVistaExcel terraVistaExcel = new TerraVistaExcel(0, wellTrackFileName);
			SheetHeader sheetHeader = terraVistaExcel.getExcelSheets().get(0).getSheetHeader();
			String a = sheetHeader.getValue("Length Unit*");
			int distUnit = 0;
			if(a.equalsIgnoreCase("m")) distUnit = 1;
			else if(a.equalsIgnoreCase("km")) distUnit = 2;
			a = sheetHeader.getValue("Origin Northing*");
			double x0 = Double.parseDouble(a);
			a = sheetHeader.getValue("Origin Easting*");
			double y0 = Double.parseDouble(a);
			a = sheetHeader.getValue("Kelly Bush*");
			double kb = Double.parseDouble(a);
			
			SheetData sheetData = terraVistaExcel.getExcelSheets().get(0).getSheetData();
			double [] N = sheetData.getColumn(4);
			double [] E = sheetData.getColumn(3);
			double [] D = sheetData.getColumn(2);
			
			wellTrack = new WellTrack(N, E, D);
			wellTrack.setWellName("unknown");
		}
	}
	public void readWellTrack(String selectedFileName) {
		wellTrack = new WellTrack(selectedFileName, 1, 0, new int [] {5, 6, 3});
		wellTrack.setWellName("unknown");
		//System.out.println(wellTrack.toString(1));
	}
	
	protected void readProject(BufferedReader reader) throws IOException {
		String line = null;
		int k = 0;
		while( (line = reader.readLine()) != null ) {
			line = line.trim();
			if(line.isEmpty()||line.startsWith("#"))  continue;
			
			if(k==0) 		{ tTimeFileType 		= Integer.parseInt(parseLine(line)); }
			else if(k==1) 	{ tTimeFileName 		= parseFileName(line); }
			else if(k==2) 	{ velFileType 			= Integer.parseInt(parseLine(line)); }
			else if(k==3) 	{ velFileName 			= parseFileName(line); }
			else if(k==4) 	{ wellTrackFileType 	= Integer.parseInt(parseLine(line)); }
			else if(k==5) 	{ wellTrackFileName 	= parseFileName(line); }
			
			k++;
		}
	}
	
	protected void writeProject(BufferedWriter writer) throws IOException {
		writeLine(writer, "#Travel Time File Type: "); 
		writeLine(writer, String.format("%d", tTimeFileType));
		writeLine(writer, "#Travel Time File Type: "); 				
		writeFileName(writer, tTimeFileName);
		
		writeLine(writer, "#Velocity File Type: ");
		writeLine(writer, String.format("%d", velFileType));
		writeLine(writer, "#Velocity File Name: "); 
		writeFileName(writer, velFileName);		
		
		if(wellTrack!=null) {
			writeLine(writer, "#WellTrack File Type: ");
			writeLine(writer, String.format("%d", wellTrackFileType));
			writeLine(writer, "#WellTrack File Name: "); 
			writeFileName(writer, wellTrackFileName);
		}
		
	}	

}
