// src/physiosim/ui/Navigator.java
package physiosim.ui;

import physiosim.control.Simulation;
import physiosim.ui.views.VitalView;
import physiosim.ui.views.CharacterView;

import javax.swing.*;
import java.awt.*;

public class Navigator {
// 연동 담당
    public enum Screen {
        VITAL,
        CHARACTER
    }

    private final JPanel     rootPanel;
    private final CardLayout cardLayout;

    private final Simulation   simulation;
    private final VitalView    vitalView;
    private final CharacterView characterView;

    public Navigator(Runnable onBackFromVital,
                     Runnable onBackFromCharacter,
                     Runnable onCommandFromCharacter) {

        cardLayout = new CardLayout();
        rootPanel  = new JPanel(cardLayout);

        simulation = Simulation.createDefault();

        vitalView = new VitalView(
                onBackFromVital,
                this::showCharacter,
                null
        );
        vitalView.bindSimulation(simulation);

        characterView = new CharacterView(
                onBackFromCharacter,
                this::showVital,
                onCommandFromCharacter
        );
        characterView.bindSimulation(simulation);
        characterView.setHeaderPatient(vitalView.getHeaderPatient());

        rootPanel.add(vitalView,     Screen.VITAL.name());
        rootPanel.add(characterView, Screen.CHARACTER.name());
    }


    public JPanel getRootPanel() { return rootPanel; }
    public VitalView getVitalView() { return vitalView; }
    public CharacterView getCharacterView() { return characterView; }
    public Simulation getSimulation() { return simulation; }
    
    public void showVital() { cardLayout.show(rootPanel, Screen.VITAL.name()); }
    public void showCharacter() {
        characterView.setHeaderPatient(vitalView.getHeaderPatient());
        cardLayout.show(rootPanel, Screen.CHARACTER.name());
    }
}
