package polymorphicSimulation.agents;

import polymorphicSimulation.environment.Map;
import polymorphicSimulation.environment.Point;
import polymorphicSimulation.utils.Direction;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static polymorphicSimulation.style.ColorInConsole.*;

public class Human extends Agent {
    private final Random random = new Random();

    public Human(String name, String group, Point location, int ep, String alliance) {
        super(name, group, location, ep, alliance);
    }

    @Override
    public void move(Map map) {
        if (getEp() <= 0) {
            System.out.println(BrightBlue+name + " is Dead"+Reset);
            return;
        }

        System.out.println(BrightBlue+name + " starting move at (" + location.x + ", " + location.y + ") with EP: "
                + getEp() + ". Messages: " + getMessages().size()+Reset);

        List<Direction> possibleDirections = getFilteredDirections(map); // Get filtered directions

        if (possibleDirections.isEmpty()) {
            System.out.println(name + " cannot move (no possible directions)");
            return; //Don't move if no possible directions
        }

        Direction direction = possibleDirections.get(random.nextInt(possibleDirections.size()));
        int maxDistance = random.nextInt(3) + 1; // 1-3 tiles

        System.out.println(name + " planning to move " + maxDistance + " steps "+ direction +Reset); // debugging

        moveInDirection(map, direction, maxDistance);

        lastDirection = direction;
        System.out.println(Red+name + " ending move at (" + location.x + ", " + location.y + ") with EP: "
                + getEp() +  ". Messages: " + getMessages().size()+Reset);
    }

    private List<Direction> getFilteredDirections(Map map) {
        List<Direction> filteredDirections = new ArrayList<>(List.of(Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST));

        if (lastHitObstacle) {
            filteredDirections.remove(lastDirection); // Remove last direction if agent last hit an obstacle.
            System.out.println("Avoided " + lastDirection + " - last obstacle hit direction");
            lastHitObstacle = false;
        }

        if (getEp() * 1.0 / getInitialEp() <= 0.2) {
            Direction toSafeZone = getSafeZoneDirection(map);
            System.out.println("EP <= 0.2 - going toward SafeZone: " + toSafeZone);
            if (toSafeZone != null) { // Only set direction to SafeZone if one exists.
                filteredDirections.clear(); // Clear other directions to prioritize the direction to the SafeZone
                filteredDirections.add(toSafeZone); // Set direction towards SafeZone.
            }
        }
        return filteredDirections;
    }
}