package jforge.engine.rendering;

import jforge.engine.Scene;
import jforge.engine.math.Vector3;
import jforge.engine.rendering.camera.Camera;
import jforge.engine.rendering.gpu.GPUManager;
import jforge.engine.rendering.gpu.GPURenderConfig;

public class RendererManager {

    private final Scene scene;

    private final GPUManager gpuManager;

    private Renderer renderer;

    public RendererManager(
            Scene scene,
            RenderBackend backend
    ) {

        if (scene == null) {
            throw new IllegalArgumentException(
                    "Scene no puede ser null."
            );
        }

        this.scene = scene;
        this.gpuManager = new GPUManager();

        this.renderer = createRenderer(backend);
    }

    private Renderer createRenderer(RenderBackend backend) {

        Renderer newRenderer =
                RendererFactory.create(
                        scene,
                        backend
                );

        GPURenderConfig config =
                gpuManager.getConfiguration();

        newRenderer.setGPUConfiguration(config);

        return newRenderer;
    }

    public Renderer getRenderer() {
        return renderer;
    }

    public RenderBackend getBackend() {
        return renderer.getBackend();
    }

    public GPUManager getGPUManager() {
        return gpuManager;
    }

    public GPURenderConfig getGPUConfiguration() {
        return gpuManager.getConfiguration();
    }

    public void setGPUConfiguration(
            GPURenderConfig configuration
    ) {

        gpuManager.setConfiguration(configuration);

        if (renderer != null) {
            renderer.setGPUConfiguration(
                    gpuManager.getConfiguration()
            );
        }
    }

    public Renderer switchBackend(
            RenderBackend backend
    ) {

        if (backend == null) {
            return renderer;
        }

        if (renderer.getBackend() == backend) {
            return renderer;
        }

        Camera oldCamera =
                renderer.getCamera();

        Vector3 oldPosition =
                oldCamera.getPosition().copy();

        double oldRotationX =
                oldCamera.getRotationX();

        double oldRotationY =
                oldCamera.getRotationY();

        double oldFov =
                oldCamera.getFieldOfView();

        double oldNear =
                oldCamera.getNearPlane();

        double oldFar =
                oldCamera.getFarPlane();

        Renderer oldRenderer =
                renderer;

        Renderer newRenderer =
                createRenderer(backend);

        Camera newCamera =
                newRenderer.getCamera();

        newCamera.setPosition(
                oldPosition.x,
                oldPosition.y,
                oldPosition.z
        );

        newCamera.setRotationX(
                oldRotationX
        );

        newCamera.setRotationY(
                oldRotationY
        );

        newCamera.setFieldOfView(
                oldFov
        );

        newCamera.setNearPlane(
                oldNear
        );

        newCamera.setFarPlane(
                oldFar
        );

        renderer = newRenderer;

        oldRenderer.dispose();

        return renderer;
    }

    public void dispose() {

        if (renderer != null) {
            renderer.dispose();
            renderer = null;
        }
    }
}