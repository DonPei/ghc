package com.geohammer.launcher;

import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import com.geohammer.core.planarmodel.LabRecord;
import com.geohammer.core.planarmodel.SingleLabRecord;
import com.geohammer.launcher.dialog.ColorGalleryDialog;
import com.geohammer.launcher.dialog.CsvColumnDialog;
import com.geohammer.launcher.dialog.CsvTrimmerDialog;
import com.geohammer.launcher.dialog.DASReceiverGeometryDialog;
import com.geohammer.launcher.dialog.FixDotDialog;
import com.geohammer.launcher.dialog.Grid3DViewerDialog;
import com.geohammer.launcher.dialog.Jave2CConverterDialog;
import com.geohammer.launcher.dialog.K51Dialog;
import com.geohammer.launcher.dialog.LabRecordDialog;
import com.geohammer.launcher.dialog.LasViewerDialog;
import com.geohammer.launcher.dialog.LoadLasFileDialog;
import com.geohammer.launcher.dialog.LoadSeg2FileDialog;
import com.geohammer.launcher.dialog.ManipulateGreenFileDialog;
import com.geohammer.launcher.dialog.ManipulateMTDialog;
import com.geohammer.launcher.dialog.ManipulateSrcMechDialog;
import com.geohammer.launcher.dialog.MovieDialog;
import com.geohammer.launcher.dialog.PhaseVelocityDemoDialog;
import com.geohammer.launcher.dialog.ReceiverGeometryDialog;
import com.geohammer.launcher.dialog.RecursiveFolderDialog;
import com.geohammer.launcher.dialog.ScreenCaptureDialog;
import com.geohammer.launcher.dialog.Unix2DosDialog;
import com.geohammer.launcher.dialog.WaveletDemoDialog;

import com.geohammer.license.LicenseGenerator;

import com.geohammer.component.LogWindowHandler;

@SuppressWarnings("serial")
public class LauncherMenuBar extends JMenuBar {
	LauncherFrame 	frame 	= null;

	public LauncherMenuBar(LauncherFrame frame, int iApp) {
		this.frame 		= frame;

		JMenu menuFile  	= new JMenu("File");
		JMenu menuUtil		= new JMenu("   Utility");
		JMenu menuTool		= new JMenu("   Tools");
		JMenu menuHelp  	= new JMenu("   Help");

		setFileMenu(frame, menuFile);
		setUtilMenu(frame, menuUtil);
		setToolMenu(frame, menuTool);
		setHelpMenu(frame, menuHelp);

		add(menuFile);
		add(menuUtil);
		if(iApp==1) add(menuTool);
		add(menuHelp);
	}

	public void setFileMenu(LauncherFrame frame, JMenu jmenu) {
		JMenuItem jMenuItem    	= null;
		jMenuItem    	= new JMenuItem("Exit");
		jMenuItem.setAccelerator( KeyStroke.getKeyStroke(KeyEvent.VK_Q, InputEvent.CTRL_MASK) );
		jmenu.add(jMenuItem);
		jMenuItem.addActionListener( new ActionListener() {
			public void actionPerformed( ActionEvent e ) {
				frame.exit();
				System.exit(0);
			}
		});
	}
	
	public void setToolMenu(LauncherFrame frame, JMenu jMenu) {
		JMenuItem jMenuItem    	= null;
		
		jMenuItem  	= new JMenuItem("Generate License");
		jMenuItem.setToolTipText("Generate a license request");
		jMenu.add(jMenuItem);
		jMenuItem.addActionListener( new ActionListener() {
			public void actionPerformed( ActionEvent e ) {
				String aTitle = new String("Request License");
				LicenseGenerator dialog = new LicenseGenerator(frame, aTitle, false, 2);
				dialog.showDialog();
			}
		});
		
		jMenuItem  	= new JMenuItem("Clean Temporary Folders");
		jMenuItem.setToolTipText("junk files will be deleted");
		//_menuUtil.add(jMenuItem);
		jMenuItem.addActionListener( new ActionListener() {
			public void actionPerformed( ActionEvent e ) { 
				String 	tmpPath = System.getProperty("java.io.tmpdir");
				File directory= new File(tmpPath);
				File [] list = directory.listFiles();
				for (File file : list) {
					if(file.isDirectory()) {
						String name = file.getName();
						if(name.contains("mpz")) {
							try {
								FileUtils.forceDelete(file);
							} catch (IOException e1) {
								e1.printStackTrace();
							} 
							//System.out.println(name); 
						}
					}
				}
			}
		});
		

		
		jMenuItem    	= new JMenuItem("ColorMap Gallery");
		jMenu.add(jMenuItem);
		jMenuItem.addActionListener( new ActionListener() {
			public void actionPerformed( ActionEvent e ) {
				String aTitle = new String("ColorMap Gallery");
				ColorGalleryDialog dialog = new ColorGalleryDialog(frame, aTitle, false, 0);
				dialog.showDialog();
			}
		});
		
		jMenuItem    	= new JMenuItem("ColorUtil Gallery");
		jMenu.add(jMenuItem);
		jMenuItem.addActionListener( new ActionListener() {
			public void actionPerformed( ActionEvent e ) {
				String aTitle = new String("ColorUtil Gallery");
				ColorGalleryDialog dialog = new ColorGalleryDialog(frame, aTitle, false, 1);
				dialog.showDialog();
			}
		});
		
	}

	public void setUtilMenu(LauncherFrame frame, JMenu jMenu) {
		JMenuItem jMenuItem  	= new JMenuItem("Anisotropy Database");
		jMenuItem.setToolTipText("published and measured anisotropic data");
		jMenu.add(jMenuItem);
		jMenuItem.addActionListener( new ActionListener() {
			public void actionPerformed( ActionEvent e ) {
				String fileName = System.getProperty("geohammer.dir")+File.separator+"lib"+File.separator+"databaseCore.xml";
				//System.out.println(fileName);
				File file = new File(fileName);
				LabRecord records = new LabRecord(fileName);
				String [] category = new String[records.getNumOfRecords()];
				String [] lithology = new String[records.getNumOfRecords()];
				double [][] properties = new double[category.length][7];
				for(int i=0; i<category.length; i++) {
					SingleLabRecord sl = records.getRecord(i);
					category[i] 	 = sl.getSampleCategory();
					lithology[i] 	 = sl.getLithology();
					properties[i][0] = sl.getVpGradientX(); //depth
					properties[i][1] = ((int)(sl.getVp()*10))/10.0;
					properties[i][2] = ((int)(sl.getVs()*10))/10.0;
					properties[i][3] = sl.getDen();
					properties[i][4] = sl.getDelta();
					properties[i][5] = sl.getEpsilon();
					properties[i][6] = sl.getGamma();
				}
				
				String aTitle = new String("Velocity And Anisotropy Database");
				LabRecordDialog dialog = new LabRecordDialog(frame, aTitle, false, 
						category, lithology, properties);
				dialog.showDialog();
			}
		});
		
		
		jMenu.addSeparator();
		
		jMenuItem    	= new JMenuItem("Make Movie");
		//jMenu.add(jMenuItem);
		jMenuItem.addActionListener( new ActionListener() {
			public void actionPerformed( ActionEvent e ) {
				String aTitle = new String("Make Movie");
//				MovieDialog dialog = new MovieDialog(frame, aTitle, false, frame.getFolderMoviePath());
//				dialog.showDialog();
			}
		});
	}

	public void setHelpMenu(LauncherFrame frame, JMenu jMenu) {
		JMenuItem jMenuItem  	= null;
//		jMenuItem  	= new JMenuItem("Online Doc");
//		jMenuItem.setToolTipText("get documents and training materials");
//		_menuHelp.add(jMenuItem);
//		jMenuItem.addActionListener( new ActionListener() {
//			public void actionPerformed( ActionEvent e ) {
//				String folder = "\\\\corp.halliburton.com\\Team\\Pinnacle\\HOU\\Development\\AdvProcessing\\UCDM_App\\doc";
//				File directory = new File(folder);
//				try {
//					Desktop.getDesktop().open(directory);
//				} catch (IOException e1) {
//					//e1.printStackTrace();
//					JOptionPane.showMessageDialog(null, "\nDocuments at: \n "+folder, 
//							"Info", JOptionPane.INFORMATION_MESSAGE);
//				} catch (java.lang.IllegalArgumentException e1) {
//					//e1.printStackTrace();
//					JOptionPane.showMessageDialog(null, "\nDocuments at: \n "+folder, 
//							"Info", JOptionPane.INFORMATION_MESSAGE);
//				}
////				File[] contents = directory.listFiles();
////				for ( File f : contents) {
////				  System.out.println(f.getAbsolutePath());
////				}
//			}
//		});
//		_menuHelp.addSeparator();
		
		jMenuItem  	= new JMenuItem("Web Page");
		jMenuItem.setToolTipText("get documents and training materials");
		//jMenu.add(jMenuItem);
		jMenuItem.addActionListener( new ActionListener() {
			public void actionPerformed( ActionEvent e ) {
				String folder = "\\\\corp.halliburton.com\\Team\\Pinnacle\\HOU\\Development\\AdvProcessing\\UCDM_App\\web\\index.html";
				File directory = new File(folder);
				try {
					Desktop.getDesktop().open(directory);
				} catch (IOException e1) {
					//e1.printStackTrace();
					JOptionPane.showMessageDialog(null, "\nDocuments at: \n "+folder, 
							"Info", JOptionPane.INFORMATION_MESSAGE);
				} catch (java.lang.IllegalArgumentException e1) {
					//e1.printStackTrace();
					JOptionPane.showMessageDialog(null, "\nDocuments at: \n "+folder, 
							"Info", JOptionPane.INFORMATION_MESSAGE);
				}
//				File[] contents = directory.listFiles();
//				for ( File f : contents) {
//				  System.out.println(f.getAbsolutePath());
//				}
			}
		});
		jMenu.addSeparator();
		
		
		jMenuItem  	= new JMenuItem("Update Software...");
		jMenuItem.setToolTipText("automatically update all modules");
		//_menuHelp.add(jMenuItem);
		jMenuItem.addActionListener( new ActionListener() {
			public void actionPerformed( ActionEvent e ) {
				//updateSoftware();
//				String aTitle = new String("Update Software");
//				ServerDialog dialog = new ServerDialog(_frame, aTitle);
//				dialog.setLauncherFrame(_frame);
//				dialog.showDialog();
			}
		});
		//_menuHelp.addSeparator();
		
		jMenuItem  	= new JMenuItem("Request License");
		jMenuItem.setToolTipText("Generate a license request");
		jMenu.add(jMenuItem);
		jMenuItem.addActionListener( new ActionListener() {
			public void actionPerformed( ActionEvent e ) {
				String aTitle = new String("Request License");
				LicenseGenerator dialog = new LicenseGenerator(frame, aTitle, false, 1);
				dialog.showDialog();
			}
		});

		jMenuItem  	= new JMenuItem("About...");
		jMenuItem.setToolTipText("About");
		jMenu.add(jMenuItem);
		jMenuItem.addActionListener( new ActionListener() {
			public void actionPerformed( ActionEvent e ) {
				JOptionPane.showMessageDialog(null, "\nGeohammer Toolbox "+LauncherApp.VERSION, 
						"Info", JOptionPane.INFORMATION_MESSAGE);
			}
		});
		
	}	
	
	public void updateSoftware() {
		//String srcPath = "C:\\prowess";
		//String srcPath = "\\\\corp.halliburton.com\\Team\\HOU\\Development\\AdvProcessing\\UCDM\\Prerequisites";
		String srcPath = "Z:\\HOU\\Development\\AdvProcessing\\UCDM\\Prerequisites";
		String targetPath = FilenameUtils.separatorsToSystem(System.getProperty("ucdm.dir"))+File.separator+"tmp";
		System.out.println(targetPath);
		System.out.println(srcPath);
		File[] listOfFiles = new File(srcPath).listFiles();
		for (File file : listOfFiles) { System.out.println(file.getName()); }
		walkingThroughFiles(srcPath, targetPath);
		
//		JOptionPane.showMessageDialog(null, "\nUtility Collection for Downhole Microseismic "+LauncherApp.VERSION, 
//				"Info", JOptionPane.INFORMATION_MESSAGE);
	}
	
	public void walkingThroughFiles(String srcPath, String targetPath) {
		//if(_cancel) return;
		String 	name = null;
		String 	srcFileName = null;
		String 	targetFileName = null;
		File[] listOfFiles = new File(srcPath).listFiles(); 

		for (int i=0; i<listOfFiles.length; i++) {
			name = listOfFiles[i].getName();
			//System.out.println(listOfFiles[i].getName());
			if (listOfFiles[i].isFile()) {
				srcFileName = srcPath+File.separator+name;
				targetFileName = targetPath+File.separator+name;
				try {
					FileUtils.copyFile(new File(srcFileName), new File(targetFileName));
					frame.getInfoLabel().setText("copying "+name);
					LogWindowHandler.getInstance().publish("Copy "+targetFileName+"\n");
				} catch (IOException e) {
					e.printStackTrace();
				}
			} else {
				String childTargetPath = targetPath+File.separator+name;
				File childFile = new File(childTargetPath);
				if(!childFile.exists()) {
					try {
						FileUtils.forceMkdir(childFile);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				walkingThroughFiles(srcPath+File.separator+name, childTargetPath);
			}
		}
	}
	

}

