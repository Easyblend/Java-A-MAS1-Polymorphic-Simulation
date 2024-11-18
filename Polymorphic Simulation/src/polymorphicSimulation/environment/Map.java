package polymorphicSimulation.environment;

import polymorphicSimulation.agents.Agent;
import polymorphicSimulation.agents.Master;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import static polymorphicSimulation.style.ColorInConsole.*;

public class Map {

    private final int width;
    private final int height;
    private final Agent[][] grid; // Now a 2D array to store agents
    private final Set<Point> obstacles;
    private final java.util.Map<String, Point> safeZones; // Use the full name of Map since we created another Map class
    private final Random random = new Random();

    public Map(int width, int height) {
        this.width = width;
        this.height = height;
        this.grid = new Agent[height][width]; // Initialize grid
        this.obstacles = new HashSet<>();
        this.safeZones = new HashMap<>();
        generateSafeZones(); // Generate SafeZones upon map creation
        generateObstacles(); // Generate obstacles upon map creation
    }


//    public boolean isTileFree(Point location) {
//        boolean inBounds = isWithinBounds(location);
//        boolean notObstacle = !obstacles.contains(location);
//        boolean notAgent = isWithinBounds(location) && grid[location.y][location.x] == null; // Null check
//        System.out.println("Location (" + location.x + ", " + location.y + "): inBounds=" + inBounds +
//                ", notObstacle=" + notObstacle + ", notAgent=" + notAgent);
//        return inBounds && notObstacle && notAgent;
//    }

    public boolean isTileFree(Point location) {
        if (!isWithinBounds(location)) { // Check bounds first
            return false;
        }
        // Only return true if within bounds and the location is empty (i.e. not obstacle and not agent)
        return  !obstacles.contains(location) && grid[location.y][location.x] == null;
    }

    public boolean isWithinBounds(Point location) {
        return location.x >= 0 && location.x < width && location.y >= 0 && location.y < height;
    }

    public boolean isInSafeZone(Point location, String group) {
        if (getSafeZoneLocation(group) == null)
            return false;
        return location.equals(safeZones.get(group));

    }

    public Point getSafeZoneLocation(String group) {
        return safeZones.get(group);
    }

    public void addObstacle(Point location) {
        obstacles.add(location);
    }

    private void generateSafeZones() {
        // Define corners as SafeZones (you can customize this)
        safeZones.put("Human", new Point(0, 0));
        safeZones.put("Elf", new Point(width - 1, 0));
        safeZones.put("Orc", new Point(0, height - 1));
        safeZones.put("Goblin", new Point(width - 1, height - 1));
    }


    public void generateObstacles() {
        System.out.println("Generating Obstacles"); // debugging
        int numObstacles = (int) (width * height * 0.05); // 5% of the map are obstacles (adjust as needed)

        // debugging
        System.out.println("numObstacles=" + numObstacles);

        for (int i = 0; i < numObstacles; i++) {
            int x = random.nextInt(width);
            int y = random.nextInt(height);
            Point location = new Point(x, y);
            System.out.println("Obstacle Point " + location.x + ", " + location.y);
            // Ensure obstacles don't overlap SafeZones or other obstacles.
            if (!safeZones.containsValue(location) && !obstacles.contains(location) && grid[y][x] == null) {
//                obstacles.add(location);
                addObstacle(location);
            } else { //Try again if an obstacle is on a forbidden place
                i--;
            }
        }
    }

    public void placeAgent(Agent agent) {
            grid[agent.location.y][agent.location.x] = agent;
            System.out.println(agent.name + " placed at (" + agent.location.x + ", " + agent.location.y + ")");
    }

    private Point findValidRandomSpot() {
        Random random = new Random();
        Point newLocation;
        do {
            int x = random.nextInt(width);
            int y = random.nextInt(height);
            newLocation = new Point(x, y);
        } while (!isTileFree(newLocation)); // Keep looking until free and in bounds spot is found
        return newLocation;
    }

    public Agent getAgentAt(Point location) {
        System.out.println("getAgentAt(" + location + " (" + location.x + ", " + location.y + ") " + grid[location.y][location.x]);
        if (isWithinBounds(location)) {
            return grid[location.y][location.x];
        }
        return null;
    }

    public void removeAgent(Point location) {
        if (isWithinBounds(location)) {
            grid[location.y][location.x] = null;
        }
    }

    // Add a method to print the map to the console (for visualization)
// In the Map class
    public void printMap() {
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Point currentPoint = new Point(x, y);

                boolean printed = false; // Flag to track if something has been printed at this location


                if (grid[y][x] != null) {
                    System.out.print(colorAgentSymbol(getAgentSymbol(grid[y][x])) + "  " + Reset);
                    printed = true;
                }


                // Iterate through the obstacles set
                for (Point obstacleLocation : obstacles) {
//                    System.out.println("currentPoint " + currentPoint.x + ", " + currentPoint.y);
//                    System.out.println("obstacleLocation " + obstacleLocation.x + ", " + obstacleLocation.y);
//                    System.out.println("obstacleLocation is: " + obstacleLocation + ", currentPoint is: " + currentPoint);
//                    System.out.println("before if, obstacleLocation=" + obstacleLocation + ", currentPoint=" + currentPoint);
//                    if (obstacleLocation.equals(currentPoint)) { // Check with .equals
//                    if (obstacleLocation.x == currentPoint.x && obstacleLocation.y == currentPoint.y) { // Check with .equals
                        if (obstacleLocation.equals(currentPoint)) {  // Use .equals() here
                        System.out.print(Red + "#  " + Reset);
                        printed = true;
                        break; // Exit the inner loop after printing the obstacle
                    }
                }


                if (!printed) { //If nothing was printed at the currentPoint print the default character or check for safeZones.
                    boolean isSafeZone = false;
                    for (var entry : safeZones.entrySet()) {
                        if (entry.getValue().equals(currentPoint)) {
                            System.out.print("S" + entry.getKey().charAt(0) + " ");
                            isSafeZone = true;
                            break;
                        }
                    }
                    if (!isSafeZone) {
                        System.out.print(".  ");
                    }
                }
            }
            System.out.println();
        }
    }

    private char getAgentSymbol(Agent agent) {
        if (agent instanceof Master){
            return agent.group.charAt(0);
        }
        return Character.toLowerCase(agent.group.charAt(0));
    }

    private String colorAgentSymbol(char inputChar) {
        return switch (inputChar) {
            case 'h' -> BrightBlue+inputChar;
            case 'e' -> BrightMagenta+inputChar;
            case 'o' -> BrightGreen+inputChar;
            case 'g' -> BrightYellow+inputChar;
            default -> String.valueOf(inputChar);
        };
    }

    public Set<Point> getObstacles(){
        return obstacles;
    }

}