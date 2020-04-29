package com.geohammer.core.planarmodel;

public class SingleLabRecord extends HalfSpace {
	private String 	_location		= null;
	private String 	_lithology		= null;
	private String 	_sampleCategory	= null;
	private String 	_sampleName		= null;
	
	public SingleLabRecord()	{ }
	
	public SingleLabRecord(int id, double vp, double vs, double den, 
			double vpGradientX, double vpGradientY, double vpGradientZ, double vsGradientX, double vsGradientY, double vsGradientZ, 
			double delta, double epsilon, double gamma, double qp, double qs, double theta, double phi, 
			double delta1, double epsilon1, double gamma1, double delta3, 
			double c11, double c12, double c13, double c33, double c44, double c55, double c66, 
			String location, String lithology, String sampleCategory, String sampleName)	{
		super(id, vp, vs, den, 
				vpGradientX, vpGradientY, vpGradientZ, vsGradientX, vsGradientY, vsGradientZ,
				delta, epsilon, gamma, qp, qs, theta, phi,  
				delta1, epsilon1, gamma1, delta3,
				c11, c12, c13, c33, c44, c55, c66);
		_location = location;
		_lithology = lithology;
		_sampleCategory = sampleCategory;
		_sampleName = sampleName;
	}
	
	public SingleLabRecord copy() { 
		return new SingleLabRecord(getId(), getVp(), getVs(), getDen(), 
				getVpGradientX(), getVpGradientY(), getVpGradientZ(), getVsGradientX(), getVsGradientY(), getVsGradientZ(), 
				getDelta(), getEpsilon(), getGamma(), getQp(), getQs(), getTheta(), getPhi(),
				getDelta1(), getEpsilon1(), getGamma1(), getDelta3(), 
				getC11(), getC12(), getC13(), getC33(), getC44(), getC55(), getC66(),
				_location, _lithology, _sampleCategory, _sampleName); 
	}
	
	public String getLocation() 		{ return _location; }
	public String getLithology() 		{ return _lithology; }
	public String getSampleCategory() 	{ return _sampleCategory; }
	public String getSampleName() 		{ return _sampleName; }
	
	public void setLocation(String location) 				{ _location = location; }
	public void setLithology(String lithology) 				{ _lithology = lithology; }
	public void setSampleCategory(String sampleCategory) 	{ _sampleCategory = sampleCategory; }
	public void setSampleName(String sampleName) 			{ _sampleName = sampleName; }
	
	public String toString() {
		String b = _sampleName;
		if(_sampleCategory!=null) 	b = _sampleCategory+" "+b;
		if(_lithology!=null) 		b = _lithology+" "+b;
		if(_location!=null) 		b = _location+" "+b;
		return super.toString() +" "+b;
	}
	
	
}
