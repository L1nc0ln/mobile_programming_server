package app_server;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Vector;

public class NetworkFileSender {
	
	private final int deltaIndex = 0;
	private final int fileSizeStartOffset = 1;
	private final int bufferSizeStartOffset = 9;
	private final int pathStartIndex = 13;
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
		File currentFile;
		byte[] fileSizeAsByte, bufferSizeAsByte, filePathBytes;
		int numberOfFiles = deltaList.size();
		outStream.write(Utils.intToByte(numberOfFiles));
		outStream.flush();
		if(pathPrefix.charAt(pathPrefix.length() - 1) != '/'){
			pathPrefix += "/";
		}
		for(Delta currentDelta: deltaList){
			currentFile = new File(pathPrefix + currentDelta.getFilePath());
			//set the status flag of the file in the first byte of the fileInfo
			writeBuffer[deltaIndex] = (byte) currentDelta.getFileStatusFlag();
			fileSizeAsByte = Utils.longToByte(currentFile.length());
			System.out.println(currentFile.length());
			for(int index = 0; index < fileSizeAsByte.length; index++){
				writeBuffer[index + fileSizeStartOffset] = fileSizeAsByte[index];
			}
			bufferSizeAsByte = Utils.intToByte(currentDelta.getFilePath().length() + pathStartIndex);
			for(int index = 0; index < bufferSizeAsByte.length; index++){
				writeBuffer[index + bufferSizeStartOffset] = bufferSizeAsByte[index];
			}
			if((currentDelta.getFileStatusFlag() == Delta.FILE_ADDED || currentDelta.getFileStatusFlag() == Delta.FILE_CHANGED) 
					&& currentFile.exists()){				
				//write the filename to the fileInfoBuffer
				filePathBytes = currentDelta.getFilePath().getBytes("UTF-8");
				//no more iterations than the length of the file name or the size of the array
				//truncates a filename if it is longer than writebufferSize - pathStartIndex letters
				for(int index = 0; index < currentDelta.getFilePath().length() && index < writeBuffer.length - pathStartIndex; index++){
					writeBuffer[index + pathStartIndex] = filePathBytes[index];
				}
			}
			System.out.println(currentDelta.getFilePath() + ", " + Utils.byteToInt(writeBuffer, bufferSizeStartOffset));
			outStream.write(writeBuffer, 0, currentDelta.getFilePath().length() + pathStartIndex);
			outStream.flush();
			//if the file was removed we do not need to send any data over to the client
			//the client will see that the FILE_REMOVED flag is set and remove the file on his side
			if(currentDelta.getFileStatusFlag() != Delta.FILE_REMOVED){
				try(FileInputStream fileInStream = new FileInputStream(currentFile)){
					while((readBytes = fileInStream.read(writeBuffer)) > 0){
						outStream.write(writeBuffer, 0, readBytes);
						outStream.flush();
					}
				}
			}
		}
	}
	
	//TODO: implement this method. Sends filename + hash
	public void sendFileInfoList(Vector<StringPair> fileInfoList) throws IOException{
		int numberOfFiles = fileInfoList.size();
		outStream.write(new byte[]{(byte)(numberOfFiles >>> 24), (byte)(numberOfFiles >>> 16), (byte)(numberOfFiles >>> 8), (byte)numberOfFiles});
		outStream.flush();
	}

}
