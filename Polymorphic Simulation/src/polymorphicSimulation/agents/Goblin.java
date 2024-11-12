package polymorphicSimulation.agents;

import polymorphicSimulation.environment.Map;
import polymorphicSimulation.environment.Point;
import polymorphicSimulation.utils.Direction;

import java.util.Random;

import static polymorphicSimulation.style.ColorInConsole.*;

public class Goblin extends Agent {
    private final Random random = new Random();

    public Goblin(String name, String group, Point location, int ep) {
        super(name, group, location, ep);
    }

    @Override
    public void move(Map map) {
        System.out.println(BrightYellow+name + " starting move at (" + location.x + ", " + location.y + ")"+Reset);
        if (getEp() <= 0) return;

        // Random movement (any direction, 1-3 tiles)
        Direction direction = Direction.values()[random.nextInt(Direction.values().length)]; // Any direction
        int maxDistance = random.nextInt(3) + 1;


        Point newLocation = moveInDirection(map, direction, maxDistance);


        lastDirection = direction;
        System.out.println(Red+name + " ending move at (" + location.x + ", " + location.y + ")"+Reset);
    }
}