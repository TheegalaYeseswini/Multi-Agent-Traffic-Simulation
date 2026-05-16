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
import java.awt.FontMetrics;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.geom.Point2D;
import java.awt.geom.RoundRectangle2D;

public class SimulationPanel extends JPanel {
    private static final int CELL_SIZE = 24;
    private static final int WORLD_MARGIN = 30;
    private static final int HUD_WIDTH = 320;
    private static final Font TITLE_FONT = new Font("SansSerif", Font.BOLD, 22);
    private static final Font SECTION_FONT = new Font("SansSerif", Font.BOLD, 14);
    private static final Font BODY_FONT = new Font("SansSerif", Font.PLAIN, 12);
    private static final Font SMALL_FONT = new Font("SansSerif", Font.PLAIN, 11);

    private final SimulationEngine engine;
    private final Timer timer;

    public SimulationPanel(SimulationEngine engine) {
        this.engine = engine;
        Grid grid = engine.getGrid();
        int width = (grid.getWidth() * CELL_SIZE) + HUD_WIDTH + (WORLD_MARGIN * 3);
        int height = (grid.getHeight() * CELL_SIZE) + (WORLD_MARGIN * 2) + 90;

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
        drawStopLines(g2);
        drawIntersection(g2);
        drawEvents(g2);
        drawSignals(g2);
        drawRoadLabels(g2);
        drawLiveStatusBanner(g2);
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

    private void drawStopLines(Graphics2D g2) {
        Stroke previousStroke = g2.getStroke();
        for (Intersection intersection : engine.getGrid().getIntersections()) {
            Color horizontal = colorForSignal(intersection.getHorizontalSignal().getState());
            Color vertical = colorForSignal(intersection.getVerticalSignal().getState());

            int minX = worldX(intersection.getMinX());
            int maxX = worldX(intersection.getMaxX());
            int minY = worldY(intersection.getMinY());
            int maxY = worldY(intersection.getMaxY());

            g2.setStroke(new BasicStroke(4f));

            g2.setColor(horizontal);
            g2.drawLine(minX - 2, worldY(10) - 14, minX - 2, worldY(10) + 14);
            g2.drawLine(maxX + 2, worldY(13) - 14, maxX + 2, worldY(13) + 14);

            g2.setColor(vertical);
            g2.drawLine(worldX(17) - 14, minY - 2, worldX(17) + 14, minY - 2);
            g2.drawLine(worldX(14) - 14, maxY + 2, worldX(14) + 14, maxY + 2);
        }
        g2.setStroke(previousStroke);
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

            g2.setColor(new Color(255, 255, 255, 55));
            g2.setFont(new Font("SansSerif", Font.BOLD, 11));
            g2.drawString("JUNCTION", x + 10, y + 20);
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

    private void drawRoadLabels(Graphics2D g2) {
        drawLabelChip(g2, worldX(6.2), worldY(6.1), "Main Street", new Color(33, 44, 54, 225));
        drawLabelChip(g2, worldX(18.6), worldY(3.4), "Central Avenue", new Color(33, 44, 54, 225));
        drawLabelChip(g2, worldX(9.3), worldY(16.2), "Crosswalk", new Color(33, 60, 68, 225));

        g2.setColor(new Color(146, 165, 176));
        g2.setFont(SMALL_FONT);
        g2.drawString("Eastbound", worldX(2.0), worldY(9.0));
        g2.drawString("Westbound", worldX(23.2), worldY(15.4));
        g2.drawString("Southbound", worldX(18.6), worldY(4.8));
        g2.drawString("Northbound", worldX(16.1), worldY(24.0));
    }

    private void drawLabelChip(Graphics2D g2, int x, int y, String label, Color background) {
        FontMetrics metrics = g2.getFontMetrics(SECTION_FONT);
        int width = metrics.stringWidth(label) + 20;

        g2.setColor(background);
        g2.fillRoundRect(x, y, width, 24, 14, 14);
        g2.setColor(new Color(255, 255, 255, 28));
        g2.drawRoundRect(x, y, width, 24, 14, 14);

        g2.setColor(new Color(245, 246, 248));
        g2.setFont(SECTION_FONT);
        g2.drawString(label, x + 10, y + 17);
    }

    private void drawLiveStatusBanner(Graphics2D g2) {
        int x = WORLD_MARGIN;
        int y = WORLD_MARGIN;
        int width = 300;
        int height = 86;

        g2.setColor(new Color(9, 14, 19, 220));
        g2.fillRoundRect(x, y, width, height, 22, 22);
        g2.setColor(new Color(255, 255, 255, 25));
        g2.drawRoundRect(x, y, width, height, 22, 22);

        g2.setColor(new Color(245, 246, 248));
        g2.setFont(SECTION_FONT);
        g2.drawString("What is happening now", x + 16, y + 24);

        Intersection intersection = engine.getGrid().getIntersections().get(0);
        drawStatusLine(g2, x + 16, y + 48, "Main Street", intersection.getHorizontalSignal().getState());
        drawStatusLine(g2, x + 16, y + 72, "Central Avenue", intersection.getVerticalSignal().getState());
    }

    private void drawStatusLine(Graphics2D g2, int x, int y, String roadLabel, SignalState state) {
        String action = state == SignalState.GREEN ? "GO" : state == SignalState.YELLOW ? "SLOW / CLEAR" : "STOP";
        Color actionColor = colorForSignal(state);

        g2.setColor(new Color(196, 210, 218));
        g2.setFont(BODY_FONT);
        g2.drawString(roadLabel, x, y);

        g2.setColor(actionColor);
        g2.setFont(new Font("SansSerif", Font.BOLD, 12));
        g2.fillRoundRect(x + 140, y - 13, 86, 18, 9, 9);
        g2.setColor(new Color(18, 24, 28));
        g2.drawString(action, x + 170, y + 1);
    }

    private void drawSignalLamp(Graphics2D g2, int x, int y, TrafficSignal signal) {
        g2.setColor(new Color(20, 24, 28));
        g2.fillRoundRect(x, y, 16, 28, 10, 10);

        Color lampColor = colorForSignal(signal.getState());

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
        g2.setFont(TITLE_FONT);
        g2.drawString("Traffic Control", x + 20, y + 36);

        g2.setFont(BODY_FONT);
        g2.setColor(new Color(190, 206, 214));
        g2.drawString("Signal phases, incidents, and lane activity", x + 20, y + 60);

        drawMetricCard(g2, x + 20, y + 82, 130, 58, "Tick", String.valueOf(engine.getTick()));
        drawMetricCard(g2, x + 170, y + 82, 130, 58, "Agents", String.valueOf(engine.getAgents().size()));
        drawMetricCard(g2, x + 20, y + 150, 130, 58, "Moving Cars", String.valueOf(engine.getRunningCarCount()));
        drawMetricCard(g2, x + 170, y + 150, 130, 58, "Blocked Lanes", String.valueOf(engine.getBlockedLaneCount()));

        drawSignalSummary(g2, x + 20, y + 236);
        drawSectionDivider(g2, x + 20, y + 372, width - 40);
        drawLegend(g2, x + 20, y + 398);
        drawSectionDivider(g2, x + 20, y + 540, width - 40);
        drawEventFeed(g2, x + 20, y + 566);
    }

    private void drawLegend(Graphics2D g2, int x, int y) {
        g2.setFont(SECTION_FONT);
        g2.setColor(new Color(245, 246, 248));
        g2.drawString("Legend", x, y);

        drawLegendItem(g2, x, y + 26, new Color(64, 145, 255), "Normal car");
        drawLegendItem(g2, x, y + 52, new Color(255, 120, 80), "Aggressive car");
        drawLegendItem(g2, x, y + 78, new Color(250, 250, 250), "Pedestrian");
        drawLegendItem(g2, x, y + 104, new Color(255, 86, 86), "Stop marker / incident");
        drawLegendItem(g2, x, y + 130, new Color(255, 170, 60), "Slow traffic zone");
    }

    private void drawLegendItem(Graphics2D g2, int x, int y, Color color, String label) {
        g2.setColor(color);
        g2.fillRoundRect(x, y - 10, 18, 12, 6, 6);
        g2.setColor(new Color(190, 206, 214));
        g2.setFont(BODY_FONT);
        g2.drawString(label, x + 28, y);
    }

    private void drawMetricCard(Graphics2D g2, int x, int y, int width, int height, String label, String value) {
        g2.setColor(new Color(18, 28, 36, 220));
        g2.fillRoundRect(x, y, width, height, 16, 16);
        g2.setColor(new Color(255, 255, 255, 28));
        g2.drawRoundRect(x, y, width, height, 16, 16);

        g2.setColor(new Color(154, 178, 190));
        g2.setFont(SMALL_FONT);
        g2.drawString(label, x + 12, y + 20);

        g2.setColor(new Color(245, 246, 248));
        g2.setFont(new Font("SansSerif", Font.BOLD, 22));
        g2.drawString(value, x + 12, y + 46);
    }

    private void drawSignalSummary(Graphics2D g2, int x, int y) {
        g2.setFont(SECTION_FONT);
        g2.setColor(new Color(245, 246, 248));
        g2.drawString("Signal Status", x, y);

        g2.setColor(new Color(145, 164, 175));
        g2.setFont(SMALL_FONT);
        g2.drawString("These labels match the roads on the map.", x, y + 18);

        int rowY = y + 24;
        for (Intersection intersection : engine.getGrid().getIntersections()) {
            drawSignalStatusRow(g2, x, rowY + 12, "Main Street", intersection.getHorizontalSignal().getState(), "east/west");
            rowY += 42;
            drawSignalStatusRow(g2, x, rowY + 12, "Central Avenue", intersection.getVerticalSignal().getState(), "north/south");
        }
    }

    private void drawSignalStatusRow(Graphics2D g2, int x, int y, String label, SignalState state, String directions) {
        g2.setColor(new Color(190, 206, 214));
        g2.setFont(BODY_FONT);
        g2.drawString(label, x, y);

        g2.setColor(new Color(130, 147, 157));
        g2.setFont(SMALL_FONT);
        g2.drawString("(" + directions + ")", x, y + 14);

        Color stateColor = colorForSignal(state);
        String stateText = state == SignalState.GREEN ? "GO NOW" : state == SignalState.YELLOW ? "CLEAR" : "STOP";

        g2.setColor(stateColor);
        g2.fillRoundRect(x + 168, y - 11, 96, 20, 10, 10);
        g2.setColor(new Color(18, 24, 28));
        g2.setFont(new Font("SansSerif", Font.BOLD, 11));
        g2.drawString(stateText, x + 194, y + 3);
    }

    private void drawEventFeed(Graphics2D g2, int x, int y) {
        g2.setFont(SECTION_FONT);
        g2.setColor(new Color(245, 246, 248));
        g2.drawString("Active Events", x, y);

        int lineY = y + 24;
        if (engine.getActiveEvents().isEmpty()) {
            g2.setFont(BODY_FONT);
            g2.setColor(new Color(160, 176, 185));
            g2.drawString("No disruptions right now.", x, lineY);
            return;
        }

        for (Event event : engine.getActiveEvents()) {
            if (lineY > getHeight() - 70) {
                g2.setColor(new Color(160, 176, 185));
                g2.setFont(new Font("SansSerif", Font.ITALIC, 12));
                g2.drawString("More events active...", x, lineY);
                return;
            }

            g2.setColor(colorForEvent(event));
            g2.fillRoundRect(x, lineY - 11, 12, 12, 4, 4);

            g2.setColor(new Color(220, 228, 232));
            g2.setFont(BODY_FONT);
            g2.drawString(event.getName() + " (" + event.getRemainingTicks() + "t)", x + 20, lineY);

            g2.setColor(new Color(150, 166, 175));
            lineY = drawWrappedText(g2, event.getDescription(), x + 20, lineY + 16, HUD_WIDTH - 52, 14, 3) + 14;
        }
    }

    private void drawSectionDivider(Graphics2D g2, int x, int y, int width) {
        g2.setColor(new Color(255, 255, 255, 18));
        g2.drawLine(x, y, x + width, y);
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

    private Color colorForSignal(SignalState state) {
        return switch (state) {
            case GREEN -> new Color(70, 240, 110);
            case YELLOW -> new Color(255, 214, 70);
            case RED -> new Color(255, 80, 80);
        };
    }

    private int worldX(double gridX) {
        return WORLD_MARGIN + (int) Math.round(gridX * CELL_SIZE);
    }

    private int worldY(double gridY) {
        return WORLD_MARGIN + (int) Math.round(gridY * CELL_SIZE);
    }

    private int drawWrappedText(Graphics2D g2, String text, int x, int y, int maxWidth, int lineHeight, int maxLines) {
        FontMetrics metrics = g2.getFontMetrics();
        String[] words = text.split(" ");
        StringBuilder currentLine = new StringBuilder();
        int currentY = y;
        int linesDrawn = 0;

        for (String word : words) {
            String candidate = currentLine.length() == 0 ? word : currentLine + " " + word;
            if (metrics.stringWidth(candidate) <= maxWidth) {
                currentLine.setLength(0);
                currentLine.append(candidate);
                continue;
            }

            g2.drawString(currentLine.toString(), x, currentY);
            linesDrawn++;
            if (linesDrawn >= maxLines) {
                return currentY;
            }

            currentY += lineHeight;
            currentLine.setLength(0);
            currentLine.append(word);
        }

        if (currentLine.length() > 0 && linesDrawn < maxLines) {
            g2.drawString(currentLine.toString(), x, currentY);
        }

        return currentY;
    }
}
