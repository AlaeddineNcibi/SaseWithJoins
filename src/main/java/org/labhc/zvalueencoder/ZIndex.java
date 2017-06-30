package org.labhc.zvalueencoder;

import java.util.Arrays;
import java.util.BitSet;
import java.util.Stack;

/**
 * Comparable ZIndex using the
 * 
 * @author kammoun & Gillani
 *
 */

public class ZIndex implements Comparable<ZIndex> {

	private BitSet zValue;
	private int nbDim;
	private int knowDim;

	// used for debug
	private int[] values;
	// private int z = 0;

	public ZIndex(int[] values, int knowDim, int[] masks) {
		super();
		this.nbDim = values.length;
		this.knowDim = knowDim;
		this.values = values;
		interleave(values, masks);
		// setZ(this.bitSetToInt(zValue, values.length));

	}

	public ZIndex(BitSet zValue, int[] values) {
		super();
		this.zValue = zValue;
		this.nbDim = values.length;

		// this.values = values;
	}

	/**
	 * ZIndex without int array
	 * 
	 * @param zValue
	 */
	public ZIndex(BitSet zValue) {
		super();
		this.zValue = zValue;
		// setZ(this.bitSetToInt(zValue, values.length));
	}

	public ZIndex() {
		// TODO Auto-generated constructor stub
	}
	// public int getZ() {
	// return z;
	// }
	//
	// public void setZ(int z) {
	// this.z = z;
	// }

	public int getKnowDim() {
		return knowDim;
	}

	public void setKnowDim(int knowDim) {
		this.knowDim = knowDim;
	}

	public int[] getValues() {
		return values;
	}

	public void setValues(int[] values) {
		this.values = values;
	}

	public BitSet getZvalue() {
		return zValue;
	}

	public void setZvalue(BitSet zvalue) {
		this.zValue = zvalue;
	}

	public int getNbDim() {
		return nbDim;
	}

	public void setNbDim(int nbDim) {
		this.nbDim = nbDim;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((zValue == null) ? 0 : zValue.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ZIndex other = (ZIndex) obj;
		if (zValue == null) {
			if (other.zValue != null)
				return false;
		} else if (!zValue.equals(other.zValue))
			return false;
		return true;
	}

	@Override
	public int compareTo(ZIndex o) {

		if (o == null)
			return -1;
		if (this.equals(o))
			return 0;

		BitSet b1 = null;
		BitSet b2 = null;

		b1 = o.getZvalue();
		b2 = this.getZvalue();

		BitSet xor = (BitSet) b2.clone();

		xor.xor(b1);
		int firstDifferent = xor.previousSetBit(b1.size() - 1);
		if (firstDifferent == -1)
			return 0;

		return b2.get(firstDifferent) ? 1 : -1;

	}

	/*
	 * @Override public int compareTo(ZIndex o) { if (o == null) return -1; if
	 * (this.equals(o)) return 0;
	 * 
	 * BitSet b1 = null; BitSet b2 = null;
	 * 
	 * b2 = o.getZvalue(); b1 = this.getZvalue();
	 * 
	 * BitSet xor = (BitSet) b2.clone();
	 * 
	 * xor.xor(b1); int r = (xor.isEmpty()) ? 0 : (xor.length() == b1.length() ?
	 * 1 : -1); xor = null; return r;
	 * 
	 * }
	 */

	public int voilationCompareTo(ZIndex o) {

		BitSet b1 = null;
		BitSet b2 = null;

		b1 = o.getZvalue();
		b2 = this.getZvalue();
		BitSet x = ((BitSet) b1.clone());
		x.xor(b2);

		int firstDifferent = x.previousSetBit(b1.size() - 1);
		if (firstDifferent == -1)
			return 0;

		return b2.get(firstDifferent) ? 1 : -1;

	}

	/**
	 * Interleave Bits from all the dimensions, by loop through all the 32 bits
	 */
	private void interleave(int[] values, int[] masks) {

		this.zValue = new BitSet(32 * values.length);

		int bp = (32 * values.length) - 1;
		for (int i = 31; i >= 0; i--) {

			for (int j = 0; j < values.length; j++) {

				if (getBit(values[j], i, masks) > 0) {
					this.zValue.set(bp);

				} else {

					this.zValue.clear(bp);
				}
				bp--;

			}
		}
	}

	public int bitSetToInt(BitSet bitSet, int size) {
		int bitInteger = 0;
		int bp = 32 * size - 1;
		for (int i = 0; i < 32 * size; i++) {
			if (bitSet.get(i))
				bitInteger |= (1 << bp);

			bp--;
		}

		return bitInteger;
	}

	/**
	 * Get the required bit of a number at defined position. This function is
	 * used by the interleaving process
	 * 
	 * @param num
	 * @param pos
	 * @return
	 */
	private int getBit(int num, int pos, int[] masks) {

		return num & masks[pos];
		// return (num >> pos) & 1;

	}

	public static BitSet nji(int gtMin[], int ltMax[], BitSet min, BitSet max, BitSet past, BitSet cur, int d) {
		int dimensionsize = 32;
		int[] flag = new int[d];
		int[] outStep = new int[d];
		// int violation = optimizedNJI(outStep, flag, gtMin, ltMax, min, max,
		// cur, d);
		// gtMinltMax(gtMin, ltMax, min, max, cur, d);
		OptimizedgtMinltMax(gtMin, ltMax, min, max, cur, d);
		// int outStepMin = FindViolation(flag, min, max, cur, d);
		int outStepMin = OptimizedFindViolation(flag, min, max, cur, d);
		if (outStepMin == -1)
			return null;
		int changebp = outStepMin;
		// bitPosition(outStep, dim(outStep, d), d);
		if (flag[dim(outStepMin, d)] == 1) {
			// changebp est incorrect
			for (int i = changebp - 1; i >= 0; i--) {
				if (cur.get(i) == false && ltMax[dim(i, d)] != -1 && i >= ltMax[dim(i, d)]) {
					{
						changebp = i;
						flag[dim(i, d)] = 2;
						break;
					}
				}
			}
		}
		for (int i = 0; i < flag.length; i++) {
			if (flag[i] == 2) {
				incrementsZValueByOneInD(past, i, dimensionsize, d);
				for (int n = 1; n <= dimensionsize; n++) {
					if (past.get(bitPosition(n, i, d))) {
						cur.set(bitPosition(n, i, d));
					} else {
						cur.clear(bitPosition(n, i, d));
					}

				}

			} else {
				if (flag[i] == 0 || flag[i] == 1) {
					if (gtMin[i] != -1 && changebp > gtMin[i]) {
						// TODO remetre changeBp to +1
						for (int n = changebp; n < dimensionsize * d; n++) {
							if (dim(n, d) == i) {
								cur.clear(n);
							}
						}
					} else
						// TODO remetre changeBp to +1
						for (int n = changebp; n < dimensionsize * d; n++) {
							if (dim(n, d) == i) {
								if (min.get(n))
									cur.set(n);
								else
									cur.clear(n);
							}
						}
				} else if (flag[i] == -1) {
					for (int n = 1; n <= dimensionsize; n++) {
						if (min.get(bitPosition(n, i, d)))
							cur.set(bitPosition(n, i, d));
						else
							cur.clear(bitPosition(n, i, d));
					}
				}

			}

		}
		return cur;
	}

	public static boolean FindViolationWithCompare(ZIndex min, ZIndex max, ZIndex cur) {
		if (min.compareTo(cur) == 1)
			return true;
		if (max.compareTo(cur) == -1)
			return true;
		return false;

	}

	public static boolean FindViolationEvolved(ZIndex min, ZIndex max, ZIndex cur) {
		boolean minviolation = true;
		boolean maxviolation = true;
		int minpos = Integer.MAX_VALUE;
		int p;
		boolean curP, minP, maxP;
		int dimensionsize = 32;
		int mindim;
		BitSet curBitSet = cur.getZvalue();
		int nbDimcur = cur.getNbDim();
		mindim = nbDimcur;
		BitSet minBitSet = min.getZvalue();
		int nbDimmin = min.getNbDim();
		if (mindim > nbDimmin)
			mindim = nbDimmin;
		BitSet maxBitSet = max.getZvalue();
		int nbDimmax = max.getNbDim();
		if (mindim > nbDimmax)
			mindim = nbDimmax;

		for (int d = mindim - 1; d >= 0; d--) {
			minviolation = true;
			maxviolation = true;
			for (int i = dimensionsize; i >= 1; i--) {
				p = bitPosition(i, d, nbDimcur);
				curP = curBitSet.get(p);
				p = bitPosition(i, d, nbDimmin);
				minP = minBitSet.get(p);
				p = bitPosition(i, d, nbDimmax);
				maxP = maxBitSet.get(p);

				if (minviolation) {
					if (curP == false & minP == true) {
						// System.out.println(i + " " + d);

						return true;
					} else if (curP == true & minP == false) {
						/**
						 * it's not possible to find minviolation
						 */
						minviolation = false;
					}
				}
				if (maxviolation) {
					if (curP == true & maxP == false) {
						return true;
					} else if (curP == false & maxP == true) {
						/**
						 * it's not possible to find maxviolation
						 */
						maxviolation = false;
					}
				}

			}
		}
		return false;
	}

	public static int FindViolation(int[] flag, BitSet min, BitSet max, BitSet cur, int dimension) {
		boolean minviolation = true;
		boolean maxviolation = true;
		int minpos = Integer.MAX_VALUE;
		int p;
		boolean curP, minP, maxP;
		int dimensionsize = 32;

		// :TODO changer par dimension
		for (int d = 0; d < dimension; d++) {
			minviolation = true;
			maxviolation = true;
			for (int i = 1; i <= dimensionsize; i++) {
				p = bitPosition(i, d, dimension);

				curP = cur.get(p);

				minP = min.get(p);

				maxP = max.get(p);

				if (minviolation) {
					if (curP == false & minP == true) {
						// System.out.println(i + " " + d);
						flag[d] = -1;
						if (minpos > p)
							minpos = p;
						minviolation = false;
					} else if (curP == true & minP == false) {
						/**
						 * it's not possible to find minviolation
						 */
						minviolation = false;
						flag[d] = 0;
					}
				}
				if (maxviolation) {
					if (curP == true & maxP == false) {
						flag[d] = 1;
						if (minpos > p)
							minpos = p;
						maxviolation = false;
					} else if (curP == false & maxP == true) {
						flag[d] = 0;
						maxviolation = false;
					}
				}
			}
		}
		if (minpos != Integer.MAX_VALUE)
			return minpos;
		return -1;
	}

	public static int OptimizedFindViolation(int[] flag, BitSet min, BitSet max, BitSet cur, int dimension) {
		// int outStepD[] = new int[dimension];
		Arrays.fill(flag, Integer.MAX_VALUE);
		int minpos = Integer.MAX_VALUE;
		boolean curP, minP, maxP;
		int dimI = -1;
		int dimensionsize = 32;
		int count = 0;
		for (int i = 0; i < dimensionsize * dimension; i++) {
			dimI = dim(i, dimension);
			curP = cur.get(i);
			minP = min.get(i);
			maxP = max.get(i);

			if (flag[dimI] == Integer.MAX_VALUE) {
				count = 0;
				if (curP == false & minP == true) {
					// System.out.println(i + " " + d);
					flag[dimI] = -1;
					if (minpos > i)
						minpos = i;

				} else if (curP == true & minP == false) {
					/**
					 * it's not possible to find minviolation
					 */

					flag[dimI] = 0;
				}

				if (curP == true & maxP == false) {
					flag[dimI] = 1;
					if (minpos > i)
						minpos = i;

				} else if (curP == false & maxP == true) {
					/**
					 * it's not possible to find maxviolation
					 */
					flag[dimI] = 0;

				}

			} else {
				count++;
				if (count == dimension)
					break;
			}
		}
		if (minpos != Integer.MAX_VALUE)
			return minpos;
		return -1;
	}

	/**
	 * pb or bitPosition: returns the bit position p in the Z-value of bit
	 * {@code bitPosition} of dimension {@code dimension} ;
	 * 
	 * @param bitPosition_in_the_dimension
	 *            position in the original dimension
	 * @param dimension
	 *            the dimension of the {@code bitPosition}
	 * @return bitPosition in the z-value
	 */
	public static int bitPosition(int bitPosition_in_the_dimension, int dimension, int nbdimention) {
		return (bitPosition_in_the_dimension - 1) * nbdimention + dimension;
	}

	public static void OptimizedgtMinltMax(int gtMin[], int ltMax[], BitSet min, BitSet max, BitSet cur, int d) {
		int dimensionsize = 32;
		// boolean curP, minP, maxP;
		Arrays.fill(gtMin, Integer.MAX_VALUE);
		Arrays.fill(ltMax, Integer.MAX_VALUE);
		int dimI = -1;
		boolean curP, minP, maxP;

		for (int n = 0; n < dimensionsize * d; n++) {

			dimI = dim(n, d);
			curP = cur.get(n);
			minP = min.get(n);
			maxP = max.get(n);

			if (curP == true && minP == false && gtMin[dimI] == Integer.MAX_VALUE) {
				gtMin[dim(n, d)] = n;

			} else if (curP == false && minP == true && gtMin[dimI] == Integer.MAX_VALUE) {
				gtMin[dimI] = -1;

			}
			// System.out.println(dim(n, d));
			// cur.get(n);
			// max.get(n);
			if (curP == false && maxP == true && ltMax[dimI] == Integer.MAX_VALUE) {
				ltMax[dimI] = n;

			} else if (curP == true && maxP == false && ltMax[dimI] == Integer.MAX_VALUE) {
				ltMax[dimI] = -1;

			}
			if (n == dimensionsize * d && gtMin[dimI] == Integer.MAX_VALUE)
				gtMin[dim(n, d)] = -1;
			if (n == dimensionsize * d && ltMax[dim(n, d)] == Integer.MAX_VALUE)
				ltMax[dim(n, d)] = -1;
		}

	}

	public static void gtMinltMax(int gtMin[], int ltMax[], BitSet min, BitSet max, BitSet cur, int d) {
		int dimensionsize = 32;
		boolean curP, minP, maxP;
		Arrays.fill(gtMin, -2);
		Arrays.fill(ltMax, -2);
		int p;
		for (int i = 0; i < d; i++) {
			/*
			 * loop over all bits of the dimension, beginning with the highest
			 * valued one (le bit le plus fort)
			 */
			for (int j = 1; j <= dimensionsize; j++) {
				/*
				 * p is returns the bit position p in the Z-value of bit j of
				 * dimension i where d is the number of dimensions;
				 */
				p = bitPosition(j, i, d);

				curP = cur.get(p);

				minP = min.get(p);

				maxP = max.get(p);

				if (curP == true && minP == false && gtMin[i] == -2) {
					gtMin[i] = p;

				} else if (curP == false && minP == true && gtMin[i] == -2) {
					gtMin[i] = -1;

				}
				if (curP == false && maxP == true && ltMax[i] == -2) {
					ltMax[i] = p;

				} else if (curP == true && maxP == false && ltMax[i] == -2) {
					ltMax[i] = -1;

				}
				if (j == dimensionsize && gtMin[i] == -2)
					gtMin[i] = -1;
				if (j == dimensionsize && ltMax[i] == -2)
					ltMax[i] = -1;
			}

		}
	}

	public static int optimizedNJI(int outStep[], int flag[], int gtMin[], int ltMax[], BitSet min, BitSet max,
			BitSet cur, int d) {
		Arrays.fill(gtMin, -1);
		Arrays.fill(ltMax, -1);
		Arrays.fill(outStep, Integer.MAX_VALUE);
		Arrays.fill(flag, 0);

		int minOutstep = Integer.MAX_VALUE;
		for (int i = 0; i < d * 32; i++) {
			if (flag[dim(i, d)] != -1 && gtMin[dim(i, d)] == -1 && cur.get(i) && !min.get(i)) {
				gtMin[dim(i, d)] = i;
			}
			if (flag[dim(i, d)] != 1 && ltMax[dim(i, d)] == -1 && !cur.get(i) && max.get(i)) {
				ltMax[dim(i, d)] = i;
			}

			if (outStep[dim(i, d)] == Integer.MAX_VALUE) {
				if (!cur.get(i) && min.get(i)) {
					outStep[dim(i, d)] = i;
					flag[dim(i, d)] = -1;
					if (minOutstep > i)
						minOutstep = i;

				} else if (!min.get(i) && cur.get(i)) {
					outStep[dim(i, d)] = i;
					flag[dim(i, d)] = 1;
					if (minOutstep > i)
						minOutstep = i;
				}
			}
		}
		return minOutstep;
	}

	/**
	 * return the original the dimension of the bit returns the dimension d, bit
	 * position p belongs to
	 * 
	 * @param zBitPosition
	 * @param nbdimention
	 * @return
	 */
	public static int dim(int zBitPosition, int nbdimention) {
		int s = zBitPosition % nbdimention;
		return s;
	}

	public static void incrementsZValueByOneInD(BitSet values, int d, int dimensionsize, int nbdimention) {
		int bp = bitPosition(dimensionsize, d, nbdimention);
		for (int i = bp; i >= 0; i = i - nbdimention) {
			if (values.get(i) == false) {
				values.set(i);
				break;
			} else {
				values.clear(i);

			}
		}

	}

	public static void decrementsZValueByOne(BitSet values, int d) {

		for (int i = 0; i < d * 32; i++) {
			if (values.get(i)) {
				values.clear(i);
				break;
			} else {
				values.set(i);

			}
		}

	}

	public static BitSet loopOverBitSetVariableDimensions(BitSet b, int t, int s) {

		int pointer1 = (b.size() - 32 - 1);
		int pointer2 = pointer1 - t;
		BitSet b2 = new BitSet(t * 32);

		int bp = b2.size() - 1;
		while (pointer2 >= 0) {

			while (pointer1 > pointer2) {
				if (b.get(pointer1)) {
					b2.set(bp, true);

				} else {
					// System.out.println(bp + " " + pointer2);
					b2.set(bp, false);

				}
				bp--;
				pointer1--;
			}
			// bp = bp - s;
			pointer2 = pointer2 - s;
			pointer1 = pointer2;
			pointer2 = pointer2 - t;
		}

		return b2;
	}

	/**
	 * Test of interleave function and compare
	 * 
	 * @param args
	 * @throws DimensionException
	 */
	@Override
	public String toString() {

		return "ZIndex [zValue=" + zValue + ", values=" + Arrays.toString(this.values) + "]";

	}

	public static boolean loopOverBitSet(BitSet b, BitSet b2, int t, int s) {

		int pointer1 = b.size();
		int pointer2 = pointer1 - t;

		while (pointer2 >= 0) {

			while (pointer1 > pointer2) {
				if (b.get(pointer1) != b2.get(pointer1)) {
					return false;
				}

				pointer1--;
			}

			pointer2 = pointer2 - s;
			pointer1 = pointer2;
			pointer2 = pointer2 - t;
		}

		return true;
	}

	public static boolean bitcompare(BitSet b, BitSet b2, int t, int s) {

		int pointer1 = b.size();
		int pointer2 = pointer1 - t;

		while (pointer2 >= 0) {

			if (!b.get(pointer2, pointer1).equals(b2.get(pointer2, pointer1))) {
				return false;
			}

			pointer2 = pointer2 - s;
			pointer1 = pointer2;
			pointer2 = pointer2 - t;

		}

		return true;
	}

	private static int createMask(int a, int b) {

		int r = 0;
		for (int i = a; i <= b; i++)
			r |= 1 << i;

		return r;
	}

	public static int nearbyPoints(BitSet b, BitSet b2, int t, int s) {

		int pointer1 = b.size();
		int pointer2 = pointer1 - t;

		while (pointer2 >= 0) {

			if (!b.get(pointer2, pointer1).equals(b2.get(pointer2, pointer1))) {
				return pointer1;
			}

			pointer2 = pointer2 - s;
			pointer1 = pointer2;
			pointer2 = pointer2 - t;

		}

		return pointer1;
	}

	public static int nearbyPointsOptimized(BitSet b1, BitSet b2, int t, int s) {
		int c = 0;
		BitSet bitsetnew2 = /* (BitSet) b2.clone(); */new BitSet(t * 32);
		bitsetnew2.clear();
		// BitSet bitsetnew1 = /*(BitSet) b1.clone();*/ new BitSet(t * 32);
		// bitsetnew1.clear();
		// System.out.println("b");
		// for (int i = b1.nextSetBit(0); i >= 0; i = b1.nextSetBit(i + 1)) {
		// // operate on index i here
		//
		// if (dim(i, t + s) > t) {
		// //System.out.println("position " + i + " dim " + dim(i, t + s));
		// bitsetnew1.set(i);
		// }
		//
		// }

		// System.out.println("b2");
		int totalnbofdim = t + s;
		for (int i = b2.nextSetBit(0); i >= 0; i = b2.nextSetBit(i + 1)) {

			// operate on index i here
			// System.out.println("dim of "+i+ " is "+ dim(i, t + s));
			int dim = (totalnbofdim - 1) - dim(i, t + s);
			if (dim < t) {
				// System.out.println("position " + i + " dim " + dim);
				// int xx= (i+1)/( t + s);
				// System.out.println("position in original dimension"+xx);
				// System.out.println("new position"+ (newbitPosition((i+1)/( t
				// + s), dim, t)-1));
				bitsetnew2.set(newbitPosition((i + 1) / (t + s), dim, t) - 1);
			}

		}
		// System.out.println("");
		bitsetnew2.xor(b1);
		// System.out.println("score" +(bitsetnew.previousSetBit(t*32)+1));
		return (bitsetnew2.previousSetBit(t * 32) + 1);

	}

	public static int newbitPosition(int bitPosition_in_the_dimension, int dimension, int nbdimention) {
		return (bitPosition_in_the_dimension) * nbdimention + dimension;
	}

	public static int zindexDistance(BitSet[] BitSetMasks, ZIndex z1, ZIndex z2, int knowndim) {

		BitSet b1 = (BitSet) BitSetMasks[knowndim - 1].clone();

		BitSet b2 = (BitSet) BitSetMasks[knowndim - 1].clone();

		b1.and(z1.getZvalue());

		b2.and(z2.getZvalue());

		if (b1.equals(b2)) {

			return 0;

		} else {

			int frst = b1.previousSetBit(b1.size() - 1);

			int sec = b2.previousSetBit(b1.size() - 1);

			while (frst == sec) {

				frst = b1.previousSetBit(frst - 1);

				sec = b2.previousSetBit(sec - 1);

			}

			return frst > sec ? 1 : -1;

		}
	}

	public static void test1() throws DimensionException {
		int[] masks = new int[32];
		for (int n = 0; n < 32; n++) {
			masks[n] = 1 << n;
		}
		int[] minBound = new int[2];
		minBound[0] = 7;
		minBound[1] = 3;

		ZIndex zMinBound = new ZIndex(minBound, 2, masks);
		// System.out.println(zMinBound.bitSetToInt(zMinBound.getZvalue()));
		int[] maxBound = new int[2];
		maxBound[0] = 3;
		maxBound[1] = 7;

		ZIndex zMaxBound = new ZIndex(maxBound, 2, masks);

		int[] cur = new int[4];
		cur[0] = 3400000;
		cur[1] = 80000;
		cur[2] = 0;
		cur[3] = 0;

		ZIndex zcur = new ZIndex(cur, 4, masks);

		BitSet[] BitSetMasks = new BitSet[4 - 1];
		for (int j = 0; j < BitSetMasks.length; j++) {

			BitSetMasks[j] = new BitSet(4 * 32);

			int knowndim = j + 1;

			BitSetMasks[j] = generateMask(BitSetMasks[j], 4, knowndim, 4 * 32);

		}

		long begin = System.nanoTime();
		System.out.println(nearbyPoints(zcur.zValue, zMaxBound.zValue, 2, 2));
		System.out.println("nearbyPoints" + (System.nanoTime() - begin));
		begin = System.nanoTime();
		// System.out.println(zindexDistance(BitSetMasks, zcur,zMaxBound));
		System.out.println("zindexDistance" + (System.nanoTime() - begin));

		// System.out.println(nearbyPointsOptimized(zMaxBound.zValue,
		// zcur.zValue, 2, 2));

		// System.out.println(FindViolationWithCompare(zMinBound, zMaxBound,
		// zcur));
		// System.out.println(FindViolationEvolved(zMinBound, zMaxBound, zcur));
		// System.out.println(zMinBound.compareTo(zcur));
	}

	/*
	 * public static void test2() throws DimensionException { int d = 2; int[]
	 * gtMin = new int[d]; int[] ltMax = new int[d]; Stack<Integer> violations =
	 * new Stack<Integer>(); /** test generator of matches Zmatch
	 */

	/*
	 * int nn = 0; ZIndex array[] = new ZIndex[10000];
	 * 
	 * for (int i = 0; i < 100; i++) { for (int j = 0; j < 100; j++) { int[]
	 * element = new int[2]; element[0] = i; element[1] = j; ZIndex zIndex = new
	 * ZIndex(element); array[nn] = zIndex; nn++; } } Arrays.sort(array);
	 * System.out.println(Arrays.toString(array)); int[] minBound = new int[2];
	 * minBound[0] = 30; minBound[1] = 2; ZIndex zMinBound = new
	 * ZIndex(minBound);
	 * 
	 * int[] maxBound = new int[2]; maxBound[0] = 31; maxBound[1] = 40; ZIndex
	 * zMaxbound = new ZIndex(maxBound);
	 * 
	 * int wasted = 0; long b = System.nanoTime(); int maxIndex =
	 * Arrays.binarySearch(array, zMaxbound); int minIndex =
	 * Arrays.binarySearch(array, zMinBound); for (int i = minIndex; i <=
	 * maxIndex; i++) { if (array[i].getValues()[0] >= 30 &&
	 * array[i].getValues()[0] <= 31 && array[i].getValues()[1] >= 2 &&
	 * array[i].getValues()[1] <= 40) System.out.println(array[i]); // continue;
	 * else wasted++;
	 * 
	 * } long e = System.nanoTime(); System.out.println((e - b) + "   wasted " +
	 * wasted); long timetest = e - b; b = System.nanoTime(); int saved = 0,
	 * newminIndex = 0; while (minIndex <= maxIndex) { if
	 * (array[minIndex].getValues()[0] >= 30 && array[minIndex].getValues()[0]
	 * <= 31 && array[minIndex].getValues()[1] >= 2 &&
	 * array[minIndex].getValues()[1] <= 40) {
	 * System.out.println(array[minIndex]); minIndex++; } else { // get last
	 * value in the box
	 * 
	 * BitSet last = (BitSet) array[minIndex - 1].getZvalue().clone(); //
	 * dincrementsZValueByOne(d, array[minIndex].getZvalue()); // using the
	 * current value get next BitSet current = (BitSet)
	 * array[minIndex].getZvalue().clone();
	 * 
	 * ZIndex matchedzz = new ZIndex(nji(gtMin, ltMax, zMinBound.getZvalue(),
	 * zMaxbound.getZvalue(), last, current, 2));
	 * 
	 * newminIndex = Arrays.binarySearch(array, matchedzz);
	 * 
	 * if (newminIndex > 0) { saved = saved + (newminIndex - minIndex); minIndex
	 * = newminIndex; System.out.println(array[minIndex]); minIndex++; } } } e =
	 * System.nanoTime(); System.out.println((e - b) + "  " + timetest);
	 * System.out.println(saved); }
	 */
	public static void main(String[] args) throws DimensionException {
		test1();
	}

	public static BitSet generateMask(BitSet b, int nbOfDimension, int t, int start) {

		int s = nbOfDimension - t;
		int skipping = s;

		int pointer1 = start - 1;
		int pointer2 = pointer1 - t;

		int bp = start - 1;

		while (pointer2 >= -t) {

			while (pointer1 > pointer2 && pointer1 >= 0) {

				b.set(bp, true);
				bp--;
				pointer1--;
			}
			while (skipping > 0) {
				if (bp > 0)
					b.set(bp, false);
				bp--;
				skipping--;
			}
			skipping = s;

			pointer2 = pointer2 - s;
			pointer1 = pointer2;
			pointer2 = pointer2 - t;
		}
		return b;

	}

	public static int preProcessingNextJumpIn(int outStep[], int flag[], int gtMin[], int ltMax[], BitSet min,
			BitSet max, BitSet cur, int d) {

		int curMSB = cur.previousSetBit(Integer.MAX_VALUE);
		int maxMSB = max.previousSetBit(Integer.MAX_VALUE);
		int begin = curMSB > maxMSB ? curMSB : maxMSB;
		Arrays.fill(flag, 0);
		Arrays.fill(gtMin, -1);
		Arrays.fill(ltMax, -1);
		Arrays.fill(outStep, Integer.MAX_VALUE);
		// Arrays.fill(flag, 0);

		int minOutstep = -1;
		for (int i = begin; i >= 0; i--) {
			int dimension = inversedDim(i, d);

			if (flag[dimension] != -1 && gtMin[dimension] == -1 && cur.get(i) && !min.get(i)) {
				gtMin[dimension] = i;
			}
			if (flag[dimension] != 1 && ltMax[dimension] == -1 && !cur.get(i) && max.get(i)) {
				ltMax[dimension] = i;
			}

			if (outStep[dimension] == Integer.MAX_VALUE) {
				if (!cur.get(i) && min.get(i) && gtMin[dimension] == -1) {
					outStep[dimension] = i;
					flag[dimension] = -1;
					if (minOutstep == -1)
						minOutstep = i;

				} else if (!max.get(i) && cur.get(i) && ltMax[dimension] == -1) {
					outStep[dimension] = i;
					flag[dimension] = 1;
					if (minOutstep == -1)
						minOutstep = i;
				}
			}
		}
		return minOutstep;
	}

	public static boolean violationFound(int flag[], int gtMin[], int ltMax[], BitSet min, BitSet max, BitSet cur,
			int d) {

		int curMSB = cur.previousSetBit(Integer.MAX_VALUE);
		int maxMSB = max.previousSetBit(Integer.MAX_VALUE);
		int begin = curMSB > maxMSB ? curMSB : maxMSB;
		Arrays.fill(gtMin, -1);
		Arrays.fill(ltMax, -1);
		Arrays.fill(flag, 0);
		int count = 0;
		for (int i = begin; i >= 0; i--) {
			int dimension = inversedDim(i, d);

			if (gtMin[dimension] == -1 && cur.get(i) && !min.get(i)) {
				gtMin[dimension] = i;
				count++;
			}
			if (ltMax[dimension] == -1 && !cur.get(i) && max.get(i)) {
				ltMax[dimension] = i;
				count++;
			}

			if (!cur.get(i) && min.get(i) && gtMin[dimension] == -1) {

				flag[dimension] = -1;
				return true;

			} else if (!max.get(i) && cur.get(i) && ltMax[dimension] == -1) {

				flag[dimension] = 1;
				return true;
			}
			if (2 * d == count)
				return false;

		}
		return false;
	}

	public static BitSet nextJumpInOptimized(int outStep[], int flag[], int gtMin[], int ltMax[], BitSet min,
			BitSet max, BitSet past, BitSet cur, int d, int changebp) {

		int dimensionsize = 32;
		int dim = inversedDim(changebp, d);
		if (flag[dim] == 1) {
			// changebp est incorrect
			for (int i = changebp + 1; i < d * dimensionsize - 1; i++) {
				if (cur.get(i) == false && ltMax[inversedDim(i, d)] != -1 && i <= ltMax[inversedDim(i, d)]) {
					{
						changebp = i;
						flag[inversedDim(i, d)] = 2;
						break;
					}
				}
			}
		}
		for (int i = 0; i < flag.length; i++) {
			if (flag[i] == 2) {
				incrementsZValueByOneInD(past, i, dimensionsize, d);
				// mask
				int bp = 0;
				for (int n = dimensionsize - 1; n > 0; n--) {
					bp = ((dimensionsize * d) - 1) - newbitPosition(n, i, d);
					if (past.get(bp)) {
						cur.set(bp);
					} else {
						cur.clear(bp);
					}
				}

			} else {
				if (flag[i] == 0 || flag[i] == 1) {
					if (gtMin[i] != -1 && changebp < gtMin[i]) {
						// TODO remetre changeBp to +1
						for (int n = changebp - 1; n >= 0; n = n - d) {
							if (inversedDim(n, d) == i) {
								cur.clear(n);
							}
						}
					} else
						// TODO remetre changeBp to +1
						for (int n = changebp - 1; n >= 0; n--) {
							if (inversedDim(n, d) == i) {
								if (min.get(n))
									cur.set(n);
								else
									cur.clear(n);
							}
						}
				} else if (flag[i] == -1) {
					int pos = ((dimensionsize * d) - 1) - newbitPosition(0, i, d);
					for (int n = pos; n >= 0; n = n - d) {
						// pos = ((dimensionsize*d)-1)-newbitPosition(n, i, d);
						if (min.get(n))
							cur.set(n);
						else
							cur.clear(n);
					}

				}

			}

		}

		return cur;

	}

	public static int inversedDim(int zBitPosition, int nbdimention) {
		return (nbdimention - 1) - dim(zBitPosition, nbdimention);
	}

	public static void test2() throws DimensionException {
		ZIndex zindex = new ZIndex();
		zindex.setNbDim(2);
		int outStep[] = new int[2];
		int flag[] = new int[2];

		int[] masks = new int[32];
		for (int n = 0; n < 32; n++) {
			masks[n] = 1 << n;
		}
		int d = 2;
		int[] gtMin = new int[d];
		int[] ltMax = new int[d];
		Stack<Integer> violations = new Stack<Integer>();
		/** test generator of matches Zmatch */

		int nn = 0;
		ZIndex array[] = new ZIndex[10000];
		for (int a = 0; a < 100; a++) {
			for (int b = 0; b < 100; b++) {
				// for (int c = 0; c < 10; c++) {
				// for (int e = 0; e < 10; e++) {
				//
				// for (int j = 0; j < 10; j++) {
				// for (int i = 0; i < 10; i++) {
				int[] element = new int[2];
				element[0] = a;
				element[1] = b;

				ZIndex zIndex = new ZIndex(element, 2, masks);
				array[nn] = zIndex;
				nn++;
			}
			// }
			// }
			// }
			// }
		}
		Arrays.sort(array);
		System.out.println(Arrays.toString(array));
		int[] minBound = new int[2];
		minBound[0] = 32;
		minBound[1] = 1;
		// minBound[2] = 0;
		// minBound[3] = 1;
		// minBound[4] = 0;
		// minBound[5] = 1;

		ZIndex zMinBound = new ZIndex(minBound, 2, masks);

		int[] maxBound = new int[2];
		maxBound[0] = 30;
		maxBound[1] = 99;
		// maxBound[2] = 3;
		// maxBound[3] = 9;
		// maxBound[4] = 2;
		// maxBound[5] = 6;
		ZIndex zMaxbound = new ZIndex(maxBound, 2, masks);
		int found = 0;
		int wasted = 0;
		long b = System.nanoTime();
		int maxIndex = Arrays.binarySearch(array, zMaxbound);
		int minIndex = Arrays.binarySearch(array, zMinBound);
		for (int i = minIndex; i <= maxIndex; i++) {
			// if (array[i].getValues()[0] >= 0 && array[i].getValues()[0] <= 7
			// && array[i].getValues()[1] >= 1
			// && array[i].getValues()[1] <= 5
			//
			// && array[i].getValues()[2] >= 0 && array[i].getValues()[2] <= 3
			// && array[i].getValues()[3] >= 1
			// && array[i].getValues()[3] <= 9
			//
			// && array[i].getValues()[4] >= 0 && array[i].getValues()[4] <= 2
			// && array[i].getValues()[5] >= 1
			// && array[i].getValues()[5] <= 6)
			if (!violationFound(flag, gtMin, ltMax, zMinBound.getZvalue(), zMaxbound.getZvalue(), array[i].getZvalue(),
					d)) {// System.out.println(
							// Arrays.toString(array[i].getValues()));
				found++;
				continue;
			} else {
				wasted++;
			}
		}
		long e = System.nanoTime();
		System.out.println((e - b) + "   wasted " + wasted + "found" + found);
		long timetest = e - b;
		b = System.nanoTime();
		int saved = 0, newminIndex = 0;
		while (minIndex <= maxIndex) {
			// if (!violationFound(flag, gtMin, ltMax, zMinBound.getZvalue(),
			// zMaxbound.getZvalue(),
			// array[minIndex].getZvalue(), 2))
			// if (array[minIndex].getValues()[0] >= 10 &&
			// array[minIndex].getValues()[0] <= 80
			// && array[minIndex].getValues()[1] >= 2 &&
			// array[minIndex].getValues()[1] <= 80)
			//
			// if (array[minIndex].getValues()[0] >= 0 &&
			// array[minIndex].getValues()[0] <= 7
			// && array[minIndex].getValues()[1] >= 1 &&
			// array[minIndex].getValues()[1] <= 5
			//
			// && array[minIndex].getValues()[2] >= 0 &&
			// array[minIndex].getValues()[2] <= 3
			// && array[minIndex].getValues()[3] >= 1 &&
			// array[minIndex].getValues()[3] <= 9
			//
			// && array[minIndex].getValues()[4] >= 0 &&
			// array[minIndex].getValues()[4] <= 2
			// && array[minIndex].getValues()[5] >= 1 &&
			// array[minIndex].getValues()[5] <= 6) {
			// // System.out.println(array[minIndex]);
			// minIndex++;
			// } else {
			// get last value in the box

			// BitSet last = (BitSet) array[minIndex - 1].getZvalue().clone();
			// // dincrementsZValueByOne(d, array[minIndex].getZvalue());
			// // using the current value get next
			// BitSet current = (BitSet) array[minIndex].getZvalue().clone();

			// ZIndex matchedzz = new ZIndex(
			// nji(gtMin, ltMax, zMinBound.getZvalue(),
			// zMaxbound.getZvalue(), last, current, 2));

			int changebp = preProcessingNextJumpIn(outStep, flag, gtMin, ltMax, zMinBound.getZvalue(),
					zMaxbound.getZvalue(), array[minIndex].getZvalue(), d);
			if (changebp == -1) {
				minIndex++;
			} else {
				BitSet last = (BitSet) array[minIndex - 1].getZvalue().clone();
				// dincrementsZValueByOne(d, array[minIndex].getZvalue());
				// using the current value get next
				BitSet current = (BitSet) array[minIndex].getZvalue().clone();
				BitSet matchedzz = nextJumpInOptimized(outStep, flag, gtMin, ltMax, zMinBound.getZvalue(),
						zMaxbound.getZvalue(), last, current, d, changebp);
				zindex.setZvalue(matchedzz);
				newminIndex = Arrays.binarySearch(array, zindex);

				if (newminIndex > 0) {
					saved = saved + (newminIndex - minIndex);
					minIndex = newminIndex;
					// System.out.println( "nji" +array[minIndex]);
					minIndex++;
				}
			}
		}
		e = System.nanoTime();
		System.out.println((e - b) + "  " + timetest);
		System.out.println(saved);
	}

}
