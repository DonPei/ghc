package org.ucdm.launcher.dialog;

public class K51Option {

	int 		_nPoints 	= 200; 	
	float 		_x1 		= -5;
	float 		_x2 		= 5;
	int 		_iCurveType = 2;
	
	float 		_a 			= 0.0f; 	
	float 		_a1 		= 0;
	float 		_a2 		= 1;
	
	float 		_b 			= 5.0f;
	
	public K51Option() {
	}
	
	public int getNPoints() 					{ return _nPoints; }
	public int getICurveType() 					{ return _iCurveType; }
	public float getX1() 						{ return _x1; }
	public float getX2() 						{ return _x2; }
	public float getA() 						{ return _a; }
	public float getA1() 						{ return _a1; }
	public float getA2() 						{ return _a2; }
	public float getB() 						{ return _b; }
	
	public void setX1(float x1) 				{ _x1 = x1; }
	public void setX2(float x2) 				{ _x2 = x2; }	
	public void setA(float a) 					{ _a = a; }
	public void setA1(float a1) 				{ _a1 = a1; }
	public void setA2(float a2) 				{ _a2 = a2; }
	public void setB(float b) 					{ _b = b; }
	public void setICurveType(int iCurveType) 	{ _iCurveType = iCurveType; }
	public void setNPoints(int nPoints) 		{ _nPoints = nPoints; }
	

}
