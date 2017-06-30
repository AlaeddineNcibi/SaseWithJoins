package org.labhc.intervaltree;

import org.labhc.zvalueencoder.ZIndex;

public class RangeQuery implements Interval {

	private ZIndex zmin;

	private ZIndex zmax;

	public RangeQuery(ZIndex zmin, ZIndex zmax) {
		super();
		this.zmin = zmin;
		this.zmax = zmax;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((zmin == null) ? 0 : zmin.hashCode());
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
		RangeQuery other = (RangeQuery) obj;
		if (zmin == null) {
			if (other.zmin != null)
				return false;
		} else if (!zmin.equals(other.zmin))
			return false;
		return true;
	}

	public ZIndex getZmin() {
		return zmin;
	}

	public void setZmin(ZIndex zmin) {
		this.zmin = zmin;
	}

	public ZIndex getZmax() {
		return zmax;
	}

	public void setZmax(ZIndex zmax) {
		this.zmax = zmax;
	}

	@Override
	public ZIndex start() {
		// TODO Auto-generated method stub
		return zmin;
	}

	@Override
	public ZIndex end() {
		// TODO Auto-generated method stub
		return zmax;
	}

}
