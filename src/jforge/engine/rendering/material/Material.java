package jforge.engine.rendering.material;

import java.awt.Color;

public class Material {

    private Color color;

    private Texture texture;

    public Material(
            Color color) {

        this.color =
                color;

        this.texture =
                null;
    }

    public Color getColor() {

        return color;
    }

    public void setColor(
            Color color) {

        if (color == null) {
            return;
        }

        this.color =
                color;
    }

    public Texture getTexture() {

        return texture;
    }

    public void setTexture(
            Texture texture) {

        this.texture =
                texture;
    }

    public boolean hasTexture() {

        return texture != null;
    }
}