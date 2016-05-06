package app_server;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class NetworkFileReceiver {
	
	private InputStream inStream;
	private byte[] readBuffer;
	private byte[] fileInfoBuffer = new byte[128];
	private int readBytes;
	
	public NetworkFileReceiver(InputStream in, int bufferSize) throws IOException{
		inStream = in;
		readBuffer = new byte[bufferSize];
	}
	
	public void receiveAndWriteFiles() throws IOException{
		inStream.read(readBuffer);
		int numberOfFiles = 0;
		for (int byteToIntOffset = 0; byteToIntOffset < 4; byteToIntOffset++) {
	        int shift = (4 - 1 - byteToIntOffset) * 8;
	        numberOfFiles += (readBuffer[byteToIntOffset] & 0x000000FF) << shift;
	    }
		File currentFile;
		for(int outerCounter = 0; outerCounter < numberOfFiles; outerCounter++){
			inStream.read(fileInfoBuffer);
			String currentFileName = new String(fileInfoBuffer, "UTF-8");
			//TODO: testing shit
			System.out.println(currentFileName);
			currentFileName = "F://Tmp/testFile.pdf";
			currentFile = new File(currentFileName);
			try(FileOutputStream fileOutStream = new FileOutputStream(currentFile)){
				while((readBytes = inStream.read(readBuffer)) > 0){
					fileOutStream.write(readBuffer, 0, readBytes);
				}
			}
		}
	}

}
