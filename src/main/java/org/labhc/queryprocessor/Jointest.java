package org.labhc.queryprocessor;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import org.davidmoten.hilbert.HilbertCurve;
import org.labhc.zvalueencoder.ZIndex;

public class Jointest {

	public static void main(String[] args) {
		// createListsandJoins();

		HilbertCurve c = HilbertCurve.bits(10).dimensions(2);

		BigInteger min = c.index(6, 5);

		ArrayList<BigInteger> v = new ArrayList<>();

		int[] masks = new int[32];
		for (int n = 0; n < 32; n++) {
			masks[n] = 1 << n;
		}

		for (int i = 0; i < 1000000; i++) {
			// v.add(c.index(i + 5, i + 10));

			int[] z1 = new int[2];
			z1[0] = i + 5;
			z1[1] = i + 10;
			ZIndex zindex1 = new ZIndex(z1, 2, masks);

		}

		BigInteger index1 = c.index(10, 6);

		v.add(index1);
		BigInteger index2 = c.index(8, 6);
		v.add(index2);

		BigInteger index4 = c.index(5, 3);

		v.add(index4);
		BigInteger index5 = c.index(9, 5);

		v.add(index5);

		BigInteger max = c.index(11, 7);

		System.out.println("Mini " + min + " Max " + max);
		/*
		 * for (int i = 0; i < v.size(); i++) {
		 * 
		 * System.out.println(v.get(i));
		 * 
		 * if (min.compareTo(v.get(i)) <= 1 && max.compareTo(v.get(i)) >= 1) {
		 * System.out.println("results "); System.out.println(i);
		 * System.out.println(v.get(i));
		 * 
		 * } }
		 */

	}

	public static void createListsandJoins() {
		Jointest js = new Jointest();

		Jointest.Vtest v1 = js.new Vtest(80, 0, 5);
		Jointest.Vtest v2 = js.new Vtest(90, 0, 3);
		Jointest.Vtest v3 = js.new Vtest(100, 0, 10);
		Jointest.Vtest v4 = js.new Vtest(110, 0, 4);
		Jointest.Vtest v5 = js.new Vtest(200, 0, 12);

		List<Vtest> L1 = new ArrayList<>();

		L1.add(v1);
		L1.add(v2);
		L1.add(v3);
		L1.add(v4);

		List<Vtest> L2 = new ArrayList<>();
		L2.add(v3);
		L2.add(v1);
		L2.add(v4);
		L2.add(v2);

		List<Vtest> L11 = new ArrayList<>();

		L11.add(v1);
		L11.add(v2);
		L11.add(v3);
		L11.add(v4);
		L11.add(v5);

		List<Vtest> L22 = new ArrayList<>();
		L22.add(v5);

		L22.add(v3);

		L22.add(v1);

		L22.add(v4);
		L22.add(v2);

		//// first permutation array

		List<Integer> P1 = createPermutationOrOffsetArray(L2, L1);

		List<Integer> P2 = createPermutationOrOffsetArray(L22, L11);

		List<Integer> O1 = createPermutationOrOffsetArray(L1, L11);

		List<Integer> O2 = createPermutationOrOffsetArray(L2, L22);

		ArrayList<Boolean> B = new ArrayList<Boolean>();

		for (int k = 0; k < 5; k++) {
			B.add(false);
		}

		int eqoff = 0;

		for (int i = 0; i < 4; i++) {

			int off2 = O2.get(i);

			for (int j = 0; j < Math.min(off2, L2.size()); j++) {

				B.set(P2.get(j), true);

			}

			int off1 = O1.get(P1.get(i));

			for (int m = off1 + eqoff; m < 5; m++) {
				if (B.get(m)) {
					System.out.println(L2.get(i).toString() + " " + L22.get(m).toString());
				}
			}
		}

	}

	public static List<Integer> createPermutationOrOffsetArray(List<Vtest> L1, List<Vtest> L2) {

		List<Integer> p = new ArrayList<>();
		for (int i = 0; i < L1.size(); i++) {

			p.add(i, L2.indexOf(L1.get(i)));
			// p.set(i, L2.indexOf(L1.get(i)));

		}

		return p;

	}

	public class Vtest {
		public int p;
		public int v;
		public int t;

		public Vtest(int p, int v, int t) {
			super();
			this.p = p;
			this.v = v;
			this.t = t;
		}

		@Override
		public String toString() {
			return "Vtest [p=" + p + ", v=" + v + ", t=" + t + "]";
		}

	}

}
