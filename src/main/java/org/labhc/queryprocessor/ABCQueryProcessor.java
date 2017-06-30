package org.labhc.queryprocessor;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.jdbm.BTree;
import org.apache.jdbm.DBAbstract;
import org.apache.jdbm.DBMaker;
import org.labhc.btreeUtils.Key;
import org.labhc.btreeUtils.Value;
import org.labhc.event.Event;
import org.labhc.event.EventType;
import org.labhc.event.StockEvent;
import org.labhc.windowsemantics.AbstractWindow;
import org.labhc.zvalueencoder.DimensionException;
import org.labhc.zvalueencoder.ZGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

;

public class ABCQueryProcessor extends AbstractQueryProcessor {

	final static Logger logger = LoggerFactory.getLogger(ABCQueryProcessor.class);

	private final BTree<Integer, Value> tree;

	private final BTree<Key, Value> intermediate;

	private ZGenerator zval;

	private ZGenerator zvalFull;

	private static int WINDOW = 20;

	// private CritBitKD<Event> cb = CritBit.createKD(64, 2);
	private int i = 0;

	// private RTree<Event, Point> rtree = RTree.maxChildren(5).create();

	private int maxBprice = 0, minBprice = Integer.MAX_VALUE, maxAvol = 0, minAvol = Integer.MAX_VALUE;

	public ABCQueryProcessor(LinkedBlockingQueue<Event> i, CountDownLatch l, AbstractWindow w) throws IOException {
		super(i, l, w);

		tree = BTree.createInstance((DBAbstract) DBMaker.openMemory().make());

		intermediate = BTree.createInstance((DBAbstract) DBMaker.openMemory().make());

		zval = new ZGenerator(2);

		zvalFull = new ZGenerator(4);

	}

	/*
	 * @Override protected void process(Event e) throws IOException,
	 * DimensionException {
	 * 
	 * i++; if (e.eventType == EventType.STOCKEVENT) {
	 * 
	 * StockEvent ce = (StockEvent) e;
	 * 
	 * // Integer key = zval.Zorder2d(ce.price, ce.vol);
	 * 
	 * // boolean[] re = zval.generate(new int[] { ce.price, ce.vol });
	 * 
	 * // KeyValue kv = new KeyValue(new Key(re), new Value(ce, //
	 * ce.timestamp));
	 * 
	 * updateMaxMin(ce.price, ce.vol);
	 * 
	 * // tree.insert(key, new Value(ce, ce.timestamp), false); // Process
	 * predicates and create matches
	 * 
	 * long[] key = new long[] { Long.valueOf(ce.price), Long.valueOf(ce.vol) //
	 * double dimension };
	 * 
	 * cb.putKD(key, ce);
	 * 
	 * // System.out.println(cb.size()); // if (i == 20) {
	 * createNewRangeQuery(ce); // }
	 * 
	 * }
	 * 
	 * }
	 */

	// @Override
	/*
	 * protected void process4(Event e) { i++; if (e.eventType ==
	 * EventType.STOCKEVENT) {
	 * 
	 * StockEvent ce = (StockEvent) e;
	 * 
	 * Integer key = zval.Zorder2d(ce.price, ce.vol);
	 * 
	 * // System.out.println("Price " + ce.price + "  Volume " + ce.vol);
	 * 
	 * // System.out.println("ZValue " + key); // boolean[] re =
	 * zval.generate(new int[] { ce.price, ce.vol });
	 * 
	 * // KeyValue kv = new KeyValue(new Key(re), new Value(ce, //
	 * ce.timestamp));
	 * 
	 * updateMaxMin(ce.price, ce.vol);
	 * 
	 * rtree = rtree.add(ce, Geometries.point(ce.price, ce.vol));
	 * 
	 * // Process predicates and create matches
	 * 
	 * if (i == 20) { createRangeForRTree(ce); }
	 * 
	 * } }
	 */

	/*
	 * private void createRangeForRTree(StockEvent ce) {
	 * 
	 * System.out.println(ce.price); Rectangle r =
	 * Geometries.rectangle(minBprice, minAvol, ce.price, maxAvol);
	 * 
	 * Observable<Entry<Event, Point>> entries = rtree.search(r);
	 * 
	 * entries.forEach((c) -> { System.out.println(c.geometry().toString()); //
	 * System.out.println("Here");
	 * 
	 * });
	 * 
	 * // System.out.println(entries.count());
	 * 
	 * }
	 */

	@Override
	protected void process(Event e) throws IOException, DimensionException {

		i++;
		if (e.eventType == EventType.STOCKEVENT) {

			StockEvent ce = (StockEvent) e;

			Integer key = zval.Zorder2d(ce.price, ce.vol);

			updateMaxMin(ce.price, ce.vol);

			tree.insert(key, new Value(ce, ce.timestamp), false);

			// Process predicates and create matches

			if (i == 20) {
				createRangeAndResultstest(ce);
			}

		}

	}

	private void createNewRangeQuery(StockEvent ce) {

		long[] min = new long[] { minBprice, minAvol };
		long[] max = new long[] { ce.price, maxAvol };
		// QueryIteratorKD<Event> it = cb.queryKD(min, max);

		// while (it.hasNext()) {

		// StockEvent se = (StockEvent) it.next();
		// System.out.println(se.price);
		// }

	}

	private void updateMaxMin(int p, int v) {

		if (p > maxBprice) {

			maxBprice = p;
		}

		if (p < minBprice) {

			minBprice = p;
		}

		if (v > maxAvol) {

			maxAvol = v;
		}

		if (v < minAvol) {

			minAvol = v;
		}

	}

	//
	// private void createRangeAndResults(StockEvent ce)
	// throws DimensionException, IOException {
	// // /create the range for B
	// Long minBrange = zval.generateLong(new int[] { 17, 20 });
	//
	// Long maxBRange = zval.generateLong(new int[] { 23, 1910 });
	//
	// // Key maxKeyB = new Key(maxBRange);
	//
	// // Key minKeyB = new Key(minBrange);
	//
	// List<Value> valB = tree.searchRange(minBrange, maxBRange);
	//
	// // /create the range for A
	//
	// Long minArange = zval.generateLong(new int[] { minBprice, ce.vol });
	//
	// Long maxArange = zval.generateLong(new int[] { maxBprice, maxAvol });
	//
	// // Key maxKeyA = new Key(maxArange);
	//
	// // Key minKeyA = new Key(minArange);
	//
	// List<Value> valA = tree.searchRange(minArange, maxArange);
	//
	// createMatchesSASESemantics(valA, valB, ce);
	//
	// }

	private void createRangeAndResultstest(StockEvent ce) throws DimensionException, IOException {
		// /create the range for B
		Integer minBrange = zval.Zorder2d(ce.price, minAvol);

		Integer maxBRange = zval.Zorder2d(maxBprice, maxAvol);

		List<Value> valB = tree.searchRange(minBrange, maxBRange);
		// /create the range for A

		Integer minArange = zval.Zorder2d(minBprice, minAvol);

		Integer maxARange = zval.Zorder2d(maxBprice, ce.vol);

		List<Value> valA = tree.searchRange(minArange, maxARange);

		createMatchesSASESemantics(valA, valB, ce);

	}

	private void createMatchesSASESemantics(List<Value> A, List<Value> B, StockEvent c) throws IOException {

		if (A != null && A.size() > 0 && B != null && B.size() > 0) {
			for (int i = 0; i < A.size(); i++) {
				StockEvent stEventA = (StockEvent) A.get(i).event;
				for (int j = 0; j < B.size(); j++) {

					StockEvent stEventB = (StockEvent) B.get(j).event;

					// System.out.println("A's Vol is " + stEventA.vol);

					// System.out.println("B's price is " + stEventB.price);

					// System.out.println("C's price " + e.price);

					// System.out.println("c's Vol " + e.vol);

					for (int t1 = 0; t1 < A.get(i).get_window().size(); t1++) {

						for (int t2 = 0; t2 < B.get(j).get_window().size(); t2++) {

							if (A.get(i).get_window().get(t1) < B.get(j).get_window().get(t2)) {

								System.out.println("A.Vol: " + stEventA.vol + "  C.vol: " + c.vol);

								System.out.println("B.price: " + stEventB.price + " c.price: " + c.price);

								System.out.println("#################### ");
								System.out.println();
								if (stEventA.vol < c.vol && c.price < stEventB.price) {

									System.out.println("Match");

									try {
										zvalFull.generate(new int[] { stEventA.timestamp, stEventA.price, stEventA.vol,
												stEventB.price, stEventB.vol });
									} catch (DimensionException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
									// /Store the matches in the intermediate
									// result set

								}

								// this.get_outputQueue().add(
								// Integer.toString(val.get(i)
								// .get_window().get(t1))
								// + " , "
								// + Integer.toString(val.get(j)
								// .get_window().get(t2)));

							}
						}

					}

				}

			}

		}

	}

	private void storeIntermediateResults(Key k) throws IOException {

		intermediate.insert(k, null, false);

		// /Add the Zaddress to the tree

	}

	private void searchIntermediateResults(StockEvent c) {

		// /create the max and min ranges based on the incoming event and the
		// other event

	}

}
