package com.geohammer.vc.dialog;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.StringTokenizer;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.apache.commons.io.FilenameUtils;

import com.geohammer.common.CommonDialog;
import com.geohammer.common.ProjectVc;
import com.geohammer.vc.VcFrame;

public class ProjectDialog  extends CommonDialog {
	private VcFrame 	frame 			= null;
	public JTextField 	velTF 			= null;
	public JTextField 	tTimeTF 		= null;
	public JTextField 	wellTrackTF 	= null;
	private int 		tTimeFileType	= 1; 	// 0 csv
	private int 		velFileType 	= 1; 	// 0 csv
	private int 		wellTrackFileType = 1; 	// 0 csv
	
	public int 			iUnit 			= 1;	// 0 ft; 1 m 2 km
	public int 			iId				= 30;	// 20 2D; 30 3D

	private String 		cwd 			= null;

	public ProjectDialog(JFrame aParent, String aTitle, boolean modal, String cwd) {
		super(aParent, aTitle, modal);
		setDialogWindowSize(700, 450);
		this.frame 		= (VcFrame)aParent;
		this.cwd 		= cwd;
	}
	
	protected JPanel createContents() {
		JPanel innerPanel = new JPanel(new BorderLayout());
		ProjectVc 		prevProject 		= null;
		ArrayList<String> files = frame.getPreferences().getRecentFileList();
		if(files!=null&&files.size()>0) {
			String projectName = files.get(0);
			if(projectName!=null) {
				File file = new File(projectName);
				if(file.exists()) {
					prevProject = new ProjectVc(projectName);
				}
			}
		}

		if(prevProject!=null) {
			iUnit = prevProject.getIUnit();
			iId = prevProject.getIId();
			LoadFilePanel panel = new LoadFilePanel(prevProject.getVelFileName(), prevProject.getTTimeFileName(), 
					prevProject.getWellTrackFileName());
			innerPanel.add(panel, BorderLayout.CENTER);
			return innerPanel;
		}
		String cwd = System.getProperty("user.dir");
		//cwd = "C:\\PINN_DATA\\BlackHills";
		LoadFilePanel panel = new LoadFilePanel(cwd, cwd, cwd);
		innerPanel.add(panel, BorderLayout.CENTER);
		return innerPanel;
	}	

	public static String [] parseFileName(String pckFileName) {
		if(pckFileName==null||pckFileName.isEmpty()) return null;
		StringTokenizer st = new StringTokenizer(pckFileName, ";");
		String [] fileName = new String[st.countTokens()];
		for(int i=0; i<fileName.length; i++) {
			fileName[i] = st.nextToken().trim();
		}
		return fileName;
	}

	protected boolean okAction() {
		boolean inValid = false;
		String velFileName = velTF.getText().trim();
		if(velFileName==null || velFileName.isEmpty()) inValid = true;
		File file = new File(velFileName);
		if(!file.isFile()) inValid = true;
		
		String tTimeFileName = tTimeTF.getText().trim();
		if(tTimeFileName==null || tTimeFileName.isEmpty()) inValid = true;
		file = new File(tTimeFileName);
		if(!file.isFile()) inValid = true;
		
		String wellTrackFileName = wellTrackTF.getText().trim();
		if(wellTrackFileName==null || wellTrackFileName.isEmpty()) wellTrackFileName = null;
		if(wellTrackFileName!=null) {
			file = new File(wellTrackFileName);
			if(!file.isFile()) wellTrackFileName = null;
		}
		
		if(inValid) {
			JOptionPane.showMessageDialog(frame, "Velocity or travel time input is empty", 
					"Error", JOptionPane.ERROR_MESSAGE );
			return false;
		}
		
		genProject(null, frame, iId, iUnit, velFileType, velFileName, tTimeFileType, tTimeFileName, 
				wellTrackFileType, wellTrackFileName);
		dispose();

		return true;
	}

	public static void genProject(String projectName, VcFrame frame, int iId, int iUnit, 
			int velFileType, String velFileName, int tTimeFileType, String tTimeFileName ,
			int wellTrackFileType, String wellTrackFileName) {

		String fullPath = FilenameUtils.getFullPath(velFileName);
		if(projectName==null) projectName = fullPath+"untitled.prj3d";
		frame.createProject(projectName, iId, iUnit, velFileType, velFileName, 
				tTimeFileType, tTimeFileName, wellTrackFileType, wellTrackFileName);

		frame.getProject().readVelocityFile();

//		String errorMsg = frame.getProject().getDipLayer1D().validate(); 
//		if(errorMsg!=null) {
//			JOptionPane.showMessageDialog(frame, errorMsg, "Error", JOptionPane.ERROR_MESSAGE );
//		}
		frame.getProject().readTTimeFile();
		frame.getProject().readWellTrackFile();
		
		frame.getProject().calAzimuthDipCenterNE();
		
		frame.getProject().setVCPair2D(frame.getProject().calVCPair2D());
		frame.getProject().setVCPair3D(frame.getProject().calVCPair3D());
		
		frame.getProject().setLayer2D(frame.getProject().calLayer2D());
		frame.getProject().setLayer3D(frame.getProject().calLayer3D());
		//frame.getProject().generateLayer2D3D();
		frame.newRayTracer();
		frame.updateWorld(true);
	}

	private class LoadFilePanel extends JPanel {
		private String 	localRoot = System.getProperty("user.dir");

		public LoadFilePanel(String mdlCWD, String pckCWD, String wellTrackCWD) {
			setLayout(new GridBagLayout());
			Insets insets = new Insets(5, 5, 5, 5);
			GridBagConstraints gbc;
			JButton browserButton = null;

			localRoot = mdlCWD;

			String [] moduleString = null;
			int n = 1;
			ButtonGroup moduleRadioGroup = null;
			JRadioButton [] moduleRadioButton = null;

			Font myFont = new Font("SansSerif", Font.PLAIN, 12);
			Color myColor = Color.BLUE;
			JPanel modulePanel = new JPanel( new GridBagLayout());

			int iRow = 0;

			moduleString = new String[]{"2D projection line with azimuth (-1 for auto regression, otherwise 0-180):", "3D"};
			n = moduleString.length;
			moduleRadioGroup = new ButtonGroup();
			moduleRadioButton = new JRadioButton[n];
			for(int i=0; i<n; i++) {
				final int j = 10+10*(i+1);	
				moduleRadioButton[i] = new JRadioButton(moduleString[i], iId==j);
				moduleRadioButton[i].addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent event) {
						if(((JRadioButton)event.getSource()).isSelected()) iId = j; 
					}
				});
				moduleRadioGroup.add(moduleRadioButton[i]);
				//modulePanel.add(moduleRadioButton[i]);
			}
			
			int k = 0;
			gbc= new GridBagConstraints(k++, 0, 1, 1, 1.0, 1.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, insets, 0, 0);
			modulePanel.add(moduleRadioButton[1], gbc);
			gbc= new GridBagConstraints(k++, 0, 1, 1, 1.0, 1.0, GridBagConstraints.EAST, GridBagConstraints.BOTH, insets, 0, 0);
			modulePanel.add(moduleRadioButton[0], gbc);		
			
			modulePanel.setBorder(BorderFactory.createTitledBorder(null, "Module", 
					TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, myFont, myColor));
			gbc= new GridBagConstraints(0, iRow, 5, 1, 1.0, 1.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, insets, 0, 0);
			add(modulePanel, gbc);

			moduleString = new String[] {"feet", "meters", "km"};
			n = moduleString.length;
			modulePanel = new JPanel( new GridLayout(1, n, 5, 2));
			moduleRadioGroup = new ButtonGroup();
			moduleRadioButton = new JRadioButton[n];

			for(int i=0; i<n; i++) {
				final int j = i;
				moduleRadioButton[i] = new JRadioButton(moduleString[i], iUnit==j);
				moduleRadioButton[i].addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent event) {
						if(((JRadioButton)event.getSource()).isSelected()) iUnit = j; 
					}
				});
				moduleRadioGroup.add(moduleRadioButton[i]);
				gbc= new GridBagConstraints(i+1, iRow, 1, 1, 1.0, 1.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, insets, 0, 0);
				modulePanel.add(moduleRadioButton[i], gbc);
			}
			iRow++;
			modulePanel.setBorder(BorderFactory.createTitledBorder(null, "Distance Unit", 
					TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, myFont, myColor));
			gbc= new GridBagConstraints(0, iRow, 5, 1, 1.0, 1.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, insets, 0, 0);
			add(modulePanel, gbc);
			
			iRow++;
			gbc= new GridBagConstraints(0, iRow, 1, 1, 1.0, 1.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, insets, 0, 0);
			add(new JLabel("Velocity Model File:"), gbc);

			moduleString = new String[] {"*.csv file", "*.xlsx file"};
			n = moduleString.length;
			moduleRadioGroup = new ButtonGroup();
			moduleRadioButton = new JRadioButton[n];

			for(int i=0; i<n; i++) {
				final int j = i;
				moduleRadioButton[i] = new JRadioButton(moduleString[i], velFileType==j);
				moduleRadioButton[i].addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent event) {
						if(((JRadioButton)event.getSource()).isSelected()) velFileType = j; 
					}
				});
				moduleRadioGroup.add(moduleRadioButton[i]);
				gbc= new GridBagConstraints(i+1, iRow, 1, 1, 1.0, 1.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, insets, 0, 0);
				add(moduleRadioButton[i], gbc);
			}

			velTF = new JTextField (mdlCWD, 5);
			browserButton = new JButton("Browse");
			browserButton.addActionListener(e -> {
				String fileName = velTF.getText().trim();
				if(fileName.isEmpty()) fileName = localRoot.trim();  

				FileNameExtensionFilter [] exts = null;
				if(velFileType==0) { exts = new FileNameExtensionFilter [] { new FileNameExtensionFilter("CSV (*.csv)", "csv") }; }
				else { exts = new FileNameExtensionFilter [] { new FileNameExtensionFilter("Excel (*.xlsx)", "xlsx") }; }

				String selectedFileName = frame.openFileUsingJFileChooser(exts, fileName); 

				if(selectedFileName==null) return;
				else { localRoot = selectedFileName.trim(); velTF.setText(localRoot); }				
			});
			iRow++;
			gbc= new GridBagConstraints(0, iRow, 4, 1, 1.0, 1.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, insets, 0, 0);
			add(velTF, gbc);
			gbc= new GridBagConstraints(4, iRow++, 1, 1, 0.0, 1.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, insets, 0, 0);
			add(browserButton, gbc);

			iRow++;
			gbc= new GridBagConstraints(0, iRow, 1, 1, 1.0, 1.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, insets, 0, 0);
			add(new JLabel("Travel Time File:"), gbc);

			moduleString = new String[] {"*.csv file", "*.xlsx file"};
			n = moduleString.length;
			moduleRadioGroup = new ButtonGroup();
			moduleRadioButton = new JRadioButton[n];

			for(int i=0; i<n; i++) {
				final int j = i;
				moduleRadioButton[i] = new JRadioButton(moduleString[i], tTimeFileType==j);
				moduleRadioButton[i].addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent event) {
						if(((JRadioButton)event.getSource()).isSelected()) tTimeFileType = j; 
					}
				});
				moduleRadioGroup.add(moduleRadioButton[i]);
				gbc= new GridBagConstraints(i+1, iRow, 1, 1, 1.0, 1.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, insets, 0, 0);
				add(moduleRadioButton[i], gbc);
			}


			tTimeTF = new JTextField (pckCWD, 5);
			browserButton = new JButton("Browse");
			browserButton.addActionListener(e -> {
				String fileName = tTimeTF.getText().trim();
				if(fileName.isEmpty()) fileName = localRoot.trim();  

				FileNameExtensionFilter [] exts = null;
				if(tTimeFileType==0) { exts = new FileNameExtensionFilter [] { new FileNameExtensionFilter("CSV (*.csv)", "csv") }; }
				else { exts = new FileNameExtensionFilter [] { new FileNameExtensionFilter("Excel (*.xlsx)", "xlsx") }; }

				String selectedFileName = frame.openFileUsingJFileChooser(exts, fileName); 

				if(selectedFileName==null) return;
				else { localRoot = selectedFileName.trim(); tTimeTF.setText(localRoot); }
			});
			iRow++;
			gbc= new GridBagConstraints(0, iRow, 4, 1, 1.0, 1.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, insets, 0, 0);
			add(tTimeTF, gbc);
			gbc= new GridBagConstraints(4, iRow++, 1, 1, 0.0, 1.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, insets, 0, 0);
			add(browserButton, gbc);
			
			iRow++;
			gbc= new GridBagConstraints(0, iRow, 1, 1, 1.0, 1.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, insets, 0, 0);
			add(new JLabel("<html>Well Track File (<font color='blue'>optional</font>):</html>"), gbc);

			moduleString = new String[] {"*.csv file", "*.xlsx file"};
			n = moduleString.length;
			moduleRadioGroup = new ButtonGroup();
			moduleRadioButton = new JRadioButton[n];

			for(int i=0; i<n; i++) {
				final int j = i;
				moduleRadioButton[i] = new JRadioButton(moduleString[i], wellTrackFileType==j);
				moduleRadioButton[i].addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent event) {
						if(((JRadioButton)event.getSource()).isSelected()) wellTrackFileType = j; 
					}
				});
				moduleRadioGroup.add(moduleRadioButton[i]);
				gbc= new GridBagConstraints(i+1, iRow, 1, 1, 1.0, 1.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, insets, 0, 0);
				add(moduleRadioButton[i], gbc);
			}


			wellTrackTF = new JTextField (wellTrackCWD, 5);
			browserButton = new JButton("Browse");
			browserButton.addActionListener(e -> {
				String fileName = wellTrackTF.getText().trim();
				if(fileName.isEmpty()) fileName = localRoot.trim();  

				FileNameExtensionFilter [] exts = null;
				if(wellTrackFileType==0) { exts = new FileNameExtensionFilter [] { new FileNameExtensionFilter("CSV (*.csv)", "csv") }; }
				else { exts = new FileNameExtensionFilter [] { new FileNameExtensionFilter("Excel (*.xlsx)", "xlsx") }; }

				String selectedFileName = frame.openFileUsingJFileChooser(exts, fileName); 

				if(selectedFileName==null) return;
				else { localRoot = selectedFileName.trim(); wellTrackTF.setText(localRoot); }
			});
			iRow++;
			gbc= new GridBagConstraints(0, iRow, 4, 1, 1.0, 1.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, insets, 0, 0);
			add(wellTrackTF, gbc);
			gbc= new GridBagConstraints(4, iRow++, 1, 1, 0.0, 1.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, insets, 0, 0);
			add(browserButton, gbc);

		}

	}

}



