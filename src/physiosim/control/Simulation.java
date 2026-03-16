// src/physiosim/control/Simulation.java
package physiosim.control;

import physiosim.event.Command;
import physiosim.sim.Core;
import physiosim.sim.OrganSystem;

import java.util.Objects;

public final class Simulation {

    private final Core core;
    private double timeSec;

    private int    heartRate;      // HR (bpm)
    private int    respRate;       // RR (breaths/min)
    private int    systolic;       // SBP
    private int    diastolic;      // DBP
    private int    map;            // MAP
    private int    spo2;           // SpO2 (%)
    private double temperature;    // 체온 (℃)

    private static final int    BASE_HR   = 75;
    private static final int    BASE_RR   = 14;
    private static final int    BASE_SBP  = 115;
    private static final int    BASE_DBP  = 75;
    private static final int    BASE_SPO2 = 98;
    private static final double BASE_TEMP = 36.6;

    private Simulation(Core core) {
        this.core = Objects.requireNonNull(core);
        timeSec = 0.0;
        setDefaultVitals();
    }

    public static Simulation createDefault() {
        return new Simulation(Core.createDefault());
    }

    private void setDefaultVitals() {
        heartRate   = BASE_HR;
        respRate    = BASE_RR;
        systolic    = BASE_SBP;
        diastolic   = BASE_DBP;
        map         = computeMap(systolic, diastolic);
        spo2        = BASE_SPO2;
        temperature = BASE_TEMP;
    }

    public void tick(double dtSec) {
        if (dtSec <= 0) return;

        timeSec += dtSec;
        core.tick(dtSec);
        recomputeVitalsFromModel();
    }

    public void sendCommand(Command cmd) {
        if (cmd == null) return;
        core.enqueueCommand(cmd);
    }

    private void recomputeVitalsFromModel() {
        OrganSystem pns = core.getSystem(OrganSystem.Type.PERIPHERAL_NERVOUS);

        double sym  = (pns != null) ? pns.getSympatheticTone()     : 0.5;
        double para = (pns != null) ? pns.getParasympatheticTone() : 0.5;

        // -1(부교감 우세) ~ +1(교감 우세)
        double net = clamp(-1.0, 1.0, sym - para);

        double volEff  = core.getBloodVolumeEffect();  // -1.0 ~ +1.0
        double tempEff = core.getTempEffect();         // -3 ~ +3 (℃)
        double oxyEff  = core.getOxygenEffect();       // -1.5 ~ +1.5
        double metEff  = core.getMetabolicEffect();    // -1.5 ~ +1.5

        // HR
        heartRate = (int) Math.round(
                BASE_HR
                        + net    * 25.0
                        + metEff * 20.0
                        - volEff * 15.0
        );
        heartRate = clampInt(heartRate, 40, 150);

        // RR
        respRate = (int) Math.round(
                BASE_RR
                        + net    * 5.0
                        + metEff * 4.0
                        - oxyEff * 4.0
        );
        respRate = clampInt(respRate, 8, 30);

        // 혈압
        int sbpBase = (int) Math.round(
                BASE_SBP
                        + net    * 10.0
                        + volEff * 45.0
        );
        int dbpBase = (int) Math.round(
                BASE_DBP
                        + net    * 5.0
                        + volEff * 25.0
        );
        systolic  = clampInt(sbpBase, 60, 220);
        diastolic = clampInt(dbpBase, 30, 140);
        map       = computeMap(systolic, diastolic);

        // SpO2
        int spoBase = (int) Math.round(
                BASE_SPO2
                        + oxyEff * 10.0
                        - metEff * 3.0
        );
        spo2 = clampInt(spoBase, 80, 100);

        // 체온
        double tBase = BASE_TEMP
                + net    * 0.3
                + tempEff
                + metEff * 0.3;
        temperature = clampDouble(tBase, 33.0, 41.0);
    }

    public void setStressLevel(double level) {
        double l = clampDouble(level, 0.0, 1.0);
        OrganSystem pns = core.getSystem(OrganSystem.Type.PERIPHERAL_NERVOUS);
        if (pns == null) return;

        double sym  = 0.3 + 0.7 * l;  // 0.3 ~ 1.0
        double para = 1.0 - 0.7 * l;  // 1.0 ~ 0.3
        pns.setSympatheticTone(sym);
        pns.setParasympatheticTone(para);
    }

    // GET
    public Core   getCore()          { return core; }
    public double getTimeSec()       { return timeSec; }

    public int    getHeartRate()     { return heartRate; }
    public int    getRespRate()      { return respRate; }
    public int    getSystolic()      { return systolic; }
    public int    getDiastolic()     { return diastolic; }
    public int    getMap()           { return map; }
    public int    getSpo2()          { return spo2; }
    public double getTemperature()   { return temperature; }

    // 유틸
    private int computeMap(int sys, int dia) {
        return (int) Math.round(dia + (sys - dia) / 3.0);
    }

    private int clampInt(int v, int lo, int hi) {
        return Math.max(lo, Math.min(hi, v));
    }

    private double clampDouble(double v, double lo, double hi) {
        return Math.max(lo, Math.min(hi, v));
    }

    private double clamp(double lo, double hi, double v) {
        if (v < lo) return lo;
        if (v > hi) return hi;
        return v;
    }
}
