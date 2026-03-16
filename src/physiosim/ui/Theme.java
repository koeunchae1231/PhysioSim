// src/physiosim/ui/Theme.java
package physiosim.ui;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.text.JTextComponent;

public final class Theme {
    private Theme() {}

    // 색상
    public static final Color BG      = new Color(21, 50, 67);   // 배경 - 딥 블루
    public static final Color FG      = new Color(235, 222, 195); // 텍스트 - 베이지
    public static final Color PANEL   = new Color(26, 63, 84);   // 카드/필드 배경
    public static final Color BTNBACK = new Color(31, 70, 90);   // 버튼 배경

    // 메시지
    public static final Color ERROR   = new Color(196, 85, 85);
    public static final Color WARNING = new Color(206,165, 78);
    public static final Color SUCCESS = new Color(102,168,120);

    // 폰트
    public static final Font TITLE   = new Font("Arial", Font.BOLD, 28);
    public static final Font LABEL   = new Font("Arial", Font.PLAIN, 13);
    public static final Font FIELD   = new Font("Arial", Font.PLAIN, 14);
    public static final Font BTN1    = new Font("Arial", Font.PLAIN, 18);
    public static final Font BTN2    = new Font("Arial", Font.BOLD, 14);

    // 한글 전용 폰트 (리포트 등)
    public static final Font KR      = new Font("맑은 고딕", Font.PLAIN, 11);
    public static final Font KR_BOLD = new Font("맑은 고딕", Font.BOLD, 13);

    // 간격 및 패딩
    public static final int PADDING = 24;  // 화면 기본 패딩
    public static final int RADIUS  = 48;  // 큰 카드/프레임 라운드 반경
    public static final int GAP_XS  = 4;
    public static final int GAP_SM  = 6;
    public static final int GAP_MD  = 12;
    public static final int GAP_LG  = 18;

    // Root 패널
    public static JPanel root() {
        JPanel p = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.setColor(BG);
                g.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(PADDING, PADDING, PADDING, PADDING));
        return p;
    }

    // 아웃라인 버튼
    public static JButton outline(String text) {
        JButton b = new JButton(text);
        b.setFont(BTN1);
        b.setForeground(FG);
        b.setBackground(BG);
        b.setFocusPainted(false);

        b.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(FG, 1),
                new EmptyBorder(10, 16, 10, 16)
        ));

        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    // 카드형 필드 (포커스 시 라벨 자동 숨김)
    public static JPanel fieldAutoHide(String label, JTextComponent input) {
        JPanel wrap = new JPanel(new BorderLayout(0, GAP_SM));
        wrap.setOpaque(false);

        JLabel l = new JLabel(label);
        l.setFont(LABEL);
        l.setForeground(FG);
        wrap.add(l, BorderLayout.NORTH);

        JPanel holder = new JPanel(new BorderLayout());
        holder.setBackground(PANEL);
        holder.setBorder(new EmptyBorder(10, 12, 10, 12));

        styleInput(input);
        holder.add(input, BorderLayout.CENTER);
        wrap.add(holder, BorderLayout.CENTER);

        input.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent e) {
                if (l.isVisible()) {
                    l.setVisible(false);
                    wrap.revalidate();
                    wrap.repaint();
                }
            }

            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                if (isEmpty(input) && !l.isVisible()) {
                    l.setVisible(true);
                    wrap.revalidate();
                    wrap.repaint();
                }
            }
        });

        return wrap;
    }

    private static void styleInput(JTextComponent input) {
        input.setFont(FIELD);
        input.setForeground(FG);
        input.setOpaque(false);
        input.setBorder(BorderFactory.createEmptyBorder());

        if (input instanceof JPasswordField pf) {
            pf.setCaretColor(BG);
        } else if (input instanceof JTextField tf) {
            tf.setCaretColor(BG);
        }
    }

    private static boolean isEmpty(JTextComponent tc) {
        String s = tc.getText();
        return s == null || s.trim().isEmpty();
    }

    // 세로 스택 유틸
    public static JPanel vstack(int gap) {
        JPanel p = new JPanel();
        p.setOpaque(false);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.putClientProperty("gap", gap);
        return p;
    }

    public static void vadd(JPanel p, Component c) {
        Object g = p.getClientProperty("gap");
        int gap = (g instanceof Integer) ? (Integer) g : 0;
        if (p.getComponentCount() > 0 && gap > 0) {
            p.add(Box.createVerticalStrut(gap));
        }
        p.add(c);
    }
}
