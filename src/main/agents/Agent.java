package agents;

import behavior.BehaviorStrategy;
import engine.SimulationEngine;
import environment.Grid;
import environment.Intersection;
import environment.Lane;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class Agent {
    private static final AtomicInteger NEXT_ID = new AtomicInteger(1);

    private final int id;
    private final String name;
    private final Lane lane;
    private final BehaviorStrategy behaviorStrategy;
    private final double baseSpeed;
    private final Color color;
    private final int renderSize;
    private double progress;
    private double currentSpeed;

    protected Agent(
            String name,
            Lane lane,
            BehaviorStrategy behaviorStrategy,
            double initialProgress,
            double baseSpeed,
            Color color,
            int renderSize) {
        this.id = NEXT_ID.getAndIncrement();
        this.name = name;
        this.lane = lane;
        this.behaviorStrategy = behaviorStrategy;
        this.progress = initialProgress;
        this.baseSpeed = baseSpeed;
        this.color = color;
        this.renderSize = renderSize;
        this.currentSpeed = 0.0;
    }

    public void update(SimulationEngine engine) {
        if (lane.isBlocked()) {
            currentSpeed = 0.0;
            return;
        }

        double desiredSpeed = Math.min(behaviorStrategy.chooseSpeed(this, engine), lane.getSpeedLimit());
        double adjustedSpeed = engine.applySpeedModifiers(this, desiredSpeed);
        double absoluteTarget = progress + adjustedSpeed;
        double finalTarget = respectTrafficSignals(engine.getGrid(), absoluteTarget);

        currentSpeed = Math.max(0.0, finalTarget - progress);
        progress = normalizeProgress(finalTarget, lane.getLength());
    }

    private double respectTrafficSignals(Grid grid, double absoluteTarget) {
        double laneLength = lane.getLength();
        double stopAt = absoluteTarget;
        List<Intersection> intersections = grid.getIntersectionsForLane(lane);

        for (Intersection intersection : intersections) {
            double marker = lane.getProgressAtIntersection(intersection);
            if (marker < 0) {
                continue;
            }

            double[] candidates = absoluteTarget > laneLength
                    ? new double[]{marker, marker + laneLength}
                    : new double[]{marker};

            for (double candidate : candidates) {
                if (candidate > progress && candidate <= absoluteTarget
                        && !intersection.canProceed(lane.getDirection(), this)) {
                    stopAt = Math.min(stopAt, Math.max(progress, candidate - 0.8));
                }
            }
        }

        return stopAt;
    }

    private double normalizeProgress(double candidateProgress, double laneLength) {
        if (laneLength <= 0) {
            return 0;
        }

        double normalized = candidateProgress % laneLength;
        if (normalized < 0) {
            normalized += laneLength;
        }
        return normalized;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Lane getLane() {
        return lane;
    }

    public BehaviorStrategy getBehaviorStrategy() {
        return behaviorStrategy;
    }

    public double getBaseSpeed() {
        return baseSpeed;
    }

    public double getCurrentSpeed() {
        return currentSpeed;
    }

    public Point2D.Double getPosition() {
        return lane.getPointAt(progress);
    }

    public double getProgress() {
        return progress;
    }

    public Color getColor() {
        return color;
    }

    public int getRenderSize() {
        return renderSize;
    }

    public abstract String getAgentType();

    public abstract boolean isCircular();
}
