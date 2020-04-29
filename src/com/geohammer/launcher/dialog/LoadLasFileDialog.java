package org.ucdm.launcher.dialog;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.ucdm.common.CommonDialog;
import org.ucdm.launcher.LauncherFrame;
import org.ucdm.soniclog.las.LasTreeDialog;
import org.ucdm.tracedisplay.TraceDisplayTreeDialog;

public class LoadLasFileDialog extends CommonDialog {
	
	LauncherFrame 		_frame 		= null;
	private JTextField  _folderTF	= null;
	
	public LoadLasFileDialog(JFrame aParent, String aTitle, boolean modal, String folderName) {
		super(aParent, aTitle, modal);
		setDialogWindowSize(800, 200);
		_frame 	= (LauncherFrame)aParent;
		if(folderName==null) _folderTF = new JTextField(System.getProperty("user.dir"));
		else _folderTF = new JTextField(folderName);
	}

	@Override
	protected JComponent createContents() {
		JPanel innerPanel = new JPanel();

		innerPanel.setLayout(new GridBagLayout());
		Insets insets = new Insets(5, 5, 5, 5);
		GridBagConstraints gbc= null;
		
		int iRow = 0;
		gbc= new GridBagConstraints(0, iRow, 5, 1, 1.0, 1.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, insets, 0, 0);
		innerPanel.add(new JLabel("Las Log Data Directory:"), gbc);
		
		JButton jButton = new JButton("Browse");
		jButton.addActionListener( new ActionListener() {
			public void actionPerformed( ActionEvent e ) {
				FileNameExtensionFilter exts[] = new FileNameExtensionFilter [] {
						new FileNameExtensionFilter("LAS (*.las)", "las") };

				String fileName = _frame.openDirectoryUsingJFileChooser(_folderTF.getText()); 
				if(fileName==null) return;
				else _folderTF.setText(fileName);
			}
		});		

		iRow++;
		gbc= new GridBagConstraints(0, iRow, 4, 1, 1.0, 1.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, insets, 0, 0);
		innerPanel.add(_folderTF, gbc);
		gbc= new GridBagConstraints(4, iRow, 1, 1, 0.0, 1.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, insets, 0, 0);
		innerPanel.add(jButton, gbc);
		
		return innerPanel;
	}

	@Override
	protected boolean okAction() {
		String dataPath = _folderTF.getText().trim();
		String aTitle = "LAS Viewer";
		LasTreeDialog dialog = new LasTreeDialog(_frame, aTitle, false, 
				 11, 3, 0, dataPath);
		dialog.showDialog();
		_frame.setLasViewerCwd(dataPath);
		return true;
	}

}
