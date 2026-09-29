package jforge.engine.rendering;

import java.awt.Color;
import java.awt.image.BufferedImage;

public class Rasterizer {

    private int width;
    private int height;

    private double[] depthBuffer;

    public Rasterizer(
            int width,
            int height) {

        resize(
                width,
                height
        );
    }

    public void resize(
            int width,
            int height) {

        this.width =
                Math.max(
                        1,
                        width
                );

        this.height =
                Math.max(
                        1,
                        height
                );

        depthBuffer =
                new double[
                        this.width *
                                this.height
                        ];

        clearDepth();
    }

    public void clearDepth() {

        /*
         * La cámara mira hacia -Z.
         *
         * Un valor muy pequeño significa
         * que está muy lejos.
         *
         * Ejemplo:
         *
         * -0.1  -> cerca
         * -5    -> más lejos
         * -1000 -> muchísimo más lejos
         *
         * Usamos -infinito como valor inicial.
         */
        for (int i = 0;
             i < depthBuffer.length;
             i++) {

            depthBuffer[i] =
                    Double.NEGATIVE_INFINITY;
        }
    }

    public void drawTriangle(
            BufferedImage image,
            Point2D p1,
            Point2D p2,
            Point2D p3,
            Color color) {

        if (image == null ||
                p1 == null ||
                p2 == null ||
                p3 == null ||
                color == null) {

            return;
        }

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
                        image.getWidth() - 1,
                        maxX
                );

        maxY =
                Math.min(
                        image.getHeight() - 1,
                        maxY
                );

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
                 * Aceptar ambos sentidos del triángulo.
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
                 * Como estamos trabajando directamente
                 * con Z en coordenadas de cámara/mundo,
                 * no invertimos el signo.
                 */
                double z =
                        alpha * p1.z +
                                beta * p2.z +
                                gamma * p3.z;

                int index =
                        y * width + x;

                /*
                 * IMPORTANTE:
                 *
                 * -2 está delante de -5.
                 *
                 * Por tanto:
                 *
                 * z > depthBuffer[index]
                 *
                 * significa que el nuevo píxel está
                 * más cerca de la cámara.
                 */
                if (z <= depthBuffer[index]) {
                    continue;
                }

                /*
                 * Guardar profundidad.
                 */
                depthBuffer[index] =
                        z;

                /*
                 * Dibujar píxel.
                 */
                image.setRGB(
                        x,
                        y,
                        color.getRGB()
                );
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