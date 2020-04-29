package com.geohammer.common.d3;

import java.awt.AWTException;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
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
import javax.swing.JPopupMenu;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.apache.commons.io.FilenameUtils;
import com.geohammer.component.LabelTextCombo;

import edu.mines.jtk.awt.Mode;
import edu.mines.jtk.awt.ModeManager;
import edu.mines.jtk.sgl.BoundingBox;
import edu.mines.jtk.sgl.ViewCanvas;

public class Common3DMouseEditingMode extends Mode {
	private static final long serialVersionUID = 1L;
	//private CommonFrame 	_frame 			= null;
	private Common3DPanel 	_panel 			= null;

	private JPopupMenu 		_popupMenu 		= new JPopupMenu();
	private JTextField 		_scaleAmpTF 	= null;

	private Component 		_component 		= null;
	private JComponent [] 	_jComponent 	= null;

	private boolean _showVisibility 		= true;
	private boolean _showMobility 			= true;
	private boolean _showAutoAxisLimit 		= true;
	private boolean _showScreenShot 		= true;
	private boolean _showManualAxisLimit 	= false;
	private boolean _showBallsize 			= false;

	/**
	 * Constructs a mouse track mode with specified manager.
	 * @param modeManager the mode manager for this mode.
	 */
	public Common3DMouseEditingMode(ModeManager modeManager) {
		super(modeManager);
		setName("Track");
		setMnemonicKey(KeyEvent.VK_Z);
		setAcceleratorKey(KeyStroke.getKeyStroke(KeyEvent.VK_T,0));
		setShortDescription("Track mouse in tile");
	}

	public void setCommon3DPanel(Common3DPanel panel) 				{ _panel = panel; }

	public void setShowVisibility(boolean showVisibility) 			{ _showVisibility 		= showVisibility; }
	public void setShowMobility(boolean showMobility) 				{ _showMobility 		= showMobility; }
	public void setShowAutoAxisLimit(boolean showAxisLimit) 		{ _showAutoAxisLimit 	= showAxisLimit; }
	public void setShowManualAxisLimit(boolean showSetAxisLimit) 	{ _showManualAxisLimit 	= showSetAxisLimit; }
	public void setShowScreenShot(boolean showTakePhoto) 			{ _showScreenShot 		= showTakePhoto; }
	public void setShowBallsize(boolean showBallsize) 				{ _showBallsize 		= showBallsize; }

	public boolean isExclusive() 									{ return false; }
	public void setJComponent(JComponent [] jComponent) 			{ _jComponent = jComponent; }

	public void setMouseListener(MouseListener ml) 					{ _component.addMouseListener(ml); }
	public void setMouseMotionListener(MouseMotionListener mml) 	{ _component.addMouseMotionListener(mml); }

	protected void setActive(Component component, boolean active) {
		_component = component;
		if (active) {
			component.addMouseListener(_ml);
		} else {
			component.removeMouseListener(_ml);
		}
	}

	private MouseListener _ml = new MouseAdapter() {			
		public void mousePressed(MouseEvent evt) {
			//int px = evt.getXOnScreen();
			int px = evt.getX();
			int py = evt.getY();
			
			if((!evt.isShiftDown())&& (!evt.isAltDown()) && evt.isMetaDown()) {
				_popupMenu = new JPopupMenu();
				_popupMenu.setLightWeightPopupEnabled(false);
				setPopupMenu();
				_popupMenu.show(evt.getComponent(), px, py);
			}
		}
		public void mouseReleased(MouseEvent evt) { 
			if((!evt.isShiftDown())&&(!evt.isAltDown())) { }
		}
	};

	public void setPopupMenu() {
		JMenuItem jMenuItem = null;

		if(_jComponent!=null) {
			for(int i=0; i<_jComponent.length; i++) {
				if(_jComponent[i] instanceof JLabel) _popupMenu.addSeparator();
				else _popupMenu.add(_jComponent[i]);
			}
			_popupMenu.addSeparator();
		}
		
		if(_showBallsize) {
			_scaleAmpTF = new JTextField("1");
			_popupMenu.add(_scaleAmpTF);
			jMenuItem = new JMenuItem("Scale Ball");
			jMenuItem.setToolTipText("non-zero postive decimal");
			jMenuItem.addActionListener( new ActionListener() {
				public void actionPerformed(ActionEvent event) {
					//_scalor *= Float.parseFloat(_scaleAmpTF.getText().trim());
					//updateWorld();
				}
			});
			_popupMenu.add(jMenuItem);
		}
		
		
		if(_showVisibility) {
			jMenuItem = new JMenuItem("Visibility");
			jMenuItem.addActionListener( new ActionListener() {
				public void actionPerformed(ActionEvent event) {
					Common3DVisibilityDialog dialog = new Common3DVisibilityDialog(_panel, "Visibility", false);
					dialog.showDialog();
				}
			});
			_popupMenu.add(jMenuItem);
		}

		if(_showMobility) {
			jMenuItem = new JMenuItem("Mobility");
			jMenuItem.addActionListener( new ActionListener() {
				public void actionPerformed(ActionEvent event) {
					String movieDir = _panel.getFrame().getCwd()+"movie3D";
					Common3DMobilityDialog dialog = new Common3DMobilityDialog(_panel, "Mobility", false, movieDir);
					dialog.showDialog();
				}
			});
			_popupMenu.add(jMenuItem);
		}
		if(_showScreenShot) {
			jMenuItem = new JMenuItem("Screen Snapshot");
			//jMenuItem.addActionListener( _popMenuAction );
			jMenuItem.addActionListener( new ActionListener() {
				public void actionPerformed(ActionEvent event) {
					FileNameExtensionFilter exts[] = new FileNameExtensionFilter [] {
							new FileNameExtensionFilter("PNG (*.png)", "png")
					};
					String tmpName = _panel.getFrame().getProject().getProjectFileName();
					tmpName = FilenameUtils.getFullPath(tmpName)+"tmp.png";
					String fileName = _panel.getFrame().saveFileUsingJFileChooser(exts, tmpName); 
					if(fileName==null) return;
					else {
						BufferedImage image = null;
						try {
							ViewCanvas canvas = _panel.getViewCanvas();
							Point pt = canvas.getLocationOnScreen();
							Robot robot = new Robot();
							Dimension d = canvas.getSize();
							Rectangle bounds = new Rectangle(pt.x, pt.y, d.width, d.height);
							image = robot.createScreenCapture(bounds);
						} catch (AWTException e1) { e1.printStackTrace(); }
						
						File f = new File(fileName);
						try { ImageIO.write(image, "png", f);
						} catch (IOException e1) { e1.printStackTrace(); }
					}
				}
			});
			_popupMenu.add(jMenuItem);			 
		}
		_popupMenu.addSeparator();

		jMenuItem = new JMenuItem("Reset View");
		jMenuItem.setToolTipText("reset 3D display parameters to default");
		jMenuItem.addActionListener( new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				_panel.getObitViewMode().reset(_panel.getViewCanvas(), _panel.getOrbitView());
			}
		});
		_popupMenu.add(jMenuItem);
	}

	
}