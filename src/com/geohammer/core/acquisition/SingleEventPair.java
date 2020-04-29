package com.geohammer.core.acquisition;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SingleEventPair {
	public int isValid[];		// 1 - good, 0 - no.

	public double eN;			// source coordinates in positive north direction
	public double eE;			// source coordinates in positive east direction
	public double eD;			// source coordinates in positive down direction
	public double rN[];		// receiver coordinates in positive north direction
	public double rE[];		// receiver coordinates in positive east direction
	public double rD[];		// receiver coordinates in positive down direction
	
	public double obsPT[];		// observed P-wave time
	public double calPT[];		// calculated P-wave time
	public double orTPT;		// P-wave event origin time
	public double obsST[];		// observed S-wave time
	public double calST[];		// calculated S-wave time
	public double orTST;		// S-wave event origin time
	
	public int 		FFID;    	// _FFID for shot
	public double 	MD;    	// _MD for receiver
	
}
