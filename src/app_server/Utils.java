package app_server;

public class Utils {

	public static byte[] intToByte(int number){
		return new byte[]{(byte)(number >>> 24), (byte)(number >>> 16), (byte)(number >>> 8), (byte)number};
	}
	
	public static int byteToInt(byte[] bytes, int startIndex){
		int returnValue = 0;
		for (int byteToIntOffset = startIndex; byteToIntOffset < startIndex + 4; byteToIntOffset++) {
	        int shift = (4 - 1 - byteToIntOffset) * 8;
	        returnValue += (bytes[byteToIntOffset] & 0x000000FF) << shift;
	    }
		return returnValue;
	}
	
	public static long byteToLong(byte[] bytes, int startIndex){
		long returnValue = 0;
		for (int byteToIntOffset = 0; byteToIntOffset < 8; byteToIntOffset++) {
	        int shift = (8 - 1 - byteToIntOffset) * 8;
	        returnValue += ((long)bytes[byteToIntOffset + startIndex] & 0x000000FF) << shift;
	    }
		return returnValue;
	}
	
	public static byte[] longToByte(long number){
		return new byte[]{(byte)(number >>> 56), (byte)(number >>> 48), (byte)(number >>> 40), (byte)(number >>> 32),
				(byte)(number >>> 24), (byte)(number >>> 16), (byte)(number >>> 8), (byte)number};
	}
	
}
