package environment;

import environment.Lane.Direction;
import environment.Lane.LaneType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Grid {
    private final int width;
    private final int height;
    private final List<Road> roads;
    private final List<Intersection> intersections;

    public Grid(int width, int height, List<Road> roads, List<Intersection> intersections) {
        this.width = width;
        this.height = height;
        this.roads = roads;
        this.intersections = intersections;
    }

    public static Grid createDefaultGrid() {
        List<Road> roads = new ArrayList<>();
        List<Intersection> intersections = new ArrayList<>();

        Road mainStreet = new Road("Main Street");
        mainStreet.addLane(new Lane("main-east", mainStreet.getName(), LaneType.VEHICLE, Direction.EAST, 2, 0, 10, 29, 10));
        mainStreet.addLane(new Lane("main-west", mainStreet.getName(), LaneType.VEHICLE, Direction.WEST, 2, 29, 13, 0, 13));
        roads.add(mainStreet);

        Road centralAvenue = new Road("Central Avenue");
        centralAvenue.addLane(new Lane("central-south", centralAvenue.getName(), LaneType.VEHICLE, Direction.SOUTH, 2, 17, 0, 17, 23));
        centralAvenue.addLane(new Lane("central-north", centralAvenue.getName(), LaneType.VEHICLE, Direction.NORTH, 2, 14, 23, 14, 0));
        roads.add(centralAvenue);

        Road crosswalk = new Road("Crosswalk");
        crosswalk.addLane(new Lane("cross-east", crosswalk.getName(), LaneType.PEDESTRIAN, Direction.EAST, 1, 8, 12, 22, 12));
        crosswalk.addLane(new Lane("cross-west", crosswalk.getName(), LaneType.PEDESTRIAN, Direction.WEST, 1, 22, 11, 8, 11));
        roads.add(crosswalk);

        intersections.add(new Intersection("central-intersection", 15, 12, 4));

        return new Grid(30, 24, roads, intersections);
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public List<Road> getRoads() {
        return Collections.unmodifiableList(roads);
    }

    public List<Intersection> getIntersections() {
        return Collections.unmodifiableList(intersections);
    }

    public List<Lane> getAllLanes() {
        List<Lane> lanes = new ArrayList<>();
        for (Road road : roads) {
            lanes.addAll(road.getLanes());
        }
        return lanes;
    }

    public List<Lane> getLanesByType(LaneType laneType) {
        List<Lane> lanes = new ArrayList<>();
        for (Lane lane : getAllLanes()) {
            if (lane.getLaneType() == laneType) {
                lanes.add(lane);
            }
        }
        return lanes;
    }

    public Lane getLaneById(String laneId) {
        for (Lane lane : getAllLanes()) {
            if (lane.getId().equals(laneId)) {
                return lane;
            }
        }
        return null;
    }

    public List<Intersection> getIntersectionsForLane(Lane lane) {
        List<Intersection> matches = new ArrayList<>();
        for (Intersection intersection : intersections) {
            if (lane.crosses(intersection)) {
                matches.add(intersection);
            }
        }
        return matches;
    }
}
