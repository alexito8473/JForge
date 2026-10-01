package jforge.engine.rendering.softwere;

import jforge.engine.rendering.geometry.Point2D;

import java.awt.*;
import java.util.Arrays;

public class Rasterizer {

    private int width;
    private int height;

    private float[] depthBuffer;

    public Rasterizer(
            int width,
            int height) {

        resize(
                width,
                height
        );
    }

    public void resize(int width, int height) {
        this.width = Math.max(1, width);
        this.height = Math.max(1, height);
        depthBuffer = new float[this.width * this.height];
        clearDepth();
    }

    public void clearDepth() {
        Arrays.fill(depthBuffer, -Float.MAX_VALUE);
    }

    public void drawTriangle(
            Framebuffer framebuffer,
            Point2D p1,
            Point2D p2,
            Point2D p3,
            Color color) {

        if (framebuffer == null ||
                p1 == null ||
                p2 == null ||
                p3 == null ||
                color == null) {

            return;
        }

        /*
         * Tamaño del framebuffer.
         */
        int width =
                framebuffer.getWidth();

        int height =
                framebuffer.getHeight();

        /*
         * Array de píxeles.
         *
         * Lo obtenemos UNA SOLA VEZ.
         */
        int[] pixels =
                framebuffer.getPixels();

        /*
         * Color.
         *
         * También lo calculamos una sola vez.
         */
        int rgb =
                color.getRGB();

        /*
         * Bounding box del triángulo.
         */
        int minX =
                Math.min(
                        p1.x,
                        Math.min(
                                p2.x,
                                p3.x
                        )
                );

        int maxX =
                Math.max(
                        p1.x,
                        Math.max(
                                p2.x,
                                p3.x
                        )
                );

        int minY =
                Math.min(
                        p1.y,
                        Math.min(
                                p2.y,
                                p3.y
                        )
                );

        int maxY =
                Math.max(
                        p1.y,
                        Math.max(
                                p2.y,
                                p3.y
                        )
                );

        /*
         * Limitar al framebuffer.
         */
        minX =
                Math.max(
                        0,
                        minX
                );

        minY =
                Math.max(
                        0,
                        minY
                );

        maxX =
                Math.min(
                        width - 1,
                        maxX
                );

        maxY =
                Math.min(
                        height - 1,
                        maxY
                );

        /*
         * Si el bounding box no tiene
         * ningún píxel válido.
         */
        if (minX > maxX ||
                minY > maxY) {

            return;
        }

        /*
         * Área del triángulo.
         */
        double area =
                edgeFunction(
                        p1.x,
                        p1.y,
                        p2.x,
                        p2.y,
                        p3.x,
                        p3.y
                );

        /*
         * Triángulo degenerado.
         */
        if (Math.abs(area) < 0.000001) {

            return;
        }

        /*
         * Recorrer píxeles.
         */
        for (int y = minY;
             y <= maxY;
             y++) {

            for (int x = minX;
                 x <= maxX;
                 x++) {

                /*
                 * Centro del píxel.
                 */
                double px =
                        x + 0.5;

                double py =
                        y + 0.5;

                /*
                 * Coordenadas baricéntricas.
                 */
                double w0 =
                        edgeFunction(
                                p2.x,
                                p2.y,
                                p3.x,
                                p3.y,
                                px,
                                py
                        );

                double w1 =
                        edgeFunction(
                                p3.x,
                                p3.y,
                                p1.x,
                                p1.y,
                                px,
                                py
                        );

                double w2 =
                        edgeFunction(
                                p1.x,
                                p1.y,
                                p2.x,
                                p2.y,
                                px,
                                py
                        );

                /*
                 * Comprobar si el píxel
                 * está dentro del triángulo.
                 */
                boolean inside;

                if (area > 0) {

                    inside =
                            w0 >= 0 &&
                                    w1 >= 0 &&
                                    w2 >= 0;

                } else {

                    inside =
                            w0 <= 0 &&
                                    w1 <= 0 &&
                                    w2 <= 0;
                }

                if (!inside) {

                    continue;
                }

                /*
                 * Normalizar pesos.
                 */
                double alpha =
                        w0 / area;

                double beta =
                        w1 / area;

                double gamma =
                        w2 / area;

                /*
                 * Interpolar profundidad.
                 *
                 * La cámara mira hacia -Z.
                 *
                 * Ejemplo:
                 *
                 * z = -2  -> cerca
                 * z = -5  -> lejos
                 *
                 * Por eso un Z MAYOR
                 * está más cerca.
                 */
                float z = (float) (alpha * p1.z + beta * p2.z + gamma * p3.z);

                /*
                 * Índice del píxel.
                 */
                int index =
                        y * width + x;

                /*
                 * Z-buffer.
                 *
                 * Si el nuevo píxel está detrás
                 * del que ya tenemos, descartarlo.
                 */
                if (z <= depthBuffer[index]) {
                    continue;
                }

                /*
                 * Guardar profundidad.
                 */
                depthBuffer[index] = z;

                /*
                 * Escribir directamente
                 * en el array de píxeles.
                 *
                 * Ya NO usamos setRGB().
                 */
                pixels[index] =
                        rgb;
            }
        }
    }

    private double edgeFunction(
            double ax,
            double ay,
            double bx,
            double by,
            double px,
            double py) {

        return
                (px - ax) *
                        (by - ay)
                        -
                        (py - ay) *
                                (bx - ax);
    }
}