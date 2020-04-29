package org.ucdm.launcher.dialog;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileFilter;
import java.util.StringTokenizer;

import javax.swing.ButtonGroup;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.apache.commons.io.FilenameUtils;
import org.ucdm.common.CommonDialog;
import org.ucdm.core.SeisPTSrcMech;
import org.ucdm.launcher.LauncherFrame;
import org.ucdm.mti.beachball.FileListAccessory;
import org.ucdm.xml.inf.Event;
import org.ucdm.xml.inf.InfXml;
import org.ucdm.xml.map.MapXml;

public class ManipulateSrcMechDialog extends CommonDialog {
	LauncherFrame 		_frame 			= null;
	
	private JTextField 	_srcMechCombineTF 	= null;
	private int 		_isFileEvent 		= 0;
	private int 		_iFunction 			= 0; //0-combine multiple files 1-generate from *.inf.xml
	
	public ManipulateSrcMechDialog(JFrame aParent, String aTitle, boolean modal, int iFunction) {
		super(aParent, aTitle, modal);
		setDialogWindowSize(800, 220);
		setOKButtonLabel("Save As...");
		_frame 		= (LauncherFrame)aParent;
		_iFunction 	= iFunction;
	}

	protected JPanel createContents() {
		JPanel innerPanel = new JPanel();

		innerPanel.setLayout(new GridBagLayout());
		Insets insets = new Insets(5, 5, 5, 5);
		GridBagConstraints gbc= null;

		gbc= new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0,
				GridBagConstraints.WEST, GridBagConstraints.BOTH, insets, 0, 0);

		if(_frame!=null) {
			String srcMechCombineCwd  = _frame.getSrcMechCombineCwd();
			LoadFilePanel loadFilePanel = new LoadFilePanel(srcMechCombineCwd);
			innerPanel.add(loadFilePanel, gbc);
		}

		return innerPanel;
	}

	public String [] parseFileName(String mtiFileName) {
		if(mtiFileName==null||mtiFileName.isEmpty()) return null;
		StringTokenizer st = new StringTokenizer(mtiFileName, ";");
		String [] fileName = new String[st.countTokens()];
		for(int i=0; i<fileName.length; i++) {
			fileName[i] = st.nextToken().trim();
		}
		return fileName;
	}
	protected boolean okAction() {
		if(_iFunction==0) return combineFiles();
		else if(_iFunction==1) return generateFile();
		else return false;
	}
	protected boolean combineFiles() {
		String [] srcMechCombineFileNames = null;
		String tmp = _srcMechCombineTF.getText().trim();
		if(tmp.contains(";")) {
			srcMechCombineFileNames = parseFileName(tmp);
		} else {
			srcMechCombineFileNames = new String[]{ tmp };
		}
		
		File file = new File(srcMechCombineFileNames[0]);
		if(file.exists()&&file.isFile()) {
			_frame.setSrcMechCombineCwd(srcMechCombineFileNames[0]);
			SeisPTSrcMech seisPTSrcMech = new SeisPTSrcMech(_isFileEvent==1, srcMechCombineFileNames[0]);
			if(_iFunction==0) {
				for(int i=1; i<srcMechCombineFileNames.length; i++) {
					//System.out.println(mtiFileNames[i]);
					SeisPTSrcMech a = new SeisPTSrcMech(_isFileEvent==1, srcMechCombineFileNames[i]);
					seisPTSrcMech.append(a);		
				}
			} 
			
			save(seisPTSrcMech);
		}		
		return true;
	}
	
	protected boolean generateFile() {
		String tmp = _srcMechCombineTF.getText().trim();		
		File file = new File(_srcMechCombineTF.getText().trim());
		if((!file.exists())) return false;
		
		//map.xml file
		MyFileFilter mapFileFilter = new MyFileFilter("map.xml");
		File[] listOfMapFiles = file.listFiles(mapFileFilter);
		if(listOfMapFiles==null || listOfMapFiles.length==0|| listOfMapFiles.length>1) return false;
		MapXml mapXml = new MapXml(listOfMapFiles[0].getAbsolutePath());
		System.out.println(mapXml.getSrcMech()+"\n");
		
		//SeisPTSrcMech seisPTSrcMech = new SeisPTSrcMech(_isFileEvent==1, srcMechCombineFileNames[0]);
		MyFileFilter myFileFilter = new MyFileFilter("inf.xml");
		File[] listOfFiles = file.listFiles(myFileFilter); 
		for (File mFile : listOfFiles) {
			//String name = mFile.getName();
			if (mFile.isDirectory()) {				
			} else {
				String selectedFileName = mFile.getAbsolutePath();
				//System.out.println(selectedFileName);
				InfXml infXml = new InfXml(selectedFileName);
				Event event = infXml.getEvent();
				String a = event.getSrcMech(1);
				//System.out.println(a);
				//if(overwriteFile) {
//					try {
//						targetFileName = targetPath+File.separator+name;
//						FileUtils.copyFile(mFile, new File(targetFileName), true);
//					} catch (IOException e) {
//						e.printStackTrace();
//					}
				//}
			}
		}
			
		return true;
	}
	
	public void save(SeisPTSrcMech seisPTSrcMech)	{ 
		String ext = null;
		FileNameExtensionFilter exts[] = null;
		String a = _frame.getSrcMechCombineCwd();
		String baseName = null;
		
		ext = "txt";
		exts = new FileNameExtensionFilter [] { new FileNameExtensionFilter("TXT (*.txt)", ext) };
		baseName = FilenameUtils.getFullPath(a)+FilenameUtils.getBaseName(a)+"_combined."+ext;
			
		
		String fileName = _frame.saveFileUsingJFileChooser(exts, baseName); 
		if(fileName==null) return;
		else {
			String selectedExt = FilenameUtils.getExtension(fileName);
			if(selectedExt==null||selectedExt.isEmpty()) fileName = fileName+"."+ext;			
			seisPTSrcMech.write(false, fileName);			
			_frame.savePreferences();
		}
	}
	
	private class LoadFilePanel extends JPanel {

		public LoadFilePanel(String srcMechCombineCwd) {
			setLayout(new GridBagLayout());
			Insets insets = new Insets(5, 5, 5, 5);
			GridBagConstraints gbc;			

			_srcMechCombineTF = new JTextField (srcMechCombineCwd, 5);

			int n = 0;
			ButtonGroup moduleRadioGroup = null;
			String [] moduleString = null;
			String [] moduleToolTip = null;
			JRadioButton [] moduleRadioButton = null;
			
			int iRow = 0;			
			gbc= new GridBagConstraints(0, iRow, 1, 1, 1.0, 1.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, insets, 0, 0);
			if(_iFunction==0) {
				add(new JLabel("Load Multiple SrcMech File(s) In Order:"), gbc);

				moduleString = new String[]{"SrcMech Perf", "SrcMech Event"};
				n = moduleString.length;
				moduleRadioGroup = new ButtonGroup();
				moduleRadioButton = new JRadioButton[n];
				for(int i=0; i<n; i++) {
					final int j = i;
					moduleRadioButton[i] = new JRadioButton(moduleString[i], _isFileEvent==i);
					moduleRadioButton[i].addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent event) {
							if(((JRadioButton)event.getSource()).isSelected()) _isFileEvent = j; 
						}
					});
					moduleRadioGroup.add(moduleRadioButton[i]);
					gbc= new GridBagConstraints(1+i, iRow, 1, 1, 1.0, 1.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, insets, 0, 0);
					add(moduleRadioButton[i], gbc);
				}
			} else if(_iFunction==1) {
				add(new JLabel("*.inf.xml Folder:"), gbc);
			}
			
			JButton srcBrowserButton = new JButton("Browes"); 
			srcBrowserButton.addActionListener(new ActionListener()	{
				public void actionPerformed(ActionEvent e)  {
					String fileNameAll = null;
					if(_iFunction==0) {
						JFrame jframe = new JFrame("Open A File");
						String loc = _srcMechCombineTF.getText().trim();
						if(loc.contains(";")) {
							String [] mtiFileNames = parseFileName(loc);
							loc = mtiFileNames[0];
						} 
						JFileChooser chooser = new JFileChooser(new File(loc));
						FileListAccessory accessory = new FileListAccessory(chooser);
						chooser.setAccessory(accessory);
						//chooser.setMultiSelectionEnabled(true);
						FileNameExtensionFilter extension = new FileNameExtensionFilter("*.txt", "txt");

						chooser.setFileFilter( extension );
						chooser.addChoosableFileFilter( extension );
						chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
						//chooser.setDialogTitle("Open A File");

						int returnVal = chooser.showOpenDialog( jframe );
						if (returnVal == JFileChooser.APPROVE_OPTION) {
							DefaultListModel model = accessory.getModel();
							for (int i = 0; i < model.getSize(); i++) {
								//System.out.println(((File)model.getElementAt(i)).getName());
								File file = (File)model.getElementAt(i);
								String path = file.getAbsolutePath();
								if(fileNameAll==null) fileNameAll = path.trim();
								else fileNameAll += "; "+path.trim();
							}
							_srcMechCombineTF.setText(fileNameAll); 
							//System.out.println("Selected file10: " + _mtiTF.getText());						
						} else if (returnVal == JFileChooser.CANCEL_OPTION) { 							
						} else { }
					} else if(_iFunction==1) {
						String  recentFileName = _srcMechCombineTF.getText().trim();
						String fileName = _frame.openDirectoryUsingJFileChooser(recentFileName); 
						if(fileName==null) return;
						else { _srcMechCombineTF.setText(fileName); }
					}
				}
			});
			
			iRow++;
			gbc= new GridBagConstraints(0, iRow, 4, 1, 1.0, 1.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, insets, 0, 0);
			add(_srcMechCombineTF, gbc);
			gbc= new GridBagConstraints(4, iRow, 1, 1, 0.0, 1.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, insets, 0, 0);
			add(srcBrowserButton, gbc);		
			
//			iRow++;
//			gbc= new GridBagConstraints(0, iRow, 1, 1, 1.0, 1.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, insets, 0, 0);
//			add(new JLabel("Functions:"), gbc);
//			
//			moduleString = new String[]{"Combine Files"};
//			n = moduleString.length;
//			moduleRadioGroup = new ButtonGroup();
//			moduleRadioButton = new JRadioButton[n];
//			for(int i=0; i<n; i++) {
//				final int j = i;
//				moduleRadioButton[i] = new JRadioButton(moduleString[i], _iFunction==i);
//				moduleRadioButton[i].addActionListener(new ActionListener() {
//					public void actionPerformed(ActionEvent event) {
//						if(((JRadioButton)event.getSource()).isSelected()) _iFunction = j; 
//					}
//				});
//				moduleRadioGroup.add(moduleRadioButton[i]);
//				gbc= new GridBagConstraints(1+i, iRow, 1, 1, 1.0, 1.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, insets, 0, 0);
//				add(moduleRadioButton[i], gbc);
//			}
		}		
	}
	
	private final class MyFileFilter implements FileFilter {
		private final String _fileType;

		private MyFileFilter(String fileType)  {
			_fileType = fileType;
		}

		public boolean accept(File pathname) {
			return pathname.getName().endsWith("."+_fileType) || pathname.isDirectory();
		}
	}
	
}
