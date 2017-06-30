package org.labhc.queryprocessor;

import java.io.IOException;
import java.util.BitSet;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.jdbm.BTree;
import org.apache.jdbm.DBAbstract;
import org.apache.jdbm.DBMaker;
import org.labhc.btreeUtils.MaxAndMinValues;
import org.labhc.btreeUtils.Value;
import org.labhc.event.Event;
import org.labhc.event.EventType;
import org.labhc.eventstore.EventStore;
import org.labhc.eventstore.TreeEventStore;
import org.labhc.windowsemantics.AbstractWindow;
import org.labhc.zvalueencoder.DimensionException;
import org.labhc.zvalueencoder.ZGenerator;
import org.labhc.zvalueencoder.ZIndex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractQueryProcessor implements Runnable {

	

//	final static Logger logger = LoggerFactory.getLogger(AbstractQueryProcessor.class);
	private final LinkedBlockingQueue<Event> _inputQueue;

	private final LinkedBlockingQueue<String> _outputQueue = new LinkedBlockingQueue<>();
	private CountDownLatch latch;

	// protected final BTree<Integer, Value> tree;

	protected final BTree<Long, int[]> intermediate;

	protected final EventStore store_1;
	protected final EventStore store_2;

	protected final EventStore pattern_store_1;
	protected final EventStore pattern_store_2;

	protected ZGenerator zval;

	protected ZGenerator zvalFull;

	protected MaxAndMinValues minmax;

	protected MaxAndMinValues minmaxInter;

	protected AbstractWindow window;

	static int[] masks = new int[32];
	public static BitSet[] BitSetMasks;

	public AbstractQueryProcessor(LinkedBlockingQueue<Event> i, CountDownLatch l, AbstractWindow w) throws IOException {
		this._inputQueue = i;
		latch = l;
		store_1 = new TreeEventStore<ZIndex, Value>(1, 0, w.windowLength, "tree", 1);
		store_2 = new TreeEventStore<ZIndex, Value>(0, 0, w.windowLength, "tree", 2);
		pattern_store_1 = new TreeEventStore<Long, int[]>(1, 0, w.windowLength, "tree", 3);

		pattern_store_2 = new TreeEventStore<Long, int[]>(0, 0, w.windowLength, "tree", 4);

		intermediate = BTree.createInstance((DBAbstract) DBMaker.openMemory().make());
		window = w;

		// /Make these generic later

		zval = new ZGenerator(2);

		zvalFull = new ZGenerator(3);

		minmax = new MaxAndMinValues(3);

		minmaxInter = new MaxAndMinValues(3);

		/// New Updates
		int dim = 6;
		for (int n = 0; n < 32; n++) {
			masks[n] = 1 << n;
		}

		BitSetMasks = new BitSet[dim - 1];
		for (int j = 0; j < BitSetMasks.length; j++) {

			BitSetMasks[j] = new BitSet(dim * 32);

			int knowndim = j + 1;

			BitSetMasks[j] = generateMask(BitSetMasks[j], dim, knowndim, dim * 32);

		}

	}

	@Override
	public void run() {
//		logger.info("Starting Query Process");
		for (;;) {
			Event e = null;
			try {
				e = _inputQueue.take();
			} catch (InterruptedException exception) {
				// TODO Auto-generated catch block
				exception.printStackTrace();
			}
			if (e.eventType == EventType.POISONPILL) {
				break;
			} else {
				try {
					process(e);
				} catch (IOException | DimensionException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		}

		finish();

	}

	protected void finish() {

		_outputQueue.add(EventType.POISONPILL.toString());
		latch.countDown();
	}

	protected abstract void process(Event e) throws IOException, DimensionException;

	public LinkedBlockingQueue<String> get_outputQueue() {
		return _outputQueue;
	}

	public BitSet generateMask(BitSet b, int nbOfDimension, int t, int start) {

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

}
