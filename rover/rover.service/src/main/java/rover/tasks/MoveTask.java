package rover.tasks;




import rover.RoverService;
import rover.PollResult;
import rover.RoverInfo;


public class MoveTask extends Task {

	
	private double initX;
	private double initY;
	
	private double xOffset;
	private double yOffset;

	private double speed;
	
	public MoveTask(RoverInfo rover, RoverService impl, double xOffset, double yOffset, double speed) {
		//how much time will it take to get there
		
		super( rover, impl, (int) (( Math.sqrt(Math.pow(xOffset, 2) + Math.pow(yOffset, 2)) / speed) * 1000) );

		setEnergyPerSecond(2 * (speed / rover.getSpeed()));
		
		this.initX = rover.getX();
		this.initY = rover.getY();
		
		this.xOffset = xOffset;
		this.yOffset = yOffset;
		
		this.speed = speed;
		
	}
	
	
	@Override
	public void Cancel() {
		//set to whatever position we currently got to.
		double pct = percentComplete();
		getRover().setX(initX + (xOffset * pct));
		getRover().setY(initY + (yOffset * pct));
		
		adjustEnergy();
	}

	@Override
	public void Complete() {
		
		//set to final position
		getRover().setX(initX + xOffset);
		getRover().setY(initY + yOffset);
		
		getRover().setPollResult(new PollResult(PollResult.MOVE, PollResult.COMPLETE));
		
		adjustEnergy();
	}

	@Override
	public void Update() {
		
		//update rover's position
		double pct = percentComplete();
		getRover().setX(initX + (xOffset * pct));
		getRover().setY(initY + (yOffset * pct));
		
		adjustEnergy();
		
		if(getRover().getEnergy() <= 0) {
			PollResult pr = new PollResult(PollResult.MOVE, PollResult.FAILED);
			pr.setPercentComplete(percentComplete());
			getRover().setPollResult(pr);
			getRover().setTask(null);
		}
	}


	@Override
	public int taskNum() {
		return PollResult.MOVE;
	}

}
