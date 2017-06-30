package org.labhc.queryprocessor;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;

import org.labhc.btreeUtils.Value;
import org.labhc.event.Event;
import org.labhc.event.EventType;
import org.labhc.event.StockEvent;
import org.labhc.eventstore.EventStore;
import org.labhc.windowsemantics.AbstractWindow;
import org.labhc.zvalueencoder.DimensionException;
import org.labhc.zvalueencoder.ZIndex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StockQueryProcessor extends AbstractQueryProcessor {
	final static Logger logger = LoggerFactory.getLogger(StockQueryProcessor.class);

	private int i = 0;

	public StockQueryProcessor(LinkedBlockingQueue<Event> i, CountDownLatch l, AbstractWindow w) throws IOException {
		super(i, l, w);

	}

	@Override
	protected void process(Event e) throws IOException, DimensionException {

		i++;
		window.initliaseWindow();
		if (e.eventType == EventType.STOCKEVENT) {

			StockEvent ce = (StockEvent) e;

			// Integer key = zval.Zorder2d(ce.price, ce.vol);
			ZIndex key = new ZIndex(new int[] { ce.price, ce.vol }, 2, masks);
			updateMaxMin(ce.price, ce.vol);
			window.updateWindow(ce.timestamp);

			EventStore store = getStore(ce);

			store.tree().insert(key, new Value(ce, ce.timestamp), false);

			// / window should be processed
			// after the query processing

			// Process predicates and create matches

			if (i == 20)

				stockABCQuery(ce);

		}

	}

	private EventStore getStore(StockEvent c) throws IOException {

		// /check first if the primary store's timestamp is within the
		// timewindow

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

	private void stockABCQuery(StockEvent ce) throws IOException, DimensionException {
		ZIndex[] ranges = createRangesForA(ce);
		List<Value> valA = queryStore(ce, ranges);

		if (valA != null)
			// System.out.println("A Size "+valA.size());
			// testOutput(valA, "A");
			ranges = createRangesForB(ce);
		List<Value> valB = queryStore(ce, ranges);

		if (valB != null)
			// System.out.println("B Size " +valB.size());

			// testOutput(valB, "B");

			createMatchesSASESemantics(valA, valB, ce);

	}

	private void testOutput(List<Value> val, String e) {

		System.out.println(e);

		for (Value v : val) {

			System.out.println(((StockEvent) v.event).price + " " + ((StockEvent) v.event).vol);
		}
	}

	private ZIndex[] createRangesForA(StockEvent ce) {
		// /For now hardcoded
		// /For A Sequence;
		ZIndex minZRange = new ZIndex(new int[] { minmax.min[0], minmax.min[1] }, 2, masks);

		ZIndex maxZRange = new ZIndex(new int[] { minmax.max[0], ce.vol - 1 }, 2, masks);

		// Integer minArange = zval.Zorder2d(minmax.min[0], minmax.min[1]);
		// Integer maxARange = zval.Zorder2d(minmax.max[0], ce.vol);

		return new ZIndex[] { minZRange, maxZRange };

	}

	private ZIndex[] createRangesForB(StockEvent ce) {

		ZIndex minZRange = new ZIndex(new int[] { ce.price, minmax.min[1] }, 2, masks);

		ZIndex maxZRange = new ZIndex(new int[] { minmax.max[0], minmax.max[1] }, 2, masks);

		// For B Sequence
		// /create the range for B
		// Integer minBrange = zval.Zorder2d(ce.price, minmax.min[1]);

		// Integer maxBRange = zval.Zorder2d(minmax.max[0], minmax.max[1]);
		return new ZIndex[] { minZRange, maxZRange };
	}

	private List<Value> queryStore(StockEvent ce, ZIndex[] ranges) throws IOException {

		// /search over both the stores if they are not empty, first select the
		// secondary store

		EventStore store = (store_1.getStoreType() == 0) ? store_1 : store_2;

		List<Value> val = null;
		if (store.tree().hasData()) {

			// store.tree().searchRangeWithNJI(ranges[0], ranges[1],2);
			// searchRangeWithValues
		//	val = store.tree().searchRangeWithintValue(ranges[0], ranges[1]);

		}

		// /select the primary datasource
		store = (store_1.getStoreType() == 1) ? store_1 : store_2;

		//if (val != null)

		//	val.addAll(store.tree().searchRangeWithintValue(ranges[0], ranges[1]));
	//	else
		//	val = store.tree().searchRangeWithintValue(ranges[0], ranges[1]);

		return val;
	}

	/*
	 * private void createRangeAndResultstest(StockEvent ce) throws
	 * DimensionException, IOException { // /create the range for B Integer
	 * minBrange = zval.Zorder2d(ce.price, minmax.min[1]);
	 * 
	 * // long minBrange = sf.computeZCode(new int[] { ce.price, minmax.min[1]
	 * // }, // 32);
	 * 
	 * Integer maxBRange = zval.Zorder2d(minmax.max[0], minmax.max[1]);
	 * 
	 * List<Value> valB = store_1.tree().searchRange(minBrange, maxBRange); //
	 * /create the range for A
	 * 
	 * Integer minArange = zval.Zorder2d(minmax.min[0], minmax.min[1]);
	 * 
	 * // long minArange = sf.computeZCode(new int[] { minmax.min[0], //
	 * minmax.min[1] }, 32);
	 * 
	 * Integer maxARange = zval.Zorder2d(minmax.max[0], ce.vol);
	 * 
	 * // long maxARange = sf.computeZCode(new int[] { minmax.max[0], ce.vol },
	 * // 32);
	 * 
	 * // List<Integer> keys = tree.searchrangeKeys(minArange, maxARange);
	 * 
	 * // checkZRanges(keys, ce);
	 * 
	 * List<Value> valA = store_1.tree().searchRange(minArange, maxARange);
	 * 
	 * createMatchesSASESemantics(valA, valB, ce);
	 * 
	 * }
	 */
	private void createMatchesSASESemantics(List<Value> A, List<Value> B, StockEvent c)
			throws IOException, DimensionException {

		if (A != null && A.size() > 0 && B != null && B.size() > 0) {
			for (int i = 0; i < A.size(); i++) {
				StockEvent stEventA = (StockEvent) A.get(i).event;
				for (int j = 0; j < B.size(); j++) {

					StockEvent stEventB = (StockEvent) B.get(j).event;

					for (int t1 = 0; t1 < A.get(i).get_window().size(); t1++) {

						for (int t2 = 0; t2 < B.get(j).get_window().size(); t2++) {

							if (A.get(i).get_window().get(t1) < B.get(j).get_window().get(t2)) {

								if (stEventA.vol < c.vol && c.price < stEventB.price) {

									// System.out.println("Match");
									// System.out.println();
									// System.out.println("A.Vol: " +
									// stEventA.vol
									// + " C.vol: " + c.vol);
									// //
									// System.out.println("B.price: "
									// + stEventB.price + " c.price: "
									// + c.price);
									//
									// System.out.println("####################
									// ");
									// System.out.println();

									// /create a key with the z-address values
									// of all the predicate of matched events

									// int[] vals = new int[] {
									// stEventA.timestamp, stEventA.price,
									// stEventA.vol, stEventB.price,
									// stEventB.vol };
									//
									// int[] key = new int[] {
									// stEventA.timestamp,
									// stEventA.vol, stEventB.price };
									//
									// boolean[] b = zvalFull.generate(key);
									// // Key k = new Key(b);
									// Long k = zval.interleaveBitsToLong(3,
									// new int[] { stEventA.timestamp,
									// stEventA.vol,
									// stEventB.price });
									// maxminIntermediateResults(vals);
									// storeIntermediateResults(k, vals);

								}

							}
						}

					}

				}

			}

		}

	}

	private void maxminIntermediateResults(int[] vals) {

		for (int i = 0; i < vals.length; i++) {

			if (vals[i] > minmaxInter.max[i])
				minmaxInter.max[i] = vals[i];

		}

		for (int i = 0; i < vals.length; i++) {
			if (vals[i] < minmaxInter.min[i])
				minmaxInter.min[i] = vals[i];
		}

	}

	private void storeIntermediateResults(Long k, int[] vals) throws IOException {

		// /Add the Zaddress to the tree
		intermediate.insert(k, vals, false);

	}

	private void searchIntermediateResults(StockEvent c) throws DimensionException, IOException {

		// /create the max and min ranges based on the incoming event and the
		// other event

		// create the ranges, take the timestamp from the window

		// int[] lkey = new int[] { window.startTimeofWindow,
		// minmaxInter.min[1],
		// minmaxInter.min[2], c.price, minmaxInter.min[4] };

		// int[] lkey = new int[] { window.startTimeofWindow,
		// minmaxInter.min[1],
		// c.price };

		// int[] hkey = new int[] { minmaxInter.max[0], minmaxInter.max[1],
		// c.vol,
		// minmaxInter.max[3], minmaxInter.max[4] };

		// int[] hkey = new int[] { minmaxInter.max[0], c.vol,
		// minmaxInter.max[3] };

		// boolean[] srange = zvalFull.generate(lkey);

		// boolean[] erange = zvalFull.generate(hkey);

		Long k1 = zval.interleaveBitsToLong(3, new int[] { window.startTimeofWindow, minmaxInter.min[1], c.price });
		Long k2 = zval.interleaveBitsToLong(3, new int[] { minmaxInter.max[0], c.vol, minmaxInter.max[3] });

		List<int[]> output = intermediate.searchRange(k1, k2);

		outputIntermediateResults(output, c);

	}

	private void outputIntermediateResults(List<int[]> output, StockEvent c) {

		System.out.println("Intermediate ResultSets " + output.size());
		System.out.println();

		int time = 0;
		for (int[] re : output) {

			if (window.startTimeofWindow <= re[0])
				time++;

			/*
			 * System.out.println("Match"); System.out.println();
			 * System.out.println("A.Vol: " + re[2] + "  C.vol: " + c.vol);
			 * 
			 * System.out.println("B.price: " + re[3] + " c.price: " + c.price);
			 * 
			 * System.out.println("#################### ");
			 * System.out.println();
			 */

		}

		System.out.println("Total " + time);

	}

	// /here 0 is price and 1 is volume
	private void updateMaxMin(int p, int v) {

		if (p > minmax.max[0]) {

			minmax.max[0] = p;
		}

		if (p < minmax.min[0]) {

			minmax.min[0] = p;
		}

		if (v > minmax.max[1]) {

			minmax.max[1] = v;
		}

		if (v < minmax.min[1]) {

			minmax.min[1] = v;
		}

	}

}
