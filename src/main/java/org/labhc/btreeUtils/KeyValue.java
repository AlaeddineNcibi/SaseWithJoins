package org.labhc.btreeUtils;

public class KeyValue {

	private Key k;

	private Value v;

	public KeyValue(Key k, Value v) {
		super();
		this.k = k;
		this.v = v;
	}

	public Key getK() {
		return k;
	}

	public void setK(Key k) {
		this.k = k;
	}

	public Value getV() {
		return v;
	}

	public void setV(Value v) {
		this.v = v;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((k == null) ? 0 : k.hashCode());
		result = prime * result + ((v == null) ? 0 : v.hashCode());
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
		KeyValue other = (KeyValue) obj;
		if (k == null) {
			if (other.k != null)
				return false;
		} else if (!k.equals(other.k))
			return false;
		if (v == null) {
			if (other.v != null)
				return false;
		} else if (!v.equals(other.v))
			return false;
		return true;
	}

}
