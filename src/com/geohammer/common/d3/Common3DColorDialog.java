package com.geohammer.common.d3;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;

import edu.mines.jtk.sgl.OrbitView;
import edu.mines.jtk.sgl.OrbitViewMode;
import edu.mines.jtk.sgl.ViewCanvas;

import com.geohammer.common.CommonDialog;

public class Common3DColorDialog  extends CommonDialog {
	private Common3DPanel 	_panel 		= null;
	private OrbitViewMode 	_ovm		= null;
	private OrbitView 		_view 		= null;
	private ViewCanvas 		_canvas 	= null;	

	private JColorChooser 			_chooser 			= null;

	public Common3DColorDialog(Common3DPanel panel, String aTitle, boolean modal) {
		super(panel.getFrame(), aTitle, modal);
		setDialogWindowSize(500, 650);
		_panel 	= panel;
		_ovm 	= panel.getObitViewMode();
		_view 	= panel.getOrbitView();
		_canvas = panel.getViewCanvas();		

		_chooser = new JColorChooser();
		PreviewButton previewButton = new PreviewButton(300, 50);
		_chooser.setPreviewPanel(previewButton);
	}
	
	public JPanel createContents() {
		JPanel innerPanel = new JPanel();
		innerPanel.setLayout(new GridBagLayout());
		Insets insets = new Insets(5, 5, 5, 5);
		GridBagConstraints gbc= new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0,
				GridBagConstraints.WEST, GridBagConstraints.BOTH, insets, 0, 0);

		int k = 0;		
		String category = "Horizon";
		String [] layerName = _panel.getEntityName(category);
		for(int i=0; i<layerName.length; i++) {
			gbc= new GridBagConstraints(0, k++, 1, 1, 1.0, 1.0,
					GridBagConstraints.WEST, GridBagConstraints.BOTH, insets, 0, 0);	
			innerPanel.add(new AdjustPanel(layerName[i], category, layerName[i]), gbc);
		}

		return innerPanel;
	}

	protected boolean okAction() { return true; }
	public JComponent getCommandRow() { return null;	}
	
	private class AdjustPanel extends JPanel {
		String 		_label 			= null;
		String 		_category 		= null;
		String 		_name 			= null;

		PreviewButton _button 		= null;
		
		public AdjustPanel(String label, String category, String name) {
			_label = label;
			_category = category;
			_name = name;

			int sizeX = 80;
			int sizeY = 30;
			setLayout(new FlowLayout(FlowLayout.LEFT));

			//_color = Color.RED;
			Color color = _ovm.getColor(_category, _name, _canvas, _view);
			_button = new PreviewButton(sizeX, sizeY, color);
			_button.addActionListener(new ActionListener() {
				public void actionPerformed( ActionEvent e ) {
					JDialog dialog = JColorChooser.createDialog(_panel.getFrame(),
							"Select Color", true, _chooser, new ActionListener() {
						public void actionPerformed(ActionEvent e) {
							adjust(_chooser.getColor());
						}
					}, null);
					dialog.setVisible(true);
				}
			});
			add(_button);
			add(new JLabel(label));
		}
		
		public void adjust(Color color) {
			_button.setColor(color);
			_ovm.setColor(_category, _name, color, _canvas, _view);
		}

		public Color getColor() {
			Color color = Color.GREEN;
			return color;
		}
	}


	private class PreviewButton extends JButton { 
		public PreviewButton(int w, int h) { 
			setPreferredSize(new Dimension(w,h));
		} 
		
		public PreviewButton(int w, int h, Color color) { 
			setPreferredSize(new Dimension(w,h));
			setColor(color);
		} 
		
		public void setColor(Color color){
			setForeground(color);
			setBackground(color);
		}
		
		@Override 
		protected void paintComponent(Graphics g) { 
			g.fillRect(0, 0, getWidth(), getHeight());
		} 
	}
	
}
