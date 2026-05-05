package behavior;

import agents.Agent;
import engine.SimulationEngine;

public interface BehaviorStrategy {
    double chooseSpeed(Agent agent, SimulationEngine engine);

    boolean canProceedOnYellow(Agent agent);

    String getName();
}
