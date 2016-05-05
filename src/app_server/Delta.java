package app_server;

public class Delta {
	
	private String filePath;
	private int fileStatusFlag;
	public static int FILE_ADDED = 1;
	public static int FILE_CHANGED = 2;
	public static int FILE_REMOVED = 4;
	
	public Delta(String filePath, int fileStatusFlag){
		this.filePath = filePath;
		if(fileStatusFlag == 1 | fileStatusFlag == 2 | fileStatusFlag == 4){
			this.fileStatusFlag = fileStatusFlag;
		}
	}

	public String getFilePath() {
		return filePath;
	}
	
	public int getFileStatusFlag(){
		return fileStatusFlag;
	}
	
	public boolean isFileChanged(){
		return fileStatusFlag == FILE_CHANGED;
	}
	
	public boolean isFileAdded(){
		return fileStatusFlag == FILE_ADDED;
	}
	
	public boolean isFileRemoved(){
		return fileStatusFlag == FILE_REMOVED;
	}

	@Override
	public String toString() {
		return filePath + ";" + fileStatusFlag;
	}

	
	
}
