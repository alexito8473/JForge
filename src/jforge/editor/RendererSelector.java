package jforge.editor;

import jforge.engine.rendering.RenderBackend;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridLayout;

public class RendererSelector
        extends JDialog {

    private RenderBackend selectedBackend;

    private boolean accepted;

    public RendererSelector(
            Frame owner) {

        super(
                owner,
                "JForge - Renderer",
                Dialog.ModalityType.APPLICATION_MODAL
        );

        selectedBackend =
                RenderBackend.SOFTWARE;

        accepted =
                false;

        buildUI();

        setSize(
                420,
                240
        );

        setLocationRelativeTo(
                owner
        );

        setResizable(
                false
        );
    }

    private void buildUI() {

        setLayout(
                new BorderLayout(
                        10,
                        10
                )
        );

        JLabel title =
                new JLabel(
                        "Selecciona el renderer de JForge"
                );

        title.setBorder(
                BorderFactory.createEmptyBorder(
                        15,
                        15,
                        5,
                        15
                )
        );

        add(
                title,
                BorderLayout.NORTH
        );

        JPanel options =
                new JPanel(
                        new GridLayout(
                                2,
                                1,
                                5,
                                5
                        )
                );

        options.setBorder(
                BorderFactory.createEmptyBorder(
                        10,
                        25,
                        10,
                        25
                )
        );

        JRadioButton software =
                new JRadioButton(
                        "Software CPU - Rasterizer Java"
                );

        JRadioButton openGL =
                new JRadioButton(
                        "OpenGL GPU - Aceleración gráfica"
                );

        ButtonGroup group =
                new ButtonGroup();

        group.add(
                software
        );

        group.add(
                openGL
        );

        software.setSelected(
                true
        );

        software.addActionListener(
                event -> {

                    selectedBackend =
                            RenderBackend.SOFTWARE;
                }
        );

        openGL.addActionListener(
                event -> {

                    selectedBackend =
                            RenderBackend.OPENGL;
                }
        );

        options.add(
                software
        );

        options.add(
                openGL
        );

        add(
                options,
                BorderLayout.CENTER
        );

        JPanel buttons =
                new JPanel(
                        new FlowLayout(
                                FlowLayout.RIGHT
                        )
                );

        JButton cancel =
                new JButton(
                        "Cancelar"
                );

        JButton start =
                new JButton(
                        "Crear Editor"
                );

        cancel.addActionListener(
                event -> {

                    accepted =
                            false;

                    dispose();
                }
        );

        start.addActionListener(
                event -> {

                    accepted =
                            true;

                    dispose();
                }
        );

        buttons.add(
                cancel
        );

        buttons.add(
                start
        );

        add(
                buttons,
                BorderLayout.SOUTH
        );

        getRootPane().setDefaultButton(
                start
        );
    }

    public RenderBackend getSelectedBackend() {

        return selectedBackend;
    }

    public boolean isAccepted() {

        return accepted;
    }
}