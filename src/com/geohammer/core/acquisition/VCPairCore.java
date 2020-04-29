package com.geohammer.core.acquisition;

import com.geohammer.dsp.LinearRegression;

import edu.mines.jtk.util.ArrayMath;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VCPairCore {

	public int iUnit 		= 0;// distance unit in ft (0), m (1), km(2)
	public int nEvents;		// total number of events/shots
	public int flag[];			// total number of pairs in each events
	public int 	orgW[];    	// 1-overwrite event origin time by calculated value 0-no over write
	public double orgT[];   	// event origin time

	public double rN[];		// receiver coordinates in positive north direction
	public double rE[];		// receiver coordinates in positive east direction
	public double rD[];		// receiver coordinates in positive down direction
	public double eN[];		// source coordinates in positive north direction
	public double eE[];		// source coordinates in positive east direction
	public double eD[];		// source coordinates in positive down direction

	public double obsPT[];		// observed P-wave time
	public double calPT[];		// calculated P-wave time
	public double obsST[];		// observed SH-wave time
	public double calST[];		// calculated SH-wave time
	public double obsSV[];		// observed SV-wave time
	public double calSV[];		// calculated SV-wave time
	
	public double data[][];	// auxilliary data holder
	
	public double INVALID_D = -99999.0;
	
	//all int variables are zero-based
	public VCPairCore() {

	}
	public VCPairCore(int nEvents, int nReceivers){
		this(nEvents, nReceivers, 2);
	}
	public VCPairCore(int nEvents, int nReceivers, int distUnit){
		this.iUnit 		= distUnit;
		this.nEvents 	= nEvents;
		allocate(nEvents, nReceivers);
	}
	public VCPairCore(double [] sE, double [] sN, double [] sD, double [] rE, double [] rN, double [] rD, int distUnit){
		this(sE.length, sE.length*rE.length, distUnit);
		
		for(int i=0, k=0; i<nEvents; i++) {
			this.flag[i] 		= rE.length;
			this.eN[i] 			= sN[i];			
			this.eE[i] 			= sE[i];
			this.eD[i] 			= sD[i];
			this.orgT[i] 		= 0.0;
			this.orgW[i] 		= 0;
			for(int j=0; j<rE.length; j++, k++) {
				this.rN[k] 			= rN[j];			
				this.rE[k] 			= rE[j];
				this.rD[k] 			= rD[j];	
				
				this.obsPT[k] 		= 0.0;			
				this.calPT[k] 		= 0.0;
				this.obsST[k] 		= 0.0;			
				this.calST[k] 		= 0.0;
				this.obsSV[k] 		= 0.0;			
				this.calSV[k] 		= 0.0;
			}
		}
	}
	public void allocate(int nEvents, int nReceivers) {
		allocateEvent(nEvents);	
		allocateReceiver(nReceivers);
	}
	public void allocateAuxillary(int nRow, int nCol) {
		this.data = new double[nRow][nCol];
	}
	public void allocateEvent(int nEvents) {
		if(nEvents<=0) return;
		flag 	= new int[nEvents];
		orgW	= new int[nEvents];
		eN 		= new double[nEvents];
		eE 		= new double[nEvents];
		eD 		= new double[nEvents];
		orgT 	= new double[nEvents];
	}
	public void allocateReceiver(int nReceivers) {
		if(nReceivers<=0) return;
		rN 		= new double[nReceivers];
		rE 		= new double[nReceivers];
		rD 		= new double[nReceivers];

		obsPT 	= new double[nReceivers];
		calPT 	= new double[nReceivers];

		obsST 	= new double[nReceivers];
		calST 	= new double[nReceivers];	

		obsSV 	= new double[nReceivers];
		calSV 	= new double[nReceivers];	
	}

	public void genReceiverCircle(double cN, double cE, double cD, double r) {
		int nReceivers = rN.length;
		double dTheta = (360/nReceivers)*(Math.PI/180);
		double theSin = 0;
		double theCos = 0;
		
		eN[0] = cN;
		eE[0] = cE;
		eD[0] = cD;
		flag[0] = nReceivers;
		for(int i=0; i<nReceivers; i++) {
			rD[i] = eD[0]+r*Math.cos(dTheta*i);
			rE[i] = eE[0]+r*Math.sin(dTheta*i);
			rN[i] = eN[0]+0;
		}		
	}
	//basic gets and sets 
	public double getData(int i, int j) 	{ return data[i][j]; }
	
	public int getFlag(int i) 		{ return flag[i]; }
	public double getOrgT(int i) 	{ return orgT[i]; }
	public int getOrgW(int i) 		{ return orgW[i]; }
	public double getEE(int i) 		{ return eE[i]; }
	public double getED(int i) 		{ return eD[i]; }
	//public double getEU(int i) 		{ return -eD[i]; }
	public double getEN(int i) 		{ return eN[i]; }

	public double getRE(int i) 		{ return rE[i]; }
	public double getRD(int i) 		{ return rD[i]; }
	//public double getRU(int i) 		{ return -rD[i]; }
	public double getRN(int i) 		{ return rN[i]; }
	public double getCalPT(int i) 	{ return calPT[i]; }
	public double getObsPT(int i) 	{ return obsPT[i]; }
	public double getCalST(int i) 	{ return calST[i]; }
	public double getObsST(int i) 	{ return obsST[i]; }
	public double getCalSV(int i) 	{ return calSV[i]; }
	public double getObsSV(int i) 	{ return obsSV[i]; }
	
	public int getMaxNumOfReceiversPerShot() 	{ 
		int max = 0;
		for(int i=0; i<nEvents; i++) {
			max = max>flag[i] ? max:flag[i];
		}
		return max; 
	}
	
	public int getNumOfReceivers(int i) 		{ return flag[i]; }
	public int getTotalNumOfReceivers() 		{ return getIndex(-1, -1); }
	public int getEventIndex(int iEvent) 		{ return getIndex(iEvent, -1);}
	public int getIndex(int iEvent, int iReceiver) {
		int k = 0;
		if(iReceiver==-1) {
			for(int i=0; i<nEvents; i++) {
				if(i==iEvent) return k;
				k += flag[i];
			}
			return k;
		} else {
			for(int i=0; i<nEvents; i++) {
				if(i==iEvent) {
					if(iReceiver>=0 &&iReceiver<flag[i]) return k+iReceiver;
				}
				k += flag[i];
			}
			return k;
		}
	}
	public int getEventIndex(double ee, double en, double ed) {
		for(int i=0; i<nEvents; i++) {
			if(ee==eE[i]&&en==eN[i]&&ed==eD[i]) return i; 
		}
		return -1;
	}

	public double [] getCalT(int iVp) {
		if(iVp==1) 		return calPT; 
		else if(iVp==2) return calST;
		else 			return calSV; 
	}
	public double [] getObsT(int iVp) {
		if(iVp==1) 		return obsPT; 
		else if(iVp==2) return obsST;
		else 			return obsSV; 
	}
	
	public void setData(int v, int []array) 		{ for(int i=0; i<array.length; i++)  array[i] = v; }
	public void setData(double v, double []array) 	{ for(int i=0; i<array.length; i++)  array[i] = v; }
	
	public void setData(int [] src, int [] target) 				{ target = src; }
	public void setData(double [] src, double [] target) 		{ target = src; }
	public void setData(int n, int [] src, int [] target) 		{ for(int i=0; i<n; i++)  target[i] = src[i]; }
	public void setData(int n, double [] src, double [] target) { for(int i=0; i<n; i++)  target[i] = src[i]; }
	public void setData(int n, float [] src, double [] target) { for(int i=0; i<n; i++)  target[i] = src[i]; }
	
	public void setData(int index, double [] target, double v) 	{ target[index] = v; }
	public void setData(int index, int [] target, int v) 		{ target[index] = v; }
	
	public void setNEvents(int v ) 				{ nEvents = v; }
	public void setUnit(int v ) 				{ iUnit = v; }
	
	public boolean isObsSTExist() {
		boolean exist = false;
		int n = obsST.length;
		for(int i=0; i<n; i++) { 
			if(obsST[i]>0 && obsST[i]<10) exist = true; 
		}
		return exist;
	}

	public boolean isValidReceiver(int id, int iReceiver){ 
		if(id==0&&isInValid(rE[iReceiver], rN[iReceiver], rD[iReceiver])) return false;
		else if(id==100010&&isInValid(obsPT[iReceiver])) return false;
		else if(id==100001&&isInValid(obsST[iReceiver])) return false;
		else if(id==100011&&isInValid(obsPT[iReceiver], obsST[iReceiver])) return false;
		
		else if(id==110000&&isInValid(calPT[iReceiver])) return false;
		else if(id==101000&&isInValid(calST[iReceiver])) return false;
		else if(id==100100&&isInValid(calSV[iReceiver])) return false;
		else if(id==111000&&isInValid(calPT[iReceiver], calST[iReceiver])) return false;
		else if(id==110100&&isInValid(calPT[iReceiver], calSV[iReceiver])) return false;
		else if(id==111100&&isInValid(calPT[iReceiver], calST[iReceiver], calSV[iReceiver])) return false;
		
		else return true;
	}
	public boolean isInValid(double v1) 				{ return isInValid(v1, INVALID_D, INVALID_D); }
	public boolean isInValid(double v1, double v2) 		{ return isInValid(v1, v2, INVALID_D); }
	public boolean isInValid(double v1, double v2, double v3){ 
		if(v1==INVALID_D&&v2==INVALID_D&&v3==INVALID_D) return true;
		else return false;
	}
	
	public int getNumOfValidEvents(){ return getNumOfValid(selectValidEvents()); }
	public boolean isValidEvent(int iShot){ // all element must be _INVALID_F
		if(isInValid(eE[iShot], eN[iShot], eD[iShot])) return false;
		else return true;
	}
	
	public int [] selectValidEvents() 				{
		int [] selected = new int[getNEvents()];
		for(int i=0; i<nEvents; i++) {
			if(isValidEvent(i)) { 
				selected[i] = 1;
			} else if(getNumOfValidReceivers(111000)==0) { 
				selected[i] = 0;
			} else {
				selected[i] = 0;
			}
		}
		return selected;
	}
	
	public int getNumOfValidReceivers(int id) 				{ return getNumOfValid(selectValidReceivers(id)); }
	public int getNumOfValidReceivers(int id, int iShot) 	{ return getNumOfValid(selectValidReceivers(id, iShot)); }
	public int [] selectValidReceivers(int id) 				{ return selectValidReceivers(id, -1); }
	public int [] selectValidReceivers(int id, int iShot) 	{
		int [] selected = new int[getTotalNumOfReceivers()];
		for(int i=0, k=0; i<nEvents; i++) {
			if(iShot==i||iShot==-1) {
				for(int j=0; j<flag[i]; j++, k++) {	
					if(isValidReceiver(id, k)) { 
						selected[k] = 1;
					} else {
						selected[k] = 0;
					}
				}
			} else {
				for(int j=0; j<flag[i]; j++, k++) {
					selected[k] = 0;
				}
			}
		}
		return selected;
	}
	public int getNumOfValid(int [] selected) 	{ 
		int k = 0;
		for(int i=0; i<selected.length; i++) {
			if(selected[i]==1) { k++; }
		} 
		return k;
	}
	
	public double [] getREArray(int iShot) 		{ return getArray(iShot, rE); }
	public double [] getRNArray(int iShot) 		{ return getArray(iShot, rN); }
	public double [] getRDArray(int iShot) 		{ return getArray(iShot, rD); }
	public double [] getObsPTArray(int iShot) 	{ return getArray(iShot, obsPT); }
	public double [] getObsSTArray(int iShot) 	{ return getArray(iShot, obsST); }
	public double [] getObsSVArray(int iShot) 	{ return getArray(iShot, obsSV); }
	public double [] getCalPTArray(int iShot) 	{ return getArray(iShot, calPT); }
	public double [] getCalSTArray(int iShot) 	{ return getArray(iShot, calST); }
	public double [] getCalSVArray(int iShot) 	{ return getArray(iShot, calSV); }
	public double [] getArray(int iShot, double [] v) {
		double [] x = new double[flag[iShot]];
		for(int k=0, i=0; i<nEvents; i++) {
			if(iShot==i) {
				for(int j=0; j<flag[i]; j++) {
					x[j] = v[k++];
				}
				return x;
			} 
			k += flag[i];
		}
		return x;
	}

	public void scale(double scalor, double [] v) {
		for(int i=0; i<v.length; i++) {v[i] *= scalor; }
	}
	
	public double [] getSize() { return new double[]{getMinE(), getMaxE(), getMinN(), getMaxN(), getMinD(), getMaxD()}; }
	public double getMinE() { double a=extreme(0, eE); double b=extreme(0, rE); return a<b?a:b; }
	public double getMinN() { double a=extreme(0, eN); double b=extreme(0, rN); return a<b?a:b; }
	public double getMinD() { double a=extreme(0, eD); double b=extreme(0, rD); return a<b?a:b; }
	
	public double getMaxE() { double a=extreme(1, eE); double b=extreme(1, rE); return a>b?a:b; }
	public double getMaxN() { double a=extreme(1, eN); double b=extreme(1, rN); return a>b?a:b; }
	public double getMaxD() { double a=extreme(1, eD); double b=extreme(1, rD); return a>b?a:b; }

	//id=0 for min 1 for max
	public double extreme(int id, double [] v) {
		double x = 1.0e10;
		if(id==1) x = -x;
		for(int i=0; i<v.length; i++) {
			if(id==0 && v[i]<x) { x = v[i]; }
			if(id==1 && v[i]>x) { x = v[i]; }
		}
		return x;
	}
	public int extremeIndex(int id, double [] v) {
		double x = 1.0e10;
		if(id==1) x = -x;
		int k = 0;
		for(int i=0; i<v.length; i++) {
			if(id==0 && v[i]<x) { x = v[i]; k = i; }
			if(id==1 && v[i]>x) { x = v[i]; k = i; }
		}
		return k;
	}
	public double receiverSpacing(int iReceiver, int jReceiver){
		double diffE = rE[iReceiver]-rE[jReceiver];
		double diffN = rN[iReceiver]-rN[jReceiver];
		double diffD = rD[iReceiver]-rD[jReceiver];
		return Math.sqrt(diffE*diffE+diffN*diffN+diffD*diffD);
	}
	public double receiverSurfaceSpacing(int iReceiver, int jReceiver){
		double diffE = rE[iReceiver]-rE[jReceiver];
		double diffN = rN[iReceiver]-rN[jReceiver];
		return Math.sqrt(diffE*diffE+diffN*diffN);
	}
	public double distance(int iShot, int iReceiver){
		double diffE = rE[iReceiver]-eE[iShot];
		double diffN = rN[iReceiver]-eN[iShot];
		double diffD = rD[iReceiver]-eD[iShot];
		return Math.sqrt(diffE*diffE+diffN*diffN+diffD*diffD);
	}
	public double surfDistance(int iShot, int iReceiver){
		double diffE = rE[iReceiver]-eE[iShot];
		double diffN = rN[iReceiver]-eN[iShot];
		return Math.sqrt(diffE*diffE+diffN*diffN);
	}
	public void projectOnLine() {
		double surfaceD = 0.0;
		for(int i=0, k=0; i<nEvents; i++) {
			surfaceD = 0.0;
			for(int j=0; j<flag[i]; j++, k++) {
				surfaceD += surfDistance(i, k);
			}
			surfaceD /= flag[i];
			
			k -= flag[i];
			for(int j=0; j<flag[i]; j++, k++) {
				rE[k] = 0.0;
				rN[k] = 0.0;
			}
			eE[i] = 0.0;
			eN[i] = surfaceD;
		}
	}

	public void shift(double shiftD)				{ shift(0.0, 0.0, shiftD); }
	public void shift(double shiftN, double shiftE)	{ shift(shiftN, shiftE, 0.0); }
	public void shift(double shiftN, double shiftE, double shiftD){
		shiftEvents(-1, shiftN, shiftE, shiftD);
		shiftReceivers(-1, shiftN, shiftE, shiftD);
	}
	public void shiftEvents(int iShot, double shiftN, double shiftE, double shiftD){
		for(int i=0; i<nEvents; i++) {
			if(iShot==i||iShot==-1) {
				eN[i] += shiftN;
				eE[i] += shiftE;
				eD[i] += shiftD;
			}
		}
	}
	public void shiftReceivers(int iShot, double shiftN, double shiftE, double shiftD){
		for(int i=0, k=0; i<nEvents; i++) {
			if(iShot==i||iShot==-1) {
				for(int j=0; j<flag[i]; j++, k++) {
					rN[k] += shiftN;
					rE[k] += shiftE;
					rD[k] += shiftD;
				}
			} else {
				k += flag[i];
			}
		}
	}
	
	 
//	public void rotate(double azimuth){
//		double ang = azimuth*Math.PI/180.0;
//		double theSin = Math.sin(ang);
//		double theCos = Math.cos(ang);
//		double[][] M = new double[][] {{theCos, -theSin},{theSin, theCos}}; 
//		double ax = 0.0;
//		double ay = 0.0;
//		for(int i=0, k=0; i<nEvents; i++) {
//			ax = M[0][0]*eE[i]+M[0][1]*eN[i]; //xprime
//			ay = M[1][0]*eE[i]+M[1][1]*eN[i];
//			eE[i] = ax;
//			eN[i] = ay;
//			for(int j=0; j<flag[i]; j++, k++) {
//				ax = M[0][0]*rE[k]+M[0][1]*rN[k];
//				ay = M[1][0]*rE[k]+M[1][1]*rN[k];
//				rE[k] = ax;
//				rN[k] = ay;
//			}
//		}
//	}
	public void rotate(double [][] R){
		double ax=0, ay=0, az=0;
		for(int i=0, k=0; i<nEvents; i++) {
			ax = R[0][0]*eE[i]+R[0][1]*eN[i]+R[0][2]*eD[i];
			ay = R[1][0]*eE[i]+R[1][1]*eN[i]+R[1][2]*eD[i];
			az = R[2][0]*eE[i]+R[2][1]*eN[i]+R[2][2]*eD[i];
			eE[i] = ax;
			eN[i] = ay;
			eD[i] = az;
			for(int j=0; j<flag[i]; j++, k++) {
				ax = R[0][0]*rE[k]+R[0][1]*rN[k]+R[0][2]*rD[k];
				ay = R[1][0]*rE[k]+R[1][1]*rN[k]+R[1][2]*rD[k];
				az = R[2][0]*rE[k]+R[2][1]*rN[k]+R[2][2]*rD[k];
				rE[k] = ax;
				rN[k] = ay;
				rD[k] = az;
			}
		}
	}
	public void rotate(double [] rp, double [][] rm) {
		shift(-rp[0], -rp[1], -rp[2]);
		rotate(rm);
		shift(rp[0], rp[1], rp[2]);
	}
	public void ftToMeter() { convertUnitSystem(0.3048); iUnit = 1; }
	public void meterToFt() { convertUnitSystem(3.28084); iUnit = 2;}
	public void convertUnitSystem(double velScalor) {
		for(int i=0, k=0; i<nEvents; i++) {
			eE[i] *= velScalor;
			eN[i] *= velScalor;
			eD[i] *= velScalor;
			for(int j=0; j<flag[i]; j++, k++) {
				rE[k] *= velScalor;
				rN[k] *= velScalor;
				rD[k] *= velScalor;
			}
		}
	}
	public void toENU() {
		for(int i=0, k=0; i<nEvents; i++) {
			double e = eE[i];
			double n = eN[i];
			double d = eD[i];
			eE[i] = 0;
			eN[i] = 0;
			eD[i] = 0;
			for(int j=0; j<flag[i]; j++, k++) {
				rE[k] -= e;
				rN[k] -= n;
				rD[k] -= d;
				rD[k] *= -1;
			}
		}
	}
	
	public double [] calBoundingBox() {
		double [][] n = new double[][] { eN, rN };
		double [][] e = new double[][] { eE, rE };
		double [][] d = new double[][] { eD, rD };
		return new double[]{ ArrayMath.min(n), ArrayMath.min(e), ArrayMath.min(d), 
				ArrayMath.max(n), ArrayMath.max(e), ArrayMath.max(d)};
	}
	
	public double [] catVector(double [] M1, double [] M2) {
		if(M2==null) return M1;
		int nRow = M1.length+M2.length;
		double [] M = new double[nRow];

		int k = 0;
		for(int i=0; i<M1.length; i++) {
			M[k++] = M1[i];
		}
		for(int i=0; i<M2.length; i++) {
			M[k++] = M2[i];
		}

		return M;
	}
	
	public boolean isValid(double v) {
		return !(v<=0.0 || v>=10.0);
	}
	
	public double calAzimuthFromShots(double centerN, double centerE) { 
		double [] x = eE;
		double [] y = eN;
		double minX = ArrayMath.min(x);
		double maxX = ArrayMath.max(x);
		double dx = maxX-minX;
		double meanX = (maxX+minX)/2.0;
		double minY = ArrayMath.min(y);
		double maxY = ArrayMath.max(y);
		double dy = maxY-minY;
		double meanY = (maxY+minY)/2.0;
		
		double azimuth = 0;
		boolean x1y2 = dx>=dy;
		if(x1y2) {
			LinearRegression linearRegression = new LinearRegression(x, y);
			//-0.544817 n + 19.802650  (R^2 = 0.969)
			//System.out.println(linearRegression.toString());		
			//centerX = meanX;
			//centerY = linearRegression.predict(centerX);
			
			double slope = linearRegression.slope();
			double theCos = 1.0/Math.sqrt(1+slope*slope);
			double theta = Math.acos(theCos)*180.0/Math.PI;
			if(slope>0) {
				azimuth = 90-theta;
			} else {
				azimuth = 90+theta;
			}
		} else {
			LinearRegression linearRegression = new LinearRegression(y, x);
			//-0.544817 n + 19.802650  (R^2 = 0.969)
			//System.out.println(linearRegression.toString());		
			//centerY = meanY;
			//centerX = linearRegression.predict(centerY);
			
			double slope = linearRegression.slope();
			double theCos = 1.0/Math.sqrt(1+slope*slope);
			double theta = Math.acos(theCos)*180.0/Math.PI;
			if(slope>0) {
				azimuth = theta;
			} else {
				azimuth = 180-theta;
			}
		}
		
		//System.out.println("azimuth="+azimuth+" x1y2="+x1y2);
		//return new double [] {azimuth, centerX, centerY};
		return azimuth;
	}
	
	public double [] calAzimuthFromShots() { 
		double [] x = eE;
		double [] y = eN;
		double minX = ArrayMath.min(x);
		double maxX = ArrayMath.max(x);
		double dx = maxX-minX;
		double meanX = (maxX+minX)/2.0;
		double minY = ArrayMath.min(y);
		double maxY = ArrayMath.max(y);
		double dy = maxY-minY;
		double meanY = (maxY+minY)/2.0;
		
		double azimuth = 0;
		double centerY = 0;
		double centerX = 0;
		boolean x1y2 = dx>=dy;
		if(x1y2) {
			LinearRegression linearRegression = new LinearRegression(x, y);
			//-0.544817 n + 19.802650  (R^2 = 0.969)
			//System.out.println(linearRegression.toString());		

			centerX = meanX;
			centerY = linearRegression.predict(centerX);
			
			double slope = linearRegression.slope();
			double theCos = 1.0/Math.sqrt(1+slope*slope);
			double theta = Math.acos(theCos)*180.0/Math.PI;
			if(slope>0) {
				azimuth = 90-theta;
			} else {
				azimuth = 90+theta;
			}
		} else {
			LinearRegression linearRegression = new LinearRegression(y, x);
			//-0.544817 n + 19.802650  (R^2 = 0.969)
			//System.out.println(linearRegression.toString());		

			centerY = meanY;
			centerX = linearRegression.predict(centerY);
			
			double slope = linearRegression.slope();
			double theCos = 1.0/Math.sqrt(1+slope*slope);
			double theta = Math.acos(theCos)*180.0/Math.PI;
			if(slope>0) {
				azimuth = theta;
			} else {
				azimuth = 180-theta;
			}
		}
		
		//System.out.println("azimuth="+azimuth+" x1y2="+x1y2);
		return new double [] {azimuth, centerX, centerY};
	}

}
