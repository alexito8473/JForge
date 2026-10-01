package jforge.engine.rendering.softwere;

import jforge.engine.GameObject;
import jforge.engine.Scene;
import jforge.engine.Transform;
import jforge.engine.components.MeshRenderer;
import jforge.engine.debug.FPSCounter;
import jforge.engine.debug.RenderStats;
import jforge.engine.math.Vector3;
import jforge.engine.rendering.RenderBackend;
import jforge.engine.rendering.Renderer;
import jforge.engine.rendering.camera.Camera;
import jforge.engine.rendering.geometry.Mesh;
import jforge.engine.rendering.geometry.Point2D;

import java.awt.Component;
import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Graphics;

public class Renderer3D extends JPanel implements Renderer {

    private final Scene scene;

    private final Camera camera;

    private final Framebuffer framebuffer;

    private final Rasterizer rasterizer;

    private final FPSCounter fpsCounter;

    private final RenderStats renderStats;

    private boolean showDebug;

    private int renderedObjects;

    private int renderedTriangles;

    public Renderer3D(
            Scene scene) {

        this.scene =
                scene;

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

        this.fpsCounter =
                new FPSCounter();

        this.renderStats =
                new RenderStats();

        this.showDebug =
                true;

        this.renderedObjects =
                0;

        this.renderedTriangles =
                0;

        setFocusable(true);
    }
    @Override
    public Component getViewComponent() {

        return this;
    }

    @Override
    public RenderStats getRenderStats() {
        return renderStats;
    }

    @Override
    public void requestRender() {

        repaint();
    }

    @Override
    public void setFrameTime(double frameTime) {

    }

    @Override
    public RenderBackend getBackend() {

        return RenderBackend.SOFTWARE;
    }

    @Override
    public void dispose() {

        /*
         * El renderer software no necesita
         * liberar recursos nativos.
         */
    }

    public Camera getCamera() {

        return camera;
    }

    @Override
    protected void paintComponent(
            Graphics g) {

        super.paintComponent(g);

        long frameStart =
                System.nanoTime();

        int width =
                getWidth();

        int height =
                getHeight();

        if (width <= 0 ||
                height <= 0) {

            return;
        }

        /*
         * Adaptar framebuffer al tamaño
         * de la ventana.
         */
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
         * CLEAR
         */
        long clearStart =
                System.nanoTime();

        framebuffer.clear(
                Color.BLACK
        );

        rasterizer.clearDepth();

        long clearEnd =
                System.nanoTime();

        renderedObjects =
                0;

        renderedTriangles =
                0;

        /*
         * RENDER
         */
        long renderStart =
                System.nanoTime();

        for (GameObject object : scene.getObjects()) {

            renderHierarchy(
                    object
            );
        }

        long renderEnd =
                System.nanoTime();

        /*
         * PRESENT
         */
        g.drawImage(
                framebuffer.getImage(),
                0,
                0,
                null
        );

        /*
         * Contar frame REAL.
         */
        fpsCounter.update();

        long frameEnd =
                System.nanoTime();

        double frameTime =
                (frameEnd - frameStart)
                        / 1_000_000.0;

        double clearTime =
                (clearEnd - clearStart)
                        / 1_000_000.0;

        double renderTime =
                (renderEnd - renderStart)
                        / 1_000_000.0;

        renderStats.setFrameTime(
                frameTime
        );

        renderStats.setClearTime(
                clearTime
        );

        renderStats.setRenderTime(
                renderTime
        );

        renderStats.setFPS(
                fpsCounter.getFPS()
        );

        renderStats.setTotalFrames(
                fpsCounter.getTotalFrames()
        );

        renderStats.setObjects(
                renderedObjects
        );

        renderStats.setTriangles(
                renderedTriangles
        );

        /*
         * DEBUG
         */
        if (showDebug) {

            drawDebugOverlay(
                    g
            );
        }
    }

    private void renderHierarchy(
            GameObject object) {

        if (object == null) {
            return;
        }

        renderObject(
                object
        );

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

        Mesh mesh =
                meshRenderer.getMesh();

        if (mesh == null) {
            return;
        }

        renderedObjects++;

        Vector3[] vertices =
                mesh.getVertices();

        int[][] triangles =
                mesh.getTriangles();

        Vector3[] worldVertices =
                new Vector3[
                        vertices.length
                        ];

        /*
         * WORLD TRANSFORM
         */
        for (int i = 0;
             i < vertices.length;
             i++) {

            worldVertices[i] =
                    transformVertex(
                            vertices[i],
                            object
                    );
        }

        /*
         * TRIANGLES
         */
        for (int[] triangle :
                triangles) {

            if (triangle == null ||
                    triangle.length < 3) {

                continue;
            }

            int a =
                    triangle[0];

            int b =
                    triangle[1];

            int c =
                    triangle[2];

            if (a < 0 ||
                    b < 0 ||
                    c < 0 ||
                    a >= worldVertices.length ||
                    b >= worldVertices.length ||
                    c >= worldVertices.length) {

                continue;
            }

            Vector3 v1 =
                    worldVertices[a];

            Vector3 v2 =
                    worldVertices[b];

            Vector3 v3 =
                    worldVertices[c];

            /*
             * TRANSFORMACIÓN A ESPACIO DE CÁMARA
             */
            Vector3 cameraV1 =
                    worldToCamera(
                            v1
                    );

            Vector3 cameraV2 =
                    worldToCamera(
                            v2
                    );

            Vector3 cameraV3 =
                    worldToCamera(
                            v3
                    );

            /*
             * CLIPPING CONTRA EL NEAR PLANE
             */
            java.util.List<Vector3> clipped =
                    clipTriangleAgainstNearPlane(
                            cameraV1,
                            cameraV2,
                            cameraV3
                    );

            if (clipped.size() < 3) {

                continue;
            }

            /*
             * COLOR
             */
            Color color =
                    meshRenderer
                            .getMaterial()
                            .getColor();

            /*
             * Un triángulo puede convertirse
             * en un cuadrilátero después del clipping.
             *
             * Lo triangulamos así:
             *
             *      0
             *     / \
             *    /   \
             *   1-----2
             *    \   /
             *     \ /
             *      3
             *
             * En realidad generamos:
             *
             * triangle(0,1,2)
             * triangle(0,2,3)
             */
            for (int i = 1;
                 i < clipped.size() - 1;
                 i++) {

                Vector3 clippedA =
                        clipped.get(0);

                Vector3 clippedB =
                        clipped.get(i);

                Vector3 clippedC =
                        clipped.get(i + 1);

                Point2D p1 =
                        project(
                                clippedA
                        );

                Point2D p2 =
                        project(
                                clippedB
                        );

                Point2D p3 =
                        project(
                                clippedC
                        );

                if (p1 == null ||
                        p2 == null ||
                        p3 == null) {

                    continue;
                }

                renderedTriangles++;

                rasterizer.drawTriangle(
                        framebuffer,
                        p1,
                        p2,
                        p3,
                        color
                );
            }
        }
    }

    private Vector3 transformVertex(
            Vector3 vertex,
            GameObject object) {

        Transform transform =
                object.getTransform();

        /*
         * Escala.
         */
        Vector3 result =
                new Vector3(
                        vertex.x *
                                transform
                                        .getScale()
                                        .x,

                        vertex.y *
                                transform
                                        .getScale()
                                        .y,

                        vertex.z *
                                transform
                                        .getScale()
                                        .z
                );

        /*
         * Rotación X.
         */
        double rx =
                Math.toRadians(
                        transform
                                .getRotation()
                                .x
                );

        double cosX =
                Math.cos(rx);

        double sinX =
                Math.sin(rx);

        double y =
                result.y * cosX -
                        result.z * sinX;

        double z =
                result.y * sinX +
                        result.z * cosX;

        result =
                new Vector3(
                        result.x,
                        y,
                        z
                );

        /*
         * Rotación Y.
         */
        double ry =
                Math.toRadians(
                        transform
                                .getRotation()
                                .y
                );

        double cosY =
                Math.cos(ry);

        double sinY =
                Math.sin(ry);

        double x =
                result.x * cosY +
                        result.z * sinY;

        z =
                -result.x * sinY +
                        result.z * cosY;

        result =
                new Vector3(
                        x,
                        result.y,
                        z
                );

        /*
         * Rotación Z.
         */
        double rz =
                Math.toRadians(
                        transform
                                .getRotation()
                                .z
                );

        double cosZ =
                Math.cos(rz);

        double sinZ =
                Math.sin(rz);

        x =
                result.x * cosZ -
                        result.y * sinZ;

        y =
                result.x * sinZ +
                        result.y * cosZ;

        result =
                new Vector3(
                        x,
                        y,
                        result.z
                );

        /*
         * Traslación.
         */
        result =
                result.add(
                        transform.getPosition()
                );

        return result;
    }

    private Vector3 worldToCamera(
            Vector3 vertex) {

        /*
         * Pasar de coordenadas mundo
         * a coordenadas relativas a la cámara.
         */
        Vector3 result =
                vertex.subtract(
                        camera.getPosition()
                );

        /*
         * Invertir yaw de la cámara.
         */
        double yaw =
                Math.toRadians(
                        -camera.getRotationY()
                );

        double cosYaw =
                Math.cos(yaw);

        double sinYaw =
                Math.sin(yaw);

        double x =
                result.x * cosYaw +
                        result.z * sinYaw;

        double z =
                -result.x * sinYaw +
                        result.z * cosYaw;

        result =
                new Vector3(
                        x,
                        result.y,
                        z
                );

        /*
         * Invertir pitch de la cámara.
         */
        double pitch =
                Math.toRadians(
                        -camera.getRotationX()
                );

        double cosPitch =
                Math.cos(pitch);

        double sinPitch =
                Math.sin(pitch);

        double y =
                result.y * cosPitch -
                        result.z * sinPitch;

        z =
                result.y * sinPitch +
                        result.z * cosPitch;

        return new Vector3(
                result.x,
                y,
                z
        );
    }

    private Point2D project(
            Vector3 vertex) {

        double z =
                vertex.z;

        double near =
                camera.getNearPlane();

        /*
         * El punto debe estar delante
         * del plano cercano.
         */
        if (z >= -near) {

            return null;
        }

        double fov =
                Math.toRadians(
                        camera.getFieldOfView()
                );

        double focalLength =
                (
                        getHeight() / 2.0
                )
                        /
                        Math.tan(
                                fov / 2.0
                        );

        double factor =
                focalLength /
                        -z;

        int x =
                (int) (
                        getWidth() / 2.0 +
                                vertex.x * factor
                );

        int y =
                (int) (
                        getHeight() / 2.0 -
                                vertex.y * factor
                );

        return new Point2D(
                x,
                y,
                z
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
                220,
                145
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
                        renderStats.getFPS(),
                18,
                47
        );

        g.drawString(
                String.format(
                        "Frame: %.2f ms",
                        renderStats.getFrameTime()
                ),
                18,
                65
        );

        g.drawString(
                String.format(
                        "Clear: %.2f ms",
                        renderStats.getClearTime()
                ),
                18,
                83
        );

        g.drawString(
                String.format(
                        "Render: %.2f ms",
                        renderStats.getRenderTime()
                ),
                18,
                101
        );

        g.drawString(
                "Objects: " +
                        renderStats.getObjects(),
                18,
                119
        );

        g.drawString(
                "Triangles: " +
                        renderStats.getTriangles(),
                18,
                137
        );
    }

    public void setDebugVisible(
            boolean visible) {

        showDebug =
                visible;
    }

    public Camera getCameraObject() {

        return camera;
    }
    private java.util.List<Vector3>
    clipTriangleAgainstNearPlane(
            Vector3 v1,
            Vector3 v2,
            Vector3 v3) {

        java.util.List<Vector3> input =
                new java.util.ArrayList<>();

        input.add(v1);
        input.add(v2);
        input.add(v3);

        java.util.List<Vector3> output =
                new java.util.ArrayList<>();

        double near =
                camera.getNearPlane();

        /*
         * Nuestro plano cercano está en:
         *
         * z = -near
         *
         * Los puntos válidos están:
         *
         * z <= -near
         */
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
                    current.z <= -near;

            boolean previousInside =
                    previous.z <= -near;

            /*
             * Caso 1:
             *
             * previous fuera
             * current dentro
             *
             * Entramos al plano.
             */
            if (!previousInside &&
                    currentInside) {

                output.add(
                        intersectNearPlane(
                                previous,
                                current,
                                near
                        )
                );

                output.add(
                        current
                );
            }

            /*
             * Caso 2:
             *
             * previous dentro
             * current dentro
             *
             * El segmento permanece
             * dentro del volumen.
             */
            else if (previousInside &&
                    currentInside) {

                output.add(
                        current
                );
            }

            /*
             * Caso 3:
             *
             * previous dentro
             * current fuera
             *
             * Salimos del plano.
             */
            else if (previousInside &&
                    !currentInside) {

                output.add(
                        intersectNearPlane(
                                previous,
                                current,
                                near
                        )
                );
            }

            /*
             * Caso 4:
             *
             * ambos fuera.
             *
             * No añadimos nada.
             */
        }

        return output;
    }
    private Vector3 intersectNearPlane(
            Vector3 a,
            Vector3 b,
            double near) {

        /*
         * Queremos encontrar el punto donde:
         *
         * z = -near
         */

        double planeZ =
                -near;

        double dz =
                b.z - a.z;

        /*
         * Evitar división por cero.
         */
        if (Math.abs(dz) < 0.000001) {

            return a.copy();
        }

        double t =
                (planeZ - a.z) /
                        dz;

        t =
                Math.max(
                        0.0,
                        Math.min(
                                1.0,
                                t
                        )
                );

        double x =
                a.x +
                        (b.x - a.x) *
                                t;

        double y =
                a.y +
                        (b.y - a.y) *
                                t;

        double z =
                a.z +
                        (b.z - a.z) *
                                t;

        return new Vector3(
                x,
                y,
                z
        );
    }
}