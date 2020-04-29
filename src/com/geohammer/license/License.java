package com.geohammer.license;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class License implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String 		_moduleName 	= null;
    private String 		_email 			= null;
    private String 		_licenseNumber 	= null;
    private LicenseType _licenseType 	= null;
    private Date 		_expiration 	= null;
    private String 		_version 		= null;
    
	String _hostName 	= null;
	String _ipAddress 	= null;
	String _macAddress 	= null;
	String _country 	= null;
	int _exYear 		= 2000;
	int _exMonth 		= 0;
	int _exDay 			= 0;
    
    public License() {
        this("ALL", "trial@version.com", "999999", new Date(), LicenseType.ONEMONTH, "0-0-0-0");
    }
    
    public License(String moduleName, String email, String licenseNumber, Date expiration, 
    		LicenseType licenseType, String version) {
        _moduleName 	= moduleName;
        _email 			= email;
        _licenseNumber 	= licenseNumber;
        _expiration 	= expiration;
        _licenseType 	= licenseType;
        _version 		= version;
    }
    
    public int getExYear() 		{ return getExpiration(1); }
    public int getExMonth() 	{ return getExpiration(2); }
    public int getExDay() 		{ return getExpiration(3); }
    public int getExpiration(int id) {
    	Calendar calendar = Calendar.getInstance();
    	calendar.setTime(getExpiration());
    	int v = 0;
    	if(id==1) v = calendar.get(Calendar.YEAR);
    	if(id==2) v = calendar.get(Calendar.MONTH);
    	if(id==3) v = calendar.get(Calendar.DAY_OF_MONTH);

    	return v;
    }
    
    public String getModuleName() 		{ return _moduleName; }
    public String getEmail() 			{ return _email; }
    public String getLicenseNumber() 	{ return _licenseNumber; }
    public Date getExpiration() 		{ return _expiration; }
    public String getVersion() 			{ return _version; }
    public LicenseType getLicenseType() { return _licenseType; }
    public String getHostName() 		{ return _hostName; }
    public String getIpAddress() 		{ return _ipAddress; }
    public String getMacAddress() 		{ return _macAddress; }
    public String getCountry() 			{ return _country; }
    
    public void setModuleName(String moduleName) 		{ _moduleName = moduleName; }
    public void setEmail(String email) 					{ _email = email; }
    public void setLicenseNumber(String licenseNumber) 	{ _licenseNumber = licenseNumber; }
    public void setExpiration(Date expiration) 			{ _expiration = expiration; }
    public void setVersion(String version) 				{ _version = version; }
    public void setLicenseType(LicenseType licenseType) { _licenseType = licenseType; }
    public void setHostName(String name) 				{ _hostName = name; }
    public void setIpAddress(String name) 				{ _ipAddress = name; }
    public void setMacAddress(String name) 				{ _macAddress = name; }
    public void setCountry(String name) 				{ _country = name; }
    
    public String getLicenseFileName() {
    	LicenseManager licenseManager = new LicenseManager();
    	Date expiration = licenseManager.addMonth(getExpiration(), 1);
    	SimpleDateFormat ft = new SimpleDateFormat ("yyyy-MM-dd");
    	//for example geolab_01.01.00_23456_don.pei@gmail.com_20120812.lic
    	
        return String.format("%s_%s_%s_%s_expire_%s.lic", getModuleName(), getVersion(), 
        		getLicenseNumber(), getEmail(), ft.format(expiration));
    }
    
    public String getLicenseInput() {
    	SimpleDateFormat ft = new SimpleDateFormat ("yyyy-MM-dd");
        return String.format("%s, %s, %s, %s, %s, %s, %s", getModuleName(), getVersion(), 
        		getLicenseNumber(), ft.format(getExpiration()), getCountry(), getMacAddress(), getHostName());
    }
    
    public String toString() {
    	return _licenseNumber+","+_macAddress+","+_moduleName+","+_version+","+
				new SimpleDateFormat("yyyy-MM-dd").format(_expiration)+","+_email;
    }

    public String getEnvVariable() {
    	String b = "";
    	Map<String, String> env = System.getenv();
		for (String envName : env.keySet()) {
			b = b.concat(envName+"="+env.get(envName)+",");
		}		
    	return b;
    }
    public int getEnvVariableCount() {
    	int k = 0;
    	Map<String, String> env = System.getenv();
		for (String envName : env.keySet()) {
			k++;
		}		
    	return k;
    }
    public String getEnvVariable(int index) {
    	int k = 0;
    	Map<String, String> env = System.getenv();
		for (String envName : env.keySet()) {
			if(k==index) return envName+"="+env.get(envName);
		}		
    	return null;
    }
    public String getSystemProperty() {
    	String b = "";
    	Properties p = System.getProperties();
	    //p.list(System.out);
        Set states = p.keySet(); 
        Iterator itr = states.iterator();
        while(itr.hasNext()) {
           String str = (String) itr.next();
           b = b.concat(str+"="+p.getProperty(str)+",");
        }
        return b;
    }
    public String getSystemProperty(int id) {
    	if(id==1) return System.getProperty ("user.name")+","+System.getProperty ("user.home")+","+System.getProperty ("user.dir");
    	else if(id==2) return System.getProperty ("os.name")+","+System.getProperty ("os.arch")+","+System.getProperty ("os.version");
    	else return System.getProperty ("java.vendor")+","+System.getProperty ("java.home")+","+System.getProperty ("java.version");
    }
}
