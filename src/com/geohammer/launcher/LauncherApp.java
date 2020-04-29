package com.geohammer.launcher;

import java.awt.Color;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import com.geohammer.common.UcdmAbstractPreference;
import com.geohammer.resource.Resource;


//This file is part of The Technic Launcher Version 3.
//public class LauncherFrame extends DraggableFrame implements
//https://book2s.com/java/src/package/net/technicpack/launcher/ui/launcherframe.html#056619eb4921be625a2a4545498379a7
//https://book2s.com/java/java.html
//http://www.technicpack.net/
public class LauncherApp {
	public static final boolean IS_WINDOWS 	= ( java.io.File.separatorChar == '\\' );
	public static final String VERSION 		= "1.1.0"; //major.minor.build
	public static final String MODULE 		= "LAUNCHER";    

	private LauncherFrame 	frame 			= null;
	float [][] 				data 			= null;
	static public int 		iApp 			= 6; 
	//1 geohammer 6 release
	
	public LauncherApp() {
		try { 
			//UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());  
		} catch (Exception e) {e.printStackTrace();     }
		
		start(iApp);
	}
	
	public static String getVersion() 	{ return VERSION; }
	
	public void start(int iApp) {
		if(iApp==1||iApp==6) {
			frame = new LauncherFrame(iApp);
			frame.getContentPane().setBackground(Color.white);
			frame.setVisible(true);
			frame.setIconImage(Resource.loadImageIcon("icon.png").getImage());
		} else if(iApp==2) {
			//MtvApp mtvApp 	= new MtvApp(null, 1,  1, iApp);
		} else if(iApp==3) {
			
		}
	}
	
	public void exit() { System.exit(0); }
	
	protected static void loadLib(String libPath) {
		String libFileName = null;
		try {
			final char SEPC = '/';
//			String libFileName1 = libPath+"\\org_ucdm_fd3d.dll";
//			libFileName = libFileName1.trim().replace('/', SEPC).replace('\\', SEPC);
//			System.out.println("lib2 = "+libFileName);
//			System.load(libFileName);

			//libFileName1 = libPath+"lib\\jogl_awt.dll";
			//libFileName = libFileName1.trim().replace('/', SEPC).replace('\\', SEPC);
			//System.out.println("lib3 = "+libFileName);
			//System.load(libFileName);

			//libFileName1 = libPath+"lib\\gluegen-rt.dll";
			//libFileName = libFileName1.trim().replace('/', SEPC).replace('\\', SEPC);
			//System.out.println("lib3 = "+libFileName);
			//System.load(libFileName);

			//System.out.println("calling org_ucdm");
			//System.loadLibrary("ucdm");
			//final String[] libraries = ClassScope.getLoadedLibraries(ClassLoader.getSystemClassLoader()); 
			//for(int i=0; i<libraries.length; i++) System.out.println(libraries[i]);
			
		} catch ( java.lang.UnsatisfiedLinkError e ) {
			System.out.println(e);
			
			String envVariables = "\n";
			Map map = System.getenv();
		    Set keys = map.keySet();
		    Iterator iterator = keys.iterator();
		    while (iterator.hasNext()) {
		      String key = (String) iterator.next();
		      String value = (String) map.get(key);

		      //System.out.println(key + " = " + value);
		      envVariables += key + " = " + value+"\n";
		    }
		    System.out.println("\n\nEnvironment Variable:\n"+envVariables+"\n");
		    
			String property = System.getProperty("java.library.path");
			StringTokenizer parser = new StringTokenizer(property, ";");
			String b = String.format("Load library failed! \n\n");
			String a = String.format("System library path:"); 
			b = new String(b.concat("\n"+a));

			while (parser.hasMoreTokens()) {
				a = parser.nextToken();
				b = new String(b.concat("\n"+a));
			}
			JOptionPane.showMessageDialog( null,
					"java.library.path = " + b+"\n"+e.toString(), "Error", JOptionPane.ERROR_MESSAGE );
			System.exit( -1 );
		}
	}
	

	public static void main(String[] args) {
		boolean createdNewDirectory = false;		
	
		if(iApp==1||iApp==6) {
			try {
				createdNewDirectory = UcdmAbstractPreference.setUcdmDirectory(".geohammer");
			} catch( Exception e ) {
				JOptionPane.showMessageDialog( null, e.toString() + " - GeoHammer will not run.", "Error",
						JOptionPane.ERROR_MESSAGE );
				System.exit( -1 );
			}
			checkHostMachine();		
			String libPath = System.getProperty( "java.library.path" );
			//System.out.println("lib = "+libPath);
			loadLib(libPath);
		} else if(iApp==2) {
			try {
				createdNewDirectory = UcdmAbstractPreference.setUcdmDirectory(".mtv");

				//File directory = new File(System.getProperty("user.dir"));				
				//String tmpdir = System.getProperty("java.io.tmpdir"); 
				//System.out.println("The default value of the java.io.tmpdir system property is: \"" + tmpdir  + "\"\n"); 

			} catch( Exception e ) {
				JOptionPane.showMessageDialog( null, e.toString() + " - UCDM will not run.", "Error",
						JOptionPane.ERROR_MESSAGE );
				System.exit( -1 );
			}
		}
		new LauncherApp();
//		SwingUtilities.invokeLater(new Runnable() {
//			public void run() {
//				new LauncherApp();
//			}
//		});
	}
	
	private static void checkHostMachine() {
		//Properties prop = System.getProperties();
	    //System.out.println("Printing all System properties");
	    //prop.list(System.out);
	    
//	    String USERDNSDOMAIN = System.getenv("USERDNSDOMAIN");
//	    //System.out.println("USERDNSDOMAIN=" + USERDNSDOMAIN);
//	    if(USERDNSDOMAIN==null||(!USERDNSDOMAIN.toUpperCase().contains("HALLIBURTON"))) {
//	    	String legal = "This software runs only on Halliburton-issued computer." +
//	    			"\nCopyright (c) 2015 Halliburton. All Rights Reserved.\n";
//	    			 
//	    	String envVariables = "\n";
//			Map map = System.getenv();
//		    Set keys = map.keySet();
//		    Iterator iterator = keys.iterator();
//		    while (iterator.hasNext()) {
//		      String key = (String) iterator.next();
//		      String value = (String) map.get(key);
//
//		      System.out.println(key + " = " + value);
//		      envVariables += key + " = " + value+"\n";
//		    }
//		    //JOptionPane.showMessageDialog( null, legal+envVariables, "Error", JOptionPane.ERROR_MESSAGE );
//	    	JOptionPane.showMessageDialog( null, legal, "Error", JOptionPane.ERROR_MESSAGE );
//			System.exit( -1 );
//	    }
	}

}


