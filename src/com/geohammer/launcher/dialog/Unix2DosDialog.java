package org.ucdm.launcher.dialog;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.apache.commons.io.FilenameUtils;
import org.ucdm.common.CommonDialog;
import org.ucdm.common.CommonFrame;

public class Unix2DosDialog extends CommonDialog {
	CommonFrame  	_frame 		= null;
	JTextField 		_cwdTF		= null;
	int				_iUnix2Dos 	= 1; // 0-Dos2Unix 1-Unix2Dos 

	public Unix2DosDialog(CommonFrame aParent, String aTitle, boolean modal) {
		super(aParent, aTitle, modal);
		setDialogWindowSize(800, 220);
		setEnableApplyButton(true);
		_frame 		= aParent;
	}
	
	protected JPanel createContents() { 
		String cwd = System.getProperty("user.dir");
		LoadFilePanel panel = new LoadFilePanel(cwd);
		return panel;		
	}
	
	protected boolean okAction() {
		String inputFileName = _cwdTF.getText().trim();
		String fullPath 	= FilenameUtils.getFullPath(inputFileName);
		String baseName 	= FilenameUtils.getBaseName(inputFileName);
		if(_iUnix2Dos==1) baseName += "_dos";
		else baseName += "_unix";
		String outputFileName = fullPath+baseName+".txt";
		
		//https://github.com/waarp/WaarpCommon/blob/master/src/main/java/org/waarp/common/utility/FileConvert.java
		// In DOS/Windows text files a line break, also known as newline, is a combination
		// of two characters: a Carriage Return (CR) followed by a Line Feed (LF). In Unix
		// text files a line break is a single character: the Line Feed (LF). In Mac text
		// files, prior to Mac OS X, a line break was single Carriage Return (CR)
		// character. Nowadays Mac OS uses Unix style (LF) line breaks
		
		//or
		//http://stackoverflow.com/questions/9374991/how-to-convert-files-from-dos-to-unix-in-java
		//String unixText = windowsText.replaceAll("\r\n", "\n"); // DOS2UNIX
		// The above line should remove all \r but for some reason it also removes 
		// the \n so I had to add it back when printing unixText to a file: unixText + "\n"
		
		File input = new File(inputFileName);
		File output = new File(outputFileName);
		try {
			output.createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
		}
		//output.createTempFile(prefix, suffix)
		convert(input, output, _iUnix2Dos==1);
		
		return true;
		
	}
	
	public boolean convert(File input, File output, boolean unix2dos) {
		FileInputStream fis = null;
		FileOutputStream fos = null;
		try {
			fis = new FileInputStream(input);
			fos = new FileOutputStream(output);
			if (unix2dos) {
				byte pb = -1;
				byte b = -1;
				while ((b = (byte) fis.read()) != -1) {
					if ((b == 10) && (pb != 13)) {
						fos.write((byte) 13);
					}
					fos.write(b);
					pb = b;
				}
			} else {
				byte b = -1;
				byte nb = -1;
				while ((b = (byte) fis.read()) != -1) {
					if (b == 13) {
						nb = (byte) fis.read();
						if (nb == -1) {
							fos.write(b);
						} else {
							if (nb == 10) {
								fos.write(nb);
							} else {
								fos.write(b);
								fos.write(nb);
							}
						}
					} else {
						fos.write(b);
					}
				}
			}
			
			fis.close(); 
			fos.close(); 
			return true;
		} catch (FileNotFoundException e) {
			return false;
		} catch (IOException e) {
			return false;
		}
	}
	
	private class LoadFilePanel extends JPanel {
		public LoadFilePanel(String cwd) {
			setLayout(new GridBagLayout());
			Insets insets = new Insets(5, 5, 5, 5);
			GridBagConstraints gbc;

			_cwdTF = new JTextField(cwd);
			JButton jButton = new JButton("Browse"); 
			jButton.addActionListener( new ActionListener() {
				public void actionPerformed( ActionEvent e ) {
					String fileName = _frame.openFileUsingJFileChooser(null, _cwdTF.getText()); 
					if(fileName==null) return;
					else {
						_cwdTF.setText(fileName);
					}
				}
			});
			
			
			String [] moduleString = new String[]{"Dos2Unix", "Unix2Dos" };
			int n = moduleString.length;
			JPanel modulePanel = new JPanel( new GridLayout(1, n, 5, 2));
			ButtonGroup moduleRadioGroup = new ButtonGroup();
			JRadioButton [] moduleRadioButton = new JRadioButton[n];
			for(int i=0; i<n; i++) {
				final int j = i;
				moduleRadioButton[i] = new JRadioButton(moduleString[i], _iUnix2Dos==j);
				moduleRadioButton[i].addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent event) {
						if(((JRadioButton)event.getSource()).isSelected()) _iUnix2Dos = j; 
					}
				});
				moduleRadioGroup.add(moduleRadioButton[i]);
				modulePanel.add(moduleRadioButton[i]);
			}

			int iRow = 0;
			gbc= new GridBagConstraints(0, iRow, 2, 1, 1.0, 1.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, insets, 0, 0);
			add(modulePanel, gbc);

			iRow++;
			gbc= new GridBagConstraints(0, iRow, 4, 1, 1.0, 1.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, insets, 0, 0);
			add(new JLabel("Input File:"), gbc);

			iRow++;
			gbc= new GridBagConstraints(0, iRow, 4, 1, 1.0, 1.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, insets, 0, 0);
			add(_cwdTF, gbc);
			gbc= new GridBagConstraints(4, iRow, 1, 1, 0.0, 1.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, insets, 0, 0);
			add(jButton, gbc);
		}
	}

}
