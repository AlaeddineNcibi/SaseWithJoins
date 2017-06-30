package org.labhc.trie;

public class Test {
	private int solutions = 0;

	String result = "";

	public static void main(String[] args) {

		// Trie<Integer> t = new Trie();
		//
		// long[] v1 = new long[] { 1, 22, 3 };
		//
		// long[] v2 = new long[] { 2, 44, 5 };
		//
		// long[] v3 = new long[] { 1, 22, 8 };
		//
		// t.insertString(v1, 1);
		//
		// t.insertString(v2, 10);
		//
		// t.insertString(v3, 12);
		//
		// System.out.println(t.search(new long[] { 1, 22, 10 }));
		//
		// for (long i = 0; i < 100; i++) {
		//
		// // t.insertString(Integer.toString(i), i);
		// }

		int[] r = new int[] { 8, 10, 22, 40, 60, 70 };
		int[] s = new int[] { 1, 5, 7, 15, 20, 22 };
		// mergesortJoin(r, s, 0);

		String[] a = new String[] { "a1", "a2" };

		String[] b = new String[] { "b1", "b2", "b3", "b4" };

		// joins(a, b, "c");

		Test t = new Test();
		;

		System.out.println(t.perm(0, 3, t.result));

	}

	public static void mergesortJoin(int[] r, int[] s, int cond) {

		// /lets us that r is the samlles one

		int pos_r = r.length - 1;
		int pos_s = s.length - 1;
		int s_block = s.length - 1;
		int r_block = r.length - 1;
		String[] results = new String[r.length * 10 + s.length * 20];
		int count = 0;
		while (pos_r >= 0) {

			if (r[pos_r] > s[pos_s]) {
				results[count] = Integer.toString(r[pos_r]) + " "
						+ Integer.toString(s[pos_s]);
				count++;
			}
			r_block = pos_r;
			while (r[pos_r] > s[pos_s]) {

				results[count] = Integer.toString(r[pos_r]) + " "
						+ Integer.toString(s[pos_s]);
				count++;

				pos_r--;
			}

			pos_s--;
			pos_r = r_block;
			s_block = pos_s;

			while (r[pos_r] > s[pos_s]) {
				results[count] = Integer.toString(r[pos_r]) + " "
						+ Integer.toString(s[pos_s]);
				count++;
				pos_s--;
				if (pos_s < 0)
					break;
			}
			pos_s = s_block;
			pos_r--;

		}

	}

	private static void joins(String[] a, String[] b, String c) {

		int first = 0;

		int last = b.length - 1;

		String[] results = new String[20];
		int r = 0;
		String firstR = "";
		String lastR = "";

		int s = 0;

		for (int i = 0; i < a.length; i++) {

			while (first <= last) {

				firstR = firstR + b[first];
				lastR = b[last] + lastR;

				results[r] = a[i] + firstR + c;
				results[++r] = a[i] + lastR + c;

				results[++r] = a[i] + firstR + lastR + c;

				if (s != 0) {
					results[++r] = a[i] + b[first] + c;

					results[++r] = a[i] + b[last] + c;
				}
				s++;

				first++;
				last--;

			}

		}

	}

	private String perm(int n, int size, String result) {
		if (n == size) {
			solutions++;

			return result;
		}
		for (int i = 0; i < size; i++) {
			result = result + '0' + Integer.toString(i);

			/*
			 * Check for the condition for the current element if condition
			 * satisfies recurse for the next n
			 */

			perm(n + 1, size, result);
		}
		return result;
	}

}
