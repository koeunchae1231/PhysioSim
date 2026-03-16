package physiosim.sim;

// 인체의 기본 단위 '세포' 모델
// 기본적으로 최소 기능 세포로 구분: 조직 단계의 기능적 차이 결정 요소
// 각 세포는 고유한 생리적 특성을 보유
// 조직 단계에서 기능 계산의 기본 단위

public class Cell {

    // 세포 종류
    public enum Type {
        NEURON,             // 뉴런
        SKELETAL_MUSCLE,    // 골격근세포
        AUTORHYTHMIC,       // 자율박동세포
        RED_BLOOD,          // 적혈구
        ALVEOLAR,           // 폐포세포
        CHROMAFFIN,         // 크롬친화세포
        EPITHELIAL          // 상피세포
    }

    private final Type type;

    // 속성
    private double excitability;      // 흥분성
    private double contractility;     // 수축성
    private double oxygenCapacity;    // 산소 운반/필요 능력
    private double secretionLevel;    // 분비 능력

    
    private Cell(Type type) { this.type = type; }
    
    public static Cell neuron() {
        Cell c = new Cell(Type.NEURON);
        c.excitability   = 1.0;
        c.contractility  = 0.0;
        c.oxygenCapacity = 0.3;
        c.secretionLevel = 0.7;   // 신경전달물질 분비
        return c;
    }

    public static Cell skeletalMuscle() {
        Cell c = new Cell(Type.SKELETAL_MUSCLE);
        c.excitability   = 0.8;
        c.contractility  = 1.0;
        c.oxygenCapacity = 0.6;
        c.secretionLevel = 0.0;
        return c;
    }

    public static Cell autorhythmic() {
        Cell c = new Cell(Type.AUTORHYTHMIC);
        c.excitability   = 1.0;
        c.contractility  = 0.4;
        c.oxygenCapacity = 0.5;
        c.secretionLevel = 0.0;
        return c;
    }

    public static Cell redBlood() {
        Cell c = new Cell(Type.RED_BLOOD);
        c.excitability   = 0.0;
        c.contractility  = 0.0;
        c.oxygenCapacity = 1.0;   // 산소 운반 담당
        c.secretionLevel = 0.0;
        return c;
    }

    public static Cell alveolar() {
        Cell c = new Cell(Type.ALVEOLAR);
        c.excitability   = 0.2;
        c.contractility  = 0.0;
        c.oxygenCapacity = 0.8;   // 가스교환 효율
        c.secretionLevel = 0.3;   // surfactant 분비 느낌
        return c;
    }

    public static Cell chromaffin() {
        Cell c = new Cell(Type.CHROMAFFIN);
        c.excitability   = 0.7;
        c.contractility  = 0.0;
        c.oxygenCapacity = 0.4;
        c.secretionLevel = 1.0;   // 카테콜아민 분비
        return c;
    }

    public static Cell epithelial() {
        Cell c = new Cell(Type.EPITHELIAL);
        c.excitability   = 0.1;
        c.contractility  = 0.0;
        c.oxygenCapacity = 0.3;
        c.secretionLevel = 0.5;   // 흡수/분비 역할
        return c;
    }

    public Type getType() { return type; }
}
