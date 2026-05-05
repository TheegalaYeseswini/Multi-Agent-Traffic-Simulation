package agents;

import behavior.BehaviorStrategy;
import environment.Lane;

import java.awt.Color;

public class Pedestrian extends Agent {
    public Pedestrian(String name, Lane lane, BehaviorStrategy behaviorStrategy, double initialProgress, Color color) {
        super(name, lane, behaviorStrategy, initialProgress, 0.65, color, 10);
    }

    @Override
    public String getAgentType() {
        return "Pedestrian";
    }

    @Override
    public boolean isCircular() {
        return true;
    }
}
