package app_server;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Vector;

public class NetworkFileSender {
	
	private final int hashLength = 64;
	private final int intAsByteLength = 4;
	private final int deltaIndex = 0;
	private final int fileSizeStartOffsetDelta = 1;
	private final int bufferSizeStartOffsetDelta = 9;
	private final int pathStartIndexDelta = 13;
	private final int fileSizeStartOffsetFiles = 0;
	private final int bufferSizeStartOffsetFiles = 8;
	private final int pathStartIndexFiles = 12;
	private BufferedOutputStream outStream;
	private byte[] writeBuffer;
	private int readBytes;
	
	public NetworkFileSender(OutputStream out, int bufferSize){
		outStream = new BufferedOutputStream(out);
		writeBuffer = new byte[bufferSize];
	}
	
	/**
	 * Writes files (name is in the deltalist) to the outputstream that was initialised in the constructor
	 * Schema is as follows:
	 * 		first 4 bytes are an Integer specifying the number of files
	 * 		for each file the first byte is the delta
	 * 			byte 1-8 are a long specifying the length of the file (call it l)
	 * 			byte 9-12 are a integer giving the last index of the file info part (call it x)
	 * 			byte 13-x is the filename as a UTF-8 encoded String
	 * 			the next l bytes are the contents of the file
	 * see also the NetworkFileSender.sendFilesFromDeltas method
	 * @param deltaList
	 * @param pathPrefix
	 * @throws IOException
	 */
	public void sendFilesFromDeltas(Vector<Delta> deltaList, String pathPrefix) throws IOException{
		int numberOfFiles = deltaList.size();
		outStream.write(Utils.intToByte(numberOfFiles));
		outStream.flush();
		if(pathPrefix.charAt(pathPrefix.length() - 1) != '/'){
			pathPrefix += "/";
		}
		for(Delta currentDelta: deltaList){
			sendData(currentDelta.getFilePath(), pathPrefix, fileSizeStartOffsetDelta, bufferSizeStartOffsetDelta,
					pathStartIndexDelta, currentDelta.getFileStatusFlag());
		}
	}
	

	public void sendFiles(Vector<String> fileList, String pathPrefix) throws IOException {
		int numberOfFiles = fileList.size();
		outStream.write(Utils.intToByte(numberOfFiles));
		outStream.flush();
		if(pathPrefix.charAt(pathPrefix.length() - 1) != '/'){
			pathPrefix += "/";
		}
		for(String currentFilePath: fileList){
			sendData(currentFilePath, pathPrefix, fileSizeStartOffsetFiles, bufferSizeStartOffsetFiles, pathStartIndexFiles,
					0);
		}
	}
	
	private void sendData(String filePath, String pathPrefix, int fileSizeStartOffset, int bufferSizeStartOffset,
			int pathStartIndex, int delta) throws IOException{
		File currentFile;
		byte[] fileSizeAsByte, bufferSizeAsByte, filePathBytes;
		currentFile = new File(pathPrefix + filePath);
		//set the status flag of the file in the first byte of the fileInfo
		if(delta != 0){
			writeBuffer[deltaIndex] = (byte) delta;
		}
		fileSizeAsByte = Utils.longToByte(currentFile.length());
		System.out.println(currentFile.length());
		Utils.copyBytesToArray(fileSizeAsByte, writeBuffer, fileSizeStartOffset);
		bufferSizeAsByte = Utils.intToByte(filePath.length() + pathStartIndex);
		Utils.copyBytesToArray(bufferSizeAsByte, writeBuffer, bufferSizeStartOffset);
		if((delta != 0 || delta == Delta.FILE_ADDED || delta == Delta.FILE_CHANGED) 
				&& currentFile.exists()){				
			//write the filename to the fileInfoBuffer
			filePathBytes = filePath.getBytes("UTF-8");
			//no more iterations than the length of the file name or the size of the array
			//truncates a filename if it is longer than writebufferSize - pathStartIndex letters
			try{
				Utils.copyBytesToArray(filePathBytes, writeBuffer, pathStartIndex);
			} catch(IndexOutOfBoundsException e){
				System.err.println(e.getMessage());
			}
		}
		System.out.println(filePath + ", " + Utils.byteToInt(writeBuffer, bufferSizeStartOffset));
		outStream.write(writeBuffer, 0, filePath.length() + pathStartIndex);
		outStream.flush();
		//if the file was removed we do not need to send any data over to the client
		//the client will see that the FILE_REMOVED flag is set and remove the file on his side
		if(delta != Delta.FILE_REMOVED){
			try(FileInputStream fileInStream = new FileInputStream(currentFile)){
				while((readBytes = fileInStream.read(writeBuffer)) > 0){
					outStream.write(writeBuffer, 0, readBytes);
					outStream.flush();
				}
			}
		}
	}

	/**
	 * writes the fileInfolist to the outStream, the first 4 byte are the number of files
	 * the schema for each file is as follows:
	 * 		byte 0 - 3 are the index of the last byte for the current file
	 * 		byte 4 - 67 are the fileHash as UTF-8 encoded String
	 * 		byte 67 - end is the filePath as UTF-8 encoded String
	 * @param fileInfoList
	 * @throws IOException
	 */
	public void sendFileInfoList(Vector<StringPair> fileInfoList) throws IOException{
		int numberOfFiles = fileInfoList.size();
		outStream.write(Utils.intToByte(numberOfFiles));
		outStream.flush();
		for(StringPair currentItem: fileInfoList){
			Utils.copyBytesToArray(Utils.intToByte(intAsByteLength + hashLength + currentItem.getFilePath().length()), writeBuffer, 0);
			Utils.copyBytesToArray(currentItem.getFileHash().getBytes("UTF-8"), writeBuffer, intAsByteLength);
			Utils.copyBytesToArray(currentItem.getFilePath().getBytes("UTF-8"), writeBuffer, intAsByteLength + hashLength);
			outStream.write(writeBuffer, 0, intAsByteLength + hashLength + currentItem.getFilePath().length());
			outStream.flush();
		}
	}
}
