// src/physiosim/ui/App.java
package physiosim.ui;

import physiosim.db.Database;
import physiosim.db.UserRepository;
import physiosim.db.CharacterRepository;
import physiosim.db.CharacterRepository.CharacterRow;
import physiosim.ui.views.*;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.sql.Connection;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class App {

    private static final int SMALL_W = 440,  SMALL_H = 620;
    private static final int LARGE_W = 1000, LARGE_H = 740;
    private static final int RADIUS  = Theme.RADIUS;

    private JFrame window;

    private Connection        conn;
    private UserRepository    userRepo;
    private CharacterRepository characterRepo;

    private String currentUserId;

    private Navigator navigator;

    // main
    public static void main(String[] args) {
        System.out.println("사용되는 DB 파일 위치(상대경로 기반): "
                + new java.io.File("physiosim.db").getAbsolutePath());

        SwingUtilities.invokeLater(() -> new App().start());
    }

    private void start() {
        initDb();

        SplashView splash = new SplashView(this::showHome);
        splash.setVisible(true);
    }

    // DB
    private void initDb() {
        try {
            conn          = Database.getConnection();
            userRepo      = new UserRepository(conn);
            characterRepo = new CharacterRepository(conn);
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(
                    null,
                    "Database 연결에 실패했습니다!",
                    "Error",
                    JOptionPane.ERROR_MESSAGE
            );
            System.exit(1);
        }
    }

    // 공통
    private JFrame ensureWindow() {
        if (window == null) {
            window = new JFrame();
            window.setUndecorated(true);
            window.setBackground(new Color(0, 0, 0, 0));
            window.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        }
        return window;
    }

    private void applySmallWindow() {
        JFrame f = ensureWindow();
        f.setSize(SMALL_W, SMALL_H);
        f.setShape(new RoundRectangle2D.Float(0, 0, SMALL_W, SMALL_H, RADIUS, RADIUS));
        f.setLocationRelativeTo(null);
    }

    private void applyLargeWindow() {
        JFrame f = ensureWindow();
        f.setSize(LARGE_W, LARGE_H);
        f.setShape(null);
        f.setLocationRelativeTo(null);
    }

    private void showInSmallWindow(JComponent view) {
        applySmallWindow();
        JFrame f = ensureWindow();
        f.setContentPane(view);
        f.revalidate();
        f.repaint();
        f.setVisible(true);
    }

    private void showInLargeWindow(JComponent view) {
        applyLargeWindow();
        JFrame f = ensureWindow();
        f.setContentPane(view);
        f.revalidate();
        f.repaint();
        f.setVisible(true);
    }

    // 1. 홈
    private void showHome() {
        HomeView home = new HomeView(this::showLogin, this::showSignup);
        showInSmallWindow(home);
    }

    // 2. 로그인
    private void showLogin() {
        LoginView login = new LoginView(
                this::showHome,
                this::showSignup,
                (id, pwChars) -> {
                    String pw = new String(pwChars);
                    boolean ok = userRepo.login(id, pw);

                    java.util.Arrays.fill(pwChars, '\0');

                    if (ok) {
                        currentUserId = id;
                        JOptionPane.showMessageDialog(
                                window,
                                "로그인 성공!",
                                "Login",
                                JOptionPane.INFORMATION_MESSAGE
                        );
                        showMain();
                    } else {
                        JOptionPane.showMessageDialog(
                                window,
                                "ID 또는 PASSWORD가 잘못되었습니다.",
                                "Login Failed",
                                JOptionPane.ERROR_MESSAGE
                        );
                    }
                }
        );
        showInSmallWindow(login);
    }

    // 2-1. 회원가입
    private void showSignup() {
        SignupView signup = new SignupView(
                this::showHome,
                this::showLogin,
                data -> {
                    boolean ok = userRepo.register(
                            data.id,
                            data.email,
                            new String(data.password),
                            data.role
                    );

                    java.util.Arrays.fill(data.password, '\0');

                    if (ok) {
                        JOptionPane.showMessageDialog(
                                window,
                                "회원가입이 완료되었습니다!",
                                "Sign Up",
                                JOptionPane.INFORMATION_MESSAGE
                        );
                        showLogin();
                    } else {
                        JOptionPane.showMessageDialog(
                                window,
                                "이미 사용 중인 ID 또는 이메일입니다.",
                                "Sign Up Failed",
                                JOptionPane.ERROR_MESSAGE
                        );
                    }
                }
        );
        showInSmallWindow(signup);
    }

    // 3. 메인
    private void showMain() {
        MainView main = new MainView(this::showPersonal);
        showInSmallWindow(main);
    }

    // 4. 개인
    private void showPersonal() {
        PersonalView personal = new PersonalView(
                this::showCharacterCreate,
                this::showList,
                this::showAccount
        );
        showInSmallWindow(personal);
    }

    // 4-1. CC
    private void showCharacterCreate() {
        CharacterCreateView ccv = new CharacterCreateView(
                this::showPersonal,
                data -> {
                    if (currentUserId == null || currentUserId.isBlank()) {
                        JOptionPane.showMessageDialog(
                                window,
                                "로그인 정보가 없습니다. 다시 로그인해 주세요.",
                                "Error",
                                JOptionPane.ERROR_MESSAGE
                        );
                        showLogin();
                        return;
                    }

                    // 출생일(YYYYMMDD) 검증
                    String birthRaw = data.birth;
                    if (birthRaw == null || birthRaw.isBlank()) {
                        JOptionPane.showMessageDialog(
                                window,
                                "BIRTH(출생일)을 입력해 주세요. 예: 20030315",
                                "입력 오류",
                                JOptionPane.WARNING_MESSAGE
                        );
                        return;
                    }

                    String birthDigits = birthRaw.replaceAll("[^0-9]", "");
                    if (birthDigits.length() != 8) {
                        JOptionPane.showMessageDialog(
                                window,
                                "BIRTH는 YYYYMMDD 형식의 8자리 숫자로 입력해야 합니다.\n예: 20030315",
                                "입력 오류",
                                JOptionPane.WARNING_MESSAGE
                        );
                        return;
                    }

                    Double height = null;
                    Double weight = null;
                    try {
                        if (data.height != null && !data.height.isBlank()) {
                            height = Double.parseDouble(data.height);
                        }
                        if (data.weight != null && !data.weight.isBlank()) {
                            weight = Double.parseDouble(data.weight);
                        }
                    } catch (NumberFormatException ex) {
                        JOptionPane.showMessageDialog(
                                window,
                                "HEIGHT와 WEIGHT는 숫자로 입력해 주세요.",
                                "입력 오류",
                                JOptionPane.WARNING_MESSAGE
                        );
                        return;
                    }

                    String sex = data.gender;

                    try {
                        int newId = characterRepo.insert(
                                currentUserId,
                                data.name,
                                sex,
                                birthDigits,
                                height,
                                weight
                        );

                        if (newId > 0) {
                            System.out.println("CREATE CHARACTER id=" + newId);
                            showList();
                        } else {
                            JOptionPane.showMessageDialog(
                                    window,
                                    "캐릭터 생성에 실패했습니다.",
                                    "Error",
                                    JOptionPane.ERROR_MESSAGE
                            );
                        }

                    } catch (Exception ex) {
                        ex.printStackTrace();
                        JOptionPane.showMessageDialog(
                                window,
                                "캐릭터 생성 중 오류가 발생했습니다.",
                                "Error",
                                JOptionPane.ERROR_MESSAGE
                        );
                    }
                }
        );
        showInSmallWindow(ccv);
    }

    // 4-2. list
    private void showList() {
        List<String>      items = Collections.emptyList();
        List<CharacterRow> rows = Collections.emptyList();

        if (currentUserId != null && !currentUserId.isBlank()) {
            try {
                rows = characterRepo.findByOwner(currentUserId);
                List<String> labels = new ArrayList<>();

                for (CharacterRow r : rows) {
                    String label = r.name();

                    if (r.sex() != null) {
                        label += " (" + r.sex() + ")";
                    }
                    if (r.heightCm() != null && r.weightKg() != null) {
                        label += " - " + r.heightCm() + "cm / " + r.weightKg() + "kg";
                    }

                    labels.add(label);
                }
                items = labels;

            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(
                        window,
                        "캐릭터 목록을 불러오는 중 오류가 발생했습니다.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE
                );
            }
        }

        final List<CharacterRow> rowsFinal   = rows;
        final List<String>       labelsFinal = items;

        ListView lv = new ListView(
                this::showPersonal,
                items,
                label -> {
                    int idx = labelsFinal.indexOf(label);
                    if (idx >= 0 && idx < rowsFinal.size()) {
                        CharacterRow row = rowsFinal.get(idx);
                        showVitals(row);
                    }
                }
        );
        showInSmallWindow(lv);
    }

    // 4-3. account
    private void showAccount() {
        AccountView av = new AccountView(
                this::showPersonal,
                () -> System.out.println("LOG OUT (not implemented)"),
                () -> System.out.println("DELETE ACCOUNT (not implemented)")
        );
        showInSmallWindow(av);
    }

    // 5. 바이탈, 캐릭터
    private void showVitals(CharacterRow row) {
        if (navigator == null) {
            navigator = new Navigator(
                    this::showList,
                    this::showList,
                    () -> System.out.println("COMMAND")
            );
        }

        String name  = row.name();
        String sex   = row.sex();
        String birth = row.birth();
        Double hCm   = row.heightCm();
        Double wKg   = row.weightKg();

        String sexCode = "MF";
        if (sex != null) {
            if ("MALE".equalsIgnoreCase(sex) || "M".equalsIgnoreCase(sex)) {
                sexCode = "M";
            } else if ("FEMALE".equalsIgnoreCase(sex) || "F".equalsIgnoreCase(sex)) {
                sexCode = "F";
            }
        }

        int age = calcAgeFromBirth(birth);

        VitalView vv = navigator.getVitalView();
        vv.setPatientInfo(name, sexCode, age);
        vv.setAlarmText("NONE");

        CharacterView cv = navigator.getCharacterView();

        int    heightInt  = 0;
        double weightVal  = 0.0;
        double bmi        = 0.0;

        if (hCm != null) {
            heightInt = hCm.intValue();
        }
        if (wKg != null) {
            weightVal = wKg;
        }
        if (hCm != null && wKg != null && hCm > 0) {
            double hM = hCm / 100.0;
            bmi = wKg / (hM * hM);
        }

        cv.setBodyInfo(heightInt, weightVal, bmi);
        cv.setHeaderPatient(vv.getHeaderPatient());

        applyLargeWindow();
        JFrame f = ensureWindow();
        f.setContentPane(navigator.getRootPanel());
        f.revalidate();
        f.repaint();
        f.setVisible(true);

        navigator.showVital();
    }

    private int calcAgeFromBirth(String birth) {
        if (birth == null || birth.isBlank()) return 0;

        String digits = birth.replaceAll("[^0-9]", "");
        if (digits.length() != 8) return 0;

        try {
            int year  = Integer.parseInt(digits.substring(0, 4));
            int month = Integer.parseInt(digits.substring(4, 6));
            int day   = Integer.parseInt(digits.substring(6, 8));

            LocalDate now       = LocalDate.now();
            LocalDate birthDate = LocalDate.of(year, month, day);

            int age = now.getYear() - year;

            if (now.getMonthValue() < month ||
               (now.getMonthValue() == month && now.getDayOfMonth() < day)) {
                age--;
            }

            if (age < 0 || age > 120) return 0;
            return age;
        } catch (Exception e) {
            return 0;
        }
    }
}
