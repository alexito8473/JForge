package jforge.engine.rendering.material;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class Texture {

    private final File file;

    private final BufferedImage image;

    private Texture(
            File file,
            BufferedImage image) {

        this.file =
                file;

        this.image =
                image;
    }

    public static Texture load(
            File file) throws IOException {

        if (file == null) {

            throw new IllegalArgumentException(
                    "Texture file es null."
            );
        }

        BufferedImage image =
                ImageIO.read(
                        file
                );

        if (image == null) {

            throw new IOException(
                    "No se pudo leer la textura: " +
                            file
            );
        }

        return new Texture(
                file,
                image
        );
    }

    public File getFile() {

        return file;
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