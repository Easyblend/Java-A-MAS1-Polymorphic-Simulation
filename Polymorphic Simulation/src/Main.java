import polymorphicSimulation.agents.*;
import polymorphicSimulation.environment.Map;
import polymorphicSimulation.environment.Point;
import polymorphicSimulation.utils.SingletonMasterFactory;

import java.util.*;

import static polymorphicSimulation.style.ColorInConsole.*;

public class Main {

    private static final int MAP_WIDTH = 14;
    private static final int MAP_HEIGHT = 8;
    private static final int MAX_AGENTS = 2; //per group
    private static final int INITIAL_EP = 100;
    private static final int MAX_SIMULATION_STEPS = 50;


    public static void main(String[] args) {
        // 1. Map Setup
        System.out.println(Yellow+"Map Setup Initiated"+Reset);
        Map map = new Map(MAP_WIDTH, MAP_HEIGHT); // Obstacles & SafeZones are generated within the map constructor
        System.out.println("Obstacles & SafeZones are generated within the map constructor");

        map.printMap(); //Print map after Obstacles & SafeZones are generated
        System.out.println(Green+"Map Setup Done"+Reset);

        // 2. Master Agent Creation and Placement
        System.out.println(Yellow+"Master Agent Creation and Placement Initiated"+Reset);
        placeMasters(map);

        map.printMap();
        System.out.println(Green+"Master Agent Creation and Placement Done"+Reset);

        System.out.println(Green+"Generate Obstacles Done"+Reset);

        // 3. Agent Creation and Placement
        System.out.println(Yellow+"Agent Creation and Placement Initiated"+Reset);
        List<Agent> agents = createAgents(map);

        map.printMap(); //Print map after Masters & Agents have been placed
        System.out.println(Green+"Agent Creation and Placement Done"+Reset);

        // 4. Simulation Loop
        System.out.println(Yellow+"Simulation Loop Initiated"+Reset);
        runSimulation(map, agents);
        System.out.println(Green+"Simulation Loop Done"+Reset);

        // 5. Determine Winner
        System.out.println(Yellow+"Determine Winner Initiated"+Reset);
        determineWinner(map);
        System.out.println(Green+"Determine Winner Done"+Reset);
    }

    private static List<Agent> createAgents(Map map) {
        List<Agent> agents = new ArrayList<>();
        String[] groups = {"Human", "Elf", "Orc", "Goblin"};
        Random random = new Random();

        for (String group : groups) {
            for (int i = 0; i < MAX_AGENTS; i++) {
                String groupName = group; //The group name of the agent
                if (i < 4) { //The number of initial safeZones
                    groupName = groupName + i; //Modify group name so that the agents are placed correctly to the initially designated safeZones.
                }


                Point location = findValidRandomSpot(map); //Helper method to find open spots


                Agent agent = switch (group) {
                    case "Human" -> new Human("Human" + i, group, location, INITIAL_EP);
                    case "Elf" -> new Elf("Elf" + i, group, location, INITIAL_EP);
                    case "Orc" -> new Orc("Orc" + i, group, location, INITIAL_EP);
                    case "Goblin" -> new Goblin("Goblin" + i, group, location, INITIAL_EP);
                    default -> throw new IllegalStateException("Unexpected value: " + group);
                };


                agent.location = location; // Set location *after* agent creation.
                map.placeAgent(agent);
                agents.add(agent);

            }
        }
        return agents;
    }

    private static void placeMasters(Map map) {
        String[] groups = {"Human", "Elf", "Orc", "Goblin"};
        for (String group : groups) {
            Point location = map.getSafeZoneLocation(group);
            Master master = SingletonMasterFactory.getMasterInstance(group, location, INITIAL_EP);
            map.placeAgent(master);
        }

    }

    private static Point findValidRandomSpot(Map map) {
        Random random = new Random();
        Point location;
        do {
            int x = random.nextInt(MAP_WIDTH);
            int y = random.nextInt(MAP_HEIGHT);
            location = new Point(x, y);
        } while (!map.isTileFree(location));

        return location;
    }

    private static void runSimulation(Map map, List<Agent> agents) {
        Random random = new Random();
//        Scanner scanner = new Scanner(System.in); // Scanner for pausing

        for (int step = 0; step < MAX_SIMULATION_STEPS; step++) {
            System.out.println("Simulation Step: " + (step + 1));

            // Print master messages at the start of each step
            printMasterMessages(map);

            printAgentStatus(agents);

            Collections.shuffle(agents); // Randomize agent order

            for (Agent agent : agents) {
                agent.move(map);
            }

            map.printMap(); // Print map after agent movements

            // Check for win condition after each step
            if (checkWinCondition(map)) {
                return;
            }
            try {
                Thread.sleep(0); // Delay as needed (in milliseconds)
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

//            System.out.print("Press Enter to continue...");
//            scanner.nextLine(); // Pause until Enter is pressed
        }
//        scanner.close(); // Close the Scanner
    }

    private static void printMasterMessages(Map map) {
        String[] groups = {"Human", "Elf", "Orc", "Goblin"};

        for (String group : groups) {
            Master master = SingletonMasterFactory.getMasterInstance(group, map.getSafeZoneLocation(group), INITIAL_EP);
            System.out.println(master.name + " (Master " + group + ") messages: " + master.getMessages());
        }
    }

    private static void printAgentStatus(List<Agent> agents) {
        for (Agent agent : agents) {
            if (!(agent instanceof Master)) { // Exclude Masters (already printed)
                System.out.println(agent.name + " (" + agent.group + ") at (" + agent.location.x + ", " + agent.location.y + ") EP: " + agent.getEp() + ". Messages: " + agent.getMessages());
            }
        }
    }

    private static boolean checkWinCondition(Map map) {
        String[] groups = {"Human", "Elf", "Orc", "Goblin"};
        int totalMessages = Agent.getTotalMessages();

        for (String group : groups) {
            Master master = SingletonMasterFactory.getMasterInstance(group, map.getSafeZoneLocation(group), INITIAL_EP);

            if (master.getMessages().size() == totalMessages) {
                System.out.println("Group " + group + " wins! (Master collected all messages)");
                return true; // End the simulation
            }
        }
        return false;
    }

    private static void determineWinner(Map map) {
        String[] groups = {"Human", "Elf", "Orc", "Goblin"};
        List<String> winningGroups = new ArrayList<>(); // List to store potential multiple winners
        int maxMessages = 0;

        for (String group : groups) {
            Master master = SingletonMasterFactory.getMasterInstance(group, map.getSafeZoneLocation(group), INITIAL_EP);
            int numMessages = master.getMessages().size();

            if (numMessages > maxMessages) {
                winningGroups.clear();      // Clear previous winners
                winningGroups.add(group);   // Add current group as the sole winner (so far)
                maxMessages = numMessages; // Update maxMessages
            } else if (numMessages == maxMessages && numMessages > 0) {
                winningGroups.add(group); // Add current group to list of winners (tie)
            }
        }

        printMasterMessages(map);

        if (winningGroups.isEmpty()) {
            System.out.println("No winner. No Master collected any messages.");
        } else if (winningGroups.size() == 1) {
            System.out.println("Group " + winningGroups.get(0) + " wins! (Collected most messages: " + maxMessages + ")");
        } else {
            System.out.print("It's a tie! Winning groups: ");
            for (int i = 0; i < winningGroups.size(); i++) {
                System.out.print(winningGroups.get(i));
                if (i < winningGroups.size() - 1) {
                    System.out.print(", ");
                }
            }
            System.out.println(" (Collected " + maxMessages + " messages each)");
        }
    }
}