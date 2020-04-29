package com.geohammer.vc;

import java.awt.*;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.awt.image.IndexColorModel;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.imageio.ImageIO;

import edu.mines.jtk.awt.ColorMap;
import edu.mines.jtk.awt.ColorMapListener;
import edu.mines.jtk.awt.ColorMapped;
import edu.mines.jtk.mosaic.Projector;
import edu.mines.jtk.mosaic.Tile;
import edu.mines.jtk.mosaic.TiledView;
import edu.mines.jtk.mosaic.Transcaler;
import edu.mines.jtk.mosaic.PixelsView.Interpolation;

import com.geohammer.common.VeconUtils;
import com.geohammer.core.planarmodel.FlatLayer1D;
import com.geohammer.core.planarmodel.Layer2D;
import com.geohammer.core.planarmodel.Layer3D;
import com.geohammer.core.planarmodel.SingleLayer2D;

import edu.mines.jtk.util.ArrayMath;
import edu.mines.jtk.util.Clips;
import static edu.mines.jtk.util.ArrayMath.*;


public class Vel2dView extends TiledView implements ColorMapped {

	public enum Orientation {
		X1RIGHT_X2UP,
		X1DOWN_X2RIGHT
	}

	public enum Mark {
		NONE,
		POINT,
		PLUS,
		CROSS,
		ASTERISK,
		HOLLOW_CIRCLE,
		HOLLOW_SQUARE,
		FILLED_CIRCLE,
		FILLED_SQUARE,
	}

	public enum Line {
		NONE,
		SOLID,
		DASH,
		DOT,
		DASH_DOT,
	}


	public Layer2D 	_layer2D 			= null;
	private Layer2D _vel2D 				= null;
	private int 	_iVp 				= 1; // 1-vp 2-sh 3-sv
	private boolean _needUpdateModel 	= false;
	
	public int 		_xmouse; // x coordinate where mouse last tracked
	public int 		_ymouse; // y coordinate where mouse last tracked
	public double 	_xmouseCoord; 
	public double 	_ymouseCoord; 
	
	public boolean [] 	_kLayer = null;
	public int []	_kNode 		= null;
	int 	_mPrevX 	= 0;
	int [] 	_prevX 		= null;
	int []	_prevY 		= null;
	int [] 	_x 			= new int[2];
	int []	_y 			= new int[2];
	int [] 	_mx 		= new int[2];
	int []	_my 		= new int[2];
	int [] 	_mxv 		= new int[2];
	int []	_myv 		= new int[2];
	int [] 	_kmx 		= new int[1];
	int []	_kmy 		= new int[1];
	
	public float 	_vx 		= 0.0f; 
	public float 	_vy 		= 0.0f; 
	public int 		_moveStepPixel = 10;
	public double 	_dpStep 	= -1;
	public double 	_velStep 	= 100.0;

	public boolean 	_mousePressed		= false;
	public boolean 	_mouseReleased		= false;
	public boolean 	_mouseDragged		= false;
	public boolean 	_controlDown		= false;
	
	public boolean 	_enableLineSelection= true;
	public boolean 	_enableNodeSelection= true;
	public boolean 	_enableVelSelection	= true;
	public boolean 	_enableRayTracing	= true;
	
	public boolean 	_enableFill 		= true;
	public boolean 	_enableLine 		= true;
	public boolean 	_enableMNode 		= false;
	public boolean 	_enableVel 			= false;
	public boolean 	_enableText			= false;
	
	public boolean 	_enableMoveGeometry	= true;
	
	public boolean 	_onBuildLayer		= false;
	public boolean 	_onCopyLayer		= false;
	public boolean 	_onMoveLayer		= false;
	public boolean 	_onDeleteLayer		= false;

	ArrayList<Point2D.Float> 	_pointList = null;

	//constants for this class
	double 	_INVALID_D		= -99999.0;
	float 	_INVALID_F		= -99999.0f;
	int 	_INVALID_I		= -99999;
	int 	_pixelTol 		= 6;		//pixels
	
	VcFrame _frame 		= null;
	

	public Vel2dView(VcFrame frame, Layer2D layer2D, int iVp, ColorMap colorMap) {
		_frame 	= frame; 
		_iVp 	= iVp;
		if(_iVp==1||_iVp==11) 	_enableMoveGeometry	= true;
		else 		_enableMoveGeometry	= false;
		init(layer2D);
		this.colorMap = colorMap;
		//_colorMap = new ColorMap(_clips[0].getClipMin(), _clips[0].getClipMax(), ColorMap.JET);
	}

	public void init(Layer2D layer2D) {
		_layer2D 	= layer2D;
		_vel2D 		= layer2D.copy(); 
		
		_kLayer 	= new boolean[_layer2D.getNumOfBoundaries()];
		_kNode 		= new int[_layer2D.getNumOfBoundaries()];
		for(int i=0; i<_layer2D.getNumOfBoundaries(); i++) {
			_kLayer[i] 	= false;
			_kNode[i] 	= _INVALID_I;
		}
		
		_clips = new Clips[1];
		float [] vel = getLayerVel();
		float min = ArrayMath.min(vel);
		float max = ArrayMath.max(vel);
		float dv = 0.05f*(max-min);
		if(dv==0) dv = 0.1f;
		_clips[0] = new Clips(getLayerVel());
		_clips[0].setClips(min-dv, max+dv);
		updateVelLayer(2, 2);
		updateBestProjectors();
		repaint();
	}
	
	public void updateColorMap() { 
		double vmin = 1.0e10;
		double vmax = -vmin;
		for(int i=0; i<_layer2D.getNumOfBoundaries(); i++) {
			double v = getVel(_frame.getIVp(), i, _layer2D);
			//System.out.println(v+" "+" vp="+getRayTracer().getIVp());
			vmin = vmin<v ? vmin:v;
			vmax = vmax>v ? vmax:v;
		}
		double a = colorMap.getMinValue();
		vmin = vmin<a?vmin:a;
		a = colorMap.getMaxValue();
		vmax = vmax>a?vmax:a;
		colorMap.setValueRange(vmin, vmax);
	}
	public Color getLayerColor(int iLayer) { return colorMap.getColor(getVel(iLayer)); }
	public double getVel(int iLayer) { return getVel(_frame.getIVp(), iLayer, _layer2D); }
	public double getVel(int iVp, int iLayer, Layer2D layer2D) {
		if(iVp==1) return layer2D.getLayer(iLayer).getVp();
		else if(iVp==2) return layer2D.getLayer(iLayer).getVs();
		else return layer2D.getLayer(iLayer).getVs();
	}
	
	public VcFrame getFrame() 					{ return _frame; }
	public double getVelStep() 					{ return _velStep; }
	public double getDpStep() 					{ return _dpStep; }
	public int getSelectedLayerNo() 			{ 
		for(int i=0; i<_layer2D.getNumOfBoundaries();i++)  { if(_kLayer[i]) return i; } 
		return -1; 
	}
	
	public void setVelStep(double velStep) 		{ _velStep = velStep; }
	public void setDpStep(double dpStep) 		{ _dpStep = dpStep; }

	public Color getLineColor() 				{ return _lineColor; }
	public Line getLineStyle() 					{ return _lineStyle; }
	public float getLineWidth() 				{ return _lineWidth; }
	public Color getMarkColor() 				{ return _markColor; }
	public float getMarkSize() 					{ return _markSize; }
	public Mark getMarkStyle() 					{ return _markStyle; }

	public int getNumOfItem()					{ return 5; }
	public boolean getIsFilled()				{ return _enableFill; }
	public boolean getIsLineDrawed()			{ return _enableLine; }
	public boolean getIsMarkerDrawed()			{ return _enableMNode; }
	public boolean getIsVelDrawed()				{ return _enableVel; }
	public boolean getIsTextable()				{ return _enableText; }
	
	public void setIsFilled(boolean v)			{ _enableFill = v; }
	public void setIsLineDrawed(boolean v)		{ _enableLine = v; }
	public void setIsMarkerDrawed(boolean v) 	{ _enableMNode = v; }
	public void setIsVelDrawed(boolean v) 		{ _enableVel = v; }
	public void setIsTextable(boolean v)		{ _enableText = v; }
	
	public float[] getLayerVel() {
		float [] vel = new float[_layer2D.getLayer().size()];
		if(_iVp==1) {
			for(int i=0; i<vel.length; i++) vel[i] = (float)_layer2D.getLayer(i).getVp();
		} else {
			for(int i=0; i<vel.length; i++) vel[i] = (float)_layer2D.getLayer(i).getVs();
		}
		return vel;
	}
	public double getLayerVel(int index) {
		if(_iVp==1) return _layer2D.getLayer(index).getVp();
		else 		return _layer2D.getLayer(index).getVs();
	}
	private void setLayerVel(int index, double v) {
		if(_iVp==1) _layer2D.getLayer(index).setVp(v);
		else 		_layer2D.getLayer(index).setVs(v);
		double vpToVs = 0.0;
		if(_layer2D.getLayer(index).getVs()>0.0) vpToVs = _layer2D.getLayer(index).getVp()/_layer2D.getLayer(index).getVs();
		_layer2D.getLayer(index).setVpToVs(vpToVs);
	}
	public String getColorBarLabel() {
		if(_iVp==1) return new String("P-wave Velocity");
		else 		return new String("S-wave Velocity");
	}
	
	public void updateVelLayer(int setX, int setZ) {
		//double dv = 0.05*(_clips[0].getClipMax()-_clips[0].getClipMin());
		double dv = 0.0;
		double minV = _clips[0].getClipMin()-dv;
		double maxV = _clips[0].getClipMax()+dv;
		//System.out.println("minV="+minV+" maxV="+maxV+" minX="+_layer2D.getX0()+" maxX="+_layer2D.getX1());
		//System.out.println(_vel2D.toString());
		
		double scale = (_layer2D.getLayer(0).getCurve().getX1()-_layer2D.getLayer(0).getCurve().getX0())/(maxV-minV);

		for (int i=0; i<_layer2D.getNumOfBoundaries();i++) {
			if(!_kLayer[i]&&setZ!=2) continue;
			SingleLayer2D sl = _vel2D.getLayer(i);
			float x = (float)(_layer2D.getLayer(0).getCurve().getX0()+scale*(getLayerVel(i)-minV));
			if(setX==1) { //horizontal move
				sl.setMasterX(0, x);
				sl.setMasterX(1, x);
				if (i<_layer2D.getNumOfBoundaries()-1) {
					float z = (float)_layer2D.getLayer(i).getCurve().getPointZ(x);
					sl.setMasterZ(0, z);
					z = (float)_layer2D.getLayer(i+1).getCurve().getPointZ(x);
					sl.setMasterZ(1, z);
				}
			}
			if(setX==2) { //initial horizontal move
				sl.setMasterX(0, x);
				sl.setMasterX(1, x);
			}
			if(setZ==1) { //vertical move
				if (i>0) {
					float z = (float)_layer2D.getLayer(i).getCurve().getPointZ(x);
					sl.setMasterZ(0, z);
					float x1 = (float)(_layer2D.getLayer(0).getCurve().getX0()+scale*(getLayerVel(i-1)-minV));
					z = (float)_layer2D.getLayer(i).getCurve().getPointZ(x1);
					_vel2D.getLayer(i-1).setMasterZ(1, z);
				}
			}
			if(setZ==2) { //initial vertical move
				float z = (float)_layer2D.getLayer(i).getCurve().getPointZ(x);
				sl.setMasterZ(0, z);

				SingleLayer2D bot = _layer2D.getLayer(i);
				if (i<_layer2D.getNumOfBoundaries()-1) {
					bot = _layer2D.getLayer(i+1);
				}
				z = (float)bot.getCurve().getPointZ(x);
				sl.setMasterZ(1, z);
			}
		}
	}
	
	public void slowRefresh(int millionSec) {		//Pause for 4 seconds
        try {
			Thread.sleep(millionSec);
		} catch (InterruptedException e) { e.printStackTrace(); }
		repaint();
	}
	
	
	private void setKLayer(boolean on) {
		for(int i=0; i<_layer2D.getNumOfBoundaries(); i++) {_kLayer[i] 	= on;}
	}
	private void setKNode(int on) {
		for(int i=0; i<_layer2D.getNumOfBoundaries(); i++) {_kNode[i] 	= on;}
	}

	public void setOrientation(Orientation orientation) {
		if (_orientation!=orientation) {
			_orientation = orientation;
			updateBestProjectors();
			repaint();
		}
	}

	public Orientation getOrientation() {
		return _orientation;
	}

	public void setStyle(String style) {
		// Color.
		if (style.contains("r")) {
			setLineColor(Color.RED);
			setMarkColor(Color.RED);
		} else if (style.contains("g")) {
			setLineColor(Color.GREEN);
			setMarkColor(Color.GREEN);
		} else if (style.contains("b")) {
			setLineColor(Color.BLUE);
			setMarkColor(Color.BLUE);
		} else if (style.contains("c")) {
			setLineColor(Color.CYAN);
			setMarkColor(Color.CYAN);
		} else if (style.contains("m")) {
			setLineColor(Color.MAGENTA);
			setMarkColor(Color.MAGENTA);
		} else if (style.contains("y")) {
			setLineColor(Color.YELLOW);
			setMarkColor(Color.YELLOW);
		} else if (style.contains("k")) {
			setLineColor(Color.BLACK);
			setMarkColor(Color.BLACK);
		} else if (style.contains("w")) {
			setLineColor(Color.WHITE);
			setMarkColor(Color.WHITE);
		} else {
			setLineColor(null);
			setMarkColor(null);
		}

		// Line style.
		if (style.contains("--.")) {
			setLineStyle(Line.DASH_DOT);
		} else if (style.contains("--")) {
			setLineStyle(Line.DASH);
		} else if (style.contains("-.")) {
			setLineStyle(Line.DOT);
		} else if (style.contains("-")) {
			setLineStyle(Line.SOLID);
		} else {
			setLineStyle(Line.NONE);
		}

		// Mark style.
		if (style.contains("+")) {
			setMarkStyle(Mark.PLUS);
		} else if (style.contains("x")) {
			setMarkStyle(Mark.CROSS);
		} else if (style.contains("o")) {
			setMarkStyle(Mark.HOLLOW_CIRCLE);
		} else if (style.contains("O")) {
			setMarkStyle(Mark.FILLED_CIRCLE);
		} else if (style.contains("s")) {
			setMarkStyle(Mark.HOLLOW_SQUARE);
		} else if (style.contains("S")) {
			setMarkStyle(Mark.FILLED_SQUARE);
		} else if (style.contains(".")) {
			int i = style.indexOf(".");
			if (i==0 || style.charAt(i-1)!='-')
				setMarkStyle(Mark.POINT);
		} else {
			setMarkStyle(Mark.NONE);
		}
	}

	public void setLineStyle(Line style) {
		_lineStyle = style;
		repaint();
	}

	public void setLineWidth(float width) {
		if (_lineWidth!=width) {
			_lineWidth = width;
			updateBestProjectors();
			repaint();
		}
	}
	public void doubleLineWidth() {
		_lineWidth += _lineWidth;
		updateBestProjectors();
		repaint();
	}

	public void setLineColor(Color color) {
		if (!equalColors(_lineColor,color)) {
			_lineColor = color;
			repaint();
		}
	}

	public void setMarkStyle(Mark style) {
		if (_markStyle!=style) {
			_markStyle = style;
			updateBestProjectors();
			repaint();
		}
	}

	public void setMarkSize(float size) {
		if (_markSize!=size) {
			_markSize = size;
			updateBestProjectors();
			repaint();
		}
	}

	public void doubleMarkSize() {
		_markSize += _markSize;
		updateBestProjectors();
		repaint();
	}

	public void setMarkColor(Color color) {
		if (!equalColors(_markColor,color)) {
			_markColor = color;
			repaint();
		}
	}

	public void setTextFormat(String format) {
		_textFormat = format;
		repaint();
	}

	public ColorMap getColorMap() {
		return colorMap;
	}

	public Color getColor(double v) {
		return getColorMap().getColor(v);
	}

	public double getVel(double x) {
		double scalor = (getClipMax()-getClipMin())/(_layer2D.getLayer(0).getX1()-_layer2D.getLayer(0).getX0());
		return getClipMin()+(x-_layer2D.getLayer(0).getX0())*scalor;
	}
	
	public void setClips(float clipMin, float clipMax) {
		setClips(0,clipMin,clipMax);
	}

	public float getClipMin() {
		return _clips[0].getClipMin();
	}

	public float getClipMax() {
		return _clips[0].getClipMax();
	}

	public void setClips(int ic, float clipMin, float clipMax) {
		_clips[ic].setClips(clipMin,clipMax);
		repaint();
	}

	private void updateClips1() {
		if (_clipMin==null)
			_clipMin = new float[1];
		if (_clipMax==null)
			_clipMax = new float[1];
		int ic = 0;
		float clipMin = _clips[ic].getClipMin();
		float clipMax = _clips[ic].getClipMax();
		if (_clipMin[ic]!=clipMin || _clipMax[ic]!=clipMax) {
			_clipMin[ic] = clipMin;
			_clipMax[ic] = clipMax;
			//_colorMap.setValueRange(clipMin,clipMax);
		}
	}
	

	public void addColorMapListener(ColorMapListener cml) {
		colorMap.addListener(cml);
	}

	public void removeColorMapListener(ColorMapListener cml) {
		colorMap.removeListener(cml);
	}

	public int getTileHeight() {
		return getTile().getHeight();
	}

	public void addPoint() {
		int k = -1;
		for(int i=0; i<_kLayer.length; i++) {
			if(_kLayer[i]) k= i;
		}
		//System.out.println("k="+k);
		if(k<0) return;
		SingleLayer2D sl = _layer2D.getLayer(k);
		//if(sl.getLayerEditable()==0) return;
		
		sl.addMasterPointAt(_vx, _vy);
		sl.setCurve();
		slowRefresh(50);
	}
	public void deletePoint() {
		int k = -1;
		for(int i=0; i<_kLayer.length; i++) {
			if(_kLayer[i]) k= i;
		}
		//System.out.println("k="+k);
		if(k<0) return;
		SingleLayer2D sl = _layer2D.getLayer(k);
		//if(sl.getLayerEditable()==0) return;
		
		if(_kNode[k]==_INVALID_I) return;
		sl.removeMasterPointAt(_kNode[k]);
		sl.setCurve();
		//_layer2D.setLayer(k, sl);
		//System.out.println("here1" + _layer2D.getLayer(k).toString(2));
		//slowRefresh(100);
	}
	public void addLayer() {
		_layer2D.addLayer(2, _vx, _vy);
		init(_layer2D);
		rayTracing();
		//allocate();
		//initK();
		//slowRefresh(100);
	}
	
	public void deleteLayer() {
		int k = -1;
		for(int i=0; i<_kLayer.length; i++) {
			if(_kLayer[i]) k= i;
		}
		//System.out.println("k="+k);
		if(k<0) return;
		_layer2D.deleteLayer(k);
		init(_layer2D);
		rayTracing();
		//allocate1();
		//initK();
		//slowRefresh(100);
	}
	public void smoothAllLayer(int iMethod) {
		for(int i=0; i<_kLayer.length; i++) {
			_layer2D.getLayer(i).setCurve();
		}
	}
	public void smoothSelectedLayer(int iMethod) {
		int k = -1;
		for(int i=0; i<_kLayer.length; i++) {
			if(_kLayer[i]) k= i;
		}
		//System.out.println("k="+k);
		if(k<0) return;
		SingleLayer2D sl = _layer2D.getLayer(k);
		sl.setCurve();
	}
	
	public void updateModel() {
		repaint();
	}
	public BufferedImage paintToImage() {
		Tile tile = getTile();
		int w = tile.getWidth();
		int h = tile.getHeight();
		int x = tile.getX();
		int y = tile.getY();
		BufferedImage image = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
		paintToImage(image);
		return image;
	}
	public void paintToImage(BufferedImage image) {
		Graphics2D g2d = (Graphics2D)image.getGraphics();
		paint(g2d);
	    //g2d.dispose();
	}
	public void paint(Graphics2D g2d) {
		if(!isVisible()) return;
		updateColorMap();

		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		Projector hp = getHorizontalProjector();
		Projector vp = getVerticalProjector();
		Transcaler ts = getTranscaler();

		// Font size and line width from graphics context.
		float fontSize = g2d.getFont().getSize2D();
		float lineWidth = 1.0f;
		Stroke stroke = g2d.getStroke();
		if (stroke instanceof BasicStroke) {
			BasicStroke bs = (BasicStroke)stroke;
			lineWidth = bs.getLineWidth();
		}

		// Graphics context for lines.
		Graphics2D gline 			= null;
		Graphics2D vline 			= null;
		Graphics2D glineSelected 	= null;
		if (_lineStyle!=Line.NONE) {
			//gline = (Graphics2D)g2d.create();
			gline = (Graphics2D)g2d;
			float width = lineWidth;
			//_lineWidth = 0.5f;
			if (_lineWidth>0.0f)
				width *= _lineWidth;
			float[] dash = null;
			if (_lineStyle!=Line.SOLID) {
				float dotLength = 0.5f*width;
				float dashLength = 2.0f*width;
				float gapLength = 2.0f*dotLength+dashLength;
				if (_lineStyle==Line.DASH) {
					dash = new float[]{dashLength,gapLength};
				} else if (_lineStyle==Line.DOT) {
					dash = new float[]{dotLength,gapLength};
				} else if (_lineStyle==Line.DASH_DOT) {
					dash = new float[]{dashLength,gapLength,dotLength,gapLength};
				}
			}
			BasicStroke bs;
			BasicStroke bsSelected;
			if (dash!=null) {
				int cap = BasicStroke.CAP_ROUND;
				int join = BasicStroke.JOIN_ROUND;
				float miter = 10.0f;
				float phase = 0.0f;
				bs = new BasicStroke(width,cap,join,miter,dash,phase);
				bsSelected = new BasicStroke(2*width,cap,join,miter,dash,phase);
			} else {
				bs = new BasicStroke(width);
				bsSelected = new BasicStroke(3*width);
			}
			gline.setStroke(bs);
			if (_lineColor!=null)
				gline.setColor(_lineColor);
			
			if(_enableLineSelection) {
				glineSelected = (Graphics2D)gline.create();
				glineSelected.setColor(Color.RED);
				glineSelected.setStroke(bsSelected);
			}
			if(_enableVel) {
				//vline = (Graphics2D)gline.create();
				vline = (Graphics2D)gline;
				vline.setColor(Color.BLACK);
				vline.setStroke(new BasicStroke(3*width));
			}
		}

		Graphics2D gmark = null;
		Graphics2D gmarkSelected = null;
		int markSize = round(fontSize/2.0f);
		if (_markStyle!=Mark.NONE) {
			gmark = (Graphics2D)g2d.create();
			if (_markSize>=0.0f)
				markSize = round(_markSize*lineWidth);
			if (_markColor!=null)
				gmark.setColor(_markColor);
			float width = lineWidth;
			if (_lineWidth!=0.0f)
				width *= _lineWidth;
			BasicStroke bs = new BasicStroke(width);
			gmark.setStroke(bs);
			
			if(_enableMNode) {
				gmarkSelected = (Graphics2D)gmark.create();
				gmarkSelected.setColor(Color.RED);
				gmarkSelected.setStroke(new BasicStroke(3*width));
			}
		}
		//gmark = null;
		// Graphics context for text labels.
		Graphics2D gtext = null;
		//if (_x3.size()>0)
		//gtext = (Graphics2D)g2d.create();

		Rectangle clipRect = new Rectangle();
		g2d.getClipBounds(clipRect);
		
		// For all plot segments, ...
		
//		if(_layer2D.getNumOfBoundaries()>_kLayer.length) {
//			_kLayer 	= new boolean[_layer2D.getNumOfBoundaries()];
//			_kNode 		= new int[_layer2D.getNumOfBoundaries()];
//		}
		if(_enableFill) {
			_mPrevX = 0;
			for (int is=0; is<_layer2D.getNumOfBoundaries(); ++is) {
				SingleLayer2D sl = _layer2D.getLayer(is);
				float[] x1 = sl.getCurve().getZ();
				float[] x2 = sl.getCurve().getX();
				int n = x1.length;	
				if(n>_x.length) { _x = new int[n]; _y = new int[n]; }
				int [] x = _x;
				int [] y = _y;

				computeXY(hp,vp,ts,n,x1,x2,x,y);

				if(_mPrevX>0) {
					GeneralPath gp = new GeneralPath();
					gp.moveTo(x[0], y[0]);
					for(int i=1; i<n; i++)  			gp.lineTo(x[i], y[i]); 
					for(int i=_mPrevX-1; i>=0; i--)  	gp.lineTo(_prevX[i], _prevY[i]); 
					//gp.lineTo(clipRect.x+clipRect.width, clipRect.y+clipRect.height);
					//gp.lineTo(clipRect.x, clipRect.y+clipRect.height);
					gp.lineTo(x[0], y[0]);

					// Retains the previous state
					Paint oldPaint = g2d.getPaint();

					Color color = getColorMap().getColor(getLayerVel(is-1));
					g2d.setPaint(color);

					//g2.setPaint(new Color((int)(_cp[0][iColor])+128, (int)(_cp[1][iColor])+128, (int)(_cp[2][iColor])+128));
					g2d.fill(gp);
					// Restores the previous state
					g2d.setPaint(oldPaint);
				}
				if(_mPrevX<n) { _prevX = new int[n]; _prevY = new int[n]; }
				_mPrevX= n;
				for(int i=0; i<n; i++) { _prevX[i] = x[i]; _prevY[i] = y[i]; }
			}
		}			
			
		double ltop = 0.0;
		double lbot = 1.0;
		double rtop = 0.0;
		double rbot = 1.0;
		for (int is=0; is<_layer2D.getNumOfBoundaries(); ++is) {
			int k = is;
			SingleLayer2D sl = _layer2D.getLayer(is);
			if(is-1>=0) 							ltop = _layer2D.getLayer(is-1).getCurve().getFirstZ(); 	else ltop = sl.getCurve().getFirstZ();
			if(is+1<_layer2D.getNumOfBoundaries()) 	lbot = _layer2D.getLayer(is+1).getCurve().getFirstZ(); 	else lbot = sl.getCurve().getFirstZ();
			if(is-1>=0) 							rtop = _layer2D.getLayer(is-1).getCurve().getLastZ(); 	else ltop = sl.getCurve().getLastZ();
			if(is+1<_layer2D.getNumOfBoundaries()) 	rbot = _layer2D.getLayer(is+1).getCurve().getLastZ(); 	else rbot = sl.getCurve().getLastZ();

			// Compute (x,y) coordinates.
			float[] x1 = sl.getCurve().getZ();
			float[] x2 = sl.getCurve().getX();
			int n = x1.length;	
			if(n>_x.length) { _x = new int[n]; _y = new int[n]; }
			int [] x = _x;
			int [] y = _y;
			
			computeXY(hp,vp,ts,n,x1,x2,x,y);

			float[] mx1 = sl.getMasterZ();
			float[] mx2 = sl.getMasterX();
			int m = mx1.length;
			if(m>_mx.length) { _mx = new int[m]; _my = new int[m]; }
			int [] mx = _mx;
			int [] my = _my;
			computeXY(hp,vp,ts,m,mx1,mx2,mx,my);
			
			mx1 = _vel2D.getLayer(is).getMasterZ();
			mx2 = _vel2D.getLayer(is).getMasterX();
			int mv = mx1.length;
			if(mv>_mxv.length) { _mxv = new int[mv]; _myv = new int[mv]; }
			int [] mxv = _mxv;
			int [] myv = _myv;
			computeXY(hp,vp,ts,mv,mx1,mx2,mxv,myv);

			if(_mousePressed) {
				//if(_mousePressed&&sl.getLayerEditable()==1) {
				double ux = ts.x(_xmouse);
				double uy = ts.y(_ymouse);
				_vx = (float)hp.v(ux);
				_vy = (float)vp.v(uy);
				double uy1 = ts.y(_ymouse+_moveStepPixel);
				if(_dpStep<0) _dpStep = vp.v(uy1) - _vy;
				
				//calculate selection
				_kLayer[k] 	= false;
				_kNode[k] 	= _INVALID_I;	//initK();
				for(int i=0; i<m; i++) {
					if(_xmouse>=mx[i]-2*_pixelTol&&_xmouse<=mx[i]+2*_pixelTol&&
							_ymouse>=my[i]-2*_pixelTol&&_ymouse<=my[i]+2*_pixelTol) {
						setKLayer(false);
						_kLayer[k] 	= true;
						_kLayer[_layer2D.getNumOfBoundaries()-1] 	= false;
						if(_kLayer[k]&&_xmouse>=mx[i]-_pixelTol&&_xmouse<=mx[i]+_pixelTol&&
								_ymouse>=my[i]-_pixelTol&&_ymouse<=my[i]+_pixelTol&&_enableNodeSelection) {
							setKNode(_INVALID_I);
							_kNode[k] 	= i;
						} 
						break;
					}
				}
			}
			
			//pickNode of master points and pickLayer
			//if(_mousePressed&&sl.getLayerEditable()==1) {
			if(_mouseDragged) {
				double ux = ts.x(_xmouse);
				double uy = ts.y(_ymouse);
				_vx = (float)hp.v(ux);
				_vy = (float)vp.v(uy);
				double uy1 = ts.y(_ymouse+_moveStepPixel);
				if(_dpStep<0.0) _dpStep = vp.v(uy1) - _vy;

				if(_kNode[k]!=_INVALID_I&&!_controlDown&&_enableMoveGeometry){
					int leftIndex 	= _kNode[k]-1;
					int rightIndex 	= _kNode[k]+1;
					int leftEdge 	= clipRect.x;
					int rightEdge 	= clipRect.x+clipRect.width;
					if(leftIndex>=0 && leftIndex<m) 	leftEdge 	= mx[leftIndex];
					if(rightIndex>=0 && rightIndex<m) 	rightEdge 	= mx[rightIndex];
					
					rightEdge 	= clipRect.x-clipRect.width;
					if(_xmouse>=leftEdge && _xmouse<=rightEdge) {
						if(_kNode[k]==0) { 
							if(_vy>ltop && _vy<lbot) {
								my[_kNode[k]] 	= _ymouse;
								sl.setMasterZ(_kNode[k], _vy);
								updateVelLayer(0, 1);
							}
						} else if(_kNode[k]==m-1) {
							if(_vy>rtop && _vy<rbot) {
								my[_kNode[k]] 	= _ymouse;
								sl.setMasterZ(_kNode[k], _vy);
								updateVelLayer(0, 1);
							}
						} else {
							mx[_kNode[k]] 	= _xmouse;
							my[_kNode[k]] 	= _ymouse;
							sl.setMasterX(_kNode[k], _vx);
							sl.setMasterZ(_kNode[k], _vy);
							updateVelLayer(1, 1);
						}

						sl.setCurve();
					}
				}
				if(_kNode[k]==_INVALID_I&&_kLayer[k]){
					sl.setCurve();
					moveTo(0, 0, _vx, _vy, false);
				}
			} 
				
			if (vline!=null&&_enableVel) {
				if(_kLayer[k]&&_enableLineSelection) {
					glineSelected.drawPolyline(mxv,myv,mv);
				} else {
					vline.drawPolyline(mxv,myv,mv);
				}
			}
			// Draw lines between consecutive points.
			if (gline!=null&&_enableLine) {
				if(_kLayer[k]&&_enableLineSelection) {
					glineSelected.drawPolyline(x,y,n);
				} else {
					gline.drawPolyline(x,y,n);
					Font f = new Font("Dialog", Font.PLAIN, 16);
					gline.setFont(f);
//					String text = "i="+is+" vp="+(int)_layer2D.getLayer(is).getVp() +
//							" vs="+(int)_layer2D.getLayer(is).getVs()+
//							" delta="+(double)((int)(_layer2D.getLayer(is).getDelta()*100))/100+
//							" epsilon="+(double)((int)(_layer2D.getLayer(is).getEpsilon()*100))/100+
//							" gamma="+(double)((int)(_layer2D.getLayer(is).getGamma()*100))/100;
					if(is>0&&_enableText) {
						double cx =  _layer2D.getLayer(is).getCenterX();
						double th = _layer2D.getLayerTop(is, cx)-_layer2D.getLayerTop(is-1, cx);
						String text = "i="+is+" th="+(int)th+" vp="+(int)_layer2D.getLayer(is-1).getVp() +
								" vs="+(int)_layer2D.getLayer(is-1).getVs()+
								" delta="+(double)((int)(_layer2D.getLayer(is-1).getDelta()*100))/100+
								" epsilon="+(double)((int)(_layer2D.getLayer(is-1).getEpsilon()*100))/100+
								" gamma="+(double)((int)(_layer2D.getLayer(is-1).getGamma()*100))/100;
						gline.drawString(text+"",x[0]+10,y[0]-10);
					}
				}
			}

//			if(gline!=null&&_onBuildLayer&&_pointList!=null) {
//				int ax = (int)_pointList.get(0).x;
//				int ay = (int)_pointList.get(0).y;
//				int bx = ax;
//				int by = ay;
//				for(int j=1; j<_pointList.size(); j++) {
//					bx = (int)_pointList.get(j).x;
//					by = (int)_pointList.get(j).y;
//					gline.drawLine(ax, ay, bx, by);
//					ax = bx;
//					ay = by;
//				}
//				//gline.drawLine(bx, by, _xmouse, _ymouse);
//			}

			// Draw marks at points.		
			//if (gmark!=null && sl.getLayerEditable()==1 &&_enableMNode) {
			if (gmark!=null &&_enableMNode) {
				int mm = m;
				if(_kNode[k]!=_INVALID_I&&_enableNodeSelection) {
					int [] kmx = new int[]{mx[_kNode[k]]};
					int [] kmy = new int[]{my[_kNode[k]]};
					if (_markStyle==Mark.POINT) {
						paintPoint(gmark,1,kmx,kmy);
					} else if (_markStyle==Mark.PLUS) {
						paintPlus(gmarkSelected,markSize,1,kmx,kmy);
					} else if (_markStyle==Mark.CROSS) {
						paintCross(gmarkSelected,markSize,1,kmx,kmy);
					} else if (_markStyle==Mark.FILLED_CIRCLE) {
						paintFilledCircle(gmarkSelected,markSize,1,kmx,kmy);
					} else if (_markStyle==Mark.HOLLOW_CIRCLE) {
						paintHollowCircle(gmarkSelected,markSize,1,kmx,kmy);
					} else if (_markStyle==Mark.FILLED_SQUARE) {
						paintFilledSquare(gmarkSelected,markSize,1,kmx,kmy);
					} else if (_markStyle==Mark.HOLLOW_SQUARE) {
						paintHollowSquare(gmarkSelected,markSize,1,kmx,kmy);
					}
					for(int i=0, j=0; i<m; i++) {
						if(_kNode[k]!=i) {mx[j] = mx[i]; my[j] = my[i]; j++; }
					}
					mm--;
				} 
				if (_markStyle==Mark.POINT) {
					paintPoint(gmark,mm,mx,my);
				} else if (_markStyle==Mark.PLUS) {
					paintPlus(gmark,markSize,mm,mx,my);
				} else if (_markStyle==Mark.CROSS) {
					paintCross(gmark,markSize,mm,mx,my);
				} else if (_markStyle==Mark.FILLED_CIRCLE) {
					paintFilledCircle(gmark,markSize,mm,mx,my);
				} else if (_markStyle==Mark.HOLLOW_CIRCLE) {
					paintHollowCircle(gmark,markSize,mm,mx,my);
				} else if (_markStyle==Mark.FILLED_SQUARE) {
					paintFilledSquare(gmark,markSize,mm,mx,my);
				} else if (_markStyle==Mark.HOLLOW_SQUARE) {
					paintHollowSquare(gmark,markSize,mm,mx,my);
				}
			}
			
			if(_mouseDragged) {

			}

			// Draw text labels.
			if (gtext!=null) {
				//float[] z = _x3.get(is);
				//paintLabel(gtext,markSize,n,x,y,z);
			}
		}
	}
	public void moveTo(float vx, float vy) 	{ moveTo(0, 0, vx, vy, true); }
	public void moveCurve(int zDir) 		{ moveTo(1, zDir, 0, 0, true); }
	public void moveNode(int zDir) 			{ moveTo(2, zDir, 0, 0, true); }
	public void moveTo(int id, int zDir, float vx, float vy, boolean rayTracing) {
		if(!_enableMoveGeometry) return;
		int k = -1;
		for(int i=0; i<_kLayer.length; i++) { if(_kLayer[i]) k = i; }
		if(k<0) return;
		
		double ltop = _layer2D.getLayer(k).getCurve().getFirstZ();
		double lbot = ltop;
		double rtop = _layer2D.getLayer(k).getCurve().getLastZ();
		double rbot = rtop;
		if(k==0) {
			lbot = _layer2D.getLayer(k+1).getCurve().getFirstZ();
			rbot = _layer2D.getLayer(k+1).getCurve().getLastZ();
		} else if (k==_layer2D.getNumOfBoundaries()-1) {
			ltop = _layer2D.getLayer(k-1).getCurve().getFirstZ();
			rtop = _layer2D.getLayer(k-1).getCurve().getLastZ();
		} else {
			ltop = _layer2D.getLayer(k-1).getCurve().getFirstZ();
			rtop = _layer2D.getLayer(k-1).getCurve().getLastZ();
			lbot = _layer2D.getLayer(k+1).getCurve().getFirstZ();
			rbot = _layer2D.getLayer(k+1).getCurve().getLastZ();
		}

		SingleLayer2D sl = _layer2D.getLayer(k);
		float top 		= (float)sl.getCurve().getPointZ(vx);
		double shiftZ 	= vy-top;
		double lz 		= sl.getMasterZ(0)+shiftZ;
		double rz 		= sl.getMasterZ(1)+shiftZ;

		if(id==0) { // move whole curve by mouse
			if(lz>ltop&&lz<lbot && rz>rtop&&rz<rbot) { 
				if(_kNode[k]==_INVALID_I) { sl.moveTo(vy); updateVelLayer(0, 1); if(rayTracing) rayTracing();}
			}
		} else if(id==1) { // move whole curve by key
			shiftZ 	= zDir*_dpStep;
			lz 		= sl.getMasterZ(0)+shiftZ;
			rz 		= sl.getMasterZ(1)+shiftZ;
			if(lz>ltop&&lz<lbot && rz>rtop&&rz<rbot) { 
				sl.shift(shiftZ); updateVelLayer(0, 1); if(rayTracing) rayTracing();
			}
		} else if(id==2) { // move individual node by key
			shiftZ 	= zDir*_dpStep;
			lz 		= sl.getMasterZ(0)+shiftZ;
			rz 		= sl.getMasterZ(1)+shiftZ;
			if(_kNode[k]==0) {
				if(lz>ltop&&lz<lbot) { sl.shift(0, 0.0, shiftZ); updateVelLayer(0, 1); if(rayTracing) rayTracing();}
			} else if(_kNode[k]==1) {
				if(rz>rtop&&rz<rbot) { sl.shift(1, 0.0, shiftZ); updateVelLayer(0, 1); if(rayTracing) rayTracing();}
			} else { }
			
		} else { }
	}
	public void moveVel(int zDir) {
		int k = -1;
		for(int i=0; i<_kLayer.length; i++) { if(_kLayer[i]) k = i; }
		if(k<0) return;

		double minV 	= _clips[0].getClipMin();
		double maxV 	= _clips[0].getClipMax();
		double shift 	= zDir*_velStep;
		double vel 		= getLayerVel(k)+shift;
		//System.out.println("vel"+vel+" minV="+minV+" maxV="+maxV);
		if(vel>=minV&&vel<=maxV) { setLayerVel(k, vel); updateVelLayer(1, 0);}
		if(zDir!=0) rayTracing();
	}
	public void rayTracing() {
//		if(!_enableRayTracing) return;
//		if(_frame.getRayTracer()==null) _frame.newRayTracer();
//		FlatLayer1D flayer1D = _layer2D.toFlatLayer1D(true);
//		//System.out.println("k=4"+" vp="+Arrays.toString(flayer1D.getVp()));
//		_frame.getRayTracer().setLayer(flayer1D);
//		_frame.getRayTracer().setConfiguration();
//		_frame.getRayTracer().startRayTracing(2, null);
	}
	public void refresh() { repaint(); }

	///////////////////////////////////////////////////////////////////////////
	// private
	private Orientation _orientation = Orientation.X1RIGHT_X2UP;
	private Line _lineStyle = Line.SOLID;
	private float _lineWidth = 1.0f;
	private Color _lineColor = Color.BLACK;
	private Mark _markStyle = Mark.HOLLOW_CIRCLE;
	private float _markSize = 8.0f;
	private Color _markColor = Color.BLACK;
	private String _textFormat = "%1.4G";

	// Interpolation method.
	private Interpolation _interpolation = Interpolation.LINEAR;

	// Clips, one for each component.
	Clips[] _clips 			= null;
	float[] _clipMin		= null;
	float[] _clipMax 		= null;

	// Color map with default gray color model.
	//private ColorMap _colorMap = null;
	private ColorMap colorMap = null;

	/**
	 * Called when we might new realignment.
	 */
	private void updateBestProjectors() {

		// Min and max (x1,x2) values.
		double x1min = _layer2D.getZ0();
		double x2min = _layer2D.getX0();
		double x1max = _layer2D.getZ1();
		double x2max = _layer2D.getX1();
		
		//System.out.println("x1min="+x1min+" x1max="+x1max+" x2min="+x2min+" x2max="+x2max);

		// Assume mark sizes and line widths less than 2% of plot dimensions.
		// The goal is to avoid clipping big marks and wide lines. The problem
		// is that mark sizes and line widths are specified in screen pixels
		// (or points), but margins u0 and u1 are specified in normalized 
		// coordinates, fractions of our tile's width and height. Here, we do 
		// not know those dimensions.
		double u0 = 0.0;
		double u1 = 1.0;
		//		if (_markStyle!=Mark.NONE || _lineWidth>1.0f) {
		//			u0 = 0.01;
		//			u1 = 0.99;
		//		}

		// Best projectors.
		Projector bhp = null;
		Projector bvp = null;
		if (_orientation==Orientation.X1RIGHT_X2UP) {
			bhp = (x1min<x1max)?new Projector(x1min,x1max,u0,u1):null;
			bvp = (x2min<x2max)?new Projector(x2max,x2min,u0,u1):null;
		} else if (_orientation==Orientation.X1DOWN_X2RIGHT) {
			bhp = (x2min<x2max)?new Projector(x2min,x2max,u0,u1):null;
			bvp = (x1min<x1max)?new Projector(x1min,x1max,u0,u1):null;
		}
		setBestProjectors(bhp,bvp);
	}

	private boolean equalColors(Color ca, Color cb) {
		return (ca==null)?cb==null:ca.equals(cb);
	}

	private void computeXY(
			Projector hp, Projector vp, Transcaler ts,
			int n, float[] x1, float[] x2, int[] x, int[] y) 
	{
		ts = ts.combineWith(hp,vp);
		float[] xv = null;
		float[] yv = null;
		if (_orientation==Orientation.X1RIGHT_X2UP) {
			xv = x1;
			yv = x2;
		} else if (_orientation==Orientation.X1DOWN_X2RIGHT) {
			xv = x2;
			yv = x1;
		}
		for (int i=0; i<n; ++i) {
			x[i] = ts.x(xv[i]);
			y[i] = ts.y(yv[i]);
		}
	}

	private void paintLines(Graphics2D g2d, int n, int[] x, int[] y) {
		int x1 = x[0];
		int y1 = y[0];
		for (int i=1; i<n; ++i) {
			int x2 = x[i];
			int y2 = y[i];
			g2d.drawLine(x1,y1,x2,y2);
			x1 = x2;
			y1 = y2;
		}
	}
	private void paintPoint(Graphics2D g2d, int n, int[] x, int[] y) {
		for (int i=0; i<n; ++i) {
			int xi = x[i];
			int yi = y[i];
			g2d.drawLine(xi,yi,xi,yi);
		}
	}

	private void paintPlus(Graphics2D g2d, int s, int n, int[] x, int[] y) {
		int wh = 2*(s/2);
		int xy = wh/2;
		for (int i=0; i<n; ++i) {
			int xi = x[i];
			int yi = y[i];
			g2d.drawLine(xi-xy,yi,xi+xy,yi);
			g2d.drawLine(xi,yi-xy,xi,yi+xy);
		}
	}

	private void paintCross(Graphics2D g2d, int s, int n, int[] x, int[] y) {
		int wh = 2*(s/2);
		int xy = wh/2;
		for (int i=0; i<n; ++i) {
			int xi = x[i];
			int yi = y[i];
			g2d.drawLine(xi-xy,yi-xy,xi+xy,yi+xy);
			g2d.drawLine(xi+xy,yi-xy,xi-xy,yi+xy);
		}
	}

	private void paintFilledCircle(Graphics2D g2d, int s, int n, int[] x, int[] y) {
		int wh = 1+2*(s/2);
		int xy = wh/2;
		for (int i=0; i<n; ++i)
			g2d.fillOval(x[i]-xy,y[i]-xy,wh,wh);
	}

	private void paintHollowCircle(Graphics2D g2d, int s, int n, int[] x, int[] y) 	{
		int wh = 1+2*(s/2);
		int xy = wh/2;
		for (int i=0; i<n; ++i)
			g2d.drawOval(x[i]-xy,y[i]-xy,wh-1,wh-1);
	}

	private void paintFilledSquare(Graphics2D g2d, int s, int n, int[] x, int[] y) {
		int wh = 1+2*(s/2);
		int xy = wh/2;
		for (int i=0; i<n; ++i)
			g2d.fillRect(x[i]-xy,y[i]-xy,wh,wh);
	}

	private void paintHollowSquare(Graphics2D g2d, int s, int n, int[] x, int[] y) {
		int wh = 1+2*(s/2);
		int xy = wh/2;
		for (int i=0; i<n; ++i)
			g2d.drawRect(x[i]-xy,y[i]-xy,wh-1,wh-1);
	}

	private void paintLabel(Graphics2D g2d, int s, int n, int[] x, int[] y, float[] z) {
		s /= 2;
		for (int i=0; i<n; ++i) {
			int xi = x[i];
			int yi = y[i];
			g2d.drawString(String.format(_textFormat,z[i]),xi+s,yi-s);
		}
	}

	public void mousePressed(MouseEvent e) {		
		setMouseStatus(e, true, false, false, e.isControlDown());
		repaint();
	}
	public void mouseDragged(MouseEvent e)  {	
		setMouseStatus(e, false, false, true, e.isControlDown());
		repaint();
	}
	public void mouseReleased(MouseEvent e) {
		if(_mouseDragged) {
			//System.out.println("updateVelocity 1");
			int k = -1;
			for(int i=0; i<_kLayer.length; i++) { if(_kLayer[i]) k = i; }
			if(k>=0)  rayTracing();
		}
		setMouseStatus(e, false, true, false, e.isControlDown());
		//repaint();
	}

	public void mouseMoved(MouseEvent e) { 
		_xmouse 		= e.getX();
		_ymouse 		= e.getY();
	}

	public void mouseClicked(MouseEvent e) { 
		//		  boolean isAltDown()     // true if Alt key middle mouse button
		//		  boolean isControlDown() // true if Control key is pressed
		//		  boolean isShiftDown()   // true if Shift key is pressed
		//		  boolean isAltGraphDown()// true if Alt Graphics key (found on some keyboards) is pressed
		//		  boolean isMetaDown()    // true if Meta key or right mouse button
	}
	
	public void setMouseStatus(MouseEvent e, boolean mousePressed, boolean mouseReleased, 
			boolean mouseDragged, boolean controlDown) {
		_mousePressed	= mousePressed;
		_mouseReleased	= mouseReleased;
		_mouseDragged	= mouseDragged;
		_xmouse 		= e.getX();
		_ymouse 		= e.getY();
		_controlDown 	= controlDown;

		repaint();
	}
	
	public void setBuildLayer(boolean onBuildLayer) {
		_onBuildLayer = onBuildLayer;
	}
	public void clearLists(int id) {
		_pointList = null;
	}
	public Point2D.Float getPointAtMouse() {
		if (getTile()==null) {
			return null;
		} else {
			Projector hp = getHorizontalProjector();
			Projector vp = getVerticalProjector();
			Transcaler ts = getTranscaler();
			double ux = ts.x(_xmouse);
			double uy = ts.y(_ymouse);
			return new Point2D.Float((float)(hp.v(ux)), (float)(vp.v(uy)));
		}
	}

	public void addKeyListener(KeyListener _kl) {	}

}

