package physiosim.event;

import java.util.ArrayList;
import java.util.List;

public final class CommandMapper {

    public List<PhysioEvent> toEvents(Command cmd) {
        List<PhysioEvent> list = new ArrayList<>();
        CommandDirection dir = cmd.getDir();
        int level = cmd.getLevel();

        switch (cmd.getId()) {
        
        // [USE]
            case INFUSE -> list.add(new PhysioEvent(
                    TargetSystem.CIRCULATION,
                    "INFUSE",
                    level,
                    0.0
            ));

            case BLEED -> list.add(new PhysioEvent(
                    TargetSystem.CIRCULATION,
                    "BLEED",
                    level,
                    0.0
            ));

            case DIURETIC -> list.add(new PhysioEvent(
                    TargetSystem.RENAL,
                    "DIURETIC",
                    level,
                    0.0
            ));

            case COLD -> list.add(new PhysioEvent(
                    TargetSystem.THERMOREGULATION,
                    "COLD",
                    level,
                    10.0
            ));

            case HOT -> list.add(new PhysioEvent(
                    TargetSystem.THERMOREGULATION,
                    "HOT",
                    level,
                    10.0
            ));

            // [UP/DOWN]
            case FLUID -> {
                String type = (dir == CommandDirection.UP) ? "FLUID_UP" : "FLUID_DOWN";
                list.add(new PhysioEvent(
                        TargetSystem.CIRCULATION,
                        type,
                        level,
                        30.0
                ));
            }

            case SALT -> {
                String type = (dir == CommandDirection.UP) ? "SALT_UP" : "SALT_DOWN";
                list.add(new PhysioEvent(
                        TargetSystem.RENAL,
                        type,
                        level,
                        60.0
                ));
            }

            case SUGAR -> {
                String type = (dir == CommandDirection.UP) ? "SUGAR_UP" : "SUGAR_DOWN";
                list.add(new PhysioEvent(
                        TargetSystem.METABOLIC,
                        type,
                        level,
                        30.0
                ));
            }

            case OXYGEN -> {
                String type = (dir == CommandDirection.UP) ? "OXYGEN_UP" : "OXYGEN_DOWN";
                list.add(new PhysioEvent(
                        TargetSystem.RESPIRATION,
                        type,
                        level,
                        10.0
                ));
            }

            case ACTIVITY -> {
                String type = (dir == CommandDirection.UP) ? "ACTIVITY_UP" : "ACTIVITY_DOWN";
                list.add(new PhysioEvent(
                        TargetSystem.METABOLIC,
                        type,
                        level,
                        30.0
                ));
            }

            case METABOLISM -> {
                String type = (dir == CommandDirection.UP) ? "METABOLISM_UP" : "METABOLISM_DOWN";
                list.add(new PhysioEvent(
                        TargetSystem.METABOLIC,
                        type,
                        level,
                        30.0
                ));
            }

            default -> {
            }
        }

        return list;
    }
}
