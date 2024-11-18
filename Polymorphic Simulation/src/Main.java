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
        Map map = new Map(MAP_WIDTH, MAP_HEIGHT);
        System.out.println(Green+"Map Setup Done"+Reset);

        // 2. Master Agent Creation and Placement
        System.out.println(Yellow+"Master Agent Creation and Placement Initiated"+Reset);
        placeMasters(map);
        System.out.println(Green+"Master Agent Creation and Placement Done"+Reset);

        // 2.1. Generate Obstacles
        System.out.println(Yellow+"Generate Obstacles Initiated"+Reset);
        map.generateObstacles();
        System.out.println(Green+"Generate Obstacles Done"+Reset);

        // 3. Agent Creation and Placement
        System.out.println(Yellow+"Agent Creation and Placement Initiated"+Reset);
        List<Agent> agents = createAgents(map);
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
                Point location = findValidRandomSpot(map); //Helper method to find open spots
                Agent agent = switch (group) {
                    case "Human" -> new Human("Human" + i, group, location, INITIAL_EP);
                    case "Elf" -> new Elf("Elf" + i, group, location, INITIAL_EP);
                    case "Orc" -> new Orc("Orc" + i, group, location, INITIAL_EP);
                    case "Goblin" -> new Goblin("Goblin" + i, group, location, INITIAL_EP);
                    default -> throw new IllegalStateException("Unexpected value: " + group);
                };
                agents.add(agent);
                map.placeAgent(agent);
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
                Thread.sleep(500); // Delay as needed (in milliseconds)
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
        String winningGroup = null;
        int maxMessages = -1;

        for (String group : groups) {
            Master master = SingletonMasterFactory.getMasterInstance(group, map.getSafeZoneLocation(group), INITIAL_EP);
            int numMessages = master.getMessages().size();
            if (numMessages > maxMessages) {
                maxMessages = numMessages;
                winningGroup = group;
            }
        }

        if (winningGroup != null) {
            System.out.println("Group " + winningGroup + " wins! (Collected most messages: " + maxMessages + ")");
        } else {
            System.out.println("No winner. No Master collected any messages.");
        }
    }
}