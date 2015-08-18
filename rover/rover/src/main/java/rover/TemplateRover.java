package rover;

import java.util.Random;



public class TemplateRover extends Rover {

	public TemplateRover() {
        super();

		//use your username for team name
		setTeam("template");
		
		try {
			//set attributes for this rover
			//speed, scan range, max load
			//has to add up to <= 9
			setAttributes(4, 4, 1);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}

	@Override
	void begin() {
		//called when the world is started
        getLog().info("BEGIN!");
		
		try {
			//move somewhere initially
			move(5,5,2);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}

	@Override
	void end() {
		// called when the world is stopped
		// the agent is killed after this
        getLog().info("END!");
	}

	@Override
	void poll(PollResult pr) {
		// This is called when one of the actions has completed

        getLog().info("Remaining Power: " + getEnergy());
		
		if(pr.getResultStatus() == PollResult.FAILED) {
            getLog().info("Ran out of power...");
			return;
		}
		
		switch(pr.getResultType()) {
		case PollResult.MOVE:
			//move finished
            getLog().info("Move complete.");
			
			//now scan
			try {
                getLog().info("Scanning...");
				scan(4);
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			break;
		case PollResult.SCAN:
            getLog().info("Scan complete");
						
			for(ScanItem item : pr.getScanItems()) {
				if(item.getItemType() == ScanItem.RESOURCE) {
                    getLog().info("Resource found at: " + item.getxOffset() + ", " + item.getyOffset());
				} else if(item.getItemType() == ScanItem.BASE) {
                    getLog().info("Base found at: " + item.getxOffset() + ", " + item.getyOffset());
				} else {
                    getLog().info("Rover found at: " + item.getxOffset() + ", " + item.getyOffset());
				}
			}
			
			// now move again
			Random rand = new Random();
			try {
                getLog().info("Moving...");
				move(5 * rand.nextDouble(), 5 * rand.nextDouble(), 4);
			} catch (Exception e) {
				e.printStackTrace();
			}
			break;
		case PollResult.COLLECT:
            getLog().info("Collect complete.");
			
			break;
		case PollResult.DEPOSIT:
            getLog().info("Deposit complete.");
			break;
		}
		
	}

}
