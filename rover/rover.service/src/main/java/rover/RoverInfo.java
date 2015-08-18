package rover;


import rover.tasks.Task;

import java.util.HashSet;
import java.util.Set;

public class RoverInfo {

	//the roverclient for callbacks
	//private RoverClient client;
	
	private String clientKey;
	private Set<String> messages;
	//rover's team
	private TeamInfo team;
	
	// current location
	private double x;
	private double y;
	
	private Task task;

	private int speed;
	private int scanRange;
	private int maxLoad;
	
	private int currentLoad;
	
	private double energy;
	
	private PollResult pollResult;
	
	private RoverService impl;
	
	public RoverInfo(RoverService impl, TeamInfo team) {
		this.team = team;
		this.impl = impl;
		this.messages = new HashSet<String>();
		this.x = team.getBaseX();
		this.y = team.getBaseY();
		
		this.pollResult = null;
		this.task = null;
	}
	
	public int getSpeed() {
		return speed;
	}

	public void setSpeed(int speed) {
		this.speed = speed;
	}

	public int getScanRange() {
		return scanRange;
	}

	public void setScanRange(int scanRange) {
		this.scanRange = scanRange;
	}

	public int getMaxLoad() {
		return maxLoad;
	}

	public void setMaxLoad(int maxLoad) {
		this.maxLoad = maxLoad;
	}

	
	
	public TeamInfo getTeam() {
		return team;
	}

	public void setTeam(TeamInfo team) {
		this.team = team;
	}

	public double getX() {
		return x;
	}



	public void setX(double x) {
		while(x < 0) {
			x = impl.getWidth() + x;
		}
		while(x > impl.getWidth()) {
			x = x - impl.getWidth();
		}
		
		this.x = x;
		
	}

	public double getY() {
		return y;
	}

	public void setY(double y) {
		
		while(y < 0) {
			y = impl.getHeight() + y;
		}
		while(y > impl.getHeight()) {
			y = y - impl.getHeight();
		}
		
		this.y = y;
	}

	public Task getTask() {
		return task;
	}

	public void setTask(Task task) {
		this.task = task;
	}

	public PollResult getPollResult() {
		return pollResult;
	}

	public void setPollResult(PollResult pollResult) {
		this.pollResult = pollResult;
	}

	public String getClientKey() {
		return clientKey;
	}

	public void setClientKey(String clientKey) {
		this.clientKey = clientKey;
	}

	public double getEnergy() {
		return energy;
	}

	public void setEnergy(double energy) {
		this.energy = energy;
	}

	public int getCurrentLoad() {
		return currentLoad;
	}

	public void setCurrentLoad(int currentLoad) {
		this.currentLoad = currentLoad;
	}


    public void receiveMessage(String message) {
        messages.add(message);
    }

    public String[] retrieveMessages() {
        String[] result;
        synchronized (messages){
            result = messages.toArray(new String[0]);
            messages.clear();
        }
        return result;
    }
}
