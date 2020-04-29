package org.ucdm.launcher.dialog;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
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
import org.ucdm.common.CommonDialog;
import org.ucdm.common.ProjectMsa;
import org.ucdm.common.WellTrack;
import org.ucdm.core.acquisition.VCPair;
import org.ucdm.launcher.LauncherFrame;
import org.ucdm.msa.MsaDataContext;

import edu.mines.jtk.util.ArrayMath;


public class DASReceiverGeometryDialog  extends CommonDialog {
	LauncherFrame 	_frame 			= null;

	public JTextField _setupTF 		= null;
	public JTextField _eventTF 		= null;
	public JTextField _gaugeLengthTF= null;
	public JTextField _topDepthTF 	= null;
	public JTextField _e0IndexInclusiveTF 	= null;
	public JTextField _e1IndexInclusiveTF 	= null;
	public int 			_iUnit 		= 2;	// 1 m; 2 ft; 3 km

	public DASReceiverGeometryDialog(JFrame aParent, String aTitle, boolean modal) {
		super(aParent, aTitle, modal);
		setDialogWindowSize(1000, 300);
		//setOKButtonLabel("Cal");
		_frame 			= (LauncherFrame)aParent;
	}

	protected JPanel createContents() {
		JPanel innerPanel = new JPanel(new BorderLayout());

		String cwd = System.getProperty("user.dir");
		String eventCwd = "C:\\PINN_DATA\\Pioneer\\2018NE_Schabauer\\Phase1\\EVENTS_1125Stage30.csv";
		//String setupCwd = "C:\\PINN_DATA\\Pioneer\\2018NE_Schabauer\\Phase1\\setup_Trimmed.xlsx";
		String setupCwd = "C:\\PINN_DATA\\Pioneer\\2018NE_Schabauer\\Phase1\\setup_1125_1127.xlsx";
		LoadFilePanel panel = new LoadFilePanel(eventCwd, setupCwd);
		innerPanel.add(panel, BorderLayout.CENTER);
		return innerPanel;
	}	

	protected static String [] parseFileName(String pckFileName) {
		if(pckFileName==null||pckFileName.isEmpty()) return null;
		StringTokenizer st = new StringTokenizer(pckFileName, ";");
		String [] fileName = new String[st.countTokens()];
		for(int i=0; i<fileName.length; i++) {
			fileName[i] = st.nextToken().trim();
		}
		return fileName;
	}
	protected boolean okAction() {
		String setupFileName 		= _setupTF.getText().trim();
		if(setupFileName==null || setupFileName.isEmpty()) setupFileName = null;
		String eventFileName 		= _eventTF.getText().trim();
		if(eventFileName==null || eventFileName.isEmpty()) eventFileName = null;

		double gaugeLength = Double.parseDouble(_gaugeLengthTF.getText().trim());
		double topDepth = Double.parseDouble(_topDepthTF.getText().trim());
		
		int e0Inclusive = Integer.parseInt(_e0IndexInclusiveTF.getText().trim());
		int e1Inclusive = Integer.parseInt(_e1IndexInclusiveTF.getText().trim());
		process(null, 120, _iUnit, eventFileName, setupFileName, gaugeLength, topDepth, e0Inclusive, e1Inclusive);
		
		return true;
	}
	
	public void process(String projectFileName, int iId, int iUnit, String eventFileName, String setupFileName, 
			double gaugeLength, double topDepth, int e0Inclusive, int e1Inclusive) {
		String fullPath = FilenameUtils.getFullPath(eventFileName);
		if(projectFileName==null) projectFileName = fullPath+"untitled.prjmsa";
		ProjectMsa project = new ProjectMsa(projectFileName, iId, iUnit, eventFileName, setupFileName, null, null);
		project.setProjectName(projectFileName);
		
		project.readTerraVistaExcel();
		project.readEventCsv();
		project.genTerraVistaColumn();
		project.genStageAttribute();	
		
		project.genMsaDataContext();
		//project.processMsaDataContext();
		//project.getMsaDataContext().convertToStageTime();
		
		MsaDataContext msaDataContext = project.getMsaDataContext();
		//msaDataContext.printClass1();
		float [][][][] enabledData = msaDataContext.getData();
		for(int i=0, k=0; i<enabledData.length; i++) {
			for(int j=0; j<enabledData[i].length; j++, k++) {
				float [][] data = enabledData[i][j];
				float [] N = data[0];
				float [] E = data[1];
				float [] D = data[2];
				float [] mag = data[3];
//				System.out.println("nRow="+data.length+" "+"nCol="+data[0].length+" ");
//				for(int ik=0; ik<10; ik++) {
//					System.out.println(N[ik]+" "+E[ik]+" "+D[ik]+" "+mag[ik]+" ");
//				}
			}
		}
		
		WellTrack [] wellTracks = project.getWellTracks();
		WellTrack [] dasWellTrack = new WellTrack[wellTracks.length];
		for(int i=0; i<wellTracks.length; i++) {
			wellTracks[i].trucateUp(topDepth);
			//wellTracks[i].printClass();
			dasWellTrack[i] = wellTracks[i].uniformSample(gaugeLength);
			//dasWellTrack[i].printClass();
		}		
		
		fullPath 			= FilenameUtils.getFullPath(eventFileName)+"Synthetics"+File.separator;	
		String baseName 	= FilenameUtils.getBaseName(eventFileName);
		String fileName 	= fullPath+baseName+"_DAS_Receivers.csv";	
		
		FileNameExtensionFilter exts[] = new FileNameExtensionFilter [] {
				new FileNameExtensionFilter("(*.csv)", "csv") };
		String selectedFileName = _frame.saveFileUsingJFileChooser(exts, fileName); 
		if(selectedFileName!=null) {
			String selectedExt = FilenameUtils.getExtension(fileName);
			if(selectedExt==null||selectedExt.isEmpty()) selectedFileName = selectedFileName+".csv";
			writeGeometry(false, selectedFileName, enabledData[0][0], dasWellTrack, e0Inclusive, e1Inclusive);
		}
		//fileName 	= fullPath+baseName+"_Model01.csv";		
		//writeModel(fileName, enabledData[0][0], dasWellTrack);
	}
	
	private void writeModel(String outputFileName, float [][] eventData, WellTrack [] dasWellTrack) {
		try{
			float [] eventN = eventData[0];
			float [] eventE = eventData[1];
			float [] eventD = eventData[2];
			float [][] eventMinMax = new float[][] {
				{ArrayMath.min(eventN), ArrayMath.min(eventE), ArrayMath.min(eventD) },
				{ArrayMath.max(eventN), ArrayMath.max(eventE), ArrayMath.max(eventD) }
			};
			
			float[] wN = dasWellTrack[0].getX();
			float[] wE = dasWellTrack[0].getY();
			float[] wD = dasWellTrack[0].getZ();			
			float [][] gemetryMinMax = new float[][] {
				{ArrayMath.min(wN), ArrayMath.min(wE), ArrayMath.min(wD) },
				{ArrayMath.max(wN), ArrayMath.max(wE), ArrayMath.max(wD) }
			};
			
			for(int ik=1; ik<dasWellTrack.length; ik++) {
				float [][] w = new float[][]{dasWellTrack[ik].getX(), dasWellTrack[ik].getY(), dasWellTrack[ik].getZ()};
				for(int i=0; i<3; i++) gemetryMinMax[0][i] = ArrayMath.min(ArrayMath.min(w[i]), gemetryMinMax[0][i]);
				for(int i=0; i<3; i++) gemetryMinMax[1][i] = ArrayMath.max(ArrayMath.max(w[i]), gemetryMinMax[1][i]);
			}
			
			float [][] minMax = new float[2][3];
			for(int i=0; i<3; i++) minMax[0][i] = ArrayMath.min(eventMinMax[0][i], gemetryMinMax[0][i]);
			for(int i=0; i<3; i++) minMax[1][i] = ArrayMath.max(eventMinMax[1][i], gemetryMinMax[1][i]);
			
			float [] d = new float[3];
			for(int i=0; i<3; i++) d[i] = minMax[1][i]-minMax[0][i];
			
			float x0 = minMax[0][0] - 0.05f*d[0];
			float y0 = minMax[0][1] - 0.05f*d[1];
			float z0 = minMax[0][2] - 0.05f*d[2];
			float x1 = minMax[1][0] + 0.05f*d[0];
			float y1 = minMax[1][1] + 0.05f*d[1];
			float z1 = minMax[1][2] + 0.05f*d[2];
			int nx = 101;
			int ny = 101;
			int nz = 101;
			float dx = (x1-x0)/(nx-1);
			float dy = (y1-y0)/(ny-1);
			float dz = (z1-z0)/(nz-1);
			
			//System.out.println("x0="+x0+" y0="+y0+" z0="+z0+" x1="+x1+" y1="+y1+" z1="+z1);
			
			String a = "Northing/Azimuth, Easting/Dip, Down/None, Vp/X0, Vs/Nx, Density_kg_m3/Dx, Delta/Y0, Epsilon/Ny, Gamma/Dy, Qp/Nz, Qs/unit_1_m_2_ft";

			BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(outputFileName, false));
			bufferedWriter.write(a+"\n");
			a ="0, 0, "+ "0, "+x0+", "+nx+", "+ dx+ ", "+
					y0+", "+ny+", "+ dy+ ", "+nz+ ", "+_iUnit; 
			bufferedWriter.write(a+"\n");
			
			double vp = 14640.0;
			double vs = 8724.0;
			double den = 2500.0;
			double delta = 0.0;
			double epsilon = 0.0;
			double gamma = 0.0;
			double qp = 1.0e9;
			double qs = 1.0e9;
			double top = z0;
			double bottom = z1;
			
			a ="0, 0, "+top+ ", "+vp+ ", "+vs+ ", "+den+ ", "+
					delta+ ", "+epsilon+ ", "+gamma+ ", "+ qp+ ", "+qs;  
			bufferedWriter.write(a+"\n");
			a ="0, 0, "+top+ ", "+vp+ ", "+vs+ ", "+den+ ", "+
					delta+ ", "+epsilon+ ", "+gamma+ ", "+ qp+ ", "+qs; 
			
			a ="0, 0, "+bottom+ ", "+vp+ ", "+vs+ ", "+den+ ", "+
					delta+ ", "+epsilon+ ", "+gamma+ ", "+ qp+ ", "+qs;  
			bufferedWriter.write(a+"\n");			
			
			bufferedWriter.close();
		} catch (IOException ioexception) {
			JOptionPane.showMessageDialog(null, "\nFile Format is not right!", 
					"Error", JOptionPane.ERROR_MESSAGE);
		}
	}
	
	private void writeGeometry(boolean eventOnly, String outputFileName, float [][] eventData, WellTrack [] dasWellTrack, 
			int e0Inclusive, int e1Inclusive) {
		try{
			String a = null;
			if(_iUnit==1) {
				a = new String("Northing(ft), Easting(ft), Down(ft)");
			} else if(_iUnit==2) {
				a = new String("Northing(m), Easting(m), Down(m)");
			} else {
				a = new String("Northing(km), Easting(km), Down(km)");
			}
			a += new String(", OrgT/Obs_PT(s), OrgW/Obs_ST(s), Unit/Obs_SV(s), Cal_PT(s), Cal_ST(s), Cal_SV(s), Item_No/Phase");
			
			float [] eventN = eventData[0];
			float [] eventE = eventData[1];
			float [] eventD = eventData[2];
			float [] eventM = eventData[3];
			
			
			BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(outputFileName, false));
			bufferedWriter.write(a+"\n");
			int n = 0;
			//for(int i=200; i<201; i++) {
			for(int i=0; i<eventN.length; i++) {
				if(i>=e0Inclusive && i<=e1Inclusive) {
					a = String.format("%.2f, %.2f, %.2f, 0, 0, %d, %.6f, 0, 0, %d", 
							eventN[i], eventE[i], eventD[i], _iUnit, eventM[i], 1000001+n); 
					bufferedWriter.write(a+"\n");

					if(eventOnly) continue;
					int k = 0;
					for(int ik=0; ik<dasWellTrack.length; ik++) {
						float[] wN = dasWellTrack[ik].getX();
						float[] wE = dasWellTrack[ik].getY();
						float[] wD = dasWellTrack[ik].getZ();
						for(int j=0; j<wN.length; j++, k++) {
							a = String.format("%.2f, %.2f, %.2f, 0, 0, 0, 0, 0, 0, %d", 
									wN[j], wE[j], wD[j], k); 
							bufferedWriter.write(a+"\n");
						}
					}
					n++;
				}
			}
			bufferedWriter.close();
			System.out.println("e0Inclusive="+e0Inclusive+" e1Inclusive="+e1Inclusive+" N="+eventN.length);
			
		} catch (IOException ioexception) {
			JOptionPane.showMessageDialog(null, "\nFile Format is not right!", 
					"Error", JOptionPane.ERROR_MESSAGE);
		}
	}

	private class LoadFilePanel extends JPanel {

		public LoadFilePanel(String eventCWD, String setupCWD) {
			setLayout(new GridBagLayout());
			Insets insets = new Insets(5, 5, 5, 5);
			GridBagConstraints gbc;
			JButton browserButton = null;

			Font myFont = new Font("SansSerif", Font.PLAIN, 12);
			Color myColor = Color.BLUE;
			JPanel modulePanel = new JPanel( new GridLayout(1, 2, 5, 2));

			int n = 0;
			ButtonGroup moduleRadioGroup = null;
			String [] moduleString = null;
			String [] moduleToolTip = null;
			JRadioButton [] moduleRadioButton = null;


			int iRow = 0;
			modulePanel = new JPanel( new GridLayout(1, 2, 5, 2));

			moduleString = new String[]{"meters", "feet"};
			n = moduleString.length;
			moduleRadioGroup = new ButtonGroup();
			moduleRadioButton = new JRadioButton[n];			
			for(int i=0; i<n; i++) {
				final int j = i+1;
				moduleRadioButton[i] = new JRadioButton(moduleString[i], _iUnit==j);
				moduleRadioButton[i].addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent event) {
						if(((JRadioButton)event.getSource()).isSelected()) _iUnit = j; 
					}
				});

				moduleRadioGroup.add(moduleRadioButton[i]);
				gbc= new GridBagConstraints(1+i, iRow, 1, 1, 1.0, 1.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, insets, 0, 0);
				modulePanel.add(moduleRadioButton[i], gbc);
			}

			modulePanel.setBorder(BorderFactory.createTitledBorder(null, "Unit", 
					TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, myFont, myColor));
			gbc= new GridBagConstraints(0, iRow, 2, 1, 1.0, 1.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, insets, 0, 0);
			add(modulePanel, gbc);
			
			modulePanel = new JPanel();
			_gaugeLengthTF = new JTextField ("15.0", 5);
			modulePanel.add(_gaugeLengthTF);			
			modulePanel.setBorder(BorderFactory.createTitledBorder(null, "Gauge Length", 
					TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, myFont, myColor));
			gbc= new GridBagConstraints(2, iRow, 1, 1, 1.0, 1.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, insets, 0, 0);
			add(modulePanel, gbc);
			modulePanel = new JPanel();
			_topDepthTF = new JTextField ("7000.0", 5);
			modulePanel.add(_topDepthTF);			
			modulePanel.setBorder(BorderFactory.createTitledBorder(null, "Top Depth", 
					TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, myFont, myColor));
			gbc= new GridBagConstraints(3, iRow, 1, 1, 1.0, 1.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, insets, 0, 0);
			add(modulePanel, gbc);
			modulePanel = new JPanel();			
			_e0IndexInclusiveTF = new JTextField ("0", 5);
			_e1IndexInclusiveTF = new JTextField ("1", 5);
			modulePanel.add(new JLabel("From:"));
			modulePanel.add(_e0IndexInclusiveTF);
			modulePanel.add(new JLabel("To:"));
			modulePanel.add(_e1IndexInclusiveTF);
			modulePanel.setBorder(BorderFactory.createTitledBorder(null, "Event Indexes (both inclusive)", 
					TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, myFont, myColor));
			gbc= new GridBagConstraints(4, iRow, 1, 1, 1.0, 1.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, insets, 0, 0);
			add(modulePanel, gbc);	
			
			iRow++;
			gbc= new GridBagConstraints(0, iRow, 5, 1, 1.0, 1.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, insets, 0, 0);
			add(new JLabel("TerraVista Event CSV File:"), gbc);			

			_eventTF = new JTextField (eventCWD, 5);
			browserButton = new JButton("Browse"); 
			browserButton.addActionListener(new ActionListener()	{
				public void actionPerformed(ActionEvent e)  {
					FileNameExtensionFilter extension = new FileNameExtensionFilter("*.csv", "csv");					
					String selectedFile = _frame.openFileUsingJFileChooser(new FileNameExtensionFilter [] {extension}, 
							_eventTF.getText().trim());
					if(selectedFile==null) return;
					else { _eventTF.setText(selectedFile.trim()); }
				}
			});
			
			iRow++;
			gbc= new GridBagConstraints(0, iRow, 5, 1, 1.0, 1.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, insets, 0, 0);
			add(_eventTF, gbc);
			gbc= new GridBagConstraints(5, iRow++, 1, 1, 0.0, 1.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, insets, 0, 0);
			add(browserButton, gbc);
			
			iRow++;				
			gbc= new GridBagConstraints(0, iRow, 5, 1, 1.0, 1.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, insets, 0, 0);
			add(new JLabel("TerraVista Setup Excel File:"), gbc);

			_setupTF = new JTextField (setupCWD, 5);			
			browserButton = new JButton("Browse"); 
			browserButton.addActionListener(new ActionListener()	{
				public void actionPerformed(ActionEvent e)  {
					FileNameExtensionFilter extension = new FileNameExtensionFilter("*.xlsx", "xlsx");					
					String selectedFile = _frame.openFileUsingJFileChooser(new FileNameExtensionFilter [] {extension}, 
							_setupTF.getText().trim());
					if(selectedFile==null) return;
					else { _setupTF.setText(selectedFile.trim()); }
				}
			});
			
			iRow++;
			gbc= new GridBagConstraints(0, iRow, 5, 1, 1.0, 1.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, insets, 0, 0);
			add(_setupTF, gbc);
			gbc= new GridBagConstraints(5, iRow++, 1, 1, 0.0, 1.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, insets, 0, 0);
			add(browserButton, gbc);
			
			iRow++;
			gbc= new GridBagConstraints(0, iRow, 5, 1, 1.0, 1.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, insets, 0, 0);
			add(new JLabel(""), gbc);
		}

	}
}


