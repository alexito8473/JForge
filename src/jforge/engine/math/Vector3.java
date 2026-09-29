package jforge.engine.math;

public class Vector3 {

    public double x;
    public double y;
    public double z;

    public Vector3() {

        this(0, 0, 0);
    }

    public Vector3(
            double x,
            double y,
            double z) {

        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Vector3 add(
            Vector3 other) {

        return new Vector3(
                x + other.x,
                y + other.y,
                z + other.z
        );
    }

    public Vector3 subtract(
            Vector3 other) {

        return new Vector3(
                x - other.x,
                y - other.y,
                z - other.z
        );
    }

    public Vector3 multiply(
            double value) {

        return new Vector3(
                x * value,
                y * value,
                z * value
        );
    }

    public double dot(
            Vector3 other) {

        return
                x * other.x +
                        y * other.y +
                        z * other.z;
    }

    public Vector3 cross(
            Vector3 other) {

        return new Vector3(

                y * other.z -
                        z * other.y,

                z * other.x -
                        x * other.z,

                x * other.y -
                        y * other.x
        );
    }

    public double length() {

        return Math.sqrt(
                x * x +
                        y * y +
                        z * z
        );
    }

    public Vector3 normalize() {

        double length =
                length();

        if (length == 0) {

            return new Vector3();
        }

        return new Vector3(
                x / length,
                y / length,
                z / length
        );
    }

    public Vector3 copy() {

        return new Vector3(
                x,
                y,
                z
        );
    }

    @Override
    public String toString() {

        return "Vector3(" +
                x + ", " +
                y + ", " +
                z + ")";
    }
}