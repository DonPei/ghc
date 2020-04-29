package com.geohammer.core;

import java.util.Date;

public class Project {
	int _id 		= 0;
	
	String _dimension		= null; // 2D or 3D
	String _siteName 		= null;
	String _timeZone 		= null;
	String _unit 			= null;
	
	int _day;
	int _month;
	int _year;
	
	//Reference point is the first tool in the first observation well
	double _utmE 		= 0.0;
	double _utmN 		= 0.0;
	String _utmZone 	= null;
	double _kbElev 		= 0.0;
	
	public Project(String siteName, int year, int month, int day, String timeZone, String unit, 
			double utmE, double utmN, String utmZone, double kbElev, String dimension) {
		_siteName 	= siteName;
		_year 		= year;
		_month 		= month;
		_day 		= day;
		_timeZone 	= timeZone;
		_unit 		= unit;
		_utmE 		= utmE;
		_utmN 		= utmN;
		_utmZone 	= utmZone;
		_kbElev 	= kbElev;
		_dimension 	= dimension;
	}
	
	public String getUnit() { return _unit; }
	
	public String toString() {
		String b = String.format("%s, %02d/%02d/%02d, %s", _siteName, _month, _day, _year, _timeZone);
		//String b = _siteName + ", "+ _month+"/"+_day+"/"+_year+", "+ _timeZone;
		String a = "Length Unit: "+ _unit;
		b = b.concat("\n"+a+"\n");
		
		a = _utmE+" "+_utmN+" "+_utmZone+" "+_kbElev+" //Coordinate(East-North-Down) system origin";
		b = b.concat(a+"\n");
		
		a = "Project Setup: "+ _dimension;
		b = b.concat(a+"\n");
		
		return b;
	}
}
