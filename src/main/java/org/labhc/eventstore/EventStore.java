package org.labhc.eventstore;

import org.apache.jdbm.BTree;

public abstract class EventStore {

	private final int ID;

	private int storeStatus; // /0 for secondary, 1 for primary

	private int startTime;

	private int endTime;

	private String storeStructure;

	public EventStore(int stype, int stime, int etime, String sstruc, int id) {
		this.storeStatus = stype;
		this.startTime = stime;
		this.endTime = etime;
		ID = id;
		this.storeStructure = sstruc;
	}

	public abstract void refreshInfo(int currTime);

	public abstract BTree tree();

	public int getStoreType() {
		return storeStatus;
	}

	public void setStoreType(int storeType) {
		this.storeStatus = storeType;
	}

	public int getStartTime() {
		return startTime;
	}

	public void setStartTime(int startTime) {
		this.startTime = startTime;
	}

	public int getEndTime() {
		return endTime;
	}

	public void setEndTime(int endTime) {
		this.endTime = endTime;
	}

	public String getStoreStructure() {
		return storeStructure;
	}

	public void setStoreStructure(String storeStructure) {
		this.storeStructure = storeStructure;
	}

}
