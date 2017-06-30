package org.labhc.queryprocessor;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class TestMatches {

	private static BufferedWriter outputWriter;
	private final static String directory = "./src/main/java/org/labhc/iofiles/";
	private static RandomAccessFile fileStore;
	private static String linesep = System.getProperty("line.separator");
	private static int testc = 0;
	private static int linecounter = 0;
	private static int numline = 0;

	public static void main(String[] args) throws IOException {

		List<Integer> a = new ArrayList<>();
		// a.add(80);
		// a.add(90);
		// a.add(100);
		// a.add(110);

		for (int i = 1; i < 20; i++) {
			a.add(i);
		}

		List<Integer> b = new ArrayList<>();
		// b.add(80);
		// b.add(90);
		// b.add(100);
		// b.add(110);
		// b.add(200);

		for (int i = 2; i < 25; i++) {
			b.add(i);
		}

		TestMatches tt = new TestMatches();

		System.out.println("Started...");
		// tt.openingFile();
		// tt.createResults2(a, b);
		// tt.finish();
		long startTime = System.nanoTime();
		tt.openingFile();
		createResults(a, b);
		tt.finish();

		System.out.println(("Global execution time " + (System.nanoTime() - startTime) / 1_000_000) + "ms\n");
	}

	private void openingFile() throws IOException {
		fileStore = new RandomAccessFile(directory + "/results.txt", "rws");
	}

	private static void createResults(List<Integer> a, List<Integer> b) throws IOException {

		int start = b.size() - 1;
		int end = b.size();
		for (int i = a.size() - 1; i >= 0; i--) {

			resuseResults(linecounter, a.get(i));

			while (start >= 0) {
				if (b.get(start) > a.get(i))
					System.out.print(""); /// create the
											/// matches over
											/// here
				else
					break;

				/// from start till the end of the list produce the patterns

				outputCombinations(a.get(i), b, start, end, 0);
				start--;
				end--;
			}

		}

	}

	private static void outputCombinations(int element, List<Integer> b, int start, int end, int linecount)
			throws IOException {

		int diff = end - start;
		StringBuilder sb = new StringBuilder();
		for (int i = 1; i < Math.pow(2, (diff)); i++) {
			// System.out.print(element);

			// outputWriter.write(Integer.toString(element));
			// String st = Integer.toString(element);
			// fileStore.writeBytes(st);
			sb = new StringBuilder();
			sb.append(element);
			char[] s = Integer.toBinaryString(i).toCharArray();

			for (int j = 0; j < s.length; j++) {

				if (s[j] == '1') {

					// System.out.println(diff + j);

					if (s.length < diff) {

						int index = j + diff - s.length;

						sb.append("," + b.get(start + index));
						// outputWriter.write("," + b.get(start + index));
						// fileStore.writeBytes("," + b.get(start + index));

					} else {
						sb.append("," + b.get(start + j));
						// outputWriter.write("," + b.get(start + j));
						// fileStore.writeBytes("," + b.get(start + j));

					}

				}
			}

			/// System.out.println(sb.toString().getBytes().length);

			// int size = sb.toString().getBytes().length;

			fileStore.writeChars(sb.toString());
			// outputWriter.newLine();
			fileStore.writeChars(linesep);
			numline++;
			// = linecounter +
			// size + 1;

			// System.out.print(sb.toString());
			// System.out.println();

			// long currentPosition = fileStore.getFilePointer();

			/// System.out.println(currentPosition);

		}

	}

	private static void resuseResults(int tlc, int element) throws IOException {

		if (testc >= 1) {
			// long currentPosition = fileStore.getFilePointer();

			// System.out.println(fileStore.length());

			// String record = fileStore.readLine();
			int i = 0;
			linecounter = (int) fileStore.getFilePointer();
			int newlines = 0;

			while (i < numline) {
				/// read from the files
				fileStore.seek(tlc);
				String results = fileStore.readLine();
				tlc = (int) fileStore.getFilePointer();

				//// write at the end of the files
				fileStore.seek(fileStore.length());

				fileStore.writeBytes(element + "," + results);
				fileStore.writeChars(linesep);
				results = results.substring(results.indexOf(",") + 1, results.length());
				fileStore.writeBytes(element + "," + results);
				fileStore.writeChars(linesep);

				// System.out.println("results " + fileStore.readLine());
				newlines = newlines + 2;

				i++;
			}
			numline = newlines;
		}

		testc++;

	}

	private void createResults2(List<Integer> a, List<Integer> b) throws IOException {

		int start = b.size() - 1;
		for (int i = a.size() - 1; i >= 0; i--) {
			// outputWriter.write(a.get(i));

			// System.out.print("A " + a.get(i));
			while (start >= 0) {
				if (b.get(start) < a.get(i))
					break;

				/// from start till the end of the list produce the patterns

				outputCombinations2(a.get(i), b, start, b.size());
				start--;
			}

		}

	}

	private void outputCombinations2(int element, List<Integer> b, int start, int end) throws IOException {

		int diff = end - start;

		StringBuilder sb = new StringBuilder();
		for (int i = 1; i < Math.pow(2, (diff)); i++) {
			outputWriter.write(Integer.toString(element));
			System.out.print(element);
			sb = new StringBuilder();
			char[] s = Integer.toBinaryString(i).toCharArray();

			for (int j = 0; j < s.length; j++) {

				if (s[j] == '1') {

					// System.out.println(diff + j);

					if (s.length < diff) {

						int index = j + diff - s.length;
						outputWriter.write("," + b.get(start + index));
						System.out.print("," + b.get(start + index));
						// sb.append(b.get(start + index) + ",");

					} else {

						outputWriter.write("," + b.get(start + j));
						System.out.print("," + b.get(start + j));
						// sb.append("," + b.get(start + j));
					}

				}
			}

			outputWriter.newLine();
			System.out.println();
			// System.out.println(sb.toString());

		}

	}

	private void finish() {
		// Close writer
		try {
			fileStore.close();
		} catch (final IOException e) {
			// logger.error("Cannot property close the output file for query " +
			// id, e);
		}

	}

	public static void yarg(String prefix, int n) {
		if (n == 0)
			System.out.println(prefix);
		else {
			gray(prefix + "1", n - 1);
			yarg(prefix + "0", n - 1);
		}
	}

	// append order n gray code to end of prefix string, and print
	public static void gray(String prefix, int n) {
		if (n == 0)
			System.out.println(prefix);
		else {
			gray(prefix + "0", n - 1);
			yarg(prefix + "1", n - 1);
		}
	}

	public static List<Integer> grayCode(int n) {
		List<Integer> ret = new LinkedList<>();
		ret.add(0);
		for (int i = 0; i < n; i++) {
			int size = ret.size();
			for (int j = size - 1; j >= 0; j--)
				ret.add(ret.get(j) + size);
		}

		System.out.println(ret.size());
		return ret;
	}
}
