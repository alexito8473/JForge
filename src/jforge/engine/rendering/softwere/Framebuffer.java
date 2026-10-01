package jforge.engine.rendering.softwere;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;

public class Framebuffer {

    private BufferedImage image;

    private int[] pixels;

    public Framebuffer(
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

        width =
                Math.max(
                        1,
                        width
                );

        height =
                Math.max(
                        1,
                        height
                );

        image =
                new BufferedImage(
                        width,
                        height,
                        BufferedImage.TYPE_INT_RGB
                );

        /*
         * Obtener directamente el array
         * de píxeles de la imagen.
         *
         * Así evitamos BufferedImage.setRGB()
         * para cada píxel.
         */
        pixels =
                ((DataBufferInt)
                        image
                                .getRaster()
                                .getDataBuffer())
                        .getData();
    }

    public void clear(
            Color color) {

        int rgb =
                color.getRGB();

        /*
         * Escribir directamente
         * sobre el framebuffer.
         */
        java.util.Arrays.fill(
                pixels,
                rgb
        );
    }

    public void setPixel(
            int x,
            int y,
            int rgb) {

        if (x < 0 ||
                y < 0 ||
                x >= image.getWidth() ||
                y >= image.getHeight()) {

            return;
        }

        pixels[
                y * image.getWidth() + x
                ] = rgb;
    }

    public int getPixel(
            int x,
            int y) {

        if (x < 0 ||
                y < 0 ||
                x >= image.getWidth() ||
                y >= image.getHeight()) {

            return 0;
        }

        return pixels[
                y * image.getWidth() + x
                ];
    }

    public int[] getPixels() {

        return pixels;
    }

    public BufferedImage getImage() {

        return image;
    }

    public int getWidth() {

        return image.getWidth();
    }

    public int getHeight() {

        return image.getHeight();
    }
}