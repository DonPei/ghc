package com.geohammer.dsp;

import java.util.ArrayList;

/** Adapted from reftrg.f from Tom Owens and reftrig.c from Passcal.
 * c
 c     routine to apply the reftek trigger algorithm
 c     to a designated SAC file
 c     LTA is initialized to STA after 2 STA time constants
 c     Trigger detection begins after trgdly seconds
 c
 c     compile with: f77 reftrg.f $SACDIR/lib/sac.a -f68881
 c
 c     Written by T.J. Owens, August 16, 1988
 c
 */
//sta/lta triggering algorithm
//http://www.geopsy.org/download.php
//https://github.com/crotwell/fissuresUtil/tree/src/main/java/edu/sc/seis/fissuresUtil/bag
//http://www.programcreek.com/java-api-examples/index.php?source_dir=sod-master/externalExample/src/edu/sc/seis/sod/example/EventVectorSubsetterExample.java#
//http://gfzpublic.gfz-potsdam.de/pubman/item/escidoc:4097:4/component/escidoc:4098/IS_8.1_rev1.pdf
//google search iris edu.iris.Fissures
//google search sta/lta triggering algorithm java
//recursive STA/LTA
//https://github.com/xumi1993/STALTA/tree/master/example
//Waveform Similarity Analysis
//http://stackoverflow.com/questions/10283224/java-executorservice-thread-synchronization-with-countdownlatch-causes-dead-loc
//http://howtodoinjava.com/java-7/forkjoin-framework-tutorial-forkjoinpool-example/

//http://www.vogella.com/tutorials/JavaConcurrency/article.html

//DIAGRAM:
//                       /\      /\/\        /\
//              /\  /\/\/  \/\  /    \/\    /  \/\
//                \/          \/        \/\/      \                   
//                                           |-STA-| -->
//         --> |-----------------LTA---------------| -->

//https://cran.r-project.org/web/packages/IRISSeismic/IRISSeismic.pdf
//http://wiki.seg.org/wiki/Phase_and_the_Hilbert_transform
//http://cseg.ca/assets/files/resources/abstracts/2012/279_GC2012_Adaptive_Microseismic_Event_Detection.pdf
//
//https://dsp.stackexchange.com/questions/25624/calculate-signal-to-noise-ratio-from-the-hillbert-envelope-of-a-stacked-signal
//https://dsp.stackexchange.com/questions/25624/calculate-signal-to-noise-ratio-from-the-hillbert-envelope-of-a-stacked-signal
	
//https://github.com/agabrown/FractalExplorer

public class StaOverLta {

	private int _nLongTime = 0;
	private int _nShortTime = 0;
	private float _threshold = 0;


	public StaOverLta(int nLongTime, int nShortTime, float threshold) {
		_nLongTime = nLongTime;
		_nShortTime = nShortTime;
		_threshold = threshold;
	}

	//staSecs length of the Short averaging window in secs (default=3)
	//ltaSecs length of the Long averaging windowin secs (default=30)
	//algorithm algorithm to be used (default="classic_LR")
	//demean boolean flag determining whether to demean the data before applying the algorithm
	//(default=TRUE)
	//detrend boolean flag determining whether to detrend the data before applying the algorithm
	//(default=TRUE)
	//taper proportion of the signal to be tapered at each end before applying the algorithm
	//(default=0.0)
	//increment the increment to	     
	//[---------- LTA --------*]
	//                       [*- STA --]
	public ArrayList<Float> calTriggers(float [] seisData) {
		float cSta = 1.0f / _nShortTime;
		float sta = 0;
		float cLta = 1.0f / _nLongTime;
		float lta = 0;
		float ratio = 0;
		
		float maxRatio = 0;
		int maxI = 0;
		
		ArrayList<Float> pickList = null;
		int M = _nShortTime+_nLongTime-1;

		for(int i=0; i<_nLongTime; i++)  lta += (seisData[i]*seisData[i])*cLta ; 
		for(int i=0; i<_nShortTime; i++)  sta += (seisData[i]*seisData[i])*cSta ;
		int ks0 = 0;
		int ks1 = _nShortTime;
		for(int i=_nShortTime; i<M; i++, ks0++, ks1++)  {
			sta += (seisData[ks1]*seisData[ks1]-seisData[ks0]*seisData[ks0])*cSta;
		}
		int kl0 = 0;
		int kl1 = _nLongTime;
		for(int i=M; i<seisData.length; i++, ks0++, ks1++, kl0++, kl1++)  {
			sta += (seisData[ks1]*seisData[ks1]-seisData[ks0]*seisData[ks0])*cSta;
			lta += (seisData[kl1]*seisData[kl1]-seisData[kl0]*seisData[kl0])*cLta;
			ratio = sta/lta;
			if(ratio>_threshold) {
				if(pickList==null) pickList= new ArrayList<Float>();
				pickList.add((float)i);
				pickList.add(ratio);
			}
			if(ratio>maxRatio) {
				maxRatio = ratio;
				maxI = i;
			}
		} 
		
		System.out.println("maxI="+maxI+ " max="+maxRatio);
		
		return pickList;
	}
	
	public ArrayList<Double> calTriggers(double [] seisData) {
		double cSta = 1.0f / _nShortTime;
		double sta = 0;
		double cLta = 1.0f / _nLongTime;
		double lta = 0;
		double ratio = 0;
		double threshold = _threshold;
		
		ArrayList<Double> pickList = null;
		int M = _nShortTime+_nLongTime-1;

		for(int i=0; i<_nLongTime; i++)  lta += (seisData[i]*seisData[i])*cLta ; 
		for(int i=0; i<_nShortTime; i++)  sta += (seisData[i]*seisData[i])*cSta ;
		int ks0 = 0;
		int ks1 = _nShortTime;
		for(int i=_nShortTime; i<M; i++, ks0++, ks1++)  {
			sta += (seisData[ks1]*seisData[ks1]-seisData[ks0]*seisData[ks0])*cSta;
		}
		int kl0 = 0;
		int kl1 = _nLongTime;
		for(int i=M; i<seisData.length; i++, ks0++, ks1++, kl0++, kl1++)  {
			sta += (seisData[ks1]*seisData[ks1]-seisData[ks0]*seisData[ks0])*cSta;
			lta += (seisData[kl1]*seisData[kl1]-seisData[kl0]*seisData[kl0])*cLta;
			ratio = sta/lta;
			if(ratio>threshold) {
				if(pickList==null) pickList= new ArrayList<Double>();
				pickList.add((double)i);
				pickList.add(ratio);
			}
		} 
		
		return pickList;
	}

//	public LongShortTrigger[] calcTriggers(LocalSeismogramImpl seis) throws FissuresException {
//		LinkedList out = new LinkedList();
//		float[] seisData = seis.get_as_floats();
//
//		//   establish number of points in LTA and STA windows as well as in trgdly
//
//		double dt = seis.getSampling().getPeriod().convertTo(UnitImpl.SECOND).get_value();
//		int nlta=(int)(longTime.divideBy(dt).convertTo(UnitImpl.SECOND).getValue()) + 1;
//		int nsta=(int)(shortTime.divideBy(dt).convertTo(UnitImpl.SECOND).getValue()) + 1;
//		int ntdly=(int)(delay.divideBy(dt).convertTo(UnitImpl.SECOND).getValue()) + 1;
//		int nmean=(int)(meanTime.divideBy(dt).convertTo(UnitImpl.SECOND).getValue()) + 1;
//
//		if (seis.getEndTime().subtract(seis.getBeginTime()).lessThan(delay) || nsta > ntdly || ntdly > seis.getNumPoints()) {
//			// seis is too short, so no trigger possible
//			return new LongShortTrigger[0];
//		}
//		/*  get weighting factor  */
//		float csta = 1.0f / nsta ;
//		float clta = 1.0f / nlta ;
//		float cmean = 1.0f / nmean ;
//
//		float mean = 0 ;
//		float mean1 = mean ;
//
//		/* now start calculations for first two windows sta=lta */
//		float sta = 0 ;
//		float lta = 0 ;
//		float trg = 0 ;   /* previous value of trigger */
//		float dat;
//		float ratio;
//		boolean hold = false;
//
//		for(int i=0 ; i < 2*nsta ; i++) {
//			mean = mean + ( seisData[i]-mean)*cmean ;
//			dat = seisData[i] - mean ;
//			mean1 = mean1 + (dat - mean1)*cmean ;
//			dat = dat - mean1 ;
//
//			sta = sta + (Math.abs(dat) - sta)*csta ;
//			lta = lta + (Math.abs(dat) - lta)*csta ;
//			ratio = sta/lta ;
//
//		}
//		long seisStart = 0;
//		double sampling = 0;
//		boolean samplingAndStartSet = false;
//		/*  now get rest of trace */
//		for(int i=2*nsta ; i < seisData.length ; i++) {
//			/* up date mean */
//			mean = mean + ( seisData[i]-mean)*cmean ;
//			dat = seisData[i] - mean ;
//			mean1 = mean1 + (dat - mean1)*cmean ;
//			dat = Math.abs(dat - mean1);
//
//			sta = sta + (dat - sta)*csta ;
//			if( (trg==1) && (hold==true)) {
//				/* do not change lta */
//			} else {
//				lta = lta + (dat - lta)*clta ;
//			}
//			ratio = sta/lta ;
//
//			if (ratio >= threshold) {
//				if(!samplingAndStartSet){
//					seisStart = seis.getBeginTime().getMicroSecondTime();
//					sampling = seis.getSampling().getPeriod().convertTo(UnitImpl.MICROSECOND).get_value();
//					samplingAndStartSet = true;
//				}
//				LongShortTrigger trigger = new LongShortTrigger(seis,
//						i,
//						ratio,
//						sta,
//						lta);
//				out.add(trigger);
//			}
//		}
//		LongShortTrigger[] trigger = (LongShortTrigger[])out.toArray(new LongShortTrigger[0]);
//		return trigger;
//	}
//
//	public LongShortTrigger[] calcTriggersTJO(LocalSeismogramImpl seis) throws FissuresException {
//		LinkedList out = new LinkedList();
//		float[] seisData = seis.get_as_floats();
//
//		//   establish number of points in LTA and STA windows
//		//    as well as in trgdly
//
//		float dt = (float)seis.getSampling().getPeriod().convertTo(UnitImpl.SECOND).get_value();
//		int nlta=(int)(longTime.divideBy(dt).convertTo(UnitImpl.SECOND).getValue()) + 1;
//		int nsta=(int)(shortTime.divideBy(dt).convertTo(UnitImpl.SECOND).getValue()) + 1;
//		int ntdly=(int)(delay.divideBy(dt).convertTo(UnitImpl.SECOND).getValue()) + 1;
//
//		if (seis.getEndTime().subtract(seis.getBeginTime()).lessThan(delay) || nsta > ntdly || ntdly > seis.getNumPoints()) {
//			// seis is too short, so no trigger possible
//			return new LongShortTrigger[0];
//		}
//
//		//  n100 is number of data points in 100 second window
//		//      (needed for data mean calculation)
//
//		int n100=(int)(100./dt) + 1;
//
//		//     clta and csta are constants in trigger algoritms
//
//		float clta=1.0f/nlta;
//		float csta=1.0f/nsta;
//
//		float xmean=0.0f;
//
//		float ylta=0;
//		float prevylta=0;
//		float ysta=0;
//		float prevysta=0;
//
//		// initialize STA, start at delay and sum backwards
//		for (int j = 0; j < nsta && j < ntdly; j++) {
//			ysta += seisData[ntdly-j-1];
//		}
//		// initialize LTA, start at delay and sum backwards
//		for (int j = 0; j < nlta && j < ntdly; j++) {
//			ylta += seisData[ntdly-j-1];
//		}
//		int nmean = 0;
//		for (nmean = 0; nmean < n100 && nmean < ntdly; nmean++) {
//			xmean += seisData[ntdly-nmean-1];
//		}
//
//		//    start the triggering process
//		for (int i = ntdly; i < seisData.length; i++) {
//			//    after 100 seconds, data mean is mean of previous 100 seconds only
//			if (nmean == n100) {
//				xmean -= seisData[i-n100];
//			} else {
//				nmean++;
//			}
//			xmean += seisData[i];
//
//			//    LTA value calculated as per REFTEK algorithm
//			prevylta = ylta;
//			float nextData = Math.abs(seisData[i] - xmean/nmean);
//			ylta = clta*nextData
//					+ (1-clta)*prevylta
//					- (i<nlta?0:clta*Math.abs(seisData[i-nlta] - xmean/nmean));
//			// don't get index of of bounds
//
//			//    STA value calculated as per REFTEK algorithm
//			prevysta = ysta;
//			ysta = csta*nextData
//					+ (1-csta)*prevysta
//					- (i<nsta?0:csta*Math.abs(seisData[i-nsta] - xmean/nmean));
//
//			//   rat is STA/LTA at each time point
//			float ratio;
//			if (ylta != 0) {
//				ratio=ysta/ylta;
//
//			} else {
//				// in this case, declare a trigger if ysta != 0, otherwise not
//				if (ysta != 0) {
//					ratio = threshold;
//				} else {
//					ratio = 0;
//				}
//			}
//			if (ratio >= threshold) {
//				LongShortTrigger trigger = new LongShortTrigger(seis,
//						i,
//						ratio,
//						ysta,
//						ylta);
//				out.add(trigger);
//			}
//		}
//		LongShortTrigger[] trigger = (LongShortTrigger[])out.toArray(new LongShortTrigger[0]);
//		return trigger;
//	}
//
//	protected TimeInterval longTime;
//	protected TimeInterval shortTime;
//	protected TimeInterval delay;
//	protected float threshold;
//	protected TimeInterval meanTime;
//


}
