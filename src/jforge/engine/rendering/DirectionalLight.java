package jforge.engine.rendering;

import jforge.engine.math.Vector3;

import java.awt.Color;

public class DirectionalLight {

    private Vector3 direction;

    private Color color;

    private double intensity;

    public DirectionalLight() {

        direction =
                new Vector3(
                        -1,
                        -1,
                        -1
                ).normalize();

        color =
                Color.WHITE;

        intensity = 1.0;
    }

    public Vector3 getDirection() {

        return direction;
    }

    public Color getColor() {

        return color;
    }

    public double getIntensity() {

        return intensity;
    }

    public void setDirection(
            Vector3 direction) {

        this.direction =
                direction.normalize();
    }

    public void setIntensity(
            double intensity) {

        this.intensity =
                Math.max(
                        0,
                        intensity
                );
    }
}