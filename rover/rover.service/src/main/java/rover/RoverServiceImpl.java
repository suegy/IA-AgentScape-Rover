package rover;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Random;

import org.iids.aos.service.AbstractDefaultService;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import rover.ResourceInfo;
import rover.RoverInfo;
import rover.RoverService;
import rover.TeamInfo;
import rover.tasks.CollectTask;
import rover.tasks.DepositTask;
import rover.tasks.MoveTask;
import rover.tasks.ScanTask;
import rover.tasks.Task;

public class RoverServiceImpl extends AbstractDefaultService implements
		RoverService {

	private HashMap<String, TeamInfo> teams;
	private HashMap<String, RoverInfo> rovers;
	private ArrayList<ResourceInfo> resources;
	private Logger logger;
	//private ArrayList<RoverMonitor> monitors;
	
	private int width;
	private int height;
	
	private int totalResources;
	private boolean isCompetitive;
	
	private int initialEnergy;
	
	private boolean started;
	private int currentScenario;
	
	private Thread worldThread;
	
	private int worldSpeed;
	
	public RoverServiceImpl() {
		width = 0;
		height = 0;
		
		initialEnergy = 200;
		totalResources = 0;
		isCompetitive = false;
		
		started = false;
        logger = LoggerFactory.getLogger(RoverService.class);

                worldSpeed = 1;
		
		//monitors = new ArrayList<RoverMonitor>();
		
		resetWorld(0);
	}
	
	@Override
	public void resetWorld(int scenario) {
		logger.info("resetWorld: scenario " + scenario);
		currentScenario = scenario;
		
		synchronized(this) {
			started = false;			
		}
		
		teams = new HashMap<String, TeamInfo>();
		rovers = new HashMap<String, RoverInfo>();
		resources = new ArrayList<ResourceInfo>();
		
		width = 50;
		height = 50;		
		
		totalResources = 0;
		
		int rscount = 0;
		int rsDist = 0;
		
		switch(scenario) {
		case 0:
			
			width = 20;
			height = 20;
			
			rscount = 1;
			rsDist = 10;
			
			initialEnergy = 5000;
			isCompetitive = false;
			
			break;
			
		case 1:	
			
			width = 40;
			height = 40;
			
			rscount = 5;
			rsDist = 5;
			
			initialEnergy = 5000;
			isCompetitive = false;
			
			break;
		
		case 2:
			
			width = 80;
			height = 80;
			
			rscount = 10;
			rsDist = 5;
			
			initialEnergy = 1000;
			isCompetitive = false;
			
			break;
			
		case 3:
			
			width = 100;
			height = 100;
			
			rscount = 10;
			rsDist = 1;
			
			initialEnergy = 1000;
			isCompetitive = false;
			
			break;
			
		case 4:
			
			width = 200;
			height = 200;
			
			rscount = 15;
			rsDist = 1;
			
			initialEnergy = 500;
			isCompetitive = false;
			
			break;
			
		case 5:
			
			width = 500;
			height = 500;
			
			rscount = 30;
			rsDist = 2;
			
			initialEnergy = 1000;
			isCompetitive = true;
			
			break;
			
		}
		
	
		Random rand = new Random();
		
		for(int i = 0; i < rscount; i++) {
			resources.add(new ResourceInfo(rand.nextDouble() * width, rand.nextDouble() * height, rsDist));
			totalResources += rsDist;
		}
	}
	
	@Override
	public void startWorld(int speed) {

		logger.info("startWorld");
		this.worldSpeed = speed;
		
		worldThread = new Thread(new Runnable() {
			
			@Override
			public void run() {
				
				
				while(started) {
					
					synchronized(this) {
						
						int carriedResources = 0;
						boolean roversRemaining = false;
						
						for(RoverInfo ri : rovers.values()) {
							
							Task t = ri.getTask();
							
							if(t != null) {
								
								Date d = new Date();
								if(d.after(t.getEndTime())) {
									//finished!
									
									ri.setTask(null);
									t.Complete();									
								} else {
									t.Update();
									
								}								
							}
							
							if(ri.getEnergy() > 0 ) {
								roversRemaining = true;
								if(ri.getCurrentLoad() > 0 || ri.getTask() instanceof CollectTask) {
									carriedResources++;
								}
								
							}							
						}
						
						boolean resourcesRemaining = false;
						
						for(ResourceInfo ri : resources) {
							if(ri.getCount() > 0) {
								resourcesRemaining = true;
								break;
							}
						}
						
						if((carriedResources == 0 && !resourcesRemaining) || !roversRemaining) {
							//no more resources in the world, and no rovers
							//with energy are carrying resources
							//we will stop the game
						
							stopWorld();
							break;
						}
						
					}
									
					try {
						Thread.sleep(150,0);
					} catch (InterruptedException e) {
				
					}
				}
			}
		});
		
		synchronized(this) {
		
			started = true;
			worldThread.start();
				
			//inform agents the world has started
			for(RoverInfo ri : rovers.values()) {
				
				PollResult pr = new PollResult(PollResult.WORLD_STARTED, PollResult.COMPLETE);
				ri.setPollResult(pr);
			
			}
			
		}
	}

	@Override
	public void stopWorld() {
		synchronized (this) {
			started = false;
			
			logger.info("Stopping World!");
			
			//inform agents the world has started
			for(RoverInfo ri : rovers.values()) {
				
				PollResult pr = new PollResult(PollResult.WORLD_STOPPED, PollResult.COMPLETE);
				ri.setPollResult(pr);
			
			}		
			

			for(TeamInfo t : teams.values()) {
                logger.info("Team " + t.getTeamName() + " collected " + t.getCollectedCount());
			}
			
			worldThread = null;
			resetWorld(currentScenario);
		}
	}
	
	
	
	@Override
	public String registerClient(String team)
			throws Exception {

        logger.info("CON: " + this.getCurrentSession().getConnectionId());
		
		
		if(started) {
			throw new Exception("Can't register clients when world is running");		
		}
		
		
		synchronized(this) {
		
			Random rand = new Random();
			
			//get team
			TeamInfo t;
			if(teams.containsKey(team)) {
				t = teams.get(team);
			} else {
				//create new team info with random base location
							
				double x = rand.nextDouble() * width;
				double y = rand.nextDouble() * height;
				t = new TeamInfo(team, x, y);
				teams.put(team, t);
                logger.info("Created team: " + team + " (" + x + ", " + y + ")");
			}
		
			RoverInfo ri = new RoverInfo(this,t);
			ri.setEnergy(initialEnergy);
			
			
			String key = team + "-" + t.getRoverCount();
			t.setRoverCount(t.getRoverCount() + 1);
			
			ri.setClientKey(key);
			
			rovers.put(key, ri);

            logger.info("New client (" + team + "): " + key);
			
			return key;			
		}
		
		
	}
	
	@Override
	public void collect(String client) throws Exception {
		// TODO Auto-generated method stub
		synchronized(this) {
			RoverInfo ri = rovers.get(client);		
		
			if(ri.getEnergy() < 0) {
				throw new Exception("Client is out of energy");
			}
			
			if(ri.getTask() != null) {
				throw new Exception("Client already has a task: " + ri.getTask());
			}
			
			if(ri.getCurrentLoad() >= ri.getMaxLoad()) {
				throw new Exception("Client already has max load");
			}
			
			ResourceInfo rs = null;
			
			for(ResourceInfo rsi : getResources()) {
				if(calcDistance(ri.getX(), ri.getY(), rsi.getX(), rsi.getY()) < .1) {
					if(rsi.getCount() > 0) {
						rs = rsi;
						break;
					}
				}
			}
			
			if(rs == null) {
				throw new Exception("No resources to collect");
			}
			
			CollectTask ct = new CollectTask(ri,this,rs);
			ri.setTask(ct);
		}
		
	}

	@Override
	public void deposit(String client) throws Exception {
		
		synchronized(this) {
			RoverInfo ri = rovers.get(client);		
		
			if(ri.getEnergy() < 0) {
				throw new Exception("Client is out of energy");
			}
			
			if(ri.getTask() != null) {
				throw new Exception("Client already has a task: " + ri.getTask());
			}
			
			if(ri.getCurrentLoad() <= 0) {
				throw new Exception("Client has no resources to deposit");
			}
			
			TeamInfo ti = null;
			for(TeamInfo t : getTeams().values()) {
				if(calcDistance(ri.getX(), ri.getY(), t.getBaseX(), t.getBaseY()) < .1) {
					ti = t;
				}
			}
			
			DepositTask dt = null;
			
			if(ti != null) {
				dt = new DepositTask(ri, this, null, ti);
			} else {
				
				ResourceInfo rs = null;
				for(ResourceInfo rsi : getResources()) {
					if(calcDistance(ri.getX(), ri.getY(), rsi.getX(), rsi.getY()) < .1) {					
							rs = rsi;
							break;
					}
				}
				if(rs == null) {
					rs = new ResourceInfo(ri.getX(), ri.getY(), 0);
					getResources().add(rs);
				}
				dt = new DepositTask(ri, this, rs, null);
				
			}
			
			ri.setTask(dt);
		}
		
	}

	@Override
	public void move(String client, double xOffset, double yOffset,
			double speed) throws Exception {
				
		//logger.info("Move: " + client + " " + xOffset + " " + yOffset + " " + speed);
		
		synchronized(this) {
			RoverInfo ri = rovers.get(client);		
		
			if(ri.getEnergy() < 0) {
				throw new Exception("Client is out of energy");
			}
			
			if(ri.getTask() != null) {
				throw new Exception("Client already has a task: " + ri.getTask());
			}
			
			if(speed > ri.getSpeed()) {
				throw new Exception("Speed is greater than client's max speed");
			}
			
			if(speed < 1) {
				throw new Exception("Speed must be >= 1");
			}
			
			MoveTask mt = new MoveTask(ri, this, xOffset, yOffset, speed);
			ri.setTask(mt);
		}
		
	}






	@Override
	public void scan(String client, double range) throws Exception {
		synchronized(this) {
			RoverInfo ri = rovers.get(client);		
		
			if(ri.getEnergy() < 0) {
				throw new Exception("Client is out of energy");
			}
			
			if(ri.getTask() != null) {
				throw new Exception("Client already has a task: " + ri.getTask());
			}
			
			if(range > ri.getScanRange()) {
				throw new Exception("Scan range is larger than client's max range.");
			}
			
			ScanTask st = new ScanTask(ri, this, range);
			ri.setTask(st);
		}
		
	}

	@Override
	public void setAttributes(String client, int speed, int scanRange,
			int maxLoad) throws Exception {
		
		if(started) {			
			throw new Exception("attributes can't be changed after the world has been started");
		}
		
		if(speed < 1) {
			throw new Exception("speed must be >= 1");
		}
		
		if(scanRange < 0) {
			throw new Exception("scanRange must be > 0");
		}
		
		if(maxLoad < 0) {
			throw new Exception("maxLoad must be > 0");
		}
		
		if(speed + scanRange + maxLoad > 9) {
			throw new Exception("speed + scanRange + maxLoad must not be > 9");
		}
		
		synchronized(this) {
            logger.info("setAttributes for: " + client);
			RoverInfo ri = rovers.get(client);
			ri.setSpeed(speed);
			ri.setScanRange(scanRange);
			ri.setMaxLoad(maxLoad);
		}
		
	}

	@Override
	public void stop(String client) throws Exception {
		
		synchronized(this) {
			
			RoverInfo ri = rovers.get(client);
		
			Task t = ri.getTask();
			
			if(t == null) {
				throw new Exception("client does not have a task");
			}
		
			double pctComplete = t.percentComplete();
			t.Cancel();
			ri.setTask(null);
			
			int statusType = 0;
			
			if(t instanceof MoveTask) {
				statusType = PollResult.MOVE;
			} else if(t instanceof ScanTask) {
				statusType = PollResult.SCAN;
			} else if(t instanceof CollectTask) {
				statusType = PollResult.COLLECT;
			} else if(t instanceof DepositTask) {
				statusType = PollResult.DEPOSIT;		
			}
			
			PollResult pr = new PollResult(statusType, PollResult.CANCELLED);
			pr.setPercentComplete(pctComplete);
			
			ri.setPollResult(pr);
			
		}
		
	}

	@Override
	public PollResult Poll(String clientKey) throws Exception {
		
		synchronized(this) {
			RoverInfo ri = rovers.get(clientKey);
			if(ri == null) {
				//if rover does not exist,
				//then send a WORLD_STOPPED result
				PollResult pr = new PollResult(PollResult.WORLD_STOPPED, PollResult.COMPLETE);
				return pr;
			} else {
				PollResult p = ri.getPollResult();
				ri.setPollResult(null);
				return p;
			}
			
		}
		
	}

	@Override
	public double getEnergy(String clientKey) throws Exception {

		synchronized(this) {
			RoverInfo ri = rovers.get(clientKey);			
			return ri.getEnergy();
		}
	}
	
	public double calcDistance(double x1, double y1, double x2, double y2) {
	
		double xdist = calcXOffset(x1, x2);
		double ydist = calcYOffset(y1, y2);
			
		double dist = Math.sqrt(xdist * xdist + ydist* ydist);
		
		return dist;
	}
	
	public double calcXOffset(double xOrig, double x) {
		
		double diff = x-xOrig;
	
		double half = width / 2.0;
		
		if(diff > 0) {
			if(diff > half) {
				diff = -(width-diff);
			}
		} else {
			if(diff < -half) {
				diff = width+diff;
			} 
		}
		
		return diff;
		
	}
	
	public double calcYOffset(double yOrig, double y) {
		double diff = y-yOrig;
		
		double half = height / 2.0;
		
		if(diff > 0) {
			if(diff > half) {
				diff = -(height-diff);
			}
		} else {
			if(diff < -half) {
				diff = height+diff;
			} 
		}
		
		return diff;
	}
	
	public HashMap<String, TeamInfo> getTeams() {
		return teams;
	}

	public void setTeams(HashMap<String, TeamInfo> teams) {
		this.teams = teams;
	}

	public HashMap<String, RoverInfo> getRovers() {
		return rovers;
	}

	public void setRovers(HashMap<String, RoverInfo> rovers) {
		this.rovers = rovers;
	}

	public ArrayList<ResourceInfo> getResources() {
		return resources;
	}

	public void setResources(ArrayList<ResourceInfo> resources) {
		this.resources = resources;
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

	@Override
	public MonitorInfo getWorldInfo() {
		
		MonitorInfo m = new MonitorInfo(width, height);
		
		synchronized (this) {
			
			
			for(TeamInfo ti : getTeams().values()) {
				MonitorInfo.Team t = m.new Team(ti.getBaseX(), ti.getBaseY(), ti.getTeamName(), ti.getCollectedCount());
				m.getTeams().add(t);
			}
			
			for(ResourceInfo ri : getResources()) {
				if(ri.getCount() > 0) {
					MonitorInfo.Resource r = m.new Resource(ri.getX(), ri.getY(), ri.getCount());
					m.getResources().add(r);
				}
			}
			
			for(RoverInfo ri : getRovers().values()) {
				MonitorInfo.Rover r = m.new Rover(ri.getX(), ri.getY(), ri.getClientKey());
				
				r.setCurrentLoad(ri.getCurrentLoad());
				r.setEnergy(ri.getEnergy());
				r.setMaxLoad(ri.getMaxLoad());
				r.setScanRange(ri.getScanRange());
				r.setSpeed(ri.getSpeed());
				
				if(ri.getTask() != null) {
					r.setTask(ri.getTask().taskNum());
					r.setTaskCompletion(ri.getTask().percentComplete());
				} else {
					r.setTask(-1);
				}
				
				m.getRovers().add(r);
			}
		
		}
		return m;
	}

	@Override
	public int getWorldHeight() {
		return height;
	}

	@Override
	public int getWorldResources() {
		return totalResources;
	}

	@Override
	public int getWorldWidth() {
		return width;
	}

	@Override
	public boolean isWorldCompetitive() {
		return isCompetitive;
	}


	@Override
	public int getScenario() {
		return currentScenario;
	}

	public int getWorldSpeed() {
		return worldSpeed;
	}

	public void setWorldSpeed(int worldSpeed) {
		this.worldSpeed = worldSpeed;
	}

	@Override
	public int getCurrentLoad(String clientKey) throws Exception {
		synchronized(this) {
			RoverInfo ri = rovers.get(clientKey);		
			return ri.getCurrentLoad();
		}
	}




	

	
	
}
