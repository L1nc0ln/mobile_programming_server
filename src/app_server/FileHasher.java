package app_server;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.ConcurrentHashMap;

public class FileHasher {

	private final int buffersize = 16384;
	private byte[] buffer = new byte[buffersize];
	private int bytesRead;
	private MessageDigest hashSum;
	private int filesAdded;
	
	public String getFileHash(String filename){
		try {
			hashSum = MessageDigest.getInstance("SHA-256");
		try(FileInputStream in = new FileInputStream(new File(filename));
		DigestInputStream digester = new DigestInputStream(in, hashSum)){
			bytesRead = digester.read(buffer, 0, buffersize);
			while(bytesRead == buffersize){
				bytesRead = digester.read(buffer, 0, buffersize);
			}
			byte[] hash = hashSum.digest();
			StringBuffer hexString = new StringBuffer();
	    	for (int i=0;i<hash.length;i++) {
	    		hexString.append(Integer.toString((hash[i] & 0xff) + 0x100, 16).substring(1));
	    	}
	    	return hexString.toString();
		} catch (IOException e) {
			e.printStackTrace();
		}
		} catch (NoSuchAlgorithmException e1) {
			e1.printStackTrace();
		}
		return "";
	}

	public void getFileHashesForDir(String directory, ConcurrentHashMap<String, String> fileMap){
		try {
			Files.walk(Paths.get(directory)).forEach(filePath -> {
			    if (Files.isRegularFile(filePath)) {
			        fileMap.put(filePath.toString(), getFileHash(filePath.toString()));
			    }
			});
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public Revision updateHashesForDir(String directory, ConcurrentHashMap<String, String> fileMap){
		//create second HashMap that tracks which files we had in our directory and which files we had not
		ConcurrentHashMap<String, Boolean> fileMapCopy = new ConcurrentHashMap<String, Boolean>(fileMap.size() * 4/3);
		for(String pathToFile: fileMap.keySet()){
			fileMapCopy.put(pathToFile, false);
		}
		Revision revision = new Revision(FileTracker.getCurrentRevisionNumber());
		filesAdded = 0;
		//go through the directory and all its subdirectories, if a file is new add it to the map and create a delta with FILE_ADDED,
		//if a file is in the map but has another hash there create a new delta with FILE_CHANGED and put the new hashValue in the map
		//for each file that is in the map set the boolean in the second hashmap to true to signal that we encountered that file
		try {
			Files.walk(Paths.get(directory)).forEach(filePath -> {
			    if (Files.isRegularFile(filePath)) {
			    	String hash = getFileHash(filePath.toString());
			    	if(fileMap.containsKey(filePath.toString())){
			    		fileMapCopy.put(filePath.toString(), true);
			    		if(!hash.equals(fileMap.get(filePath.toString()))){
			    			fileMap.put(filePath.toString(), hash);
			    			revision.addDelta(new Delta(filePath.toString(), Delta.FILE_CHANGED));
			    		}
			    	} else{
			    		fileMap.put(filePath.toString(), hash);
			    		revision.addDelta(new Delta(filePath.toString(), Delta.FILE_ADDED));
			    		filesAdded++;
			    	}
			    }
			});
		} catch (IOException e) {
			e.printStackTrace();
		}
		//check if files were removed
		for(String pathToFile: fileMapCopy.keySet()){
			if(!fileMapCopy.get(pathToFile)){
				revision.addDelta(new Delta(pathToFile.toString(), Delta.FILE_REMOVED));
			}
		}
		//if files changed send back the new revision with the deltas, else return null
		if(revision.getNumberOfDeltas() == 0){
			return null;
		} else{
			return revision;
		}
	}

}
