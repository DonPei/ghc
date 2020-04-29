package com.geohammer.vc.run;

import com.geohammer.core.acquisition.VCPair;
import com.geohammer.core.planarmodel.DipLayer1D;

import edu.mines.jtk.util.ArrayMath;

public class EffectiveVelocityToken {
	
	
	public static DipLayer1D genEffectiveVelocity(DipLayer1D dipLayer1D, VCPair vcPW) {
		if(dipLayer1D.getNumOfBoundaries()==2) return dipLayer1D.copy();
		DipLayer1D effectiveVelocity = genEffectiveVelocity(dipLayer1D);
		//System.out.println(effectiveVelocity.toString());
		
		EffectiveVelocityAlone effectiveVelocityAlone = new EffectiveVelocityAlone(0, 4, effectiveVelocity, vcPW);
		effectiveVelocityAlone.start();
		//System.out.println(effectiveVelocity.toString());

		//System.out.println("minRms="+effectiveVelocityAlone.getMinRms());
		return effectiveVelocity;
	}
	
	public static DipLayer1D genEffectiveVelocity(DipLayer1D dipLayer1D) {
		double dip = 0.0; //_dip negtative up 
		double strike = 0.0;

		int n = dipLayer1D.getNumOfBoundaries()-1;
		double [] top = new double[]{dipLayer1D.getLayer(0).getTopDepth(), dipLayer1D.getLayer(n).getBottomDepth(), 
				dipLayer1D.getLayer(n).getBottomDepth(), dipLayer1D.getLayer(n).getBottomDepth() };

		double [] vp = dipLayer1D.getLayerProperty(1);
		double [] vs = dipLayer1D.getLayerProperty(2);
		double [] epsilon = dipLayer1D.getLayerProperty(20);
		double [] delta = dipLayer1D.getLayerProperty(21);
		double [] gamma = dipLayer1D.getLayerProperty(22);
		for(int i=0; i<vp.length; i++) {
			vp[i] *= (1.0+epsilon[i]);
			vs[i] *= (1.0+gamma[i]);
		}
		double vpAve = ArrayMath.sum(vp)/vp.length;
		double vsAve = ArrayMath.sum(vs)/vs.length;
		double deltaAve = ArrayMath.sum(delta)/delta.length;
		double epsilonAve = ArrayMath.sum(epsilon)/epsilon.length;
		double gammaAve = ArrayMath.sum(gamma)/gamma.length;

		double a = 1.2;
		double b = 0.8;
		double vpMax = a*ArrayMath.max(vp);
		double vpMin = b*ArrayMath.min(vp);
		double vsMax = a*ArrayMath.max(vs);
		double vsMin = b*ArrayMath.min(vs);
		double deltaMax = a*ArrayMath.max(delta);
		double deltaMin = b*ArrayMath.min(delta);
		if(deltaMin==0.0&&deltaMax==0.0) {
			deltaMax = 0.3;
			deltaMin = -0.3;
		}
		double epsilonMax = a*ArrayMath.max(epsilon);
		double epsilonMin = b*ArrayMath.min(epsilon);
		if(epsilonMin==0.0&&epsilonMax==0.0) {
			epsilonMax = 0.4;
			epsilonMin = -0.1;
		}
		double gammaMax = a*ArrayMath.max(gamma);
		double gammaMin = b*ArrayMath.min(gamma);
		if(gammaMin==0.0&&gammaMax==0.0) {
			gammaMax = 0.4;
			gammaMin = -0.1;
		}
		int nx = 50;
		double vpD = (vpMax-vpMin)/nx;
		double vsD = (vsMax-vsMin)/nx;
		double deltaD = (deltaMax-deltaMin)/nx;
		double epsilonD = (epsilonMax-epsilonMin)/nx;
		double gammaD = (gammaMax-gammaMin)/nx;
		
		DipLayer1D effectiveVelocity = new DipLayer1D(top, new double [] {vpAve, vpMin, vpMax, vpD}, 
				new double [] {vsAve, vsMin, vsMax, vsD}, new double [] {2.0, 2.0, 2.0, 2.0}, 
				new double [] {deltaAve, deltaMin, deltaMax, deltaD}, 
				new double [] {epsilonAve, epsilonMin, epsilonMax, epsilonD}, 
				new double [] {gammaAve, gammaMin, gammaMax, gammaD}, null, null, null, null, strike, dip);


		for(int i=0; i<effectiveVelocity.getNumOfBoundaries(); i++) {
			effectiveVelocity.getLayer(i).setSize(2, 2, 2, 1, 1, 1, 0, 0, 0);
			effectiveVelocity.getLayer(i).setLayerName("layer_"+i);
		}

		return effectiveVelocity;
	}
}
