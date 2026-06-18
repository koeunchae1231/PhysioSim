package physiosim.control;

import org.junit.jupiter.api.Test;
import physiosim.event.Command;
import physiosim.event.CommandDirection;
import physiosim.event.CommandId;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SimulationTest {

    @Test
    void createDefault_setsNormalBaselineVitals() {
        Simulation simulation = Simulation.createDefault();

        assertEquals(75, simulation.getHeartRate());
        assertEquals(14, simulation.getRespRate());
        assertEquals(115, simulation.getSystolic());
        assertEquals(75, simulation.getDiastolic());
        assertEquals(88, simulation.getMap());
        assertEquals(98, simulation.getSpo2());
        assertEquals(36.6, simulation.getTemperature(), 0.01);
    }

    @Test
    void tick_ignoresNonPositiveDelta() {
        Simulation simulation = Simulation.createDefault();

        simulation.tick(0);
        simulation.tick(-1);

        assertEquals(0.0, simulation.getTimeSec(), 0.01);
        assertEquals(75, simulation.getHeartRate());
        assertEquals(14, simulation.getRespRate());
    }

    @Test
    void setStressLevel_clampsHighBoundaryAndRecomputesVitals() {
        Simulation simulation = Simulation.createDefault();

        simulation.setStressLevel(10.0);
        simulation.tick(1.0);

        assertEquals(1.0, simulation.getTimeSec(), 0.01);
        assertEquals(93, simulation.getHeartRate());
        assertEquals(18, simulation.getRespRate());
        assertEquals(122, simulation.getSystolic());
        assertEquals(79, simulation.getDiastolic());
        assertEquals(93, simulation.getMap());
        assertEquals(98, simulation.getSpo2());
        assertEquals(36.8, simulation.getTemperature(), 0.01);
    }

    @Test
    void setStressLevel_clampsLowBoundaryAndRecomputesVitals() {
        Simulation simulation = Simulation.createDefault();

        simulation.setStressLevel(-10.0);
        simulation.tick(1.0);

        assertEquals(58, simulation.getHeartRate());
        assertEquals(11, simulation.getRespRate());
        assertEquals(108, simulation.getSystolic());
        assertEquals(72, simulation.getDiastolic());
        assertEquals(84, simulation.getMap());
        assertEquals(98, simulation.getSpo2());
        assertEquals(36.4, simulation.getTemperature(), 0.01);
    }

    @Test
    void strongBleedingCommand_keepsVitalsInsideSafetyClamps() {
        Simulation simulation = Simulation.createDefault();

        simulation.sendCommand(new Command(CommandId.BLEED, CommandDirection.NONE, 10));
        simulation.tick(1.0);

        assertTrue(simulation.getHeartRate() >= 40 && simulation.getHeartRate() <= 150);
        assertTrue(simulation.getRespRate() >= 8 && simulation.getRespRate() <= 30);
        assertTrue(simulation.getSystolic() >= 60 && simulation.getSystolic() <= 220);
        assertTrue(simulation.getDiastolic() >= 30 && simulation.getDiastolic() <= 140);
        assertTrue(simulation.getSpo2() >= 80 && simulation.getSpo2() <= 100);
        assertTrue(simulation.getTemperature() >= 33.0 && simulation.getTemperature() <= 41.0);
    }
}
