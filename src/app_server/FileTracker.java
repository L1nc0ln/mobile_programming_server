package app_server;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

public class FileTracker {
	
	private static int currentRevisionNumber = 0;
	private Vector<Revision> revisions;
	private ConcurrentHashMap<String, String> fileMap;
	private final int buffersize = 16384;
	private byte[] buffer = new byte[buffersize];
	private int bytesRead;
	private MessageDigest hashSum;
	private int filesAdded;

	public void addRevision(Revision newRevision){
		revisions.add(newRevision);
		currentRevisionNumber++;
	}
	
	public void addRevisionWithWriteToFile(Revision newRevision){
		revisions.add(newRevision);
		saveRevisionToFile(newRevision);
		currentRevisionNumber++;
	}
	
	public void saveRevisionToFile(Revision revision){
		try(FileWriter fileWriter = new FileWriter(new File("Revisions.txt"), true)){
			fileWriter.write(revision.toString());
			fileWriter.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void readRevisionsFromFile() throws FileNotFoundException, IOException{
		try(BufferedReader reader = new BufferedReader(new FileReader(new File("Revisions.txt")))){
			String currentLine = reader.readLine();
			Revision revision = new Revision(Integer.parseInt(currentLine));
			while(currentLine != null){
				currentLine = reader.readLine();
				if(currentLine.equals("#")){
					addRevision(revision);
					currentLine = reader.readLine();
					if(currentLine == null || currentLine.equals("\n")){
						break;
					}
					revision = new Revision(Integer.parseInt(currentLine));
				} else{
					String[] splitLine = currentLine.split(";");
					if(splitLine[1].equals("1")){
						revision.addDelta(new Delta(splitLine[0], 1));
					} else if(splitLine[1].equals("2")){
						revision.addDelta(new Delta(splitLine[0], 2));
					} else if(splitLine[1].equals("4")){
						revision.addDelta(new Delta(splitLine[0], 4));
					}
				}
			}
		}
	}
	
	public void writeHashMapToFile(){
		try(FileWriter fileWriter = new FileWriter(new File("FileHashes.txt"))){
			for(Map.Entry<String, String> currentFileHash: fileMap.entrySet()){
				fileWriter.write(currentFileHash.getKey() + ";" + currentFileHash.getValue() + "\n");
				fileWriter.flush();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void readHashMapFromFile() throws FileNotFoundException, IOException{
		try(BufferedReader reader = new BufferedReader(new FileReader(new File("FileHashes.txt")))){
			String currentLine = reader.readLine();
			while(currentLine != null){
				String[] currentLineSplit = currentLine.split(";");
				fileMap.put(currentLineSplit[0], currentLineSplit[1]);
				currentLine = reader.readLine();
			}
		}
	}
	
	public synchronized Vector<Delta> createTotalDelta(int startRevision){
		int counter = 0;
		Vector<Delta> deltaList = new Vector<Delta>();
		//for every revision 
		for(Revision currentRevision: revisions){
			//if it is above the revision of the client
			if(counter >= startRevision){
				//get every delta
				for(Delta currentDelta: currentRevision.getDeltas()){
					//and go through each delta already in the list
					for(Delta deltaFromList: deltaList){
						//and check if there is already a delta for the same file in there
						if(deltaFromList.getFilePath().equals(currentDelta.getFilePath())){
							if(deltaFromList.getFileStatusFlag() == Delta.FILE_ADDED){
								if(currentDelta.getFileStatusFlag() == Delta.FILE_CHANGED){
									deltaFromList = currentDelta;
									break;
								} else if(currentDelta.getFileStatusFlag() == Delta.FILE_REMOVED){
									deltaList.remove(deltaFromList);
									break;
								} else{
									break;
								}
							} else if(deltaFromList.getFileStatusFlag() == Delta.FILE_CHANGED){
								if(currentDelta.getFileStatusFlag() == Delta.FILE_CHANGED){
									deltaList.remove(deltaFromList);
									break;
								} else{
									break;
								}
							} else if(deltaFromList.getFileStatusFlag() == Delta.FILE_REMOVED){
								//TODO: what happens if a file gets removed and another file with the same path gets added?
								break;
							}
						}
					}
					//if no delta for the file was found add the delta to the list
					deltaList.add(currentDelta);
				}
			}
			counter++;
		}
		return deltaList;
	}
	
	public synchronized Vector<String> getChangedFiles(Vector<StringPair> filesFromClient){
		Vector<String> changedFiles = new Vector<String>();
		ConcurrentHashMap<String, Boolean> fileMapCopy = new ConcurrentHashMap<String, Boolean>(fileMap.size() * 4/3);
		for(String pathToFile: fileMap.keySet()){
			fileMapCopy.put(pathToFile, false);
		}
		for(StringPair currentStringPair: filesFromClient){
			if(fileMapCopy.get(currentStringPair.getFilePath()).equals(currentStringPair.getFileHash())){
				fileMapCopy.put(currentStringPair.getFilePath(), true);
			}
		}
		for(String pathToFile: fileMapCopy.keySet()){
			if(!fileMapCopy.get(pathToFile)){
				changedFiles.add(pathToFile);
			}
		}
		return changedFiles;
	}
	
	/**
	 * Calculates and returns the hash for a given file
	 * @param filename Path to the file
	 * @return the sha-256 hash for the given file
	 */
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

	/**
	 * Puts all files in the directory (and its subdirectories) in the fileMap (of this class),
	 * path as the key and the hashvalue as the value
	 * @param directory Path to the directory
	 */
	public void getFileHashesForDir(String directory){
		try {
			Files.walk(Paths.get(directory)).forEach(filePath -> {
			    if (Files.isRegularFile(filePath)) {
			        fileMap.put(filePath.toString().substring(directory.length()), getFileHash(filePath.toString()));
			    }
			});
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Compares the files in the given directory with all files in the fileMap of this class
	 * If a file was changed/added/deleted creates a Delta for the change and adds it to a Revision
	 * that is returned if there was a change in the directory
	 * @param directory directory to check for changes
	 * @return null if no change was detected, else a Revision object containing all changes that were detected
	 */
	public Revision updateHashesForDir(String directory){
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
			    	String relativePath = filePath.toString().substring(directory.length());
			    	String hash = getFileHash(filePath.toString());
			    	if(fileMap.containsKey(relativePath)){
			    		fileMapCopy.put(relativePath, true);
			    		if(!hash.equals(fileMap.get(relativePath))){
			    			fileMap.put(relativePath, hash);
			    			revision.addDelta(new Delta(relativePath, Delta.FILE_CHANGED));
			    		}
			    	} else{
			    		fileMap.put(relativePath, hash);
			    		revision.addDelta(new Delta(relativePath, Delta.FILE_ADDED));
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
				fileMap.remove(pathToFile);
			}
		}
		//if files changed send back the new revision with the deltas, else return null
		if(revision.getNumberOfDeltas() == 0){
			return null;
		} else{
			return revision;
		}
	}
	
	/**
	 * 
	 * @return the current Revision number
	 */
	public static int getCurrentRevisionNumber(){
		return currentRevisionNumber;
	}
	
	public FileTracker(){
		revisions = new Vector<Revision>();
		fileMap = new ConcurrentHashMap<String, String>();
	}

	/**
	 * 
	 * @return the fileMap saved in this class
	 */
	public ConcurrentHashMap<String, String> getFileMap() {
		return fileMap;
	}

}
