package com.geohammer.component;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.apache.commons.io.FilenameUtils;
import com.geohammer.common.CommonDialog;
import com.geohammer.common.CommonFrame;
import com.geohammer.common.CommonHtmlPage;
import com.geohammer.common.VeconUtils;

public class HtmlDialog extends CommonDialog {
	
	private CommonFrame 		_frame 		= null;
	private CommonHtmlPage 		_htmlPage 	= null;
	
	public HtmlDialog(JFrame aParent, String aTitle, boolean modal) {
		super(aParent, aTitle, modal);
		setDialogWindowSize(800, 800);
		setEnableSaveButton(true);
		_frame 		= (CommonFrame)aParent;
		_htmlPage = new CommonHtmlPage();
	}

	//protected JComponent getCommandRow() { return null; }	
	public void addTable(ArrayList<float []> hList, ArrayList<float []> vList) {
		_htmlPage.addTable(hList, vList);
	}
	
	protected boolean okAction() {	return true;	}
	protected JPanel createContents() {				
		JScrollPane scroll = new JScrollPane(_htmlPage);
		scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED); 
		scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED); 
		scroll.setMinimumSize(new Dimension(400, 300));     
		//scroll.setPreferredSize(new Dimension(400, 300));
		
		JPanel jPanel = new JPanel(new BorderLayout());
		jPanel.add(scroll, BorderLayout.CENTER );
		_htmlPage.setCaretPosition(0);
		return jPanel;
	}
	
	public boolean saveAction() {	
		String text = _htmlPage.tableToTxt();
		FileNameExtensionFilter exts[] = new FileNameExtensionFilter [] {
				new FileNameExtensionFilter("TXT (*.txt)", "txt")
		};
		String tmpName = _frame.getProject().getProjectFileName();
		tmpName = FilenameUtils.getFullPath(tmpName)+FilenameUtils.getBaseName(tmpName)+"_data.txt";
		String fileName = _frame.saveFileUsingJFileChooser(exts, tmpName); 
		if(fileName==null) return false;
		else {
			VeconUtils.saveTextToFile(text, fileName);
		}
		return true;	
	}

}

