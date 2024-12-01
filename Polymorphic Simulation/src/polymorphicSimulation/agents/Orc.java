package polymorphicSimulation.agents;

import polymorphicSimulation.environment.Map;
import polymorphicSimulation.environment.Point;
import polymorphicSimulation.utils.Direction;

import java.util.List;
import java.util.Random;

import static polymorphicSimulation.style.ColorInConsole.*;

public class Orc extends Agent {
    private final Random random = new Random();

    public Orc(String name, String group, Point location, int ep, String alliance) {
        super(name, group, location, ep, alliance);
    }

    @Override
    public void move(Map map) {
        if (getEp() <= 0) {
            System.out.println(BrightGreen+name + " is Dead"+Reset);
            return;
        }
        System.out.println(BrightGreen+name + " starting move at (" + location.x + ", " + location.y + ") with EP: " + getEp()+Reset);

        List<Direction> possibleDirections = List.of(Direction.values());
        Direction direction = possibleDirections.get(random.nextInt(possibleDirections.size()));

        System.out.println(name + " planning to move 1 step "+ direction +Reset); // debugging

        moveInDirection(map, direction, 1); // Max distance is always 1 for King

        lastDirection = direction;
        System.out.println(Red+name + " ending move at (" + location.x + ", " + location.y + ") with EP: " + getEp()+Reset);
    }
}