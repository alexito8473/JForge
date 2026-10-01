package jforge.engine.rendering.gpu;

public enum GPUMultiStrategy {

    SPLIT_FRAME(
            "Dividir imagen",
            "Cada GPU procesa una parte de la imagen final."
    ),

    SPLIT_OBJECTS(
            "Dividir objetos",
            "Cada GPU procesa diferentes grupos de objetos."
    ),

    ALTERNATE_FRAMES(
            "Frames alternos",
            "Las GPUs se alternan para renderizar frames."
    ),

    DEDICATED_TASKS(
            "GPU por tarea",
            "Cada GPU se dedica a determinadas tareas del pipeline."
    );

    private final String displayName;
    private final String description;

    GPUMultiStrategy(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return displayName;
    }
}