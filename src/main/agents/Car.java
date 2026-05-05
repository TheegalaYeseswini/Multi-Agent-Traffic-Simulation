package agents;

import behavior.BehaviorStrategy;
import environment.Lane;

import java.awt.Color;

public class Car extends Agent {
    public Car(String name, Lane lane, BehaviorStrategy behaviorStrategy, double initialProgress, Color color) {
        super(name, lane, behaviorStrategy, initialProgress, 1.35, color, 18);
    }

    @Override
    public String getAgentType() {
        return "Car";
    }

    @Override
    public boolean isCircular() {
        return false;
    }
}
