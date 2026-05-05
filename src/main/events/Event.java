package events;

import engine.SimulationEngine;

public abstract class Event {
    private final String name;
    private final String description;
    private int remainingTicks;
    private boolean active;

    protected Event(String name, String description, int durationTicks) {
        this.name = name;
        this.description = description;
        this.remainingTicks = durationTicks;
        this.active = false;
    }

    public final void update(SimulationEngine engine) {
        if (!active) {
            apply(engine);
            active = true;
        }

        onTick(engine);
        remainingTicks--;
    }

    public final void finish(SimulationEngine engine) {
        if (active) {
            clear(engine);
            active = false;
        }
    }

    protected void onTick(SimulationEngine engine) {
        // Hook for subclasses that need per-tick work.
    }

    protected abstract void apply(SimulationEngine engine);

    protected abstract void clear(SimulationEngine engine);

    public boolean isExpired() {
        return remainingTicks <= 0;
    }

    public boolean isActive() {
        return active;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public int getRemainingTicks() {
        return remainingTicks;
    }
}
