package physiosim.sim;

public final class Snapshot {

    public final int hr;
    public final int sys;
    public final int dia;
    public final double temp;
    public final int spo2;

    // 캐릭터 상태
    public final SpriteState spriteState;

    public Snapshot(int hr, int sys, int dia,
                    double temp, int spo2,
                    SpriteState spriteState) {
        this.hr = hr;
        this.sys = sys;
        this.dia = dia;
        this.temp = temp;
        this.spo2 = spo2;
        this.spriteState = spriteState;
    }
}
