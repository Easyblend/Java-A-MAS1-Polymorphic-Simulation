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
        if (getEp() <= 0) {
            System.out.println(BrightYellow+name + " is Dead"+Reset);
            return;
        }
        System.out.println(BrightYellow+name + " starting move at (" + location.x + ", " + location.y + ") with EP: " + getEp()+Reset); // debugging

        // Random movement (any direction, 1-3 tiles)
        Direction direction = Direction.values()[random.nextInt(Direction.values().length)]; // Any direction
        int maxDistance = random.nextInt(3) + 1;

        System.out.println(name + " planning to move " + maxDistance + " steps "+ direction +Reset); // debugging

        moveInDirection(map, direction, maxDistance);

        lastDirection = direction;
        System.out.println(Red+name + " ending move at (" + location.x + ", " + location.y + ") with EP: " + getEp()+Reset); // debugging
    }
}