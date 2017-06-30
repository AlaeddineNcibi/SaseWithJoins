package org.labhc.outputmanager;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;

import org.labhc.event.EventType;

public class ResultWriter implements Runnable {

	private BufferedWriter outputWriter;

	private final int id;

	public final LinkedBlockingQueue<String> resultqueue;

	private final String directory;

	private CountDownLatch latch;

	public ResultWriter(final int id, final LinkedBlockingQueue<String> resultqueue, final String directory,
			CountDownLatch l) {
		super();
		this.id = id;
		this.latch = l;
		this.resultqueue = resultqueue;
		this.directory = directory;
	}

	@Override
	public void run() {
		try {
			final String pathname = directory + "/joinResults" + id + ".txt";
			outputWriter = new BufferedWriter(new FileWriter(new File(pathname)));

		} catch (final IOException e) {

			System.exit(-1);
		}

		while (true) {
			try {
				final String line = resultqueue.take();

				if (line != EventType.POISONPILL.toString()) {
					// System.out.println(line);
					// System.out.println("REsults +" + line);
					writeLine(line);
				} else {

					// System.out.println("REsults +" + line);
					break;
				}
			} catch (final InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		finish();

	}

	protected void writeLine(final String line) {
		try {
			outputWriter.write(line);
			// outputWriter.newLine();
		} catch (final IOException e) {
			// logger.error("Could not write new line for query processor " + id
			// + ", line content " + line, e);
		}

	}

	private void finish() {
		// Close writer
		try {
			outputWriter.close();
		} catch (final IOException e) {
			// logger.error("Cannot property close the output file for query " +
			// id, e);
		}
		latch.countDown();
	}

}
