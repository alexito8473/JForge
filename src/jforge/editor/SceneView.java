package jforge.editor;

import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Graphics;

public class SceneView extends JPanel {

    public SceneView() {

        setBackground(Color.DARK_GRAY);
    }

    @Override
    protected void paintComponent(Graphics g) {

        super.paintComponent(g);

        g.setColor(Color.WHITE);

        g.fillRect(100, 100, 64, 64);
    }
}