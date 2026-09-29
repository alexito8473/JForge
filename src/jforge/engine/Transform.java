package jforge.engine;

import jforge.engine.math.Matrix4;
import jforge.engine.math.Vector3;

public class Transform {

    private final Vector3 position;
    private final Vector3 rotation;
    private final Vector3 scale;

    public Transform() {

        position =
                new Vector3(
                        0,
                        0,
                        0
                );

        rotation =
                new Vector3(
                        0,
                        0,
                        0
                );

        scale =
                new Vector3(
                        1,
                        1,
                        1
                );
    }

    public Vector3 getPosition() {
        return position;
    }

    public Vector3 getRotation() {
        return rotation;
    }

    public Vector3 getScale() {
        return scale;
    }

    public void setPosition(
            double x,
            double y,
            double z) {

        position.x = x;
        position.y = y;
        position.z = z;
    }

    public void setRotation(
            double x,
            double y,
            double z) {

        rotation.x = x;
        rotation.y = y;
        rotation.z = z;
    }

    public void setScale(
            double x,
            double y,
            double z) {

        scale.x = x;
        scale.y = y;
        scale.z = z;
    }

    public void translate(
            double x,
            double y,
            double z) {

        position.x += x;
        position.y += y;
        position.z += z;
    }

    public void rotate(
            double x,
            double y,
            double z) {

        rotation.x += x;
        rotation.y += y;
        rotation.z += z;
    }

    public void scale(
            double x,
            double y,
            double z) {

        scale.x *= x;
        scale.y *= y;
        scale.z *= z;
    }

    public Matrix4 getLocalMatrix() {

        Matrix4 translation =
                Matrix4.translation(
                        position.x,
                        position.y,
                        position.z
                );

        Matrix4 rotationX =
                Matrix4.rotationX(
                        rotation.x
                );

        Matrix4 rotationY =
                Matrix4.rotationY(
                        rotation.y
                );

        Matrix4 rotationZ =
                Matrix4.rotationZ(
                        rotation.z
                );

        Matrix4 scaleMatrix =
                Matrix4.scale(
                        scale.x,
                        scale.y,
                        scale.z
                );

        Matrix4 rotation =
                rotationZ
                        .multiply(rotationY)
                        .multiply(rotationX);

        return translation
                .multiply(rotation)
                .multiply(scaleMatrix);
    }
}