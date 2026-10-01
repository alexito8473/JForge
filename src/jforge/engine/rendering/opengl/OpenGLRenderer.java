package jforge.engine.rendering.opengl;

import jforge.engine.GameObject;
import jforge.engine.Scene;
import jforge.engine.components.MeshRenderer;
import jforge.engine.debug.FPSCounter;
import jforge.engine.debug.RenderStats;
import jforge.engine.math.Vector2;
import jforge.engine.math.Vector3;
import jforge.engine.rendering.camera.Camera;
import jforge.engine.rendering.gpu.GPURenderConfig;
import jforge.engine.rendering.material.Material;
import jforge.engine.rendering.geometry.Mesh;
import jforge.engine.rendering.RenderBackend;
import jforge.engine.rendering.Renderer;
import jforge.engine.rendering.material.Texture;

import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.awt.AWTGLCanvas;
import org.lwjgl.opengl.awt.GLData;
import org.lwjgl.system.MemoryUtil;

import javax.swing.SwingUtilities;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_DEPTH_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_DEPTH_TEST;
import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL11.GL_LINEAR;
import static org.lwjgl.opengl.GL11.GL_LINEAR_MIPMAP_LINEAR;
import static org.lwjgl.opengl.GL11.GL_LESS;
import static org.lwjgl.opengl.GL11.GL_RENDERER;
import static org.lwjgl.opengl.GL11.GL_RGBA;
import static org.lwjgl.opengl.GL11.GL_RGBA8;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_MAG_FILTER;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_MIN_FILTER;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_WRAP_S;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_WRAP_T;
import static org.lwjgl.opengl.GL11.GL_REPEAT;
import static org.lwjgl.opengl.GL11.GL_TRIANGLES;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_BYTE;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_INT;
import static org.lwjgl.opengl.GL11.GL_VENDOR;
import static org.lwjgl.opengl.GL11.GL_VERSION;

import static org.lwjgl.opengl.GL11.glBindTexture;
import static org.lwjgl.opengl.GL11.glClear;
import static org.lwjgl.opengl.GL11.glClearColor;
import static org.lwjgl.opengl.GL11.glDeleteTextures;
import static org.lwjgl.opengl.GL11.glDepthFunc;
import static org.lwjgl.opengl.GL11.glDisable;
import static org.lwjgl.opengl.GL11.glDrawElements;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL11.glGenTextures;
import static org.lwjgl.opengl.GL11.glGetString;
import static org.lwjgl.opengl.GL11.glTexImage2D;
import static org.lwjgl.opengl.GL11.glTexParameteri;
import static org.lwjgl.opengl.GL11.glViewport;

import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.glActiveTexture;

import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.GL_ELEMENT_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.GL_STATIC_DRAW;
import static org.lwjgl.opengl.GL15.glBindBuffer;
import static org.lwjgl.opengl.GL15.glBufferData;
import static org.lwjgl.opengl.GL15.glDeleteBuffers;
import static org.lwjgl.opengl.GL15.glGenBuffers;

import static org.lwjgl.opengl.GL20.GL_COMPILE_STATUS;
import static org.lwjgl.opengl.GL20.GL_FRAGMENT_SHADER;
import static org.lwjgl.opengl.GL20.GL_LINK_STATUS;
import static org.lwjgl.opengl.GL20.GL_VERTEX_SHADER;
import static org.lwjgl.opengl.GL20.glAttachShader;
import static org.lwjgl.opengl.GL20.glCompileShader;
import static org.lwjgl.opengl.GL20.glCreateProgram;
import static org.lwjgl.opengl.GL20.glCreateShader;
import static org.lwjgl.opengl.GL20.glDeleteProgram;
import static org.lwjgl.opengl.GL20.glDeleteShader;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glGetProgramInfoLog;
import static org.lwjgl.opengl.GL20.glGetProgrami;
import static org.lwjgl.opengl.GL20.glGetShaderInfoLog;
import static org.lwjgl.opengl.GL20.glGetShaderi;
import static org.lwjgl.opengl.GL20.glGetUniformLocation;
import static org.lwjgl.opengl.GL20.glLinkProgram;
import static org.lwjgl.opengl.GL20.glShaderSource;
import static org.lwjgl.opengl.GL20.glUniform1i;
import static org.lwjgl.opengl.GL20.glUniform3f;
import static org.lwjgl.opengl.GL20.glUniform4f;
import static org.lwjgl.opengl.GL20.glUniformMatrix4fv;
import static org.lwjgl.opengl.GL20.glUseProgram;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;

import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL30.glDeleteVertexArrays;
import static org.lwjgl.opengl.GL30.glGenerateMipmap;
import static org.lwjgl.opengl.GL30.glGenVertexArrays;

public class OpenGLRenderer
        implements Renderer {
    private GPURenderConfig gpuConfiguration = GPURenderConfig.auto();
    private final Scene scene;

    private final Camera camera;

    private final AWTGLCanvas canvas;

    private final Map<Mesh, GPUModel> gpuModels;

    private final Map<Texture, Integer> gpuTextures;

    private final AtomicBoolean renderQueued;

    private final FPSCounter fpsCounter;

    private final RenderStats renderStats;

    private int shaderProgram;

    private int mvpLocation;

    private int modelLocation;

    private int colorLocation;

    private int textureLocation;

    private int useTextureLocation;

    private int lightDirectionLocation;

    private boolean initialized;

    private boolean showDebug;

    public OpenGLRenderer(
            Scene scene) {

        if (scene == null) {

            throw new IllegalArgumentException(
                    "Scene no puede ser null."
            );
        }

        this.scene =
                scene;

        this.camera =
                new Camera();

        this.gpuModels =
                new IdentityHashMap<>();

        this.gpuTextures =
                new IdentityHashMap<>();

        this.renderQueued =
                new AtomicBoolean(
                        false
                );

        this.fpsCounter =
                new FPSCounter();

        this.renderStats =
                new RenderStats();

        this.initialized =
                false;

        this.showDebug =
                true;

        GLData data =
                new GLData();

        /*
         * OpenGL 3.3 Core.
         */
        data.majorVersion =
                3;

        data.minorVersion =
                3;

        data.profile =
                GLData.Profile.CORE;
        /*
         * VSync OFF
         * 0 = no esperar al refresco del monitor
         */
        data.swapInterval = 0;
        this.canvas =
                new AWTGLCanvas(
                        data
                ) {

                    private static final long
                            serialVersionUID =
                            1L;

                    @Override
                    public void initGL() {

                        initializeOpenGL();
                    }

                    @Override
                    public void paintGL() {

                        renderFrame();
                    }
                };

        canvas.setPreferredSize(
                new Dimension(
                        900,
                        600
                )
        );

        canvas.setFocusable(
                true
        );
    }

    /*
     * =========================================================
     * OPENGL INITIALIZATION
     * =========================================================
     */

    private void initializeOpenGL() {

        /*
         * El contexto ya está actual dentro
         * de paintGL/initGL.
         */
        GL.createCapabilities();
        String glVersion =
                GL11.glGetString(GL11.GL_VERSION);

        String glRenderer =
                GL11.glGetString(GL11.GL_RENDERER);

        String glVendor =
                GL11.glGetString(GL11.GL_VENDOR);

        System.out.println();
        System.out.println("========== JForge OpenGL ==========");
        System.out.println("GL_VERSION  : " + glVersion);
        System.out.println("GL_RENDERER : " + glRenderer);
        System.out.println("GL_VENDOR   : " + glVendor);
        System.out.println("==================================");
        System.out.println();
        System.out.println(
                "================================="
        );

        System.out.println(
                "JForge OpenGL"
        );

        System.out.println(
                "OpenGL: " +
                        glGetString(
                                GL_VERSION
                        )
        );

        System.out.println(
                "GPU: " +
                        glGetString(
                                GL_RENDERER
                        )
        );

        System.out.println(
                "Vendor: " +
                        glGetString(
                                GL_VENDOR
                        )
        );

        System.out.println(
                "================================="
        );

        /*
         * Shaders.
         */
        shaderProgram =
                createShaderProgram();

        mvpLocation =
                glGetUniformLocation(
                        shaderProgram,
                        "uMVP"
                );

        modelLocation =
                glGetUniformLocation(
                        shaderProgram,
                        "uModel"
                );

        colorLocation =
                glGetUniformLocation(
                        shaderProgram,
                        "uColor"
                );

        textureLocation =
                glGetUniformLocation(
                        shaderProgram,
                        "uTexture"
                );

        useTextureLocation =
                glGetUniformLocation(
                        shaderProgram,
                        "uUseTexture"
                );

        lightDirectionLocation =
                glGetUniformLocation(
                        shaderProgram,
                        "uLightDirection"
                );

        /*
         * Depth testing por hardware.
         */
        glEnable(
                GL_DEPTH_TEST
        );

        glDepthFunc(
                GL_LESS
        );

        /*
         * Dejamos el backface culling
         * desactivado hasta tener todo
         * el pipeline sólido.
         */
        glDisable(
                org.lwjgl.opengl.GL11.GL_CULL_FACE
        );

        glClearColor(
                0.05f,
                0.05f,
                0.07f,
                1.0f
        );

        initialized =
                true;
    }

    /*
     * =========================================================
     * FRAME
     * =========================================================
     */

    private void renderFrame() {

        if (!initialized) {
            return;
        }

        long frameStart =
                System.nanoTime();

        int width =
                canvas.getFramebufferWidth();

        int height =
                canvas.getFramebufferHeight();

        if (width <= 0 ||
                height <= 0) {

            return;
        }

        /*
         * Viewport.
         */
        glViewport(
                0,
                0,
                width,
                height
        );

        /*
         * CLEAR
         */
        long clearStart =
                System.nanoTime();

        glClear(
                GL_COLOR_BUFFER_BIT |
                        GL_DEPTH_BUFFER_BIT
        );

        long clearEnd =
                System.nanoTime();

        /*
         * Matrices de cámara.
         */
        float[] projection =
                createProjectionMatrix(
                        width,
                        height
                );

        float[] view =
                createViewMatrix();

        /*
         * Shader.
         */
        glUseProgram(
                shaderProgram
        );

        /*
         * Luz básica.
         *
         * La dirección apunta desde la escena
         * hacia la fuente.
         */
        glUniform3f(
                lightDirectionLocation,
                -0.5f,
                -1.0f,
                -0.5f
        );

        int objects =
                0;

        int triangles =
                0;

        long renderStart =
                System.nanoTime();

        /*
         * ESCENA
         */
        for (GameObject object :
                scene.getObjects()) {

            RenderResult result =
                    renderHierarchy(
                            object,
                            identity(),
                            view,
                            projection
                    );

            objects +=
                    result.objects;

            triangles +=
                    result.triangles;
        }

        long renderEnd =
                System.nanoTime();

        glUseProgram(
                0
        );

        /*
         * IMPORTANTE:
         *
         * swapBuffers() se realiza dentro
         * de paintGL().
         */
        canvas.swapBuffers();

        long frameEnd =
                System.nanoTime();

        /*
         * FPS.
         */
        fpsCounter.update();

        double frameTime =
                (
                        frameEnd -
                                frameStart
                )
                        /
                        1_000_000.0;

        double clearTime =
                (
                        clearEnd -
                                clearStart
                )
                        /
                        1_000_000.0;

        double renderTime =
                (
                        renderEnd -
                                renderStart
                )
                        /
                        1_000_000.0;

        renderStats.setFPS(
                fpsCounter.getFPS()
        );

        renderStats.setTotalFrames(
                fpsCounter.getTotalFrames()
        );

        renderStats.setFrameTime(
                frameTime
        );

        renderStats.setClearTime(
                clearTime
        );

        renderStats.setRenderTime(
                renderTime
        );

        renderStats.setObjects(
                objects
        );

        renderStats.setTriangles(
                triangles
        );
    }

    /*
     * =========================================================
     * HIERARCHY
     * =========================================================
     */

    private RenderResult renderHierarchy(
            GameObject object,
            float[] parentWorld,
            float[] view,
            float[] projection) {

        if (object == null ||
                !object.isActive()) {

            return new RenderResult(
                    0,
                    0
            );
        }

        float[] local =
                createLocalMatrix(
                        object
                );

        float[] world =
                multiply(
                        parentWorld,
                        local
                );

        int objects =
                0;

        int triangles =
                0;

        MeshRenderer meshRenderer =
                object.getMeshRenderer();

        if (meshRenderer != null &&
                meshRenderer.getMesh() != null) {

            int rendered =
                    renderObject(
                            meshRenderer,
                            world,
                            view,
                            projection
                    );

            if (rendered > 0) {

                objects++;

                triangles +=
                        rendered;
            }
        }

        /*
         * Hijos.
         */
        for (GameObject child :
                object.getChildren()) {

            RenderResult childResult =
                    renderHierarchy(
                            child,
                            world,
                            view,
                            projection
                    );

            objects +=
                    childResult.objects;

            triangles +=
                    childResult.triangles;
        }

        return new RenderResult(
                objects,
                triangles
        );
    }

    /*
     * =========================================================
     * OBJECT
     * =========================================================
     */

    private int renderObject(
            MeshRenderer meshRenderer,
            float[] world,
            float[] view,
            float[] projection) {

        Mesh mesh =
                meshRenderer.getMesh();

        if (mesh == null) {
            return 0;
        }

        GPUModel gpuModel =
                getGPUModel(
                        mesh
                );

        if (gpuModel == null) {
            return 0;
        }

        /*
         * VIEW * MODEL
         */
        float[] viewModel =
                multiply(
                        view,
                        world
                );

        /*
         * PROJECTION * VIEW * MODEL
         */
        float[] mvp =
                multiply(
                        projection,
                        viewModel
                );

        /*
         * MVP
         */
        glUniformMatrix4fv(
                mvpLocation,
                false,
                mvp
        );

        /*
         * MODEL
         */
        glUniformMatrix4fv(
                modelLocation,
                false,
                world
        );

        /*
         * MATERIAL
         */
        Material material =
                meshRenderer.getMaterial();

        if (material == null) {

            material =
                    new Material(
                            java.awt.Color.WHITE
                    );
        }

        java.awt.Color color =
                material.getColor();

        if (color == null) {

            color =
                    java.awt.Color.WHITE;
        }

        glUniform4f(
                colorLocation,
                color.getRed() /
                        255.0f,
                color.getGreen() /
                        255.0f,
                color.getBlue() /
                        255.0f,
                color.getAlpha() /
                        255.0f
        );

        /*
         * TEXTURA
         */
        Texture texture =
                material.getTexture();

        if (texture != null) {

            int textureId =
                    getGPUTexture(
                            texture
                    );

            if (textureId != 0) {

                glActiveTexture(
                        GL_TEXTURE0
                );

                glBindTexture(
                        GL_TEXTURE_2D,
                        textureId
                );

                glUniform1i(
                        textureLocation,
                        0
                );

                glUniform1i(
                        useTextureLocation,
                        1
                );

            } else {

                glBindTexture(
                        GL_TEXTURE_2D,
                        0
                );

                glUniform1i(
                        useTextureLocation,
                        0
                );
            }

        } else {

            glActiveTexture(
                    GL_TEXTURE0
            );

            glBindTexture(
                    GL_TEXTURE_2D,
                    0
            );

            glUniform1i(
                    useTextureLocation,
                    0
            );
        }

        /*
         * VAO
         */
        glBindVertexArray(
                gpuModel.vao
        );

        /*
         * DRAW GPU
         */
        glDrawElements(
                GL_TRIANGLES,
                gpuModel.indexCount,
                GL_UNSIGNED_INT,
                0
        );

        glBindVertexArray(
                0
        );

        /*
         * Desenlazar textura.
         */
        glBindTexture(
                GL_TEXTURE_2D,
                0
        );

        return gpuModel.indexCount / 3;
    }

    /*
     * =========================================================
     * GPU MODEL
     * =========================================================
     */

    private GPUModel getGPUModel(
            Mesh mesh) {

        GPUModel existing =
                gpuModels.get(
                        mesh
                );

        if (existing != null) {

            return existing;
        }

        if (mesh == null) {
            return null;
        }

        Vector3[] vertices =
                mesh.getVertices();

        Vector3[] normals =
                mesh.getNormals();

        Vector2[] uvs =
                mesh.getUVs();

        int[][] triangles =
                mesh.getTriangles();

        if (vertices == null ||
                vertices.length == 0 ||
                triangles == null ||
                triangles.length == 0) {

            return null;
        }

        /*
         * Compatibilidad:
         *
         * Si algún Mesh antiguo no tiene
         * normales/UV, crear datos por defecto.
         */
        if (normals == null ||
                normals.length != vertices.length) {

            normals =
                    calculateFallbackNormals(
                            vertices,
                            triangles
                    );
        }

        if (uvs == null ||
                uvs.length != vertices.length) {

            uvs =
                    createDefaultUVs(
                            vertices.length
                    );
        }

        int indexCount =
                countValidIndices(
                        vertices,
                        triangles
                );

        if (indexCount == 0) {
            return null;
        }

        int vao =
                glGenVertexArrays();

        int vbo =
                glGenBuffers();

        int ebo =
                glGenBuffers();

        try {

            glBindVertexArray(
                    vao
            );

            /*
             * =================================================
             * VERTEX BUFFER
             *
             * position = 3
             * normal   = 3
             * uv       = 2
             *
             * total = 8 floats
             * =================================================
             */

            int floatsPerVertex =
                    8;

            FloatBuffer vertexBuffer =
                    MemoryUtil.memAllocFloat(
                            vertices.length *
                                    floatsPerVertex
                    );

            try {

                for (int i = 0;
                     i < vertices.length;
                     i++) {

                    Vector3 position =
                            vertices[i];

                    Vector3 normal =
                            normals[i];

                    Vector2 uv =
                            uvs[i];

                    if (position == null) {

                        position =
                                new Vector3();
                    }

                    if (normal == null) {

                        normal =
                                new Vector3(
                                        0,
                                        1,
                                        0
                                );
                    }

                    if (uv == null) {

                        uv =
                                new Vector2(
                                        0,
                                        0
                                );
                    }

                    vertexBuffer.put(
                            (float) position.x
                    );

                    vertexBuffer.put(
                            (float) position.y
                    );

                    vertexBuffer.put(
                            (float) position.z
                    );

                    vertexBuffer.put(
                            (float) normal.x
                    );

                    vertexBuffer.put(
                            (float) normal.y
                    );

                    vertexBuffer.put(
                            (float) normal.z
                    );

                    vertexBuffer.put(
                            (float) uv.x
                    );

                    vertexBuffer.put(
                            (float) uv.y
                    );
                }

                vertexBuffer.flip();

                glBindBuffer(
                        GL_ARRAY_BUFFER,
                        vbo
                );

                glBufferData(
                        GL_ARRAY_BUFFER,
                        vertexBuffer,
                        GL_STATIC_DRAW
                );

            } finally {

                MemoryUtil.memFree(
                        vertexBuffer
                );
            }

            /*
             * =================================================
             * INDEX BUFFER
             * =================================================
             */

            IntBuffer indexBuffer =
                    MemoryUtil.memAllocInt(
                            indexCount
                    );

            try {

                for (int[] triangle :
                        triangles) {

                    if (!isValidTriangle(
                            triangle,
                            vertices.length
                    )) {

                        continue;
                    }

                    indexBuffer.put(
                            triangle[0]
                    );

                    indexBuffer.put(
                            triangle[1]
                    );

                    indexBuffer.put(
                            triangle[2]
                    );
                }

                indexBuffer.flip();

                glBindBuffer(
                        GL_ELEMENT_ARRAY_BUFFER,
                        ebo
                );

                glBufferData(
                        GL_ELEMENT_ARRAY_BUFFER,
                        indexBuffer,
                        GL_STATIC_DRAW
                );

            } finally {

                MemoryUtil.memFree(
                        indexBuffer
                );
            }

            /*
             * =================================================
             * VERTEX ATTRIBUTES
             * =================================================
             */

            int stride =
                    8 *
                            Float.BYTES;

            /*
             * Position
             *
             * location = 0
             */
            glVertexAttribPointer(
                    0,
                    3,
                    GL_FLOAT,
                    false,
                    stride,
                    0L
            );

            glEnableVertexAttribArray(
                    0
            );

            /*
             * Normal
             *
             * location = 1
             */
            glVertexAttribPointer(
                    1,
                    3,
                    GL_FLOAT,
                    false,
                    stride,
                    3L *
                            Float.BYTES
            );

            glEnableVertexAttribArray(
                    1
            );

            /*
             * UV
             *
             * location = 2
             */
            glVertexAttribPointer(
                    2,
                    2,
                    GL_FLOAT,
                    false,
                    stride,
                    6L *
                            Float.BYTES
            );

            glEnableVertexAttribArray(
                    2
            );

            glBindVertexArray(
                    0
            );

            glBindBuffer(
                    GL_ARRAY_BUFFER,
                    0
            );

            GPUModel result =
                    new GPUModel(
                            vao,
                            vbo,
                            ebo,
                            indexCount
                    );

            gpuModels.put(
                    mesh,
                    result
            );

            return result;

        } catch (RuntimeException exception) {

            glBindVertexArray(
                    0
            );

            glDeleteVertexArrays(
                    vao
            );

            glDeleteBuffers(
                    vbo
            );

            glDeleteBuffers(
                    ebo
            );

            throw exception;
        }
    }

    /*
     * =========================================================
     * GPU TEXTURE
     * =========================================================
     */

    private int getGPUTexture(
            Texture texture) {

        if (texture == null) {
            return 0;
        }

        Integer existing =
                gpuTextures.get(
                        texture
                );

        if (existing != null) {

            return existing;
        }

        BufferedImage image =
                texture.getImage();

        if (image == null) {
            return 0;
        }

        int width =
                image.getWidth();

        int height =
                image.getHeight();

        if (width <= 0 ||
                height <= 0) {

            return 0;
        }

        int[] pixels =
                new int[
                        width *
                                height
                        ];

        image.getRGB(
                0,
                0,
                width,
                height,
                pixels,
                0,
                width
        );

        ByteBuffer buffer =
                MemoryUtil.memAlloc(
                        width *
                                height *
                                4
                );

        int textureId =
                0;

        try {

            /*
             * OpenGL texture coordinates tienen
             * origen inferior.
             *
             * Por eso recorremos Y al revés.
             */
            for (int y = height - 1;
                 y >= 0;
                 y--) {

                for (int x = 0;
                     x < width;
                     x++) {

                    int pixel =
                            pixels[
                                    y *
                                            width +
                                            x
                                    ];

                    int red =
                            (
                                    pixel >>
                                            16
                            )
                                    &
                                    0xFF;

                    int green =
                            (
                                    pixel >>
                                            8
                            )
                                    &
                                    0xFF;

                    int blue =
                            pixel &
                                    0xFF;

                    int alpha =
                            (
                                    pixel >>
                                            24
                            )
                                    &
                                    0xFF;

                    buffer.put(
                            (byte) red
                    );

                    buffer.put(
                            (byte) green
                    );

                    buffer.put(
                            (byte) blue
                    );

                    buffer.put(
                            (byte) alpha
                    );
                }
            }

            buffer.flip();

            textureId =
                    glGenTextures();

            glBindTexture(
                    GL_TEXTURE_2D,
                    textureId
            );

            /*
             * Filtrado.
             */
            glTexParameteri(
                    GL_TEXTURE_2D,
                    GL_TEXTURE_MIN_FILTER,
                    GL_LINEAR_MIPMAP_LINEAR
            );

            glTexParameteri(
                    GL_TEXTURE_2D,
                    GL_TEXTURE_MAG_FILTER,
                    GL_LINEAR
            );

            /*
             * Repetición.
             */
            glTexParameteri(
                    GL_TEXTURE_2D,
                    GL_TEXTURE_WRAP_S,
                    GL_REPEAT
            );

            glTexParameteri(
                    GL_TEXTURE_2D,
                    GL_TEXTURE_WRAP_T,
                    GL_REPEAT
            );

            /*
             * Subir textura.
             */
            glTexImage2D(
                    GL_TEXTURE_2D,
                    0,
                    GL_RGBA8,
                    width,
                    height,
                    0,
                    GL_RGBA,
                    GL_UNSIGNED_BYTE,
                    buffer
            );

            /*
             * Mipmaps.
             */
            glGenerateMipmap(
                    GL_TEXTURE_2D
            );

            glBindTexture(
                    GL_TEXTURE_2D,
                    0
            );

            gpuTextures.put(
                    texture,
                    textureId
            );

            return textureId;

        } catch (RuntimeException exception) {

            if (textureId != 0) {

                glDeleteTextures(
                        textureId
                );
            }

            throw exception;

        } finally {

            MemoryUtil.memFree(
                    buffer
            );
        }
    }

    /*
     * =========================================================
     * CAMERA
     * =========================================================
     */

    private float[] createViewMatrix() {

        Vector3 eye =
                camera.getPosition();

        Vector3 forward =
                camera
                        .getForward()
                        .normalize();

        Vector3 worldUp =
                new Vector3(
                        0,
                        1,
                        0
                );

        Vector3 right =
                forward
                        .cross(
                                worldUp
                        )
                        .normalize();

        /*
         * Si la cámara estuviese exactamente
         * mirando hacia arriba, evitamos una
         * base degenerada.
         */
        if (right.length() < 0.000001) {

            right =
                    new Vector3(
                            1,
                            0,
                            0
                    );
        }

        Vector3 up =
                right
                        .cross(
                                forward
                        )
                        .normalize();

        float[] result =
                identity();

        /*
         * Columna 0
         */
        result[0] =
                (float) right.x;

        result[1] =
                (float) up.x;

        result[2] =
                (float) -forward.x;

        /*
         * Columna 1
         */
        result[4] =
                (float) right.y;

        result[5] =
                (float) up.y;

        result[6] =
                (float) -forward.y;

        /*
         * Columna 2
         */
        result[8] =
                (float) right.z;

        result[9] =
                (float) up.z;

        result[10] =
                (float) -forward.z;

        /*
         * Traslación.
         */
        result[12] =
                (float)
                        -right.dot(
                                eye
                        );

        result[13] =
                (float)
                        -up.dot(
                                eye
                        );

        result[14] =
                (float)
                        forward.dot(
                                eye
                        );

        return result;
    }

    /*
     * =========================================================
     * PROJECTION
     * =========================================================
     */

    private float[] createProjectionMatrix(
            int width,
            int height) {

        float aspect =
                (float) width /
                        (float) height;

        float fov =
                (float)
                        Math.toRadians(
                                camera.getFieldOfView()
                        );

        float near =
                (float)
                        camera.getNearPlane();

        float far =
                (float)
                        camera.getFarPlane();

        float f =
                1.0f /
                        (float)
                                Math.tan(
                                        fov / 2.0f
                                );

        float[] result =
                new float[16];

        result[0] =
                f / aspect;

        result[5] =
                f;

        result[10] =
                (
                        far + near
                )
                        /
                        (
                                near - far
                        );

        result[11] =
                -1.0f;

        result[14] =
                (
                        2.0f *
                                far *
                                near
                )
                        /
                        (
                                near - far
                        );

        result[15] =
                0.0f;

        return result;
    }

    /*
     * =========================================================
     * MODEL MATRIX
     * =========================================================
     */

    private float[] createLocalMatrix(
            GameObject object) {

        Vector3 position =
                object
                        .getTransform()
                        .getPosition();

        Vector3 rotation =
                object
                        .getTransform()
                        .getRotation();

        Vector3 scale =
                object
                        .getTransform()
                        .getScale();

        float[] translation =
                translation(
                        (float) position.x,
                        (float) position.y,
                        (float) position.z
                );

        float[] rotationX =
                rotationX(
                        (float)
                                Math.toRadians(
                                        rotation.x
                                )
                );

        float[] rotationY =
                rotationY(
                        (float)
                                Math.toRadians(
                                        rotation.y
                                )
                );

        float[] rotationZ =
                rotationZ(
                        (float)
                                Math.toRadians(
                                        rotation.z
                                )
                );

        float[] scaleMatrix =
                scale(
                        (float) scale.x,
                        (float) scale.y,
                        (float) scale.z
                );

        float[] rotationMatrix =
                multiply(
                        multiply(
                                rotationZ,
                                rotationY
                        ),
                        rotationX
                );

        return multiply(
                multiply(
                        translation,
                        rotationMatrix
                ),
                scaleMatrix
        );
    }

    /*
     * =========================================================
     * MATRIX MATH
     * =========================================================
     */

    private float[] identity() {

        float[] result =
                new float[16];

        result[0] =
                1.0f;

        result[5] =
                1.0f;

        result[10] =
                1.0f;

        result[15] =
                1.0f;

        return result;
    }

    private float[] translation(
            float x,
            float y,
            float z) {

        float[] result =
                identity();

        result[12] =
                x;

        result[13] =
                y;

        result[14] =
                z;

        return result;
    }

    private float[] scale(
            float x,
            float y,
            float z) {

        float[] result =
                identity();

        result[0] =
                x;

        result[5] =
                y;

        result[10] =
                z;

        return result;
    }

    private float[] rotationX(
            float radians) {

        float cos =
                (float)
                        Math.cos(
                                radians
                        );

        float sin =
                (float)
                        Math.sin(
                                radians
                        );

        float[] result =
                identity();

        result[5] =
                cos;

        result[6] =
                sin;

        result[9] =
                -sin;

        result[10] =
                cos;

        return result;
    }

    private float[] rotationY(
            float radians) {

        float cos =
                (float)
                        Math.cos(
                                radians
                        );

        float sin =
                (float)
                        Math.sin(
                                radians
                        );

        float[] result =
                identity();

        result[0] =
                cos;

        result[2] =
                -sin;

        result[8] =
                sin;

        result[10] =
                cos;

        return result;
    }

    private float[] rotationZ(
            float radians) {

        float cos =
                (float)
                        Math.cos(
                                radians
                        );

        float sin =
                (float)
                        Math.sin(
                                radians
                        );

        float[] result =
                identity();

        result[0] =
                cos;

        result[1] =
                sin;

        result[4] =
                -sin;

        result[5] =
                cos;

        return result;
    }

    private float[] multiply(
            float[] a,
            float[] b) {

        float[] result =
                new float[16];

        for (int column = 0;
             column < 4;
             column++) {

            for (int row = 0;
                 row < 4;
                 row++) {

                result[
                        column * 4 +
                                row
                        ] =
                        a[
                                0 * 4 +
                                        row
                                ]
                                *
                                b[
                                        column * 4 +
                                                0
                                        ]

                                +

                                a[
                                        1 * 4 +
                                                row
                                        ]
                                        *
                                        b[
                                                column * 4 +
                                                        1
                                                ]

                                +

                                a[
                                        2 * 4 +
                                                row
                                        ]
                                        *
                                        b[
                                                column * 4 +
                                                        2
                                                ]

                                +

                                a[
                                        3 * 4 +
                                                row
                                        ]
                                        *
                                        b[
                                                column * 4 +
                                                        3
                                                ];
            }
        }

        return result;
    }

    /*
     * =========================================================
     * SHADERS
     * =========================================================
     */

    private int createShaderProgram() {

        String vertexShaderSource =
                """
                #version 330 core

                layout(location = 0)
                in vec3 aPosition;

                layout(location = 1)
                in vec3 aNormal;

                layout(location = 2)
                in vec2 aUV;

                uniform mat4 uMVP;

                uniform mat4 uModel;

                out vec3 vNormal;

                out vec2 vUV;

                void main() {

                    gl_Position =
                        uMVP *
                        vec4(
                            aPosition,
                            1.0
                        );

                    vNormal =
                        normalize(
                            mat3(uModel) *
                            aNormal
                        );

                    vUV =
                        aUV;
                }
                """;

        String fragmentShaderSource =
                """
                #version 330 core

                in vec3 vNormal;

                in vec2 vUV;

                uniform vec4 uColor;

                uniform sampler2D uTexture;

                uniform bool uUseTexture;

                uniform vec3 uLightDirection;

                out vec4 fragmentColor;

                void main() {

                    vec4 baseColor =
                        uColor;

                    if (uUseTexture) {

                        baseColor *=
                            texture(
                                uTexture,
                                vUV
                            );
                    }

                    vec3 normal =
                        normalize(
                            vNormal
                        );

                    vec3 lightDirection =
                        normalize(
                            -uLightDirection
                        );

                    float diffuse =
                        max(
                            dot(
                                normal,
                                lightDirection
                            ),
                            0.0
                        );

                    float lighting =
                        0.20 +
                        diffuse * 0.80;

                    fragmentColor =
                        vec4(
                            baseColor.rgb *
                                    lighting,
                            baseColor.a
                        );
                }
                """;

        int vertexShader =
                compileShader(
                        GL_VERTEX_SHADER,
                        vertexShaderSource
                );

        int fragmentShader =
                compileShader(
                        GL_FRAGMENT_SHADER,
                        fragmentShaderSource
                );

        int program =
                glCreateProgram();

        glAttachShader(
                program,
                vertexShader
        );

        glAttachShader(
                program,
                fragmentShader
        );

        glLinkProgram(
                program
        );

        if (glGetProgrami(
                program,
                GL_LINK_STATUS
        ) == 0) {

            String log =
                    glGetProgramInfoLog(
                            program
                    );

            glDeleteShader(
                    vertexShader
            );

            glDeleteShader(
                    fragmentShader
            );

            glDeleteProgram(
                    program
            );

            throw new IllegalStateException(
                    "Error enlazando programa OpenGL:\n" +
                            log
            );
        }

        glDeleteShader(
                vertexShader
        );

        glDeleteShader(
                fragmentShader
        );

        return program;
    }

    private int compileShader(
            int type,
            String source) {

        int shader =
                glCreateShader(
                        type
                );

        glShaderSource(
                shader,
                source
        );

        glCompileShader(
                shader
        );

        if (glGetShaderi(
                shader,
                GL_COMPILE_STATUS
        ) == 0) {

            String log =
                    glGetShaderInfoLog(
                            shader
                    );

            glDeleteShader(
                    shader
            );

            throw new IllegalStateException(
                    "Error compilando shader OpenGL:\n" +
                            log
            );
        }

        return shader;
    }

    /*
     * =========================================================
     * FALLBACKS
     * =========================================================
     */

    private int countValidIndices(
            Vector3[] vertices,
            int[][] triangles) {

        int count =
                0;

        for (int[] triangle :
                triangles) {

            if (isValidTriangle(
                    triangle,
                    vertices.length
            )) {

                count += 3;
            }
        }

        return count;
    }

    private boolean isValidTriangle(
            int[] triangle,
            int vertexCount) {

        if (triangle == null ||
                triangle.length < 3) {

            return false;
        }

        int a =
                triangle[0];

        int b =
                triangle[1];

        int c =
                triangle[2];

        return
                a >= 0 &&
                        b >= 0 &&
                        c >= 0 &&
                        a < vertexCount &&
                        b < vertexCount &&
                        c < vertexCount;
    }

    private Vector2[] createDefaultUVs(
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

    private Vector3[] calculateFallbackNormals(
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

            if (!isValidTriangle(
                    triangle,
                    vertices.length
            )) {

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

            if (a == null ||
                    b == null ||
                    c == null) {

                continue;
            }

            Vector3 edge1 =
                    b.subtract(
                            a
                    );

            Vector3 edge2 =
                    c.subtract(
                            a
                    );

            Vector3 normal =
                    edge1.cross(
                            edge2
                    ).normalize();

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

    /*
     * =========================================================
     * RENDERER INTERFACE
     * =========================================================
     */

    @Override
    public Camera getCamera() {

        return camera;
    }

    @Override
    public Component getViewComponent() {

        return canvas;
    }

    @Override
    public RenderStats getRenderStats() {

        return renderStats;
    }

    @Override
    public void requestRender() {

        /*
         * Si ya hay un render en cola,
         * no añadimos otro.
         */
        if (!renderQueued.compareAndSet(
                false,
                true
        )) {

            return;
        }

        Runnable renderTask =
                () -> {

                    try {

                        if (!canvas.isValid()) {

                            return;
                        }

                        canvas.render();

                    } finally {

                        renderQueued.set(
                                false
                        );
                    }
                };

        /*
         * AWTGLCanvas debe renderizarse
         * desde el hilo de eventos AWT.
         */
        if (SwingUtilities.isEventDispatchThread()) {

            renderTask.run();

        } else {

            SwingUtilities.invokeLater(
                    renderTask
            );
        }
    }

    @Override
    public void setFrameTime(
            double frameTime) {

        /*
         * La métrica real del renderer
         * se calcula en renderFrame().
         *
         * Este método queda para mantener
         * compatible la interfaz.
         */
    }

    @Override
    public void setDebugVisible(
            boolean visible) {

        showDebug =
                visible;
    }

    @Override
    public void dispose() {

        Runnable cleanup =
                () -> {

                    if (!initialized) {
                        return;
                    }

                    /*
                     * Meshes GPU.
                     */
                    for (GPUModel model :
                            gpuModels.values()) {

                        glDeleteVertexArrays(
                                model.vao
                        );

                        glDeleteBuffers(
                                model.vbo
                        );

                        glDeleteBuffers(
                                model.ebo
                        );
                    }

                    gpuModels.clear();

                    /*
                     * Texturas GPU.
                     */
                    for (Integer textureId :
                            gpuTextures.values()) {

                        if (textureId != null) {

                            glDeleteTextures(
                                    textureId
                            );
                        }
                    }

                    gpuTextures.clear();

                    /*
                     * Shader.
                     */
                    if (shaderProgram != 0) {

                        glDeleteProgram(
                                shaderProgram
                        );

                        shaderProgram =
                                0;
                    }

                    initialized =
                            false;
                };

        if (SwingUtilities
                .isEventDispatchThread()) {

            cleanup.run();

        } else {

            SwingUtilities.invokeLater(
                    cleanup
            );
        }
    }

    @Override
    public RenderBackend getBackend() {

        return RenderBackend.OPENGL;
    }

    /*
     * =========================================================
     * GPU MODEL
     * =========================================================
     */

    private static class GPUModel {

        private final int vao;

        private final int vbo;

        private final int ebo;

        private final int indexCount;

        private GPUModel(
                int vao,
                int vbo,
                int ebo,
                int indexCount) {

            this.vao =
                    vao;

            this.vbo =
                    vbo;

            this.ebo =
                    ebo;

            this.indexCount =
                    indexCount;
        }
    }

    /*
     * =========================================================
     * RENDER RESULT
     * =========================================================
     */

    private static class RenderResult {

        private final int objects;

        private final int triangles;

        private RenderResult(
                int objects,
                int triangles) {

            this.objects =
                    objects;

            this.triangles =
                    triangles;
        }
    }
    @Override
    public void setGPUConfiguration(
            GPURenderConfig configuration
    ) {

        if (configuration == null) {
            this.gpuConfiguration =
                    GPURenderConfig.auto();
            return;
        }

        this.gpuConfiguration =
                configuration.copy();

        System.out.println(
                "[OpenGL] Configuración GPU solicitada: " +
                        this.gpuConfiguration
        );

        if (this.gpuConfiguration.isMultiGPU()) {

            System.out.println(
                    "[OpenGL] Modo multi-GPU solicitado: " +
                            this.gpuConfiguration.getMultiStrategy()
            );

            System.out.println(
                    "[OpenGL] El backend OpenGL/AWT actual " +
                            "todavía no reparte realmente el render " +
                            "entre varios contextos GPU."
            );
        }
    }

    @Override
    public GPURenderConfig getGPUConfiguration() {
        return gpuConfiguration.copy();
    }
}