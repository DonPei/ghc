package com.geohammer.license;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.util.*;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;

public class VcLicense extends JDialog  implements ActionListener{
	String _currTime 	= null;
	String _hostName 	= null;
	String _userName	= null;
	String _ipAddress 	= null;
	String _macAddress 	= null;
	String _locale 		= null;
	String _country 	= null;
	Calendar _calendar 	= null;
	String _serverFile 	= null;

	public VcLicense() {
		String plainString = generateLicenseFile(1);
		System.out.println(plainString);
		System.out.println(encoding (plainString));
		System.out.println(decoding (encoding (plainString)));
	}

	private String getLocalhostIpAddress() {
		String myIp = "UNKNOWN";
		try {
			myIp = InetAddress.getLocalHost().getHostAddress();
//			String hostName = InetAddress.getLocalHost().getHostName();
//			InetAddress addrs[] = InetAddress.getAllByName(hostName);
//			for (InetAddress addr: addrs) {
////				System.out.println ("addr.getHostAddress() = " + addr.getHostAddress());
////				System.out.println ("addr.getHostName() = " + addr.getHostName());
////				System.out.println ("addr.isAnyLocalAddress() = " + addr.isAnyLocalAddress());
////				System.out.println ("addr.isLinkLocalAddress() = " + addr.isLinkLocalAddress());
////				System.out.println ("addr.isLoopbackAddress() = " + addr.isLoopbackAddress());
////				System.out.println ("addr.isMulticastAddress() = " + addr.isMulticastAddress());
////				System.out.println ("addr.isSiteLocalAddress() = " + addr.isSiteLocalAddress());
////				System.out.println ("");
//
//				if (!addr.isLoopbackAddress() && addr.isSiteLocalAddress()) {
//					myIp = addr.getHostAddress();
//				}
//			}
//			//System.out.println ("\nIP = " + myIp);
		} catch (UnknownHostException e) {
			//System.out.println (e.toString());
		}
		return myIp;
	}
	
	public String getLocalhostMacAddress() {
		String macAddress = "UNKNOWN";
		try { 
			InetAddress ip = InetAddress.getLocalHost();
			//System.out.println("Current IP address : " + ip.getHostAddress());
	 
			NetworkInterface network = NetworkInterface.getByInetAddress(ip);
			byte[] mac = network.getHardwareAddress();
			//System.out.print("Current MAC address : ");
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < mac.length; i++) {
				sb.append(String.format("%02X%s", mac[i], (i < mac.length - 1) ? "-" : ""));		
			}
			macAddress = sb.toString();
			//System.out.println(sb.toString());
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (SocketException e){
			e.printStackTrace();
		}

		return macAddress;
	}
	private String generateLicenseNo() { 
		Calendar calendar 	= Calendar.getInstance();
		return String.valueOf(calendar.getTime().getTime());
	}
	public String generateLicenseFile() { 
		String plainText = generateLicenseFile( 1 );
		return encoding (plainText);
	}
	public String generateLicenseFile( int id )	{
		Calendar calendar 	= Calendar.getInstance();
		String licenseNo 	= generateLicenseNo();
		int year 			= calendar.get(Calendar.YEAR);
		int month 			= calendar.get(Calendar.MONTH);
		int day 			= calendar.get(Calendar.DAY_OF_MONTH);
		  
		Locale locale1 		= Locale.getDefault();
		String locale 		= locale1.toString();
		String country 		= locale1.getCountry().toString();
		String ipAddress 	= getLocalhostIpAddress();
		String macAddress 	= getLocalhostMacAddress();
		
		String userName		=  System.getProperty( "user.name" );
		String hostName 	= null;
		
		try {
			hostName = InetAddress.getLocalHost().getHostName();
		} catch(UnknownHostException e) { }
		
		if(id==1) {
			return String.format("%s, %d, %d, %d, %s, %s, %s, %s, %s", 
					licenseNo, year, month, day, country, locale, ipAddress, macAddress, hostName);
		} else if(id==2){
			return String.format("%s, %s, %s", ""+ licenseNo, hostName, userName);	
		} else if(id==3){
			return String.format("%s, %s",  hostName, userName);	
		} else { }
		return null;
	}
	
	private int daysBetween2Dates() {
		String line = readLicenseFile("C:\\temp\\junk.dat");
		StringTokenizer st = new StringTokenizer(line, ",");
		long a = Long.valueOf(st.nextToken().trim().toString());
		long b = _calendar.getTime().getTime();
		int days = (int)((b-a)/(24.0*3600.0*1000.0));
		
//		System.out.println("3. No of Days between 2 dates\n");
//		Calendar c1 = Calendar.getInstance(); //new GregorianCalendar();
//		Calendar c2 = Calendar.getInstance(); //new GregorianCalendar();
//		c1.set(1999, 0, 20);
//		c2.set(1999, 0, 22);
//		System.out.println("Days Between " + c1.getTime() + " and " + c2.getTime() + " is");
//		System.out.println((c2.getTime().getTime() - c1.getTime().getTime())	/ (24 * 3600 * 1000));
//		System.out.println("\n-------------------------------------");
//	
		return days;
	}
	
	private void resize() {
		Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize();
		int w = dimension.width / 2;
		int h = dimension.height / 2;
		setBounds(w/2, h/2, 500, 130);
	}

	public void actionPerformed(ActionEvent actionevent) {
		JButton jbutton = (JButton)actionevent.getSource();
		String commandID = jbutton.getActionCommand();

		if(commandID == "Save") { 
		} else if(commandID == "Save As") { 
			saveAs(0, encoding(generateLicenseFile(2)));
			System.exit(0);
		} else if(commandID == "Close") { 
			//System.out.println(readLicenseFile("C:\\temp\\junk.dat"));
			//appendUserInfo();
			//saveAs(0, generateLicenseFile(1));
			System.exit(0);
			//setVisible( false );
			//dispose();
		} else {}
	}
	
	private void saveAs( int id, String text )	{
		if(!checkLicense()) {
			String errorMsg = new String("You already downloaded the license file more than once!" + 
					"\n To get a new license file, contact support@geohammer.com ");
			JOptionPane.showMessageDialog(null, errorMsg , "Error", JOptionPane.ERROR_MESSAGE);
			return;
		}

		JFrame frame = new JFrame();
		JFileChooser chooser = new JFileChooser( );	
		chooser.addChoosableFileFilter( new FileNameExtensionFilter("(*.dat)", "license data file") );
		chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		chooser.setDialogTitle("Select File for Writing");

		int returnVal = chooser.showSaveDialog( frame );
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File file = chooser.getSelectedFile();
			if (file.exists()) {  // Ask the user whether to replace the file.
				int response = JOptionPane.showConfirmDialog( this,
						"The file \"" + file.getName()
						+ "\" already exists.\nDo you want to replace it?", 
						"Confirm Save",
						JOptionPane.YES_NO_OPTION, 
						JOptionPane.WARNING_MESSAGE );
				if (response != JOptionPane.YES_OPTION)
					return;  // User does not want to replace the file.
			}
			String loc = file.getAbsolutePath();
			String loc1 = file.getAbsolutePath();		
			String extension = getFileExtension(file);	
			if (extension==null) {
				loc1 = new String(loc+".dat");	
			} else if (!extension.equalsIgnoreCase("dat")) {
				loc1 = changeExtension(loc, ".dat");
			} else { }
			
			_serverFile = file.getParent()+File.separator + "vcLicenseLog.txt";
			appendUserInfo(_serverFile);
			File f = new File(_serverFile);
			try{
				uploadFile("shake.seismo.unr.edu", "donghong", "unr1225", "public_html/vcLicenseLog.txt", f);
				f.delete();
			} catch (IOException e) {
				f.delete();
				return;
			}
			saveTextToFile(text, loc1);
		}
	}	
	
	private void saveTextToFile( String text, String selectedFile ) {	
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

	private String changeExtension(String originalName, String newExtension) {
		int lastDot = originalName.lastIndexOf(".");
		if (lastDot != -1) {
			return originalName.substring(0, lastDot) + newExtension;
		} else {
			return originalName + newExtension;
		}
	}

	private String getFileExtension(File f) {
		String ext = null;
		String s = f.getName();
		int i = s.lastIndexOf('.');

		if (i > 0 &&  i < s.length() - 1) {
			ext = s.substring(i+1).toLowerCase();
		}
		return ext;
	}

	private String encoding (String plainText) { 
		return Base64Coder.encodeString(plainText);
	}

	private String decoding (String base64Text) {
		return Base64Coder.decodeString(base64Text);
	}

	private void decodeFile (String inputFileName, String outputFileName) throws IOException {
		BufferedReader in = null;
		BufferedOutputStream out = null;
		try {
			in = new BufferedReader(new FileReader(inputFileName));
			out = new BufferedOutputStream(new FileOutputStream(outputFileName));
			decodeStream (in, out);
			out.flush(); 
		} finally {
			if (in != null) in.close();
			if (out != null) out.close(); 
		}
	}

	private void decodeStream (BufferedReader in, OutputStream out) throws IOException {
		while (true) {
			String s = in.readLine();
			if (s == null) break;
			byte[] buf = Base64Coder.decodeLines(s);
			out.write (buf); }
	}

	private void encodeFile (String inputFileName, String outputFileName) throws IOException {
		BufferedInputStream in = null;
		BufferedWriter out = null;
		try {
			in = new BufferedInputStream(new FileInputStream(inputFileName));
			out = new BufferedWriter(new FileWriter(outputFileName));
			encodeStream (in, out);
			out.flush(); 
		}	finally {
			if (in != null) in.close();
			if (out != null) out.close(); 
		}
	}

	private void encodeStream (InputStream in, BufferedWriter out) throws IOException {
		int lineLength = 72;
		byte[] buf = new byte[lineLength/4*3];
		while (true) {
			int len = in.read(buf);
			if (len <= 0) break;
			out.write (Base64Coder.encode(buf, 0, len));
			out.newLine(); }
	}
	private void appendUserInfo(String fileName) {
		ArrayList<String> records = readUserRecords(1);
		records.add(generateLicenseFile(1));
		
		try {
			BufferedWriter out = new BufferedWriter(new FileWriter(fileName));

//			URL url = new URL("http://crack.seismo.unr.edu/~donghong/log.txt");
//			URLConnection connection = url.openConnection();
//			connection.setDoOutput(true);
//			Writer out = new BufferedWriter(new OutputStreamWriter(connection.getOutputStream()));
			for(String a: records) {
				out.write(a); out.newLine(); 
			}
			out.flush();
			out.close();
		} catch(Exception e) { 
			String errorMsg = new String("Append error!\n"+e.toString());
			JOptionPane.showMessageDialog(new JFrame(), errorMsg , "Error", JOptionPane.ERROR_MESSAGE);
			//System.out.println(e.toString());
		}
	}
	private boolean checkLicense() {
		int nTrials = getUserHits();
		if(nTrials>1) return false;
		else return true;
	}
	private int getUserHits() {
		int nTrials = 0;
		ArrayList<String> records = readUserRecords(3);
		if(records==null) {
			return nTrials;
		}
		String ls = generateLicenseFile(3);
		for(String a: records) {
			if(ls.equalsIgnoreCase(a)) nTrials++;
		}
		return nTrials;
	}
	private String readLicenseFile(String licenseFileName) {
		String line = null;
		try{
			BufferedReader reader = new BufferedReader(new FileReader(licenseFileName));
			line=reader.readLine();
			reader.close();
		} catch (IOException e) {}
		
		if(line==null) return null;
		else return decoding(line);
	}
	private ArrayList<String> readUserRecords(int id) {
		ArrayList<String> records = new ArrayList<String>();
		try{
			//BufferedReader reader = new BufferedReader(new FileReader("C:\\temp\\junk3.dat"));
			URL oracle = new URL("http://crack.seismo.unr.edu/~donghong/vcLicenseLog.txt");
			URLConnection yc = oracle.openConnection();
			yc.setDoInput(true);
			BufferedReader reader = new BufferedReader(new InputStreamReader(yc.getInputStream()));
			
			String line;
			while ((line=reader.readLine()) != null) {
				StringTokenizer st = new StringTokenizer(line, ",");
				String a = st.nextToken().trim().toString();
				String b = st.nextToken().trim().toString();
				String c = st.nextToken().trim().toString();
				String d = st.nextToken().trim().toString();
				String e = st.nextToken().trim().toString();
				String f = st.nextToken().trim().toString();
				String g = st.nextToken().trim().toString();
				
				String sa = null;
				if(id==1) {
					sa = String.format("%s, %s, %s, %s, %s, %s, %s", a, b, c, d, e, f, g);
					//System.out.println(sa);
				} else if(id==2) {
					sa = String.format("%s, %s, %s", b, c, d);
					//System.out.println(sa);
				} else if(id==3) {
					sa = String.format("%s, %s", c, d);
					//System.out.println(sa);
				} else {}
				records.add(sa);
			}
			reader.close();
			return records;
		} catch (IOException e) {
			String errorMsg = new String("Connection error!" + e.toString());
			JOptionPane.showMessageDialog(new JFrame(), errorMsg , "Error", JOptionPane.ERROR_MESSAGE);
			return null;
		}
	}

	/**
	 * Upload a file to a FTP server. A FTP URL is generated with the
	 * following syntax:
	 * ftp://user:password@host:port/filePath;type=i.
	 * 
	 * @param ftpServer , FTP server address (optional port ':portNumber').
	 * @param user , Optional user name to login.
	 * @param password , Optional password for user.
	 * @param fileName , Destination file name on FTP server (with optional
	 *            preceding relative path, e.g. "myDir/myFile.txt").
	 * @param source , Source file to upload.
	 * @throws MalformedURLException, IOException on error.
	 */
	public void uploadFile( String ftpServer, String user, String password,
			String fileName, File source ) throws MalformedURLException, IOException {
		if (ftpServer != null && fileName != null && source != null)
		{
			StringBuffer sb = new StringBuffer( "ftp://" );
			// check for authentication else assume its anonymous access.
			if (user != null && password != null) {
				sb.append( user );
				sb.append( ':' );
				sb.append( password );
				sb.append( '@' );
			}
			sb.append( ftpServer );
			sb.append( '/' );
			sb.append( fileName );
			/*
			 * type ==> a=ASCII mode, i=image (binary) mode, d= file directory
			 * listing
			 */
			sb.append( ";type=i" );

			BufferedInputStream bis = null;
			BufferedOutputStream bos = null;
			try {
				URL url = new URL( sb.toString() );
				URLConnection urlc = url.openConnection();

				bos = new BufferedOutputStream( urlc.getOutputStream() );
				bis = new BufferedInputStream( new FileInputStream( source ) );

				int i;
				// read byte by byte until end of stream
				while ((i = bis.read()) != -1) {
					bos.write( i );
				}
			} finally {
				if (bis != null) {
					try {
						bis.close();
					} catch (IOException ioe) {
						ioe.printStackTrace();
					}
				}
				if (bos != null){
					try {
						bos.close();
					} catch (IOException ioe) {
						ioe.printStackTrace();
					}
				}
			}
		} else {
			System.out.println( "Input not available." );
		}
	}

	/**
	 * Download a file from a FTP server. A FTP URL is generated with the
	 * following syntax:
	 * ftp://user:password@host:port/filePath;type=i.
	 * 
	 * @param ftpServer , FTP server address (optional port ':portNumber').
	 * @param user , Optional user name to login.
	 * @param password , Optional password for user.
	 * @param fileName , Name of file to download (with optional preceeding
	 *            relative path, e.g. one/two/three.txt).
	 * @param destination , Destination file to save.
	 * @throws MalformedURLException, IOException on error.
	 */
	public void downloadFile( String ftpServer, String user, String password,
			String fileName, File destination ) throws MalformedURLException, IOException {
		if (ftpServer != null && fileName != null && destination != null) {
			StringBuffer sb = new StringBuffer( "ftp://" );
			// check for authentication else assume its anonymous access.
			if (user != null && password != null) {
				sb.append( user );
				sb.append( ':' );
				sb.append( password );
				sb.append( '@' );
			}
			sb.append( ftpServer );
			sb.append( '/' );
			sb.append( fileName );
			/*
			 * type ==> a=ASCII mode, i=image (binary) mode, d= file directory
			 * listing
			 */
			sb.append( ";type=i" );
			BufferedInputStream bis = null;
			BufferedOutputStream bos = null;
			try {
				URL url = new URL( sb.toString() );
				URLConnection urlc = url.openConnection();

				bis = new BufferedInputStream( urlc.getInputStream() );
				bos = new BufferedOutputStream( new FileOutputStream(destination.getName() ) );

				int i;
				while ((i = bis.read()) != -1) {
					bos.write( i );
				}
			} finally {
				if (bis != null) {
					try {
						bis.close();
					} catch (IOException ioe) {
						ioe.printStackTrace();
					}
				}
				if (bos != null) {
					try {
						bos.close();
					} catch (IOException ioe) {
						ioe.printStackTrace();
					}
				}
			}
		}
		else {
			System.out.println( "Input not available" );
		}
	}

//	public static void main(String s[]) {
//		JFrame frame = new JFrame("License");
//		frame.addWindowListener(new WindowAdapter() {
//			public void windowClosing(WindowEvent e) {
//				System.exit(0);
//			}
//		});
//
//		frame.setTitle("Save your license file");
//		frame.setUndecorated(true);
//		frame.setResizable(false);
//		frame.getRootPane().setWindowDecorationStyle(JRootPane.PLAIN_DIALOG);
//		frame.setLocationRelativeTo(null);
//		frame.setDefaultCloseOperation(EXIT_ON_CLOSE);
//		
//		VcLicense license = new VcLicense();
//		LicenseGeneratorDialog dialog = new LicenseGeneratorDialog(frame, license.generateLicenseFile());
//		
//		frame.add(dialog);
//		frame.setVisible(true);
//	}

}
