package com.geohammer.common;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.io.File;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.apache.commons.io.FilenameUtils;

public class TextViewDialog extends CommonDialog {	
	private CommonFrame _frame 	= null;
	private String 		_text 	= null;
	
	public TextViewDialog(JFrame aParent, String aTitle, boolean modal, String text) {
		super(aParent, aTitle, modal);
		setDialogWindowSize(800, 800);
		setEnableSaveButton(true);
		_frame 		= (CommonFrame)aParent;
		_text 		= text;
	}

	public void setText(String text) { _text = text; }
	
	protected boolean okAction() {	return true;	}
	protected JPanel createContents() {				
		JScrollPane scroll = new JScrollPane(new JTextArea(_text));
		scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED); 
		scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED); 
		scroll.setMinimumSize(new Dimension(400, 300));     
		//scroll.setPreferredSize(new Dimension(400, 300));
		
		JPanel jPanel = new JPanel(new BorderLayout());
		jPanel.add(scroll, BorderLayout.CENTER );
		return jPanel;
	}
	
	public boolean saveAction() {
		FileNameExtensionFilter exts[] = new FileNameExtensionFilter [] {
				new FileNameExtensionFilter("TXT (*.txt)", "txt")
		};
		String tmpName = System.getProperty("user.dir")+File.separator+"untitled.txt";
		if(_frame.getProject()!=null) tmpName = _frame.getProject().getProjectFileName();
		tmpName = FilenameUtils.getFullPath(tmpName)+FilenameUtils.getBaseName(tmpName)+"_data.txt";
		String fileName = _frame.saveFileUsingJFileChooser(exts, tmpName); 
		if(fileName==null) return false;
		else {
			VeconUtils.saveTextToFile(_text, fileName);
		}
		return true;	
	}

}

