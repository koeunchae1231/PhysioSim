// src/physiosim/ui/views/MainView.java
package physiosim.ui.views;

import physiosim.ui.Theme;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class MainView extends JPanel {
    // 하단 일러스트
    private final Image img = new ImageIcon(
        getClass().getResource("/images/dissection.png")
    ).getImage();

    // START 버튼 히트 영역
    private Rectangle startRect;
    private final Runnable onStart;

    public MainView() { this(null); }
    public MainView(Runnable onStart) {
        this.onStart = onStart;
        setOpaque(false);

        // 클릭/호버 처리
        MouseAdapter m = new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (startRect != null && startRect.contains(e.getPoint())) {
                    if (onStart != null) onStart.run();
                }
            }
            @Override public void mouseMoved(MouseEvent e) {
                boolean hover = startRect != null && startRect.contains(e.getPoint());
                setCursor(hover ? Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
                                : Cursor.getDefaultCursor());
            }
        };
        addMouseListener(m);
        addMouseMotionListener(m);
    }

    @Override protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();

        // 안티앨리어싱
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // 남색 둥근 배경
        g2.setColor(Theme.BG);
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), Theme.RADIUS, Theme.RADIUS);

        // 제목
        g2.setColor(Theme.FG);
        g2.setFont(Theme.TITLE);
        drawCenter(g2, "PHYSIOSIM", getWidth()/2, 80);

        // START 버튼
        int bw = 140, bh = 56;
        int bx = (getWidth() - bw) / 2;
        int by = 120;
        startRect = new Rectangle(bx, by, bw, bh);

        g2.setColor(Theme.BTNBACK);
        g2.fillRoundRect(bx, by, bw, bh, 16, 16);

        g2.setColor(Theme.FG);
        g2.setStroke(new BasicStroke(2f));
        g2.drawRoundRect(bx, by, bw, bh, 16, 16);

        g2.setFont(Theme.BTN1);
        int baseY = by + (bh + g2.getFontMetrics().getAscent() - g2.getFontMetrics().getDescent()) / 2;
        drawCenter(g2, "START", getWidth()/2, baseY);

        // 하단 이미지(바닥에 맞춰 스케일)
        int iw = img.getWidth(this), ih = img.getHeight(this);
        if (iw > 0 && ih > 0) {
            int availW = getWidth() - 60;   // 좌우 여백
            int availH = getHeight() - 220; // 상단 텍스트 영역 제외
            double s = Math.min(availW / (double) iw, availH / (double) ih);
            int w = (int) (iw * s), h = (int) (ih * s);
            int x = (getWidth() - w) / 2;
            int y = getHeight() - h;        // 바닥에 딱 붙임
            g2.drawImage(img, x, y, w, h, this);
        }

        g2.dispose();
    }

    // 중앙 정렬 텍스트 유틸
    private void drawCenter(Graphics2D g2,String text,int cx,int y){
        FontMetrics fm = g2.getFontMetrics();
        int x = cx - fm.stringWidth(text) / 2;
        g2.drawString(text, x, y);
    }
}
