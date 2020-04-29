package com.geohammer.dsp;
/**
   computes the weighted median of values(n) using the weights in w(n) and puts the result in medianvalue.
   doctored to give the same results normally expected for the case of equal non-zero weights
   has been trapped to tolerate negative weights, they are treated as if they are 0's.
*/
public class WeightedMedian extends Object {
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

    public static final void main(String [] args) {
        double xtest [] = {13.,2.,12.,4.,11.,6.,10.,8.,9.,7.,5.,3.,1.,0.,14.};
        double equalWts [] =   {.5,.5,.5,.5,.5,.5,.5,.5,.5, .5, .5, .5, .5,.5,.5};
        double biasedHiWts [] =  {1.,.1,1.,.1,1.,.1,.5,.2,.4, .3, .1, .1, .1,.2,.8};
        double biasedLoWts [] =  {.1,1.,.1,.8,.1,.6,.1,.2,.1, .2, .3, 1., 1.,.8,.2};
        double median = calcMedian(xtest.length, xtest, equalWts);
        System.out.println("equal wts median: " + median);
        median = calcMedian(xtest.length, xtest, biasedHiWts);
        System.out.println("biased high wts median: " + median);
        median = calcMedian(xtest.length, xtest, biasedLoWts);
        System.out.println("biased low wts median: " + median);
    }
}

