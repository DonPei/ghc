package com.geohammer.core;

public class FractureModel {
	private String 			_label 			= null;
	private int 			_id 			= 0;  //model type
	
	private double [][]		_model 			= null;
	
	public FractureModel() { 
		_model 		= new double[4][17];
	}
	public FractureModel(int id, String label, double [][] model) { 
		_id 			= id;
		_label 			= label;
		_model 			= model;	
	}
	public FractureModel(int id, String label, double cn, double ce, double cTVD) { 
		this();	
		_id 			= id;
		_label 			= label;
		_model[0][0] 	= cn;
		_model[0][1] 	= ce;
		_model[0][2] 	= cTVD;		
	}
	public FractureModel(int id, String label, double cn, double ce, double cTVD, double strike, double dip, double rake) { 
		this(id, label, cn, ce, cTVD, strike, dip, rake, 0, 0, 0);	
	}
	public FractureModel(int id, String label, double cn, double ce, double cTVD, double strike, double halfLength, double height, double width) { 
		this(id, label, cn, ce, cTVD, strike, 90, 90, halfLength, height, width);	
	}
	public FractureModel(int id, String label, double cn, double ce, double cTVD, double strike, double dip, double rake,
			double halfLength, double height, double width) { 
		this(id, label, cn, ce, cTVD);	
		_model[0][3] 	= strike;
		_model[0][4] 	= dip;
		_model[0][5] 	= rake;		
		_model[0][6] 	= halfLength;
		_model[0][7] 	= height;
		_model[0][8] 	= width;	
	}
	
//	public boolean isValid() {
//		double len = 2*_model[6];
//		return (len>_model[7] && _model[7]>_model[8]);		
//	}
//	public void validate() {
//		double a = 0;
//		if(_model[7]<_model[8]) {
//			a = _model[7];
//			_model[7] = _model[8];
//			_model[8] = a;
//		} 
//		
//		double len = 2*_model[6];
//		if(len<_model[7]) {
//			a = _model[7];
//			_model[7] = len;
//			_model[6] = 0.5*_model[7];
//		} 	
//	}

	private String [][] _elementNames = new String [][] {
		{"CenterN", "CenterE", "CenterTVD","Azimuth", "Dip", "Rake","HalfLength", "Height", "Width",
			"StrikeSlip", "DipSlip", "UpperFracHeight","LowerFracHeight", 
			"LeftFracHalfLen", "RightFracHalfLen","LeakOffZoneWidth", "PressureChangeRatio"
			}
	};
	public int getId() 						{ return _id; }
	public String getLabel() 				{ return _label; }
	public double [][] getModel() 			{ return _model; }
	
	public double getCenterN() 				{ return _model[0][0]; }
	public double getCenterE() 				{ return _model[0][1]; }
	public double getCenterTVD() 			{ return _model[0][2]; }
	public double getAzimuth() 				{ return _model[0][3]; }
	public double getDip() 					{ return _model[0][4]; }
	public double getRake() 				{ return _model[0][5]; }
	public double getHalfLength() 			{ return _model[0][6]; }
	public double getHeight() 				{ return _model[0][7]; }
	public double getWidth() 				{ return _model[0][8]; }
	public double getStrikeSlip() 			{ return _model[0][9]; }
	public double getDipSlip() 				{ return _model[0][10]; }
	public double getUpperFracHeight() 		{ return _model[0][11]; }
	public double getLowerFracHeight() 		{ return _model[0][12]; }
	public double getLeftFracHalfLen() 		{ return _model[0][13]; }
	public double getRightFracHalfLen() 	{ return _model[0][14]; }	
	public double getLeakOffZoneWidth() 	{ return _model[0][15]; }
	public double getPressureChangeRatio() 	{ return _model[0][16]; }
	
	public void setId(int id) 				{ _id = id; }
	public void setLabel(String label) 		{ _label = label; }
	public void setModel(double [][] model) { _model = model; }
	
	public void setCenterN(double v) 			{ _model[0][0] = v; }
	public void setCenterE(double v) 			{ _model[0][1] = v; }
	public void setCenterTVD(double v) 			{ _model[0][2] = v; }
	public void setAzimuth(double v) 			{ _model[0][3] = v; }
	public void setDip(double v) 				{ _model[0][4] = v; }
	public void setRake(double v)				{ _model[0][5] = v; }
	public void setHalfLength(double v) 		{ _model[0][6] = v; }
	public void setHeight(double v) 			{ _model[0][7] = v; }
	public void setWidth(double v) 				{ _model[0][8] = v; }
	public void setStrikeSlip(double v) 		{ _model[0][9] = v; }
	public void setDipSlip(double v) 			{ _model[0][10] = v; }
	public void setUpperFracHeight(double v) 	{ _model[0][11] = v; }
	public void setLowerFracHeight(double v) 	{ _model[0][12] = v; }
	public void setLeftFracHalfLen(double v) 	{ _model[0][13] = v; }
	public void setRightFracHalfLen(double v) 	{ _model[0][14] = v; }	
	public void setLeakOffZoneWidth(double v) 	{ _model[0][15] = v; }
	public void setPressureChangeRatio(double v){ _model[0][16] = v; }
	
	public void printClass() {
		System.out.println( _id + ", "+ _label );
		int k = 0;
		for(int i=0; i<_elementNames.length; i++) {
			for(int j=0; j<_elementNames[i].length; j++) {
				System.out.println(_elementNames[i][j] + " = "+ _model[i][j]);
			}
		}
	}
	
	public String toString() {
		String b = _id + ", "+ _label;
		int k = 0;
		for(int i=0; i<_model.length; i++) {
			String a = ", "+ _model[i][k++]+", "+ _model[i][k++]+", "+ _model[i][k++]+", "+ _model[i][k++]+", "+ _model[i][k++]+
					", "+ _model[i][k++]+", "+ _model[i][k++]+", "+ _model[i][k++]+", "+ _model[i][k++]+", "+ _model[i][k++]+
					", "+ _model[i][k++]+", "+ _model[i][k++]+", "+ _model[i][k++]+", "+ _model[i][k++]+", "+ _model[i][k++]+
					", "+ _model[i][k++]+", "+ _model[i][k++];
			b = b.concat(a+"\n");
		}
		
		return b;
	}
}
