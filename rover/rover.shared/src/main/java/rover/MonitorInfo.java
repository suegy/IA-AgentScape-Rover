package rover;

import java.io.Serializable;
import java.util.ArrayList;

public class MonitorInfo  implements Serializable {

	public abstract class WorldObject implements Serializable {
		private double x;
		private double y;
		
		public WorldObject(double x, double y) {
			this.x = x;
			this.y = y;
		}
		
		public double getX() {
			return x;
		}
		public void setX(double x) {
			this.x = x;
		}
		public double getY() {
			return y;
		}
		public void setY(double y) {
			this.y = y;
		}
	}
	
	public class Team extends WorldObject implements Serializable {
		
		private String teamName;
		private int totalCollected;
		
		public Team(double x, double y, String name, int collected ){
			super(x,y);
			teamName = name;
			totalCollected = collected;
		}
		
		public String getTeamName() {
			return teamName;
		}
		public void setTeamName(String teamName) {
			this.teamName = teamName;
		}
		public int getTotalCollected() {
			return totalCollected;
		}
		public void setTotalCollected(int totalCollected) {
			this.totalCollected = totalCollected;
		}
	}
	
	public class Rover extends WorldObject implements Serializable {
		private String key;
		private int speed;
		private int scanRange;
		private int maxLoad;		
		private int currentLoad;		
		private double energy;
		private int task;
		private double taskCompletion;
		
		public Rover(double x, double y, String key) {
			super(x,y);
			this.key = key;
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
		public int getCurrentLoad() {
			return currentLoad;
		}
		public void setCurrentLoad(int currentLoad) {
			this.currentLoad = currentLoad;
		}
		public double getEnergy() {
			return energy;
		}
		public void setEnergy(double energy) {
			this.energy = energy;
		}
		public int getTask() {
			return task;
		}
		public void setTask(int task) {
			this.task = task;
		}
		public double getTaskCompletion() {
			return taskCompletion;
		}
		public void setTaskCompletion(double taskCompletion) {
			this.taskCompletion = taskCompletion;
		}

		public String getKey() {
			return key;
		}

		public void setKey(String key) {
			this.key = key;
		}		
	}
	
	public class Resource extends WorldObject implements Serializable {
		private int count;

		public Resource(double x, double y, int count) {
			super(x,y);
			this.count = count;
		}
		
		public int getCount() {
			return count;
		}

		public void setCount(int count) {
			this.count = count;
		}
	}
	
	private int width;
	private int height;
	
	private ArrayList<Team> teams;
	private ArrayList<Rover> rovers;
	private ArrayList<Resource> resources;
	
	public MonitorInfo(int w, int h) {
		teams = new ArrayList<Team>();
		rovers = new ArrayList<Rover>();
		resources = new ArrayList<Resource>();
		
		width = w;
		height = h;
	}

	public int getWidth() {
		return width;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public int getHeight() {
		return height;
	}

	public void setHeight(int height) {
		this.height = height;
	}

	public ArrayList<Team> getTeams() {
		return teams;
	}

	public void setTeams(ArrayList<Team> teams) {
		this.teams = teams;
	}

	public ArrayList<Rover> getRovers() {
		return rovers;
	}

	public void setRovers(ArrayList<Rover> rovers) {
		this.rovers = rovers;
	}

	public ArrayList<Resource> getResources() {
		return resources;
	}

	public void setResources(ArrayList<Resource> resources) {
		this.resources = resources;
	}
	
	
}
