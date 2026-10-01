package jforge.engine.runtime;

import jforge.engine.rendering.camera.Camera;
import jforge.engine.rendering.RenderBackend;
import jforge.engine.rendering.Renderer;
import jforge.engine.rendering.RendererManager;
import jforge.engine.rendering.camera.CameraController;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.Component;

public class GameRuntime {

    private final LoadedScene loadedScene;

    private RendererManager rendererManager;
    private JFrame window;
    private CameraController cameraController;

    private volatile boolean running;

    public GameRuntime(
            LoadedScene loadedScene
    ) {

        if (loadedScene == null) {
            throw new IllegalArgumentException(
                    "LoadedScene no puede ser null."
            );
        }

        this.loadedScene = loadedScene;
    }

    public void start() {

        SwingUtilities.invokeLater(
                this::createWindow
        );
    }

    private void createWindow() {

        SceneRuntimeState state =
                createRenderer();

        rendererManager =
                state.rendererManager;

        Renderer renderer =
                rendererManager.getRenderer();

        cameraController =
                new CameraController(
                        renderer.getCamera()
                );

        applyLoadedCamera(
                loadedScene.getCamera(),
                renderer.getCamera()
        );

        Component view =
                renderer.getViewComponent();

        view.setFocusable(true);

        view.addKeyListener(
                cameraController
        );

        window =
                new JFrame(
                        loadedScene
                                .getScene()
                                .getName()
                );

        window.setDefaultCloseOperation(
                JFrame.EXIT_ON_CLOSE
        );

        window.setLayout(
                new BorderLayout()
        );

        window.add(
                view,
                BorderLayout.CENTER
        );

        window.setSize(
                1280,
                720
        );

        window.setLocationRelativeTo(
                null
        );

        window.setVisible(true);

        view.requestFocusInWindow();

        running = true;

        startLoop();
    }

    private SceneRuntimeState createRenderer() {

        RenderBackend backend =
                loadedScene.getBackend();

        if (backend == null) {
            backend =
                    RenderBackend.OPENGL;
        }

        RendererManager manager =
                new RendererManager(
                        loadedScene.getScene(),
                        backend
                );

        return new SceneRuntimeState(
                manager
        );
    }

    private void applyLoadedCamera(
            Camera source,
            Camera destination
    ) {

        if (source == null ||
                destination == null) {
            return;
        }

        var position =
                source.getPosition();

        destination.setPosition(
                position.x,
                position.y,
                position.z
        );

        destination.setRotationX(
                source.getRotationX()
        );

        destination.setRotationY(
                source.getRotationY()
        );

        destination.setFieldOfView(
                source.getFieldOfView()
        );

        destination.setNearPlane(
                source.getNearPlane()
        );

        destination.setFarPlane(
                source.getFarPlane()
        );
    }

    private void startLoop() {

        Thread thread =
                new Thread(() -> {

                    long previous =
                            System.nanoTime();

                    while (running) {

                        long current =
                                System.nanoTime();

                        double deltaTime =
                                (
                                        current -
                                                previous
                                ) /
                                        1_000_000_000.0;

                        previous =
                                current;

                        if (cameraController != null) {

                            cameraController.update(
                                    deltaTime
                            );
                        }

                        Renderer renderer =
                                rendererManager
                                        .getRenderer();

                        if (renderer != null) {

                            renderer.setFrameTime(
                                    deltaTime
                            );

                            renderer.requestRender();
                        }

                        try {

                            Thread.sleep(1);

                        } catch (
                                InterruptedException e
                        ) {

                            Thread
                                    .currentThread()
                                    .interrupt();

                            break;
                        }
                    }

                });

        thread.setName(
                "JForge-GameLoop"
        );

        thread.setDaemon(true);

        thread.start();
    }

    private static class SceneRuntimeState {

        private final RendererManager rendererManager;

        private SceneRuntimeState(
                RendererManager rendererManager
        ) {
            this.rendererManager =
                    rendererManager;
        }
    }
}