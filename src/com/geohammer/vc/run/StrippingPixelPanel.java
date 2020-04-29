package com.geohammer.vc.run;

import java.awt.Color;
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

import javax.swing.JColorChooser;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JTextField;
import javax.swing.KeyStroke;

import com.geohammer.common.TextViewDialog;

import com.geohammer.common.CommonFrame;
import com.geohammer.common.CommonMouseEditingMode;
import com.geohammer.common.CommonPanel;
import com.geohammer.vc.VcFrame;

import edu.mines.jtk.awt.ColorMap;
import edu.mines.jtk.awt.Mode;
import edu.mines.jtk.awt.ModeManager;
import edu.mines.jtk.dsp.Sampling;
import edu.mines.jtk.mosaic.ContoursViewDouble;
import edu.mines.jtk.mosaic.GridView;
import edu.mines.jtk.mosaic.Mosaic;
import edu.mines.jtk.mosaic.PixelsViewDouble;
import edu.mines.jtk.mosaic.Projector;
import edu.mines.jtk.mosaic.Tile;
import edu.mines.jtk.mosaic.Transcaler;


public class StrippingPixelPanel extends CommonPanel {
	final VcFrame _frame;
	Sampling 	_firstIndexSampling 	= null;
	Sampling 	_secondIndexSampling 	= null;
	int 		_nContours 				= 12;
	JTextField 	_nContoursTF 			= null;
	float 		_lineWidth 				= 2.0f;
	JTextField 	_lineWidthTF 			= null;
	
	Color 		_lineColor 				= null;
	double[][] 	_data 					= null;
	private JLabel 		_minValueLabel 	= null;
	private JLabel 		_trackingLabel 	= null;
	private boolean 	_enableTracking = false;
	
	public StrippingPixelPanel(VcFrame frame, String hLabel, String vLabel, 
			Sampling firstIndexSampling, Sampling secondIndexSampling, JLabel minValueLabel, JLabel trackingLabel) {
		super(1, 1, Orientation.X1RIGHT_X2UP, AxesPlacement.LEFT_TOP, null);
		setBackground(Color.white);
    	setHLabel(0, hLabel);
	    setVLabel(0, vLabel);
	    setFrame(frame);
	    _frame 			= frame;
	    _minValueLabel 	= minValueLabel;
	    _trackingLabel 	= trackingLabel;
	    
		_firstIndexSampling 	= firstIndexSampling;
		_secondIndexSampling 	= secondIndexSampling;
		Font font = new Font ("Arial", Font.BOLD, 12); //Monospaced, Serif, Dialog, Sanserif
		setFont(font);
		init();
	}
	
	public void init() {		
		setEnableTracking(false);
		setEnableEditing(true);
		setEnableZoom(true);

		addModeManager();
		CommonMouseEditingMode mouseEditingMode = getMouseEditingMode();
		mouseEditingMode.setShowAutoAxisLimit(false);
		mouseEditingMode.setJComponent(genJMenuItem());

		Mosaic mosaic = getMosaic();		
		ModeManager modeManager = mosaic.getModeManager();
		MouseTrackInfoMode mouseTrackInfoMode = new MouseTrackInfoMode(modeManager);
		//mouseTrackInfoMode.setTrackingLabel(_trackingLabel);
		mouseTrackInfoMode.setActive(true);		
	}
	
	private JComponent[] genJMenuItem() {
		_nContoursTF 			= new JTextField(_nContours+"");
		JMenuItem jMenuItem1 = new JMenuItem("Set Num Of Contours");		
		jMenuItem1.addActionListener(new ActionListener() 	{
			public void actionPerformed(ActionEvent e)   {
				_nContours = Integer.parseInt(_nContoursTF.getText().trim());
				updateContourViewDouble(_nContours, _lineWidth, _lineColor);
			}
		});
		_lineWidthTF 			= new JTextField(_lineWidth+"");
		JMenuItem jMenuItem2 = new JMenuItem("Set Contour Line Width");		
		jMenuItem2.addActionListener(new ActionListener() 	{
			public void actionPerformed(ActionEvent e)   {
				_lineWidth = Float.parseFloat(_lineWidthTF.getText().trim());
				updateContourViewDouble(_nContours, _lineWidth, _lineColor);
			}
		});
		
		JMenuItem jMenuItem3 = new JMenuItem("Choose Line Color");		
		jMenuItem3.addActionListener(new ActionListener() 	{
			public void actionPerformed(ActionEvent e)   {
				Color newColor = JColorChooser.showDialog(_frame,
	                     "Choose Contour Line Color", _lineColor);
	                if(newColor != null){
	                	updateContourViewDouble(_nContours, _lineWidth, newColor);
	                }
			}
		});
		
		JMenuItem jMenuItem10 = new JMenuItem("View/Save Data");		
		jMenuItem10.addActionListener(new ActionListener() 	{
			public void actionPerformed(ActionEvent e)   {
				StringBuilder text = new StringBuilder("");

				for(int i=0; i<_data.length; i++) {
					for(int j=0; j<_data[i].length; j++) {
						text.append(_data[i][j]+" ");
					}
					text.append(System.getProperty("line.separator"));
				}

				TextViewDialog dialog = new TextViewDialog(_frame, "Data Context", false, text.toString());
				dialog.showDialog();
			}
		});
		

		return new JComponent[] {_nContoursTF, jMenuItem1, _lineWidthTF, jMenuItem2, jMenuItem3,
				new JLabel(""), jMenuItem10};
	}
	
	public void addBackgroundGrid(boolean visible) {
		GridView gv = new GridView(Color.LIGHT_GRAY);
		gv.setName("Stripping", "Major Grid");
	    //gv.setVisible(getVisible(gv.getCategory(), gv.getName()));
	    gv.setVisible(visible);
	    removeView(gv.getCategory(), gv.getName());
		addGridView(0, 0, gv);
	}
	
	public double [] calMinValues() {
		if(_minValueLabel==null) return null;
		double min = _data[0][0];
		int minI = 0;
		int minJ = 0;
		for(int i=0; i<_data.length; i++) {
			for(int j=0; j<_data[i].length; j++) {
				if(min>_data[i][j]) {
					min = _data[i][j];
					minI = i;
					minJ = j;
				}
			}
		}
		double vmI = _secondIndexSampling.getValue(minI); //m
		double vnJ = _firstIndexSampling.getValue(minJ);
		String htmlText = "<html>" + "<font size=\"4\" color=\"black\"><b>"+ 
		" min RMS="+ ((int)(10000*min))/10000.0 +" at x="+
				((int)(10000*vnJ))/10000.0 +" y="+((int)(10000*vmI))/10000.0 +" </b></font>  ";
		_minValueLabel.setText(htmlText);
		_enableTracking = true;
		return new double[] {vmI, vnJ, min};
	}
	
	public void updateContourViewDouble(int nContours, float lineWidth, Color lineColor) {
		_nContours = nContours;
		_lineWidth = lineWidth;
		_lineColor = lineColor;
		ContoursViewDouble cv = new ContoursViewDouble(_firstIndexSampling, _secondIndexSampling, _data);
	    cv.setColorModel(ColorMap.JET);
	    cv.setContours(nContours);
	    cv.setLineColor(lineColor);
	    cv.setLineWidth(lineWidth);
		cv.setName("Stripping", "Contour");
	    removeViews(0, 0, cv.getCategory(), cv.getName());
		addContoursViewDouble(0, 0, cv);
	}
	
	public void update(double[][] f) {
		_data = f;
	    PixelsViewDouble pv = new PixelsViewDouble(_firstIndexSampling, _secondIndexSampling, f);
	    pv.setInterpolation(PixelsViewDouble.Interpolation.LINEAR);
	    //pv.setInterpolation(PixelsViewDouble.Interpolation.NEAREST);
	    pv.setColorModel(ColorMap.JET);
	    pv.setPercentiles(0.0f,100.0f);
		pv.setName("Stripping", "Pixel");
	    removeViews(0, 0, pv.getCategory(), pv.getName());
		addPixelsViewDouble(0, 0, pv);
	}
	
	
	public static float [] toFloatArray(double [] d) {
		float [] f = new float[d.length];
		for(int i=0; i<f.length; i++) f[i] = (float)d[i];
		return f;
	}
	
	public class MouseTrackInfoMode extends Mode {
		private static final long serialVersionUID = 1L;
		
		private double _vx = 0;
		private double _vy = 0;

		private Tile _tile = null; // tile in which tracking; null, if not tracking.
		private int _xmouse; // x coordinate where mouse last tracked
		private int _ymouse; // y coordinate where mouse last tracked 

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
					//component.addMouseMotionListener(_mml);
				} else {
					component.removeMouseListener(_ml);
					//component.removeMouseMotionListener(_mml);
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
				//_currTile = (Tile)evt.getSource();
				_xmouse 		= evt.getX();
				_ymouse 		= evt.getY();
				if(!evt.isShiftDown()){
					Projector hp = _tile.getHorizontalProjector();
					Projector vp = _tile.getVerticalProjector();
					Transcaler ts = _tile.getTranscaler();
					double ux = ts.x(_xmouse);
					double uy = ts.y(_ymouse);
				}
			}
			public void mouseDragged(MouseEvent evt)  {	 }
			public void mouseReleased(MouseEvent evt) {
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

				if(_enableTracking) {
					int ki = _secondIndexSampling.indexOfNearest(_vy); //m
					int kj = _firstIndexSampling.indexOfNearest(_vx);
					double v = _data[ki][kj];
					//double v = 0;

					String htmlText = "<html>" + "<font size=\"4\" color=\"black\"><b>"+((int)(1000*v))/1000.0 +" "+
							//" ki="+ki +" kj="+kj +" "+
							((int)(10000*_vx))/10000.0 +" "+((int)(10000*_vy))/10000.0 +" </b></font>  ";
					_trackingLabel.setText(htmlText);
				}
			}
		}
	}

}

