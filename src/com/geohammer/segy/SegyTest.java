package com.geohammer.segy;

import java.io.File;
import java.util.GregorianCalendar;

import javax.swing.SwingUtilities;

import org.apache.commons.io.FilenameUtils;

import com.geohammer.core.SeisPTUtil;
import com.geohammer.core.SeismicTraceComponent;
import com.geohammer.core.SeismicTraceSensor;

public class SegyTest implements Runnable {
	public SegyTest() {
		
	}
	public void run() {	
		//walkThroughDir();
		//test1();
		System.out.println("Successfully Done!");
	}
	public static void main(String[] args) {
		System.out.println("Executing Test()");
		SwingUtilities.invokeLater( new SegyTest() );
	}
	public void walkThroughDir() {
		String srcPath = "Z:\\HOU\\Downloads\\Don\\fd3dSynthetics\\explosion_ISO";
		String srcFileExtension = "dat";
		//String targetPath = "Z:\\HOU\\Downloads\\Don\\fd3dSynthetics\\dc_ISO";
		String targetPath = "C:\\PINN_DATA\\fd3d\\homoBoth\\explosion_ISO\\segy";
		String targetFileExtension = "sgy";
		SeisPTUtil.makeDir(targetPath);		
		walkThroughDir(srcPath, srcFileExtension, targetPath, targetFileExtension);
	}
	
	public void walkThroughDir(String srcPath, String srcFileExtension, String targetPath, String targetFileExtension) {
		String 	name = null;
		File[] listOfFiles = new File(srcPath).listFiles(); 
		
		String 	srcFileName = null;
		String 	targetFileName = null;
		
		for (int i=0; i<listOfFiles.length; i++) {
			name = listOfFiles[i].getName();
			srcFileName = srcPath+File.separator+name;
			if (listOfFiles[i].isFile()) {
				if(FilenameUtils.isExtension(name, srcFileExtension)) {
					System.out.println(srcFileName);
					targetFileName = targetPath+File.separator+FilenameUtils.removeExtension(name)+"."+targetFileExtension;
					System.out.println(targetFileName);
					//convertToSegy(srcFileName, targetFileName, null, null, null);
					
				} else {
				}
			} else {
			}
		}
	}
	
}
