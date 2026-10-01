package jforge.engine.rendering.geometry;

import jforge.engine.math.Vector3;
import jforge.engine.math.Vector2;
import java.util.ArrayList;
import java.util.List;

public class Mesh {

    private final Vector3[] vertices;

    private final Vector3[] normals;

    private final Vector2[] uvs;

    private final int[][] triangles;

    /*
     * Constructor antiguo.
     *
     * Permite que el código existente
     * siga funcionando.
     */
    public Mesh(
            Vector3[] vertices,
            int[][] triangles) {

        this(
                vertices,
                calculateNormals(
                        vertices,
                        triangles
                ),
                createDefaultUVs(
                        vertices.length
                ),
                triangles
        );
    }

    /*
     * Nuevo constructor completo.
     */
    public Mesh(
            Vector3[] vertices,
            Vector3[] normals,
            Vector2[] uvs,
            int[][] triangles) {

        this.vertices =
                vertices;

        this.normals =
                normals;

        this.uvs =
                uvs;

        this.triangles =
                triangles;
    }

    public Vector3[] getVertices() {

        return vertices;
    }

    public Vector3[] getNormals() {

        return normals;
    }

    public Vector2[] getUVs() {

        return uvs;
    }

    public int[][] getTriangles() {

        return triangles;
    }

    /*
     * =====================================================
     * CUBO
     * =====================================================
     *
     * Usamos 24 vértices, cuatro por cara,
     * para que cada cara pueda tener su
     * propia normal y UV.
     */
    public static Mesh createCube() {

        List<Vector3> vertices =
                new ArrayList<>();

        List<Vector3> normals =
                new ArrayList<>();

        List<Vector2> uvs =
                new ArrayList<>();

        List<int[]> triangles =
                new ArrayList<>();

        addFace(
                vertices,
                normals,
                uvs,
                triangles,

                new Vector3(
                        -1, -1, -1
                ),

                new Vector3(
                        1, -1, -1
                ),

                new Vector3(
                        1,  1, -1
                ),

                new Vector3(
                        -1,  1, -1
                ),

                new Vector3(
                        0, 0, -1
                )
        );

        addFace(
                vertices,
                normals,
                uvs,
                triangles,

                new Vector3(
                        1, -1, 1
                ),

                new Vector3(
                        -1, -1, 1
                ),

                new Vector3(
                        -1,  1, 1
                ),

                new Vector3(
                        1,  1, 1
                ),

                new Vector3(
                        0, 0, 1
                )
        );

        addFace(
                vertices,
                normals,
                uvs,
                triangles,

                new Vector3(
                        -1, -1, 1
                ),

                new Vector3(
                        1, -1, 1
                ),

                new Vector3(
                        1, -1, -1
                ),

                new Vector3(
                        -1, -1, -1
                ),

                new Vector3(
                        0, -1, 0
                )
        );

        addFace(
                vertices,
                normals,
                uvs,
                triangles,

                new Vector3(
                        -1, 1, -1
                ),

                new Vector3(
                        1, 1, -1
                ),

                new Vector3(
                        1, 1, 1
                ),

                new Vector3(
                        -1, 1, 1
                ),

                new Vector3(
                        0, 1, 0
                )
        );

        addFace(
                vertices,
                normals,
                uvs,
                triangles,

                new Vector3(
                        -1, -1, 1
                ),

                new Vector3(
                        -1, -1, -1
                ),

                new Vector3(
                        -1,  1, -1
                ),

                new Vector3(
                        -1,  1, 1
                ),

                new Vector3(
                        -1, 0, 0
                )
        );

        addFace(
                vertices,
                normals,
                uvs,
                triangles,

                new Vector3(
                        1, -1, -1
                ),

                new Vector3(
                        1, -1, 1
                ),

                new Vector3(
                        1,  1, 1
                ),

                new Vector3(
                        1,  1, -1
                ),

                new Vector3(
                        1, 0, 0
                )
        );

        return new Mesh(
                vertices.toArray(
                        new Vector3[0]
                ),
                normals.toArray(
                        new Vector3[0]
                ),
                uvs.toArray(
                        new Vector2[0]
                ),
                triangles.toArray(
                        new int[0][]
                )
        );
    }

    private static void addFace(
            List<Vector3> vertices,
            List<Vector3> normals,
            List<Vector2> uvs,
            List<int[]> triangles,

            Vector3 a,
            Vector3 b,
            Vector3 c,
            Vector3 d,

            Vector3 normal) {

        int start =
                vertices.size();

        vertices.add(a);
        vertices.add(b);
        vertices.add(c);
        vertices.add(d);

        normals.add(
                normal.copy()
        );

        normals.add(
                normal.copy()
        );

        normals.add(
                normal.copy()
        );

        normals.add(
                normal.copy()
        );

        uvs.add(
                new Vector2(
                        0, 0
                )
        );

        uvs.add(
                new Vector2(
                        1, 0
                )
        );

        uvs.add(
                new Vector2(
                        1, 1
                )
        );

        uvs.add(
                new Vector2(
                        0, 1
                )
        );

        triangles.add(
                new int[] {
                        start,
                        start + 1,
                        start + 2
                }
        );

        triangles.add(
                new int[] {
                        start,
                        start + 2,
                        start + 3
                }
        );
    }

    private static Vector2[] createDefaultUVs(
            int count) {

        Vector2[] result =
                new Vector2[count];

        for (int i = 0;
             i < count;
             i++) {

            result[i] =
                    new Vector2(
                            0,
                            0
                    );
        }

        return result;
    }

    private static Vector3[] calculateNormals(
            Vector3[] vertices,
            int[][] triangles) {

        Vector3[] result =
                new Vector3[
                        vertices.length
                        ];

        for (int i = 0;
             i < result.length;
             i++) {

            result[i] =
                    new Vector3();
        }

        for (int[] triangle :
                triangles) {

            if (triangle == null ||
                    triangle.length < 3) {

                continue;
            }

            Vector3 a =
                    vertices[
                            triangle[0]
                            ];

            Vector3 b =
                    vertices[
                            triangle[1]
                            ];

            Vector3 c =
                    vertices[
                            triangle[2]
                            ];

            Vector3 normal =
                    b.subtract(a)
                            .cross(
                                    c.subtract(a)
                            )
                            .normalize();

            result[
                    triangle[0]
                    ] =
                    result[
                            triangle[0]
                            ].add(
                            normal
                    );

            result[
                    triangle[1]
                    ] =
                    result[
                            triangle[1]
                            ].add(
                            normal
                    );

            result[
                    triangle[2]
                    ] =
                    result[
                            triangle[2]
                            ].add(
                            normal
                    );
        }

        for (int i = 0;
             i < result.length;
             i++) {

            result[i] =
                    result[i]
                            .normalize();
        }

        return result;
    }
}