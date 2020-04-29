package com.geohammer.zip;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.SwingUtilities;

public class MtiZipTest implements Runnable {

	public MtiZipTest() {

	}
	public void run() {	
		test3();
		System.out.println("Successfully Done!");
	}
	public static void main(String[] args) {
		System.out.println("Executing Test()");
		SwingUtilities.invokeLater( new MtiZipTest() );
	}
	
	public void test1() {
		String root = "C:\\PINN_DATA\\mti\\AnadarkoTrinity\\";
    	String inputFileName = root+"results.zip";
    	String outputFileName = root+"resultsOut.zip";
    	
    	MtiZip mtiZip = new MtiZip("mpz", "mpz", root+"stage8.mpz");
    	mtiZip.addTmpFolderAndRestoreFileName();
    	//MtiZip.getAllFilesInDirectory(new File(root));
	}
	public void test2() {
		//String timeStamp = new SimpleDateFormat("yyyyMMddhhmm").format(new Date());
		String timeStamp = new SimpleDateFormat("yyyyMMddhhmmssSSS").format(new Date());
		System.out.println(timeStamp);
	}
	
	public void test3() {
    	
    	String mtvFileName = "C:\\MTI\\P29_combined_self_combined_local_mtv.mtv";
		
		MtiZip mtiZip = new MtiZip("mtv", null, mtvFileName);
		String src = mtiZip.getTempPath();
		System.out.println("src="+src+" mtvFileName="+mtvFileName);
	}
}