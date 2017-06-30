package org.labhc.queryprocessor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;

import org.labhc.benchmark.joins.IEjoin;
import org.labhc.btreeUtils.Value;
import org.labhc.event.Event;
import org.labhc.event.EventType;
import org.labhc.event.StockEvent;
import org.labhc.eventstore.EventStore;
import org.labhc.windowsemantics.AbstractWindow;
import org.labhc.zvalueencoder.DimensionException;
import org.labhc.zvalueencoder.ZIndex;

public class ExtendedStockQueryProcessor extends AbstractQueryProcessor {
	int i = 0;

	public ExtendedStockQueryProcessor(LinkedBlockingQueue<Event> i, CountDownLatch l, AbstractWindow w)
			throws IOException {
		super(i, l, w);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void process(Event e) throws IOException, DimensionException {
		window.initliaseWindow();
		if (e.eventType == EventType.STOCKEVENT) {
			i++;

			StockEvent ce = (StockEvent) e;
			updateMaxMin(ce.timestamp, ce.price, ce.vol);
			if (i == 34)
				stockABCQuery(ce);
			// Integer key = zval.Zorder2d(ce.price, ce.vol);
			ZIndex key = new ZIndex(new int[] { ce.timestamp, ce.price, ce.vol }, 2, masks);

			window.updateWindow(ce.timestamp);

			EventStore store = getStore(ce);

			store.tree().insert(key, new Value(ce, ce.timestamp), false);

			// }

		}

	}

	private ZIndex[] createRangesForB(StockEvent ce) {

		ZIndex minZRange = new ZIndex(new int[] { minmax.min[0], minmax.min[1], minmax.min[2] }, 3, masks);

		ZIndex maxZRange = new ZIndex(new int[] { minmax.max[0], ce.price, minmax.max[2] }, 3, masks);

		return new ZIndex[] { minZRange, maxZRange };
	}

	private ZIndex[] createRangesForA(StockEvent be, int cprice) {
		// /For now hardcoded
		// /For A Sequence;
		ZIndex minZRange = new ZIndex(new int[] { minmaxInter.min[0], minmaxInter.min[1], minmaxInter.min[2] }, 3,
				masks);

		ZIndex maxZRange = new ZIndex(new int[] { minmaxInter.max[0] - 1, minmaxInter.max[1] - 1, minmaxInter.max[2] },
				3, masks);

		// Integer minArange = zval.Zorder2d(minmax.min[0], minmax.min[1]);
		// Integer maxARange = zval.Zorder2d(minmax.max[0], ce.vol);

		return new ZIndex[] { minZRange, maxZRange };

	}

	private void stockABCQuery(StockEvent ce) throws IOException, DimensionException {
		ZIndex[] ranges = createRangesForB(ce);
		List<Value> valB = queryStore(ce, ranges);
		/// from B get the A bindings
		if (valB == null || valB.isEmpty())
			return;


		// Collections.sort(valB, new TimeComparator());

		// ranges= createRangesForA(ce);
		ranges = createRangesForA((StockEvent) valB.get(valB.size() - 1).event, ce.price);
		List<Value> valA = queryStore(ce, ranges);
		if (valA == null || valA.isEmpty())
			return;

		// computeMatches(valA, valB, ce);

	   // createResultsABC(valA, valB, "<", ce.price);
		long startTime = System.nanoTime();
		//createResultsIEjoin3(valA, valB, ce);
	    createResultsABCComplexJoin(valA, valB, "<", ce);
		long endTime = System.nanoTime();
		long totalTime = endTime - startTime;
		System.out.println("Execution time: " + totalTime + "ns");
	}

	private void computeMatches(List<Value> valA, List<Value> valB, StockEvent ce) {

		for (Value va : valA) {

			for (Value vb : valB) {

				// System.out.println(va.get_window().get(0) + " " +
				// vb.get_window().get(0));

				// System.out.println(((StockEvent) va.event).price + " " +
				// ((StockEvent) vb.event).price);

				if (((StockEvent) va.event).price < ((StockEvent) vb.event).price
						&& va.get_window().get(0) < vb.get_window().get(0)) {

					// System.out.println("Event A " + ((StockEvent)
					// va.event).price + " Event B "
					// + ((StockEvent) vb.event).price + " Event C " +
					// ce.price);
					//
				}
			}

		}

	}

	private void createResultsIEjoin3(List<Value> a, List<Value> b, StockEvent c) throws IOException {
		IEjoin.IEJoin3((ArrayList<Value>) a, (ArrayList<Value>) b, get_outputQueue(), c);

	}

	private void createResultsIEjoin2(List<Value> a, List<Value> b, StockEvent c) throws IOException {
		IEjoin.IEJoin2((ArrayList<Value>) a, (ArrayList<Value>) b, get_outputQueue(), c);

	}

	/**
	 * Simple SEQ(A,B+,C) with A.price< B.price and from range query B.price <
	 * C.price
	 * 
	 * @param a
	 * @param b
	 * @param bop
	 * @param c
	 * @throws IOException
	 */
	private void createResultsABC(List<Value> a, List<Value> b, String bop, int c) throws IOException {

		int start = b.size() - 1;
		/// int end = b.size();
		int pos = 0;
		int numofsetbits = 0;
		int[] setbits = new int[b.size()];
		for (int i = a.size() - 1; i >= 0; i--) {
			setbits = new int[b.size()];

			numofsetbits = 0;
			start = b.size() - 1;
			// check.clear();
			pos = b.size() - 1;
			while (start >= 0) {
				if (b.get(start).compareTo(a.get(i), 1) >= 1) {

					setbits[pos] = start;
					pos--;

					// outputCombinations2(a.get(i).p, b, start, b.size(),
					// check);
					start--;
					// end--;
					numofsetbits++;
					/// matches over
					/// from start till the end of the list produce the patterns
				} /// here
				else if (b.get(start).compareTo(a.get(i), 1) < 1)
					break;
				else {
					start--;
				}

				if (numofsetbits > 0)
					outputCombinations(a.get(i).getPredicate(1), b, numofsetbits, b.size() - 1, c, setbits);

			}

		}

	}

	/**
	 * Another Query SEQ(A,B+,C) with A.price < B.price && A.vol > C.vol from
	 * range query B.price < c.Price
	 * 
	 * @param a
	 * @param b
	 * @param bop
	 * @param c
	 * @throws IOException
	 */

	private void createResultsABCvol(List<Value> a, List<Value> b, String bop, Event c) throws IOException {

		Collections.sort(a, new PriceComparator());

		Collections.sort(b, new PriceComparator());

		int start = b.size() - 1;
		/// int end = b.size();
		int pos = 0;
		int numofsetbits = 0;
		int[] setbits = new int[b.size()];
		for (int i = a.size() - 1; i >= 0; i--) {
			setbits = new int[b.size()];

			numofsetbits = 0;
			start = b.size() - 1;
			// check.clear();
			pos = b.size() - 1;
			while (start >= 0) {
				if (b.get(start).compareTo(a.get(i), 1) >= 1 && a.get(i).compareToEvent(c, 2) < 1) {

					setbits[pos] = start;
					pos--;

					// outputCombinations2(a.get(i).p, b, start, b.size(),
					// check);
					start--;
					// end--;
					numofsetbits++;
					/// matches over
					/// from start till the end of the list produce the patterns
				} /// here
				else if (b.get(start).compareTo(a.get(i), 1) < 1)
					break;
				else {
					start--;
				}

				if (numofsetbits > 0)
					outputCombinations(a.get(i).getPredicate(1), b, numofsetbits, b.size() - 1, ((StockEvent) c).price,
							setbits);

			}

		}

	}

	/**
	 * Using the pattern SEQ (A,B+,C) where A.price < B.price && A.vol > B.vol
	 */

	private void createResultsABCComplexJoin(List<Value> a, List<Value> b, String bop, Event c) throws IOException {

		Collections.sort(a, new TimeComparator());

		Collections.sort(b, new TimeComparator());

		int start = b.size() - 1;
		/// int end = b.size();
		int pos = 0;
		int numofsetbits = 0;
		int[] setbits; // = new int[b.size()];
		for (int i = a.size() - 1; i >= 0; i--) {
			setbits = new int[b.size()];

			numofsetbits = 0;
			start = b.size() - 1;

			pos = b.size() - 1;

			while (start >= 0) {
				if (b.get(start).compareTo(a.get(i), 0) >= 1 && b.get(start).compareTo(a.get(i), 1) >= 1
						&& b.get(start).compareTo(a.get(i), 2) < 0) {

					setbits[pos] = start;
					pos--;

					// outputCombinations2(a.get(i).p, b, start, b.size(),
					// check);
					start--;
					// end--;
					numofsetbits++;
					/// matches over
					/// from start till the end of the list produce the patterns
				} /// here
				else if (b.get(start).compareTo(a.get(i), 0) < 0)
					break;
				else {
					start--;
				}

			}
			
			if (numofsetbits > 0){
				System.out.println("before combinations"+i);
				outputCombinations(a.get(i).event.id, b, numofsetbits, b.size() - 1, ((StockEvent) c).id, setbits);
				System.out.println("after combinations"+i);
			}
				

		}

	}

	private void outputCombinations(int element, List<Value> b, int numofsetbits, int end, int c, int[] setbits)
			throws IOException {

		for (int i = 1; i < Math.pow(2, (numofsetbits)); i++) {

			BitSet bs = BitSet.valueOf(new long[] { i });
		//	 System.out.print(Integer.toString(element));
			get_outputQueue().add(Integer.toString(element));
			for (int j = bs.previousSetBit(numofsetbits); j >= 0; j = bs.previousSetBit(j - 1)) {
				// operate on index i here
				if (j == Integer.MIN_VALUE) {
					break; // or (i+1) would overflow
				}
				/// get the set bits from the array

				// outputWriter.write("," + b.get(setbits[end - j]));
		//		 System.out.print("," + b.get(setbits[end - j]).event.id);
				get_outputQueue().add("," + b.get(setbits[end - j]).event.id);
			}

			// System.out.println();
			// System.out.print("," + c + "\n");
			get_outputQueue().add("," + c);
			get_outputQueue().add("\n");

		}
		// System.out.println();
	}

	private List<Value> queryStore(StockEvent ce, ZIndex[] ranges) throws IOException {

		// /search over both the stores if they are not empty, first select the
		// secondary store

		EventStore store = (store_1.getStoreType() == 0) ? store_1 : store_2;

		List<Value> val = null;
		if (store.tree().hasData()) {

			val = store.tree().searchRangeWithintValue(ranges[0], ranges[1], minmaxInter);
		}

		// /select the primary datasource
		store = (store_1.getStoreType() == 1) ? store_1 : store_2;

		if (val != null)
			val.addAll(store.tree().searchRangeWithintValue(ranges[0], ranges[1], minmaxInter));
		else
			val = store.tree().searchRangeWithintValue(ranges[0], ranges[1], minmaxInter);

		return val;
	}

	private EventStore getStore(StockEvent c) throws IOException {

		EventStore store = (store_1.getStoreType() == 0) ? store_1 : store_2;

		if (window.startTimeofWindow > store.getEndTime() && !store.tree().hasData()) {

			store.tree().clear(); // /change it later, making it null and
		} // reinitialising a new tree

		store = (store_1.getStoreType() == 1) ? store_1 : store_2;

		if (c.timestamp > store.getEndTime()) {
			// /stop putting in this tree and select the other tree
			EventStore primary_store = (store_1.getStoreType() == 0) ? store_1 : store_2;

			store.setStoreType(0); // /make it a secondary store since the new
									// event will be outside its defined window
			primary_store.setStoreType(1); // make it the primary one and put
											// the event in this one
			primary_store.setStartTime(c.timestamp);
			primary_store.setEndTime(c.timestamp + window.windowLength);

			store = primary_store;
		}

		return store;

	}

	// /here 0 is timestamp 1 is price and 2 is volume
	private void updateMaxMin(int t, int p, int v) {

		if (t > minmax.max[0]) {

			minmax.max[0] = t;
		}

		if (t < minmax.min[0]) {

			minmax.min[0] = t;
		}

		if (p > minmax.max[1]) {

			minmax.max[1] = p;
		}

		if (p < minmax.min[1]) {

			minmax.min[1] = p;
		}

		if (v > minmax.max[2]) {

			minmax.max[2] = v;
		}

		if (v < minmax.min[2]) {

			minmax.min[2] = v;
		}

	}

	public static class PriceComparator implements Comparator<Value> {

		@Override
		public int compare(Value o1, Value o2) {
			if (((StockEvent) o1.event).price == ((StockEvent) o2.event).price)
				return 0;
			return ((StockEvent) o1.event).price - ((StockEvent) o2.event).price > 0 ? 1 : -1;
		}
	}

	public static class VolumeComparator implements Comparator<Value> {

		@Override
		public int compare(Value o1, Value o2) {
			if (((StockEvent) o1.event).vol == ((StockEvent) o2.event).vol)
				return 0;
			return ((StockEvent) o1.event).vol - ((StockEvent) o2.event).vol > 0 ? 1 : -1;
		}
	}

	public static class TimeComparator implements Comparator<Value> {

		@Override
		public int compare(Value o1, Value o2) {
			if (((StockEvent) o1.event).timestamp == ((StockEvent) o2.event).timestamp)
				return 0;
			return ((StockEvent) o1.event).timestamp - ((StockEvent) o2.event).timestamp > 0 ? 1 : -1;
		}
	}

}
