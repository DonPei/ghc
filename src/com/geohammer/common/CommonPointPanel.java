package com.geohammer.common;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.JMenuItem;

import com.geohammer.component.PointsViewWithLegend;

import edu.mines.jtk.mosaic.AxisScale;
import edu.mines.jtk.mosaic.GridView;
import edu.mines.jtk.mosaic.PointsView;
import edu.mines.jtk.mosaic.Projector;
import edu.mines.jtk.mosaic.Tile;
import edu.mines.jtk.util.ArrayMath;


public class CommonPointPanel extends CommonPanel {
	
	private int _iType = 0; //
	
	String _hLabel = null;
	String _vLabel = null;
	String _catalog = null;
	
	private ArrayList<float []> _hList = null;
	private ArrayList<float []> _vList = null;

	public float [][] 			_minMax = new float[][]{{1.0e20f, 1.0e20f}, {-1.0e20f, -1.0e20f}}; 
	
	public boolean 	_legendOn 		= false;
	public String 	_legendLabel 	= null;
	//public int 		_legendX 		= 10;
	public int 		_legendY 		= 20;
	//public int 		_legendH 		= 20;
	//public int 		_legendW 		= 40;
	
	public boolean _isDepthPlot = false;
	public boolean getLegendOn() 					{ return _legendOn; }
	public void setLegendOn(boolean legendOn) 		{ _legendOn = legendOn; }
	public void setLegendLabel(String legendLabel) 	{ _legendLabel = legendLabel; }
	//public void setLegendX(int legendX) 			{ _legendX = legendX; }
	public void setLegendY(int legendY) 			{ _legendY = legendY; }
	//public void setLegendW(int legendW) 			{ _legendW = legendW; }
	//public void setLegendH(int legendH) 			{ _legendH = legendH; }
	public void addLegendY(int increment) 		{ _legendY += increment; }
	
	public CommonPointPanel(int iType, boolean isDepthPlot, String catalog, String hLabel, String vLabel) {
		this(1, 1, iType, isDepthPlot, catalog, hLabel, vLabel);
	}
	
	public CommonPointPanel(int nRow, int nCol, int iType, boolean isDepthPlot, 
			String catalog, String hLabel, String vLabel) {
		//super(1, 1, iTtype<50?Orientation.X1DOWN_X2RIGHT:Orientation.X1RIGHT_X2UP, 
		//		iTtype<50?AxesPlacement.LEFT_TOP:AxesPlacement.LEFT_BOTTOM, null);
		super(nRow, nCol, isDepthPlot?Orientation.X1DOWN_X2RIGHT:Orientation.X1RIGHT_X2UP, 
				isDepthPlot?AxesPlacement.LEFT_TOP:AxesPlacement.LEFT_BOTTOM, null);
		_iType 	= iType;
		_isDepthPlot = isDepthPlot;
		_catalog 	= catalog;
		_hLabel 	= hLabel;
		_vLabel 	= vLabel;
		
		_hList 		= new ArrayList<float []>();
		_vList 		= new ArrayList<float []>();
		
		Font font = new Font ("Arial", Font.BOLD, 12); //Monospaced, Serif, Dialog, Sanserif
		setFont(font);
	}
	
	public int getIType() { return _iType; }

	public ArrayList<float []> getHList() { return _hList; }
	public ArrayList<float []> getVList() { return _vList; }
	
	public void addBackgroundGrid() {
		GridView gv = new GridView(Color.LIGHT_GRAY);
		gv.setName("Grid", "Major");
	    gv.setVisible(getVisible(gv.getCategory(), gv.getName()));
	    removeView(gv.getCategory(), gv.getName());
		addGridView(0, 0, gv);
	}
	private float min(float a, float b) { return a>b?b:a; }
	private float max(float a, float b) { return a>b?a:b; }
	
	public void adjustAxislimit(double dv0, double dv1, double dh0, double dh1) {
		double hmin = _minMax[0][1];
		double hmax = _minMax[1][1];
		double vmin = _minMax[0][0];
		double vmax = _minMax[1][0];
		if(vmin==vmax) return;
		if(hmin==hmax) return;
		
		double v = vmax - vmin;
		double h = hmax - hmin;
		//System.out.println("v="+v+" h="+h+" hmin="+hmin+" hmax="+hmax+" vmin="+vmin+" vmax="+vmax);
		vmin -= dv0*v;
		vmax += dv1*v;
		hmin -= dh0*h;
		hmax += dh1*h;
		
		setDataLimits(hmin, vmin, hmax, vmax);		
		setLimits(hmin, vmin, hmax, vmax);
	}
	public void adjustLogAxislimit(double dv1, double dh1) {
		double hmin = _minMax[0][1];
		double hmax = _minMax[1][1];
		double vmin = _minMax[0][0];
		double vmax = _minMax[1][0];
		if(vmin==vmax) return;
		if(hmin==hmax) return;
		
		double v = vmax - vmin;
		double h = hmax - hmin;
		vmax += dv1*v;
		hmax += dh1*h;
		
		setDataLimits(hmin, vmin, hmax, vmax);		
		setLimits(hmin, vmin, hmax, vmax);
	}
	public void setEqualAxis(double dm0, double dm1) {
		double hmin = _minMax[0][1];
		double hmax = _minMax[1][1];
		double vmin = _minMax[0][0];
		double vmax = _minMax[1][0];
		if(vmin==vmax) return;
		if(hmin==hmax) return;
		
//		double v1 = vmax>vmin?vmax:vmin;
//		double v0 = vmax>vmin?vmin:vmax;
//		double h1 = hmax>hmin?hmax:hmin;
//		double h0 = hmax>hmin?hmin:hmax;
//		
//		double v = v1 - v0;
//		double h = h1 - h0;
//		v0 -= dv0*v;
//		v1 += dv1*v;
//		h0 -= dh0*h;
//		h1 += dh1*h;
			
		
		double m0 = vmin<hmin?vmin:hmin;
		double m1 = vmax>hmax?vmax:hmax;
		double m = m1-m0;
		
		setLimits(m0-m*dm0, m0-m*dm0, m1+m*dm1, m1+m*dm1);
	}
	public void add(double [] hValue, double [] vValue, int append, String hLabel, String vLabel, 
			String style, String name, float markSize, float lineWidth) {
		add(toFloatArray(hValue), toFloatArray(vValue), append, hLabel, vLabel, 
				style, name, markSize, lineWidth);
	}
	public void add(float [] hValue, float [] vValue, int append, String hLabel, String vLabel, 
			String style, String name, float markSize, float lineWidth) {
		add(0, 0, hValue, vValue, append, hLabel, vLabel, 
				style, name, markSize, lineWidth);
	}
	public void add(int iRow, int iCol, float [] hValue, float [] vValue, int append, String hLabel, String vLabel, 
			String style, String name, float markSize, float lineWidth) {
	    if(hLabel!=null) {
	    	if(append==0) _hLabel = hLabel;
	    	else _hLabel += " "+hLabel;
	    }
    	setHLabel(iCol, _hLabel);
	    if(vLabel!=null) {
	    	if(append==0) _vLabel = vLabel;
	    	else _vLabel += " "+vLabel;
	    }
	    setVLabel(iRow, _vLabel);
	    
	    _hList.add(hValue);
	    _vList.add(vValue);
	    _minMax[0][1] = min(_minMax[0][1], ArrayMath.min(hValue));
	    _minMax[1][1] = max(_minMax[1][1], ArrayMath.max(hValue));
	    _minMax[0][0] = min(_minMax[0][0], ArrayMath.min(vValue));
	    _minMax[1][0] = max(_minMax[1][0], ArrayMath.max(vValue));

	    PointsViewWithLegend pv = null;
	    if(_isDepthPlot) pv = new PointsViewWithLegend(vValue, hValue);
	    else pv = new PointsViewWithLegend(hValue, vValue);
	    
	    pv.setLegendOn(_legendOn);
	    pv.setLegendLabel(_legendLabel);
	    //pv.setLegendX(_legendX);
	    pv.setLegendY(_legendY);
	    //pv.setLegendW(_legendW);
	    //pv.setLegendH(_legendH);
	    
		pv.setName(_catalog, name);
		pv.setStyle(style);
	    pv.setMarkSize(markSize);
	    pv.setLineWidth(lineWidth);
	    
	    removeViews(iRow, iCol, pv.getCategory(), pv.getName());
		addPointsViewWithLegend(iRow, iCol, pv);
		
		//panel.getWorld().getTile(0,0).getTileAxisBottom().setFormat("%1.0f");
		//panel.getWorld().getTile(0,0).getTileAxisLeft().setFormat("%1.0f");
		
	    //revalidate();
	}
	public void add(int iRow, int iCol, float [] hValue, float [] vValue, int append, String hLabel, String vLabel, 
			String style, String name, float markSize, float lineWidth, boolean hLinear, boolean vLinear) {
	    if(hLabel!=null) {
	    	if(append==0) _hLabel = hLabel;
	    	else _hLabel += " "+hLabel;
	    }
    	setHLabel(0, _hLabel);
	    if(vLabel!=null) {
	    	if(append==0) _vLabel = vLabel;
	    	else _vLabel += " "+vLabel;
	    }
	    setVLabel(0, _vLabel);
	    
	    _hList.add(hValue);
	    _vList.add(vValue);
	    _minMax[0][1] = min(_minMax[0][1], ArrayMath.min(hValue));
	    _minMax[1][1] = max(_minMax[1][1], ArrayMath.max(hValue));
	    _minMax[0][0] = min(_minMax[0][0], ArrayMath.min(vValue));
	    _minMax[1][0] = max(_minMax[1][0], ArrayMath.max(vValue));

	    PointsView pv = addPoints(iRow, iCol,hValue, vValue);
	    
	    if(hLinear) pv.setHScale(AxisScale.LINEAR);
	    else pv.setHScale(AxisScale.LOG10);
	    if(vLinear) pv.setVScale(AxisScale.LINEAR);
	    else pv.setVScale(AxisScale.LOG10);
	    
		pv.setName(_catalog, name);
		pv.setStyle(style);
	    pv.setMarkSize(markSize);
	    pv.setLineWidth(lineWidth);
	    
	    removeViews(iRow, iCol, pv.getCategory(), pv.getName());
		addPointsView(iRow, iCol, pv);
	}
	
	public static float [] toFloatArray(double [] d) {
		float [] f = new float[d.length];
		for(int i=0; i<f.length; i++) f[i] = (float)d[i];
		return f;
	}
	

}
