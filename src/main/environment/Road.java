package environment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Road {
    private final String name;
    private final List<Lane> lanes;

    public Road(String name) {
        this.name = name;
        this.lanes = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public void addLane(Lane lane) {
        lanes.add(lane);
    }

    public List<Lane> getLanes() {
        return Collections.unmodifiableList(lanes);
    }
}
