package org.labhc.inputmanager;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class TestStockEventGenerator {
	private BufferedWriter outputWriter;

	private static String DIRECTORY = "./src/main/java/org/labhc/iofiles/";

	public TestStockEventGenerator() {
		initialise();
	}

	public static void main(String[] args) throws IOException {
		TestStockEventGenerator tg = new TestStockEventGenerator();
		// for (int i = 0; i < 10000; i++) {
		tg.generateUpstream(1000);
		tg.generateDownStream(100);
		tg.generateUpstream(1000);
		// }

		tg.close();

	}

	public void close() throws IOException {
		outputWriter.close();
	}

	public void initialise() {
		try {
			final String pathname = DIRECTORY + "stockevent.txt";
			outputWriter = new BufferedWriter(new FileWriter(new File(pathname)));

		} catch (final IOException e) {

			System.exit(-1);
		}

	}

	public void generateUpstream(int max) throws IOException {

		int price = 5;

		int vol = 20;
		for (int i = 0; i < max; i++) {

			String id = Integer.toString(i);

			String p = Integer.toString(price);

			String v = Integer.toString(vol);
			outputWriter.write(id + "," + id + "," + p + "," + v);
			outputWriter.newLine();

			price = price + 2;
			vol = vol + 10;
		}

	}

	public void generateDownStream(int max) throws IOException {

		int price = max;

		int vol = max;
		for (int i = 10; i < max; i++) {

			String id = Integer.toString(i);

			String p = Integer.toString(price);

			String v = Integer.toString(vol);
			outputWriter.write(id + "," + id + "," + p + "," + v);
			outputWriter.newLine();

			price = price - 2;
			vol = vol - 5;
		}
	}

}
