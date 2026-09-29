package jforge.editor;


import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;

public class InspectorPanel extends JPanel {

    public InspectorPanel() {

        setLayout(new BorderLayout());

        add(new JLabel("Inspector"), BorderLayout.NORTH);
    }
}
