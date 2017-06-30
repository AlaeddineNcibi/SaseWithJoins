package org.labhc.intervaltree;

import org.labhc.zvalueencoder.ZIndex;

/**
 * Closed-open, [), interval on the integer number line.
 * 
 * https://github.com/masonmlai/interval-tree
 */
public interface Interval extends Comparable<Interval> {

	/**
	 * Returns the starting point of this.
	 */
	ZIndex start();

	/**
	 * Returns the ending point of this.
	 * <p>
	 * The interval does not include this point.
	 */
	ZIndex end();

	/**
	 * Returns the length of this.
	 */
	// default int length() {
	// // end() - start()
	// return 0;
	// }

	/**
	 * Returns if this interval is adjacent to the specified interval.
	 * <p>
	 * Two intervals are adjacent if either one ends where the other starts.
	 * 
	 * @param interval
	 *            - the interval to compare this one to
	 * @return if this interval is adjacent to the specified interval.
	 */
	default boolean isAdjacent(Interval other) {

		// TODO: start() == other.end() || end() == other.start();

		return start().compareTo(other.end()) == 0 || end().compareTo(other.start()) == 0;// start()
																							// ==
																							// other.end()
																							// ||
																							// end()
																							// ==
																							// other.start();
	}

	default boolean overlaps(Interval o) {

		return end().compareTo(o.start()) >= 1 && o.end().compareTo(start()) >= 1;

		// return end() > o.start() && o.end() > start();
	}

	default int compareTo(Interval o) {

		int c = start().compareTo(o.start());
		if (c == 0) {

			return end().compareTo(o.end());
		}

		return c;

		/*
		 * if (start() > o.start()) { return 1; } else if (start() < o.start())
		 * { return -1; } else if (end() > o.end()) { return 1; } else if (end()
		 * < o.end()) { return -1; } else { return 0; }
		 */
	}
}
