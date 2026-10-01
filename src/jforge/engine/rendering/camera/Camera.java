package jforge.engine.rendering.camera;

import jforge.engine.math.Vector3;

public class Camera {

    private final Vector3 position;

    private double rotationX;
    private double rotationY;

    private double fieldOfView;

    private double nearPlane;
    private double farPlane;

    public Camera() {

        position =
                new Vector3(
                        0,
                        0,
                        0
                );

        rotationX = 0.0;
        rotationY = 0.0;

        fieldOfView = 70.0;

        nearPlane = 0.1;
        farPlane = 1000.0;
    }

    public Vector3 getPosition() {

        return position;
    }

    public void setPosition(
            double x,
            double y,
            double z) {

        position.x = x;
        position.y = y;
        position.z = z;
    }

    public void translate(
            double x,
            double y,
            double z) {

        position.x += x;
        position.y += y;
        position.z += z;
    }

    public double getRotationX() {

        return rotationX;
    }

    public void setRotationX(
            double rotationX) {

        this.rotationX =
                rotationX;

        clampRotation();
    }

    public double getRotationY() {

        return rotationY;
    }

    public void setRotationY(
            double rotationY) {

        this.rotationY =
                rotationY;

        normalizeRotationY();
    }

    public void rotate(
            double x,
            double y) {

        rotationX += x;
        rotationY += y;

        clampRotation();
        normalizeRotationY();
    }

    private void clampRotation() {

        rotationX =
                Math.max(
                        -89.0,
                        Math.min(
                                89.0,
                                rotationX
                        )
                );
    }

    private void normalizeRotationY() {

        while (rotationY >= 360.0) {

            rotationY -= 360.0;
        }

        while (rotationY < 0.0) {

            rotationY += 360.0;
        }
    }

    /*
     * Dirección EXACTA hacia donde mira
     * la cámara.
     *
     * Con rotación 0:
     *
     * X =  0
     * Y =  0
     * Z = -1
     *
     * Es decir:
     *
     * W -> -Z
     */
    public Vector3 getForward() {

        double yaw =
                Math.toRadians(
                        rotationY
                );

        double pitch =
                Math.toRadians(
                        rotationX
                );

        double cosPitch =
                Math.cos(
                        pitch
                );

        double sinPitch =
                Math.sin(
                        pitch
                );

        double sinYaw =
                Math.sin(
                        yaw
                );

        double cosYaw =
                Math.cos(
                        yaw
                );

        return new Vector3(

                -sinYaw * cosPitch,

                sinPitch,

                -cosYaw * cosPitch

        ).normalize();
    }

    /*
     * Dirección hacia la derecha
     * de la cámara.
     */
    public Vector3 getRight() {

        Vector3 forward =
                getForward();

        Vector3 worldUp =
                new Vector3(
                        0,
                        1,
                        0
                );

        return worldUp
                .cross(forward)
                .normalize();
    }

    /*
     * Movimiento hacia delante.
     *
     * W:
     *
     * posición += forward
     */
    public void moveForward(
            double distance) {

        Vector3 forward =
                getForward();

        position.x +=
                forward.x *
                        distance;

        position.y +=
                forward.y *
                        distance;

        position.z +=
                forward.z *
                        distance;
    }

    /*
     * Movimiento hacia atrás.
     *
     * S:
     *
     * posición -= forward
     */
    public void moveBackward(
            double distance) {

        moveForward(
                -distance
        );
    }

    /*
     * Movimiento lateral
     * respecto a la cámara.
     */
    public void moveRight(
            double distance) {

        Vector3 right =
                getRight();

        position.x +=
                right.x *
                        distance;

        position.y +=
                right.y *
                        distance;

        position.z +=
                right.z *
                        distance;
    }
    public void moveLeft(
            double distance) {

        moveRight(
                -distance
        );
    }

    /*
     * Movimiento vertical
     * del mundo.
     */
    public void moveUp(
            double distance) {

        position.y +=
                distance;
    }

    public double getFieldOfView() {

        return fieldOfView;
    }

    public void setFieldOfView(
            double fieldOfView) {

        this.fieldOfView =
                Math.max(
                        1.0,
                        Math.min(
                                179.0,
                                fieldOfView
                        )
                );
    }

    public double getNearPlane() {

        return nearPlane;
    }

    public void setNearPlane(
            double nearPlane) {

        this.nearPlane =
                Math.max(
                        0.001,
                        nearPlane
                );
    }

    public double getFarPlane() {

        return farPlane;
    }

    public void setFarPlane(
            double farPlane) {

        this.farPlane =
                Math.max(
                        nearPlane + 0.001,
                        farPlane
                );
    }
}
