package org.labhc.zvalueencoder;

/**
 * Interface to Generate and Decode Z-Values
 * 
 * @author sydgillani
 *
 */
public interface Zvalues {

	/**
	 * Generate Z-Values of the given int array. At some point should extend it
	 * for long
	 * 
	 * @param values
	 * @return
	 * @throws DimensionException
	 */
	public boolean[] generate(int[] values) throws DimensionException;

	/**
	 * To decode the Z-values back to given values
	 * 
	 * @param zadd
	 * @return
	 */

	public int[] reverse(boolean[] zadd);

	/**
	 * To get the output of decoded values in an int array
	 * 
	 * @param values
	 * @return
	 */
	public int[] decodeValues(String[] values);
}
