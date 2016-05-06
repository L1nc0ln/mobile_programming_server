package app_server;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Vector;

public class NetworkFileSender {
	
	private OutputStream outStream;
	private byte[] writeBuffer;
	private byte[] fileInfoBuffer = new byte[128];
	private int readBytes;
	
	public NetworkFileSender(OutputStream out, int bufferSize){
		outStream = out;
		writeBuffer = new byte[bufferSize];
	}
	
	public void sendFile(Vector<String> filePathList) throws IOException{
		int numberOfFiles = filePathList.size();
		outStream.write(new byte[]{(byte)(numberOfFiles >>> 24), (byte)(numberOfFiles >>> 16), (byte)(numberOfFiles >>> 8), (byte)numberOfFiles});
		outStream.flush();
		File currentFile;
		for(String filePath: filePathList){
			currentFile = new File(filePath);
			if(currentFile.exists()){				
				//write the filename to the fileInfoBuffer from byte 5+
				byte[] filePathBytes = filePath.getBytes("UTF-8");
				//no more iterations than the length of the file name or the size of the array
				//truncates a filename if it is longer than 124 letters
				for(int i = 0; i < filePath.length() && i < 128; i++){
					fileInfoBuffer[i] = filePathBytes[i];
				}
			}
			outStream.write(fileInfoBuffer);
			outStream.flush();
			try(FileInputStream fileInStream = new FileInputStream(currentFile)){
				while((readBytes = fileInStream.read(writeBuffer)) > 0){
					outStream.write(writeBuffer, 0, readBytes);
				}
			}
		}
	}

}
