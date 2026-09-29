package jforge.engine.rendering;

import javax.swing.JFrame;

public class Window extends JFrame {

    public Window(
            Renderer3D renderer) {

        super("JForge Engine");

        setSize(1280, 720);

        setLocationRelativeTo(null);

        setDefaultCloseOperation(
                JFrame.EXIT_ON_CLOSE
        );

        add(renderer);
    }
}