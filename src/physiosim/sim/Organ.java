// src/physiosim/sim/Organ.java
package physiosim.sim;

import java.util.*;

public class Organ {

    public enum Type {
        // 순환계
        HEART,          // 심장
        BLOOD,          // 혈액
        VESSEL,         // 혈관
        // 호흡계
        LUNG,           // 폐
        // 배설계
        KIDNEY,         // 신장
        // 소화계
        STOMACH,        // 위
        SMALL_INTESTINE,// 소장
        // 근육계 / 내분비계
        SKELETAL_MUSCLE,// 골격근
        ADRENAL_MEDULLA,// 부신수질
        PANCREAS,       // 이자
        THYROID,        // 갑상샘
        // 신경계
        BRAIN,          // 뇌
        SPINAL_CORD     // 척수
    }

    private final Type type;
    private final String name;
    private final List<Tissue> tissues;

    private Organ(Type type, String name, List<Tissue> tissues) {
        this.type = type;
        this.name = name;
        this.tissues = new ArrayList<>(tissues);
    }

    private static List<Tissue> listOf(Tissue... ts) {
        List<Tissue> list = new ArrayList<>();
        for (Tissue t : ts) {
            list.add(t);
        }
        return list;
    }
    
    // 순환
    public static Organ heart() {
        return new Organ(
                Type.HEART,
                "Heart",
                listOf(
                        Tissue.muscle(),     // myocardium
                        Tissue.epithelial()  // inner lining
                )
        );
    }

    public static Organ blood() {
        return new Organ(
                Type.BLOOD,
                "Blood",
                listOf(
                        Tissue.blood()
                )
        );
    }

    public static Organ vessel() {
        return new Organ(
                Type.VESSEL,
                "Vessel",
                listOf(
                        Tissue.muscle(),     // smooth muscle
                        Tissue.epithelial()  // endothelium
                )
        );
    }

    // 호흡
    public static Organ lung() {
        return new Organ(
                Type.LUNG,
                "Lung",
                listOf(
                        Tissue.epithelial()  // alveolar epithelium
                )
        );
    }

    // 배설
    public static Organ kidney() {
        return new Organ(
                Type.KIDNEY,
                "Kidney",
                listOf(
                        Tissue.epithelial(), // tubule
                        Tissue.blood()       // renal blood
                )
        );
    }

    // 소화
    public static Organ stomach() {
        return new Organ(
                Type.STOMACH,
                "Stomach",
                listOf(
                        Tissue.epithelial(), // mucosa
                        Tissue.muscle()      // smooth muscle
                )
        );
    }

    public static Organ smallIntestine() {
        return new Organ(
                Type.SMALL_INTESTINE,
                "SmallIntestine",
                listOf(
                        Tissue.epithelial(), // absorptive epithelium
                        Tissue.muscle()      // smooth muscle
                )
        );
    }

    // 근육
    public static Organ skeletalMuscle() {
        return new Organ(
                Type.SKELETAL_MUSCLE,
                "SkeletalMuscle",
                listOf(
                        Tissue.muscle()
                )
        );
    }

    // 내분비
    public static Organ adrenalMedulla() {
        return new Organ(
                Type.ADRENAL_MEDULLA,
                "AdrenalMedulla",
                listOf(
                        Tissue.endocrine()
                )
        );
    }

    public static Organ pancreas() {
        return new Organ(
                Type.PANCREAS,
                "Pancreas",
                listOf(
                        Tissue.endocrine(),
                        Tissue.epithelial()
                )
        );
    }

    public static Organ thyroid() {
        return new Organ(
                Type.THYROID,
                "Thyroid",
                listOf(
                        Tissue.endocrine()
                )
        );
    }

    // 신경계
    public static Organ brain() {
        return new Organ(
                Type.BRAIN,
                "Brain",
                listOf(
                        Tissue.nervous()
                )
        );
    }

    public static Organ spinalCord() {
        return new Organ(
                Type.SPINAL_CORD,
                "SpinalCord",
                listOf(
                        Tissue.nervous()
                )
        );
    }

    // GET
    public Type getType()       { return type; }
    public String getName()     { return name; }

    public List<Tissue> getTissues() {
        return Collections.unmodifiableList(tissues);
    }

    public Tissue getMainTissue() {
        return tissues.isEmpty() ? null : tissues.get(0);
    }

    public Tissue findTissueByType(Tissue.Type tissueType) {
        for (Tissue t : tissues) {
            if (t.getType() == tissueType) {
                return t;
            }
        }
        return null;
    }
}
