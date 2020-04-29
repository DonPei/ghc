package com.geohammer.license;

import java.io.*;
import java.net.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.apache.commons.io.FilenameUtils;
import com.geohammer.launcher.LauncherApp;

public class LicenseManager {
	private static LicenseManager instance 	= null;
	public static boolean IS_TRIAL 			= true;
	public static boolean IS_LICENSED 		= false;
	//public static License LICENSE 			= null;
	private static final int ENTROPY 		= 456456456;
	private static final String HEXES 		= "0123456789ABCDEF";

	public static final String LICENSE_FILENAME 	= "license";
	public static final String HASH_FILENAME 		= "license.sha1";
	public static final String SIGNATURE_FILENAME 	= "license.sig";

	private static final int KEY_LEN 			= 62;
	private static final byte[] def 			= new byte[]{24, 4, 124, 10, 91};
	private static final byte[][] params 		= new byte[][]{{24, 4, 127}, {10, 0, 56}, {1, 2, 91}, {7, 1, 100}};
	private static final Set<String> blacklist 	= new TreeSet<String>();

	private Timer t;
	private static final int DELAY = 900000;

	static {
		blacklist.add("11111111");
	}

    private String 		_moduleName 	= null;
    private String 		_version 		= null;
    private String 		_path 			= null;
    public License 		_license 		= null;
    
    public LicenseManager() { }
	public LicenseManager(String path) {
		_path = path;
	}

	public License getLicense() 		{ return _license; }
	public String getPath() 			{ return _path; }
    public String getModuleName() 		{ return _moduleName; }
    public String getVersion() 			{ return _version; }
    
	public void setModuleName(String moduleName) 	{ _moduleName = moduleName; }
    public void setVersion(String version) 			{ _version = version; }
	public void setLicense(License License)			{ _license = License; }
	public void setPath(String fileName)			{ _path = fileName; }
	
//	public void start() {
//        t = new Timer();
//        t.scheduleAtFixedRate(new CheckLicenseTask(), DELAY, DELAY);
//	}
	public static LicenseManager getLicenseManager() {
		if (instance == null) { instance = new LicenseManager(); }
		return instance;
	}
	
	public boolean verifyLicense(JFrame frame) {
		int errorCode = 0;
		//int errorCode = checkLicense();
		
		if(errorCode==-1||errorCode==-4) {
			return true;
//			String msg = "errorCode="+errorCode+ 
//					" - License file (*.lic) does not exist. It should be under license folder.";
//			JOptionPane.showMessageDialog(frame, msg, "Error", JOptionPane.ERROR_MESSAGE);
//			return false;
		} else if(errorCode==-5||errorCode==-6||errorCode==-7) {
			String msg = "errorCode="+errorCode+ 
					" - License file (*.lic) is not right.";
			JOptionPane.showMessageDialog(frame, msg, "Error", JOptionPane.ERROR_MESSAGE);
			return false;
		} else if(errorCode<15) {
			String msg = "errorCode="+errorCode+ 
					" - License would expire in 15 days!";
			JOptionPane.showMessageDialog(frame, msg, "Info", JOptionPane.INFORMATION_MESSAGE);
			return true;
		}
		return true;
		
	}
	public boolean isExpired()  {
		File folder = new File(_path);
		if(!folder.exists()) return false;
		
		File[] listOfFiles = folder.listFiles();
		for (int i = 0; i < listOfFiles.length; i++) {
		//for (int i = 0; i <1; i++) {
			if (listOfFiles[i].isFile()) {
				String fileName = listOfFiles[i].getName();
				//System.out.println(fileName);
				String extension = FilenameUtils.getExtension(fileName);
				if(extension.equalsIgnoreCase("lic")) {
					try {
						fileName = listOfFiles[i].getCanonicalPath();
					} catch (IOException e) {
						e.printStackTrace();
					}
					_license = readLicenseFile(0, fileName);
					//System.out.println(fileName);
					return isExpired(_license);
				}
			}
		}
		
		return true;
	}
//	public int checkLicense(int id, License license) {
//		String a = null;
//		
//		int v = license.getExYear();
//		if(getCurrentYear()>v) return 365;
//		v = license.getExMonth();
//		if(getCurrentMonth()>v) return 31;
//		v = license.getExDay();
//		if(getCurrentDay()>v) return getCurrentDay()-v;
//		
//		if(id==1) {
//			a = license.getMacAddress();
//			if(!a.equalsIgnoreCase(getLocalhostMacAddress())) return -5;
//			a = license.getHostName();
//			if(!a.equalsIgnoreCase(getLocalhostName())) return -6;
//			a = license.getCountry();
//			if(!a.equalsIgnoreCase(getCountry())) return -7;
//			a = license.getVersion();
//			//if(!a.equalsIgnoreCase(getVersion())) return -8;
//		}
//		
//		return 0;
//	}
	
	public boolean isExpired(License license) {
		int year = license.getExYear();
		int month = license.getExMonth();
		int day = license.getExDay();
		
		if(getCurrentYear()<year) return false;
		else if(getCurrentYear()>year) return true;
		
		if(getCurrentMonth()<month) return false;
		else if(getCurrentMonth()>month) return true;
		
		if(getCurrentDay()<=day) return false;
		else return true;
	}
	
	public int daysAfterExpiration()  {
		File folder = new File(_path);
		if(!folder.exists()) return 365;
		
		File[] listOfFiles = folder.listFiles();
		//for (int i = 0; i < listOfFiles.length; i++) {
		for (int i = 0; i <1; i++) {
			if (listOfFiles[i].isFile()) {
				String fileName = listOfFiles[i].getName();
				String extension = FilenameUtils.getExtension(fileName);
				if(extension.equalsIgnoreCase("lic")) {
					try {
						fileName = listOfFiles[i].getCanonicalPath();
					} catch (IOException e) {
						e.printStackTrace();
					}
					_license = readLicenseFile(0, fileName);
					return daysAfterExpiration(_license);
				}
			}
		}
		
		return 365;
	}
	
	public int daysAfterExpiration(License license) {
		int year = license.getExYear();
		int month = license.getExMonth();
		int day = license.getExDay();
		
		if(getCurrentYear()<year) return -365;
		else if(getCurrentYear()>year) return 365;
		
		GregorianCalendar gc = new GregorianCalendar();
		gc.set(GregorianCalendar.YEAR, year);
		gc.set(GregorianCalendar.MONTH, month);
		gc.set(GregorianCalendar.DAY_OF_MONTH, day);
		int dayOfYearExpiraton 	=gc.get(GregorianCalendar.DAY_OF_YEAR);
		
		gc.set(GregorianCalendar.YEAR, getCurrentYear());
		gc.set(GregorianCalendar.MONTH, getCurrentMonth());
		gc.set(GregorianCalendar.DAY_OF_MONTH, getCurrentDay());
		int dayOfYearCurrent 	=gc.get(GregorianCalendar.DAY_OF_YEAR);

		return dayOfYearCurrent-dayOfYearExpiraton;		
	}
	
//	public License readLicenseInputFile(String licenseFileName) {
//		return readLicenseFile(2, licenseFileName);
//	}
//	public License readLicenseFile(String licenseFileName) {
//		return readLicenseFile(1, licenseFileName);
//	}
	private License readLicenseFile(int id, String licenseFileName) {
		License license = null;
		String line = null;
		try{
			BufferedReader reader = new BufferedReader(new FileReader(licenseFileName));
			line=reader.readLine();
			reader.close();
			
			license = new License();
			
			String text = decoding(line);

			//System.out.println(text);
			StringTokenizer st = new StringTokenizer(text, " ,");
			String a = st.nextToken().trim().toString();
			license.setLicenseNumber(a);
			a = st.nextToken().trim().toString();
			license.setMacAddress(a);
			a = st.nextToken().trim().toString();
			license.setModuleName(a);
			a = st.nextToken().trim().toString();
			license.setVersion(a);			
			
			String input = st.nextToken().trim().toString();
			SimpleDateFormat ft = new SimpleDateFormat ("yyyy-MM-dd");
	    	Date expiration = null;
	    	try {
	    		expiration = ft.parse(input);
			} catch (ParseException e) {
			} 
			license.setExpiration(expiration);
			
			
			if(id==1) {
				a = st.nextToken().trim().toString();
				license.setCountry(a);
				
				a = st.nextToken().trim().toString();
				license.setHostName(a);
			}
			if(id==2) {
				st = new StringTokenizer(licenseFileName, "_");
				st.nextToken();
				st.nextToken();
				st.nextToken();
				a = st.nextToken().trim().toString();
				license.setEmail(a);
			}
			
		} catch (IOException e) {}
		
		return license;
	}

	public String encoding (String plainText) 	{ return Base64Coder.encodeString(plainText); }
	public String decoding (String base64Text) 	{ return Base64Coder.decodeString(base64Text); }
	public License getTrialLicense() {
		//String moduleName = LauncherApp.getModelName();
		String moduleName = "Test";
		String version = LauncherApp.getVersion();
		Date expiration = addMonth(1);
		String licenseNumber = generateLicenseNo();
		String email = "trial@license.com";
		License license = new License(moduleName, email, licenseNumber, expiration, LicenseType.ONEMONTH, version);
		license.setCountry(getCountry());
		license.setMacAddress(getLocalhostMacAddress());
		license.setHostName(getLocalhostName());
		
		return license;
	}
	
	public String getLicense(String selectedFileName, String email, LicenseType licenseType) {
		return readLicenseInputFile(selectedFileName, email, licenseType).getLicenseInput();
	}
	public String getLicenseFileName(String selectedFileName, String email, LicenseType licenseType) {
		return readLicenseInputFile(selectedFileName, email, licenseType).getLicenseFileName();
	}
	public License readLicenseInputFile(String selectedFileName, String email, LicenseType licenseType) {
		if(selectedFileName==null) return null;
		License license = readLicenseFile(1, selectedFileName);
		//System.out.println(license.getExpiration().toString());
		license.setEmail(email);
		license.setLicenseType(licenseType);
		if(licenseType==LicenseType.ONEMONTH) {
			license.setExpiration(addMonth(1));
		} else if(licenseType==LicenseType.SIXMONTH) {
			license.setExpiration(addMonth(6));
		} else if(licenseType==LicenseType.ONEYEAR) {
			license.setExpiration(addYear(1));
		} else if(licenseType==LicenseType.TWOYEAR) {
			license.setExpiration(addYear(2));
		} else if(licenseType==LicenseType.FIVEYEAR) {
			license.setExpiration(addYear(5));
		} else if(licenseType==LicenseType.LIFETIME) {
			license.setExpiration(addYear(100));
		} else { }
		//System.out.println(license.getExpiration().toString());
		
		return license;
	}
	
	public void generateFreeLicenseInput() {
		License license = getTrialLicense();

		String fileName = license.getLicenseFileName();
		String plainText = license.getLicenseInput();

		String selectedFileName = _path+File.separator+fileName;
		String encodedText = encoding(plainText);
		write(encodedText, selectedFileName);
	}

	public String getLocalhostName() {
		String hostName 	= "UNKNOWN";
		try {
			hostName = InetAddress.getLocalHost().getHostName();
		} catch(UnknownHostException e) { }
		return hostName;
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
	public String getCountry() { return Locale.getDefault().getCountry().toString(); }
	
	public String generateLicenseNo() { 
		Calendar calendar 	= Calendar.getInstance();
		return String.valueOf(calendar.getTime().getTime()/1000);
	}

	public int getCurrentYear()		{ return Calendar.getInstance().get(Calendar.YEAR); }
	public int getCurrentMonth()	{ return Calendar.getInstance().get(Calendar.MONTH); }
	public int getCurrentDay()		{ return Calendar.getInstance().get(Calendar.DAY_OF_MONTH); }
	
	public Date addYear(int nYear) 		{ return addTime(Calendar.getInstance().getTime(), nYear, 0, 0); }
	public Date addMonth(int nMonth) 	{ return addTime(Calendar.getInstance().getTime(), 0, nMonth, 0); }
	public Date addDay(int nDay) 		{ return addTime(Calendar.getInstance().getTime(), 0, 0, nDay); }
	
	public Date addYear(Date expiration, int nYear) 	{ return addTime(expiration, nYear, 0, 0); }
	public Date addMonth(Date expiration, int nMonth) 	{ return addTime(expiration, 0, nMonth, 0); }
	public Date addDay(Date expiration, int nDay) 		{ return addTime(expiration, 0, 0, nDay); }
    public Date addTime(Date expiration, int nYear, int nMonth, int nDay) {
    	int year = getExpirationYear(expiration)+nYear;
    	int month = getExpirationMonth(expiration)+nMonth;
    	int day = getExpirationDay(expiration)+nDay;
    	String input = year+"-"+month+"-"+day;
    	//System.out.println(input + " "+getCurrentMonth());
    	SimpleDateFormat ft = new SimpleDateFormat ("yyyy-MM-dd");
    	Date future = null;
    	try {
    		future = ft.parse(input);
		} catch (ParseException e) {
		} 

		//System.out.println(future.toString());
    	return future;
    }
	public int getExpirationYear(Date expiration )	{
		Calendar calendar 	= Calendar.getInstance();
		calendar.setTime(expiration);
		return calendar.get(Calendar.YEAR);
	}
	public int getExpirationMonth(Date expiration)	{
		Calendar calendar 	= Calendar.getInstance();
		calendar.setTime(expiration);
		return calendar.get(Calendar.MONTH);
	}
	public int getExpirationDay(Date expiration )	{
		Calendar calendar 	= Calendar.getInstance();
		calendar.setTime(expiration);
		return calendar.get(Calendar.DAY_OF_MONTH);
	}
	
	private int daysBetween2Dates(String line) {
		Calendar calendar 	= Calendar.getInstance();
		StringTokenizer st = new StringTokenizer(line, ",");
		long a = Long.valueOf(st.nextToken().trim().toString());
		long b = calendar.getTime().getTime();
		int days = (int)((b-a)/(24.0*3600.0*1000.0));
		return days;
	}
	
	public License read(String licenseFileName) {
		License license = null;
		String line = null;
		try{
			BufferedReader reader = new BufferedReader(new FileReader(licenseFileName));
			line=reader.readLine();
			reader.close();
			
			license = new License();
			
			String text = decoding(line);
			StringTokenizer st = new StringTokenizer(text, " ,");
			String a = st.nextToken().toString().trim();
			license.setLicenseNumber(a);
			a = st.nextToken().toString().trim();
			license.setMacAddress(a);
			a = st.nextToken().toString().trim();
			license.setModuleName(a);
			a = st.nextToken().toString().trim();
			license.setVersion(a);
			
			a = st.nextToken().toString().trim();
			SimpleDateFormat ft = new SimpleDateFormat("yyyy-MM-dd");
	    	Date expiration = null;
	    	try { expiration = ft.parse(a);
			} catch (ParseException e) {
			} 
			license.setExpiration(expiration);
			
			a = st.nextToken().trim().toString();
			license.setEmail(a);
			return license;
		} catch (IOException e) {}
		
		
		return license;
	}
	
	public String openFileUsingJFileChooser(String base)	{
		FileNameExtensionFilter extension = new FileNameExtensionFilter("*.lic", "lic");
		JFrame jframe = new JFrame("Open A File");
		String fullPath = FilenameUtils.getFullPath(base);
		JFileChooser chooser = new JFileChooser(fullPath);
		chooser.addChoosableFileFilter( extension );
		chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		chooser.setDialogTitle("Open A File");
		chooser.setSelectedFile(new File(base));
		
		int returnVal = chooser.showOpenDialog( jframe );
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File file = chooser.getSelectedFile();
			String fullFileName = file.getAbsolutePath();
			if (!file.exists()) {
				String message = "File does not exit!";
				String title = "Alert";
				JOptionPane.showMessageDialog(null, message, title, JOptionPane.ERROR_MESSAGE);
				return null;
			}
			
			return fullFileName;
		} else if (returnVal == JFileChooser.CANCEL_OPTION) { return null;		 							
		} else { }
		return null;
	}

	public String saveFileUsingJFileChooser(String base)	{
		FileNameExtensionFilter extension = new FileNameExtensionFilter("*.lic", "lic");
		JFrame jframe = new JFrame("Save A File");
		String fullPath = FilenameUtils.getFullPath(base);
		//String baseName = FilenameUtils.getBaseName(base);
		JFileChooser chooser = new JFileChooser(fullPath);
		chooser.addChoosableFileFilter( extension );
		chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		chooser.setDialogTitle("Save A File");
		chooser.setSelectedFile(new File(base));
		
		int returnVal = chooser.showSaveDialog( jframe );
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File file = chooser.getSelectedFile();
			String fullFileName = file.getAbsolutePath();
			if (file.exists ()) {
				int response = JOptionPane.showConfirmDialog (null,
						"Overwrite existing file?","Confirm Overwrite",
						JOptionPane.OK_CANCEL_OPTION,
						JOptionPane.QUESTION_MESSAGE);
				if (response == JOptionPane.CANCEL_OPTION) { return null; }
			}
			String []tmp = extension.getExtensions();
			if(FilenameUtils.getExtension(fullFileName)==null) fullFileName = fullFileName+"."+tmp[0];
			if(!fullFileName.contains(tmp[0])) {
				String message = "File name extension should be "+tmp[0];
				String title = "Alert";
				JOptionPane.showMessageDialog(null, message, title, JOptionPane.ERROR_MESSAGE);
				return null;
			}
			return fullFileName;
		} else if (returnVal == JFileChooser.CANCEL_OPTION) { return null;		 							
		} else { }
		return null;
	}
	
	public String readMtvLicenseText(String licenseFileName) {
		String line = null;
		try{
			BufferedReader reader = new BufferedReader(new FileReader(licenseFileName));
			line=reader.readLine();
			reader.close();
			
		} catch (IOException e) {}
		return line;
	}
	public String genMtvLicenseText() {
		String USERDNSDOMAIN = System.getenv("USERDNSDOMAIN");
		String version = LauncherApp.getVersion();
		String time = getCurrentYear()+"-"+getCurrentMonth()+"-"+getCurrentDay();
		String username = System.getProperty("user.name");
		
		return USERDNSDOMAIN+","+version+","+time+","+username;
	}
	public boolean write(String licenseText, String selectedFileName) {
		if(selectedFileName==null) return false;
		try{
			BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(selectedFileName, false));
			bufferedWriter.write(licenseText);
			bufferedWriter.close();
		} catch (IOException ioexception) {
			JOptionPane.showMessageDialog(null, "\nFile Format is not right!", 
					"Error", JOptionPane.ERROR_MESSAGE);
		}
		return true;
	}
	
//	private class CheckLicenseTask extends TimerTask {
//		public CheckLicenseTask() { }
//
//		@Override
//		public void run() {
//			System.out.println("checking license");
//		}
//	}
	
	public static void main1(String s[]) {
		String path = "C:\\prowess\\ucdm\\license";
		LicenseManager licenseManager = new LicenseManager(path);
		//System.out.println("checking license: " + licenseManager.checkLicense());		
	}

}
