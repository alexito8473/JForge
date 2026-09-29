package jforge.engine.math;

public class Matrix4 {

    private final double[][] m;

    public Matrix4() {
        m = new double[4][4];
    }

    public static Matrix4 identity() {

        Matrix4 result = new Matrix4();

        for (int i = 0; i < 4; i++) {
            result.m[i][i] = 1.0;
        }

        return result;
    }

    public static Matrix4 translation(
            double x,
            double y,
            double z) {

        Matrix4 result = identity();

        result.m[0][3] = x;
        result.m[1][3] = y;
        result.m[2][3] = z;

        return result;
    }

    public static Matrix4 scale(
            double x,
            double y,
            double z) {

        Matrix4 result = identity();

        result.m[0][0] = x;
        result.m[1][1] = y;
        result.m[2][2] = z;

        return result;
    }

    public static Matrix4 rotationX(
            double degrees) {

        double radians =
                Math.toRadians(degrees);

        double cos =
                Math.cos(radians);

        double sin =
                Math.sin(radians);

        Matrix4 result = identity();

        result.m[1][1] = cos;
        result.m[1][2] = -sin;

        result.m[2][1] = sin;
        result.m[2][2] = cos;

        return result;
    }

    public static Matrix4 rotationY(
            double degrees) {

        double radians =
                Math.toRadians(degrees);

        double cos =
                Math.cos(radians);

        double sin =
                Math.sin(radians);

        Matrix4 result = identity();

        result.m[0][0] = cos;
        result.m[0][2] = sin;

        result.m[2][0] = -sin;
        result.m[2][2] = cos;

        return result;
    }

    public static Matrix4 rotationZ(
            double degrees) {

        double radians =
                Math.toRadians(degrees);

        double cos =
                Math.cos(radians);

        double sin =
                Math.sin(radians);

        Matrix4 result = identity();

        result.m[0][0] = cos;
        result.m[0][1] = -sin;

        result.m[1][0] = sin;
        result.m[1][1] = cos;

        return result;
    }

    public Matrix4 multiply(
            Matrix4 other) {

        Matrix4 result =
                new Matrix4();

        for (int row = 0; row < 4; row++) {

            for (int column = 0; column < 4; column++) {

                double value = 0;

                for (int i = 0; i < 4; i++) {

                    value +=
                            m[row][i] *
                                    other.m[i][column];
                }

                result.m[row][column] =
                        value;
            }
        }

        return result;
    }

    public Vector3 transform(
            Vector3 vector) {

        double x =
                m[0][0] * vector.x +
                        m[0][1] * vector.y +
                        m[0][2] * vector.z +
                        m[0][3];

        double y =
                m[1][0] * vector.x +
                        m[1][1] * vector.y +
                        m[1][2] * vector.z +
                        m[1][3];

        double z =
                m[2][0] * vector.x +
                        m[2][1] * vector.y +
                        m[2][2] * vector.z +
                        m[2][3];

        double w =
                m[3][0] * vector.x +
                        m[3][1] * vector.y +
                        m[3][2] * vector.z +
                        m[3][3];

        if (w != 0 && w != 1) {

            x /= w;
            y /= w;
            z /= w;
        }

        return new Vector3(
                x,
                y,
                z
        );
    }
}