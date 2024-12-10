package polymorphicSimulation.agents;

import polymorphicSimulation.environment.Map;
import polymorphicSimulation.environment.Point;
import polymorphicSimulation.utils.Direction;
import polymorphicSimulation.utils.SingletonMasterFactory;
import polymorphicSimulation.utils.MonteCarloRNG;

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
    public boolean lastHitObstacle = false;
    private static final int MAX_MESSAGES = 5; // Max messages generated per agent
    private static int totalMessages = 0; // Keep track of total messages
    protected final MonteCarloRNG MonteCarloRNG = new MonteCarloRNG();

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

    public abstract String getColor();  // Abstract method for color

    // Common movement and logging logic
    protected void commonMoveLogic(Map map, List<Direction> possibleDirections) {
        if (getEp() <= 0) {
            System.out.println(getColor()+getDeadMessage()+Reset);
            return;
        }

        System.out.println(getColor()+getStartMoveMessage()+Reset);

        if (possibleDirections.isEmpty()) {
            System.out.println(name + " cannot move (no possible directions)");
            return;
        }

        Direction direction = possibleDirections.get(MonteCarloRNG.nextInt(possibleDirections.size()));
        int maxDistance = getMaxDistance(); // You can vary maxDistance if needed

        System.out.println(name + " planning to move " + maxDistance + " steps " + direction); // debugging
        moveInDirection(map, direction, maxDistance);

        lastDirection = direction;
        System.out.println(Red+getEndMoveMessage()+Reset);
    }

    // Common logic for filtered directions
    protected List<Direction> getFilteredDirections(Map map, List<Direction> allDirections) {
        List<Direction> filteredDirections = new ArrayList<>(allDirections);

        if (lastHitObstacle) {
            filteredDirections.remove(lastDirection); // Remove last direction if agent last hit an obstacle.
            lastHitObstacle = false;
        }

        if (getEp() != 0 && getEp() * 1.0 / getInitialEp() <= 0.2) {
            Direction toSafeZone = getSafeZoneDirection(map);
            System.out.println(getColor()+getName() + " EP <= 0.2 - SafeZone direction: " + toSafeZone+Reset);
            if (toSafeZone != null) {
                filteredDirections.clear(); // Prioritize direction to SafeZone
                filteredDirections.add(toSafeZone);
            }
        }
        return filteredDirections;
    }

    // Helper methods to generate common messages
    private String getDeadMessage() {
        return name + " is Dead";
    }

    private String getStartMoveMessage() {
        return name + " starting move at (" + location.x + ", " + location.y + ") with EP: "
                + getEp() + ". Messages: " + getMessages().size();
    }

    private String getEndMoveMessage() {
        return name + " ending move at (" + location.x + ", " + location.y + ") with EP: "
                + getEp() + ". Messages: " + getMessages().size();
    }

    private int getMaxDistance() {
        return MonteCarloRNG.nextInt(3) + 1; // Default distance 1-3, can be adjusted
    }

    protected boolean withinBounds(Point newLocation, Map map) {
        return map.isTileWithinBounds(newLocation);
    }

    protected boolean TileObstacle(Point newLocation, Map map) {
        return map.isObstacleAt(newLocation);
    }

    protected void updateLocation(Point newLocation, Map map) {
        map.removeAgent(this.location);
        location = newLocation;
        map.placeAgent(this);
    }

    public void exchangeMessages(Agent other, Map map) {
        if (this.group.equals(other.group)) { // Same group - Union of messages
            unionMessages(other);

        } else if (this.getAlliance().equals(other.getAlliance())) { // Different group, same alliance - alliance exchange
            exchangeAllianceMessages(other);

        } else if (!map.isInSafeZone(this.location, this.group) && !map.isInSafeZone(other.location, other.group)) {
            // Different group, different alliance, not in safe zone - Battle
            System.out.println(Cyan + this.name + " (" + this.group + ") of " + this.getMessages().size() + " messages, battled " + other.name + " (" + other.group + ") of " + other.getMessages().size() + " messages." + Reset);
            battle(other);
            System.out.println(Cyan + "Result of the battle: " + this.name + " has " + this.messages.size() + ", " + other.name + " has " + other.messages.size() + " messages" + Reset);
        }
    }

    private void unionMessages(Agent other) {
        Set<String> combinedMessages = new HashSet<>(this.messages); // Use a Set to automatically handle duplicates
        combinedMessages.addAll(other.messages);      // Add all of the other agent's messages (more efficient this way)

        System.out.print(Cyan + this.name + " (" + this.group + ") met his fellow " + other.name + " and unioned messages." + Reset);
        System.out.print(Cyan + " Before union: " + this.name + ": " + this.messages.size() + " messages, " + other.name + ": " + other.messages.size() + " messages" + Reset);

        this.messages = new ArrayList<>(combinedMessages);  // Update my messages
        other.messages = new ArrayList<>(combinedMessages); // Update the other agent's messages

        System.out.println(Cyan + ", After union: Both have " + this.messages.size() + " messages" + Reset);

    }

    private void exchangeAllianceMessages(Agent other) { //New method
        System.out.println(Cyan + this.name + " (" + this.group + ") met " + other.name + " from allied group (" + other.getGroup() + ") and exchanged messages." + Reset);

        int numMessagesToExchange = MonteCarloRNG.nextInt(3) + 1;  // Exchange 1-3 messages

        System.out.print(Cyan + "Before exchange: " + this.name + " has " + this.messages.size() + ", " + other.name + " has " + other.messages.size() + ". " + Reset);

        List<String> myMessagesCopy = new ArrayList<>(this.messages); // Make copies of messages to avoid ConcurrentModificationException
        List<String> otherMessagesCopy = new ArrayList<>(other.messages);

        // Exchange unique messages between the 2 agents.
        transferUniqueMessages(myMessagesCopy, other, numMessagesToExchange);
        transferUniqueMessages(otherMessagesCopy, this, numMessagesToExchange);

        System.out.println(Cyan + "After exchange: " + this.name + " has " + this.messages.size() + ", " + other.name + " has " + other.messages.size() + Reset);
    }

    private void transferUniqueMessages(List<String> sourceMessages, Agent recipient, int numMessages) {
        MonteCarloRNG MonteCarloRNG = new MonteCarloRNG();

        for (int i = 0; i < numMessages && !sourceMessages.isEmpty(); i++) {
            String message = sourceMessages.remove(MonteCarloRNG.nextInt(sourceMessages.size()));

            if (!recipient.messages.contains(message)) {
                recipient.messages.add(message);
            } else {
                i--; //If the recipient already had that message, then try transferring another message
            }
        }
    }

    private void battle(Agent other) {
        String[] options = {"stone", "leaf", "scissors"};
        MonteCarloRNG MonteCarloRNG = new MonteCarloRNG();
        String myChoice = options[MonteCarloRNG.nextInt(3)];
        String otherChoice = options[MonteCarloRNG.nextInt(3)];

        System.out.print(Cyan + this.getName() + " chose " + myChoice + ", " + other.getName() + " chose: " + otherChoice + " ||| " + Reset);

        int result = compareChoices(myChoice, otherChoice);

        if (result == 1) { // won
            transferMessages(other, this);
        } else if (result == -1) { // lost
            transferMessages(this, other);
        } else {
            System.out.print(Cyan + "Tie, no message transfer. " + Reset);
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

    private void transferMessages(Agent loser, Agent winner) { // Removed numMessages parameter
        System.out.println(Cyan + "Winner: " + winner.getName() + ", loser: " + loser.getName() + Reset);

        if(loser.messages.isEmpty()){ // ensure loser has at least 1 message
            System.out.println(Cyan+"Loser " + loser.getName() + " has no messages to transfer"+Reset);
            return;
        }
        MonteCarloRNG MonteCarloRNG = new MonteCarloRNG();
        int numMessagesToTransfer = MonteCarloRNG.nextInt(loser.messages.size()) + 1 ;;

        int uniqueMessagesTransferred = 0;

        System.out.print(Cyan+"Winner will take " + numMessagesToTransfer + " messages out of " + loser.messages.size()+Reset);  // debugging

        // Transfer unique messages first
        Iterator<String> iterator = loser.messages.iterator(); //Use iterator to avoid ConcurrentModificationException
        while (iterator.hasNext() && uniqueMessagesTransferred < numMessagesToTransfer) {
            String message = iterator.next();
            if (!winner.messages.contains(message)) {
                winner.messages.add(message);
                iterator.remove(); //Safe removal of the message using the iterator
                uniqueMessagesTransferred++;
            }
        }

        System.out.println(Cyan+" ||| Unique messages taken: " + uniqueMessagesTransferred+Reset); // debugging

        // If not enough unique messages were transferred, remove remaining from loser
        int remainingMessagesToTransfer = numMessagesToTransfer - uniqueMessagesTransferred;
        if (remainingMessagesToTransfer > 0) {
            removeMonteCarloRNGMessages(loser, remainingMessagesToTransfer);
        }
    }

    private void removeMonteCarloRNGMessages(Agent loser, int numToRemove) {
        MonteCarloRNG MonteCarloRNG = new MonteCarloRNG();

        System.out.println(Cyan+"Messages left to remove: " + numToRemove+Reset);

        for (int i = 0; i < numToRemove && !loser.messages.isEmpty(); i++) {
            loser.messages.remove(MonteCarloRNG.nextInt(loser.messages.size()));
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
            return null;
        }
    }

    protected void generateMessages() {
        int numMessages = MonteCarloRNG.nextInt(MAX_MESSAGES) + 1; // Generates 1 to MAX_MESSAGES

        for (int i = 0; i < numMessages; i++) {
            String newMessage = "M" + totalMessages++;
            if (!messages.contains(newMessage)) {
                messages.add(newMessage);
            }
        }
    }

    public void transferMessagesToMaster(Map map) {
        if (this instanceof Master) return; // Masters don't transfer messages to themselves

        Master master = SingletonMasterFactory.getMasterInstance(group, map.getSafeZoneLocation(group), initialEp, this.alliance);

        if (map.isInSafeZone(location, group)) {
            int numOfMessages = master.getMessages().size();
            for (String message : getMessages()) {
                master.receiveMessage(message);
            }
            System.out.println("Transferred to master. Before " + master.getName() + " had " + numOfMessages + " messages. After: " + master.getMessages().size() + " messages"); // debugging
        }
    }

    protected void moveInDirection(Map map, Direction direction, int maxDistance) {
        Point currentLocation = new Point(location.x, location.y);
        Point newLocation = null;
        for (int i = 0; i < maxDistance; i++) {
            int stepsLeft = maxDistance - i;
            newLocation = calculateNextLocation(currentLocation, direction);

            if (!withinBounds(newLocation, map)) {
                System.out.println("No Move - Outside Bounds");
                break;
            }
            if (TileObstacle(newLocation, map)) {
                System.out.println("No Move - Hit Obstacle");
                barrierHit(map, direction, stepsLeft);
                break; //Stop if blocked
            }
            if (map.isInOtherSafeZone(newLocation, group)) {
                System.out.println("No Move - Hit Other's SafeZone");
                barrierHit(map, direction, stepsLeft);
                break; //Stop if blocked
            }
            Agent otherAgent = map.getAgentAt(newLocation);  // Check if another agent is present at the target location
            if (otherAgent != null && otherAgent != this) {
                handleAgentInteraction(otherAgent, map);
                break; // Stop further movement after interaction
            } else {
                updateLocation(newLocation, map); // updating location before updating EP
                if(updateEp(map, currentLocation)){// updateEp and check death (true for death)
                    break;
                }
                currentLocation = newLocation;
                transferMessagesToMaster(map);
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

    protected boolean updateEp(Map map, Point oldLocation) { //return true if agent dead
        if (map.isInSafeZone(location, group)) {
            System.out.print("EP fully restored from " + Red + getEp() + Reset);
            setEp(getInitialEp());
            System.out.println(" to " + Green + getEp() + Reset + " Safe Zone");
        } else {
            System.out.print("EP before setEp: " + getEp());
            System.out.print(" ||| manhattanDistance " + manhattanDistance(location, oldLocation));
            setEp(Math.max(0, getEp() - manhattanDistance(location, oldLocation))); // Ensure ep doesn't go below 0
            System.out.println(" ||| EP after: " + getEp());

            if (getEp() <= 0 && !(this instanceof Master)) { //Check if agent is dead after movement
                System.out.println(getName() + " becomeObstacle()");
                becomeObstacle(map);
                return true;
            }
        }
        return false;
    }

    protected void barrierHit(Map map, Direction direction, int stepsLeft) {
        int epLost = switch (direction) {
            case NORTH, SOUTH, EAST, WEST -> stepsLeft;
            case NORTHEAST, NORTHWEST, SOUTHEAST, SOUTHWEST -> 2 * stepsLeft;
        };
        lastHitObstacle = true;
        System.out.print("EP before hitting barrier = " + getEp());
        setEp(Math.max(0, getEp() - epLost));
        System.out.println(" --> Lost " + epLost + " --> current EP = " + getEp());

        // TODO: this is repeated with updateEp, try to combine later
        if (getEp() <= 0 && !(this instanceof Master)) { //Check if agent is dead after movement
            System.out.println(getName() + " becomeObstacle()");
            becomeObstacle(map);
        }
    }

    private void becomeObstacle(Map map) {
        map.addDeadAgent(this.location, this.group);
        map.addObstacle(this.location);
        map.removeAgent(this.location);
    }

    private void handleAgentInteraction(Agent otherAgent, Map map) {
        if (otherAgent instanceof Master) { //Check if other agent is Master before interaction. If so, only transfer messages
            System.out.println(BrightBlack+BackgroundBrightCyan+getName() + " had the honor of having a cup of tea with his master!"+Reset);
            transferMessagesToMaster(map); // useful if there's no safe zone around the master
        } else {
            exchangeMessages(otherAgent, map);
        }
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