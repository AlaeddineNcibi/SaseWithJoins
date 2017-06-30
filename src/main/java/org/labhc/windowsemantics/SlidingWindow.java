package org.labhc.windowsemantics;

public class SlidingWindow extends AbstractWindow {

	public final int slide;

	public SlidingWindow(int w, int s) {
		super(w);

		slide = s;

	}

	@Override
	public void updateWindow(int currentTS) {

		int check = slide + startTimeofWindow;

		if (currentTS >= check) {

			startTimeofWindow = currentTS;
			endTimeofWindow = currentTS + windowLength;
		}

	}

	@Override
	public void initliaseWindow() {

		startTimeofWindow = 0; // later system.time

		endTimeofWindow = windowLength; // /later in terms of system.time, i.e.
										// adding seconds, minutes or hours to
										// the system.current.time

	}

}
