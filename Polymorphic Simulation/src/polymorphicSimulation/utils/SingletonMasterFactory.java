package polymorphicSimulation.utils;

import org.w3c.dom.ls.LSOutput;
import polymorphicSimulation.agents.Master;
import polymorphicSimulation.environment.Point;

import java.util.HashMap;
import java.util.Map;

public class SingletonMasterFactory {
    private static final Map<String, Master> masterInstances = new HashMap<>();

    private SingletonMasterFactory() {
        // Private constructor to prevent instantiation
    }

    public static Master getMasterInstance(String group, Point location, int initialEp, String alliance) {
        return masterInstances.computeIfAbsent(group, k -> new Master("Master" + k, k, location, initialEp, alliance));
    }

}

//public class SingletonALaClasse {
//    private static SingletonALaClasse lUnique;
//    private SingletonALaClasse() {};
//    public static SingletonALaClasse getInstance() {
//        if (lUnique==null) {
//            lUnique = new SingletonALaClasse();
//        }
//        return lUnique;
//    }
//}

