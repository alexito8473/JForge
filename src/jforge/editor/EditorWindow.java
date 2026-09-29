package jforge.editor;

import javax.swing.*;
import java.awt.*;

public class EditorWindow extends JFrame {
    public EditorWindow() {

        setTitle("JForge");

        setSize(1200, 700);

        setLocationRelativeTo(null);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        createInterface();
    }

    private void createInterface() {

        setLayout(new BorderLayout());

        // Panel izquierdo
        JPanel hierarchyContainer = new JPanel(new BorderLayout());

        hierarchyContainer.add(
                new JLabel("  HIERARCHY"),
                BorderLayout.NORTH
        );

        hierarchyContainer.add(
                new HierarchyPanel(),
                BorderLayout.CENTER
        );

        // Centro
        SceneView sceneView = new SceneView();

        // Panel derecho
        JPanel inspectorContainer = new JPanel(new BorderLayout());

        inspectorContainer.add(
                new JLabel("  INSPECTOR"),
                BorderLayout.NORTH
        );

        inspectorContainer.add(
                new InspectorPanel(),
                BorderLayout.CENTER
        );

        // Izquierda + centro
        JSplitPane leftSplit = new JSplitPane(
                JSplitPane.HORIZONTAL_SPLIT,
                hierarchyContainer,
                sceneView
        );

        leftSplit.setDividerLocation(200);

        // Todo + inspector
        JSplitPane mainSplit = new JSplitPane(
                JSplitPane.HORIZONTAL_SPLIT,
                leftSplit,
                inspectorContainer
        );

        mainSplit.setDividerLocation(900);

        add(mainSplit, BorderLayout.CENTER);
    }
}
