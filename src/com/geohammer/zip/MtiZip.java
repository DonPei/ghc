package com.geohammer.zip;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.io.ZipOutputStream;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.util.Zip4jConstants;

public class MtiZip {
	private String 			_srcFileName 	= null;
	private String 			_saveAsFileName = null;
	private static String 	_extension 		= "mpz";
	private String 			_tmpPath 		= System.getProperty("java.io.tmpdir");

	public MtiZip(String prefix) {
		this(true, prefix);
	}
	public MtiZip(boolean cleanFolder, String prefix) {
		_tmpPath += prefix+File.separator;
		String folderToDelete = getTempPath();
		File file = new File(folderToDelete);
		if(file.exists()&&cleanFolder) {
			cleanDirectory(folderToDelete);
		} else {
			file.mkdir();
		}
	}
	public MtiZip(String prefix, String root, String selectedFileName) {
		if(prefix!=null) { _tmpPath += prefix+File.separator; }
		if(selectedFileName!=null) {
			_srcFileName 	= new String(selectedFileName);
			_saveAsFileName = new String(selectedFileName);		
			try {
				ZipFile zipFile = new ZipFile(selectedFileName);
				if(root==null) cleanDirectory(getTempPath());
				else cleanDirectory(getDestFilePath());
				zipFile.extractAll(_tmpPath);
			} catch (ZipException e) {
				e.printStackTrace();
			}
		}
	}

	public void cleanDirectory(String path) {
		File file = new File(path);
		if(file.exists()) {
			if(file.list().length>0){
				try {
					FileUtils.cleanDirectory(file);
				} catch (IOException e) { e.printStackTrace(); }
			} else {
				file.mkdir();
			}
		} 
	}
	
	public static String getExtension() 	{ return _extension; }
	public String getSrcFileName() 			{ return _srcFileName; }
	public String getDestFilePath() 		{ return _tmpPath+"mpz"+System.getProperty("file.separator"); }
	//public String getTempPath() 			{ return _tmpPath+System.getProperty("file.separator"); }
	public String getTempPath() 			{ return _tmpPath; }
	
	public void setSrcFileName(String filename) 			{ _srcFileName 	= filename; }
	public void setSaveAsFileName(String filename) 			{ _saveAsFileName 	= filename; }
	
	public void addTmpFolderAndRestoreFileName() {
		try {
			String base = FilenameUtils.removeExtension(_srcFileName);
			long currTime = System.currentTimeMillis();
			String tmpZipFileName = base + currTime%1000+"."+_extension;
			File tmpFile = new File(tmpZipFileName);
			
			while (tmpFile.exists()) {
				currTime = System.currentTimeMillis();
				tmpZipFileName = base + currTime%1000+"."+_extension;
				tmpFile = new File(tmpZipFileName);
			}
			
			ZipFile zipFile = new ZipFile(tmpFile);
			ZipParameters parameters = new ZipParameters();
			parameters.setCompressionMethod(Zip4jConstants.COMP_DEFLATE); 
			parameters.setCompressionLevel(Zip4jConstants.DEFLATE_LEVEL_NORMAL); 
			
			String folderToAdd = getDestFilePath();
			zipFile.addFolder(folderToAdd, parameters);
			//System.out.println("src="+base +"."+_extension+" saveAs="+_saveAsFileName);
			restoreFileName(new File(base +"."+_extension), new File(_saveAsFileName), tmpZipFileName);
			_srcFileName = _saveAsFileName;
			
		} catch (ZipException e) {
			e.printStackTrace();
		} 
	}
	
	public void zip(String zipFileName) {
		try {
			ZipFile zipFile = new ZipFile(new File(zipFileName));
			ZipParameters parameters = new ZipParameters();
			parameters.setCompressionMethod(Zip4jConstants.COMP_DEFLATE); 
			parameters.setCompressionLevel(Zip4jConstants.DEFLATE_LEVEL_NORMAL); 
			
			String folderToAdd = getTempPath();
			zipFile.addFolder(folderToAdd, parameters);
		} catch (ZipException e) {
			e.printStackTrace();
		} 
	}
	
	private void restoreFileName(File zipFile, File saveAsFile, String tmpZipFileName) throws ZipException {
		//System.out.println(tmpZipFileName + " zip="+zipFile.getAbsolutePath());
		if(zipFile.getName().contains("untitled")) {
			if (!zipFile.delete()) throw new ZipException("cannot delete old mpz file "+zipFile);
		}
		if(saveAsFile.exists()) {
			if (!saveAsFile.delete()) throw new ZipException("cannot delete save as mpz file "+saveAsFile.getName());
		}
		File newZipFile = new File(tmpZipFileName);
		if (!newZipFile.renameTo(saveAsFile)) {
			throw new ZipException("cannot rename modified mpz file "+tmpZipFileName+" to "+saveAsFile.getName());
		}
	}

	public static void getAllFilesInDirectory(File dir) {
		try {
			System.out.println("Getting all files in " + dir.getCanonicalPath() + " including those in subdirectories");
		} catch (IOException e) {
			e.printStackTrace();
		}
		List<File> files = (List<File>) FileUtils.listFiles(dir, TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE);
		for (File file : files) {
			try {
				System.out.println("file: " + file.getCanonicalPath());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public static void create(String zipFileName) {
		try {
			// Instantiate a Date object
			Date date = new Date();
			//System.out.println(date.toString());
			String strToWrite = date.toString();
			byte[] bytesToWrite = strToWrite.getBytes();

			InMemoryOutputStream inMemoryOutputStream = new InMemoryOutputStream();

			ZipOutputStream zos = new ZipOutputStream(inMemoryOutputStream);

			ZipParameters parameters = new ZipParameters();
			parameters.setCompressionMethod(Zip4jConstants.COMP_DEFLATE);
			parameters.setCompressionLevel(Zip4jConstants.DEFLATE_LEVEL_NORMAL);
			parameters.setFileNameInZip("mpz"+File.separator+"init.txt");
			parameters.setSourceExternalStream(true);

			zos.putNextEntry(null, parameters);
			zos.write(bytesToWrite);
			//zos.write(bytesToWrite,0,bytesToWrite.length);
			zos.closeEntry();
			zos.finish();
			zos.close();

			byte[] zipContent = inMemoryOutputStream.getZipContent();
			FileOutputStream os = new FileOutputStream(new File(zipFileName));
			os.write(zipContent);
			os.close();				
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}
