package rover;

import javax.swing.SwingUtilities;

import org.iids.aos.agent.Agent;
import org.iids.aos.service.ServiceBroker;

import org.iids.aos.service.ServiceException;
import rover.MonitorInfo.Resource;
import rover.MonitorInfo.Rover;
import rover.MonitorInfo.Team;



public class MonitorAgent extends Agent  {
	
	private static final long serialVersionUID = 1L;

	private IRoverService service;
	
	private ServiceBroker sb;
	private RoverDisplay display;
	
	public MonitorAgent() {
		service = null;
		sb = null;
		display = null;
		
		//preload cause of agentscape bug;
		MonitorInfo mi = new MonitorInfo(0, 0);
		Rover r = mi.new Rover(0, 0, "");
		Team t = mi.new Team(0, 0, "", 0);
		Resource rs = mi.new Resource(0, 0, 0);
		
	}

	@Override
	public void run() {
		
		sb = getServiceBroker();
				


        //Runnable createDisplay = new Runnable() {
          //  @Override
          //  public void run() {

                display = new RoverDisplay(sb);

                display.setVisible(true);
            //}
        //};

        //SwingUtilities.invokeLater(createDisplay);

		runMonitor();

		
	}



	
	private void runMonitor() {

                boolean connected = true;
                Runnable updateDisplay = new Runnable() {
                    @Override
                    public void run() {
                        display.UpdateDisplay(service.getWorldInfo());

                    }
                };

				while(display.isVisible() &&  connected) {

                    try {
                        service = sb.bind(IRoverService.class);
                        connected = true;

                    } catch (ServiceException s) {
                        connected = false;
                        return;
                    }
                    catch (Exception e) {
                        e.printStackTrace();

                        return;
                    }

                    SwingUtilities.invokeLater(updateDisplay);
					
					/*
					
					boolean found = false;
					
					for(Rover r : m.getRovers()) {
						Log.console("[monitor] Rover " + r.getKey() + " x:" + r.getX() + " y:" + r.getY() + " energy:" + r.getEnergy() + " load:" + r.getCurrentLoad());
						if(r.getEnergy() > 0) {
							found = true;
						}
					}
					
					if(!found) {
						//all rovers are out of energy
						Log.console("[monitor] All rovers dead.");
						
						for(Team t  : m.getTeams()) {
							Log.console("[monitor] Team " + t.getTeamName() + " collected: " + t.getTotalCollected());
						}
						
						break;
					}
					*/
					
					
					try {
						Thread.sleep(500);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				


	}
	

}
