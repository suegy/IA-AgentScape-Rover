package rover.tasks;


import rover.RoverInfo;
import rover.RoverService;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;



public abstract class Task {

	private Date startTime;
	private Date endTime;
	
	private Date lastUpdateTime;
	
	private RoverInfo rover;
	private RoverService impl;
	
	private double energyPerSecond;
	
	public Task(RoverInfo rover, RoverService impl, int ms) {
		
		this.rover = rover;
		this.impl = impl;
		
		//adjust speed
		ms = ms / impl.getWorldSpeed();
		
		startTime = new Date();
		
		Calendar cal = new GregorianCalendar();
		cal.setTime(startTime);
		cal.add(Calendar.MILLISECOND, ms);
		
		endTime = cal.getTime();
	
		lastUpdateTime = (Date) startTime.clone();
		
		//Log.console("New task: " + startTime.toGMTString() + " to " + endTime.toGMTString());
		
	}
	
	public Date getStartTime() {
		return startTime;
	}

	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}

	public Date getEndTime() {
		return endTime;
	}

	public void setEndTime(Date endTime) {
		this.endTime = endTime;
	}


	public RoverInfo getRover() {
		return rover;
	}

	public void setRover(RoverInfo rover) {
		this.rover = rover;
	}

	public abstract void Update();
	public abstract void Complete();
	public abstract void Cancel();
	
	public abstract int taskNum();
	
	public double percentComplete() {
		long diff = endTime.getTime() - startTime.getTime();
		Date now = new Date();
		double pct = (now.getTime() - startTime.getTime()) / (double) diff;
		if(pct > 1) {
			pct = 1;
		}
		return pct;
	}

	public void adjustEnergy() {
		Date now = new Date();
		
		//make sure we don't use more energy than we should
		if(now.after(getEndTime())) {
			now = getEndTime();
		}
		
		double seconds = (now.getTime() - lastUpdateTime.getTime()) / 1000.0;
		
		//adjust seconds for world speed to make energy use accurate
		seconds *= impl.getWorldSpeed();
		
		rover.setEnergy(rover.getEnergy() - (seconds * getEnergyPerSecond()) );
		lastUpdateTime = now;		
	}
	
	public Date getLastUpdateTime() {
		return lastUpdateTime;
	}

	public void setLastUpdateTime(Date lastUpdateTime) {
		this.lastUpdateTime = lastUpdateTime;
	}

	public double getEnergyPerSecond() {
		return energyPerSecond;
	}

	public void setEnergyPerSecond(double energyPerSecond) {
		this.energyPerSecond = energyPerSecond;
	}
	
	
	
}
