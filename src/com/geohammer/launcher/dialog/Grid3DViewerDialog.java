package org.ucdm.launcher.dialog;

import java.awt.Color;
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
import org.ucdm.common.CommonFrame;
import org.ucdm.core.planarmodel.Grid3D;
import org.ucdm.launcher.LauncherFrame;

public class Grid3DViewerDialog  extends CommonDialog {
	private LauncherFrame 	_frame 				= null;
	private JTextField 	 	_sourceFolderTF 	= null;
	private String  		_cwd 				= null;

	public Grid3DViewerDialog(JFrame aParent, String aTitle, boolean modal, String cwd) {
		super(aParent, aTitle, modal);
		setDialogWindowSize(900, 180);
		_frame 	= (LauncherFrame)aParent;
		_cwd 	= cwd;
	}

	protected JPanel createContents() { 
		JPanel jPanel = new JPanel(new GridBagLayout());
		Insets insets = new Insets(5, 5, 5, 5);
		GridBagConstraints gbc;

		_sourceFolderTF = new JTextField (_cwd);
		JButton ssBB = new JButton("Browse");
		ssBB.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				FileNameExtensionFilter exts[] = new FileNameExtensionFilter [] {
						new FileNameExtensionFilter("bin (*.bin)", "bin") };
				String fileName = _frame.openFileUsingJFileChooser(exts, _sourceFolderTF.getText()); 
				if(fileName==null) return;
				else {
					_sourceFolderTF.setText(fileName);
				}
			}
		}); 

		int iRow = 0;
		gbc= new GridBagConstraints(0, iRow, 4, 1, 1.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, insets, 0, 0);
		jPanel.add(new JLabel("Grid 3D File:"), gbc);

		iRow++;
		gbc= new GridBagConstraints(0, iRow, 3, 1, 1.0, 1.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, insets, 0, 0);
		jPanel.add(_sourceFolderTF, gbc);
		gbc= new GridBagConstraints(3, iRow, 1, 1, 0.0, 1.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, insets, 0, 0);
		jPanel.add(ssBB, gbc);
		return jPanel;
	}

	protected boolean okAction() {
		String grid3DFileName = _sourceFolderTF.getText().trim();
		String aTitle = new String("Grid 3D Viewer");
		Plot3DDialog dialog = new Plot3DDialog(_frame, aTitle, false, grid3DFileName);
		dialog.showDialog();

		_frame.setGrid3DFileName(grid3DFileName);

		return true;
	}

	private class Plot3DDialog  extends CommonDialog {
		private CommonFrame 		_frame 			= null;
		private Grid3DPanel3D 		_plot3D			= null;
		private Grid3D 				_grid3D			= null;
		private String 				_imageFolder 	= null;

		public Plot3DDialog(JFrame aParent, String aTitle, boolean modal, String grid3DFileName) {
			super(aParent, aTitle, modal);
			setDialogWindowSize(800, 820);
			setBackground(Color.white);	
			_frame 		= (CommonFrame)aParent;
			_grid3D 	= new Grid3D(grid3DFileName);
			//_grid3D.setOutmostFaces(0);
		}
		public void setImageFolder(String imageFolder) { _imageFolder = imageFolder; }

		protected JComponent getCommandRow() 	{ return null; }
		protected boolean okAction() 			{ return true; }

		protected JPanel createContents() { 
			_plot3D	= new Grid3DPanel3D(_frame, _grid3D);
			return _plot3D;
		}

		public void update(String label) { 
			if(_imageFolder!=null) {
				String fileName = _imageFolder+"plot3D_"+label+".jpg";
				_plot3D.getViewCanvas().paintToFile(fileName);
			}
		}
	}
}
