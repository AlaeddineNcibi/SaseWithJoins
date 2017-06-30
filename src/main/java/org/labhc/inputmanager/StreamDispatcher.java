package org.labhc.inputmanager;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.concurrent.LinkedBlockingQueue;

import org.labhc.event.Event;
import org.labhc.event.EventType;
import org.labhc.event.PoisonPill;

public class StreamDispatcher extends AbstractDispatcher {

	BufferedReader _reader;
	// private static final String INPUT_FILE =
	// "/Users/sydgillani/Documents/Post-Doc/CEPStuff/CET-master/CET/src/iofiles/streamNew.txt";
	private final LinkedBlockingQueue<Event> _inputQueue;
	private final EventType et;

	public StreamDispatcher(final String string, int capacity, EventType e) {
		super(string);
		et = e;
		_inputQueue = new LinkedBlockingQueue<>(capacity);

	}

	/**
	 * Open the readers for the file
	 * 
	 * @throws FileNotFoundException
	 */
	private void openReader() throws FileNotFoundException {

		_reader = new BufferedReader(new FileReader(this.fileLocation));

	}

	public void run() {
		// TODO Auto-generated method stub
		// Event e = new PoisonPill(0, 0, EventType.POISONPILL);
		String line = "";

		try {
			openReader();
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		try {

			while ((line = _reader.readLine()) != null) {
				Event e = Event.parse(line, et);
				_inputQueue.add(e);
				this.records++;
			}

			_inputQueue.add(new PoisonPill(0, 0, EventType.POISONPILL));

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public LinkedBlockingQueue<Event> get_inputQueue() {
		return _inputQueue;
	}
}
