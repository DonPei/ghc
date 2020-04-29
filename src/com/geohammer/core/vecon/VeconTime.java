package com.geohammer.core.vecon;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import javax.swing.JOptionPane;

public class VeconTime extends VeconModel {

	
	public VeconTime() {
	}
	
	public void writeAsVtkText(String label, String selectedFileName) {
		writeAsVtkText("Vencon3D", label, selectedFileName);
	}
	
	public void writeAsVtkText(String idString, String label, String selectedFileName){	
		try{
			BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(selectedFileName, false));
			bufferedWriter.write("# vtk DataFile Version 3.0\n");
			bufferedWriter.write("vtk "+idString+"\n");
			bufferedWriter.write("ASCII\n\n");
			bufferedWriter.write("DATASET STRUCTURED_POINTS\n");
			bufferedWriter.write("DIMENSIONS "+getNx()+" "+getNy()+" "+getNz()+"\n");
			bufferedWriter.write("ORIGIN "+getX0()+" "+getY0()+" "+getZ0()+"\n");
			bufferedWriter.write("SPACING "+getDx()+" "+getDy()+" "+getDz()+"\n");
			bufferedWriter.write("POINT_DATA "+getNx()*getNy()*getNz()+"\n");
			bufferedWriter.write("SCALARS "+label+" float 1 "+"\n");
			bufferedWriter.write("LOOKUP_TABLE default "+"\n");
			for(int ix=0; ix<getNx(); ix++) {
				for(int iy=0; iy<getNy(); iy++) {
					for(int iz=0; iz<getNz(); iz++) {
						bufferedWriter.write(getData(ix, iy, iz)+"\n");
					}
				}
			}
			bufferedWriter.close();
		} catch (IOException ioexception) {
			JOptionPane.showMessageDialog(null, "\nFile Format is not right!", 
					"Error", JOptionPane.ERROR_MESSAGE);
		}
	}
}
