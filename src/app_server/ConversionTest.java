package app_server;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

public class ConversionTest {

	public static void main(String[] args) throws FileNotFoundException, IOException {
//		int testNumber = 6450;
//		byte[] testArray = new byte[10];
//		
//		testArray[0] = (byte)((testNumber >>> 24));
//		testArray[1] = (byte)((testNumber >>> 16));
//		testArray[2] = (byte)((testNumber >>> 8));
//		testArray[3] = (byte)(testNumber);
//		
////		int numberAfter = 0;
////	    for (int i = 0; i < 4; i++) {
////	        int shift = (4 - 1 - i) * 8;
////	        numberAfter += (testArray[i] & 0x000000FF) << shift;
////	    }
////		System.out.println(numberAfter);
//	    
//	    String testString = "asdf";
////	    byte[] stringArray = new byte[testString.length() + 1];
//	    byte[] tmp = testString.getBytes("UTF-8");
//	    for(int i = 0; i < tmp.length; i++){
//	    	testArray[i + 4] = tmp[i];
//	    }
//	    //stringArray[testString.length()] = (byte)'\0';
//	    String stringAfter = new String(testArray, "UTF-8");
//	    
//	    System.out.println(stringAfter.substring(4));
//		String test = "test";
//		try(FileOutputStream fout = new FileOutputStream(new File("F:\\Test\\textures\\fx\\flare_refl_rainbow.tex"))){
//			fout.write(test.getBytes());
//			fout.flush();
//		}
		byte[] fileSizeAsByte = Utils.longToByte(349695L);
		byte[] fileInfoBuffer = new byte[10];
		for(int index = 0; index < 8; index++){
			fileInfoBuffer[index + 1] = fileSizeAsByte[index];
		}
		System.out.println(Utils.byteToLong(fileInfoBuffer, 1));

	}

}
