package jforge.engine.rendering;

import jforge.engine.math.Vector3;

public class Triangle {

    public Vector3 a;

    public Vector3 b;

    public Vector3 c;

    public Triangle(
            Vector3 a,
            Vector3 b,
            Vector3 c) {

        this.a = a;

        this.b = b;

        this.c = c;
    }

    public Vector3 getNormal() {

        Vector3 ab =
                b.subtract(a);

        Vector3 ac =
                c.subtract(a);

        return ab
                .cross(ac)
                .normalize();
    }
}