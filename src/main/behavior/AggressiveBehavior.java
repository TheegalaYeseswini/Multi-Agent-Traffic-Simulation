package behavior;

import agents.Agent;
import engine.SimulationEngine;

public class AggressiveBehavior implements BehaviorStrategy {
    @Override
    public double chooseSpeed(Agent agent, SimulationEngine engine) {
        return agent.getBaseSpeed() * 1.35;
    }

    @Override
    public boolean canProceedOnYellow(Agent agent) {
        return true;
    }

    @Override
    public String getName() {
        return "Aggressive";
    }
}
