package jforge.engine.rendering;

import jforge.engine.GameObject;
import jforge.engine.Scene;
import jforge.engine.components.MeshRenderer;
import jforge.engine.debug.DebugStats;
import jforge.engine.debug.FPSCounter;
import jforge.engine.math.Matrix4;
import jforge.engine.math.Vector3;

import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.List;

public class Renderer3D extends JPanel {
    private final DebugStats debugStats;
    private final FPSCounter fpsCounter;
    private boolean showDebug;

    private int renderedObjects;
    private int renderedTriangles;
    private final Scene scene;
    private int fps;
    private final Camera camera;
    private final Framebuffer framebuffer;
    private final Rasterizer rasterizer;

    private static final double NEAR_PLANE = 0.1;

    public Renderer3D(Scene scene) {

        this.scene = scene;

        this.camera =
                new Camera();

        this.framebuffer =
                new Framebuffer(
                        800,
                        600
                );

        this.rasterizer =
                new Rasterizer(
                        800,
                        600
                );

        setFocusable(true);
        fpsCounter =
                new FPSCounter();
        debugStats =
                new DebugStats();

        showDebug =
                true;

        renderedObjects =
                0;

        renderedTriangles =
                0;
    }

    @Override
    protected void paintComponent(
            Graphics g) {

        super.paintComponent(g);

        int width =
                getWidth();

        int height =
                getHeight();

        if (width <= 0 ||
                height <= 0) {

            return;
        }

        if (framebuffer.getWidth() != width ||
                framebuffer.getHeight() != height) {

            framebuffer.resize(
                    width,
                    height
            );

            rasterizer.resize(
                    width,
                    height
            );
        }

        /*
         * Limpiar pantalla.
         */
        framebuffer.clear(
                Color.BLACK
        );

        /*
         * Limpiar Z-buffer.
         */
        rasterizer.clearDepth();
        renderedObjects = 0;
        renderedTriangles = 0;
        /*
         * Dibujar todos los objetos raíz.
         */
        for (GameObject object :
                scene.getObjects()) {

            renderHierarchy(
                    object
            );
        }

        /*
         * Mostrar framebuffer.
         */
        g.drawImage(
                framebuffer.getImage(),
                0,
                0,
                null
        );
        fpsCounter.frameRendered();

        /*
         * Actualizar estadísticas.
         */
        debugStats.setFPS(
                fpsCounter.getFPS()
        );

        debugStats.setTotalFrames(
                fpsCounter.getTotalFrames()
        );

        debugStats.setObjects(
                renderedObjects
        );

        debugStats.setTriangles(
                renderedTriangles
        );

        if (showDebug) {

            drawDebugOverlay(
                    g
            );
        }
        g.setColor(Color.WHITE);

        g.fillRect(
                5,
                5,
                100,
                25
        );

        g.setColor(Color.BLACK);

        g.drawString(
                "FPS: " + fps,
                10,
                22
        );
    }
    public void setFPS(
            int fps) {

        this.fps =
                fps;
    }
    private void renderHierarchy(
            GameObject object) {

        if (object == null) {
            return;
        }

        if (!object.isActive()) {
            return;
        }

        /*
         * Dibujar objeto.
         */
        renderObject(object);

        /*
         * Dibujar hijos.
         */
        for (GameObject child :
                object.getChildren()) {

            renderHierarchy(
                    child
            );
        }
    }

    private void renderObject(
            GameObject object) {

        MeshRenderer meshRenderer =
                object.getMeshRenderer();

        if (meshRenderer == null) {
            return;
        }
        renderedObjects++;
        Mesh mesh =
                meshRenderer.getMesh();

        if (mesh == null) {
            return;
        }

        Material material =
                meshRenderer.getMaterial();

        if (material == null) {
            return;
        }

        /*
         * Matriz mundial del objeto.
         *
         * Incluye también las transformaciones
         * de todos sus padres.
         */
        Matrix4 worldMatrix =
                getWorldMatrix(
                        object
                );

        Vector3[] vertices =
                mesh.getVertices();

        int[][] triangles =
                mesh.getTriangles();

        /*
         * Recorrer triángulos.
         */
        for (int[] triangle :
                triangles) {

            if (triangle == null ||
                    triangle.length < 3) {

                continue;
            }
            renderedTriangles++;
            int indexA =
                    triangle[0];

            int indexB =
                    triangle[1];

            int indexC =
                    triangle[2];

            /*
             * Comprobar índices.
             */
            if (indexA < 0 ||
                    indexA >= vertices.length ||
                    indexB < 0 ||
                    indexB >= vertices.length ||
                    indexC < 0 ||
                    indexC >= vertices.length) {

                continue;
            }

            /*
             * Vértices locales.
             */
            Vector3 localA =
                    vertices[indexA];

            Vector3 localB =
                    vertices[indexB];

            Vector3 localC =
                    vertices[indexC];

            /*
             * Convertir a coordenadas mundiales.
             */
            Vector3 a =
                    worldMatrix.transform(
                            localA
                    );

            Vector3 b =
                    worldMatrix.transform(
                            localB
                    );

            Vector3 c =
                    worldMatrix.transform(
                            localC
                    );

            /*
             * Normal de la cara.
             */
            Vector3 edge1 =
                    b.subtract(a);

            Vector3 edge2 =
                    c.subtract(a);

            Vector3 normal =
                    edge1
                            .cross(edge2)
                            .normalize();

            /*
             * IMPORTANTE:
             *
             * No hacemos backface culling todavía.
             *
             * Antes estábamos descartando caras aquí
             * y eso provocaba que algunas caras del cubo
             * desaparecieran.
             *
             * El Z-buffer se encargará de ocultar las caras
             * que estén detrás.
             */

            double lighting =
                    calculateLighting(
                            normal
                    );

            /*
             * Añadir iluminación ambiental mínima
             * para que las caras no queden completamente
             * negras cuando la luz no las alcanza.
             */
            lighting =
                    Math.max(
                            0.20,
                            lighting
                    );

            Color color =
                    shadeColor(
                            material.getColor(),
                            lighting
                    );

            /*
             * Dibujar triángulo.
             */
            drawTriangle3D(
                    a,
                    b,
                    c,
                    color
            );
        }
    }

    private Matrix4 getWorldMatrix(
            GameObject object) {

        Matrix4 result =
                object
                        .getTransform()
                        .getLocalMatrix();

        GameObject parent =
                object.getParent();

        while (parent != null) {

            Matrix4 parentMatrix =
                    parent
                            .getTransform()
                            .getLocalMatrix();

            result =
                    parentMatrix
                            .multiply(
                                    result
                            );

            parent =
                    parent.getParent();
        }

        return result;
    }

    private void drawTriangle3D(
            Vector3 a,
            Vector3 b,
            Vector3 c,
            Color color) {

        /*
         * Recortar contra el plano cercano.
         */
        List<Vector3> clipped =
                clipAgainstNearPlane(
                        a,
                        b,
                        c
                );

        if (clipped.size() < 3) {
            return;
        }

        /*
         * Triángulo normal.
         */
        if (clipped.size() == 3) {

            drawProjectedTriangle(
                    clipped.get(0),
                    clipped.get(1),
                    clipped.get(2),
                    color
            );

            return;
        }

        /*
         * Después del clipping puede aparecer
         * un cuadrilátero.
         *
         * Lo dividimos en dos triángulos.
         */
        if (clipped.size() == 4) {

            drawProjectedTriangle(
                    clipped.get(0),
                    clipped.get(1),
                    clipped.get(2),
                    color
            );

            drawProjectedTriangle(
                    clipped.get(0),
                    clipped.get(2),
                    clipped.get(3),
                    color
            );
        }
    }

    private void drawProjectedTriangle(
            Vector3 a,
            Vector3 b,
            Vector3 c,
            Color color) {

        Point2D p1 =
                project(a);

        Point2D p2 =
                project(b);

        Point2D p3 =
                project(c);

        if (p1 == null ||
                p2 == null ||
                p3 == null) {

            return;
        }

        rasterizer.drawTriangle(
                framebuffer.getImage(),
                p1,
                p2,
                p3,
                color
        );
    }

    private List<Vector3> clipAgainstNearPlane(
            Vector3 a,
            Vector3 b,
            Vector3 c) {

        List<Vector3> input =
                new ArrayList<>();

        input.add(a);
        input.add(b);
        input.add(c);

        List<Vector3> output =
                new ArrayList<>();

        for (int i = 0;
             i < input.size();
             i++) {

            Vector3 current =
                    input.get(i);

            Vector3 previous =
                    input.get(
                            (i + input.size() - 1)
                                    % input.size()
                    );

            boolean currentInside =
                    current.z <= -NEAR_PLANE;

            boolean previousInside =
                    previous.z <= -NEAR_PLANE;

            /*
             * Si uno está dentro y otro fuera,
             * calcular intersección con el plano.
             */
            if (currentInside !=
                    previousInside) {

                double t =
                        (
                                -NEAR_PLANE -
                                        previous.z
                        )
                                /
                                (
                                        current.z -
                                                previous.z
                                );

                Vector3 intersection =
                        interpolate(
                                previous,
                                current,
                                t
                        );

                output.add(
                        intersection
                );
            }

            /*
             * Añadir vértice si está dentro.
             */
            if (currentInside) {

                output.add(
                        current
                );
            }
        }

        return output;
    }

    private Vector3 interpolate(
            Vector3 a,
            Vector3 b,
            double t) {

        return new Vector3(
                a.x +
                        (b.x - a.x) * t,

                a.y +
                        (b.y - a.y) * t,

                a.z +
                        (b.z - a.z) * t
        );
    }

    private Point2D project(
            Vector3 vertex) {

        double z =
                vertex.z;

        /*
         * Nunca proyectar delante del plano cercano.
         */
        if (z >= -NEAR_PLANE) {
            return null;
        }

        /*
         * Distancia focal.
         */
        double focalLength =
                300.0;

        /*
         * Perspectiva.
         */
        double factor =
                focalLength /
                        -z;

        /*
         * Coordenada X.
         */
        int x =
                (int) (
                        getWidth() / 2.0
                                +
                                vertex.x * factor
                );

        /*
         * Coordenada Y.
         *
         * En pantalla Y crece hacia abajo,
         * por eso invertimos el signo.
         */
        int y =
                (int) (
                        getHeight() / 2.0
                                -
                                vertex.y * factor
                );

        return new Point2D(
                x,
                y,
                z
        );
    }

    private double calculateLighting(
            Vector3 normal) {

        Vector3 lightDirection =
                scene
                        .getLight()
                        .getDirection()
                        .multiply(-1)
                        .normalize();

        double value =
                normal.dot(
                        lightDirection
                );

        value =
                Math.max(
                        0,
                        value
                );

        return value *
                scene
                        .getLight()
                        .getIntensity();
    }

    private Color shadeColor(
            Color base,
            double intensity) {

        intensity =
                Math.max(
                        0,
                        Math.min(
                                1,
                                intensity
                        )
                );

        int red =
                (int) (
                        base.getRed()
                                *
                                intensity
                );

        int green =
                (int) (
                        base.getGreen()
                                *
                                intensity
                );

        int blue =
                (int) (
                        base.getBlue()
                                *
                                intensity
                );

        return new Color(
                red,
                green,
                blue
        );
    }
    private void drawDebugOverlay(
            Graphics g) {

        g.setColor(
                new Color(
                        0,
                        0,
                        0,
                        190
                )
        );

        g.fillRect(
                8,
                8,
                200,
                115
        );

        g.setColor(
                Color.WHITE
        );

        g.drawString(
                "JForge Debug",
                18,
                28
        );

        g.drawString(
                "FPS: " +
                        debugStats.getFPS(),
                18,
                47
        );

        g.drawString(
                String.format(
                        "Frame: %.3f ms",
                        debugStats.getFrameTime()
                ),
                18,
                65
        );

        g.drawString(
                "Frames: " +
                        debugStats.getTotalFrames(),
                18,
                83
        );

        g.drawString(
                "Objects: " +
                        renderedObjects,
                18,
                101
        );

        g.drawString(
                "Triangles: " +
                        renderedTriangles,
                18,
                119
        );
    }

    public void setFrameTime(
            double frameTime) {

        debugStats.setFrameTime(
                frameTime
        );
    }

    public void setDebugVisible(
            boolean visible) {

        showDebug =
                visible;
    }
}