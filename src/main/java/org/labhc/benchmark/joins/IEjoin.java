package org.labhc.benchmark.joins;

import java.io.IOException;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

import org.labhc.btreeUtils.Value;
import org.labhc.event.ActivityEvent;
import org.labhc.event.StockEvent;
import org.labhc.queryprocessor.ExtendedStockQueryProcessor.PriceComparator;
//import org.labhc.queryprocessor.ExtendedStockQueryProcessor.TimeComparator;
import org.labhc.queryprocessor.ExtendedStockQueryProcessor.VolumeComparator;
import org.labhc.queryprocessor.ActivityQueryProcessor.TimeComparator;
import org.labhc.queryprocessor.ActivityQueryProcessor.RateComparator;


public class IEjoin {
	static ArrayList<Value> L1 = new ArrayList<Value>();
	static ArrayList<Value> L1_dash = new ArrayList<Value>();
	static ArrayList<Value> L2 = new ArrayList<Value>();
	static ArrayList<Value> L2_dash = new ArrayList<Value>();
	static ArrayList<Value> L3 = new ArrayList<Value>();
	static ArrayList<Value> L3_dash = new ArrayList<Value>();
	static ArrayList<Integer> P = new ArrayList<Integer>();
	static ArrayList<Integer> P_dash = new ArrayList<Integer>();
	static ArrayList<Integer> P32 = new ArrayList<Integer>();
	static ArrayList<Integer> P_dash32 = new ArrayList<Integer>();
	static ArrayList<Integer> P21 = new ArrayList<Integer>();
	static ArrayList<Integer> P_dash21 = new ArrayList<Integer>();
	static ArrayList<Integer> P31 = new ArrayList<Integer>();
	static ArrayList<Integer> P_dash31 = new ArrayList<Integer>();
	static PriceComparator bc_price = new PriceComparator();
	static RateComparator bc_rate = new RateComparator();
	static VolumeComparator bc_volume = new VolumeComparator();
	static TimeComparator bc_time = new TimeComparator();
	static ArrayList<Integer> O1 = new ArrayList<Integer>();
	static ArrayList<Integer> O2 = new ArrayList<Integer>();
	static ArrayList<Integer> O3 = new ArrayList<Integer>();
	static BitSet B_dash = new BitSet();
	static BitSet B32 = new BitSet();
	static BitSet B21 = new BitSet();
	
	// ****************************************************************************************//

	/*
	 * Get the Values from a (Value1) and from b (Value2) that satisfy the
	 * relation Value1.methodName op Value2.methodName if op is < choose True
	 * for "isAscending" and False otherwise
	 */
	public static void joinArrays(ArrayList<Value> a, ArrayList<Value> b, String methodName, boolean isAscending) {
		// ArrayList<ArrayList<Value>> results = new
		// ArrayList<ArrayList<Value>>(); //Create the table of the join results
		BeanComparator bc = new BeanComparator(Value.class, methodName, isAscending); // create
																						// the
																						// comparator
		Collections.sort(a, bc); // sort table 1
		Collections.sort(b, bc); // sort b
		ArrayList<Integer> O = new ArrayList<Integer>(a.size() - 1);
		/**
		 * create
		 * 
		 * the offset table (offset of a w.r.t table 2)
		 */
		computeOffset(a, b, O, bc, true); // compute the offset table
		for (int i = 0; i < a.size(); i++) { // for every Value in the
												// first table
			if (O.get(i) < b.size()) { // if there are Values in the
										// table 2 that respect the relation
										// with the Value i of the table 1
				for (int j = O.get(i); j < b.size(); j++) {

					/**
					 * for all the Values in b hat respect the relation with the
					 * Value i of a ArrayList<Value> result = new ArrayList
					 * <Value>(2); //create a new couple for a new join result
					 * result.add(a.get(i)); //add the first Value to the couple
					 * result.add(b.get(j)); //add the second elemnt to the
					 * couple results.add(result); add the couple to the join
					 * results table
					 */

					System.out.println(a.get(i).getPredicate(1) + " vol" + a.get(i).getPredicate(2)
							+ b.get(i).getPredicate(1) + "vol" + b.get(i).getPredicate(1));
				}
			}
		}
		// return results; //return the join results
	}

	// ****************************************************************************************//

	/*
	 * Compute the permutation table " permutations"of L2 in L1
	 * 
	 */
	public static void computePermutations(ArrayList<Value> L2, ArrayList<Value> L1, ArrayList<Integer> permutations) {
		for (int i = 0; i < L2.size(); i++) { // for every Value in L2
			permutations.add(L1.indexOf(L2.get(i))); // find the position of
														// that Value i in
														// table L1
		}
	}

	// ****************************************************************************************//

	/*
	 * Compute the offset table "offset" of L1 w.r.t to L1_dash bc is the
	 * comparator used to sort the Values of L1 and L1_dash choose True for
	 * "strict" to find the strict relative positions of the Values of L1 in
	 * L1_dash
	 */
	public static void computeOffset(ArrayList<Value> L1, ArrayList<Value> L1_dash, ArrayList<Integer> offset,
			Comparator<Value> bc, boolean strict) {
		boolean found;
		int j;
		for (int i = 0; i < L1.size(); i++) { // for every Value in a
			found = false;
			j = 0;
			while (j < L1_dash.size() & found == false) { // while we haven't
															// found the
															// position and we
															// didn"t check all
															// the Values of
															// L1_dash
				if (strict) {
					if (bc.compare(L1.get(i), L1_dash.get(j)) < 0) {
						offset.add(j);
						found = true;
					}
				} else if (bc.compare(L1.get(i), L1_dash.get(j)) <= 0) {
					offset.add(j);
					found = true;
				}
				j++;
			}
			if (found == false) {
				offset.add(L1_dash.size());
			}

		}
	}

	public static void IEJoin2(ArrayList<Value> a, ArrayList<Value> b, LinkedBlockingQueue<String> outputWriter, StockEvent c) throws IOException {

		// *** Article's method ***//

		// Create the arrays L1, L1_dash, L2 and L2_dash
		L1.clear();
		L1_dash.clear();
		L2.clear();
		L2_dash.clear();
		for (int l = 0; l < a.size(); l++) {
			L1.add(a.get(l));
			L2.add(a.get(l));
		}
		for (int l = 0; l < b.size(); l++) {
			L1_dash.add(b.get(l));
			L2_dash.add(b.get(l));
		}
		// Sort the arrays L1, L1_dash, L2 and L2_dash

		Collections.sort(L1, bc_price);
		Collections.sort(L1_dash, bc_price);
		Collections.sort(L2, bc_volume);
		Collections.sort(L2_dash, bc_volume);

		// Compute the permutation arrays P and P_dash

		P.clear();
		P_dash.clear();
		computePermutations(L2, L1, P);
		computePermutations(L2_dash, L1_dash, P_dash);

		// Compute the offset arrays O1 and O2
		O1.clear();
		O2.clear();
		computeOffset(L1, L1_dash, O1, bc_price, true);
		computeOffset(L2, L2_dash, O2, bc_volume, false);

		// Initialize the bit array B_dash
		//BitSet B_dash = new BitSet();

		// Initialize the results ArrayList
	//	ArrayList<ArrayList<Value>> results = new ArrayList<ArrayList<Value>>();
	
		int i = 0, j = 0, k = 0, off1 = 0, off2 = 0;
	//	long startTime = System.nanoTime();
		int m = a.size(), n = b.size();
		for (i = 1; i <= m; i++) {
			
			B_dash.clear();
			off2 = O2.get(i - 1) + 1; // +1 because décalage indices

			for (j = 1; j <= Math.min(off2 - 1, L2_dash.size()); j++) {
				/**
				 * mistake b ou L2_dashinstead of L2 + mistake off2-1
				 */
				B_dash.set(P_dash.get(j - 1));

			}
			off1 = O1.get(P.get(i - 1)) + 1;
			int  setbits [] = new int [L1_dash.size()];
			int p=0;
			for (k = off1; k <= n; k++) {

				if (B_dash.get(k - 1)) {
					
					
					setbits[p]=(k - 1);p++;
				//	System.out.println(L2.get(i - 1).event.id+"," + L1_dash.get(k - 1).event.id +","+ c.id +"\n");

//					outputWriter.add(L2.get(i - 1).event.id+"," + L1_dash.get(k - 1).event.id +","+ c.id +"\n");
//					ArrayList<Value> result = new ArrayList<Value>(2);
//					result.add(L2.get(i - 1));
//					result.add(L1_dash.get(k - 1)); // it was a mistake here
//					results.add(result);
				}
			
			}
			
			if (B_dash.cardinality() > 0)
				outputCombinations(L2.get(i - 1).event.id, (List)L1_dash,p,p - 1, ((StockEvent) c).id ,
						setbits,outputWriter);
		}

		// Show the results
		//long endTime = System.nanoTime();
		//long totalTime = endTime - startTime;
		//System.out.println("Execution time for the articles method: " + totalTime + "ms");
//		System.out.println("Results of the article's method");
//		for (int l = 0; l < results.size(); l++) {
//			System.out.println(
//					"(" + results.get(l).get(0).getPredicate(1) + "," + results.get(l).get(1).getPredicate(2) + ")");
//		}

	}
	
	
	public static void IEJoin2(ArrayList<Value> a, ArrayList<Value> b, LinkedBlockingQueue<String> outputWriter, ActivityEvent c) throws IOException {

		// *** Article's method ***//

		// Create the arrays L1, L1_dash, L2 and L2_dash
		L1.clear();
		L1_dash.clear();
		L2.clear();
		L2_dash.clear();
		for (int l = 0; l < a.size(); l++) {
			L1.add(a.get(l));
			L2.add(a.get(l));
		}
		for (int l = 0; l < b.size(); l++) {
			L1_dash.add(b.get(l));
			L2_dash.add(b.get(l));
		}
		// Sort the arrays L1, L1_dash, L2 and L2_dash

		Collections.sort(L1, bc_time);
		Collections.sort(L1_dash, bc_time);
		Collections.sort(L2, bc_rate);
		Collections.sort(L2_dash, bc_rate);

		// Compute the permutation arrays P and P_dash

		P.clear();
		P_dash.clear();
		computePermutations(L2, L1, P);
		computePermutations(L2_dash, L1_dash, P_dash);

		// Compute the offset arrays O1 and O2
		O1.clear();
		O2.clear();
		computeOffset(L1, L1_dash, O1, bc_time, true);
		computeOffset(L2, L2_dash, O2, bc_rate, false);

		// Initialize the bit array B_dash
		//BitSet B_dash = new BitSet();

		// Initialize the results ArrayList
	//	ArrayList<ArrayList<Value>> results = new ArrayList<ArrayList<Value>>();
	
		int i = 0, j = 0, k = 0, off1 = 0, off2 = 0;
	//	long startTime = System.nanoTime();
		int m = a.size(), n = b.size();
		for (i = 1; i <= m; i++) {
			
			B_dash.clear();
			off2 = O2.get(i - 1) + 1; // +1 because décalage indices

			for (j = 1; j <= Math.min(off2 - 1, L2_dash.size()); j++) {
				/**
				 * mistake b ou L2_dashinstead of L2 + mistake off2-1
				 */
				B_dash.set(P_dash.get(j - 1));

			}
			off1 = O1.get(P.get(i - 1)) + 1;
			int  setbits [] = new int [L1_dash.size()];
			int p=0;
			for (k = off1; k <= n; k++) {

				if (B_dash.get(k - 1)) {
					
					
					setbits[p]=(k - 1);p++;
				//	System.out.println(L2.get(i - 1).event.id+"," + L1_dash.get(k - 1).event.id +","+ c.id +"\n");

//					outputWriter.add(L2.get(i - 1).event.id+"," + L1_dash.get(k - 1).event.id +","+ c.id +"\n");
//					ArrayList<Value> result = new ArrayList<Value>(2);
//					result.add(L2.get(i - 1));
//					result.add(L1_dash.get(k - 1)); // it was a mistake here
//					results.add(result);
				}
			
			}
			
			if (B_dash.cardinality() > 0)
				outputCombinations(L2.get(i - 1).event.id, (List)L1_dash,p,p - 1, ((ActivityEvent) c).id ,
						setbits,outputWriter);
		}

		// Show the results
		//long endTime = System.nanoTime();
		//long totalTime = endTime - startTime;
		//System.out.println("Execution time for the articles method: " + totalTime + "ms");
//		System.out.println("Results of the article's method");
//		for (int l = 0; l < results.size(); l++) {
//			System.out.println(
//					"(" + results.get(l).get(0).getPredicate(1) + "," + results.get(l).get(1).getPredicate(2) + ")");
//		}

	}
	
	
	private static void outputCombinations(int element, List<Value> b, int numofsetbits, int end, int c, int[] setbits, LinkedBlockingQueue<String>  outputQueue)
			throws IOException {

		for (int i = 1; i < Math.pow(2, (numofsetbits)); i++) {

			BitSet bs = BitSet.valueOf(new long[] { i });
		//	System.out.print(Integer.toString(element));
			outputQueue.add(Integer.toString(element));
			for (int j = bs.previousSetBit(numofsetbits); j >= 0; j = bs.previousSetBit(j - 1)) {
				if (j == Integer.MIN_VALUE) {
					break; 
				}
			//	System.out.print("," + b.get(setbits[end - j]).event.id);
				outputQueue.add("," + b.get(setbits[end - j]).event.id);
			}

			// System.out.println();
		//	System.out.print("," + c + "\n");
			outputQueue.add("," + c);
			outputQueue.add("\n");

		}
		//System.out.println();
	}
	
	
	

	public static void IEJoin3(ArrayList<Value> a, ArrayList<Value> b, LinkedBlockingQueue<String> outputWriter, StockEvent c) throws IOException {

		/** Create the arrays L1, L1_dash, L2, L2_dash, L3, L3_dash */
		L1.clear();
		/**
		 * L1 is the a sorted on the third attribute
		 */
		L1_dash.clear();
		/**
		 * L1_dash is the b sorted on the third attribute
		 */

		L2.clear();
		/**
		 * L2 is the a sorted on the second attribute
		 */
		L2_dash.clear();
		/**
		 * L1 is the b sorted on the second attribute
		 */

		L3.clear();
		/**
		 * L3 is the a sorted on the first attribute
		 */
		L3_dash.clear();
		/**
		 * L3_dash is the
		 * 
		 * b sorted on the first attribute
		 */
		for (int l = 0; l < a.size(); l++) {
			L1.add(a.get(l));
			L2.add(a.get(l));
			L3.add(a.get(l));
		}
		for (int l = 0; l < b.size(); l++) {
			L1_dash.add(b.get(l));
			L2_dash.add(b.get(l));
			L3_dash.add(b.get(l));
		}

		//TimeComparator bc_price = new TimeComparator();
		//VolumeComparator bc_volume = new VolumeComparator();
		//PriceComparator bc_time = new PriceComparator();

		Collections.sort(L1, bc_volume);
		Collections.sort(L1_dash, bc_volume);
		Collections.sort(L2, bc_price);
		Collections.sort(L2_dash, bc_price);
		Collections.sort(L3, bc_time);
		Collections.sort(L3_dash, bc_time);

		/**
		 * Compute the permutation arrays P32 and P_dash32, P31 and P_dash31,
		 * P21 and P_dash21
		 */
		computePermutations(L3, L1, P31);
		/**
		 * P31 is the permutation array of L3 in L1
		 */
		computePermutations(L3_dash, L1_dash, P_dash31);
		/**
		 * P_dash31 is the permutation array of L3_dash in L1_dash
		 */
		computePermutations(L2, L1, P21);
		/**
		 * P21 is the permutation array of L2 in L1
		 */
		computePermutations(L2_dash, L1_dash, P_dash21);
		/**
		 * P_dash21 is the permutation array of L2_dash in L1_dash
		 */
		computePermutations(L3, L2, P32);
		/** 
		 * P32 is the permutation array of L3 in L2 
		 */
		computePermutations(L3_dash, L2_dash, P_dash32);
		/**
		 * P_dash32 is the permutation array of L3_dash in L2_dash
		 */

		/** Compute the offset arrays O1 and O2 */
		computeOffset(L1, L1_dash, O1, bc_volume, false);
		/**
		 * O1 is the offset array of L1 w.r.t L1_dash without strict relative
		 * positions
		 */
		computeOffset(L2, L2_dash, O2, bc_price, true);
		/**
		 * O2 is the offset array of L2 w.r.t L2_dash with strict relative
		 * positions
		 */
		computeOffset(L3, L3_dash, O3, bc_time, true);
		/**
		 * O3 is the offset array of L3 w.r.t L3_dash with strict relative
		 * positions
		 */

		/** Initialize the bit arrays B32 and B21 */
		//BitSet B32 = new BitSet(b.size());
		//BitSet B21 = new BitSet(b.size());

		/** Initialize the results ArrayList */
		//ArrayList<ArrayList<Value>> results = new ArrayList<ArrayList<Value>>();
		//int numofsetbits;
		//int[] setbits;
		// BEGIN
		int i = 0, j = 0, k = 0, l = 0, off1 = 0, off2 = 0, off3 = 0;
		//long startTime = System.nanoTime();
		int m = a.size(), n = b.size();
		for (i = 1; i <= m; i++) { // for all Values in L3
			
			B32.clear(); // clear the bitset B32
			B21.clear(); // clear the bitset B21
			off3 = O3.get(i - 1) + 1; // find the relative position of L3[i] in
										// L3_dash
			for (j = off3; j <= n; j++) { // for all the Values from L3_dash
											// that respect the first relation
											// (for the first attribute:time)
											// with L3[i]
				B32.set(P_dash32.get(j - 1));
			}
			off2 = O2.get(P32.get(i - 1)) + 1; // find the relative position of
												// L3[i] in L2_dash
			for (k = off2; k <= n; k++) { // for all the Values from L2_dash
											// that respect the second relation
											// (for the second attribute:price)
											// with L3[i]
				if (B32.get(k - 1)) {
					B21.set(P_dash21.get(k - 1));
				}
			}
			off1 = O1.get(P31.get(i - 1)) + 1; // find the relative position of
												// L3[i] in L1_dash
			 
			 int  setbits [] = new int [L1_dash.size()];
			 int p=0;
			 for (l = 1; l <= Math.min(off1 - 1, L1_dash.size()); l++) {
				/**
				 * for all the Values from L1_dash that respect the third
				 * relation (for the third attribute:volume) with L3[i]
				 */
				if (B21.get(l - 1)) {
					
					setbits[p]=(l - 1);p++;
					// ArrayList<Value> result = new ArrayList<Value>(2);
					// result.add(L3.get(i-1));
					// result.add(L1_dash.get(l-1));
					// results.add(result);
					//System.out.println(L3.get(i - 1).getPredicate(1) + " vol" + L3.get(i - 1).getPredicate(2)
					//		+ L1_dash.get(l - 1).getPredicate(1) + "vol" + L1_dash.get(l - 1).getPredicate(1));

				
				}
			}
			
			
			///if (numofsetbits > 0)
		//	outputCombinations(a.get(i).event.id, b, numofsetbits, b.size() - 1, ((StockEvent) c).id,
			//		setbits);
			
			 
		//	 if (B_dash.cardinality() > 0)
		/*	// System.out.println("before cominations"+i);
			 outputCombinations(L3.get(i - 1).event.id, (List)L1_dash,p,p - 1, ((StockEvent) c).id ,
							setbits,outputWriter);
			// System.out.println("afetr combinations"+i);
			 * 
			 */
		}

		// Show the results
		//long endTime = System.nanoTime();
		//long totalTime = endTime - startTime;
		//System.out.println("**Execution time: " + totalTime + " ns");
		//System.out.println("**Results**");
		//for (l = 0; l < results.size(); l++) {
			//System.out.println(
					//"(" + results.get(l).get(0).getPredicate(1) + "," + results.get(l).get(1).getPredicate(2) + ")");
		//}

	}
	
	
	public static void IEJoin3(ArrayList<Value> a, ArrayList<Value> b, LinkedBlockingQueue<String> outputWriter, ActivityEvent c) throws IOException {

		/** Create the arrays L1, L1_dash, L2, L2_dash, L3, L3_dash */
		L1.clear();
		/**
		 * L1 is the a sorted on the third attribute
		 */
		L1_dash.clear();
		/**
		 * L1_dash is the b sorted on the third attribute
		 */

		L2.clear();
		/**
		 * L2 is the a sorted on the second attribute
		 */
		L2_dash.clear();
		/**
		 * L1 is the b sorted on the second attribute
		 */

		L3.clear();
		/**
		 * L3 is the a sorted on the first attribute
		 */
		L3_dash.clear();
		/**
		 * L3_dash is the
		 * 
		 * b sorted on the first attribute
		 */
		for (int l = 0; l < a.size(); l++) {
			L1.add(a.get(l));
			L2.add(a.get(l));
			L3.add(a.get(l));
		}
		for (int l = 0; l < b.size(); l++) {
			L1_dash.add(b.get(l));
			L2_dash.add(b.get(l));
			L3_dash.add(b.get(l));
		}

		//TimeComparator bc_price = new TimeComparator();
		//VolumeComparator bc_volume = new VolumeComparator();
		//PriceComparator bc_time = new PriceComparator();

		Collections.sort(L1, bc_volume);
		Collections.sort(L1_dash, bc_volume);
		Collections.sort(L2, bc_price);
		Collections.sort(L2_dash, bc_price);
		Collections.sort(L3, bc_time);
		Collections.sort(L3_dash, bc_time);

		/**
		 * Compute the permutation arrays P32 and P_dash32, P31 and P_dash31,
		 * P21 and P_dash21
		 */
		computePermutations(L3, L1, P31);
		/**
		 * P31 is the permutation array of L3 in L1
		 */
		computePermutations(L3_dash, L1_dash, P_dash31);
		/**
		 * P_dash31 is the permutation array of L3_dash in L1_dash
		 */
		computePermutations(L2, L1, P21);
		/**
		 * P21 is the permutation array of L2 in L1
		 */
		computePermutations(L2_dash, L1_dash, P_dash21);
		/**
		 * P_dash21 is the permutation array of L2_dash in L1_dash
		 */
		computePermutations(L3, L2, P32);
		/** 
		 * P32 is the permutation array of L3 in L2 
		 */
		computePermutations(L3_dash, L2_dash, P_dash32);
		/**
		 * P_dash32 is the permutation array of L3_dash in L2_dash
		 */

		/** Compute the offset arrays O1 and O2 */
		computeOffset(L1, L1_dash, O1, bc_volume, false);
		/**
		 * O1 is the offset array of L1 w.r.t L1_dash without strict relative
		 * positions
		 */
		computeOffset(L2, L2_dash, O2, bc_price, true);
		/**
		 * O2 is the offset array of L2 w.r.t L2_dash with strict relative
		 * positions
		 */
		computeOffset(L3, L3_dash, O3, bc_time, true);
		/**
		 * O3 is the offset array of L3 w.r.t L3_dash with strict relative
		 * positions
		 */

		/** Initialize the bit arrays B32 and B21 */
		//BitSet B32 = new BitSet(b.size());
		//BitSet B21 = new BitSet(b.size());

		/** Initialize the results ArrayList */
		//ArrayList<ArrayList<Value>> results = new ArrayList<ArrayList<Value>>();
		//int numofsetbits;
		//int[] setbits;
		// BEGIN
		int i = 0, j = 0, k = 0, l = 0, off1 = 0, off2 = 0, off3 = 0;
		//long startTime = System.nanoTime();
		int m = a.size(), n = b.size();
		for (i = 1; i <= m; i++) { // for all Values in L3
			
			B32.clear(); // clear the bitset B32
			B21.clear(); // clear the bitset B21
			off3 = O3.get(i - 1) + 1; // find the relative position of L3[i] in
										// L3_dash
			for (j = off3; j <= n; j++) { // for all the Values from L3_dash
											// that respect the first relation
											// (for the first attribute:time)
											// with L3[i]
				B32.set(P_dash32.get(j - 1));
			}
			off2 = O2.get(P32.get(i - 1)) + 1; // find the relative position of
												// L3[i] in L2_dash
			for (k = off2; k <= n; k++) { // for all the Values from L2_dash
											// that respect the second relation
											// (for the second attribute:price)
											// with L3[i]
				if (B32.get(k - 1)) {
					B21.set(P_dash21.get(k - 1));
				}
			}
			off1 = O1.get(P31.get(i - 1)) + 1; // find the relative position of
												// L3[i] in L1_dash
			 
			 int  setbits [] = new int [L1_dash.size()];
			 int p=0;
			 for (l = 1; l <= Math.min(off1 - 1, L1_dash.size()); l++) {
				/**
				 * for all the Values from L1_dash that respect the third
				 * relation (for the third attribute:volume) with L3[i]
				 */
				if (B21.get(l - 1)) {
					
					setbits[p]=(l - 1);p++;
					// ArrayList<Value> result = new ArrayList<Value>(2);
					// result.add(L3.get(i-1));
					// result.add(L1_dash.get(l-1));
					// results.add(result);
					//System.out.println(L3.get(i - 1).getPredicate(1) + " vol" + L3.get(i - 1).getPredicate(2)
					//		+ L1_dash.get(l - 1).getPredicate(1) + "vol" + L1_dash.get(l - 1).getPredicate(1));

				
				}
			}
			
			
			///if (numofsetbits > 0)
		//	outputCombinations(a.get(i).event.id, b, numofsetbits, b.size() - 1, ((StockEvent) c).id,
			//		setbits);
			
			 
		//	 if (B_dash.cardinality() > 0)
		/*	// System.out.println("before cominations"+i);
			 outputCombinations(L3.get(i - 1).event.id, (List)L1_dash,p,p - 1, ((StockEvent) c).id ,
							setbits,outputWriter);
			// System.out.println("afetr combinations"+i);
			 * 
			 */
		}

		// Show the results
		//long endTime = System.nanoTime();
		//long totalTime = endTime - startTime;
		//System.out.println("**Execution time: " + totalTime + " ns");
		//System.out.println("**Results**");
		//for (l = 0; l < results.size(); l++) {
			//System.out.println(
					//"(" + results.get(l).get(0).getPredicate(1) + "," + results.get(l).get(1).getPredicate(2) + ")");
		//}

	}

}
