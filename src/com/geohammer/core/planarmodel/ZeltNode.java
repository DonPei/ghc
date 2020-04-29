package com.geohammer.core.planarmodel;


public class ZeltNode {	
	private int _nElements = 20;
	private int _nodeNo;
	
	private double _x;
	private double _z;
	private double _vp1;
	private double _vp2;
	private double _vs1;
	private double _vs2;
	private double _den1;
	private double _den2; 
	
	// required for anisotropic study
	private double _gamma1; 	//
	private double _gamma2; 	//
	private double _delta1; 	//
	private double _delta2; 	//
	private double _epsilon1; 	// 
	private double _epsilon2; 	//
    
	private double _theta1; 	//
	private double _theta2; 	//
	private double _phi1; 	// 
	private double _phi2; 	//
	
	public ZeltNode() {
		setBndry(3);
		setX(0.0);
		setZ(0.0);
		setvp1(0.0);
		setvp2(0.0);
		setvs1(0.0);
		setvs2(0.0);
		setden1(0.0);
		setden2(0.0); 

		setgamma1(0.0);
		setgamma2(0.0); 
		setdelta1(0.0);
		setdelta2(0.0); 
		setepsilon1(0.0);
		setepsilon2(0.0); 
		
		settheta1(0.0);
		settheta2(0.0); 
		setphi1(0.0);
		setphi2(0.0);
	}
	public ZeltNode(int iBndry, double x, double z, double vp1, double vp2, double vs1, double vs2, double den1, double den2, 
			double gamma1, double gamma2, double delta1, double delta2, double epsilon1, double epsilon2, 
			double theta1, double theta2, double phi1, double phi2) {
		setBndry(iBndry);
		setX(x);
		setZ(z);
		setvp1(vp1);
		setvp2(vp2);
		setvs1(vs1);
		setvs2(vs2);
		setden1(den1);
		setden2(den2); 

		setgamma1(gamma1);
		setgamma2(gamma2); 
		setdelta1(delta1);
		setdelta2(delta2); 
		setepsilon1(epsilon1);
		setepsilon2(epsilon2); 
		
		settheta1(theta1);
		settheta2(theta2); 
		setphi1(phi1);
		setphi2(phi2);
	}

    public void setValueAt( int i, Object obj )  {
    	double d=0;
    	int ia=1;

    	if(i==8) ia = Integer.parseInt(obj.toString());
    	else if(i==17) ia = Integer.parseInt(obj.toString());
    	else d = Double.parseDouble(obj.toString());
    	
    	if( i==0 ) 		setX(d);
    	else if( i==1 ) setZ(d);
    	else if( i==2 ) setvp1(d);
    	else if( i==3 ) setvp2(d);
    	else if( i==4 ) setvs1(d);
    	else if( i==5 ) setvs2(d);
    	else if( i==6 ) setden1(d);
    	else if( i==7 ) setden2(d);
    	
    	else if( i==8 ) setBndry(ia);	
    	
    	else if( i==11 ) setgamma1(d);
    	else if( i==12 ) setgamma2(d);
    	else if( i==13 ) setdelta1(d);
    	else if( i==14 ) setdelta2(d);
    	else if( i==15 ) setepsilon1(d);
    	else if( i==16 ) setepsilon2(d);
    	
    	else if( i==17 ) settheta1(d);
    	else if( i==18 ) settheta2(d);
    	else if( i==19 ) setphi1(d);
    	else if( i==20 ) setphi2(d);
    	
    	else if( i==21 ) setnElement(ia);
    	else { } 	
    }
    
    public void setNodes( int n, double[] DA )  {
    	double d=0;
    	int ia=1;

    	if(n==_nElements) {

    		for(int i=0; i<n; i++) {
    			if(i==8) ia = (int)(DA[i]);
    			else if(i==17) ia = (int)(DA[i]);
    			else d = DA[i];

    			if( i==0 ) 		setX(d);
    			else if( i==1 ) setZ(d);
    			else if( i==2 ) setvp1(d);
    			else if( i==3 ) setvp2(d);
    			else if( i==4 ) setvs1(d);
    			else if( i==5 ) setvs2(d);
    			else if( i==6 ) setden1(d);
    			else if( i==7 ) setden2(d);

    			else if( i==8 ) setBndry(ia);

    			else if( i==11 ) setgamma1(d);
    			else if( i==12 ) setgamma2(d);
    			else if( i==13 ) setdelta1(d);
    			else if( i==14 ) setdelta2(d);
    			else if( i==15 ) setepsilon1(d);
    			else if( i==16 ) setepsilon2(d);
    			
    			else if( i==17 ) settheta1(d);
    	    	else if( i==18 ) settheta2(d);
    	    	else if( i==19 ) setphi1(d);
    	    	else if( i==20 ) setphi2(d);
    			
    	    	else if( i==21 ) setnElement(ia);
    			else { } 	
    		}
    	}
    }
    
    public double [] toArray(  )  {
    	double [] DA = new double[_nElements];

    	DA[0] = getX();
    	DA[1] = getZ();
    	DA[2] = getvp1();
    	DA[3] = getvp2();
    	DA[4] = getvs1();
    	DA[5] = getvs2();
    	DA[6] = getden1();
    	DA[7] = getden2();

    	DA[8] = (double)getBndry();

    	DA[11] = getgamma1();
    	DA[12] = getgamma2();
    	DA[13] = getdelta1();
    	DA[14] = getdelta2();
    	DA[15] = getepsilon1();
    	DA[16] = getepsilon2();

    	DA[17] = gettheta1();
    	DA[18] = gettheta2();
    	DA[19] = getphi1();
    	DA[20] = getphi2();
    	
    	DA[21] = (double)getnElement();
    	
    	return DA;
    }
    public int getnElement()	{return _nElements;}
    public int getBndry()		{return _nodeNo;}
    public double getX()		{return _x;}
    public double getZ()		{return _z;}
    public double getvp1()		{return _vp1;}
    public double getvp2()		{return _vp2;}
    public double getvs1()		{return _vs1;}
    public double getvs2()		{return _vs2;}
    public double getden1()		{return _den1;}
    public double getden2()		{return _den2;}
    
    public double getgamma1()		{return _gamma1;}
    public double getgamma2()		{return _gamma2;}
    public double getdelta1()		{return _delta1;}
    public double getdelta2()		{return _delta2;}
    public double getepsilon1()		{return _epsilon1;}
    public double getepsilon2()		{return _epsilon2;}
    public double gettheta1()		{return _theta1;}
    public double gettheta2()		{return _theta2;}
    public double getphi1()			{return _phi1;}
    public double getphi2()			{return _phi2;}
    
    public void setnElement(int i)				{_nElements = i;}
    public void setBndry(int i)					{_nodeNo = i;}
    public void setX(double d)					{_x = d;}
    public void setZ(double d)					{_z = d;}
    public void setvp1(double d)				{_vp1 = d;}
    public void setvp2(double d)				{_vp2 = d;}
    public void setvs1(double d)				{_vs1 = d;}
    public void setvs2(double d)				{_vs2 = d;}
    public void setden1(double d)				{_den1 = d;}
    public void setden2(double d)				{_den2 = d;}
    
    public void setgamma1(double d)				{_gamma1 = d;}
    public void setgamma2(double d)				{_gamma2 = d;}
    public void setdelta1(double d)				{_delta1 = d;}
    public void setdelta2(double d)				{_delta2 = d;}
    public void setepsilon1(double d)			{_epsilon1 = d;}
    public void setepsilon2(double d)			{_epsilon2 = d;}
    
    public void settheta1(double d)				{_theta1 = d;}
    public void settheta2(double d)				{_theta2 = d;}
    public void setphi1(double d)				{_phi1 = d;}
    public void setphi2(double d)				{_phi2 = d;}
    
    public String toString() {
		String b;
	    
		b = String.format("%4d, %4d, %8.2f, %8.2f, %8.2f, %8.2f, %8.2f, %8.2f, %8.2f, %8.2f, " +
				"%8.2f, %8.2f, %8.2f, %8.2f, %8.2f, %8.2f, %8.2f, %8.2f, %8.2f, %8.2f \n", 
				_nElements, _nodeNo, _x, _z, _vp1, _vp2, _vs1, _vs2, _den1, _den2, 
				_gamma1, _gamma2, _delta1, _delta2, _epsilon1, _epsilon2, _theta1, _theta2, _phi1, _phi2); 
	     
    	return b;
    }

    
}
