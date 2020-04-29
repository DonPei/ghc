package com.geohammer.common;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.LayoutManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTextField;

import com.geohammer.common.CommonPanel.Orientation;
import com.geohammer.component.PointsViewWithLegend;

import edu.mines.jtk.awt.Mode;
import edu.mines.jtk.awt.ModeManager;
import edu.mines.jtk.mosaic.Tile;
import edu.mines.jtk.mosaic.TiledView;

public class CommonMouseEditingMode  extends Mode {
	private static final long serialVersionUID = 1L;
	public CommonPanel 		_panel 			= null;
	private JPopupMenu 		_popupMenu 		= new JPopupMenu();
	private Component 		_component 		= null;
	private JComponent [] 	_jComponent 	= null;
	
	private boolean _showLegend 			= false;
	private boolean _showVisibility 		= true;
	private boolean _showAutoAxisLimit 		= true;
	private boolean _showScreenShot 		= true;
	private boolean _showManualAxisLimit 	= false;
	
	private JTextField [] _minMaxField 		= new JTextField[4];
	private JTextField  _legendTopX 		= new JTextField("10");
	private JTextField  _legendTopY 		= new JTextField("20");
	
	private String _selectedCategory = null;
	
	public CommonMouseEditingMode(ModeManager modeManager) {
		super(modeManager);
		setName("Track");
		setShortDescription("Track mouse in tile");
	}
	
	public void setShowLegend(boolean showLegend) 					{ _showLegend 			= showLegend; }
	public void setShowVisibility(boolean showVisibility) 			{ _showVisibility 		= showVisibility; }
	public void setShowAutoAxisLimit(boolean showAxisLimit) 		{ _showAutoAxisLimit 	= showAxisLimit; }
	public void setShowManualAxisLimit(boolean showSetAxisLimit) 	{ _showManualAxisLimit 	= showSetAxisLimit; }
	public void setShowScreenShot(boolean showTakePhoto) 			{ _showScreenShot 		= showTakePhoto; }
	
	public boolean isExclusive() { return false; }
	public void setCommonPanel(CommonPanel panel) 					{ _panel = panel; }
	public void setJComponent(JComponent [] jComponent) 			{ _jComponent = jComponent; }
	
	public void setMouseListener(MouseListener ml) 					{ _component.addMouseListener(ml); }
	public void setMouseMotionListener(MouseMotionListener mml) 	{ _component.addMouseMotionListener(mml); }
	protected void setActive(Component component, boolean active) {
		if ((component instanceof Tile)) {
			if (active) {
				_component = component;
				component.addMouseListener(_ml);
			} else {
				component.removeMouseListener(_ml);
			}
		}
	}
	
	public String getSelectedCategory() { return _selectedCategory; };
	public TiledView getView(String category) {
		return getView(category, null);
	}
	public TiledView getView(String category, String name) {
		int nViews = _tile.countTiledViews();
		for(int i=0; i<nViews; i++) {
			TiledView element = _tile.getTiledView(i);
			//System.out.println(element.getCategory());
			if(element.getCategory().equalsIgnoreCase(category.trim())) {
				if(name==null) {
					return element;
				} else {
					if(element.getName().equalsIgnoreCase(name.trim())) 
						return element;
				}
			}
		}
		return null;
	}

	///////////////////////////////////////////////////////////////////////////
	// private
//	private KeyListener _kl2 = new KeyAdapter() {
//		public void keyTyped(KeyEvent e) { }
//		public void keyReleased(KeyEvent e) { }
//		public void keyPressed(KeyEvent e) { }
//	};

	private Tile _tile = null; // tile in which tracking; null, if not tracking.
	PopMenuAction _popMenuAction = null;
	public MouseListener _ml = new MouseAdapter() {
		public void mouseEntered(MouseEvent e) {
			beginTracking(e);
		}
		public void mouseExited(MouseEvent e) {
			endTracking();
		}
		
		public void mousePressed(MouseEvent evt) {
			_tile = (Tile)evt.getSource();
			if(_tile==null) return;
			if(_tile.countTiledViews()<=0) return;
			if(_panel!=null) _panel.mousePressedOn(evt);
			
			if(evt.isMetaDown()) {//Press mouse right button
				if((!evt.isAltDown())&&(!evt.isShiftDown())) { 
					_popupMenu = new JPopupMenu();
					setPopupMenu(_tile, _tile.getTiledView(0));
					_popupMenu.show(evt.getComponent(), evt.getX(), evt.getY());
					TiledView element = _tile.getTiledView(0);
				}
			} else {
				//int index = (int)_vx;
			}
		}

		public void mouseReleased(MouseEvent evt) { }
	};

	private MouseMotionListener _mml = new MouseMotionAdapter() {
		public void mouseDragged(MouseEvent evt)  {
			if((!evt.isShiftDown())&& (!evt.isAltDown())) {
				_tile = (Tile)evt.getSource();
				if(_tile==null) return;
			}
		}
	};

	private void beginTracking(MouseEvent e) {
		_tile = (Tile)e.getSource();
		_tile.addMouseMotionListener(_mml);
	}

	private void endTracking() {
		_tile.removeMouseMotionListener(_mml);
		_tile = null;
	}

	public void setPopupMenu(Tile tile, TiledView view) {
		_selectedCategory = tile.getTiledView(0).getCategory();
		_popMenuAction = new PopMenuAction(tile, view);
		JMenuItem jMenuItem = null;
		JMenu jMenu = null;
		
		if(_panel.getFrame()!=null) {
			if(_jComponent!=null) {
				for(int i=0; i<_jComponent.length; i++) {
					if(_jComponent[i] instanceof JLabel) _popupMenu.addSeparator();
					else _popupMenu.add(_jComponent[i]);
				}
			}
			_popupMenu.addSeparator();
			if(_showVisibility) {
				jMenuItem = new JMenuItem("Visibility");
				jMenuItem.addActionListener( _popMenuAction );
				_popupMenu.add(jMenuItem);
				_popupMenu.addSeparator();
			}
		} 
		
		if(_showScreenShot) {
			jMenuItem = new JMenuItem("Screen Snapshot To File");
			jMenuItem.addActionListener( _popMenuAction );
			_popupMenu.add(jMenuItem);
			jMenuItem = new JMenuItem("Screen Snapshot To Clipboard");
			jMenuItem.addActionListener( _popMenuAction );
			_popupMenu.add(jMenuItem);
			_popupMenu.addSeparator();
		}
		
		if(_showAutoAxisLimit) {
			jMenu = new JMenu("Auto Axis Limit");
			_popupMenu.add(jMenu);
			
			jMenuItem = new JMenuItem("Horizontal Axis Max +5%");
			jMenuItem.addActionListener( _popMenuAction );
			jMenu.add(jMenuItem);
			jMenuItem = new JMenuItem("Horizontal Axis Max -5%");
			jMenuItem.addActionListener( _popMenuAction );
			jMenu.add(jMenuItem);
			jMenuItem = new JMenuItem("Horizontal Axis Min +5%");
			jMenuItem.addActionListener( _popMenuAction );
			jMenu.add(jMenuItem);
			jMenuItem = new JMenuItem("Horizontal Axis Min -5%");
			jMenuItem.addActionListener( _popMenuAction );
			jMenu.add(jMenuItem);

			jMenu.addSeparator();

			jMenuItem = new JMenuItem("Vertical Axis Max +5%");
			jMenuItem.addActionListener( _popMenuAction );
			jMenu.add(jMenuItem);
			jMenuItem = new JMenuItem("Vertical Axis Max -5%");
			jMenuItem.addActionListener( _popMenuAction );
			jMenu.add(jMenuItem);
			jMenuItem = new JMenuItem("Vertical Axis Min +5%");
			jMenuItem.addActionListener( _popMenuAction );
			jMenu.add(jMenuItem);
			jMenuItem = new JMenuItem("Vertical Axis Min -5%");
			jMenuItem.addActionListener( _popMenuAction );
			jMenu.add(jMenuItem);
		}
		if(_showManualAxisLimit) {
			jMenu = new JMenu("Manual Axis Limit");
			_popupMenu.add(jMenu);
			double h0 = view.getHorizontalProjector().v0();
			double h1 = view.getHorizontalProjector().v1();
			double v0 = view.getVerticalProjector().v0();
			double v1 = view.getVerticalProjector().v1();
			
			_minMaxField[0] = new JTextField(h0+"");
			_minMaxField[1] = new JTextField(h1+"");
			_minMaxField[2] = new JTextField(v0+"");
			_minMaxField[3] = new JTextField(v1+"");
			if(h1>h0) {
				jMenu.add(new LabelTextCombo("H_min ", _minMaxField[0]));
				jMenu.add(new LabelTextCombo("H_max", _minMaxField[1]));
			} else {
				jMenu.add(new LabelTextCombo("H_min ", _minMaxField[1]));
				jMenu.add(new LabelTextCombo("H_max", _minMaxField[0]));	
			}
			if(v1>v0) {
				jMenu.add(new LabelTextCombo("V_min ", _minMaxField[2]));
				jMenu.add(new LabelTextCombo("V_max", _minMaxField[3]));	
			} else {
				jMenu.add(new LabelTextCombo("V_min ", _minMaxField[3]));
				jMenu.add(new LabelTextCombo("V_max", _minMaxField[2]));
			}

			jMenuItem = new JMenuItem("Manual Set Axis");
			jMenuItem.addActionListener( _popMenuAction );
			jMenu.add(jMenuItem);
			
			_popupMenu.addSeparator();
		}
	
		if(_showLegend) {
			jMenu = new JMenu("Legend");
			_popupMenu.add(jMenu);

			jMenuItem = new JMenuItem("Toggle Legend");
			jMenuItem.addActionListener( _popMenuAction );
			jMenu.add(jMenuItem);
			jMenu.add(new LabelTextCombo("Top-Left Pixel X", _legendTopX));
			jMenu.add(new LabelTextCombo("Top-Left Pixel Y", _legendTopY));
			jMenuItem = new JMenuItem("Set Top-Left Pixel(x,y)");
			jMenuItem.addActionListener( _popMenuAction );
			jMenu.add(jMenuItem);
		}
	}

	
	private class PopMenuAction implements ActionListener {
		TiledView _view = null;
		Tile _popTile = null;
		public PopMenuAction(Tile tile, TiledView view) {
			_view = view;
			_popTile = tile;
		}
		
		private void setAxis(int iAxis, double minPlus, double maxPlus) {
			if(iAxis==0) {
				double minClip = _view.getHorizontalProjector().v0();
				double maxClip = _view.getHorizontalProjector().v1();
				double dv = 0.025*(maxClip-minClip);
				_panel.setBestHorizontalClips(0, minClip+minPlus*dv, maxClip+maxPlus*dv);
			} else {
				double minClip = _view.getVerticalProjector().v0();
				double maxClip = _view.getVerticalProjector().v1();
				double dv = 0.025*(maxClip-minClip);
				_panel.setBestVerticalClips(0, minClip+minPlus*dv, maxClip+maxPlus*dv);
			}
			_panel.repaint();
		}
		private void setAxis(double h0, double h1, double v0, double v1) {
			_panel.setBestHorizontalClips(0, h0, h1);
			_panel.setBestVerticalClips(0, v0, v1);
			_panel.repaint();
		}
		
		private void setLegend(int id) {
			try{
				CommonPointPanel pp = (CommonPointPanel)_panel;
				Tile tile = pp.getTile(0, 0);
				for(int i = 0, k=0; i<tile.countTiledViews(); i++) {
					TiledView tv = tile.getTiledView(i);
					try{
						PointsViewWithLegend pv = (PointsViewWithLegend)tv;
						if(id==0) pv.setLegendOn(!pv.getLegendOn());
						else {
							int legendX = Integer.parseInt(_legendTopX.getText().trim());
							int legendY = Integer.parseInt(_legendTopY.getText().trim())+20*k++;
							pv.setLegendX(legendX);
							pv.setLegendY(legendY);
						}
						pv.refresh();
					} catch(java.lang.ClassCastException e) { }
				}
			} catch(java.lang.ClassCastException e){	}
		}
		
		public void actionPerformed(ActionEvent actionevent) {
			String cmd;

			try {
				JMenuItem jmenuitem = (JMenuItem)actionevent.getSource();
				cmd = jmenuitem.getText();
			} catch(ClassCastException classcastexception) {
				JCheckBox jbutton = (JCheckBox)actionevent.getSource();
				cmd = jbutton.getActionCommand();
			}
			
			if(cmd.equalsIgnoreCase("Visibility")) {
				CommonVisibilityDialog dialog = new CommonVisibilityDialog(_panel, "Visibility", false);
				dialog.setDialogWindowSize(700, 700);
				dialog.showDialog();
			} else if(cmd.equalsIgnoreCase("Screen Snapshot To File")) {
				_panel.paintToImage(true);
			} else if(cmd.equalsIgnoreCase("Screen Snapshot To Clipboard")) {
				_panel.paintToImage(false);
			}
			else if(cmd.equalsIgnoreCase("Manual Set Axis")) {
				double h0 = Double.parseDouble(_minMaxField[0].getText().trim());
				double h1 = Double.parseDouble(_minMaxField[1].getText().trim());
				double v0 = Double.parseDouble(_minMaxField[2].getText().trim());
				double v1 = Double.parseDouble(_minMaxField[3].getText().trim());
				setAxis(h0, h1, v0, v1);
			} 
			
			else if(cmd.equalsIgnoreCase("Toggle Legend")) {
				setLegend(0);
			} else if(cmd.equalsIgnoreCase("Set Top-Left Pixel(x,y)")) {
				setLegend(1);				
			}
			
			else if(cmd.equalsIgnoreCase("Horizontal Axis Max +5%")) {
				setAxis(0, 0, 1);
			} else if(cmd.equalsIgnoreCase("Horizontal Axis Max -5%")) {
				setAxis(0, 0, -1);				
			} else if(cmd.equalsIgnoreCase("Horizontal Axis Min +5%")) {
				setAxis(0, 1, 0);				
			} else if(cmd.equalsIgnoreCase("Horizontal Axis Min -5%")) {
				setAxis(0, -1, 0);				
			} else if(cmd.equalsIgnoreCase("Vertical Axis Max +5%")) {
				if(_panel.getOrientation()==Orientation.X1RIGHT_X2UP) setAxis(1, -1, 0);
				else setAxis(1, 0, 1);
			} else if(cmd.equalsIgnoreCase("Vertical Axis Max -5%")) {
				if(_panel.getOrientation()==Orientation.X1RIGHT_X2UP) setAxis(1, 1, 0);	
				else setAxis(1, 0, -1);
			} else if(cmd.equalsIgnoreCase("Vertical Axis Min +5%")) {
				if(_panel.getOrientation()==Orientation.X1RIGHT_X2UP) setAxis(1, 0, -1);
				else setAxis(1, 1, 0);
			} else if(cmd.equalsIgnoreCase("Vertical Axis Min -5%")) {
				if(_panel.getOrientation()==Orientation.X1RIGHT_X2UP) setAxis(1, 0, 1);	
				else setAxis(1, -1, 0);
			} else { }
		}			
	}
	
	private class LabelTextCombo extends JPanel{
		public LabelTextCombo(String text, JTextField field) {
			setLayout(new BorderLayout());
			add(new JLabel(text), BorderLayout.WEST);
			add(field, BorderLayout.CENTER); 
		}
	}
}