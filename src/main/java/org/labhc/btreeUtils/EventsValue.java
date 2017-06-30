package org.labhc.btreeUtils;

import java.util.ArrayList;
import java.util.List;

import org.labhc.event.Event;

public class EventsValue {
	private List<Event> _events;

	private int _numOfEvents;

	public EventsValue(List<Event> _events, int _numOfEvents) {
		super();
		this._events = _events;
		this._numOfEvents = _numOfEvents;
	}

	public List<Event> get_events() {
		return _events;
	}

	public void set_events(List<Event> _events) {
		this._events = _events;
	}

	public int get_numOfEvents() {
		return _numOfEvents;
	}

	public void set_numOfEvents(int _numOfEvents) {
		this._numOfEvents = _numOfEvents;
	}

	public void add_events(Event _event) {
		if (this._events == null) {
			this._events = new ArrayList<>();
		}

		this._events.add(_event);
		this._numOfEvents++;

	}

}
