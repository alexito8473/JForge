package jforge.engine.rendering.gpu;

public enum GPUSelectionMode {

    AUTO("Automático"),
    SINGLE_GPU("Una GPU"),
    MULTI_GPU("Varias GPUs");

    private final String displayName;

    GPUSelectionMode(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}