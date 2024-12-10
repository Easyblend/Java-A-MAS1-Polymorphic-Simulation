package polymorphicSimulation.agents;

import polymorphicSimulation.environment.Map;
import polymorphicSimulation.environment.Point;
import polymorphicSimulation.utils.Direction;

import java.util.List;

import static polymorphicSimulation.style.ColorInConsole.*;

public class Goblin extends Agent {

    public Goblin(String name, String group, Point location, int ep, String alliance) {
        super(name, group, location, ep,alliance);
    }

    @Override
    public String getColor() {
        return BrightYellow;
    }

    @Override
    public void move(Map map) {
        List<Direction> possibleDirections = getFilteredDirections(map, List.of(Direction.values()));
        commonMoveLogic(map, possibleDirections);
    }
}