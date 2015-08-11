package rover;

import javax.management.monitor.MonitorMBean;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JSplitPane;
import javax.swing.SwingUtilities;

import org.iids.aos.agent.Agent;
import org.iids.aos.log.Log;
import org.iids.aos.service.ServiceBroker;

import rover.MonitorInfo.Resource;
import rover.MonitorInfo.Rover;
import rover.MonitorInfo.Team;



public class MonitorAgent extends Agent  {
	
	private static final long serialVersionUID = 1L;

	private RoverService service;
	
	private ServiceBroker sb;
	private Thread thread;
	
	private RoverDisplay display;
	
	public MonitorAgent() {
		service = null;
		sb = null;
		thread = null;
		
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
				
		try {
			service = sb.bind(RoverService.class);
		
		} catch (Exception e) {
			e.printStackTrace();
		
			return;
		}
		
		display = new RoverDisplay(sb);
		
		display.setVisible(true);
		
		Monitor();
		
	}



	
	private void Monitor() {
		thread = new Thread(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				
				while(true) {
					
					try {
						service = sb.bind(RoverService.class);
					} catch (Exception e1) {
						e1.printStackTrace();
					}
					
					final MonitorInfo m = service.getWorldInfo();
					
					Runnable updateRunnable = new Runnable() {						
						@Override
						public void run() {

							display.UpdateDisplay(m);
							
						}
					};
					
					SwingUtilities.invokeLater(updateRunnable);
					
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
		});
		thread.start();
	}
	

}
