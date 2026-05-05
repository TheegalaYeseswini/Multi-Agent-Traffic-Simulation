package events;

import engine.SimulationEngine;
import environment.Lane;

public class AccidentEvent extends Event {
    private final Lane affectedLane;

    public AccidentEvent(Lane affectedLane, int durationTicks) {
        super(
                "Accident",
                "Accident blocking " + affectedLane.getRoadName() + " / " + affectedLane.getId(),
                durationTicks);
        this.affectedLane = affectedLane;
    }

    @Override
    protected void apply(SimulationEngine engine) {
        affectedLane.setBlocked(true, getDescription());
    }

    @Override
    protected void clear(SimulationEngine engine) {
        affectedLane.setBlocked(false, null);
    }

    public Lane getAffectedLane() {
        return affectedLane;
    }
}
