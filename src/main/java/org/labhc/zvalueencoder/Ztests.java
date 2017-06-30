package org.labhc.zvalueencoder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.BitSet;

import org.labhc.intervaltree.IntervalTree;
import org.labhc.intervaltree.RangeQuery;

public class Ztests {
	public static BitSet[] BitSetMasks;

	public static void main(String[] args) throws IOException, DimensionException {

		ArrayList<ZIndex> a = new ArrayList<>();

		int[] masks = new int[32];
		for (int n = 0; n < 32; n++) {
			masks[n] = 1 << n;
		}
		int[] z1 = new int[3];
		z1[0] = 50;
		z1[1] = 7;
		z1[2] = 1;
		ZIndex zindex1 = new ZIndex(z1, 3, masks);

		int[] zr = new int[3];
		zr[0] = 40;
		zr[1] = 9;
		zr[2] = 2;

		 zindex1 = new ZIndex(z1, 3, masks);
		// zindex1.setNbDim(0);

		// System.out.println(a.get(0).getZvalue());

		// zr[2] = 0;
		ZIndex zindex2 = new ZIndex(zr, 3, masks);

		// System.out.println("New one "+zindex1.testCompareTo(zindex2));

		IntervalTree<RangeQuery> tree = new IntervalTree<>();

		System.out.println("old one " + zindex1.compareTo(zindex2));

		int dim = 3;

		BitSetMasks = new BitSet[dim];
		for (int j = 0; j < BitSetMasks.length; j++) {

			BitSetMasks[j] = new BitSet(dim * 32);

			int knowndim = 1;

			BitSetMasks[j] = generateMask(BitSetMasks[j], dim, knowndim, (dim * 32) - j);

		}

		System.out.println(zindex1.zindexDistance(BitSetMasks, zindex1, zindex2, 3));

		// zindex1.getZvalue().and(BitSetMasks[1]);

		// zindex2.getZvalue().and(BitSetMasks[1]);

		// System.out.println(zindex1.voilationCompareTo(zindex2));

		// compare that ///compare with this
		// System.out.println(compareTo( zindex2.getZvalue(),
		// zindex1.getZvalue()));

		//
		// ZGenerator z = new ZGenerator(2);
		//
		// // int k = 32 - Integer.numberOfLeadingZeros(12000);
		// // System.out.println(k);
		//
		// boolean[] a = z.generate(new int[] { 5, 80 });
		//
		// int[] re = z.reverse(a);
		//
		// for (int i = 0; i < re.length; i++) {
		//
		// System.out.println(re[i]);
		//
		// }
		//
		// DB db = DBMaker.openMemory().make();
		// BTree<Key, Integer> tree = BTree.createInstance((DBAbstract) db);
		//
		// for (int i = 1; i < 20; i++) {
		// Key k = new Key();
		//
		// k.setKey(z.generate(new int[] { i * 2, i * 3 }));
		//
		// tree.insert(k, i, true);
		// }
		//
		// Key k1 = new Key();
		//
		// Key k2 = new Key();
		//
		// k1.setKey(z.generate(new int[] { 2, 3 }));
		//
		// k2.setKey(z.generate(new int[] { 15, 10 }));
		//
		// System.out.println(tree.searchRange(k1, k2));
		//
		// db.close();
	}

	public static int factorial(int n) {
		int fact = 1; // this will be the result
		for (int i = 1; i <= n; i++) {
			fact *= i;
		}
		return fact;
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

	public static int compareTo(BitSet bs1, BitSet bs2) {
		BitSet x = ((BitSet) bs1.clone());
		x.xor(bs2);
		int firstDifferent = x.previousSetBit(bs1.size() - 1);
		if (firstDifferent == -1)
			return 0;

		return bs2.get(firstDifferent) ? 1 : -1;
	}

}
