package org.labhc.btreeUtils;

import java.io.IOException;

import org.apache.jdbm.BTree;
import org.apache.jdbm.DB;
import org.apache.jdbm.DBAbstract; //change back its visibility
import org.apache.jdbm.DBMaker;

public class Test {

	public static void main(String[] args) throws IOException {

		DB db = DBMaker.openMemory().make();
		BTree<Integer, Integer> tree = BTree.createInstance((DBAbstract) db);

		tree.insert(1, 1, true);

		tree.insert(5, 5, true);

		tree.insert(8, 8, true);

		tree.insert(9, 9, true);

		tree.insert(7, 7, true);

		tree.insert(3, 3, true);

		tree.insert(2, 2, true);
		//
		// for (int i = 0; i < 1000; i++) {
		//
		// tree.insert(i + 2, i + 2, true);
		// }
		//
		// // System.out.println(tree.get(3));
		//
		System.out.println(tree.searchRange(5, 12));

		db.close();

	}

}
