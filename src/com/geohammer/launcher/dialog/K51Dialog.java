package org.ucdm.launcher.dialog;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseMotionListener;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.KeyStroke;

import org.ucdm.common.CommonDialog;
import org.ucdm.common.CommonMouseEditingMode;
import org.ucdm.common.CommonPanel;
import org.ucdm.component.StatusBar;
import org.ucdm.launcher.LauncherFrame;
import org.ucdm.resource.Resource;

import edu.mines.jtk.awt.Mode;
import edu.mines.jtk.awt.ModeManager;
import edu.mines.jtk.mosaic.GridView;
import edu.mines.jtk.mosaic.Mosaic;
import edu.mines.jtk.mosaic.PointsView;
import edu.mines.jtk.mosaic.Projector;
import edu.mines.jtk.mosaic.Tile;
import edu.mines.jtk.mosaic.Transcaler;


public class K51Dialog extends CommonDialog {

	LauncherFrame 		_frame 			= null;

	public K51Dialog(JFrame aParent, String aTitle, boolean modal) {
		super(aParent, aTitle, modal);
		setDialogWindowSize(800, 800);
		_frame 	= (LauncherFrame)aParent;
	}

	@Override
	protected JComponent createContents() {
		K51Panel k51Panel = new K51Panel(_frame, 1, 0);
		k51Panel.updateDialog();
		return  k51Panel;
	}
	@Override
	protected boolean okAction() { return true; }
	@Override
	protected JComponent getCommandRow() { return null; }
	
	public class K51Panel extends CommonPanel { 		

		K51Option 			_k51Option 		= new K51Option();
		private int 		_nRows 			= -1;
		private int 		_iType 			= -1;
		
		public K51Panel(LauncherFrame frame, int nRows, int iType) {
			super(nRows,1,CommonPanel.Orientation.X1RIGHT_X2UP, CommonPanel.AxesPlacement.LEFT_BOTTOM, true, null);
			setBackground(Color.white);
			_nRows 			= nRows;
			_iType 			= iType;
			setFrame(frame);
			_frame = frame;
			_k51Option = new K51Option();

			setMinimumSize(new Dimension(800, 100));     
			setPreferredSize(new Dimension(800, 250));
			//getMosaic().getTileAxisBottom(0).setShowTimeFormatId(4);
			init();
		}
		
		public void init() {	
			addStatusBar(true, genStatusBar());	
			
			setEnableTracking(false);
			setEnableEditing(true);
			setEnableZoom(true);
			
			addModeManager();
			CommonMouseEditingMode mouseEditingMode = getMouseEditingMode();
			mouseEditingMode.setShowAutoAxisLimit(false);
			//mouseEditingMode.setJComponent(genJMenuItem());
			
			Mosaic mosaic = getMosaic();		
			ModeManager modeManager = mosaic.getModeManager();
			MouseTrackInfoMode mouseTrackInfoMode = new MouseTrackInfoMode(modeManager);		
			mouseTrackInfoMode.setActive(true);	

			setHLabel(0, "X");
			setVLabel(0, "Y");
		}
		
		public StatusBar genStatusBar() {
			//https://www.daniweb.com/programming/software-development/threads/476264/how-to-resize-jbutton-according-to-size-of-picture
			
			JPanel oakpane = new JPanel();
			oakpane.setLayout(new BoxLayout(oakpane, BoxLayout.X_AXIS));
			//oakpane.setBackground(Color.white);
			JPanel pa = new JPanel();
			pa.setBackground(Color.white);
			Dimension dBt = new Dimension(20,20);
			JButton settingsButton = new JButton(Resource.loadImageIcon("settingsmall.png"));
			//settingsButton.setBackground(Color.red);
			settingsButton.setToolTipText("Global Settings");
			settingsButton.setMinimumSize(dBt);
			settingsButton.setPreferredSize(dBt);
			settingsButton.setMaximumSize(dBt);
			settingsButton.setSize(dBt);
			settingsButton.addActionListener(new ActionListener() {
				public void actionPerformed( ActionEvent e ) {
					String aTitle = new String("Pressure Signal Processing");
					K51OptionDialog dialog = new K51OptionDialog(_frame, aTitle, 
							false, K51Panel.this, _k51Option);				
					dialog.showDialog();
				}
			});
			pa.add(settingsButton);
			oakpane.add(pa);
			
			StatusBar statusBar = new StatusBar(); 
			//statusBar.setZoneBorder(BorderFactory.createLineBorder(Color.GRAY)); 
			statusBar.setZoneBorder(null); 
			statusBar.setZones( new String[] { "Settings", "Time", "Tracking", "remaining_zones" },     
					new Component[] {oakpane, new JLabel("Time1"), new JLabel("Tracking1"), new JLabel("remaining")     },     
					new String[] {"5%", "20%", "20%", "*"} );
			
			_trackingLabel	= (JLabel)statusBar.getZone("Tracking");

			_trackingLabel	= null;
			return statusBar;
		}	
		
		public float[] getDomain(float x1, float x2, int n) {
			float [] x = new float[n];
			float dx = (x2-x1)/(n-1);
			for(int i=0; i<n; i++) {
				x[i] = x1+i*dx;
			}
			return x;
		}
		
		public float getY(int iCurve, float b, float x) {
			if(iCurve==0) {
				return (x-b)*(x-b)+3;				
			} else if(iCurve==1) {
				return (x-b)*(x-b)-b*b+2;				
			} else if(iCurve==2) {
				return -(x-b)*(x-b)-b*b+2;				
			} else {
				return 0;
			}
		}
		
		
		public void updateDialog() {
			removeAllViews();
			
			GridView gv = new GridView(Color.LIGHT_GRAY);
			gv.setName("Well", "Grid Major");
		    gv.setVisible(getVisible(gv.getCategory(), gv.getName()));
		    removeView(gv.getCategory(), gv.getName());
			addGridView(0, 0, gv);
			
			PointsView pv	 = null;
			
			int iRow = 0;
			float x1 = _k51Option.getX1();
			float x2 = _k51Option.getX2();
			int np = _k51Option.getNPoints();
			float [] x = getDomain(x1, x2, np);
			float [] y = new float[x.length];
			for(int i=0; i<x.length; i++) {
				y[i] = getY(_k51Option.getICurveType(), _k51Option.getB(), x[i]);
			}
			
			pv	 = new PointsView(x, y);
			pv.setStyle("k--");
			pv.setLineWidth(1.5f);
			pv.setName("Well", "Raw"); 			
			pv.setVisible(getVisible(pv.getCategory(), pv.getName()));
			addTiledView(0, 0, pv);
			
//			x1 = _k51Option.getA();
//			x2 = _k51Option.getA()+4;
//			np = _k51Option.getNPoints()/2;
//			x = getDomain(x1, x2, np);
//			y = new float[x.length];
//			for(int i=0; i<x.length; i++) {
//				y[i] = getY(_k51Option.getICurveType(), _k51Option.getB(), x[i]);
//			}
			
//			pv	 = new PointsView(x, y);
//			pv.setStyle("r-");
//			pv.setLineWidth(4.5f);
//			pv.setName("Well", "Domain"); 			
//			pv.setVisible(getVisible(pv.getCategory(), pv.getName()));
//			addTiledView(0, 0, pv);
			x1 = 0;
			pv	 = new PointsView(new float[] {x1, x1}, new float[] {0, -20});
			pv.setStyle("k--");
			pv.setLineWidth(4.5f);
			pv.setName("Well", "Domain1"); 			
			pv.setVisible(getVisible(pv.getCategory(), pv.getName()));
			addTiledView(0, 0, pv);
			
			x2 = 1;
			pv	 = new PointsView(new float[] {x2, x2}, new float[] {0, -20});
			pv.setStyle("k--");
			pv.setLineWidth(4.5f);
			pv.setName("Well", "Domain2"); 			
			pv.setVisible(getVisible(pv.getCategory(), pv.getName()));
			addTiledView(0, 0, pv);
			
			
			pv	 = new PointsView(new float[] {_k51Option.getB(), _k51Option.getB()}, new float[] {0, -10});
			pv.setStyle("r--");
			pv.setLineWidth(1.5f);
			pv.setName("Well", "Symmetry"); 			
			pv.setVisible(getVisible(pv.getCategory(), pv.getName()));
			addTiledView(0, 0, pv);
			
			setHLimits(-6, 6);
		}
		
		
		public class MouseTrackInfoMode extends Mode {
			private static final long serialVersionUID = 1L;
			private JLabel 	_trackingLabel 		= null;

			private double _vx = 0;
			private double _vy = 0;

			private Tile _tile; // tile in which tracking; null, if not tracking.
			private int _xmouse; // x coordinate where mouse last tracked
			private int _ymouse; // y coordinate where mouse last tracked 

			private int _ymouse0; // y coordinate where mouse last tracked
			private int _ymouse1; // y coordinate where mouse last tracked

			public MouseTrackInfoMode(ModeManager modeManager) {
				super(modeManager);
				setName("Track");
				setIcon(loadIcon(MouseTrackInfoMode.class,"Track24.gif"));
				setMnemonicKey(KeyEvent.VK_Z);
				setAcceleratorKey(KeyStroke.getKeyStroke(KeyEvent.VK_T,0));
				setShortDescription("Track mouse in tile");
			}

			public boolean isExclusive() { return false; }
			public void setTrackingLabel( JLabel trackingLabel) { _trackingLabel = trackingLabel; }

			protected void setActive(Component component, boolean active) {
				if ((component instanceof Tile)) {
					if (active) {
						component.addMouseListener(_ml);
						//component.setFocusable(true);
						//component.requestFocusInWindow();
						//component.addKeyListener(_kl);
					} else {
						component.removeMouseListener(_ml);
						//component.setFocusable(false);
						//component.removeKeyListener(_kl);
					}
				}
			}
			
			private KeyListener _kl = new KeyAdapter() {
				public void keyTyped(KeyEvent e) { 
					System.out.println("p=");
				}
				public void keyReleased(KeyEvent e) { }
				public void keyPressed(KeyEvent e) {
					System.out.println("q=");
				}
			};
			
			private MouseListener _ml = new MouseAdapter() {
				public void mouseEntered(MouseEvent e) {
					beginTracking(e);
				}
				public void mouseExited(MouseEvent e) {
					endTracking();
				}
				public void mousePressed(MouseEvent evt) {					
					//setPicks(evt); 
					Projector hp = _tile.getHorizontalProjector();
					Projector vp = _tile.getVerticalProjector();
					Transcaler ts = _tile.getTranscaler();

					int id = (int)(_vy-0.5);
					double clickY = id+0.5;

					double y = vp.u(clickY);
					_ymouse0 = ts.y(y);
					y = vp.u(clickY+1.0);
					_ymouse1 = ts.y(y);
				}
				public void mouseDragged(MouseEvent evt)  {	 }
				public void mouseReleased(MouseEvent evt) {  }
			};

			private MouseListener _ml2 = new MouseAdapter() {
				public void mousePressed(MouseEvent evt) {
					//PressurePointsView lv = _pressurePointsView;
					//PressurePointsView lv = null;
//					if(lv==null) return;
//
//					
//					if((!evt.isShiftDown())&& (!evt.isAltDown())) lv.mousePressed(evt);
//
//					if((!evt.isControlDown())&&(!evt.isAltDown())&&(!evt.isShiftDown())&&evt.isMetaDown()) { //Press mouse right button
//						int iComp = lv.getSelectedCompIndex();
//						if(iComp<0) return;
//						int sh = lv.getP2(iComp, 1);				
//						if(sh>10) {
//							_beginTF = new JTextField((sh-50)+"");
//							_endTF = new JTextField((sh+100)+"");
//						} else {
//							_beginTF = new JTextField("0");
//							_endTF = new JTextField("0");
//						}
//						_beginWTF = new JTextField("0");
//						_endWTF = new JTextField("0");
//						_popupMenu = new JPopupMenu();
//						setPopupMenu(lv);
//						_popupMenu.show(evt.getComponent(), evt.getX(), evt.getY());
//					}
				}

				public void mouseReleased(MouseEvent evt) { 
					if((!evt.isShiftDown())&&(!evt.isAltDown())) {
//						PressurePointsView lv = _pressurePointsView;
//						if(lv==null) return;
//						lv.mouseReleased(evt);
//						if((!evt.isControlDown())&&(!evt.isAltDown())&&(!evt.isShiftDown())&&(!evt.isMetaDown())) { 
//							lv.getFrame().updateHodogramDialog();
//						}
					}
				}			
			};
			
			private MouseMotionListener _mml = new MouseMotionAdapter() {
				public void mouseDragged(MouseEvent evt)  {
					duringTracking(evt);
					if(evt.isControlDown()&&(!evt.isShiftDown())&& (!evt.isAltDown())) {					
						//System.out.println(String.format("x:%.2f, y:%.2f", _vx, _vy));
						_k51Option.setA((float)_vx);
						updateDialog();
					}
					
					if(!evt.isControlDown()&&(!evt.isShiftDown())&& (evt.isAltDown())) {					
						//System.out.println(String.format("x:%.2f, y:%.2f", _vx, _vy));
						_k51Option.setB((float)_vx);
						_k51Option.setX1(_k51Option.getB()-6);
						_k51Option.setX2(_k51Option.getB()+6);
						updateDialog();
					}
				}

				public void mouseMoved(MouseEvent e) {
					duringTracking(e);
				}
			};

			private void beginTracking(MouseEvent e) {
				_xmouse = e.getX();
				_ymouse = e.getY();
				_tile = (Tile)e.getSource();
				fireTrack();
				_tile.addMouseListener(_ml2);
				_tile.addMouseMotionListener(_mml);
			}

			private void endTracking() {
				_tile.removeMouseListener(_ml2);
				_tile.removeMouseMotionListener(_mml);
				fireTrack();
				_tile = null;
			}

			private void duringTracking(MouseEvent e) {
				_xmouse = e.getX();
				_ymouse = e.getY();
				_tile = (Tile)e.getSource();
				fireTrack();
				//if(_ymouse>_ymouse0&&_ymouse<_ymouse1) setPicks(e);
			}

			private void fireTrack() {
				if (_tile==null) {
				} else {
					Projector hp = _tile.getHorizontalProjector();
					Projector vp = _tile.getVerticalProjector();
					Transcaler ts = _tile.getTranscaler();
					double ux = ts.x(_xmouse);
					double uy = ts.y(_ymouse);
					_vx = hp.v(ux);
					_vy = vp.v(uy);
					if(_trackingLabel!=null) {
						_trackingLabel.setText(String.format("x:%.2f, y:%.2f", _vx, _vy));
					}
				}
			}
		}
		
	}
	
	

}
