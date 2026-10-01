package jforge.editor.debug;

import jforge.engine.debug.RenderStats;
import jforge.engine.rendering.RenderBackend;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.Timer;

import java.awt.FlowLayout;

public class DebugPanel
        extends JPanel {

    private final RenderStats stats;

    private final RenderBackend backend;

    private final JLabel fpsLabel;

    private final JLabel frameLabel;

    private final JLabel clearLabel;

    private final JLabel renderLabel;

    private final JLabel objectsLabel;

    private final JLabel trianglesLabel;

    private final JLabel totalFramesLabel;

    private final JLabel backendLabel;

    private final Timer timer;

    public DebugPanel(
            RenderStats stats,
            RenderBackend backend) {

        this.stats =
                stats;

        this.backend =
                backend;

        setLayout(
                new FlowLayout(
                        FlowLayout.LEFT,
                        12,
                        4
                )
        );

        setBorder(
                BorderFactory.createTitledBorder(
                        "Statistics"
                )
        );

        fpsLabel =
                new JLabel(
                        "FPS: 0"
                );

        frameLabel =
                new JLabel(
                        "Frame: 0.00 ms"
                );

        clearLabel =
                new JLabel(
                        "Clear: 0.00 ms"
                );

        renderLabel =
                new JLabel(
                        "Render: 0.00 ms"
                );

        objectsLabel =
                new JLabel(
                        "Objects: 0"
                );

        trianglesLabel =
                new JLabel(
                        "Triangles: 0"
                );

        totalFramesLabel =
                new JLabel(
                        "Frames: 0"
                );

        backendLabel =
                new JLabel(
                        "Renderer: " +
                                backend
                );

        add(
                fpsLabel
        );

        add(
                frameLabel
        );

        add(
                clearLabel
        );

        add(
                renderLabel
        );

        add(
                objectsLabel
        );

        add(
                trianglesLabel
        );

        add(
                totalFramesLabel
        );

        add(
                backendLabel
        );

        /*
         * Actualizar el panel cada 100 ms.
         */
        timer =
                new Timer(
                        100,
                        event ->
                                updateStats()
                );

        timer.start();
    }

    private void updateStats() {

        if (stats == null) {
            return;
        }

        fpsLabel.setText(
                "FPS: " +
                        stats.getFPS()
        );

        frameLabel.setText(
                String.format(
                        "Frame: %.2f ms",
                        stats.getFrameTime()
                )
        );

        clearLabel.setText(
                String.format(
                        "Clear: %.2f ms",
                        stats.getClearTime()
                )
        );

        renderLabel.setText(
                String.format(
                        "Render: %.2f ms",
                        stats.getRenderTime()
                )
        );

        objectsLabel.setText(
                "Objects: " +
                        stats.getObjects()
        );

        trianglesLabel.setText(
                "Triangles: " +
                        stats.getTriangles()
        );

        totalFramesLabel.setText(
                "Frames: " +
                        stats.getTotalFrames()
        );
    }

    public void dispose() {

        timer.stop();
    }
}