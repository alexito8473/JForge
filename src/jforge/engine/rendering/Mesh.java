package jforge.engine.rendering;

import jforge.engine.math.Vector3;

public class Mesh {

    private final Vector3[] vertices;

    private final int[][] triangles;

    public Mesh(
            Vector3[] vertices,
            int[][] triangles) {

        this.vertices = vertices;
        this.triangles = triangles;
    }

    public Vector3[] getVertices() {

        return vertices;
    }

    public int[][] getTriangles() {

        return triangles;
    }

    public static Mesh createCube() {

        Vector3[] vertices = {

                new Vector3(-1, -1, -1),
                new Vector3( 1, -1, -1),
                new Vector3( 1,  1, -1),
                new Vector3(-1,  1, -1),

                new Vector3(-1, -1,  1),
                new Vector3( 1, -1,  1),
                new Vector3( 1,  1,  1),
                new Vector3(-1,  1,  1)
        };

        int[][] triangles = {

                // Frente
                {0, 1, 2},
                {0, 2, 3},

                // Atrás
                {4, 6, 5},
                {4, 7, 6},

                // Abajo
                {0, 4, 5},
                {0, 5, 1},

                // Arriba
                {2, 6, 7},
                {2, 7, 3},

                // Izquierda
                {0, 3, 7},
                {0, 7, 4},

                // Derecha
                {1, 5, 6},
                {1, 6, 2}
        };

        return new Mesh(
                vertices,
                triangles
        );
    }
}
