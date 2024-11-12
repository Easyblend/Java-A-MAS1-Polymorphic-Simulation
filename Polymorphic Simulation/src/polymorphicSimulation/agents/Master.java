package polymorphicSimulation.agents;

import polymorphicSimulation.environment.Map;
import polymorphicSimulation.environment.Point;

public class Master extends Agent {

    public Master(String name, String group, Point location, int ep) {
        super(name, group, location, ep);
    }

    @Override
    public void move(Map map) {
        // Masters don't move, so this method is empty
    }

    public void receiveMessage(String message) {
        if (!messages.contains(message)) {
            messages.add(message);
        }
    }

    // You can add other Master-specific methods here, like checking for a win condition
}