package com.geohammer.excel;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;

import org.apache.commons.io.FilenameUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;


public class TerraVistaExcel {
	ArrayList<ExcelSheet> 	excelSheets 	= null;
	String 					fileName 		= null;
	int 					fileType 		= 0; //0-survey; 1-model; 2-picks
	
	int 					headerM 		= 3; 
	int 					notesM 			= 1;
	
	public TerraVistaExcel(int fileType, String selectedFileName) {
		fileName 	= selectedFileName;
		this.fileType 	= fileType;
		String extension = FilenameUtils.getExtension(selectedFileName);
		if(extension.equalsIgnoreCase("csv")) {
			//readVcPW(selectedFileName);
		} else if(extension.equalsIgnoreCase("xlsx")) { 
			readExcel(selectedFileName); 
		}
		else { }
		//printInfo(selectedFileName);
	}

	private void shiftCeter() {
		for(int i=0; i<excelSheets.size(); i++) {	
			String eastingOffset = excelSheets.get(i).getSheetHeader().getHeaderValue("Offset East - Local Tangent Plane");
			float eOffset = Float.parseFloat(eastingOffset.trim());
			String northingOffset = excelSheets.get(i).getSheetHeader().getHeaderValue("Offset North - Local Tangent Plane");
			float nOffset = Float.parseFloat(northingOffset.trim());
			excelSheets.get(i).getSheetData().shiftCenter(true, nOffset, eOffset, 0);
		}
	}
	
	private void compareMinMax(double [][] minMax, double [][] tmp) {
		int i = 0;
		for(int j=0; j<minMax[i].length; j++) {
			if(minMax[i][j]>tmp[i][j])  minMax[i][j] = tmp[i][j];
		}
		i = 1;
		for(int j=0; j<minMax[i].length; j++) {
			if(minMax[i][j]<tmp[i][j])  minMax[i][j] = tmp[i][j];
		}
	}
	public double [][] getMinMax() {
		double a = 1.0e12;
		double [][] minMax = new double[][]{ {a, a, a}, {-a, -a, -a} }; 
		double [][] tmp = new double[2][3];
		
		for(int i=0; i<excelSheets.size(); i++) {
			excelSheets.get(i).getSheetData().calMinMax();
			excelSheets.get(i).getSheetData().getMinMaxNED(tmp);
			compareMinMax(minMax, tmp);
		}
		
		return minMax;
	}
	
	public String getFileName() 					{ return fileName; }
	
	public ArrayList<ExcelSheet> getExcelSheets() 	{ return excelSheets; }
	
	public void shiftCenter(boolean local, float cN, float cE, float cD) {
		for(int i=0; i<excelSheets.size(); i++) {
			excelSheets.get(i).getSheetData().shiftCenter(local, cN, cE, cD);
		}
	}
	
	public void printInfo(String selectedFileName) {
		try {
			File excel = new File(selectedFileName);
			FileInputStream fis = new FileInputStream(excel);
			XSSFWorkbook workbook = new XSSFWorkbook(fis);
			Iterator<Sheet> bookItr = workbook.iterator();
			ArrayList<String> sheetName = new ArrayList<String>();
			int k = 0;
			while (bookItr.hasNext()) {
				XSSFSheet spreadsheet = (XSSFSheet)bookItr.next();
				sheetName.add(spreadsheet.getSheetName());
				Row row = spreadsheet.getRow(4);
				int nCol = row.getLastCellNum();
				System.out.println("spreadsheet name=" + sheetName.get(k)+" nCol="+nCol);
				k++;
			}

			workbook.close();
			fis.close();

		} catch (FileNotFoundException fe) {
			fe.printStackTrace();
		} catch (IOException ie) {
			ie.printStackTrace();
		}
	}
	
	
	private int getNumOfTools(String fracWell, SheetData wellAttachment) {
		String [][] dataS = wellAttachment.getDataS();
		int k = 0; 
		for(int i=0; i<dataS.length; i++) {
			if(dataS[i][0].contains(fracWell)) k++;
		}
		return k;
	}
	
	public void write(String selectedFileName) {
		XSSFWorkbook workbook = new XSSFWorkbook();
		Font boldFont = workbook.createFont();
		//boldFont.setColor(IndexedColors.BROWN.getIndex());
		//boldFont.setBoldweight(Font.BOLDWEIGHT_BOLD);
		boldFont.setBold(true);
		CellStyle boldStyle = workbook.createCellStyle();
		boldStyle.setFont(boldFont);

		for(int i=0; i<excelSheets.size(); i++) {
			XSSFSheet spreadsheet = workbook.createSheet(excelSheets.get(i).getSpreadsheetName());
			writeSheetHeader(boldStyle, spreadsheet, excelSheets.get(i).getSheetHeader());
			writeSheetNotes(boldStyle, spreadsheet, excelSheets.get(i).getSheetNotes(), false);
			writeSheetData(boldStyle, spreadsheet, excelSheets.get(i).getSheetData());
			for(int j=0; j<20; j++) { spreadsheet.autoSizeColumn(j); }
			writeSheetNotes(boldStyle, spreadsheet, excelSheets.get(i).getSheetNotes(), true);
		}
		
		//Write the workbook in file system
		FileOutputStream out;
		try {
			out = new FileOutputStream( new File(selectedFileName));

			workbook.write(out);
			out.close();
			workbook.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void writeSheetHeader(CellStyle style, XSSFSheet spreadsheet, SheetHeader sheetHeader) {
		int nCol = sheetHeader.getItem().length;
		int headerN = nCol/headerM;
		for(int i=0, k=0; i<headerM; i++) {
			Row row = spreadsheet.createRow(i);
			for(int j=0; j<headerN; j++) {
				Cell cell = row.createCell(2*j);
				cell.setCellValue(sheetHeader.getItem(k));
				cell.setCellStyle(style);
				cell = row.createCell(2*j+1);
				cell.setCellValue(sheetHeader.getValue(k));
				k++;
			}
		}
	}
	
	public void writeSheetNotes(CellStyle style, XSSFSheet spreadsheet, SheetNotes sheetNotes, boolean filled) {
		int startRow = headerM;
		int nCol = sheetNotes.getItem().length;
		for(int i=0; i<notesM; i++) {
			Row row = spreadsheet.createRow(startRow+i);
			for(int j=0; j<nCol; j++) {
				Cell cell = row.createCell(j);
				if(filled) cell.setCellValue(sheetNotes.getValue(j));
			}
		}
	}
	
	public void writeSheetData(CellStyle style, XSSFSheet spreadsheet, SheetData sheetData) {
		double [][] data = sheetData.getData();
		String [][] dataS = sheetData.getDataS();
		String [] columnName = sheetData.getColumnName();
		
		int startRow = headerM+notesM;
		Row row = spreadsheet.createRow(startRow);
		startRow++;
		row = spreadsheet.createRow(startRow);
		Cell cell = null;
		for(int j=0; j<columnName.length; j++) {
			cell = row.createCell(j);
			cell.setCellValue(columnName[j]);
			cell.setCellStyle(style);
		}
		startRow++;
		
		for(int i=0; i<data.length; i++) {
			int k = 0;
			row = spreadsheet.createRow(startRow+i);
			if(dataS!=null) {
				for(int j=0; j<dataS[i].length; j++) {
					cell = row.createCell(k);
					cell.setCellValue(dataS[i][j]);
					k++;
				}
			}
			for(int j=0; j<data[i].length; j++) {
				cell = row.createCell(k);
				cell.setCellValue(data[i][j]);
				k++;
			}
		}
	}

	public void readExcel(String selectedFileName) {		
		try {
			File excel = new File(selectedFileName);
			FileInputStream fis = new FileInputStream(excel);
			readExcel(fis);
		} catch (FileNotFoundException fe) {
			fe.printStackTrace();
		} catch (IOException ie) {
			ie.printStackTrace();
		}
	}
	
	public void readExcel(InputStream is) {
		try {
			XSSFWorkbook workbook = new XSSFWorkbook(is);
			Iterator<Sheet> bookItr = workbook.iterator();
			int sheetNameIndex = 0;
			while (bookItr.hasNext()) {
				XSSFSheet spreadsheet = (XSSFSheet)bookItr.next();
				Row row = spreadsheet.getRow(headerM+notesM+1);
				//int nCol = row.getLastCellNum();
				int nCol = calCellCount(row);
				//System.out.println("spreadsheet name=" + spreadsheet.getSheetName()+" nCol="+nCol);
				sheetNameIndex++;

				SheetHeader wellHeader = readSheetHeader(spreadsheet);
				SheetNotes wellNotes = readSheetNotes(spreadsheet);
				int nDataS = 0;
				if(fileType==1||fileType==2) nDataS = 1;
				SheetData deviationSurvey = readSheetData(spreadsheet, nDataS, nCol-nDataS);
				ExcelSheet well = new ExcelSheet(wellHeader, wellNotes, deviationSurvey);
				well.setSpreadsheetName(spreadsheet.getSheetName());
				deviationSurvey.setExcelSheet(well);
				if(excelSheets==null) excelSheets = new ArrayList<ExcelSheet>();
				excelSheets.add(well);
			}

			workbook.close();
			is.close();

		} catch (FileNotFoundException fe) {
			fe.printStackTrace();
		} catch (IOException ie) {
			ie.printStackTrace();
		}
	}

	public SheetHeader readSheetHeader(XSSFSheet spreadsheet) {
		Iterator<Row> itr = spreadsheet.iterator();
		Row row = itr.next();
		int headerN = calCellCount(row);
		headerN /= 2;
		
		SheetHeader wellHeader = new SheetHeader(headerM*headerN);
		itr = spreadsheet.iterator();

		for(int i=0, k=0; i<headerM; i++) {
			row = itr.next();
			for(int j=0; j<headerN; j++) {
				Cell cell = row.getCell(2*j);
				if(cell==null) wellHeader.setItem(k, "null");
				else wellHeader.setItem(k, (row.getCell(2*j).getStringCellValue()).trim());

				cell = row.getCell(2*j+1);
				if(cell==null) wellHeader.setValue(k, "0");
				else {
					switch (cell.getCellType()) {
					case STRING:
						wellHeader.setValue(k, cell.getStringCellValue());
						break;
					case NUMERIC:
						wellHeader.setValue(k, cell.getNumericCellValue()+"");
						break;
					default:
					}
				}
				k++;
			}
		} 

		return wellHeader;
	}
	public SheetNotes readSheetNotes(XSSFSheet spreadsheet) {
		Iterator<Row> itr = spreadsheet.iterator();
		Row row = null;
		int skipN = headerM;
		for(int i=0; i<skipN; i++) {
			row = itr.next();
		}
		row = itr.next();
		int nCol = calCellCount(row);
		
		SheetNotes wellNotes = new SheetNotes(notesM*nCol);
		itr = spreadsheet.iterator();
		for(int i=0; i<skipN; i++) {
			row = itr.next();
		}
		
		for(int i=0; i<notesM; i++) {
			row = itr.next();
			for(int j=0; j<nCol; j++) {
				Cell cell = row.getCell(j);
				wellNotes.setItem(j, "Note "+j);
				wellNotes.setValue(j, (cell.getStringCellValue()).trim());
			}
		}

		return wellNotes;
	}
	
	public SheetData readSheetData(XSSFSheet spreadsheet, int nDataSCol, int nDataCol) {
		int skipN = headerM+notesM+1;
		int rowIndex0 = skipN+1;
		//int rowIndex1 = spreadsheet.getPhysicalNumberOfRows();
		//int rowCount = spreadsheet.getLastRowNum()+1;
		int rowCount = calRowCount(spreadsheet);
		int nRows = rowCount-rowIndex0;
		
		//System.out.println("rowIndex0="+rowIndex0+" rowCount="+rowCount+" nRows="+nRows);

		int nCol = nDataCol+nDataSCol;
		String [] columnName = new String[nCol];
		double [][] data = new double[nRows][nDataCol];
		String [][] dataS = null;
		if(nDataSCol>0) dataS = new String[nRows][nDataSCol];

		Iterator<Row> itr = spreadsheet.iterator();
		Row row = null;
		for(int i=0; i<skipN; i++) {
			row = itr.next();
		}
		row = itr.next();
		
		for(int j=0; j<nCol; j++) {
			columnName[j] = row.getCell(j).getStringCellValue();
			//System.out.println(columnName[j]);
		}
			
		for(int i=0; i<nRows; i++) {
			row = itr.next();
			for(int j=0; j<nDataSCol; j++) {
				dataS[i][j] = getCellValue(row.getCell(j));
			}
			for(int j=0; j<nDataCol; j++) {
				data[i][j] = row.getCell(nDataSCol+j).getNumericCellValue();
			}
		}

		SheetData deviationSurvey = new SheetData(null, columnName,  data, dataS);

		return deviationSurvey;
	}
	
	private int calCellCount(Row row) {
		int cellCount = row.getLastCellNum();
		if(cellCount <=0 ) return cellCount;
		int a = cellCount;
		for(int i=cellCount-1; i>=0; i--) {
			boolean isEmpty = false;
			Cell cell = row.getCell(i);
			if(cell==null) { a--; isEmpty = true; }
			else {
				String b = getCellValue(cell);
				if(b==null || b.isEmpty()) {
					a--;
					isEmpty = true;
				}
			}
			if(!isEmpty) return a;
		}
		return a;
	}
	private int calRowCount(XSSFSheet spreadsheet) {
		int rowCount = spreadsheet.getLastRowNum()+1;
		if(rowCount <=0 ) return rowCount;
		int a = rowCount;
		for(int i=rowCount-1; i>=0; i--) {
			boolean isEmpty = false;
			Row row = spreadsheet.getRow(i);
			Cell cell = row.getCell(0);
			if(cell==null) { a--; isEmpty = true; }
			else {
				String b = getCellValue(cell);
				if(b==null || b.isEmpty()) {
					a--;
					isEmpty = true;
				}
			}
			if(!isEmpty) return a;
		}
		return a;
	}
	private String getCellValue( Cell cell )	{
		switch (cell.getCellType()){
		case STRING:
			return cell.getStringCellValue();
		case NUMERIC:
			return String.valueOf( ( int ) cell.getNumericCellValue() );
		case BOOLEAN:
			return String.valueOf( cell.getBooleanCellValue() );
		}
		return null;
	}
	
}
