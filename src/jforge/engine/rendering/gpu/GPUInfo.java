package jforge.engine.rendering.gpu;

public class GPUInfo {

    private final int index;
    private final String name;
    private final String vendor;
    private final String driverVersion;
    private final long memoryMB;
    private final boolean discrete;
    private final String detectionSource;

    public GPUInfo(
            int index,
            String name,
            String vendor,
            String driverVersion,
            long memoryMB,
            boolean discrete,
            String detectionSource
    ) {
        this.index = index;
        this.name = name != null ? name : "Unknown GPU";
        this.vendor = vendor != null ? vendor : "Unknown";
        this.driverVersion = driverVersion != null ? driverVersion : "Unknown";
        this.memoryMB = Math.max(0, memoryMB);
        this.discrete = discrete;
        this.detectionSource = detectionSource != null ? detectionSource : "Unknown";
    }

    public int getIndex() {
        return index;
    }

    public String getName() {
        return name;
    }

    public String getVendor() {
        return vendor;
    }

    public String getDriverVersion() {
        return driverVersion;
    }

    public long getMemoryMB() {
        return memoryMB;
    }

    public boolean isDiscrete() {
        return discrete;
    }

    public String getDetectionSource() {
        return detectionSource;
    }

    public String getDisplayName() {
        if (vendor.isBlank()) {
            return index + " - " + name;
        }

        return index + " - " + vendor + " " + name;
    }

    @Override
    public String toString() {
        return getDisplayName();
    }
}