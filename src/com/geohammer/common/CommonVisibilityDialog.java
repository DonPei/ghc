package com.geohammer.common;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.geohammer.common.util.UiUtil;

public class CommonVisibilityDialog  extends CommonDialog {
	CommonPanel 			_panel = null;
	ArrayList<String> 		_cList = new ArrayList<String>();
	ArrayList<String[]> 	_nList = new ArrayList<String[]>();
	ArrayList<Boolean[]> 	_vList = new ArrayList<Boolean[]>();
	
	ArrayList<VisibilityPanel> 	_vPanelList 	= new ArrayList<VisibilityPanel>();
	String _currentTab 		= null;

	public CommonVisibilityDialog(CommonPanel panel, String aTitle, boolean modal) {
		super(panel.getFrame(), aTitle, modal);
		_panel = panel;
		setParameters();
	}
	
	public void setParameters() {
		String [] categories = _panel.getWorldUniqueCategory();

		ArrayList<String> 		cList = new ArrayList<String>();
		ArrayList<String[]> 	nList = new ArrayList<String[]>();
		ArrayList<Boolean[]> 	vList = new ArrayList<Boolean[]>();
		for(int i=0; i<categories.length; i++) {
			cList.add(categories[i]);
			vList.add(_panel.getEntityVisibility(categories[i]));
			nList.add(_panel.getEntityName(categories[i]));
		}
		
		setParameters(cList, nList, vList);
	}
	
	public void setParameters(ArrayList<String> cList, ArrayList<String[]> nList, ArrayList<Boolean[]> 	vList) {
		_cList = cList;
		_nList = nList;
		_vList = vList;
	}
	
	protected JPanel createContents() {
		JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP, JTabbedPane.SCROLL_TAB_LAYOUT);
		// Register a change listener
		tabbedPane.addChangeListener(new ChangeListener() {
		    // This method is called whenever the selected tab changes
		    public void stateChanged(ChangeEvent evt) {
		        JTabbedPane pane = (JTabbedPane)evt.getSource();
		        // Get current tab
		        int k = pane.getSelectedIndex();
		        _currentTab = pane.getTitleAt(k);
		    }
		});
		
		for(int i=0; i<_cList.size(); i++) {
			Boolean [] visibility = _vList.get(i);
			String [] name = _nList.get(i);
			String category = _cList.get(i);
			VisibilityPanel v = new VisibilityPanel(visibility, name);
			_vPanelList.add(v);
			tabbedPane.addTab(" "+_cList.get(i)+" ", v);
		}
		JPanel topPanel = new JPanel();
		topPanel.setLayout(new GridBagLayout());
		Insets insets = new Insets(5, 5, 5, 5);
		GridBagConstraints gbc= new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0,
				GridBagConstraints.WEST, GridBagConstraints.BOTH, insets, 0, 0);
			
		topPanel.add( tabbedPane, gbc);
			
		return topPanel;
	}

	protected boolean okAction() {
		for(int i=0; i<_cList.size(); i++) {
			setVisibilitySelf(_cList.get(i), _vList.get(i));
		}
		return true;
	}
	public void setVisibilitySelf(String category, Boolean [] visibilityList) {
		boolean [] visibility = new boolean[visibilityList.length];
		for(int i=0; i<visibilityList.length; i++) {
			visibility[i] = visibilityList[i].booleanValue();
		}
		_panel.setVisibility(category, visibility);
	}
	
	protected void selectAction(boolean isSelected) {
		for(int i=0, k=0; i<_cList.size(); i++) {
			Boolean [] visibility = _vList.get(i);
			String category = _cList.get(i);
			if(category.equalsIgnoreCase(_currentTab.trim())) {
				for(int j=0; j<visibility.length; j++) {
					visibility[j] = new Boolean(isSelected);
				}
				_vPanelList.get(k).setCheckBox(visibility);
			} 

			if(!category.equalsIgnoreCase("Horizon")) { k++; }
		}
	}
	
	public JComponent getCommandRow() {
		JButton selectAll = new JButton("Select All");
		selectAll.addActionListener( new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				selectAction(true);
			}
		});
		JButton deselectAll = new JButton("Clear All");
		deselectAll.addActionListener( new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				selectAction(false);
			}
		});
		JButton ok = new JButton("OK");
		ok.addActionListener( new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				if(okAction()) dispose();
			}
		});
		JButton apply = new JButton("Apply");
		apply.addActionListener( new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				okAction();
			}
		});
		this.getRootPane().setDefaultButton( ok );
		JButton cancel = new JButton("Cancel");
		cancel.addActionListener( new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				dispose();
			}
		});
		List<JComponent> buttons = new ArrayList<JComponent>();
		buttons.add( selectAll );
		buttons.add( deselectAll );
		buttons.add( apply );
		buttons.add( ok );
		buttons.add( cancel );
		return UiUtil.getCommandRow( buttons );
	}

	private class VisibilityPanel extends JPanel {
		JCheckBox [] checkBox;
		public VisibilityPanel(Boolean [] visibility, String [] name) {
			setLayout(new GridBagLayout());
			Insets insets = new Insets(5, 5, 5, 5);
			GridBagConstraints gbc;
			
			final Boolean [] visibility1 = visibility;
			checkBox = new JCheckBox[name.length];

			for(int i=0; i<name.length; i++) {
				final int j = i;
				checkBox[i] = new JCheckBox("   "+name[i]);
				checkBox[i].setSelected(visibility[i].booleanValue());
				checkBox[i].addItemListener( new ItemListener() {
				      public void itemStateChanged(ItemEvent itemEvent) {
				          int state = itemEvent.getStateChange();
				          if (state == ItemEvent.SELECTED) {
				        	  visibility1[j] = new Boolean(true);
				          } else {
				        	  visibility1[j] = new Boolean(false);
				          }
				        }
				      });
				
				if(name.length>15) gbc = new GridBagConstraints(i/15, i%15, 1, 1, 1.0, 0.0,
						GridBagConstraints.WEST, GridBagConstraints.NORTH, insets, 0, 0);
				else {
					if(i==name.length-1) {
						gbc = new GridBagConstraints(0, i, 1, 1, 1.0, 1.0,
								GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, insets, 0, 0);
					} else {
						gbc = new GridBagConstraints(0, i, 1, 1, 0.0, 0.0,
								GridBagConstraints.WEST, GridBagConstraints.NORTH, insets, 0, 0);
					}
				}
				add(checkBox[i], gbc);
			}
		}
		
		public void setCheckBox(Boolean [] visibility) {
			for(int i=0; i<visibility.length; i++) {
				checkBox[i].setSelected(visibility[i].booleanValue());
			}
		}
	}
	
}
