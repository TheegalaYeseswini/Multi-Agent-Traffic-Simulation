package environment;

import java.awt.geom.Point2D;

public class Lane {
    public enum Direction {
        EAST,
        WEST,
        NORTH,
        SOUTH;

        public boolean isHorizontal() {
            return this == EAST || this == WEST;
        }
    }

    public enum LaneType {
        VEHICLE,
        PEDESTRIAN
    }

    private final String id;
    private final String roadName;
    private final LaneType laneType;
    private final Direction direction;
    private final int speedLimit;
    private final int startX;
    private final int startY;
    private final int endX;
    private final int endY;
    private boolean blocked;
    private String blockageReason;

    public Lane(
            String id,
            String roadName,
            LaneType laneType,
            Direction direction,
            int speedLimit,
            int startX,
            int startY,
            int endX,
            int endY) {
        this.id = id;
        this.roadName = roadName;
        this.laneType = laneType;
        this.direction = direction;
        this.speedLimit = speedLimit;
        this.startX = startX;
        this.startY = startY;
        this.endX = endX;
        this.endY = endY;
    }

    public String getId() {
        return id;
    }

    public String getRoadName() {
        return roadName;
    }

    public LaneType getLaneType() {
        return laneType;
    }

    public Direction getDirection() {
        return direction;
    }

    public int getSpeedLimit() {
        return speedLimit;
    }

    public int getStartX() {
        return startX;
    }

    public int getStartY() {
        return startY;
    }

    public int getEndX() {
        return endX;
    }

    public int getEndY() {
        return endY;
    }

    public boolean isBlocked() {
        return blocked;
    }

    public String getBlockageReason() {
        return blockageReason;
    }

    public void setBlocked(boolean blocked, String blockageReason) {
        this.blocked = blocked;
        this.blockageReason = blockageReason;
    }

    public double getLength() {
        return Math.max(Math.abs(endX - startX), Math.abs(endY - startY));
    }

    public Point2D.Double getPointAt(double progress) {
        double bounded = clamp(progress, 0.0, getLength());
        double length = Math.max(getLength(), 1.0);
        double unitX = (endX - startX) / length;
        double unitY = (endY - startY) / length;
        return new Point2D.Double(startX + unitX * bounded, startY + unitY * bounded);
    }

    public boolean crosses(Intersection intersection) {
        if (direction.isHorizontal()) {
            int minX = Math.min(startX, endX);
            int maxX = Math.max(startX, endX);
            return startY == intersection.getCenterY()
                    && intersection.getCenterX() >= minX
                    && intersection.getCenterX() <= maxX;
        }

        int minY = Math.min(startY, endY);
        int maxY = Math.max(startY, endY);
        return startX == intersection.getCenterX()
                && intersection.getCenterY() >= minY
                && intersection.getCenterY() <= maxY;
    }

    public double getProgressAtIntersection(Intersection intersection) {
        if (!crosses(intersection)) {
            return -1.0;
        }

        if (direction.isHorizontal()) {
            return Math.abs(intersection.getCenterX() - startX);
        }

        return Math.abs(intersection.getCenterY() - startY);
    }

    private double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
}
