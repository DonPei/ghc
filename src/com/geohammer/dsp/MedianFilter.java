package com.geohammer.dsp;

import java.util.Arrays;

public class MedianFilter {
	private float [][] 		_data2D 		= null;
	private float [] 		_data 			= null;
	private float [] 		_buffer			= null;
	private int [] 			_sortedIndex	= null;
	private int  			_winLen			= 5;
	
	public MedianFilter(int winLen) {
		_winLen		= winLen;
		_buffer 	= new float[winLen];
		_sortedIndex = new int[winLen];
	}
	public void apply(float [] data) {
		_data2D		= null;
		_data		= data;
		process();
	}
	public void apply(float [][] data2D) {
		_data2D		= data2D;
		_data		= data2D[0];
		process();
	}
	
	public void process() {		
		int midIndex = _winLen/2;
		int N = 1;
		if(_data2D!=null) N = _data2D.length;
		
		for(int k=0; k<N; k++) {
			if(N>1) _data = _data2D[k];
			for(int j=0; j<midIndex; j++) _buffer[j] = _data[0];
			for(int j=midIndex; j<_winLen; j++) _buffer[j] = _data[j-midIndex];
			sortIndex();
			_data[0] = _buffer[_sortedIndex[midIndex]];
			for(int i=1; i<_data.length; i++) {
				for(int j=1; j<_winLen; j++) _buffer[j-1] = _buffer[j];
				if(i>=_data.length-midIndex) 	_buffer[_winLen-1] = _data[_data.length-1];	
				else 							_buffer[_winLen-1] = _data[i+midIndex];		
				sortIndex();
				_data[i] = _buffer[_sortedIndex[midIndex]];
			}
		}
	}
	
	public void process1() {
		System.out.println("index0: " + Arrays.toString(_data));
		float [] data = new float[_data.length];
		
		int midIndex = _winLen/2;
		for(int j=0; j<midIndex; j++) _buffer[j] = _data[0];
		for(int j=midIndex; j<_winLen; j++) _buffer[j] = _data[j-midIndex];
		sortIndex();
		data[0] = _buffer[_sortedIndex[midIndex]];
		for(int i=1; i<_data.length; i++) {
			for(int j=1; j<_winLen; j++) _buffer[j-1] = _buffer[j];
			if(i>=_data.length-midIndex) 	_buffer[_winLen-1] = _data[_data.length-1];	
			else 							_buffer[_winLen-1] = _data[i+midIndex];	
			sortIndex();
			data[i] = _buffer[_sortedIndex[midIndex]];
		}
		System.out.println("index1: " + Arrays.toString(data));		
	}
	
	public void sortIndex() {
		int J, I,NEXT;
		int N = _winLen;
		for (I = 0; I < N; I++)   _sortedIndex[I] = I; 

		//  for (J=0; J<(N-2); J++)
		for (J=0; J<(N-1); J++) {
			NEXT = _sortedIndex[J + 1];

			for (I=J ; I>=0; I--) {
				if ( _buffer[NEXT] >= _buffer[_sortedIndex[I]] ) break;
				_sortedIndex[I + 1] = _sortedIndex[I];
			} // 20
			_sortedIndex[I + 1] = NEXT;
		} // 40
	}
	
	/**
	 * Sort routine based on ISORT.FOR from the CUSP library which was based
	 * on method of MEISSNER & ORGANICK, P.352 
	 * Returns array of indexes sorted in ascending order leaving the 
	 * original array untouched.
	 */
	public int[] getSortedIndexes (double[] RX) {
		int J, I,NEXT;
		int N = RX.length;

		int IX[] = new int[RX.length];

		//  INDIRECT SORT ROUTINE FROM MEISSNER & ORGANICK, P.352
		//  STORES ASCENDING SORT ORDER OF RX IN ARRAY IX, LEAVING RX UNCHANGED
		// starting array
		for (I = 0; I < N; I++)   IX[I] = I; 

		//  for (J=0; J<(N-2); J++)
		for (J=0; J<(N-1); J++) {
			NEXT = IX[J + 1];

			for (I=J ; I>=0; I--) {
				if ( RX[NEXT] >= RX[IX[I]] ) break;
				IX[I + 1] = IX[I];
			} // 20
			IX[I + 1] = NEXT;
		} // 40

		return (IX);
	}
	
	public int[] getSortedIndexes (int[] RX)
	{
		int J, I, NEXT;
		int N = RX.length;

		int IX[] = new int[RX.length];

		for (I = 0; I < N; I++) IX[I] = I; 
		for (J=0; J<(N-1); J++) {
			NEXT = IX[J + 1];

			for (I=J ; I>=0; I--) {
				if ( RX[NEXT] >= RX[IX[I]] ) break;
				IX[I + 1] = IX[I];
			}  
			IX[I + 1] = NEXT; 
		} 

		return IX;
	}
	
	public static final void main(String [] args) {
        float xtest [] = {13, 2, 12, 4, 11, 6, 10, 8, 9, 7, 5, 3, 1, 0, 14};
//        double equalWts [] =   {.5,.5,.5,.5,.5,.5,.5,.5,.5, .5, .5, .5, .5,.5,.5};
//        double biasedHiWts [] =  {1.,.1,1.,.1,1.,.1,.5,.2,.4, .3, .1, .1, .1,.2,.8};
//        double biasedLoWts [] =  {.1,1.,.1,.8,.1,.6,.1,.2,.1, .2, .3, 1., 1.,.8,.2};
//        double median = calcMedian(xtest.length, xtest, equalWts);
//        System.out.println("equal wts median: " + median);
//        median = calcMedian(xtest.length, xtest, biasedHiWts);
//        System.out.println("biased high wts median: " + median);
//        median = calcMedian(xtest.length, xtest, biasedLoWts);
//        System.out.println("biased low wts median: " + median);
        
        MedianFilter medianFilter = new MedianFilter(5);
        //medianFilter.process1();
        medianFilter.apply(xtest);
    }
	
	public static final double calcMedian(int nValues, double [] values, double [] weights) {
        if (nValues == 0) {
                return 0.;
        } else if (nValues == 1) {
                return values[0];
        }

        double sumWts = 0.;
        for (int idx = 0 ; idx < nValues; idx++) {
            if (weights[idx] >= 0.0)  sumWts += weights[idx];
        }
        if (sumWts <= 0.0) return 0.; // NEGATIVE WEIGHTS ARE TREATED AS ZERO WEIGHTS   

        double sumWts2 = sumWts/2.;
        double averageWts  = sumWts/nValues;   // MAKES WT MEDIAN CORRESPOND TO THAT EXPECTED WITH EQUAL WEIGHTS
        sumWts2 = sumWts2 + averageWts/2.;
                                      
        sumWts = 0.0;
        double medianValue = 0.;

        int [] orderedIndex = IndexSort.getSortedIndexes(values); // tagsort(nValues, values);
        for (int idx = 0; idx < nValues; idx++) {
             if (weights[orderedIndex[idx]] >= 0.) sumWts += weights[orderedIndex[idx]];
             if (sumWts >= sumWts2)  { 
                 double valueDifference = values[orderedIndex[idx]] - values[orderedIndex[idx-1]];
                 double ratio = (sumWts2 - sumWts + weights[orderedIndex[idx]])/weights[orderedIndex[idx]];
                 medianValue = values[orderedIndex[idx-1]] + valueDifference*ratio;
                 //System.out.println("value: "+values[orderedIndex[idx-1]]+"diff: "+valueDifference+" ratio: "+ratio);
                 break;
             }                         
        }
        return medianValue;
    }

    public static final int [] tagsort(int nValues, double [] values) {
        int igap = nValues;
        if (nValues < 1 ) return new int[0];

        int [] orderedIndex = new int [nValues];
        for (int idx = 0; idx < nValues; idx++) {
            orderedIndex[idx] = idx;
        }
        if (nValues == 1) return orderedIndex;

        while (igap > 1) { 
            igap = igap/2;
            int idxMax = nValues - igap;
            int counter = 0;
            while (orderArrayElements(igap, idxMax, values, orderedIndex)) ;
        }
        return orderedIndex;
    }

    static private boolean orderArrayElements(int igap, int idxMax, double [] values, int [] orderedIndex) {
        int iex = 0;
        for (int idx = 0; idx < idxMax; idx++) {
            int firstIndex = idx;
            int secondIndex = idx + igap; 
            if (values[orderedIndex[firstIndex]] <= values[orderedIndex[secondIndex]]) continue; 
            int isave = orderedIndex[firstIndex];
            orderedIndex[firstIndex] = orderedIndex[secondIndex];
            orderedIndex[secondIndex] = isave;
            iex++;
        }
        return (iex > 0);
    }
}
