package app_server;

public class Utils {

	/**
	 * 
	 * @param number the int to be transformed
	 * @return byte Array of size 4 with the transformed integer value
	 */
	public static byte[] intToByte(int number){
		return new byte[]{(byte)(number >>> 24), (byte)(number >>> 16), (byte)(number >>> 8), (byte)number};
	}
	
	/**
	 * 
	 * @param bytes the array containing the integer value
	 * @param startIndex the index from which the integer value starts
	 * @return the integer contained in the array
	 */
	public static int byteToInt(byte[] bytes, int startIndex){
		int returnValue = 0;
		for (int byteToIntOffset = 0; byteToIntOffset < 4; byteToIntOffset++) {
	        int shift = (4 - 1 - byteToIntOffset) * 8;
	        returnValue += (bytes[byteToIntOffset + startIndex] & 0x000000FF) << shift;
	    }
		return returnValue;
	}
	
	/**
	 * 
	 * @param bytes the array containing the long value
	 * @param startIndex the index from which the long value starts
	 * @return the long value contained in the array
	 */
	public static long byteToLong(byte[] bytes, int startIndex){
		long returnValue = 0;
		for (int byteToIntOffset = 0; byteToIntOffset < 8; byteToIntOffset++) {
	        int shift = (8 - 1 - byteToIntOffset) * 8;
	        returnValue += ((long)bytes[byteToIntOffset + startIndex] & 0x000000FF) << shift;
	    }
		return returnValue;
	}
	
	/**
	 * 
	 * @param number the long value to be transformed
	 * @return size 8 byte array containing the long value
	 */
	public static byte[] longToByte(long number){
		return new byte[]{(byte)(number >>> 56), (byte)(number >>> 48), (byte)(number >>> 40), (byte)(number >>> 32),
				(byte)(number >>> 24), (byte)(number >>> 16), (byte)(number >>> 8), (byte)number};
	}
	
	/**
	 * 
	 * @param source the array containing the wanted bytes
	 * @param startIndex the first index, included in the returned byte array
	 * @param endIndex the last index, included in the returned byte array
	 * @return a byte array containing the specified bytes
	 */
	public static byte[] getBytesFromTo(byte[] source, int startIndex, int endIndex){
		byte[] returnValue = new byte[endIndex - startIndex];
		for(int index = 0; index < endIndex - startIndex; index++){
			returnValue[index] = source[index + startIndex];
		}
		return returnValue;
	}

	/**
	 * moves length bytes from the source beginning to the indices starting from destBeginning
	 * @param byteArray array containing the bytes to be moved
	 * @param sourceBeginning starting index for the bytes to be moved
	 * @param length how many bytes should be moved
	 * @param destBeginning this is where the first byte from the source beginning will be moved
	 */
	public static void moveBytes(byte[] byteArray, int sourceBeginning, int length, int destBeginning) {
		if(sourceBeginning + length > byteArray.length || destBeginning + length > byteArray.length){
			throw new IndexOutOfBoundsException("Source or DestBeginning plus length is bigger than the size of the byteArray");
		} else {
			for(int index = 0; index < length; index++){
				byteArray[destBeginning + index] = byteArray[sourceBeginning + index];
			}
		}
	}
	
	/**
	 * copy all bytes from sourceArray to the destArray, with a potential offset in the destarray
	 * @param sourceArray
	 * @param destArray
	 * @param destOffset
	 */
	public static void copyBytesToArray(byte[] sourceArray, byte[] destArray, int destOffset){
		if(!(sourceArray.length + destOffset > destArray.length)){
			for(int index = 0; index < sourceArray.length; index++){
				destArray[index + destOffset] = sourceArray[index];
			}
		}
	}
	
}
