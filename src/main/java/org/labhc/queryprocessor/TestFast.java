package org.labhc.queryprocessor;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

import org.labhc.queryprocessor.Jointest.Vtest;

public class TestFast {
	private static BufferedWriter outputWriter;
	private final static String directory = "./src/main/java/org/labhc/iofiles/";
	private static RandomAccessFile fileStore;
	private static String linesep = System.getProperty("line.separator");
	private static int testc = 0;
	private static int linecounter = 0;
	private static int numline = 0;
	private static ArrayList<StringBuilder> results = new ArrayList<>();

	public static void main(String[] args) throws IOException {

		List<Vtest> a = new ArrayList<>();

		Jointest js = new Jointest();

		Jointest.Vtest v1 = js.new Vtest(80, 0, 5);
		Jointest.Vtest v2 = js.new Vtest(90, 0, 3);
		Jointest.Vtest v3 = js.new Vtest(100, 0, 10);
		Jointest.Vtest v4 = js.new Vtest(110, 0, 4);
		Jointest.Vtest v5 = js.new Vtest(200, 0, 12);

		a.add(v1);
		a.add(v2);
		a.add(v3);
		a.add(v4);

		for (int i = 110; i < 130; i++) {
			a.add(js.new Vtest(i, 0, i - 110));
		}

		List<Vtest> b = new ArrayList<>();

		b.add(v1);
		b.add(v2);
		b.add(v3);
		b.add(v4);
		b.add(v5);

		for (int i = 200; i < 230; i++) {
			b.add(js.new Vtest(i, 0, i - 110));
		}

		TestFast tt = new TestFast();

		System.out.println("Started...");
		// tt.openingFile();
		// tt.createResults2(a, b);
		// tt.finish();
		long startTime = System.nanoTime();
		tt.openingFile();
		createResults(a, b, ">");
		tt.finish();

		System.out.println(("Global execution time " + (System.nanoTime() - startTime) / 1_000_000) + "ms\n");
	}

	private void openingFile() throws IOException {
		outputWriter = new BufferedWriter(new FileWriter(new File(directory + "/results-new.txt")));
	}

	/**
	 * Send the two sorted list with the operator >, <. Note that the sorting
	 * will differ according to the define operator for the predicate, i.e. if
	 * a[i] < b[i], sort a and b list in ascending order. Otherwise if a[i] >
	 * b[i] sort the list in descending order.
	 * 
	 * @param a
	 * @param b
	 * @throws IOException
	 */

	private static void createResults(List<Vtest> a, List<Vtest> b, String bop) throws IOException {

		boolean now = true;
		int start = b.size() - 1;
		/// int end = b.size();
		int pos = 0;
		int numofsetbits = 0;
		int[] setbits = new int[b.size()];
		// BitSet check = new BitSet(b.size());
		for (int i = a.size() - 1; i >= 0; i--) {

			setbits = new int[b.size()];
			if (a.get(i).p == 80) {
				System.out.println();
			}
			numofsetbits = 0;
			start = b.size() - 1;
			// check.clear();
			pos = b.size() - 1;
			while (start >= 0) {
				if (b.get(start).p > a.get(i).p && a.get(i).t < b.get(start).t) {
					// check.set(pos);
					/*
					 * if (i == 0 && now) { check.clear(1); check.clear(3); now
					 * = false; }
					 */
					setbits[pos] = start;
					pos--;

					// outputCombinations2(a.get(i).p, b, start, b.size(),
					// check);
					start--;
					// end--;
					numofsetbits++;
					/// create the
					/// matches over
					/// from start till the end of the list produce the patterns
				} /// here
				else if (b.get(start).p > a.get(i).p && a.get(i).t > b.get(start).t) {
					start--;
					// check.clear(pos);
					// pos++;
				} else
					break;

			}

			if (numofsetbits > 0)
				outputCombinations2(a.get(i).p, b, numofsetbits, b.size() - 1, setbits);

		}

	}

	private static void outputCombinations2(int element, List<Vtest> b, int pos, int end, int[] setbits)
			throws IOException {

		// int diff = pos;// end - start;

		// Set<BitSet> dup = new HashSet<BitSet>();
		for (int i = 1; i < Math.pow(2, (pos)); i++) {

			BitSet bs = BitSet.valueOf(new long[] { i });

			// bs.and(check);

			/*
			 * if (bs.isEmpty() || dup.contains(bs)) continue; else { //
			 * System.out.print("A: " + element);
			 * outputWriter.write(Integer.toString(element)); dup.add(bs); }
			 */
			outputWriter.write(Integer.toString(element));
			for (int j = bs.previousSetBit(pos); j >= 0; j = bs.previousSetBit(j - 1)) {
				// operate on index i here
				if (j == Integer.MIN_VALUE) {
					break; // or (i+1) would overflow
				}
				/// get the set bits from the array

				outputWriter.write("," + b.get(setbits[end - j]));
			}

			/*
			 * for (int k = bs.previousSetBit(bs.size()); k >= 0; k =
			 * bs.previousSetBit(k - 1)) { // operate on index i here if (k ==
			 * Integer.MIN_VALUE) { break; // or (i+1) would overflow }
			 * outputWriter.write("," + b.get(end - k - 1)); //
			 * System.out.print(", " + b.get(end - k - 1));
			 * 
			 * }
			 */
			// System.out.println();
			outputWriter.newLine();

		}

	}

	private static void outputCombinations(int element, List<Integer> b, int start, int end, BitSet check)
			throws IOException {

		int diff = end - start;
		StringBuilder sb = new StringBuilder();
		for (int i = 1; i < Math.pow(2, (diff)); i++) {
			// System.out.print(element);

			outputWriter.write(Integer.toString(element));
			// String st = Integer.toString(element);
			// fileStore.writeBytes(st);
			sb = new StringBuilder();
			// sb.append(element);
			char[] s = Integer.toBinaryString(i).toCharArray();

			for (int j = 0; j < s.length; j++) {

				if (s[j] == '1') {

					// System.out.println(diff + j);

					if (s.length < diff) {

						int index = j + diff - s.length;

						if (check.get(index + 1)) {

							sb.append("," + b.get(start + index));
							outputWriter.write("," + b.get(start + index));
							System.out.println(b.get(start + index));
						}
					} else {

						if (check.get(j + 1)) {

							System.out.println(b.get(start + j));
							sb.append("," + b.get(start + j));
							outputWriter.write("," + b.get(start + j));
						}
					}

				}
			}

			/// System.out.println(sb.toString().getBytes().length);

			// int size = sb.toString().getBytes().length;

			// fileStore.writeChars(sb.toString());
			// results.add(sb);

			outputWriter.newLine();
			// fileStore.writeChars(linesep);
			// numline++;
			// = linecounter +
			// size + 1;

			// System.out.print(sb.toString());
			// System.out.println();

			// long currentPosition = fileStore.getFilePointer();

			/// System.out.println(currentPosition);

		}

	}

	private static void resuseResults(int element) throws IOException {

		ArrayList<StringBuilder> asb = new ArrayList<>();
		for (StringBuilder sb : results) {
			String r2 = sb.substring(sb.indexOf(",") + 1, sb.length());
			sb.insert(0, element + ",");
			asb.add(sb);
			outputWriter.write(sb.toString());
			outputWriter.newLine();
			StringBuilder sb2 = new StringBuilder();
			sb2.append(element);
			sb2.append(",");
			sb2.append(r2);
			asb.add(sb2);
			outputWriter.write(sb2.toString());
			outputWriter.newLine();

		}
		results = asb;

	}

	private void finish() {
		// Close writer
		try {
			outputWriter.close();
		} catch (final IOException e) {
			// logger.error("Cannot property close the output file for query " +
			// id, e);
		}

	}

	public static BitSet convert(long value) {
		BitSet bits = new BitSet();
		int index = 0;
		while (value != 0L) {
			if (value % 2L != 0) {
				bits.set(index);
			}
			++index;
			value = value >>> 1;
		}
		return bits;
	}

}
