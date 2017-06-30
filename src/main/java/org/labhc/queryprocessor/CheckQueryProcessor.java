package org.labhc.queryprocessor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.jdbm.BTree;
import org.apache.jdbm.DBAbstract;
import org.apache.jdbm.DBMaker;
import org.labhc.btreeUtils.Value;
import org.labhc.event.CheckEvent;
import org.labhc.event.Event;
import org.labhc.event.EventType;
import org.labhc.windowsemantics.AbstractWindow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CheckQueryProcessor extends AbstractQueryProcessor {
	final static Logger logger = LoggerFactory.getLogger(CheckQueryProcessor.class);

	private final BTree<Integer, Value> tree;

	private int maxBprice = 0, minBprice = 0, maxAprice = 0, minAprice = 0;
	private int nbmatchs = 0;

	public CheckQueryProcessor(LinkedBlockingQueue<Event> i, CountDownLatch l, AbstractWindow w) throws IOException {
		super(i, l, w);

		tree = BTree.createInstance((DBAbstract) DBMaker.openMemory().make());

	}

	public void initialise() {

	}

	@Override
	protected void process(Event e) throws IOException {

		if (e.eventType == EventType.CHECKEVENT) {

			CheckEvent ce = (CheckEvent) e;
			// createMatchesSASESemantics(ce);

			this.tree.insert(ce.source, new Value(ce.source, ce.timestamp), false);

			createMatchesCETSemantics(ce);

		}
	}

	private void createMatchesCETSemantics(CheckEvent e) throws IOException {
		List<Value> val = tree.searchRange(e.source, e.source);

		ArrayList<StringBuilder> reList = new ArrayList<>();
		boolean ck = false;
		if (val != null) {
			for (int i = 0; i < val.size(); i++) {

				for (int j = 0; j < val.size(); j++) {
					for (int t1 = 0; t1 < val.get(i).get_window().size() - val.get(i).get_window().size() + 1; t1++) {

						StringBuilder result = new StringBuilder("");
						result.append(val.get(i).get_window().get(t1));
						for (int t2 = 0; t2 < val.get(j).get_window().size(); t2++) {

							if (val.get(i).get_window().get(t1) < val.get(j).get_window().get(t2)
									&& val.get(i) == val.get(j)) {
								ck = true;
								result.append(val.get(j).get_window().get(t2));
							}
						}
						// if (ck) {
						reList.add(result);
						ck = false;
						// }
					}

				}

			}

			this.get_outputQueue().add(reList.toString());

		}

	}

	private void createMatchesSASESemantics(CheckEvent e) throws IOException {
		List<Value> val = tree.searchRange(e.source, e.source);

		ArrayList<StringBuilder> reList = new ArrayList<>();
		boolean ck = false;
		if (val != null) {
			for (int i = 0; i < val.size(); i++) {

				for (int j = 0; j < val.size(); j++) {
					for (int t1 = 0; t1 < val.get(i).get_window().size() - val.get(i).get_window().size() + 1; t1++) {

						for (int t2 = 0; t2 < val.get(j).get_window().size(); t2++) {

							if (val.get(i).get_window().get(t1) < val.get(j).get_window().get(t2)
									&& val.get(i) == val.get(j)) {
								ck = true;

								this.get_outputQueue().add(Integer.toString(val.get(i).get_window().get(t1)) + " , "
										+ Integer.toString(val.get(j).get_window().get(t2)));

							}
						}

					}

				}

			}

		}

	}
}
