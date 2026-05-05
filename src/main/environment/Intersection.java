package environment;

import agents.Agent;
import traffic.TrafficSignal;
import traffic.TrafficSignal.SignalState;

public class Intersection {
    private final String id;
    private final int centerX;
    private final int centerY;
    private final int size;
    private final TrafficSignal horizontalSignal;
    private final TrafficSignal verticalSignal;

    public Intersection(String id, int centerX, int centerY, int size) {
        this.id = id;
        this.centerX = centerX;
        this.centerY = centerY;
        this.size = size;
        this.horizontalSignal = new TrafficSignal(id + "-H");
        this.verticalSignal = new TrafficSignal(id + "-V");
    }

    public String getId() {
        return id;
    }

    public int getCenterX() {
        return centerX;
    }

    public int getCenterY() {
        return centerY;
    }

    public int getSize() {
        return size;
    }

    public TrafficSignal getHorizontalSignal() {
        return horizontalSignal;
    }

    public TrafficSignal getVerticalSignal() {
        return verticalSignal;
    }

    public void updateSignals(int phaseTick, int greenDuration, int yellowDuration) {
        int cycle = (greenDuration + yellowDuration) * 2;
        int current = phaseTick % cycle;

        if (current < greenDuration) {
            horizontalSignal.setState(SignalState.GREEN);
            verticalSignal.setState(SignalState.RED);
        } else if (current < greenDuration + yellowDuration) {
            horizontalSignal.setState(SignalState.YELLOW);
            verticalSignal.setState(SignalState.RED);
        } else if (current < (greenDuration * 2) + yellowDuration) {
            horizontalSignal.setState(SignalState.RED);
            verticalSignal.setState(SignalState.GREEN);
        } else {
            horizontalSignal.setState(SignalState.RED);
            verticalSignal.setState(SignalState.YELLOW);
        }
    }

    public boolean canProceed(Lane.Direction direction, Agent agent) {
        TrafficSignal signal = getSignalFor(direction);
        if (signal.getState() == SignalState.GREEN) {
            return true;
        }
        if (signal.getState() == SignalState.YELLOW) {
            return agent.getBehaviorStrategy().canProceedOnYellow(agent);
        }
        return false;
    }

    public TrafficSignal getSignalFor(Lane.Direction direction) {
        return direction.isHorizontal() ? horizontalSignal : verticalSignal;
    }
}
