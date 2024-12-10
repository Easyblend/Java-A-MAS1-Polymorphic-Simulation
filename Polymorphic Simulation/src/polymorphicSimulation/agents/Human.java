package polymorphicSimulation.agents;

import polymorphicSimulation.environment.Map;
import polymorphicSimulation.environment.Point;
import polymorphicSimulation.utils.Direction;

import java.util.List;
import java.util.Random;

import static polymorphicSimulation.style.ColorInConsole.*;

public class Human extends Agent {
    private final Random random = new Random();

    public Human(String name, String group, Point location, int ep, String alliance) {
        super(name, group, location, ep, alliance);
    }

    @Override
    public String getColor() {
        return BrightBlue;
    }

    @Override
    public void move(Map map) {
        List<Direction> possibleDirections = getFilteredDirections(map, List.of(Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST));
        commonMoveLogic(map, possibleDirections);
    }
}