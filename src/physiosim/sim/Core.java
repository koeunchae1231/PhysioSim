// src/physiosim/sim/Core.java
package physiosim.sim;

import physiosim.event.Command;
import physiosim.event.CommandMapper;
import physiosim.event.PhysioEvent;
import physiosim.event.TargetSystem;

import java.util.*;

public class Core {

    // ===== 기관 / 기관계 =====
    private final List<Organ>       organs;
    private final List<OrganSystem> systems;
    private final Map<Organ.Type, Organ>             organMap;
    private final Map<OrganSystem.Type, OrganSystem> systemMap;

    // ===== 이벤트 처리 =====
    private final Queue<PhysioEvent> eventQueue = new ArrayDeque<>();
    private final CommandMapper      commandMapper = new CommandMapper();

    // ===== 생리 효과 (Simulation에서 사용) =====
    // 대략 -1.0 ~ +1.0 범위
    private double bloodVolumeEffect;   // 혈액량/순환계 (BP)
    private double tempEffect;          // 체온 변화 (℃)
    private double oxygenEffect;        // 산소 상태 (SpO₂/호흡)
    private double metabolicEffect;     // 대사/활동 (HR/RR/TEMP)

    private Core(List<Organ> organs, List<OrganSystem> systems) {
        this.organs  = Collections.unmodifiableList(new ArrayList<>(organs));
        this.systems = Collections.unmodifiableList(new ArrayList<>(systems));

        Map<Organ.Type, Organ> om = new EnumMap<>(Organ.Type.class);
        for (Organ o : this.organs) om.put(o.getType(), o);
        organMap = Collections.unmodifiableMap(om);

        Map<OrganSystem.Type, OrganSystem> sm = new EnumMap<>(OrganSystem.Type.class);
        for (OrganSystem s : this.systems) sm.put(s.getType(), s);
        systemMap = Collections.unmodifiableMap(sm);
    }

    public static Core createDefault() {
        // 기관
        List<Organ> organs = new ArrayList<>(Arrays.asList(
                Organ.heart(),
                Organ.blood(),
                Organ.vessel(),
                Organ.lung(),
                Organ.kidney(),
                Organ.stomach(),
                Organ.smallIntestine(),
                Organ.skeletalMuscle(),
                Organ.adrenalMedulla(),
                Organ.pancreas(),
                Organ.thyroid(),
                Organ.brain(),
                Organ.spinalCord()
        ));

        // 기관계
        OrganSystem cns = OrganSystem.centralNervous(
                organs.stream().filter(o -> o.getType() == Organ.Type.BRAIN).findFirst().orElse(null),
                organs.stream().filter(o -> o.getType() == Organ.Type.SPINAL_CORD).findFirst().orElse(null)
        );
        OrganSystem pns = OrganSystem.peripheralNervous();

        List<OrganSystem> systems = new ArrayList<>(Arrays.asList(cns, pns));

        return new Core(organs, systems);
    }

    // ===== 조회 =====
    public List<Organ>       getOrgans()                    { return organs; }
    public List<OrganSystem> getSystems()                   { return systems; }
    public Organ             getOrgan(Organ.Type type)      { return organMap.get(type); }
    public OrganSystem       getSystem(OrganSystem.Type t ) { return systemMap.get(t); }

    // ===== 명령 → 이벤트 큐 =====
    public void enqueueCommand(Command cmd) {
        if (cmd == null) return;
        List<PhysioEvent> events = commandMapper.toEvents(cmd);
        eventQueue.addAll(events);
    }

    // ===== 시뮬레이션 한 틱 =====
    public void tick(double dtSec) {
        if (dtSec <= 0) return;

        // 1) 큐에 쌓인 이벤트 처리
        while (!eventQueue.isEmpty()) {
            PhysioEvent ev = eventQueue.poll();
            dispatchEvent(ev, dtSec);
        }

        // 2) 생리 효과값 서서히 감소 (half-life ~30초)
        decayEffects(dtSec);

        // 3) TODO: Organ / OrganSystem 자연 경과 업데이트
    }

    // ===== 이벤트 라우팅 =====
    private void dispatchEvent(PhysioEvent ev, double dtSec) {
        if (ev == null) return;
        TargetSystem target = ev.getTarget();
        switch (target) {
            case CIRCULATION      -> applyCirculationEvent(ev, dtSec);
            case RENAL            -> applyRenalEvent(ev, dtSec);
            case RESPIRATION      -> applyRespirationEvent(ev, dtSec);
            case THERMOREGULATION -> applyThermoEvent(ev, dtSec);
            case METABOLIC        -> applyMetabolicEvent(ev, dtSec);
        }
    }

    /* ================== 각 시스템별 이벤트 처리 ================== */

    private void applyCirculationEvent(PhysioEvent ev, double dtSec) {
        String type = ev.getType();
        double mag  = ev.getMagnitude(); // 주로 1~3

        switch (type) {
            case "INFUSE"      -> adjustBloodVolume(+0.4 * mag); // 수액: 혈액량 ↑↑
            case "BLEED"       -> adjustBloodVolume(-0.4 * mag); // 출혈: 혈액량 ↓↓
            case "FLUID_UP"    -> adjustBloodVolume(+0.2 * mag); // 체액 증가
            case "FLUID_DOWN"  -> adjustBloodVolume(-0.2 * mag); // 탈수
            default -> { }
        }
    }

    private void applyRenalEvent(PhysioEvent ev, double dtSec) {
        String type = ev.getType();
        double mag  = ev.getMagnitude();

        switch (type) {
            case "DIURETIC"   -> adjustBloodVolume(-0.3 * mag); // 이뇨제
            case "SALT_UP"    -> adjustBloodVolume(+0.2 * mag); // 염분↑ → 체액↑
            case "SALT_DOWN"  -> adjustBloodVolume(-0.2 * mag); // 염분↓ → 체액↓
            default -> { }
        }
    }

    private void applyRespirationEvent(PhysioEvent ev, double dtSec) {
        String type = ev.getType();
        double mag  = ev.getMagnitude();

        switch (type) {
            case "OXYGEN_UP"   -> adjustOxygen(+0.4 * mag); // 산소 공급↑
            case "OXYGEN_DOWN" -> adjustOxygen(-0.5 * mag); // 저산소
            default -> { }
        }
    }

    private void applyThermoEvent(PhysioEvent ev, double dtSec) {
        String type = ev.getType();
        double mag  = ev.getMagnitude();

        switch (type) {
            case "HOT"  -> adjustTemp(+0.5 * mag); // 고온 자극
            case "COLD" -> adjustTemp(-0.5 * mag); // 저온 자극
            default -> { }
        }
    }

    private void applyMetabolicEvent(PhysioEvent ev, double dtSec) {
        String type = ev.getType();
        double mag  = ev.getMagnitude();

        switch (type) {
            case "SUGAR_UP"        -> adjustMetabolic(+0.3 * mag);
            case "SUGAR_DOWN"      -> adjustMetabolic(-0.3 * mag);
            case "ACTIVITY_UP"     -> adjustMetabolic(+0.4 * mag);
            case "ACTIVITY_DOWN"   -> adjustMetabolic(-0.4 * mag);
            case "METABOLISM_UP"   -> adjustMetabolic(+0.5 * mag);
            case "METABOLISM_DOWN" -> adjustMetabolic(-0.5 * mag);
            default -> { }
        }
    }

    /* ================== 효과값 조정/감쇠 ================== */

    private void adjustBloodVolume(double delta) {
        bloodVolumeEffect = clamp(bloodVolumeEffect + delta, -1.0, 1.0);
    }

    private void adjustTemp(double delta) {
        tempEffect = clamp(tempEffect + delta, -3.0, 3.0); // -3℃ ~ +3℃
    }

    private void adjustOxygen(double delta) {
        oxygenEffect = clamp(oxygenEffect + delta, -1.5, 1.5);
    }

    private void adjustMetabolic(double delta) {
        metabolicEffect = clamp(metabolicEffect + delta, -1.5, 1.5);
    }

    private void decayEffects(double dtSec) {
        double k = Math.pow(0.5, dtSec / 30.0); // half-life ≈ 30초
        bloodVolumeEffect *= k;
        tempEffect        *= k;
        oxygenEffect      *= k;
        metabolicEffect   *= k;
    }

    private static double clamp(double v, double lo, double hi) {
        return Math.max(lo, Math.min(hi, v));
    }

    // ===== Simulation에서 읽어가는 getter =====
    public double getBloodVolumeEffect() { return bloodVolumeEffect; }
    public double getTempEffect()        { return tempEffect; }
    public double getOxygenEffect()      { return oxygenEffect; }
    public double getMetabolicEffect()   { return metabolicEffect; }

    public Snapshot getSnapshot() {
        // 1) 기본값 설정 (나중엔 Organ에서 끌어오는 걸로 교체 가능)
        double baseHR   = 80;
        double baseSys  = 120;
        double baseDia  = 80;
        double baseTemp = 36.8;
        double baseSpO2 = 98;

        // 2) Core 효과 적용
        double hr   = baseHR   + metabolicEffect * 20;
        double sys  = baseSys  + bloodVolumeEffect * 25;
        double dia  = baseDia  + bloodVolumeEffect * 15;
        double temp = baseTemp + tempEffect;
        double spo2 = baseSpO2 + oxygenEffect * 5;

        // 3) 범위 제한
        hr   = clamp(hr,   20, 180);
        sys  = clamp(sys,  60, 200);
        dia  = clamp(dia,  40, 130);
        temp = clamp(temp, 30, 42);
        spo2 = clamp(spo2, 50, 100);
        SpriteState state = evaluateSpriteState((int) sys, temp);
        return new Snapshot((int) hr, (int) sys, (int) dia, temp, (int) spo2, state);
    }
    // ===== 스프라이트 상태 평가 =====
    private SpriteState evaluateSpriteState(int sys, double temp) {
        if (temp < 35.0) return SpriteState.HYPOTHERMIA;
        if (temp > 38.0) return SpriteState.FEVER;
        if (sys  < 90)   return SpriteState.HYPOTENSION;
        if (sys  > 140)  return SpriteState.HYPERTENSION;
        return SpriteState.NORMAL;
    }


}
