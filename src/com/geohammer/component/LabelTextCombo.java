package com.geohammer.component;

import java.awt.BorderLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class LabelTextCombo  extends JPanel{
	
	public LabelTextCombo(String text, JTextField field) {
		setLayout(new BorderLayout());
		add(new JLabel(text), BorderLayout.WEST);
		add(field, BorderLayout.CENTER); 
	}
}