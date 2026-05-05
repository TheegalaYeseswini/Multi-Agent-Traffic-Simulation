package traffic;

public class TrafficSignal {
    public enum SignalState {
        RED,
        GREEN,
        YELLOW
    }

    private final String id;
    private SignalState state;

    public TrafficSignal(String id) {
        this.id = id;
        this.state = SignalState.RED;
    }

    public String getId() {
        return id;
    }

    public SignalState getState() {
        return state;
    }

    public void setState(SignalState state) {
        this.state = state;
    }
}
