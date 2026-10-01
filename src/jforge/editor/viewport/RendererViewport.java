package jforge.editor.viewport;

import jforge.editor.debug.DebugPanel;
import jforge.engine.rendering.camera.CameraController;
import jforge.engine.rendering.Renderer;

import javax.swing.BorderFactory;
import javax.swing.JPanel;

import java.awt.BorderLayout;
import java.awt.Component;

public class RendererViewport
        extends JPanel {

    private Renderer renderer;

    private final CameraController
            cameraController;

    private DebugPanel debugPanel;

    public RendererViewport(
            Renderer renderer,
            CameraController cameraController) {

        this.cameraController =
                cameraController;

        setLayout(
                new BorderLayout()
        );

        setBorder(
                BorderFactory.createTitledBorder(
                        "Scene"
                )
        );

        setRenderer(
                renderer
        );
    }

    public void setRenderer(
            Renderer renderer) {

        if (renderer == null) {
            return;
        }

        /*
         * Quitar listeners del renderer
         * anterior.
         */
        if (this.renderer != null) {

            Component oldComponent =
                    this.renderer
                            .getViewComponent();

            oldComponent.removeKeyListener(cameraController);
        }

        /*
         * Parar debug anterior.
         */
        if (debugPanel != null) {

            debugPanel.dispose();

            debugPanel =
                    null;
        }

        removeAll();

        this.renderer =
                renderer;

        /*
         * Actualizar cámara.
         */
        cameraController.setCamera(
                renderer.getCamera()
        );

        /*
         * Conectar teclado.
         */
        Component component =
                renderer.getViewComponent();

        component.addKeyListener(
                cameraController
        );


        component.setFocusable(
                true
        );

        /*
         * Debug.
         */
        debugPanel =
                new DebugPanel(
                        renderer.getRenderStats(),
                        renderer.getBackend()
                );

        /*
         * Añadir viewport.
         */
        add(
                component,
                BorderLayout.CENTER
        );

        /*
         * Añadir estadísticas.
         */
        add(
                debugPanel,
                BorderLayout.SOUTH
        );

        revalidate();

        repaint();

        component.requestFocusInWindow();
    }

    public Renderer getRenderer() {

        return renderer;
    }

    public void focusViewport() {

        if (renderer == null) {
            return;
        }

        renderer
                .getViewComponent()
                .requestFocusInWindow();
    }
}