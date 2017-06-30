package org.labhc.outputmanager;

import java.io.File;
import java.io.IOException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.io.FileUtils;
import org.labhc.queryprocessor.AbstractQueryProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author Julien
 *
 */

public class QueryProcessorMeasure {

//	final static Logger logger = LoggerFactory
//			.getLogger(QueryProcessorMeasure.class);
	/**
	 * Program starts
	 */
	long startTime;
	/**
	 * Program finishes
	 */
	long endTime;
	/**
	 * Maps PID <-> start time
	 */
	private final ConcurrentHashMap<Integer, Long> timePerProcessorStart;
	/**
	 * Maps PID <-> finish time
	 */
	private final ConcurrentHashMap<Integer, Long> timePerProcessorFinish;
	/**
	 * Number of records processed
	 */
	private long records;

	private String directory;

	public QueryProcessorMeasure() {
		timePerProcessorStart = new ConcurrentHashMap<>();
		timePerProcessorFinish = new ConcurrentHashMap<>();
		startTime = System.nanoTime();
	}

	public QueryProcessorMeasure(final String directory) {
		this();
		this.directory = directory;

	}

	/**
	 *
	 * @param processorId
	 *            id of the {@link AbstractQueryProcessor} that just started
	 */
	public void notifyStart(final int processorId) {
		timePerProcessorStart.put(processorId, System.nanoTime());
	}

	/**
	 *
	 * @param processorId
	 *            id of the {@link AbstractQueryProcessor} that just started
	 */
	public void notifyFinish(final int processorId) {
		timePerProcessorFinish.put(processorId, System.nanoTime());
	}

	public void setProcessedRecords(final long records) {
		this.records = records;
	}

	/**
	 * Writes the results into a file in the result/ directory
	 */
	public void outputMeasure() {
		endTime = System.nanoTime();
		final Set<Integer> pids = timePerProcessorStart.keySet();
		final StringBuffer sbTime = new StringBuffer();
		sbTime.append("Global execution time "
				+ ((endTime - startTime) / 1_000_000) + "ms\n");
		for (final Integer pid : pids) {
			final long nanoDiff = timePerProcessorFinish.get(pid)
					- timePerProcessorStart.get(pid);
			final long msDiff = nanoDiff / 1_000_000;
			sbTime.append("Query " + pid + " runtime: " + msDiff + "ms\n");
			final long throughput = (records * 1_000_000) / nanoDiff;
			sbTime.append("Query " + pid + " throughput: " + throughput
					+ "K events/second\n");
			System.out.println(sbTime.toString());
		}
		try {
			FileUtils.writeStringToFile(new File(directory
					+ "result/performance.txt"), sbTime.toString());
		} catch (final IOException e) {
//			logger.error("Error while saving running time stats", e);
		}
	}

}