package jforge;

import jforge.editor.EditorWindow;
import jforge.engine.GameObject;
import jforge.engine.Scene;
import jforge.engine.components.MeshRenderer;
import jforge.engine.rendering.*;
import jforge.engine.rendering.Renderer;
import jforge.engine.rendering.geometry.Mesh;
import jforge.engine.rendering.gpu.WindowsGPUPreference;
import jforge.engine.rendering.material.Material;

import javax.swing.*;
import java.awt.*;

public class Main {
    public static void main(String[] args) {

        /*
         * Windows debe decidir la GPU antes de crear
         * el contexto OpenGL.
         */
        if (WindowsGPUPreference.isWindows()) {

            boolean configured =
                    WindowsGPUPreference
                            .setHighPerformanceForCurrentJava();

            System.out.println(
                    "[GPU] Preferencia High Performance: " +
                            configured
            );
        }
        try {
            UIManager.setLookAndFeel(
                    UIManager.getSystemLookAndFeelClassName()
            );
        }catch (Exception ignored){

        }
        SwingUtilities.invokeLater(
                Main::start
        );
    }

    private static void start() {

        Scene scene =
                new Scene("Main Scene");

        GameObject cube =
                new GameObject("Cube");

        cube.getTransform()
                .setPosition(0, 0, -5);

        cube.setMeshRenderer(
                new MeshRenderer(
                        Mesh.createCube(),
                        new Material(Color.RED)
                )
        );

        scene.add(cube);

        RendererManager rendererManager =
                new RendererManager(
                        scene,
                        RenderBackend.OPENGL
                );

        EditorWindow editor =
                new EditorWindow(
                        scene,
                        rendererManager
                );

        editor.setTitle(
                "JForge Editor - " +
                        rendererManager.getBackend()
        );

        editor.setVisible(true);

        startLoop(
                rendererManager,
                editor
        );
    }

    private static void startLoop(
            RendererManager rendererManager,
            EditorWindow editor
    ) {

        Thread thread =
                new Thread(() -> {

                    long previous =
                            System.nanoTime();

                    while (
                            !Thread
                                    .currentThread()
                                    .isInterrupted()
                    ) {

                        long current =
                                System.nanoTime();

                        double deltaTime =
                                (
                                        current -
                                                previous
                                ) /
                                        1_000_000_000.0;

                        previous = current;

                        editor
                                .getCameraController()
                                .update(
                                        deltaTime
                                );

                        Renderer renderer =
                                rendererManager
                                        .getRenderer();

                        if (renderer != null) {
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
}