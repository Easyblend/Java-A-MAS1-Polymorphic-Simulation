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
        generateSafeZones(3, 2); // Generate SafeZones upon map creation
        generateObstacles(); // Generate obstacles upon map creation
    }

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
        for (java.util.Map.Entry<String, Point> entry : safeZones.entrySet()) {
            if (entry.getKey().startsWith(group) && entry.getValue().equals(location)) { //Check if group name starts with the provided String
                return true;
            }
        }
        return false;
    }

    public Point getSafeZoneLocation(String group) {
        for (java.util.Map.Entry<String, Point> entry : safeZones.entrySet()) {
            if (entry.getKey().equals(group)) {
                return entry.getValue();
            }
        }
        return null; // Or throw an exception, or return a default safe zone. Handle appropriately
    }

    public void addObstacle(Point location) {
        obstacles.add(location);
    }

    private void generateSafeZones(int sizeX, int sizeY) {
        defineSafeZone("Human", 0, 0, 1, 1, sizeX, sizeY);
        defineSafeZone("Elf", width - 1, 0, -1, 1, sizeX, sizeY);
        defineSafeZone("Orc", 0, height - 1, 1, -1, sizeX, sizeY);
        defineSafeZone("Goblin", width - 1, height - 1, -1, -1,sizeX ,sizeY);

        System.out.println("safeZones" + safeZones);
    }

    private void defineSafeZone(String name, int startX, int startY, int deltaX, int deltaY, int sizeX, int sizeY) {
        int count = 0;
        for (int y = 0; y < sizeY; y++) {
            for (int x = 0; x < sizeX; x++) {
                safeZones.put(name + (count == 0 ? "" : count),
                        new Point(startX + (x * deltaX), startY + (y * deltaY)));
                count++;
            }
        }
    }

    public void generateObstacles() {
        System.out.println("Generating Obstacles"); // debugging
        int numObstacles = (int) (width * height * 0.04); // 4% of the map are obstacles (adjust as needed)

        System.out.print("Obstacles:");
        for (int i = 0; i < numObstacles; i++) {
            int x = random.nextInt(width);
            int y = random.nextInt(height);
            Point location = new Point(x, y);
            // Ensure obstacles don't overlap SafeZones or other obstacles.
            if (!safeZones.containsValue(location) && !obstacles.contains(location) && grid[y][x] == null) {
                System.out.print(" - (" + location.x + ", " + location.y + ")");
                addObstacle(location);
            } else { //Try again if an obstacle is on a forbidden place
                i--;
            }
        }
        System.out.println();
    }

    public void placeAgent(Agent agent) {
            grid[agent.location.y][agent.location.x] = agent;
            System.out.println(agent.name + " placed at (" + agent.location.x + ", " + agent.location.y + ")"); // into - game
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
        System.out.print("Agent at (" + location.x + ", " + location.y + "): ");
        if(grid[location.y][location.x] != null) {
            System.out.println(grid[location.y][location.x].getName());
        } else {
            System.out.println("none");
        }
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
                    if (grid[y][x].getEp() == 0) {
                        System.out.print(colorDeadAgentSymbol(getAgentSymbol(grid[y][x])) + "  " + Reset);

                    } else {
                        System.out.print(colorAgentSymbol(getAgentSymbol(grid[y][x])) + "  " + Reset);
                    }
                    printed = true;
                }

                // Iterate through the obstacles set
                for (Point obstacleLocation : obstacles) {
                        if (obstacleLocation.equals(currentPoint)) {
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
            case 'H' -> BrightBlue+inputChar;
            case 'e' -> BrightMagenta+inputChar;
            case 'E' -> BrightMagenta+inputChar;
            case 'o' -> BrightGreen+inputChar;
            case 'O' -> BrightGreen+inputChar;
            case 'g' -> BrightYellow+inputChar;
            case 'G' -> BrightYellow+inputChar;
            default -> String.valueOf(inputChar);
        };
    }

    private String colorDeadAgentSymbol(char inputChar) {
        return switch (inputChar) {
            case 'h' -> BrightBlue+'x';
            case 'e' -> BrightMagenta+'x';
            case 'o' -> BrightGreen+'x';
            case 'g' -> BrightYellow+'x';
            default -> String.valueOf('x');
        };
    }

    public Set<Point> getObstacles(){
        return obstacles;
    }

}