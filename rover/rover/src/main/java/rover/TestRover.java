package rover;

import java.util.Random;

import org.iids.aos.log.Log;


public class TestRover extends Rover {

	public TestRover() {
		Log.console("TestRover start");
		
		//use your username for team name
		setTeam("tristan");
		
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
		Log.console("BEGIN!");
		
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
		Log.console("END!");
	}

	@Override
	void poll(PollResult pr) {
		// This is called when one of the actions has completed

		Log.console("Remaining Power: " + getEnergy());
		
		if(pr.getResultStatus() == PollResult.FAILED) {
			Log.console("Ran out of power...");
			return;
		}
		
		switch(pr.getResultType()) {
		case PollResult.MOVE:
			//move finished
			Log.console("Move complete.");			
			
			//now scan
			try {
				Log.console("Scanning...");
				scan(4);
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			break;
		case PollResult.SCAN:
			Log.console("Scan complete");
						
			for(ScanItem item : pr.getScanItems()) {
				if(item.getItemType() == ScanItem.RESOURCE) {
					Log.console("Resource found at: " + item.getxOffset() + ", " + item.getyOffset());					
				} else if(item.getItemType() == ScanItem.BASE) {
					Log.console("Base found at: " + item.getxOffset() + ", " + item.getyOffset());
				} else {
					Log.console("Rover found at: " + item.getxOffset() + ", " + item.getyOffset());
				}
			}
			
			// now move again
			Random rand = new Random();
			try {
				Log.console("Moving...");
				move(5 * rand.nextDouble(), 5 * rand.nextDouble(), 4);
			} catch (Exception e) {
				e.printStackTrace();
			}
			break;
		case PollResult.COLLECT:
			Log.console("Collect complete.");
			
			break;
		case PollResult.DEPOSIT:
			Log.console("Deposit complete.");
			break;
		}
		
	}

}
