package org.labhc.inputmanager;

import java.util.ArrayList;
import java.util.Collection;

import org.labhc.queryprocessor.AbstractQueryProcessor;

/**
 * 
 * @author julien, sydgillani
 *
 */

public abstract class AbstractDispatcher implements Runnable {
	// final static Logger logger =
	// LoggerFactory.getLogger(AbstractDispatcher.class);

	/**
	 * Files containing one event per line
	 */

	final String fileLocation;

	/**
	 * Number of records
	 */
	protected long records = 0;

	/**
	 * List of registered query processors
	 */
	Collection<AbstractQueryProcessor> registeredProcessors = new ArrayList<>();

	public AbstractDispatcher(final String string) {
		this.fileLocation = string;
	}

	public void registerQueryProcessor(
			final AbstractQueryProcessor queryProcessor) {
		registeredProcessors.add(queryProcessor);
	}

	/**
	 *
	 * @return the number of parsed records
	 */
	public long getRecords() {
		return records;
	}

}
