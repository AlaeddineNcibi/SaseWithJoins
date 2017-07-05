package org.labhc.event;

import java.util.Comparator;

public class ActivityEvent extends Event {


	public int personId;
	public int rte;
	
	
	public ActivityEvent(int ts, int i, int personId, int heartRate) {
		super(ts, i, EventType.ACTIVITYEVENT);
		this.personId = personId;
		this.rte = heartRate;

	}

	public static Event parse(String line) {
		String[] values = line.split(",");

		int ts = Integer.parseInt(values[0]);
		int id = Integer.parseInt(values[1]);
		int personId = Integer.parseInt(values[2]);

		int heartRate = Integer.parseInt(values[3]);

		Event event = new ActivityEvent(ts, id, personId, heartRate);
		// System.out.println(event.toString());
		return event;

	}
	

	class RateComparator implements Comparator<ActivityEvent> {

		@Override
		public int compare(ActivityEvent o1, ActivityEvent o2) {

			return o1.rte - o2.rte;
		}
	}


	@Override
	public String toString() {
		return "ActivityEvent [PersonId=" + personId + ", heartRate=" + rte + ", timestamp=" + this.timestamp +"]";
	}
	
	

}
