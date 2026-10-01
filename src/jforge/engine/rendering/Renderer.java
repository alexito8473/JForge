package jforge.engine.rendering;

import jforge.engine.debug.RenderStats;
import jforge.engine.rendering.camera.Camera;
import jforge.engine.rendering.gpu.GPURenderConfig;

import java.awt.Component;

public interface Renderer {

    Camera getCamera();

    Component getViewComponent();

    RenderStats getRenderStats();

    void requestRender();

    void setFrameTime(
            double frameTime
    );

    void setDebugVisible(
            boolean visible
    );

    void dispose();

    RenderBackend getBackend();
    default void setGPUConfiguration(GPURenderConfig configuration) {
    }

    default GPURenderConfig getGPUConfiguration() {
        return GPURenderConfig.auto();
    }
}