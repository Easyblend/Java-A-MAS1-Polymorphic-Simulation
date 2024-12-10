import polymorphicSimulation.agents.*;
import polymorphicSimulation.environment.Map;
import polymorphicSimulation.environment.Point;
import polymorphicSimulation.utils.SingletonMasterFactory;

import java.util.*;

import static polymorphicSimulation.style.ColorInConsole.*;

public class Main {

    private static final int MAP_WIDTH = 14;
    private static final int MAP_HEIGHT = 8;
    private static final int MAX_AGENTS = 4; // per group
    private static final int INITIAL_EP = 100;
    private static final int MAX_SIMULATION_STEPS = 100;
    private static final boolean SHOW_INFO = true;


    public static void main(String[] args) {
        // 1. Map Setup
        System.out.println(Yellow+"Map Setup Initiated"+Reset);
        Map map = new Map(MAP_WIDTH, MAP_HEIGHT);
        System.out.println(Green+"Map Setup Done"+Reset);

        // 2. Master Agent Creation and Placement
        System.out.println(Yellow+"Master Agent Creation and Placement Initiated"+Reset);
        placeMasters(map);
        System.out.println(Green+"Master Agent Creation and Placement Done"+Reset);

        // 3. Agent Creation and Placement
        System.out.println(Yellow+"Agent Creation and Placement Initiated"+Reset);
        List<Agent> agents = createAgents(map);
        map.printMap();
        System.out.println(Green+"Agent Creation and Placement Done"+Reset);

        // 4. Simulation Loop
        System.out.println(Yellow+"Simulation Loop Initiated"+Reset);
        boolean winnerFound = runSimulation(map, agents); // Store result of runSimulation
        System.out.println(Green+"Simulation Loop Done"+Reset);

        // 5. Determine Winner
        if (!winnerFound) { // Call only if no winner during simulation
            determineWinner(map);
        }

        // 6. Display Final Info
        if (SHOW_INFO) {
            printFinalResults(agents);
        }

    }

    private static List<Agent> createAgents(Map map) {
        List<Agent> agents = new ArrayList<>();
        String[] groups = {"Human", "Elf", "Orc", "Goblin"};

        for (String group : groups) {
            for (int i = 0; i < MAX_AGENTS; i++) {
                Point location = findValidRandomSpot(map); //Helper method to find open spots

                Agent agent = switch (group) {
                    case "Human" -> new Human("Human" + i, group, location, INITIAL_EP, "LightSide");
                    case "Elf" -> new Elf("Elf" + i, group, location, INITIAL_EP, "LightSide");
                    case "Orc" -> new Orc("Orc" + i, group, location, INITIAL_EP, "DarkSide");
                    case "Goblin" -> new Goblin("Goblin" + i, group, location, INITIAL_EP, "DarkSide");
                    default -> throw new IllegalStateException("Unexpected value: " + group);
                };

                agent.location = location; // Set location after agent creation.
                map.placeAgent(agent);
                agents.add(agent);
            }
        }
        return agents;
    }

    private static Point findValidRandomSpot(Map map) {
        Random random = new Random();
        Point location;
        do {
            int x = random.nextInt(MAP_WIDTH);
            int y = random.nextInt(MAP_HEIGHT);
            location = new Point(x, y);
        } while (!map.isTileFree(location) || map.isSafeZone(location));

        return location;
    }


    private static void placeMasters(Map map) {
        String[] groups = {"Human", "Elf", "Orc", "Goblin"};
        String[] alliances = {"LightSide", "LightSide", "DarkSide", "DarkSide"}; // Alliances corresponding to the groups

        for (int i = 0; i < groups.length; i++) {
            String group = groups[i];
            String alliance = alliances[i];
            Point location = map.getSafeZoneLocation(group);
            Master master = SingletonMasterFactory.getMasterInstance(group, location, INITIAL_EP, alliance); // Pass alliance
            map.placeAgent(master);
        }
    }

    private static boolean runSimulation(Map map, List<Agent> agents) {

        for (int step = 0; step < MAX_SIMULATION_STEPS; step++) {
            System.out.println("Simulation Step: " + (step + 1));

            // Print master messages at the start of each step

            if(SHOW_INFO){
                printMasterMessages(map);
                printAgentStatus(agents);
            }

            Collections.shuffle(agents); // Randomize agent order

            for (Agent agent : agents) {
                agent.move(map);
            }

            map.printMap(); // Print map after agent movements

            // Check for win condition after each step
            if (checkWinCondition(map)) {
                return true;
            }
            try {
                Thread.sleep(0); // Delay as needed (in milliseconds)
            } catch (InterruptedException e) {
                System.out.println("something went wrong");
                e.printStackTrace();
            }
        }
        return false; // No winner found during simulation
    }

    private static void printMasterMessages(Map map) {
        String[] groups = {"Human", "Elf", "Orc", "Goblin"};
        String[] alliances = {"LightSide", "LightSide", "DarkSide", "DarkSide"};

        for (int i = 0; i < groups.length; i++) { //Use index-based loop
            Master master = SingletonMasterFactory.getMasterInstance(groups[i], map.getSafeZoneLocation(groups[i]), INITIAL_EP, alliances[i]);
            System.out.println(master.name + " (Master " + groups[i] + ") messages: " + master.getMessages());
        }
    }

    private static void printAgentStatus(List<Agent> agents) {
        for (Agent agent : agents) {
            if (!(agent instanceof Master)) { // Exclude Masters (already printed)
                System.out.println(agent.name + " (" + agent.group + ") at (" + agent.location.x + ", "
                        + agent.location.y + ") EP: " + agent.getEp() + ". Messages: " + agent.getMessages());
            }
        }
    }

    private static void printFinalResults(List<Agent> agents){
        for (Agent agent : agents) {
            if(!(agent instanceof Master)) {
                System.out.println(agent.name + " has " + agent.getMessages().size() + " messages.");
            }
        }
    }

    private static boolean checkWinCondition(Map map) {
        String[] groups = {"Human", "Elf", "Orc", "Goblin"};
        String[] alliances = {"LightSide", "LightSide", "DarkSide", "DarkSide"};
        int totalMessages = Agent.getTotalMessages();

        for (int i = 0; i < groups.length; i++) { //Use index-based loop
            Master master = SingletonMasterFactory.getMasterInstance(groups[i], map.getSafeZoneLocation(groups[i]), INITIAL_EP, alliances[i]);

            if (master.getMessages().size() == totalMessages) {
                System.out.println(Green+"Group " + groups[i] + " wins! (Master collected " +BackgroundBlue+Black+ "all messages - " + master.getMessages().size() + "/" + totalMessages + ")" +Reset);
                printMasterMessages(map);
                return true; // End the simulation
            }
        }
        return false;
    }

    private static void determineWinner(Map map) {
        String[] groups = {"Human", "Elf", "Orc", "Goblin"};
        String[] alliances = {"LightSide", "LightSide", "DarkSide", "DarkSide"};
        List<String> winningGroups = new ArrayList<>(); // List to store potential multiple winners
        int maxMessages = 0;

        for (int i = 0; i < groups.length; i++) { // Use index-based loop
            Master master = SingletonMasterFactory.getMasterInstance(groups[i], map.getSafeZoneLocation(groups[i]), INITIAL_EP, alliances[i]);
            int numMessages = master.getMessages().size();

            if (numMessages > maxMessages) {
                winningGroups.clear();      // Clear previous winners
                winningGroups.add(groups[i]);   // Add current group as the sole winner (so far)
                maxMessages = numMessages; // Update maxMessages
            } else if (numMessages == maxMessages && numMessages > 0) {
                winningGroups.add(groups[i]); // Add current group to list of winners (tie)
            }
        }

        if (winningGroups.isEmpty()) {
            System.out.println("No winner. No Master collected any messages.");
        } else if (winningGroups.size() == 1) {
            int totalMessages = Agent.getTotalMessages();
            System.out.println(Green+"Group " + winningGroups.getFirst() + " wins! (Collected most messages: " + maxMessages + "/" + totalMessages + ")"+Reset);
        } else {
            System.out.print(Green+"It's a tie! Winning groups: ");
            for (int i = 0; i < winningGroups.size(); i++) {
                System.out.print(winningGroups.get(i));
                if (i < winningGroups.size() - 1) {
                    System.out.print(", ");
                }
            }
            System.out.println(" (Collected " + maxMessages + " messages each)"+Reset);
        }
        printMasterMessages(map);
    }
}