package physiosim.sim;

import physiosim.event.PhysioEvent;
import physiosim.event.TargetSystem;

public interface EventConsumer {
    boolean canHandle(TargetSystem target);
    void applyEvent(PhysioEvent event, double dtSec);
}
