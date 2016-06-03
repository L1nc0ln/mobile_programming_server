package app_server;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Vector;

public class NetworkFileReceiver {
	
	private final int deltaIndex = 0;
	private final int fileSizeStartOffsetDelta = 1;
	private final int bufferSizeStartOffsetDelta = 9;
	private final int pathStartIndexDelta = 13;
	private final int fileSizeStartOffsetFiles = 0;
	private final int bufferSizeStartOffsetFiles = 8;
	private final int pathStartIndexFiles = 12;
	private final int sizeInteger = 4;
	private final int hashLength = 64;
	private BufferedInputStream inStream;
	private byte[] readBuffer;
	private int readBytes;
	private boolean overflow = false;
	
	public NetworkFileReceiver(InputStream in, int bufferSize) throws IOException{
		inStream = new BufferedInputStream(in);
		readBuffer = new byte[bufferSize];
	}
	
	/**
	 * Reads files with deltas from the inputStream initialised in the constructor
	 * Schema is as follows:
	 * 		first 4 bytes are an Integer specifying the number of files
	 * 		for each file the first byte is the delta
	 * 			byte 1-8 are a long specifying the length of the file (call it l)
	 * 			byte 9-12 are a integer giving the last index of the file info part (call it x)
	 * 			byte 13-x is the filename as a UTF-8 encoded String
	 * 			the next l bytes are the contents of the file
	 * see also the NetworkFileSender.sendFilesFromDeltas method
	 * @param pathPrefix gets put in front of the received path
	 * @throws IOException
	 */
	public void receiveAndWriteFilesFromDeltas(String pathPrefix) throws IOException{
		int numberOfFiles;
		if(pathPrefix.charAt(pathPrefix.length() - 1) != '/'){
			pathPrefix += "/";
		}
		readBytes = inStream.read(readBuffer, 0, sizeInteger);
		numberOfFiles = Utils.byteToInt(readBuffer, 0);
		for(int fileNumber = 0; fileNumber < numberOfFiles; fileNumber++){
			receiveAndWriteFile(pathPrefix, fileSizeStartOffsetDelta, bufferSizeStartOffsetDelta, pathStartIndexDelta, true);
		}
	}
	
	public void receiveAndWriteFileList(String pathPrefix) throws IOException{
		int numberOfFiles;
		if(pathPrefix.charAt(pathPrefix.length() - 1) != '/'){
			pathPrefix += "/";
		}
		readBytes = inStream.read(readBuffer, 0, sizeInteger);
		numberOfFiles = Utils.byteToInt(readBuffer, 0);
		for(int fileNumber = 0; fileNumber < numberOfFiles; fileNumber++){
			receiveAndWriteFile(pathPrefix, fileSizeStartOffsetFiles, bufferSizeStartOffsetFiles, pathStartIndexFiles, false);
		}
	}
	
	private void receiveAndWriteFile(String pathPrefix, int fileSizeStartOffset, int fileInfoLengthStartOffset,
			int pathStartIndex, boolean containsDelta) throws IOException{
		int fileInfoLength;
		long currentFileBytesLeft;
		File currentFile, parentFile;
		String currentFileName;
		readBytes = inStream.read(readBuffer);
		System.out.println(Arrays.toString(Utils.getBytesFromTo(readBuffer, 0, pathStartIndex)));
		currentFileBytesLeft = Utils.byteToLong(readBuffer, fileSizeStartOffset);
		fileInfoLength = Utils.byteToInt(readBuffer, fileInfoLengthStartOffset);
		if(fileInfoLength == readBytes){
			currentFileName = new String(Utils.getBytesFromTo(readBuffer, pathStartIndex, readBytes), "UTF-8");
		} else{
			System.out.println(fileInfoLength + ", " + readBytes);
			currentFileName = new String(Utils.getBytesFromTo(readBuffer, pathStartIndex, fileInfoLength), "UTF-8");
			overflow = true;
			currentFileBytesLeft = currentFileBytesLeft - (readBytes - fileInfoLength);
		}
		currentFileName = pathPrefix + currentFileName;
		currentFileName = currentFileName.replace("\\", "/");
		currentFile = new File(currentFileName);
		System.out.println(currentFile.getAbsolutePath());
		parentFile = currentFile.getParentFile();
		if(!parentFile.exists() && !parentFile.mkdirs()){
		    throw new IllegalStateException("Couldn't create dir: " + parentFile);
		}
		if(!containsDelta || (containsDelta && readBuffer[deltaIndex] != Delta.FILE_REMOVED)){
			try(FileOutputStream fileOutStream = new FileOutputStream(currentFile)){
				if(overflow){
					fileOutStream.write(readBuffer, fileInfoLength, readBytes - fileInfoLength);
					overflow = false;
				}
				while(currentFileBytesLeft > readBuffer.length){
					readBytes = inStream.read(readBuffer);
					fileOutStream.write(readBuffer, 0, readBytes);
					currentFileBytesLeft = currentFileBytesLeft - readBytes;
				}
				readBytes = inStream.read(readBuffer, 0, (int)currentFileBytesLeft);
				fileOutStream.write(readBuffer, 0, readBytes);
			}
		} else {
			currentFile.delete();
		}
	}
	
	public Vector<StringPair> receiveFileInfoList() throws IOException{
		int numberOfFiles, readBytes, fileInfoLength, fileInfoStartPointer;
		Vector<StringPair> fileInfoList = new Vector<StringPair>();
		StringPair currentPair;
		readBytes = inStream.read(readBuffer, 0, sizeInteger);
		numberOfFiles = Utils.byteToInt(readBuffer, 0);
		for(int index = 0; index < numberOfFiles; index++){
			readBytes = inStream.read(readBuffer);
			fileInfoLength = Utils.byteToInt(readBuffer, 0);
			if(readBytes == fileInfoLength){
				currentPair = new StringPair(
						new String(Utils.getBytesFromTo(readBuffer, sizeInteger + hashLength, readBytes), "UTF-8"),
						new String(Utils.getBytesFromTo(readBuffer, sizeInteger, sizeInteger + hashLength), "UTF-8"));
				fileInfoList.add(currentPair);
			} else {
				currentPair = new StringPair(
						new String(Utils.getBytesFromTo(readBuffer, sizeInteger + hashLength, fileInfoLength), "UTF-8"),
						new String(Utils.getBytesFromTo(readBuffer, sizeInteger, sizeInteger + hashLength), "UTF-8"));
				fileInfoList.add(currentPair);
				fileInfoStartPointer = sizeInteger + hashLength +  fileInfoLength;
				fileInfoLength = Utils.byteToInt(readBuffer, sizeInteger + hashLength + fileInfoLength);
				index ++;
				while(fileInfoStartPointer + fileInfoLength < readBuffer.length && fileInfoStartPointer + fileInfoLength <= readBytes){
					currentPair = new StringPair(
							new String(Utils.getBytesFromTo(readBuffer, fileInfoStartPointer + sizeInteger + hashLength, fileInfoStartPointer + fileInfoLength), "UTF-8"),
							new String(Utils.getBytesFromTo(readBuffer, fileInfoStartPointer + sizeInteger, fileInfoStartPointer + sizeInteger + hashLength), "UTF-8"));
					fileInfoList.add(currentPair);
					fileInfoStartPointer = fileInfoStartPointer + sizeInteger + hashLength +  fileInfoLength;
					fileInfoLength = Utils.byteToInt(readBuffer, fileInfoStartPointer + sizeInteger + hashLength + fileInfoLength);
					index++;
				}
			}
		}
		return fileInfoList;
	}

}
