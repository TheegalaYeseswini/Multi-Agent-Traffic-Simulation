package ui;

import agents.Agent;
import engine.SimulationEngine;
import environment.Grid;
import environment.Intersection;
import environment.Lane;
import environment.Road;
import events.AccidentEvent;
import events.CongestionEvent;
import events.Event;
import traffic.TrafficSignal;
import traffic.TrafficSignal.SignalState;

import javax.swing.JPanel;
import javax.swing.Timer;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Point2D;
import java.awt.geom.RoundRectangle2D;

public class SimulationPanel extends JPanel {
    private static final int CELL_SIZE = 24;
    private static final int WORLD_MARGIN = 30;
    private static final int HUD_WIDTH = 260;

    private final SimulationEngine engine;
    private final Timer timer;

    public SimulationPanel(SimulationEngine engine) {
        this.engine = engine;
        Grid grid = engine.getGrid();
        int width = (grid.getWidth() * CELL_SIZE) + HUD_WIDTH + (WORLD_MARGIN * 3);
        int height = (grid.getHeight() * CELL_SIZE) + (WORLD_MARGIN * 2);

        setPreferredSize(new Dimension(width, height));
        setBackground(new Color(14, 20, 26));

        this.timer = new Timer(120, e -> {
            this.engine.update();
            repaint();
        });
        this.timer.start();
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        Graphics2D g2 = (Graphics2D) graphics.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        paintBackground(g2);
        drawWorld(g2);
        drawHud(g2);

        g2.dispose();
    }

    private void paintBackground(Graphics2D g2) {
        GradientPaint paint = new GradientPaint(
                0, 0, new Color(18, 30, 38),
                getWidth(), getHeight(), new Color(7, 12, 17));
        g2.setPaint(paint);
        g2.fillRect(0, 0, getWidth(), getHeight());
    }

    private void drawWorld(Graphics2D g2) {
        drawRoads(g2);
        drawIntersection(g2);
        drawEvents(g2);
        drawSignals(g2);
        drawAgents(g2);
    }

    private void drawRoads(Graphics2D g2) {
        for (Road road : engine.getGrid().getRoads()) {
            for (Lane lane : road.getLanes()) {
                int thickness = lane.getLaneType() == Lane.LaneType.VEHICLE ? 20 : 10;
                int startX = worldX(lane.getStartX());
                int startY = worldY(lane.getStartY());
                int endX = worldX(lane.getEndX());
                int endY = worldY(lane.getEndY());

                g2.setColor(lane.getLaneType() == Lane.LaneType.VEHICLE
                        ? new Color(55, 60, 68)
                        : new Color(90, 105, 112));
                g2.setStroke(new BasicStroke(thickness, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.drawLine(startX, startY, endX, endY);

                g2.setColor(new Color(232, 232, 210, 150));
                g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND, 1f, new float[]{8f, 8f}, 0f));
                g2.drawLine(startX, startY, endX, endY);
            }
        }
    }

    private void drawIntersection(Graphics2D g2) {
        for (Intersection intersection : engine.getGrid().getIntersections()) {
            int size = intersection.getSize() * CELL_SIZE;
            int x = worldX(intersection.getCenterX()) - (size / 2);
            int y = worldY(intersection.getCenterY()) - (size / 2);

            g2.setColor(new Color(78, 84, 94));
            g2.fillRoundRect(x, y, size, size, 18, 18);

            g2.setColor(new Color(255, 255, 255, 28));
            g2.setStroke(new BasicStroke(2f));
            g2.drawRoundRect(x, y, size, size, 18, 18);
        }
    }

    private void drawEvents(Graphics2D g2) {
        for (Event event : engine.getActiveEvents()) {
            if (event instanceof AccidentEvent accident) {
                Lane lane = accident.getAffectedLane();
                g2.setColor(new Color(255, 86, 86, 170));
                g2.setStroke(new BasicStroke(10f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.drawLine(worldX(lane.getStartX()), worldY(lane.getStartY()), worldX(lane.getEndX()), worldY(lane.getEndY()));
            }

            if (event instanceof CongestionEvent congestion) {
                int x = worldX(congestion.getStartX());
                int y = worldY(congestion.getStartY());
                int width = congestion.getWidth() * CELL_SIZE;
                int height = congestion.getHeight() * CELL_SIZE;

                g2.setColor(new Color(255, 170, 60, 75));
                g2.fillRoundRect(x, y, width, height, 18, 18);
                g2.setColor(new Color(255, 180, 85, 150));
                g2.setStroke(new BasicStroke(2f));
                g2.drawRoundRect(x, y, width, height, 18, 18);
            }
        }
    }

    private void drawSignals(Graphics2D g2) {
        for (Intersection intersection : engine.getGrid().getIntersections()) {
            drawSignalLamp(g2, worldX(intersection.getCenterX()) - 18, worldY(intersection.getCenterY()) - 36, intersection.getHorizontalSignal());
            drawSignalLamp(g2, worldX(intersection.getCenterX()) + 6, worldY(intersection.getCenterY()) + 22, intersection.getHorizontalSignal());
            drawSignalLamp(g2, worldX(intersection.getCenterX()) - 36, worldY(intersection.getCenterY()) + 4, intersection.getVerticalSignal());
            drawSignalLamp(g2, worldX(intersection.getCenterX()) + 22, worldY(intersection.getCenterY()) - 20, intersection.getVerticalSignal());
        }
    }

    private void drawSignalLamp(Graphics2D g2, int x, int y, TrafficSignal signal) {
        g2.setColor(new Color(20, 24, 28));
        g2.fillRoundRect(x, y, 16, 28, 10, 10);

        Color lampColor = switch (signal.getState()) {
            case GREEN -> new Color(70, 240, 110);
            case YELLOW -> new Color(255, 214, 70);
            case RED -> new Color(255, 80, 80);
        };

        g2.setColor(lampColor);
        g2.fillOval(x + 3, y + 6, 10, 10);
    }

    private void drawAgents(Graphics2D g2) {
        for (Agent agent : engine.getAgents()) {
            Point2D.Double point = agent.getPosition();
            int x = worldX(point.x);
            int y = worldY(point.y);

            g2.setColor(agent.getColor());
            if (agent.isCircular()) {
                int diameter = agent.getRenderSize();
                g2.fillOval(x - (diameter / 2), y - (diameter / 2), diameter, diameter);
            } else {
                int width = agent.getLane().getDirection().isHorizontal() ? agent.getRenderSize() + 8 : agent.getRenderSize() - 2;
                int height = agent.getLane().getDirection().isHorizontal() ? agent.getRenderSize() - 4 : agent.getRenderSize() + 8;
                RoundRectangle2D.Double shape = new RoundRectangle2D.Double(
                        x - (width / 2.0),
                        y - (height / 2.0),
                        width,
                        height,
                        8,
                        8);
                g2.fill(shape);
            }

            g2.setColor(new Color(255, 255, 255, 210));
            g2.setFont(new Font("SansSerif", Font.PLAIN, 10));
            g2.drawString(String.valueOf(agent.getId()), x + 8, y - 8);
        }
    }

    private void drawHud(Graphics2D g2) {
        int x = WORLD_MARGIN + (engine.getGrid().getWidth() * CELL_SIZE) + WORLD_MARGIN;
        int y = WORLD_MARGIN;
        int width = HUD_WIDTH;
        int height = getHeight() - (WORLD_MARGIN * 2);

        g2.setColor(new Color(9, 14, 19, 210));
        g2.fillRoundRect(x, y, width, height, 24, 24);
        g2.setColor(new Color(255, 255, 255, 30));
        g2.drawRoundRect(x, y, width, height, 24, 24);

        g2.setColor(new Color(245, 246, 248));
        g2.setFont(new Font("SansSerif", Font.BOLD, 22));
        g2.drawString("Simulation", x + 20, y + 36);

        g2.setFont(new Font("SansSerif", Font.PLAIN, 13));
        g2.setColor(new Color(190, 206, 214));
        g2.drawString("Tick: " + engine.getTick(), x + 20, y + 72);
        g2.drawString("Agents: " + engine.getAgents().size(), x + 20, y + 96);
        g2.drawString("Moving Cars: " + engine.getRunningCarCount(), x + 20, y + 120);
        g2.drawString("Blocked Lanes: " + engine.getBlockedLaneCount(), x + 20, y + 144);

        drawLegend(g2, x + 20, y + 188);
        drawEventFeed(g2, x + 20, y + 320);
    }

    private void drawLegend(Graphics2D g2, int x, int y) {
        g2.setFont(new Font("SansSerif", Font.BOLD, 14));
        g2.setColor(new Color(245, 246, 248));
        g2.drawString("Legend", x, y);

        drawLegendItem(g2, x, y + 26, new Color(64, 145, 255), "Normal car");
        drawLegendItem(g2, x, y + 52, new Color(255, 120, 80), "Aggressive car");
        drawLegendItem(g2, x, y + 78, new Color(250, 250, 250), "Pedestrian");
        drawLegendItem(g2, x, y + 104, new Color(255, 86, 86), "Accident block");
        drawLegendItem(g2, x, y + 130, new Color(255, 170, 60), "Congestion zone");
    }

    private void drawLegendItem(Graphics2D g2, int x, int y, Color color, String label) {
        g2.setColor(color);
        g2.fillRoundRect(x, y - 10, 18, 12, 6, 6);
        g2.setColor(new Color(190, 206, 214));
        g2.setFont(new Font("SansSerif", Font.PLAIN, 12));
        g2.drawString(label, x + 28, y);
    }

    private void drawEventFeed(Graphics2D g2, int x, int y) {
        g2.setFont(new Font("SansSerif", Font.BOLD, 14));
        g2.setColor(new Color(245, 246, 248));
        g2.drawString("Active Events", x, y);

        int lineY = y + 24;
        if (engine.getActiveEvents().isEmpty()) {
            g2.setFont(new Font("SansSerif", Font.PLAIN, 12));
            g2.setColor(new Color(160, 176, 185));
            g2.drawString("No disruptions right now.", x, lineY);
            return;
        }

        for (Event event : engine.getActiveEvents()) {
            g2.setColor(colorForEvent(event));
            g2.fillRoundRect(x, lineY - 11, 12, 12, 4, 4);

            g2.setColor(new Color(220, 228, 232));
            g2.setFont(new Font("SansSerif", Font.PLAIN, 12));
            g2.drawString(event.getName() + " (" + event.getRemainingTicks() + "t)", x + 20, lineY);

            g2.setColor(new Color(150, 166, 175));
            g2.drawString(event.getDescription(), x + 20, lineY + 16);
            lineY += 40;
        }
    }

    private Color colorForEvent(Event event) {
        if (event instanceof AccidentEvent) {
            return new Color(255, 86, 86);
        }
        if (event instanceof CongestionEvent) {
            return new Color(255, 170, 60);
        }
        return new Color(180, 180, 180);
    }

    private int worldX(double gridX) {
        return WORLD_MARGIN + (int) Math.round(gridX * CELL_SIZE);
    }

    private int worldY(double gridY) {
        return WORLD_MARGIN + (int) Math.round(gridY * CELL_SIZE);
    }
}
