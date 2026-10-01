package jforge.engine.rendering;

import jforge.engine.Scene;
import jforge.engine.rendering.opengl.OpenGLRenderer;
import jforge.engine.rendering.softwere.Renderer3D;

public final class RendererFactory {

    private RendererFactory() {
    }

    public static Renderer create(
            Scene scene,
            RenderBackend backend) {

        if (scene == null) {

            throw new IllegalArgumentException(
                    "La escena no puede ser null."
            );
        }

        if (backend == null) {

            backend =
                    RenderBackend.SOFTWARE;
        }

        switch (backend) {

            case SOFTWARE:

                return new Renderer3D(
                        scene
                );

            case OPENGL:

                return new OpenGLRenderer(
                        scene
                );

            default:

                throw new IllegalArgumentException(
                        "Backend desconocido: " +
                                backend
                );
        }
    }
}