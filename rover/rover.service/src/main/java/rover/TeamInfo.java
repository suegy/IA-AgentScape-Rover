package rover;

import org.iids.aos.log.Log;

public class TeamInfo {

	private String teamName;	
	private double baseX;
	private double baseY;
	
	private int roverCount;
	
	private int collectedCount;
	
	public TeamInfo(String name, double x, double y) {
		teamName = name;
		baseX = x;
		baseY = y;
		roverCount = 0;
		collectedCount = 0;
	}
	
	public String getTeamName() {
		return teamName;
	}

	public void setTeamName(String teamName) {
		this.teamName = teamName;
	}

	public double getBaseX() {
		return baseX;
	}

	public void setBaseX(double baseX) {
		this.baseX = baseX;
	}

	public double getBaseY() {
		return baseY;
	}

	public void setBaseY(double baseY) {
		this.baseY = baseY;
	}

	public int getCollectedCount() {
		return collectedCount;
	}

	public void setCollectedCount(int collectedCount) {
		this.collectedCount = collectedCount;
		Log.console("Team " + teamName + ": " + collectedCount + " resources collected.");
	}

	public int getRoverCount() {
		return roverCount;
	}

	public void setRoverCount(int roverCount) {
		this.roverCount = roverCount;
	}

	
}
