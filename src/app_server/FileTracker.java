package app_server;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

public class FileTracker {
	
	private static int currentRevisionNumber = 0;
	private Vector<Revision> revisions;

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
	
	public void writeHashMapToFile(ConcurrentHashMap<String, String> fileHashes){
		try(FileWriter fileWriter = new FileWriter(new File("FileHashes.txt"))){
			for(Map.Entry<String, String> currentFileHash: fileHashes.entrySet()){
				fileWriter.write(currentFileHash.getKey() + ";" + currentFileHash.getValue() + "\n");
				fileWriter.flush();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void readHashMapFromFile(ConcurrentHashMap<String, String> fileHashes) throws FileNotFoundException, IOException{
		try(BufferedReader reader = new BufferedReader(new FileReader(new File("FileHashes.txt")))){
			String currentLine = reader.readLine();
			while(currentLine != null){
				String[] currentLineSplit = currentLine.split(";");
				fileHashes.put(currentLineSplit[0], currentLineSplit[1]);
				currentLine = reader.readLine();
			}
		}
	}
	
	public Vector<Delta> createTotalDelta(int startRevision){
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
	
	public static int getCurrentRevisionNumber(){
		return currentRevisionNumber;
	}
	
	public FileTracker(){
		revisions = new Vector<Revision>();
	}

}
