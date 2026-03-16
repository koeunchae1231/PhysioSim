// src/physiosim/ui/views/VitalView.java
package physiosim.ui.views;

import physiosim.control.Simulation;
import physiosim.ui.Theme;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Path2D;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class VitalView extends JPanel {

    private static final int PAD      = 8;
    private static final int TOPBAR_H = 36;
    private static final int BOTBAR_H = 50;
    private static final int GAP      = 12;

    private static final double LEFT_RATIO = 0.62;
    private static final int    ROWS       = 4;

    private final int[] rowCenterY = new int[ROWS];
    private int rowH;

    private static final double WINDOW_SEC    = 4.0;
    private static final double TIME_STEP_SEC = 0.04;

    private double timeSec = 0.0;
    private final Timer waveTimer;

    private Simulation simulation;

    private int hr   = 0;
    private int spo2 = 0;
    private int rr   = 0;
    private int sbp  = 0;
    private int dbp  = 0;
    private int map  = 0;
    private double temp = 0.0;

    private final Timer clockTimer;
    private static final DateTimeFormatter FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private String nowStr = LocalDateTime.now().format(FMT);

    private String patientName = "NAME";
    private String patientSex  = "MF";
    private int    patientAge  = 0;

    private Rectangle backR, charR, defR;

    private final Runnable onBack;
    private final Runnable onCharacter;
    private final Runnable onDefault;

    private String alarmText = "";

    public VitalView(Runnable onBack, Runnable onCharacter, Runnable onDefault) {
        this.onBack      = onBack;
        this.onCharacter = onCharacter;
        this.onDefault   = onDefault;

        setOpaque(false);
        setLayout(null);

        // 파형 + 시뮬레이션 타이머
        waveTimer = new Timer((int) (TIME_STEP_SEC * 1000), e -> {
            timeSec += TIME_STEP_SEC;
            if (simulation != null) {
                simulation.tick(TIME_STEP_SEC);
                pullFromSimulation();
            }
            repaint();
        });
        waveTimer.start();

        // 상단 시계
        clockTimer = new Timer(1000, e -> {
            nowStr = LocalDateTime.now().format(FMT);
            repaint();
        });
        clockTimer.start();

        // 마우스 처리
        MouseAdapter m = new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                Point p = e.getPoint();
                if (runIfHit(backR, p, onBack))      return;
                if (runIfHit(charR, p, onCharacter)) return;
                runIfHit(defR, p, onDefault);
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                Point p = e.getPoint();
                boolean over =
                        (backR != null && backR.contains(p)) ||
                        (charR != null && charR.contains(p)) ||
                        (defR  != null && defR.contains(p));

                setCursor(over
                        ? Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
                        : Cursor.getDefaultCursor());
            }
        };
        addMouseListener(m);
        addMouseMotionListener(m);
    }

    private boolean runIfHit(Rectangle r, Point p, Runnable action) {
        if (r != null && r.contains(p) && action != null) {
            action.run();
            return true;
        }
        return false;
    }

    public void bindSimulation(Simulation sim) {
        this.simulation = sim;
        if (sim != null) pullFromSimulation();
    }

    private void pullFromSimulation() {
        hr   = simulation.getHeartRate();
        spo2 = simulation.getSpo2();
        rr   = simulation.getRespRate();
        sbp  = simulation.getSystolic();
        dbp  = simulation.getDiastolic();
        map  = simulation.getMap();
        temp = simulation.getTemperature();
    }

    @Override
    public void doLayout() {
        int w = getWidth();
        int h = getHeight();

        int top    = PAD + TOPBAR_H;
        int height = h - PAD * 2 - TOPBAR_H - BOTBAR_H;

        rowH = height / ROWS;
        for (int i = 0; i < ROWS; i++) {
            rowCenterY[i] = top + i * rowH + rowH / 2;
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();

        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                            RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                            RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int w  = getWidth();
        int h  = getHeight();

        g2.setColor(Theme.BG);
        g2.fillRect(0, 0, w, h);

        // 상단 바
        g2.setColor(Theme.PANEL);
        g2.fillRect(PAD, PAD, w - PAD * 2, TOPBAR_H);

        g2.setColor(Theme.FG);
        g2.setFont(Theme.BTN2);
        g2.drawString("PHYSIOSIM VITAL", PAD + 12, PAD + 24);

        String right = getHeaderPatient() + "   TIME: " + nowStr;
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(right, w - PAD - fm.stringWidth(right) - 12, PAD + 24);

        int leftX  = PAD;
        int topY   = PAD + TOPBAR_H;
        int totalW = w - PAD * 2;
        int totalH = h - PAD * 2 - TOPBAR_H - BOTBAR_H;

        int waveW  = (int) (totalW * LEFT_RATIO);
        int rightW = totalW - waveW - GAP;

        // 왼쪽 파형 영역
        g2.setColor(Color.BLACK);
        g2.fillRect(leftX, topY, waveW, totalH);

        drawWaveECG (g2, leftX, waveW, rowCenterY[0], hr);
        drawWaveSPO2(g2, leftX, waveW, rowCenterY[1], hr);
        drawWaveResp(g2, leftX, waveW, rowCenterY[2], rr);
        drawWaveBP  (g2, leftX, waveW, rowCenterY[3], sbp, dbp);

        // 오른쪽 수치 영역
        int rx = leftX + waveW + GAP;
        drawRight(g2, rx, topY, rightW);

        // 하단 바
        drawBottom(g2, PAD, h - PAD - BOTBAR_H, w - PAD * 2, BOTBAR_H);

        g2.dispose();
    }

    // 파형
    private void drawFlatLine(Graphics2D g2, int x, int w, int cy, Color c) {
        g2.setColor(c);
        g2.setStroke(new BasicStroke(2f));
        int sx = x + 20;
        int ex = x + w - 20;
        g2.drawLine(sx, cy, ex, cy);
    }

    private void drawWaveECG(Graphics2D g2, int x, int w, int cy, int hrVal) {
        Color c = new Color(0, 225, 100);
        int sx = x + 20;
        int ex = x + w - 20;

        if (hrVal <= 0) {
            drawFlatLine(g2, x, w, cy, c);
            return;
        }

        double pixels = Math.max(1, ex - sx);
        double pxDt   = WINDOW_SEC / pixels;
        double period = 60.0 / Math.max(1, hrVal);

        g2.setColor(c);
        Path2D p = new Path2D.Double();

        for (int xx = sx; xx <= ex; xx++) {
            double t = timeSec + (xx - sx) * pxDt;
            double u = (t % period) / period;

            double v;
            if      (u < 0.12) v = 0.2 * Math.sin(u / 0.12 * Math.PI);
            else if (u < 0.17) v = -1.5;
            else if (u < 0.19) v = 3.5;
            else if (u < 0.25) v = -1.0;
            else               v = 0.3 * Math.sin((u - 0.25) / 0.75 * Math.PI * 2);

            double yy = cy - v * 8;
            if (xx == sx) p.moveTo(xx, yy); else p.lineTo(xx, yy);
        }
        g2.setStroke(new BasicStroke(2f));
        g2.draw(p);
    }

    private void drawWaveSPO2(Graphics2D g2, int x, int w, int cy, int hrVal) {
        Color c = new Color(60, 170, 230);
        int sx = x + 20;
        int ex = x + w - 20;

        if (hrVal <= 0) {
            drawFlatLine(g2, x, w, cy, c);
            return;
        }

        double pixels = Math.max(1, ex - sx);
        double pxDt   = WINDOW_SEC / pixels;
        double period = (60.0 / Math.max(1, hrVal)) * 2.0;

        g2.setColor(c);
        Path2D p = new Path2D.Double();

        for (int xx = sx; xx <= ex; xx++) {
            double t = timeSec + (xx - sx) * pxDt;
            double u = (t % period) / period;
            double theta = 2 * Math.PI * u;

            double yy = cy - Math.sin(theta) * 6;
            if (xx == sx) p.moveTo(xx, yy); else p.lineTo(xx, yy);
        }
        g2.setStroke(new BasicStroke(2f));
        g2.draw(p);
    }

    private void drawWaveResp(Graphics2D g2, int x, int w, int cy, int rrVal) {
        Color c = new Color(255, 215, 60);
        int sx = x + 20;
        int ex = x + w - 20;

        if (rrVal <= 0) {
            drawFlatLine(g2, x, w, cy, c);
            return;
        }

        double pixels = Math.max(1, ex - sx);
        double pxDt   = WINDOW_SEC / pixels;
        double period = 60.0 / Math.max(1, rrVal);

        g2.setColor(c);
        Path2D p = new Path2D.Double();

        for (int xx = sx; xx <= ex; xx++) {
            double t = timeSec + (xx - sx) * pxDt;
            double u = (t % period) / period;
            double theta = 2 * Math.PI * u;

            double base = Math.sin(theta);
            double mod  = 0.2 * Math.sin(2 * theta);
            double yy   = cy - (base + mod) * 7;

            if (xx == sx) p.moveTo(xx, yy); else p.lineTo(xx, yy);
        }
        g2.setStroke(new BasicStroke(2f));
        g2.draw(p);
    }

    private void drawWaveBP(Graphics2D g2, int x, int w, int cy, int sbpVal, int dbpVal) {
        Color c = new Color(255, 140, 60);
        int sx = x + 20;
        int ex = x + w - 20;

        if (sbpVal <= 0 || dbpVal <= 0 || hr <= 0) {
            drawFlatLine(g2, x, w, cy, c);
            return;
        }

        double pixels = Math.max(1, ex - sx);
        double pxDt   = WINDOW_SEC / pixels;
        double period = 60.0 / Math.max(1, hr);

        double baseAmp = Math.max(4, Math.min(12, (sbpVal - dbpVal) * 0.12));

        g2.setColor(c);
        Path2D p = new Path2D.Double();

        for (int xx = sx; xx <= ex; xx++) {
            double t = timeSec + (xx - sx) * pxDt;
            double u = (t % period) / period;

            double up    = Math.exp(-u * 6.0);
            double main  = Math.sin(Math.min(u * Math.PI * 3, Math.PI));
            double notch = 0.2 * Math.exp(-Math.pow((u - 0.4) / 0.06, 2));

            double v  = main * up - notch;
            double yy = cy - v * baseAmp;

            if (xx == sx) p.moveTo(xx, yy); else p.lineTo(xx, yy);
        }
        g2.setStroke(new BasicStroke(2f));
        g2.draw(p);
    }

    // 우측
    private void drawRight(Graphics2D g2, int x, int top, int w) {
        int cx = x + 14;

        g2.setColor(new Color(0, 225, 100));
        g2.setFont(new Font("Arial", Font.BOLD, 12));
        g2.drawString("HR", cx, rowCenterY[0] - 20);
        g2.setFont(new Font("Arial", Font.BOLD, 40));
        g2.drawString(String.valueOf(hr), cx, rowCenterY[0] + 15);

        g2.setColor(new Color(60, 170, 230));
        g2.setFont(new Font("Arial", Font.BOLD, 12));
        g2.drawString("SpO2", cx, rowCenterY[1] - 20);
        g2.setFont(new Font("Arial", Font.BOLD, 40));
        g2.drawString(String.valueOf(spo2), cx, rowCenterY[1] + 15);

        g2.setColor(new Color(255, 215, 60));
        g2.setFont(new Font("Arial", Font.BOLD, 12));
        g2.drawString("RR", cx, rowCenterY[2] - 20);
        g2.setFont(new Font("Arial", Font.BOLD, 40));
        g2.drawString(String.valueOf(rr), cx, rowCenterY[2] + 15);

        g2.setColor(new Color(255, 140, 60));
        g2.setFont(new Font("Arial", Font.BOLD, 12));
        g2.drawString("BP", cx, rowCenterY[3] - 20);

        g2.setFont(new Font("Arial", Font.BOLD, 34));
        String sbps = sbp + "/" + dbp;
        g2.drawString(sbps, cx, rowCenterY[3] + 10);

        int rightEdge = x + w - 12;
        g2.setColor(new Color(245, 246, 250));
        g2.setFont(new Font("Arial", Font.BOLD, 12));
        g2.drawString("TEMP", rightEdge - 45, top + 50);

        g2.setFont(new Font("Arial", Font.BOLD, 32));
        String st = String.format("%.1f", temp);
        int tw = g2.getFontMetrics().stringWidth(st);
        g2.drawString(st, rightEdge - tw, top + 85);

        g2.setFont(new Font("Arial", Font.BOLD, 24));
        String sm  = "(" + map + ")";
        int   smW = g2.getFontMetrics().stringWidth(sm);
        int   smX = rightEdge - smW;
        int   smY = top + 140;
        g2.drawString(sm, smX, smY);
    }

    // 하단
    private void drawBottom(Graphics2D g2, int x, int y, int w, int h) {
        g2.setColor(new Color(26, 63, 84));
        g2.fillRect(x, y, w, h);

        g2.setFont(Theme.BTN2);
        g2.setColor(new Color(214, 81, 94));
        String alarmStr = "ALARM: " + alarmText;
        FontMetrics fm = g2.getFontMetrics();
        int ay = y + (h + fm.getAscent() - fm.getDescent()) / 2;
        g2.drawString(alarmStr, x + 14, ay);

        int bw = 100, bh = 32, gap = 12;
        int bx3 = x + w - bw - 12;
        int bx2 = bx3 - bw - gap;
        int bx1 = bx2 - bw - gap;

        defR  = new Rectangle(bx1, y + (h - bh) / 2, bw, bh);
        charR = new Rectangle(bx2, y + (h - bh) / 2, bw, bh);
        backR = new Rectangle(bx3, y + (h - bh) / 2, bw, bh);

        drawBtn(g2, defR,  "DEFAULT");
        drawBtn(g2, charR, "CHARACTER");
        drawBtn(g2, backR, "BACK");
    }

    private void drawBtn(Graphics2D g2, Rectangle r, String s) {
        g2.setColor(Theme.BTNBACK);
        g2.fillRoundRect(r.x, r.y, r.width, r.height, 16, 16);

        g2.setColor(Theme.FG);
        g2.setStroke(new BasicStroke(2f));
        g2.drawRoundRect(r.x, r.y, r.width, r.height, 16, 16);

        g2.setFont(Theme.BTN2);
        FontMetrics fm = g2.getFontMetrics();
        int tx = r.x + (r.width  - fm.stringWidth(s)) / 2;
        int ty = r.y + (r.height + fm.getAscent() - fm.getDescent()) / 2;
        g2.drawString(s, tx, ty);
    }

    // 공통
    private void drawCenter(Graphics g, String text, int cx, int baselineY) {
        FontMetrics fm = g.getFontMetrics();
        g.drawString(text, cx - fm.stringWidth(text) / 2, baselineY);
    }

    // SET
    public void setPatientInfo(String name, String sex, int age) {
        this.patientName = (name == null ? "NAME" : name);
        this.patientSex  = (sex  == null ? "MF"   : sex);
        this.patientAge  = Math.max(0, age);
        repaint();
    }
    public void setAlarmText(String text) {
        this.alarmText = (text == null || text.isBlank()) ? "NONE" : text;
        repaint();
    }
    public String getHeaderPatient() {
        return "PATIENT: " + patientName + " " + patientSex + " / " + patientAge;
    }
}
