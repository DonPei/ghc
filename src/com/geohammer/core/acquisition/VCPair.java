package com.geohammer.core.acquisition;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import javax.swing.JOptionPane;

import com.geohammer.core.MSEvent;
import com.geohammer.core.MatrixFileReader;
import com.geohammer.core.ObsWell;
import com.geohammer.core.Sensor;

public class VCPair extends VCPairCore {

	public VCPair(String selectedFile) {
		this(1, selectedFile);
	}
	public VCPair(int dimension, String selectedFile) {
		if(dimension==1) read(selectedFile);
		else if(dimension==3) readVSP(0, selectedFile);
		//		else if(dimension==4) readHeaderDump(selectedFile);
		//		else  readTT(dimension, selectedFile);
	}
	public VCPair(int nEvents, int nReceivers, int distUnit){
		super(nEvents, nReceivers, distUnit);
	}	
	public VCPair(int nEvents){
		super(nEvents, 0, 2);
	}

	public VCPair(double [] sE, double [] sN, double [] sD, double [] rE, double [] rN, double [] rD, int distUnit){
		super(sE, sN, sD, rE, rN, rD, distUnit);
	}
	
	public static VCPair genVCPairFromAzimuth(double [] azimuth, double [] distance, double z0, double z1, int nz, int distUnit){
		VCPair vcPW = new VCPair(distance.length, distance.length*nz, distUnit);
		double dz = (z1-z0)/(nz-1);
		
		double rad = Math.PI/180.0;
		for(int i=0, k=0; i<vcPW.getNEvents(); i++) {
			vcPW.setData(i, vcPW.getFlag(), nz);
			vcPW.setData(i, vcPW.getEN(), 0);
			vcPW.setData(i, vcPW.getEE(), 0);
			vcPW.setData(i, vcPW.getED(), 0);
			double rn = distance[i]*Math.cos(rad*azimuth[i]);
			double re = distance[i]*Math.sin(rad*azimuth[i]);
			for(int j=0; j<nz; j++, k++) {
				vcPW.setData(k, vcPW.getRN(), rn);
				vcPW.setData(k, vcPW.getRE(), re);
				vcPW.setData(k, vcPW.getRD(), z0+j*dz);
			}
		}
		return vcPW;		
	}

	public static VCPair toVCPair(MSEvent [] msEvents) {
		int nEvents = msEvents.length;
		int fp = 0;
		for(int i=0; i<nEvents; i++) fp += msEvents[i].getNumOfReceivers(-1); 

		VCPair vcPW = new VCPair(nEvents, fp, msEvents[0].getUnit());
		for(int ie=0, ir=0; ie<nEvents; ie++) {
			MSEvent msEvent = msEvents[ie];
			double sampleInterval = msEvent.getSampleInterval();
			double [] pPick = msEvent.getPpicks();
			double [] shPick = msEvent.getSHpicks();
			
			vcPW.setData(ie, vcPW.getEN(), msEvent.getX());
			vcPW.setData(ie, vcPW.getEE(), msEvent.getY());
			vcPW.setData(ie, vcPW.getED(), msEvent.getZ());
			
			int k = 0;
			for(int i=0; i<msEvent.getNumOfObsWells(); i++) {
				ObsWell well 	= msEvent.getObsWells(i);
				for(int j=0; j<well.getNumOfSensors(); j++) {
					Sensor 	sensor 	= well.getSensor(j);
					vcPW.setData(ir, vcPW.getRN(), sensor.getX());
					vcPW.setData(ir, vcPW.getRE(), sensor.getY());
					vcPW.setData(ir, vcPW.getRD(), sensor.getZ());

					vcPW.setData(ir, vcPW.getObsPT(), vcPW.INVALID_D);
					vcPW.setData(ir, vcPW.getObsST(), vcPW.INVALID_D);
					vcPW.setData(ir, vcPW.getObsSV(), vcPW.INVALID_D);
					
					if(pPick!=null) { if(pPick[k]>10.0) vcPW.setData(ir, vcPW.getObsPT(), pPick[k]*sampleInterval); }
					if(shPick!=null) { if(shPick[k]>10.0) vcPW.setData(ir, vcPW.getObsST(), shPick[k]*sampleInterval); }
					k++;
					ir++;
				}
			}			
			vcPW.setData(ie, vcPW.getFlag(), k);			
		}

		return vcPW;
	}
	
	public VCPair copy( ) {
		int [] shotIndex = new int[nEvents];
		for(int i=0; i<nEvents; i++) {
			shotIndex[i] = i;
		}
		return extract(shotIndex);
	}
	public VCPair copy(int iShot) { return extract(new int[]{ iShot }); }
	public VCPair extract(int [] shotIndex) {
		int fp = 0;
		for(int i=0; i<shotIndex.length; i++) {
			fp += flag[shotIndex[i]];
		}
		VCPair target = new VCPair(shotIndex.length, fp, iUnit);

		int it = 0;
		int jt = 0;
		for(int is=0; is<shotIndex.length; is++) {
			int iShot = shotIndex[is];
			for(int i=0, k=0; i<nEvents; i++) {
				if(i==iShot) { 
					target.flag[it] 		= flag[i];
					target.eN[it] 			= eN[i];			
					target.eE[it] 			= eE[i];
					target.eD[it] 			= eD[i];
					target.orgT[it] 		= orgT[i];
					target.orgW[it] 		= orgW[i];
					it++;
					for(int j=0; j<flag[i]; j++, k++) {		
						target.rN[jt] 			= rN[k];			
						target.rE[jt] 			= rE[k];
						target.rD[jt] 			= rD[k];			
						target.obsPT[jt] 		= obsPT[k];			
						target.calPT[jt] 		= calPT[k];
						target.obsST[jt] 		= obsST[k];			
						target.calST[jt] 		= calST[k];
						target.obsSV[jt] 		= obsSV[k];			
						target.calSV[jt] 		= calSV[k];
						jt++;
					}
				} else { k += flag[i]; }
			}
		}
		return target;
	}

	public VCPair append(VCPair other) {
		int mEvents = this.nEvents+ other.nEvents;
		int nReceivers = getTotalNumOfReceivers()+other.getTotalNumOfReceivers();
		VCPair target = new VCPair(mEvents, nReceivers, iUnit);

		int it = 0;
		int jt = 0;
		for(int i=0, k=0; i<nEvents; i++) {
			target.flag[it] 		= flag[i];
			target.eN[it] 			= eN[i];			
			target.eE[it] 			= eE[i];
			target.eD[it] 			= eD[i];
			target.orgT[it] 		= orgT[i];
			target.orgW[it] 		= orgW[i];
			it++;
			for(int j=0; j<flag[i]; j++, k++) {		
				target.rN[jt] 			= rN[k];			
				target.rE[jt] 			= rE[k];
				target.rD[jt] 			= rD[k];			
				target.obsPT[jt] 		= obsPT[k];			
				target.calPT[jt] 		= calPT[k];
				target.obsST[jt] 		= obsST[k];			
				target.calST[jt] 		= calST[k];
				target.obsSV[jt] 		= obsSV[k];			
				target.calSV[jt] 		= calSV[k];
				jt++;
			}
		}
		
		for(int i=0, k=0; i<other.nEvents; i++) {
			target.flag[it] 		= other.flag[i];
			target.eN[it] 			= other.eN[i];			
			target.eE[it] 			= other.eE[i];
			target.eD[it] 			= other.eD[i];
			target.orgT[it] 		= other.orgT[i];
			target.orgW[it] 		= other.orgW[i];
			it++;
			for(int j=0; j<other.flag[i]; j++, k++) {		
				target.rN[jt] 			= other.rN[k];			
				target.rE[jt] 			= other.rE[k];
				target.rD[jt] 			= other.rD[k];			
				target.obsPT[jt] 		= other.obsPT[k];			
				target.calPT[jt] 		= other.calPT[k];
				target.obsST[jt] 		= other.obsST[k];			
				target.calST[jt] 		= other.calST[k];
				target.obsSV[jt] 		= other.obsSV[k];			
				target.calSV[jt] 		= other.calSV[k];
				jt++;
			}
		}
		return target;
	}
	public int [] calReceiverIndexOfEachObsWell() {
		double a = 0.0;
		double b = 2.0*receiverSpacing(0, 1);
		int n = 1;
		for(int j=1; j<flag[0]-1; j++) {		
			a = receiverSpacing(j, j+1);
			if(a>b) n++;
		}
		int [] obsIndex = new int[n];
		obsIndex[0] = 0;
		n = 1;
		for(int j=1; j<flag[0]-1; j++) {		
			a = receiverSpacing(j, j+1);
			if(a>b) { obsIndex[n] = j+1; n++; }
		}
		return obsIndex;
	}

	public int getNumOfWells() {
		double a = 0.0;
		double threshold = 2.0*receiverSurfaceSpacing(0, 1);
		int nWells = 1;
		for(int j=1; j<flag[0]-1; j++) {		
			a = receiverSurfaceSpacing(j, j+1);
			if(a>threshold) {
				nWells++;
				//threshold = 2.0*a;
			}
		}
		return nWells;
	}
	public VCPair trim(int id) { return trimEvent().trimReceiver(id); }
	public VCPair trimEvent() { return trimEvent(selectValidEvents()); }
	public VCPair trimEvent(int [] selected) {
		if(selected==null) return copy();
		int mEvents = 0;
		int fp = 0;
		for(int i=0; i<nEvents; i++) { if(selected[i]==1) { mEvents++; fp+=flag[i]; } }

		VCPair target = new VCPair(mEvents, fp, iUnit);

		int it = 0;
		int jt = 0;
		for(int i=0, k=0; i<nEvents; i++) {
			if(selected[i]==1) { 
				target.flag[it] 		= flag[i];
				target.eN[it] 			= eN[i];			
				target.eE[it] 			= eE[i];
				target.eD[it] 			= eD[i];
				target.orgT[it] 		= orgT[i];
				target.orgW[it] 		= orgW[i];
				it++;
				for(int j=0; j<flag[i]; j++, k++) {		
					target.rN[jt] 			= rN[k];			
					target.rE[jt] 			= rE[k];
					target.rD[jt] 			= rD[k];			
					target.obsPT[jt] 		= obsPT[k];			
					target.calPT[jt] 		= calPT[k];
					target.obsST[jt] 		= obsST[k];			
					target.calST[jt] 		= calST[k];
					target.obsSV[jt] 		= obsSV[k];			
					target.calSV[jt] 		= calSV[k];
					jt++;
				}
			} else {
				k+=flag[i];
			}
		}

		return target;
	}
	public VCPair trimReceiver(int id) { return trimReceiver(selectValidReceivers(id)); }
	public VCPair trimReceiver(int iType, int [] selected0) {
		if(selected0==null) return copy();
		int [] selected = null;

		//within one event
		if(iType==0) {
			selected = new int[getTotalNumOfReceivers()];
			for(int i=0, k=0; i<nEvents; i++) { 
				for(int j=0; j<flag[i]; j++, k++) {
					selected[k] = selected0[j]; 
				}
			}
		} else {
			selected = selected0;
		}
		return trimReceiver(selected);
	}
	public VCPair trimReceiver(int [] selected) {
		if(selected==null) return copy();
		int fp = 0;
		for(int i=0, k=0; i<nEvents; i++) { 
			for(int j=0; j<flag[i]; j++, k++) {
				if(selected[k]==1) { fp++; } 
			}
		}

		VCPair target = new VCPair(nEvents, fp, iUnit);

		int it = 0;
		int jt = 0;
		for(int i=0, k=0; i<nEvents; i++) {
			target.flag[it] 		= flag[i];
			target.eN[it] 			= eN[i];			
			target.eE[it] 			= eE[i];
			target.eD[it] 			= eD[i];
			target.orgT[it] 		= orgT[i];
			target.orgW[it] 		= orgW[i];
			it++;
			for(int j=0; j<flag[i]; j++, k++) {	
				if(selected[k]==1) {
					target.rN[jt] 			= rN[k];			
					target.rE[jt] 			= rE[k];
					target.rD[jt] 			= rD[k];			
					target.obsPT[jt] 		= obsPT[k];			
					target.calPT[jt] 		= calPT[k];
					target.obsST[jt] 		= obsST[k];			
					target.calST[jt] 		= calST[k];
					target.obsSV[jt] 		= obsSV[k];			
					target.calSV[jt] 		= calSV[k];
					jt++;
				}
			}
		}

		return target;
	}
	
	public void trimAz() {
		for(int i=0, k=0; i<nEvents; i++) {
			for(int j=0; j<flag[i]; j++, k++) {	
				if(obsSV[k]<0.0) obsSV[k] = INVALID_D;
				if(obsSV[k]==90.0) obsSV[k] = INVALID_D;
				else if(obsPT[k]==INVALID_D&&obsST[k]==INVALID_D) obsSV[k] = INVALID_D;
			}
		}
		for(int i=0, k=0; i<nEvents; i++) {
			double sum = 0.0;
			int m = 0;
			for(int j=0; j<flag[i]; j++, k++) {	
				if(obsSV[k]!=INVALID_D) { sum += obsSV[k]; m++;}
			}
			if(m>0) orgT[i] = (sum/m)*3.1415926/180.0;
			else orgT[i] = INVALID_D;
		}
	}

	public int getMaxNumReceiverPerShot() {
		int iMax = 0;
		for(int i=0; i<nEvents; i++) {
			iMax = iMax > flag[i] ? iMax:flag[i];
		}
		return iMax;
	}

	public void read(String selectedFileName) {
		//System.out.println("selectedFileName="+selectedFileName);
		MatrixFileReader reader = new MatrixFileReader();
		reader.readTextualData(selectedFileName, 0, 1, 0, null, ",");
		//reader.printClass();

		int nRow = reader.getRowCount();
		int nCol = reader.getColumnCount();

		int k = 0;
		int ia = 0;
		int iReceiver = 0;
		for(int i=0; i<nRow; i++) {
			ia = reader.getDataInt(i, nCol-1);
			if(ia>1000000) {
				k++;
				iReceiver = 0;
			} else {
				iReceiver++;
			}
		}
		nEvents 	= k;
		allocate(nEvents, nRow-nEvents);
		iUnit = reader.getDataInt(0, 5);
		k = 0;
		for(int i=0; i<nRow; i++) {
			ia = reader.getDataInt(i, nCol-1);
			if(ia>1000000) {
				eN[k] = reader.getDataDouble(i, 0);
				eE[k] = reader.getDataDouble(i, 1);
				eD[k] = reader.getDataDouble(i, 2);
				orgT[k] = reader.getDataDouble(i, 3);
				orgW[k] = reader.getDataInt(i, 4);
				flag[k] = iReceiver; //get nReceiver from last event
				k++;
				iReceiver = 0;
			} else {
				iReceiver++;
			}
		}
		//adjust _flag
		ia = flag[0];
		for(int i=0; i<eN.length-1; i++) { flag[i] = flag[i+1]; }
		flag[eN.length-1] = ia;
		
		k = 0;
		for(int i=0; i<nRow; i++) {
			ia = reader.getDataInt(i, nCol-1);
			if(ia>1000000) {
			} else {
				rN[k] = reader.getDataDouble(i, 0);
				rE[k] = reader.getDataDouble(i, 1);
				rD[k] = reader.getDataDouble(i, 2);
				obsPT[k] = reader.getDataDouble(i, 3);
				obsST[k] = reader.getDataDouble(i, 4);
				obsSV[k] = reader.getDataDouble(i, 5);
				calPT[k] = reader.getDataDouble(i, 6);
				calST[k] = reader.getDataDouble(i, 7);
				calSV[k] = reader.getDataDouble(i, 8);
				k++;
			}
		}
	}
	
	public void write(boolean eventOnly, String outputFileName) {
		try{
			String a = null;
			if(iUnit==1) {
				a = new String("Northing(ft), Easting(ft), Down(ft)");
			} else if(iUnit==2) {
				a = new String("Northing(m), Easting(m), Down(m)");
			} else {
				a = new String("Northing(km), Easting(km), Down(km)");
			}
			a += new String(", OrgT/Obs_PT(s), OrgW/Obs_ST(s), Unit/Obs_SV(s), Cal_PT(s), Cal_ST(s), Cal_SV(s), Item_No/Phase");
			
			BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(outputFileName, false));
			bufferedWriter.write(a+"\n");
			for(int i=0, k=0; i<nEvents; i++) {
				a = String.format("%.15f, %.15f, %.15f, %.15f, %d, %d, 0, 0, 0, %d", 
						eN[i], eE[i], eD[i], orgT[i], orgW[i], iUnit, 1000001+i); 
				bufferedWriter.write(a+"\n");

				if(eventOnly) continue;
				
				for(int j=0; j<flag[i]; j++, k++) {
					a = String.format("%.15f, %.15f, %.15f, %.15f, %.15f, %.15f, %.15f, %.15f, %.15f, %d", 
							rN[k], rE[k], rD[k], obsPT[k], obsST[k], obsSV[k], calPT[k], calST[k], calSV[k], k); 
					bufferedWriter.write(a+"\n");
				}
			}
			bufferedWriter.close();
		} catch (IOException ioexception) {
			JOptionPane.showMessageDialog(null, "\nFile Format is not right!", 
					"Error", JOptionPane.ERROR_MESSAGE);
		}
	}

	public void readVSP(int iFormat, String selectedFileName) {
		//System.out.println("selectedFileName="+selectedFileName);
		MatrixFileReader reader = new MatrixFileReader();
		reader.readTextualData(selectedFileName, 0, 1, 0, null, ",");
		//reader.printClass();

		int nRow = reader.getRowCount();
		int nCol = reader.getColumnCount();

		int k = 1;
		int ia = reader.getDataInt(0, 0);
		int ib = ia;
		for(int i=1; i<nRow; i++) {
			ib = reader.getDataInt(i, 0);
			if(ib!=ia) {
				k++;
				ia = ib;
			}
		}
		nEvents = k;
		allocate(nEvents, nRow);
		iUnit = 1;
		
		k = 0;
		eN[k] = reader.getDataDouble(0, 9);
		eE[k] = reader.getDataDouble(0, 8);
		eD[k] = 0;
		rN[k] = reader.getDataDouble(0, 7);
		rE[k] = reader.getDataDouble(0, 6);
		rD[k] = reader.getDataDouble(0, 5);
		obsPT[k] = 0.001*reader.getDataDouble(0, 10);
		obsST[k] = 0;
		obsSV[k] = 0;
		calPT[k] = 0;
		calST[k] = 0;
		calSV[k] = 0;
		k = 1;
		ia = reader.getDataInt(0, 0);
		ib = ia;
		int nPicks = 1;
		for(int i=1; i<nRow; i++) {
			ib = reader.getDataInt(i, 0);
			if(ib!=ia) {
				eN[k] = reader.getDataDouble(i, 9);
				eE[k] = reader.getDataDouble(i, 8);
				eD[k] = 0;
				flag[k-1] = nPicks;
				k++;
				ia = ib;
				nPicks = 0;
			}
			nPicks++;
			
			rN[i] = reader.getDataDouble(i, 7);
			rE[i] = reader.getDataDouble(i, 6);
			rD[i] = reader.getDataDouble(i, 5);
			obsPT[i] = 0.001*reader.getDataDouble(i, 10);
			obsST[i] = 0;
			obsSV[i] = 0;
			calPT[i] = 0;
			calST[i] = 0;
			calSV[i] = 0;			
		}
		flag[k-1] = nPicks;		
	}
	public String toString()		{ return toString(false); }
	public String toString(boolean eventOnly){
		String a = null;
		String b = null;
		if(iUnit==1) {
			b = new String("Northing(ft), Easting(ft), Down(ft)");
		} else if(iUnit==2) {
			b = new String("Northing(m), Easting(m), Down(m)");
		} else {
			b = new String("Northing(km), Easting(km), Down(km)");
		}
		b += new String(", OrgT/Obs_PT(s), OrgW/Obs_ST(s), Unit/Obs_SV(s), Cal_PT(s), Cal_ST(s), Cal_SV(s), Item_No/Phase\n");

		for(int i=0, k=0; i<nEvents; i++) {
		//for(int i=0, k=0; i<1; i++) {
			a = String.format("%f, %f, %f, %f, %d, %d, 0, 0, 0, %d", 
					eN[i], eE[i], eD[i], orgT[i], orgW[i], iUnit, 1000001+i); 
			b = new String(b.concat(a+"\n"));

			if(eventOnly) continue;
			
			for(int j=0; j<flag[i]; j++, k++) {
				a = String.format("%f, %f, %f, %f, %f, %f, %f, %f, %f, %d", 
						rN[k], rE[k], rD[k], obsPT[k], obsST[k], obsSV[k], calPT[k], calST[k], calSV[k], k); 
				b = new String(b.concat(a+"\n"));
			}
		}
		return b;
	}
	
	public void applyT0(int PP, int SH, int SV) { 
		if(PP==1) applyT0(1);
		if(SH==1) applyT0(2);
		if(SV==1) applyT0(3);
	}
	public void applyT0(int iVp) {
		double [] cal = getCalT(iVp);
		for(int i=0, k=0; i<nEvents; i++) {
			for(int j=0; j<flag[i]; j++, k++) { cal[k] += orgT[i]; }
		}
	}
	public void estimateT0(int PP, int SH, int SV) { 
		double [][] diff = new double[6][nEvents];
		for(int i=0; i<6; i++){
			for(int j=0; j<nEvents; j++) { diff[i][j] = 0; }
		}

		double [] tSum = new double[nEvents];
		double [] NT = new double[nEvents];
		
		if(PP==1) {
			estimateT0(1, tSum, NT);
			for(int i=0; i<nEvents; i++) { diff[0][i] = tSum[i]; diff[1][i] = NT[i]; }
		}
		if(SH==1) {
			estimateT0(2, tSum, NT);
			for(int i=0; i<nEvents; i++) { diff[2][i] = tSum[i]; diff[3][i] = NT[i]; }
		}
		if(SV==1) {
			estimateT0(3, tSum, NT);
			for(int i=0; i<nEvents; i++) { diff[4][i] = tSum[i]; diff[5][i] = NT[i]; }
		}

		for(int i=0; i<nEvents; i++) {
			double sum = diff[0][i]+diff[2][i]+diff[4][i];
			double m = diff[1][i]+diff[3][i]+diff[5][i];
			if(orgW[i]>0) {
				if(m>0)  { orgT[i]=(sum)/(m);  }
				else  { orgT[i]=0.0;   }
			}
			//System.out.println("orgT="+_orgT[i]+" sum="+sum+" m="+m+" _orgW="+_orgW[i]+" _orgT[i]="+_orgT[i]);
		}
	}
	public void estimateT0(int iVp) { 
		double [] tSum = new double[nEvents];
		double [] NT = new double[nEvents];
		
		estimateT0(iVp, tSum, NT);

		for(int i=0; i<nEvents; i++) {
			if(orgW[i]>0) {
				if(NT[i]>0)  { orgT[i]=(tSum[i])/(NT[i]);  }
				else  { orgT[i]=0.0;   }
			}
		}
	}
	
	public void estimateT0(int iVp, double [] tSum, double [] NT) { 
		double [] r 	= new double[2];
		double [] diff 	= new double[getMaxNumReceiverPerShot()];
		for(int i=0; i<nEvents; i++) {
			getRMS(iVp, i, r, diff);
			int m 		= 0;
			double sum 	= 0.0;
			for(int j=0; j<diff.length; j++) {
				if(diff[j] != INVALID_D) sum += diff[j];
			}
			m = (int)r[0];
			tSum[i] = sum;
			NT[i] = m;

			//if(iVp==1) System.out.println("i="+i+" m="+m+" sum="+sum+" tSum[i]="+tSum[i]);
		}
	}
	public double getRMS(int PP, int SH, int SV) { 
		double [][] rs = new double[3][2];
		for(int i=0; i<3; i++) { for(int j=0; j<2; j++) rs[i][j] = 0; }
		double rms1 = 0.0;
		double rms2 = 0.0;
		double rms3 = 0.0;
		
		if(PP==1) rms1 = getRMS(1, rs[0]);
		if(SH==1) rms2 = getRMS(2, rs[1]);
		if(SV==1) rms3 = getRMS(3, rs[2]);
		
		int n = (int)(rs[0][0]+rs[1][0]+rs[2][0]);
		double sum = rs[0][1]+rs[1][1]+rs[2][1];
		if(n==0) { return 99999.0; }
		else return Math.sqrt(sum/n);
	}

	public double getRMS(int iVp, int iShot, double [] r) 	{ return getRMS(iVp, iShot, r, null); }
	public double getRMS(int iVp, double [] r) 				{ return getRMS(iVp, -1, r, null); }
	public double getRMS(int iVp) 							{ return getRMS(iVp, -1); }
	public double getRMS(int iVp, int iShot) 				{ double [] r = new double[2]; return getRMS(iVp, iShot, r, null); }
	public double getRMS(int iVp, int iShot, double [] r, double [] diff) {
		double [] obs = getObsT(iVp);
		double [] cal = getCalT(iVp);
		if(diff!=null) { for(int i=0; i<diff.length; i++) diff[i] = INVALID_D; }

		double a = 0.0;
		double sum = 0.0;
		int n = 0;
		for(int i=0, k=0; i<nEvents; i++) {
			if(iShot<0) { }
			else if(iShot!=i) { k+=flag[i]; continue; }
			else { }
			for(int j=0; j<flag[i]; j++, k++) {	
				if(obs[k]<=0.0 || obs[k]>=10.0) continue;
				if(cal[k]<=0.0 || cal[k]>=10.0) continue;
				a = obs[k] - cal[k];
				if(a>-10.0 && a<10.0) {
					sum += a*a;
					n++;
					if(diff!=null) diff[j] = a;
				}
			}
		}
		if(r!=null) { r[0] = n; r[1] = sum; }
		if(n==0) {
//			System.out.println(toString());
//			System.out.println("iVP="+iVp+" "+Arrays.toString(obs));
//			System.out.println(Arrays.toString(cal));
//			System.out.println(Arrays.toString(_calPT));
//			System.exit(1);
			return 99999.0;
		}
		else return Math.sqrt(sum/n);
	}

	
	public double [] getTimeDiff(int iType, int iShot) {
		double [] D1 = getTimeDiff(iType, 1, iShot);
		double [] D2 = getTimeDiff(iType, 2, iShot);
		double [] D3 = getTimeDiff(iType, 3, iShot);
		double [] D = catVector(D1, D2);
		return catVector(D, D3);
	}
	public double [] getTimeDiff(int iType, int iVp, int iShot) {
		double [] obsT 	= getObsT(iVp);
		double [] calT 	= getCalT(iVp);
		if(iType==0) return obsT;
		if(iType==1) return calT;

		double [] diff = null;
		if(iShot==-1) {
			diff = new double[obsT.length];
			for(int j=0; j<diff.length; j++) {
				diff[j] = obsT[j]-calT[j]; 
			}
		} else {
			diff = new double[obsT.length];
			for(int i=0, k=0; i<nEvents; i++) {
				if(iShot==i) { 
					for(int j=0; j<flag[i]; j++, k++) {
						diff[j] = obsT[k]-calT[k]; 
					}
				} else { k+=flag[i]; }
			}
		}
		return diff;
	}

	public double getAzimuth(int iShot) 		{ 
		double [] sum = new double [] {0, 0, 0, 0};
		int [] n = new int [] {0, 0, 0, 0};
		for(int i=0, k=0; i<nEvents; i++) {
			if(iShot<0) { }
			else if(iShot!=i) { k+=flag[i]; continue; }
			else { }
			sum[0] += getEE(i); n[0]++;
			sum[1] += getEN(i); n[1]++;
			for(int j=0; j<flag[i]; j++, k++) {
				sum[2] += getRE(k); n[2]++;
				sum[3] += getRN(k); n[3]++;
			}
		}
		
		if(n[0]==0) return 0.0;
		for(int i=0; i<sum.length; i++) { sum[i] /= n[i]; }
		
		double pe = sum[2] - sum[0];
		double pn = sum[3] - sum[1];
		double len = Math.sqrt(pe*pe+pn*pn);
		double a = Math.acos(pn/len);
		if(pe>0) return a;
		else  return 360-a;
	}
	
	public double getAzimuth(int iShot, int iReceiver) 		{ 
		double [] sum = new double [] {0, 0, 0, 0};
		int [] n = new int [] {0, 0, 0, 0};
		for(int i=0, k=0; i<nEvents; i++) {
			if(iShot<0) { }
			else if(iShot!=i) { k+=flag[i]; continue; }
			else { }
			sum[0] += getEE(i); n[0]++;
			sum[1] += getEN(i); n[1]++;
			for(int j=0; j<flag[i]; j++, k++) {
				if(j==iReceiver) {
					sum[2] += getRE(k); n[2]++;
					sum[3] += getRN(k); n[3]++;
				}
			}
		}
		
		if(n[0]==0) return 0.0;
		for(int i=0; i<sum.length; i++) { sum[i] /= n[i]; }
		
		double pe = sum[2] - sum[0];
		double pn = sum[3] - sum[1];
		double len = Math.sqrt(pe*pe+pn*pn);
		double a = Math.acos(pn/len);
		if(pe>0) return a;
		else  return 360-a;
	}
}


