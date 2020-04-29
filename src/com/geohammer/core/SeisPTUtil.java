package com.geohammer.core;

import java.awt.Color;
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
import java.io.Writer;
import java.net.URL;
import java.nio.ByteOrder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.StringTokenizer;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;

import edu.mines.jtk.dsp.Sampling;
import edu.mines.jtk.io.ArrayFile;
import edu.mines.jtk.mosaic.PlotFrame;
import edu.mines.jtk.mosaic.PlotPanel;
import edu.mines.jtk.mosaic.PointsView;
import edu.mines.jtk.mosaic.SequenceView;
import edu.mines.jtk.util.ArrayMath;

public class SeisPTUtil {


	//20 very useful Java code snippets for Java Developers 
	//http://viralpatel.net/blogs/20-useful-java-code-snippets-for-java-developers/
	
	//http://snipplr.com/all/language/java
	
	
	public static double 		_INVALID_D 	= -99999.0;

	// This routine calculates the amplitude due to a moment tensor source 
	// for a uniform constant velocity model.
	// output is three amplitude (Amp_P, Amp_Sv, Amp_Sh)
	public static double [] genAmplitude1(double r, double [][] theCos, double [][] MT, 
			double vp, double vs, double den, double qpFactor, double qsFactor) {
		double [] q 	= new double[] {qpFactor, qsFactor, qsFactor};
		double [] v 	= new double[] {vp, vs, vs}; 
		double c 		= 4.0*Math.PI*r*den;

		//calc amplitude for each Mij
		double [] amp = new double [3];  //amplitude of {p, sv, sh}
		double [] A = new double [3];

		double [] pCos = theCos[0];
		double [] svCos = theCos[1];
		double [] shCos = theCos[2];

		//A = MT(3x3)*pCos'(3x1)
		double sum = 0.0;
		for(int ii=0; ii<3; ii++) {
			sum = 0.0;
			for(int jj=0; jj<3; jj++) {
				sum += MT[ii][jj]*pCos[jj];
			}
			A[ii] = sum;
		}

		amp[0] = pCos[0]*A[0]+pCos[1]*A[1]+pCos[2]*A[2]; 	// =P*MT*P ampP
		amp[1] = svCos[0]*A[0]+svCos[1]*A[1]+svCos[2]*A[2]; // =SV*MT*P ampSV
		amp[2] = shCos[0]*A[0]+shCos[1]*A[1]+shCos[2]*A[2]; // =SH*MT*P ampSH
		//		sum = 0.0;
		//		for(int ii=0; ii<3; ii++) {
		//			sum = 0.0;
		//			for(int jj=0; jj<3; jj++) {
		//				sum += MT[ii][jj]*svCos[jj];
		//			}
		//			A[ii] = sum;
		//		}
		//		amp[1] = svCos[0]*A[0]+svCos[1]*A[1]+svCos[2]*A[2]; // =SV*MT*P ampSV
		//		sum = 0.0;
		//		for(int ii=0; ii<3; ii++) {
		//			sum = 0.0;
		//			for(int jj=0; jj<3; jj++) {
		//				sum += MT[ii][jj]*shCos[jj];
		//			}
		//			A[ii] = sum;
		//		}
		//		amp[2] = shCos[0]*A[0]+shCos[1]*A[1]+shCos[2]*A[2]; // =SH*MT*P ampSH	

		//all corrections
		for(int k=0; k<3; k++) {
			amp[k] *= q[k]/(c*v[k]*v[k]*v[k]);
		}
		return amp;
	}

	public static void printArray(double[][] array) { 
		printArray(array.length, array[0].length, 1, 1, array); 
	}
	public static void printArray(int rSteps, int cSteps, double[][] array) { 
		printArray(array.length, array[0].length, rSteps, cSteps, array); 
	}
	public static void printArray(int nRow, int nCol, int rSteps, int cSteps, double [][] array) {
		System.out.println("nRow="+array.length+" nCol="+array[0].length);
		for(int i=0; i<nRow; i+=rSteps) {
			for(int j=0; j<nCol; j+=cSteps) {
				System.out.print(array[i][j] + " ");
			}
			System.out.println();
		}
		System.out.println();
	}

	// This function converts the six elem array (6 by 1) in NEUp system into
	//  moment tensor(3 by 3) in ENUp cooridnate system. 

	// (Mxx, Myy, Mzz, Mxy, Myz, Mxz) in NEU into
	//  moment tensor(3 by 3) in NEUp cooridnate system. 
	// MomentTensorInNEUp (Mxx, Myy, Mzz, Mxy, Myz, Mxz)
	// page 3 of Norm 2010 SPE paper
	public static double [][] genMT( double [] base) {

		double [][] convA = {{0, 0, 0, -1, 0, 1},
				{0, 0, 0, 0, -1, 1}, 
				{0, 0, 0, 1, 1, 1}, 
				{1, 0, 0, 0, 0, 0}, 
				{0, 0, -1, 0, 0, 0}, 
				{0, 1, 0, 0, 0, 0}};
		double [] A = new double[6];
		for(int i=0; i<6; i++) {
			double a = 0.0;
			for(int j=0; j<6; j++)  a += convA[i][j]*base[j]; 
			A[i] = a;
		}

		double [][] NEU = new double [][] {{A[0], A[3], A[5]},
				{A[3], A[1], A[4]}, 
				{A[5], A[4], A[2]}};
		//convert from NEUp to ENUp
		double [][] convB = {{0, 1, 0},
				{1, 0, 0}, 
				{0, 0, 1}};

		double [][] B = new double[3][3];
		for(int i=0; i<3; i++) {
			for(int j=0; j<3; j++)  {
				double a = 0.0;
				for(int k=0; k<3; k++) a += convB[i][k]*NEU[k][j]; 
				B[i][j] = a;
			}
		}
		double [][] ENU = new double[3][3];
		for(int i=0; i<3; i++) {
			for(int j=0; j<3; j++)  {
				double a = 0.0;
				for(int k=0; k<3; k++) a += B[i][k]*convB[k][j]; 
				ENU[i][j] = a;
			}
		}

		return ENU;
	}

	// This routine calculates the unit vectors from a source and a sensor.
	// The coordinate system is x=East, y=North z=vertical Up
	// TakeoffAngle (in radian) would be used if given (!=-99999.0). 
	public static double [][] getUnitVector(double srcE, double srcN, double srcU, double recE, double recN, double recU) {
		double [] diff = new double[] {recE-srcE, recN-srcN, recU-srcU};
		double r = 0.0;
		for(int i=0; i<diff.length; i++) {
			r += diff[i]*diff[i];
		}
		r = Math.sqrt(r);

		double [] pCos = new double[] {diff[0]/r, diff[1]/r, diff[2]/r}; // direction cosine of P wave (E, N, U)
		//double theta = Math.acos(pCos[2]); 				// angle between R and Up
		double phi = Math.atan2(pCos[0], pCos[1]); 			// angle = E/N
		//		double theta = takeOffAngle; 						// angle between R and Up
		//		
		//		if(theta!=_INVALID_D) { 
		//			pCos[0] = Math.sin(theta)*Math.sin(phi); //Easting
		//			pCos[1] = Math.sin(theta)*Math.cos(phi); //Northing
		//			pCos[2] = Math.cos(theta);
		//		}

		double [] shCos = new double[] {-Math.cos(phi), Math.sin(phi), 0.0};

		double[] svCos = computeCrossProduct (pCos, shCos);

		double [][] theCos = new double[][] {
				{pCos[0], pCos[1], pCos[2]},
				{svCos[0], svCos[1], svCos[2]},
				{shCos[0], shCos[1], shCos[2]} };
		return theCos;
	}
	public static double computeNorm(double[] u) {
		return Math.sqrt(u[0] * u[0] + u[1] * u[1] + u[2] * u[2]);
	}
	public static double computeDotProduct(double[] u, double[] v) {
		return u[0] * v[0] + u[1] * v[1] + u[2] * v[2];
	}
	public static double[] computeCrossProduct(double[] u, double[] v) {
		double x = u[1] * v[2] - u[2] * v[1];
		double y = u[2] * v[0] - u[0] * v[2];
		double z = u[0] * v[1] - u[1] * v[0];
		return new double[]{x, y, z};
	}

	public static void printSampling(Sampling sampling) {
		int n = sampling.getCount();
		double d = sampling.getDelta();
		double f = sampling.getFirst();
		double v = f;
		System.out.println("n="+n+" d="+d+" f="+f);
		for (int i=0; i<n; ++i,v+=d) {
			System.out.println(" "+v);
		}
	}

	public static double nextPower2(int numOfSamples){
		int exponent = 1;
		double power = Math.pow(2, exponent);
		while(power<numOfSamples){
			exponent++;
			power = Math.pow(2, exponent);
		};
		return power;
	}

	public static float [] padding(float[] a, int nfft) {
		float [] b = new float[nfft];
		for(int i=0; i<nfft; i++) {
			b[i] = 0.0f;
		}
		for(int i=0; i<a.length; i++) {
			b[i] = a[i];
		}
		return b;
	}

	public static void plotPoints(int nx, double dx, double fx, String [] label, float [][] data) {
		PlotPanel panel = new PlotPanel(data.length,1);
		Sampling sx = new Sampling(nx,dx,fx);
		double vmin = ArrayMath.min(data);
		double vmax = ArrayMath.max(data);
		//panel.setLimits(sx.getFirst(),ArrayMath.min(data),sx.getLast(),ArrayMath.max(data));


		for(int i=0; i<data.length; i++) {
			PointsView pv = panel.addPoints(i, 0, sx, data[i]);
			pv.setStyle("r-");
			//pv.setLineStyle(PointsView.Line.NONE);
			pv.setLineWidth(1.0f);
			//pv.setMarkStyle(PointsView.Mark.FILLED_CIRCLE);

			//pv.setTextFormat("%4.2f");
			panel.setVLimits(i, vmin, vmax);
			panel.setVLabel(i, label[i]);
		}


		//panel.setTitle("Amplitude");
		panel.setHLabel("Time(s)");

		//		Font font = new Font("Monospaced", Font.PLAIN, 25);
		//		panel.setFont(font);
		//		Font font = new Font("Verdana", Font.BOLD, 24);
		//		panel.setFont(font);
		//		panel.setBackground(Color.BLUE);

		PlotFrame frame = new PlotFrame(panel);
		frame.setDefaultCloseOperation(PlotFrame.EXIT_ON_CLOSE);
		frame.setSize(1500,500);
		frame.setVisible(true);
	}

	public static void plotSequence(int nx, double dx, double fx, float [][] data) {
		PlotPanel panel = new PlotPanel(data.length,1);

		for(int i=0; i<data.length; i++) {
			Sampling sx = new Sampling(nx,dx,fx);
			SequenceView sv = panel.addSequence(i,0,sx,data[i]);
			if(i==1) sv.setColor(Color.RED);
			else sv.setColor(Color.BLUE);
		}
		//		SequenceView sv1 = panel.addSequence(0,0,sx,f1);
		//		sv1.setColor(Color.RED);
		//
		//		SequenceView sv2 = panel.addSequence(1,0,sx,f2);
		//		sv2.setZero(SequenceView.Zero.MIDDLE);

		PlotFrame frame = new PlotFrame(panel);
		frame.setDefaultCloseOperation(PlotFrame.EXIT_ON_CLOSE);
		frame.setSize(950,500);
		frame.setVisible(true);
		//frame.paintToPng(300,6,"junk.png");
	}

	public static void pointsPlot(float [] ax, float [] ay, String xAxisLabel, String yAxisLabel, double delta, double epsilon){ 
		String aTitle = "Calculated Time - " + yAxisLabel;
		JFrame frame = new JFrame();
		frame.setSize(1000, 500);
		frame.setVisible(true);
//		PointsPlotDialog dialog = new PointsPlotDialog(frame, aTitle, 1, ax, ay, xAxisLabel, yAxisLabel);
//		dialog.showDialog();
	}


	public static boolean isFileExist(String srcPath, String fileExtension) {
		String 	name = null;
		File[] listOfFiles = new File(srcPath).listFiles(); 

		for (int i=0; i<listOfFiles.length; i++) {
			name = listOfFiles[i].getName();
			if (listOfFiles[i].isFile()) {
				if(FilenameUtils.isExtension(name, fileExtension)) {
					return true;
				} else {
				}
			} else {
			}
		}
		return false;
	}
	public static boolean makeDir(String dirPath) {
		File file = new File(dirPath);

		if (!file.exists()) {
			if (file.mkdir()) {
				//System.out.println("Directory is created! "+dirPath); 
				return true;
			}
		}
		return false;
	}
	public static void cleanDirFiles(String dirPath) {
		File[] listOfFiles = new File(dirPath).listFiles();
		
		int k = 0;
		for (int i=0; i<listOfFiles.length; i++) {
			if (listOfFiles[i].isFile()) {
				try {
					FileUtils.forceDelete(listOfFiles[i]);
				} catch (IOException e) {
					e.printStackTrace();
				}
			} 
		}
	}

	//	public static String writeTextFile(double[][] array) {
	//		String b = " ";
	//
	//		for(int i=0; i<array.length; i++) {
	//			String a = Arrays.toString(array[i]); 			b = b.concat(a+"\n");
	//		}
	//		return b;
	//	}
	public static void writeTextFile(String text, String fileName) {
		Writer writer = null;
		try {
			File file = new File(fileName);
			writer = new BufferedWriter(new FileWriter(file));
			writer.write(text);
		} catch (FileNotFoundException e) {e.printStackTrace();
		} catch (IOException e) {e.printStackTrace();
		} finally {
			try {if (writer != null) { writer.close();}
			} catch (IOException e) { e.printStackTrace();}
		}

	}

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

	public static void printArray(String selectedFileName, int nx, int ny, float[] array) {
		printArray(selectedFileName, nx, ny, 1, 0, 0, 0, array);
	}
	public static void printArray(String selectedFileName, int nx, int ny, int xSteps, int ySteps, float[] array) {
		printArray(selectedFileName, nx, ny, 1, xSteps, ySteps, 0, array);
	}
	public static void printArray(String selectedFileName, int nx, int ny, int nz, float[] array) {
		printArray(selectedFileName, nx, ny, nz, 0, 0, 0, array);
	}
	public static void printArray(String selectedFileName, int nx, int ny, int nz, int xSteps, int ySteps, int zSteps, float[] array) {
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

	public static void printArray(String selectedFileName, int nx, int ny, double[] array) {
		printArray(selectedFileName, nx, ny, 1, 1, 1, 1, array);
	}
	public static void printArray(String selectedFileName, int nx, int ny, int xSteps, int ySteps, double[] array) {
		printArray(selectedFileName, nx, ny, 1, xSteps, ySteps, 1, array);
	}
	public static void printArray(String selectedFileName, int nx, int ny, int nz, double[] array) {
		printArray(selectedFileName, nx, ny, nz, 1, 1, 1, array);
	}
	public static void printArray(String selectedFileName, int nx, int ny, int nz, int xSteps, int ySteps, int zSteps, double[] array) {
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

	public static String getCurrentTimeStamp() {
		DateFormat dateFormat1 = new SimpleDateFormat("yyyyMMdd");
		DateFormat dateFormat2 = new SimpleDateFormat("HHmmss");
		DateFormat dateFormat3 = new SimpleDateFormat("SSSSSS");
		Date date = new Date();
		return dateFormat1.format(date)+"t"+dateFormat2.format(date)+"_"+dateFormat3.format(date)+"S";
	}
	
	public static String [] sortFilesByLastModifiedDate(String [] imageFileName, final boolean sortAsc) {
		File [] images = new File[imageFileName.length];
		for(int i=0; i<imageFileName.length; i++) images[i] = new File(imageFileName[i]);
		sortFilesByLastModifiedDate(images, sortAsc);
		
		String [] fileNames = new String[imageFileName.length]; 
		for(int i=0; i<imageFileName.length; i++) {
			try {
				fileNames[i] = images[i].getCanonicalPath();
			} catch (IOException e) { }
		}
		
		return fileNames;
	}
	/**
     * Function sorts files by their lastModified date
     *
     * @param sortAsc sorting Flag [true=ASC|false=DESC]
     */
    public static void sortFilesByLastModifiedDate(File [] images, final boolean sortAsc) {

        Comparator<File> comparator = new Comparator<File>() {
            @Override
            public int compare(File o1, File o2) {
                if (sortAsc) {
                    return Long.valueOf(o1.lastModified()).
                            compareTo(o2.lastModified());
                } else {
                    return -1 * Long.valueOf(o1.lastModified()).
                            compareTo(o2.lastModified());
                }
            }
        };
        Arrays.sort(images, comparator);

//        for (File image : images) {
//            System.out.println(image.getName() + "t" + new Date(image.lastModified()));
//        }
    }
    /**
     * Function sorts files by their name
     *
     * @param sortAsc sorting Flag [true=ASC|false=DESC]
     */
    public static void sortFilesByName(File [] images, final boolean sortAsc) {

        Comparator<File> comparator = new Comparator<File>() {

            @Override
            public int compare(File o1, File o2) {
                if (sortAsc) {
                    return (o1.getName()).compareTo(o2.getName());
                } else {
                    return -1 * (o1.getName()).compareTo(o2.getName());
                }
            }
        };
        Arrays.sort(images, comparator);

//        System.out.println("###n### Sort file by name ######");
//        for (File image : images) {
//            System.out.println(image.getName() + "t" +  new Date(image.lastModified()));
//        }
    }
    /**
     * Function sorts files by their Id on the end of the file name. 
     * (e.q. img_1.png, img_2.png, ...)
     * 
     * @param sortAsc sorting Flag [true=ASC|false=DESC]
     */
    public static void sortFilesByIdName(File [] images, final boolean sortAsc) {

        Comparator<File> comparator = new Comparator<File>() {

            @Override
            public int compare(File o1, File o2) {
                if (sortAsc) {
                    return getFileId(o1).compareTo(getFileId(o2));
                } else {
                    return -1 * getFileId(o1).compareTo(getFileId(o2));
                }
            }
        };
        Arrays.sort(images, comparator);

//        System.out.println("###n### Sort file by Id in file name ######");
//        for (File image : images) {
//            System.out.println(image.getName() + "t" +  new Date(image.lastModified()));
//        }
    }

    /**
     * Helper method to determine file Id for sorting. File name has following
     * structure: img_11.png
     *
     * @param file
     * @return
     */
    private static Integer getFileId(File file) {
        String fileName = first(file.getName().split("."));
        String fileId = last(fileName.split("_"));
        return Integer.parseInt(fileId);
    }

    /**
     * Generic helper methode to get the last field of an array
     *
     * @param <T> Array type
     * @param array Array with elements
     * @return last field of an array
     */
    private static <T> T last(T[] array) {
        return array[array.length - 1];
    }

    /**
     * Generic helper methode to get the first field of an array
     *
     * @param <T> Array type
     * @param array Array with elements
     * @return first field of an array
     */
    private static <T> T first(T[] array) {
        return array[0];
    }

    public static String [] recursivelyListFiles(String fileExtension, String path) {
    	IOFileFilter fileFilter = FileFilterUtils.suffixFileFilter(fileExtension);
		IOFileFilter dirFilter = FileFilterUtils.trueFileFilter();
		System.out.println("start");
		Collection  files = FileUtils.listFiles(new File(path), fileFilter, dirFilter);
		System.out.println("end");
		String [] filenames = new String[files.size()];

        Iterator i = files.iterator();
        int k = 0;
        while (i.hasNext()) {
        	//String name = ((File)i.next()).getName();
        	try {
        		filenames[k] = ((File)i.next()).getCanonicalPath();
			} catch (IOException e) {
				e.printStackTrace();
			}
        	
            System.out.println(filenames[k]);
            k++;
        }
        return filenames;
	}
    
	public static String [] listFiles(int id, String [] fileExtensions, String path) {
		return listFiles(id, null, fileExtensions, path);
	}
	public static String [] listFiles(int id, String containString, String [] fileExtensions, String path) {
		return listFiles(id, null, containString, fileExtensions, path);
	}
	
	public static String [] listFiles(int id, String startWithString, String containString, String [] fileExtensions, String path) {
		ArrayList<String> nameList = new ArrayList<String>();
		String names = null;
		File folder = new File(path);
		File[] listOfFiles = folder.listFiles(); 

		for (int i = 0; i < listOfFiles.length; i++) {
			if (listOfFiles[i].isFile()) {
				if(id==1) {
					names = listOfFiles[i].getName();
				} else {
					try {
						names = listOfFiles[i].getCanonicalPath();
					} catch (IOException e) { }
				}

				boolean endWith = false;
				for(int j=0; j<fileExtensions.length; j++) {
					if (names.endsWith(fileExtensions[j])) { endWith = true; break; }
				}
				if (endWith) {
					if(containString!=null) {
						if(names.contains(containString)) {
							nameList.add(names);
						}
					} else {
						nameList.add(names);
					}
				}
			}
		}
		if(nameList.size()==0) return null;
		String [] fileNames = new String[nameList.size()];
		for(int i=0; i<fileNames.length; i++) fileNames[i] = nameList.get(i);
		
		if(startWithString==null) return fileNames;
		
		int k = 0;
		for(int i=0; i<fileNames.length; i++) {
			String localName = FilenameUtils.getName(fileNames[i]);
			if(localName.startsWith(startWithString)) k++;
		}
		String [] fileNames1 = new String[k];
		k = 0;
		for(int i=0; i<fileNames.length; i++) {
			String localName = FilenameUtils.getName(fileNames[i]);
			if(localName.startsWith(startWithString)) {
				fileNames1[k] = fileNames[i];
				k++;
			}
		}
		//return fileNames1;
		return sortFilesByLastModifiedDate(fileNames1, true);
	}

	public static long getFileFolderSize(File dir) {
		long size = 0;
		if (dir.isDirectory()) {
			for (File file : dir.listFiles()) {
				if (file.isFile()) {
					size += file.length();
				} else
					size += getFileFolderSize(file);
			}
		} else if (dir.isFile()) {
			size += dir.length();
		}
		return size;
	}

	public static long getNumOfFile(File dir) {
		long size = 0;
		if (dir.isDirectory()) {
			for (File file : dir.listFiles()) {
				if (file.isFile()) {
					size ++;
				} else
					size += getNumOfFile(file);
			}
		} else if (dir.isFile()) {
			size ++;
		}
		return size;
	}

	public static String makeDir(String root, String subDir) {
		String dir = root+File.separator+subDir;
		File file = new File(dir);

		if (!file.exists()) {
			if (file.mkdir()) {
				//System.out.println("Directory is created! "+dir); 
			}
		}
		return dir;
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
	public static float dotProduct(float v1[], float v2[]) {
		return v1[0]*v2[0] + v1[1]*v2[1] + v1[2]*v2[2];
	}
	public static void crossProduct(float v1[], float v2[], float vR[]) {
		vR[0] =   ( (v1[1] * v2[2]) - (v1[2] * v2[1]) );
		vR[1] = - ( (v1[0] * v2[2]) - (v1[2] * v2[0]) );
		vR[2] =   ( (v1[0] * v2[1]) - (v1[1] * v2[0]) );
	}

	public static void normalize(float v1[], float vR[]) {
		float fMag = (float) Math.sqrt(v1[0]*v1[0]+v1[1]*v1[1]+v1[2]*v1[2]); 

		vR[0] = v1[0] / fMag;
		vR[1] = v1[1] / fMag;
		vR[2] = v1[2] / fMag;
	}
	public static double norm(double v1[]) {
		return Math.sqrt(v1[0]*v1[0]+v1[1]*v1[1]+v1[2]*v1[2]); 
	}
	public static double dotProduct(double v1[], double v2[]) {
		return v1[0]*v2[0] + v1[1]*v2[1] + v1[2]*v2[2];
	}
	public static void crossProduct(double v1[], double v2[], double vR[]) {
		vR[0] =   ( (v1[1] * v2[2]) - (v1[2] * v2[1]) );
		vR[1] = - ( (v1[0] * v2[2]) - (v1[2] * v2[0]) );
		vR[2] =   ( (v1[0] * v2[1]) - (v1[1] * v2[0]) );
	}

	public static void normalize(double v1[], double vR[]) {
		double fMag = Math.sqrt(v1[0]*v1[0]+v1[1]*v1[1]+v1[2]*v1[2]); 

		vR[0] = v1[0] / fMag;
		vR[1] = v1[1] / fMag;
		vR[2] = v1[2] / fMag;
	}
	
	// pad with " " to the right to the given length (n)
	  public static String padRight(String s, int n) {
	    return String.format("%1$-" + n + "s", s);
	  }

	  // pad with " " to the left to the given length (n)
	  public static String padLeft(String s, int n) {
	    return String.format("%1$" + n + "s", s);
	  }
	  //System.out.println(String.format("%10s", "howto").replace(' ', '*'));
	  //System.out.println(String.format("%-10s", "howto").replace(' ', '*'));
	
	public static void simplePlot(double [] ax, double [] ay, String xAxisLabel, String yAxisLabel) { 
		if(ax==null || ay==null) {
			String message = "Data are not available!";
			String title = "Alert";
			JOptionPane.showMessageDialog(null, message, title, JOptionPane.ERROR_MESSAGE);
			return;
		}
		
		//Create and set up the window.
		JFrame frame = new JFrame("Data Viewer");
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

		//Create and set up the content pane.
//		Seg2Tree newContentPane = new Seg2Tree(frame, srcFileName, dataPath);
//        newContentPane.setOpaque(true); //content panes must be opaque
//        frame.setContentPane(newContentPane);

		//Display the window.
		frame.pack();
		frame.setVisible(true);
		
//		XyPlot dialog = new XyPlot(frame, "Data Viewer", false, "r-o", 10.0f, ax, ay, 
//				xAxisLabel, yAxisLabel, "Points", "Data");
//		dialog.showDialog();
		
	}
}
