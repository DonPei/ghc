package com.geohammer.dsp;

/**
 * Sort routine based on ISORT.FOR from the CUSP library which was based
 * on method of MEISSNER & ORGANICK, P.352 
 * Returns array of indexes sorted in ascending order leaving the 
 * original array untouched.
 */
public class IndexSort
{

	public IndexSort () 
	{
	}

	/** 
	 * Sort integer array
	 */
	public static int[] getSortedIndexes (int[] RX)
	{
		int J, I, NEXT;
		int N = RX.length;

		int IX[] = new int[RX.length];

		for (I = 0; I < N; I++)   // starting array
		{
			IX[I] = I;
		}

		for (J=0; J<(N-1); J++)
		{
			NEXT = IX[J + 1];

			for (I=J ; I>=0; I--)
			{
				if ( RX[NEXT] >= RX[IX[I]] ) break;
				IX[I + 1] = IX[I];
			} 

			IX[I + 1] = NEXT;

		} 

		return (IX);
	}


	/**
	 * Sort double array
	 */
	public static int[] getSortedIndexes (double[] RX)
	{
		int J, I,NEXT;
		int N = RX.length;

		int IX[] = new int[RX.length];

		//  INDIRECT SORT ROUTINE FROM MEISSNER & ORGANICK, P.352
		//  STORES ASCENDING SORT ORDER OF RX IN ARRAY IX, LEAVING RX UNCHANGED

		for (I = 0; I < N; I++)   // starting array
		{
			IX[I] = I;
		}

		//      for (J=0; J<(N-2); J++)
		for (J=0; J<(N-1); J++)
		{
			NEXT = IX[J + 1];

			for (I=J ; I>=0; I--)
			{
				if ( RX[NEXT] >= RX[IX[I]] ) break;
				IX[I + 1] = IX[I];
			} // 20

			IX[I + 1] = NEXT;

		} // 40

		return (IX);
	}

}
