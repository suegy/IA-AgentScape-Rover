package rover.tasks;


import rover.*;
import rover.RoverService;

public class DepositTask extends Task {

	private ResourceInfo ri;
	private TeamInfo ti;
	
	public DepositTask(RoverInfo rover, RoverService impl, ResourceInfo ri, TeamInfo ti) {
		super(rover, impl, 5000);
		
		setEnergyPerSecond(1);
		
		this.ri = ri;
		this.ti = ti;
	}

	@Override
	public void Cancel() {		
		adjustEnergy();		
	}

	@Override
	public void Complete() {
		adjustEnergy();
		
		//decrease rover count
		getRover().setCurrentLoad(getRover().getCurrentLoad() - 1);
		
		//increase resource count
		if(ri != null) {
			ri.setCount(ri.getCount() + 1);
		} else {
			ti.setCollectedCount(ti.getCollectedCount() + 1);
		}
		
		getRover().setPollResult(new PollResult(PollResult.DEPOSIT, PollResult.COMPLETE));
		
	}

	@Override
	public void Update() {
		
		adjustEnergy();
		if(getRover().getEnergy() < 0) {			
			
			
			PollResult pr = new PollResult(PollResult.DEPOSIT, PollResult.FAILED);
			pr.setPercentComplete(percentComplete());
			getRover().setPollResult(pr);
			
			getRover().setTask(null);
		}
		
	}

	@Override
	public int taskNum() {
		return PollResult.DEPOSIT;
	}

}
