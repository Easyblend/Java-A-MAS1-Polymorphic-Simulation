package polymorphicSimulation.agents;

import polymorphicSimulation.environment.Map;
import polymorphicSimulation.environment.Point;
import polymorphicSimulation.utils.Direction;

import java.util.List;
import java.util.Random;

import static polymorphicSimulation.style.ColorInConsole.*;

public class Orc extends Agent {
    private final Random random = new Random();

    public Orc(String name, String group, Point location, int ep) {
        super(name, group, location, ep);
    }

    @Override
    public void move(Map map) {
        System.out.println(BrightGreen+name + " starting move at (" + location.x + ", " + location.y + ")"+Reset);
        if (getEp() <= 0) return;

        List<Direction> possibleDirections = List.of(Direction.values());
        Direction direction = possibleDirections.get(random.nextInt(possibleDirections.size()));

        Point newLocation = moveInDirection(map, direction, 1); // Max distance is always 1 for King

        lastDirection = direction;
        System.out.println(Red+name + " ending move at (" + location.x + ", " + location.y + ")"+Reset);
    }
}