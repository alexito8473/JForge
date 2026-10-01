package jforge.engine.runtime;

import jforge.engine.GameObject;
import jforge.engine.Scene;
import jforge.engine.components.MeshRenderer;
import jforge.engine.math.Vector2;
import jforge.engine.math.Vector3;
import jforge.engine.rendering.camera.Camera;
import jforge.engine.rendering.material.Material;
import jforge.engine.rendering.geometry.Mesh;
import jforge.engine.rendering.RenderBackend;
import jforge.engine.rendering.material.Texture;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public final class SceneSerializer {

    private static final String HEADER = "JFORGE_SCENE 1";

    private SceneSerializer() {
    }

    public static void save(
            Scene scene,
            Camera camera,
            RenderBackend backend,
            File file
    ) throws IOException {

        save(
                scene,
                camera,
                backend,
                file,
                source -> {
                    if (source == null) {
                        return null;
                    }

                    return source.getAbsolutePath();
                }
        );
    }

    public static void save(
            Scene scene,
            Camera camera,
            RenderBackend backend,
            File file,
            Function<File, String> texturePathResolver
    ) throws IOException {

        if (scene == null) {
            throw new IllegalArgumentException(
                    "Scene no puede ser null."
            );
        }

        if (camera == null) {
            throw new IllegalArgumentException(
                    "Camera no puede ser null."
            );
        }

        if (backend == null) {
            backend = RenderBackend.OPENGL;
        }

        if (file == null) {
            throw new IllegalArgumentException(
                    "El archivo no puede ser null."
            );
        }

        File parent = file.getParentFile();

        if (parent != null) {
            Files.createDirectories(parent.toPath());
        }

        List<GameObject> objects = new ArrayList<>();

        for (GameObject object : scene.getObjects()) {
            collectObjects(object, objects);
        }

        Map<GameObject, Integer> ids =
                new HashMap<>();

        for (int i = 0; i < objects.size(); i++) {
            ids.put(objects.get(i), i);
        }

        try (
                BufferedWriter writer =
                        Files.newBufferedWriter(
                                file.toPath(),
                                StandardCharsets.UTF_8
                        )
        ) {

            writer.write(HEADER);
            writer.newLine();

            writer.write(
                    "SCENE " +
                            encode(scene.getName())
            );
            writer.newLine();

            writer.write(
                    "BACKEND " +
                            backend.name()
            );
            writer.newLine();

            Vector3 cameraPosition =
                    camera.getPosition();

            writer.write(
                    "CAMERA_POSITION " +
                            cameraPosition.x + " " +
                            cameraPosition.y + " " +
                            cameraPosition.z
            );
            writer.newLine();

            writer.write(
                    "CAMERA_ROTATION " +
                            camera.getRotationX() + " " +
                            camera.getRotationY()
            );
            writer.newLine();

            writer.write(
                    "CAMERA_PROJECTION " +
                            camera.getFieldOfView() + " " +
                            camera.getNearPlane() + " " +
                            camera.getFarPlane()
            );
            writer.newLine();

            writer.write(
                    "OBJECT_COUNT " +
                            objects.size()
            );
            writer.newLine();

            for (GameObject object : objects) {

                int id =
                        ids.get(object);

                GameObject gameObject =
                        object.getParent();

                int parentId =
                        gameObject == null
                                ? -1
                                : ids.getOrDefault(
                                gameObject,
                                -1
                        );

                writer.write(
                        "OBJECT " +
                                id + " " +
                                parentId + " " +
                                object.isActive() + " " +
                                encode(object.getName())
                );
                writer.newLine();

                Vector3 position =
                        object.getTransform()
                                .getPosition();

                Vector3 rotation =
                        object.getTransform()
                                .getRotation();

                Vector3 scale =
                        object.getTransform()
                                .getScale();

                writer.write(
                        "POSITION " +
                                position.x + " " +
                                position.y + " " +
                                position.z
                );
                writer.newLine();

                writer.write(
                        "ROTATION " +
                                rotation.x + " " +
                                rotation.y + " " +
                                rotation.z
                );
                writer.newLine();

                writer.write(
                        "SCALE " +
                                scale.x + " " +
                                scale.y + " " +
                                scale.z
                );
                writer.newLine();

                MeshRenderer meshRenderer =
                        object.getMeshRenderer();

                if (meshRenderer == null ||
                        meshRenderer.getMesh() == null) {

                    writer.write("MESH NONE");
                    writer.newLine();

                } else {

                    writeMesh(
                            writer,
                            meshRenderer.getMesh()
                    );

                    writeMaterial(
                            writer,
                            meshRenderer.getMaterial(),
                            texturePathResolver
                    );
                }

                writer.write("OBJECT_END");
                writer.newLine();
            }

            writer.write("END_SCENE");
            writer.newLine();
        }
    }

    public static LoadedScene load(
            File file
    ) throws IOException {

        if (file == null ||
                !file.isFile()) {

            throw new IOException(
                    "No existe la escena: " +
                            file
            );
        }

        String sceneName =
                "Loaded Scene";

        RenderBackend backend =
                RenderBackend.OPENGL;

        Camera camera =
                new Camera();

        List<ObjectRecord> records =
                new ArrayList<>();

        ObjectRecord currentObject = null;

        try (
                BufferedReader reader =
                        Files.newBufferedReader(
                                file.toPath(),
                                StandardCharsets.UTF_8
                        )
        ) {

            String line;

            String header =
                    reader.readLine();

            if (!HEADER.equals(header)) {

                throw new IOException(
                        "Archivo de escena no válido."
                );
            }

            while ((line = reader.readLine()) != null) {

                if (line.isBlank()) {
                    continue;
                }

                String[] parts =
                        line.split(" ", -1);

                String command =
                        parts[0];

                switch (command) {

                    case "SCENE":

                        sceneName =
                                decode(
                                        parts[1]
                                );

                        break;

                    case "BACKEND":

                        backend =
                                RenderBackend.valueOf(
                                        parts[1]
                                );

                        break;

                    case "CAMERA_POSITION":

                        camera.setPosition(
                                Double.parseDouble(parts[1]),
                                Double.parseDouble(parts[2]),
                                Double.parseDouble(parts[3])
                        );

                        break;

                    case "CAMERA_ROTATION":

                        camera.setRotationX(
                                Double.parseDouble(parts[1])
                        );

                        camera.setRotationY(
                                Double.parseDouble(parts[2])
                        );

                        break;

                    case "CAMERA_PROJECTION":

                        camera.setFieldOfView(
                                Double.parseDouble(parts[1])
                        );

                        camera.setNearPlane(
                                Double.parseDouble(parts[2])
                        );

                        camera.setFarPlane(
                                Double.parseDouble(parts[3])
                        );

                        break;

                    case "OBJECT":

                        currentObject =
                                new ObjectRecord(
                                        Integer.parseInt(parts[1]),
                                        Integer.parseInt(parts[2]),
                                        Boolean.parseBoolean(parts[3]),
                                        decode(parts[4])
                                );

                        records.add(
                                currentObject
                        );

                        break;

                    case "POSITION":

                        requireObject(currentObject);

                        currentObject.position =
                                new Vector3(
                                        Double.parseDouble(parts[1]),
                                        Double.parseDouble(parts[2]),
                                        Double.parseDouble(parts[3])
                                );

                        break;

                    case "ROTATION":

                        requireObject(currentObject);

                        currentObject.rotation =
                                new Vector3(
                                        Double.parseDouble(parts[1]),
                                        Double.parseDouble(parts[2]),
                                        Double.parseDouble(parts[3])
                                );

                        break;

                    case "SCALE":

                        requireObject(currentObject);

                        currentObject.scale =
                                new Vector3(
                                        Double.parseDouble(parts[1]),
                                        Double.parseDouble(parts[2]),
                                        Double.parseDouble(parts[3])
                                );

                        break;

                    case "MESH":

                        requireObject(currentObject);

                        if ("NONE".equals(parts[1])) {
                            currentObject.mesh = null;
                        }

                        break;

                    case "MESH_VERTICES":

                        requireObject(currentObject);

                        int vertexCount =
                                Integer.parseInt(parts[1]);

                        currentObject.vertices =
                                new Vector3[vertexCount];

                        for (int i = 0; i < vertexCount; i++) {

                            String vertexLine =
                                    reader.readLine();

                            String[] values =
                                    vertexLine.split(" ");

                            currentObject.vertices[i] =
                                    new Vector3(
                                            Double.parseDouble(values[1]),
                                            Double.parseDouble(values[2]),
                                            Double.parseDouble(values[3])
                                    );
                        }

                        break;

                    case "MESH_NORMALS":

                        requireObject(currentObject);

                        int normalCount =
                                Integer.parseInt(parts[1]);

                        currentObject.normals =
                                new Vector3[normalCount];

                        for (int i = 0; i < normalCount; i++) {

                            String normalLine =
                                    reader.readLine();

                            String[] values =
                                    normalLine.split(" ");

                            currentObject.normals[i] =
                                    new Vector3(
                                            Double.parseDouble(values[1]),
                                            Double.parseDouble(values[2]),
                                            Double.parseDouble(values[3])
                                    );
                        }

                        break;

                    case "MESH_UVS":

                        requireObject(currentObject);

                        int uvCount =
                                Integer.parseInt(parts[1]);

                        currentObject.uvs =
                                new Vector2[uvCount];

                        for (int i = 0; i < uvCount; i++) {

                            String uvLine =
                                    reader.readLine();

                            String[] values =
                                    uvLine.split(" ");

                            currentObject.uvs[i] =
                                    new Vector2(
                                            Double.parseDouble(values[1]),
                                            Double.parseDouble(values[2])
                                    );
                        }

                        break;

                    case "MESH_TRIANGLES":

                        requireObject(currentObject);

                        int triangleCount =
                                Integer.parseInt(parts[1]);

                        currentObject.triangles =
                                new int[triangleCount][3];

                        for (int i = 0; i < triangleCount; i++) {

                            String triangleLine =
                                    reader.readLine();

                            String[] values =
                                    triangleLine.split(" ");

                            currentObject.triangles[i][0] =
                                    Integer.parseInt(values[1]);

                            currentObject.triangles[i][1] =
                                    Integer.parseInt(values[2]);

                            currentObject.triangles[i][2] =
                                    Integer.parseInt(values[3]);
                        }

                        break;

                    case "MESH_END":

                        requireObject(currentObject);

                        if (currentObject.vertices != null &&
                                currentObject.triangles != null) {

                            if (currentObject.normals != null &&
                                    currentObject.uvs != null) {

                                currentObject.mesh =
                                        new Mesh(
                                                currentObject.vertices,
                                                currentObject.normals,
                                                currentObject.uvs,
                                                currentObject.triangles
                                        );

                            } else {

                                currentObject.mesh =
                                        new Mesh(
                                                currentObject.vertices,
                                                currentObject.triangles
                                        );
                            }
                        }

                        break;

                    case "MATERIAL":

                        requireObject(currentObject);

                        currentObject.material =
                                new Material(
                                        Color.WHITE
                                );

                        break;

                    case "COLOR":

                        requireObject(currentObject);

                        currentObject.color =
                                new Color(
                                        Integer.parseInt(parts[1]),
                                        Integer.parseInt(parts[2]),
                                        Integer.parseInt(parts[3]),
                                        Integer.parseInt(parts[4])
                                );

                        break;

                    case "TEXTURE":

                        requireObject(currentObject);

                        if (!"NONE".equals(parts[1])) {

                            String storedPath =
                                    decode(parts[1]);

                            File textureFile =
                                    resolveTexture(
                                            file,
                                            storedPath
                                    );

                            if (textureFile != null &&
                                    textureFile.isFile()) {

                                try {

                                    currentObject.texture =
                                            Texture.load(
                                                    textureFile
                                            );

                                } catch (IOException e) {

                                    System.err.println(
                                            "[Scene] No se pudo cargar textura: " +
                                                    textureFile
                                    );
                                }
                            }
                        }

                        break;

                    case "OBJECT_END":

                        currentObject = null;

                        break;

                    case "END_SCENE":
                        break;

                    default:
                        break;
                }
            }
        }

        Scene scene =
                new Scene(sceneName);

        Map<Integer, GameObject> loadedObjects =
                new HashMap<>();

        for (ObjectRecord record : records) {

            GameObject object =
                    new GameObject(
                            record.name
                    );

            object.setActive(
                    record.active
            );

            object.getTransform()
                    .setPosition(
                            record.position.x,
                            record.position.y,
                            record.position.z
                    );

            object.getTransform()
                    .setRotation(
                            record.rotation.x,
                            record.rotation.y,
                            record.rotation.z
                    );

            object.getTransform()
                    .setScale(
                            record.scale.x,
                            record.scale.y,
                            record.scale.z
                    );

            if (record.mesh != null) {

                Material material =
                        record.material != null
                                ? record.material
                                : new Material(
                                Color.WHITE
                        );

                if (record.color != null) {
                    material.setColor(
                            record.color
                    );
                }

                if (record.texture != null) {
                    material.setTexture(
                            record.texture
                    );
                }

                object.setMeshRenderer(
                        new MeshRenderer(
                                record.mesh,
                                material
                        )
                );
            }

            loadedObjects.put(
                    record.id,
                    object
            );
        }

        for (ObjectRecord record : records) {

            GameObject object =
                    loadedObjects.get(
                            record.id
                    );

            if (record.parentId < 0) {

                scene.add(object);

            } else {

                GameObject parent =
                        loadedObjects.get(
                                record.parentId
                        );

                if (parent != null) {
                    parent.addChild(object);
                } else {
                    scene.add(object);
                }
            }
        }

        return new LoadedScene(
                scene,
                camera,
                backend
        );
    }

    private static void writeMesh(
            BufferedWriter writer,
            Mesh mesh
    ) throws IOException {

        Vector3[] vertices =
                mesh.getVertices();

        writer.write(
                "MESH_VERTICES " +
                        vertices.length
        );
        writer.newLine();

        for (Vector3 vertex : vertices) {

            writer.write(
                    "V " +
                            vertex.x + " " +
                            vertex.y + " " +
                            vertex.z
            );

            writer.newLine();
        }

        Vector3[] normals =
                mesh.getNormals();

        if (normals != null) {

            writer.write(
                    "MESH_NORMALS " +
                            normals.length
            );

            writer.newLine();

            for (Vector3 normal : normals) {

                writer.write(
                        "N " +
                                normal.x + " " +
                                normal.y + " " +
                                normal.z
                );

                writer.newLine();
            }
        }

        Vector2[] uvs =
                mesh.getUVs();

        if (uvs != null) {

            writer.write(
                    "MESH_UVS " +
                            uvs.length
            );

            writer.newLine();

            for (Vector2 uv : uvs) {

                writer.write(
                        "U " +
                                uv.x + " " +
                                uv.y
                );

                writer.newLine();
            }
        }

        int[][] triangles =
                mesh.getTriangles();

        writer.write(
                "MESH_TRIANGLES " +
                        triangles.length
        );

        writer.newLine();

        for (int[] triangle : triangles) {

            writer.write(
                    "T " +
                            triangle[0] + " " +
                            triangle[1] + " " +
                            triangle[2]
            );

            writer.newLine();
        }

        writer.write("MESH_END");
        writer.newLine();
    }

    private static void writeMaterial(
            BufferedWriter writer,
            Material material,
            Function<File, String> texturePathResolver
    ) throws IOException {

        if (material == null) {

            writer.write("MATERIAL NONE");
            writer.newLine();

            return;
        }

        writer.write("MATERIAL YES");
        writer.newLine();

        Color color =
                material.getColor();

        if (color == null) {
            color = Color.WHITE;
        }

        writer.write(
                "COLOR " +
                        color.getRed() + " " +
                        color.getGreen() + " " +
                        color.getBlue() + " " +
                        color.getAlpha()
        );

        writer.newLine();

        Texture texture =
                material.getTexture();

        if (texture == null ||
                texture.getFile() == null) {

            writer.write("TEXTURE NONE");
            writer.newLine();

        } else {

            String path =
                    texturePathResolver.apply(
                            texture.getFile()
                    );

            if (path == null ||
                    path.isBlank()) {

                writer.write("TEXTURE NONE");
                writer.newLine();

            } else {

                writer.write(
                        "TEXTURE " +
                                encode(path)
                );

                writer.newLine();
            }
        }

        writer.write("MATERIAL_END");
        writer.newLine();
    }

    private static void collectObjects(
            GameObject object,
            List<GameObject> output
    ) {

        output.add(object);

        for (GameObject child :
                object.getChildren()) {

            collectObjects(
                    child,
                    output
            );
        }
    }

    private static void requireObject(
            ObjectRecord record
    ) {

        if (record == null) {
            throw new IllegalStateException(
                    "Datos de objeto fuera de OBJECT/OBJECT_END."
            );
        }
    }

    private static String encode(
            String value
    ) {

        if (value == null) {
            value = "";
        }

        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(
                        value.getBytes(
                                StandardCharsets.UTF_8
                        )
                );
    }

    private static String decode(
            String value
    ) {

        if (value == null ||
                value.isBlank()) {

            return "";
        }

        return new String(
                Base64.getUrlDecoder()
                        .decode(value),
                StandardCharsets.UTF_8
        );
    }

    private static File resolveTexture(
            File sceneFile,
            String storedPath
    ) {

        File candidate =
                new File(storedPath);

        if (candidate.isAbsolute()) {
            return candidate;
        }

        File sceneDirectory =
                sceneFile.getParentFile();

        if (sceneDirectory == null) {
            return candidate;
        }

        return new File(
                sceneDirectory,
                storedPath
        );
    }

    private static class ObjectRecord {

        private final int id;
        private final int parentId;
        private final boolean active;
        private final String name;

        private Vector3 position =
                new Vector3();

        private Vector3 rotation =
                new Vector3();

        private Vector3 scale =
                new Vector3(1, 1, 1);

        private Vector3[] vertices;
        private Vector3[] normals;
        private Vector2[] uvs;
        private int[][] triangles;

        private Mesh mesh;

        private Material material;

        private Color color;

        private Texture texture;

        private ObjectRecord(
                int id,
                int parentId,
                boolean active,
                String name
        ) {
            this.id = id;
            this.parentId = parentId;
            this.active = active;
            this.name = name;
        }
    }
}