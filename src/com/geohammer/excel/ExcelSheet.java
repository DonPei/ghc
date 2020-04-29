package com.geohammer.excel;

import java.util.ArrayList;

import com.geohammer.core.PointAbstract;
import com.geohammer.dsp.LinearRegression;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ExcelSheet extends PointAbstract {
	
	private double 			kbElev 			= 0.0;
	private SheetHeader 	sheetHeader 	= null;
	private SheetNotes 		sheetNotes 		= null;
	private SheetData 		sheetData 		= null;
	private String 			spreadsheetName = null;
	
	public ExcelSheet(int id, String label, double x, double y, double z, double kbElev) {
		this(id, label, x, y, z, kbElev, null, null, null);
	}
	public ExcelSheet(SheetHeader sheetHeader, SheetNotes sheetNotes, SheetData sheetData) {
		this(0, null, 0, 0, 0, 0, sheetHeader, sheetNotes, sheetData);
	}
	public ExcelSheet(int id, String label, double x, double y, double z, double kbElev, 
			SheetHeader sheetHeader, SheetNotes sheetNotes, SheetData sheetData) {
		super(id, label, x, y, z);
		this.kbElev  		= kbElev;
		this.sheetHeader 	= sheetHeader;
		this.sheetNotes 	= sheetNotes;
		this.sheetData 		= sheetData;
	}
	
	public ExcelSheet copy() {
		ExcelSheet target = new ExcelSheet(getId(), getLabel(), getX(), getY(), getZ(), kbElev);
		target.sheetHeader = sheetHeader.copy();
		target.sheetNotes = sheetNotes.copy();
		target.sheetData = sheetData.copy();
		target.sheetData.setExcelSheet(target);
		target.spreadsheetName = spreadsheetName;
		return target;
	}
	
	public int [] calAssociatedStageIndex(String stageName) {
		String [][] dataS = sheetData.getDataS();
		ArrayList<Integer> selected = new ArrayList<Integer>();
		int iCol = 1;
		for(int i=0; i<dataS.length; i++) {
			if(stageName.equalsIgnoreCase(dataS[i][iCol])) {
				selected.add(i);
			}
		}
		if(selected.size()==0) return null;
		int [] index = new int[selected.size()];
		for(int i=0; i<selected.size(); i++) {
			index[i] = selected.get(i);
		}
		return index;
	}
	
	public float [] calPerfCenter(int [] index) {
		float sumN = 0;
		float sumE = 0;
		float sumD = 0;
		
		for(int i=0; i<index.length; i++) {
			sumN += sheetData.getNorthing(true, index[i]);
			sumE += sheetData.getEasting(true, index[i]);
			sumD += sheetData.getTVD(true, index[i]);
		}
		return new float[] {sumN/index.length, sumE/index.length, sumD/index.length};
	}
	
	public boolean isOriginWell() {
		String eastingOffset = sheetHeader.getHeaderValue("Offset East - Local Tangent Plane");
		float eOffset = Float.parseFloat(eastingOffset.trim());
		String northingOffset = sheetHeader.getHeaderValue("Offset North - Local Tangent Plane");
		float nOffset = Float.parseFloat(northingOffset.trim());
		
		return eOffset==0 && nOffset==0;
	}
	
	public LinearRegression calMapProjectionLine() {
		int nRow = sheetData.getNumOfRow();
		double [] e = new double[nRow];
		double [] n = new double[nRow];
		for(int j=0; j<nRow; j++) {
			e[j] = sheetData.getEasting(true, j);
			n[j] = sheetData.getNorthing(true, j);
		}
		
		// first pass
        double sumx = 0.0, sumy = 0.0, sumx2 = 0.0;
        for (int i = 0; i < nRow; i++)  sumx  += e[i]; 
        double xbar = sumx / nRow;

        double xxbar = 0.0;
        for (int i = 0; i < nRow; i++) xxbar += (e[i] - xbar) * (e[i] - xbar);
		if(xxbar==0.0) return null;
		else return new LinearRegression(e, n);
	}
	
	public double calMapProjectionLineAzimuth() {
		int nRow = sheetData.getNumOfRow();
		double [] e = new double[nRow];
		double [] n = new double[nRow];
		for(int j=0; j<nRow; j++) {
			e[j] = sheetData.getEasting(true, j);
			n[j] = sheetData.getNorthing(true, j);
		}
		
        double sumx = 0.0, sumy = 0.0, sumx2 = 0.0;
        for (int i = 0; i < nRow; i++)  sumx  += e[i]; 
        double xbar = sumx / nRow;

        double xxbar = 0.0;
        for (int i = 0; i < nRow; i++) xxbar += (e[i] - xbar) * (e[i] - xbar);
		if(xxbar==0.0) return 0;
		else {
			LinearRegression linearRegression = new LinearRegression(e, n);
			double slope = linearRegression.slope();
			double angle = Math.atan(slope)*180.0/Math.PI;
			double az = 90-angle;
			return az;
		}
	}
	
	public String toString() {
		String b = super.toString();
		String a = kbElev + " // kbElev";
		b = b.concat("\n"+a+"\n");
		
		return b;
	}
	
}
