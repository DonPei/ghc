package org.ucdm.launcher.dialog;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.apache.commons.io.FilenameUtils;
import org.ucdm.common.CommonDialog;
import org.ucdm.core.SeisPTSrcMech;
import org.ucdm.core.SeisPTVel;
import org.ucdm.core.acquisition.VCPair;
import org.ucdm.core.planarmodel.DipLayer1D;
import org.ucdm.excel.ReceiverGeometryExcel;
import org.ucdm.launcher.LauncherFrame;
import org.ucdm.rt.pseudo3d.Pseudo3DRayTracer;
import org.ucdm.xml.mdl.MdlXml;


public class ReceiverGeometryDialog  extends CommonDialog {
	LauncherFrame 		_frame 		= null;

	public JTextField _recGeoTF 	= null;
	public JTextField _mdlGeoTF 	= null;
	public JTextField _sampleIntervalTF 	= null;
	
	private int 		_ttFileType	= 2; 	// 0 for srcMech Perf 1 for srcMech Event 2 iVC
	private int 		_velFileType= 2; 	// 0 for SeisPT Vel Xml 1 for SeisptVel 2 iVC
	public int 			_iUnit 		= 2;		//1 m; 2 ft; 3 km
	public int 			_iModule 	= 0;		//0 full function; 1 recording geometry from .xlsx to .csv; 2 .csv to srcmech

	public ReceiverGeometryDialog(JFrame aParent, String aTitle, boolean modal, int iModule) {
		super(aParent, aTitle, modal);
		if(_iModule==0) setDialogWindowSize(700, 370);
		else if(_iModule==1) setDialogWindowSize(700, 250);
		setOKButtonLabel("Save As...");
		
		_frame 	= (LauncherFrame)aParent;
		_iModule = iModule;
	}

	protected JPanel createContents() {
		JPanel innerPanel = new JPanel();

		innerPanel.setLayout(new GridBagLayout());
		Insets insets = new Insets(5, 5, 5, 5);
		GridBagConstraints gbc= null;

		gbc= new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0,
				GridBagConstraints.WEST, GridBagConstraints.BOTH, insets, 0, 0);
		//String src = "C:\\PINN_DATA\\Marathon\\Forshage\\FH1H Stage 15 Extracted\\SrcMech\\SrcMechResults_FullMT.csv";

		if(_frame!=null) {
			String recGeoCwd  = _frame.getRecGeoCwd();
			String mdlGeoCwd  = _frame.getMdlGeoCwd();
			LoadFilePanel loadFilePanel = new LoadFilePanel(recGeoCwd, mdlGeoCwd);
			innerPanel.add(loadFilePanel, gbc);
		}

		return innerPanel;
	}
	
	private void startRayTracing(DipLayer1D dipLayer1D, VCPair vcPW) {
		Pseudo3DRayTracer rt = new Pseudo3DRayTracer(null, dipLayer1D, vcPW, 
				1, 0, 0, null, null, 1, 0.01, 0, 0, 2);
			rt.setIMethod(1);
			rt.setIVp(1); rt.setConfiguration(); rt.start(0);
			rt.setIVp(2); rt.setConfiguration(); rt.start(0);
			rt.setIVp(3); rt.setConfiguration(); rt.start(0);
			
			int iT0 = rt.getIT0();
			if(iT0>0) {
				if(iT0==1)  {
					vcPW.estimateT0(1, 0, 0);
					vcPW.applyT0(1, 0, 0);
				} else if(iT0==2)  {
					vcPW.estimateT0(0, 1, 0);
					vcPW.applyT0(0, 1, 0);
				} else if(iT0==3)  {
					vcPW.estimateT0(0, 0, 1);
					vcPW.applyT0(0, 0, 1);
				} else if(iT0==11)  {
					vcPW.estimateT0(1, 1, 0);
					vcPW.applyT0(1, 1, 0);
				} else if(iT0==111)  {
					vcPW.estimateT0(1, 1, 1);
					vcPW.applyT0(1, 1, 1);
				}
			}
	}
	
	protected boolean okAction() {
		String recGeoCwd  = _recGeoTF.getText().trim();
		File file = new File(recGeoCwd);
		if(file.exists()&&file.isFile()) {
			_frame.setRecGeoCwd(recGeoCwd);
			VCPair vcPW = null;
			String ext = FilenameUtils.getExtension(recGeoCwd);
			if(ext.equalsIgnoreCase("xlsx")) {
				ReceiverGeometryExcel receiverGeometryExcel = new ReceiverGeometryExcel(recGeoCwd);			
				vcPW = receiverGeometryExcel.toVCPair(_iUnit);
			} else {
				vcPW = new VCPair(1, recGeoCwd);
				vcPW.setUnit(_iUnit);
			}
			
			
			String mdlFileName  = _mdlGeoTF.getText().trim();
			file = new File(mdlFileName);
			if(file.exists()&&file.isFile()) {
				_frame.setMdlGeoCwd(mdlFileName);
				
				DipLayer1D dipLayer1D = null;
				if(_velFileType==0) {
					MdlXml mdlXml = new MdlXml(mdlFileName);
					dipLayer1D = mdlXml.toDipLayer1D();
				} else if(_velFileType==1) {
					SeisPTVel seisptVel = new SeisPTVel(mdlFileName, 1);
					dipLayer1D = seisptVel.toDipLayer1D(null);
				} else {
					dipLayer1D = new DipLayer1D(mdlFileName);	
				}
				startRayTracing(dipLayer1D, vcPW);
			}
			
			//System.out.println(vcPW.toString());
			save(vcPW);
		}		
		return true;
	}
	
	public void save(VCPair vcPW)	{ 
		String ext = null;
		FileNameExtensionFilter exts[] = null;
		String a = _frame.getRecGeoCwd();
		String baseName = null;
		if(_ttFileType==0||_ttFileType==1) {
			ext = "txt";
			exts = new FileNameExtensionFilter [] { new FileNameExtensionFilter("TXT (*.txt)", ext) };
			baseName = FilenameUtils.getFullPath(a)+FilenameUtils.getBaseName(a)+"_SrcMech."+ext;
		} else if(_ttFileType==2) {
			ext = "csv";
			exts = new FileNameExtensionFilter [] { new FileNameExtensionFilter("CSV (*.csv)", ext) };
			baseName = FilenameUtils.getFullPath(a)+FilenameUtils.getBaseName(a)+"."+ext;
		}		
		
		String fileName = _frame.saveFileUsingJFileChooser(exts, baseName); 
		if(fileName==null) return;
		else {
			String selectedExt = FilenameUtils.getExtension(fileName);
			if(selectedExt==null||selectedExt.isEmpty()) fileName = fileName+"."+ext;			
			if(_ttFileType==0) {
				double sampleInterval = Double.parseDouble(_sampleIntervalTF.getText().trim());
				SeisPTSrcMech seisPTSrcMech = new SeisPTSrcMech(sampleInterval, vcPW);
				seisPTSrcMech.write(false, fileName);
			} else if(_ttFileType==1) {
				double sampleInterval = Double.parseDouble(_sampleIntervalTF.getText().trim());
				SeisPTSrcMech seisPTSrcMech = new SeisPTSrcMech(sampleInterval, vcPW);
				seisPTSrcMech.write(false, fileName);
			} else if(_ttFileType==2) {
				vcPW.write(false, fileName);
			}
			
			_frame.savePreferences();
		}
	}	

	private class LoadFilePanel extends JPanel {

		public LoadFilePanel(String recGeoCwd, String mdlGeoCwd) {
			setLayout(new GridBagLayout());
			Insets insets = new Insets(5, 5, 5, 5);
			GridBagConstraints gbc;			

			_recGeoTF = new JTextField (recGeoCwd, 5);
			_mdlGeoTF = new JTextField (mdlGeoCwd, 5);
			
			int iRow = 0;
			int n = 0;
			ButtonGroup moduleRadioGroup = null;
			String [] moduleString = null;
			String [] moduleToolTip = null;
			JRadioButton [] moduleRadioButton = null;
			JButton browserButton = null;
			
			gbc= new GridBagConstraints(0, iRow, 1, 1, 1.0, 1.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, insets, 0, 0);
			add(new JLabel("Input File (Excel or CSV):"), gbc);
			
			browserButton = new JButton("Browse"); 
			browserButton.addActionListener(new ActionListener()	{
				public void actionPerformed(ActionEvent e)  {
					FileNameExtensionFilter [] exts =  new FileNameExtensionFilter [] { 
							new FileNameExtensionFilter("Excel (*.xlsx)", "xlsx"), 
							new FileNameExtensionFilter("CSV (*.csv)", "csv")};

					String fileName = _recGeoTF.getText();
					String selectedFileName = _frame.openFileUsingJFileChooser(exts, fileName);

					if(selectedFileName==null) return;
					else _recGeoTF.setText(selectedFileName.trim());
				}
			});
			
			iRow++;
			gbc= new GridBagConstraints(0, iRow, 4, 1, 1.0, 1.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, insets, 0, 0);
			add(_recGeoTF, gbc);
			gbc= new GridBagConstraints(4, iRow, 1, 1, 0.0, 1.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, insets, 0, 0);
			add(browserButton, gbc);
			
			iRow++;
			gbc= new GridBagConstraints(0, iRow, 1, 1, 1.0, 1.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, insets, 0, 0);
			add(new JLabel("Distance Unit:"), gbc);

			moduleString = new String[]{"meters", "feet"};
			n = moduleString.length;
			moduleRadioGroup = new ButtonGroup();
			moduleRadioButton = new JRadioButton[n];
			for(int i=0; i<n; i++) {
				final int j = i;
				moduleRadioButton[i] = new JRadioButton(moduleString[i], _iUnit==(i+1));
				moduleRadioButton[i].addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent event) {
						if(((JRadioButton)event.getSource()).isSelected()) _iUnit = (j+1); 
					}
				});
				moduleRadioGroup.add(moduleRadioButton[i]);
				gbc= new GridBagConstraints(1+i, iRow, 1, 1, 1.0, 1.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, insets, 0, 0);
				add(moduleRadioButton[i], gbc);
			}
			
			iRow++;
			gbc= new GridBagConstraints(0, iRow, 5, 1, 1.0, 1.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, insets, 0, 0);
			//add(new JLabel(""), gbc);
			add(new JSeparator(JSeparator.HORIZONTAL), gbc);
			
			
			iRow++;
			gbc= new GridBagConstraints(0, iRow, 1, 1, 1.0, 1.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, insets, 0, 0);
			//add(new JLabel("Excel File:"), gbc);
			add(new JLabel("<html>3D Velocity File (<font color='blue'>optional</font>):</html>"), gbc);
			
			moduleString = new String[]{"SeisPT Velocity XML", "SeisPTVel TXT", "CSV"};
			moduleToolTip = new String[]{"*.mdl.xml SeisPT velocity file",
					".txt file exported from SeisPT", "CSV format file"};
			n = moduleString.length;
			moduleRadioGroup = new ButtonGroup();
			moduleRadioButton = new JRadioButton[n];
			for(int i=0; i<n; i++) {
				final int j = i;
				moduleRadioButton[i] = new JRadioButton(moduleString[i], _velFileType==i);
				moduleRadioButton[i].setToolTipText(moduleToolTip[i]);
				moduleRadioButton[i].addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent event) {
						if(((JRadioButton)event.getSource()).isSelected()) _velFileType = j; 
					}
				});
				moduleRadioGroup.add(moduleRadioButton[i]);
				gbc= new GridBagConstraints(1+i, iRow, 1, 1, 1.0, 1.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, insets, 0, 0);
				add(moduleRadioButton[i], gbc);
			}
			
			browserButton = new JButton("Browse"); 
			browserButton.addActionListener(new ActionListener()	{
				public void actionPerformed(ActionEvent e)  {
					FileNameExtensionFilter [] exts = null;
					if(_velFileType==0) { exts = new FileNameExtensionFilter [] { new FileNameExtensionFilter("XML (*.par.xml)", "xml") }; }
					else if(_velFileType==1) { exts = new FileNameExtensionFilter [] { new FileNameExtensionFilter("Text (*.txt)", "txt") }; }
					else { exts = new FileNameExtensionFilter [] { new FileNameExtensionFilter("CSV (*.csv)", "csv") }; }
					
					String fileName = _mdlGeoTF.getText();
					String selectedFileName = _frame.openFileUsingJFileChooser(exts, fileName);

					if(selectedFileName==null) return;
					else _mdlGeoTF.setText(selectedFileName.trim());
				}
			});
			
			iRow++;
			gbc= new GridBagConstraints(0, iRow, 4, 1, 1.0, 1.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, insets, 0, 0);
			add(_mdlGeoTF, gbc);
			gbc= new GridBagConstraints(4, iRow, 1, 1, 0.0, 1.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, insets, 0, 0);
			add(browserButton, gbc);
			
			iRow++;
			gbc= new GridBagConstraints(0, iRow, 1, 1, 1.0, 1.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, insets, 0, 0);
			add(new JLabel("Save As:"), gbc);

			moduleString = new String[]{"SrcMech Perf", "SrcMech Event", "CSV"};
			moduleToolTip = new String[]{"SeisPT exported *.txt file. Perf event only. May or may not have perf timing",
					"SeisPT exported *.txt file. MS event only", "CSV format file"};
			n = moduleString.length;
			moduleRadioGroup = new ButtonGroup();
			moduleRadioButton = new JRadioButton[n];
			for(int i=0; i<n; i++) {
				final int j = i;
				moduleRadioButton[i] = new JRadioButton(moduleString[i], _ttFileType==i);
				moduleRadioButton[i].setToolTipText(moduleToolTip[i]);
				moduleRadioButton[i].addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent event) {
						if(((JRadioButton)event.getSource()).isSelected()) _ttFileType = j; 
					}
				});
				moduleRadioGroup.add(moduleRadioButton[i]);
				gbc= new GridBagConstraints(1+i, iRow, 1, 1, 1.0, 1.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, insets, 0, 0);
				add(moduleRadioButton[i], gbc);
			}
			
			_sampleIntervalTF = new JTextField("0.00025");
			iRow++;
			gbc= new GridBagConstraints(0, iRow, 1, 1, 1.0, 1.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, insets, 0, 0);
			add(new JLabel("Sample Interval (s):"), gbc);
			gbc= new GridBagConstraints(1, iRow, 1, 1, 1.0, 1.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, insets, 0, 0);
			add(_sampleIntervalTF, gbc);
		}

		
	}

}

