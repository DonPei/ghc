package com.geohammer.license;

import java.lang.reflect.InvocationTargetException; 
import java.lang.reflect.Method; 
import java.util.HashMap; 
import java.util.Map; 
import java.util.ArrayList; 
import java.util.List; 
import java.util.prefs.Preferences; 


/*I know this question is old, but it is the first search result on google to "java read/write to registry". Recently I found this amazing piece of code which:
The JDK contains the required code (java.util.prefs.WindowsPreferences) to access the Windows registry but to preserve the "purity" of Java, the code is declared as private so it's not visible. 
The trick is to use reflection to access private methods defined in the WindowsPreference class. 

Can read/write to ANY part of the registry. 
DOES NOT USE JNI. 
DOES NOT USE ANY 3rd PARTY/EXTERNAL APPLICATIONS TO WORK. 
DOES NOT USE THE WINDOWS API (directly) 
This is pure, Java code.

It uses reflection to work, by actually accessing the private methods in the java.util.prefs.Preferences class. The internals of this class are complicated, but the class itself is very easy to use.

For example, the following code obtains the exact windows distribution from the registry:

String value = WinRegistry.readString ( 
WinRegistry.HKEY_LOCAL_MACHINE, //HKEY 
"SOFTWARE\\Microsoft\\Windows NT\\CurrentVersion", //Key 
"ProductName"); //ValueName 
System.out.println("Windows Distribution = " + value); 
 */

public class WinRegistry { 
	public static final int HKEY_CURRENT_USER = 0x80000001; 
	public static final int HKEY_LOCAL_MACHINE = 0x80000002; 
	public static final int REG_SUCCESS = 0; 
	public static final int REG_NOTFOUND = 2; 
	public static final int REG_ACCESSDENIED = 5; 

	private static final int KEY_ALL_ACCESS = 0xf003f; 
	private static final int KEY_READ = 0x20019; 
	private static Preferences userRoot = Preferences.userRoot(); 
	private static Preferences systemRoot = Preferences.systemRoot(); 
	private static Class<? extends Preferences> userClass = userRoot.getClass(); 
	private static Method regOpenKey = null; 
	private static Method regCloseKey = null; 
	private static Method regQueryValueEx = null; 
	private static Method regEnumValue = null; 
	private static Method regQueryInfoKey = null; 
	private static Method regEnumKeyEx = null; 
	private static Method regCreateKeyEx = null; 
	private static Method regSetValueEx = null; 
	private static Method regDeleteKey = null; 
	private static Method regDeleteValue = null; 

	static { 
		try { 
			regOpenKey = userClass.getDeclaredMethod("WindowsRegOpenKey", 
					new Class[] { int.class, byte[].class, int.class }); 
			regOpenKey.setAccessible(true); 
			regCloseKey = userClass.getDeclaredMethod("WindowsRegCloseKey", 
					new Class[] { int.class }); 
			regCloseKey.setAccessible(true); 
			regQueryValueEx = userClass.getDeclaredMethod("WindowsRegQueryValueEx", 
					new Class[] { int.class, byte[].class }); 
			regQueryValueEx.setAccessible(true); 
			regEnumValue = userClass.getDeclaredMethod("WindowsRegEnumValue", 
					new Class[] { int.class, int.class, int.class }); 
			regEnumValue.setAccessible(true); 
			regQueryInfoKey = userClass.getDeclaredMethod("WindowsRegQueryInfoKey1", 
					new Class[] { int.class }); 
			regQueryInfoKey.setAccessible(true); 
			regEnumKeyEx = userClass.getDeclaredMethod( 
					"WindowsRegEnumKeyEx", new Class[] { int.class, int.class, 
							int.class }); 
			regEnumKeyEx.setAccessible(true); 
			regCreateKeyEx = userClass.getDeclaredMethod( 
					"WindowsRegCreateKeyEx", new Class[] { int.class, 
							byte[].class }); 
			regCreateKeyEx.setAccessible(true); 
			regSetValueEx = userClass.getDeclaredMethod( 
					"WindowsRegSetValueEx", new Class[] { int.class, 
							byte[].class, byte[].class }); 
			regSetValueEx.setAccessible(true); 
			regDeleteValue = userClass.getDeclaredMethod( 
					"WindowsRegDeleteValue", new Class[] { int.class, 
							byte[].class }); 
			regDeleteValue.setAccessible(true); 
			regDeleteKey = userClass.getDeclaredMethod( 
					"WindowsRegDeleteKey", new Class[] { int.class, 
							byte[].class }); 
			regDeleteKey.setAccessible(true); 
		} catch (Exception e) { 
			e.printStackTrace(); 
		} 
	} 

	private WinRegistry() { } 

	/** 
	 * Read a value from key and value name 
	 * @param hkey HKEY_CURRENT_USER/HKEY_LOCAL_MACHINE 
	 * @param key 
	 * @param valueName 
	 * @return the value 
	 * @throws IllegalArgumentException 
	 * @throws IllegalAccessException 
	 * @throws InvocationTargetException 
	 */ 
	public static String readString(int hkey, String key, String valueName) 
			throws IllegalArgumentException, IllegalAccessException, InvocationTargetException 
			{ 
		if (hkey == HKEY_LOCAL_MACHINE) { 
			return readString(systemRoot, hkey, key, valueName); 
		} else if (hkey == HKEY_CURRENT_USER) { 
			return readString(userRoot, hkey, key, valueName); 
		} else { 
			throw new IllegalArgumentException("hkey=" + hkey); 
		} 
			} 

	/** 
	 * Read value(s) and value name(s) form given key 
	 * @param hkey HKEY_CURRENT_USER/HKEY_LOCAL_MACHINE 
	 * @param key 
	 * @return the value name(s) plus the value(s) 
	 * @throws IllegalArgumentException 
	 * @throws IllegalAccessException 
	 * @throws InvocationTargetException 
	 */ 
	public static Map<String, String> readStringValues(int hkey, String key) 
			throws IllegalArgumentException, IllegalAccessException, InvocationTargetException 
			{ 
		if (hkey == HKEY_LOCAL_MACHINE) { 
			return readStringValues(systemRoot, hkey, key); 
		} 
		else if (hkey == HKEY_CURRENT_USER) { 
			return readStringValues(userRoot, hkey, key); 
		} 
		else { 
			throw new IllegalArgumentException("hkey=" + hkey); 
		} 
			} 

	/** 
	 * Read the value name(s) from a given key 
	 * @param hkey HKEY_CURRENT_USER/HKEY_LOCAL_MACHINE 
	 * @param key 
	 * @return the value name(s) 
	 * @throws IllegalArgumentException 
	 * @throws IllegalAccessException 
	 * @throws InvocationTargetException 
	 */ 
	public static List<String> readStringSubKeys(int hkey, String key) 
			throws IllegalArgumentException, IllegalAccessException, 
			InvocationTargetException 
			{ 
		if (hkey == HKEY_LOCAL_MACHINE) { 
			return readStringSubKeys(systemRoot, hkey, key); 
		} 
		else if (hkey == HKEY_CURRENT_USER) { 
			return readStringSubKeys(userRoot, hkey, key); 
		} 
		else { 
			throw new IllegalArgumentException("hkey=" + hkey); 
		} 
			} 

	/** 
	 * Create a key 
	 * @param hkey HKEY_CURRENT_USER/HKEY_LOCAL_MACHINE 
	 * @param key 
	 * @throws IllegalArgumentException 
	 * @throws IllegalAccessException 
	 * @throws InvocationTargetException 
	 */ 
	public static void createKey(int hkey, String key) 
			throws IllegalArgumentException, IllegalAccessException, 
			InvocationTargetException 
			{ 
		int [] ret; 
		if (hkey == HKEY_LOCAL_MACHINE) { 
			ret = createKey(systemRoot, hkey, key); 
			regCloseKey.invoke(systemRoot, new Object[] { new Integer(ret[0]) }); 
		} 
		else if (hkey == HKEY_CURRENT_USER) { 
			ret = createKey(userRoot, hkey, key); 
			regCloseKey.invoke(userRoot, new Object[] { new Integer(ret[0]) }); 
		} 
		else { 
			throw new IllegalArgumentException("hkey=" + hkey); 
		} 
		if (ret[1] != REG_SUCCESS) { 
			throw new IllegalArgumentException("rc=" + ret[1] + " key=" + key); 
		} 
			} 

	/** 
	 * Write a value in a given key/value name 
	 * @param hkey 
	 * @param key 
	 * @param valueName 
	 * @param value 
	 * @throws IllegalArgumentException 
	 * @throws IllegalAccessException 
	 * @throws InvocationTargetException 
	 */ 
	public static void writeStringValue 
	(int hkey, String key, String valueName, String value) 
			throws IllegalArgumentException, IllegalAccessException, 
			InvocationTargetException 
			{ 
		if (hkey == HKEY_LOCAL_MACHINE) { 
			writeStringValue(systemRoot, hkey, key, valueName, value); 
		} 
		else if (hkey == HKEY_CURRENT_USER) { 
			writeStringValue(userRoot, hkey, key, valueName, value); 
		} 
		else { 
			throw new IllegalArgumentException("hkey=" + hkey); 
		} 
			} 

	/** 
	 * Delete a given key 
	 * @param hkey 
	 * @param key 
	 * @throws IllegalArgumentException 
	 * @throws IllegalAccessException 
	 * @throws InvocationTargetException 
	 */ 
	public static void deleteKey(int hkey, String key) 
			throws IllegalArgumentException, IllegalAccessException, 
			InvocationTargetException 
			{ 
		int rc = -1; 
		if (hkey == HKEY_LOCAL_MACHINE) { 
			rc = deleteKey(systemRoot, hkey, key); 
		} 
		else if (hkey == HKEY_CURRENT_USER) { 
			rc = deleteKey(userRoot, hkey, key); 
		} 
		if (rc != REG_SUCCESS) { 
			throw new IllegalArgumentException("rc=" + rc + " key=" + key); 
		} 
			} 

	/** 
	 * delete a value from a given key/value name 
	 * @param hkey 
	 * @param key 
	 * @param value 
	 * @throws IllegalArgumentException 
	 * @throws IllegalAccessException 
	 * @throws InvocationTargetException 
	 */ 
	public static void deleteValue(int hkey, String key, String value) 
			throws IllegalArgumentException, IllegalAccessException, 
			InvocationTargetException 
			{ 
		int rc = -1; 
		if (hkey == HKEY_LOCAL_MACHINE) { 
			rc = deleteValue(systemRoot, hkey, key, value); 
		} 
		else if (hkey == HKEY_CURRENT_USER) { 
			rc = deleteValue(userRoot, hkey, key, value); 
		} 
		if (rc != REG_SUCCESS) { 
			throw new IllegalArgumentException("rc=" + rc + " key=" + key + " value=" + value); 
		} 
			} 

	// ===================== 

	private static int deleteValue 
	(Preferences root, int hkey, String key, String value) 
			throws IllegalArgumentException, IllegalAccessException, 
			InvocationTargetException 
			{ 
		int[] handles = (int[]) regOpenKey.invoke(root, new Object[] { 
				new Integer(hkey), toCstr(key), new Integer(KEY_ALL_ACCESS) }); 
		if (handles[1] != REG_SUCCESS) { 
			return handles[1]; // can be REG_NOTFOUND, REG_ACCESSDENIED 
		} 
		int rc =((Integer) regDeleteValue.invoke(root, 
				new Object[] { 
				new Integer(handles[0]), toCstr(value) 
		})).intValue(); 
		regCloseKey.invoke(root, new Object[] { new Integer(handles[0]) }); 
		return rc; 
			} 

	private static int deleteKey(Preferences root, int hkey, String key) 
			throws IllegalArgumentException, IllegalAccessException, 
			InvocationTargetException 
			{ 
		int rc =((Integer) regDeleteKey.invoke(root, 
				new Object[] { new Integer(hkey), toCstr(key) })).intValue(); 
		return rc; // can REG_NOTFOUND, REG_ACCESSDENIED, REG_SUCCESS 
			} 

	private static String readString(Preferences root, int hkey, String key, String value) 
			throws IllegalArgumentException, IllegalAccessException, 
			InvocationTargetException 
			{ 
		int[] handles = (int[]) regOpenKey.invoke(root, new Object[] { 
				new Integer(hkey), toCstr(key), new Integer(KEY_READ) }); 
		if (handles[1] != REG_SUCCESS) { 
			return null; 
		} 
		byte[] valb = (byte[]) regQueryValueEx.invoke(root, new Object[] { 
				new Integer(handles[0]), toCstr(value) }); 
		regCloseKey.invoke(root, new Object[] { new Integer(handles[0]) }); 
		return (valb != null ? new String(valb).trim() : null); 
			} 

	private static Map<String,String> readStringValues 
	(Preferences root, int hkey, String key) 
			throws IllegalArgumentException, IllegalAccessException, 
			InvocationTargetException 
			{ 
		HashMap<String, String> results = new HashMap<String,String>(); 
		int[] handles = (int[]) regOpenKey.invoke(root, new Object[] { 
				new Integer(hkey), toCstr(key), new Integer(KEY_READ) }); 
		if (handles[1] != REG_SUCCESS) { 
			return null; 
		} 
		int[] info = (int[]) regQueryInfoKey.invoke(root, 
				new Object[] { new Integer(handles[0]) }); 

		int count = info[2]; // count 
		int maxlen = info[3]; // value length max 
		for(int index=0; index<count; index++) { 
			byte[] name = (byte[]) regEnumValue.invoke(root, new Object[] { 
					new Integer 
					(handles[0]), new Integer(index), new Integer(maxlen + 1)}); 
			String value = readString(hkey, key, new String(name)); 
			results.put(new String(name).trim(), value); 
		} 
		regCloseKey.invoke(root, new Object[] { new Integer(handles[0]) }); 
		return results; 
			} 

	private static List<String> readStringSubKeys 
	(Preferences root, int hkey, String key) 
			throws IllegalArgumentException, IllegalAccessException, 
			InvocationTargetException 
			{ 
		List<String> results = new ArrayList<String>(); 
		int[] handles = (int[]) regOpenKey.invoke(root, new Object[] { 
				new Integer(hkey), toCstr(key), new Integer(KEY_READ) 
		}); 
		if (handles[1] != REG_SUCCESS) { 
			return null; 
		} 
		int[] info = (int[]) regQueryInfoKey.invoke(root, 
				new Object[] { new Integer(handles[0]) }); 

		int count = info[2]; // count 
		int maxlen = info[3]; // value length max 
		for(int index=0; index<count; index++) { 
			byte[] name = (byte[]) regEnumKeyEx.invoke(root, new Object[] { 
					new Integer 
					(handles[0]), new Integer(index), new Integer(maxlen + 1) 
			}); 
			results.add(new String(name).trim()); 
		} 
		regCloseKey.invoke(root, new Object[] { new Integer(handles[0]) }); 
		return results; 
			} 

	private static int [] createKey(Preferences root, int hkey, String key) 
			throws IllegalArgumentException, IllegalAccessException, 
			InvocationTargetException 
			{ 
		return (int[]) regCreateKeyEx.invoke(root, 
				new Object[] { new Integer(hkey), toCstr(key) }); 
			} 

	private static void writeStringValue 
	(Preferences root, int hkey, String key, String valueName, String value) 
			throws IllegalArgumentException, IllegalAccessException, 
			InvocationTargetException 
			{ 
		int[] handles = (int[]) regOpenKey.invoke(root, new Object[] { 
				new Integer(hkey), toCstr(key), new Integer(KEY_ALL_ACCESS) }); 

		regSetValueEx.invoke(root, 
				new Object[] { 
				new Integer(handles[0]), toCstr(valueName), toCstr(value) 
		}); 
		regCloseKey.invoke(root, new Object[] { new Integer(handles[0]) }); 
			} 

	// utility 
	private static byte[] toCstr(String str) { 
		byte[] result = new byte[str.length() + 1]; 

		for (int i = 0; i < str.length(); i++) { 
			result[i] = (byte) str.charAt(i); 
		} 
		result[str.length()] = 0; 
		return result; 
	} 
	
	//test
	public static void main(String args[]) throws Exception {
		String value = "";

		// IE Download directory (HKEY_CURRENT_USER)
		value = WinRegistry.readString(WinRegistry.HKEY_CURRENT_USER,
				"Software\\Microsoft\\Internet Explorer", "Download Directory");
		System.out.println("IE Download directory = " + value);

		// Query for Acrobat Reader installation path (HKEY_LOCAL_MACHINE)
		value = WinRegistry.readString(WinRegistry.HKEY_LOCAL_MACHINE,
				"SOFTWARE\\Microsoft\\Windows\\CurrentVersion\\App Paths\\AcroRd32.exe", "");
		System.out.println("Acrobat Reader Path = " + value);

		// Loop through installed JRE and print the JAVA_HOME value
		// HKEY_LOCAL_MACHINE\SOFTWARE\JavaSoft\Java Runtime Environment
		java.util.Map res1 = WinRegistry.readStringValues(WinRegistry.HKEY_LOCAL_MACHINE, 
				"SOFTWARE\\Microsoft\\Windows NT\\CurrentVersion");
		System.out.println("res1 = " + res1.toString());

		java.util.List res2 = WinRegistry.readStringSubKeys(WinRegistry.HKEY_LOCAL_MACHINE, 
				"SOFTWARE\\Microsoft\\Windows NT\\CurrentVersion");
		System.out.println("res2 = " + res2.toString());

//		WinRegistry.createKey(
//				WinRegistry.HKEY_CURRENT_USER, "SOFTWARE\\rgagnon.com");
//		WinRegistry.writeStringValue(
//				WinRegistry.HKEY_CURRENT_USER, 
//				"SOFTWARE\\rgagnon.com", 
//				"HowTo", 
//				"java");

		//	      WinRegistry.deleteValue(
		//	          WinRegistry.HKEY_CURRENT_USER, 
		//	          "SOFTWARE\\rgagnon.com", "HowTo");
		//	      WinRegistry.deleteKey(
		//	          WinRegistry.HKEY_CURRENT_USER, 
		//	          "SOFTWARE\\rgagnon.com\\");

		System.out.println("Done." );
	}
}



/*
package org.kodejava.example.util;

import java.util.prefs.Preferences;

public class RegistryDemo {
    public static final String PREF_KEY = "org.kodejava";
    public static void main(String[] args) {
        //
        // Write Preferences information to HKCU (HKEY_CURRENT_USER),
        // HKCU\Software\JavaSoft\Prefs\org.kodejava
        //
        Preferences userPref = Preferences.userRoot();
        userPref.put(PREF_KEY, "www.kodejava.org");

        //
        // Below we read back the value we've written in the code above.
        //
        System.out.println("Preferences = "
                + userPref.get(PREF_KEY, PREF_KEY + " was not found."));

        //
        // Write Preferences information to HKLM (HKEY_LOCAL_MACHINE),
        // HKLM\Software\JavaSoft\Prefs\org.kodejava
        //
        Preferences systemPref = Preferences.systemRoot();
        systemPref.put(PREF_KEY, "www.kodejava.org");

        //
        // Read back the value we've written in the code above.
        //
        System.out.println("Preferences = "
                + systemPref.get(PREF_KEY, PREF_KEY + " was not found."));
    }
}

package org.kodejava.example.util;

import java.util.Calendar;

public class CalendarExample {
    public static void main(String[] args) {
        //
        // Get various information from the Date object.
        //
        Calendar cal = Calendar.getInstance();
        int day = cal.get(Calendar.DATE);
        int month = cal.get(Calendar.MONTH) + 1;
        int year = cal.get(Calendar.YEAR);
        int dow = cal.get(Calendar.DAY_OF_WEEK);
        int dom = cal.get(Calendar.DAY_OF_MONTH);
        int doy = cal.get(Calendar.DAY_OF_YEAR);

        System.out.println("Current Date: " + cal.getTime());
        System.out.println("Day: " + day);
        System.out.println("Month: " + month);
        System.out.println("Year: " + year);
        System.out.println("Day of Week: " + dow);
        System.out.println("Day of Month: " + dom);
        System.out.println("Day of Year: " + doy);
    }
}
package org.kodejava.example.util;

import java.util.Calendar;

public class DateDifferenceExample 
{    
    public static void main(String[] args)
    {
        // Creates two calendars instances
        Calendar cal1 = Calendar.getInstance();
        Calendar cal2 = Calendar.getInstance();

        // Set the date for both of the calendar instance
        cal1.set(2006, 12, 30);
        cal2.set(2007, 5, 3);

        // Get the represented date in milliseconds
        long milis1 = cal1.getTimeInMillis();
        long milis2 = cal2.getTimeInMillis();

        // Calculate difference in milliseconds
        long diff = milis2 - milis1;

        // Calculate difference in seconds
        long diffSeconds = diff / 1000;

        // Calculate difference in minutes
        long diffMinutes = diff / (60 * 1000);

        // Calculate difference in hours
        long diffHours = diff / (60 * 60 * 1000);

        // Calculate difference in days
        long diffDays = diff / (24 * 60 * 60 * 1000);

        System.out.println("In milliseconds: " + diff + " milliseconds.");
        System.out.println("In seconds: " + diffSeconds + " seconds.");
        System.out.println("In minutes: " + diffMinutes + " minutes.");
        System.out.println("In hours: " + diffHours + " hours.");
        System.out.println("In days: " + diffDays + " days.");
    }
}
package org.kodejava.example.util;

import java.util.Calendar;

public class LastDayOfMonth {
    public static void main(String[] args) {
	//
	// Get a calendar instance
	//
	Calendar calendar = Calendar.getInstance();

	//
	// Get the last date of the current month. To get the last date for a
	// specific month you can set the calendar month using calendar object
	// calendar.set(Calendar.MONTH, theMonth) method.
	//	
	int lastDate = calendar.getActualMaximum(Calendar.DATE);

	//
	// Set the calendar date to the last date of the month so then we can
	// get the last day of the month
	//
	calendar.set(Calendar.DATE, lastDate);	
	int lastDay = calendar.get(Calendar.DAY_OF_WEEK);

	//
	// Print the current date and the last date of the month
	//
	System.out.println("Last Date: " + calendar.getTime());

	//
	// The lastDay will be in a value from 1 to 7 where 1 = monday and 7 =
	// saturday respectively.
	//
	System.out.println("Last Day : " + lastDay);	
    }
}


UUID / GUID (Universally / Globally Unique IDentifier) are frequently use in programming. 
Some of its usage are for creating random file names, session id in web application,
 transaction id and for record's primary keys in database replacing the sequence 
 or auto generated number.


package org.kodejava.example.util;

import java.util.UUID;

public class RandomStringUUID {
    public static void main(String[] args) {
        //
        // Creating a random UUID (Universally unique identifier).
        //
        UUID uuid = UUID.randomUUID();
        String randomUUIDString = uuid.toString();

        System.out.println("Random UUID String = " + randomUUIDString);
        System.out.println("UUID version       = " + uuid.version());
        System.out.println("UUID variant       = " + uuid.variant());
    }
}

import java.util.Calendar;

public class GetMonthNameExample {
    public static void main(String[] args) {
        String[] monthName = {"January", "February",
            "March", "April", "May", "June", "July",
            "August", "September", "October", "November",
            "December"};

        Calendar cal = Calendar.getInstance();
        String month = monthName[cal.get(Calendar.MONTH)];

        System.out.println("Month name: " + month);
    }
}
[HKEY_LOCAL_MACHINE\SOFTWARE\JavaSoft\Java Runtime Environment\CurrentVersion]

and then get the path of the JRE from the corresponding key 
[HKEY_LOCAL_MACHINE\SOFTWARE\JavaSoft\Java Runtime Environment\1.5\JavaHome]



 */
