package com.geohammer.rt.pseudo3d;

public class RTCoefficient {
	
	private double p 	= 0.0; // ray parameter
	
	private double vpi 	= 0.0; // incident media
	private double vsi 	= 0.0;
	private double deni = 0.0;
	private double vpt 	= 0.0; // transmitted media
	private double vst 	= 0.0;
	private double dent = 0.0;
	
	public RTCoefficient(double p, double vpi, double vsi, double deni, double vpt, double vst, double dent) {
		this.p 		= p;
		
		this.vpi 	= vpi;
		this.vsi 	= vsi;
		this.deni 	= deni;
		this.vpt 	= vpt;
		this.vst 	= vst;
		this.dent 	= dent;
	}
	
	public void start() {
		// vertical slownesses
		double p2 = p*p;
		double vpi2 = vpi*vpi;
		double vpt2 = vpt*vpt;
		double vsi2 = vsi*vsi;
		double vst2 = vst*vst;
		
		double etaai = Math.sqrt(1.0/vpi2 - p2);
		double etaat = Math.sqrt(1.0/vpt2 - p2);
		double etabi = Math.sqrt(1.0/vsi2 - p2);
		double etabt = Math.sqrt(1.0/vst2 - p2);
		
		double a = dent*(1.0-2.0*vst2*p2) - deni*(1.0-2.0*vsi2*p2);
		double b = dent*(1.0-2.0*vst2*p2) + 2.0*deni*vsi2*p2;
		double c = deni*(1.0-2.0*vsi2*p2) + 2.0*dent*vst2*p2;
		double d = 2.0*(dent*vst2-deni*vsi2);
		
		double E = b* etaai + c* etaat;
		double F = b* etabi + c* etabt;
		double G = a - d * etaai*etabt;
		double H = a - d * etaat* etabi;
		double D = E*F + G*H*p2;
		
		double Rpp = ( (b*etaai-c*etaat)*F - (a + d*etaai*etabt)*H*p2)/D;
		double Rps = -(2 * etaai* (a* b + d * c* etaat* etabt)* p * vpi/vsi )/D;
		double Rss = -((b*etabi-c*etabt)*E-(a+d*etaat*etabi)*G*p2)/D;
		double Rsp = -(2*etabi*(a*b+d*c*etaat*etabt)*p*(vsi/vpi))/D;
		double Tpp = (2*deni*etaai*F*(vpi/vpt))/D;
		double Tps = (2*deni*etaai*H*p*(vpi/vst))/D;
		double Tss = 2*deni*etabi*E*(vsi/vst)/D;
		double Tsp = -2*(deni*etabi*G*p*(vsi/vpt))/D;
		
	}

}
