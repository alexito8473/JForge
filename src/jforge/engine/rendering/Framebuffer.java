package jforge.engine.rendering;

import java.awt.Color;
import java.awt.image.BufferedImage;

public class Framebuffer {

    private BufferedImage image;

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

        width = Math.max(
                1,
                width
        );

        height = Math.max(
                1,
                height
        );

        image =
                new BufferedImage(
                        width,
                        height,
                        BufferedImage.TYPE_INT_RGB
                );
    }

    public void clear(
            Color color) {

        int rgb =
                color.getRGB();

        for (
                int y = 0;
                y < image.getHeight();
                y++
        ) {

            for (
                    int x = 0;
                    x < image.getWidth();
                    x++
            ) {

                image.setRGB(
                        x,
                        y,
                        rgb
                );
            }
        }
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