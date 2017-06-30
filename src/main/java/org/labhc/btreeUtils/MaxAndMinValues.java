package org.labhc.btreeUtils;

public class MaxAndMinValues {

	public int[] max;

	public int[] min;

	public MaxAndMinValues(int n) {

		max = new int[n];

		min = new int[n];

		for (int i = 0; i < min.length; i++) {

			max[i] = 0;
			min[i] = Integer.MAX_VALUE;
		}
	}
	
	
	public  void updateMaxMin(int t, int p, int v) {

		if (t > max[0]) {

			max[0] = t;
		}

		if (t < min[0]) {

			min[0] = t;
		}

		if (p > max[1]) {

			max[1] = p;
		}

		if (p <min[1]) {

			min[1] = p;
		}

		if (v > max[2]) {

			max[2] = v;
		}

		if (v < min[2]) {

			min[2] = v;
		}

	}
	

}
