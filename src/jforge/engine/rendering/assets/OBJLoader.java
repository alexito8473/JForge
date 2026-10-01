package jforge.engine.rendering.assets;

import jforge.engine.math.Vector2;
import jforge.engine.math.Vector3;
import jforge.engine.rendering.geometry.Mesh;
import jforge.engine.rendering.material.LoadedModel;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OBJLoader {

    private OBJLoader() {
    }

    public static Mesh load(File file) throws IOException {

        return loadModel(
                file
        ).getMesh();
    }

    public static LoadedModel loadModel(
            File file) throws IOException {

        List<Vector3> sourcePositions =
                new ArrayList<>();

        List<Vector2> sourceUVs =
                new ArrayList<>();

        List<Vector3> sourceNormals =
                new ArrayList<>();

        List<Vector3> vertices =
                new ArrayList<>();

        List<Vector2> uvs =
                new ArrayList<>();

        List<Vector3> normals =
                new ArrayList<>();

        List<int[]> triangles =
                new ArrayList<>();

        Map<String, Integer> vertexMap =
                new HashMap<>();

        File mtlFile =
                null;

        boolean[] vertexHasNormal =
                new boolean[0];

        try (
                BufferedReader reader =
                        new BufferedReader(
                                new FileReader(
                                        file
                                )
                        )
        ) {

            String line;

            while (
                    (line = reader.readLine())
                            != null
            ) {

                line =
                        line.trim();

                if (line.isEmpty() ||
                        line.startsWith("#")) {

                    continue;
                }

                /*
                 * VERTEX
                 */
                if (line.startsWith("v ")) {

                    String[] parts =
                            line.split(
                                    "\\s+"
                            );

                    if (parts.length < 4) {
                        continue;
                    }

                    sourcePositions.add(
                            new Vector3(
                                    Double.parseDouble(
                                            parts[1]
                                    ),
                                    Double.parseDouble(
                                            parts[2]
                                    ),
                                    Double.parseDouble(
                                            parts[3]
                                    )
                            )
                    );

                    continue;
                }

                /*
                 * UV
                 */
                if (line.startsWith("vt ")) {

                    String[] parts =
                            line.split(
                                    "\\s+"
                            );

                    if (parts.length < 3) {
                        continue;
                    }

                    sourceUVs.add(
                            new Vector2(
                                    Double.parseDouble(
                                            parts[1]
                                    ),
                                    1.0 -
                                            Double.parseDouble(
                                                    parts[2]
                                            )
                            )
                    );

                    continue;
                }

                /*
                 * NORMAL
                 */
                if (line.startsWith("vn ")) {

                    String[] parts =
                            line.split(
                                    "\\s+"
                            );

                    if (parts.length < 4) {
                        continue;
                    }

                    sourceNormals.add(
                            new Vector3(
                                    Double.parseDouble(
                                            parts[1]
                                    ),
                                    Double.parseDouble(
                                            parts[2]
                                    ),
                                    Double.parseDouble(
                                            parts[3]
                                    )
                            )
                    );

                    continue;
                }

                /*
                 * MATERIAL LIBRARY
                 */
                if (line.startsWith("mtllib ")) {

                    String name =
                            line.substring(
                                    7
                            ).trim();

                    mtlFile =
                            new File(
                                    file.getParentFile(),
                                    name
                            );

                    continue;
                }

                /*
                 * FACE
                 */
                if (line.startsWith("f ")) {

                    String[] parts =
                            line.split(
                                    "\\s+"
                            );

                    if (parts.length < 4) {
                        continue;
                    }

                    List<Integer> faceIndices =
                            new ArrayList<>();

                    for (int i = 1;
                         i < parts.length;
                         i++) {

                        String token =
                                parts[i];

                        Integer index =
                                vertexMap.get(
                                        token
                                );

                        if (index == null) {

                            index =
                                    createVertex(
                                            token,
                                            sourcePositions,
                                            sourceUVs,
                                            sourceNormals,
                                            vertices,
                                            uvs,
                                            normals
                                    );

                            vertexMap.put(
                                    token,
                                    index
                            );
                        }

                        faceIndices.add(
                                index
                        );
                    }

                    /*
                     * Triangularizar polígono.
                     */
                    for (int i = 1;
                         i < faceIndices.size() - 1;
                         i++) {

                        triangles.add(
                                new int[] {
                                        faceIndices.get(0),
                                        faceIndices.get(i),
                                        faceIndices.get(i + 1)
                                }
                        );
                    }
                }
            }
        }

        /*
         * Si faltan normales, calcularlas.
         */
        calculateMissingNormals(
                vertices,
                normals,
                triangles,
                sourceNormals
        );

        Mesh mesh =
                new Mesh(
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

        /*
         * Buscar textura diffuse.
         */
        File diffuseTexture =
                findDiffuseTexture(
                        mtlFile
                );

        return new LoadedModel(
                mesh,
                diffuseTexture
        );
    }

    private static int createVertex(
            String token,
            List<Vector3> sourcePositions,
            List<Vector2> sourceUVs,
            List<Vector3> sourceNormals,
            List<Vector3> vertices,
            List<Vector2> uvs,
            List<Vector3> normals) {

        String[] parts =
                token.split(
                        "/",
                        -1
                );

        /*
         * Posición
         */
        int positionIndex =
                parseIndex(
                        parts[0],
                        sourcePositions.size()
                );

        Vector3 position =
                sourcePositions.get(
                        positionIndex
                );

        vertices.add(
                position.copy()
        );

        /*
         * UV
         */
        Vector2 uv =
                new Vector2(
                        0,
                        0
                );

        if (parts.length > 1 &&
                !parts[1].isEmpty()) {

            int uvIndex =
                    parseIndex(
                            parts[1],
                            sourceUVs.size()
                    );

            if (uvIndex >= 0 &&
                    uvIndex < sourceUVs.size()) {

                uv =
                        sourceUVs
                                .get(
                                        uvIndex
                                )
                                .copy();
            }
        }

        uvs.add(
                uv
        );

        /*
         * Normal
         */
        Vector3 normal =
                new Vector3();

        if (parts.length > 2 &&
                !parts[2].isEmpty()) {

            int normalIndex =
                    parseIndex(
                            parts[2],
                            sourceNormals.size()
                    );

            if (normalIndex >= 0 &&
                    normalIndex < sourceNormals.size()) {

                normal =
                        sourceNormals
                                .get(
                                        normalIndex
                                )
                                .copy()
                                .normalize();
            }
        }

        normals.add(
                normal
        );

        return vertices.size() - 1;
    }

    private static void calculateMissingNormals(
            List<Vector3> vertices,
            List<Vector3> normals,
            List<int[]> triangles,
            List<Vector3> sourceNormals) {

        /*
         * Si ya existen normales del OBJ,
         * respetarlas.
         */
        if (!sourceNormals.isEmpty()) {

            for (int i = 0;
                 i < normals.size();
                 i++) {

                if (normals.get(i).length()
                        < 0.000001) {

                    normals.set(
                            i,
                            new Vector3(
                                    0,
                                    0,
                                    0
                            )
                    );
                }
            }
        }

        /*
         * Acumulador.
         */
        Vector3[] accumulated =
                new Vector3[
                        vertices.size()
                        ];

        boolean[] missing =
                new boolean[
                        vertices.size()
                        ];

        for (int i = 0;
             i < vertices.size();
             i++) {

            accumulated[i] =
                    new Vector3();

            missing[i] =
                    normals.get(i)
                            .length()
                            < 0.000001;
        }

        for (int[] triangle :
                triangles) {

            Vector3 a =
                    vertices.get(
                            triangle[0]
                    );

            Vector3 b =
                    vertices.get(
                            triangle[1]
                    );

            Vector3 c =
                    vertices.get(
                            triangle[2]
                    );

            Vector3 normal =
                    b.subtract(a)
                            .cross(
                                    c.subtract(a)
                            )
                            .normalize();

            for (int index :
                    triangle) {

                if (missing[index]) {

                    accumulated[index] =
                            accumulated[index]
                                    .add(
                                            normal
                                    );
                }
            }
        }

        for (int i = 0;
             i < vertices.size();
             i++) {

            if (missing[i]) {

                normals.set(
                        i,
                        accumulated[i]
                                .normalize()
                );
            }
        }
    }

    private static int parseIndex(
            String value,
            int size) {

        int index =
                Integer.parseInt(
                        value
                );

        if (index > 0) {

            return index - 1;
        }

        if (index < 0) {

            return size + index;
        }

        return -1;
    }

    private static File findDiffuseTexture(
            File mtlFile) {

        if (mtlFile == null ||
                !mtlFile.exists()) {

            return null;
        }

        try (
                BufferedReader reader =
                        new BufferedReader(
                                new FileReader(
                                        mtlFile
                                )
                        )
        ) {

            String line;

            while (
                    (line = reader.readLine())
                            != null
            ) {

                line =
                        line.trim();

                if (line.startsWith(
                        "map_Kd "
                )) {

                    String textureName =
                            line.substring(
                                    7
                            ).trim();

                    File texture =
                            new File(
                                    mtlFile.getParentFile(),
                                    textureName
                            );

                    if (texture.exists()) {

                        return texture;
                    }
                }
            }

        } catch (IOException ignored) {

        }

        return null;
    }
}