package app_server;

public class StringPair {

	private String filePath;
	private String fileHash;
	
	public String getFilePath() {
		return filePath;
	}
	
	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}
	
	public String getFileHash() {
		return fileHash;
	}
	
	public void setFileHash(String fileHash) {
		this.fileHash = fileHash;
	}

	@Override
	public String toString() {
		return filePath + ", " + fileHash;
	}
	
	StringPair(String filePath, String fileHash){
		this.filePath = filePath;
		this.fileHash = fileHash;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
		    return false;
		  }
		  if (obj == this) {
		    return true;
		  }
		  if (obj.getClass() == this.getClass()) {
		    StringPair b = (StringPair)obj;
		    if (this.fileHash.equals(b.getFileHash()) && this.filePath.equals(b.getFilePath())) {
		      return true;
		    }
		  }
		  return false;
	}
	
	
	
}
