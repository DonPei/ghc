package com.geohammer.vc;

import java.awt.Component;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseMotionListener;

import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

import com.geohammer.common.CommonMouseEditingMode;
import com.geohammer.common.CommonPanel;
import com.geohammer.component.StatusBar;
import com.geohammer.component.TransparentPixelsView;
import com.geohammer.core.planarmodel.Layer2D;
import com.geohammer.vc.RaypathView;

import edu.mines.jtk.awt.Mode;
import edu.mines.jtk.awt.ModeManager;
import edu.mines.jtk.mosaic.PointsView;
import edu.mines.jtk.mosaic.Projector;
import edu.mines.jtk.mosaic.Tile;
import edu.mines.jtk.mosaic.Transcaler;


public class Vel2dPanel extends CommonPanel {
	private static final long serialVersionUID = 1L;

	private boolean _enableDrawLine 	= false;
	private boolean _enableDrawRec 		= false;
	private float [][] _distanceP 		= new float[][]{ {0, 0}, {0, 0}};

	public Vel2dPanel(int nrow, int ncol, Orientation orientation, AxesPlacement axesPlacement, StatusBar statusBar) {
		super(nrow,ncol,orientation, axesPlacement, statusBar);
		Font font = new Font ("Arial", Font.BOLD, 12); //Monospaced, Serif, Dialog, Sanserif
		setFont(font);
	}

	public Vel2dView addVel2dView(int irow, int icol, Vel2dView mv) {
		Font font = new Font ("Arial", Font.BOLD, 12); //Monospaced, Serif, Dialog, Sanserif
		setFont(font);
		
		if (_orientation==Orientation.X1RIGHT_X2UP) {
			mv.setOrientation(Vel2dView.Orientation.X1RIGHT_X2UP);
		} else if (_orientation==Orientation.X1DOWN_X2RIGHT) {
			mv.setOrientation(Vel2dView.Orientation.X1DOWN_X2RIGHT);
		}
		addTiledView(irow,icol,mv);
		return mv;
	}
	
	public RaypathView addRaypathView(int irow, int icol, RaypathView rv) {
		if (_orientation==Orientation.X1RIGHT_X2UP) {
			rv.setOrientation(RaypathView.Orientation.X1RIGHT_X2UP);
		} else if (_orientation==Orientation.X1DOWN_X2RIGHT) {
			rv.setOrientation(RaypathView.Orientation.X1DOWN_X2RIGHT);
		}
		addTiledView(irow,icol,rv);
		return rv;
	}

	public TransparentPixelsView addTransparentPixelsView(int irow, int icol, TransparentPixelsView rv) {
		if (_orientation==Orientation.X1RIGHT_X2UP) {
			rv.setOrientation(TransparentPixelsView.Orientation.X1RIGHT_X2UP);
		} else if (_orientation==Orientation.X1DOWN_X2RIGHT) {
			rv.setOrientation(TransparentPixelsView.Orientation.X1DOWN_X2RIGHT);
		}
		addTiledView(irow,icol,rv);
		return rv;
	}
	
	public void init() {
		setEnableDrawLine(false);
		setEnableTracking(true);
		setEnableEditing(true);
		setEnableZoom(true);
		
		addModeManager();
		CommonMouseEditingMode mouseEditingMode = getMouseEditingMode();
		mouseEditingMode.setShowAutoAxisLimit(false);
		mouseEditingMode.setJComponent(genJMenuItem());
	}
	
	private JMenuItem[] genJMenuItem() {		
//		JMenuItem jMenuItem2 = new JMenuItem("Statistics");		
//		jMenuItem2.addActionListener(new ActionListener() 	{
//			public void actionPerformed(ActionEvent e)   {
//				String movieDir = _frame.getCwd()+"dotImage";
//				DotPlayerDialog dialog = new DotPlayerDialog(_world, "Dot Player", false, movieDir, SectionDialog.this);
//				dialog.showDialog();
//			}
//		});
		
		JMenuItem jMenuItem1 = new JMenuItem("Histogram");		
		jMenuItem1.addActionListener(new ActionListener() 	{
			public void actionPerformed(ActionEvent e)   {
				String title = "Histogram";
				//HistogramDialog dialog = new HistogramDialog(_frame, title, false, _world, 0);
				//dialog.showDialog();
			}
		});
		
		
				
		return new JMenuItem[] { jMenuItem1 };
	}
	
	public void refresh() {
//		int nrow = getMosaic().countRows();
//		int ncol = getMosaic().countColumns();
//		for (int irow=0; irow<nrow; ++irow) {
//			for (int icol=0; icol<ncol; ++icol) {
//				Tile tile = getTile(irow,icol);
//				int N = tile.countTiledViews();
//				for(int k=0; k<N; k++) {
//					try{
//						MapView mv = (MapView)tile.getTiledView(k);
//						mv.refresh();
//					} catch(java.lang.ClassCastException e) { }
//				}
//			}
//		}
	}
	public void setEnableDrawLine(boolean enableDrawLine) 	{ _enableDrawLine = enableDrawLine; }
	public void setEnableDrawRec(boolean enableDrawRec) 	{ _enableDrawRec = enableDrawRec; }
	private Vel2dPanel getWorld() { return this; }
	public void drawLine() {
		PointsView pv = null;
		if(getWorld().getTile(0, 0).getTileAxisBottom()==null) {
			pv = new PointsView(_distanceP[1], _distanceP[0]);
		} else {
			pv = new PointsView(_distanceP[0], _distanceP[1]);
		}
		pv.setName("Measurement", "Distance");
		pv.setStyle("r-");
		pv.setLineWidth(2);
		getWorld().removeView(pv.getCategory(), pv.getName());
		getWorld().addPointsView(0, 0, pv);
	}
	public void removeLine() {
		getWorld().removeView("Measurement", "Distance");
	}
	public void drawRec() {
		PointsView pv = null;
//		float x1 = _distanceP[1][0];
//		float x3 = _distanceP[1][1];
//		float y1 = _distanceP[0][0];
//		float y3 = _distanceP[0][1];
//		if(getWorld().getTile(0, 0).getTileAxisBottom()==null) {
//			pv = new PointsView(new float[]{x1, x3, x3, x1, x1}, new float[]{y1, y1, y3, y3, y1});
//		} else {
//			//pv = new PointsView(_distanceP[0], _distanceP[1]);
//			pv = new PointsView(new float[]{y1, y3, y3, y1, y1}, new float[]{x1, x1, x3, x3, x1});
//		}
		float v1 = _distanceP[1][0];
		float v3 = _distanceP[1][1];
		float h1 = _distanceP[0][0];
		float h3 = _distanceP[0][1];
		if(getWorld().getTile(0, 0).getTileAxisBottom()==null) {
			pv = new PointsView(new float[]{v1, v3, v3, v1, v1}, new float[]{h1, h1, h3, h3, h1});
		} else {
			//pv = new PointsView(_distanceP[0], _distanceP[1]);
			pv = new PointsView(new float[]{h1, h3, h3, h1, h1}, new float[]{v1, v1, v3, v3, v1});
		}
		pv.setName("Measurement", "Rectangle");
		pv.setStyle("r--");
		pv.setLineWidth(1);
		getWorld().removeView(pv.getCategory(), pv.getName());
		getWorld().addPointsView(0, 0, pv);
	}
	public void removeRec() {
		getWorld().removeView("Measurement", "Rectangle");
		float v1 = _distanceP[1][0];
		float v3 = _distanceP[1][1];
		float h1 = _distanceP[0][0];
		float h3 = _distanceP[0][1];
		if(getWorld().getTile(0, 0).getTileAxisBottom()==null) {
		} else {
			float emin = h1<h3?h1:h3;
			float emax = h1>h3?h1:h3;
			float nmin = v1<v3?v1:v3;
			float nmax = v1>v3?v1:v3;
			//_tv.toggleSelected(emin, emax, nmin, nmax);
		}
	}
	public double calDistance() 				{ 
		double a=_distanceP[0][0]-_distanceP[0][1];
		double b=_distanceP[1][0]-_distanceP[1][1];
		return Math.sqrt(a*a+b*b);
	}

	public class MouseTrackInfoMode1 extends Mode {
		private static final long serialVersionUID = 1L;
		private JLabel 	_trackingLabel 		= null;

		private double _vx = 0;
		private double _vy = 0;
		boolean _drawingLine = false;
		boolean _drawingRec = false;

		private Tile _tile; // tile in which tracking; null, if not tracking.
		private int _xmouse; // x coordinate where mouse last tracked
		private int _ymouse; // y coordinate where mouse last tracked 

		public MouseTrackInfoMode1(ModeManager modeManager) {
			super(modeManager);
			setName("Track");
			setIcon(loadIcon(MouseTrackInfoMode1.class,"Track24.gif"));
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
				} else {
					component.removeMouseListener(_ml);
				}
			}
		} 

		private MouseListener _ml = new MouseAdapter() {
			public void mouseEntered(MouseEvent e) {
				beginTracking(e);
			}
			public void mouseExited(MouseEvent e) {
				endTracking();
			}
			public void mousePressed(MouseEvent evt) {			
				_xmouse 		= evt.getX();
				_ymouse 		= evt.getY();
				if(!evt.isShiftDown()){
					if((!evt.isAltDown())&&evt.isControlDown()&&_enableDrawLine) {
						_drawingLine = false;
						_drawingRec = true;
					} 
					if((!evt.isControlDown())&&evt.isAltDown()&&_enableDrawRec) {
						_drawingLine = true;
						_drawingRec = false;
					} 
					Projector hp = _tile.getHorizontalProjector();
					Projector vp = _tile.getVerticalProjector();
					Transcaler ts = _tile.getTranscaler();
					double ux = ts.x(_xmouse);
					double uy = ts.y(_ymouse);
					_distanceP[0][0] = (float)hp.v(ux);
					_distanceP[1][0] = (float)vp.v(uy);
				}
			}
			public void mouseDragged(MouseEvent evt)  {	 }
			public void mouseReleased(MouseEvent evt) {
				_drawingLine 	= false;
				_drawingRec 	= false;
				if(!evt.isShiftDown()){
					if((!evt.isAltDown())&&evt.isControlDown()&&_enableDrawLine) {
						removeRec();
					} 
					if((!evt.isControlDown())&&evt.isAltDown()&&_enableDrawRec) {
						removeLine();
					}
				}
			}
		};

		private MouseMotionListener _mml = new MouseMotionAdapter() {
			public void mouseDragged(MouseEvent e) {
				duringTracking(e);
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
			_tile.addMouseMotionListener(_mml);
		}

		private void endTracking() {
			_tile.removeMouseMotionListener(_mml);
			fireTrack();
			_tile = null;
		}
		
		private void duringTracking(MouseEvent e) {
			_xmouse = e.getX();
			_ymouse = e.getY();
			_tile = (Tile)e.getSource();
			fireTrack();
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
				boolean calDistance = false;
				if(_drawingLine) {
					_distanceP[0][1] = (float)_vx;
					_distanceP[1][1] = (float)_vy;
					calDistance = true;
					drawLine();
				} 
				if(_drawingRec) {
					_distanceP[0][1] = (float)_vx;
					_distanceP[1][1] = (float)_vy;
					drawRec();
				} 
				if(_trackingLabel!=null) {
					if(calDistance) _trackingLabel.setText(String.format("%.1f, %.1f, d:%.1f", _vx, _vy, calDistance()));
					else _trackingLabel.setText(String.format("x:%.2f, y:%.2f", _vx, _vy));
				}
			}
		}
	}

}
