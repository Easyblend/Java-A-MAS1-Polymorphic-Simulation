package polymorphicSimulation.agents;

import polymorphicSimulation.environment.Map;
import polymorphicSimulation.environment.Point;
import polymorphicSimulation.utils.Direction;
import polymorphicSimulation.utils.SingletonMasterFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;

public abstract class Agent {

    public String name;
    public String group;
    public Point location;
    private int ep;
    private final int initialEp;
    public List<String> messages;
    public Direction lastDirection;
    private static final int MAX_MESSAGES = 5; // Max messages per agent
    private static int totalMessages = 0; // Keep track of total messages
    protected final Random random = new Random();

    public Agent(String name, String group, Point location, int ep) {
        this.name = name;
        this.group = group;
        this.location = location;
        this.ep = ep;
        this.initialEp = ep;
        this.messages = new ArrayList<>();
        this.lastDirection = null;
        this.generateMessages(); //Generate messages upon Agent creation
    }

    public abstract void move(Map map); //Polymorphic method for agent movement

    protected boolean canMove(Point newLocation, Map map) {
        return map.isWithinBounds(newLocation);
    }

    protected void updateLocation(Point newLocation, Map map) {
            map.removeAgent(this.location); // Remove from old spot
            location = newLocation;
            map.placeAgent(this); // Add to new Spot
    }

    public void exchangeMessages(Agent other, Map map) {
        System.out.println("exchangeMessages method initiated"); // Debug print
        if (this.group.equals(other.group)) { // Allies
            other.messages.stream()
                    .filter(msg -> !this.messages.contains(msg))
                    .forEach(this.messages::add);
            System.out.println(this.name + " (" + this.group + ") met ally " + other.name + " and shared messages.");

        } else if (!map.isInSafeZone(this.location, this.group) && !map.isInSafeZone(other.location, other.group)) {
            // Enemies (and not in SafeZone) - Battle
            System.out.println(this.name + " (" + this.group + ") battled " + other.name + "."); // Print before battle
            battle(other);
            //After the battle
            System.out.println("Result of the battle: " + this.name + " has " + this.messages.size() + ", " + other.name + " has " + other.messages.size() + " messages" );
        }
    }

    private void battle(Agent other) {
        System.out.println("Battle method initiated");
        String[] options = {"stone", "leaf", "scissors"};
        Random random = new Random();
        String myChoice = options[random.nextInt(3)];
        String otherChoice = options[random.nextInt(3)];

        int result = compareChoices(myChoice, otherChoice);

        if (result == 1) { // I win
            transferMessages(other, this, random.nextInt(other.messages.size() + 1) ); // Transfer up to all messages
        } else if (result == -1) { // I lose
            transferMessages(this, other, random.nextInt(this.messages.size() + 1) ); // Transfer up to all messages
        } // Tie: No message transfer
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

    private void transferMessages(Agent loser, Agent winner, int numMessages) {
        System.out.println("TransferMessage method initiated. Agent Winner: " + winner + ", agent loser: " + loser + ", numMessages: " + numMessages);
        if (numMessages <= 0) return;

        Random random = new Random();

        // Add random message to winner.messages if the winner doesn't have it already
        for (int i = 0; i < numMessages && !loser.messages.isEmpty(); i++) {
            String message = loser.messages.remove(random.nextInt(loser.messages.size()));

            if (!winner.messages.contains(message)) {
                winner.messages.add(message);
            } else { //If the winner had the same message, try again (to ensure that the winner gets all the messages it's owed)
                i--;
            }

        }
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
        } else if (Math.abs(dy) > Math.abs(dx)){ //Prioritize vertical movement if dy is greater than dx
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
        System.out.println("GENERATING MESSAGES for " + name + " INITIATED");
        int numMessages = random.nextInt(MAX_MESSAGES) + 1; // Generates 1 to MAX_MESSAGES

        for (int i = 0; i < numMessages; i++) {
            String newMessage = "M" + totalMessages++;
            if (!messages.contains(newMessage)) {
                messages.add(newMessage);
            }
        }
        System.out.println("GENERATING MESSAGES DONE");
    }

    public void transferMessagesToMaster(Map map) {
        System.out.println("transferMessagesToMaster method initiated");
        if (this instanceof Master) return; //Masters don't transfer messages to themselves
        Master master = SingletonMasterFactory.getMasterInstance(group, map.getSafeZoneLocation(group), initialEp); // Make sure initialEp is passed correctly

        if (map.isInSafeZone(location, group)) {
            for (String message : getMessages()) {
                System.out.println("Master " + master.name + "receive Message method initiated");
                master.receiveMessage(message);
                this.messages.remove(message); //Removing the message from the agent after transferring it
                System.out.println(this.name + " transferred a message to Master " + master.name + "."); // Print transfer
                break; // Transfer a single message per encounter
            }
        }
    }

    protected Point moveInDirection(Map map, Direction direction, int maxDistance) {
        System.out.println(this.name + " moveInDirection method initiated");
        Point currentLocation = new Point(location.x, location.y);
        Point newLocation = null;
        for (int i = 0; i < maxDistance; i++) {
            newLocation = calculateNextLocation(currentLocation, direction); // Calculate the next potential location - 1 step

            System.out.println("Potential next step: " + newLocation + "(" + newLocation.x + ", " + newLocation.y + ")"); // debugging

            if (canMove(newLocation, map)) {
                System.out.println("moveInDirection entered 1st if"); // debugging
                Agent otherAgent = map.getAgentAt(newLocation); // Check for other agents at the target location BEFORE moving
                System.out.println("otherAgent: " + otherAgent); // debugging
                if (otherAgent != null && otherAgent != this) {
                    System.out.println("moveInDirection entered 2nd if: exchangeMessages");
                    exchangeMessages(otherAgent, map);
                    break; // Stop further movement for this step after interaction.
                }

                currentLocation = newLocation; // Update currentLocation after checking for agents and exchangeMessages.
                updateLocation(currentLocation, map); // Then update location on map
                transferMessagesToMaster(map); //Transfer messages after each step. Since it has a check if the agent is in a SafeZone it will work even if it is called here
            } else {
                System.out.println("moveInDirection entered else"); // debugging
                break; // Stop if blocked
            }

        }

        updateEp(map, currentLocation); // Update the agent's EP.

        return Objects.requireNonNullElseGet(newLocation, () -> new Point(location.x, location.y));
    }

    private Point calculateNextLocation(Point current, Direction direction) {
        int dx = 0, dy = 0;

        switch (direction) {
            case NORTH: dy = 1; break;
            case SOUTH: dy = -1; break;
            case EAST: dx = 1; break;
            case WEST: dx = -1; break;
            case NORTHEAST: dx = 1; dy = 1; break; // Correct diagonal calculations
            case NORTHWEST: dx = -1; dy = 1; break;
            case SOUTHEAST: dx = 1; dy = -1; break;
            case SOUTHWEST: dx = -1; dy = -1; break;
        }

        return new Point(current.x + dx, current.y + dy);
    }

    protected void updateEp(Map map, Point newLocation) {
        if (!map.isInSafeZone(newLocation, group)) {
            setEp(Math.max(0, getEp() - manhattanDistance(location, newLocation))); // Ensure ep doesn't go below 0
        } else {
            setEp(getInitialEp());
        }
    }


    public int getEp() {
        return ep;
    }

    public void setEp(int ep) { this.ep = ep; }

    public int getInitialEp() {
        return initialEp;
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