package jforge.engine.rendering.gpu;

import java.util.Arrays;

public class GPURenderConfig {

    private GPUSelectionMode selectionMode;
    private int[] selectedGpuIndices;
    private GPUMultiStrategy multiStrategy;
    private boolean allowFallback;

    public GPURenderConfig() {
        this(
                GPUSelectionMode.AUTO,
                new int[0],
                GPUMultiStrategy.SPLIT_FRAME,
                true
        );
    }

    public GPURenderConfig(
            GPUSelectionMode selectionMode,
            int[] selectedGpuIndices,
            GPUMultiStrategy multiStrategy,
            boolean allowFallback
    ) {
        this.selectionMode = selectionMode != null
                ? selectionMode
                : GPUSelectionMode.AUTO;

        this.selectedGpuIndices = selectedGpuIndices != null
                ? selectedGpuIndices.clone()
                : new int[0];

        this.multiStrategy = multiStrategy != null
                ? multiStrategy
                : GPUMultiStrategy.SPLIT_FRAME;

        this.allowFallback = allowFallback;
    }

    public static GPURenderConfig auto() {
        return new GPURenderConfig(
                GPUSelectionMode.AUTO,
                new int[0],
                GPUMultiStrategy.SPLIT_FRAME,
                true
        );
    }

    public static GPURenderConfig single(int gpuIndex) {
        return new GPURenderConfig(
                GPUSelectionMode.SINGLE_GPU,
                new int[]{gpuIndex},
                GPUMultiStrategy.SPLIT_FRAME,
                true
        );
    }

    public static GPURenderConfig multi(
            int[] gpuIndices,
            GPUMultiStrategy strategy
    ) {
        return new GPURenderConfig(
                GPUSelectionMode.MULTI_GPU,
                gpuIndices,
                strategy,
                true
        );
    }

    public GPURenderConfig copy() {
        return new GPURenderConfig(
                selectionMode,
                selectedGpuIndices,
                multiStrategy,
                allowFallback
        );
    }

    public GPUSelectionMode getSelectionMode() {
        return selectionMode;
    }

    public void setSelectionMode(GPUSelectionMode selectionMode) {
        this.selectionMode = selectionMode != null
                ? selectionMode
                : GPUSelectionMode.AUTO;
    }

    public int[] getSelectedGpuIndices() {
        return selectedGpuIndices.clone();
    }

    public void setSelectedGpuIndices(int[] selectedGpuIndices) {
        this.selectedGpuIndices = selectedGpuIndices != null
                ? selectedGpuIndices.clone()
                : new int[0];
    }

    public GPUMultiStrategy getMultiStrategy() {
        return multiStrategy;
    }

    public void setMultiStrategy(GPUMultiStrategy multiStrategy) {
        this.multiStrategy = multiStrategy != null
                ? multiStrategy
                : GPUMultiStrategy.SPLIT_FRAME;
    }

    public boolean isAllowFallback() {
        return allowFallback;
    }

    public void setAllowFallback(boolean allowFallback) {
        this.allowFallback = allowFallback;
    }

    public boolean isAutomatic() {
        return selectionMode == GPUSelectionMode.AUTO;
    }

    public boolean isSingleGPU() {
        return selectionMode == GPUSelectionMode.SINGLE_GPU;
    }

    public boolean isMultiGPU() {
        return selectionMode == GPUSelectionMode.MULTI_GPU;
    }

    public int getSelectedGpuCount() {
        return selectedGpuIndices.length;
    }

    @Override
    public String toString() {
        return "GPURenderConfig{" +
                "selectionMode=" + selectionMode +
                ", selectedGpuIndices=" + Arrays.toString(selectedGpuIndices) +
                ", multiStrategy=" + multiStrategy +
                ", allowFallback=" + allowFallback +
                '}';
    }
}