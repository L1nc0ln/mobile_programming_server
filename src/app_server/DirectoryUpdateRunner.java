package app_server;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

public class DirectoryUpdateRunner extends Thread implements Runnable {
	
	private String directory;
	private ConcurrentHashMap<String, String> fileMap;
	private int sleepTime;
	private FileHasher fileHasher;
	private FileTracker fileTracker;
	


	public DirectoryUpdateRunner(String directory, ConcurrentHashMap<String, String> fileMap, int sleepTime, FileHasher fileHasher,
			FileTracker fileTracker) {
		this.directory = directory;
		this.fileMap = fileMap;
		this.sleepTime = sleepTime;
		this.fileHasher = fileHasher;
		this.fileTracker = fileTracker;
	}

	@Override
	public void run() {
		//check if FileHashes.txt exists
		File hashMapFile = new File("FileHashes.txt");
		if(hashMapFile.exists()){
			try {
				fileTracker.readHashMapFromFile(fileMap);
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else{
			fileHasher.getFileHashesForDir(directory, fileMap);
			fileTracker.writeHashMapToFile(fileMap);
		}
		hashMapFile = null;
		//check if Revisions.txt exists
		File revisionsFile = new File("Revisions.txt");
		if(revisionsFile.exists()){
			try {
				fileTracker.readRevisionsFromFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else{
			Revision firstRevision = new Revision(FileTracker.getCurrentRevisionNumber());
			for(String filePath: fileMap.keySet()){
				firstRevision.addDelta(new Delta(filePath, Delta.FILE_ADDED));
			}
			fileTracker.addRevisionWithWriteToFile(firstRevision);
		}
		revisionsFile = null;
		Revision revision;
		while(true){
			try {
				Thread.sleep(sleepTime);
				if(ServerMain.serverShutdown.get()){
					break;
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			revision = fileHasher.updateHashesForDir(directory, fileMap);
			if(revision != null){
				fileTracker.addRevisionWithWriteToFile(revision);
				fileTracker.writeHashMapToFile(fileMap);
			}
		}
		System.out.println("received shutdown signal - shutting down now");

	}

}
