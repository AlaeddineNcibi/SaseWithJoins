package org.labhc.event;

import java.util.Comparator;

public class StockEvent extends Event {

	public int price;

	public int vol;

	public StockEvent(int ts, int i, int p, int v) {
		super(ts, i, EventType.STOCKEVENT);
		this.price = p;
		this.vol = v;

	}

	public static Event parse(String line) {

		String[] values = line.split(",");

		int ts = Integer.parseInt(values[0]);
		int id = Integer.parseInt(values[1]);
		int p = Integer.parseInt(values[2]);

		int v = Integer.parseInt(values[3]);

		Event event = new StockEvent(ts, id, p, v);
		// System.out.println(event.toString());
		return event;

	}
	

	class PriceComparator implements Comparator<StockEvent> {

		@Override
		public int compare(StockEvent o1, StockEvent o2) {

			return o1.price - o2.price;
		}
	}

	class VolumeComparator implements Comparator<StockEvent> {

		@Override
		public int compare(StockEvent o1, StockEvent o2) {

			return o1.vol - o2.vol;
		}
	}

	@Override
	public String toString() {
		return "StockEvent [price=" + price + ", vol=" + vol + ", timestamp=" + this.timestamp +"]";
	}
	
	

}
