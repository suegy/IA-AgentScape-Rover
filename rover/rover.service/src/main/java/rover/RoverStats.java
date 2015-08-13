package rover;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.core.util.StatusPrinter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Stats collect game info and store it in a log for later analysis
 * Created by suegy on 11/08/15.
 */
public class RoverStats {

    private Logger logger;

    public RoverStats(){
        logger = LoggerFactory.getLogger("STATS");
        LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
        // print logback's internal status
        StatusPrinter.print(lc);

    }

    /**
     * Called after stopping a World in the monitor
     * @param team TeamInfo containing the Rover info of a team
     */
    public void teamFinishedWorld(TeamInfo team){
        logger.info("Team {} collected {} resources using {} rovers",team.getTeamName(),team.getCollectedCount(),team.getRoverCount());
    }
    public void worldStopped(){
        logger.info("world stopped");
    }

    public void worldStarted(){
        logger.info("world started");
    }
}
