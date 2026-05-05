package events;

import engine.SimulationEngine;

import java.awt.geom.Point2D;

public class CongestionEvent extends Event {
    private final int startX;
    private final int startY;
    private final int width;
    private final int height;
    private final double slowdownFactor;

    public CongestionEvent(int startX, int startY, int width, int height, int durationTicks, double slowdownFactor) {
        super(
                "Congestion",
                "Congestion slowing traffic around zone (" + startX + ", " + startY + ")",
                durationTicks);
        this.startX = startX;
        this.startY = startY;
        this.width = width;
        this.height = height;
        this.slowdownFactor = slowdownFactor;
    }

    @Override
    protected void apply(SimulationEngine engine) {
        // Presence in the active event list is enough for the engine to honor the slowdown.
    }

    @Override
    protected void clear(SimulationEngine engine) {
        // No state to revert.
    }

    public boolean contains(Point2D.Double point) {
        return point.x >= startX
                && point.x <= startX + width
                && point.y >= startY
                && point.y <= startY + height;
    }

    public int getStartX() {
        return startX;
    }

    public int getStartY() {
        return startY;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public double getSlowdownFactor() {
        return slowdownFactor;
    }
}
