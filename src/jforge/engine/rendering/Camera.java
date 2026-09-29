package jforge.engine.rendering;

import jforge.engine.math.Vector3;

public class Camera {

    private final Vector3 position;

    private double fieldOfView;

    private double nearPlane;

    private double farPlane;

    public Camera() {

        position =
                new Vector3(0, 0, 5);

        fieldOfView = 70;

        nearPlane = 0.1;

        farPlane = 1000;
    }

    public Vector3 getPosition() {

        return position;
    }

    public double getFieldOfView() {

        return fieldOfView;
    }

    public double getNearPlane() {

        return nearPlane;
    }

    public double getFarPlane() {

        return farPlane;
    }
}
