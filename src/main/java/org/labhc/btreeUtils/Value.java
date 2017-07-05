package org.labhc.btreeUtils;

import java.util.ArrayList;
import java.util.List;

import org.labhc.event.ActivityEvent;
import org.labhc.event.Event;
import org.labhc.event.EventType;
import org.labhc.event.StockEvent;

public class Value {

	private int _value;
	private int timestamp;

	private List<Integer> _window;

	public Event event;
	private int _numOfEvents;

	public Value(int value, int timestamp) {
		this._value = value;
		this.timestamp = timestamp;
		this._numOfEvents = 1;
		this._window = new ArrayList<>();
		this._window.add(timestamp);
	}

	public int getPredicate(int pred) {
		if (this.event.eventType == EventType.STOCKEVENT) {
			StockEvent se = (StockEvent) this.event;
			if (pred == 1) // get the price
				return se.price;
			else
				return se.vol;

		}
		if (this.event.eventType == EventType.ACTIVITYEVENT) {
			ActivityEvent se = (ActivityEvent) this.event;
			if (pred == 1) // get the price
				return se.rte;
			else
				return 0;

		}
		return 1;
	}

	public int compareTo(Value o, int pred) {

		/**
		 * Add more types for other event types
		 */
		if (this.event.eventType == EventType.STOCKEVENT) {
			StockEvent se = (StockEvent) this.event;
			if (pred == 1) { // get the price

				if (se.price == ((StockEvent) o.event).price)
					return 0;
				return se.price > ((StockEvent) o.event).price ? 1 : -1;

			} else if( pred==2){
				if (se.vol == ((StockEvent) o.event).vol)
					return 0;
				return se.vol > ((StockEvent) o.event).vol ? 1 : -1;
			}
			else{
				if (se.timestamp == ((StockEvent) o.event).timestamp)
					return 0;
				return se.timestamp > ((StockEvent) o.event).timestamp ? 1 : -1;
			}
		}

		return 0;
	}

	public int compareToEvent(Event event, int pred) {
		if (this.event.eventType == EventType.STOCKEVENT) {
			StockEvent se = (StockEvent) this.event;
			if (pred == 1) { // get the price

				if (se.price == ((StockEvent) event).price)
					return 0;
				return se.price > ((StockEvent) event).price ? 1 : -1;

			} else {
				if (se.vol == ((StockEvent) event).vol)
					return 0;
				return se.vol > ((StockEvent) event).vol ? 1 : -1;
			}
		}

		return 0;

	}

	public Value(Event e, int t) {

		event = e;
		timestamp = t;
		_numOfEvents = 1;
		_window = new ArrayList<>();
		_window.add(timestamp);
	}

	// public void setEvents(List<Value> events) {
	// this.events = events;
	// }
	//
	// public List<Value> getEvents() {
	// return this.events ;
	// }

	// public int[] get_values() {
	// return _values;
	// }
	//
	// public void set_values(int[] _values) {
	// this._values = _values;
	// }

	public int get_value() {
		return _value;
	}

	public void set_value(int _value) {
		this._value = _value;
	}

	public List<Integer> get_window() {
		if (_window == null)
			this._window = new ArrayList<>();
		return _window;
	}

	public void set_window(List<Integer> _window) {
		this._window = _window;
	}

	public int get_numOfEvents() {
		return _numOfEvents;
	}

	public void set_numOfEvents(int _numOfEvents) {
		this._numOfEvents = _numOfEvents;
	}

	public int getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(int timestamp) {
		this.timestamp = timestamp;
	}

	@Override
	public String toString() {
		return "Value [ event=" + event.toString()
				+ ", _numOfEvents=" + _numOfEvents + "]";
	}

	//
	// @Override
	// public String toString() {
	// return "Value [_values=" + Arrays.toString(_values) + ", _window=" +
	// _window + ", _numOfRuns=" + _numOfRuns
	// + "]";
	// }
}
