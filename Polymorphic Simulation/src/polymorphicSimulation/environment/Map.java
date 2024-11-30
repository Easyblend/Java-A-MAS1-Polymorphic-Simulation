package polymorphicSimulation.environment;

import polymorphicSimulation.agents.Agent;
import polymorphicSimulation.agents.Master;

import java.util.*;

import static polymorphicSimulation.style.ColorInConsole.*;

public class Map {

    private final int width;
    private final int height;
    private final Agent[][] grid; // Now a 2D array to store agents
    private final Set<Point> obstacles;
    private final java.util.Map<Point, String> deadAgents;
    private final java.util.Map<String, Point> safeZones; // Use the full name of Map since we created another Map class
    private final Random random = new Random();

    public Map(int width, int height) {
        this.width = width;
        this.height = height;
        this.grid = new Agent[height][width]; // Initialize grid
        this.obstacles = new HashSet<>();
        this.deadAgents = new HashMap<>();
        this.safeZones = new HashMap<>();
        generateSafeZones(3, 2); // Generate SafeZones upon map creation
        generateObstacles(); // Generate obstacles upon map creation
    }

    public boolean isTileFree(Point location) {
        if (!isTileWithinBounds(location)) { // Check bounds first
            return false;
        }
        return !isObstacleAt(location) && !isAgentAt(location);
    }

    public boolean isTileWithinBounds(Point location) {
        return location.x >= 0 && location.x < width && location.y >= 0 && location.y < height;
    }

    public boolean isObstacleAt(Point location) {
        return obstacles.contains(location);
    }

    public boolean isAgentAt(Point location) {
        return grid[location.y][location.x] != null;
    }

    public boolean isDeadAgentAt(Point location) {
        return deadAgents.containsKey(location);
    }

    public void addDeadAgent(Point location, String group) {
        this.deadAgents.put(location, group);
    }

    private String getDeadAgentGroup(Point location) {
        return this.deadAgents.get(location);
    }

    public boolean isInSafeZone(Point location, String group) {
        for (java.util.Map.Entry<String, Point> entry : safeZones.entrySet()) {
            if (entry.getKey().startsWith(group) && entry.getValue().equals(location)) { //Check if group name starts with the provided String
                return true;
            }
        }
        return false;
    }

    public boolean isInOtherSafeZone(Point location, String group) {
        for (java.util.Map.Entry<String, Point> entry : safeZones.entrySet()) {
            if (!entry.getKey().startsWith(group) && entry.getValue().equals(location)) { //Check if not the agent's group and the point is in another SafeZone
                return true;
            }
        }
        return false;
    }

    public boolean isSafeZone(Point location) {
        return safeZones.containsValue(location);
    }

    // TODO: Check more
    public Point getSafeZoneLocation(String group) {
        for (java.util.Map.Entry<String, Point> entry : safeZones.entrySet()) {
            if (entry.getKey().equals(group)) {
                return entry.getValue();
            }
        }
        return null; // Or throw an exception, or return a default safe zone. Handle appropriately
    }

    public void addObstacle(Point location) {
        System.out.println("addObstacle method initiated. Adding obstacle at location: (" + location.x + ", " + location.y + ")");
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
        int numObstacles = (int) (width * height * 0.00); // 4% of the map are obstacles (adjust as needed)

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
        if(grid[location.y][location.x] != null) {
            grid[location.y][location.x].getName();
        }
        if (isTileWithinBounds(location)) {
            return grid[location.y][location.x];
        }
        return null;
    }

        public Agent getAgentAt0(Point location) {
        System.out.print("Agent at (" + location.x + ", " + location.y + "): ");
        if(grid[location.y][location.x] != null) {
            System.out.println(grid[location.y][location.x].getName());
        } else {
            System.out.println("none");
        }
        if (isTileWithinBounds(location)) {
            return grid[location.y][location.x];
        }
        return null;
    }



    public void removeAgent(Point location) {
        if (isTileWithinBounds(location)) {
            grid[location.y][location.x] = null;
        }
    }

    public void printMap() {
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Point currentPoint = new Point(x, y);
                boolean printed = false;

                Agent agentAt = getAgentAt(currentPoint);
                if (agentAt != null) {
                    System.out.print(colorAgentSymbol(getAgentSymbol(agentAt), agentAt.getGroup()) + "  " + Reset);
                    printed = true;
                }

                // Check for obstacles and dead agents
                if (!printed) {
                    if (deadAgents.containsKey(currentPoint)) {
                        String deadAgentGroup = getDeadAgentGroup(currentPoint);
                        char symbol = 'X';
                        System.out.print(colorAgentSymbol(symbol, deadAgentGroup) + "  " + Reset);

                        printed = true;

                    } else if (obstacles.contains(currentPoint)) {
                        System.out.print(Red + "#  " + Reset);
                        printed = true;
                    }
                    else {
                        for (var entry : safeZones.entrySet()) {
                            if (entry.getValue().equals(currentPoint)) {
                                System.out.print("S" + entry.getKey().charAt(0) + " ");
                                printed = true;
                                break;
                            }
                        }
                    }

                    // Print ".  " for empty spaces
                    if (!printed) {
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

    private String colorAgentSymbol(char inputChar, String group) {
        return switch (group) {
            case "Human" -> BrightBlue + inputChar;
            case "Elf" -> BrightMagenta + inputChar;
            case "Orc" -> BrightGreen + inputChar;
            case "Goblin" -> BrightYellow + inputChar;
            default -> White + inputChar; // Default color white
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