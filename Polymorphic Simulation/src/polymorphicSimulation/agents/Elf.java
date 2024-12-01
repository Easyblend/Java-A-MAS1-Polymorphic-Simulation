package polymorphicSimulation.agents;

import polymorphicSimulation.environment.Map;
import polymorphicSimulation.environment.Point;
import polymorphicSimulation.utils.Direction;

import java.util.List;
import java.util.Random;

import static polymorphicSimulation.style.ColorInConsole.*;

public class Elf extends Agent {
    private final Random random = new Random();

    public Elf(String name, String group, Point location, int ep, String alliance) {
        super(name, group, location, ep, alliance);
    }

    @Override
    public void move(Map map) {
        if (getEp() <= 0) {
            System.out.println(BrightMagenta+name + " is Dead"+Reset);
            return;
        }
        System.out.println(BrightMagenta+name + " starting move at (" + location.x + ", " + location.y + ") with EP: " + getEp()+Reset);

        List<Direction> possibleDirections = List.of(Direction.NORTHEAST, Direction.NORTHWEST, Direction.SOUTHEAST, Direction.SOUTHWEST);
        Direction direction = possibleDirections.get(random.nextInt(possibleDirections.size()));

        int maxDistance = random.nextInt(3) + 1; // 1-3 tiles

        System.out.println(name + " planning to move " + maxDistance + " steps "+ direction +Reset); // debugging

        moveInDirection(map, direction, maxDistance);

        lastDirection = direction;
        System.out.println(Red+name + " ending move at (" + location.x + ", " + location.y + ") with EP: " + getEp()+Reset);

    }

}