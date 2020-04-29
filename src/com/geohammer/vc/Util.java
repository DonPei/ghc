package com.geohammer.vc;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.StringTokenizer;

import javax.swing.JOptionPane;

public class Util {

	public Util(String inputFileName) {
		String contents2 = "* Module_D" + "4" + "#Begin" + "# End" + "#Begin1" + "# End1";
		updateModuleContents("Module_D", contents2, inputFileName);
	}
	public static void main(String[] args) {		
		new Util("C:\\TEMP\\test.txt");
	}

	public static String [] getModuleContents(String module, String selectedFileName) {
		int lineCount = 0;
		String [] contents = null;
		if(selectedFileName==null) return null;
		try{
			BufferedReader reader = new BufferedReader(new FileReader(selectedFileName));

			String line = null;
			while ((line=reader.readLine()) != null) {
				if(line.trim().startsWith("*")) {
					StringTokenizer st = new StringTokenizer(line, " ");
					st.nextToken();
					String item = st.nextToken().trim();
					if(item.equalsIgnoreCase(module)) {
						String a = line;
						line=reader.readLine();
						st = new StringTokenizer(line, " ");
						item = st.nextToken().trim();
						int nLines = Integer.parseInt(item);
						contents = new String[nLines+1];
						contents[0] = a;
						for(int i=1; i<contents.length; i++) {
							line=reader.readLine();
							contents[i] = line;
							//System.out.println(line);
						}
						lineCount++;
					}
				}
			}
			reader.close();
		} catch (IOException ioexception) {
		}	
		return contents;
	}

	public static void updateModuleContents(String key, String contents, String selectedFileName) {
		if(selectedFileName==null) return;
		String [] tmpA = getModuleContents(key, selectedFileName);
		if(tmpA==null) {
			try{
				BufferedWriter writer = new BufferedWriter(new FileWriter(selectedFileName, true));
				if(contents!=null) {
					writer.write(contents);
					writer.newLine();
				}
				writer.close();
			} catch (IOException ioexception) {
			}
		} else {
			try{
				String tmpFileName = selectedFileName+"_tmp";
				BufferedReader reader = new BufferedReader(new FileReader(selectedFileName));
				BufferedWriter writer = new BufferedWriter(new FileWriter(tmpFileName, false));

				String line = null;
				while ((line=reader.readLine()) != null) {
					if(line.trim().startsWith("*")) {
						StringTokenizer st = new StringTokenizer(line, " ");
						st.nextToken();
						String item = st.nextToken().trim();
						if(item.equalsIgnoreCase(key)) {
							String a = line;
							line=reader.readLine();
							st = new StringTokenizer(line, " ");
							item = st.nextToken().trim();
							int nLines = Integer.parseInt(item);
							String [] tmp = new String[nLines+1];
							tmp[0] = a;
							for(int i=1; i<tmp.length; i++) {
								line=reader.readLine();
								tmp[i] = line;
								//System.out.println(line);
							}

							if(contents!=null) {
								writer.write(contents);
								writer.newLine();
							}
						} else {
							writer.write(line);
							writer.newLine();
						}
					} else {
						writer.write(line);
						writer.newLine();	
					}
				}
				reader.close();
				writer.close();

				copyFile(tmpFileName, selectedFileName);
				deleteFile(tmpFileName);

			} catch (IOException ioexception) {
			}
		}
	}
	
	public static void copyFile(String fromFile, String toFile) {
		try{
			BufferedReader reader = new BufferedReader(new FileReader(fromFile));
			BufferedWriter writer = new BufferedWriter(new FileWriter(toFile, false));

			//... Loop as long as there are input lines.
			String line = null;
			while ((line=reader.readLine()) != null) {
				writer.write(line);
				writer.newLine();   // Write system dependent end of line.
			}

			//... Close reader and writer.
			reader.close();  // Close to unlock.
			writer.close();  // Close to unlock and flush to disk.
		} catch (IOException ioexception) {
		}	
	}
	public static void deleteFile(String selectedFileName) {
		try{
			File file = new File(selectedFileName);
			file.delete();
		}catch(Exception e){}
	}

}
