package com.geohammer.common;

import java.awt.Component;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseMotionListener;

import javax.swing.JLabel;
import javax.swing.KeyStroke;

import edu.mines.jtk.awt.Mode;
import edu.mines.jtk.awt.ModeManager;
import edu.mines.jtk.mosaic.Projector;
import edu.mines.jtk.mosaic.Tile;
import edu.mines.jtk.mosaic.TileAxis;
import edu.mines.jtk.mosaic.Transcaler;

public class CommonMouseTrackInfoMode extends Mode {
	private static final long serialVersionUID = 1L;

	public JLabel 			_trackingLabel 	= null;	
	public String 			_trackingLabelFormat 	= "x:%.2f, y:%.2f";	
	private double 			_vx 			= 0;
	private double 			_vy 			= 0;
	
	public CommonMouseTrackInfoMode(ModeManager modeManager) {
		super(modeManager);
		setName("Track");
		//setIcon(loadIcon(MouseTrackInfoMode.class,"Track24.gif"));
		//setMnemonicKey(KeyEvent.VK_Z);
		//setAcceleratorKey(KeyStroke.getKeyStroke(KeyEvent.VK_T,0));
		setShortDescription("Track mouse in tile");
	}

	public boolean isExclusive() { return false; }
	public void setTrackingLabel(JLabel trackingLabel) 		{ _trackingLabel = trackingLabel; }
	public void setTrackingLabelFormat(String trackingLabelFormat) { _trackingLabelFormat = trackingLabelFormat; }
	
	///////////////////////////////////////////////////////////////////////////
	// protected

	protected void setActive(Component component, boolean active) {
		if ((component instanceof Tile)) {
			if (active) {
				component.addMouseListener(_tml);
			} else {
				component.removeMouseListener(_tml);
			}
		}
	}

	///////////////////////////////////////////////////////////////////////////
	// private

	
	private Tile _tile; // tile in which tracking; null, if not tracking.
	private int _xmouse; // x coordinate where mouse last tracked
	private int _ymouse; // y coordinate where mouse last tracked  

	private MouseListener _tml = new MouseAdapter() {
		public void mouseEntered(MouseEvent e) {
			beginTracking(e);
		}
		public void mouseExited(MouseEvent e) {
			endTracking();
		}
		public void mousePressed(MouseEvent evt) {	}
		public void mouseDragged(MouseEvent evt)  {	 }
		public void mouseReleased(MouseEvent evt) { }
	};

	private MouseMotionListener _tmml = new MouseMotionAdapter() {
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
		_tile.addMouseMotionListener(_tmml);
	}
	private void beginTracking(TileAxis ta, int x, int y) { }

	private void duringTracking(MouseEvent e) {
		_xmouse = e.getX();
		_ymouse = e.getY();
		_tile = (Tile)e.getSource();
		fireTrack();
	}
	private void duringTracking(TileAxis ta, int x, int y) { }

	private void endTracking() {
		_tile.removeMouseMotionListener(_tmml);
		fireTrack();
		_tile = null;
	}
	private void endTracking(TileAxis ta) { }

	private void fireTrack() {
		if (_tile==null) {
		} else {
			int iRow = _tile.getRowIndex();
			int iCol = _tile.getColumnIndex();
			Projector hp = _tile.getHorizontalProjector();
			Projector vp = _tile.getVerticalProjector();
			Transcaler ts = _tile.getTranscaler();
			double ux = ts.x(_xmouse);
			double uy = ts.y(_ymouse);
			_vx = hp.v(ux);
			_vy = vp.v(uy);
			if(_trackingLabel!=null)  {
				_trackingLabel.setText(String.format(_trackingLabelFormat, _vx, _vy)+","+iRow+","+iCol);
				//System.out.println("I am here");
			}
			//System.out.println("I am here 0");
			
		}

	}
}