package org.labhc.queryprocessor;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;

import org.labhc.event.Event;
import org.labhc.windowsemantics.AbstractWindow;

public class QueryProcessor extends AbstractQueryProcessor {

	public QueryProcessor(LinkedBlockingQueue<Event> i, CountDownLatch l,
			AbstractWindow w) throws IOException {
		super(i, l, w);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void process(Event e) {

		System.out.println(e.id);

		this.get_outputQueue().add(Integer.toString(e.id));
	}

}
