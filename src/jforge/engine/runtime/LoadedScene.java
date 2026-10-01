package jforge.engine.runtime;

import jforge.engine.Scene;
import jforge.engine.rendering.camera.Camera;
import jforge.engine.rendering.RenderBackend;

public class LoadedScene {

    private final Scene scene;
    private final Camera camera;
    private final RenderBackend backend;

    public LoadedScene(
            Scene scene,
            Camera camera,
            RenderBackend backend
    ) {
        this.scene = scene;
        this.camera = camera;
        this.backend = backend;
    }

    public Scene getScene() {
        return scene;
    }

    public Camera getCamera() {
        return camera;
    }

    public RenderBackend getBackend() {
        return backend;
    }
}