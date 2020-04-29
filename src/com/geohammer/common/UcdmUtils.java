package com.geohammer.common;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.ByteOrder;
import java.util.EnumSet;
import java.util.StringTokenizer;

import javax.swing.JOptionPane;

import com.geohammer.core.SeismicTraceComponent;

import edu.mines.jtk.io.ArrayFile;

import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.FileVisitOption;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

public class UcdmUtils  {

	
	public static String [] findNumbersInString(String str) {
		//String str = "qwerty1qwerty2";      
		str = str.replaceAll("[^0-9]+", " ");
		str = str.trim();
		if(str.isEmpty()) return null;
		String [] results = str.split(" ");
		//System.out.println(Arrays.asList(str.trim().split(" "))); //[1, 2]
		return results;
		
		//If you want to include - a.e minus, add -?:
//		String str = "qwerty-1qwerty-2 455 f0gfg 4";      
//		str = str.replaceAll("[^-?0-9]+", " "); 
//		System.out.println(Arrays.asList(str.trim().split(" "))); //[-1, -2, 455, 0, 4]
//		Description [^-?0-9]+
//		+ Between one and unlimited times, as many times as possible, giving back as needed
//		-? One of the characters �-?�
//		0-9 A character in the range between �0� and �9�
	}
	
	//float version
	public static void printArray(float [] array) {
		System.out.print(""+array[0]);
		for(int i=1; i<array.length; i++) System.out.print(", "+array[i]);
		System.out.println(" ");
	}
	public static void printArray(float [][] array) {
		for(int i=0; i<array.length; i++) {
			System.out.print("\n"+array[i][0]);
			for(int j=1; j<array[i].length; j++) System.out.print(", "+array[i][j]);
		}

		System.out.println(" ");
	}

	//double version
	public static void printArray(double [] array) {
		System.out.print(""+array[0]);
		for(int i=1; i<array.length; i++) System.out.print(", "+array[i]);
		System.out.println(" ");
	}
	public static void printArray(double [][] array) {
		for(int i=0; i<array.length; i++) {
			System.out.print("\n"+array[i][0]);
			for(int j=1; j<array[i].length; j++) System.out.print(", "+array[i][j]);
		}

		System.out.println(" ");
	}
	public static float [][] transposeMatrix(float [][] array) {
		int nRow = array.length;
		int nCol = array[0].length;
		float [][] b = new float [nCol][nRow];
		for(int i=0; i<b.length; i++) {
			for(int j=0; j<b[i].length; j++) {
				b[i][j] = array[j][i];
			}
		}
		return b;
	}
	public static float [] toFloatVector(boolean transpose, double [] v) {
		int nRow = v.length;
		float [] b = new float[nRow];
		for(int i=0; i<b.length; i++) {
			b[i] = (float)v[i];
		}
		return b;
	}
	public static float [][] toFloatMatrix(boolean transpose, double [][] array) {
		int nRow = array.length;
		int nCol = array[0].length;
		float [][] b = null;
		if(transpose) {
			b = new float[nCol][nRow];
			for(int i=0; i<b.length; i++) {
				for(int j=0; j<b[i].length; j++) {
					b[i][j] = (float)array[j][i];
				}
			}
		} else {
			b = new float[nRow][nCol];
			for(int i=0; i<b.length; i++) {
				for(int j=0; j<b[i].length; j++) {
					b[i][j] = (float)array[i][j];
				}
			}
		}
		return b;
	}
	

	public static SeismicTraceComponent [] convertTo1D(SeismicTraceComponent [][] compsOrig) {
		SeismicTraceComponent [] comps = new SeismicTraceComponent[compsOrig.length*compsOrig[0].length];
		for(int i=0, k=0; i<compsOrig.length; i++) { 
			for(int j=0; j<compsOrig[i].length; j++) { 
				comps[k++] = compsOrig[i][j];
			}
		}
		return comps;
	}
	public static boolean saveTraceToBinaryFile(SeismicTraceComponent [][] comps, String selectedFileName) {	
		return saveTraceToBinaryFile(convertTo1D(comps), selectedFileName);	
	}
	public static boolean saveTraceToBinaryFile(SeismicTraceComponent [] comps, String selectedFileName) {	
		ArrayFile af 		= null;
		try {
			File file = new File(selectedFileName);
			if (file.exists ()) {
				file.delete();
			}
			file = new File(selectedFileName);
			af = new ArrayFile(file,"rw", ByteOrder.LITTLE_ENDIAN, ByteOrder.LITTLE_ENDIAN);
			af.seek(0);
			int version = 1;
			af.writeInt(version);				
			af.writeInt(comps.length);

			for(int i=0; i<comps.length; i++) {
				float [] data = comps[i].getData();
				af.writeDouble(comps[i].getX());
				af.writeDouble(comps[i].getY());
				af.writeDouble(comps[i].getZ());				
				af.writeInt(comps[i]._wellId);
				af.writeInt(comps[i]._sensorId);
				af.writeInt(comps[i]._compId);
				af.writeInt(data.length);
				af.writeDouble(comps[i]._sampleInterval);
				af.writeDouble(comps[i].getPpick());
				af.writeDouble(comps[i].getSHpick());
				af.writeDouble(comps[i].getSVpick());
				af.writeDouble(comps[i].getPcpick());
				af.writeDouble(comps[i].getSHcpick());
				af.writeDouble(comps[i].getSVcpick());
				af.writeFloats(data);
			}
		} catch (FileNotFoundException e) {
			System.err.println("Caught FileNotFoundException: " + e.getMessage());
			throw new RuntimeException(e);
		} catch (IOException e) {
			System.err.println("Caught IOException: " + e.getMessage());
		} finally {
			if (af!=null) try { af.close(); } catch (IOException e) {}
		}
		return true;
	}

	public static SeismicTraceComponent [] readBinaryFileToTrace(String selectedFileName ) {	
		ArrayFile af 		= null;
		try {
			File file = new File(selectedFileName);
			if (!file.exists ())  return null; 
			file = new File(selectedFileName);
			af = new ArrayFile(file,"r", ByteOrder.LITTLE_ENDIAN, ByteOrder.LITTLE_ENDIAN);
			af.seek(0);
			int version = af.readInt();
			int k = af.readInt();
			SeismicTraceComponent [] comps = new SeismicTraceComponent[k];
			for(int i=0; i<comps.length; i++) {
				double x = af.readDouble();
				double y = af.readDouble();
				double z = af.readDouble();
				int wellId = af.readInt();
				int sensorId = af.readInt();
				int compId = af.readInt();
				int n = af.readInt();
				double sampleInterval = af.readDouble();
				double pPick = af.readDouble();
				double shPick = af.readDouble();
				double svPick = af.readDouble();
				double pcPick = af.readDouble();
				double shcPick = af.readDouble();
				double svcPick = af.readDouble();

				float [] data = new float[n];
				af.readFloats(data);

				comps[i] = new SeismicTraceComponent(i, "comp "+i, x, y, z, 
						wellId, sensorId, compId, sampleInterval, data);
				comps[i].setPpick(pPick);
				comps[i].setSHpick(shPick);
				comps[i].setSVpick(svPick);
				comps[i].setPcpick(pcPick);
				comps[i].setSHcpick(shcPick);
				comps[i].setSVcpick(svcPick);
			}
			return comps;
		} catch (FileNotFoundException e) {
			System.err.println("Caught FileNotFoundException: " + e.getMessage());
			throw new RuntimeException(e);
		} catch (IOException e) {
			System.err.println("Caught IOException: " + e.getMessage());
		} finally {
			if (af!=null) try { af.close(); } catch (IOException e) {}
		}
		return null;
	}

	//http://www.geeksforgeeks.org/next-power-of-2/
	public static int nextPowerOf2(final int a)   {
		int b = 1;
		while (b < a) {
			b = b << 1;
		}
		return b;
	}

	public static String readTextFile(String selectedFile ) {		
		try {
			BufferedReader reader = new BufferedReader(new FileReader(selectedFile));
			String line = reader.readLine(); 
			StringBuilder sb = new StringBuilder(); 
			while(line != null){ 
				sb.append(line).append("\n"); 
				line = reader.readLine(); 
			} 
			reader.close();

			String fileAsString = sb.toString();
			return fileAsString;
		} catch (IOException ioexception) {
			ioexception.printStackTrace();
		}
		return null;
	}

	public static boolean saveArrayToBinaryFileF3(float [][][] array, String selectedFileName) {	
		ArrayFile af 		= null;
		try {
			File file = new File(selectedFileName);
			if (file.exists ()) {
				file.delete();
			}
			file = new File(selectedFileName);
			af = new ArrayFile(file,"rw", ByteOrder.LITTLE_ENDIAN, ByteOrder.LITTLE_ENDIAN);
			af.seek(0);
			int version = 1;
			af.writeInt(version);	
			float [] a = null;
			af.writeInt(array.length);
			for(int i=0; i<array.length; i++) {
				af.writeInt(array[i].length);
				for(int j=0; j<array[i].length; j++) {
					a = array[i][j];
					if(a==null) af.writeInt(0);
					else {
						af.writeInt(a.length);
						af.writeFloats(a);
					}
				}
			}
		} catch (FileNotFoundException e) {
			System.err.println("Caught FileNotFoundException: " + e.getMessage());
			throw new RuntimeException(e);
		} catch (IOException e) {
			System.err.println("Caught IOException: " + e.getMessage());
		} finally {
			if (af!=null) try { af.close(); } catch (IOException e) {}
		}
		return true;
	}

	public static float [][][] readBinaryFileToArrayF3(String selectedFileName ) {	
		ArrayFile af 		= null;
		try {
			File file = new File(selectedFileName);
			if (!file.exists ())  return null; 
			file = new File(selectedFileName);
			af = new ArrayFile(file,"r", ByteOrder.LITTLE_ENDIAN, ByteOrder.LITTLE_ENDIAN);
			af.seek(0);
			int version = af.readInt();
			int nI = af.readInt();
			float [][][] array = new float[nI][][];
			for(int i=0; i<nI; i++) {
				int nJ = af.readInt();
				float [][] dJ = new float[nJ][];
				for(int j=0; j<nJ; j++) {
					int nK = af.readInt();
					float [] a = null;
					if(nK!=0) {
						a = new float[nK];
						af.readFloats(a);
					}
					dJ[j] = a;
				}
				array[i] = dJ;
			}

			return array;
		} catch (FileNotFoundException e) {
			System.err.println("Caught FileNotFoundException: " + e.getMessage());
			throw new RuntimeException(e);
		} catch (IOException e) {
			System.err.println("Caught IOException: " + e.getMessage());
		} finally {
			if (af!=null) try { af.close(); } catch (IOException e) {}
		}
		return null;
	}
	public static boolean saveArrayToBinaryFileD2(double [][] array, String selectedFileName) {	
		ArrayFile af 		= null;
		try {
			File file = new File(selectedFileName);
			if (file.exists ()) {
				file.delete();
			}
			file = new File(selectedFileName);
			af = new ArrayFile(file,"rw", ByteOrder.LITTLE_ENDIAN, ByteOrder.LITTLE_ENDIAN);
			af.seek(0);
			int version = 1;
			af.writeInt(version);	
			double [] a = null;
			af.writeInt(array.length);
			for(int i=0; i<array.length; i++) {
				a = array[i];
				if(a==null) af.writeInt(0);
				else {
					af.writeInt(a.length);
					af.writeDoubles(a);
				}
			}
		} catch (FileNotFoundException e) {
			System.err.println("Caught FileNotFoundException: " + e.getMessage());
			throw new RuntimeException(e);
		} catch (IOException e) {
			System.err.println("Caught IOException: " + e.getMessage());
		} finally {
			if (af!=null) try { af.close(); } catch (IOException e) {}
		}
		return true;
	}
	public static double [][] readBinaryFileToArrayD2(String selectedFileName ) {	
		ArrayFile af 		= null;
		try {
			File file = new File(selectedFileName);
			if (!file.exists ())  return null; 
			file = new File(selectedFileName);
			af = new ArrayFile(file,"r", ByteOrder.LITTLE_ENDIAN, ByteOrder.LITTLE_ENDIAN);
			af.seek(0);
			int version = af.readInt();
			int nI = af.readInt();
			double [][] array = new double[nI][];
			for(int i=0; i<nI; i++) {
				int nK = af.readInt();
				double [] a = null;
				if(nK!=0) {
					a = new double[nK];
					af.readDoubles(a);
				}
				array[i] = a;
			}

			return array;
		} catch (FileNotFoundException e) {
			System.err.println("Caught FileNotFoundException: " + e.getMessage());
			throw new RuntimeException(e);
		} catch (IOException e) {
			System.err.println("Caught IOException: " + e.getMessage());
		} finally {
			if (af!=null) try { af.close(); } catch (IOException e) {}
		}
		return null;
	}

	public void listFileTree(String selectedFilePath ) {
		Path path = Paths.get(selectedFilePath);
		ListFiles listFiles = new ListFiles();
		try {
			Files.walkFileTree(path, listFiles);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	//Using the SimpleFileVisitor class to traverse file systems
	private class ListFiles extends SimpleFileVisitor<Path> {
		private final int indentionAmount = 3;
		private int indentionLevel;

		public ListFiles() {
			indentionLevel = 0;
		}

		private void indent() {
			for (int i = 0; i < indentionLevel; i++) {
				System.out.print(' ');
			}
		}

		@Override
		public FileVisitResult visitFile(Path file, BasicFileAttributes attributes) {
			indent();
			System.out.println("Visiting file:" + file.getFileName());
			//		    if (file.toString().endsWith(".java")) {
			//		        System.out.println(file.getFileName());
			//		      }
			return FileVisitResult.CONTINUE;
		}

		@Override
		public FileVisitResult postVisitDirectory(Path directory, IOException e)
				throws IOException {
			indentionLevel -= indentionAmount;
			indent();
			System.out.println("Finished with the directory: "
					+ directory.getFileName());
			return FileVisitResult.CONTINUE;
		}

		@Override
		public FileVisitResult preVisitDirectory(Path directory,
				BasicFileAttributes attributes) throws IOException {
			indent();
			System.out.println("About to traverse the directory: "
					+ directory.getFileName());
			indentionLevel += indentionAmount;
			return FileVisitResult.CONTINUE;
		}

		@Override
		public FileVisitResult visitFileFailed(Path file, IOException exc)
				throws IOException {
			System.out.println("A file traversal error ocurred");
			return super.visitFileFailed(file, exc);
		}
	}

	public void copyFileTree(String sourcePath, String targetPath) {
		try {
			Path source = Paths.get(sourcePath);
			Path target = Paths.get(targetPath);
			Files.walkFileTree(source, EnumSet.of(FileVisitOption.FOLLOW_LINKS),
					Integer.MAX_VALUE, new CopyDirectory(source, target));
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	private class CopyDirectory extends SimpleFileVisitor<Path> {

		private Path source;
		private Path target;

		public CopyDirectory(Path source, Path target) {
			this.source = source;
			this.target = target;
		}

		@Override
		public FileVisitResult visitFile(Path file, BasicFileAttributes attributes)
				throws IOException {
			System.out.println("Copying " + source.relativize(file));
			Files.copy(file, target.resolve(source.relativize(file)));
			return FileVisitResult.CONTINUE;
		}

		@Override
		public FileVisitResult preVisitDirectory(Path directory,
				BasicFileAttributes attributes) throws IOException {
			Path targetDirectory = target.resolve(source.relativize(directory));
			try {
				System.out.println("Copying " + source.relativize(directory));
				Files.copy(directory, targetDirectory);
			} catch (FileAlreadyExistsException e) {
				if (!Files.isDirectory(targetDirectory)) {
					throw e;
				}
			}
			return FileVisitResult.CONTINUE;
		}
	}

	public void deleteFileTree(String selectedFilePath ) {
		try {
			Files.walkFileTree(Paths.get(selectedFilePath), new DeleteDirectory());
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	private class DeleteDirectory extends SimpleFileVisitor<Path> {
		@Override
		public FileVisitResult visitFile(Path file, BasicFileAttributes attributes)
				throws IOException {
			System.out.println("Deleting " + file.getFileName());
			Files.delete(file);
			return FileVisitResult.CONTINUE;
		}

		@Override
		public FileVisitResult postVisitDirectory(Path directory,
				IOException exception) throws IOException {
			if (exception == null) {
				System.out.println("Deleting " + directory.getFileName());
				Files.delete(directory);
				return FileVisitResult.CONTINUE;
			} else {
				throw exception;
			}
		}
	}


}
