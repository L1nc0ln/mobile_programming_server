package app_server;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Vector;

public class NetworkFileSender {
	
	private final int fileInfoBufferSize = 512;
	private final int sizeStartOffset = 1;
	private final int pathStartIndex = 9;
	private OutputStream outStream;
	private byte[] writeBuffer;
	private byte[] fileInfoBuffer = new byte[fileInfoBufferSize];
	private int readBytes;
	
	public NetworkFileSender(OutputStream out, int bufferSize){
		outStream = out;
		writeBuffer = new byte[bufferSize];
	}
	
//	public void sendFiles(Vector<String> filePathList) throws IOException{
//		int numberOfFiles = filePathList.size();
//		outStream.write(new byte[]{(byte)(numberOfFiles >>> 24), (byte)(numberOfFiles >>> 16), (byte)(numberOfFiles >>> 8), (byte)numberOfFiles});
//		outStream.flush();
//		File currentFile;
//		for(String filePath: filePathList){
//			currentFile = new File(filePath);
//			if(currentFile.exists()){				
//				//write the filename to the fileInfoBuffer
//				byte[] filePathBytes = filePath.getBytes("UTF-8");
//				//no more iterations than the length of the file name or the size of the array
//				//truncates a filename if it is longer than 128/fileInfoBufferSize letters
//				for(int index = 0; index < filePath.length() && index < fileInfoBufferSize; index++){
//					fileInfoBuffer[index] = filePathBytes[index];
//				}
//			}
//			outStream.write(fileInfoBuffer);
//			outStream.flush();
//			try(FileInputStream fileInStream = new FileInputStream(currentFile)){
//				while((readBytes = fileInStream.read(writeBuffer)) > 0){
//					outStream.write(writeBuffer, 0, readBytes);
//				}
//			}
//		}
//	}
	
	public void sendFilesFromDeltas(Vector<Delta> deltaList, String pathPrefix) throws IOException{
		int numberOfFiles = deltaList.size();
		outStream.write(new byte[]{(byte)(numberOfFiles >>> 24), (byte)(numberOfFiles >>> 16), (byte)(numberOfFiles >>> 8), (byte)numberOfFiles});
		File currentFile;
		if(pathPrefix.charAt(pathPrefix.length() - 1) != '/'){
			pathPrefix += "/";
		}
		for(Delta currentDelta: deltaList){
			currentFile = new File(pathPrefix + currentDelta.getFilePath());
			//set the status flag of the file in the first byte of the fileInfo
			fileInfoBuffer[0] = (byte) currentDelta.getFileStatusFlag();
			byte[] fileSizeAsByte = Utils.longToByte(currentFile.length());
			for(int index = 0; index < fileSizeAsByte.length; index++){
				fileInfoBuffer[index + sizeStartOffset] = fileSizeAsByte[index];
			}
			if((currentDelta.getFileStatusFlag() == Delta.FILE_ADDED || currentDelta.getFileStatusFlag() == Delta.FILE_CHANGED) 
					&& currentFile.exists()){				
				//write the filename to the fileInfoBuffer
				byte[] filePathBytes = currentDelta.getFilePath().getBytes("UTF-8");
				//no more iterations than the length of the file name or the size of the array
				//truncates a filename if it is longer than fileInfoBufferSize - 1 letters
				//index starts at 1 instead of 0 because the first bit is reserved for the status flag
				for(int index = 0; index < currentDelta.getFilePath().length() && index < fileInfoBufferSize - pathStartIndex; index++){
					fileInfoBuffer[index + pathStartIndex] = filePathBytes[index];
				}
			}
			if(currentDelta.getFilePath().length() + pathStartIndex < fileInfoBufferSize){
				System.out.println(currentDelta.getFilePath() + ", " + (currentDelta.getFilePath().length() + pathStartIndex));
				outStream.write(fileInfoBuffer, 0, currentDelta.getFilePath().length() + pathStartIndex);
			} else{
				outStream.write(fileInfoBuffer, 0, fileInfoBufferSize);
			}
			//if the file was removed we do not need to send any data over to the client
			//the client will see that the FILE_REMOVED flag is set and remove the file on his side
			if(currentDelta.getFileStatusFlag() != Delta.FILE_REMOVED){
				try(FileInputStream fileInStream = new FileInputStream(currentFile)){
					while((readBytes = fileInStream.read(writeBuffer)) > 0){
						outStream.write(writeBuffer, 0, readBytes);
					}
				}
			}
		}
	}
	
	public void sendFileInfoList(Vector<StringPair> fileInfoList) throws IOException{
		int numberOfFiles = fileInfoList.size();
		outStream.write(new byte[]{(byte)(numberOfFiles >>> 24), (byte)(numberOfFiles >>> 16), (byte)(numberOfFiles >>> 8), (byte)numberOfFiles});
		outStream.flush();
	}

}
