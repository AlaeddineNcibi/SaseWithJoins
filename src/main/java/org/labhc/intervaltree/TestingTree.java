package org.labhc.intervaltree;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;

import org.labhc.zvalueencoder.ZIndex;

public class TestingTree {

	public static void main(String[] args) {

		IntervalTree<RangeQuery> tree = new IntervalTree<>();

		int[] masks = new int[32];
		for (int n = 0; n < 32; n++) {
			masks[n] = 1 << n;
		}
		int[] z1 = new int[2];
		z1[0] = 1;
		z1[1] = 5;

		ZIndex zmin1 = new ZIndex(z1, 2, masks);

		z1 = new int[2];
		z1[0] = 2;
		z1[1] = 4;
		ZIndex zmax1 = new ZIndex(z1, 2, masks);

		System.out.println(zmax1.compareTo(zmin1));

		tree.insert(new RangeQuery(zmin1, zmax1));
		/////////////////////////////////////////

		z1 = new int[2];
		z1[0] = 2;
		z1[1] = 8;

		ZIndex zmin2 = new ZIndex(z1, 2, masks);

		z1 = new int[2];
		z1[0] = 4;
		z1[1] = 6;
		ZIndex zmax2 = new ZIndex(z1, 2, masks);
		tree.insert(new RangeQuery(zmin2, zmax2));
		///////////////////////////////////////

		z1 = new int[2];
		z1[0] = 1;
		z1[1] = 10;

		ZIndex zmin3 = new ZIndex(z1, 2, masks);

		z1 = new int[2];
		z1[0] = 8;
		z1[1] = 16;
		ZIndex zmax3 = new ZIndex(z1, 2, masks);
		tree.insert(new RangeQuery(zmin3, zmax3));
		/////////////////////////////////////////////

		z1 = new int[2];
		z1[0] = 1;
		z1[1] = 6;

		ZIndex zmin4 = new ZIndex(z1, 2, masks);

		z1 = new int[2];
		z1[0] = 5;
		z1[1] = 7;
		ZIndex zmax4 = new ZIndex(z1, 2, masks);

		tree.insert(new RangeQuery(zmin4, zmax4));
		/////////////////////////////////////////

		z1 = new int[2];
		z1[0] = 2;
		z1[1] = 5;

		ZIndex zminq = new ZIndex(z1, 2, masks);

		z1 = new int[2];
		z1[0] = 2;
		z1[1] = 10;
		ZIndex zmaxq = new ZIndex(z1, 2, masks);

		// );

		/// Query///

		tree.overlappers(new RangeQuery(zminq, zmaxq))
				.forEachRemaining(x -> System.out.println(x.start() + " " + x.end()));

		ArrayList<Integer> e = new ArrayList<>();
		e.add(1);
		e.add(2);
		e.add(3);
		Enumeration<Integer> em = Collections.enumeration(e);

		while (em.hasMoreElements()) {
			System.out.println(em.nextElement());
		}

		System.out.println(em);

	}

}
