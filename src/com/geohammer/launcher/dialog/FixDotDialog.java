package org.ucdm.launcher.dialog;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.apache.commons.io.FilenameUtils;
import org.ucdm.common.CommonDialog;
import org.ucdm.common.CommonFrame;

public class FixDotDialog extends CommonDialog {
	CommonFrame  	_frame 		= null;
	JTextField 		_cwdTF		= null;
	
	public FixDotDialog(CommonFrame aParent, String aTitle, boolean modal) {
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
		String outputFileName = fullPath+baseName+"_withoutDot.csv";
		convert(inputFileName, outputFileName);
		
		return true;
		
	}
	public void convert(String inputName, String outputName) {			
		String delimiter = "[,]+";

		try{
			BufferedReader reader = new BufferedReader(new FileReader(inputName));
			BufferedWriter writer = new BufferedWriter(new FileWriter(outputName, false));

			String [] splits = null;
			String line=reader.readLine();
			writer.write(line);
			writer.write("\n");
			while ((line=reader.readLine()) != null)  {
				splits = line.split(delimiter);
				for(int i=1; i<3; i++) {
					splits[i] = splits[i].replace('.', ' ');
				}
				writer.write(splits[0]);
				for(int i=1; i<splits.length; i++) {
					writer.write(", "+splits[i]);
				}
				writer.write("\n");

				//writer.write(line);
			}

			reader.close();
			writer.close();
		} catch (IOException ioexception) {
			JOptionPane.showMessageDialog(null, "\nFile Format is not right!", 
					"Error", JOptionPane.ERROR_MESSAGE);
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

			int iRow = 0;
			gbc= new GridBagConstraints(0, iRow, 4, 1, 1.0, 1.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, insets, 0, 0);
			add(new JLabel("Input TV Event File:"), gbc);

			iRow++;
			gbc= new GridBagConstraints(0, iRow, 4, 1, 1.0, 1.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, insets, 0, 0);
			add(_cwdTF, gbc);
			gbc= new GridBagConstraints(4, iRow, 1, 1, 0.0, 1.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, insets, 0, 0);
			add(jButton, gbc);
		}
	}

}
