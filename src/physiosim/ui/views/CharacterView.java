// src/physiosim/ui/views/CharacterView.java
package physiosim.ui.views;

import physiosim.control.Simulation;
import physiosim.event.Command;
import physiosim.event.CommandDirection;
import physiosim.event.CommandId;
import physiosim.ui.Theme;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.EnumMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

public class CharacterView extends JPanel {

    private static final int PAD      = 8;
    private static final int TOPBAR_H = 36;
    private static final int BOTBAR_H = 50;
    private static final int GAP      = 12;

    private static final int LEFT_W   = 180;
    private static final int RIGHT_W  = 260;
    private static final int MAX_EVENT_LINES = 8;

    private static final Color TOPBAR_BG    = Theme.PANEL;
    private static final Color BOTTOM_BG    = new Color(26, 63, 84);
    private static final Color LEFT_BG      = Color.BLACK;
    private static final Color CENTER_BG    = new Color(246, 236, 210);
    private static final Color RIGHT_BG     = Color.WHITE;
    private static final Color RIGHT_BORDER = new Color(80, 80, 80);

    public enum State {
        NORMAL,
        FEVER,
        HYPERTENSION,
        HYPOTENSION,
        HYPOTHERMIA,
    }

    private static final Map<State, Image[]> SPRITES = new EnumMap<>(State.class);
    static {
        SPRITES.put(State.NORMAL,       pair("/images/normalD.png", "/images/normalU.png"));
        SPRITES.put(State.FEVER,        pair("/images/feverD.png",  "/images/feverU.png"));
        SPRITES.put(State.HYPERTENSION, pair("/images/hyperD.png",  "/images/hyperU.png"));
        SPRITES.put(State.HYPOTENSION,  pair("/images/hypoD.png",   "/images/hypoU.png"));
        SPRITES.put(State.HYPOTHERMIA,  pair("/images/hypo2D.png",  "/images/hypo2U.png"));
    }

    private static Image[] pair(String d, String u) {
        return new Image[]{ loadImage(d), loadImage(u) };
    }

    private static Image loadImage(String path) {
        var url = CharacterView.class.getResource(path);
        if (url == null) {
            System.err.println("[CharacterView] 이미지 리소스를 찾을 수 없습니다: " + path);
            return new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        }
        return new ImageIcon(url).getImage();
    }

    private Simulation simulation;
    private int    hr, spo2, rr, sbp, dbp, map;
    private double temp;

    private static final DateTimeFormatter FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private String nowStr        = LocalDateTime.now().format(FMT);
    private String headerPatient = "PATIENT: NAME MF / 0";

    private final Timer clockTimer;
    private final Timer breathTimer;
    private boolean upFrame = false;

    private State currentState = State.NORMAL;
    /** 1=저체온, 2=저혈압, 3=정상, 4=고혈압, 5=발열 */
    private int currentScore   = 3;

    private int    heightCm    = 0;
    private double weightKg    = 0.0;
    private double bmi         = 0.0;
    private String reportText  =
            "정상 상태\n"
          + "현재로서는 뚜렷한 이상 소견이 없습니다.\n"
          + "바이탈이 급격히 변하지 않는지 계속 관찰이 필요합니다.";
    private String eventNote   = "";

    private Rectangle cmdR, vitalR, backR;
    private final JTextField cmdField = new JTextField();

    private final Runnable onBack;
    private final Runnable onVital;
    private final Runnable onCommand;

    public CharacterView() { this(null, null, null); }

    public CharacterView(Runnable onBack, Runnable onVital, Runnable onCommand) {
        this.onBack    = onBack;
        this.onVital   = onVital;
        this.onCommand = onCommand;

        setOpaque(false);
        setLayout(null);

        initCommandField();

        breathTimer = new Timer(800, e -> {
            upFrame = !upFrame;
            pullFromSimulation();
            repaint();
        });
        breathTimer.start();

        clockTimer = new Timer(1000, e -> {
            nowStr = LocalDateTime.now().format(FMT);
            repaint();
        });
        clockTimer.start();

        initMouse();
    }

    // 헬퍼
    private void initCommandField() {
        cmdField.setBorder(null);
        cmdField.setFont(Theme.BTN2);
        cmdField.setBackground(new Color(12, 35, 52));
        cmdField.setForeground(Color.WHITE);
        cmdField.setCaretColor(Color.WHITE);
        cmdField.setToolTipText("명령어를 입력하고 Enter를 누르세요.");
        cmdField.addActionListener(e -> handleCommandFromField());
        add(cmdField);
    }

    private void initMouse() {
        MouseAdapter m = new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                Point p = e.getPoint();

                if (cmdR != null && cmdR.contains(p)) {
                    showCommandHelp();
                    if (onCommand != null) onCommand.run();
                    return;
                }
                if (vitalR != null && vitalR.contains(p)) {
                    if (onVital != null) onVital.run();
                    return;
                }
                if (backR != null && backR.contains(p)) {
                    if (onBack != null) onBack.run();
                }
            }

            @Override public void mouseMoved(MouseEvent e) {
                Point p = e.getPoint();
                boolean over =
                        (cmdR   != null && cmdR.contains(p)) ||
                        (vitalR != null && vitalR.contains(p)) ||
                        (backR  != null && backR.contains(p));
                setCursor(over
                        ? Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
                        : Cursor.getDefaultCursor());
            }
        };
        addMouseListener(m);
        addMouseMotionListener(m);
    }

    // 연동
    public void bindSimulation(Simulation sim) {
        this.simulation = sim;
        pullFromSimulation();
    }

    private void pullFromSimulation() {
        if (simulation == null) return;

        hr   = simulation.getHeartRate();
        spo2 = simulation.getSpo2();
        rr   = simulation.getRespRate();
        sbp  = simulation.getSystolic();
        dbp  = simulation.getDiastolic();
        map  = simulation.getMap();
        temp = simulation.getTemperature();

        recalcStateFromVitals();
    }

    // 명령어
    private void handleCommandFromField() {
        String cmd = cmdField.getText();
        if (cmd == null || cmd.isBlank()) return;
        handleCommand(cmd.trim());
        cmdField.setText("");
    }

    private void showCommandHelp() {
        String help = """
                [USE 명령어]
                - INFUSE [level]      : 수액 (기본 1)
                - BLEED [level]       : 출혈
                - DIURETIC [level]    : 이뇨제
                - COLD [level]        : 저온 자극
                - HOT [level]         : 고온 자극

                [UP/DOWN 명령어]
                - FLUID UP|DOWN [lv]
                - SALT UP|DOWN [lv]
                - SUGAR UP|DOWN [lv]
                - OXYGEN UP|DOWN [lv]
                - ACTIVITY UP|DOWN [lv]
                - METABOLISM UP|DOWN [lv]
                
                [초기화 명령어]
                - clear
                - clear log

                예)
                INFUSE 2
                BLEED 1
                FLUID UP 1
                OXYGEN DOWN 2

                ※ STATUS 입력창에 명령어를 입력한 뒤 Enter 키를 누르세요.
                """;

        JOptionPane.showMessageDialog(
                this,
                help,
                "COMMAND HELP",
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    private void handleCommand(String raw) {
        if (simulation == null) {
            JOptionPane.showMessageDialog(
                    this,
                    "Simulation이 연결되지 않았습니다.",
                    "COMMAND",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        String cmd = raw.trim().toLowerCase();
        if (cmd.isEmpty()) return;

        String[] parts = cmd.split("\\s+");
        if (parts.length == 0) return;

        String main = parts[0];
        String arg1 = (parts.length >= 2) ? parts[1] : null;
        String arg2 = (parts.length >= 3) ? parts[2] : null;

        Command command = null;
        String eventMsg = null;

        if (cmd.equals("clear") || cmd.equals("clear log")) {
            clearEventNote();
            appendEventNote("");
            return;
        }

        // [USE] 계열
        switch (main) {
            case "infuse", "bleed", "diuretic", "cold", "hot" -> {
                int level = parseLevel(arg1, 1);

                CommandId id = switch (main) {
                    case "infuse"   -> CommandId.INFUSE;
                    case "bleed"    -> CommandId.BLEED;
                    case "diuretic" -> CommandId.DIURETIC;
                    case "cold"     -> CommandId.COLD;
                    case "hot"      -> CommandId.HOT;
                    default         -> null;
                };

                if (id == null) break;

                command = new Command(id, CommandDirection.NONE, level);

                eventMsg = switch (id) {
                    case INFUSE -> String.format(
                            "수액 주입 Lv.%d : 순환 혈액량이 증가하며 혈압이 상승할 수 있습니다.", level);
                    case BLEED -> String.format(
                            "출혈 Lv.%d : 혈액량 감소로 저혈압과 빈맥이 발생할 수 있습니다.", level);
                    case DIURETIC -> String.format(
                            "이뇨제 투여 Lv.%d : 체액이 빠져나가며 혈압이 서서히 감소할 수 있습니다.", level);
                    case COLD -> String.format(
                            "저온 자극 Lv.%d : 체온이 감소하고 말초혈관이 수축합니다.", level);
                    case HOT -> String.format(
                            "고온 자극 Lv.%d : 체온이 상승하고 말초혈관이 확장할 수 있습니다.", level);
                    default -> String.format("%s Lv.%d 명령이 적용되었습니다.", main.toUpperCase(), level);
                };
            }

            // [UP/DOWN] 계열
            case "fluid", "salt", "sugar", "oxygen", "activity", "metabolism" -> {
                if (arg1 == null) {
                    JOptionPane.showMessageDialog(
                            this,
                            "UP 또는 DOWN을 함께 입력하세요. 예) FLUID UP 1",
                            "COMMAND",
                            JOptionPane.INFORMATION_MESSAGE
                    );
                    return;
                }

                CommandDirection dir;
                String dirKo;
                if ("up".equals(arg1)) {
                    dir   = CommandDirection.UP;
                    dirKo = "증가";
                } else if ("down".equals(arg1)) {
                    dir   = CommandDirection.DOWN;
                    dirKo = "감소";
                } else {
                    JOptionPane.showMessageDialog(
                            this,
                            "두 번째 인자는 UP 또는 DOWN이어야 합니다.",
                            "COMMAND",
                            JOptionPane.INFORMATION_MESSAGE
                    );
                    return;
                }

                int level = parseLevel(arg2, 1);

                CommandId id = switch (main) {
                    case "fluid"      -> CommandId.FLUID;
                    case "salt"       -> CommandId.SALT;
                    case "sugar"      -> CommandId.SUGAR;
                    case "oxygen"     -> CommandId.OXYGEN;
                    case "activity"   -> CommandId.ACTIVITY;
                    case "metabolism" -> CommandId.METABOLISM;
                    default           -> null;
                };

                if (id == null) break;

                command = new Command(id, dir, level);

                eventMsg = switch (id) {
                    case FLUID -> (dir == CommandDirection.UP)
                            ? String.format("체액 Lv.%d %s : 순환 혈액량이 늘어나 혈압이 상승할 수 있습니다.", level, dirKo)
                            : String.format("체액 Lv.%d %s : 탈수로 인해 혈압이 감소하고 맥박이 빨라질 수 있습니다.", level, dirKo);

                    case SALT -> (dir == CommandDirection.UP)
                            ? String.format("염분 Lv.%d %s : 나트륨 증가로 체액이 늘어나고 혈압이 상승할 수 있습니다.", level, dirKo)
                            : String.format("염분 Lv.%d %s : 나트륨 감소로 체액이 줄어들 수 있습니다.", level, dirKo);

                    case SUGAR -> (dir == CommandDirection.UP)
                            ? String.format("혈당 Lv.%d %s : 에너지가 증가하고 대사가 촉진됩니다.", level, dirKo)
                            : String.format("혈당 Lv.%d %s : 저혈당 위험으로 의식·자율신경 변화가 나타날 수 있습니다.", level, dirKo);

                    case OXYGEN -> (dir == CommandDirection.UP)
                            ? String.format("산소 공급 Lv.%d %s : SpO₂가 회복되고 호흡곤란이 완화될 수 있습니다.", level, dirKo)
                            : String.format("산소 공급 Lv.%d %s : 저산소로 SpO₂가 떨어지고 호흡수가 증가할 수 있습니다.", level, dirKo);

                    case ACTIVITY -> (dir == CommandDirection.UP)
                            ? String.format("활동량 Lv.%d %s : 근육 사용이 증가해 HR과 RR이 상승할 수 있습니다.", level, dirKo)
                            : String.format("활동량 Lv.%d %s : 대사 요구량이 감소해 HR과 RR이 떨어질 수 있습니다.", level, dirKo);

                    case METABOLISM -> (dir == CommandDirection.UP)
                            ? String.format("대사 Lv.%d %s : 전신 대사가 증가해 심박수와 체온이 상승할 수 있습니다.", level, dirKo)
                            : String.format("대사 Lv.%d %s : 전신 대사가 감소해 체온과 에너지 소모가 줄어듭니다.", level, dirKo);

                    default -> String.format("%s %s Lv.%d 적용.", main.toUpperCase(), dir.name(), level);
                };
            }

            default -> {
                JOptionPane.showMessageDialog(
                        this,
                        "알 수 없는 명령어입니다.",
                        "COMMAND",
                        JOptionPane.INFORMATION_MESSAGE
                );
                return;
            }
        }

        if (command == null) return;

        simulation.sendCommand(command);

        if (eventMsg != null && !eventMsg.isBlank()) {
            appendEventNote(eventMsg);
        } else {
            appendEventNote(raw + " 명령이 적용되었습니다.");
        }
    }

    private int parseLevel(String raw, int defaultVal) {
        if (raw == null || raw.isBlank()) return defaultVal;
        try {
            int v = Integer.parseInt(raw);
            return (v > 0) ? v : defaultVal;
        } catch (NumberFormatException e) {
            return defaultVal;
        }
    }

    // 그리기
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();

        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                            RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                            RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();

        g2.setColor(Theme.BG);
        g2.fillRect(0, 0, w, h);

        drawTopBar(g2, w);

        int contentX = PAD;
        int contentY = PAD + TOPBAR_H;
        int contentW = w - PAD * 2;
        int contentH = h - PAD * 2 - TOPBAR_H - BOTBAR_H;

        int leftW   = LEFT_W;
        int rightW  = RIGHT_W;
        int centerW = contentW - leftW - rightW - GAP * 2;
        if (centerW < 100) centerW = 100;

        int leftX   = contentX;
        int centerX = leftX + leftW + GAP;
        int rightX  = centerX + centerW + GAP;

        drawLeftVitals(g2, leftX,  contentY, leftW,  contentH);
        drawCenterCharacter(g2, centerX, contentY, centerW, contentH);
        drawRightPanel(g2, rightX, contentY, rightW, contentH);
        drawBottomBar(g2, PAD, h - PAD - BOTBAR_H, w - PAD * 2, BOTBAR_H);

        g2.dispose();
    }

    // 상단 바
    private void drawTopBar(Graphics2D g2, int w) {
        g2.setColor(TOPBAR_BG);
        g2.fillRect(PAD, PAD, w - PAD * 2, TOPBAR_H);

        g2.setColor(Theme.FG);
        g2.setFont(Theme.BTN2);
        g2.drawString("PHYSIOSIM CHARACTER", PAD + 12, PAD + 24);

        String header = headerPatient + "   TIME: " + nowStr;
        FontMetrics fm = g2.getFontMetrics();
        int x = w - PAD - fm.stringWidth(header) - 12;
        g2.drawString(header, x, PAD + 24);
    }

    // 좌측
    private void drawLeftVitals(Graphics2D g2, int x, int y, int w, int h) {
        g2.setColor(LEFT_BG);
        g2.fillRect(x, y, w, h);

        int tx = x + 10;
        int ty = y + 24;
        int lineH = 22;

        g2.setFont(Theme.BTN2);

        g2.setColor(new Color(0, 225, 100));
        g2.drawString(String.format("HR    %3d", hr), tx, ty); ty += lineH;

        g2.setColor(new Color(60, 170, 230));
        g2.drawString(String.format("SpO2  %3d", spo2), tx, ty); ty += lineH;

        g2.setColor(new Color(255, 215, 60));
        g2.drawString(String.format("RR    %3d", rr), tx, ty); ty += lineH;

        g2.setColor(new Color(255, 140, 60));
        g2.drawString(String.format("NIBP  %3d/%3d", sbp, dbp), tx, ty); ty += lineH;

        g2.setColor(new Color(255, 140, 60));
        g2.drawString(String.format("MAP   %3d", map), tx, ty); ty += lineH;

        g2.setColor(new Color(245, 246, 250));
        g2.drawString(String.format("TEMP  %4.1f", temp), tx, ty);
    }

    // 캐릭터
    private void drawCenterCharacter(Graphics2D g2, int x, int y, int w, int h) {
        Shape card = new RoundRectangle2D.Double(x, y, w, h, 24, 24);
        g2.setColor(CENTER_BG);
        g2.fill(card);

        g2.setColor(new Color(0, 0, 0, 40));
        g2.setStroke(new BasicStroke(2f));
        g2.draw(card);

        g2.setFont(Theme.BTN2);
        g2.setColor(Color.BLACK);
        String stateText = "STATE SCORE: " + currentScore + "/5  (" + stateName(currentState) + ")";
        FontMetrics fm = g2.getFontMetrics();
        int sx = x + (w - fm.stringWidth(stateText)) / 2;
        int sy = y + 90;
        g2.drawString(stateText, sx, sy);

        Image[] arr = SPRITES.get(currentState);
        if (arr == null) return;

        Image raw = arr[upFrame ? 1 : 0];
        int ow = raw.getWidth(this);
        int oh = raw.getHeight(this);
        if (ow <= 0 || oh <= 0) return;

        int maxSize = (int) (Math.min(w, h) * 0.8);
        double scale = Math.min((double) maxSize / ow, (double) maxSize / oh);
        int nw = (int) (ow * scale);
        int nh = (int) (oh * scale);

        int ix = x + (w - nw) / 2;
        int iy = y + (h - nh) / 2 + 10;

        g2.drawImage(raw, ix, iy, nw, nh, this);
    }

    private String stateName(State s) {
        return switch (s) {
            case NORMAL       -> "NORMAL";
            case FEVER        -> "FEVER";
            case HYPERTENSION -> "HYPERTENSION";
            case HYPOTENSION  -> "HYPOTENSION";
            case HYPOTHERMIA  -> "HYPOTHERMIA";
        };
    }

    // 우측
    private void drawRightPanel(Graphics2D g2, int x, int y, int w, int h) {
        g2.setColor(RIGHT_BG);
        g2.fillRect(x, y, w, h);
        g2.setColor(RIGHT_BORDER);
        g2.setStroke(new BasicStroke(2f));
        g2.drawRect(x, y, w, h);

        int pad   = 14;
        int tx    = x + pad;
        int ty    = y + pad + 4;
        int lineH = 20;

        g2.setFont(Theme.BTN2);
        g2.setColor(Color.BLACK);
        g2.drawString("CHARACTER REPORT", tx, ty);
        ty += lineH + 4;

        g2.setFont(Theme.FIELD);
        g2.drawString(String.format("Height : %d cm", heightCm), tx, ty); ty += lineH;
        g2.drawString(String.format("Weight : %.1f kg", weightKg), tx, ty); ty += lineH;
        g2.drawString(String.format("BMI    : %.1f", bmi), tx, ty); ty += lineH * 2;

        g2.setFont(Theme.BTN2);
        g2.drawString("Current Status", tx, ty);
        ty += lineH;

        g2.setFont(Theme.KR);
        FontMetrics bodyFm = g2.getFontMetrics();
        int textMaxWidth   = w - pad * 2;

        String fullText = reportText;
        if (eventNote != null && !eventNote.isBlank()) {
            fullText = fullText + "\n" + "[EVENT]\n" + eventNote;
        }

        List<String> wrappedLines = wrapText(fullText, bodyFm, textMaxWidth);

        for (String line : wrappedLines) {
            if (ty > y + h - pad) break;

            if ("[EVENT]".equals(line)) {
                g2.setColor(new Color(180, 30, 30));
            } else {
                g2.setColor(Color.BLACK);
            }

            g2.drawString(line, tx, ty);
            ty += lineH;
        }
    }

    private List<String> wrapText(String text, FontMetrics fm, int maxWidth) {
        List<String> result = new ArrayList<>();
        if (text == null || text.isEmpty()) return result;

        String[] paragraphs = text.split("\\r?\\n");
        for (String para : paragraphs) {
            if (para.isEmpty()) {
                result.add("");
                continue;
            }

            String line = para;
            while (!line.isEmpty()) {
                if (fm.stringWidth(line) <= maxWidth) {
                    result.add(line);
                    break;
                }

                int cut = line.length();
                while (cut > 0 && fm.stringWidth(line.substring(0, cut)) > maxWidth) {
                    cut--;
                }
                if (cut == 0) cut = 1;

                result.add(line.substring(0, cut));
                line = line.substring(cut);
            }
        }

        return result;
    }

    // 하단 바
    private void drawBottomBar(Graphics2D g2, int x, int y, int w, int h) {
        g2.setColor(BOTTOM_BG);
        g2.fillRect(x, y, w, h);

        int bw = 100, bh = 32, gap = 12;
        int bx3 = x + w - bw - 12;
        int bx2 = bx3 - bw - gap;
        int bx1 = bx2 - bw - gap;

        cmdR   = new Rectangle(bx1, y + (h - bh) / 2, bw, bh);
        vitalR = new Rectangle(bx2, y + (h - bh) / 2, bw, bh);
        backR  = new Rectangle(bx3, y + (h - bh) / 2, bw, bh);

        String label = "STATUS / CMD:";
        g2.setFont(Theme.BTN2);
        FontMetrics fm = g2.getFontMetrics();

        int labelX = x + 14;
        int labelY = y + (h + fm.getAscent() - fm.getDescent()) / 2;

        g2.setColor(new Color(180, 200, 220));
        g2.drawString(label, labelX, labelY);

        int fieldH = 28;
        int fieldY = y + (h - fieldH) / 2;
        int fieldX = labelX + fm.stringWidth(label) + 10;
        int fieldW = bx1 - fieldX - 12;

        cmdField.setBounds(fieldX, fieldY, Math.max(60, fieldW), fieldH);

        drawBtn(g2, cmdR,   "COMMAND");
        drawBtn(g2, vitalR, "VITAL");
        drawBtn(g2, backR,  "BACK");
    }

    private void drawBtn(Graphics2D g2, Rectangle r, String s) {
        g2.setColor(Theme.BTNBACK);
        g2.fillRoundRect(r.x, r.y, r.width, r.height, 16, 16);
        g2.setColor(Theme.FG);
        g2.setStroke(new BasicStroke(2f));
        g2.drawRoundRect(r.x, r.y, r.width, r.height, 16, 16);

        g2.setFont(Theme.BTN2);
        FontMetrics fm = g2.getFontMetrics();
        int tx = r.x + (r.width - fm.stringWidth(s)) / 2;
        int ty = r.y + (r.height + fm.getAscent() - fm.getDescent()) / 2;
        g2.drawString(s, tx, ty);
    }

    // 상태
    private void recalcStateFromVitals() {
        boolean fever       = temp >= 38.0;
        boolean hypothermia = temp > 0 && temp < 35.0;
        boolean highBP      = (sbp >= 140) || (dbp >= 90) || (map >= 110);
        boolean lowBP       = (sbp > 0 && sbp <= 90) || (map > 0 && map <= 70);

        if (hypothermia)        currentState = State.HYPOTHERMIA;
        else if (fever)         currentState = State.FEVER;
        else if (lowBP)         currentState = State.HYPOTENSION;
        else if (highBP)        currentState = State.HYPERTENSION;
        else                    currentState = State.NORMAL;

        currentScore = switch (currentState) {
            case HYPOTHERMIA  -> 1;
            case HYPOTENSION  -> 2;
            case NORMAL       -> 3;
            case HYPERTENSION -> 4;
            case FEVER        -> 5;
        };

        switch (currentState) {
            case NORMAL -> reportText =
                    "정상 상태\n"
                  + "현재로서는 뚜렷한 이상 소견이 없습니다.\n"
                  + "바이탈이 급격히 변하지 않는지 계속 관찰이 필요합니다.";
            case FEVER -> reportText =
                    "발열 상태\n"
                  + "체온 상승이 관찰됩니다.\n"
                  + "감염/염증, 탈수, 약물 반응 등을 고려해야 합니다.";
            case HYPERTENSION -> reportText =
                    "고혈압 상태\n"
                  + "혈압 또는 MAP이 상승한 상태입니다.\n"
                  + "심혈관계 부담 증가, 뇌혈관·신장 등 표적 장기 손상 위험이 있습니다.";
            case HYPOTENSION -> reportText =
                    "저혈압 상태\n"
                  + "혈압 또는 MAP이 낮아 장기 관류 저하가 우려됩니다.";
            case HYPOTHERMIA -> reportText =
                    "저체온 상태\n"
                  + "체온이 정상보다 낮은 상태입니다.\n"
                  + "보온, 떨림/의식 변화, 부정맥 위험 등을 주의 깊게 봐야 합니다.";
        }
    }

    // 외부
    public void setBodyInfo(int heightCm, double weightKg, double bmi) {
        this.heightCm = Math.max(0, heightCm);
        this.weightKg = Math.max(0.0, weightKg);
        this.bmi      = Math.max(0.0, bmi);
        repaint();
    }

    public void setReportText(String text) {
        this.reportText = (text == null ? "" : text);
        repaint();
    }

    public void setHeaderPatient(String header) {
        if (header == null || header.isBlank()) {
            this.headerPatient = "PATIENT: NAME MF / 0";
        } else {
            this.headerPatient = header;
        }
        repaint();
    }

    public void stopAnimation() {
        breathTimer.stop();
        clockTimer.stop();
    }

    public void startAnimation() {
        if (!breathTimer.isRunning()) breathTimer.start();
        if (!clockTimer.isRunning())  clockTimer.start();
    }

    public void setEventNote(String note) {
        this.eventNote = (note == null ? "" : note.trim());
        repaint();
    }

    public void appendEventNote(String extra) {
        if (extra == null || extra.isBlank()) return;

        if (this.eventNote == null || this.eventNote.isBlank()) {
            this.eventNote = extra.trim();
        } else {
            this.eventNote = this.eventNote + "\n" + extra.trim();
        }

        String[] lines = this.eventNote.split("\\r?\\n");
        if (lines.length > MAX_EVENT_LINES) {
            int start = lines.length - MAX_EVENT_LINES;
            StringBuilder sb = new StringBuilder();
            for (int i = start; i < lines.length; i++) {
                if (sb.length() > 0) sb.append("\n");
                sb.append(lines[i]);
            }
            this.eventNote = sb.toString();
        }

        repaint();
    }

    public void clearEventNote() {
        this.eventNote = "";
        repaint();
    }
}
