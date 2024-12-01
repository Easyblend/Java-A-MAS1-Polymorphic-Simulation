package polymorphicSimulation.agents;

import polymorphicSimulation.environment.Map;
import polymorphicSimulation.environment.Point;
import polymorphicSimulation.utils.Direction;
import polymorphicSimulation.utils.SingletonMasterFactory;

import java.util.*;

import static polymorphicSimulation.style.ColorInConsole.*;

public abstract class Agent {

    public String name;
    public String group;
    public Point location;
    private int ep;
    private final int initialEp;
    private final String alliance;
    public List<String> messages;
    public Direction lastDirection;
    private static final int MAX_MESSAGES = 5; // Max messages per agent
    private static int totalMessages = 0; // Keep track of total messages
    protected final Random random = new Random();

    public Agent(String name, String group, Point location, int ep, String alliance) {
        this.name = name;
        this.group = group;
        this.location = location;
        this.ep = ep;
        this.initialEp = ep;
        this.messages = new ArrayList<>();
        this.lastDirection = null;
        this.alliance = alliance;
        this.generateMessages(); //Generate messages upon Agent creation
    }

    public abstract void move(Map map); //Polymorphic method for agent movement

    protected boolean withinBounds(Point newLocation, Map map) {
        return map.isTileWithinBounds(newLocation);
    }

    protected boolean TileObstacle(Point newLocation, Map map) {
        return map.isObstacleAt(newLocation);
    }

    protected void updateLocation(Point newLocation, Map map) {
        map.removeAgent(this.location); // Remove from old spot
        location = newLocation;
        map.placeAgent(this); // Add to new Spot
    }

//    public void exchangeMessages(Agent other, Map map) {
//        System.out.println("exchangeMessages method initiated"); // Debug print
//        System.out.println("this.group.equals(other.group: " + this.group + " - " + other.group);
//        if (this.group.equals(other.group)) { // Allies
//            other.messages.stream()
//                    .filter(msg -> !this.messages.contains(msg))
//                    .forEach(this.messages::add);
//            System.out.println(this.name + " (" + this.group + ") met ally " + other.name + " and shared messages.");
//
//        } else if (!map.isInSafeZone(this.location, this.group) && !map.isInSafeZone(other.location, other.group)) {
//            // Enemies (and not in SafeZone) - Battle
//            System.out.println(this.name + " (" + this.group + ") battled " + other.name + "."); // Print before battle
//            battle(other);
//            System.out.println("Result of the battle: " + this.name + " has " + this.messages.size() + ", " + other.name + " has " + other.messages.size() + " messages");
//        }
//    }

    public void exchangeMessages(Agent other, Map map) {
        System.out.println("exchangeMessages method initiated"); // Debug print
        if (this.group.equals(other.group)) { // Same group - Union of messages
            unionMessages(other);

        } else if (this.getAlliance().equals(other.getAlliance())) { // Different group, same alliance - alliance exchange
            exchangeAllianceMessages(other);

        } else if (!map.isInSafeZone(this.location, this.group) && !map.isInSafeZone(other.location, other.group)) {
            // Different group, different alliance, not in safe zone - Battle
            System.out.println(Cyan+this.name + " (" + this.group + ") of " + this.getMessages().size() + " messages, battled " + other.name + " (" + other.group + ") of " + other.getMessages().size() + " messages."+Reset);
            battle(other);
            System.out.println(Cyan+"Result of the battle: " + this.name + " has " + this.messages.size() + ", " + other.name + " has " + other.messages.size() + " messages"+Reset);
        }
    }

    private void unionMessages(Agent other) {
        Set<String> combinedMessages = new HashSet<>(this.messages); // Use a Set to automatically handle duplicates
        combinedMessages.addAll(other.messages);      // Add all of the other agent's messages (more efficient this way)

        System.out.print(Cyan+this.name + " (" + this.group + ") met his fellow " + other.name + " and unioned messages."+Reset);
        System.out.print(Cyan+"Before union: " + this.name + ": " + this.messages.size() + " messages, " + other.name + ": " + other.messages.size() + " messages"+Reset);

        this.messages = new ArrayList<>(combinedMessages);  // Update my messages
        other.messages = new ArrayList<>(combinedMessages); // Update the other agent's messages

        System.out.println(Cyan+"After union: Both have " + this.messages.size() + " messages"+Reset);

    }

    private void exchangeAllianceMessages(Agent other) { //New method
        System.out.println(Cyan+this.name + " (" + this.group + ") met " + other.name + " from allied group ("+ other.getGroup() + ") and exchanged messages."+Reset);

        int numMessagesToExchange = random.nextInt(3) + 1;  // Exchange 1-3 messages

        System.out.print(Cyan+"Before exchange: " + this.name + " has " + this.messages.size() + ", " + other.name + " has " + other.messages.size() + ". "+Reset);

        List<String> myMessagesCopy = new ArrayList<>(this.messages); // Make copies of messages to avoid ConcurrentModificationException
        List<String> otherMessagesCopy = new ArrayList<>(other.messages);

        // Exchange unique messages between the 2 agents.
        transferUniqueMessages(myMessagesCopy, other, numMessagesToExchange);
        transferUniqueMessages(otherMessagesCopy, this, numMessagesToExchange);

        System.out.println(Cyan+"After exchange: " + this.name + " has " + this.messages.size() + ", " + other.name + "has " + other.messages.size()+Reset);

    }

    private void transferUniqueMessages(List<String> sourceMessages, Agent recipient, int numMessages) {
        Random random = new Random();

        for (int i = 0; i < numMessages && !sourceMessages.isEmpty(); i++) {
            String message = sourceMessages.remove(random.nextInt(sourceMessages.size()));

            if (!recipient.messages.contains(message)) {
                recipient.messages.add(message);
            } else {
                i--; //If the recipient already had that message, then try transferring another message
            }
        }
    }

    private void battle(Agent other) {
        String[] options = {"stone", "leaf", "scissors"};
        Random random = new Random();
        String myChoice = options[random.nextInt(3)];
        String otherChoice = options[random.nextInt(3)];

        System.out.print(Cyan+this.getName() + " chose " + myChoice + ", " + other.getName() + " chose: " + otherChoice + " ||| "+Reset);

        int result = compareChoices(myChoice, otherChoice);

        if (result == 1) { // won
//            transferMessages(other, this, random.nextInt(other.messages.size() + 1) ); // Transfer up to all messages
            transferMessages(other, this);
        } else if (result == -1) { // lost
//            transferMessages(this, other, random.nextInt(this.messages.size() + 1) ); // Transfer up to all messages
            transferMessages(this, other);
        } else {
            System.out.print(Cyan+"Tie, no message transfer. "+Reset);
        }
    }

    private int compareChoices(String choice1, String choice2) {
        if (choice1.equals(choice2)) {
            return 0; // Tie
        } else if ((choice1.equals("stone") && choice2.equals("scissors")) ||
                (choice1.equals("leaf") && choice2.equals("stone")) ||
                (choice1.equals("scissors") && choice2.equals("leaf"))) {
            return 1; // Win
        } else {
            return -1; // Lose
        }
    }


    // TODO: take unique messages first, then if there are still left to take but no unique, just delete the remaining from the loser
    private void transferMessages(Agent loser, Agent winner) { // Removed numMessages parameter
        System.out.println(Cyan+"Winner: " + winner.getName() + ", loser: " + loser.getName()+Reset);

        // Iterate through a copy of the loser's messages to avoid ConcurrentModificationException
        for (String message : new ArrayList<>(loser.messages)) { //Using a copy of loser.messages
            if (!winner.messages.contains(message)) {
                winner.messages.add(message);
            }
        }
        loser.messages.clear(); // Clear all messages from the loser after transfer

    }

    public Direction getSafeZoneDirection(Map map) {
        Point safeZone = map.getSafeZoneLocation(this.group);
        if (safeZone == null) {
            return null; // Or throw an exception, or return a default direction.
        }

        int dx = safeZone.x - this.location.x;
        int dy = safeZone.y - this.location.y;

        if (dx == 0 && dy == 0) {
            return null; // Already in the SafeZone
        }

        if (Math.abs(dx) > Math.abs(dy)) { // Move horizontally first
            if (dx > 0) {
                return Direction.EAST;
            } else {
                return Direction.WEST;
            }
        } else if (Math.abs(dy) > Math.abs(dx)) { //Prioritize vertical movement if dy is greater than dx
            if (dy > 0) {
                return Direction.NORTH;
            } else {
                return Direction.SOUTH;
            }
        } else { // dx == dy (Diagonal movement)
            if (dx > 0 && dy > 0) return Direction.NORTHEAST;
            if (dx < 0 && dy > 0) return Direction.NORTHWEST;
            if (dx > 0 && dy < 0) return Direction.SOUTHEAST;
            if (dx < 0 && dy < 0) return Direction.SOUTHWEST;
            return null; // This shouldn't happen if dx == dy != 0, but it is important to always return something
        }
    }

    protected void generateMessages() {
        int numMessages = random.nextInt(MAX_MESSAGES) + 1; // Generates 1 to MAX_MESSAGES

        for (int i = 0; i < numMessages; i++) {
            String newMessage = "M" + totalMessages++;
            if (!messages.contains(newMessage)) {
                messages.add(newMessage);
            }
        }
    }

    public void transferMessagesToMaster(Map map) {
        if (this instanceof Master) return; // Masters don't transfer messages to themselves

        System.out.print("transferMessagesToMaster method initiated");

        Master master = SingletonMasterFactory.getMasterInstance(group, map.getSafeZoneLocation(group), initialEp, this.alliance);

        System.out.println(" ||| map.isInSafeZone: " + map.isInSafeZone(location, group)); // debugging

        if (map.isInSafeZone(location, group)) {
            for (String message : getMessages()) {
//                System.out.println("Master " + master.name + "receive Message method initiated"); // debugging
                master.receiveMessage(message);
//                this.messages.remove(message); // Option: Removing the message from the agent after transferring it to master, this will make it hard to collect all messages
//                System.out.println(this.name + " transferred a message to Master " + master.name + "."); // debugging - Print transfer
//                break; // Option: Transfer a single message per encounter
            }
            System.out.println("Done transferring"); // debugging
        }
    }

    // TODO: now fix the obstacles interaction
    // TODO: check if transferMessagesToMaster is working correctly in all the safe zones
//    protected Point moveInDirection(Map map, Direction direction, int maxDistance) {
//        Point currentLocation = new Point(location.x, location.y);
//        Point newLocation = null;
//        for (int i = 0; i < maxDistance; i++) {
//            newLocation = calculateNextLocation(currentLocation, direction); // Calculate the next potential location - 1 step
//
//            System.out.print("Potential next step: (" + newLocation.x + ", " + newLocation.y + ") ||| "); // debugging
//
//            if (withinBounds(newLocation, map)) {
//                Agent otherAgent = map.getAgentAt(newLocation); // Check for other agents at the target location BEFORE moving
//                if (otherAgent != null && otherAgent != this) { // TODO: fix when the otherAgent is a Master or safe-zone
//                    exchangeMessages(otherAgent, map);
//                    break; // Stop further movement for this step after interaction.
//                } else if (map.getObstacles().contains(newLocation)) { // check if there is obstacle in the next potential location
//                    System.out.println("Obstacle encountered at (" + newLocation.x + ", " + newLocation.y + ")");
//                    break;
//                }
//
//                updateEp(map, newLocation); // Update the agent's EP.
//
//                currentLocation = newLocation; // Update currentLocation after checking for agents and exchangeMessages.
//                updateLocation(currentLocation, map); // Then update location on map
//                transferMessagesToMaster(map); //Transfer messages after each step. Since it has a check if the agent is in a SafeZone it will work even if it is called here
//            } else {
//                System.out.println("Cannot Move Outside");
//                break; // Stop if blocked (outside map)
//            }
//
//        }
//
//        return Objects.requireNonNullElseGet(newLocation, () -> new Point(location.x, location.y));
//    }

    protected void moveInDirection(Map map, Direction direction, int maxDistance) {
        System.out.println(this.name + " moveInDirection method initiated");
        Point currentLocation = new Point(location.x, location.y);
        Point newLocation = null;
        for (int i = 0; i < maxDistance; i++) {
            int stepsLeft = maxDistance - i;
            newLocation = calculateNextLocation(currentLocation, direction);

            if (withinBounds(newLocation, map)) {
                if (!TileObstacle(newLocation, map)) {
                    if (!map.isInOtherSafeZone(newLocation, group)) {
                        Agent otherAgent = map.getAgentAt(newLocation);  // Check if another agent is present at the target location
                        if (otherAgent != null && otherAgent != this) {
                            if (otherAgent instanceof Master) { //Check if other agent is Master before interaction. If so, only transfer messages
                                transferMessagesToMaster(map); // could be used if map with no safe zones
                            } else {
                                exchangeMessages(otherAgent, map);
                            }
                            break; // Stop further movement after interaction
                        }

                        updateEp(map, newLocation);
                        currentLocation = newLocation; //The agent moves to the new location

                        updateLocation(currentLocation, map);
                        if (getEp() <= 0 && !(this instanceof Master)) { //Check if agent is dead after movement
                            System.out.println(getName() + " becomeObstacle()");
                            becomeObstacle(map);
                            return; //Agent is dead, stop moving.
                        }
                        transferMessagesToMaster(map);

                    } else {
                        System.out.println("No Move - Hit Other's SafeZone");
                        barrierHit(map, direction, stepsLeft);
                        break; //Stop if blocked
                    }
                } else { // If can't move due to obstacle
                    System.out.println("No Move - Hit Obstacle");
                    barrierHit(map, direction, stepsLeft);
                    break; //Stop if blocked
                }
            } else {
                System.out.println("No Move - Outside Bounds");
                break;
            }

        }
    }

    private Point calculateNextLocation(Point current, Direction direction) {
        int dx = 0, dy = 0;

        switch (direction) {
            case NORTH: dy = 1; break;
            case SOUTH: dy = -1; break;
            case EAST: dx = 1; break;
            case WEST: dx = -1; break;
            case NORTHEAST: dx = 1; dy = 1; break;
            case NORTHWEST: dx = -1; dy = 1; break;
            case SOUTHEAST: dx = 1; dy = -1; break;
            case SOUTHWEST: dx = -1; dy = -1; break;
        }
        return new Point(current.x + dx, current.y + dy);
    }

    // TODO: fix this method
    protected void updateEp(Map map, Point newLocation) {
        if (!map.isInSafeZone(newLocation, group)) {
            System.out.print("EP before setEp: " + getEp());
            System.out.print(" ||| manhattanDistance " + manhattanDistance(location, newLocation));
            setEp(Math.max(0, getEp() - manhattanDistance(location, newLocation))); // Ensure ep doesn't go below 0
            System.out.println(" ||| EP after set Ep: " + getEp());

        } else {
            System.out.print("EP fully restored from " + Red + getEp() + Reset);
            setEp(getInitialEp());
            System.out.println(" to " + Green + getEp() + Reset + " Safe Zone");
        }
    }

    protected void barrierHit(Map map, Direction direction, int stepsLeft) {
        int epLost = switch (direction) {
            case NORTH, SOUTH, EAST, WEST -> stepsLeft;
            case NORTHEAST, NORTHWEST, SOUTHEAST, SOUTHWEST -> 2 * stepsLeft;
        };
        System.out.print("EP before hitting barrier = " + getEp());
        setEp(Math.max(0, getEp() - epLost));
        System.out.println(" --> Lost " + epLost + " --> current EP = " + getEp());

    }

    private void becomeObstacle(Map map) {
        map.addDeadAgent(this.location, this.group);
        map.addObstacle(this.location);
        map.removeAgent(this.location);
    }

    public String getName() {
        return name;
    }

    public int getEp() {
        return ep;
    }

    public String getGroup() {
        return group;
    }

    public void setEp(int ep) {
        this.ep = ep;
    }

    public int getInitialEp() {
        return initialEp;
    }

    public String getAlliance() {
        return alliance;
    }

    public List<String> getMessages() {
        return new ArrayList<>(messages); // Return copy
    }


    public Direction getLastDirection() {
        return lastDirection;
    }

    public static int getTotalMessages() {
        return totalMessages;
    }

    @Override
    public boolean equals(Object o) {  // Important for Sets/Maps
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Agent agent = (Agent) o;
        return Objects.equals(name, agent.name) && Objects.equals(group, agent.group);
    }

    @Override
    public int hashCode() {  // Important for Sets/Maps
        return Objects.hash(name, group);
    }

    protected int manhattanDistance(Point p1, Point p2) { // Utility method
        return Math.abs(p1.x - p2.x) + Math.abs(p1.y - p2.y);
    }

}