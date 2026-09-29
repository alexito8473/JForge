package jforge;

import jforge.editor.EditorWindow;
import jforge.engine.GameObject;
import jforge.engine.Scene;
import jforge.engine.components.MeshRenderer;
import jforge.engine.debug.FPSCounter;
import jforge.engine.rendering.Material;
import jforge.engine.rendering.Mesh;
import jforge.engine.rendering.Renderer3D;
import jforge.engine.rendering.Window;

import javax.swing.*;
import java.awt.*;

public class Main {

    public static void main(String[] args) {

        SwingUtilities.invokeLater(() -> {

            // =========================================
            // ESCENA
            // =========================================

            Scene scene =
                    new Scene(
                            "Main Scene"
                    );


            // =========================================
            // MESH
            // =========================================

            Mesh cubeMesh =
                    Mesh.createCube();


            // =========================================
            // PARENT
            // =========================================

            GameObject parent =
                    new GameObject(
                            "Parent"
                    );

            parent
                    .getTransform()
                    .setPosition(
                            0,
                            0,
                            -3
                    );

            parent
                    .getTransform()
                    .setRotation(
                            0,
                            45,
                            0
                    );

            parent
                    .getTransform()
                    .setScale(
                            1,
                            1,
                            1
                    );

            parent.setMeshRenderer(
                    new MeshRenderer(
                            cubeMesh,
                            new Material(
                                    Color.RED
                            )
                    )
            );

            scene.add(
                    parent
            );


            // =========================================
            // CHILD
            // =========================================

            GameObject child =
                    new GameObject(
                            "Child"
                    );

            child
                    .getTransform()
                    .setPosition(
                            1.5,
                            0,
                            0
                    );

            child
                    .getTransform()
                    .setRotation(
                            0,
                            0,
                            0
                    );

            child
                    .getTransform()
                    .setScale(
                            0.5,
                            0.5,
                            0.5
                    );

            child.setMeshRenderer(
                    new MeshRenderer(
                            cubeMesh,
                            new Material(
                                    Color.BLUE
                            )
                    )
            );

            parent.addChild(
                    child
            );


            // =========================================
            // CHILD 2
            // =========================================

            GameObject child2 =
                    new GameObject(
                            "Child 2"
                    );

            child2
                    .getTransform()
                    .setPosition(
                            -1.5,
                            0,
                            0
                    );

            child2
                    .getTransform()
                    .setRotation(
                            0,
                            0,
                            0
                    );

            child2
                    .getTransform()
                    .setScale(
                            0.5,
                            0.5,
                            0.5
                    );

            child2.setMeshRenderer(
                    new MeshRenderer(
                            cubeMesh,
                            new Material(
                                    Color.GREEN
                            )
                    )
            );

            parent.addChild(
                    child2
            );


            // =========================================
            // RENDERER
            // =========================================

            Renderer3D renderer =
                    new Renderer3D(
                            scene
                    );


            // =========================================
            // WINDOW
            // =========================================

            Window window =
                    new Window(
                            renderer
                    );

            window.setVisible(
                    true
            );


            // =========================================
            // GAME LOOP
            // =========================================

            startLoop(
                    renderer,
                    parent
            );
        });
    }


    // =============================================
    // GAME LOOP
    // =============================================

    private static void startLoop(
            Renderer3D renderer,
            GameObject parent) {

        FPSCounter fpsCounter =
                new FPSCounter();

        Thread thread =
                new Thread(() -> {

                    long previous =
                            System.nanoTime();

                    while (true) {

                        long current =
                                System.nanoTime();

                        double deltaTime =
                                (
                                        current -
                                                previous
                                )
                                        /
                                        1_000_000_000.0;

                        previous =
                                current;

                        /*
                         * Actualizar juego.
                         */
                        update(
                                parent,
                                deltaTime
                        );

                        /*
                         * FPS.
                         */
                        fpsCounter.update();

                        renderer.setFPS(
                                fpsCounter.getFPS()
                        );

                        /*
                         * Tiempo del frame.
                         */
                        renderer.setFrameTime(
                                deltaTime * 1000.0
                        );

                        /*
                         * Dibujar.
                         */
                        renderer.repaint();

                        try {

                            Thread.sleep(
                                    1
                            );

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

        thread.start();
    }

    // =============================================
    // UPDATE
    // =============================================

    private static void update(
            GameObject parent,
            double deltaTime) {

        parent
                .getTransform()
                .rotate(
                        0,
                        45.0 * deltaTime,
                        0
                );
    }
}
