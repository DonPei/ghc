package org.ucdm.launcher.dialog;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.ucdm.common.CommonDialog;
import org.ucdm.launcher.LauncherFrame;
import org.ucdm.tracedisplay.TraceDisplayTreeDialog;

public class LoadSeg2FileDialog extends CommonDialog {
	
	LauncherFrame 		_frame 		= null;
	private JTextField  _folderTF	= null;
	public int 			_iDataType 	= 0; //0-SEG2 1-Green Function
	public int 			_iFold 		= 3; 
	
	public LoadSeg2FileDialog(JFrame aParent, String aTitle, boolean modal, String folderName, int iDataType) {
		super(aParent, aTitle, modal);
		setDialogWindowSize(800, 200);
		_frame 	= (LauncherFrame)aParent;
		if(folderName==null) _folderTF = new JTextField(System.getProperty("user.dir"));
		else _folderTF = new JTextField(folderName);
		_iDataType = iDataType;
	}

	@Override
	protected JComponent createContents() {
		JPanel innerPanel = new JPanel();

		innerPanel.setLayout(new GridBagLayout());
		Insets insets = new Insets(5, 5, 5, 5);
		GridBagConstraints gbc= null;
		
		int iRow = 0;
		
		if(_iDataType==0) {
			gbc= new GridBagConstraints(0, iRow, 1, 1, 1.0, 1.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, insets, 0, 0);
			innerPanel.add(new JLabel("SEG2 Data Directory:"), gbc);
			
			String [] moduleString = new String[] {
					new String( "1 Component"),
					new String( "2 Components"),
					new String( "3 Components")
			};
			int n = moduleString.length;
			ButtonGroup moduleRadioGroup = new ButtonGroup();
			JRadioButton [] moduleRadioButton = new JRadioButton[n];

			for(int i=0; i<n; i++) {
				final int j = i+1;
				moduleRadioButton[i] = new JRadioButton(moduleString[i], j==_iFold);
				moduleRadioButton[i].addItemListener( new ItemListener() {
					public void itemStateChanged(ItemEvent itemEvent) {
						if (itemEvent.getStateChange() == ItemEvent.SELECTED) { _iFold = j; } 
					}
				});

				moduleRadioGroup.add(moduleRadioButton[i]);
				gbc= new GridBagConstraints(j, iRow, 1, 1, 1.0, 1.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, insets, 0, 0);
				innerPanel.add(moduleRadioButton[i], gbc);
			}
		} else if(_iDataType==1) {
			gbc= new GridBagConstraints(0, iRow, 5, 1, 1.0, 1.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, insets, 0, 0);
			innerPanel.add(new JLabel("Green Function Data Directory:"), gbc);
		}
		JButton jButton = new JButton("Browse");
		jButton.addActionListener( new ActionListener() {
			public void actionPerformed( ActionEvent e ) {
				FileNameExtensionFilter exts[] = new FileNameExtensionFilter [] {
						new FileNameExtensionFilter("DAT (*.dat)", "dat") };

				String fileName = _frame.openFileAndDirectoryUsingJFileChooser(exts, _folderTF.getText()); 
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
		String aTitle = "Seismogram Viewer";
		TraceDisplayTreeDialog dialog = new TraceDisplayTreeDialog(_frame, aTitle, false, 
				 11, _iFold, _iDataType, dataPath);
		dialog.showDialog();
		
		_frame.setSeismogramViewerCwd(dataPath);
		return true;
	}

}
