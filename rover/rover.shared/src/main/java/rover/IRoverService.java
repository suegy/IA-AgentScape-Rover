package rover;

import org.iids.aos.service.Service;
import org.iids.aos.service.ServiceListener;


public interface IRoverService extends Service {
	
	/*
	interface RoverClient extends ServiceListener {
		void worldStarted() throws Exception;
		void worldStopped() throws Exception;
	}
	
	interface RoverMonitor extends ServiceListener {
		void worldReset() throws Exception;
		void worldUpdate(WorldInfo info) throws Exception;
	}
	*/
	
	//client methods
	
	int getWorldWidth();
	int getWorldHeight();
	
	int getWorldResources();
	boolean isWorldCompetitive();
	
	public int getScenario();

    public Integer[] getScenarioIDs();
	
	void move(String clientKey, double xOffset, double yOffest, double speed) throws Exception;
	void stop(String clientKey) throws Exception;

    void broadCastToTeam(String clientKey,String message);
    void broadCastToUnit(String clientKey, String remoteUnit, String message);

    String[] receiveMessages(String clientKey);

	void scan(String clientKey, double range) throws Exception;

	void collect(String clientKey) throws Exception;
	void deposit(String clientkey) throws Exception;
	
	int getCurrentLoad(String clientKey) throws Exception;
	
	String registerClient(String team) throws Exception;
	void setAttributes(String clientKey, int speed, int scanRange, int maxLoad) throws Exception;
	
	PollResult Poll(String clientKey) throws Exception;
	
	double getEnergy(String clientKey) throws Exception;
	
	// monitor methods
	
	void resetWorld(int scenario)  throws Exception;
	void startWorld(int speed) throws Exception;
	void stopWorld();
	
	MonitorInfo getWorldInfo();

	
}
