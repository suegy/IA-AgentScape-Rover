package rover.tasks;


import rover.RoverService;
import rover.PollResult;
import rover.ResourceInfo;
import rover.RoverInfo;

public class CollectTask extends Task {

	private ResourceInfo ri;
	
	public CollectTask(RoverInfo rover, RoverService impl, ResourceInfo ri) {
		super(rover, impl, 5000);
		
		this.ri = ri;
		
		setEnergyPerSecond(1);
		
		//adjust count down
		ri.setCount(ri.getCount()- 1);
	}

	@Override
	public void Cancel() {
		// reduce rover power and add resource
		// back to world
				
		ri.setCount(ri.getCount()+ 1);
		
		adjustEnergy();
		
	}

	@Override
	public void Complete() {
		// add resource to rover and reduce power
		
		adjustEnergy();
		
		getRover().setPollResult(new PollResult(PollResult.COLLECT, PollResult.COMPLETE));
		getRover().setCurrentLoad(getRover().getCurrentLoad() + 1);
		
	}

	@Override
	public void Update() {
		// don't need to do anything
		adjustEnergy();
		if(getRover().getEnergy() < 0) {
				PollResult pr = new PollResult(PollResult.COLLECT, PollResult.FAILED);
				pr.setPercentComplete(percentComplete());
				getRover().setPollResult(pr);
				
				//add resource back
				ri.setCount(ri.getCount()+ 1);
				getRover().setTask(null);
		}
	}

	@Override
	public int taskNum() {
		return PollResult.COLLECT;
	}

}
