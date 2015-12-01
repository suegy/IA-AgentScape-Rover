package rover;

import java.util.*;

import org.iids.aos.kernel.xdr.prepare_wait_ac_in;
import org.iids.aos.service.AbstractDefaultService;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import rover.tasks.CollectTask;
import rover.tasks.DepositTask;
import rover.tasks.MoveTask;
import rover.tasks.ScanTask;
import rover.tasks.Task;

public class RoverService extends AbstractDefaultService implements
        IRoverService {

    private Map<String, TeamInfo> teams;
    private Map<Integer, Scenario> availableScenarios;
    private Map<String, RoverInfo> rovers;
    private ArrayList<ResourceInfo> resources;
    private Logger logger;
    private RoverStats stats;
    //private ArrayList<RoverMonitor> monitors;

    private Scenario selectedScenario;
    private ScenarioFactory scenarioFactory;
    private Map<String,String> messages;
    private int totalResources;

    private boolean started;
    private int currentScenarioID;

    private Thread worldThread;

    private int worldSpeed;

    public RoverService() {

        selectedScenario = Scenario.Empty();
        scenarioFactory = new ScenarioFactory();
        availableScenarios = scenarioFactory.deSerializeScenarios("scenarios");
        totalResources = 0;
        started = false;
        teams = new HashMap<String, TeamInfo>();
        rovers = new HashMap<String, RoverInfo>();
        logger = LoggerFactory.getLogger(IRoverService.class);
        stats = new RoverStats();
        worldSpeed = 1;



        resetWorld(0);
    }

    @Override
    public void resetWorld(int scenario) {
        logger.info("resetWorld: scenario " + scenario);

        Random rand = new Random();
        totalResources = 0;
        started = false;
        currentScenarioID = scenario;
        resources = new ArrayList<ResourceInfo>();

        if (availableScenarios.containsKey(scenario))
            selectedScenario = availableScenarios.get(scenario);
        else
            selectedScenario = Scenario.Empty();

        synchronized(this) {


            //inform agents the world has started
            if (rovers.size() > 0){

                RoverInfo [] old = rovers.values().toArray(new RoverInfo[0]);
                rovers = new HashMap<String, RoverInfo>();
                teams = new HashMap<String, TeamInfo>();
                logger.debug("rovers: " + rovers.size() + " previous number" + old.length);

                for(RoverInfo ri : old) {
                    logger.debug("Team " + ri.getTeam() + " rover " + ri.getClientKey() + "resetting");

                    PollResult pr = new PollResult(PollResult.ROVER_UNKNOWN, PollResult.COMPLETE);
                    ri.setPollResult(pr);
                    try {
                        Thread.sleep(50,0);
                    } catch (InterruptedException e) {
                        e.printStackTrace();

                    }
                }
              
            }

        }

        for(int i = 0; i < selectedScenario.getResourceCount(); i++) {
            resources.add(new ResourceInfo(rand.nextDouble() * selectedScenario.getWidth(), rand.nextDouble() * selectedScenario.getHeight(), selectedScenario.getResourceDistribution()));
            totalResources += selectedScenario.getResourceDistribution();
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
                        e.printStackTrace();

                    }
                }
            }
        });

        synchronized(this) {

            started = true;
            worldThread.start();
            stats.worldStarted();
            //inform agents the world has started
            for(RoverInfo ri : rovers.values()) {

                PollResult pr = new PollResult(PollResult.WORLD_STARTED, PollResult.COMPLETE);
                ri.setPollResult(pr);

            }

        }
    }

    @Override
    public void stopWorld() {
        synchronized (worldThread) {
            started = false;

            logger.info("Stopping World!");

            //inform agents the world has started
            for(RoverInfo ri : rovers.values()) {
                logger.debug("Team " + ri.getTeam() + " rover " + ri.getClientKey() + "stopping" );

                PollResult pr = new PollResult(PollResult.WORLD_STOPPED, PollResult.COMPLETE);
                ri.setPollResult(pr);

            }


            for(TeamInfo t : teams.values()) {
                logger.info("Team " + t.getTeamName() + " collected " + t.getCollectedCount() );
                stats.teamFinishedWorld(t);
            }
            stats.worldStopped();
            worldThread = null;
        }
    }



    @Override
    public String registerClient(String team)
            throws Exception {

        logger.info("CON: " + this.getCurrentSession().getConnectionId());


        if(started) {
            throw new Exception("Can't register clients when world is running");
        }


        synchronized(rovers) {

            Random rand = new Random();

            //get team
            TeamInfo t;
            if(teams.containsKey(team)) {
                t = teams.get(team);
            } else {
                //create new team info with random base location

                double x = rand.nextDouble() * selectedScenario.getWidth();
                double y = rand.nextDouble() * selectedScenario.getHeight();
                t = new TeamInfo(team, x, y);
                teams.put(team, t);
                logger.info("Created team: " + team + " (" + x + ", " + y + ")");
            }

            RoverInfo ri = new RoverInfo(this,t);
            ri.setEnergy(selectedScenario.getEnergy());


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
    public void broadCastToTeam(String client, String message) {
        synchronized(this) {
            RoverInfo ri = rovers.get(client);

            //FIXME:clunky code because team has no link back to team mates
            for (RoverInfo rover : rovers.values())
                if ( (ri.getTeam() == rover.getTeam()) && (ri.getClientKey()) != rover.getClientKey())
                    rover.receiveMessage(message);


        }

    }

    @Override
    public String[] receiveMessages(String client){
        synchronized(this) {
            RoverInfo ri = rovers.get(client);

            return ri.retrieveMessages();
        }
    }

    @Override
    public void broadCastToUnit(String client, String remoteUnit, String message) {
        synchronized(this) {
            RoverInfo ri = rovers.get(client);

            //FIXME:clunky code because team has no link back to team mates
            for (RoverInfo rover : rovers.values())
                // a rover is only allowed to send messages to its team mates
                    if ( (ri.getTeam()==rover.getTeam()) && (remoteUnit.equals(rover.getClientKey())) )
                        rover.receiveMessage(message);



        }
    }

    @Override
    public PollResult Poll(String clientKey) throws Exception {

        synchronized(this) {
            RoverInfo ri = rovers.get(clientKey);
            if(ri == null) {
                //if rover does not exist,
                //then send a WORLD_STOPPED result
                if (started)
                    return new PollResult(PollResult.ROVER_UNKNOWN, PollResult.WORLD_STARTED);
                else
                    return new PollResult(PollResult.ROVER_UNKNOWN, PollResult.COMPLETE);
            } else {
                PollResult p = ri.getPollResult();
                ri.setPollResult(null);
                return p;
            }

        }

    }

    @Override
    public double getEnergy(String clientKey) throws Exception {

        synchronized(rovers) {
            RoverInfo ri = rovers.get(clientKey);
            return ri.getEnergy();
        }
    }

    /**
     * calculates the distance between two points in world coordinates
     * @param aX x coordinate of the first point
     * @param aY y coordinate of the first point
     * @param bX x coordinate of the second point
     * @param bY y coordinate of the second point
     * @return
     */
    public double calcDistance(double aX, double aY, double bX, double bY) {

        double xdist = calcOffset(aX, bX, true);
        double ydist = calcOffset(aY, bY, false);

        double dist = Math.sqrt(xdist * xdist + ydist* ydist);

        return dist;
    }


    public double calcOffset(double orig, double pos, boolean horizontal) {

        double diff = pos-orig;
        double dimension =  horizontal ? selectedScenario.getWidth() : selectedScenario.getHeight();


        double half = dimension / 2.0;

        if(diff > 0) {
            if(diff > half) {
                diff = -(dimension-diff);
            }
        } else {
            if(diff < -half) {
                diff = dimension+diff;
            }
        }

        return diff;
    }



    public Map<String, TeamInfo> getTeams() {
        return teams;
    }

    public void setTeams(Map<String, TeamInfo> teams) {
        this.teams = teams;
    }

    public Map<String, RoverInfo> getRovers() {
        return rovers;
    }

    public void setRovers(Map<String, RoverInfo> rovers) {
        this.rovers = rovers;
    }

    public ArrayList<ResourceInfo> getResources() {
        return resources;
    }

    public void setResources(ArrayList<ResourceInfo> resources) {
        this.resources = resources;
    }

    public int getWidth() {
        return selectedScenario.getWidth();
    }

    public int getHeight() {
        return selectedScenario.getHeight();
    }

	/*
	public void setWidth(int width) {
		this.width = width;
	}
	public void setHeight(int height) {
		this.height = height;
	}*/

    @Override
    public MonitorInfo getWorldInfo() {

        MonitorInfo m = new MonitorInfo(selectedScenario.getWidth(), selectedScenario.getHeight());

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
        return selectedScenario.getHeight();
    }

    @Override
    public int getWorldResources() {
        return totalResources;
    }

    @Override
    public int getWorldWidth() {
        return selectedScenario.getWidth();
    }

    @Override
    public boolean isWorldCompetitive() {
        return selectedScenario.isCompetitive();
    }


    @Override
    public int getScenario() {
        return currentScenarioID;
    }

    /**
     * Returns the Scenarios IDS from all available scenarios in rover.services
     * @return
     */
    @Override
    public Integer[] getScenarioIDs() {

        return availableScenarios.keySet().toArray(new Integer[0]);
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
