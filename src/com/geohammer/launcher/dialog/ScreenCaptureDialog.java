package org.ucdm.launcher.dialog;

import java.awt.AWTException;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.DisplayMode;
import java.awt.Graphics;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.PointerInfo;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;
import javax.swing.event.MouseInputAdapter;

import org.ucdm.common.CommonDialog;
import org.ucdm.launcher.LauncherFrame;

import com.sun.awt.AWTUtilities;

import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;


public class ScreenCaptureDialog  extends CommonDialog {
	private LauncherFrame 	_frame 		= null;

	JTextField 	 	_sourceFolderTF 	= null;
	JLabel 			_p1Label 			= null;
	JLabel 			_p2Label 			= null;
	JLabel 			_infoLabel 			= null;

	private String  		_cwd 		= null;
	private int 			_pageNo 	= 1;
	private JTextField 		_pageTF 	= null;

	private GlassFrame 		_glassFrame = null;
	private int 			_p1Xmouse 	= 0; 
	private int 			_p1Ymouse 	= 0; 
	private int 			_p2Xmouse 	= 0; 
	private int 			_p2Ymouse 	= 0;

	public ScreenCaptureDialog(JFrame aParent, String aTitle, boolean modal, String cwd) {
		super(aParent, aTitle, modal);
		setDialogWindowSize(900, 350);
		setEnableApplyButton(true);
		_frame 	= (LauncherFrame)aParent;
		_cwd 	= cwd;
		_cwd = "C:\\Don\\Personal\\Steven\\Grade07\\science\\Chapter6";

		//Toolkit.getDefaultToolkit().addAWTEventListener(new Listener(), AWTEvent.MOUSE_EVENT_MASK | AWTEvent.FOCUS_EVENT_MASK);
	}
	protected JPanel createContents() { return new LoadPanel(_cwd);}
	public MouseListener _ml = new MouseAdapter() {
		public void mouseEntered(MouseEvent e) { }
		public void mouseExited(MouseEvent e) { }
		
		public void mousePressed(MouseEvent e) 		{ beginTracking(e); }
		public void mouseReleased(MouseEvent e) 	{ endTracking(); }
	};
	private MouseMotionListener _mml = new MouseMotionAdapter() {
		public void mouseDragged(MouseEvent e) 	{ duringTracking(e); }
		public void mouseMoved(MouseEvent e) 	{ duringTracking(e); }
	};
	private void beginTracking(MouseEvent e) {
		_p1Xmouse 	= e.getX(); 
		_p1Ymouse 	= e.getY(); 
		_glassFrame.addMouseMotionListener(_mml);
	}
	private void duringTracking(MouseEvent e) {
		_p2Xmouse 	= e.getX(); 
		_p2Ymouse 	= e.getY();
		//PointerInfo inf = MouseInfo.getPointerInfo();
		//Point p = inf.getLocation();
		//_infoLabel.setText(e.getX()+", "+e.getY()+" P= "+p.x+", "+p.y);
		_infoLabel.setText(e.getX()+", "+e.getY());
		_glassFrame.getGlassPane().repaint();
	}

	private void endTracking() {
		_p1Label.setText(_p1Xmouse+", "+_p1Ymouse+", "+_p2Xmouse+", "+_p2Ymouse);
		_glassFrame.removeMouseMotionListener(_mml);
		_glassFrame.setVisible(false); 
		_glassFrame.dispose();
	}

	private class LoadPanel extends JPanel {

		public LoadPanel(String srcPath) {
			setLayout(new GridBagLayout());
			Insets insets = new Insets(5, 5, 5, 5);
			GridBagConstraints gbc;
			_infoLabel 			= new JLabel("info:");

			String [] moduleString = null;
			int n = 0;

			JPanel modulePanel = null;	
			ButtonGroup moduleRadioGroup = null;
			JRadioButton [] moduleRadioButton = null;

			Border loweredetched = BorderFactory.createEtchedBorder(EtchedBorder.LOWERED);

			_sourceFolderTF = new JTextField (srcPath);
			JButton ssBB = new JButton("Browse");
			ssBB.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					String fileName = _frame.openDirectoryUsingJFileChooser(_sourceFolderTF.getText()); 
					if(fileName==null) return;
					else {
						_sourceFolderTF.setText(fileName);
					}
				}
			}); 

			int iRow = 0;
			gbc= new GridBagConstraints(0, iRow, 4, 1, 1.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, insets, 0, 0);
			add(new JLabel("Image File (jpg, png) Directory:"), gbc);

			iRow++;
			gbc= new GridBagConstraints(0, iRow, 3, 1, 1.0, 1.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, insets, 0, 0);
			add(_sourceFolderTF, gbc);
			gbc= new GridBagConstraints(3, iRow, 1, 1, 0.0, 1.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, insets, 0, 0);
			add(ssBB, gbc);


			Rectangle screenRect = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());

			_p1Xmouse 	= (int)screenRect.getMinX(); 
			_p1Ymouse 	= (int)screenRect.getMinY(); 
			_p2Xmouse 	= (int)screenRect.getMaxX(); 
			_p2Ymouse 	= (int)screenRect.getMaxY();
			_p1Label = new JLabel(_p1Xmouse +", "+_p1Ymouse+", "+_p2Xmouse+", "+_p2Ymouse);
			JButton p1Button = new JButton("Set Rectangle ");
			p1Button.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					//GraphicsDevice device = GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices()[0];
					//final JPanel glass = (JPanel) _frame.getGlassPane();
					//glassPanel.setOpaque(false);
					//glassPanel.setVisible(false);
					//glassPanel.addMouseListener(new MouseAdapter() {});
					//glassPanel.setFocusable(true);
					_glassFrame  = new GlassFrame();
					_glassFrame.setVisible(true);
				}
			});
			_p2Label = new JLabel(screenRect.getMaxX() +", "+screenRect.getMaxY());
			JButton p2Button = new JButton("Set P2 ");
			p2Button.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					//GlassFrame frame = new GlassFrame();
					_glassFrame  = new GlassFrame();
				}
			});
			_pageTF 	= new JTextField(_pageNo+"");
			JButton pageButton = new JButton("Update Page No");
			pageButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					_pageNo = Integer.parseInt(_pageTF.getText().trim());
					_infoLabel.setText(_pageNo+"");					
				}
			});

			modulePanel = new JPanel( new GridLayout(2, 2, 5, 5));
			modulePanel.add(_p1Label);
			modulePanel.add(p1Button);
			//modulePanel.add(_p2Label);
			//modulePanel.add(p2Button);
			modulePanel.add(_pageTF);
			modulePanel.add(pageButton);

			iRow++;			
			modulePanel.setBorder(BorderFactory.createTitledBorder(loweredetched, "Rectangle:"));
			gbc= new GridBagConstraints(0, iRow, 3, 1, 1.0, 1.0, GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH, insets, 0, 0);
			add(modulePanel, gbc);

			
			modulePanel = new JPanel( new GridLayout(1, 1, 5, 5));
			modulePanel.add(_infoLabel);

			iRow++;			
			modulePanel.setBorder(BorderFactory.createTitledBorder(loweredetched, "Info:"));
			gbc= new GridBagConstraints(0, iRow, 3, 1, 1.0, 1.0, GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH, insets, 0, 0);
			add(modulePanel, gbc);

			//			iRow++;
			//			gbc= new GridBagConstraints(0, iRow, 5, 1, 1.0, 1.0, GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH, insets, 0, 0);
			//			add(new JLabel(" "), gbc);

			//addMouseMotionListener(_tmml);
		}
	}

	protected boolean okAction() {
		String sourceDir = _sourceFolderTF.getText().trim();
		//System.out.println(sourceDir);
		writeJpg(sourceDir);
		return true;
	}

	//https://github.com/bahusvel/JavaScreenCapture
	//https://stackoverflow.com/questions/321736/how-to-set-dpi-information-in-an-image?noredirect=1&lq=1
	private void writeJpg(String folder) {
		try {
			String format = "png"; //"jpg";
			String str = String.format("%03d", _pageNo); 
			_pageNo++;
			String fileName = folder +File.separator+"Page" + str+"."+format;

			Robot robot = new Robot();
			//String fileName = "D://PartScreenshot.jpg";
			//Rectangle screenRect = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
			//BufferedImage screenFullImage = robot.createScreenCapture(screenRect);
			//ImageIO.write(screenFullImage, "jpg", new File(fileName));


			// Define an area of size 500*400 starting at coordinates (10,50)
			Rectangle rectArea = new Rectangle(_p1Xmouse, _p1Ymouse, _p2Xmouse-_p1Xmouse, _p2Ymouse-_p1Ymouse);
			BufferedImage screenFullImage = robot.createScreenCapture(rectArea);
			ImageIO.write(screenFullImage, format, new File(fileName));
			_infoLabel.setText(fileName);
		} catch (AWTException | IOException ex) {
			System.err.println(ex);
		}

	}

	private class GlassFrame extends JFrame {

		public GlassFrame() {
			super();
			
			MyGlassPane myGlassPane = new MyGlassPane(null, null, null);
			//changeButton.addItemListener(myGlassPane);
			setGlassPane(myGlassPane);
			
			setUndecorated(true);
			AWTUtilities.setWindowOpacity(this, 0.5f);
			
			//setBounds(0, 0, getToolkit().getScreenSize().width, getToolkit().getScreenSize().height);
			//setBounds(0, 0, 500, 500);
			//setVisible(true);
			addMouseListener(_ml);
		}

		public void setVisible(boolean visible) {
			super.setVisible(visible);
			getGlassPane().setVisible(visible);
			System.out.println(getMonitorSizes());
			if(visible) {
				int width = 0;
				int height = 0;
				
				int iMethod = 0;
				if(iMethod==0) {
					setExtendedState(JFrame.MAXIMIZED_BOTH);
				} else if(iMethod==1) {
					//Cover all monitors
					GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
					GraphicsDevice[] gs = ge.getScreenDevices();
					for (GraphicsDevice curGs : gs) 	{
					  DisplayMode mode = curGs.getDisplayMode();
					  width += mode.getWidth();
					  height = mode.getHeight();
					}
					setSize(width, height);
					setLocationRelativeTo(null);
				} else if(iMethod==2) {
//					GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
//					GraphicsDevice gd = ge.getDefaultScreenDevice();
//					GraphicsConfiguration [] gc = gd.getConfigurations();
//					Rectangle gcBounds = gc[0].getBounds();			
//					int w = (int)(7.0*gcBounds.getWidth() / 10.0);
//					int h = (int)(7.0*gcBounds.getHeight() / 10.0);			
//					setSize(w, h);
//					//setSize(1000, 800);
//					setLocationRelativeTo(null);
				} else if(iMethod==3) {
					Rectangle2D result = new Rectangle2D.Double();
					GraphicsEnvironment localGE = GraphicsEnvironment.getLocalGraphicsEnvironment();
					for (GraphicsDevice gd : localGE.getScreenDevices()) {
					  for (GraphicsConfiguration graphicsConfiguration : gd.getConfigurations()) {
					    result.union(result, graphicsConfiguration.getBounds(), result);
					  }
					}
					setSize((int)result.getWidth(), (int)result.getHeight());
				}

			}
		}
		
		private String getMonitorSizes() {        
		    GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		    GraphicsDevice[]    gs = ge.getScreenDevices();
		    StringBuilder sb = new StringBuilder();
		    for (int i = 0; i < gs.length; i++) {
		        DisplayMode dm = gs[i].getDisplayMode();
		        sb.append(i + ", width: " + dm.getWidth() + ", height: " + dm.getHeight() + "\n");
		    }    
		    return sb.toString();
		}

	}

	class MyGlassPane extends JComponent {

		public MyGlassPane(AbstractButton aButton, JMenuBar menuBar, Container contentPane) {
			//CBListener listener = new CBListener(aButton, menuBar, this, contentPane);
			//addMouseListener(listener);
			//addMouseMotionListener(listener);
			setVisible(true);
		}

		protected void paintComponent(Graphics g) {
//			if (point != null) {
//				g.setColor(Color.red);
//				g.fillOval(point.x - 10, point.y - 10, 20, 20);
//			}
			float alpha = 0.1f;
            //AlphaComposite alcom = AlphaComposite.getInstance( AlphaComposite.SRC_OVER, alpha);
            //g.setComposite(alcom);
			g.setColor(Color.red);
            //g.fillRect(0, 0, getWidth(), getHeight());       
            //g.fillRect(10, 10, 50, 50); 
            g.drawRect(_p1Xmouse, _p1Ymouse, _p2Xmouse-_p1Xmouse, _p2Ymouse-_p1Ymouse);
            //System.out.println(_p1Xmouse+"");
		}
	}

	/**
	 * Listen for all events that our check box is likely to be
	 * interested in.  Redispatch them to the check box.
	 */
	class CBListener extends MouseInputAdapter {
		Toolkit toolkit;
		Component liveButton;
		JMenuBar menuBar;
		MyGlassPane glassPane;
		Container contentPane;

		public CBListener(Component liveButton, JMenuBar menuBar,MyGlassPane glassPane, Container contentPane) {
			toolkit = Toolkit.getDefaultToolkit();
			this.liveButton = liveButton;
			this.menuBar = menuBar;
			this.glassPane = glassPane;
			this.contentPane = contentPane;
		}

		public void mouseMoved(MouseEvent e) 	{ redispatchMouseEvent(e, false); }
		public void mouseDragged(MouseEvent e) 	{ redispatchMouseEvent(e, false); }
		public void mouseClicked(MouseEvent e) 	{ redispatchMouseEvent(e, false); }
		public void mouseEntered(MouseEvent e) 	{ redispatchMouseEvent(e, false); }
		public void mouseExited(MouseEvent e) 	{ redispatchMouseEvent(e, false); }

		//public void mousePressed(MouseEvent e) 	{ redispatchMouseEvent(e, false); }
		public void mousePressed(MouseEvent e) 	{ 
			System.out.println(e.getX()+", "+e.getY());
			PointerInfo inf = MouseInfo.getPointerInfo();
			Point p = inf.getLocation();
			System.out.println(p.x+", "+p.y);
		 }
		public void mouseReleased(MouseEvent e) { redispatchMouseEvent(e, true); }

		//A basic implementation of redispatching events.
		private void redispatchMouseEvent(MouseEvent e, boolean repaint) {
//			Point glassPanePoint = e.getPoint();
//			Container container = contentPane;
//			Point containerPoint = SwingUtilities.convertPoint(glassPane, glassPanePoint, contentPane);
//			if (containerPoint.y < 0) { //we're not in the content pane
//				if (containerPoint.y + menuBar.getHeight() >= 0) { 
//					//The mouse event is over the menu bar.
//					//Could handle specially.
//				} else { 
//					//The mouse event is over non-system window decorations, such as the ones provided by
//					//the Java look and feel.
//					//Could handle specially.
//				}
//			} else {
//				//The mouse event is probably over the content pane. Find out exactly which component it's over.  
//				Component component = SwingUtilities.getDeepestComponentAt(container, containerPoint.x, containerPoint.y);
//
//				if ((component != null) && (component.equals(liveButton))) {
//					//Forward events over the check box.
//					Point componentPoint = SwingUtilities.convertPoint(glassPane,glassPanePoint,component);
//					component.dispatchEvent(new MouseEvent(component,e.getID(),e.getWhen(),e.getModifiers(),
//							componentPoint.x, componentPoint.y, e.getClickCount(), e.isPopupTrigger()));
//				}
//			}
//
//			//Update the glass pane if requested.
//			if (repaint) {
//				//glassPane.setPoint(glassPanePoint);
//				glassPane.repaint();
//			}
		}



	}
}
