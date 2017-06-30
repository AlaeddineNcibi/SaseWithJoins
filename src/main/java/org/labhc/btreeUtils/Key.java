package org.labhc.btreeUtils;

import java.io.Serializable;
import java.util.Arrays;

public class Key implements Comparable, Serializable {

	private boolean[] key;

	public Key() {

	}

	public Key(boolean[] key) {
		this.key = key;
	}

	@Override
	public int compareTo(Object o) {

		if (((Key) o).key.length != this.key.length) {

			return ((Key) o).key.length > this.key.length ? -1 : 1;
		}

		// //check each entry unless you find a difference of 0 or 1;

		for (int i = 0; i < this.key.length; i++) {

			if (((Key) o).key[i] != this.key[i]) {

				if (this.key[i] == true)
					return 1;
				else
					return -1;

			}

			//
			// if (((Key) o).key.get(i) > this.key.get(i))
			// return -1;
			// else if (((Key) o).key.get(i) < this.key.get(i))
			// return 1;

		}

		return 0;
	}

	public boolean[] getKey() {
		return key;
	}

	public void setKey(boolean[] key) {
		this.key = key;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(key);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Key other = (Key) obj;
		if (!Arrays.equals(key, other.key))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Key [key=" + Arrays.toString(key) + "]";
	}

}
