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
import org.ucdm.common.UcdmUtils;
import org.ucdm.core.SeismicTraceComponent;
import org.ucdm.launcher.LauncherFrame;
import org.ucdm.mti.beachball.FileListAccessory;

public class ManipulateGreenFileDialog extends CommonDialog {
	LauncherFrame 		_frame 			= null;
	
	
	private JTextField 	_srcMechCombineTF 	= null;
	private int 		_isDivideFile 		= 0; //0-combine 1-divide
	private int 		_iFunction 			= 0; //0-green
	
	public ManipulateGreenFileDialog(JFrame aParent, String aTitle, boolean modal, int iFunction) {
		super(aParent, aTitle, modal);
		setDialogWindowSize(800, 220);
		setOKButtonLabel("OK");
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
			String srcMechCombineCwd  = _frame.getGreenCombineCwd();
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
		String [] srcMechCombineFileNames = null;
		String tmp = _srcMechCombineTF.getText().trim();
		if(tmp.contains(";")) {
			srcMechCombineFileNames = parseFileName(tmp);
		} else {
			srcMechCombineFileNames = new String[]{ tmp };
		}
		
		File file = new File(srcMechCombineFileNames[0]);
		if(file.exists()&&file.isFile()) {
			_frame.setGreenCombineCwd(srcMechCombineFileNames[0]);
			
			if(_iFunction==0) {
				if(_isDivideFile==0) {
					SeismicTraceComponent [][] comps = new SeismicTraceComponent[srcMechCombineFileNames.length][];
					for(int i=0; i<srcMechCombineFileNames.length; i++) {
						//System.out.println(mtiFileNames[i]);
						comps[i] = UcdmUtils.readBinaryFileToTrace(srcMechCombineFileNames[i]);
					}

					FileNameExtensionFilter exts[] = new FileNameExtensionFilter [] { 
							new FileNameExtensionFilter("Bin (*.bin)", "bin") };
					String baseName = FilenameUtils.getFullPath(srcMechCombineFileNames[0])+
							FilenameUtils.getBaseName(srcMechCombineFileNames[0])+"_combined.bin";					

					String fileName = _frame.saveFileUsingJFileChooser(exts, baseName); 
					if(fileName==null) return false;
					else {
						String selectedExt = FilenameUtils.getExtension(fileName);
						if(selectedExt==null||selectedExt.isEmpty()) fileName = fileName+"."+"bin";	
						UcdmUtils.saveTraceToBinaryFile(comps, fileName);
						_frame.savePreferences();
					}
				} else if(_isDivideFile==1) {
					String baseName = FilenameUtils.getFullPath(srcMechCombineFileNames[0])+
							FilenameUtils.getBaseName(srcMechCombineFileNames[0]);
					SeismicTraceComponent [] comps0 = UcdmUtils.readBinaryFileToTrace(srcMechCombineFileNames[0]);
					int nTraces = comps0.length/6;
					for(int i=0; i<6; i++) {
						String selectedFileName = baseName+"_"+i+".bin";
						int ik1 = i*nTraces;
						int ik2 = ik1+nTraces;
						SeismicTraceComponent [] comps = new SeismicTraceComponent[nTraces];
						for(int ik=ik1, jk=0; ik<ik2; ik++) comps[jk++] = comps0[ik];
						
						UcdmUtils.saveTraceToBinaryFile(comps, selectedFileName);
					}
					_frame.savePreferences();
				}
			} 
		}		
		return true;
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
				add(new JLabel("Load Single/Multiple Green File(s) In Order:"), gbc);

				moduleString = new String[]{"Combine Files", "Divide Files"};
				n = moduleString.length;
				moduleRadioGroup = new ButtonGroup();
				moduleRadioButton = new JRadioButton[n];
				for(int i=0; i<n; i++) {
					final int j = i;
					moduleRadioButton[i] = new JRadioButton(moduleString[i], _isDivideFile==i);
					moduleRadioButton[i].addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent event) {
							if(((JRadioButton)event.getSource()).isSelected()) _isDivideFile = j; 
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
						FileNameExtensionFilter extension = new FileNameExtensionFilter("*.bin", "bin");

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
