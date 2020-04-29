package com.geohammer.common;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.util.ArrayList;

import javax.swing.JPanel;
import javax.swing.JScrollPane;

//https://github.com/karlvr/bounce/blob/master/src/main/java/org/bounce/CardPanel.java

public class CommonCardPanel extends JPanel  {

	private static final long serialVersionUID = 3258135768999737650L;

	private CardLayout 		_layout 		= null;
	private JPanel 			_center 		= null;
	private JScrollPane 	_scroller 		= null;
	private JPanel 			_current 		= null;
	private String 			_name 			= null;
	
	private ArrayList<JPanel> 	_cards 		= new ArrayList<JPanel>();
	private ArrayList<String> 	_cardNames 	= new ArrayList<String>();
	
	public CommonCardPanel() { this( false); }
	protected CommonCardPanel(boolean scrollable) {
		super(new BorderLayout());

		_layout = new CardLayout();
		_center = new JPanel(_layout);

		if (scrollable) {
			_scroller = new JScrollPane(_center, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
			_scroller.setPreferredSize(new Dimension(160, 100));

			add(_scroller, BorderLayout.CENTER);
		} else {
			add(_center, BorderLayout.CENTER);
		}
	}

	public int getIndex(String name) {
		int k = -1;
		if(_cardNames==null) return k;
		for(int i=0; i<_cardNames.size(); i++) {
			if(name.equals(_cardNames.get(i))) { k = i; break; }
		}
		return k;
	}
	public JPanel getJPanel(String name) {
		int k = getIndex(name);
		if(k<0) return null;
		else return _cards.get(k);
	}
	public void addCard(JPanel card, String name) {	
		_cards.add(card);
		_cardNames.add(name);
		_center.add(card, name);		
	}
	public void showCard(String name) {
		JPanel card = getJPanel(name);
		if(card==null) return;
		_current = card;
		_name 	= name;
		//_current.requestFocusInWindow();			
		_layout.show(_center, name);
	}	

	public void removeCard(String name) {
		JPanel card = getJPanel(name);
		if(card==null) return;
		_cards.remove(card);
		_cardNames.remove(name);		
		_layout.removeLayoutComponent(card);
	}

	public JPanel getCurrent() 					{ return _current; }
	public String getCurrentName() 				{ return _name; }
	
	public void setCurrent(JPanel current) 		{ _current = current; }
	public void setCurrentName(String name) 	{ _name = name; }
	
	private static final String toString(Object panel) {
		return panel.getClass().getName() + "@" + Integer.toHexString(panel.hashCode());
	}

}