package rover;

import java.io.Serializable;
import java.util.ArrayList;

public class PollResult implements Serializable {

	
	private static final long serialVersionUID = 1L;
	
	public static final int MOVE = 1;
	public static final int SCAN = 2;
	public static final int COLLECT = 3;
	public static final int DEPOSIT = 4;
	
	public static final int WORLD_STARTED = 10;
	public static final int WORLD_STOPPED = 11;
    public static final int ROVER_UNKNOWN = 12;
	
	
	public static final int COMPLETE = 1;
	public static final int CANCELLED = 2;
	public static final int FAILED = 3;
	
	//things that can be sensed on the scanner
	public static final int RESOURCE = 1;
	public static final int ROVER = 2;
	public static final int BASE = 3;
		
		
			
	private int resultStatus;
	private int resultType;
		
	private ScanItem[] scanItems;
		
	private double percentComplete;
	
	public PollResult(int resultType, int resultStatus) {
		this.resultStatus = resultStatus;
		this.resultType = resultType;
			
		this.scanItems = null;
		
		if(resultStatus == COMPLETE) {
			percentComplete = 1;
		} else {
			percentComplete = 0;
		}
	}

	public int getResultStatus() {
		return resultStatus;
	}

	public void setResultStatus(int resultStatus) {
		this.resultStatus = resultStatus;
	}

	public int getResultType() {
		return resultType;
	}

	public void setResultType(int resultType) {
		this.resultType = resultType;
	}

	public ScanItem[] getScanItems() {
		return scanItems;
	}

	public void setScanItems(ScanItem[] scanItems) {
		this.scanItems = scanItems;
	}

	public double getPercentComplete() {
		return percentComplete;
	}

	public void setPercentComplete(double percentComplete) {
		this.percentComplete = percentComplete;
	}
		
}
