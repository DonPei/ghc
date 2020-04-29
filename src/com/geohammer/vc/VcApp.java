package com.geohammer.vc;

import javax.swing.JFrame;
import javax.swing.UIManager;

import com.geohammer.resource.Resource;

public class VcApp {
	private int 		exitCode 			= 1; //1 system.exit() 2 dispose()
	private VcFrame 	frame 				= null;

	public VcApp(JFrame parent, int exitCode, int mode, int processID, String title) {
		try { 
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());     
		} catch (Exception e) {e.printStackTrace();     }
		exitCode 	= exitCode;
		start(exitCode, mode, processID, title);
	}

	public void start(int exitCode, int mode, int processID, String title) {
		frame = new VcFrame(exitCode, title);
		frame.setProcessID(processID);
		frame.setVisible(true);
		frame.setIconImage(Resource.loadImageIcon("cube.png").getImage());
	}
	
	public void exit() {if(frame!=null) frame.exit(false); }
}

