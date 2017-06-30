package org.labhc.eventstore;

import java.io.IOException;

import org.apache.jdbm.BTree;
import org.apache.jdbm.DBAbstract;
import org.apache.jdbm.DBMaker;

public class TreeEventStore<Key, Value> extends EventStore {

	public final BTree<Key, Value> tree;

	@SuppressWarnings("unchecked")
	public TreeEventStore(int stype, int stime, int etime, String struc, int id)
			throws IOException {
		super(stype, stime, etime, struc, id);
		tree = (BTree<Key, Value>) BTree.createInstance((DBAbstract) DBMaker
				.openMemory().make());

	}

	@Override
	public void refreshInfo(int currTime) {
		if (currTime > this.getEndTime())
			this.setStoreType(0);

	}

	public void closeTree() throws IOException {
		tree.clear();
	}

	@Override
	public BTree tree() {

		return tree;
	}

}
