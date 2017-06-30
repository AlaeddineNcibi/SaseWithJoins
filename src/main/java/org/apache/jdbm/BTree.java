/*******************************************************************************
 * Copyright 2010 Cees De Groot, Alex Boisvert, Jan Kotek
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package org.apache.jdbm;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOError;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.labhc.btreeUtils.MaxAndMinValues;
import org.labhc.btreeUtils.Value;
import org.labhc.zvalueencoder.ZIndex;

/**
 * B+Tree persistent indexing data structure. B+Trees are optimized for
 * block-based, random I/O storage because they store multiple keys on one tree
 * node (called <code>BTreeNode</code>). In addition, the leaf nodes directly
 * contain (inline) small values associated with the keys, allowing a single (or
 * sequential) disk read of all the values on the node.
 * <p/>
 * B+Trees are n-airy, yeilding log(N) search cost. They are self-balancing,
 * preventing search performance degradation when the size of the tree grows.
 * <p/>
 * BTree stores its keys sorted. By default JDBM expects key to implement
 * <code>Comparable</code> interface but user may supply its own
 * <code>Comparator</code> at BTree creation time. Comparator is serialized and
 * stored as part of BTree.
 * <p/>
 * The B+Tree allows traversing the keys in forward and reverse order using a
 * TupleBrowser obtained from the browse() methods. But it is better to use
 * <code>BTreeMap</code> wrapper which implements <code>SortedMap</code>
 * interface
 * <p/>
 * This implementation does not directly support duplicate keys. It is possible
 * to handle duplicates by grouping values using an ArrayList as value. This
 * scenario is supported by JDBM serialization so there is no big performance
 * penalty.
 * <p/>
 * There is no limit on key size or value size, but it is recommended to keep
 * keys as small as possible to reduce disk I/O. If serialized value exceeds 32
 * bytes, it is stored in separate record and tree contains only recid reference
 * to it. BTree uses delta compression for its keys.
 *
 *
 * @author Alex Boisvert
 * @author Jan Kotek
 * @author Syed Gillani
 */
public class BTree<K, V> {

	private static final boolean DEBUG = false;

	/**
	 * Default node size (number of entries per node)
	 */
	public static final int DEFAULT_SIZE = 4; // TODO test optimal size, it has
												// serious impact on sequential
												// write and read 32

	/**
	 * Record manager used to persist changes in BTreeNodes
	 */
	protected transient DBAbstract _db;

	/**
	 * This BTree's record ID in the DB.
	 */
	private transient long _recid;

	/**
	 * Comparator used to index entries (optional)
	 */
	protected Comparator<K> _comparator;

	/**
	 * Serializer used to serialize index keys (optional)
	 */
	protected Serializer<K> keySerializer;

	/***
	 * The result set for the Range Queries
	 */
	// private ArrayList<V> _rvalues = new ArrayList<>();

	/**
	 * Serializer used to serialize index values (optional)
	 */
	protected Serializer<V> valueSerializer;

	/**
	 * indicates if values should be loaded during deserialization, set to false
	 * during defragmentation
	 */
	boolean loadValues = true;

	/** if false map contains only keys, used for set */
	boolean hasValues = true;

	/**
	 * The number of structural modifications to the tree for fail fast
	 * iterators. This value is just for runtime, it is not persisted
	 */
	transient int modCount = 0;

	/**
	 * cached instance of an insert result, so we do not have to allocate new
	 * object on each insert
	 */
	/**
	 * TODO investigate performance impact of removing this
	 */
	protected BTreeNode.InsertResult<K, V> insertResultReuse;

	public Serializer<K> getKeySerializer() {
		return keySerializer;
	}

	public Serializer<V> getValueSerializer() {
		return valueSerializer;
	}

	/**
	 * Height of the B+Tree. This is the number of BTreeNodes you have to
	 * traverse to get to a leaf BTreeNode, starting from the root.
	 */
	private int _height;

	/**
	 * Recid of the root BTreeNode
	 */
	private transient long _root;

	/**
	 * Total number of entries in the BTree
	 */
	protected volatile long _entries;

	/**
	 * Serializer used for BTreeNodes of this tree
	 */
	private transient BTreeNode<K, V> _nodeSerializer = new BTreeNode();
	{
		_nodeSerializer._btree = this;
	}

	/**
	 * Listeners which are notified about changes in records
	 */
	protected RecordListener[] recordListeners = new RecordListener[0];

	final protected ReadWriteLock lock = new ReentrantReadWriteLock();

	/**
	 * No-argument constructor used by serialization.
	 */
	public BTree() {
		// empty
	}

	public boolean hasData() throws IOException {
		// BTreeNode<K, V> rootNode = getRoot();

		return getRoot() == null;
	}

	/**
	 * Create a new persistent BTree
	 */
	@SuppressWarnings("unchecked")
	public static <K extends Comparable, V> BTree<K, V> createInstance(DBAbstract db) throws IOException {
		return createInstance(db, null, null, null, true);
	}

	/**
	 * Create a new persistent BTree
	 */
	public static <K, V> BTree<K, V> createInstance(DBAbstract db, Comparator<K> comparator,
			Serializer<K> keySerializer, Serializer<V> valueSerializer, boolean hasValues) throws IOException {
		BTree<K, V> btree;

		if (db == null) {
			throw new IllegalArgumentException("Argument 'db' is null");
		}

		btree = new BTree<K, V>();
		btree._db = db;
		btree._comparator = comparator;
		btree.keySerializer = keySerializer;
		btree.valueSerializer = valueSerializer;
		btree.hasValues = hasValues;
		btree._recid = db.insert(btree, btree.getRecordManager().defaultSerializer(), false);

		return btree;
	}

	/**
	 * Load a persistent BTree.
	 *
	 * @param db
	 *            DB used to store the persistent btree
	 * @param recid
	 *            Record id of the BTree
	 */
	@SuppressWarnings("unchecked")
	public static <K, V> BTree<K, V> load(DBAbstract db, long recid) throws IOException {
		BTree<K, V> btree = (BTree<K, V>) db.fetch(recid);
		btree._recid = recid;
		btree._db = db;
		btree._nodeSerializer = new BTreeNode<K, V>();
		btree._nodeSerializer._btree = btree;
		return btree;
	}

	/**
	 * Get the {@link ReadWriteLock} associated with this BTree. This should be
	 * used with browsing operations to ensure consistency.
	 *
	 * @return
	 */
	public ReadWriteLock getLock() {
		return lock;
	}

	/**
	 * Insert an entry in the BTree.
	 * <p/>
	 * The BTree cannot store duplicate entries. An existing entry can be
	 * replaced using the <code>replace</code> flag. If an entry with the same
	 * key already exists in the BTree, its value is returned.
	 *
	 * @param key
	 *            Insert key
	 * @param value
	 *            Insert value
	 * @param replace
	 *            Set to true to replace an existing key-value pair.
	 * @return Existing value, if any.
	 */
	public V insert(final K key, final V value, final boolean replace) throws IOException {
		if (key == null) {
			throw new IllegalArgumentException("Argument 'key' is null");
		}
		if (value == null) {
			throw new IllegalArgumentException("Argument 'value' is null");
		}
		// try {
		// lock.writeLock().lock();
		BTreeNode<K, V> rootNode = getRoot();

		if (rootNode == null) {
			// BTree is currently empty, create a new root BTreeNode
			if (DEBUG) {
				System.out.println("BTree.insert() new root BTreeNode");
			}
			rootNode = new BTreeNode<K, V>(this, key, value);
			_root = rootNode._recid;
			_height = 1;
			_entries = 1;
			_db.update(_recid, this);
			modCount++;
			// notifi listeners
			for (RecordListener<K, V> l : recordListeners) {
				l.recordInserted(key, value);
			}
			return null;
		} else {
			BTreeNode.InsertResult<K, V> insert = rootNode.insert(_height, key, value, replace);
			boolean dirty = false;
			if (insert._overflow != null) {
				// current root node overflowed, we replace with a new root
				// node
				if (DEBUG) {
					System.out.println("BTreeNode.insert() replace root BTreeNode due to overflow");
				}
				rootNode = new BTreeNode<K, V>(this, rootNode, insert._overflow);
				_root = rootNode._recid;
				_height += 1;
				dirty = true;
			}
			if (insert._existing == null) {
				_entries++;
				modCount++;
				dirty = true;
			}
			if (dirty) {
				_db.update(_recid, this);
			}
			// notify listeners
			for (RecordListener<K, V> l : recordListeners) {
				if (insert._existing == null)
					l.recordInserted(key, value);
				else
					l.recordUpdated(key, insert._existing, value);
			}

			// insert might have returned an existing value
			V ret = insert._existing;
			// zero out tuple and put it for reuse
			insert._existing = null;
			insert._overflow = null;
			this.insertResultReuse = insert;
			return ret;
		}
		// } finally {
		// lock.writeLock().unlock();
		// }
	}

	/**
	 * Remove an entry with the given key from the BTree.
	 *
	 * @param key
	 *            Removal key
	 * @return Value associated with the key, or null if no entry with given key
	 *         existed in the BTree.
	 */
	public V remove(K key) throws IOException {
		if (key == null) {
			throw new IllegalArgumentException("Argument 'key' is null");
		}
		// try {
		// lock.writeLock().lock();
		BTreeNode<K, V> rootNode = getRoot();
		if (rootNode == null) {
			return null;
		}
		boolean dirty = false;
		BTreeNode.RemoveResult<K, V> remove = rootNode.remove(_height, key);
		if (remove._underflow && rootNode.isEmpty()) {
			_height -= 1;
			dirty = true;

			_db.delete(_root);
			if (_height == 0) {
				_root = 0;
			} else {
				_root = rootNode.loadLastChildNode()._recid;
			}
		}
		if (remove._value != null) {
			_entries--;
			modCount++;
			dirty = true;
		}
		if (dirty) {
			_db.update(_recid, this);
		}
		if (remove._value != null)
			for (RecordListener<K, V> l : recordListeners)
				l.recordRemoved(key, remove._value);
		return remove._value;
		// } finally {
		// lock.writeLock().unlock();
		// }
	}

	public ArrayList<K> searchrangeKeys(K k1, K k2) throws IOException {

		if (k1 == null || k2 == null) {
			throw new IllegalArgumentException("Argument 'key' is null");
		}

		// lock.readLock().lock();
		BTreeNode<K, V> rootNode = getRoot();
		if (rootNode == null) {
			// return null;
		}
		ArrayList<K> _rvalues = new ArrayList<>();
		// _rvalues.clear();

		BTreeNode<K, V> start = rootNode.findRange(_height, k1);

		byte indexLow = start.findChildren(k1, true);

		byte indexHigh = start.findChildren(k2, true);

		while (indexLow <= indexHigh) {

			_rvalues.add((K) start._keys[indexLow]);
			indexLow++;
		}

		// boolean low=false,high=false;

		// /Just deal with this node and get the low and high values, If
		// high values are not
		// obtained from this, then
		if (start._next == 0 || ((Comparable) start._keys[indexHigh]).compareTo(k2) >= 0)
			return _rvalues;

		while (start._next != 0) { // /do something for a Tree having just
									// one node

			start = start.loadNode(start._next);

			indexHigh = start.findChildren(k2, true);
			// if the maximum is found then return true
			indexLow = start._first;

			while (indexLow <= indexHigh) {
				_rvalues.add((K) start._keys[indexLow]);
				indexLow++;
			}

			if (((Comparable) start._keys[indexHigh]).compareTo(k2) >= 0) {

				break;
			}

			// start.dump(_height);
		}

		return _rvalues;

	}

	private boolean queryBoxcheck(int v, int k1, int k2) {

		if ((v | k1) != v) {
			return false;
		}

		if ((v & k2) != v) {
			return false;
		}

		return true;
	}

	private int nextaddress(int v, int k1, int k2) {

		int v2 = v | (~k1);

		v2++;

		v2 = (v2 & k2) | k1;

		return v2;

	}

	private final int compare(final K value1, final K value2) {
		if (value1 == null) {
			return 1;
		}
		if (value2 == null) {
			return -1;
		}

		return ((Comparable) value1).compareTo(value2);

	}

	public ArrayList<V> searchRangeWithintValue(K k1, K k2, MaxAndMinValues minMaxInter) throws IOException {

		if (k1 == null || k2 == null) {
			throw new IllegalArgumentException("Argument 'key' is null");
		}

		// try {
		// lock.readLock().lock();
		BTreeNode<K, V> rootNode = getRoot();

		if (rootNode == null) {
			return null;
		}
		ArrayList<V> _rvalues = new ArrayList<>();
		// _rvalues.clear();

		BTreeNode<K, V> start = rootNode.findRange(_height, k1);

		if (start == null)
			return null;

		byte indexLow = start.findChildren(k1, true);
		int[] minValue = ((ZIndex) k1).getValues();
		int[] maxValue = ((ZIndex) k2).getValues();
		// you don't really need this
		// byte indexHigh = start.findChildren(k2, true);

		while (indexLow <= DEFAULT_SIZE - 1) {

			if (start._keys[indexLow] != null && ((Comparable) start._keys[indexLow]).compareTo(k2) <= 0) {

				/// over here check if this value can be inside
				boolean violation = false;
				// start.dumpRecursive(_height, 0);
				int[] current = ((ZIndex) start._keys[indexLow]).getValues();

				for (int i = 0; i < current.length; i++) {

					if (minValue[i] > current[i] || current[i] > maxValue[i]) {
						violation = true;
						break;
					}
				}
				if (!violation) {
					//((Value) start._values[indexLow]).
					minMaxInter.updateMaxMin(((Value) start._values[indexLow]).getTimestamp(), ((Value) start._values[indexLow]).getPredicate(1), ((Value) start._values[indexLow]).getPredicate(2));
					_rvalues.add((V) start._values[indexLow]);
				}

			}

			indexLow++;

		}

		/*
		 * while (indexLow <= indexHigh) {
		 * 
		 * _rvalues.add((V) start._values[indexLow]);
		 * 
		 * indexLow++; }
		 */

		// boolean low=false,high=false;

		// /Just deal with this node and get the low and high values, If
		// high values are not
		// obtained from this, then

		if (indexLow >= DEFAULT_SIZE)
			indexLow = (byte) (DEFAULT_SIZE - 1);

		if (start._next == 0 || ((Comparable) start._keys[indexLow]).compareTo(k2) >= 0)
			return _rvalues;

		while (start._next != 0) { // /do something for a Tree having just
									// one node

			start = start.loadNode(start._next);

			byte indexHigh = start.findChildren(k2, true);
			// if the maximum is found then return true
			indexLow = start._first;

			while (indexLow <= indexHigh && start._values[indexLow] != null) {
				boolean violation = false;
				// start.dumpRecursive(_height, 0);
				int[] current = ((ZIndex) start._keys[indexLow]).getValues();

				for (int i = 0; i < current.length; i++) {

					if (minValue[i] > current[i] || current[i] > maxValue[i]) {
						violation = true;
						break;
					}
				}
				if (!violation) {
					minMaxInter.updateMaxMin(((Value) start._values[indexLow]).getTimestamp(), ((Value) start._values[indexLow]).getPredicate(1), ((Value) start._values[indexLow]).getPredicate(2));

					_rvalues.add((V) start._values[indexLow]);
				}
				indexLow++;

			}

			if (start._keys[indexHigh] != null && ((Comparable) start._keys[indexHigh]).compareTo(k2) >= 0) {

				break;
			}

		}

		return _rvalues;

	}

	public ArrayList<V> searchRangeWithValues(K k1, K k2) throws IOException {

		if (k1 == null || k2 == null) {
			throw new IllegalArgumentException("Argument 'key' is null");
		}
		// try {
		// lock.readLock().lock();
		BTreeNode<K, V> rootNode = getRoot();
		if (rootNode == null)
			return null;

		// _rvalues.clear();

		BTreeNode<K, V> start = rootNode.findRange(_height, k1);
		BTreeNode<K, V> end = rootNode.findRange(_height, k2);
		if (start == null)
			return null;
		ArrayList<V> _rvalues = new ArrayList<>();
		boolean violation = false;
		int[] current;
		int[] minValue = ((ZIndex) k1).getValues();
		int[] maxValue = ((ZIndex) k2).getValues();
		byte indexLow = start.findChildren(k1, true);
		byte indexHigh = start.findChildren(k2, true);
		while (start != null) {
			indexLow = start.findChildren(k1, true);
			indexHigh = start.findChildren(k2, true);

			while (indexLow <= indexHigh) {
				violation = false;
				if (start._keys[indexLow] == null)
					return _rvalues;
				current = ((ZIndex) start._keys[indexLow]).getValues();
				for (int i = 0; i < current.length; i++) {

					if (minValue[i] > current[i] || current[i] > maxValue[i]) {
						violation = true;
						break;
					}
				}
				if (!violation) {
					_rvalues.add((V) start._values[indexLow]);
				}
				indexLow++;
			}
			if (start.equals(end))
				break;
			if (start._next != 0) {
				start = start.loadNode(start._next);
			} else {
				start = null;
			}
		}

		return _rvalues;

		// } finally {
		// lock.readLock().unlock();
		// }

	}

	public ArrayList<V> searchRangeWithViolation(K k1, K k2) throws IOException {

		if (k1 == null || k2 == null) {
			throw new IllegalArgumentException("Argument 'key' is null");
		}
		// try {
		// lock.readLock().lock();
		BTreeNode<K, V> rootNode = getRoot();
		if (rootNode == null) {
			return null;
		}
		ArrayList<V> _rvalues = new ArrayList<>();
		// _rvalues.clear();

		BTreeNode<K, V> start = rootNode.findRange(_height, k1);

		if (start == null)
			return null;
		int d = 2;
		int[] gtMin = new int[d];
		int[] ltMax = new int[d];
		int outStep[] = new int[d];
		int flag[] = new int[d];

		byte indexLow = start.findChildren(k1, true);

		/// while indexlow is less than the size of the leaf node

		byte indexHigh = start.findChildren(k2, true);

		while (indexLow <= indexHigh) {

			if (start._keys[indexLow] != null && !ZIndex.violationFound(flag, gtMin, ltMax, ((ZIndex) k1).getZvalue(),
					((ZIndex) k2).getZvalue(), ((ZIndex) start._keys[indexLow]).getZvalue(), d)) {

				_rvalues.add((V) start._values[indexLow]);
			}
			// }else
			// System.out.println(((ZIndex) start._keys[indexLow]));

			indexLow++;
		}

		// boolean low=false,high=false;

		// /Just deal with this node and get the low and high values, If
		// high values are not
		// obtained from this, then
		if (start._next == 0 || ((Comparable) start._keys[indexHigh]).compareTo(k2) >= 0)
			return _rvalues;

		while (start._next != 0) { // /do something for a Tree having just
									// one node

			start = start.loadNode(start._next);

			indexHigh = start.findChildren(k2, true);
			// if the maximum is found then return true
			indexLow = start._first;

			while (indexLow <= indexHigh) {
				// System.out.println(((ZIndex) start._keys[indexLow]));
				if (start._keys[indexLow] != null
						&& !ZIndex.violationFound(flag, gtMin, ltMax, ((ZIndex) k1).getZvalue(),
								((ZIndex) k2).getZvalue(), ((ZIndex) start._keys[indexLow]).getZvalue(), d)) {
					_rvalues.add((V) start._values[indexLow]);
				}
				// else
				// {
				// System.out.println(((ZIndex) start._keys[indexLow]));
				// }

				indexLow++;
			}

			if (start._keys[indexHigh] == null || ((Comparable) start._keys[indexHigh]).compareTo(k2) >= 0) {

				break;
			}

			// start.dump(_height);
		}

		return _rvalues;

		// } finally {
		// lock.readLock().unlock();
		// }

	}

	public ArrayList<V> searchRangeWithNJI(K k1, K k2, int d) throws IOException {

		int[] gtMin = new int[d];
		int[] ltMax = new int[d];
		int outStep[] = new int[d];
		int flag[] = new int[d];
		int changebp;
		ZIndex zindex = new ZIndex();
		zindex.setNbDim(d);

		if (k1 == null || k2 == null) {
			throw new IllegalArgumentException("Argument 'key' is null");
		}
		// try {
		// lock.readLock().lock();
		BTreeNode<K, V> rootNode = getRoot();
		if (rootNode == null) {
			return null;
		}
		ArrayList<V> _rvalues = new ArrayList<>();
		BTreeNode<K, V> start = rootNode.findRange(_height, k1);
		if (start == null)
			return null;
		boolean jump = false;
		while (start != null && ((ZIndex) k2).compareTo((ZIndex) start._keys[start._first]) > 0) {
			byte indexLow = start.findChildren(k1, true);
			byte indexHigh = start.findChildren(k2, true);

			while (indexLow <= indexHigh) {
				if (start._keys[indexLow] == null) {
					indexLow++;
					continue;
				}
				changebp = ZIndex.preProcessingNextJumpIn(outStep, flag, gtMin, ltMax, ((ZIndex) k1).getZvalue(),
						((ZIndex) k2).getZvalue(), ((ZIndex) start._keys[indexLow]).getZvalue(), d);
				// if (changebp != -1)
				// System.out.println(changebp);

				if (changebp == -1) {
					_rvalues.add((V) start._values[indexLow]);
					indexLow++;
					jump = false;
				} else if (!jump) {
					BitSet last = (BitSet) ((ZIndex) start._keys[indexLow]).getZvalue().clone();
					ZIndex.decrementsZValueByOne(last, d);
					BitSet current = (BitSet) ((ZIndex) start._keys[indexLow]).getZvalue().clone();
					// System.out.println(((ZIndex)
					// start._keys[indexLow]).toString());
					BitSet matchedzz = ZIndex.nextJumpInOptimized(outStep, flag, gtMin, ltMax,
							((ZIndex) k1).getZvalue(), ((ZIndex) k2).getZvalue(), last, current, d, changebp);
					zindex.setZvalue(matchedzz);
					jump = true;
					// int newminIndex = Arrays.binarySearch(start._keys,
					// zindex);
					// if (newminIndex)
					// indexLow = start.findChildren((K) zindex, false);
					// if(indexLow== BTree.DEFAULT_SIZE - 1)
					break;
				} else
					indexLow++;
			}
			if (zindex.getZvalue() == null) {
				if (start._next != 0) {
					start = start.loadNode(start._next);
				} else
					break;
			} else {
				BTreeNode<K, V> newStart = rootNode.findRange(_height, (K) zindex);
				if (newStart.equals(start))
					break;

			}
			zindex.setZvalue(null);
		}
		// /Just deal with this node and get the low and high values, If
		// high values are not
		// obtained from this, then
		return _rvalues;
		// } finally {
		// lock.readLock().unlock();
		// }
	}

	public ArrayList<V> searchRange(K k1, K k2) throws IOException {

		if (k1 == null || k2 == null) {
			throw new IllegalArgumentException("Argument 'key' is null");
		}

		BTreeNode<K, V> rootNode = getRoot();

		// rootNode.dumpRecursive(_height, 0);

		if (rootNode == null) {
			return null;
		}
		ArrayList<V> _rvalues = new ArrayList<>();
		// _rvalues.clear();

		BTreeNode<K, V> start = rootNode.findRange(_height, k1);

		if (start == null)
			return null;

		byte indexLow = start.findChildren(k1, true);

		// you don't really need this
		// byte indexHigh = start.findChildren(k2, true);

		while (indexLow <= DEFAULT_SIZE - 1) {

			if (start._keys[indexLow] != null && ((Comparable) start._keys[indexLow]).compareTo(k2) <= 0) {

				/// over here check if this value can be inside

				_rvalues.add((V) start._values[indexLow]);
			}

			indexLow++;

		}

		/*
		 * while (indexLow <= indexHigh) {
		 * 
		 * _rvalues.add((V) start._values[indexLow]);
		 * 
		 * indexLow++; }
		 */

		// boolean low=false,high=false;

		// /Just deal with this node and get the low and high values, If
		// high values are not
		// obtained from this, then

		if (indexLow >= DEFAULT_SIZE)
			indexLow = (byte) (DEFAULT_SIZE - 1);

		if (start._next == 0 || ((Comparable) start._keys[indexLow]).compareTo(k2) >= 0)
			return _rvalues;

		while (start._next != 0) { // /do something for a Tree having just
									// one node

			start = start.loadNode(start._next);

			byte indexHigh = start.findChildren(k2, true);
			// if the maximum is found then return true
			indexLow = start._first;

			while (indexLow <= indexHigh && start._values[indexLow] != null) {

				// start.dumpRecursive(_height, 0);
				_rvalues.add((V) start._values[indexLow]);

				indexLow++;
			}

			if (start._keys[indexHigh] != null && ((Comparable) start._keys[indexHigh]).compareTo(k2) >= 0) {

				break;
			}

		}

		return _rvalues;

	}

	private boolean checkNextRange(K[] keys) {

		int i = 0;
		while (keys[i] != null) {
			i++;
		}
		K first = keys[i];

		K last = keys[keys.length - 1];

		return true;
	}

	/**
	 * Find the value associated with the given key.
	 *
	 * @param key
	 *            Lookup key.
	 * @return Value associated with the key, or null if not found.
	 */
	public V get(K key) throws IOException {
		if (key == null) {
			throw new IllegalArgumentException("Argument 'key' is null");
		}
		// try {
		// lock.readLock().lock();
		BTreeNode<K, V> rootNode = getRoot();
		if (rootNode == null) {
			return null;
		}

		return rootNode.findValue(_height, key);
		// } finally {
		// lock.readLock().unlock();
		// }
	}

	/**
	 * Find the value associated with the given key, or the entry immediately
	 * following this key in the ordered BTree.
	 *
	 * @param key
	 *            Lookup key.
	 * @return Value associated with the key, or a greater entry, or null if no
	 *         greater entry was found.
	 */
	public BTreeTuple<K, V> findGreaterOrEqual(K key) throws IOException {
		BTreeTuple<K, V> tuple;
		BTreeTupleBrowser<K, V> browser;

		if (key == null) {
			// there can't be a key greater than or equal to "null"
			// because null is considered an infinite key.
			return null;
		}

		tuple = new BTreeTuple<K, V>(null, null);
		browser = browse(key, true);
		if (browser.getNext(tuple)) {
			return tuple;
		} else {
			return null;
		}
	}

	/**
	 * Get a browser initially positioned at the beginning of the BTree.
	 * <p>
	 * <b> WARNING: If you make structural modifications to the BTree during
	 * browsing, you will get inconsistent browing results. </b>
	 *
	 * @return Browser positionned at the beginning of the BTree.
	 */
	@SuppressWarnings("unchecked")
	public BTreeTupleBrowser<K, V> browse() throws IOException {
		// try {
		// lock.readLock().lock();
		BTreeNode<K, V> rootNode = getRoot();
		if (rootNode == null) {
			return EMPTY_BROWSER;
		}
		return rootNode.findFirst();
		// } finally {
		// lock.readLock().unlock();
		// }
	}

	/**
	 * Get a browser initially positioned just before the given key.
	 * <p>
	 * <b> WARNING: �If you make structural modifications to the BTree during
	 * browsing, you will get inconsistent browing results. </b>
	 *
	 * @param key
	 *            Key used to position the browser. If null, the browser will be
	 *            positionned after the last entry of the BTree. (Null is
	 *            considered to be an "infinite" key)
	 * @return Browser positionned just before the given key.
	 */
	@SuppressWarnings("unchecked")
	public BTreeTupleBrowser<K, V> browse(final K key, final boolean inclusive) throws IOException {
		// try {
		// lock.readLock().lock();
		BTreeNode<K, V> rootNode = getRoot();
		if (rootNode == null) {
			return EMPTY_BROWSER;
		}
		BTreeTupleBrowser<K, V> browser = rootNode.find(_height, key, inclusive);
		return browser;
		// } finally {
		// lock.readLock().unlock();
		// }
	}

	/**
	 * Return the persistent record identifier of the BTree.
	 */
	public long getRecid() {
		return _recid;
	}

	/**
	 * Return the root BTreeNode, or null if it doesn't exist.
	 */
	BTreeNode<K, V> getRoot() throws IOException {
		if (_root == 0) {
			return null;
		}
		BTreeNode<K, V> root = _db.fetch(_root, _nodeSerializer);
		if (root != null) {
			root._recid = _root;
			root._btree = this;
		}
		return root;
	}

	static BTree readExternal(DataInput in, Serialization ser) throws IOException, ClassNotFoundException {
		BTree tree = new BTree();
		tree._db = ser.db;
		tree._height = in.readInt();
		tree._recid = in.readLong();
		tree._root = in.readLong();
		tree._entries = in.readLong();
		tree.hasValues = in.readBoolean();
		tree._comparator = (Comparator) ser.deserialize(in);
		tree.keySerializer = (Serializer) ser.deserialize(in);
		tree.valueSerializer = (Serializer) ser.deserialize(in);
		return tree;
	}

	public void writeExternal(DataOutput out) throws IOException {
		out.writeInt(_height);
		out.writeLong(_recid);
		out.writeLong(_root);
		out.writeLong(_entries);
		out.writeBoolean(hasValues);
		_db.defaultSerializer().serialize(out, _comparator);
		_db.defaultSerializer().serialize(out, keySerializer);
		_db.defaultSerializer().serialize(out, valueSerializer);
	}

	/**
	 * Copyes tree from one db to other, defragmenting it allong the way
	 * 
	 * @param recid
	 * @param r1
	 * @param r2
	 * @throws IOException
	 */
	public static void defrag(long recid, DBStore r1, DBStore r2) throws IOException {
		try {
			byte[] data = r1.fetchRaw(recid);
			r2.forceInsert(recid, data);
			DataInput in = new DataInputOutput(data);
			BTree t = (BTree) r1.defaultSerializer().deserialize(in);
			t.loadValues = false;
			t._db = r1;
			t._nodeSerializer = new BTreeNode(t, false);

			BTreeNode p = t.getRoot();
			if (p != null) {
				r2.forceInsert(t._root, r1.fetchRaw(t._root));
				p.defrag(r1, r2);
			}

		} catch (ClassNotFoundException e) {
			throw new IOError(e);
		}
	}

	/**
	 * Browser returning no element.
	 */
	private static final BTreeTupleBrowser EMPTY_BROWSER = new BTreeTupleBrowser() {

		public boolean getNext(BTreeTuple tuple) {
			return false;
		}

		public boolean getPrevious(BTreeTuple tuple) {
			return false;
		}

		public void remove(Object key) {
			throw new IndexOutOfBoundsException();
		}
	};

	/**
	 * add RecordListener which is notified about record changes
	 *
	 * @param listener
	 */
	public void addRecordListener(RecordListener<K, V> listener) {
		recordListeners = Arrays.copyOf(recordListeners, recordListeners.length + 1);
		recordListeners[recordListeners.length - 1] = listener;
	}

	/**
	 * remove RecordListener which is notified about record changes
	 *
	 * @param listener
	 */
	public void removeRecordListener(RecordListener<K, V> listener) {
		List l = Arrays.asList(recordListeners);
		l.remove(listener);
		recordListeners = (RecordListener[]) l.toArray(new RecordListener[1]);
	}

	public DBAbstract getRecordManager() {
		return _db;
	}

	public Comparator<K> getComparator() {
		return _comparator;
	}

	/**
	 * Deletes all BTreeNodes in this BTree
	 */
	public void clear() throws IOException {
		// try {
		// lock.writeLock().lock();
		BTreeNode<K, V> rootNode = getRoot();
		if (rootNode != null)
			rootNode.delete();
		_entries = 0;
		modCount++;
		// } finally {
		// lock.writeLock().unlock();
		// }
	}

	/**
	 * Used for debugging and testing only. Populates the 'out' list with the
	 * recids of all child nodes in the BTree.
	 *
	 * @param out
	 * @throws IOException
	 */
	void dumpChildNodeRecIDs(List<Long> out) throws IOException {
		BTreeNode<K, V> root = getRoot();
		if (root != null) {
			out.add(root._recid);
			root.dumpChildNodeRecIDs(out, _height);
		}
	}

	public boolean hasValues() {
		return hasValues;
	}

	/**
	 * Browser to traverse a collection of tuples. The browser allows for
	 * forward and reverse order traversal.
	 *
	 *
	 */
	static interface BTreeTupleBrowser<K, V> {

		/**
		 * Get the next tuple.
		 *
		 * @param tuple
		 *            Tuple into which values are copied.
		 * @return True if values have been copied in tuple, or false if there
		 *         is no next tuple.
		 */
		boolean getNext(BTree.BTreeTuple<K, V> tuple) throws IOException;

		/**
		 * Get the previous tuple.
		 *
		 * @param tuple
		 *            Tuple into which values are copied.
		 * @return True if values have been copied in tuple, or false if there
		 *         is no previous tuple.
		 */
		boolean getPrevious(BTree.BTreeTuple<K, V> tuple) throws IOException;

		/**
		 * Remove an entry with given key, and increases browsers
		 * expectedModCount This method is here to support
		 * 'ConcurrentModificationException' on Map interface.
		 *
		 * @param key
		 */
		void remove(K key) throws IOException;

	}

	/**
	 * Tuple consisting of a key-value pair.
	 */
	static final class BTreeTuple<K, V> {

		K key;

		V value;

		BTreeTuple() {
			// empty
		}

		BTreeTuple(K key, V value) {
			this.key = key;
			this.value = value;
		}

	}

}
