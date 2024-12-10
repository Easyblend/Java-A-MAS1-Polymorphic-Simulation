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
    public String getColor() {
        return BrightMagenta;
    }

    @Override
    public void move(Map map) {
        List<Direction> possibleDirections = getFilteredDirections(map, List.of(Direction.NORTHEAST, Direction.NORTHWEST, Direction.SOUTHEAST, Direction.SOUTHWEST));
        commonMoveLogic(map, possibleDirections);
    }

}