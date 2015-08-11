package rover;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;

/**
 * Created by suegy on 10/08/15.
 */
public class WorldPanel extends JPanel {

    private MonitorInfo monitorInfo = null;

    private int scale = 10;

    public int getScale() {
        return scale;
    }

    public void setScale(int scale) {
        this.scale = scale;

        if(monitorInfo != null) {
            this.setSize(monitorInfo.getWidth() * scale, monitorInfo.getHeight() * scale);
            this.setPreferredSize(new Dimension(monitorInfo.getWidth() * scale, monitorInfo.getHeight() * scale));
        }
    }

    public MonitorInfo getMonitorInfo() {
        return monitorInfo;
    }

    public void setMonitorInfo(MonitorInfo monitorInfo) {
        this.monitorInfo = monitorInfo;

        this.setSize(monitorInfo.getWidth() * scale, monitorInfo.getHeight() * scale);
        this.setPreferredSize(new Dimension(monitorInfo.getWidth() * scale, monitorInfo.getHeight() * scale));

        this.repaint();

    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);

        Graphics2D g2 = (Graphics2D) g;

        if(monitorInfo != null ) {

            g2.setPaint(Color.WHITE);
            g2.fill(new Rectangle2D.Double(0,0, monitorInfo.getWidth() * scale, monitorInfo.getHeight() * scale));

            g2.setPaint(Color.RED);
            for(MonitorInfo.Team t : monitorInfo.getTeams()) {
                g2.fill( new Rectangle2D.Double(t.getX() * scale, t.getY() * scale, 10 + scale, 10 + scale));
            }

            g2.setPaint(Color.BLUE);
            for(MonitorInfo.Resource r : monitorInfo.getResources()) {
                g2.fill( new RoundRectangle2D.Double(r.getX() * scale, r.getY() * scale, 10 + scale, 10 + scale, 2 ,2 ));
            }

            g2.setPaint(Color.GREEN);
            for(MonitorInfo.Rover r : monitorInfo.getRovers()) {
                g2.fill( new Ellipse2D.Double(r.getX() * scale, r.getY() * scale, 10 + scale,10 +scale));
            }

        }

    }
}
