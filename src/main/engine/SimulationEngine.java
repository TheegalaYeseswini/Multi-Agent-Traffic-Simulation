package engine;

import agents.Agent;
import agents.Car;
import agents.Pedestrian;
import behavior.AggressiveBehavior;
import behavior.NormalBehavior;
import environment.Grid;
import environment.Intersection;
import environment.Lane;
import environment.Lane.LaneType;
import events.AccidentEvent;
import events.CongestionEvent;
import events.Event;
import traffic.SignalController;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class SimulationEngine {
    private final Grid grid;
    private final SignalController signalController;
    private final List<Agent> agents;
    private final List<Event> activeEvents;
    private final Random random;
    private int tick;

    public SimulationEngine() {
        this.grid = Grid.createDefaultGrid();
        this.signalController = new SignalController(grid.getIntersections());
        this.agents = new ArrayList<>();
        this.activeEvents = new ArrayList<>();
        this.random = new Random();
        this.tick = 0;
        seedAgents();
    }

    public void update() {
        tick++;
        signalController.update();
        updateEvents();

        if (tick % 70 == 0 && activeEvents.size() < 3 && random.nextDouble() < 0.55) {
            spawnRandomEvent();
        }

        for (Agent agent : agents) {
            agent.update(this);
        }
    }

    public double applySpeedModifiers(Agent agent, double desiredSpeed) {
        Point2D.Double position = agent.getPosition();
        double speed = desiredSpeed;

        for (Event event : activeEvents) {
            if (event instanceof CongestionEvent congestion && congestion.isActive() && congestion.contains(position)) {
                speed *= congestion.getSlowdownFactor();
            }
        }

        return Math.max(0.2, speed);
    }

    public Grid getGrid() {
        return grid;
    }

    public SignalController getSignalController() {
        return signalController;
    }

    public List<Agent> getAgents() {
        return Collections.unmodifiableList(agents);
    }

    public List<Event> getActiveEvents() {
        return Collections.unmodifiableList(activeEvents);
    }

    public int getTick() {
        return tick;
    }

    public int getRunningCarCount() {
        int count = 0;
        for (Agent agent : agents) {
            if ("Car".equals(agent.getAgentType()) && agent.getCurrentSpeed() > 0.01) {
                count++;
            }
        }
        return count;
    }

    public int getBlockedLaneCount() {
        int count = 0;
        for (Lane lane : grid.getAllLanes()) {
            if (lane.isBlocked()) {
                count++;
            }
        }
        return count;
    }

    private void seedAgents() {
        Lane mainEast = grid.getLaneById("main-east");
        Lane mainWest = grid.getLaneById("main-west");
        Lane centralSouth = grid.getLaneById("central-south");
        Lane centralNorth = grid.getLaneById("central-north");
        Lane crossEast = grid.getLaneById("cross-east");
        Lane crossWest = grid.getLaneById("cross-west");

        agents.add(new Car("Car-1", mainEast, new NormalBehavior(), 2.0, new Color(64, 145, 255)));
        agents.add(new Car("Car-2", mainEast, new AggressiveBehavior(), 13.0, new Color(255, 120, 80)));
        agents.add(new Car("Car-3", mainWest, new NormalBehavior(), 5.0, new Color(80, 200, 140)));
        agents.add(new Car("Car-4", mainWest, new AggressiveBehavior(), 18.0, new Color(255, 210, 70)));
        agents.add(new Car("Car-5", centralSouth, new NormalBehavior(), 4.0, new Color(130, 110, 255)));
        agents.add(new Car("Car-6", centralNorth, new AggressiveBehavior(), 14.0, new Color(255, 90, 140)));
        agents.add(new Pedestrian("Ped-1", crossEast, new NormalBehavior(), 1.0, new Color(250, 250, 250)));
        agents.add(new Pedestrian("Ped-2", crossWest, new NormalBehavior(), 5.5, new Color(255, 235, 120)));
        agents.add(new Pedestrian("Ped-3", crossEast, new AggressiveBehavior(), 8.0, new Color(180, 255, 255)));
    }

    private void updateEvents() {
        Iterator<Event> iterator = activeEvents.iterator();
        while (iterator.hasNext()) {
            Event event = iterator.next();
            event.update(this);
            if (event.isExpired()) {
                event.finish(this);
                iterator.remove();
            }
        }
    }

    private void spawnRandomEvent() {
        if (random.nextBoolean()) {
            spawnAccident();
        } else {
            spawnCongestion();
        }
    }

    private void spawnAccident() {
        List<Lane> candidates = new ArrayList<>(grid.getLanesByType(LaneType.VEHICLE));
        candidates.removeIf(Lane::isBlocked);
        if (candidates.isEmpty()) {
            return;
        }

        Lane lane = candidates.get(random.nextInt(candidates.size()));
        activeEvents.add(new AccidentEvent(lane, 40 + random.nextInt(35)));
    }

    private void spawnCongestion() {
        Intersection anchor = grid.getIntersections().isEmpty() ? null : grid.getIntersections().get(0);
        int baseX = 10;
        int baseY = 8;

        if (anchor != null) {
            baseX = Math.max(2, anchor.getCenterX() - 4 + random.nextInt(3));
            baseY = Math.max(2, anchor.getCenterY() - 4 + random.nextInt(3));
        }

        activeEvents.add(new CongestionEvent(baseX, baseY, 8, 8, 45 + random.nextInt(35), 0.45));
    }
}
