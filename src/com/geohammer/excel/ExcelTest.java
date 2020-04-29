package com.geohammer.excel;

import javax.swing.SwingUtilities;

public class ExcelTest implements Runnable {

	public ExcelTest() {

	}
	public void run() {	
		testRead();
		System.out.println("Successfully Done!");
	}
	public static void main(String[] args) {
		System.out.println("Executing Test()");
		SwingUtilities.invokeLater( new ExcelTest() );
	}

	public void testRead() {
		String fileName = "C:\\GH_DATA\\MOL\\02 Velocity\\MOL Well Setup.xlsx";
		//String fileName = "C:\\GH_DATA\\MOL\\02 Velocity\\MOL Model.xlsx";
		//String fileName = "C:\\GH_DATA\\MOL\\02 Velocity\\MOL Picks.xlsx";
		TerraVistaExcel terraVistaExcel = new TerraVistaExcel(0, fileName);
		String selectedFileName = "C:\\GH_DATA\\MOL\\02 Velocity\\MOL Well Setup out.xlsx";
		terraVistaExcel.write(selectedFileName);
		
	}

}
