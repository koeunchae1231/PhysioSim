package physiosim.event;

public final class PhysioEvent {
    private final TargetSystem target;
    private final String type;   // "INFUSE", "BLEED", "FLUID_UP" 같은 태그
    private final double magnitude;   // 강도/양
    private final double durationSec; // 0이면 즉시형

    public PhysioEvent(TargetSystem target, String type,
                       double magnitude, double durationSec) {
        this.target = target;
        this.type = type;
        this.magnitude = magnitude;
        this.durationSec = durationSec;
    }

    public TargetSystem getTarget() { return target; }
    public String getType() { return type; }
    public double getMagnitude() { return magnitude; }
    public double getDurationSec() { return durationSec; }
}
