package rover.tasks;


import java.util.ArrayList;

import rover.PollResult;
import rover.ResourceInfo;
import rover.RoverInfo;
import rover.RoverService;
import rover.RoverServiceImpl;
import rover.ScanItem;
import rover.TeamInfo;




public class ScanTask extends Task {

	RoverServiceImpl impl;
	double scanDist;
	
	private static final double scanMultiplier = 2.0;
	
	public ScanTask(RoverInfo rover, RoverServiceImpl impl, double scanDist) {
		super(rover, impl, 5000);
		
		setEnergyPerSecond(2 * (scanDist / rover.getScanRange()));
		
		this.impl = impl;
		this.scanDist = scanDist;
	}

	@Override
	public void Cancel() {
		// just need to reduce power
		adjustEnergy();
	}

	@Override
	public	void Complete() {
		// send the client the result of the scan
		PollResult pr = new PollResult(PollResult.SCAN, PollResult.COMPLETE);
		
		ArrayList<ScanItem> items = new ArrayList<ScanItem>();
		
		for(ResourceInfo rsi : impl.getResources()) {
			
			if(rsi.getCount() > 0) {			
				if(impl.calcDistance(getRover().getX(), getRover().getY(), rsi.getX(), rsi.getY()) < scanDist * scanMultiplier) {
					ScanItem  si = new ScanItem(ScanItem.RESOURCE,impl.calcXOffset(getRover().getX(), rsi.getX()), impl.calcYOffset(getRover().getY(), rsi.getY())) ;
					items.add(si);
				}			
			}
			
		}
		
		for(RoverInfo ri : impl.getRovers().values()) {
			if(ri != getRover()) {
				if(impl.calcDistance(getRover().getX(), getRover().getY(), ri.getX(), ri.getY()) < scanDist * scanMultiplier) {
					ScanItem si = new ScanItem(ScanItem.ROVER, impl.calcXOffset(getRover().getX(), ri.getX()), impl.calcYOffset(getRover().getY(), ri.getY()));
					items.add(si);
				}
			}
		}
		
		for(TeamInfo ti : impl.getTeams().values()) {
			if(impl.calcDistance(getRover().getX(), getRover().getY(), ti.getBaseX(), ti.getBaseY()) < scanDist * scanMultiplier) {
				ScanItem si = new ScanItem(ScanItem.BASE, impl.calcXOffset(getRover().getX(), ti.getBaseX()), impl.calcYOffset(getRover().getY(), ti.getBaseY()));
				items.add(si);
			}
		}
		
		pr.setScanItems(items.toArray(new ScanItem[] {}));
		
		getRover().setPollResult(pr);
		
		adjustEnergy();
	}

	@Override
	public void Update() {
		//we do not need to do anything here
		
		adjustEnergy();
		if(getRover().getEnergy() < 0) {
			PollResult pr = new PollResult(PollResult.SCAN, PollResult.FAILED);
			pr.setPercentComplete(percentComplete());
			getRover().setPollResult(pr);

			getRover().setTask(null);
		}
		
	}

	@Override
	public int taskNum() {
		return PollResult.SCAN;
	}

}
