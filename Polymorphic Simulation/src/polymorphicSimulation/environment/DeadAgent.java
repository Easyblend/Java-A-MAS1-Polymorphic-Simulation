package polymorphicSimulation.environment;

import polymorphicSimulation.agents.Agent;

public class DeadAgent extends Obstacle {

    private Agent deadAgent;

    public DeadAgent(Agent agent) {
        super(agent.location);
        this.deadAgent = agent;
    }
}