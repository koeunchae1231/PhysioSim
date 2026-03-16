package physiosim.sim;

public enum SpriteState {
    HYPOTHERMIA(1),   // 저체온
    HYPOTENSION(2),   // 저혈압
    NORMAL(3),        // 정상
    HYPERTENSION(4),  // 고혈압
    FEVER(5);         // 발열

    private final int code;

    SpriteState(int code) { this.code = code; }
    
    public int code() { return code; }
}
