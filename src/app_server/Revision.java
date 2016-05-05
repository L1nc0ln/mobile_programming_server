package app_server;

import java.util.Vector;

public class Revision {

	private Vector<Delta> deltas;
	private int revisionNumber;
	
	public Revision(Vector<Delta> deltas, int revisionNumber){
		this.deltas = deltas;
		this.revisionNumber = revisionNumber;
	}
	
	public Revision(int revisionNumber) {
		deltas = new Vector<Delta>();
		this.revisionNumber = revisionNumber;
	}

	public int getRevisionNumber() {
		return revisionNumber;
	}

	public Vector<Delta> getDeltas(){
		return deltas;
	}
	
	public void addDelta(Delta delta){
		deltas.add(delta);
	}
	
	public int getNumberOfDeltas(){
		return deltas.size();
	}

	@Override
	public String toString() {
		String returnValue = revisionNumber + "\n";
		for(Delta currentDelta: deltas){
			returnValue += currentDelta + "\n";
		}
		returnValue += "#\n";
		return returnValue;
	}
	
	
	
}
