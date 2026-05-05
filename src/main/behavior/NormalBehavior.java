package behavior;

import agents.Agent;
import engine.SimulationEngine;

public class NormalBehavior implements BehaviorStrategy {
    @Override
    public double chooseSpeed(Agent agent, SimulationEngine engine) {
        return agent.getBaseSpeed();
    }

    @Override
    public boolean canProceedOnYellow(Agent agent) {
        return false;
    }

    @Override
    public String getName() {
        return "Normal";
    }
}
