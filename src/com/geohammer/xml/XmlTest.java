package com.geohammer.xml;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.SwingUtilities;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.ucdm.core.MatrixFileReader;
import org.ucdm.core.SeisPTSrcMech;
import org.ucdm.core.SeisPTVel;
import org.ucdm.core.acquisition.VCPair;
import org.ucdm.core.planarmodel.DipLayer1D;
import org.ucdm.core.planarmodel.Layer2D;
import org.ucdm.xml.mdl.MdlXml;
import org.ucdm.xml.msx.MsxXml;
import org.ucdm.xml.tpt.TptXml;


public class XmlTest implements Runnable {
	public XmlTest() {

	}
	public void run() {	
		//mdlTest3();
		//csvToXml();
		//test3();
		//tptTest1();
		//msxTest();
		parseEmptyElement();
		
		System.out.println("Successfully Done!");
	}
	public static void main(String[] args) {
		System.out.println("Executing SeisPTTest()");
		SwingUtilities.invokeLater( new XmlTest() );
	}

	public void mdlTest1()  {
		String fileNamePar = "C:\\PINN_DATA\\Seispt-mti-test\\v1.par.xml";
		String fileName = "C:\\PINN_DATA\\Seispt-mti-test\\mdlVTI.csv";
		DipLayer1D dipLayer1D = new DipLayer1D(fileName);
		//System.out.println(dipLayer1D.toString());
		MdlXml mdlXml = new MdlXml(3, dipLayer1D);
		//MdlXml mdlXml = new MdlXml(fileNamePar);
		System.out.println(mdlXml.toString());
		String selectedFileName = "C:\\PINN_DATA\\Seispt-mti-test\\v3.par.xml";
		mdlXml.write(selectedFileName);
	}
	public void mdlTest2()  {
		String fileNameMdl = "C:\\PINN_DATA\\aramis\\EOG\\Stage06\\MSUC\\c.mdl.xml";
		//String fileNameMdl = "C:\\PINN_DATA\\Marathon\\MMC Starting Model.mdl.xml";
		MdlXml mdlXml = new MdlXml(fileNameMdl);
		//System.out.println(mdlXml.toString());
		Layer2D layer2D = mdlXml.toDipLayer1D().toLayer2D();
		System.out.println(layer2D.toString(3));

		//String selectedFileName = "C:\\PINN_DATA\\aramis\\EOG\\Stage06\\MSUC\\junk1.mdl.xml";
		//mdlXml.write(selectedFileName);
	}

	public void mdlTest3()  {
		String fileNameMdl = "C:\\PINN_DATA\\Seispt-mti-test\\seisptvel.txt";
		String fileNamePck = "C:\\PINN_DATA\\Seispt-mti-test\\srcmech.txt";
		SeisPTVel seisptVel = new SeisPTVel(fileNameMdl, 1);
		SeisPTSrcMech seisptSrcMech = new SeisPTSrcMech(true, fileNamePck, null);
		VCPair vcPW = seisptSrcMech.toVCPair(0);
		DipLayer1D dlayer1D = seisptVel.toDipLayer1D(vcPW);
		MdlXml  mdlXml = new MdlXml(3, dlayer1D);
		mdlXml.write("C:\\PINN_DATA\\Seispt-mti-test\\test5.par.xml");
	}

	public void tptTest1()  {
		String fileName = "C:\\PINN_DATA\\ARC\\Parkland\\tilt\\Arc_Parkland-A5-9-81-FINAL.tpt";
		TptXml  tptXml = new TptXml(fileName);
		tptXml.printClass();
		
		//tptXml.write("C:\\PINN_DATA\\Seispt-mti-test\\test5.par.xml");
	}
	
	public void csvToXml()  {
		String root = "C:\\prowess\\ucdm\\configure\\";
		//CsvXml csvXml = new CsvXml(root+"data.csv");
		CsvXml csvXml = new CsvXml(root+"databaseCore.xml");
		//reader.printClass();

		String fileName = root+"databaseCore1.xml";
		csvXml.write(fileName);
		
	}
	public void test3() {
		//String fileName = new SimpleDateFormat("yyyyMMddhhmmss'.txt'").format(new Date());
//		String fileName = new SimpleDateFormat("yyyyMMddhhmm").format(new Date());
//		String userName = System.getProperty("user.name");
//		System.out.println(fileName + " "+ userName);
		
		String a = "C:\\PINN_DATA\\EOG\\university\\stage9\\stage   9.mdl.xml";
		//String a = "/home/docs/status.txt";
		Path path = FileSystems.getDefault().getPath(a);
		System.out.println(path.toString());
		
		File file = new File(a);
		URL url=null;
		try{
			System.out.println(System.getProperty("user.name"));
			//The file may or may not exist
			url=file.toURI().toURL(); //file:/C:/work/chandan/deepak.txt
			System.out.println("The url is" + url);

			// change the URL to a file object
			file=new File(url.getFile());  // c:/work/chandan/deepak.txt
			System.out.println("The file name is " + file);

		} catch (MalformedURLException e){
			System.out.println("Don't worry,exception has been caught" + e);
		}
		catch (IOException e){
			System.out.println(e.getMessage());
		} 
		
		a = a.replaceAll("\\\\", "_");
		System.out.println(a);
	}

	public void msxTest()  {
		//String fileName = "C:\\prowess\\matlab\\pvz\\test\\State Mercury 7 No 15H, 17H - Final Results\\"
		//		+ "State Mercury 7 No 17H - Stage 14 Final Frac.msx";
		String fileName = "C:\\prowess\\matlab\\pvz\\test\\State Mercury 7 No 15H, 17H - Final Results - Perfs\\"
				+ "State Mercury 7 No 15H - Stage 16 Final Perfs.msx";
		//String fileNameMdl = "C:\\PINN_DATA\\Marathon\\MMC Starting Model.mdl.xml";
		MsxXml msxXml = new MsxXml(fileName);
		msxXml.printClass();
		//System.out.println(mdlXml.toString());

	}
	
	public void parseEmptyElement()  {
		String fileName = "C:\\PINN_DATA\\BlackHills\\emptyTag.xml";
		TptXml  tptXml = new TptXml(fileName);
		tptXml.printClass();
		
		//tptXml.write("C:\\PINN_DATA\\Seispt-mti-test\\test5.par.xml");
	}
}


