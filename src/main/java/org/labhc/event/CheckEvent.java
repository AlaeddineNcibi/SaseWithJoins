package org.labhc.event;

public class CheckEvent extends Event {

	public int source;

	public CheckEvent(int ts, int id, int s) {

		super(ts, id, EventType.CHECKEVENT);

		this.source = s;
	}

	public static Event parse(String line) {

		String[] values = line.split(",");

		int ts = Integer.parseInt(values[1]);
		int id = Integer.parseInt(values[0]);
		int s = Integer.parseInt(values[2]);

		Event event = new CheckEvent(ts, id, s);
		// System.out.println(event.toString());
		return event;

	}

}
