package org.labhc.zvalueencoder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.List;

/**
 * Given dimenstions and a set of values this class generated Z-values and
 * reverse them using the bit interleaving
 * 
 * @author sydgillani
 *
 */
public class ZGenerator implements Zvalues {

	private static final long serialVersionUID = 2017_02_15_001L;

	private final int _d;

	/**
	 * For Test Methods
	 */

	private static int B[] = { 0x55555555, 0x33333333, 0x0F0F0F0F, 0x00FF00FF };
	private static int S[] = { 1, 2, 4, 8 };

	/**
	 * The usual constructor to set the # of dimensions
	 * 
	 * @param d
	 */
	public ZGenerator(int d) {

		_d = d;
	}

	/**
	 * Generate the Z-Address from the given list of values
	 * 
	 * @throws DimensionException
	 */

	@Override
	public boolean[] generate(int[] values) throws DimensionException {

		if (_d != values.length)
			throw new DimensionException(
					"The dimenstions should be the same as the number of incoming values");

		return interleaveBitsByte(_d, values);
	}

	public Long generateLong(int[] values) throws DimensionException {
		if (_d != values.length)
			throw new DimensionException(
					"The dimenstions should be the same as the number of incoming values");

		return interleaveBitsToLong(_d, values);

	}

	/**
	 * Deinterleave the Z-address into a set of values.
	 */

	@Override
	public int[] reverse(boolean[] zadd) {

		return decodeValues(deInterleaveBitsByte(zadd));
	}

	/**
	 * Given the Z-address, Deinterleave it into the dimension numbers
	 * 
	 * @param zadd
	 * @return
	 */
	private String[] deInterleaveBits(List<Integer> zadd) {

		String[] dimVal = new String[_d];
		Arrays.fill(dimVal, "");
		int j = zadd.size() - 1;
		while (j >= 0) {

			for (int i = _d - 1; i >= 0 && j >= 0; i--) {

				dimVal[i] = new StringBuilder().append(zadd.get(j).toString())
						.append(dimVal[i]).toString();
				j--;

			}

		}

		return dimVal;
	}

	/**
	 * Interleave Bits from all the dimensions, by loop through all the 32 bits
	 * We start the interleaving process from the position where the first bit
	 * is ON (i.e. for the highest number in the list).
	 */

	private List<Integer> interleaveBits(int d, int[] values) {

		List<Integer> zadd = new ArrayList<>();
		for (int i = getMSB(values); i >= 0; i--) {
			for (int j = 0; j <= d - 1; j++) {

				zadd.add(getBit(values[j], i));

			}
		}

		return zadd;

	}

	// if (!start && getBit(values[j], i) == 1)
	// start = true;

	// if (start == true) {

	/**
	 * Get the first set bit of the highest number in the list
	 * 
	 * @param values
	 * @return
	 */
	private int getMSB(int[] values) {
		int max = 0;
		for (int i = 0; i < values.length; i++) {
			int k = 32 - Integer.numberOfLeadingZeros(values[i]);
			if (k > max)
				max = k;
		}

		return max;
	}

	/**
	 * Get the required bit of a number at defined position. This function is
	 * used by the interleaving process
	 * 
	 * @param num
	 * @param pos
	 * @return
	 */
	private int getBit(int num, int pos) {

		return (num >> pos) & 1;
	}

	/**
	 * Test Method to convert to Long instead of boolean
	 */

	public Long interleaveBitsToLong(int d, int[] values) {

		int s = getMSB(values);

		StringBuilder st = new StringBuilder();

		for (int i = s; i >= 0; i--) {
			for (int j = 0; j <= d - 1; j++) {

				st.append(getBit(values[j], i) == 1 ? 1 : 0);

			}
		}

		return Long.parseLong(st.toString(), 2);

	}

	/**
	 * Interleave Bits from all the dimensions, by loop through all the 32 bits
	 * We start the interleaving process from the position where the first bit
	 * is ON (i.e. for the highest number in the list). Instead of integer list,
	 * this function used a boolean array.
	 */

	private boolean[] interleaveBitsByte(int d, int[] values) {

		int s = getMSB(values);
		boolean[] data = new boolean[(s * d) + d];

		int bp = 0;
		for (int i = s; i >= 0; i--) {
			for (int j = 0; j <= d - 1; j++) {

				data[bp] = getBit(values[j], i) == 1 ? true : false;

				bp++;

			}
		}

		return data;

	}

	/**
	 * Given the Z-address, Deinterleave it into the dimension numbers. Instead
	 * of Integer List, this function used a static boolean array
	 * 
	 * @param zadd
	 * @return
	 */
	private String[] deInterleaveBitsByte(boolean[] data) {

		String[] dimVal = new String[_d];
		Arrays.fill(dimVal, "");
		int j = data.length - 1;
		while (j >= 0) {

			for (int i = _d - 1; i >= 0 && j >= 0; i--) {

				dimVal[i] = new StringBuilder().append(data[j] == true ? 1 : 0)
						.append(dimVal[i]).toString();
				j--;

			}

		}

		return dimVal;
	}

	@Override
	public int[] decodeValues(String[] values) {

		int[] decodedValues = new int[values.length];

		for (int i = 0; i < values.length; i++) {

			decodedValues[i] = Integer.parseInt(values[i], 2);
		}

		return decodedValues;
	}

	/**
	 * Test Method
	 */
	public int Zorder2d(int x, int y) {
		x = (x | (x << S[3])) & B[3];
		x = (x | (x << S[2])) & B[2];
		x = (x | (x << S[1])) & B[1];
		x = (x | (x << S[0])) & B[0];

		y = (y | (y << S[3])) & B[3];
		y = (y | (y << S[2])) & B[2];
		y = (y | (y << S[1])) & B[1];
		y = (y | (y << S[0])) & B[0];

		return x | (y << 1);
	}

	public Long zcurve4D(int x, int y, int z) {
		Long answer = 0L;
		for (Long i = 0L; i < (Long.SIZE * 3) / 3; ++i) {
			answer |= ((x & (1 << i)) << 2 * i)
					| ((y & (1 << i)) << (2 * i + 1))
					| ((z & (1 << i)) << (2 * i + 2));
		}
		return answer;
	}

	public BitSet interleave(int[] values) {

		BitSet bits = new BitSet(32 * _d);

		int bp = 0;
		for (int i = 31; i >= 0; i--) {
			for (int j = 0; j <= _d - 1; j++) {

				if (getBit(values[j], i) == 1)

					bits.set(bp);
				else

					bits.clear(bp);

				bp++;

			}
		}

		return bits;
	}

	public int[] deinterleave(BitSet data) {
		int[] results = new int[_d];

		int j = 0;
		while (j <= data.length() - 1) {

			for (int i = _d - 1; i >= 0 && j >= 0; i--) {

				results[i] += data.get(j) ? (1 << j) : 0;
				j++;
			}

		}
		return results;
	}

	/*
	 * public String[] deinterleave(BitSet data) {
	 * 
	 * String[] dimVal = new String[_d];
	 * 
	 * StringBuilder[] sb = new StringBuilder[100]; for (int i = 0; i < _d; i++)
	 * { sb[i] = new StringBuilder(); }
	 * 
	 * Arrays.fill(dimVal, ""); int j = 0; while (j <= data.length() - 1) {
	 * 
	 * for (int i = _d - 1; i >= 0 && j >= 0; i--) {
	 * 
	 * dimVal[i] = new StringBuilder() .append(data.get(j) == true ? 1 :
	 * 0).append(dimVal[i]) .toString();
	 * 
	 * sb[i].append(data.get(j) == true ? 1 : 0); j++;
	 * 
	 * }
	 * 
	 * }
	 * 
	 * return dimVal; }
	 */
	public static void main(String[] args) throws IOException,
			DimensionException {

		ZGenerator z = new ZGenerator(2);

		boolean[] b = z.interleaveBitsByte(2, new int[] { 5, 10 });
		System.out.println("original " + b);

		BitSet r2 = z.interleave(new int[] { 5, 10 });

		z.deinterleave(r2);
		System.out.println(r2.toString());
		// z.

	}

}
