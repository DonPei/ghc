package com.geohammer.rt.pseudo3d;

public class ArrayMath {

	/**
	 * Performs a binary search in a monotonic array of values. Values are
	 * assumed to increase or decrease monotonically, with no equal values.
	 * This method is most efficient when called repeatedly for slightly 
	 * changing search values; in such cases, the index returned from one 
	 * call should be passed in the next.
	 * <p>
	 * Warning: this method does not ensure that the specified array is
	 * monotonic; that check would be more costly than this search.
	 * @param a the array of values, assumed to be monotonic.
	 * @param x the value for which to search.
	 * @param i the index at which to begin the search. If negative, this 
	 *  method interprets this index as if returned from a previous call.
	 * @return the index at which the specified value is found, or, if not
	 *  found, -(i+1), where i equals the index at which the specified value 
	 *  would be located if it was inserted into the monotonic array.
	 */
	public static int binarySearch(float[] a, float x, int i) {
		int n = a.length;
		int nm1 = n-1;
		int low = 0;
		int high = nm1;
		boolean increasing = n<2 || a[0]<a[1];
		if (i<n) {
			high = (0<=i)?i:-(i+1);
			low = high-1;
			int step = 1;
			if (increasing) {
				for (; 0<low && x<a[low]; low-=step,step+=step)
					high = low;
				for (; high<nm1 && a[high]<x; high+=step,step+=step)
					low = high;
			} else {
				for (; 0<low && x>a[low]; low-=step,step+=step)
					high = low;
				for (; high<nm1 && a[high]>x; high+=step,step+=step)
					low = high;
			}
			if (low<0) low = 0;
			if (high>nm1) high = nm1;
		}
		if (increasing) {
			while (low<=high) {
				int mid = (low+high)>>1;
			float amid = a[mid];
			if (amid<x)
				low = mid+1;
			else if (amid>x)
				high = mid-1;
			else
				return mid;
			}
		} else {
			while (low<=high) {
				int mid = (low+high)>>1;
			float amid = a[mid];
			if (amid>x)
				low = mid+1;
			else if (amid<x)
				high = mid-1;
			else
				return mid;
			}
		}
		return -(low+1);
	}
	
	/**
	 * Returns the absolute value of the specified value.
	 * Note that {@code abs(-0.0f)} returns {@code -0.0f};
	 * the sign bit is not cleared.
	 * If this is a problem, use {@code Math.abs}.
	 * @param x the value.
	 * @return the absolute value.
	 */
	public static float abs(float x) {
		return (x>=0.0f)?x:-x;
	}
	
	/**
	 * Returns the minimum of the specified values.
	 * @param a a value.
	 * @param b a value.
	 * @return the minimum value.
	 */
	public static float min(float a, float b) {
		return (a<=b)?a:b;
	}
	/**
	 * Returns the maximum of the specified values.
	 * @param a a value.
	 * @param b a value.
	 * @return the maximum value.
	 */
	public static float max(float a, float b) {
		return (a>=b)?a:b;
	}
}
