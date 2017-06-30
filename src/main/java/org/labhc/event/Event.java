package org.labhc.event;

public abstract class Event implements Comparable<Event> {

	public int timestamp;

	public EventType eventType;

	public int id;

	public Event(String data) {

		// ParsingEvent over here
		System.out.println(data);

	}

	public static Event parse(String line, EventType type) {
		Event event = null;
		if (type == EventType.CHECKEVENT) {

			event = CheckEvent.parse(line);
		} else if (type == EventType.STOCKEVENT) {

			event = StockEvent.parse(line);
		}
		return event;
	}

	public Event(int ts, int i, EventType et) {
		timestamp = ts;
		id = i;
		eventType = et;
	}

	public int compareTo(Event other) {
		if (this.timestamp > other.timestamp)
			return 1;
		else
			return -1;

	}

	public boolean equals(Event other) {
		return this.id == other.id;
	}

	@Override
	public String toString() {
		return "Event [timestamp=" + timestamp + ", id=" + id + "]";
	}

}
