package org.labhc.btreeUtils;

public class RQuery {

	private Key maxKey;

	private Key minKey;

	public RQuery(Key maxKey, Key minKey) {
		super();
		this.maxKey = maxKey;
		this.minKey = minKey;
	}

	public Key getMaxKey() {
		return maxKey;
	}

	public void setMaxKey(Key maxKey) {
		this.maxKey = maxKey;
	}

	public Key getMinKey() {
		return minKey;
	}

	public void setMinKey(Key minKey) {
		this.minKey = minKey;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((maxKey == null) ? 0 : maxKey.hashCode());
		result = prime * result + ((minKey == null) ? 0 : minKey.hashCode());
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
		RQuery other = (RQuery) obj;
		if (maxKey == null) {
			if (other.maxKey != null)
				return false;
		} else if (!maxKey.equals(other.maxKey))
			return false;
		if (minKey == null) {
			if (other.minKey != null)
				return false;
		} else if (!minKey.equals(other.minKey))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "RQuery [maxKey=" + maxKey + ", minKey=" + minKey + "]";
	}

}
