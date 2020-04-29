package com.geohammer.component;

/*
Logging In Java with the JDK 1.4 Logging API and Apache log4j
by Samudra Gupta    
Apress Copyright 2003 
ISBN:1590590996

 */

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JTextArea;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.apache.commons.io.FilenameUtils;
import com.geohammer.common.CommonFrame;
import com.geohammer.common.VeconUtils;

public class LogWindow extends CommonFrame {
	
	private JTextArea 	textArea 		= null;
	private int 		maxNumOfLine 	= 500;
	private int 		iLine 			= 0;
	private boolean 	listeningStatus = true;

	public LogWindow(String title, int width, int height) {
		setLayout(new GridBagLayout());
		Insets insets = new Insets(5, 5, 5, 5);
		GridBagConstraints gbc;
		
		
		textArea = new JTextArea();
		gbc= new GridBagConstraints(0, 0, 2, 1, 1.0, 1.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, insets, 0, 0);
		add(createContents(), gbc);
		
		JCheckBox jCheckBox = new JCheckBox("Listening", listeningStatus);
		jCheckBox.addItemListener( new ItemListener() {
			public void itemStateChanged(ItemEvent itemEvent) {
				listeningStatus = itemEvent.getStateChange() == ItemEvent.SELECTED; 
			}
		});
		gbc= new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, insets, 0, 0);
		add(jCheckBox, gbc);
		
		JButton saveAsBB = new JButton("Save As");
		saveAsBB.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				saveDataAs();
			}
		});
		gbc= new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0, GridBagConstraints.NORTHEAST, GridBagConstraints.NONE, insets, 0, 0);
		add(saveAsBB, gbc);
		
		//getContentPane().add(createContents());
		setVisible(true);
		setSize(width, height);
		setTitle(title);
	}

	public void setListeningStatus(boolean listeningStatus) { this.listeningStatus = listeningStatus; }
	public void setMaxNumOfLines(int maxNumOfLine) { this.maxNumOfLine = maxNumOfLine; }
	public boolean getListeningStatus() { return listeningStatus; }

	private JScrollPane createContents() {
		JScrollPane scrollPane = new JScrollPane(textArea);
		scrollPane.setAlignmentX(LEFT_ALIGNMENT); 
		scrollPane.setColumnHeaderView(null);
		scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		scrollPane.setWheelScrollingEnabled(true);
		//scrollPane.setMinimumSize(new Dimension(750, 500));     
		//scrollPane.setPreferredSize(new Dimension(750, 500));
		
		scrollPane.getVerticalScrollBar().addAdjustmentListener(new AdjustmentListener() {
			public void adjustmentValueChanged(AdjustmentEvent e) {
				if(listeningStatus) e.getAdjustable().setValue(e.getAdjustable().getMaximum());
			}}
		);
		return scrollPane;
	}
	private void saveDataAs()	{ 
		FileNameExtensionFilter exts[] = new FileNameExtensionFilter [] {
				new FileNameExtensionFilter("Text (*.txt)","txt")
		};
		String fileName = saveFileUsingJFileChooser(exts, System.getProperty("user.home")); 
		if(fileName==null) return;
		else {
			String selectedExt = FilenameUtils.getExtension(fileName);
			int k = 0;
			for(int i=0; i<exts.length; i++) {
				String [] possibleExt = exts[i].getExtensions();
				for(int j=0; j<possibleExt.length; j++) {
					if(selectedExt.equals(possibleExt[j])) {
						k = i;
						j = possibleExt.length+1;
						i = exts.length+1;
					}
				}
			}
			if(k==0) VeconUtils.saveTextToFile(textArea.getText(), fileName);
		}
	}
	
	public void showInfo(String data) {
		textArea.append(data);
		iLine++;
		if(iLine>maxNumOfLine) { textArea.setText(" "); iLine = 0; }
		//System.out.println(_textArea.getText());
		this.getContentPane().validate();
	}
	public void clearContents() {
		textArea.setText(" "); 
		iLine = 0; 
		this.getContentPane().validate();
	}
}




