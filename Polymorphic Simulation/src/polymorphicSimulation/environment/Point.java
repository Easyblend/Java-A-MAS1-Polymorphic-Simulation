package polymorphicSimulation.environment;

import java.util.Objects;

public class Point {
    public int x;
    public int y;

    public Point(int x, int y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public boolean equals(Object o) {
        // 1. Check for reference equality (fastest check)
        if (this == o) return true; // If the same object (in memory), they are equal

        // 2. Check for null and class type
        if (o == null || getClass() != o.getClass()) return false;  // If null or different class, not equal

        // 3. Cast to Point (safe because of the class check above)
        Point point = (Point) o;

        // 4. Compare fields for equality
        return x == point.x && y == point.y; // Points are equal if x and y values are the same
    }

    @Override
    public int hashCode() {
        // Generate a hash code based on the x and y values.
        // Objects.hash() is a good way to do this, handling nulls correctly.
        return Objects.hash(x, y);
    }
}


