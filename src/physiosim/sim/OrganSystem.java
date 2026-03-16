// src/physiosim/sim/OrganSystem.java
package physiosim.sim;

import java.util.*;

public class OrganSystem {

    public enum Type {
        CENTRAL_NERVOUS,    // 중추신경계
        PERIPHERAL_NERVOUS  // 말초신경계
    }

    // 말초신경계의 세 가지 가지
    public enum PnsBranch {
        SYMPATHETIC,        // 교감
        PARASYMPATHETIC,    // 부교감
        SOMATIC             // 체성
    }

    private final Type type;
    private final List<Organ> organs;

    // PNS 전용 톤 값 (0.0 ~ 1.0)
    private double sympatheticTone;
    private double parasympatheticTone;
    private double somaticTone;

    private OrganSystem(Type type, List<Organ> organs) {
        this.type = type;
        this.organs = new ArrayList<>(organs);

        this.sympatheticTone     = 0.5;
        this.parasympatheticTone = 0.5;
        this.somaticTone         = 0.5;
    }

    private static List<Organ> listOf(Organ... os) {
        List<Organ> list = new ArrayList<>();
        for (Organ o : os) list.add(o);
        return list;
    }

    public static OrganSystem centralNervous(Organ brain, Organ spinalCord) {
        return new OrganSystem(
                Type.CENTRAL_NERVOUS,
                listOf(brain, spinalCord)
        );
    }

    public static OrganSystem peripheralNervous() {
        return new OrganSystem(
                Type.PERIPHERAL_NERVOUS,
                new ArrayList<>()
        );
    }

    // GET
    public Type getType() {return type;}
    public List<Organ> getOrgans() {return Collections.unmodifiableList(organs);}

    public double getSympatheticTone() {return sympatheticTone;}
    public double getParasympatheticTone() {return parasympatheticTone;}
    public double getSomaticTone() {return somaticTone;}
    public double getPnsBranchTone(PnsBranch branch) {
        return switch (branch) {
            case SYMPATHETIC     -> sympatheticTone;
            case PARASYMPATHETIC -> parasympatheticTone;
            case SOMATIC         -> somaticTone;
        };
    }

    public void setSympatheticTone(double value) {this.sympatheticTone = clamp01(value);}

    public void setParasympatheticTone(double value) {this.parasympatheticTone = clamp01(value);}

    public void setSomaticTone(double value) {this.somaticTone = clamp01(value);}

    public void setPnsBranchTone(PnsBranch branch, double value) {
        double v = clamp01(value);
        switch (branch) {
            case SYMPATHETIC     -> sympatheticTone = v;
            case PARASYMPATHETIC -> parasympatheticTone = v;
            case SOMATIC         -> somaticTone = v;
        }
    }

    private double clamp01(double v) {
        if (v < 0.0) return 0.0;
        if (v > 1.0) return 1.0;
        return v;
    }
}
