package polymorphicSimulation.environment; //Package declaration

import java.util.Objects; //Import for using the Objects utility class

public class Obstacle { //Class definition, accessible from other packages.
    public Point location; //Public field to store the location of the obstacle.

    public Obstacle(Point location) { //Constructor: Takes a Point object as input and sets the location of the obstacle accordingly.
        this.location = location;
    }

    @Override
    public boolean equals(Object o) { //Overridden equals method. Used for comparing obstacles.
        if (this == o) return true; //If the compared object is the same instance, they are equal.
        if (o == null || getClass() != o.getClass()) return false; //If the compared object is null or not an Obstacle, they are not equal.
        Obstacle obstacle = (Obstacle) o; //Cast the object to an Obstacle
        return Objects.equals(location, obstacle.location); //Compare the locations of the two obstacles using Objects.equals for null safety. If they have the same location, return true. Otherwise, return false.
    }

    @Override
    public int hashCode() { //Overridden hashCode method. Used in hash-based collections like HashSet and HashMap.
        return Objects.hash(location); //Generate a hash code based on the object's location using Objects.hash for null safety.
    }
}