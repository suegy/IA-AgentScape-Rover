package rover.tasks;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;

import java.io.Serializable;

/**
 * Created by suegy on 11/08/15.
 */
@Root
public class Scenario {
    @Attribute
    private int width = 0;
    @Attribute
    private int height = 0;

    @Attribute
    private int rscount = 0;
    @Attribute
    private int rsDist = 0;

    @Attribute
    private int initialEnergy = 0;
    @Attribute
    private boolean isCompetitive = false;

    public Scenario(int width, int height, int resources, int resourceDist, int energy, boolean competitive) {
        this.width = width;
        this.height = height;
        this.rscount  =resources;
        this.rsDist = resourceDist;
        this.initialEnergy = energy;
        this.isCompetitive = competitive;
    }

    public int getWidth(){
        return width;
    }
    public int getHeight(){
        return height;
    }
    public int getResourceCount(){
        return rscount;
    }
    public int getResourceDistribution(){
        return rsDist;
    }
    public int getEnergy(){
        return initialEnergy;
    }
    public boolean isCompetitive(){
        return isCompetitive;
    }
}
