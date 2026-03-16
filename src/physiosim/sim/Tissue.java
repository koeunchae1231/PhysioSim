package physiosim.sim;

// 여러 세포(Cell)들이 모여 이루는 기능 단위 '조직' 모델
// 구성 세포의 생리적 특성 집합이 조직의 성질을 결정
// 기관 단계에서 기능 계산의 기본 단위

public class Tissue {

    public enum Type {
        NERVOUS_TISSUE,     // 신경조직
        MUSCLE_TISSUE,      // 근육조직
        EPITHELIAL_TISSUE,  // 상피조직 (폐포 포함)
        BLOOD_TISSUE,       // 혈액
        ENDOCRINE_TISSUE    // 내분비조직
    }

    private final Type type;
    private final Cell.Type mainCellType;

    // 상태값
    private double functionLevel;   // 기능률(0~1)
    private double perfusion;       // 관류(0~1)
    private double oxygenLevel;     // 산소화(0~1)


    private Tissue(Type type, Cell.Type mainCellType) {
        this.type = type;
        this.mainCellType = mainCellType;
        this.functionLevel = 1.0;
        this.perfusion     = 1.0;
        this.oxygenLevel   = 1.0;
    }


    // 신경조직: 뉴런
    public static Tissue nervous() { return new Tissue(Type.NERVOUS_TISSUE, Cell.Type.NEURON); }
    // 근육조직: 골격근 + AUTORHYTHMIC
    public static Tissue muscle() { return new Tissue(Type.MUSCLE_TISSUE, Cell.Type.SKELETAL_MUSCLE); }
    // 상피조직: 기본 상피 기반 + ALVEOLAR
    public static Tissue epithelial() { return new Tissue(Type.EPITHELIAL_TISSUE, Cell.Type.EPITHELIAL); }
    // 혈액조직: 적혈구 기반
    public static Tissue blood() { return new Tissue(Type.BLOOD_TISSUE, Cell.Type.RED_BLOOD); }
    // 내분비조직: 크롬친화세포 기반
    public static Tissue endocrine() { return new Tissue(Type.ENDOCRINE_TISSUE, Cell.Type.CHROMAFFIN); }

    // GET
    public Type getType() {return type;}
    public Cell.Type getMainCellType() {return mainCellType;}
    
    public double getFunctionLevel() {return functionLevel;}
    public double getPerfusion() {return perfusion;}
    public double getOxygenLevel() {return oxygenLevel;}
    
    // SET
    public void setFunctionLevel(double functionLevel) {this.functionLevel = clamp01(functionLevel);}
    public void setPerfusion(double perfusion) {this.perfusion = clamp01(perfusion);}
    public void setOxygenLevel(double oxygenLevel) {this.oxygenLevel = clamp01(oxygenLevel);}

    private double clamp01(double v) {
        if (v < 0.0) return 0.0;
        if (v > 1.0) return 1.0;
        return v;
    }
}
