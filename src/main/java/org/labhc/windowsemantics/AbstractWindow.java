package org.labhc.windowsemantics;

public abstract class AbstractWindow {

	public final int windowLength;

	public int startTimeofWindow;

	public int endTimeofWindow;

	public AbstractWindow(int w) {

		windowLength = w;

	}

	public abstract void initliaseWindow();

	public abstract void updateWindow(int currentTS);
}
