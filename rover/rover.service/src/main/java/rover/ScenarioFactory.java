package rover;


import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * The purpose of the class is create xml based scenario files for use with the rover world
 * Created by suegy on 17/08/15.
 */
public class ScenarioFactory {

    protected Set<Scenario> scenarios;
    private Serializer serializer;

    public ScenarioFactory(){
        scenarios  =new HashSet<Scenario>();
        serializer = new Persister();
    }

    public boolean addScenario(int id, int width, int height, int resources, int resourceDist, int energy, boolean competitive){
        return scenarios.add(new Scenario(id,width,height,resources,resourceDist,energy,competitive));
    }
    public boolean serializeScenarios(String directory){

        boolean error  =false;
        File dir = new File(directory);
        if (!dir.exists() || !dir.isDirectory())
            dir.mkdir();

        for (Scenario scene : scenarios){




            try {
                File result = new File(String.format(dir.getCanonicalPath()+File.separator+"scenario-%s.xml",scene.getId()));
                serializer.write(scene, result);
            } catch (Exception e) {
                System.err.println("Could not serialize: " + scene.getId());
                e.printStackTrace();
            }
        }

        return error;
    }
    public Map<Integer,Scenario> deSerializeScenarios(String scenarioLocation)  {
        HashMap<Integer,Scenario> allScenarios = new HashMap<Integer,Scenario>();

        FileFilter filter = new FileFilter() {
            @Override
            public boolean accept(File file) {
                return file.isFile() && file.getName().endsWith(".xml") && file.canRead();
            }
        };
        File dir = new File(scenarioLocation);

        if (!dir.exists() || dir.listFiles(filter).length < 1)
            return  allScenarios;

        for (File file : dir.listFiles(filter)){
            try {
                Scenario scene = serializer.read(Scenario.class, file);
                allScenarios.put(scene.getId(),scene);

            } catch (Exception e) {
                System.err.println("Could not load scenarios from "+scenarioLocation);
                e.printStackTrace();
            }



        }

        return allScenarios;
    }

    public static void main(String [] args){

        /*
        creates the initial set of scenarios used for the Intelligent Agents course.
         */
        ScenarioFactory creator  = new ScenarioFactory();
        creator.addScenario(0,20,20,1,10,5000,false);
        creator.addScenario(1,40,40,5,5,5000,false);
        creator.addScenario(2,80,80,10,5,1000,false);
        creator.addScenario(3,100,100,10,1,1000,false);
        creator.addScenario(4,200,200,15,1,500,false);
        creator.addScenario(5,500,500,30,2,1000,true);
        creator.serializeScenarios("scenarios");
    }


}