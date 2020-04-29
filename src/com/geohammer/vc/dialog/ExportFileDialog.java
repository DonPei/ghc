package com.geohammer.vc.dialog;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.apache.commons.io.FilenameUtils;
import com.geohammer.common.CommonDialog;
import com.geohammer.common.CommonFrame;
import com.geohammer.core.acquisition.VCPair;
import com.geohammer.core.planarmodel.DipLayer1D;
import com.geohammer.vc.VcFrame;

public class ExportFileDialog extends CommonDialog {
	
	private CommonFrame 	_frame 		= null;
	private int 			_iType 		= 0;
	private JLabel 			_msgLabel 	= new JLabel(" ");
	private String 			_fileName 	= null;
	private JTextField 		_fileNameTF = null;
	
	public ExportFileDialog(JFrame aParent, String aTitle, boolean modal, int iType) {
		super(aParent, aTitle, modal);
		setDialogWindowSize(800, 220);		
		_frame 		= (CommonFrame)aParent;
		_iType 		= iType;
	}
	
	public void setFileName(String fileName) 			{ _fileName = fileName; }
	
	protected boolean okAction() {	
		VcFrame frame = (VcFrame)_frame;
		String selectedFileName = _fileNameTF.getText().trim();

		if(selectedFileName==null || selectedFileName.trim().isEmpty()) return false;
		
		if(_iType==0) {
			DipLayer1D dipLayer1D = frame.getRayTracer().getDipLayer1D();
			frame.getProject().writeVelocityFile(dipLayer1D, selectedFileName);
		} else if(_iType==1) {
			VCPair vCPair = frame.getRayTracer().getVCPair();
			frame.getProject().writeTTimeFile(vCPair, selectedFileName);
		}	
		return true;	
	}
	protected JPanel createContents() {
		JPanel jPanel = new JPanel(new GridBagLayout());
		Insets insets = new Insets(5, 5, 5, 5);
		GridBagConstraints gbc = null;
		
		_fileNameTF = new JTextField(_fileName);
		JButton jButton = new JButton("Browse"); 
		jButton.setToolTipText("Browse file");
		jButton.addActionListener( new ActionListener() {
			public void actionPerformed( ActionEvent e ) {
				String fileName = null;
				if(_fileName!=null) {
					String ext = FilenameUtils.getExtension(_fileName);
					FileNameExtensionFilter [] exts = new FileNameExtensionFilter [] {
							new FileNameExtensionFilter("Excel (*.xlsx)", "xlsx") };
					fileName = _frame.openFileUsingJFileChooser(exts, _fileNameTF.getText());
					if(fileName!=null) {
						String selectedExt = FilenameUtils.getExtension(fileName);
						String possibleExt = exts[0].getExtensions()[0];
						if(selectedExt.equals(possibleExt)) {
							_fileNameTF.setText(fileName);
						}
					}
				} else {
					fileName = _frame.openFileUsingJFileChooser(null, _fileNameTF.getText());
					if(fileName!=null) _fileNameTF.setText(fileName);
				}
			}
		});

		int iRow = 0;
		gbc= new GridBagConstraints(0, iRow, 4, 1, 1.0, 1.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, insets, 0, 0);
		jPanel.add(new JLabel("New file name:"), gbc);

		iRow++;
		gbc= new GridBagConstraints(0, iRow, 4, 1, 1.0, 1.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, insets, 0, 0);
		jPanel.add(_fileNameTF, gbc);
		gbc= new GridBagConstraints(4, iRow, 1, 1, 0.0, 1.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, insets, 0, 0);
		jPanel.add(jButton, gbc);

		iRow++;
		gbc= new GridBagConstraints(0, iRow, 1, 1, 0.0, 1.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, insets, 0, 0);
		jPanel.add(new JLabel("Status: "), gbc);
		gbc= new GridBagConstraints(1, iRow, 3, 1, 1.0, 1.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, insets, 0, 0);
		jPanel.add(_msgLabel, gbc);
		
		return jPanel;
	}
	
}
