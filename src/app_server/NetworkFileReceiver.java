package app_server;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class NetworkFileReceiver {
	
	private final int fileInfoBufferSize = 512;
	private final int sizeStartOffset = 1;
	private final int pathStartIndex = 9;
	private InputStream inStream;
	private byte[] readBuffer;
	private byte[] fileInfoBuffer = new byte[fileInfoBufferSize];
	private int readBytes;
	
	public NetworkFileReceiver(InputStream in, int bufferSize) throws IOException{
		inStream = in;
		readBuffer = new byte[bufferSize];
	}
	
//	public void receiveAndWriteFiles(String pathPrefix) throws IOException{
//		inStream.read(readBuffer);
//		int numberOfFiles = 0;
//		for (int byteToIntOffset = 0; byteToIntOffset < 4; byteToIntOffset++) {
//	        int shift = (4 - 1 - byteToIntOffset) * 8;
//	        numberOfFiles += (readBuffer[byteToIntOffset] & 0x000000FF) << shift;
//	    }
//		File currentFile;
//		for(int outerCounter = 0; outerCounter < numberOfFiles; outerCounter++){
//			inStream.read(fileInfoBuffer);
//			String currentFileName = new String(fileInfoBuffer, "UTF-8");
//			currentFileName = pathPrefix + currentFileName;
//			//TODO: testing shit
////			System.out.println(currentFileName);
////			currentFileName = "F://Tmp/testFile.pdf";
//			currentFile = new File(currentFileName);
//			File parentFile = currentFile.getParentFile();
//			if(!parentFile.exists() && !parentFile.mkdirs()){
//			    throw new IllegalStateException("Couldn't create dir: " + parentFile);
//			}
//			try(FileOutputStream fileOutStream = new FileOutputStream(currentFile)){
//				while((readBytes = inStream.read(readBuffer)) > 0){
//					fileOutStream.write(readBuffer, 0, readBytes);
//				}
//			}
//		}
//	}
	
	public void receiveAndWriteFilesFromDeltas(String pathPrefix) throws IOException{
		inStream.read(readBuffer, 0, 4);
		if(pathPrefix.charAt(pathPrefix.length() - 1) != '/'){
			pathPrefix += "/";
		}
		int numberOfFiles = Utils.byteToInt(readBuffer, 0);
		System.out.println(numberOfFiles);
		File currentFile;
		for(int outerCounter = 0; outerCounter < numberOfFiles; outerCounter++){
			readBytes = inStream.read(fileInfoBuffer);
			long currentFileSize = Utils.byteToLong(fileInfoBuffer, sizeStartOffset);
			System.out.println(currentFileSize);
			String currentFileName = new String(fileInfoBuffer, "UTF-8").substring(pathStartIndex, readBytes);
			currentFileName = pathPrefix + currentFileName;
			currentFileName = currentFileName.replace("\\", "/");
			currentFile = new File(currentFileName);
			File parentFile = currentFile.getParentFile();
			if(!parentFile.exists() && !parentFile.mkdirs()){
			    throw new IllegalStateException("Couldn't create dir: " + parentFile);
			}
			System.out.println(currentFile.getAbsolutePath());
			if(fileInfoBuffer[0] != Delta.FILE_REMOVED){
				try(FileOutputStream fileOutStream = new FileOutputStream(currentFile)){
					readBytes = 0;
					for(int counter = 0; counter < currentFileSize; counter = counter + readBytes){
						readBytes = inStream.read(readBuffer);
						fileOutStream.write(readBuffer, 0, readBytes);
					}
				}
			} else{
				currentFile.delete();
			}
		}
	}

}
