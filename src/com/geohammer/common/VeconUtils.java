package com.geohammer.common;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.URL;
import java.nio.ByteOrder;
import java.util.StringTokenizer;

import javax.swing.JOptionPane;

import org.apache.commons.io.FilenameUtils;

import edu.mines.jtk.io.ArrayFile;

public class VeconUtils {
	
	public static float[][] to2DArray(int nx, int ny, float[] dp){
		float[][] v = new float[ny][nx];
		int k = 0;
		for(int iy=0; iy<ny; iy++) {
			for(int ix=0; ix<nx; ix++) {
				v[iy][ix] = dp[k];
				k++;
			}
		}
		return v;
	}
	public static float[][] to2DArrayTranspose(int nx, int ny, float[] dp){
		float[][] v = new float[nx][ny];
		int k = 0;
		for(int iy=0; iy<ny; iy++) {
			for(int ix=0; ix<nx; ix++) {
				v[ix][iy] = dp[k];
				k++;
			}
		}
		return v;
	}
	public static float [] from2DArray(float[][] v){
		int nx = v[0].length;
		int ny = v.length;
		float [] dp = new float[nx*ny];
		int k = 0;
		for(int iy=0; iy<ny; iy++) {
			for(int ix=0; ix<nx; ix++) {
				dp[k] = v[iy][ix];
				k++;
			}
		}
		return dp;
	}
	
	public static float[][] MatrixTranspose(float[][] f){
		int nx = f[0].length;
		int ny = f.length;

		float[][] v = new float[nx][ny];
		for(int ix=0; ix<nx; ix++) {
			for(int iy=0; iy<ny; iy++) {
				v[ix][iy] = f[iy][ix];
			}
		}
		return v;
	}
	public static double[][] MatrixTranspose(double[][] f){
		int nx = f[0].length;
		int ny = f.length;

		double[][] v = new double[nx][ny];
		for(int ix=0; ix<nx; ix++) {
			for(int iy=0; iy<ny; iy++) {
				v[ix][iy] = f[iy][ix];
			}
		}
		return v;
	}
	public static float[] toFloatArray(double [] v) {
		int n = v.length;
		float [] f = new float[n];
		for(int i=0; i<n; i++) {
			f[i] = (float)v[i];
		}
		return f;
	}
	public static double[] toDoubleArray(float [] v) {
		int n = v.length;
		double [] f = new double[n];
		for(int i=0; i<n; i++) {
			f[i] = (double)v[i];
		}
		return f;
	}
	//float version
	public static float[] csvFloatReader(String selectedFileName, int skipNumRow, int col1Index, int col2Index, int col3Index, int col4Index ) {
		double [] v = csvDoubleReader(selectedFileName, skipNumRow, col1Index, col2Index, col3Index, col4Index);
		if(v==null) {
			return null;
		} 
		return  toFloatArray(v);
	}
	public static double[] csvReader(String selectedFileName, int skipNumRow, int col1Index, int col2Index, int col3Index, int col4Index ) {
		return csvDoubleReader(selectedFileName, skipNumRow, col1Index, col2Index, col3Index, col4Index);
	}
	public static double[] csvDoubleReader(String selectedFileName, int skipNumRow, int col1Index, int col2Index, int col3Index, int col4Index ) {
		int n = 0;
		int [] colIndex = new int[]{col1Index, col2Index, col3Index, col4Index};
		if(col1Index>=0) n++;
		if(col2Index>=0) n++;
		if(col3Index>=0) n++;
		if(col4Index>=0) n++;
		double [] v = null;
		
		String line = null;
		
		try {
			BufferedReader reader = new BufferedReader(new FileReader(selectedFileName));

			for(int i=0; i<skipNumRow;i++) {
				line=reader.readLine();
			}
			int nLines = 0;
			while ((line=reader.readLine()) != null)   	{
				nLines++;
			}
			reader.close(); 
			
			v = new double[nLines*n+1];
			v[0] = (double)n;
			
			BufferedReader reader1 = new BufferedReader(new FileReader(selectedFileName));
			for(int i=0; i<skipNumRow;i++) {
				line=reader1.readLine();
			}
			
			StringTokenizer st = null;
			double a = 0.0;
			int k = 0;
			int s = 1;
			for(int i=0; i<nLines; i++) {
				line=reader1.readLine();
				st = new StringTokenizer(line, " ,");
				k = 0;
				while (st.hasMoreTokens()) {
					a = Double.parseDouble(st.nextToken());
					for(int j=0; j<colIndex.length;j++) {
						if(k==colIndex[j]) {
							v[s] = a;
							s++;
						}
					}
					k++;	
				}		
			}
			reader1.close();
			return v;
		} catch (IOException ioexception) {
			ioexception.printStackTrace();
		}
		return v;
	}
	
	//float version
	public static void printArray(int nx, int ny, float[] array) {
		printArray(nx, ny, 1, 1, 1, 1, array);
	}
	public static void printArray(int nx, int ny, int xSteps, int ySteps, float[] array) {
		printArray(nx, ny, 1, xSteps, ySteps, 1, array);
	}
	public static void printArray(int nx, int ny, int nz, float[] array) {
		printArray(nx, ny, nz, 1, 1, 1, array);
	}
	public static void printArray(int nx, int ny, int nz, int xSteps, int ySteps, int zSteps, float[] array) {
		int k = 0;
		int nLen = array.length;
		int nxy = nx*ny;
		System.out.println("nx="+nx+" ny="+ny+" nz="+nz+" nxy="+nxy+" nxyz="+nxy*nz+" arrayLength="+nLen+
				" xSteps="+xSteps+" ySteps="+ySteps+" zSteps="+zSteps);
		int t = 0;
		for(int iz=0; iz<nz; iz+=zSteps) {
			for(int i=0; i<ny; i+=ySteps) {
				for(int j=0; j<nx; j+=xSteps) {
					//if(k<nLen) System.out.print(array[k] + " " + k + " ");
					if(k<nLen) System.out.print(array[k] + " ");
					else { t = 1; break; }
					k = iz*nxy+i*nx+j;
				}
				if(t==1) break;
				System.out.println();
			}
			if(t==1) break;
			System.out.println();
		}
	}
	
	public static void printfArray(String selectedFileName, int nx, int ny, float[] array) {
		printfArray(selectedFileName, nx, ny, 1, 0, 0, 0, array);
	}
	public static void printfArray(String selectedFileName, int nx, int ny, int xSteps, int ySteps, float[] array) {
		printfArray(selectedFileName, nx, ny, 1, xSteps, ySteps, 0, array);
	}
	public static void printfArray(String selectedFileName, int nx, int ny, int nz, float[] array) {
		printfArray(selectedFileName, nx, ny, nz, 0, 0, 0, array);
	}
	public static void printfArray(String selectedFileName, int nx, int ny, int nz, int xSteps, int ySteps, int zSteps, float[] array) {
		try{
			boolean append = false;
			BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(selectedFileName, append));
			
			int k = 0;
			int nLen = array.length;
			int nxy = nx*ny;
			System.out.println("nx="+nx+" ny="+ny+" nz="+nz+" nxy="+nxy+" nxyz="+nxy*nz+" arrayLength="+nLen+
					" xSteps="+xSteps+" ySteps="+ySteps+" zSteps="+zSteps);
			int t = 0;
			for(int iz=0; iz<nz; iz+=zSteps) {
				for(int i=0; i<ny; i+=ySteps) {
					for(int j=0; j<nx; j+=xSteps) {
						if(k<nLen)  bufferedWriter.write(array[k] + " "); 
						else { t = 1; break; }
						k = iz*nxy+i*nx+j;
					}
					if(t==1) break;
					bufferedWriter.write("\n");
				}
				if(t==1) break;
				bufferedWriter.write("\n"); 
			}
			bufferedWriter.close();
		} catch (IOException ioexception) {
			JOptionPane.showMessageDialog(null, "\nFile Format is not right!", 
					"Error", JOptionPane.ERROR_MESSAGE);
		}
	}
	
	//double version
	public static void printArray(int nx, int ny, double[] array) {
		printArray(nx, ny, 1, 1, 1, 1, array);
	}
	public static void printArray(int nx, int ny, int xSteps, int ySteps, double[] array) {
		printArray(nx, ny, 1, xSteps, ySteps, 1, array);
	}
	public static void printArray(int nx, int ny, int nz, double[] array) {
		printArray(nx, ny, nz, 1, 1, 1, array);
	}
	public static void printArray(int nx, int ny, int nz, int xSteps, int ySteps, int zSteps, double[] array) {
		int k = 0;
		int nLen = array.length;
		int nxy = nx*ny;
		System.out.println("nx="+nx+" ny="+ny+" nz="+nz+" nxy="+nxy+" nxyz="+nxy*nz+" arrayLength="+nLen+
				" xSteps="+xSteps+" ySteps="+ySteps+" zSteps="+zSteps);
		int t = 0;
		for(int iz=0; iz<nz; iz+=zSteps) {
			for(int i=0; i<ny; i+=ySteps) {
				for(int j=0; j<nx; j+=xSteps) {
					//if(k<nLen) System.out.print(array[k] + " " + k + " ");
					if(k<nLen) System.out.print(array[k] + " ");
					else { t = 1; break; }
					k = iz*nxy+i*nx+j;
				}
				if(t==1) break;
				System.out.println();
			}
			if(t==1) break;
			System.out.println();
		}
	}
	
	public static void printfArray(String selectedFileName, int nx, int ny, double[] array) {
		printfArray(selectedFileName, nx, ny, 1, 1, 1, 1, array);
	}
	public static void printfArray(String selectedFileName, int nx, int ny, int xSteps, int ySteps, double[] array) {
		printfArray(selectedFileName, nx, ny, 1, xSteps, ySteps, 1, array);
	}
	public static void printfArray(String selectedFileName, int nx, int ny, int nz, double[] array) {
		printfArray(selectedFileName, nx, ny, nz, 1, 1, 1, array);
	}
	public static void printfArray(String selectedFileName, int nx, int ny, int nz, int xSteps, int ySteps, int zSteps, double[] array) {
		try{
			boolean append = false;
			BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(selectedFileName, append));
			
			int k = 0;
			int nLen = array.length;
			int nxy = nx*ny;
			System.out.println("nx="+nx+" ny="+ny+" nz="+nz+" nxy="+nxy+" nxyz="+nxy*nz+" arrayLength="+nLen+
					" xSteps="+xSteps+" ySteps="+ySteps+" zSteps="+zSteps);
			int t = 0;
			for(int iz=0; iz<nz; iz+=zSteps) {
				for(int i=0; i<ny; i+=ySteps) {
					for(int j=0; j<nx; j+=xSteps) {
						k = iz*nxy+i*nx+j;
						if(k<nLen)  bufferedWriter.write(array[k] + ", "); 
						else { t = 1; break; }
					}
					if(t==1) break;
					bufferedWriter.write("\n");
				}
				if(t==1) break;
				bufferedWriter.write("\n"); 
			}
			bufferedWriter.close();
		} catch (IOException ioexception) {
			JOptionPane.showMessageDialog(null, "\nFile Format is not right!", 
					"Error", JOptionPane.ERROR_MESSAGE);
		}
	}

	public static boolean containsLetter(String s) {
		if ( s == null )
			return false;
		boolean letterFound = false;
		for (int i = 0; !letterFound && i < s.length(); i++)
			letterFound = letterFound || Character.isLetter(s.charAt(i));
		return letterFound;
	} 
	public static boolean isNumber(String s) {
		return !containsLetter(s);
	} 

	public static Dimension stringSize(String str, Graphics g) {
		if (g instanceof Graphics2D) {
			java.awt.geom.Rectangle2D bounds = g.getFont().getStringBounds(str, ((Graphics2D)g).getFontRenderContext());
			return new Dimension( (int)(bounds.getWidth()+.5), (int)(bounds.getHeight()+.5));
		}        
		else
			return new Dimension(g.getFontMetrics().stringWidth(str), g.getFontMetrics().getHeight());
	}
	
	
	public static URL getResource(final String filename) throws IOException {
        // Try to load resource from jar
        URL url = ClassLoader.getSystemResource(filename);
        // If not found in jar, then load from disk
        if (url == null) {
            return new URL("file", "localhost", filename);
        } else {
            return url;
        }
    }

    public static InputStream getResourceAsStream(final String filename) throws IOException {
        // Try to load resource from jar
        InputStream stream = ClassLoader.getSystemResourceAsStream(filename);
        // If not found in jar, then load from disk
        if (stream == null) {
            return new FileInputStream(filename);
        } else {
            return stream;
        }
    }
    
	public static String replaceFileExtension(String fileName, String newExtension) {
		String fullPath = FilenameUtils.getFullPath(fileName);
		String baseName = FilenameUtils.getBaseName(fileName);
		return fullPath+baseName+"."+newExtension;
	}
	public static boolean readTextFileToArray(float [][] D, String selectedFile ) {
		int nx = D[0].length;
		int nz = D.length;
		
		try {
			BufferedReader reader = new BufferedReader(new FileReader(selectedFile));
			
			for(int i=0; i<nz; i++) {
				String line=reader.readLine();
				StringTokenizer st = new StringTokenizer(line, " ");
				for(int j=0; j<nx; j++) {
					D[i][j] = Float.parseFloat(st.nextToken().trim());
				}
			}
			reader.close();
		} catch (IOException ioexception) {
			ioexception.printStackTrace();
		}
		return true;
	}
	public static boolean saveArrayToTextFile(float [][] D, String selectedFile ) {
		int nx = D[0].length;
		int nz = D.length;
		
		try {
			BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(selectedFile, false));

			for(int i=0; i<nz; i++) {
				for(int j=0; j<nx; j++) bufferedWriter.write(D[i][j]+" ");
				bufferedWriter.newLine();
			}
			bufferedWriter.close();
		} catch (IOException ioexception) {
			ioexception.printStackTrace();
		}
		return true;
	}
	public static void saveTextToFile( String text, String selectedFile ) {	
		try {
			PrintWriter out = new PrintWriter(new FileWriter(selectedFile));
			out.print(text);
			if (out.checkError()) throw new IOException("Error while writing to file.");
			out.close();
		}
		catch (IOException ioexception)	{
			String s1 = "IOException: " + selectedFile;
			JOptionPane.showMessageDialog(null, s1, "Error", JOptionPane.ERROR_MESSAGE);
		}
	}
	public static boolean saveArrayToBinaryFile(float [][] D, String selectedFileName) {	
		ArrayFile af 		= null;
		try {
			File file = new File(selectedFileName);
			if (file.exists ()) {
				file.delete();
			}
			file = new File(selectedFileName);
			af = new ArrayFile(file,"rw", ByteOrder.LITTLE_ENDIAN, ByteOrder.LITTLE_ENDIAN);
			af.seek(0);
			for(int i=0; i<D.length; i++) {
				for(int j=0; j<D[0].length; j++) {
					af.writeFloat(D[i][j]);
				}
			}
			//af.writeFloats(D);
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
	public static boolean readBinaryFileToArray(float [][] D, String selectedFileName ) {	
		ArrayFile af 		= null;
		try {
			File file = new File(selectedFileName);
			if (!file.exists ())  return false; 
			file = new File(selectedFileName);
			af = new ArrayFile(file,"r", ByteOrder.LITTLE_ENDIAN, ByteOrder.LITTLE_ENDIAN);
			af.seek(0);
			for(int i=0; i<D.length; i++) {
				for(int j=0; j<D[0].length; j++) {
					D[i][j] = af.readFloat();
				}
			}
			//af.readFloats(D);
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
	
	public static boolean saveArrayToBinaryFile1(float [][] D, String selectedFileName) {	
		DataOutputStream dos = null;
		try {
			File file = new File(selectedFileName);
			if (file.exists ()) {
				file.delete();
			}
			file = new File(selectedFileName);
			dos = new DataOutputStream(new FileOutputStream(selectedFileName));
			for(int i=0; i<D.length; i++) {
				for(int j=0; j<D[0].length; j++) {
					dos.writeFloat(D[i][j]);
				}
			}
			//af.writeFloats(D);
		} catch (FileNotFoundException e) {
			System.err.println("Caught FileNotFoundException: " + e.getMessage());
			throw new RuntimeException(e);
		} catch (IOException e) {
			System.err.println("Caught IOException: " + e.getMessage());
		} finally {
			if (dos!=null) try { dos.close(); } catch (IOException e) {}
		}
		return true;
	}
	public static boolean readBinaryFileToArray1(float [][] D, String selectedFileName ) {	
		DataInputStream dis = null;
		try {
			File file = new File(selectedFileName);
			if (!file.exists ())  return false; 
			file = new File(selectedFileName);
			dis = new DataInputStream(new FileInputStream(selectedFileName));
			for(int i=0; i<D.length; i++) {
				for(int j=0; j<D[0].length; j++) {
					D[i][j] = dis.readFloat();
				}
			}
			//af.readFloats(D);
		} catch (FileNotFoundException e) {
			System.err.println("Caught FileNotFoundException: " + e.getMessage());
			throw new RuntimeException(e);
		} catch (IOException e) {
			System.err.println("Caught IOException: " + e.getMessage());
		} finally {
			if (dis!=null) try { dis.close(); } catch (IOException e) {}
		}
		return true;
	}
	
	// sort arrays
	public static void shellSort(float [] dist, float [] calT) {
		int i = 0, j = 0, inc = 1;
		float a = 0.0f;
		float b = 0.0f;
		
		int m = dist.length;
		do{
			inc *=3;
			inc++;
		} while(inc<=m);
		
		do {
			inc /= 3;
			for(i=inc; i<m; i++) {
				a = dist[i];
				b = calT[i];
				j = i;
				while(dist[j-inc]>a) {
					dist[j] = dist[j-inc];
					calT[j] = calT[j-inc];
					j -= inc;
					if(j<inc) break;
				}
				dist[j] = a;
				calT[j] = b;
			}
		} while(inc>1);
	}
	
	// sort arrays
	public static void shellSort(float [] z, float [] x, float [] y) {
		int i = 0, j = 0, inc = 1;
		float a = 0.0f;
		float b = 0.0f;
		float c = 0.0f;
		
		int m = z.length;
		do{
			inc *=3;
			inc++;
		} while(inc<=m);
		
		do {
			inc /= 3;
			for(i=inc; i<m; i++) {
				a = z[i];
				b = x[i];
				c = y[i];
				j = i;
				while(z[j-inc]>a) {
					z[j] = z[j-inc];
					x[j] = x[j-inc];
					y[j] = y[j-inc];
					j -= inc;
					if(j<inc) break;
				}
				z[j] = a;
				x[j] = b;
				y[j] = c;
			}
		} while(inc>1);
	}
	
	public static float getMin(float [] data, int [] index) {
		int k = 0;
		float min = data[k];
		for(int i=1; i<data.length; i++) {
			if(min>data[i]) { min=data[i]; k=i; }
		}
		if(index!=null) index[0] = k;
		return min;
	}
	
	public static float getMax(float [] data, int [] index) {
		int k = 0;
		float max = data[k];
		for(int i=1; i<data.length; i++) {
			if(max<data[i]) { max=data[i]; k=i; }
		}
		if(index!=null) index[0] = k;
		return max;
	}
}
