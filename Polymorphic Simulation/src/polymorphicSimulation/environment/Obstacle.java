package polymorphicSimulation.environment;

import java.util.Objects;

public class Obstacle {
    public Point location;

    public Obstacle(Point location) {
        this.location = location;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Obstacle obstacle = (Obstacle) o;
        return Objects.equals(location, obstacle.location);
    }

    @Override
    public int hashCode() {
        return Objects.hash(location);
    }
}