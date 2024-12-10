package polymorphicSimulation.agents;

import polymorphicSimulation.environment.Map;
import polymorphicSimulation.environment.Point;

public class Master extends Agent {

    public Master(String name, String group, Point location, int ep, String alliance) {
        super(name, group, location, ep, alliance);
    }

    @Override
    public void move(Map map) {
//         Masters don't move, so this method is empty
    }

    @Override
    public String getColor() {
        return null;
    }

    @Override
    protected void generateMessages() {
//        No messages generated for Masters
    }

    public void receiveMessage(String message) {
        if (!messages.contains(message)) {
            messages.add(message);
        }
    }

    // Maybe add methods like checking for a win condition later
}