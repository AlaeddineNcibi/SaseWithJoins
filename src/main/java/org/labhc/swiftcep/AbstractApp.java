package org.labhc.swiftcep;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

import org.labhc.event.EventType;
import org.labhc.inputmanager.StreamDispatcher;
import org.labhc.outputmanager.QueryProcessorMeasure;
import org.labhc.outputmanager.ResultWriter;
import org.labhc.queryprocessor.AbstractQueryProcessor;
import org.labhc.queryprocessor.ExtendedStockQueryProcessor;
import org.labhc.windowsemantics.AbstractWindow;
import org.labhc.windowsemantics.SlidingWindow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AbstractApp {
	//final static Logger logger = LoggerFactory.getLogger(AbstractApp.class);

	private static String INPUT_FILE = "./src/main/java/org/labhc/iofiles/teststockevent.txt";
	private static String OUTPUT_DIRC = "./src/main/java/org/labhc/iofiles/";

	final static QueryProcessorMeasure measure = new QueryProcessorMeasure();

	public static void start() throws IOException {

		final CountDownLatch latch = new CountDownLatch(1 + 1);
		StreamDispatcher sd = new StreamDispatcher(INPUT_FILE, 2000000, EventType.STOCKEVENT);

		AbstractWindow w = new SlidingWindow(10, 20);

		AbstractQueryProcessor qp = new ExtendedStockQueryProcessor(sd.get_inputQueue(), latch, w);

		ResultWriter rw = new ResultWriter(1, qp.get_outputQueue(), OUTPUT_DIRC, latch);

		Thread queryProc = new Thread(qp);

		queryProc.start();

		Thread writer = new Thread(rw);

		writer.start();

		Thread inputProc = new Thread(sd);

		inputProc.start();
		measure.notifyStart(1);

		try {
			latch.await();
		} catch (final InterruptedException e) {

			System.out.println("Error");
		//	logger.error("Error while waiting for the program to end", e);
		}

		measure.notifyFinish(1);
		measure.setProcessedRecords(sd.getRecords());

		measure.outputMeasure();
	}

}
