package traffic;

import environment.Intersection;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SignalController {
    private final List<Intersection> intersections;
    private final int greenDuration;
    private final int yellowDuration;
    private int phaseTick;

    public SignalController(List<Intersection> intersections) {
        this(intersections, 36, 10);
    }

    public SignalController(List<Intersection> intersections, int greenDuration, int yellowDuration) {
        this.intersections = new ArrayList<>(intersections);
        this.greenDuration = greenDuration;
        this.yellowDuration = yellowDuration;
        this.phaseTick = 0;
        refreshSignals();
    }

    public void update() {
        phaseTick++;
        refreshSignals();
    }

    public List<Intersection> getIntersections() {
        return Collections.unmodifiableList(intersections);
    }

    public int getGreenDuration() {
        return greenDuration;
    }

    public int getYellowDuration() {
        return yellowDuration;
    }

    private void refreshSignals() {
        for (Intersection intersection : intersections) {
            intersection.updateSignals(phaseTick, greenDuration, yellowDuration);
        }
    }
}
