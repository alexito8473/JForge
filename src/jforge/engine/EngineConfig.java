package jforge.engine;

import jforge.engine.rendering.RenderBackend;

public class EngineConfig {

    private RenderBackend renderBackend;

    public EngineConfig() {

        renderBackend =
                RenderBackend.SOFTWARE;
    }

    public RenderBackend getRenderBackend() {

        return renderBackend;
    }

    public void setRenderBackend(
            RenderBackend renderBackend) {

        if (renderBackend == null) {

            return;
        }

        this.renderBackend =
                renderBackend;
    }
}